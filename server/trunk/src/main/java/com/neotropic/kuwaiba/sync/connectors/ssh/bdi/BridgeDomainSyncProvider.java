/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.neotropic.kuwaiba.sync.connectors.ssh.bdi;

import com.neotropic.kuwaiba.sync.connectors.ssh.bdi.parsers.BridgeDomainsASR920Parser;
import com.neotropic.kuwaiba.sync.model.AbstractDataEntity;
import com.neotropic.kuwaiba.sync.model.AbstractSyncProvider;
import com.neotropic.kuwaiba.sync.model.PollResult;
import com.neotropic.kuwaiba.sync.model.SyncAction;
import com.neotropic.kuwaiba.sync.model.SyncDataSourceConfiguration;
import com.neotropic.kuwaiba.sync.model.SyncFinding;
import com.neotropic.kuwaiba.sync.model.SyncResult;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.util.i18n.I18N;

/**
 * This provider connects to Cisco routers via SSH, retrieves the bridge domain configuration, and creates/updates the relationships between
 * the bridge domains and the logical/physical 
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class BridgeDomainSyncProvider extends AbstractSyncProvider {

    @Override
    public String getDisplayName() {
        return "Bridge Domains and Bridge Domain Interfaces Sync Provider";
    }

    @Override
    public String getId() {
        return "BridgeDomainSyncProvider";
    }

    @Override
    public boolean isAutomated() {
        return true;
    }

    @Override
    public List<AbstractDataEntity> unmappedPoll(SynchronizationGroup syncGroup) {
        throw new UnsupportedOperationException("This provider does not support unmapped polling");
    }

    @Override
    public PollResult mappedPoll(SynchronizationGroup syncGroup) {
        List<SyncDataSourceConfiguration> syncDataSourceConfigurations = syncGroup.getSyncDataSourceConfigurations();
        
        final SSHClient ssh = new SSHClient();
        PollResult res = new PollResult();
        
        for (SyncDataSourceConfiguration dataSourceConfiguration : syncDataSourceConfigurations) {
            Session session = null;
            long deviceId;
            int port;
            String className, host, user, password;
            
            if (dataSourceConfiguration.getParameters().containsKey("deviceId")) //NOI18N
                deviceId = Long.valueOf(dataSourceConfiguration.getParameters().get("deviceId")); //NOI18N
            else {
                res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_not_defined"), "deviceId", syncGroup.getName())));
                continue;
            }
            
            if (dataSourceConfiguration.getParameters().containsKey("deviceClass")) //NOI18N
                className = dataSourceConfiguration.getParameters().get("deviceClass"); //NOI18N
            else {
                res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_not_defined"), "deviceClass", syncGroup.getName()))); //NOI18N
                continue;
            }
            
            if (dataSourceConfiguration.getParameters().containsKey("ipAddress")) //NOI18N
                host = dataSourceConfiguration.getParameters().get("host"); //NOI18N
            else {
                res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_not_defined"), "ipAddress", syncGroup.getName()))); //NOI18N
                continue;
            }
            
            if (dataSourceConfiguration.getParameters().containsKey("port")) //NOI18N
                port = Integer.valueOf(dataSourceConfiguration.getParameters().get("port")); //NOI18N
            else {
                res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_not_defined"), "port", syncGroup.getName())));
                continue;
            }
            
            if (dataSourceConfiguration.getParameters().containsKey("user")) //NOI18N
                user = dataSourceConfiguration.getParameters().get("user"); //NOI18N
            else {
                res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_not_defined"), "user", syncGroup.getName()))); //NOI18N
                continue;
            }
            
            if (dataSourceConfiguration.getParameters().containsKey("password")) //NOI18N
                password = dataSourceConfiguration.getParameters().get("password"); //NOI18N
            else {
                res.getSyncDataSourceConfigurationExceptions(dataSourceConfiguration).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_not_defined"), "password", syncGroup.getName()))); //NOI18N
                continue;
            }
            
            try {
                ssh.connect(host, port);
                ssh.loadKnownHosts();
                ssh.authPassword(user, password);
                session = ssh.startSession();
                final Session.Command cmd = session.exec("/home/lulita/bridge-domain.sh");
                
                BridgeDomainsASR920Parser parser = new BridgeDomainsASR920Parser();               
                
                cmd.join(5, TimeUnit.SECONDS);
                if (cmd.getExitStatus() != 0) {
                    res.getExceptions().put(dataSourceConfiguration, Arrays.asList(new InvalidArgumentException(cmd.getExitErrorMessage())));
                } else 
                    res.getResult().put(new BusinessObjectLight(className, deviceId, ""), 
                            parser.parse(IOUtils.readFully(cmd.getInputStream()).toString()));
                
                
            } catch (Exception ex) {
                res.getExceptions().put(dataSourceConfiguration, Arrays.asList(ex));
            } finally {
                try {
                    if (session != null)
                        session.close();
                    ssh.disconnect();
                } catch (IOException e) { }
            }
        }
        return res;
    }

    @Override
    public List<SyncResult> automatedSync(PollResult pollResult) {
        List<SyncResult> res = new ArrayList<>();
        for (BusinessObjectLight relatedOject : pollResult.getResult().keySet())
            res.add(new SyncResult(SyncResult.SUCCESS, "Added to " + relatedOject, "Interfaces: " + pollResult.getResult().get(relatedOject).size()));
        
        return res;
    }

    @Override
    public List<SyncResult> automatedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support automated sync for unmapped pollings");
    }
    
    @Override
    public List<SyncFinding> supervisedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support supervised sync");
    }
    
    @Override
    public List<SyncFinding> supervisedSync(PollResult pollResult) {
        throw new UnsupportedOperationException("This provider does not support supervised sync");
    }

    @Override
    public List<String> finalize(List<SyncAction> actions) {
        return new ArrayList<>();
    }

}

/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package com.neotropic.kuwaiba.sync.connectors.snmp.bgp;

import com.neotropic.kuwaiba.sync.connectors.snmp.SnmpManager;
import com.neotropic.kuwaiba.sync.model.AbstractDataEntity;
import com.neotropic.kuwaiba.sync.model.AbstractSyncProvider;
import com.neotropic.kuwaiba.sync.model.PollResult;
import com.neotropic.kuwaiba.sync.model.SyncAction;
import com.neotropic.kuwaiba.sync.model.SyncDataSourceConfiguration;
import com.neotropic.kuwaiba.sync.model.SyncFinding;
import com.neotropic.kuwaiba.sync.model.SyncResult;
import com.neotropic.kuwaiba.sync.model.SyncUtil;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import com.neotropic.kuwaiba.sync.model.TableData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ConnectionException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.util.i18n.I18N;
import org.snmp4j.smi.OID;

/**
 * This provider sync the BGP info from routers
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class BgpSyncProvider extends AbstractSyncProvider{
    @Override
    public String getDisplayName() {
        //BGP Map
        return "Border Gateway Protocol";
    }

    @Override
    public String getId() {
        return BgpSyncProvider.class.getName();
    }

    @Override
    public boolean isAutomated() {
        return true;
    }

    @Override
    public List<AbstractDataEntity> unmappedPoll(SynchronizationGroup syncGroup) {
        throw new UnsupportedOperationException("This provider does not support unmapped polling"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PollResult mappedPoll(SynchronizationGroup syncGroup) {
        PollResult pollResult = new PollResult();
        
        for (SyncDataSourceConfiguration agent : syncGroup.getSyncDataSourceConfigurations()) {


            if (!agent.getParameters().containsKey("deviceId")) { //NOI18N
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_deviceId_no_defined"), syncGroup.getName(), syncGroup.getId())));
                continue;
            }
            if (!agent.getParameters().containsKey("deviceClass")) { //NOI18N
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_deviceClass_no_defined"), syncGroup.getName(), syncGroup.getId())));
                    continue;
            }
            if (!agent.getParameters().containsKey("ipAddress")) { //NOI18N
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_ipAddress_no_defined"), syncGroup.getName(), syncGroup.getId())));
                continue;
            }

            if (!agent.getParameters().containsKey("port")) { //NOI18N 
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_port_no_defined"), syncGroup.getName(), syncGroup.getId())));
                continue;
            }
            
            if (!agent.getParameters().containsKey(Constants.PROPERTY_SNMP_VERSION)) {
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_snmp_version_no_defined"), syncGroup.getName(), syncGroup.getId())));
                continue;
            }
            
            String snmpVersion =  agent.getParameters().get(Constants.PROPERTY_SNMP_VERSION);
            
            if (SnmpManager.VERSION_2C.equals(snmpVersion)) {
                if (!agent.getParameters().containsKey(Constants.PROPERTY_COMMUNITY)) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_community_no_defined"), syncGroup.getName(), syncGroup.getId())));
                    continue;
                }
            }
            
            if (SnmpManager.VERSION_3.equals(snmpVersion)) {
                if (!agent.getParameters().containsKey(Constants.PROPERTY_AUTH_PROTOCOL)) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_auth_protocol_no_defined"), syncGroup.getName(), syncGroup.getId())));
                    continue;
                }
                    
                if (!agent.getParameters().containsKey(Constants.PROPERTY_SECURITY_NAME)) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_security_name_no_defined"), syncGroup.getName(), syncGroup.getId())));
                    continue;
                }
            }

            try {
                BusinessObjectLight mappedObjLight = PersistenceService.getInstance().
                        getBusinessEntityManager().getObjectLight(agent.getParameters().get("deviceClass"), agent.getParameters().get("deviceId"));
                SnmpManager snmpManager = SnmpManager.getInstance();

                snmpManager.setAddress(String.format("udp:%s/%s", agent.getParameters().get("ipAddress"), agent.getParameters().get("port"))); //NOI18N
                snmpManager.setVersion(snmpVersion);

                if (SnmpManager.VERSION_2C.equals(snmpVersion))
                    snmpManager.setCommunity(agent.getParameters().get(Constants.PROPERTY_COMMUNITY));

                if (SnmpManager.VERSION_3.equals(snmpVersion)) {
                    snmpManager.setAuthProtocol(agent.getParameters().get(Constants.PROPERTY_AUTH_PROTOCOL));
                    snmpManager.setAuthPass(agent.getParameters().get(Constants.PROPERTY_AUTH_PASS));
                    snmpManager.setSecurityLevel(agent.getParameters().get(Constants.PROPERTY_SECURITY_LEVEL));
                    snmpManager.setContextName(agent.getParameters().get(Constants.PROPERTY_CONTEXT_NAME));
                    snmpManager.setSecurityName(agent.getParameters().get(Constants.PROPERTY_SECURITY_NAME));
                    snmpManager.setPrivacyProtocol(agent.getParameters().get(Constants.PROPERTY_PRIVACY_PROTOCOL));
                    snmpManager.setPrivacyPass(agent.getParameters().get(Constants.PROPERTY_PRIVACY_PASS));
                }
                //ipAddrTable table
                SnmpBgpResourceDefinition bgpTable = new SnmpBgpResourceDefinition();
                List<List<String>> tableAsString = snmpManager.getTableAsString(bgpTable.values().toArray(new OID[0]));

                if (tableAsString == null) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new ConnectionException(String.format(I18N.gm("snmp_agent_connection_exception"), mappedObjLight.toString())));
                    return pollResult;
                }

                pollResult.getResult().put(agent, new ArrayList<>());
                pollResult.getResult().get(agent).add(
                        new TableData("bgpTable", SyncUtil.parseMibTable("instance", bgpTable, tableAsString))); //NOI18N
                //
                SnmpBgpLocalResourceDefinition bgpLocalTable = new SnmpBgpLocalResourceDefinition();
                List<List<String>> bgpLocalTableAsString = snmpManager.getTableAsString(bgpLocalTable.values().toArray(new OID[0]));

                if (bgpLocalTableAsString == null) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new ConnectionException(String.format(I18N.gm("snmp_agent_connection_exception"), mappedObjLight.toString())));
                    return pollResult;
                }

                pollResult.getResult().get(agent).add(
                        new TableData("bgpLocalTable", SyncUtil.parseMibTable("instance", bgpLocalTable, bgpLocalTableAsString))); //NOI18N
            } catch(InventoryException ex) {
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("snmp_sync_object_not_found"), ex.getMessage())));
            }
        }
        return pollResult;
    }

    @Override
    public List<SyncFinding> supervisedSync(PollResult pollResult){
        throw new UnsupportedOperationException("This provider does not support supervised sync for unmapped pollings");
    }

    @Override
    public List<SyncFinding> supervisedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public List<SyncResult> automatedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support automated sync");
    }

    @Override
    public  List<SyncResult> automatedSync(PollResult pollResult) {
        HashMap<SyncDataSourceConfiguration, List<AbstractDataEntity>> originalData = pollResult.getResult();
        List<SyncResult> res = new ArrayList<>();
        // Adding to findings list the not blocking execution exception found during the mapped poll
        for (SyncDataSourceConfiguration agent : pollResult.getExceptions().keySet()) {
            for (Exception ex : pollResult.getExceptions().get(agent))
                res.add(new SyncResult(agent.getId(), SyncFinding.EVENT_ERROR, String.format("Severe error while processing data source configuration %s", agent.getName()), ex.getLocalizedMessage()));
        }
        for (Map.Entry<SyncDataSourceConfiguration, List<AbstractDataEntity>> entrySet : originalData.entrySet()) {
            List<TableData> mibTables = new ArrayList<>();
            entrySet.getValue().forEach((value) -> {
                mibTables.add((TableData)value);
            });
            BGPSynchronizer ipSync = new BGPSynchronizer(entrySet.getKey().getId(),
                    new BusinessObjectLight(entrySet.getKey().getParameters().get("deviceClass"), 
                    entrySet.getKey().getParameters().get("deviceId"), ""), 
                    mibTables);
            res.addAll(ipSync.execute());
        }
        return res;
    }

    @Override
    public List<SyncResult> finalize(List<SyncAction> actions) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    } 
}

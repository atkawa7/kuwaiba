/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
package com.neotropic.kuwaiba.sync.connectors.snmp.reference;

import com.neotropic.kuwaiba.sync.connectors.snmp.SnmpManager;
import com.neotropic.kuwaiba.sync.model.AbstractDataEntity;
import com.neotropic.kuwaiba.sync.model.AbstractSyncProvider;
import com.neotropic.kuwaiba.sync.model.PollResult;
import com.neotropic.kuwaiba.sync.model.SyncAction;
import com.neotropic.kuwaiba.sync.model.SyncDataSourceConfiguration;
import com.neotropic.kuwaiba.sync.model.SyncFinding;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import com.neotropic.kuwaiba.sync.model.TableData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ConnectionException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.utils.i18n.I18N;
import org.snmp4j.smi.OID;

/**
 * Synchronization provider to SNMP agents
 * This class implement the logic to connect with a group of SNMP agents to 
 * retrieve the data and compare the differences with the management objects
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ReferenceSnmpSyncProvider extends AbstractSyncProvider {

    @Override
    public String getName() {
        return "Reference SNMP Synchronization Provider";
    }

    @Override
    public String getId() {
        return ReferenceSnmpSyncProvider.class.getName();
    }
    
    @Override
    public boolean isAutomated() {
        return false;
    }
    
    @Override
    public List<AbstractDataEntity> unmappedPoll(SynchronizationGroup syncGroup) {
        throw new UnsupportedOperationException("This provider does not support unmapped polling");
    }

    @Override
    public PollResult mappedPoll(SynchronizationGroup syncGroup) {            
        PollResult pollResult = new PollResult();

        for (SyncDataSourceConfiguration agent : syncGroup.getSyncDataSourceConfigurations()) {
            long id = -1L;
            String className = null;                
            String address = null;
            String port = null;
            String community = null;

            if (agent.getParameters().containsKey("deviceId")) //NOI18N
                id = Long.valueOf(agent.getParameters().get("deviceId")); //NOI18N
            else 
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_deviceId_no_defined"), syncGroup.getName(), syncGroup.getId())));

            if (agent.getParameters().containsKey("deviceClass")) //NOI18N
                className = agent.getParameters().get("deviceClass"); //NOI18N
            else
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_deviceClass_no_defined"), syncGroup.getName(), syncGroup.getId())));

            if (agent.getParameters().containsKey("ipAddress")) //NOI18N
                address = agent.getParameters().get("ipAddress"); //NOI18N
            else 
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_ipAddress_no_defined"), syncGroup.getName(), syncGroup.getId())));

            if (agent.getParameters().containsKey("port")) //NOI18N 
                port = agent.getParameters().get("port"); //NOI18N
            else 
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_port_no_defined"), syncGroup.getName(), syncGroup.getId())));

            String version = SnmpManager.VERSION_2c;
            if (agent.getParameters().containsKey(Constants.PROPERTY_SNMP_VERSION))
                version = agent.getParameters().get(Constants.PROPERTY_SNMP_VERSION);
//            else
//                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
//                    new InvalidArgumentException(String.format(I18N.gm("parameter_snmp_version_no_defined"), syncGroup.getName(), syncGroup.getId())));

            if (SnmpManager.VERSION_2c.equals(version)) {
                if (!agent.getParameters().containsKey(Constants.PROPERTY_COMMUNITY))
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_community_no_defined"), syncGroup.getName(), syncGroup.getId())));
            }
            if (SnmpManager.VERSION_3.equals(version)) {
                if (!agent.getParameters().containsKey(Constants.PROPERTY_AUTH_PROTOCOL))
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_auth_protocol_no_defined"), syncGroup.getName(), syncGroup.getId())));
                
                if (!agent.getParameters().containsKey(Constants.PROPERTY_SECURITY_NAME))
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_security_name_no_defined"), syncGroup.getName(), syncGroup.getId())));
            }

            if (pollResult.getSyncDataSourceConfigurationExceptions(agent).isEmpty()) {

                RemoteBusinessObjectLight mappedObjLight = null;

                try {
                    mappedObjLight = PersistenceService.getInstance().getBusinessEntityManager().getObjectLight(className, id);
                } catch(InventoryException ex) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(I18N.gm("snmp_sync_object_not_found"), ex.getMessage())));
                }
                if (mappedObjLight != null) {
                    SnmpManager snmpManager = SnmpManager.getInstance();

                    snmpManager.setAddress(String.format("udp:%s/%s", address, port)); //NOI18N
                    snmpManager.setVersion(version);

                    if (SnmpManager.VERSION_2c.equals(version))
                        snmpManager.setCommunity(agent.getParameters().get(Constants.PROPERTY_COMMUNITY));

                    if (SnmpManager.VERSION_3.equals(version)) {
                        snmpManager.setAuthProtocol(agent.getParameters().get(Constants.PROPERTY_AUTH_PROTOCOL));
                        snmpManager.setAuthPass(agent.getParameters().get(Constants.PROPERTY_AUTH_PASS));
                        snmpManager.setSecurityLevel(agent.getParameters().get(Constants.PROPERTY_SECURITY_LEVEL));
                        snmpManager.setContextName(agent.getParameters().get(Constants.PROPERTY_CONTEXT_NAME));
                        snmpManager.setSecurityName(agent.getParameters().get(Constants.PROPERTY_SECURITY_NAME));
                        snmpManager.setPrivacyProtocol(agent.getParameters().get(Constants.PROPERTY_PRIVACY_PROTOCOL));
                        snmpManager.setPrivacyPass(agent.getParameters().get(Constants.PROPERTY_PRIVACY_PASS));
                    }
                    ReferenceSnmpResourceDefinition entPhysicalTable = new ReferenceSnmpResourceDefinition();
                    List<List<String>> tableAsString = snmpManager.getTableAsString(entPhysicalTable.values().toArray(new OID[0]));

                    if (tableAsString == null) {
                        pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                            new ConnectionException(String.format(I18N.gm("snmp_agent_connection_exception"), mappedObjLight.toString())));
                        return pollResult;
                    }
                    HashMap<String, List<String>> value = new HashMap();
                    int i = 0;
                    for (String mibTreeNodeName : entPhysicalTable.keySet()) {                        
                        List<String> currentColumn = new ArrayList();

                        for (List<String> cell : tableAsString)
                            currentColumn.add(cell.get(i));

                        value.put(mibTreeNodeName, currentColumn);
                        i++;                            
                    }
                    int size = entPhysicalTable.keySet().size();
                    List<String> instances = new ArrayList();
                    for (List<String> cell : tableAsString)
                        instances.add(cell.get(size));
                    value.put("instance", instances); //NOI18N

                    pollResult.getResult().put(mappedObjLight, new TableData("entPhysicalTable", value)); //NOI18N
                }
            }
        }
        return pollResult;
    }
    
    @Override
    public List<SyncFinding> sync(PollResult pollResult) throws Exception {
        HashMap<RemoteBusinessObjectLight, AbstractDataEntity> originalData = pollResult.getResult();
        List<SyncFinding> findings = new ArrayList<>();
        // Adding to findings list the not blocking execution exception found during the mapped poll
        for (SyncDataSourceConfiguration agent : pollResult.getExceptions().keySet()) {
            for (Exception exception : pollResult.getExceptions().get(agent))
                findings.add(new SyncFinding(SyncFinding.EVENT_ERROR, 
                        exception.getMessage(), 
                        Json.createObjectBuilder().add("type","ex").build().toString()));
        }
        for (Map.Entry<RemoteBusinessObjectLight, AbstractDataEntity> entrySet : originalData.entrySet()) {
            TableData table = (TableData)entrySet.getValue();
            SNMPDataProcessor x = new SNMPDataProcessor(entrySet.getKey(), (HashMap<String, List<String>>)table.getValue());
            try {
                findings.addAll(x.load());
            } catch (MetadataObjectNotFoundException | ObjectNotFoundException | InvalidArgumentException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
                throw new Exception(ex.getMessage());
            }
        }
        return findings;
    }

    @Override
    public List<SyncFinding> sync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support unmapped polling");
    }

    @Override
    public List<String> finalize(List<SyncAction> actions) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

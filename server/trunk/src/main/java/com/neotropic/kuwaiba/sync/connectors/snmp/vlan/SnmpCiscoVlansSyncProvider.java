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
package com.neotropic.kuwaiba.sync.connectors.snmp.vlan;

import com.neotropic.kuwaiba.sync.connectors.snmp.SnmpManager;
import com.neotropic.kuwaiba.sync.connectors.snmp.reference.SnmpifXTableResocurceDefinition;
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
 *
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SnmpCiscoVlansSyncProvider extends AbstractSyncProvider{
    
    @Override
    public String getDisplayName() {
        return "Cisco - vlanTrunkPortTable SNMP Synchronization Provider";
    }

    @Override
    public String getId() {
        return SnmpCiscoVlansSyncProvider.class.getName();
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
        PollResult pollResult = new PollResult();
        
        for (SyncDataSourceConfiguration agent : syncGroup.getSyncDataSourceConfigurations()) {
            long id = -1L;
            String className = null;                
            String address = null;
            String port = null;
            //String community = null;

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
                BusinessObjectLight mappedObjLight = null;
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
                    //VlanTrunkPortsTable
                    SnmpVlanTrunkPortsTableResourceDefinition VlanTrunkPortsTable = new SnmpVlanTrunkPortsTableResourceDefinition();
                    List<List<String>> vlansMibTableAsString = snmpManager.getTableAsString(VlanTrunkPortsTable.values().toArray(new OID[0]));
                    
                    if (vlansMibTableAsString == null) {
                        pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                            new ConnectionException(String.format(I18N.gm("snmp_agent_connection_exception"), mappedObjLight.toString())));
                        return pollResult;
                    }
                    pollResult.getResult().put(mappedObjLight, new ArrayList<>());
                    pollResult.getResult().get(mappedObjLight).add(
                            new TableData("vlansMibTable", SyncUtil.parseMibTable("instance", VlanTrunkPortsTable, vlansMibTableAsString))); //NOI18N
                    //ifXTable
                    SnmpifXTableResocurceDefinition ifXTable = new SnmpifXTableResocurceDefinition();
                    List<List<String>> ifXTableAsString = snmpManager.getTableAsString(ifXTable.values().toArray(new OID[0]));
                    
                    if (ifXTableAsString == null) {
                        pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                            new ConnectionException(String.format(I18N.gm("snmp_agent_connection_exception"), mappedObjLight.toString())));
                        return pollResult;
                    }
                    
                    pollResult.getResult().get(mappedObjLight).add(
                            new TableData("ifXTable", SyncUtil.parseMibTable("instance", ifXTable, ifXTableAsString))); //NOI18N
                    //VlanInfo
                    SnmpVtpVlanTableResourceDefinition vlanInfo = new SnmpVtpVlanTableResourceDefinition();
                    List<List<String>> vlanInfoAsString = snmpManager.getTableAsString(vlanInfo.values().toArray(new OID[0]));
                    
                    if (vlanInfoAsString == null) {
                        pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                            new ConnectionException(String.format(I18N.gm("snmp_agent_connection_exception"), mappedObjLight.toString())));
                        return pollResult;
                    }
                    
                    pollResult.getResult().get(mappedObjLight).add(
                            new TableData("vlanInfo", SyncUtil.parseMibTable("instance", vlanInfo, vlanInfoAsString))); //NOI18N

                    //vmMemberShipTable
                    SnmpvmMembershipTableResourceDefinition vmMembershipTable = new SnmpvmMembershipTableResourceDefinition();
                    List<List<String>> vmMembershipTableAsString = snmpManager.getTableAsString(vmMembershipTable.values().toArray(new OID[0]));
                    
                    if (vmMembershipTableAsString == null) {
                        pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                            new ConnectionException(String.format(I18N.gm("snmp_agent_connection_exception"), mappedObjLight.toString())));
                        return pollResult;
                    }
                    
                    pollResult.getResult().get(mappedObjLight).add(
                            new TableData("vmMembershipTable", SyncUtil.parseMibTable("instance", vmMembershipTable, vmMembershipTableAsString))); //NOI18N
                      
                }
            }
        }
        return pollResult;
    }
    
    @Override
    public List<SyncFinding> supervisedSync(PollResult pollResult){
        throw new UnsupportedOperationException("This provider does not support automated sync");
    }

    @Override
    public List<SyncFinding> supervisedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support unmapped polling");
    }
    
    @Override
    public List<SyncResult> automatedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("This provider does not support supervised sync for unmapped pollings");
    }

    @Override
    public List<SyncResult> automatedSync(PollResult pollResult) {
        List<SyncResult> res = new ArrayList<>();
        HashMap<BusinessObjectLight, List<AbstractDataEntity>> originalData = pollResult.getResult();
        // Adding to result list the not blocking execution exception found during the mapped poll
        for (SyncDataSourceConfiguration agent : pollResult.getExceptions().keySet()) {
            for (Exception ex : pollResult.getExceptions().get(agent))
                res.add(new SyncResult(SyncFinding.EVENT_ERROR, String.format("Severe error while processing data source configuration %s", agent.getName()), ex.getLocalizedMessage()));
        }
        for (Map.Entry<BusinessObjectLight, List<AbstractDataEntity>> entrySet : originalData.entrySet()) {
            List<TableData> mibTables = new ArrayList<>();
            entrySet.getValue().forEach((value) -> {
                mibTables.add((TableData)value);
            });
            CiscoVlansSinchronizer ciscoSync = new CiscoVlansSinchronizer(entrySet.getKey(), mibTables);
            res.addAll(ciscoSync.execute());
        }
        return res;
    }

    @Override
    public List<SyncResult> finalize(List<SyncAction> actions) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

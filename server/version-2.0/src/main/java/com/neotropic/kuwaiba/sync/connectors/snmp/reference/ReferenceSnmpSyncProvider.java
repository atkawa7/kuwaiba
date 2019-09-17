/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import com.neotropic.kuwaiba.sync.model.SyncResult;
import com.neotropic.kuwaiba.sync.model.SyncUtil;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import com.neotropic.kuwaiba.sync.model.TableData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.ConnectionException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.util.i18n.I18N;
import org.openide.util.Exceptions;
import org.snmp4j.smi.OID;

/**
 * Synchronization provider to SNMP agents
 * This class implement the logic to connect with a group of SNMP agents to 
 * retrieve the data and compare the differences with the management objects
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ReferenceSnmpSyncProvider extends AbstractSyncProvider {
        
    @Override
    public String getDisplayName() {
        //Reference SNMP Synchronization Provider
        return "Physical / Virtual Interfaces";
    }

    @Override
    public String getId() {
        return ReferenceSnmpSyncProvider.class.getName();
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
        
        for (SyncDataSourceConfiguration dsConfig : syncGroup.getSyncDataSourceConfigurations()) {
            String id = null;
            String className = null;                
            String address = null;
            String port = null;
            //String community = null;

            if (dsConfig.getParameters().containsKey("deviceId")) //NOI18N
                id = dsConfig.getParameters().get("deviceId"); //NOI18N
            else 
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_deviceId_no_defined"), syncGroup.getName(), syncGroup.getId())));

            if (dsConfig.getParameters().containsKey("deviceClass")) //NOI18N
                className = dsConfig.getParameters().get("deviceClass"); //NOI18N
            else
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_deviceClass_no_defined"), syncGroup.getName(), syncGroup.getId())));

            if (dsConfig.getParameters().containsKey("ipAddress")) //NOI18N
                address = dsConfig.getParameters().get("ipAddress"); //NOI18N
            else 
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_ipAddress_no_defined"), syncGroup.getName(), syncGroup.getId())));

            if (dsConfig.getParameters().containsKey("port")) //NOI18N 
                port = dsConfig.getParameters().get("port"); //NOI18N
            else 
                pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_port_no_defined"), syncGroup.getName(), syncGroup.getId())));

            String version = SnmpManager.VERSION_2C;
            if (dsConfig.getParameters().containsKey(Constants.PROPERTY_SNMP_VERSION))
                version = dsConfig.getParameters().get(Constants.PROPERTY_SNMP_VERSION);

            if (SnmpManager.VERSION_2C.equals(version)) {
                if (!dsConfig.getParameters().containsKey(Constants.PROPERTY_COMMUNITY))
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_community_no_defined"), syncGroup.getName(), syncGroup.getId())));
            }
            if (SnmpManager.VERSION_3.equals(version)) {
                if (!dsConfig.getParameters().containsKey(Constants.PROPERTY_AUTH_PROTOCOL))
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_auth_protocol_no_defined"), syncGroup.getName(), syncGroup.getId())));
                
                if (!dsConfig.getParameters().containsKey(Constants.PROPERTY_SECURITY_NAME))
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_security_name_no_defined"), syncGroup.getName(), syncGroup.getId())));
            }

            if (pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).isEmpty()) {

                BusinessObjectLight mappedObjLight = null;

                try {
                    mappedObjLight = PersistenceService.getInstance().getBusinessEntityManager().getObjectLight(className, id);
                } catch(InventoryException ex) {
                    pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                        new InvalidArgumentException(String.format(I18N.gm("snmp_sync_object_not_found"), ex.getMessage())));
                }
                if (mappedObjLight != null) {
                    SnmpManager snmpManager = SnmpManager.getInstance();

                    snmpManager.setAddress(String.format("udp:%s/%s", address, port)); //NOI18N
                    snmpManager.setVersion(version);

                    if (SnmpManager.VERSION_2C.equals(version))
                        snmpManager.setCommunity(dsConfig.getParameters().get(Constants.PROPERTY_COMMUNITY));

                    if (SnmpManager.VERSION_3.equals(version)) {
                        snmpManager.setAuthProtocol(dsConfig.getParameters().get(Constants.PROPERTY_AUTH_PROTOCOL));
                        snmpManager.setAuthPass(dsConfig.getParameters().get(Constants.PROPERTY_AUTH_PASS));
                        snmpManager.setSecurityLevel(dsConfig.getParameters().get(Constants.PROPERTY_SECURITY_LEVEL));
                        snmpManager.setContextName(dsConfig.getParameters().get(Constants.PROPERTY_CONTEXT_NAME));
                        snmpManager.setSecurityName(dsConfig.getParameters().get(Constants.PROPERTY_SECURITY_NAME));
                        snmpManager.setPrivacyProtocol(dsConfig.getParameters().get(Constants.PROPERTY_PRIVACY_PROTOCOL));
                        snmpManager.setPrivacyPass(dsConfig.getParameters().get(Constants.PROPERTY_PRIVACY_PASS));
                    }
                    //ENTITY-MIB table
                    ReferenceSnmpEntPhysicalTableResourceDefinition entPhysicalTable = new ReferenceSnmpEntPhysicalTableResourceDefinition();
                    List<List<String>> tableAsString = snmpManager.getTableAsString(entPhysicalTable.values().toArray(new OID[0]));
                        
                    if (tableAsString == null) {
                        pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                            new ConnectionException(String.format(I18N.gm("snmp_agent_connection_exception"), mappedObjLight.toString())));
                        return pollResult;
                    }
                    
                    pollResult.getResult().put(dsConfig, new ArrayList<>());
                    pollResult.getResult().get(dsConfig).add(
                            new TableData("entPhysicalTable", SyncUtil.parseMibTable("instance", entPhysicalTable, tableAsString))); //NOI18N
                
                    //IF_MIB
                    SnmpifXTableResocurceDefinition ifMibTable = new SnmpifXTableResocurceDefinition();
                    List<List<String>> ifMibTableAsString = snmpManager.getTableAsString(ifMibTable.values().toArray(new OID[0]));

                    if (ifMibTableAsString == null) {
                        pollResult.getSyncDataSourceConfigurationExceptions(dsConfig).add(
                            new ConnectionException(String.format(I18N.gm("snmp_agent_connection_exception"), mappedObjLight.toString())));
                        return pollResult;
                    }
                    
                    pollResult.getResult().get(dsConfig).add(
                            new TableData("ifMibTable", SyncUtil.parseMibTable("instance", ifMibTable, ifMibTableAsString))); //NOI18N
                }
            }
        }
        return pollResult;
    }
    
    @Override
    public List<SyncFinding> supervisedSync(PollResult pollResult) {
         throw new UnsupportedOperationException("This provider does not support unmapped polling");
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
        HashMap<SyncDataSourceConfiguration, List<AbstractDataEntity>> originalData = pollResult.getResult();
        List<SyncResult> results = new ArrayList<>();
        // Adding to findings list the not blocking execution exception found during the mapped poll
        for (SyncDataSourceConfiguration agent : pollResult.getExceptions().keySet()) {
            for (Exception exception : pollResult.getExceptions().get(agent))
                results.add(new SyncResult(agent.getId(), SyncFinding.EVENT_ERROR, 
                        exception.getMessage(), 
                        Json.createObjectBuilder().add("type","ex").build().toString()));
        }
        for (Map.Entry<SyncDataSourceConfiguration, List<AbstractDataEntity>> entrySet : originalData.entrySet()) {
            List<TableData> mibTables = new ArrayList<>();
            entrySet.getValue().forEach((value) -> {
                mibTables.add((TableData)value);
            });
            
            EntPhysicalSynchronizer x = new EntPhysicalSynchronizer(entrySet.getKey().getId(),
                    new BusinessObjectLight(entrySet.getKey().getParameters().get("deviceClass"), 
                    entrySet.getKey().getParameters().get("deviceId"), ""),
                    mibTables);
            
            try {
                results.addAll(x.sync());
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException | OperationNotPermittedException | ApplicationObjectNotFoundException | ArraySizeMismatchException | NotAuthorizedException | ServerSideException ex) {
                Exceptions.printStackTrace(ex);
            }
           
        }
        return results;
    }

    @Override
    public List<SyncResult> finalize(List<SyncAction> actions) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

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
import com.neotropic.kuwaiba.sync.model.SNMPDataProcessor;
import com.neotropic.kuwaiba.sync.model.SyncAction;
import com.neotropic.kuwaiba.sync.model.SyncDataSourceConfiguration;
import com.neotropic.kuwaiba.sync.model.SyncFinding;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import com.neotropic.kuwaiba.sync.model.TableData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
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
    public HashMap<RemoteBusinessObjectLight, AbstractDataEntity> mappedPoll(SynchronizationGroup syncGroup) throws InvalidArgumentException {
        try {
            HashMap<RemoteBusinessObjectLight, AbstractDataEntity> result  = new HashMap();
            SnmpManager snmpMananger = SnmpManager.getInstance();
            
            for (SyncDataSourceConfiguration agent : syncGroup.getSyncDataSourceConfigurations()) {
                long id = -1L;
                String className = null;                
                String address = null;
                String port = null;
                String community = null;
                
                if (agent.getParameters().containsKey("deviceId")) //NOI18N
                    id = Long.valueOf(agent.getParameters().get("deviceId")); //NOI18N
                else 
                    throw new InvalidArgumentException(String.format("Parameter deviceId not defined in synchronization group %s (%s)", 
                            syncGroup.getName(), syncGroup.getId()));
                
                if (agent.getParameters().containsKey("deviceClass")) //NOI18N
                    className = agent.getParameters().get("deviceClass"); //NOI18N
                else 
                    throw new InvalidArgumentException(String.format("Parameter deviceClass not defined in synchronization group %s (%s)", 
                            syncGroup.getName(), syncGroup.getId()));
                                
                if (agent.getParameters().containsKey("ipAddress")) //NOI18N
                    address = agent.getParameters().get("ipAddress"); //NOI18N
                else 
                    throw new InvalidArgumentException(String.format("Parameter ipAddress not defined in synchronization group %s (%s)", 
                            syncGroup.getName(), syncGroup.getId()));
                    
                if (agent.getParameters().containsKey("port")) //NOI18N 
                    port = agent.getParameters().get("port"); //NOI18N
                else 
                    throw new InvalidArgumentException(String.format("Parameter port not defined in synchronization group %s (%s)", 
                            syncGroup.getName(), syncGroup.getId()));
                
                if (agent.getParameters().containsKey("community")) //NOI18N
                    community = agent.getParameters().get("community"); //NOI18N
                else 
                    throw new InvalidArgumentException(String.format("Parameter community not defined in synchronization group %s (%s)", 
                            syncGroup.getName(), syncGroup.getId()));
                
                snmpMananger.setAddress("udp:" + address + "/" + port); //NOI18N
                snmpMananger.setCommunity(community);

                ReferenceSnmpResourceDefinition entPhysicalTable = new ReferenceSnmpResourceDefinition();

                List<List<String>> tableAsString = snmpMananger.getTableAsString(entPhysicalTable.values().toArray(new OID[0]));

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
                
                try {
                    RemoteBusinessObjectLight mappedObjectLight = PersistenceService.getInstance().getBusinessEntityManager().getObjectLight(className, id);
                    result.put(mappedObjectLight, new TableData("entPhysicalTable", value)); //NOI18N
                } catch (InventoryException ex) {
                    throw new InvalidArgumentException(String.format("The inventory object associated to sync group %s could not be retrived: ", ex.getMessage()));
                }
            }
            return result;
        } catch (IOException ex) {
            throw new InternalError(String.format("The SNMP manager could not be started: ", ex.getMessage()));
        }
    }
    
    @Override
    public List<SyncFinding> sync(HashMap<RemoteBusinessObjectLight, AbstractDataEntity> originalData) {
        List<SyncFinding> findings = new ArrayList<>();
        for (Map.Entry<RemoteBusinessObjectLight, AbstractDataEntity> entrySet : originalData.entrySet()) {
            RemoteBusinessObjectLight obj = entrySet.getKey();
            TableData table = (TableData)entrySet.getValue();
            SNMPDataProcessor x = new SNMPDataProcessor(obj.getClassName(), obj.getId(), (HashMap<String, List<String>>)table.getValue());
            try {
                findings.addAll(x.load());
            } catch (MetadataObjectNotFoundException | ObjectNotFoundException | InvalidArgumentException | OperationNotPermittedException | ApplicationObjectNotFoundException ex) {
                Logger.getLogger(ReferenceSnmpSyncProvider.class.getName()).log(Level.SEVERE, null, ex);
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

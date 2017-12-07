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
package com.neotropic.kuwaiba.sync.connectors.snmp.model;

import com.neotropic.kuwaiba.sync.connectors.snmp.SnmpManager;
import com.neotropic.kuwaiba.sync.model.AbstractDataEntity;
import com.neotropic.kuwaiba.sync.model.AbstractSyncProvider;
import com.neotropic.kuwaiba.sync.model.SyncAction;
import com.neotropic.kuwaiba.sync.model.SyncDataSourceConfiguration;
import com.neotropic.kuwaiba.sync.model.SyncFinding;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.snmp4j.smi.OID;

/**
 * Synchronization provider to SNMP agents
 * This class implement the logic to connect with a group of SNMP agents to 
 * retrieve the data and compare the differences with the management objects
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SnmpSyncProvider extends AbstractSyncProvider {

    @Override
    public String getName() {
        return "Reference SNMP Synchronization Provider";
    }

    @Override
    public String getId() {
        return SnmpSyncProvider.class.getName();
    }
    
    @Override
    public boolean isAutomated() {
        return false;
    }
    
    private SynchronizationGroup testSynGroup() {        
        try {
            List<SyncDataSourceConfiguration> agents = new ArrayList();
            HashMap<String, String> config1Params = new HashMap<>();
            config1Params.put("id", "888"); //NOI18N
            config1Params.put("name", "test1"); //NOI18N
            config1Params.put("class", "Test1"); //NOI18N
            config1Params.put("address", "127.0.0.1"); //NOI18N
            config1Params.put("port", "1161"); //NOI18N
            config1Params.put("community", "community1"); //NOI18N

            HashMap<String, String> config2Params = new HashMap<>();
            config1Params.put("id", "999"); //NOI18N
            config1Params.put("name", "test2"); //NOI18N
            config1Params.put("class", "Test2"); //NOI18N
            config1Params.put("address", "127.0.0.1"); //NOI18N
            config1Params.put("port", "1161"); //NOI18N
            config1Params.put("community", "community2"); //NOI18N
            
            SyncDataSourceConfiguration agent1 = new SyncDataSourceConfiguration(1, "agent1", config1Params); //NOI18N
            SyncDataSourceConfiguration agent2 = new SyncDataSourceConfiguration(2, "agent2", config2Params); //NOI18N
            
            agents.add(agent1);
            agents.add(agent2);
            SynchronizationGroup testSyncGroup = new SynchronizationGroup(0, "SNMPAgents", this, agents); //NOI18N
            
            return testSyncGroup;
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(SnmpSyncProvider.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    @Override
    public List<AbstractDataEntity> unmappedPoll(SynchronizationGroup syncGroup) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HashMap<RemoteBusinessObjectLight, AbstractDataEntity> mappedPoll(SynchronizationGroup syncGroup) {
        //syncGroup = testSynGroup();
        //syncGroup.ge
        try {
            HashMap<RemoteBusinessObjectLight, AbstractDataEntity> result  = new HashMap();
                        
            SnmpManager snmpMananger = SnmpManager.getInstance();
            
            for (SyncDataSourceConfiguration agent : syncGroup.getSyncDataSourceConfigurations()) {
                Long id = -1L;
                String name = null;
                String className = null;                
                String address = null;
                String port = null;
                String readCommunity = null;
                
                if (agent.getParameters().containsKey("id")) //NOI18N
                    id = Long.valueOf(agent.getParameters().get("id")); //NOI18N
                
                if (agent.getParameters().containsKey("name")) //NOI18N
                    name = agent.getParameters().get("name"); //NOI18N
                
                if (agent.getParameters().containsKey("class")) //NOI18N
                    className = agent.getParameters().get("class"); //NOI18N
                                
                if (agent.getParameters().containsKey("address")) //NOI18N
                    address = agent.getParameters().get("address"); //NOI18N
                    
                if (agent.getParameters().containsKey("port")) //NOI18N 
                    port = agent.getParameters().get("port"); //NOI18N
                
                if (agent.getParameters().containsKey("readCommunity")) //NOI18N
                    readCommunity = agent.getParameters().get("readCommunity"); //NOI18N
                
                if (id != -1L && name != null && className != null && address != null &&  port != null && readCommunity != null) {
                    snmpMananger.setAddress("udp:" + address + "/" + port); //NOI18N
                    snmpMananger.setCommunity(readCommunity);
                
                    HashMap<String, String> oids = SnmpEntPhysicalTable.EntPhysicalEntry.getInstance().getOids();
                    
                    List<OID> onlyNumberOids = new ArrayList();
                    for (String oid : oids.values())
                        onlyNumberOids.add(new OID(oid));

                    List<List<String>> tableAsString = snmpMananger.getTableAsString(onlyNumberOids.toArray(new OID[0]));
                    
                    HashMap<String, List<String>> value = new HashMap();
                    int i = 0;
                    for (String mibTreeNodeName : oids.keySet()) {                        
                        List<String> currentColumn = new ArrayList();
                        
                        for (List<String> cell : tableAsString)
                            currentColumn.add(cell.get(i));
                        
                        value.put(mibTreeNodeName, currentColumn);
                        i += 1;                            
                    }
                    int size = oids.keySet().size();
                    List<String> instances = new ArrayList();
                    for (List<String> cell : tableAsString)
                        instances.add(cell.get(size));
                    value.put("instance", instances); //NOI18N
                    
                    result.put(new RemoteBusinessObjectLight(id, name, className), new SnmpEntPhysicalTable(value));
                }
            }
            return result;
        } catch (IOException ex) {
            return null;
        }
    }
    
    @Override
    public List<SyncFinding> sync(String className, long objectId, List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<SyncFinding> sync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> finalize(List<SyncAction> actions) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

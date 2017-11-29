/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.neotropic.kuwaiba.sync.connectors.snmp;

import com.neotropic.kuwaiba.sync.model.SyncDataSourceConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.batch.api.chunk.ItemReader;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;

/**
 * This reader will poll one by one the queued sync groups and retrieve the 
 * information declared in the polling definition and map it to a generic 
 * structure in memory 
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DefaultSnmpSyncReader implements ItemReader {
    @Inject
    private JobContext jobContext;
    
    /**
     * The list of agents to be polled
     */
    private List<SyncDataSourceConfiguration> managedObjects;
    /**
     * The index of the managed object that's being processed currently
     */
    private int currentManagedObject = 0;
    
    @Override
    public void open(Serializable checkpoint) throws Exception {
        JobOperator jobOperator = BatchRuntime.getJobOperator();
        Properties jobParameters = jobOperator.getParameters(jobContext.getExecutionId());
        managedObjects = new ArrayList<>();
        for (Object manageObjectLabel : jobParameters.keySet()) { //Each parameter is a serialized version of the configuration of a managed object
            String[] configParameters = ((String)jobParameters.get(manageObjectLabel)).split(";");
             
            if (configParameters.length != 3) {
                System.out.println(String.format("Malformed managed object configuration. Ignoring configuration %s with value %s", 
                        manageObjectLabel, jobParameters.get(manageObjectLabel)));
                continue;
            }
            
            int port;
            
            try {
                port = Integer.getInteger(configParameters[1]);
            } catch (NumberFormatException ex) {
                System.out.println(String.format("Port is not a number. Using default port (161)", 
                        manageObjectLabel, jobParameters.get(manageObjectLabel)));
                port = 161; //Default SNMP port
            }
            
            if (port > 65535 || port < 1) {
                System.out.println(String.format("Invalid port value. Ignoring configuration %s with value %s", 
                        manageObjectLabel, jobParameters.get(manageObjectLabel)));
                continue;
            }

//            managedObjects.add(new SyncDataSourceConfiguration(Arrays.asList("deviceAddress", "agentPort", "agentCommunity"), 
//                                   Arrays.asList(configParameters[0], port, configParameters[2]))); //NOI18N
        }
    }

    @Override
    public void close() throws Exception { }

    @Override
    public Object readItem() throws Exception {
        currentManagedObject++;
        return null;
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return currentManagedObject;
    }

}

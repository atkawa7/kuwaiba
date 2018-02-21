/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package com.neotropic.kuwaiba.sync.model;

import java.io.Serializable;
import java.util.List;

/**
 * Creates a Synchronization Group. A Sync Group is a set of Synchronization 
 * Configurations that will be processed by the same Synchronization Provider
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SynchronizationGroup implements Serializable {
    /**
     * Group id
     */
    private long id;
    /**
     * Group name
     */
    private String name;
    /**
     * Group provider
     */
    private AbstractSyncProvider provider;
    /**
     * The configurations to be processed
     */
    private List<SyncDataSourceConfiguration> syncDataSourceConfigurations;
    
    public SynchronizationGroup(long id, String name, 
            AbstractSyncProvider provider, 
            List<SyncDataSourceConfiguration> syncDataSourceConfigurations) {
        this.id = id;
        this.name = name;
        this.provider = provider;
        this.syncDataSourceConfigurations = syncDataSourceConfigurations;
    }
        
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AbstractSyncProvider getProvider() {
        return provider;
    }

    public void setProvider(AbstractSyncProvider provider) {
        this.provider = provider;
    }

    public List<SyncDataSourceConfiguration> getSyncDataSourceConfigurations() {
        return syncDataSourceConfigurations;
    }

    public void setSyncDataSourceConfigurations(List<SyncDataSourceConfiguration> syncDataSourceConfigurations) {
        this.syncDataSourceConfigurations = syncDataSourceConfigurations;
    }
}

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

package com.neotropic.kuwaiba.sync.model;

import java.io.Serializable;
import java.util.List;

/**
 * Creates a Synchronization Group. A Sync Group is a set of Synchronization 
 * Configurations that will be processed by the same Synchronization Provider
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
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
     * Running provider
     */
    private AbstractSyncProvider currentProvider;
    /**
     * Group provider
     */
    private List<AbstractSyncProvider> lastSelectedProviders;
    /**
     * The configurations to be processed
     */
    private List<SyncDataSourceConfiguration> syncDataSourceConfigurations;

    public SynchronizationGroup(long id, String name, 
            List<SyncDataSourceConfiguration> syncDataSourceConfigurations) {
        this.id = id;
        this.name = name;
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

    public AbstractSyncProvider getCurrentProvider() {
        return currentProvider;
    }

    public void setCurrentProvider(AbstractSyncProvider currentProvider) {
        this.currentProvider = currentProvider;
    }

    public List<AbstractSyncProvider> getLastSelectedProviders() {
        return lastSelectedProviders;
    }

    public void setLastSelectedProviders(List<AbstractSyncProvider> lastSelectedProviders) {
        this.lastSelectedProviders = lastSelectedProviders;
    }

    public List<SyncDataSourceConfiguration> getSyncDataSourceConfigurations() {
        return syncDataSourceConfigurations;
    }

    public void setSyncDataSourceConfigurations(List<SyncDataSourceConfiguration> syncDataSourceConfigurations) {
        this.syncDataSourceConfigurations = syncDataSourceConfigurations;
    }
}

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
package com.neotropic.inventory.modules.sync.nodes.actions;

import org.inventory.core.services.api.actions.GenericInventoryAction;

/**
 * Action factory for the Inventory Sync Module
 * @author Charles Edward Bedon Cortazar <johny.ortega@kuwaiba.org>
 */
public class SyncManagerActionFactory {
    /**
     * Singleton for the New Sync Group action
     */
    private static NewSyncGroupAction newSyncGroupAction;
    /**
     * This action will launch a synchronization process for the selected sync group
     */
    private static RunSynchronizationProcessAction runSynchronizationProcessAction;
    /**
     * Action that adds data source configurations to a given sync group
     */
    private static NewSyncDataSourceConfigurationAction newSyncDataSourceConfigurationAction;
        
    public static GenericInventoryAction getNewSyncGroupAction() {
        return newSyncGroupAction == null ? newSyncGroupAction = new NewSyncGroupAction() : newSyncGroupAction;
    }
    
    public static GenericInventoryAction getNewRunSynchronizationProcessAction() {
        return runSynchronizationProcessAction == null ? runSynchronizationProcessAction = new RunSynchronizationProcessAction() : runSynchronizationProcessAction;
    }
    
    public static GenericInventoryAction getNewSyncDataSourceConfigurationAction() {
        return newSyncDataSourceConfigurationAction == null ? newSyncDataSourceConfigurationAction = new NewSyncDataSourceConfigurationAction() : newSyncDataSourceConfigurationAction;
    }
}

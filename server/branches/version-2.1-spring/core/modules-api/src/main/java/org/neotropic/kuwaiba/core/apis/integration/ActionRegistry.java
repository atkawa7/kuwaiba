/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.core.apis.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * All inventory-object related actions from all modules must be registered here at module startup. 
 * Then, the menus will be built using the registered actions and what kind of inventory actions 
 * they are applicable to.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class ActionRegistry {
    /**
     * The list of registered actions.
     */
    private List<AbstractVisualInventoryAction> actions;
    /**
     * Reference to the MetadataEntityManager to access the data model cache.
     */
    @Autowired
    private MetadataEntityManager mem;
    
    public ActionRegistry() {
        this.actions = new ArrayList<>();
    }
    
    public List<AbstractVisualInventoryAction> getActionsApplicableTo(String filter) {
        return this.actions.stream().filter((anAction) -> {
            try {
                return mem.isSubclassOf(filter, anAction.appliesTo());
            } catch (MetadataObjectNotFoundException ex) { // No existing (or cached) classes will be ignored
                return false;
            }
        }).collect(Collectors.toList());
    }
    
    /**
     * Adds an action to the registry
     * @param action The action to be added. Duplicated action ids are allowed, as long as the duplicate can be used 
     * to overwrite default behaviors, for example, if an object (say a connection) has a specific delete routine  that should 
     * be executed instead of the general purpose delete action, both actions should have the same id, and the renderer should 
     * override the default action with the specific one.
     */
    public void registerAction(AbstractVisualInventoryAction action) {
        this.actions.add(action);
    }
    
    public List<AbstractVisualInventoryAction> getActions() {
        return this.actions;
    }
}

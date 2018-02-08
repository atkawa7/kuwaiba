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
package org.inventory.navigation.navigationtree.nodes.actions;

import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.util.Lookup;
import org.openide.util.Utilities;


/**
 * Superclass to all actions related to a node
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class GenericObjectNodeAction extends GenericInventoryAction {
    
    protected List<LocalObjectLight> selectedObjects;
        
    @Override
    public boolean isEnabled() {
        Lookup.Result<? extends ObjectNode> selectedObjectNode = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class);
        
        if (selectedObjectNode == null)
            return false;
        
        selectedObjects = new ArrayList<>();
        
        for (ObjectNode selectedNode : selectedObjectNode.allInstances())
            selectedObjects.add(selectedNode.getObject());
        
        return numberOfNodes() == -1 ? !selectedObjects.isEmpty() : selectedObjects.size() == numberOfNodes();
    }
    
    public void setSelectedObjects(List<LocalObjectLight> selectedObjects) {
        this.selectedObjects = selectedObjects;
    }
    
    /**
     * The validators necessary for the action to be enabled
     * @return A validator. A string that declares a particular condition in an object (for example, saying that a port is already connected). Use null if this action can be applied to any object
     * You can add your own if the server supports them
     */
    public abstract String[] getValidators();
    /**
     * Instances of these classes are eligible to perform this action. Abstract and super classes are allowed
     * @return An array with the class names
     */
    public abstract String[] appliesTo();
    
    /**
     * The number of nodes that has to be selected so this action is enabled. Use -1 for any (provided at least one is selected)
     * @return 
     */
    public abstract int numberOfNodes();
}

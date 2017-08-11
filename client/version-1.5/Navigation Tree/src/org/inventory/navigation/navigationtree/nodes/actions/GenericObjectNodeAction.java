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
import java.util.Iterator;
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
        Iterator<? extends ObjectNode> selectedNodes = null;
        if (selectedObjectNode != null)
            selectedNodes = selectedObjectNode.allInstances().iterator();
        
        if (selectedObjects == null)
            selectedObjects = new ArrayList<>();
        else
            selectedObjects.clear();
        
        if (selectedNodes == null)
            return false;
        
        while(selectedNodes.hasNext()) {
            LocalObjectLight selectedObject  = selectedNodes.next().getLookup().lookup(LocalObjectLight.class);
            selectedObjects.add(selectedObject);
        }
        return !selectedObjects.isEmpty();
    }
    
    /**
     * What instances support this action
     * @return A validator. See class Constants (in Application Nodes) for possible values so far.
     * You can add your own if the server supports them
     */
    public abstract String getValidator();
}

/**
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

package com.neotropic.inventory.modules.mpls.actions;

import com.neotropic.inventory.modules.mpls.MPLSModuleService;
import com.neotropic.inventory.modules.mpls.scene.MPLSModuleScene;
import java.awt.event.ActionEvent;
import java.util.Set;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.visual.scene.AbstractScene;

/**
 * Removes an element from the view
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class RemoveObjectFromViewAction extends GenericInventoryAction {
    private MPLSModuleScene scene;
    
    public RemoveObjectFromViewAction(MPLSModuleScene scene) {
        this.putValue(NAME, "Remove From View"); 
        this.scene = scene;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Set<?> selectedObjects = scene.getSelectedObjects();
        for (Object selectedObject : selectedObjects) {
            LocalObjectLight castedObject = (LocalObjectLight)selectedObject;
            if (CommunicationsStub.getInstance().isSubclassOf(castedObject.getClassName(), MPLSModuleService.CLASS_GENERICEQUIPMENT))
                scene.removeNodeWithEdges(castedObject);

            else
                scene.removeEdge(castedObject);

            scene.fireChangeEvent(new ActionEvent(selectedObject, AbstractScene.SCENE_CHANGE, "manualDelete"));
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_MPLS_MODULE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}

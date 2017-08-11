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
package org.inventory.design.topology.actions;

import java.awt.event.ActionEvent;
import java.util.Set;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.design.topology.scene.TopologyViewScene;

/**
 * Action to delete an <code>ObjectConnectionWidget</code> from topology designer scene
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeleteConnectionAction extends GenericInventoryAction {
    private static DeleteConnectionAction instance;
    private final TopologyViewScene scene;
    
    private DeleteConnectionAction(TopologyViewScene scene) {
        putValue(NAME, "Remove Connection from View");
        this.scene = scene;        
    }
    
    public static DeleteConnectionAction getInstance(TopologyViewScene scene) {
        if (scene == null)
            return null;
        
        return instance == null ? instance = new DeleteConnectionAction(scene) : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Set<?> selectedObjects = scene.getSelectedObjects();
        for (Object selectedObject : selectedObjects) {
            String edge = (String)selectedObject;
            scene.removeEdge(edge);
            scene.fireChangeEvent(new ActionEvent(selectedObject, AbstractScene.SCENE_CHANGE, "manualDelete"));
        }
    }
    
    @Override
    public LocalPrivilege getPrivilege() {        
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TOPOLOGY_DESIGNER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}

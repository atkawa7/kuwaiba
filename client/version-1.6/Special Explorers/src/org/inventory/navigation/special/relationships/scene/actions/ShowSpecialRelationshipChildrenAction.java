/**
 * Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.navigation.special.relationships.scene.actions;

import java.awt.event.ActionEvent;
import java.util.Set;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.navigation.special.relationships.scene.SpecialRelationshipsGraphExplorerScene;

/**
 * Action to Show Special Relationship Children of the selected node
 * into a special relationship graphical representation scene.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ShowSpecialRelationshipChildrenAction  extends GenericInventoryAction {
    public static final String COMMAND = "showSpecialRelationshipChildren";
    private final SpecialRelationshipsGraphExplorerScene scene;
    private static ShowSpecialRelationshipChildrenAction instance;
    
    private ShowSpecialRelationshipChildrenAction(
        SpecialRelationshipsGraphExplorerScene scene) {
        
        this.scene = scene;
        putValue(NAME, "Show special relationships");
    }
    
    public static ShowSpecialRelationshipChildrenAction getInstance(SpecialRelationshipsGraphExplorerScene scene) {
        if (scene == null)
            return null;
        return instance == null ? instance = new ShowSpecialRelationshipChildrenAction(scene) : instance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Set<?> selectedObjects = scene.getSelectedObjects();
        
        for (Object selectedObject : selectedObjects) {
            scene.fireChangeEvent(new ActionEvent(
                selectedObject, 
                AbstractScene.SCENE_CHANGE, 
                ShowSpecialRelationshipChildrenAction.COMMAND));
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SPECIAL_EXPLORERS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}

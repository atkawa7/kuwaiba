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
package org.inventory.customization.classhierarchy.scene.actions;

import java.awt.event.ActionEvent;
import java.util.Set;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.customization.classhierarchy.scene.ClassHierarchyScene;

/**
 * Action to hide subclasses of selected widget in the Class Hierarchy Scene
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class HideSubclassAction extends GenericInventoryAction {
    public static final String COMMAND = "hideSubclasses";
    private final ClassHierarchyScene scene;
    private static HideSubclassAction instance;
    
    private HideSubclassAction(ClassHierarchyScene scene) {
        putValue(NAME, I18N.gm("hide_subclases"));
        this.scene = scene;
    }
    
    public static HideSubclassAction getInstance(ClassHierarchyScene scene) {
        if (scene == null)
            return null;
        return instance == null ? instance = new HideSubclassAction(scene) : instance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Set<?> selectedObjects = scene.getSelectedObjects();
        for (Object selectedObject : selectedObjects) {
            scene.fireChangeEvent(new ActionEvent(selectedObject, AbstractScene.SCENE_CHANGE, HideSubclassAction.COMMAND));
            scene.reorganizeNodes();
        }
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_DATA_MODEL_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}

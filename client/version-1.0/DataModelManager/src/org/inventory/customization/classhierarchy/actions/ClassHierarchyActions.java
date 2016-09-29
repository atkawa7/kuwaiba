/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.customization.classhierarchy.actions;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.NAME;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.scene.SelectableVMDNodeWidget;
import org.inventory.customization.classhierarchy.scene.ClassHierarchyScene;
import org.inventory.navigation.applicationnodes.classmetadatanodes.action.DeleteAttributeAction;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 * Set of actions to the nodes on the current class hierarchy scene
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ClassHierarchyActions {
    ClassHierarchyScene scene;
    
    private PopupMenuProvider nodeMenu;
    private ShowSubclassAction showSubclass;
    private HideSubclassAction hideSubclass;
    
    public ClassHierarchyActions(ClassHierarchyScene scene) {
        this.scene = scene;
        showSubclass = new ShowSubclassAction();
        hideSubclass = new HideSubclassAction();
    }
    
    public PopupMenuProvider createMenuForNode() {
        if (nodeMenu == null) {
            nodeMenu = new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                    JPopupMenu popupMenu = new JPopupMenu("Options");
                    popupMenu.add(showSubclass);
                    popupMenu.add(hideSubclass);
                    popupMenu.add(new JSeparator());
                    
                    Action [] classMetadataNodeActions = ((SelectableVMDNodeWidget) widget).getNode().getActions(false);
                    for (Action action : classMetadataNodeActions) {
                        if (action != null){
                            if (action instanceof DeleteAttributeAction)
                                popupMenu.add(((DeleteAttributeAction) action).getPopupPresenter());
                            else
                                popupMenu.add(action);
                            
                        }
                    }
                    
                    return popupMenu;
                }
            };
        }
        
        return nodeMenu;
    }
    
    private class ShowSubclassAction extends AbstractAction {
        public ShowSubclassAction() {
            this.putValue(NAME, "Show subclasses");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Set<?> selectedObjects = scene.getSelectedObjects();
            for (Object selectedObject : selectedObjects) {
                scene.fireChangeEvent(new ActionEvent(selectedObject, AbstractScene.SCENE_CHANGE, "showSubclasses"));
                scene.reorganizeNodes();
            }
        }
    }
    
    private class HideSubclassAction extends AbstractAction {
        public HideSubclassAction() {
            this.putValue(NAME, "Hide subclasses");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Set<?> selectedObjects = scene.getSelectedObjects();
            for (Object selectedObject : selectedObjects) {
                scene.fireChangeEvent(new ActionEvent(selectedObject, AbstractScene.SCENE_CHANGE, "hideSubclasses"));
                scene.reorganizeNodes();
            }
        }
    }
}

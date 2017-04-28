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
package org.inventory.graphicalrepresentation.specialrelationships.actions;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.inventory.graphicalrepresentation.specialrelationships.scene.GraphicalRepSpecialRelationshipsScene;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Utilities;

/**
 * Actions Factory for Graphical Representation of Special Relationships
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class GraphicalRepSpecialRelationshipsActionsFactory {
    private PopupMenuProvider specialRelatedObjectNodeMenu;
    private final HideSpecialRelationshipChildrenAction hideSpecialRelationshipChildrenAction; 
    private final ShowSpecialRelationshipChildrenAction showSpecialRelationshipChildrenAction;
    
    public GraphicalRepSpecialRelationshipsActionsFactory(
        GraphicalRepSpecialRelationshipsScene scene) {
        
        hideSpecialRelationshipChildrenAction = new HideSpecialRelationshipChildrenAction(scene);
        showSpecialRelationshipChildrenAction = new ShowSpecialRelationshipChildrenAction(scene);
    }
    
    public PopupMenuProvider createSpecialRelatedObjectNodeMenu() {
        if (specialRelatedObjectNodeMenu == null) {
            specialRelatedObjectNodeMenu = new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                    JPopupMenu popupMenu = new JPopupMenu("Options");                    
                    popupMenu.add(showSpecialRelationshipChildrenAction);
                    popupMenu.add(hideSpecialRelationshipChildrenAction);
                    popupMenu.add(new JSeparator());
                    
                    // Actions for ObjectNode
                    List<Action> listOfactions = new ArrayList();
                    
                    Action [] arrayOfActions = widget.getLookup().lookup(ObjectNode.class).getActions(true);
                    
                    for (Action action : arrayOfActions)
                        listOfactions.add(action);
                    
                    listOfactions.add(0, null);
                    listOfactions.add(0, hideSpecialRelationshipChildrenAction);
                    listOfactions.add(0, showSpecialRelationshipChildrenAction);
                    
                    return Utilities.actionsToPopup(listOfactions.toArray(new Action[0]), 
                            widget.getScene().getView());
                    //TODO: action for PoolNode
                }
            };
        }
        return specialRelatedObjectNodeMenu;
    }
}

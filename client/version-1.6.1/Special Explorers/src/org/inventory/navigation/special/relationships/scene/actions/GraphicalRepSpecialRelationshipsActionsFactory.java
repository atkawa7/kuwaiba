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

import java.awt.Point;
import javax.swing.JPopupMenu;
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
        
    public PopupMenuProvider createSpecialRelatedObjectNodeMenu() {
        if (specialRelatedObjectNodeMenu == null) {
            specialRelatedObjectNodeMenu = new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {                    
                    return Utilities.actionsToPopup(widget.getLookup().lookup(ObjectNode.class).getActions(true), 
                            widget.getScene().getView());
                }
            };
        }
        return specialRelatedObjectNodeMenu;
    }
}

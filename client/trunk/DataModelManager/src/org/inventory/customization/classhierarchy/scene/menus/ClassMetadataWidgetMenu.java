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
package org.inventory.customization.classhierarchy.scene.menus;

import java.awt.Point;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.visual.scene.SelectableVMDNodeWidget;
import org.inventory.customization.classhierarchy.nodes.ClassMetadataNode;
import org.inventory.customization.classhierarchy.scene.ClassHierarchyScene;
import org.inventory.customization.classhierarchy.scene.actions.HideSubclassAction;
import org.inventory.customization.classhierarchy.scene.actions.ShowSubclassAction;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 * Action to Create a menu to the widget that represent an instance of 
 * <code>ClassMetadataNode</code> in the Class Hierarchy Scene.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ClassMetadataWidgetMenu implements PopupMenuProvider {
    private static ClassMetadataWidgetMenu instance;
    
    private ClassMetadataWidgetMenu() {
    }
    
    public static ClassMetadataWidgetMenu getInstance() {
        return instance == null ? instance = new ClassMetadataWidgetMenu() : instance;
    }

    @Override
    public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
        JPopupMenu popupMenu = new JPopupMenu(I18N.gm("options"));
        popupMenu.add(ShowSubclassAction.getInstance((ClassHierarchyScene) widget.getScene()));
        popupMenu.add(HideSubclassAction.getInstance((ClassHierarchyScene) widget.getScene()));
        popupMenu.add(new JSeparator());
        
        Action [] classMetadataNodeActions = ((SelectableVMDNodeWidget) widget).
            getLookup().lookup(ClassMetadataNode.class).getActions(false);
        
        for (Action action : classMetadataNodeActions) {
            if (action != null)
                popupMenu.add(action);
        }
        return popupMenu;
    }
}

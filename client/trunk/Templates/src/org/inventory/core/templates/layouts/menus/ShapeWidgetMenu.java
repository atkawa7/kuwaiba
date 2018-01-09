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
 * 
 */
package org.inventory.core.templates.layouts.menus;

import java.awt.Point;
import javax.swing.JPopupMenu;
import org.inventory.core.templates.layouts.scene.widgets.actions.CopyShapeAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.DeleteShapeAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.GroupCopyShapeAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.GroupShapesAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.PasteShapeAction;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 * Popup Menu to the shape widgets
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ShapeWidgetMenu implements PopupMenuProvider {
    private static ShapeWidgetMenu instance;
    private JPopupMenu popupMenu = null;
    
    private ShapeWidgetMenu() {
    }
    
    public static ShapeWidgetMenu getInstance() {
        return instance == null ? instance = new ShapeWidgetMenu() : instance;        
    }

    @Override
    public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
        if (popupMenu == null) {
            popupMenu = new JPopupMenu();
            popupMenu.add(CopyShapeAction.getInstance());
            popupMenu.add(PasteShapeAction.getInstance());
            popupMenu.addSeparator();
            popupMenu.add(GroupCopyShapeAction.getInstance());
            popupMenu.addSeparator();
            popupMenu.add(GroupShapesAction.getInstance());
            popupMenu.addSeparator();
            popupMenu.add(DeleteShapeAction.getInstance());
        }
        CopyShapeAction.getInstance().setSelectedWidget(widget);
        
        GroupCopyShapeAction.getInstance().setSelectedWidget(widget);
        
        PasteShapeAction.getInstance().setSelectedWidget(widget);
        PasteShapeAction.getInstance().setLocalLocation(localLocation);
        
        DeleteShapeAction.getInstance().setSelectedWidget(widget);
        return popupMenu;
    }
    
}


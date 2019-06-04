/**
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import org.inventory.core.templates.layouts.scene.widgets.actions.BringToBackAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.BringToBackContainerAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.DeleteShapeAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.GenericShapeAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.BringToBackOneStepAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.BringToFrontAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.BringToFrontContainerAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.BringToFrontOneStepAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.CopyShapeAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.DeleteContainerShapeAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.GroupShapesAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.PasteShapeAction;
import org.inventory.core.templates.layouts.scene.widgets.actions.UngroupShapesAction;
import org.inventory.core.templates.layouts.widgets.ContainerShapeWidget;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 * Popup Menu to the shape widgets
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ShapeWidgetMenu implements PopupMenuProvider {
    private static ShapeWidgetMenu instance;
    private JPopupMenu popupMenu = null;
    private JPopupMenu containerShapePopupMenu = null;
    private List<GenericShapeAction> actions = null;
    
    private ShapeWidgetMenu() {
    }
    
    public static ShapeWidgetMenu getInstance() {
        return instance == null ? instance = new ShapeWidgetMenu() : instance;        
    }

    @Override
    public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
        if (widget instanceof ContainerShapeWidget)
            return getContainerShapePopupMenu(widget, localLocation);
        else
            return getShapePopupMenu(widget, localLocation);
            

    }
    
    private JPopupMenu getShapePopupMenu(Widget widget, Point localLocation) {
        if (popupMenu == null) {
            actions = new ArrayList();
            actions.add(CopyShapeAction.getInstance());
            actions.add(PasteShapeAction.getInstance());
            actions.add(BringToFrontAction.getInstance());
            actions.add(BringToFrontOneStepAction.getInstance());
            actions.add(BringToBackAction.getInstance());
            actions.add(BringToBackOneStepAction.getInstance());
            actions.add(DeleteShapeAction.getInstance());
                                    
            popupMenu = new JPopupMenu();
            popupMenu.add(CopyShapeAction.getInstance());
            popupMenu.add(PasteShapeAction.getInstance());
            popupMenu.addSeparator();
            popupMenu.add(BringToFrontAction.getInstance());
            popupMenu.add(BringToFrontOneStepAction.getInstance());
            popupMenu.addSeparator();
            popupMenu.add(BringToBackAction.getInstance());
            popupMenu.add(BringToBackOneStepAction.getInstance());
            popupMenu.addSeparator();
            popupMenu.add(DeleteShapeAction.getInstance());
        }
        for (Action action : actions)
            ((GenericShapeAction) action).setSelectedWidget(widget);
                
        PasteShapeAction.getInstance().setLocation(localLocation);
        return popupMenu;
    }
    
    private JPopupMenu getContainerShapePopupMenu(Widget widget, Point localLocation) {
        containerShapePopupMenu = new JPopupMenu();
        
        containerShapePopupMenu.add(CopyShapeAction.getInstance());
        containerShapePopupMenu.add(PasteShapeAction.getInstance());
        containerShapePopupMenu.addSeparator();
                
        if (widget instanceof ContainerShapeWidget) {
            if (!((ContainerShapeWidget) widget).isCustomShape()) {
                if (((ContainerShapeWidget) widget).getShapesSet().isEmpty())
                    containerShapePopupMenu.add(GroupShapesAction.getInstance());
                else
                    containerShapePopupMenu.add(UngroupShapesAction.getInstance());
                containerShapePopupMenu.addSeparator();
            }
        }
        containerShapePopupMenu.add(BringToFrontContainerAction.getInstance());
        containerShapePopupMenu.add(BringToBackContainerAction.getInstance());
        containerShapePopupMenu.addSeparator();
        containerShapePopupMenu.add(DeleteContainerShapeAction.getInstance());
        
        
        CopyShapeAction.getInstance().setSelectedWidget(widget);
        PasteShapeAction.getInstance().setSelectedWidget(widget);
        PasteShapeAction.getInstance().setLocation(localLocation);        
        
        if (widget instanceof ContainerShapeWidget) {
            if (!((ContainerShapeWidget) widget).isCustomShape()) {
                if (((ContainerShapeWidget) widget).getShapesSet().isEmpty())
                    GroupShapesAction.getInstance().setSelectedWidget(widget);
                else
                    UngroupShapesAction.getInstance().setSelectedWidget(widget);
            }
        }
        BringToFrontContainerAction.getInstance().setSelectedWidget(widget);
        BringToBackContainerAction.getInstance().setSelectedWidget(widget);
        DeleteContainerShapeAction.getInstance().setSelectedWidget(widget);
        
        return containerShapePopupMenu;
    }
}


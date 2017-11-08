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
package org.inventory.design.deviceLayout.scene.widgets.actions;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.design.deviceLayout.model.Shape;
import org.inventory.design.deviceLayout.scene.ModelLayoutScene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Action used to paste a widget in the scene
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PasteShapeAction extends GenericInventoryAction {
    private static PasteShapeAction instance;
    private Widget selectedWidget;
    private Point localLocation;
    
    private PasteShapeAction() {
        putValue(NAME, "Paste");

    }
    
    public static PasteShapeAction getInstance() {
        instance = instance == null ? instance = new PasteShapeAction() : instance;
        
        if (CopyShapeAction.getInstance().getShapeToCopy() == null && 
            GroupCopyShapeAction.getInstance().getShapeToCopy() == null)
            instance.setEnabled(false);
        else
            instance.setEnabled(true);
                
        return instance;        
    }
    
    public Widget getSelectedWidget() {
        return selectedWidget;        
    }
    
    public void setSelectedWidget(Widget selectedWidget) {
        this.selectedWidget = selectedWidget;
    }
    
    public Point getLocalLocation() {
        return localLocation;                        
    }
    
    public void setLocalLocation(Point localLocation) {
        this.localLocation = localLocation;
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (selectedWidget != null) {
            if (localLocation != null) {
                Shape shapeToCopy = CopyShapeAction.getInstance().getShapeToCopy();
                if (shapeToCopy != null)
                    recursivePaste(selectedWidget, localLocation, shapeToCopy, false);
                
                shapeToCopy = GroupCopyShapeAction.getInstance().getShapeToCopy();
                if (shapeToCopy != null)
                    recursivePaste(selectedWidget, localLocation, shapeToCopy, true);
            }
        }
    }
    
    public void recursivePaste(Widget parentWidget, Point localLocation, Shape shapeToCpy, boolean recursive) {
        ModelLayoutScene scene = (ModelLayoutScene) parentWidget.getScene();
        Object parentObject = scene.findObject(parentWidget);
        
        if (parentObject instanceof Shape) {
            Shape shapeCpy = shapeToCpy.shapeCopy();
            shapeCpy.setParent((Shape) parentObject);
            
            Widget shapeCpyWidget = scene.addNode(shapeCpy);
            
            shapeCpyWidget.setPreferredLocation(new Point(localLocation.x, localLocation.y));
            shapeCpyWidget.setPreferredSize(new Dimension(shapeCpy.getWidth(), shapeCpy.getHeight()));
            shapeCpyWidget.setBackground(shapeCpy.getColor());
            scene.validate();
            scene.paint();
            
            shapeCpyWidget.bringToFront();
            
            if (recursive) {
                Widget widgetToCpy = scene.findWidget(shapeToCpy);
                if (widgetToCpy != null) {
                    for (Widget child : widgetToCpy.getChildren()) {
                        Object childObj = scene.findObject(child);
                        if (childObj instanceof Shape) {
                            recursivePaste(shapeCpyWidget, child.getPreferredLocation(), (Shape) childObj, recursive);
                        }
                    }
                }
            }
        }
    }
}

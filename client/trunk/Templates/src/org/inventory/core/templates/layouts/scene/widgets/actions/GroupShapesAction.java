/*
 *  Copyright 2010-2018, Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.inventory.core.templates.layouts.scene.widgets.actions;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.widgets.ContainerShapeWidget;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Action use to group shapes
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GroupShapesAction extends GenericShapeAction {
    private static GroupShapesAction instance;
    
    private GroupShapesAction() {
        putValue(NAME, I18N.gm("lbl_group_action"));
    }
    
    public static GroupShapesAction getInstance() {
        return instance == null ? instance = new GroupShapesAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (selectedWidget instanceof ContainerShapeWidget) {
            if (!(selectedWidget.getScene() instanceof DeviceLayoutScene))
                return;
            
            DeviceLayoutScene scene = (DeviceLayoutScene) selectedWidget.getScene();
            
            Rectangle selectedWidgetLocalBounds = selectedWidget.getBounds();
                        
            if (selectedWidgetLocalBounds == null)
                return;
            
            Rectangle selectedWidgetSceneBounds = selectedWidget.convertLocalToScene(selectedWidgetLocalBounds);

            List<Shape> shapesSet = new ArrayList();

            Widget parent = selectedWidget.getParentWidget();
            for (Widget child : parent.getChildren()) {
                if (child.equals(selectedWidget))
                    continue;
                
                Rectangle childLocalBounds = child.getBounds();
                if (childLocalBounds == null)
                    continue;
                
                Rectangle childSceneBounds = child.convertLocalToScene(childLocalBounds);

                if (selectedWidgetSceneBounds.contains(childSceneBounds)) {                    
                    Object shape = scene.findObject(child);
                    
                    if (shape != null && shape instanceof Shape)
                        shapesSet.add((Shape) shape);
                }
            }            
            ((ContainerShapeWidget) selectedWidget).setShapesSet(shapesSet);
            scene.fireChangeEvent(new ActionEvent(this, DeviceLayoutScene.SCENE_CHANGE, "Shape Paste"));
        }
    }
}

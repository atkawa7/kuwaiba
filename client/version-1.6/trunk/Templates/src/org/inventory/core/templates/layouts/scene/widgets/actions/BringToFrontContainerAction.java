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

import java.awt.event.ActionEvent;
import java.util.List;
import static javax.swing.Action.NAME;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;
import org.inventory.core.templates.layouts.widgets.ContainerShapeWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * Action used to Bring to Front a Container
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class BringToFrontContainerAction extends GenericShapeAction {
    private static BringToFrontContainerAction instance;
    
    private BringToFrontContainerAction() {
        putValue(NAME, "Bring To Front");
    }
    
    public static BringToFrontContainerAction getInstance() {
        return instance == null ? instance = new BringToFrontContainerAction() : instance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (selectedWidget instanceof ContainerShapeWidget) {
            if (selectedWidget.getScene() instanceof DeviceLayoutScene) {
                
                DeviceLayoutScene scene = (DeviceLayoutScene) selectedWidget.getScene();
                
                List<Shape> shapes = ((ContainerShapeWidget) selectedWidget).getShapesSet();
                                
                for (Shape shape : shapes) {
                    Widget shapeWidget = scene.findWidget(shape);
                    
                    BringToFrontAction.getInstance().setSelectedWidget(shapeWidget);
                    BringToFrontAction.getInstance().actionPerformed(e);
                }
                BringToFrontAction.getInstance().setSelectedWidget(selectedWidget);
                BringToFrontAction.getInstance().actionPerformed(e);
                
                scene.fireChangeEvent(new ActionEvent(this, DeviceLayoutScene.SCENE_CHANGE, "Shape bring to front"));
            }
        }
    }
    
}

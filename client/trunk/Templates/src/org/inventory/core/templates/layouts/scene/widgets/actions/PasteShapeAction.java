/*
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
package org.inventory.core.templates.layouts.scene.widgets.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import static javax.swing.Action.NAME;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.model.ContainerShape;
import org.inventory.core.templates.layouts.model.CustomShape;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.widgets.ContainerShapeWidget;
import org.inventory.core.templates.layouts.widgets.providers.MoveContainerShapeProvider;
import org.inventory.core.templates.layouts.widgets.providers.ResizeContainerShapeProvider;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Action used to Paste Shapes
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PasteShapeAction extends GenericShapeAction {
    private static PasteShapeAction instance;
    private Point location;
    
    private PasteShapeAction() {
        putValue(NAME, I18N.gm("lbl_paste_action"));
    }
    
    public static PasteShapeAction getInstance() {
        if (instance == null)
            instance = new PasteShapeAction();
        
        if (CopyShapeAction.getInstance().getShapeToCopy() == null)
            instance.setEnabled(false);
        else
            instance.setEnabled(true);
        
        return instance;
    }
    
    public Point getLocation() {
        return location;
    }
    
    public void setLocation(Point location) {
        this.location = location;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (selectedWidget != null) {
            if (location != null) {
                DeviceLayoutScene scene = null;
                
                if (selectedWidget instanceof DeviceLayoutScene)
                    scene = (DeviceLayoutScene) selectedWidget;
                else if (selectedWidget.getScene() instanceof DeviceLayoutScene) {
                    scene = (DeviceLayoutScene) selectedWidget.getScene();
                    location = selectedWidget.convertLocalToScene(location);
                }
                                    
                if (scene != null) {
                    Shape shapeToCpy = CopyShapeAction.getInstance().getShapeToCopy();

                    Shape shape = shapeToCpy.shapeCopy();                    
                    
                    if (shape instanceof ContainerShape) {
                        ContainerShapeWidget containerToCpy = (ContainerShapeWidget) scene.findWidget(shapeToCpy);
                        List<Shape> shapeSetToCpy = containerToCpy.getShapesSet();
                        
                        List<Shape> innerShapes = new ArrayList();
                        
                        for (Shape innerShapeToCpy : shapeSetToCpy) {
                            Shape innerShape = innerShapeToCpy.shapeCopy();
                            innerShapes.add(innerShape);
                            scene.addNode(innerShape);
                        }
                        ContainerShapeWidget containerWidget = (ContainerShapeWidget) scene.addNode(shape);
                        containerWidget.setShapesSet(innerShapes);
                        
                        MoveContainerShapeProvider moveContainerShapeProvider = new MoveContainerShapeProvider();
                        moveContainerShapeProvider.movementStarted(containerWidget);
                        
                        containerWidget.setPreferredLocation(location);
                        containerWidget.revalidate();
                        
                        moveContainerShapeProvider.movementFinished(containerWidget);
                        
                    } else if (shape instanceof CustomShape) {
                        shape.setX(location.x);
                        shape.setY(location.y);
                        
                        CustomShape tempCustomShape = (CustomShape) shape.shapeCopy();

                        Widget widget = scene.addNode(shape);
                        scene.validate();
                        scene.paint();

                        ResizeContainerShapeProvider resizeContainerShapeProvider = new ResizeContainerShapeProvider();
                        resizeContainerShapeProvider.resizingStarted(widget);

                        widget.setPreferredLocation(new Point(tempCustomShape.getX(), tempCustomShape.getY()));
                        widget.setPreferredBounds(new Rectangle(
                            -Shape.DEFAULT_BORDER_SIZE, -Shape.DEFAULT_BORDER_SIZE, 
                            tempCustomShape.getWidth(), tempCustomShape.getHeight()));
                        widget.revalidate();
                        
                        resizeContainerShapeProvider.resizingFinished(widget);                        
                    } else {
                        shape.setX(location.x);
                        shape.setY(location.y);
                        
                        scene.addNode(shape);
                        scene.validate();
                        scene.paint();
                    }
                    scene.fireChangeEvent(new ActionEvent(this, DeviceLayoutScene.SCENE_CHANGE, "Shape Paste"));
                }
            }
        }
    }
    
}

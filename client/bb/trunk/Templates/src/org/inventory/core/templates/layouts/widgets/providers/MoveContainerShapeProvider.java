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
package org.inventory.core.templates.layouts.widgets.providers;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import org.inventory.core.templates.layouts.LayoutOutputManager;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.widgets.ContainerShapeWidget;
import org.inventory.core.templates.layouts.widgets.ShapeWidgetUtil;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 * Provided used to Move a Container Shape
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MoveContainerShapeProvider implements MoveProvider {
    private Point startPoint;
    private List<Widget> shapeWidgetSet;

    @Override
    public void movementStarted(Widget widget) {
        startPoint = new Point(widget.getPreferredLocation());
        
        if (widget instanceof ContainerShapeWidget) {
            
            if (!(widget.getScene() instanceof DeviceLayoutScene))
                return;                
            
            DeviceLayoutScene scene = (DeviceLayoutScene) widget.getScene();
            
            List<Shape> shapeSet = ((ContainerShapeWidget) widget).getShapesSet();
            if (shapeSet == null)
                return;
            shapeWidgetSet = new ArrayList();
            
            for (Shape shape : shapeSet) {
                Widget shapeWidget = scene.findWidget(shape);
                if (shapeWidget == null)
                    continue;
                shapeWidget.setVisible(false);
                shapeWidget.revalidate();                
                
                shapeWidgetSet.add(shapeWidget);                
            }
            ShapeWidgetUtil.makingVisibleChanges(widget);
        }
    }

    @Override
    public void movementFinished(Widget widget) {
        Point finishPoint = widget.getPreferredLocation();
        if (finishPoint.x < 0 || finishPoint.y < 0) {
            widget.setPreferredLocation(startPoint);
            widget.revalidate();
            
            if (shapeWidgetSet == null) {
                for (Widget innerWidget : shapeWidgetSet) {
                    innerWidget.setVisible(true);                    
                    innerWidget.revalidate();
                }
            }
            ShapeWidgetUtil.makingVisibleChanges(widget);
            
            if (widget.getScene() instanceof DeviceLayoutScene) {
                DeviceLayoutScene scene = (DeviceLayoutScene) widget.getScene();

                LayoutOutputManager.getInstance().getLayoutOutput(scene.getModel())
                    .printLine("The shape cannot be moved to negative positions", Color.RED);
                
                LayoutOutputManager.getInstance().getLayoutOutput(scene.getModel())
                    .printLine("Select the shape to show the shapes contained", Color.BLACK);
            }
            return;
        }
        
        if (shapeWidgetSet == null)
            return;
        
        if (widget instanceof ContainerShapeWidget) {
            if (widget.getScene() instanceof DeviceLayoutScene) {
                
                DeviceLayoutScene scene = (DeviceLayoutScene) widget.getScene();
                
                int changeInX = finishPoint.x - startPoint.x;
                int changeInY = finishPoint.y - startPoint.y;

                for (Widget shapeWidget : shapeWidgetSet) {
                    Point innerShapeStartPoint = shapeWidget.getPreferredLocation();
                    Point innerShapeFinishPoint = new Point(innerShapeStartPoint.x + changeInX, innerShapeStartPoint.y + changeInY);

                    shapeWidget.setPreferredLocation(innerShapeFinishPoint);
                    shapeWidget.setVisible(true);
                    shapeWidget.revalidate();
                    
                    Shape innerShape = (Shape) scene.findObject(shapeWidget);
                    if (innerShape != null) {
                        innerShape.setX(innerShapeFinishPoint.x);
                        innerShape.firePropertyChange(widget, Shape.PROPERTY_X, innerShapeStartPoint.x, innerShapeFinishPoint.x);
                        innerShape.setY(innerShapeFinishPoint.y);
                        innerShape.firePropertyChange(widget, Shape.PROPERTY_Y, innerShapeStartPoint.y, innerShapeFinishPoint.y);
                    }
                }
                Shape shape = (Shape) scene.findObject(widget);
                
                if (shape != null) {
                    Point shapeStartPoint = new Point(shape.getX(), shape.getY());
                    Point shapeFinishPoint = widget.getPreferredLocation();

                    shape.setX(finishPoint.x);
                    shape.firePropertyChange(widget, Shape.PROPERTY_X, shapeStartPoint.x, shapeFinishPoint.x);
                    shape.setY(finishPoint.y);
                    shape.firePropertyChange(widget, Shape.PROPERTY_Y, shapeStartPoint.y, shapeFinishPoint.y);
                }
                ShapeWidgetUtil.makingVisibleChanges(widget);
                scene.fireChangeEvent(new ActionEvent(this, DeviceLayoutScene.SCENE_CHANGE, "Shape change"));
            }
        }
    }

    @Override
    public Point getOriginalLocation(Widget widget) {
        return widget.getPreferredLocation();
    }

    @Override
    public void setNewLocation(Widget widget, Point location) {
        widget.setPreferredLocation(location);
    }
    
}

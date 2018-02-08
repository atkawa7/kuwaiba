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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import org.inventory.core.templates.layouts.LayoutOutputManager;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.widgets.ContainerShapeWidget;
import org.inventory.core.templates.layouts.widgets.ShapeWidgetUtil;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.widget.Widget;

/**
 * Provider used to resize container shapes
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ResizeContainerShapeProvider implements ResizeProvider, ResizeStrategy {
    private Point startPoint;
    private Rectangle startBounds;
    private List<Widget> shapeWidgetSet;
    
    public ResizeContainerShapeProvider() {        
    } 
        
    @Override
    public void resizingStarted(Widget widget) {
        startPoint = new Point(widget.getPreferredLocation());
        startBounds = new Rectangle(widget.getPreferredBounds());
        
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
    public void resizingFinished(Widget widget) {
        ResizeShapeWidgetProvider resizeShapeWidgetProvider = new ResizeShapeWidgetProvider(startPoint, startBounds);
        resizeShapeWidgetProvider.resizingFinished(widget);
        
        if (widget.getPreferredLocation().equals(startPoint) && 
            widget.getPreferredBounds().equals(startBounds)) {
            
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
                    .printLine("The shape cannot be resized because the given size is out of bounds of layout", Color.RED);
                LayoutOutputManager.getInstance().getLayoutOutput(scene.getModel())
                    .printLine("Select the shape to show the shapes contained", Color.BLACK);
            }
            return;
        }
        
        if (shapeWidgetSet == null)
            return;
        
        if (widget.getScene() instanceof DeviceLayoutScene) {
            if (widget instanceof ContainerShapeWidget) {
                DeviceLayoutScene scene = (DeviceLayoutScene) widget.getScene();
                
                Point finishPoint = widget.getPreferredLocation();
                Rectangle finishBounds = widget.getPreferredBounds();

                double percentWidth = finishBounds.getWidth() / startBounds.getWidth();
                double percentHeight = finishBounds.getHeight() / startBounds.getHeight();
                                
                for (Widget innerWidget : shapeWidgetSet) {
                    innerWidget.setVisible(true);

                    Point innerStartPoint = innerWidget.getPreferredLocation();
                    Rectangle innerStartBounds = innerWidget.getPreferredBounds();

                    int dx = Math.abs(startPoint.x - innerStartPoint.x);
                    int dy = Math.abs(startPoint.y - innerStartPoint.y);
                                        
                    int x = finishPoint.x + (int) Math.round(dx * percentWidth);
                    int y = finishPoint.y + (int) Math.round(dy * percentHeight);
                    int width = (int) Math.round(innerStartBounds.width * percentWidth);
                    int height = (int) Math.round(innerStartBounds.height * percentHeight);
                    
                    if (x <= 0 || y <= 0 || width <= 4 * Shape.DEFAULT_BORDER_SIZE || height <= 4 * Shape.DEFAULT_BORDER_SIZE) {
                        for (Widget innerWidget2 : shapeWidgetSet) {
                            innerWidget2.setVisible(true);
                            innerWidget2.revalidate();
                        }
                        
                        resizeShapeWidgetProvider.resizingRestarted(widget);
                        Shape containerShape = (Shape) scene.findObject(widget);
                        resizeShapeWidgetProvider.firePropertyChange(containerShape, widget);
                        
                        ShapeWidgetUtil.makingVisibleChanges(widget);
                        return;
                    }
                }
                
                for (Widget innerWidget : shapeWidgetSet) {
                    innerWidget.setVisible(true);

                    Point innerStartPoint = innerWidget.getPreferredLocation();
                    Rectangle innerStartBounds = innerWidget.getPreferredBounds();

                    int dx = Math.abs(startPoint.x - innerStartPoint.x);
                    int dy = Math.abs(startPoint.y - innerStartPoint.y);
                    
                    int x = finishPoint.x + (int) Math.round(dx * percentWidth);
                    int y = finishPoint.y + (int) Math.round(dy * percentHeight);
                    int width = (int) Math.round(innerStartBounds.width * percentWidth);
                    int height = (int) Math.round(innerStartBounds.height * percentHeight);

                    Point innerFinishPoint = new Point(x, y);
                    Rectangle innerFinishBounds = new Rectangle(
                        -Shape.DEFAULT_BORDER_SIZE, -Shape.DEFAULT_BORDER_SIZE, 
                        width, 
                        height);

                    innerWidget.setPreferredBounds(innerFinishBounds);
                    innerWidget.setPreferredLocation(innerFinishPoint);
                    innerWidget.revalidate();
                    
                    Shape innerShape = (Shape) scene.findObject(innerWidget);             
                    resizeShapeWidgetProvider.firePropertyChange(innerShape, innerWidget);
                }
                ShapeWidgetUtil.makingVisibleChanges(widget);
                scene.fireChangeEvent(new ActionEvent(this, DeviceLayoutScene.SCENE_CHANGE, "Shape change"));
            }
        }
    }
    
    @Override
    public Rectangle boundsSuggested(Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ControlPoint controlPoint) {
        return suggestedBounds;
    }
    
}

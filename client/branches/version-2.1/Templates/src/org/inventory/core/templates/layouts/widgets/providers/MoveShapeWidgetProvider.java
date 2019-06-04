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
import org.inventory.core.templates.layouts.LayoutOutputManager;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 * Provided used to Move a Shape
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MoveShapeWidgetProvider implements MoveProvider {
    private Point startPoint;

    @Override
    public void movementStarted(Widget widget) {
        startPoint = new Point(widget.getPreferredLocation());
    }

    @Override
    public void movementFinished(Widget widget) {
        Point finishPoint = widget.getPreferredLocation();
        if (finishPoint.x < 0 || finishPoint.y < 0) {
            widget.setPreferredLocation(startPoint);
            widget.revalidate();
            
            if (widget.getScene() instanceof DeviceLayoutScene) {
                DeviceLayoutScene scene = (DeviceLayoutScene) widget.getScene();

                LayoutOutputManager.getInstance().getLayoutOutput(scene.getModel())
                    .printLine("The shape cannot be moved to negative positions", Color.RED);
            }
            return;
        }
        
        if (widget.getScene() instanceof DeviceLayoutScene) {
            DeviceLayoutScene scene = (DeviceLayoutScene) widget.getScene();
            
            Shape shape = (Shape) scene.findObject(widget);
            if (shape != null) {                
                shape.setX(finishPoint.x);
                shape.firePropertyChange(widget, Shape.PROPERTY_X, startPoint.x, finishPoint.x);
                shape.setY(finishPoint.y);
                shape.firePropertyChange(widget, Shape.PROPERTY_Y, startPoint.y, finishPoint.y);
            }
        }
        
        if (widget.getScene() instanceof DeviceLayoutScene)
            ((DeviceLayoutScene) widget.getScene()).fireChangeEvent(new ActionEvent(this, DeviceLayoutScene.SCENE_CHANGE, "Shape change"));
    }

    @Override
    public Point getOriginalLocation(Widget widget) {
        return widget.getPreferredLocation();
    }

    @Override
    public void setNewLocation(Widget widget, Point point) {
        widget.setPreferredLocation(point);
    }    
}

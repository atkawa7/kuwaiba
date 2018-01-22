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

import java.awt.Point;
import java.awt.Rectangle;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts2.scene.EquipmentLayoutScene;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ResizeShapeWidgetProvider implements ResizeProvider, ResizeStrategy {
    private Point startPoint;
    private Rectangle startBounds;
    
    public ResizeShapeWidgetProvider() {        
    }
    
    public ResizeShapeWidgetProvider(Point startPoint, Rectangle startBounds) {
        this.startPoint = new Point(startPoint);
        this.startBounds = new Rectangle(startBounds);
    }

    @Override
    public void resizingStarted(Widget widget) {
        startPoint = new Point(widget.getPreferredLocation());
        startBounds = new Rectangle(widget.getPreferredBounds());
    }
    
    public void resizingRestarted(Widget widget) {
        widget.setPreferredLocation(startPoint);
        widget.setPreferredBounds(startBounds);
        widget.revalidate();
    }

    @Override
    public void resizingFinished(Widget widget) {        
        if (widget.getScene() instanceof EquipmentLayoutScene) {
            EquipmentLayoutScene scene = (EquipmentLayoutScene) widget.getScene();
            Shape shape = (Shape) scene.findObject(widget);
            
            if (shape != null) {
                Point finishPoint = widget.getPreferredLocation();
                Rectangle finishBounds = widget.getPreferredBounds();
                
                finishPoint = new Point(
                    finishPoint.x + finishBounds.x + Shape.DEFAULT_BORDER_SIZE, 
                    finishPoint.y + finishBounds.y + Shape.DEFAULT_BORDER_SIZE
                );
                widget.setPreferredBounds(new Rectangle(
                    -Shape.DEFAULT_BORDER_SIZE, 
                    -Shape.DEFAULT_BORDER_SIZE,
                    finishBounds.width, 
                    finishBounds.height));
                widget.setPreferredLocation(finishPoint);
                widget.revalidate();
                
                if (widget.getPreferredLocation().x < 0 ||
                    widget.getPreferredLocation().y < 0 ||
                    widget.getPreferredBounds().width <= 2 * Shape.DEFAULT_BORDER_SIZE || 
                    widget.getPreferredBounds().height <= 2 * Shape.DEFAULT_BORDER_SIZE) {
                    
                    resizingRestarted(widget);
                    widget.revalidate();
                }
                firePropertyChange(shape, widget);
            }
        }        
    }
    
    public void firePropertyChange(Shape shape, Widget widget) {
        shape.firePropertyChange(widget, Shape.PROPERTY_X, startPoint.x, widget.getPreferredLocation().x);
        shape.setX(widget.getPreferredLocation().x);

        shape.firePropertyChange(widget, Shape.PROPERTY_Y, startPoint.y, widget.getPreferredLocation().y);
        shape.setY(widget.getPreferredLocation().y);

        shape.firePropertyChange(widget, Shape.PROPERTY_WIDTH, startBounds.width, widget.getPreferredBounds().width);
        shape.setWidth(widget.getPreferredBounds().width);

        shape.firePropertyChange(widget, Shape.PROPERTY_HEIGHT, startBounds.height, widget.getPreferredBounds().height);
        shape.setHeight(widget.getPreferredBounds().height);
    }

    @Override
    public Rectangle boundsSuggested(Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ControlPoint controlPoint) {
        return suggestedBounds;
    }
}

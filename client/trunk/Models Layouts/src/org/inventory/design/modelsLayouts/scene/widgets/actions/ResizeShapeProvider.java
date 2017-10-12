/**
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.design.modelsLayouts.scene.widgets.actions;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import org.inventory.design.modelsLayouts.scene.ModelLayoutScene;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.widget.Widget;

/**
 * Implementation of resize provider and resize strategy. Used to fix the 
 * locations and dimensions of a selected shape and its children
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ResizeShapeProvider implements ResizeProvider, ResizeStrategy {
    // The initial location of the widget
    private Point oldLocation;
    // The initial bounds of the widget
    private Rectangle oldBounds;
    // Selected control point to resize            
    private ResizeProvider.ControlPoint controlPoint;
    
    public ResizeShapeProvider() {
    }

    @Override
    public void resizingStarted(Widget widget) {
        Point location = widget.getLocation();
        Rectangle bounds = widget.getBounds();
        
        if (location == null || bounds == null)
            return;
        oldLocation = new Point(location);
        oldBounds = new Rectangle(bounds);
    }
        
    @Override
    public void resizingFinished(Widget widget) {        
        if (oldLocation == null || oldBounds == null)
            return;
        ModelLayoutScene scene = (ModelLayoutScene) widget.getScene();
        
        double widthPercentage = Double.valueOf(Integer.toString(widget.getPreferredBounds().width)) / Double.valueOf(Integer.toString(oldBounds.width));
        double heightPercentage = Double.valueOf(Integer.toString(widget.getPreferredBounds().height)) / Double.valueOf(Integer.toString(oldBounds.height));
        
        Widget newWidget = scene.changeWidget(new Dimension(widget.getPreferredBounds().width, widget.getPreferredBounds().height), widget, oldBounds, widthPercentage, heightPercentage, controlPoint);
                
        Rectangle widgetOldBounds = new Rectangle(oldLocation.x, oldLocation.y, oldBounds.width, oldBounds.height);
        
        Point newLocation = newWidget.getLocation();
        Rectangle newBounds = newWidget.getPreferredBounds();
        Rectangle widgetNewBounds = new Rectangle(newLocation.x, newLocation.y, newBounds.width, newBounds.height);
        
        scene.fireShapeBoundsChange(newWidget, widgetOldBounds, widgetNewBounds);        
        scene.repaint();
        
        scene.fireChangeEvent(new ActionEvent(this, ModelLayoutScene.SCENE_CHANGE, "Shape resized"));
        oldLocation = null;
        oldBounds = null;
    }
    
    @Override
    public Rectangle boundsSuggested(Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ControlPoint controlPoint) {
        this.controlPoint = controlPoint;
        for (Widget child : widget.getChildren())
            child.setVisible(false);
        return suggestedBounds;
    }
};

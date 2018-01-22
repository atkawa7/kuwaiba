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
package org.inventory.core.templates.layouts.scene.widgets.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.scene.ModelLayoutScene;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MoveShapeProvider implements MoveProvider {
    private Point initialLocalLocation;
    private Rectangle initialBounds;

    public MoveShapeProvider() {
    }

    @Override
    public void movementStarted(Widget widget) {                
//        widget.bringToFront();
        initialLocalLocation = widget.getLocation();
        initialBounds = new Rectangle(widget.getBounds());
    }

    @Override
    public void movementFinished(Widget widget) {
        ModelLayoutScene scene = ((ModelLayoutScene) widget.getScene());
        
        List<Shape> containIn = new ArrayList();
        for (Shape shape : scene.getNodes()) {
            Widget shapeWidget = scene.findWidget(shape);
            if(!widget.equals(shapeWidget) && isContained2(shapeWidget, initialBounds))
                containIn.add(shape);
        }
        containIn.size();
        
        Widget parent = scene;
        
        Widget parent2 = scene;        
        
        for (Shape shape : scene.getNodes()) {
            Widget shapeWidget = scene.findWidget(shape);

            if (isContained(widget, shapeWidget)) {
                if (!widget.equals(shapeWidget) && isContained(shapeWidget, parent2))
                    parent2 = shapeWidget;
            }
        }
        
        Point oldLocalLocation = new Point(widget.getLocation());
        Point oldSceneLocation = widget.convertLocalToScene(oldLocalLocation);

        widget.removeFromParent();
        parent.addChild(widget);
        Point newLocalLocation = widget.convertSceneToLocal(oldSceneLocation);
////    
        int move_x =  widget.getLocation().x - initialLocalLocation.x;
        int move_y =  widget.getLocation().y - initialLocalLocation.y;
        for (Shape shape : containIn) {
            Widget shapeWidget = scene.findWidget(shape);
            int x = shapeWidget.getLocation().x;
            int y = shapeWidget.getLocation().y;
            
            Point otherLocation = new Point(x + move_x, y + move_y);
                        
            shape.setX(otherLocation.x);
            shape.setY(otherLocation.y);
            shapeWidget.setPreferredLocation(otherLocation);
            shapeWidget.getScene().revalidate();
        }
////        
        widget.setPreferredLocation(newLocalLocation);

        widget.getScene().revalidate();

        Shape shape = (Shape) scene.findObject(widget);
        if (shape != null) {
            shape.setX(newLocalLocation.x);
            shape.firePropertyChange(widget, Shape.PROPERTY_X, oldLocalLocation.x, newLocalLocation.x);
            shape.setY(newLocalLocation.y);
            shape.firePropertyChange(widget, Shape.PROPERTY_Y, oldLocalLocation.y, newLocalLocation.y);
                        
            Shape parentShape = (Shape) scene.findObject(parent2);
            if (parentShape != null)
                shape.setParent(parentShape);
        }
        if (!initialLocalLocation.equals(newLocalLocation))
            scene.fireChangeEvent(new ActionEvent(this, ModelLayoutScene.SCENE_CHANGE, "Shape moved"));
    }

    private boolean isContained(Widget child, Widget possibleParent) {
        if (child == null || possibleParent == null)
            return false;

        Rectangle childLocalBounds = child.getBounds();
        if (childLocalBounds == null)
            return false;

        Rectangle parentLocalBounds = possibleParent.getBounds();
        if (parentLocalBounds == null)
            return false;

        Rectangle childSceneBounds = child.convertLocalToScene(childLocalBounds);
        Rectangle parentSceneBounds = possibleParent.convertLocalToScene(parentLocalBounds);

        return parentSceneBounds.contains(childSceneBounds);
    }
    
    private boolean isContained2(Widget child, Rectangle parentBounds) {
        Rectangle childBounds = child.getBounds();
        if (childBounds == null)
            return false;
        return parentBounds.contains(childBounds);
    }

    @Override
    public Point getOriginalLocation(Widget widget) {
        return widget.getPreferredLocation();
    }

    @Override
    public void setNewLocation(Widget widget, Point point) {
        //TODO: Boundaries in move and resize
        widget.setPreferredLocation(point);
    }
}

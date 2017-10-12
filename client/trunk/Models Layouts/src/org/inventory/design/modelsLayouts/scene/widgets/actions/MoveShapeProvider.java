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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import org.inventory.design.modelsLayouts.model.Shape;
import org.inventory.design.modelsLayouts.scene.ModelLayoutScene;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MoveShapeProvider implements MoveProvider {
    private Point initialLocalLocation;

    public MoveShapeProvider() {
    }

    @Override
    public void movementStarted(Widget widget) {                
        widget.bringToFront();
        initialLocalLocation = widget.getLocation();
    }

    @Override
    public void movementFinished(Widget widget) {
        ModelLayoutScene scene = ((ModelLayoutScene) widget.getScene());

        Widget parent = scene;

        for (Shape shape : scene.getNodes()) {
            Widget shapeWidget = scene.findWidget(shape);

            if (isContained(widget, shapeWidget)) {
                if (!widget.equals(shapeWidget) && isContained(shapeWidget, parent))
                    parent = shapeWidget;
            }
        }  
        Point oldLocalLocation = new Point(widget.getLocation());
        Point oldSceneLocation = widget.convertLocalToScene(oldLocalLocation);

        widget.removeFromParent();
        parent.addChild(widget);
        Point newLocalLocation = widget.convertSceneToLocal(oldSceneLocation);
        widget.setPreferredLocation(newLocalLocation);

        widget.getScene().revalidate();

        Shape shape = (Shape) scene.findObject(widget);
        if (shape != null) {
            shape.setX(newLocalLocation.x);
            shape.firePropertyChange(widget, Shape.PROPERTY_X, oldLocalLocation.x, newLocalLocation.x);
            shape.setY(newLocalLocation.y);
            shape.firePropertyChange(widget, Shape.PROPERTY_Y, oldLocalLocation.y, newLocalLocation.y);
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

    @Override
    public Point getOriginalLocation(Widget widget) {
        return widget.getPreferredLocation();
    }

    @Override
    public void setNewLocation(Widget widget, Point point) {
        widget.setPreferredLocation(point);
    }
}

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
package org.inventory.core.templates.layouts.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import org.inventory.core.templates.layouts.model.CircleShape;
import org.inventory.core.templates.layouts.model.ContainerShape;
import org.inventory.core.templates.layouts.model.CustomShape;
import org.inventory.core.templates.layouts.model.LabelShape;
import org.inventory.core.templates.layouts.model.PolygonShape;
import org.inventory.core.templates.layouts.model.RectangleShape;
import org.inventory.core.templates.layouts.model.Shape;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ShapeWidgetFactory {
    private static ShapeWidgetFactory instance;
    
    private ShapeWidgetFactory() {
    }
    
    public static ShapeWidgetFactory getInstance() {
        return instance == null ? instance = new ShapeWidgetFactory() : instance;
    }
    
    public Widget getShapeWidget(Scene scene, Shape shape) {
        if (scene == null || shape == null)
            return null;
        
        Widget widget = null;
        
        if (shape instanceof ContainerShape) {
            widget = new ContainerShapeWidget(scene, (ContainerShape) shape);
        }
        else if (shape instanceof CustomShape) {
            widget = new ContainerShapeWidget(scene, (CustomShape) shape);
        }
        else if (shape instanceof LabelShape) {
            widget = new LabelShapeWidget(scene, (LabelShape) shape);            
        }
        else if (shape instanceof RectangleShape) {
            widget = new RectangleShapeWidget(scene, (RectangleShape) shape);
        }
        else if (shape instanceof CircleShape) {
            widget = new CircleShapeWidget(scene, (CircleShape) shape);
        }
        else if (shape instanceof PolygonShape) {
            widget = new PolygonShapeWidget(scene, (PolygonShape) shape);
        }
        if (widget != null)
            mappedShapeInWidget(shape, widget);
        
        return widget;
    }
    
    public void mappedShapeInWidget(Shape sourceShape, Widget targetWidget) {
        targetWidget.setPreferredLocation(new Point(sourceShape.getX(), sourceShape.getY()));
        
        targetWidget.setOpaque(sourceShape.isOpaque());
        targetWidget.setBackground(sourceShape.getColor());
        targetWidget.setPreferredSize(new Dimension(sourceShape.getWidth(), sourceShape.getHeight()));
        
        if (sourceShape.isOpaque()) {
            targetWidget.setBorder(BorderFactory.createLineBorder(sourceShape.getBorderWidth(), sourceShape.getBorderColor()));
        } else {
            if (sourceShape instanceof ContainerShape) {
                targetWidget.setBorder(BorderFactory.createResizeBorder(4, Color.BLACK, true));
            } else {
                targetWidget.setBorder(BorderFactory.createOpaqueBorder(
                    sourceShape.getBorderWidth(), sourceShape.getBorderWidth(), 
                    sourceShape.getBorderWidth(), sourceShape.getBorderWidth()));
            }
        }
    }
}

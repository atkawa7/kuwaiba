/**
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
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.inventory.core.templates.layouts.lookup.SharedContent;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.nodes.ShapeNode;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.spi.palette.PaletteController;
import org.openide.util.Lookup;

/**
 * Set of methods to manage the updates on shape nodes
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ShapeWidgetUtil {
    
    public static Lookup fixLookup(ShapeNode shapeNode) {
        PaletteController pallete = SharedContent.getInstance().getAbstractLookup().lookup(PaletteController.class);
        
        List lst = new ArrayList<>();
        lst.add(shapeNode);
        lst.add(pallete);
        
        SharedContent.getInstance().getInstanceContent().set(lst, null);
                
        return SharedContent.getInstance().getAbstractLookup();
    }
    
    public static void propertyChange(Widget widget, Shape shape, PropertyChangeEvent evt) {
        if (evt == null || widget == null || shape == null)
            return;
        if (evt.getPropertyName() == null)
            return;
                
        if (Shape.PROPERTY_NAME.equals(evt.getPropertyName())) {
        }
        else if (Shape.PROPERTY_X.equals(evt.getPropertyName())) {
            int x = (Integer) evt.getNewValue();
            widget.setPreferredLocation(new Point(x, shape.getY()));
        }
        else if (Shape.PROPERTY_Y.equals(evt.getPropertyName())) {
            int y = (Integer) evt.getNewValue();
            widget.setPreferredLocation(new Point(shape.getX(), y));
        }
        else if (Shape.PROPERTY_WIDTH.equals(evt.getPropertyName())) {
            Rectangle bounds = widget.getBounds();
            if (bounds == null) 
                return;
            int newWidthValue = (Integer) evt.getNewValue();
            widget.setPreferredSize(new Dimension(newWidthValue, bounds.height));         
        }
        else if (Shape.PROPERTY_HEIGHT.equals(evt.getPropertyName())) {
            Rectangle bounds = widget.getBounds();
            if (bounds == null) 
                return;
            int newHeightValue = (Integer) evt.getNewValue();
            widget.setPreferredSize(new Dimension(bounds.width, newHeightValue));
        }
        else if (Shape.PROPERTY_COLOR.equals(evt.getPropertyName())) {
            widget.setBackground((Color) evt.getNewValue());
        }
        else if (Shape.PROPERTY_OPAQUE.equals(evt.getPropertyName())) {
            widget.setOpaque((Boolean) evt.getNewValue());
            if (!((Boolean) evt.getNewValue())) {
                widget.setBorder(BorderFactory.createOpaqueBorder(
                    shape.getBorderWidth(), shape.getBorderWidth(), 
                    shape.getBorderWidth(), shape.getBorderWidth()));
            }
        }
        widget.getScene().validate();
        widget.getScene().paint();        
    }
    
    public static void makingVisibleChanges(Widget widget) {
        widget.getScene().setVisible(false);
        widget.getScene().revalidate();
        widget.getScene().setVisible(true);
        widget.getScene().revalidate();
    }
    
    public static void shapeToWidget(Shape sourceShape, Widget targetWidget, boolean revalidate) {
        if (sourceShape == null || targetWidget == null)
            return;
        targetWidget.setPreferredLocation(new Point(sourceShape.getX(), sourceShape.getY()));
        targetWidget.setPreferredBounds(new Rectangle(
            sourceShape.getBorderWidth(), sourceShape.getBorderWidth(), 
            sourceShape.getWidth(), sourceShape.getHeight()));
        targetWidget.setPreferredSize(new Dimension(sourceShape.getWidth(), sourceShape.getHeight()));
        targetWidget.setBackground(sourceShape.getColor());
        if (sourceShape.isOpaque())
            targetWidget.setBorder(BorderFactory.createLineBorder(sourceShape.getBorderWidth(), sourceShape.getBorderColor()));
        else {
            targetWidget.setBorder(BorderFactory.createOpaqueBorder(
                sourceShape.getBorderWidth(), sourceShape.getBorderWidth(), 
                sourceShape.getBorderWidth(), sourceShape.getBorderWidth()));
        }
        targetWidget.setOpaque(sourceShape.isOpaque());
        
        if (!revalidate)
            targetWidget.revalidate();
    }
}

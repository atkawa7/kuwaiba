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
package org.inventory.layout.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.inventory.layout.lookup.SharedContent;
import org.inventory.layout.model.Shape;
import org.inventory.layout.nodes.ShapeNode;
import org.inventory.layout.scene.ModelLayoutScene;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.spi.palette.PaletteController;
import org.openide.util.Lookup;

/**
 * Set of methods to manage the shape nodes update
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ShapeWidgetUtil {
    
    public static Lookup fixLookup(ShapeNode shapeNode) {
        PaletteController pallete = SharedContent.getInstance().getAbstractLookup().lookup(PaletteController.class);
        
        List lst = new ArrayList();
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
        Scene scene = widget.getScene();
        
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
                        
            if (scene instanceof ModelLayoutScene) {
                int oldWidthValue = (Integer) evt.getOldValue();
                
                double widthPercentage = Double.parseDouble(Integer.toString(newWidthValue)) / Double.parseDouble(Integer.toString(oldWidthValue));
                ((ModelLayoutScene) scene).updateWidget(widget, widthPercentage, 1);
            }            
        }
        else if (Shape.PROPERTY_HEIGHT.equals(evt.getPropertyName())) {
            Rectangle bounds = widget.getBounds();
            if (bounds == null) 
                return;
            int newHeightValue = (Integer) evt.getNewValue();
            widget.setPreferredSize(new Dimension(bounds.width, newHeightValue));
                        
            if (scene instanceof ModelLayoutScene) {
                int oldHeightValue = (Integer) evt.getOldValue();
                
                double heightPercentage = Double.parseDouble(Integer.toString(newHeightValue)) / Double.parseDouble(Integer.toString(oldHeightValue));
                ((ModelLayoutScene) scene).updateWidget(widget, 1, heightPercentage);
            }
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
        
        if (scene instanceof ModelLayoutScene)
            ((ModelLayoutScene) scene).fireChangeEvent(new ActionEvent(widget, ModelLayoutScene.SCENE_CHANGE, evt.getPropertyName() + " Property Changed"));
    }
}

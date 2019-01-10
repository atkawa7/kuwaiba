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
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.inventory.core.templates.layouts.lookup.SharedContentLookup;
import org.inventory.core.templates.layouts.model.LabelShape;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.nodes.ShapeNode;
import org.netbeans.api.visual.widget.Scene;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Widget used to represent a label in the scene
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class LabelShapeWidget extends ResizableLabelWidget  implements PropertyChangeListener, SharedContentLookup {
    ShapeNode shapeNode;
    Lookup lookup;

    public LabelShapeWidget(Scene scene, LabelShape labelShape) {
        super(scene);
        labelShape.addPropertyChangeListener(this);
        lookup = Lookups.fixed(labelShape);
        shapeNode = new ShapeNode(labelShape);        
        
        Font font = new Font(null, 0, labelShape.getFontSize());
        setFont(font);
        setLabel(labelShape.getLabel());
        setForeground(labelShape.getTextColor());
    }
    
    @Override
    public Lookup fixLookup() {
        return ShapeWidgetUtil.fixLookup(shapeNode);
    }
    
    @Override
    public Lookup getLookup() {
        fixLookup();
        return super.getLookup();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Shape shape = lookup.lookup(Shape.class);
        if (shape == null)
            return;
        
        if (LabelShape.PROPERTY_LABEL.equals(evt.getPropertyName())) {            
            setLabel((String) evt.getNewValue());
        } else if (LabelShape.PROPERTY_TEXT_COLOR.equals(evt.getPropertyName())) {            
            setForeground((Color) evt.getNewValue());
        } else if (LabelShape.PROPERTY_FONT_SIZE.equals(evt.getPropertyName())) { 
            setFont(new Font(null, 0, (Integer) evt.getNewValue()));
        }
        ShapeWidgetUtil.propertyChange(this, shape, evt);
    }
}

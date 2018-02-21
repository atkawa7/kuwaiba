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
package org.inventory.core.templates.layouts.widgets;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.inventory.core.templates.layouts.lookup.SharedContentLookup;
import org.inventory.core.templates.layouts.model.CircleShape;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.nodes.ShapeNode;
import org.netbeans.api.visual.widget.Scene;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Selectable circle widget
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CircleShapeWidget extends CircleWidget implements PropertyChangeListener, SharedContentLookup {
    private final ShapeNode shapeNode;
    private final Lookup lookup;

    public CircleShapeWidget(Scene scene, CircleShape circleShape) {
        super(scene);
        circleShape.addPropertyChangeListener(this);
        
        setBackground(circleShape.getColor());        
        setEllipseColor(circleShape.getEllipseColor());
        setOvalColor(circleShape.getOvalColor());
        
        lookup = Lookups.fixed(circleShape);
        shapeNode = new ShapeNode(circleShape);        
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Shape shape = lookup.lookup(Shape.class);
        if (shape == null)
            return;
        
        if (CircleShape.PROPERTY_ELLIPSE_COLOR.equals(evt.getPropertyName())) {
            setEllipseColor((Color) evt.getNewValue());
        } else if (CircleShape.PROPERTY_OVAL_COLOR.equals(evt.getPropertyName())) {
            setOvalColor((Color) evt.getNewValue());
        }
        ShapeWidgetUtil.propertyChange(this, shape, evt);
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
}

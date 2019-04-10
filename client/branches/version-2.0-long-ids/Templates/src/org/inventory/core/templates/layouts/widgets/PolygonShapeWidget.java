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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.inventory.core.templates.layouts.lookup.SharedContentLookup;
import org.inventory.core.templates.layouts.model.PolygonShape;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.nodes.ShapeNode;
import org.netbeans.api.visual.widget.Scene;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Selectable polygon widget
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PolygonShapeWidget extends PolygonWidget implements PropertyChangeListener,  SharedContentLookup {
    private final ShapeNode shapeNode;
    private final Lookup lookup;
    
    public PolygonShapeWidget(Scene scene, PolygonShape polygonShape) {
        super(scene);
        polygonShape.addPropertyChangeListener(this);
        
        setBackground(polygonShape.getColor());
        setOutlineColor(polygonShape.getOutlineColor());
        setInteriorColor(polygonShape.getInteriorColor());
        
        lookup = Lookups.fixed(polygonShape);
        shapeNode = new ShapeNode(polygonShape);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Shape shape = lookup.lookup(Shape.class);
        if (shape == null)
            return;
        
        if (PolygonShape.PROPERTY_OUTLINE_COLOR.equals(evt.getPropertyName())) {
            setOutlineColor((Color) evt.getNewValue());
        } else if (PolygonShape.PROPERTY_INTERIOR_COLOR.equals(evt.getPropertyName())) {
            setInteriorColor((Color) evt.getNewValue());
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

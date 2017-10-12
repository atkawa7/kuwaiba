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
package org.inventory.design.modelsLayouts.nodes;

import org.inventory.design.modelsLayouts.model.Shape;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.inventory.design.modelsLayouts.model.LabelShape;
import org.inventory.design.modelsLayouts.nodes.properties.ShapeGeneralProperty;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * A shape in the palette or a node in the scene
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ShapeNode extends AbstractNode implements PropertyChangeListener {
    public ShapeNode(Shape shape) {
        super(Children.LEAF, Lookups.singleton(shape));
        shape.addPropertyChangeListener(this);
        
        if (shape.getUrlIcon() != null)
            setIconBaseWithExtension(shape.getUrlIcon());
    }
    
    public Shape getShape() {
        return getLookup().lookup(Shape.class);
    }
        
    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
                
        Sheet.Set generalPropertySet = Sheet.createPropertiesSet();
        Sheet.Set propertiesPropertySet = Sheet.createPropertiesSet();
        
        ShapeGeneralProperty propertyName = new ShapeGeneralProperty(Shape.PROPERTY_NAME, String.class, Shape.PROPERTY_NAME, "");
        ShapeGeneralProperty propertyX = new ShapeGeneralProperty(Shape.PROPERTY_X, Integer.class, Shape.PROPERTY_X, "");
        ShapeGeneralProperty propertyY = new ShapeGeneralProperty(Shape.PROPERTY_Y, Integer.class, Shape.PROPERTY_Y, "");
        ShapeGeneralProperty propertyWidth = new ShapeGeneralProperty(Shape.PROPERTY_WIDTH, Integer.class, Shape.PROPERTY_WIDTH, "");
        ShapeGeneralProperty propertyHeigth = new ShapeGeneralProperty(Shape.PROPERTY_HEIGHT, Integer.class, Shape.PROPERTY_HEIGHT, "");
        ShapeGeneralProperty propertyColor = new ShapeGeneralProperty(Shape.PROPERTY_COLOR, Color.class, Shape.PROPERTY_COLOR, "");
        //ShapeGeneralProperty propertyBorderWidth = new ShapeGeneralProperty(Shape.PROPERTY_BORDER_WIDTH, Integer.class, Shape.PROPERTY_BORDER_WIDTH, "");
        ShapeGeneralProperty propertyBoderColor = new ShapeGeneralProperty(Shape.PROPERTY_BORDER_COLOR, Color.class, Shape.PROPERTY_BORDER_COLOR, "");
        ShapeGeneralProperty propertyIsEquipment = new ShapeGeneralProperty(Shape.PROPERTY_IS_EQUIPMENT, Boolean.class, Shape.PROPERTY_IS_EQUIPMENT, "");
        
        generalPropertySet.setDisplayName("General");
        generalPropertySet.setName("general");  //NOI18N
        generalPropertySet.put(propertyName);
        generalPropertySet.put(propertyX);
        generalPropertySet.put(propertyY);
        generalPropertySet.put(propertyWidth);
        generalPropertySet.put(propertyHeigth);
        generalPropertySet.put(propertyColor);
        //generalPropertySet.put(propertyBorderWidth);
        generalPropertySet.put(propertyBoderColor);
        generalPropertySet.put(propertyIsEquipment);
        sheet.put(generalPropertySet);
        
        propertiesPropertySet.setDisplayName("Properties");
        propertiesPropertySet.setName("properties");  //NOI18N
        if (getShape() instanceof LabelShape) {
            ShapeGeneralProperty propertyLabel = new ShapeGeneralProperty(LabelShape.PROPERTY_LABEL, String.class, LabelShape.PROPERTY_LABEL, "");
            ShapeGeneralProperty propertyTextColor = new ShapeGeneralProperty(LabelShape.PROPERTY_TEXT_COLOR, Color.class, LabelShape.PROPERTY_TEXT_COLOR, "");            
            ShapeGeneralProperty propertyFontSize = new ShapeGeneralProperty(LabelShape.PROPERTY_FONT_SIZE, Integer.class, LabelShape.PROPERTY_FONT_SIZE, "");
            
            propertiesPropertySet.put(propertyLabel);
            propertiesPropertySet.put(propertyTextColor);
            propertiesPropertySet.put(propertyFontSize);
        }
        sheet.put(propertiesPropertySet);
        return sheet;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setSheet(createSheet());
    }
}

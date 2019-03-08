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
package org.inventory.core.templates.layouts.nodes;

import org.inventory.core.templates.layouts.model.Shape;
import java.awt.Color;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.model.CircleShape;
import org.inventory.core.templates.layouts.model.LabelShape;
import org.inventory.core.templates.layouts.model.PolygonShape;
import org.inventory.core.templates.layouts.model.CustomShape;
import org.inventory.core.templates.layouts.model.RectangleShape;
import org.inventory.core.templates.layouts.nodes.properties.ShapeGeneralProperty;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 * A shape in the palette or a node in the scene
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ShapeNode extends AbstractNode implements PropertyChangeListener {
    public ShapeNode(Shape shape) {
        super(Children.LEAF, Lookups.singleton(shape));
        shape.addPropertyChangeListener(this);
        
        if (shape.getUrlIcon() != null)
            setIconBaseWithExtension(shape.getUrlIcon());
        setShortDescription(shape.getName());
    }
    
    public ShapeNode(Shape shape, ShapeHierarchyChildren children) {
        super(children, Lookups.singleton(shape));
        shape.addPropertyChangeListener(this);
        
        if (shape.getUrlIcon() != null)
            setIconBaseWithExtension(shape.getUrlIcon());
    }
    
    public Shape getShape() {
        return getLookup().lookup(Shape.class);
    }
    
    @Override
    public String getDisplayName() {
        return getShape().getName();
    }
    
    @Override
    public Image getIcon(int i){
        Image icon;
        if (getShape() instanceof CustomShape) {
            icon = ((CustomShape) getShape()).getIcon();
            
            if (icon != null)
                return icon;
        }
        
        if (getShape().getUrlIcon() != null) {
                setIconBaseWithExtension(getShape().getUrlIcon());
                return super.getIcon(i);
        }
        return ImageUtilities.loadImage("org/inventory/core/templates/res/list-type-item.png");
    }
        
    @Override
    public Image getOpenedIcon(int i){
        return getIcon(i);
    }
        
    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
                
        Sheet.Set generalPropertySet = Sheet.createPropertiesSet();
        Sheet.Set propertiesPropertySet = Sheet.createPropertiesSet();
        
        ShapeGeneralProperty propertyName = new ShapeGeneralProperty(Shape.PROPERTY_NAME, String.class, 
            I18N.gm("property_display_name_name"), "");
        
        ShapeGeneralProperty propertyX = new ShapeGeneralProperty(Shape.PROPERTY_X, Integer.class, "x", "");
        
        ShapeGeneralProperty propertyY = new ShapeGeneralProperty(Shape.PROPERTY_Y, Integer.class, "y", "");
        
        ShapeGeneralProperty propertyWidth = new ShapeGeneralProperty(Shape.PROPERTY_WIDTH, Integer.class, 
            I18N.gm("property_display_name_width"), "");
        
        ShapeGeneralProperty propertyHeigth = new ShapeGeneralProperty(Shape.PROPERTY_HEIGHT, Integer.class, 
            I18N.gm("property_display_name_height"), "");
        
        ShapeGeneralProperty propertyColor = new ShapeGeneralProperty(Shape.PROPERTY_COLOR, Color.class, 
            I18N.gm("property_display_name_color"), "");
        
        ShapeGeneralProperty propertyBoderColor = new ShapeGeneralProperty(Shape.PROPERTY_BORDER_COLOR, Color.class, 
            I18N.gm("property_display_name_shape_border_color"), "");
        
        ShapeGeneralProperty propertyIsEquipment = new ShapeGeneralProperty(Shape.PROPERTY_IS_EQUIPMENT, Boolean.class, 
            I18N.gm("property_display_name_shape_is_equipment"), "");
        
        ShapeGeneralProperty propertyOpaque = new ShapeGeneralProperty(Shape.PROPERTY_OPAQUE, Boolean.class, 
            I18N.gm("property_display_name_opaque"), "");
        
        generalPropertySet.setDisplayName(I18N.gm("property_set_display_name_general"));
        generalPropertySet.setName("general");  //NOI18N
        generalPropertySet.put(propertyName);
        generalPropertySet.put(propertyX);
        generalPropertySet.put(propertyY);
        generalPropertySet.put(propertyWidth);
        generalPropertySet.put(propertyHeigth);
        generalPropertySet.put(propertyColor);
        generalPropertySet.put(propertyBoderColor);
        ////generalPropertySet.put(propertyIsEquipment);
        generalPropertySet.put(propertyOpaque);
        sheet.put(generalPropertySet);
        
        propertiesPropertySet.setDisplayName(I18N.gm("property_set_display_name_properties"));
        propertiesPropertySet.setName("properties");  //NOI18N
        if (getShape() instanceof RectangleShape) {
            ShapeGeneralProperty propertyIsSlot = new ShapeGeneralProperty(RectangleShape.PROPERTY_IS_SLOT, Boolean.class, 
                I18N.gm("property_display_name_shape_is_slot"), "");
            
            propertiesPropertySet.put(propertyIsSlot);
                                    
        } else if (getShape() instanceof LabelShape) {
            ShapeGeneralProperty propertyLabel = new ShapeGeneralProperty(LabelShape.PROPERTY_LABEL, String.class, 
                I18N.gm("property_display_name_shape_label"), "");
            
            ShapeGeneralProperty propertyTextColor = new ShapeGeneralProperty(LabelShape.PROPERTY_TEXT_COLOR, Color.class, 
                I18N.gm("property_display_name_shape_text_color"), "");   
            
            propertiesPropertySet.put(propertyLabel);
            propertiesPropertySet.put(propertyTextColor);
        } else if (getShape() instanceof CircleShape) {
            ShapeGeneralProperty propertyEllipseColor = new ShapeGeneralProperty(
                CircleShape.PROPERTY_ELLIPSE_COLOR, Color.class, 
                I18N.gm("property_display_name_shape_outline_color"), "");
            
            ShapeGeneralProperty propertyOvalColor = new ShapeGeneralProperty(
                CircleShape.PROPERTY_OVAL_COLOR, Color.class, 
                I18N.gm("property_display_name_shape_interior_color"), "");
            
            propertiesPropertySet.put(propertyEllipseColor);
            propertiesPropertySet.put(propertyOvalColor);
        } else if (getShape() instanceof PolygonShape) {
            
            ShapeGeneralProperty propertyPolygonOutlineColor = new ShapeGeneralProperty(
                    PolygonShape.PROPERTY_OUTLINE_COLOR, Color.class, 
                    I18N.gm("property_display_name_shape_outline_color"), "");
            
            ShapeGeneralProperty propertyPolygonInteriorColor = new ShapeGeneralProperty(
                    PolygonShape.PROPERTY_INTERIOR_COLOR, Color.class, 
                    I18N.gm("property_display_name_shape_interior_color"), "");
            
            propertiesPropertySet.put(propertyPolygonOutlineColor);
            propertiesPropertySet.put(propertyPolygonInteriorColor);
        }
        sheet.put(propertiesPropertySet);
        return sheet;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setSheet(createSheet());
    }
}

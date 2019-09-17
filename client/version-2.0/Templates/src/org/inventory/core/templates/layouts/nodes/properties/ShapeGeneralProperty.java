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
package org.inventory.core.templates.layouts.nodes.properties;

import java.awt.Color;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.inventory.core.templates.layouts.lookup.SharedContent;
import org.inventory.core.templates.layouts.model.CircleShape;
import org.inventory.core.templates.layouts.model.LabelShape;
import org.inventory.core.templates.layouts.model.PolygonShape;
import org.inventory.core.templates.layouts.model.RectangleShape;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.nodes.ShapeNode;
import org.openide.nodes.PropertySupport;

/**
 * Property for a shape node
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ShapeGeneralProperty extends PropertySupport.ReadWrite {

    public ShapeGeneralProperty(String name, Class type, String displayName, String shortDescription) {
        super(name, type, displayName, shortDescription);
    }
    
    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        ShapeNode shapeNode = SharedContent.getInstance().getAbstractLookup().lookup(ShapeNode.class);
        Shape shape = shapeNode.getLookup().lookup(Shape.class);
        
        if (Shape.PROPERTY_NAME.equals(getName()))
            return shape.getName();        
        else if (Shape.PROPERTY_X.equals(getName()))
            return shape.getX();
        else if (Shape.PROPERTY_Y.equals(getName()))
            return shape.getY();
        else if (Shape.PROPERTY_WIDTH.equals(getName()))
            return shape.getWidth();
        else if (Shape.PROPERTY_HEIGHT.equals(getName()))
            return shape.getHeight();        
        else if (Shape.PROPERTY_COLOR.equals(getName()))            
            return shape.getColor();
        else if (Shape.PROPERTY_BORDER_WIDTH.equals(getName()))
            return shape.getBorderWidth();
        else if (Shape.PROPERTY_BORDER_COLOR.equals(getName()))
            return shape.getBorderColor();
        else if (Shape.PROPERTY_BORDER_COLOR.equals(getName()))
            return shape.getBorderColor();
        else if (Shape.PROPERTY_IS_EQUIPMENT.equals(getName()))
            return shape.isEquipment();
        else if (Shape.PROPERTY_OPAQUE.equals(getName()))
            return shape.isOpaque();
        else if (shape instanceof RectangleShape) {
            if (RectangleShape.PROPERTY_IS_SLOT.equals(getName()))
                return ((RectangleShape) shape).isSlot();
        }
        else if (shape instanceof LabelShape) {
            if (LabelShape.PROPERTY_LABEL.equals(getName()))
                return ((LabelShape) shape).getLabel();
            if (LabelShape.PROPERTY_TEXT_COLOR.equals(getName()))
                return ((LabelShape) shape).getTextColor();
            if (LabelShape.PROPERTY_FONT_SIZE.equals(getName()))
                return ((LabelShape) shape).getFontSize();
        }
        else if (shape instanceof CircleShape) {
            if (CircleShape.PROPERTY_ELLIPSE_COLOR.equals(getName()))
                return ((CircleShape) shape).getEllipseColor();
            if (CircleShape.PROPERTY_OVAL_COLOR.equals(getName()))
                return ((CircleShape) shape).getOvalColor();
        }
        else if (shape instanceof PolygonShape) {
            if (PolygonShape.PROPERTY_INTERIOR_COLOR.equals(getName()))
                return ((PolygonShape) shape).getInteriorColor();
            if (PolygonShape.PROPERTY_OUTLINE_COLOR.equals(getName()))
                return ((PolygonShape) shape).getOutlineColor();
        }
        return null;
    }

    @Override
    public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ShapeNode shapeNode = SharedContent.getInstance().getAbstractLookup().lookup(ShapeNode.class);
        Shape shape = shapeNode.getLookup().lookup(Shape.class);
        if (shape instanceof RectangleShape) {
            if (RectangleShape.PROPERTY_IS_SLOT.equals(getName())) {
                shape.firePropertyChange(shapeNode, RectangleShape.PROPERTY_IS_SLOT, ((RectangleShape) shape).isSlot(), val);
                ((RectangleShape) shape).setIsSlot((Boolean) val);
                return;
            }
        } else if (shape instanceof LabelShape) {
            if (LabelShape.PROPERTY_LABEL.equals(getName())) {
                shape.firePropertyChange(shapeNode, LabelShape.PROPERTY_LABEL, ((LabelShape) shape).getLabel(), val);
                ((LabelShape) shape).setLabel((String) val);
                return;
            }
            if (LabelShape.PROPERTY_TEXT_COLOR.equals(getName())) {
                shape.firePropertyChange(shapeNode, LabelShape.PROPERTY_TEXT_COLOR, ((LabelShape) shape).getTextColor(), val);
                ((LabelShape) shape).setTextColor((Color) val);
                return;
            }
            if (LabelShape.PROPERTY_FONT_SIZE.equals(getName())) {
                shape.firePropertyChange(shapeNode, LabelShape.PROPERTY_FONT_SIZE, ((LabelShape) shape).getFontSize(), val);
                ((LabelShape) shape).setFontSize((Integer) val);
                return;
            }
        } else if (shape instanceof CircleShape) {
            if (CircleShape.PROPERTY_ELLIPSE_COLOR.equals(getName())) {
                shape.firePropertyChange(shapeNode, CircleShape.PROPERTY_ELLIPSE_COLOR, ((CircleShape) shape).getEllipseColor(), val);
                ((CircleShape) shape).setEllipseColor((Color) val);
                return;
            }
            if (CircleShape.PROPERTY_OVAL_COLOR.equals(getName())) {
                shape.firePropertyChange(shapeNode, CircleShape.PROPERTY_OVAL_COLOR, ((CircleShape) shape).getOvalColor(), val);
                ((CircleShape) shape).setOvalColor((Color) val);
                return;
            }
        } else if (shape instanceof PolygonShape) {
            if (PolygonShape.PROPERTY_INTERIOR_COLOR.equals(getName())) {
                ((PolygonShape) shape).setInteriorColor((Color) val);
                shape.firePropertyChange(shapeNode, PolygonShape.PROPERTY_INTERIOR_COLOR, ((PolygonShape) shape).getInteriorColor(), val);
                return;
            }
            if (PolygonShape.PROPERTY_OUTLINE_COLOR.equals(getName())) {
                ((PolygonShape) shape).setOutlineColor((Color) val);
                shape.firePropertyChange(shapeNode, PolygonShape.PROPERTY_OUTLINE_COLOR, ((PolygonShape) shape).getOutlineColor(), val);
                return;
            }
        }
        if (Shape.PROPERTY_NAME.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_NAME, shape.getName(), val);
            shape.setName((String) val);
        } else if (Shape.PROPERTY_X.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_X, shape.getX(), val);
            shape.setX((Integer) val);
        } else if (Shape.PROPERTY_Y.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_Y, shape.getY(), val);
            shape.setY((Integer) val);
        } else if (Shape.PROPERTY_WIDTH.equals(getName())) {            
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_WIDTH, shape.getWidth(), val);            
            shape.setWidth((Integer) val);
        } else if (Shape.PROPERTY_HEIGHT.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_HEIGHT, shape.getHeight(), val);
            shape.setHeight((Integer) val);
        } else if (Shape.PROPERTY_COLOR.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_COLOR, shape.getColor(), val);
            shape.setColor((Color) val);
        } else if (Shape.PROPERTY_BORDER_WIDTH.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_BORDER_WIDTH, shape.getBorderWidth(), val);
            shape.setBorderWidth((Integer) val);
        } else if (Shape.PROPERTY_BORDER_COLOR.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_BORDER_COLOR, shape.getBorderColor(), val);
            shape.setBorderColor((Color) val);
        } else if (Shape.PROPERTY_IS_EQUIPMENT.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_IS_EQUIPMENT, shape.isEquipment(), val);
            shape.setIsEquipment((Boolean) val);
        } else if (Shape.PROPERTY_OPAQUE.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_OPAQUE, shape.isOpaque(), val);
            shape.setOpaque((Boolean) val);
        }
    }
    
    @Override
    public PropertyEditor getPropertyEditor(){
        if (Shape.PROPERTY_NAME.equals(getName())) {
            return new ShapeNamePropertyEditor();            
        } else {
            return super.getPropertyEditor();
        }
    }    
}

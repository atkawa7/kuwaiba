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
package org.inventory.design.modelsLayouts.nodes.properties;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import org.inventory.design.modelsLayouts.lookup.SharedContent;
import org.inventory.design.modelsLayouts.model.LabelShape;
import org.inventory.design.modelsLayouts.model.Shape;
import org.inventory.design.modelsLayouts.nodes.ShapeNode;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
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
        if (shape instanceof LabelShape && LabelShape.PROPERTY_LABEL.equals(getName()))
            return ((LabelShape) shape).getLabel();
        if (shape instanceof LabelShape && LabelShape.PROPERTY_TEXT_COLOR.equals(getName()))
            return ((LabelShape) shape).getTextColor();
        return null;
    }

    @Override
    public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ShapeNode shapeNode = SharedContent.getInstance().getAbstractLookup().lookup(ShapeNode.class);
        Shape shape = shapeNode.getLookup().lookup(Shape.class);
        
        if (shape instanceof LabelShape && LabelShape.PROPERTY_LABEL.equals(getName())) {
            shape.firePropertyChange(shapeNode, LabelShape.PROPERTY_LABEL, ((LabelShape) shape).getLabel(), val);
            ((LabelShape) shape).setLabel((String) val);
            return;
        }
        
        if (shape instanceof LabelShape && LabelShape.PROPERTY_TEXT_COLOR.equals(getName())) {
            shape.firePropertyChange(shapeNode, LabelShape.PROPERTY_TEXT_COLOR, ((LabelShape) shape).getTextColor(), val);
            ((LabelShape) shape).setTextColor((Color) val);
            return;
        }
        
        if (Shape.PROPERTY_NAME.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_NAME, shape.getName(), val);
            shape.setName((String) val);
        }
        else if (Shape.PROPERTY_X.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_X, shape.getX(), val);
            shape.setX((Integer) val);
        }
        else if (Shape.PROPERTY_Y.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_Y, shape.getY(), val);
            shape.setY((Integer) val);
        }
        else if (Shape.PROPERTY_WIDTH.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_WIDTH, shape.getWidth(), val);
            shape.setWidth((Integer) val);
        }
        else if (Shape.PROPERTY_HEIGHT.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_HEIGHT, shape.getHeight(), val);
            shape.setHeight((Integer) val);
        }
        else if (Shape.PROPERTY_COLOR.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_COLOR, shape.getColor(), val);
            shape.setColor((Color) val);
        } else if (Shape.PROPERTY_BORDER_WIDTH.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_BORDER_WIDTH, shape.getBorderWidth(), val);
            shape.setBorderWidth((Integer) val);
        } else if (Shape.PROPERTY_BORDER_COLOR.equals(getName())) {
            shape.firePropertyChange(shapeNode, Shape.PROPERTY_BORDER_COLOR, shape.getBorderColor(), val);
            shape.setBorderColor((Color) val);
        }
    }
    
}

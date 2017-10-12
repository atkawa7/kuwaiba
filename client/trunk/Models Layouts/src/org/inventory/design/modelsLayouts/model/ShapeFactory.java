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
package org.inventory.design.modelsLayouts.model;

/**
 * Factory used to get instances of shapes, given a type of shape
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ShapeFactory {
    private static ShapeFactory instance;
    
    private ShapeFactory() {
    }
    
    public static ShapeFactory getInstance() {
        return instance == null ? instance = new ShapeFactory() : instance;        
    }
    
    public Shape getShape(String type) {
        if (type == null)
            return null;
        
        if (RectangleShape.SHAPE_TYPE.equals(type))
            return new RectangleShape();
        
        if (LabelShape.SHAPE_TYPE.equals(type))
            return new LabelShape();
        
        return null;
    }    
}

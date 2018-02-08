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
package org.inventory.core.templates.layouts.model;

import java.awt.Color;

/**
 * Class used to represent polygons 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PolygonShape extends Shape {
    public static String SHAPE_TYPE = "polygon"; //NOI18N
    public static String PROPERTY_NUM_OF_SIDES = "numberOfSides"; //NOI18N
    public static String PROPERTY_OUTLINE_COLOR = "outlineColor"; //NOI18N
    public static String PROPERTY_INTERIOR_COLOR = "interiorColor"; //NOI18N
    
    private int numberOfSides = 3; // default value the minimum number of sides
    private Color outlineColor = Color.BLACK;
    private Color interiorColor = Color.WHITE;
    
    public PolygonShape() {
        super();
        setOpaque(false);
    }
    
    public PolygonShape(String urlIcon) {
        super(urlIcon);
        setOpaque(false);
    }
    
    public PolygonShape(Shape parent) {
        super(parent);
        setOpaque(false);
    }
    
    public Integer getNumberOfSides() {
        return numberOfSides;
    }
    
    public void setNumberOfSides(Integer numberOfSides) {
        this.numberOfSides = numberOfSides;        
    }
    
    public Color getOutlineColor() {
        return outlineColor;        
    }
    
    public void setOutlineColor(Color outlineColor) {
        this.outlineColor = outlineColor;
    }
    
    public Color getInteriorColor() {
        return interiorColor;
    }
    
    public void setInteriorColor(Color interiorColor) {
        this.interiorColor = interiorColor;        
    }
    
    
    @Override
    public Shape shapeCopy() {
        PolygonShape shapeCpy = new PolygonShape();
        shapeCopy(shapeCpy);
        return shapeCpy;
    }
    
    @Override
    protected void shapeCopy(Shape shapeCpy) {   
        super.shapeCopy(shapeCpy);
        ((PolygonShape) shapeCpy).setNumberOfSides(this.getNumberOfSides());
        ((PolygonShape) shapeCpy).setOutlineColor(this.getOutlineColor());
        ((PolygonShape) shapeCpy).setInteriorColor(this.getInteriorColor());
    }

    @Override
    public String getShapeType() {
        return SHAPE_TYPE;
    }
    
}

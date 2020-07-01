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
package org.inventory.core.templates.layouts.model;

import java.awt.Color;

/**
 * Class used to represent circles
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CircleShape extends Shape {
    public static String SHAPE_TYPE = "ellipse"; //NOI18N    
    public static String PROPERTY_ELLIPSE_COLOR = "ellipseColor"; //NOI18N
    public static String PROPERTY_OVAL_COLOR = "ovalColor"; //NOI18N
        
    private Color ellipseColor = Color.WHITE;
    private Color ovalColor = Color.BLACK;
    
    public CircleShape() {
        super();
        setOpaque(false);
    }
    
    public CircleShape(String urlIcon) {
        super(urlIcon);
        setOpaque(false);
    }
    
    public CircleShape(Shape parent) {
        super(parent);
        setBorderColor(parent.getColor());
        setOpaque(false);
    }
    
    @Override
    public String getShapeType() {
        return SHAPE_TYPE;     
    }
    
    public Color getEllipseColor() {
        return ellipseColor;
    }
        
    public void setEllipseColor(Color ellipseColor) {
        this.ellipseColor = ellipseColor;
    }
    
    public Color getOvalColor() {
        return ovalColor;
    }
    
    public void setOvalColor(Color ovalColor) {
        this.ovalColor = ovalColor;        
    }
    
    @Override
    public Shape shapeCopy() {
        CircleShape shapeCpy = new CircleShape();
        shapeCopy(shapeCpy);
        return shapeCpy;
    }
    
    @Override
    protected void shapeCopy(Shape shapeCpy) {   
        super.shapeCopy(shapeCpy);
        ((CircleShape) shapeCpy).setEllipseColor(this.getEllipseColor());
        ((CircleShape) shapeCpy).setOvalColor(this.getOvalColor());
    }
}

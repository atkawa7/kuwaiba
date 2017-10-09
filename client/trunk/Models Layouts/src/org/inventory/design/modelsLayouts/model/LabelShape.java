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

import java.awt.Color;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class LabelShape extends Shape {
    public static String SHAPE_TYPE = "label";
    public static String PROPERTY_LABEL = "label";
    public static String PROPERTY_TEXT_COLOR = "textColor";
    private String label;
    private Color textColor = Color.BLACK;
    
    public LabelShape() {
        super();
        label = "New Label";
    }
    
    public LabelShape(String urlIcon) {
        super(urlIcon);
    }
    
    public LabelShape(Shape parent) {
        super(parent);
        label = "New Label";
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public Color getTextColor() {
        return textColor;
    }
    
    public void setTextColor(Color textColor) {
        this.textColor = textColor;        
    }
    
    @Override
    public Shape shapeCopy() {
        LabelShape shapeCpy = new LabelShape();
        shapeCopy(shapeCpy);
        return shapeCpy;
    }
    
    @Override
    protected void shapeCopy(Shape shapeCpy) {   
        super.shapeCopy(shapeCpy);
        ((LabelShape) shapeCpy).setLabel(this.getLabel());
    }
}

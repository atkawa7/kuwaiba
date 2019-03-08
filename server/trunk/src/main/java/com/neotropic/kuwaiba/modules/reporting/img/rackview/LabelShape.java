/**
 *  Copyright 2010-2019, Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.reporting.img.rackview;

import java.awt.Color;

/**
 * Class used to represent labels
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class LabelShape extends Shape {
    public static String SHAPE_TYPE = "label"; //NOI18N
    public static String PROPERTY_LABEL = "label"; //NOI18N
    public static String PROPERTY_TEXT_COLOR = "textColor"; //NOI18N
    public static String PROPERTY_FONT_SIZE = "fontSize"; //NOI18N
    private String label;
    private Color textColor = Color.BLACK;
    private Integer fontSize = 11;
    
    public LabelShape() {
        super();
        label = "New Label";
        setOpaque(false);
    }
    
    public LabelShape(String urlIcon) {
        super(urlIcon);
        setOpaque(false);
    }
    
    public LabelShape(Shape parent) {
        super(parent);
        label = "New Label";
        setOpaque(false);
    }
    
    @Override
    public String getShapeType() {
        return SHAPE_TYPE;     
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
    
    public Integer getFontSize() {
        return fontSize;
    }
    
    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;        
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
        ((LabelShape) shapeCpy).setTextColor(this.getTextColor());
        ((LabelShape) shapeCpy).setFontSize(this.getFontSize());
    }
}


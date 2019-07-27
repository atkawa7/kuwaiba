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

/**
 * Class used to represent rectangles
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RectangleShape extends Shape {
    public static final String SHAPE_TYPE = "rectangle";  //NOI18N
    public static final String PROPERTY_IS_SLOT = "isSlot";
    
    private boolean isSlot = false;
    
    public RectangleShape() {
        super();
    }
    
    public RectangleShape(String urlIcon) {
        super(urlIcon);
    }
    
    public RectangleShape(Shape parent) {
        super(parent);
    }
    
    public boolean isSlot() {
        return isSlot;
    }
        
    public void setIsSlot(boolean isSlot) {
        this.isSlot = isSlot;                
    }
        
    @Override
    public String getShapeType() {
        return SHAPE_TYPE;     
    }
    
    @Override
    public Shape shapeCopy() {
        RectangleShape shapeCpy = new RectangleShape();
        shapeCopy(shapeCpy);
        return shapeCpy;
    }
    
    @Override
    protected void shapeCopy(Shape shapeCpy) {   
        super.shapeCopy(shapeCpy);
    }
}

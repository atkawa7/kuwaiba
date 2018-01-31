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

import java.awt.Image;
import org.inventory.communications.core.LocalObjectListItem;

/**
 * A custom shape is a predefined shape used the custom shape tool
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CustomShape extends Shape {
    public static String SHAPE_TYPE = "custom"; //NOI18N
    
    private Image icon;
    private LocalObjectListItem listItem;
        
    public CustomShape(LocalObjectListItem listItem) {
        super();
        this.listItem = listItem;
        setOpaque(false);
    }
    
    public CustomShape(LocalObjectListItem object, Image icon) {
        this(object);
        this.icon = icon;
    }
    
    public Image getIcon() {
        return icon;
    }
    
    public void setIcon(Image icon) {
        this.icon = icon;
    }
    
    public LocalObjectListItem getListItem() {
        return listItem;        
    }
    
    public void setListItem(LocalObjectListItem object) {
        this.listItem = object;
    }
    
    @Override
    public Shape shapeCopy() {
        CustomShape customShape = new CustomShape(getListItem(), getIcon());
        shapeCopy(customShape);
        return customShape;
    }

    @Override
    public String getShapeType() {
        return SHAPE_TYPE;
    }
    
    @Override
    protected void shapeCopy(Shape shapeCpy) {   
        super.shapeCopy(shapeCpy);
    }
}

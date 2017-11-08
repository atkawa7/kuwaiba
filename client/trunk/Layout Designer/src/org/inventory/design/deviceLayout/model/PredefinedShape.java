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
package org.inventory.design.deviceLayout.model;

import java.awt.Image;
import org.inventory.communications.core.LocalObjectListItem;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PredefinedShape extends Shape {
    private Image icon;
    private LocalObjectListItem object;
    
    public PredefinedShape(LocalObjectListItem object) {
        this.object = object;
    }
    
    public PredefinedShape(LocalObjectListItem object, Image icon) {
        this(object);
        this.icon = icon;
    }
    
    public Image getIcon() {
        return icon;
    }
    
    public void setIcon(Image icon) {
        this.icon = icon;
    }
    
    public LocalObjectListItem getObject() {
        return object;        
    }
    
    public void setObject(LocalObjectListItem object) {
        this.object = object;
    }
    
    @Override
    public Shape shapeCopy() {
        PredefinedShape predefinedShape = new PredefinedShape(getObject(), getIcon());
        shapeCopy(predefinedShape);
        return predefinedShape;
    }

    @Override
    public String getShapeType() {
        return "predefinedShape";
    }
}

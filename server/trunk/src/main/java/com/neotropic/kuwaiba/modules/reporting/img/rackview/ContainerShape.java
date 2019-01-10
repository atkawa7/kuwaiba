/*
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

/**
 * A Container Shape is a set of shapes grouped into the container
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ContainerShape extends Shape {
    public static final String SHAPE_TYPE = "container"; //NOI18N
            
    public ContainerShape() {
        super();
        setOpaque(false);
    }
    
    public ContainerShape(String urlIcon) {
        super(urlIcon);
        setOpaque(false);
    }
    
    @Override
    public String getShapeType() {
        return SHAPE_TYPE;     
    }
    
    @Override
    public Shape shapeCopy() {
        ContainerShape customShape = new ContainerShape();
        shapeCopy(customShape);
        return customShape;
    }

    @Override
    protected void shapeCopy(Shape shapeCpy) {   
        super.shapeCopy(shapeCpy);
    }
}

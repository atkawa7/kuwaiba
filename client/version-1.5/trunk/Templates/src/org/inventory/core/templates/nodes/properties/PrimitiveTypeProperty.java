/*
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.core.templates.nodes.properties;

import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.core.LocalObject;
import org.openide.nodes.PropertySupport;

/**
 * This class allows to edit primitive type properties.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PrimitiveTypeProperty extends PropertySupport.ReadWrite<Object> {
    private LocalObject businessObject;
    
    public PrimitiveTypeProperty(String propertyName, Class propertyType, String displayName, 
            String shortDescription, LocalObject businessObject) {
        super(propertyName, propertyType, displayName, shortDescription);
        this.businessObject = businessObject;
    }
    
    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return businessObject.getAttribute(getName());
    }

    @Override
    public void setValue(Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        businessObject.setAttribute(getName(), value);    
    }
}
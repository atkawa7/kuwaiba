/*
 *  Copyright 2010-2018, Neotropic SAS <contact@neotropic.co>
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

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.util.Constants;
import org.inventory.navigation.navigationtree.nodes.properties.DatePickerPropertyEditorSupport;
import org.openide.nodes.PropertySupport;

/**
 * This class allows to edit date-like properties.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class DateTypeProperty extends PropertySupport.ReadWrite<Date> {
    private LocalObject businessObject;
    
    public DateTypeProperty(String propertyName, String displayName, String shortDescription, LocalObject businessObject) {
        super(propertyName, Date.class, displayName, shortDescription);
        this.businessObject = businessObject;
    }
    
    @Override
    public Date getValue() throws IllegalAccessException, InvocationTargetException {
        return (Date)businessObject.getAttribute(getName());
    }

    @Override
    public void setValue(Date value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        businessObject.setAttribute(getName(), value);        
    }
    @Override
    public PropertyEditor getPropertyEditor(){        
        return new DatePickerPropertyEditorSupport((Date)businessObject.getAttribute(getName()));
    }

    @Override
    public boolean canWrite(){
        //The creation date is read-only data
        return !getName().equals(Constants.PROPERTY_CREATIONDATE);   
    }
}
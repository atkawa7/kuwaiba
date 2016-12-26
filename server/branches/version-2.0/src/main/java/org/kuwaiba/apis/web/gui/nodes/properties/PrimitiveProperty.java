/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.web.gui.nodes.properties;

import com.vaadin.data.Validator;
import com.vaadin.ui.TextField;

/**
 * A field to represent primitive values(numeric, string) in a property sheet
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class PrimitiveProperty extends TextField{
    
    private String propertyName;
    private String propertyType;
    
    public PrimitiveProperty(String propertyName, String propertyType, String value) {
        super();
        this.propertyName = propertyName;
        this.propertyType = propertyType;
        setValue(value == null ? "" : (String)value);
    }
    
    public PrimitiveProperty(int value) {
        super();
        setConverter(Integer.class);
        setValue(Integer.toString(value));
        this.addValidator(new numericValidator());
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPropertyType() {
        return propertyType;
    }
    
    private class numericValidator implements Validator {
        @Override
        public void validate(Object value)
                throws Validator.InvalidValueException {
            if (!(value instanceof Integer &&
                    (value instanceof Double))) {
                throw new Validator.InvalidValueException("The value is not a number");
            }     
        }
    }
}

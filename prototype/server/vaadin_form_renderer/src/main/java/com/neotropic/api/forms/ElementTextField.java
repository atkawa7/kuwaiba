/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.neotropic.api.forms;

import com.neotropic.web.components.ChangeDescriptor;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ElementTextField extends AbstractElement {
    
    private String value;    
    private boolean enabled = true;
    
    public ElementTextField() {
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;        
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void initFromXMl(XMLStreamReader reader) throws XMLStreamException {
        setId(reader);
        setArea(reader);
        setEvents(reader);
                
        String attrValue = reader.getAttributeValue(null, Constants.Attribute.VALUE);
////        if (attrValue == null)
////            throw new XMLStreamException(String.format("Missing attribute %s in tag %s", Constants.Attribute.VALUE, Constants.Tag.TEXT_FIELD));
        
        value = attrValue;
        
        attrValue = reader.getAttributeValue(null, Constants.Attribute.ENABLED);
        
        if (attrValue != null)
            enabled = Boolean.valueOf(attrValue);
    }

    @Override
    public void componentChange(ChangeDescriptor changeDescriptor) {
        if (changeDescriptor != null) {
            
            if (Constants.Attribute.VALUE.equals(changeDescriptor.getPropertyName()))
                value = (String) changeDescriptor.getNewValue();
        }
    }
        
}

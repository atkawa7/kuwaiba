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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * A field is a terminal element that contain data
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public abstract class AbstractElementField extends AbstractElement {
    private Object value;
    
    public Object getValue() {
        return value;        
    }
    
    public void setValue(Object value) {
        this.value = value;        
    }
    
    @Override
    public void onComponentEvent(EventDescriptor event) {
        if (this instanceof AbstractElementField) {
            if (Constants.EventAttribute.ONVALUECHANGE.equals(event.getName())) {
                if (event.getNewValue() != null || event.getOldValue() != null)
                    ((AbstractElementField) this).setValue(event.getNewValue());
            }
        }
        super.onComponentEvent(event);
    }
            
    @Override
    public void initFromXMl(XMLStreamReader reader) throws XMLStreamException {
        super.initFromXMl(reader);
        value = reader.getAttributeValue(null, Constants.Attribute.VALUE);                
    }
    
}
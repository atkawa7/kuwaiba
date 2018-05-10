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

import com.vaadin.ui.Notification;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * A field is a terminal element that contain data
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public abstract class AbstractElementField extends AbstractElement {
    private Object value;
    private boolean mandatory = false;
    private boolean cleanable = true;
    private String dataType;
    
    public Object getValue() {
        return value;        
    }
    
    public void setValue(Object value) {
        this.value = value;        
    }
    
    public boolean isMandatory() {
        return mandatory;
    }
    
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;                
    }
    
    public boolean isCleanable() {
        return cleanable;
    }
    
    public void setCleanable(boolean cleanable) {
        this.cleanable = cleanable;        
    }
    
    public String getDataType() {
        return dataType;                
    }
        
    public void setDataType(String dataType) {
        this.dataType = dataType;        
    }
        
    @Override
    public void onComponentEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            if (event.getNewValue() != null || event.getOldValue() != null) {
                
                if (Constants.Property.VALUE.equals(event.getPropertyName())) {
                                        
                    setValue(event.getNewValue()); // TODO: rollback
                    
                    if (hasProperty(Constants.Function.VALIDATE, Constants.Property.VALUE)) {
                            
                        List<String> list = getEvents().get(Constants.Function.VALIDATE).get(Constants.Property.VALUE);

                        if (list != null && !list.isEmpty()) {

                            String functionName = list.get(0);

                            Validator validator = (Validator) getFormStructure().getElementScript().getFunctionByName(functionName);

                            List parameters = new ArrayList();

                            for (int i = 1; i < list.size(); i += 1) {
                                AbstractElement anElement = getFormStructure().getElementById(list.get(i));
                                parameters.add(anElement != null ? anElement : list.get(i));
                            }
                            
                            Object newValue = validator.run(parameters);
                            if (newValue instanceof Boolean && !((Boolean) newValue)) {
                                Notification.show("WARNING", validator.getMessage(), Notification.Type.WARNING_MESSAGE);
                                return;
                            }
                        }
                    }
                    firePropertyChangeEvent();
                }
            }
        }
        super.onComponentEvent(event);
    }
    
    @Override
    public void fireOnload() {
        super.fireOnload(); 
        
        if (hasProperty(Constants.EventAttribute.ONLOAD, Constants.Property.VALUE)) {
            
            List<String> list = getEvents().get(Constants.EventAttribute.ONLOAD).get(Constants.Property.VALUE);

            if (list != null && !list.isEmpty()) {

                String functionName = list.get(0);

                Runner runner = getFormStructure().getElementScript().getFunctionByName(functionName);

                List parameters = new ArrayList();

                for (int i = 1; i < list.size(); i += 1) {
                    AbstractElement anElement = getFormStructure().getElementById(list.get(i));
                    parameters.add(anElement != null ? anElement : list.get(i));
                }

                Object newValue = runner.run(parameters);
                
                setValue(newValue);
                
                fireElementEvent(new EventDescriptor(
                    Constants.EventAttribute.ONPROPERTYCHANGE, 
                    Constants.Property.VALUE, newValue, null));
            }
        }                        
    }
            
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        super.initFromXML(reader);
        setValue(reader);
        setMandatory(reader);
        setDataType(reader);
    }
    
    private void setValue(XMLStreamReader reader) {
        value = reader.getAttributeValue(null, Constants.Attribute.VALUE);        
    }
    
    private void setMandatory(XMLStreamReader reader) {
        String attrValue = reader.getAttributeValue(null, Constants.Attribute.MANDATORY);
                
        if (attrValue != null)
            mandatory = Boolean.valueOf(attrValue);
    }   
    
    private void setDataType(XMLStreamReader reader) {
        dataType = reader.getAttributeValue(null, Constants.Attribute.DATA_TYPE);
    }
    
    @Override
    public void propertyChange() {
        if (hasProperty(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.VALUE)) {
            
            Object oldValue = getValue();
            Object newValue = getNewValue(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.VALUE);

            setValue(newValue);

            firePropertyChangeEvent();
            
            fireElementEvent(new EventDescriptor(
                Constants.EventAttribute.ONPROPERTYCHANGE, 
                Constants.Property.VALUE, newValue, oldValue));
        }
        super.propertyChange();
    }
        
}
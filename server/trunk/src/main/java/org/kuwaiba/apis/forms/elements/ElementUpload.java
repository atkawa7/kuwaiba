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
package org.kuwaiba.apis.forms.elements;

import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ElementUpload extends AbstractElementField {
    private String caption;
    
    public ElementUpload() {
    }
    
    public String getCaption() {
        return caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;        
    }
    
    @Override
    public String getTagName() {
        return Constants.Tag.UPLOAD;
    }
    
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        super.initFromXML(reader);
        setCaption(reader);
    }
    
    private void setCaption(XMLStreamReader reader) {
        caption = reader.getAttributeValue(null, Constants.Attribute.CAPTION);
    }
    
    @Override
    public void onComponentEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            if (Constants.Property.CAPTION.equals(event.getPropertyName())) {
                
                if (event.getNewValue() instanceof String) {
                                        
                    setCaption((String) event.getNewValue());
                    firePropertyChangeEvent();
                }
            }
        }
        super.onComponentEvent(event);        
    }
    
    private void loadValue(List<String> list) {
        if (list != null && !list.isEmpty()) {

            String functionName = list.get(0);

            Runner runner = getFormStructure().getElementScript().getFunctionByName(functionName);

            List parameters = new ArrayList();

            for (int i = 1; i < list.size(); i += 1) {
                AbstractElement anElement = getFormStructure().getElementById(list.get(i));
                parameters.add(anElement != null ? anElement : list.get(i));
            }

            Object newValue = runner.run(parameters);

            setCaption((String) newValue);

            fireElementEvent(new EventDescriptor(
                Constants.EventAttribute.ONPROPERTYCHANGE, 
                Constants.Property.CAPTION, newValue, null));
        }
    }
        
    @Override
    public void fireOnLoad() {
        super.fireOnLoad(); 
        
        if (hasProperty(Constants.EventAttribute.ONLOAD, Constants.Property.CAPTION)) {
            
            List<String> list = getEvents().get(Constants.EventAttribute.ONLOAD).get(Constants.Property.CAPTION);
            
            loadValue(list);
        }                        
    }
}

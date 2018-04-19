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
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ElementButton extends AbstractElement {
    private String caption;

    
    
    public ElementButton() {
                
    }
    
    public void setCaption(String caption) {
        this.caption = caption;        
    }
    
    public String getCaption() {
        return caption;
    }
    
    @Override
    public void onComponentEvent(EventDescriptor event) {
        if (hasProperty(Constants.EventAttribute.ONCLICK, Constants.Function.OPEN)) {
            String elementId = getEvents().get(Constants.EventAttribute.ONCLICK).get(Constants.Function.OPEN).get(0);

            AbstractElement anElement = getFormStructure().getElementById(elementId);

            if (anElement instanceof ElementSubform)
                anElement.fireElementEvent(new EventDescriptor(Constants.Function.OPEN));
        }
    }
    
    @Override
    public void initFromXMl(XMLStreamReader reader) throws XMLStreamException {
        setId(reader);
        setEvents(reader);
        
        caption = reader.getAttributeValue(null, Constants.Attribute.CAPTION);
                
        if (caption == null)
            throw new XMLStreamException(String.format("Missing attribute %s in tag %s", Constants.Attribute.CAPTION, Constants.Tag.BUTTON));
        
////        events = new HashMap();
        
////        String onclickValue = reader.getAttributeValue(null, Constants.Attribute.ONCLICK);
////        if (onclickValue != null) {
            
////            String cpyOnclickValue = onclickValue;
////            String[] values = cpyOnclickValue.split("\\.");
            
////            String functionName = values[0];
            // TODO: create a list with more than one parameter in a better way
////            List<String> params = new ArrayList();
////            for (int i = 1; i < values.length; i += 1)
////                params.add(values[i]);
                                                    
////            HashMap<String, List<String>> function = new HashMap();
////            function.put(functionName, params);
            
////            events.put(Constants.Attribute.ONCLICK, function);
////        }
    }

////    @Override
////    public void componentChange(ChangeDescriptor changeDecriptor) {
////        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
////    }        
}

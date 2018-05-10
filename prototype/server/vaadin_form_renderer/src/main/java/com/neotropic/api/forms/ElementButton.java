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

import com.neotropic.forms.Variable;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        if (hasEventAttribute(Constants.EventAttribute.ONCLICK) && getEvents().get(Constants.EventAttribute.ONCLICK) != null) {
            
            for (String key : getEvents().get(Constants.EventAttribute.ONCLICK).keySet()) {
                
                if (Constants.Function.OPEN.equals(key)) {

                    String elementId = getEvents().get(Constants.EventAttribute.ONCLICK).get(Constants.Function.OPEN).get(0);

                    AbstractElement anElement = getFormStructure().getElementById(elementId);

                    if (anElement instanceof ElementSubform)
                        anElement.fireElementEvent(new EventDescriptor(Constants.EventAttribute.ONCLICK, Constants.Function.OPEN));

                } else if (Constants.Function.ADD_GRID_ROW.equals(key)) {
                    List<String> functionParams = getEvents().get(Constants.EventAttribute.ONCLICK).get(Constants.Function.ADD_GRID_ROW);

                    String elementId = functionParams.get(0);

                    List<String> elements = new ArrayList();

                    for (int i = 1; i < functionParams.size(); i += 1) {
                        AbstractElement ae = getFormStructure().getElementById(functionParams.get(i));

                        if (ae instanceof AbstractElementField) {
                            AbstractElementField aef = (AbstractElementField) ae;
                            elements.add(aef.getValue() != null ? aef.getValue().toString() : new NullObject().toString());
                        } else
                            elements.add(new NullObject().toString());
                    }

                    AbstractElement anElement = getFormStructure().getElementById(elementId);

                    if (anElement instanceof ElementGrid)
                        anElement.fireElementEvent(new EventDescriptor(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.ROWS, elements, null));

                } else if (Constants.Function.CLOSE.equals(key)) {

                    String elementId = getEvents().get(Constants.EventAttribute.ONCLICK).get(Constants.Function.CLOSE).get(0);

                    AbstractElement anElement = getFormStructure().getElementById(elementId);

                    if (anElement instanceof ElementSubform)
                        anElement.fireElementEvent(new EventDescriptor(Constants.EventAttribute.ONCLICK, Constants.Function.CLOSE));
                    
                } else if (Constants.Function.CLEAN.equals(key)) {
                    
                    String elementId = getEvents().get(Constants.EventAttribute.ONCLICK).get(Constants.Function.CLEAN).get(0);

                    AbstractElement anElement = getFormStructure().getElementById(elementId);

                    if (anElement instanceof AbstractElementContainer) {
                        ((AbstractElementContainer) anElement).clean();
                        anElement.fireElementEvent(new EventDescriptor(Constants.EventAttribute.ONCLICK, Constants.Function.CLEAN));
                    }
                } else if (Constants.Function.OPEN_FORM.equals(key)) {
                    String form = getEvents().get(Constants.EventAttribute.ONCLICK).get(Constants.Function.OPEN_FORM).get(0);
                    fireElementEvent(new EventDescriptor(Constants.EventAttribute.ONCLICK, Constants.Function.OPEN_FORM, form, null));
                    
                } else if (Constants.Function.SAVE.equals(key)) {
                                                            
                    byte [] structure = new FormInstanceCreator(getFormStructure()).getStructure();
                    
                    // TODO: save by form id instance
                    try {
                        String fileName = Variable.FORM_RESOURCE_INSTANCES + "/" + "instance" + String.valueOf(new Date().getTime()) + ".xml";
                        PrintWriter printWriter = new PrintWriter(fileName);
                        printWriter.print(new String(structure));
                        printWriter.close();
                        
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ElementButton.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
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
    public String getTagName() {
        return Constants.Tag.BUTTON;       
    }
    
}

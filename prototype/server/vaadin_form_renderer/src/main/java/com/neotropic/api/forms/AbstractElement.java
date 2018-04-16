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

import com.neotropic.web.components.ComponentEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * 
 * API IN TOP FORM
 * General java representation of a Tag
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public abstract class AbstractElement implements Tag, ComponentEventListener {
    /**
     * Tags of Attributes
     */
    private String id;
    private String name;
    private List<Integer> area;
    private String styleName;
    private boolean enabled = true;
    /**
     * event->function->parameters
     */    
    private HashMap<String, HashMap<String, List<String>>> events;
    
    private ScriptRunner scriptRunner;    
    
    private ElementEventListener elementEventListener;
    
    public String getId() {
        return id;
    }
        
    public void setId(String id) {
        this.id = id;
    }
        
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
        
    public List<Integer> getArea() {
        return area;
    }
        
    public void setArea(List<Integer> area) {
        this.area = area;        
    }
        
    public String getStyleName() {
        return styleName;
    }
        
    public void setStyleName(String styleName) {
        this.styleName = styleName;        
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public HashMap<String, HashMap<String, List<String>>> getEvents() {
        return events;
    }
        
    public void setEvents(HashMap<String, HashMap<String, List<String>>> events) {
        this.events = events;
    }
    
    public ScriptRunner getScriptRunner() {
        return scriptRunner;
    }
    
    public void setScriptRunner(ScriptRunner scriptRunner) {
        this.scriptRunner = scriptRunner;
    }
        
    @Override
    public void onComponentEvent(EventDescriptor event) {
        getScriptRunner().run(this, event.getName());
    }
    
    public void setElementEventListener(ElementEventListener elementEventListener) {
        this.elementEventListener = elementEventListener;
    }
    
    public ElementEventListener getElementEventListener() {
        return elementEventListener;
    }
    
    public void fireElementEvent(EventDescriptor eventDescriptor) {
        elementEventListener.onElementEvent(eventDescriptor);
    }    
    
    @Override
    public void initFromXMl(XMLStreamReader reader) throws XMLStreamException {
        setId(reader);
        setArea(reader);
        setEvents(reader);
        setStyleName(reader);
    }
    
    public void setId(XMLStreamReader reader) {
        id = reader.getAttributeValue(null, Constants.Attribute.ID);
    }
    
    public void setArea(XMLStreamReader reader) {
        String attrValue = reader.getAttributeValue(null, Constants.Attribute.AREA);
        
        if (attrValue != null) {
            String[] elements = attrValue.split(",");
            
            if (elements != null && elements.length >= 1) {
                area = new ArrayList();
                                
                for (String element : elements)
                    area.add(Integer.valueOf(element));
            }
        }
    }
    
    public void setEvents(XMLStreamReader reader) {
        String [] eventAttrs = {
            Constants.EventAttribute.ONCLICK, 
            Constants.EventAttribute.ONVALUECHANGE, 
            Constants.EventAttribute.ONNOTIFY};
        
        for (String eventAttr : eventAttrs) {
            
            String eventValue = reader.getAttributeValue(null, eventAttr);
            
            if (eventValue != null) {
                
                if (events == null)
                    events = new HashMap();
                
                String[] functions = eventValue.split(" ");
                for (String aFunction : functions) {
                    
                    String[] values = aFunction.split("\\.");

                    String functionName = values[0];
                    // TODO: create a list with more than one parameter in a better way
                    List<String> params = new ArrayList();

                    for (int i = 1; i < values.length; i += 1)
                        params.add(values[i]);

                    if (events.get(eventAttr) == null)
                        events.put(eventAttr, new HashMap());

                    HashMap<String, List<String>> function = events.get(eventAttr);

                    function.put(functionName, params);                                        
                }
            }
        }
    }
    
    public void setStyleName(XMLStreamReader reader) {
        styleName = reader.getAttributeValue(null, Constants.Attribute.STYLE_NAME);
    }
    
    public void setEnabled(XMLStreamReader reader) {
        String attrValue = reader.getAttributeValue(null, Constants.Attribute.ENABLED);
                
        if (attrValue != null)
            enabled = Boolean.valueOf(attrValue);
    }
}

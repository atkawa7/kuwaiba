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
import com.neotropic.web.components.ComponentChangeListener;
import com.neotropic.web.components.ComponentEventListener;
import com.neotropic.web.components.ElementChangeListener;
import com.neotropic.web.components.EventDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.xml.stream.XMLStreamReader;

/**
 * 
 * API IN TOP FORM
 * General java representation of a Tag
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public abstract class AbstractElement implements Tag, ComponentChangeListener, ComponentEventListener {
    private String id;
    private String name;
////    private List<AbstractElement> children;
    /**
     * event->function->parameters
     */    
    private HashMap<String, HashMap<String, List<String>>> events;
    
    private List<Integer> area;
    
    private List<String> precondition;
    
    protected List<ElementChangeListener> changeListeners;
    
    private ScriptRunner scriptRunner;    
        
    public void addElementChangeListener(ElementChangeListener changeListener) {
        if (changeListener != null) {
            
            if (changeListeners == null)
                changeListeners = new ArrayList();
            
            changeListeners.add(changeListener);
        }
    }
    
    public void removeElementChangeListener(ElementChangeListener changeListener) {
        if (changeListener != null) {
            
            if (changeListeners != null)
                changeListeners.remove(changeListener);
        }
    }
    
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
    
////    public List<AbstractElement> getChildren() {
////        return children == null ? children = new ArrayList() : children;
////    }
////    
////    public void setChildren(List<AbstractElement> children) {
////        this.children = children;
////    }
    
    public HashMap<String, HashMap<String, List<String>>> getEvents() {
        return events;
    }
        
    public void setEvents(HashMap<String, HashMap<String, List<String>>> events) {
        this.events = events;
    }
    
    public void setArea(List<Integer> area) {
        this.area = area;        
    }
    
    public List<Integer> getArea() {
        return area;
    }
    
    public ScriptRunner getScriptRunner() {
        return scriptRunner;
    }
    
    public void setScriptRunner(ScriptRunner scriptRunner) {
        this.scriptRunner = scriptRunner;
    }
                        
//    public abstract void initFromXMl(XMLStreamReader reader) throws XMLStreamException;
    
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
////                events.put(eventAttr, function);
            }
        }
    }    
    
    public final void fireElementChange(ChangeDescriptor changeDescriptor) {
        Iterator<ElementChangeListener> iterator = changeListeners.iterator();
        
        while (iterator.hasNext())
            iterator.next().elementChange(changeDescriptor);
    }
    
    @Override
    public void onEvent(EventDescriptor event) {
        getScriptRunner().run(this, event.getName());
    }
}

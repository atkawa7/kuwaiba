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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * 
 * API IN TOP FORM
 * General java representation of a Tag
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public abstract class AbstractElement implements Tag, ComponentEventListener, PropertyChangeListener {
    /**
     * Tags of Attributes
     */
    private String id;
    private List<Integer> area;
    private String styleName;
    /**
     * Properties
     */
    private boolean enabled = true;
    
    private String width;
    
    private String height;
    
    private boolean hidden = false;
    /**
     * event->function->parameters
     */    
    private HashMap<String, LinkedHashMap<String, List<String>>> events;
    
    private ElementEventListener elementEventListener;
    
    private List<String> propertyChangeListeners;
    
    private FormStructure formStructure;
    
    public FormStructure getFormStructure() {
        return formStructure;
    }
    
    public void setFormStructure(FormStructure formStructure) {
        this.formStructure = formStructure;
    }
        
    public String getId() {
        return id;
    }
        
    public void setId(String id) {
        this.id = id;
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
    
    public String getHeight() {
        return height;
    }
    
    public void setHeight(String height) {
        this.height = height;
    }
    
    public String getWidth() {
        return width;
    }
    
    public void setWidth(String width) {
        this.width = width;
    }
    
    public boolean isHidden() {
        return hidden;
    }
        
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    
    public void addPropertyChangeListener(String propertyChangeListener) {
        if (propertyChangeListeners == null)
            propertyChangeListeners = new ArrayList();
        
        if (propertyChangeListener != null)
            propertyChangeListeners.add(propertyChangeListener);
    }
    
    public void removePropertyChangeListener(String propertyChangeListener) {
        if (propertyChangeListeners != null)
            propertyChangeListeners.remove(propertyChangeListener);
    }
    
    public void firePropertyChangeEvent() {
        if (propertyChangeListeners != null) {
            
            Iterator<String> iterator = propertyChangeListeners.iterator();
            
            while (iterator.hasNext()) {

                PropertyChangeListener pcl = getFormStructure().getElementById(iterator.next());

                if (pcl != null)
                    pcl.propertyChange();
            }
        }
    }
    
    @Override
    public void propertyChange() {
        if (hasProperty(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.ENABLED)) {
            
            boolean oldValue = isEnabled();
            boolean newValue = (boolean) getNewValue(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.ENABLED);

            setEnabled(newValue);

            firePropertyChangeEvent();

            fireElementEvent(new EventDescriptor(
                Constants.EventAttribute.ONPROPERTYCHANGE, 
                Constants.Property.ENABLED, newValue, oldValue));
            
        } else if (hasProperty(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.HIDDEN)) {
            
            boolean oldValue = isHidden();
            boolean newValue = (boolean) getNewValue(Constants.EventAttribute.ONPROPERTYCHANGE, Constants.Property.HIDDEN);
            
            setHidden(newValue);

            firePropertyChangeEvent();
           
            fireElementEvent(new EventDescriptor(
                Constants.EventAttribute.ONPROPERTYCHANGE, 
                Constants.Property.HIDDEN, newValue, oldValue));
        }
    }
        
    public Object getNewValue(String eventAttribute, String propertyName) {
        if (hasProperty(eventAttribute, propertyName)) {
            List<String> list = getEvents().get(eventAttribute).get(propertyName);
            if (list != null && !list.isEmpty()) {
                
                String functionName = list.get(0);
                
                Runner function = getFormStructure().getElementScript().getFunctionByName(functionName);
                
                // An item of parameters list can be an AbstractElement or a String
                List parameters = new ArrayList();
                
                for (int i = 1; i < list.size(); i += 1) {
                    AbstractElement anElement = getFormStructure().getElementById(list.get(i));
                    parameters.add(anElement != null ? anElement : list.get(i));
                }
                return function.run(parameters);
            }
        }
        return null;        
    }    
        
    public HashMap<String, LinkedHashMap<String, List<String>>> getEvents() {
        return events;
    }
        
    public void setEvents(HashMap<String, LinkedHashMap<String, List<String>>> events) {
        this.events = events;
    }
    
    @Override
    public void onComponentEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            if (Constants.Property.ENABLED.equals(event.getPropertyName())) {
                try {
                    setEnabled(Boolean.valueOf(String.valueOf(event.getNewValue())));
                    firePropertyChangeEvent();
                } catch (Exception ex) {
                    Logger.getLogger(AbstractElement.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void setElementEventListener(ElementEventListener elementEventListener) {
        this.elementEventListener = elementEventListener;
    }
    
    public ElementEventListener getElementEventListener() {
        return elementEventListener;
    }
    
    public void fireElementEvent(EventDescriptor eventDescriptor) {
        if (elementEventListener != null)
            elementEventListener.onElementEvent(eventDescriptor);
    }    
    
    @Override
    public void initFromXML(XMLStreamReader reader) throws XMLStreamException {
        setId(reader);
        setArea(reader);
        setEvents(reader);
        setStyleName(reader);
        setPropertyChangeListener(reader);
        setEnabled(reader);
        setHeight(reader);
        setWidth(reader);
        setHidden(reader);
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
    
    public void setPropertyChangeListener(XMLStreamReader reader) {
        String listeners = reader.getAttributeValue(null, Constants.Attribute.PROPERTY_CHANGE_LISTENER);
        
        if (listeners != null) {
            
            String[] listenersArray = listeners.split(" ");
            
            if (listenersArray != null) {
                propertyChangeListeners = new ArrayList();
                
                for (String listener : listenersArray)
                    propertyChangeListeners.add(listener);
            }
        }
    }
    
    public void setEvents(XMLStreamReader reader) {
        String [] eventAttrs = {
            Constants.EventAttribute.ONCLICK, 
            Constants.EventAttribute.ONNOTIFY, 
            Constants.EventAttribute.ONPROPERTYCHANGE, 
            Constants.EventAttribute.ONLOAD,
            Constants.Function.VALIDATE};
        
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
                        events.put(eventAttr, new LinkedHashMap());

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
    
    public void setHeight(XMLStreamReader reader) {
        height = reader.getAttributeValue(null, Constants.Property.HEIGHT);
    }
    
    public void setWidth(XMLStreamReader reader) {
        width = reader.getAttributeValue(null, Constants.Property.WIDTH);
    }
    
    public void setHidden(XMLStreamReader reader) {
        hidden = Boolean.valueOf(reader.getAttributeValue(null, Constants.Property.HIDDEN));
    }
    
    public void fireOnload() {
    }
    
    public boolean hasEventAttribute(String eventAttribute) {
        return getEvents() != null && getEvents().containsKey(eventAttribute);
    }
    
    public boolean hasProperty(String eventAttribute, String propertyName) {
        return hasEventAttribute(eventAttribute) && 
               getEvents().get(eventAttribute) != null && 
               getEvents().get(eventAttribute).containsKey(propertyName);
    }
    
}

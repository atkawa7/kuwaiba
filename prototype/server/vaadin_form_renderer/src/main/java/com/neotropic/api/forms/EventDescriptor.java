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

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class EventDescriptor {
    private String eventName;
    private String propertyName;
    private Object oldValue;
    private Object newValue;
    
    public EventDescriptor() {
    }
    
    public EventDescriptor(String eventName) {
        this.eventName = eventName;
    }
    
    public EventDescriptor(String eventName, String propertyName) {
        this(eventName);
        this.propertyName = propertyName;
    }
    
    public EventDescriptor(String eventName, String propertyName, Object newValue, Object oldValue) {
        this(eventName, propertyName);
        this.newValue = newValue;
        this.oldValue = oldValue;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public void setEventName(String name) {
        this.eventName = name;
    }
    
    public Object getNewValue() {
        return newValue;
    }
    
    public void setNewValue(Object newValue) {
        this.newValue = newValue;        
    }
    
    public Object getOldValue() {
        return oldValue;        
    }
    
    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }
}

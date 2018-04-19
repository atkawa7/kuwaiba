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
 * TODO: deletes this class
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PropertyChangeEvent {
    private AbstractElement source;
    private String propertyName;
    private Object newValue;
    private Object oldValue;
    
    public PropertyChangeEvent(AbstractElement source, String propertyName, Object newValue, Object oldValue) {
        this.source = source;
        this.propertyName = propertyName;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }
    
    public AbstractElement getSource() {
        return source;
    }
    
    public void setSource(AbstractElement source) {
        this.source = source;        
    }
    
    public String getPropertyName() {
        return propertyName;        
    }
    
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;        
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

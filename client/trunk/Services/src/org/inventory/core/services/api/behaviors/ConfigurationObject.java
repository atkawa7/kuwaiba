/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
 * 
 */
package org.inventory.core.services.api.behaviors;

import java.util.HashMap;

/**
 * A configuration object is a singleton that is used to store and share configuration information (variables, etc) 
 * among components of the same module (for example, an option selected in a TopComponent that is also needed by an action in a graph scene within)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class ConfigurationObject {
    protected HashMap<String, Object> properties;

    public ConfigurationObject() {
        properties = new HashMap<>();
    }    
        
    public void setProperty(String propertyName, Object value) {
        properties.put(propertyName, value);
    }
    
    public void removeProperty(String propertyName) {
        properties.remove(propertyName);
    }
    
    public Object getProperty(String propertyName) {
        return properties.get(propertyName);
    }
}

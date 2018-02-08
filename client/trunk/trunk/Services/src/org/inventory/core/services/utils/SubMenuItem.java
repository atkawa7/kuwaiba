/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.core.services.utils;

import java.util.HashMap;

/**
 * This class represent a item in a subMenu
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SubMenuItem {
    private final String caption;
    private String toolTipText;
    private HashMap<String, Object> properties;
        
    public SubMenuItem(String caption) {
        this.caption = caption;
    }
    
    public String getCaption() {
        return caption;
    }
    
    public String getToolTipText() {
        return toolTipText;
    }
    
    public void setToolTipText(String toolTipText) {
        this.toolTipText = toolTipText;
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    public void addProperty(String key, Object value) {
        if (properties == null)
            properties = new HashMap();
        
        properties.put(key, value);
    }
    
    public void removeProperty(String key) {
        if (properties == null)
            return;
        
        properties.remove(key);
    }
    
    @Override
    public String toString() {
        return caption != null ? caption : "";
    }
}

/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.apis.web.gui.properties;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;

/**
 * A property in a property sheet
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <T> The type of the property
 */
public abstract class AbstractProperty<T> {
    private String name;
    private String displayName;
    private String description;
    private T value;

    public AbstractProperty(String name, String displayName, String description, T value) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.value = value;
    }

    public AbstractProperty(String name, T value) {
        this.name = name;
        this.value = value;
        this.displayName = name;
        this.description = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
    
    /**
     * An advanced editor for the property
     * @return 
     */
    public abstract Component getAdvancedEditor();
    /**
     * Does it support an advanced editor? If true  
     * @return 
     */
    public abstract boolean supportsAdvancedEditor();
    /**
     * A simple field that will be used to edit the property
     * @return 
     */
    public abstract AbstractField getInplaceEditor();
    /**
     * The value to be displayed on screen 
     * @return The string representation of the value of the property
     */
    public abstract String getAsString();
}

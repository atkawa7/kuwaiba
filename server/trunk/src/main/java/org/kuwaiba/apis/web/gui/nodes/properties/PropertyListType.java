/*
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
package org.kuwaiba.apis.web.gui.nodes.properties;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.ComboBox;
import java.util.List;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * A field to represent a set of values(a list) in a property sheet
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 * @param <T>
 */
public class PropertyListType <T> extends ComboBox {

    private final String propertyName;
    private T oldValue;
    
    public PropertyListType(String propertyName, List<T> list, T actualValue) {
        this.propertyName = propertyName;
        this.setTextInputAllowed(false);
        this.setNullSelectionAllowed(false);
        for (T item : list) 
            addItem(item);
        
        this.select(actualValue);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public T getOldValue() {
        return oldValue;
    }

    public void setOldValue(T oldValue) {
        this.oldValue = oldValue;
    }
}

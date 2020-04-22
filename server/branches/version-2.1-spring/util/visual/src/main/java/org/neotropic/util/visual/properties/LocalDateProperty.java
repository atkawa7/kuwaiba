/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.util.visual.properties;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.datepicker.DatePicker;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Support for local-date-type properties
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class LocalDateProperty extends AbstractProperty<LocalDate>{

    public LocalDateProperty(String name, String displayName, String description, LocalDate value) {
        super(name, displayName, description, value);
    }

    public LocalDateProperty(String name, String displayName, String description, LocalDate value, String type) {
        super(name, displayName, description, value, type);
    }   
    
    public LocalDateProperty(String name, String displayName, String description, long value) {
        super(name, displayName, description, Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate());
        
    }

    @Override
    public AbstractField getAdvancedEditor() {
        throw new UnsupportedOperationException("This property type does not support an advanced editor."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean supportsAdvancedEditor() {
        return false;
    }

    @Override
    public AbstractField getInplaceEditor() {       
        DatePicker datePicker = new DatePicker();
        datePicker.setPlaceholder("...");
        return datePicker;
    }

    @Override
    public String getAsString() {
        return getValue() == null ? "Not Set" : getValue().toString();
    }

    @Override
    public String getAsStringToPersist() {
        if (getValue() != null) {
        Instant instant = getValue().atStartOfDay(ZoneId.systemDefault()).toInstant();	
	long timeInMillis = instant.toEpochMilli();
        return timeInMillis + "";
        } else 
            return "0";
    }
    
    @Override
    public boolean supportsInplaceEditor() {
        return true;
    }

}

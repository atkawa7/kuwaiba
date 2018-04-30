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
package com.neotropic.web.components;

import com.neotropic.api.forms.EventDescriptor;
import com.neotropic.api.forms.AbstractElement;
import com.neotropic.api.forms.Constants;
import com.neotropic.api.forms.ElementDateField;
import com.vaadin.data.HasValue;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.ui.DateField;
import java.time.LocalDate;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentDateField extends GraphicalComponent {
        
    public ComponentDateField() {
        super(new DateField());
    }
    
    @Override
    public DateField getComponent() {
        return (DateField) super.getComponent();
    }

    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementDateField) {
            getComponent().setValue(LocalDate.now());
            
            getComponent().addValueChangeListener(new ValueChangeListener<LocalDate>() {
                @Override
                public void valueChange(HasValue.ValueChangeEvent<LocalDate> event) {
                    
                    if (event.isUserOriginated()) {
                        fireComponentEvent(new EventDescriptor(
                            Constants.EventAttribute.ONPROPERTYCHANGE, 
                            Constants.Property.VALUE, 
                            event.getValue(), event.getOldValue()));
                    }
                }
            });
        }
    }
        
    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            
            if (Constants.Property.VALUE.equals(event.getPropertyName())) {
            }
        }
    }
    
}

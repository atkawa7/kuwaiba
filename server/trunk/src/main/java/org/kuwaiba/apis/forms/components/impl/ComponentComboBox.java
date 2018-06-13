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
package org.kuwaiba.apis.forms.components.impl;

import org.kuwaiba.apis.forms.elements.EventDescriptor;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.ElementComboBox;
import com.vaadin.data.HasValue;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.ComboBox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentComboBox extends GraphicalComponent {
    
    public ComponentComboBox() {
        super(new ComboBox());
    }
    
    @Override
    public ComboBox getComponent() {
        return (ComboBox) super.getComponent();
    }
    
    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementComboBox) {
            ElementComboBox comboBox = (ElementComboBox) element;
                        
            if (comboBox.getItems() != null)
                getComponent().setItems(comboBox.getItems());
            
            if (comboBox.getValue() != null)
                getComponent().setValue(comboBox.getValue());
            
            getComponent().setRequiredIndicatorVisible(comboBox.isMandatory());
            
            getComponent().addValueChangeListener(new ValueChangeListener() {
                @Override
                public void valueChange(HasValue.ValueChangeEvent event) {
                    
                    if (event.isUserOriginated()) {
                        fireComponentEvent(new EventDescriptor(
                            Constants.EventAttribute.ONPROPERTYCHANGE, 
                            Constants.Property.VALUE, 
                            event.getValue(), event.getOldValue()));
                    }
                } 
            });
            
            getComponent().addFocusListener(new FieldEvents.FocusListener() {
                @Override
                public void focus(FieldEvents.FocusEvent event) {
                    getComponent().setItems(Collections.EMPTY_LIST);
                    comboBox.fireOnLazyLoad();
                }
            });
        }
    }
    
    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            
            if (Constants.Property.ITEMS.equals(event.getPropertyName())) {
                List lst = new ArrayList();
                if (event.getNewValue() != null) {
                    for (Object obj : (List) event.getNewValue())
                        lst.add(obj);
                }
                getComponent().setItems(lst);
            } else if (Constants.Property.VALUE.equals(event.getPropertyName())) {
                getComponent().setValue(event.getNewValue());
            }
        }
    }
    
}

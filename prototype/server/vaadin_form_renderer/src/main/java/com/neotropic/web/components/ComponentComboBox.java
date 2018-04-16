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
import com.neotropic.api.forms.ElementComboBox;
import com.vaadin.data.HasValue;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.ui.ComboBox;

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
            
            getComponent().addValueChangeListener(new ValueChangeListener() {
                @Override
                public void valueChange(HasValue.ValueChangeEvent event) {
                    fireComponentEvent(new EventDescriptor(Constants.EventAttribute.ONVALUECHANGE, 
                        event.getValue(), event.getOldValue()));
                }
            });
        }
        /*
        childComponent = new ComboBox();
        List items = ((ElementComboBox) childElement).getItems();
        if (items != null)
            ((ComboBox) childComponent).setItems(items);// .addItems(items);

        ((ComboBox) childComponent).addValueChangeListener(new HasValue.ValueChangeListener() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent event) {
                if (childElement.getEvents() != null) {

                    if (childElement.getEvents().containsKey(Constants.EventAttribute.ONVALUECHANGE))
                        comboboxOnvaluechange(childElement.getEvents().get(Constants.EventAttribute.ONVALUECHANGE));                               
                }                        
            }
        });
        */
    }
    
    @Override
    public void onElementEvent(EventDescriptor event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

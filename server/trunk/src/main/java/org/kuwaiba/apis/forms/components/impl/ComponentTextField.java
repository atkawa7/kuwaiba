/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
import org.kuwaiba.apis.forms.elements.ElementTextField;
import com.vaadin.data.HasValue;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.ui.TextField;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentTextField extends GraphicalComponent {
    
    public ComponentTextField() {
        super(new TextField());
    }
    
    @Override
    public TextField getComponent() {
        return (TextField) super.getComponent();
    }
        
    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementTextField) {
            ElementTextField textField = (ElementTextField) element;
            
            getComponent().setValue(textField.getValue() != null ? textField.getValue().toString() : "");
            getComponent().setEnabled(textField.isEnabled());
            
            getComponent().addValueChangeListener(new ValueChangeListener() {
                
                @Override
                public void valueChange(HasValue.ValueChangeEvent event) {
                    
                    if (event.isUserOriginated()) {
                        fireComponentEvent(new EventDescriptor(
                            Constants.EventAttribute.ONPROPERTYCHANGE, 
                            Constants.Property.VALUE, event.getValue(), event.getOldValue()));
                    }
                }
            });
        }
        
    }
    
    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            
            if (Constants.Property.VALUE.equals(event.getPropertyName()))
                getComponent().setValue(event.getNewValue() != null ? event.getNewValue().toString() : "");
            
            if (Constants.Property.ENABLED.equals(event.getPropertyName()))
                getComponent().setEnabled((boolean) event.getNewValue());
            
            if (Constants.Property.HIDDEN.equals(event.getPropertyName()))
                getComponent().setVisible(!((ElementTextField) getComponentEventListener()).isHidden());
        }
        
    }
    
}

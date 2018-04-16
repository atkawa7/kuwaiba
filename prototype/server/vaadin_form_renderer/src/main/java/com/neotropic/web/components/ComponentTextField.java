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
import com.neotropic.api.forms.ElementTextField;
import com.vaadin.data.HasValue;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.ui.TextField;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentTextField extends GraphicalComponent {
    
    public ComponentTextField() {
        super(new TextField());
    }
    
    @Override
    public TextField getComponent() {
        return (TextField) super.getComponent();
    }
        
//    public ComponentTextField(ElementTextField elementTextField) {
//        elementTextField.addElementChangeListener(this);
//        
//        addValueChangeListener(new ValueChangeListener() {
//            @Override
//            public void valueChange(ValueChangeEvent event) {
//                
//                if (event.isUserOriginated()) {
//                    elementChange(new ChangeDescriptor(
//                        ComponentTextField.this, Constants.Attribute.VALUE, event.getOldValue(), event.getValue()));
//                }
//            }
//        });
//    }
    
    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementTextField) {
            ElementTextField textField = (ElementTextField) element;
            
            getComponent().setValue(textField.getValue() != null ? textField.getValue() : "");
            getComponent().setEnabled(textField.isEnabled());
            
            getComponent().addValueChangeListener(new ValueChangeListener() {
                
                @Override
                public void valueChange(HasValue.ValueChangeEvent event) {
                    fireComponentEvent(new EventDescriptor(Constants.EventAttribute.ONVALUECHANGE, event.getValue(), event.getOldValue()));
                }
            });
        }
        /*
        childComponent = new TextField();
        String value = evaluator.getValue(((ElementTextField) childElement).getValue());
        ((TextField) childComponent).setValue(value != null ? value : "");
        ((TextField) childComponent).setEnabled(((ElementTextField) childElement).isEnabled());

        ((TextField) childComponent).addValueChangeListener(new HasValue.ValueChangeListener() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent event) {
                if (childElement.getEvents() != null)
                    if (childElement.getEvents().containsKey(Constants.EventAttribute.ONVALUECHANGE)) {
                        textFieldOnvaluechange(childElement.getEvents().get(Constants.EventAttribute.ONVALUECHANGE));
                    }                            
            }
        });
        */
    }
    
    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.Function.SET_VALUE.equals(event.getName())) {
            getComponent().setValue("Hola Text Field");
        }
    }
    
}

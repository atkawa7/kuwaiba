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

import com.vaadin.data.HasValue;
import com.vaadin.ui.CheckBox;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.ElementCheckBox;
import org.kuwaiba.apis.forms.elements.EventDescriptor;

/**
 * Vaadin component wrapper used to render a ElementCheckBox
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentCheckBox extends GraphicalComponent {

    public ComponentCheckBox() {
        super(new CheckBox());
    }
    
    @Override
    public CheckBox getComponent() {
        return (CheckBox) super.getComponent();
    }

    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementCheckBox) {
            ElementCheckBox checkBox = (ElementCheckBox) element;
            getComponent().setValue(checkBox.getValue());
            
            getComponent().addValueChangeListener(new HasValue.ValueChangeListener<Boolean>() {
                @Override
                public void valueChange(HasValue.ValueChangeEvent<Boolean> event) {
                                        
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
            
            if (Constants.Property.VALUE.equals(event.getPropertyName()))
                getComponent().setValue(event.getNewValue() != null ? (Boolean) event.getNewValue() : Boolean.FALSE);
            
            if (Constants.Property.ENABLED.equals(event.getPropertyName()))
                getComponent().setEnabled((boolean) event.getNewValue());
        }
    }
    
}

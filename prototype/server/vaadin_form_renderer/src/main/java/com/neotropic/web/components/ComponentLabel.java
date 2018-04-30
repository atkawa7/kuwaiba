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
import com.neotropic.api.forms.ElementLabel;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentLabel extends GraphicalComponent {
    
    public ComponentLabel() {
        super(new Label());        
    }
    
    @Override
    public Label getComponent() {
        return (Label) super.getComponent();
    }
    

    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementLabel) {
            ElementLabel label = (ElementLabel) element;
            
            if (label.getStyleName() != null)
                getComponent().setStyleName(label.getStyleName());
            getComponent().setContentMode(ContentMode.HTML);
            getComponent().setValue(label.getValue());
        }
        
    }
    
    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            
            if (Constants.Property.VALUE.equals(event.getPropertyName()))
                getComponent().setValue(((ElementLabel) getComponentEventListener()).getValue());
        }
    }
    
}

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
import com.neotropic.api.forms.ElementButton;
import com.neotropic.forms.FormDisplayer;
import com.neotropic.forms.Variable;
import com.vaadin.ui.Button;
import java.io.File;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentButton extends GraphicalComponent {
            
    public ComponentButton() {
        super(new Button());
    }
    
    @Override
    public Button getComponent() {
        return (Button) super.getComponent();
    }
        
    @Override
    public void initFromElement(AbstractElement element) {
        
        if (element instanceof ElementButton) {
            
            ElementButton button = (ElementButton) element;
            
            getComponent().setCaption(button.getCaption());
            
            getComponent().addClickListener(new Button.ClickListener() {
                
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    fireComponentEvent(new EventDescriptor(Constants.EventAttribute.ONCLICK));
                }
            });
        }
    }
    
    @Override
    public void onElementEvent(EventDescriptor event) {
        
        if (Constants.EventAttribute.ONCLICK.equals(event.getEventName())) {
            
            if (Constants.Function.OPEN_FORM.equals(event.getPropertyName())) {
                File file = new File(Variable.FORM_RESOURCE_STRUCTURES + "/" + event.getNewValue() + ".xml");
                FormDisplayer.getInstance().display(file, false);
            }
        }
    }
    
}

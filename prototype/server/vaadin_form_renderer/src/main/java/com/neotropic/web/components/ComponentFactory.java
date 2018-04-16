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

import com.neotropic.api.forms.AbstractElement;
import com.neotropic.api.forms.ElementButton;
import com.neotropic.api.forms.ElementComboBox;
import com.neotropic.api.forms.ElementDateField;
import com.neotropic.api.forms.ElementGrid;
import com.neotropic.api.forms.ElementGridLayout;
import com.neotropic.api.forms.ElementHorizontalLayout;
import com.neotropic.api.forms.ElementImage;
import com.neotropic.api.forms.ElementLabel;
import com.neotropic.api.forms.ElementSubform;
import com.neotropic.api.forms.ElementTextArea;
import com.neotropic.api.forms.ElementTextField;
import com.neotropic.api.forms.ElementVerticalLayout;
import com.vaadin.ui.Component;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentFactory {
    private static ComponentFactory instance;
    
    private ComponentFactory() {
    }
    
    public static ComponentFactory getInstance() {
        return instance == null ? instance = new ComponentFactory() : instance;
    }
    
    
    public Component getComponent(AbstractElement element) {
        GraphicalComponent component = null;
        
        if (element instanceof ElementGridLayout) {
            component = new ComponentGridLayout();
        } else if (element instanceof ElementVerticalLayout) {
            component = new ComponentVerticalLayout();
        } else if (element instanceof ElementLabel) {
            component = new ComponentLabel();
        } else if (element instanceof ElementTextField) {
            component = new ComponentTextField();            
        } else if (element instanceof ElementTextArea) {
            component = new ComponentTextArea();            
        } else if (element instanceof ElementDateField) {
            component = new ComponentDateField();            
        } else if (element instanceof ElementComboBox) {
            component = new ComponentComboBox();            
        } else if (element instanceof ElementGrid) {
            component = new ComponentGrid();            
        } else if (element instanceof ElementButton) {
            component = new ComponentButton();            
        } else if (element instanceof ElementHorizontalLayout) {
            component = new ComponentHorizontalLayout();            
        } else if (element instanceof ElementImage) {
            component = new ComponentImage();
        } else if (element instanceof ElementSubform) {
            component = new ComponentSubform();            
        }
        
        if (component != null && element != null) {
            component.initFromElement(element);
            
            element.setElementEventListener(component);
            component.setComponentEventListener(element);
        }
        return component != null && component.getComponent() != null && 
            component.getComponent() instanceof Component ? component.getComponent() : null;
    }
        
}

/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.flow.component.paper.toggle;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;

/**
 * <paper-toogle-button>
 * label
 * </paper-toogle-button>
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("paper-toggle-button")
@JsModule("@polymer/paper-toggle-button/paper-toggle-button.js")
@NpmPackage(value = "@polymer/paper-toggle-button", version = "^3.0.1")
public class PaperToggleButton extends Component implements HasComponents, HasStyle, HasEnabled,  HasSize {
    public PaperToggleButton() {}
    
    public PaperToggleButton(String catpion) {
        add(catpion);
    }
    
    public void setChecked(boolean checked) {
        getElement().setProperty("checked", checked);
    }
    @Synchronize(property = "checked", value = "change")
    public boolean getChecked() {
        return getElement().getProperty("checked", false);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        getElement().setProperty("disabled", !enabled);
    }

    @Override
    public boolean isEnabled() {
        return !getElement().getProperty("disabled", false);
    }
    
    public void setInvalid(boolean invalid){
        getElement().setProperty("invalid", invalid);
    }
    
    public boolean getInvalid(){
        return getElement().getProperty("invalid", false);
    }
    
    public void setNoInk(boolean noInk){
        getElement().setProperty("noink", noInk);
    }
    
    public boolean getNoInk(){
        return getElement().getProperty("noink", false);
    }
    
    public Registration addValueChangeListener(ComponentEventListener<ToggleButtonEvent> listener) {
        return addListener(ToggleButtonEvent.class, listener);
    }
    
    @DomEvent("change")
    public static class ToggleButtonEvent extends ComponentEvent<PaperToggleButton> {

        public ToggleButtonEvent(PaperToggleButton source, boolean fromClient) {
            super(source, fromClient);
        }
        
        public boolean getValue(){
            return getSource().getChecked();
        }
        
        public boolean getOldValue(){
            return !getSource().getChecked();
        }
    }
}

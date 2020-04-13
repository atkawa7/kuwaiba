/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package com.neotropic.vaadin.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("paper-icon-button")
@JsModule("@polymer/paper-icon-button/paper-icon-button.js")
@NpmPackage(value = "@polymer/paper-icon-button", version = "3.0.0")
public class PaperIconButton extends Component {
    public PaperIconButton() {
    }
    public void setSlot(String slot) {
        getElement().setProperty(
            Constants.PaperIconButton.Property.SLOT.getPropertyName(), slot);
    }
    /**
     * @param suffix default value false
     */
    public void setSuffix(boolean suffix) {
        getElement().setProperty(
            Constants.PaperIconButton.Property.SUFFIX.getPropertyName(), suffix);
    }
    public void setIcon(String icon) {
        getElement().setProperty(
            Constants.PaperIconButton.Property.ICON.getPropertyName(), icon);
    }
    public void setPrefix(boolean prefix) {
        getElement().setProperty(
            Constants.PaperIconButton.Property.PREFIX.getPropertyName(), prefix);
    }
}

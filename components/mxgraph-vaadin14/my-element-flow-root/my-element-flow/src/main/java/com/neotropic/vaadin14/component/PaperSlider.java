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
package com.neotropic.vaadin14.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.shared.Registration;

@Tag("paper-slider")
@JsModule("@polymer/paper-slider/paper-slider.js")
@NpmPackage(value = "@polymer/paper-slider", version = "3.0.1")
/*
 If you wish to include your own JS modules in the add-on jar, add the module
 files to './src/main/resources/META-INF/resources/frontend' and insert an
 annotation @JsModule("./my-module.js") here.
*/
/**
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PaperSlider extends Component {
    private static final String PROPERTY_VALUE = "value";

    public PaperSlider() {
    }
    
    @Synchronize(property = "value", value = "value-change")
    public double getValue() {
        return Double.valueOf(getElement().getProperty(PROPERTY_VALUE));
    }
        
    public void setValue(double value) {
        getElement().setProperty(PROPERTY_VALUE, value);
    }
    
    public Registration addValueChangeListener(ComponentEventListener<PaperSliderValueChangeEvent> valueChangeListener) {
        return super.addListener(PaperSliderValueChangeEvent.class, valueChangeListener);
    }
}

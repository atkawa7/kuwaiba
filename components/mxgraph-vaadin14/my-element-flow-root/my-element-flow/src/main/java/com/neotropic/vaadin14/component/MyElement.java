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
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

/**
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("my-element")
@JsModule("./my-element.js")
public class MyElement extends Component {
    private static final String PROPERTY_PROP1 = "prop1";
    
    public MyElement() {
    }
    
    public String getProp1() {
        return getElement().getProperty(PROPERTY_PROP1);
    }
        
    public void setProp1(String prop1) {
        getElement().setProperty(PROPERTY_PROP1, prop1);
    }
}

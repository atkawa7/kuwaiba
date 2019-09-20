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
package com.neotropic.vaadin14.component.googlemap;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

/**
 * Google Map Component is the Vaadin Flow integration of the web component <google-map>
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("google-map")
@JsModule("./google-map.js")
public class GoogleMap extends Component {
    /** Element Properties */
    private static final String PROPERTY_API_KEY = "apiKey";
    private static final String PROPERTY_WIDTH = "width";
    private static final String PROPERTY_HEIGHT = "height";
            
    public GoogleMap(String apiKey) {
        getElement().getStyle().set(PROPERTY_WIDTH, "100%");
        getElement().getStyle().set(PROPERTY_HEIGHT, "100%");        
        setApiKey(apiKey);
    }
    /**
     * Sets the google-map element property
     * @param apiKey your application API key. See https://developers.google.com/maps/documentation/javascript/get-api-key
     */
    public final void setApiKey(String apiKey) {
        getElement().setProperty(PROPERTY_API_KEY, apiKey);
    }        
}

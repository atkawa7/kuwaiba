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
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("google-map-marker")
@JsModule("./google-map-marker.js")
public class GoogleMapMarker extends Component {
    public GoogleMapMarker(double lat, double lng) {
        getElement().setProperty(Constants.Property.LAT, lat);
        getElement().setProperty(Constants.Property.LNG, lng);
    }
    public Registration addMarkerClickListener(ComponentEventListener<GoogleMapEvent.MarkerClickEvent> listener) {
        return addListener(GoogleMapEvent.MarkerClickEvent.class, listener);        
    }
    public Registration addMarkerDblClickListener(ComponentEventListener<GoogleMapEvent.MarkerDblClickEvent> listener) {
        return addListener(GoogleMapEvent.MarkerDblClickEvent.class, listener);        
    }
    public Registration addMarkerRightClickListener(ComponentEventListener<GoogleMapEvent.MarkerRightClickEvent> listener) {
        return addListener(GoogleMapEvent.MarkerRightClickEvent.class, listener);        
    }
}


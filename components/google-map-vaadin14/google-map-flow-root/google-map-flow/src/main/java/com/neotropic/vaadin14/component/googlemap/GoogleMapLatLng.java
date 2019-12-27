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
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("google-map-lat-lng")
@JsModule("./google-map-lat-lng.js")
public class GoogleMapLatLng extends Component {
    public GoogleMapLatLng(double lat, double lng) {
        getElement().setProperty(Constants.Property.LAT, lat);
        getElement().setProperty(Constants.Property.LNG, lng);
    }
    
    public double getLat() {
        return getElement().getProperty(Constants.Property.LAT, Constants.Default.LAT);
    }
    
    public void setLat(double lat) {
        getElement().setProperty(Constants.Property.LAT, lat);
    }
    
    public double getLng() {
        return getElement().getProperty(Constants.Property.LNG, Constants.Default.LNG);
    }
    
    public void setLng(double lng) {
        getElement().setProperty(Constants.Property.LNG, lng);
    }
}

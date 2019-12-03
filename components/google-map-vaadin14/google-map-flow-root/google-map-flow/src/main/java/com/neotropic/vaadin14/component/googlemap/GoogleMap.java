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
@Tag("google-map")
@JsModule("./google-map.js")
public class GoogleMap extends Component {
    /**
     * @param apiKey Your API key and set the client id to null. See https://developers.google.com/maps/documentation/javascript/get-api-key
     * @param clientId Your client id and set the apiKey to null. See https://developers.google.com/maps/documentation/javascript/get-api-key
     */    
    public GoogleMap(String apiKey, String clientId) {
        getElement().getStyle().set(Constants.Property.WIDTH, "100%");
        getElement().getStyle().set(Constants.Property.HEIGHT, "100%");        
        getElement().setProperty(Constants.Property.API_KEY, apiKey);
        getElement().setProperty(Constants.Property.CLIENT_ID, clientId);
    }
    
    public double getCenterLat() {
        return getElement().getProperty(Constants.Property.CENTER_LAT, Constants.Default.LAT);
    }
    
    public void setCenterLat(double lat) {
        getElement().setProperty(Constants.Property.CENTER_LAT, lat);
    }
    
    public double getCenterLng() {
        return getElement().getProperty(Constants.Property.CENTER_LNG, Constants.Default.LNG);
    }
    
    public void setCenterLng(double lng) {
        getElement().setProperty(Constants.Property.CENTER_LNG, lng);
    }
    
    public void newMarker(GoogleMapMarker googleMapMarker) {
        getElement().appendChild(googleMapMarker.getElement());
    }
        
    public Registration addMapClickListener(ComponentEventListener<GoogleMapEvent.MapClickEvent> listener) {
        return addListener(GoogleMapEvent.MapClickEvent.class, listener);
    }
    
    public Registration addMapDblClickListener(ComponentEventListener<GoogleMapEvent.MapDblClickEvent> listener) {
        return addListener(GoogleMapEvent.MapDblClickEvent.class, listener);
    }
    
    public Registration addMapRightClickListener(ComponentEventListener<GoogleMapEvent.MapRightClickEvent> listener) {
        return addListener(GoogleMapEvent.MapRightClickEvent.class, listener);
    }
    
    public Registration addMapCenterChangedListener(ComponentEventListener<GoogleMapEvent.MapCenterChangedEvent> listener) {
        return addListener(GoogleMapEvent.MapCenterChangedEvent.class, listener);
    }
    
    public Registration addMapMouseMoveListener(ComponentEventListener<GoogleMapEvent.MapMouseMoveEvent> listener) {
        return addListener(GoogleMapEvent.MapMouseMoveEvent.class, listener);
    }
    
    public Registration addMapMouseOutListener(ComponentEventListener<GoogleMapEvent.MapMouseOutEvent> listener) {
        return addListener(GoogleMapEvent.MapMouseOutEvent.class, listener);
    }
    
    public Registration addMapMouseOverListener(ComponentEventListener<GoogleMapEvent.MapMouseOverEvent> listener) {
        return addListener(GoogleMapEvent.MapMouseOverEvent.class, listener);
    }
    
    public Registration addMapZoomChangedListener(ComponentEventListener<GoogleMapEvent.MapZoomChangedEvent> listener) {
        return addListener(GoogleMapEvent.MapZoomChangedEvent.class, listener);
    }
}


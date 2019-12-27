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
@Tag("google-map-polyline")
@JsModule("./google-map-polyline.js")
public class GoogleMapPolyline extends Component {
    public GoogleMapPolyline() {
    }
    
    public String getStrokeColor() {
        return getElement().getProperty(Constants.Property.STROKE_COLOR, Constants.Default.STROKE_COLOR);
    }   
    
    public void setStrokeColor(String strokeColor) {
        getElement().setProperty(Constants.Property.STROKE_COLOR, strokeColor);
    }
    
    public void appendCoordinate(GoogleMapLatLng coordinate) {
        getElement().appendChild(coordinate.getElement());
    }
    
    public void removeCoordinate(GoogleMapLatLng coordinate) {
        getElement().removeChild(coordinate.getElement());
    }
    
    public Registration addPolylineClickListener(ComponentEventListener<GoogleMapEvent.PolylineClickEvent> listener) {
        return addListener(GoogleMapEvent.PolylineClickEvent.class, listener);
    }
        
    public Registration addPolylineDblClickListener(ComponentEventListener<GoogleMapEvent.PolylineDblClickEvent> listener) {
        return addListener(GoogleMapEvent.PolylineDblClickEvent.class, listener);
    }
    
    public Registration addPolylineMouseOutListener(ComponentEventListener<GoogleMapEvent.PolylineMouseOutEvent> listener) {
        return addListener(GoogleMapEvent.PolylineMouseOutEvent.class, listener);
    }
    
    public Registration addPolylineMouseOverListener(ComponentEventListener<GoogleMapEvent.PolylineMouseOverEvent> listener) {
        return addListener(GoogleMapEvent.PolylineMouseOverEvent.class, listener);
    }
    
    public Registration addPolylineRightClickListener(ComponentEventListener<GoogleMapEvent.PolylineRightClickEvent> listener) {
        return addListener(GoogleMapEvent.PolylineRightClickEvent.class, listener);
    }
}

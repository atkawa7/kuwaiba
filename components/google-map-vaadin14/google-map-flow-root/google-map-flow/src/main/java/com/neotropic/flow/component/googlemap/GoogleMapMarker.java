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
package com.neotropic.flow.component.googlemap;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonValue;

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
    //<editor-fold desc="Marker Properties" defaultstate="collapsed">
    @Synchronize(property="lat", value="marker-position-changed")
    public double getLat() {
        return getElement().getProperty(Constants.Property.LAT, Constants.Default.LAT);
    }
    
    public void setLat(double lat) {
        getElement().setProperty(Constants.Property.LAT, lat);
    }
    @Synchronize(property="lng", value="marker-position-changed")
    public double getLng() {
        return getElement().getProperty(Constants.Property.LNG, Constants.Default.LNG);
    }
    
    public void setLng(double lng) {
        getElement().setProperty(Constants.Property.LNG, lng);
    }
    
    public String getIcon() {
        return getElement().getProperty(Constants.Property.ICON, null);
    }
    
    public void setIcon(JsonValue icon) {
        getElement().setPropertyJson(Constants.Property.ICON, icon);
    }
    
    public String getTitle() {
        return getElement().getProperty(Constants.Property.TITLE, null);
    }
    
    public void setTitle(String title) {
        getElement().setProperty(Constants.Property.TITLE, title);
    }
    
    public String getLabel() {
        return getElement().getProperty(Constants.Property.LABEL, null);
    }
    
    public void setLabel(JsonValue label) {
        getElement().setPropertyJson(Constants.Property.LABEL, label);
    }
    
    public boolean getDraggable() {
        return getElement().getProperty(Constants.Property._DRAGGABLE, Constants.Default.DRAGGABLE);
    }
    
    public void setDraggable(boolean draggable) {
        getElement().setProperty(Constants.Property._DRAGGABLE, draggable);
    }
    
    public boolean getMarkerVisible() {
        return getElement().getProperty(Constants.Property.VISIBLE, Constants.Default.VISIBLE);
    }
    
    public void setMarkerVisible(boolean visible) {
        getElement().setProperty(Constants.Property.VISIBLE, visible);
    }
    @Synchronize(property = "animation", value = "marker-animation-changed")
    public Animation getAnimation() {
        return Animation.getAnimation(
            getElement().getProperty(Constants.Property.ANIMATION, null)
        );
    }
    /**
     * Sets to start an animation, or null to stop
     * @param animation 
     */
    public void setAnimation(Animation animation) {
        if (animation != null)
            getElement().setProperty(Constants.Property.ANIMATION, animation.animation());
        else
            getElement().setPropertyJson(Constants.Property.ANIMATION, Json.createNull());
    }
    //</editor-fold>
    //<editor-fold desc="Marker Listeners" defaultstate="collapsed">
    public Registration addMarkerClickListener(ComponentEventListener<GoogleMapEvent.MarkerClickEvent> listener) {
        return addListener(GoogleMapEvent.MarkerClickEvent.class, listener);        
    }
    
    public Registration addMarkerDblClickListener(ComponentEventListener<GoogleMapEvent.MarkerDblClickEvent> listener) {
        return addListener(GoogleMapEvent.MarkerDblClickEvent.class, listener);        
    }
    
    public Registration addMarkerDragEndListener(ComponentEventListener<GoogleMapEvent.MarkerDragEnd> listener) {
        return addListener(GoogleMapEvent.MarkerDragEnd.class, listener);
    }
    
    public Registration addMarkerDragStartListener(ComponentEventListener<GoogleMapEvent.MarkerDragStart> listener) {
        return addListener(GoogleMapEvent.MarkerDragStart.class, listener);
    }
    
    public Registration addMarkerMouseOutListener(ComponentEventListener<GoogleMapEvent.MarkerMouseOutEvent> listener) {
        return addListener(GoogleMapEvent.MarkerMouseOutEvent.class, listener);
    }
    
    public Registration addMarkerMouseOverListener(ComponentEventListener<GoogleMapEvent.MarkerMouseOverEvent> listener) {
        return addListener(GoogleMapEvent.MarkerMouseOverEvent.class, listener);
    }
    public Registration addMarkerPositionChangedListener(ComponentEventListener<GoogleMapEvent.MarkerPositionChange> listener) {
        return addListener(GoogleMapEvent.MarkerPositionChange.class, listener);
    }
    public Registration addMarkerRightClickListener(ComponentEventListener<GoogleMapEvent.MarkerRightClickEvent> listener) {
        return addListener(GoogleMapEvent.MarkerRightClickEvent.class, listener);        
    }
    public Registration addMarkerAnimationChangedListener(ComponentEventListener<GoogleMapEvent.MarkerAnimationChangedEvent> listener) {
        return addListener(GoogleMapEvent.MarkerAnimationChangedEvent.class, listener);
    }
    //</editor-fold>
}


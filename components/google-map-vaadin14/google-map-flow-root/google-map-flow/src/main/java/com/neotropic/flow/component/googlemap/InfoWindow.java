/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
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
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonNull;
import elemental.json.JsonObject;
import java.io.Serializable;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("info-window")
@JsModule("./info-window.js")
public class InfoWindow extends Component implements HasComponents {
    public InfoWindow() {
    }
    public boolean getDisableAutoPan() {
        return getElement().getProperty(Constants.Property.DISABLE_AUTO_PAN, false);
    }
    public void setDisableAutoPan(boolean disableAutoPan) {
        getElement().setProperty(Constants.Property.DISABLE_AUTO_PAN, disableAutoPan);
    }
    public double getMaxWidth() {
        return getElement().getProperty(Constants.Property.MAX_WIDTH, 0);
    }
    public void setMaxWidth(double maxWidth) {
        getElement().setProperty(Constants.Property.MAX_WIDTH, maxWidth);
    }
    public Size getPixelOffset() {
        Serializable pixelOffset = getElement().getPropertyRaw(Constants.Property.PIXEL_OFFSET);
        if (pixelOffset instanceof JsonNull)
            return null;
        return new Size((JsonObject) pixelOffset);
    }
    public void setPixelOffset(Size pixelOffset) {
        if (pixelOffset != null)
            getElement().setPropertyJson(Constants.Property.PIXEL_OFFSET, pixelOffset.toJson());
        else
            getElement().setPropertyJson(Constants.Property.PIXEL_OFFSET, Json.createNull());
        
    }
    public LatLng getPosition() {
        Serializable position = getElement().getPropertyRaw(Constants.Property.POSITION);
        if (position instanceof JsonNull)
            return null;
        return new LatLng((JsonObject) position);
    }
    public void setPosition(LatLng position) {
        if (position != null)
            getElement().setPropertyJson(Constants.Property.POSITION, position.toJson());
        else
            getElement().setPropertyJson(Constants.Property.POSITION, Json.createNull());
    }
    public double getZIndex() {
        return getElement().getProperty(Constants.Property.Z_INDEX, 0);
    }
    public void setZIndex(double zIndex) {
        getElement().setProperty(Constants.Property.Z_INDEX, zIndex);
    }
    public void open(GoogleMap map, Component component) {
        getElement().executeJs(
            "this.open($0.getMVCObject(), $1.getMVCObject())", 
            map, component);
    }
    public void open(GoogleMap map) {
        getElement().executeJs(
            "this.open($0.getMVCObject())", 
            map);
    }
    public void close() {
        getElement().executeJs("this.close()");
    }
    public Registration addInfoWindowCloseClickListener(ComponentEventListener<GoogleMapEvent.InfoWindowCloseClickEvent> listener) {
        return addListener(GoogleMapEvent.InfoWindowCloseClickEvent.class, listener);
    }
    public Registration addInfoWindowContentChangedListener(ComponentEventListener<GoogleMapEvent.InfoWindowContentChangedEvent> listener) {
        return addListener(GoogleMapEvent.InfoWindowContentChangedEvent.class, listener);
    }
    public Registration addInfoWindowDomReadyListener(ComponentEventListener<GoogleMapEvent.InfoWindowDomReadyEvent> listener) {
        return addListener(GoogleMapEvent.InfoWindowDomReadyEvent.class, listener);
    }
    public Registration addInfoWindowPositionChangedListener(ComponentEventListener<GoogleMapEvent.InfoWindowPositionChangedEvent> listener) {
        return addListener(GoogleMapEvent.InfoWindowPositionChangedEvent.class, listener);
    }
    public Registration addInfoWindowZIndexChangedListener(ComponentEventListener<GoogleMapEvent.InfoWindowZIndexChangedEvent> listener) {
        return addListener(GoogleMapEvent.InfoWindowZIndexChangedEvent.class, listener);
    }
    public Registration addInfoWindowAddedListener(ComponentEventListener<GoogleMapEvent.InfoWindowAddedEvent> listener) {
        return addListener(GoogleMapEvent.InfoWindowAddedEvent.class, listener);
    }
}
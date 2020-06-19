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

import com.neotropic.flow.component.googlemap.GoogleMapEvent.RectangleBoundsChangedEvent;
import com.neotropic.flow.component.googlemap.GoogleMapEvent.RectangleClickEvent;
import com.neotropic.flow.component.googlemap.GoogleMapEvent.RectangleDblClickEvent;
import com.neotropic.flow.component.googlemap.GoogleMapEvent.RectangleDragEndEvent;
import com.neotropic.flow.component.googlemap.GoogleMapEvent.RectangleDragEvent;
import com.neotropic.flow.component.googlemap.GoogleMapEvent.RectangleDragStartEvent;
import com.neotropic.flow.component.googlemap.GoogleMapEvent.RectangleMouseDownEvent;
import com.neotropic.flow.component.googlemap.GoogleMapEvent.RectangleMouseMoveEvent;
import com.neotropic.flow.component.googlemap.GoogleMapEvent.RectangleMouseOutEvent;
import com.neotropic.flow.component.googlemap.GoogleMapEvent.RectangleMouseOverEvent;
import com.neotropic.flow.component.googlemap.GoogleMapEvent.RectangleMouseUpEvent;
import com.neotropic.flow.component.googlemap.GoogleMapEvent.RectangleRightClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonObject;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("google-map-rectangle")
@JsModule("./google-map-rectangle.js")
public class GoogleMapRectangle extends Component {
    
    public GoogleMapRectangle() {
    }
    
    public GoogleMapRectangle(LatLngBounds bounds) {
        if (bounds != null)
            getElement().setPropertyJson(Constants.Property.BOUNDS, bounds.toJson());
    }
    //<editor-fold desc="Properties" defaultstate="collapsed">
    @Synchronize(property="bounds", value="rectangle-bounds-changed")
    public LatLngBounds getBounds() {
        return new LatLngBounds(
            (JsonObject) getElement().getPropertyRaw(Constants.Property.BOUNDS)
        );
    }
    
    public void setBounds(LatLngBounds bounds) {
        if (bounds != null)
            getElement().setPropertyJson(Constants.Property.BOUNDS, bounds.toJson());
        else
            getElement().setPropertyJson(Constants.Property.BOUNDS, Json.createNull());
    }
    
    public boolean getClickable() {
        return getElement().getProperty(Constants.Property.CLICKABLE, true);
    }
    
    public void setClickable(boolean clickable) {
        getElement().setProperty(Constants.Property.CLICKABLE, clickable);
    }
    
    public boolean getDraggable() {
        return getElement().getProperty(Constants.Property.DRAGGABLE, false);
    }
    
    public void setDraggable(boolean draggable) {
        getElement().setProperty(Constants.Property.DRAGGABLE, draggable);
    }
    
    public boolean getEditable() {
        return getElement().getProperty(Constants.Property.EDITABLE, false);
    }
    
    public void setEditable(boolean editable) {
        getElement().setProperty(Constants.Property.EDITABLE, editable);
    }
    
    public String getFillColor() {
        return getElement().getProperty(Constants.Property.FILL_COLOR);
    }
    
    public void setFillColor(String fillColor) {
        getElement().setProperty(Constants.Property.FILL_COLOR, fillColor);
    }
    
    public double getFillOpacity() {
        return getElement().getProperty(Constants.Property.FILL_OPACITY, Constants.Default.FILL_OPACITY);
    }
    
    public void setFillOpacity(double fillOpacity) {
        getElement().setProperty(Constants.Property.FILL_OPACITY, fillOpacity);
    }
    
    public String getStrokeColor() {
        return getElement().getProperty(Constants.Property.STROKE_COLOR);
    }
    
    public void setStrokeColor(String strokeColor) {
        getElement().setProperty(Constants.Property.STROKE_COLOR, strokeColor);
    }
    
    public double getStrokeOpacity() {
        return getElement().getProperty(Constants.Property.STROKE_OPACITY, Constants.Default.STROKE_OPACITY);
    }
    
    public void setStrokeOpacity(double strokeOpacity) {
        getElement().setProperty(Constants.Property.STROKE_OPACITY, strokeOpacity);
    }
    
    public String getStrokePosition() {
        return getElement().getProperty(Constants.Property.STROKE_POSITION);
    }
    
    public void setStrokePosition(StrokePosition strokePosition) {
        if (strokePosition != null)
            getElement().setProperty(Constants.Property.STROKE_POSITION, strokePosition.asString());
        else
            getElement().setPropertyJson(Constants.Property.STROKE_POSITION, Json.createNull());
    }
    
    public double getStrokeWeight() {
        return getElement().getProperty(Constants.Property.STROKE_WEIGHT, Constants.Default.STROKE_WEIGHT);
    }
    
    public void setStrokeWeight(double strokeWeight) {
        getElement().setProperty(Constants.Property.STROKE_WEIGHT, strokeWeight);
    }
    
    public boolean getVisible() {
        return getElement().getProperty(Constants.Property.VISIBLE, true);
    }
    
    public void setRectangleVisible(boolean visible) {
        getElement().setProperty(Constants.Property.VISIBLE, visible);
    }
    
    public double getZIndex() {
        return getElement().getProperty(Constants.Property.Z_INDEX, Constants.Default.Z_INDEX);
    }
    
    public void setZIndex(double zIndex) {
        getElement().setProperty(Constants.Property.Z_INDEX, zIndex);
    }
    //</editor-fold>
    //<editor-fold desc="Listeners" defaultstate="collapsed">
    public Registration addRectangleBoundsChangedListener(ComponentEventListener<RectangleBoundsChangedEvent> listener) {
        return addListener(RectangleBoundsChangedEvent.class, listener);
    }
    
    public Registration addRectangleClickListener(ComponentEventListener<RectangleClickEvent> listener) {
        return addListener(RectangleClickEvent.class, listener);
    }
    
    public Registration addRectangleDblClickListener(ComponentEventListener<RectangleDblClickEvent> listener) {
        return addListener(RectangleDblClickEvent.class, listener);
    }
    
    public Registration addRectangleDragListener(ComponentEventListener<RectangleDragEvent> listener) {
        return addListener(RectangleDragEvent.class, listener);
    }
    
    public Registration addRectangleDragEndListener(ComponentEventListener<RectangleDragEndEvent> listener) {
        return addListener(RectangleDragEndEvent.class, listener);
    }
    
    public Registration addRectangleDragStartListener(ComponentEventListener<RectangleDragStartEvent> listener) {
        return addListener(RectangleDragStartEvent.class, listener);
    }
    
    public Registration addRectangleMouseDownListener(ComponentEventListener<RectangleMouseDownEvent> listener) {
        return addListener(RectangleMouseDownEvent.class, listener);
    }
    
    public Registration addRectangleMouseMoveListener(ComponentEventListener<RectangleMouseMoveEvent> listener) {
        return addListener(RectangleMouseMoveEvent.class, listener);
    }
    
    public Registration addRectangleMouseOutListener(ComponentEventListener<RectangleMouseOutEvent> listener) {
        return addListener(RectangleMouseOutEvent.class, listener);
    }
    
    public Registration addRectangleMouseOverListener(ComponentEventListener<RectangleMouseOverEvent> listener) {
        return addListener(RectangleMouseOverEvent.class, listener);
    }
    
    public Registration addRectangleMouseUpListener(ComponentEventListener<RectangleMouseUpEvent> listener) {
        return addListener(RectangleMouseUpEvent.class, listener);
    }
    
    public Registration addRectangleRightClickListener(ComponentEventListener<RectangleRightClickEvent> listener) {
        return addListener(RectangleRightClickEvent.class, listener);
    }
    //</editor-fold>
}

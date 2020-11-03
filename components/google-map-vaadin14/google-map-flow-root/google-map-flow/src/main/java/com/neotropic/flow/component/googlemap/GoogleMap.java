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
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.JsonValue;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("google-map")
@JsModule("./google-map.js")
public class GoogleMap extends Component implements HasComponents {
    /**
     * @param apiKey Your API key and set the client id to null. See https://developers.google.com/maps/documentation/javascript/get-api-key
     * @param clientId Your client id and set the apiKey to null. See https://developers.google.com/maps/documentation/javascript/get-api-key
     */    
    public GoogleMap(String apiKey, String clientId) {
        getElement().getStyle().set(Constants.Property.WIDTH, "100%");
        getElement().getStyle().set(Constants.Property.HEIGHT, "100%");        
        getElement().setProperty(Constants.Property.API_KEY, apiKey);
        getElement().getStyle().set(Constants.Property.MIN_WIDTH, "250px");  
        getElement().setProperty(Constants.Property.CLIENT_ID, clientId);
    }
    public GoogleMap(String apiKey, String clientId, String width, String height) {
        getElement().getStyle().set(Constants.Property.WIDTH, width);
        getElement().getStyle().set(Constants.Property.HEIGHT, height);  
        getElement().getStyle().set(Constants.Property.MIN_WIDTH, "250px");  
        getElement().setProperty(Constants.Property.API_KEY, apiKey);
        getElement().setProperty(Constants.Property.CLIENT_ID, clientId);
    }
    public GoogleMap(String apiKey, String clientId, String libraries) {
        this(apiKey, clientId);
        getElement().setProperty(Constants.Property.LIBRARIES, libraries);
    }
    @Synchronize(property = "lat", value = "map-center-changed")
    public double getCenterLat() {
        return getElement().getProperty(Constants.Property.LAT, Constants.Default.LAT);
    }
    
    public void setCenterLat(double lat) {
        getElement().setProperty(Constants.Property.LAT, lat);
    }
    @Synchronize(property = "lng", value = "map-center-changed")
    public double getCenterLng() {
        return getElement().getProperty(Constants.Property.LNG, Constants.Default.LNG);
    }
    
    public void setCenterLng(double lng) {
        getElement().setProperty(Constants.Property.LNG, lng);
    }
    @Synchronize(property = "zoom", value = "map-zoom-changed")
    public double getZoom() {
        return getElement().getProperty(Constants.Property.ZOOM, Constants.Default.ZOOM);
    }
    
    public void setZoom(double zoom) {
        getElement().setProperty(Constants.Property.ZOOM, zoom);
    }
    
    public String getMapTypeId() {
        return getElement().getProperty(Constants.Property.MAP_TYPE_ID, Constants.Default.MAP_TYPE_ID);
    }
    
    public void setMapTypeId(String mapTypeId) {
        getElement().setProperty(Constants.Property.MAP_TYPE_ID, mapTypeId);
    }
    public boolean getDisableDefaultUi() {
        return getElement().getProperty(Constants.Property.DISABLE_DEFAULT_UI, false);
    }
    public void setDisableDefaultUi(boolean disableDefaultUi) {
        getElement().setProperty(Constants.Property.DISABLE_DEFAULT_UI, disableDefaultUi);
    }
    public boolean getZoomControl() {
        return getElement().getProperty(Constants.Property.ZOOM_CONTROL, false);
    }
    public void setZoomControl(boolean zoomControl) {
        getElement().setProperty(Constants.Property.ZOOM_CONTROL, zoomControl);        
    }
    public boolean getMapTypeControl() {
        return getElement().getProperty(Constants.Property.MAP_TYPE_CONTROL, false);
    }
    public void setMapTypeControl(boolean mapTypeControl) {
        getElement().setProperty(Constants.Property.MAP_TYPE_CONTROL, mapTypeControl);
    }
    public boolean getScaleControl() {
        return getElement().getProperty(Constants.Property.SCALE_CONTROL, false);
    }
    public void setScaleControl(boolean scaleControl) {
        getElement().setProperty(Constants.Property.SCALE_CONTROL, scaleControl);
    }
    public boolean getStreetViewControl() {
        return getElement().getProperty(Constants.Property.STREET_VIEW_CONTROL, false);
    }
    public void setStreetViewControl(boolean streetViewControl) {
        getElement().setProperty(Constants.Property.STREET_VIEW_CONTROL, streetViewControl);
    }
    public boolean getRotateControl() {
        return getElement().getProperty(Constants.Property.ROTATE_CONTROL, false);
    }
    public void setRotateControl(boolean rotateControl) {
        getElement().setProperty(Constants.Property.ROTATE_CONTROL, rotateControl);
    }
    public boolean getFullscreenControl() {
        return getElement().getProperty(Constants.Property.FULLSCREEN_CONTROL, false);
    }
    public void setFullscreenControl(boolean fullscreenControl) {
        getElement().setProperty(Constants.Property.FULLSCREEN_CONTROL, fullscreenControl);
    }
    /**
     * Gets if false, prevents the map from being dragged.
     * @return If dragging is enabled.
     */
    public boolean getDraggable() {
        return getElement().getProperty(Constants.Property.DRAGGABLE, true);
    }
    /**
     * Sets if false, prevents the map from being dragged.
     * @param draggable Dragging is enabled
     */
    public void setDraggable(boolean draggable) {
        getElement().setProperty(Constants.Property.DRAGGABLE, draggable);
    }
    /**
     * Gets the maximum zoom level which will be displayed on the map.
     * @return The maximum zoom level.
     */
    public double getMaxZoom() {
        return getElement().getProperty(Constants.Property.MAX_ZOOM, 0.0);
    }
    /**
     * Sets the maximum zoom level which will be displayed on the map.
     * @param maxZoom The maximum zoom level
     */
    public void setMaxZoom(Double maxZoom) {
        if (maxZoom != null)
            getElement().setProperty(Constants.Property.MAX_ZOOM, maxZoom);
        else
            getElement().setPropertyJson(Constants.Property.MAX_ZOOM, Json.createNull());
    }
    /**
     * Gets the minimum zoom level which will be displayed on the map.
     * @return The minimum zoom level.
     */
    public double getMinZoom() {
        return getElement().getProperty(Constants.Property.MIN_ZOOM, 0.0);
    }
    /**
     * Sets the minimum zoom level which will be displayed on the map.
     * @param minZoom The minimum zoom level
     */
    public void setMinZoom(Double minZoom) {
        if (minZoom != null)
            getElement().setProperty(Constants.Property.MIN_ZOOM, minZoom);
        else
            getElement().setPropertyJson(Constants.Property.MIN_ZOOM, Json.createNull());
    }
    public JsonValue getStyles() {
        return (JsonValue) getElement().getPropertyRaw(Constants.Property.STYLES);
    }
    
    public void setStyles(JsonValue styles) {
        getElement().setPropertyJson(Constants.Property.STYLES, styles);
    }
    
    public void setWidth(String width) {
        getElement().setProperty(Constants.Property.WIDTH, width);
    }
 
    public void setHeight(String height) {
        getElement().setProperty(Constants.Property.HEIGHT, height);
    }
    
    public void setMinWidth(String width) {
        getElement().setProperty(Constants.Property.MIN_WIDTH, width);
    }
 
    public void setMinHeight(String height) {
        getElement().setProperty(Constants.Property.MIN_HEIGHT, height);
    }
    @Synchronize(property="bounds", value="map-bounds-changed")
    public LatLngBounds getBounds() {
        JsonObject bounds = (JsonObject) getElement().getPropertyRaw(Constants.Property.BOUNDS);
        return bounds != null ? new LatLngBounds(bounds) : null;
    }
    
    public void newMarker(GoogleMapMarker googleMapMarker) {
        add(googleMapMarker);
    }
    
    public void removeMarker(GoogleMapMarker googleMapMarker) {
        remove(googleMapMarker);
    }
    
    public void newPolyline(GoogleMapPolyline polyline) {
        add(polyline);
    }
    
    public void removePolyline(GoogleMapPolyline polyline) {
        remove(polyline);
    }
    
    public void newPolygon(GoogleMapPolygon polygon) {
        add(polygon);
    }
    
    public void removePolygon(GoogleMapPolygon polygon) {
        remove(polygon);
    }
    
    public void newDrawingManager(DrawingManager drawingManager) {
        add(drawingManager);
    }
    
    public void removeDrawingManager(DrawingManager drawingManager) {
        remove(drawingManager);
    }
    
    public void addInfoWindow(InfoWindow infoWindow) {
        add(infoWindow);
    }
    
    public void removeInfoWindow(InfoWindow infoWindow) {
        remove(infoWindow);
    }
    
    public void addRectangle(GoogleMapRectangle rectangle) {
        add(rectangle);
    }
    
    public void removeRectangle(GoogleMapRectangle rectangle) {
        remove(rectangle);
    }
    
    public void addOverlayView(OverlayView overlayView) {
        add(overlayView);
    }
    
    public void removeOverlayView(OverlayView overlayView) {
        remove(overlayView);
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
    
    public Registration addMapBoundsChanged(ComponentEventListener<GoogleMapEvent.MapBoundsChangedEvent> listener) {
        return addListener(GoogleMapEvent.MapBoundsChangedEvent.class, listener);
    }
}


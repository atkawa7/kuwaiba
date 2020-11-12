/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ospman.api;

import com.vaadin.flow.component.HasComponents;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * Operations to implement an overlay on the map in the Outside Plant module.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface MapOverlay {
    /**
     * Gets a component that represent a map overlay
     * @return A component
     */
    HasComponents getComponent();
    /**
     * Gets the pixel coordinates of the given geographical location
     * @param latLng geographical location
     * @param pixelConsumer operation that accepts a pixel coordinates
     */
    void getProjectionFromLatLngToDivPixel(GeoCoordinate latLng, Consumer<GeoPoint> pixelConsumer);
    /**
     * Executes callback to get the pixel coordinates of the given geographical location.
     * @param geoCoordinates List of geographical coordinates.
     * @param callback Callback to get the pixel coordinates of the given geographical location.
     */
    void getProjectionFromLatLngToDivPixel(List<GeoCoordinate> geoCoordinates, Consumer<List<GeoPoint>> callback);
    /**
     * Executes callback to get the pixel coordinates of the given geographical location.
     * @param geoCoordinates List of geographical coordinates.
     * @param callback Callback to get the pixel coordinates of the given geographical location.
     */
    void getProjectionFromLatLngToDivPixel(HashMap<String, List<GeoCoordinate>> geoCoordinates, Consumer<HashMap<String, List<GeoPoint>>> callback);
    /**
     * Gets the geographical location of the given pixel coordinates
     * @param pixel pixel coordinates
     * @param latLngConsumer operation that accepts a geographical location
     */
    void getProjectionFromDivPixelToLatLng(GeoPoint pixel, Consumer<GeoCoordinate> latLngConsumer);
    /**
     * Executes callback to get the geographical coordinates from pixel coordinates.
     * @param pixelCoordinates List of pixel coordinates.
     * @param callback Callback to get the geographical coordinates from pixel coordinates.
     */
    void getProjectionFromDivPixelToLatLng(List<GeoPoint> pixelCoordinates, Consumer<List<GeoCoordinate>> callback);
    /**
     * Executes callback to get the geographical coordinates from pixel coordinates.
     * @param pixelCoordinates List of pixel coordinates.
     * @param callback Callback to get the geographical coordinates from pixel coordinates.
     */
    void getProjectionFromDivPixelToLatLng(HashMap<String, List<GeoPoint>> pixelCoordinates, Consumer<HashMap<String, List<GeoCoordinate>>> callback);
    /**
     * Adds a width change event.
     * @param listener Callback executed when width changed.
     */
    public void addWidthChangedEventListener(WidthChangedEventListener listener);
    /**
     * Removes a width change event.
     * @param listener Callback executed when width changed.
     */
    public void removeWidthChangedEventListener(WidthChangedEventListener listener);
    /**
     * Removes all width change event.
     */
    public void removeAllWidthChangedEventListener();
    /**
     * Callback executed when width changed.
     */    
    public interface WidthChangedEventListener extends Consumer<WidthChangedEvent> {
    }
    /**
     * Fired when width changed
     */
    public class WidthChangedEvent {
        private double width;
        private WidthChangedEventListener listener;
        
        public WidthChangedEvent(double width, WidthChangedEventListener listener) {
            this.width = width;
            this.listener = listener;
        }
        
        public WidthChangedEventListener getListener() {
            return listener;
        }
        
        public double getWidth() {
            return width;
        }
    }
}

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

import com.vaadin.flow.component.Component;
import java.util.List;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Operations to implement a map in the Outside Plant module.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface MapProvider {
    /**
     * Create a component that represents a map.
     * @param ts The Translation Service
     * @param aem The Application Entity Manager
     */
    void createComponent(ApplicationEntityManager aem, TranslationService ts);
    /**
     * Gets a component that represents a map.
     */
    Component getComponent();
    /**
     * Creates a map overlay.
     * @param bounds A rectangle in geographical coordinates
     * @return A map overlay
     */
    MapOverlay createOverlay(GeoBounds bounds);
    /**
     * Removes a map overlay.
     * @param mapOverlay Map overlay to remove.
     */
    void removeOverlay(MapOverlay mapOverlay);
    /**
     * Gets the map center
     * @return map center
     */
    GeoCoordinate getCenter();
    /**
     * Gets the lat/lng of the current viewport
     * @return map bounds
     */
    GeoBounds getBounds();
    /**
     * Gets the lat/lng of the current viewport
     * @param consumer
     * @return map bounds
     */
    void getBounds(Consumer<GeoBounds> consumer);
    /**
     * Set map center
     * @param center map center
     */
    void setCenter(GeoCoordinate center);
    /**
     * Gets the map zoom
     * @return map zoom
     */
    double getZoom();
    /**
     * Set the map zoom
     * @param zoom map zoom
     */
    void setZoom(double zoom);
    /**
     * Gets if false, prevents the map from being dragged.
     * @return If dragging is enabled.
     */
    boolean getDraggable();
    /**
     * Sets if false, prevents the map from being dragged.
     * @param draggable Dragging is enabled
     */
    void setDraggable(boolean draggable);
    /**
     * Gets the maximum zoom level which will be displayed on the map.
     * @return The maximum zoom level.
     */
    double getMaxZoom();
    /**
     * Sets the maximum zoom level which will be displayed on the map.
     * @param maxZoom The maximum zoom level
     */
    void setMaxZoom(Double maxZoom);
    /**
     * Gets the minimum zoom level which will be displayed on the map.
     * @return The minimum zoom level.
     */
    double getMinZoom();
    /**
     * Sets the minimum zoom level which will be displayed on the map.
     * @param minZoom The minimum zoom level
     */
    void setMinZoom(Double minZoom);
    /**
     * Set the map hand mode.
     */
    void setHandMode();
    /**
     * Set the drawing mode to overlay.
     * @param drawingOverlayComplete Operation that accepts a rectangle in geographical coordinates
     */
    void setDrawingOverlayMode(Consumer<GeoBounds> drawingOverlayComplete);
    /**
     * Set the drawing mode to marker.
     * @param drawingMarkerComplete Operation that accepts a coordinate
     */
    void setDrawingMarkerMode(Consumer<GeoCoordinate> drawingMarkerComplete);
    /**
     * Set the drawing mode to polyline.
     * @param drawingPolylineComplete Operation that accepts coordinates
     */
    void setDrawingPolylineMode(Consumer<List<GeoCoordinate>> drawingPolylineComplete);
    /**
     * Adds a bounds changed event.
     * @param listener Callback executed when bounds changed.
     */
    public void addBoundsChangedEventListener(BoundsChangedEventListener listener);
    /**
     * Removes a bounds changed event.
     * @param listener Callback executed when bounds changed.
     */
    public void removeBoundsChangedEventListener(BoundsChangedEventListener listener);
    /**
     * Removes all bounds changed event.
     */
    public void removeAllBoundsChangedEventListener();
    /**
     * Callback executed when bounds changed.
     */
    public interface BoundsChangedEventListener extends Consumer<BoundsChangedEvent> {
    }
    public class BoundsChangedEvent {
        private GeoBounds bounds;
        private BoundsChangedEventListener listener;
        
        public BoundsChangedEvent(GeoBounds bounds, BoundsChangedEventListener listener) {
            this.bounds = bounds;
            this.listener = listener;
        }
        
        public BoundsChangedEventListener getListener() {
            return listener;
        }
        
        public GeoBounds getBounds() {
            return bounds;
        }
    }
}

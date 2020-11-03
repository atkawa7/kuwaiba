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
package com.neotropic.kuwaiba.modules.commercial.ospman.providers.google;

import com.neotropic.flow.component.googlemap.LatLng;
import com.neotropic.flow.component.googlemap.LatLngBounds;
import com.neotropic.flow.component.googlemap.OverlayView;
import com.neotropic.flow.component.googlemap.Point;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoBounds;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoPoint;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapOverlay;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Map Overlay implementation to Google Maps
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GoogleMapsOverlay implements MapOverlay {
    private final GeoBounds bounds;
    private OverlayView overlayView;
    /**
     * The map overlay width
     */
    private Double width;
    /**
     * The map overlay title
     */
    private String title;
    /**
     * Is map overlay enabled?
     */
    private boolean enabled;
    /**
     * The map overlay id
     */
    private String id;
    /**
     * Set of width changed event listeners
     */
    private List<WidthChangedEventListener> widthChangedEventListeners = new ArrayList();
    
    public GoogleMapsOverlay(GeoBounds bounds) {
        this.bounds = bounds;
        this.id = UUID.randomUUID().toString();
    }
    @Override
    public GeoBounds getBounds() {
        return bounds;
    }
    @Override
    public Double getWidth() {
        return width;
    }
    @Override
    public void setWidth(double width) {
        this.width = width;
    }
    @Override
    public OverlayView getComponent() {
        if (overlayView == null) {
            GeoCoordinate northeast = bounds.getNortheast();
            GeoCoordinate southwest = bounds.getSouthwest();
            overlayView = new OverlayView(new LatLngBounds(
                new LatLng(southwest.getLatitude(), southwest.getLongitude()), 
                new LatLng(northeast.getLatitude(), northeast.getLongitude()) 
            ));
            overlayView.addWidthChangedListener(event -> {
                setWidth(event.getWidth());
                new ArrayList<>(widthChangedEventListeners).forEach(listener -> {
                    if (widthChangedEventListeners.contains(listener))
                        listener.accept(new WidthChangedEvent(event.getWidth(), listener));
                });
            });
        }
        return overlayView;
    }
    @Override
    public void getProjectionFromLatLngToDivPixel(GeoCoordinate latLng, Consumer<GeoPoint> pixelConsumer) {
        Objects.requireNonNull(latLng);
        Objects.requireNonNull(pixelConsumer);
        overlayView.fromLatLngToDivPixel(
            new LatLng(latLng.getLatitude(), latLng.getLongitude()), 
            point -> pixelConsumer.accept(new GeoPoint(point.getX(), point.getY()))
        );
    }
    @Override
    public void getProjectionFromDivPixelToLatLng(GeoPoint pixel, Consumer<GeoCoordinate> latLngConsumer) {
        Objects.requireNonNull(pixel);
        Objects.requireNonNull(latLngConsumer);
        overlayView.fromDivPixelToLatLng(
            new Point(pixel.getX(), pixel.getY()), 
            latLng -> latLngConsumer.accept(new GeoCoordinate(latLng.getLat(), latLng.getLng()))
        );
    }
    @Override
    public void addWidthChangedEventListener(WidthChangedEventListener listener) {
        widthChangedEventListeners.add(listener);
    }
    @Override
    public void removeWidthChangedEventListener(WidthChangedEventListener listener) {
        widthChangedEventListeners.removeIf(l -> l.equals(listener));
    }
    @Override
    public void removeAllWidthChangedEventListener() {
        widthChangedEventListeners.clear();
    }
}

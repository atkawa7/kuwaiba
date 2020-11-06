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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
     * Set of width changed event listeners
     */
    private List<WidthChangedEventListener> widthChangedEventListeners = new ArrayList();
    
    public GoogleMapsOverlay(GeoBounds bounds) {
        this.bounds = bounds;
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
    public void getProjectionFromLatLngToDivPixel(List<GeoCoordinate> geoCoordinates, Consumer<List<GeoPoint>> callback) {
        Objects.requireNonNull(geoCoordinates);
        Objects.requireNonNull(callback);
        List<LatLng> coordinates = new ArrayList();
        geoCoordinates.forEach(geoCoordinate ->
            coordinates.add(new LatLng(geoCoordinate.getLatitude(), geoCoordinate.getLongitude()))
        );
        overlayView.fromLatLngToDivPixel(coordinates, pixelCoordinates -> {
            if (pixelCoordinates != null) {
                List<GeoPoint> points = new ArrayList();
                pixelCoordinates.forEach(pixelCoordinate -> 
                    points.add(new GeoPoint(pixelCoordinate.getX(), pixelCoordinate.getY()))
                );
                callback.accept(points);
            }
            else
                callback.accept(null);
        });
    }
    @Override
    public void getProjectionFromLatLngToDivPixel(HashMap<String, List<GeoCoordinate>> geoCoordinates, Consumer<HashMap<String, List<GeoPoint>>> callback) {
        Objects.requireNonNull(geoCoordinates);
        Objects.requireNonNull(callback);
        if (!geoCoordinates.isEmpty()) {
            HashMap<String, List<LatLng>> coordinates = new HashMap();
            geoCoordinates.forEach((key, value) -> {
                if (value != null && !value.isEmpty()) {
                    List<LatLng> coordinatesList = new ArrayList();
                    value.forEach(coordinate -> coordinatesList.add(new LatLng(coordinate.getLatitude(), coordinate.getLongitude())));
                    coordinates.put(key, coordinatesList);
                }
            });
            overlayView.fromLatLngToDivPixel(coordinates, pixelCoordinates -> {
                if (pixelCoordinates != null) {
                    HashMap<String, List<GeoPoint>> points = new HashMap();
                    pixelCoordinates.forEach((key, value) -> {
                        if (value != null && !value.isEmpty()) {
                            List<GeoPoint> pointsList = new ArrayList();
                            value.forEach(point -> pointsList.add(new GeoPoint(point.getX(), point.getY())));
                            points.put(key, pointsList);
                        }
                    });
                    callback.accept(points);
                }
                else
                    callback.accept(null);
            });
        }
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
    public void getProjectionFromDivPixelToLatLng(List<GeoPoint> pixelCoordinates, Consumer<List<GeoCoordinate>> callback) {
        Objects.requireNonNull(pixelCoordinates);
        Objects.requireNonNull(callback);
        List<Point> points = new ArrayList();
        pixelCoordinates.forEach(pixelCoordinate -> 
            points.add(new Point(pixelCoordinate.getX(), pixelCoordinate.getY()))
        );
        overlayView.fromDivPixelToLatLng(points, geoCoordinates -> {
            if (geoCoordinates != null) {
                List<GeoCoordinate> coordinates = new ArrayList();
                geoCoordinates.forEach(geoCoordinate -> 
                    coordinates.add(new GeoCoordinate(geoCoordinate.getLat(), geoCoordinate.getLng()))
                );
                callback.accept(coordinates);
            }
            else
                callback.accept(null);
        });
    }
    @Override
    public void getProjectionFromDivPixelToLatLng(HashMap<String, List<GeoPoint>> pixelCoordinates, Consumer<HashMap<String, List<GeoCoordinate>>> callback) {
        Objects.requireNonNull(pixelCoordinates);
        Objects.requireNonNull(callback);
        if (!pixelCoordinates.isEmpty()) {
            HashMap<String, List<Point>> points = new HashMap();
            pixelCoordinates.forEach((key, value) -> {
                if (value != null && !value.isEmpty()) {
                    List<Point> pointsList = new ArrayList();
                    value.forEach(point -> pointsList.add(new Point(point.getX(), point.getY())));
                    points.put(key, pointsList);
                }
            });
            overlayView.fromDivPixelToLatLng(points, geoCoordinates -> {
                if (geoCoordinates != null) {
                    HashMap<String, List<GeoCoordinate>> coordinates = new HashMap();
                    geoCoordinates.forEach((key, value) -> {
                        if (value != null && !value.isEmpty()) {
                            List<GeoCoordinate> coordinatesList = new ArrayList();
                            value.forEach(coordinate -> coordinatesList.add(new GeoCoordinate(coordinate.getLat(), coordinate.getLng())));
                            coordinates.put(key, coordinatesList);
                        }
                    });
                    callback.accept(coordinates);
                }
                else
                    callback.accept(null);
            });
        }
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

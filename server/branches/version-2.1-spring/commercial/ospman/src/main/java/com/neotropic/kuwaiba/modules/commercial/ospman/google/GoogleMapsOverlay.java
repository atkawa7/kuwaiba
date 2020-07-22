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
package com.neotropic.kuwaiba.modules.commercial.ospman.google;

import com.neotropic.flow.component.googlemap.LatLng;
import com.neotropic.flow.component.googlemap.LatLngBounds;
import com.neotropic.flow.component.googlemap.OverlayView;
import com.neotropic.kuwaiba.modules.commercial.ospman.GeoBounds;
import com.neotropic.kuwaiba.modules.commercial.ospman.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.GeoPoint;
import com.neotropic.kuwaiba.modules.commercial.ospman.MapOverlay;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Map Overlay implementation to Google Maps
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GoogleMapsOverlay implements MapOverlay {
    private final GeoBounds bounds;
    private OverlayView overlayView;
    private Double width;
    
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
        }
        return overlayView;
    }

    @Override
    public void getProjectionFromLatLngToDivPixel(GeoCoordinate latLng, Consumer<GeoPoint> pixelConsumer) {
        Objects.requireNonNull(latLng);
        Objects.requireNonNull(pixelConsumer);
        overlayView.fromLatLngToDivPixel(new LatLng(latLng.getLatitude(), latLng.getLongitude()), point -> {
            pixelConsumer.accept(new GeoPoint(point.getX(), point.getY()));
        });
    }

    @Override
    public void getProjectionFromDivPixelToLatLng(GeoPoint pixel, Consumer<GeoCoordinate> latLngConsumer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addWidthChangedConsumer(Consumer<Double> widthChangedConsumer) {
        Objects.requireNonNull(widthChangedConsumer);
        overlayView.addWidthChangedListener(event -> {
            widthChangedConsumer.accept(event.getWidth());
        });
    }
}

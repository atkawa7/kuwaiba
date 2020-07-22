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
package com.neotropic.kuwaiba.modules.commercial.ospman;

import com.vaadin.flow.component.HasComponents;
import java.util.function.Consumer;

/**
 * Operations to implement an overlay on the map in the Outside Plant module.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface MapOverlay {
    /**
     * Gets a rectangle representation in geographical coordinates from 
     * southwest and northeast.
     * @return A rectangle representation in geographical coordinates
     */
    GeoBounds getBounds();
    /**
     * Gets the map overlay width
     * @return The width
     */
    Double getWidth();
    /**
     * Set the map overlay width
     * @param width the map overlay width
     */
    void setWidth(double width);
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
     * Gets the geographical location of the given pixel coordinates
     * @param pixel pixel coordinates
     * @param latLngConsumer operation that accepts a geographical location
     */
    void getProjectionFromDivPixelToLatLng(GeoPoint pixel, Consumer<GeoCoordinate> latLngConsumer);
    /**
     * Adds a operation that accepts a width value
     * @param widthChangedConsumer 
     */
    void addWidthChangedConsumer(Consumer<Double> widthChangedConsumer);
}

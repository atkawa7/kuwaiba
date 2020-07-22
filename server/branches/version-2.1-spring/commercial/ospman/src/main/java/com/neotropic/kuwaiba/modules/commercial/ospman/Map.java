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

import com.vaadin.flow.component.Component;
import java.util.List;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Operations to implement a map in the Outside Plant module.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface Map {
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
}

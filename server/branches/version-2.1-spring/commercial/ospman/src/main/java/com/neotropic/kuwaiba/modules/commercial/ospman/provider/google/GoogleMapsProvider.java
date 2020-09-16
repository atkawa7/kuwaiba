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
package com.neotropic.kuwaiba.modules.commercial.ospman.provider.google;

import com.neotropic.flow.component.googlemap.DrawingManager;
import com.neotropic.flow.component.googlemap.GoogleMap;
import com.neotropic.flow.component.googlemap.LatLng;
import com.neotropic.flow.component.googlemap.OverlayType;
import com.neotropic.kuwaiba.modules.commercial.ospman.persistence.OutsidePlantService;
import com.neotropic.kuwaiba.modules.commercial.ospman.provider.GeoBounds;
import com.neotropic.kuwaiba.modules.commercial.ospman.provider.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.provider.MapOverlay;
import com.vaadin.flow.component.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.SimpleNotification;
import com.neotropic.kuwaiba.modules.commercial.ospman.provider.MapProvider;

/**
 * Map implementation to Google Maps
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GoogleMapsProvider implements MapProvider {
    public static final String LIBRARIES = "drawing"; //NOI18N
    public static final String PROPERTY_API_KEY = "apiKey"; //NOI18N
    public static final String PROPERTY_CENTER = "center"; //NOI18N
    public static final String PROPERTY_ZOOM = "zoom"; //NOI18N
    
    private GoogleMap googleMap;
    private DrawingManager drawingManager;
    
    public GoogleMapsProvider() {
    }
    
    private HashMap<String, Object> getProperties(ApplicationEntityManager aem, TranslationService ts) {
        HashMap<String, Object> properties = new HashMap();
        
        try {
            properties.put(PROPERTY_API_KEY, aem.getConfigurationVariableValue("general.maps.apiKey")); //NOI18N
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                "The configuration variable general.maps.apiKey has not been set. The default map will be used"
            ).open();
        }
        double mapCenterLat;
        double mapCenterLng;
        
        try {
            mapCenterLat = (double) aem.getConfigurationVariableValue("widgets.simplemap.centerLatitude"); //NOI18N
        } catch (InventoryException ex) {
            mapCenterLat = OutsidePlantService.DEFAULT_CENTER_LATITUDE;
        }
        
        try {
            mapCenterLng = (double) aem.getConfigurationVariableValue("widgets.simplemap.centerLongitude"); //NOI18N
        } catch (InventoryException ex) {
            mapCenterLng = OutsidePlantService.DEFAULT_CENTER_LONGITUDE;
        }
        
        properties.put(PROPERTY_CENTER, new GeoCoordinate(mapCenterLat, mapCenterLng)); //NOI18N
        
        try {
            properties.put(PROPERTY_ZOOM, aem.getConfigurationVariableValue("widgets.simplemap.zoom")); //NOI18N
        } catch (InventoryException ex) {
            properties.put(PROPERTY_ZOOM, OutsidePlantService.DEFAULT_ZOOM);
        }
        
        return properties;
    }
    
    @Override
    public void createComponent(ApplicationEntityManager aem, TranslationService ts) {
        HashMap<String, Object> properties = getProperties(aem, ts);
        
        final String apiKey = (String) properties.get(PROPERTY_API_KEY);
        final GeoCoordinate center = (GeoCoordinate) properties.get(PROPERTY_CENTER);
        final double zoom = Double.valueOf(String.valueOf(properties.get(PROPERTY_ZOOM)));

        googleMap = new GoogleMap(apiKey, null, LIBRARIES);
        googleMap.setDisableDefaultUi(true);
        googleMap.setCenterLat(center.getLatitude());
        googleMap.setCenterLng(center.getLongitude());
        googleMap.setZoom(zoom);
        
        drawingManager = new DrawingManager();
        googleMap.newDrawingManager(drawingManager);
    }

    @Override
    public Component getComponent() {
        return googleMap;
    }

    @Override
    public MapOverlay createOverlay(GeoBounds bounds) {
        Objects.requireNonNull(bounds);
        
        if (googleMap != null) {
            GoogleMapsOverlay mapOverlay = new GoogleMapsOverlay(bounds);
            googleMap.addOverlayView(mapOverlay.getComponent());
            return mapOverlay;
        }
        return null;
    }
    
    public GeoCoordinate getCenter() {
        return new GeoCoordinate(googleMap.getCenterLat(), googleMap.getCenterLng());
    }
    
    public void setCenter(GeoCoordinate center) {
        googleMap.setCenterLat(center.getLatitude());
        googleMap.setCenterLng(center.getLongitude());
    }
    
    public double getZoom() {
        return googleMap.getZoom();
    }
    
    public void setZoom(double zoom) {
        googleMap.setZoom(zoom);
    }
    
    @Override
    public void setHandMode() {
        if (drawingManager != null)
            drawingManager.setDrawingMode(null);
    }
    
    @Override
    public void setDrawingOverlayMode(Consumer<GeoBounds> drawingOverlayComplete) {
        if (drawingManager != null) {
            drawingManager.setDrawingMode(null);
            drawingManager.setDrawingMode(OverlayType.RECTANGLE);
            if (drawingOverlayComplete != null)
                drawingManager.addDrawingManagerRectangleCompleteListener(event -> {
                    drawingManager.setDrawingMode(null);
                    event.unregisterListener();
                    
                    LatLng northEast = event.getBounds().getNorthEast();
                    LatLng southWest = event.getBounds().getSouthWest();
                    
                    drawingOverlayComplete.accept(new GeoBounds(
                        new GeoCoordinate(northEast.getLat(), northEast.getLng()), 
                        new GeoCoordinate(southWest.getLat(), southWest.getLng())
                    ));
                });
        }
    }
    
    @Override    
    public void setDrawingMarkerMode(Consumer<GeoCoordinate> drawingMarkerComplete) {
        if (drawingManager != null) {
            drawingManager.setDrawingMode(null);
            drawingManager.setDrawingMode(OverlayType.MARKER);
            if (drawingMarkerComplete != null)
                drawingManager.addDrawingManagerMarkerCompleteListener(event -> {
                    drawingManager.setDrawingMode(null);
                    event.unregisterListener();
                    
                    drawingMarkerComplete.accept(new GeoCoordinate(event.getLat(), event.getLng()));
                });
        }
    }
    
    @Override    
    public void setDrawingPolylineMode(Consumer<List<GeoCoordinate>> drawingPolylineComplete) {
        if (drawingManager != null) {
            drawingManager.setDrawingMode(null);
            drawingManager.setDrawingMode(OverlayType.POLYLINE);
            if (drawingPolylineComplete != null)
                drawingManager.addDrawingManagerPolylineCompleteListener(event -> {
                    drawingManager.setDrawingMode(null);
                    event.unregisterListener();
                    
                    List<GeoCoordinate> coordinates = new ArrayList();
                    for (LatLng latLng : event.getPath())
                        coordinates.add(new GeoCoordinate(latLng.getLat(), latLng.getLng()));
                    drawingPolylineComplete.accept(coordinates);
                });
        }
    }
}

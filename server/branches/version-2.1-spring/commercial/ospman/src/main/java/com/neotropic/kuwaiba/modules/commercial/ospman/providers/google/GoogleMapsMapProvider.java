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

import com.neotropic.flow.component.googlemap.DrawingManager;
import com.neotropic.flow.component.googlemap.GeometryPoly;
import com.neotropic.flow.component.googlemap.GoogleMap;
import com.neotropic.flow.component.googlemap.LatLng;
import com.neotropic.flow.component.googlemap.OverlayType;
import com.neotropic.kuwaiba.modules.commercial.ospman.OutsidePlantService;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapEdge;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapNode;
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
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapProvider;
import java.util.function.BiConsumer;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.util.visual.notifications.AbstractNotification;

/**
 * Map implementation to Google Maps
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GoogleMapsMapProvider implements MapProvider {
    public static final String LIBRARIES = "drawing,geometry"; //NOI18N
    public static final String PROPERTY_API_KEY = "apiKey"; //NOI18N
    public static final String PROPERTY_CENTER = "center"; //NOI18N
    public static final String PROPERTY_ZOOM = "zoom"; //NOI18N
    
    private GoogleMap googleMap;
    private DrawingManager drawingManager;
    private GeometryPoly geometryPoly;
    private double minZoomForLabels;
    /**
     * Set of idle event listeners.
     */
    private final List<IdleEventListener> idleEventListeners = new ArrayList();
    /**
     * Set of View and Google Map Nodes
     */
    private final HashMap<BusinessObjectViewNode, GoogleMapNode> nodes = new HashMap();
    /**
     * Set of View and Google Map Edges
     */
    private final HashMap<BusinessObjectViewEdge, GoogleMapEdge> edges = new HashMap();
    /**
     * Resource factory. Used to get node icons.
     */
    private ResourceFactory resourceFactory;
    /**
     * Metadata Entity Manager.
     */
    private MetadataEntityManager mem;
    /**
     * Translation Service.
     */
    private TranslationService ts;
    
    private EdgeHelper edgeHelper;
    
    private PathSelectionHelper pathSelectionHelper;
    
    public GoogleMapsMapProvider() {
    }
    
    private HashMap<String, Object> getProperties(ApplicationEntityManager aem, TranslationService ts) {
        HashMap<String, Object> properties = new HashMap();
        
        try {
            properties.put(PROPERTY_API_KEY, aem.getConfigurationVariableValue("general.maps.apiKey")); //NOI18N
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                "The configuration variable general.maps.apiKey has not been set. The default map will be used", 
                AbstractNotification.NotificationType.ERROR, ts
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
        
        try {
            setMinZoomForLabels(Double.valueOf(String.valueOf(aem.getConfigurationVariableValue("module.ospman.minZoomForLabels")))); //NOI18N
        } catch (InventoryException ex) {
            setMinZoomForLabels(OutsidePlantService.DEFAULT_MIN_ZOOM_FOR_LABELS); //NOI18N
        }
        
        return properties;
    }
    
    @Override
    public void createComponent(ApplicationEntityManager aem, MetadataEntityManager mem, ResourceFactory resourceFactory, TranslationService ts) {
        Objects.requireNonNull(aem);
        Objects.requireNonNull(resourceFactory);
        Objects.requireNonNull(ts);
        Objects.requireNonNull(mem);
        HashMap<String, Object> properties = getProperties(aem, ts);
        this.resourceFactory = resourceFactory;
        this.ts = ts;
        this.mem = mem;
        
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
        
        geometryPoly = new GeometryPoly(googleMap);
        
        googleMap.addMapIdleListener(event ->
            new ArrayList<>(idleEventListeners).forEach(listener -> {
                if (idleEventListeners.contains(listener))
                    listener.accept(new IdleEvent(listener));
            })
        );
    }

    @Override
    public Component getComponent() {
        return googleMap;
    }
    @Override
    public GeoCoordinate getCenter() {
        return new GeoCoordinate(googleMap.getCenterLat(), googleMap.getCenterLng());
    }
    @Override
    public void setCenter(GeoCoordinate center) {
        googleMap.setCenterLat(center.getLatitude());
        googleMap.setCenterLng(center.getLongitude());
    }
    @Override
    public double getZoom() {
        return googleMap.getZoom();
    }
    @Override
    public void setZoom(double zoom) {
        googleMap.setZoom(zoom);
    }
    @Override
    public double getMinZoomForLabels() {
        return minZoomForLabels;
    }
    @Override
    public void setMinZoomForLabels(double minZoomForLabels) {
        this.minZoomForLabels = minZoomForLabels;
    }
    @Override
    public void setHandMode() {
        if (drawingManager != null)
            drawingManager.setDrawingMode(null);
        if (edgeHelper != null)
            edgeHelper.cancel();
        if (pathSelectionHelper != null)
            pathSelectionHelper.cancel();
    }
    
    @Override    
    public void setDrawingMarkerMode(Consumer<GeoCoordinate> drawingMarkerComplete) {
        setHandMode();
        if (drawingManager != null) {
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
    public void setDrawingEdgeMode(BiConsumer<HashMap<String, Object>, Runnable> callbackEdgeComplete) {
        setHandMode();
        if (edgeHelper == null)
            edgeHelper = new EdgeHelper(callbackEdgeComplete, nodes.values(), edges.values(), googleMap);
        edgeHelper.init();
    }
    
    @Override
    public void setPathSelectionMode(BiConsumer<List<BusinessObjectViewEdge>, Runnable> callbackPathSelectionComplete) {
        setHandMode();
        if (pathSelectionHelper == null)
            pathSelectionHelper = new PathSelectionHelper(callbackPathSelectionComplete, googleMap, nodes.values(), edges.values());
        pathSelectionHelper.init();
    }
    
    @Override
    public void addIdleEventListener(IdleEventListener listener) {
        idleEventListeners.add(listener);
    }
    @Override
    public void removeIdleEventListener(IdleEventListener listener) {
        idleEventListeners.removeIf(l -> l.equals(listener));
    }
    @Override
    public void removeAllIdleEventListener() {
        idleEventListeners.clear();
    }
    @Override
    public void callbackContainsLocation(GeoCoordinate coordinate, List<List<GeoCoordinate>> paths, Consumer<Boolean> callback) {
        Objects.requireNonNull(coordinate);
        Objects.requireNonNull(paths);
        Objects.requireNonNull(callback);

        geometryPoly.callbackContainsLocation(
            new LatLng(coordinate.getLatitude(), coordinate.getLongitude()),
            getPaths(paths),
            callback
        );
    }
    @Override
    public void callbackContainsLocations(HashMap<String, GeoCoordinate> coordinates, List<List<GeoCoordinate>> paths, Consumer<HashMap<String, Boolean>> callback) {
        Objects.requireNonNull(coordinates);
        Objects.requireNonNull(paths);
        Objects.requireNonNull(callback);
        
        HashMap<String, LatLng> points = new HashMap();
        coordinates.forEach((id, coordinate) -> points.put(id, new LatLng(coordinate.getLatitude(), coordinate.getLongitude())));
        
        geometryPoly.callbackContainsLocations(points, getPaths(paths), callback);
    }
    
    @Override
    public MapNode addNode(BusinessObjectViewNode viewNode) {
        GoogleMapNode node = new GoogleMapNode(viewNode, resourceFactory);
        nodes.put(viewNode, node);
        googleMap.newMarker(node);
        return node;
    }
    
    @Override
    public MapEdge addEdge(BusinessObjectViewEdge viewEdge) {
        GoogleMapEdge edge = new GoogleMapEdge(viewEdge, mem, ts);
        edges.put(viewEdge, edge);
        googleMap.newPolyline(edge);
        return edge;
    }
    
    @Override
    public void removeNode(BusinessObjectViewNode viewNode) {
        GoogleMapNode node = nodes.remove(viewNode);
        googleMap.removeMarker(node);
    }
    
    @Override
    public void removeEdge(BusinessObjectViewEdge viewEdge) {
        GoogleMapEdge edge = edges.remove(viewEdge);
        googleMap.removePolyline(edge);
    }
    
    private List<List<LatLng>> getPaths(List<List<GeoCoordinate>> paths) {
        List<List<LatLng>> latLngPaths = new ArrayList();
        paths.forEach(path -> {
            List<LatLng> latLngPath = new ArrayList();
            path.forEach(geoCoordinate -> 
                latLngPath.add(new LatLng(geoCoordinate.getLatitude(), geoCoordinate.getLongitude()))
            );
            latLngPaths.add(latLngPath);
        });
        return latLngPaths;
    }
}

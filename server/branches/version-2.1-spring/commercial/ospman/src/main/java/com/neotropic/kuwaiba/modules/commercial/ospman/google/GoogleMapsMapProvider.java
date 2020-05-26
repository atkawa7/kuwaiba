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

import com.neotropic.flow.component.googlemap.Animation;
import com.neotropic.flow.component.googlemap.DrawingManager;
import com.neotropic.flow.component.googlemap.GoogleMap;
import com.neotropic.flow.component.googlemap.GoogleMapMarker;
import com.neotropic.flow.component.googlemap.GoogleMapPolygon;
import com.neotropic.flow.component.googlemap.GoogleMapPolyline;
import com.neotropic.flow.component.googlemap.InfoWindow;
import com.neotropic.flow.component.googlemap.LatLng;
import com.neotropic.flow.component.googlemap.OverlayType;
import com.vaadin.flow.component.Component;
import java.util.List;
import java.util.Properties;
import com.neotropic.kuwaiba.modules.commercial.ospman.AbstractMapProvider;
import com.neotropic.kuwaiba.modules.commercial.ospman.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.OutsidePlantTools;
import org.neotropic.util.visual.views.ViewEventListener;
import elemental.json.Json;
import elemental.json.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GoogleMapsMapProvider extends AbstractMapProvider {
    // Tmp
    private BusinessObjectLight tmpObject;
    /**
     * Saves the mouse position in the map
     */
    private final LatLng mapMouseMoveLatLng = new LatLng();
    // Graphic
    /**
     * Google Map Component
     */
    private GoogleMap googleMap;
    /**
     * Info Window Component
     */
    private InfoWindow infoWindow;
    // Maps
    // Nodes
    private final HashMap<BusinessObjectLight, OSPNode> objNode = new HashMap();
    private final HashMap<OSPNode, GoogleMapMarker> nodeMarker = new HashMap();
    private final HashMap<GoogleMapMarker, OSPNode> markerNode = new HashMap();
    // Edges
    private final HashMap<BusinessObjectLight, OSPEdge> objEdge = new HashMap();
    private final HashMap<OSPEdge, GoogleMapPolyline> edgePolyline = new HashMap();
    private final HashMap<GoogleMapPolyline, OSPEdge> polylineEdge = new HashMap();
    // Node and Edges
    private final HashMap<BusinessObjectLight, List<BusinessObjectLight>> nodeEdges = new HashMap();
    
    private OutsidePlantTools outsidePlantTools;
    
    public GoogleMapsMapProvider() {
    }
    private List<GeoCoordinate> getGeoCoordinates(List<LatLng> latLngs) {
        List<GeoCoordinate> geoCoordinates = new ArrayList();
        latLngs.forEach(latLng -> 
            geoCoordinates.add(new GeoCoordinate(latLng.getLat(), latLng.getLng()))
        );
        return geoCoordinates;
    }
    private List<LatLng> getLatLngs(List<GeoCoordinate> geoCoordinates) {
        List<LatLng> latLngs = new ArrayList();
        geoCoordinates.forEach(geoCoordinate -> 
            latLngs.add(new LatLng(geoCoordinate.getLatitude(), geoCoordinate.getLongitude()))
        );
        return latLngs;
    }
    private void updateOutsidePlantTools() {
        outsidePlantTools.setMarkers(getMarkers());
    }
    @Override
    public void initialize(Properties properties) {
        String apiKey = (String) properties.get("apiKey"); //NOI18N
        String clientId = (String) properties.get("clientId"); //NOI18N
        GeoCoordinate center = (GeoCoordinate) properties.get("center"); //NOI18N
        int zoom = (int) properties.get("zoom"); //NOI18N
        BusinessEntityManager bem = (BusinessEntityManager) properties.get("bem"); //NOI18N
        
        this.googleMap = new GoogleMap(apiKey, clientId, "drawing"); //NOI18N
        this.googleMap.setDisableDefaultUi(true);
        this.googleMap.setCenterLat(center.getLatitude());
        this.googleMap.setCenterLng(center.getLongitude());
        this.googleMap.setZoom(zoom);
        
        this.infoWindow = new InfoWindow();
        this.googleMap.addInfoWindow(infoWindow);
        this.googleMap.addMapMouseMoveListener(event -> {
            /** 
             * Tracks the mouse move in the map 
             */
            mapMouseMoveLatLng.setLat(event.getLat());
            mapMouseMoveLatLng.setLng(event.getLng());
        });
        DrawingManager drawingManager = new DrawingManager();
        this.googleMap.newDrawingManager(drawingManager);
        
        drawingManager.addDrawingManagerPolygonCompleteListener(event -> {
            googleMap.newPolygon(new GoogleMapPolygon(event.getPaths()));
        });
        
        outsidePlantTools = new OutsidePlantTools(bem);
        this.googleMap.add(outsidePlantTools);
        
        outsidePlantTools.addToolChangeListener(event -> {
            if (OutsidePlantTools.Tool.HAND.equals(event.getTool()))
                drawingManager.setDrawingMode(null);
            if (OutsidePlantTools.Tool.MARKER.equals(event.getTool()))
                drawingManager.setDrawingMode(OverlayType.MARKER);
            if (OutsidePlantTools.Tool.POLYGON.equals(event.getTool()))
                drawingManager.setDrawingMode(OverlayType.POLYGON);
            if (OutsidePlantTools.Tool.POLYLINE.equals(event.getTool())) {
                drawingManager.setDrawingMode(null);
                outsidePlantTools.setDrawPolylineTool(
                    new DrawPolylineTool(googleMap, drawingManager, outsidePlantTools));
            }
        });
        outsidePlantTools.addNewMarkerListener(event -> {
            drawingManager.setDrawingMode(OverlayType.MARKER);
            tmpObject = event.getObject();
        });
        outsidePlantTools.addMarkerSelectedChangeListener(event -> {
            googleMap.setCenterLat(event.getOspNode().getLocation().getLatitude());
            googleMap.setCenterLng(event.getOspNode().getLocation().getLongitude());
            
            GoogleMapMarker marker = nodeMarker.get(event.getOspNode());
            marker.setAnimation(Animation.BOUNCE);
            // Temporal click listener used to stop the marker animation
            marker.addMarkerClickListener(clickEvent -> {
                marker.setAnimation(null);
                // Removes the temporal listener
                clickEvent.unregisterListener();
            });
        });
        outsidePlantTools.addPolylineCompletedListener(event -> {
            if (event.getTargetMarker() != null) {
                List<LatLng> path = event.getPath();
                path.set(0, new LatLng(
                    event.getSourceMarker().getLat(), 
                    event.getSourceMarker().getLng()
                ));
                path.set(path.size() - 1, new LatLng(
                    event.getTargetMarker().getLat(), 
                    event.getTargetMarker().getLng()));
                
                OSPNode target = markerNode.get(event.getSourceMarker());
                OSPNode source = markerNode.get(event.getTargetMarker());
                addPolyline(
                    new BusinessObjectLight(null, UUID.randomUUID().toString(),null), 
                    source.getBusinessObject(), 
                    target.getBusinessObject(), 
                    getGeoCoordinates(path), 
                    null
                );
            }
            outsidePlantTools.setDrawPolylineTool(
                new DrawPolylineTool(googleMap, drawingManager, outsidePlantTools)
            );
        });
        drawingManager.addDrawingManagerMarkerCompleteListener(event -> {
            addMarker(tmpObject, 
                new GeoCoordinate(event.getLat(), event.getLng()), "marker.png");
            drawingManager.setDrawingMode(null);
        });
    }
    @Override
    public void reload(Properties properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addMarker(BusinessObjectLight businessObject, GeoCoordinate position, String iconUrl) {
        OSPNode ospNode = new OSPNode(businessObject, position);
        
        GoogleMapMarker googleMapMarker = new GoogleMapMarker(position.getLatitude(), position.getLongitude());
        googleMap.newMarker(googleMapMarker);
        
        JsonObject label = Json.createObject();
////        label.put("color", "#305F72"); //NOI18N
        label.put("text", businessObject.getName()); //NOI18N

        googleMapMarker.setLabel(label);

        JsonObject icon = Json.createObject();
        JsonObject labelOrigin = Json.createObject();
        labelOrigin.put("x", 20); //NOI18N
        labelOrigin.put("y", 40); //NOI18N
        icon.put("url", iconUrl); //NOI18N
        icon.put("labelOrigin", labelOrigin); //NOI18N

        googleMapMarker.setIcon(icon);
        
        googleMapMarker.addMarkerRightClickListener(event -> {
            infoWindow.removeAll();
            MarkerTools markerTools = new MarkerTools(ospNode, googleMapMarker, infoWindow);
            markerTools.addDeleteMarkerEventListener(deleteMarkerEvent -> 
                removeMarker(ospNode.getBusinessObject())
            );
            infoWindow.add(markerTools);
            infoWindow.open(googleMap, googleMapMarker);
        });
        nodeMarker.put(ospNode, googleMapMarker);
        markerNode.put(googleMapMarker, ospNode);
        objNode.put(businessObject, ospNode);
        updateOutsidePlantTools();
    }

    @Override
    public void removeMarker(BusinessObjectLight businessObject) {
        OSPNode node = objNode.get(businessObject);
        GoogleMapMarker marker = nodeMarker.get(node);
        
        objNode.remove(businessObject);
        nodeMarker.remove(node);
        markerNode.remove(marker);
        if (nodeEdges.get(businessObject) != null) {
            while (nodeEdges.get(businessObject).size() > 0)
                removePolyline(nodeEdges.get(businessObject).get(0));
        }
        nodeEdges.remove(businessObject);
        googleMap.removeMarker(marker);
        updateOutsidePlantTools();
    }
    


    @Override
    public void addPolyline(BusinessObjectLight businessObject, BusinessObjectLight sourceObject, BusinessObjectLight targetObject, List<GeoCoordinate> controlPoints, Properties properties) {
        GoogleMapPolyline googleMapPolyline = new GoogleMapPolyline();
        googleMapPolyline.setPath(getLatLngs(controlPoints));
        googleMap.newPolyline(googleMapPolyline);
        
        OSPEdge polyline = new OSPEdge(businessObject, sourceObject, targetObject, controlPoints);
        
        googleMapPolyline.addPolylineRightClickListener(event -> {
            infoWindow.removeAll();
            PolylineTools polylineTools = new PolylineTools(polyline, googleMapPolyline, infoWindow);
            polylineTools.addDeletePolylineEventListener(deletePolylineEvent -> {
                infoWindow.close();
                removePolyline(businessObject);
            });
            infoWindow.add(polylineTools);
            infoWindow.setPosition(mapMouseMoveLatLng);
            infoWindow.open(googleMap);
        });
        objEdge.put(businessObject, polyline);
        edgePolyline.put(polyline, googleMapPolyline);
        polylineEdge.put(googleMapPolyline, polyline);
        
        if (nodeEdges.get(sourceObject) == null)
            nodeEdges.put(sourceObject, new ArrayList());
        nodeEdges.get(sourceObject).add(businessObject);
        
        if (nodeEdges.get(targetObject) == null)
            nodeEdges.put(targetObject, new ArrayList());
        nodeEdges.get(targetObject).add(businessObject);
        updateOutsidePlantTools();
    }

    @Override
    public void removePolyline(BusinessObjectLight businessObject) {
        OSPEdge edge = objEdge.get(businessObject);
        GoogleMapPolyline polyline = edgePolyline.get(edge);
        
        objEdge.remove(businessObject);
        edgePolyline.remove(edge);
        polylineEdge.remove(polyline);
        nodeEdges.get(edge.getSourceObject()).remove(businessObject);
        nodeEdges.get(edge.getTargetObject()).remove(businessObject);
        
        googleMap.removePolyline(polyline);
        updateOutsidePlantTools();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<OSPNode> getMarkers() {
        return Arrays.asList(nodeMarker.keySet().toArray(new OSPNode[0])) ;
    }

    @Override
    public List<OSPEdge> getPolylines() {
        return Arrays.asList(edgePolyline.keySet().toArray(new OSPEdge[0]));
    }

    @Override
    public int getZoom() {
        return (int) this.googleMap.getZoom();
    }

    @Override
    public GeoCoordinate getCenter() {
        return new GeoCoordinate(
            this.googleMap.getCenterLat(), 
            this.googleMap.getCenterLng()
        );
    }

    @Override
    public Component getComponent() {
        return this.googleMap;
    }
    
    @Override
    public void removeListeners() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addMarkerClickListener(ViewEventListener ev) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addMarkerRightClickListener(ViewEventListener ev) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPolylineClickListener(ViewEventListener ev) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPolylineRightClickListener(ViewEventListener ev) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
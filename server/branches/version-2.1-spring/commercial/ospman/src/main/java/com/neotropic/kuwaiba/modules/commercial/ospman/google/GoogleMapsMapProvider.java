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

import com.neotropic.flow.component.googlemap.DrawingManager;
import com.neotropic.flow.component.googlemap.GoogleMap;
import com.neotropic.flow.component.googlemap.GoogleMapMarker;
import com.neotropic.flow.component.googlemap.GoogleMapPolygon;
import com.neotropic.flow.component.googlemap.GoogleMapPolyline;
import com.neotropic.flow.component.googlemap.OverlayType;
import com.vaadin.flow.component.Component;
import java.util.List;
import java.util.Properties;
import com.neotropic.kuwaiba.modules.commercial.ospman.AbstractMapProvider;
import com.neotropic.kuwaiba.modules.commercial.ospman.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.OutsidePlantTools;
import elemental.json.Json;
import elemental.json.JsonObject;
import java.util.ArrayList;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewEventListener;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GoogleMapsMapProvider extends AbstractMapProvider {
    private GoogleMap googleMap;
    private List<OSPNode> markers;
    private List<OSPEdge> polylines;
    private BusinessObjectLight tmpObject;
    
    public GoogleMapsMapProvider() {
        markers = new ArrayList();
        polylines = new ArrayList();
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
        
        DrawingManager drawingManager = new DrawingManager();
        googleMap.newDrawingManager(drawingManager);
//        drawingManager.addDrawingManagerMarkerCompleteListener(event -> {
//            GoogleMapMarker googleMapMarker = new GoogleMapMarker(event.getLat(), event.getLng());
//            googleMap.newMarker(googleMapMarker);
//            
//            JsonObject icon = Json.createObject();
//            JsonObject labelOrigin = Json.createObject();
//            labelOrigin.put("x", 20); //NOI18N
//            labelOrigin.put("y", 40); //NOI18N
//            icon.put("url", "marker.png"); //NOI18N
//            icon.put("labelOrigin", labelOrigin); //NOI18N
//            
//            googleMapMarker.setIcon(icon);
//        });
        drawingManager.addDrawingManagerPolygonCompleteListener(event -> {
            googleMap.newPolygon(new GoogleMapPolygon(event.getPaths()));
        });
        drawingManager.addDrawingManagerPolylineCompleteListener(event -> {
            GoogleMapPolyline googleMapPolyline = new GoogleMapPolyline();
            googleMapPolyline.setPath(event.getPath());
            googleMap.newPolyline(googleMapPolyline);
        });
        
        OutsidePlantTools outsidePlantTools = new OutsidePlantTools(bem, markers);
        this.googleMap.getElement().appendChild(outsidePlantTools.getElement());
        outsidePlantTools.addToolChangeListener(event -> {
            if (OutsidePlantTools.Tool.HAND.equals(event.getTool()))
                drawingManager.setDrawingMode(null);
            if (OutsidePlantTools.Tool.MARKER.equals(event.getTool()))
                drawingManager.setDrawingMode(OverlayType.MARKER);
            if (OutsidePlantTools.Tool.POLYGON.equals(event.getTool()))
                drawingManager.setDrawingMode(OverlayType.POLYGON);
            if (OutsidePlantTools.Tool.POLYLINE.equals(event.getTool()))
                drawingManager.setDrawingMode(OverlayType.POLYLINE);
        });
        outsidePlantTools.addNewMarkerListener(event -> {
            drawingManager.setDrawingMode(OverlayType.MARKER);
            tmpObject = event.getObject();
        });
        outsidePlantTools.addCenterChangeListener(event -> {
            googleMap.setCenterLat(event.getGeoCoordinate().getLatitude());
            googleMap.setCenterLng(event.getGeoCoordinate().getLongitude());
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
        markers.add(ospNode);
        
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
    }

    @Override
    public void removeMarker(BusinessObjectLight businessObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPolyline(BusinessObjectLight businessObject, BusinessObjectLight sourceObject, BusinessObjectLight targetObject, List<GeoCoordinate> controlPoints, Properties properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removePolyline(BusinessObjectLight businessObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<OSPNode> getMarkers() {
        return markers;
    }

    @Override
    public List<OSPEdge> getPolylines() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
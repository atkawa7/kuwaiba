/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kuwaiba.custom;

import com.google.common.eventbus.EventBus;
import com.vaadin.data.Property;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.VaadinSession;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.connection.Connection;
import org.kuwaiba.custom.events.ControlPointMarkerClickListener;
import org.kuwaiba.custom.events.ControlPointMarkerDragListener;
import org.kuwaiba.custom.events.NodeMarkerClickListener;
import org.kuwaiba.custom.events.NodeMarkerDragListener;
import org.kuwaiba.custom.map.buttons.ConnectionButton;
import org.kuwaiba.custom.map.buttons.DrawPolygonButton;
import org.kuwaiba.custom.map.buttons.MarkerButton;
import org.kuwaiba.custom.map.buttons.MeasureButton;
import org.kuwaiba.custom.map.buttons.SaveButton;
import org.kuwaiba.custom.map.buttons.UploadMapButton;
import org.kuwaiba.custom.overlays.ControlPointMarker;
import org.kuwaiba.custom.overlays.NodeMarker;
import org.kuwaiba.custom.polyline.events.EdgeClick;
import org.kuwaiba.custom.polyline.events.EdgeDblClick;
import org.kuwaiba.polygon.MapPolygon;
import org.kuwaiba.polygon.events.PolygonMapClickListener;
import org.kuwaiba.polygon.events.PolygonMarkerClickListener;
import org.kuwaiba.polygon.events.PolygonRightClickListenerImpl;
// remove the action change connection color
// ck remove the action add break point
// ck remove the delete connection action
// ck remove break point
// ck download and upload map
// storage the color
// TODO:storage the zoom of the map
/**
 * GIS View section accept drop and select actions
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@SuppressWarnings("serial")
public class GISView extends CustomComponent {
    private final String apiKey = "";
    private final GoogleMap googleMap = new GoogleMap(apiKey, null, "english");
    private double centerLat = 2.4448;
    private double centerLon = -76.6147;  
    // mark
    // connection
    // measure
    private List<Connection> edges;
    private List<MapPolygon> mapPolygons;
    
    public GISView(final EventBus eventBus, VaadinSession session) {   
        edges = new ArrayList();
        mapPolygons = new ArrayList();
        // initialize status of buttons
        session.setAttribute(MarkerButton.NAME, true);
        session.setAttribute(ConnectionButton.NAME, false);
        session.setAttribute(MeasureButton.NAME, false);
        
        googleMap.setCenter(new LatLon(centerLat, centerLon));
        googleMap.setZoom(17);
        googleMap.setWidth("1000px");
        googleMap.setHeight("500px");
        
        googleMap.addMarkerClickListener(new NodeMarkerClickListener(edges, googleMap, session, eventBus));
        googleMap.addMarkerClickListener(new ControlPointMarkerClickListener(googleMap, session));
        
        ControlPointMarkerDragListener controlPointMarkerDragListener = new ControlPointMarkerDragListener(googleMap, session);

        googleMap.addMarkerDragListener(controlPointMarkerDragListener);
        googleMap.addMarkerDragListener(new NodeMarkerDragListener(controlPointMarkerDragListener, edges));
        
        googleMap.addPolylineClickListener(new EdgeClick());
        googleMap.addPolylineDblClickListener(new EdgeDblClick());
        /*
        googleMap.addPolylineRightClickListener(new PolylineRightClickListener() {

            @Override
            public void polylineRightClicked(GoogleMapPolyline clickedPolyline) {
                Notification.show("Right click on polyline", Notification.Type.ERROR_MESSAGE);
            }
        });
        */
        googleMap.addPolygonRightClickListener(new PolygonRightClickListenerImpl(googleMap));
        /*
        googleMap.addPolygonClickListener(new PolygonClickListener() {

            @Override
            public void polygonClicked(GoogleMapPolygon clickedPolygon) {
                Notification.show("Left click on polygon", Notification.Type.ERROR_MESSAGE);
            }
        });
        
        googleMap.addPolygonDblClickListener(new PolygonDblClickListener() {

            @Override
            public void polygonDblClicked(GoogleMapPolygon clickedPolygon) {
                Notification.show("double click on polygon", Notification.Type.ERROR_MESSAGE);
            }
        });
        
        googleMap.addPolygonRightClickListener(new PolygonRightClickListener() {

            @Override
            public void polygonRightClicked(GoogleMapPolygon clickedPolygon) {
                Notification.show("right click on polygon", Notification.Type.ERROR_MESSAGE);
            }
        });
        */
        
        DragAndDropWrapper wrapper = new DragAndDropWrapper(googleMap);
        
        wrapper.setSizeFull();        
        wrapper.setDropHandler(new DropHandler() {

            @Override
            public void drop(DragAndDropEvent event) {
                String strSource = event.getTransferable().getData(event.getTransferable().getDataFlavors().toArray()[0].toString()).toString();                
                //TODo: change this for more complex objects
                for (GoogleMapMarker marker : googleMap.getMarkers()) {
                    if (marker.getCaption().equals(strSource)) {
                        Notification.show("The object is in the map");
                        return;
                    }
                }
                NodeMarker nodeMarker = new NodeMarker(googleMap, strSource, googleMap.getCenter(), true);
                googleMap.addMarker(nodeMarker);
                googleMap.setInfoWindowContents(nodeMarker.getInfoWindow(), nodeMarker.getDeleteButton());
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }
        });
        HorizontalLayout mapSearchLayout = new HorizontalLayout();
        
        TextField txtMapSearch = new TextField();
        mapSearchLayout.addComponent(txtMapSearch);
        
        Button btnMapSearch = new Button("Search");
        mapSearchLayout.addComponent(btnMapSearch);
        
        btnMapSearch.addClickListener(new MapSearch(txtMapSearch));
                
        Label lblLayers = new Label("Layers");
        mapSearchLayout.addComponent(lblLayers);
        
        NativeSelect nativeSelectLayers = new NativeSelect();
        nativeSelectLayers.addItems("Departamentos", "Municipios");
        
        Property.ValueChangeListener listener = new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() == null) {
                    valueChangeEmpty();
                    return;
                }
                
                List<String> municipios = new ArrayList();
                municipios.add("Santiago de Cali");
                municipios.add("Popayán");
                municipios.add("Pasto");
                
                List<String> departamentos = new ArrayList();
                departamentos.add("Valle del Cauca");
                departamentos.add("Cauca");
                departamentos.add("Nariño");
                
                List<NodeMarker> nodes = new ArrayList();
                for (GoogleMapMarker node : googleMap.getMarkers())
                    if (node instanceof NodeMarker)
                        nodes.add((NodeMarker) node);
                
                if (event.getProperty().getValue().equals("Municipios")) {
                    valueChangeEmpty();
                    
                    for (GoogleMapMarker googleMapMarker : nodes) {
                        if (googleMapMarker instanceof NodeMarker) {
                        NodeMarker theNode = (NodeMarker) googleMapMarker;
                                
                        if (departamentos.contains(theNode.getCaption())) {
                            if (googleMap.getMarkers().contains(theNode)) {
                                googleMap.removeMarker(googleMapMarker);
                                        
                                for (Connection conn : theNode.getConnections()) {
                                    googleMap.removePolyline(conn.getEdge());
                                    for (ControlPointMarker controlPoint : conn.getControlPoints())
                                        if (googleMap.getMarkers().contains(controlPoint))
                                            googleMap.removeMarker(controlPoint);
                                    }
                                }
                            }
                        }
                    }
                }
                if (event.getProperty().getValue().equals("Departamentos")) {
                    valueChangeEmpty();
                    
                    for (GoogleMapMarker googleMapMarker : nodes) {
                        if (googleMapMarker instanceof NodeMarker) {
                            NodeMarker theNode = (NodeMarker) googleMapMarker;
                                
                            if (municipios.contains(theNode.getCaption())) {
                                if (googleMap.getMarkers().contains(theNode)) {
                                        googleMap.removeMarker(googleMapMarker);
                                        
                                    for (Connection conn : theNode.getConnections()) {
                                        googleMap.removePolyline(conn.getEdge());
                                        for (ControlPointMarker controlPoint : conn.getControlPoints())
                                            if (googleMap.getMarkers().contains(controlPoint))
                                                googleMap.removeMarker(controlPoint);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            private void valueChangeEmpty() {
                List<GoogleMapMarker> markers = new ArrayList();
                for (GoogleMapMarker marker : googleMap.getMarkers())
                    markers.add(marker);
                    
                for (Connection edge : edges) {
                    if (!googleMap.getMarkers().contains(edge.getSource()))
                        googleMap.addMarker(edge.getSource());
                    if (!googleMap.getMarkers().contains(edge.getTarget()))
                        googleMap.addMarker(edge.getTarget());
                        
                    for (GoogleMapMarker controlPoint : edge.getControlPoints())
                        if (!googleMap.getMarkers().contains(controlPoint))
                            googleMap.addMarker(controlPoint);
                        
                    googleMap.removePolyline(edge.getEdge());
                    googleMap.addPolyline(edge.getEdge());
                }
            }
        };
        nativeSelectLayers.addListener(listener);
        
        mapSearchLayout.addComponent(nativeSelectLayers);   
        // Polygons
        Label lblPolygons = new Label("Polygons");
        mapSearchLayout.addComponent(lblPolygons);
        
        NativeSelect nativeSelectPolygonsLayers = new NativeSelect();
        mapSearchLayout.addComponent(nativeSelectPolygonsLayers);
        /**/
        PolygonMapClickListener polygonMapClickListener = new PolygonMapClickListener(session, googleMap, nativeSelectPolygonsLayers, mapPolygons);
        googleMap.addMapClickListener(polygonMapClickListener);
        googleMap.addMarkerClickListener(new PolygonMarkerClickListener(session, polygonMapClickListener));
        /**/        
        /*
        Button btnEditPolygon = new Button("Edit polygon");
        mapSearchLayout.addComponent(btnEditPolygon);
        
        btnEditPolygon.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                MapPolygon mapPolygon = null;
                // google map polygon id
                Long id = Long.valueOf((String) nativeSelectPolygonsLayers.getValue());
                
                for (MapPolygon mapPolygon_ : mapPolygons) {
                    if (mapPolygon_.getPolygonId() == id) {
                        mapPolygon = mapPolygon_;
                        break;
                    }
                }
                GoogleMapPolygon polygon = mapPolygon.getPolygon();
                
//                googleMap.setCenter(mapPolygon.getPolygon().getCoordinates().get(0));
                
                List<PolygonMarker> vertices = new ArrayList();
                
                List<LatLon> coordinates = polygon.getCoordinates();                
                for (int i = 0; i < coordinates.size(); i += 1) {
                    PolygonMarker pointA = new PolygonMarker();
                    pointA.setPosition(coordinates.get(i));
                    
                    PolygonMarker pointB = new PolygonMarker();
                    
                    if (i != coordinates.size() - 1)
                        pointB.setPosition(coordinates.get(i + 1));
                    else
                        pointB.setPosition(coordinates.get(0));
                        
                    PolygonMarker middlePoint = new PolygonMarker();
                    middlePoint.setPosition(ConnectionUtils.midPoint(pointA.getPosition(), pointB.getPosition()));
                    middlePoint.setIconUrl("VAADIN/img/polygonMiddleControlPoint.png");
                    
                    googleMap.addMarker(pointA);
                    vertices.add(pointA);
                    
                    googleMap.addMarker(middlePoint);
                    vertices.add(middlePoint);
                }
                vertices.add(vertices.get(0));
                
                GoogleMapPolyline polyline = new GoogleMapPolyline();
                polyline.setStrokeColor("orange");
                polyline.setStrokeOpacity(1);
                polyline.setStrokeWeight(2);
                
                for (int i = 0; i < vertices.size(); i += 1) {
                    if (i % 2 == 0) {
                        PolygonMarker vertex = vertices.get(i);
                        polyline.getCoordinates().add(vertex.getPosition());
                    }
                }                    
                googleMap.addPolyline(polyline);
                
                mapPolygon.setPolygon(polygon);
                mapPolygon.setPolyline(polyline);
                mapPolygon.setVertices(vertices);
                
                if (mapPolygon.getDragListener() == null) {
                    mapPolygon.setDragListener(new PolygonMarkerDragListener(mapPolygon));
                    googleMap.addMarkerDragListener(mapPolygon.getDragListener());
                }
                if (mapPolygon.getClickListener() == null) {
                    mapPolygon.setClickListener(new VertexMarkerClickListener(mapPolygon));
                    // double click dummy vertex disable edit polygon
                    // double click vertex so delete vertex
                    googleMap.addMarkerClickListener(mapPolygon.getClickListener());
                }
            }
        });
                */
        /*
        Button btnDeletePolygon = new Button("Delete polygon");
        mapSearchLayout.addComponent(btnDeletePolygon);
        
        btnDeletePolygon.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                MapPolygon mapPolygon = null;
                // google map polygon id
                Long id = Long.valueOf((String) nativeSelectPolygonsLayers.getValue());
                
                for (MapPolygon mapPolygon_ : mapPolygons) {
                    if (mapPolygon_.getPolygonId() == id) {
                        mapPolygon = mapPolygon_;
                        break;
                    }
                }
                if (mapPolygon.getVertices() != null) {
                    for (PolygonMarker vertex : mapPolygon.getVertices())
                        if (googleMap.getMarkers().contains(vertex))
                            googleMap.removeMarker(vertex);
                }
                                    
                googleMap.removePolygonOverlay(mapPolygon.getPolygon());
                if (mapPolygon.getPolyline() != null)
                    googleMap.removePolyline(mapPolygon.getPolyline());
                if (mapPolygon.getDragListener() != null)
                    googleMap.removeMarkerDragListener(mapPolygon.getDragListener());
                if (mapPolygon.getClickListener() != null)
                    googleMap.removeMarkerClickListener(mapPolygon.getClickListener());
                
                nativeSelectPolygonsLayers.removeItem((String) nativeSelectPolygonsLayers.getValue());
                mapPolygons.remove(mapPolygon);
            }
        });
                */
        
        NativeSelect nativeSelectPolygonColor = new NativeSelect();
        nativeSelectPolygonColor.addItem("red");
        nativeSelectPolygonColor.addItem("yellow");
        nativeSelectPolygonColor.addItem("green");
        
        Property.ValueChangeListener polygonListener = new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() == null) {
                    valueChangeEmpty();
                    return;
                }
                MapPolygon mapPolygon = null;
                
                Long id = Long.valueOf((String) event.getProperty().getValue());
                
                for (MapPolygon mapPolygon_ : mapPolygons) {
                    if (mapPolygon_.getPolygonId() == id) {
                        mapPolygon = mapPolygon_;
                        break;
                    }
                }
                googleMap.setCenter(mapPolygon.getPolygon().getCoordinates().get(0));                
            }
            
            private void valueChangeEmpty() {
            }
        };
        nativeSelectPolygonsLayers.addListener(polygonListener);
        
        mapSearchLayout.addComponent(nativeSelectPolygonColor);
        
        Button btnEditPolygonColor = new Button("Change polygon color");
        mapSearchLayout.addComponent(btnEditPolygonColor);
        
        btnEditPolygonColor.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                MapPolygon mapPolygon = null;
                // google map polygon id
                Long id = Long.valueOf((String) nativeSelectPolygonsLayers.getValue());
                String color = (String) nativeSelectPolygonColor.getValue();
                if (color == null)
                    color = "blue";
                
                for (MapPolygon mapPolygon_ : mapPolygons) {
                    if (mapPolygon_.getPolygonId() == id) {
                        mapPolygon = mapPolygon_;
                        break;
                    }
                }
                GoogleMapPolygon oldPolygon = mapPolygon.getPolygon();
                googleMap.removePolygonOverlay(mapPolygon.getPolygon());
                
                GoogleMapPolygon newPolygon = new GoogleMapPolygon();
                newPolygon.setCoordinates(oldPolygon.getCoordinates());
                newPolygon.setFillColor(color);
                newPolygon.setFillOpacity(oldPolygon.getFillOpacity());
                newPolygon.setStrokeColor(color);
                newPolygon.setStrokeOpacity(oldPolygon.getStrokeOpacity());
                newPolygon.setStrokeWeight(oldPolygon.getStrokeWeight());
                
//                mapPolygon.setPolygon(newPolygon);
                googleMap.addPolygonOverlay(newPolygon);
            }
        });
        // end Polygons        
        VerticalLayout mapLayout = new VerticalLayout();
        mapLayout.setSizeFull();
        
        mapLayout.addComponent(mapSearchLayout);
        
        mapLayout.addComponent(wrapper);                
        
        HorizontalLayout hLayout = new HorizontalLayout();
        
        hLayout.setSizeFull();
        hLayout.addComponent(new MarkerButton(session));
        hLayout.addComponent(new ConnectionButton(session));
        hLayout.addComponent(new MeasureButton(session));
        hLayout.addComponent(new SaveButton(edges, mapPolygons, googleMap));
        hLayout.addComponent(new UploadMapButton(googleMap, edges, mapPolygons, nativeSelectPolygonsLayers));
        hLayout.addComponent(new DrawPolygonButton(session));
                
        mapLayout.addComponent(hLayout);
        
        setCompositionRoot(mapLayout);
    } 
    
    private class MapSearch implements Button.ClickListener {
        private TextField txtMapSearch;        

        public MapSearch(TextField txtMapSearch) {
            this.txtMapSearch = txtMapSearch;
        }

        @Override
        public void buttonClick(Button.ClickEvent event) {
            for (GoogleMapMarker marker : googleMap.getMarkers())
                if (marker.getCaption().equals(txtMapSearch.getValue()))
                    googleMap.setCenter(marker.getPosition());
        }
    }
}

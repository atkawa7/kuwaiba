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
package org.kuwaiba.custom.events;

import com.google.common.eventbus.EventBus;
import com.vaadin.server.VaadinSession;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.connection.Connection;
import org.kuwaiba.connection.ConnectionUtils;
import org.kuwaiba.connection.Measure;
import org.kuwaiba.custom.map.buttons.ConnectionButton;
import org.kuwaiba.custom.map.buttons.MarkerButton;
import org.kuwaiba.custom.map.buttons.MeasureButton;
import org.kuwaiba.custom.overlays.ControlPointMarker;
import org.kuwaiba.custom.overlays.NodeMarker;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class NodeMarkerClickListener implements MarkerClickListener {
    private final VaadinSession session;
    private final EventBus eventBus;
    private List<Connection> edges; // all edges
    private GoogleMap googleMap;
    private int i = 0;
    private Measure measure;
    
    public NodeMarkerClickListener(List<Connection> edges, GoogleMap googleMap, VaadinSession session, final EventBus eventBus) {
        this.measure = new Measure(googleMap);
        this.session = session;
        this.googleMap = googleMap;
        this.eventBus = eventBus;
        
        this.edges = edges;
        int i = 0;
    }

    @Override
    public void markerClicked(GoogleMapMarker clickedMarker) {
        if (clickedMarker instanceof NodeMarker) {
            if ((Boolean) session.getAttribute(MarkerButton.NAME))
                googleMap.openInfoWindow(((NodeMarker) clickedMarker).getInfoWindow());
            
            if ((Boolean) session.getAttribute(ConnectionButton.NAME)) {
                
                if (i == 0) {
                    i = 1;
                    edges.add(new Connection(googleMap, edges));
                }
                
                if (i == 1) {
                    int edgesLength = edges.size() - 1;
                    Connection conn = edges.get(edgesLength);
                    conn.setStartAndEndControlPoints((NodeMarker) clickedMarker);
                    // fix source and target
                    if (conn.getI() == 0) {
                        GoogleMapPolyline connection = new GoogleMapPolyline(new ArrayList(), "green", 1, 5);
                        conn.setConnection(connection);

                        for (ControlPointMarker controlPoint : conn.getControlPoints()) {
                            if (!googleMap.getMarkers().contains(controlPoint)) {
                                googleMap.addMarker(controlPoint);

                                Button btnConnColor = new Button("Connection Color");
                                btnConnColor.addClickListener(new Button.ClickListener() {

                                    @Override
                                    public void buttonClick(Button.ClickEvent event) {
                                        GoogleMapPolyline polyline = new GoogleMapPolyline(new ArrayList(), "yellow", 1, 5);

                                        for (ControlPointMarker controlPoint : conn.getControlPoints())
                                            polyline.getCoordinates().add(controlPoint.getPosition());

                                        conn.getMap().removePolyline(conn.getConnection());
                                        conn.setConnection(polyline);
                                        conn.getMap().addPolyline(polyline);
                                    }
                                });

                                Button btnAddBreakPoint = new Button("Add control point");

                                btnAddBreakPoint.addClickListener(new Button.ClickListener() {

                                    @Override
                                    public void buttonClick(Button.ClickEvent event) {
                                        //TODO: Review don't work
                                        googleMap.closeInfoWindow(conn.getControlPoints().get(0).getInfoWindows());

                                        ControlPointMarker controlPoint_ = new ControlPointMarker(conn.getSource().getPosition(), edges);
                                        controlPoint_.setDraggable(true);
                                        controlPoint_.setIconUrl("VAADIN/img/dragControlPoint.png");
                                        //TODO: Add Control Point between two cotrols points
                                        googleMap.addMarker(controlPoint_);
                                        conn.getControlPoints().add(1, controlPoint_); 
                                        controlPoint_.setConnection(conn);

                                        VerticalLayout verticalLayout = new VerticalLayout();
                                        verticalLayout.addComponent(controlPoint_.getBtnDeleteControlPoint());
                                        verticalLayout.addComponent(controlPoint_.getBtnDeleteConnection());

                                        googleMap.setInfoWindowContents(controlPoint_.getInfoWindows(), verticalLayout);
                                    }
                                } );

                                //Button btnDelete = new Button("Delete Connection");
                                VerticalLayout verticalLayout = new VerticalLayout();
                                verticalLayout.addComponent(btnConnColor);
                                verticalLayout.addComponent(btnAddBreakPoint);
                                //verticalLayout.addComponent(btnDelete);

                                googleMap.setInfoWindowContents(controlPoint.getInfoWindows(), verticalLayout);

                                connection.getCoordinates().add(controlPoint.getPosition());
                            }
                        }
                        // Central Control Point
                        LatLon position1 = conn.getControlPoints().get(0).getPosition();
                        LatLon position2 = conn.getControlPoints().get(1).getPosition();
                        LatLon position3 = ConnectionUtils.midPoint(position1, position2);
                        ControlPointMarker centralControlPoint = new ControlPointMarker(position3, edges);
                        centralControlPoint.setDraggable(true);
                        centralControlPoint.setIconUrl("VAADIN/img/dragControlPoint.png");
                        googleMap.addMarker(centralControlPoint);
                        conn.getControlPoints().add(1, centralControlPoint);
                        centralControlPoint.setConnection(conn);

                        VerticalLayout verticalLayout = new VerticalLayout();
                        verticalLayout.addComponent(centralControlPoint.getBtnDeleteControlPoint());
                        verticalLayout.addComponent(centralControlPoint.getBtnDeleteConnection());

                        googleMap.setInfoWindowContents(centralControlPoint.getInfoWindows(), verticalLayout);
                        //
                        googleMap.addPolyline(connection);
                        
                        i = 0;
                    }
                }
            }
            
            if ((Boolean) session.getAttribute(MeasureButton.NAME)) {
                if (measure.getI() == 0) {
                    measure.setSource((NodeMarker) clickedMarker);
                    measure.setI(1);
                    return;
                }
                if (measure.getI() == 1) {
                    NodeMarker source = measure.getSource();
                    
                    NodeMarker target = (NodeMarker) clickedMarker;
                    measure.setTarget(target);
                    
                    Connection connection = null;
                    
                    for (Connection edge : target.getConnections()) {
                        if (source.equals(edge.getSource()) || source.equals(edge.getTarget())) {
                            connection = edge;
                            break;
                        }
                    }                    
                    googleMap.removePolyline(connection.getConnection());
                    List<LatLon> coordinates = connection.getConnection().getCoordinates();
                    GoogleMapPolyline polyline = new GoogleMapPolyline(coordinates, "gray", 1, 5);
                    connection.setConnection(polyline);
                    googleMap.addPolyline(connection.getConnection());
                    
                    double distance = measure.calculateDistance(connection);
                    Notification.show("Distance between Source and Target " + distance);                    
                }
            }
            else {
                measure.endMeasure();
            }
            eventBus.post(clickedMarker);
        }
    }
}


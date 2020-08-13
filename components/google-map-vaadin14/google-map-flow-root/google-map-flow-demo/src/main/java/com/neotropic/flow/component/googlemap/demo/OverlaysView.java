/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package com.neotropic.flow.component.googlemap.demo;

import com.neotropic.flow.component.googlemap.DrawingManager;
import com.neotropic.flow.component.googlemap.GoogleMap;
import com.neotropic.flow.component.googlemap.LatLng;
import com.neotropic.flow.component.googlemap.OverlayType;
import com.neotropic.flow.component.googlemap.OverlayView;
import com.neotropic.flow.component.googlemap.Point;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphCell;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.shared.Registration;
import elemental.json.Json;
import elemental.json.JsonArray;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Push
@Route(value="overlay")
public class OverlaysView extends VerticalLayout {
    @Value("${google.maps.api-key}")
    private String apiKey;
    @Value("${google.maps.libraries}")
    private String libraries;
    
    private Registration registrationRectangle;
    private Registration registrationMarker;
    private Registration registrationPolyline;
    private OverlayView overlayView;
    private MxGraph mxGraph;
    
    private double mxGraphWidth = -1;    
    private boolean mxGraphLoaded = false;
    private Consumer<Double> consumer;
    private double startWidth = -1;
    
    private boolean drawPolyline;
    private MxGraphCell vertexSource;
    private MxGraphCell vertexTarget;
    private List<Point> path;
    private double scale;
    private Point southWest;
    private Point northEast;
    
    @Override
    public void onAttach(AttachEvent attachEvent) {
        setSizeFull();
        setMargin(false);
        setPadding(false);
        
        GoogleMap gMap = new GoogleMap(apiKey, null, libraries);
        gMap.setDisableDefaultUi(true);
        
        DrawingManager drawingManager = new DrawingManager();
        gMap.newDrawingManager(drawingManager);
        
        Tabs tabs = new Tabs();
        tabs.getStyle().set("position", "absolute"); //NOI18N
        tabs.getStyle().set("z-index", "5"); //NOI18N
        tabs.getStyle().set("top", "10px"); //NOI18N
        tabs.getStyle().set("left", "25%"); //NOI18N
        tabs.getStyle().set("background-color", "#fff"); //NOI18N
        
        Tab tabMxGraphOverlay = new Tab(new Icon(VaadinIcon.SQUARE_SHADOW));
        Tab tabHand = new Tab(new Icon(VaadinIcon.HAND));
        Tab tabVertex = new Tab(new Icon(VaadinIcon.MAP_MARKER));
        Tab tabEdge = new Tab(new Icon(VaadinIcon.PLUG));
        Tab tabGoogleMap = new Tab(new Icon(VaadinIcon.GLOBE));
        
        tabs.add(tabHand, tabMxGraphOverlay, tabVertex, tabEdge, tabGoogleMap);
        
        tabs.addSelectedChangeListener(event -> {
            if (tabHand.equals(event.getSelectedTab())) {
                unregisterListeners();
                drawingManager.setDrawingMode(null);                
            } else if (tabMxGraphOverlay.equals(event.getSelectedTab())) {
                unregisterListeners();
                drawingManager.setDrawingMode(null);
                drawingManager.setDrawingMode(OverlayType.RECTANGLE);
                
                registrationRectangle = drawingManager.addDrawingManagerRectangleCompleteListener(theEvent -> {
                    mxGraph = new MxGraph();   
                    mxGraph.getElement().getStyle().set("outline", "1px solid  black");
                    mxGraph.setFullSize();
                    mxGraph.setOverflow("");
                    mxGraph.addMouseOverEvent(mouseOverEvent -> {
                        //System.out.println(">>> mxGraph mouse over event");
                    });
                    
                    mxGraph.addCellSelectedListener(cellSelected -> {
                        Iterator<Component> components = cellSelected.getSource().getChildren().iterator();
                        while (components.hasNext()) {
                            Component component = components.next();
                            if (component instanceof MxGraphCell) {
                                MxGraphCell cell = (MxGraphCell) component;
                                if (cellSelected.getCellId().equals(cell.getUuid())) {
                                    if (drawPolyline) {
                                        if (vertexSource == null) {
                                            vertexSource = cell;
                                            
                                            unregisterListeners();
                                            drawingManager.setDrawingMode(null);
                                            drawingManager.setDrawingMode(OverlayType.POLYLINE);

                                            registrationPolyline = drawingManager.addDrawingManagerPolylineCompleteListener(polylineComplete -> {
                                                List<Point> points = new ArrayList();
                                                setPoints(points, polylineComplete.getPath(), () -> {
                                                    overlayView.fromLatLngToDivPixel(overlayView.getBounds().getSouthWest(), sw -> {
                                                        overlayView.fromLatLngToDivPixel(overlayView.getBounds().getNorthEast(), ne -> {
                                                            mxGraph.getElement().executeJs("return this.graph.view.scale").then(Double.class, scale -> {
                                                                path = points;
                                                                this.scale = scale;
                                                                this.southWest = sw;
                                                                this.northEast = ne;
                                                                unregisterListeners();
                                                                drawingManager.setDrawingMode(null);
                                                            });
                                                        });
                                                    });
                                                });
                                            });
                                        } else if (vertexTarget == null) {
                                            vertexTarget = cell;
                                            
                                            MxGraphCell edge = new MxGraphCell();
                                            edge.setIsEdge(true);
                                            edge.setStrokeWidth(1);
                                            edge.setStrokeColor("blue");
                                            edge.setSource(vertexSource.getUuid());
                                            edge.setTarget(vertexTarget.getUuid());
                                            
                                            for (Point point : path) {
                                                point.setX(Math.abs(point.getX() - southWest.getX()) / scale);
                                                point.setY(Math.abs(point.getY() - northEast.getY()) / scale);
                                            }
                                            path.get(0).setX(vertexSource.getX());
                                            path.get(0).setY(vertexSource.getY());
                                            
                                            path.get(path.size() -1).setX(vertexTarget.getX());
                                            path.get(path.size() -1).setY(vertexTarget.getY());
                                            
                                            JsonArray pointsAsJson = Json.createArray();
                                            for (int i = 0; i < path.size(); i++)
                                                pointsAsJson.set(i, path.get(i).toJson());
                                            
                                            edge.setPoints(pointsAsJson.toJson());
                                            mxGraph.addCell(edge);
                                            
                                            vertexSource = null;
                                            vertexTarget = null;
                                            path = null;
                                        }
                                    }
                                }
                            }
                        }
                    });
                    overlayView = new OverlayView(theEvent.getBounds());
                    gMap.addOverlayView(overlayView);
                    overlayView.add(mxGraph);
                    
                    consumer = newWidth -> {
                        if (startWidth == -1)
                            startWidth = newWidth;
                        mxGraph.getElement().executeJs("this.graph.view.setScale($0 / $1);", newWidth, startWidth);
                    };
                    
                    mxGraph.addGraphLoadedListener(graphLoaded -> {
                        overlayView.getElement().executeJs("mxUtils.getCurrentStyle = function() {return null;}").then(nil0 -> {
                            if (mxGraphWidth != -1)
                                consumer.accept(mxGraphWidth);
                            mxGraphLoaded = true;
                        });
                    });
                    overlayView.addWidthChangedListener(widthChanged -> {
                        if (mxGraphLoaded)
                            consumer.accept(widthChanged.getWidth());
                        else
                            mxGraphWidth = widthChanged.getWidth();
                    });
                });
            } else if (tabVertex.equals(event.getSelectedTab())) {
                unregisterListeners();
                drawingManager.setDrawingMode(null);
                drawingManager.setDrawingMode(OverlayType.MARKER);
                
                registrationMarker = drawingManager.addDrawingManagerMarkerCompleteListener(theEvent -> {
                    
                    overlayView.fromLatLngToDivPixel(overlayView.getBounds().getSouthWest(), sw -> {
                        overlayView.fromLatLngToDivPixel(overlayView.getBounds().getNorthEast(), ne -> {
                            overlayView.fromLatLngToDivPixel(new LatLng(theEvent.getLat(), theEvent.getLng()), point -> {
                                mxGraph.getElement().executeJs("return this.graph.view.scale").then(Double.class, scale -> {
                                    double x = Math.abs(point.getX() - sw.getX()) / scale;
                                    double y = Math.abs(point.getY() - ne.getY()) / scale;

                                    MxGraphCell vertex = new MxGraphCell();
                                    vertex.setUuid(UUID.randomUUID().toString());
                                    mxGraph.addCell(vertex);
                                    vertex.setGeometry((int) x, (int) y, 32, 32);
                                    vertex.setIsVertex(true);
                                });
                            });
                        });
                    });
                });
            } else if (tabEdge.equals(event.getSelectedTab())) {
                unregisterListeners();
                drawingManager.setDrawingMode(null);
                drawPolyline = true;                
            } else if (tabGoogleMap.equals(event.getSelectedTab()))
                UI.getCurrent().navigate(MainView.class);
        });
        add(tabs);
        add(gMap);
    }
    
    private void setPoints(List<Point> points, List<LatLng> path, Command command) {
        if (points != null && !path.isEmpty()) {
            LatLng latLng = path.remove(0);
            overlayView.fromLatLngToDivPixel(latLng, point -> {
                points.add(point);
                setPoints(points, path, command);
            });
        }
        else
            command.execute();
    }
    
    private void unregisterListeners() {
        if (registrationRectangle != null) {
            registrationRectangle.remove();
            registrationRectangle = null;
        }
        if (registrationMarker != null) {
            registrationMarker.remove();
            registrationMarker = null;
        }
        if (registrationPolyline != null) {
            registrationPolyline.remove();
            registrationPolyline = null;
        }
    }
}

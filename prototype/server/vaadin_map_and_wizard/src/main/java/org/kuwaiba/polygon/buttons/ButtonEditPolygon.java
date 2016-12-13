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
package org.kuwaiba.polygon.buttons;

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.Button;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.connection.ConnectionUtils;
import org.kuwaiba.custom.overlays.PolygonMarker;
import org.kuwaiba.polygon.MapPolygon;
import org.kuwaiba.polygon.PolygonExt;
import org.kuwaiba.polygon.events.PolygonMarkerDragListener;
import org.kuwaiba.polygon.events.VertexMarkerClickListener;
import org.kuwaiba.utils.Constants;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ButtonEditPolygon extends Button {
    private PolygonExt polygonExt;
    
    public ButtonEditPolygon(PolygonExt polygonExt) {
        super("Edit");
        this.polygonExt = polygonExt;
        addClickListener(new ButtonClickListenerImpl());
    }
    
    private class ButtonClickListenerImpl implements Button.ClickListener {
        public ButtonClickListenerImpl() {
        }

        @Override
        public void buttonClick(ClickEvent event) {
            MapPolygon mapPolygon = polygonExt.getMapPolygon();
            GoogleMap googleMap = mapPolygon.getGoogleMap();
            PolygonExt polygon = mapPolygon.getPolygon();
            
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
                middlePoint.setIconUrl(Constants.dummyVertexIconUrl);
                    
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
    };
}
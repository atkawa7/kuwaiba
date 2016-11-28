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
package org.kuwaiba.polygon.events;

import com.vaadin.server.VaadinSession;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.MapClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.NativeSelect;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.custom.map.buttons.DrawPolygonButton;
import org.kuwaiba.custom.overlays.PolygonMarker;
import org.kuwaiba.polygon.MapPolygon;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PolygonMapClickListener implements MapClickListener {
    VaadinSession session;
    
    List<PolygonMarker> vertices;

    GoogleMap googleMap;
    GoogleMapPolyline polyline;
    GoogleMapPolygon polygon;
    
    private final NativeSelect nativeSelect;
    private final List<MapPolygon> mapPolygons;
    
    public PolygonMapClickListener(VaadinSession session, GoogleMap googleMap, NativeSelect nativeSelect, List<MapPolygon> mapPolygons) {
        this.session = session;
                
        vertices = new ArrayList();
        
        this.nativeSelect = nativeSelect;
        this.mapPolygons = mapPolygons;
        
        this.googleMap = googleMap;
        polygon = null;
        polyline = null;
    }

    @Override
    public void mapClicked(LatLon position) {
        if ((Boolean) session.getAttribute(DrawPolygonButton.NAME)) {
            PolygonMarker polygonMarker = new PolygonMarker();
            polygonMarker.setPosition(position);
            
            if (!vertices.isEmpty()) {
                vertices.add(polygonMarker);
                
                List<LatLon> coordinates = new ArrayList();

                for (PolygonMarker vertex : vertices)
                    coordinates.add(vertex.getPosition());

                if (polyline != null)
                    googleMap.removePolyline(polyline);
                
                polyline = new GoogleMapPolyline();
                polyline.setCoordinates(coordinates);
                polyline.setStrokeColor("orange");
                polyline.setStrokeOpacity(1);
                polyline.setStrokeWeight(5);

                googleMap.addPolyline(polyline);

                if (polygon != null)
                    googleMap.removePolygonOverlay(polygon);
                
                polygon = new GoogleMapPolygon();
                polygon.setFillColor("blue");
                polygon.setFillOpacity(0.5);
                polygon.setStrokeColor("blue");
                polygon.setStrokeOpacity(1);
                polygon.setStrokeWeight(1);                
                
                polygon.setCoordinates(coordinates);

                googleMap.addPolygonOverlay(polygon);
                
                if (position.equals(vertices.get(0).getPosition())) {
                    for (PolygonMarker vertex : vertices)
                        googleMap.removeMarker(vertex);
                    googleMap.removePolyline(polyline);
                    
                    polygon.getCoordinates().remove(polygonMarker.getPosition());
                    
                    MapPolygon mapPolygon = new MapPolygon(googleMap);
                    
                    List<PolygonMarker> vertices_ = new ArrayList();
                    for (PolygonMarker vertex : vertices)
                        vertices_.add(vertex);
                    
                    mapPolygon.setVertices(vertices_);
                    mapPolygon.setPolygon(polygon);
                    mapPolygon.setPolyline(polyline);
                    mapPolygon.setPolygonId(mapPolygons.size());
                    
                    mapPolygons.add(mapPolygon);
                    nativeSelect.addItem(Long.toString(mapPolygon.getPolygonId()));
//                    vertices.removeAll(vertices);
                    session.setAttribute(DrawPolygonButton.NAME, false);
                    
                    this.vertices = new ArrayList();
                    this.polygon = null;
                    this.polyline = null;
                }
                else
                    googleMap.addMarker(polygonMarker);
            }
            else {
                vertices.add(polygonMarker);
                googleMap.addMarker(polygonMarker);
            }
        }               
    }
    
}

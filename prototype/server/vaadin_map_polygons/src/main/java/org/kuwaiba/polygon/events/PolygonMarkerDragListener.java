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

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.MarkerDragListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.connection.ConnectionUtils;
import org.kuwaiba.custom.overlays.PolygonMarker;
import org.kuwaiba.polygon.MapPolygon;
import org.kuwaiba.polygon.PolygonExt;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PolygonMarkerDragListener implements MarkerDragListener {
    private final MapPolygon mapPolygon;
    
    public PolygonMarkerDragListener(MapPolygon mapPolygon) {
        this.mapPolygon = mapPolygon;
    }
    
    @Override
    public void markerDragged(GoogleMapMarker draggedMarker, LatLon oldPosition) {
        if (draggedMarker instanceof PolygonMarker) {
            List<PolygonMarker> vertices = mapPolygon.getVertices();
                
            if (vertices.contains((PolygonMarker) draggedMarker)) {

                GoogleMap googleMap = mapPolygon.getGoogleMap();
                GoogleMapPolygon polygon = mapPolygon.getPolygon();
                GoogleMapPolyline polyline = mapPolygon.getPolyline();

                addVertex(vertices.indexOf((PolygonMarker) draggedMarker));

                googleMap.removePolygonOverlay(polygon);
                googleMap.removePolyline(polyline);

                List<LatLon> coordinatesPolygon = new ArrayList();
                List<LatLon> coordinatesPolyline = new ArrayList();

                for (int i = 0; i < vertices.size(); i += 1) {
                    if (i % 2 == 0) {
                        PolygonMarker vertex = vertices.get(i);

                        coordinatesPolyline.add(vertex.getPosition());

                        if (i != vertices.size() - 1)
                            coordinatesPolygon.add(vertex.getPosition());
                    }
                }
//                GoogleMapPolygon newPolygon = new GoogleMapPolygon();
                PolygonExt newPolygonExt = new PolygonExt(mapPolygon);
                GoogleMapPolyline newPolyline = new GoogleMapPolyline();

                newPolygonExt.setCoordinates(coordinatesPolygon);
                newPolygonExt.setFillColor(polygon.getFillColor());
                newPolygonExt.setFillOpacity(polygon.getFillOpacity());
                newPolygonExt.setStrokeColor(polygon.getStrokeColor());
                newPolygonExt.setStrokeOpacity(polygon.getStrokeOpacity());
                newPolygonExt.setStrokeWeight(polygon.getStrokeWeight());

                newPolyline.setCoordinates(coordinatesPolyline);
                newPolyline.setStrokeColor(polyline.getStrokeColor());
                newPolyline.setStrokeOpacity(polyline.getStrokeOpacity());
                newPolyline.setStrokeWeight(polyline.getStrokeWeight());

                googleMap.addPolyline(newPolyline);
                googleMap.addPolygonOverlay(newPolygonExt);

                mapPolygon.setPolygon(newPolygonExt);
                mapPolygon.setPolyline(newPolyline);
            }
        }
    }
    
    public void addVertex(int index) {
        GoogleMap googleMap = mapPolygon.getGoogleMap();
        List<PolygonMarker> vertices = mapPolygon.getVertices();
        
        if (index % 2 != 0) {
            PolygonMarker vertex = vertices.get(index);
            vertex.setIconUrl("VAADIN/img/polygonControlPoint.png");
            
            PolygonMarker rightVertex =  vertices.get(index + 1);
            PolygonMarker leftVertex = vertices.get(index - 1);
            
            PolygonMarker rightMiddlePoint = new PolygonMarker();
            rightMiddlePoint.setPosition(ConnectionUtils.midPoint(vertex.getPosition(), rightVertex.getPosition()));
            rightMiddlePoint.setIconUrl("VAADIN/img/polygonMiddleControlPoint.png");
                        
            PolygonMarker leftMiddlePoint = new PolygonMarker();
            leftMiddlePoint.setPosition(ConnectionUtils.midPoint(leftVertex.getPosition(), vertex.getPosition()));
            leftMiddlePoint.setIconUrl("VAADIN/img/polygonMiddleControlPoint.png");
            
            googleMap.addMarker(leftMiddlePoint);
            googleMap.addMarker(rightMiddlePoint);
            
            vertices.add(index + 1, rightMiddlePoint);
            vertices.add(index, leftMiddlePoint); 
        }
        else {
            if (vertices.get(index).equals(vertices.get(0))) {
                int thirdLastIndex = (vertices.size() - 1) - 2;
                movePairMarket(0, 2, thirdLastIndex);
            }
            else
                movePairMarket(index, index + 2, index - 2); 
        }
    }
    
    private void movePairMarket(int vertexIndex, int leftVertexIndex, int rightVertexIndex) {
        GoogleMap googleMap = mapPolygon.getGoogleMap();
        List<PolygonMarker> vertices = mapPolygon.getVertices();
        
        PolygonMarker vertex = vertices.get(vertexIndex);
        PolygonMarker leftVertex = vertices.get(leftVertexIndex);
        PolygonMarker rightVertex = vertices.get(rightVertexIndex);
        
        PolygonMarker leftMiddlePoint = vertices.get(leftVertexIndex - 1);
        PolygonMarker rightMiddlePoint = vertices.get(rightVertexIndex + 1);
                
        vertices.remove(leftMiddlePoint);
        vertices.remove(rightMiddlePoint);
        googleMap.removeMarker(leftMiddlePoint);
        googleMap.removeMarker(rightMiddlePoint);
                
        leftMiddlePoint = new PolygonMarker();
        leftMiddlePoint.setPosition(ConnectionUtils.midPoint(leftVertex.getPosition(), vertex.getPosition()));
        leftMiddlePoint.setIconUrl("VAADIN/img/polygonMiddleControlPoint.png");
                
        rightMiddlePoint = new PolygonMarker();
        rightMiddlePoint.setPosition(ConnectionUtils.midPoint(vertex.getPosition(), rightVertex.getPosition()));
        rightMiddlePoint.setIconUrl("VAADIN/img/polygonMiddleControlPoint.png");
        if (vertexIndex == 0) {
            vertices.add(leftVertexIndex - 1, leftMiddlePoint);
            vertices.add(rightVertexIndex + 1, rightMiddlePoint);
        }
        else {
            vertices.add(rightVertexIndex + 1, rightMiddlePoint);
            vertices.add(leftVertexIndex - 1, leftMiddlePoint);
        }
        googleMap.addMarker(leftMiddlePoint);
        googleMap.addMarker(rightMiddlePoint);
    }
}

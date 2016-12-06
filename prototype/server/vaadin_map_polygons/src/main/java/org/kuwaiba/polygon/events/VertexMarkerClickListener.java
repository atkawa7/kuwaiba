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
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
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
public class VertexMarkerClickListener implements MarkerClickListener {
    private final MapPolygon mapPolygon;
    
    private PolygonMarker clickedVertex;
    private int vertexClickCounter;
    private int dummyVertexClickCounter;
    
    public VertexMarkerClickListener(MapPolygon mapPolygon) {
        this.mapPolygon = mapPolygon;
        clickedVertex = null;
        vertexClickCounter = 0;
        dummyVertexClickCounter = 0;
    }

    @Override
    public void markerClicked(GoogleMapMarker clickedMarker) {
        if (clickedMarker instanceof PolygonMarker) {
            PolygonMarker vertex = (PolygonMarker) clickedMarker;
            List<PolygonMarker> vertices = mapPolygon.getVertices();

            if (vertices.contains(vertex)) {
                if (clickedVertex == null)
                    clickedVertex = vertex;
                
                if (vertex.equals(clickedVertex)) {
                    int indexClickedVertex = vertices.indexOf(vertex);
                    if (indexClickedVertex % 2 == 0) { // is a vertex
                        if (vertexClickCounter == 0)
                            vertexClickCounter += 1;
                        else {
                            removeVertex(vertex);
                            vertexClickCounter = 0;
                            clickedVertex = null;
                        }
                    }
                    else { // is a dummy vertex
                        if (dummyVertexClickCounter == 0)
                            dummyVertexClickCounter += 1;
                        else {
                            disableEditPolygon();
                            dummyVertexClickCounter = 0;
                            clickedVertex = null;
                        }
                    }
                }
                else {
                    clickedVertex = null;
                    vertexClickCounter = 0;
                    dummyVertexClickCounter = 0; 
                }
            }
        }
    }
    
    private void removeVertex(PolygonMarker vertex) {
        GoogleMap googleMap = mapPolygon.getGoogleMap();
        GoogleMapPolygon polygon = mapPolygon.getPolygon();
        GoogleMapPolyline polyline = mapPolygon.getPolyline();
        
        List<PolygonMarker> vertices = mapPolygon.getVertices();
        
        googleMap.removePolyline(polyline);
        googleMap.removePolygonOverlay(polygon);
        
        int vertexIndex = vertices.indexOf(vertex);
        
        if (vertexIndex == 0) {
            int firstVertexIdx = vertexIndex;
            PolygonMarker firstVertex = vertices.get(firstVertexIdx);
            
            int secondVertexIdx = vertexIndex + 1;
            PolygonMarker secondVertex = vertices.get(secondVertexIdx);
            
            googleMap.removeMarker(firstVertex);
            googleMap.removeMarker(secondVertex);
            
            int lastVertexIdx = vertices.size() - 1;
            PolygonMarker lastVertex = vertices.get(lastVertexIdx);
            
            int secondLastVertexIdx = vertices.size() - 2;
            PolygonMarker secondLastVertex = vertices.get(secondLastVertexIdx);
            googleMap.removeMarker(secondLastVertex);
            
            int thirdVertexIdx = 2;
            PolygonMarker thirdVertex = vertices.get(thirdVertexIdx);
            
            int thirdLastVertexIdx = vertices.size() - 3;
            PolygonMarker lastThirdVertex = vertices.get(thirdLastVertexIdx);
            
            vertices.remove(firstVertex);
            vertices.remove(secondVertex);
            vertices.remove(lastVertex);
            vertices.remove(secondLastVertex);
            
            PolygonMarker dummyVertex = new PolygonMarker();
            dummyVertex.setPosition(ConnectionUtils.midPoint(thirdVertex.getPosition(), lastThirdVertex.getPosition()));
            dummyVertex.setIconUrl("VAADIN/img/polygonMiddleControlPoint.png");
            
            googleMap.addMarker(dummyVertex);
            
            vertices.add(dummyVertex);
            vertices.add(thirdVertex);        
        }
        else {
            PolygonMarker pastVertex = vertices.get(vertexIndex - 2);
            PolygonMarker pastDummyVertex = vertices.get(vertexIndex - 1);
            
            PolygonMarker nextDummyVertex = vertices.get(vertexIndex + 1);
            PolygonMarker nextVertex = vertices.get(vertexIndex + 2);
            
            googleMap.removeMarker(vertex);
            googleMap.removeMarker(pastDummyVertex);
            googleMap.removeMarker(nextDummyVertex);
            
            PolygonMarker newDummyVertex = new PolygonMarker();
            newDummyVertex.setPosition(ConnectionUtils.midPoint(pastVertex.getPosition(), nextVertex.getPosition()));
            newDummyVertex.setIconUrl("VAADIN/img/polygonMiddleControlPoint.png");
            
            googleMap.addMarker(newDummyVertex);
            vertices.add(vertexIndex, newDummyVertex);
                        
            vertices.remove(vertex);
            vertices.remove(pastDummyVertex);
            vertices.remove(nextDummyVertex);
        }
        List<LatLon> coordinatesPolyline = new ArrayList();
        List<LatLon> coordinatesPolygon = new ArrayList();
        
        for (int i = 0; i < vertices.size(); i += 1) {
            if (i % 2 == 0) {
                PolygonMarker v = vertices.get(i);
                
                coordinatesPolyline.add(v.getPosition());                                
                
                if (i != vertices.size() - 1)
                    coordinatesPolygon.add(v.getPosition());
            }
        }
        /*
        for (PolygonMarker vertex_ : vertices)
            coordinates.add(vertex_.getPosition());
        coordinates.add(vertices.get(0).getPosition());
          */  
//        GoogleMapPolygon newPolygon = new GoogleMapPolygon();
        PolygonExt polygonExt = new PolygonExt(mapPolygon);
        GoogleMapPolyline newPolyline = new GoogleMapPolyline();
            
        polygonExt.setCoordinates(coordinatesPolygon);
        polygonExt.setFillColor(polygon.getFillColor());
        polygonExt.setFillOpacity(polygon.getFillOpacity());
        polygonExt.setStrokeColor(polygon.getStrokeColor());
        polygonExt.setStrokeOpacity(polygon.getStrokeOpacity());
        polygonExt.setStrokeWeight(polygon.getStrokeWeight());
            
        newPolyline.setCoordinates(coordinatesPolyline);
        newPolyline.setStrokeColor(polyline.getStrokeColor());
        newPolyline.setStrokeOpacity(polyline.getStrokeOpacity());
        newPolyline.setStrokeWeight(polyline.getStrokeWeight());
            
        googleMap.addPolyline(newPolyline);
        googleMap.addPolygonOverlay(polygonExt);
                   
        mapPolygon.setPolygon(polygonExt);
        mapPolygon.setPolyline(newPolyline);
    }
    
    private void disableEditPolygon() {
        GoogleMap googleMap = mapPolygon.getGoogleMap();
        GoogleMapPolyline polyline = mapPolygon.getPolyline();
        
        List<PolygonMarker> vertices = mapPolygon.getVertices();
        
        googleMap.removePolyline(polyline);
        
        for (PolygonMarker vertex : vertices)
            googleMap.removeMarker(vertex);
    }
}

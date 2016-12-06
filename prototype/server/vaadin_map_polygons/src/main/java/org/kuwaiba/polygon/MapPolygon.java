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
package org.kuwaiba.polygon;

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import java.util.List;
import org.kuwaiba.custom.overlays.PolygonMarker;
import org.kuwaiba.polygon.events.PolygonMarkerDragListener;
import org.kuwaiba.polygon.events.VertexMarkerClickListener;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MapPolygon {
    private long polygonId;
    
    private List<PolygonMarker> vertices;
    
    private final GoogleMap googleMap;
    private PolygonExt polygon;
    private GoogleMapPolyline polyline;
    
    private PolygonMarkerDragListener dragListener;
    private VertexMarkerClickListener clickListener;
    
    public MapPolygon(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public PolygonExt getPolygon() {
        return polygon;
    }

    public void setPolygon(PolygonExt polygon) {
        this.polygon = polygon;
    }

    public GoogleMapPolyline getPolyline() {
        return polyline;
    }

    public void setPolyline(GoogleMapPolyline polyline) {
        this.polyline = polyline;
    }
    
    public GoogleMap getGoogleMap() {
        return googleMap;
    }
    
    public long getPolygonId() {
        return polygonId;
    }
    
    public void setPolygonId(long polygonId) {
        this.polygonId = polygonId;
    }
    
    public List<PolygonMarker> getVertices() {
        return vertices;        
    }
    
    public void setVertices(List<PolygonMarker> vertices) {
        this.vertices = vertices;
    }

    public PolygonMarkerDragListener getDragListener() {
        return dragListener;
    }

    public void setDragListener(PolygonMarkerDragListener dragListener) {
        this.dragListener = dragListener;
    }

    public VertexMarkerClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(VertexMarkerClickListener clickListener) {
        this.clickListener = clickListener;
    }    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.map.events;

import com.vaadin.shared.communication.SharedState;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.MarkerDragListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.Notification;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author johnyortega
 */
public class MoveMarkerOnMarkerDragListener implements MarkerDragListener {
    GoogleMap googleMap;
    List<GoogleMapPolyline> edges;
    
    public MoveMarkerOnMarkerDragListener(GoogleMap googleMap, List<GoogleMapPolyline> edges) {
        this.googleMap = googleMap;
        this.edges = edges;
    }

    @Override
    public void markerDragged(GoogleMapMarker draggedMarker, LatLon oldPosition) {
        for (GoogleMapPolyline edge : edges)
            googleMap.removePolyline(edge);
        
        ArrayList<LatLon> points = new ArrayList();
        for (GoogleMapMarker marker : googleMap.getMarkers())
            points.add(marker.getPosition());
        
        GoogleMapPolyline edge = new GoogleMapPolyline(points, "#d31717", 0.8, 10);
        googleMap.addPolyline(edge);
        edges.add(edge);
    }
}

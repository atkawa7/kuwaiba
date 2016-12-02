/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.custom.polyline.events;

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.events.PolylineDblClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import org.kuwaiba.connection.Connection;
import org.kuwaiba.custom.overlays.ControlPointMarker;
import org.kuwaiba.custom.polyline.Edge;

/**
 *
 * @author johnyortega
 */
public class EdgeDblClick implements PolylineDblClickListener {
    
    public EdgeDblClick() {
        
    }
    
    @Override
    public void polylineDblClicked(GoogleMapPolyline clickedPolyline) {
        if (clickedPolyline instanceof Edge) {
            Edge edge = (Edge) clickedPolyline;
            Connection connection = edge.getConnection();
            GoogleMap googleMap = connection.getMap();
            
            connection.getSource().getConnections().remove(connection);
            connection.getTarget().getConnections().remove(connection);
            connection.getEdges().remove(connection);
        
            for (ControlPointMarker cp : connection.getControlPoints())
                googleMap.removeMarker(cp);
            googleMap.removePolyline(connection.getEdge());
        }
    }
    
}

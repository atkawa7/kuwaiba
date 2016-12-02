/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.custom.polyline.events;

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.PolylineClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import org.kuwaiba.connection.Connection;
import org.kuwaiba.custom.polyline.Edge;
import org.kuwaiba.utils.Constans;

/**
 *
 * @author johnyortega
 */
public class EdgeClick implements PolylineClickListener {
    
    public EdgeClick() {
    }

    @Override
    public void polylineClicked(GoogleMapPolyline clickedPolyline) {
        if (clickedPolyline instanceof Edge) {
            Edge oldEdge = (Edge) clickedPolyline;
            Connection connection = oldEdge.getConnection();
            GoogleMap googleMap = connection.getMap();
                                    
            Edge newEdge = new Edge(connection);
            for (LatLon coordinate : oldEdge.getCoordinates())
                newEdge.getCoordinates().add(coordinate);
                    
            if (Constans.defaultSelectedConnColor.equals(oldEdge.getStrokeColor()))
                newEdge.setStrokeColor(connection.getColor());
            else
                newEdge.setStrokeColor(Constans.defaultSelectedConnColor);
                    
            newEdge.setStrokeOpacity(clickedPolyline.getStrokeOpacity());
            newEdge.setStrokeWeight(clickedPolyline.getStrokeWeight());                
                                    
            googleMap.removePolyline(clickedPolyline);  
                    
            connection.setConnection(newEdge);
            googleMap.addPolyline(newEdge);
        }
    }
    
}

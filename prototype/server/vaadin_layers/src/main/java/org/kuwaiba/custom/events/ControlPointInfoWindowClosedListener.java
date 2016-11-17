/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.custom.events;

import com.vaadin.tapio.googlemaps.client.events.InfoWindowClosedListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapInfoWindow;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import java.util.ArrayList;
import org.kuwaiba.connection.Connection;
import org.kuwaiba.custom.overlays.ControlPointMarker;

/**
 *
 * @author johnyortega
 */
public class ControlPointInfoWindowClosedListener implements InfoWindowClosedListener {
    
    public ControlPointInfoWindowClosedListener() {
    }

    @Override
    public void infoWindowClosed(GoogleMapInfoWindow window) {
        if (window.getAnchorMarker() instanceof ControlPointMarker) {
            ControlPointMarker marker = (ControlPointMarker) window.getAnchorMarker();
            
            Connection conn = marker.getConnection();
            conn.getConnection().setStrokeColor("green");
            conn.getConnection().setStrokeOpacity(1);

            String strokeColor = conn.getConnection().getStrokeColor();
            double strokeOpacity = conn.getConnection().getStrokeOpacity();
            int strokeWeight = conn.getConnection().getStrokeWeight();

            GoogleMapPolyline polyline = new GoogleMapPolyline(new ArrayList(), strokeColor, strokeOpacity, strokeWeight);
            for (ControlPointMarker controlPoint : conn.getControlPoints())
                polyline.getCoordinates().add(controlPoint.getPosition());
            conn.getMap().removePolyline(conn.getConnection());
            conn.setConnection(polyline);
            conn.getMap().addPolyline(polyline);
        }
    }
    
}

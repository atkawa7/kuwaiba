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
package org.kuwaiba.custom.events;

import com.vaadin.server.VaadinSession;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import java.util.ArrayList;
import org.kuwaiba.connection.Connection;
import org.kuwaiba.custom.map.buttons.ConnectionButton;
import org.kuwaiba.custom.overlays.ControlPointMarker;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ControlPointMarkerClickListener implements MarkerClickListener {
    public final VaadinSession session;
    private final GoogleMap googleMap;
    
    public ControlPointMarkerClickListener(GoogleMap googleMap, VaadinSession session) {
        this.googleMap = googleMap;
        this.session = session;
    }
    
    @Override
    public void markerClicked(GoogleMapMarker clickedMarker) {
        if (clickedMarker instanceof ControlPointMarker) {
            if ((Boolean) session.getAttribute(ConnectionButton.NAME)) {
                googleMap.openInfoWindow(((ControlPointMarker) clickedMarker).getInfoWindows());
                
                ControlPointMarker marker = (ControlPointMarker) clickedMarker;
                
                Connection conn = marker.getConnection();
                conn.getConnection().setStrokeColor("orange");
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
}

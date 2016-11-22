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
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.MarkerDragListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import java.util.ArrayList;
import org.kuwaiba.custom.overlays.ControlPointMarker;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ControlPointMarkerDragListener implements MarkerDragListener {
    private final VaadinSession session;
    GoogleMap googleMap;
    
    public ControlPointMarkerDragListener(GoogleMap googleMap, VaadinSession session) {
        this.googleMap = googleMap;
        this.session = session;
    }
    @Override
    public void markerDragged(GoogleMapMarker draggedMarker, LatLon oldPosition) {
        if (draggedMarker instanceof ControlPointMarker) {
            //((ControlPointMarker) draggedMarker);
            //for (GoogleMapMarker marker : googleMap.getMarkers())
//            if ((Boolean) session.getAttribute(ConnectionButton.NAME)) {
                GoogleMapPolyline polyline = new GoogleMapPolyline(new ArrayList(), "green", 1, 5);

                for (ControlPointMarker controlPoint : ((ControlPointMarker) draggedMarker).getConnection().getControlPoints()) {
    //                if (controlPoint.getPosition().equals(oldPosition))
    //                    polyline.getCoordinates().add(draggedMarker.getPosition());
    //                else
                    polyline.getCoordinates().add(controlPoint.getPosition());
                }            
                googleMap.removePolyline(((ControlPointMarker) draggedMarker).getConnection().getConnection());
                ((ControlPointMarker) draggedMarker).getConnection().setConnection(polyline);
    //            ((ControlPointMarker) draggedMarker).setPolyline(polyline);
                googleMap.addPolyline(polyline);
                //             googleMap.addMarker(new GoogleMapMarker("New marker " + id, position, true, "VAADIN/img/building_32.png"));
            }
//        }
    }
    
}

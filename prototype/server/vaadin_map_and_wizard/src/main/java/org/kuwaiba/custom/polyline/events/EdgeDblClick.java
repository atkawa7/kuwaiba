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
package org.kuwaiba.custom.polyline.events;

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.events.PolylineDblClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import org.kuwaiba.connection.Connection;
import org.kuwaiba.custom.overlays.ControlPointMarker;
import org.kuwaiba.custom.polyline.Edge;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
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

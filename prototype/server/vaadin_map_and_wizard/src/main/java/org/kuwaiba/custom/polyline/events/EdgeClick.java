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
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.PolylineClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import org.kuwaiba.connection.Connection;
import org.kuwaiba.custom.polyline.Edge;
import org.kuwaiba.utils.Constants;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
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
                    
            if (Constants.defaultSelectedConnColor.equals(oldEdge.getStrokeColor()))
                newEdge.setStrokeColor(connection.getColor());
            else
                newEdge.setStrokeColor(Constants.defaultSelectedConnColor);
                    
            newEdge.setStrokeOpacity(clickedPolyline.getStrokeOpacity());
            newEdge.setStrokeWeight(clickedPolyline.getStrokeWeight());                
                                    
            googleMap.removePolyline(clickedPolyline);  
                    
            connection.setConnection(newEdge);
            googleMap.addPolyline(newEdge);
        }
    }
    
}

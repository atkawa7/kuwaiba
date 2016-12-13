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
package org.kuwaiba.connection;

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.custom.overlays.NodeMarker;
import org.kuwaiba.custom.polyline.Edge;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Measure {
    private GoogleMap googleMap;
    private NodeMarker source;
    private NodeMarker target;
    
    private List<Connection> connections;
    
    private double distance;
    
    private int i = 0;
    
    public Measure(GoogleMap googleMap) {
        this.googleMap = googleMap;
        connections = new ArrayList();
        distance = 0;        
    }
    
    public double calculateDistance(Connection connection) {
        List<LatLon> coordinates = connection.getEdge().getCoordinates();
        
        int sizeCoordinates = coordinates.size();
        
        double edge_distance = 0;
        for (int i = 0; i < sizeCoordinates - 1; i += 1) {
            LatLon position1 = coordinates.get(i);
            LatLon position2 = coordinates.get(i + 1);
            
            edge_distance += ConnectionUtils.distance(position1, position2);
        }
        
        distance += edge_distance;
        i = 0;
        source = null;
        target = null;
        
        connections.add(connection);
        return distance;
    }
    
    public void endMeasure() {
        distance = 0;
        i = 0;
        for (Connection connection : connections) {
            googleMap.removePolyline(connection.getEdge());
            List<LatLon> coordinates = connection.getEdge().getCoordinates();
            Edge edge = new Edge(connection, coordinates, "green", 1, 5);
            connection.setConnection(edge);
            googleMap.addPolyline(connection.getEdge());
        }
        source = null;
        target = null;
        connections.removeAll(connections);
    }

    public NodeMarker getSource() {
        return source;
    }

    public void setSource(NodeMarker source) {
        this.source = source;
    }

    public NodeMarker getTarget() {
        return target;
    }

    public void setTarget(NodeMarker target) {
        this.target = target;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }
    
    
}

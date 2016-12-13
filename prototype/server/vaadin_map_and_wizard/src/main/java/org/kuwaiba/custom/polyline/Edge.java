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
package org.kuwaiba.custom.polyline;

import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import java.util.List;
import org.kuwaiba.connection.Connection;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Edge extends GoogleMapPolyline {
    
    Connection connection;
    
    public Edge(Connection connection) {
        this.connection = connection;
    }
    
    public Edge(Connection connection, List<LatLon> coordinates, String strokeColor,
        double strokeOpacity, int strokeWeight) {
        
        super(coordinates, strokeColor, strokeOpacity, strokeWeight);
        this.connection = connection;
    }
    
    public Connection getConnection() {
        return connection;
    }
}

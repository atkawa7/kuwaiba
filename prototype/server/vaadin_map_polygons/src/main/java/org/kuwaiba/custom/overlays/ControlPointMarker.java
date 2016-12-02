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
package org.kuwaiba.custom.overlays;

import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import java.util.List;
import org.kuwaiba.connection.Connection;
/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ControlPointMarker extends GoogleMapMarker {
    Connection conn;
    List<Connection> edges;
    
    public ControlPointMarker(LatLon latlon, List<Connection> edges) {
        this.edges = edges;
        
        setPosition(latlon);
        setIconUrl("VAADIN/img/controlPoint.png");
                
        setAnimationEnabled(false);
        setDraggable(true);
    }
    
    public void setConnection(Connection conn) {
        this.conn = conn;
    }
    
    public Connection getConnection() {
        return conn;
    }
}

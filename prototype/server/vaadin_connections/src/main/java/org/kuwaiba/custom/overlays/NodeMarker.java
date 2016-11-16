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

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapInfoWindow;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.Button;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.connection.Connection;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class NodeMarker extends GoogleMapMarker {
    List<Connection> connections;
    GoogleMapInfoWindow infoWindow;
    Button btnDeleteNode;
    GoogleMap googleMap;
    
    private class ButtonDeleteNode implements Button.ClickListener {
        
        public ButtonDeleteNode() {
        }
        
        @Override
        public void buttonClick(Button.ClickEvent event) {
            googleMap.closeInfoWindow(infoWindow);
            googleMap.removeMarker(infoWindow.getAnchorMarker());
            
            for (Connection conn : connections) {                
                if (this.equals(conn.getSource()))
                    conn.getTarget().getConnections().remove(conn);
                if (this.equals(conn.getTarget()))
                    conn.getSource().getConnections().remove(conn);
                
                for (ControlPointMarker controlPoint_ : conn.getControlPoints()) 
                    conn.getMap().removeMarker(controlPoint_);

                conn.getMap().removePolyline(conn.getConnection());

                conn.setI(1);            
            }
            connections.removeAll(connections);
        }
    }
    
    public NodeMarker(GoogleMap googleMap, String caption, LatLon position, boolean draggable) {
        super(caption, position, draggable);
        connections = new ArrayList();
        
        infoWindow = new GoogleMapInfoWindow("Info window node",this);
        btnDeleteNode = new Button("Delete");
        btnDeleteNode.addClickListener(new ButtonDeleteNode());
        
        this.googleMap = googleMap;        
    }
    
    public List<Connection> getConnections() {
        return connections;
    }
    
    public GoogleMapInfoWindow getInfoWindow() {
        return infoWindow;
    }
    
    public Button getDeleteButton() {
        return btnDeleteNode;
    }
}

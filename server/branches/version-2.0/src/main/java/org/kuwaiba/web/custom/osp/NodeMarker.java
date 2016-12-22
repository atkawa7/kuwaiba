/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.custom.osp;

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapInfoWindow;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.Button;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
//import org.kuwaiba.connection.Connection;

/**
 * Represents a marker in the map (google-map)
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class NodeMarker extends GoogleMapMarker {
    
    //List<Connection> connections;
    private final GoogleMapInfoWindow infoWindow;
    private final Button btnDeleteNode;
    private final GoogleMap googleMap;
    private final RemoteBusinessObjectLight remoteBusinessObject;
    
    public NodeMarker(GoogleMap googleMap, LatLon position, boolean draggable,
            RemoteBusinessObjectLight remoteBusinessObject)
    {
        super(remoteBusinessObject.getName(), position, draggable);
        //connections = new ArrayList();
        infoWindow = new GoogleMapInfoWindow("Info window node",this);
        btnDeleteNode = new Button("Delete");
        btnDeleteNode.addClickListener(new ButtonDeleteNode());
        this.remoteBusinessObject = remoteBusinessObject;
        this.googleMap = googleMap;        
    }
    
    public RemoteBusinessObjectLight getRemoteBusinessObject() {
        return remoteBusinessObject;
    }

//    public List<Connection> getConnections() {
//        return connections;
//    }

    public GoogleMapInfoWindow getInfoWindow() {
        return infoWindow;
    }
    
    public Button getDeleteButton() {
        return btnDeleteNode;
    }
    
    private class ButtonDeleteNode implements Button.ClickListener {
        
        public ButtonDeleteNode() {
        }
        
        @Override
        public void buttonClick(Button.ClickEvent event) {
            googleMap.closeInfoWindow(infoWindow);
            googleMap.removeMarker(infoWindow.getAnchorMarker());
//            
//            for (Connection conn : connections) {                
//                if (this.equals(conn.getSource()))
//                    conn.getTarget().getConnections().remove(conn);
//                if (this.equals(conn.getTarget()))
//                    conn.getSource().getConnections().remove(conn);
//                
//                for (ControlPointMarker controlPoint_ : conn.getControlPoints()) 
//                    conn.getMap().removeMarker(controlPoint_);
//
//                conn.getMap().removePolyline(conn.getConnection());
//
//                conn.setI(1);            
//            }
//            connections.removeAll(connections);
        }
    }
}

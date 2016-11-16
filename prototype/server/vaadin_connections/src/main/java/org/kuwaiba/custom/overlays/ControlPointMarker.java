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
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapInfoWindow;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.Button;
import java.util.ArrayList;
import org.kuwaiba.connection.Connection;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ControlPointMarker extends GoogleMapMarker {
    GoogleMapInfoWindow infoWindow;
    Connection conn;
    Button btnDeleteControlPoint;
    Button btnDeleteConnection;
    
    private class ButtonDeleteClickListener implements Button.ClickListener {
        private final ControlPointMarker controlPoint;
        
        public ButtonDeleteClickListener(ControlPointMarker controlPoint) {
            this.controlPoint = controlPoint;            
        }

        @Override
        public void buttonClick(Button.ClickEvent event) {
            conn.getMap().closeInfoWindow(controlPoint.getInfoWindows());
            
            conn.getMap().removeMarker(controlPoint);
            conn.getControlPoints().remove(controlPoint);
            
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
    
    private class ButtonDeleteConnection implements Button.ClickListener {
        private final ControlPointMarker controlPoint;
        
        public ButtonDeleteConnection(ControlPointMarker controlPoint) {
            this.controlPoint = controlPoint;            
        }
        
        @Override
        public void buttonClick(Button.ClickEvent event) {
            conn.getMap().closeInfoWindow(controlPoint.getInfoWindows());
            conn.getSource().getConnections().remove(conn);
            conn.getTarget().getConnections().remove(conn);
            
            for (ControlPointMarker controlPoint_ : conn.getControlPoints()) 
                conn.getMap().removeMarker(controlPoint_);
                
            conn.getMap().removePolyline(conn.getConnection());
            
            conn.setI(1);
        }
    }
    
    
    
    public ControlPointMarker(LatLon latlon) {
        setPosition(latlon);
        setIconUrl("VAADIN/img/controlPoint.png");
                
        btnDeleteControlPoint = new Button("Delete Control Point");
        btnDeleteControlPoint.addClickListener(new ButtonDeleteClickListener(this));
        
        btnDeleteConnection = new Button("Delete Connection");
        btnDeleteConnection.addClickListener(new ButtonDeleteConnection(this));
        
        infoWindow = new GoogleMapInfoWindow("Control point", this);
    }
    
    public void setInfoWindows(GoogleMapInfoWindow infoWindows) {
        this.infoWindow = infoWindows;
    }
    
    public GoogleMapInfoWindow getInfoWindows() {
        return infoWindow;
    }
    
    public void setConnection(Connection conn) {
        this.conn = conn;
    }
    
    public Connection getConnection() {
        return conn;
    }
    
    public Button getBtnDeleteControlPoint() {
        return btnDeleteControlPoint;
    }
    
    public Button getBtnDeleteConnection() {
        return btnDeleteConnection;
    }
}

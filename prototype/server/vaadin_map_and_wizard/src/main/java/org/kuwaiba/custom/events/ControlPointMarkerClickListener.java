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
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import java.util.List;
import org.kuwaiba.connection.Connection;
import org.kuwaiba.connection.ConnectionUtils;
import org.kuwaiba.custom.map.buttons.ConnectionButton;
import org.kuwaiba.custom.overlays.ControlPointMarker;
import org.kuwaiba.custom.polyline.Edge;
import org.kuwaiba.utils.Constants;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ControlPointMarkerClickListener implements MarkerClickListener {
    public final VaadinSession session;
    private final GoogleMap googleMap;
    
    private ControlPointMarker controlPointClicked;
    
    public ControlPointMarkerClickListener(GoogleMap googleMap, VaadinSession session) {
        this.googleMap = googleMap;
        this.session = session;
        
        controlPointClicked = null;
    }
    
    @Override
    public void markerClicked(GoogleMapMarker clickedMarker) {
        if (clickedMarker instanceof ControlPointMarker) {
            if ((Boolean) session.getAttribute(ConnectionButton.NAME)) {
//                googleMap.openInfoWindow(((ControlPointMarker) clickedMarker).getInfoWindows());
                ControlPointMarker controlPoint = (ControlPointMarker) clickedMarker;
                
                if (controlPointClicked == null) { // repaint polyline
                    controlPointClicked = controlPoint;
                    
                    GoogleMapPolyline polyline = controlPoint.getConnection().getEdge();
                    polyline.setStrokeColor(Constants.selectedConnColor);
                    repaintConnection(controlPoint.getConnection());
                }
                else {
                    if (controlPointClicked.equals(controlPoint)) {
                        List<ControlPointMarker> controlPoints = controlPoint.getConnection().getControlPoints();
                        
                        if (isDummyControlPoint(controlPoint, controlPoints)) // Dummy control point
                            deleteConnection(controlPoint);
                        else // control point
                            deleteControlPoint(controlPoint);
                    }
                    else {
                        GoogleMapPolyline polyline = controlPoint.getConnection().getEdge();
                        polyline.setStrokeColor(Constants.unselectedConnColor);
                        repaintConnection(controlPoint.getConnection());
                    }
                    controlPointClicked = null;
                }
            }
        }
    }
    
    private boolean isDummyControlPoint(ControlPointMarker controlPoint, List<ControlPointMarker> controlPoints) {
        int index = controlPoints.indexOf(controlPoint);
        
        return !(index % 2 == 0);
    }
    
    private void deleteControlPoint(ControlPointMarker controlPoint) {
        Connection connection = controlPoint.getConnection();
        List<ControlPointMarker> controlPoints = connection.getControlPoints();
        
        int cpIndex = controlPoints.indexOf(controlPoint);
        int leftcpIndex = cpIndex - 2;
        int rightcpIndex = cpIndex + 2;
        
        ControlPointMarker leftcp = controlPoints.get(leftcpIndex);
        ControlPointMarker rightcp = controlPoints.get(rightcpIndex);
        
        LatLon latlon = ConnectionUtils.midPoint(leftcp.getPosition(), rightcp.getPosition());
        
        ControlPointMarker newDummycp = new ControlPointMarker(latlon, connection.getEdges());
        newDummycp.setIconUrl(Constants.dummyControlPointIconUrl);
        newDummycp.setConnection(connection);
        
        ControlPointMarker rightcpDummy = controlPoints.get(rightcpIndex - 1);
        ControlPointMarker leftcpDummy = controlPoints.get(leftcpIndex + 1);
        
        connection.getControlPoints().remove(rightcpDummy); // remove right dummy control point
        connection.getControlPoints().remove(controlPoint);        
        connection.getControlPoints().remove(leftcpDummy); // remove left dummy control point
        
        connection.getControlPoints().add(leftcpIndex + 1, newDummycp);
        
        googleMap.removeMarker(rightcpDummy);
        googleMap.removeMarker(controlPoint);
        googleMap.removeMarker(leftcpDummy);
        googleMap.addMarker(newDummycp);
        
        repaintConnection(connection);
    }
    
    private void deleteConnection(ControlPointMarker controlPoint) {
        Connection connection = controlPoint.getConnection();
        
        connection.getSource().getConnections().remove(connection);
        connection.getTarget().getConnections().remove(connection);
        connection.getEdges().remove(connection);
        
        for (ControlPointMarker cp : connection.getControlPoints())
            googleMap.removeMarker(cp);
        googleMap.removePolyline(connection.getEdge());
    }
    
    private void repaintConnection(Connection connection) {
        GoogleMapPolyline oldPolyline = connection.getEdge();
        googleMap.removePolyline(oldPolyline);
        
        Edge edge = new Edge(connection);
                            
        String strokeColor = oldPolyline.getStrokeColor();
        edge.setStrokeColor(strokeColor);
        edge.setStrokeOpacity(Constants.connStrokeOpacity);
        edge.setStrokeWeight(Constants.connStrokeWeight);
                    
        for (ControlPointMarker cp : connection.getControlPoints())
            edge.getCoordinates().add(cp.getPosition());
                    
        connection.setConnection(edge);
        googleMap.addPolyline(edge);
    }
}

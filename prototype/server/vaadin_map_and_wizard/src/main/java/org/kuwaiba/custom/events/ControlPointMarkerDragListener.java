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
import java.util.List;
import org.kuwaiba.connection.Connection;
import org.kuwaiba.connection.ConnectionUtils;
import org.kuwaiba.custom.overlays.ControlPointMarker;
import org.kuwaiba.custom.polyline.Edge;
import org.kuwaiba.utils.Constants;

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
                ControlPointMarker controlPoint = (ControlPointMarker) draggedMarker;
                
                if (isDummyControlPoint(controlPoint))
                    addControlPoint(controlPoint);
                else
                    repaintEdge(controlPoint);
            }
    }
    
    private boolean isDummyControlPoint(ControlPointMarker draggedMarker) {
        int index = draggedMarker.getConnection().getControlPoints().indexOf(draggedMarker);
        
        if (index % 2 == 0)
            return false;
        return true;
    }
    
    private void addControlPoint(ControlPointMarker controlPoint) {
        Connection connection = controlPoint.getConnection();
        List<ControlPointMarker> controlPoints = connection.getControlPoints();
        
        
        int controlPointIdx = controlPoints.indexOf(controlPoint);
        
        int leftControlPointIdx = controlPointIdx - 1;
        int rightControlPointIdx = controlPointIdx + 1;
        
        ControlPointMarker leftControlPoint = controlPoints.get(leftControlPointIdx);
        ControlPointMarker rightControlPoint = controlPoints.get(rightControlPointIdx);
        
        LatLon leftCoordinate = ConnectionUtils.midPoint(leftControlPoint.getPosition(), controlPoint.getPosition());
        LatLon rightCoordinate = ConnectionUtils.midPoint(controlPoint.getPosition(), rightControlPoint.getPosition());
        
        ControlPointMarker leftDummy = new ControlPointMarker(leftCoordinate, connection.getEdges());
        leftDummy.setConnection(connection);
        leftDummy.setIconUrl(Constants.dummyControlPointIconUrl);        
        googleMap.addMarker(leftDummy);
        
        ControlPointMarker rightDummy = new ControlPointMarker(rightCoordinate, connection.getEdges());
        rightDummy.setConnection(connection);
        rightDummy.setIconUrl(Constants.dummyControlPointIconUrl);
        googleMap.addMarker(rightDummy);
        
        controlPoints.add(rightControlPointIdx, rightDummy);
        controlPoints.add(controlPointIdx, leftDummy);
        
        addNewPolyline(controlPoint);
    }
    
    private void repaintEdge(ControlPointMarker controlPoint) {
        Connection connection = controlPoint.getConnection();
        List<ControlPointMarker> controlPoints = connection.getControlPoints();
        
        int controlPointIdx = controlPoints.indexOf(controlPoint);
        
        int leftControlPointIdx = controlPointIdx - 2;
        int rightControlPointIdx = controlPointIdx + 2;
        
        ControlPointMarker leftControlPoint = controlPoints.get(leftControlPointIdx);
        ControlPointMarker rightControlPoint = controlPoints.get(rightControlPointIdx);
        
        LatLon leftCoordinate = ConnectionUtils.midPoint(leftControlPoint.getPosition(), controlPoint.getPosition());
        LatLon rightCoordinate = ConnectionUtils.midPoint(controlPoint.getPosition(), rightControlPoint.getPosition());
        
        int leftDummyIdx = controlPointIdx - 1;
        ControlPointMarker oldLeftDummy = controlPoints.get(leftDummyIdx);
        googleMap.removeMarker(oldLeftDummy);
        
        int rightDummyIdx = controlPointIdx + 1;
        ControlPointMarker oldRightDummy = controlPoints.get(rightDummyIdx);
        googleMap.removeMarker(oldRightDummy);
        
        ControlPointMarker newLeftDummy = new ControlPointMarker(leftCoordinate, connection.getEdges());
        newLeftDummy.setConnection(connection);
        newLeftDummy.setIconUrl(Constants.dummyControlPointIconUrl);        
        googleMap.addMarker(newLeftDummy);
        
        ControlPointMarker newRightDummy = new ControlPointMarker(rightCoordinate, connection.getEdges());
        newRightDummy.setConnection(connection);
        newRightDummy.setIconUrl(Constants.dummyControlPointIconUrl);
        googleMap.addMarker(newRightDummy);
        
        controlPoints.add(rightControlPointIdx, newRightDummy);
        controlPoints.add(controlPointIdx, newLeftDummy);
        
        controlPoints.remove(oldRightDummy);
        controlPoints.remove(oldLeftDummy);
        addNewPolyline(controlPoint);
    }
    
    private void addNewPolyline(ControlPointMarker controlPoint) {
        controlPoint.setIconUrl(Constants.controlPointIconUrl);
        
        Connection connection = controlPoint.getConnection();
        List<ControlPointMarker> controlPoints = connection.getControlPoints();
        
        GoogleMapPolyline oldPolyline = connection.getEdge();
        googleMap.removePolyline(oldPolyline);
        
        Edge edge = new Edge(connection);
                        
        edge.setStrokeColor(oldPolyline.getStrokeColor());
        edge.setStrokeOpacity(oldPolyline.getStrokeOpacity());
        edge.setStrokeWeight(oldPolyline.getStrokeWeight());
        
        for (ControlPointMarker cp : controlPoints)
            edge.getCoordinates().add(cp.getPosition());
        
        connection.setConnection(edge);
        googleMap.addPolyline(edge);
    }
}

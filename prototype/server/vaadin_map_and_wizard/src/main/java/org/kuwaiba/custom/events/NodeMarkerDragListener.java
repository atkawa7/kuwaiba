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

import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.MarkerDragListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import java.util.List;
import org.kuwaiba.connection.Connection;
import org.kuwaiba.connection.ConnectionUtils;
import org.kuwaiba.custom.overlays.ControlPointMarker;
import org.kuwaiba.custom.overlays.NodeMarker;
import org.kuwaiba.custom.polyline.Edge;
import org.kuwaiba.utils.Constants;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class NodeMarkerDragListener implements MarkerDragListener {
    ControlPointMarkerDragListener dragListener;
    List<Connection> edges;
    
    public NodeMarkerDragListener(ControlPointMarkerDragListener dragListener, List<Connection> edges) {
        this.dragListener = dragListener;
        this.edges = edges;
    }
    
    @Override
    public void markerDragged(GoogleMapMarker draggedMarker, LatLon oldPosition) {
        if (draggedMarker instanceof NodeMarker) {
            NodeMarker nodeMarker = (NodeMarker) draggedMarker;
            
            for (Connection connection : nodeMarker.getConnections()) {
                if (connection.getSource().equals(nodeMarker))
                    markerDragSource(nodeMarker.getPosition(), connection);
                
                if (connection.getTarget().equals(nodeMarker))
                    markerDragTarget(nodeMarker.getPosition(), connection);
            }
        }
    }
    
    private void markerDragSource(LatLon newPosition, Connection connection) {
        ControlPointMarker controlPoint = new ControlPointMarker(newPosition, connection.getEdges());
        controlPoint.setConnection(connection);
        
        int cpIndex = 0;
        int dummycpIndex = 1;
        int othercpIndex = 2;
        
        ControlPointMarker othercp = connection.getControlPoints().get(othercpIndex);
        
        connection.getMap().removeMarker(connection.getControlPoints().get(dummycpIndex));
        connection.getControlPoints().remove(dummycpIndex);
        connection.getControlPoints().remove(cpIndex);
        
        LatLon latlon = ConnectionUtils.midPoint(controlPoint.getPosition(), othercp.getPosition());
        
        ControlPointMarker dummyControlPoint = new ControlPointMarker(latlon, connection.getEdges());
        dummyControlPoint.setConnection(connection);
        dummyControlPoint.setIconUrl(Constants.dummyControlPointIconUrl);
        
        connection.getMap().addMarker(dummyControlPoint);
        connection.getControlPoints().add(cpIndex, controlPoint);
        connection.getControlPoints().add(dummycpIndex, dummyControlPoint);
        
        repaintConnection(connection);
    }
    
    private void markerDragTarget(LatLon newPosition, Connection connection) {
        ControlPointMarker controlPoint = new ControlPointMarker(newPosition, connection.getEdges());
        controlPoint.setConnection(connection);
        
        int cpIndex = connection.getControlPoints().size() - 1;
        int dummycpIndex = cpIndex - 1;
        int othercpIndex = cpIndex -2;
        
        connection.getMap().removeMarker(connection.getControlPoints().get(dummycpIndex));
        connection.getControlPoints().remove(cpIndex);
        connection.getControlPoints().remove(dummycpIndex);
                
        ControlPointMarker othercp = connection.getControlPoints().get(othercpIndex);
        LatLon latlon = ConnectionUtils.midPoint(controlPoint.getPosition(), othercp.getPosition());
        
        ControlPointMarker dummyControlPoint = new ControlPointMarker(latlon, connection.getEdges());
        dummyControlPoint.setConnection(connection);
        dummyControlPoint.setIconUrl(Constants.dummyControlPointIconUrl);
        
        connection.getMap().addMarker(dummyControlPoint);
        connection.getControlPoints().add(dummyControlPoint);
        connection.getControlPoints().add(controlPoint);
        
        repaintConnection(connection);
    }
    
    private void repaintConnection(Connection connection) {
        Edge oldEdge = connection.getEdge();
        
        Edge newEdge = new Edge(connection);
        
        newEdge.setStrokeColor(oldEdge.getStrokeColor());
        newEdge.setStrokeOpacity(oldEdge.getStrokeOpacity());
        newEdge.setStrokeWeight(oldEdge.getStrokeWeight());
        
        for (ControlPointMarker controlPoint : connection.getControlPoints())
            newEdge.getCoordinates().add(controlPoint.getPosition());
        
        connection.setConnection(newEdge);
        
        connection.getMap().removePolyline(oldEdge);
        connection.getMap().addPolyline(newEdge);
    }
}

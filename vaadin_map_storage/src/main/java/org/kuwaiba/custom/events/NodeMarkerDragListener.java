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
import org.kuwaiba.custom.overlays.ControlPointMarker;
import org.kuwaiba.custom.overlays.NodeMarker;

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
                if (((NodeMarker) draggedMarker).equals(connection.getSource())) {
                    ControlPointMarker oldSourceControlPoint = connection.getControlPoints().get(0);
                    connection.getMap().removeMarker(oldSourceControlPoint);
                    connection.getControlPoints().remove(0);

                    ControlPointMarker sourceControlPoint = new ControlPointMarker(draggedMarker.getPosition(), edges);
                    sourceControlPoint.setInfoWindows(oldSourceControlPoint.getInfoWindows());

                    connection.getMap().addMarker(sourceControlPoint);
                    connection.getControlPoints().add(0, sourceControlPoint);
                    sourceControlPoint.setConnection(connection);
                    dragListener.markerDragged(connection.getControlPoints().get(0), oldPosition);
                }
                if (((NodeMarker) draggedMarker).equals(connection.getTarget())) {
                    int lastIndex = connection.getControlPoints().size() - 1;

                    ControlPointMarker oldSourceControlPoint = connection.getControlPoints().get(lastIndex);
                    connection.getMap().removeMarker(oldSourceControlPoint);

                    connection.getControlPoints().remove(lastIndex);

                    ControlPointMarker sourceControlPoint = new ControlPointMarker(draggedMarker.getPosition(), edges);
                    sourceControlPoint.setInfoWindows(oldSourceControlPoint.getInfoWindows());

                    connection.getMap().addMarker(sourceControlPoint);
                    connection.getControlPoints().add(lastIndex, sourceControlPoint);
                    sourceControlPoint.setConnection(connection);
                    dragListener.markerDragged(connection.getControlPoints().get(lastIndex), oldPosition);
                }
            }
        }
    }
}

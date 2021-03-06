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
import com.vaadin.ui.Notification;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.custom.overlays.ControlPointMarker;
import org.kuwaiba.custom.overlays.NodeMarker;
import org.kuwaiba.custom.polyline.Edge;
import org.kuwaiba.utils.Constants;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Connection {
    private String connectionColor;
    
    private GoogleMap googleMap;
    
    private NodeMarker source;
    private NodeMarker target;
    private Edge edge;
    private List<ControlPointMarker> controlPoints;
    private List<Connection> edges;
    
    private int i;
    
    public Connection(GoogleMap googleMap, List<Connection> edges) {
        i = 1;
        this.googleMap = googleMap;
        controlPoints = new ArrayList();
        edge = new Edge(this);
        this.edges = edges;
        source = null;
        target = null;
        
        connectionColor = Constants.defaultConnectionColor;
    }
    
    public void setStartAndEndControlPoints(NodeMarker nodeMarker) {
        if (i == 1) {
            source = nodeMarker;
            source.getConnections().add(this);
            
            ControlPointMarker controlPointMarker = new ControlPointMarker(nodeMarker.getPosition(), edges);
            controlPointMarker.setConnection(this);
            controlPointMarker.setDraggable(true);
            controlPoints.add(controlPointMarker);            
            i = 2;
            
            Notification.show("Source setted");
            return;
        }
        if (i == 2) {
            target = nodeMarker;
            target.getConnections().add(this);
            
            ControlPointMarker controlPointMarker = new ControlPointMarker(nodeMarker.getPosition(), edges);
            controlPointMarker.setConnection(this);
            controlPointMarker.setDraggable(true);
            controlPoints.add(controlPointMarker);            
            i = 0;
            
            Notification.show("Target setted");
            return;
        }
    }
        
    public void setControlPoints(List<ControlPointMarker> controlPoints) {
        this.controlPoints = controlPoints;
    }
    
    public List<ControlPointMarker> getControlPoints() {
        return controlPoints;
    }
    
    public int getI() {
        return i;
    }
    
    public void setI(int i) {
        this.i = i;
        source = null;
        target = null;
        controlPoints.removeAll(controlPoints);
    }
    
    public void setConnection(Edge edge) {
        this.edge = edge;
    }
    
    public Edge getEdge() {
        return edge;
    }
    
    public void setSource(NodeMarker source) {
        this.source = source;
    }
    
    public NodeMarker getSource() {
        return source;
    }
    
    public void setTarget(NodeMarker target) {
        this.target = target;
    }
    
    public NodeMarker getTarget() {
        return target;        
    }
    
    public GoogleMap getMap() {
        return googleMap;
    }
    
    public List<Connection> getEdges() {
        return edges;
    }
    
    public String getColor() {
        return connectionColor;
    }
}


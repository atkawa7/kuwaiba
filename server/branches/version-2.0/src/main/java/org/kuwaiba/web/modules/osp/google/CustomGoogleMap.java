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
package org.kuwaiba.web.modules.osp.google;

import com.google.common.eventbus.Subscribe;
import org.kuwaiba.web.modules.osp.google.overlays.NodeMarker;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.events.MapClickListener;
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kuwaiba.apis.web.gui.modules.EmbeddableComponent;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.modules.osp.google.overlays.ConnectionPolyline;
import org.kuwaiba.web.custom.wizards.physicalconnection.PhysicalConnectionConfiguration;
import org.kuwaiba.web.modules.osp.events.PolylineClickListenerImpl;
import org.kuwaiba.web.modules.osp.events.MarkerDblClickListenerImpl;
import org.kuwaiba.web.modules.osp.events.MarkerDragListenerImpl;
import org.kuwaiba.web.modules.osp.events.PolygonClickListenerImpl;
import org.kuwaiba.web.modules.osp.events.PolygonDblClickListenerImpl;
import org.kuwaiba.web.modules.osp.events.PolylineDblClickListenerImpl;
import org.kuwaiba.web.modules.osp.google.overlays.Marker;
import org.kuwaiba.web.modules.osp.google.overlays.PointMarker;
import org.kuwaiba.web.modules.osp.google.overlays.Polygon;
import org.kuwaiba.web.modules.osp.google.overlays.Polyline;
import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;

/**
 * Custom GoogleMap for Kuwaiba
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CustomGoogleMap extends GoogleMap implements EmbeddableComponent, PropertyChangeListener {
    private final TopComponent parentComponent;
    /**
     * List of connections for node
     */
    private Map<NodeMarker, List<ConnectionPolyline>> connectionsForNode = new HashMap<>();
    
    private ConnectionPolyline newConnection = null;
    private Polygon newPolygon = null;
    
    private boolean connectionEnable = false;
    private boolean drawPolygonEnable = false;
    
    public CustomGoogleMap(TopComponent parentComponent, String apiKey, String clientId, String language) {
        super(apiKey, clientId, language);
        this.parentComponent = parentComponent;
        
        addMapClickListener(new MapClickListenerImpl());
                
        addMarkerClickListener(new MarkerClickListenerImpl());
        addMarkerDblClickListener(new MarkerDblClickListenerImpl());
        addMarkerDragListener(new MarkerDragListenerImpl());
        
        addPolygonClickListener(new PolygonClickListenerImpl());
        addPolygonDblClickListener(new PolygonDblClickListenerImpl());
        
        addPolylineClickListener(new PolylineClickListenerImpl());
        addPolylineDblClickListener(new PolylineDblClickListenerImpl());
    }
    
    public void addNodeMarker(RemoteObjectLight node) {
        if (getState().markers.containsKey(node.getOid())) {
            Notification.show("The map containt the object", Notification.Type.WARNING_MESSAGE);
            return;
        }
        WebserviceBeanLocal wsBean = getTopComponent().getWsBean();
        String ipAddress = getUI().getPage().getWebBrowser().getAddress();
        String sessioId = getTopComponent().getApplicationSession().getSessionId();
                
        if (!wsBean.isSubclassOf(node.getClassName(), "ViewableObject", ipAddress, sessioId)) { //NOI18N
            Notification.show("Only ViewableObject are allowed", Notification.Type.WARNING_MESSAGE);
            return;
        }
        NodeMarker nodeMarker = new NodeMarker(node);
        nodeMarker.setId(node.getOid());
        nodeMarker.setCaption(node.toString());
        nodeMarker.setPosition(getCenter());
        
        nodeMarker.addPropertyChangeListener(this);
        
        getState().markers.put(node.getOid(), nodeMarker);
    }
    
    public void register() {
        if (parentComponent != null)
            parentComponent.getEventBus().register(this);
    }
    
    public void unregister() {
        if (parentComponent != null)
            parentComponent.getEventBus().unregister(this);
    }

    @Override
    public TopComponent getTopComponent() {
        return parentComponent;
    }
        
    @Subscribe
    public void enableTool(Button.ClickEvent event) {
        if ("Connect".equals(event.getButton().getDescription()))
            connectionEnable = !connectionEnable;
        if ("Draw polygon".equals(event.getButton().getDescription()))
            drawPolygonEnable = !drawPolygonEnable;
    }
    
    @Subscribe
    public void addConnectionPolyline(WizardCancelledEvent event) {
        removeComponent(newConnection.getWizard());
        newConnection = null;                
    }
        
    @Subscribe
    public void addConnectionPolyline(WizardCompletedEvent event) {
        PhysicalConnectionConfiguration connConfig = newConnection.getWizard().getConnectionConfiguration();
        
        newConnection.setCaption(connConfig.getCaption());
        newConnection.setStrokeColor(connConfig.getStrokeColor());
        newConnection.setStrokeOpacity(connConfig.getStrokeOpacity());
        newConnection.setStrokeWeight(connConfig.getStrokeWeight());
                
        RemoteObjectLight aRbo = newConnection.getSource().getRemoteObjectLight();
        RemoteObjectLight bRbo = newConnection.getTarget().getRemoteObjectLight();
                
        String [] names = null;
        String [][] values = null;
        
        String name = connConfig.getCaption(); // connection name
        
        long typeOid = connConfig.getTypeOid();
        if (typeOid == 0) {
            names = new String[]{"name"};
            values = new String[][]{new String[]{name}};
        }
        else {
            String type = Long.toString(connConfig.getTypeOid());
            
            names = new String[]{"name", "type"};
            values = new String[][]{new String[]{name}, new String[]{type}};
        }
        RemoteObject commonParent = null;
        long connectionId = -1L;
        
        String connectionClass = connConfig.getConnectionClass();
        String errorMessage = "";
        
        WebserviceBeanLocal wsBean = getTopComponent().getWsBean();
        String ipAddress = getUI().getPage().getWebBrowser().getAddress();
        String sessioId = getTopComponent().getApplicationSession().getSessionId();
        
        try {        
            commonParent = wsBean.getCommonParent(aRbo.getClassName(), aRbo.getOid(), bRbo.getClassName(), bRbo.getOid(), ipAddress, sessioId);
            connectionId = wsBean.createPhysicalConnection(aRbo.getClassName(), aRbo.getOid(), bRbo.getClassName(), bRbo.getOid(), commonParent.getClassName(), commonParent.getOid(), names, values, connectionClass, ipAddress, sessioId);
        } catch (ServerSideException ex) {
            errorMessage = ex.getMessage();
        }   
        if (connectionId != -1L) {
            getState().polylines.put(connectionId, newConnection);
                        
            int numberOfChildren = connConfig.getNumChildren();
            if (numberOfChildren > 0) {
                String childrenType = connConfig.getPortType();
                try {
                    wsBean.createBulkPhysicalConnections(childrenType, numberOfChildren, connectionClass, connectionId, ipAddress, sessioId);
                    Notification.show("Children connections were created successfully", Notification.Type.HUMANIZED_MESSAGE);
                } catch (ServerSideException ex) {
                    errorMessage = ex.getMessage();
                    NotificationsUtil.showError(errorMessage);
                }
            }
            Notification.show("The object was created successfully", Notification.Type.HUMANIZED_MESSAGE);
        }
        else
            NotificationsUtil.showError(errorMessage);
        
        removeComponent(newConnection.getWizard());
        newConnection = null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null || evt.getPropertyName() == null)
            return;
        
        if (evt.getPropertyName().equals("endingPolygonDraw")) {
            newPolygon = null;
            return;
        } 
        
        if (evt.getPropertyName().equals("addPolyline")) {
            GoogleMapPolyline gmPolyline = 
                    (GoogleMapPolyline) evt.getNewValue();
            getState().polylines.put(gmPolyline.getId(), gmPolyline);
            return;
        }
        
        if (evt.getPropertyName().equals("updatePolyline")) {
            getState().polylinesChanged
                    .add((GoogleMapPolyline) evt.getNewValue());
            return;
        }
        
        if (evt.getPropertyName().equals("hidePolyline")) {
            GoogleMapPolyline polyline = (GoogleMapPolyline) evt.getNewValue();
            polyline.setVisible(false);
            getState().polylinesChanged.add(polyline);
            return;
        }
        
        if (evt.getPropertyName().equals("showPolyline")) {
            GoogleMapPolyline polyline = (GoogleMapPolyline) evt.getNewValue();
            polyline.setVisible(true);
            getState().polylinesChanged.add(polyline);
            return;
        }
        
        if (evt.getPropertyName().equals("removeMarkers")) {
            List<Marker> markers = 
                    (ArrayList<Marker>) evt.getNewValue();
            for (Marker marker : markers)
                removeMarker((Marker) marker);
            return;
        }
        
        if (evt.getPropertyName().equals("addPolygon")) {
            GoogleMapPolygon gmPolygon = (GoogleMapPolygon) evt.getNewValue();
            getState().polygons.put(gmPolygon.getId(), gmPolygon);
            return;
        }
        
        if (evt.getPropertyName().equals("hidePolygon")) {
            GoogleMapPolygon gmPolygon = (GoogleMapPolygon) evt.getNewValue();
            gmPolygon.setVisible(false);
            getState().polygonsChanged.add(gmPolygon);
            return;
        }
        
        if (evt.getPropertyName().equals("showPolygon")) {
            GoogleMapPolygon gmPolygon = (GoogleMapPolygon) evt.getNewValue();
            gmPolygon.setVisible(true);
            getState().polygonsChanged.add(gmPolygon);
            return;
        }
        
        if (evt.getPropertyName().equals("updatePolygon")) {
            getState().polygonsChanged
                    .add((GoogleMapPolygon) evt.getNewValue());
            return;
        }
        
        if (evt.getPropertyName().equals("addMarker")) {
            GoogleMapMarker marker = (GoogleMapMarker) evt.getNewValue();
            getState().markers.put(marker.getId(), marker);
            return;
        }
        
        if (evt.getPropertyName().equals("removeMarker")) {
            removeMarker((Marker) evt.getNewValue());
            return;
        }
        
        if (evt.getPropertyName().equals("updateMarker")) {
            if (evt.getNewValue() instanceof Marker)
                getState().markersChanged.add((GoogleMapMarker) evt.getNewValue());
            return;
        }
        
        if (evt.getPropertyName().equals("updateMarkers")) {
            getState().markersChanged = 
                    (ArrayList<GoogleMapMarker>) evt.getNewValue();
        }
        
        if (evt.getPropertyName().equals("showMarkers")) {
            List<GoogleMapMarker> markers = (ArrayList<GoogleMapMarker>) evt.getNewValue();
            for (GoogleMapMarker marker : markers)
                marker.setVisible(true);
            getState().markersChanged = markers;
            return;
        }
        
        if (evt.getPropertyName().equals("hideMarkers")) {
            List<GoogleMapMarker> markers = (ArrayList<GoogleMapMarker>) evt.getNewValue();
            for (GoogleMapMarker marker : markers)
                marker.setVisible(false);
            getState().markersChanged = markers;
            return;
        }
        
        if (evt.getPropertyName().equals("removePolygon")) {
            removePolygon((Polygon) evt.getSource());
            return;
        }
        
        if (evt.getPropertyName().equals("removePolyline")) {
            removePolyline((Polyline) evt.getSource());
            return;
        }
    }
    
    private void removeMarker(Marker marker) {
        if (marker instanceof NodeMarker) {
            NodeMarker node = ((NodeMarker) marker);
                        
            if (connectionsForNode.get(node) == null)
                return;
            
            for (ConnectionPolyline conn : connectionsForNode.get(node)) {
                
                if (node.equals(conn.getSource()))
                    connectionsForNode.get(conn.getTarget()).remove(conn);
                
                if (node.equals(conn.getTarget()))
                    connectionsForNode.get(conn.getSource()).remove(conn);
                
                removePolyline(conn);
            }
            connectionsForNode.get(node).clear();            
            
            node.removedFromView(true);
            getState().markers.remove(node.getId());
            return;
        }
        if (marker instanceof PointMarker) {
            if (!marker.getRemoved())
                marker.removeAllPropertyChangeListener();
            
            getState().markers.remove(marker.getId());
            return;
        }
    }
    
    private void removePolygon(Polygon polygon) {
        polygon.getPolyline().removedFromView(true);
        removePolyline(polygon.getPolyline());
        polygon.removedFromView(true);
        getState().polygons.remove(polygon.getId());
    }
    
    private void removePolyline(Polyline polyline) {
        if (polyline instanceof ConnectionPolyline) {
            ConnectionPolyline connection = (ConnectionPolyline) polyline;
                        
            NodeMarker source = connection.getSource();
            source.removePropertyChangeListener(connection);
                        
            NodeMarker target = connection.getTarget();
            target.removePropertyChangeListener(connection);
        }           
        List<PointMarker> points = polyline.getPoints();
        if (!points.isEmpty()) {
            for (Marker point : points) {
                point.removeAllPropertyChangeListener();
                getState().markers.remove(point.getId());
            }
            points.clear();
        }
        if (polyline.getRemoved())
            polyline.removeAllPropertyChangeListener();
        else
            polyline.removedFromView(true);
        
        getState().polylines.remove(polyline.getId());
    }
        
    private class MarkerClickListenerImpl implements MarkerClickListener {
        
        public MarkerClickListenerImpl() {
        }

        @Override
        public void markerClicked(GoogleMapMarker clickedMarker) {
            if (clickedMarker instanceof Marker)
                ((Marker) clickedMarker)
                        .firePropertyChangeEvent("markerClicked", null, null);
            
            if (clickedMarker instanceof NodeMarker) {
                if (connectionEnable) {
                    if (newConnection == null) {
                        newConnection = new ConnectionPolyline((NodeMarker) clickedMarker);
                        Notification.show("Source node of connection", 
                                Notification.Type.HUMANIZED_MESSAGE);
                    }
                    else {
                        newConnection.setTarget((NodeMarker) clickedMarker);
                        Notification.show("Target node of connection", 
                                Notification.Type.HUMANIZED_MESSAGE);
                        /*
                        PhysicalConnectionWizard wizard = new PhysicalConnectionWizard(parentComponent, newConnection);
                        //newConnection.setTarget((NodeMarker) clickedMarker);
                        newConnection.setWizard(wizard);
                        
                        addComponent(wizard);
                                                
                        wizard.setPopupVisible(true);
                                */
                        addNewPolyline(); // remove before method only for test 
                    }
                }
                // for property sheet
                parentComponent.getEventBus().post(clickedMarker);
            }           
        }
    }
    
    private class MapClickListenerImpl implements MapClickListener {
        public MapClickListenerImpl() {
        }

        @Override
        public void mapClicked(LatLon position) {
            if (drawPolygonEnable) {
                PointMarker point = new PointMarker(position, true);
                getState().markers.put(point.getId(), point);
                
                if (newPolygon == null)
                    initNewPolygon(point);
                else
                    newPolygon.addPoint(point);
            }
        }
    }
    
    private void initNewPolygon(PointMarker point) {
        newPolygon = new Polygon(point, this);
    }
    
    private void addNewPolyline() {
        newConnection.setStrokeColor(Polyline.defaultPolylineColor);
        newConnection.setStrokeOpacity(1);
        newConnection.setStrokeWeight(3);

        newConnection.addPropertyChangeListener(this);

        getState().polylines.put(newConnection.getId(), newConnection);
        
        // fix the new connection to the source and target node
        NodeMarker source = newConnection.getSource();
        NodeMarker target = newConnection.getTarget();
        
        if (connectionsForNode.get(source) == null)
            connectionsForNode.put(source, new ArrayList());
        connectionsForNode.get(source).add(newConnection);
        
        if (connectionsForNode.get(target) == null)
            connectionsForNode.put(target, new ArrayList());
        connectionsForNode.get(target).add(newConnection);
        
        newConnection = null;
    }
}

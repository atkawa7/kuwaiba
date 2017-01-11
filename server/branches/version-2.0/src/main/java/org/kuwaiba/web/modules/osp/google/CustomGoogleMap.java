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
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.web.gui.modules.EmbeddableComponent;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.modules.osp.google.overlays.ConnectionPolyline;
import org.kuwaiba.web.custom.wizards.physicalconnection.PhysicalConnectionConfiguration;
import org.kuwaiba.web.modules.osp.events.ConnectionPolylineClickListener;
import org.kuwaiba.web.modules.osp.events.MarkerDragListenerImpl;
import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;

/**
 * Custom GoogleMap for Kuwaiba
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CustomGoogleMap extends GoogleMap implements EmbeddableComponent, PropertyChangeListener {
    private final TopComponent parentComponent;
    /* get this for the getState
    private Map<Long, NodeMarker> nodes = new HashMap<>();
    private Map<Long, ConnectionPolyline> connections = new HashMap<>();
    */
    ConnectionPolyline newConnection = null;
    private boolean connectionEnable = false;
    
    public CustomGoogleMap(TopComponent parentComponent, String apiKey, String clientId, String language) {
        super(apiKey, clientId, language);
        this.parentComponent = parentComponent;
        addMarkerClickListener(new MarkerClickListenerImpl());
        addMarkerDragListener(new MarkerDragListenerImpl());
        addPolylineClickListener(new ConnectionPolylineClickListener());
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
        if (evt.getSource() instanceof ConnectionPolyline) {
            if (evt.getPropertyName().equals("coordinates"))
                getState().polylinesChanged.add((ConnectionPolyline) evt.getSource());
            
            if (evt.getPropertyName().equals("addMarker")) {
                GoogleMapMarker marker = (GoogleMapMarker) evt.getNewValue();
                getState().markers.put(marker.getId(), marker);
            }
            /*
            if (evt.getPropertyName().equals("markerIsVisible")) {
                PointMarker midpoint = (PointMarker) evt.getNewValue();
                getState().markersChanged.add(midpoint);
            }
            */
            if (evt.getPropertyName().equals("updateMarker")) {
                getState().markersChanged.add((GoogleMapMarker) evt.getNewValue());
            }
            
            if (evt.getPropertyName().equals("showMarkers")) {
                List<GoogleMapMarker> markers = (ArrayList<GoogleMapMarker>) evt.getNewValue();
                for (GoogleMapMarker marker : markers)
                    marker.setVisible(true);
                getState().markersChanged = markers;
            }
            if (evt.getPropertyName().equals("hideMarkers")) {
                List<GoogleMapMarker> markers = (ArrayList<GoogleMapMarker>) evt.getNewValue();
                for (GoogleMapMarker marker : markers)
                    marker.setVisible(false);
                getState().markersChanged = markers;
            }
                
            
        }
    }
        
    private class MarkerClickListenerImpl implements MarkerClickListener {
        
        public MarkerClickListenerImpl() {
        }

        @Override
        public void markerClicked(GoogleMapMarker clickedMarker) {
            if (clickedMarker instanceof NodeMarker) {
                if (connectionEnable) {
                    if (newConnection == null)
                        newConnection = new ConnectionPolyline((NodeMarker) clickedMarker);
                    else {
                        newConnection.setTarget((NodeMarker) clickedMarker);
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
                parentComponent.getEventBus().post(clickedMarker);
            }           
        }
    }
    
    private void addNewPolyline() {
//        Long id  = Long.getLong(String.valueOf(getState().polylines.size()));
        newConnection.setStrokeColor("green");
        newConnection.setStrokeOpacity(1);
        newConnection.setStrokeWeight(3);

        newConnection.addPropertyChangeListener(this);

        getState().polylines.put(newConnection.getId(), newConnection);

        newConnection = null;
    }
}

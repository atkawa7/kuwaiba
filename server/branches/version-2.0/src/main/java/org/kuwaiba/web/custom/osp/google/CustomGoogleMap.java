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
package org.kuwaiba.web.custom.osp.google;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.kuwaiba.web.custom.osp.google.overlays.NodeMarker;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import java.util.HashMap;
import java.util.Map;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.web.custom.osp.google.overlays.ConnectionPolyline;
import org.kuwaiba.web.custom.wizards.connection.ConnectionConfiguration;
import org.kuwaiba.web.custom.wizards.connection.FirstStep;
import org.kuwaiba.web.custom.wizards.connection.PopupConnectionWizardView;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;

/**
 * Custom GoogleMap for Kuwaiba
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CustomGoogleMap extends GoogleMap {
    private final EventBus eventBus;
    private Map<Long, NodeMarker> nodes = new HashMap<>();
    Map<Long, ConnectionPolyline> connections = new HashMap<>();
    
    ConnectionPolyline newConnection = null;
    private boolean connectionEnable = false;
    
    public CustomGoogleMap(EventBus eventBus, String apiKey, String clientId, String language) {
        super(apiKey, clientId, language);
        this.eventBus = eventBus;
        
        this.eventBus.register(this);
        addMarkerClickListener(new MarkerClickListenerImpl());
    }
    
    public void addNodeMarker(RemoteBusinessObjectLight node) {
        if (nodes.containsKey(node.getId())) {
            Notification.show(KuwaibaConstants.ADD_MARKER_MESSAGE, Notification.Type.WARNING_MESSAGE);
            return;
        }
        MetadataEntityManager mem = PersistenceService.getInstance().getMetadataEntityManager();
        if (!mem.isSubClass(KuwaibaConstants.CLASS_VIEWABLEOBJECT, node.getClassName())) {
            Notification.show(KuwaibaConstants.VIEWABLEOBJECT_MESSAGE, Notification.Type.WARNING_MESSAGE);
            return;
        }
        NodeMarker nodeMarker = new NodeMarker(node);
        nodeMarker.setCaption(node.toString());
        nodeMarker.setPosition(getCenter());
        addMarker(nodeMarker);
                    
        nodes.put(node.getId(), nodeMarker);
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
                        PopupConnectionWizardView wizard = new PopupConnectionWizardView(eventBus, newConnection);
                        newConnection.setTarget((NodeMarker) clickedMarker);
                        newConnection.setWizard(wizard);
                        
                        addComponent(wizard);
                                                
                        wizard.setPopupVisible(true);
                    }
                }
                eventBus.post(clickedMarker);
            }           
        }
    }
    
    @Subscribe
    public void enableTool(Button.ClickEvent event) {
        if ("Connection".equals(event.getButton().getDescription()))
            connectionEnable = !connectionEnable;
    }
    
    @Subscribe
    public void addConnectionPolyline(WizardCompletedEvent event) {
        // change for the object node id
        ConnectionConfiguration connConfig = ((FirstStep) event.getWizard().getSteps().get(0)).getConnConfig();        
        Long id = Long.valueOf(String.valueOf(connections.size()));
        newConnection.setCaption(connConfig.getCaption());
        newConnection.setStrokeColor(connConfig.getStrokeColor());
        newConnection.setStrokeOpacity(connConfig.getStrokeOpacity());
        newConnection.setStrokeWeight(connConfig.getStrokeWeight());
        
        connections.put(id, newConnection);
                        
        addPolyline(newConnection);
        removeComponent(newConnection.getWizard());
        newConnection = null;
    }
}

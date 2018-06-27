/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Notification;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.kuwaiba.apis.web.gui.modules.EmbeddableComponent;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * This wrapper contain the GoogleMap.
 * Give the power of drag and drop elements to the map
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MapComponentWrapper extends DragAndDropWrapper implements EmbeddableComponent {
    private DropHandler dropHandler = new DropHandler() {

        @Override
        public void drop(DragAndDropEvent event) {
//            Object object = event.getTransferable().getData("itemId");
//            if (object instanceof InventoryObjectNode) {
//                InventoryObjectNode objectNode = (InventoryObjectNode) object;
//                                
//                map.addNodeMarker((RemoteObjectLight) objectNode.getObject());
//            }
//            else
//                NotificationsUtil.showError("Only inventory objects are allowed to be dropped here");
        }

        @Override
        public AcceptCriterion getAcceptCriterion() {
            return AcceptAll.get();
        }
    };
        
    public static String CLASS_VIEW = "OutsidePlantModuleView";
    private TopComponent parentComponent;
    private OSPTopComponent map;
    
    private RemoteViewObject currentView;
    private boolean viewClosedByNewView = false;
    private boolean viewClosedByOpenView = false;
    
    public MapComponentWrapper(TopComponent parentComponent) {
        this.parentComponent = parentComponent;
        
        try {
            Context context = new InitialContext();
            String apiKey = (String)context.lookup("java:comp/env/googleMapsApiKey"); //NOI18N
            String mapLanguage = (String)context.lookup("java:comp/env/mapLanguage"); //NOI18N
            double longitude = (double)context.lookup("java:comp/env/defaultCenterLongitude"); //NOI18N
            double latitude = (double)context.lookup("java:comp/env/defaultCenterLatitude"); //NOI18N
            int zoom = (int)context.lookup("java:comp/env/defaultZoom"); //NOI18N
            
            map = new OSPTopComponent(parentComponent, apiKey, null, mapLanguage);
            map.setCenter(new LatLon(latitude, longitude));
            map.setZoom(zoom);
            map.setSizeFull();
            
            setCompositionRoot(map);
            setDropHandler(dropHandler);
        } catch (NamingException ex){
            System.out.println(String.format("[KUWAIBA] Unexpected exception while loading a map in the OSP module: %s", ex.getMessage()));
            Notification.show("An error occurred while loading the map default settings. Please contact your administrator", 
                    Notification.Type.ERROR_MESSAGE);
        }
    }
    
    public OSPTopComponent getMap() {
        return map;
    }
    
    public RemoteViewObject getCurrentView() {
        return currentView;
    }
    
    public void setCurrentView(RemoteViewObject currentView) {
        this.currentView = currentView;
    }
        
    public boolean isViewClosedByNewView() {
        return viewClosedByNewView;
    }
    
    public void setViewClosedByNewView(boolean viewClosedByNewView) {
        this.viewClosedByNewView = viewClosedByNewView;
    }
    
    public boolean isViewClosedByOpenView() {
        return viewClosedByOpenView;
    }
    
    public void setViewClosedByOpenView(boolean viewClosedByOpenView) {
        this.viewClosedByOpenView = viewClosedByOpenView;
    }
    
    public void initNewMap() {
        viewClosedByNewView = false;
        viewClosedByOpenView = false;
        currentView = null;
        map.newMap();
    }
    
    public void register() {
        if (parentComponent != null) {
            parentComponent.getEventBus().register(this);
            map.register();
        }
    }
    
    public void unregister() {
        if (parentComponent != null) {
            parentComponent.getEventBus().unregister(this);
            map.unregister();
        }
    }
    
    @Override
    public TopComponent getTopComponent() {
        return parentComponent;
    }
}

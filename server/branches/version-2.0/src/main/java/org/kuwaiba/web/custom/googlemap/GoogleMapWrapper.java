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
package org.kuwaiba.web.custom.googlemap;

import com.google.common.eventbus.EventBus;
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
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.web.gui.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.nodes.ObjectNode;
import org.kuwaiba.apis.web.gui.nodes.util.NotificationsUtil;

/**
 * Custom GoogleMap for Kuwaiba
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class GoogleMapWrapper extends DragAndDropWrapper {
    private CustomGoogleMap map;
    
    public GoogleMapWrapper(final EventBus eventBus) {
        try {
            Context context = new InitialContext();
            String apiKey = (String)context.lookup("java:comp/env/googleMapsApiKey"); //NOI18N
            String mapLanguage = (String)context.lookup("java:comp/env/mapLanguage"); //NOI18N
            double longitude = (double)context.lookup("java:comp/env/defaultCenterLongitude"); //NOI18N
            double latitude = (double)context.lookup("java:comp/env/defaultCenterLatitude"); //NOI18N
            int zoom = (int)context.lookup("java:comp/env/defaultZoom"); //NOI18N
            
            map = new CustomGoogleMap(eventBus, apiKey, null, mapLanguage);
            map.setCenter(new LatLon(latitude, longitude));
            map.setZoom(zoom);
            map.setSizeFull();
            
            setCompositionRoot(map);
            setDropHandler(new DropHandlerImpl());
        } catch (NamingException ex){
            Notification.show("An error occurred while loading the map default settings. Please contact your administrator", 
                    Notification.Type.ERROR_MESSAGE);
        }
    }
    
    private class DropHandlerImpl implements DropHandler {
        
        public DropHandlerImpl() {
        }

        @Override
        public void drop(DragAndDropEvent event) {
            AbstractNode transferable = (AbstractNode)event.getTransferable().getData("itemId");
            if (transferable.getObject() instanceof RemoteBusinessObjectLight) {
                map.addNodeMarker(((RemoteBusinessObjectLight)transferable.getObject()));
                /*
                map.addMarker(((RemoteBusinessObjectLight)transferable.getObject()).toString(), 
                        map.getCenter(), true, null);
                        */
            }
            else
                NotificationsUtil.showError("Only inventory objects are allowed to be dropped here");
        }
        
        @Override
        public AcceptCriterion getAcceptCriterion() {
            return AcceptAll.get();
        }
    }
}

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
package org.kuwaiba.web.custom.osp;

import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.VerticalLayout;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.web.gui.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.nodes.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.nodes.util.NotificationsUtil;
import org.kuwaiba.web.custom.core.AbstractTooledComponent;
import org.kuwaiba.web.custom.wizards.connection.PopupConnectionWizardView;

/**
 * GISView implementation for Google Maps
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class GoogleMapsGISView extends AbstractTooledComponent implements AbstractGISView {
    private VerticalLayout lytMapComponent;
    private GoogleMap map;
    private PopupConnectionWizardView wzdPhysicalConnections;
    
    
    public GoogleMapsGISView() {
        super(new AbstractAction[0], AbstractTooledComponent.TOOLBAR_ORIENTATION_HORIZONTAL, 
                    AbstractTooledComponent.ToolBarSize.NORMAL);
        try {
            Context context = new InitialContext();
            String apiKey = (String)context.lookup("java:comp/env/googleMapsApiKey"); //NOI18N
            String mapLanguage = (String)context.lookup("java:comp/env/mapLanguage"); //NOI18N
            double longitude = (double)context.lookup("java:comp/env/defaultCenterLongitude"); //NOI18N
            double latitude = (double)context.lookup("java:comp/env/defaultCenterLatitude"); //NOI18N
            int zoom = (int)context.lookup("java:comp/env/defaultZoom"); //NOI18N
            wzdPhysicalConnections = new PopupConnectionWizardView();
            
            map = new GoogleMap(apiKey, null, mapLanguage);
            map.setCenter(new LatLon(latitude, longitude));
            map.setZoom(zoom);
            map.setSizeFull();
            
            DragAndDropWrapper dndWrapper = new DragAndDropWrapper(map);
            dndWrapper.setDropHandler(new DropHandler() {

                @Override
                public void drop(DragAndDropEvent event) {
                    AbstractNode transferable = (AbstractNode)event.getTransferable().getData("itemId");

                    if (transferable.getObject() instanceof RemoteBusinessObjectLight) {

                        map.addMarker(((RemoteBusinessObjectLight)transferable.getObject()).toString(), 
                                map.getCenter(), true, null);

                    } else
                        NotificationsUtil.showError("Only inventory objects are allowed to be dropped here");
                }

                @Override
                public AcceptCriterion getAcceptCriterion() {
                    return AcceptAll.get();
                }
            });

            dndWrapper.setSizeFull();
            setMainComponent(dndWrapper);
            
        } catch (NamingException ex){
            NotificationsUtil.showError("An error occurred while loading the map default settings. Please contact your administrator");
        }
    }

    @Override
    public String getName() {
        return "OSP Module for Google Maps";
    }

    @Override
    public String getDescription() {
        return "OSP Module that uses Google Maps as map provider";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS";
    }
}

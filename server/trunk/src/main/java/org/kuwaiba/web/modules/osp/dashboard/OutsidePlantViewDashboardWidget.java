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

package org.kuwaiba.web.modules.osp.dashboard;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.DropTargetExtension;
import com.vaadin.ui.dnd.event.DropEvent;
import com.vaadin.ui.dnd.event.DropListener;
import java.util.HashMap;
import java.util.Optional;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.dashboards.DashboardEventBus;
import org.kuwaiba.apis.web.gui.dashboards.DashboardEventListener;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A widget that displays a map and allows to drop elements from a navigation tree and create physical connections
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class OutsidePlantViewDashboardWidget extends AbstractDashboardWidget {
    /**
     * Default map widget
     */
    private GoogleMap mapMain;
    /**
     * A map with the existing nodes
     */
    private HashMap<Long, RemoteObjectLight> nodes;
    
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;
    public OutsidePlantViewDashboardWidget(DashboardEventBus eventBus, WebserviceBean wsBean) {
        super("Outside Plant Viewer", eventBus);
        nodes = new HashMap<>();
        createContent();
        setSizeFull();
    }

    
    
    @Override
    public void createCover() {
        throw new UnsupportedOperationException("This widget supports only embedded mode"); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void createContent() {
        try {
            
            Context context = new InitialContext();
            String apiKey = (String)context.lookup("java:comp/env/googleMapsApiKey");
            String language = (String)context.lookup("java:comp/env/mapLanguage");

            mapMain = new GoogleMap(apiKey, null, language);
            mapMain.setSizeFull();
            
            //Enable the tree as a drop target
            DropTargetExtension<GoogleMap> dropTarget = new DropTargetExtension<>(mapMain);
            dropTarget.setDropEffect(DropEffect.MOVE);

            dropTarget.addDropListener(new DropListener<GoogleMap>() {
                @Override
                public void drop(DropEvent<GoogleMap> event) {
                    Optional<String> transferData = event.getDataTransferData(RemoteObjectLight.DATA_TYPE); //Only get this type of data. Note that the type of the data to be trasferred is set in the drag source

                    if (transferData.isPresent()) {
                        for (String serializedObject : transferData.get().split("~o~")) {
                            String[] serializedObjectTokens = serializedObject.split("~a~", -1);
                            RemoteObjectLight businessObject = new RemoteObjectLight(serializedObjectTokens[1], Long.valueOf(serializedObjectTokens[0]), serializedObjectTokens[2]);
                            GoogleMapMarker newMarker = mapMain.addMarker(businessObject.toString(), mapMain.getCenter(), true, "/icons/" + businessObject.getClassName() + ".png");
                            nodes.put(newMarker.getId(), businessObject);
                        }
                    } 
                }
            });
            
            mapMain.addMarkerClickListener((clickedMarker) -> {
                eventBus.notifySubscribers(new DashboardEventListener.DashboardEvent(this, DashboardEventListener.DashboardEvent.TYPE_SELECTION, nodes.get(clickedMarker.getId())));
            });
            
            MenuBar mnuMain = new MenuBar();
        
            mnuMain.addItem("New", VaadinIcons.FOLDER_ADD, (selectedItem) -> {

            });

            mnuMain.addItem("Open", VaadinIcons.FOLDER_OPEN, (selectedItem) -> {

            });
            
            mnuMain.addItem("Save", VaadinIcons.ARROW_DOWN, (selectedItem) -> {

            });

            MenuBar.MenuItem mnuConnect = mnuMain.addItem("Connect");
            mnuConnect.setIcon(VaadinIcons.CONNECT);

            mnuConnect.addItem("Using a Container", (selectedItem) -> {
            });

            mnuConnect.addItem("Using a Link", (selectedItem) -> {
            });
            
            VerticalLayout lytContent = new VerticalLayout(mnuMain, mapMain);
            lytContent.setExpandRatio(mnuMain, 0.3f);
            lytContent.setExpandRatio(mapMain, 9.7f);
            lytContent.setSizeFull();
            contentComponent = lytContent;
            addComponent(contentComponent);
            
        } catch (NamingException ne) {
            Notifications.showError("An error ocurred while reading the map provider configuration. Contact your administrator");
        }
    }

}

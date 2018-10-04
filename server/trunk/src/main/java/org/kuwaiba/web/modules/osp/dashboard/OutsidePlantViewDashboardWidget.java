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
import com.vaadin.tapio.googlemaps.GoogleMapsComponent;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.dnd.DropTargetExtension;
import com.vaadin.ui.dnd.event.DropEvent;
import com.vaadin.ui.dnd.event.DropListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.dashboards.DashboardEventBus;
import org.kuwaiba.apis.web.gui.dashboards.DashboardEventListener;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.tools.Wizard;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.modules.physicalcon.wizards.NewPhysicalConnectionWizard;

/**
 * A widget that displays a map and allows to drop elements from a navigation tree and create physical connections
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class OutsidePlantViewDashboardWidget extends AbstractDashboardWidget {
    /**
     * Default map widget
     */
    private GoogleMapsComponent mapMain;
    /**
     * A map with the existing nodes
     */
    private HashMap<GoogleMapMarker, RemoteObjectLight> markersToObjects;
    /**
     * A map with the existing nodes
     */
    private HashMap<RemoteObjectLight, GoogleMapMarker> objectsToMarkers;
    /**
     * A map with the existing connection
     */
    private HashMap<GoogleMapPolyline, RemoteObjectLight> edgesToObjects;
    /**
     * A map with the existing connection
     */
    private HashMap<RemoteObjectLight, GoogleMapPolyline> objectsToEdges;
    /**
     * The current position in coordinates of the mouse pointer
     */
    private LatLon currentMousePointerPosition;
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;
    
    public OutsidePlantViewDashboardWidget(DashboardEventBus eventBus, WebserviceBean wsBean) {
        super("Outside Plant Viewer", eventBus);
        this.markersToObjects = new HashMap<>();
        this.edgesToObjects = new HashMap<>();
        this.objectsToMarkers = new HashMap<>();
        this.objectsToEdges = new HashMap<>();
        this.wsBean= wsBean;
        this.createContent();
        this.setSizeFull();
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

            mapMain = new GoogleMapsComponent(apiKey, null, language);
            mapMain.setSizeFull();
            
            mapMain.showEdgeLabels(true);
            mapMain.showMarkerLabels(true);
            
            currentMousePointerPosition = mapMain.getCenter();
            
            //Enable the tree as a drop target
            DropTargetExtension<GoogleMapsComponent> dropTarget = new DropTargetExtension<>(mapMain);
            dropTarget.setDropEffect(DropEffect.MOVE);

            dropTarget.addDropListener(new DropListener<GoogleMapsComponent>() {
                @Override
                public void drop(DropEvent<GoogleMapsComponent> event) {
                    Optional<String> transferData = event.getDataTransferData(RemoteObjectLight.DATA_TYPE); //Only get this type of data. Note that the type of the data to be trasferred is set in the drag source

                    if (transferData.isPresent()) {
                        for (String serializedObject : transferData.get().split("~o~")) {
                            String[] serializedObjectTokens = serializedObject.split("~a~", -1);                            
                            RemoteObjectLight businessObject = new RemoteObjectLight(serializedObjectTokens[1], Long.valueOf(serializedObjectTokens[0]), serializedObjectTokens[2]);
                            
                            if (businessObject.getId() !=  -1) { //Ignore the dummy root
                                if (objectsToMarkers.containsKey(businessObject))
                                    Notifications.showError(String.format("The object %s already exists in this view", businessObject));
                                else {
                                    GoogleMapMarker newMarker = mapMain.addMarker(businessObject.toString(), currentMousePointerPosition, true, "/icons/" + businessObject.getClassName() + ".png");
                                    markersToObjects.put(newMarker, businessObject);
                                    objectsToMarkers.put(businessObject, newMarker);
                                }
                            }
                        }
                    } 
                }
            });
            
            mapMain.addMarkerClickListener((clickedMarker) -> {
                eventBus.notifySubscribers(new DashboardEventListener.DashboardEvent(this, 
                        DashboardEventListener.DashboardEvent.TYPE_SELECTION, markersToObjects.get(clickedMarker)));
            });
            
            mapMain.addEdgeClickListener((clickedEdge) -> {
                eventBus.notifySubscribers(new DashboardEventListener.DashboardEvent(this, 
                        DashboardEventListener.DashboardEvent.TYPE_SELECTION, edgesToObjects.get(clickedEdge)));
            });
            
            mapMain.addMapMouseOverListener((position) -> {
                currentMousePointerPosition = position;
            });
            
            MenuBar mnuMain = new MenuBar();
        
            mnuMain.addItem("New", VaadinIcons.FOLDER_ADD, (selectedItem) -> {

            });

            mnuMain.addItem("Open", VaadinIcons.FOLDER_OPEN, (selectedItem) -> {

            });
            
            mnuMain.addItem("Save", VaadinIcons.ARROW_DOWN, (selectedItem) -> {

            });

            mnuMain.addItem("Connect", VaadinIcons.CONNECT, (selectedItem) -> {
                Window wdwSelectRootObjects = new Window("Select the Root Objects");
                
                ComboBox<RemoteObjectLight> cmbASideRoot = new ComboBox<>("A Side", objectsToMarkers.keySet());
                cmbASideRoot.setEmptySelectionAllowed(false);
                cmbASideRoot.setEmptySelectionCaption("Select the A Side...");
                ComboBox<RemoteObjectLight> cmbBSideRoot = new ComboBox<>("B Side", objectsToMarkers.keySet());
                cmbBSideRoot.setEmptySelectionAllowed(false);
                cmbBSideRoot.setEmptySelectionCaption("Select the B Side...");
                Button btnOk = new Button("OK");
                
                wdwSelectRootObjects.center();
                wdwSelectRootObjects.setModal(true);
                
                UI.getCurrent().addWindow(wdwSelectRootObjects);
                
                btnOk.addClickListener((event) -> {
                    wdwSelectRootObjects.close();
                    NewPhysicalConnectionWizard wizard = new NewPhysicalConnectionWizard(cmbASideRoot.getSelectedItem().get(), 
                                    cmbBSideRoot.getSelectedItem().get(), wsBean);
                    
                    wizard.setSizeUndefined();
                    
                    Window wdwWizard = new Window("New Connection Wizard", wizard);
                    wdwWizard.center();
                    wdwWizard.setModal(true);
                    wdwWizard.setWidth(40, Unit.PERCENTAGE);
                
                    wizard.addEventListener((wizardEvent) -> {
                        switch (wizardEvent.getType()) {
                            case Wizard.WizardEvent.TYPE_FINAL_STEP:
                                RemoteObjectLight newConnection = (RemoteObjectLight)wizardEvent.getInformation().get("connection");
                                RemoteObjectLight aSide = (RemoteObjectLight)wizardEvent.getInformation().get("rootASide");
                                RemoteObjectLight bSide = (RemoteObjectLight)wizardEvent.getInformation().get("rootBSide");

                                GoogleMapMarker mrkSource = objectsToMarkers.get(aSide);
                                GoogleMapMarker mrkDestination = objectsToMarkers.get(bSide);
                                
                                List<LatLon> coordinates = new ArrayList();
                                coordinates.add(mrkSource.getPosition());
                                coordinates.add(mrkDestination.getPosition());
                                                                
                                GoogleMapPolyline connection = new GoogleMapPolyline(newConnection.toString(), coordinates);
                                connection.setStrokeWeight(2);
                                
                                edgesToObjects.put(connection, newConnection);
                                objectsToEdges.put(newConnection, connection);
                                mapMain.addEdge(connection, mrkSource, mrkDestination);
                                
                                Notifications.showInfo(String.format("Connection %s created successfully", newConnection));
                            case Wizard.WizardEvent.TYPE_CANCEL:
                                wdwWizard.close();
                        }
                        
                    });
                    

                    UI.getCurrent().addWindow(wdwWizard);
                });
                
                FormLayout lytContent = new FormLayout(cmbASideRoot, cmbBSideRoot, btnOk);
                lytContent.setSizeUndefined();
                
                wdwSelectRootObjects.setContent(lytContent);
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

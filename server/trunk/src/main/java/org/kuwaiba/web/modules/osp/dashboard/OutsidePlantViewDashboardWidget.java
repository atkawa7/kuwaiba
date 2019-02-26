/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

import org.kuwaiba.web.modules.osp.OSPConstants;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.tapio.googlemaps.GoogleMapsComponent;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.dnd.DropTargetExtension;
import com.vaadin.ui.dnd.event.DropEvent;
import com.vaadin.ui.dnd.event.DropListener;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.dashboards.DashboardEventBus;
import org.kuwaiba.apis.web.gui.dashboards.DashboardEventListener;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.tools.Wizard;
import org.kuwaiba.apis.web.gui.views.util.UtilHtml;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObject;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.kuwaiba.web.modules.physicalcon.wizards.NewPhysicalConnectionWizard;

/**
 * A widget that displays a map and allows to drop elements from a navigation tree and create physical connections.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class OutsidePlantViewDashboardWidget extends AbstractDashboardWidget {
    /**
     * The version of the XML document this view is exported to 
     */
    public static final String VIEW_FORMAT_VERSION = "1.0";
    /**
     * Default map widget
     */
    private GoogleMapsComponent mapMain;
    /**
     * A map with the existing nodes
     */
    private List<OSPNode> nodes;
    /**
     * A map with the existing edges
     */
    private List<OSPEdge> edges;
    /**
     * Reference to the currently displayed view
     */
    private RemoteViewObject currentView;
    /**
     * A hash that caches the colors of the connections by connection class name
     */
    private HashMap<String, String> connectionColors;
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;
    /**
     * The default zoom of the map.
     */
    private int defaultZoom;
    /**
     * Default center of the map
     */
    private LatLon defaultCenter;
    
    public OutsidePlantViewDashboardWidget(DashboardEventBus eventBus, WebserviceBean wsBean) {
        super("Outside Plant Viewer", eventBus);
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.wsBean= wsBean;
        this.connectionColors = new HashMap<>();
        this.createContent();
        this.setSizeFull();
    }

    @Override
    public void createCover() {
        throw new UnsupportedOperationException("This widget supports only embedded mode"); 
    }
    
    @Override
    public void createContent() {
        
        String apiKey, language;
        double mapLatitude, mapLongitude;
        int mapZoom;
        
        RemoteSession session = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
        
        try {
            apiKey = (String)wsBean.getConfigurationVariableValue("general.maps.apiKey", session.getIpAddress(), session.getSessionId());
        } catch (ServerSideException ex) {
            apiKey = null;
            Notifications.showWarning("The configuration variable general.maps.apiKey has not been set. The default map will be used");
        }

        try {
            language = (String)wsBean.getConfigurationVariableValue("general.maps.language", session.getIpAddress(), session.getSessionId());
        } catch (ServerSideException ex) {
            language = OSPConstants.DEFAULT_LANGUAGE;
        }

        try {
            mapLatitude = (double)wsBean.getConfigurationVariableValue("widgets.simplemap.centerLatitude", session.getIpAddress(), session.getSessionId());
        } catch (ServerSideException ex) {
            mapLatitude = OSPConstants.DEFAULT_CENTER_LATITUDE;
        }

        try {
            mapLongitude = (double)wsBean.getConfigurationVariableValue("widgets.simplemap.centerLongitude", session.getIpAddress(), session.getSessionId());
        } catch (ServerSideException ex) {
            mapLongitude = OSPConstants.DEFAULT_CENTER_LONGITUDE;
        }

        try {
            mapZoom = (int)wsBean.getConfigurationVariableValue("widgets.simplemap.zoom", session.getIpAddress(), session.getSessionId());
        } catch (ServerSideException ex) {
            mapZoom = OSPConstants.DEFAULT_ZOOM;
        }

        mapMain = new GoogleMapsComponent(apiKey, null, language);
        mapMain.setSizeFull();
        
        this.defaultCenter = new LatLon(mapLatitude, mapLongitude);
        mapMain.setCenter(defaultCenter);
        this.defaultZoom = mapZoom;
        mapMain.setZoom(defaultZoom);

        mapMain.showEdgeLabels(true);
        mapMain.showMarkerLabels(true);

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

                            if (getMarkerFromBusinesObject(businessObject) != null)
                                Notifications.showError(String.format("The object %s already exists in this view", businessObject));
                            else {
                                GoogleMapMarker newMarker = mapMain.addMarker(businessObject.toString(), mapMain.getCenter(), true, "/icons/" + businessObject.getClassName() + ".png");
                                nodes.add(new OSPNode(newMarker, businessObject));
                            }
                        }
                    }
                } 
            }
        });

        mapMain.addMarkerClickListener((clickedMarker) -> {
            eventBus.notifySubscribers(new DashboardEventListener.DashboardEvent(this, 
                    DashboardEventListener.DashboardEvent.TYPE_SELECTION, getBusinesObjectFromMarker(clickedMarker)));
        });

        mapMain.addEdgeClickListener((clickedEdge) -> {
            eventBus.notifySubscribers(new DashboardEventListener.DashboardEvent(this, 
                    DashboardEventListener.DashboardEvent.TYPE_SELECTION, getBusinesObjectFromPolyline(clickedEdge)));
        });

        MenuBar mnuMain = new MenuBar();

        mnuMain.addItem("New", VaadinIcons.FOLDER_ADD, (selectedItem) -> {
            currentView = null;
            clearView();
        });

        mnuMain.addItem("Open", VaadinIcons.FOLDER_OPEN, (selectedItem) -> {
            try {

                List<RemoteViewObjectLight> ospViews = wsBean.getOSPViews(((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getIpAddress(), 
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

                if (ospViews.isEmpty())
                    Notifications.showInfo("There are not OSP views saved at the moment");
                else {
                    Window wdwOpen = new Window("Open OSP View");
                    VerticalLayout lytContent = new VerticalLayout();
                    Grid<RemoteViewObjectLight> tblOSPViews = new Grid<>("Select a view from the list", ospViews);
                    tblOSPViews.setHeaderVisible(false);
                    tblOSPViews.setSelectionMode(Grid.SelectionMode.SINGLE);
                    tblOSPViews.addColumn(RemoteViewObjectLight::getName).setWidthUndefined();
                    tblOSPViews.addColumn(RemoteViewObjectLight::getDescription);
                    tblOSPViews.setSizeFull();

                    Button btnOk = new Button("OK", (event) -> {

                        if (tblOSPViews.getSelectedItems().isEmpty())
                            Notifications.showInfo("You have to select a view");
                        else {
                            try {
                                currentView = wsBean.getOSPView(tblOSPViews.getSelectedItems().iterator().next().getId(), Page.getCurrent().getWebBrowser().getAddress(), 
                                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                clearView();
                                render(currentView.getStructure());
                                wdwOpen.close();
                            } catch (ServerSideException ex) {
                                Notifications.showError(ex.getLocalizedMessage());
                                wdwOpen.close();
                            }
                        }

                    });

                    Button btnCancel = new Button("Cancel", (event) -> {
                        wdwOpen.close();
                    });

                    HorizontalLayout lytButtons = new HorizontalLayout(btnOk, btnCancel);

                    lytContent.addComponents(tblOSPViews, lytButtons);
                    lytContent.setExpandRatio(tblOSPViews, 9);
                    lytContent.setExpandRatio(lytButtons, 1);
                    lytContent.setComponentAlignment(lytButtons, Alignment.MIDDLE_RIGHT);
                    lytContent.setWidth(100, Unit.PERCENTAGE);

                    wdwOpen.setContent(lytContent);

                    wdwOpen.center();
                    wdwOpen.setModal(true);
                    UI.getCurrent().addWindow(wdwOpen);
                }
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getLocalizedMessage());
            }
        });

        mnuMain.addItem("Save", VaadinIcons.ARROW_DOWN, (selectedItem) -> {
//            if (nodes.isEmpty()) 
//                Notifications.showInfo("The view is empty. There's nothing to save");
//            else {
//                VerticalLayout lytContent = new VerticalLayout();
//                Window wdwSave = new Window("Save OSP View");
//                wdwSave.setWidth(300, Unit.PIXELS);
//
//                TextField txtName = new TextField("Name");
//                txtName.setValue(currentView == null ? "" : currentView.getName());
//                TextField txtDescription = new TextField("Description");
//                txtDescription.setValue(currentView == null ? "" : currentView.getDescription());
//
//                Button btnOk = new Button("OK", (event) -> {
//
//                    if (txtName.getValue().trim().isEmpty())
//                        Notifications.showInfo("The name of the view can not be empty");
//                    else {
//                        try {
//                            if (currentView == null) { //It's a new view
//                                long newViewId = wsBean.createOSPView(txtName.getValue(), txtDescription.getValue(), getAsXml(), Page.getCurrent().getWebBrowser().getAddress(), 
//                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
//                                currentView = new RemoteViewObject();
//                                currentView.setId(newViewId);
//                            } else
//                                wsBean.updateOSPView(currentView.getId(), txtName.getValue(), txtDescription.getValue(), getAsXml(), Page.getCurrent().getWebBrowser().getAddress(), 
//                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
//
//                            currentView.setName(txtName.getValue());
//                                currentView.setDescription(txtName.getDescription());
//
//                            Notifications.showInfo("View saved successfully");
//                            wdwSave.close();
//                        } catch (ServerSideException ex) {
//                            Notifications.showError(ex.getLocalizedMessage());
//                            wdwSave.close();
//                        }
//                    }
//
//                });
//
//                Button btnCancel = new Button("Cancel", (event) -> {
//                    wdwSave.close();
//                });
//
//                FormLayout lytProperties = new FormLayout(txtName, txtDescription);
//                lytProperties.setSizeFull();
//
//                HorizontalLayout lytButtons = new HorizontalLayout(btnOk, btnCancel);
//
//                lytContent.addComponents(lytProperties, lytButtons);
//                lytContent.setExpandRatio(lytProperties, 9);
//                lytContent.setExpandRatio(lytButtons, 1);
//                lytContent.setComponentAlignment(lytButtons, Alignment.MIDDLE_RIGHT);
//                lytContent.setSizeFull();
//
//                wdwSave.setHeight(20, Unit.PERCENTAGE);
//                wdwSave.setWidth(25, Unit.PERCENTAGE);
//                wdwSave.setContent(lytContent);
//
//                wdwSave.center();
//                wdwSave.setModal(true);
//                UI.getCurrent().addWindow(wdwSave);
//            }


        });

        mnuMain.addItem("Connect", VaadinIcons.CONNECT, (selectedItem) -> {
            Window wdwSelectRootObjects = new Window("New Connection");

            ComboBox<OSPNode> cmbASideRoot = new ComboBox<>("A Side", nodes);
            cmbASideRoot.setEmptySelectionAllowed(false);
            cmbASideRoot.setEmptySelectionCaption("Select the A Side...");
            cmbASideRoot.setWidth(250, Unit.PIXELS);
            ComboBox<OSPNode> cmbBSideRoot = new ComboBox<>("B Side", nodes);
            cmbBSideRoot.setEmptySelectionAllowed(false);
            cmbBSideRoot.setEmptySelectionCaption("Select the B Side...");
            cmbBSideRoot.setWidth(250, Unit.PIXELS);
            Button btnOk = new Button("OK");

            wdwSelectRootObjects.center();
            wdwSelectRootObjects.setWidth(80, Unit.PERCENTAGE);
            wdwSelectRootObjects.setHeight(50, Unit.PERCENTAGE);
            wdwSelectRootObjects.setModal(true);

            UI.getCurrent().addWindow(wdwSelectRootObjects);

            btnOk.addClickListener((Button.ClickEvent event) -> {

                if (!cmbASideRoot.getSelectedItem().isPresent() || !cmbBSideRoot.getSelectedItem().isPresent()) {
                    Notifications.showError("Select both sides of the connection");
                    return;
                }

                if (cmbASideRoot.getSelectedItem().get().equals(cmbBSideRoot.getSelectedItem().get())){
                    Notifications.showError("The selected nodes must be different");
                    return;
                }

                wdwSelectRootObjects.close();
                NewPhysicalConnectionWizard wizard = new NewPhysicalConnectionWizard(cmbASideRoot.getSelectedItem().get().getBusinessObject(), 
                                cmbBSideRoot.getSelectedItem().get().getBusinessObject(), wsBean);

                wizard.setWidth(100, Unit.PERCENTAGE);

                Window wdwWizard = new Window("New Connection Wizard", wizard);
                wdwWizard.center();
                wdwWizard.setModal(true);
                wdwWizard.setWidth(80, Unit.PERCENTAGE);
                wdwWizard.setHeight(50, Unit.PERCENTAGE);

                wizard.addEventListener((wizardEvent) -> {
                    switch (wizardEvent.getType()) {
                        case Wizard.WizardEvent.TYPE_FINAL_STEP:
                            RemoteObjectLight newConnection = (RemoteObjectLight)wizardEvent.getInformation().get("connection");
                            RemoteObjectLight aSide = (RemoteObjectLight)wizardEvent.getInformation().get("rootASide");
                            RemoteObjectLight bSide = (RemoteObjectLight)wizardEvent.getInformation().get("rootBSide");

                            GoogleMapMarker mrkSource = getMarkerFromBusinesObject(aSide);
                            GoogleMapMarker mrkDestination = getMarkerFromBusinesObject(bSide);

                            List<LatLon> coordinates = new ArrayList();
                            coordinates.add(mrkSource.getPosition());
                            coordinates.add(mrkDestination.getPosition());

                            GoogleMapPolyline connection = new GoogleMapPolyline(newConnection.toString(), coordinates);
                            connection.setStrokeWeight(3);

                            connection.setStrokeColor(getConnectionColorFromClassName(newConnection.getClassName()));

                            OSPEdge newEdge = new OSPEdge(connection, newConnection);
                            newEdge.setSourceObject(getNodeFromBusinessObject(aSide));
                            newEdge.setTargetObject(getNodeFromBusinessObject(bSide));

                            edges.add(newEdge);
                            mapMain.addEdge(connection, mrkSource, mrkDestination);

                            Notifications.showInfo(String.format("Connection %s created successfully", newConnection));
                        case Wizard.WizardEvent.TYPE_CANCEL:
                            wdwWizard.close();
                    }

                });


                UI.getCurrent().addWindow(wdwWizard);
            });

            FormLayout lytContent = new FormLayout(cmbASideRoot, cmbBSideRoot, btnOk);
            lytContent.setMargin(true);
            lytContent.setWidthUndefined();

            wdwSelectRootObjects.setContent(lytContent);
        });

        VerticalLayout lytContent = new VerticalLayout(mnuMain, mapMain);
        lytContent.setExpandRatio(mnuMain, 0.3f);
        lytContent.setExpandRatio(mapMain, 9.7f);
        lytContent.setSizeFull();
        contentComponent = lytContent;
        addComponent(contentComponent);
    }
    

    
    /**
     * Renders a view from an XML document
     * @param structure The view as a byte array
     */
    public void render(byte[] structure) {
        if (structure.length == 0) {
            Notifications.showInfo("This view appears to be empty. There could have been a problem while it was saved");
            return;
        }
        try {
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                QName qZoom = new QName("zoom"); //NOI18N
                QName qCenter = new QName("center"); //NOI18N
                QName qNode = new QName("node"); //NOI18N
                QName qEdge = new QName("edge"); //NOI18N
                QName qLabel = new QName("label"); //NOI18N
                QName qControlPoint = new QName("controlpoint"); //NOI18N

                ByteArrayInputStream bais = new ByteArrayInputStream(structure);
                XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

                while (reader.hasNext()) {
                    int event = reader.next();
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        if (reader.getName().equals(qNode)) {
                            String objectClass = reader.getAttributeValue(null, "class"); //NOI18N

                            double lon = Double.valueOf(reader.getAttributeValue(null,"lon")); //NOI18N
                            double lat = Double.valueOf(reader.getAttributeValue(null,"lat")); //NOI18N
                            long objectId = Long.valueOf(reader.getElementText());
                            
                            try {
                                RemoteObjectLight businessObject = wsBean.getObjectLight(objectClass, objectId, Page.getCurrent().getWebBrowser().getAddress(), 
                                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                
                                if (getMarkerFromBusinesObject(businessObject) != null)
                                    Notifications.showError(String.format("The object %s already exists in this view", businessObject));
                                else {
                                    GoogleMapMarker newMarker = mapMain.addMarker(businessObject.toString(), new LatLon(lat, lon), true, "/icons/" + businessObject.getClassName() + ".png");
                                    nodes.add(new OSPNode(newMarker, businessObject));
                                }
                            } catch (ServerSideException sse) {
                                Notifications.showInfo(sse.getLocalizedMessage());
                            }
                            
                        } else {
                            if (reader.getName().equals(qEdge)) {
                                try {
                                    long objectId = Long.valueOf(reader.getAttributeValue(null, "id")); //NOI18N

                                    long aSide = Long.valueOf(reader.getAttributeValue(null, "aside")); //NOI18N
                                    long bSide = Long.valueOf(reader.getAttributeValue(null, "bside")); //NOI18N

                                    String objectClass = reader.getAttributeValue(null,"class"); //NOI18N
                                    
                                    RemoteObjectLight businessObject = wsBean.getObjectLight(objectClass, objectId, Page.getCurrent().getWebBrowser().getAddress(), 
                                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

                                    List<LatLon> controlPoints = new ArrayList<>();
                                    
                                    OSPNode sourceNode = getNodeFromBusinessObjectId(aSide);
                                    if (sourceNode == null) {
                                        Notifications.showInfo(String.format("Source node for %s was not added because it does not exist anymore", businessObject));
                                        continue;
                                    }
                                    
                                    OSPNode targetNode = getNodeFromBusinessObjectId(bSide);
                                    if (targetNode == null) {
                                        Notifications.showInfo(String.format("Target node for %s was not added because it does not exist anymore", businessObject));
                                        continue;
                                    }
                                    
                                    while(true) {
                                        reader.nextTag();

                                        if (reader.getName().equals(qControlPoint)) {
                                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                                controlPoints.add(new LatLon(Double.valueOf(reader.getAttributeValue(null,"lat")), 
                                                        Double.valueOf(reader.getAttributeValue(null,"lon"))));
                                        } else //The closing </edges> tag
                                            break;
                                    }
                                    
                                    GoogleMapPolyline newPolyline = new GoogleMapPolyline(businessObject.toString(), controlPoints);
                                    newPolyline.setStrokeWeight(3);
                                    newPolyline.setStrokeColor(getConnectionColorFromClassName(businessObject.getClassName()));
                                    OSPEdge newEdge = new OSPEdge(newPolyline, businessObject);
                                    newEdge.setSourceObject(sourceNode);
                                    newEdge.setTargetObject(targetNode);

                                    edges.add(newEdge);
                                    mapMain.addEdge(newEdge.getPolyline(), sourceNode.getMarker(), targetNode.getMarker());
                                } catch (ServerSideException sse) {
                                    Notifications.showInfo(sse.getLocalizedMessage());
                                }
                            } else {
                                if (reader.getName().equals(qLabel)) {
                                    //Unavailable for now
                                } else {
                                    if (reader.getName().equals(qZoom))
                                        mapMain.setZoom(Integer.valueOf(reader.getElementText()));
                                    else {
                                        if (reader.getName().equals(qCenter)) {
                                            double lon = Double.valueOf(reader.getAttributeValue(null, "lon")); //NOI18N
                                            double lat = Double.valueOf(reader.getAttributeValue(null, "lat")); //NOI18N
                                            mapMain.setCenter(new LatLon(lat, lon));
                                        } 
                                    }
                                }
                            }
                        }
                    }
                }
                reader.close();
            } catch (Exception ex) {
                Notifications.showError(String.format("An unexpected error appeared while reading the OSP view: " + ex.getLocalizedMessage()));
            } 
    }

    /**
     * Clears and centers the map
     */
    public void clearView() {
        nodes.forEach((node) -> { mapMain.removeMarker(node.getMarker()); });
        edges.forEach((edge) -> { mapMain.removeEdge(edge.getPolyline()); });
        
        nodes.clear();
        edges.clear();
        
        mapMain.setCenter(defaultCenter);
        mapMain.setZoom(defaultZoom);
    }
    
    /**
     * Gets the color of a connection using as input its class
     * @param className The connection class
     * @return The color of the connection as an HTML-compatible hex value. Defaults to black in case of error
     */
    private String getConnectionColorFromClassName(String className) {
        String connectionColor = connectionColors.get(className);
                                
        if (connectionColor == null) {
            try {
                RemoteClassMetadata classMetadata = wsBean.getClass(className, Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                connectionColor = UtilHtml.toHexString(new Color(classMetadata.getColor()));
            } catch (ServerSideException ex) {
                connectionColor = "#FFFFFF"; //NOI18N
                Notifications.showError(ex.getLocalizedMessage());
            }
        }
        
        return connectionColor;
    }
    
    /**
     * A simple class wrapping a node and its properties and high level events not managed by the map widget
     */
    private class OSPNode {
        /**
         * The marker displayed in the map
         */
        private GoogleMapMarker marker;
        /**
         * The business object behind the marker
         */
        private RemoteObjectLight businessObject;

        public OSPNode(GoogleMapMarker marker, RemoteObjectLight businessObject) {
            this.marker = marker;
            this.businessObject = businessObject;
        }

        public GoogleMapMarker getMarker() {
            return marker;
        }

        public RemoteObjectLight getBusinessObject() {
            return businessObject;
        }
        
        @Override
        public boolean equals(Object obj) {
            return obj instanceof OSPNode ? ((OSPNode)obj).getBusinessObject().equals(businessObject) : false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + Objects.hashCode(this.businessObject);
            return hash;
        }
        
        @Override
        public String toString() {
            return businessObject.toString();
        }
    }
    
    public RemoteObjectLight getBusinesObjectFromMarker(GoogleMapMarker marker) {
        for (OSPNode node : nodes) {
            if (node.getMarker().getId() == marker.getId())
                return node.getBusinessObject();
        }
        return null;
    }
    
    public RemoteObjectLight getBusinesObjectFromPolyline(GoogleMapPolyline polyLine) {
        for (OSPEdge edge : edges) {
            if (edge.getPolyline().getId() == polyLine.getId())
                return edge.getBusinessObject();
        }
        return null;
    }
    
    public GoogleMapMarker getMarkerFromBusinesObject(RemoteObjectLight businessObject) {
        for (OSPNode node : nodes) {
            if (node.getBusinessObject().getId() == businessObject.getId())
                return node.getMarker();
        }
        return null;
    }
    
    public GoogleMapPolyline getPolylineFromBusinessObject(RemoteObjectLight businessObject) {
        for (OSPEdge edge : edges) {
            if (edge.getBusinessObject().getId() == businessObject.getId())
                return edge.getPolyline();
        }
        return null;
    }
    
    private OSPNode getNodeFromBusinessObject(RemoteObjectLight businessObject) {
        for (OSPNode node : nodes) {
            if (node.getBusinessObject().equals(businessObject))
                return node;
        }
        
        return null;
    }
    
    private OSPEdge getEdgeFromBusinessObject(RemoteObjectLight businessObject) {
        for (OSPEdge edge : edges) {
            if (edge.getBusinessObject().equals(businessObject))
                return edge;
        }
        
        return null;
    }
    
    private OSPNode getNodeFromBusinessObjectId(long id) {
        for (OSPNode node : nodes) {
            if (node.getBusinessObject().getId() == id)
                return node;
        }
        
        return null;
    }
    
    /**
     * A simple class wrapping a node and its properties and high level events not managed by the map widget
     */
    private class OSPEdge {
        /**
         * The polyline displayed in the map
         */
        private GoogleMapPolyline polyLine;
        /**
         * The business object behind the marker
         */
        private RemoteObjectLight businessObject;
        /**
         * Reference to the source node
         */
        private OSPNode sourceObject;
        /**
         * Reference to the target node
         */
        private OSPNode targetObject;

        public OSPEdge(GoogleMapPolyline polyLine, RemoteObjectLight businessObject) {
            this.polyLine =polyLine;
            this.businessObject = businessObject;
        }

        public GoogleMapPolyline getPolyline() {
            return polyLine;
        }

        public RemoteObjectLight getBusinessObject() {
            return businessObject;
        }

        public OSPNode getSourceObject() {
            return sourceObject;
        }

        public void setSourceObject(OSPNode sourceObject) {
            this.sourceObject = sourceObject;
        }
        
        public OSPNode getTargetObject() {
            return targetObject;
        }

        public void setTargetObject(OSPNode targetObject) {
            this.targetObject = targetObject;
        }
        
        @Override
        public boolean equals(Object obj) {
            return obj instanceof OSPNode ? ((OSPNode)obj).getBusinessObject().equals(businessObject) : false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + Objects.hashCode(this.businessObject);
            return hash;
        }
        
        @Override
        public String toString() {
            return businessObject.toString();
        }
    }
}

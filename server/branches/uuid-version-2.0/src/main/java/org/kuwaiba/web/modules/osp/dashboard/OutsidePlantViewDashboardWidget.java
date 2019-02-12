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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
import org.kuwaiba.apis.web.gui.navigation.views.AbstractScene;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.tools.Wizard;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObject;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.kuwaiba.web.modules.physicalcon.wizards.NewPhysicalConnectionWizard;

/**
 * A widget that displays a map and allows to drop elements from a navigation tree and create physical connections
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
     * Default center as in the ejb-jar.xml file
     */
    private LatLon defaultCenter;
    /**
     * Default zoom as in the ejb-jar.xml file
     */
    private int defaultZoom;
    /**
     * A hash that caches the colors of the connections by connection class name
     */
    private HashMap<String, String> connectionColors;
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;
    
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
        try {
            
            Context context = new InitialContext();
            String apiKey = (String)context.lookup("java:comp/env/googleMapsApiKey");
            String language = (String)context.lookup("java:comp/env/mapLanguage");
            defaultCenter = new LatLon((double)context.lookup("java:comp/env/defaultCenterLatitude") , (double)context.lookup("java:comp/env/defaultCenterLongitude"));
            defaultZoom = (int)context.lookup("java:comp/env/defaultZoom");

            mapMain = new GoogleMapsComponent(apiKey, null, language);
            mapMain.setSizeFull();
            mapMain.setCenter(defaultCenter);
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
                            RemoteObjectLight businessObject = new RemoteObjectLight(serializedObjectTokens[1], serializedObjectTokens[0], serializedObjectTokens[2]);
                            
                            if (businessObject.getId() != null && !businessObject.getId().equals("-1")) { //Ignore the dummy root
                                
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
                    
                    List<RemoteViewObjectLight> ospViews = wsBean.getOSPViews(Page.getCurrent().getWebBrowser().getAddress(), 
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
                if (nodes.isEmpty()) 
                    Notifications.showInfo("The view is empty. There's nothing to save");
                else {
                    VerticalLayout lytContent = new VerticalLayout();
                    Window wdwSave = new Window("Save OSP View");
                    wdwSave.setWidth(300, Unit.PIXELS);

                    TextField txtName = new TextField("Name");
                    txtName.setValue(currentView == null ? "" : currentView.getName());
                    TextField txtDescription = new TextField("Description");
                    txtDescription.setValue(currentView == null ? "" : currentView.getDescription());

                    Button btnOk = new Button("OK", (event) -> {

                        if (txtName.getValue().trim().isEmpty())
                            Notifications.showInfo("The name of the view can not be empty");
                        else {
                            try {
                                if (currentView == null) { //It's a new view
                                    long newViewId = wsBean.createOSPView(txtName.getValue(), txtDescription.getValue(), getAsXml(), Page.getCurrent().getWebBrowser().getAddress(), 
                                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                    currentView = new RemoteViewObject();
                                    currentView.setId(newViewId);
                                } else
                                    wsBean.updateOSPView(currentView.getId(), txtName.getValue(), txtDescription.getValue(), getAsXml(), Page.getCurrent().getWebBrowser().getAddress(), 
                                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                    
                                currentView.setName(txtName.getValue());
                                    currentView.setDescription(txtName.getDescription());
                                
                                Notifications.showInfo("View saved successfully");
                                wdwSave.close();
                            } catch (ServerSideException ex) {
                                Notifications.showError(ex.getLocalizedMessage());
                                wdwSave.close();
                            }
                        }

                    });

                    Button btnCancel = new Button("Cancel", (event) -> {
                        wdwSave.close();
                    });

                    FormLayout lytProperties = new FormLayout(txtName, txtDescription);
                    lytProperties.setSizeFull();
                    
                    HorizontalLayout lytButtons = new HorizontalLayout(btnOk, btnCancel);
                    
                    lytContent.addComponents(lytProperties, lytButtons);
                    lytContent.setExpandRatio(lytProperties, 9);
                    lytContent.setExpandRatio(lytButtons, 1);
                    lytContent.setComponentAlignment(lytButtons, Alignment.MIDDLE_RIGHT);
                    lytContent.setSizeFull();

                    wdwSave.setHeight(20, Unit.PERCENTAGE);
                    wdwSave.setWidth(25, Unit.PERCENTAGE);
                    wdwSave.setContent(lytContent);

                    wdwSave.center();
                    wdwSave.setModal(true);
                    UI.getCurrent().addWindow(wdwSave);
                }
                
                
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
            
        } catch (NamingException ne) {
            Notifications.showError("An error ocurred while reading the map provider configuration. Contact your administrator");
        }
    }
    
    /**
     * Exports the view to XML
     * @return The XML document as a byte array
     */
    public byte[] getAsXml() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();

            QName qnameView = new QName("view"); //NOI18N
            xmlew.add(xmlef.createStartElement(qnameView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), VIEW_FORMAT_VERSION)); //NOI18N

            QName qnameClass = new QName("class"); //NOI18N
            xmlew.add(xmlef.createStartElement(qnameClass, null, null));
            xmlew.add(xmlef.createCharacters("OSPView")); //NOI18N
            xmlew.add(xmlef.createEndElement(qnameClass, null));
            
            QName qCenter = new QName("center"); //NOI18N
            xmlew.add(xmlef.createStartElement(qCenter, null, null));
            xmlew.add(xmlef.createAttribute(new QName("lon"), Double.toString(mapMain.getCenter().getLon()))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("lat"), Double.toString(mapMain.getCenter().getLat()))); //NOI18N
            xmlew.add(xmlef.createEndElement(qCenter, null));
            
            QName qZoom = new QName("zoom"); //NOI18N
            xmlew.add(xmlef.createStartElement(qZoom, null, null));
            xmlew.add(xmlef.createCharacters(String.valueOf(mapMain.getZoom()))); //NOI18N
            xmlew.add(xmlef.createEndElement(qZoom, null));

            QName qnameNodes = new QName("nodes"); //NOI18N
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            
            //First the nodes
            for (OSPNode node : nodes) {
                QName qnameNode = new QName("node"); //NOI18N
                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                xmlew.add(xmlef.createAttribute(new QName("lon"), Double.toString(node.getMarker().getPosition().getLon()))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("lat"), Double.toString(node.getMarker().getPosition().getLat()))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("class"), node.getBusinessObject().getClassName())); //NOI18N
                xmlew.add(xmlef.createCharacters(node.getBusinessObject().getId()));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));

            //Now the connections
            QName qnameEdges = new QName("edges"); //NOI18N
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            
            for (OSPEdge edge : edges) {
                GoogleMapPolyline lnEdge = edge.getPolyline();
                QName qnameEdge = new QName("edge"); //NOI18N
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                xmlew.add(xmlef.createAttribute(new QName("id"), edge.getBusinessObject().getId())); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("class"), edge.getBusinessObject().getClassName())); //NOI18N

                xmlew.add(xmlef.createAttribute(new QName("aside"), String.valueOf(edge.getSourceObject().getBusinessObject().getId()))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("bside"), String.valueOf(edge.getTargetObject().getBusinessObject().getId()))); //NOI18N
                
                for (LatLon point : lnEdge.getCoordinates()) {
                    QName qnameControlpoint = new QName("controlpoint"); //NOI18N
                    xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("lon"), Double.toString(point.getLon()))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("lat"), Double.toString(point.getLat()))); //NOI18N
                    xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                }
                xmlew.add(xmlef.createEndElement(qnameEdge, null));
            }
            xmlew.add(xmlef.createEndElement(qnameEdges, null));
            xmlew.add(xmlef.createEndElement(qnameView, null));
            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) { 
            //Should not happen
            return new byte[0];
        }
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
                            String objectId = reader.getElementText();
                            
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
                                    String objectId = reader.getAttributeValue(null, "id"); //NOI18N

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
                System.out.println("Unexpected error reading OSP view" + ex.getLocalizedMessage());
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
                connectionColor = AbstractScene.toHexString(new Color(classMetadata.getColor()));
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
//////        for (OSPNode node : nodes) {
//////            if (node.getBusinessObject().getId() == id)
//////                return node;
//////        }
        
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

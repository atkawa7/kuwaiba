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
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import com.vaadin.ui.AbstractComponent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.dashboards.DashboardEventBus;
import org.kuwaiba.apis.web.gui.dashboards.DashboardEventListener;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.tools.Wizard;
import org.kuwaiba.apis.web.gui.views.AbstractView;
import org.kuwaiba.apis.web.gui.views.AbstractViewNode;
import org.kuwaiba.apis.web.gui.views.util.UtilHtml;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObject;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.web.modules.physicalcon.wizards.NewPhysicalConnectionWizard;

/**
 * A widget that displays a map and allows to drop elements from a navigation tree and create physical connections.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class OutsidePlantViewDashboardWidget extends AbstractDashboardWidget {
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
        this.wsBean= wsBean;
        this.connectionColors = new HashMap<>();
        this.createContent();
        this.setSizeFull();
    }

    @Override
    public void createCover() {
        throw new UnsupportedOperationException("This widget only supports embedded mode"); 
    }
    
    @Override
    public void createContent() {
        try {
            AbstractView theOspView = (AbstractView)PersistenceService.getInstance().getViewFactory().
                    createViewInstance("org.kuwaiba.web.modules.osp.OutsidePlantView"); //NOI18N
            theOspView.buildEmptyView();
            AbstractComponent mapComponent = theOspView.getAsComponent();
            
            //Enable the component as a drop target
            DropTargetExtension<AbstractComponent> dropTarget = new DropTargetExtension<>(mapComponent);
            dropTarget.setDropEffect(DropEffect.MOVE);

            dropTarget.addDropListener(new DropListener<AbstractComponent>() {
                @Override
                public void drop(DropEvent<AbstractComponent> event) {
                    Optional<String> transferData = event.getDataTransferData(RemoteObjectLight.DATA_TYPE); //Only get this type of data. Note that the type of the data to be trasferred is set in the drag source

                    if (transferData.isPresent()) {
                        for (String serializedObject : transferData.get().split("~o~")) {
                            String[] serializedObjectTokens = serializedObject.split("~a~", -1);                            
                            RemoteObjectLight businessObject = new RemoteObjectLight(serializedObjectTokens[1], serializedObjectTokens[0], serializedObjectTokens[2]);

                            if (businessObject.getId() != null && !businessObject.getId().equals("-1")) { //Ignore the dummy root
                                if (theOspView.getAsViewMap().findNode(businessObject.getId()) != null)
                                    Notifications.showError(String.format("The object %s already exists in this view", businessObject));
                                else
                                    theOspView.addNode(businessObject, new Properties());
                            }
                        }
                    } 
                }
            });

            theOspView.addNodeClickListener((source, type) -> {
                eventBus.notifySubscribers(new DashboardEventListener.DashboardEvent(this, 
                        DashboardEventListener.DashboardEvent.TYPE_SELECTION, new RemoteObjectLight((BusinessObjectLight)source)));
            });
                    
            theOspView.addEdgeClickListener((source, type) -> {
                eventBus.notifySubscribers(new DashboardEventListener.DashboardEvent(this, 
                        DashboardEventListener.DashboardEvent.TYPE_SELECTION, new RemoteObjectLight((BusinessObjectLight)source)));
            });

            MenuBar mnuMain = new MenuBar();

            mnuMain.addItem("New", VaadinIcons.FOLDER_ADD, (selectedItem) -> {
                theOspView.buildEmptyView();
                try {
                    theOspView.getAsComponent(); //This will not create a new map, it will only refresh it, and since the new viewMap is empty, it will clean up the actual map
                } catch (Exception ex) {
                    //Should not happen
                }
            });

            mnuMain.addItem("Open", VaadinIcons.FOLDER_OPEN, (selectedItem) -> {
                try {

                    List<RemoteViewObjectLight> ospViews = wsBean.getOSPViews(((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getIpAddress(), 
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

                    if (ospViews.isEmpty())
                        Notifications.showInfo("There are not OSP views saved at the moment");
                    else {
                        Window wdwOpen = new Window("Open an OSP View");
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
                                    RemoteViewObject savedView = wsBean.getOSPView(tblOSPViews.getSelectedItems().iterator().next().getId(), Page.getCurrent().getWebBrowser().getAddress(), 
                                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                    
                                    theOspView.getProperties().put(Constants.PROPERTY_ID, savedView.getId());
                                    theOspView.getProperties().put(Constants.PROPERTY_NAME, savedView.getName());
                                    theOspView.getProperties().put(Constants.PROPERTY_DESCRIPTION, savedView.getDescription());
                                    
                                    theOspView.buildWithSavedView(savedView.getStructure());
                                    theOspView.getAsComponent();
                                    
                                    wdwOpen.close();
                                } catch (Exception ex) {
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
                        wdwOpen.setWidth(40, Unit.PERCENTAGE);
                        
                        wdwOpen.center();
                        wdwOpen.setModal(true);
                        UI.getCurrent().addWindow(wdwOpen);
                    }
                } catch (Exception ex) {
                    Notifications.showError(ex.getLocalizedMessage());
                }
            });

            mnuMain.addItem("Save", VaadinIcons.ARROW_DOWN, (selectedItem) -> {
                if (theOspView.getAsViewMap().getNodes().isEmpty()) 
                    Notifications.showInfo("The view is empty. There's nothing to save");
                else {
                    VerticalLayout lytContent = new VerticalLayout();
                    Window wdwSave = new Window("Save OSP View");

                    TextField txtName = new TextField("Name");
                    txtName.setValue(theOspView.getProperties().getProperty(Constants.PROPERTY_NAME)== null ? "" : 
                            theOspView.getProperties().getProperty(Constants.PROPERTY_NAME));
                    TextField txtDescription = new TextField("Description");
                    txtDescription.setValue(theOspView.getProperties().getProperty(Constants.PROPERTY_DESCRIPTION)== null ? "" : 
                            theOspView.getProperties().getProperty(Constants.PROPERTY_DESCRIPTION));

                    Button btnOk = new Button("OK", (event) -> {

                        if (txtName.getValue().trim().isEmpty())
                            Notifications.showInfo("The name of the view can not be empty");
                        else {
                            try {
                                if (theOspView.getProperties().get(Constants.PROPERTY_ID).equals(-1)) { //It's a new view
                                    long newViewId = wsBean.createOSPView(txtName.getValue(), txtDescription.getValue(), theOspView.getAsXml(), Page.getCurrent().getWebBrowser().getAddress(), 
                                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                    theOspView.getProperties().put(Constants.PROPERTY_ID, newViewId);
                                } else
                                    wsBean.updateOSPView((Long)theOspView.getProperties().get(Constants.PROPERTY_ID), txtName.getValue(), txtDescription.getValue(), theOspView.getAsXml(), Page.getCurrent().getWebBrowser().getAddress(), 
                                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

                                theOspView.getProperties().put(Constants.PROPERTY_NAME, txtName.getValue());
                                theOspView.getProperties().put(Constants.PROPERTY_DESCRIPTION, txtName.getDescription());
                                
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
                    lytContent.setExpandRatio(lytProperties, 8);
                    lytContent.setExpandRatio(lytButtons, 2);
                    lytContent.setComponentAlignment(lytButtons, Alignment.MIDDLE_RIGHT);
                    lytContent.setMargin(true);
                    lytContent.setSizeFull();

                    wdwSave.setHeight(30, Unit.PERCENTAGE);
                    wdwSave.setWidth(35, Unit.PERCENTAGE);
                    wdwSave.setContent(lytContent);

                    wdwSave.center();
                    wdwSave.setModal(true);
                    UI.getCurrent().addWindow(wdwSave);
                }
            });

            mnuMain.addItem("Connect", VaadinIcons.CONNECT, (selectedItem) -> {
                Window wdwSelectRootObjects = new Window("New Connection");

                ComboBox<AbstractViewNode> cmbASideRoot = new ComboBox<>("A Side", theOspView.getAsViewMap().getNodes());
                cmbASideRoot.setEmptySelectionAllowed(false);
                cmbASideRoot.setEmptySelectionCaption("Select the A Side...");
                cmbASideRoot.setWidth(250, Unit.PIXELS);
                ComboBox<AbstractViewNode> cmbBSideRoot = new ComboBox<>("B Side", theOspView.getAsViewMap().getNodes());
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
                    
                    NewPhysicalConnectionWizard wizard = new NewPhysicalConnectionWizard(new RemoteObjectLight((BusinessObjectLight)cmbASideRoot.getSelectedItem().get().getIdentifier()), 
                                    new RemoteObjectLight((BusinessObjectLight)cmbBSideRoot.getSelectedItem().get().getIdentifier()), wsBean);

                    wizard.setWidth(100, Unit.PERCENTAGE);

                    Window wdwWizard = new Window("New Connection Wizard", wizard);
                    wdwWizard.center();
                    wdwWizard.setModal(true);
                    wdwWizard.setWidth(80, Unit.PERCENTAGE);
                    wdwWizard.setHeight(80, Unit.PERCENTAGE);

                    wizard.addEventListener((wizardEvent) -> {
                        switch (wizardEvent.getType()) {
                            case Wizard.WizardEvent.TYPE_FINAL_STEP:
                                RemoteObjectLight newConnection = (RemoteObjectLight)wizardEvent.getInformation().get("connection");
                                RemoteObjectLight aSide = (RemoteObjectLight)wizardEvent.getInformation().get("rootASide");
                                RemoteObjectLight bSide = (RemoteObjectLight)wizardEvent.getInformation().get("rootBSide");
                                
                                Properties edgeProperties = new Properties();
                                edgeProperties.put(Constants.PROPERTY_COLOR, getConnectionColorFromClassName(newConnection.getClassName()));
                                
                                theOspView.addEdge(newConnection, aSide, bSide, edgeProperties);
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

            VerticalLayout lytContent = new VerticalLayout(mnuMain, mapComponent);
            lytContent.setExpandRatio(mnuMain, 0.3f);
            lytContent.setExpandRatio(mapComponent, 9.7f);
            lytContent.setSizeFull();
            this.contentComponent = lytContent;
            addComponent(contentComponent);
        } catch (Exception ex) {
            this.contentComponent = new VerticalLayout();
            Notifications.showError(String.format("An unexpected error occurred while creating the content of this view: %s", ex.getLocalizedMessage()));
        }
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
}

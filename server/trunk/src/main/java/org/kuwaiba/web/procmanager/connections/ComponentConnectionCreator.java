/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.web.procmanager.connections;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.dnd.DragSourceExtension;
import com.vaadin.ui.dnd.DropTargetExtension;
import com.vaadin.ui.dnd.event.DropEvent;
import com.vaadin.ui.dnd.event.DropListener;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.apis.web.gui.navigation.BasicIconGenerator;
import org.kuwaiba.apis.web.gui.navigation.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.navigation.nodes.ChildrenProvider;
import org.kuwaiba.apis.web.gui.navigation.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.navigation.trees.BasicTree;
import org.kuwaiba.apis.web.gui.navigation.trees.BasicTreeLayout;
import org.kuwaiba.apis.web.gui.navigation.trees.TreeLayout.ItemHorizontalLayout;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;

/**
 * A list of a given devices, links and a nav tree are shown, with this
 * ports of the devices and links can be drag/drop into a connections table
 * to create relationships between endpoints.
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class ComponentConnectionCreator extends VerticalLayout {
    private final WebserviceBean webserviceBean;
        
    public ComponentConnectionCreator(List<RemoteObject> devicesList, WebserviceBean webserviceBean) {
        this.webserviceBean = webserviceBean;     
        setSpacing(false);
        setSizeFull();
        initializeComponent(devicesList);
    }
    
    @Override
    public final void setSizeFull() {
        super.setSizeFull();
    }
        
    private void initializeComponent(List<RemoteObject> devicesList) {
        RemoteSession remoteSession = (RemoteSession) UI.getCurrent().getSession().getAttribute("session"); //NOI18N
        //Sources devices and links
        List<RemoteObjectLight> deviceListLight = new ArrayList<>();
        List<RemoteObjectLight> linksListLight = new ArrayList<>();
        
        devicesList.forEach(device -> {
            
            boolean isSubclassOfGenericPhysicalLink = false;
            try {
                isSubclassOfGenericPhysicalLink = webserviceBean.isSubclassOf(
                    device.getClassName(), 
                    "GenericPhysicalLink", //NOI18N 
                    remoteSession.getIpAddress(), 
                    remoteSession.getSessionId());
            } catch(ServerSideException ex) {
                Notifications.showError(ex.getMessage());
            }
            if (isSubclassOfGenericPhysicalLink)
                linksListLight.add(device);
            else
                deviceListLight.add(device);
        });
        
        Button btnConnect = new Button("Connect");
        btnConnect.setIcon(VaadinIcons.PLUG);
        btnConnect.addStyleName(ValoTheme.BUTTON_PRIMARY);
        btnConnect.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_RIGHT);
        
        Panel pnlLinks = new Panel("Connections Log");
        pnlLinks.setSizeFull();
        
        Grid gridLog = createInstallationMaterialGrid(linksListLight);
        
        pnlLinks.setContent(gridLog);
               
        HorizontalLayout lytConnection = new HorizontalLayout();
        lytConnection.setWidth(100, Unit.PERCENTAGE);
        lytConnection.setHeight(100, Unit.PERCENTAGE);
        lytConnection.setSpacing(false);
          
        //we create the connections tables
        Panel pnlConnections = new Panel();
        pnlConnections.setSizeFull();
        VerticalLayout verticalLayoutEndpointsA = createLayoutEndpoint("Endpoint A");
        Grid grdLinks = createEndpointsLinksGrid(linksListLight);
        VerticalLayout verticalLayoutEndpointsB = createLayoutEndpoint("Endpoint B");
        
        btnConnect.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                List<RemoteObjectLight> endpointsA = null;
                List<RemoteObjectLight> links = null;
                List<RemoteObjectLight> endpointsB = null;
                
                if (verticalLayoutEndpointsA.getComponentCount() > 0 && 
                    verticalLayoutEndpointsA.getComponent(0) instanceof TabSheet && 
                    ((TabSheet.Tab) ((TabSheet) verticalLayoutEndpointsA.getComponent(0)).getTab(0)).getComponent() instanceof Panel &&
                    ((Panel) ((TabSheet.Tab) ((TabSheet) verticalLayoutEndpointsA.getComponent(0)).getTab(0)).getComponent()).getContent() instanceof BasicTreeLayout) {
                                        
                    BasicTreeLayout simpleTree = (BasicTreeLayout) ((Panel) ((TabSheet.Tab) ((TabSheet) verticalLayoutEndpointsA.getComponent(0)).getTab(0)).getComponent()).getContent();
                    
                    if (simpleTree.getSelectedItems() != null && 
                        !simpleTree.getSelectedItems().isEmpty()) {
                        
                        endpointsA = new ArrayList();
                        
                        for (Object item : simpleTree.getSelectedItems()) {
                            if (item instanceof AbstractNode && 
                                ((AbstractNode) item).getObject() instanceof RemoteObjectLight) {
                                
                                endpointsA.add((RemoteObjectLight) ((AbstractNode) item).getObject());
                            }
                        }
                    }
                }
                                
                if (grdLinks.getSelectedItems() != null) {
                    
                    if (grdLinks.getSelectedItems() != null && 
                        !grdLinks.getSelectedItems().isEmpty()) {
                        
                        links = new ArrayList();
                        
                        for (Object item : grdLinks.getSelectedItems()) {
                            if (item instanceof RemoteObjectLight) {
                                links.add((RemoteObjectLight) item);
                            }
                        }
                    }
                }
                
                if (verticalLayoutEndpointsB.getComponentCount() > 0 && 
                    verticalLayoutEndpointsB.getComponent(0) instanceof TabSheet && 
                    ((TabSheet.Tab) ((TabSheet) verticalLayoutEndpointsB.getComponent(0)).getTab(0)).getComponent() instanceof Panel &&
                    ((Panel) ((TabSheet.Tab) ((TabSheet) verticalLayoutEndpointsB.getComponent(0)).getTab(0)).getComponent()).getContent() instanceof BasicTreeLayout) {
                    
                    BasicTreeLayout simpleTree = (BasicTreeLayout) ((Panel) ((TabSheet.Tab) ((TabSheet) verticalLayoutEndpointsB.getComponent(0)).getTab(0)).getComponent()).getContent();
                    
                    if (simpleTree.getSelectedItems() != null && 
                        !simpleTree.getSelectedItems().isEmpty()) {
                        
                        endpointsB = new ArrayList();
                        
                        for (Object item : simpleTree.getSelectedItems()) {
                            if (item instanceof AbstractNode && 
                                ((AbstractNode) item).getObject() instanceof RemoteObjectLight) {
                                
                                endpointsB.add((RemoteObjectLight) ((AbstractNode) item).getObject());
                            }
                        }
                    }
                }
                
                if (endpointsA != null && links != null && endpointsB != null && 
                    (endpointsA.size() == links.size() && links.size() ==  endpointsB.size())) {
                    
                    if (createConnection(endpointsA, links, endpointsB)) {
                        grdLinks.setItems(getLinks(linksListLight));
                        gridLog.setItems(getLinkBeans(linksListLight));
                        
                        BasicTreeLayout simpleTreeA = (BasicTreeLayout) ((Panel) ((TabSheet.Tab) ((TabSheet) verticalLayoutEndpointsA.getComponent(0)).getTab(0)).getComponent()).getContent();
                        if (simpleTreeA.getSelectedItems() != null)
                            simpleTreeA.clearSelectedItems();
                                                                        
                        BasicTreeLayout simpleTreeB = (BasicTreeLayout) ((Panel) ((TabSheet.Tab) ((TabSheet) verticalLayoutEndpointsB.getComponent(0)).getTab(0)).getComponent()).getContent();
                        if (simpleTreeB.getSelectedItems() != null)
                            simpleTreeB.clearSelectedItems();
                    }
                }
                else {
                    Notifications.showInfo("Please select the same number of endpoints and links");
                }
            }
        });
        lytConnection.addComponent(verticalLayoutEndpointsA);
        lytConnection.setExpandRatio(verticalLayoutEndpointsA, 0.30f);
        
        lytConnection.addComponent(grdLinks);
        lytConnection.setExpandRatio(grdLinks, 0.30f);
        
        lytConnection.addComponent(verticalLayoutEndpointsB);
        lytConnection.setExpandRatio(verticalLayoutEndpointsB, 0.30f);
        
        pnlConnections.setContent(lytConnection);
   
        VerticalLayout lytRightSide = new VerticalLayout(pnlConnections, btnConnect, pnlLinks);
        lytRightSide.setSpacing(true);
        lytRightSide.setSizeFull();
        lytRightSide.setExpandRatio(pnlLinks, 0.30f);
        lytRightSide.setExpandRatio(pnlConnections, 0.65f);
        lytRightSide.setExpandRatio(btnConnect, 0.05f);
        lytRightSide.setComponentAlignment(btnConnect, Alignment.BOTTOM_CENTER);
        //End Connections
        
        Panel pnlSourceDevices = new Panel("Select a device from the material for installation");
        pnlSourceDevices.setSizeFull();
        
        VerticalLayout vlySourceDevices = new VerticalLayout();
        vlySourceDevices.setWidth(100, Unit.PERCENTAGE);
        vlySourceDevices.setHeightUndefined();
        
        //we create the tree for the given devices
        pnlSourceDevices.setContent(createInstallationMaterialTree(deviceListLight));

        //Right side/bottom nav tree 
        Panel pnlNavTree = (Panel)crateNavTree();
        pnlNavTree.setSizeFull();
        pnlNavTree.addStyleName(ValoTheme.PANEL_BORDERLESS);
        
        // Source Side
        VerticalLayout sourceLayout = new VerticalLayout(pnlSourceDevices, pnlNavTree);
        sourceLayout.setSizeFull();
                
        sourceLayout.setExpandRatio(pnlSourceDevices, 0.35f);
        sourceLayout.setExpandRatio(pnlNavTree, 0.65f);

        HorizontalLayout mainLayout = new HorizontalLayout(sourceLayout, lytRightSide);        
        mainLayout.setSpacing(false);
        mainLayout.setSizeFull();
    
        mainLayout.setExpandRatio(sourceLayout, 0.30f);
        mainLayout.setExpandRatio(lytRightSide, 0.70f);        
                
        addComponent(mainLayout);
    }
    
    private boolean createConnection(List<RemoteObjectLight> endpointsA, List<RemoteObjectLight> links, List<RemoteObjectLight> endpointsB) {
        try {
            RemoteSession remoteSession = (RemoteSession) UI.getCurrent().getSession().getAttribute("session"); //NOI18N
            int size = endpointsA.size();
            
            String[] sideAClassNames = new String[size];
            String[] sideBClassNames = new String[size];
            String[] linksClassNames = new String[size];

            String[] sideAIds = new String[size];
            String[] sideBIds = new String[size];
            String[] linksIds = new String[size];
            
            List<RemoteObjectLight> newLinksParents = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                sideAClassNames[i] = endpointsA.get(i).getClassName();
                sideAIds[i] = endpointsA.get(i).getId();

                newLinksParents.add(webserviceBean.getCommonParent(
                        endpointsA.get(i).getClassName(), endpointsA.get(i).getId(), 
                        endpointsB.get(i).getClassName(), endpointsB.get(i).getId(), 
                        remoteSession.getIpAddress(),
                        remoteSession.getSessionId()));

                sideBClassNames[i] = endpointsB.get(i).getClassName();
                sideBIds[i] = endpointsB.get(i).getId();

                linksClassNames[i] = links.get(i).getClassName();
                linksIds[i] = links.get(i).getId();
            }
            //we create the end points
            webserviceBean.connectPhysicalLinks(sideAClassNames, sideAIds, 
                    linksClassNames, linksIds, 
                    sideBClassNames, sideBIds, 
                    remoteSession.getIpAddress(),
                    remoteSession.getSessionId());
            
            //we move the link from to under a new parent
            for (int i = 0; i < newLinksParents.size(); i++) {
                
                if (!newLinksParents.get(i).getId().equals("-1")) { //Ignore the dummy root
                    webserviceBean.moveSpecialObjects(
                        newLinksParents.get(i).getClassName(), 
                        newLinksParents.get(i).getId(), 
                        new String[] {linksClassNames[i]}, 
                        new String[] {linksIds[i]}, 
                        remoteSession.getIpAddress(),
                        remoteSession.getSessionId());
                }
            }
            Notifications.showInfo("The connections were created successfully");
            return true;
        } catch (ServerSideException ex) {
            Notifications.showInfo(ex.getMessage());
            return false;
        }
    }

    /**
     * Creates the navigation tree
     * @return a simple navigation tree
     */
    private Component crateNavTree(){
        Panel pnlNavTree = new Panel();
        pnlNavTree.setSizeFull();
         pnlNavTree.setContent(new ComponentConnectionTarget(null, webserviceBean));
        return pnlNavTree;
    }
    
    private VerticalLayout createLayoutEndpoint(final String caption) {
        VerticalLayout vlEndpoint = new VerticalLayout();
        vlEndpoint.setWidth(100, Unit.PERCENTAGE);
        vlEndpoint.setHeight(100, Unit.PERCENTAGE);
        vlEndpoint.setCaption(caption);
        vlEndpoint.setSizeFull();               
        
        DropTargetExtension<VerticalLayout> dropTarget = new DropTargetExtension(vlEndpoint);
        dropTarget.setDropEffect(DropEffect.MOVE);
        
        dropTarget.addDropListener(new DropListener<VerticalLayout>() {
            @Override
            public void drop(DropEvent<VerticalLayout> event) {
                vlEndpoint.setCaption(caption);
                vlEndpoint.removeAllComponents();
                
                Optional<String> transferData = event.getDataTransferData(RemoteObjectLight.DATA_TYPE);
                if (transferData.isPresent()) {
                    for (String serializedObject : transferData.get().split("\n")) {
                        String[] serializedObjectTokens = serializedObject.split("~a~", -1);
                        try {
                            final RemoteSession remoteSession = (RemoteSession) UI.getCurrent().getSession().getAttribute("session"); //NOI18N
                                                        
                            RemoteObjectLight businessObject = webserviceBean.getObjectLight(
                                serializedObjectTokens[1], 
                                serializedObjectTokens[0], 
                                remoteSession.getIpAddress(), remoteSession.getSessionId());
                        
                            boolean isGenericCommunicationsElement = false;
                            boolean isODF = false;
                                                        
                            isGenericCommunicationsElement = webserviceBean.isSubclassOf(
                                businessObject.getClassName(), 
                                "GenericCommunicationsElement", //NOI18N
                                remoteSession.getIpAddress(), 
                                remoteSession.getSessionId());
                            
                            isODF = webserviceBean.isSubclassOf(
                                businessObject.getClassName(), 
                                "ODF", //NOI18N
                                remoteSession.getIpAddress(),
                                remoteSession.getSessionId());
                            
                            if (!businessObject.getId().equals("-1") && (isGenericCommunicationsElement | isODF)) { //Ignore the dummy root
                                vlEndpoint.setData(businessObject);
                                vlEndpoint.setCaption(caption + ": " + businessObject);
                                
                                final TabSheet tabSheet = new TabSheet();
                                tabSheet.setSizeFull();

                                final Panel pnlPorts = new Panel();
                                pnlPorts.setSizeFull();

                                Panel pnlHierarchy = new Panel();
                                pnlHierarchy.setSizeFull();
                                
                                InventoryObjectNode inventoryObjectNode = new InventoryObjectNode(businessObject);
                                final BasicTreeLayout treePorts = new BasicTreeLayout(new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                                    @Override
                                    public List<RemoteObjectLight> getChildren(RemoteObjectLight parent) {
                                        if (parent != null) {
                                            try {
                                                return webserviceBean.getChildrenOfClassLightRecursive(
                                                        parent.getId(),
                                                        parent.getClassName(),
                                                        "GenericPhysicalPort", //NOI18N
                                                        0,
                                                        remoteSession.getIpAddress(),
                                                        remoteSession.getSessionId());
                                            } catch (ServerSideException ex) {
                                                Notifications.showError(ex.getMessage());
                                            }
                                        }
                                        return null;
                                    }
                                }, 
                                new BasicIconGenerator(webserviceBean, remoteSession), 
                                inventoryObjectNode);
                                treePorts.expand(inventoryObjectNode);
                                
                                final BasicTreeLayout treeHierarchy = new BasicTreeLayout(new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                                    @Override
                                    public List<RemoteObjectLight> getChildren(RemoteObjectLight parent) {
                                        if (parent != null) {
                                            try {
                                                return webserviceBean.getObjectChildren(
                                                        parent.getClassName(), 
                                                        parent.getId(), 
                                                        -1, 
                                                        remoteSession.getIpAddress(), 
                                                        remoteSession.getSessionId());
                                            } catch (ServerSideException ex) {
                                                Notifications.showError(ex.getMessage());
                                            }
                                        }
                                        return null;
                                    }
                                }, 
                                new BasicIconGenerator(webserviceBean, remoteSession), 
                                new InventoryObjectNode(businessObject));
                                treeHierarchy.setDropEffect(DropEffect.MOVE);
                                treeHierarchy.addDropListener(new DropListener() {
                                    @Override
                                    public void drop(DropEvent event) {
                                        Optional<String> transferData = event.getDataTransferData(RemoteObjectLight.DATA_TYPE);
                                        if (transferData.isPresent() && event.getComponent() instanceof ItemHorizontalLayout) {
                                            Object item = ((ItemHorizontalLayout) event.getComponent()).getItem();
                                            if (item instanceof InventoryObjectNode && 
                                                ((InventoryObjectNode) item).getObject() instanceof RemoteObjectLight) {
                                                InventoryObjectNode parentNode = (InventoryObjectNode) item;
                                                RemoteObjectLight parent = parentNode.getObject();
                                                                                                
                                                for (String serializedObject : transferData.get().split("\n")) {
                                                    String[] serializedObjectTokens = serializedObject.split("~a~", -1);
                                                    try {
                                                        final RemoteSession remoteSession = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");                                 
                                                        RemoteObjectLight businessObject = webserviceBean.getObjectLight(
                                                            serializedObjectTokens[1], 
                                                            serializedObjectTokens[0], 
                                                            remoteSession.getIpAddress(),
                                                            remoteSession.getSessionId());
                                                        
                                                        String classNameTransceiver = "Transceiver";
                                                        
                                                        if (webserviceBean.isSubclassOf(
                                                            businessObject.getClassName(), 
                                                            classNameTransceiver, //NOI18N
                                                            remoteSession.getIpAddress(), 
                                                            remoteSession.getSessionId())) {
                                                            
                                                            String opticalPortClassName = "OpticalPort";
                                                            
                                                            List<RemoteClassMetadataLight> possibleChildren = webserviceBean.getPossibleChildren(
                                                                parent.getClassName(), remoteSession.getIpAddress(), remoteSession.getSessionId());
                                                            
                                                            boolean isPossibleChild = false;
                                                            
                                                            for (RemoteClassMetadataLight possibleChild : possibleChildren) {
                                                                if (opticalPortClassName.equals(possibleChild.getClassName())) {
                                                                    isPossibleChild = true;
                                                                    break;
                                                                }
                                                            }
                                                            if (isPossibleChild) {
                                                                Window window = new Window();
                                                                window.setHeight(50, Unit.PERCENTAGE);
                                                                window.setWidth(50, Unit.PERCENTAGE);
                                                                window.setModal(true);
                                                                
                                                                VerticalLayout vly = new VerticalLayout();
                                                                vly.setSizeFull();
                                                                
                                                                GridLayout gly = new GridLayout();
                                                                gly.setSpacing(true);
                                                                gly.setColumns(2);
                                                                gly.setRows(3);
                                                                
                                                                Label lblTransceiverName = new Label("Transceiver Name");
                                                                final TextField txtTransceiverName = new TextField();
                                                                
                                                                Label lblPortType = new Label("Port Type");
                                                                final ComboBox<String> cmbPortType = new ComboBox<>();
                                                                
                                                                Label lblPortName = new Label("Port Name");
                                                                final TextField txtPortName = new TextField();
                                                                
                                                                Button btnOK = new Button("OK");
                                                                Button btnCancel = new Button("Cancel");
                                                                
                                                                cmbPortType.setItems("OpticalPort", "ElectricalPort");
                                                                
                                                                btnOK.setWidth(80, Unit.PIXELS);
                                                                btnOK.addClickListener(new ClickListener() {
                                                                    @Override
                                                                    public void buttonClick(Button.ClickEvent event) {
                                                                        if (txtTransceiverName.getValue() != null && !txtTransceiverName.getValue().isEmpty()) {
                                                                            if (cmbPortType.getValue() != null && !cmbPortType.getValue().isEmpty()) {
                                                                                if (txtPortName.getValue() != null && !txtPortName.getValue().isEmpty()) {
                                                                                    try {
                                                                                        String portId = webserviceBean.createObject(
                                                                                            cmbPortType.getValue(), //NOI18N
                                                                                            parent.getClassName(), 
                                                                                            parent.getId(), 
                                                                                            new String[] {"name"}, //NOI18N
                                                                                            new String[] {txtPortName.getValue()}, 
                                                                                            null, 
                                                                                            remoteSession.getIpAddress(), 
                                                                                            remoteSession.getSessionId());

                                                                                        RemoteObjectLight portObject = webserviceBean.getObjectLight(
                                                                                            cmbPortType.getValue(), //NOI18N
                                                                                            portId, 
                                                                                            remoteSession.getIpAddress(), 
                                                                                            remoteSession.getSessionId());

                                                                                        webserviceBean.moveObjects(
                                                                                            portObject.getClassName(), 
                                                                                            portObject.getId(), 
                                                                                            new String[] {businessObject.getClassName()}, 
                                                                                            new String[] {businessObject.getId()}, 
                                                                                            remoteSession.getIpAddress(), 
                                                                                            remoteSession.getSessionId());
                                                                                        
                                                                                        List<StringPair> attrs = new ArrayList();
                                                                                        attrs.add(new StringPair("name", txtTransceiverName.getValue()));
                                                                                                                                                                                
                                                                                        webserviceBean.updateObject(
                                                                                            businessObject.getClassName(),
                                                                                            businessObject.getId(), 
                                                                                            attrs, 
                                                                                            remoteSession.getIpAddress(), 
                                                                                            remoteSession.getSessionId());
                                                                                        
                                                                                        treeHierarchy.expand(parentNode);
                                                                                    } catch (ServerSideException ex) {
                                                                                        Notifications.showError(ex.getMessage());
                                                                                    }
                                                                                    window.close();
                                                                                }
                                                                                else
                                                                                    Notifications.showWarning("Port Name not set");
                                                                            }
                                                                            else
                                                                                Notifications.showWarning("Port Type not set");
                                                                        }
                                                                        else
                                                                            Notifications.showWarning("Transceiver Name not set");
                                                                    }
                                                                });
                                                                
                                                                btnCancel.setWidth(80, Unit.PIXELS);
                                                                btnCancel.addClickListener(new ClickListener() {
                                                                    @Override
                                                                    public void buttonClick(Button.ClickEvent event) {
                                                                        window.close();
                                                                    }
                                                                });
                                                                
                                                                gly.addComponent(lblTransceiverName);
                                                                gly.addComponent(txtTransceiverName);
                                                                gly.addComponent(lblPortType);
                                                                gly.addComponent(cmbPortType);
                                                                gly.addComponent(lblPortName);
                                                                gly.addComponent(txtPortName);
                                                                gly.addComponent(btnOK);
                                                                gly.addComponent(btnCancel);
                                                                
                                                                gly.setComponentAlignment(lblTransceiverName, Alignment.MIDDLE_LEFT);
                                                                gly.setComponentAlignment(lblPortType, Alignment.MIDDLE_LEFT);
                                                                gly.setComponentAlignment(lblPortName, Alignment.MIDDLE_LEFT);
                                                                gly.setComponentAlignment(txtTransceiverName, Alignment.MIDDLE_CENTER);
                                                                gly.setComponentAlignment(cmbPortType, Alignment.MIDDLE_CENTER);
                                                                gly.setComponentAlignment(txtPortName, Alignment.MIDDLE_CENTER);
                                                                gly.setComponentAlignment(btnOK, Alignment.MIDDLE_RIGHT);
                                                                gly.setComponentAlignment(btnCancel, Alignment.MIDDLE_LEFT);
                                                                
                                                                vly.addComponent(gly);
                                                                vly.setComponentAlignment(gly, Alignment.MIDDLE_CENTER);
                                                                window.setContent(vly);
                                                                                                                                
                                                                UI.getCurrent().addWindow(window);
                                                            }
                                                            else
                                                                Notifications.showWarning(String.format("%s cannot contain %s", parent.getClassName(), opticalPortClassName));
                                                        }
                                                        else {
                                                            List<RemoteClassMetadataLight> possibleChildren = webserviceBean.getPossibleChildren(
                                                                parent.getClassName(), remoteSession.getIpAddress(), remoteSession.getSessionId());
                                                            
                                                            boolean isPossibleChild = false;
                                                            
                                                            for (RemoteClassMetadataLight possibleChild : possibleChildren) {
                                                                if (businessObject.getClassName().equals(possibleChild.getClassName())) {
                                                                    isPossibleChild = true;
                                                                    break;
                                                                }
                                                            }
                                                            if (isPossibleChild) {
                                                                webserviceBean.moveObjects(
                                                                    parent.getClassName(), 
                                                                    parent.getId(), 
                                                                    new String[] {businessObject.getClassName()}, 
                                                                    new String[] {businessObject.getId()}, 
                                                                    remoteSession.getIpAddress(), 
                                                                    remoteSession.getSessionId());
                                                                treeHierarchy.expand(parentNode);
                                                            }
                                                            else
                                                                Notifications.showWarning(String.format("%s cannot contain %s", parent.getClassName(), businessObject.getClassName()));
                                                        }
                                                    } catch (ServerSideException ex) {
                                                        Notifications.showError(ex.getMessage());
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                });
                                
                                pnlPorts.setContent(treePorts);
                                pnlHierarchy.setContent(treeHierarchy);
                                
                                tabSheet.addTab(pnlPorts, "Ports", VaadinIcons.CONNECT);
                                tabSheet.addTab(pnlHierarchy, "Hierarchy", VaadinIcons.FILE_TREE);
                                tabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {
                                    @Override
                                    public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                                        if (tabSheet.getTabPosition(tabSheet.getTab(tabSheet.getSelectedTab())) == 0) {
                                            treePorts.resetTo(new InventoryObjectNode[] {inventoryObjectNode});
                                            treePorts.expand(inventoryObjectNode);
                                        }
                                    }
                                });
                                
                                vlEndpoint.addComponent(tabSheet);
                            }
                        } catch (ServerSideException ex) {
                            Notifications.showError(ex.getMessage());
                        }
                        break;
                    }
                }
            }
        });
        return vlEndpoint;
    }
        
    private List<RemoteObjectLight> getLinks(List<RemoteObjectLight> links) {
        RemoteSession remoteSession = (RemoteSession) UI.getCurrent().getSession().getAttribute("session"); //NOI18N
        
        List<RemoteObjectLight> result = new ArrayList();
                
        for (RemoteObjectLight link : links) {
            RemoteObjectLight endpointA = null;
            RemoteObjectLight endpointB = null;
            
            try {            
                List<RemoteObjectLight> theEndpointsA = webserviceBean.getSpecialAttribute(
                    link.getClassName(), 
                    link.getId(), 
                    "endpointA", //NOI18N
                    remoteSession.getIpAddress(), 
                    remoteSession.getSessionId());
                
                if (theEndpointsA != null && !theEndpointsA.isEmpty())
                    endpointA = theEndpointsA.get(0);
            } catch(ServerSideException ex) {
                Notifications.showError(ex.getMessage());
            }
            try {
                List<RemoteObjectLight> theEndpointsB = webserviceBean.getSpecialAttribute(
                    link.getClassName(), 
                    link.getId(), 
                    "endpointB", //NOI18N
                    remoteSession.getIpAddress(), 
                    remoteSession.getSessionId());
                
                if (theEndpointsB != null && !theEndpointsB.isEmpty())
                    endpointB = theEndpointsB.get(0);
            } catch(ServerSideException ex) {
                Notifications.showError(ex.getMessage());
            }
            if (endpointA == null || endpointB == null)
                result.add(link);
        }        
        return result;                
    }
    
    private Grid createEndpointsLinksGrid(List<RemoteObjectLight> links) {
        Grid<RemoteObjectLight> grdEndpoints = new Grid();
        grdEndpoints.setSizeFull();
        grdEndpoints.addColumn(RemoteObjectLight::getName).setCaption("Links");        
        
        List<RemoteObjectLight> items = getLinks(links);
        
        grdEndpoints.setItems(items);
        
        return grdEndpoints;
    }
    
    private Component createInstallationMaterialTree(List<RemoteObjectLight> deviceList) {
        RemoteSession remoteSession = (RemoteSession) UI.getCurrent().getSession().getAttribute("session"); //NOI18N
        
        BasicTree tree = new BasicTree(
                new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                    @Override
                    public List<RemoteObjectLight> getChildren(RemoteObjectLight c) {
                        try {
                            return webserviceBean.getObjectChildren(
                                c.getClassName(), 
                                c.getId(), 
                                -1, 
                                remoteSession.getIpAddress(),
                                remoteSession.getSessionId());
                        } catch (ServerSideException ex) {
                            Notifications.showError(ex.getLocalizedMessage());
                            return new ArrayList<>();
                        }
                    }
                }, 
                new BasicIconGenerator(webserviceBean, remoteSession), 
                InventoryObjectNode.asNodeList(deviceList));

        tree.resetTo(InventoryObjectNode.asNodeList(deviceList));
        tree.setSelectionMode(Grid.SelectionMode.MULTI);

        DragSourceExtension<BasicTree> dragSource = new DragSourceExtension<>(tree);
        dragSource.setEffectAllowed(EffectAllowed.MOVE);

        return tree;
    }
    
    private List<LinkBean> getLinkBeans(List<RemoteObjectLight> links) {
        RemoteSession remoteSession = (RemoteSession) UI.getCurrent().getSession().getAttribute("session"); //NOI18N
        List<LinkBean> items = new ArrayList();
        
        for (RemoteObjectLight link : links) {        
            RemoteObjectLight endpointA = null;
            RemoteObjectLight endpointB = null;
            
            try {            
                List<RemoteObjectLight> theEndpointsA = webserviceBean.getSpecialAttribute(
                    link.getClassName(), 
                    link.getId(), 
                    "endpointA", //NOI18N
                    remoteSession.getIpAddress(), 
                    remoteSession.getSessionId());
                
                if (theEndpointsA != null && !theEndpointsA.isEmpty())
                    endpointA = theEndpointsA.get(0);
            } catch(ServerSideException ex) {
                Notifications.showError(ex.getMessage());
            }
            try {
                List<RemoteObjectLight> theEndpointsB = webserviceBean.getSpecialAttribute(
                    link.getClassName(), 
                    link.getId(), 
                    "endpointB", //NOI18N
                    remoteSession.getIpAddress(), 
                    remoteSession.getSessionId());
                
                if (theEndpointsB != null && !theEndpointsB.isEmpty())
                    endpointB = theEndpointsB.get(0);
            } catch(ServerSideException ex) {
                Notifications.showError(ex.getMessage());
            }
            if (endpointA != null || endpointB != null)
                items.add(new LinkBean(endpointA, link, endpointB));
        }
        
        return items;
    }
    
    /**
     * Creates a simple tree, every device from the material installation is a root
     * @param deviceList a given device list
     * @return a simple tree
     */
    private Grid createInstallationMaterialGrid(List<RemoteObjectLight> deviceList) {
        Grid<LinkBean> grid = new Grid();
        grid.setSizeFull();
                
        List<LinkBean> items = getLinkBeans(deviceList);
        
        grid.addColumn(LinkBean::getEndpointA).setCaption("Endpoint A");
        grid.addColumn(LinkBean::getObjectLink).setCaption("Link");
        grid.addColumn(LinkBean::getEndpointB).setCaption("Endpoint B");
        
        grid.setItems(items);
        return grid;        
    }
    
    private class LinkBean {
        private final RemoteObjectLight endpointA;
        private final RemoteObjectLight objectLink;
        private final RemoteObjectLight endpointB;
        
        public LinkBean(RemoteObjectLight endpointA, RemoteObjectLight objectLink, RemoteObjectLight endpointB) {
            this.endpointA = endpointA;
            this.objectLink = objectLink;
            this.endpointB = endpointB;
        }
        
        public RemoteObjectLight getEndpointA() {
            return endpointA;
        }
        
        public RemoteObjectLight getObjectLink() {
            return objectLink;            
        }
        
        public RemoteObjectLight getEndpointB() {
            return endpointB;
        }
    }
}

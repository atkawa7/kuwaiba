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
import com.vaadin.server.Page;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.shared.ui.dnd.EffectAllowed;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.DragSourceExtension;
import com.vaadin.ui.dnd.DropTargetExtension;
import com.vaadin.ui.dnd.event.DropEvent;
import com.vaadin.ui.dnd.event.DropListener;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.navigation.SimpleIconGenerator;
import org.kuwaiba.apis.web.gui.navigation.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.navigation.nodes.ChildrenProvider;
import org.kuwaiba.apis.web.gui.navigation.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.navigation.trees.SimpleTree;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

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
////    private List<RemoteObjectLight> endpointsA;
////    private List<RemoteObjectLight> endpointsB;
////    private List<RemoteObjectLight> links;
////    private Grid<RemoteObjectLight> grdEndpointsA; 
    private Grid<RemoteObjectLight> grdEndpointsB;
    private Grid<RemoteObjectLight> grdLinks; 
    
    public ComponentConnectionCreator(List<RemoteObject> devicesList, /*ComponentConnectionSource componentConnectionSource,*/ WebserviceBean webserviceBean) {
        this.webserviceBean = webserviceBean;        
        setSizeFull();
////        endpointsA = new ArrayList<>();
////        endpointsB = new ArrayList<>();
////        links = new ArrayList<>();
        initializeComponent(devicesList/*componentConnectionSource*/);                        
    }
    
    @Override
    public final void setSizeFull() {
        super.setSizeFull();
    }
        
    private void initializeComponent(List<RemoteObject> devicesList/*ComponentConnectionSource componentConnectionSource*/) {
////        List<RemoteObject> deviceList = componentConnectionSource.getDeviceList();
        //Sources devices and links
        List<RemoteObjectLight> deviceListLight = new ArrayList<>();
        List<RemoteObjectLight> linksListLight = new ArrayList<>();
        
        devicesList.forEach(device -> {
////            if(device.getClassName().toLowerCase().contains("link"))
            boolean isSubclassOfGenericPhysicalLink = false;
            try {
                isSubclassOfGenericPhysicalLink = webserviceBean.isSubclassOf(
                    device.getClassName(), 
                    "GenericPhysicalLink", 
                    Page.getCurrent().getWebBrowser().getAddress(), ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
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
////        btnConnect.addClickListener(new Button.ClickListener() {
////            @Override
////            public void buttonClick(Button.ClickEvent event) {
////                if(endpointsA.size() == endpointsB.size() && endpointsA.size() == links.size()){
////                    try {
////                        String[] sideAClassNames =  new String[endpointsA.size()];
////                        String[] sideBClassNames =  new String[endpointsA.size()];
////                        String[] linksClassNames =  new String[endpointsA.size()];
////                        
////                        Long[] sideAIds =  new Long[endpointsA.size()];
////                        Long[] sideBIds =  new Long[endpointsA.size()];
////                        long[] linksIds =  new long[endpointsA.size()];
////                        List<RemoteObjectLight> newLinksParents = new ArrayList<>();
////                        
////                        for (int i = 0; i < endpointsA.size(); i++) {
////                            sideAClassNames[i] = endpointsA.get(i).getClassName();
////                            sideAIds[i] = endpointsA.get(i).getId();
////                            
////                            newLinksParents.add(webserviceBean.getCommonParent(endpointsA.get(i).getClassName(), endpointsA.get(i).getId(), 
////                                    endpointsB.get(i).getClassName(), endpointsB.get(i).getId(), 
////                                    Page.getCurrent().getWebBrowser().getAddress(),
////                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()));
////                            
////                            sideBClassNames[i] = endpointsB.get(i).getClassName();
////                            sideBIds[i] = endpointsB.get(i).getId();
////                            
////                            linksClassNames[i] = links.get(i).getClassName();
////                            linksIds[i] = links.get(i).getId();
////                        }
////                        //we create the end points
////                        webserviceBean.connectPhysicalLinks(sideAClassNames, sideAIds, 
////                                linksClassNames, linksIds, 
////                                sideBClassNames, sideBIds, 
////                                Page.getCurrent().getWebBrowser().getAddress(),
////                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
////
////////                        grdEndpointsA.setItems(new ArrayList<>());
////                        grdEndpointsB.setItems(new ArrayList<>());
////                        grdLinks.setItems(new ArrayList<>());
////                        
////                        //we move the link from to under a new parent
////                        for (int i = 0; i < newLinksParents.size(); i++) {
////                            webserviceBean.moveObjects(newLinksParents.get(i).getClassName(), newLinksParents.get(i).getId(), 
////                                    new String[] {linksClassNames[i]}, new long[] {linksIds[i]}, 
////                                Page.getCurrent().getWebBrowser().getAddress(),
////                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
////                        }
////                        
////                        Notifications.showInfo("Las conexiones fueron creadas exitosamente");
////                        
////                                                
////                    } catch (ServerSideException ex) {
////                        Notifications.showInfo(ex.getLocalizedMessage());
////                    }
////                }
////                else
////                    Notifications.showInfo("Please select the same number of endpoints and links");
////            }
////        });

        Panel pnlLinks = new Panel("Select links from the material for installation");
        pnlLinks.setSizeFull();
        
        pnlLinks.setContent(createInstallationMaterialGrid(linksListLight));
               
        HorizontalLayout lytConnection = new HorizontalLayout();
        lytConnection.setWidth(100, Unit.PERCENTAGE);
        lytConnection.setHeight(100, Unit.PERCENTAGE);
////        lytConnection.setHeightUndefined();
        lytConnection.setSpacing(true);
          
        //we create the connections tables
        Panel pnlConnections = new Panel();
////        grdEndpointsA=  createEndpointsAGrid();
        VerticalLayout verticalLayoutEndpointsA = createEndpointsA();
////        grdLinks = createEndpointsLinksGrid();
        grdLinks = createEndpointsLinksGrid(linksListLight);
        VerticalLayout verticalLayoutEndpointsB = createEndpointsB();
        
        btnConnect.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (verticalLayoutEndpointsA.getComponentCount() > 0) {
                    if (verticalLayoutEndpointsA.getComponent(0) instanceof SimpleTree) {
                        SimpleTree simpleTree = (SimpleTree) verticalLayoutEndpointsA.getComponent(0);
                        simpleTree.getSelectedItems();
                    }
                }
                
                if (verticalLayoutEndpointsB.getComponentCount() > 0) {
                    if (verticalLayoutEndpointsB.getComponent(0) instanceof SimpleTree) {
                        SimpleTree simpleTree = (SimpleTree) verticalLayoutEndpointsB.getComponent(0);
                        simpleTree.getSelectedItems();
                    }
                }
            }
        });
////        grdEndpointsB = createEndpointsBGrid();
        //Connections Table
////        lytConnection.addComponent(grdEndpointsA);
////        lytConnection.setExpandRatio(grdEndpointsA, 0.30f);
        lytConnection.addComponent(verticalLayoutEndpointsA);
        lytConnection.setExpandRatio(verticalLayoutEndpointsA, 0.30f);
        lytConnection.addComponent(grdLinks);
        lytConnection.setExpandRatio(grdLinks, 0.30f);
////        lytConnection.addComponent(grdEndpointsB);
////        lytConnection.setExpandRatio(grdEndpointsB, 0.30f);
        lytConnection.addComponent(verticalLayoutEndpointsB);
        lytConnection.setExpandRatio(verticalLayoutEndpointsB, 0.30f);
        pnlConnections.setContent(lytConnection);
   
        VerticalLayout lytRightSide = new VerticalLayout(pnlLinks, pnlConnections, btnConnect);
        lytRightSide.setSpacing(true);
        lytRightSide.setSizeFull();
        lytRightSide.setExpandRatio(pnlLinks, 0.30f);
        lytRightSide.setExpandRatio(pnlConnections, 0.50f);
        lytRightSide.setExpandRatio(btnConnect, 0.20f);
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
    
    private VerticalLayout createEndpointsA() {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidth(100, Unit.PERCENTAGE);
//        verticalLayout.setHeight(100, Unit.PERCENTAGE);
        verticalLayout.setHeight(200, Unit.PIXELS);
        verticalLayout.setCaption("Endpoint A");
        DropTargetExtension<VerticalLayout> dropTarget = new DropTargetExtension(verticalLayout);
        dropTarget.setDropEffect(DropEffect.MOVE);
        
        dropTarget.addDropListener(new DropListener<VerticalLayout>() {
            @Override
            public void drop(DropEvent<VerticalLayout> event) {
                Optional<String> transferData = event.getDataTransferData(RemoteObjectLight.DATA_TYPE);
                if (transferData.isPresent()) {
                    for (String serializedObject : transferData.get().split("\n")) {
                        String[] serializedObjectTokens = serializedObject.split("~a~", -1);
                        RemoteObjectLight businessObject = new RemoteObjectLight(serializedObjectTokens[1], Long.valueOf(serializedObjectTokens[0]), serializedObjectTokens[2]);
                        boolean isGenericCommunicationsElement = false;
                        boolean isODF = false;
                        try {
                            isGenericCommunicationsElement = webserviceBean.isSubclassOf(
                                businessObject.getClassName(), 
                                "GenericCommunicationsElement", //NOI18N
                                Page.getCurrent().getWebBrowser().getAddress(), 
                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                            
                            isODF = webserviceBean.isSubclassOf(
                                businessObject.getClassName(), 
                                "ODF", //NOI18N
                                Page.getCurrent().getWebBrowser().getAddress(), 
                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                        } catch(ServerSideException ex) {
                            Notifications.showError(ex.getMessage());
                        }
                        if (businessObject.getId() != -1 && (isGenericCommunicationsElement || isODF)) { //Ignore the dummy root
////                            endpointsB.add(businessObject);
////                            grdEndpoints.setItems(endpointsB);
                            
                            SimpleTree dynamicTree = new SimpleTree(new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                                @Override
                                public List<RemoteObjectLight> getChildren(RemoteObjectLight parentObject) {
                                    try {       
                                        return webserviceBean.getChildrenOfClassLightRecursive(
                                            businessObject.getId(), 
                                            businessObject.getClassName(), 
                                            "GenericCommunicationsPort", 
                                            0, 
                                            Page.getCurrent().getWebBrowser().getAddress(), 
                                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
////                                        return webserviceBean.getObjectChildren(
////                                                businessObject.getClassName(), 
////                                                businessObject.getId(), -1, Page.getCurrent().getWebBrowser().getAddress(), 
////                                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                    } catch (ServerSideException ex) {
                                        Notifications.showError(ex.getLocalizedMessage());
                                        return new ArrayList<>();
                                    }
                                }
                                    }, new SimpleIconGenerator(webserviceBean, (RemoteSession) UI.getCurrent().getSession().getAttribute("session")),
                                    new AbstractNode<RemoteObjectLight>(businessObject) {
                                        @Override
                                        public AbstractAction[] getActions() { return new AbstractAction[0]; }

                                        @Override
                                        public void refresh(boolean recursive) { }
                                }
                            );
                            verticalLayout.addComponent(dynamicTree);
                        }
                    }
                }
            }
        });
        return verticalLayout;
    }
    
    private VerticalLayout createEndpointsB() {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidth(100, Unit.PERCENTAGE);
//        verticalLayout.setHeight(100, Unit.PERCENTAGE);
        verticalLayout.setHeight(200, Unit.PIXELS);
////        verticalLayout.setSizeFull();
        verticalLayout.setCaption("Endpoint B");
        DropTargetExtension<VerticalLayout> dropTarget = new DropTargetExtension(verticalLayout);
        dropTarget.setDropEffect(DropEffect.MOVE);
        
        dropTarget.addDropListener(new DropListener<VerticalLayout>() {
            @Override
            public void drop(DropEvent<VerticalLayout> event) {
                Optional<String> transferData = event.getDataTransferData(RemoteObjectLight.DATA_TYPE);
                if (transferData.isPresent()) {
                    for (String serializedObject : transferData.get().split("\n")) {
                        String[] serializedObjectTokens = serializedObject.split("~a~", -1);
                        RemoteObjectLight businessObject = new RemoteObjectLight(serializedObjectTokens[1], Long.valueOf(serializedObjectTokens[0]), serializedObjectTokens[2]);
                        boolean isGenericCommunicationsElement = false;
                        boolean isODF = false;
                        try {
                            isGenericCommunicationsElement = webserviceBean.isSubclassOf(
                                businessObject.getClassName(), 
                                "GenericCommunicationsElement", //NOI18N
                                Page.getCurrent().getWebBrowser().getAddress(), 
                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                            
                            isODF = webserviceBean.isSubclassOf(
                                businessObject.getClassName(), 
                                "ODF", //NOI18N
                                Page.getCurrent().getWebBrowser().getAddress(), 
                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                        } catch(ServerSideException ex) {
                            Notifications.showError(ex.getMessage());
                        }
                        if (businessObject.getId() != -1 && (isGenericCommunicationsElement || isODF)) { //Ignore the dummy root
////                            endpointsB.add(businessObject);
////                            grdEndpoints.setItems(endpointsB);
                            
                            SimpleTree dynamicTree = new SimpleTree(new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                                        @Override
                                        public List<RemoteObjectLight> getChildren(RemoteObjectLight parentObject) {
                                            try {
                                                return webserviceBean.getChildrenOfClassLightRecursive(
                                                    businessObject.getId(), 
                                                    businessObject.getClassName(), 
                                                    "GenericCommunicationsPort", 
                                                    0, 
                                                    Page.getCurrent().getWebBrowser().getAddress(), 
                                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
////                                                return webserviceBean.getObjectChildren(
////                                                        businessObject.getClassName(), 
////                                                        businessObject.getId(), -1, Page.getCurrent().getWebBrowser().getAddress(), 
////                                                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                            } catch (ServerSideException ex) {
                                                Notifications.showError(ex.getLocalizedMessage());
                                                return new ArrayList<>();
                                            }
                                        }
                                    }, new SimpleIconGenerator(webserviceBean, (RemoteSession) UI.getCurrent().getSession().getAttribute("session")),
                                    new AbstractNode<RemoteObjectLight>(businessObject) {
                                        @Override
                                        public AbstractAction[] getActions() { return new AbstractAction[0]; }

                                        @Override
                                        public void refresh(boolean recursive) { }
                                }
                            );
                            verticalLayout.addComponent(dynamicTree);
                        }
                    }
                }
            }
        });
        return verticalLayout;
    }
        
////    private Grid createEndpointsAGrid(){
////        Grid<RemoteObjectLight> grdEndpoints = new Grid();
////        grdEndpoints.setSizeFull();
////        grdEndpoints.addColumn(RemoteObjectLight::getName).setCaption("Endpoint A");
////                
////        DropTargetExtension<Grid> topDropTarget = new DropTargetExtension<>(grdEndpoints);
////        topDropTarget.setDropEffect(DropEffect.MOVE);
////
////        topDropTarget.addDropListener(new DropListener<Grid>() {
////            @Override
////            public void drop(DropEvent<Grid> event) {
////                Optional<String> transferData = event.getDataTransferData(RemoteObjectLight.DATA_TYPE);
////                if (transferData.isPresent()) {
////                    for (String serializedObject : transferData.get().split("\n")) {
////                        String[] serializedObjectTokens = serializedObject.split("~a~", -1);                            
////                        RemoteObjectLight businessObject = new RemoteObjectLight(serializedObjectTokens[1], Long.valueOf(serializedObjectTokens[0]), serializedObjectTokens[2]);
////////                        if (businessObject.getId() !=  -1 && businessObject.getClassName().toLowerCase().contains("port")) { //Ignore the dummy root
////                        boolean isGenericCommunicationsElement = false;
////                        boolean isODF = false;
////                        try {
////                            isGenericCommunicationsElement = webserviceBean.isSubclassOf(
////                                businessObject.getClassName(), 
////                                "GenericCommunicationsElement", //NOI18N
////                                Page.getCurrent().getWebBrowser().getAddress(), 
////                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
////                            
////                            isODF = webserviceBean.isSubclassOf(
////                                businessObject.getClassName(), 
////                                "ODF", //NOI18N
////                                Page.getCurrent().getWebBrowser().getAddress(), 
////                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
////                        } catch(ServerSideException ex) {
////                            Notifications.showError(ex.getMessage());
////                        }
////                        
////                        if (businessObject.getId() != -1 && (isGenericCommunicationsElement || isODF)) {
////                            endpointsA.add(businessObject);
////                            grdEndpoints.setItems(endpointsA);
////                        }
////                    }
////                }
////            }
////        });     
////        
////        return grdEndpoints;
////    }
    
////    private Grid createEndpointsBGrid(){
////        Grid<RemoteObjectLight> grdEndpoints = new Grid();
////        grdEndpoints.setSizeFull();
////        grdEndpoints.addColumn(RemoteObjectLight::getName).setCaption("Endpoint B");
////                
////        DropTargetExtension<Grid> topDropTarget = new DropTargetExtension<>(grdEndpoints);
////        topDropTarget.setDropEffect(DropEffect.MOVE);
////
////        topDropTarget.addDropListener(new DropListener<Grid>() {
////            @Override
////            public void drop(DropEvent<Grid> event) {
////                Optional<String> transferData = event.getDataTransferData(RemoteObjectLight.DATA_TYPE);
////                if (transferData.isPresent()) {
////                    for (String serializedObject : transferData.get().split("\n")) {
////                        String[] serializedObjectTokens = serializedObject.split("~a~", -1);                            
////                        RemoteObjectLight businessObject = new RemoteObjectLight(serializedObjectTokens[1], Long.valueOf(serializedObjectTokens[0]), serializedObjectTokens[2]);
////////                        if (businessObject.getId() !=  -1 && businessObject.getClassName().toLowerCase().contains("port")){//Ignore the dummy root
////                        boolean isGenericCommunicationsElement = false;
////                        boolean isODF = false;
////                        try {
////                            isGenericCommunicationsElement = webserviceBean.isSubclassOf(
////                                businessObject.getClassName(), 
////                                "GenericCommunicationsElement", //NOI18N
////                                Page.getCurrent().getWebBrowser().getAddress(), 
////                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
////                            
////                            isODF = webserviceBean.isSubclassOf(
////                                businessObject.getClassName(), 
////                                "ODF", //NOI18N
////                                Page.getCurrent().getWebBrowser().getAddress(), 
////                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
////                        } catch(ServerSideException ex) {
////                            Notifications.showError(ex.getMessage());
////                        }
////                        if (businessObject.getId() != -1 && (isGenericCommunicationsElement || isODF)) { //Ignore the dummy root
////                            endpointsB.add(businessObject);
////                            grdEndpoints.setItems(endpointsB);
////                        }
////                    }
////                }
////            }
////        });     
////        
////        return grdEndpoints;
////    }
    
////    private Grid createEndpointsLinksGrid(){
////        Grid<RemoteObjectLight> grdEndpoints = new Grid();
////        grdEndpoints.setSizeFull();
////        grdEndpoints.addColumn(RemoteObjectLight::getName).setCaption("Links");
////                
////        DropTargetExtension<Grid> topDropTarget = new DropTargetExtension<>(grdEndpoints);
////        topDropTarget.setDropEffect(DropEffect.MOVE);
////
////        topDropTarget.addDropListener(new DropListener<Grid>() {
////            @Override
////            public void drop(DropEvent<Grid> event) {
////                Optional<String> transferData = event.getDataTransferData(RemoteObjectLight.DATA_TYPE);
////                if (transferData.isPresent()) {
////                    for (String serializedObject : transferData.get().split("\n")) {
////                        String[] serializedObjectTokens = serializedObject.split("~a~", -1);                            
////                        RemoteObjectLight businessObject = new RemoteObjectLight(serializedObjectTokens[1], Long.valueOf(serializedObjectTokens[0]), serializedObjectTokens[2]);
////                        if (businessObject.getClassName().toLowerCase().contains("link")){//Ignore the dummy root
////                            links.add(businessObject);
////                            grdEndpoints.setItems(links);
////                        }
////                    }
////                }
////            }
////        });     
////        
////        return grdEndpoints;
////    }
    
    private Grid createEndpointsLinksGrid(List<RemoteObjectLight> links) {
        Grid<RemoteObjectLight> grdEndpoints = new Grid();
        grdEndpoints.setSizeFull();
        grdEndpoints.addColumn(RemoteObjectLight::getName).setCaption("Links");        
        
        List<RemoteObjectLight> items = new ArrayList();
        
        for (RemoteObjectLight link : links) {
            RemoteObjectLight endpointA = null;
            RemoteObjectLight endpointB = null;
            
            try {            
                List<RemoteObjectLight> theEndpointsA = webserviceBean.getSpecialAttribute(
                    link.getClassName(), 
                    link.getId(), 
                    "endpointA", 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                
                if (theEndpointsA != null && !theEndpointsA.isEmpty())
                    endpointA = theEndpointsA.get(0);
            } catch(ServerSideException ex) {
                Notifications.showError(ex.getMessage());
            }
            try {
                List<RemoteObjectLight> theEndpointsB = webserviceBean.getSpecialAttribute(
                    link.getClassName(), 
                    link.getId(), 
                    "endpointB", 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                
                if (theEndpointsB != null && !theEndpointsB.isEmpty())
                    endpointB = theEndpointsB.get(0);
            } catch(ServerSideException ex) {
                Notifications.showError(ex.getMessage());
            }
            if (endpointA == null || endpointB == null)
                items.add(link);
        }
        
        grdEndpoints.setItems(items);
        
        return grdEndpoints;
    }
    
    private Component createInstallationMaterialTree(List<RemoteObjectLight> deviceList) {
        SimpleTree tree = new SimpleTree(
                new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                    @Override
                    public List<RemoteObjectLight> getChildren(RemoteObjectLight c) {
                        try {
                            return webserviceBean.getObjectChildren(
                                c.getClassName(), 
                                c.getId(), 
                                -1, 
                                Page.getCurrent().getWebBrowser().getAddress(),
                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                        } catch (ServerSideException ex) {
                            Notifications.showError(ex.getLocalizedMessage());
                            return new ArrayList<>();
                        }
                    }
                }, 
                new SimpleIconGenerator(webserviceBean, ((RemoteSession) UI.getCurrent().getSession().getAttribute("session"))), 
                InventoryObjectNode.asNodeList(deviceList));

        tree.resetTo(InventoryObjectNode.asNodeList(deviceList));
        tree.setSelectionMode(Grid.SelectionMode.MULTI);

        DragSourceExtension<SimpleTree> dragSource = new DragSourceExtension<>(tree);
        dragSource.setEffectAllowed(EffectAllowed.MOVE);

        return tree;
    }
    
    /**
     * Creates a simple tree, every device from the material installation is a root
     * @param deviceList a given device list
     * @return a simple tree
     */
    private Component createInstallationMaterialGrid(List<RemoteObjectLight> deviceList){
////        SimpleTree tree = new SimpleTree(
////                new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
////                    @Override
////                    public List<RemoteObjectLight> getChildren(RemoteObjectLight c) {
////                        try {
////                            return webserviceBean.getObjectChildren(
////                                c.getClassName(), 
////                                c.getId(), 
////                                -1, 
////                                Page.getCurrent().getWebBrowser().getAddress(),
////                                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
////                        } catch (ServerSideException ex) {
////                            Notifications.showError(ex.getLocalizedMessage());
////                            return new ArrayList<>();
////                        }
////                    }
////                }, 
////                new SimpleIconGenerator(webserviceBean, ((RemoteSession) UI.getCurrent().getSession().getAttribute("session"))), 
////                InventoryObjectNode.asNodeList(deviceList));
////        
////        tree.resetTo(InventoryObjectNode.asNodeList(deviceList));
////        tree.setSelectionMode(Grid.SelectionMode.MULTI);
////        
////        DragSourceExtension<SimpleTree> dragSource = new DragSourceExtension<>(tree);
////        dragSource.setEffectAllowed(EffectAllowed.MOVE);
////
////        return tree;
        Grid<LinkBean> grid = new Grid();
        grid.setSizeFull();
        
        List<LinkBean> items = new ArrayList();
        
        for (RemoteObjectLight device : deviceList) {        
            RemoteObjectLight endpointA = null;
            RemoteObjectLight objectLink = device;
            RemoteObjectLight endpointB = null;
            
            try {            
                List<RemoteObjectLight> theEndpointsA = webserviceBean.getSpecialAttribute(
                    device.getClassName(), 
                    device.getId(), 
                    "endpointA", 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                
                if (theEndpointsA != null && !theEndpointsA.isEmpty())
                    endpointA = theEndpointsA.get(0);
            } catch(ServerSideException ex) {
                Notifications.showError(ex.getMessage());
            }
            try {
                List<RemoteObjectLight> theEndpointsB = webserviceBean.getSpecialAttribute(
                    device.getClassName(), 
                    device.getId(), 
                    "endpointB", 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                
                if (theEndpointsB != null && !theEndpointsB.isEmpty())
                    endpointB = theEndpointsB.get(0);
            } catch(ServerSideException ex) {
                Notifications.showError(ex.getMessage());
            }
            if (endpointA != null || endpointB != null)
                items.add(new LinkBean(endpointA, objectLink, endpointB));
        }
        grid.addColumn(LinkBean::getEndpointA).setCaption("Endpoint A");
        grid.addColumn(LinkBean::getObjectLink).setCaption("Link");
        grid.addColumn(LinkBean::getEndpointB).setCaption("Endpoint B");
        grid.setItems(items);
        return grid;        
    }
    
    private class LinkBean {
        private RemoteObjectLight endpointA;
        private RemoteObjectLight objectLink;
        private RemoteObjectLight endpointB;
        
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

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
import org.kuwaiba.apis.web.gui.navigation.BasicIconGenerator;
import org.kuwaiba.apis.web.gui.navigation.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.navigation.nodes.ChildrenProvider;
import org.kuwaiba.apis.web.gui.navigation.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.navigation.trees.BasicTree;
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
        
    public ComponentConnectionCreator(List<RemoteObject> devicesList, WebserviceBean webserviceBean) {
        this.webserviceBean = webserviceBean;        
        setSizeFull();
        initializeComponent(devicesList);
    }
    
    @Override
    public final void setSizeFull() {
        super.setSizeFull();
    }
        
    private void initializeComponent(List<RemoteObject> devicesList) {
        //Sources devices and links
        List<RemoteObjectLight> deviceListLight = new ArrayList<>();
        List<RemoteObjectLight> linksListLight = new ArrayList<>();
        
        devicesList.forEach(device -> {
            
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
        
        Panel pnlLinks = new Panel("Connections Log");
        pnlLinks.setSizeFull();
        
        Grid gridLog = createInstallationMaterialGrid(linksListLight);
        
        pnlLinks.setContent(gridLog);
               
        HorizontalLayout lytConnection = new HorizontalLayout();
        lytConnection.setWidth(100, Unit.PERCENTAGE);
        lytConnection.setHeight(100, Unit.PERCENTAGE);
        lytConnection.setSpacing(true);
          
        //we create the connections tables
        Panel pnlConnections = new Panel();
        pnlConnections.setSizeFull();
        VerticalLayout verticalLayoutEndpointsA = createEndpointsA();
        Grid grdLinks = createEndpointsLinksGrid(linksListLight);
        VerticalLayout verticalLayoutEndpointsB = createEndpointsB();
        
        btnConnect.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                List<RemoteObjectLight> endpointsA = null;
                List<RemoteObjectLight> links = null;
                List<RemoteObjectLight> endpointsB = null;
                
                if (verticalLayoutEndpointsA.getComponentCount() > 0 && 
                    verticalLayoutEndpointsA.getComponent(0) instanceof BasicTree) {
                    
                    BasicTree simpleTree = (BasicTree) verticalLayoutEndpointsA.getComponent(0);
                    
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
                    verticalLayoutEndpointsB.getComponent(0) instanceof BasicTree) {
                    
                    BasicTree simpleTree = (BasicTree) verticalLayoutEndpointsB.getComponent(0);
                    
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
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()));

                sideBClassNames[i] = endpointsB.get(i).getClassName();
                sideBIds[i] = endpointsB.get(i).getId();

                linksClassNames[i] = links.get(i).getClassName();
                linksIds[i] = links.get(i).getId();
            }
            //we create the end points
            webserviceBean.connectPhysicalLinks(sideAClassNames, sideAIds, 
                    linksClassNames, linksIds, 
                    sideBClassNames, sideBIds, 
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            //we move the link from to under a new parent
            for (int i = 0; i < newLinksParents.size(); i++) {
                
                if (newLinksParents.get(i).getId() != null && !newLinksParents.get(i).getId().equals("-1")) { //Ignore the dummy root
                    webserviceBean.moveObjects(newLinksParents.get(i).getClassName(), newLinksParents.get(i).getId(), 
                            new String[] {linksClassNames[i]}, new String[] {linksIds[i]}, 
                        Page.getCurrent().getWebBrowser().getAddress(),
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
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
    
    private VerticalLayout createEndpointsA() {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidth(100, Unit.PERCENTAGE);
        verticalLayout.setHeight(100, Unit.PERCENTAGE);
        
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
                        RemoteObjectLight businessObject = new RemoteObjectLight(serializedObjectTokens[1], serializedObjectTokens[0], serializedObjectTokens[2]);
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
                        if (businessObject.getId() != null && !businessObject.getId().equals("-1") && (isGenericCommunicationsElement || isODF)) { //Ignore the dummy root
                            
                            BasicTree dynamicTree = new BasicTree(new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                                @Override
                                public List<RemoteObjectLight> getChildren(RemoteObjectLight parentObject) {
                                    try {       
                                        return webserviceBean.getChildrenOfClassLightRecursive(
                                            businessObject.getId(), 
                                            businessObject.getClassName(), 
                                            "GenericPhysicalPort", 
                                            0, 
                                            Page.getCurrent().getWebBrowser().getAddress(), 
                                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                        
                                    } catch (ServerSideException ex) {
                                        Notifications.showError(ex.getLocalizedMessage());
                                        return new ArrayList<>();
                                    }
                                }
                                    }, new BasicIconGenerator(webserviceBean, (RemoteSession) UI.getCurrent().getSession().getAttribute("session")),
                                    new AbstractNode<RemoteObjectLight>(businessObject) {
                                        @Override
                                        public AbstractAction[] getActions() { return new AbstractAction[0]; }

                                        @Override
                                        public void refresh(boolean recursive) { }
                                }
                            );
                            verticalLayout.removeAllComponents();
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
        verticalLayout.setHeight(100, Unit.PERCENTAGE);
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
                        RemoteObjectLight businessObject = new RemoteObjectLight(serializedObjectTokens[1], serializedObjectTokens[0], serializedObjectTokens[2]);
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
                        if (businessObject.getId() != null && businessObject.getId().equals("-1") && (isGenericCommunicationsElement || isODF)) { //Ignore the dummy root
                            BasicTree dynamicTree = new BasicTree(new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                                        @Override
                                        public List<RemoteObjectLight> getChildren(RemoteObjectLight parentObject) {
                                            try {
                                                return webserviceBean.getChildrenOfClassLightRecursive(
                                                    businessObject.getId(), 
                                                    businessObject.getClassName(), 
                                                    "GenericPhysicalPort", 
                                                    0, 
                                                    Page.getCurrent().getWebBrowser().getAddress(), 
                                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                            } catch (ServerSideException ex) {
                                                Notifications.showError(ex.getLocalizedMessage());
                                                return new ArrayList<>();
                                            }
                                        }
                                    }, new BasicIconGenerator(webserviceBean, (RemoteSession) UI.getCurrent().getSession().getAttribute("session")),
                                    new AbstractNode<RemoteObjectLight>(businessObject) {
                                        @Override
                                        public AbstractAction[] getActions() { return new AbstractAction[0]; }

                                        @Override
                                        public void refresh(boolean recursive) { }
                                }
                            );
                            verticalLayout.removeAllComponents();
                            verticalLayout.addComponent(dynamicTree);
                        }
                    }
                }
            }
        });
        return verticalLayout;
    }
    
    private List<RemoteObjectLight> getLinks(List<RemoteObjectLight> links) {
        List<RemoteObjectLight> result = new ArrayList();
        
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
        BasicTree tree = new BasicTree(
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
                new BasicIconGenerator(webserviceBean, ((RemoteSession) UI.getCurrent().getSession().getAttribute("session"))), 
                InventoryObjectNode.asNodeList(deviceList));

        tree.resetTo(InventoryObjectNode.asNodeList(deviceList));
        tree.setSelectionMode(Grid.SelectionMode.MULTI);

        DragSourceExtension<BasicTree> dragSource = new DragSourceExtension<>(tree);
        dragSource.setEffectAllowed(EffectAllowed.MOVE);

        return tree;
    }
    
    private List<LinkBean> getLinkBeans(List<RemoteObjectLight> links) {
        List<LinkBean> items = new ArrayList();
        
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
    private Grid createInstallationMaterialGrid(List<RemoteObjectLight> deviceList){
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

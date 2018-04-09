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
package org.kuwaiba.web.modules.physicalconnections.windows;

import com.vaadin.event.Action;
import com.vaadin.server.Page;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.nodes.AbstractNode;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.custom.tree.DynamicTree;

/**
 * Connection Link Tool to connect ports with physical links
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ConnectLinksWindow extends Window {
    private TopComponent parentComponent;
    
    public static String ENDPOINT_A_COLUMN_HEADER = "endpointA";
    private final String LINK_COLUMN_HEADER = "link";
    private final String ENDPOINT_B_COLUMN_HEADER = "endpointB";
    public static String IN_USE = "in-use";
    public static String FREE_ENDPOINT = "free";
    
    private final String btnConnectCaption = "Connect";
    
    private DynamicTree treeEndpointA;
    //private Table tblAvailableConnections;
    private DynamicTree treeEndpointB;
    
    private Panel pnlResult;
    /**
     * A property value change listener used for the trees and the table.
     */
    //private Property.ValueChangeListener generalValueChangeListener;
        
    public ConnectLinksWindow(TopComponent parentComponent, RemoteObjectLight connection) {
//        super("Connecting Links");
//        this.parentComponent = parentComponent;
//        
//        generalValueChangeListener = (Property.ValueChangeEvent event) -> {
//            updateLstResult();
//        };
//        
//        center();
//        setHeight("80%");
//        setWidth("80%");
//        setModal(true);
//        
//        List<RemoteObjectLight> links;
//        RemoteObjectLight [] containerEndpoints;        
//        try {
//            links = this.parentComponent.getWsBean().getObjectSpecialChildren(
//                    connection.getClassName(), 
//                    connection.getOid(), 
//                    Page.getCurrent().getWebBrowser().getAddress(), 
//                    this.parentComponent.getApplicationSession().getSessionId());
//                        
//            containerEndpoints = this.parentComponent.getWsBean().getConnectionEndpoints(
//                    connection.getClassName(), 
//                    connection.getOid(), 
//                    Page.getCurrent().getWebBrowser().getAddress(), 
//                    this.parentComponent.getApplicationSession().getSessionId());
//            
//            if (containerEndpoints[0] == null || containerEndpoints[1] == null){
//                Notification.show(
//                        String.format("Container %s is missing one of its endpoints", connection), 
//                        Notification.Type.ERROR_MESSAGE);
//                return;
//            }
//        } catch (ServerSideException ex) {
//            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
//            return;
//        }
//        VerticalLayout content = new VerticalLayout();
//        content.setMargin(true);
//        content.setSizeFull();
//        
//        Label lblInstructions = new Label("<b>Select the connection you wish to connect and at least one endpoint.</b>", ContentMode.HTML);
//        content.addComponent(lblInstructions);
//        content.setExpandRatio(lblInstructions, 0.05f);
//        
//        HorizontalLayout pnlLayout = new HorizontalLayout();
//        pnlLayout.setSizeFull();
//        
//        Panel pnlLeft = new Panel();
//        pnlLeft.setSizeFull();
//        
//        this.initTreeEndPointA(containerEndpoints[0]);
//        
//        pnlLeft.setContent(treeEndpointA);        
//        pnlLayout.addComponent(pnlLeft);        
//        pnlLayout.setExpandRatio(pnlLeft, 0.4f);
//        
//        Panel pnlCenter = new Panel();
//        pnlCenter.setSizeFull();
//        
//        VerticalLayout linksLayout = new VerticalLayout();
//        linksLayout.setSizeFull();
//                
//        this.initTblAvailableConnections(links);
//                        
//        linksLayout.addComponent(tblAvailableConnections);
//        linksLayout.setExpandRatio(tblAvailableConnections, 0.95f);
//        tblAvailableConnections.setSizeFull();        
//                
//        Button btnConnect = new Button(btnConnectCaption);
//        
//        btnConnect.addClickListener((Button.ClickEvent event) -> {
//            connectLinks();
//        });
//                
//        linksLayout.addComponent(btnConnect);
//        linksLayout.setExpandRatio(btnConnect, 0.05f);
//        btnConnect.setSizeFull();
//        
//        pnlCenter.setContent(linksLayout);
//        pnlLayout.addComponent(pnlCenter);
//        pnlLayout.setExpandRatio(pnlCenter, 0.2f);
//        
//        Panel pnlRight = new Panel();
//        pnlRight.setSizeFull();
//        
//        this.initTreeEndPointB(containerEndpoints[1]);
//                
//        pnlRight.setContent(treeEndpointB);
//        pnlLayout.addComponent(pnlRight);
//        pnlLayout.setExpandRatio(pnlRight, 0.4f);
//        
//        content.addComponent(pnlLayout);
//        content.setExpandRatio(pnlLayout, 0.85f);
//        
//        pnlResult = new Panel();
//        VerticalLayout lytResult = new VerticalLayout();
//        lytResult.setSizeUndefined();
//        pnlResult.setContent(lytResult);
//        pnlResult.setSizeFull();
//        
//        content.addComponent(pnlResult);
//        content.setExpandRatio(pnlResult, 0.1f);
//                
//        setContent(content);
    }
    
    private void initTreeEndPointA(RemoteObjectLight object) {
        EndpointNode rootNodeA = new EndpointNode(object);
        treeEndpointA = new DynamicTree(rootNodeA, parentComponent);
        //treeEndpointA.setDragMode(Tree.TreeDragMode.NONE);
        rootNodeA.setTree(treeEndpointA);
//        treeEndpointA.setSelectable(true);
//        treeEndpointA.setImmediate(true);
//        treeEndpointA.setMultiSelect(true);
//        treeEndpointA.setNullSelectionAllowed(true);
//                        
//        treeEndpointA.addActionHandler(new Action.Handler() {
//            
//            @Override
//            public Action[] getActions(Object target, Object sender) {
//                if (target instanceof AbstractNode) {
//                    AbstractAction [] actions = ((AbstractNode)target).getActions();
//                    List<Action> actionsList = new ArrayList();
//                    
//                    for (AbstractAction action : actions)
//                        actionsList.add(action);
//                    actionsList.add(new Action("Free"));
//                    
//                    return actionsList.toArray(new Action[0]);
//                }
//                return null;
//            }
//            
//            @Override
//            public void handleAction(Action action, Object sender, Object target) {
//                if (action.getCaption().equals("Free"))
//                    freeEndpoint(((EndpointNode) target).getObjectLink(), true);
//                else
//                    ((AbstractAction) action).actionPerformed(sender, target);
//            }
//        });
        
//        treeEndpointA.setItemStyleGenerator((Tree source, Object itemId) -> {
//            if (itemId instanceof EndpointNode) {
//                if (!((EndpointNode) itemId).isFree())
//                    return "disabled"; //NOI18N
//            }
//            return null;
//        });
        
        //treeEndpointA.addValueChangeListener(generalValueChangeListener);
    }
    
//    private void initTblAvailableConnections(List<RemoteObjectLight> links) {
//        tblAvailableConnections = new Table();
//        tblAvailableConnections.setSizeFull();
//        tblAvailableConnections.addContainerProperty(ENDPOINT_A_COLUMN_HEADER, String.class, null);
//        tblAvailableConnections.addContainerProperty(LINK_COLUMN_HEADER, RemoteObjectLight.class, null);
//        tblAvailableConnections.addContainerProperty(ENDPOINT_B_COLUMN_HEADER, String.class, null);
//        
//        tblAvailableConnections.setCellStyleGenerator((Table source, Object itemId, Object propertyId) -> {
//            if (propertyId != null) {
//                Item item = source.getItem(itemId);
//                String state = null;
//                
//                if (propertyId.equals(ENDPOINT_A_COLUMN_HEADER))
//                    state = (String) item.getItemProperty(propertyId).getValue();
//                
//                if (propertyId.equals(ENDPOINT_B_COLUMN_HEADER))
//                    state = (String) item.getItemProperty(propertyId).getValue();
//                
//                if (state != null && state.equals(IN_USE))
//                    return "disable"; //NOI18N
//            }
//            return null;
//        });
//                
//        for (RemoteObjectLight link : links) {
//            try {
//                List<RemoteObjectLight> aEndpointList = parentComponent.getWsBean().getSpecialAttribute(                        
//                        link.getClassName(),
//                        link.getOid(),
//                        "endpointA", //NOI18N
//                        Page.getCurrent().getWebBrowser().getAddress(),
//                        this.parentComponent.getApplicationSession().getSessionId());
//                
//                List<RemoteObjectLight> bEndpointList = parentComponent.getWsBean().getSpecialAttribute(
//                        link.getClassName(), 
//                        link.getOid(), 
//                        "endpointB", //NOI18N
//                        Page.getCurrent().getWebBrowser().getAddress(), 
//                        this.parentComponent.getApplicationSession().getSessionId());
//                
//                aEndpointList.equals(bEndpointList);
//                
//                String aEndpointConnected = FREE_ENDPOINT;
//                String bEndpointConnected = FREE_ENDPOINT;
//                
//                if (!aEndpointList.isEmpty()){
//                    aEndpointConnected = IN_USE;
//                }
//                if (!bEndpointList.isEmpty()){
//                    bEndpointConnected = IN_USE;
//                }
//                tblAvailableConnections.addItem(new Object[]{aEndpointConnected, link,  bEndpointConnected}, link);
//            }
//            catch (ServerSideException ex) {
//                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
//            }
//        }
//        tblAvailableConnections.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
//        tblAvailableConnections.addStyleName(Reindeer.TABLE_BORDERLESS) ;
//        tblAvailableConnections.addStyleName(ValoTheme.TABLE_NO_STRIPES);
//        tblAvailableConnections.addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
//        tblAvailableConnections.addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
//        tblAvailableConnections.setMultiSelect(true);
//        
//        tblAvailableConnections.setSelectable(true);
//        tblAvailableConnections.setImmediate(true);
//        
//        tblAvailableConnections.addActionHandler(new Action.Handler() {
//
//            @Override
//            public Action[] getActions(Object target, Object sender) {
//                
//                return new Action[] {
//                    new Action("Free Endpoint A"),
//                    new Action("Free Endpoint B"),
//                    new Action("Free Both Endpoints"),
//                };
//            }
//
//            @Override
//            public void handleAction(Action action, Object sender, Object target) {
//                if ("Free Endpoint A".equals(action.getCaption()))
//                    freeEndpoint((RemoteObjectLight) target, true);
//                
//                if ("Free Endpoint B".equals(action.getCaption()))
//                    freeEndpoint((RemoteObjectLight) target, false);
//                
//                if ("Free Both Endpoints".equals(action.getCaption())) {
//                    freeEndpoint((RemoteObjectLight) target, true);
//                    freeEndpoint((RemoteObjectLight) target, false);
//                }
//            }
//        });
//        tblAvailableConnections.addValueChangeListener(generalValueChangeListener);        
//    }
    
    private void freeEndpoint(RemoteObjectLight objectLink, boolean freeEndpointA) {        
//        if (objectLink == null)
//            return;
//        
//        Table table = tblAvailableConnections;
//        DynamicTree tree = treeEndpointA;
//        String endpoint = "endpointA"; //NOI18N
//        
//        if (freeEndpointA) {
//            
//            table.getItem(objectLink).getItemProperty(ENDPOINT_A_COLUMN_HEADER)
//                    .setValue(ConnectLinksWindow.FREE_ENDPOINT);
//        }
//        else {
//            
//            table.getItem(objectLink).getItemProperty(ENDPOINT_B_COLUMN_HEADER)
//                    .setValue(ConnectLinksWindow.FREE_ENDPOINT);
//            
//            tree = treeEndpointB;
//            endpoint = "endpointB"; //NOI18N
//        }
//        
//        try {
//            
//            List<RemoteObjectLight> portObjectList = parentComponent.getWsBean().getSpecialAttribute(
//                    objectLink.getClassName(),
//                    objectLink.getOid(),
//                    endpoint,
//                    Page.getCurrent().getWebBrowser().getAddress(),
//                    parentComponent.getApplicationSession().getSessionId());
//            
//            if (portObjectList.isEmpty())
//                return;
//            
//            RemoteObjectLight objectPort = portObjectList.get(0);
//            
//            for (Object itemId : tree.getItemIds()) {
//                
//                if (itemId instanceof EndpointNode) {
//                    if (((RemoteObjectLight)((EndpointNode) itemId).getObject()).equals(objectPort)) {
//                        ((EndpointNode) itemId).setObjectLink(null);
//                        ((EndpointNode) itemId).setFree(true);
//                                                
//                        parentComponent.getWsBean().releasePhysicalLink(
//                                objectLink.getClassName(), 
//                                objectLink.getOid(), 
//                                endpoint,
//                                objectPort.getClassName(),
//                                objectPort.getOid(),
//                                Page.getCurrent().getWebBrowser().getAddress(),
//                                parentComponent.getApplicationSession().getSessionId());
//                    }
//                }
//            }
//        } catch (ServerSideException ex) {
//            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
//        }
    }
    
    private void initTreeEndPointB(RemoteObjectLight object) {
        EndpointNode rootNodeB = new EndpointNode(object);
        treeEndpointB = new DynamicTree(rootNodeB, parentComponent);
//        treeEndpointB.setDragMode(Tree.TreeDragMode.NONE);
//        rootNodeB.setTree(treeEndpointB);
//        treeEndpointB.setSelectable(true);
//        treeEndpointB.setImmediate(true);
//        treeEndpointB.setMultiSelect(true);
//        treeEndpointB.setNullSelectionAllowed(true);
//        
//        treeEndpointB.addActionHandler(new Action.Handler() {
//            
//            @Override
//            public Action[] getActions(Object target, Object sender) {
//                
//                if (target instanceof AbstractNode) {
//                    AbstractAction [] actions = ((AbstractNode)target).getActions();
//                    List<Action> actionsList = new ArrayList();
//                    
//                    for (AbstractAction action : actions)
//                        actionsList.add(action);
//                    actionsList.add(new Action("Free"));
//                    
//                    return actionsList.toArray(new Action[0]);
//                }
//                return null;
//            }
//            
//            @Override
//            public void handleAction(Action action, Object sender, Object target) {
//                if (action.getCaption().equals("Free"))
//                    freeEndpoint(((EndpointNode) target).getObjectLink(), false);
//                else
//                    ((AbstractAction) action).actionPerformed(sender, target);
//            }
//        });
//        
//        treeEndpointB.setItemStyleGenerator((Tree source, Object itemId) -> {
//            if (itemId instanceof EndpointNode) {
//                if (!((EndpointNode) itemId).isFree())
//                    return "disabled"; //NOI18N
//            }
//            return null;
//        });
//        
//        treeEndpointB.addValueChangeListener(generalValueChangeListener);
    }
    
//    private EndpointNode [] getASelectedNodes() {
//        return ((Collection<EndpointNode>) treeEndpointA.getValue())
//                .toArray(new EndpointNode[0]);
//    }
//    
//    private RemoteObjectLight [] getLinksSelected() {
//        return ((Collection<RemoteObjectLight>) tblAvailableConnections.getValue())
//                .toArray(new RemoteObjectLight[0]);
//    }
//    
//    private EndpointNode [] getBSelectedNodes() {
//        return ((Collection<EndpointNode>) treeEndpointB.getValue())
//                .toArray(new EndpointNode[0]);
//    }
    
    private int currentNumberOfLinks() {
//        EndpointNode [] aSelectedNodes = getASelectedNodes();
//        RemoteObjectLight [] connections = getLinksSelected();
//        EndpointNode [] bSelectedNodes = getBSelectedNodes();
//        
//        int numberNodesA = aSelectedNodes == null ? 0 : aSelectedNodes.length;
//        int numberConnections = connections == null ? 0 : connections.length;
//        int numberNodesB = bSelectedNodes == null ? 0 : bSelectedNodes.length;
//        
//        if (numberNodesA >= numberConnections) {
//            if (numberNodesA >=  numberNodesB)
//                return numberNodesA;
//            else
//                return numberNodesB;
//        }
//        else {
//            if (numberConnections >= numberNodesB)
//                return numberConnections;
//            else
//                return numberNodesB;
//        }
        return 0;
    }
    
    private void updateLstResult() {
//        VerticalLayout lytResult = (VerticalLayout) pnlResult.getContent();
//        lytResult.removeAllComponents();
//        
//        int rows = currentNumberOfLinks();
//        
//        if (rows > 0) {
//            EndpointNode [] aSelectedNodes = getASelectedNodes();
//            RemoteObjectLight [] connections = getLinksSelected();
//            EndpointNode [] bSelectedNodes = getBSelectedNodes();
//            // 4 new labels for make a blank space
////            lytResult.addComponent(new Label(" ", ContentMode.HTML), 0);
////            lytResult.addComponent(new Label(" ", ContentMode.HTML), 0);
////            lytResult.addComponent(new Label(" ", ContentMode.HTML), 0);
////            lytResult.addComponent(new Label(" ", ContentMode.HTML), 0);
//            
//            for (int i = 0; i < rows; i += 1) {
//                String aSelectedObject;
//                String connection;
//                String bSelectedObject;
//
//                if (aSelectedNodes != null && aSelectedNodes.length > i)
//                    aSelectedObject = ((RemoteObjectLight) aSelectedNodes[i].getObject()).toString();                
//                else                
//                    aSelectedObject = "Free";                
//
//                if (connections != null && connections.length > i)
//                    connection = connections[i].toString();
//                else                
//                    connection = "No connection";
//
//                if (bSelectedNodes != null && bSelectedNodes.length > i)
//                    bSelectedObject = ((RemoteObjectLight) bSelectedNodes[i].getObject()).toString();                
//                else                
//                    bSelectedObject = "Free";
//
//
//                String result = String.format("<font color=\"blue\"> %s <-> %s <-> %s", 
//                    aSelectedObject, 
//                    connection, 
//                    bSelectedObject);
//
//                    lytResult.addComponent(new Label(result, ContentMode.HTML));
//            }            
//        }
    }
    
    private void connectLinks() {
//        List<String> results = new ArrayList();
//        
//        int rows = currentNumberOfLinks();
//        
//        if (rows > 0) {
//            EndpointNode [] aSelectedNodes = getASelectedNodes();
//            RemoteObjectLight [] connections = getLinksSelected();
//            EndpointNode [] bSelectedNodes = getBSelectedNodes();
//                    
//            for (int i = 0; i < rows; i += 1) {
//                String sideAClassName = null;
//                Long sideAId = null;
//                String linkClassName = null;
//                Long linkId = null;
//                String sideBClassName = null;
//                Long sideBId = null;
//
//                if (aSelectedNodes != null && aSelectedNodes.length > i) {
//                    sideAClassName = ((RemoteObjectLight) aSelectedNodes[i].getObject()).getClassName();
//                    sideAId = ((RemoteObjectLight) aSelectedNodes[i].getObject()).getOid();
//                }
//
//                if (connections != null && connections.length > i) {
//                    linkClassName = connections[i].getClassName();
//                    linkId = connections[i].getOid();
//                }           
//                else {
//                    results.add(String.format(
//                            "<font color=\"red\">%s", 
//                            "Select a link from the list"));
//                    continue;
//                }
//
//
//                if (bSelectedNodes != null && bSelectedNodes.length > i) {
//                    sideBClassName = ((RemoteObjectLight) bSelectedNodes[i].getObject()).getClassName();
//                    sideBId = ((RemoteObjectLight) bSelectedNodes[i].getObject()).getOid();
//                }
//
//                try {
//                    parentComponent.getWsBean().connectPhysicalLinks(
//                            new String[]{sideAClassName},
//                            new Long[]{sideAId},
//                            new String[]{linkClassName},
//                            new Long[]{linkId},
//                            new String[]{sideBClassName},
//                            new Long[]{sideBId},
//                            Page.getCurrent().getWebBrowser().getAddress(),
//                            parentComponent.getApplicationSession().getSessionId());
//
//                    results.add(String.format(
//                            "<font color=\"green\">%s", 
//                            "Connection was made successfully"));
//
//                    if (aSelectedNodes != null && aSelectedNodes.length > i) {
//                        aSelectedNodes[i].setObjectLink(connections[i]);
//                        aSelectedNodes[i].setFree(false);
//                    }
//
//                    if (bSelectedNodes != null && bSelectedNodes.length > i) {
//                        bSelectedNodes[i].setObjectLink(connections[i]);
//                        bSelectedNodes[i].setFree(false);
//                    }
//
//                    if (!tblAvailableConnections.getItem(connections[i])
//                            .getItemProperty(ENDPOINT_A_COLUMN_HEADER) 
//                            .getValue().equals(IN_USE)) {
//
//                        tblAvailableConnections.getItem(connections[i])
//                                .getItemProperty(ENDPOINT_A_COLUMN_HEADER) 
//                                .setValue(sideAClassName == null ? FREE_ENDPOINT : IN_USE);
//                    }
//
//                    if (!tblAvailableConnections.getItem(connections[i])
//                            .getItemProperty(ENDPOINT_B_COLUMN_HEADER)                        
//                            .getValue().equals(IN_USE)) {
//
//                        tblAvailableConnections.getItem(connections[i])
//                                .getItemProperty(ENDPOINT_B_COLUMN_HEADER) 
//                                .setValue(sideBClassName == null ? FREE_ENDPOINT : IN_USE);
//                    }
//
//                } catch (ServerSideException ex) {
//                    results.add(String.format(
//                            "<font color=\"red\">%s", 
//                            ex.getMessage()));
//                }
//            }
//            VerticalLayout lytResult = (VerticalLayout) pnlResult.getContent();
//
//            for (int i = 0; i < rows; i += 1) {
//                Label lblResult = (Label) lytResult.getComponent(i);
//                lblResult.setValue(lblResult.getValue() + " " + results.get(i));
//            }            
//            treeEndpointA.removeValueChangeListener(generalValueChangeListener);
//            tblAvailableConnections.removeValueChangeListener(generalValueChangeListener);
//            treeEndpointB.removeValueChangeListener(generalValueChangeListener);
//
//            treeEndpointA.setValue(Collections.emptySet());
//            tblAvailableConnections.setValue(Collections.emptySet());
//            treeEndpointB.setValue(Collections.emptySet());
//            
//            treeEndpointA.addValueChangeListener(generalValueChangeListener);
//            tblAvailableConnections.addValueChangeListener(generalValueChangeListener);
//            treeEndpointB.addValueChangeListener(generalValueChangeListener);
//        }
    }
    
    /*
    @Override
    public String getValidator() {
        return Constants.VALIDATOR_PHYSICAL_CONTAINER;
    }
    */
    @Override    
    public void close() {
//        treeEndpointA.removeValueChangeListener(generalValueChangeListener);
//        //tblAvailableConnections.removeValueChangeListener(generalValueChangeListener);
//        treeEndpointB.removeValueChangeListener(generalValueChangeListener);
//        super.close();
    }
}

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
package org.kuwaiba.web.modules.navtree;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.dashboards.widgets.AttachedFilesTabWidget;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.apis.web.gui.navigation.BasicIconGenerator;
import org.kuwaiba.apis.web.gui.navigation.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.navigation.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.navigation.trees.BasicTree;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.web.KuwaibaConst;
import org.kuwaiba.web.MainLayout;
import org.openide.util.Exceptions;

/**
 * The main component of the Navigation Tree module.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route(value = KuwaibaConst.PAGE_NAVIGATION_TREE, layout = MainLayout.class)
@PageTitle(KuwaibaConst.TITLE_NAVIGATION_TREE)
public class NavigationTreeComponent extends AbstractTopComponent {
    /**
     * Reference to the backend bean
     */
    @Inject
    private WebserviceBean webserviceBean;
    
    public NavigationTreeComponent() {
    }
    
    @Override
    public void registerComponents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void unregisterComponents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        RemoteSession remoteSession = UI.getCurrent().getSession().getAttribute(RemoteSession.class);
        setSizeFull();
        SplitLayout splitLayout = new SplitLayout();
        
        HierarchicalDataProvider dataProvider = new AbstractBackEndHierarchicalDataProvider<InventoryObjectNode, Void>() {
            @Override
            protected Stream<InventoryObjectNode> fetchChildrenFromBackEnd(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    RemoteObjectLight object = parent.getObject();
                    try {
                        List<RemoteObjectLight> children = webserviceBean.getObjectChildren(object.getClassName(), object.getId(), -1, remoteSession.getIpAddress(), remoteSession.getSessionId());
                        List<InventoryObjectNode> theChildren = new ArrayList();
                        for (RemoteObjectLight child : children)
                            theChildren.add(new InventoryObjectNode(child));
                        return theChildren.stream();
                    } catch (ServerSideException ex) {
                        Notification.show(ex.getMessage());
                        return new ArrayList().stream();
                    }
                } else {
                    return Arrays.asList(new InventoryObjectNode(new RemoteObjectLight(Constants.NODE_DUMMYROOT, "-1", "Root"))).stream();
                }
            }

            @Override
            public int getChildCount(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    RemoteObjectLight object = parent.getObject();
                    try {
                        return webserviceBean.getObjectChildren(object.getClassName(), object.getId(), -1, remoteSession.getIpAddress(), remoteSession.getSessionId()).size();
                    } catch (ServerSideException ex) {
                        Notification.show(ex.getMessage());
                        return 0;
                    }
                    
                } else
                    return 1;
            }

            @Override
            public boolean hasChildren(InventoryObjectNode node) {
                return true;
            }
        };
        BasicTree<InventoryObjectNode> basicTree = new BasicTree(dataProvider, new BasicIconGenerator(webserviceBean));
        basicTree.setSizeFull();
        
        basicTree.addItemClickListener(new ComponentEventListener<ItemClickEvent<InventoryObjectNode>>() {
            @Override
            public void onComponentEvent(ItemClickEvent<InventoryObjectNode> event) {
                InventoryObjectNode item = event.getItem();
                AttachedFilesTabWidget attachedFilesTabWidget = new AttachedFilesTabWidget(item.getObject(), webserviceBean);
                attachedFilesTabWidget.createContent();
                splitLayout.addToSecondary(attachedFilesTabWidget);
            }
        });        
        splitLayout.addToPrimary(basicTree);
        //splitLayout.addToSecondary(new Label(">>>"));
        splitLayout.setSplitterPosition(30);
        splitLayout.setSizeFull();
        add(splitLayout);
    }
        
}

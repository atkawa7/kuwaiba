/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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

import com.vaadin.cdi.CDIView;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.IconGenerator;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.apis.web.gui.navigation.AbstractNode;
import org.kuwaiba.apis.web.gui.navigation.ChildrenProvider;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.apis.web.gui.navigation.DynamicTree;
import org.kuwaiba.apis.web.gui.navigation.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.resources.ResourceFactory;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.web.IndexUI;

/**
 * The main component of the Navigation Tree module.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@CDIView("navtree")
class NavigationTreeComponent extends AbstractTopComponent {
    /**
     * View identifier
     */
    public static String VIEW_NAME = "navtree";
    /**
     * Text field to filter the services
     */
    private TextField txtFilter;
    /**
     * Layout for all the graphic components on the left side
     */
    private VerticalLayout lytLeftPanel;
    /**
     * The backend bean
     */
    @Inject
    private WebserviceBean wsBean;
    /**
     * The current session
     */
    private RemoteSession session;
    /**
     * The tree
     */
    private DynamicTree tree;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setStyleName("dashboards");
        addStyleName("misc");
        
        HorizontalSplitPanel pnlMain = new HorizontalSplitPanel();
        pnlMain.setSplitPosition(33, Unit.PERCENTAGE);
        MenuBar mnuMain = ((IndexUI)getUI()).getMainMenu();

        addComponent(mnuMain);
        addComponent(pnlMain);
        setExpandRatio(mnuMain, 0.5f);
        setExpandRatio(pnlMain, 9.5f);
        setSizeFull();
        
        this.session = ((RemoteSession) getSession().getAttribute("session"));
        this.lytLeftPanel = new VerticalLayout();

        this.txtFilter = new TextField();
        this.txtFilter.setWidth(100, Unit.PERCENTAGE);
        this.txtFilter.setPlaceholder("Search...");

        this.tree = new DynamicTree(new RemoteObjectLight(Constants.DUMMY_ROOT, -1, "Navigation Root"), new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                        @Override
                        public List<RemoteObjectLight> getChildren(RemoteObjectLight c) {
                            try {
                                return wsBean.getObjectChildren(c.getClassName(), 
                                    c.getId(), -1, Page.getCurrent().getWebBrowser().getAddress(),
                                    session.getSessionId());

                            } catch (ServerSideException ex) {
                                Notifications.showError(ex.getLocalizedMessage());
                                return new ArrayList<>();
                            }
                        }
                    }, new IconGenerator<AbstractNode>() {
                            @Override
                            public Resource apply(AbstractNode item) {

                                if (item instanceof InventoryObjectNode) { //It's not the root node
                                    RemoteObjectLight businessObject = (RemoteObjectLight)item.getObject();
                                    if (ResourceFactory.getInstance().isSmallIconCached(businessObject.getClassName()))
                                        return ResourceFactory.getInstance().getSmallIcon(businessObject.getClassName());
                                    else {
                                        try {
                                            RemoteClassMetadata classMetadata = wsBean.getClass(businessObject.getClassName(), Page.getCurrent().getWebBrowser().getAddress(),
                                                    session.getSessionId());
                                            return ResourceFactory.getInstance().getSmallIcon(classMetadata);
                                        } catch (ServerSideException ex) {
                                            Notifications.showError(ex.getLocalizedMessage());
                                            return ResourceFactory.DEFAULT_SMALL_ICON;
                                        }
                                    }
                                } else
                                    return VaadinIcons.STAR;


                            }
                });

        lytLeftPanel.addComponents(txtFilter, tree);
        lytLeftPanel.setExpandRatio(tree, 9);
        lytLeftPanel.setExpandRatio(txtFilter, 1);
        lytLeftPanel.setSizeFull();
        pnlMain.setFirstComponent(lytLeftPanel);
    }
    
        
    @Override
    public void registerComponents() {
        //tree.register();
    }

    @Override
    public void unregisterComponents() {
        //tree.unregister();
    }
}

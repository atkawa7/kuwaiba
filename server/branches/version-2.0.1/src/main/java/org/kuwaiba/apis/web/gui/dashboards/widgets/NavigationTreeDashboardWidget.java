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

package org.kuwaiba.apis.web.gui.dashboards.widgets;

import com.vaadin.ui.UI;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.dashboards.DashboardEventBus;
import org.kuwaiba.apis.web.gui.navigation.BasicIconGenerator;
import org.kuwaiba.apis.web.gui.navigation.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.navigation.nodes.ChildrenProvider;
import org.kuwaiba.apis.web.gui.navigation.trees.BasicTree;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * A simple widget that allows to embed a navigation tree into a dashboard widget. Other widgets can be subscribed to the selection and drag/drop events
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class NavigationTreeDashboardWidget extends AbstractDashboardWidget {
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;
    /**
     * The object currently representing the root of the tree
     */
    private RemoteObjectLight root;
    
    /**
     * Use this constructor if the root is other object but the Navigation Tree Root (a.k.a. DummyRoot)
     * @param root The object representing the root of the tree
     * @param eventBus The event bus to notify selection, updates and other events
     * @param wsBean Reference to the backend bean
     */
    public NavigationTreeDashboardWidget(RemoteObjectLight root, DashboardEventBus eventBus, WebserviceBean wsBean) {
        super("Navigation Tree", eventBus);
        this.root = root;
        this.wsBean = wsBean;
        this.setSizeFull();
        this.createContent();
    }
    
    /**
     * Use this constructor if the root is the navtree root
     * @param eventBus The event bus to notify selection, updates and other events
     * @param wsBean Reference to the backend bean
     */
    public NavigationTreeDashboardWidget(DashboardEventBus eventBus, WebserviceBean wsBean) {
        super("Navigation Tree", eventBus);
        this.wsBean = wsBean;
        this.setSizeFull();
        createContent();
    }

    @Override
    public void createCover() {
        throw new UnsupportedOperationException("This widget supports only embedded mode");
    }
    
    @Override
    public void createContent() {
        RemoteSession session = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
        this.contentComponent = new BasicTree(
            new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                    @Override
                    public List<RemoteObjectLight> getChildren(RemoteObjectLight c) {
                        try {
                            return wsBean.getObjectChildren(c.getClassName(), 
                                c.getId(), -1, session.getIpAddress(),
                                    session.getSessionId());

                        } catch (ServerSideException ex) {
                            Notifications.showError(ex.getLocalizedMessage());
                            return new ArrayList<>();
                        }
                    }
                }, new BasicIconGenerator(wsBean, session), 
                new AbstractNode<RemoteObjectLight>(root == null ? new RemoteObjectLight(Constants.DUMMY_ROOT, "-1", "Navigation Root") : root) {
                    @Override
                    public AbstractAction[] getActions() { return new AbstractAction[0]; }

                    @Override
                    public void refresh(boolean recursive) { }
            });
        
        addComponent(this.contentComponent);
    }
}

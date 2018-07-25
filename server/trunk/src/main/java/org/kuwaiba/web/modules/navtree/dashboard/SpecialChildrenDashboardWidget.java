/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.web.modules.navtree.dashboard;

import com.vaadin.server.Page;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.navigation.ChildrenProvider;
import org.kuwaiba.apis.web.gui.navigation.SimpleTree;
import org.kuwaiba.apis.web.gui.navigation.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.navigation.SimpleIconGenerator;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A widget that shows the special children (as in the special containment hierarchy)
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class SpecialChildrenDashboardWidget extends AbstractDashboardWidget {
    /**
     * Reference to the currently selected object
     */
    private RemoteObjectLight selectedObject;
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;
    public SpecialChildrenDashboardWidget(RemoteObjectLight selectedObject, WebserviceBean wsBean) {
        super(String.format("Special Children of %s", selectedObject));
        this.wsBean = wsBean;
        this.selectedObject = selectedObject;
        this.createCover();
    }

    @Override
    public void createCover() {
        VerticalLayout lytSpecialChildrenWidgetCover = new VerticalLayout();
        Label lblText = new Label("Special Children");
        lblText.setStyleName("text-bottomright");
        lytSpecialChildrenWidgetCover.addLayoutClickListener((event) -> {
            if (event.getButton() == MouseEventDetails.MouseButton.LEFT) {
                this.createContent();
                launch();
            }
        });
        
        lytSpecialChildrenWidgetCover.addComponent(lblText);
        lytSpecialChildrenWidgetCover.setSizeFull();
        lytSpecialChildrenWidgetCover.setStyleName("dashboard_cover_widget-darkred");
        this.coverComponent = lytSpecialChildrenWidgetCover;
        addComponent(coverComponent);
    }

    @Override
    public void createContent() {
        SimpleTree treeSpecialChildren = new SimpleTree(new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
                    @Override
                    public List<RemoteObjectLight> getChildren(RemoteObjectLight parentObject) {
                        try {
                            return wsBean.getObjectSpecialChildren(parentObject.getClassName(), 
                                    parentObject.getId(), Page.getCurrent().getWebBrowser().getAddress(), 
                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                        } catch (ServerSideException ex) {
                            Notifications.showError(ex.getLocalizedMessage());
                            return new ArrayList<>();
                        }
                    }
                }, new SimpleIconGenerator(wsBean, (RemoteSession) UI.getCurrent().getSession().getAttribute("session")),
                new InventoryObjectNode(selectedObject)
            );

        treeSpecialChildren.expand(treeSpecialChildren.getTreeData().getRootItems());
        VerticalLayout lytSpecialChildren = new VerticalLayout(treeSpecialChildren);
        lytSpecialChildren.setWidth(100, Unit.PERCENTAGE);

        this.contentComponent = lytSpecialChildren;
    }
}

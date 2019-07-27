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

package org.kuwaiba.web.modules.navtree.dashboard;

import com.vaadin.server.Page;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.navigation.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.navigation.trees.RelationshipsTree;
import org.kuwaiba.apis.web.gui.navigation.BasicIconGenerator;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectSpecialRelationships;

/**
 * A widget that mimics the old Relationship Explorer
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class RelationshipsDashboardWidget extends AbstractDashboardWidget {
    /**
     * Reference to the currently selected object
     */
    private RemoteObjectLight selectedObject;
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;
    public RelationshipsDashboardWidget(RemoteObjectLight selectedObject, WebserviceBean wsBean) {
        super(String.format("Relationships of %s", selectedObject));
        this.wsBean = wsBean;
        this.selectedObject = selectedObject;
        this.createCover();
    }

    @Override
    public void createCover() {
        VerticalLayout lytRelationshipsWidgetCover = new VerticalLayout();
        Label lblText = new Label("Relationships");
        lblText.setStyleName("text-bottomright");
        lytRelationshipsWidgetCover.addLayoutClickListener((event) -> {
            if (event.getButton() == MouseEventDetails.MouseButton.LEFT) {
                this.createContent();
                launch();
            }
        });
        
        lytRelationshipsWidgetCover.addComponent(lblText);
        lytRelationshipsWidgetCover.setSizeFull();
        lytRelationshipsWidgetCover.setStyleName("dashboard_cover_widget-darkgreen");
        this.coverComponent = lytRelationshipsWidgetCover;
        addComponent(coverComponent);
    }

    @Override
    public void createContent() {
        VerticalLayout lytRelationships = new VerticalLayout();
        try {
            RemoteObjectSpecialRelationships specialAttributes = 
                    wsBean.getSpecialAttributes(selectedObject.getClassName(), selectedObject.getId(),Page.getCurrent().getWebBrowser().getAddress(), 
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

            RelationshipsTree treeRelationships = new RelationshipsTree(new InventoryObjectNode(selectedObject), 
                                                    specialAttributes.asHashMap(), new BasicIconGenerator(wsBean, (RemoteSession) UI.getCurrent().getSession().getAttribute("session")));

            
            lytRelationships.addComponent(treeRelationships);
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getLocalizedMessage());
        }
        
        lytRelationships.setWidth(100, Unit.PERCENTAGE);
        this.contentComponent = lytRelationships;
    }
}

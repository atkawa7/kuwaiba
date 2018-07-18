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

import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A widget that mimics the old Relationship Explorer
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
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
//        DynamicTree treeRelationships = new DynamicTree(selectedObject, new ChildrenProvider<RemoteObjectLight, RemoteObjectLight>() {
//                    @Override
//                    public List<RemoteObjectLight> getChildren(RemoteObjectLight parentObject) {
//                        return new ArrayList<>();
//                        try {
//                            
//                        } catch (ServerSideException ex) {
//                            Notifications.showError(ex.getLocalizedMessage());
//                            return new ArrayList<>();
//                        }
//                    }
//                }, new SimpleIconGenerator(wsBean, (RemoteSession) UI.getCurrent().getSession().getAttribute("session")));

//        treeRelationships.expand(treeRelationships.getTreeData().getRootItems());
//        VerticalLayout lytSpecialChildren = new VerticalLayout(treeRelationships);
//        lytSpecialChildren.setWidth(100, Unit.PERCENTAGE);

        this.contentComponent = new Label("Not imlemented yet");
    }
}

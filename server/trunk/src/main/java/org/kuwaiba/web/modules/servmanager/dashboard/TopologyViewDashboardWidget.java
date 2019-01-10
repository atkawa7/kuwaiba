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
package org.kuwaiba.web.modules.servmanager.dashboard;

import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboard;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.web.modules.servmanager.views.TopologyView;

/**
 * A simple dashboard widget that displays the Topology View for the selected service
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class TopologyViewDashboardWidget extends AbstractDashboardWidget {
    /**
     * The service we want the resources from
     */
    private RemoteObjectLight service;
    /**
     * Web service bean reference
     */
    private WebserviceBean wsBean;
    
    public TopologyViewDashboardWidget(AbstractDashboard rootComponent, RemoteObjectLight service, WebserviceBean wsBean) {
        super("Topology View", rootComponent);
        this.service = service;
        this.wsBean = wsBean;
        this.createCover();
    }
    
    @Override
    public void createCover() {
        VerticalLayout lytViewsWidgetCover = new VerticalLayout();
        Label lblText = new Label(title);
        lblText.setStyleName("text-bottomright");
        lytViewsWidgetCover.addLayoutClickListener((event) -> {
            if (event.getButton() == MouseEventDetails.MouseButton.LEFT) {
                createContent();
                swap();
            }
        });
        
        lytViewsWidgetCover.addComponent(lblText);
        lytViewsWidgetCover.setSizeFull();
        lytViewsWidgetCover.setStyleName("dashboard_cover_widget-darkblue");
        this.coverComponent = lytViewsWidgetCover;
        addComponent(coverComponent);
    }

    @Override
    public void createContent() {
        VerticalLayout lytContent = new VerticalLayout();
        lytContent.addComponent(new TopologyView(service, wsBean));
            
        this.contentComponent = lytContent;
        
    }
}

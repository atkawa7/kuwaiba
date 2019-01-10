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

import com.vaadin.server.Page;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.beans.WebserviceBean;

/**
 * A simple dashboard widget that shows the resources associated to a service
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ResourcesDashboardWidget extends AbstractDashboardWidget {
    /**
     * The service we want the resources from
     */
    private RemoteObjectLight service;
    /**
     * Web service bean reference
     */
    private WebserviceBean wsBean;
    
    public ResourcesDashboardWidget(RemoteObjectLight service, WebserviceBean wsBean) {
        super("Service Resources");
        this.service = service;
        this.wsBean = wsBean;
        this.createCover();
    }
    
    @Override
    public void createCover() {
        VerticalLayout lytResourcesWidgetCover = new VerticalLayout();
        Label lblText = new Label(title);
        lblText.setStyleName("text-bottomright");
        lytResourcesWidgetCover.addLayoutClickListener((event) -> {
            if (event.getButton() == MouseEventDetails.MouseButton.LEFT) {
                createContent();
                launch();
            }
        });
        
        lytResourcesWidgetCover.addComponent(lblText);
        lytResourcesWidgetCover.setSizeFull();
        lytResourcesWidgetCover.setStyleName("dashboard_cover_widget-darkpink");
        this.coverComponent = lytResourcesWidgetCover;
        addComponent(coverComponent);       
     }

    @Override
    public void createContent() {
        try {
            List<RemoteObjectLight> serviceResources = wsBean.getServiceResources(service.getClassName(), service.getId(), Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            if (serviceResources.isEmpty())
                this.contentComponent = new Label("This service does not have resources associated to it");
            else {
                Grid<RemoteObjectLight> lstResources = new Grid<>();
                lstResources.setSizeFull();
                lstResources.addColumn(RemoteObjectLight::getClassName).setCaption("Resource Type");
                lstResources.addColumn(RemoteObjectLight::getName).setCaption("Resource Name");
                lstResources.setItems(serviceResources);
                
                VerticalLayout lytContacts = new VerticalLayout(lstResources);
                lytContacts.setWidth(100, Unit.PERCENTAGE);
                
                this.contentComponent = lytContacts;
            }
            
        } catch (ServerSideException ex) {
            this.contentComponent = new Label(ex.getMessage());
        }
    }
}

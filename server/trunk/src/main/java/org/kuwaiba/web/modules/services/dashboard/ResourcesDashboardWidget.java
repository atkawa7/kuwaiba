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
package org.kuwaiba.web.modules.services.dashboard;

import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A simple dashboard widget that shows the resources associated to a service
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class ResourcesDashboardWidget extends AbstractDashboardWidget {
    /**
     * The service we want the resources from
     */
    private RemoteObjectLight service;
    /**
     * Web service bean reference
     */
    private WebserviceBeanLocal wsBean;
    
    public ResourcesDashboardWidget(RemoteObjectLight service, WebserviceBeanLocal wsBean) {
        super();
        this.service = service;
        this.wsBean = wsBean;
        this.createCover();
        this.createContent();
    }
    
    @Override
    public void createCover() {
        Panel pnlResourcesWidgetCover = new Panel("Resources Associated to this Service");
        pnlResourcesWidgetCover.setContent(new VerticalLayout(new Label("X resources found"), new Button("See More...", (event) -> {
            flip();
        })));
        pnlResourcesWidgetCover.setSizeFull();
        this.coverComponent = pnlResourcesWidgetCover;
        
        addComponent(coverComponent);
    }

    @Override
    public void createContent() {
        Grid<RemoteObjectLight> lstResources = new Grid<>();
        lstResources.setHeaderVisible(false);
        lstResources.setSizeUndefined();
        this.contentComponent = lstResources;
        
        try {
            List<RemoteObjectLight> serviceResources = wsBean.getServiceResources(service.getClassName(), service.getId(), Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            lstResources.addColumn(RemoteObjectLight::getClassName);
            lstResources.addColumn(RemoteObjectLight::getName);
            lstResources.setItems(serviceResources);
        } catch (ServerSideException ex) {
            NotificationsUtil.showError(ex.getMessage());
        }
    }
}

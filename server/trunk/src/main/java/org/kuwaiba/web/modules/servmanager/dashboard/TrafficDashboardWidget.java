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
package org.kuwaiba.web.modules.servmanager.dashboard;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Embedded;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A simple dashboard widget that shows the resources associated to a service
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class TrafficDashboardWidget extends AbstractDashboardWidget {
    /**
     * The service we want the resources from
     */
    private RemoteObjectLight service;
    /**
     * Web service bean reference
     */
    private WebserviceBeanLocal wsBean;
    
    public TrafficDashboardWidget(RemoteObjectLight service, WebserviceBeanLocal wsBean) {
        super("Traffic");
        this.service = service;
        this.wsBean = wsBean;
        this.createContent();
    }
    
    @Override
    public void createCover() { }

    @Override
    public void createContent() {
        this.contentComponent = new Embedded("Sample Traffic", new ExternalResource("https://support.zabbix.com/secure/attachment/16643/graph.png"));
        addComponent(this.contentComponent);
    }
}

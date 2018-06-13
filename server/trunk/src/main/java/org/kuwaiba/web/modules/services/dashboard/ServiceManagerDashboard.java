/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.web.modules.services.dashboard;

import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboard;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * The dashboard used to show the information related to a given service
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class ServiceManagerDashboard extends AbstractDashboard {
    
    public ServiceManagerDashboard(RemoteObjectLight customer, RemoteObjectLight service, WebserviceBeanLocal wsBean) {
        super(service.toString(), 1, 3);
        this.addDashBoardWidget(new TrafficDashboardWidget(service, wsBean), 0, 0);
        this.addDashBoardWidget(new ResourcesDashboardWidget(service, wsBean), 0, 1);
        this.addDashBoardWidget(new ContactsDashboardWidget(customer, wsBean), 0, 2);
    }
}

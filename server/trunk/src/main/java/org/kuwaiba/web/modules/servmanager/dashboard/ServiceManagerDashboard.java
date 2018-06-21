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
package org.kuwaiba.web.modules.servmanager.dashboard;

import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboard;
import org.kuwaiba.apis.web.gui.dashboards.layouts.TheaterDashboardLayout;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.beans.WebserviceBean;

/**
 * The dashboard used to show the information related to a given service
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class ServiceManagerDashboard extends AbstractDashboard {
    
    public ServiceManagerDashboard(RemoteObjectLight customer, RemoteObjectLight service, WebserviceBean wsBean) {
        super(service.toString(), new TheaterDashboardLayout(3, 2));
        ((TheaterDashboardLayout)getDashboardLayout()).setScreenWidget(new TrafficDashboardWidget(service, wsBean));
        ((TheaterDashboardLayout)getDashboardLayout()).setChairWidget(0, 0, new ResourcesDashboardWidget(service, wsBean));
        ((TheaterDashboardLayout)getDashboardLayout()).setChairWidget(1, 0, new ContactsDashboardWidget(customer, wsBean));
        ((TheaterDashboardLayout)getDashboardLayout()).setChairWidget(2, 0, new ServiceViewsDashboardWidget(service, wsBean));
        ((TheaterDashboardLayout)getDashboardLayout()).setChairWidget(0, 1, new DummyDashboardWidget("Tickets", "dashboard_cover_widget-darkorange"));
        ((TheaterDashboardLayout)getDashboardLayout()).setChairWidget(1, 1, new DummyDashboardWidget("SLAs", "dashboard_cover_widget-darkpurple"));
        ((TheaterDashboardLayout)getDashboardLayout()).setChairWidget(2, 1, new DummyDashboardWidget("Reports", "dashboard_cover_widget-darkgrey"));
    }
}
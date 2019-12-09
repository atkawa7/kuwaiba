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

import com.neotropic.kuwaiba.modules.reporting.model.RemoteReport;
import com.vaadin.server.Page;
import com.vaadin.ui.UI;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboard;
import org.kuwaiba.apis.web.gui.dashboards.layouts.TheaterDashboardLayout;
import org.kuwaiba.apis.web.gui.dashboards.widgets.AttachedFilesDashboardWidget;
import org.kuwaiba.apis.web.gui.dashboards.widgets.SimpleLabelDashboardWidget;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.web.modules.navtree.dashboard.RelationshipsDashboardWidget;
import org.kuwaiba.web.modules.reports.dashboard.ReportShortcutWidget;
import org.kuwaiba.web.modules.servmanager.views.FormDashboardWidget;

/**
 * The dashboard used to show the information related to a given service
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ServiceManagerDashboard extends AbstractDashboard {
    
    public ServiceManagerDashboard(RemoteObjectLight customer, RemoteObjectLight service, WebserviceBean wsBean) {
        super(service.toString(), new TheaterDashboardLayout(3, 3));
        //((TheaterDashboardLayout)getDashboardLayout()).setScreenWidget(new ZabbixGraphDashboardWidget(service, wsBean));
        ((TheaterDashboardLayout)getDashboardLayout()).setScreenWidget(new SimpleLabelDashboardWidget(service.getName(), service.getClassName()));
        ((TheaterDashboardLayout)getDashboardLayout()).setChairWidget(0, 0, new ResourcesDashboardWidget(service, wsBean));
        ((TheaterDashboardLayout)getDashboardLayout()).setChairWidget(1, 0, new TopologyViewDashboardWidget(this, service, wsBean));
        ((TheaterDashboardLayout)getDashboardLayout()).setChairWidget(2, 0, new EndToEndViewDashboardWidget(this, service, wsBean));
        ((TheaterDashboardLayout)getDashboardLayout()).setChairWidget(0, 1, new RelationshipsDashboardWidget(service, wsBean));
        ((TheaterDashboardLayout)getDashboardLayout()).setChairWidget(1, 1, new FormDashboardWidget(this, service, wsBean));
        ((TheaterDashboardLayout)getDashboardLayout()).setChairWidget(2, 1, new AttachedFilesDashboardWidget(service, wsBean));
        ((TheaterDashboardLayout)getDashboardLayout()).setChairWidget(0, 2, new ContactsDashboardWidget(customer, wsBean));
        
        try {
            Object object = wsBean.getConfigurationVariableValue("org.kuwaiba.report.shortcut.report1", 
                Page.getCurrent().getWebBrowser().getAddress(), 
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            if (object != null) {
                String reportIdAsString = object.toString();

                long reportId = Long.valueOf(reportIdAsString);

                RemoteReport remoteReport = wsBean.getReport(reportId, 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

                if (remoteReport != null)
                    ((TheaterDashboardLayout)getDashboardLayout()).setChairWidget(1, 2, new ReportShortcutWidget(service, remoteReport, wsBean));
            }
                        
        } catch (ServerSideException | NumberFormatException ex) {
            Notifications.showError(ex.getMessage());
        }
        
    }
}

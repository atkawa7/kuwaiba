/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.web.modules.reports.dashboard;

import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.neotropic.kuwaiba.modules.reporting.model.RemoteReportLight;
import com.vaadin.server.ResourceReference;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.Calendar;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.resources.ResourceFactory;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A shortcut widget that shows a class level report
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ReportShortcutWidget extends AbstractDashboardWidget {
    private RemoteObjectLight businessObject;
    private final RemoteReportLight remoteReportLight;
    private final WebserviceBean webserviceBean;
    
    public ReportShortcutWidget(RemoteObjectLight businessObject, RemoteReportLight remoteReportLight, WebserviceBean webserviceBean) {
        super(remoteReportLight != null ? remoteReportLight.getName() : null);
        this.businessObject = businessObject;
        this.remoteReportLight = remoteReportLight;
        this.webserviceBean = webserviceBean;   
        this.createCover();
    }
    
    @Override
    public void createCover() {
        VerticalLayout lytContactsWidgetCover = new VerticalLayout();
        Label lblText = new Label(title);
        lblText.setStyleName("text-bottomright");
        lytContactsWidgetCover.addLayoutClickListener((event) -> {
            if (event.getButton() == MouseEventDetails.MouseButton.LEFT && remoteReportLight != null) {
                try {
                    byte[] reportBody = webserviceBean.executeClassLevelReport(businessObject.getClassName(), 
                            businessObject.getId(), remoteReportLight.getId(), Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

                    StreamResource fileStream = ResourceFactory.getFileStream(reportBody, businessObject.getClassName() + "_" + Calendar.getInstance().getTimeInMillis() + ".html");
                    fileStream.setMIMEType("text/html"); //NOI18N
                    setResource(String.valueOf(remoteReportLight.getId()), fileStream);
                    ResourceReference rr = ResourceReference.create(fileStream, this, String.valueOf(remoteReportLight.getId()));
                    Page.getCurrent().open(rr.getURL(), "Download Report", true);
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getLocalizedMessage());
                }
            }
        });
        
        lytContactsWidgetCover.addComponent(lblText);
        lytContactsWidgetCover.setSizeFull();
        lytContactsWidgetCover.setStyleName("dashboard_cover_widget-darkgreen");
        this.coverComponent = lytContactsWidgetCover;
        addComponent(coverComponent);
    }

    @Override
    public void createContent() { }
}

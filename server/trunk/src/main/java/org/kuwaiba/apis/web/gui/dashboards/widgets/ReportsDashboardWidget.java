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
package org.kuwaiba.apis.web.gui.dashboards.widgets;

import com.neotropic.kuwaiba.modules.reporting.model.RemoteReportLight;
import com.vaadin.server.Page;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Calendar;
import java.util.List;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.resources.ResourceFactory;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A widget that allows the user to launch predefined reports
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ReportsDashboardWidget extends AbstractDashboardWidget {
    /**
     * The reference to the business object the reports are related to
     */
    private RemoteObjectLight businessObject;
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;
    
    public ReportsDashboardWidget(RemoteObjectLight businessObject, WebserviceBean wsBean) {
        super("Reports");
        this.businessObject = businessObject;
        this.wsBean = wsBean;
        this.createCover();
    }

    @Override
    public void createCover() {
        VerticalLayout lytReportsWidgetCover = new VerticalLayout();
        Label lblText = new Label(title);
        lblText.setStyleName("text-bottomright");
        lytReportsWidgetCover.addLayoutClickListener((event) -> {
            if (event.getButton() == MouseEventDetails.MouseButton.LEFT) {
                this.createContent();
                launch();
            }
        });
        
        lytReportsWidgetCover.addComponent(lblText);
        lytReportsWidgetCover.setSizeFull();
        lytReportsWidgetCover.setStyleName("dashboard_cover_widget-darkgrey");
        this.coverComponent = lytReportsWidgetCover;
        addComponent(coverComponent);
    }

    @Override
    public void createContent() {
        try {
            RemoteSession session = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
            List<RemoteReportLight> classLevelReports = wsBean.getClassLevelReports(businessObject.getClassName(), true, false, session.getIpAddress(),
                    session.getSessionId());
            
            if (classLevelReports.isEmpty())
                this.contentComponent = new Label(String.format("The class %s does not have reports associated to it", businessObject.getClassName()));
            else {
                VerticalLayout lytReports = new VerticalLayout();
                Grid<RemoteReportLight> tblReports = new Grid<>();
                tblReports.setItems(classLevelReports);
                tblReports.setHeaderVisible(false);
                tblReports.addComponentColumn((source) -> {
                    Button btnReport = new Button(source.getName());
                    btnReport.setStyleName(ValoTheme.BUTTON_LINK);
                    btnReport.addClickListener((event) -> {
                        try {
                            byte[] reportBody = wsBean.executeClassLevelReport(businessObject.getClassName(), 
                                    businessObject.getId(), source.getId(), session.getIpAddress(),
                                    session.getSessionId());

                            StreamResource fileStream = ResourceFactory.getFileStream(reportBody, businessObject.getClassName() + "_" + Calendar.getInstance().getTimeInMillis() + ".html");
                            fileStream.setMIMEType("text/html"); //NOI18N
                            setResource(String.valueOf(source.getId()), fileStream);
                            ResourceReference rr = ResourceReference.create(fileStream, this, String.valueOf(source.getId()));
                            Page.getCurrent().open(rr.getURL(), "Download Report", true);
                        } catch (ServerSideException ex) {
                            Notifications.showError(ex.getLocalizedMessage());
                        }
                    });
                                        
                    return btnReport; 
                });
                tblReports.addColumn(RemoteReportLight::getDescription).setCaption("Description");
                tblReports.setSizeFull();
                
                lytReports.addComponent(tblReports);
                lytReports.setWidth(100, Unit.PERCENTAGE);
                this.contentComponent = lytReports;
            }
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getLocalizedMessage());
        }
    }
}

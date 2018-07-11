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

import com.neotropic.kuwaiba.modules.reporting.model.RemoteReportLight;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A widget that allows the user to launch predefined reports
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
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
        VerticalLayout lytContactsWidgetCover = new VerticalLayout();
        Label lblText = new Label(title);
        lblText.setStyleName("text-bottomright");
        lytContactsWidgetCover.addLayoutClickListener((event) -> {
            if (event.getButton() == MouseEventDetails.MouseButton.LEFT) {
                this.createContent();
                launch();
            }
        });
        
        lytContactsWidgetCover.addComponent(lblText);
        lytContactsWidgetCover.setSizeFull();
        lytContactsWidgetCover.setStyleName("dashboard_cover_widget-darkgrey");
        this.coverComponent = lytContactsWidgetCover;
        addComponent(coverComponent);
    }

    @Override
    public void createContent() {
        try {
            List<RemoteReportLight> classLevelReports = wsBean.getClassLevelReports(businessObject.getClassName(), true, false, Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            if (classLevelReports.isEmpty())
                Notifications.showInfo(String.format("The class %s does not have reports associated to it", businessObject.getClassName()));
            else {
                VerticalLayout lytReports = new VerticalLayout();
                Grid<RemoteReportLight> tblReports = new Grid<>();
                tblReports.setItems(classLevelReports);
                tblReports.addColumn(RemoteReportLight::getName).setCaption("Name");
                tblReports.addColumn(RemoteReportLight::getDescription).setCaption("Description");
                tblReports.setSizeFull();
                
                Button btnDownload = new Button("Download Report");
                btnDownload.setEnabled(false);
                
                tblReports.addItemClickListener((e) -> {
                    if (e.getMouseEventDetails().isDoubleClick()) {
                        try {
                            byte[] reportBody = wsBean.executeClassLevelReport(businessObject.getClassName(), 
                                    businessObject.getId(), e.getItem().getId(), Page.getCurrent().getWebBrowser().getAddress(),
                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                            
                            StreamResource fileStream = getFileStream(reportBody, businessObject.getClassName() + "_" + Calendar.getInstance().getTimeInMillis() + ".html");
                            FileDownloader fileDownloader = new FileDownloader(fileStream);
                            fileDownloader.extend(btnDownload);
                            btnDownload.setEnabled(true);
                        } catch (ServerSideException ex) {
                            Notifications.showError(ex.getLocalizedMessage());
                        }
                    }
                });
                
                lytReports.addComponents(new Label("Double click on a report to generate a download link"), tblReports, btnDownload);
                lytReports.setWidth(100, Unit.PERCENTAGE);
                lytReports.setComponentAlignment(btnDownload, Alignment.BOTTOM_CENTER);
                this.contentComponent = lytReports;
            }
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getLocalizedMessage());
        }
    }
    
    /**
     * Creates a file stream from a byte array, so it can be downloaded. Credits to https://vaadin.com/forum/thread/2864064
     * @param fileContents The contents of the file
     * @param fileName The name of the file
     * @return The stream to the file
     */
    private StreamResource getFileStream(byte[] fileContents, String fileName) {
        StreamResource.StreamSource source = new StreamResource.StreamSource() {
                @Override
                public InputStream getStream() {
                    InputStream input = new ByteArrayInputStream(fileContents);
                    return input;
            }
        };
  	StreamResource resource = new StreamResource ( source, fileName);
        resource.setCacheTime(-1);
        resource.setMIMEType("text/html");
        return resource;
    }
}

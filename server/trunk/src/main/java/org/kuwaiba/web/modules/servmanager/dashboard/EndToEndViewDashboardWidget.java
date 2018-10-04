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

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboard;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.modules.servmanager.views.EndToEndView;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.web.modules.servmanager.views.FormCreator;
import org.openide.util.Exceptions;

/**
 * A simple dashboard widget that shows the available views for a particular service
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class EndToEndViewDashboardWidget extends AbstractDashboardWidget {
    /**
     * The service we want the resources from
     */
    private RemoteObjectLight service;
    /**
     * Web service bean reference
     */
    private WebserviceBean wsBean;
    
    public EndToEndViewDashboardWidget(AbstractDashboard rootComponent, RemoteObjectLight service, WebserviceBean wsBean) {
        super("End to End View", rootComponent);
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
        lytViewsWidgetCover.setStyleName("dashboard_cover_widget-darkred");
        this.coverComponent = lytViewsWidgetCover;
        addComponent(coverComponent);
    }

    @Override
    public void createContent() {
        VerticalLayout lytContent = new VerticalLayout();
        try {
             String status = wsBean.getAttributeValueAsString(service.getClassName(), service.getId(), "Status", 
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            String bandwidth = wsBean.getAttributeValueAsString(service.getClassName(), service.getId(), "Bandwidth", 
                    Page.getCurrent().getWebBrowser().getAddress(),
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            Label lblTitle = new Label(service.getName());
            lblTitle.addStyleNames("header", "title");
        
            Label info = new Label(String.format("Status: %s - Bandwidth: %s" , status != null ? status : "<Not Set>", bandwidth != null ? bandwidth : "<Not Set>"));
            
            lytContent.addComponent(new HorizontalLayout(lblTitle, info));
        
            Button btnFormTable = new Button("Show Form", VaadinIcons.GRID_H);
            lytContent.addComponent(btnFormTable);

            btnFormTable.addClickListener(click ->{
                Window formWindow = new Window(" ");
                try {
                    FormCreator servManagerFormCreator = new FormCreator(service, wsBean, Page.getCurrent().getWebBrowser().getAddress(),
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                    formWindow.setContent(servManagerFormCreator.createForm());
                    formWindow.center();
                    UI.getCurrent().addWindow(formWindow);
                    
                } catch (ServerSideException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
            
            lytContent.addComponent(new EndToEndView(service, wsBean));
            
            this.contentComponent = lytContent;
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}

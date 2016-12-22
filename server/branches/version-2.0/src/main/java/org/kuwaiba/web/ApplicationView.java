/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web;

import com.google.common.eventbus.EventBus;
import org.kuwaiba.apis.web.gui.nodes.properties.ObjectNodeProperty;
import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import javax.inject.Inject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.web.custom.osp.GoogleMapsGISView;
import org.kuwaiba.web.custom.tree.TreeView;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;


/**
 * Main application component
 * @author Charles Edward Bedon Cortazar<charles.bedon@kuwaiba.org>
 */
@CDIView("app")
class ApplicationView extends CustomComponent implements View {
    static String VIEW_NAME = "app";
    
    EventBus eventBus = new EventBus();
    @Inject
    private WebserviceBeanLocal bean;
 
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        final RemoteSession session = (RemoteSession)getSession().getAttribute("session");
        
        if (session == null) //NOI18N
             getUI().getNavigator().navigateTo(LoginView.class.getName());
        else {
            setSizeFull();
            VerticalLayout lytRoot = new VerticalLayout();
            lytRoot.setSizeFull();
            Page.getCurrent().setTitle(String.format("%s - [%s]", "Kuwaiba Open Network Inventory", session.getUsername()));
            
            TreeView treeView = new TreeView(
                    new RemoteBusinessObjectLight(-1, "Navigation Tree Root", "Root"),
                    "Navigation Tree", eventBus);
            ObjectNodeProperty propertySheet = new ObjectNodeProperty(eventBus);
            
            eventBus.register(treeView);
            eventBus.register(propertySheet);
            
            VerticalSplitPanel pnlSplitExplorer = new VerticalSplitPanel(treeView, propertySheet);
            pnlSplitExplorer.setSplitPosition(70);
            pnlSplitExplorer.setSizeFull();
            
            GoogleMapsGISView gisView = new GoogleMapsGISView();
            
            HorizontalSplitPanel pnlSplitMain = new HorizontalSplitPanel(pnlSplitExplorer, gisView);
            pnlSplitMain.setSplitPosition(20);
            
            Button btnLogout = new Button("Logout");
            
            btnLogout.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    try {
                        bean.closeSession(session.getSessionId(), Page.getCurrent().getWebBrowser().getAddress());
                        getSession().setAttribute("session", null);
                        getUI().getNavigator().navigateTo(LoginView.VIEW_NAME);
                    } catch (ServerSideException ex) {
                        Notification ntfLoginError = new Notification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                        ntfLoginError.setPosition(Position.ASSISTIVE);
                        ntfLoginError.setDelayMsec(3000);
                        ntfLoginError.show(Page.getCurrent());
                    }
                }
            });
            
            lytRoot.addComponents(btnLogout, pnlSplitMain);
            lytRoot.setExpandRatio(pnlSplitMain, 2);
            setCompositionRoot(lytRoot);
        }
    }
}

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
import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.web.custom.osp.GoogleMapsGISView;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.web.modules.navtree.NavigationTreeModule;


/**
 * Main application component
 * @author Charles Edward Bedon Cortazar<charles.bedon@kuwaiba.org>
 */
@CDIView("app")
class ApplicationView extends CustomComponent implements View {
    static String VIEW_NAME = "app";
    
    EventBus eventBus = new EventBus();
    @Inject
    private WebserviceBeanLocal wsBean;
 
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
            
            NavigationTreeModule mdlNavTree = new NavigationTreeModule(eventBus, wsBean, session);        
            GoogleMapsGISView gisView = new GoogleMapsGISView(eventBus);
            
            HorizontalSplitPanel pnlSplitMain = new HorizontalSplitPanel(mdlNavTree.open(), gisView);
            pnlSplitMain.setSplitPosition(20);
            
            Button btnLogout = new Button("Logout");
            
            btnLogout.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    try {
                        wsBean.closeSession(session.getSessionId(), Page.getCurrent().getWebBrowser().getAddress());
                        getSession().setAttribute("session", null);
                        getUI().getNavigator().navigateTo(LoginView.VIEW_NAME);
                    } catch (ServerSideException ex) {
                        NotificationsUtil.showError(ex.getMessage());
                    }
                }
            });
            
            lytRoot.addComponents(btnLogout, pnlSplitMain);
            lytRoot.setExpandRatio(pnlSplitMain, 2);
            setCompositionRoot(lytRoot);
        }
    }
}

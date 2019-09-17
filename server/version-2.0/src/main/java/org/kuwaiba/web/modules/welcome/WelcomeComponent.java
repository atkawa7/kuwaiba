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
package org.kuwaiba.web.modules.welcome;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import javax.inject.Inject;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.web.IndexUI;
import org.kuwaiba.web.LoginView;
import org.kuwaiba.web.modules.osp.dashboard.SimpleMapDashboardWidget;

/**
 * The welcome screen
 * @author Charles Edward Bedon Cortazar{@literal <charles.bedon@kuwaiba.org>}
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@CDIView("welcome")
public class WelcomeComponent extends AbstractTopComponent implements View {
    /**
     * View identifier
     */
    public static String VIEW_NAME = "welcome";
    
    @Inject
    private WebserviceBean wsBean;
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        
        final RemoteSession session = (RemoteSession)getSession().getAttribute("session");
        
        if (session == null) 
             getUI().getNavigator().navigateTo(LoginView.VIEW_NAME);
        else {
            Page.getCurrent().setTitle(String.format("Kuwaiba Open Network Inventory - [%s]", session.getUsername()));
            
            VerticalLayout lytContent = new VerticalLayout();
            SimpleMapDashboardWidget wdtMap = new SimpleMapDashboardWidget("Geolocated Buildings", wsBean);
            lytContent.addComponent(wdtMap);
            lytContent.setSizeFull();
            
            MenuBar mnuMain = ((IndexUI)getUI()).getMainMenu();
            
            this.addComponents(mnuMain, lytContent);
            this.setExpandRatio(mnuMain, 0.2f);
            this.setExpandRatio(lytContent, 9.7f);
            this.setSizeFull();
        }
    }

    @Override
    public void registerComponents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void unregisterComponents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

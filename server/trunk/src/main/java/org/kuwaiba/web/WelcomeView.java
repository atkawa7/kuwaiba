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
package org.kuwaiba.web;

import com.google.common.eventbus.EventBus;
import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import javax.inject.Inject;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 * The welcome screen
 * @author Charles Edward Bedon Cortazar<charles.bedon@kuwaiba.org>
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@CDIView("welcome")
class WelcomeView extends VerticalLayout implements View {
    static String VIEW_NAME = "welcome";
    
    /**
     * The event bus used to share information (mostly objects and messages) between components of the same UI
     */
    EventBus eventBus = new EventBus();
    
    @Inject
    private WebserviceBean wsBean;
        
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        
        final RemoteSession session = (RemoteSession)getSession().getAttribute("session");
        
        if (session == null) //NOI18N
             getUI().getNavigator().navigateTo(LoginView.VIEW_NAME);
        else {
            Page.getCurrent().setTitle(String.format("%s - [%s]", "Kuwaiba Open Network Inventory", session.getUsername()));
            addStyleName("misc");
            setSizeFull();
            
            MenuBar mnuMain = ((IndexUI)getUI()).getMainMenu();
            
            addComponent(mnuMain);
        }
    }
}

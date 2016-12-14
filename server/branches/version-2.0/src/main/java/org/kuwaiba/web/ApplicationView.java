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
import com.neotropic.kuwaiba.web.nodes.properties.ObjectNodeProperties;
import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import javax.inject.Inject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.web.custom.googlemaps.GeographicInformation;
import org.kuwaiba.web.custom.tree.TreeView;
import org.kuwaiba.ws.toserialize.application.RemoteSession;


/**
 * Main application component
 * @author Charles Edward Bedon Cortazar<charles.bedon@kuwaiba.org>
 */
@CDIView("app")
class ApplicationView extends CustomComponent implements View {
    static String NAME = "app";
    
    EventBus eventBus = new EventBus();
    @Inject
    private WebserviceBeanLocal bean;
 
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        final RemoteSession session = (RemoteSession)getSession().getAttribute("session");
        
        if (session == null) //NOI18N
             getUI().getNavigator().navigateTo(LoginView.class.getName());
        else {
            VerticalLayout lytRoot = new VerticalLayout();
            lytRoot.setSizeFull();
            Page.getCurrent().setTitle(String.format("%s - [%s]", "Kuwaiba Open Network Inventory", session.getUsername()));

            /* Header */
            HorizontalLayout lytHeader = new HorizontalLayout();
            lytHeader.setWidth("100%");
            lytHeader.setHeight("10%");
            lytHeader.addStyleName("kuwaiba-light-official-background");
            
            Button btnLogout = new Button("Logout");
            
            btnLogout.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    try {
                        bean.closeSession(session.getSessionId(), Page.getCurrent().getWebBrowser().getAddress());
                        getSession().close();
                        getUI().getNavigator().navigateTo(LoginView.NAME);
                    } catch (ServerSideException ex) {
                        Notification ntfLoginError = new Notification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                        ntfLoginError.setPosition(Position.ASSISTIVE);
                        ntfLoginError.setDelayMsec(3000);
                        ntfLoginError.show(Page.getCurrent());
                    }
                }
            });
            
            lytHeader.addComponent(btnLogout);
            lytHeader.setComponentAlignment(btnLogout, Alignment.TOP_RIGHT);
            
            /*Content*/
            HorizontalLayout lytContent = new HorizontalLayout();
            lytContent.setHeight("90%");
            
            /*Left side panel*/
            TreeView treeNavigation = new TreeView(
                    new RemoteBusinessObjectLight(Long.valueOf(-1), "/", "Root"),
                    "Navigation Tree", eventBus);
            ObjectNodeProperties properties = new ObjectNodeProperties(eventBus);
            
            GeographicInformation geoInfo = new GeographicInformation();
            
            eventBus.register(treeNavigation); // subscribers
            eventBus.register(properties); // subscribers
            VerticalLayout lytLeftSide = new VerticalLayout(treeNavigation);
                        
            lytLeftSide.setHeight("100%");
            lytLeftSide.setWidth("30%");
            
            VerticalLayout lytCenter = new VerticalLayout();
            
            lytCenter.setWidth("50%");
            lytCenter.addComponent(geoInfo);
            
            VerticalLayout lytRightSide = new VerticalLayout();
            lytRightSide.setWidth("20%");
            lytRightSide.addComponent(properties);
            
            lytContent.addComponents(lytLeftSide, lytCenter, lytRightSide);
            lytRoot.addComponents(lytHeader, lytContent);

            setCompositionRoot(lytRoot);
        }
    }
}

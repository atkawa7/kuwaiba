/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.view;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import javax.inject.Inject;
import org.kuwaiba.beans.WebserviceBeanRemote;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.ws.toserialize.application.RemoteSession;

/**
 *
 * @author duckman
 */
@CDIView(value= "application")
public class ApplicationView extends VerticalLayout implements View {

    @Inject
    private WebserviceBeanRemote bean;
 
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        final RemoteSession session = (RemoteSession)getSession().getAttribute("session");
        
        if (session == null) //NOI18N
             getUI().getNavigator().navigateTo(LoginView.class.getName());
        else {
            setSizeFull();
            
            Button btnLogout = new Button("Logout");
            btnLogout.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    try {
                        bean.closeSession(session.getSessionId(), Page.getCurrent().getWebBrowser().getAddress());
                        getUI().getNavigator().navigateTo(LoginView.class.getName());
                    } catch (ServerSideException ex) {
                        Notification ntfLoginError = new Notification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                        ntfLoginError.setPosition(Position.BOTTOM_CENTER);
                        ntfLoginError.setDelayMsec(3000);
                        ntfLoginError.show(Page.getCurrent());
                    }
                }
            });
            
            addComponent(btnLogout);
        }
    }    
}

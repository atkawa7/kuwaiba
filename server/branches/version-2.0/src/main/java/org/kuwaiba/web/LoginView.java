/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import javax.inject.Inject;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.ws.toserialize.application.RemoteSession;
import org.vaadin.maddon.layouts.MVerticalLayout;

/**
 *
 * @author duckman
 */
@CDIView("")
class LoginView extends CustomComponent implements View {
    static String NAME = "";

    @Inject
    private WebserviceBeanLocal bean;
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setSizeFull();
        
        Page.getCurrent().setTitle(String.format("Kuwaiba Open Network Inventory - %s", "Login Page"));
        
        addStyleName("kuwaiba-light-official-background"); //NOI18N
        addStyleName("kuwaiba-light-white-color"); //NOI18N
        
        final TextField txtUsername = new TextField("Username");
        final PasswordField txtPassword = new PasswordField("Password");

        Button btnLogin = new Button("Login");
        btnLogin.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    RemoteSession aSession = bean.createSession(txtUsername.getValue(), //NOI18N
                            txtPassword.getValue(),  //NOI18N
                            Page.getCurrent().getWebBrowser().getAddress());
                    getSession().setAttribute("session", aSession); //NOI18N
                    getUI().getNavigator().navigateTo(ApplicationView.NAME);
                    
                } catch (ServerSideException ex) {
                    Notification ntfLoginError = new Notification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                    ntfLoginError.setPosition(Position.BOTTOM_CENTER);
                    ntfLoginError.setDelayMsec(3000);
                    ntfLoginError.show(Page.getCurrent());
                }
            }
        });

        Panel pnlLogin = new Panel();
        pnlLogin.setSizeUndefined();
        pnlLogin.addStyleName("kuwaiba-light-official-background"); //NOI18N
        pnlLogin.addStyleName("kuwaiba-light-white-color"); //NOI18N

        pnlLogin.setContent(new MVerticalLayout(txtUsername, txtPassword, btnLogin)
                .withAlign(btnLogin, Alignment.BOTTOM_RIGHT));
        
        btnLogin.focus();
        
        VerticalLayout lytFooterFiller = new VerticalLayout();
        lytFooterFiller.setWidth("100%"); //NOI18N
        lytFooterFiller.setHeight("30%"); //NOI18N
        
        Image anImage = new Image(null, 
                        new ThemeResource("img/neotropic_logo.png"));
        lytFooterFiller.setSpacing(true);
        
        Label lblFooterMessage = new Label(String.format("<center><small>%s <a style=\"color:white\" href=\"http://www.neotropic.co\"><b>%s</b></a> <br /> Network Management, Data Analysis and Free Software</small></center>", 
                        "This project is backed by", "Neotropic SAS"), ContentMode.HTML);
        
        lblFooterMessage.setStyleName("kuwaiba-light-white-color");
        
        lytFooterFiller.addComponents(lblFooterMessage, anImage);
        lytFooterFiller.setComponentAlignment(anImage, Alignment.MIDDLE_CENTER);
        
        Label lblTitle = new Label("Kuwaiba Open Network Inventory", ContentMode.HTML);
        lblTitle.setStyleName("title-1");
        
        setCompositionRoot(new MVerticalLayout(lblTitle, pnlLogin, lytFooterFiller)
                .withAlign(pnlLogin, Alignment.TOP_CENTER)
                .withAlign(lblTitle, Alignment.BOTTOM_CENTER)
                .withFullHeight());
    }
    
}

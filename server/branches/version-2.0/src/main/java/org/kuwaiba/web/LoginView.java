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

import com.vaadin.cdi.CDIView;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import javax.inject.Inject;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.vaadin.maddon.layouts.MVerticalLayout;

/**
 * Login form
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
@CDIView("")
class LoginView extends CustomComponent implements View {
    
    public static String VIEW_NAME = "";
    @Inject
    private WebserviceBeanLocal bean;
   
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setSizeFull();
        
        Page.getCurrent().setTitle(String.format("Kuwaiba Open Network Inventory - %s", "Login Page"));
        
        final TextField txtUsername = new TextField("Username");
        final PasswordField txtPassword = new PasswordField("Password");
        
        txtUsername.focus();
        txtPassword.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        txtPassword.setIcon(FontAwesome.LOCK);

        Button btnLogin = new Button("Login");
        btnLogin.setClickShortcut(KeyCode.ENTER);
        
        btnLogin.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    RemoteSession aSession = bean.createSession(txtUsername.getValue(), //NOI18N
                            txtPassword.getValue(),  //NOI18N
                            Page.getCurrent().getWebBrowser().getAddress());
                    getSession().setAttribute("session", aSession); //NOI18N
                    getUI().getNavigator().navigateTo(ApplicationView.VIEW_NAME);
                    
                } catch (ServerSideException ex) {
                    showNotification(new Notification(ex.getMessage(),
                    Notification.Type.ERROR_MESSAGE));
                    txtUsername.focus();
                }
            }
        });

        Panel pnlLogin = new Panel();
        pnlLogin.setSizeUndefined();

        pnlLogin.setCaption("<b>Kuwaiba Open Network Inventory</b>");
        pnlLogin.setContent(new MVerticalLayout(txtUsername, txtPassword, btnLogin)
                .withAlign(btnLogin, Alignment.BOTTOM_LEFT));
        
        btnLogin.focus();
        
        VerticalLayout lytFooterFiller = new VerticalLayout();
        lytFooterFiller.setWidth("100%"); //NOI18N
        lytFooterFiller.setHeight("30%"); //NOI18N
        
        setCompositionRoot(new MVerticalLayout(new VerticalLayout(), pnlLogin, lytFooterFiller)
                .withAlign(pnlLogin, Alignment.TOP_CENTER)
                .withFullHeight());
    }
    
       
    private void showNotification(Notification notification) {
        // keep the notification visible a little while after moving the
        // mouse, or until clicked
        notification.setPosition(Position.BOTTOM_CENTER);
        notification.setDelayMsec(3000);
        notification.show(Page.getCurrent());
    }
}

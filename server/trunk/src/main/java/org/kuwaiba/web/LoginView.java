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

import com.vaadin.cdi.CDIView;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * Login form
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
@CDIView("")
public class LoginView extends CustomComponent implements View {
    
    public static String VIEW_NAME = "";
    @Inject
    private WebserviceBeanLocal bean;
   
    private TextField txtUsername;
    private PasswordField txtPassword;
    private Button btnLogin;
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setStyleName("login");
        VerticalLayout lyt = new VerticalLayout();
        lyt.setStyleName("main");
        lyt.setSizeFull();
        
        lyt.addComponent(new Panel()); //Padding
        lyt.addComponent(buildLoginForm());
        lyt.addComponent(buildLoginFooter());
        setSizeFull();
        
        setCompositionRoot(lyt);

    }
    
    private Component buildLoginForm(){        
        txtUsername = new TextField();
        txtUsername.setWidth(18, Unit.EM);
        txtUsername.setPlaceholder("Username");
        
        txtPassword = new PasswordField();
        txtPassword.setWidth(18, Unit.EM);
        txtPassword.setPlaceholder("Password");
        
        btnLogin = new Button("Login");
        btnLogin.setIcon(FontAwesome.SIGN_IN);
        btnLogin.addStyleName(ValoTheme.BUTTON_LARGE);
        
        
        btnLogin.setClickShortcut(KeyCode.ENTER);
        btnLogin.addClickListener((Button.ClickEvent event) -> {
            try {
                RemoteSession aSession = bean.createSession(txtUsername.getValue(), //NOI18N
                        txtPassword.getValue(),  //NOI18N
                        Page.getCurrent().getWebBrowser().getAddress());
                getSession().setAttribute("session", aSession); //NOI18N
                getUI().getNavigator().navigateTo(ApplicationView.VIEW_NAME);
                
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
                txtUsername.focus();
            }
        });
        
        VerticalLayout lytForm = new VerticalLayout(txtUsername, txtPassword, btnLogin);
        lytForm.setSpacing(true);
        
        VerticalLayout lytloginPanel = new VerticalLayout();
        lytloginPanel.addStyleName("login-form");
        lytloginPanel.addComponents(lytForm);
        lytloginPanel.setSizeUndefined();
        
        return lytloginPanel;
     }
    
    private Component buildLoginFooter() {
        Image imgLogo = new Image(null, 
                            new ThemeResource("img/neotropic_logo.png"));
        
        Label lblCopyright = new Label("Copyright 2010-2018 Neotropic SAS");
        
        VerticalLayout lytFooter = new VerticalLayout(imgLogo, lblCopyright); 
        lytFooter.setWidth(100, Unit.PERCENTAGE);
        lytFooter.setStyleName("dark");
        lytFooter.addStyleName("v-align-right");
        
        lytFooter.setComponentAlignment(imgLogo, Alignment.BOTTOM_CENTER);
        lytFooter.setComponentAlignment(lblCopyright, Alignment.TOP_CENTER);
        return lytFooter;
    }
}

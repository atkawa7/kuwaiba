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
package org.kuwaiba.web;

import com.vaadin.cdi.CDIView;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 * Login form
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@CDIView("")
public class LoginView extends CustomComponent implements View {
    
    public static String VIEW_NAME = "";
    @Inject
    private WebserviceBean bean;
   
    private TextField txtUsername;
    private PasswordField txtPassword;
    private Button btnLogin;
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setStyleName("login");
        Page.getCurrent().setTitle("Kuwaiba Open Network Inventory");
        VerticalLayout lyt = new VerticalLayout();
        lyt.setStyleName("main");
        lyt.setSizeFull();

        HorizontalLayout lytLogo = new HorizontalLayout();
        Embedded imgCompanyLogo = new Embedded(null, new ThemeResource("img/company_logo.png"));
        lytLogo.addComponents(new HorizontalLayout(), new HorizontalLayout(), imgCompanyLogo);
        lytLogo.setComponentAlignment(imgCompanyLogo, Alignment.MIDDLE_RIGHT);
        lytLogo.setSizeFull();

        lyt.addComponent(lytLogo); //Padding
        lyt.addComponent(buildLoginForm());
        lyt.addComponent(buildLoginFooter());
        setSizeFull();
        
        setCompositionRoot(lyt);

    }
    
    private Component buildLoginForm() {        
        txtUsername = new TextField();
        txtUsername.setWidth(18, Unit.EM);
        txtUsername.setPlaceholder("User");
        txtUsername.focus();
        
        txtPassword = new PasswordField();
        txtPassword.setWidth(18, Unit.EM);
        txtPassword.setPlaceholder("Password");
        
        btnLogin = new Button("Login");
        btnLogin.setIcon(VaadinIcons.SIGN_IN);
        
        
        btnLogin.setClickShortcut(KeyCode.ENTER);
        btnLogin.addClickListener((Button.ClickEvent event) -> {
            try {
                RemoteSession aSession = bean.createSession(txtUsername.getValue(), //NOI18N
                        txtPassword.getValue(),  RemoteSession.TYPE_WEB,
                        Page.getCurrent().getWebBrowser().getAddress());
                getSession().setAttribute("session", aSession); //NOI18N
                //Update the login view to go to welcome view if the login was successful
                Page.getCurrent().reload();
                
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getMessage());
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
        
        Label lblCopyright = new Label("Copyright 2010-2019 <a style=\"color:white\" target=\"blank\" href=\"http://www.neotropic.co\">Neotropic SAS</a>", ContentMode.HTML);
        
        VerticalLayout lytFooter = new VerticalLayout(new HorizontalLayout(), imgLogo, lblCopyright); 
        lytFooter.setWidth(100, Unit.PERCENTAGE);
        lytFooter.setStyleName("dark");
        lytFooter.addStyleName("v-align-right");
        lytFooter.setExpandRatio(imgLogo, 3);
        lytFooter.setExpandRatio(lblCopyright, 2);
        lytFooter.setSizeFull();
        
        lytFooter.setComponentAlignment(imgLogo, Alignment.BOTTOM_CENTER);
        lytFooter.setComponentAlignment(lblCopyright, Alignment.TOP_CENTER);
        return lytFooter;
    }
}

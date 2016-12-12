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
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.Position;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import javax.inject.Inject;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.ws.toserialize.application.RemoteSession;
import org.vaadin.maddon.layouts.MVerticalLayout;

/**
 * Login view of kuwaiba
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
@CDIView("")
class LoginView extends CustomComponent implements View {
    public static String NAME = "";

    @Inject
    private WebserviceBeanLocal bean;
    
    private TextField txtUsername;
    private PasswordField txtPasswd;
    private Button btnLogin;
    private Button btnForgotPassword;
    
        
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setSizeFull();
        HorizontalLayout row = new HorizontalLayout();
        row.addStyleName(ValoTheme.LAYOUT_HORIZONTAL_WRAPPING);
        Component form = buildLoginForm();
        Component foot = buildLoginFoot();

        setCompositionRoot(new MVerticalLayout(row, form, foot)
                .withAlign(form, Alignment.MIDDLE_CENTER)
                .withAlign(foot, Alignment.BOTTOM_RIGHT)
                .withFullHeight());
    }
    
    private Component buildLoginForm(){
        Label lblTitle = new Label("Log in into Kuwaiba, Open network inventory");
        
        txtUsername = new TextField("Username: ");
        txtUsername.setWidth(17, Unit.EM);
        txtUsername.setInputPrompt("Type your username");
        
        txtPasswd = new PasswordField("Password: ");
        txtPasswd.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        txtPasswd.setIcon(FontAwesome.LOCK);
                
        VerticalLayout lytForm = new VerticalLayout(txtUsername, txtPasswd);
        lytForm.setSpacing(true);
        btnForgotPassword = new Button("Forgot Password?");
        btnForgotPassword.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                showNotification(new Notification("Hint: Try anything"));
            }
        });
        btnForgotPassword.addStyleName("v-button-link");
        btnForgotPassword.addStyleName("v-button-borderless");
        
        btnLogin = new Button("Login");
        btnLogin.setClickShortcut(KeyCode.ENTER);
        btnLogin.addClickListener((Button.ClickEvent event) -> {
            login();
        });

        HorizontalLayout lytButtons = new HorizontalLayout(btnForgotPassword, btnLogin);
        lytButtons.setSizeFull();
        lytButtons.setExpandRatio(btnForgotPassword, 1.0f);
        
        VerticalLayout lytloginPanel = new VerticalLayout();
        lytloginPanel.addStyleName("login-form");
        lytloginPanel.addComponents(lblTitle, lytForm, lytButtons);
        lytloginPanel.setSizeUndefined();
        
        return lytloginPanel;
     }
    
    private Component buildLoginFoot(){
                       
        Image logo = new Image(null, 
                        new ThemeResource("img/neotropic_logo.png"));
        logo.addStyleName("foot-label");
        logo.addStyleName("v-align-right");
     
        Label lblMessage = new Label("");
        lblMessage.addStyleName("v-align-right");

        HorizontalLayout lytTopFoot =  new HorizontalLayout(lblMessage,logo);
        lytTopFoot.setComponentAlignment(lblMessage, Alignment.BOTTOM_RIGHT);
        
        GridLayout grid =  new GridLayout(4,1);
        grid.setSizeFull();
        grid.addComponent(lytTopFoot, 3, 0);
        grid.setColumnExpandRatio(0, 0.2f);
        grid.setColumnExpandRatio(1, 0.2f);
        grid.setColumnExpandRatio(2, 0.2f);
        grid.setColumnExpandRatio(3, 0.4f);
        
        grid.setComponentAlignment(lytTopFoot, Alignment.BOTTOM_RIGHT);
        
        Label lblNeotropic = new Label("Backed by Neotropic SAS");
        lblNeotropic.addStyleName("foot-label");
        lblNeotropic.addStyleName("v-align-right");
        
        Label lblTerms = new Label("Terms");
        lblTerms.addStyleName("foot-label");
                
        HorizontalLayout lytLfoot = new HorizontalLayout(lblTerms);
        HorizontalLayout lytRfoot=  new HorizontalLayout(lblNeotropic);
        lytRfoot.setWidth("100%");
        
        HorizontalLayout lytBottomFoot = new HorizontalLayout(lytLfoot, lytRfoot);
        
        lytBottomFoot.setWidth("100%");
        lytBottomFoot.setExpandRatio(lytRfoot, 1.0f);

        lytBottomFoot.setExpandRatio(lytLfoot, 0.4f);
        lytBottomFoot.setExpandRatio(lytRfoot, 0.6f);
        
        VerticalLayout lytFoot = new VerticalLayout(grid, lytBottomFoot);
        lytFoot.setStyleName("foot");
        
        lytLfoot.setSizeUndefined();
        
        return lytFoot;
    }
    
    
    private void login() {
        
        try {
            RemoteSession aSession = bean.createSession(txtUsername.getValue(), //NOI18N
                    txtPasswd.getValue(),  //NOI18N
                    Page.getCurrent().getWebBrowser().getAddress());
            getSession().setAttribute("session", aSession); //NOI18N
            getUI().getNavigator().navigateTo(ApplicationView.NAME);

        } catch (ServerSideException ex) {
//            Notification ntfLoginError = new Notification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
//            ntfLoginError.setPosition(Position.BOTTOM_CENTER);
//            ntfLoginError.setDelayMsec(3000);
//            ntfLoginError.show(Page.getCurrent());
            
            showNotification(new Notification(ex.getMessage(),
                    Notification.Type.ERROR_MESSAGE));
            txtUsername.focus();
        }
    }
    
    private void showNotification(Notification notification) {
        // keep the notification visible a little while after moving the
        // mouse, or until clicked
        notification.setPosition(Position.BOTTOM_CENTER);
        notification.setDelayMsec(2000);
        notification.show(Page.getCurrent());
    }
}

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
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * Login form
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
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
        Label lblTitle = new Label("<h1>Log into Kuwaiba</h1>",ContentMode.HTML);
        //Label lblText = new Label("Open Network Inventory");
        
        txtUsername = new TextField();
        txtUsername.setWidth(18, Unit.EM);
        
        txtPassword = new PasswordField();
        txtPassword.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        txtPassword.setWidth(18, Unit.EM);
        txtPassword.setIcon(FontAwesome.LOCK);
        
        btnLogin = new Button();
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
        lytloginPanel.addComponents(lblTitle, lytForm);
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
        
        //Label lblTerms = new Label("Terms");
        //lblTerms.addStyleName("foot-label");
                
        HorizontalLayout lytLfoot = new HorizontalLayout();
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
}

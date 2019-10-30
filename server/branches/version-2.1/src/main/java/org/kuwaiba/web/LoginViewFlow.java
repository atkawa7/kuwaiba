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

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
//import com.vaadin.server.Page;
import javax.inject.Inject;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;

/**
 * Login form
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route("Login")
@PageTitle("Kuwaiba Open Network Inventory")
public class LoginViewFlow extends VerticalLayout {
    
    public static String VIEW_NAME = "";
    /**
     * Needed to manage the session
     */
    @Inject
    private WebserviceBean bean;
    /**
     * User name text field
     */
    private TextField txtUsername;
    /**
     * Password text field
     */
    private PasswordField txtPassword;
    /**
     * Button that uses the filled data to create a session
     */
    private Button btnLogin;

    /**
     * Default constructor
     */
    public LoginViewFlow() {
        initResources();
    }
    
    /**
     * Builds all visual components
     */
    public void initResources() {
        VerticalLayout lyt = new VerticalLayout();
        //lyt.setClassName("main");
        lyt.setSizeFull();

//        HorizontalLayout lytLogo = new HorizontalLayout();
//        Image imgCompanyLogo = new Image("./img/company_logo.png", "Kuwaiba Logo");
//        lytLogo.add(new HorizontalLayout(), new HorizontalLayout(), imgCompanyLogo);
//        lytLogo.setAlignItems(Alignment.CENTER);
//        lytLogo.setSizeFull();
//
//        lyt.add(lytLogo); //Padding
        lyt.add(buildLoginForm());
        lyt.add(buildLoginFooter());
        setSizeFull();
        add(lyt);
//        setCompositionRoot(lyt);

    }
    
    /**
     * Used to create the log in form
     * @return a form layout holding the needed fields to authenticate
     */
    private FormLayout buildLoginForm() {        
        txtUsername = new TextField();
        txtUsername.setWidth("18em");
        txtUsername.setPlaceholder("User");
        txtUsername.focus();
        
        txtPassword = new PasswordField();
        txtPassword.setWidth("18em");
        txtPassword.setPlaceholder("Password");
        
        Icon loginIcon = new Icon(VaadinIcon.SIGN_IN);
        btnLogin = new Button("Login", loginIcon);
        
        // This is used to press Enter instead of clicking the button
        btnLogin.addClickShortcut(Key.ENTER);
        
        
        btnLogin.addClickListener(event ->{
            getUI().ifPresent(ui -> { 
                RemoteSession aSession;
                try {
                    // Create the session object
                    aSession = bean.createSession(txtUsername.getValue(), //NOI18N
                            txtPassword.getValue(),  RemoteSession.TYPE_WEB,
                            UI.getCurrent().getRouter().getUrl(LoginViewFlow.class));
                    // Send the session object to browser's session
                    ui.getSession().setAttribute(RemoteSession.class, aSession);
                    // Navigate to Welcome page
                    ui.navigate("welcome");
                } catch (ServerSideException ex) {
                    System.out.println("Error on login: " + ex.getMessage());
                }
             });
        });
        FormLayout formResult = new FormLayout();
        formResult.addFormItem(txtUsername, "User Name");
        formResult.addFormItem(txtPassword, "Password");
        formResult.add(btnLogin);
        formResult.setWidth("400px");
        
        return formResult;
     }
    
    private VerticalLayout buildLoginFooter() {
        Image imgLogo = new Image("img/neotropic_logo.png", "Kuwaiba Logo");
        
        Div lblCopyright = new Div(new Html("Copyright 2010-2019 <a style=\"color:black\" target=\"blank\" href=\"http://www.neotropic.co\">Neotropic SAS</a>"));
        
        VerticalLayout lytFooter = new VerticalLayout(new HorizontalLayout(), imgLogo, lblCopyright); 
        lytFooter.setWidth("100%");
        lytFooter.setClassName("dark");
        lytFooter.addClassName("v-align-right");
//        lytFooter.setExpandRatio(imgLogo, 3);
//        lytFooter.setExpandRatio(lblCopyright, 2);
        lytFooter.setSizeFull();
        
//        lytFooter.setComponentAlignment(imgLogo, Alignment.CENTER);
        lytFooter.setAlignItems(Alignment.CENTER);
        return lytFooter;
    }
}

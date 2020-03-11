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
package org.neotropic.kuwaiba.web.ui;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.persistence.application.Session;
import org.neotropic.kuwaiba.core.persistence.exceptions.InventoryException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Login form
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route("")
public class LoginUI extends VerticalLayout implements BeforeEnterObserver {
    /**
     * User name text field.
     */
    private TextField txtUsername;
    /**
     * Password text field.
     */
    private PasswordField txtPassword;
    /**
     * Button that uses the filled data to create a session.
     */
    private Button btnLogin;
    /**
     * Reference to the Application Entity Manager to authenticate the user.
     */
    @Autowired
    private ApplicationEntityManager aem;

    /**
     * Default constructor
     */
    public LoginUI() {
        setSizeFull();
        add(new HorizontalLayout()); // Top filler
        add(new HorizontalLayout(new HorizontalLayout() /* Left filler */, 
                buildLoginForm(), /* Content */
                new HorizontalLayout() /* Right filler */));
        add(new HorizontalLayout() /* Left filler */, 
                buildLoginFooter(), /* Footer content */ 
                new HorizontalLayout() /* Right filler */);
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
                try {
                    // Create the session object
                    Session aSession = aem.createSession(txtUsername.getValue(), //NOI18N
                            txtPassword.getValue(), Session.TYPE_WEB,
                            UI.getCurrent().getSession().getBrowser().getAddress());
                    // Send the session object to browser's session
                    ui.getSession().setAttribute(Session.class, aSession);
                    // Navigate to Welcome page
                    //ui.navigate("welcome");
                    Notification.show("Authenticated");
                } catch (InventoryException ex) { // User not found is no longer caught. Generic exception for any other unexpected situation
                    Notification.show("Login or password incorrect");
                } catch (Exception ex) { 
                    Notification.show("An unexpected error occurred. Contact your administrator for details");
                    Logger.getLogger(LoginUI.class.getName()).log(Level.SEVERE, ex.getMessage());
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
        lytFooter.setSizeFull();
        lytFooter.setAlignItems(Alignment.CENTER);
        return lytFooter;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (UI.getCurrent().getSession().getAttribute(Session.class) != null) // If there is a session, redirect to the welcome page
            event.forwardTo(WelcomeUI.class);
    }
}

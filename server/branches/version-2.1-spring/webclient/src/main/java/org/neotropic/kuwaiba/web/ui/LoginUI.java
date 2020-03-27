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

import com.vaadin.flow.component.AttachEvent;
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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Login form
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Theme(Material.class)
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
     * Button that uses the data filled in by the user to create a session.
     */
    private Button btnLogin;
    /**
     * Reference to the Application Entity Manager to authenticate the user.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the internationalization service.
     */
    @Autowired
    private TranslationService ts;
    
    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        HorizontalLayout lytTopFiller =new HorizontalLayout();
        lytTopFiller.setSizeFull();
        add(lytTopFiller); // Top filler
        HorizontalLayout lytRightFiller = new HorizontalLayout();
        lytRightFiller.setSizeFull();
        HorizontalLayout lytLeftFiller = new HorizontalLayout();
        lytLeftFiller.setSizeFull();
        HorizontalLayout lytMidContent = new HorizontalLayout(lytLeftFiller /* Left filler */, 
                buildLoginForm(), /* Content */
                lytRightFiller /* Right filler */);
        lytMidContent.setSizeFull();
        add(lytMidContent);
        HorizontalLayout lytFooterContent = new HorizontalLayout(new HorizontalLayout() /* Left filler */, 
                buildLoginFooter(), /* Footer content */ 
                new HorizontalLayout() /* Right filler */);
        lytFooterContent.setSizeFull();
        add(lytFooterContent);
        getUI().ifPresent( ui -> ui.getPage().setTitle(ts.getTranslatedString("module.login.ui.title")));
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
                    ui.navigate(ServiceManagerDashboard.class);
                } catch (InventoryException ex) { // User not found is no longer caught. Generic exception for any other unexpected situation
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                            ts.getTranslatedString("module.login.ui.cant-login")).open();
                } catch (Exception ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), 
                            ts.getTranslatedString("module.general.messages.unexpected-error")).open();
                    Logger.getLogger(LoginUI.class.getName()).log(Level.SEVERE, ex.getMessage());
                }
             });
        });
        
        FormLayout lytForm = new FormLayout();
        lytForm.addFormItem(txtUsername, "User");
        lytForm.addFormItem(txtPassword, "Password");
        lytForm.add(btnLogin);
        lytForm.setSizeFull();
        
        return lytForm;
     }
    
    private VerticalLayout buildLoginFooter() {
        Image imgLogo = new Image("img/neotropic_logo.png", "Neotropic SAS Logo");
        Div lblCopyright = new Div(new Html("<span style=\"font-size:small\">Copyright 2010-2020 <a target=\"blank\" href=\"http://www.neotropic.co\">Neotropic SAS</a></span>"));
        VerticalLayout lytFooter = new VerticalLayout(new HorizontalLayout(), imgLogo, lblCopyright); 
        lytFooter.setAlignItems(Alignment.CENTER);
        return lytFooter;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (UI.getCurrent().getSession().getAttribute(Session.class) != null) // If there is an active session, redirect to the home page, else, show the login form
            event.forwardTo(HomeUI.class);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.authentication;

import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.WebBrowser;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.kuwaiba.beans.WebserviceBeanRemote;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.ws.toserialize.application.RemoteSession;

/**
 *
 * @author johnyortega
 */
//@CDIView("login")
public class LoginView extends CustomComponent implements View, Button.ClickListener {
    //@EJB
//    @Inject
    private WebserviceBeanRemote wsBean = lookupWebserviceBeanRemoteBean();
    
    public static final String NAME = "login";
    private final TextField user;
    private final PasswordField password;
    private final Button loginButton;
    
    public LoginView() {
        setSizeFull();
        user = new TextField("User:");
        user.setWidth("300px");
        user.setRequired(true);
        user.setInputPrompt("Your username");
        user.setInvalidAllowed(false);
        
        password = new PasswordField("Password:");
        password.setWidth("300px");
        password.addValidator(new PasswordValidator());
        password.setRequired(true);
        password.setValue("");
        password.setNullRepresentation("");
        
        loginButton = new Button("Login", this);
        
        VerticalLayout fields = new VerticalLayout(user, password, loginButton);
        fields.setCaption("Please login to access the application.");
        fields.setSpacing(true);
        fields.setMargin(new MarginInfo(true, true, true, false));
        fields.setSizeUndefined();
        
        VerticalLayout viewLayout = new VerticalLayout(fields);
        viewLayout.setSizeFull();
        viewLayout.setComponentAlignment(fields, Alignment.MIDDLE_CENTER);
        viewLayout.setStyleName(Reindeer.LAYOUT_BLUE);
        
        setCompositionRoot(viewLayout);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        user.focus();
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        if (!password.isValid())
            return;
        
        try {
            String username = user.getValue();
            String pw = password.getValue();
            WebBrowser webBrowser = Page.getCurrent().getWebBrowser();
            String ipAddress = webBrowser.getAddress();
        
            RemoteSession remoteSession = wsBean.createSession(username, pw, ipAddress);
            getSession().setAttribute("username", remoteSession.getUsername());
            getSession().setAttribute("sessionId", remoteSession.getSessionId());
            getSession().setAttribute("ipAddress", ipAddress);
            //getSession().setAttribute(RemoteSession.class, remoteSession);
            
            getUI().getNavigator().navigateTo(LoginMainView.NAME);
        } catch (Exception e) {
            if (e instanceof ServerSideException) {
                try {
                    throw e;
                } catch (Exception ex) {
                    Logger.getLogger(LoginView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else {
                //user.setValue("username = " +  user.getValue() + " password = " + password.getValue());
                password.setValue(null);
                password.focus();
                System.out.println("[KUWAIBA] An unexpected error occurred in createSession: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    private static final class PasswordValidator extends AbstractValidator<String> {

        public PasswordValidator() {
            super("The password provided is not valid");
        }

        @Override
        protected boolean isValidValue(String value) {
            return true;
        }

        @Override
        public Class<String> getType() {
            return String.class;
        }
    }
    
    private WebserviceBeanRemote lookupWebserviceBeanRemoteBean() {
        try {
            Context c = new InitialContext();
            return (WebserviceBeanRemote) c.lookup("java:global/KuwaibaServer-ear-1.0.0/KuwaibaServer-ejb-1.0.0/WebserviceBean!org.kuwaiba.beans.WebserviceBeanRemote");
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }
}
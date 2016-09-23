/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.view;

import com.vaadin.cdi.CDIView;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.VerticalLayout;
import javax.inject.Inject;
import org.kuwaiba.beans.WebserviceBeanRemote;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.ws.toserialize.application.RemoteSession;

/**
 *
 * @author duckman
 */
@CDIView(value = "login")
public class LoginView extends VerticalLayout implements View {
    
    @Inject
    private WebserviceBeanRemote bean;
    
    private TextField txtUsername; 
    private PasswordField txtPassword;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Page.getCurrent().setTitle(String.format("Kuwaiba Open Network Inventory - %s", "Login Page"));
        
        setSizeFull();
        addStyleName("kuwaiba-light-official-background"); //NOI18N
        addStyleName("kuwaiba-light-white-color"); //NOI18N
        
        /*Header*/
        VerticalLayout lytHeaderFiller = new VerticalLayout();
        lytHeaderFiller.setWidth("100%"); //NOI18N
        lytHeaderFiller.setHeight("30%"); //NOI18N
        
        Label lblTitle = new Label(String.format("<h1><center>%s</center></h1>", "Kuwaiba Open Network Inventory"), ContentMode.HTML);        
        lytHeaderFiller.addComponent(lblTitle);
        lytHeaderFiller.setComponentAlignment(lblTitle, Alignment.BOTTOM_CENTER);
        
        /*Content*/
        HorizontalLayout lytCenterFiller = new HorizontalLayout();
        lytCenterFiller.setWidth("100%"); //NOI18N
        
        VerticalLayout lytLeftFiller = new VerticalLayout();
        
        FormLayout lytLogin = new FormLayout();
        
        VerticalLayout lytRightFiller = new VerticalLayout();
        
        lytCenterFiller.addComponents(lytLeftFiller, 
                new VerticalLayout(),
                lytLogin,
                new VerticalLayout(),
                lytRightFiller);
        
        /*Footer*/
        VerticalLayout lytFooterFiller = new VerticalLayout();
        lytFooterFiller.setWidth("100%"); //NOI18N
        lytFooterFiller.setHeight("30%"); //NOI18N
        
        Image anImage = new Image(null, 
                        new ThemeResource("img/neotropic_logo.png"));
        lytFooterFiller.setSpacing(true);
        lytFooterFiller.addComponents(new Label(String.format("<center><small>%s <a style=\"color:white\" href=\"http://www.neotropic.co\"><b>%s</b></a> <br /> Network Management, Data Analysis and Free Software</small></center>", 
                        "This project is backed by", "Neotropic SAS"), ContentMode.HTML), anImage);
        lytFooterFiller.setComponentAlignment(anImage, Alignment.MIDDLE_CENTER);
                
        /*The form*/
        txtUsername = new TextField("User Name");
        txtPassword = new PasswordField("Password");
        Button btnLogin = new Button("Login");
        
        lytLogin.addComponents(txtUsername, txtPassword, btnLogin);
        
        btnLogin.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    RemoteSession aSession = bean.createSession(txtUsername.getValue(), txtPassword.getValue(), 
                            Page.getCurrent().getWebBrowser().getAddress());
                    getSession().setAttribute("session", aSession);
                    getUI().getNavigator().navigateTo(ApplicationView.class.getName());
                    
                } catch (ServerSideException ex) {
                    Notification ntfLoginError = new Notification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                    ntfLoginError.setPosition(Position.BOTTOM_CENTER);
                    ntfLoginError.setDelayMsec(3000);
                    ntfLoginError.show(Page.getCurrent());
                }
            }
        });
        
        addComponents(lytHeaderFiller, lytCenterFiller, lytFooterFiller);
    }
}

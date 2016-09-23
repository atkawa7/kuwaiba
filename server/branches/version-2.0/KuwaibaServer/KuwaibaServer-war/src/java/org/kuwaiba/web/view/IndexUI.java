/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.view;

import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

/**
 *
 * @author duckman
 */
@Theme("kuwaiba-light")
@CDIUI(value = "index")
public class IndexUI extends UI{

    @Override
    protected void init(VaadinRequest request) {
        Navigator navigator = new Navigator(this, this);
        navigator.addView(LoginView.class.getName(), LoginView.class);
        navigator.addView(ApplicationView.class.getName(), ApplicationView.class);
        setNavigator(navigator);

        if (getSession().getAttribute("session") == null)  //NOI18N
            getNavigator().navigateTo(LoginView.class.getName());
        
        else
            getNavigator().navigateTo(ApplicationView.class.getName());
    }
    
}

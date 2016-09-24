/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web;

import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import javax.inject.Inject;

/**
 *
 * @author duckman
 */
@Theme("kuwaiba-light")
@CDIUI("")
public class IndexUI extends UI {
    @Inject
    CDIViewProvider viewProvider;
    
    @Override
    protected void init(VaadinRequest request) {
        Navigator navigator = new Navigator(this, this);
        navigator.addProvider(viewProvider);
        
        if (getSession().getAttribute("session") == null)
            navigator.navigateTo(LoginView.NAME);
        else
            navigator.navigateTo(ApplicationView.NAME);
        
        
    }
    
}

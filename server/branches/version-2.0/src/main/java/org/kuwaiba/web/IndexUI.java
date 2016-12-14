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

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;
import javax.inject.Inject;

/**
 * Main application entry point. It also serves as the fallback controller
 * @author Charles Edward Bedon Cortazar<charles.bedon@kuwaiba.org>
 */
@Theme("nuqui")
@CDIUI("")
@SuppressWarnings("serial")
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
    
    @VaadinServletConfiguration(productionMode = false, ui = IndexUI.class, widgetset = "org.kuwaiba.KuwaibaWidgetSet")
    public static class Servlet extends VaadinServlet {
    }
}
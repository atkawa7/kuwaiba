/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.inventory.vaadin.push;

import com.vaadin.annotations.Push;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.server.VaadinCDIServlet;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import javax.inject.Inject;

/**
 * Vaadin Push is an application use to explore the concepts of push and async. 
 * The functionality are open windows async meanwhile the user do any other thing
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@CDIUI("")
@Theme("mytheme")
@SuppressWarnings("serial")
@Push
public class MyUI extends UI {
    
    @Inject
    Service service;
    
    private Button btnOpenWindow;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        VerticalLayout vly = new VerticalLayout();
        
        btnOpenWindow = new Button("Open a new window");
        btnOpenWindow.addClickListener(e -> {
            service.runService(this);
        });
        Button btnDoAnyOtherThing = new Button("Do any other thing");
        btnDoAnyOtherThing.addClickListener(e -> {
            vly.addComponent(new Label("Hello"));
        });
        vly.addComponents(btnOpenWindow);
        vly.addComponents(btnDoAnyOtherThing);
        
        setContent(vly);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinCDIServlet {
    }
}

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

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.cdi.server.VaadinCDIServlet;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import java.io.IOException;
import java.net.URL;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
            navigator.navigateTo(LoginView.VIEW_NAME);
        else
            navigator.navigateTo(ApplicationView.VIEW_NAME);
    }
    
    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = IndexUI.class, widgetset = "org.kuwaiba.KuwaibaWidgetSet")
    public static class Servlet extends VaadinCDIServlet {
        
        @Override
        protected void writeStaticResourceResponse(HttpServletRequest request,
                HttpServletResponse response, URL resourceUrl) throws IOException {

            /* Optimized widgetset serving */
            if (resourceUrl.getFile().contains("/widgetsets/")
                    && (resourceUrl.getFile().endsWith(".js") || resourceUrl.
                    getFile().endsWith(".css"))) {
                URL gzipurl = new URL(resourceUrl.toString() + ".gz");
                response.setHeader("Content-Encoding", "gzip");
                super.writeStaticResourceResponse(request, response, gzipurl);
                return;
            }
            super.writeStaticResourceResponse(request, response, resourceUrl);
        }
    }
}
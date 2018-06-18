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
import com.vaadin.server.Page;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.web.modules.servmanager.ServiceManagerView;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.web.procmanager.ProcessManagerView;

/**
 * Main application entry point. It also serves as the fallback controller
 * @author Charles Edward Bedon Cortazar<charles.bedon@kuwaiba.org>
 */
@Theme("nuqui")
@CDIUI("")
@SuppressWarnings("serial")
public class IndexUI extends UI {
    @Inject
    private CDIViewProvider viewProvider;
    /**
     * The reference to the back end bean
     */
    @Inject
    WebserviceBean wsBean;
    
    private Navigator navigator;
    /**
     * Main menu
     */
    private MenuBar mnuMain;
        
    @Override
    protected void init(VaadinRequest request) {
        this.navigator = new Navigator(this, this);
        this.navigator.addProvider(viewProvider);
        
        if (getSession().getAttribute("session") == null)
            this.navigator.navigateTo(LoginView.VIEW_NAME);
        else {
            this.mnuMain = new MenuBar();
            this.mnuMain.setStyleName("misc-main");
            this.mnuMain.setWidth("100%");

            MenuItem menuItem = this.mnuMain.addItem("Processes", null);

            try {
                List<RemoteProcessDefinition> processDefinitions = wsBean.getProcessDefinitions(
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) getSession().getAttribute("session")).getSessionId());
                
                for (RemoteProcessDefinition processDefinition : processDefinitions) {
                    
                    menuItem.addItem(processDefinition.getName(), null, new MenuBar.Command() {
                        @Override
                        public void menuSelected(MenuBar.MenuItem selectedItem) {
                            getSession().setAttribute("selectedProcessDefinition", processDefinition);
                            IndexUI.this.navigator.navigateTo(ProcessManagerView.VIEW_NAME);                            
                        }
                    });
                }
                
            } catch (ServerSideException ex) {
                NotificationsUtil.showError(ex.getMessage());
            }



            this.mnuMain.addItem("Service Manager", new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    getUI().getNavigator().navigateTo(ServiceManagerView.VIEW_NAME);
                }
            });

            this.mnuMain.addItem("Log Out", new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    RemoteSession session = (RemoteSession) getSession().getAttribute("session");
                    try {
                        wsBean.closeSession(session.getSessionId(), Page.getCurrent().getWebBrowser().getAddress());
                        getSession().setAttribute("session", null);
                    } catch (ServerSideException ex) {
                        NotificationsUtil.showError(ex.getMessage());
                    } finally {
                        getNavigator().navigateTo(LoginView.VIEW_NAME);
                    }

                }
            });
            this.navigator.navigateTo(WelcomeView.VIEW_NAME);
        }
    }

    public MenuBar getMainMenu() {
        return mnuMain;
    }
    
    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = IndexUI.class, widgetset = "org.kuwaiba.KuwaibaWidgetSet")
    public static class Servlet extends VaadinCDIServlet implements SessionDestroyListener {
        
        @Override
        protected void servletInitialized() throws ServletException {
            super.servletInitialized();
            getService().addSessionDestroyListener(this);
        }
        
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

        @Override
        public void sessionDestroy(SessionDestroyEvent event) {
//            RemoteSession session = (RemoteSession) getSession().getAttribute("session");
//            if (session != null) { //The Vaadin session is destroyed, but the Kuwaiba session is still open
//                System.out.println("Session is being purged");
//                try {
//                    wsBean.closeSession(session.getSessionId(), Page.getCurrent().getWebBrowser().getAddress());
//                } catch (ServerSideException ex) {
//                    //No matter what happens here
//                }
//                
//                VaadinSession.getCurrent().setAttribute("session", null);
//            }
//            getNavigator().navigateTo(LoginView.VIEW_NAME);
        }
    }
}

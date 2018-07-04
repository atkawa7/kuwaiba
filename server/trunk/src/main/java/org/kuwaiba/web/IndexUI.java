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
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import java.io.IOException;
import java.net.URL;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.web.modules.contacts.ContactManagerModule;
import org.kuwaiba.web.modules.ltmanager.ListTypeManagerModule;
import org.kuwaiba.web.modules.servmanager.ServiceManagerModule;
import org.kuwaiba.web.procmanager.ProcessManagerModule;

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
    /**
     * The reference to the back end bean
     */
    @Inject
    WebserviceBean wsBean;
    /**
     * Main menu
     */
    private MenuBar mnuMain;
        
    @Override
    protected void init(VaadinRequest request) {
        this.setNavigator(new Navigator(this, this));
        this.getNavigator().addProvider(viewProvider);
        this.getNavigator().setErrorView(new ErrorView());
        
        if (getSession().getAttribute("session") == null)
            this.getNavigator().navigateTo(LoginView.VIEW_NAME);
        else {
            this.mnuMain = new MenuBar();
            this.mnuMain.setStyleName("misc-main");
            this.mnuMain.setWidth("100%");
            // Adding Process Manager Module
            ProcessManagerModule processManagerModule = new ProcessManagerModule(null, wsBean, 
                (RemoteSession) getSession().getAttribute("session"));
            processManagerModule.attachToMenu(mnuMain);
            // Adding Service Manager Module
            ServiceManagerModule servManagerModule = new ServiceManagerModule(null, wsBean, 
                        (RemoteSession) getSession().getAttribute("session"));
            servManagerModule.attachToMenu(mnuMain);
            // Adding List Type Manager Module
            ListTypeManagerModule ltmModule = new ListTypeManagerModule(null, wsBean, 
                        (RemoteSession) getSession().getAttribute("session"));
            ltmModule.attachToMenu(mnuMain);
            
            ContactManagerModule cmModule = new ContactManagerModule(null, wsBean, 
                        (RemoteSession) getSession().getAttribute("session"));
            cmModule.attachToMenu(mnuMain);

            this.mnuMain.addItem("Log Out", new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    RemoteSession session = (RemoteSession) getSession().getAttribute("session");
                    try {
                        wsBean.closeSession(session.getSessionId(), Page.getCurrent().getWebBrowser().getAddress());
                        getSession().setAttribute("session", null);
                    } catch (ServerSideException ex) {
                        Notifications.showError(ex.getMessage());
                    } finally {
                        getUI().getNavigator().navigateTo(LoginView.VIEW_NAME);
                    }

                }
            });
            this.getNavigator().navigateTo(WelcomeView.VIEW_NAME);
        }
    }

    public MenuBar getMainMenu() {
        return mnuMain;
    }
    
    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = IndexUI.class, widgetset = "org.kuwaiba.KuwaibaWidgetSet")
    public static class Servlet extends VaadinCDIServlet {
        
//        @Override
//        protected void servletInitialized() throws ServletException {
//            super.servletInitialized();
//            getService().addSessionDestroyListener(new KuwaibaSessionDestroyHandler());
//        }
//        
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
    
    private class KuwaibaSessionDestroyHandler implements SessionDestroyListener {

        @Override
        public void sessionDestroy(SessionDestroyEvent event) {
            RemoteSession session = (RemoteSession) VaadinSession.getCurrent().getSession().getAttribute("session");
            if (session != null) { //The Vaadin session is being destroyed, but the Kuwaiba session is still open
                System.out.println("Session is being purged");
                try {
                    wsBean.closeSession(session.getSessionId(), Page.getCurrent().getWebBrowser().getAddress());
                } catch (ServerSideException ex) {
                    //No matter what happens here
                }

                VaadinSession.getCurrent().setAttribute("session", null);
            }
            getNavigator().navigateTo(LoginView.VIEW_NAME);
        }
    }
}

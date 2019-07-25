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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.h
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kuwaiba.web;

import org.kuwaiba.web.modules.welcome.WelcomeComponent;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.cdi.server.VaadinCDIServlet;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.resources.ResourceFactory;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.kuwaiba.web.menucommand.ProcessDefinitionReloader;
import org.kuwaiba.web.menucommand.RackViewUpdater;
import org.kuwaiba.web.modules.contacts.ContactManagerModule;
import org.kuwaiba.web.modules.ipam.IPAddressManagerModule;
import org.kuwaiba.web.modules.ltmanager.ListTypeManagerModule;
import org.kuwaiba.web.modules.navtree.NavigationTreeModule;
import org.kuwaiba.web.modules.osp.OutsidePlantModule;
import org.kuwaiba.web.modules.servmanager.ServiceManagerModule;
import org.kuwaiba.web.modules.warehouse.WarehouseManagerModule;
import org.kuwaiba.web.procmanager.ProcessManagerModule;

/**
 * Main application entry point. It also serves as the fallback controller
 * @author Charles Edward Bedon Cortazar{@literal <charles.bedon@kuwaiba.org>}
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
        
        if (getSession().getAttribute("session") == null)
            this.getNavigator().navigateTo(LoginView.VIEW_NAME);
        else {
            this.mnuMain = new MenuBar();
            this.mnuMain.setStyleName("misc-main");
            this.mnuMain.setWidth("100%");
            
            this.mnuMain.addItem("", new ThemeResource("img/company_logo_micro.png"), (selectedItem) -> {
                getUI().getNavigator().navigateTo(WelcomeComponent.VIEW_NAME);
            });
            
            // Navigation Tree Module
            NavigationTreeModule navTreeModule = new NavigationTreeModule(wsBean, 
                        (RemoteSession) getSession().getAttribute("session"));
            navTreeModule.attachToMenu(mnuMain);
            
            // Service Manager Module
            ServiceManagerModule servManagerModule = new ServiceManagerModule(wsBean, 
                        (RemoteSession) getSession().getAttribute("session"));
            servManagerModule.attachToMenu(mnuMain);
            
            // List Type Manager Module
            ListTypeManagerModule ltmModule = new ListTypeManagerModule(wsBean, 
                        (RemoteSession) getSession().getAttribute("session"));
            ltmModule.attachToMenu(mnuMain);
            
            // IP Address Manager module
            IPAddressManagerModule ipamModule = new IPAddressManagerModule(wsBean, 
                        (RemoteSession) getSession().getAttribute("session"));
            ipamModule.attachToMenu(mnuMain);

            // Outside Plant
            OutsidePlantModule outsidePlantModule = new OutsidePlantModule(wsBean, 
                        (RemoteSession) getSession().getAttribute("session"));
            outsidePlantModule.attachToMenu(mnuMain);
            
            // Warehouse Manager
            WarehouseManagerModule warehouseManagerModule = new WarehouseManagerModule(wsBean, 
                        (RemoteSession) getSession().getAttribute("session"));
            warehouseManagerModule.attachToMenu(mnuMain);
            
            // Process Manager Module
            ProcessManagerModule processManagerModule = new ProcessManagerModule(wsBean, 
                (RemoteSession) getSession().getAttribute("session"));
            processManagerModule.attachToMenu(mnuMain);
            
            // Contacts Module
            ContactManagerModule cmModule = new ContactManagerModule(wsBean, 
                        (RemoteSession) getSession().getAttribute("session"));
            cmModule.attachToMenu(mnuMain);
            
            MenuBar.MenuItem optionsMenuItem = this.mnuMain.addItem("Options", null);
            optionsMenuItem.addItem("Update Rack Views", 
                new RackViewUpdater(wsBean, (RemoteSession) getSession().getAttribute("session")));
            optionsMenuItem.addItem("Reload Process Definitions", 
                new ProcessDefinitionReloader(wsBean, (RemoteSession) getSession().getAttribute("session")));

            this.mnuMain.addItem("Log Out", new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    RemoteSession session = (RemoteSession) getSession().getAttribute("session");
                    try {
                        wsBean.closeSession(session.getSessionId(), session.getIpAddress());
                        getSession().setAttribute("session", null);
                    } catch (ServerSideException ex) {
                        Notifications.showError(ex.getMessage());
                    } finally {
                        getUI().getNavigator().navigateTo(LoginView.VIEW_NAME);
                    }

                }
            });
            
            this.getSession().addRequestHandler((mySession, myRequest, myResponse) -> {
                if ("/icons".equals(myRequest.getPathInfo())) { //NOI18N
                    RemoteSession session = (RemoteSession) this.getSession().getAttribute("session"); //NOI18N
                    if (session == null) {
                        myResponse.setContentType("text/plain"); //NOI18N
                        myResponse.getWriter().append("You are not authorized to access this resource");
                        return true;
                    }

                    String className = myRequest.getParameter("class");
                    if (className == null) {
                        myResponse.setContentType("text/plain");
                        myResponse.getWriter().append("You have to provide a class name (include a parameter named <b>class</b>)");
                    } else {
                        myResponse.setContentType("image/png"); //NOI18N
                        try {
                            String color = myRequest.getParameter("color");
                            RemoteClassMetadata aClass = wsBean.getClass(className, 
                                    session.getIpAddress(), 
                                    session.getSessionId());
                            
                            if (color != null && color.equals("true"))
                                myResponse.getOutputStream().write(ResourceFactory.createRectangleIcon(new Color(aClass.getColor()), 16, 16));
                            else
                                myResponse.getOutputStream().write(aClass.getIcon().length == 0 ? 
                                       ResourceFactory.createRectangleIcon(Color.BLACK, 16, 16) : aClass.getIcon());
                        } catch (ServerSideException ex) {
                            myResponse.getOutputStream().write(ResourceFactory.createRectangleIcon(Color.BLACK, 16, 16));
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            });
            
            this.getNavigator().navigateTo(WelcomeComponent.VIEW_NAME);
            
        }
    }

    public MenuBar getMainMenu() {
        return mnuMain;
    }
    
    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = true, ui = IndexUI.class, widgetset = "org.kuwaiba.KuwaibaWidgetSet")
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
    

    
//    private class KuwaibaSessionDestroyHandler implements SessionDestroyListener {
//
//        @Override
//        public void sessionDestroy(SessionDestroyEvent event) {
//            RemoteSession session = (RemoteSession) VaadinSession.getCurrent().getSession().getAttribute("session");
//            if (session != null) { //The Vaadin session is being destroyed, but the Kuwaiba session is still open
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
//        }
//    }
}

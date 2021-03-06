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
package org.kuwaiba.web;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.PWA;
import java.util.HashMap;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.modules.AbstractModule;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import static org.kuwaiba.web.KuwaibaConst.META_VIEW_PORT;
import org.kuwaiba.web.modules.navtree.NavigationTreeModule;
import org.kuwaiba.web.modules.osp.OutsidePlantModule;
import org.kuwaiba.web.modules.servmanager.ServiceManagerModule;
import org.kuwaiba.web.procmanager.ProcessManagerModule;

/**
 * The Kuwaiba Web Client Layout
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Viewport(META_VIEW_PORT)
@PWA(name = "Kuwaiba", shortName="Kuwaiba", startPath = "login")
public class MainLayout extends AppLayout implements BeforeEnterObserver {
    @Inject
    private WebserviceBean webserviceBean;
    
    private final Label lblModule = new Label();
    private final Tabs tabs;
    private final HashMap<Class<? extends AbstractTopComponent>, AbstractModule> navigationTargetToTab = new HashMap();
    
    public MainLayout() {
        Button btnAdvancedSearch = new Button("Advanced Search", VaadinIcon.SEARCH.create());
        Button btnNotifications = new Button("Notifications", VaadinIcon.BELL.create());
        Button btnSyncManager = new Button("Sync Manager", VaadinIcon.REFRESH.create());
        
        addToNavbar(new DrawerToggle());
        addToNavbar(lblModule);        
        addToNavbar(btnAdvancedSearch);
        addToNavbar(btnNotifications);
        addToNavbar(btnSyncManager);
        
        setPrimarySection(AppLayout.Section.DRAWER);
        RemoteSession remoteSession = UI.getCurrent().getSession().getAttribute(RemoteSession.class);
        
        tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
                        
        NavigationTreeModule navigationTreeModule = new NavigationTreeModule(webserviceBean, remoteSession);
        navigationTreeModule.attachToMenu(tabs);
        navigationTargetToTab.put(navigationTreeModule.getTopComponentClass(), navigationTreeModule);
                
        ServiceManagerModule serviceManagerModule = new ServiceManagerModule(webserviceBean, remoteSession);
        serviceManagerModule.attachToMenu(tabs);
        navigationTargetToTab.put(serviceManagerModule.getTopComponentClass(), serviceManagerModule);
                
        Tab tabListTypeManager = new Tab("List Type Manager");
        tabs.add(tabListTypeManager);
        
        Tab tabIPAddressManager = new Tab("IP Address Manager");
        tabs.add(tabIPAddressManager);
        
        OutsidePlantModule outsidePlantModule = new OutsidePlantModule(webserviceBean, remoteSession);
        outsidePlantModule.attachToMenu(tabs);
        navigationTargetToTab.put(outsidePlantModule.getTopComponentClass(), outsidePlantModule);
                
        Tab tabWarehouseManager = new Tab("Warehouse Manager");
        tabs.add(tabWarehouseManager);
        
        ProcessManagerModule processManagerModule = new ProcessManagerModule(webserviceBean, remoteSession);
        processManagerModule.attachToMenu(tabs);
        navigationTargetToTab.put(processManagerModule.getTopComponentClass(), processManagerModule);
                
        Tab tabContactManager = new Tab("Contact Manager");
        tabs.add(tabContactManager);
        
        Tab tabOptions = new Tab("Options");
        tabs.add(tabOptions);
        
        RouterLink routerLinkLogOut = new RouterLink(null, LoginView.class);
        routerLinkLogOut.add(VaadinIcon.SIGN_OUT.create());
        routerLinkLogOut.add("Log Out");
        
        Tab tabLogOut = new Tab(routerLinkLogOut);
        tabs.add(tabLogOut);
        
        tabs.addSelectedChangeListener(new ComponentEventListener<Tabs.SelectedChangeEvent>() {
            @Override
            public void onComponentEvent(Tabs.SelectedChangeEvent event) {
                if (event.getSelectedTab().equals(tabLogOut)) {
                    UI.getCurrent().getSession().setAttribute(RemoteSession.class, null);
                }
            }
        });
        
        addToDrawer(tabs);
    }    

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (navigationTargetToTab.containsKey((Class<? extends AbstractTopComponent>) event.getNavigationTarget()))
            lblModule.setText(navigationTargetToTab.get((Class<? extends AbstractTopComponent>) event.getNavigationTarget()).getName());
    }
}

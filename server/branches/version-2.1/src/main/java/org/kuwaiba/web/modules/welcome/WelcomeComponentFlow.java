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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kuwaiba.web.modules.welcome;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import javax.inject.Inject;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.web.authentication.AccessControl;

/**
 * The welcome screen
 * @author Charles Edward Bedon Cortazar{@literal <charles.bedon@kuwaiba.org>}
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@PageTitle("Welcome")
@Route(value = "welcome")
public class WelcomeComponentFlow extends VerticalLayout implements BeforeEnterObserver {
    /**
     * View identifier
     */
    public static String VIEW_NAME = "welcome";
    
    @Inject
    private WebserviceBean wsBean;
    //contains valid user logged inside application
    private static AccessControl accessControl;
    /**
     * This will hold the navigation links
     */
    private MenuBar menuBar;

    /**
     * Default Constructor
     */
    public WelcomeComponentFlow(){
        initResources();
    }
    
    /**
     * 
     * @param event 
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        getUI().ifPresent(ui -> { 
                RemoteSession logedSession = null;
                logedSession = ui.getSession().getAttribute(RemoteSession.class);
                if(logedSession == null){
                    // Session not found
                    ui.navigate("login");
                }
                else{
                    // Session found
                    initResources();
                    add(new Button("Boton de revision"));
                }
             });
    }

    public static AccessControl getAccessControl() {
        return accessControl;
    }

    public static void setAccessControl(AccessControl accessControl) {
        WelcomeComponentFlow.accessControl = accessControl;
    }
    
    public void initResources(){  
        VerticalLayout lytContent = new VerticalLayout();
//        SimpleMapDashboardWidget wdtMap = new SimpleMapDashboardWidget("Geolocated Buildings", wsBean);
//        lytContent.addComponent(wdtMap);
//        lytContent.setSizeFull();
        setMenuBar(new MenuBar());
        getMenuBar().setOpenOnHover(true);
        
        MenuItem navigationTreeMItem = getMenuBar().addItem("Navigation Tree");
        MenuItem servicesMItem = getMenuBar().addItem("Services");
        MenuItem listTypesMItem = getMenuBar().addItem("List Types");
        MenuItem ipAddressManagerMItem = getMenuBar().addItem("IP Address Manager");
        MenuItem outsidePlantMItem = getMenuBar().addItem("Outside Plant");
        MenuItem warehouseManagerMItem = getMenuBar().addItem("Warehouse Manager");
        MenuItem processManagerMItem = getMenuBar().addItem("Process Manager");
        MenuItem contactsMItem = getMenuBar().addItem("Contacts");
        MenuItem optionsMItem = getMenuBar().addItem("Options");
        MenuItem logoutMItem = getMenuBar().addItem("Log Out", event ->{
            getUI().ifPresent(ui -> { 
                System.out.println("Entro al evento del click Logout");
                ui.getSession().setAttribute(RemoteSession.class, null);
                ui.navigate("Login");
             });
        });
        
        SubMenu servicesSubMenu = servicesMItem.getSubMenu();
        MenuItem serviceManagerSMItem = servicesSubMenu.addItem("Service Manager");
        MenuItem addCustomerSMItem = servicesSubMenu.addItem("Add Customer");
        MenuItem addServiceSMItem = servicesSubMenu.addItem("Add Service");
        
        SubMenu listTypesSubMenu = listTypesMItem.getSubMenu();
        MenuItem listTypeManagerSMItem = listTypesSubMenu.addItem("List Type Manager");
        MenuItem addListTypeItemSMItem = listTypesSubMenu.addItem("Add List Type Item");
        
        SubMenu processManagerSubMenu = processManagerMItem.getSubMenu();
        MenuItem startNewProcessSMItem = processManagerSubMenu.addItem("Start New Process");
        MenuItem exploreProcessesMItem = processManagerSubMenu.addItem("Explore Processes");
        
        SubMenu contactsSubMenu = contactsMItem.getSubMenu();
        MenuItem addContactSMItem = contactsSubMenu.addItem("Add Contact");
        
        SubMenu optionsSubMenu = optionsMItem.getSubMenu();
        MenuItem reloadProcessDefinitionsSMItem = optionsSubMenu.addItem("Reload Process Definitions");
        
        add(getMenuBar());
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public void setMenuBar(MenuBar menuBar) {
        this.menuBar = menuBar;
    }
    
    
}

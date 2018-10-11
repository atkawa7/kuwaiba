/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.web.modules.warehouse;

import com.google.common.eventbus.EventBus;
import com.vaadin.navigator.View;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import org.kuwaiba.apis.web.gui.modules.AbstractModule;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class WarehouseManagerModule extends AbstractModule {
        
    private WarehouseManagerComponent warehouseManagerComponent;
    
    public WarehouseManagerModule(EventBus eventBus, WebserviceBean wsBean, RemoteSession session) {
        super(eventBus, wsBean, session);
    }

    @Override
    public String getName() {
        return "Warehouse Manager";
    }

    @Override
    public String getDescription() {
        return "Manage customer, warehouses and resources associated to those warehouses";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>";
    }

    @Override
    public int getType() {
        return MODULE_TYPE_FREE_CORE;
    }

    @Override
    public void attachToMenu(MenuBar menuBar) {
        MenuBar.MenuItem warehouseModuleMenuItem = menuBar.addItem(getName(), new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                UI.getCurrent().getNavigator().navigateTo(WarehouseManagerComponent.VIEW_NAME);
            }
        });
        warehouseModuleMenuItem.setDescription(getDescription());
    }

    @Override
    public View open() {
        warehouseManagerComponent = new WarehouseManagerComponent();
        //Register components in the event bus
        warehouseManagerComponent.registerComponents();
        return warehouseManagerComponent;
    }

    @Override
    public void close() {
        warehouseManagerComponent.unregisterComponents();
    }
    
}

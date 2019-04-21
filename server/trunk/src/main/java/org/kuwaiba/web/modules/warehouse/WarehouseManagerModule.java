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
package org.kuwaiba.web.modules.warehouse;

import com.vaadin.navigator.View;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import org.kuwaiba.apis.web.gui.modules.AbstractModule;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * The definition of the Warehouse Manager Module. This module will allow managing 
 * the devices and parts stored in warehouses and it is intended to work with the 
 * Process Manager to handle work flows associated to purchases, decommissions and 
 * repairs.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WarehouseManagerModule extends AbstractModule {
        
    private WarehouseManagerComponent warehouseManagerComponent;
    
    public WarehouseManagerModule(WebserviceBean wsBean, RemoteSession session) {
        super(wsBean, session);
    }

    @Override
    public String getName() {
        return "Warehouse Manager";
    }

    @Override
    public String getDescription() {
        return "This module will allow managing the devices and parts stored in warehouses.";
    }

    @Override
    public String getVersion() {
        return "1.1";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>";
    }

    @Override
    public int getType() {
        return MODULE_TYPE_COMMERCIAL;
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

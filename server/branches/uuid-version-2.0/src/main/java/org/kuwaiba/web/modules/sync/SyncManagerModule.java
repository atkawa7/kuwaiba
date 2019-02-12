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
package org.kuwaiba.web.modules.sync;

import com.vaadin.navigator.View;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import org.kuwaiba.apis.web.gui.modules.AbstractModule;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * Manage the way the inventory will keep up-to-date by syncing against external sources, such as NMS, legacy systems and devices
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SyncManagerModule extends AbstractModule {
    
    private SyncManagerComponent syncManagerComponent;

    public SyncManagerModule(WebserviceBean wsBean, RemoteSession session) {
        super(wsBean, session);
    }

    @Override
    public String getName() {
        return "Synchronization Manager";
    }

    @Override
    public String getDescription() {
        return "Manage the way the inventory will keep up-to-date by syncing against external sources, such as NMS, legacy systems and devices";
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
        return MODULE_TYPE_COMMERCIAL;
    }

    @Override
    public void attachToMenu(MenuBar menuBar) {
        MenuBar.MenuItem serviceManagerMenuItem = menuBar.addItem("Synchronization", null);
        serviceManagerMenuItem.setDescription(getDescription());
        
        serviceManagerMenuItem.addItem(getName(), new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                UI.getCurrent().getNavigator().navigateTo(SyncManagerComponent.VIEW_NAME);
            }
        });
    }

    @Override
    public View open() {
        syncManagerComponent = new SyncManagerComponent();
        //Register components in the event bus
        syncManagerComponent.registerComponents();
        return syncManagerComponent;
    }

    @Override
    public void close() {
        syncManagerComponent.unregisterComponents();
    }
    
}

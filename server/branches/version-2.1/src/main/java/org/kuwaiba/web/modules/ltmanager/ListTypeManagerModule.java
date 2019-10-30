/**
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
package org.kuwaiba.web.modules.ltmanager;

import org.kuwaiba.web.modules.ltmanager.actions.AddListTypeItemWindow;
import com.vaadin.navigator.View;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import org.kuwaiba.apis.web.gui.events.OperationResultListener;
import org.kuwaiba.apis.web.gui.modules.AbstractModule;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 * The definition of the List Types Manager module
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ListTypeManagerModule  extends AbstractModule {
    /**
     * The actual component
     */
    private ListTypeManagerComponent listTypeManagerComponent;
    
    public ListTypeManagerModule(WebserviceBean wsBean, RemoteSession session) {
        super(wsBean, session);
        icon = new ThemeResource("img/mod_icon_list.png");
    }

    @Override
    public String getName() {
        return "List Type Manager";
    }

    @Override
    public String getDescription() {
        return "This module allows to manage the list type items for the available list types previously created using the Data Model Manager";
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
        return MODULE_TYPE_FREE_CORE;
    }

    @Override
    public void attachToMenu(MenuBar menuBar) {
        MenuBar.MenuItem listTypeManagerMenuItem = menuBar.addItem("List Types", null);
        listTypeManagerMenuItem.setDescription(getDescription());
        
        listTypeManagerMenuItem.addItem(getName(), new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                UI.getCurrent().getNavigator().navigateTo(ListTypeManagerComponent.VIEW_NAME);
            }
        });
        
        //Apart from the full-fledged List Type Manager, we add a small action to create a type
        listTypeManagerMenuItem.addItem("Add List Type Item", new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                AddListTypeItemWindow wdwNewListTypeItem = new AddListTypeItemWindow(wsBean, new OperationResultListener() {
                    @Override
                    public void doIt() {
                        Notifications.showInfo("List Type added successfully");
                    }
                });
                UI.getCurrent().addWindow(wdwNewListTypeItem);
            }
        });
    }

    @Override
    public View open() {
        listTypeManagerComponent = new ListTypeManagerComponent();
        //Register components in the event bus
        listTypeManagerComponent.registerComponents();
        return listTypeManagerComponent;
    }

    @Override
    public void close() {
        //Unregister components from the event bus
        listTypeManagerComponent.unregisterComponents();
    }
}

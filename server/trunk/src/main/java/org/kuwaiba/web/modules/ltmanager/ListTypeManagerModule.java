/**
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
package org.kuwaiba.web.modules.ltmanager;

import com.google.common.eventbus.EventBus;
import com.vaadin.navigator.View;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import org.kuwaiba.apis.web.gui.modules.AbstractModule;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 * The definition of the List Types Manager module
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ListTypeManagerModule  extends AbstractModule {
    /**
     * The actual component
     */
    private ListTypeManagerComponent listTypeManagerComponent;
    private WebserviceBean wsBean;
    private RemoteSession session;
    
    public ListTypeManagerModule(EventBus eventBus, WebserviceBean wsBean, RemoteSession session) {
        super(eventBus);
        this.wsBean = wsBean;
        this.session = session;
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
        return "Neotropic SAS";
    }

    @Override
    public int getType() {
        return MODULE_TYPE_FREE_CORE;
    }

    @Override
    public void attachToMenu(MenuBar menuBar) {
        MenuBar.MenuItem listTypeManagerMenuItem = menuBar.addItem(getName(), new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                UI.getCurrent().getNavigator().addView(ListTypeManagerComponent.VIEW_NAME, open());
                UI.getCurrent().getNavigator().navigateTo(ListTypeManagerComponent.VIEW_NAME);
            }
        });
        listTypeManagerMenuItem.setDescription(getDescription());
    }

    @Override
    public View open() {
        listTypeManagerComponent = new ListTypeManagerComponent(eventBus, wsBean, session);
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

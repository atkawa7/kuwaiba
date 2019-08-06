/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.lists;

import com.google.common.eventbus.EventBus;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import org.kuwaiba.apis.web.gui.modules.AbstractModule;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * The definition of the List Types Manager module
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ListManagerModule  extends AbstractModule {
    /**
     * The actual component
     */
    private ListManagerComponent listManager;
    private WebserviceBeanLocal wsBean;
    private RemoteSession session;
    
    public ListManagerModule(EventBus eventBus, WebserviceBeanLocal wsBean, RemoteSession session) {
        super(eventBus);
        this.wsBean = wsBean;
        this.session = session;
        icon = new ThemeResource("img/mod_icon_list.png");
    }

    @Override
    public String getName() {
        return "List Manager";
    }

    @Override
    public String getDescription() {
        return "List Types Manager.";
    }

    @Override
    public String getVersion() {
        return "1.0";
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
    public String getLocation() {
        return "Tools/Navigation";
    }

    @Override
    public int getMode() {
        return AbstractModule.COMPONENT_MODE_EXPLORER;
    }

    @Override
    public Component open() {
        if (instanceCount == 0) {
            listManager = new ListManagerComponent(eventBus, wsBean, session);
            instanceCount ++;
        }
        //Register components in the event bus
        listManager.registerComponents();
        return listManager;
    }

    @Override
    public void close() {
        //Unregister components from the event bus
        listManager.unregisterComponents();
    }
}

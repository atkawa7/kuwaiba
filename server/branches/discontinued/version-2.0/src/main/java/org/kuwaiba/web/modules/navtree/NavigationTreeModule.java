/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.web.modules.navtree;

import com.google.common.eventbus.EventBus;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Component;
import org.kuwaiba.apis.web.gui.modules.AbstractModule;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * The definition of the Navigation Tree module.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class NavigationTreeModule extends AbstractModule {
    /**
     * The actual component
     */
    private NavigationTreeComponent treeNavTree;
    private final WebserviceBeanLocal wsBean;
    private final RemoteSession session;
    
    public NavigationTreeModule(EventBus eventBus, WebserviceBeanLocal wsBean, RemoteSession session) {
        super(eventBus);
        this.wsBean = wsBean;
        this.session = session;
        icon = new ThemeResource("img/mod_icon_navtree.png");
    }
    
    @Override
    public String getName() {
        return "Navigation Tree";
    }

    @Override
    public String getDescription() {
        return "Navigate through your physical assets in a hierarchical fashion.";
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
            treeNavTree = new NavigationTreeComponent(eventBus, wsBean, session);
            instanceCount ++;
        }
        //Register components in the event bus
        treeNavTree.registerComponents();
        return treeNavTree;
    }

    @Override
    public void close() {
        //Unregister components from the event bus
        treeNavTree.unregisterComponents();
    }
}

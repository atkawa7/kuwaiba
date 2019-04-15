/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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

import com.vaadin.navigator.View;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import org.kuwaiba.apis.web.gui.modules.AbstractModule;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 * This is the next generation equivalent of the old navigation tree, which provides a quick way to 
 * navigate through the containment hierarchy
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class NavigationTreeModule extends AbstractModule {
    /**
     * The actual component
     */
    private NavigationTreeComponent treeNavTree;
    
    public NavigationTreeModule(WebserviceBean wsBean, RemoteSession session) {
        super(wsBean, session);
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
        return "2.0";
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
        MenuBar.MenuItem navTreeMenuItem = menuBar.addItem(getName(), new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                UI.getCurrent().getNavigator().navigateTo(NavigationTreeComponent.VIEW_NAME);
            }
        });
        navTreeMenuItem.setDescription(getDescription());
    }

    @Override
    public View open() {
        treeNavTree = new NavigationTreeComponent();
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

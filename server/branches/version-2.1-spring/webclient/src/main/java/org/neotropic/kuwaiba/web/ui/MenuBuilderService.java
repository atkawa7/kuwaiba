/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.web.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import java.util.HashMap;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This singleton builds navigation menus based on user privileges and manage their changes 
 * and instances.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class MenuBuilderService {
    /**
     * The list of cached menus. The key of the hash map is the user name.
     */
    private HashMap<String, MenuBar> menuList;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;

    public MenuBuilderService() {
        this.menuList = new HashMap<>();
    }
    
    /**
     * Deletes from the cache (if existing) the menu associated to a given session 
     * or user.
     * @param session The session to unregister.
     */
    public void unregisterMenu(Session session) {
        this.menuList.remove(session.getUser().getUserName());
    }
    
    /**
     * Builds or retrieves from the cache 
     * @param session
     * @return 
     */
    public MenuBar buildMenuForSession(Session session) {
        if (this.menuList.containsKey(session.getUser().getUserName()))
            return this.menuList.get(session.getUser().getUserName());
        
        MenuBar mnuNewBar = new MenuBar();
        mnuNewBar.addThemeVariants(MenuBarVariant.MATERIAL_OUTLINED);
        mnuNewBar.addItem(ts.getTranslatedString("module.login.ui.home"), ev -> UI.getCurrent().navigate(HomeUI.class));
        mnuNewBar.addItem(ts.getTranslatedString("module.serviceman.name"), ev -> UI.getCurrent().navigate(ServiceManagerUI.class));
        mnuNewBar.addItem(ts.getTranslatedString("module.login.ui.logout"), ev -> UI.getCurrent().navigate(LogoutUI.class));

        this.menuList.put(session.getUser().getUserName(), mnuNewBar);
        return mnuNewBar;
    }
}

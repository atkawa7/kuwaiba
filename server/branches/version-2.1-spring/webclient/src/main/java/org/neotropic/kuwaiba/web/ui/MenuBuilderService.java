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

import com.vaadin.flow.component.menubar.MenuBar;
import java.util.HashMap;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.springframework.stereotype.Service;

/**
 * This singleton builds navigation menus based on user privileges and manage their changes 
 * and instances.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class MenuBuilderService {
    /**
     * The list of 
     */
    private HashMap<String, MenuBar> menuList;

    public MenuBuilderService() {
        menuList = new HashMap<>();
    }
    
    public void registerMenu(Session aSession) {
        
    }
    
    public void unregisterMenu(Session aSession) {
    }
}

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

package org.kuwaiba.web.modules.warehouse.dashboard;

import com.vaadin.event.ShortcutAction;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import eu.maxschuster.vaadin.autocompletetextfield.AutocompleteTextField;
import java.util.List;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.navigation.ObjectsAndClassesSuggestionProvider;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * The main Warehouse Manager dashboard component. Allows to search for spared and 
 * reserved elements in warehouses and physical locations.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class WarehouseManagerSearchWidget extends AbstractDashboardWidget {
    /**
     * Reference to the backend bean.
     */
    private WebserviceBean wsBean;
    public WarehouseManagerSearchWidget(String title, WebserviceBean wsBean) {
        super(title);
        setSizeFull();
        this.createContent();
    }

    @Override
    public void createContent() {
        RemoteSession session = ((RemoteSession) getSession().getAttribute("session"));
        VerticalLayout lytContent = new VerticalLayout();
        HorizontalLayout lytSearch = new HorizontalLayout();
        
        AutocompleteTextField txtSearch = new AutocompleteTextField("Search");
        txtSearch.setPlaceholder("Type a device name, physical location or warehouse name...");
        txtSearch.setMinChars(3);
        txtSearch.setDelay(500);
        txtSearch.setSuggestionProvider(new ObjectsAndClassesSuggestionProvider(wsBean, title));
//        txtSearch.addSelectListener((e) -> {
//            RemoteObjectLight selectedObject = e.getSuggestion().getData()));
//        });
        
        Button btnSearch = new Button(VaadinIcons.SEARCH, (e) -> {
            
            if (txtSearch.getValue().length() < 3) {
                Notifications.showInfo("Please refine your search");
                return;
            }
            try {
                List<RemoteObjectLight> suggestedObjects = wsBean.getSuggestedObjectsWithFilter(txtSearch.getValue(),
                        -1, Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                
                if (suggestedObjects.isEmpty())
                    Notifications.showInfo("Your search has 0 results");
                else {
                }
                
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getLocalizedMessage());
            }
        });
        btnSearch.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        
        lytSearch.addComponents(txtSearch, btnSearch);
        lytContent.setSizeFull();
        
    }
}

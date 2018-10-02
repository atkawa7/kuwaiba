/*
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

package org.kuwaiba.web.modules.osp.dashboard;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;

/**
 * A widget that displays a map and allows to drop elements from a navigation tree and create physical connections
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class OutsidePlantViewDashboardWidget extends AbstractDashboardWidget {
    /**
     * Default map widget
     */
    private GoogleMap mapMain;
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;
    public OutsidePlantViewDashboardWidget(WebserviceBean wsBean) {
        super("Outside Plant Viewer");
        createContent();
        setSizeFull();
    }

    
    
    @Override
    public void createCover() {
        throw new UnsupportedOperationException("This widget supports only embedded mode"); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void createContent() {
        try {
            
            Context context = new InitialContext();
            String apiKey = (String)context.lookup("java:comp/env/googleMapsApiKey");
            String language = (String)context.lookup("java:comp/env/mapLanguage");

            mapMain = new GoogleMap(apiKey, null, language);
            mapMain.setSizeFull();
            
            MenuBar mnuMain = new MenuBar();
        
            mnuMain.addItem("New", VaadinIcons.FOLDER_ADD, (selectedItem) -> {

            });

            mnuMain.addItem("Open", VaadinIcons.FOLDER_OPEN, (selectedItem) -> {

            });

            MenuBar.MenuItem mnuConnect = mnuMain.addItem("Connect");
            mnuConnect.setIcon(VaadinIcons.CONNECT);

            mnuConnect.addItem("Using a Container", (selectedItem) -> {
            });

            mnuConnect.addItem("Using a Link", (selectedItem) -> {
            });
            
            VerticalLayout lytContent = new VerticalLayout(mnuMain, mapMain);
            lytContent.setExpandRatio(mnuMain, 0.3f);
            lytContent.setExpandRatio(mapMain, 9.7f);
            lytContent.setSizeFull();
            contentComponent = lytContent;
            addComponent(contentComponent);
            
        } catch (NamingException ne) {
            Notifications.showError("An error ocurred while reading the map provider configuration. Contact your administrator");
        }
    }

}

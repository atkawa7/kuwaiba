/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.web.modules.warehouse;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.MenuBar;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.web.IndexUI;
import org.kuwaiba.web.modules.warehouse.dashboard.SpareAndReservedDashboardWidget;

/**
 * Main content of the Warehouse Manager Module.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@CDIView("whmanager")
public class WarehouseManagerComponent extends AbstractTopComponent {
    public static String VIEW_NAME = "whmanager";
    /**
     * The backend bean
     */
    @Inject
    private WebserviceBean webserviceBean;
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setStyleName("dashboards");
        MenuBar mnuMain = ((IndexUI)getUI()).getMainMenu();
        SpareAndReservedDashboardWidget spareInventoryObjectsDashboardWidget = new SpareAndReservedDashboardWidget(webserviceBean);
                        
        addComponent(mnuMain);
        addComponent(spareInventoryObjectsDashboardWidget);
        setSpacing(false);
        setMargin(false);
        setExpandRatio(mnuMain, 0.3f);
        setExpandRatio(spareInventoryObjectsDashboardWidget, 9.7f);
    }

    @Override
    public void registerComponents() {
    }

    @Override
    public void unregisterComponents() {
    }
    
}

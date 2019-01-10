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
package org.kuwaiba.apis.web.gui.dashboards;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;

/**
 * Most Kuwaiba modules use master-detail views present information to the user. The master part is usually implemented 
 * using trees or lists, and when one of their items is selected, the detail shows more information about such item. The aim 
 * of AbstractDashboard is to provide a standard mechanism to implement a detail view in a dashboard fashion, that is, a grid 
 * with widgets displaying detailed information (charts, lists, etc)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class AbstractDashboard extends Panel {
    /**
     * The layout used by the panel. See some sample layouts in 
     * the package org.kuwaiba.apis.web.gui.dashboards.layouts
     */
    private AbstractOrderedLayout dashboardLayout;
    
    /**
     * Default constructor
     * @param title The title of the dashboard
     * @param dashboardLayout The layout to be used. See some sample layouts in 
     * the package org.kuwaiba.apis.web.gui.dashboards.layouts
     */
    public AbstractDashboard(String title, AbstractOrderedLayout dashboardLayout) {
        this.dashboardLayout = dashboardLayout;
        //this.addComponent(dashboardLayout);
        setContent(dashboardLayout);
        this.setSizeFull();
    }

    /**
     * Alternate constructor
     * @param title The title of the dashboard
     * @param dashboardLayout The layout to be used. See some sample layouts in 
     * the package org.kuwaiba.apis.web.gui.dashboards.layouts
     * @param actions The actions to be placed in menu bar on top of the dashboard
     */
    public AbstractDashboard(String title, AbstractOrderedLayout dashboardLayout, AbstractAction[] actions) {
        this.dashboardLayout = dashboardLayout;
        
        MenuBar mnuDashboard = new MenuBar();
        
        for (AbstractAction action : actions)
            mnuDashboard.addItem(action.getCaption(), new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    action.actionPerformed();
                }
            });
        
        this.setContent(new VerticalLayout(mnuDashboard, dashboardLayout));
        this.setSizeFull();
    }

    public AbstractOrderedLayout getDashboardLayout() {
        return dashboardLayout;
    }
}

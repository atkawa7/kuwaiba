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

package org.kuwaiba.web.modules.navtree.dashboard;

import com.vaadin.server.Page;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboard;
import org.kuwaiba.apis.web.gui.dashboards.layouts.ShelfDashboardLayout;
import org.kuwaiba.apis.web.gui.dashboards.widgets.AttachedFilesDashboardWidget;
import org.kuwaiba.apis.web.gui.dashboards.widgets.ReportsDashboardWidget;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * The dashboard for the Navigation Tree module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class NavigationTreeDashboard extends AbstractDashboard {
    public NavigationTreeDashboard(RemoteObjectLight selectedObject, WebserviceBean wsBean) {
        super(selectedObject.toString(), new ShelfDashboardLayout(selectedObject.toString(), String.format("Object id: %s", selectedObject.getId())));
        ((ShelfDashboardLayout)getDashboardLayout()).setMainDashboardWidget(new NavigationTreeExplorerDashboardWidget(selectedObject, wsBean));
        ((ShelfDashboardLayout)getDashboardLayout()).addToPile(new RelationshipsDashboardWidget(selectedObject, wsBean));
        ((ShelfDashboardLayout)getDashboardLayout()).addToPile(new SpecialChildrenDashboardWidget(selectedObject, wsBean));
        ((ShelfDashboardLayout)getDashboardLayout()).addToPile(new ObjectViewDashboardWidget(this, selectedObject, wsBean));
        ((ShelfDashboardLayout)getDashboardLayout()).addToPile(new ReportsDashboardWidget(selectedObject, wsBean));
        ((ShelfDashboardLayout)getDashboardLayout()).addToPile(new AttachedFilesDashboardWidget(selectedObject, wsBean));
        
        Page.getCurrent().setTitle(String.format("Kuwaiba Open Network Inventory - Exploring %s", selectedObject));
    }

}

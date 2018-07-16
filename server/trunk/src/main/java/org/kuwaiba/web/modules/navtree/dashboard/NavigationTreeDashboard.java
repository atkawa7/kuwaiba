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

import com.vaadin.ui.AbstractOrderedLayout;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboard;

/**
 * The dashboard for the Navigation Tree module
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class NavigationTreeDashboard extends AbstractDashboard {

    public NavigationTreeDashboard(String title, AbstractOrderedLayout dashboardLayout) {
        super(title, dashboardLayout);
    }

}

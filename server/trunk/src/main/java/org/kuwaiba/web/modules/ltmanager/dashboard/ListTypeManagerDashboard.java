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
package org.kuwaiba.web.modules.ltmanager.dashboard;

import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboard;
import org.kuwaiba.apis.web.gui.dashboards.layouts.SingleWidgetDashboardLayout;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;

/**
 * The dashboard used in the List Type Manager
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ListTypeManagerDashboard extends AbstractDashboard {
    
    public ListTypeManagerDashboard(RemoteClassMetadataLight aListType, WebserviceBean wsBean) {
        super(aListType.getClassName(), new SingleWidgetDashboardLayout());
        ((SingleWidgetDashboardLayout)getDashboardLayout()).addComponent(new ListTypeItemManagerDashboardWidget(aListType, wsBean));
    }
    
}

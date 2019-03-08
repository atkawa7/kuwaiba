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

package org.kuwaiba.apis.web.gui.dashboards.widgets;

import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.dashboards.DashboardEventListener;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.properties.PropertyFactory;
import org.kuwaiba.apis.web.gui.properties.PropertySheet;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A simple dashboard widget that contains a property sheet that listen for selection events
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class PropertySheetDashboardWidget extends AbstractDashboardWidget implements DashboardEventListener {
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;

    public PropertySheetDashboardWidget(WebserviceBean wsBean) {
        super("Property Sheet");
        this.wsBean = wsBean;
        this.createContent();
        this.setSizeFull();
    }
    @Override
    public void createCover() {
        throw new UnsupportedOperationException("This widget supports only embedded mode");
    }
    
    @Override
    public void createContent() {
        contentComponent = new PropertySheet();
        addComponent(contentComponent);
    }

    @Override
    public void eventReceived(DashboardEvent event) {
        if (event.getType() == DashboardEvent.TYPE_SELECTION) {
            try {
                ((PropertySheet)contentComponent).setItems(PropertyFactory.propertiesFromRemoteObject((RemoteObjectLight)event.getPayload(), wsBean));
            } catch(ServerSideException ex) {
                Notifications.showError(ex.getLocalizedMessage());
            }
        }
    }
}

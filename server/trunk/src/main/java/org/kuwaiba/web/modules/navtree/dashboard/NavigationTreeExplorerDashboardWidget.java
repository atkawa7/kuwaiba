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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.nodes.PropertyFactory;
import org.kuwaiba.apis.web.gui.nodes.PropertySheet;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;

/**
 * The dashboard widget to be used as the main widget in the Navigation Tree dashboard
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class NavigationTreeExplorerDashboardWidget extends AbstractDashboardWidget {
    /**
     * The property sheet that allows to edit a properties of the selected item in the nav tree
     */
    private PropertySheet propertySheet;
    /**
     * Reference to the ws bean
     */
    private WebserviceBean wsBean;
    /**
     * Reference to the business object to be explored
     */
    private RemoteObjectLight selectedObject;
    
    public NavigationTreeExplorerDashboardWidget(RemoteObjectLight selectedObject, WebserviceBean wsBean) {
        super(String.format("Properties for %s", selectedObject));
        this.wsBean = wsBean;
        this.selectedObject = selectedObject;
        this.createContent();
        this.setSizeFull();
    }

    @Override
    public void createCover() { }

    @Override
    public void createContent() {
        HorizontalLayout lytContent = new HorizontalLayout();
        lytContent.setMargin(true);
        try {
            RemoteObject seletedItemDetails = wsBean.getObject(selectedObject.getClassName(), selectedObject.getId(), Page.getCurrent().getWebBrowser().getAddress(), 
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

            RemoteClassMetadata listTypeClassMetadata = wsBean.getClass(selectedObject.getClassName(),Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

            this.propertySheet = new PropertySheet(PropertyFactory.propertiesFromRemoteObject(seletedItemDetails, listTypeClassMetadata), title);
            lytContent.addComponent(this.propertySheet);
            this.contentComponent = lytContent;
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getLocalizedMessage());
        }
        this.contentComponent = lytContent;
        addComponent(contentComponent);
    }

}

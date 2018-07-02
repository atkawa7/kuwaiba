/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.web.modules.ltmanager.dashboard;

import com.vaadin.server.Page;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;

/**
 * A dashboard widget that allows to manage the list type items associated to a given list type
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class ListTypeItemManagerDashboardWidget extends AbstractDashboardWidget {
    /**
     * The list type associated to this widget
     */
    private RemoteClassMetadataLight listType;
    /**
     * Reference to the ws bean
     */
    private WebserviceBean wsBean;
    
    public ListTypeItemManagerDashboardWidget(RemoteClassMetadataLight listType, WebserviceBean wsBean) {
        super(String.format("List Type Items for %s", listType.getClassName()));
        this.wsBean = wsBean;
        this.listType = listType;
        this.createContent();
        this.setSizeFull();
    }
    
    @Override
    public void createCover() { }  //Not used

    @Override
    public void createContent() { 
        VerticalLayout lytContent = new VerticalLayout();
        Grid<RemoteObjectLight> lstListTypeItems = new Grid<>();
        try {
            List<RemoteObjectLight> listTypeItems = wsBean.getListTypeItems(listType.getClassName(), Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            if (listTypeItems.isEmpty())
                lytContent.addComponent(new Label("There are no list type items associated to this list type"));
            else {
                lstListTypeItems.setItems(listTypeItems);
                lstListTypeItems.addColumn(RemoteObjectLight::getName).setCaption("List Type Items");
                lytContent.addComponent(lstListTypeItems);
            }
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        this.contentComponent = lytContent;
        addComponent(contentComponent);
    }
    
}

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
package org.kuwaiba.web.modules.services.dashboard;

import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteContact;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A simple dashboard widget that shows the contacts associated to a service
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class ContactsDashboardWidget extends AbstractDashboardWidget {
    /**
     * The customer we want the Contacts from
     */
    private RemoteObjectLight customer;
    /**
     * Web service bean reference
     */
    private WebserviceBeanLocal wsBean;
    
    public ContactsDashboardWidget(RemoteObjectLight customer, WebserviceBeanLocal wsBean) {
        super("Contacts");
        this.customer = customer;
        this.wsBean = wsBean;
        this.createCover();
        this.createContent();
    }
    
    @Override
    public void createCover() {
        Panel pnlContactsWidgetCover = new Panel("Contacts");
        pnlContactsWidgetCover.setContent(new VerticalLayout(new Label("Y contacts found"), new Button("See More...", (event) -> {
            flip();
        })));
        pnlContactsWidgetCover.setSizeFull();        
        this.coverComponent = pnlContactsWidgetCover;
        this.coverComponent.setStyleName("k-dashboard_widget-purple");
        addComponent(coverComponent);
    }

    @Override
    public void createContent() {
        Grid<RemoteContact> lstContacts = new Grid<>();
        lstContacts.setHeaderVisible(false);
        lstContacts.setSizeFull();
        this.contentComponent = lstContacts;
        
        try {
            
            List<RemoteContact> customerContacts = wsBean.getContactsForCustomer(customer.getClassName(), customer.getId(), Page.getCurrent().getWebBrowser().getAddress(), 
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            lstContacts.addColumn(RemoteContact::getName);
            lstContacts.addColumn((source) -> {
                for (StringPair attribute : source.getAttributes()) {
                    if (attribute.getKey().equals("email")) //NOI18N
                        return attribute.getValue();
                }
                return "NA"; //NOI18N
            });
            lstContacts.setItems(customerContacts);
            
        } catch (ServerSideException ex) {
            NotificationsUtil.showError(ex.getMessage());
        }        
    }
}

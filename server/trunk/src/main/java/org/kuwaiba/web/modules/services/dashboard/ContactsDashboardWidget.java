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
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
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
        VerticalLayout lytContactsWidgetCover = new VerticalLayout();
        Label lblText = new Label(title);
        lblText.setStyleName("text-bottomright");
        lytContactsWidgetCover.addLayoutClickListener((event) -> {
            if (event.getButton() == MouseEventDetails.MouseButton.LEFT)
                launch();
        });
        
        lytContactsWidgetCover.addComponent(lblText);
        lytContactsWidgetCover.setSizeFull();
        lytContactsWidgetCover.setStyleName("dashboard_cover_widget-darkblue");
        this.coverComponent = lytContactsWidgetCover;
        addComponent(coverComponent);
    }

    @Override
    public void createContent() {        
        try {
            List<RemoteContact> customerContacts = wsBean.getContactsForCustomer(customer.getClassName(), customer.getId(), Page.getCurrent().getWebBrowser().getAddress(), 
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            if (customerContacts.isEmpty()) {
                this.contentComponent = new Label("The customer associated to this service does not have registered contacts");
            }
            else {
                Grid<RemoteContact> lstContacts = new Grid<>();
                lstContacts.setSizeFull();
                lstContacts.addColumn(RemoteContact::getName).setCaption("Name");
                
                lstContacts.addColumn((source) -> {
                    for (StringPair attribute : source.getAttributes()) {
                        if (attribute.getKey().equals("email")) //NOI18N
                            return attribute.getValue();
                    }
                    return "NA"; //NOI18N
                }).setCaption("e-mail");
                
                lstContacts.addColumn((source) -> {
                    for (StringPair attribute : source.getAttributes()) {
                        if (attribute.getKey().equals("telephone1")) //NOI18N
                            return attribute.getValue();
                    }
                    return "NA"; //NOI18N
                }).setCaption("Telephone 1");
                
                lstContacts.setItems(customerContacts);
                
                Panel pnlContacts = new Panel(lstContacts);
                pnlContacts.setSizeFull();
                this.contentComponent = pnlContacts;
            }
        } catch (ServerSideException ex) {
            this.contentComponent = new Label(ex.getMessage());
        }        
    }
}

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
package org.kuwaiba.web.modules.servmanager.dashboard;

import com.vaadin.server.Page;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteContact;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;

/**
 * A simple dashboard widget that shows the contacts associated to a service
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ContactsDashboardWidget extends AbstractDashboardWidget {
    /**
     * The customer we want the Contacts from
     */
    private RemoteObjectLight customer;
    /**
     * Web service bean reference
     */
    private WebserviceBean wsBean;
    
    public ContactsDashboardWidget(RemoteObjectLight customer, WebserviceBean wsBean) {
        super("Contacts");
        this.customer = customer;
        this.wsBean = wsBean;
        this.createCover();
        
    }
    
    @Override
    public void createCover() {
        VerticalLayout lytContactsWidgetCover = new VerticalLayout();
        Label lblText = new Label(title);
        lblText.setStyleName("text-bottomright");
        lytContactsWidgetCover.addLayoutClickListener((event) -> {
            if (event.getButton() == MouseEventDetails.MouseButton.LEFT) {
                this.createContent();
                launch();
            }
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
            
            if (customerContacts.isEmpty()) 
                this.contentComponent = new Label("The customer associated to this service does not have registered contacts");
            
            else {
                
                HashMap<String, List<RemoteContact>> contactsPerClass = new HashMap<>();
                VerticalLayout lytContacts = new VerticalLayout();
                
                for(RemoteContact contact : customerContacts) {
                    if (!contactsPerClass.containsKey(contact.getClassName()))
                        contactsPerClass.put(contact.getClassName(), new ArrayList<>());

                    contactsPerClass.get(contact.getClassName()).add(contact);
                }
                
                for (String contactType : contactsPerClass.keySet()) {
                    Grid<RemoteContact> tblContactsPerType = new Grid<>(contactType);
                    tblContactsPerType.setItems(contactsPerClass.get(contactType));
                    
                    tblContactsPerType.addColumn(RemoteContact::getName).setCaption("Name");
                    
                    RemoteClassMetadata contactTypeClass = wsBean.getClass(contactType, Page.getCurrent().getWebBrowser().getAddress(), 
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                    
                    for (String attributeName : contactTypeClass.getAttributesNames()) {
                        if (!attributeName.equals("name") && !attributeName.equals("creationDate")) { //We ignore the name (already added) and the creation date (unnecessary)
                            tblContactsPerType.addColumn((source) -> {
                                try {
                                    return wsBean.getAttributeValueAsString(source.getClassName(), source.getId(),
                                            attributeName, Page.getCurrent().getWebBrowser().getAddress(), ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                } catch (ServerSideException ex) {
                                    return ex.getMessage();
                                }
                            }).setCaption(attributeName);
                        }
                    }
                    tblContactsPerType.setSizeFull();
                    lytContacts.addComponent(tblContactsPerType);
                }
                
                lytContacts.setWidth(100, Unit.PERCENTAGE);
                
                this.contentComponent = lytContacts;
            }
        } catch (ServerSideException ex) {
            this.contentComponent = new Label(ex.getMessage());
        }        
    }
}

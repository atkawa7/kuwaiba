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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.web.gui.dashboards.AbstractTab;
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
public class ContactsTabWidget extends AbstractTab {
    /**
     * The customer we want the Contacts from
     */
    private RemoteObjectLight customer;
    /**
     * Web service bean reference
     */
    private WebserviceBean wsBean;
    
    public ContactsTabWidget(RemoteObjectLight customer, WebserviceBean wsBean) {
//        super("Contacts");
        this.customer = customer;
        this.wsBean = wsBean;
        this.createContent();
        
    }

    @Override
    public void createContent() {        
        try {
            RemoteSession currentSession = UI.getCurrent().getSession().getAttribute(RemoteSession.class);
            List<RemoteContact> customerContacts = wsBean.getContactsForCustomer(customer.getClassName(), customer.getId(), currentSession.getIpAddress(), 
                currentSession.getSessionId());
            
            if (customerContacts.isEmpty()) 
                getContentPage().add(new Label("The customer associated to this service does not have registered contacts"));
            
            else {
                
                HashMap<String, List<RemoteContact>> contactsPerClass = new HashMap<>();
                VerticalLayout lytContacts = new VerticalLayout();
                
                for(RemoteContact contact : customerContacts) {
                    if (!contactsPerClass.containsKey(contact.getClassName()))
                        contactsPerClass.put(contact.getClassName(), new ArrayList<>());

                    contactsPerClass.get(contact.getClassName()).add(contact);
                }
                
                for (String contactType : contactsPerClass.keySet()) {
//                    Grid<RemoteContact> tblContactsPerType = new Grid<>(contactType);
                    Grid<RemoteContact> tblContactsPerType = new Grid<>();
                    tblContactsPerType.setItems(contactsPerClass.get(contactType));
                    
                    tblContactsPerType.addColumn(RemoteContact::getName).setHeader("Name");
                    
                    RemoteClassMetadata contactTypeClass = wsBean.getClass(contactType, currentSession.getIpAddress(), 
                            currentSession.getSessionId());
                    
                    for (String attributeName : contactTypeClass.getAttributesNames()) {
                        if (!attributeName.equals("name") && !attributeName.equals("creationDate")) { //We ignore the name (already added) and the creation date (unnecessary)
                            tblContactsPerType.addColumn((source) -> {
                                try {
                                    return wsBean.getAttributeValueAsString(source.getClassName(), source.getId(),
                                            attributeName, currentSession.getIpAddress(), currentSession.getSessionId());
                                } catch (ServerSideException ex) {
                                    return ex.getMessage();
                                }
                            }).setHeader(attributeName);
                        }
                    }
                    tblContactsPerType.setSizeFull();
                    lytContacts.add(tblContactsPerType);
                }
                
                lytContacts.setWidthFull();
                
                getContentPage().add(lytContacts);
            }
        } catch (ServerSideException ex) {
            getContentPage().add(new Label("There was an error loading Customer contacts. Please contact the Administrator."));
            System.out.println("Error con ContactsTabWidget: " + ex.getMessage());
        }        
    }
}

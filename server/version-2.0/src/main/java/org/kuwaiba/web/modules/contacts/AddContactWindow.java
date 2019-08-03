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
package org.kuwaiba.web.modules.contacts;

import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.apis.web.gui.events.OperationResultListener;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * A simple window to add contacts
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class AddContactWindow extends Window {

    /**
     * To be used when you already know what customer the contact will be related to
     * @param customer The customer
     * @param wsBean The reference to the backend bean
     * @param listener The listener to perform actions after the operation has been finished successfully
     */
    public AddContactWindow(RemoteObjectLight customer, WebserviceBean wsBean, 
            OperationResultListener listener) {
        try {
            List<RemoteClassMetadataLight> contactTypes = wsBean.getSubClassesLight(Constants.CLASS_GENERICCONTACT,
                            false, false, Page.getCurrent().getWebBrowser().getAddress(), ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

            ComboBox<RemoteClassMetadataLight> cmbContactTypes = new ComboBox<>("Type", contactTypes);
            cmbContactTypes.setEmptySelectionAllowed(false);
            cmbContactTypes.setTextInputAllowed(false);
            cmbContactTypes.setRequiredIndicatorVisible(true);
            cmbContactTypes.setSizeFull();

            TextField txtName = new TextField("Name");
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            Button btnOK = new Button("OK", (e) -> {
                try {
                    if (!cmbContactTypes.getSelectedItem().isPresent())
                        Notifications.showError("You must select a contact type");
                    else {
                        
                        List<StringPair> contactProperties = new ArrayList<>();
                        contactProperties.add(new StringPair("name", txtName.getValue()));
                        
                        wsBean.createContact(cmbContactTypes.getSelectedItem().get().getClassName(), contactProperties, 
                                customer.getClassName(), customer.getId(), Page.getCurrent().getWebBrowser().getAddress(), 
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                        listener.doIt();
                        close();
                    }
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getMessage());
                }
            });
            btnOK.setClickShortcut(ShortcutAction.KeyCode.ENTER);

            btnOK.setEnabled(false);
            txtName.addValueChangeListener((e) -> {
                btnOK.setEnabled(!txtName.isEmpty());
            });

            Button btnCancel = new Button("Cancel", (e) -> {
                close();
            });

            setModal(true);
            setWidth(40, Unit.PERCENTAGE);
            center();

            FormLayout lytTextFields = new FormLayout(cmbContactTypes, txtName);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
            VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
            lytMain.setComponentAlignment(lytMoreButtons, Alignment.TOP_RIGHT);

            setContent(lytMain);
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getLocalizedMessage());
        }
    }

    /**
     * To be used when you already know what customer the contact will be related to
     * @param wsBean The reference to the backend bean
     * @param listener The listener to perform actions after the operation has been finished successfully
     */
    public AddContactWindow(WebserviceBean wsBean, 
            OperationResultListener listener) {
        try {
            
            List<RemoteObjectLight> customers = wsBean.getObjectsOfClassLight(Constants.CLASS_GENERICCUSTOMER,
                            -1, Page.getCurrent().getWebBrowser().getAddress(), ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            ComboBox<RemoteObjectLight> cmbCustomers = new ComboBox<>("Customer", customers);
            cmbCustomers.setEmptySelectionAllowed(false);
            cmbCustomers.setRequiredIndicatorVisible(true);
            cmbCustomers.setSizeFull();
            
            List<RemoteClassMetadataLight> contactTypes = wsBean.getSubClassesLight(Constants.CLASS_GENERICCONTACT,
                            false, false, Page.getCurrent().getWebBrowser().getAddress(), ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

            ComboBox<RemoteClassMetadataLight> cmbContactTypes = new ComboBox<>("Type", contactTypes);
            cmbContactTypes.setEmptySelectionAllowed(false);
            cmbContactTypes.setTextInputAllowed(false);
            cmbContactTypes.setRequiredIndicatorVisible(true);
            cmbContactTypes.setSizeFull();

            TextField txtName = new TextField("Name");
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            Button btnOK = new Button("OK", (e) -> {
                try {
                    if (!cmbContactTypes.getSelectedItem().isPresent() || !cmbCustomers.getSelectedItem().isPresent())
                        Notifications.showError("You must provide a valid contact type and customer");
                    else {
                        
                        List<StringPair> contactProperties = new ArrayList<>();
                        contactProperties.add(new StringPair("name", txtName.getValue()));
                        
                        wsBean.createContact(cmbContactTypes.getSelectedItem().get().getClassName(), contactProperties, 
                                cmbCustomers.getSelectedItem().get().getClassName(), cmbCustomers.getSelectedItem().get().getId(), Page.getCurrent().getWebBrowser().getAddress(), 
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                        listener.doIt();
                        close();
                    }
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getMessage());
                }
            });

            btnOK.setEnabled(false);
            txtName.addValueChangeListener((e) -> {
                btnOK.setEnabled(!txtName.isEmpty());
            });

            Button btnCancel = new Button("Cancel", (e) -> {
                close();
            });

            setModal(true);
            setWidth(40, Unit.PERCENTAGE);
            center();

            FormLayout lytTextFields = new FormLayout(cmbCustomers, cmbContactTypes, txtName);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
            VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
            lytMain.setComponentAlignment(lytMoreButtons, Alignment.TOP_RIGHT);

            setContent(lytMain);
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getLocalizedMessage());
        }
    }
    
}

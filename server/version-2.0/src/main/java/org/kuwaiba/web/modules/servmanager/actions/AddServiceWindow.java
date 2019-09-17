/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.web.modules.servmanager.actions;

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
import java.util.List;
import org.kuwaiba.apis.web.gui.events.OperationResultListener;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemotePool;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * A simple window that allows to create a new service
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class AddServiceWindow extends Window {
    /**
     * Constructor to be used if you already know what's the pool the service will be created in
     * @param servicePool The service pool the service will be added to
     * @param wsBean The reference to the web service bean
     * @param listener What to do after the operation has been performed
     */
    public AddServiceWindow(RemotePool servicePool, WebserviceBean wsBean, 
            OperationResultListener listener) {
        super("New Service");
        
        try {
        
            List<RemoteClassMetadataLight> serviceTypes = wsBean.getSubClassesLight(Constants.CLASS_GENERICSERVICE,
                            false, false, Page.getCurrent().getWebBrowser().getAddress(), ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

            ComboBox<RemoteClassMetadataLight> cmbServiceTypes = new ComboBox<>("Type", serviceTypes);
            cmbServiceTypes.setEmptySelectionAllowed(false);
            cmbServiceTypes.setRequiredIndicatorVisible(true);
            cmbServiceTypes.setSizeFull();

            TextField txtName = new TextField("Name");
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            Button btnOK = new Button("OK", (e) -> {
                try {
                    if (!cmbServiceTypes.getSelectedItem().isPresent())
                        Notifications.showError("You must select a service type");
                    else {
                        wsBean.createPoolItem(servicePool.getId(), cmbServiceTypes.getSelectedItem().get().getClassName(), 
                                new String[] { "name" }, new String[] { txtName.getValue() }, null, Page.getCurrent().getWebBrowser().getAddress(), 
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

            FormLayout lytTextFields = new FormLayout(cmbServiceTypes, txtName);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
            VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
            lytMain.setComponentAlignment(lytMoreButtons, Alignment.TOP_RIGHT);

            setContent(lytMain);
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
    }
    
    /**
     * This constructor should be used if you want to create a new service requesting all the required 
     * information to the user
     * @param wsBean Web service bean reference
     * @param listener What to do after the operation has been performed
     */
    public AddServiceWindow(WebserviceBean wsBean, OperationResultListener listener) {
        super("New Service");
        try {
            List<RemoteObjectLight> customers = wsBean.getObjectsOfClassLight(Constants.CLASS_GENERICCUSTOMER, -1, Page.getCurrent().getWebBrowser().getAddress(), 
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            ComboBox<RemoteObjectLight> cmbCustomers = new ComboBox<>("Customer", customers);
            cmbCustomers.setEmptySelectionAllowed(false);
            cmbCustomers.setRequiredIndicatorVisible(true);
            cmbCustomers.setSizeFull();
                       
            ComboBox<RemotePool> cmbServicePools = new ComboBox<>("Service Pools");
            cmbServicePools.setEmptySelectionAllowed(false);
            cmbServicePools.setRequiredIndicatorVisible(true);
            cmbServicePools.setTextInputAllowed(false);
            cmbServicePools.setSizeFull();
            
            cmbCustomers.addSelectionListener((e) -> {
                RemoteObjectLight customer = cmbCustomers.getSelectedItem().get();
                try {
                    List<RemotePool> servicePools = wsBean.getPoolsInObject(customer.getClassName(), customer.getId(), 
                            Constants.CLASS_GENERICSERVICE, Page.getCurrent().getWebBrowser().getAddress(), 
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                    cmbServicePools.setItems(servicePools);
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getMessage());
                }
            });
            
            List<RemoteClassMetadataLight> serviceTypes = wsBean.getSubClassesLight(Constants.CLASS_GENERICSERVICE,
                        false, false, Page.getCurrent().getWebBrowser().getAddress(), ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            ComboBox<RemoteClassMetadataLight> cmbServiceTypes = new ComboBox<>("Type", serviceTypes);
            cmbServiceTypes.setEmptySelectionAllowed(false);
            cmbServiceTypes.setRequiredIndicatorVisible(true);
            cmbServiceTypes.setSizeFull();
        
            TextField txtName = new TextField("Name");
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            Button btnOK = new Button("OK", (e) -> {
                try {
                    if (!cmbCustomers.getSelectedItem().isPresent() || !cmbServiceTypes.getSelectedItem().isPresent() || 
                            !cmbServicePools.getSelectedItem().isPresent() || txtName.isEmpty())
                        Notifications.showError("You must fill-in all the fields");
                    else {
                        wsBean.createPoolItem(cmbServicePools.getSelectedItem().get().getId(), cmbServiceTypes.getSelectedItem().get().getClassName(), 
                                new String[] { "name" }, new String[] { txtName.getValue()}, null, Page.getCurrent().getWebBrowser().getAddress(), 
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

            FormLayout lytTextFields = new FormLayout(cmbCustomers, cmbServicePools, cmbServiceTypes, txtName);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
            VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
            lytMain.setComponentAlignment(lytMoreButtons, Alignment.TOP_RIGHT);

            setContent(lytMain);
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        } 
    }
}

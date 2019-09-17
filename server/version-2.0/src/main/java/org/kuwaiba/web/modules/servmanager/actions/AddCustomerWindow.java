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
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.web.gui.events.OperationResultListener;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemotePool;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * A simple window to create a customer
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class AddCustomerWindow extends Window {
    /**
     * Constructor to be used if you already know what's the pool the service will be added to
     * @param customerPool The customer pool the customer will be added to
     * @param wsBean The reference to the web service bean
     * @param listener What to do after the operation has been performed
     */
    public AddCustomerWindow(RemotePool customerPool, WebserviceBean wsBean, 
            OperationResultListener listener) {
        super("New Customer");
        
        try {
        
            List<RemoteClassMetadataLight> customerTypes = wsBean.getSubClassesLight(Constants.CLASS_GENERICCUSTOMER,
                            false, false, Page.getCurrent().getWebBrowser().getAddress(), ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

            ComboBox<RemoteClassMetadataLight> cmbCustomerTypes = new ComboBox<>("Type", customerTypes);
            cmbCustomerTypes.setEmptySelectionAllowed(false);
            cmbCustomerTypes.setRequiredIndicatorVisible(true);
            cmbCustomerTypes.setSizeFull();

            TextField txtName = new TextField("Name");
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            Button btnOK = new Button("OK", (e) -> {
                try {
                    if (!cmbCustomerTypes.getSelectedItem().isPresent())
                        Notifications.showError("You must select a customer type");
                    else {
                        wsBean.createPoolItem(customerPool.getId(), cmbCustomerTypes.getSelectedItem().get().getClassName(), 
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

            FormLayout lytTextFields = new FormLayout(cmbCustomerTypes, txtName);
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
    public AddCustomerWindow(WebserviceBean wsBean, OperationResultListener listener) {
        super("New Customer");
        try {
            
            List<RemotePool> customerPools = wsBean.getRootPools(Constants.CLASS_GENERICCUSTOMER, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, 
                    false, Page.getCurrent().getWebBrowser().getAddress(), ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            ComboBox<RemotePool> cmbCustomerPools = new ComboBox<>("Customer Pool", customerPools);
            cmbCustomerPools.setEmptySelectionAllowed(false);
            cmbCustomerPools.setRequiredIndicatorVisible(true);
            cmbCustomerPools.setTextInputAllowed(false);
            cmbCustomerPools.setSizeFull();
            
            List<RemoteClassMetadataLight> customerTypes = wsBean.getSubClassesLight(Constants.CLASS_GENERICCUSTOMER,
                        false, false, Page.getCurrent().getWebBrowser().getAddress(), ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            ComboBox<RemoteClassMetadataLight> cmbCustomerTypes = new ComboBox<>("Type", customerTypes);
            cmbCustomerTypes.setEmptySelectionAllowed(false);
            cmbCustomerTypes.setRequiredIndicatorVisible(true);
            cmbCustomerTypes.setSizeFull();
        
            TextField txtName = new TextField("Name");
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            Button btnOK = new Button("OK", (e) -> {
                try {
                    if (!cmbCustomerPools.getSelectedItem().isPresent() 
                            || !cmbCustomerTypes.getSelectedItem().isPresent() || txtName.isEmpty())
                        Notifications.showError("You must fill-in all the fields");
                    else {
                        wsBean.createPoolItem(cmbCustomerPools.getSelectedItem().get().getId(), cmbCustomerTypes.getSelectedItem().get().getClassName(), 
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

            FormLayout lytTextFields = new FormLayout(cmbCustomerPools, cmbCustomerTypes, txtName);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
            VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
            lytMain.setComponentAlignment(lytMoreButtons, Alignment.TOP_RIGHT);

            setContent(lytMain);
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        } 
    }
}

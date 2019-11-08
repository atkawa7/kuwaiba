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

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import org.kuwaiba.apis.web.gui.events.OperationResultListener;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemotePool;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.services.persistence.util.Constants;
import org.openide.util.Exceptions;

/**
 * A simple window that allows to create a new service
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
//public class AddServiceWindow{}
public class AddServiceWindow extends Dialog {
    /**
     * Constructor to be used if you already know what's the pool the service will be created in
     * @param servicePool The service pool the service will be added to
     * @param wsBean The reference to the web service bean
     * @param listener What to do after the operation has been performed
     */
    public AddServiceWindow(RemotePool servicePool, WebserviceBean wsBean, 
            OperationResultListener listener) {
//        super("New Service");
        getUI().ifPresent(ui -> {
            RemoteSession currentSession = ui.getSession().getAttribute(RemoteSession.class);
            List<RemoteClassMetadataLight> serviceTypes;
            try {
                serviceTypes = wsBean.getSubClassesLight(Constants.CLASS_GENERICSERVICE,
                        false, false, UI.getCurrent().getRouter().getUrl(AddServiceWindow.class), currentSession.getSessionId());
            
                ComboBox<RemoteClassMetadataLight> cmbServiceTypes = new ComboBox<>("Type", serviceTypes);
    //        cmbServiceTypes.setEmptySelectionAllowed(false);
                cmbServiceTypes.setRequiredIndicatorVisible(true);
                cmbServiceTypes.setSizeFull();
                TextField txtName = new TextField("Name");
                txtName.setRequiredIndicatorVisible(true);
                txtName.setSizeFull();
                Button btnOK = new Button("OK", (e) -> {
                    if (cmbServiceTypes.getValue() == null)
                        new Notification("You must select a service type", 3000, Position.TOP_START).open();
                    else {

                            try {
                                wsBean.createPoolItem(servicePool.getId(), cmbServiceTypes.getValue().getClassName(),
                                        new String[] { "name" }, new String[] { txtName.getValue() }, null, UI.getCurrent().getRouter().getUrl(AddServiceWindow.class),
                                        currentSession.getSessionId());
                            } catch (ServerSideException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            listener.doIt();
                            close();

                    }
                });
                btnOK.addClickShortcut(Key.ENTER);
                btnOK.setEnabled(false);
                txtName.addValueChangeListener((e) -> {
                    btnOK.setEnabled(!txtName.isEmpty());
                });
                Button btnCancel = new Button("Cancel", (e) -> {
                    close();
                });
                //            setModal(true);
                setWidth("40%");
        //            center();
                FormLayout lytTextFields = new FormLayout(cmbServiceTypes, txtName);
                HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
                VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
                lytMain.setAlignItems(Alignment.END);
                add(lytMain);
            } catch (ServerSideException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }
    
    /**
     * This constructor should be used if you want to create a new service requesting all the required 
     * information to the user
     * @param wsBean Web service bean reference
     * @param listener What to do after the operation has been performed
     */
    public AddServiceWindow(WebserviceBean wsBean, OperationResultListener listener) {
//        super("New Service");
        getUI().ifPresent(ui -> {
            RemoteSession currentSession = ui.getSession().getAttribute(RemoteSession.class);
            String currentUrl = UI.getCurrent().getRouter().getUrl(AddServiceWindow.class);
            List<RemoteObjectLight> customers;
            try {
                customers = wsBean.getObjectsOfClassLight(Constants.CLASS_GENERICCUSTOMER, -1, currentUrl, 
                        currentSession.getSessionId());
            
                ComboBox<RemoteObjectLight> cmbCustomers = new ComboBox<>("Customer", customers);
        //        cmbCustomers.setEmptySelectionAllowed(false);
                cmbCustomers.setRequiredIndicatorVisible(true);
                cmbCustomers.setSizeFull();
                ComboBox<RemotePool> cmbServicePools = new ComboBox<>("Service Pools");
        //        cmbServicePools.setEmptySelectionAllowed(false);
                cmbServicePools.setRequiredIndicatorVisible(true);
        //        cmbServicePools.setTextInputAllowed(false);
                cmbServicePools.setSizeFull();
                cmbCustomers.addValueChangeListener((e) -> {
                    RemoteObjectLight customer = cmbCustomers.getValue();
                    List<RemotePool> servicePools;
                    try {
                        servicePools = wsBean.getPoolsInObject(customer.getClassName(), customer.getId(),
                                Constants.CLASS_GENERICSERVICE, currentUrl,
                                currentSession.getSessionId());
                    
                    cmbServicePools.setItems(servicePools);
                    } catch (ServerSideException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                });
                List<RemoteClassMetadataLight> serviceTypes = wsBean.getSubClassesLight(Constants.CLASS_GENERICSERVICE,
                        false, false, currentUrl, currentSession.getSessionId());
                ComboBox<RemoteClassMetadataLight> cmbServiceTypes = new ComboBox<>("Type", serviceTypes);
        //        cmbServiceTypes.setEmptySelectionAllowed(false);
                cmbServiceTypes.setRequiredIndicatorVisible(true);
                cmbServiceTypes.setSizeFull();
                TextField txtName = new TextField("Name");
                txtName.setRequiredIndicatorVisible(true);
                txtName.setSizeFull();
                Button btnOK = new Button("OK", (e) -> {
                    if (cmbCustomers.getValue() == null || cmbServiceTypes.getValue() == null ||
                            cmbServicePools.getValue() == null || txtName.isEmpty())
                        new Notification("You must fill-in all the fields", 3000, Position.TOP_START).open();
                    else {
                        try {
                            wsBean.createPoolItem(cmbServicePools.getValue().getId(), cmbServiceTypes.getValue().getClassName(),
                                    new String[] { "name" }, new String[] { txtName.getValue()}, null, currentUrl,
                                    currentSession.getSessionId());
                        } catch (ServerSideException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        listener.doIt();
                        close();
                    }
                });
                btnOK.setEnabled(false);
                txtName.addValueChangeListener((e) -> {
                    btnOK.setEnabled(!txtName.isEmpty());
                });
                Button btnCancel = new Button("Cancel", (e) -> {
                    close();
                });
                //            setModal(true);
                setWidth("40%");
                //            center();
                FormLayout lytTextFields = new FormLayout(cmbCustomers, cmbServicePools, cmbServiceTypes, txtName);
                HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
                VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
                lytMain.setAlignItems(Alignment.END);
                add(lytMain);
                } catch (ServerSideException ex) {
                Exceptions.printStackTrace(ex);
            }
          });
        }
}

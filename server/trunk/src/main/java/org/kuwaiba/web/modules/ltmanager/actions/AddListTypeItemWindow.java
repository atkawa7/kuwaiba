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
package org.kuwaiba.web.modules.ltmanager.actions;

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
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * A simple window that allows to create a new list type item
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class AddListTypeItemWindow extends Window {
    /**
     * Constructor to be used if you already know what list type the new list type item will be instance of
     * @param listType The list type
     * @param wsBean The reference to the web service bean
     * @param listener What to do after the operation has been performed
     */
    public AddListTypeItemWindow(RemoteClassMetadataLight listType, WebserviceBean wsBean, 
            OperationResultListener listener) {
        super("New List Type Item");
        
        TextField txtName = new TextField("Name");
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();

        TextField txtDisplayName = new TextField("Display Name");
        txtDisplayName.setSizeFull();

        Button btnOK = new Button("OK", (e) -> {
            try {
                wsBean.createListTypeItem(listType.getClassName(), txtName.getValue(), 
                        txtDisplayName.getValue(), Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                listener.doIt();
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getMessage());
            }
            close();
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

        FormLayout lytTextFields = new FormLayout(txtName, txtDisplayName);
        HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
        VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
        lytMain.setComponentAlignment(lytMoreButtons, Alignment.TOP_RIGHT);

        setContent(lytMain);
    }
    
    /**
     * This constructor should be used if you want to create a new list type, and you want the 
     * user to select the list type from a list
     * @param wsBean Web service bean reference
     * @param listener What to do after the operation has been performed
     */
    public AddListTypeItemWindow(WebserviceBean wsBean, OperationResultListener listener) {
        super("New List Type Item");
        try {
            List<RemoteClassMetadataLight> listTypes = wsBean.getSubClassesLight(Constants.CLASS_GENERICOBJECTLIST, false, false, Page.getCurrent().getWebBrowser().getAddress(), 
                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
            
            ComboBox<RemoteClassMetadataLight> cmbListTypes = new ComboBox<>("List Type", listTypes);
            cmbListTypes.setEmptySelectionAllowed(false);
            cmbListTypes.setRequiredIndicatorVisible(true);
            cmbListTypes.setSizeFull();
        
            TextField txtName = new TextField("Name");
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            TextField txtDisplayName = new TextField("Display Name");
            txtDisplayName.setSizeFull();

            Button btnOK = new Button("OK", (e) -> {
                try {
                    if (!cmbListTypes.getSelectedItem().isPresent())
                        Notifications.showError("You must select a list type first");
                    else {
                        wsBean.createListTypeItem(cmbListTypes.getSelectedItem().get().getClassName(), txtName.getValue(), 
                                txtDisplayName.getValue(), Page.getCurrent().getWebBrowser().getAddress(), 
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

            FormLayout lytTextFields = new FormLayout(cmbListTypes, txtName, txtDisplayName);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
            VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
            lytMain.setComponentAlignment(lytMoreButtons, Alignment.TOP_RIGHT);

            setContent(lytMain);
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        } 
    }
}

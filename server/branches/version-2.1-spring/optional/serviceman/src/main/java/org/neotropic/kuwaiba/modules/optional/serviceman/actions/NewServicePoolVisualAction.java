/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.modules.optional.serviceman.actions;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.AbstractModuleAction;
import org.neotropic.kuwaiba.core.apis.integration.AbstractVisualModuleAction;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of a new customer pool action.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class NewServicePoolVisualAction extends AbstractVisualModuleAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewServicePoolAction newServicePoolAction;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    protected BusinessEntityManager bem;
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameter... parameters) {
        try {
            Dialog wdwNewCustomerPool = new Dialog();
            
            ComboBox<BusinessObjectLight> cmbCustomers = new ComboBox<>(ts.getTranslatedString("module.serviceman.actions.new-service-pool.ui.customer"), 
                    bem.getObjectsOfClassLight(Constants.CLASS_GENERICCUSTOMER, -1));
            cmbCustomers.setWidthFull();
            
            TextField txtName = new TextField(ts.getTranslatedString("module.serviceman.actions.new-customer-pool.ui.pool-name"));
            txtName.setRequired(true);
            txtName.setRequiredIndicatorVisible(true);
            txtName.setWidthFull();

            TextField txtDescription = new TextField(ts.getTranslatedString("module.serviceman.actions.new-customer-pool.ui.pool-description"));
            txtDescription.setWidthFull();

            Label lblMessages = new Label();
            lblMessages.setClassName("embedded-notification-error");
            lblMessages.setWidthFull();
            lblMessages.setVisible(false);

            Button btnOK = new Button(ts.getTranslatedString("module.general.messages.ok"), e -> {
                try {
                    if (txtName.isEmpty() || cmbCustomers.getValue() == null) {
                        lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                        lblMessages.setVisible(true);
                    } else {
                        newServicePoolAction.getCallback().execute(new ModuleActionParameter<>(Constants.PROPERTY_PARENT, cmbCustomers.getValue()), 
                                new ModuleActionParameter<>(Constants.PROPERTY_NAME, txtName.getValue()), 
                                new ModuleActionParameter<>(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue()));

                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCESS, 
                                ts.getTranslatedString("module.serviceman.actions.new-customer-pool.ui.customer-pool-created-success"), NewCustomerPoolAction.class));
                        wdwNewCustomerPool.close();
                    }
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR, 
                                ex.getMessage(), NewCustomerAction.class));
                }
            });

            btnOK.setEnabled(false);
            txtName.addValueChangeListener(e -> {
                btnOK.setEnabled(!txtName.isEmpty());
            });

            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (e) -> {
                wdwNewCustomerPool.close();
            });

            FormLayout lytTextFields = new FormLayout(cmbCustomers, txtName, txtDescription);
            lytTextFields.setWidthFull();
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
            lytMoreButtons.setAlignItems(FlexComponent.Alignment.END);
            VerticalLayout lytMain = new VerticalLayout(lblMessages, lytTextFields, lytMoreButtons);
            lytMain.setSizeFull();

            wdwNewCustomerPool.add(lytMain);

            return wdwNewCustomerPool;
            
        } catch(InventoryException ex) {
            return new Dialog(new Label(ex.getLocalizedMessage()));
        }
    }

    @Override
    public AbstractModuleAction getModuleAction() {
        return newServicePoolAction;
    }
}
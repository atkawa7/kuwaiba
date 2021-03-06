/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.modules.optional.serviceman.actions;

import java.util.HashMap;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of a new customer action that provides means to choose the service pool and type.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class NewCustomerVisualAction extends AbstractVisualInventoryAction {
    private Pool selectedCustomerPool; 
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewCustomerAction newCustomerAction;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    protected MetadataEntityManager mem;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    protected BusinessEntityManager bem;
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        // This action might be called with or without parameters depending on who launches it. 
        // For example, if launched from the dashboard, it won't received any initial parameter and all the 
        // necessary information will have to be requested (the parent customer pool and the customer type), 
        // but if launched from a customer pool, only the customer type will be requested.
        try {
            Dialog wdwNewCustomer = new Dialog();
            
            ComboBox<Pool> cmbCustomerPools = new ComboBox<>(ts.getTranslatedString("module.serviceman.actions.new-customer.ui.customer-pool"));
            cmbCustomerPools.setRequiredIndicatorVisible(true);
            cmbCustomerPools.setAllowCustomValue(false);
            cmbCustomerPools.setSizeFull();
            cmbCustomerPools.addValueChangeListener( ev -> this.selectedCustomerPool = cmbCustomerPools.getValue() );

            ComboBox<ClassMetadataLight> cmbCustomerTypes = 
                    new ComboBox<>(ts.getTranslatedString("module.serviceman.actions.new-customer.ui.customer-type"), 
                                   mem.getSubClassesLight(Constants.CLASS_GENERICCUSTOMER, false, false));
            cmbCustomerTypes.setRequiredIndicatorVisible(true);
            cmbCustomerTypes.setSizeFull();
            
            // To show errors or warnings related to the input parameters.
            Label lblMessages = new Label();
            lblMessages.setClassName("embedded-notification-error");
            lblMessages.setWidthFull();
            lblMessages.setVisible(false);
            
            TextField txtName = new TextField(ts.getTranslatedString("module.serviceman.actions.new-customer.ui.customer-name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            Button btnOK = new Button(ts.getTranslatedString("module.general.messages.ok"), (e) -> {
                try {
                    if (this.selectedCustomerPool == null || cmbCustomerTypes.getValue() == null || txtName.isEmpty()) {
                        lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                        lblMessages.setVisible(true);
                    } else {
                        HashMap<String, String> attributes = new HashMap<>();
                        attributes.put(Constants.PROPERTY_NAME, txtName.getValue());
                        newCustomerAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("poolId", this.selectedCustomerPool.getId()), 
                                new ModuleActionParameter<>("customerClass", cmbCustomerTypes.getValue().getName()),
                                new ModuleActionParameter<>("attributes", attributes)));
                        
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS, 
                                ts.getTranslatedString("module.serviceman.actions.new-customer.ui.customer-created-success"), NewCustomerAction.class));
                        wdwNewCustomer.close();
                    }
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR, 
                                ex.getMessage(), NewCustomerAction.class));
                }
            });

            btnOK.setEnabled(false);
            txtName.addValueChangeListener((e) -> {
                btnOK.setEnabled(!txtName.isEmpty());
            });

            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (e) -> {
                wdwNewCustomer.close();
            });

            FormLayout lytTextFields = new FormLayout();
            if (parameters.containsKey("customerPool")) // The action is launched from a customer pool
                this.selectedCustomerPool = (Pool)parameters.get("customerPool");
            else { // The action is launched without context
                cmbCustomerPools.setItems(bem.getRootPools(Constants.CLASS_GENERICCUSTOMER, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false));
                lytTextFields.add(cmbCustomerPools);
            }
            
            lytTextFields.add(cmbCustomerTypes, txtName);
            lytTextFields.setWidthFull();
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
            lytMoreButtons.setAlignItems(FlexComponent.Alignment.END);
            VerticalLayout lytMain = new VerticalLayout(lblMessages, lytTextFields, lytMoreButtons);
            lytMain.setSizeFull();
            wdwNewCustomer.add(lytMain);
            
            return wdwNewCustomer;
        } catch (InventoryException ex) {
            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR, 
                                ex.getMessage(), NewCustomerAction.class));
            return new Dialog(new Label(ex.getMessage()));
        } 
    }

    @Override
    public AbstractAction getModuleAction() {
        return newCustomerAction;
    }
    
    @Override
    public boolean isQuickAction() {
        return true;
    }
    
    @Override
    public String appliesTo() {
        return null;
    }
    
    @Override
    public boolean isModuleAction() {
        return true;
    }
}

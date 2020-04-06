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

import java.util.List;
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
import org.neotropic.kuwaiba.core.apis.integration.AbstractModuleAction;
import org.neotropic.kuwaiba.core.apis.integration.AbstractVisualModuleAction;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
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
public class NewServiceVisualAction extends AbstractVisualModuleAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewServiceAction newServiceAction;
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
    public Dialog getVisualComponent(ModuleActionParameter... parameters) {
        // This action might be called with or without parameters depending on who launches it. 
        // For example, if launched from the dashboard, it won't received any initial parameter and all the 
        // necessary information will have to be requested (the parent customer pool and the customer type), 
        // but if launched from a customer pool, only the customer type will be requested.
        try {
            Dialog wdwNewCustomer = new Dialog();
            
            // To show errors or warnings related to the input parameters.
            Label lblMessages = new Label();
            
            List<BusinessObjectLight> customers = bem.getObjectsOfClassLight(Constants.CLASS_GENERICCUSTOMER, -1);
            ComboBox<BusinessObjectLight> cmbCustomers = new ComboBox<>(ts.getTranslatedString("module.serviceman.actions.new-service.ui.customer"), customers);
            cmbCustomers.setRequired(true);
            cmbCustomers.setAllowCustomValue(false);
            cmbCustomers.setSizeFull();
            
            ComboBox<Pool> cmbServicePools = new ComboBox<>(ts.getTranslatedString("module.serviceman.actions.new-service.ui.service-pool"));
            cmbServicePools.setRequiredIndicatorVisible(true);
            cmbServicePools.setAllowCustomValue(false);
            cmbServicePools.setSizeFull();
            cmbCustomers.addValueChangeListener((event) -> {
                try {
                    BusinessObjectLight selectedCustomer = cmbCustomers.getValue();
                    cmbServicePools.setItems(bem.getPoolsInObject(selectedCustomer.getClassName(), selectedCustomer.getId(), Constants.CLASS_GENERICSERVICE));
                } catch (InventoryException ex) {
                    lblMessages.setText(ex.getLocalizedMessage());
                }
            });
            
            List<ClassMetadataLight> serviceTypes = mem.getSubClassesLight(Constants.CLASS_GENERICSERVICE, false, false);

            ComboBox<ClassMetadataLight> cmbServiceTypes = 
                    new ComboBox<>(ts.getTranslatedString("module.serviceman.actions.new-service.ui.service-type"), serviceTypes);
            cmbServiceTypes.setRequiredIndicatorVisible(true);
            cmbServiceTypes.setSizeFull();

            
            TextField txtName = new TextField(ts.getTranslatedString("module.serviceman.actions.new-service.ui.service-name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            Button btnOK = new Button(ts.getTranslatedString("module.general.messages.ok"), (e) -> {
                try {
                    if (cmbCustomers.getValue() == null || cmbServicePools.getValue() == null || cmbServiceTypes.getValue() == null || txtName.isEmpty())
                        lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                    else {
                        HashMap<String, String> attributes = new HashMap<>();
                        attributes.put(Constants.PROPERTY_NAME, txtName.getValue());
                        newServiceAction.getCallback().execute(new ModuleActionParameter<>("poolId", cmbServicePools.getValue().getId()), 
                                new ModuleActionParameter<>("serviceClass", cmbServiceTypes.getValue().getName()),
                                new ModuleActionParameter<>("attributes", attributes));
                        
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCESS, 
                                ts.getTranslatedString("module.serviceman.actions.new-service.ui.service-created-success"), NewCustomerAction.class));
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

            FormLayout lytTextFields = new FormLayout(cmbCustomers, cmbServicePools, cmbServiceTypes, txtName);
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
    public AbstractModuleAction getModuleAction() {
        return newServiceAction;
    }
}
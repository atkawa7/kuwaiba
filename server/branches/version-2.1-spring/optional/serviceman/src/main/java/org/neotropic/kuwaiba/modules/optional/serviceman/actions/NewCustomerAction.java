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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.AbstractModuleAction;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a new customer.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class NewCustomerAction extends AbstractModuleAction {
    @Autowired
    private TranslationService ts;

    public NewCustomerAction() {
        this.id = "serviceman.new-customer";
        this.displayName = ts.getTranslatedString("module.serviceman.actions.new-customer.name");
        this.description = ts.getTranslatedString("module.serviceman.actions.new-customer.description");
        this.order = 1000;
    
        setCallback((parameters) -> {
            String poolId = ((ModuleActionParameter<String>)parameters[0]).getValue();
            String poolDisplayName = ((ModuleActionParameter<String>)parameters[1]).getValue();
            //ModuleActionParameter<BusinessObjectLight> aa = bem.createPoolItem(id, displayName, attributeNames, attributeValues, displayName)
        });
    }

    @Override
    public int getRequiredAccessLevel() {
        return Privilege.ACCESS_LEVEL_READ_WRITE;
    }

    /**
     * The window that captures the info necessary to create the customer (a parent pool and its type).
     */
    private class AddCustomerWindow extends Dialog {
        /**
        * This constructor should be used if you want to create a new service requesting all the required 
        * information to the user
        * @param wsBean Web service bean reference
        * @param listener What to do after the operation has been performed
        */
        public AddCustomerWindow() {
           try {
               List<Pool> customerPools = bem.getRootPools(Constants.CLASS_GENERICCUSTOMER, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);

               ComboBox<Pool> cmbCustomerPools = new ComboBox<>(ts.getTranslatedString("module.serviceman.ui.customer-pool"), customerPools);
               cmbCustomerPools.setRequiredIndicatorVisible(true);
               cmbCustomerPools.setAllowCustomValue(false);
               cmbCustomerPools.setSizeFull();

               List<ClassMetadataLight> customerTypes = mem.getSubClassesLight(Constants.CLASS_GENERICCUSTOMER, false, false);

               ComboBox<ClassMetadataLight> cmbCustomerTypes = new ComboBox<>(ts.getTranslatedString("module.serviceman.ui.customer-type"), customerTypes);
               cmbCustomerTypes.setRequiredIndicatorVisible(true);
               cmbCustomerTypes.setSizeFull();

               TextField txtName = new TextField(ts.getTranslatedString("module.serviceman.ui.customer-name"));
               txtName.setRequiredIndicatorVisible(true);
               txtName.setSizeFull();

               Button btnOK = new Button(ts.getTranslatedString("module.serviceman.ui.ok"), (e) -> {
                   try {
                       if (cmbCustomerPools.getValue() == null || cmbCustomerTypes.getValue() == null || txtName.isEmpty())
                           System.out.println("You must fill-in all the fields");
                       else {
                           bem.createPoolItem(cmbCustomerPools.getValue().getId(), cmbCustomerTypes.getValue().getName(), 
                                   new String[] { "name" }, new String[] { txtName.getValue()}, null);
                           close();
                       }
                   } catch (InventoryException ex) {
                       System.out.println(ex.getMessage());
                   }
               });

               btnOK.setEnabled(false);
               txtName.addValueChangeListener((e) -> {
                   btnOK.setEnabled(!txtName.isEmpty());
               });

               Button btnCancel = new Button("Cancel", (e) -> {
                   close();
               });

               setWidth("40%");

               FormLayout lytTextFields = new FormLayout(cmbCustomerPools, cmbCustomerTypes, txtName);
               HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
               VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
               lytMain.setAlignItems(FlexComponent.Alignment.BASELINE);
               lytMain.setSizeFull();
               
               add(lytMain);
           } catch (InventoryException ex) {
               System.out.println(ex.getMessage());
           } 
        }
    }
    
}

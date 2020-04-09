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

package org.neotropic.kuwaiba.modules.core.listtypes.listtypeman.actions;

import java.util.List;
import java.util.HashMap;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.AbstractModuleAction;
import org.neotropic.kuwaiba.core.apis.integration.AbstractVisualModuleAction;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
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
public class NewListTypeItemVisualAction extends AbstractVisualModuleAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewListTypeItemAction newListTypeItemAction;
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
            List<ClassMetadataLight> listTypes = mem.getSubClassesLight(Constants.CLASS_GENERICOBJECTLIST, false, false);
            
            ComboBox<ClassMetadataLight> cmbListTypes = new ComboBox<>("List Type", listTypes);
            cmbListTypes.setAllowCustomValue(false);
            cmbListTypes.setRequiredIndicatorVisible(true);
            cmbListTypes.setSizeFull();
        
            TextField txtName = new TextField("Name");
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            TextField txtDisplayName = new TextField("Display Name");
            txtDisplayName.setSizeFull();
            
            Dialog wdwNewListTypeItem = new Dialog();
            
            // To show errors or warnings related to the input parameters.
            Label lblMessages = new Label();

            Button btnOK = new Button("OK", (e) -> {
                try {
                    if (cmbListTypes.getValue() == null)
                       lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                    else {
                        
                        HashMap<String, String> attributes = new HashMap<>();
                        attributes.put(Constants.PROPERTY_NAME, txtName.getValue());
                        newListTypeItemAction.getCallback().execute(new ModuleActionParameter<>("className", cmbListTypes.getValue().toString()), 
                                new ModuleActionParameter<>("name", txtName.getValue()),
                                new ModuleActionParameter<>("displayName", txtDisplayName.getValue()));
                        
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCESS, 
                                ts.getTranslatedString("module.listtypeman.actions.new-list-type-item.ui.item-created-success"), NewListTypeItemAction.class));
                        wdwNewListTypeItem.close();
                    }
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR, 
                                ex.getMessage(), NewListTypeItemAction.class));
                }
            });

            btnOK.setEnabled(false);
            txtName.addValueChangeListener((e) -> {
                btnOK.setEnabled(!txtName.isEmpty());
            });

            Button btnCancel = new Button("Cancel", (e) -> {
                wdwNewListTypeItem.close();
            });

            FormLayout lytTextFields = new FormLayout(cmbListTypes, txtName, txtDisplayName);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
            VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
            lytMain.setSizeFull();
            
            wdwNewListTypeItem.add(lytMain);
            
            return wdwNewListTypeItem;
        } catch (InventoryException ex) {
            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR, 
                                ex.getMessage(), NewListTypeItemAction.class));
            return new Dialog(new Label(ex.getMessage()));
        } 
    }

    @Override
    public AbstractModuleAction getModuleAction() {
        return newListTypeItemAction;
    }
}

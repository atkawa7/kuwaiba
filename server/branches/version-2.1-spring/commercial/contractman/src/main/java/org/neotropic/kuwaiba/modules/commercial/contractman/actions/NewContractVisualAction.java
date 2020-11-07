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
package org.neotropic.kuwaiba.modules.commercial.contractman.actions;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new contract action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewContractVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewContractAction newContractAction;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    private BusinessEntityManager bem; 
    /**
     * Type of pool module root. These pools are used in models and are the root of such model
     */
    public static final int POOL_TYPE_MODULE_ROOT = 2;
    /**
     * Dialog to create new Contract
     */
    Dialog wdwNewContract;

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            Pool selectedPool = null;
            if (parameters.containsKey("pool"))
                selectedPool = (Pool) parameters.get("pool");
            
            TextField txtPool = new TextField(ts.getTranslatedString("module.contractman.contract.label.pool-type"));
            txtPool.setEnabled(false);
            
            List<Pool> listPool = bem.getRootPools(Constants.CLASS_GENERICCONTRACT, POOL_TYPE_MODULE_ROOT, true);
            ComboBox<Pool> cmbPool = new ComboBox<>(ts.getTranslatedString("module.contractman.contract.label.pool-name"), listPool);
            cmbPool.addValueChangeListener((event) -> {
               if(event.getValue() != null)
                   txtPool.setValue(event.getValue().getClassName());
               else 
                   txtPool.setValue("");
            });
            cmbPool.setAllowCustomValue(false);
            cmbPool.setRequiredIndicatorVisible(true);
            cmbPool.setSizeFull();
            
            //Contract pool selected if exists
            if (selectedPool != null) {
                cmbPool.setValue(selectedPool);
                cmbPool.setEnabled(false);
                txtPool.setValue(selectedPool.getClassName());
            }
            
            TextField txtName = new TextField(ts.getTranslatedString("module.contractman.contract.label.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();

            TextField txtDescription = new TextField(ts.getTranslatedString("module.contractman.contract.label.description"));
            txtDescription.setSizeFull();
            
            wdwNewContract = new Dialog();
            Label lblMessages = new Label();
            
            Button btnOk = new Button(ts.getTranslatedString("module.general.labels.create"), (event) -> {
                try {
                    if (cmbPool.getValue() == null)
                        lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                    else {
                            newContractAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("pool", cmbPool.getValue()),
                                new ModuleActionParameter<>("name", txtName.getValue()),
                                new ModuleActionParameter<>("description", txtDescription.getValue())
                        ));
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.contractman.actions.contract.new-contract-success"), NewContractAction.class));
                        wdwNewContract.close();
                    }
                } catch (ModuleActionException ex) {
                        Logger.getLogger(NewContractVisualAction.class.getName()).log(Level.SEVERE, null, ex);
                  }
            });
            
            btnOk.setEnabled(false);
            txtName.addValueChangeListener((e) -> {
            btnOk.setEnabled(!txtName.isEmpty());
            });
            
            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (event) -> {
            wdwNewContract.close();
            });
            
            FormLayout lytAttributes = new FormLayout(cmbPool, txtPool, txtName, txtDescription);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOk, btnCancel);
            lytMoreButtons.setSpacing(false);
            VerticalLayout lytMain = new VerticalLayout(lytAttributes, lytMoreButtons);
            lytMain.setSizeFull();
            wdwNewContract.add(lytMain);
            
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(NewContractVisualAction.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return wdwNewContract;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newContractAction;
    }
    
}

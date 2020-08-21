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
package org.neotropic.kuwaiba.modules.core.configman.variables.actions;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new configuration variable pool action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewConfigurationVariablesPoolVisualAction extends AbstractVisualAction<Dialog>{   
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewConfigurationVariablesPoolAction newConfigurationVariablesPoolAction;
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
        
            TextField txtName = new TextField(ts.getTranslatedString("module.configvarman.configvar.label.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();
            
            TextField txtDescription = new TextField(ts.getTranslatedString("module.configvarman.configvar.label.description"));
            txtDescription.setSizeFull();
            
            Dialog wdwNewConfigurationVariablePool = new Dialog();
            
             Button btnOK = new Button(ts.getTranslatedString("module.general.labels.create"), (e) -> {
                try {            
                        newConfigurationVariablesPoolAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("name", txtName.getValue()),
                                new ModuleActionParameter<>("description", txtDescription.getValue())
                        ));
                        
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.configvarman.actions.new-configuration-variable-pool.ui.pool-created-success"), NewConfigurationVariablesPoolAction.class));
                        wdwNewConfigurationVariablePool.close();
                    
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), NewConfigurationVariablesPoolAction.class));
                }
            });
            
            btnOK.setEnabled(false);
            txtName.addValueChangeListener((e) -> {
                btnOK.setEnabled(!txtName.isEmpty());
            });
            
            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (e) -> {
                wdwNewConfigurationVariablePool.close();
            });
            
            FormLayout lytTextFields = new FormLayout(txtName, txtDescription);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
            VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
            lytMain.setSizeFull();
            
            wdwNewConfigurationVariablePool.add(lytMain);
            
        return wdwNewConfigurationVariablePool;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newConfigurationVariablesPoolAction;
    }
    
}

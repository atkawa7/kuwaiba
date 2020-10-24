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
package org.neotropic.kuwaiba.core.configuration.variables.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete configuration variables pool action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteConfigurationVariablesPoolVisualAction extends AbstractVisualAction<Dialog>{
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteConfigurationVariablesPoolAction deleteConfigurationVariablesPoolAction;
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
     
        Pool selectedConfigurationVariablePool;
        
        if(parameters.containsKey("configurationVariablePool")){
            selectedConfigurationVariablePool = (Pool) parameters.get("configurationVariablePool");
            
            ConfirmDialog wdwDeleteConfigurationVariablePool = new ConfirmDialog(ts.getTranslatedString("module.general.labels.confirmation"),
                ts.getTranslatedString("module.general.labels.confirm-delete"),
                ts.getTranslatedString("module.general.labels.delete"));
            
            wdwDeleteConfigurationVariablePool.getBtnConfirm().addClickListener((event) -> { 
               try{
                   deleteConfigurationVariablesPoolAction.getCallback().execute(new ModuleActionParameterSet(
                   new ModuleActionParameter<>("poolId", selectedConfigurationVariablePool.getId())));
                   
                   fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                        ts.getTranslatedString("module.configvarman.actions.delete-configuration-variable-pool.ui.pool-deleted-success"), DeleteConfigurationVariablesPoolAction.class));
                wdwDeleteConfigurationVariablePool.close();
               }catch(ModuleActionException ex){
                   fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), DeleteConfigurationVariablesPoolAction.class));
                   wdwDeleteConfigurationVariablePool.close();
               }
            
        });
            return wdwDeleteConfigurationVariablePool; 
      
         }else 
            return new Dialog(new Label(ts.getTranslatedString("module.configvarman.error-param-configuration-variable-pool")));    }

    @Override
    public AbstractAction getModuleAction() {
       return deleteConfigurationVariablesPoolAction;
    }
    
}

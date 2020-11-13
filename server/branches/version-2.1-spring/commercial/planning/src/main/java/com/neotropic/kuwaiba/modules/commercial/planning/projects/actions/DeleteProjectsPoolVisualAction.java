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
package com.neotropic.kuwaiba.modules.commercial.planning.projects.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.Command;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete projects pool action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class DeleteProjectsPoolVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Close action command
     */
    private Command commandClose;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteProjectsPoolAction deleteProjectsPoolAction;
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
       Pool selectedPool;
        if (parameters.containsKey("pool")) {
            selectedPool = (Pool) parameters.get("pool");
            commandClose = (Command) parameters.get("commandClose");
            
            ConfirmDialog wdwDeletePool = new ConfirmDialog(ts.getTranslatedString("module.general.labels.confirmation"),
                    String.format("%s %s?", ts.getTranslatedString("module.projects.actions.pool.delete-pool-confirm"), selectedPool.getName()),
                    ts.getTranslatedString("module.general.labels.delete"));
            wdwDeletePool.setCloseOnOutsideClick(false);
            wdwDeletePool.setCloseOnEsc(false);
            
            wdwDeletePool.getBtnConfirm().addClickListener((event) -> {
                    try{
                        ActionResponse actionResponse =  
                        deleteProjectsPoolAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("pool", selectedPool)));
                        
                        if (actionResponse.containsKey("exception"))
                            throw new ModuleActionException(((Exception)actionResponse.get("exception")).getLocalizedMessage());
                        //refresh related grid
                        getCommandClose().execute(); 
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.projects.actions.pool.delete-pool-success"), DeleteProjectsPoolVisualAction.class)); 
                    } catch (ModuleActionException ex) {
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                        Logger.getLogger(DeleteProjectsPoolVisualAction.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    wdwDeletePool.close();
            });
            return wdwDeletePool;
        } else
            return new Dialog(new Label(ts.getTranslatedString("module.projects.actions.pool.delete-pool-error"))); 
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteProjectsPoolAction;
    }
 
    /**
     * refresh grid
     * @return commandClose;Command; refresh action 
     */
    public Command getCommandClose() {
        return commandClose;
    }

    /**
     * @param commandClose;Command; refresh action 
     */
    public void setCommandClose(Command commandClose) {
        this.commandClose = commandClose;
    }    
}

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
package org.neotropic.kuwaiba.modules.core.templateman.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Delete a template sub item, this item is inside a item of template.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Component
public class DeleteTemplateSubItemVisualAction extends AbstractVisualAction<Dialog> {

    /**
     * Close action command
     */
    private Command commandClose ;
    
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;

    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteTemplateAction deleteTemplateItemAction;

    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        TemplateObjectLight seletedTemplateItem;

        if (parameters.containsKey("templateItem")) {
            seletedTemplateItem = (TemplateObjectLight) parameters.get("templateItem");
            commandClose = (Command) parameters.get("commandClose");

            ConfirmDialog wdwDeleteTemplateItem = new ConfirmDialog(ts.getTranslatedString("module.general.labels.confirmation"),
                    ts.getTranslatedString("module.general.labels.confirmdeletemessage"),
                    ts.getTranslatedString("module.general.labels.delete"));

            wdwDeleteTemplateItem.getBtnConfirm().addClickListener((ev) -> {
                try {
                    deleteTemplateItemAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("className", seletedTemplateItem.getClassName()),
                            new ModuleActionParameter<>("oid", seletedTemplateItem.getId())));

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.templateman.actions.delete-template-subitem.ui.item-created-success"), DeleteTemplateAction.class));
                    wdwDeleteTemplateItem.close();
                    //refresh related grid
                   commandClose.execute();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), DeleteTemplateAction.class));
                    wdwDeleteTemplateItem.close();
                }
            });
            return wdwDeleteTemplateItem;
        } else 
            return new Dialog(new Label(ts.getTranslatedString("module.templateman.error-param-template-subitem")));
        
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteTemplateItemAction;
    }

    /**
     * Receive action from parent layout, in this case refresh grid
     * 
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

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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Delete a template item.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Component
public class NewTemplateVisualAction extends AbstractVisualAction<Dialog> {

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
    private NewTemplateAction newTemplateItemAction;

    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            Label lblDialogName = new Label(ts.getTranslatedString("module.templateman.component.dialog.new-template.description"));
            TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            Button btnOK = new Button();
            Dialog wdwNewListTypeItem = new Dialog();
            HorizontalLayout lytMoreButtons = new HorizontalLayout();
            FormLayout lytTextFields = new FormLayout();
            VerticalLayout lytMain = new VerticalLayout();
            Label lblMessages = new Label();
            //define elements behavior
            lytMain.setSizeFull();
            txtName.setSizeFull();
            txtName.setRequiredIndicatorVisible(true);
            txtName.setValueChangeMode(ValueChangeMode.EAGER);
            btnOK.setText(ts.getTranslatedString("module.general.labels.create"));
            btnOK.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnOK.addClickListener((e) -> {
                try {
                    commandClose = (Command) parameters.get("commandClose");
                    newTemplateItemAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("className", (String) parameters.get("className")),
                            new ModuleActionParameter<>("name", txtName.getValue())
                    ));

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.templateman.actions.new-template-item.ui.item-created-success"), NewTemplateAction.class));
                    wdwNewListTypeItem.close();
                    //refresh related grid
                    commandClose.execute();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), NewTemplateAction.class));
                }
            });

            btnOK.setEnabled(false);
            txtName.addValueChangeListener((e) -> {
                boolean enable = !txtName.isEmpty();
                btnOK.setEnabled(enable);
                if (enable) {
                    btnOK.setClassName("primary-button");
                } else {
                    btnOK.removeClassName("primary-button");
                }
            });

            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (e) -> {
                wdwNewListTypeItem.close();
            });
            lytTextFields.add(txtName);
            lytMoreButtons.add(btnCancel, btnOK);
            lytMain.add(lblDialogName, lytTextFields, lytMoreButtons);

            wdwNewListTypeItem.add(lytMain);
            return wdwNewListTypeItem;
        } catch (Exception ex) {
            fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                    ex.getMessage(), NewTemplateAction.class));
            return new Dialog(new Label(ex.getMessage()));
        }
    }

    @Override
    public AbstractAction getModuleAction() {
        return newTemplateItemAction;
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

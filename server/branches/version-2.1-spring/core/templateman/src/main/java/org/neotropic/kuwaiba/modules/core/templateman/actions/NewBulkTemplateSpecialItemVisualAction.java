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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.Command;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Delete a template item.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Component
public class NewBulkTemplateSpecialItemVisualAction extends AbstractVisualAction<Dialog> {

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
    private NewBulkTemplateSpecialItemAction newBulkTemplateSpecialItemAction;

    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;

    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            Label lblDialogName = new Label(ts.getTranslatedString("module.templateman.component.dialog.new-template-item-multiple.description"));
            TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.pattern-name"));
            Dialog wdwNewListTypeItem = new Dialog();
            Button btnOK = new Button();
            ComboBox<ClassMetadataLight> cbxPossibleSpecialChildren = new ComboBox<>();
            HorizontalLayout lytMoreButtons = new HorizontalLayout();
            FormLayout lytTextFields = new FormLayout();
            VerticalLayout lytMain = new VerticalLayout();
            //define elements behavior
            lytMain.setSizeFull();
            txtName.setSizeFull();
            txtName.setRequiredIndicatorVisible(true);
            txtName.setValueChangeMode(ValueChangeMode.EAGER);
            txtName.setPlaceholder(ts.getTranslatedString("module.templateman.component.txt.special-item.placeholder"));
            btnOK.setEnabled(false);
            btnOK.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            String parentClassName = (String) parameters.get("parentClassName");

            List<ClassMetadataLight> possibleSpecialChildren = mem.getPossibleSpecialChildrenNoRecursive((String) parameters.get("parentClassName"));
            if (!possibleSpecialChildren.isEmpty()) {
                cbxPossibleSpecialChildren.setItems(possibleSpecialChildren);
                cbxPossibleSpecialChildren.setLabel(ts.getTranslatedString("module.templateman.component.cbx.template-item.label"));
                cbxPossibleSpecialChildren.setItemLabelGenerator(element -> !element.getDisplayName().isEmpty() ? element.getDisplayName() : element.getName());
                //validation listeners
                btnOK.setText(ts.getTranslatedString("module.general.labels.create"));
                btnOK.addClickListener(e -> {
                    try {
                        commandClose = (Command) parameters.get("commandClose");
                        newBulkTemplateSpecialItemAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("tsElementclass", cbxPossibleSpecialChildren.getValue().getName()),
                                new ModuleActionParameter<>("tsElementParentClassName", parentClassName),
                                new ModuleActionParameter<>("tsElementParentId", (String) parameters.get("parentId")),
                                new ModuleActionParameter<>("tsElementName", txtName.getValue())
                        ));
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.templateman.actions.new-template-specialitem.multiple.item-created-success"), NewTemplateAction.class));
                        wdwNewListTypeItem.close();
                        //refresh related grid
                        commandClose.execute();
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                                ex.getMessage(), NewTemplateAction.class));
                    }
                });
                txtName.addValueChangeListener((e) -> {
                    boolean enable = !txtName.isEmpty() && cbxPossibleSpecialChildren.getValue() != null;
                    btnOK.setEnabled(enable);
                    if (enable) {
                        btnOK.setClassName("primary-button");
                    } else
                         btnOK.removeClassName("primary-button");                    
                });
                cbxPossibleSpecialChildren.addValueChangeListener((e) -> {
                    boolean enable = !txtName.isEmpty() && cbxPossibleSpecialChildren.getValue() != null;
                    btnOK.setEnabled(enable);
                    if (enable) {
                        btnOK.setClassName("primary-button");
                    } else
                         btnOK.removeClassName("primary-button");
                });
                Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (e) -> {
                    wdwNewListTypeItem.close();
                });
                lytTextFields.add(cbxPossibleSpecialChildren, txtName);
                lytMoreButtons.add(btnCancel, btnOK);
                lytMain.add(lblDialogName, lytTextFields, lytMoreButtons);

            } else {
                Label lblNoClassAvailable = new Label(String.format("The containment configuration does not allow instances of class %s to have children.", parentClassName));
                lytMoreButtons.add(btnOK);
                btnOK.setText(ts.getTranslatedString("module.general.messages.ok"));
                btnOK.setClassName("primary-button");
                btnOK.setEnabled(true);
                btnOK.addClickListener(event -> wdwNewListTypeItem.close());
                lytMain.add(lblDialogName, lblNoClassAvailable, lytMoreButtons);
            }

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
        return newBulkTemplateSpecialItemAction;
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

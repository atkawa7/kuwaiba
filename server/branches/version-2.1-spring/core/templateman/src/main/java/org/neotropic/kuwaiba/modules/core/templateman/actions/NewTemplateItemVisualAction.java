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
public class NewTemplateItemVisualAction extends AbstractVisualAction<Dialog> {

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
    private NewTemplateItemAction newTemplateItemAction;

    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;

    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            Label lblDialogName = new Label(ts.getTranslatedString("module.templateman.actions.addItem-template.description"));
            TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
            Button btnOK = new Button(ts.getTranslatedString("module.general.labels.create"));
            ComboBox<ClassMetadataLight> cbxPossibleChildren = new ComboBox<>();            
            Dialog wdwNewListTypeItem = new Dialog();            
            //define elements behavior
            txtName.setSizeFull();            
            txtName.setRequiredIndicatorVisible(true);
            txtName.setValueChangeMode(ValueChangeMode.EAGER);            
            btnOK.setEnabled(false);
            btnOK.addClickListener(e -> {                
                try {
                    commandClose = (Command) parameters.get("commandClose");
                    newTemplateItemAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("templateElementclass", cbxPossibleChildren.getValue().getName())
                            , new ModuleActionParameter<>("templateElementParentClassName", (String) parameters.get("parentClassName"))
                            , new ModuleActionParameter<>("templateElementParentId", (String) parameters.get("parentId"))
                            , new ModuleActionParameter<>("templateElementName", txtName.getValue())                                
                    ));
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.templateman.actions.new-template-item.ui.item-created-success"), NewTemplateAction.class));
                    wdwNewListTypeItem.close();
                   //refresh related grid
                    getCommandClose().execute();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                            ex.getMessage(), NewTemplateAction.class));
                }
            });
            List<ClassMetadataLight> possibleChildren = mem.getPossibleChildrenNoRecursive((String) parameters.get("parentClassName"));            
            cbxPossibleChildren.setItems(possibleChildren);
            cbxPossibleChildren.setLabel(ts.getTranslatedString("module.templateman.component.cbx.template-item.label"));
            cbxPossibleChildren.setItemLabelGenerator(element-> !element.getDisplayName().isEmpty() ? element.getDisplayName() : element.getName());
            //validation listeners
            txtName.addValueChangeListener((e) -> {
                btnOK.setEnabled(!txtName.isEmpty() && cbxPossibleChildren.getValue() != null );                
            });            
            cbxPossibleChildren.addValueChangeListener((e) -> {
                btnOK.setEnabled(!txtName.isEmpty() && cbxPossibleChildren.getValue() != null );                
            });
            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (e) -> {
                wdwNewListTypeItem.close();
            });

            FormLayout lytTextFields = new FormLayout(cbxPossibleChildren, txtName);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
            VerticalLayout lytMain = new VerticalLayout(lblDialogName, lytTextFields, lytMoreButtons);
            lytMain.setSizeFull();
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

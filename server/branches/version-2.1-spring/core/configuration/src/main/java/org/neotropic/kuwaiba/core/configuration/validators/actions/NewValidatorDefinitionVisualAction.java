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
package org.neotropic.kuwaiba.core.configuration.validators.actions;

import com.neotropic.flow.component.paperdialog.PaperToggleButton;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new validator definition action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewValidatorDefinitionVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewValidatorDefinitionAction newValidatorDefinitionAction;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    protected MetadataEntityManager mem;

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        List<ClassMetadataLight> classes = new ArrayList<>();
        try {
            classes = mem.getSubClassesLight(Constants.CLASS_INVENTORYOBJECT, true, true);
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(NewValidatorDefinitionVisualAction.class.getName()).log(Level.SEVERE, null, ex);
        }

        ComboBox<ClassMetadataLight> cbxClasses = new ComboBox<>(ts.getTranslatedString("module.configman.classes"), classes);
        cbxClasses.setWidthFull();
        cbxClasses.setAllowCustomValue(false);
        cbxClasses.setRequired(true);
        cbxClasses.setRequiredIndicatorVisible(true);

        TextField txtName = new TextField(ts.getTranslatedString("module.configman.validator.label.name"));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();

        TextField txtDescription = new TextField(ts.getTranslatedString("module.configman.validator.label.description"));
        txtDescription.setSizeFull();

        TextArea txtScript = new TextArea(ts.getTranslatedString("module.configman.validator.label.script"));
        txtScript.setSizeFull();
        
        PaperToggleButton btnEnable = new PaperToggleButton(ts.getTranslatedString("module.configman.validator.label.enable"));
        btnEnable.setChecked(true);
        btnEnable.setClassName("green", true);

        // Windows to create a new Validator Definition
        Dialog wdwNewValidatorDefinition = new Dialog();

        // To show errors or warnings related to the input parameters.
        Label lblMessages = new Label();

        Button btnOK = new Button(ts.getTranslatedString("module.general.labels.create"), (e) -> {
            try {
                if (cbxClasses.getValue() == null) 
                    lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                else {
                    newValidatorDefinitionAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("name", txtName.getValue()),
                            new ModuleActionParameter<>("description", txtDescription.getValue()),
                            new ModuleActionParameter<>("classToBeApplied", cbxClasses.getValue().getName()),
                            new ModuleActionParameter<>("script", txtScript.getValue()),
                            new ModuleActionParameter<>("enable", btnEnable.getChecked())
                    ));

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.configman.validator.actions.new-validator.ui.created-success"), NewValidatorDefinitionAction.class));
                    wdwNewValidatorDefinition.close();
                }
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewValidatorDefinitionAction.class));
            }
        });

        btnOK.setEnabled(false);
        txtName.addValueChangeListener((event) -> {
            btnOK.setEnabled(!txtName.isEmpty());
        });

        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (event) -> {
            wdwNewValidatorDefinition.close();
        });

        VerticalLayout lytAttributes = new VerticalLayout(cbxClasses, txtName, txtDescription, btnEnable);
        HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
        lytMoreButtons.setSpacing(false);
        VerticalLayout lytMain = new VerticalLayout(lytAttributes, lytMoreButtons);
        lytMain.setSizeFull();

        wdwNewValidatorDefinition.add(lytMain);
        wdwNewValidatorDefinition.setWidth("50%");
        return wdwNewValidatorDefinition;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newValidatorDefinitionAction;
    }
    
}

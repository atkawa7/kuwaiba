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

import com.neotropic.flow.component.paperdialog.PaperToggleButton;
import java.util.List;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new configuration variable action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewConfigurationVariableVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewConfigurationVariableAction newConfigurationVariableAction;
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

        Pool selectedConfigurationVariablePool = null;

        if (parameters.containsKey("configurationVariable")) {
            selectedConfigurationVariablePool = (Pool) parameters.get("configurationVariable");
        }

        List<Pool> configurationVariablesPool = aem.getConfigurationVariablesPools();

        ComboBox<Pool> cbxConfigurationVariablesPool = new ComboBox<>(ts.getTranslatedString("module.configvarman.configurationvariablespool"), configurationVariablesPool);
        cbxConfigurationVariablesPool.setAllowCustomValue(false);
        cbxConfigurationVariablesPool.setRequiredIndicatorVisible(true);
        cbxConfigurationVariablesPool.setSizeFull();

        // Configuration Variable Pool selected if exists
        if (selectedConfigurationVariablePool != null) {
            cbxConfigurationVariablesPool.setValue(selectedConfigurationVariablePool);
            cbxConfigurationVariablesPool.setEnabled(false);
        }

        TextField txtName = new TextField(ts.getTranslatedString("module.configvarman.configvar.label.name"));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();

        TextField txtDescription = new TextField(ts.getTranslatedString("module.configvarman.configvar.label.description"));
        txtDescription.setSizeFull();

        TextField txtValue = new TextField(ts.getTranslatedString("module.configvarman.configvar.label.value"));
        txtValue.setSizeFull();
                
        PaperToggleButton btnMasked = new PaperToggleButton(ts.getTranslatedString("module.configvarman.configvar.label.masked"));
        btnMasked.setChecked(false);
        btnMasked.setClassName("green", true);

        ComboBox<ConfigurationVariableType> cbxTypes = new ComboBox<>(ts.getTranslatedString("module.configvarman.configvar.label.type"));
        cbxTypes.setItems(new ConfigurationVariableType(ts.getTranslatedString("module.configvarman.configvar.type.integer"), 1),
                new ConfigurationVariableType(ts.getTranslatedString("module.configvarman.configvar.type.string"), 2),
                new ConfigurationVariableType(ts.getTranslatedString("module.configvarman.configvar.type.boolean"), 3),
                new ConfigurationVariableType(ts.getTranslatedString("module.configvarman.configvar.type.array"), 4),
                new ConfigurationVariableType(ts.getTranslatedString("module.configvarman.configvar.type.table"), 5)
        );
        cbxTypes.setValue(new ConfigurationVariableType(ts.getTranslatedString("module.configvarman.configvar.type.integer"), 1));
        cbxTypes.setAllowCustomValue(false);
        cbxTypes.setRequiredIndicatorVisible(false);
        cbxTypes.setSizeFull();
        
        // Window to create a new Configuration Variable Item
        Dialog wdwNewConfigurationVariableItem = new Dialog();

        // To show errors or warnings related to the input parameters.
        Label lblMessages = new Label();

        Button btnOK = new Button(ts.getTranslatedString("module.general.labels.create"), (e) -> {
            try {
                if (cbxConfigurationVariablesPool.getValue() == null) {
                    lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                } else {

                    newConfigurationVariableAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("configVariablesPoolId", cbxConfigurationVariablesPool.getValue().getId()),
                            new ModuleActionParameter<>("name", txtName.getValue()),
                            new ModuleActionParameter<>("description", txtDescription.getValue()),
                            new ModuleActionParameter<>("valueDefinition", txtValue.getValue()),
                            new ModuleActionParameter<>("masked", btnMasked.getChecked()),
                            new ModuleActionParameter<>("type", cbxTypes.getValue().getType())             
                    ));

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.configvarman.actions.new-configuration-variable.ui.created-success"), NewConfigurationVariableAction.class));
                    wdwNewConfigurationVariableItem.close();
                }
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewConfigurationVariableAction.class));
            }
        });

        btnOK.setEnabled(false);
        txtName.addValueChangeListener((e) -> {
            btnOK.setEnabled(!txtName.isEmpty());
        });

        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (e) -> {
            wdwNewConfigurationVariableItem.close();
        });

        FormLayout lytAttributes = new FormLayout(cbxConfigurationVariablesPool, txtName, txtDescription, txtValue, cbxTypes, btnMasked);
        
        HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
        lytMoreButtons.setSpacing(false);
        VerticalLayout lytMain = new VerticalLayout(lytAttributes, lytMoreButtons);
        lytMain.setSizeFull();

        wdwNewConfigurationVariableItem.add(lytMain);

        return wdwNewConfigurationVariableItem;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newConfigurationVariableAction;
    }

    /**
     * Dummy class to be used in the configuration variable type combo box
     */
    private class ConfigurationVariableType {

        private final String displayName;
        private final int type;

        public ConfigurationVariableType(String displayName, int type) {
            this.displayName = displayName;
            this.type = type;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getType() {
            return type;
        }

        @Override
        public String toString() {
            return displayName;
        }

    }

}

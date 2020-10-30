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
package org.neotropic.kuwaiba.modules.optional.taskman.actions;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Task;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new task parameter action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewTaskParameterVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewTaskParameterAction newTaskParameterAction;

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        Task selectedTask = null;
        if (parameters.containsKey("task")) 
            selectedTask = (Task) parameters.get("task");
        
        List<Task> listTask = aem.getTasks();
        ComboBox<Task> cbxTask = new ComboBox<>(ts.getTranslatedString("module.taskman.task.label.name"), listTask);
        cbxTask.setAllowCustomValue(false);
        cbxTask.setItemLabelGenerator(item -> item.getName());
        cbxTask.setReadOnly(true);
        cbxTask.setSizeFull();

        if (selectedTask != null) {
            cbxTask.setValue(selectedTask);
            cbxTask.setEnabled(false);
        }

        TextField txtParameterName = new TextField(ts.getTranslatedString("module.taskman.task.parameters.name"));
        txtParameterName.setRequiredIndicatorVisible(true);
        txtParameterName.setSizeFull();

        TextField txtParameterValue = new TextField(ts.getTranslatedString("module.taskman.task.parameters.value"));
        txtParameterValue.setRequiredIndicatorVisible(true);
        txtParameterValue.setSizeFull();

        // Window to create a new task parameter
        Dialog wdwNewTaskParameter = new Dialog();

        // To show errors or warnings related to the input parameters.
        Label lblMessages = new Label();

        Button btnOK = new Button(ts.getTranslatedString("module.general.labels.create"), (event) -> {
            try {
                if (cbxTask.getValue() == null)
                    lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                else {
                    newTaskParameterAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("taskId", cbxTask.getValue().getId()),
                            new ModuleActionParameter<>("name", txtParameterName.getValue()),
                            new ModuleActionParameter<>("value", txtParameterValue.getValue())
                    ));

                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.taskman.task.actions.new-task-parameter-success"), NewTaskParameterAction.class));
                    wdwNewTaskParameter.close();
                }
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewTaskParameterAction.class));
            }
        });

        btnOK.setEnabled(false);
        txtParameterName.addValueChangeListener((event) -> {
            btnOK.setEnabled(!txtParameterName.isEmpty());
        });

        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (event) -> {
            wdwNewTaskParameter.close();
        });

        FormLayout lytAttributes = new FormLayout(cbxTask, txtParameterName, txtParameterValue);
        HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
        VerticalLayout lytMain = new VerticalLayout(lytAttributes, lytMoreButtons);
        lytMain.setSizeFull();

        wdwNewTaskParameter.add(lytMain);
        return wdwNewTaskParameter;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newTaskParameterAction;
    }
    
}

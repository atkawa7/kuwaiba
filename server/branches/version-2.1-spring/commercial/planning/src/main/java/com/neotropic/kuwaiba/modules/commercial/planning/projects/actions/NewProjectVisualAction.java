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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new project action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewProjectVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewProjectAction newProjectAction;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    private BusinessEntityManager bem;   
    /**
     * Dialog to create new Project
     */
    Dialog wdwNewProject;
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            Pool selectedPool = null;
            if (parameters.containsKey("pool"))
                selectedPool = (Pool) parameters.get("pool");
            
            TextField txtPool = new TextField(ts.getTranslatedString("module.projects.project.label.pool-type"));
            txtPool.setEnabled(false);
            
            List<Pool> listPool = bem.getRootPools(Constants.CLASS_GENERICPROJECT, ApplicationEntityManager.POOL_TYPE_MODULE_COMPONENT, true);
            ComboBox<Pool> cmbPool = new ComboBox<>(ts.getTranslatedString("module.projects.project.label.pool-name"), listPool);
            cmbPool.addValueChangeListener((event) -> {
               if(event.getValue() != null)
                   txtPool.setValue(event.getValue().getClassName());
               else 
                   txtPool.setValue("");
            });
            cmbPool.setAllowCustomValue(false);
            cmbPool.setRequiredIndicatorVisible(true);
            cmbPool.setSizeFull();
            
            //Contract pool selected if exists
            if (selectedPool != null) {
                cmbPool.setValue(selectedPool);
                cmbPool.setEnabled(false);
                txtPool.setValue(selectedPool.getClassName());
            }
            
            TextField txtName = new TextField(ts.getTranslatedString("module.projects.project.label.name"));
            txtName.setSizeFull();

            TextField txtNotes = new TextField(ts.getTranslatedString("module.projects.project.label.notes"));
            txtNotes.setSizeFull();
            
            wdwNewProject = new Dialog();
            Label lblMessages = new Label();
            
            Button btnOk = new Button(ts.getTranslatedString("module.general.labels.create"), (event) -> {
                try {
                    if (cmbPool.getValue() == null)
                        lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                    else {
                            newProjectAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("pool", cmbPool.getValue()),
                                new ModuleActionParameter<>("name", txtName.getValue()), 
                                new ModuleActionParameter<>("notes", txtNotes.getValue())
                        ));
                        wdwNewProject.close();    
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.projects.actions.project.new-project-success"), NewProjectAction.class));
                    }
                } catch (ModuleActionException ex) {
                        Logger.getLogger(NewProjectVisualAction.class.getName()).log(Level.SEVERE, null, ex);
                  }
            });
            
            btnOk.setEnabled(false);
            txtName.addValueChangeListener((e) -> {
            btnOk.setEnabled(!txtName.isEmpty());
            });
            
            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (event) -> {
            wdwNewProject.close();
            });
            
            FormLayout lytAttributes = new FormLayout(cmbPool, txtPool, txtName, txtNotes);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOk, btnCancel);
            lytMoreButtons.setSpacing(false);
            VerticalLayout lytMain = new VerticalLayout(lytAttributes, lytMoreButtons);
            lytMain.setSizeFull();
            wdwNewProject.add(lytMain);
            
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(NewProjectVisualAction.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return wdwNewProject;    
    }

    @Override
    public AbstractAction getModuleAction() {
        return newProjectAction;
    }
    
}

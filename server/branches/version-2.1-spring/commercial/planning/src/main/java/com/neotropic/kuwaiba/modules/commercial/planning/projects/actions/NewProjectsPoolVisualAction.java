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
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new projects pool action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewProjectsPoolVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;    
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewProjectsPoolAction newProjectsPoolAction;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * ComboBox for classes 
     */
    ComboBox cmbClasses;
    /**
     * List of classes
     */
    List<ClassMetadataLight> classes;
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        TextField txtName = new TextField(ts.getTranslatedString("module.projects.pool.label.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();
        TextField txtDescription = new TextField(ts.getTranslatedString("module.projects.pool.label.description"));
            txtDescription.setSizeFull();
        try {
            classes = mem.getSubClassesLight(Constants.CLASS_GENERICPROJECT, true, false);
            cmbClasses = new ComboBox(ts.getTranslatedString("module.projects.pool.label.type"));
            cmbClasses.setItems(classes);
            cmbClasses.setSizeFull();
            cmbClasses.setAllowCustomValue(false);
            cmbClasses.setRequiredIndicatorVisible(true);
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(NewProjectsPoolVisualAction.class.getName()).log(Level.SEVERE, null, ex);
        }
        
            Dialog wdwProjectsPool = new Dialog();
            wdwProjectsPool.setCloseOnOutsideClick(false);
            wdwProjectsPool.setCloseOnEsc(false);
            // To show errors or warnings related to the input parameters.
            Label lblMessages = new Label();
            
            Button btnOk = new Button(ts.getTranslatedString("module.general.labels.create"), (event) -> {
                try {
                    if (cmbClasses.getValue() == null) 
                       lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields")); 
                    else {
                        ClassMetadataLight poolType = (ClassMetadataLight) cmbClasses.getValue();
                        
                        newProjectsPoolAction.getCallback().execute(new ModuleActionParameterSet(
                           new ModuleActionParameter<>("name", txtName.getValue()),
                           new ModuleActionParameter<>("description", txtDescription.getValue()),
                           new ModuleActionParameter<>("class", poolType.getName())
                        ));
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.projects.actions.pool.new-pool-success"), NewProjectsPoolVisualAction.class));
                    wdwProjectsPool.close();    
                    }
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewProjectsPoolVisualAction.class));
            }
            });
            
            btnOk.setEnabled(false);
            txtName.addValueChangeListener((e) -> {
            btnOk.setEnabled(!txtName.isEmpty());
            });
            
            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (event) -> {
            wdwProjectsPool.close();
            });
            
            FormLayout lytAttributes = new FormLayout(txtName, txtDescription, cmbClasses);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOk, btnCancel);
            lytMoreButtons.setSpacing(false);
            VerticalLayout lytMain = new VerticalLayout(lytAttributes, lytMoreButtons);
            lytMain.setSizeFull();
            
            wdwProjectsPool.add(lytMain);
            return wdwProjectsPool;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newProjectsPoolAction;
    }
    
}

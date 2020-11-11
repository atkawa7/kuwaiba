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
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new activity action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewProjectActivityVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewProjectActivityAction newProjectActivityAction;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    private BusinessEntityManager bem;
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
    /**
     * Pool items limit. -1 To return all
     */
    public static final int LIMIT = -1;    
    /**
     * Dialog to create new Project
     */
    Dialog wdwNewActivity;
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        try {
            BusinessObjectLight selectedProject = null;
            Pool selectedPool = null;
       
            if (parameters.containsKey("project"))
                selectedProject = (BusinessObjectLight) parameters.get("project");
            if (parameters.containsKey("pool"))
                selectedPool = (Pool) parameters.get("pool");
            
            List<BusinessObjectLight> listProjects = bem.getPoolItems(selectedPool.getId(), LIMIT);
            ComboBox<BusinessObjectLight> cmbProject = new ComboBox<>(ts.getTranslatedString("module.projects.activity.label.project-name"), listProjects);    
            cmbProject.setAllowCustomValue(false);
            cmbProject.setRequiredIndicatorVisible(true);
            cmbProject.setSizeFull();
         
            //Project selected if exists
            if (selectedProject != null) {
                cmbProject.setValue(selectedProject);
                cmbProject.setEnabled(false);
            }
            
            TextField txtName = new TextField(ts.getTranslatedString("module.projects.activity.label.name"));
            txtName.setSizeFull();
            
                try {
                    classes = mem.getSubClassesLight(Constants.CLASS_GENERICACTIVITY, true, false);
                    cmbClasses = new ComboBox(ts.getTranslatedString("module.projects.activity.label.type"));
                    cmbClasses.setItems(classes);
                    cmbClasses.setSizeFull();
                    cmbClasses.setAllowCustomValue(false);
                    cmbClasses.setRequiredIndicatorVisible(true);
                } catch (MetadataObjectNotFoundException ex) {
                    Logger.getLogger(NewProjectActivityVisualAction.class.getName()).log(Level.SEVERE, null, ex);
                }

            wdwNewActivity = new Dialog();
            Label lblMessages = new Label();
            
            Button btnOk = new Button(ts.getTranslatedString("module.general.labels.create"), (event) -> {
                try {
                    if (cmbProject.getValue() == null)
                        lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                    else {
                            ClassMetadataLight activityType = (ClassMetadataLight) cmbClasses.getValue();
                            
                            newProjectActivityAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter<>("project", cmbProject.getValue()),
                                new ModuleActionParameter<>("name", txtName.getValue()),
                                new ModuleActionParameter<>("class", activityType.getName())    
                        ));
                        fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.projects.actions.activity.new-activity-success"), NewProjectAction.class));
                        wdwNewActivity.close();
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
            wdwNewActivity.close();
            });
            
            FormLayout lytAttributes = new FormLayout(cmbProject, txtName, cmbClasses);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOk, btnCancel);
            lytMoreButtons.setSpacing(false);
            VerticalLayout lytMain = new VerticalLayout(lytAttributes, lytMoreButtons);
            lytMain.setSizeFull();
            wdwNewActivity.add(lytMain);
            
        } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            Logger.getLogger(NewProjectVisualAction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return wdwNewActivity;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newProjectActivityAction;
    }
    
}

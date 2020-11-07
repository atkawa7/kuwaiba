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
package org.neotropic.kuwaiba.modules.commercial.contractman.actions;

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
 * Visual wrapper of create a new contracts pool action.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Component
public class NewContractsPoolVisualAction extends AbstractVisualAction<Dialog> {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;    
    /**
     * Reference to the underlying action.
     */
    @Autowired    
    private NewContractsPoolAction newContractsPoolAction;
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
        TextField txtName = new TextField(ts.getTranslatedString("module.contractman.pool.label.name"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setSizeFull();
        TextField txtDescription = new TextField(ts.getTranslatedString("module.contractman.pool.label.description"));
            txtDescription.setSizeFull();
        try {
            classes = mem.getSubClassesLight(Constants.CLASS_GENERICCONTRACT, false, false);
            cmbClasses = new ComboBox(ts.getTranslatedString("module.contractman.pool.label.type"));
            cmbClasses.setItems(classes);
            cmbClasses.setSizeFull();
            cmbClasses.setAllowCustomValue(false);
            cmbClasses.setRequiredIndicatorVisible(true);
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(NewContractsPoolVisualAction.class.getName()).log(Level.SEVERE, null, ex);
        }
        
            Dialog wdwContractsPool = new Dialog();
            // To show errors or warnings related to the input parameters.
            Label lblMessages = new Label();
            
            Button btnOk = new Button(ts.getTranslatedString("module.general.labels.create"), (event) -> {
                try {
                    if (cmbClasses.getValue() == null) 
                       lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields")); 
                    else {
                        ClassMetadataLight poolType = (ClassMetadataLight) cmbClasses.getValue();
                        
                        newContractsPoolAction.getCallback().execute(new ModuleActionParameterSet(
                           new ModuleActionParameter<>("name", txtName.getValue()),
                           new ModuleActionParameter<>("description", txtDescription.getValue()),
                           new ModuleActionParameter<>("class", poolType.getName())
                        ));
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.contractman.actions.pool.new-pool-success"), NewContractsPoolVisualAction.class));
                    wdwContractsPool.close();    
                    }
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewContractsPoolVisualAction.class));
            }
            });
            
            btnOk.setEnabled(false);
            txtName.addValueChangeListener((e) -> {
            btnOk.setEnabled(!txtName.isEmpty());
            });
            
            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (event) -> {
            wdwContractsPool.close();
            });
            
            FormLayout lytAttributes = new FormLayout(txtName, txtDescription, cmbClasses);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOk, btnCancel);
            lytMoreButtons.setSpacing(false);
            VerticalLayout lytMain = new VerticalLayout(lytAttributes, lytMoreButtons);
            lytMain.setSizeFull();
            
            wdwContractsPool.add(lytMain);
            return wdwContractsPool;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newContractsPoolAction;
    }
    
}
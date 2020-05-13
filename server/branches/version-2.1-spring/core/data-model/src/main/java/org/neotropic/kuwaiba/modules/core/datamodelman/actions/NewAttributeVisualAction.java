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
package org.neotropic.kuwaiba.modules.core.datamodelman.actions;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.integration.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.general.BoldLabel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of create a new Attribute action
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class NewAttributeVisualAction extends AbstractVisualAction<Dialog> {

    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private NewAttributeAction newAttributeAction;
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
        
        ClassMetadataLight seletedClass;
        if (parameters.containsKey("class")) {
            seletedClass = (ClassMetadataLight) parameters.get("class");
        } else
            return null;
        
        BoldLabel lblClass = new BoldLabel("Class :"+ seletedClass.getName());
        TextField txtName = new TextField(ts.getTranslatedString("module.general.labels.name"));
        txtName.setRequiredIndicatorVisible(true);
        txtName.setSizeFull();
        TextField txtDisplayName = new TextField(ts.getTranslatedString("module.general.labels.display-name"));
        List<ClassMetadataLight> listTypes = new ArrayList<>();
        try {
            listTypes = mem.getSubClassesLight(Constants.CLASS_GENERICOBJECTLIST, false, false);
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(NewAttributeVisualAction.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        List<String> lstListTypes = listTypes.stream().map(ClassMetadataLight::getName).collect(Collectors.toList());
        List<String> lstAllTypes  = new ArrayList(Arrays.asList(Constants.DATA_TYPES));
        lstAllTypes.addAll(lstListTypes);
        
        ComboBox cbxType = new ComboBox<>(ts.getTranslatedString("module.general.labels.type"));
        cbxType.setWidthFull();
        cbxType.setItems(lstAllTypes);
        cbxType.setClearButtonVisible(true);
        cbxType.setValue(Constants.DATA_TYPE_STRING);
        cbxType.setAllowCustomValue(false);
        cbxType.setRequired(true);
        cbxType.setRequiredIndicatorVisible(true);
        
        Dialog wdwNewAttribute = new Dialog();
        // To show errors or warnings related to the input parameters.
        Label lblMessages = new Label();
        Button btnOK = new Button(ts.getTranslatedString("module.general.labels.create"), (e) -> {
            try {
                if (txtName.getValue() == null || txtName.getValue().isEmpty()) {
                    lblMessages.setText(ts.getTranslatedString("module.general.messages.must-fill-all-fields"));
                } else {
                    
                    newAttributeAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter<>("className", seletedClass.getName()),
                            new ModuleActionParameter<>("attributeName", txtName.getValue()),
                            new ModuleActionParameter<>("attributeDisplayName", txtDisplayName.getValue()),
                            new ModuleActionParameter<>("attributeType", cbxType.getValue())));

                    
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.datamodelman.actions.new-class-attribute-success"), NewAttributeAction.class));
                    wdwNewAttribute.close();
                }
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), NewAttributeAction.class));
            }
        });
        
        btnOK.setEnabled(false);
        txtName.addValueChangeListener((e) -> {
            btnOK.setEnabled(!txtName.isEmpty());
        });
        
        Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), (e) -> {
            wdwNewAttribute.close();
        });
        
        FormLayout lytTextFields = new FormLayout(lblClass, txtName, txtDisplayName, cbxType);
        HorizontalLayout lytMoreButtons = new HorizontalLayout(btnOK, btnCancel);
        VerticalLayout lytMain = new VerticalLayout(lytTextFields, lytMoreButtons);
        lytMain.setSizeFull();
        wdwNewAttribute.add(lytMain);
        
        return wdwNewAttribute;        
    }
    
    @Override
    public AbstractAction getModuleAction() {
        return newAttributeAction;
    }
}

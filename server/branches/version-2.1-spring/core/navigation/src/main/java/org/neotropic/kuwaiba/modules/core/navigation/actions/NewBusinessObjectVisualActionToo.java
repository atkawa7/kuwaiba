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
package org.neotropic.kuwaiba.modules.core.navigation.actions;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener.ActionCompletedEvent;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of a new business object action.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Component
public class NewBusinessObjectVisualActionToo extends AbstractVisualInventoryAction {
    /**
     * New business object visual action parameter business object.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to module action.
     */
    @Autowired
    private NewBusinessObjectAction newBusinessObjectAction;
    
    
    /**
     * Creates the visual component for new object visual action
     * Label H4 with the title
     * Label with info about the Parent
     * radio buttons with the possible options (single new object,  multiple 
     * objects, new object from template)
     * For single object 
     * List of possible children
     * Mandatory Fields
     * For multiple objects
     * quantity(a mandatory field)
     * optional command field
     * For new object from template
     * available templates
     * @param parameters need it parameters
     * @return a dialog
     */
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        VerticalLayout lytContent =  new VerticalLayout();
        lytContent.setSpacing(true);
        lytContent.setPadding(false);
        lytContent.setMargin(false);
        
        VerticalLayout lytMandatoryAttributes =  new VerticalLayout();
        lytMandatoryAttributes.setSpacing(true);
        lytMandatoryAttributes.setPadding(false);
        lytMandatoryAttributes.setMargin(false);
        
        BusinessObjectLight businessObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
        Dialog wdwNewBusinessObject = new Dialog();
        
        if (businessObject != null) {
            try {
                HorizontalLayout lytHeader = new HorizontalLayout();
                lytContent.setSpacing(true);
                lytContent.setPadding(false);
                lytContent.setMargin(false);
        
                H4 hdnTitle = new H4(newBusinessObjectAction.getDisplayName());
                //wdwNewBusinessObject.setLblHeaderTitle(new H4(newBusinessObjectAction.getDisplayName()));
                Label lblParent = new Label("for: " + businessObject);
                lytHeader.add(hdnTitle, lblParent);
                
                lytContent.add(lblParent);
                
                RadioButtonGroup<String> rdbNewObjtOptions = new RadioButtonGroup<>();
                rdbNewObjtOptions.setRequired(true);
                rdbNewObjtOptions.setItems(ts.getTranslatedString("module.navigation.actions.new-business-object.name"),
                        ts.getTranslatedString("module.navigation.actions.new-multiple-business-object.name"),
                        ts.getTranslatedString("module.navigation.actions.new-business-object-from-template.name"));
                rdbNewObjtOptions.setValue(ts.getTranslatedString("module.navigation.actions.new-business-object.name"));
                lytContent.add(rdbNewObjtOptions);
                
                ComboBox<ClassMetadataLight> cmbPossibleChildrenClass = new ComboBox(ts.getTranslatedString("module.navigation.actions.new-business-object.ui.object-class"));
                cmbPossibleChildrenClass.setItems(mem.getPossibleChildrenNoRecursive(businessObject.getClassName()));
                cmbPossibleChildrenClass.setRequired(true);
                cmbPossibleChildrenClass.setEnabled(true);
                cmbPossibleChildrenClass.setItemLabelGenerator(class_ -> 
                    class_.getDisplayName() != null && !class_.getDisplayName().isEmpty() ? class_.getDisplayName() : class_.getName()
                );
                lytContent.add(cmbPossibleChildrenClass);
                lytContent.add(lytMandatoryAttributes);
                        

                
                
                
                
                //mandatory fields
                TextField txtName = new TextField(ts.getTranslatedString("module.navigation.actions.new-business-object.ui.object-name"));
                txtName.setErrorMessage(ts.getTranslatedString("module.navigation.actions.new-business-object.ui.empty-name"));
                txtName.setClearButtonVisible(true);
                txtName.setValueChangeMode(ValueChangeMode.EAGER);
                txtName.setRequiredIndicatorVisible(true);
                
                
                ComboBox<TemplateObjectLight> cmbTemplate = new ComboBox(ts.getTranslatedString("module.navigation.actions.new-business-object.ui.object-template"));
                cmbTemplate.setEnabled(false);
                cmbTemplate.setItemLabelGenerator(TemplateObjectLight::getName);
                
                FormLayout lytFields = new FormLayout(lblParent, txtName, cmbPossibleChildrenClass, cmbTemplate);
                
                Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), event -> wdwNewBusinessObject.close());
                Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"));
                ShortcutRegistration btnOkShortcut = btnOk.addClickShortcut(Key.ENTER).listenOn(wdwNewBusinessObject);
                
                btnOk.addClickListener(event -> {
                    try {
                        HashMap<String, String> attributes = new HashMap();
                        attributes.put(Constants.PROPERTY_NAME, txtName.getValue());
                        newBusinessObjectAction.getCallback().execute(new ModuleActionParameterSet(
                                new ModuleActionParameter(NewBusinessObjectAction.PARAM_CLASS_NAME, cmbPossibleChildrenClass.getValue().getName()),
                                new ModuleActionParameter(NewBusinessObjectAction.PARAM_PARENT_CLASS_NAME, businessObject.getClassName()),
                                new ModuleActionParameter(NewBusinessObjectAction.PARAM_PARENT_OID, businessObject.getId()),
                                new ModuleActionParameter(NewBusinessObjectAction.PARAM_ATTRIBUTES, attributes),
                                new ModuleActionParameter(NewBusinessObjectAction.PARAM_TEMPLATE_ID,
                                        cmbTemplate.getValue() != null ? cmbTemplate.getValue().getId() : null)
                        ));
                        fireActionCompletedEvent(new ActionCompletedEvent(
                                ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.navigation.actions.new-business-object.ui.success"),
                                NewBusinessObjectAction.class)
                        );
                        wdwNewBusinessObject.close();
                    } catch (ModuleActionException ex) {
                        fireActionCompletedEvent(new ActionCompletedEvent(
                                ActionCompletedEvent.STATUS_ERROR, ex.getMessage(),
                                NewBusinessObjectAction.class));
                    }
                    btnOkShortcut.remove();
                    event.unregisterListener();
                });
                btnOk.setAutofocus(true);
                btnOk.setEnabled(false);
                
                
//                rdbNewObjtOptions.addValueChangeListener(event -> {
//                    event.getValue().equals(event)
//                });
                
                txtName.addValueChangeListener(event -> {
                    if (event.getValue() != null && !event.getValue().isEmpty()) {
                        txtName.setInvalid(false);
                        cmbPossibleChildrenClass.setEnabled(true);
                        if (cmbPossibleChildrenClass.getValue() != null)
                            btnOk.setEnabled(true);
                        else
                            btnOk.setEnabled(false);
                    } else {
                        txtName.setInvalid(true);
                        cmbPossibleChildrenClass.setEnabled(false);
                        btnOk.setEnabled(false);
                    }
                });
                
                
                
                
                //TODO class
                cmbPossibleChildrenClass.addValueChangeListener(event -> {
                    if (event.getValue() != null) {
                        try {//Mandatory attribtues
                            List<AttributeMetadata> mandatoryAttributesInClass = mem.getMandatoryAttributesInClass(event.getValue().getName());
                            lytMandatoryAttributes.removeAll();
                            lytMandatoryAttributes.add(createMandatoryAttributes(mandatoryAttributesInClass));
                            
                            //templates
                            cmbTemplate.setItems(aem.getTemplatesForClass(event.getValue().getName()));
                            cmbTemplate.setEnabled(true);
                            btnOk.setEnabled(true);
                            
                            
                            
                        } catch (InventoryException ex) {
                            lytMandatoryAttributes.removeAll();
                            cmbTemplate.setValue(null);
                            cmbTemplate.setEnabled(false);
                            btnOk.setEnabled(false);
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                            
                        }
                    }
                    else {
                        cmbTemplate.setValue(null);
                        cmbTemplate.setEnabled(false);
                        btnOk.setEnabled(false);
                    }
                });
                
                HorizontalLayout lytButtons = new HorizontalLayout(btnCancel,btnOk);
                
                VerticalLayout lytMain = new VerticalLayout(lytHeader, rdbNewObjtOptions, lytFields, lytButtons);
                lytMain.setHorizontalComponentAlignment(FlexComponent.Alignment.END, lytButtons);
                
                wdwNewBusinessObject.add(lytMain);
            } catch (InventoryException ex) {
                fireActionCompletedEvent(new ActionCompletedEvent(
                    ActionCompletedEvent.STATUS_ERROR, ex.getMessage(), 
                    NewBusinessObjectAction.class)
                );
                wdwNewBusinessObject.add(new Label(ex.getMessage()));
            }
        }
        return wdwNewBusinessObject;
    }

    @Override
    public AbstractAction getModuleAction() {
        return newBusinessObjectAction;
    }

    @Override
    public String appliesTo() {
        return null;
    }
    
    //helpers
    
    private com.vaadin.flow.component.Component createMandatoryAttributes(List<AttributeMetadata> mandatoryAttributesInClass){
        FormLayout lytMandatoryAttributes = new FormLayout();
        mandatoryAttributesInClass.forEach(attr -> {
            if(attr.isMandatory() && AttributeMetadata.isPrimitive(attr.getType())){
                if(attr.getType().equals(String.class)){
                    TextField txtAttr = new TextField(attr.getName());
                    lytMandatoryAttributes.add(txtAttr);
                }
            }
        });
    
        return lytMandatoryAttributes;
    }
}

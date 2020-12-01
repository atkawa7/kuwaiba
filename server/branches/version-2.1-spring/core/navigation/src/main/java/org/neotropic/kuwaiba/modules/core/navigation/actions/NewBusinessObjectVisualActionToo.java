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
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener.ActionCompletedEvent;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
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
     * Attributes for the new business object
     */
    private HashMap<String, String> attributes;
    /**
     * To keep track of the fulfilled mandatory attributes
     */
    private HashMap<String, Boolean> mandatoryAttrtsState;
    /**
     * To keep the mandatory attributes of the selected class
     */
    private List<AttributeMetadata> mandatoryAttributesInSelectedClass;
    /**
     * 
     */
    private Button btnOk;
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
        attributes = new HashMap();
        mandatoryAttrtsState =  new HashMap<>();
        mandatoryAttributesInSelectedClass = new ArrayList<>();
        
        //options for object creation 
        String singleObj = ts.getTranslatedString("module.navigation.actions.new-single-business-object");
        String multipleObj = ts.getTranslatedString("module.navigation.actions.new-multiple-business-object");
        String templateObj = ts.getTranslatedString("module.navigation.actions.new-business-object-from-template");
        
        HorizontalLayout lytExtraFields =  new HorizontalLayout();
        lytExtraFields.setWidth("100%");
        
        BusinessObjectLight businessObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
        Dialog wdwNewBusinessObject = new Dialog();
        wdwNewBusinessObject.setMinWidth("450px");
        
        if (businessObject != null) {
            try {
                VerticalLayout lytHeader = new VerticalLayout();
                lytHeader.setWidth("100%");
                lytHeader.setSpacing(false);
                lytHeader.setPadding(false);
                lytHeader.setMargin(false);
                Label lblTitle = new Label(newBusinessObjectAction.getDisplayName());
                lblTitle.setClassName("dialog_title");
                Label lblParent = new Label(businessObject.toString());
                
                Accordion acrNewObjtOptions = new Accordion();
                acrNewObjtOptions.setWidth("100%");
                
                
                ComboBox<ClassMetadataLight> cmbPossibleChildrenClass = new ComboBox(ts.getTranslatedString("module.navigation.actions.new-business-object.ui.object-class"));
                cmbPossibleChildrenClass.setItems(getChildren(businessObject.getClassName()));
                cmbPossibleChildrenClass.setRequired(true);
                cmbPossibleChildrenClass.setEnabled(true);
                cmbPossibleChildrenClass.setPlaceholder(ts.getTranslatedString("module.navigation.actions.new-business-object.select-class"));
                cmbPossibleChildrenClass.setItemLabelGenerator(class_ -> 
                    class_.getDisplayName() != null && !class_.getDisplayName().isEmpty() ? class_.getDisplayName() : class_.getName()
                );
                //New object name
                TextField txtName = new TextField(ts.getTranslatedString("module.navigation.actions.new-business-object.ui.object-name"));
                txtName.setErrorMessage(ts.getTranslatedString("module.navigation.actions.new-business-object.ui.empty-name"));
                txtName.setClearButtonVisible(true);
                txtName.setValueChangeMode(ValueChangeMode.EAGER);
                txtName.setRequiredIndicatorVisible(true);
                
                //Templantes combo
                ComboBox<TemplateObjectLight> cmbTemplate = new ComboBox(ts.getTranslatedString("module.navigation.actions.new-business-object.ui.object-template"));
                cmbTemplate.setEnabled(false);
                cmbTemplate.setSizeFull();
                cmbTemplate.setItemLabelGenerator(TemplateObjectLight::getName);
                //Multilpe pattern
                TextField txtPattern = new TextField(ts.getTranslatedString("module.navigation.actions.new-multiple-business-object-pattern"));
                txtPattern.setSizeFull();
                txtPattern.setPlaceholder("[sequence(x,y)]...");
                //Mandatory Fields
                FormLayout lytFields = new FormLayout();
                lytFields.add(cmbPossibleChildrenClass);
                lytFields.add(txtName);
                VerticalLayout lytSingleObj = new VerticalLayout(lytFields, lytExtraFields);
                lytSingleObj.setPadding(false);
                lytSingleObj.setMargin(false);
                lytSingleObj.setSpacing(false);
                
                AccordionPanel add = acrNewObjtOptions.add(singleObj, lytSingleObj);
                add.addThemeName("vaadin-accordion-panel");
                AccordionPanel add1 = acrNewObjtOptions.add(multipleObj, txtPattern);
                add1.addThemeName("vaadin-accordion-panel");
                
                Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), event -> wdwNewBusinessObject.close());
                btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"));
                ShortcutRegistration btnOkShortcut = btnOk.addClickShortcut(Key.ENTER).listenOn(wdwNewBusinessObject);
                
                btnOk.addClickListener(event -> {
                    try {
                        attributes.put(Constants.PROPERTY_NAME, txtName.getValue());
                        OptionalInt openedIndex = acrNewObjtOptions.getOpenedIndex();
                        ModuleActionParameterSet params = new ModuleActionParameterSet(
                            new ModuleActionParameter(NewBusinessObjectAction.PARAM_CLASS_NAME, cmbPossibleChildrenClass.getValue().getName()),
                            new ModuleActionParameter(NewBusinessObjectAction.PARAM_PARENT_CLASS_NAME, businessObject.getClassName()),
                            new ModuleActionParameter(NewBusinessObjectAction.PARAM_PARENT_OID, businessObject.getId()));
                        openedIndex.ifPresent(i ->{
                            if(i == 0){
                                cmbTemplate.clear();
                                txtPattern.clear();
                                params.put(NewBusinessObjectAction.PARAM_ATTRIBUTES, attributes);
                            }
                            else if(i == 1){
                                txtPattern.clear();
                                params.put(NewBusinessObjectAction.PARAM_TEMPLATE_ID,
                                        cmbTemplate.getValue() != null ? cmbTemplate.getValue().getId() : null);
                            }else if(i == 2){
                                cmbTemplate.clear();    
                                params.put(NewBusinessObjectAction.PARAM_PATTERN,
                                        txtPattern.getValue());
                            }
                        });
                        ActionResponse actionResponse = new ActionResponse();
                        actionResponse.put(NewBusinessObjectAction.PARAM_PARENT_OID, businessObject.getId());
                        actionResponse.put(NewBusinessObjectAction.PARAM_PARENT_CLASS_NAME, businessObject.getClassName());
                        actionResponse.put(NewBusinessObjectAction.PARAM_CLASS_NAME, businessObject.getClassName());
                        
                        newBusinessObjectAction.getCallback().execute(params);
                        fireActionCompletedEvent(new ActionCompletedEvent(
                                ActionCompletedEvent.STATUS_SUCCESS,
                                ts.getTranslatedString("module.navigation.actions.new-business-object.ui.success"),
                                NewBusinessObjectAction.class, actionResponse)
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
                //To keep the AccordionPanel for templates to remove it when the seletec class has no template
                List<AccordionPanel> acpTemplate = new ArrayList<>();
                cmbPossibleChildrenClass.addValueChangeListener(event -> {
                    if (event.getValue() != null) {
                        try {
                            lytExtraFields.removeAll();
                            btnOk.setEnabled(false);
                            //Templates
                            List<TemplateObjectLight> templatesForSelectedClass = aem.getTemplatesForClass(event.getValue().getName());
                            
                            if(!templatesForSelectedClass.isEmpty()){
                                cmbTemplate.setItems(templatesForSelectedClass);
                                cmbTemplate.setEnabled(true);
                                cmbTemplate.setPlaceholder(ts.getTranslatedString("module.navigation.actions.new-business-object-from-template.select-template"));
                                AccordionPanel add2 = acrNewObjtOptions.add(templateObj, cmbTemplate);
                                add2.addThemeName("vaadin-accordion-panel");
                                acpTemplate.add(add2);
                            }
                            else
                                acrNewObjtOptions.remove(acpTemplate.get(0));
                            //Mandatory attribtues
                            mandatoryAttributesInSelectedClass = mem.getMandatoryAttributesInClass(event.getValue().getName());
                            if(!mandatoryAttributesInSelectedClass.isEmpty())
                                lytExtraFields.add(createMandatoryAttributes(mandatoryAttributesInSelectedClass));
                           
                        } catch (InventoryException ex) {
                            lytExtraFields.removeAll();
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
                
                cmbTemplate.addValueChangeListener(e ->{
                    txtPattern.clear();
                    btnOk.setEnabled(e.getValue()!= null);
                });
                
                txtPattern.addValueChangeListener(e ->{
                    cmbTemplate.clear();
                    btnOk.setEnabled(e.getValue()!= null);
                });
                
                HorizontalLayout lytButtons = new HorizontalLayout(btnCancel, btnOk);
                
                VerticalLayout lytMain = new VerticalLayout(lblTitle, lblParent); 
                lytMain.setHorizontalComponentAlignment(FlexComponent.Alignment.END, lytButtons);
                lytMain.setPadding(true);
                
                lytMain.addAndExpand(acrNewObjtOptions);
                lytMain.add(lytButtons);
                
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
    /**
     * Creates a form layout with the mandatory attributes for new object
     * @param mandatoryAttributesInClass
     * @return 
     */
    private FormLayout createMandatoryAttributes(List<AttributeMetadata> mandatoryAttributesInClass){
        FormLayout lytMandatoryAttributes = new FormLayout();
        mandatoryAttributesInClass.forEach(attr -> {
            mandatoryAttrtsState.put(attr.getName(), false);
            if(attr.isMandatory() && AttributeMetadata.isPrimitive(attr.getType())){
                //String
                if(attr.getType().equals(String.class.getSimpleName())){
                    TextField txtAttr = new TextField(attr.getName());
                    txtAttr.setRequiredIndicatorVisible(true);
                    lytMandatoryAttributes.add(txtAttr);
                    
                    txtAttr.addValueChangeListener(e ->{
                        attributes.put(attr.getName(), e.getValue());
                        mandatoryAttrtsState.put(attr.getName(), true);
                        canBeSave();
                    });
                }//int
                else if(attr.getType().equals(Integer.class.getSimpleName())){
                    IntegerField nbfAttr = new IntegerField(attr.getName());
                    nbfAttr.setHasControls(true);
                    nbfAttr.setStep(1);
                    nbfAttr.setValue(0);
                    nbfAttr.setRequiredIndicatorVisible(true);
                    lytMandatoryAttributes.add(nbfAttr);
                    
                    nbfAttr.addValueChangeListener(e ->{
                        attributes.put(attr.getName(), e.getValue().toString());
                        mandatoryAttrtsState.put(attr.getName(), true);
                        canBeSave();
                    });
                }//float and long
                else if(attr.getType().equals(Float.class.getSimpleName()) || attr.getType().equals(Long.class.getSimpleName())){
                    NumberField nbfAttr = new NumberField(attr.getName());
                    nbfAttr.setValue(0.0);
                    nbfAttr.setRequiredIndicatorVisible(true);
                    lytMandatoryAttributes.add(nbfAttr);
                    
                    nbfAttr.addValueChangeListener(e ->{
                        attributes.put(attr.getName(), e.getValue().toString());
                        mandatoryAttrtsState.put(attr.getName(), true);
                        canBeSave();
                    });
                }//boolean
                else if(attr.getType().equals(Boolean.class.getSimpleName())){
                    Checkbox cbxAttr = new Checkbox(attr.getName());
                    cbxAttr.setRequiredIndicatorVisible(true);
                    lytMandatoryAttributes.add(cbxAttr);
                    
                    cbxAttr.addValueChangeListener(e ->{
                        attributes.put(attr.getName(), e.getValue().toString());
                        mandatoryAttrtsState.put(attr.getName(), true);
                        canBeSave();
                    });
                }//Date
                else if(attr.getType().equals(Date.class.getSimpleName())){
                    DatePicker dtpAttr = new DatePicker(attr.getName());
                    dtpAttr.setRequiredIndicatorVisible(true);
                    lytMandatoryAttributes.add(dtpAttr);
                    
                    dtpAttr.addValueChangeListener(e ->{
                        //attributes.put(attr.getName(), dtpAttr.getValue().to);
                        //mandatoryAttrtsState.put(attr.getName(), true);
                        //canBeSave();
                    });
                }//timesptap
                else if(attr.getType().equals("Timestamp")){
                    TimePicker tmpAttr = new TimePicker(attr.getName());
                    tmpAttr.setRequiredIndicatorVisible(true);
                    lytMandatoryAttributes.add(tmpAttr);

                    tmpAttr.addValueChangeListener(e ->{
                        
                        attributes.put(attr.getName(), Long.toString(e.getValue().toNanoOfDay()));
                        mandatoryAttrtsState.put(attr.getName(), tmpAttr.getValue() != null);
                        canBeSave();
                    });
                }
            }
            else{//ListTypes
                try {
                    List<BusinessObjectLight> listTypeItems = aem.getListTypeItems(attr.getType());
                    ComboBox<BusinessObjectLight> cbxListType = new ComboBox<>(attr.getName());
                    cbxListType.setAllowCustomValue(false);
                    cbxListType.setRequiredIndicatorVisible(true);
                    cbxListType.setItems(listTypeItems);
                    cbxListType.setItemLabelGenerator(listTypeItem -> listTypeItem.getName());
                    lytMandatoryAttributes.add(cbxListType);
                    
                    cbxListType.addValueChangeListener(e ->{
                        attributes.put(attr.getName(), e.getValue().getId());
                        mandatoryAttrtsState.put(attr.getName(), cbxListType.getValue() != null);
                        canBeSave();
                    });
                } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
                    Logger.getLogger(NewBusinessObjectVisualActionToo.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    
        return lytMandatoryAttributes;
    }

    /**
     * Gets the possible children removing the the abstract classes and 
     * adding the children of those abstract classes
     * @param className the class name to get its possible children
     * @return the list of possible children
     * @throws MetadataObjectNotFoundException 
     */
    private List<ClassMetadataLight> getChildren(String className) throws MetadataObjectNotFoundException, InvalidArgumentException {
        List<ClassMetadataLight> possibleChildrenWithoutAbstracts = new ArrayList<>();
        List<ClassMetadataLight> possibleChildrenNoRecursive = mem.getPossibleChildrenNoRecursive(className);
        for (ClassMetadataLight classMetadata : possibleChildrenNoRecursive) {
            if(classMetadata.isAbstract())
                possibleChildrenWithoutAbstracts.addAll(mem.getSubClassesLight(classMetadata.getName(), false, false));
            else
                possibleChildrenWithoutAbstracts.add(classMetadata);
        }
        return possibleChildrenWithoutAbstracts;
    }
    
    /**
     * checks if every mandatory attribute has a value and enables or disables 
     * the ok button
     */
    private void canBeSave(){
        for (Map.Entry<String, Boolean> entry : mandatoryAttrtsState.entrySet()) {
            if(!entry.getValue()){
                btnOk.setEnabled(false);
                return;
            }
        }
        btnOk.setEnabled(true);
    }

    @Override
    public boolean isModuleAction() {
        return false;
    }
}

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
package org.neotropic.kuwaiba.core.configuration.validators;

import com.neotropic.flow.component.aceeditor.AceEditor;
import com.neotropic.flow.component.aceeditor.AceMode;
import com.neotropic.flow.component.paper.toggle.PaperToggleButton;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ValidatorDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.configuration.ConfigurationManagerLayout;
import org.neotropic.kuwaiba.core.configuration.validators.actions.DeleteValidatorDefinitionVisualAction;
import org.neotropic.kuwaiba.core.configuration.validators.actions.NewValidatorDefinitionVisualAction;
import org.neotropic.kuwaiba.core.configuration.validators.nodes.ClassNode;
import org.neotropic.kuwaiba.core.configuration.validators.nodes.ValidatorDefinitionNode;
import org.neotropic.kuwaiba.core.configuration.variables.ConfigurationVariablesUI;
import org.neotropic.kuwaiba.modules.core.navigation.icons.BasicTreeNodeIconGenerator;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.tree.nodes.AbstractNode;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Validator Definition module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "configuration/validators", layout = ConfigurationManagerLayout.class)
public class ValidatorDefinitionUI extends VerticalLayout implements ActionCompletedListener, HasDynamicTitle {
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference o the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Factory to build resources from data source
     */  
    @Autowired
    private ResourceFactory resourceFactory;
    /**
     * The visual action to create a validator definition
     */
    @Autowired
    private NewValidatorDefinitionVisualAction newValidatorDefinitionVisualAction;
    /**
     * The visual action to delete a validator definition
     */
    @Autowired
    private DeleteValidatorDefinitionVisualAction deleteValidatorDefinitionVisualAction;
    /**
     * Button used to delete a validator definition
     */
    Button btnDeleteValidatorDefinition;   
    /**
     * Factory to build resources from data source
     */
    TreeGrid<AbstractNode> objectTree;  
    /**
     * Current selected validator
     */
    ValidatorDefinition currentValidator;
    /**
     * Split the content
     */
    SplitLayout splitLayout;
    /**
     * Combo filter for classes tree
     */
    ComboBox<ClassMetadataLight> cbxFilterTree;
    /**
     * Layout of validator definition form
     */
    VerticalLayout lytValidatorDefinition;

    public ValidatorDefinitionUI() {
        super();
        setSizeFull();
    }

    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);

        try {
            createContent();
        } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
            Logger.getLogger(ConfigurationVariablesUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onDetach(DetachEvent ev) {
        this.newValidatorDefinitionVisualAction.unregisterListener(this);
        this.deleteValidatorDefinitionVisualAction.unregisterListener(this);
    }

    private void createContent() throws InvalidArgumentException, MetadataObjectNotFoundException {
        this.newValidatorDefinitionVisualAction.registerActionCompletedLister(this);
        this.deleteValidatorDefinitionVisualAction.registerActionCompletedLister(this);

        Button btnAddNewValidatorDefinition = new Button(this.newValidatorDefinitionVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
                    this.newValidatorDefinitionVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
                });
        btnAddNewValidatorDefinition.setEnabled(true);
        btnAddNewValidatorDefinition.setClassName("icon-button");

        Command deleteValidator = () -> {
            lytValidatorDefinition.setVisible(false);
            btnDeleteValidatorDefinition.setEnabled(false);
        };
        btnDeleteValidatorDefinition = new Button(this.deleteValidatorDefinitionVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.TRASH),
                (event) -> {
                    this.deleteValidatorDefinitionVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("validatorDefinition", currentValidator),
                            new ModuleActionParameter("commandClose", deleteValidator) 
                    )).open();
                });
        btnDeleteValidatorDefinition.setEnabled(false);
        btnDeleteValidatorDefinition.setClassName("icon-button");

        splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(25);
        initializeValidatorsTree();

        List<ClassMetadataLight> classes = mem.getSubClassesLight(Constants.CLASS_INVENTORYOBJECT, true, true);
        cbxFilterTree = new ComboBox<>();
        cbxFilterTree.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        cbxFilterTree.setWidthFull();
        cbxFilterTree.setItems(classes);
        cbxFilterTree.setClearButtonVisible(true);
        cbxFilterTree.setItemLabelGenerator(ClassMetadataLight::getName);
        cbxFilterTree.setClassName("search");

        cbxFilterTree.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                try {
                    objectTree.setDataProvider(ValidatorHierarchicalDataProvider(Arrays.asList(mem.getClass(event.getValue().getName()))));
                } catch (MetadataObjectNotFoundException ex) {
                    Logger.getLogger(ValidatorDefinitionUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else 
                objectTree.setDataProvider(ValidatorHierarchicalDataProvider(classes));
        });

        VerticalLayout lytInventoryTree = new VerticalLayout(cbxFilterTree, objectTree, btnAddNewValidatorDefinition, btnDeleteValidatorDefinition);
        lytInventoryTree.setPadding(false);
        lytInventoryTree.setSpacing(false);
        lytInventoryTree.setMargin(false);
        lytInventoryTree.setHeightFull();

        splitLayout.addToPrimary(lytInventoryTree);
        add(splitLayout);
    }

    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
                objectTree.getDataProvider().refreshAll();
            } catch (UnsupportedOperationException ex) {
                Logger.getLogger(ConfigurationVariablesUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
    }

    private void initializeValidatorsTree() throws MetadataObjectNotFoundException, InvalidArgumentException {
        HierarchicalDataProvider dataProvider = ValidatorHierarchicalDataProvider(mem.getSubClassesLight(Constants.CLASS_INVENTORYOBJECT, true, true));
        objectTree = new ValidatorDefinitionTree(dataProvider, new BasicTreeNodeIconGenerator(resourceFactory));
        objectTree.addItemClickListener(event -> {
            if (event.getItem() instanceof ValidatorDefinitionNode) {
                try {
                    currentValidator = (ValidatorDefinition) event.getItem().getObject();
                    ValidatorDefinitionNode selectedTreeNode = (ValidatorDefinitionNode) event.getItem();
                    ValidatorDefinitionForm validatorForm = new ValidatorDefinitionForm(selectedTreeNode.getObject());
                    splitLayout.addToSecondary(validatorForm);
                    btnDeleteValidatorDefinition.setEnabled(true);
                } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                    Logger.getLogger(ValidatorDefinitionUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    private HierarchicalDataProvider ValidatorHierarchicalDataProvider(List<ClassMetadataLight> classes) {
        return new AbstractBackEndHierarchicalDataProvider() {
            @Override
            protected Stream fetchChildrenFromBackEnd(HierarchicalQuery hq) {
                if (hq.getParent() == null) {
                    List<ClassNode> classesNodes = new ArrayList();
                    classes.forEach(aClass -> classesNodes.add(new ClassNode(aClass)));
                    return classesNodes.stream();
                }

                if (hq.getParent() instanceof ClassNode) {
                    ClassNode classNode = (ClassNode) hq.getParent();
                    List<ValidatorDefinition> validators = aem.getValidatorDefinitionsForClass(classNode.getObject().getName());
                    List<ValidatorDefinitionNode> validatorNodes = new ArrayList();
                    validators.forEach(aValidator -> validatorNodes.add(new ValidatorDefinitionNode(aValidator)));
                    return validatorNodes.stream();
                } else 
                    return Collections.EMPTY_SET.stream();
            }

            @Override
            public int getChildCount(HierarchicalQuery hq) {
                if (hq.getParent() == null) {
                    return classes.size();
                }
                if (hq.getParent() instanceof ClassNode) {
                    ClassNode classNode = (ClassNode) hq.getParent();
                    return aem.getValidatorDefinitionsForClass(classNode.getObject().getName()).size();
                } else 
                    return 0;
            }

            @Override
            public boolean hasChildren(Object t) {
                return t instanceof ClassNode;
            }
        };
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.configman.validator.title");
    }

    private class ValidatorDefinitionForm extends VerticalLayout {
        public ValidatorDefinitionForm(ValidatorDefinition validator) throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
            setMargin(false);
            setPadding(false);
            setSpacing(false);
            
            H4 headerMain = new H4(String.format("%s %s", validator.getName(), ts.getTranslatedString("module.configman.validator.header-name")));
            headerMain.setClassName("header");
            
            Label lblScript = new Label(ts.getTranslatedString("module.configman.validator.label.script"));
            lblScript.addClassName("bold-font");
            AceEditor editorScript = new AceEditor();
            editorScript.setMode(AceMode.groovy);
            editorScript.setValue(validator.getScript());
            editorScript.addAceEditorValueChangedListener(event -> { 
                validator.setScript(editorScript.getValue());
            });
            
            Button btnSave = new Button(ts.getTranslatedString("module.configman.validator.properties-general.button-save"), new Icon(VaadinIcon.DOWNLOAD));
            btnSave.setClassName("icon-button");
            btnSave.setAutofocus(true);
            btnSave.addClickListener(event -> {
                try {
                    aem.updateValidatorDefinition(validator.getId(), validator.getName(), validator.getDescription(), validator.getClassToBeApplied(), editorScript.getValue(), validator.isEnabled());
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.configman.validator.properties-script.notification-saved"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                    objectTree.getDataProvider().refreshAll();
                } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                    Logger.getLogger(ValidatorDefinitionUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            
            Button btnEditProperties = new Button(ts.getTranslatedString("module.configman.validator.properties-general.button-edit-properties"), new Icon(VaadinIcon.EDIT));
            btnEditProperties.setClassName("icon-button");
            btnEditProperties.addClickListener(event -> {
                UpdatePropertiesDialog scriptDialog = new UpdatePropertiesDialog(validator);
                add(scriptDialog);
            });
               
            HorizontalLayout lytHeader = new HorizontalLayout(headerMain);
            lytHeader.setWidthFull();
            lytHeader.setPadding(false);
            lytHeader.setMargin(false);
            lytHeader.setSpacing(false);
            HorizontalLayout lytButton = new HorizontalLayout(btnEditProperties, btnSave);
            lytButton.setAlignItems(Alignment.END);
            lytButton.setSpacing(false);
            lytButton.setMargin(false);
            lytButton.setPadding(false);
            HorizontalLayout lytHeaderMain = new HorizontalLayout(lytHeader, lytButton);
            lytHeaderMain.setWidthFull();
            lytHeaderMain.setPadding(false);
            lytHeaderMain.setMargin(false);
            VerticalLayout lytScript = new VerticalLayout(lblScript, editorScript);
            lytScript.setHeightFull();
            lytScript.setMargin(false);
            lytScript.setSpacing(false);
            
            lytValidatorDefinition = new VerticalLayout(lytHeaderMain, lytScript);
            lytValidatorDefinition.setHeightFull();
            lytValidatorDefinition.setMargin(false);
            lytValidatorDefinition.setPadding(false);
            lytValidatorDefinition.setSpacing(false);
            add(lytValidatorDefinition);
        }
    }
    private class UpdatePropertiesDialog extends Dialog {
        private UpdatePropertiesDialog(ValidatorDefinition validator) {
            H4 header = new H4(ts.getTranslatedString("module.configman.validator.header-properties"));
            
            TextField txtName = new TextField(ts.getTranslatedString("module.configman.validator.label.name"),
                    validator.getName(), ts.getTranslatedString("module.configman.validator.label.name.info"));
            txtName.setRequiredIndicatorVisible(true);
            txtName.setWidthFull();
            txtName.addValueChangeListener(event -> {
                validator.setName(txtName.getValue());
            });
            
            TextField txtDescription = new TextField(ts.getTranslatedString("module.configman.validator.label.description"),
                    validator.getDescription() == null ? "" : validator.getDescription(), ts.getTranslatedString("module.configman.validator.label.description.info"));
            txtDescription.setWidthFull();
            txtDescription.addValueChangeListener(event -> {
               validator.setDescription(txtDescription.getValue());
            });
                        
            PaperToggleButton btnEnable = new PaperToggleButton(ts.getTranslatedString("module.configman.validator.label.enable"));
            btnEnable.setChecked(validator.isEnabled());
            btnEnable.setClassName("green", true);
            btnEnable.addValueChangeListener(event -> {
               validator.setEnabled(event.getValue());
            });
            
            // Windows to update validator properties
            Dialog wdwUpdateProperties = new Dialog();
            
            Button btnSave = new Button(ts.getTranslatedString("module.configman.validator.properties-general.button-save"), new Icon(VaadinIcon.DOWNLOAD));
            btnSave.setAutofocus(true);
            btnSave.addClickListener(event -> {
                try {
                    aem.updateValidatorDefinition(validator.getId(), txtName.getValue(), txtDescription.getValue(), validator.getClassToBeApplied(), validator.getScript(), validator.isEnabled());
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.configman.validator.properties-general.notification-saved"), 
                            AbstractNotification.NotificationType.INFO, ts).open();
                    objectTree.getDataProvider().refreshAll();
                    wdwUpdateProperties.close();
                } catch (ApplicationObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
                    Logger.getLogger(ValidatorDefinitionUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            btnSave.setClassName("icon-button");
            
            Button btnCancel = new Button(ts.getTranslatedString("module.configman.validator.properties-general.button-cancel"), new Icon(VaadinIcon.CLOSE_SMALL));
            btnCancel.addClickListener(event -> {
                        wdwUpdateProperties.close();
            });
            btnCancel.setClassName("icon-button");
            
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnSave, btnCancel);
            lytMoreButtons.setSpacing(false);
            VerticalLayout lytProperties = new VerticalLayout(header, txtName, txtDescription, btnEnable);
            lytProperties.setMargin(false);
            lytProperties.setSizeFull();
            lytProperties.setHeight("90%");
            VerticalLayout lytMain = new VerticalLayout(lytProperties, lytMoreButtons);
            lytMain.setMargin(false);
            lytMain.setSizeFull();
            lytMain.setHeightFull();
            
            wdwUpdateProperties.add(lytMain);
            wdwUpdateProperties.setWidth("50%");
            wdwUpdateProperties.setHeightFull();
            wdwUpdateProperties.open();
        }
    }  
}

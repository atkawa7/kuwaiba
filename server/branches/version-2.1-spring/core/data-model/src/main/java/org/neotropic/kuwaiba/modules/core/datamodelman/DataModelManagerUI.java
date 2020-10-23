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

package org.neotropic.kuwaiba.modules.core.datamodelman;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.neotropic.kuwaiba.modules.core.datamodelman.nodes.DataModelNode;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener.ActionCompletedEvent;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.datamodelman.actions.DeleteAttributeVisualAction;
import org.neotropic.kuwaiba.modules.core.datamodelman.actions.DeleteClassVisualAction;
import org.neotropic.kuwaiba.modules.core.datamodelman.actions.NewAttributeVisualAction;
import org.neotropic.kuwaiba.modules.core.datamodelman.actions.NewClassVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.icons.BasicIconGenerator;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.kuwaiba.modules.core.navigation.properties.PropertyFactory;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.tree.BasicTree;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Data Model manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
@Route(value = "dmman", layout = DataModelManagerLayout.class)
public class DataModelManagerUI extends VerticalLayout implements HasDynamicTitle{
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * factory to build resources from data source
     */  
    @Autowired
    private ResourceFactory resourceFactory;
    /**
     * the visual action to create a new class
     */
    @Autowired
    private NewClassVisualAction newClassVisualAction;
    /**
     * the visual action to delete a class
     */
    @Autowired
    private DeleteClassVisualAction deleteClassVisualAction;
    /**
     * the visual action to create a new attribute
     */
    @Autowired
    private NewAttributeVisualAction newAttributeVisualAction;
   /**
     * the visual action to delete  attribute
     */
    @Autowired
    private DeleteAttributeVisualAction deleteAttributeVisualAction;
     /**
     * factory to build resources from data source
     */ 
    TreeGrid<DataModelNode> inventoryObjectTree;
     /**
     * factory to build resources from data source
     */ 
    TreeGrid<DataModelNode> genericObjectListTree;       
    /**
     * sheet for general Attributes
     */     
    PropertySheet propsheetGeneralAttributes;
     /**
     * sheet for class Attributes properties
     */   
    PropertySheet propsheetClassAttributes;
     /**
     * current selected class 
     */   
    ClassMetadataLight selectedClass;
    
    DataModelNode selectedTreeNode;
     /**
     * combo filter for inventory tree
     */   
    ComboBox<ClassMetadataLight> cbxFilterInventoryTree;
     /**
     * combo filter for list type tree
     */   
    ComboBox<ClassMetadataLight> cbxFilterListTypeTree;
     /**
     * grid to list class attributes
     */   
    Grid<AttributeMetadata> tblClassAttributes;
     /**
     * current selected class attribute
     */   
    AttributeMetadata selectedAttribute;
    /**
     * upload control to class icon
     */
    Upload uploadIcon;
    /**
     * upload control to small class icon
     */
    Upload uploadSmallIcon;
    /**
     * icon class image
     */
    Image iconImage;
    /**
     * upload control to small class icon
     */        
    Image smallIconImage;  
    /**
     * layout to show class attributes property sheet
     */
    VerticalLayout lytPropSheetClassAttributes;
    /**
     * contains class icons
     */
    VerticalLayout lytIcons ;
    Button btnDeleteAttribute;
    Button btnDeleteClass;
    /**
     * listener to attribute actions
     */
    ActionCompletedListener listenerAttributeActions;
    /**
     * listener to delete class action
     */
    ActionCompletedListener listenerDeleteClassAction;
    /**
     * listener to new class action
     */
    ActionCompletedListener listenerNewClassAction;
    
    public DataModelManagerUI() {
        super();
        setSizeFull();
    }
    
    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        try {
            createContent();
        } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
            
        }
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
         this.newClassVisualAction.unregisterListener(listenerNewClassAction);
         this.deleteClassVisualAction.unregisterListener(listenerDeleteClassAction);
         this.newAttributeVisualAction.unregisterListener(listenerAttributeActions);
         this.deleteAttributeVisualAction.unregisterListener(listenerAttributeActions);
    }
    
    public void showActionCompledMessages(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();                                          
            } catch (Exception ex) {
                Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
 
    }

    private void createContent() throws InvalidArgumentException, MetadataObjectNotFoundException {
         
        listenerNewClassAction = (ActionCompletedEvent ev) -> {          
                genericObjectListTree.getDataProvider().refreshAll();
                inventoryObjectTree.getDataProvider().refreshAll();                            
                showActionCompledMessages(ev);      
        }; 
        this.newClassVisualAction.registerActionCompletedLister(listenerNewClassAction);
        
        listenerDeleteClassAction = (ActionCompletedEvent ev) -> {
           
                    genericObjectListTree.getDataProvider().refreshAll();
                    inventoryObjectTree.getDataProvider().refreshAll();
                    //clear general attributes section
                    propsheetGeneralAttributes.clear();
                    lytIcons.setVisible(false);
                    btnDeleteClass.setEnabled(false);         
                    
                    selectedClass = null;
                    selectedAttribute = null;
                    selectedTreeNode = null;
                    //clear class attributes section
                    updateGridClassAttributes(selectedClass);
                    propsheetClassAttributes.clear();
                    btnDeleteAttribute.setEnabled(false);         
                    
                    showActionCompledMessages(ev);            
        }; 
        this.deleteClassVisualAction.registerActionCompletedLister(listenerDeleteClassAction);      
        
        listenerAttributeActions = (ActionCompletedEvent ev) -> {
           
                    updateGridClassAttributes(selectedClass);
                    selectedAttribute = null;
                    propsheetClassAttributes.clear();
                    btnDeleteAttribute.setEnabled(false);                   
                    showActionCompledMessages(ev);            
        };       
        this.newAttributeVisualAction.registerActionCompletedLister(listenerAttributeActions);
        this.deleteAttributeVisualAction.registerActionCompletedLister(listenerAttributeActions);
        
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(25); 
               
        initializeInventoryObjectTree();
        initializeGenericObjectListTree(); 
         
        Accordion accordion = new Accordion();
        accordion.setSizeFull();
        
        BoldLabel lblInventoryTree = new BoldLabel(ts.getTranslatedString("module.datamodelman.inventory-classes"));
        lblInventoryTree.addClassName("lbl-accordion");
        HorizontalLayout lytSummaryInventoryTree = new HorizontalLayout(lblInventoryTree); 
        lytSummaryInventoryTree.setWidthFull();
        VerticalLayout lytInventoryTree = new VerticalLayout(cbxFilterInventoryTree, inventoryObjectTree);
        lytInventoryTree.setPadding(false);
        lytInventoryTree.setSpacing(false);
        AccordionPanel apInventoryTree = new AccordionPanel(lytSummaryInventoryTree, lytInventoryTree);
        accordion.add(apInventoryTree);
               
        BoldLabel lblListType = new BoldLabel(ts.getTranslatedString("module.datamodelman.list-types"));
        lblListType.addClassName("lbl-accordion");
        HorizontalLayout lytSummaryListType = new HorizontalLayout(lblListType);  
        lytSummaryListType.setWidthFull();        
        VerticalLayout lytListType = new VerticalLayout(cbxFilterListTypeTree, genericObjectListTree);
        lytListType.setPadding(false);
        lytListType.setSpacing(false);
        AccordionPanel apListType = new AccordionPanel(lytSummaryListType, lytListType);
        accordion.add(apListType);
        
        Button btnNewClass = new Button(this.newClassVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
                    if (selectedClass != null) {
                        this.newClassVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("parentClass", selectedClass))).open();
                    } else 
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.datamodelman.messages.class-unselected")).open();
                });
        btnDeleteClass = new Button(this.deleteClassVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.TRASH),
                (event) -> {
                    if (selectedClass != null) {
                        this.deleteClassVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("class", selectedClass))).open();
                    } else 
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.datamodelman.messages.class-unselected")).open();
                });
        btnDeleteClass.setEnabled(false);
        HorizontalLayout lylActions = new HorizontalLayout(btnNewClass, btnDeleteClass);
        
        accordion.close();       
        VerticalLayout lytTrees = new VerticalLayout(new H4(ts.getTranslatedString("module.datamodelman.classes")), 
                                               lylActions, accordion);
        lytTrees.setPadding(false);
        lytTrees.setSizeFull();
        splitLayout.addToPrimary(lytTrees);
        
        initializePropSheetGenericAttributes(); 
        initializeGridClassAttributes();
        initializePropSheetClassAttributes(); 
        initializeIconUploaders();
        
        BoldLabel lblIcon = new BoldLabel(ts.getTranslatedString("module.datamodelman.icon"));
        lblIcon.setClassName("lbl-icon-dmman");
        Div divIcon = new Div(iconImage);
       
        divIcon.setClassName("div-icon-dmman");
        HorizontalLayout lytClassIcon = new HorizontalLayout(lblIcon, divIcon, uploadIcon);
        lytClassIcon.setSpacing(true);
        lytClassIcon.setAlignItems(Alignment.CENTER);
        
        BoldLabel lblSmallIcon = new BoldLabel(ts.getTranslatedString("module.datamodelman.smallicon"));
        lblSmallIcon.setClassName("lbl-icon-dmman");
        Div divSmallIcon = new Div(smallIconImage);
        divSmallIcon.setClassName("div-icon-dmman");
        HorizontalLayout lytSmallClassIcon = new HorizontalLayout(lblSmallIcon, divSmallIcon, uploadSmallIcon);
        lytSmallClassIcon.setSpacing(true);
        lytSmallClassIcon.setAlignItems(Alignment.CENTER);
        
        Hr divisor = new Hr();
        divisor.setClassName("width100p");
        BoldLabel lblInfoFile = new BoldLabel(String.format("%s.     %s: %s bytes",
                ts.getTranslatedString("module.datamodelman.accepted-icon-file-types"), 
                ts.getTranslatedString("module.datamodelman.max-size"),
                Constants.MAX_ICON_SIZE_IN_BYTES));
        lblInfoFile.setClassName("text-secondary");
        HorizontalLayout lytUploadIcons = new HorizontalLayout( lytClassIcon, lytSmallClassIcon);
        lytIcons = new VerticalLayout(new H4(ts.getTranslatedString("module.datamodelman.icons")), 
                                      lblInfoFile, lytUploadIcons);  
//        lytIcons.setWidth("60%");
        lytIcons.setVisible(false);
        VerticalLayout lytGeneralAttributes = new VerticalLayout(propsheetGeneralAttributes);
        
        VerticalLayout lytAttributes = new VerticalLayout();
        lytAttributes.add(lytGeneralAttributes, lytIcons);
        lytAttributes.setSizeFull();
        lytAttributes.setSpacing(false);
        lytAttributes.setPadding(false);
        
        Button btnNewAttribute = new Button(this.newAttributeVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
                    if (selectedClass != null) {
                        this.newAttributeVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("class", selectedClass))).open();
                    } else 
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.datamodelman.messages.class-unselected")).open();
                });
        btnDeleteAttribute = new Button(this.deleteAttributeVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.TRASH),
                (event) -> {
                    if (selectedClass != null && selectedAttribute != null) {
                        this.deleteAttributeVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("class", selectedClass),
                        new ModuleActionParameter("attribute", selectedAttribute))).open();
                    } else 
                        new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), ts.getTranslatedString("module.datamodelman.messages.class-unselected")).open();
                });
        btnDeleteAttribute.setEnabled(false);
        VerticalLayout lytListClassAttributes = new VerticalLayout(new HorizontalLayout(btnNewAttribute, btnDeleteAttribute),
                                                           tblClassAttributes);
        lytPropSheetClassAttributes = new VerticalLayout(new H4(ts.getTranslatedString("module.datamodelman.attributes")), propsheetClassAttributes);
        lytPropSheetClassAttributes.setSpacing(false);
        lytPropSheetClassAttributes.setVisible(false);
        HorizontalLayout lytClassAttributes = new HorizontalLayout(lytListClassAttributes, lytPropSheetClassAttributes);
        lytClassAttributes.setSizeFull();
        
        Tab tab1 = new Tab(ts.getTranslatedString("module.datamodelman.properties"));
        Div page1 = new Div();
        page1.setSizeFull();
        page1.add(lytAttributes);

        Tab tab2 = new Tab(ts.getTranslatedString("module.datamodelman.class-attributes"));
        Div page2 = new Div();
        page2.add(lytClassAttributes);
        page2.setVisible(false);
        
        Map<Tab, Component> tabsToPages = new HashMap<>();
        tabsToPages.put(tab1, page1);
        tabsToPages.put(tab2, page2);
        Tabs tabs = new Tabs(tab1, tab2);
        Div pages = new Div(page1, page2);
        pages.setWidthFull();
        Set<Component> pagesShown = Stream.of(page1)
                .collect(Collectors.toSet());

        tabs.addSelectedChangeListener(event -> {
            pagesShown.forEach(page -> page.setVisible(false));
            pagesShown.clear();
            Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
            selectedPage.setVisible(true);
            pagesShown.add(selectedPage);
        });
              
        splitLayout.addToSecondary(new VerticalLayout(tabs, pages));
        
        add(splitLayout);
    }

    private void initializeInventoryObjectTree() {
        HierarchicalDataProvider dataProvider = buildHierarchicalDataProvider(Constants.CLASS_INVENTORYOBJECT);
        
        inventoryObjectTree = new BasicTree(dataProvider , new BasicIconGenerator(resourceFactory));
        
        List<ClassMetadataLight> inventoryObjectClasses = new ArrayList<>();
        try {
            inventoryObjectClasses = mem.getSubClassesLight(Constants.CLASS_INVENTORYOBJECT, true, true);
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        cbxFilterInventoryTree = new ComboBox<>(ts.getTranslatedString("module.general.labels.filter"));
        cbxFilterInventoryTree.setWidthFull();
        cbxFilterInventoryTree.setItems(inventoryObjectClasses);
        cbxFilterInventoryTree.setClearButtonVisible(true);
        cbxFilterInventoryTree.setItemLabelGenerator(ClassMetadataLight::getName);
        
        cbxFilterInventoryTree.addValueChangeListener(ev -> {
            if (ev.getValue() == null) 
                inventoryObjectTree.setDataProvider(buildHierarchicalDataProvider(Constants.CLASS_INVENTORYOBJECT));
            else 
                inventoryObjectTree.setDataProvider(buildHierarchicalDataProvider(ev.getValue().getName()));
           
        });
        
        inventoryObjectTree.addItemClickListener( item ->  {
            selectedClass = item.getItem().getObject();
            selectedTreeNode = item.getItem();
            updatePropertySheetGeneralAttributes();
            updateGridClassAttributes(item.getItem().getObject());
            updateIconImages();
            propsheetClassAttributes.clear();
            lytPropSheetClassAttributes.setVisible(false);
            lytIcons.setVisible(true);
            btnDeleteAttribute.setEnabled(false);
            btnDeleteClass.setEnabled(true);
        });
    }
    
    private void updatePropertySheetGeneralAttributes() {
        try {
            ClassMetadata classMetadata = mem.getClass(selectedClass.getName());
            
            propsheetGeneralAttributes.setItems(PropertyFactory.generalPropertiesFromClass(classMetadata));
        } catch (MetadataObjectNotFoundException ex) {
            Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void updatePropertySheetClassAttributes() {
        try {           
            propsheetClassAttributes.setItems(PropertyFactory.generalPropertiesFromAttribute(selectedAttribute));
        } catch (Exception ex) {
            Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void initializeGenericObjectListTree() {
        HierarchicalDataProvider dataProvider = buildHierarchicalDataProvider(Constants.CLASS_GENERICOBJECTLIST);
        
        genericObjectListTree = new BasicTree(dataProvider , new BasicIconGenerator(resourceFactory));
        genericObjectListTree.addItemClickListener( item ->  {
            selectedClass = item.getItem().getObject();
            updatePropertySheetGeneralAttributes();
            updateGridClassAttributes(item.getItem().getObject());
            updateIconImages();
            propsheetClassAttributes.clear();
            lytPropSheetClassAttributes.setVisible(false);
            lytIcons.setVisible(true);
            btnDeleteAttribute.setEnabled(false);
            btnDeleteClass.setEnabled(true);
        });
        
        List<ClassMetadataLight> listTypeClasses = new ArrayList<>();
        try {       
            listTypeClasses = mem.getSubClassesLight(Constants.CLASS_GENERICOBJECTLIST, true, true);
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        cbxFilterListTypeTree = new ComboBox<>(ts.getTranslatedString("module.general.labels.filter"));
        cbxFilterListTypeTree.setWidthFull();
        cbxFilterListTypeTree.setItems(listTypeClasses);
        cbxFilterListTypeTree.setClearButtonVisible(true);
        cbxFilterListTypeTree.setItemLabelGenerator(ClassMetadataLight::getName);
        
        cbxFilterListTypeTree.addValueChangeListener(ev -> {
            if (ev.getValue() == null) 
                genericObjectListTree.setDataProvider(buildHierarchicalDataProvider(Constants.CLASS_GENERICOBJECTLIST));
            else 
                genericObjectListTree.setDataProvider(buildHierarchicalDataProvider(ev.getValue().getName()));
           
        });
    }

    private void initializePropSheetGenericAttributes() {
        propsheetGeneralAttributes = new PropertySheet(ts, new ArrayList<>(), "");
        propsheetGeneralAttributes.addPropertyValueChangedListener((AbstractProperty<? extends Object> property) -> {
            try {
                if (selectedClass != null) { 

                    ClassMetadata classToUpdate = new ClassMetadata();
                    classToUpdate.setId(selectedClass.getId());
                    switch(property.getName()) {
                        case Constants.PROPERTY_NAME:
                            classToUpdate.setName(property.getValue().toString());
                            selectedClass.setName(property.getValue().toString());
                            break;
                        case Constants.PROPERTY_DISPLAY_NAME:
                            classToUpdate.setDisplayName(property.getValue().toString());
                            selectedClass.setDisplayName(property.getValue().toString());
                            break;
                        case Constants.PROPERTY_DESCRIPTION:
                            classToUpdate.setDescription(property.getValue().toString());
                            break;
                        case Constants.PROPERTY_ABSTRACT:
                            classToUpdate.setAbstract((Boolean)property.getValue());
                            selectedClass.setAbstract((Boolean)property.getValue());
                            break;
                        case Constants.PROPERTY_IN_DESIGN:
                            classToUpdate.setInDesign((Boolean)property.getValue());
                            selectedClass.setInDesign((Boolean)property.getValue());
                            break;
                        case Constants.PROPERTY_COUNTABLE:
                            classToUpdate.setCountable((Boolean)property.getValue());
                            break; 
                        case Constants.PROPERTY_COLOR:
                            int color = Color.decode((String) property.getValue()).getRGB();
                            classToUpdate.setColor(color);
                            selectedClass.setColor(color);
                            break;  
                    }                                     
                    mem.setClassProperties(classToUpdate);   

                    genericObjectListTree.getDataProvider().refreshAll();
                    inventoryObjectTree.getDataProvider().refreshAll();
                    updatePropertySheetGeneralAttributes();
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update")).open();
                }
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException
                    | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });     
    }


    private void initializeGridClassAttributes() {
        
        tblClassAttributes = new Grid();
        tblClassAttributes.addThemeVariants(GridVariant.LUMO_COMPACT);
        tblClassAttributes.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);
        tblClassAttributes.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        tblClassAttributes.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        tblClassAttributes.addColumn(AttributeMetadata::getName)
                .setHeader(ts.getTranslatedString("module.general.labels.attribute-name"))
                .setKey(ts.getTranslatedString("module.general.labels.name"));
        
        tblClassAttributes.addItemClickListener(ev -> {
            try {
                selectedAttribute = ev.getItem();
                updatePropertySheetClassAttributes();
                lytPropSheetClassAttributes.setVisible(true);
                btnDeleteAttribute.setEnabled(true);
            } catch (Exception ex) {

            }
        });
    }

    private void updateGridClassAttributes(ClassMetadataLight object) {
        
        try {
            if (object != null) {
            ClassMetadata classMetadata = mem.getClass(object.getName());
            tblClassAttributes.setItems(classMetadata.getAttributes());
            tblClassAttributes.getDataProvider().refreshAll();
            } else
                tblClassAttributes.setItems(new ArrayList<>());
        } catch (MetadataObjectNotFoundException ex) {
            Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initializePropSheetClassAttributes() {
        propsheetClassAttributes = new PropertySheet(ts, new ArrayList<>(), "");
        propsheetClassAttributes.addPropertyValueChangedListener((AbstractProperty<? extends Object> property) -> {
             try {
                if (selectedAttribute != null && selectedClass != null) {
                    
                    AttributeMetadata attributeMetadataToUpdate = new AttributeMetadata();
                    attributeMetadataToUpdate.setId(selectedAttribute.getId());
                    attributeMetadataToUpdate.setName(null);
                    
                    switch(property.getName()) {
                        case Constants.PROPERTY_NAME:
                            attributeMetadataToUpdate.setName(property.getValue().toString());
                            break;
                        case Constants.PROPERTY_DISPLAY_NAME:
                            attributeMetadataToUpdate.setDisplayName(property.getValue().toString());
                            break;
                        case Constants.PROPERTY_DESCRIPTION:
                            attributeMetadataToUpdate.setDescription(property.getValue().toString());
                            break;
                        case Constants.PROPERTY_MANDATORY:
                            attributeMetadataToUpdate.setMandatory((Boolean)property.getValue());
                            break;
                        case Constants.PROPERTY_UNIQUE:
                            attributeMetadataToUpdate.setUnique((Boolean)property.getValue());
                            break;
                        case Constants.PROPERTY_MULTIPLE:
                            attributeMetadataToUpdate.setMultiple((Boolean)property.getValue());
                            break;                     
                        case Constants.PROPERTY_VISIBLE:
                            attributeMetadataToUpdate.setVisible((Boolean)property.getValue());
                            break;                     
                        case Constants.PROPERTY_ADMINISTRATIVE:
                            attributeMetadataToUpdate.setAdministrative((Boolean)property.getValue());
                            break;                     
                        case Constants.PROPERTY_NO_COPY:
                            attributeMetadataToUpdate.setNoCopy((Boolean)property.getValue());
                            break;                     
                        case Constants.PROPERTY_ORDER:
                            attributeMetadataToUpdate.setOrder((Integer)property.getValue());
                            break;                     
                    }
                                      
                    mem.setAttributeProperties(selectedClass.getId(), attributeMetadataToUpdate);
                    // Refresh Objects
                    selectedAttribute = mem.getAttribute(selectedClass.getId(), selectedAttribute.getId());
                    updateGridClassAttributes(selectedClass);
                    // Update Property Sheet
                    updatePropertySheetClassAttributes();                  
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update")).open();
                }
            }catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException
                    | InvalidArgumentException ex) {
                new SimpleNotification("", ex.getMessage()).open();
                Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });     
    }
    
    public HierarchicalDataProvider buildHierarchicalDataProvider(String rootClass) {
        return new AbstractBackEndHierarchicalDataProvider<DataModelNode, Void>() {
            @Override
            protected Stream<DataModelNode> fetchChildrenFromBackEnd(HierarchicalQuery<DataModelNode, Void> query) {
                DataModelNode parent = query.getParent();
                if (parent != null) {
                    ClassMetadataLight object = parent.getObject();
                    try {
                        List<ClassMetadataLight> children = mem.getSubClassesLightNoRecursive(object.getName(), true, false);
                        List<DataModelNode> theChildren = new ArrayList();
                        for (ClassMetadataLight child : children)
                            theChildren.add(new DataModelNode(child, child.getName()));
                        return theChildren.stream();
                    } catch (MetadataObjectNotFoundException ex) {
                        Notification.show(ex.getMessage());
                        return new ArrayList().stream();
                    }
                } else {
                    try {
                        ClassMetadata inventoryObjectClass = mem.getClass(rootClass);
                        return Arrays.asList(new DataModelNode(
                                new ClassMetadataLight(inventoryObjectClass.getId(),
                                        inventoryObjectClass.getName(),
                                        inventoryObjectClass.getDisplayName()), inventoryObjectClass.getName())).stream();
                    } catch (MetadataObjectNotFoundException ex) {
                        Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                        return new ArrayList().stream();
                    }
                }              
            }

            @Override
            public int getChildCount(HierarchicalQuery<DataModelNode, Void> query) {
                DataModelNode parent = query.getParent();
                if (parent != null) {
                    ClassMetadataLight object = parent.getObject();
                    try {
                        return (int) mem.getSubClassesCount(object.getName());
                    } catch (MetadataObjectNotFoundException ex) {
                        Notification.show(ex.getMessage());
                        return 0;
                    }
                    
                } else
                    return 1;
            }

            @Override
            public boolean hasChildren(DataModelNode node) {
                return true;
            }
        };
    }

    private void initializeIconUploaders() {
          
        iconImage = new Image();
        iconImage.setWidth(Constants.DEFAULT_ICON_SIZE);
        iconImage.setHeight(Constants.DEFAULT_ICON_SIZE);
        
        smallIconImage = new Image();
        smallIconImage.setWidth(Constants.DEFAULT_SMALL_ICON_SIZE);
        smallIconImage.setHeight(Constants.DEFAULT_SMALL_ICON_SIZE);
        
        MemoryBuffer bufferIcon = new MemoryBuffer();
        uploadIcon = new Upload(bufferIcon);
        uploadIcon.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        uploadIcon.setMaxFiles(1);
        uploadIcon.setDropLabel(new Label(ts.getTranslatedString("module.datamodelman.dropmessage")));
        uploadIcon.setMaxFileSize(Constants.MAX_ICON_SIZE_IN_BYTES);
        uploadIcon.addSucceededListener(event -> {
            try {
                byte [] imageData = IOUtils.toByteArray(bufferIcon.getInputStream());
                
                if (selectedClass != null) { 

                    ClassMetadata classToUpdate = new ClassMetadata();
                    classToUpdate.setId(selectedClass.getId());
                    
                    classToUpdate.setIcon(imageData);
                    
                    mem.setClassProperties(classToUpdate);
                    genericObjectListTree.getDataProvider().refreshAll();
                    inventoryObjectTree.getDataProvider().refreshAll();
                    
                    StreamResource resource = new StreamResource("icon", () -> bufferIcon.getInputStream());
                    iconImage.setSrc(resource);

                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update")).open();
                }
            } catch (IOException | ApplicationObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException ex) {
                Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }); 
        
        uploadIcon.addFileRejectedListener(listener -> {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                       listener.getErrorMessage()).open();             
            }
        );
        
        //small icon
        
        MemoryBuffer bufferSmallIcon = new MemoryBuffer();
        uploadSmallIcon = new Upload(bufferSmallIcon);
        uploadSmallIcon.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        uploadSmallIcon.setMaxFiles(1);
        uploadSmallIcon.setDropLabel(new Label(ts.getTranslatedString("module.datamodelman.dropmessage")));
        uploadSmallIcon.setMaxFileSize(Constants.MAX_ICON_SIZE_IN_BYTES);
        
        uploadSmallIcon.addSucceededListener(event -> {
            try {
                byte [] imageData = IOUtils.toByteArray(bufferSmallIcon.getInputStream());
                
                if (selectedClass != null) { 

                    ClassMetadata classToUpdate = new ClassMetadata();
                    classToUpdate.setId(selectedClass.getId());
                    
                    classToUpdate.setSmallIcon(imageData);
                    
                    mem.setClassProperties(classToUpdate);  
                    genericObjectListTree.getDataProvider().refreshAll();
                    inventoryObjectTree.getDataProvider().refreshAll();
                    
                    StreamResource resource = new StreamResource("icon", () -> bufferSmallIcon.getInputStream());
                    smallIconImage.setSrc(resource);

                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update")).open();
                }
            } catch (IOException | ApplicationObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException ex) {
                Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }); 
        
        uploadSmallIcon.addFileRejectedListener(listener -> {
                     new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                       listener.getErrorMessage()).open();             
            }
        );
        
    }

    private void updateIconImages() {
        try {
            ClassMetadata classMetadata = mem.getClass(selectedClass.getName());
            byte[] iconBytes = classMetadata.getIcon();
            if (iconBytes.length > 0) {
                StreamResource resource = new StreamResource("icon.jpg", () -> new ByteArrayInputStream(iconBytes));
                iconImage.setSrc(resource); // Icon 32X32
            } else {
                iconImage.setSrc("img/no_image.png");
            }
            //small icon
            byte[] smallIconBytes = classMetadata.getSmallIcon();
            if (smallIconBytes.length > 0) {
                StreamResource resource = new StreamResource("Small icon.jpg", () -> new ByteArrayInputStream(smallIconBytes));
                smallIconImage.setSrc(resource); // "Small Icon 16X16");
            } else {
                smallIconImage.setSrc("img/no_image.png");
            }
        } catch (MetadataObjectNotFoundException ex) {
            Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.listtypeman.title");
    }
}

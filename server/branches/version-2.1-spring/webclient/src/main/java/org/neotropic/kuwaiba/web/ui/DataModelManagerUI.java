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

package org.neotropic.kuwaiba.web.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.modules.core.datamodelman.nodes.DataModelNode;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
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
import org.neotropic.kuwaiba.modules.core.navigation.icons.BasicIconGenerator;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertyFactory;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.tree.BasicTree;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Data Model manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
@Route(value = "dmman", layout = MainLayout.class)
public class DataModelManagerUI extends VerticalLayout implements ActionCompletedListener {


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
    
    public DataModelManagerUI() {
        super();
        setSizeFull();
    }
    
    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        getUI().ifPresent( ui -> ui.getPage().setTitle(ts.getTranslatedString("module.listtypeman.title")));      

        try {
            createContent();
        } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
            
        }
        
    }
    
    @Override
    public void onDetach(DetachEvent ev) {

    }
    
    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCESS) {
            try {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
                                            
            } catch (Exception ex) {
                Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }

    private void createContent() throws InvalidArgumentException, MetadataObjectNotFoundException {
                
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(25); 
               
        initializeInventoryObjectTree();
        initializeGenericObjectListTree(); 
         
        Accordion accordion = new Accordion();
        accordion.setSizeFull();
        accordion.add(ts.getTranslatedString("module.datamodelman.inventory-classes"), 
                new VerticalLayout(cbxFilterInventoryTree, inventoryObjectTree));
        accordion.add(ts.getTranslatedString("module.datamodelman.list-types"), 
                new VerticalLayout(cbxFilterListTypeTree, genericObjectListTree));
        accordion.close();
                
        VerticalLayout lytTrees = new VerticalLayout(new H4(ts.getTranslatedString("module.datamodelman.classes")), accordion);
        lytTrees.setPadding(false);
        lytTrees.setSizeFull();
        splitLayout.addToPrimary(lytTrees);
        
        initializePropSheetGenericAttributes(); 
        initializeGridClassAttributes();
        initializePropSheetClassAttributes(); 
        VerticalLayout lytLeft = new VerticalLayout(new H4(ts.getTranslatedString("module.datamodelman.general-class-attributes")),
                                    propsheetGeneralAttributes, new H4(ts.getTranslatedString("module.datamodelman.class-attributes")),
                                    tblClassAttributes);
        VerticalLayout lytRight = new VerticalLayout(new H4(ts.getTranslatedString("module.datamodelman.general-attributes")), propsheetClassAttributes);
        HorizontalLayout lytSecundaryContent = new HorizontalLayout(lytLeft, lytRight);
        lytSecundaryContent.setSizeFull();
        
        splitLayout.addToSecondary(lytSecundaryContent);
        
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
            updatePropertySheetGeneralAttributes();
            updateGridClassAttributes(item.getItem().getObject());
            propsheetClassAttributes.clear();
        });
    }
    
    private void updatePropertySheetGeneralAttributes() {
        try {
            ClassMetadata classMetadata = mem.getClass(selectedClass.getName());
            
            propsheetGeneralAttributes.setItems(PropertyFactory.generalPropertiesFromClass(classMetadata));
        } catch (MetadataObjectNotFoundException ex) {
            Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void updatePropertySheetClassAttributes() {
        try {           
            propsheetClassAttributes.setItems(PropertyFactory.generalPropertiesFromAttribute(selectedAttribute));
        } catch (Exception ex) {
            Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void initializeGenericObjectListTree() {
        HierarchicalDataProvider dataProvider = buildHierarchicalDataProvider(Constants.CLASS_GENERICOBJECTLIST);
        
        genericObjectListTree = new BasicTree(dataProvider , new BasicIconGenerator(resourceFactory));
//        genericObjectListTree.addco
        genericObjectListTree.addItemClickListener( item ->  {
            selectedClass = item.getItem().getObject();
            updatePropertySheetGeneralAttributes();
            updateGridClassAttributes(item.getItem().getObject());
            propsheetClassAttributes.clear();
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
                            break;
                        case Constants.PROPERTY_DISPLAY_NAME:
                            classToUpdate.setDisplayName(property.getValue().toString());
                            break;
                        case Constants.PROPERTY_DESCRIPTION:
                            classToUpdate.setDescription(property.getValue().toString());
                            break;
                        case Constants.PROPERTY_ABSTRACT:
                            classToUpdate.setAbstract((Boolean)property.getValue());
                            break;
                        case Constants.PROPERTY_IN_DESIGN:
                            classToUpdate.setInDesign((Boolean)property.getValue());
                            break;
                        case Constants.PROPERTY_COUNTABLE:
                            classToUpdate.setCountable((Boolean)property.getValue());
                            break;                     
                    }                                     
                    mem.setClassProperties(classToUpdate);                   

                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update")).open();
                }
            }catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException
                    | InvalidArgumentException ex) {
                Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ApplicationObjectNotFoundException ex) {
                Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });     
    }


    private void initializeGridClassAttributes() {
        
        tblClassAttributes = new Grid();
             
        tblClassAttributes.addColumn(AttributeMetadata::getName)
                .setHeader(String.format("%s %s", ts.getTranslatedString("module.listtypeman.listtype"), ts.getTranslatedString("module.general.labels.name")))
                .setKey(ts.getTranslatedString("module.general.labels.name"));
        
        tblClassAttributes.addItemClickListener(ev -> {
            try {
                selectedAttribute = ev.getItem();
                updatePropertySheetClassAttributes();
            } catch (Exception ex) {

            }
        });
    }

    private void updateGridClassAttributes(ClassMetadataLight object) {
        
        try {
            ClassMetadata classMetadata = mem.getClass(object.getName());
            tblClassAttributes.setItems(classMetadata.getAttributes());
            tblClassAttributes.getDataProvider().refreshAll();
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
                            attributeMetadataToUpdate.setVisible((Boolean)property.getValue());
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

                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update")).open();
                }
            }catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException
                    | InvalidArgumentException ex) {
                Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
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
}

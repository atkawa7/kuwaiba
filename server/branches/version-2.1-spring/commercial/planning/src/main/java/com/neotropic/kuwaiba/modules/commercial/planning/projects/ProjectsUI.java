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
package com.neotropic.kuwaiba.modules.commercial.planning.projects;

import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.DeleteProjectActivityVisualAction;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.DeleteProjectVisualAction;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.DeleteProjectsPoolVisualAction;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.NewProjectActivityVisualAction;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.NewProjectVisualAction;
import com.neotropic.kuwaiba.modules.commercial.planning.projects.actions.NewProjectsPoolVisualAction;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.properties.PropertyFactory;
import org.neotropic.kuwaiba.modules.core.navigation.properties.PropertyValueConverter;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Projects Module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "planning/projects", layout = ProjectsLayout.class) 
public class ProjectsUI extends VerticalLayout implements ActionCompletedListener,
        PropertySheet.IPropertyValueChangedListener, HasDynamicTitle {
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Business Entity Manager .
     */
    @Autowired
    private BusinessEntityManager bem;    
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
     * Layout of projects pool
     */
    VerticalLayout lytPools;
    /**
     * Layout of projects 
     */
    VerticalLayout lytProjects;
    /**
     * The grid with the list pools
     */
    private final Grid<Pool> tblPools;
    /**
     * The grid with the list projects
     */
    private final Grid<BusinessObjectLight> tblProjects;
    /**
     * Pool items limit. -1 To return all
     */
    public static final int LIMIT = -1;
    /**
     * Object to save the selected pool
     */
    private Pool currentPool;
    /**
     * Object to save the selected project
     */
    private BusinessObjectLight currentProject;
    /**
     * Object to save the selected activity
     */
    private BusinessObjectLight currentActivity;
    /**
     * The visual action to create a new projects pool
     */
    @Autowired
    private NewProjectsPoolVisualAction newProjectsPoolVisualAction;
    /**
     * Button used to create a new pool
     */
    Button btnAddPool;
    /**
     * The visual action to delete a projects pool
     */
    @Autowired
    private DeleteProjectsPoolVisualAction deleteProjectsPoolVisualAction;
    /**
     * Button used to delete a pool
     */
    Button btnDeletePool;    
    /**
     * The visual action to create a new project
     */
    @Autowired
    private NewProjectVisualAction newProjectVisualAction;
    /**
     * Button used to create a new project
     */
    Button btnAddProject;
    /**
     * Button used to create a new project with the pool preselected
     */    
    Button btnAddProjectSec;
    /**
     * The visual action to delete a project
     */
    @Autowired
    private DeleteProjectVisualAction deleteProjectVisualAction;
    /**
     * The visual action to create a new activity
     */
    @Autowired
    private NewProjectActivityVisualAction newProjectActivityVisualAction;
    /**
     * Button used to create a new activity
     */
    Button btnAddActivity; 
    /**
     * The visual action to delete an activity
     */
    @Autowired
    private DeleteProjectActivityVisualAction deleteProjectActivityVisualAction;
    /**
     * Button used to delete an activity
     */
    Button btnDeleteActivity;
    /**
     * Object to save pool name preselected
     */
    H4 headerPoolName;
    /**
     * Layout of property sheet
     */
    VerticalLayout lytPropertySheet;
    VerticalLayout lytActivityPropertySheet;
    PropertySheet propertySheet;
    PropertySheet activityPropertySheet;
    /**
     * Boolean used to update properties 
     */
    Boolean isPool;
    Boolean isProject;
    /**
     * Command used to refresh activities grid 
     */
    Command commandRefresh;
    
    public ProjectsUI() {
        super();
        setSizeFull();
        tblPools = new Grid<>();
        tblProjects = new Grid<>();
    }
    
    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            if (currentPool != null) {
                loadPools();
                loadProjects(currentPool);
            } else
                loadPools();
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open(); 
    }
    
    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);
        getUI().ifPresent(ui -> ui.getPage().setTitle(ts.getTranslatedString("module.projects.title")));

        createContent();
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.newProjectsPoolVisualAction.unregisterListener(this);
        this.newProjectVisualAction.unregisterListener(this);
        this.newProjectActivityVisualAction.unregisterListener(this);
        this.deleteProjectVisualAction.unregisterListener(this);
        this.deleteProjectsPoolVisualAction.unregisterListener(this);
        this.deleteProjectActivityVisualAction.unregisterListener(this);
    }
    
    private void createContent() {
        HorizontalLayout lytMainContent = new HorizontalLayout();
        lytMainContent.setSizeFull();
        lytMainContent.setMargin(false);
        lytMainContent.setPadding(false); 
        
        this.newProjectsPoolVisualAction.registerActionCompletedLister(this);
        this.newProjectVisualAction.registerActionCompletedLister(this);
        this.newProjectActivityVisualAction.registerActionCompletedLister(this);
        this.deleteProjectVisualAction.registerActionCompletedLister(this);
        this.deleteProjectsPoolVisualAction.registerActionCompletedLister(this);
        this.deleteProjectActivityVisualAction.registerActionCompletedLister(this);
        
        btnAddPool = new Button(this.newProjectsPoolVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
           (event) -> {
               this.newProjectsPoolVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
           });
        btnAddPool.getElement().setProperty("title", this.newProjectsPoolVisualAction.getModuleAction().getDescription());
        btnAddPool.setClassName("icon-button");
        
        Command deletePool = () -> {
          currentPool = null;
          currentProject = null;
          lytPropertySheet.setVisible(false);
          lytProjects.setVisible(false);
          btnDeletePool.setEnabled(false);
          tblPools.getDataProvider().refreshAll();
        };
        btnDeletePool = new Button(this.deleteProjectsPoolVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.TRASH),
           (event) -> {
                this.deleteProjectsPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("pool", currentPool),
                        new ModuleActionParameter("commandClose", deletePool)
                )).open();
           });
        btnDeletePool.getElement().setProperty("title", this.deleteProjectsPoolVisualAction.getModuleAction().getDescription());
        btnDeletePool.setEnabled(false);
        btnDeletePool.setClassName("icon-button");
        
        btnAddProject = new Button(this.newProjectVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
           (event) -> {
              this.newProjectVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
           });
        btnAddProject.getElement().setProperty("title", this.newProjectVisualAction.getModuleAction().getDescription());
        btnAddProject.setClassName("icon-button");
        
        btnAddProjectSec = new Button(this.newProjectVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
           (event) -> {
              this.newProjectVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("pool", currentPool))).open();
           });
        btnAddProjectSec.getElement().setProperty("title", this.newProjectVisualAction.getModuleAction().getDescription());
        btnAddProjectSec.setClassName("icon-button");
        btnAddProjectSec.setEnabled(false);
        
        btnAddActivity = new Button(this.newProjectActivityVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
           (event) -> {
              this.newProjectActivityVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("pool", currentPool))).open();
           });
        btnAddActivity.getElement().setProperty("title", this.newProjectActivityVisualAction.getModuleAction().getDescription());
        btnAddActivity.setClassName("icon-button");
                
        buildPoolGrid();
        H4 headerPools = new H4(ts.getTranslatedString("module.projects.pool.header"));
        headerPools.setClassName("header-position");
        lytPools = new VerticalLayout(headerPools, tblPools, btnAddProject, btnAddPool, btnDeletePool);
        lytPools.setWidth("25%");
        lytPools.setPadding(false);
        lytPools.setMargin(false);
        lytPools.setSpacing(false);
        
        buildProjectsGrid();
        headerPoolName = new H4();
        headerPoolName.setClassName("header-position");
        H4 headerProject = new H4(ts.getTranslatedString("module.projects.project.header"));
        headerProject.setClassName("header-position");
        HorizontalLayout lytHeaders = new HorizontalLayout(headerProject, headerPoolName);
        lytHeaders.setClassName("header-layout-position");
        lytHeaders.setMargin(false);
        lytHeaders.setPadding(false);
        HorizontalLayout lytButtons = new HorizontalLayout(btnAddProjectSec, btnAddActivity);
        lytButtons.setSpacing(false);
        lytButtons.setClassName("project-button-position");
        lytProjects = new VerticalLayout(lytHeaders, lytButtons, tblProjects);
        lytProjects.setPadding(false);
        lytProjects.setMargin(false);
        lytProjects.setSpacing(false);
        lytProjects.setWidth("30%");
        lytProjects.setHeightFull();
        lytProjects.setVisible(false);
        
        propertySheet = new PropertySheet(ts, new ArrayList<>(), "");
        propertySheet.addPropertyValueChangedListener(this);
        H4 headerPropertySheet = new H4(ts.getTranslatedString("module.propertysheet.labels.header"));
        headerPropertySheet.setClassName("header");
        lytPropertySheet = new VerticalLayout(headerPropertySheet, propertySheet);
        lytPropertySheet.setWidth("45%");
        lytPropertySheet.setVisible(false);
        lytPropertySheet.setMargin(false);
        lytPropertySheet.setPadding(false);
        lytPropertySheet.setSpacing(false);
                
        lytMainContent.add(lytPools, lytProjects, lytPropertySheet);
        add(lytMainContent);
    }
    
    private void loadPools() {
        try {
            List<Pool> listPool = bem.getRootPools(Constants.CLASS_GENERICPROJECT, ApplicationEntityManager.POOL_TYPE_MODULE_COMPONENT, true);
            tblPools.setItems(listPool);
            tblPools.getDataProvider().refreshAll();
            if (listPool.isEmpty())
                btnAddProject.setEnabled(false);
            else
                btnAddProject.setEnabled(true);
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(ProjectsUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void buildPoolGrid () { 
        try {
            List<Pool> listPool = bem.getRootPools(Constants.CLASS_GENERICPROJECT, ApplicationEntityManager.POOL_TYPE_MODULE_COMPONENT, true);
            ListDataProvider<Pool> dataProvider = new ListDataProvider<>(listPool);
            tblPools.setDataProvider(dataProvider);
            tblPools.setHeightFull();
            tblPools.addColumn(Pool::getName)
                    .setKey(ts.getTranslatedString("module.general.labels.name"));
            if(listPool.isEmpty())
                btnAddProject.setEnabled(false);
            else
                btnAddProject.setEnabled(true);
            tblPools.addItemClickListener(event -> {
                btnDeletePool.setEnabled(true);
                lytProjects.setVisible(true);
                btnAddProjectSec.setEnabled(true);
                currentPool = event.getItem();
                loadProjects(currentPool);
                headerPoolName.setText(currentPool.getName());
                updatePropertySheet(currentPool);
                lytPropertySheet.setVisible(true);
            });
            
            // Filter Pool by Name
            HeaderRow filterRow = tblPools.appendHeaderRow();
            TextField txtPoolName = createTxtFieldPoolName(dataProvider);
            filterRow.getCell(tblPools.getColumnByKey(ts.getTranslatedString("module.general.labels.name"))).setComponent(txtPoolName);
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(ProjectsUI.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
    /**
     * create a new input field to pool in the header row
     * @param dataProvider data provider to filter
     * @return the new input field filter
     */
    private TextField createTxtFieldPoolName(ListDataProvider<Pool> dataProvider) {
        Icon iconSearch = VaadinIcon.SEARCH.create();
        iconSearch.getElement().setProperty("title", ts.getTranslatedString("module.projects.pool.filter"));
        iconSearch.setSize("16px");
        
        TextField txtPoolName = new TextField();
        txtPoolName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtPoolName.setValueChangeMode(ValueChangeMode.EAGER);
        txtPoolName.setWidthFull();
        txtPoolName.setClassName("search");
        txtPoolName.setSuffixComponent(iconSearch);
        txtPoolName.addValueChangeListener(event -> dataProvider.addFilter(
                project -> StringUtils.containsIgnoreCase(project.getName(),
                        txtPoolName.getValue())));
        return txtPoolName;
    }
    
    private void loadProjects(Pool pool) {
        try {
            List<BusinessObjectLight> listProjects = bem.getPoolItems(pool.getId(), LIMIT);
            tblProjects.setItems(listProjects);
            tblProjects.getDataProvider().refreshAll();
            if (listProjects.isEmpty())
                btnAddActivity.setEnabled(false);
            else 
                btnAddActivity.setEnabled(true);
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(ProjectsUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void buildProjectsGrid() {
        tblProjects.setHeightFull();
        tblProjects.addColumn(BusinessObjectLight::getName)
                .setKey("module.general.labels.name");
        tblProjects.addComponentColumn(this::createProjectActionGrid);
        tblProjects.addItemClickListener(event -> {
            try {
                currentProject = event.getItem();
                updatePropertySheet(currentProject, true);
            } catch (BusinessObjectNotFoundException | InvalidArgumentException ex) {
                Logger.getLogger(ProjectsUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InventoryException ex) {
                Logger.getLogger(ProjectsUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    private HorizontalLayout createProjectActionGrid(BusinessObjectLight project) {
        HorizontalLayout lytActions;
        
        Command deleteProject = () -> {
            currentProject = null;
            loadProjects(currentPool);
            updatePropertySheet(currentPool);
        };
        Button btnDeleteProject = new Button(new Icon(VaadinIcon.TRASH),
            (event) -> {
                this.deleteProjectVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("project", project),
                        new ModuleActionParameter("commandClose", deleteProject)
                )).open();
            });
        btnDeleteProject.getElement().setProperty("title", this.deleteProjectVisualAction.getModuleAction().getDisplayName());
        
        Button btnActivity = new Button(new Icon(VaadinIcon.FILE_TREE),
            (event) -> {
                ActivityDialog activityDialog = new ActivityDialog(project);
            });
        btnActivity.getElement().setProperty("title", ts.getTranslatedString("module.projects.actions.activity.button-activity.name"));
        
        lytActions = new HorizontalLayout(btnDeleteProject, btnActivity);
        lytActions.setClassName("project-button-action-position");
        lytActions.setSpacing(false);
        return lytActions;
    }
    
    /**
     * Class to manage activities
     */
    private class ActivityDialog extends Dialog {
        Grid<BusinessObjectLight> tblActivities;  
        private ActivityDialog(BusinessObjectLight project) {
            super();
            tblActivities = new Grid<>();
            VerticalLayout lytMainContent = new VerticalLayout();
            lytMainContent.setSizeFull();
            lytMainContent.setHeightFull();              
            //Window to manage activities
            Dialog wdwActivityDialog = new Dialog();
            wdwActivityDialog.setCloseOnOutsideClick(false);
            wdwActivityDialog.setCloseOnEsc(false);
            
            Button btnClose = new Button(ts.getTranslatedString("module.projects.actions.activity.button-activity.close"), new Icon(VaadinIcon.CLOSE_SMALL),
                (event) -> {
                    wdwActivityDialog.close();
                    activityPropertySheet.removeAllColumns();
                    tblActivities.removeAllColumns();
                    lytActivityPropertySheet.setVisible(false);
                    btnDeleteActivity.setEnabled(false);
                });
            btnClose.setClassName("icon-button");
            
            Command addActivity = () -> {
                loadActivities(project);
            };
            Button btnAddActivity = new Button(ProjectsUI.this.newProjectActivityVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
                    ProjectsUI.this.newProjectActivityVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("pool", currentPool),
                        new ModuleActionParameter("project", project),
                        new ModuleActionParameter("commandRefresh", addActivity)
                    )).open();
                });
            btnAddActivity.getElement().setProperty("title", ProjectsUI.this.newProjectActivityVisualAction.getModuleAction().getDescription());
            btnAddActivity.setClassName("icon-button");
                 
            Command deleteActivity = () -> {
                lytActivityPropertySheet.setVisible(false);
                btnDeleteActivity.setEnabled(false);
                loadActivities(project);
            };
            btnDeleteActivity = new Button(ProjectsUI.this.deleteProjectActivityVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.TRASH),
                (event) -> {
                    ProjectsUI.this.deleteProjectActivityVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("activity", currentActivity),
                        new ModuleActionParameter("commandClose", deleteActivity)
                    )).open();
                 });
            btnDeleteActivity.getElement().setProperty("title", ProjectsUI.this.deleteProjectActivityVisualAction.getModuleAction().getDescription());
            btnDeleteActivity.setClassName("icon-button");
            btnDeleteActivity.setEnabled(false);
            
            commandRefresh = () -> {
                loadActivities(project);
            };
            activityPropertySheet = new PropertySheet(ts, new ArrayList<>(), "");
            activityPropertySheet.addPropertyValueChangedListener(ProjectsUI.this);
            H4 headerPropertySheet = new H4(ts.getTranslatedString("module.propertysheet.labels.header"));
            headerPropertySheet.setClassName("header");
            lytActivityPropertySheet = new VerticalLayout(headerPropertySheet, activityPropertySheet);
            lytActivityPropertySheet.setWidth("320px");
            lytActivityPropertySheet.setHeightFull();
            lytActivityPropertySheet.setVisible(false);
            lytActivityPropertySheet.setPadding(false);
            lytActivityPropertySheet.setSpacing(false);

            buildActivitiesGrid(project);
            H4 headerActivities = new H4(ts.getTranslatedString("module.projects.activity.header"));
            headerActivities.setClassName("header");
            HorizontalLayout lytButtons = new HorizontalLayout(btnAddActivity, btnDeleteActivity, btnClose);
            lytButtons.setSpacing(false);
            VerticalLayout lytTblActivities = new VerticalLayout(headerActivities, tblActivities);
            lytTblActivities.setWidth("200px");
            lytTblActivities.setHeight("500px");
            lytTblActivities.setPadding(false);
            lytTblActivities.setSpacing(false);
            HorizontalLayout lytContent = new HorizontalLayout(lytTblActivities, lytActivityPropertySheet);
            
            lytMainContent.add(lytContent, lytButtons);
            wdwActivityDialog.add(lytMainContent);
            wdwActivityDialog.setHeightFull();
            wdwActivityDialog.setWidthFull();
            wdwActivityDialog.open();
        }

        private void loadActivities(BusinessObjectLight project) {
            try {
                List<BusinessObjectLight> listActivities = bem.getObjectSpecialChildren(project.getClassName(), project.getId());
                tblActivities.setItems(listActivities);
                tblActivities.getDataProvider().refreshAll();
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                Logger.getLogger(ProjectsUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        private void buildActivitiesGrid(BusinessObjectLight project) {
            try {
                List<BusinessObjectLight> listActivities = bem.getObjectSpecialChildren(project.getClassName(), project.getId());
                ListDataProvider<BusinessObjectLight> dataProvider = new ListDataProvider<>(listActivities);
                tblActivities.setDataProvider(dataProvider);
                tblActivities.setHeightFull();
                tblActivities.addColumn(BusinessObjectLight::getName)
                        .setKey(ts.getTranslatedString("module.general.labels.name"));
                tblActivities.addItemClickListener(event -> {
                    try {
                        btnDeleteActivity.setEnabled(true);
                        currentActivity = event.getItem();
                        lytActivityPropertySheet.setVisible(true);
                        updatePropertySheet(currentActivity, false);
                    } catch (BusinessObjectNotFoundException | InvalidArgumentException ex) {
                        Logger.getLogger(ProjectsUI.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InventoryException ex) {
                        Logger.getLogger(ProjectsUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                Logger.getLogger(ProjectsUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Property sheet for pools
     * @param pool
     */
    private void updatePropertySheet(Pool pool) {
        try {
            Pool aWholePool = bem.getPool(pool.getId());
            propertySheet.setItems(PropertyFactory.propertiesFromPool(aWholePool));
            isPool = true;
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            Logger.getLogger(ProjectsUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Property sheet for objects, projects and activities
     * @param object object to update
     * @param project validate if the object is a project or activity 
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException
     * @throws InvalidArgumentException
     * @throws InventoryException 
     */
    private void updatePropertySheet(BusinessObjectLight object, Boolean project) throws 
            MetadataObjectNotFoundException, BusinessObjectNotFoundException, 
            InvalidArgumentException, InventoryException {
        isPool = false;
        isProject = false;
        BusinessObject aWholeObject = bem.getObject(object.getClassName(), object.getId());
        if (project == true) {
            isProject = true;
            propertySheet.setItems(PropertyFactory.propertiesFromBusinessObject(aWholeObject, ts, aem, mem));
        } else
            activityPropertySheet.setItems(PropertyFactory.propertiesFromBusinessObject(aWholeObject, ts, aem, mem));     
    }
        
    /**
     * Update properties for pools, projects and activities. 
     * @param property 
     */
    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        if(isPool == true) {
            try {
                if (property.getName().equals(Constants.PROPERTY_NAME)) {
                    aem.setPoolProperties(currentPool.getId(), String.valueOf(property.getValue()), currentPool.getDescription());
                    currentPool.setName(String.valueOf(property.getValue()));
                    headerPoolName.setText(String.valueOf(property.getValue()));
                } else if (property.getDescription().equals(Constants.PROPERTY_DESCRIPTION)) {
                    aem.setPoolProperties(currentPool.getId(), currentPool.getName(), String.valueOf(property.getValue()));
                    currentPool.setDescription(String.valueOf(property.getValue()));
                }
                tblPools.select(currentPool);
                updatePropertySheet(currentPool);
                loadPools();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            } catch (Exception ex) {
                Logger.getLogger(ProjectsUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if(isProject == true) {
            try {
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                bem.updateObject(currentProject.getClassName(), currentProject.getId(), attributes);
                if (property.getName().equals(Constants.PROPERTY_NAME))
                    currentProject.setName(String.valueOf(property.getValue()));
                loadProjects(currentPool);
                tblProjects.select(currentProject);
                updatePropertySheet(currentProject, true);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException | InvalidArgumentException ex) {
                Logger.getLogger(ProjectsUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InventoryException ex) {
                Logger.getLogger(ProjectsUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                bem.updateObject(currentActivity.getClassName(), currentActivity.getId(), attributes);
                if (property.getName().equals(Constants.PROPERTY_NAME))
                    currentActivity.setName(String.valueOf(property.getValue()));
                updatePropertySheet(currentActivity, false);
                commandRefresh.execute();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException | InvalidArgumentException ex) {
                Logger.getLogger(ProjectsUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InventoryException ex) {
                Logger.getLogger(ProjectsUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.projects.title");
    }
}

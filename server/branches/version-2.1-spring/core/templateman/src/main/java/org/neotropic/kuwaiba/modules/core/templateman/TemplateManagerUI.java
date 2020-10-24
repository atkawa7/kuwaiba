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
package org.neotropic.kuwaiba.modules.core.templateman;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.properties.PropertyFactory;
import org.neotropic.kuwaiba.modules.core.templateman.actions.DeleteTemplateItemVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.actions.DeleteTemplateSubItemVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.actions.DeleteTemplateVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.actions.NewBulkTemplateItemVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.actions.NewBulkTemplateSpecialItemVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.actions.NewTemplateItemVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.actions.NewTemplateSpecialItemVisualAction;
import org.neotropic.kuwaiba.modules.core.templateman.actions.NewTemplateVisualAction;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main UI for template manager module, initialize all display elements and
 * business logic.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Route(value = "templates", layout = TemplateManagerLayout.class)
public class TemplateManagerUI extends SplitLayout implements ActionCompletedListener, PropertySheet.IPropertyValueChangedListener, HasDynamicTitle {

    /**
     * grid to list class attributes
     */
    private final Grid<ClassMetadataLight> tblClasses;

    /**
     * data provider use for lazy loading and filtering, belongs to tblsCasses
     */
    private ListDataProvider<ClassMetadataLight> classesDataProvider;

    /**
     * grid to list class attributes
     */
    private final Grid<TemplateObjectLight> tblTemplates;

    /**
     * grid for tblTemplates and it options
     */
    private final VerticalLayout lytvTemplates;

    /**
     * grid to list class attributes
     */
    private Button btnAddTemplate;

    /*
     * grid to list class attributes
     */
    private Button btnRemoveTemplate;

    /**
     * data provider use for lazy loading and filtering, belongs to tblTemplates
     */
    private ListDataProvider<TemplateObjectLight> templatesDataProvider;

    /**
     * Grid to list class attributes
     */
    private final TreeGrid<TemplateObjectLight> tblChildsTemplate;

    /**
     * Data provider use for lazy loading and filtering, belongs to
     * tblChildsTemplate
     */
    private HierarchicalDataProvider childsTemplateDataProvider;

    /**
     * Menu for tblChildsTemplate in case need add new item or special item
     */
    private MenuBar mnuAddChildTemplateItems;

    /**
     * Sub menu of mnuAddChildTemplateItems, option for add item and multiple
     * item
     */
    private MenuItem mnuAddChildsTemplateItem;

    /**
     * Sub menu of mnuAddChildTemplateItems, option for add special item and
     * multiple special item
     */
    private MenuItem mnuAddSpecialChildsTemplateItem;

    /**
     * Grid for tblChildsTemplate and it options
     */
    private final VerticalLayout lytvChildTemplate;

    /**
     * the visual action to delete a list type item
     */
    private VerticalLayout lytvPropertySheet;

    /**
     * Properties by any element selected
     */
    private PropertySheet propertysheet;

    /**
     * Icon inside tree grid size
     */
    public static final String ICON_INLINE_SIZE = "16px";

    /**
     * Color for special add icon
     */
    public static final String BLUE_COLOR = "BLUE";

    /**
     * Color for remove icon
     */
    public static final String RED_COLOR = "RED";

    /**
     * Color for special add icon
     */
    public static final String GREEN_COLOR = "BLUE";

    /**
     * refresh template grid action
     */
    private Command refreshTemplateAction;

    /**
     * refresh template child grid action
     */
    private Command refreshChildAction;

    /**
     * Class selected
     */
    private ClassMetadataLight selectedClass;

    /**
     * Template selected
     */
    private TemplateObjectLight selectedTemplate;

    /**
     * Template item selected
     */
    private TemplateObjectLight selectedTemplateItem;

    /**
     * Property to edit is for child
     */
    private boolean editChild;

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
     * translation service
     */
    @Autowired
    private TranslationService ts;

    /**
     * Delete a template visual action
     */
    @Autowired
    private DeleteTemplateVisualAction deleteTemplateVisualAction;

    /**
     * Create a new Template visual
     */
    @Autowired
    private NewTemplateVisualAction newTemplateVisualAction;

    /**
     * Delete a template visual action
     */
    @Autowired
    private DeleteTemplateItemVisualAction deleteTemplateItemVisualAction;

    /**
     * Create a new Template visual
     */
    @Autowired
    private NewTemplateItemVisualAction newTemplateItemVisualAction;

    /**
     * Create bulk Templates
     */
    @Autowired
    private NewBulkTemplateItemVisualAction newBulkTemplateItemVisualAction;

    /**
     * Delete a template visual action
     */
    @Autowired
    private DeleteTemplateSubItemVisualAction deleteTemplateSubItemVisualAction;

    /**
     * Create a new Template visual
     */
    @Autowired
    private NewTemplateSpecialItemVisualAction newTemplateSpecialItemVisualAction;

    /**
     * Create a new Template visual
     */
    @Autowired
    private NewBulkTemplateSpecialItemVisualAction newBulkTemplateSpecialItemVisualAction;

    public TemplateManagerUI() {
        super();
        setSizeFull();
        setOrientation(Orientation.HORIZONTAL);
        //init principal elements
        this.tblClasses = new Grid<>();
        this.tblTemplates = new Grid<>();
        this.tblChildsTemplate = new TreeGrid<>();
        this.lytvTemplates = new VerticalLayout();
        this.lytvChildTemplate = new VerticalLayout();
        this.lytvPropertySheet = new VerticalLayout();
    }

    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        createContent();
    }

    /**
     * Fired when a dialog or other action finish
     *
     * @param ev
     */
    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
        } else {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
        }
    }

    private void createContent() {
        setSizeFull();
        setPrimaryStyle("minWidth", "25%");
        setSecondaryStyle("maxWidth", "25%");
        SplitLayout classesLayout = new SplitLayout();
        SplitLayout templateLayout = new SplitLayout();
        //create action for close dialog
        refreshChildAction = () -> {
            childsTemplateDataProvider.refreshAll();
        };
        //register visual actions       
        this.newTemplateVisualAction.registerActionCompletedLister(this);
        this.deleteTemplateVisualAction.registerActionCompletedLister(this);
        this.newTemplateItemVisualAction.registerActionCompletedLister(this);
        this.newBulkTemplateItemVisualAction.registerActionCompletedLister(this);
        this.deleteTemplateItemVisualAction.registerActionCompletedLister(this);
        this.newTemplateSpecialItemVisualAction.registerActionCompletedLister(this);
        this.newBulkTemplateSpecialItemVisualAction.registerActionCompletedLister(this);
        this.deleteTemplateSubItemVisualAction.registerActionCompletedLister(this);
        //set elements properties
        classesLayout.setOrientation(Orientation.HORIZONTAL);
        templateLayout.setOrientation(Orientation.HORIZONTAL);
        buildMainClassesGrid();
        buildTemplateGrid();
        buildChildTemplateGrid();
        buildPropertySheetLayout();

        //define layout        
        classesLayout.setPrimaryStyle("minWidth", "25%");
        classesLayout.setSecondaryStyle("minWidth", "25%");
        classesLayout.addToPrimary(tblClasses);
        classesLayout.addToSecondary(templateLayout);
        templateLayout.setPrimaryStyle("minWidth", "25%");
        templateLayout.setSecondaryStyle("minWidth", "25%");
        templateLayout.addToPrimary(lytvTemplates);
        templateLayout.addToSecondary(lytvChildTemplate);
        addToPrimary(classesLayout);
        addToSecondary(lytvPropertySheet);
    }

    /**
     * Create display form and set action listeners
     */
    private void buildMainClassesGrid() {
        tblClasses.setHeightFull();
        tblClasses.setSelectionMode(Grid.SelectionMode.SINGLE);
        tblClasses.addColumn(ClassMetadataLight::toString)
                .setHeader(String.format("%s", ts.getTranslatedString("module.templateman.clases")))
                .setKey(ts.getTranslatedString("module.general.labels.name"));

        tblClasses.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                selectedClass = event.getValue();
                editChild = false;
                updateTemplateGrid(event.getValue());
                updateChildTemplateItemsGrid(null);
                updatePropertySheet(event.getValue());
            } else {
                btnAddTemplate.setEnabled(false);
                btnRemoveTemplate.setEnabled(false);
            }
        });
        //define listers and data providers
        try {
            buildClasessItemsGrid();
        } catch (MetadataObjectNotFoundException ex) {
            Logger.getLogger(TemplateManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Create data provider for principal grid
     *
     * @throws MetadataObjectNotFoundException; not found or invalid query
     * search
     */
    private void buildClasessItemsGrid() throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> allClassesLight = mem.getAllClassesLight(false, false);
        //order alphabetically
        allClassesLight.stream().sorted(Comparator.comparing(ClassMetadataLight::toString))
                .collect(Collectors.toList());

        this.classesDataProvider = new ListDataProvider<>(allClassesLight);
        tblClasses.setDataProvider(classesDataProvider);

        createTblClassesfilter();
    }

    /**
     * Create display filter for main grid
     */
    private void createTblClassesfilter() {
        HeaderRow filterRow = tblClasses.appendHeaderRow();
        Icon icon = VaadinIcon.SEARCH.create();
        icon.setSize(ICON_INLINE_SIZE);

        TextField txtFilterListTypeName = new TextField(ts.getTranslatedString("module.general.labels.filter"),
                ts.getTranslatedString("module.general.labels.filter-placeholder"));
        //properties        
        filterRow.getCell(tblClasses.getColumnByKey(ts.getTranslatedString("module.general.labels.name"))).setComponent(txtFilterListTypeName);
        txtFilterListTypeName.setValueChangeMode(ValueChangeMode.EAGER);
        txtFilterListTypeName.setSuffixComponent(icon);
        txtFilterListTypeName.setWidthFull();
        txtFilterListTypeName.addValueChangeListener(event -> classesDataProvider.addFilter(
                element -> {
                    return StringUtils.containsIgnoreCase(
                            element.getName(), txtFilterListTypeName.getValue())
                    || StringUtils.containsIgnoreCase(
                            element.getDisplayName(), txtFilterListTypeName.getValue()
                    );
                }
        ));
    }

    /**
     * Create display form and set action listeners
     */
    private void buildTemplateGrid() {
        HorizontalLayout lythButton = new HorizontalLayout();
        btnAddTemplate = new Button(String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template.name")));
        btnRemoveTemplate = new Button(String.format("%s", ts.getTranslatedString("module.templateman.actions.delete-template.name")));
        //set element properties
        btnAddTemplate.setIcon(new Icon(VaadinIcon.PLUS));
        btnAddTemplate.setEnabled(false);
        btnAddTemplate.getElement().setProperty("title",
                String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template.description")));
        btnAddTemplate.addClickListener(clickEvent -> {
            this.newTemplateVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("className", selectedClass.getName()),
                    new ModuleActionParameter("commandClose", refreshTemplateAction)
            )).open();
        });

        btnRemoveTemplate.setIcon(new Icon(VaadinIcon.TRASH));
        btnRemoveTemplate.setEnabled(false);
        btnRemoveTemplate.getElement().setProperty("title",
                String.format("%s", ts.getTranslatedString("module.templateman.actions.delete-template.description")));
        btnRemoveTemplate.addClickListener(clickEvent -> {
            Command deleteTemplateAction = () -> {//refresh template grid and refres property sheet
                refreshTemplates(selectedClass);
                updatePropertySheet(selectedClass);
            };
            this.deleteTemplateVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("templateItem", selectedTemplate),
                    new ModuleActionParameter("commandClose", deleteTemplateAction)
            )).open();
        });

        tblTemplates.setHeightFull();
        tblTemplates.setSelectionMode(Grid.SelectionMode.SINGLE);
        tblTemplates.addColumn(TemplateObjectLight::getName)
                .setHeader(String.format("%s", ts.getTranslatedString("module.templateman.templates")))
                .setKey(ts.getTranslatedString("module.general.labels.name"));

        tblTemplates.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                this.selectedTemplate = event.getValue();
                updateChildTemplateItemsGrid(event.getValue());
                btnRemoveTemplate.setEnabled(true);
            } else {
                if (event.isFromClient()) 
                    this.selectedTemplate = null;
                
                btnRemoveTemplate.setEnabled(false);
            }
        });

        createTblTemplatesfilter();
        //create layout
        lythButton.add(btnAddTemplate, btnRemoveTemplate);
        this.lytvTemplates.setPadding(false);
        this.lytvTemplates.setSpacing(false);
        this.lytvTemplates.add(tblTemplates, lythButton);
    }

    /**
     * Create data provider for principal grid
     *
     * @throws MetadataObjectNotFoundException; not found or invalid query
     * search
     */
    private void updateTemplateGrid(ClassMetadataLight object) {
        if (object != null) {
            refreshTemplates(object);
            btnAddTemplate.setEnabled(true);
            //create action when dialog finish
            refreshTemplateAction = () -> {
                refreshTemplates(object);
            };
        } else {
            tblTemplates.setItems(new ArrayList<>());
            btnAddTemplate.setEnabled(false);
            btnRemoveTemplate.setEnabled(false);
        }
    }

    /**
     * Create display filter for main grid
     */
    private void createTblTemplatesfilter() {
        HeaderRow filterRow = tblTemplates.appendHeaderRow();
        Icon icon = VaadinIcon.SEARCH.create();
        icon.setSize(ICON_INLINE_SIZE);

        TextField txtFilterListTypeName = new TextField(
                ts.getTranslatedString("module.general.labels.filter"),
                ts.getTranslatedString("module.general.labels.filter-placeholder"));
        //properties        
        filterRow.getCell(tblTemplates.getColumnByKey(
                ts.getTranslatedString("module.general.labels.name")))
                .setComponent(txtFilterListTypeName);
        txtFilterListTypeName.setValueChangeMode(ValueChangeMode.EAGER);
        txtFilterListTypeName.setSuffixComponent(icon);
        txtFilterListTypeName.setWidthFull();
        txtFilterListTypeName.addValueChangeListener(event -> {
            if (templatesDataProvider != null) {
                templatesDataProvider.addFilter(
                        element -> StringUtils.containsIgnoreCase(element.getName(),
                                txtFilterListTypeName.getValue())
                );
            }
        }
        );
    }

    /**
     * Create display form and set action listeners
     */
    private void buildChildTemplateGrid() {
        mnuAddChildTemplateItems = new MenuBar();
        //elements properties        
        mnuAddChildTemplateItems.setWidthFull();

        mnuAddChildsTemplateItem = mnuAddChildTemplateItems.addItem(
                new Button(String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template-item.name")),
                        new Icon(VaadinIcon.PLUS)));
        mnuAddChildsTemplateItem.getElement().getThemeList().add("BUTTON_SMALL");
        mnuAddChildsTemplateItem.getElement().setProperty("title",
                String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template-item.description")));
        mnuAddChildsTemplateItem.getSubMenu().addItem(
                String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template-item-sigle.name")),
                e -> newStructureItem());
        mnuAddChildsTemplateItem.getSubMenu().addItem(
                String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template-item-multiple.name")),
                e -> newBulkStructureItem());
        mnuAddChildsTemplateItem.setEnabled(false);

        mnuAddSpecialChildsTemplateItem = mnuAddChildTemplateItems.addItem(
                new Button(String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template-special-item.name")),
                        new Icon(VaadinIcon.ASTERISK)));
        mnuAddSpecialChildsTemplateItem.getElement().getThemeList().add("BUTTON_SMALL");
        mnuAddSpecialChildsTemplateItem.getElement().setProperty("title",
                String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template-special-item.description")));
        mnuAddSpecialChildsTemplateItem.getSubMenu().addItem(
                String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template-item-sigle.name")),
                e -> newStructureSpecialItem());
        mnuAddSpecialChildsTemplateItem.getSubMenu().addItem(
                String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template-item-multiple.name")),
                e -> newBulkStructureSpecialItem());
        mnuAddSpecialChildsTemplateItem.setEnabled(false);

        tblChildsTemplate.setHeightFull();
        tblChildsTemplate.addHierarchyColumn(TemplateObjectLight::getName)
                .setHeader(String.format("%s", ts.getTranslatedString("module.templateman.items")));

        tblChildsTemplate.addComponentColumn(this::buildChildTemplateItemsOptions);
        tblChildsTemplate.addSelectionListener(this::editStructureItem);
        this.lytvChildTemplate.add(tblChildsTemplate, mnuAddChildTemplateItems);
    }

    /**
     * Create data provider for principal grid
     *
     * @throws MetadataObjectNotFoundException; not found or invalid query
     * search
     */
    private void updateChildTemplateItemsGrid(TemplateObjectLight object) {
        if (object != null) {
            selectedTemplate = object;
            refreshStructure(object);
            updatePropertySheet(object);
            //enable menu buttons            
            mnuAddChildsTemplateItem.setEnabled(true);
            mnuAddSpecialChildsTemplateItem.setEnabled(true);
        } else {
            mnuAddChildsTemplateItem.setEnabled(false);
            mnuAddSpecialChildsTemplateItem.setEnabled(false);
            refreshStructure(null);
        }
    }

    /**
     * create option for any member inside tree grid
     *
     * @param selectedItem;TemplateObjectLight; inline element
     * @return lythOptions;Component; Horizontal layout with option
     */
    private Component buildChildTemplateItemsOptions(TemplateObjectLight selectedItem) {
        MenuBar menuBar = new MenuBar();
        //element properties
        menuBar.setWidthFull();
        menuBar.getElement().getThemeList().add("short-icons");

        //add sub items
        MenuItem smnuAddChildsTemplateItem = menuBar.addItem(new Icon(VaadinIcon.PLUS));
        smnuAddChildsTemplateItem.getElement().setProperty("title",
                String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template-item.description")));
        smnuAddChildsTemplateItem.getSubMenu().addItem(
                String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template-item-sigle.name")),
                e -> newStructureItem(selectedItem, true));
        smnuAddChildsTemplateItem.getSubMenu().addItem(
                String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template-item-multiple.name")),
                e -> newBulkStructureItem(selectedItem, true));

        MenuItem smnuAddSpecialChildsTemplateItem = menuBar.addItem(new Icon(VaadinIcon.ASTERISK));
        smnuAddSpecialChildsTemplateItem.getElement().setProperty("title",
                String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template-special-item.description")));
        smnuAddSpecialChildsTemplateItem.getSubMenu().addItem(
                String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template-item-sigle.name")),
                e -> newStructureSpecialItem(selectedItem, true));
        smnuAddSpecialChildsTemplateItem.getSubMenu().addItem(
                String.format("%s", ts.getTranslatedString("module.templateman.actions.new-template-item-multiple.name")),
                e -> newBulkStructureSpecialItem(selectedItem, true));

        MenuItem smnuRemoveChildsTemplateItem = menuBar.addItem(
                new Icon(VaadinIcon.TRASH), e -> deleteStructureItem(selectedItem));
        smnuRemoveChildsTemplateItem.getElement().setProperty("title",
                String.format("%s", ts.getTranslatedString("module.templateman.actions.deleteItem-template.description")));

        return menuBar;
    }

    /**
     * Create display property sheet
     */
    private void buildPropertySheetLayout() {
        H4 headerPropertySheet = new H4(ts.getTranslatedString("module.propertysheet.labels.header"));
        propertysheet = new PropertySheet(ts, new ArrayList<>(), "");
        lytvPropertySheet = new VerticalLayout(headerPropertySheet, propertysheet);
        lytvPropertySheet.setWidth("45%");
        propertysheet.addPropertyValueChangedListener(this);

    }

    /**
     * update property sheet in case element selected is a class
     *
     * @param object;ClassMetadataLight; parent class element
     */
    private void updatePropertySheet(ClassMetadataLight object) {
        try {
            ClassMetadata objectFull = mem.getClass(object.getId());
            if (objectFull != null) {
                propertysheet.setItems(PropertyFactory.generalPropertiesFromClass(objectFull));
                propertysheet.setReadOnly(true);
            }
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage()).open();
            Logger.getLogger(TemplateManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * update property sheet in case element selected is a template
     *
     * @param object;ClassMetadataLight; parent class element
     */
    private void updatePropertySheet(TemplateObjectLight object) {
        try {
            TemplateObject objectFull = aem.getTemplateElement(object.getClassName(), object.getId());
            if (objectFull != null) {
                propertysheet.setItems(PropertyFactory.propertiesFromTemplateObject(objectFull, ts, aem, mem));
                propertysheet.setReadOnly(false);
            }
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage()).open();
            Logger.getLogger(TemplateManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Creates a new Structure item, based in allowed content.
     *
     * @param se;SelectionEvent; event of click in expandable item grid
     */
    private void editStructureItem(SelectionEvent<Grid<TemplateObjectLight>, TemplateObjectLight> se) {
        
        if (se.getFirstSelectedItem().orElse(null) != null) {
            editChild = true;
            selectedTemplateItem = se.getFirstSelectedItem().orElse(null);
            updatePropertySheet(selectedTemplateItem);
        }else
            if(se.isFromClient())
                selectedTemplateItem = null;
        
    }

    /**
     * Creates a new Structure item, based in allowed content.
     *
     * @param object;TemplateObjectLight; parent structure item
     */
    private void editStructureItem(TemplateObjectLight templateObjectLight) {

        if (templateObjectLight != null) {
            editChild = true;
            updatePropertySheet(templateObjectLight);
        }
    }

    /**
     * Creates a new template item, based in allowed content.
     */
    private void newStructureItem() {
        newStructureItem(null, false);
    }

    /**
     * Creates a new template item, based in allowed content. if element is
     * first in hierarchy , template element is take as father, if no it is a
     * sub item, parent is item above it.
     *
     * @param isSubItem;Boolean; true if element is a sub item
     * @param childElement;TemplateObjectLight; sub item element
     */
    private void newStructureItem(TemplateObjectLight childElement, boolean isSubItem) {
        this.refreshChildAction = () -> {
            refreshStructure(selectedTemplate);
        };
        if (!isSubItem) {
            this.newTemplateItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("parentClassName", selectedTemplate.getClassName()),
                    new ModuleActionParameter("parentId", selectedTemplate.getId()),
                    new ModuleActionParameter("commandClose", refreshChildAction)
            )).open();
        } else {
            this.newTemplateItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("parentClassName", childElement.getClassName()),
                    new ModuleActionParameter("parentId", childElement.getId()),
                    new ModuleActionParameter("commandClose", childElement)
            )).open();
        }
    }

    /**
     *
     * Creates massive template items, based in allowed content.
     */
    private void newBulkStructureItem() {
        newBulkStructureItem(null, false);
    }

    /**
     *
     * Creates massive template items, based in allowed content. if element is
     * first in hierarchy , template element is take as father, if no it is a
     * sub item, parent is item above it.
     *
     * @param isSubItem;Boolean; true if element is a sub item,
     * @param childElement;TemplateObjectLight; sub item element
     */
    private void newBulkStructureItem(TemplateObjectLight childElement, boolean isSubItem) {
        this.refreshChildAction = () -> {
            refreshStructure(selectedTemplate);
        };
        if (!isSubItem) {
            this.newBulkTemplateItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("parentClassName", selectedTemplate.getClassName()),
                    new ModuleActionParameter("parentId", selectedTemplate.getId()),
                    new ModuleActionParameter("commandClose", refreshChildAction)
            )).open();
        } else {
            this.newBulkTemplateItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("parentClassName", childElement.getClassName()),
                    new ModuleActionParameter("parentId", childElement.getId()),
                    new ModuleActionParameter("commandClose", refreshChildAction)
            )).open();
        }
    }

    /**
     * Creates a new template special item, based in allowed content
     */
    private void newStructureSpecialItem() {
        newStructureSpecialItem(null, false);
    }

    /**
     * Creates a new template special item, based in allowed content. if element
     * is first in hierarchy , template element is take as father, if no it is a
     * sub item, parent is item above it.
     *
     * @param isSubItem;TemplateObjectLight; true if element is a sub item
     * @param childElement;TemplateObjectLight; sub item element
     */
    private void newStructureSpecialItem(TemplateObjectLight childElement, boolean isSubItem) {
        this.refreshChildAction = () -> {
            refreshStructure(selectedTemplate);
        };
        if (!isSubItem) {
            this.newTemplateSpecialItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("parentClassName", selectedTemplate.getClassName()),
                    new ModuleActionParameter("parentId", selectedTemplate.getId()),
                    new ModuleActionParameter("commandClose", refreshChildAction)
            )).open();
        } else {
            this.newTemplateSpecialItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("parentClassName", childElement.getClassName()),
                    new ModuleActionParameter("parentId", childElement.getId()),
                    new ModuleActionParameter("commandClose", refreshChildAction)
            )).open();
        }
    }

    /**
     *
     * Creates massive template special item, based in allowed content.
     */
    private void newBulkStructureSpecialItem() {
        newBulkStructureSpecialItem(null, false);
    }

    /**
     *
     * Creates massive template special item, based in allowed content. if
     * element is first in hierarchy , template element is take as father, if no
     * it is a sub item, parent is item above it.
     *
     * @param isSubItem;TemplateObjectLight; true if element is a sub item,
     */
    private void newBulkStructureSpecialItem(TemplateObjectLight childElement, boolean isSubItem) {
        this.refreshChildAction = () -> {
            refreshStructure(selectedTemplate);
        };
        if (!isSubItem) {
            this.newBulkTemplateSpecialItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("parentClassName", selectedTemplate.getClassName()),
                    new ModuleActionParameter("parentId", selectedTemplate.getId()),
                    new ModuleActionParameter("commandClose", refreshChildAction)
            )).open();
        } else {
            this.newBulkTemplateSpecialItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("parentClassName", childElement.getClassName()),
                    new ModuleActionParameter("parentId", childElement.getId()),
                    new ModuleActionParameter("commandClose", refreshChildAction)
            )).open();
        }
    }

    /**
     * Delete selected Structure item.
     *
     * @param object;TemplateObjectLight; parent template item
     */
    private void deleteStructureItem(TemplateObjectLight object) {
        Command deleteChildAction = () -> {
            refreshStructure(selectedTemplate);
            //reset focus to father , in case selected element is in property sheet focus
            updatePropertySheet(selectedTemplate);
        };
        this.deleteTemplateItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                new ModuleActionParameter("templateItem", object),
                new ModuleActionParameter("className", object.getName()),
                new ModuleActionParameter("commandClose", deleteChildAction)
        )).open();
    }

    /**
     * Create template data provider and refresh items
     *
     * @param object; ClassMetadataLight; parent class
     */
    private void refreshTemplates(ClassMetadataLight object) {
        try {
            List<TemplateObjectLight> allTemplatesLight = aem.getTemplatesForClass(object.getName());
            this.templatesDataProvider = new ListDataProvider<>(allTemplatesLight);
            tblTemplates.setDataProvider(templatesDataProvider);
            this.templatesDataProvider.refreshAll();            
        } catch (MetadataObjectNotFoundException ex) {
            Logger.getLogger(TemplateManagerLayout.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Create template data provider items (structure )and refresh items
     *
     * @param object; TemplateObjectLight; parent template
     */
    private void refreshStructure(TemplateObjectLight object) {
        //create fisrt call data
        List<TemplateObjectLight> allChildsTemplateLight = new ArrayList<>();
        if (object != null) {
            allChildsTemplateLight.addAll(aem.getTemplateElementChildren(object.getClassName(), object.getId()));
            allChildsTemplateLight.addAll(aem.getTemplateSpecialElementChildren(object.getClassName(), object.getId()));
        }
        childsTemplateDataProvider = new AbstractBackEndHierarchicalDataProvider<TemplateObjectLight, Void>() {
            //count parent elements
            @Override
            public int getChildCount(HierarchicalQuery<TemplateObjectLight, Void> query) {
                if (query.getParent() != null) {
                    List<TemplateObjectLight> allInnerChildsTemplateLight = new ArrayList<>();
                    allInnerChildsTemplateLight.addAll(aem.getTemplateElementChildren(query.getParent().getClassName(), query.getParent().getId()));
                    allInnerChildsTemplateLight.addAll(aem.getTemplateSpecialElementChildren(query.getParent().getClassName(), query.getParent().getId()));
                    return allInnerChildsTemplateLight.size();
                } else { //in case element set for fisrt time 
                    return allChildsTemplateLight.size();
                }
            }

            //load child elements    
            @Override
            public boolean hasChildren(TemplateObjectLight item) {
                List<TemplateObjectLight> innerChildsTemplateLight = new ArrayList<>();
                innerChildsTemplateLight.addAll(aem.getTemplateElementChildren(item.getClassName(), item.getId()));
                innerChildsTemplateLight.addAll(aem.getTemplateSpecialElementChildren(item.getClassName(), item.getId()));

                return innerChildsTemplateLight.size() > 0;
            }

            //return parent elements list
            @Override
            protected Stream<TemplateObjectLight> fetchChildrenFromBackEnd(HierarchicalQuery<TemplateObjectLight, Void> query) {
                //load normal and special children and gorus both to be display
                if (query.getParent() != null) {
                    //update property sheet
                    editStructureItem(query.getParent());

                    List<TemplateObjectLight> allInnerChildsTemplateLight = new ArrayList<>();
                    allInnerChildsTemplateLight.addAll(aem.getTemplateElementChildren(query.getParent().getClassName(), query.getParent().getId()));
                    allInnerChildsTemplateLight.addAll(aem.getTemplateSpecialElementChildren(query.getParent().getClassName(), query.getParent().getId()));
                    return allInnerChildsTemplateLight.stream();
                } else {
                    return allChildsTemplateLight.stream();
                }
            }
        };
        tblChildsTemplate.setDataProvider(childsTemplateDataProvider);
        tblChildsTemplate.getDataProvider().refreshAll();
    }

    @Override
    public void updatePropertyChanged(AbstractProperty property) {
        try {
            String[] attributesNames = {property.getName()};
            String[] attributesValues = {property.getValue().toString()};

            if (!editChild) {//update first containment child               
                aem.updateTemplateElement(selectedTemplate.getClassName(), selectedTemplate.getId(),
                        attributesNames, attributesValues);
                refreshTemplates(selectedClass);
                tblTemplates.asSingleSelect().setValue(selectedTemplate);
                
            } else {//update lower containment child
                aem.updateTemplateElement(selectedTemplateItem.getClassName(), selectedTemplateItem.getId(),
                        attributesNames, attributesValues);
                refreshStructure(selectedTemplate);
                tblChildsTemplate.asSingleSelect().setValue(selectedTemplateItem);
                
            }

            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update")).open();
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException | MetadataObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage()).open();
        }
    }

    @Override
    public void onDetach(DetachEvent ev) {
        this.newTemplateVisualAction.unregisterListener(this);
        this.newBulkTemplateItemVisualAction.unregisterListener(this);
        this.deleteTemplateVisualAction.unregisterListener(this);
        this.newTemplateItemVisualAction.unregisterListener(this);
        this.deleteTemplateItemVisualAction.unregisterListener(this);
        this.newTemplateSpecialItemVisualAction.unregisterListener(this);
        this.newBulkTemplateSpecialItemVisualAction.unregisterListener(this);
        this.deleteTemplateSubItemVisualAction.unregisterListener(this);
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.templateman.title");
    }
}

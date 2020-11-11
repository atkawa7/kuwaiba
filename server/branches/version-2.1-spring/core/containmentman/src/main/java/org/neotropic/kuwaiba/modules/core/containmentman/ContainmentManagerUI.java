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
package org.neotropic.kuwaiba.modules.core.containmentman;

import com.neotropic.flow.component.paper.toggle.PaperToggleButton;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.ExpandEvent;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.containmentman.api.ClassMetadataLightCustom;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main UI for containment manager module, initialize all display elements and
 * business logic.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Route(value = "containment", layout = ContainmentManagerLayout.class)
public class ContainmentManagerUI extends VerticalLayout implements ActionCompletedListener, HasDynamicTitle {

    private HorizontalLayout lytMainContent;
    private Button btnAddChild;
    private ComboBox<ClassMetadataLightCustom> cbxFilterClassNameField;
    private PaperToggleButton tgbContainmentHierarchyType;
    private TreeGrid<ClassMetadataLightCustom> tblParentClass;
    private Grid<ClassMetadataLightCustom> tblChildClass;
    private List<ClassMetadataLightCustom> parentClasses;
    private boolean containmentHierarchyType;

    /**
     * data provider use for lazy loading and filtering, belongs to
     * tblChildClass
     */
    private ListDataProvider<ClassMetadataLightCustom> childClassesDataProvider;
    /**
     * translation service
     */
    @Autowired
    private TranslationService ts;

    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;

    public ContainmentManagerUI() {
        super();
        setSizeFull();
    }

    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        lytMainContent = new HorizontalLayout();
        lytMainContent.setSizeFull();
        createContent();
    }

    private void createContent() {
        buildContainmentOptions();
        buildMainContent();
        add(new Label(ts.getTranslatedString("module.containmentman.instructions")));
    }

    /**
     * build all Containment options elements
     */
    private void buildContainmentOptions() {
        HorizontalLayout lytContainmentHierarchyType = new HorizontalLayout();
        tgbContainmentHierarchyType = new PaperToggleButton();
        tgbContainmentHierarchyType.setClassName("green", true);
        Label standard = new Label(ts.getTranslatedString("module.containmentman.combobox.standard"));
        Label special = new Label(ts.getTranslatedString("module.containmentman.combobox.special"));
        //components properties
        standard.setClassName("enable-label");
        special.removeClassName("enable-label");
        containmentHierarchyType = true;
        lytContainmentHierarchyType.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        tgbContainmentHierarchyType.addValueChangeListener(ef -> {
            if (ef.getValue()) {
                containmentHierarchyType = false;
                standard.removeClassName("enable-label");
                special.setClassName("enable-label");
                new SimpleNotification(ts.getTranslatedString("module.general.messages.information"),
                        ts.getTranslatedString("module.containmentman.combobox.special"),
                        AbstractNotification.NotificationType.INFO, ts).open();
                if (cbxFilterClassNameField.getValue() != null) {
                    tblParentClass.setItems(getPosibleChildrenClones(cbxFilterClassNameField.getValue()), element -> getSubClassMetadataClones(element));
                    tblParentClass.getDataProvider().refreshAll();
                }
            } else {
                containmentHierarchyType = true;
                standard.setClassName("enable-label");
                special.removeClassName("enable-label");
                new SimpleNotification(ts.getTranslatedString("module.general.messages.information"),
                        ts.getTranslatedString("module.containmentman.combobox.standard"),
                        AbstractNotification.NotificationType.INFO, ts).open();
                if (cbxFilterClassNameField.getValue() != null) {
                    tblParentClass.setItems(getPosibleChildrenClones(cbxFilterClassNameField.getValue()), element -> getSubClassMetadataClones(element));
                    tblParentClass.getDataProvider().refreshAll();
                }
            }
        });

        lytContainmentHierarchyType.add(standard, tgbContainmentHierarchyType, special);
        add(lytContainmentHierarchyType);
        setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, lytContainmentHierarchyType);
    }

    /**
     * build all displayed elements
     */
    private void buildMainContent() {
        buildParentClasessGrid();
        buildButtonsOptionsComponent();
        buildChildClasessGrid();
        add(lytMainContent);
    }

    /**
     * build parent classes grid, add to main layout and set listeners
     */
    private void buildParentClasessGrid() {
        VerticalLayout lytParentClass = new VerticalLayout();
        tblParentClass = new TreeGrid<>();
        //elements properties
        lytParentClass.setPadding(false);
        lytParentClass.setSpacing(false);
        lytParentClass.setMargin(false);
        tblParentClass.setMultiSort(false);
        tblParentClass.setColumnReorderingAllowed(false);
        tblParentClass.setHeightFull();
        tblParentClass.setWidthFull();

        tblParentClass.addComponentHierarchyColumn(element -> {
            Label childName = new Label(element.toString());
            if(!element.isRoot()) 
                childName.setClassName("children-text");

            return childName;
            
            });
                
        tblParentClass.addComponentColumn(element -> {
            if (element.isRoot())
                return buildRemoveButtonChindren(element);
            else 
                return new Label();
        });

        // First filter                
        cbxFilterClassNameField = new ComboBox<>();
        cbxFilterClassNameField.addValueChangeListener(this::enableTransferButtons);
        cbxFilterClassNameField.setWidthFull();
        cbxFilterClassNameField.setPlaceholder(ts.getTranslatedString("module.containmentman.combobox.filter.placeholder"));
        cbxFilterClassNameField.setItems(getClasessClones());

        lytParentClass.add(cbxFilterClassNameField, tblParentClass);
        lytMainContent.add(lytParentClass);
    }

    /**
     * Create display form and set action listeners
     */
    private Button buildRemoveButtonChindren(ClassMetadataLightCustom classMetadataLightCustom) {
        Button btnRemoveChindren = new Button(new Icon(VaadinIcon.CLOSE));
        btnRemoveChindren.getElement().setProperty("title",
                String.format("%s", ts.getTranslatedString("module.containmentman.button.actions.remove-child.description")));
        btnRemoveChindren.addClickListener(clickEvent -> removeChild(classMetadataLightCustom));

        return btnRemoveChindren;
    }

    /**
     * build button and listener for add new child
     */
    private void buildButtonsOptionsComponent() {
        //options buttons
        btnAddChild = new Button(new Icon(VaadinIcon.CHEVRON_LEFT));        
        btnAddChild.setEnabled(false);
        btnAddChild.getElement().setProperty("title",
                String.format("%s", ts.getTranslatedString("module.containmentman.button.actions.new-child.description")));
        btnAddChild.addClickListener(this::addNewChild);

        lytMainContent.add(btnAddChild);
        lytMainContent.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER,
                btnAddChild);
    }

    /**
     * build child classes grid, add to main layout and set listeners
     */
    private void buildChildClasessGrid() {
        tblChildClass = new Grid<>();
        tblChildClass.setHeightFull();
        tblChildClass.setWidthFull();
        tblChildClass.setSelectionMode(Grid.SelectionMode.MULTI);
        Grid.Column<ClassMetadataLightCustom> childClassNameColumn = tblChildClass.addComponentColumn(element ->{
            Label childName = new Label(element.toString());
                if(element.getClassMetadataLight().isAbstract())
                    childName.setClassName("abstract-class-text");
                return childName;
            });
        childrenClassDataprovider();

        // First filter 
        Icon icon = VaadinIcon.SEARCH.create();
        icon.setSize("16px");
        icon.setClassName("icon-filter");
        TextField txtFilterChildClassNameField = new TextField(ts.getTranslatedString("module.general.labels.filter"),
                ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtFilterChildClassNameField.setWidthFull();
        txtFilterChildClassNameField.setPlaceholder(ts.getTranslatedString("module.containmentman.textField.filter.placeholder"));
        txtFilterChildClassNameField.setValueChangeMode(ValueChangeMode.EAGER);
        txtFilterChildClassNameField.setSuffixComponent(icon);
        txtFilterChildClassNameField.addValueChangeListener(event -> {
            childClassesDataProvider.addFilter(
                    element -> {
                        return StringUtils.containsIgnoreCase(
                                element.getName(), txtFilterChildClassNameField.getValue())
                        || StringUtils.containsIgnoreCase(
                                element.getClassMetadataLight().getDisplayName(), txtFilterChildClassNameField.getValue()
                        );
                    }
            );
        });

        HeaderRow filterRow = tblChildClass.appendHeaderRow();
        filterRow.getCell(childClassNameColumn).setComponent(txtFilterChildClassNameField);

        lytMainContent.add(tblChildClass);
    }

    /**
     * create children class dataprovider
     */
    private void childrenClassDataprovider() {
        childClassesDataProvider = new ListDataProvider<>(getClasessAndAbstractClones());
        tblChildClass.setDataProvider(childClassesDataProvider);
    }

    /**
     * Search all child of root node, and change id, this to admit display
     * similar sub item related to different parent node.
     *
     * @param parent;ClassMetadataLightCustom;root node to which will search
     * childrens
     * @return childrensFound; List<ClassMetadataLightCustom>; childrens found
     */
    private List<ClassMetadataLightCustom> getPosibleChildrenClones(ClassMetadataLightCustom parent) {
        List<ClassMetadataLightCustom> childrensFound = new ArrayList<>();
        if (containmentHierarchyType) {
            try {
                List<ClassMetadataLight> tempClassesLight = mem.getPossibleChildrenNoRecursive(parent.getName());
                childrensFound = tempClassesLight.stream()
                        .map(item -> new ClassMetadataLightCustom(item, true))
                        .collect(Collectors.toList());
            } catch (MetadataObjectNotFoundException ex) {
                Logger.getLogger(ContainmentManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        } else {
            try {
                List<ClassMetadataLight> tempClassesLight = mem.getPossibleSpecialChildrenNoRecursive(parent.getName());
                childrensFound = tempClassesLight.stream()
                        .map(item -> new ClassMetadataLightCustom(item, true))
                        .collect(Collectors.toList());
            } catch (MetadataObjectNotFoundException ex) {
                Logger.getLogger(ContainmentManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                        AbstractNotification.NotificationType.ERROR, ts).open();
            }
        }
        return childrensFound;
    }

    /**
     * Search all not abstract child node subclasses, package in new class this
     * to admit display similar sub item related to different parent node.
     *
     * @param parent;ClassMetadataLightCustom;root node to which will search
     * childrens
     * @return childrensFound; List<ClassMetadataLightCustom>; childrens found
     */
    private List<ClassMetadataLightCustom> getSubClassMetadataClones(ClassMetadataLightCustom parent) {
        List<ClassMetadataLightCustom> subclassesFound = new ArrayList<>();

        List<ClassMetadataLight> tempClassesLight;
        try {
            tempClassesLight = mem.getSubClassesLight(parent.getName(), false, false);
            subclassesFound = tempClassesLight.stream()
                    .map(item -> new ClassMetadataLightCustom(item, false))
                    .collect(Collectors.toList());
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(ContainmentManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }

        return subclassesFound;
    }

    /**
     * search all root node, and change id, this to admit display similar root
     * node
     *
     * @return parentClasses;List<ClassMetadataLightCustom>; root nodes found
     */
    private List<ClassMetadataLightCustom> getClasessClones() {
        parentClasses = new ArrayList<>();
        try {
            List<ClassMetadataLight> tempClassesLight = mem.getAllClassesLight(false, false);
            parentClasses = tempClassesLight.stream()
                    .filter(element -> !element.isAbstract())
                    .map(item -> new ClassMetadataLightCustom(item, true))
                    .sorted(Comparator.comparing(ClassMetadataLightCustom::toString))
                    .collect(Collectors.toList());
        } catch (MetadataObjectNotFoundException ex) {
            Logger.getLogger(ContainmentManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return parentClasses;
    }

    /**
     * all classes with abstract classes, right grid
     *
     * @return allClassesLight;List<ClassMetadataLightCustom>; all classes found
     */
    private List<ClassMetadataLightCustom> getClasessAndAbstractClones() {
        List<ClassMetadataLightCustom> allClassesLight = new ArrayList<>();
        try {
            List<ClassMetadataLight> tempClassesLight = mem.getAllClassesLight(false, false);
            allClassesLight = tempClassesLight.stream()
                    .map(item -> new ClassMetadataLightCustom(item, true))
                    .sorted(Comparator.comparing(ClassMetadataLightCustom::toString))
                    .collect(Collectors.toList());
        } catch (MetadataObjectNotFoundException ex) {
            Logger.getLogger(ContainmentManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return allClassesLight;
    }

    /**
     * Add new child event
     *
     * @param event;ClickEvent<Button>; button click event
     */
    private void addNewChild(ClickEvent<Button> event) {
        if (cbxFilterClassNameField.getValue() == null) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ts.getTranslatedString("module.containmentman.error.actions.new-child.right-no-selected"),
                    AbstractNotification.NotificationType.ERROR, ts).open();

        } else if (tblChildClass.getSelectedItems() == null || tblChildClass.getSelectedItems().isEmpty()) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    containmentHierarchyType ? ts.getTranslatedString("module.containmentman.error.actions.new-child.right-no-selected")
                            : ts.getTranslatedString("module.containmentman.error.actions.new-special-child.right-no-selected"),
                    AbstractNotification.NotificationType.ERROR, ts).open();

        } else {
            ClassMetadataLightCustom parentClass = cbxFilterClassNameField.getValue();
            List<ClassMetadataLightCustom> selectedItems = new ArrayList(tblChildClass.getSelectedItems());
            if (containmentHierarchyType) {
                try {
                    mem.addPossibleChildren(parentClass.getName(),
                            selectedItems.stream().map(element -> element.getName()).toArray(String[]::new));
                    refreshParentClass(cbxFilterClassNameField.getValue());

                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            tblChildClass.getSelectedItems().size() > 1
                            ? ts.getTranslatedString("module.containmentman.successful.actions.new-children")
                            : ts.getTranslatedString("module.containmentman.successful.actions.new-child"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
                    Logger.getLogger(ContainmentManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            } else {
                try {
                    mem.addPossibleSpecialChildren(parentClass.getName(),
                            selectedItems.stream().map(element -> element.getName()).toArray(String[]::new));
                    refreshParentClass(cbxFilterClassNameField.getValue());

                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            tblChildClass.getSelectedItems().size() > 1
                            ? ts.getTranslatedString("module.containmentman.successful.actions.new-special-children")
                            : ts.getTranslatedString("module.containmentman.successful.actions.new-special-child"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
                    Logger.getLogger(ContainmentManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            }
        }

    }    
    
    /**
     * Remove existing child event
     *
     * @param event;ClickEvent<Button>; button click event
     */
    private void removeChild(ClassMetadataLightCustom classMetadataLightCustom) {
        if (cbxFilterClassNameField.getValue() == null) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    ts.getTranslatedString("module.containmentman.error.actions.new-child.right-no-selected"),
                    AbstractNotification.NotificationType.ERROR, ts).open();

        } else if (classMetadataLightCustom == null) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                    containmentHierarchyType ? ts.getTranslatedString("module.containmentman.error.actions.new-child.right-no-selected")
                            : ts.getTranslatedString("module.containmentman.error.actions.new-special-child.right-no-selected"),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        } else {
            if (containmentHierarchyType) {
                try {
                    ClassMetadataLightCustom parentClass = cbxFilterClassNameField.getValue();
                    mem.removePossibleChildren(parentClass.getClassMetadataLight().getId(),
                            Arrays.asList(classMetadataLightCustom.getClassMetadataLight().getId()).stream().mapToLong(item -> item).toArray()
                    );
                    refreshParentClass(cbxFilterClassNameField.getValue());

                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            ts.getTranslatedString("module.containmentman.successful.actions.remove-child"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                } catch (MetadataObjectNotFoundException ex) {
                    Logger.getLogger(ContainmentManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            } else {
                try {
                    ClassMetadataLightCustom parentClass = cbxFilterClassNameField.getValue();
                    mem.removePossibleSpecialChildren(parentClass.getClassMetadataLight().getId(),
                            Arrays.asList(classMetadataLightCustom.getClassMetadataLight().getId()).stream().mapToLong(item -> item).toArray()
                    );
                    refreshParentClass(cbxFilterClassNameField.getValue());

                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"),
                            ts.getTranslatedString("module.containmentman.successful.actions.remove-special-child"),
                            AbstractNotification.NotificationType.INFO, ts).open();
                } catch (MetadataObjectNotFoundException ex) {
                    Logger.getLogger(ContainmentManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage(),
                            AbstractNotification.NotificationType.ERROR, ts).open();
                }
            }
        }
    }

    /**
     * Enable selected button and fill table
     *
     * @param event
     */
    private void enableTransferButtons(AbstractField.ComponentValueChangeEvent<ComboBox<ClassMetadataLightCustom>, ClassMetadataLightCustom> event) {
        if (event.getValue() != null) {
            this.btnAddChild.setEnabled(true);
            this.btnAddChild.setClassName("primary-button button-left");            
            refreshParentClass(event.getValue());
            
            this.tblParentClass.addExpandListener(this::expandItemsRecursive);
        } else {
            tblParentClass.setItems(new ArrayList<>());
            this.btnAddChild.setEnabled(true);
            this.btnAddChild.removeClassName("primary-button button-left");
        }
    }

    /**
     * refresh ParentClass tree grid
     */
    private void refreshParentClass(ClassMetadataLightCustom classMetadataLightCustom){
        tblParentClass.setItems(getPosibleChildrenClones(classMetadataLightCustom), this::getSubClassMetadataClones);
        tblParentClass.getDataProvider().refreshAll();
    }
       
    
    private void expandItemsRecursive(ExpandEvent<ClassMetadataLightCustom, TreeGrid<ClassMetadataLightCustom>> event) {
        if (!event.isFromClient()) {
            this.tblParentClass.expandRecursively(event.getItems(), event.getItems().size());
        }
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.containmentman.title");
    }

    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

  
}

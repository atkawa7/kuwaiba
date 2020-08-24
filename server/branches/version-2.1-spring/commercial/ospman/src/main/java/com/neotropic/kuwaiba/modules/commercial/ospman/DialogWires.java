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
package com.neotropic.kuwaiba.modules.commercial.ospman;

import static com.neotropic.kuwaiba.modules.commercial.ospman.OutsidePlanService.SPECIAL_RELATIONSHIP_OSPMAN_HAS_PATH;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.persistence.PhysicalConnectionsService;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Dialog to select the parents to the wire path
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@CssImport(value = "css/custom-vaadin-dialog-overlay.css", themeFor="vaadin-dialog-overlay")
public class DialogWires extends Dialog {
    private final String ATTR_COLOR = "color"; //NOI18N
    private final String ATTR_VALUE = "value"; //NOI18N
    
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    
    private final List<BusinessObjectLight> rootContainers = new ArrayList();
    private final List<BusinessObjectLight> containers = new ArrayList();
    private final HashMap<BusinessObjectLight, List<Checkbox>> checkBoxes = new HashMap();
    
    private final HashMap<BusinessObjectLight, List<BusinessObjectLight>> parents = new HashMap();
    private final HashMap<BusinessObjectLight, List<BusinessObjectLight>> children = new HashMap();
    
    private final HashMap<BusinessObjectLight, BusinessObjectLight> endpointsA = new HashMap();
    private final HashMap<BusinessObjectLight, BusinessObjectLight> endpointsB = new HashMap();
    
    private String containerName;
    private ClassMetadataLight containerClass;
    private TemplateObjectLight containerTemplate;
    
    public DialogWires(List<BusinessObjectViewEdge> edges, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem,
        TranslationService ts) {
        
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        edges.forEach(edge -> rootContainers.add(edge.getIdentifier()));
    }
    
    @Override
    public void open() {
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        getElement().getThemeList().add("osp-70vh-70-vw");
        
        if (rootContainers.isEmpty()) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ts.getTranslatedString("module.ospman.containers.error.select-container")
            ).open();
            return;            
        }
        containers.addAll(rootContainers);
        rootContainers.forEach(rootContainer -> {
            try {
                List<BusinessObjectLight> listEndpointsA = bem.getSpecialAttribute(
                    rootContainer.getClassName(), rootContainer.getId(), 
                    PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA
                );
                List<BusinessObjectLight> listEndpointsB = bem.getSpecialAttribute(
                    rootContainer.getClassName(), rootContainer.getId(), 
                    PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB
                );
                if (!listEndpointsA.isEmpty())
                    endpointsA.put(rootContainer, listEndpointsA.get(0));
                if (!listEndpointsB.isEmpty())
                    endpointsB.put(rootContainer, listEndpointsB.get(0));
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage()
                ).open();
            }
        });
        if (!hasPath()) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ts.getTranslatedString("module.ospman.containers.path.not-continuous")
            ).open();
        } else {
            init();
            super.open();
        }
    }
    
    private void init() {
        try {
            Tab tabContainer = new Tab(ts.getTranslatedString("module.ospman.containers.info.step"));
            Tab tabPath = new Tab(ts.getTranslatedString("module.ospman.containers.path.step"));
            tabPath.setEnabled(false);
                        
            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), event -> close());
            
            Button btnPrevious = new Button(ts.getTranslatedString("module.general.messages.previous"));
            btnPrevious.setVisible(false);
            
            Button btnNext = new Button(ts.getTranslatedString("module.general.messages.next"));
            btnNext.setEnabled(false);
            
            Button btnFinish = new Button(ts.getTranslatedString("module.general.messages.finish"));
            btnFinish.setVisible(false);
            
            Component stepContainer = getContainerStep(enabled -> btnNext.setEnabled(enabled));
            Component stepPath = getPathStep();
            Div page = new Div();
            page.setWidth("100%");
            page.setHeight("80%");
            
            Tabs tabs = new Tabs(tabContainer, tabPath);
            tabs.setFlexGrowForEnclosedTabs(1);
            tabs.setAutoselect(true);
            
            btnPrevious.addClickListener(event -> {
                tabs.setSelectedTab(tabContainer);
                btnPrevious.setVisible(false);
                btnNext.setVisible(true);
                btnFinish.setVisible(false);
            });
            
            btnNext.addClickListener(event -> {
                tabPath.setEnabled(true);
                tabs.setSelectedTab(tabPath);
                btnPrevious.setVisible(true);
                btnNext.setVisible(false);
                btnFinish.setVisible(true);
            });
            
            btnFinish.addClickListener(event -> {
                if (containers.isEmpty()) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.warning"), 
                        ts.getTranslatedString("module.ospman.containers.error.select-container")
                    ).open();
                } else if (!hasPath()) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ts.getTranslatedString("module.ospman.containers.path.not-continuous")
                    ).open();
                } else {
                    try {
                        BusinessObjectLight newContainer = null;
                        for (int i = 0; i < containers.size(); i++) {
                            if (i == 0) {
                                HashMap<String, String> attrs = new HashMap();
                                attrs.put(Constants.PROPERTY_NAME, containerName);
                                
                                String containerId = bem.createSpecialObject(containerClass.getName(), 
                                    containers.get(0).getClassName(), containers.get(0).getId(), attrs, 
                                    containerTemplate != null ? containerTemplate.getId() : null);
                                
                                newContainer = bem.getObjectLight(containerClass.getName(), containerId);
                            } else {
                                bem.addSpecialObjectToObject(
                                    newContainer.getClassName(), newContainer.getId(), 
                                    containers.get(i).getClassName(), containers.get(i).getId()
                                );
                            }
                            List<BusinessObjectLight> roots = getRoots(containers.get(i));
                            for (BusinessObjectLight root : roots) {
                                bem.createSpecialRelationship(
                                    newContainer.getClassName(), newContainer.getId(), 
                                    root.getClassName(), root.getId(), 
                                    SPECIAL_RELATIONSHIP_OSPMAN_HAS_PATH, true
                                );
                            }
                        }
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.information"), 
                            ts.getTranslatedString("module.ospman.container.created-successfully")
                        ).open();
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage()
                        ).open();
                    }
                    close();
                }
            });
            
            tabs.addSelectedChangeListener(event -> {
                page.removeAll();
                if (tabContainer.equals(event.getSelectedTab()))
                    page.add(stepContainer);
                if (tabPath.equals(event.getSelectedTab()))
                    page.add(stepPath);
            });
            tabs.setSelectedTab(tabContainer);
            page.add(stepContainer);
            
            HorizontalLayout lytButtons = new HorizontalLayout(
                    btnCancel, btnPrevious, btnNext, btnFinish);
            
            VerticalLayout lyt = new VerticalLayout(tabs, page, lytButtons);
            lyt.setMargin(false);
            lyt.setPadding(false);
            lyt.setSizeFull();
            lyt.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, lytButtons);
            add(lyt);
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage()
            ).open();
        }
    }
    
    private Component getContainerStep(Consumer<Boolean> consumerEnabledNextButton) 
        throws MetadataObjectNotFoundException, InvalidArgumentException {
        
        TextField txtContainerName = new TextField();
        txtContainerName.setLabel(ts.getTranslatedString("module.ospman.containers.info.name"));
        txtContainerName.setRequired(true);
        
        ComboBox<ClassMetadataLight> cmbContainerClass = new ComboBox();
        cmbContainerClass.setItemLabelGenerator(item -> 
            item.getDisplayName() != null && !item.getDisplayName().isEmpty() ? item.getDisplayName() : item.getName());
        cmbContainerClass.setItems(mem.getSubClassesLight(Constants.CLASS_GENERICPHYSICALCONTAINER, false, false));
        cmbContainerClass.setLabel(ts.getTranslatedString("module.ospman.containers.info.class"));
        cmbContainerClass.setRequired(true);
        cmbContainerClass.setEnabled(false);
        
        ComboBox<TemplateObjectLight> cmbContainerTemplate = new ComboBox();
        cmbContainerTemplate.setItemLabelGenerator(TemplateObjectLight::getName);
        cmbContainerTemplate.setLabel(ts.getTranslatedString("module.ospman.containers.info.template"));
        cmbContainerTemplate.setEnabled(false);
        
        txtContainerName.addValueChangeListener(event -> {
            containerName = event.getValue();
            boolean enabled = containerName != null && !containerName.isEmpty();
            cmbContainerClass.setEnabled(enabled);
            if (!enabled) {
                cmbContainerTemplate.setEnabled(enabled);
                consumerEnabledNextButton.accept(enabled);
            }
        });
        
        cmbContainerClass.addValueChangeListener(event -> {
            containerClass = event.getValue();
            boolean enabled = containerClass != null;
            cmbContainerTemplate.setEnabled(enabled);
            cmbContainerTemplate.setValue(null);
            consumerEnabledNextButton.accept(enabled);
            if (enabled) {
                try {
                    cmbContainerTemplate.setItems(aem.getTemplatesForClass(containerClass.getName()));
                } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getMessage()
                    ).open();
                    cmbContainerTemplate.setEnabled(false);
                }
            }
        });
        
        cmbContainerTemplate.addValueChangeListener(event -> containerTemplate = event.getValue());
        // Building the container step content
        FormLayout formLayout = new FormLayout(txtContainerName, cmbContainerClass, cmbContainerTemplate);
        
        VerticalLayout lytHorizontalCenter = new VerticalLayout(formLayout);
        lytHorizontalCenter.setSpacing(false);
        lytHorizontalCenter.setMargin(false);
        lytHorizontalCenter.setPadding(false);
        lytHorizontalCenter.setWidthFull();
        lytHorizontalCenter.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, formLayout);
        
        HorizontalLayout lytVerticalCenter = new HorizontalLayout(lytHorizontalCenter);
        lytVerticalCenter.setSpacing(false);
        lytVerticalCenter.setMargin(false);
        lytVerticalCenter.setPadding(false);
        lytVerticalCenter.setSizeFull();
        lytVerticalCenter.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, lytHorizontalCenter);
        
        return lytVerticalCenter;
    }
    
    public Component getPathStep() {
        TreeGrid<BusinessObjectItem> treeGrid = new TreeGrid();
        treeGrid.setSizeFull();
        treeGrid.addThemeVariants(
            GridVariant.LUMO_NO_BORDER, 
            GridVariant.LUMO_NO_ROW_BORDERS, 
            GridVariant.LUMO_COMPACT
        );
        List<BusinessObjectItem> items = new ArrayList();
        rootContainers.forEach(rootContainer -> items.add(new BusinessObjectItem(rootContainer)));
        treeGrid.setItems(items, parent -> {
            try {
                List<BusinessObjectLight> specialChildren = bem.getSpecialChildrenOfClassLight(
                    parent.getBusinessObject().getId(), parent.getBusinessObject().getClassName(), Constants.CLASS_GENERICPHYSICALCONTAINER, -1);
                specialChildren.forEach(child -> {
                    if (!parents.containsKey(child))
                        parents.put(child, new ArrayList());
                    parents.get(child).add(parent.getBusinessObject());
                });
                children.put(parent.getBusinessObject(), specialChildren);
                
                List<BusinessObjectItem> itemChildren = new ArrayList();
                specialChildren.forEach(specialChild -> itemChildren.add(new BusinessObjectItem(specialChild)));
                return itemChildren;
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), //NOI18N
                    ex.getLocalizedMessage()
                ).open();
            }
            return null;
        });
        
        treeGrid.addComponentColumn(item -> {
            Checkbox chkItem = new Checkbox();
            chkItem.addValueChangeListener(event -> {
                if (event.isFromClient()) {
                    unselectRecursively();
                    List<BusinessObjectLight> removeContainers = new ArrayList();
                    
                    getRoots(item.getBusinessObject()).forEach(root -> {
                        containers.forEach(container -> {
                            getRoots(container).forEach(containerRoot -> {
                                if (root.getId() != null && root.getId().equals(containerRoot.getId()))
                                    removeContainers.add(container);
                            });
                        });
                    });
                    containers.removeAll(removeContainers);
                    if (event.getValue()) {
                        if (!containers.contains(item.getBusinessObject()))
                            containers.add(item.getBusinessObject());
                    }
                    containers.forEach(container -> {
                        if (checkBoxes.containsKey(container)) {
                            checkBoxes.get(container).forEach(checkBox -> {
                                // Preventing update the same value
                                if (!checkBox.getValue().equals(Boolean.TRUE))
                                    checkBox.setValue(Boolean.TRUE);
                            });
                        }
                    });
                }
            });
            if (!checkBoxes.containsKey(item.getBusinessObject()))
                checkBoxes.put(item.getBusinessObject(), new ArrayList());
            checkBoxes.get(item.getBusinessObject()).add(chkItem);
            
            chkItem.setValue(containers.contains(item.getBusinessObject()));
            // Note: It is necessary to add the check box to a layout so that 
            // the tree does not delay calculating the new size of the check box
            // when it is selected or unselected
            VerticalLayout lyt = new VerticalLayout(chkItem);
            return lyt;
        }).setFlexGrow(0).setAutoWidth(true);
        
        treeGrid.addComponentHierarchyColumn(item -> getComponentHierarchyColumn(item.getBusinessObject()))
            .setHeader(ts.getTranslatedString("module.ospman.containers.containers"));
        
        treeGrid.addComponentColumn(item -> {
            if (rootContainers.contains(item.getBusinessObject()) && endpointsA.containsKey(item.getBusinessObject()))
                return getColumnComponent(endpointsA.get(item.getBusinessObject()));
            return new Div();
        }).setHeader(ts.getTranslatedString("module.ospman.containers.endpointa"));
        
        treeGrid.addComponentColumn(item -> {
            if (rootContainers.contains(item.getBusinessObject()) && endpointsB.containsKey(item.getBusinessObject()))
                return getColumnComponent(endpointsB.get(item.getBusinessObject()));
            return new Div();
        }).setHeader(ts.getTranslatedString("module.ospman.containers.endpointb"));
        
        return treeGrid;
    }
    
    private HorizontalLayout getComponentHierarchyColumn(BusinessObjectLight item) {
        HorizontalLayout lytItem = new HorizontalLayout();
        try {
            if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONTAINER, item.getClassName())) {
                ClassMetadata itemClass = mem.getClass(item.getClassName());
                if (itemClass.hasAttribute(ATTR_COLOR)) {
                    ClassMetadata colorClass = mem.getClass(itemClass.getType(ATTR_COLOR));
                    if (colorClass.hasAttribute(ATTR_VALUE)) {
                        BusinessObject itemObject = bem.getObject(item.getClassName(), item.getId());
                        String colorId = (String) itemObject.getAttributes().get(ATTR_COLOR);
                        if (colorId != null) {
                            BusinessObject colorObject = aem.getListTypeItem(itemClass.getType(ATTR_COLOR), colorId);
                            String colorValue = (String) colorObject.getAttributes().get(ATTR_VALUE);
                            if (colorValue != null) {
                                Icon icon = new Icon(VaadinIcon.CIRCLE);
                                icon.getStyle().set(colorId, colorId);
                                icon.setColor(colorValue);
                                lytItem.add(icon);
                            }
                        }
                    }
                }
            }
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), //NOI18N
                ex.getLocalizedMessage()
            ).open();
        }
        lytItem.add(new Label(item.getName()));
        return lytItem;
    }
    
    private VerticalLayout getColumnComponent(BusinessObjectLight businessObject) {
        VerticalLayout lyt = new VerticalLayout();
        lyt.setMargin(false);
        lyt.setPadding(false);
        lyt.setSpacing(false);
        
        Label lblName = new Label(businessObject.getName());
        lyt.add(lblName);
        return lyt;
    }
    
    private List<BusinessObjectLight> getRoots(BusinessObjectLight container) {
        List<BusinessObjectLight> roots = new ArrayList();
        rootContainers.forEach(rootContainer -> {
            if (isParent(rootContainer, container))
                roots.add(rootContainer);
        });
        return roots;
    }
    
    private boolean isParent(BusinessObjectLight parent, BusinessObjectLight container) {
        if (parent != null && container != null && parent.getId() != null) {
            if (parent.getId().equals(container.getId()))
                return true;
            if (children.containsKey(parent))
                for (BusinessObjectLight child : children.get(parent)) {
                    if (isParent(child, container))
                        return true;
                    isParent(child, container);
                }
        }
        return false;
    }
    /**
     * Unselect all the items recursively
     */
    private void unselectRecursively(BusinessObjectLight root) {
        if (root == null)
            return;
        if (checkBoxes.containsKey(root)) {
            checkBoxes.get(root).forEach(checkBox -> {
                // Preventing new calls to the method select container
                if (!checkBox.getValue().equals(Boolean.FALSE))
                    checkBox.setValue(Boolean.FALSE);
            });
        }
        if (children.containsKey(root))
            children.get(root).forEach(child -> unselectRecursively(child));
    }
    /**
     * Unselect all the items recursively
     */
    private void unselectRecursively() {
        rootContainers.forEach(root -> unselectRecursively(root));
    }
    
    private boolean hasPath() {
        List<BusinessObjectLight> edges = new ArrayList();
        containers.forEach(container -> edges.addAll(getRoots(container)));
        
        if (edges.size() == 1)
            return true;
        
        List<BusinessObjectLight> nodes = new ArrayList();
        edges.forEach(edge -> {
            BusinessObjectLight nodeA = endpointsA.get(edge);
            BusinessObjectLight nodeB = endpointsB.get(edge);

            if (!nodes.contains(nodeA))
                nodes.add(nodeA);

            if (!nodes.contains(nodeB))
                nodes.add(nodeB);
        });
        /**
         * Graph
         * 
         * (n1)--e1--(n2)--e2--(n3)
         * 
         * Graph Matrix
         * 
         * +-------------+--------------+
         * | edges\nodes | n1 | n2 | n3 |
         * +-------------+--------------+
         * | e1          | 1  | 1  | 0  |
         * +-------------+--------------+
         * | e2          | 0  | 1  | 1  |
         * +-------------+--------------+
         *   Sum           1    2    1
         * 
         * It is a valid path if the sum of each column is not major that two
         * the number of one is two and the number of two must be equal to the 
         * number of edges minus one
         */
        List<List<Integer>> matrix = new ArrayList();
        // Initializing to zero the cells
        edges.forEach(edge -> {
            List<Integer> row = new ArrayList();
            nodes.forEach(node -> row.add(0));
            matrix.add(row);
        });
        // Building the graph matrix
        edges.forEach(edge -> {
            BusinessObjectLight nodeA = endpointsA.get(edge);
            BusinessObjectLight nodeB = endpointsB.get(edge);

            int iEdge = edges.indexOf(edge);
            int jNodeA = nodes.indexOf(nodeA);
            int jNodeB = nodes.indexOf(nodeB);
            matrix.get(iEdge).set(jNodeA, 1);
            matrix.get(iEdge).set(jNodeB, 1);
        });
        // Cheking it is a valid path
        List<Integer> sums = new ArrayList();
        for (int j = 0; j < nodes.size(); j++) {
            int sum = 0;
            for (int i = 0; i < edges.size(); i++)                
                sum += matrix.get(i).get(j);
            if (sum == 0 || sum > 2)
                return false;
            sums.add(sum);
        }
        /*
        // For the purpose of testing only
        matrix.forEach(row -> {
            row.forEach(cell -> System.out.print(" " + cell));
            System.out.println("");
        });
        sums.forEach(sum -> System.out.print(" " + sum));
        System.out.println("\n");
        */
        int sumOne = 0;
        int sumTwo = 0;
        for (int sum : sums) {
            if (sum == 1)
                sumOne++;
            if (sum == 2)
                sumTwo++;
        }
        // In a path must exist two one and edges minus one edges
        if (sumOne == 2 && sumTwo == edges.size() - 1)
            return true;
        
        return false;
    }
    /**
     * Business Object Light wrapper
     */
    private class BusinessObjectItem {
        private BusinessObjectLight businessObject;
        
        public BusinessObjectItem(BusinessObjectLight businessObject) {
            this.businessObject = businessObject;
        }
        
        public BusinessObjectLight getBusinessObject() {
            return businessObject;
        }
    }
}

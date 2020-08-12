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

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Emphasis;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import elemental.json.Json;
import elemental.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.persistence.PhysicalConnectionsService;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DialogWires extends Dialog {
    
    public DialogWires(List<BusinessObjectViewEdge> edges, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem,
        TranslationService ts) {
        
//        VerticalLayout lytWires = new VerticalLayout();
//        lytWires.setWidthFull();
        TreeGrid<WireElement> treeGrid = new TreeGrid();
        treeGrid.setWidth("50%");
        treeGrid.addThemeVariants(
            GridVariant.LUMO_NO_BORDER, 
            GridVariant.LUMO_NO_ROW_BORDERS, 
            GridVariant.LUMO_COMPACT
        );
        String template = new StringBuilder()
            .append("<vaadin-grid-tree-toggle leaf='[[item.leaf]]' expanded='{{expanded}}' level='[[level]]'>") //NOI18N
            .append(    "<vaadin-vertical-layout>") //NOI18N
            .append(        "<vaadin-horizontal-layout theme='spacing'>") //NOI18N            
            .append(            "<template is='dom-if' if='[[item.icon]]'>") //NOI18N
            .append(                "<iron-icon icon='vaadin:[[item.icon.icon]]' style='color:[[item.icon.color]];height:[[item.icon.height]];width:[[item.icon.width]]'></iron-icon>") //NOI18N
            .append(            "</template>") //NOI18N
            .append(            "<label>[[item.name]]</label>") //NOI18N
            .append(        "</vaadin-horizontal-layout>") //NOI18N
            .append(    "</vaadin-vertical-layout>") //NOI18N
            .append("</vaadin-grid-tree-toggle>") //NOI18N
            .toString();
        TemplateRenderer<WireElement> templateRenderer = TemplateRenderer.<WireElement> of (template);
        templateRenderer.withProperty("leaf", item -> item.getLeaf()); //NOI18N
        templateRenderer.withProperty("name", item -> item.getBusinessObject().getName()); //NOI18N
        templateRenderer.withProperty("icon", item -> { //NOI18N
            if (item.getIcon() != null) {
                return item.getIcon().toJsonObject();
            }
            return null;
        });
        List<WireElement> roots = new ArrayList();
        for (BusinessObjectViewEdge edge : edges)
            roots.add(new WireElement(edge.getIdentifier(), aem, bem, mem));
        
        treeGrid.addColumn(templateRenderer).setHeader(ts.getTranslatedString("module.ospman.containers.containers"));
//        treeGrid.addColumn(item -> item.getClassDisplayName()).setHeader(ts.getTranslatedString("module.ospman.containers.type"));
        treeGrid.addComponentColumn(item -> {
            if (roots.contains(item)) {
                try {
                    List<BusinessObjectLight> endpointsA = bem.getSpecialAttribute(
                        item.getBusinessObject().getClassName(), item.getBusinessObject().getId(), 
                        PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA
                    );
                    if (!endpointsA.isEmpty())
                        return getColumnComponent(endpointsA.get(0), mem);
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage()
                    );
                }
            }
            return new Div();
        }).setHeader(ts.getTranslatedString("module.ospman.containers.endpointa"));
        treeGrid.addComponentColumn(item -> {
            if (roots.contains(item)) {
                try {
                    List<BusinessObjectLight> endpointsB = bem.getSpecialAttribute(
                        item.getBusinessObject().getClassName(), item.getBusinessObject().getId(), 
                        PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB
                    );
                    if (!endpointsB.isEmpty())
                        return getColumnComponent(endpointsB.get(0), mem);
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage()
                    );
                }
            }
            return new Div();
        }).setHeader(ts.getTranslatedString("module.ospman.containers.endpointb"));        
        treeGrid.setDataProvider(getDataProvider(new WireElementService(roots, aem, bem, mem, ts)));
        treeGrid.setSelectionMode(Grid.SelectionMode.MULTI);
//        lytWires.add(treeGrid);
        
        Grid<BusinessObjectLight> tbl = new Grid();
        tbl.setWidth("50%");
        tbl.addColumn(item -> item.getName()).setHeader(ts.getTranslatedString("module.ospman.containers.containers"));
        tbl.addColumn(item -> item.getClassName()).setHeader(ts.getTranslatedString("module.ospman.containers.type"));
        treeGrid.asMultiSelect().addValueChangeListener(event -> {
            List<BusinessObjectLight> tblItems = new ArrayList();
            event.getValue().forEach(wireElement -> tblItems.add(wireElement.getBusinessObject()));
            tbl.setItems(tblItems);
        });
        //setWidthFull();
//        setResizable(true)
        //getElement().getStyle().set("width", "70%");
        //setWidth("calc(100vw - (4*var(--lumo-space-m)))");
        //setResizable(true);
        HorizontalLayout lyt = new HorizontalLayout();
        //lyt.setWidth("70%");
        lyt.setSpacing(false);
        lyt.setPadding(false);
        lyt.setMargin(false);
        lyt.setSizeFull();
        lyt.add(treeGrid);
        lyt.add(tbl);
//        setWidth("70vw");
        //setHeight("calc(100vh - (2*var(--lumo-space-m)))");
        //setWidth("calc(100vw - (4*var(--lumo-space-m)))");
        /*
dialog.getElement().getNode().addAttachListener(() -> {
  dialog.getElement().executeJs("this.$.overlay.$.overlay.style[$0]=$1", ElementConstants.STYLE_MAX_HEIGHT, "95vh");
  dialog.getElement().executeJs("this.$.overlay.$.overlay.style[$0]=$1", ElementConstants.STYLE_MAX_WIDTH, "95vw");
});
         */
//        getElement().getNode().addAttachListener(() -> {
//            getElement().executeJs("this.$.overlay.$.overlay.style[$0]=$1", "width", "70vw");
//        });
        //setWidth("100vw + browser");        
        add(lyt);
        //getElement().executeJs("vaadin.forceLayout()");
        //add(lytWires);
        //add(tbl);
        
    }
    
    private VerticalLayout getColumnComponent(BusinessObjectLight businessObject, MetadataEntityManager mem) throws MetadataObjectNotFoundException {
        VerticalLayout lyt = new VerticalLayout();
        lyt.setMargin(false);
        lyt.setPadding(false);
        lyt.setSpacing(false);
        
        Label lblName = new Label(businessObject.getName());
        
//        ClassMetadata classMetadata = mem.getClass(businessObject.getClassName());
//        Emphasis empClass = new Emphasis(classMetadata.getDisplayName() != null && !classMetadata.getDisplayName().isEmpty() ? 
//            classMetadata.getDisplayName() : classMetadata.getName());
        
        lyt.add(lblName);
//        lyt.add(empClass);
        return lyt;
    }
    
    private HierarchicalDataProvider getDataProvider(WireElementService wireElementService) {
        return new AbstractBackEndHierarchicalDataProvider<WireElement, Void>() {
            @Override
            protected Stream<WireElement> fetchChildrenFromBackEnd(HierarchicalQuery<WireElement, Void> query) {
                return wireElementService.fetchChildrenFromBackEnd(query.getParent()).stream();
            }

            @Override
            public int getChildCount(HierarchicalQuery<WireElement, Void> query) {
                return wireElementService.getChildCount(query.getParent());
            }

            @Override
            public boolean hasChildren(WireElement item) {
                return wireElementService.hasChildren(item);
            }
        };
    }
    
    private class WireElementService {
        private final List<WireElement> children = new ArrayList();
        
        private final List<WireElement> roots;
        private final ApplicationEntityManager aem;
        private final BusinessEntityManager bem;
        private final MetadataEntityManager mem;
        private final TranslationService ts;
        
        public WireElementService(List<WireElement> roots, 
            ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem,
            TranslationService ts) {
            
            this.roots = roots;
            this.aem = aem;
            this.bem = bem;
            this.mem = mem;
            this.ts = ts;
        }
        
        public int getChildCount(WireElement parent) {
            if (parent == null)
                return roots.size();
            return children.size();
        }
        
        public List<WireElement> fetchChildrenFromBackEnd(WireElement parent) {
            if (parent == null)
                return roots;
            return children;
        }
        
        public boolean hasChildren(WireElement item) {
            children.clear();
            try {
                List<BusinessObjectLight> specialChildren = bem.getObjectSpecialChildren(
                    item.getBusinessObject().getClassName(), 
                    item.getBusinessObject().getId()
                );
                for (BusinessObjectLight specialChild : specialChildren) {
                    if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONTAINER, specialChild.getClassName()))
                        children.add(new WireElement(specialChild, aem, bem, mem));
                }
                return !children.isEmpty();
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ex.getLocalizedMessage()
                ).open();
                return false;
            }
        }
    }
    
    private class WireElement {
        private static final String ATTR_COLOR = "color"; //NOI18N
        private static final String ATTR_VALUE = "value"; //NOI18N
        private final BusinessObjectLight businessObject;
        private WireIcon wireIcon;
        private Boolean leaf;
        private String classDisplayName;
        
        public WireElement(BusinessObjectLight businessObject, 
            ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem) {
            this.businessObject = businessObject;
            
            try {
                if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONTAINER, businessObject.getClassName()))
                    wireIcon = new WireIcon(VaadinIcon.CIRCLE);
                
                if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, businessObject.getClassName()))
                    wireIcon = new WireIcon(VaadinIcon.CIRCLE);
                
                ClassMetadata physicalClass = mem.getClass(businessObject.getClassName());
                classDisplayName = physicalClass.getDisplayName() != null && !physicalClass.getDisplayName().isEmpty() ? 
                    physicalClass.getDisplayName() : physicalClass.getName();
                
                if (wireIcon != null) {
                    BusinessObject physicalObject = bem.getObject(businessObject.getClassName(), businessObject.getId());

                    if (physicalObject.getAttributes().containsKey(ATTR_COLOR) && physicalClass.hasAttribute(ATTR_COLOR)) {
                        BusinessObject colorObject = aem.getListTypeItem(
                            physicalClass.getAttribute(ATTR_COLOR).getType(),
                            (String) physicalObject.getAttributes().get(ATTR_COLOR)
                        );
                        if (colorObject.getAttributes().containsKey(ATTR_VALUE))
                            wireIcon.setColor((String) colorObject.getAttributes().get(ATTR_VALUE));
                    }
                    if (wireIcon.getColor() == null)
                        wireIcon.setVaadinIcon(null);
                }
            } catch (InventoryException ex) {
                
            }
        }
        
        public BusinessObjectLight getBusinessObject() {
            return businessObject;
        }
        
        public String getClassDisplayName() {
            return classDisplayName;
        }
        
        public Boolean getLeaf() {
            return leaf;
        }
        
        public void setLeaf(boolean leaf) {
            this.leaf = leaf;
        }
        
        public WireIcon getIcon() {
            return wireIcon;
        }
    }
    
    private class WireIcon {
        private VaadinIcon vaadinIcon;
        private String color;
        private String width = "20px";
        private String height = "20px";
        
        public WireIcon(VaadinIcon vaadinIcon) {
            this.vaadinIcon = vaadinIcon;
        }
        
        public String getColor() {
            return color;
        }
        
        public void setColor(String color) {
            this.color = color;
        }
        
        public String getHeight() {
            return height;
        }
        
        public void setHeight(String height) {
            this.height = height;
        }
        
        public String getWidth() {
            return width;
        }
        
        public void setWidthr(String width) {
            this.width = width;
        }
        
        public VaadinIcon getVaadinIcon() {
            return vaadinIcon;
        }
        
        public void setVaadinIcon(VaadinIcon vaadinIcon) {
            this.vaadinIcon = vaadinIcon;
        }
        
        public JsonObject toJsonObject() {
            JsonObject jsonObject = Json.createObject();
            if (vaadinIcon != null)
                jsonObject.put("icon", vaadinIcon.name().toLowerCase(Locale.ENGLISH).replace('_', '-')); //NOI18N
            if (color != null)
                jsonObject.put("color", color); //NOI18N
            if (width != null)
                jsonObject.put("width", width); //NOI18N
            if (height != null)
                jsonObject.put("height", height); //NOI18N
            if (jsonObject.keys().length != 0)
                return jsonObject;
            return null;
        }
    }
}

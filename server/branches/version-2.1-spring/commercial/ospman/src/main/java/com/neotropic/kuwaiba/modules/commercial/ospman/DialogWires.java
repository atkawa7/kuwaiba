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

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import java.util.ArrayList;
import java.util.List;
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
 * Dialog to select the parents to the wire path
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@CssImport(value = "css/custom-vaadin-dialog-overlay.css", themeFor="vaadin-dialog-overlay")
public class DialogWires extends Dialog {
    private final String ATTR_COLOR = "color";
    private final String ATTR_VALUE = "value";
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final TranslationService ts;
    
    public DialogWires(List<BusinessObjectViewEdge> edges, 
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem,
        TranslationService ts) {
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        getElement().getThemeList().add("osp-width-70-vw");
        
        TreeGrid<BusinessObjectLight> treeGrid = new TreeGrid();
        treeGrid.setWidth("50vw");
        treeGrid.addThemeVariants(
            GridVariant.LUMO_NO_BORDER, 
            GridVariant.LUMO_NO_ROW_BORDERS, 
            GridVariant.LUMO_COMPACT
        );
        treeGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        List<BusinessObjectLight> roots = new ArrayList();
        edges.forEach(edge -> roots.add(edge.getIdentifier()));
        
        treeGrid.setItems(roots, item -> {
            try {
                return bem.getSpecialChildrenOfClassLight(item.getId(), item.getClassName(), Constants.CLASS_GENERICPHYSICALCONTAINER, -1);
            } catch (InventoryException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), //NOI18N
                    ex.getLocalizedMessage()
                ).open();
            }
            return null;
        });
        
        treeGrid.addComponentHierarchyColumn(item -> getComponentHierarchyColumn(item))
            .setHeader(ts.getTranslatedString("module.ospman.containers.containers"));
        
        treeGrid.addComponentColumn(item -> {
            if (roots.contains(item)) {
                try {
                    List<BusinessObjectLight> endpointsA = bem.getSpecialAttribute(
                        item.getClassName(), item.getId(), 
                        PhysicalConnectionsService.RELATIONSHIP_ENDPOINTA
                    );
                    if (!endpointsA.isEmpty())
                        return getColumnComponent(endpointsA.get(0));
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
                        item.getClassName(), item.getId(), 
                        PhysicalConnectionsService.RELATIONSHIP_ENDPOINTB
                    );
                    if (!endpointsB.isEmpty())
                        return getColumnComponent(endpointsB.get(0));
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage()
                    );
                }
            }
            return new Div();
        }).setHeader(ts.getTranslatedString("module.ospman.containers.endpointb"));        
        
        Grid<BusinessObjectLight> tbl = new Grid();
        tbl.setWidth("50vw");
        tbl.addColumn(item -> item.getName()).setHeader(ts.getTranslatedString("module.ospman.containers.containers"));
        tbl.addColumn(item -> item.getClassName()).setHeader(ts.getTranslatedString("module.ospman.containers.type"));
        treeGrid.asMultiSelect().addValueChangeListener(event -> {
            tbl.setItems(event.getValue());
        });
        
        HorizontalLayout lyt = new HorizontalLayout();
        lyt.setPadding(false);
        lyt.setMargin(false);
        lyt.setSizeFull();
        lyt.add(treeGrid);
        lyt.add(tbl);
        
        add(lyt);
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
    
    private VerticalLayout getColumnComponent(BusinessObjectLight businessObject) throws MetadataObjectNotFoundException {
        VerticalLayout lyt = new VerticalLayout();
        lyt.setMargin(false);
        lyt.setPadding(false);
        lyt.setSpacing(false);
        
        Label lblName = new Label(businessObject.getName());
        lyt.add(lblName);
        return lyt;
    }
}

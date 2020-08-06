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
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DialogWires extends Dialog {
    
    public DialogWires(List<BusinessObjectViewEdge> edges, BusinessEntityManager bem, TranslationService ts) {
        VerticalLayout lytWires = new VerticalLayout();
        TreeGrid<WireElement> treeGrid = new TreeGrid();
        treeGrid.setMinWidth("300px");
        treeGrid.addThemeVariants(
            GridVariant.LUMO_NO_BORDER, 
            GridVariant.LUMO_NO_ROW_BORDERS, 
            GridVariant.LUMO_COMPACT
        );
        String template = new StringBuilder()
            .append("<vaadin-grid-tree-toggle leaf='[[item.leaf]]' expanded='{{expanded}}' level='[[level]]'>") //NOI18N
            .append("<p>[[item.name]]</p>")
            //.append("<template is='dom-if' if='[[item.leaf]]'>Leaf</template>") 
            //.append("<template is='dom-if' if='[[!item.leaf]]'>!Leaf</template>")
            .append("</vaadin-grid-tree-toggle>")
            .toString();
        TemplateRenderer<WireElement> templateRenderer = TemplateRenderer.<WireElement> of (template);
        templateRenderer.withProperty("leaf", item -> item.getLeaf());
        templateRenderer.withProperty("name", item -> item.getBusinessObject().getName());
        treeGrid.addColumn(templateRenderer);
        List<WireElement> roots = new ArrayList();
        for (BusinessObjectViewEdge edge : edges)
            roots.add(new WireElement(edge.getIdentifier()));
        treeGrid.setDataProvider(getDataProvider(new WireElementService(roots, bem, ts)));
        lytWires.add(treeGrid);
        add(lytWires);
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
        private final BusinessEntityManager bem;
        private final TranslationService ts;
        
        public WireElementService(List<WireElement> roots, BusinessEntityManager bem, TranslationService ts) {
            this.roots = roots;
            this.bem = bem;
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
                for (BusinessObjectLight specialChild : specialChildren)
                    children.add(new WireElement(specialChild));
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
        private final BusinessObjectLight businessObject;
        private Boolean leaf;
                
        public WireElement(BusinessObjectLight businessObject) {
            this.businessObject = businessObject;
        }
        
        public BusinessObjectLight getBusinessObject() {
            return businessObject;
        }
        
        public Boolean getLeaf() {
            return leaf;
        }
        
        public void setLeaf(boolean leaf) {
            this.leaf = leaf;
        }
    }
}

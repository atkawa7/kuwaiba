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
package org.neotropic.kuwaiba.modules.core.navigation.navtree;

import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.server.StreamResourceRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.nodes.InventoryObjectNode;
import org.neotropic.util.visual.icons.IconGenerator;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 * @param <T>
 */
public class NavigationTree<T extends InventoryObjectNode> extends TreeGrid<T> {
    public NavigationTree(HierarchicalDataProvider<T, Void> dataProvider, IconGenerator<T> iconGenerator) {
        String template = new StringBuilder()
            .append("<vaadin-grid-tree-toggle")
            .append(" leaf='[[item.leaf]]'")
            .append(" expanded='{{expanded}}'")
            .append(" level='[[level]]'")
            .append(">")
            .append("<img")
            .append(" width='15px'")
            .append(" height='15px'")
            .append(" src='[[item.icon]]'")
            .append(">")
            .append("<p style='margin: 0 0 0 10px;'>[[item.name]] <span class=\"text-secondary\">[[item.class]]</span></p>")
            .append("</vaadin-grid-tree-toggle>")
            .toString();
        TemplateRenderer<T> renderer = TemplateRenderer.<T> of (template);
        renderer.withProperty("leaf", item -> false);
        renderer.withProperty("icon", item -> StreamResourceRegistry.getURI(iconGenerator.apply(item)).toString());
        renderer.withProperty("name", item -> item.getObject().getName());
        renderer.withProperty("class", item -> Constants.DUMMY_ROOT.equals(item.getObject().getClassName()) ? "" : item.getObject().getClassName());

        addColumn(renderer);
        addThemeVariants(
            GridVariant.LUMO_NO_BORDER, 
            GridVariant.LUMO_NO_ROW_BORDERS, 
            GridVariant.LUMO_COMPACT
        );
        setDataProvider(dataProvider);
    }
}

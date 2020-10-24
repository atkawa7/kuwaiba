/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.core.configuration.validators;

import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.server.StreamResourceRegistry;
import org.neotropic.kuwaiba.core.configuration.nodes.ClassNode;
import org.neotropic.kuwaiba.core.configuration.nodes.ValidatorDefinitionNode;
import org.neotropic.util.visual.icons.IconGenerator;
import org.neotropic.util.visual.tree.nodes.AbstractNode;

/**
 * A tree that extends the features of the Tree Grid and makes use of the Nodes API
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class ValidatorDefinitionTree extends TreeGrid<AbstractNode> {
    
    public ValidatorDefinitionTree(HierarchicalDataProvider<AbstractNode, Void> dataProvider, IconGenerator<AbstractNode> iconGenerator) {
        StringBuilder template = new StringBuilder();
        template.append("<vaadin-grid-tree-toggle ");
        template.append("leaf='[[item.leaf]]' ");
        template.append("expanded='{{expanded}}' ");
        template.append("level='[[level]]'");
        template.append(">");
        
        template.append("<img ");
        template.append("width='15px' ");
        template.append("height='15px' ");
        template.append("src='[[item.icon]]' ");
        template.append("alt='' ");
        template.append(">");
        
        template.append("<p style=\"margin:0 0 0 10px;\">[[item.name]]</p>");
        
        template.append("</vaadin-grid-tree-toggle>");
        
        TemplateRenderer<AbstractNode> renderer = TemplateRenderer.<AbstractNode>of(template.toString());
        renderer.withProperty("leaf", item -> item instanceof ValidatorDefinitionNode);
        renderer.withProperty("icon", item -> {
            if (item instanceof ClassNode) {
                return StreamResourceRegistry.getURI(iconGenerator.apply(item)).toString();
            }
            if (item instanceof ValidatorDefinitionNode) {
                return "img/validators.png";
            }
            return null;
        });
        renderer.withProperty("name", item -> item.getClassName());
        
        addColumn(renderer);
        addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);
        setDataProvider(dataProvider);
    }
    
}

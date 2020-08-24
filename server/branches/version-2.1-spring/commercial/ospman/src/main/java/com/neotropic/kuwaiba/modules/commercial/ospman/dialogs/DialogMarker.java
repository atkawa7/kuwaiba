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
package com.neotropic.kuwaiba.modules.commercial.ospman.dialogs;

import com.neotropic.flow.component.paperdialog.PaperDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * The dialog to show markers information
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DialogMarker extends PaperDialog {
    private final String WIDTH = "350px";
    private final int MIN_FILTER_LENGTH = 3;    
    private LinkedHashMap<String, AbstractViewNode> nodesMap = new LinkedHashMap();
    
    public DialogMarker(Component positionTarget, 
        ApplicationEntityManager aem, 
        BusinessEntityManager bem,
        MetadataEntityManager mem, TranslationService ts, 
        List<AbstractViewNode> nodes, Consumer<BusinessObjectLight> consumerAddMarker) {
        
        Objects.requireNonNull(positionTarget);
        Objects.requireNonNull(nodes);
        setNodesMap(nodes);
        
        positionTarget(positionTarget);
        setNoOverlap(true);
        setHorizontalAlign(PaperDialog.HorizontalAlign.LEFT);
        setVerticalAlign(PaperDialog.VerticalAlign.TOP);
        setMargin(false);
        setMaxWidth(WIDTH);
        setMinWidth(WIDTH);
        
        TextField txtFilter = new TextField();
        txtFilter.focus();
        txtFilter.setMinWidth(WIDTH);
        txtFilter.setMaxWidth(WIDTH);
        txtFilter.setValueChangeMode(ValueChangeMode.EAGER);
        txtFilter.setClearButtonVisible(true);
        txtFilter.setPlaceholder(ts.getTranslatedString("module.general.messages.search"));
        txtFilter.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        add(txtFilter);
        
        Grid<BusinessObjectLight> grid = new Grid();
        grid.setVisible(false);
        grid.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
        grid.setMaxWidth(WIDTH);
        grid.setMinWidth(WIDTH);
        grid.addComponentColumn(item -> new ComponentColumnMarker(ts, mem, item, nodesMap.get(item.getId()), this, consumerAddMarker));
        add(grid);
        
        txtFilter.getStyle().set("margin", "0px"); //NOI18N
        txtFilter.getStyle().set("padding", "0px"); //NOI18N
        
        grid.getStyle().set("margin", "0px"); //NOI18N
        grid.getStyle().set("padding", "0px"); //NOI18N
        
        txtFilter.addValueChangeListener(event -> {
            if (event.getValue().length() >= MIN_FILTER_LENGTH) {
                List<BusinessObjectLight> objects = bem.getSuggestedObjectsWithFilter(event.getValue(), Constants.CLASS_VIEWABLEOBJECT, 10);
                if (!objects.isEmpty()) {
                    grid.setItems(objects);
                    grid.setVisible(true);
                    return;
                }
            }
            grid.setItems(Collections.EMPTY_LIST);
            grid.setVisible(false);
        });
    }
    
    private void setNodesMap(List<AbstractViewNode> nodes) {
        for (AbstractViewNode<BusinessObjectLight> node : nodes)
            nodesMap.put(node.getIdentifier().getId(), node);
    }
}

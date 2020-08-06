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

import com.neotropic.flow.component.paperdialog.PaperDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Emphasis;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MarkerFilter extends VerticalLayout {
    private final int MIN_FILTER_LENGTH = 3;
    private LinkedHashMap<String, AbstractViewNode> nodesMap = new LinkedHashMap();
    
    public MarkerFilter(TranslationService ts, BusinessEntityManager bem, 
        List<AbstractViewNode> nodes) {
        
        Objects.requireNonNull(nodes);
        setNodesMap(nodes);
        setPadding(false);
        setMargin(false);
        setSpacing(false);
        
        TextField txtDummyFilter = new TextField();
        txtDummyFilter.setPlaceholder(ts.getTranslatedString("module.general.messages.search"));
        txtDummyFilter.setWidth("400px");
        txtDummyFilter.setPrefixComponent(new Icon(VaadinIcon.MAP_MARKER));
        add(txtDummyFilter);
        
        PaperDialog paperDialog = new PaperDialog();
        paperDialog.setHorizontalAlign(PaperDialog.HorizontalAlign.LEFT);
        paperDialog.setVerticalAlign(PaperDialog.VerticalAlign.TOP);
        paperDialog.setMargin(false);
        paperDialog.positionTarget(this);
        paperDialog.setWidth(txtDummyFilter.getWidth());
        
        TextField txtFilter = new TextField();
        txtFilter.setWidth("350px");
        txtFilter.setValueChangeMode(ValueChangeMode.EAGER);
        txtFilter.setClearButtonVisible(true);
        txtFilter.setPlaceholder(ts.getTranslatedString("module.general.messages.search"));
        txtFilter.setPrefixComponent(new Icon(VaadinIcon.MAP_MARKER));
        
        add(paperDialog);
        paperDialog.add(txtFilter);
        
        this.addClickListener(event -> {
            paperDialog.open();
        });
        
        Grid<BusinessObjectLight> grid = new Grid();
        //paperDialog.add(grid);
        
        grid.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
        grid.setMaxWidth("350px");
        grid.setMinWidth("350px");
        grid.addComponentColumn(item -> new MarkerComponent(ts, item, nodesMap.get(item.getId()), paperDialog, txtFilter));
        grid.addSelectionListener(selectionEvent -> {
            if (selectionEvent.getFirstSelectedItem().isPresent()) {
                BusinessObjectLight businessObject = selectionEvent.getFirstSelectedItem().get();
                if (nodesMap.containsKey(businessObject.getId())) {

                }
            }
        });
    }
    
    private void setNodesMap(List<AbstractViewNode> nodes) {
        for (AbstractViewNode<BusinessObjectLight> node : nodes)
            nodesMap.put(node.getIdentifier().getId(), node);
    }
    
    private class MarkerComponent extends HorizontalLayout {
        public MarkerComponent(
            TranslationService ts, 
            BusinessObjectLight businessObject, 
            AbstractViewNode<BusinessObjectLight> node, 
            PaperDialog paperDialog, 
            TextField txtFilter) {
            
            Objects.requireNonNull(paperDialog);
            Objects.requireNonNull(txtFilter);
            
            setPadding(false);
            setMargin(false);
            
            Icon iconMarker = new Icon(VaadinIcon.MAP_MARKER);
            
            VerticalLayout lyt = new VerticalLayout();
            lyt.setMargin(false);
            lyt.setPadding(false);
            lyt.setSpacing(false);
            
            Label lblName = new Label(businessObject.getName());
            Emphasis empClass = new Emphasis(businessObject.getClassName());
            
            lyt.add(lblName, empClass);
            
            Button btnAdd = new Button(new Icon(VaadinIcon.PLUS));
            paperDialog.dialogConfirm(btnAdd);
            btnAdd.addClickListener(event -> {
                txtFilter.setValue(businessObject.getName());
            });
            if (node != null) {
                iconMarker.setColor("#E74C3C");
                btnAdd.setVisible(false);
            }
            else
                iconMarker.setColor("#737373");
            
            add(iconMarker, lyt, btnAdd);
            setJustifyContentMode(FlexComponent.JustifyContentMode.START);
            setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        }
    }
}

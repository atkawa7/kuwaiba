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
package com.neotropic.kuwaiba.modules.commercial.mpls.tools;

import com.neotropic.flow.component.paperdialog.PaperDialog;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Emphasis;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 *
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class MplsSearch extends Div {
    public MplsSearch(BusinessEntityManager bem, List<Object> markers) {
        TextField txtSearch = new TextField();
        txtSearch.setWidth("400px");
        txtSearch.setValueChangeMode(ValueChangeMode.EAGER);
        txtSearch.setClearButtonVisible(true);
        txtSearch.setPlaceholder("Search");
        Button btnSearch = new Button(new Icon(VaadinIcon.SEARCH));
        txtSearch.setPrefixComponent(btnSearch);
        
        PaperDialog paperDialog = new PaperDialog();
        paperDialog.setNoOverlap(true);
        paperDialog.setHorizontalAlign(PaperDialog.HorizontalAlign.LEFT);
        paperDialog.setVerticalAlign(PaperDialog.VerticalAlign.TOP);
        paperDialog.setMargin(false);
        paperDialog.positionTarget(txtSearch);
        paperDialog.setWidth(txtSearch.getWidth());
        
        add(txtSearch);
        add(paperDialog);
        
        txtSearch.addValueChangeListener(event -> {
            paperDialog.removeAll();
            if (event.isFromClient()) {
                try {
                    List<BusinessObjectLight> objects = bem.getObjectsOfClassLight(event.getValue(), -1);
                    if (!objects.isEmpty()) {
                        Grid<BusinessObjectLight> grid = new Grid();
                        grid.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS);
                        grid.setMaxWidth("350px");
                        grid.setWidth("350px");
                        grid.setItems(objects);
                        grid.addColumn(new ComponentRenderer<>(obj -> {
                            HorizontalLayout hly = new HorizontalLayout();
                            hly.setMargin(false);
                            hly.setPadding(false);
                            Icon icon = new Icon(VaadinIcon.MAP_MARKER);
                            icon.setColor("#737373");
                            
                            VerticalLayout vlyObj = new VerticalLayout();
                            vlyObj.setMargin(false);
                            vlyObj.setPadding(false);
                            vlyObj.setSpacing(false);

                            Label lblObjName = new Label(obj.getName());
                            Emphasis emObjClass = new Emphasis(obj.getClassName());

                            vlyObj.add(lblObjName, emObjClass);

                            Button btnAdd = new Button(new Icon(VaadinIcon.PLUS));
                            paperDialog.dialogConfirm(btnAdd);
                            btnAdd.addClickListener(clickEvent -> {
                                txtSearch.setValue(obj.getName());      
                                fireEvent(new NewEvent(this, false, obj));
                            });
                            hly.add(icon, vlyObj, btnAdd);
                            hly.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
                            hly.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
                            
                            if (getObject(markers, obj) != null) {
                                icon.setColor("#E74C3C");
                                btnAdd.setVisible(false);
                            }
                            return hly;
                        }));
                        grid.addSelectionListener(selectionEvent -> {
//                            Object ospNode = getObject(markers, selectionEvent.getFirstSelectedItem().get());
//                            if (ospNode != null) {
//                                txtSearch.setValue(ospNode.getBusinessObject().getName());      
//                                fireEvent(new SelectionEvent(this, false, ospNode));
//                            }
                        });
                        paperDialog.add(grid);
                        paperDialog.open();
                    }          
                } catch (Exception ex) {
    //                ex.printStackTrace();
                }
            }
        });
    }
    private final Object getObject(List<Object> markers, BusinessObjectLight obj) {
        Object ospNode = null;
        for (Object marker : markers) {
//            if (obj.equals(marker.getBusinessObject())) {
//                ospNode = marker;
//                break;
//            }
        }
        return ospNode;
    }
    public Registration addSelectionListener(ComponentEventListener<SelectionEvent> listener) {
        return addListener(SelectionEvent.class, listener);
    }
    public Registration addNewListener(ComponentEventListener<NewEvent> listener) {
        return addListener(NewEvent.class, listener);
    }
    public class SelectionEvent extends ComponentEvent<MplsSearch> {
        private final Object ospNode;
        
        public SelectionEvent(MplsSearch source, boolean fromClient, Object ospNode) {
            super(source, fromClient);
            this.ospNode = ospNode;
        }
        public Object getOspNode() {
            return ospNode;
        }
    }
    public class NewEvent extends ComponentEvent<MplsSearch> {
        private final BusinessObjectLight object;
        
        public NewEvent(MplsSearch source, boolean fromClient, BusinessObjectLight object) {
            super(source, fromClient);
            this.object = object;
        }
        
        public BusinessObjectLight getObject() {
            return object;
        }
    }
}

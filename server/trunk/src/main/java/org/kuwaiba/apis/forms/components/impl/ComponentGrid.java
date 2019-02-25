/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.apis.forms.components.impl;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import org.kuwaiba.apis.forms.elements.EventDescriptor;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.ElementColumn;
import org.kuwaiba.apis.forms.elements.ElementGrid;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.TextRenderer;
import com.vaadin.ui.themes.ValoTheme;
import elemental.json.Json;
import elemental.json.JsonValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.vaadin.teemusa.gridextensions.paging.PagedDataProvider;
import org.vaadin.teemusa.gridextensions.paging.PagingControls;

/**
 * Vaadin Implementation to an ElementGrid to the API Form
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentGrid extends GraphicalComponent {
    private class IndexedHashMap<V, K> extends HashMap<V, K> {
        private long index;
        
        public IndexedHashMap(long index) {
            this.index = index;
        }
        
        public long getIndex() {
            return index;
        }
        
        public void setIndex(long index) {
            this.index = index;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + (int) (this.index ^ (this.index >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final IndexedHashMap<?, ?> other = (IndexedHashMap<?, ?>) obj;
            if (this.index != other.index) {
                return false;
            }
            return true;
        }
    }
    
    private final List<HashMap<String, Object>> rows = new ArrayList();

    private Grid<HashMap<String, Object>> mainGrid;
    private VerticalLayout paginationButtons;
    private boolean usePagination;
  
    public ComponentGrid() {        
        //super(new Grid<HashMap<String, Object>>());
        super(new VerticalLayout());
        mainGrid = new Grid<HashMap<String, Object>>();
        mainGrid.setStyleName(ValoTheme.TABLE_SMALL);
        paginationButtons = new VerticalLayout();
        getComponent().addComponent(mainGrid);
        getComponent().addComponent(paginationButtons);
        //paginationButtons.setHeight(25, Sizeable.Unit.PIXELS);
        paginationButtons.setSpacing(false);
        paginationButtons.setMargin(false);
        getComponent().setSpacing(false);
        getComponent().setMargin(false);
        usePagination = false;
    }
    
    @Override
    public VerticalLayout getComponent() {
        return (VerticalLayout) super.getComponent();
    }
    
    private class ComponentGridTextRenderer extends TextRenderer {
        
        public ComponentGridTextRenderer() {            
        }
        
        @Override
        public JsonValue encode(Object value) {
            if (value == null) {
                return super.encode(null);
            }
            else if (value instanceof RemoteObjectLight) {
                return Json.create(((RemoteObjectLight) value).getName());
            }
            else if (value instanceof String) {                
                return Json.create((String) value);
            }
            else {
                return Json.create(value.toString());
            }
        }
    }

    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementGrid) {
            ElementGrid grid = (ElementGrid) element;
            getComponent().setId("contenedorBtnPag");
            if (grid.getSelectionMode() != null) {
                switch(grid.getSelectionMode()) {
                    case ElementGrid.SELECTION_MODE_MULTI:
                        mainGrid.setSelectionMode(Grid.SelectionMode.MULTI);
                    break;
                    case ElementGrid.SELECTION_MODE_NONE:
                        mainGrid.setSelectionMode(Grid.SelectionMode.NONE);
                    break;
                    case ElementGrid.SELECTION_MODE_SINGLE:
                        mainGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
                    break;
                    default:
                        mainGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
                    break;
                }
            }
            
            if(grid.isUsePagination())
                usePagination = true;
            
            if (grid.getColums() != null) {
                for (ElementColumn column : grid.getColums()) {
                    Grid.Column gridColumn = mainGrid.addColumn(row -> row.get(column.getCaption())).setCaption(column.getCaption());
                    gridColumn.setRenderer(new ComponentGridTextRenderer());
                }
            }
            if (grid.getRows() != null) {
                updateRows(grid);
                
            }
            if (grid.getWidth() != null)
                mainGrid.setWidth(grid.getWidth());
            if (grid.getHeight() != null)
                mainGrid.setHeight(grid.getHeight());
            
            if (!grid.isEnabled()) {
                mainGrid.setSelectionMode(Grid.SelectionMode.NONE);
                return;
            }
                                    
            mainGrid.addSelectionListener(new SelectionListener() {
                @Override
                public void selectionChange(SelectionEvent event) {
                    
                    if (ElementGrid.SELECTION_MODE_MULTI.equals(grid.getSelectionMode())) {
                        if (event.getAllSelectedItems() != null) {
                            List<Long> selectedRows = new ArrayList();
                            
                            if (!event.getAllSelectedItems().isEmpty()) {
                                for (Object selectedItem : event.getAllSelectedItems()) {
                                    if (selectedItem instanceof IndexedHashMap) {
                                        IndexedHashMap selectedRow = (IndexedHashMap) selectedItem;
                                        selectedRows.add(selectedRow.getIndex());
                                    }
                                }
                            }
                            else {
                                selectedRows = Collections.EMPTY_LIST;
                            }
                            fireComponentEvent(new EventDescriptor(
                                Constants.EventAttribute.ONPROPERTYCHANGE, 
                                Constants.Property.SELECTED_ROWS, 
                                selectedRows, 
                                null));
                        }                       
                    }
                    else {                    
                        long idSelectRow = -1;

                        if (!event.getFirstSelectedItem().equals(Optional.empty())) {

                            Object selectedItem = event.getFirstSelectedItem().get();

                            if (selectedItem instanceof IndexedHashMap) {
                                IndexedHashMap selectedRow = (IndexedHashMap) selectedItem;
                                idSelectRow = selectedRow.getIndex();
                            }
                        }                        
                        fireComponentEvent(new EventDescriptor(
                            Constants.EventAttribute.ONPROPERTYCHANGE, 
                            Constants.Property.SELECTED_ROW, 
                            idSelectRow, -1));
                    }
                }
            });
            
        }
    }
        
    private void updateRows(ElementGrid grid) {
                        
        List<ElementColumn> columns = grid.getColums();
        
        List<List<Object>> gridRows = grid.getRows();
        
        if (gridRows != null) {
            
            rows.clear();
                        
            for (int i = 0; i < gridRows.size(); i += 1) {
                
                IndexedHashMap<String, Object> row = new IndexedHashMap(i);
                List<Object> gridRow = gridRows.get(i);
                
                for (int j = 0; j < gridRow.size(); j += 1)
                    row.put(columns.get(j).getCaption(), gridRow.get(j));
                
                rows.add(row);
            }
            mainGrid.setItems(Collections.EMPTY_LIST);
            mainGrid.setItems(rows);
            
            if(usePagination){
                PagedDataProvider<HashMap<String, Object>, SerializablePredicate<HashMap<String, Object>>> dataProvider = new PagedDataProvider<HashMap<String, Object>, SerializablePredicate<HashMap<String, Object>>>(
                    DataProvider.ofCollection(getRows()));
                mainGrid.setDataProvider(dataProvider);
                PagingControls pagingControls = dataProvider.getPagingControls();

                HorizontalLayout pages = new HorizontalLayout();
                pages.setSpacing(false);
                pages.setMargin(false);

                pagingControls.setPageLength(10);
                pages.setCaption("");
                Button firstBtn = new Button("First", e -> pagingControls.setPageNumber(0));
                Button previousBtn = new Button("Previous", e -> pagingControls.previousPage());
                Button nextBtn = new Button("Next", e -> pagingControls.nextPage());
                Button lastBtn = new Button("Last", e -> pagingControls.setPageNumber(pagingControls.getPageCount() - 1));
                firstBtn.setStyleName(ValoTheme.BUTTON_SMALL);
                previousBtn.setStyleName(ValoTheme.BUTTON_SMALL);
                nextBtn.setStyleName(ValoTheme.BUTTON_SMALL);
                lastBtn.setStyleName(ValoTheme.BUTTON_SMALL);
                pages.addComponent(firstBtn);
                pages.addComponent(previousBtn);
                pages.addComponent(nextBtn);
                pages.addComponent(lastBtn);
                VerticalLayout controls = new VerticalLayout();
                controls.setSpacing(false);
                controls.setMargin(false);
                controls.addComponent(pages);
                controls.setWidth("100%");
                controls.setComponentAlignment(pages, Alignment.BOTTOM_CENTER);
                paginationButtons.addComponent(controls);
                paginationButtons.setComponentAlignment(controls, Alignment.MIDDLE_CENTER);
                paginationButtons.setSpacing(false);
                paginationButtons.setMargin(false);
            }
        }
    }
    
    public List<HashMap<String, Object>> getRows() {
        return rows;
    }
    
    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            
            if (Constants.Property.ROWS.equals(event.getPropertyName())) {
                ElementGrid grid = (ElementGrid) getComponentEventListener();
                updateRows(grid);
            }
        }
    }
    
}

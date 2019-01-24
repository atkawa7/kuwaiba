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

import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.event.selection.SelectionListener;
import org.kuwaiba.apis.forms.elements.EventDescriptor;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.ElementColumn;
import org.kuwaiba.apis.forms.elements.ElementGrid;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.TextRenderer;
import elemental.json.Json;
import elemental.json.JsonValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

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
    
    public ComponentGrid() {        
        super(new Grid<HashMap<String, Object>>());
    }
    
    @Override
    public Grid<HashMap<String, Object>> getComponent() {
        return (Grid<HashMap<String, Object>>) super.getComponent();
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
            
            if (grid.getColums() != null) {
                for (ElementColumn column : grid.getColums()) {
                    Grid.Column gridColumn = getComponent().addColumn(row -> row.get(column.getCaption())).setCaption(column.getCaption());
                    gridColumn.setRenderer(new ComponentGridTextRenderer());
                }
            }
            if (grid.getRows() != null) {
                                
                updateRows(grid);
            }
            if (grid.getWidth() != null)
                getComponent().setWidth(grid.getWidth());
            if (grid.getHeight() != null)
                getComponent().setHeight(grid.getHeight());
            
            if (!grid.isEnabled()) {
                getComponent().setSelectionMode(Grid.SelectionMode.NONE);
                return;
            }
                                    
            getComponent().addSelectionListener(new SelectionListener() {
                @Override
                public void selectionChange(SelectionEvent event) {
                    long idSelectRow = -1;
                    
                    if (!event.getFirstSelectedItem().equals(Optional.empty())) {
                        
                        Object selectedItem = event.getFirstSelectedItem().get();
                        
                        if (selectedItem instanceof IndexedHashMap) {
                            IndexedHashMap selectedRow = (IndexedHashMap) selectedItem;
                            idSelectRow = selectedRow.getIndex();
                        }
                    }
                    if (event.isUserOriginated()) {
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
            getComponent().setItems(Collections.EMPTY_LIST);
            getComponent().setItems(rows);
        }
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

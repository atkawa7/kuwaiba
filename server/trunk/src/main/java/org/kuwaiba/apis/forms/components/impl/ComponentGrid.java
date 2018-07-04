/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
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
    
    private final List<HashMap<String, String>> rows = new ArrayList();
    
    public ComponentGrid() {        
        super(new Grid<HashMap<String, String>>());
    }
    
    @Override
    public Grid<HashMap<String, String>> getComponent() {
        return (Grid<HashMap<String, String>>) super.getComponent();
    }

    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementGrid) {
            ElementGrid grid = (ElementGrid) element;
            
            if (grid.getColums() != null) {
                for (ElementColumn column : grid.getColums())
                    getComponent().addColumn(row -> row.get(column.getCaption())).setCaption(column.getCaption());                
            }
            if (grid.getRows() != null) {
                
                List<List<Object>> gridRows = grid.getRows();
                
                for (List<Object> gridRow : gridRows) {
                    
                    List<ElementColumn> columns = grid.getColums();
                    
                    List<String> strRow = new ArrayList();
                    
                    for (Object data : gridRow)
                        strRow.add(data.toString());
                                        
                    List<String> values = strRow;
                    
                    addRow(values, columns);
                }
            }
            if (grid.getWidth() != null)
                getComponent().setWidth(grid.getWidth());
            if (grid.getHeight() != null)
                getComponent().setHeight(grid.getHeight());
            
            getComponent().addSelectionListener(new SelectionListener() {
                @Override
                public void selectionChange(SelectionEvent event) {
                    int i = 0;
                }
            });
        }
    }
    
    private void addRow(List<String> values, List<ElementColumn> columns) {
        
        if (values.size() == columns.size()) {
            
            IndexedHashMap<String, String> row = new IndexedHashMap(rows.size());

            for (int i = 0; i < values.size(); i += 1)
                row.put(columns.get(i).getCaption(), values.get(i));

            rows.add(row);
            getComponent().setItems(rows);
        }
    }
    
    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            if (Constants.Property.ROWS.equals(event.getPropertyName())) {
                if (event.getNewValue() instanceof List) {
                    
                    ElementGrid grid = (ElementGrid) getComponentEventListener();
                    
                    List<ElementColumn> columns = grid.getColums();
                    List<String> values = (List<String>) event.getNewValue();
                    
                    addRow(values, columns);
                }
            }
        }
    }
    
}

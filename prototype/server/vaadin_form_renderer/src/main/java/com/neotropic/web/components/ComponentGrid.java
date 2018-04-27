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
package com.neotropic.web.components;

import com.neotropic.api.forms.EventDescriptor;
import com.neotropic.api.forms.AbstractElement;
import com.neotropic.api.forms.Constants;
import com.neotropic.api.forms.ElementColumn;
import com.neotropic.api.forms.ElementGrid;
import com.vaadin.ui.Grid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentGrid extends GraphicalComponent {
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
            if (grid.getWidth() != null)
                getComponent().setWidth(grid.getWidth());
            if (grid.getHeight() != null)
                getComponent().setHeight(grid.getHeight());
        }
        /*
        Grid<HashMap<String, String>> gridComponent = new Grid<>();
        childComponent = gridComponent;

        ((Grid) childComponent).setSizeFull();

        ElementGrid grid = (ElementGrid) childElement;
        if (grid.getColums() != null) {
            for (ElementColumn column : grid.getColums())
                gridComponent.addColumn(row -> row.get(column.getCaption())).setCaption(column.getCaption());
////                        ((Grid) childComponent).addColumn(column.getCaption());
        }
        */
    }
    
    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            if (Constants.Property.ROWS.equals(event.getPropertyName())) {
                if (event.getNewValue() instanceof List) {
                    
                    ElementGrid grid = (ElementGrid) getComponentEventListener();
                    
                    List<ElementColumn> columns = grid.getColums();
                    List<String> values = (List<String>) event.getNewValue();
                    
                    if (values.size() == columns.size()) {
                        
                        HashMap<String, String> row = new HashMap();
                        
                        for (int i = 0; i < values.size(); i += 1)
                            row.put(columns.get(i).getCaption(), values.get(i));
                        
                        rows.add(row);
                        getComponent().setItems(rows);
                    }
                }
            }
        }
    }
    
    /*
                                        List<String> params = functions.get(addGridRow);

                                        if (params != null) {
                                            String subformId = params.get(0);
                                            String elementId = params.get(1);

                                            ElementGrid elementGrid = (ElementGrid) getElement(elementId);
                                            Grid<HashMap<String, String>> grid = (Grid) getComponent(elementId);

                                            if (grid != null) {

                                                int ncolumns = grid.getColumns().size();
                                                String [] values = new String [ncolumns];

                                                for (int i = 0; i < ncolumns; i += 1) {
                                                    String paramId = params.get(i + 2);
                                                    Component component = getComponent(paramId);
                                                    if (component != null && component instanceof AbstractField) {
                                                        Object value = ((AbstractField) component).getValue();
                                                        if (value != null)
                                                            values[i] = value.toString();
                                                        else
                                                            values[i] = "no set";
                                                    } else {
                                                        values[i] = "no set";
                                                    }
                                                }

                                                HashMap<String, String> columnValues = new HashMap<>();

                                                List<ElementColumn> columns = elementGrid.getColums();

                                                for (int i = 0; i < ncolumns; i+= 1)
                                                    columnValues.put(columns.get(i).getCaption(), values[i]);

                                                List lst = new ArrayList();
                                                lst.add(columnValues);

                                                grid.setItems(lst);
                                                closeWindow(subformId);
                                            }
                                        }
                                    }
    */    
}

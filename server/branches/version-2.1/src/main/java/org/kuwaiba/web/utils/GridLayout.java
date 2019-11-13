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
package org.kuwaiba.web.utils;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is an approximation to Vaadin 8's Gridlayout on Vaadin 14
 * @author Jalbersson Guillermo Plazas {@literal <jalbersson.plazas@kuwaiba.org>}
 */
public class GridLayout extends VerticalLayout{
    /**
     * The total rows that will be used on the Layout. Each row has a list to hold its columns
     */
    private List<GridLayoutRow> rows;
    
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Default Constructor
     * @param columns
     * @param rows 
     */
     public GridLayout(int columns, int rows) {
        if(columns > 0 && rows > 0) {
            setHeightFull();
            setWidthFull();
            
            // Addition of rows and columns
            setRows(new ArrayList<>());
            for(int i = 0; i < rows; i++){
                GridLayoutRow row = new GridLayoutRow();
                for(int j = 0; j< columns; j++){
                    row.getColumns().add(new VerticalLayout());
                }
                getRows().add(row);
            }
            for(GridLayoutRow row : getRows())
                add(row);
        }
    }
    //</editor-fold>
   
     //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
     public List<GridLayoutRow> getRows() {
        return rows;
    }

    public void setRows(List<GridLayoutRow> rows) {
        this.rows = rows;
    }
    //</editor-fold>

}

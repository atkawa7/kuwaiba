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

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * This class supports GridLayout
 * @author Jalbersson Guillermo Plazas {@literal <jalbersson.plazas@kuwaiba.org>}
 */
public class GridLayoutRow extends HorizontalLayout{
    /**
     * This can allow custom columns on each row of the Grid Layout
     */
    List<VerticalLayout> columns;

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    public GridLayoutRow() {
        setColumns(new ArrayList<>());
        setWidthFull(); 
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public List<VerticalLayout> getColumns() {
        return columns;
    }

    public void setColumns(List<VerticalLayout> columns) {
        this.columns = columns;
    }
    //</editor-fold>

}

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

import com.neotropic.api.forms.AbstractElement;
import com.neotropic.api.forms.ElementColumn;
import com.neotropic.api.forms.ElementGrid;
import com.vaadin.ui.Grid;
import java.util.HashMap;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentGrid extends Grid<HashMap<String, String>> implements GraphicalComponent {

    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementGrid) {
            ElementGrid grid = (ElementGrid) element;
            
            if (grid.getColums() != null) {
                for (ElementColumn column : grid.getColums())
                    addColumn(row -> row.get(column.getCaption())).setCaption(column.getCaption());
            }
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
    public void elementChange(ChangeDescriptor changeDecriptor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setComponentEventListener(ComponentEventListener componentEventListener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ComponentEventListener getComponentEventListener() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void fireComponentEvent(EventDescriptor eventDescriptor) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package com.neotropic.vaadin14.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;

/**
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Tag("mx-graph")
@JsModule("./mx-graph/mx-graph.js")
public class MxGraph extends Component {
    private static final String PROPERTY_GRID = "grid";
    private static final String PROPERTY_WIDTH = "width";
    private static final String PROPERTY_HEIGHT = "height";
    
    public MxGraph() {
    }
    
     public String getGrid() {
        return getElement().getProperty(PROPERTY_GRID);
    }
        
    public void setGrid(String grid) {
        getElement().setProperty(PROPERTY_GRID, grid);
    }
    
    public String getWidth() {
        return getElement().getProperty(PROPERTY_WIDTH);
    }
        
    public void setWidth(String prop) {
        getElement().setProperty(PROPERTY_WIDTH, prop);
    }
    
    public String getHeight() {
        return getElement().getProperty(PROPERTY_HEIGHT);
    }
        
    public void setHeight(String prop) {
        getElement().setProperty(PROPERTY_HEIGHT, prop);
    }
       
    public Registration addClickEdgeListener(ComponentEventListener<MxGraphClickEdgeEvent> clickEdgeListener) {
        return super.addListener(MxGraphClickEdgeEvent.class, clickEdgeListener);
    }
    
    public void addCell(MxGraphCell mxGraphCell) {
        getElement().appendChild(mxGraphCell.getElement());     
    }
}

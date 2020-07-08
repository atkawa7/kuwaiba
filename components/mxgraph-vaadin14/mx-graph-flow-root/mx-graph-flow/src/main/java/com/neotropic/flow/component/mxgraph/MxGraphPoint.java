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
package com.neotropic.flow.component.mxgraph;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

/**
 * 
 * @author Orlando Paz Duarte {@literal <orlando.paz@kuwaiba.org>}
 */
@Tag("mx-graph-point")
@JsModule("./mx-graph/mx-graph-point.js")
public class MxGraphPoint extends Component {
    private static final String PROPERTY_UUID = "uuid";   
    private static final String PROPERTY_X = "x";
    private static final String PROPERTY_Y = "y";   


    
    public MxGraphPoint() {
    }
    
    public String getUuid() {
        return getElement().getProperty(PROPERTY_UUID);
    }
        
    public void setUuid(String prop) {
        getElement().setProperty(PROPERTY_UUID, prop);
    }
    
    public int getX() {
        return getElement().getProperty(PROPERTY_X, 0);
    }
        
    public void setX(int prop) {
        getElement().setProperty(PROPERTY_X, prop);
    }
    
    public int getY() {
        return getElement().getProperty(PROPERTY_Y,0);
    }
        
    public void setY(int prop) {
        getElement().setProperty(PROPERTY_Y, prop);
    }   
    
//    public Registration addClickEdgeListener(ComponentEventListener<MxGraphClickEdgeEvent> clickEdgeListener) {
//        return super.addListener(MxGraphClickEdgeEvent.class, clickEdgeListener);
//    }
}

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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;

/**
 * Event fired when any cell is unselected
 *
 * @author Orlando Paz Duarte {@literal <orlando.paz@kuwaiba.org>}
 */
@DomEvent("cell-unselected")
public class MxGraphCellUnselectedEvent extends ComponentEvent<MxGraph> {

    private String cellId;
    private boolean isVertex;

    public MxGraphCellUnselectedEvent(MxGraph source, boolean fromClient, @EventData("event.detail.cellId") String cellId, @EventData("event.detail.isVertex") boolean isVertex) {
        super(source, fromClient);
        this.cellId = cellId;
        this.isVertex = isVertex;
    }

    public String getCellId() {
        return cellId;
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    public boolean isVertex() {
        return isVertex;
    }

    public void setIsVertex(boolean isVertex) {
        this.isVertex = isVertex;
    }    

}

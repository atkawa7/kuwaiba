/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
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
 * Event to get the source id and target id for a new connection
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@DomEvent("edge-complete")
public class MxGraphEdgeCompleteEvent extends ComponentEvent<MxGraph> {
    /**
     * Source cell id
     */
    private final String sourceId;
    /**
     * Target cell id
     */
    private final String targetId;
    
    public MxGraphEdgeCompleteEvent(MxGraph source, boolean fromClient, 
        @EventData("event.detail.sourceId") String sourceId, 
        @EventData("event.detail.targetId") String targetId) {
        super(source, fromClient);
        this.sourceId = sourceId;
        this.targetId = targetId;
    }
    
    public String getSourceId() {
        return sourceId;
    }
    
    public String getTargetId() {
        return targetId;
    }
}

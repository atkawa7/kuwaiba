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
package org.kuwaiba.apis.persistence.application.process;

import java.util.List;

/**
 * Representation of a BPMN flow object -Parallel Gateway-
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ParallelActivityDefinition extends ActivityDefinition {
    /**
     * Outgoing Sequence Flows
     */
    public static int FORK = 1;
    /**
     * Incoming Sequence Flows
     */
    public static int JOIN = 2;
    /**
     * Incoming/Outgoing Sequence Flows
     */    
    public static int JOIN_FORK = 3;
    /**
     * List of paths
     */
    private List<ActivityDefinition> paths;
    /**
     * Outgoing or Incoming or Incoming/Outgoing  Sequence Flows
     */
    private int sequenceFlow = FORK;
    /**
     * Outgoing Sequence Flow Id (Fork Parallel Activity Definition Id)
     */
    private long outgoingSequenceFlowId = -1;
    /**
     * Incoming Sequence Flow Id (Join Parallel Activity Definition Id)
     */
    private long incomingSequenceFlowId = -1;
    
    public ParallelActivityDefinition(long id, String name, String description, int type, ArtifactDefinition arfifact, Actor actor, List<Kpi> kpis, List<KpiAction> kpiActions, boolean idling, boolean confirm, String color) {
        super(id, name, description, type, arfifact, actor, kpis, kpiActions, idling, confirm, color);
    }
    
    @Override
    public boolean isIdling() {
        return false;
    }
    
    public List<ActivityDefinition> getPaths() {
        return paths;
    }
    
    public void setPaths(List<ActivityDefinition> paths) {
        this.paths = paths;
    }
    
    public int getSequenceFlow() {
        return sequenceFlow;
    }
    
    public void setSequenceFlow(int sequenceFlow) {
        this.sequenceFlow = sequenceFlow;
    }
    
    public long getOutgoingSequenceFlowId() {
        return outgoingSequenceFlowId;
    }
    
    public void setOutgoingSequenceFlowId(long outgoingSequenceFlowId) {
        this.outgoingSequenceFlowId = outgoingSequenceFlowId;
    }
    
    public long getIncomingSequenceFlowId() {
        return incomingSequenceFlowId;
    }
    
    public void setIncomingSequenceFlowId(long incomingSequenceFlowId) {
        this.incomingSequenceFlowId = incomingSequenceFlowId;
    }
}

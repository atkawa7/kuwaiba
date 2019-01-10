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
package org.kuwaiba.interfaces.ws.toserialize.application;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * This is just an Parallel Activity Definition wrapper
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteParallelActivityDefinition extends RemoteActivityDefinition implements Serializable {
    /**
     * List of paths
     */
    @XmlElement(name = "paths")
    private List<RemoteActivityDefinition> paths;
    /**
     * Outgoing or Incoming or Incoming/Outgoing  Sequence Flows
     */
    private int sequenceFlow;
    /**
     * Outgoing Sequence Flow Id (Fork Parallel Activity Definition Id)
     */
    private long outgoingSequenceFlowId;
    /**
     * Incoming Sequence Flow Id (Join Parallel Activity Definition Id)
     */
    private long incomingSequenceFlowId;
    
    public RemoteParallelActivityDefinition(long id, String name, String description, 
        int type, RemoteArtifactDefinition arfifact, RemoteActor actor, boolean confirm, 
        String color, List<RemoteKpi> kpis, List<RemoteKpiAction> kpiActions) {
        
        super(id, name, description, type, arfifact, actor, false, confirm, color, kpis, kpiActions);
    }
            
    @Override
    public boolean isIdling() {
        return false;
    }
        
    public List<RemoteActivityDefinition> getPaths() {
        return paths;
    }
        
    public void setPaths(List<RemoteActivityDefinition> paths) {
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

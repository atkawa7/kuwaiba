/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.communications.core;

import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.wsclient.RemoteLogicalConnectionDetails;
import org.inventory.communications.wsclient.RemoteObjectLight;

/**
 * This is the local representation of the RemoteLocalConnectionsDetails class
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalLogicalConnectionDetails {
    /**
     * The complete information of the connection (that is, all its attributes)
     */
    private LocalObject connectionObject;
    /**
     * One endpoint of the connection
     */
    private LocalObjectLight endpointA;
    /**
     * The other endpoint of the connection
     */
    private LocalObjectLight endpointB;
    /**
     * Physical path of the connection's endpoint A (all the chain of physical elements -patches, mirror ports, etc- until reaching the next physical port)
     */
    private List<LocalObjectLight> physicalPathForEndpointA;
    /**
     * Physical path of the connection's endpoint A (all the chain of physical elements -patches, mirror ports, etc- until reaching the next physical port)
     */
    private List<LocalObjectLight> physicalPathForEndpointB;

    public LocalLogicalConnectionDetails(RemoteLogicalConnectionDetails remoteCircuitDetails) {
        LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(remoteCircuitDetails.getConnectionObject().getClassName(), false);
        this.connectionObject =  new LocalObject(remoteCircuitDetails.getConnectionObject().getClassName(), remoteCircuitDetails.getConnectionObject().getId(),
                                    remoteCircuitDetails.getConnectionObject().getAttributes(), classMetadata);
        this.endpointA =  new LocalObjectLight(remoteCircuitDetails.getEndpointA().getId(), remoteCircuitDetails.getEndpointA().getName(), remoteCircuitDetails.getEndpointA().getClassName());
        this.endpointB = new LocalObjectLight(remoteCircuitDetails.getEndpointB().getId(), remoteCircuitDetails.getEndpointB().getName(), remoteCircuitDetails.getEndpointB().getClassName());
        this.physicalPathForEndpointA = new ArrayList<>();
        for (RemoteObjectLight physicalPathForEndpointAElement : remoteCircuitDetails.getPhysicalPathForEndpointA())
            this.physicalPathForEndpointA.add(new LocalObjectLight(physicalPathForEndpointAElement.getId(), physicalPathForEndpointAElement.getName(), physicalPathForEndpointAElement.getClassName()));
        this.physicalPathForEndpointB = new ArrayList<>();
        for (RemoteObjectLight physicalPathForEndpointBElement : remoteCircuitDetails.getPhysicalPathForEndpointB())
            this.physicalPathForEndpointB.add(new LocalObjectLight(physicalPathForEndpointBElement.getId(), physicalPathForEndpointBElement.getName(), physicalPathForEndpointBElement.getClassName()));
    }
    
    

    public LocalObject getConnectionObject() {
        return connectionObject;
    }

    public LocalObjectLight getEndpointA() {
        return endpointA;
    }

    public LocalObjectLight getEndpointB() {
        return endpointB;
    }

    public List<LocalObjectLight> getPhysicalPathForEndpointA() {
        return physicalPathForEndpointA;
    }

    public List<LocalObjectLight> getPhysicalPathForEndpointB() {
        return physicalPathForEndpointB;
    }
}

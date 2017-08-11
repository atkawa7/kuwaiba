/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.ws.toserialize.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;

/**
 * Instances of this class represent the details of a logical connection and the physical resources associated to the endpoints of such connection.
 * This information is useful to build reports and end-to-end views
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteLogicalConnectionDetails implements Serializable {
    /**
     * The complete information of the connection (that is, all its attributes)
     */
    private RemoteObject connectionObject;
    /**
     * One endpoint of the connection
     */
    private RemoteObjectLight endpointA;
    /**
     * The other endpoint of the connection
     */
    private RemoteObjectLight endpointB;
    /**
     * Physical path of the connection's endpoint A (all the chain of physical elements -patches, mirror ports, etc- until reaching the next physical port)
     */
    private List<RemoteObjectLight> physicalPathForEndpointA;
    /**
     * Physical path of the connection's endpoint A (all the chain of physical elements -patches, mirror ports, etc- until reaching the next physical port)
     */
    private List<RemoteObjectLight> physicalPathForEndpointB;

    /**
     * Default parameter-less constructor
     */
    public RemoteLogicalConnectionDetails() { }

    public RemoteLogicalConnectionDetails(RemoteBusinessObject connectionObject, 
            RemoteBusinessObjectLight endpointA, RemoteBusinessObjectLight endpointB, 
            List<RemoteBusinessObjectLight> physicalPathForEndpointA, List<RemoteBusinessObjectLight> physicalPathForEndpointB) {
        this.connectionObject = new RemoteObject(connectionObject);
        this.endpointA =  new RemoteObjectLight(endpointA);
        this.endpointB = new RemoteObjectLight(endpointB);
        this.physicalPathForEndpointA = new ArrayList<>();
        for (RemoteBusinessObjectLight physicalPathForEndpointAElement : physicalPathForEndpointA)
            this.physicalPathForEndpointA.add(new RemoteObjectLight(physicalPathForEndpointAElement));
        this.physicalPathForEndpointB = new ArrayList<>();
        for (RemoteBusinessObjectLight physicalPathForEndpointBElement : physicalPathForEndpointB)
            this.physicalPathForEndpointB.add(new RemoteObjectLight(physicalPathForEndpointBElement));
    }
    
    

    public RemoteObject getConnectionObject() {
        return connectionObject;
    }

    public void setConnectionObject(RemoteObject connectionObject) {
        this.connectionObject = connectionObject;
    }

    public RemoteObjectLight getEndpointA() {
        return endpointA;
    }

    public void setEndpointA(RemoteObjectLight endpointA) {
        this.endpointA = endpointA;
    }

    public RemoteObjectLight getEndpointB() {
        return endpointB;
    }

    public void setEndpointB(RemoteObjectLight endpointB) {
        this.endpointB = endpointB;
    }

    public List<RemoteObjectLight> getPhysicalPathForEndpointA() {
        return physicalPathForEndpointA;
    }

    public void setPhysicalPathForEndpointA(List<RemoteObjectLight> physicalPathForEndpointA) {
        this.physicalPathForEndpointA = physicalPathForEndpointA;
    }

    public List<RemoteObjectLight> getPhysicalPathForEndpointB() {
        return physicalPathForEndpointB;
    }

    public void setPhysicalPathForEndpointB(List<RemoteObjectLight> physicalPathForEndpointB) {
        this.physicalPathForEndpointB = physicalPathForEndpointB;
    }
}

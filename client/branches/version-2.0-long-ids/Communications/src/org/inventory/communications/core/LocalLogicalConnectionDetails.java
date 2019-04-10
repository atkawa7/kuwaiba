/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.wsclient.RemoteLogicalConnectionDetails;
import org.inventory.communications.wsclient.RemoteObjectLight;
import org.inventory.communications.wsclient.RemoteObjectLightList;

/**
 * This is the local representation of the RemoteLocalConnectionsDetails class
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
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
    /**
     * Physical paths, one for every port that belongs to the same VLAN of the endpoint A
     */
    private Map<LocalObjectLight, List<LocalObjectLight>> physicalPathForVlansEndpointA;
    /**
     * Physical paths, one for every port that belongs to the same VLAN of the endpoint B
     */
    private Map<LocalObjectLight, List<LocalObjectLight>> physicalPathForVlansEndpointB;

    public LocalLogicalConnectionDetails(RemoteLogicalConnectionDetails remoteCircuitDetails) {
        
        LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(remoteCircuitDetails.getConnectionObject().getClassName(), false);
        this.connectionObject =  new LocalObject(remoteCircuitDetails.getConnectionObject().getClassName(), remoteCircuitDetails.getConnectionObject().getId(),
                                    remoteCircuitDetails.getConnectionObject().getAttributes(), classMetadata);
        this.endpointA = remoteCircuitDetails.getEndpointA() == null ? null : new LocalObjectLight(remoteCircuitDetails.getEndpointA().getId(), remoteCircuitDetails.getEndpointA().getName(), remoteCircuitDetails.getEndpointA().getClassName());
        this.endpointB = remoteCircuitDetails.getEndpointB() == null ? null : new LocalObjectLight(remoteCircuitDetails.getEndpointB().getId(), remoteCircuitDetails.getEndpointB().getName(), remoteCircuitDetails.getEndpointB().getClassName());
        this.physicalPathForEndpointA = new ArrayList<>();
        for (RemoteObjectLight physicalPathForEndpointAElement : remoteCircuitDetails.getPhysicalPathForEndpointA())
            this.physicalPathForEndpointA.add(new LocalObjectLight(physicalPathForEndpointAElement.getId(), physicalPathForEndpointAElement.getName(), physicalPathForEndpointAElement.getClassName()));
        this.physicalPathForEndpointB = new ArrayList<>();
        for (RemoteObjectLight physicalPathForEndpointBElement : remoteCircuitDetails.getPhysicalPathForEndpointB())
            this.physicalPathForEndpointB.add(new LocalObjectLight(physicalPathForEndpointBElement.getId(), physicalPathForEndpointBElement.getName(), physicalPathForEndpointBElement.getClassName()));
        
        this.physicalPathForVlansEndpointA = new HashMap<>();
        if(remoteCircuitDetails.getPhysicalPathForVlansEndpointA() != null){
            List<RemoteObjectLight> objsA = remoteCircuitDetails.getPhysicalPathForVlansEndpointA().getObjs();
            List<RemoteObjectLightList> relatedObjectsA = remoteCircuitDetails.getPhysicalPathForVlansEndpointA().getRelatedObjects();

            for (int i = 0; i < objsA.size(); i++){
                RemoteObjectLightList relatedRemoteObjects = relatedObjectsA.get(i);
                LocalObjectLight[] relatedLocalObjects = new LocalObjectLight[relatedRemoteObjects.getList().size()];
                int j = 0;
                for (RemoteObjectLight relatedRemoteObject : relatedRemoteObjects.getList()) {
                    relatedLocalObjects[j] = new LocalObjectLight(relatedRemoteObject.getId(), 
                                                    relatedRemoteObject.getName(), 
                                                    relatedRemoteObject.getClassName());
                    j++;
                }
                this.physicalPathForVlansEndpointA.put(
                        new LocalObjectLight(objsA.get(i).getId(), 
                                objsA.get(i).getName(), 
                                objsA.get(i).getClassName()),
                        Arrays.asList(relatedLocalObjects));
            }
        }
        if(remoteCircuitDetails.getPhysicalPathForVlansEndpointB() != null){
            this.physicalPathForVlansEndpointB = new HashMap<>();
            List<RemoteObjectLight> objsB = remoteCircuitDetails.getPhysicalPathForVlansEndpointB().getObjs();
            List<RemoteObjectLightList> relatedObjectsB = remoteCircuitDetails.getPhysicalPathForVlansEndpointB().getRelatedObjects();
        
            for (int i = 0; i < objsB.size(); i++){
                RemoteObjectLightList relatedRemoteObjects = relatedObjectsB.get(i);
                LocalObjectLight[] relatedLocalObjects = new LocalObjectLight[relatedRemoteObjects.getList().size()];
                int j = 0;
                for (RemoteObjectLight relatedRemoteObject : relatedRemoteObjects.getList()) {
                    relatedLocalObjects[j] = new LocalObjectLight(relatedRemoteObject.getId(), 
                                                    relatedRemoteObject.getName(), 
                                                    relatedRemoteObject.getClassName());
                    j++;
                }
                this.physicalPathForVlansEndpointB.put(
                        new LocalObjectLight(objsB.get(i).getId(), 
                                objsB.get(i).getName(), 
                                objsB.get(i).getClassName()),
                        Arrays.asList(relatedLocalObjects));
            }
        }
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

    public Map<LocalObjectLight, List<LocalObjectLight>> getPhysicalPathForVlansEndpointA() {
        return physicalPathForVlansEndpointA;
    }

    public Map<LocalObjectLight, List<LocalObjectLight>> getPhysicalPathForVlansEndpointB() {
        return physicalPathForVlansEndpointB;
    }

}

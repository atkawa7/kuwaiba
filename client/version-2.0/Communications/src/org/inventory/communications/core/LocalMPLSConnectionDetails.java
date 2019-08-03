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

import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.wsclient.RemoteMPLSConnectionDetails;

/**
 * This is the local representation of the RemoteMPLSConnectionsDetails class
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class LocalMPLSConnectionDetails {
    /**
    * The complete information of the connection (that is, all its attributes)
    */
    private LocalObject connectionObject;
    /**
     *  the real endpoint of the mpls link side A
     */
    private LocalObjectLight endpointA;
    /**
     *  device parent of the endpointA
     */
    private LocalObjectLight deviceA;
    /**
     *  the real endpoint of the mpls link side B
     */
    private LocalObjectLight endpointB;
    /**
     *  device parent of the endpointB
     */
    private LocalObjectLight deviceB;
    /**
     * a possible logical endpoint of a mpls link
     */
    private LocalObjectLight pseudowireA;
    /**
     * a possible logical endpoint of a mpls link
     */
    private LocalObjectLight pseudowireB;
    /**
     * vfi related with PseudowireA
     */
    private LocalObjectLight vfiA;
    /**
     * vfi related with PseudowireB
     */
    private LocalObjectLight vfiB;
    
    public LocalMPLSConnectionDetails() {
    }

    public LocalMPLSConnectionDetails(RemoteMPLSConnectionDetails mplsLinkEndpoints) {
        if(mplsLinkEndpoints.getConnectionObject() != null){
            LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(mplsLinkEndpoints.getConnectionObject().getClassName(), false);
            this.connectionObject = new LocalObject(mplsLinkEndpoints.getConnectionObject().getClassName(), mplsLinkEndpoints.getConnectionObject().getId(), mplsLinkEndpoints.getConnectionObject().getAttributes(), classMetadata);
        }
        else
            this.connectionObject = null;
        this.vfiA = mplsLinkEndpoints.getVfiA()!= null ? new LocalObjectLight(mplsLinkEndpoints.getVfiA().getId(), mplsLinkEndpoints.getVfiA().getName(), mplsLinkEndpoints.getVfiA().getClassName()) : null;
        this.vfiB = mplsLinkEndpoints.getVfiB() != null ? new LocalObjectLight(mplsLinkEndpoints.getVfiB().getId(), mplsLinkEndpoints.getVfiB().getName(), mplsLinkEndpoints.getVfiB().getClassName()) : null;
        this.endpointA = mplsLinkEndpoints.getEndpointA() != null ? new LocalObjectLight(mplsLinkEndpoints.getEndpointA().getId(), mplsLinkEndpoints.getEndpointA().getName(), mplsLinkEndpoints.getEndpointA().getClassName()) : null;
        this.endpointB = mplsLinkEndpoints.getEndpointB() != null ? new LocalObjectLight(mplsLinkEndpoints.getEndpointB().getId(), mplsLinkEndpoints.getEndpointB().getName(), mplsLinkEndpoints.getEndpointB().getClassName()) : null;
        this.deviceA = mplsLinkEndpoints.getDeviceA() != null ? new LocalObjectLight(mplsLinkEndpoints.getDeviceA().getId(), mplsLinkEndpoints.getDeviceA().getName(), mplsLinkEndpoints.getDeviceA().getClassName()) : null;
        this.deviceB = mplsLinkEndpoints.getDeviceB() != null ? new LocalObjectLight(mplsLinkEndpoints.getDeviceB().getId(), mplsLinkEndpoints.getDeviceB().getName(), mplsLinkEndpoints.getDeviceB().getClassName()) : null;
    }

    public LocalObject getConnectionObject() {
        return connectionObject;
    }

    public void setConnectionObject(LocalObject connectionObject) {
        this.connectionObject = connectionObject;
    }

    public LocalObjectLight getPseudowireA() {
        return pseudowireA;
    }

    public void setPseudowireA(LocalObjectLight pseudowireA) {
        this.pseudowireA = pseudowireA;
    }

    public LocalObjectLight getPseudowireB() {
        return pseudowireB;
    }

    public void setPseudowireB(LocalObjectLight pseudowireB) {
        this.pseudowireB = pseudowireB;
    }

    public LocalObjectLight getEndpointA() {
        return endpointA;
    }

    public void setEndpointA(LocalObjectLight endpointA) {
        this.endpointA = endpointA;
    }

    public LocalObjectLight getEndpointB() {
        return endpointB;
    }

    public void setEndpointB(LocalObjectLight endpointB) {
        this.endpointB = endpointB;
    }

    public LocalObjectLight getDeviceA() {
        return deviceA;
    }

    public void setDeviceA(LocalObjectLight deviceA) {
        this.deviceA = deviceA;
    }

    public LocalObjectLight getDeviceB() {
        return deviceB;
    }

    public void setDeviceB(LocalObjectLight deviceB) {
        this.deviceB = deviceB;
    }

}

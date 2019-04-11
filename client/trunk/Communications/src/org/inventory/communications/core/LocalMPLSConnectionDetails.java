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
     * the real endpoint of the mpls link, it could be a PhysicalPort or a VirtualPort
     */
    private LocalObjectLight outputInterfaceA;
    /**
     * the real endpoint of the mpls link, it could be a PhysicalPort or a VirtualPort
     */
    private LocalObjectLight outputInterfaceB;
    /**
     * At this moment this two fields are just for information, they are not 
     * useful to show continuity for now
     */
    private LocalObjectLight tunnelA;
    private LocalObjectLight tunnelB;

    public LocalMPLSConnectionDetails() {
    }

    public LocalMPLSConnectionDetails(LocalObject connectionObject, LocalObjectLight pseudowireA, LocalObjectLight pseudowireB, LocalObjectLight outputInterfaceA, LocalObjectLight outputInterfaceB, LocalObjectLight tunnelA, LocalObjectLight tunnelB) {
        this.connectionObject = connectionObject;
        this.pseudowireA = pseudowireA;
        this.pseudowireB = pseudowireB;
        this.outputInterfaceA = outputInterfaceA;
        this.outputInterfaceB = outputInterfaceB;
        this.tunnelA = tunnelA;
        this.tunnelB = tunnelB;
    }

    public LocalMPLSConnectionDetails(RemoteMPLSConnectionDetails mplsLinkEndpoints) {
        if(mplsLinkEndpoints.getConnectionObject() != null){
            LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(mplsLinkEndpoints.getConnectionObject().getClassName(), false);
            this.connectionObject = new LocalObject(mplsLinkEndpoints.getConnectionObject().getClassName(), mplsLinkEndpoints.getConnectionObject().getId(), mplsLinkEndpoints.getConnectionObject().getAttributes(), classMetadata);
        }
        else
            this.connectionObject = null;
        this.pseudowireA = mplsLinkEndpoints.getPseudowireA() != null ? new LocalObjectLight(mplsLinkEndpoints.getPseudowireA().getId(), mplsLinkEndpoints.getPseudowireA().getName(), mplsLinkEndpoints.getPseudowireA().getClassName()) : null;
        this.pseudowireB = mplsLinkEndpoints.getPseudowireB() != null ? new LocalObjectLight(mplsLinkEndpoints.getPseudowireB().getId(), mplsLinkEndpoints.getPseudowireB().getName(), mplsLinkEndpoints.getPseudowireB().getClassName()) : null;
        this.outputInterfaceA = mplsLinkEndpoints.getOutputInterfaceA() != null ? new LocalObjectLight(mplsLinkEndpoints.getOutputInterfaceA().getId(), mplsLinkEndpoints.getOutputInterfaceA().getName(), mplsLinkEndpoints.getOutputInterfaceA().getClassName()) : null;
        this.outputInterfaceB = mplsLinkEndpoints.getOutputInterfaceB() != null ? new LocalObjectLight(mplsLinkEndpoints.getOutputInterfaceB().getId(), mplsLinkEndpoints.getOutputInterfaceB().getName(), mplsLinkEndpoints.getOutputInterfaceB().getClassName()) : null;
        this.tunnelA = mplsLinkEndpoints.getTunnelA() != null ? new LocalObjectLight(mplsLinkEndpoints.getTunnelA().getId(), mplsLinkEndpoints.getTunnelA().getName(), mplsLinkEndpoints.getTunnelA().getClassName()) : null;
        this.tunnelB = mplsLinkEndpoints.getTunnelB() != null ? new LocalObjectLight(mplsLinkEndpoints.getTunnelB().getId(), mplsLinkEndpoints.getTunnelB().getName(), mplsLinkEndpoints.getTunnelB().getClassName()) : null;
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

    public LocalObjectLight getOutputInterfaceA() {
        return outputInterfaceA;
    }

    public void setOutputInterfaceA(LocalObjectLight outputInterfaceA) {
        this.outputInterfaceA = outputInterfaceA;
    }

    public LocalObjectLight getOutputInterfaceB() {
        return outputInterfaceB;
    }

    public void setOutputInterfaceB(LocalObjectLight outputInterfaceB) {
        this.outputInterfaceB = outputInterfaceB;
    }

    public LocalObjectLight getTunnelA() {
        return tunnelA;
    }

    public void setTunnelA(LocalObjectLight tunnelA) {
        this.tunnelA = tunnelA;
    }

    public LocalObjectLight getTunnelB() {
        return tunnelB;
    }

    public void setTunnelB(LocalObjectLight tunnelB) {
        this.tunnelB = tunnelB;
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

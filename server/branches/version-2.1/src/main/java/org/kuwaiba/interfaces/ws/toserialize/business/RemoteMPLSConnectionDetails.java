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

package org.kuwaiba.interfaces.ws.toserialize.business;

import com.neotropic.kuwaiba.modules.mpls.MPLSConnectionDefinition;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Instances of this class represent the details of a mpls link (a logical connection)
 * and the resources associated to the endpoints of such connection.
 * This information is useful to build reports and end-to-end views
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteMPLSConnectionDetails {
    /**
    * The complete information of the connection (that is, all its attributes)
    */
    private RemoteObject connectionObject;
    /**
     * the real endpoint of the mpls link side A
     */
    private RemoteObjectLight endpointA;
    /**
     * the device parent of the endpointA
     */
    private RemoteObjectLight deviceA;
    /**
     * the real endpoint of the mpls link side B
     */
    private RemoteObjectLight endpointB;
    /**
     * the device parent of the endpointB
     */
    private RemoteObjectLight deviceB;
    /**
     * a possible logical endpoint of a mpls link
     */
    private RemoteObjectLight pseudowireA;
    /**
     * a possible logical endpoint of a mpls link
     */
    private RemoteObjectLight pseudowireB;
    /**
     * the real endpoint of the mpls link, it could be a PhysicalPort or a VirtualPort
     */
    private RemoteObjectLight outputInterfaceA;
    /**
     * the real endpoint of the mpls link, it could be a PhysicalPort or a VirtualPort
     */
    private RemoteObjectLight outputInterfaceB;
    /**
     * At this moment this two fields are just for information, they are not 
     * useful to show continuity for now
     */
    private RemoteObjectLight tunnelA;
    private RemoteObjectLight tunnelB;

    public RemoteMPLSConnectionDetails(RemoteObject connectionObject, RemoteObjectLight pseudowireA, RemoteObjectLight pseudowireB, RemoteObjectLight outputInterfaceA, RemoteObjectLight outputInterfaceB, RemoteObjectLight tunnelA, RemoteObjectLight tunnelB) {
        this.connectionObject = connectionObject;
        this.pseudowireA = pseudowireA;
        this.pseudowireB = pseudowireB;
        this.outputInterfaceA = outputInterfaceA;
        this.outputInterfaceB = outputInterfaceB;
        this.tunnelA = tunnelA;
        this.tunnelB = tunnelB;
    }

    public RemoteMPLSConnectionDetails(MPLSConnectionDefinition mplsLinkEndpoints) {
        this.connectionObject = mplsLinkEndpoints.getConnectionObject() != null ? new RemoteObject(mplsLinkEndpoints.getConnectionObject()) : null;
        this.pseudowireA = mplsLinkEndpoints.getPseudowireA() != null ? new RemoteObjectLight(mplsLinkEndpoints.getPseudowireA()) : null;
        this.pseudowireB = mplsLinkEndpoints.getPseudowireB() != null ? new RemoteObjectLight(mplsLinkEndpoints.getPseudowireB()) : null;
        this.outputInterfaceA = mplsLinkEndpoints.getOutputInterfaceA() != null ? new RemoteObjectLight(mplsLinkEndpoints.getOutputInterfaceA()) : null;
        this.outputInterfaceB = mplsLinkEndpoints.getOutputInterfaceB() != null ? new RemoteObjectLight(mplsLinkEndpoints.getOutputInterfaceB()) : null;
        this.tunnelA =  mplsLinkEndpoints.getTunnelA() != null ? new RemoteObjectLight(mplsLinkEndpoints.getTunnelA()) : null;
        this.tunnelB =  mplsLinkEndpoints.getTunnelB() != null ? new RemoteObjectLight(mplsLinkEndpoints.getTunnelB()) : null;
        this.endpointA = mplsLinkEndpoints.getEndpointA() != null ? new RemoteObjectLight(mplsLinkEndpoints.getEndpointA()) : null;
        this.endpointB = mplsLinkEndpoints.getEndpointB() != null ? new RemoteObjectLight(mplsLinkEndpoints.getEndpointB()) : null;
        this.deviceA = mplsLinkEndpoints.getDeviceA() != null ? new RemoteObjectLight(mplsLinkEndpoints.getDeviceA()) : null;
        this.deviceB = mplsLinkEndpoints.getDeviceB() != null ? new RemoteObjectLight(mplsLinkEndpoints.getDeviceB()) : null;
    }

    public RemoteObject getConnectionObject() {
        return connectionObject;
    }

    public void setConnectionObject(RemoteObject connectionObject) {
        this.connectionObject = connectionObject;
    }

    public RemoteObjectLight getPseudowireA() {
        return pseudowireA;
    }

    public void setPseudowireA(RemoteObjectLight pseudowireA) {
        this.pseudowireA = pseudowireA;
    }

    public RemoteObjectLight getPseudowireB() {
        return pseudowireB;
    }

    public void setPseudowireB(RemoteObjectLight pseudowireB) {
        this.pseudowireB = pseudowireB;
    }

    public RemoteObjectLight getOutputInterfaceA() {
        return outputInterfaceA;
    }

    public void setOutputInterfaceA(RemoteObjectLight outputInterfaceA) {
        this.outputInterfaceA = outputInterfaceA;
    }

    public RemoteObjectLight getOutputInterfaceB() {
        return outputInterfaceB;
    }

    public void setOutputInterfaceB(RemoteObjectLight outputInterfaceB) {
        this.outputInterfaceB = outputInterfaceB;
    }

    public RemoteObjectLight getTunnelA() {
        return tunnelA;
    }

    public void setTunnelA(RemoteObjectLight tunnelA) {
        this.tunnelA = tunnelA;
    }

    public RemoteObjectLight getTunnelB() {
        return tunnelB;
    }

    public void setTunnelB(RemoteObjectLight tunnelB) {
        this.tunnelB = tunnelB;
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

    public RemoteObjectLight getDeviceA() {
        return deviceA;
    }

    public void setDeviceA(RemoteObjectLight deviceA) {
        this.deviceA = deviceA;
    }

    public RemoteObjectLight getDeviceB() {
        return deviceB;
    }

    public void setDeviceB(RemoteObjectLight deviceB) {
        this.deviceB = deviceB;
    }

}

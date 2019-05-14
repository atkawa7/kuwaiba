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

package com.neotropic.kuwaiba.modules.mpls;

import org.kuwaiba.apis.persistence.business.BusinessObject;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;

/**
 * Instances of this class represent the details of a MPLSLink (logical connection)
 * and the resources associated to the physical an logical endpoints of such connection.
 * This information is useful to build reports and end-to-end views
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class MPLSConnectionDefinition {
    /**
    * The complete information of the connection (that is, all its attributes)
    */
    private BusinessObject connectionObject;
    /**
     * the real endpoint of the mplsLink side A
     */
    private BusinessObjectLight endpointA;
    /**
     * the parent of the endpointA
     */
    private BusinessObjectLight deviceA;
    /**
     * the real endpoint of the mplsLink side B
     */
    private BusinessObjectLight endpointB;
    /**
     * the parent of the endpointA
     */
    private BusinessObjectLight deviceB;
    /**
     * a possible logical endpoint of a mpls link
     */
    private BusinessObjectLight pseudowireA;
    /**
     * a possible logical endpoint of a mpls link
     */
    private BusinessObjectLight pseudowireB;
    /**
     * the real endpoint of the mpls link, it could be a PhysicalPort or a VirtualPort
     */
    private BusinessObjectLight outputInterfaceA;
    /**
     * the real endpoint of the mpls link, it could be a PhysicalPort or a VirtualPort
     */
    private BusinessObjectLight outputInterfaceB;
    /**
     * At this moment this two fields are just for information, they are not 
     * useful to show continuity for now
     */
    private BusinessObjectLight tunnelA;
    private BusinessObjectLight tunnelB;

    public MPLSConnectionDefinition(BusinessObject connectionObject) {
        this.connectionObject = connectionObject;
    }

    public MPLSConnectionDefinition(BusinessObject connectionObject, BusinessObjectLight pseudowireA, BusinessObjectLight pseudowireB, BusinessObjectLight outputInterfaceA, BusinessObjectLight outputInterfaceB, BusinessObjectLight tunnelA, BusinessObjectLight tunnelB) {
        this.connectionObject = connectionObject;
        this.pseudowireA = pseudowireA;
        this.pseudowireB = pseudowireB;
        this.outputInterfaceA = outputInterfaceA;
        this.outputInterfaceB = outputInterfaceB;
        this.tunnelA = tunnelA;
        this.tunnelB = tunnelB;
    }

    public BusinessObject getConnectionObject() {
        return connectionObject;
    }

    public void setConnectionObject(BusinessObject connectionObject) {
        this.connectionObject = connectionObject;
    }

    public BusinessObjectLight getPseudowireA() {
        return pseudowireA;
    }

    public void setPseudowireA(BusinessObjectLight pseudowireA) {
        this.pseudowireA = pseudowireA;
    }

    public BusinessObjectLight getPseudowireB() {
        return pseudowireB;
    }

    public void setPseudowireB(BusinessObjectLight pseudowireB) {
        this.pseudowireB = pseudowireB;
    }

    public BusinessObjectLight getOutputInterfaceA() {
        return outputInterfaceA;
    }

    public void setOutputInterfaceA(BusinessObjectLight outputInterfaceA) {
        this.outputInterfaceA = outputInterfaceA;
    }

    public BusinessObjectLight getOutputInterfaceB() {
        return outputInterfaceB;
    }

    public void setOutputInterfaceB(BusinessObjectLight outputInterfaceB) {
        this.outputInterfaceB = outputInterfaceB;
    }

    public BusinessObjectLight getTunnelA() {
        return tunnelA;
    }

    public void setTunnelA(BusinessObjectLight tunnelA) {
        this.tunnelA = tunnelA;
    }

    public BusinessObjectLight getTunnelB() {
        return tunnelB;
    }

    public void setTunnelB(BusinessObjectLight tunnelB) {
        this.tunnelB = tunnelB;
    }

    public BusinessObjectLight getEndpointA() {
        return endpointA;
    }

    public void setEndpointA(BusinessObjectLight endpointA) {
        this.endpointA = endpointA;
    }

    public BusinessObjectLight getEndpointB() {
        return endpointB;
    }

    public void setEndpointB(BusinessObjectLight endpointB) {
        this.endpointB = endpointB;
    }

    public BusinessObjectLight getDeviceA() {
        return deviceA;
    }

    public void setDeviceA(BusinessObjectLight deviceA) {
        this.deviceA = deviceA;
    }

    public BusinessObjectLight getDeviceB() {
        return deviceB;
    }

    public void setDeviceB(BusinessObjectLight deviceB) {
        this.deviceB = deviceB;
    }

    
}

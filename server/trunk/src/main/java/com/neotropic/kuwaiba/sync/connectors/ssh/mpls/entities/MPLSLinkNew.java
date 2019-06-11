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

package com.neotropic.kuwaiba.sync.connectors.ssh.mpls.entities;

import com.neotropic.kuwaiba.sync.model.AbstractDataEntity;

/**
 * Entity class that represents a Cisco's MPLS transport link data
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class MPLSLinkNew extends AbstractDataEntity {
        /**
     * if is VPWS or VPLS
     */
    private String serviceType;
    /**
     * The local physical port where the MPLSLinkNew could be connected or the parent of the service instance or a virtual port
     */
    private String localPhysicalInterface;
    /**
     * The local Pseusowire, VirtualPort or ServiceInstance where the endpoint of the MPLSLinkNew is connected
     */
    private String localVirtualInterface;
    /**
     * The local Pseusowire
     */
    private String pseudowireA;
    /**
     * Local ip address related with the MPLSLinkNew's local interface
     */
    private String destinyIPAddress;
    /**
     * vcId identifier 
     */
    private String vcidA;
    /**
     * A target vcId, necesary when both fields are Pseudowires
       A normal MPLSLinkNew:
       VPWS name: HUAWEI-BRAZZAVILLE-LAGOS, State: UP
       pw100002                       185.35.142.4:2410(MPLS)         0     UP  UP   
       Fa0/1/1                        Fa0/1/1:10(Eth VLAN)            0     UP  UP 

       When both are pseudowire:
       VPWS name: AFR-EP2P-FTW-TEV-128k-001, State: UP
       pw713                          185.35.140.3:713(MPLS)          0     UP  UP   
       pw714                          41.223.133.74:714(MPLS)         0     UP  UP  
     *
     */
    private String vcidB;
//    /**
//     * The target physical port where the MPLSLinkNew could be connected or the parent of the service instance or a virtual port
//     */
//    private String targetPhysicalInterface;
//    /**
//     * The target Pseusowire, VirtualPort or ServiceInstance where the endpoint of the MPLSLinkNew is connected
//     */
//    private String targetVirtualInterface;
    /**
     * The local Pseusowire
     */
    private String pseudowireB;
    /**
     * target ip address of MPLSLinkNew's target interface
     */
    private String destinyIPAddressB;
    /**
     * The service's name related with the MPLSLinkNew
     */
    private String serviceName;
    /**
     * The VFI name
     */
    private String vfiName;
    /**
     * 
     */
    private String serviceCustomerGroup;
    
    public MPLSLinkNew() {
    }

    public MPLSLinkNew(String localPhysicalInterface, String localVirtualInterface, String vcidA, String destinyIPAddressB, String serviceName, String serviceCustomerGroup) {
        this.localPhysicalInterface = localPhysicalInterface;
        this.localVirtualInterface = localVirtualInterface;
        this.vcidA = vcidA;
        this.destinyIPAddressB = destinyIPAddressB;
        this.serviceName = serviceName;
        this.serviceCustomerGroup = serviceCustomerGroup;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getLocalPhysicalInterface() {
        return localPhysicalInterface;
    }

    public void setLocalPhysicalInterface(String localPhysicalInterface) {
        this.localPhysicalInterface = localPhysicalInterface;
    }

    public String getLocalVirtualInterface() {
        return localVirtualInterface;
    }

    public void setLocalVirtualInterface(String localVirtualInterface) {
        this.localVirtualInterface = localVirtualInterface;
    }

    public String getPseudowireA() {
        return pseudowireA;
    }

    public void setPseudowireA(String pseudowireA) {
        this.pseudowireA = pseudowireA;
    }

    public String getDestinyIPAddress() {
        return destinyIPAddress;
    }

    public void setDestinyIPAddress(String destinyIPAddress) {
        this.destinyIPAddress = destinyIPAddress;
    }

    public String getVcidA() {
        return vcidA;
    }

    public void setVcidA(String vcidA) {
        this.vcidA = vcidA;
    }

    public String getVcidB() {
        return vcidB;
    }

    public void setVcidB(String vcidB) {
        this.vcidB = vcidB;
    }

    public String getPseudowireB() {
        return pseudowireB;
    }

    public void setPseudowireB(String pseudowireB) {
        this.pseudowireB = pseudowireB;
    }

    public String getDestinyIPAddressB() {
        return destinyIPAddressB;
    }

    public void setDestinyIPAddressB(String destinyIPAddressB) {
        this.destinyIPAddressB = destinyIPAddressB;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVfiName() {
        return vfiName;
    }

    public void setVfiName(String vfiName) {
        this.vfiName = vfiName;
    }

    public String getServiceCustomerGroup() {
        return serviceCustomerGroup;
    }

    public void setServiceCustomerGroup(String serviceCustomerGroup) {
        this.serviceCustomerGroup = serviceCustomerGroup;
    }

    @Override
    public DATA_TYPE getType() {
        return DATA_TYPE.JAVA_OBJECT;
    }
}

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
 * Entity class that represents a Cisco's MPLS tanspor link data
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class MPLSLink extends AbstractDataEntity {
    /**
     * Local interface of the MPLSLink
     */
    private String localInterface;
    /**
     * Local interface of the MPLSLink
     */
    private String localInterfaceDetail;
    /**
     * The output interface where the  MPLSLink is connected
     */
    private String outputInterface;
    /**
     * Local ip address related with the MPLSLink's local interface
     */
    private String localInterfaceIp;
    /**
     * Local interface of the MPLSLink
     */
    private String localInterfaceIpDetail;
    /**
     * vcId identifier 
     */
    private String vcId;
    /**
     * destiny ip address of MPLSLink's destiny interface
     */
    private String destinationIp;
     /**
     * destiny ip address of MPLSLink's destiny interface
     */
    private String destinationIpDetail;
    /**
     * The service's name related with the MPLSLink
     */
    private String service;
    /**
     * The an acronym for the customer's name
     */
    private String serviceAccronym;

    public MPLSLink() {
    }
            
    public MPLSLink(String localInterface, 
            String localInterfaceIp, String vcId, String destinationIp) {
        this.localInterface = localInterface;
        this.localInterfaceIp = localInterfaceIp;
        this.vcId = vcId;
        this.destinationIp = destinationIp;
    }
    
     public MPLSLink(String localInterface, String vcId, 
             String destinationIp, String service, String serviceAccronym) {
        this.localInterface = localInterface;
        this.vcId = vcId;
        this.destinationIp = destinationIp;
        this.service = service;
        this.serviceAccronym = serviceAccronym;
    }

    public String getLocalInterface() {
        return localInterface;
    }

    public void setLocalInterface(String localInterface) {
        this.localInterface = localInterface;
    }

    public String getLocalInterfaceIp() {
        return localInterfaceIp;
    }

    public void setLocalInterfaceIp(String localInterfaceIp) {
        this.localInterfaceIp = localInterfaceIp;
    }

    public String getVcId() {
        return vcId;
    }

    public void setVcId(String vcId) {
        this.vcId = vcId;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    public String getService() {
        return service;
    }

    public void setServiceName(String service) {
        this.service = service;
    }

    public String getServiceAccronym() {
        return serviceAccronym;
    }

    public void setServiceAccronym(String serviceAccronym) {
        this.serviceAccronym = serviceAccronym;
    }

    public String getLocalInterfaceDetail() {
        return localInterfaceDetail;
    }

    public void setLocalInterfaceDetail(String localInterfaceDetail) {
        this.localInterfaceDetail = localInterfaceDetail;
    }

    public String getOutputInterface() {
        return outputInterface;
    }

    public void setOutputInterface(String outputInterface) {
        this.outputInterface = outputInterface;
    }

    public String getLocalInterfaceIpDetail() {
        return localInterfaceIpDetail;
    }

    public void setLocalInterfaceIpDetail(String localInterfaceIpDetail) {
        this.localInterfaceIpDetail = localInterfaceIpDetail;
    }

    public String getDestinationIpDetail() {
        return destinationIpDetail;
    }

    public void setDestinationIpDetail(String destinationIpDetail) {
        this.destinationIpDetail = destinationIpDetail;
    }

    @Override
    public DATA_TYPE getType() {
        return DATA_TYPE.JAVA_OBJECT;
    }
}

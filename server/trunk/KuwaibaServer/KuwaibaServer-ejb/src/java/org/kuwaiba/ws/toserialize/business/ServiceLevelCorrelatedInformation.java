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
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Groups a customer and the services associated to it that are related somehow to an interface/device 
 * provided in the <code>getAffectedServices</code> method. It's actually a replacement for a Hashmap, but more <i>web service-friendly</i>
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceLevelCorrelatedInformation implements Serializable {
    /**
     * The customer all the services belong to
     */
    private RemoteObjectLight customer;
    /**
     * The services related to the interface/device provided
     */
    private List<RemoteObjectLight> services;
    
    /**
     * Mandatory parameter-less constructor
     */
    public ServiceLevelCorrelatedInformation() { }
    
    public ServiceLevelCorrelatedInformation(RemoteObjectLight customer, List<RemoteObjectLight> services) {
        this.customer = customer;
        this.services = services;
    }

    public RemoteObjectLight getCustomer() {
        return customer;
    }

    public void setCustomer(RemoteObjectLight customer) {
        this.customer = customer;
    }

    public List<RemoteObjectLight> getServices() {
        return services;
    }

    public void setServices(List<RemoteObjectLight> services) {
        this.services = services;
    }
}
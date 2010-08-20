/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
 *  under the License.
 */

package entity.qos.services;

import entity.core.AdministrativeItem;
import entity.multiple.people.Employee;
import entity.qos.OLA;
import entity.qos.SLA;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

/**
 * Represents a simple service
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericService extends AdministrativeItem implements Serializable {
    protected String serviceId;
    @OneToOne
    protected SLA sla; //To the customer
    @OneToOne
    protected OLA ola; //To the company
    @ManyToMany
    protected List<Employee> responsibles;

    public OLA getOla() {
        return ola;
    }

    public void setOla(OLA ola) {
        this.ola = ola;
    }

    public List<Employee> getResponsibles() {
        return responsibles;
    }

    public void setResponsibles(List<Employee> responsibles) {
        this.responsibles = responsibles;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public SLA getSla() {
    return sla;
    }

    public void setSla(SLA sla) {
    this.sla = sla;
    }
}

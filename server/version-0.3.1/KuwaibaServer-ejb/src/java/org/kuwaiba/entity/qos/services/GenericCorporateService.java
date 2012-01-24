/**
 *  Copyright 2012 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package org.kuwaiba.entity.qos.services;

import org.kuwaiba.core.annotations.NoSerialize;
import org.kuwaiba.entity.multiple.people.Employee;
import org.kuwaiba.entity.qos.OLA;
import org.kuwaiba.entity.qos.SLA;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 * Represents a service provided to a corporative customer
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class GenericCorporateService extends GenericService{
    /**
     * Indicate the measurement unit in the attribute's display label
     */
    protected Float rate;
    @OneToMany
    @NoSerialize //For now
    protected List<Employee> responsibles;
    /**
     * Level agreement facing to the customer
     */
    @OneToOne
    @NoSerialize //For now, since we don't have a suitable editor for this (SLA/OLA are not subclasses of GenericObjectClass)
    protected SLA sla;
    /**
     * Level agreement facing to the company
     */
    @OneToOne
    @NoSerialize //For now
    protected OLA ola;

    public Float getRate() {
        return rate;
    }

    public void setRate(Float rate) {
        this.rate = rate;
    }

    public OLA getOla() {
        return ola;
    }

    public void setOla(OLA ola) {
        this.ola = ola;
    }

    public SLA getSla() {
    return sla;
    }

    public void setSla(SLA sla) {
    this.sla = sla;
    }

    public List<Employee> getResponsibles() {
        return responsibles;
    }

    public void setResponsibles(List<Employee> responsibles) {
        this.responsibles = responsibles;
    }

}

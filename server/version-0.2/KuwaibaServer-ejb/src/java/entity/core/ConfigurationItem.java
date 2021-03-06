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
 */

package entity.core;

import core.annotations.Administrative;
import entity.multiple.companies.Vendor;
import entity.multiple.misc.EnvironmentalCondition;
import entity.multiple.states.OperationalState;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;



/**
 * Represents a configuration item according to ITIL. Descendants are every element
 * that can be somehow to be configured (equipment, ports, etc)
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Administrative
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class ConfigurationItem extends ViewableObject  {

    protected String serialNumber;
    @ManyToOne
    protected Vendor vendor;
    @ManyToOne
    protected EnvironmentalCondition conditions;
    @ManyToOne
    protected OperationalState state;

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public String getSerialNumber(){
        return this.serialNumber;
    }

    public void setSerialNumber(String serialNumber){
            this.serialNumber =serialNumber;
    }

    public EnvironmentalCondition getConditions() {
        return conditions;
    }

    public void setConditions(EnvironmentalCondition conditions) {
        this.conditions = conditions;
    }

    public OperationalState getState() {
        return state;
    }

    public void setState(OperationalState state) {
        this.state = state;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ConfigurationItem)) {
            return false;
        }
        ConfigurationItem other = (ConfigurationItem) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.ConfigurationItem[id=" + id + "]";
    }

}

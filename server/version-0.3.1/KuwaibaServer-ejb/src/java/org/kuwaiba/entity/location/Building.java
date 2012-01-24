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
package org.kuwaiba.entity.location;

import org.kuwaiba.entity.multiple.companies.LocationOwner;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import org.kuwaiba.entity.multiple.types.other.FacilityType;

/**
 * Represents a simple building
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class Building extends GenericLocation {
    protected String address;
    @ManyToOne
    protected LocationOwner owner;
    protected String phoneNumber;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocationOwner getOwner() {
        return owner;
    }

    public void setOwner(LocationOwner owner) {
        this.owner = owner;
    }
}

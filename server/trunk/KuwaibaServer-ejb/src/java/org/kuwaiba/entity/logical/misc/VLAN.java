/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.entity.logical.misc;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import org.kuwaiba.entity.logical.GenericLogicalElement;
import org.kuwaiba.entity.multiple.companies.CorporateCustomer;

/**
 * A simple VLAN
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Entity
public class VLAN extends GenericLogicalElement {
    @ManyToOne
    protected CorporateCustomer customer;

    public CorporateCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(CorporateCustomer customer) {
        this.customer = customer;
    }
}

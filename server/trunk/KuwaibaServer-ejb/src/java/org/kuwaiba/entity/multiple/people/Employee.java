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

package org.kuwaiba.entity.multiple.people;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import org.kuwaiba.core.annotations.NoSerialize;
import org.kuwaiba.entity.qos.services.GenericService;

/**
 * Represents a employee
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class Employee extends GenericPerson {
    /**
     * Corporate identification
     */
    protected String companyId;
    /**
     * Services this employee is related too
     */
    @OneToMany
    @NoSerialize
    protected GenericService services;

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}

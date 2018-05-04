/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.persistence.business;

/**
 * A simplified version of {@link Contact}
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ContactLight {
    /**
     * Contact oid
     */
    private long id;
    /**
     * Contact name
     */
    private String name;
    /**
     * Contact class name
     */
    private String className;
    /**
     * Customer this contact is associated to
     */
    private BusinessObjectLight customer;

    public ContactLight(long id, String name, String className, BusinessObjectLight customer) {
        this.id = id;
        this.name = name;
        this.className = className;
        this.customer = customer;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public BusinessObjectLight getCustomer() {
        return customer;
    }

    public void setCustomer(BusinessObjectLight customer) {
        this.customer = customer;
    }
    
    
}

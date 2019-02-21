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

package org.kuwaiba.apis.persistence.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.persistence.application.Validator;

/**
 * Contains a business object basic information
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class BusinessObjectLight implements Serializable, Comparable<BusinessObjectLight> {

    /**
     * Object's id
     */
    private long id;
    /**
     * Object's name
     */
    private String name;
    /**
     * Class this object is instance of
     */
    private String className;
    /**
     * The result of evaluating all the validators associated to the instance
     */
    private List<Validator> validators;

    public BusinessObjectLight(String className, long id, String name) {
        this.id = id;
        this.name = name;
        this.className = className;
        this.validators = new ArrayList<>();
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

    public List<Validator> getValidators() {
        return validators;
    }

    public void setValidators(List<Validator> validators) {
        this.validators = validators;
    }
    
    @Override
    public String toString() {
        return getName() + " [" + getClassName() + "]";
    }

    @Override
    public int compareTo(BusinessObjectLight o) {
        return this.name.compareTo(o.getName());
    }
    
    @Override
    public boolean equals(Object obj){
       if(obj == null)
           return false;
       if (!(obj instanceof BusinessObjectLight))
           return false;
       return (this.id == ((BusinessObjectLight)obj).getId());
   }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}

/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

/**
 * Contains a business object basic information
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class RemoteBusinessObjectLight implements Serializable, Comparable<RemoteBusinessObjectLight> {

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

    protected RemoteBusinessObjectLight() {}

    public RemoteBusinessObjectLight(long id, String name, String className) {
        this.id = id;
        this.name = name;
        this.className = className;
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
    
    @Override
    public String toString() {
        return getName() + " [" + getClassName() + "]";
    }

    @Override
    public int compareTo(RemoteBusinessObjectLight o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteBusinessObjectLight other = (RemoteBusinessObjectLight) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
    
}

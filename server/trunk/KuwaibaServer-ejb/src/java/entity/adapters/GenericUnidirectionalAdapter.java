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
package entity.adapters;

//import javax.persistence.MappedSuperclass;

import core.annotations.Dummy;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;


/**
 * A Proxy in this context is just workaround used to support "generic" relationships
 * I.e., for physical container connections it's necessary to reference an object implementing the PhysicalNode
 * interface, and many classes can do that (in different branches of the class hierarchy). As far as a know, it's not possible to do this
 * using JPA
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
//@MappedSuperclass
@Entity
@Dummy
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class GenericUnidirectionalAdapter implements Serializable{
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    protected Long id;
    protected Long aSide;
    protected String aSideClass;

    public Long getaSide() {
        return aSide;
    }

    public void setaSide(Long aSide) {
        this.aSide = aSide;
    }

    public String getaSideClass() {
        return aSideClass;
    }

    public void setaSideClass(String aSideClass) {
        this.aSideClass = aSideClass;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * The serialized representation of this is the a side oid
     * @return
     */
    @Override
    public String toString (){
        return aSide.toString();
    }
}

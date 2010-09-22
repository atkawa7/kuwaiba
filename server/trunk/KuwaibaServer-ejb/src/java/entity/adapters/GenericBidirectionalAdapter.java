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
import javax.persistence.Entity;


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
public abstract class GenericBidirectionalAdapter extends GenericUnidirectionalAdapter{
    protected Long bSide;
    protected String bSideClass;

    public String getbSideClass() {
        return bSideClass;
    }

    public void setbSideClass(String bSideClass) {
        this.bSideClass = bSideClass;
    }

    public Long getbSide() {
        return bSide;
    }

    public void setbSide(Long bSide) {
        this.bSide = bSide;
    }
}

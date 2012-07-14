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
package org.kuwaiba.entity.core;

import org.kuwaiba.core.annotations.NoCopy;
import org.kuwaiba.core.annotations.NoSerialize;
import org.kuwaiba.core.annotations.ReadOnly;

import java.util.Date;
import java.util.Calendar;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * The Root of all hierarchy
 * @author Charles Bedon <charles.bedon@zoho.com>
 */
@Entity
public abstract class InventoryObject extends RootObject{

    public static final Long PARENT_ROOT = new Long(0); // This is the id for the single instance of the root object
    public static final Class ROOT_CLASS = DummyRoot.class; // this is the class that represents the root object

    @NoCopy
    @NoSerialize
    @ManyToOne
    protected InventoryObject parent = null;
    /**
     * When was the object created?
     */
    @NoCopy
    @ReadOnly
    @Temporal(value=TemporalType.TIMESTAMP)
    protected Date creationDate = Calendar.getInstance().getTime();

    public InventoryObject(){}

    public InventoryObject getParent() {
        return parent;
    }

    public void setParent(InventoryObject parent) {
        this.parent = parent;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}

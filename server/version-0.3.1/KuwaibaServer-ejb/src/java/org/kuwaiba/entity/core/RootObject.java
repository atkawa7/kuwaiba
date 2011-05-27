/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 * This is the root of the class hierarchy
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED) //Default is SINGLE_TABLE, so all data will be stored in a single table
public abstract class RootObject implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE) //How to generate the primary key (SEQUENCE makes it customizable)
    @NoCopy
    @Column(updatable=false)
    @ReadOnly
    @NoSerialize
    protected Long id; //Primary key

    @Column(nullable=false)
    protected String name = ""; //Name
    /**
     * Is this object locked read-only?
     */
    @Column(nullable=false)
    @NoCopy
    @NoSerialize
    protected Boolean locked= false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isLocked() {
        return locked;
    }

    public void setLocked(Boolean isLocked) {
        this.locked = isLocked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (!(obj instanceof RootObject))
            return false;

        if (this.id == null || ((RootObject) obj).id == null)
            return false;

        if (this.id.longValue() != ((RootObject) obj).id.longValue())
            return false;

        return true;
    }

    @Override
    public String toString() {
        return getName() +" ["+getClass().getSimpleName()+"]"; //NOI18N
    }
}
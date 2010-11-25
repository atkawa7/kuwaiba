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
package entity.core;

import core.annotations.Administrative;
import core.annotations.NoCopy;
import java.io.Serializable;

//Annotations
import java.util.Date;
import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * The Root of all hierarchy
 * @author Charles Bedon <charles.bedon@zoho.com>
 */
@Entity
@Administrative
@Inheritance(strategy=InheritanceType.JOINED) //Default is SINGLE_TABLE, so all data will be stored in a single table
public abstract class RootObject implements Serializable, Cloneable {

    public static final Long PARENT_ROOT = new Long(0); // This is the id for the single instance of the root object
    public static final Class ROOT_CLASS = DummyRoot.class; // this is the class that represents the root object

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE) //How to generate the primary key (SEQUENCE makes it customizable)
    @NoCopy
    @Column(updatable=false)
    protected Long id; //Primary key
    @Column(nullable=false)
    protected String name = ""; //Name
    /**
     * Is this object locked read-only?
     */
    @Column(nullable=false)
    @NoCopy
    protected Boolean isLocked= false;
    @NoCopy
    protected Long parent = null;
    /**
     * When was the object created?
     */
    @NoCopy
    @Temporal(value=TemporalType.TIMESTAMP)
    protected Date creationDate = Calendar.getInstance().getTime();

    public RootObject(){}

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean isLocked) {
        this.isLocked = isLocked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RootObject other = (RootObject) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    //@Override
    public boolean equals(RootObject object) {
        if (!(object instanceof RootObject)) {
            return false;
        }
        RootObject other = (RootObject) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.core.Object[id=" + id + "]";
    }

}

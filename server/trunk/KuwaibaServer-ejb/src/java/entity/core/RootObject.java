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
package entity.core;

import core.annotations.NoCopy;
import java.io.Serializable;

//Annotations
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


/**
 * The Root of all hierarchy
 * @author Charles Bedon <charles.bedon@zoho.com>
 */
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS) //Por defecto el valor es SINGLE_TABLE, con lo cual, todas las subclases se mapena en la misma tabla
@Table(name="RootObject") //Esto le dice con qué nombre crear la tabla. El defaul, es decir, si uno no usa esta anotación es el mismo nombre de la tabla
public abstract class RootObject implements Serializable, Cloneable {

    public static final Long PARENT_ROOT = new Long(0); // This is the id for the single instance of the root object
    public static final Class ROOT_CLASS = DummyRoot.class; // this is the class that represents the root object

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE) //Esto le dice cómo generar la llave primaria (SEQUENCE lo hacecustomizable, uno le puede decir cómo la genere)
    @NoCopy
    protected Long id; //Llave primaria
    @Column(nullable=false)
    protected String name = ""; //Nombre
    @Column(nullable=false)
    @NoCopy
    protected Boolean isLocked= false; //indica si un objeto está bloqueado para sólo lectura
    @NoCopy
    protected Long parent = null;

    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent;
    }

    public Boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(boolean isLocked) {
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
    //TODO: Verificar si efectivamente es necesario
    public boolean equals(RootObject object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
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

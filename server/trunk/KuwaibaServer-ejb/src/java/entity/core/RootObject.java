/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.core;

import java.io.Serializable;

//Anotaciones
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


/**
 *
 * @author Charles Bedon <charles.bedon@zoho.com>
 */
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS) //Por defecto el valor es SINGLE_TABLE, con lo cual, todas las subclases se mapena en la misma tabla
@Table(name="RootObject") //Esto le dice con qué nombre crear la tabla. El defaul, es decir, si uno no usa esta anotación es el mismo nombre de la tabla
public abstract class RootObject implements Serializable {

    public static final Long PARENT_ROOT = new Long(0); // Indica el valor reservado para aquellos objetos cuyo padre es el root

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE) //Esto le dice cómo generar la llave primaria (SEQUENCE lo hacecustomizable, uno le puede decir cómo la genere)
    protected Long id; //Llave primaria
    @Column(nullable=false)
    protected String name = ""; //Nombre
    @Column(nullable=false)
    protected Boolean isLocked= false; //indica si un objeto está bloqueado para sólo lectura
    
    //protected RootObject parent = null; //Indica el oid del padre. El 0 indica que su padre es nulo (por ahora dejémolo null)
    //protected List<RootObject> children;
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

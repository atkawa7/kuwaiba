/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.core.metamodel;

import core.annotations.Administrative;
import core.annotations.Metadata;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Permite guardar la información acerca de los objetos que se lockean
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Administrative //Anotación personalizada para marcarla como que no se debe pasar a los
          //clientes para que administren su meta, ya que ella es una clase de utilidad
@Table(name="locks")
public class LockEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public Long getId() {
        return id;
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
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof LockEntry)) {
            return false;
        }
        LockEntry other = (LockEntry) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.core.metadata.LockManager[id=" + id + "]";
    }

}

package entity.core;

import core.annotations.Dummy;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * This is a dummy class used *only* to hold container information about the root node
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Dummy
public class DummyRoot implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
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
        if (!(object instanceof DummyRoot)) {
            return false;
        }
        DummyRoot other = (DummyRoot) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.core.DummyRoot[id=" + id + "]";
    }

}

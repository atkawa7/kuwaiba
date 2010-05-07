/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.session;

import core.annotations.Hidden;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Hidden
public class ClientDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(nullable=false)
    protected String platform;
    
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
        if (!(object instanceof ClientDetail)) {
            return false;
        }
        ClientDetail other = (ClientDetail) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.session.ClientDetail[id=" + id + "]";
    }

}

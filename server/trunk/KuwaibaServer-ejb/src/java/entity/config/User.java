/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.config;

import core.annotations.Administrative;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import java.util.List;

/**
 *
 * @author dib
 */
@Entity
@Administrative
@Table(name="Users") //User un nombre de tabla reservado
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String cellphone;
    private String position;
    private List<UserGroup> groups;
    

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
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.config.Users[id=" + id + "]";
    }

}

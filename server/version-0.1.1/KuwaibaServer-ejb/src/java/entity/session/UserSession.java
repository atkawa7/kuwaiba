/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package entity.session;

import core.annotations.Administrative;
import entity.config.User;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Administrative
public class UserSession implements Serializable {
    private static final long serialVersionUID = 1L;
    @OneToOne
    @JoinColumn(nullable=false)
    protected User user;
    @Temporal(TemporalType.TIMESTAMP)
    protected Date loginTime;
    @Column(nullable=false,length=32)
    protected String token;
    @Column(length=15,nullable=false)
    protected String ipAddress;
    @OneToOne
    protected ClientDetail detail; //Detalles de cliente, como plataforma, versión de componentes, etc
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
        if (!(object instanceof UserSession)) {
            return false;
        }
        UserSession other = (UserSession) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    public ClientDetail getDetail() {
        return detail;
    }

    public void setDetail(ClientDetail detail) {
        this.detail = detail;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "entity.session.UserSession[id=" + id + "]";
    }

}

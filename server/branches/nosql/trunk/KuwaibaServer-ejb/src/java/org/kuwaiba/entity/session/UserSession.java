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

package org.kuwaiba.entity.session;

import org.kuwaiba.entity.core.ApplicationObject;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.kuwaiba.util.MetadataUtils;

/**
 * Represents a single user session
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class UserSession extends ApplicationObject{
    @OneToOne
    @JoinColumn(nullable=false)
    protected User user;
    @Temporal(TemporalType.TIMESTAMP)
    protected Date loginTime = Calendar.getInstance().getTime();
    @Column(nullable=false,length=50)
    protected String token;
    @Column(length=15,nullable=false)
    protected String ipAddress;
    /**
     * Client details like OS, components version, etc
     */
    @OneToOne
    protected ClientDetail detail;

    public UserSession() {
    }

    public UserSession(User user) {
        this.user = user;
        this.token = user.getId() + MetadataUtils.getMD5Hash(loginTime.toString());
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
        return getUser() +"["+getLoginTime()+"]"; //NOI18N
    }

}

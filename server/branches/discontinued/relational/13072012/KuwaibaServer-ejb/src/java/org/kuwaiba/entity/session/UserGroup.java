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

import org.kuwaiba.core.annotations.NoCopy;
import org.kuwaiba.core.annotations.NoSerialize;
import org.kuwaiba.entity.core.ApplicationObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * This class represents a group of users. Those users will have the same privileges
 * @author Charles Edward Bedón Cortázar <charles.bedon@zoho.com>
 */
@Entity
@Table(name="Groups")
public class UserGroup extends ApplicationObject { //UserGroup is a keyword in JPQL

    @NoCopy
    @Temporal(value=TemporalType.TIMESTAMP)
    private Date creationDate;
    private String description;
    
    @ManyToMany(mappedBy = "groups")
    @NoSerialize
    @JoinColumn(nullable=true)
    protected List<User> users = new ArrayList<User>();

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return getName();
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}

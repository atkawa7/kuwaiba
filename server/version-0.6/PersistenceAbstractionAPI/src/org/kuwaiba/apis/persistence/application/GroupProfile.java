/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.persistence.application;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a group
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GroupProfile implements Serializable{
    /**
     * Group's id (oid)
     */
    private long id;
    /**
     * Group's name
     */
    private String name;
    /**
     * Group's description
     */
    private String description;
    /**
     * Group's creation date (in milliseconds, it's a timestamp)
     */
    private long creationDate;
    /**
     * Group's users
     */
    private List<UserProfile> users;
    /**
     * Group's privileges. See class Privileges for the complete list of supported privileges
     */
    private List<Privilege> privileges;

    public GroupProfile() {
    }

    public GroupProfile(long id, String name, String description, long creationDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
    }

    public GroupProfile(long id, String name, String description, long creationDate,
            List<UserProfile> users, List<Privilege> privileges) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.users = users;
        this.privileges = privileges;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long groupId) {
        this.id = groupId;
    }

    
    public String getDescription() {
        return description;
    }

    public void setDescription(String groupDescription) {
        this.description = groupDescription;
    }

    public String getName() {
        return name;
    }

    public void setName(String groupName) {
        this.name = groupName;
    }

    public List<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
    }

    public List<UserProfile> getUsers() {
        return users;
    }

    public void setUsers(List<UserProfile> users) {
        this.users = users;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }
}

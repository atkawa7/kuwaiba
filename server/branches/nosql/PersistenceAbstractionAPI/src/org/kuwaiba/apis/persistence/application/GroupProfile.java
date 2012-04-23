/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
    public static final String PROPERTY_ID = "id"; //NOI18N
    public static final String PROPERTY_GROUPNAME = "username"; //NOI18N
    public static final String PROPERTY_DESCRIPTION = "description"; //NOI18N
    public static final String PROPERTY_PRIVILEGES = "privileges"; //NOI18N
    public static final String PROPERTY_CREATION_DATE = "creationDate"; //NOI18N

    /**
     * Group's id (oid)
     */
    private Long id;
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
    private Long creationDate;
    /**
     * Group's users
     */
    private List<UserProfile> users;
    /**
     * Group's privileges. See class Privileges for the complete list of supported privileges
     */
    private List<Integer> privileges;

    public GroupProfile() {
    }

    public GroupProfile(Long id, String name, String description, Long creationDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
    }

    public GroupProfile(Long id, String name, String description, Long creationDate,
            List<UserProfile> users, List<Integer> privileges) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.users = users;
        this.privileges = privileges;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long groupId) {
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

    public List<Integer> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Integer> privileges) {
        this.privileges = privileges;
    }

    public List<UserProfile> getUsers() {
        return users;
    }

    public void setUsers(List<UserProfile> users) {
        this.users = users;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }
}

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
 * Represents the privileges associated to a user
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class UserProfile implements Serializable{

    public static final String PROPERTY_ID = "id"; //NOI18N
    public static final String PROPERTY_USERNAME = "username"; //NOI18N
    public static final String PROPERTY_DESCRIPTION = "description"; //NOI18N
    public static final String PROPERTY_PASSWORD = "password"; //NOI18N
    public static final String PROPERTY_SALT = "salt"; //NOI18N
    public static final String PROPERTY_FIRST_NAME = "firstName"; //NOI18N
    public static final String PROPERTY_LAST_NAME = "lastName"; //NOI18N
    public static final String PROPERTY_PRIVILEGES = "privileges"; //NOI18N
    public static final String PROPERTY_CREATION_DATE = "creationDate"; //NOI18N
    public static final String PROPERTY_ENABLED = "enabled"; //NOI18N
    /**
     * User's id (oid)
     */
    private Long id;
    /**
     * User's username
     */
    private String userName;
    /**
     * User's first name
     */
    private String firstName;
    /**
     * User's last name
     */
    private String lastName;
    /**
     * User's creation date (a time stamp)
     */
    private Long creationDate;
    /**
     * Is this user enabled?
     */
    private Boolean enabled;
    /**
     * User's privileges. See class Privileges for the complete list of supported privileges
     */
    private List<GroupProfile> groups;
    /**
     * User's privileges. See class Privileges for the complete list of supported privileges
     */
    private List<Integer> privileges;

    public UserProfile() {
    }

    public UserProfile(Long id, String userName, String firstName, String lastName, Boolean enabled, Long creationDate,
            List<Integer> privileges) {
        this.id = id;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.privileges = privileges;
        this.enabled = enabled;
        this.creationDate = creationDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String login) {
        this.userName = login;
    }

    public List<GroupProfile> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupProfile> groups) {
        this.groups = groups;
    }

    public List<Integer> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Integer> privileges) {
        this.privileges = privileges;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }


}

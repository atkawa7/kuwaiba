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

import java.util.List;

/**
 * Represents a group
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GroupProfile {
    /**
     * Group's id (oid)
     */
    private Long groupId;
    /**
     * Group's name
     */
    private String groupName;
    /**
     * Group's description
     */
    private String groupDescription;
    /**
     * Group's users
     */
    private List<UserProfile> users;
    /**
     * Group's privileges. See class Privileges for the complete list of supported privileges
     */
    private List<Integer> privileges;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    
    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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
}

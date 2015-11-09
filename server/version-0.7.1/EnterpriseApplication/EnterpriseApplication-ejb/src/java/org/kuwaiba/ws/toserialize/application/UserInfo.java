/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.ws.toserialize.application;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.application.UserProfile;

/**
 * Wrapper for entity class User
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UserInfo extends UserInfoLight {
   /**
    * User's first name
    */
    private String firstName;
   /**
    * User's last name
    */
    private String lastName;
   /**
    * User's creation date
    */
    private long creationDate;
   /**
    * Indicates if this account is enabled
    */
    protected boolean enabled;

    private GroupInfoLight[] groups;
    /**
     * Group's privileges
     */
    private PrivilegeInfo[] privileges;
    
    //No-arg constructor required
    public UserInfo() { }
    
    public UserInfo(UserProfile user){
        super(user);
        this.enabled = user.isEnabled();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.creationDate = user.getCreationDate();
        if(user.getGroups() != null){
            this.groups = new GroupInfoLight[user.getGroups().size()];
            for(int i=0; i < user.getGroups().size(); i++)
                this.groups[i] = new GroupInfoLight(user.getGroups().get(i));
        }
        
        this.privileges = new PrivilegeInfo[user.getPrivileges().size()];
        for(int i = 0; i < user.getPrivileges().size(); i++)
                privileges[i] = new PrivilegeInfo(user.getPrivileges().get(i));
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

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public GroupInfoLight[] getGroups() {
        return groups;
    }

    public void setGroups(GroupInfoLight[] groups) {
        this.groups = groups;
    }

    public PrivilegeInfo[] getPrivileges() {
        return privileges;
    }

    public void setPrivileges(PrivilegeInfo[] privileges) {
        this.privileges = privileges;
    }

    
}
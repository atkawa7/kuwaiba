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

package org.kuwaiba.ws.toserialize.application;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.UserProfile;

/**
 * Wrapper for entity class User
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UserInfo implements Serializable {
    /**
     * User's oid
     */
    private long oid;
   /**
    * User's login name
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
    * User's creation date
    */
    private long creationDate;
   /**
    * Indicates if this account is enabled
    */
    protected boolean enabled;

    private UserGroupInfoLight[] groups;

    private int[] privileges;

    public UserInfo(){}

    public UserInfo(UserProfile user){

        this.oid = user.getId();
        this.userName = user.getUserName();
        this.enabled = user.isEnabled();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        if (user.getCreationDate() == null)
            this.creationDate = 0;
        else
            this.creationDate = user.getCreationDate();
        if (user.getPrivileges() == null)
            this.privileges = new int[0];
        else
            this.privileges = user.getPrivileges();

        List<GroupProfile> entityGroups = user.getGroups();
        if (entityGroups == null)
            this.groups = null;
        else{
            this.groups = new UserGroupInfoLight[entityGroups.size()];
            int i = 0;
            for (GroupProfile group : entityGroups){
                this.groups[i] = new UserGroupInfoLight(group);
                i++;
            }
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public UserGroupInfoLight[] getGroups() {
        return groups;
    }

    public void setGroups(UserGroupInfoLight[] groups) {
        this.groups = groups;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.enabled = isEnabled;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }
}
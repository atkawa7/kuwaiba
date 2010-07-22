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

package core.toserialize;

import entity.config.User;
import entity.config.UserGroup;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Wrapper for entity class User
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UserInfo {
    /**
     * User's oid
     */
    private Long oid;
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
    private Long creationDate;
   /**
    * Indicates if this account is enabled
    */
    protected Boolean isEnabled;

    private UserGroupInfoLight[] groups;
    //private PrivilegeInfo[] privileges;

    public UserInfo(){}
    public UserInfo(User _user){

        this.oid = _user.getId();
        this.userName = _user.getUsername();
        this.isEnabled = _user.getIsEnabled();
        this.firstName = _user.getFirstName();
        this.lastName = _user.getLastName();
        if (_user.getCreationDate() == null)
            this.creationDate = null;
        else
            this.creationDate = _user.getCreationDate().getTime();
        List<UserGroup> entityGroups = _user.getGroups();
        if (entityGroups == null)
            this.groups = null;
        else{
            this.groups = new UserGroupInfoLight[entityGroups.size()];
            int i = 0;
            for (UserGroup group : entityGroups){
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

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }

    public Boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }


}
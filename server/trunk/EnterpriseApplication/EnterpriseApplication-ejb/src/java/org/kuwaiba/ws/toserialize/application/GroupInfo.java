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

package org.kuwaiba.ws.toserialize.application;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.application.GroupProfile;

/**
 * Wrapper for entity class UserGroup
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupInfo extends GroupInfoLight{
    
    /**
     * Membres of the group;
     */
    protected UserInfo[] users;
    /**
     * Object's creation date. Since there's no a seamless map for java.util.Date
     * (xsd:date has less information than Date, so it's mapped into Calendar), we use a long instead (a timestamp)
     */
    protected long creationDate;
    /**
     * UserGroup's description
     */
    protected String description;
    /**
     * Group's privileges
     */
    private PrivilegeInfo[] privileges;

    //No-arg constructor required
    public GroupInfo(){}
    
    public GroupInfo(GroupProfile group){
        super (group);
        this.description = group.getDescription();
        this.creationDate = group.getCreationDate();
        users = new UserInfo[group.getUsers().size()];
        privileges = new PrivilegeInfo[group.getPrivileges().size()];
        
        for(int i=0; i < group.getUsers().size(); i++)
            users[i]= new UserInfo(group.getUsers().get(i));
        
        for(int i=0; i < group.getPrivileges().size(); i++)
            privileges[i] = new PrivilegeInfo(group.getPrivileges().get(i));
    } 

    public UserInfoLight[] getUsers() {
        return users;
    }

    public void setUsers(UserInfo[] users) {
        this.users = users;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PrivilegeInfo[] getPrivileges() {
        return privileges;
    }

    public void setPrivileges(PrivilegeInfo[] privileges) {
        this.privileges = privileges;
    }
}

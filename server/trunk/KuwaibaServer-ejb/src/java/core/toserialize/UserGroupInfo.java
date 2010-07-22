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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Wrapper for entity class UserGroup
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UserGroupInfo extends UserGroupInfoLight{
    protected UserInfo[] members;
    /**
     * Object's creation date. Since there's no a seamlessly map for java.util.Date
     * (xsd:date has less information than Date, so it's mapped into Calendar), we use a Long instead (a timestamp)
     */
    protected Long creationDate;
    /**
     * Group's description
     */
    protected String description;
    //private PrivilegeInfo privileges;

    public UserGroupInfo(){}
    public UserGroupInfo(UserGroup group){
        super (group);
        this.members = new UserInfo[group.getUsers().size()];
        this.description = group.getDescription();
        this.creationDate = group.getCreationDate().getTime();
        int i = 0;
        for (User member : group.getUsers())
            this.members[i] = new UserInfo(member);
    }

    public String getDescription() {
        return description;
    }

    public UserInfo[] getMembers() {
        return members;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }
}

/*
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.UserProfile;

/**
 * Wrapper for entity class UserGroup
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UserGroupInfo extends UserGroupInfoLight{
    protected UserInfo[] members;
    /**
     * Object's creation date. Since there's no a seamless map for java.util.Date
     * (xsd:date has less information than Date, so it's mapped into Calendar), we use a Long instead (a timestamp)
     */
    protected Long creationDate;
    /**
     * UserGroup's description
     */
    protected String description;

    public UserGroupInfo(){}
    public UserGroupInfo(GroupProfile group){
        super (group);
        this.members = new UserInfo[group.getUsers().size()];
        this.description = group.getDescription();
        if (group.getCreationDate() != null)
            this.creationDate = group.getCreationDate();
        else
            this.creationDate = null;
        int i = 0;
        for (UserProfile member : group.getUsers())
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

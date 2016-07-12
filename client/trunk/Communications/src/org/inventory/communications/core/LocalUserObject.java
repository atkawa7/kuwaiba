/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.communications.core;

import org.kuwaiba.wsclient.GroupInfoLight;
import org.kuwaiba.wsclient.UserInfo;

/**
 * Implementation for the local representation of an application user
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalUserObject extends LocalUserObjectLight {
    private String firstName;
    private String lastName;
    private LocalUserGroupObjectLight[] groups;

    public LocalUserObject(UserInfo user) {
        super(user.getId(), user.getUserName());
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        if (user.getGroups() == null)
            this.groups = null;
        else{
            groups = new LocalUserGroupObjectLight[user.getGroups().size()];

            int i = 0;
            for(GroupInfoLight group : user.getGroups()){
                groups[i] = new LocalUserGroupObjectLight(group);
                i++;
            }
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public LocalUserGroupObjectLight[] getGroups() {
        return groups;
    }

    public String getLastName() {
        return lastName;
    }

    public void setGroups(LocalUserGroupObjectLight[] groups) {
        this.groups = groups;
    }
    
    @Override
    public String toString() {
        return firstName == null || lastName == null ? getUserName() : String.format("%s, %s - %s", lastName, firstName, getUserName());
    }
}

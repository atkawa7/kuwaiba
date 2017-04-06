/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */

package org.inventory.communications.core;

import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.wsclient.GroupInfo;
import org.inventory.communications.wsclient.PrivilegeInfo;
import org.inventory.communications.wsclient.UserInfo;

/**
 * Implementation for the local representation of an application users group
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalUserGroupObject extends LocalUserGroupObjectLight {
    private List<LocalPrivilege> privileges;
    private List<LocalUserObject> users;
    
    public LocalUserGroupObject(GroupInfo group) {
        super(group);
        this.privileges = new ArrayList<>();
        this.users = new ArrayList<>();
        for (PrivilegeInfo remotePrivilege : group.getPrivileges())
            privileges.add(new LocalPrivilege(remotePrivilege.getFeatureToken(), remotePrivilege.getAccessLevel()));
        
        for (UserInfo remoteUser : group.getUsers()) {
            List<LocalPrivilege> userPrivileges = new ArrayList<>();
            for (PrivilegeInfo remotePrivilege : remoteUser.getPrivileges())
                userPrivileges.add(new LocalPrivilege(remotePrivilege.getFeatureToken(), remotePrivilege.getAccessLevel()));
            
            users.add(new LocalUserObject(remoteUser.getId(), remoteUser.getUserName(), 
                    remoteUser.getFirstName(), remoteUser.getLastName(), remoteUser.isEnabled(), 
                    remoteUser.getType(), userPrivileges));
            }
    }

    public List<LocalPrivilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<LocalPrivilege> privileges) {
        this.privileges = privileges;
    }

    public List<LocalUserObject> getUsers() {
        return users;
    }

    public void setUsers(List<LocalUserObject> users) {
        this.users = users;
    }
}

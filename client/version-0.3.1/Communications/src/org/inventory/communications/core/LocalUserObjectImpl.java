/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

import org.inventory.core.services.api.session.LocalUserGroupObjectLight;
import org.inventory.core.services.api.session.LocalUserObject;
import org.kuwaiba.wsclient.UserGroupInfoLight;
import org.kuwaiba.wsclient.UserInfo;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation for the local representation of an application user
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@ServiceProvider(service=LocalUserObject.class)
public class LocalUserObjectImpl implements LocalUserObject {

    private Long oid;
    private String userName;
    private String firstName;
    private String lastName;
    private LocalUserGroupObjectLight[] groups;

    public LocalUserObjectImpl() {    }

    public LocalUserObjectImpl(UserInfo user) {
        this.oid = user.getOid();
        this.userName = user.getUserName();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        if (user.getGroups() == null)
            this.groups = null;
        else{
            groups = new LocalUserGroupObjectLight[user.getGroups().size()];

            int i = 0;
            for(UserGroupInfoLight group : user.getGroups()){
                groups[i] = new LocalUserGroupObjectLightImpl(group);
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

    public Long getOid() {
        return oid;
    }

    public String getUserName() {
        return userName;
    }

    public void setGroups(LocalUserGroupObjectLight[] _groups) {
        this.groups = _groups;
    }
}

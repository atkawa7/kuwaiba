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

import java.util.List;

/**
 * Implementation for the local representation of an application user
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalUserObject extends LocalUserObjectLight {
    
    private List<LocalPrivilege> privileges;

    public LocalUserObject(long userId, String username, String firstName, String lastName, 
            boolean enabled, int type, List<LocalPrivilege> privileges) {
        super(userId, username, firstName, lastName, enabled, type);
        this.privileges = privileges;
    }

    public List<LocalPrivilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<LocalPrivilege> privileges) {
        this.privileges = privileges;
    }
    
    @Override
    public String toString() {
        return getFirstName() == null || getLastName() == null || getFirstName().isEmpty() || getLastName().isEmpty() ? getUserName() : String.format("%s, %s  (%s)", getLastName(), getFirstName(), getUserName());
    }
}

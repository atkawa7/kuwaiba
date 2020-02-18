/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

import java.beans.PropertyVetoException;
import java.util.List;

/**
 * Implementation for the local representation of an application user
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LocalUserObject extends LocalUserObjectLight {
    
    public static final String PROPERTY_PRIVILEGES = "privileges"; //NOI18N
    private List<LocalPrivilege> privileges;

    public LocalUserObject(long userId, String username, String firstName, String lastName, 
            boolean enabled, int type, String email, List<LocalPrivilege> privileges) {
        super(userId, username, firstName, lastName, enabled, type, email);
        this.privileges = privileges;
    }

    public List<LocalPrivilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<LocalPrivilege> privileges) {
        this.privileges = privileges;
    }
    
    public LocalPrivilege getPrivilege(String featureToken) {
        for (LocalPrivilege privilege : privileges) {
            if (privilege.getFeatureToken().equals(featureToken))
                return privilege;
        }
        return null;
    }
    
    /**
     * Sets a privilege. If the privilege already exists, the access level is updated, otherwise, a new privilege is created
     * @param featureToken
     * @param accessLevel New access level. Check ACCESS_LEVEL* for possible values. Use ACCESS_LEVEL_UNSET to remove an existing privilege 
     */
    public void setPrivilege(String featureToken, int accessLevel) {
        LocalPrivilege privilege = new LocalPrivilege(featureToken, accessLevel);
        
        if (accessLevel == LocalPrivilege.ACCESS_LEVEL_UNSET) { //Delete an existing privilege
            try {       
                firePropertyChange(PROPERTY_PRIVILEGES, privilege, null);
                privileges.remove(privilege);
            } catch (PropertyVetoException ex) { }
        } else {
            try {
                LocalPrivilege newPrivilege = new LocalPrivilege(featureToken, accessLevel);
                int existingPrivilegeIndex = privileges.contains(privilege) ? privileges.indexOf(privilege) : -1;
                if (existingPrivilegeIndex != -1) {
                    LocalPrivilege existingPrivilege = privileges.get(existingPrivilegeIndex);
                    
                    firePropertyChange(PROPERTY_PRIVILEGES, existingPrivilege, newPrivilege);
                    privileges.set(existingPrivilegeIndex, newPrivilege);
                } else {
                    firePropertyChange(PROPERTY_PRIVILEGES, null, newPrivilege);
                    privileges.add(newPrivilege);
                }
            } catch (PropertyVetoException ex) { }
        }
    }
    
    @Override
    public String toString() {
        return getFirstName() == null || getLastName() == null || getFirstName().isEmpty() || getLastName().isEmpty() ? getUserName() : String.format("%s, %s  (%s)", getLastName(), getFirstName(), getUserName());
    }
}

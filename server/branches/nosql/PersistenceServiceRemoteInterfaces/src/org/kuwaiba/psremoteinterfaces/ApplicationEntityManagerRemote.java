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

package org.kuwaiba.psremoteinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;

/**
 * RMI wrapper for the ApplicationEntityManager interface
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface ApplicationEntityManagerRemote extends Remote{
    public static final String REFERENCE_AEM = "aem";
    /**
     * Verifies if a pair username/password matches
     * @param username User name
     * @param password password (in plain text)
     * @return The user's profile. Null if the username/password don't match or any of them is null
     */
    public UserProfile login(String username, String password) throws RemoteException;
    /**
     * Creates a user
     * @param userName New user's name. Mandatory.
     * @param password New user's password
     * @param firstName New user's first name
     * @param lastName New user's last name
     * @param enabled Shall the new user be enabled by default
     * @param privileges New user's privileges. See Privileges class documentation for a list of available permissions. Use null for none
     * @param groups A list with the ids of the groups this user will belong to. Use null for none
     * @return The id of the newly created user
     * @throws InvalidArgumentException Thrown if the username is null or empty or the username already exists
     * @throws ObjectNotFoundException Thrown if any of the ids provided for the groups does not belong to an existing group
     */
    public Long createUser(String userName, String password, String firstName,
            String lastName, Boolean enabled, List<Integer> privileges, List<Long> groups)
            throws InvalidArgumentException, ObjectNotFoundException, RemoteException;
    /**
     * Creates a user
     * @param userName New user's name. Mandatory.
     * @param password New user's password. Use null to leave it unchanged
     * @param firstName New user's first name. Use null to leave it unchanged
     * @param lastName New user's last name. Use null to leave it unchanged
     * @param privileges New user's privileges. See Privileges class documentation for a list of available permissions. Use null to leave it unchanged
     * @param groups A list with the ids of the groups this user will belong to. Use null to leave it unchanged
     * @return The id of the newly created user
     * @throws InvalidArgumentException Thrown if the username is null or empty or the username already exists
     * @throws ObjectNotFoundException Thrown if any of the ids provided for the groups does not belong to an existing group
     */
    public void setUserProperties(String userName, String password, String firstName,
            String lastName, List<Integer> privileges, List<Long> groups)
            throws InvalidArgumentException, ObjectNotFoundException, RemoteException;
}

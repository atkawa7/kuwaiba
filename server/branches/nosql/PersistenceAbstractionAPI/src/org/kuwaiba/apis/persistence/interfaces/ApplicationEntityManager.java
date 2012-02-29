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

package org.kuwaiba.apis.persistence.interfaces;

import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;

/**
 * This is the entity in charge of manipulating application objects such as users, views, etc
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface ApplicationEntityManager {
    /**
     * Verifies if a pair username/password matches
     * @param username User name
     * @param password password (in plain text)
     * @return The user's profile
     */
    public UserProfile login(String username, String password);
    /**
     *
     * @param userName New user's name. Mandatory.
     * @param password New user's password
     * @param firstName New user's first name
     * @param lastName New user's last name
     * @param privileges New user's privileges. See Privileges class documentation for a list of available permissions
     * @param groups A list with the ids of the groups this user will belong to
     * @return The id of the newly created user
     * @throws InvalidArgumentException Thrown if the username is null or empty
     * @throws ObjectNotFoundException Thrown if any of the ids provided for the groups does not belong to an existing group
     */
    public Long createUser(String userName, String password, String firstName, 
            String lastName, List<Integer> privileges, List<Long> groups)
            throws InvalidArgumentException, ObjectNotFoundException;
    /**
     * Modifies user properties
     * @param oid User's oid
     * @param properties List of properties to be changed (a set of pairs name-value). Note that
     * the user's groups can also be set here. For the sake of simpleness, you have to provide the
     * group ids separated by a comma (","). E.g "1,4,7" will try to relate the user to the groups with
     * ids 1, 4 and 7. The values must be long-convertible or an InvalidArgumentException will be raised
     * @throws InvalidArgumentException If any of the property names provided does not exist
     */
    public void setUserProperties(Long oid, HashMap<String,String> properties) throws InvalidArgumentException;
}

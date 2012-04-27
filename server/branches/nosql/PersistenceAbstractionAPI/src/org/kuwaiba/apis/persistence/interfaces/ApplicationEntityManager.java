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

import java.util.List;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;

/**
 * This is the entity in charge of manipulating application objects such as users, views, etc
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface ApplicationEntityManager {
    /**
     * Verifies if a pair username/password matches
     * @param username User name
     * @param password password (in plain text)
     * @return The user's profile. Null if the username/password don't match or any of them is null
     */
    public UserProfile login(String username, String password);
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
            throws InvalidArgumentException, ObjectNotFoundException;
    
    /**
     * Set user attributes (group membership is managed using other methods)
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
    public void setUserProperties(Long oid, String userName, String password, String firstName,
            String lastName, Boolean enabled, List<Integer> privileges, List<Long> groups) throws InvalidArgumentException, ObjectNotFoundException;

    /**
     * Creates a group
     * @param name
     * @param description
     * @param creationDate
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public Long createGroup(String groupName, String description)throws InvalidArgumentException, ObjectNotFoundException;

    /**
     * Retrieves the user list
     * @return An array of UserProfile
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public List<UserProfile> getUsers()throws InvalidArgumentException, ObjectNotFoundException;

    /**
     * Retrieves the group list
     * @return An array of GroupProfile
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public List<GroupProfile> getGroups()throws InvalidArgumentException, ObjectNotFoundException;

    /**
     * Creates a new user
     * @return The newly created user
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public UserProfile addUser()throws InvalidArgumentException, ObjectNotFoundException;

    /**
     * Set user attributes (group membership is managed using other methods)
     * @param groupName
     * @param description
     * @param creationDate
     * @param privileges
     * @return
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public void setGroupProperties(Long oid, String groupName, String description,
            List<Integer> privileges)throws InvalidArgumentException, ObjectNotFoundException;

    /**
     * Creates a new group
     * @return
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public GroupProfile addGroup()throws InvalidArgumentException, ObjectNotFoundException;

    /**
     * Removes a list of users
     * @param oids
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public void deleteUsers(Long[] oids)throws InvalidArgumentException, ObjectNotFoundException;

    /**
     * Removes a list of groups
     * @param oids
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public void deleteGroups(Long[] oids)throws InvalidArgumentException, ObjectNotFoundException;

    /**
     * Assigns groups to a user
     * @param groupsOids
     * @param userOid
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public void addGroupsToUser(List<Long> groupsOids, Long userOid)throws InvalidArgumentException, ObjectNotFoundException;

    /**
     * Removes groups to a user
     * @param groupsOids
     * @param userOid
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public void removeGroupsFromUser(List<Long> groupsOids, Long userOid)throws InvalidArgumentException, ObjectNotFoundException;

    /**
     * Creates a list type item
     * @param className List type
     * @param name new item's name
     * @param displayName new item's display name
     * @return new item's id
     * @throws MetadataObjectNotFoundException if className is not an existing class
     * @throws InvalidArgumentException if the class provided is not a list type
     */
    public Long createListTypeItem(String className, String name, String displayName)
            throws MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * Retrieves all the items related to a given list type
     * @param className list type
     * @return A list of RemoteBusinessObjectLight instances representing the items
     * @throws MetadataObjectNotFoundException if className is not an existing class
     * @throws InvalidArgumentException if the class provided is not a list type
     */
    public List<RemoteBusinessObjectLight> getListTypeItems(String className)
            throws MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * Get the possible list types
     * @return A list of ClassMetadataLight instances representing the possible list types
     * @throws MetadataObjectNotFoundException
     */
    public List<ClassMetadataLight> getInstanceableListTypes()
            throws MetadataObjectNotFoundException;
}

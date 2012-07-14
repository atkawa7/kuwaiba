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
import org.kuwaiba.apis.persistence.application.CompactQuery;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.ResultRecord;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.application.View;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;

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
     */
    public long createUser(String userName, String password, String firstName,
            String lastName, Boolean enabled, List<Integer> privileges, List<Long> groups)
            throws InvalidArgumentException, RemoteException;

    /**
     * Updates the information of a given user using the id to search for it
     * @param oid user's oid. Mandatory
     * @param userName New user's name.
     * @param password New user's password. Use null to leave it unchanged
     * @param firstName New user's first name. Use null to leave it unchanged
     * @param lastName New user's last name. Use null to leave it unchanged
     * @param privileges New user's privileges. See Privileges class documentation for a list of available permissions. Use null to leave it unchanged
     * @param groups A list with the ids of the groups this user will belong to. Use null to leave it unchanged
     * @return The id of the newly created user
     * @throws InvalidArgumentException Thrown if the username is null or empty or the username already exists
     * @throws ApplicationObjectNotFoundException Thrown if any of the ids provided for the groups does not belong to an existing group
     */
    public void setUserProperties(long oid, String userName, String password, String firstName,
            String lastName, Boolean enabled, List<Integer> privileges, List<Long> groups)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, RemoteException;

    /**
     * Updates the information of a given user using the id to search for it
     * @param formerUsername Former username. Mandatory
     * @param userName New user's name. Mandatory.
     * @param password New user's password. Use null to leave it unchanged
     * @param firstName New user's first name. Use null to leave it unchanged
     * @param lastName New user's last name. Use null to leave it unchanged
     * @param privileges New user's privileges. See Privileges class documentation for a list of available permissions. Use null to leave it unchanged
     * @param groups A list with the ids of the groups this user will belong to. Use null to leave it unchanged
     * @return The id of the newly created user
     * @throws InvalidArgumentException Thrown if the username is null or empty or the username already exists
     * @throws ApplicationObjectNotFoundException Thrown if any of the ids provided for the groups does not belong to an existing group
     */
    public void setUserProperties(String formerUsername, String userName, String password, String firstName,
            String lastName, Boolean enabled, List<Integer> privileges, List<Long> groups)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, RemoteException;

    /**
     * Creates a group
     * @param name
     * @param description
     * @param creationDate
     * @throws InvalidArgumentException
     */
    public long createGroup(String groupName, String description, List<Integer> privileges, List<Long> users)
            throws InvalidArgumentException, RemoteException;

    /**
     * Retrieves the user list
     * @return An array of UserProfile
     */
    public List<UserProfile> getUsers()throws RemoteException;

    /**
     * Retrieves the group list
     * @return An array of GroupProfile
     */
    public List<GroupProfile> getGroups()throws RemoteException;

   /**
     * Set user attributes (group membership is managed using other methods)
     * @param groupName
     * @param description
     * @param creationDate
     * @param privileges
     * @return
     * @throws InvalidArgumentException
     * @throws ApplicationObjectNotFoundException
     */
    public void setGroupProperties(long oid, String groupName, String description,
            List<Integer> privileges,  List<Long> users)throws InvalidArgumentException, ApplicationObjectNotFoundException, RemoteException;

   /**
     * Removes a list of users
     * @param oids
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public void deleteUsers(List<Long> oids)throws ApplicationObjectNotFoundException, RemoteException;

    /**
     * Removes a list of groups
     * @param oids
     * @throws InvalidArgumentException
     * @throws ApplicationObjectNotFoundException
     */
    public void deleteGroups(List<Long> oids)
            throws ApplicationObjectNotFoundException, RemoteException;

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
            throws MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;

    /**
     * Retrieves all the items related to a given list type
     * @param className list type
     * @return A list of RemoteBusinessObjectLight instances representing the items
     * @throws MetadataObjectNotFoundException if className is not an existing class
     * @throws InvalidArgumentException if the class provided is not a list type
     */
    public List<RemoteBusinessObjectLight> getListTypeItems(String className)
            throws MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;

    /**
     * Get the possible list types
     * @return A list of ClassMetadataLight instances representing the possible list types
     * @throws MetadataObjectNotFoundException
     */
    public List<ClassMetadataLight> getInstanceableListTypes()
            throws MetadataObjectNotFoundException, RemoteException;

    public void deleteListTypeItem(String className, Long oid, boolean realeaseRelationships)
            throws MetadataObjectNotFoundException, OperationNotPermittedException, ObjectNotFoundException, RemoteException;

    public View getView(Long oid, String objectClass, int viewType)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;

    public void saveView(Long oid, String objectClass, int viewType, byte[] structure, String backgroundPath)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;

    /**
     * Creates a Query
     * @param queryName
     * @param ownerOid
     * @param queryStructure
     * @param description
     * @return
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     */
    public Long createQuery(String queryName, Long ownerOid, byte[] queryStructure,
            String description) throws MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;

    /**
     * Resaves a edited query
     * @param queryOid
     * @param queryName
     * @param ownerOid
     * @param queryStructure
     * @param description
     * @throws MetadataObjectNotFoundException
     */
    public void saveQuery(Long queryOid, String queryName, Long ownerOid, byte[] queryStructure, String description) throws MetadataObjectNotFoundException, RemoteException;

    /**
     * Deletes a Query
     * @param queryOid
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     */
    public void deleteQuery(Long queryOid) throws MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;

    /**
     * Gets all queries
     * @param showPublic
     * @return
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     */
    public List<CompactQuery> getQueries(boolean showPublic) throws MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;

    /**
     * Gets a single query
     * @param queryOid
     * @return
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     */
    public CompactQuery getQuery(Long queryOid) throws MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;

    /**
     * Used to perform complex queries. Please note
     * that the first record is reserved for the column headers, so and empty result set
     * will have at least one record.
     * @param myQuery The code-friendly representation of the query made using the graphical query builder
     * @return a set of objects matching the specified criteria as ResultRecord array
     * @throws Exception
     */
    public List<ResultRecord> executeQuery(ExtendedQuery query) throws MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;

    /**
     * Get the data model class hierarchy as an XML document
     * @param showAll
     * @return
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     * @throws RemoteException
     */
    public byte[] getClassHierachy(boolean showAll) throws MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;
}

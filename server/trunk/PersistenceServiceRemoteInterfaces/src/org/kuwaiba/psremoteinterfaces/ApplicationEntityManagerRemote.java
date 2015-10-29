/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.CompactQuery;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.ResultRecord;
import org.kuwaiba.apis.persistence.application.Session;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.application.ViewObject;
import org.kuwaiba.apis.persistence.application.ViewObjectLight;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;

/**
 * RMI wrapper for the ApplicationEntityManager interface
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface ApplicationEntityManagerRemote extends Remote {
    public static final String REFERENCE_AEM = "aem";
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
            String lastName, boolean enabled, long[] privileges, long[] groups) 
            //,String ipAddress, String sessionId)
            throws InvalidArgumentException, NotAuthorizedException, RemoteException;

    /**
     * Set the properties of a given user using the id to search for it
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
    public void setUserProperties(long oid, String userName, String password, String firstName,
            String lastName, boolean enabled, long[] privileges, long[] groups, String ipAddress, String sessionId)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    public void setUserProperties(String formerUsername, String newUserName, String password, String firstName,
            String lastName, boolean enabled, long[] privileges, long[] groups, String ipAddress, String sessionId)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

    /**
     * Creates a group
     * @param name
     * @param description
     * @param creationDate
     * @throws InvalidArgumentException if there's already a group with that name
     */
    public long createGroup(String groupName, String description, long[]
            privileges, long[] users)//, String ipAddress, String sessionId)
            throws InvalidArgumentException, NotAuthorizedException, RemoteException;

    /**
     * Retrieves the user list
     * @return An array of UserProfile
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public List<UserProfile> getUsers(String ipAddress, String sessionId) throws NotAuthorizedException, RemoteException;

    /**
     * Retrieves the group list
     * @return An array of GroupProfile
     */
    public List<GroupProfile> getGroups(String ipAddress, String sessionId) throws NotAuthorizedException, RemoteException;

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
            long[] privileges, long[] users, String ipAddress,  String sessionId)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

   /**
     * Removes a list of users
     * @param oids
     * @throws InvalidArgumentException
     * @throws ApplicationObjectNotFoundException
     */
    public void deleteUsers(long[] oids, String ipAddress,  String sessionId)
            throws ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

    /**
     * Removes a list of groups
     * @param oids
     * @throws InvalidArgumentException
     * @throws ApplicationObjectNotFoundException
     */
    public void deleteGroups(long[] oids, String ipAddress, String sessionId)
            throws ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

   /**
     * Creates a list type item
     * @param className List type
     * @param name new item's name
     * @param displayName new item's display name
     * @return new item's id
     * @throws MetadataObjectNotFoundException if className is not an existing class
     * @throws InvalidArgumentException if the class provided is not a list type
     */
    public long createListTypeItem(String className, String name, String displayName, String ipAddress,  String sessionId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException, OperationNotPermittedException, RemoteException;

    /**
     * Retrieves all the items related to a given list type
     * @param className list type
     * @return A list of RemoteBusinessObjectLight instances representing the items
     * @throws MetadataObjectNotFoundException if className is not an existing class
     * @throws InvalidArgumentException if the class provided is not a list type
     */
    public List<RemoteBusinessObjectLight> getListTypeItems(String className, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException, RemoteException;

    /**
     * Retrieves all the list type items to a given list item name
     * @param listTypeName
     * @return the 
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException 
     */
    public RemoteBusinessObjectLight getListTypeItem(String listTypeName, String ipAddress, String sessionId) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException, RemoteException;
            
    /**
     * Deletes a list type item
     * @param className List type item class
     * @param oid list type item oid
     * @param realeaseRelationships Should the relationships be released
     * @throws MetadataObjectNotFoundException if the class name is not valid
     * @throws ObjectNotFoundException if the list type item can't be found
     * @throws OperationNotPermittedException if the object has relationships
     */
    public void deleteListTypeItem(String className, long oid, boolean realeaseRelationships,String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, NotAuthorizedException, RemoteException;
    /**
     * Get the possible list types
     * @return A list of ClassMetadataLight instances representing the possible list types
     * @throws ApplicationObjectNotFoundException If the GenericObjectListClass does not exist
     */
    public List<ClassMetadataLight> getInstanceableListTypes(String ipAddress, String sessionId)
            throws ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

    /**
     * Get a view related to an object, such as the default, rack or equipment views
     * @param oid object's id
     * @param objectClass object's class
     * @param viewId view id
     * @return The associated view (there should be only one of each type). Null if there's none yet
     * @throws ObjectNotFoundException if the object can not be found
     * @throws MetadataObjectNotFoundException if the corresponding class metadata can not be found
     * @throws InvalidArgumentException if the provided view type is not supported
     */
    public ViewObject getObjectRelatedView(long oid, String objectClass, long viewId, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException, RemoteException;

    /**
     * Get a view related to an object, such as the default, rack or equipment views
     * @param oid object's id
     * @param objectClass object's class
     * @param limit max number of results
     * @return The associated views
     * @throws ObjectNotFoundException if the object can not be found
     * @throws MetadataObjectNotFoundException if the corresponding class metadata can not be found
     * @throws InvalidArgumentException if the provided view type is not supported
     */
    public List<ViewObjectLight> getObjectRelatedViews(long oid, String objectClass, int limit, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException, RemoteException;

    /**
     * Retrieves the list of views not related to a given object like GIS, topological views
     * @param viewType Type of view to be retrieved. The implementor must defined what are the possible admitted values
     * @param limit maximum
     * @return a list of object with the minimum information about the view (id, class and name)
     * @throws InvalidArgumentException if the viewType is not a valid value
     */
    public List<ViewObjectLight> getGeneralViews(int viewType, int limit, String ipAddress, String sessionId)
            throws InvalidArgumentException, NotAuthorizedException, RemoteException;

    /**
     * Returns a view of those that are not related to a particular object (i.e.: GIS views)
     * @param viewId view id
     * @return An object representing the view
     * @throws ObjectNotFoundException if the requested view
     */
    public ViewObject getGeneralView(long viewId, String ipAddress, String sessionId) 
            throws ObjectNotFoundException, NotAuthorizedException, RemoteException;

    /**
     * Creates a view for a given object. If there's already a view of the provided view type, it will be overwritten
     * @param oid object's oid
     * @param objectClass object class
     * @param name view name
     * @param description view description
     * @param viewType view type (See class ViewObject for details about the supported types)
     * @param structure XML document with the view structure (see http://sourceforge.net/apps/mediawiki/kuwaiba/index.php?title=XML_Documents#To_Save_Object_Views for details about the supported format)
     * @param background background image
     * @throws ObjectNotFoundException if the object can not be found
     * @throws MetadataObjectNotFoundException if the object class can not be found
     * @throws InvalidArgumentException if the view type is not supported
     */
    public long createObjectRelatedView(long oid, String objectClass, String name, 
            String description, int viewType, byte[] structure, byte[] background, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException, RemoteException;

    /**
     * Creates a view not related to a particular object
     * @param view id
     * @param viewType
     * @param name view name
     * @param description view description
     * @param structure XML document specifying the view structure (nodes, edges, control points)
     * @param background Background image. Null for none
     * @throws InvalidArgumentException if the view type is invalid
     */
    public long createGeneralView(int viewType, String name, String description, byte[] structure, byte[] background, String ipAddress, String sessionId)
            throws InvalidArgumentException, NotAuthorizedException, RemoteException;

    /**
     * Create a view for a given object. If there's already a view of the provided view type, it will be overwritten
     * @param oid object's oid
     * @param objectClass object class
     * @param view id
     * @param name view name
     * @param description view description
     * @param structure XML document with the view structure (see http://sourceforge.net/apps/mediawiki/kuwaiba/index.php?title=XML_Documents#To_Save_Object_Views for details about the supported format)
     * @param background background image
     * @throws ObjectNotFoundException if the object can not be found
     * @throws MetadataObjectNotFoundException if the object class can not be found
     * @throws InvalidArgumentException if the view type is not supported
     */
    public void updateObjectRelatedView(long oid, String objectClass, long viewId, 
            String name, String description, byte[] structure, byte[] background, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException, RemoteException;

    /**
     * Saves a view not related to a particular object. The view type can not be changed
     * @param view id
     * @param name view name. Null to leave unchanged
     * @param description view description. Null to leave unchanged
     * @param structure XML document specifying the view structure (nodes, edges, control points). Null to leave unchanged
     * @param background background image, if applicable. Null to remove it, 0-sized array to leave unchanged
     * @throws InvalidArgumentException if the view type is invalid
     * @throws ObjectNotFoundException if the view couldn't be found
     */
    public void updateGeneralView(long oid, String name, String description, byte[] structure, byte[] background, String ipAddress, String sessionId)
            throws InvalidArgumentException, ObjectNotFoundException, NotAuthorizedException, RemoteException;


    /**
     * Deletes a list of general views
     * @param ids view ids
     * @throws ObjectNotFoundException if the view can't be found
     */
    public void deleteGeneralViews(long[] ids, String ipAddress, String sessionId)
            throws ObjectNotFoundException, NotAuthorizedException, RemoteException;

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
    public long createQuery(String queryName, long ownerOid, byte[] queryStructure,
            String description, String ipAddress, String sessionId) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException, RemoteException;

    /**
     * Resaves a edited query
     * @param queryOid
     * @param queryName
     * @param ownerOid
     * @param queryStructure
     * @param description
     * @throws MetadataObjectNotFoundException
     */
    public void saveQuery(long queryOid, String queryName, long ownerOid, byte[] queryStructure, String description, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, NotAuthorizedException, RemoteException;

    /**
     * Deletes a Query
     * @param queryOid
     * @throws ApplicationObjectNotFoundException
     * @throws InvalidArgumentException
     */
    public void deleteQuery(long queryOid, String ipAddress, String sessionId)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, NotAuthorizedException, RemoteException;

    /**
     * Gets all queries
     * @param showPublic
     * @return
     * @throws ApplicationObjectNotFoundException
     * @throws InvalidArgumentException
     */
    public List<CompactQuery> getQueries(boolean showPublic, String ipAddress, String sessionId)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, NotAuthorizedException, RemoteException;

    /**
     * Gets a single query
     * @param queryOid
     * @return
     * @throws ApplicationObjectNotFoundException
     * @throws InvalidArgumentException
     */
    public CompactQuery getQuery(long queryOid, String ipAddress, String sessionId)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, NotAuthorizedException, RemoteException;

    /**
     * * Used to perform complex queries. Please note
     * that the first record is reserved for the column headers, so and empty result set
     * will have at least one record.
     * @param myQuery The code-friendly representation of the query made using the graphical query builder
     * @return a set of objects matching the specified criteria as ResultRecord array
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     * @throws RemoteException 
     */
    public List<ResultRecord> executeQuery(ExtendedQuery query, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException, RemoteException;

     /**
     * Get the data model class hierarchy as an XML document
     * @param showAll
     * @return
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     */
    public byte[] getClassHierachy(boolean showAll, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException, RemoteException;

    //Pools
    /**
     * Creates a pool
     * @param name Pool name
     * @param description Pool description
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @return The id of the new pool
     * @throws MetadataObjectNotFoundException If instancesOfClass is not a valid subclass of InventoryObject
     * @throws InvalidArgumentException If the owner doesn't exist
     */
    public long createPool(long parentId, String name, String description, String instancesOfClass, String ipAddress, String sessionId) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, ObjectNotFoundException, NotAuthorizedException, RemoteException;
    
    /**
     * Creates an object inside a pool
     * @param poolId Parent pool id
     * @param attributeNames Attributes to be set
     * @param attributeValues Attribute values to be set
     * @param templateId Template used to create the object, if applicable. -1 for none
     * @throws ApplicationObjectNotFoundException If the parent pool can't be found
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @return the id of the newly created object
     */
    public long createPoolItem(long poolId, String className, String[] attributeNames, 
            String[][] attributeValues, long templateId, String ipAddress, String sessionId) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, ArraySizeMismatchException, NotAuthorizedException, RemoteException;

    /**
     * Deletes a set of pools
     * @param ids the list of ids from the objects to be deleted
     * @throws InvalidArgumentException If any of the pools to be deleted couldn't be found
     */
    public void deletePools(long[] ids, String ipAddress, String sessionId) throws InvalidArgumentException, NotAuthorizedException, RemoteException;
    
    
     /**
     * Gets the available pools for a specific parent id
     * @param limit
     * @param parentId
     * @param className
     * @param ipAddress
     * @param sessionId
     * @return
     * @throws NotAuthorizedException 
     */
    public List<RemoteBusinessObjectLight> getPools(int limit, long parentId, String className, String ipAddress, String sessionId) throws NotAuthorizedException, ObjectNotFoundException, RemoteException;
    
    /**
     * Gets the available pools
     * @param limit Maximum number of pool records to be returned. -1 to return all
     * @return The list of pools as RemoteBusinessObjectLight instances
     */
    public List<RemoteBusinessObjectLight> getPools(int limit, String className, String ipAddress, String sessionId) throws NotAuthorizedException, RemoteException;
    /**
     * Gets the objects into a pool
     * @param poolId Parent pool id
     * @param limit max number of results. -1 to get all
     * @return The list of objects
     * @throws MetadataObjectNotFoundException If the id provided does not belong to an existing pool
     * @throws RemoteException 
     */
    public List<RemoteBusinessObjectLight> getPoolItems(long poolId, int limit, String ipAddress, String sessionId) 
            throws ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    
    /**
     * Gets a business object audit trail
     * @param objectClass
     * @param objectId
     * @param limit
     * @return The list of activity entries
     * @throws ObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException If the provided class couldn't be found
     * @throws InvalidArgumentException If the class provided is not subclass of  InventoryObject
     */
    public List<ActivityLogEntry> getBusinessObjectAuditTrail(String objectClass, long objectId, int limit, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException, RemoteException;
    
    /**
     * Retrieves the list of activity log entries
     * @param page current page
     * @param limit limit of results per page. 0 to retrieve them all
     * @return The list of activity log entries
     */
    public List<ActivityLogEntry> getGeneralActivityAuditTrail(int page, int limit, String ipAddress, String sessionId) throws NotAuthorizedException, RemoteException;
    
    /**
     * Validate if an user is allowed to perform an operation
     * @param methodName the method name
     * @param user the user profile
     * @throws ApplicationObjectNotFoundException
     * @throws RemoteException 
     */
    public void validateCall(String methodName, String ipAddress, String sessionId) throws ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    
    /**
     * 
     * @param user
     * @param password
     * @param IPAddress
     * @return a session
     * @throws ApplicationObjectNotFoundException
     * @throws RemoteException 
     */
    public Session createSession(String user, String password, String IPAddress) throws ApplicationObjectNotFoundException, RemoteException;
    
    /**
     * 
     * @param IPAddress
     * @param sessionId
     * @return
     * @throws ApplicationObjectNotFoundException
     * @throws NotAuthorizedException
     * @throws RemoteException 
     */
    public UserProfile getUserInSession(String IPAddress, String sessionId) throws ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    
    /**
     * close a session
     * @param sessionId
     * @param remoteAddress
     * @throws NotAuthorizedException
     * @throws RemoteException 
     */
    public void closeSession(String sessionId, String remoteAddress) throws NotAuthorizedException ,RemoteException;
}

/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
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

package org.kuwaiba.apis.persistence.application;

import com.neotropic.kuwaiba.modules.GenericCommercialModule;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectList;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.util.ChangeDescriptor;
import org.kuwaiba.ws.todeserialize.StringPair;
import org.kuwaiba.ws.toserialize.application.TaskNotificationDescriptor;
import org.kuwaiba.ws.toserialize.application.TaskScheduleDescriptor;
import org.kuwaiba.ws.toserialize.application.UserInfoLight;

/**
 * This is the entity in charge of manipulating application objects such as users, views, etc
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface ApplicationEntityManager {
    /**
     * Type of pool general purpose. These pools are not linked to any particular model
     */
    public static final int POOL_TYPE_GENERAL_PURPOSE = 1;
    /**
     * Type of pool module root. These pools are used in models and are the root of such model
     */
    public static final int POOL_TYPE_MODULE_ROOT = 2;
    /**
     * Type of pool module component. These pools are used in models and are in the lower levels of the pool containment hierarchy
     */
    public static int POOL_TYPE_MODULE_COMPONENT = 3;
    /**
     * String that identifies the class used for pools
     */
    public static final String CLASS_POOL = "Pool";
    
   
    /**
     * Gets current sessions
     * @return A dictionary whose keys are the user names and the keys are the session related objects
     */
    public HashMap<String, Session> getSessions();
    
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
            String lastName, boolean enabled, long[] privileges, long[] groups)//)
            throws InvalidArgumentException, NotAuthorizedException;
    
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
            String lastName, boolean enabled, long[] privileges, long[] groups)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException;
    /**
     * Updates the attributes of a user, using its username as key to find it
     * @param formerUsername Current user name
     * @param newUserName New name. Null if unchanged
     * @param password Password. Null if unchanged
     * @param firstName User's first name. Null if unchanged
     * @param lastName User's last name. Null if unchanged
     * @param enabled Is this user enabled?
     * @param privileges Privileges
     * @param groups List of ids with the groups. It overwrites the existing ones
     * @throws InvalidArgumentException If the format of any of the parameters provided is erroneous
     * @throws ApplicationObjectNotFoundException If the user can not be found
     * @throws NotAuthorizedException If the session is not valid or the caller doesn't have enough rights to perform this operation
     */
    public void setUserProperties(String formerUsername, String newUserName, String password, String firstName,
            String lastName, boolean enabled, long[] privileges, long[] groups)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException;
    /**
     * Creates a group
     * @param groupName the group name
     * @param description group description
     * @param privileges group privileges
     * @param users users who belong the group
     * @return group id
     * @throws InvalidArgumentException if there's already a group with that name
     * @throws NotAuthorizedException 
     */
     public long createGroup(String groupName, String description, long[]
            privileges, long[] users)
            throws InvalidArgumentException, NotAuthorizedException;

    /**
     * Retrieves the list of all users
     * @return An array of UserProfile
     * @throws NotAuthorizedException
     */
    public List<UserProfile> getUsers() throws NotAuthorizedException;

    /**
     * Retrieves the list of all groups
     * @return An array of GroupProfile
     * @throws NotAuthorizedException
     */
    public List<GroupProfile> getGroups() throws NotAuthorizedException;

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
            long[] privileges, long[] users)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException;

   /**
     * Removes a list of users
     * @param oids
     * @throws InvalidArgumentException
     * @throws ApplicationObjectNotFoundException
     */
    public void deleteUsers(long[] oids)
            throws ApplicationObjectNotFoundException, NotAuthorizedException;

    /**
     * Removes a list of groups
     * @param oids
     * @throws InvalidArgumentException
     * @throws ApplicationObjectNotFoundException
     */
    public void deleteGroups(long[] oids) 
            throws ApplicationObjectNotFoundException, NotAuthorizedException;
    
   /**
     * Creates a list type item
     * @param className List type
     * @param name new item's name
     * @param displayName new item's display name
     * @return new item's id
     * @throws MetadataObjectNotFoundException if className is not an existing class
     * @throws InvalidArgumentException if the class provided is not a list type
     */
    public long createListTypeItem(String className, String name, String displayName)
            throws MetadataObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, NotAuthorizedException;

    /**
     * Retrieves all the items related to a given list type
     * @param className list type
     * @param ipAddress
     * @param sessionId
     * @return A list of RemoteBusinessObjectLight instances representing the items
     * @throws MetadataObjectNotFoundException if className is not an existing class
     * @throws InvalidArgumentException if the class provided is not a list type
     */
    public List<RemoteBusinessObjectLight> getListTypeItems(String className)
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException;

    /**
     * Retrieves all the list type items to a given list item name
     * @param listTypeName
     * @return the 
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException 
     */
    public RemoteBusinessObjectLight getListTypeItem(String listTypeName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException;
    
    /**
     * Deletes a list type item
     * @param className List type item class
     * @param oid list type item oid
     * @param realeaseRelationships Should the relationships be released
     * @throws MetadataObjectNotFoundException if the class name is not valid
     * @throws ObjectNotFoundException if the list type item can't be found
     * @throws OperationNotPermittedException if the object has relationships
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException If the class provided is not a list type
     * @throws org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException If the user can't delete a list type item
     */
    public void deleteListTypeItem(String className, long oid, boolean realeaseRelationships)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException, NotAuthorizedException;

    /**
     * Get the possible list types
     * @param ipAddress
     * @param sessionId
     * @return A list of ClassMetadataLight instances representing the possible list types
     * @throws ApplicationObjectNotFoundException if the GenericObjectList class does not exist
     */
    public List<ClassMetadataLight> getInstanceableListTypes()
            throws ApplicationObjectNotFoundException, NotAuthorizedException;

    /**
     * Get a view related to an object, such as the default, rack or equipment views
     * @param oid object's id
     * @param objectClass object's class
     * @param viewId view id
     * @return The associated view (there should be only one of each type). Null if there's none yet
     * @throws ObjectNotFoundException if the object or the view can not be found
     * @throws MetadataObjectNotFoundException if the corresponding class metadata can not be found
     * @throws InvalidArgumentException if the provided view type is not supported
     */
    public ViewObject getObjectRelatedView(long oid, String objectClass, long viewId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException;

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
    public List<ViewObjectLight> getObjectRelatedViews(long oid, String objectClass, int limit)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException;
  
    /**
     * Allows to retrieve a list of views of a certain type, specifying their class
     * @param viewClassName The class name
     * @param limit The limit of results. -1 for all
     * @return The list of views
     * @throws InvalidArgumentException If the view class does not exist
     * @throws NotAuthorizedException If the user is not allowed to query for general views
     */
    public List<ViewObjectLight> getGeneralViews(String viewClassName, int limit) throws InvalidArgumentException, NotAuthorizedException;;

    /**
     * Returns a view of those that are not related to a particular object (i.e.: GIS views)
     * @param viewId view id
     * @return An object representing the view
     * @throws ObjectNotFoundException if the requested view
     */
    public ViewObject getGeneralView(long viewId) throws ObjectNotFoundException, NotAuthorizedException;

    /**
     * Creates a view for a given object. If there's already a view of the provided view type, it will be overwritten
     * @param oid object's oid
     * @param objectClass object class
     * @param name view name
     * @param description view description
     * @param viewClassName view class name (See class ViewObject for details about the supported types)
     * @param structure XML document with the view structure (see http://sourceforge.net/apps/mediawiki/kuwaiba/index.php?title=XML_Documents#To_Save_Object_Views for details about the supported format)
     * @param background background image
     * @throws ObjectNotFoundException if the object can not be found
     * @throws MetadataObjectNotFoundException if the object class can not be found
     * @throws InvalidArgumentException if the view type is not supported
     */
    public long createObjectRelatedView(long oid, String objectClass, String name, String description, 
            String viewClassName, byte[] structure, byte[] background)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException;

    /**
     * Creates a view not related to a particular object
     * @param viewClass View class
     * @param name view name
     * @param description view description
     * @param structure XML document specifying the view structure (nodes, edges, control points)
     * @param background Background image
     * @return The id of the newly created view
     * @throws InvalidArgumentException if the view type is invalid
     * @throws org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException If the user is not allowed to create general views
     */
    public long createGeneralView(String viewClass, String name, String description, byte[] structure, byte[] background)
            throws InvalidArgumentException, NotAuthorizedException;

    /**
     * Create a view for a given object. If there's already a view of the provided view type, it will be overwritten
     * @param oid object's oid
     * @param objectClass object class
     * @param view id
     * @param name view name
     * @param description view description
     * @param structure XML document with the view structure (see http://neotropic.co/kuwaiba/wiki/index.php?title=XML_Documents#To_Save_Object_Views for details about the supported format)
     * @param background Background image. If null, the previous will be removed, if 0-sized array, it will remain unchanged
     * @return The summary of the changes
     * @throws ObjectNotFoundException if the object can not be found
     * @throws MetadataObjectNotFoundException if the object class can not be found
     * @throws InvalidArgumentException if the view type is not supported
     */
    public ChangeDescriptor updateObjectRelatedView(long oid, String objectClass, long viewId, String name, 
            String description, byte[] structure, byte[] background)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException;

    /**
     * Saves a view not related to a particular object. The view type can not be changed
     * @param oid View id
     * @param name view name. Null to leave unchanged
     * @param description view description. Null to leave unchanged
     * @param structure XML document specifying the view structure (nodes, edges, control points). Null to leave unchanged
     * @param background Background image. If null, the previous will be removed, if 0-sized array, it will remain unchanged
     * @return The summary of the changes.
     * @throws InvalidArgumentException if the view type is invalid
     * @throws ObjectNotFoundException if the view couldn't be found
     */
    public ChangeDescriptor updateGeneralView(long oid, String name, String description, byte[] structure, byte[] background)
            throws InvalidArgumentException, ObjectNotFoundException, NotAuthorizedException;


    /**
     * Deletes a list of general views
     * @param ids view ids
     * @throws ObjectNotFoundException if the view can't be found
     */
    public void deleteGeneralViews(long[] ids) throws ObjectNotFoundException, NotAuthorizedException;

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
            String description) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException;

    /**
     * Resaves a edited query
     * @param queryOid
     * @param queryName
     * @param ownerOid
     * @param queryStructure
     * @param description
     * @param ipAddress
     * @throws ApplicationObjectNotFoundException If the query can not be found
     */
    public void saveQuery(long queryOid, String queryName, long ownerOid, byte[] queryStructure, String description)
            throws ApplicationObjectNotFoundException, NotAuthorizedException;

    /**
     * Deletes a Query
     * @param queryOid
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     */
    public void deleteQuery(long queryOid) throws ApplicationObjectNotFoundException, InvalidArgumentException, NotAuthorizedException;

    /**
     * Gets all queries
     * @param showPublic
     * @return
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     */
    public List<CompactQuery> getQueries(boolean showPublic) throws ApplicationObjectNotFoundException, InvalidArgumentException, NotAuthorizedException;

    /**
     * Gets a single query
     * @param queryOid
     * @return
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     */
    public CompactQuery getQuery(long queryOid) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, NotAuthorizedException;

    /**
     * Used to perform complex queries. Please note
     * that the first record is reserved for the column headers, so and empty result set
     * will have at least one record.
     * @param query The code-friendly representation of the query made using the graphical query builder
     * @return a set of objects matching the specified criteria as ResultRecord array
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException 
     */
    public List<ResultRecord> executeQuery(ExtendedQuery query)
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException;

    /**
     * Get the data model class hierarchy as an XML document
     * @param showAll
     * @return
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     */
    public byte[] getClassHierachy(boolean showAll) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException;
    
    //Pools
    /**
     * Creates a pool without a parent. They're used as general purpose place to put inventory objects, or as root for particular models
     * @param name Pool name
     * @param description Pool description
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @param type Type of pool. For possible values see ApplicationManager.POOL_TYPE_XXX
     * @return The id of the new pool
     * @throws MetadataObjectNotFoundException If instancesOfClass is not a valid subclass of InventoryObject
     * @throws NotAuthorizedException If the user is not authorized to create pools
     */
    public long createRootPool(String name, String description, String instancesOfClass, int type)
            throws MetadataObjectNotFoundException, NotAuthorizedException;
    
    /**
     * Creates a pool that will have as parent an inventory object. This special containment structure can be used to 
     * provide support for new models
     * @param parentClassname Class name of the parent object
     * @param parentId Id of the parent object
     * @param name Pool name
     * @param description Pool description
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @param type Type of pool. For possible values see ApplicationManager.POOL_TYPE_XXX
     * @return The id of the new pool
     * @throws MetadataObjectNotFoundException If instancesOfClass is not a valid subclass of InventoryObject
     * @throws ObjectNotFoundException If the parent object can not be found
     * @throws NotAuthorizedException If the user is not authorized to create pools
     */
    public long createPoolInObject(String parentClassname, long parentId, String name, String description, String instancesOfClass, int type)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, NotAuthorizedException;

    /**
     * Creates a pool that will have as parent another pool. This special containment structure can be used to 
     * provide support for new models
     * @param parentId Id of the parent pool
     * @param name Pool name
     * @param description Pool description
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @param type Type of pool. Not used so far, but it will be in the future. It will probably be used to help organize the existing pools
     * @return The id of the new pool
     * @throws MetadataObjectNotFoundException If instancesOfClass is not a valid subclass of InventoryObject
     * @throws ApplicationObjectNotFoundException If the parent object can not be found
     * @throws NotAuthorizedException If the user is not authorized to create pools
     */
    public long createPoolInPool(long parentId, String name, String description, String instancesOfClass, int type)
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException;
    
    /**
     * Deletes a single pool
     * @param id The pool id
     * @throws ApplicationObjectNotFoundException If the pool could not be found
     * @throws OperationNotPermittedException If any of the pool children had relationships
     * @throws NotAuthorizedException If the user is not allowed to delete pools
     */
    public void deletePool(long id) throws ApplicationObjectNotFoundException, OperationNotPermittedException, NotAuthorizedException;
    
    /**
     * Deletes a set of pools. Note that this method will delete and commit the changes until it finds an error, so if deleting any of the pools fails, don't try to delete those that were already processed
     * @param ids the list of ids from the objects to be deleted
     * @throws ApplicationObjectNotFoundException If any of the pools to be deleted couldn't be found
     * @throws OperationNotPermittedException If any of the objects in the pool can not be deleted because it's not a business related instance (it's more a security restriction)
     * @throws NotAuthorizedException If the user is not authorized to delete pools
     */
    public void deletePools(long[] ids) throws ApplicationObjectNotFoundException, OperationNotPermittedException, NotAuthorizedException;
   
    /**
     * Updates a pool. The class name field is read only to preserve the integrity of the pool. Same happens to the field type
     * @param poolId Pool Id
     * @param name Pool name. If null, this field will remain unchanged
     * @param description Pool description. If null, this field will remain unchanged
     */
    public void setPoolProperties(long poolId, String name, String description);
    
    /**
     * Retrieves the pools that don't have any parent and are normally intended to be managed by the Pool Manager
     * @param className The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use null if you want to get all
     * @param type The type of pools that should be retrieved. Root pools can be for general purpose, or as roots in models
     * @param includeSubclasses Use <code>true</code> if you want to get only the pools whose <code>className</code> property matches exactly the one provided, and <code>false</code> if you want to also include the subclasses
     * @return A set of pools
     * @throws NotAuthorizedException If the user is not authorized to retrieve pool information
     */
    public List<Pool> getRootPools(String className, int type, boolean includeSubclasses) throws NotAuthorizedException;
    /**
     * Retrieves the pools associated to a particular object
     * @param objectClassName The parent object class name
     * @param objectId The parent object id
     * @param poolClass The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use null if you want to get all
     * @return A set of pools
     * @throws NotAuthorizedException If the user is not authorized to retrieve pool information
     * @throws ObjectNotFoundException If the parent object can not be found
     */
    public List<Pool> getPoolsInObject(String objectClassName, long objectId, String poolClass) 
            throws NotAuthorizedException, ObjectNotFoundException;
    /**
     * Retrieves the pools associated to a particular pool
     * @param parentPoolId The parent pool id
     * @param poolClass The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use null if you want to get all
     * @return A set of pools
     * @throws NotAuthorizedException If the user is not authorized to retrieve pool information
     * @throws ApplicationObjectNotFoundException If the parent object can not be found
     */
    public List<Pool> getPoolsInPool(long parentPoolId, String poolClass) 
            throws NotAuthorizedException, ApplicationObjectNotFoundException;
    
    /**
     * Gets a pool by its id 
     * @param poolId The pool's id
     * @return the pool as a Pool object
     * @throws ApplicationObjectNotFoundException If the pool could not be found
     * @throws NotAuthorizedException If the user is not authorized to retrieve a pool's info
     */
    public Pool getPool(long poolId) throws ApplicationObjectNotFoundException, NotAuthorizedException;
    
    /**
     * Gets the list of objects into a pool
     * @param poolId Parent pool id
     * @param limit Result limit. -1 To return all
     * @return The list of items inside the pool
     * @throws ApplicationObjectNotFoundException If the pool id provided is not valid
     */
    public List<RemoteBusinessObjectLight> getPoolItems(long poolId, int limit)
            throws ApplicationObjectNotFoundException, NotAuthorizedException;
    
    /**
     * Gets a business object audit trail
     * @param objectClass Object class
     * @param objectId Object id
     * @param limit Max number of results to be shown
     * @return The list of activity entries
     * @throws ObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException If the provided class couldn't be found
     * @throws InvalidArgumentException If the class provided is not subclass of  InventoryObject
     */
    public List<ActivityLogEntry> getBusinessObjectAuditTrail(String objectClass, long objectId, int limit)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException;
    
    /**
     * Retrieves the list of activity log entries
     * @param page current page
     * @param limit limit of results per page. 0 to retrieve them all
     * @return The list of activity log entries
     */
    public List<ActivityLogEntry> getGeneralActivityAuditTrail(int page, int limit) throws NotAuthorizedException;
    
    /**
     * Validates if an user is allowed to perform an operation
     * @param methodName the method name
     * @param ipAddress
     * @param sessionId
     * @throws ApplicationObjectNotFoundException 
     */
    public void validateCall(String methodName, String ipAddress, String sessionId) throws NotAuthorizedException;
    
    /**
     * 
     * @param user
     * @param password
     * @param IPAddress
     * @return
     * @throws ApplicationObjectNotFoundException 
     */
    public Session createSession(String user, String password, String IPAddress)throws ApplicationObjectNotFoundException;
    
    /**
     * 
     * @param IPAddress
     * @param sessionId
     * @return
     * @throws ApplicationObjectNotFoundException
     * @throws NotAuthorizedException 
     */
    public UserProfile getUserInSession(String IPAddress, String sessionId) throws ApplicationObjectNotFoundException, NotAuthorizedException;
    
    /**
     * Executes a patch file 
     * @return 
     * @throws MetadataObjectNotFoundException
     * @throws NotAuthorizedException 
     */
    public int[] executePatch() throws NotAuthorizedException;
    
    /**
     * Closes a session
     * @param sessionId
     * @param remoteAddress
     * @throws NotAuthorizedException 
     */
    public void closeSession(String sessionId, String remoteAddress) throws NotAuthorizedException;
 
    /**
     * Set the configuration variables of this manager
     * @param properties A Properties object with the configuration variables
     */
    public void setConfiguration(Properties properties);
    
    /**
     * Set the configuration variables of this manager
     * @return A Properties object with the configuration variables
     */
    public Properties getConfiguration();
    
    /**
     * Creates a general activity log entry, that is, an entry that is not associated to a particular object
     * @param userName User that performed the action
     * @param type Type of action. See class ActivityLogEntry for possible values
     * @param notes Optional additional notes related to the action. The Id of the element, if it was created/deleted. Null if no notes should be added 
     * @throws ApplicationObjectNotFoundException If the log root node could not be found
     */
    public void createGeneralActivityLogEntry(String userName, int type,
            String notes) throws ApplicationObjectNotFoundException;
    /**
     * Creates a general activity log entry, that is, an entry that is not associated to a particular object
     * @param userName User that performed the action
     * @param type Type of action. See class ActivityLogEntry for possible values
     * @param changeDescriptor The descriptor with all the changes performed by the method
     * @throws ApplicationObjectNotFoundException If the log root node could not be found
     */
    public void createGeneralActivityLogEntry(String userName, int type, ChangeDescriptor changeDescriptor) throws ApplicationObjectNotFoundException;
    
    /**
     * Creates an object activity log entry, that is, an entry that is directly related to an object, such as the modification of the value of an attribute
     * @param userName User that performs the operation
     * @param className The class of the object being modified
     * @param oid The oid of the object being modified
     * @param type The type of action. See ActivityLogEntry class for possible values
     * @param affectedProperties Properties that were affected. Normally, they're separated by spaces, but it's not required
     * @param oldValues Old values. Normally, they're separated by spaces, but it's not required
     * @param newValues New values. Normally, they're separated by spaces, but it's not required
     * @param notes Additional notes associated with the change
     * @throws ApplicationObjectNotFoundException If the object activity log could no be found
     * @throws ObjectNotFoundException  If the modified object itself could not be found
     */
    public void createObjectActivityLogEntry(String userName, String className, long oid, int type, 
        String affectedProperties, String oldValues, String newValues, String notes) throws ApplicationObjectNotFoundException, ObjectNotFoundException;
    
    /**
     * Creates an object activity log entry, that is, an entry that is directly related to an object, such as the modification of the value of an attribute
     * @param userName User that performs the operation
     * @param className The class of the object being modified
     * @param oid The oid of the object being modifies
     * @param type The type of action. See ActivityLogEntry class for possible values
     * @param changeDescriptor The summary of the changes that were done
     * @throws ApplicationObjectNotFoundException If the object activity log could no be found
     * @throws ObjectNotFoundException If the modified object itself could not be found
     */
    public void createObjectActivityLogEntry(String userName, String className, long oid,  
            int type, ChangeDescriptor changeDescriptor) throws ApplicationObjectNotFoundException, ObjectNotFoundException;
    
    /**
     * Allows to execute custom database queries. This method should not be used as it's only a temporary solution
     * @param dbCode A string with the query
     * @return A table with results, that could also be interpreted as a multidimensional array with numerous paths
     * @throws NotAuthorizedException If the user is not allowed to run arbitrary code on the database
     * @deprecated Don't use it, instead, create a method in the corresponding entity manager instead of running code directly on the database
     */
    public HashMap<String, RemoteBusinessObjectList> executeCustomDbCode(String dbCode) throws NotAuthorizedException;
    
    /**
     * Registers a commercial module. Replaces an existing one if the name of provided one is already registered
     * @param module The module to be registered
     * @throws NotAuthorizedException If the user is not authorized to register commercial modules
     */
    public void registerCommercialModule(GenericCommercialModule module) throws NotAuthorizedException;
    /**
     * Gets a particular commercial module based on its name
     * @param moduleName The module name
     * @return The module. Null if the name could not be found
     * @throws NotAuthorizedException If the user is not authorized to access a particular commercial module
     */
    public GenericCommercialModule getCommercialModule(String moduleName) throws NotAuthorizedException;
    /**
     * Gets a commercial module based on its name
     * @return The module instance
     * @throws NotAuthorizedException If the user is not authorized to access a particular commercial module
     */
    public Collection<GenericCommercialModule> getCommercialModules() throws NotAuthorizedException;    
    /**
     * Creates and schedule a task. A task is an application entity that allows to run jobs that will be executed depending on certain schedule
     * @param name Task name
     * @param description Task description
     * @param enabled Is the task enabled?
     * @param script The script to be executed
     * @param parameters The parameters for the script
     * @param schedule When the task should be executed
     * @param notificationType How the result of the task should be notified to the associated users 
     * @return The id of the newly created task
     */
    public long createTask(String name, String description, boolean enabled, String script, List<StringPair> parameters, TaskScheduleDescriptor schedule, TaskNotificationDescriptor notificationType);
    /**
     * Updates any of these properties from a task: name, description, enabled and script
     * @param taskId Task id
     * @param propertyName Property name. Possible values: "name", "description", "enabled" and "script"
     * @param propertyValue The value of the property. For the property "enabled", the allowed values are "true" and "false"
     * @throws ApplicationObjectNotFoundException If the task could not be found
     * @throws InvalidArgumentException If the property name has an invalid value
     */
    public void updateTaskProperties(long taskId, String propertyName, String propertyValue) throws ApplicationObjectNotFoundException, InvalidArgumentException;
    /**
     * Updates the parameters of a task. If any of the values is null, that parameter will be deleted, if the parameter does not exist, it will be created
     * @param taskId Task id
     * @param parameters The parameters to be modified as pairs paramName/paramValue
     * @throws ApplicationObjectNotFoundException If the task could not be found
     */
    public void updateTaskParameters(long taskId, List<StringPair> parameters) throws ApplicationObjectNotFoundException;
    /**
     * Updates a task schedule
     * @param taskId Task id
     * @param schedule New schedule
     * @throws ApplicationObjectNotFoundException If the task could not be found
     */
    public void updateTaskSchedule(long taskId, TaskScheduleDescriptor schedule) throws ApplicationObjectNotFoundException;
    /**
     * Updates a task notification type
     * @param taskId Task id
     * @param notificationType New notification type
     */
    public void updateTaskNotificationType(long taskId, TaskNotificationDescriptor notificationType) throws ApplicationObjectNotFoundException;
    /**
     * Deletes a task and unsubscribes all users from it
     * @param taskId Task id
     * @throws ApplicationObjectNotFoundException If the task could not be found
     */
    public void deleteTask(long taskId) throws ApplicationObjectNotFoundException;
    /**
     * Subscribes a user to a task, so it will be notified of the result of its execution
     * @param taskId Id of the task
     * @param userId Id of the user
     * @throws ApplicationObjectNotFoundException If the task or the user could not be found
     * @throws InvalidArgumentException If the user is already subscribed to the task
     */
    public void subscribeUserToTask(long userId, long taskId) throws ApplicationObjectNotFoundException, InvalidArgumentException;
    /**
     * Unsubscribes a user from a task, so it will no longer be notified about the result of its execution
     * @param taskId Id of the task
     * @param userId Id of the user
     * @throws ApplicationObjectNotFoundException If the task or the user could not be found
     */
    public void unsubscribeUserFromTask(long userId, long taskId) throws ApplicationObjectNotFoundException;
    /**
     * Retrieves the information about a particular task
     * @param taskId Id of the task
     * @return A remote task object representing the task
     * @throws ApplicationObjectNotFoundException If the task could not be found
     */
    public Task getTask(long taskId) throws ApplicationObjectNotFoundException;
    /**
     * Gets the subscribers of a particular task
     * @param taskId task id
     * @return The list of users subscribers to the task identified with the id taskId
     * @throws ApplicationObjectNotFoundException Id the task could not be found
     */
    public List<UserInfoLight> getSubscribersForTask(long taskId) throws ApplicationObjectNotFoundException;
    /**
     * Gets all registered tasks
     * @return A list with the task objects
     */
    public List<Task> getTasks();
    /**
     * Gets the tasks associated to a particular user
     * @param userId Id if the user
     * @return A list with the task objects
     * @throws ApplicationObjectNotFoundException If the user can not be found
     */
    public List<Task> getTasksForUser(long userId) throws ApplicationObjectNotFoundException;
    /**
     * Executes a task on demand
     * @param taskId Id of the task
     * @return An object representing the task result
     * @throws ApplicationObjectNotFoundException If the task could not be found
     * @throws InvalidArgumentException  If the task doesn't have a script
     */
    public TaskResult executeTask(long taskId) throws ApplicationObjectNotFoundException, InvalidArgumentException;
    
}

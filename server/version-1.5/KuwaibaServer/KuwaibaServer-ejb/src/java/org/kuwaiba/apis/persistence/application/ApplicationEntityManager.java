/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectList;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.BusinessRuleException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.UnsupportedPropertyException;
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
     * Type of pool general purpose. 
     * These pools are not linked to any particular model
     */
    public static final int POOL_TYPE_GENERAL_PURPOSE = 1;
    /**
     * Type of pool module root. 
     * These pools are used in models and are the root of such model
     */
    public static final int POOL_TYPE_MODULE_ROOT = 2;
    /**
     * Type of pool module component. 
     * These pools are used in models and are in the lower levels of the pool 
     * containment hierarchy.
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
     * @param type User type. See UserProfileLight.USER_TYPE_* for possible values
     * @param privileges New user's privileges. Use null for none
     * @param defaultGroupId Default group this user will be associated to
     * @return The id of the newly created user
     * @throws InvalidArgumentException Thrown if the username is null or empty or the username already exists
     */
    public long createUser(String userName, String password, String firstName,
            String lastName, boolean enabled, int type, List<Privilege> privileges, long defaultGroupId)
            throws InvalidArgumentException;
    
    /**
     * Set the properties of a given user using the id to search for it
     * @param oid User id
     * @param userName New user's name. User null to leave it unchanged.
     * @param password New user's password. Use null to leave it unchanged
     * @param firstName New user's first name. Use null to leave it unchanged
     * @param lastName New user's last name. Use null to leave it unchanged
     * @param enabled 0 for false, 1 for true, -1 to leave it unchanged
     * @param type User type. See UserProfile.USER_TYPE* for possible values. Use -1 to leave it unchanged
     * @throws InvalidArgumentException Thrown if the username is null or empty or the username already exists
     * @throws ApplicationObjectNotFoundException If the user could not be found
     */
    public void setUserProperties(long oid, String userName, String password, String firstName,
            String lastName, int enabled, int type)
            throws InvalidArgumentException, ApplicationObjectNotFoundException;
    /**
     * Updates the attributes of a user, using its username as key to find it
     * @param formerUsername Current user name
     * @param newUserName New name. Null if unchanged
     * @param password Password. Null if unchanged
     * @param firstName User's first name. Null if unchanged
     * @param lastName User's last name. Null if unchanged
     * @param enabled 0 for false, 1 for true, -1 to leave it unchanged
     * @param type User type. See UserProfile.USER_TYPE* for possible values. -1 to leave it unchanged
     * @throws InvalidArgumentException If the format of any of the parameters provided is erroneous
     * @throws ApplicationObjectNotFoundException If the user could not be found
     */
    public void setUserProperties(String formerUsername, String newUserName, String password, String firstName,
            String lastName, int enabled, int type)
            throws InvalidArgumentException, ApplicationObjectNotFoundException;
    
    /**
     * Adds a user to a group
     * @param userId The id of the user to be added to the group
     * @param groupId Id of the group which the user will be added to
     * @throws InvalidArgumentException If the user is already related to that group.
     * @throws ApplicationObjectNotFoundException If the user or group can not be found
     */
    public void addUserToGroup(long userId, long groupId) throws InvalidArgumentException, ApplicationObjectNotFoundException;
    /**
     * Removes a user from a group
     * @param userId The id of the user to be added to the group
     * @param groupId Id of the group which the user will be added to
     * @throws InvalidArgumentException If the user is not related to that group
     * @throws ApplicationObjectNotFoundException If the user or the group could not be found
     */
    public void removeUserFromGroup(long userId, long groupId) throws InvalidArgumentException, ApplicationObjectNotFoundException;
    /**
     * Sets a privilege to a user. If the feature token provided already has been assigned to the user, the access level will be changed, otherwise, a privilege will be created
     * @param userId The user Id
     * @param featureToken The feature token. See class Privilege for details. Note that this token must match to the one expected by the client application. That's the only way the correct features will be enabled.
     * @param accessLevel The feature token. See class Privilege.ACCESS_LEVEL* for details. 
     * @throws InvalidArgumentException If the access level is invalid or if the featureToken has a wrong format
     * @throws ApplicationObjectNotFoundException If the user could not be found.
     */
    public void setPrivilegeToUser(long userId, String featureToken, int accessLevel) throws InvalidArgumentException, ApplicationObjectNotFoundException;
    /**
     * Sets a privilege to a group. If the feature token provided already has been assigned to the group, the access level will be changed, otherwise, a privilege will be created
     * @param groupId The group Id
     * @param featureToken The feature token. See class Privilege for details. Note that this token must match to the one expected by the client application. That's the only way the correct features will be enabled.
     * @param accessLevel The feature token. See class Privilege.ACCESS_LEVEL* for details. 
     * @throws InvalidArgumentException If the access level is invalid or if the featureToken has a wrong format
     * @throws ApplicationObjectNotFoundException If the group could not be found
     */
    public void setPrivilegeToGroup(long groupId, String featureToken, int accessLevel) throws InvalidArgumentException, ApplicationObjectNotFoundException;
    
    /**
     * Removes a privilege from a user
     * @param userId Id of the user
     * @param featureToken The feature token. See class Privilege for details. 
     * @throws InvalidArgumentException If the feature token is not related to the user
     * @throws ApplicationObjectNotFoundException If the user could not be found
     */
    public void removePrivilegeFromUser(long userId, String featureToken) throws InvalidArgumentException, ApplicationObjectNotFoundException;
    /**
     * Removes a privilege from a user
     * @param groupId Id of the group
     * @param featureToken The feature token. See class Privilege for details. 
     * @throws InvalidArgumentException If the feature token is not related to the group
     * @throws ApplicationObjectNotFoundException If the group could not be found
     */
    public void removePrivilegeFromGroup(long groupId, String featureToken) throws InvalidArgumentException, ApplicationObjectNotFoundException;
    /**
     * Creates a group
     * @param groupName The group name
     * @param description The group description
     * @param users users who belong the group
     * @return The new group id.
     * @throws InvalidArgumentException if there's already a group with that name
     * @throws ApplicationObjectNotFoundException If any of the provided users could not be found
     */
     public long createGroup(String groupName, String description, List<Long> users)
            throws InvalidArgumentException, ApplicationObjectNotFoundException;

    /**
     * Retrieves the list of all users
     * @return The list of users
     */
    public List<UserProfile> getUsers() ;

    /**
     * Retrieves the list of all groups
     * @return An array of GroupProfile
     */
    public List<GroupProfile> getGroups();

    /**
     * Set the attributes of a group
     * @param oid The oid of the group.
     * @param groupName The name of the group. Use null to leave the old value.
     * @param description The description of the group. Use null to leave the old value.
     * @throws InvalidArgumentException If any of the privileges ids is invalid.
     * @throws ApplicationObjectNotFoundException If the group could not be found.
     */
    public void setGroupProperties(long oid, String groupName, String description)
            throws InvalidArgumentException, ApplicationObjectNotFoundException;

   /**
     * Removes a list of users
     * @param oids The ids of the users to be deleted.
     * @throws ApplicationObjectNotFoundException If any of the users could not be found.
     * @throws InvalidArgumentException If any of the users is the default administrator, which can't be deleted
     */
    public void deleteUsers(long[] oids)
            throws ApplicationObjectNotFoundException, InvalidArgumentException;

    /**
     * Removes a list of groups
     * @param oids The oid of the groups to delete.
     * @throws ApplicationObjectNotFoundException If any of the groups could not be found.
     * @throws InvalidArgumentException If the group you are trying to delete contains the default administrator
     */
    public void deleteGroups(long[] oids) throws ApplicationObjectNotFoundException, InvalidArgumentException;
    
   /**
     * Creates a list type item
     * @param className List type
     * @param name new item's name
     * @param displayName new item's display name
     * @return new item's id
     * @throws MetadataObjectNotFoundException if className is not an existing class
     * @throws InvalidArgumentException if the class provided is not a list type
     * @throws OperationNotPermittedException If the class is abstract or marked as in design.
     */
    public long createListTypeItem(String className, String name, String displayName)
            throws MetadataObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException;

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
     * Retrieves the item related to a given list type
     * @param listTypeClassName The class name of list type item
     * @param listTypeItemId The id of list type item
     * @return A RemoteBusinessObjectLight instance representing the item
     * @throws MetadataObjectNotFoundException If the list type class Name is not an existing class
     * @throws InvalidArgumentException if the list type class name provided is not a list type
     * @throws ObjectNotFoundException If the the item id can not be found
     */
    public RemoteBusinessObjectLight getListTypeItem(String listTypeClassName, long listTypeItemId) throws 
        MetadataObjectNotFoundException, InvalidArgumentException, ObjectNotFoundException;
    
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
     * @return A list of ClassMetadataLight instances representing the possible list types
     * @throws ApplicationObjectNotFoundException if the GenericObjectList class does not exist
     */
    public List<ClassMetadataLight> getInstanceableListTypes()
            throws ApplicationObjectNotFoundException;

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
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;

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
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
  
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
    public ViewObject getGeneralView(long viewId) throws ObjectNotFoundException;

    /**
     * Creates a view for a given object. If there's already a view of the provided view type, it will be overwritten
     * @param oid object's oid
     * @param objectClass object class
     * @param name view name
     * @param description view description
     * @param viewClassName view class name (See class ViewObject for details about the supported types)
     * @param structure XML document with the view structure (see http://sourceforge.net/apps/mediawiki/kuwaiba/index.php?title=XML_Documents#To_Save_Object_Views for details about the supported format)
     * @param background background image
     * @return The id of the new view.
     * @throws ObjectNotFoundException if the object can not be found
     * @throws MetadataObjectNotFoundException if the object class can not be found
     * @throws InvalidArgumentException if the view type is not supported
     */
    public long createObjectRelatedView(long oid, String objectClass, String name, String description, 
            String viewClassName, byte[] structure, byte[] background)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * Creates a view not related to a particular object
     * @param viewClass View class
     * @param name view name
     * @param description view description
     * @param structure XML document specifying the view structure (nodes, edges, control points)
     * @param background Background image
     * @return The id of the newly created view
     * @throws InvalidArgumentException if the view type is invalid
     */
    public long createGeneralView(String viewClass, String name, String description, byte[] structure, byte[] background)
            throws InvalidArgumentException;

    /**
     * Updates a view for a given object. If there's already a view of the provided view type, it will be overwritten
     * @param oid object's oid
     * @param objectClass object class
     * @param viewId viewId
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
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * Saves a view not related to a particular object. The view type can not be changed
     * @param oid View id
     * @param name view name. Null to leave unchanged
     * @param description view description. Null to leave unchanged
     * @param structure XML document specifying the view structure (nodes, edges, control points). Null to leave unchanged
     * @param background Background image. If null, the previous will be removed, if 0-sized array, it will remain unchanged
     * @return The summary of the changes.
     * @throws InvalidArgumentException if the view type is invalid or if the background could not be saved.
     * @throws ApplicationObjectNotFoundException if the view couldn't be found
     */
    public ChangeDescriptor updateGeneralView(long oid, String name, String description, byte[] structure, byte[] background)
            throws InvalidArgumentException, ApplicationObjectNotFoundException;


    /**
     * Deletes a list of general views
     * @param ids The ids of the views to be deleted.
     * @throws ApplicationObjectNotFoundException if the view can't be found
     */
    public void deleteGeneralViews(long[] ids) throws ApplicationObjectNotFoundException;

    /**
     * Creates a Query
     * @param queryName The name of the query.
     * @param ownerOid The if of the user that will own the query. Use -1 to make it public.
     * @param queryStructure The structure of the query as an XML document.
     * @param description The description of the query.
     * @return The id of the newly created query.
     * @throws ApplicationObjectNotFoundException If the owner could not be found.
     */
    public long createQuery(String queryName, long ownerOid, byte[] queryStructure,
            String description) throws ApplicationObjectNotFoundException;

    /**
     * Updates an existing query
     * @param queryOid The id of the query
     * @param queryName The name of the query. Leave null to keep the old value.
     * @param ownerOid The id of the user that owns this query. Use -1 to keep the old value.
     * @param queryStructure The structure of the query as an XML document. Leave null to keep the old value.
     * @param description The description of the query. Leave null to keep the old value.
     * @return The summary of the changes.
     * @throws ApplicationObjectNotFoundException If the query can not be found.
     */
    public ChangeDescriptor saveQuery(long queryOid, String queryName, long ownerOid, byte[] queryStructure, String description)
            throws ApplicationObjectNotFoundException;

    /**
     * Deletes a Query
     * @param queryOid The id of the query.
     * @throws ApplicationObjectNotFoundException If the query could not be found
     */
    public void deleteQuery(long queryOid) throws ApplicationObjectNotFoundException;

    /**
     * Gets all queries
     * @param showPublic Include public queries or show only the private ones.
     * @return The list of queries.
     */
    public List<CompactQuery> getQueries(boolean showPublic);

    /**
     * Gets a single query
     * @param queryOid The id of the query.
     * @return The query as an object.
     * @throws ApplicationObjectNotFoundException If the query could not be found.
     */
    public CompactQuery getQuery(long queryOid) 
            throws ApplicationObjectNotFoundException;

    /**
     * Used to perform complex queries. Please note
     * that the first record is reserved for the column headers, so and empty result set
     * will have at least one record.
     * @param query The code-friendly representation of the query made using the graphical query builder
     * @return a set of objects matching the specified criteria as ResultRecord array
     * @throws MetadataObjectNotFoundException If the class to be search is cannot be found
     */
    public List<ResultRecord> executeQuery(ExtendedQuery query) throws MetadataObjectNotFoundException;

    /**
     * Get the data model class hierarchy as an XML document
     * @param showAll
     * @return The class hierarchy as an XML document
     * @throws MetadataObjectNotFoundException If one of the core classes could not be found
     */
    public byte[] getClassHierachy(boolean showAll) 
            throws MetadataObjectNotFoundException;
    
    //Pools
    /**
     * Creates a pool without a parent. They're used as general purpose place to put inventory objects, or as root for particular models
     * @param name Pool name
     * @param description Pool description
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @param type Type of pool. For possible values see ApplicationManager.POOL_TYPE_XXX
     * @return The id of the new pool
     * @throws MetadataObjectNotFoundException If instancesOfClass is not a valid subclass of InventoryObject
     */
    public long createRootPool(String name, String description, String instancesOfClass, int type)
            throws MetadataObjectNotFoundException;
    
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
     */
    public long createPoolInObject(String parentClassname, long parentId, String name, String description, String instancesOfClass, int type)
            throws MetadataObjectNotFoundException, ObjectNotFoundException;

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
     */
    public long createPoolInPool(long parentId, String name, String description, String instancesOfClass, int type)
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException;    
    
    /**
     * Deletes a set of pools. Note that this method will delete and commit the changes until it finds an error, so if deleting any of the pools fails, don't try to delete those that were already processed
     * @param ids the list of ids from the objects to be deleted
     * @throws ApplicationObjectNotFoundException If any of the pools to be deleted couldn't be found
     * @throws OperationNotPermittedException If any of the objects in the pool can not be deleted because it's not a business related instance (it's more a security restriction)
     */
    public void deletePools(long[] ids) throws ApplicationObjectNotFoundException, OperationNotPermittedException;
   
    /**
     * Updates a pool. The class name field is read only to preserve the integrity of the pool. Same happens to the field type
     * @param poolId Pool Id
     * @param name Pool name. If null, this field will remain unchanged
     * @param description Pool description. If null, this field will remain unchanged
     * @return The summary of the changes.
     */
    public ChangeDescriptor setPoolProperties(long poolId, String name, String description);
    
    /**
     * Retrieves the pools that don't have any parent and are normally intended to be managed by the Pool Manager
     * @param className The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use null if you want to get all
     * @param type The type of pools that should be retrieved. Root pools can be for general purpose, or as roots in models
     * @param includeSubclasses Use <code>true</code> if you want to get only the pools whose <code>className</code> property matches exactly the one provided, and <code>false</code> if you want to also include the subclasses
     * @return A set of pools
     */
    public List<Pool> getRootPools(String className, int type, boolean includeSubclasses);
    /**
     * Retrieves the pools associated to a particular object
     * @param objectClassName The parent object class name
     * @param objectId The parent object id
     * @param poolClass The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use null if you want to get all
     * @return A set of pools
     * @throws ObjectNotFoundException If the parent object can not be found
     */
    public List<Pool> getPoolsInObject(String objectClassName, long objectId, String poolClass) 
            throws ObjectNotFoundException;
    /**
     * Retrieves the pools associated to a particular pool
     * @param parentPoolId The parent pool id
     * @param poolClass The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use null if you want to get all
     * @return A set of pools
     * @throws ApplicationObjectNotFoundException If the parent object can not be found
     */
    public List<Pool> getPoolsInPool(long parentPoolId, String poolClass) 
            throws ApplicationObjectNotFoundException;
    
    /**
     * Gets a pool by its id 
     * @param poolId The pool's id
     * @return the pool as a Pool object
     * @throws ApplicationObjectNotFoundException If the pool could not be found
     */
    public Pool getPool(long poolId) throws ApplicationObjectNotFoundException;
    
    /**
     * Gets the list of objects into a pool
     * @param poolId Parent pool id
     * @param limit Result limit. -1 To return all
     * @return The list of items inside the pool
     * @throws ApplicationObjectNotFoundException If the pool id provided is not valid
     */
    public List<RemoteBusinessObjectLight> getPoolItems(long poolId, int limit)
            throws ApplicationObjectNotFoundException;
    
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
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Retrieves the list of general activity log entries
     * @param page current page
     * @param limit limit of results per page. 0 to retrieve them all
     * @return The list of activity log entries
     */
    public List<ActivityLogEntry> getGeneralActivityAuditTrail(int page, int limit);
    
    /**
     * Validates if a user is allowed to call a given web service method
     * @param methodName The method to check if the user is allowed to call it.
     * @param ipAddress The IP address the method is being invoked from
     * @param sessionId The session token
     * @throws NotAuthorizedException If the user is not allowed to invoke the method
     */
    public void validateWebServiceCall(String methodName, String ipAddress, String sessionId) throws NotAuthorizedException;
    
    /**
     * Creates a session
     * @param user User name
     * @param password Password
     * @param IPAddress IP address the session is created from
     * @return A session object with information about the session itself plus information about the user
     * @throws ApplicationObjectNotFoundException If the user does not exist
     * @throws NotAuthorizedException If the password is incorrect or if the user is not enabled.
     */
    public Session createSession(String user, String password, String IPAddress) throws ApplicationObjectNotFoundException, NotAuthorizedException;
    
    /**
     * Retrieves a user from the session ring given a session id
     * @param sessionId The session token
     * @return The user associated to a given session
     * @throws ApplicationObjectNotFoundException If the user could not be found
     */
    public UserProfile getUserInSession(String sessionId) throws ApplicationObjectNotFoundException;
    /**
     * Gets all the users in a group
     * @param groupId The id of the group
     * @return The list of users in that group
     * @throws ApplicationObjectNotFoundException If the group could not be found. 
     */
    public List<UserProfile> getUsersInGroup(long groupId) throws ApplicationObjectNotFoundException;
    /**
     * Retrieves the list of groups a user belongs to
     * @param userId The id of the user
     * @return The list of groups for this user
     * @throws ApplicationObjectNotFoundException If the user could not be found 
     */
    public List<GroupProfileLight> getGroupsForUser(long userId) throws ApplicationObjectNotFoundException;
    /**
     * Executes a set of patches
     * @return The error messages (if any) for every executed patch. If null, the execution was successful
     */
    public String[] executePatch() ;
    
    /**
     * Closes a session
     * @param sessionId The session id
     * @param remoteAddress remote IP Address
     * @throws NotAuthorizedException If the session ID is Invalid or the IP does not match with the one registered for this session
     */
    public void closeSession(String sessionId, String remoteAddress) throws NotAuthorizedException;
 
    /**
     * Sets the configuration variables of this manager
     * @param properties A Properties object with the configuration variables
     */
    public void setConfiguration(Properties properties);
    
    /**
     * Gets the configuration variables of this manager
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
     * @param needReturn
     * @return A table with results, that could also be interpreted as a multidimensional array with numerous paths
     * @throws NotAuthorizedException If the user is not allowed to run arbitrary code on the database
     * @deprecated Don't use it, instead, create a method in the corresponding entity manager instead of running code directly on the database
     */
    public HashMap<String, RemoteBusinessObjectList> executeCustomDbCode(String dbCode, boolean needReturn) throws NotAuthorizedException;
    
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
     * @return The summary of the changes
     * @throws ApplicationObjectNotFoundException If the task could not be found
     * @throws InvalidArgumentException If the property name has an invalid value
     */
    public ChangeDescriptor updateTaskProperties(long taskId, String propertyName, String propertyValue) throws ApplicationObjectNotFoundException, InvalidArgumentException;
    /**
     * Updates the parameters of a task. If any of the values is null, that parameter will be deleted, if the parameter does not exist, it will be created
     * @param taskId Task id
     * @param parameters The parameters to be modified as pairs paramName/paramValue
     * @return The summary of the changes
     * @throws ApplicationObjectNotFoundException If the task could not be found
     */
    public ChangeDescriptor updateTaskParameters(long taskId, List<StringPair> parameters) throws ApplicationObjectNotFoundException;
    /**
     * Updates a task schedule
     * @param taskId Task id
     * @param schedule New schedule
     * @return The summary of the changes
     * @throws ApplicationObjectNotFoundException If the task could not be found
     */
    public ChangeDescriptor updateTaskSchedule(long taskId, TaskScheduleDescriptor schedule) throws ApplicationObjectNotFoundException;
    /**
     * Updates a task notification type
     * @param taskId Task id
     * @param notificationType New notification type
     * @return The summary of the changes
     * @throws ApplicationObjectNotFoundException If the task could not be found
     */
    public ChangeDescriptor updateTaskNotificationType(long taskId, TaskNotificationDescriptor notificationType) throws ApplicationObjectNotFoundException;
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
     * @return The summary of the changes
     * @throws ApplicationObjectNotFoundException If the task or the user could not be found
     * @throws InvalidArgumentException If the user is already subscribed to the task
     */
    public ChangeDescriptor subscribeUserToTask(long userId, long taskId) throws ApplicationObjectNotFoundException, InvalidArgumentException;
    /**
     * Unsubscribes a user from a task, so it will no longer be notified about the result of its execution
     * @param taskId Id of the task
     * @param userId Id of the user
     * @return The summary of the changes
     * @throws ApplicationObjectNotFoundException If the task or the user could not be found
     */
    public ChangeDescriptor unsubscribeUserFromTask(long userId, long taskId) throws ApplicationObjectNotFoundException;
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
    /**
     * Creates a template.
     * @param templateClass The class you want to create a template for.
     * @param templateName The name of the template. It can not be null.
     * @return The id of the newly created template.
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException If the provided class does not exist
     * @throws org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException If the template class is abstract.
     */
    public long createTemplate(String templateClass, String templateName) throws MetadataObjectNotFoundException, OperationNotPermittedException;
    /**
     * Creates an object inside a template.
     * @param templateElementClass Class of the object you want to create.
     * @param templateElementParentClassName Class of the parent to the obejct you want to create.
     * @param templateElementParentId Id of the parent to the obejct you want to create.
     * @param templateElementName Name of the element.
     * @return The id of the new object.
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException If the object (or its parent) class could not be found
     * @throws org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException If the parent object could not be found
     * @throws org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException If the class provided to create the new element from is abstract.
     */
    public long createTemplateElement(String templateElementClass, String templateElementParentClassName, 
        long templateElementParentId, String templateElementName) 
        throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, OperationNotPermittedException;
    
    /**
     * Create an special object inside an template
     * @param tsElementClass Template special element class
     * @param tsElementParentClassName Template special element parent class name
     * @param tsElementParentId Template special element parent Id
     * @param tsElementName Template special element name
     * @return The id of the new object
     * @throws OperationNotPermittedException If the element class are not a possible special child of the element parent class
     *                                        Or if the element class given are abstract
     * @throws MetadataObjectNotFoundException If the element class or element parent class can not be found
     * @throws ApplicationObjectNotFoundException If the element parent can no be found
     */
    public long createTemplateSpecialElement(String tsElementClass, String tsElementParentClassName, long tsElementParentId, String tsElementName) 
        throws OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException;
    /**
     * Updates the value of an attribute of a template element.
     * @param templateElementClass Class of the element you want to update.
     * @param templateElementId Id of the element you want to update.
     * @param attributeNames Names of the attributes that you want to be updated as an array of strings.
     * @param attributeValues The values of the attributes you want to upfate. For list types, it's the id of the related type
     * @return The summary of the changes
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException If any of the classes provided as arguments do not exist
     * @throws org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException If the template element could not be found
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException If the arrays attributeNames and attributeValues have different sizes
     */
    public ChangeDescriptor updateTemplateElement(String templateElementClass, long templateElementId, 
            String[] attributeNames, String[] attributeValues) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException;
    /**
     * Deletes an element within a template or a template itself.
     * @param templateElementClass The template element class.
     * @param templateElementId The template element id.
     * @return The summary of the changes
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException If the element's class could not be found.
     * @throws org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException If the element could not be found.
     */
    public ChangeDescriptor deleteTemplateElement(String templateElementClass, long templateElementId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException;
    /**
     * Gets the templates available for a given class
     * @param className Class whose templates we need
     * @return A list of templates (actually, the top element) as a list of RemoteOObjects
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException If the class provided could not be found.
     */
    public List<RemoteBusinessObjectLight> getTemplatesForClass(String className) throws MetadataObjectNotFoundException;
    /**
     * Retrieves the children of a given template element.
     * @param templateElementClass Template element class.
     * @param templateElementId Template element id.
     * @return The template element's children as a list of RemoteBusinessObjectLight instances.
     */
    public List<RemoteBusinessObjectLight> getTemplateElementChildren(String templateElementClass, long templateElementId);
    /**
     * Retrieves the children of a given template special element.
     * @param tsElementClass Template special element class.
     * @param tsElementId Template special element id.
     * @return The template element's children as a list of RemoteBusinessObjectLight instances.
     */
    public List<RemoteBusinessObjectLight> getTemplateSpecialElementChildren(String tsElementClass, long tsElementId);
    /**
     * Retrives all the information of a given template element.
     * @param templateElementClass Template element class.
     * @param templateElementId Template element id.
     * @return The template element information
     * @throws MetadataObjectNotFoundException If the template class does not exist
     * @throws ApplicationObjectNotFoundException If the template element could not be found.
     * @throws InvalidArgumentException If an attribute value can't be mapped into value.
     */
    public RemoteBusinessObject getTemplateElement(String templateElementClass, long templateElementId)
        throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Copy template elements within templates. Should not be used to copy entire templates.
     * @param sourceObjectsClassNames Array with the class names of the elements to be copied.
     * @param sourceObjectsIds  Array with the ids of the elements to be copied.
     * @param newParentClassName Class of the parent of the copied objects.
     * @param newParentId Id of the parent of the copied objects.
     * @return An array with the ids of the newly created elements in the same order they were provided.
     * @throws MetadataObjectNotFoundException If any of the classes could not be found.
     * @throws ApplicationObjectNotFoundException If any of the source template elements could not be found.
     * @throws ArraySizeMismatchException If the arrays provided as arguments have different sizes.
     */
    public long[] copyTemplateElements(String[] sourceObjectsClassNames, long[] sourceObjectsIds, 
            String newParentClassName, long newParentId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, ArraySizeMismatchException;
    
    /**
     * Copy template special elements within templates,
     * @param sourceObjectsClassNames Array with the class names of the special elements to be copied.
     * @param sourceObjectsIds Array with the ids of the special elements to be copied.
     * @param newParentClassName Class of the parent of the copied objects.
     * @param newParentId Id of the parent of the copied objects.
     * @return An array with the ids of the newly created special elements in the same order they were provided.
     * @throws ArraySizeMismatchException If the arrays provided as arguments have different sizes.
     * @throws MetadataObjectNotFoundException If any of the classes could not be found.
     * @throws ApplicationObjectNotFoundException If any of the source template elements could not be found.
     */
    public long [] copyTemplateSpecialElement(String[] sourceObjectsClassNames, long [] sourceObjectsIds, String newParentClassName, long newParentId) 
        throws ArraySizeMismatchException, ApplicationObjectNotFoundException, MetadataObjectNotFoundException;
    // Favorites
    /**
     * Adds an object to the favorites folder
     * @param objectClass Object class
     * @param objectId Object id
     * @param favoritesFolderId favorites folder id
     * @param userId User Id
     * @throws ApplicationObjectNotFoundException If the favorites folder can not be found
     * @throws MetadataObjectNotFoundException If the object can not be found
     * @throws ObjectNotFoundException If the object can not be found
     * @throws OperationNotPermittedException If the object have a relationship with the favorite folder
     */
    public void addObjectTofavoritesFolder(String objectClass, long objectId, long favoritesFolderId, long userId) 
        throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException;
    
    /**
     * Removes an object associated to a favorites folder
     * @param objectClass Object class
     * @param objectId Object id
     * @param favoritesFolderId favorites folder id
     * @param userId User Id
     * @throws ApplicationObjectNotFoundException If the favorites folder can not be found
     * @throws MetadataObjectNotFoundException If the object can not be found
     * @throws ObjectNotFoundException If the object can not be found
     */
    public void removeObjectFromfavoritesFolder(String objectClass, long objectId, long favoritesFolderId, long userId) 
        throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException, ObjectNotFoundException;
        
    /**
     * Create a relationship between an user and a new favorites folder
     * @param name favorites folder name
     * @param userId User id
     * @return The new favorites folder Id
     * @throws ApplicationObjectNotFoundException If the user can not be found 
     * @throws InvalidArgumentException If the name is null or empty
     */
    public long createFavoritesFolderForUser(String name, long userId) 
        throws ApplicationObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Delete a Bookmark Folder of an User
     * @param favoritesFolderId favorites folder id
     * @param userId User Id
     * @throws ApplicationObjectNotFoundException If any favorites folder in the array can not be found
     */
    public void deleteFavoritesFolders (long[] favoritesFolderId, long userId)
        throws ApplicationObjectNotFoundException;
    
    /**
     * Get the favorites folders create by an user.
     * @param userId user id
     * @return List of Bookmarks folders for an User
     * @throws ApplicationObjectNotFoundException If the user can not be found
     */
    public List<FavoritesFolder> getFavoritesFoldersForUser(long userId) 
        throws ApplicationObjectNotFoundException;
    
    /**
     * Get the object assigned to the bookmark
     * @param favoritesFolderId favorites folder id
     * @param limit Max number of results
     * @param userId User Id
     * @throws ApplicationObjectNotFoundException If the favorites folder can not be found
     * @return List of objects related to bookmark
     */
    public List<RemoteBusinessObjectLight> getObjectsInFavoritesFolder(long favoritesFolderId, long userId, int limit) 
        throws ApplicationObjectNotFoundException;
    /**
     * Get the bookmarks where an object is associated
     * @param userId User id
     * @param objectClass Object class
     * @param objectId Object id
     * @return list of favorites folders where an object are an item
     * @throws MetadataObjectNotFoundException If the object can not be found
     * @throws ObjectNotFoundException If the object can not be found
     * @throws ApplicationObjectNotFoundException If the object is associated to a bookmark folder but 
     *                                            The favorites folder is not associated to the current user
     */
    public List<FavoritesFolder> getFavoritesFoldersForObject(long userId, String objectClass, long objectId) 
        throws MetadataObjectNotFoundException, ObjectNotFoundException, ApplicationObjectNotFoundException;
    
    /**
     * Gets a favorites folder
     * @param favoritesFolderId favorites folder id
     * @param userId User id
     * @throws ApplicationObjectNotFoundException If the favorites folder can not be found
     * @return The favorite folder with id
     */
    public FavoritesFolder getFavoritesFolder(long favoritesFolderId, long userId) 
        throws ApplicationObjectNotFoundException;
    
    /**
     * Updates a favorites folder
     * @param favoritesFolderId favorites folder id
     * @param userId User Id
     * @param favoritesFolderName favorites folder name
     * @throws ApplicationObjectNotFoundException If the favorites folder can not be found
     * @throws IllegalArgumentException If the name of the favorites folder is null or empty
     */
    public void updateFavoritesFolder(long favoritesFolderId, long userId, String favoritesFolderName) 
        throws ApplicationObjectNotFoundException, IllegalArgumentException;
    
    /**
     * Creates a business rule given a set of constraints
     * @param ruleName Rule name
     * @param ruleDescription Rule description
     * @param ruleType Rule type. See BusinesRule.TYPE* for possible values.
     * @param ruleScope The scope of the rule. See BusinesRule.SCOPE* for possible values.
     * @param appliesTo The class this rule applies to. Can not be null.
     * @param ruleVersion The version of the rule. Useful to migrate it if necessary in further versions of the platform
     * @param constraints An array with the definition of the logic to be matched with the rule. Can not be empty or null
     * @return The id of the newly created business rule
     * @throws InvalidArgumentException If any of the parameters is null (strings) or leer than 1 or if the constraints array is null or empty
     */
    public long createBusinessRule(String ruleName, String ruleDescription, int ruleType, 
            int ruleScope, String appliesTo, String ruleVersion, List<String> constraints) throws InvalidArgumentException;
    
    /**
     * Deletes a business rule
     * @param businessRuleId Rule id
     * @throws ApplicationObjectNotFoundException If the given rule does not exist
     */
    public void deleteBusinessRule(long businessRuleId) throws ApplicationObjectNotFoundException;
    
    /**
     * Retrieves the business rules of a particular type.
     * @param type Rule type. See BusinesRule.TYPE* for possible values. Use -1 to retrieve all
     * @return The list of business rules with the matching type.
     */
    public List<BusinessRule> getBusinessRules(int type);
    /**
     * check if a relationship can be established between two objects with the attribute values defined in the rule
     * @param sourceObjectClassName The class of the element that's the subject of the rule
     * @param sourceObjectId The id of the subject of the rule
     * @param targetObjectClassName The class of the element that's the object of the rule
     * @param targetObjectId The id of the object of the rule
     * @throws BusinessRuleException If the rule matches, and the involved objects don't comply with the conditions stated by the rule
     * @throws InvalidArgumentException If the rule is malformed or can not be read.
     */
    public void checkRelationshipByAttributeValueBusinessRules(String sourceObjectClassName, long sourceObjectId ,
            String targetObjectClassName, long targetObjectId) throws BusinessRuleException, InvalidArgumentException;
}
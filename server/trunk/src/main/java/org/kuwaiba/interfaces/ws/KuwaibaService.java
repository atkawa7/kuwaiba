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

package org.kuwaiba.interfaces.ws;

import com.neotropic.kuwaiba.modules.reporting.model.RemoteReport;
import com.neotropic.kuwaiba.modules.reporting.model.RemoteReportLight;
import com.neotropic.kuwaiba.scheduling.BackgroundJob;
import com.neotropic.kuwaiba.scheduling.JobManager;
import com.neotropic.kuwaiba.sync.model.SyncAction;
import com.neotropic.kuwaiba.sync.model.SyncFinding;
import com.neotropic.kuwaiba.sync.model.SyncResult;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.util.Constants;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.interfaces.ws.todeserialize.TransientQuery;
import org.kuwaiba.interfaces.ws.toserialize.application.ApplicationLogEntry;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteQuery;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteQueryLight;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.application.ResultRecord;
import org.kuwaiba.interfaces.ws.toserialize.application.GroupInfo;
import org.kuwaiba.interfaces.ws.toserialize.application.GroupInfoLight;
import org.kuwaiba.interfaces.ws.toserialize.application.PrivilegeInfo;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteBackgroundJob;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteBusinessRule;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteContact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteFavoritesFolder;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteFileObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteFileObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.application.RemotePool;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationConfiguration;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationGroup;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteTask;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteTaskResult;
import org.kuwaiba.interfaces.ws.toserialize.application.TaskNotificationDescriptor;
import org.kuwaiba.interfaces.ws.toserialize.application.TaskScheduleDescriptor;
import org.kuwaiba.interfaces.ws.toserialize.application.UserInfo;
import org.kuwaiba.interfaces.ws.toserialize.application.UserInfoLight;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObject;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.AssetLevelCorrelatedInformation;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteLogicalConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLightList;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectSpecialRelationships;
import org.kuwaiba.interfaces.ws.toserialize.business.modules.sdh.RemoteSDHContainerLinkDefinition;
import org.kuwaiba.interfaces.ws.toserialize.business.modules.sdh.RemoteSDHPosition;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteAttributeMetadata;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteConfigurationVariable;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationProvider;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteValidator;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteValidatorDefinition;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteMPLSConnectionDetails;

/**
 * Main web service
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Singleton
@WebService (serviceName = "KuwaibaService")
public class KuwaibaService {
    /**
     * The main session bean in charge of providing the business logic
     */
    @EJB
    private WebserviceBean wsBean;
   /**
     * The context to get information about each request
     */
    @Resource
    private WebServiceContext context;

    // <editor-fold defaultstate="collapsed" desc="Application methods. Click on the + sign on the left to edit the code.">
    /**
     * Creates a session. Only one session per type is allowed. If a new session is created and there was already one of the same type, the old one will be discarded. See RemoteSession.TYPE_XXX for possible session types
     * @param username user login name
     * @param password user password
     * @param sessionType The type of session to be created. This type depends on what kind of client is trying to access (a desktop client, a web client, a web service user, etc. See RemoteSession.TYPE_XXX for possible session types
     * @return A session object, including the session token
     * @throws ServerSideException If the user does not exist
     *                             If the password is incorrect or if the user is not enabled.
     */
    @WebMethod(operationName = "createSession")
    public RemoteSession createSession(@WebParam(name = "username") String username,
            @WebParam(name = "password") String password, @WebParam(name = "sessionType") int sessionType) throws ServerSideException{
        try {
            String remoteAddress = getIPAddress();
            return wsBean.createSession(username, password, sessionType, remoteAddress);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createSession: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    /**
     * Closes a session
     * @param sessionId Session token
     * @throws ServerSideException If the session ID is Invalid or the IP does not match with the one registered for this session
     */
    @WebMethod(operationName = "closeSession")
    public void closeSession(@WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            String remoteAddress = getIPAddress();
            wsBean.closeSession(sessionId, remoteAddress);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in closeSession: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

   /**
     * Retrieves the list of uses
     * @param sessionId session token
     * @return The list of users
     * @throws ServerSideException If the user is not allowed to invoke the method
     */
    @WebMethod(operationName = "getUsers")
    public List<UserInfo> getUsers(@WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try
        {
            return wsBean.getUsers(getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getUsers: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the users in a group
     * @param groupId The id of the group
     * @param sessionId Session token
     * @return The list of users in the requested group
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the group does not exist or something unexpected happened.
     */
    @WebMethod(operationName = "getUsersInGroup")
    public List<UserInfo> getUsersInGroup(@WebParam(name = "groupId")long groupId, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.getUsersInGroup(groupId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getUsersInGroup: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the groups a user belongs to
     * @param userId  The id of the user
     * @param sessionId Session token
     * @return The list of groups the user belongs to
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the group does not exist or something unexpected happened.
     */
    @WebMethod(operationName = "getGroupsForUser")
    public List<GroupInfoLight> getGroupsForUser(@WebParam(name = "userId")long userId, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.getGroupsForUser(userId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getGroupsForUser: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the list of groups
     * @param sessionId Session token
     * @return A group object list
     * @throws ServerSideException If the user is not allowed to invoke the method 
     *                             or any possible error raised at runtime
     */
    @WebMethod(operationName = "getGroups")
    public List<GroupInfo> getGroups(@WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getGroups(getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getGroups: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Creates a user
     * @param username User name. Can't be null, empty or have non standard characters.
     * @param password A password (in plain text, it'll be encrypted later). Can't be null nor an empty string
     * @param firstName User's first name
     * @param lastName User's last name
     * @param enabled Is this user enable by default?
     * @param type The type of the user. See UserProfileLight.USER_TYPE* for possible values
     * @param privileges A list privileges that will be granted to this user.
     * @param defaultGroupId Default group this user will be associated to. Users <b>always</b> belong to at least one group. Other groups can be added later.
     * @param sessionId Session token
     * @return The new user Id
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the username is null or empty or the username already exists
     */
    @WebMethod(operationName = "createUser")
    public long createUser(
            @WebParam(name = "username")String username,
            @WebParam(name = "password")String password,
            @WebParam(name = "firstName")String firstName,
            @WebParam(name = "LastName")String lastName,
            @WebParam(name = "enabled")boolean enabled,
            @WebParam(name = "type") int type,
            @WebParam(name = "privileges")List<PrivilegeInfo> privileges,
            @WebParam(name = "defaultGroupId")long defaultGroupId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.createUser(username, password, firstName, lastName, enabled, type, privileges, defaultGroupId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createUser: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Sets the properties of a given user using the id to search for it
     * @param oid User id
     * @param username New user's name. Use null to leave it unchanged.
     * @param password New user's password. Use null to leave it unchanged
     * @param firstName New user's first name. Use null to leave it unchanged
     * @param lastName New user's last name. Use null to leave it unchanged
     * @param enabled 0 for false, 1 for true, -1 to leave it unchanged
     * @param type User type. See UserProfile.USER_TYPE* for possible values. Use -1 to leave it unchanged
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the username is null or empty or 
     *                             the username already exists or if the user could not be found
     */
    @WebMethod(operationName = "setUserProperties")
    public void setUserProperties(
            @WebParam(name = "oid")long oid,
            @WebParam(name = "username")String username,
            @WebParam(name = "firstName")String firstName,
            @WebParam(name = "lastName")String lastName,
            @WebParam(name = "password")String password,
            @WebParam(name = "enabled")int enabled,
            @WebParam(name = "type")int type,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.setUserProperties(oid, username, password, firstName, lastName, enabled, type, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in setUserProperties: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Adds a user to a group
     * @param userId The id of the user to be added to the group
     * @param groupId Id of the group which the user will be added to
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the user is already related to that group or if the user or group can not be found.
     */
    @WebMethod(operationName = "addUserToGroup")
    public void addUserToGroup(@WebParam(name = "userId") long userId, 
            @WebParam(name = "groupId") long groupId, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.addUserToGroup(userId, groupId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in addUserToGroup: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Removes a user from a group
     * @param userId The id of the user to be added to the group
     * @param groupId Id of the group which the user will be added to
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the user is not related to that group or if the user or the group could not be found
     */
    @WebMethod(operationName = "removeUserFromGroup")
    public void removeUserFromGroup(@WebParam(name = "userId") long userId, 
            @WebParam(name = "groupId") long groupId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.removeUserFromGroup(userId, groupId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in removeUserFromGroup: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Sets a privilege to a user. If the privilege already exists, the access level is updated, otherwise, the new privilege is added to the user.
     * @param userId The user Id
     * @param featureToken The feature token. See class Privilege for details. Note that this token must match to the one expected by the client application. That's the only way the correct features will be enabled.
     * @param accessLevel The feature token. See class Privilege.ACCESS_LEVEL* for details. 
     * @param sessionId Session token
     * @throws ServerSideException If the access level is invalid, if the featureToken has a wrong format or if the user already has that privilege or if the user could not be found.
     */
    @WebMethod(operationName = "setPrivilegeToUser")
    public void setPrivilegeToUser(@WebParam(name = "userId") long userId, 
            @WebParam(name = "featureToken") String featureToken, 
            @WebParam(name = "accessLevel") int accessLevel, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.setPrivilegeToUser(userId, featureToken, accessLevel, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in addPrivilegeToUser: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Sets a privilege to a group. If the privilege already exists, the access level is updated, otherwise, the new privilege is added to the group.
     * @param groupId The user Id
     * @param featureToken The feature token. See class Privilege for details. Note that this token must match to the one expected by the client application. That's the only way the correct features will be enabled.
     * @param accessLevel The feature token. See class Privilege.ACCESS_LEVEL* for details. 
     * @param sessionId Session token
     * @throws ServerSideException If the access level is invalid, if the featureToken has a wrong format or if the group already has that privilege or if the group could not be found
     */
    @WebMethod(operationName = "setPrivilegeToGroup")
    public void setPrivilegeToGroup(@WebParam(name = "groupId") long groupId, 
            @WebParam(name = "featureToken") String featureToken, 
            @WebParam(name = "accessLevel") int accessLevel, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.setPrivilegeToGroup(groupId, featureToken, accessLevel, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in addPrivilegeToGroup: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Removes a privilege from a user
     * @param userId Id of the user
     * @param featureToken The feature token. See class Privilege for details. 
     * @param sessionId Session token
     * @throws ServerSideException If the feature token is not related to the user or if the user could not be found
     */
    @WebMethod(operationName = "removePrivilegeFromUser")
    public void removePrivilegeFromUser(@WebParam(name = "userId") long userId, 
            @WebParam(name = "featureToken") String featureToken, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.removePrivilegeFromUser(userId, featureToken, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in removePrivilegeFromUser: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Removes a privilege from a user
     * @param groupId Id of the group
     * @param featureToken The feature token. See class Privilege for details. 
     * @param sessionId Session token
     * @throws ServerSideException If the feature token is not related to the group or if the group could not be found
     */
    @WebMethod(operationName = "removePrivilegeFromGroup")
    public void removePrivilegeFromGroup(@WebParam(name = "groupId") long groupId, 
            @WebParam(name = "featureToken") String featureToken, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.removePrivilegeFromGroup(groupId, featureToken, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in removePrivilegeFromGroup: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Creates a group
     * @param groupName Group name
     * @param description Group description
     * @param users List of user ids to be related to this group
     * @param sessionId Session token
     * @return The group id
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the provided users could not be found
     */
    @WebMethod(operationName = "createGroup")
    public long createGroup(
            @WebParam(name = "groupName")String groupName,
            @WebParam(name = "description")String description,
            @WebParam(name = "users")List<Long> users,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try
        {
            return wsBean.createGroup(groupName, description, users, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createGroup: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Sets the properties for an existing group
     * @param oid Group id
     * @param groupName New group name (null if unchanged)
     * @param description New group description (null if unchanged)
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
                                      If any of the privileges ids is invalid.
                                      If the group could not be found.
     */
    @WebMethod(operationName = "setGroupProperties")
    public void setGroupProperties(@WebParam(name = "oid")long oid,
            @WebParam(name = "groupName")String groupName,
            @WebParam(name = "description")String description,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try
        {
            wsBean.setGroupProperties(oid, groupName, description, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in setGroupProperties: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Deletes a list of users
     * @param oids List of user ids to be deleted
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the users is the default administrator, which can't be deleted
     *                             Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteUsers")
    public void deleteUsers(@WebParam(name = "oids")long[] oids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.deleteUsers(oids, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteUsers: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Deletes a list of groups
     * @param oids list of group ids to be deleted
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the group you are trying to delete contains the default administrator
     *                             Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteGroups")
    public void deleteGroups(@WebParam(name = "oids")long[] oids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.deleteGroups(oids, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteGroups: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Gets a particular view related to an object
     * @param oid Object id
     * @param objectClass Object class
     * @param viewId The view id
     * @param sessionId Session token
     * @return The View object (which is basically an XML document)
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object or the view can not be found
     *                             If the corresponding class metadata can not be found
     *                             If the provided view type is not supported
     */
    @WebMethod(operationName = "getObjectRelatedView")
    public RemoteViewObject getObjectRelatedView(@WebParam(name = "oid")String oid,
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "viewId")long viewId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getObjectRelatedView(oid, objectClass, viewId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getObjectRelatedView: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Returns list of views associated to an object
     * @param oid Object id
     * @param objectClass Object class
     * @param viewType View type
     * @param limit Max number of results
     * @param sessionId Session token
     * @return List of objects related to the object
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object can not be found
     *                             If the corresponding class metadata can not be found
     *                             If the provided view type is not supported
     */
    @WebMethod(operationName = "getObjectRelatedViews")
    public List<RemoteViewObjectLight> getObjectRelatedViews(@WebParam(name = "oid")String oid,
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "viewType")int viewType,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getObjectRelatedViews(oid, objectClass, viewType, limit, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getObjectRelatedViews: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Gets all views that are not related to a particular object
     * @param viewClass View class. Used to filter
     * @param limit Max number if results
     * @param sessionId Session token
     * @return A list of views
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the view class does not exist
     *                             If the user is not allowed to query for general views
     */
    @WebMethod(operationName = "getGeneralViews")
    public RemoteViewObjectLight[] getGeneralViews(@WebParam(name = "viewClass")String viewClass,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getGeneralViews(viewClass, limit, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getGeneralViews: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Gets the information of a particular view
     * @param viewId View id
     * @param sessionId Session token
     * @return The view
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the requested view
     */
    @WebMethod(operationName = "getGeneralView")
    public RemoteViewObject getGeneralView(@WebParam(name = "viewId")long viewId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try {
            return wsBean.getGeneralView(viewId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getGeneralView: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates a view for a given list type item. If there's already a view of the provided view type, it will be overwritten
     * @param listTypeItemId list type item id
     * @param listTypeItemClassName list type item class name
     * @param viewClassName view class name
     * @param name view name
     * @param description view description
     * @param structure XML document with the view structure
     * @param background background image
     * @param sessionId Session token
     * @return The id of the new view.
     * @throws ServerSideException If the list type item class can not be found
     *                             If the view type is not supported
     */
    @WebMethod(operationName = "createListTypeItemRelatedView")
    public long createListTypeItemRelatedView(
        @WebParam(name = "listTypeItemId") String listTypeItemId, 
        @WebParam(name = "listTypeItemClassName") String listTypeItemClassName, 
        @WebParam(name = "viewClassName") String viewClassName, 
        @WebParam(name = "name") String name, 
        @WebParam(name = "description") String description, 
        @WebParam(name = "structure") byte [] structure, 
        @WebParam(name = "background") byte [] background, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            return wsBean.createListTypeItemRelatedView(listTypeItemId, listTypeItemClassName, viewClassName, 
                name, description, structure, background, getIPAddress(), sessionId);            
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createListTypeItemRelateView: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Updates a view for a given list type item. If there's already a view of the provided view type, it will be overwritten
     * @param listTypeItemId list type item id
     * @param listTypeItemClass list type item class
     * @param viewId viewId
     * @param name view name
     * @param description view description
     * @param structure XML document with the view structure
     * @param background Background image. If null, the previous will be removed, if 0-sized array, it will remain unchanged
     * @param sessionId Session token
     * @throws ServerSideException If the list type item can not be found
     *                             If the list type item class can not be found
     *                             If the view type is not supported
     */
    @WebMethod(operationName = "updateListTypeItemRelatedView")
    public void updateListTypeItemRelatedView(
        @WebParam(name = "listTypeItemId") String listTypeItemId, 
        @WebParam(name = "listTypeItemClass") String listTypeItemClass, 
        @WebParam(name = "viewId") long viewId, 
        @WebParam(name = "name") String name, 
        @WebParam(name = "description") String description, 
        @WebParam(name = "structure") byte[] structure, 
        @WebParam(name = "background") byte[] background, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        
        try {
            wsBean.updateListTypeItemRelatedView(listTypeItemId, listTypeItemClass, viewId, 
                name, description, structure, background, getIPAddress(), sessionId);            
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateListTypeItemRelatedView: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets a view related to a list type item, given its id. An example of these views is a layout associated to a particular device model
     * @param listTypeItemId list type item id
     * @param listTypeItemClass list type item class
     * @param viewId view id
     * @param sessionId Session token
     * @return The associated view (there should be only one of each type). Null if there's none yet
     * @throws ServerSideException If the list type item or the view can not be found.
     *                             If the corresponding class metadata can not be found.
     *                             If the provided view type is not supported.
     */
    @WebMethod(operationName = "getListTypeItemRelatedView")
    public RemoteViewObject getListTypeItemRelatedView(
        @WebParam(name = "listTypeItemId") String listTypeItemId, 
        @WebParam(name = "listTypeItemClass") String listTypeItemClass, 
        @WebParam(name = "viewId") long viewId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            return wsBean.getListTypeItemRelatedView(listTypeItemId, listTypeItemClass, viewId, getIPAddress(), sessionId);            
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getListTypeItemRelatedView: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets all the views related to a list type item. An example of these views is a layout associated to a particular device model
     * @param listTypeItemId list type item id
     * @param listTypeItemClass list type class name
     * @param limit max number of results
     * @param sessionId Session token
     * @return The associated views
     * @throws ServerSideException If the corresponding class metadata can not be found
     *                             If the provided view type is not supported
     */
    @WebMethod(operationName = "getListTypeItemRelatedViews")
    public RemoteViewObjectLight[] getListTypeItemRelatedViews(
        @WebParam(name = "listTypeItemId")  String listTypeItemId, 
        @WebParam(name = "listTypeItemClass")  String listTypeItemClass, 
        @WebParam(name = "limit")  int limit, 
        @WebParam(name = "sessionId")  String sessionId) throws ServerSideException {
        
        try {
            return wsBean.getListTypeItemRelatedViews(listTypeItemId, listTypeItemClass, limit, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getListTypeItemRelatedViews: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }        
    }
    
    /**
     * Deletes a view related to a list type item
     * @param listTypeItemId list type item id
     * @param listTypeItemClass list type class name
     * @param viewId related view id
     * @param sessionId Session token
     * @throws ServerSideException If the list type item class can not be found
     *                             If the list type item can no be found using the id
     *                             If the view can not be found
     */
    @WebMethod(operationName = "deleteListTypeItemRelatedView")
    public void deleteListTypeItemRelatedView(
        @WebParam(name = "listTypeItemId") String listTypeItemId, 
        @WebParam(name = "listTypeItemClass") String listTypeItemClass, 
        @WebParam(name = "viewId") long viewId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.deleteListTypeItemRelatedView(listTypeItemId, listTypeItemClass, viewId, getIPAddress(), sessionId);
        } catch (Exception e) {
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteListTypeItemRelatedView: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the list of template elements with a device layout
     * @param sessionId Session id token
     * @return the list of template elements with a device layout
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "getDeviceLayouts")
    public List<RemoteObjectLight> getDeviceLayouts(
        @WebParam(name = "sessionId")  String sessionId) throws ServerSideException {
        try {
            return wsBean.getDeviceLayouts(getIPAddress(), sessionId);
        } catch (Exception e) {
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getDeviceLayouts: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Gets the device layout structure
     * @param oid object id
     * @param className class of object
     * @param sessionId Session id token
     * @return the structure of the device layout
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "getDeviceLayoutStructure")
    public byte[] getDeviceLayoutStructure(
        @WebParam(name = "oid") String oid, 
        @WebParam(name = "className") String className, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.getDeviceLayoutStructure(oid, className, getIPAddress(), sessionId);
        } catch (Exception e) {
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getDeviceLayoutStructure: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }    

    /**
     * Creates a view an relates it to an existing object
     * @param objectId Object id
     * @param objectClass Object class
     * @param name View name
     * @param description View description
     * @param viewClassName View class name
     * @param structure Structure (as an XML document)
     * @param background Background
     * @param sessionId Session id
     * @return The id of the newly created view
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object can not be found
     *                             If the object class can not be found
     *                             If the view type is not supported
     */
    @WebMethod(operationName = "createObjectRelatedView")
    public long createObjectRelatedView(@WebParam(name = "objectId")String objectId,
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "name")String name,
            @WebParam(name = "description")String description,
            @WebParam(name = "viewClassName")String viewClassName,
            @WebParam(name = "structure")byte[] structure,
            @WebParam(name = "background")byte[] background,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try {
            return wsBean.createObjectRelatedView(objectId, objectClass, name, description, viewClassName, structure, background, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createObjectRelatedView: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates a general view (a view that is not associated to any object)
     * @param viewClass View class
     * @param name View name
     * @param description Description
     * @param structure Structure
     * @param background background
     * @param sessionId Session id
     * @return The id of the newly created view
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             InvalidArgumentException if the view type is invalid
     */
    @WebMethod(operationName = "createGeneralView")
    public long createGeneralView(@WebParam(name = "viewClass")String viewClass,
            @WebParam(name = "name")String name,
            @WebParam(name = "description")String description,
            @WebParam(name = "structure")byte[] structure,
            @WebParam(name = "background")byte[] background,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.createGeneralView(viewClass, name, description, structure, background, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createGeneralView: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Updates an object view (a view that is linked to a particular object)
     * @param objectOid Object id
     * @param objectClass Object class
     * @param viewId View id
     * @param viewName View name. Null to leave unchanged
     * @param viewDescription View description. Null to leave unchanged
     * @param structure View structure. Null to leave unchanged
     * @param background Background. Null to leave unchanged
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the view type is not supported
     */
    @WebMethod(operationName = "updateObjectRelatedView")
    public void updateObjectRelatedView(@WebParam(name = "objectOid")String objectOid,
            @WebParam(name = "objectClass")String objectClass, @WebParam(name = "viewId")long viewId,
            @WebParam(name = "viewName")String viewName, @WebParam(name = "viewDescription")String viewDescription,
            @WebParam(name = "structure")byte[] structure,
            @WebParam(name = "background")byte[] background, @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try {
            wsBean.updateObjectRelatedView(objectOid, objectClass, viewId, viewName, viewDescription, structure, background, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateObjectRelatedView: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Updates a general view (a view that is not linked to any particular object)
     * @param viewId View id
     * @param viewName View name. Null to leave unchanged
     * @param viewDescription View Description. Null to leave unchanged
     * @param structure View structure. Null to leave unchanged
     * @param background Background. Null to leave unchanged
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the view type is invalid or if the background could not be saved.
     *                             If the view couldn't be found
     */
    @WebMethod(operationName = "updateGeneralView")
    public void updateGeneralView(@WebParam(name = "viewId")long viewId,
            @WebParam(name = "viewName")String viewName, @WebParam(name = "viewDescription")String viewDescription,
            @WebParam(name = "structure")byte[] structure, @WebParam(name = "background")byte[] background, @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.updateGeneralView(viewId, viewName, viewDescription, structure, background, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateGeneralView: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    /**
     * Deletes views
     * @param oids Ids of the views to be deleted
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the view can't be found
     */
    @WebMethod(operationName = "deleteGeneralView")
    public void deleteGeneralView(@WebParam(name = "oids")long [] oids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.deleteGeneralView(oids, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteGeneralView: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Creates a list type item
     * @param className List type item class name
     * @param name List type item name
     * @param displayName List type item display name
     * @param sessionId Session token
     * @return the id of the new object
     * @throws ServerSideException If the class provided is not a list type
     *                             If the class is abstract or marked as in design.
     */
    @WebMethod(operationName = "createListTypeItem")
    public String createListTypeItem(
            @WebParam(name = "className") String className,
            @WebParam(name = "name") String name,
            @WebParam(name = "displayName") String displayName,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException{
        try
        {
            return wsBean.createListTypeItem(className, name, displayName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createListTypeItem: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }

    }

    /**
     * Deletes a list type item
     * @param className list type item class name
     * @param oid list type item id
     * @param releaseRelationships should the deletion process release the relationships attached to this object
     * @param sessionId Session token
     * @throws ServerSideException If the class provided is not a list type
     *                             If the user can't delete a list type item
     */
    @WebMethod(operationName = "deleteListTypeItem")
    public void deleteListTypeItem(
            @WebParam(name = "className") String className,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "releaseRelationships") boolean releaseRelationships,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException{

        try{
            wsBean.deleteListTypeItem(className, oid, releaseRelationships, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteListTypeItem: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }

    }

    /**
     * Retrieves all items for a single list type
     * @param className The list type class
     * @param sessionId Session token
     * @return a list of list type items
     * @throws ServerSideException If className is not an existing class
     *                             If the class provided is not a list type
     */
    @WebMethod(operationName = "getListTypeItems")
    public List<RemoteObjectLight> getListTypeItems(
            @WebParam(name = "className") String className,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException{
        try{
            return wsBean.getListTypeItems(className, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getListTypeItems: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }

    }

    /**
     * Retrieves all possible list types
     * @param sessionId Session token
     * @return A list of list types as ClassInfoLight instances
     * @throws ServerSideException If the GenericObjectList class does not exist
     */
    @WebMethod(operationName = "getInstanceableListTypes")
    public RemoteClassMetadataLight[] getInstanceableListTypes(
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException{
        try
        {
            return wsBean.getInstanceableListTypes(getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getInstanceableListTypes: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Executes a complex query generated using the Graphical Query Builder.  Please note
     * that the first record is reserved for the column headers, so and empty result set
     * will have at least one record.
     * @param query The TransientQuery object (a code friendly version of the graphical query designed at client side).
     * @param sessionId session id to check permissions
     * @return An array of records (the first raw is used to put the headers)
     * @throws ServerSideException If the class to be search is cannot be found
     *                             If the user is not allowed to invoke the method
     */
    @WebMethod(operationName = "executeQuery")
    public ResultRecord[] executeQuery(@WebParam(name="query")TransientQuery query,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.executeQuery(query, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in executeQuery: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Creates a query using the Graphical Query Builder
     * @param queryName Query name
     * @param ownerOid OwnerOid. Null if public
     * @param queryStructure XML document as a byte array
     * @param description a short descriptions for the query
     * @param sessionId session id to check permissions
     * @return a RemoteObjectLight wrapping the newly created query
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the owner could not be found.
     */
    @WebMethod(operationName = "createQuery")
    public long createQuery(@WebParam(name="queryName")String queryName,
            @WebParam(name="ownerOid")long ownerOid,
            @WebParam(name="queryStructure")byte[] queryStructure,
            @WebParam(name="description")String description,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.createQuery(queryName, ownerOid, queryStructure, description, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createQuery: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Save the query made in the graphical Query builder
     * @param queryOid query oid to be updated
     * @param queryName query name (the same if unchanged)
     * @param ownerOid owneroid (if unchanged)
     * @param queryStructure XML document if unchanged. Null otherwise
     * @param description Query description. Null if unchanged
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the query can not be found
     */
    @WebMethod(operationName = "saveQuery")
    public void saveQuery(@WebParam(name="queryOid")long queryOid,
            @WebParam(name = "queryName")String queryName,
            @WebParam(name = "ownerOid")long ownerOid,
            @WebParam(name = "queryStructure")byte[] queryStructure,
            @WebParam(name = "description")String description,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.saveQuery(queryOid, queryName, ownerOid, queryStructure, description, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in saveQuery: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Deletes a query
     * @param queryOid Query id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the query could not be found
     */
    @WebMethod(operationName = "deleteQuery")
    public void deleteQuery(@WebParam(name="queryOid")long queryOid,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.deleteQuery(queryOid, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteQuery: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Retrieves all saved queries
     * @param showPublic should this method return the public queries along with the private to this user?
     * @param sessionId Session token
     * @return A list with the available queries
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getQueries")
    public RemoteQueryLight[] getQueries(@WebParam(name="showPublic")boolean showPublic,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getQueries(showPublic, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getQueries: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Retrieves a saved query
     * @param queryOid Query id
     * @param sessionId Session token
     * @return The query
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the query could not be found
     */
    @WebMethod(operationName = "getQuery")
    public RemoteQuery getQuery(@WebParam(name="queryOid")long queryOid,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getQuery(queryOid, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getQuery: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Creates an XML document describing the class hierarchy
     * @param showAll should this method return all entity classes or only InventoryObject subclasses
     * @param sessionId session identifier
     * @return A byte array containing the class hierarchy as an XML document. 
     * @throws ServerSideException If one of the core classes could not be found
     */
    @WebMethod(operationName = "getClassHierarchy")
    public byte[] getClassHierarchy(@WebParam(name = "showAll")boolean showAll,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getClassHierarchy(showAll, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getClassHierarchy: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Methods related to manage pools
     */
    /**
     * Creates a pool without a parent. They're used as general purpose place to put inventory objects, or as root for particular models
     * @param name Pool name
     * @param description Pool description
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @param type Type of pool. For possible values see ApplicationManager.POOL_TYPE_XXX
     * @param sessionId The session token
     * @return The id of the new pool
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the log root node could not be found
     */
    @WebMethod(operationName = "createRootPool")
    public String createRootPool(@WebParam(name = "name")String name, 
                               @WebParam(name = "description")String description, 
                               @WebParam(name = "instancesOfClass")String instancesOfClass, 
                               @WebParam(name = "type")int type, 
                               @WebParam(name = "sessionId")String sessionId)
            throws ServerSideException {
        try {
            return wsBean.createRootPool(name, description, instancesOfClass, type, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createRootPool: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates a pool that will have as parent an inventory object. This special containment structure can be used to 
     * provide support for new models
     * @param parentClassname Class name of the parent object
     * @param parentId Id of the parent object
     * @param name Pool name
     * @param description Pool descriptionCreates a pool that will have as parent an inventory object. This special containment structure can be used to 
     * provide support for new models
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @param type Type of pool. For possible values see ApplicationManager.POOL_TYPE_XXX
     * @param sessionId The session token
     * @return The id of the new pool
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If instancesOfClass is not a valid subclass of InventoryObject
     *                             If the parent object can not be found
     */
    @WebMethod(operationName = "createPoolInObject")
    public String createPoolInObject(@WebParam(name = "parentClassname")String parentClassname, 
                                   @WebParam(name = "parentId")String parentId, 
                                   @WebParam(name = "name")String name, 
                                   @WebParam(name = "description")String description, 
                                   @WebParam(name = "instancesOfClass")String instancesOfClass, 
                                   @WebParam(name = "type")int type, 
                               @WebParam(name = "sessionId")String sessionId)
            throws ServerSideException {
        try {
            return wsBean.createPoolInObject(parentClassname, parentId, name, description, instancesOfClass, type, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createPoolInObject: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates a pool that will have as parent another pool. This special containment structure can be used to 
     * provide support for new models
     * @param parentId Id of the parent pool
     * @param name Pool name
     * @param description Pool description
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @param type Type of pool. Not used so far, but it will be in the future. It will probably be used to help organize the existing pools
     * @param sessionId The session token
     * @return The id of the new pool
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If instancesOfClass is not a valid subclass of InventoryObject
     *                             If the parent object can not be found
     */
    @WebMethod(operationName = "createPoolInPool")
    public String createPoolInPool(@WebParam(name = "parentId")String parentId, 
                                   @WebParam(name = "name")String name, 
                                   @WebParam(name = "description")String description, 
                                   @WebParam(name = "instancesOfClass")String instancesOfClass, 
                                   @WebParam(name = "type")int type, 
                               @WebParam(name = "sessionId")String sessionId)
            throws ServerSideException {
        try {
            return wsBean.createPoolInPool(parentId, name, description, instancesOfClass, type, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createPoolInPool: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates an object within a pool
     * @param poolId Id of the pool under which the object will be created
     * @param className Class this object is going to be instance of
     * @param attributeNames Attributes to be set in the new object. Null or empty array for none
     * @param attributeValues Attributes to be set in the new object (values). Null for none. The size of this array must match attributeNames size
     * @param templateId Template to be used. Use -1 to not use any template
     * @param sessionId Session identifier
     * @return The id of the newly created object
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If attributeNames and attributeValues have different sizes
     *                             If the class name could not be found 
     */
    @WebMethod(operationName = "createPoolItem")
    public String createPoolItem(@WebParam(name = "poolId")String poolId,
            @WebParam(name = "className")String className,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[] attributeValues,
            @WebParam(name = "templateId")String templateId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.createPoolItem(poolId, className, attributeNames, attributeValues, templateId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createPoolItem: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a set of pools
     * @param ids Pools to be deleted
     * @param sessionId Session identifier
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the pools to be deleted couldn't be found
     *                             If any of the objects in the pool can not be deleted because it's not a business related instance (it's more a security restriction)
     */
    @WebMethod(operationName = "deletePools")
    public void deletePools(@WebParam(name = "ids")String[] ids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.deletePools(ids, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deletePools: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the pools that don't have any parent and are normally intended to be managed by the Pool Manager
     * @param className The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use null if you want to get all
     * @param type The type of pools that should be retrieved. Root pools can be for general purpose, or as roots in models
     * @param includeSubclasses Use <code>true</code> if you want to get only the pools whose <code>className</code> property matches exactly the one provided, and <code>false</code> if you want to also include the subclasses
     * @param sessionId Session token
     * @return A set of pools
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             or in case something goes wrong
     */
    @WebMethod(operationName = "getRootPools")
    public List<RemotePool> getRootPools(@WebParam(name = "className")String className, 
                                         @WebParam(name = "type")int type, 
                                         @WebParam(name = "includeSubclasses")boolean includeSubclasses, 
                                         @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getRootPools(className, type, includeSubclasses, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getRootPools: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves all the pools that are children of a particular object.
     * @param objectClassName Object class.
     * @param objectId Object id.
     * @param poolClass Type of the pools that are to be retrieved (that is, the class of the objects contained within the pool)
     * @param sessionId Session id.
     * @return A list of children pools.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the parent object can not be found
     */
    @WebMethod(operationName = "getPoolsInObject")
    public List<RemotePool> getPoolsInObject(@WebParam(name = "objectClassName")String objectClassName, 
                                             @WebParam(name = "objectId")String objectId,
                                             @WebParam(name = "poolClass")String poolClass, 
                                             @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getPoolsInObject(objectClassName, objectId, poolClass, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getPoolsInObject: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the pools contained inside a pool.
     * @param parentPoolId Parent pool id.
     * @param poolClass Class of the objects contained by the desired pool (not the parent pool).
     * @param sessionId Session token.
     * @return A list of children pools
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the parent object can not be found
     */
    @WebMethod(operationName = "getPoolsInPool")
    public List<RemotePool> getPoolsInPool(@WebParam(name = "parentPoolId")String parentPoolId,
                                             @WebParam(name = "poolClass")String poolClass, 
                                             @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getPoolsInPool(parentPoolId, poolClass, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getPoolsInPool: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves information about a particular pool
     * @param poolId The id of the pool
     * @param sessionId The session token
     * @return The pool object
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the pool could not be found
     */
    @WebMethod(operationName = "getPool")
    public RemotePool getPool(@WebParam(name = "poolId") String poolId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException{
        try{
            return wsBean.getPool(poolId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getPool: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Updates pool name and description
     * @param poolId Pool Id
     * @param name Pool name
     * @param description Pool description
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "setPoolProperties")
    public void setPoolProperties(@WebParam(name = "poolId") String poolId, 
            @WebParam(name = "name") String name, 
            @WebParam(name = "description") String description, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.setPoolProperties(poolId, name, description, getIPAddress(), sessionId);
        } catch (Exception e) {
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in setPoolProperties: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the objects contained into a pool
     * @param poolId Parent pool id
     * @param limit limit of results. -1 to return all
     * @param sessionId Session identifier
     * @return The list of items
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the pool id provided is not valid
     */
    @WebMethod(operationName = "getPoolItems")
    public List<RemoteObjectLight> getPoolItems(@WebParam(name = "poolId")String poolId,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getPoolItems(poolId, limit, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getPoolItems: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates and schedule a task. A task is an application entity that allows to run jobs that will be executed depending on certain schedule
     * @param name Task name
     * @param description Task description
     * @param enabled Is the task enabled?
     * @param commitOnExecute Should this task commit the changes (if any) after its execution? <b>Handle with extreme care, you are basically running arbitrary code and affecting the db</b>
     * @param script The script to be executed
     * @param parameters The parameters for the script
     * @param schedule When the task should be executed
     * @param notificationType How the result of the task should be notified to the associated users 
     * @param sessionId The session token
     * @return The id of the newly created task
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task is disabled
     *                             If something goes wrong
     */
    @WebMethod(operationName = "createTask")
    public long createTask(@WebParam(name = "name")String name,
            @WebParam(name = "description")String description,
            @WebParam(name = "enabled")boolean enabled,
            @WebParam(name = "commitOnExecute")boolean commitOnExecute,
            @WebParam(name = "script")String script,
            @WebParam(name = "parameters")List<StringPair> parameters,
            @WebParam(name = "schedule")TaskScheduleDescriptor schedule,
            @WebParam(name = "notificationType")TaskNotificationDescriptor notificationType,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.createTask(name, description, enabled, commitOnExecute, script, parameters, 
                    schedule, notificationType, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createTask: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Updates any of these properties from a task: name, description, enabled and script
     * @param taskId Task id
     * @param propertyName Property name. Possible values: "name", "description", "enabled" and "script"
     * @param propertyValue The value of the property. For the property "enabled", the allowed values are "true" and "false"
     * @param sessionId The session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task could not be found
     *                             If the property name has an invalid value
     */
    @WebMethod(operationName = "updateTaskProperties")
    public void updateTaskProperties(@WebParam(name = "taskId")long taskId,
            @WebParam(name = "propertyName")String propertyName,
            @WebParam(name = "propertyValue")String propertyValue,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.updateTaskProperties(taskId, propertyName, propertyValue, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateTaskProperties: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Updates the parameters of a task. If any of the values is null, that parameter will be deleted, if the parameter does not exist, it will be created
     * @param taskId Task id
     * @param parameters The parameters to be modified as pairs paramName/paramValue. A null value means that that parameter should be deleted
     * @param sessionId The session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task could not be found
     */
    @WebMethod(operationName = "updateTaskParameters")
    public void updateTaskParameters(@WebParam(name = "taskId")long taskId,
            @WebParam(name = "parameters")List<StringPair> parameters,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.updateTaskParameters(taskId, parameters, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateTaskParameters: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Updates a task schedule
     * @param taskId Task id
     * @param schedule New schedule
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task could not be found
     */
    @WebMethod(operationName = "updateTaskSchedule")
    public void updateTaskSchedule(@WebParam(name = "taskId")long taskId,
            @WebParam(name = "schedule")TaskScheduleDescriptor schedule,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.updateTaskSchedule(taskId, schedule, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateTaskSchedule: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Updates a task notification type
     * @param taskId Task id
     * @param notificationType New notification type
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task could not be found
     */
    @WebMethod(operationName = "updateTaskNotificationType")
    public void updateTaskNotificationType(@WebParam(name = "taskId")long taskId,
            @WebParam(name = "notificationType")TaskNotificationDescriptor notificationType,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.updateTaskNotificationType(taskId, notificationType, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateTaskNotificationType: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the information about a particular task
     * @param taskId Id of the task
     * @param sessionId Session token
     * @return A remote task object representing the task
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task could not be found
     */
    @WebMethod(operationName = "getTask")
    public RemoteTask getTask(@WebParam(name = "taskId")long taskId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getTask(taskId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getTask: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets all the registered tasks
     * @param sessionId Session token
     * @return A list of task objects
     * @throws ServerSideException If the user is not allowed to invoke the method or
     *                             in case something goes wrong
     */
    @WebMethod(operationName = "getTasks")
    public List<RemoteTask> getTasks(@WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getTasks(getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getTasks: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets all the tasks related to a particular user
     * @param userId User if
     * @param sessionId Session token
     * @return A list of task objects
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the user can not be found
     */
    @WebMethod(operationName = "getTasksForUser")
    public List<RemoteTask> getTasksForUser(@WebParam(name = "userId")long userId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getTasksForUser(userId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getTasksForUser: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the users subscribed to a particular task.
     * @param taskId Task id.
     * @param sessionId Session token.
     * @return The list of subscribed users.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             Id the task could not be found
     */
    public List<UserInfoLight> getSubscribersForTask(@WebParam(name = "taskId")long taskId,
                                                     @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getSubscribersForTask(taskId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSubscribersForTask: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a task and unsubscribes all users from it
     * @param taskId Task id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task could not be found
     */
    @WebMethod(operationName = "deleteTask")
    public void deleteTask(@WebParam(name = "taskId")long taskId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.deleteTask(taskId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteTask: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Subscribes a user to a task, so it will be notified of the result of its execution
     * @param taskId Id of the task
     * @param userId Id of the user
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the user is already subscribed to the task
     */
    @WebMethod(operationName = "subscribeUserToTask")
    public void subscribeUserToTask(@WebParam(name = "userId")long userId,
            @WebParam(name = "taskId")long taskId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.subscribeUserToTask(userId, taskId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in subscribeUserToTask: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Unsubscribes a user from a task, so it will no longer be notified about the result of its execution
     * @param taskId Id of the task
     * @param userId Id of the user
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task or the user could not be found
     */
    @WebMethod(operationName = "unsubscribeUserFromTask")
    public void unsubscribeUserFromTask(@WebParam(name = "userId")long userId,
            @WebParam(name = "taskId")long taskId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.unsubscribeUserFromTask(userId, taskId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in unsubscribeUserFromTask: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Executes a task on demand.
     * @param taskId The task id
     * @param sessionId The session token
     * @return A RemoteTaskResult object wrapping the task execution messages and details.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the task doesn't have a script
     */
    @WebMethod(operationName = "executeTask")
    public RemoteTaskResult executeTask(@WebParam(name = "taskId")long taskId, 
                                        @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.executeTask(taskId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in executeTask: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates a contact
     * @param contactClass The class of the new contact. It must be a subclass of GenericContact
     * @param properties A dictionary (key-value list) with the set of string-type attributes to be set. No string-type attributes are not currently supported. Attribute <b>name</b> is mandatory.
     * @param customerClassName The class of the customer this contact will be associated to
     * @param customerId The id of the customer this contact will be associated to
     * @param sessionId The session token
     * @return The id of the newly created contact
     * @throws ServerSideException If the contact class provided is not a valid GenericCustomer, or if the customer does not exist or if any of the properties does not exist or its type is invalid (not a string)
     */
    public String createContact(
        @WebParam(name = "contactClass")String contactClass, 
        @WebParam(name = "properties")List<StringPair> properties, 
        @WebParam(name = "customerClassName")String customerClassName, 
        @WebParam(name = "customerId")String customerId, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.createContact(contactClass, properties, customerClassName, customerId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createContact: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Updates a set of properties of a contact
     * @param contactClass The class of the contact to be updated
     * @param contactId The id of the contact to be updated
     * @param properties A set of pairs key-value with the properties to be updated
     * @param sessionId The session token
     * @throws ServerSideException If the contact could not be found or if any of the attributes to be set could not be found or has an invalid value
     */
    public void updateContact(
        @WebParam(name = "contactClass")String contactClass, 
        @WebParam(name = "contactId")String contactId, 
        @WebParam(name = "properties")List<StringPair> properties, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.updateContact(contactClass, contactId, properties, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateContact: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a contact
     * @param contactClass The class of the contact to be deleted
     * @param contactId The id of the contact to be deleted
     * @param sessionId The session token
     * @throws ServerSideException If the contact was not found
     */
    public void deleteContact(
        @WebParam(name = "contactClass")String contactClass, 
        @WebParam(name = "contactId")String contactId, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.deleteContact(contactClass, contactId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteContact: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the whole information about a contact
     * @param contactClass The class of the contact
     * @param contactId The id of the contact
     * @param sessionId The session token
     * @return The contact object
     * @throws ServerSideException If the contact could not be found
     */
    public RemoteContact getContact(
        @WebParam(name = "contactClass")String contactClass, 
        @WebParam(name = "contactId")String contactId, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getContact(contactClass, contactId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getContact: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Searches for contacts given a search string, This string will be searched in the attribute values of all contacts
     * @param searchString The string to be searched. Use null or an empty string to retrieve all the contacts
     * @param maxResults Maximum number of results. Use -1 to retrieve all results at once
     * @param sessionId The session token
     * @return The list of contacts for whom at least one of their attributes matches  
     * @throws org.kuwaiba.exceptions.ServerSideException 
     */
    public List<RemoteContact> searchForContacts(@WebParam(name = "searchString")String searchString, 
            @WebParam(name = "maxResults")int maxResults, @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.searchForContacts(searchString, maxResults, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in searchForContacts: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the contacts associated to a given customer
     * @param customerClass The class of the customer to get the contacts from
     * @param customerId The id of the customer to get the contacts from
     * @param sessionId The session token
     * @return The list of contacts associated to the customer
     * @throws ServerSideException If the customer could not be found
     */
    public List<RemoteContact> getContactsForCustomer(
        @WebParam(name = "customerClass")String customerClass, 
        @WebParam(name = "customerId")String customerId, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getContactsForCustomer(customerClass, customerId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getContactsForCustomer: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="Configuration Values">
    /**
     * Creates a configuration variable inside a pool. A configuration variable is a place where a value will be stored so it can retrieved by whomever need it. 
     * These variables are typically used to store values that help other modules to work, such as URLs, user names, dimensions, etc
     * @param configVariablesPoolId The id of the pool where the config variable will be put
     * @param name The name of the pool. This value can not be null or empty. Duplicate variable names are not allowed
     * @param description The description of the what the variable does
     * @param type The type of the variable. Use 1 for number, 2 for strings, 3 for booleans, 4 for unidimensional arrays and 5 for matrixes. 
     * @param masked If the value should be masked when rendered (for security reasons, for example)
     * @param valueDefinition In most cases (primitive types like numbers, strings or booleans) will be the actual value of the variable as a string (for example "5" or "admin" or "true"). For arrays and matrixes use the following notation: <br> 
     * Arrays: (value1,value2,value3,valueN), matrixes: [(row1col1, row1col2,... row1colN), (row2col1, row2col2,... row2colN), (rowNcol1, rowNcol2,... rowNcolN)]. The values will be interpreted as strings 
     * @param sessionId The session token
     * @return The id of the newly created variable
     * @throws ServerSideException If the parent pool could not be found or if the name is empty, the type is invalid, the value definition is empty
     */
    @WebMethod(operationName = "createConfigurationVariable")
    public long createConfigurationVariable(@WebParam(name = "configVariablesPoolId")String configVariablesPoolId, @WebParam(name = "name")String name, 
            @WebParam(name = "description")String description, @WebParam(name = "type")int type, @WebParam(name = "masked")boolean masked, @WebParam(name = "valueDefinition")String valueDefinition, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.createConfigurationVariable(configVariablesPoolId, name, description, type, masked, valueDefinition, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createConfigurationVariable: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Updates the value of a configuration variable. See #{@link #createConfigurationVariable(long, java.lang.String, java.lang.String, int, boolean, java.lang.String, java.lang.String) } for value definition syntax
     * @param name The current name of the variable that will be modified
     * @param propertyToUpdate The name of the property to be updated. Possible values are: "name", "description", "type", "masked" and "value"
     * @param newValue The new value as string
     * @param sessionId The session token
     * @throws ServerSideException If the property to be updated can not be recognized or if the config variable can not be found
     */
    @WebMethod(operationName = "updateConfigurationVariable")
    public void updateConfigurationVariable(@WebParam(name = "name")String name, @WebParam(name = "propertyToUpdate")String propertyToUpdate, 
            @WebParam(name = "newValue")String newValue, @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.updateConfigurationVariable(name, propertyToUpdate, newValue, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateConfigurationVariable: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a config variable
     * @param name The name of the variable to be deleted
     * @param sessionId The session token
     * @throws ServerSideException If the config variable could not be found
     */
    @WebMethod(operationName = "deleteConfigurationVariable")
    public void deleteConfigurationVariable(@WebParam(name = "name")String name, @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.deleteConfigurationVariable(name, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteConfigurationVariable: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves a configuration variable
     * @param name The name of the variable to be retrieved
     * @param sessionId The session token
     * @return The variable
     * @throws ServerSideException If the variable could not be found
     */
    @WebMethod(operationName = "getConfigurationVariable")
    public RemoteConfigurationVariable getConfigurationVariable(@WebParam(name = "name")String name, @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getConfigurationVariable(name, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getConfigurationVariable: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the config variables in a config variable pool
     * @param poolId The id pool to retrieve the variables from
     * @param sessionId The session token
     * @return The list of config variables in the given pool
     * @throws ServerSideException If the pool could not be found
     */
    @WebMethod(operationName = "getConfigurationVariablesInPool")
    public List<RemoteConfigurationVariable> getConfigurationVariablesInPool(@WebParam(name = "poolId")String poolId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getConfigurationVariablesInPool(poolId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getConfigurationVariablesInPool: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the list of pools of config variables.
     * @param sessionId The session token.
     * @return The available pools of configuration variables.
     * @throws org.kuwaiba.exceptions.ServerSideException If an unexpected error occurred.
     */
    @WebMethod(operationName = "getConfigurationVariablesPools")
    public List<RemotePool> getConfigurationVariablesPools(@WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getConfigurationVariablesPools(getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getConfigurationVariablesPools: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates a pool of configuration variables.
     * @param name The name of the pool. Empty or null values are not allowed.
     * @param description The description of the pool.
     * @param sessionId The session token.
     * @return The id of the newly created pool.
     * @throws ServerSideException If the name provided is null or empty.
     */
    @WebMethod(operationName = "createConfigurationVariablesPool")
    public String createConfigurationVariablesPool(@WebParam(name = "name")String name, @WebParam(name = "description")String description, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.createConfigurationVariablesPool(name, description, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createConfigurationVariablesPool: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Updates an attribute of a given configuration variables pool.
     * @param poolId The id of the pool to update.
     * @param propertyToUpdate The property to update. The valid values are "name" and "description".
     * @param value The value of the property to be updated.
     * @param sessionId The session token.
     * @throws ServerSideException If the pool could not be found or If the property provided is not valid.
     */
    @WebMethod(operationName = "updateConfigurationVariablesPool")
    public void updateConfigurationVariablesPool(@WebParam(name = "poolId")String poolId, @WebParam(name = "propertyToUpdate")String propertyToUpdate, 
            @WebParam(name = "value")String value, @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.updateConfigurationVariablesPool(poolId, propertyToUpdate, value, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateConfigurationVariablesPool: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a configuration variables pool. Deleting a pool also deletes the configuration variables contained within.
     * @param poolId The id of the pool to be deleted.
     * @param sessionId The session token.
     * @throws ServerSideException If the pool could not be found.
     */
    @WebMethod(operationName = "deleteConfigurationVariablesPool")
    public void deleteConfigurationVariablesPool(@WebParam(name = "poolId")String poolId, @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.deleteConfigurationVariablesPool(poolId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteConfigurationVariablesPool: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    //</editor-fold>
    
    //<editor-fold desc="Validators" defaultstate="collapsed">
    /**
     * Creates a validator definition. 
     * @param name The name of the validator. It's recommended to use camel case notation (for example thisIsAName). This field is mandatory
     * @param description The optional description of the validator
     * @param classToBeApplied The class or super class of the classes whose instances will be checked against this validator
     * @param script The groovy script containing the logic of the validator , that is, the 
     * @param enabled If this validador should be applied or not
     * @param sessionId The session token
     * @return The id of the newly created validator definition
     * @throws ServerSideException If the name is null or empty or if the classToBeApplied argument could not be found
     */
    @WebMethod(operationName = "createValidatorDefinition")
    public long createValidatorDefinition(@WebParam(name = "name")String name, @WebParam(name = "description")String description, 
            @WebParam(name = "classToBeApplied")String classToBeApplied, @WebParam(name = "script")String script, 
            @WebParam(name = "enabled")boolean enabled, @WebParam(name = "sessionId")String sessionId) 
            throws ServerSideException {
        try {
            return wsBean.createValidatorDefinition(name, description, classToBeApplied, script, enabled, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createValidatorDefinition: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Updates the properties of a validator. The null values will be ignored
     * @param validatorDefinitionId The id of teh validator definition to be updated
     * @param name The new name, if any, null otherwise
     * @param description The new description, if any, null otherwise
     * @param classToBeApplied The new class to be associated to this validator, if any, null otherwise
     * @param script The new script, if any, null otherwise
     * @param enabled If the validator should be enabled or not, if any, null otherwise
     * @param sessionId The session token
     * @throws ServerSideException If the validator definition could not be found or if the classToBeApplied parameter is not valid or if the name is not null, but it is empty
     */
    @WebMethod(operationName = "updateValidatorDefinition")
    public void updateValidatorDefinition(@WebParam(name = "validatorDefinitionId")long validatorDefinitionId, @WebParam(name = "name")String name, 
            @WebParam(name = "description")String description, @WebParam(name = "classToBeApplied")String classToBeApplied, 
            @WebParam(name = "script")String script, @WebParam(name = "enabled")Boolean enabled, @WebParam(name = "sessionId")String sessionId) 
            throws ServerSideException {
        try {
            wsBean.updateValidatorDefinition(validatorDefinitionId, name, description, classToBeApplied, script, enabled, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateValidatorDefinition: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    /**
     * Retrieves all the validator definitions in the system
     * @param className The class to retrieve the validator definitions from.
     * @param sessionId The session token
     * @return The list of validator definitions
     * @throws ServerSideException In case of an unexpected server side error
     */
    @WebMethod(operationName = "getValidatorDefinitionsForClass")
    public List<RemoteValidatorDefinition> getValidatorDefinitionsForClass(@WebParam(name = "className")String className, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getValidatorDefinitionsForClass(className, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getValidatorDefinitions: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    /**
     * Runs the existing validations for the class associated to the given object. Validators set to enabled = false will be ignored
     * @param objectClass The class of the object
     * @param objectId The id of the object
     * @param sessionId The session token
     * @return The list of validators associated to the object and its class
     * @throws ServerSideException If the object can not be found
     */
    @WebMethod(operationName = "runValidationsForObject")
    public List<RemoteValidator> runValidationsForObject(@WebParam(name = "objectClass")String objectClass, @WebParam(name = "objectId")long objectId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.runValidationsForObject(objectClass, objectId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in runValidationsForObject: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a validator definition
     * @param validatorDefinitionId the id of the validator to be deleted
     * @param sessionId The session token
     * @throws ServerSideException If the validator definition could not be found
     */
    @WebMethod(operationName = "deleteValidatorDefinition")
    public void deleteValidatorDefinition(@WebParam(name = "validatorDefinitionId")long validatorDefinitionId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.deleteValidatorDefinition(validatorDefinitionId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteValidatorDefinition: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    //</editor-fold>
    
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Business Methods. Click on the + sign on the left to edit the code.">
    /**
     * Gets the children of a given object given his class id and object id
     * @param objectClassId object's class id
     * @param oid object's id
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array of all the direct children of the provided object according with the current container hierarchy
     * @throws ServerSideException If the class could not be found
     */
    @WebMethod(operationName = "getObjectChildrenForClassWithId")
    public List<RemoteObjectLight> getObjectChildrenForClassWithId(@WebParam(name = "oid") String oid,
            @WebParam(name = "objectClassId") long objectClassId,
            @WebParam(name = "maxResults") int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            List<RemoteObjectLight> res = wsBean.getObjectChildren(oid,objectClassId, maxResults, getIPAddress(), sessionId);
            return res;
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getObjectChildrenForClassWithId: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

     /**
     * Gets the children of a given object given his class name and object id
     * @param oid Object's oid
     * @param objectClassName object's class name
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array of all the direct children of the provided object according with the current container hierarchy
     * @throws ServerSideException If the class could not be found.
     */
    @WebMethod(operationName = "getObjectChildren")
    public List<RemoteObjectLight> getObjectChildren(@WebParam(name = "objectClassName") String objectClassName,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "maxResults") int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            List<RemoteObjectLight> res = wsBean.getObjectChildren(objectClassName, oid, maxResults, getIPAddress(), sessionId);
            return res;
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getObjectChildren: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Returns the siblings of an object in the containment hierarchy
     * @param objectClassName Object class
     * @param oid Object oid
     * @param maxResults Max number of results to be returned
     * @param sessionId Session token
     * @return List of siblings
     * @throws ServerSideException If the class does not exist
     *                             If the object does not exist
     */
    @WebMethod(operationName = "getSiblings")
    public List<RemoteObjectLight> getSiblings(@WebParam(name = "objectClassName") String objectClassName,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "maxResults") int  maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            List<RemoteObjectLight> res = wsBean.getSiblings(objectClassName, oid, maxResults, getIPAddress(), sessionId);
            return res;
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSiblings: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Gets all children of an object of a given class
     * @param parentOid Parent whose children are requested
     * @param parentClass Class name of the element we want the children from
     * @param childrenClass The type of children we want to retrieve
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array with the children objects
     * @throws ServerSideException If any of the classes can not be found
     *                             If parent object can not be found
     *                             If the database objects can not be correctly mapped into serializable Java objects.
     */
    @WebMethod(operationName="getChildrenOfClass")
    public List<RemoteObject> getChildrenOfClass(@WebParam(name="parentOid")String parentOid,
            @WebParam(name="parentClass")String parentClass,
            @WebParam(name="childrenClass")String childrenClass,
            @WebParam(name="maxResults")int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try {
            List<RemoteObject> res = wsBean.getChildrenOfClass(parentOid,parentClass,childrenClass, maxResults, getIPAddress(), sessionId);
            return res;
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getChildrenOfClass: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets recursively all children of an object of a given class
     * @param parentOid Parent whose children are requested
     * @param parentClass Class name of the element we want the children from
     * @param childrenClass The type of children we want to retrieve
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array with the children objects
     * @throws ServerSideException If any of the classes can not be found
     *                             If parent object can not be found
     *                             If the database objects can not be correctly mapped into serializable Java objects.
     */
    @WebMethod(operationName="getChildrenOfClassLightRecursive")
    public List<RemoteObjectLight> getChildrenOfClassLightRecursive(
        @WebParam(name="parentOid") String parentOid,
        @WebParam(name="parentClass") String parentClass,
        @WebParam(name="childrenClass") String childrenClass,
        @WebParam(name="maxResults") int maxResults,
        @WebParam(name="sessionId") String sessionId) throws ServerSideException {
        try{
            List<RemoteObjectLight> res = wsBean.getChildrenOfClassLightRecursive(parentOid, parentClass, childrenClass, maxResults, getIPAddress(), sessionId);
            return res;
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getChildrenOfClassLightRecursive: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

     /**
     * Gets all children of an object of a given class
     * @param parentOid Object oid whose children will be returned
     * @param parentClass
     * @param childrenClass
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array with children
     * @throws ServerSideException If any of the classes can not be found
     *                             If parent object can not be found
     */
    @WebMethod(operationName="getChildrenOfClassLight")
    public List<RemoteObjectLight> getChildrenOfClassLight(@WebParam(name="parentOid")String parentOid,
            @WebParam(name="parentClass")String parentClass,
            @WebParam(name="childrenClass")String childrenClass,
            @WebParam(name="maxResults")int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getChildrenOfClassLight(parentOid,parentClass,childrenClass,maxResults, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getChildrenOfClassLight: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Returns the special children of a given object as RemoteObjectLight instances. This method is not recursive.
     * @param parentOid The id of the parent object
     * @param parentClass The class name of the parent object
     * @param classToFilter The superclass/class to be used to filter the results. You can also use abstract superclasses.
     * @param maxResults The max number of results to fetch. Use -1 to retrieve all
     * @param sessionId The session token
     * @return The list of special children of the given object, filtered using classToFilter
     * @throws ServerSideException If the parent class name provided could not be found or if the parent object could not be found
     */
    @WebMethod(operationName="getSpecialChildrenOfClassLight")
    public List<RemoteObjectLight> getSpecialChildrenOfClassLight(@WebParam(name="parentOid")String parentOid,
            @WebParam(name="parentClass")String parentClass,
            @WebParam(name="classToFilter")String classToFilter,
            @WebParam(name="maxResults")int maxResults,
            @WebParam(name ="sessionId")String sessionId) throws ServerSideException {
        try{
            return wsBean.getSpecialChildrenOfClassLight(parentOid, parentClass, classToFilter, maxResults, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSpecialChildrenOfClassLight: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
      * Gets the complete information about a given object (all its attributes)
      * @param objectClass Object class
      * @param oid Object id
      * @param sessionId Session token
      * @return a representation of the entity as a RemoteObject. The list of attribute values is a hashmap of strings (should be mapped to 
      * actual types by the consumer of the service). Single list types are represented by the id of the list type item (a numeric value), 
      * while multiple list types are strings wit the ids of the related list type items separated by semicolons (e.g. 123;786576;92332)
      * @throws ServerSideException If the className class could not be found
      *                             If the requested object could not be found
      *                             If the object id could not be found
      */
    @WebMethod(operationName = "getObject")
    public RemoteObject getObject(@WebParam(name = "objectClass") String objectClass,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{

        try {
            return wsBean.getObject(objectClass, oid, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getObject: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Gets the basic information about a given object (oid, classname, name)
     * @param objectClass Object class name
     * @param oid Object oid
     * @param sessionId Session token
     * @return a representation of the entity as a RemoteObjectLight
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectLight")
    public RemoteObjectLight getObjectLight(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getObjectLight(objectClass, oid, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getObjectLight: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves a list of light instances of a given class given a simple filter. This method will search for all objects with a string-based attribute (filterName) whose value matches a value provided (filterValue)
     * @param className The class of the objects to be searched. This method support abstract superclasses as well
     * @param filterName The attribute to be used as filter. This will work <b>only</b> with String-type attributes. Example: serialNumber
     * @param filterValue The value to be use to match the instances. Example "Serial-12345"
     * @param sessionId The session token
     * @return The list of instances that matches the filterName/filterValue criteria
     * @throws ServerSideException if the class provided could not be found
     */    
    @WebMethod(operationName = "getObjectsWithFilterLight")
    public List<RemoteObjectLight> getObjectsWithFilterLight(@WebParam(name = "className") String className,
            @WebParam(name = "filterName") String filterName,
            @WebParam(name = "filterValue") String filterValue,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getObjectsWithFilterLight(className, filterName, filterValue, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getObjectsWithFilterLight: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    
    /**
     * Same as {@link #getObjectsWithFilterLight(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}, but returns RemoteObjects instead of RemoteObjectLights
     * @param className The class of the objects to be searched. This method support abstract superclasses as well
     * @param filterName The attribute to be used as filter. This will work <b>only</b> with String-type attributes. Example: serialNumber
     * @param filterValue The value to be use to match the instances. Example "Serial-12345"
     * @param sessionId The session token
     * @return The list of instances that matches the filterName/filterValue criteria
     * @throws ServerSideException if the class provided could not be found
     */   
    @WebMethod(operationName = "getObjectsWithFilter")
    public List<RemoteObject> getObjectsWithFilter(@WebParam(name = "className") String className,
            @WebParam(name = "filterName") String filterName,
            @WebParam(name = "filterValue") String filterValue,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getObjectsWithFilter(className, filterName, filterValue, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getObjectsWithFilter: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets all objects of a given class
     * @param className Class name
     * @param maxResults Max number of results. Use -1 to retrieve all
     * @param sessionId Session token
     * @return A list of instances of @className
     * @throws ServerSideException If the class can not be found
     *                             If the class is not subclass of InventoryObject
     */
    @WebMethod(operationName = "getObjectsOfClassLight")
    public List<RemoteObjectLight> getObjectsOfClassLight(@WebParam(name = "className") String className,
            @WebParam(name = "maxResults")int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getObjectsOfClassLight(className, maxResults, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getObjectsOfClassLight: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the common parent between an a object and b object
     * @param aObjectClass Object a class name
     * @param aOid Object a id
     * @param bObjectClass Object b class name
     * @param bOid Object a id
     * @param sessionId Session Id token
     * @return The common parent
     * @throws ServerSideException If the requested object can't be found
     *                             If any of the class nodes involved is malformed
     *                             If the database object could not be properly mapped into a serializable java object.
     */
    @WebMethod(operationName = "getCommonParent")
    public RemoteObjectLight getCommonParent(
        @WebParam(name = "aObjectClass") String aObjectClass, 
        @WebParam(name = "aOid") String aOid, 
        @WebParam(name = "bObjectClass") String bObjectClass, 
        @WebParam(name = "bOid") String bOid, 
        @WebParam(name = "sessionId") String sessionId) 
        throws ServerSideException {
        try {
            return wsBean.getCommonParent(aObjectClass, aOid, bObjectClass, bOid, getIPAddress(), sessionId);
        } catch (Exception e) {
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getCommonParent: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the parent of a given object in the standard and special containment hierarchy
     * @param objectClass Object class of child
     * @param oid Object id for the child
     * @param sessionId Session id
     * @return The parent object
     * @throws ServerSideException If the requested object can't be found
     *                             If any of the class nodes involved is malformed
     *                             If the database object could not be properly 
     *                            mapped into a serializable java object.
     */
    @WebMethod(operationName = "getParent")
    public RemoteObjectLight getParent(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getParent(objectClass, oid, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getParent: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves all the ancestors of an object in the containment hierarchy. If the provided object is in a pool, the ancestor pools will be returned.
     * @param objectClass Object class
     * @param oid Object id.
     * @param sessionId Session token.
     * @return The list of ancestors.
     * @throws ServerSideException In case something goes wrong.
     */
    @WebMethod(operationName = "getParents")
    public List<RemoteObjectLight> getParents(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getParents(objectClass, oid, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getParents: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Returns all the special relationships of a given object as a hashmap whose keys are
     * the names of the relationships and the values the list of related objects
     * @param objectClass Object class
     * @param oid Object id
     * @param sessionId Session token
     * @return An object comprising the list of special relationships of the given object and the other end of such relationship
     * @throws ServerSideException If case something goes wrong
     */
    @WebMethod(operationName = "getSpecialAttributes")
    public RemoteObjectSpecialRelationships getSpecialAttributes(@WebParam(name = "objectClass") String objectClass,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getSpecialAttributes(objectClass, oid, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSpecialAttributes: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the list of parents (according to the special and standard containment hierarchy) until it finds an instance of class 
     * objectToMatchClassName (for example "give me the parents of this port until you find the nearest rack")
     * @param objectClass Class of the object to get the parents from
     * @param oid Id of the object to get the parents from
     * @param objectToMatchClassName Class of the object that will limit the search. It can be a superclass, if you want to match many classes at once
     * @param sessionId Session token
     * @return The list of parents until an instance of objectToMatchClassName is found. If no instance of that class is found, all parents until the Dummy Root will be returned
     * @throws ServerSideException If the object to evaluate can not be found or if any of the classes provided could not be found.
     */
    @WebMethod(operationName = "getParentsUntilFirstOfClass")
    public List<RemoteObjectLight> getParentsUntilFirstOfClass(
        @WebParam(name = "objectClass") String objectClass, 
        @WebParam(name = "oid") String oid, 
        @WebParam(name = "objectToMatchClassName") String objectToMatchClassName, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try{
            return wsBean.getParentsUntilFirstOfClass(objectClass, oid, objectToMatchClassName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getParentsUntilFirstOfClass: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Gets the first occurrence of a parent with a given class (according to the special and standard containment hierarchy)
     * (for example "give me the parent of this port until you find the nearest rack")
     * @param objectClass Class of the object to get the parent from
     * @param oid Id of the object to get the parent from
     * @param objectToMatchClassName Class of the object that will limit the search. It can be a superclass, if you want to match many classes at once
     * @param sessionId The session id token
     * @return The the first occurrence of a parent with a given class. If no instance of that class is found, the child of Dummy Root related in this hierarchy will be returned
     * @throws ServerSideException If the object to evaluate can not be found
                                   If any of the classes provided could not be found
                                   If the object provided is not in the standard containment hierarchy
     */
    @WebMethod(operationName = "getFirstParentOfClass")
    public RemoteObjectLight getFirstParentOfClass(
            @WebParam(name = "objectClass") String objectClass, 
            @WebParam(name = "oid") String oid, 
            @WebParam(name = "objectToMatchClassName") String objectToMatchClassName, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try{
            return wsBean.getFirstParentOfClass(objectClass, oid, objectToMatchClassName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getParentsUntilFirstOfClass: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the first parent of an object which matches the given class in the containment hierarchy
     * @param objectClass Object class
     * @param oid Object oid
     * @param parentClass Class to be matched
     * @param sessionId sssion Id
     * @return The direct parent of the provided object.
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getParentOfClass")
    public RemoteObject getParentOfClass(@WebParam(name = "objectClass") String objectClass,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "parentClass") String parentClass,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getParentOfClass(objectClass, oid, parentClass, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getParentOfClass: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Gets the value of a special attribute, this is, those related to a model, such as cables connected to ports
     * @param objectClass Object's class
     * @param oid object oid
     * @param attributeName attribute's name
     * @param sessionId Session token
     * @return A list of the values related to the given object through attributeName.
     * Note that this is a <strong>string</strong> array on purpose, so the values used not necessarily are not longs
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getSpecialAttribute")
    public List<RemoteObjectLight> getSpecialAttribute(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") String oid,
            @WebParam(name = "attributename") String attributeName,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getSpecialAttribute(objectClass, oid, attributeName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSpecialAttribute: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the special children of a given object. This relationship depends on the model. The relationship between a container and the links in the physical layer model is an example of this kind of relationships.
     * @param objectClass The class of the object to be searched.
     * @param objectId The id of the object to be searched.
     * @param sessionId Session token.
     * @return A list of special children.
     * @throws ServerSideException If the class could not be found
     *                             If the object could not be found
     */
    @WebMethod(operationName = "getObjectSpecialChildren")
    public List<RemoteObjectLight> getObjectSpecialChildren (@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "objectId") String objectId,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try{
            return wsBean.getObjectSpecialChildren(objectClass, objectId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getObjectSpecialChildren: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Updates attributes of a given object
     * @param className object's class name
     * @param id Object id
     * @param attributes A dictionary with pairs key-value, being <b>key</b>, the attribute name, and <b>value</b>, 
     * the serialized version of the attribute value. Single list types are represented by the id of the list type item (a numeric value), 
     * while multiple list types are strings wit the ids of the related list type items separated by semicolons (e.g. 123;786576;92332)
     * @param sessionId Session token
     * @throws ServerSideException If the object class can't be found
     *                             If the object can't be found
     *                             If the update can't be performed due a business rule or because the object is blocked
     *                             If any of the names provided does not exist or can't be set using this method or of the value of any of the attributes can not be mapped correctly.
     */
    @WebMethod(operationName = "updateObject")
    public void updateObject(@WebParam(name = "className")String className,
            @WebParam(name = "id")String id,
            @WebParam(name = "attributes")List<StringPair> attributes,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.updateObject(className, id, attributes, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateObject: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Creates a business object
     * @param className New object class name
     * @param parentObjectClassName New object parent's class name
     * @param parentOid New object parent's id
     * @param attributeNames Names of the attributes to be set at creation time
     * @param attributeValues Values for those attributes
     * @param templateId Template id. Use -1 to not use any template
     * @param sessionId Session token
     * @return the id of the new object
     * @throws ServerSideException If the object's class can't be found
     *                             If the parent id is not found
     *                             If there's a business constraint that doesn't allow to create the object.
     *                             If any of the attribute values has an invalid value or format.
     *                             If the specified template could not be found
     */
    @WebMethod(operationName = "createObject")
    public String createObject(@WebParam(name = "className")String className,
            @WebParam(name = "parentObjectClassName")String parentObjectClassName,
            @WebParam(name = "parentOid")String parentOid,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[] attributeValues,
            @WebParam(name = "templateId")String templateId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.createObject(className,parentObjectClassName, parentOid,attributeNames, attributeValues, templateId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createObject: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates a special business object. It's a generic method to create objects proper to
     * special models. Parent object won't be linked to the new object through a conventional 
     * containment relationship
     * @param className New object class name
     * @param parentObjectClassName New object parent's class name
     * @param parentOid New object parent's id
     * @param attributeNames Names of the attributes to be set at creation time
     * @param attributeValues Values for those attributes
     * @param templateId Template id. Use -1 to not use any template
     * @param sessionId Session token
     * @return the id of the new object
     * @throws ServerSideException If the object's class can't be found
     *                             If the parent id is not found
     *                             If the update can't be performed due to a format issue
     *                             If any of the attribute values has an invalid value or format.
     *                             If the specified template could not be found.
     */
    @WebMethod(operationName = "createSpecialObject")
    public String createSpecialObject(@WebParam(name = "className")String className,
            @WebParam(name = "parentObjectClassName")String parentObjectClassName,
            @WebParam(name = "parentOid")String parentOid,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[] attributeValues,
            @WebParam(name = "templateId")String templateId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.createSpecialObject(className,parentObjectClassName, parentOid,attributeNames, attributeValues, templateId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createSpecialObject: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a set of objects. Note that this method must be used only for business objects (not metadata or application ones)
     * @param className Objects class names
     * @param oid object id from the objects to be deleted
     * @param releaseRelationships Should the deletion be forced, deleting all the relationships?
     * @param sessionId Session token
     * @throws ServerSideException If the object couldn't be found
     *                             If the class could not be found
     *                             If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     */
    @WebMethod(operationName = "deleteObject")
    public void deleteObject(@WebParam(name = "className")String className,
            @WebParam(name = "oid")String oid,
            @WebParam(name = "releaseRelationships") boolean releaseRelationships,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.deleteObject(className,oid, releaseRelationships, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteObjects: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Deletes a set of objects. Note that this method must be used only for business objects (not metadata or application ones)
     * @param classNames Objects class names
     * @param oids object id from the objects to be deleted
     * @param releaseRelationships Should the deletion be forced, deleting all the relationships?
     * @param sessionId Session token
     * @throws ServerSideException If the requested object can't be found
     *                             If the requested object class can't be found
     *                             If the update can't be performed due a business rule or because the object is blocked or it has relationships and releaseRelationships is false
     *                             If it was not possible to release the possible unique attributes
     */
    @WebMethod(operationName = "deleteObjects")
    public void deleteObjects(@WebParam(name = "classNames")String[] classNames,
            @WebParam(name = "oid")String[] oids,
            @WebParam(name = "releaseRelationships") boolean releaseRelationships,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.deleteObjects(classNames,oids, releaseRelationships, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteObjects: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
     /**
     * Moves objects from their current parent to a pool target object.
     * @param  targetClass New parent object id
     * @param targetOid The new parent's oid
     * @param objectClasses Class names of the objects to be moved
     * @param objectOids Oids of the objects to be moved
     * @param sessionId Session token
     * @throws ServerSideException If the object's or new parent's class can't be found
     *                             If the object or its new parent can't be found
     *                             If the update can't be performed due to a business rule
     */
    @WebMethod(operationName = "moveObjectsToPool")
    public void moveObjectsToPool(@WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")String targetOid,
            @WebParam(name = "objectsClasses")String[] objectClasses,
            @WebParam(name = "objectsOids")String[] objectOids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.moveObjectsToPool(targetClass,targetOid, objectClasses, objectOids, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in moveObjectsToPool: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Moves objects from their current parent to a target object.
     * @param  targetClass New parent object id
     * @param targetOid The new parent's oid
     * @param objectClasses Class names of the objects to be moved
     * @param objectOids Oids of the objects to be moved
     * @param sessionId Session token
     * @throws ServerSideException If the object's or new parent's class can't be found
     *                             If the object or its new parent can't be found
     *                             If the update can't be performed due to a business rule
     */
    @WebMethod(operationName = "moveObjects")
    public void moveObjects(@WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")String targetOid,
            @WebParam(name = "objectsClasses")String[] objectClasses,
            @WebParam(name = "objectsOids")String[] objectOids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.moveObjects(targetClass,targetOid, objectClasses, objectOids, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in moveObjects: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
     /**
     * Moves special objects from their current parent to a target object.
     * @param  targetClass New parent object id
     * @param targetOid The new parent's oid
     * @param objectClasses Class names of the objects to be moved
     * @param objectOids Oids of the objects to be moved
     * @param sessionId Session token
     * @throws ServerSideException If the object's or new parent's class can't be found
     *                             If the object or its new parent can't be found
     *                             If the update can't be performed due to a business rule
     */
    @WebMethod(operationName = "moveSpecialObjects")
    public void moveSpecialObjects(@WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")String targetOid,
            @WebParam(name = "objectsClasses")String[] objectClasses,
            @WebParam(name = "objectsOids")String[] objectOids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.moveSpecialObjects(targetClass,targetOid, objectClasses, objectOids, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in moveSpecialObjects: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Move a pool item from a pool to another pool
     * @param poolId The id of the pool node
     * @param poolItemClassName The class name for the pool item
     * @param poolItemId The id for the pool item
     * @param sessionId Session token
     * @throws ServerSideException If the pool node can not be found
     *                             If the pool item can not be move to the selected pool
     *                             If the pool item can not be found
     *                             If the pool item class name can no be found
     */
    @WebMethod(operationName = "movePoolItemToPool")
    public void movePoolItemToPool(
        @WebParam(name = "poolId") String poolId, 
        @WebParam(name = "poolItemClassName") String poolItemClassName, 
        @WebParam(name = "poolItemId") String poolItemId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try{
            wsBean.movePoolItem(poolId, poolItemClassName, poolItemId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in movePoolItemToPool: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }        
    }

     /**
     * Copy objects from its current parent to a target. This is <b>not</b> a deep copy. Only the selected object will be copied, not the children
     * @param targetClass  The new parent class name
     * @param targetOid The new parent oid
     * @param objectClasses Class names of the objects to be copied
     * @param objectOids Oids of the objects to be copied
     * @param recursive should the objects be copied recursively? (themselves plus their children)
     * @param sessionId Session token
     * @return An array with the ids of the new objects
     * @throws ServerSideException If any of the provided classes couldn't be found
     *                             If any of the template objects couldn't be found
     *                             If the target parent can't contain any of the new instances
     */
    @WebMethod(operationName = "copyObjects")
    public String[] copyObjects(
            @WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")String targetOid,
            @WebParam(name = "templateClases")String[] objectClasses,
            @WebParam(name = "templateOids")String[] objectOids,
            @WebParam(name = "recursive")boolean recursive,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.copyObjects(targetClass,targetOid, objectClasses, objectOids, recursive, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in copyObjects: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
     /**
     * Copy special objects from its current parent to a target. 
     * This is <b>not</b> a deep copy. Only the selected object will be copied, not the children
     * @param targetClass  The new parent class name
     * @param targetOid The new parent oid
     * @param objectClasses Class names of the objects to be copied
     * @param objectOids Oids of the objects to be copied
     * @param recursive should the objects be copied recursively? (themselves plus their children)
     * @param sessionId Session token
     * @return An array with the ids of the new objects
     * @throws ServerSideException If any of the provided classes couldn't be found
     *                             If any of the template objects couldn't be found
     *                             If the target parent can't contain any of the new instances
     */
    @WebMethod(operationName = "copySpecialObjects")
    public String[] copySpecialObjects(
            @WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")String targetOid,
            @WebParam(name = "templateClases")String[] objectClasses,
            @WebParam(name = "templateOids")String[] objectOids,
            @WebParam(name = "recursive")boolean recursive,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.copySpecialObjects(targetClass,targetOid, objectClasses, objectOids, recursive, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in copySpecialObjects: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Copy a pool item from a pool to another pool
     * @param poolId The id of the pool node
     * @param poolItemClassName The class name for the pool item
     * @param poolItemId The id for the pool item
     * @param recursive If this operation should also copy the children objects recursively
     * @param sessionId Session token
     * @throws ServerSideException If the pool node can not be found
     *                             If the pool item can not be move to the selected pool
     *                             If the pool item can not be found
     *                             If the pool item class name can no be found
     */
    @WebMethod(operationName = "copyPoolItemToPool")
    public void copyPoolItemToPool(
        @WebParam(name = "poolId") String poolId, 
        @WebParam(name = "poolItemClassName") String poolItemClassName, 
        @WebParam(name = "poolItemId") String poolItemId, 
        @WebParam(name = "recursive") boolean recursive,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try{
            wsBean.copyPoolItem(poolId, poolItemClassName, poolItemId, recursive, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in copyPoolItemToPool: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }        
    }
    
    /**
     * Retrieves the mandatory attributes for a given class
     * @param className The class name
     * @param sessionId Session token
     * @return The list of mandatory attributes in the given class
     * @throws ServerSideException If the class doesn't exist
     */
    @WebMethod(operationName = "getMandatoryAttributesInClass")
    public List<RemoteAttributeMetadata> getMandatoryAttributesInClass(
            @WebParam(name = "className") String className,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException{
        try{
            return wsBean.getMandatoryAttributesInClass(className, getIPAddress(), sessionId);
        }catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getMandatoryAttributesInClass: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates multiple objects using a given name pattern
     * @param className The class name for the new objects
     * @param parentClassName The parent class name for the new objects
     * @param parentOid The object id of the parent
     * @param numberOfObjects Number of objects to be created
     * @param namePattern A pattern to create the names for the new objects
     * @param sessionId Session id token
     * @return A list of ids for the new objects
     * @throws ServerSideException If the className or the parentClassName can not be found.
     *                             If the className is not a possible children of parentClassName.
     *                             If the className is not in design or are abstract.
     *                             If the className is not an InventoryObject.
     *                             If the parent node can not be found.
     *                             If the given name pattern not match with the regular expression to build the new object name.
     */
    @WebMethod(operationName = "createBulkObjects")
    public String[] createBulkObjects(
        @WebParam(name = "className") String className, 
        @WebParam(name = "parentClassName") String parentClassName, 
        @WebParam(name = "parentOid") String parentOid, 
        @WebParam(name = "numberOfObjects") int numberOfObjects, 
        @WebParam(name = "namePattern") String namePattern, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try{
            return wsBean.createBulkObjects(className, parentClassName, parentOid, numberOfObjects, namePattern, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createBulkObjects: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates multiple special objects using a given naming pattern
     * @param className The class name for the new special objects
     * @param parentClassName The parent class name for the new special objects
     * @param parentId The object id of the parent
     * @param numberOfSpecialObjects Number of special objects to be created
     * @param namePattern A pattern to create the names for the new special objects
     * @param sessionId Session id token
     * @return A list of ids for the new special objects
     * @throws ServerSideException If the className or the parentClassName can not be found.
     *                             If the parent node can not be found.
     *                             If the given name pattern not match with the regular expression to build the new object name.
     *                             If the className is not a possible special children of parentClassName.
     *                             If the className is not in design or are abstract.
     *                             If the className is not an InventoryObject.
     */
    @WebMethod(operationName = "createBulkSpecialObjects")
    public String[] createBulkSpecialObjects(
        @WebParam(name = "className") String className, 
        @WebParam(name = "parentClassName") String parentClassName, 
        @WebParam(name = "parentId") String parentId, 
        @WebParam(name = "numberOfSpecialObjects") int numberOfSpecialObjects, 
        @WebParam(name = "namePattern") String namePattern, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try{
            return wsBean.createBulkSpecialObjects(className, parentClassName, parentId, numberOfSpecialObjects, namePattern, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createBulkSpecialObjects: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    /**
     * Models
     */

    //Physical and Logical connections
    /**
     * Connect two ports using a mirror relationship
     * @param aObjectClass Port A class
     * @param aObjectId Port A id
     * @param bObjectClass Port B class
     * @param bObjectId Port B id
     * @param sessionId Session token
     * @throws ServerSideException If any of the objects can't be found
     *                             If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     *                             If any of the classes provided can not be found
     */
    @WebMethod(operationName = "connectMirrorPort")
    public void connectMirrorPort(
            @WebParam(name = "aObjectClass")String[] aObjectClass,
            @WebParam(name = "aObjectId")String[] aObjectId,
            @WebParam(name = "bObjectClass")String[] bObjectClass,
            @WebParam(name = "bObjectId")String[] bObjectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.connectMirrorPort(aObjectClass, aObjectId, bObjectClass, bObjectId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in connectMirrorPort: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Releases a port mirroring relationship between two ports, receiving one of the ports as parameter
     * @param objectClass Object class
     * @param objectId Object id
     * @param sessionId Session token
     * @throws ServerSideException If the object can not be found
     *                             If the class can not be found
     */
    @WebMethod(operationName = "releaseMirrorPort")
    public void releaseMirrorPort(
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")String objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.releaseMirrorPort(objectClass, objectId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in releaseMirrorPort: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates a physical connection (a container or a link). The validations are made at server side (this is,
     * if the connection can be established between the two endpoints, if they're not already connected, etc)
     * @param aObjectClass "a" endpoint object class
     * @param aObjectId "a" endpoint object id
     * @param bObjectClass "b" endpoint object class
     * @param bObjectId "b" endpoint object id
     * @param parentClass Parent object class
     * @param parentId Parent object id
     * @param name COnnection name. Leave empty if you want to use the one in the template
     * @param connectionClass Class used to create the connection. See Constants class for supported values
     * @param templateId Id of the template for class connectionClass. Use -1 if you want to create a connection without template
     * @param sessionId Session token
     * @return The new connection id
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object's class can't be found
     *                             If the parent id is not found
     *                             If the update can't be performed due to a format issue
     *                             If any of the attribute values has an invalid value or format.
     *                             If the specified template could not be found.
     *                             If any of the objects can't be found
     *                             If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     *                             If any of the classes provided can not be found
     */
    @WebMethod(operationName = "createPhysicalConnection")
    public String createPhysicalConnection(
            @WebParam(name = "aObjectClass")String aObjectClass,
            @WebParam(name = "aObjectId")String aObjectId,
            @WebParam(name = "bObjectClass")String bObjectClass,
            @WebParam(name = "bObjectId")String bObjectId,
            @WebParam(name = "name")String name,
            @WebParam(name = "connectionClass") String connectionClass,
            @WebParam(name = "templateId") String templateId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.createPhysicalConnection(aObjectClass, aObjectId,bObjectClass, bObjectId,
                   name, connectionClass, templateId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createPhysicalConnection: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Returns the endpoints of a physical connection
     * @param connectionClass Connection class
     * @param connectionId Connection id
     * @param sessionId Session token
     * @return An array of two positions: the first is the A endpoint and the second is the B endpoint
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object can not be found
     *                             If either the object class or the attribute can not be found
     */
    @WebMethod(operationName = "getPhysicalConnectionEndpoints")
    public RemoteObjectLight[] getPhysicalConnectionEndpoints(@WebParam(name = "connectionClass")String connectionClass, 
            @WebParam(name = "connectionId")String connectionId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getPhysicalConnectionEndpoints(connectionClass, connectionId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getPhysicalConnectionEndpoints: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Returns the structure of a logical connection. The current implementation is quite simple and the return object 
     * simply provides the endpoints and the next ports connected to such endpoints using a physical connection
     * @param linkClass The class of the connection to be evaluated
     * @param linkId The id of the connection to be evaluated
     * @param sessionId Session token
     * @return An object with the details of the connection and the physical resources associated to it
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the provided connection could not be found
     */
    @Deprecated
    @WebMethod(operationName = "getLogicalLinkDetails")
    public RemoteLogicalConnectionDetails getLogicalLinkDetails(@WebParam(name = "linkClass")String linkClass, 
                                        @WebParam(name = "linkId")String linkId,
                                        @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getLogicalLinkDetails(linkClass, linkId, getIPAddress(), sessionId);
        } catch(Exception e) {
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getLogicalLinkDetails: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
   
    /**
     * Validates a saved structure of a end to end view. The current implementation
     * provides the endpoints and the next ports connected to such endpoints using a physical connection
     * also adds continuity if a VLAN or a BridgeDomain is found
     * @param linkClasses The class of the connection to be evaluated
     * @param linkIds The id of the connection to be evaluated
     * @param savedView a given saved view to validate
     * @param sessionId Session token
     * @return An object with the details of the connection and the physical resources associated to it
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the provided connection could not be found
     */
    @WebMethod(operationName = "validateSavedE2EView")
    public RemoteViewObject validateSavedE2EView( 
            @WebParam(name = "linkClasses")List<String> linkClasses,
            @WebParam(name = "linkIds")List<String> linkIds,
            @WebParam(name = "savedView") RemoteViewObject savedView,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.validateSavedE2EView(linkClasses, linkIds, savedView, getIPAddress(), sessionId);
        } catch(Exception e) {
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in validateSavedE2EView: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Returns the structure of a logical connection. The current implementation is quite simple and the return object 
     * simply provides the endpoints and the next ports connected to such endpoints using a physical connection
     * @param linkClasses The class of the connection to be evaluated
     * @param linkIds The id of the connection to be evaluated
     * @param includeVLANs true to include the bridge domains continuity
     * @param includeBDIs true to include the bridge domains continuity
     * @param sessionId Session token
     * @return An object with the details of the connection and the physical resources associated to it
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the provided connection could not be found
     */
    @WebMethod(operationName = "getE2EView")
    public RemoteViewObject getE2View( 
            @WebParam(name = "linkClasses")List<String> linkClasses,
            @WebParam(name = "linkIds")List<String> linkIds,
            @WebParam(name = "includeVLANs")boolean includeVLANs,
            @WebParam(name = "includeBDIs")boolean includeBDIs,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getE2EMap(linkClasses, linkIds, true, true, true, includeVLANs, includeBDIs, getIPAddress(), sessionId);
        } catch(Exception e) {
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getE2EView: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
   
    /**
     * Retrieves the existing containers between two given nodes. 
     * @param objectAClass The class of the object A.
     * @param objectAId The id of the object A.
     * @param objectBClass The class of the object B. (end point B class)
     * @param objectBId The id of the object B (end point B id).
     * @param containerClass The class of the containers to be return.
     * @param sessionId Session token
     * @return A list with the common wire containers between the two objects
     * @throws ServerSideException if an objects doesn't exist or if a given class doesn't exist

     */
    @WebMethod(operationName = "getContainersBetweenObjects")
    public List<RemoteObjectLight> getContainersBetweenObjects(@WebParam(name = "objectAClass")String objectAClass, 
            @WebParam(name = "objectAId")String objectAId, 
            @WebParam(name = "objectBClass")String objectBClass, 
            @WebParam(name = "objectBId")String objectBId, 
            @WebParam(name = "containerClass")String containerClass, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getContainersBetweenObjects(objectAClass, objectAId, objectBClass, objectBId, containerClass, getIPAddress(), sessionId);
        } catch(Exception e) {
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getContainersBetweenObjects: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Loops through all instances of GenericCommunicationsPort at any level inside the given object and gets the physical path. 
     * Only the ports with connections {@literal (physicalPath.size > 1)} are returned
     * @param objectClass The class of the object.
     * @param objectId The id of the object.
     * @param sessionId Session token
     * @return A list of physical paths from all the ports with connections inside the given object. See <code>getPhysicalPath</code> for details about the structure of each entry
     * @throws ServerSideException In the same cases as <code>getChildrenOfClassLightRecursive</code> and <code>getPhysicalPath</code>

     */
    @WebMethod(operationName = "getPhysicalConnectionsInObject")
    public List<RemoteObjectLightList> getPhysicalConnectionsInObject(@WebParam(name = "objectClass")String objectClass, 
            @WebParam(name = "objectId")String objectId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getPhysicalConnectionsInObject(objectClass, objectId, getIPAddress(), sessionId);
        } catch(Exception e) {
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getPhysicalConnectionsInObject: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Convenience method that returns the link connected to a port (if any). It serves to avoid calling {@link getSpecialAttribute} two times.
     * @param portClassName The class of the port
     * @param portId The id of the port
     * @param sessionId The session token
     * @return The link connected to the port or null if there isn't any
     * @throws org.kuwaiba.exceptions.ServerSideException If the port could not be found or if the class provided does not exist or if The class provided is not a subclass of GenericPort
     */
    @WebMethod(operationName = "getLinkConnectedToPort")
    public RemoteObject getLinkConnectedToPort(
        @WebParam(name = "portClassName") String portClassName, 
        @WebParam(name = "portId") String portId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.getLinkConnectedToPort(portClassName, portId, getIPAddress(), sessionId);
        } catch(Exception e) {
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getLinkConnectedToPort: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the physical trace of connections and ports from a given port.
     * @param objectClass Object class
     * @param objectId Object id
     * @param sessionId Session token
     * @return An array containing the sorted elements in the physical path of the given port. The first element is the port from which the trace is generated, 
     * while the last is the destination port until which there is physical continuity. If the source port does not have any connection, the physical path will have only one element (that port).
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             
     */
    @WebMethod(operationName = "getPhysicalPath")
    public List<RemoteObjectLight> getPhysicalPath (@WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")String objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getPhysicalPath(objectClass, objectId, getIPAddress(), sessionId);
        } catch(Exception e) {
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getPhysicalPath: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }  
   
    /**
     * Connects pairs of ports (if they are not connected already) using physical link
     * @param sideAClassNames The list of classes of one of the sides of the connection
     * @param sideAIds The list of ids the objects on one side of the connection
     * @param linksClassNames the classes of the links that will connect the two sides
     * @param linksIds The ids of these links
     * @param sideBClassNames The list of classes of the other side of the connection
     * @param sideBIds The list of ids the objects on the other side of the connection
     * @param sessionId Session token
     * @throws ServerSideException If the object can not be found
     *                             If either the object class or the attribute can not be found
     *                             If any of the objects can't be found
     *                             If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     *                             If any of the classes provided can not be found
     *                             If the object activity log could no be found
     */
    @WebMethod(operationName = "connectPhysicalLinks")
    public void connectPhysicalLinks (@WebParam(name = "sideAClassNames")String[] sideAClassNames, @WebParam(name = "sideAIds")String[] sideAIds,
                                      @WebParam(name = "linksClassNames")String[] linksClassNames, @WebParam(name = "linksIds")String[] linksIds,
                                      @WebParam(name = "sideBClassNames")String[] sideBClassNames, @WebParam(name = "sideBIds")String[] sideBIds,
                                      @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            if ((sideAClassNames.length + sideAIds.length + linksClassNames.length + linksIds.length + sideBClassNames.length + sideBIds.length) / 4 != sideAClassNames.length)
                throw new ServerSideException("The array sizes don't match");
            
            wsBean.connectPhysicalLinks(sideAClassNames, sideAIds, linksClassNames, linksIds, sideBClassNames, sideBIds, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in connectPhysicalLinks: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Connects pairs of elements (of any class except subclasses of GenericPort) using containers (subclasses of GenericPhysicalContainer) 
     * @param sideAClassNames The list of classes of one of the sides of the connection
     * @param sideAIds The list of ids the objects on one side of the connection
     * @param containersClassNames the classes of the containers that will connect the two sides
     * @param containersIds The ids of these containers
     * @param sideBClassNames The list of classes of the other side of the connection
     * @param sideBIds The list of ids the objects on the other side of the connection
     * @param sessionId Session token
     * @throws ServerSideException If any of the provided objects can not be found, if the endpoints are already connected, or if one of the endpoints is a port
     */
    @WebMethod(operationName = "connectPhysicalContainers")
    public void connectPhysicalContainers (@WebParam(name = "sideAClassNames")String[] sideAClassNames, @WebParam(name = "sideAIds")String[] sideAIds,
                                      @WebParam(name = "containersClassNames")String[] containersClassNames, @WebParam(name = "containersIds")String[] containersIds,
                                      @WebParam(name = "sideBClassNames")String[] sideBClassNames, @WebParam(name = "sideBIds")String[] sideBIds,
                                      @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            if ((sideAClassNames.length + sideAIds.length + containersClassNames.length + containersIds.length + sideBClassNames.length + sideBIds.length) / 4 != sideAClassNames.length)
                throw new ServerSideException("The array sizes do not match");
            
            wsBean.connectPhysicalContainers(sideAClassNames, sideAIds, containersClassNames, containersIds, sideBClassNames, sideBIds, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in connectPhysicalContainers: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
        /**
     * Disconnects a side or both sides of a physical connection (a link or a container)
     * @param connectionClass Class of the connection to be edited
     * @param connectionId Id of the connection to be edited
     * @param sideToDisconnect Side to disconnect. Use 1 to disconnect only the side a, 2 to disconnect only side b and 3 to disconnect both sides at once
     * @param sessionId Session token
     * @throws ServerSideException If the object can not be found
     *                             If either the object class or the attribute can not be found
     *                             If the class provided does not exist
     *                             If any of the relationships is now allowed according to the defined data model
     *                             If the object activity log could no be found
     */
    @WebMethod(operationName = "disconnectPhysicalConnection")
    public void disconnectPhysicalConnection(@WebParam(name = "connectionClass")String connectionClass,
                                      @WebParam(name = "connectionId")String connectionId, 
                                      @WebParam(name = "sideToDisconnect")int sideToDisconnect,
                                      @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.disconnectPhysicalConnection(connectionClass, connectionId, sideToDisconnect, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in disconnectPhysicalConnection: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
        
    /**
     * Changes one or both sides (endpoints) of a physical connection (link or container). Use this method carefully in containers, as it does not check 
     * if the endpoints of the links inside the container that was reconnected are consistent with its new endpoints. Also note that 
     * when used in physical links, the link will NOT be moved (as in the special containment hierarchy) to the nearest common parent of both endpoints. 
     * This method can not be used to <i>disconnect</i> connections, to do that use {@link #disconnectPhysicalConnection(java.lang.String, long, int, java.lang.String) }.
     * @param connectionClass The class of the connection to be modified
     * @param connectionId The id of the connection to be modified
     * @param newASideClass The class of the new side A of the connection. Use null if this side is not to be changed.
     * @param newASideId The id of the new side A of the connection. Use -1 if this side is not to be changed.
     * @param newBSideClass The class of the new side B of the connection. Use null if this side is not to be changed.
     * @param newBSideId The id of the new side B of the connection. Use -1 if this side is not to be changed.
     * @param sessionId The session token
     * @throws ServerSideException If any of the objects provided could not be found or if the new endpoint is not a port (if reconnecting a link) or if it is a port 
     * (if reconnecting a container)
     */
    @WebMethod(operationName = "reconnectPhysicalConnection")
    public void reconnectPhysicalConnection(@WebParam(name = "connectionClass")String connectionClass,
                                      @WebParam(name = "connectionId")String connectionId, 
                                      @WebParam(name = "newASideClass")String newASideClass,
                                      @WebParam(name = "newASideId")String newASideId,
                                      @WebParam(name = "newBSideClass")String newBSideClass,
                                      @WebParam(name = "newBSideId")String newBSideId,
                                      @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.reconnectPhysicalConnection(connectionClass, connectionId, newASideClass, newASideId, newBSideClass, newBSideId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in reconnectPhysicalConnection: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Deletes a physical connection
     * @param objectClass Object class
     * @param objectId Object id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object couldn't be found
     *                             If the class could not be found
     *                             If the object could not be deleted because there's some 
     *                            business rules that avoids it or it has incoming relationships.
     *                             If the log root node could not be found
     */
    @WebMethod(operationName = "deletePhysicalConnection")
    public void deletePhysicalConnection(
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")String objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.deletePhysicalConnection(objectClass, objectId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deletePhysicalConnection: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    //Services manager
    /**
     * Associates an object (a resource) to an existing service
     * @param objectClass Object class
     * @param objectId Object id
     * @param serviceClass service class
     * @param serviceId service id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the objects can't be found
     *                             If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     *                             If any of the classes provided can not be found
     */
    @WebMethod(operationName = "associateObjectToService")
    public void associateObjectToService (
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")String objectId,
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "serviceId")String serviceId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.associateObjectToService(objectClass, objectId, serviceClass, serviceId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in associateObjectToService: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
     /**
     * Associates a list of objects (resources) to an existing service
     * @param objectClass Object class
     * @param objectId Object id
     * @param serviceClass service class
     * @param serviceId service id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the objects can't be found
     *                             If any of the objects involved can't be connected (i.e. if it's not an inventory object)
     *                             If any of the classes provided can not be found
     */
    @WebMethod(operationName = "associateObjectsToService")
    public void associateObjectsToService (
            @WebParam(name = "objectClass")String[] objectClass,
            @WebParam(name = "objectId")String[] objectId,
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "serviceId")String serviceId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.associateObjectsToService(objectClass, objectId, serviceClass, serviceId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in associateObjectsToService: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Releases an object from a service that is using it
     * @param serviceClass Service class
     * @param serviceId Service id
     * @param targetId target object id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object can not be found
     *                             If the class can not be found
     *                             If the object activity log could no be found
     */
    @WebMethod(operationName = "releaseObjectFromService")
    public void releaseObjectFromService (
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "serviceId")String serviceId,
            @WebParam(name = "targetId")String targetId,           
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.releaseObjectFromService(serviceClass, serviceId, targetId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in releaseObjectFromService: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the services associated to a service 
     * @param serviceClass Service class
     * @param serviceId Service id
     * @param sessionId Session token
     * @return A list of services
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object can not be found
     *                             If either the object class or the attribute can not be found
     */
    @WebMethod(operationName = "getServiceResources")
    public List<RemoteObjectLight> getServiceResources (
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "serviceId")String serviceId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            return wsBean.getServiceResources(serviceClass, serviceId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getServiceResources: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    //Audit Trail
    /**
     * Retrieves the log entries for a given [business] object
     * @param objectClass Object class
     * @param objectId Object id
     * @param limit Max number of results (0 to retrieve all)
     * @param sessionId Session token
     * @return The object's audit trail
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the class provided is not subclass of  InventoryObject
     */
    @WebMethod(operationName = "getBusinessObjectAuditTrail")
    public ApplicationLogEntry[] getBusinessObjectAuditTrail (
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")String objectId,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            return wsBean.getBusinessObjectAuditTrail (objectClass, objectId, limit, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getObjectAuditTrail: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the list of activity log entries
     * @param page current page
     * @param limit limit of results per page. 0 to retrieve them all
     * @param sessionId The session id
     * @return The list of activity log entries
     * @throws ServerSideException If the user is not allowed to invoke the method, If anything goes wrong
     */
    @WebMethod(operationName = "getGeneralActivityAuditTrail")
    public ApplicationLogEntry[] getGeneralActivityAuditTrail (
            @WebParam(name = "page")int page,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            return wsBean.getGeneralActivityAuditTrail (page, limit, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getActivityAuditTrail: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Attaches a file to an inventory object
     * @param name The name of the file. It's more like its title, instead of the file name
     * @param tags A semicolon (";") separated string with the tags associated to this document. These tags can be used to help find documents in a search
     * @param file The actual file
     * @param className The class name of the inventory object the file will be attached to
     * @param objectId The id of the inventory object the file will be attached to
     * @param sessionId Session token
     * @return The id of the file object that was created
     * @throws ServerSideException If the file can not be saved or if there's already a file with that name related to the object or if the file exceeds the max size configured
     */
    @WebMethod(operationName = "attachFileToObject")
    public long attachFileToObject(@WebParam(name = "name")String name, 
            @WebParam(name = "tags")String tags, @WebParam(name = "file")byte[] file, 
            @WebParam(name = "className")String className, @WebParam(name = "objectId")String objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.attachFileToObject(name, tags, file, className, objectId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in attachFileToObject: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Detaches a file from an inventory object. Note that the file will also be deleted. 
     * @param fileObjectId The id of the file object
     * @param className The class of the object the file will be detached from
     * @param objectId The id of the object the file will be detached from
     * @param sessionId The session token
     * @throws ServerSideException If the object or its class could not be found, or if the file object could not be found or if there was a problem physically deleting the file from disk
     */
    @WebMethod(operationName = "detachFileFromObject")
    public void detachFileFromObject(@WebParam(name = "fileObjectId")long fileObjectId,
            @WebParam(name = "className")String className, @WebParam(name = "objectId")String objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.detachFileFromObject(fileObjectId, className, objectId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in detachFileFromObject: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the files associated to a given inventory object
     * @param className The class of the object o retrieve the files from
     * @param objectId The id of the object o retrieve the files from
     * @param sessionId The session token
     * @return A list of light file objects
     * @throws ServerSideException If the object or its class could not be found
     */
    @WebMethod(operationName = "getFilesForObject")
    public List<RemoteFileObjectLight> getFilesForObject(@WebParam(name = "className")String className, @WebParam(name = "objectId")String objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            return wsBean.getFilesForObject(className, objectId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getFilesForObject: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves a particular file from those attached to an inventory object. The returned object contains the contents of the file
     * @param fileObjectId The id of the file object
     * @param className The class of the object the file will be detached from
     * @param objectId The id of the object the file will be detached from
     * @param sessionId The session token
     * @return The object file encapsulating the contents of the file.
     * @throws ServerSideException If the object or its class could not be found, or if the file object could not be found or if there was a problem physically deleting the file from disk
     */
    @WebMethod(operationName = "getFile")
    public RemoteFileObject getFile(@WebParam(name = "fileObjectId")long fileObjectId, 
            @WebParam(name = "className")String className, @WebParam(name = "objectId")String objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getFile(fileObjectId, className, objectId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getFilesForObject: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Updates the properties of a file object (name or tags)
     * @param fileObjectId The id of the file object
     * @param properties The set of properties as a dictionary key-value. Valid keys are "name" and "tags"
     * @param className The class of the object the file is attached to
     * @param objectId The id of the object the file is attached to
     * @param sessionId The session token
     * @throws ServerSideException If the object file is attached to could not be found or if the file object could not be found or if any of the properties has an invalid name or if the file name is empty or if the class of the object file is attached to could not be found
     */
    @WebMethod(operationName = "updateFileProperties")
    public void updateFileProperties(@WebParam(name = "fileObjectId") long fileObjectId, 
            @WebParam(name = "properties")List<StringPair> properties, @WebParam(name = "className")String className, 
            @WebParam(name = "objectId")String objectId, @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.updateFileProperties(fileObjectId, properties, className, objectId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getFilesForObject: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Metadata Methods. Click on the + sign on the left to edit the code.">

    /**
     * Creates a class metadata object. This method is still under testing and might be buggy
     * @param className Class name
     * @param displayName Class display name
     * @param description Class description
     * @param isAbstract is this class abstract?
     * @param isCustom Is this class part of the core of the application (can not be deleted) or if it's an extension to the default data model. In most cases, this should be "true".
     * @param parentClassName Parent class name
     * @param isCountable NOt used so far. It's intended to be used to mark the classes that are created to make consistent the model, but that are not actual inventory elements, such as Slots
     * @param icon Icon for views. The size is limited by the value in Constants.MAX_ICON_SIZE and it's typically 32x32 pixels
     * @param isInDesign Says if a class can be instantiated or not. This is useful if you are creating many classes and want to avoid the users to create objects from those classes until you have finished the data model roll-out.
     * @param smallIcon Icon for trees. The size is limited by the value in Constants.MAX_ICON_SIZE
     * @param sessionId Session token
     * @param color The color to be used to display the instances of this class (depends on the client used)
     * @return the id of the new class metadata object
     * @throws ServerSideException If the specified parent class doesn't exist
     *                             If the reference node doesn't exist
     *                             If any of the fields of the class definition has an invalid value
     */
    @WebMethod(operationName = "createClass")
    public long createClass(@WebParam(name = "className")
        String className, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "description")
        String description, @WebParam(name = "isAbstract")
        boolean isAbstract, @WebParam(name = "isCustom")
        boolean isCustom, @WebParam(name = "isCountable")
        boolean isCountable, @WebParam(name = "isInDesign")
        boolean isInDesign, @WebParam(name = "parentClassName")
        String parentClassName, @WebParam(name = "icon")
        byte[] icon, @WebParam(name = "smallIcon")
        byte[] smallIcon, @WebParam(name = "color")
        int color, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException {
        
        try{
            if (icon != null){
                if (icon.length > Constants.MAX_ICON_SIZE){
                    throw new ServerSideException(String.format("The uploaded file exceeds the max file size (%s)", Constants.MAX_BACKGROUND_SIZE));
                }
            }
            if (smallIcon != null){
                if (smallIcon.length > Constants.MAX_ICON_SIZE){
                    throw new ServerSideException(String.format("The uploaded file exceeds the max file size (%s)", Constants.MAX_BACKGROUND_SIZE));
                }
            }
            RemoteClassMetadata ci = new RemoteClassMetadata();
            ci.setClassName(className);
            ci.setDisplayName(displayName);
            ci.setDescription(description);
            ci.setIcon(icon);
            ci.setSmallIcon(smallIcon);
            ci.setColor(color);
            ci.setParentClassName(parentClassName);
            ci.setAbstract(isAbstract);
            ci.setCountable(isCountable);
            ci.setCustom(isCustom);
            ci.setInDesign(isInDesign);

            return wsBean.createClass(ci, getIPAddress(), sessionId);

        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createClass: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

     /**
     * Updates a class metadata properties. Use null values for those properties that shouldn't be touched
     * @param classId
     * @param className metadata name. Null if unchanged
     * @param displayName New class metadata display name. Null if unchanged
     * @param description New class metadata description. Null if unchanged
     * @param isAbstract is this class abstract?
     * @param icon New icon for views. Null if unchanged. The size is limited by the value in Constants.MAX_ICON_SIZE
     * @param color The color of the instances of this class.
     * @param smallIcon New icon for trees. Null if unchanged. The size is limited by the value in Constants.MAX_ICON_SIZE
     * @param isInDesign If the class is in design stage (see createClass).
     * @param isCustom If the class is custom (see createClass).
     * @param isCountable If the class is countable (see createClass). 
     * @param sessionId Session token
     * @throws ServerSideException If there is any problem retrieving an object, 
     *                             while checking if every created object of the 
     *                             class with an attributes marked as mandatory has value.
     */
    @WebMethod(operationName = "setClassProperties")
    public void setClassProperties(@WebParam(name = "classId")
        long classId, @WebParam(name = "className")
        String className, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "description")
        String description, @WebParam(name = "smallIcon")
        byte[] smallIcon,  @WebParam(name = "icon")
        byte[] icon, @WebParam(name = "color")
        int color,@WebParam(name = "isAbstract")
        Boolean isAbstract, @WebParam(name = "isInDesign")
        Boolean isInDesign, @WebParam(name = "isCustom")
        Boolean isCustom, @WebParam(name = "isCountable")
        Boolean isCountable, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException {
        try
        {
            if (icon != null){
                if (icon.length > Constants.MAX_ICON_SIZE)
                    throw new ServerSideException(String.format("The file exceeds the file size limits (%s)", Constants.MAX_BACKGROUND_SIZE));
            }
            if (smallIcon != null){
                if (smallIcon.length > Constants.MAX_ICON_SIZE)
                    throw new ServerSideException(String.format("The file exceeds the file size limits (%s)", Constants.MAX_BACKGROUND_SIZE));
            }
            RemoteClassMetadata ci = new RemoteClassMetadata();
            ci.setId(classId);
            ci.setClassName(className);
            ci.setDisplayName(displayName);
            ci.setDescription(description);
            ci.setIcon(icon);
            ci.setSmallIcon(smallIcon);
            ci.setColor(color);
            ci.setAbstract(isAbstract);
            ci.setInDesign(isInDesign);
            ci.setCountable(isCountable);
            ci.setCustom(isCustom);

            wsBean.setClassProperties(ci, getIPAddress(), sessionId);

        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in setClassProperty: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Checks if a class has a attribute with a given name
     * @param className Class name
     * @param attributeName Attribute name
     * @param sessionId Session token
     * @return True if the given class has the attribute
     * @throws ServerSideException If there is no a class with such className
     */
    @WebMethod(operationName = "hasAttribute")
    public boolean hasAttribute(
        @WebParam(name = "className") String className, 
        @WebParam(name = "attributeName") String attributeName, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            return wsBean.hasAttribute(className, attributeName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in hasAttribute: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets a class attribute, using the class name as key to find it
     * @param className the class name
     * @param attributeName
     * @param sessionId Session token
     * @return the class attribute
     * @throws ServerSideException If there is no a class with such className
     *                             If the attributeName does not exist
     */
    @WebMethod(operationName = "getAttribute")
    public RemoteAttributeMetadata getAttribute(@WebParam(name = "className")
    String className, @WebParam(name = "attributeName")
    String attributeName, @WebParam(name = "sesionId")
    String sessionId) throws ServerSideException {
        try {
            return wsBean.getAttribute(className, attributeName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getAttribute: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets a class attribute, using the class id as key to find it
     * @param classId Class id
     * @param attributeName Attribute name
     * @param sessionId  Session token
     * @return The attribute definition
     * @throws ServerSideException If there is no a class with such classId
     *                             If the attributeName does not exist
     */
    @WebMethod(operationName = "getAttributeForClassWithId")
    public RemoteAttributeMetadata getAttributeForClassWithId(@WebParam(name = "classId")
        String classId, @WebParam(name = "attributeName")
        String attributeName, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException{
        try {
            return wsBean.getAttribute(classId, attributeName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getAttributeForClassWithId: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
   
    /**
     * Adds an attribute to a class using its name as key to find it. If value of a given attribute is null, a default value will be set (except for the name, which is mandatory)
     * @param className Class name where the attribute will be attached
     * @param name attribute name
     * @param displayName attribute display name
     * @param type attribute type
     * @param description attribute description
     * @param administrative is the attribute administrative?
     * @param visible is the attribute visible?
     * @param noCopy Marks an attribute as not to be copied during a copy operation.
     * @param isReadOnly is the attribute read only?
     * @param unique should this attribute be unique?
     * @param mandatory is the attribute mandatory when an object is created
     * @param multiple Indicates if the attribute is a multiple selection list type. This flag has no effect in primitive types, such as strings or numbers
     * @param order Tells the system how to sort the attributes. A call to any method that returns the attributes of a class will return them sorted by order.
     * This is useful to show the attributes in property sheets in order of importance, for example. The default value is 1000
     * @param sessionId session token
     * @throws ServerSideException If there is no a class with such className
     *                             If any of the parameters to create the attribute has a wrong value
     */
    @WebMethod(operationName = "createAttribute")
    public void createAttribute(@WebParam(name = "className")
        String className,  @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "type")
        String type, @WebParam(name = "description")
        String description, @WebParam(name = "administrative")
        boolean administrative, @WebParam(name = "visible")
        boolean visible, @WebParam(name = "isReadOnly")
        boolean isReadOnly, @WebParam(name = "noCopy")
        boolean noCopy, @WebParam(name = "unique")
        boolean unique, @WebParam(name = "mandatory")
        boolean mandatory, @WebParam(name = "multiple")
        boolean multiple, @WebParam(name = "order")
        int order, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException {

        try {
            RemoteAttributeMetadata attrInfo = new RemoteAttributeMetadata(name, displayName, type, administrative, 
                    visible, isReadOnly, unique, mandatory, multiple, description, noCopy, order);

            wsBean.createAttribute(className, attrInfo, getIPAddress(), sessionId);

        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createAttribute: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Adds an attribute to a class using its id as key to find it. If value of a given attribute is null, a default value will be put in place (except for the name, which is mandatory)
     * @param ClassId Class id where the attribute will be attached
     * @param name attribute name
     * @param displayName attribute display name
     * @param type attribute type
     * @param description attribute description
     * @param administrative is the attribute administrative?
     * @param visible is the attribute visible?
     * @param readOnly is the attribute read only?
     * @param noCopy Marks an attribute as not to be copied during a copy operation.
     * @param unique should this attribute be unique?
     * @param mandatory is the attribute mandatory when an object is created
     * @param multiple Indicates if the attribute is a multiple selection list type. This flag has no effect in primitive types, such as strings or numbers
     * @param order Tells the system how to sort the attributes. A call to any method that returns the attributes of a class will return them sorted by order.
     * This is useful to show the attributes in property sheets in order of importance, for example. The default value is 1000
     * @param sessionId session token
     * @throws ServerSideException If any of the parameters to create the attribute has a wrong value
     */
    @WebMethod(operationName = "createAttributeForClassWithId")
    public void createAttributeForClassWithId(@WebParam(name = "classId")
        long ClassId, @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "type")
        String type, @WebParam(name = "description")
        String description, @WebParam(name = "administrative")
        boolean administrative, @WebParam(name = "visible")
        boolean visible, @WebParam(name = "readOnly")
        boolean readOnly, @WebParam(name = "noCopy")
        boolean noCopy, @WebParam(name = "unique")
        boolean unique, @WebParam(name = "mandatory")
        boolean mandatory, @WebParam(name = "multiple")
        boolean multiple, @WebParam(name = "order")
        int order, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException {

        try {
            RemoteAttributeMetadata attrInfo = new RemoteAttributeMetadata(name, displayName, type, administrative, 
                                   visible, readOnly, unique, mandatory, multiple, description, noCopy, order);

            wsBean.createAttribute(ClassId, attrInfo, getIPAddress(), sessionId);

        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createAttributeForClassWithId: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Updates a class attribute taking its name as key to find it. If value of a given attribute is null, the old value will remain unchanged.
     * @param className Class the attribute belongs to
     * @param attributeId attribute id
     * @param name attribute name
     * @param displayName attribute display name
     * @param type attribute type
     * @param description attribute description
     * @param administrative is the attribute administrative?
     * @param visible is the attribute visible?
     * @param readOnly is the attribute read only?
     * @param unique should this attribute be unique?
     * @param mandatory is the attribute mandatory when an object is created
     * @param multiple Indicates if the attribute is a multiple selection list type. This flag has no effect in primitive types, such as strings or numbers
     * @param noCopy can this attribute be copy in copy/paste operation?
     * @param order Tells the system how to sort the attributes. A call to any method that returns the attributes of a class will return them sorted by order.
     * This is useful to show the attributes in property sheets in order of importance, for example. The default value is 1000
     * @param sessionId session token
     * @throws ServerSideException If an object can't be find, while it is checking 
     *                             if every object of the class (or subclasses) has 
     *                             a value in an attribute marked as mandatory
     */
    @WebMethod(operationName = "setAttributeProperties")
    public void setAttributeProperties(@WebParam(name = "className")
        String className, @WebParam(name = "attributeId")
        long attributeId, @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "description")
        String description, @WebParam(name = "type")
        String type, @WebParam(name = "administrative")
        Boolean administrative, @WebParam(name = "mandatory")
        Boolean mandatory, @WebParam(name = "multiple")
        Boolean multiple, @WebParam(name = "noCopy")
        Boolean noCopy, @WebParam(name = "readOnly")
        Boolean readOnly, @WebParam(name = "unique")
        Boolean unique, @WebParam(name = "visible")
        Boolean visible, @WebParam(name = "order")
        Integer order, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException {

        try {
            RemoteAttributeMetadata ai = new RemoteAttributeMetadata(attributeId, name, displayName,
                    type, administrative, visible, readOnly, unique, mandatory, multiple,
                    description, noCopy, order);
            wsBean.setAttributeProperties(className, ai, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in setAttributeProperties: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Updates a class attribute taking its id as key to find it. If value of a given attribute is null, the old value will remain unchanged.
     * @param classId Class the attribute belongs to
     * @param attributeId attribute id
     * @param name attribute name
     * @param displayName attribute display name
     * @param type attribute type
     * @param description attribute description
     * @param administrative is the attribute administrative?
     * @param visible is the attribute visible?
     * @param readOnly is the attribute read only?
     * @param unique should this attribute be unique?
     * @param mandatory is the attribute mandatory when an object is created
     * @param multiple Indicates if the attribute is a multiple selection list type. This flag has no effect in primitive types, such as strings or numbers
     * @param noCopy can this attribute be copy in copy/paste operation?
     * @param order Tells the system how to sort the attributes. A call to any method that returns the attributes of a class will return them sorted by order.
     * This is useful to show the attributes in property sheets in order of importance, for example. The default value is 1000
     * @param sessionId session token
     * @throws ServerSideException If an object can't be find, while it is checking 
     *                             if every object of the class (or subclasses) has 
     *                             a value in an attribute marked as mandatory
     */
    @WebMethod(operationName = "setAttributePropertiesForClassWithId")
    public void setAttributePropertiesForClassWithId(@WebParam(name = "classId")
        long classId, @WebParam(name = "attributeId")
        long attributeId, @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "description")
        String description, @WebParam(name = "type")
        String type, @WebParam(name = "administrative")
        Boolean administrative, @WebParam(name = "mandatory")
        Boolean mandatory, @WebParam(name = "multiple")
        Boolean multiple, @WebParam(name = "noCopy")
        Boolean noCopy, @WebParam(name = "readOnly")
        Boolean readOnly, @WebParam(name = "unique")
        Boolean unique, @WebParam(name = "visible")
        Boolean visible, @WebParam(name = "order")
        Integer order, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException {

        try {
            RemoteAttributeMetadata ai = new RemoteAttributeMetadata(attributeId, name, displayName, 
                    type, administrative, visible, readOnly, unique, mandatory, multiple,
                    description, noCopy, order);
            wsBean.setAttributeProperties(classId, ai, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in setAttributePropertiesForClassWithId: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes an attribute from a class using the class name as key to find it
     * @param className Class name
     * @param attributeName Attribute name
     * @param sessionId Session token
     * @throws ServerSideException If the class could not be found.
     *                             If the attributes name or creationDate are to be deleted.
     */    
    @WebMethod(operationName = "deleteAttribute")
    public void deleteAttribute(@WebParam(name = "className") 
            String className, @WebParam(name = "attributeName")
            String attributeName, @WebParam(name = "sessionId")
            String sessionId) throws ServerSideException{
        try {
            wsBean.deleteAttribute(className, attributeName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteAttribute: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Deletes an attribute from a class using the class id as key to find it
     * @param classId Class id
     * @param attributeName Attribute name
     * @param sessionId Session token
     * @throws ServerSideException If the class could not be found.
     *                             If the attributes name or creationDate are to be deleted.
     */
    @WebMethod(operationName = "deleteAttributeForClassWithId")
    public void deleteAttributeForClassWithId(@WebParam(name = "classId") 
            long classId, @WebParam(name = "attributeName")
            String attributeName, @WebParam(name = "sessionId")
            String sessionId) throws ServerSideException{
        try {
            wsBean.deleteAttribute(classId, attributeName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteAttributeForClassWithId: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Gets the metadata of a given class using its name as key to find it
     * @param className Class name
     * @param sessionId Session token
     * @return The metadata as a ClassInfo instance
     * @throws ServerSideException If there is no class with such className
     */
    @WebMethod(operationName = "getClass")
    public RemoteClassMetadata getClass(@WebParam(name = "className")
    String className, @WebParam(name = "sessionId")
    String sessionId) throws ServerSideException {

        try {
            return wsBean.getClass(className, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getClass: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Gets the metadata of a given class using its id as key to find it
     * @param classId Class metadata object id
     * @param sessionId session token
     * @return The metadata as a ClassInfo instance
     * @throws ServerSideException If there is no class with such classId
     */
    @WebMethod(operationName = "getClassWithId")
    public RemoteClassMetadata getClassWithId(@WebParam(name = "classId")
    long classId, @WebParam(name = "sessionId")
    String sessionId) throws ServerSideException {
        try {
            return wsBean.getClass(classId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getClassWithId: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the subclasses of a given class
     * @param className Class name
     * @param includeAbstractClasses should the result include the abstract classes?
     * @param includeSelf Should the list include the subclasses and the parent class?
     * @param sessionId Session token
     * @return The list of subclasses
     * @throws ServerSideException If the provided class is not a subclass of InventoryObject
     */
    @WebMethod(operationName = "getSubClassesLight")
    public List<RemoteClassMetadataLight> getSubClassesLight(
            @WebParam(name = "className")String className,
            @WebParam(name = "includeAbstractClasses")boolean includeAbstractClasses,
            @WebParam(name = "includeSelf")boolean includeSelf,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException{
        try
        {
            return wsBean.getSubClassesLight(className, includeAbstractClasses, includeSelf, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSubclassesLight: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }

    }
    
    /**
     * Gets the subclasses of a given class
     * @param className Class name
     * @param includeAbstractClasses should the result include the abstract classes?
     * @param includeSelf Should the list include the subclasses and the parent class?
     * @param sessionId Session token
     * @return The list of subclasses
     * @throws ServerSideException Exception If the class could not be found.
     */
    @WebMethod(operationName = "getSubClassesLightNoRecursive")
    public List<RemoteClassMetadataLight> getSubClassesLightNoRecursive(
            @WebParam(name = "className")String className,
            @WebParam(name = "includeAbstractClasses")boolean includeAbstractClasses,
            @WebParam(name = "includeSelf")boolean includeSelf,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException{
        try
        {
            return wsBean.getSubClassesLightNoRecursive(className, includeAbstractClasses, includeSelf, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSubClassesLightNoRecursive: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Retrieves the metadata for the entire class hierarchy as ClassInfo instances
     * @param sessionId Session token
     * @param includeListTypes boolean to indicate if the list should include the subclasses of
     * GenericObjectList
     * @return An array with the metadata for the entire class hierarchy as ClassInfo instances
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getAllClasses")
    public List<RemoteClassMetadata> getAllClasses(
            @WebParam(name = "includeListTypes")boolean includeListTypes,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException{
        try
        {
            return wsBean.getAllClasses(includeListTypes, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getAllClasses: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the metadata for the entire class hierarchy as ClassInfoLight instances
     * @param sessionId Session token
     * @param includeListTypes boolean to indicate if the list should include the subclasses of
     * GenericObjectList
     * @return An array with the metadata for the entire class hierarchy as ClassInfoLight instances
     * @throws ServerSideException If GenericListType class does not exist.
     */
    @WebMethod(operationName = "getAllClassesLight")
    public List<RemoteClassMetadataLight> getAllClassesLight(
            @WebParam(name = "includeListTypes")boolean includeListTypes,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException{
        try
        {
            return wsBean.getAllClassesLight(includeListTypes, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getAllClassesLight: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Deletes a class from the data model using its name as key to find it
     * @param className Class name
     * @param sessionId Session token
     * @throws ServerSideException If there is not a class with de ClassName
     *                             If the class is a core class, has instances, has 
     *                            incoming relationships or is a list type that is 
     *                            used by another class.
     */
    @WebMethod(operationName = "deleteClass")
    public void deleteClass(@WebParam(name = "className")
    String className, @WebParam(name = "sessionId")
    String sessionId) throws ServerSideException {

        try {
            wsBean.deleteClass(className, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteClass: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Deletes a class from the data model using its id as key to find it
     * @param classId Class id
     * @param sessionId Session token
     * @throws ServerSideException If the class is a core class, has instances, has 
     *                             incoming relationships or is a list type that is
     *                             used by another class.
     */
    @WebMethod(operationName = "deleteClassWithId")
    public void deleteClassWithId(@WebParam(name = "classId")
    long classId, @WebParam(name = "sessionId")
    String sessionId) throws ServerSideException {

        try {
            wsBean.deleteClass(classId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteClassWithId: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Gets the possible children of a class according to the containment hierarchy. This method is recursive, and if a possible child is an abstract class, it gets its non-abstract subclasses
     * @param parentClassName Class to retrieve its possible children
     * @param sessionId Session token
     * @return A list of possible children as ClassInfoLight instances
     * An array with the metadata for the entire class hierarchy as ClassInfoLight instances
     * @throws ServerSideException If the class can not be found
     */
    @WebMethod(operationName = "getPossibleChildren")
    public List<RemoteClassMetadataLight> getPossibleChildren(@WebParam(name = "parentClassName")
                    String parentClassName, @WebParam(name = "sessionId")
                    String sessionId) throws ServerSideException {

        try {
            return wsBean.getPossibleChildren(parentClassName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getPossibleChildren: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the possible special children of a class according to the containment hierarchy. 
     * This method is recursive, and if a possible special child is an abstract class, 
     * it gets its non-abstract subclasses
     * @param parentClassName Class to retrieve its possible special children
     * @param sessionId Session token
     * @return A list of possible special children as ClassInfoLight instances
     * @throws ServerSideException If the class can not be found
     */
    @WebMethod(operationName = "getPossibleSpecialChildren")
    public List<RemoteClassMetadataLight> getPossibleSpecialChildren(
        @WebParam(name = "parentClassName") String parentClassName, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            return wsBean.getPossibleSpecialChildren(parentClassName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getPossibleChildren: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Gets the possible children of a class according to the containment hierarchy.
     * This method is not recursive, and only returns the direct possible children,
     * even if they're abstract
     * @param parentClassName Class to retrieve its possible children
     * @param sessionId Session token
     * @return An array with the metadata for the entire class hierarchy as ClassInfoLight instances
     * @throws ServerSideException If the class could not be found
     */
    @WebMethod(operationName = "getPossibleChildrenNoRecursive")
    public List<RemoteClassMetadataLight> getPossibleChildrenNoRecursive(@WebParam(name = "parentClassName")
    String parentClassName, @WebParam(name = "sessionId")
    String sessionId) throws ServerSideException {

        try {
            return wsBean.getPossibleChildrenNoRecursive(parentClassName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getPossibleChildrenNoRecursive: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the possible special children of a class according to the containment hierarchy.
     * This method is not recursive, and only returns the direct possible special children,
     * even if they're abstract
     * @param parentClassName Class to retrieve its possible special children
     * @param sessionId Session token
     * @return A List with the metadata for the entire class hierarchy as ClassInfoLight instances
     * @throws ServerSideException If the class could not be found
     */
    @WebMethod(operationName = "getPossibleSpecialChildrenNoRecursive")
    public List<RemoteClassMetadataLight> getPossibleSpecialChildrenNoRecursive(
        @WebParam(name = "parentClassName") String parentClassName, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            return wsBean.getPossibleSpecialChildrenNoRecursive(parentClassName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getPossibleChildrenNoRecursive: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
        
    /**
     * Adds possible children to a given class using its id as argument. If any of the arguments provided are already added,
     * it will abort the operation and rise an exception
     * @param parentClassId Class to attach the new possible children
     * @param newPossibleChildren List of new possible children. Abstract classes are de-aggregated
     * @param sessionId Session token
     * @throws ServerSideException If any of the possible children or the parent doesn't exist
     *                             If any of the possible children classes already are possible children.
     */
    @WebMethod(operationName = "addPossibleChildrenForClassWithId")
    public void addPossibleChildrenForClassWithId(@WebParam(name = "parentClassId")
            long parentClassId, @WebParam(name = "childrenToBeAdded")
            long[] newPossibleChildren, @WebParam(name = "sessionId")
            String sessionId) throws ServerSideException {

        try {
            wsBean.addPossibleChildren(parentClassId, newPossibleChildren, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in addPossibleChildrenForClassWithId: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Adds possible special children to a given class using its id as argument. If any of the arguments provided are already added,
     * it will abort the operation and rise an exception
     * @param parentClassId Class to attach the new possible special children
     * @param possibleSpecialChildren List of new possible children. Abstract classes are de-aggregated
     * @param sessionId Session token
     * @throws ServerSideException If any of the possible children or the parent doesn't exist
     *                             If any of the possible children classes already are possible special children.
     */
    @WebMethod(operationName = "addPossibleSpecialChildrenWithId")
    public void addPossibleSpecialChildrenWithId(
        @WebParam(name = "parentClassId") long parentClassId, 
        @WebParam(name = "possibleSpecialChildren") long[] possibleSpecialChildren, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.addPossibleSpecialChildren(parentClassId, possibleSpecialChildren, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in addPossibleChildrenForClassWithId: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

     /**
     * Adds possible children to a given class using its name as argument.
     * If any of the arguments provided are already added,
     * it will abort the operation and rise an exception
     * @param parentClassName Class to attach the new possible children
     * @param childrenToBeAdded List of new possible children. Abstract classes are de-aggregated
     * @param sessionId Session token
     * @throws ServerSideException If the parent class or any of the possible children can not be found
     *                             If any of the given possible children can not be a possible children of parentClassName
     */
    @WebMethod(operationName = "addPossibleChildren")
    public void addPossibleChildren(@WebParam(name = "parentClassName")
            String parentClassName, @WebParam(name = "childrenToBeAdded")
            String[] childrenToBeAdded, @WebParam(name = "sessionId")
            String sessionId) throws ServerSideException {
        try {
            wsBean.addPossibleChildren(parentClassName, childrenToBeAdded, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in addPossibleChildren: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Adds special possible children to a given class using its name.
     * If any of the arguments provided are already added,
     * it will abort the operation and rise an exception
     * @param parentClassName Class to attach the new possible special children
     * @param possibleSpecialChildren List of new possible special children. Abstract classes are de-aggregated
     * @param sessionId Session token
     * @throws ServerSideException  If the parent class or any of the possible children can not be found
     *                              If any of the given possible children can not be a possible children of parentClassName
     */
    @WebMethod(operationName = "addPossibleSpecialChildren")
    public void addPossibleSpecialChildren(
        @WebParam(name = "parentClassName") String parentClassName, 
        @WebParam(name = "possibleSpecialChildren") String[] possibleSpecialChildren, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            wsBean.addPossibleSpecialChildren(parentClassName, possibleSpecialChildren, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in addPossibleSpecialChildren: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Removes a set of possible children for a given class
     * @param parentClassId Class the possible children are going to be removed from
     * @param childrenToBeRemoved List of ids of classes to be removed as possible children
     * @param sessionId Session token
     * @throws ServerSideException If any of the ids provided can't be found
     */
    @WebMethod(operationName = "removePossibleChildrenForClassWithId")
    public void removePossibleChildrenForClassWithId(@WebParam(name = "parentClassId")
    long parentClassId, @WebParam(name = "childrenToBeRemoved")
    long[] childrenToBeRemoved, @WebParam(name = "sessionId")
    String sessionId) throws ServerSideException {
        try{
            wsBean.removePossibleChildren(parentClassId, childrenToBeRemoved, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in removePossibleChildrenForClassWithId: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Removes a set of possible special children for a given class.
     * @param parentClassId Parent Class of the possible special children are going to be removed from
     * @param specialChildrenToBeRemoved List of ids of classes to be remove as possible special children
     * @param sessionId Session token
     * @throws ServerSideException If any of the ids provided can't be found
     */
    @WebMethod(operationName = "removePossibleSpecialChildren")
    public void removePossibleSpecialChildren(
        @WebParam(name = "parentClassId") long parentClassId, 
        @WebParam(name = "specialChildrenToBeRemoved") long[] specialChildrenToBeRemoved, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try{
            wsBean.removePossibleSpecialChildren(parentClassId, specialChildrenToBeRemoved, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in removePossibleSpecialChildren: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }

    /**
     * Gets the containment hierarchy of a given class, but upwards (i.e. for Building, it could return 
     * City, Country, Continent)
     * @param className Class to be evaluated
     * @param recursive do it recursively or not
     * @param sessionId
     * @return List of classes in the upstream containment hierarchy
     * @throws ServerSideException If className does not correspond to any existing class
     */
    @WebMethod(operationName = "getUpstreamContainmentHierarchy")
    public List<RemoteClassMetadataLight> getUpstreamContainmentHierarchy(@WebParam(name = "className")
            String className, @WebParam(name = "recursive")
            boolean recursive, @WebParam(name = "sessionId")
            String sessionId) throws ServerSideException {
        try{
            return wsBean.getUpstreamContainmentHierarchy(className, recursive, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getUpstreamContainmentHierarchy: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the special containment hierarchy of a given class, but upwards (i.e. for Building, it could return 
     * City, Country, Continent)
     * @param className Class to be evaluated
     * @param recursive Do it recursively or not
     * @param sessionId Session id token
     * @return List of classes in upstream special containment hierarchy
     * @throws ServerSideException If className does not correspond to any existing class
     * 
     */
    @WebMethod(operationName = "getUpstreamSpecialContainmentHierarchy")
    public List<RemoteClassMetadataLight> getUpstreamSpecialContainmentHierarchy(
        @WebParam(name="className") String className, 
        @WebParam(name="recursive") boolean recursive, 
        @WebParam(name="sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.getUpstreamSpecialContainmentHierarchy(className, recursive, getIPAddress(), sessionId);
        } catch(Exception e) {
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getUpstreamSpecialContainmentHierarchy: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the parent classes of a given class up to RootObject.
     * @param className The class to get the superclasses from
     * @param includeSelf If the result should also include the class in className
     * @param sessionId The session token
     * @return The list of super classes until the root of the hierarchy
     * @throws org.kuwaiba.exceptions.ServerSideException If the class provided could not be found
     */
    @WebMethod(operationName = "getUpstreamClassHierarchy")
    public List<RemoteClassMetadataLight> getUpstreamClassHierarchy(
        @WebParam(name="className") String className, 
        @WebParam(name="includeSelf") boolean includeSelf, 
        @WebParam(name="sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.getUpstreamClassHierarchy(className, includeSelf, getIPAddress(), sessionId);
        } catch(Exception e) {
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getUpstreamClassHierarchy: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Utility methods. Click on the + sign on the left to edit the code.">/**
    /**
     * Tests if a class is subclass of another.
     * @param className Class to be tested.
     * @param allegedParentClass Class to be tested against.
     * @param sessionId Session token.
     * @return If the tested class is subclass of allegedParentClass or not.
     * @throws ServerSideException In case something goes wrong.
     */
    @WebMethod(operationName =  "isSubclassOf")
    public boolean isSubClassOf(@WebParam(name = "className") String className, 
                                @WebParam(name = "allegedParentClass") String allegedParentClass,
                                @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.isSubclassOf(className, allegedParentClass, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in isSubClassOf: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Sync/ bulk load methods. Click on the + sign on the left to edit the code.">/**
    /**
     * Creates many objects at once given a well formatted file. See user manual for details on how to format the file
     * @param file The file with size no greater 
     * @param commitSize The records are not committed one by one, but in batch. This number tells Kuwaiba how many records (lines) to commit at once.
     * @param dataType What kind of data contains the file, listTypes, inventory objects, etc
     * @param sessionId Session token.
     * @return The result of the operation.
     * @throws ServerSideException If something goes wrong.
     */
    @WebMethod(operationName = "bulkUpload")
    public String bulkUpload(@WebParam(name = "file")
        byte[] file, @WebParam(name = "commitSize")
        int commitSize, @WebParam(name = "dataType")
        int dataType, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException {
        try{
            return wsBean.bulkUpload(file, commitSize, dataType, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in downloadBulkLoadLog: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the log file product of a bulk load operation.
     * @param fileName The name of the file  (provided by the method that performs the bulk creation)
     * @param sessionId Session token
     * @return The contents of the file.
     * @throws ServerSideException  If something goes wrong.
     */
    @WebMethod(operationName = "downloadBulkLoadLog")
    public byte[] downloadBulkLoadLog(@WebParam(name = "fileName")
        String fileName, @WebParam(name = "sessionId")
            String sessionId) throws ServerSideException {
        try{
            return wsBean.downloadBulkLoadLog(fileName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in downloadBulkLoadLog: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Services">
    //public 
    // </editor-fold>
    
    //<editor-fold desc="Templates" defaultstate="collapsed">
    /**
     * Creates a template.
     * @param templateClass The class you want to create a template for.
     * @param templateName The name of the template.
     * @param sessionId Session token.
     * @return The id of the newly created template.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the provided class does not exist
     *                             If the template class is abstract.
     */
    @WebMethod(operationName = "createTemplate")
    public String createTemplate(@WebParam(name = "templateClass")String templateClass, 
            @WebParam(name = "templateName")String templateName, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.createTemplate(templateClass, templateName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createTemplate: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates an object inside a template.
     * @param templateElementClass Class of the object you want to create.
     * @param templateElementParentClassName Class of the parent to the object you want to create.
     * @param templateElementParentId Id of the parent to the object you want to create.
     * @param templateElementName Name of the element.
     * @param sessionId Session token.
     * @return The id of the new object.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object (or its parent) class could not be found
     *                             If the parent object could not be found
     *                             If the class provided to create the new element from is abstract.
     */
    @WebMethod(operationName = "createTemplateElement")
    public String createTemplateElement(@WebParam(name = "templateElementClass")String templateElementClass, 
            @WebParam(name = "templateElementParentClassName")String templateElementParentClassName,
            @WebParam(name = "templateElementParentId")String templateElementParentId,
            @WebParam(name = "templateElementName")String templateElementName,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.createTemplateElement(templateElementClass, templateElementParentClassName, 
                    templateElementParentId, templateElementName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createTemplateElement: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates an special object inside a template.
     * @param tsElementClass Class of the special object you want to create.
     * @param tsElementParentClassName Class of the parent to the special object you want to create.
     * @param tsElementParentId Id of the parent to the special object you want to create.
     * @param tsElementName Name of the element.
     * @param sessionId Session token.
     * @return The id of the new object.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the element class are not a possible special child of the element parent class
     *                             If the element class given are abstract
     *                             If the element class or element parent class can not be found
     *                             If the element parent can no be found
     */
    @WebMethod(operationName = "createTemplateSpecialElement")
    public String createTemplateSpecialElement(
            @WebParam(name = "templateElementClass")String tsElementClass, 
            @WebParam(name = "tsElementParentClassName")String tsElementParentClassName,
            @WebParam(name = "tsElementParentId")String tsElementParentId,
            @WebParam(name = "tsElementName")String tsElementName,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.createTemplateSpecialElement(tsElementClass, tsElementParentClassName, 
                    tsElementParentId, tsElementName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createTemplateSpecialElement: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates multiple template elements using a given name pattern
     * @param templateElementClassName The class name of the new set of template elements
     * @param templateElementParentClassName The parent class name of the new set of template elements
     * @param templateElementParentId The parent id of the new set of template elements
     * @param numberOfTemplateElements The number of template elements
     * @param templateElementNamePattern Name pattern of the new set of template elements
     * @param sessionId Session id token
     * @return An array of ids for the new template elements
     * @throws ServerSideException If the parent class name or the template element class name cannot be found
     *                             If the given template element class cannot be a child of the given parent
     *                             If the parent class name cannot be found
     *                             If the given pattern to generate the name has less possibilities that the number of template elements to be created
     */
    @WebMethod(operationName = "createBulkTemplateElement")
    public String[] createBulkTemplateElement(
        @WebParam(name = "templateElementClass") String templateElementClassName, 
        @WebParam(name = "templateElementParentClassName") String templateElementParentClassName, 
        @WebParam(name = "templateElementParentId") String templateElementParentId, 
        @WebParam(name = "numberOfTemplateElements") int numberOfTemplateElements, 
        @WebParam(name = "templateElementNamePattern") String templateElementNamePattern, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.createBulkTemplateElement(templateElementClassName, templateElementParentClassName, 
                templateElementParentId, numberOfTemplateElements, templateElementNamePattern, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createBulkTemplateElement: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates multiple special template elements using a given name pattern
     * @param stElementClass The class name of the new set of special template elements
     * @param stElementParentClassName The parent class name of the new set of special template elements
     * @param stElementParentId The parent id of the new set of special template elements
     * @param numberOfTemplateElements The number of template elements
     * @param stElementNamePattern Name pattern of the new set of special template elements
     * @param sessionId Session id token
     * @return An array if ids for the new special template elements
     * @throws ServerSideException If the parent class name or the special template element class name cannot be found
     *                             If the given special template element class cannot be a child of the given parent
     *                             If the parent class name cannot be found
     *                             If the given pattern to generate the name has less possibilities that the number of special template elements to be created
     */
    @WebMethod(operationName = "createBulkSpecialTemplateElement")
    public String[] createBulkSpecialTemplateElement(
        @WebParam(name = "stElementClass") String stElementClass, 
        @WebParam(name = "stElementParentClassName") String stElementParentClassName, 
        @WebParam(name = "stElementParentId") String stElementParentId, 
        @WebParam(name = "numberOfTemplateElements") int numberOfTemplateElements, 
        @WebParam(name = "stElementNamePattern") String stElementNamePattern, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.createBulkSpecialTemplateElement(stElementClass, stElementParentClassName, 
                stElementParentId, numberOfTemplateElements, stElementNamePattern, getIPAddress(), sessionId);
            
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createBulkSpecialTemplateElement: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Updates the value of an attribute of a template element.
     * @param templateElementClass Class of the element you want to update.
     * @param templateElementId Id of the element you want to update.
     * @param attributeNames Names of the attributes that you want to be updated as an array of strings.
     * @param attributeValues The values of the attributes you want to upfate. For list types, it's the id of the related type
     * @param sessionId Session token.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the classes provided as arguments do not exist
     *                             If the template element could not be found
     *                             If the arrays attributeNames and attributeValues have different sizes
     */
    @WebMethod(operationName = "updateTemplateElement")
    public void updateTemplateElement(@WebParam(name = "templateElementClass")String templateElementClass, 
            @WebParam(name = "templateElementId")String templateElementId,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[] attributeValues,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.updateTemplateElement(templateElementClass, templateElementId, 
                    attributeNames, attributeValues, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateTemplateElement: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes an element within a template or a template itself.
     * @param templateElementClass The template element class.
     * @param templateElementId The template element id.
     * @param sessionId Session token.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the element's class could not be found.
     *                             If the element could not be found.
     */
    @WebMethod(operationName = "deleteTemplateElement")
    public void deleteTemplateElement(@WebParam(name = "templateElementClass")String templateElementClass, 
            @WebParam(name = "templateElementId")String templateElementId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.deleteTemplateElement(templateElementClass, templateElementId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteTemplateElement: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the templates available for a given class
     * @param className Class whose templates we need
     * @param sessionId Session token
     * @return A list of templates (actually, the top element) as a list of RemoteOObjects
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the class provided could not be found.
     */
    @WebMethod(operationName = "getTemplatesForClass")
    public List<RemoteObjectLight> getTemplatesForClass(
            @WebParam(name = "className")String className, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getTemplatesForClass(className, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getTemplatesForClass: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Copy template elements within templates. Should not be used to copy entire templates.
     * @param sourceObjectsClassNames Array with the class names of the elements to be copied.
     * @param sourceObjectsIds  Array with the ids of the elements to be copied.
     * @param newParentClassName Class of the parent of the copied objects.
     * @param newParentId Id of the parent of the copied objects.
     * @param sessionId Session token.
     * @return An array with the ids of the newly created elements in the same order they were provided.
     * @throws ServerSideException If the user is not allowed to invoke the method.
     *                             If any of the classes could not be found.
     *                             If any of the source template elements could not be found.
     *                             If the arrays provided as arguments have different sizes.
     */
    @WebMethod(operationName = "copyTemplateElements")
    public String[] copyTemplateElements(@WebParam(name = "sourceObjectsClassNames")String[] sourceObjectsClassNames, 
                                       @WebParam(name = "sourceObjectsIds")String[] sourceObjectsIds, 
                                       @WebParam(name = "newParentClassName")String newParentClassName,
                                       @WebParam(name = "newParentId")String newParentId, 
                                       @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.copyTemplateElements(sourceObjectsClassNames, sourceObjectsIds, newParentClassName, newParentId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in copyTemplateElements: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    
    /**
     * Copy template special elements within templates. Should not be used to copy entire templates.
     * @param sourceObjectsClassNames Array with the class names of the special elements to be copied.
     * @param sourceObjectsIds  Array with the ids of the special elements to be copied.
     * @param newParentClassName Class of the parent of the copied objects.
     * @param newParentId Id of the parent of the copied objects.
     * @param sessionId Session token.
     * @return An array with the ids of the newly created special elements in the same order they were provided.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the arrays provided as arguments have different sizes.
     *                             If any of the classes could not be found.
     *                             If any of the source template elements could not be found.
     */
    @WebMethod(operationName = "copyTemplateSpecialElements")
    public String[] copyTemplateSpecialElements(
        @WebParam(name = "sourceObjectsClassNames")String[] sourceObjectsClassNames, 
        @WebParam(name = "sourceObjectsIds")String[] sourceObjectsIds, 
        @WebParam(name = "newParentClassName")String newParentClassName,
        @WebParam(name = "newParentId")String newParentId, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        
        try {
            return wsBean.copyTemplateSpecialElements(sourceObjectsClassNames, sourceObjectsIds, newParentClassName, newParentId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in copyTemplateSpecialElements: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the children of a given template element.
     * @param templateElementClass Template element class.
     * @param templateElementId Template element id.
     * @param sessionId 
     * @return The template element's children as a list of RemoteBusinessObjectLight instances.
     * @throws ServerSideException If the user is not allowed to invoke the method
     */
    @WebMethod(operationName = "getTemplateElementChildren")
    public List<RemoteObjectLight> getTemplateElementChildren(
        @WebParam(name = "templateElementClass")String templateElementClass, 
        @WebParam(name = "templateElementId")String templateElementId, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        
        try {
            return wsBean.getTemplateElementChildren(templateElementClass, templateElementId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getTemplateElementChildren: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the children of a given template special element.
     * @param tsElementClass Template special element class.
     * @param tsElementId Template special element id.
     * @param sessionId 
     * @return The template element's children as a list of RemoteBusinessObjectLight instances.
     * @throws ServerSideException If the user is not allowed to invoke the method
     */
    @WebMethod(operationName = "getTemplateSpecialElementChildren")
    public List<RemoteObjectLight> getTemplateSpecialElementChildren(
        @WebParam(name = "tsElementClass")String tsElementClass, 
        @WebParam(name = "tsElementId")String tsElementId, 
        @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        
        try {
            return wsBean.getTemplateSpecialElementChildren(tsElementClass, tsElementId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getTemplateElementChildren: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrives all the information of a given template element.
     * @param templateElementClass Template element class.
     * @param templateElementId Template element id.
     * @param sessionId session token
     * @return The template element information
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the template class does not exist
     *                             If the template element could not be found
     *                             If an attribute value can't be mapped into value
     */
    @WebMethod(operationName = "getTemplateElement")
    public RemoteObject getTemplateElement(@WebParam(name = "templateElementClass")String templateElementClass, 
            @WebParam(name = "templateElementId")String templateElementId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getTemplateElement(templateElementClass, templateElementId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getTemplateElement: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Reporting API methods">
    /**
     * Creates a class level report (a report that will be available for all instances of a given class -and its subclasses-)
     * @param className Class this report is going to be related to. It can be ab abstract class and the report will be available for all its subclasses
     * @param reportName Name of the report.
     * @param reportDescription Report description.
     * @param script Script text.
     * @param outputType What will be the default output of this report? See ClassLevelReportDescriptor for possible values
     * @param enabled If enabled, a report can be executed.
     * @param sessionId Session token
     * @return The id of the newly created report.
     * @throws ServerSideException If the user is not allowed to invoke the method.
     *                             If the class provided could not be found.
     */
    @WebMethod(operationName = "createClassLevelReport")
    public long createClassLevelReport(@WebParam(name = "className")String className, 
            @WebParam(name = "reportName")String reportName, @WebParam(name = "reportDescription")String reportDescription, 
            @WebParam(name = "script")String script, @WebParam(name = "outputType")int outputType, 
            @WebParam(name = "enabled")boolean enabled, @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.createClassLevelReport(className, reportName, reportDescription, 
                    script, outputType, enabled, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createClassLevelReport: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates an inventory level report (a report that is not tied to a particlar instance or class. In most cases, they also receive parameters)
     * @param reportName Name of the report.
     * @param reportDescription Report description.
     * @param script Script text.
     * @param outputType What will be the default output of this report? See InventoryLevelReportDescriptor for possible values
     * @param enabled If enabled, a report can be executed.
     * @param parameters Optional (it might be either null or an empty array). The list of the names parameters that this report will support. They will always be captured as strings, so it's up to the author of the report the sanitization and conversion of the inputs
     * @param sessionId Session token
     * @return The id of the newly created report.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the dummy root could not be found, which is actually a severe problem.
     */
    @WebMethod(operationName = "createInventoryLevelReport")
    public long createInventoryLevelReport(@WebParam(name = "reportName")String reportName, 
            @WebParam(name = "reportDescription")String reportDescription, @WebParam(name = "script")String script, 
            @WebParam(name = "outputType")int outputType, @WebParam(name = "enabled")boolean enabled, 
            @WebParam(name = "parameters")List<StringPair> parameters, @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.createInventoryLevelReport(reportName, reportDescription, script, 
                    outputType, enabled, parameters, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createInventoryLevelReport: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a report
     * @param reportId The id of the report.
     * @param sessionId Session token.
     * @throws ServerSideException If the user is not allowed to invoke the method.
     *                             If the report could not be found.
     */
    @WebMethod(operationName = "deleteReport")
    public void deleteReport(@WebParam(name = "reportId")long reportId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.deleteReport(reportId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteReport: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Updates the properties of an existing class level report.
     * @param reportId Id of the report.
     * @param reportName The name of the report. Null to leave it unchanged.
     * @param reportDescription The description of the report. Null to leave it unchanged.
     * @param enabled Is the report enabled? . Null to leave it unchanged.
     * @param type Type of the output of the report. See LocalReportLight for possible values
     * @param script Text of the script. 
     * @param sessionId Session token.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the report properties has a wrong or unexpected format
     *                             If the report could not be found.
     */
    @WebMethod(operationName = "updateReport")
    public void updateReport(@WebParam(name = "reportId")long reportId, @WebParam(name = "reportName")String reportName, 
            @WebParam(name = "reportDescription")String reportDescription, @WebParam(name = "enabled")Boolean enabled,
            @WebParam(name = "type")Integer type, @WebParam(name = "script")String script, @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.updateReport(reportId, reportName, reportDescription, enabled,
                                    type, script, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateReport: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Updates the value of any of the parameters of a given report.
     * @param reportId Report id.
     * @param parameters List of pairs attribute-value of the report. Valid values are name, description, script and enabled.
     * @param sessionId Session token.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the any of the parameters has an invalid name
     */
    @WebMethod(operationName = "updateReportParameters")
    public void updateReportParameters(@WebParam(name = "reportId")long reportId, 
            @WebParam(name = "parameters")List<StringPair> parameters, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException 
    {
        try {
            wsBean.updateReportParameters(reportId, parameters, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateReport: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the class level reports associated to the given class (or its superclasses)
     * @param className The class to extract the reports from.
     * @param recursive False to get only the directly associated reports. True top get also the reports associate top its superclasses
     * @param includeDisabled True to also include the reports marked as disabled. False to return only the enabled ones.
     * @param sessionId Session token.
     * @return The list of reports.
     * @throws ServerSideException If the class could not be found
     *                             If the user is not allowed to invoke the method
     */
    @WebMethod(operationName = "getClassLevelReports")
    public List<RemoteReportLight> getClassLevelReports(@WebParam(name = "className")String className, 
            @WebParam(name = "recursive") boolean recursive, 
            @WebParam(name = "includeDisabled") boolean includeDisabled, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException
    {
        try {
            return wsBean.getClassLevelReports(className, recursive, includeDisabled, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getClassLevelReports: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the inventory class reports.
     * @param includeDisabled True to also include the reports marked as disabled. False to return only the enabled ones.
     * @param sessionId Session token.
     * @return The list of reports.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the dummy root could not be found, which is actually a severe problem.
     */
    @WebMethod(operationName = "getInventoryLevelReports")
    public List<RemoteReportLight> getInventoryLevelReports(@WebParam(name = "includeDisabled")boolean includeDisabled, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getInventoryLevelReports(includeDisabled, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getInventoryLevelReports: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the information related to a class level report.
     * @param reportId The id of the report.
     * @param sessionId Session token.
     * @return  The report.
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the report could not be found.
     */
    @WebMethod(operationName = "getReport")
    public RemoteReport getReport(@WebParam(name = "reportId")long reportId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getReport(reportId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getReport: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Executes a class level report and returns the result.
     * @param objectClassName The class of the instance that will be used as input for the report.
     * @param objectId The id of the instance that will be used as input for the report.
     * @param reportId The id of the report.
     * @param sessionId Session token.
     * @return The result of the report execution.
     * @throws ServerSideException If the class could not be found or 
     *                             if the report could not be found or 
     *                             if the inventory object could not be found or 
     *                             if there's an error during the execution of the report. 
     */
    @WebMethod(operationName = "executeClassLevelReport")
    public byte[] executeClassLevelReport(@WebParam(name = "objectClassName")String objectClassName, 
            @WebParam(name = "objectId")String objectId, @WebParam(name = "reportId")long reportId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.executeClassLevelReport(objectClassName, objectId, reportId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in executeClassLevelReport: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
   
    /**
     * Executes an inventory level report and returns the result.
     * @param reportId The id of the report.
     * @param parameters List of pairs param name - param value
     * @param sessionId Session token.
     * @return The result of the report execution.
     * @throws ServerSideException If the user is not allowed to invoke the method or
     *                             If the report could not be found or 
     *                             if the associated script exits with error.
     */
    @WebMethod(operationName = "executeInventoryLevelReport")
    public byte[] executeInventoryLevelReport(@WebParam(name = "reportId")long reportId, 
            @WebParam(name = "parameters")List<StringPair> parameters, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.executeInventoryLevelReport(reportId, parameters, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in executeInventoryLevelReport: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    // </editor-fold>

    //<editor-fold desc="Favorites" defaultstate="collapsed">
    /**
     * Creates a favorites folder for User.
     * @param favoritesFolderName Bookmark folder name
     * @param userId User id
     * @param sessionId The session token
     * @return The id of the new Bookmark folder
     * @throws ServerSideException If the user is not allowed to invoke the method or 
     *                             If the user can not be found or
     *                             If the name is null or empty
     */
    @WebMethod(operationName = "createFavoritesFolderForUser")
    public long createFavoritesFolderForUser(
        @WebParam(name = "favoritesFolderName") String favoritesFolderName, 
        @WebParam(name = "userId") long userId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            return wsBean.createFavoritesFolderForUser(favoritesFolderName, userId, getIPAddress(), sessionId);
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createFavoritesFolderForUser: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a set of favorites folders
     * @param favoritesFolderId Bookmark folder id
     * @param userId The User id
     * @param sessionId The session token
     * @throws ServerSideException If the user is not allowed to invoke the method or
     *                             If any favorites folder in the array can not be found
     */
    @WebMethod(operationName = "deleteFavoritesFolders")
    public void deleteFavoritesFolders (
        @WebParam(name = "favoritesFolderId") long[] favoritesFolderId, 
        @WebParam(name = "userId") long userId,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            wsBean.deleteFavoritesFolders (favoritesFolderId, userId, getIPAddress(), sessionId);
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteFavoritesFolders: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
        
    /**
     * Gets the list of favorites folders of a given User.
     * @param userId User id
     * @param sessionId The session token
     * @return The list of Bookmarks for user
     * @throws ServerSideException If the user is not allowed to invoke the method or
     *                             If the user can not be found
     */
    @WebMethod(operationName = "getFavoritesFoldersForUser")
    public List<RemoteFavoritesFolder> getFavoritesFoldersForUser(
        @WebParam(name = "userId") long userId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
                
        try {
            return wsBean.getFavoritesFoldersForUser(userId, getIPAddress(), sessionId);
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getFavoritesFoldersForUser: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Associates a list of objects to an existing favorites folder
     * @param objectClass Object class name
     * @param objectId Object id
     * @param favoritesFolderId Bookmark folder id
     * @param userId The User id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the favorites folder can not be found
     *                             If the object can not be found
     *                             If the object have a relationship with the favorite folder
     */
    @WebMethod(operationName = "addObjectsToFavoritesFolder")    
    public void addObjectsToFavoritesFolder(
        @WebParam(name = "objectClass") String[] objectClass, 
        @WebParam(name = "objectId") String[] objectId, 
        @WebParam(name = "favoritesFolderId") long favoritesFolderId, 
        @WebParam(name = "userId") long userId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            wsBean.addObjectsToFavoritesFolder(objectClass, objectId, favoritesFolderId, userId, getIPAddress(), sessionId);
        } catch(Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in addObjectsToFavoritesFolder: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Removes a list of objects from a given favorites folder
     * @param objectClass Object class name
     * @param objectId Object id
     * @param favoritesFolderId Bookmark folder id
     * @param userId User id the favorites folder belongs to
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the favorites folder can not be found
     *                             If the object can not be found
     *                             If the object can not be found
     */
    @WebMethod(operationName = "removeObjectsFromFavoritesFolder")
    public void removeObjectsFromFavoritesFolder(
        @WebParam(name = "objectClass") String[] objectClass, 
        @WebParam(name = "objectId") String[] objectId, 
        @WebParam(name = "favoritesFolderId") long favoritesFolderId, 
        @WebParam(name = "userId") long userId,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            wsBean.removeObjectsFromFavoritesFolder(objectClass, objectId, favoritesFolderId, userId, getIPAddress(), sessionId);
        } catch(Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in removeObjectsFromFavoritesFolder: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the objects in a to favorites folder
     * @param favoritesFolderId Bookmark folder id
     * @param userId User Id
     * @param limit Max number of results. Use -1 to retrieve all.
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the favorites folder can not be found
     * @return The list of objects
     */
    @WebMethod(operationName = "getObjectsInFavoritesFolder")
    public List<RemoteObjectLight> getObjectsInFavoritesFolder(
        @WebParam(name = "favoritesFolderId") long favoritesFolderId, 
        @WebParam(name = "userId") long userId,
        @WebParam(name = "limit") int limit,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.getObjectsInFavoritesFolder(favoritesFolderId, userId, limit, getIPAddress(), sessionId);
        } catch(Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getFavoritesFolderItems: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the favorites folders an object is included into.
     * @param userId User Id
     * @param objectClass Object Class name
     * @param objectId Object id
     * @param sessionId Session token
     * @return The list of bookmarks where an object is associated
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object can not be found
     *                             If the object is associated to a bookmark folder but 
     *                            The favorites folder is not associated to the current user
     */
    @WebMethod(operationName = "getFavoritesFoldersForObject")
    public List<RemoteFavoritesFolder> getFavoritesFoldersForObject(
        @WebParam(name = "userId") long userId,
        @WebParam(name = "objectClass") String objectClass, 
        @WebParam(name = "objectId") String objectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            return wsBean.getFavoritesFoldersForObject(userId, objectClass, objectId, getIPAddress(), sessionId);
        } catch(Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getFavoritesFoldersForObject: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
        
    /**
     * Gets a favorites folder
     * @param favoritesFolderId Bookmark folder id
     * @param userId User id
     * @param sessionId Session token
     * @return The Bookmark folder
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the favorites folder can not be found
     */
    @WebMethod(operationName = "getFavoritesFolder")
    public RemoteFavoritesFolder getFavoritesFolder(
        @WebParam(name = "favoritesFolderId") long favoritesFolderId, 
        @WebParam(name = "userId") long userId,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            return wsBean.getFavoritesFolder(favoritesFolderId, userId, getIPAddress(), sessionId);
        } catch(Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getFavoritesFolder: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Updates a favorites folder
     * @param favoritesFolderId Favorites folder id
     * @param favoritesFolderName Favorites folder name
     * @param userId User id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the favorites folder can not be found
     *                             If the name of the favorites folder is null or empty
     */
    @WebMethod(operationName = "updateFavoritesFolder")
    public void updateFavoritesFolder(
        @WebParam(name = "favoritesFolderId") long favoritesFolderId, 
        @WebParam(name = "favoritesFolderName") String favoritesFolderName, 
        @WebParam(name = "userId") long userId,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            wsBean.updateFavoritesFolder(favoritesFolderId, userId, favoritesFolderName, getIPAddress(), sessionId);
        } catch(Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in updateFavoritesFolder: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    //</editor-fold>
    
    //<editor-fold desc="Business Rule Engine" defaultstate="collapsed">
    /**
     * Creates a business rule given a set of constraints
     * @param ruleName Rule name
     * @param ruleDescription Rule description
     * @param ruleType Rule type. See BusinesRule.TYPE* for possible values.
     * @param ruleScope The scope of the rule. See BusinesRule.SCOPE* for possible values.
     * @param appliesTo The class this rule applies to. Can not be null.
     * @param ruleVersion The version of the rule. Useful to migrate it if necessary in further versions of the platform
     * @param constraints An array with the definition of the logic to be matched with the rule. Can not be empty or null
     * @param sessionId Session token
     * @return The id of the newly created business rule
     * @throws ServerSideException If any of the parameters is null (strings) or leer than 1 or if the constraints array is null or empty
     */
    @WebMethod(operationName = "createBusinessRule")
    public long createBusinessRule(@WebParam(name = "ruleName")String ruleName, @WebParam(name = "ruleDescription")String ruleDescription, 
            @WebParam(name = "ruleType")int ruleType, @WebParam(name = "ruleScope")int ruleScope, @WebParam(name = "appliesTo")String appliesTo, 
            @WebParam(name = "ruleVersion")String ruleVersion, @WebParam(name = "constraints")List<String> constraints, @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.createBusinessRule(ruleName, ruleDescription, ruleType, ruleScope, appliesTo, ruleVersion, constraints, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createBusinessRule: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a business rule
     * @param businessRuleId Rule id
     * @param sessionId Session token
     * @throws ServerSideException If the given rule does not exist
     */
    @WebMethod(operationName = "deleteBusinessRule")
    public void deleteBusinessRule(@WebParam(name = "businessRuleId")long businessRuleId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            wsBean.deleteBusinessRule(businessRuleId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteBusinessRule: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the business rules of a particular type.
     * @param type Rule type. See BusinesRule.TYPE* for possible values. Use -1 to retrieve all
     * @param sessionId Session token
     * @return The list of business rules with the matching type.
     * @throws ServerSideException If something unexpected happens
     */
    @WebMethod(operationName = "getBusinessRules")
    public List<RemoteBusinessRule> getBusinessRules(int type, String sessionId) throws ServerSideException {
        try {
            return wsBean.getBusinessRules(type, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getBusinessRules: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    //</editor-fold>
    
    //<editor-fold desc="Synchronization Framework methods" defaultstate="collapsed">
    /**
     * Executes a supervised synchronization job, which consist on connecting to the sync data source 
     * using the configuration attached to the given sync group and finding the differences 
     * between the information currently in the inventory platform and what's in the sync data source. 
     * A supervised sync job needs a human to review the differences and decide what to do,
     * while an automated sync job automatically decides what to do based on built-in business rules.
     * Please note that the execution might take some time, so it is expected that the client to implement an asynchronous call
     * @param syncGroupId The sync group id
     * @param sessionId The session token
     * @return A list of differences that require the authorization of a user to be resolved
     * @throws ServerSideException If the sync group could not be found or if
     */
    @WebMethod(operationName = "launchSupervisedSynchronizationTask")
    public List<SyncFinding> launchSupervisedSynchronizationTask(@WebParam(name = "syncGroupId") long syncGroupId, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            BackgroundJob managedJob = wsBean.launchSupervisedSynchronizationTask(syncGroupId, getIPAddress(), sessionId);                      
            int retries = 0;
            
            while (!managedJob.getStatus().equals(BackgroundJob.JOB_STATUS.FINISHED) && retries < 20) {
                try {                
                    //For some reason (probably thread-concurrency related), the initial "managedJob" instance is different from the one
                    //updated in the SyncProcessor/Writer, so we have to constantly fetch it again.
                    managedJob = JobManager.getInstance().getJob(managedJob.getId());

                    if (managedJob.getStatus().equals(BackgroundJob.JOB_STATUS.ABORTED)) {
                        Exception exceptionThrownByTheJob = managedJob.getExceptionThrownByTheJob();

                        if (exceptionThrownByTheJob != null) {
                            if (exceptionThrownByTheJob instanceof InventoryException)
                                throw new ServerSideException(managedJob.getExceptionThrownByTheJob().getMessage());
                            else {
                                System.out.println("[KUWAIBA] An unexpected error occurred in launchSupervisedSynchronizationTask: " + exceptionThrownByTheJob.getMessage());
                                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                            }
                        }
                    }
                    Thread.sleep(2000);
                }catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage());
                }
                retries ++;
            }
            if (retries == 20)
                throw new ServerSideException("The supervised synchronization task can no be executed");
                
            return (List<SyncFinding>)managedJob.getJobResult();
            
        } catch(ServerSideException | RuntimeException e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in launchSupervisedSynchronizationTask: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
  
    /**
     * Executes an automated synchronization job, which consist on connecting to the sync data source 
     * using the configuration attached to the given sync group and finding the differences 
     * between the information currently in the inventory platform and what's in the sync data source. 
     * An automated sync job does not need human intervention it automatically decides what to do based 
     * on built-in business rules
     * @param syncGroupId The sync group id
     * @param providersName
     * @param sessionId The session token
     * @return The set of results 
     * @throws ServerSideException If the sync group could not be found
     */
    @WebMethod(operationName = "launchAutomatedSynchronizationTask")
    public List<SyncResult> launchAutomatedSynchronizationTask(
            @WebParam(name = "syncGroupId") long syncGroupId, 
            @WebParam(name = "providersName")  String providersName, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            BackgroundJob managedJob = wsBean.launchAdHocAutomatedSynchronizationTask(syncGroupId, providersName, getIPAddress(), sessionId);                      
            int retries = 0;
            while (!managedJob.getStatus().equals(BackgroundJob.JOB_STATUS.FINISHED) && retries < 30) {
                try {                
                    //For some reason (probably thread-concurrency related), the initial "managedJob" instance is different from the one
                    //updated in the SyncProcessor/Writer, so we have to constantly fetch it again.
                    managedJob = JobManager.getInstance().getJob(managedJob.getId());

                    if (managedJob.getStatus().equals(BackgroundJob.JOB_STATUS.ABORTED)) {
                        Exception exceptionThrownByTheJob = managedJob.getExceptionThrownByTheJob();

                        if (exceptionThrownByTheJob != null) {
                            if (exceptionThrownByTheJob instanceof InventoryException)
                                throw new ServerSideException(managedJob.getExceptionThrownByTheJob().getMessage());
                            else {
                                System.out.println("[KUWAIBA] An unexpected error occurred in launchAutomatedSynchronizationTask: " + exceptionThrownByTheJob.getMessage());
                                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                            }
                        }
                    }
                    Thread.sleep(3000);
                }catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage());
                }
                retries ++;
            }
            if (retries == 30)
                throw new ServerSideException("The automated synchronization task can no be executed");
                
            return (List<SyncResult>)managedJob.getJobResult();
            
        } catch(ServerSideException | RuntimeException e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in launchAutomatedSynchronizationTask: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Executes an automated synchronization job, which consist on connecting to the sync data source 
     * using the configuration attached to the given sync group and finding the differences 
     * between the information currently in the inventory platform and what's in the sync data source. 
     * An automated sync job does not need human intervention it automatically decides what to do based 
     * on built-in business rules
     * @param synDsConfigIds The sync data source configurations ids
     * @param providersName
     * @param sessionId The session token
     * @return The set of results 
     * @throws ServerSideException If the sync group could not be found
     */
    @WebMethod(operationName = "launchAdHocAutomatedSynchronizationTask")
    public List<SyncResult> launchAdHocAutomatedSynchronizationTask(
            @WebParam(name = "synDsConfigIds") long[] synDsConfigIds, 
            @WebParam(name = "providersName")  String providersName, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            BackgroundJob managedJob = wsBean.launchAdHocAutomatedSynchronizationTask(synDsConfigIds, providersName, getIPAddress(), sessionId);                      
            int retries = 0;
            while (!managedJob.getStatus().equals(BackgroundJob.JOB_STATUS.FINISHED) && retries < 30) {
                try {                
                    //For some reason (probably thread-concurrency related), the initial "managedJob" instance is different from the one
                    //updated in the SyncProcessor/Writer, so we have to constantly fetch it again.
                    managedJob = JobManager.getInstance().getJob(managedJob.getId());

                    if (managedJob.getStatus().equals(BackgroundJob.JOB_STATUS.ABORTED)) {
                        Exception exceptionThrownByTheJob = managedJob.getExceptionThrownByTheJob();

                        if (exceptionThrownByTheJob != null) {
                            if (exceptionThrownByTheJob instanceof InventoryException)
                                throw new ServerSideException(managedJob.getExceptionThrownByTheJob().getMessage());
                            else {
                                System.out.println("[KUWAIBA] An unexpected error occurred in launchAdHocAutomatedSynchronizationTask: " + exceptionThrownByTheJob.getMessage());
                                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                            }
                        }
                    }
                    Thread.sleep(3000);
                }catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage());
                }
                retries ++;
            }
            if (retries == 30)
                throw new ServerSideException("The automated synchronization task can no be executed");
                
            return (List<SyncResult>)managedJob.getJobResult();
            
        } catch(ServerSideException | RuntimeException e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in launchAdHocAutomatedSynchronizationTask: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Executes the synchronization actions that the user selected after check the  list of findings
     * @param syncGroupId the sync groupId
     * @param actions the list findings to be processed
     * @param sessionId the session token
     * @return the list of results after the actions were executed
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "executeSyncActions")
    public List<SyncResult> executeSyncActions(
            @WebParam(name = "syncGroupId") long syncGroupId,
            @WebParam(name = "actions") List<SyncAction> actions,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.executeSyncActions(syncGroupId, actions, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in executeSyncActions: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the current jobs which are executing
     * @param sessionId the session id token
     * @throws ServerSideException
     * @return The list of the current jobs which are executing
     */
    @WebMethod(operationName = "getCurrentJobs")
    public List<RemoteBackgroundJob> getCurrentJobs(
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.getCurrentJobs(getIPAddress(), sessionId);            
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getCurrentJobs: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Kills a job given its id
     * @param jobId id of job to kill
     * @param sessionId the session id token
     * @throws ServerSideException If the job cannot be found
     */
    @WebMethod(operationName = "killJob")
    public void killJob(
        @WebParam(name = "jobId") long jobId,
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
    
        try {
            wsBean.killJob(jobId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in killJob: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="SDH Networks Module">
    /**
     * Creates an SDH transport link (STMX)
     * @param classNameEndpointA The class name of the endpoint A (some kind of port)
     * @param idEndpointA Id of endpoint A
     * @param classNameEndpointB  The class name of the endpoint Z (some kind of port)
     * @param idEndpointB Id of endpoint Z
     * @param linkType Type of link (STM1, STM4, STM16, STM256, etc)
     * @param defaultName The default name of th
     * @param sessionId Session token
     * @return The id of the newly created transport link
     * @throws ServerSideException If the user is not allowed to invoke the method or
     *                             If any of the requested objects can't be found or 
     *                             If any of the classes provided can not be found or
     *                             If any of the objects involved can't be connected.
     */
    @WebMethod(operationName = "createSDHTransportLink")
    public String createSDHTransportLink(@WebParam(name = "classNameEndpointA") String classNameEndpointA, 
            @WebParam(name = "idEndpointA") String idEndpointA, 
            @WebParam(name = "classNameEndpointB") String classNameEndpointB, 
            @WebParam(name = "idEndpointB") String idEndpointB, 
            @WebParam(name = "linkType") String linkType, 
            @WebParam(name = "defaultName") String defaultName, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.createSDHTransportLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, linkType, defaultName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createSDHTransportLink: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates an SDH container link (VCX). In practical terms, it's always a high order container, such a VC4XXX
     * @param classNameEndpointA The class name of the endpoint A (a GenericCommunicationsEquipment)
     * @param idEndpointA Id of endpoint A
     * @param classNameEndpointB  The class name of the endpoint B (GenericCommunicationsEquipment)
     * @param idEndpointB Id of endpoint B
     * @param linkType Type of link (VC4, VC3, V12, etc. A VC12 alone doesn't make much sense, though)
     * @param positions This param specifies the transport links and positions used by the container. For more details on how this works, please read the "SDH Model: Technical Design and Tools" document. Please note that is greatly advisable to provide them already sorted
     * @param defaultName the name to be assigned to the new element. If null, an empty string will be used
     * @param sessionId Sesion token
     * @return The id of the newly created container link
     * @throws ServerSideException If the user is not allowed to invoke the method or
     *                             If any of the requested objects can't be found or 
     *                             If any of the classes provided can not be found or
     *                             If any of the objects involved can't be connected.
     */
    @WebMethod(operationName = "createSDHContainerLink")
    public String createSDHContainerLink(@WebParam(name = "classNameEndpointA") String classNameEndpointA, 
            @WebParam(name = "idEndpointA") String idEndpointA, 
            @WebParam(name = "classNameEndpointB") String classNameEndpointB,
            @WebParam(name = "idEndpointB") String idEndpointB,
            @WebParam(name = "linkType") String linkType, 
            @WebParam(name = "positions") List<RemoteSDHPosition> positions, 
            @WebParam(name = "defaultName") String defaultName,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.createSDHContainerLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, linkType, positions, defaultName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createSDHContainerLink: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates an SDH tributary link (VCXTributaryLink)
     * @param classNameEndpointA The class name of the endpoint A (some kind of tributary port)
     * @param idEndpointA Id of endpoint A
     * @param classNameEndpointB  The class name of the endpoint B (some kind of tributary port)
     * @param idEndpointB Id of endpoint B
     * @param linkType Type of link (VC4TributaryLink, VC3TributaryLink, V12TributaryLink, etc)
     * @param positions This param specifies the transport links and positions used by the container. For more details on how this works, please read the SDH Model: Technical Design and Tools document. Please note that is greatly advisable to provide them already sorted. Please note that creating a tributary link automatically creates a container link to deliver it
     * @param defaultName the name to be assigned to the new element
     * @param sessionId Session token
     * @return The id of the newly created tributary link
     * @throws ServerSideException If the user is not allowed to invoke the method or
     *                             If any of the requested objects can't be found or 
     *                             If any of the classes provided can not be found or
     *                             If any of the objects involved can't be connected.
     */
    @WebMethod(operationName = "createSDHTributaryLink")
    public String createSDHTributaryLink(@WebParam(name = "classNameEndpointA") String classNameEndpointA, 
            @WebParam(name = "idEndpointA") String idEndpointA, 
            @WebParam(name = "classNameEndpointB") String classNameEndpointB, 
            @WebParam(name = "idEndpointB") String idEndpointB, 
            @WebParam(name = "linkType") String linkType, 
            @WebParam(name = "positions") List<RemoteSDHPosition> positions, 
            @WebParam(name = "defaultName") String defaultName, 
            @WebParam(name = "sessionId")  String sessionId) throws ServerSideException {
        try {
            return wsBean.createSDHTributaryLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, linkType, positions, defaultName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createSDHTributaryLink: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a transport link
     * @param transportLinkClass Transport Link class
     * @param transportLinkId Transport link id
     * @param forceDelete Delete recursively all sdh elements transported by the transport link
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the transport link could not be found
     */
    @WebMethod(operationName = "deleteSDHTransportLink")
    public void deleteSDHTransportLink(@WebParam(name = "transportLinkClass") String transportLinkClass, 
            @WebParam(name = "transportLinkId") String transportLinkId, 
            @WebParam(name = "forceDelete") boolean forceDelete, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.deleteSDHTransportLink(transportLinkClass, transportLinkId, forceDelete, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteSDHTransportLink: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a container link
     * @param containerLinkClass Container link class
     * @param containerLinkId Container class id
     * @param forceDelete Delete recursively all sdh elements contained by the container link
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the container link could not be found
     */
    @WebMethod(operationName = "deleteSDHContainerLink")
    public void deleteSDHContainerLink(@WebParam(name = "containerLinkClass") String containerLinkClass, 
            @WebParam(name = "containerLinkId") String containerLinkId, 
            @WebParam(name = "forceDelete") boolean forceDelete, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.deleteSDHContainerLink(containerLinkClass, containerLinkId, forceDelete, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteSDHContainerLink: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a tributary link and its corresponding container link. This method will delete all the object relationships.
     * @param tributaryLinkClass The class of the tributary link
     * @param tributaryLinkId the id of the tributary link
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the tributary link could not be found
     */
    @WebMethod(operationName = "deleteSDHTributaryLink")
    public void deleteSDHTributaryLink(@WebParam(name = "tributaryLinkClass") String tributaryLinkClass, 
            @WebParam(name = "tributaryLinkId") String tributaryLinkId, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.deleteSDHTributaryLink(tributaryLinkClass, tributaryLinkId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteSDHTributaryLink: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Finds a route between two GenericcommunicationsEquipment based on the TransportLinks network map (for more details on how this works, please read the SDH Model: Technical Design and Tools document)
     * @param communicationsEquipmentClassA The class of one of the route endpoints
     * @param communicationsEquipmentIdA The id of one of the route endpoints
     * @param communicationsEquipmentClassB The class of the other route endpoint
     * @param communicationsEquipmentIB The id of the other route endpoint
     * @param sessionId Session token
     * @return A sorted list of RemoteObjectLights containing the route. This list includes the transport links and the nodes in between, including the very endpoints
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the given communication equipment is no subclass of GenericCommunicationsEquipment
     */
    @WebMethod(operationName = "findSDHRoutesUsingTransportLinks")
    public List<RemoteObjectLightList> findSDHRoutesUsingTransportLinks(@WebParam(name = "communicationsEquipmentClassA") String communicationsEquipmentClassA, 
                                            @WebParam(name = "communicationsEquipmentIdA") String  communicationsEquipmentIdA, 
                                            @WebParam(name = "communicationsEquipmentClassB") String communicationsEquipmentClassB, 
                                            @WebParam(name = "communicationsEquipmentIB") String  communicationsEquipmentIB, 
                                            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.findSDHRoutesUsingTransportLinks(communicationsEquipmentClassA, communicationsEquipmentIdA, communicationsEquipmentClassB, communicationsEquipmentIB, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in findSDHRoutesUsingTransportLinks: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Finds a route between two GenericcommunicationsEquipment based on the ContainerLinks network map (for more details on how this works, please read the SDH Model: Technical Design and Tools document)
     * @param communicationsEquipmentClassA The class of one of the route endpoints
     * @param communicationsEquipmentIdA The id of one of the route endpoints
     * @param communicationsEquipmentClassB The class of the other route endpoint
     * @param communicationsEquipmentIB The id of the other route endpoint
     * @param sessionId Session token
     * @return A sorted list of RemoteObjectLights containing the route. This list includes the transport links and the nodes in between, including the very endpoints
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the given communication equipment is no subclass of GenericCommunicationsEquipment
     */
    @WebMethod(operationName = "findSDHRoutesUsingContainerLinks")
    public List<RemoteObjectLightList> findSDHRoutesUsingContainerLinks(@WebParam(name = "communicationsEquipmentClassA") String communicationsEquipmentClassA, 
                                            @WebParam(name = "communicationsEquipmentIdA") String  communicationsEquipmentIdA, 
                                            @WebParam(name = "communicationsEquipmentClassB") String communicationsEquipmentClassB, 
                                            @WebParam(name = "communicationsEquipmentIB") String  communicationsEquipmentIB, 
                                            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.findSDHRoutesUsingContainerLinks(communicationsEquipmentClassA, communicationsEquipmentIdA, communicationsEquipmentClassB, communicationsEquipmentIB, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in findSDHRoutesUsingContainerLinks: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the container links within a transport link (e.g. the VC4XX in and STMX)
     * @param transportLinkClass Transportlink's class
     * @param transportLinkId Transportlink's id
     * @param sessionId Session token
     * @return The list of the containers that go through that transport link
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the given transport link is no subclass of GenericSDHTransportLink
     */
    @WebMethod(operationName = "getSDHTransportLinkStructure")
    public List<RemoteSDHContainerLinkDefinition> getSDHTransportLinkStructure(@WebParam(name = "transportLinkClass")String transportLinkClass, 
            @WebParam(name = "transportLinkId")String transportLinkId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getSDHTransportLinkStructure(transportLinkClass, transportLinkId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSDHTransportLinkStructure: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the internal structure of a container link. This is useful to provide information about the occupation of a link. This is only applicable to VC4XX
     * @param containerLinkClass Container class
     * @param containerLinkId Container Id
     * @param sessionId Session token
     * @return The list of containers contained in the container
     * @throws ServerSideException If the user is not authorized to know the structure of a container link, 
     *                             if the container supplied is not subclass of GenericSDHHighOrderContainerLink, 
     *                             if the container could not be found or if the class could not be found
     */
    @WebMethod(operationName = "getSDHContainerLinkStructure")
    public List<RemoteSDHContainerLinkDefinition> getSDHContainerLinkStructure(@WebParam(name = "containerLinkClass")String containerLinkClass, 
            @WebParam(name = "containerLinkId")String containerLinkId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getSDHContainerLinkStructure(containerLinkClass, containerLinkId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSDHContainerLinkStructure: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
        // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc="IPAM Module"> 
    /**
     * Retrieves all the pools of subnets
     * @param parentId parent id parent id of the pool, -1 to retrieve the pools from the root nodes
     * @param className IPv4 or IPv6 subnet
     * @param sessionId the session token
     * @return a set of subnet pools
     * @throws ServerSideException if there are not IPAM root nodes or if can't get the pools of a subnet pool
     */
    @WebMethod(operationName = "getSubnetPools")
    public RemotePool[] getSubnetPools(@WebParam(name = "parentId") String parentId,
            @WebParam(name = "className") String className,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException{
        try{
            return wsBean.getSubnetPools(parentId, className, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSubnetPools: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the subnets of a given pool id
     * @param poolId subnet pool id
     * @param limit limit of returned subnets, -1 to no limit
     * @param sessionId the session token
     * @return a set of subnets
     * @throws ServerSideException if the given subnet pool id is not valid
     */
    @WebMethod(operationName = "getSubnets")
    public List<RemoteObjectLight> getSubnets(@WebParam(name = "poolId")String poolId,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getSubnets(poolId, limit, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSubnets: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates a pool of subnets if the parentId is -1 the pool will be created 
     * in the default root for pools of subnets
     * @param parentId subnet parent Id, -1 to if want to create the pool in the root node
     * @param subnetPoolName subnet pool name
     * @param subnetPoolDescription subnet pool description
     * @param className if is a IPv4 or an IPv6 subnet
     * @param sessionId session token
     * @return id of the created subnet pool 
     * @throws ServerSideException if the IPAM root nodes doesn't exists, or if the IPv4 or IPv6 classes doesn't exists
     */
    @WebMethod(operationName = "createSubnetPool")
    public String createSubnetPool(
            @WebParam(name = "parentId")String parentId, 
            @WebParam(name = "subnetPoolName")String subnetPoolName, 
            @WebParam(name = "subnetPoolDescription")String subnetPoolDescription, 
            @WebParam(name = "className")String className, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.createSubnetPool(parentId, subnetPoolName, subnetPoolDescription, className, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createSubnetPool: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates a subnet
     * @param poolId The id of the pool that will contain the subnet
     * @param className The class name of the subnet (e.g. SubnetIPv4, SubnetIPv6)
     * @param attributes The attributes that will be set on the newly created element as a string-based key-value dictionary
     * @param sessionId Session token.
     * @return The id of the new subnet.
     * @throws ServerSideException If something goes wrong, can't find the 
     * parent id, IPv4 or IPv6 classes doesn't exists, or some problem with 
     * attributes, different size between attribute names and attribute values.
     */
    @WebMethod(operationName = "createSubnet")
    public String createSubnet(@WebParam(name = "poolId")String poolId,
            @WebParam(name = "className")String className,
            @WebParam(name = "attributes")List<StringPair> attributes,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.createSubnet(poolId, className, attributes, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createSubnet: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a set of subnet pools
     * @param ids ids of the pools to be deleted
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteSubnetPools")
    public void deleteSubnetPools(@WebParam(name = "ids")String[] ids,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.deleteSubnetPools(ids, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteSubnetPools: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
     /**
     * Deletes a subnet. All subnets must be instances of the same class
     * @param oids The ids of the subnets to be deleted
     * @param className The subnet class
     * @param releaseRelationships Should the deletion be forced, deleting all the relationships?
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteSubnets")
    public void deleteSubnets(@WebParam(name = "className") String className,
            @WebParam(name = "oids")List<String> oids,
            @WebParam(name = "releaseRelationships") boolean releaseRelationships,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try {
            wsBean.deleteSubnets(className, oids, releaseRelationships, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteSubnets: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
      * Gets the complete information about a given subnet (all its attributes)
      * @param id Subnet id
      * @param className Subnet class IPv4 o IPv6
      * @param sessionId Session token
      * @return a representation of the subnet as a RemoteObject
      * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
      */
    @WebMethod(operationName = "getSubnet")
    public RemoteObject getSubnet(
            @WebParam(name = "id") String id,
            @WebParam(name = "className") String className,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getSubnet(id, className, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSubnet: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
      * Gets the complete information about a given subnet pool (all its attributes)
      * @param subnetPoolId Subnet pool id
      * @param sessionId Session token
      * @return a representation of the entity as a RemoteObject
      * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
      */
    @WebMethod(operationName = "getSubnetPool")
    public RemotePool getSubnetPool(
            @WebParam(name = "subnetPoolId") String subnetPoolId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{

        try{
            return wsBean.getSubnetPool(subnetPoolId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSubnetPool: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Adds an IP address to a Subnet
     * @param id ipAddres id
     * @param parentClassName the parent class name
     * @param attributes IP address attributes as a String based key-value dictionary
     * @param sessionId The session token
     * @return the id of the new IP Address
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "addIPAddress")
    public String addIPAddress(@WebParam(name = "id")String id,
            @WebParam(name = "parentClassName")String parentClassName,
            @WebParam(name = "attributes")List<StringPair> attributes,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.addIPAddress(id, parentClassName, attributes, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in addIPAddress: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Removes a set of IP Addresses from a subnet. Note that this method must be used only for Subnet objects
     * @param oids ids of the IPs to be deleted
     * @param releaseRelationships Should the deletion be forced, deleting all the relationships?
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "removeIP")
    public void removeIP(
            @WebParam(name = "oid")String[] oids,
            @WebParam(name = "releaseRelationships") boolean releaseRelationships,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.removeIP(oids, releaseRelationships, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in removeIP: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    /**
     * Retrieves the IP addresses of a subnet
     * @param id subnet id
     * @param limit limit of returned subnets
     * @param className the class name
     * @param sessionId the session id
     * @return a set of IPs
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "getSubnetUsedIps")
    public List<RemoteObjectLight> getSubnetUsedIps(@WebParam(name = "id")String id,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "className")String className,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getSubnetUsedIps(id, className, limit, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSubnetUsedIps: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Retrieves the sub-subnets of a subnet
     * @param id subnet id
     * @param limit limit of returned subnets
     * @param className the class name if is IPv6 or IPv4
     * @param sessionId The session token
     * @return a set of subnets
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "getSubnetsInSubnet")
    public List<RemoteObjectLight> getSubnetsInSubnet(@WebParam(name = "id")String id,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "className")String className,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getSubnetsInSubnet(id, className, limit, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSubnetsInSubnet: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates a relation between a Subnet and a VLAN, this method is also using to 
     * associate VFRs, and BDIs to a VLAN  
     * TODO: check the model, there are redundant relationships
     * @param id Subnet id
     * @param className if the subnet has IPv4 or IPv6 IP addresses
     * @param vlanId VLAN id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "relateSubnetToVlan")
    public void relateSubnetToVlan (
            @WebParam(name = "id")String id,
            @WebParam(name = "className")String className,
            @WebParam(name = "vlanId")String vlanId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.relateSubnetToVlan(id, className, vlanId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in relateSubnetToVlan: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Releases the relation between a subnet and a VLAN, this method is also using 
     * to release VFRs, and BDIs from a VLAN  
     * TODO: check the model there are redundant relationships 
     * @param subnetId Subnet id
     * @param vlanId the VLAN id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime    
     */
    @WebMethod(operationName = "releaseSubnetFromVlan")
    public void releaseSubnetFromVlan (
            @WebParam(name = "subnetId")String subnetId,
            @WebParam(name = "vlanId")String vlanId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.releaseSubnetFromVlan(vlanId, subnetId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in releaseSubnetFromVlan: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Releases the relation between a subnet and a VRF
     * @param subnetId Subnet id
     * @param vrfId the VRF id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime    
     */
    @WebMethod(operationName = "releaseSubnetFromVRF")
    public void releaseSubnetFromVRF (
            @WebParam(name = "subnetId")String subnetId,
            @WebParam(name = "vrfId")String vrfId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.releaseSubnetFromVRF(subnetId, vrfId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in releaseSubnetFromVRF: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates a relation between a subnet and a VRF
     * @param id Subnet id
     * @param className if the subnet is IPv4 or IPv6
     * @param vrfId VRF id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "relateSubnetToVrf")
    public void relateSubnetToVrf (
            @WebParam(name = "id")String id,
            @WebParam(name = "className")String className,
            @WebParam(name = "vrfId")String vrfId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.relateSubnetToVrf(id, className, vrfId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in relateSubnetToVrf: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates a relation between an IP address and a port
     * @param id IP address id
     * @param portClassName port class
     * @param portId port id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "relateIPtoPort")
    public void relateIPtoPort (
            @WebParam(name = "id")String id,
            @WebParam(name = "portClassName")String portClassName,
            @WebParam(name = "portId")String portId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.relateIPtoPort(id, portClassName, portId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in relateIPtoPort: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Checks if a new subnet overlaps with an existing one
     * @param networkIp the network ip for the subnet
     * @param broadcastIp the broadcast ip for the subnet
     * @param sessionId Session token
     * @return true if overlaps
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "itOverlaps")
    public boolean itOverlaps (
            @WebParam(name = "networkIp")String networkIp,
            @WebParam(name = "broadcastIp")String broadcastIp,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            return wsBean.itOverlaps(networkIp, broadcastIp, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in itOverlaps: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Releases the relation between an IP address and a port
     * @param deviceClassName port class name
     * @param deviceId port id
     * @param id Subnet id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime    
     */
    @WebMethod(operationName = "releasePortFromIP")
    public void releasePortFromIP (
            @WebParam(name = "deviceClassName")String deviceClassName,
            @WebParam(name = "deviceId")String deviceId,
            @WebParam(name = "id")String id,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.releasePortFromIP(deviceClassName, deviceId, id, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in releasePortFromIP: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
        //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Contract Manager">
    /**
     * Associates a list of objects (instances of a subclass of InventoryObject) to an existing contract (most probably a support contract)
     * @param objectClass Object class
     * @param objectId Object id
     * @param contractClass contract class
     * @param contractId contract id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If any of the objects can't be found
     *                             If any of the objects involved can't be associated
     *                             If any of the classes provided can not be found
     */
    @WebMethod(operationName = "associateObjectsToContract")
    public void associateObjectsToContract (
            @WebParam(name = "objectClass")String[] objectClass,
            @WebParam(name = "objectId")String[] objectId,
            @WebParam(name = "contractClass")String contractClass,
            @WebParam(name = "contractId")String contractId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.associateObjectsToContract(objectClass, objectId, contractClass, contractId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in associateObjectsToContract: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }    
    
    /**
     * Releases an inventory object from a contract it was related to
     * @param objectClass Class of the inventory object
     * @param objectId Id of the inventory object
     * @param contractId Contract id
     * @param sessionId Session token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object can not be found
     *                             If the class can not be found
     */
    @WebMethod(operationName = "releaseObjectFromContract")
    public void releaseObjectFromContract (
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")String objectId,
            @WebParam(name = "contractId")String contractId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.releaseObjectFromContract(objectClass, objectId, contractId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in releaseObjectFromContract: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }    
        // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="MPLS Module">
    /**
     * Creates an MPLS link
     * @param classNameEndpointA The class name of the endpoint A (some kind of port)
     * @param idEndpointA Id of endpoint A
     * @param classNameEndpointB  The class name of the endpoint Z (some kind of port)
     * @param idEndpointB Id of endpoint Z
     * @param attributesToBeSet Attributes to be set, e.g. mplsLink's name
     * @param sessionId Session token
     * @return The id of the newly created transport link
     * @throws ServerSideException If the given linkType is no subclass of GenericLogicalConnection
     *                             If any of the requested objects can't be found
     *                             If any of the classes provided can not be found
     *                             If any of the objects involved can't be connected
     */
    @WebMethod(operationName = "createMPLSLink")
    public String createMPLSLink(@WebParam(name = "classNameEndpointA") String classNameEndpointA, 
            @WebParam(name = "idEndpointA") String idEndpointA, 
            @WebParam(name = "classNameEndpointB") String classNameEndpointB, 
            @WebParam(name = "idEndpointB") String idEndpointB, 
            @WebParam(name="parameters") List<StringPair> attributesToBeSet,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.createMPLSLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, attributesToBeSet, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createMPLSLink: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
                
    /**
     * The details of a given mpls link
     * @param connectionId the mpls link id
     * @param sessionId Session token
     * @return An array of two positions: the first is the A endpoint and the second is the B endpoint
     * @throws ServerSideException f the given id class name is not MPLS Link
     *                             If any of the requested objects can't be found
     *                             If any of the classes provided can not be found
     *                             If any of the objects involved can't be connected  
     */
    @WebMethod(operationName = "getMPLSLinkEndpoints")
    public RemoteMPLSConnectionDetails getMPLSLinkEndpoints(@WebParam(name = "connectionId") String connectionId, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.getMPLSLinkEndpoints(connectionId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getMPLSLinkEndpoints: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
        
    /**
     * Connect a given mpls links with a given ports for every side
     * @param sideAClassNames end point side A class names
     * @param sideAIds end point side A ids
     * @param linksIds mpls links ids
     * @param sideBClassNames end point side B class names
     * @param sideBIds end point side B ids
     * @param sessionId session token
     * @throws ServerSideException If the given ports are not subclass of GenericPort
     *                             If the given link ids are not of the class MPLSLink
     *                             If any of the requested objects can't be found
     *                             If any of the classes provided can not be found
     *                             If any of the objects involved can't be connected  
     */
    @WebMethod(operationName = "connectMplsLink")
    public void connectMplsLink(@WebParam(name = "sideAClassNames") String[] sideAClassNames,
            @WebParam(name = "sideAIds") String[] sideAIds,
            @WebParam(name = "linksIds") String[] linksIds,
            @WebParam(name = "sideBClassNames") String[] sideBClassNames,
            @WebParam(name = "sideBIds") String[] sideBIds,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.connectMplsLink(sideAClassNames, sideAIds, linksIds, sideBClassNames, sideBIds, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in connectMplsLink: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * disconnect mpls link
     * @param connectionId mpls link id
     * @param sideToDisconnect which side will be disconnect 1 side A, 2 side B, 3 both sides 
     * @param sessionId session token
     * @throws ServerSideException If the given link id is not of the class MPLSLink
     *                             If any of the requested objects can't be found
     *                             If any of the classes provided can not be found
     *                             If any of the objects involved can't be connected 
     */
    @WebMethod(operationName = "disconnectMPLSLink")
    public void disconnectMPLSLink(@WebParam(name = "connectionId") String connectionId,
            @WebParam(name = "sideToDisconnect") int sideToDisconnect,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.disconnectMPLSLink(connectionId, sideToDisconnect, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in disconnectMPLSLink: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a MPLS link
     * @param linkId the id of the mpls link
     * @param forceDelete Ignore the existing relationships
     * @param sessionId Session token
     * @throws ServerSideException If the object can not be found
     *                             If either the object class or the attribute can not be found
     *                             If the class could not be found
     *                             If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     */
    @WebMethod(operationName = "deleteMPLSLink")
    public void deleteMPLSLink(@WebParam(name = "linkId") String linkId, 
            @WebParam(name = "forceDelete") boolean forceDelete, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.deleteMPLSLink(linkId, forceDelete, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteMPLSLink: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Associates any GenericCommunicationsPort to existing BridgeDomainInterface
     * @param portId MPLSTunnel or BridgeDomain or FrameRelay or VRF id
     * @param portClassName if is a MPLSTunnel or BridgeDomain or FrameRelay or VRF
     * @param interfaceClassName network element class name
     * @param interfaceId network element id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "relatePortToInterface")
    public void relatePortToInterface (
            @WebParam(name = "portId")String portId,
            @WebParam(name = "portClassName")String portClassName,
            @WebParam(name = "interfaceClassName")String interfaceClassName,
            @WebParam(name = "interfaceId")String interfaceId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.relatePortToInterface(portId, portClassName, interfaceClassName, interfaceId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in relatePortToInterface: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Release the association between a network element and a MPLSTunnel or BridgeDomain or 
     * FrameRelay or VRF
     * @param portId MPLSTunnel or BridgeDomain or FrameRelay or VRF id
     * @param interfaceClassName network element class name
     * @param interfaceId network element id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    
    //String networkElementClass, long networkElementId ,long id
    @WebMethod(operationName = "releasePortFromInterface")
    public void releasePortFromInterface (
            @WebParam(name = "interfaceClassName")String interfaceClassName,
            @WebParam(name = "interfaceId")String interfaceId,
            @WebParam(name = "portId")String portId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.releasePortFromInterface(interfaceClassName, interfaceId, portId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in releasePortFromInterface: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
        // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Projects Module">
    /**
     * Gets the project pools
     * @param sessionId Session id token
     * @return The list of project pools
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getProjectPools")
    public List<RemotePool> getProjectPools(@WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.getProjectPools(getIPAddress(), sessionId);
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getProjectPools: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Adds a Project
     * @param parentId Parent Id
     * @param parentClassName Parent class name
     * @param className Class name
     * @param attributeNames Attributes names
     * @param attributeValues Attributes values
     * @param sessionId Session id token
     * @return The Project id
     * @throws ServerSideException If any of the attributes or its type is invalid
     *                             If attributeNames and attributeValues have different sizes.
     *                             If the class name could not be found
     */
    @WebMethod(operationName = "addProject")
    public String addProject(
        @WebParam(name = "parentId") String parentId, 
        @WebParam(name = "parentClassName") String parentClassName, 
        @WebParam(name = "className") String className, 
        @WebParam(name = "attributeNames") String[] attributeNames, 
        @WebParam(name = "attributeValues") String[] attributeValues, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.addProject(parentId, parentClassName, className, attributeNames, attributeValues, getIPAddress(), sessionId);
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in addProject: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a Project
     * @param className Class name
     * @param oid Object id
     * @param releaseRelationships Release relationships
     * @param sessionId Session id token
     * @throws ServerSideException If the object couldn't be found
     *                             If the class could not be found
     *                             If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships
     */
    @WebMethod(operationName = "deleteProject")
    public void deleteProject(
        @WebParam(name = "className") String className, 
        @WebParam(name = "oid") String oid, 
        @WebParam(name = "releaseRelationships") boolean releaseRelationships, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            wsBean.deleteProject(className, oid, releaseRelationships, getIPAddress(), sessionId);
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteProject: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Adds an Activity
     * @param parentId Parent Id
     * @param parentClassName Parent class name
     * @param className Class name
     * @param attributeNames Attributes names
     * @param attributeValues Attributes values
     * @param sessionId Session id token
     * @return The Activity id
     * @throws ServerSideException If the object's class can't be found
     *                             If the parent id is not found
     *                             If any of the attribute values has an invalid value or format
     *                             If the update can't be performed due to a format issue
     *                             If attributeNames and attributeValues have different sizes.
     */
    @WebMethod(operationName = "addActivity")
    public String addActivity(
        @WebParam(name ="parentId") String parentId, 
        @WebParam(name ="parentClassName") String parentClassName, 
        @WebParam(name ="className") String className, 
        @WebParam(name ="attributeNames") String[] attributeNames, 
        @WebParam(name ="attributeValues") String[] attributeValues, 
        @WebParam(name ="sessionId") String sessionId) throws ServerSideException {
        
        try {
            return wsBean.addActivity(parentId, parentClassName, className, attributeNames, attributeValues, getIPAddress(), sessionId);
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in addActivity: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes an Activity
     * @param className Class name
     * @param oid Object id
     * @param releaseRelationships Release relationships
     * @param sessionId Session id token
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the object couldn't be found
     *                             If the class could not be found
     *                             If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships
     */
    @WebMethod(operationName = "deleteActivity")
    public void deleteActivity(
        @WebParam(name ="className") String className, 
        @WebParam(name ="oid") String oid, 
        @WebParam(name ="releaseReltationships") boolean releaseRelationships, 
        @WebParam(name ="sessionId") String sessionId) throws ServerSideException {
        
        try {        
            wsBean.deleteActivity(className, oid, releaseRelationships, getIPAddress(), sessionId);
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deleteActivity: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
        
    }   

    /**
     * Gets the project in a Project pool
     * @param poolId Project pool id
     * @param limit Max number of results, no limit with -1
     * @param sessionId Session id token
     * @return An array of projects in a project pool
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the Project pool is not found
     */
    @WebMethod(operationName = "getProjectsInProjectPool")
    public List<RemoteObjectLight> getProjectsInProjectPool(
        @WebParam(name = "poolId") String poolId, 
        @WebParam(name = "limit") int limit, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.getProjectsInProjectPool(poolId, limit, getIPAddress(), sessionId);
        } catch(Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getProjectsInProjectPool: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
        
    /**
     * Gets the resources (objects) associates with a Project
     * @param projectClass Project class
     * @param projectId Project id
     * @param sessionId Session id
     * @return An array of resources
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the project is not subclass of GenericProject
     */
    @WebMethod(operationName = "getProjectResurces")
    public List<RemoteObjectLight> getProjectResurces(
        @WebParam(name = "projectClass") String projectClass, 
        @WebParam(name = "projectId") String projectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            return wsBean.getProjectResurces(projectClass, projectId, getIPAddress(), sessionId);
           
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getProjectResurces: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }      
    }
    
    /**
     * Gets project activities
     * @param projectClass Project class
     * @param projectId Project Id
     * @param sessionId Session Id
     * @return An array of activities
     * @throws ServerSideException If the user is not allowed to invoke the method
     *                             If the project is not subclass of GenericProject
     *                             If the project class is not found
     *                             If the project is not found
     */
    @WebMethod(operationName = "getProjectActivities")
    public List<RemoteObjectLight> getProjectActivities(
        @WebParam(name = "projectClass") String projectClass, 
        @WebParam(name = "projectId") String projectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            return wsBean.getProjectActivities(projectClass, projectId, getIPAddress(), sessionId);
            
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getProjectActivities: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }  
    }   
        
    /**
     * Associates a set of objects with a Project
     * @param projectClass Project class
     * @param projectId Project id
     * @param objectClass Object class
     * @param objectId Object id
     * @param sessionId Session id token
     * @throws ServerSideException Generic If the project is not subclass of GenericProject
     *                                     If array sizes of objectClass and objectId are not the same
     */
    @WebMethod(operationName = "associateObjectsToProject")
    public void associateObjectsToProject(
        @WebParam(name = "projectClass") String projectClass, 
        @WebParam(name = "projectId") String projectId, 
        @WebParam(name = "objectClass") String[] objectClass, 
        @WebParam(name = "objectId") String[] objectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            wsBean.associateObjectsToProject(projectClass, projectId, objectClass, objectId, getIPAddress(), sessionId);
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in associateObjectsToProject: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        } 
    }
    
    /**
     * Associates an object to a Project
     * @param projectClass Project class
     * @param projectId Project id
     * @param objectClass Object class
     * @param objectId Object id
     * @param sessionId Session id token
     * @throws ServerSideException If the project is not subclass of GenericProject
     */
    @WebMethod(operationName = "associateObjectToProject")
    public void associateObjectToProject(
        @WebParam(name = "projectClass") String projectClass, 
        @WebParam(name = "projectId") String projectId, 
        @WebParam(name = "objectClass") String objectClass, 
        @WebParam(name = "objectId") String objectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            wsBean.associateObjectToProject(projectClass, projectId, objectClass, objectId, getIPAddress(), sessionId);
            
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in associateObjectToProject: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        } 
    }
        
    /**
     * Releases an object associated to a Project
     * @param objectClass Object class
     * @param objectId Object id
     * @param projectClass Project class
     * @param projectId Project id
     * @param sessionId Session id token
     * @throws ServerSideException If the project is not subclass of GenericProject
     */    
    @WebMethod(operationName = "freeObjectFromProject")
    public void freeObjectFromProject(
        @WebParam(name = "objectClass") String objectClass, 
        @WebParam(name = "objectId") String objectId, 
        @WebParam(name = "projectClass") String projectClass, 
        @WebParam(name = "projectId") String projectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            wsBean.releaseObjectFromProject(objectClass, objectId, projectClass, projectId, getIPAddress(), sessionId);
            
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in freeObjectFromProject: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the projects associate to an object
     * @param objectClass Object class
     * @param objectId Object id
     * @param sessionId The session id token
     * @return An array of projects
     * @throws ServerSideException If the project is no found
     *                             If the project class is no found
     */
    @WebMethod(operationName = "getProjectsAssociateToObject")
    public List<RemoteObjectLight> getProjectsAssociateToObject(
        @WebParam(name = "objectClass") String objectClass, 
        @WebParam(name = "ObjectId") String objectId, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        
        try {
            return wsBean.getProjectsAssociateToObject(objectClass, objectId, getIPAddress(), sessionId);
           
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getProjectsAssociateToObject: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }      
    }
    
    /**
     * Creates a Project Pool
     * @param name Project Pool name
     * @param description Project Pool description
     * @param instanceOfClass Project Pool class
     * @param sessionId Session id token
     * @return The id of the new Project Pool
     * @throws ServerSideException If he project pool class is no found
     */
    @WebMethod(operationName = "createProjectPool")
    public String createProjectPool(
        @WebParam(name = "name") String name, 
        @WebParam(name = "description") String description, 
        @WebParam(name = "instanceOfClass") String instanceOfClass, 
        @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.createProjectPool(name, description, instanceOfClass, getIPAddress(), sessionId);
            
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createProjectPool: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
        // </editor-fold>
    
        //<editor-fold desc="Fault Management Integration" defaultstate="collapsed">
    /**
     * Retrieves the services affected when a given network resource is alarmed (or down). 
     * The services associated directly to the given resource are returned first, but depending on the resource type, the analysis might be more extensive. 
     * If the resource is a link (logical or physical), the services associated to the endpoint ports are also returned. 
     * If the the resource is a container (physical or logical), the services associated to the contained links are also returned. 
     * If the resource is a network equipment, the services associated directly to the ports contained and their connections are also returned.
     * @param resourceType Use 1 for hardware and 2 for logical links
     * @param resourceDefinition A semi-colon (;) separated string. The first segment (mandatory) is the name of the affected element, 
     * the second is the number of the slot (optional) and the third is the port (optional). Note that to address a logical connection, the 
     * resource definition will contain only the name of such connection.
     * @param sessionId Session token
     * @return A compact summary with the full information about the device/interface provided, the related services and their customers
     * @throws ServerSideException If the resource could not be found or if the resource definition/resource type is not valid
     */
    @WebMethod(operationName = "getAffectedServices")
    public AssetLevelCorrelatedInformation getAffectedServices(@WebParam(name = "resourceType")int resourceType,
            @WebParam(name = "resourceDefinition")String resourceDefinition, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.getAffectedServices(resourceType, resourceDefinition, getIPAddress(), sessionId);
        } catch (Exception ex) {
            if (ex instanceof ServerSideException)
                throw ex;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getAffectedServices: " + ex.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
        //</editor-fold>
    
        //<editor-fold desc="Inventory Synchronization" defaultstate="collapsed">
        /**
         * Get the set of sync providers defined in the configuration variables pool called -Sync Providers-
         * @param sessionId Session token
         * @return The set of sync providers defined in the configuration variables pool called -Sync Providers-
         * @throws ServerSideException If the pool could not be found
         * If the value of the variable could not be successfully translated into a java type variable
         * If no configuration variable with that name could be found
         */
        @WebMethod(operationName = "getSynchronizationProviders")
        public List<RemoteSynchronizationProvider> getSynchronizationProviders(
            @WebParam(name="sessionId") String sessionId)throws ServerSideException {
            try {
                return wsBean.getSynchronizationProviders(getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in getSynchronizationProviders: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            } 
        }
    
        /**
         * Creates a Synchronization Data Source Configuration. A Sync data source configuration is a set of parameters 
         * used to connect to a sync data source (usually IPs, paths, etc)
         * @param objectId Id of the object that the configuration is attached to         
         * @param syncGroupId Id of the sync group this configuration is related to         
         * @param name The name of the new sync data source configuration
         * @param parameters The list of parameters to be stored as pairs name/value. 
         * Note that the Sync provider provides metadata definition to check if the number 
         * and format of the parameters correct, so it can be checked at server side
         * @param sessionId Session token
         * @return The id of the newly created sync config
         * @throws ServerSideException If the sync group could not be found or if 
         * the any of the parameters does not comply with the expected format
         */
        @WebMethod(operationName = "createSynchronizationDataSourceConfig")
        public long createSynchronizationDataSourceConfig(
                @WebParam(name="objectId")String objectId, 
                @WebParam(name="syncGroupId")long syncGroupId,
                @WebParam(name="name")String name, 
                @WebParam(name="parameters")List<StringPair> parameters, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                return wsBean.createSynchronizationDataSourceConfig(objectId, syncGroupId, name, parameters, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in createSynchronizationDataSourceConfig: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            } 
        }
    
        /**
         * Creates a Synchronization Group. A Sync Group is a set of Synchronization Configurations that will be processed by the same
         * Synchronization Provider. Take into account that the schedule for the SG to be executed is not configured here, but in Task Manager's task
         * @param name The name of the new sync group
         * @param sessionId Session token
         * @return The id of the newly created sync group
         * @throws ServerSideException If the name or the sync provider are invalid 
         */
        @WebMethod(operationName = "createSynchronizationGroup")
        public long createSynchronizationGroup(@WebParam(name="name")String name, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                return wsBean.createSynchronizationGroup(name, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in createSynchronizationGroup: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            } 
        }
        
         /**
         * Creates a Synchronization Group. A Sync Group is a set of Synchronization Configurations that will be processed by the same
         * Synchronization Provider. Take into account that the schedule for the SG to be executed is not configured here, but in Task Manager's task
         * @param syncGroupId The name of the new sync group
         * @param syncGroupProperties The synchronization group properties
         * @param sessionId Session token
         * @throws ServerSideException If the name or the sync provider are invalid 
         */
        @WebMethod(operationName = "updateSynchronizationGroup")
        public void updateSynchronizationGroup(@WebParam(name="syncGroupId") long syncGroupId,
                @WebParam(name="syncGroupProperties")List<StringPair> syncGroupProperties, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                wsBean.updateSynchronizationGroup(syncGroupId, syncGroupProperties, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in updateSynchronizationGroup: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            } 
        }
        
        /**
         * Gets a given sync group
         * @param syncGroupId The sync group id
         * @param sessionId Session token
         * @return The requested sync group
         * @throws ServerSideException If the sync group could not be found
         */
        @WebMethod(operationName = "getSynchronizationGroup")
        public RemoteSynchronizationGroup getSynchronizationGroup(
                @WebParam(name="syncGroupId")long syncGroupId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                //return wsBean.getSynchronizationGroup(syncGroupId, getIPAddress(), sessionId);
                return null;
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in getSynchronizationGroup: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            } 
        }
        
        /**
         * Gets the available sync groups
         * @param sessionId Session token
         * @return The list of available sync groups
         * @throws ServerSideException If something unexpected goes wrong
         */
        @WebMethod(operationName = "getSynchronizationGroups")
        public List<RemoteSynchronizationGroup> getSynchronizationGroups(
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                return wsBean.getSynchronizationGroups(getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in getSynchronizationGroups: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            } 
        }
        
        /**
         * Gets the synchronization data source configurations for an object
         * @param objectId the objectId
         * @param sessionId Session token
         * @return The list of available sync groups
         * @throws ServerSideException If something unexpected goes wrong
         */
        @WebMethod(operationName = "getSyncDataSourceConfiguration")
        public RemoteSynchronizationConfiguration getSyncDataSourceConfiguration(
                @WebParam(name="objectId")String objectId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                return wsBean.getSyncDataSourceConfiguration(objectId, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in getSyncDataSourceConfiguration: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            } 
        }
        
        /**
         * Gets the synchronization data source configurations for a sync group
         * @param syncGroupId the syncGroupId
         * @param sessionId Session token
         * @return The list of available sync groups
         * @throws ServerSideException If something unexpected goes wrong
         */
        @WebMethod(operationName = "getSyncDataSourceConfigurations")
        public List<RemoteSynchronizationConfiguration> getSyncDataSourceConfigurations(
                @WebParam(name="syncGroupId")long syncGroupId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                return wsBean.getSyncDataSourceConfigurations(syncGroupId, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in getSyncDataSourceConfigurations: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            } 
        }
        
        /**
         * Updates a sync data source configuration
         * @param syncDataSourceConfigId the sync source configuration Id
         * @param parameters the updated parameters
         * @param sessionId session token 
         * @throws ServerSideException If the sync data source could not be found or if 
         * the any of the parameters does not comply with the expected format
         */
        @WebMethod(operationName = "updateSyncDataSourceConfiguration")
        public void updateSyncDataSourceConfiguration(
                @WebParam(name="syncDataSourceConfigId")long syncDataSourceConfigId, 
                @WebParam(name="parameters")List<StringPair> parameters, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                wsBean.updateSyncDataSourceConfiguration(syncDataSourceConfigId, parameters, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in updateSyncDataSourceConfiguration: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            } 
        }
        
        /**
         * Deletes a synchronization group and all the sync configurations associated to it
         * @param syncGroupId The id of the group
         * @param sessionId Session token
         * @throws ServerSideException If the group could not be found
         */
        @WebMethod(operationName = "deleteSynchronizationGroup")
        public void deleteSynchronizationGroup(@WebParam(name="syncGroupId")long syncGroupId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                wsBean.deleteSynchronizationGroup(syncGroupId, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in deleteSynchronizationGroup: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            } 
        }
        
        /**
         * Deletes a sync data source configuration.
         * @param syncDataSourceConfigId The id of the configuration
         * @param sessionId Session token
         * @throws ServerSideException If the config could not be found
         */
        @WebMethod(operationName = "deleteSynchronizationDataSourceConfig")
        public void deleteSynchronizationDataSourceConfig(@WebParam(name="syncDataSourceConfigId")long syncDataSourceConfigId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                wsBean.deleteSynchronizationDataSourceConfig(syncDataSourceConfigId, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in deleteSynchronizationDataSourceConfig: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            } 
        }
        
        /**
         * Copy a set of sync group
         * @param syncGroupIds The array of sync groups ids to copy
         * @param sessionId Session Token
         * @return A list of new sync groups
         * @throws ServerSideException If some of the sync group cannot be found or If the provider of the sync group cannot be found
         *                             If the sync group is malformed
         */
//        @WebMethod(operationName = "copySyncGroup")        
//        public List<RemoteSynchronizationGroup> copySyncGroup(
//            @WebParam(name="syncGroupIds") long[] syncGroupIds, 
//            @WebParam(name="sessionId") String sessionId) throws ServerSideException {
//            try {
//                return wsBean.copySyncGroup(syncGroupIds, getIPAddress(), sessionId);
//            } catch (Exception ex) {
//                if (ex instanceof ServerSideException)
//                    throw ex;
//                else {
//                    System.out.println("[KUWAIBA] An unexpected error occurred in copySyncGroup: " + ex.getMessage());
//                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
//                }
//            }
//        }
        
        /**
         * Creates "copy" a relation between a set of sync data source configurations and a given sync group
         * @param syncGroupId The Sync Group Id target
         * @param syncDataSourceConfigurationIds Set of sync data source configuration ids
         * @param sessionId Session Token
         * @throws ServerSideException If the sync group cannot be found, or some sync data source configuration cannot be found
         *                             If the sync group is malformed, or some sync data source configuration is malformed
         */
        @WebMethod(operationName = "copySyncDataSourceConfiguration")
        public void copySyncDataSourceConfiguration(
            @WebParam(name="syncGroupId") long syncGroupId, 
            @WebParam(name="syncDataSourceConfigurationId") long[] syncDataSourceConfigurationIds, 
            @WebParam(name="sessionId") String sessionId) throws ServerSideException {
            try {
                wsBean.copySyncDataSourceConfiguration(syncGroupId, syncDataSourceConfigurationIds, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in copySyncDataSourceConfiguration: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        
        /**
         * Release a set of sync data source configuration from a given sync group
         * @param syncGroupId The Sync Group Id target
         * @param syncDataSourceConfigurationIds Set of sync data source configuration ids
         * @param sessionId Session Token
         * @throws ServerSideException If the sync group cannot be found, or some sync data source configuration cannot be found
         *                             If the sync group is malformed, or some sync data source configuration is malformed
         */
        @WebMethod(operationName = "releaseSyncDataSourceConfigFromSyncGroup")
        public void releaseSyncDataSourceConfigFromSyncGroup(
            @WebParam(name="syncGroupId") long syncGroupId, 
            @WebParam(name="syncDataSourceConfigurationId") long[] syncDataSourceConfigurationIds, 
            @WebParam(name="sessionId") String sessionId) throws ServerSideException {
            try {
                wsBean.releaseSyncDataSourceConfigFromSyncGroup(syncGroupId, syncDataSourceConfigurationIds, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in releaseSyncDataSourceConfigFromSyncGroup: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }

        /**
         * Moves a sync data source configuration from a sync group to another sync group
         * @param oldSyncGroupId The Sync Group Id target to related
         * @param newSyncGroupId The Sync Group Id target to release
         * @param syncDataSourceConfigurationIds Set of sync data source configuration ids
         * @param sessionId Session token
         * @throws ServerSideException If the sync group cannot be found, or some sync data source configuration cannot be found
         *                             If the sync group is malformed, or some sync data source configuration is malformed
         */
        @WebMethod(operationName = "moveSyncDataSourceConfiguration")
        public void moveSyncDataSourceConfiguration(
            @WebParam(name="oldSyncGroupId") long oldSyncGroupId, 
            @WebParam(name="newSyncGroupId") long newSyncGroupId, 
            @WebParam(name="syncDataSourceConfiguration") long[] syncDataSourceConfigurationIds, 
            @WebParam(name="sessionId") String sessionId) throws ServerSideException {
            try {
                wsBean.moveSyncDataSourceConfiguration(oldSyncGroupId, newSyncGroupId, syncDataSourceConfigurationIds, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in moveSyncDataSourceConfiguration: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        //</editor-fold>
        
        //<editor-fold desc="Process Manager" defaultstate="collapsed">
        /**
         * Creates a process definition. A process definition is the metadata that defines the steps and constraints 
         * of a given project
         * @param name The name of the new process definition
         * @param description The description of the new process definition
         * @param version The version of the new process definition. This is a three numbers, dot separated string (e.g. 2.4.1)
         * @param enabled If the project is enabled to create instances from it
         * @param structure The structure of the process definition. It's an XML document that represents a BPMN process definition
         * @param sessionId The session token
         * @return The id of the newly created process definition
         * @throws ServerSideException If the process structure defines a malformed process or if the version is invalid
         */
        @WebMethod(operationName = "createProcessDefinition")
        public long createProcessDefinition(@WebParam(name = "name")String name, 
                @WebParam(name="description")String description, @WebParam(name="version")String version, 
                @WebParam(name="enabled")boolean enabled, @WebParam(name="structure")byte[] structure, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                return wsBean.createProcessDefinition(name, description, version, enabled, structure, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in createProcessDefinition: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        
        /**
         * Updates a process definition, either its standard properties or its structure
         * @param processDefinitionId The process definition id
         * @param properties A key value dictionary with the standard properties to be updated. These properties are: name, description, version and enabled (use 'true' or 'false' for the latter)
         * @param structure A byte array withe XML process definition body
         * @param sessionId The session token
         * @throws ServerSideException If the structure is invalid, or the process definition could not be found or one of the properties is malformed or have an unexpected name
         */
        @WebMethod(operationName = "updateProcessDefinition")
        public void updateProcessDefinition(@WebParam(name="processDefinitionId")long processDefinitionId, 
                @WebParam(name="properties")List<StringPair> properties, @WebParam(name="structure")byte[] structure, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                wsBean.updateProcessDefinition(processDefinitionId, properties, structure, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in updateProcessDefinition: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        
        /**
         * Deletes a process definition
         * @param processDefinitionId The process definition to be deleted
         * @param sessionId The session token
         * @throws ServerSideException If the process definition could not be found or if there are process instances related to the process definition
         */
        @WebMethod(operationName = "deleteProcessDefinition")
        public void deleteProcessDefinition(@WebParam(name="processDefinitionId")long processDefinitionId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                wsBean.deleteProcessDefinition(processDefinitionId, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in deleteProcessDefinition: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        
        /**
         * Retrieves a process definition
         * @param processDefinitionId The id of the process
         * @param sessionId The session token
         * @return The process definition. It contains an XML document to be parsed by the consumer
         * @throws ServerSideException If the process could not be found or if it's malformed
         */
        @WebMethod(operationName = "getProcessDefinition")
        public RemoteProcessDefinition getProcessDefinition(@WebParam(name="processDefinitionId")long processDefinitionId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                return wsBean.getProcessDefinition(processDefinitionId, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in getProcessDefinition: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        
        /**
         * Creates an instance of a process, that is, starts one
         * @param processDefinitionId The id of the process to be started
         * @param processInstanceName The name of the new process
         * @param processInstanceDescription The description of the new process
         * @param sessionId The session token
         * @return The id of the newly created process instance
         * @throws ServerSideException If the process definition could not be found or if it's disabled
         */
        @WebMethod(operationName = "createProcessInstance")
        public long createProcessInstance(@WebParam(name="processDefinitionId")long processDefinitionId, 
                @WebParam(name="processInstancename")String processInstanceName, 
                @WebParam(name="processInstanceDescription")String processInstanceDescription, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                return wsBean.createProcessInstance(processDefinitionId, processInstanceName, processInstanceDescription, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in createProcessInstance: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        
        /**
         * Requests for the next activity to be executed in a process instance.
         * @param processInstanceId The running process to get the next activity from
         * @param sessionId The session id
         * @return The activity definition
         * @throws ServerSideException If the process instance could not be found, or if the process already ended
         */
        @WebMethod(operationName = "getNextActivityForProcessInstance")
        public RemoteActivityDefinition getNextActivityForProcessInstance(@WebParam(name="processInstanceId")long processInstanceId,
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                return wsBean.getNextActivityForProcessInstance(processInstanceId, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in getNextActivityForProcessInstance: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        
        /**
         * Saves the artifact generated once an activity has been completed (for example, the user filled in a form). 
         * @param processInstanceId The process instance the activity belongs to
         * @param activityDefinitionId The activity id
         * @param artifact The artifact to be saved
         * @param sessionId The session token
         * @throws ServerSideException If the process could not be found, or if the activity had been already executed, 
         * or if the activity definition could not be found, or of there's a mismatch in the artifact versions or if the user is not an authorized actor to carry on with the activity
         */
        @WebMethod(operationName = "commitActivity")
        public void commitActivity(@WebParam(name="processInstanceId")long processInstanceId, 
                @WebParam(name="activityDefinitionId")long activityDefinitionId, 
                @WebParam(name="artifact")RemoteArtifact artifact, @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                wsBean.commitActivity(processInstanceId, activityDefinitionId, artifact, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in commitActivity: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        
        /**
         * Given an activity definition, returns the artifact definition associated to it
         * @param processDefinitionId The id of the process the activity is related to
         * @param activityDefinitionId The id of the activity
         * @param sessionId The session token
         * @return An object containing the artifact definition
         * @throws ServerSideException If the process or the activity could not be found
         */
        @WebMethod(operationName = "getArtifactDefinitionForActivity")
        public RemoteArtifactDefinition getArtifactDefinitionForActivity(@WebParam(name="processDefinitionId")long processDefinitionId, 
                @WebParam(name="activityDefinitionId")long activityDefinitionId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                return wsBean.getArtifactDefinitionForActivity(processDefinitionId, activityDefinitionId, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in getArtifactDefinitionForActivity: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        
        /**
         * Gets the artifact associated to an activity (for example, a form that was already filled in by a user in a previous, already committed activity)
         * @param processInstanceId The id of the process instance. This process may have been ended already.
         * @param activityId The id of the activity the artifact belongs to
         * @param sessionId The session token
         * @return The artifact corresponding to the given activity
         * @throws ServerSideException If the process instance or activity couldn't be found.
         */
        @WebMethod(operationName = "getArtifactForActivity")
        public RemoteArtifact getArtifactForActivity(@WebParam(name="processinstanceId")long processInstanceId, 
                @WebParam(name="activityId")long activityId, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                return wsBean.getArtifactForActivity(processInstanceId, activityId, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in getArtifactForActivity: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        //</editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc="Warehouse Module">
        /**
         * Gets the warehouse module root pools
         * @param sessionId Session token
         * @return the warehouse module root pools
         * @throws ServerSideException If the class Warehouse or VirtualWatehouse not exist
         */
        @WebMethod(operationName = "getWarehouseRootPools")
        public List<RemotePool> getWarehouseRootPools(
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
            
            try {
                return wsBean.getWarehouseRootPool(getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in getWarehouseRootPools: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        /**
         * Associates an object (a resource) to an existing warehouse or virtual warehouse
         * @param objectClass Object class
         * @param objectId Object id
         * @param warehouseClass Warehouse class
         * @param warehouseId Warehouse id
         * @param sessionId Session token
         * @throws ServerSideException If the user is not allowed to invoke the method
         *                             If any of the objects can't be found
         *                             If any of the objects involved can't be connected (i.e. if it's not an inventory object)
         *                             If any of the classes provided can not be found
         */
        @WebMethod(operationName = "associatePhysicalNodeToWarehouse")
        public void associatePhysicalNodeToWarehouse (
                @WebParam(name = "objectClass")String objectClass,
                @WebParam(name = "objectId")String objectId,
                @WebParam(name = "warehouseClass")String warehouseClass,
                @WebParam(name = "warehouseId")String warehouseId,
                @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
            try{
                wsBean.associatePhysicalNodeToWarehouse(objectClass, objectId, warehouseClass, warehouseId, getIPAddress(), sessionId);
            } catch(Exception e){
                if (e instanceof ServerSideException)
                    throw e;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in associatePhysicalNodeToWarehouse: " + e.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }

         /**
         * Associates a list of objects (resources) to an existing warehouse or virtual warehouse
         * @param objectClass Object class
         * @param objectId Object id
         * @param warehouseClass Warehouse class
         * @param warehouseId Warehouse id
         * @param sessionId Session token
         * @throws ServerSideException If the user is not allowed to invoke the method
         *                             If any of the objects can't be found
         *                             If any of the objects involved can't be connected (i.e. if it's not an inventory object)
         *                             If any of the classes provided can not be found
         */
        @WebMethod(operationName = "associatesPhysicalNodeToWarehouse")
        public void associatesPhysicalNodeToWarehouse (
                @WebParam(name = "objectClass")String[] objectClass,
                @WebParam(name = "objectId")String[] objectId,
                @WebParam(name = "warehouseClass")String warehouseClass,
                @WebParam(name = "warehouseId")String warehouseId,
                @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
            try{
                wsBean.associatesPhysicalNodeToWarehouse(objectClass, objectId, warehouseClass, warehouseId, getIPAddress(), sessionId);
            } catch(Exception e){
                if (e instanceof ServerSideException)
                    throw e;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in associatesPhysicalNodeToWarehouse: " + e.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }

        /**
         * Releases an object from a warehouse or virtual warehouse that is using it
         * @param warehouseClass Warehouse class
         * @param warehouseId Warehouse id
         * @param targetId target object id
         * @param sessionId Session token
         * @throws ServerSideException If the user is not allowed to invoke the method
         *                             If the object can not be found
         *                             If the class can not be found
         *                             If the object activity log could no be found
         */
        @WebMethod(operationName = "releasePhysicalNodeFromWarehouse")
        public void releasePhysicalNodeFromWarehouse (
                @WebParam(name = "warehouseClass")String warehouseClass,
                @WebParam(name = "warehouseId")String warehouseId,
                @WebParam(name = "targetId")String targetId,           
                @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
            try{
                wsBean.releasePhysicalNodeFromWarehouse(warehouseClass, warehouseId, targetId, getIPAddress(), sessionId);
            } catch(Exception e){
                if (e instanceof ServerSideException)
                    throw e;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in releasePhysicalNodeFromWarehouse: " + e.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        /**
        * Moves objects from their current parent to a warehouse pool target object.
        * @param  targetClass New parent object id
        * @param targetOid The new parent's oid
        * @param objectClasses Class names of the objects to be moved
        * @param objectOids Oids of the objects to be moved
        * @param sessionId Session token
        * @throws ServerSideException If the object's or new parent's class can't be found
        *                             If the object or its new parent can't be found
        *                             If the update can't be performed due to a business rule
        */
        @WebMethod(operationName = "moveObjectsToWarehousePool")
        public void moveObjectsToWarehousePool(@WebParam(name = "targetClass")String targetClass,
                @WebParam(name = "targetOid")String targetOid,
                @WebParam(name = "objectsClasses")String[] objectClasses,
                @WebParam(name = "objectsOids")String[] objectOids,
                @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
            try{
                wsBean.moveObjectsToWarehousePool(targetClass,targetOid, objectClasses, objectOids, getIPAddress(), sessionId);
            } catch(Exception e){
                if (e instanceof ServerSideException)
                    throw e;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in moveObjectsToWarehousePool: " + e.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }

        /**
         * Moves objects from their current parent to a target object.
         * @param  targetClass New parent object id
         * @param targetOid The new parent's oid
         * @param objectClasses Class names of the objects to be moved
         * @param objectOids Oids of the objects to be moved
         * @param sessionId Session token
         * @throws ServerSideException If the object's or new parent's class can't be found
         *                             If the object or its new parent can't be found
         *                             If the update can't be performed due to a business rule
         */
        @WebMethod(operationName = "moveObjectsToWarehouse")
        public void moveObjectsToWarehouse(@WebParam(name = "targetClass")String targetClass,
                @WebParam(name = "targetOid")String targetOid,
                @WebParam(name = "objectsClasses")String[] objectClasses,
                @WebParam(name = "objectsOids")String[] objectOids,
                @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
            try{
                wsBean.moveObjectsToWarehouse(targetClass,targetOid, objectClasses, objectOids, getIPAddress(), sessionId);
            } catch(Exception e){
                if (e instanceof ServerSideException)
                    throw e;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in moveObjectsToWarehouse: " + e.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        // </editor-fold>
        
        //<editor-fold desc="Routing Explorer Module" defaultstate="collapsed">
        /**
         * Creates/updates a map of the BGPLinks
         * @param mappedBgpLinksIds BGPLinks that are already mapped, it can be 
         * an empty array if nothing is mapped of if the whole map should be generated again.
         * @param sessionId The session token
         * @return A list of BGPlinks with their endPoints(ports) and the CommunicationsElements parents of the ports
         * @throws ServerSideException If the structure is invalid, or the process definition could not be found or one of the properties is malformed or have an unexpected name
         */
        @WebMethod(operationName = "getBGPMap")
        public List<RemoteLogicalConnectionDetails> getBGPMap(@WebParam(name="mappedBgpLinksIds")List<String> mappedBgpLinksIds, 
                @WebParam(name="sessionId")String sessionId) throws ServerSideException {
            try {
                return wsBean.getBGPMap(mappedBgpLinksIds, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in updateProcessDefinition: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        // </editor-fold>
        
        //<editor-fold desc="Outside plant">
        /**
         * Creates an Outside Plant View
         * @param name The name of the new view
         * @param description The description of the new view
         * @param content The XML document with the contents of the view. The format of the XML document is consistent with the other views
         * @param sessionId Session token
         * @throws ServerSideException If the name is empty. 
         * @return The id of the newly created view
         */
        @WebMethod(operationName = "createOSPView")
        public long createOSPView(@WebParam(name = "name")String name, @WebParam(name = "description")String description, 
                @WebParam(name = "content")byte[] content, @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
            try {
                return wsBean.createOSPView(name, description, content, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in createOSPView: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        
        /**
         * Retrieves the specific information about an existing OSP view
         * @param viewId The id of the view
         * @param sessionId The session token
         * @return An object containing the view details and structure
         * @throws ServerSideException If the view could not be found
         */
        @WebMethod(operationName = "getOSPView")
        public RemoteViewObject getOSPView(@WebParam(name = "viewId")long viewId, @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
            try {
                return wsBean.getOSPView(viewId, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in getOSPView: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        
        /**
         * Retrieves the existing OSP views
         * @param sessionId The session token
         * @return The list of existing OSP views
         * @throws ServerSideException If an unexpected error appeared
         */
        @WebMethod(operationName = "getOSPViews")
        public List<RemoteViewObjectLight> getOSPViews(@WebParam(name = "sessionId")String sessionId) throws ServerSideException {
            try {
                return wsBean.getOSPViews(getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in getOSPViews: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        
        /**
         * Updates an existing OSP view
         * @param viewId The id of the view
         * @param name The new name of the view. Null if to remain unchanged
         * @param description The new description of the view. Null if to remain unchanged
         * @param content  The new content of the view. Null if to remain unchanged
         * @param sessionId The session token
         * @throws ServerSideException If the view could not be found or if the new name (if applicable) is empty
         */
        @WebMethod(operationName = "updateOSPView")
        public void updateOSPView(@WebParam(name = "viewId")long viewId, @WebParam(name = "name")String name, @WebParam(name = "description")String description, 
                @WebParam(name = "content")byte[] content, @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
            try {
                wsBean.updateOSPView(viewId, name, description, content, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in updateOSPView: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        
        /**
         * Deletes an existing OSP view
         * @param viewId The id of the view to be deleted
         * @param sessionId The session token
         * @throws ServerSideException If the view could not be found
         */
        @WebMethod(operationName = "deleteOSPView")
        public void deleteOSPView(@WebParam(name = "viewId")long viewId, @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
            try {
                wsBean.deleteOSPView(viewId, getIPAddress(), sessionId);
            } catch (Exception ex) {
                if (ex instanceof ServerSideException)
                    throw ex;
                else {
                    System.out.println("[KUWAIBA] An unexpected error occurred in deleteOSPView: " + ex.getMessage());
                    throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
                }
            }
        }
        
        //</editor-fold>
    // </editor-fold>
 
    // <editor-fold defaultstate="collapsed" desc="Helpers. Click on the + sign on the left to edit the code.">/**
    /**
     * Gets the IP address from the client issuing the request
     * @return the IP address as string
     */
    private String getIPAddress(){
        return ((HttpServletRequest)context.getMessageContext().
                    get("javax.xml.ws.servlet.request")).getRemoteAddr(); //NOI18N
    }// </editor-fold>
}
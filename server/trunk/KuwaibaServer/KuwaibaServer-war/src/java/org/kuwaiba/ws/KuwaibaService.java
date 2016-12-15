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

package org.kuwaiba.ws;

import com.neotropic.kuwaiba.modules.reporting.model.RemoteReport;
import com.neotropic.kuwaiba.modules.reporting.model.RemoteReportLight;
import com.neotropic.kuwaiba.modules.sdh.SDHContainerLinkDefinition;
import com.neotropic.kuwaiba.modules.sdh.SDHPosition;
import java.util.List;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLightList;
import org.kuwaiba.beans.WebserviceBeanRemote;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.util.Constants;
import org.kuwaiba.ws.todeserialize.StringPair;
import org.kuwaiba.ws.todeserialize.TransientQuery;
import org.kuwaiba.ws.toserialize.application.ApplicationLogEntry;
import org.kuwaiba.ws.toserialize.application.RemoteQuery;
import org.kuwaiba.ws.toserialize.application.RemoteQueryLight;
import org.kuwaiba.ws.toserialize.application.RemoteSession;
import org.kuwaiba.ws.toserialize.application.ResultRecord;
import org.kuwaiba.ws.toserialize.application.GroupInfo;
import org.kuwaiba.ws.toserialize.application.RemotePool;
import org.kuwaiba.ws.toserialize.application.RemoteTask;
import org.kuwaiba.ws.toserialize.application.RemoteTaskResult;
import org.kuwaiba.ws.toserialize.application.TaskNotificationDescriptor;
import org.kuwaiba.ws.toserialize.application.TaskScheduleDescriptor;
import org.kuwaiba.ws.toserialize.application.UserInfo;
import org.kuwaiba.ws.toserialize.application.UserInfoLight;
import org.kuwaiba.ws.toserialize.application.ViewInfo;
import org.kuwaiba.ws.toserialize.application.ViewInfoLight;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.business.RemoteObjectSpecialRelationships;
import org.kuwaiba.ws.toserialize.metadata.AttributeInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfoLight;

/**
 * Main web service
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
@WebService (serviceName = "KuwaibaService")
public class KuwaibaService {
    /**
     * The main session bean in charge of providing the business logic
     */
    @EJB
    private WebserviceBeanRemote wsBean;
   /**
     * The context to get information about each request
     */
    @Resource
    private WebServiceContext context;

    // <editor-fold defaultstate="collapsed" desc="Application methods. Click on the + sign on the left to edit the code.">
    /**
     * Creates a session
     * @param username user login name
     * @param password user password
     * @return A session object, including the session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createSession")
    public RemoteSession createSession(@WebParam(name = "username") String username,
            @WebParam(name = "password") String password) throws ServerSideException{
        try {
            String remoteAddress = getIPAddress();
            return wsBean.createSession(username, password, remoteAddress);
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "closeSession")
    public void closeSession(@WebParam(name = "sessionId")String sessionId) throws ServerSideException{
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */

    @WebMethod(operationName = "getUsers")
    public UserInfo[] getUsers(@WebParam(name = "sessionId")String sessionId) throws ServerSideException {
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
     * Retrieves the list of groups
     * @param sessionId Session token
     * @return A group object list
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getGroups")
    public GroupInfo[] getGroups(@WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try
        {
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
     * @param privileges A list of ints specifying the privileges for this user. Does nothing for now
     * @param groups List of the ids of the groups to relate to this user
     * @param sessionId Session token
     * @return The new user Id
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createUser")
    public long createUser(
            @WebParam(name = "username")String username,
            @WebParam(name = "password")String password,
            @WebParam(name = "firstName")String firstName,
            @WebParam(name = "LastName")String lastName,
            @WebParam(name = "enabled")boolean enabled,
            @WebParam(name = "privileges")long[] privileges,
            @WebParam(name = "groups")long[] groups,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try
        {
            return wsBean.createUser(username, password, firstName, lastName, enabled, privileges, groups, getIPAddress(), sessionId);
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
     * Set an existing user properties
     * @param oid User id
     * @param username New username (null if unchanged)
     * @param firstName New user's first name (null if unchanged)
     * @param lastName (null if unchanged)
     * @param password (null if unchanged)
     * @param enabled (null if unchanged)
     * @param privileges (null if unchanged). Does nothing for now
     * @param groups List of ids of the groups to be related to this user(null if unchanged)
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "setUserProperties")
    public void setUserProperties(
            @WebParam(name = "oid")long oid,
            @WebParam(name = "username")String username,
            @WebParam(name = "firstName")String firstName,
            @WebParam(name = "lastName")String lastName,
            @WebParam(name = "password")String password,
            @WebParam(name = "enabled")boolean enabled,
            @WebParam(name = "privileges")long[] privileges,
            @WebParam(name = "groups")long[] groups,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try
        {
            wsBean.setUserProperties(oid, username, password, firstName, lastName, enabled, privileges, groups, getIPAddress(), sessionId);
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
     * Creates a group
     * @param groupName Group name
     * @param description Group description
     * @param privileges Group privileges. Does nothing for now
     * @param users List of user ids to be related to this group
     * @param sessionId Session token
     * @return The group id
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createGroup")
    public long createGroup(
            @WebParam(name = "groupName")String groupName,
            @WebParam(name = "description")String description,
            @WebParam(name = "privileges")long[] privileges,
            @WebParam(name = "users")long[] users,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try
        {
            return wsBean.createGroup(groupName, description, privileges, users, getIPAddress(), sessionId);
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
     * @param privileges New group privileges (null if unchanged)
     * @param users New group users (null if unchanged)
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "setGroupProperties")
    public void setGroupProperties(@WebParam(name = "oid")long oid,
            @WebParam(name = "groupName")String groupName,
            @WebParam(name = "description")String description,
            @WebParam(name = "privileges")long[] privileges,
            @WebParam(name = "users")long[] users,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try
        {
            wsBean.setGroupProperties(oid, groupName, description, privileges, users, getIPAddress(), sessionId);
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectRelatedView")
    public ViewInfo getObjectRelatedView(@WebParam(name = "oid")long oid,
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
     * @throws ServerSideException Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectRelatedViews")
    public ViewInfoLight[] getObjectRelatedViews(@WebParam(name = "oid")long oid,
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
     * @throws ServerSideException Exception Generic exception encapsulating any possible error raised at runtime 
     */
    @WebMethod(operationName = "getGeneralViews")
    public ViewInfoLight[] getGeneralViews(@WebParam(name = "viewClass")String viewClass,
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
     * @throws ServerSideException Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getGeneralView")
    public ViewInfo getGeneralView(@WebParam(name = "viewId")long viewId,
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
     * @throws ServerSideException Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createObjectRelatedView")
    public long createObjectRelatedView(@WebParam(name = "objectId")long objectId,
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
     * @throws ServerSideException Exception Generic exception encapsulating any possible error raised at runtime
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "updateObjectRelatedView")
    public void updateObjectRelatedView(@WebParam(name = "objectOid")long objectOid,
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createListTypeItem")
    public long createListTypeItem(
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteListTypeItem")
    public void deleteListTypeItem(
            @WebParam(name = "className") String className,
            @WebParam(name = "oid") long oid,
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getListTypeItems")
    public RemoteObjectLight[] getListTypeItems(
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getInstanceableListTypes")
    public ClassInfoLight[] getInstanceableListTypes(
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * @return A byte array containing the class hierarchy as an XML document. See the <a href="http://neotropic.co/kuwaiba/wiki/index.php?title=XML_Documents#To_Save_Queries">wiki entry</a> for details on the document structure
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * @throws ServerSideException In case something goes wrong
     */
    @WebMethod(operationName = "createRootPool")
    public long createRootPool(@WebParam(name = "name")String name, 
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
     * @param description Pool description
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @param type Type of pool. For possible values see ApplicationManager.POOL_TYPE_XXX
     * @param sessionId The session token
     * @return The id of the new pool
     * @throws ServerSideException In case something goes wrong
     */
    @WebMethod(operationName = "createPoolInObject")
    public long createPoolInObject(@WebParam(name = "parentClassname")String parentClassname, 
                                   @WebParam(name = "parentId")long parentId, 
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
     * @throws ServerSideException In case something goes wrong
     */
    @WebMethod(operationName = "createPoolInPool")
    public long createPoolInPool(@WebParam(name = "parentId")long parentId, 
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
     * @param attributeValues Attributes to be set in the new object (values). Null or empty array for none. The size of this array must match attributeNames size
     * @param templateId Template to be used
     * @param sessionId Session identifier
     * @return The id of the newly created object
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createPoolItem")
    public long createPoolItem(@WebParam(name = "poolId")long poolId,
            @WebParam(name = "className")String className,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[][] attributeValues,
            @WebParam(name = "templateId")long templateId,
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
     * Deletes a pool
     * @param id Pool to be deleted
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deletePool")
    public void deletePool(@WebParam(name = "id")long id,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.deletePool(id, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in deletePool: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Deletes a set of pools
     * @param ids Pools to be deleted
     * @param sessionId Session identifier
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deletePools")
    public void deletePools(@WebParam(name = "ids")long[] ids,
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
     * @throws ServerSideException In case something goes wrong
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
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "getPoolsInObject")
    public List<RemotePool> getPoolsInObject(@WebParam(name = "objectClassName")String objectClassName, 
                                             @WebParam(name = "objectId")long objectId,
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
     * @throws ServerSideException In case something goes wrong.
     */
    @WebMethod(operationName = "getPoolsInPool")
    public List<RemotePool> getPoolsInPool(@WebParam(name = "parentPoolId")long parentPoolId,
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
     * @throws ServerSideException In case something goes wrong
     */
    @WebMethod(operationName = "getPool")
    public RemotePool getPool(@WebParam(name = "poolId") long poolId,
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
     * Update pool name and description
     * @param poolId Pool Id
     * @param name Pool name
     * @param description Pool description
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "setPoolProperties")
    public void setPoolProperties(@WebParam(name = "poolId") long poolId, 
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
     * Get the objects contained into a pool
     * @param poolId Parent pool id
     * @param limit limit of results. -1 to return all
     * @param sessionId Session identifier
     * @return The list of items
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getPoolItems")
    public RemoteObjectLight[] getPoolItems(@WebParam(name = "poolId")long poolId,
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
     * @param script The script to be executed
     * @param parameters The parameters for the script
     * @param schedule When the task should be executed
     * @param notificationType How the result of the task should be notified to the associated users 
     * @param sessionId The session token
     * @return The id of the newly created task
     * @throws ServerSideException If something goes wrong
     */
    @WebMethod(operationName = "createTask")
    public long createTask(@WebParam(name = "name")String name,
            @WebParam(name = "description")String description,
            @WebParam(name = "enabled")boolean enabled,
            @WebParam(name = "script")String script,
            @WebParam(name = "parameters")List<StringPair> parameters,
            @WebParam(name = "schedule")TaskScheduleDescriptor schedule,
            @WebParam(name = "notificationType")TaskNotificationDescriptor notificationType,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try {
            return wsBean.createTask(name, description, enabled, script, parameters, 
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
     * @throws ServerSideException In case something goes wrong
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
     * @throws ServerSideException In case something goes wrong
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
     * @throws ServerSideException In case something goes wrong
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
     * @throws ServerSideException In case something goes wrong
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
     * @throws ServerSideException In case something goes wrong
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
     * Gets all the regiistered tasks
     * @param sessionId Session token
     * @return A list of task objects
     * @throws ServerSideException In case something goes wrong
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
     * @throws ServerSideException In case something goes wrong
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
     * @throws ServerSideException In case something goes wrong.
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
     * @throws ServerSideException In case something goes wrong
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
     * @throws ServerSideException In case something goes wrong
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
     * @throws ServerSideException In case something goes wrong
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
     * @throws ServerSideException In case something goes wrong
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
    
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Business Methods. Click on the + sign on the left to edit the code.">
    /**
     * Gets the children of a given object given his class id and object id
     * @param objectClassId object's class id
     * @param oid object's id
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array of all the direct children of the provided object according with the current container hierarchy
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectChildrenForClassWithId")
    public RemoteObjectLight[] getObjectChildrenForClassWithId(@WebParam(name = "oid") long oid,
            @WebParam(name = "objectClassId") long objectClassId,
            @WebParam(name = "maxResults") int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            RemoteObjectLight[] res = wsBean.getObjectChildren(oid,objectClassId, maxResults, getIPAddress(), sessionId);
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectChildren")
    public RemoteObjectLight[] getObjectChildren(@WebParam(name = "objectClassName") String objectClassName,
            @WebParam(name = "oid") long oid,
            @WebParam(name = "maxResults") int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            RemoteObjectLight[] res = wsBean.getObjectChildren(objectClassName, oid, maxResults, getIPAddress(), sessionId);
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getSiblings")
    public RemoteObjectLight[] getSiblings(@WebParam(name = "objectClassName") String objectClassName,
            @WebParam(name = "oid") long oid,
            @WebParam(name = "maxResults") int  maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            RemoteObjectLight[] res = wsBean.getSiblings(objectClassName, oid, maxResults, getIPAddress(), sessionId);
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName="getChildrenOfClass")
    public RemoteObject[] getChildrenOfClass(@WebParam(name="parentOid")long parentOid,
            @WebParam(name="parentClass")String parentClass,
            @WebParam(name="childrenClass")String childrenClass,
            @WebParam(name="maxResults")int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            RemoteObject[] res = wsBean.getChildrenOfClass(parentOid,parentClass,childrenClass, maxResults, getIPAddress(), sessionId);
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
     * Gets all children of an object of a given class
     * @param parentOid Object oid whose children will be returned
     * @param parentClass
     * @param childrenClass
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array with children
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName="getChildrenOfClassLight")
    public RemoteObjectLight[] getChildrenOfClassLight(@WebParam(name="parentOid")long parentOid,
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
      * Gets the complete information about a given object (all its attributes)
      * @param objectClass Object class
      * @param oid Object id
      * @param sessionId Session token
      * @return a representation of the entity as a RemoteObject
      * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
      */
    @WebMethod(operationName = "getObject")
    public RemoteObject getObject(@WebParam(name = "objectClass") String objectClass,
            @WebParam(name = "oid") long oid,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{

        try{
            return wsBean.getObject(objectClass, oid, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getobject: " + e.getMessage());
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
            @WebParam(name = "oid") long oid,
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
     * Gets all objects of a given class
     * @param className Class name
     * @param maxResults Max number of results. 0 to retriever all
     * @param sessionId Session token
     * @return A list of instances of @className
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectsOfClassLight")
    public RemoteObjectLight[] getObjectsOfClassLight(@WebParam(name = "className") String className,
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
     * Gets the parent of a given object in the containment hierarchy
     * @param objectClass Object class
     * @param oid Object id
     * @param sessionId Session id
     * @return The parent object
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getParent")
    public RemoteObject getParent(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") long oid,
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
    public RemoteObjectLight[] getParents(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") long oid,
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
     * @return
     * @throws ServerSideException If case something goes wrong
     */
    @WebMethod(operationName = "getSpecialAttributes")
    public RemoteObjectSpecialRelationships getSpecialAttributes(@WebParam(name = "objectClass") String objectClass,
            @WebParam(name = "oid") long oid,
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
     * Gets the first parent of an object which matches the given class in the containment hierarchy
     * @param objectClass Object class
     * @param oid Object oid
     * @param parentClass Class to be matched
     * @param sessionId sssion Id
     * @return The direct parent of the provided object.
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getParentOfClass")
    public RemoteObject getParentOfClass(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") long oid,
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
    public RemoteObjectLight[] getSpecialAttribute(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") long oid,
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
     * @throws ServerSideException If something goes wrong.
     */
    @WebMethod(operationName = "getObjectSpecialChildren")
    public RemoteObjectLight[] getObjectSpecialChildren (@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "objectId") long objectId,
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
     * @param oid Object's oid
     * @param  attributeNames attribute names to be changed
     * @param  attributeValues attribute values for the attributes above
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "updateObject")
    public void updateObject(@WebParam(name = "className")String className,
            @WebParam(name = "oid")long oid,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[][] attributeValues,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.updateObject(className,oid,attributeNames, attributeValues, getIPAddress(), sessionId);
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
     * @param templateId Template id. Does nothing for now
     * @param sessionId Session token
     * @return the id of the new object
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createObject")
    public long createObject(@WebParam(name = "className")String className,
            @WebParam(name = "parentObjectClassName")String parentObjectClassName,
            @WebParam(name = "parentOid")long parentOid,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[][] attributeValues,
            @WebParam(name = "templateId")long templateId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.createObject(className,parentObjectClassName, parentOid,attributeNames,attributeValues, templateId, getIPAddress(), sessionId);
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
     * @param templateId Template id. Does nothing for now
     * @param sessionId Session token
     * @return the id of the new object
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createSpecialObject")
    public long createSpecialObject(@WebParam(name = "className")String className,
            @WebParam(name = "parentObjectClassName")String parentObjectClassName,
            @WebParam(name = "parentOid")long parentOid,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[][] attributeValues,
            @WebParam(name = "templateId")long templateId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.createSpecialObject(className,parentObjectClassName, parentOid,attributeNames,attributeValues, templateId, getIPAddress(), sessionId);
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
     * Delete a set of objects. Note that this method must be used only for business objects (not metadata or application ones)
     * @param className Objects class names
     * @param oid object id from the objects to be deleted
     * @param releaseRelationships Should the deletion be forced, deleting all the relationships?
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteObject")
    public void deleteObject(@WebParam(name = "className")String className,
            @WebParam(name = "oid")long oid,
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
     * Delete a set of objects. Note that this method must be used only for business objects (not metadata or application ones)
     * @param classNames Objects class names
     * @param oids object id from the objects to be deleted
     * @param releaseRelationships Should the deletion be forced, deleting all the relationships?
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteObjects")
    public void deleteObjects(@WebParam(name = "classNames")String[] classNames,
            @WebParam(name = "oid")long[] oids,
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
     * Moves objects from their current parent to a target object.
     * @param  targetClass New parent object id
     * @param targetOid The new parent's oid
     * @param objectClasses Class names of the objects to be moved
     * @param objectOids Oids of the objects to be moved
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "moveObjects")
    public void moveObjects(@WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")long targetOid,
            @WebParam(name = "objectsClasses")String[] objectClasses,
            @WebParam(name = "objectsOids")long[] objectOids,
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
     * Copy objects from its current parent to a target. This is <b>not</b> a deep copy. Only the selected object will be copied, not the children
     * @param targetClass  The new parent class name
     * @param targetOid The new parent oid
     * @param objectClasses Class names of the objects to be copied
     * @param objectOids Oids of the objects to be copied
     * @param recursive should the objects be copied recursively? (themselves plus their children)
     * @param sessionId Session token
     * @return An array with the ids of the new objects
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "copyObjects")
    public long[] copyObjects(
            @WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")long targetOid,
            @WebParam(name = "templateClases")String[] objectClasses,
            @WebParam(name = "templateOids")long[] objectOids,
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
     * Models
     */

    //Physical connections
    /**
     * Connect two ports using a mirror relationship
     * @param aObjectClass Port A class
     * @param aObjectId Port A id
     * @param bObjectClass Port B class
     * @param bObjectId Port B id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime 
     */
    @WebMethod(operationName = "connectMirrorPort")
    public void connectMirrorPort(
            @WebParam(name = "aObjectClass")String aObjectClass,
            @WebParam(name = "aObjectId")long aObjectId,
            @WebParam(name = "bObjectClass")String bObjectClass,
            @WebParam(name = "bObjectId")long bObjectId,
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "releaseMirrorPort")
    public void releaseMirrorPort(
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")long objectId,
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
     * @param attributeNames Default attributes to be set
     * @param attributeValues Default attributes to be set
     * @param connectionClass Class used to create the connection. See Constants class for supported values
     * @param sessionId Session token
     * @return The new connection id
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "createPhysicalConnection")
    public long createPhysicalConnection(
            @WebParam(name = "aObjectClass")String aObjectClass,
            @WebParam(name = "aObjectId")long aObjectId,
            @WebParam(name = "bObjectClass")String bObjectClass,
            @WebParam(name = "bObjectId")long bObjectId,
            @WebParam(name = "parentClass")String parentClass,
            @WebParam(name = "parentId")long parentId,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[][] attributeValues,
            @WebParam(name = "connectionClass") String connectionClass,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.createPhysicalConnection(aObjectClass, aObjectId,bObjectClass, bObjectId,
                   parentClass, parentId, attributeNames, attributeValues, connectionClass, getIPAddress(), sessionId);
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
     * Allows to create multiple connections at once
     * @param connectionClass Class all the connections are going to be instance of
     * @param numberOfChildren Number of connections to be created
     * @param parentClass Class of the parent object to the connections. Null for none, anything for DummyRoot
     * @param parentId Id of the parent object to the connections. Anything none, -1 for DummyRoot
     * @param sessionId Session token
     * @return The ids of the new objects
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    public long[] createBulkPhysicalConnections(@WebParam(name = "connectionClass")String connectionClass, 
            @WebParam(name = "numberOfChildren")int numberOfChildren, 
            @WebParam(name = "parentClass")String parentClass, 
            @WebParam(name = "parentId")long parentId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.createBulkPhysicalConnections(connectionClass, numberOfChildren,
                   parentClass, parentId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createBulkPhysicalConnections: " + e.getMessage());
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    public RemoteObjectLight[] getConnectionEndpoints(@WebParam(name = "connectionClass")String connectionClass, 
            @WebParam(name = "connectionId")long connectionId, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getConnectionEndpoints(connectionClass, connectionId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getConnectionEndpoints: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Gets the physical trace of connections and ports from a port
     * @param objectClass Object class
     * @param objectId Object id
     * @param sessionId Session token
     * @return An array containing the sorted elements in the physical path of the given port
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "getPhysicalPath")
    public RemoteObjectLight[] getPhysicalPath (@WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")long objectId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getPhysicalPath(objectClass, objectId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getPhysicalPath: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    } 
    
    /**
     * Connect pairs of ports (if they are not connected already) using physical link (cable, fibers, all subclasses of GenericPhysicalConnection)
     * @param sideAClassNames The list of classes of one of the sides of the connection
     * @param sideAIds The list of ids the objects on one side of the connection
     * @param linksClassNames the classes of the links that will connect the two sides
     * @param linksIds The ids of these links
     * @param sideBClassNames The list of classes of the other side of the connection
     * @param sideBIds The list of ids the objects on the other side of the connection
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    public void connectPhysicalLinks (@WebParam(name = "sideAClassNames")String[] sideAClassNames, @WebParam(name = "sideAIds")Long[] sideAIds,
                                      @WebParam(name = "linksClassNames")String[] linksClassNames, @WebParam(name = "linksIds")Long[] linksIds,
                                      @WebParam(name = "sideBClassNames")String[] sideBClassNames, @WebParam(name = "sideBIds")Long[] sideBIds,
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
     * Deletes a physical connection
     * @param objectClass Object class
     * @param objectId Object id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deletePhysicalConnection")
    public void deletePhysicalConnection(
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")long objectId,
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "associateObjectToService")
    public void associateObjectToService (
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")long objectId,
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "serviceId")long serviceId,
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "associateObjectsToService")
    public void associateObjectsToService (
            @WebParam(name = "objectClass")String[] objectClass,
            @WebParam(name = "objectId")long[] objectId,
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "serviceId")long serviceId,
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime    
     */
    @WebMethod(operationName = "releaseObjectFromService")
    public void releaseObjectFromService (
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "serviceId")long serviceId,
            @WebParam(name = "targetId")long targetId,           
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "getServiceResources")
    public RemoteObjectLight[] getServiceResources (
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "serviceId")long serviceId,
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
    
    /**
     * Creates a customer (with no parent in the containment hierarchy)
     * @param customerClass Customer class
     * @param attributes Default attributes to be set
     * @param attributeValues Default values to #attributes
     * @param sessionId Session token
     * @return The id of the newly created customer
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "createCustomer")
    public long createCustomer (
            @WebParam(name = "customerClass")String customerClass,
            @WebParam(name = "attributes")String[] attributes,
            @WebParam(name = "attributeValues")String[] attributeValues,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            return wsBean.createCustomer(customerClass, attributes, attributeValues, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createCustomer: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Creates a service and relates it to a customer
     * @param serviceClass Service class
     * @param customerClass Customer class
     * @param customerId Customer id
     * @param attributes Service attributes
     * @param attributeValues Default values to #attributes
     * @param sessionId Session token
     * @return The id of the newly created service
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime  
     */
    @WebMethod(operationName = "createService")
    public long createService (
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "customerClass")String customerClass,
            @WebParam(name = "customerId")long customerId,
            @WebParam(name = "attributes")String[] attributes,
            @WebParam(name = "attributeValues")String[] attributeValues,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            return wsBean.createService(serviceClass, customerClass, customerId, attributes, attributeValues, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in createService: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Returns the services associated to a customer
     * @param customerClass Customer class
     * @param customerId Customer Id
     * @param sessionId Session token
     * @return The list of services related to the give customer
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getServices")
    public RemoteObjectLight[] getServices(@WebParam(name = "customerClass")String customerClass, 
            @WebParam(name = "customerId")long customerId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.getServices(customerClass, customerId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getServices: " + e.getMessage());
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getBusinessObjectAuditTrail")
    public ApplicationLogEntry[] getBusinessObjectAuditTrail (
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")long objectId,
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
     * @throws ServerSideException If anything goes wrong
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
     * Retrieves the log entries for a given [application] object (Users, Pools, etc)
     * @param objectClass Object class
     * @param objectId Object id
     * @param limit Max number of results (0 for all)
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getApplicationObjectAuditTrail")
    public void getApplicationObjectAuditTrail (
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")long objectId,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.getApplicationObjectAuditTrail (objectClass, objectId, limit, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getApplicationObjectAuditTrail: " + e.getMessage());
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
            ClassInfo ci = new ClassInfo();
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
            ClassInfo ci = new ClassInfo();
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
     * Gets a class attribute, using the class name as key to find it
     * @param className the class name
     * @param attributeName
     * @param sessionId Session token
     * @return the class attribute
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime  
     */
    @WebMethod(operationName = "getAttribute")
    public AttributeInfo getAttribute(@WebParam(name = "className")
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime 
     */
    @WebMethod(operationName = "getAttributeForClassWithId")
    public AttributeInfo getAttributeForClassWithId(@WebParam(name = "classId")
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
     * Adds an attribute to a class using its name as key to find it
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
     * @param sessionId session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
        boolean unique, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException {

        try {
            AttributeInfo ai = new AttributeInfo(name, displayName, type, administrative, 
                    visible, isReadOnly, unique, description, noCopy);

            wsBean.createAttribute(className, ai, getIPAddress(), sessionId);

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
     * Adds an attribute to a class using its id as key to find it
     * @param ClassId Class id where the attribute will be attached
     * @param name attribute name
     * @param displayName attribute display name
     * @param type attribute type
     * @param description attribute description
     * @param administrative is the attribute administrative?
     * @param visible is the attribute visible?
     * @param isReadOnly is the attribute read only?
     * @param noCopy Marks an attribute as not to be copied during a copy operation.
     * @param unique should this attribute be unique?
     * @param sessionId session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createAttributeForClassWithId")
    public void createAttributeForClassWithId(@WebParam(name = "classId")
        long ClassId, @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "type")
        String type, @WebParam(name = "description")
        String description, @WebParam(name = "administrative")
        boolean administrative, @WebParam(name = "visible")
        boolean visible, @WebParam(name = "isReadOnly")
        boolean isReadOnly, @WebParam(name = "noCopy")
        boolean noCopy, @WebParam(name = "unique")
        boolean unique, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException {

        try {
            AttributeInfo ai = new AttributeInfo(name, displayName, type, administrative, 
                                   visible, isReadOnly, unique, description, noCopy);

            wsBean.createAttribute(ClassId, ai, getIPAddress(), sessionId);

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
     * Updates a class attribute taking its name as key to find it
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
     * @param noCopy can this attribute be copy in copy/paste operation?
     * @param sessionId session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "setAttributeProperties")
    public void setAttributeProperties(@WebParam(name = "className")
        String className, @WebParam(name = "attributeId")
        long attributeId, @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "type")
        String type, @WebParam(name = "description")
        String description, @WebParam(name = "administrative")
        Boolean administrative, @WebParam(name = "visible")
        Boolean visible, @WebParam(name = "readOnly")
        Boolean readOnly, @WebParam(name = "unique")
        Boolean unique, @WebParam(name = "noCopy")
        Boolean noCopy, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException {

        try {
            AttributeInfo ai = new AttributeInfo(attributeId, name, displayName, 
                    type, administrative, visible, readOnly, unique, description, noCopy);
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
     * Updates a class attribute taking its id as key to find it
     * @param classId Class the attribute belongs to
     * @param attributeId attribute id
     * @param name attribute name
     * @param displayName attribute display name
     * @param type attribute type
     * @param description attribute description
     * @param isAdministrative is the attribute administrative?
     * @param isVisible is the attribute visible?
     * @param isReadOnly is the attribute read only?
     * @param isUnique should this attribute be unique?
     * @param noCopy can this attribute be copy in copy/paste operation?
     * @param sessionId session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "setAttributePropertiesForClassWithId")
    public void setAttributePropertiesForClassWithId(@WebParam(name = "classId")
        long classId, @WebParam(name = "attributeId")
        long attributeId, @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "type")
        String type, @WebParam(name = "description")
        String description, @WebParam(name = "isAdministrative")
        Boolean isAdministrative, @WebParam(name = "isVisible")
        Boolean isVisible, @WebParam(name = "isReadOnly")
        Boolean isReadOnly, @WebParam(name = "noCopy")
        Boolean noCopy, @WebParam(name = "isUnique")
        Boolean isUnique, @WebParam(name = "sessionId")
        String sessionId) throws ServerSideException {

        try {
            AttributeInfo ai = new AttributeInfo(attributeId, name, displayName, 
                    type, isAdministrative, isVisible, isReadOnly, isUnique, description, noCopy);
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getClass")
    public ClassInfo getClass(@WebParam(name = "className")
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getClassWithId")
    public ClassInfo getClassWithId(@WebParam(name = "classId")
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
     * @throws ServerSideException Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getSubClassesLight")
    public List<ClassInfoLight> getSubClassesLight(
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
     * @throws ServerSideException Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getSubClassesLightNoRecursive")
    public List<ClassInfoLight> getSubClassesLightNoRecursive(
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
    public List<ClassInfo> getAllClasses(
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getAllClassesLight")
    public List<ClassInfoLight> getAllClassesLight(
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     *
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
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * Get the possible children of a class according to the containment hierarchy. This method is recursive, and if a possible child is an abstract class, it gets its non-abstract subclasses
     * @param parentClassName Class to retrieve its possible children
     * @param sessionId Session token
     * @return A list of possible children as ClassInfoLight instances
     * An array with the metadata for the entire class hierarchy as ClassInfoLight instances
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getPossibleChildren")
    public List<ClassInfoLight> getPossibleChildren(@WebParam(name = "parentClassName")
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
     * Get the possible children of a class according to the containment hierarchy.
     * This method is not recursive, and only returns the direct possible children,
     * even if they're abstract
     * @param parentClassName Class to retrieve its possible children
     * @param sessionId Session token
     * @return An array with the metadata for the entire class hierarchy as ClassInfoLight instances
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getPossibleChildrenNoRecursive")
    public List<ClassInfoLight> getPossibleChildrenNoRecursive(@WebParam(name = "parentClassName")
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
     * Gets the possible children of a given non-abstract class according to the business rules set for a 
     * particular model
     * @param parentClassName Class to retrieve its possible children
     * @param sessionId Session token
     * @return The list of possible special children
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getSpecialPossibleChildren")
    public List<ClassInfoLight> getSpecialPossibleChildren(@WebParam(name = "parentClassName")
    String parentClassName, @WebParam(name = "sessionId")
    String sessionId) throws ServerSideException {

        try {
            return wsBean.getSpecialPossibleChildren(parentClassName, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in getSpecialPossibleChildren: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Adds possible children to a given class using its id as argument. If any of the arguments provided are already added,
     * it will abort the operation and rise an exception
     * @param parentClassId Class to attach the new possible children
     * @param newPossibleChildren List of nre possible children. Abstract classes are de-aggregated
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * Adds possible children to a given class using its name as argument.
     * If any of the arguments provided are already added,
     * it will abort the operation and rise an exception
     * @param parentClassName Class to attach the new possible children
     * @param childrenToBeAdded List of nre possible children. Abstract classes are de-aggregated
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * Removes a set of possible children for a given class
     * @param parentClassId Class the possible children are going to be removed from
     * @param childrenToBeRemoved List of ids of classes to be removed as possible children
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
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
     * Get the containment hierarchy of a given class, but upwards (i.e. for Building, it could return 
     * City, Country, Continent)
     * @param className Class to be evaluated
     * @param recursive do it recursively or not
     * @param sessionId
     * @return List of classes in the upstream containment hierarchy
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getUpstreamContainmentHierarchy")
    public List<ClassInfoLight> getUpstreamContainmentHierarchy(@WebParam(name = "className")
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
    
    //<editor-fold desc="Templates" defaultstate="collapsed">
    /**
     * Creates a template.
     * @param templateClass The class you want to create a template for.
     * @param templateName The name of the template.
     * @param sessionId Session token.
     * @return The id of the newly created template.
     * @throws ServerSideException If something goes wrong
     */
    @WebMethod(operationName = "createTemplate")
    public long createTemplate(@WebParam(name = "templateClass")String templateClass, 
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
     * @param templateElementParentClassName Class of the parent to the obejct you want to create.
     * @param templateElementParentId Id of the parent to the obejct you want to create.
     * @param templateElementName Name of the element.
     * @param sessionId Session token.
     * @return The id of the new object.
     * @throws ServerSideException If something goes wrong.
     */
    @WebMethod(operationName = "createTemplateElement")
    public long createTemplateElement(@WebParam(name = "templateElementClass")String templateElementClass, 
            @WebParam(name = "templateElementParentClassName")String templateElementParentClassName,
            @WebParam(name = "templateElementParentId")long templateElementParentId,
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
     * Updates the value of an attribute of a template element.
     * @param templateElementClass Class of the element you want to update.
     * @param templateElementId Id of the element you want to update.
     * @param attributeNames Names of the attributes that you want to be updated as an array of strings.
     * @param attributeValues The values of the attributes you want to upfate. For list types, it's the id of the related type
     * @param sessionId Session token.
     * @throws ServerSideException If something goes wrong.
     */
    @WebMethod(operationName = "updateTemplateElement")
    public void updateTemplateElement(@WebParam(name = "templateElementClass")String templateElementClass, 
            @WebParam(name = "templateElementId")long templateElementId,
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
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "deleteTemplateElement")
    public void deleteTemplateElement(@WebParam(name = "templateElementClass")String templateElementClass, 
            @WebParam(name = "templateElementId")long templateElementId,
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
     * @throws ServerSideException If somethings goes wrong
     */
    @WebMethod(operationName = "getTemplatesForClass")
    public List<RemoteObjectLight> getTemplatesForClass(@WebParam(name = "className")String className, 
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
     * @throws org.kuwaiba.exceptions.ServerSideException In case something goes wrong.
     */
    public long[] copyTemplateElements(@WebParam(name = "sourceObjectsClassNames")String[] sourceObjectsClassNames, 
                                       @WebParam(name = "sourceObjectsIds")long[] sourceObjectsIds, 
                                       @WebParam(name = "newParentClassName")String newParentClassName,
                                       @WebParam(name = "newParentId")long newParentId, 
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
     * Retrieves the children of a given template element.
     * @param templateElementClass Template element class.
     * @param templateElementId Template element id.
     * @param sessionId 
     * @return The template element's children as a list of RemoteBusinessObjectLight instances.
     * @throws org.kuwaiba.exceptions.ServerSideException
     */
    @WebMethod(operationName = "getTemplateElementChildren")
    public List<RemoteObjectLight> getTemplateElementChildren(@WebParam(name = "templateElementClass")String templateElementClass, 
            @WebParam(name = "templateElementId")long templateElementId, 
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
     * Retrives all the information of a given template element.
     * @param templateElementClass Template element class.
     * @param templateElementId Template element id.
     * @param sessionId session token
     * @return The template element information
     * @throws org.kuwaiba.exceptions.ServerSideException In case someting goes wrong.
     */
    @WebMethod(operationName = "getTemplateElement")
    public RemoteObject getTemplateElement(@WebParam(name = "templateElementClass")String templateElementClass, 
            @WebParam(name = "templateElementId")long templateElementId, 
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
     * @throws ServerSideException If the class provided could not be found.
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
     * @throws ServerSideException If the dummy root could not be found, which is actually a severe problem.
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
     * @throws ServerSideException If the report could not be found.
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
     * @throws ServerSideException If any of the report properties has a wrong or unexpected format or if the report could not be found.
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
     * @throws ServerSideException If something goes wrong.
     */
    @WebMethod(operationName = "updateReportParameters")
    public void updateReportParameters(@WebParam(name = "reportId")long reportId, @WebParam(name = "parameters")List<StringPair> parameters, 
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
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
     */
    @WebMethod(operationName = "getClassLevelReports")
    public List<RemoteReportLight> getClassLevelReports(@WebParam(name = "className")String className, 
            @WebParam(name = "recursive")boolean recursive, @WebParam(name = "includeDisabled")boolean includeDisabled, @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
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
     * @throws ServerSideException If the dummy root could not be found, which is actually a severe problem.
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
     * @throws ServerSideException If the report could not be found.
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
     * @throws ServerSideException If the class could not be found or if the report could not be found or if the inventory object could not be found or if there's an error during the execution of the report. 
     */
    @WebMethod(operationName = "executeClassLevelReport")
    public byte[] executeClassLevelReport(@WebParam(name = "objectClassName")String objectClassName, 
            @WebParam(name = "objectId")long objectId, @WebParam(name = "reportId")long reportId, 
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
     * @throws ServerSideException If the report could not be found or if the associated script exits with error.
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
    
    // <editor-fold defaultstate="collapsed" desc="Commercial modules data methods">
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
     * @throws ServerSideException In case something goes wrong with the creation process
     */
    @WebMethod(operationName = "createSDHTransportLink")
    public long createSDHTransportLink(@WebParam(name = "classNameEndpointA") String classNameEndpointA, 
            @WebParam(name = "idEndpointA") long idEndpointA, 
            @WebParam(name = "classNameEndpointB") String classNameEndpointB, 
            @WebParam(name = "idEndpointB") long idEndpointB, 
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
     * @throws ServerSideException In case something goes wrong with the creation process
     */
    @WebMethod(operationName = "createSDHContainerLink")
    public long createSDHContainerLink(@WebParam(name = "classNameEndpointA") String classNameEndpointA, 
            @WebParam(name = "idEndpointA") long idEndpointA, 
            @WebParam(name = "classNameEndpointB") String classNameEndpointB,
            @WebParam(name = "idEndpointB") long idEndpointB,
            @WebParam(name = "linkType") String linkType, 
            @WebParam(name = "positions") List<SDHPosition> positions, 
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
     * @throws ServerSideException In case something goes wrong with the creation process
     */
    @WebMethod(operationName = "createSDHTributaryLink")
    public long createSDHTributaryLink(@WebParam(name = "classNameEndpointA") String classNameEndpointA, 
            @WebParam(name = "idEndpointA") long idEndpointA, 
            @WebParam(name = "classNameEndpointB") String classNameEndpointB, 
            @WebParam(name = "idEndpointB") long idEndpointB, 
            @WebParam(name = "linkType") String linkType, 
            @WebParam(name = "positions") List<SDHPosition> positions, 
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
     * @throws org.kuwaiba.exceptions.ServerSideException If something goes wrong
     */
    @WebMethod(operationName = "deleteSDHTransportLink")
    public void deleteSDHTransportLink(@WebParam(name = "transportLinkClass") String transportLinkClass, 
            @WebParam(name = "transportLinkId") long transportLinkId, 
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
     * @throws ServerSideException If some high level thing goes wrong
     */
    @WebMethod(operationName = "deleteSDHContainerLink")
    public void deleteSDHContainerLink(@WebParam(name = "containerLinkClass") String containerLinkClass, 
            @WebParam(name = "containerLinkId") long containerLinkId, 
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
     * Deletes a tributary link and its corresponding container link
     * @param tributaryLinkClass The class of the tributary link
     * @param tributaryLinkId the id of the tributary link
     * @param forceDelete Ignore the existing relationships
     * @param sessionId Session token
     * @throws ServerSideException If some high level thing goes wrong
     */
    @WebMethod(operationName = "deleteSDHTributaryLink")
    public void deleteSDHTributaryLink(@WebParam(name = "tributaryLinkClass") String tributaryLinkClass, 
            @WebParam(name = "tributaryLinkId") long tributaryLinkId, 
            @WebParam(name = "forceDelete") boolean forceDelete, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.deleteSDHTributaryLink(tributaryLinkClass, tributaryLinkId, forceDelete, getIPAddress(), sessionId);
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
     * @throws ServerSideException If something goes wrong
     * 
     */
    @WebMethod(operationName = "findSDHRoutesUsingTransportLinks")
    public List<RemoteBusinessObjectLightList> findSDHRoutesUsingTransportLinks(@WebParam(name = "communicationsEquipmentClassA") String communicationsEquipmentClassA, 
                                            @WebParam(name = "communicationsEquipmentIdA") long  communicationsEquipmentIdA, 
                                            @WebParam(name = "communicationsEquipmentClassB") String communicationsEquipmentClassB, 
                                            @WebParam(name = "communicationsEquipmentIB") long  communicationsEquipmentIB, 
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
     * @throws ServerSideException If something goes wrong
     * 
     */
    @WebMethod(operationName = "findSDHRoutesUsingContainerLinks")
    public List<RemoteBusinessObjectLightList> findSDHRoutesUsingContainerLinks(@WebParam(name = "communicationsEquipmentClassA") String communicationsEquipmentClassA, 
                                            @WebParam(name = "communicationsEquipmentIdA") long  communicationsEquipmentIdA, 
                                            @WebParam(name = "communicationsEquipmentClassB") String communicationsEquipmentClassB, 
                                            @WebParam(name = "communicationsEquipmentIB") long  communicationsEquipmentIB, 
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
     * @throws ServerSideException If something gos wrong 
     */
    @WebMethod(operationName = "getSDHTransportLinkStructure")
    public List<SDHContainerLinkDefinition> getSDHTransportLinkStructure(@WebParam(name = "transportLinkClass")String transportLinkClass, 
            @WebParam(name = "transportLinkId")long transportLinkId, 
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
     * @throws ServerSideException If the user is not authorized to know the structure of a container link, if the container supplied is not subclass of GenericSDHHighOrderContainerLink, if the container could not be found or if the class could not be found
     */
    @WebMethod(operationName = "getSDHContainerLinkStructure")
    public List<SDHContainerLinkDefinition> getSDHContainerLinkStructure(@WebParam(name = "containerLinkClass")String containerLinkClass, 
            @WebParam(name = "containerLinkId")long containerLinkId, 
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
     * Retrieves all the subnet pools
     * @param limit limit
     * @param parentId parent id
     * @param className if is an IPv4 or an IPv6 subnet
     * @param sessionId the session id
     * @return a set of subnet pools
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "getSubnetPools")
    public RemotePool[] getSubnetPools(@WebParam(name = "limit")
            int limit, @WebParam(name = "parentId") long parentId,
            @WebParam(name = "className") String className,
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException{
        try{
            return wsBean.getSubnetPools(limit, parentId, className, getIPAddress(), sessionId);
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
     * Retrieves the subnets of a same pool of subnets
     * @param poolId subnet pool id
     * @param limit limit of returned subnets
     * @param sessionId
     * @return a set of subnets
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "getSubnets")
    public RemoteObjectLight[] getSubnets(@WebParam(name = "poolId")long poolId,
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
     * Create a pool of subnets
     * @param parentId subnet parent Id
     * @param subnetPoolName subnet pool name
     * @param subnetPoolDescription
     * @param className if is a IPv4 or an IPv6 subnet
     * @param sessionId
     * @return id of the created subnet pool 
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "createSubnetPool")
    public long createSubnetPool(
            @WebParam(name = "parentId")long parentId, 
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
     * @param attributeNames Names of the attributes that will be set on the newly created element.
     * @param attributeValues The values to be set in the aforementioned attributes.
     * @param sessionId Session token.
     * @return The id of the new subnet.
     * @throws ServerSideException If something goes wrong.
     */
    @WebMethod(operationName = "createSubnet")
    public long createSubnet(@WebParam(name = "poolId")long poolId,
            @WebParam(name = "className")String className,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[][] attributeValues,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.createSubnet(poolId, className, attributeNames, attributeValues, getIPAddress(), sessionId);
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
     * @param ids Pools to be deleted
     * @param sessionId Session identifier
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteSubnetPools")
    public void deleteSubnetPools(@WebParam(name = "ids")long[] ids,
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
     * Delete a set of subnets. Note that this method must be used only for Subnet objects
     * @param oids object id from the objects to be deleted
     * @param className The class of the subnet
     * @param releaseRelationships Should the deletion be forced, deleting all the relationships?
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteSubnets")
    public void deleteSubnets(
            @WebParam(name = "oid")long[] oids,
            @WebParam(name = "className") String className,
            @WebParam(name = "releaseRelationships") boolean releaseRelationships,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            wsBean.deleteSubnets(oids, className, releaseRelationships, getIPAddress(), sessionId);
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
      * @param className VPN classs
      * @param sessionId Session token
      * @return a representation of the entity as a RemoteObject
      * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
      */
    @WebMethod(operationName = "getSubnet")
    public RemoteObject getSubnet(
            @WebParam(name = "id") long id,
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
            @WebParam(name = "subnetPoolId") long subnetPoolId,
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
     * Adds an IP to a Subnet
     * @param id ipAddres id
     * @param parentClassName the parent class name
     * @param attributeNames IP Address Attributes
     * @param attributeValues IP Addres values
     * @param sessionId
     * @return the id of the new IP Address
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "addIP")
    public long addIP(@WebParam(name = "id")long id,
            @WebParam(name = "parentClassName")String parentClassName,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[][] attributeValues,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException{
        try{
            return wsBean.addIP(id, parentClassName, attributeNames, attributeValues, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in addIP: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Removes an IP Address from a subnets. Note that this method must be used only for Subnet objects
     * @param oids object id from the objects to be deleted
     * @param releaseRelationships Should the deletion be forced, deleting all the relationships?
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "removeIP")
    public void removeIP(
            @WebParam(name = "oid")long[] oids,
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
     * @return a set of subnets
     * @throws ServerSideException 
     */
    @WebMethod(operationName = "getSubnetUsedIps")
    public RemoteObjectLight[] getSubnetUsedIps(@WebParam(name = "id")long id,
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
     * release an element From a VLAN
     * Releases an subnet from a VLAN that is using it
     * @param id Subnet id
     * @param vlanId the VLAN id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime    
     */
    @WebMethod(operationName = "releaseFromVlan")
    public void releaseFromVlan (
            @WebParam(name = "id")long id,
            @WebParam(name = "vlanId")long vlanId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.releaseFromVlan(vlanId, id, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in releaseFromVlan: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * releaseSubnetFromVRF
     * Releases an subnet from a VLAN that is using it
     * @param id Subnet id
     * @param vrfId the VRF id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime    
     */
    @WebMethod(operationName = "releaseSubnetFromVrf")
    public void releaseSubnetFromVrf (
            @WebParam(name = "id")long id,
            @WebParam(name = "vrfId")long vrfId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.releaseSubnetFromVrf(vrfId, id, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in releaseSubnetFromVrf: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Associates a element to existing VLAN
     * @param id Subnet id
     * @param className if the subnet has IPv4 or IPv6 IP addresses
     * @param vlanId VLAN id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "relateToVlan")
    public void relateToVlan (
            @WebParam(name = "id")long id,
            @WebParam(name = "className")String className,
            @WebParam(name = "vlanId")long vlanId,
            @WebParam(name = "sessionId")String sessionId) throws ServerSideException {
        try{
            wsBean.relateToVlan(id, className, vlanId, getIPAddress(), sessionId);
        } catch(Exception e){
            if (e instanceof ServerSideException)
                throw e;
            else {
                System.out.println("[KUWAIBA] An unexpected error occurred in relateToVlan: " + e.getMessage());
                throw new RuntimeException("An unexpected error occurred. Contact your administrator.");
            }
        }
    }
    
    /**
     * Associates a subnet to existing VRF
     * @param id Subnet id
     * @param className if the subnet has IPv4 or IPv6 IP addresses
     * @param vrfId VRF id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "relateSubnetToVrf")
    public void relateSubnetToVrf (
            @WebParam(name = "id")long id,
            @WebParam(name = "className")String className,
            @WebParam(name = "vrfId")long vrfId,
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
     * Associates a port to an IP address
     * @param id IP address id
     * @param portClassName port class
     * @param portId port id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "relateIPtoPort")
    public void relateIPtoPort (
            @WebParam(name = "id")long id,
            @WebParam(name = "portClassName")String portClassName,
            @WebParam(name = "portId")long portId,
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
     * Releases a port from an IP address that is using it
     * @param deviceClassName device class name
     * @param deviceId device id
     * @param id Subnet id
     * @param sessionId Session token
     * @throws ServerSideException Generic exception encapsulating any possible error raised at runtime    
     */
    @WebMethod(operationName = "releasePortFromIP")
    public void releasePortFromIP (
            @WebParam(name = "deviceClassName")String deviceClassName,
            @WebParam(name = "deviceId")long deviceId,
            @WebParam(name = "id")long id,
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
     * @throws ServerSideException In case something goes wrong. A server side exception is a managed error, while a RunTimeException is something unexpected
     */
    @WebMethod(operationName = "associateObjectsToContract")
    public void associateObjectsToContract (
            @WebParam(name = "objectClass")String[] objectClass,
            @WebParam(name = "objectId")long[] objectId,
            @WebParam(name = "contractClass")String contractClass,
            @WebParam(name = "contractId")long contractId,
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
     * @throws ServerSideException In case something goes wrong
     */
    @WebMethod(operationName = "releaseObjectFromContract")
    public void releaseObjectFromContract (
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")long objectId,
            @WebParam(name = "contractId")long contractId,
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
     * @param linkType Type of link (MPLSLink)
     * @param defaultName The default name of th
     * @param sessionId Session token
     * @return The id of the newly created transport link
     * @throws ServerSideException In case something goes wrong with the creation process
     */
    @WebMethod(operationName = "createMPLSLink")
    public long createMPLSLink(@WebParam(name = "classNameEndpointA") String classNameEndpointA, 
            @WebParam(name = "idEndpointA") long idEndpointA, 
            @WebParam(name = "classNameEndpointB") String classNameEndpointB, 
            @WebParam(name = "idEndpointB") long idEndpointB, 
            @WebParam(name = "linkType") String linkType, 
            @WebParam(name = "defaultName") String defaultName, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            return wsBean.createMPLSLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, linkType, defaultName, getIPAddress(), sessionId);
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
     * Deletes a MPLS link and its corresponding container link
     * @param linkClass The class of the link
     * @param linkId the id of the link
     * @param forceDelete Ignore the existing relationships
     * @param sessionId Session token
     * @throws ServerSideException If some high level thing goes wrong
     */
    @WebMethod(operationName = "deleteMPLSLink")
    public void deleteMPLSLink(@WebParam(name = "linkClass") String linkClass, 
            @WebParam(name = "linkId") long linkId, 
            @WebParam(name = "forceDelete") boolean forceDelete, 
            @WebParam(name = "sessionId") String sessionId) throws ServerSideException {
        try {
            wsBean.deleteMPLSLink(linkClass, linkId, forceDelete, getIPAddress(), sessionId);
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
            @WebParam(name = "portId")long portId,
            @WebParam(name = "portClassName")String portClassName,
            @WebParam(name = "interfaceClassName")String interfaceClassName,
            @WebParam(name = "interfaceId")long interfaceId,
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
            @WebParam(name = "interfaceId")long interfaceId,
            @WebParam(name = "portId")long portId,
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

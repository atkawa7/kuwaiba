/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>.
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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import org.kuwaiba.beans.WebserviceBeanRemote;
import org.kuwaiba.beans.sessions.Session;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.util.Constants;
import org.kuwaiba.util.Util;
import org.kuwaiba.ws.todeserialize.TransientQuery;
import org.kuwaiba.ws.toserialize.application.RemoteQuery;
import org.kuwaiba.ws.toserialize.application.RemoteQueryLight;
import org.kuwaiba.ws.toserialize.application.RemoteSession;
import org.kuwaiba.ws.toserialize.application.ResultRecord;
import org.kuwaiba.ws.toserialize.application.UserGroupInfo;
import org.kuwaiba.ws.toserialize.application.UserInfo;
import org.kuwaiba.ws.toserialize.application.ViewInfo;
import org.kuwaiba.ws.toserialize.application.ViewInfoLight;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.metadata.AttributeInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfoLight;

/**
 * Main webservice
 * @author Adrian Martinez Molina <Adrian.Martinez@kuwaiba.org>
 */
@WebService()
public class Kuwaiba {
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
     * Authenticates the user
     * @param username user login name
     * @param password user password
     * @return A session object
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createSession")
    public RemoteSession createSession(@WebParam(name = "username") String username,
            @WebParam(name = "password") String password) throws Exception{
        try{
            String remoteAddress = getIPAddress();
            return wsBean.createSession(username,password, remoteAddress);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    /**
     * Closes a session
     * @param sessionId The session token to be closed
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "closeSession")
    public void closeSession(@WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            String remoteAddress = getIPAddress();
            wsBean.closeSession(sessionId, remoteAddress);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

   /**
     * Retrieves the user list
     * @param sessionId session token
     * @return The list of users
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */

    @WebMethod(operationName = "getUsers")
    public UserInfo[] getUsers(@WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            wsBean.validateCall("getUsers", getIPAddress(), sessionId);
            return wsBean.getUsers();
        } catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Retrieves the group list
     * @param sessionId
     * @return A group object list
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getGroups")
    public UserGroupInfo[] getGroups(@WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            wsBean.validateCall("getGroups", getIPAddress(), sessionId);
            return wsBean.getGroups();
        } catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Creates a user
     * @param username User name. Can't be null
     * @param password A password (in plain text, it'll be encrypted later). Can't be null nor an empty string
     * @param firstName User's first name
     * @param lastName User's last name
     * @param enabled Is this user enable by default?
     * @param privileges A list of ints specifying the privileges for this user. Does nothing for now
     * @param groups List of the ids of the groups to relate to this user
     * @param sessionId Session token
     * @return The new user Id
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createUser")
    public long createUser(
            @WebParam(name = "username")String username,
            @WebParam(name = "password")String password,
            @WebParam(name = "firstName")String firstName,
            @WebParam(name = "LastName")String lastName,
            @WebParam(name = "enabled")boolean enabled,
            @WebParam(name = "privileges")int[] privileges,
            @WebParam(name = "groups")long[] groups,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            wsBean.validateCall("createUser", getIPAddress(), sessionId);
            return wsBean.createUser(username, password, firstName, lastName, enabled, privileges, groups);

        } catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "setUserProperties")
    public void setUserProperties(
            @WebParam(name = "oid")long oid,
            @WebParam(name = "username")String username,
            @WebParam(name = "firstName")String firstName,
            @WebParam(name = "lastName")String lastName,
            @WebParam(name = "password")String password,
            @WebParam(name = "enabled")boolean enabled,
            @WebParam(name = "privileges")int[] privileges,
            @WebParam(name = "groups")long[] groups,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            wsBean.validateCall("setUserProperties", getIPAddress(), sessionId);
            wsBean.setUserProperties(oid, username, password, firstName, lastName, enabled, privileges, groups);

        } catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createGroup")
    public long createGroup(
            @WebParam(name = "groupName")String groupName,
            @WebParam(name = "description")String description,
            @WebParam(name = "privileges")int[] privileges,
            @WebParam(name = "users")long[] users,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            wsBean.validateCall("createGroup", getIPAddress(), sessionId);
            return wsBean.createGroup(groupName, description, privileges, users);

        } catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "setGroupProperties")
    public void setGroupProperties(@WebParam(name = "oid")long oid,
            @WebParam(name = "groupName")String groupName,
            @WebParam(name = "description")String description,
            @WebParam(name = "privileges")int[] privileges,
            @WebParam(name = "users")long[] users,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            wsBean.validateCall("setGroupProperties", getIPAddress(), sessionId);
            wsBean.setGroupProperties(oid, groupName, description, privileges, users);
        } catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Deletes a list of users
     * @param oids List of user ids to be deleted
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteUsers")
    public void deleteUsers(@WebParam(name = "oids")long[] oids,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            wsBean.validateCall("deleteUsers", getIPAddress(), sessionId);
            wsBean.deleteUsers(oids);
        } catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Deletes a list of groups
     * @param oids list of group ids to be deleted
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteGroups")
    public void deleteGroups(@WebParam(name = "oids")long[] oids,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            wsBean.validateCall("deleteGroups", getIPAddress(), sessionId);
            wsBean.deleteGroups(oids);
        } catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    @WebMethod(operationName = "getObjectRelatedView")
    public ViewInfo getObjectRelatedView(@WebParam(name = "oid")long oid,
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "viewId")long viewId,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getObjectRelatedView", getIPAddress(), sessionId);
            return wsBean.getObjectRelatedView(oid, objectClass, viewId);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     *
     * @param oid
     * @param objectClass
     * @param viewType
     * @param limit
     * @param sessionId
     * @return
     * @throws Exception
     */
    @WebMethod(operationName = "getObjectRelatedViews")
    public ViewInfoLight[] getObjectRelatedViews(@WebParam(name = "oid")long oid,
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "viewType")int viewType,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getObjectRelatedViews", getIPAddress(), sessionId);
            return wsBean.getObjectRelatedViews(oid, objectClass, viewType, limit);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    @WebMethod(operationName = "getGeneralViews")
    public ViewInfoLight[] getGeneralViews(@WebParam(name = "viewType")int viewType,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getGeneralViews", getIPAddress(), sessionId);
            return wsBean.getGeneralViews(viewType, limit);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    @WebMethod(operationName = "getGeneralView")
    public ViewInfo getGeneralView(@WebParam(name = "viewId")long viewId,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getGeneralView", getIPAddress(), sessionId);
            return wsBean.getGeneralView(viewId);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    @WebMethod(operationName = "createObjectRelatedView")
    public long createObjectRelatedView(@WebParam(name = "objectId")long objectId,
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "name")String name,
            @WebParam(name = "description")String description,
            @WebParam(name = "viewType")int viewType,
            @WebParam(name = "structure")byte[] structure,
            @WebParam(name = "background")byte[] background,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("createObjectRelatedView", getIPAddress(), sessionId);
            return wsBean.createObjectRelatedView(objectId, objectClass, name, description, viewType, structure, background);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    @WebMethod(operationName = "createGeneralView")
    public long createGeneralView(@WebParam(name = "viewType")int viewType,
            @WebParam(name = "name")String name,
            @WebParam(name = "description")String description,
            @WebParam(name = "structure")byte[] structure,
            @WebParam(name = "background")byte[] background,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("createGeneralView", getIPAddress(), sessionId);
            return wsBean.createGeneralView(viewType, name, description, structure, background);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    @WebMethod(operationName = "updateObjectRelatedView")
    public void updateObjectRelatedView(@WebParam(name = "objectOid")long objectOid,
            @WebParam(name = "objectClass")String objectClass, @WebParam(name = "viewId")long viewId,
            @WebParam(name = "viewName")String viewName, @WebParam(name = "viewDescription")String viewDescription,
            @WebParam(name = "structure")byte[] structure,
            @WebParam(name = "background")byte[] background, @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("updateObjectRelatedView", getIPAddress(), sessionId);
            wsBean.updateObjectRelatedView(objectOid, objectClass, viewId, viewName, viewDescription, structure, background);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    @WebMethod(operationName = "updateGeneralView")
    public void updateGeneralView(@WebParam(name = "viewId")long viewId,
            @WebParam(name = "viewName")String viewName, @WebParam(name = "viewDescription")String viewDescription,
            @WebParam(name = "structure")byte[] structure, @WebParam(name = "background")byte[] background, @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("updateGeneralView", getIPAddress(), sessionId);
            wsBean.updateGeneralView(viewId, viewName, viewDescription, structure, background);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    /**
     * Deletes views
     * @param oids
     * @param sessionId
     * @throws Exception
     */
    @WebMethod(operationName = "deleteGeneralView")
    public void deleteGeneralView(@WebParam(name = "oids")long [] oids,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("updateGeneralView", getIPAddress(), sessionId);
            wsBean.deleteGeneralView(oids);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Creates a list type item
     * @param className List type item class name
     * @param name List type item name
     * @param displayName List type item display name
     * @param sessionId Session token
     * @return the id of the new object
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createListTypeItem")
    public long createListTypeItem(
            @WebParam(name = "className") String className,
            @WebParam(name = "name") String name,
            @WebParam(name = "displayName") String displayName,
            @WebParam(name = "sessionId") String sessionId) throws Exception{

        try
        {
            wsBean.validateCall("createListTypeItem", getIPAddress(), sessionId);
            return wsBean.createListTypeItem(className, name, displayName);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }

    }

    /**
     * Deletes a list type item
     * @param className list type item class name
     * @param oid list type item id
     * @param releaseRelationships should the deletion process release the relationships attached to this object
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteListTypeItem")
    public void deleteListTypeItem(
            @WebParam(name = "className") String className,
            @WebParam(name = "oid") long oid,
            @WebParam(name = "releaseRelationships") boolean releaseRelationships,
            @WebParam(name = "sessionId") String sessionId) throws Exception{

        try{
            wsBean.validateCall("deleteListTypeItem", getIPAddress(), sessionId);
            wsBean.deleteListTypeItem(className, oid, releaseRelationships);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }

    }

    /**
     * Retrieves all items for a single list type
     * @param className The list type class
     * @param sessionId Session token
     * @return a list of list type items
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getListTypeItems")
    public RemoteObjectLight[] getListTypeItems(
            @WebParam(name = "className") String className,
            @WebParam(name = "sessionId") String sessionId) throws Exception{
        try{
            wsBean.validateCall("getListTypeItems", getIPAddress(), sessionId);
            return wsBean.getListTypeItems(className);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }

    }

    /**
     * Retrieves all possible list types
     * @param sessionId Session token
     * @return A list of list types as ClassInfoLight instances
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getInstanceableListTypes")
    public ClassInfoLight[] getInstanceableListTypes(
            @WebParam(name = "sessionId") String sessionId) throws Exception{
        try
        {
            wsBean.validateCall("getInstanceableListTypes", getIPAddress(), sessionId);
            return wsBean.getInstanceableListTypes();
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Executes a complex query generated using the Graphical Query Builder.  Please note
     * that the first record is reserved for the column headers, so and empty result set
     * will have at least one record.
     * @param query The TransientQuery object (a code friendly version of the graphical query designed at client side).
     * @param sessionId session id to check permissions
     * @return An array of records (the first raw is used to put the headers)
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "executeQuery")
    public ResultRecord[] executeQuery(@WebParam(name="query")TransientQuery query,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("executeQuery", getIPAddress(), sessionId);
            return wsBean.executeQuery(query);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createQuery")
    public long createQuery(@WebParam(name="queryName")String queryName,
            @WebParam(name="ownerOid")long ownerOid,
            @WebParam(name="queryStructure")byte[] queryStructure,
            @WebParam(name="description")String description,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("createQuery", getIPAddress(), sessionId);
            return wsBean.createQuery(queryName, ownerOid, queryStructure, description);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "saveQuery")
    public void saveQuery(@WebParam(name="queryOid")long queryOid,
            @WebParam(name = "queryName")String queryName,
            @WebParam(name = "ownerOid")long ownerOid,
            @WebParam(name = "queryStructure")byte[] queryStructure,
            @WebParam(name = "description")String description,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("saveQuery", getIPAddress(), sessionId);
            wsBean.saveQuery(queryOid, queryName, ownerOid, queryStructure, description);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Deletes a query
     * @param queryOid Query id
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteQuery")
    public void deleteQuery(@WebParam(name="queryOid")long queryOid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("deleteQuery", getIPAddress(), sessionId);
            wsBean.deleteQuery(queryOid);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Retrieves all saved queries
     * @param showPublic should this method return the public queries along with the private to this user?
     * @param sessionId Session token
     * @return A list with the available queries
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getQueries")
    public RemoteQueryLight[] getQueries(@WebParam(name="showPublic")boolean showPublic,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getQueries", getIPAddress(), sessionId);
            return wsBean.getQueries(showPublic);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Retrieves a saved query
     * @param queryOid Query id
     * @param sessionId Session token
     * @return The query
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getQuery")
    public RemoteQuery getQuery(@WebParam(name="queryOid")long queryOid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getQuery", getIPAddress(), sessionId);
            return wsBean.getQuery(queryOid);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Creates an XML document describing the class hierarchy
     * @param showAll should this method return all entity classes or only InventoryObject subclasses
     * @param sessionId session identifier
     * @return A byte array containing the class hierarchy as an XML document. See the <a href="http://neotropic.co/kuwaiba/wiki/index.php?title=XML_Documents#To_Save_Queries">wiki entry</a> for details on the document structure
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getClassHierarchy")
    public byte[] getClassHierarchy(@WebParam(name = "showAll")boolean showAll,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getClassHierarchy", getIPAddress(), sessionId); //NOI18N
            return wsBean.getClassHierarchy(showAll);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Methods related to manage pools
     */
    /**
     * Creates a pool
     * @param name Pool name
     * @param description Pool description
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @param sessionId Session identifier
     * @return id of the new pool
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createPool")
    public long createPool(@WebParam(name = "name")String name,
            @WebParam(name = "description")String description,
            @WebParam(name = "instancesOfClass")String instancesOfClass,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            Session session = wsBean.validateCall("createPool", getIPAddress(), sessionId); //NOI18N
            UserInfo user = wsBean.getUserInSession(sessionId);
            return wsBean.createPool(name, description, instancesOfClass, user.getOid());
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createPoolItem")
    public long createPoolItem(@WebParam(name = "poolId")long poolId,
            @WebParam(name = "className")String className,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[][] attributeValues,
            @WebParam(name = "templateId")long templateId,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("createPoolItem", getIPAddress(), sessionId); //NOI18N
            return wsBean.createPoolItem(poolId, className, attributeNames, attributeValues, templateId);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    /**
     * Deletes a set of pools
     * @param ids Pools to be deleted
     * @param sessionId Session identifier
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deletePools")
    public void deletePools(@WebParam(name = "ids")long[] ids,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("deletePools", getIPAddress(), sessionId); //NOI18N
            wsBean.deletePools(ids);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    /**
     * Get a set of pools
     * @param limit Maximum number of pool records to be returned
     * @param sessionId Session identifier
     * @return The list of pools as RemoteObjectLight instances
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getPools")
    public RemoteObjectLight[] getPools(@WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getPools", getIPAddress(), sessionId); //NOI18N
            return wsBean.getPools(limit);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    /**
     * Get the objects contained into a pool
     * @param poolId Parent pool id
     * @param limit limit of results. -1 to return all
     * @param sessionId Session identifier
     * @return The list of items
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getPoolItems")
    public RemoteObjectLight[] getPoolItems(@WebParam(name = "poolId")long poolId,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getPoolItems", getIPAddress(), sessionId); //NOI18N
            return wsBean.getPoolItems(poolId, limit);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectChildrenForClassWithId")
    public RemoteObjectLight[] getObjectChildrenForClassWithId(@WebParam(name = "oid") long oid,
            @WebParam(name = "objectClassId") long objectClassId,
            @WebParam(name = "maxResults") int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getObjectChildrenForClassWithId", getIPAddress(), sessionId);
            RemoteObjectLight[] res = wsBean.getObjectChildren(oid,objectClassId, maxResults);
            return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

     /**
     * Gets the children of a given object given his class name and object id
     * @param oid Object's oid
     * @param objectClassName object's class name
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array of all the direct children of the provided object according with the current container hierarchy
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectChildren")
    public RemoteObjectLight[] getObjectChildren(@WebParam(name = "objectClassName") String objectClassName,
            @WebParam(name = "oid") long oid,
            @WebParam(name = "maxResults") int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getObjectChildren", getIPAddress(), sessionId);
            RemoteObjectLight[] res = wsBean.getObjectChildren(objectClassName, oid, maxResults);
            return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Gets all children of an object of a given class
     * @param parentOid Parent whose children are requested
     * @param parentClass
     * @param childrenClass
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array with children
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName="getChildrenOfClass")
    public RemoteObject[] getChildrenOfClass(@WebParam(name="parentOid")long parentOid,
            @WebParam(name="parentClass")String parentClass,
            @WebParam(name="childrenClass")String childrenClass,
            @WebParam(name="maxResults")int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getChildrenOfClass", getIPAddress(), sessionId);
            RemoteObject[] res = wsBean.getChildrenOfClass(parentOid,parentClass,childrenClass, maxResults);
            return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName="getChildrenOfClassLight")
    public RemoteObjectLight[] getChildrenOfClassLight(@WebParam(name="parentOid")long parentOid,
            @WebParam(name="parentClass")String parentClass,
            @WebParam(name="childrenClass")String childrenClass,
            @WebParam(name="maxResults")int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getChildrenOfClassLight", getIPAddress(), sessionId);
            return wsBean.getChildrenOfClassLight(parentOid,parentClass,childrenClass,maxResults);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

     /**
      * Gets the complete information about a given object (all its attributes)
      * @param objectClass Object class
      * @param oid Object id
      * @param sessionId Session token
      * @return a representation of the entity as a RemoteObject
      * @throws Exception Generic exception encapsulating any possible error raised at runtime
      */
    @WebMethod(operationName = "getObject")
    public RemoteObject getObject(@WebParam(name = "objectClass") String objectClass,
            @WebParam(name = "oid") long oid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{

        try{
            wsBean.validateCall("getObject", getIPAddress(), sessionId);
            return wsBean.getObject(objectClass, oid);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Gets the basic information about a given object (oid, classname, name)
     * @param objectClass Object class name
     * @param oid Object oid
     * @param sessionId Session token
     * @return a representation of the entity as a RemoteObjectLight
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectLight")
    public RemoteObjectLight getObjectLight(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") long oid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getObjectLight", getIPAddress(), sessionId);
            return wsBean.getObjectLight(objectClass, oid);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    /**
     * Gets the parent of a given object in the containment hierarchy
     * @param objectClass Object class
     * @param oid Object id
     * @param sessionId Session id
     * @return The parent object
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getParent")
    public RemoteObject getParent(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") long oid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getParent", getIPAddress(), sessionId);
            return wsBean.getParent(objectClass, oid);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    /**
     * Gets the first parent of an object which matches the given class in the containment hierarchy
     * @param objectClass Object class
     * @param oid Object oid
     * @param parentClass Class to be matched
     * @param sessionId sssion Id
     * @return
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getParentOfClass")
    public RemoteObject getParentOfClass(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") long oid,
            @WebParam(name = "parentClass") String parentClass,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getParentOfClass", getIPAddress(), sessionId);
            return wsBean.getParentOfClass(objectClass, oid, parentClass);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getSpecialAttribute")
    public String[] getSpecialAttribute(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") long oid,
            @WebParam(name = "attributename") String attributeName,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getSpecialAttribute", getIPAddress(), sessionId);
            return wsBean.getSpecialAttribute(objectClass, oid, attributeName);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Updates attributes of a given object
     * @param className object's class name
     * @param oid Object's oid
     * @param  attributeNames attribute names to be changed
     * @param  attributeValues attribute values for the attributes above
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "updateObject")
    public void updateObject(@WebParam(name = "className")String className,
            @WebParam(name = "oid")long oid,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[][] attributeValues,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("updateObject", getIPAddress(), sessionId);
            wsBean.updateObject(className,oid,attributeNames, attributeValues);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createObject")
    public long createObject(@WebParam(name = "className")String className,
            @WebParam(name = "parentObjectClassName")String parentObjectClassName,
            @WebParam(name = "parentOid")long parentOid,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[][] attributeValues,
            @WebParam(name = "templateId")long templateId,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("createObject", getIPAddress(), sessionId);
            return wsBean.createObject(className,parentObjectClassName, parentOid,attributeNames,attributeValues, templateId);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Delete a set of objects. Note that this method must be used only for business objects (not metadata or application ones)
     * @param classNames Objects class names
     * @param oids object id from the objects to be deleted
     * @param releaseRelationships Should the deletion be forced, deleting all the relationships?
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteObjects")
    public void deleteObjects(@WebParam(name = "classNames")String[] classNames,
            @WebParam(name = "oid")long[] oids,
            @WebParam(name = "releaseRelationships") boolean releaseRelationships,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("deleteObjects", getIPAddress(), sessionId);
            wsBean.deleteObjects(classNames,oids, releaseRelationships);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Moves objects from their current parent to a target object.
     * @param  targetClass New parent object id
     * @param targetOid The new parent's oid
     * @param objectClasses Class names of the objects to be moved
     * @param objectOids Oids of the objects to be moved
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "moveObjects")
    public void moveObjects(@WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")long targetOid,
            @WebParam(name = "objectsClasses")String[] objectClasses,
            @WebParam(name = "objectsOids")long[] objectOids,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("moveObjects", getIPAddress(), sessionId);
            wsBean.moveObjects(targetClass,targetOid, objectClasses, objectOids);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "copyObjects")
    public long[] copyObjects(
            @WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")long targetOid,
            @WebParam(name = "templateClases")String[] objectClasses,
            @WebParam(name = "templateOids")long[] objectOids,
            @WebParam(name = "recursive")boolean recursive,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("copyObjects", getIPAddress(), sessionId);
           return wsBean.copyObjects(targetClass,targetOid, objectClasses, objectOids, recursive);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    /**
     * Models
     */

    //Physical connections
    /**
     * Creates a physical connection (a container or a link). The validations are made at server side (this is,
     * if the connection can be established between the two endpoints, if they're not already connected, etc)
     * @param aObjectClass "a" endpoint object class
     * @param aObjectId "a" endpoint object id
     * @param bObjectClass "b" endpoint object class
     * @param bObjectId "b" endpoint object id
     * @param parentClass
     * @param parentId
     * @param attributeNames
     * @param attributeValues
     * @param connectionClass Class used to create the connection. See Constants class for supported values
     * @param sessionId Session token
     * @return The new connection id
     * @throws Exception In case something goes wrong
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
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("createPhysicalConnection", getIPAddress(), sessionId);
           return wsBean.createPhysicalConnection(aObjectClass, aObjectId,bObjectClass, bObjectId,
                   parentClass, parentId, attributeNames, attributeValues, connectionClass);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Deletes a physical connection
     * @param objectClass Object class
     * @param objectId Object id
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deletePhysicalConnection")
    public void deletePhysicalConnection(
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")long objectId,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("deletePhysicalConnection", getIPAddress(), sessionId);
            wsBean.deletePhysicalConnection(objectClass, objectId);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Metadata Methods. Click on the + sign on the left to edit the code.">

    /**
     * Creates a class metadata object. This method is still under testing and might be buggy
     * @param name Class name
     * @param displayName Class display name
     * @param description Class description
     * @param abstractClass is this class abstract?
     * @param parentClassName Parent class name
     * @param icon Icon fro view. The size is limited by the value in Constants.MAX_ICON_SIZE
     * @param smallIcon Icon for trees. The size is limited by the value in Constants.MAX_ICON_SIZE
     * @param sessionId Session token
     * @return the id of the new class metadata object
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createClass")
    public long createClass(@WebParam(name = "className")
        String className, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "description")
        String description, @WebParam(name = "_abstract")
        boolean _abstract, @WebParam(name = "custom")
        boolean custom, @WebParam(name = "countable")
        boolean countable, @WebParam(name = "inDesign")
        boolean inDesign, @WebParam(name = "parentClassName")
        String parentClassName, @WebParam(name = "icon")
        byte[] icon, @WebParam(name = "smallIcon")
        byte[] smallIcon, @WebParam(name = "sessionId")
        String sessionId) throws Exception {
        
        try{
            wsBean.validateCall("createClass", getIPAddress(), sessionId);
            if (icon != null){
                if (icon.length > Constants.MAX_ICON_SIZE){
                    throw new ServerSideException(Level.WARNING, Util.formatString("The uploaded file exceeds the max file size (%1s)", Constants.MAX_BACKGROUND_SIZE));
                }
            }
            if (smallIcon != null){
                if (smallIcon.length > Constants.MAX_ICON_SIZE){
                    throw new ServerSideException(Level.WARNING, Util.formatString("The uploaded file exceeds the max file size (%1s)", Constants.MAX_BACKGROUND_SIZE));
                }
            }
            ClassInfo ci = new ClassInfo();
            ci.setClassName(className);
            ci.setDisplayName(displayName);
            ci.setDescription(description);
            ci.setIcon(icon);
            ci.setSmallIcon(smallIcon);
            ci.setParentClassName(parentClassName);
            ci.setAbstract(_abstract);
            ci.setCountable(countable);
            ci.setCustom(custom);
            ci.setInDesign(inDesign);

            return wsBean.createClass(ci);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

     /**
     * Updates a class metadata properties. Use null values for those properties that shouldn't be touched
     * @param class metadata id.
     * @param class metadata name. Null if unchanged
     * @param displayName New class metadata display name. Null if unchanged
     * @param description New class metadata description. Null if unchanged
     * @param abstractClass is this class abstract?
     * @param icon New icon for views. Null if unchanged. The size is limited by the value in Constants.MAX_ICON_SIZE
     * @param smallIcon New icon for trees. Null if unchanged. The size is limited by the value in Constants.MAX_ICON_SIZE
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "setClassProperties")
    public void setClassProperties(@WebParam(name = "classId")
        long classId, @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "description")
        String description, @WebParam(name = "smallIcon")
        byte[] smallIcon,  @WebParam(name = "icon")
        byte[] icon, @WebParam(name = "_abstract")
        Boolean _abstract, @WebParam(name = "inDesign")
        Boolean inDesign, @WebParam(name = "custom")
        Boolean custom, @WebParam(name = "countable")
        Boolean countable, @WebParam(name = "sessionId")
        String sessionId) throws Exception {
        try
        {
            wsBean.validateCall("setClassProperties", getIPAddress(), sessionId);
            if (icon != null){
                if (icon.length > Constants.MAX_ICON_SIZE)
                    throw new ServerSideException(Level.WARNING, Util.formatString("The uploaded file exceeds the max file size (%1s)", Constants.MAX_BACKGROUND_SIZE));
            }
            if (smallIcon != null){
                if (smallIcon.length > Constants.MAX_ICON_SIZE)
                    throw new ServerSideException(Level.WARNING, Util.formatString("The uploaded file exceeds the max file size (%1s)", Constants.MAX_BACKGROUND_SIZE));
            }
            ClassInfo ci = new ClassInfo();
            ci.setId(classId);
            ci.setClassName(name);
            ci.setDisplayName(displayName);
            ci.setDescription(description);
            ci.setIcon(icon);
            ci.setSmallIcon(smallIcon);
            ci.setAbstract(_abstract);
            ci.setInDesign(inDesign);
            ci.setCountable(countable);
            ci.setCustom(custom);

            wsBean.setClassProperties(ci);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    /**
     * Get an attribute from a class by the class name
     * @param className the class name
     * @param sessionId
     * @return the class attribute
     * @throws Exception 
     */
    @WebMethod(operationName = "getAttribute")
    public AttributeInfo getAttribute(@WebParam(name = "className")
    String className, @WebParam(name = "attributeName")
    String attributeName, @WebParam(name = "sesionId")
    String sessionId) throws Exception{
            try {
            wsBean.validateCall("getAttribute", getIPAddress(), sessionId);
            return wsBean.getAttribute(className, attributeName);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    /**
     * Returns a class attribute, providing the class id
     * @param classId Class id
     * @param attributeName Attribute name
     * @param sessionId 
     * @return The attribute definition
     * @throws Exception 
     */
    @WebMethod(operationName = "getAttributeForClassWithId")
    public AttributeInfo getAttributeForClassWithId(@WebParam(name = "classId")
    String classId, @WebParam(name = "attributeName")
    String attributeName, @WebParam(name = "sesionId")
    String sessionId) throws Exception{
            try {
            wsBean.validateCall("getAttributeForClassWithId", getIPAddress(), sessionId);
            
            wsBean.getAttribute(classId, attributeName);
            return null;

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
   
    /**
     * Adds an attribute to a classMetadata
     * @param ClassName Class name where the attribute will be attached
     * @param name attribute name
     * @param displayName attribute display name
     * @param type attribute type
     * @param description attribute description
     * @param administrative is the attribute administrative?
     * @param visible is the attribute visible?
     * @param readOnly is the attribute read only?
     * @param unique should this attribute be unique?
     * @param sessionId session token
     * @throws Exception IN case something goes wrong
     */
    @WebMethod(operationName = "createAttribute")
    public void createAttribute(@WebParam(name = "className")
        String className,  @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "type")
        String type, @WebParam(name = "description")
        String description, @WebParam(name = "administrative")
        boolean administrative, @WebParam(name = "visible")
        boolean visible, @WebParam(name = "readOnly")
        boolean readOnly, @WebParam(name = "noCopy")
        boolean noCopy, @WebParam(name = "unique")
        boolean unique, @WebParam(name = "sessionId")
        String sessionId) throws Exception {

        try {
            wsBean.validateCall("createAttribute", getIPAddress(), sessionId);
            AttributeInfo ai = new AttributeInfo(name, displayName, type, administrative, 
                    visible, readOnly, unique, description, noCopy);

            wsBean.createAttribute(className, ai);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Adds an attribute to a classMetadata
     * @param ClassId Class id where the attribute will be attached
     * @param name attribute name
     * @param displayName attribute display name
     * @param type attribute type
     * @param description attribute description
     * @param administrative is the attribute administrative?
     * @param visible is the attribute visible?
     * @param readOnly is the attribute read only?
     * @param unique should this attribute be unique?
     * @param sessionId session token
     * @throws Exception IN case something goes wrong
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
        boolean unique, @WebParam(name = "sessionId")
        String sessionId) throws Exception {

        try {
            wsBean.validateCall("createAttributeForClassWithId", getIPAddress(), sessionId);
            AttributeInfo ai = new AttributeInfo(name, displayName, type, administrative, 
                                   visible, readOnly, unique, description, noCopy);

            wsBean.createAttribute(ClassId, ai);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    /**
     * Update a class attribute taking its class name as parameter
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
     * @throws Exception
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
        String sessionId) throws Exception {

        try {
            wsBean.validateCall("setClassAttributeProperties", getIPAddress(), sessionId);
            AttributeInfo ai = new AttributeInfo(attributeId, name, displayName, 
                    type, administrative, visible, readOnly, unique, description, noCopy);

            wsBean.setAttributeProperties(className, ai);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Update a class attribute taking its class id as parameter
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
     * @throws Exception
     */
    @WebMethod(operationName = "setAttributePropertiesForClassWithId")
    public void setAttributePropertiesForClassWithId(@WebParam(name = "classid")
        long classid, @WebParam(name = "attributeId")
        long attributeId, @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "type")
        String type, @WebParam(name = "description")
        String description, @WebParam(name = "administrative")
        Boolean administrative, @WebParam(name = "visible")
        Boolean visible, @WebParam(name = "readOnly")
        Boolean readOnly, @WebParam(name = "noCopy")
        Boolean noCopy, @WebParam(name = "unique")
        Boolean unique, @WebParam(name = "sessionId")
        String sessionId) throws Exception {

        try {
            wsBean.validateCall("setAttributePropertiesForClassWithId", getIPAddress(), sessionId);
            AttributeInfo ai = new AttributeInfo(attributeId, name, displayName, 
                    type, administrative, visible, readOnly, unique, description, noCopy);

            wsBean.setAttributeProperties(classid, ai);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    /**
     * Deletes a class attribute
     * @param className
     * @param attributeName
     * @param sessionId
     * @throws Exception 
     */
    
    @WebMethod(operationName = "deleteAttribute")
    public void deleteAttribute(@WebParam(name = "className") 
            String className, @WebParam(name = "attributeName")
            String attributeName, @WebParam(name = "sessionId")
            String sessionId) throws Exception{
        try {
            wsBean.validateCall("deleteAttribute", getIPAddress(), sessionId);
            wsBean.deleteAttribute(className, attributeName);
        } catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException){
                level = ((ServerSideException)e).getLevel();
            }
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Deletes a class attribute
     * @param classId
     * @param attributeName
     * @param sessionId
     * @throws Exception 
     */
    @WebMethod(operationName = "deleteAttributeForClassWithId")
    public void deleteAttributeForClassWithId(@WebParam(name = "classId") 
            long classId, @WebParam(name = "attributeName")
            String attributeName, @WebParam(name = "sessionId")
            String sessionId) throws Exception{
        try {
            wsBean.validateCall("deleteAttributeForClassWithId", getIPAddress(), sessionId);
            wsBean.deleteAttribute(classId, attributeName);
        } catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException){
                level = ((ServerSideException)e).getLevel();
            }
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Gets the metadata for a given class using its name as argument
     * @param className Class name
     * @param sessionId Session token
     * @return The metadata as a ClassInfo instance
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getClass")
    public ClassInfo getClass(@WebParam(name = "className")
    String className, @WebParam(name = "sessionId")
    String sessionId) throws Exception {

        try {
            wsBean.validateCall("getClass", getIPAddress(), sessionId);
            return wsBean.getClass(className);
        } catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Gets the metadata for a given class using its id as argument
     * @param classId Class metadata object id
     * @param sessionId session token
     * @return The metadata as a ClassInfo instance
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getClassWithId")
    public ClassInfo getClassWithId(@WebParam(name = "classId")
    long classId, @WebParam(name = "sessionId")
    String sessionId) throws Exception {
        try {
            wsBean.validateCall("getClassWithId", getIPAddress(), sessionId);
            return wsBean.getClass(classId);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

     
    
    /**
     * Gets the subclasses of a given class
     * @param className Class name
     * @param includeAbstractClasses should the result include the abstract classes?
     * @param includeSelf Should the list include the subclasses and the parent class?
     * @param sessionId Session token
     * @return The list of subclasses
     * @throws Exception Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getSubClassesLight")
    public List<ClassInfoLight> getSubClassesLight(
            @WebParam(name = "className")String className,
            @WebParam(name = "includeAbstractClasses")boolean includeAbstractClasses,
            @WebParam(name = "includeSelf")boolean includeSelf,
            @WebParam(name = "sessionId") String sessionId) throws Exception{
        try
        {
            wsBean.validateCall("getSubClassesLight", getIPAddress(), sessionId);
            return wsBean.getSubClassesLight(className, includeAbstractClasses, includeSelf);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }

    }
    
    /**
     * Gets the subclasses of a given class
     * @param className Class name
     * @param includeAbstractClasses should the result include the abstract classes?
     * @param includeSelf Should the list include the subclasses and the parent class?
     * @param sessionId Session token
     * @return The list of subclasses
     * @throws Exception Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getSubClassesLightNoRecursive")
    public List<ClassInfoLight> getSubClassesLightNoRecursive(
            @WebParam(name = "className")String className,
            @WebParam(name = "includeAbstractClasses")boolean includeAbstractClasses,
            @WebParam(name = "includeSelf")boolean includeSelf,
            @WebParam(name = "sessionId") String sessionId) throws Exception{
        try
        {
            wsBean.validateCall("getSubClassesLightNoRecursive", getIPAddress(), sessionId);
            return wsBean.getSubClassesLightNoRecursive(className, includeAbstractClasses, includeSelf);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }

    }

    /**
     * Retrieves the metadata for the entire class hierarchy as ClassInfo instances
     * @param sessionId Session token
     * @param includeListTypes boolean to indicate if the list should include the subclasses of
     * GenericObjectList
     * @return An array with the metadata for the entire class hierarchy as ClassInfo instances
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getAllClasses")
    public List<ClassInfo> getAllClasses(
            @WebParam(name = "includeListTypes")boolean includeListTypes,
            @WebParam(name = "sessionId") String sessionId) throws Exception{
        try
        {
            wsBean.validateCall("getAllClasses", getIPAddress(), sessionId);
            return wsBean.getAllClasses(includeListTypes);
        } catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    /**
     * Retrieves the metadata for the entire class hierarchy as ClassInfoLight instances
     * @param sessionId Session token
     * @param includeListTypes boolean to indicate if the list should include the subclasses of
     * GenericObjectList
     * @return An array with the metadata for the entire class hierarchy as ClassInfoLight instances
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getAllClassesLight")
    public List<ClassInfoLight> getAllClassesLight(
            @WebParam(name = "includeListTypes")boolean includeListTypes,
            @WebParam(name = "sessionId") String sessionId) throws Exception{
        try
        {
            wsBean.validateCall("getAllClassesLight", getIPAddress(), sessionId);
            return wsBean.getAllClassesLight(includeListTypes);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }

    }

    /**
     * Deletes a class metadata entry for a given class using its name as argument
     * @param className Class name
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     *
     */
    @WebMethod(operationName = "deleteClass")
    public void deleteClass(@WebParam(name = "className")
    String className, @WebParam(name = "sessionId")
    String sessionId) throws Exception {

        try {
            wsBean.validateCall("deleteClass", getIPAddress(), sessionId);
            wsBean.deleteClass(className);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Deletes a class metadata entry for a given class using its id as argument
     * @param classId Class id
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */

    @WebMethod(operationName = "deleteClassWithId")
    public void deleteClassWithId(@WebParam(name = "classId")
    long classId, @WebParam(name = "sessionId")
    String sessionId) throws Exception {

        try {
            wsBean.validateCall("deleteClassWithId", getIPAddress(), sessionId);
            wsBean.deleteClass(classId);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Get the possible children of a class according to the containment hierarchy. This method is recursive, and if a possible child is an abstract class, it gets its non-abstract subclasses
     * @param parentClassName Class to retrieve its possible children
     * @param sessionId Session token
     * @return A list of possible children as ClassInfoLight instances
     * An array with the metadata for the entire class hierarchy as ClassInfoLight instances
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getPossibleChildren")
    public List<ClassInfoLight> getPossibleChildren(@WebParam(name = "parentClassName")
                    String parentClassName, @WebParam(name = "sessionId")
                    String sessionId) throws Exception {

        try {
            wsBean.validateCall("getPossibleChildren", getIPAddress(), sessionId);
            return wsBean.getPossibleChildren(parentClassName);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Get the possible children of a class according to the containment hierarchy.
     * This method is not recursive, and only returns the direct possible children,
     * even if they're abstract
     * @param parentClassName Class to retrieve its possible children
     * @param sessionId Session token
     * @return An array with the metadata for the entire class hierarchy as ClassInfoLight instances
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getPossibleChildrenNoRecursive")
    public List<ClassInfoLight> getPossibleChildrenNoRecursive(@WebParam(name = "parentClassName")
    String parentClassName, @WebParam(name = "sessionId")
    String sessionId) throws Exception {

        try {
            wsBean.validateCall("getPossibleChildrenNoRecursive", getIPAddress(), sessionId);
            return wsBean.getPossibleChildrenNoRecursive(parentClassName);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    /**
     * Adds possible children to a given class using its id as argument. If any of the arguments provided are already added,
     * it will abort the operation and rise an exception
     * @param parentClassId Class to attach the new possible children
     * @param newPossibleChildren List of nre possible children. Abstract classes are de-aggregated
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "addPossibleChildrenForClassWithId")
    public void addPossibleChildrenForClassWithId(@WebParam(name = "parentClassId")
            long parentClassId, @WebParam(name = "childrenToBeAdded")
            long[] newPossibleChildren, @WebParam(name = "sessionId")
            String sessionId) throws Exception {

        try {
            wsBean.validateCall("addPossibleChildrenForClassWithId", getIPAddress(), sessionId);
            wsBean.addPossibleChildren(parentClassId, newPossibleChildren);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

     /**
     * Adds possible children to a given class using its name as argument.
     * If any of the arguments provided are already added,
     * it will abort the operation and rise an exception
     * @param parentClassName Class to attach the new possible children
     * @param childrenToBeAdded List of nre possible children. Abstract classes are de-aggregated
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "addPossibleChildren")
    public void addPossibleChildren(@WebParam(name = "parentClassName")
            String parentClassName, @WebParam(name = "childrenToBeAdded")
            String[] childrenToBeAdded, @WebParam(name = "sessionId")
            String sessionId) throws Exception {
        try {
            wsBean.validateCall("addPossibleChildren", getIPAddress(), sessionId);
            wsBean.addPossibleChildren(parentClassName, childrenToBeAdded);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Removes a set of possible children for a given class
     * @param parentClassId Class the possible children are going to be removed from
     * @param childrenToBeRemoved List of ids of classes to be removed as possible children
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "removePossibleChildrenForClassWithId")
    public void removePossibleChildrenForClassWithId(@WebParam(name = "parentClassId")
    long parentClassId, @WebParam(name = "childrenToBeRemoved")
    long[] childrenToBeRemoved, @WebParam(name = "sessionId")
    String sessionId) throws Exception {

        try{
            wsBean.validateCall("removePossibleChildrenForClassWithId", getIPAddress(), sessionId);
            wsBean.removePossibleChildren(parentClassId, childrenToBeRemoved);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     *
     * @param className
     * @param recursive
     * @return Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getUpstreamContainmentHierarchy")
    public List<ClassInfoLight> getUpstreamContainmentHierarchy(@WebParam(name = "className")
            String className, @WebParam(name = "recursive")
            boolean recursive, @WebParam(name = "sessionId")
            String sessionId) throws Exception {
        try{
            wsBean.validateCall("getUpstreamContainmentHierarchy", getIPAddress(), sessionId);
            return wsBean.getUpstreamContainmentHierarchy(className, recursive);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Sync/Load methods. Click on the + sign on the left to edit the code.">/**
    @WebMethod(operationName = "loadDataFromFile")
    public String loadDataFromFile(@WebParam(name = "chossenFile")
        byte[] choosenFile, @WebParam(name = "sessionId")
            String sessionId) throws Exception {
        try{
            wsBean.validateCall("loadDataFromFile", getIPAddress(), sessionId);
            UserInfo user = wsBean.getUserInSession(sessionId);
            return wsBean.loadDataFromFile(choosenFile, user.getOid());
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    
    @WebMethod(operationName = "downloadErrors")
    public byte[] downloadErrors(@WebParam(name = "fileName")
        String fileName, @WebParam(name = "sessionId")
            String sessionId) throws Exception {
        try{
            wsBean.validateCall("downloadErrors", getIPAddress(), sessionId);
            return wsBean.downloadErrors(fileName);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
     
    @WebMethod(operationName = "downloadLog")
    public byte[] downloadLog(@WebParam(name = "fileName")
        String fileName, @WebParam(name = "sessionId")
            String sessionId) throws Exception {
        try{
            wsBean.validateCall("downloadLog", getIPAddress(), sessionId);
            return wsBean.downloadLog(fileName);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Helpers. Click on the + sign on the left to edit the code.">/**
    /**
     * Gets the IP address from the client issuing the request
     * @return the IP address as string
     */
    private String getIPAddress(){
        return ((HttpServletRequest)context.getMessageContext().
                    get("javax.xml.ws.servlet.request")).getRemoteAddr().toString(); //NOI18N
    }// </editor-fold>
}

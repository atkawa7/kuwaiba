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
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.util.Constants;
import org.kuwaiba.ws.todeserialize.TransientQuery;
import org.kuwaiba.ws.toserialize.application.ApplicationLogEntry;
import org.kuwaiba.ws.toserialize.application.RemoteQuery;
import org.kuwaiba.ws.toserialize.application.RemoteQueryLight;
import org.kuwaiba.ws.toserialize.application.RemoteSession;
import org.kuwaiba.ws.toserialize.application.ResultRecord;
import org.kuwaiba.ws.toserialize.application.GroupInfo;
import org.kuwaiba.ws.toserialize.application.UserInfo;
import org.kuwaiba.ws.toserialize.application.ViewInfo;
import org.kuwaiba.ws.toserialize.application.ViewInfoLight;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.business.RemoteObjectSpecialRelationships;
import org.kuwaiba.ws.toserialize.metadata.AttributeInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfoLight;

/**
 * Main webservice
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
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
     * Creates a session
     * @param username user login name
     * @param password user password
     * @return A session object, including the session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createSession")
    public RemoteSession createSession(@WebParam(name = "username") String username,
            @WebParam(name = "password") String password) throws Exception{
        try{
            String remoteAddress = getIPAddress();
            return wsBean.createSession(username, password, remoteAddress);
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
     * @param sessionId Session token
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
     * Retrieves the list of uses
     * @param sessionId session token
     * @return The list of users
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */

    @WebMethod(operationName = "getUsers")
    public UserInfo[] getUsers(@WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            return wsBean.getUsers(getIPAddress(), sessionId);
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
     * Retrieves the list of groups
     * @param sessionId Session token
     * @return A group object list
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getGroups")
    public GroupInfo[] getGroups(@WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            return wsBean.getGroups(getIPAddress(), sessionId);
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
            @WebParam(name = "privileges")long[] privileges,
            @WebParam(name = "groups")long[] groups,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            return wsBean.createUser(username, password, firstName, lastName, enabled, privileges, groups, getIPAddress(), sessionId);
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
            @WebParam(name = "privileges")long[] privileges,
            @WebParam(name = "groups")long[] groups,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            wsBean.setUserProperties(oid, username, password, firstName, lastName, enabled, privileges, groups, getIPAddress(), sessionId);
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
            @WebParam(name = "privileges")long[] privileges,
            @WebParam(name = "users")long[] users,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            return wsBean.createGroup(groupName, description, privileges, users, getIPAddress(), sessionId);
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
            @WebParam(name = "privileges")long[] privileges,
            @WebParam(name = "users")long[] users,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            wsBean.setGroupProperties(oid, groupName, description, privileges, users, getIPAddress(), sessionId);
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
            wsBean.deleteUsers(oids, getIPAddress(), sessionId);
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
            wsBean.deleteGroups(oids, getIPAddress(), sessionId);
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
     * Gets a particular view related to an object
     * @param oid Object id
     * @param objectClass Object class
     * @param viewId The view id
     * @param sessionId Session token
     * @return The View object (which is basically an XML document)
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectRelatedView")
    public ViewInfo getObjectRelatedView(@WebParam(name = "oid")long oid,
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "viewId")long viewId,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return wsBean.getObjectRelatedView(oid, objectClass, viewId, getIPAddress(), sessionId);
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
     * Returns list of views associated to an object
     * @param oid Object id
     * @param objectClass Object class
     * @param viewType View type
     * @param limit Max number of results
     * @param sessionId Session token
     * @return List of objects related to the object
     * @throws Exception Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectRelatedViews")
    public ViewInfoLight[] getObjectRelatedViews(@WebParam(name = "oid")long oid,
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "viewType")int viewType,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return wsBean.getObjectRelatedViews(oid, objectClass, viewType, limit, getIPAddress(), sessionId);
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
     * Gets all views that are not related to a particular object
     * @param viewType View type. Used to filter. Use 0 retrieve all
     * @param limit Max number if reaults
     * @param sessionId Session token
     * @return A list of views
     * @throws Exception Exception Generic exception encapsulating any possible error raised at runtime 
     */
    @WebMethod(operationName = "getGeneralViews")
    public ViewInfoLight[] getGeneralViews(@WebParam(name = "viewType")int viewType,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return wsBean.getGeneralViews(viewType, limit, getIPAddress(), sessionId);
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
     * Gets the information of a particular view
     * @param viewId View id
     * @param sessionId Session token
     * @return The view
     * @throws Exception Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getGeneralView")
    public ViewInfo getGeneralView(@WebParam(name = "viewId")long viewId,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return wsBean.getGeneralView(viewId, getIPAddress(), sessionId);
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
     * Creates a view an relates it to an existing object
     * @param objectId Object id
     * @param objectClass Object class
     * @param name View name
     * @param description View description
     * @param viewType View type
     * @param structure Structure (as an XML document)
     * @param background Background
     * @param sessionId Session id
     * @return The id of the newly created view
     * @throws Exception Exception Generic exception encapsulating any possible error raised at runtime
     */
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
            return wsBean.createObjectRelatedView(objectId, objectClass, name, description, viewType, structure, background, getIPAddress(), sessionId);
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
     * Creates a general view (a view that is not associated to any object)
     * @param viewType View type
     * @param name View name
     * @param description Description
     * @param structure Structure
     * @param background background
     * @param sessionId Session id
     * @return The id of the newly created view
     * @throws Exception Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createGeneralView")
    public long createGeneralView(@WebParam(name = "viewType")int viewType,
            @WebParam(name = "name")String name,
            @WebParam(name = "description")String description,
            @WebParam(name = "structure")byte[] structure,
            @WebParam(name = "background")byte[] background,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return wsBean.createGeneralView(viewType, name, description, structure, background, getIPAddress(), sessionId);
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
     * Updates an object view (a view that is linked to a particular object)
     * @param objectOid Object id
     * @param objectClass Object class
     * @param viewId View id
     * @param viewName View name. Null to leave unchanged
     * @param viewDescription View description. Null to leave unchanged
     * @param structure View structure. Null to leave unchanged
     * @param background Background. Null to leave unchanged
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "updateObjectRelatedView")
    public void updateObjectRelatedView(@WebParam(name = "objectOid")long objectOid,
            @WebParam(name = "objectClass")String objectClass, @WebParam(name = "viewId")long viewId,
            @WebParam(name = "viewName")String viewName, @WebParam(name = "viewDescription")String viewDescription,
            @WebParam(name = "structure")byte[] structure,
            @WebParam(name = "background")byte[] background, @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.updateObjectRelatedView(objectOid, objectClass, viewId, viewName, viewDescription, structure, background, getIPAddress(), sessionId);
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
     * Updates a general view (a view that is not linked to any particular object)
     * @param viewId View id
     * @param viewName View name. Null to leave unchanged
     * @param viewDescription View Description. Null to leave unchanged
     * @param structure View structure. Null to leave unchanged
     * @param background Background. Null to leave unchanged
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "updateGeneralView")
    public void updateGeneralView(@WebParam(name = "viewId")long viewId,
            @WebParam(name = "viewName")String viewName, @WebParam(name = "viewDescription")String viewDescription,
            @WebParam(name = "structure")byte[] structure, @WebParam(name = "background")byte[] background, @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.updateGeneralView(viewId, viewName, viewDescription, structure, background, getIPAddress(), sessionId);
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
     * @param oids Ids of the views to be deleted
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteGeneralView")
    public void deleteGeneralView(@WebParam(name = "oids")long [] oids,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.deleteGeneralView(oids, getIPAddress(), sessionId);
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
            return wsBean.createListTypeItem(className, name, displayName, getIPAddress(), sessionId);
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
            wsBean.deleteListTypeItem(className, oid, releaseRelationships, getIPAddress(), sessionId);
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
            return wsBean.getListTypeItems(className, getIPAddress(), sessionId);
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
            return wsBean.getInstanceableListTypes(getIPAddress(), sessionId);
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
            return wsBean.executeQuery(query, getIPAddress(), sessionId);
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
            return wsBean.createQuery(queryName, ownerOid, queryStructure, description, getIPAddress(), sessionId);
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
            wsBean.saveQuery(queryOid, queryName, ownerOid, queryStructure, description, getIPAddress(), sessionId);
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
            wsBean.deleteQuery(queryOid, getIPAddress(), sessionId);
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
            return wsBean.getQueries(showPublic, getIPAddress(), sessionId);
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
            return wsBean.getQuery(queryOid, getIPAddress(), sessionId);
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
            return wsBean.getClassHierarchy(showAll, getIPAddress(), sessionId);
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
     * @param parentId Id of the parent of this pool. -1 for none
     * @param name Pool name
     * @param description Pool description
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @param sessionId Session identifier
     * @return id of the new pool
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createPool")
    public long createPool(@WebParam(name = "parentid")long parentId,
            @WebParam(name = "name")String name,
            @WebParam(name = "description")String description,
            @WebParam(name = "instancesOfClass")String instancesOfClass,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return wsBean.createPool(parentId, name, description, instancesOfClass, getIPAddress(),  sessionId);
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
            return wsBean.createPoolItem(poolId, className, attributeNames, attributeValues, templateId, getIPAddress(), sessionId);
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
            wsBean.deletePools(ids, getIPAddress(), sessionId);
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
     * Get a set of pools for a specific parent
     * @param limit Maximum number of pool records to be returned
     * @param parentId Pool's parent id
     * @param className class type for the pools
     * @param sessionId Session token
     * @return The list of pools as RemoteObjectLight instances for an specific parent
     * @throws Exception Generic exception encapsulating any possible error raised at runtime 
     */
    @WebMethod(operationName = "getPoolsForParentWithId")
    public RemoteObjectLight[] getPoolsForParentWithId(@WebParam(name = "limit")
            int limit, @WebParam(name = "parentId") 
            long parentId, @WebParam(name = "className") 
            String className, @WebParam(name = "sessionId") 
            String sessionId) throws Exception{
        try{
            return wsBean.getPools(limit, parentId, className, getIPAddress(), sessionId);
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
     * @param className class type for the pools
     * @param sessionId Session identifier
     * @return The list of pools as RemoteObjectLight instances
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getPools")
    public RemoteObjectLight[] getPools(@WebParam(name = "limit")int limit,
            @WebParam(name = "className") String className,
            @WebParam(name = "sessionId") String sessionId) throws Exception{
        try{
            return wsBean.getPools(limit, className, getIPAddress(), sessionId);
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
            return wsBean.getPoolItems(poolId, limit, getIPAddress(), sessionId);
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
            RemoteObjectLight[] res = wsBean.getObjectChildren(oid,objectClassId, maxResults, getIPAddress(), sessionId);
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
            RemoteObjectLight[] res = wsBean.getObjectChildren(objectClassName, oid, maxResults, getIPAddress(), sessionId);
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
     * Returns the siblings of an object in the containment hierarchy
     * @param objectClassName Object class
     * @param oid Object oid
     * @param maxResults Max number of results to be returned
     * @param sessionId Session token
     * @return List of siblings
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getSiblings")
    public RemoteObjectLight[] getSiblings(@WebParam(name = "objectClassName") String objectClassName,
            @WebParam(name = "oid") long oid,
            @WebParam(name = "maxResults") int  maxResults,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            RemoteObjectLight[] res = wsBean.getSiblings(objectClassName, oid, maxResults, getIPAddress(), sessionId);
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
            RemoteObject[] res = wsBean.getChildrenOfClass(parentOid,parentClass,childrenClass, maxResults, getIPAddress(), sessionId);
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
            return wsBean.getChildrenOfClassLight(parentOid,parentClass,childrenClass,maxResults, getIPAddress(), sessionId);
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
            return wsBean.getObject(objectClass, oid, getIPAddress(), sessionId);
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
            return wsBean.getObjectLight(objectClass, oid, getIPAddress(), sessionId);
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
     * Gets all objects of a given class
     * @param className Class name
     * @param maxResults Max number of results. 0 to retriever all
     * @param sessionId Session token
     * @return A list of instances of @className
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectsOfClassLight")
    public RemoteObjectLight[] getObjectsOfClassLight(@WebParam(name = "className") String className,
            @WebParam(name = "maxResults")int maxResults,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return wsBean.getObjectsOfClassLight(className, maxResults, getIPAddress(), sessionId);
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
            return wsBean.getParent(objectClass, oid, getIPAddress(), sessionId);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    @WebMethod(operationName = "getParents")
    public RemoteObjectLight[] getParents(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") long oid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return wsBean.getParents(objectClass, oid, getIPAddress(), sessionId);
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
     * Returns all the special relationships of a given object as a hashmap whose keys are
     * the names of the relationships and the values the list of related objects
     * @param objectClass Object class
     * @param oid Object id
     * @param sessionId Session token
     * @return
     * @throws Exception If case something goes wrong
     */
    @WebMethod(operationName = "getSpecialAttributes")
    public RemoteObjectSpecialRelationships getSpecialAttributes(@WebParam(name = "objectClass") String objectClass,
            @WebParam(name = "oid") long oid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return wsBean.getSpecialAttributes(objectClass, oid, getIPAddress(), sessionId);
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
            return wsBean.getParentOfClass(objectClass, oid, parentClass, getIPAddress(), sessionId);
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
    public RemoteObjectLight[] getSpecialAttribute(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") long oid,
            @WebParam(name = "attributename") String attributeName,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return wsBean.getSpecialAttribute(objectClass, oid, attributeName, getIPAddress(), sessionId);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    @WebMethod(operationName = "getObjectSpecialChildren")
    public RemoteObjectLight[] getObjectSpecialChildren (@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "objectId") long objectId,
            @WebParam(name = "sessionId") String sessionId) throws Exception {
        try{
            return wsBean.getObjectSpecialChildren(objectClass, objectId, getIPAddress(), sessionId);
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
            wsBean.updateObject(className,oid,attributeNames, attributeValues, getIPAddress(), sessionId);
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
            return wsBean.createObject(className,parentObjectClassName, parentOid,attributeNames,attributeValues, templateId, getIPAddress(), sessionId);
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "createSpecialObject")
    public long createSpecialObject(@WebParam(name = "className")String className,
            @WebParam(name = "parentObjectClassName")String parentObjectClassName,
            @WebParam(name = "parentOid")long parentOid,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[][] attributeValues,
            @WebParam(name = "templateId")long templateId,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return wsBean.createSpecialObject(className,parentObjectClassName, parentOid,attributeNames,attributeValues, templateId, getIPAddress(), sessionId);
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
            wsBean.deleteObjects(classNames,oids, releaseRelationships, getIPAddress(), sessionId);
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
            wsBean.moveObjects(targetClass,targetOid, objectClasses, objectOids, getIPAddress(), sessionId);
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
            return wsBean.copyObjects(targetClass,targetOid, objectClasses, objectOids, recursive, getIPAddress(), sessionId);
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
     * Connect two ports using a mirror relationship
     * @param aObjectClass Port A class
     * @param aObjectId Port A id
     * @param bObjectClass Port B class
     * @param bObjectId Port B id
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime 
     */
    @WebMethod(operationName = "connectMirrorPort")
    public void connectMirrorPort(
            @WebParam(name = "aObjectClass")String aObjectClass,
            @WebParam(name = "aObjectId")long aObjectId,
            @WebParam(name = "bObjectClass")String bObjectClass,
            @WebParam(name = "bObjectId")long bObjectId,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.connectMirrorPort(aObjectClass, aObjectId, bObjectClass, bObjectId, getIPAddress(), sessionId);
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
     * Releases a port mirroring relationship between two ports, receiving one of the ports as parameter
     * @param objectClass Object class
     * @param objectId Object id
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "releaseMirrorPort")
    public void releaseMirrorPort(
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")long objectId,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.releaseMirrorPort(objectClass, objectId, getIPAddress(), sessionId);
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime   
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
            return wsBean.createPhysicalConnection(aObjectClass, aObjectId,bObjectClass, bObjectId,
                   parentClass, parentId, attributeNames, attributeValues, connectionClass, getIPAddress(), sessionId);
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
     * Allows to create multiple connections at once
     * @param connectionClass Class all the connections are going to be instance of
     * @param numberOfChildren Number of connections to be created
     * @param parentClass Class of the parent object to the connections. Null for none, anything for DummyRoot
     * @param parentId Id of the parent object to the connections. Anything none, -1 for DummyRoot
     * @param sessionId Session token
     * @return The ids of the new objects
     * @throws Exception Generic exception encapsulating any possible error raised at runtime   
     */
    public long[] createBulkPhysicalConnections(@WebParam(name = "connectionClass")String connectionClass, 
            @WebParam(name = "numberOfChildren")int numberOfChildren, 
            @WebParam(name = "parentClass")String parentClass, 
            @WebParam(name = "parentId")long parentId, 
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return wsBean.createBulkPhysicalConnections(connectionClass, numberOfChildren,
                   parentClass, parentId, getIPAddress(), sessionId);
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
     * Returns the endpoints of a physical connection
     * @param connectionClass Connection class
     * @param connectionId Connection id
     * @param sessionId Session token
     * @return An array of two positions: the first is the A endpoint and the second is the B endpoint
     * @throws Exception Generic exception encapsulating any possible error raised at runtime   
     */
    public RemoteObjectLight[] getConnectionEndpoints(@WebParam(name = "connectionClass")String connectionClass, 
            @WebParam(name = "connectionId")long connectionId, 
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return wsBean.getConnectionEndpoints(connectionClass, connectionId, getIPAddress(), sessionId);
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
     * Gets the physical trace of connections and ports from a port
     * @param objectClass Object class
     * @param objectId Object id
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "getPhysicalPath")
    public RemoteObjectLight[] getPhysicalPath (@WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")long objectId,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return wsBean.getPhysicalPath(objectClass, objectId, getIPAddress(), sessionId);
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
     * Connect pairs of ports (if they are not connected already) using physical link (cable, fibers, all subclasses of GenericPhysicalConnection)
     * @param sideAClassNames The list of classes of one of the sides of the connection
     * @param sideAIds The list of ids the objects on one side of the connection
     * @param linksClassNames the classes of the links that will connect the two sides
     * @param linksIds The ids of these links
     * @param sideBClassNames The list of classes of the other side of the connection
     * @param sideBIds The list of ids the objects on the other side of the connection
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime   
     */
    public void connectPhysicalLinks (@WebParam(name = "sideAClassNames")String[] sideAClassNames, @WebParam(name = "sideAIds")Long[] sideAIds,
                                      @WebParam(name = "linksClassNames")String[] linksClassNames, @WebParam(name = "linksIds")Long[] linksIds,
                                      @WebParam(name = "sideBClassNames")String[] sideBClassNames, @WebParam(name = "sideBIds")Long[] sideBIds,
                                      @WebParam(name = "sessionId")String sessionId) throws Exception {
        try{
            if ((sideAClassNames.length + sideAIds.length + linksClassNames.length + linksIds.length + sideBClassNames.length + sideBIds.length) / 4 != sideAClassNames.length)
                throw new Exception("The array sizes don't match");
            
            wsBean.connectPhysicalLinks(sideAClassNames, sideAIds, linksClassNames, linksIds, sideBClassNames, sideBIds, getIPAddress(), sessionId);
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
            wsBean.deletePhysicalConnection(objectClass, objectId, getIPAddress(), sessionId);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "associateObjectToService")
    public void associateObjectToService (
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")long objectId,
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "serviceId")long serviceId,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try{
            wsBean.associateObjectToService(objectClass, objectId, serviceClass, serviceId, getIPAddress(), sessionId);
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
     * Releases an object from a service that is using it
     * @param serviceClass Service class
     * @param serviceId Service id
     * @param targetId target object id
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime    
     */
    @WebMethod(operationName = "releaseObjectFromService")
    public void releaseObjectFromService (
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "serviceId")long serviceId,
            @WebParam(name = "targetId")long targetId,           
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try{
            wsBean.releaseObjectFromService(serviceClass, serviceId, targetId, getIPAddress(), sessionId);
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
     * Gets the services associated to a service 
     * @param serviceClass Service class
     * @param serviceId Service id
     * @param sessionId Session token
     * @return A list of services
     * @throws Exception Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "getServiceResources")
    public RemoteObjectLight[] getServiceResources (
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "serviceId")long serviceId,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try{
            return wsBean.getServiceResources(serviceClass, serviceId, getIPAddress(), sessionId);
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
     * Creates a customer (with no parent in the containment hierarchy)
     * @param customerClass Customer class
     * @param attributes Default attributes to be set
     * @param attributeValues Default values to #attributes
     * @param sessionId Session token
     * @return The id of the newly created customer
     * @throws Exception Generic exception encapsulating any possible error raised at runtime   
     */
    @WebMethod(operationName = "createCustomer")
    public long createCustomer (
            @WebParam(name = "customerClass")String customerClass,
            @WebParam(name = "attributes")String[] attributes,
            @WebParam(name = "attributeValues")String[] attributeValues,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try{
            return wsBean.createCustomer(customerClass, attributes, attributeValues, getIPAddress(), sessionId);
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
     * Creates a service and relates it to a customer
     * @param serviceClass Service class
     * @param customerClass Customer class
     * @param customerId Customer id
     * @param attributes Service attributes
     * @param attributeValues Default values to #attributes
     * @param sessionId Session token
     * @return The id of the newly created service
     * @throws Exception Generic exception encapsulating any possible error raised at runtime  
     */
    @WebMethod(operationName = "createService")
    public long createService (
            @WebParam(name = "serviceClass")String serviceClass,
            @WebParam(name = "customerClass")String customerClass,
            @WebParam(name = "customerId")long customerId,
            @WebParam(name = "attributes")String[] attributes,
            @WebParam(name = "attributeValues")String[] attributeValues,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try{
            return wsBean.createService(serviceClass, customerClass, customerId, attributes, attributeValues, getIPAddress(), sessionId);
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
     * Returns the services associated to a customer
     * @param customerClass Customer class
     * @param customerId Customer Id
     * @param sessionId Session token
     * @return The list of services related to the give customer
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getServices")
    public RemoteObjectLight[] getServices(@WebParam(name = "customerClass")String customerClass, 
            @WebParam(name = "customerId")long customerId,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return wsBean.getServices(customerClass, customerId, getIPAddress(), sessionId);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    //Audit Trail
    /**
     * Retrieves the log entries for a given [business] object
     * @param objectClass Object class
     * @param objectId Object id
     * @param limit Max number of results (0 to retrieve all)
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getBusinessObjectAuditTrail")
    public ApplicationLogEntry[] getBusinessObjectAuditTrail (
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")long objectId,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try{
            return wsBean.getBusinessObjectAuditTrail (objectClass, objectId, limit, getIPAddress(), sessionId);
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
     * Retrieves the list of activity log entries
     * @param page current page
     * @param limit limit of results per page. 0 to retrieve them all
     * @return The list of activity log entries
     */
    @WebMethod(operationName = "getGeneralActivityAuditTrail")
    public ApplicationLogEntry[] getGeneralActivityAuditTrail (
            @WebParam(name = "page")int page,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try{
            return wsBean.getGeneralActivityAuditTrail (page, limit, getIPAddress(), sessionId);
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
     * Retrieves the log entries for a given [application] object (Users, Pools, etc)
     * @param objectClass Object class
     * @param objectId Object id
     * @param limit Max number of results (0 for all)
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getApplicationObjectAuditTrail")
    public void getApplicationObjectAuditTrail (
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")long objectId,
            @WebParam(name = "limit")int limit,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.getApplicationObjectAuditTrail (objectClass, objectId, limit, getIPAddress(), sessionId);
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
        byte[] smallIcon, @WebParam(name = "color")
        int color, @WebParam(name = "sessionId")
        String sessionId) throws Exception {
        
        try{
            if (icon != null){
                if (icon.length > Constants.MAX_ICON_SIZE){
                    throw new ServerSideException(Level.WARNING, String.format("The uploaded file exceeds the max file size (%s)", Constants.MAX_BACKGROUND_SIZE));
                }
            }
            if (smallIcon != null){
                if (smallIcon.length > Constants.MAX_ICON_SIZE){
                    throw new ServerSideException(Level.WARNING, String.format("The uploaded file exceeds the max file size (%s)", Constants.MAX_BACKGROUND_SIZE));
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
            ci.setAbstract(_abstract);
            ci.setCountable(countable);
            ci.setCustom(custom);
            ci.setInDesign(inDesign);

            return wsBean.createClass(ci, getIPAddress(), sessionId);

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
        byte[] icon, @WebParam(name = "color")
        int color,@WebParam(name = "_abstract")
        Boolean _abstract, @WebParam(name = "inDesign")
        Boolean inDesign, @WebParam(name = "custom")
        Boolean custom, @WebParam(name = "countable")
        Boolean countable, @WebParam(name = "sessionId")
        String sessionId) throws Exception {
        try
        {
            if (icon != null){
                if (icon.length > Constants.MAX_ICON_SIZE)
                    throw new ServerSideException(Level.WARNING, String.format("The file exceeds the file size limits (%s)", Constants.MAX_BACKGROUND_SIZE));
            }
            if (smallIcon != null){
                if (smallIcon.length > Constants.MAX_ICON_SIZE)
                    throw new ServerSideException(Level.WARNING, String.format("The file exceeds the file size limits (%s)", Constants.MAX_BACKGROUND_SIZE));
            }
            ClassInfo ci = new ClassInfo();
            ci.setId(classId);
            ci.setClassName(name);
            ci.setDisplayName(displayName);
            ci.setDescription(description);
            ci.setIcon(icon);
            ci.setSmallIcon(smallIcon);
            ci.setColor(color);
            ci.setAbstract(_abstract);
            ci.setInDesign(inDesign);
            ci.setCountable(countable);
            ci.setCustom(custom);

            wsBean.setClassProperties(ci, getIPAddress(), sessionId);

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
     * Gets a class attribute, using the class name as key to find it
     * @param className the class name
     * @param sessionId Session token
     * @return the class attribute
     * @throws Exception Generic exception encapsulating any possible error raised at runtime  
     */
    @WebMethod(operationName = "getAttribute")
    public AttributeInfo getAttribute(@WebParam(name = "className")
    String className, @WebParam(name = "attributeName")
    String attributeName, @WebParam(name = "sesionId")
    String sessionId) throws Exception{
        try {
            return wsBean.getAttribute(className, attributeName, getIPAddress(), sessionId);
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
     * Gets a class attribute, using the class id as key to find it
     * @param classId Class id
     * @param attributeName Attribute name
     * @param sessionId  Session token
     * @return The attribute definition
     * @throws Exception Generic exception encapsulating any possible error raised at runtime 
     */
    @WebMethod(operationName = "getAttributeForClassWithId")
    public AttributeInfo getAttributeForClassWithId(@WebParam(name = "classId")
        String classId, @WebParam(name = "attributeName")
        String attributeName, @WebParam(name = "sessionId")
        String sessionId) throws Exception{
        try {
            return wsBean.getAttribute(classId, attributeName, getIPAddress(), sessionId);
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
     * Adds an attribute to a class using its name as key to find it
     * @param className Class name where the attribute will be attached
     * @param name attribute name
     * @param displayName attribute display name
     * @param type attribute type
     * @param description attribute description
     * @param administrative is the attribute administrative?
     * @param visible is the attribute visible?
     * @param readOnly is the attribute read only?
     * @param unique should this attribute be unique?
     * @param sessionId session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
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
            AttributeInfo ai = new AttributeInfo(name, displayName, type, administrative, 
                    visible, readOnly, unique, description, noCopy);

            wsBean.createAttribute(className, ai, getIPAddress(), sessionId);

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
     * Adds an attribute to a class using its id as key to find it
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
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
            AttributeInfo ai = new AttributeInfo(name, displayName, type, administrative, 
                                   visible, readOnly, unique, description, noCopy);

            wsBean.createAttribute(ClassId, ai, getIPAddress(), sessionId);

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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
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
            AttributeInfo ai = new AttributeInfo(attributeId, name, displayName, 
                    type, administrative, visible, readOnly, unique, description, noCopy);
            wsBean.setAttributeProperties(className, ai, getIPAddress(), sessionId);
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
     * Updates a class attribute taking its id as key to find it
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
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
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
            AttributeInfo ai = new AttributeInfo(attributeId, name, displayName, 
                    type, administrative, visible, readOnly, unique, description, noCopy);
            wsBean.setAttributeProperties(classid, ai, getIPAddress(), sessionId);
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
     * Deletes an attribute from a class using the class name as key to find it
     * @param className Class name
     * @param attributeName Attribute name
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    
    @WebMethod(operationName = "deleteAttribute")
    public void deleteAttribute(@WebParam(name = "className") 
            String className, @WebParam(name = "attributeName")
            String attributeName, @WebParam(name = "sessionId")
            String sessionId) throws Exception{
        try {
            wsBean.deleteAttribute(className, attributeName, getIPAddress(), sessionId);
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
     * Deletes an attribute from a class using the class id as key to find it
     * @param classId Class id
     * @param attributeName Attribute name
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "deleteAttributeForClassWithId")
    public void deleteAttributeForClassWithId(@WebParam(name = "classId") 
            long classId, @WebParam(name = "attributeName")
            String attributeName, @WebParam(name = "sessionId")
            String sessionId) throws Exception{
        try {
            wsBean.deleteAttribute(classId, attributeName, getIPAddress(), sessionId);
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
     * Gets the metadata of a given class using its name as key to find it
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
            return wsBean.getClass(className, getIPAddress(), sessionId);
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
     * Gets the metadata of a given class using its id as key to find it
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
            return wsBean.getClass(classId, getIPAddress(), sessionId);
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
            return wsBean.getSubClassesLight(className, includeAbstractClasses, includeSelf, getIPAddress(), sessionId);
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
            return wsBean.getSubClassesLightNoRecursive(className, includeAbstractClasses, includeSelf, getIPAddress(), sessionId);
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
            return wsBean.getAllClasses(includeListTypes, getIPAddress(), sessionId);
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
            return wsBean.getAllClassesLight(includeListTypes, getIPAddress(), sessionId);
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
     * Deletes a class from the data model using its name as key to find it
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
            wsBean.deleteClass(className, getIPAddress(), sessionId);
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
     * Deletes a class from the data model using its id as key to find it
     * @param classId Class id
     * @param sessionId Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */

    @WebMethod(operationName = "deleteClassWithId")
    public void deleteClassWithId(@WebParam(name = "classId")
    long classId, @WebParam(name = "sessionId")
    String sessionId) throws Exception {

        try {
            wsBean.deleteClass(classId, getIPAddress(), sessionId);
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
            return wsBean.getPossibleChildren(parentClassName, getIPAddress(), sessionId);
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
            return wsBean.getPossibleChildrenNoRecursive(parentClassName, getIPAddress(), sessionId);
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
     * Gets the possible children of a given non-abstract class according to the business rules set for a 
     * particular model
     * @param parentClassName Class to retrieve its possible children
     * @param sessionId Session token
     * @return The list of possible special children
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getSpecialPossibleChildren")
    public List<ClassInfoLight> getSpecialPossibleChildren(@WebParam(name = "parentClassName")
    String parentClassName, @WebParam(name = "sessionId")
    String sessionId) throws Exception {

        try {
            return wsBean.getSpecialPossibleChildren(parentClassName, getIPAddress(), sessionId);
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
            wsBean.addPossibleChildren(parentClassId, newPossibleChildren, getIPAddress(), sessionId);
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
            wsBean.addPossibleChildren(parentClassName, childrenToBeAdded, getIPAddress(), sessionId);
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
            wsBean.removePossibleChildren(parentClassId, childrenToBeRemoved, getIPAddress(), sessionId);
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
     * Get the containment hierarchy of a given class, but upwards (i.e. for Building, it could return 
     * City, Country, Continent)
     * @param className Class to be evaluated
     * @param recursive do it recursively or not
     * @return Session token
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getUpstreamContainmentHierarchy")
    public List<ClassInfoLight> getUpstreamContainmentHierarchy(@WebParam(name = "className")
            String className, @WebParam(name = "recursive")
            boolean recursive, @WebParam(name = "sessionId")
            String sessionId) throws Exception {
        try{
            return wsBean.getUpstreamContainmentHierarchy(className, recursive, getIPAddress(), sessionId);
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

    // <editor-fold defaultstate="collapsed" desc="Sync/ bulk load methods. Click on the + sign on the left to edit the code.">/**
    @WebMethod(operationName = "bulkUpload")
    public String bulkUpload(@WebParam(name = "file")
        byte[] file, @WebParam(name = "commitSize")
        int commitSize, @WebParam(name = "dataType")
        int dataType, @WebParam(name = "sessionId")
        String sessionId) throws Exception {
        try{
            return wsBean.bulkUpload(file, commitSize, dataType, getIPAddress(), sessionId);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    @WebMethod(operationName = "downloadBulkLoadLog")
    public byte[] downloadBulkLoadLog(@WebParam(name = "fileName")
        String fileName, @WebParam(name = "sessionId")
            String sessionId) throws Exception {
        try{
            return wsBean.downloadBulkLoadLog(fileName, getIPAddress(), sessionId);
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

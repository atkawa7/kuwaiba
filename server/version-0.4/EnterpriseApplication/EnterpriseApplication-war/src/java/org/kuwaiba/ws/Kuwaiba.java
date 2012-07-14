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

package org.kuwaiba.ws;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.servlet.http.HttpServletRequest;
import org.kuwaiba.beans.WebserviceBeanRemote;
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
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.metadata.AttributeInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfoLight;

/**
 * Main webservice
 * @author Adrian Maritnez Molina <Adrian.Martinez@kuwaiba.org>
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
     * @param username user login
     * @param password user password
     * @return the SessionID
     * @throws Exception
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
     * @param sessionId The session to be closed
     * @return true if it could close the session, false otherwise.
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
     * @throws Exception
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
     * Retrieves all groups
     * @param sessionId
     * @return A group list
     * @throws Exception
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
     * @param username
     * @param password
     * @param firstName
     * @param lastName
     * @param enabled
     * @param priviliges
     * @param groups
     * @param sessionId
     * @throws Exception
     */
    @WebMethod(operationName = "createUser")
    public Long createUser(
            @WebParam(name = "username")String username,
            @WebParam(name = "password")String password,
            @WebParam(name = "firstName")String firstName,
            @WebParam(name = "LastName")String lastName,
            @WebParam(name = "enabled")Boolean enabled,
            @WebParam(name = "priviliges")Integer[] priviliges,
            @WebParam(name = "groups")Long[] groups,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            wsBean.validateCall("createUser", getIPAddress(), sessionId);
            return wsBean.createUser(username, password, firstName, lastName, enabled, priviliges, groups);

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
     * @param oid
     * @param username
     * @param firstName
     * @param lastName
     * @param password
     * @param enabled
     * @param priviliges
     * @param groups
     * @param sessionId
     * @throws Exception
     */
    @WebMethod(operationName = "setUserProperties")
    public void setUserProperties(
            @WebParam(name = "oid")Long oid,
            @WebParam(name = "username")String username,
            @WebParam(name = "firstName")String firstName,
            @WebParam(name = "LastName")String lastName,
            @WebParam(name = "password")String password,
            @WebParam(name = "enabled")Boolean enabled,
            @WebParam(name = "priviliges")Integer[] priviliges,
            @WebParam(name = "groups")Long[] groups,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            wsBean.validateCall("setUserProperties", getIPAddress(), sessionId);
            wsBean.setUserProperties(oid, username, password, firstName, lastName, enabled, priviliges, groups);

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
     * @param groupName
     * @param description
     * @param priviliges
     * @param users
     * @param sessionId
     * @throws Exception
     */
    @WebMethod(operationName = "createGroup")
    public Long createGroup(
            @WebParam(name = "groupName")String groupName,
            @WebParam(name = "description")String description,
            @WebParam(name = "priviliges")Integer[] priviliges,
            @WebParam(name = "users")Long[] users,
            @WebParam(name = "sessionId")String sessionId) throws Exception {
        try
        {
            wsBean.validateCall("createGroup", getIPAddress(), sessionId);
            return wsBean.createGroup(groupName, description, priviliges, users);

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
     * @param oid
     * @param groupName
     * @param description
     * @param privileges
     * @param users
     * @param sessionId
     * @throws Exception
     */
    @WebMethod(operationName = "setGroupProperties")
    public void setGroupProperties(@WebParam(name = "oid")Long oid,
            @WebParam(name = "groupName")String groupName,
            @WebParam(name = "description")String description,
            @WebParam(name = "privileges")Integer[] privileges,
            @WebParam(name = "users")Long[] users,
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
     * @param oids
     * @param sessionId
     * @throws Exception
     */
    @WebMethod(operationName = "deleteUsers")
    public void deleteUsers(@WebParam(name = "oids")Long[] oids,
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
     * @param oids
     * @param sessionId
     * @throws Exception
     */
    @WebMethod(operationName = "deleteGroups")
    public void deleteGroups(@WebParam(name = "oids")Long[] oids,
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

    /**
     * This method generates/retrieves the default view for a given object
     * @param oid Object id for the object
     * @param objectClass
     * @param viewType
     * @param sessionId
     * @return a view object associated to the given object. If there's no default view, an empty one (all field set to null) is returned
     * @throws Exception
     */
    @WebMethod(operationName = "getView")
    public ViewInfo getView(@WebParam(name="oid")Long oid,
            @WebParam(name="objectClass")String objectClass,
            @WebParam(name="viewType")Integer viewType,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
       try{
            wsBean.validateCall("getView", getIPAddress(), sessionId);
            return wsBean.getView(oid, objectClass,viewType);
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
     * Saves/create a view
     * @param oid Object id
     * @param objectClass object class
     * @param viewType View type. Search on the class View (Persistence Abstraction Layer) documentation for supported types
     * @param structure XML document containing the view structure. Null if you don't want to change the current structure
     * @param background View background . If null, the background is removed. If a 0-sized array, it stays unmodified
     * @param sessionId Session token
     * @throws Exception If something -unexpected or not- goes wrong
     */
    @WebMethod(operationName = "saveView")
    public void saveView(@WebParam(name="oid")Long oid,
            @WebParam(name="objectClass")String objectClass,
            @WebParam(name="viewType")Integer viewType,
            @WebParam(name="structure")byte[] structure,
            @WebParam(name="background")byte[] background,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
           wsBean.validateCall("saveView", getIPAddress(), sessionId);

           if (background != null )
               if (background.length > Constants.MAX_BACKGROUND_SIZE)
                   throw new ServerSideException(Level.WARNING, Util.formatString("The uploaded file exceeds the max file size (%1s)", Constants.MAX_BACKGROUND_SIZE));

           if (structure != null)
               if (structure.length > Constants.MAX_BINARY_FILE_SIZE)
                   throw new ServerSideException(Level.WARNING, Util.formatString("The uploaded file exceeds the max file size (%1s)", Constants.MAX_BACKGROUND_SIZE));

           wsBean.saveView(oid, objectClass,viewType, structure, background);
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
     * @param className
     * @param name
     * @param displayName
     * @param sessionId
     * @return
     * @throws Exception
     */
    @WebMethod(operationName = "createListTypeItem")
    public Long createListTypeItem(
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
     * @param className
     * @param oid
     * @param releaseRelationships
     * @param sessionId
     * @throws Exception If something goes wrong
     */
    @WebMethod(operationName = "deleteListTypeItem")
    public void deleteListTypeItem(
            @WebParam(name = "className") String className,
            @WebParam(name = "oid") Long oid,
            @WebParam(name = "releaseRelationships") Boolean releaseRelationships,
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
     * @throws Exception
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
     * @return A list of light class metadata objects
     * @throws Exception If something goes wrong
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
     * @return An array of records
     * @throws Exception
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
     * @param sessionId session id to check permissions
     * @return a RemoteObjectLight wrapping the newly created query
     * @throws Exception in case something goes wrong
     */
    @WebMethod(operationName = "createQuery")
    public Long createQuery(@WebParam(name="queryName")String queryName,
            @WebParam(name="ownerOid")Long ownerOid,
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
     * @param queryStructure XML document if unchanged
     * @param sessionId session id to check permissions
     * @return success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "saveQuery")
    public void saveQuery(@WebParam(name="queryOid")Long queryOid,
            @WebParam(name = "queryName")String queryName,
            @WebParam(name = "ownerOid")Long ownerOid,
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
     * Deletes the query load in the graphical query builder
     * @param queryOid query oid to be deleted
     * @param sessionId session id to check permissions
     * @return success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "deleteQuery")
    public void deleteQuery(@WebParam(name="queryOid")Long queryOid,
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
     * Retrieves all queries made in the graphical Query builder
     * @param showPublic
     * @param sessionId
     * @return A list with the available queries
     * @throws Exception
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
     * Load a query from all saved queries
     * @param queryOid
     * @param sessionId
     * @return The query
     * @throws Exception If something goes wrong
     */
    @WebMethod(operationName = "getQuery")
    public RemoteQuery getQuery(@WebParam(name="queryOid")Long queryOid,
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
     * @param should this method return all entity classes or only InventoryObject subclasses
     * @param sessionId session identifier
     * @return A byte array containing the class hierarchy as an XML document
     * @throws Exception
     */
    @WebMethod(operationName = "getClassHierarchy")
    public byte[] getClassHierarchy(@WebParam(name = "showAll")Boolean showAll,
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

    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Business Methods. Click on the + sign on the left to edit the code.">
    /**
     * Gets the children of a given object given his class id and object id
     * @param oid object's id
     * @param objectClassId object's class id
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array of all the direct children of the provided object according with the current container hierarchy
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectChildren")
    public RemoteObjectLight[] getObjectChildren(@WebParam(name = "oid") Long oid,
            @WebParam(name = "objectClassId") Long objectClassId,
            @WebParam(name = "maxResults") Integer maxResults,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getObjectChildren", getIPAddress(), sessionId);
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
    @WebMethod(operationName = "getObjectChildrenByClassName")
    public RemoteObjectLight[] getObjectChildrenByClassName(@WebParam(name = "oid") Long oid,
            @WebParam(name = "objectClassName") Long objectClassName,
            @WebParam(name = "maxResults") Integer maxResults,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            wsBean.validateCall("getObjectChildrenByClassName", getIPAddress(), sessionId);
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
     * @param childrenClass
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array with children
     * @throws An general exception in case of error. Consumer of this method must check the message for details
     */
    @WebMethod(operationName="getChildrenOfClass")
    public RemoteObject[] getChildrenOfClass(@WebParam(name="parentOid")Long parentOid,
            @WebParam(name="parentClass")String parentClass,
            @WebParam(name="childrenClass")String childrenClass,
            @WebParam(name="maxResults")Integer maxResults,
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
     * @param childrenClass
     * @param maxResults Max number of children to be returned. O for all
     * @param sessionId Session token
     * @return An array with children
     * @throws An general exception in case of error. Consumer of this method must check the message for details
     */
    @WebMethod(operationName="getChildrenOfClassLight")
    public RemoteObjectLight[] getChildrenOfClassLight(@WebParam(name="parentOid")Long parentOid,
            @WebParam(name="parentClass")String parentClass,
            @WebParam(name="childrenClass")String childrenClass,
            @WebParam(name="maxResults")Integer maxResults,
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
      * @param objectClass
      * @param oid
      * @param sessionId
      * @return a representation of the entity as a RemoteObject
      * @throws Exception
      */
    @WebMethod(operationName = "getObjectInfo")
    public RemoteObject getObjectInfo(@WebParam(name = "objectClass") String objectClass,
            @WebParam(name = "oid") Long oid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{

        if (oid == null)
            throw new ServerSideException(Level.WARNING, "Object id can't be null");
        try{
            wsBean.validateCall("getObjectInfo", getIPAddress(), sessionId);
            return wsBean.getObjectInfo(objectClass, oid);
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
     * Gets the basic information about a given object (oid, classname, etc)
     * @param objectClass className object class. No need to use the package
     * @param oid oid object oid
     * @param sessionId
     * @return a representation of the entity as a RemoteObjectLight
     * @throws Exception
     */
    @WebMethod(operationName = "getObjectInfoLight")
    public RemoteObjectLight getObjectInfoLight(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") Long oid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        assert oid == null : "Object id can't be null";

        try{
            wsBean.validateCall("getObjectInfoLight", getIPAddress(), sessionId);
            return wsBean.getObjectInfoLight(objectClass, oid);
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
     * Note that this is a <strong>string</strong> array on purpose, so the values used not necessarily are not Longs
     * @throws Exception
     */
    @WebMethod(operationName = "getSpecialAttribute")
    public String[] getSpecialAttribute(@WebParam(name = "objectclass") String objectClass,
            @WebParam(name = "oid") Long oid,
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
     * @param sessionId
     * @throws Exception
     */
    @WebMethod(operationName = "updateObject")
    public void updateObject(@WebParam(name = "className")String className,
            @WebParam(name = "oid")Long oid,
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
     * Creates a busines object
     * @param className
     * @param parentObjectClassName
     * @param parentOid
     * @param attributeNames
     * @param attributeValues
     * @param templateId
     * @param sessionId
     * @return
     * @throws Exception
     */
    @WebMethod(operationName = "createObject")
    public Long createObject(@WebParam(name = "className")String className,
            @WebParam(name = "parentObjectClassName")String parentObjectClassName,
            @WebParam(name = "parentOid")Long parentOid,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[][] attributeValues,
            @WebParam(name = "templateId")Long templateId,
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
     * @param className Objects class names
     * @param oid objects oid
     * @param releaseRelationships should the deletion be forced, deleting all the relationships?
     * @param sessionId Session token
     * @throws Exception If something goes wrong
     */
    @WebMethod(operationName = "deleteObjects")
    public void deleteObjects(@WebParam(name = "classNames")String[] classNames,
            @WebParam(name = "oid")Long[] oids,
            @WebParam(name = "releaseRelationships") Boolean releaseRelationships,
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
     * Moves objects from its current parent to a target.
     * @param targetOid The new parent's oid
     * @param objectClasses Class names of the objects to be moved
     * @param objectOids Oids of the objects to be moved
     * @param sessionId
     * @return Success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "moveObjects")
    public void moveObjects(@WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")Long targetOid,
            @WebParam(name = "objectsClasses")String[] objectClasses,
            @WebParam(name = "objectsOids")Long[] objectOids,
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
     * Note: This method does *not* check if the parent change is possible according to the container hierarchy
     * the developer must check it on his side!
     * @param targetClass  The new parent class name
     * @param targetOid The new parent oid
     * @param objectClasses Class names of the objects to be copied
     * @param templateObjects Oids of the objects to be copied
     * @param sessionId Session token
     * @return An array with the ids of the new objects
     * @throws Exception If something goes wrong
     */
    @WebMethod(operationName = "copyObjects")
    public Long[] copyObjects(
            @WebParam(name = "targetClass")String targetClass,
            @WebParam(name = "targetOid")Long targetOid,
            @WebParam(name = "templateClases")String[] objectClasses,
            @WebParam(name = "templateOids")Long[] objectOids,
            @WebParam(name = "recursive")Boolean recursive,
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
     * @param connectionClass Class used to create the connection. See Constants class for supported values
     * @param sessionId Session token
     * @return The new connection id
     * @throws Exception In case something goes wrong
     */
    @WebMethod(operationName = "createPhysicalConnection")
    public Long createPhysicalConnection(
            @WebParam(name = "aObjectClass")String aObjectClass,
            @WebParam(name = "aObjectId")Long aObjectId,
            @WebParam(name = "bObjectClass")String bObjectClass,
            @WebParam(name = "bObjectId")Long bObjectId,
            @WebParam(name = "parentClass")String parentClass,
            @WebParam(name = "parentId")Long parentId,
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

    @WebMethod(operationName = "deletePhysicalConnection")
    public void deletePhysicalConnection(
            @WebParam(name = "objectClass")String objectClass,
            @WebParam(name = "objectId")Long objectId,
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
     * Creates a Class Metadata entry
     * @param name
     * @param displayName
     * @param description
     * @param flags
     * @param abstractClass
     * @param parentClassName
     * @param icon
     * @param smallIcon
     * @return
     * @throws Exception
     */
    @WebMethod(operationName = "createClassMetadata")
    public Long createClassMetadata(@WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "description")
        String description, @WebParam(name = "abstractClass")
        Boolean abstractClass, @WebParam(name = "parentClassName")
        String parentClassName, @WebParam(name = "icon")
        byte[] icon, @WebParam(name = "smallIcon")
        byte[] smallIcon, @WebParam(name = "sessionId")
        String sessionId) throws Exception {

            try{
                wsBean.validateCall("createClassMetadata", getIPAddress(), sessionId);

                if (icon != null)
                    if (icon.length > Constants.MAX_ICON_SIZE)
                        throw new ServerSideException(Level.WARNING, Util.formatString("The uploaded file exceeds the max file size (%1s)", Constants.MAX_BACKGROUND_SIZE));

                if (smallIcon != null)
                    if (smallIcon.length > Constants.MAX_ICON_SIZE)
                        throw new ServerSideException(Level.WARNING, Util.formatString("The uploaded file exceeds the max file size (%1s)", Constants.MAX_BACKGROUND_SIZE));

                ClassInfo ci = new ClassInfo();
                ci.setClassName(name);
                ci.setDisplayName(displayName);
                ci.setDescription(description);
                ci.setIcon(icon);
                ci.setSmallIcon(smallIcon);
                ci.setParentClassName(parentClassName);
                ci.setIsAbstract(abstractClass);

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
     * Changes a Class Metadata definition
     * @param name
     * @param displayName
     * @param description
     * @param flags
     * @param abstractClass
     * @param parentClassName
     * @param icon
     * @param smallIcon
     * @throws Exception
     */
    @WebMethod(operationName = "changeClassMetadataDefinition")
    public void changeClassMetadataDefinition(@WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "description")
        String description, @WebParam(name = "abstractClass")
        Boolean abstractClass, @WebParam(name = "parentClassName")
        String parentClassName, @WebParam(name = "icon")
        byte[] icon, @WebParam(name = "smallIcon")
        byte[] smallIcon, @WebParam(name = "sessionId")
        String sessionId) throws Exception {

        try
        {
            wsBean.validateCall("changeClassMetadataDefinition", getIPAddress(), sessionId);

            if (icon != null)
                if (icon.length > Constants.MAX_ICON_SIZE)
                    throw new ServerSideException(Level.WARNING, Util.formatString("The uploaded file exceeds the max file size (%1s)", Constants.MAX_BACKGROUND_SIZE));

            if (smallIcon != null)
                if (smallIcon.length > Constants.MAX_ICON_SIZE)
                    throw new ServerSideException(Level.WARNING, Util.formatString("The uploaded file exceeds the max file size (%1s)", Constants.MAX_BACKGROUND_SIZE));

            ClassInfo ci = new ClassInfo();
            ci.setClassName(name);
            ci.setDisplayName(displayName);
            ci.setDescription(description);
            ci.setIcon(icon);
            ci.setSmallIcon(smallIcon);
            ci.setParentClassName(parentClassName);
            ci.setIsAbstract(abstractClass);

            wsBean.updateClassDefinition(ci);

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
     * Sets the value of a property associated to an attribute. So far there are only
     * 4 possible properties:
     * -displayName
     * -isVisible
     * -isAdministrative
     * -description
     * @param classId The id of the class associated to the attribute
     * @param attributeName The name of the attribute
     * @param propertyName The name of the property
     * @param propertyValue The value of the property
     * @param sessionId
     * @return Success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "setAttributePropertyValue")
    public void setAttributePropertyValue(@WebParam(name = "classId")Long classId,
            @WebParam(name = "attributeName")String attributeName,
            @WebParam(name = "propertyName")String propertyName,
            @WebParam(name = "propertyValue")String propertyValue,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try {
            wsBean.validateCall("setAttributePropertyValue", getIPAddress(), sessionId);
            wsBean.setAttributePropertyValue(classId, attributeName, propertyName, propertyValue);
            
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
     * Sets the string attributes in a class meta data (by now only the display name and description)
     * @param classId Class to be modified
     * @param attributeName attribute to be modified
     * @param attributeValue value for such attribute
     * @param sessionId
     * @return Success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "setClassPlainAttribute")
    public void setClassPlainAttribute(@WebParam(name = "classId")Long classId,
            @WebParam(name = "attributeName")String attributeName,
            @WebParam(name = "attributeValue")String attributeValue,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try {
            wsBean.validateCall("setClassPlainAttribute", getIPAddress(), sessionId);
            wsBean.setClassPlainAttribute(classId, attributeName, attributeValue);
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
      * Sets the image (icons) attributes in a class meta data (smallIcon and Icon)
      * @param classId Class to be modified
      * @param iconAttribute icon attribute to be modified
      * @param iconImage image as a byte array
      * @param sessionId
      * @return success or failure
      * @throws Exception
      */
    @WebMethod(operationName = "setClassIcon")
    public void setClassIcon(@WebParam(name = "classId")Long classId,
            @WebParam(name = "iconAttribute")String iconAttribute,
            @WebParam(name = "iconImage")byte[] iconImage,
            @WebParam(name = "sessionId")String sessionId) throws Exception{

        try {
            wsBean.validateCall("setClassIcon", getIPAddress(), sessionId);
            wsBean.setClassIcon(classId, iconAttribute, iconImage);
        
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
     * Adds an attribute to a classMeatdatada by its ClassId
     * @param ClassName
     * @param name
     * @param displayName
     * @param type
     * @param description
     * @param administrative
     * @param visible
     * @param mapping
     * @param readOnly
     * @param unique should this attribute be unique?
     * @throws Exception
     */
    @WebMethod(operationName = "addAttributeByClassId")
    public void addAttributeByClassId(@WebParam(name = "className")
        String ClassName, @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "type")
        String type, @WebParam(name = "description")
        String description, @WebParam(name = "administrative")
        Boolean administrative, @WebParam(name = "visible")
        Boolean visible, @WebParam(name = "mapping")
        int mapping, @WebParam(name = "readOnly")
        Boolean readOnly, @WebParam(name = "unique")
        Boolean unique, @WebParam(name = "sessionId")
        String sessionId) throws Exception {

        try {
            wsBean.validateCall("addAttributeByClassId", getIPAddress(), sessionId);
            AttributeInfo ai = new AttributeInfo(name, displayName, type, administrative,
                                            visible, description, mapping);

            wsBean.addAttribute(ClassName, ai);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /*
     * Adds an attribute to a classMeatdatada by its ClassName
     * @param ClassName
     * @param name
     * @param displayName
     * @param type
     * @param description
     * @param administrative
     * @param visible
     * @param mapping
     * @param readOnly
     * @param unique
     * @return
     * @throws Exception
     */
    @WebMethod(operationName = "addAttributeByClassName")
    public void addAttributeByClassName(@WebParam(name = "ClassId")
        Long ClassId, @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "type")
        String type, @WebParam(name = "description")
        String description, @WebParam(name = "administrative")
        Boolean administrative, @WebParam(name = "visible")
        Boolean visible, @WebParam(name = "mapping")
        int mapping, @WebParam(name = "readOnly")
        Boolean readOnly, @WebParam(name = "unique")
        Boolean unique, @WebParam(name = "sessionId")
        String sessionId) throws Exception {

        try {
            wsBean.validateCall("addAttributeByClassName", getIPAddress(), sessionId);
            AttributeInfo ai = new AttributeInfo(name, displayName, type, administrative,
                                                visible, description, mapping);

            wsBean.addAttribute(ClassId, ai);

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
     * Gets the metadata for a given class using its name as argument
     * @param className
     * @return
     * @throws Exception
     */
    @WebMethod(operationName = "getMetadataForClass")
    public ClassInfo getMetadataForClass(@WebParam(name = "className")
    String className, @WebParam(name = "sessionId")
    String sessionId) throws Exception {

        try {
            wsBean.validateCall("getMetadataForClass", getIPAddress(), sessionId);
            return wsBean.getMetadataForClass(className);
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
     * @param classId
     * @return
     * @throws Exception
     */
    @WebMethod(operationName = "getMetadataForClassById")
    public ClassInfo getMetadataForClassById(@WebParam(name = "classId")
    Long classId, @WebParam(name = "sessionId")
    String sessionId) throws Exception {

        try {
            wsBean.validateCall("getMetadataForClassById", getIPAddress(), sessionId);
            return wsBean.getMetadataForClass(classId);

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
     * Provides metadata for all classes, but the light version
     * @param sessionId
     * @param includeListTypes boolean to indicate if the list should include the subclasses of
     * GenericObjectList
     * @return An array with the basic class metadata
     * @throws Exception
     */
    @WebMethod(operationName = "getLightMetadata")
    public List<ClassInfoLight> getLightMetadata(
            @WebParam(name = "includeListTypes")Boolean includeListTypes,
            @WebParam(name = "sessionId") String sessionId) throws Exception{

        try
        {
            wsBean.validateCall("getLightMetadata", getIPAddress(), sessionId);
            if (includeListTypes == null)
                includeListTypes = false;

            return wsBean.getLightMetadata(includeListTypes);

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
     * Retrieves all class metadata
     * @param sessionId
     * @param includeListTypes boolean to indicate if the list should include the subclasses of
     * GenericObjectList
     * @return An array with the complete metadata for each class
     * @throws Exception
     */
    @WebMethod(operationName = "getMetadata")
    public List<ClassInfo> getMetadata(
            @WebParam(name = "includeListTypes")Boolean includeListTypes,
            @WebParam(name = "sessionId") String sessionId) throws Exception{

        try
        {
            wsBean.validateCall("getMetadata", getIPAddress(), sessionId);

            if (includeListTypes == null)
                includeListTypes = false;

            return wsBean.getMetadata(includeListTypes);
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
     * Deletes a class metadata entry for a given class using its name as argument
     * @param className
     * @throws Exception
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
     * @param className
     * @return
     * @throws Exception
     */

    @WebMethod(operationName = "deleteClassById")
    public void deleteClassById(@WebParam(name = "classId")
    Long classId, @WebParam(name = "sessionId")
    String sessionId) throws Exception {

        try {
            wsBean.validateCall("deleteClassById", getIPAddress(), sessionId);
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
    
    @WebMethod(operationName = "addPossibleChildren")
    public void addPossibleChildren(@WebParam(name = "parentClassId")
            Long parentClassId, @WebParam(name = "childrenToBeAdded")
            Long[] newPossibleChildren, @WebParam(name = "sessionId")
            String sessionId) throws Exception {

        try {
            wsBean.validateCall("addPossibleChildren", getIPAddress(), sessionId);
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

    @WebMethod(operationName = "addPossibleChildrenByClassName")
    public void addPossibleChildrenByClassName(@WebParam(name = "parentClassName")
            String parentClassName, @WebParam(name = "childrenToBeAdded")
            String[] childrenToBeAdded, @WebParam(name = "sessionId")
            String sessionId) throws Exception {
        try {
            wsBean.validateCall("addPossibleChildrenByClassName", getIPAddress(), sessionId);
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
     * @param parentClassId
     * @param childrenToBeRemoved
     * @param sessionId
     * @throws Exception
     */
    @WebMethod(operationName = "removePossibleChildren")
    public void removePossibleChildren(@WebParam(name = "parentClassId")
    Long parentClassId, @WebParam(name = "childrenToBeRemoved")
    Long[] childrenToBeRemoved, @WebParam(name = "sessionId")
    String sessionId) throws Exception {

        try{
            wsBean.validateCall("removePossibleChildren", getIPAddress(), sessionId);
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
    // </editor-fold>

    /**
     * Helpers
     */

    /**
     * Gets the IP address from the client issuing the request
     * @return the IP address as string
     */
    private String getIPAddress(){
        return ((HttpServletRequest)context.getMessageContext().
                    get("javax.xml.ws.servlet.request")).getRemoteAddr().toString(); //NOI18N
    }

}
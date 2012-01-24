/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

import org.kuwaiba.ws.toserialize.ClassInfo;
import org.kuwaiba.ws.toserialize.ObjectList;
import org.kuwaiba.ws.todeserialize.ObjectUpdate;
import org.kuwaiba.ws.toserialize.RemoteObject;
import org.kuwaiba.ws.toserialize.RemoteObjectLight;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import org.kuwaiba.businesslogic.BackendBeanRemote;
import org.kuwaiba.core.exceptions.ArraySizeMismatchException;
import org.kuwaiba.core.exceptions.InventoryException;
import org.kuwaiba.core.exceptions.MiscException;
import org.kuwaiba.core.exceptions.NotViewableObjectException;
import org.kuwaiba.ws.todeserialize.TransientQuery;
import org.kuwaiba.ws.toserialize.ClassInfoLight;
import org.kuwaiba.ws.toserialize.RemoteQuery;
import org.kuwaiba.ws.toserialize.RemoteQueryLight;
import org.kuwaiba.ws.toserialize.RemoteSession;
import org.kuwaiba.ws.toserialize.ResultRecord;
import org.kuwaiba.ws.toserialize.ServerStatus;
import org.kuwaiba.ws.toserialize.UserGroupInfo;
import org.kuwaiba.ws.toserialize.UserInfo;
import org.kuwaiba.ws.toserialize.ViewInfo;
import org.kuwaiba.entity.connections.physical.GenericPhysicalConnection;
import org.kuwaiba.entity.core.InventoryObject;
import org.kuwaiba.entity.core.ViewableObject;
import org.kuwaiba.entity.session.UserSession;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import org.kuwaiba.core.exceptions.InvalidArgumentException;
import org.kuwaiba.entity.multiple.GenericObjectList;
import org.kuwaiba.entity.session.User;
import org.kuwaiba.entity.session.UserGroup;
import org.kuwaiba.util.Constants;
import org.kuwaiba.util.HierarchyUtils;
import org.kuwaiba.util.MetadataUtils;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@WebService
public class Kuwaiba {
    /**
     * The backend bean with all business logic
     */
    @EJB
    private BackendBeanRemote sbr;
    /**
     * The context to get information about each request
     */
    @Resource
    private WebServiceContext context;

    /**
     * This method is useful to test if the server is actually running. By now, it always says "true"
     * @return a boolean showing if the server up or down
     */
    @WebMethod(operationName = "getServerStatus")
    public ServerStatus getServerStatus(){
        return new ServerStatus(Constants.SERVER_VERSION);
    }

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

            return new RemoteSession(sbr.createSession(username,password, remoteAddress));
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }


    /**
     * Closes a session
     * @param sessionId
     * @return true if it could close the session, false otherwise. In this case, an error message is written in the error stack
     */
    @WebMethod(operationName = "closeSession")
    public boolean closeSession(@WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            String remoteAddress = getIPAddress();
            return sbr.closeSession(sessionId, remoteAddress);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Gets the children of a given object
     * @param oid
     * @param objectClassId
     * @param sessionId
     * @return An array of all the direct children of the provided object according with the current container hierarchy
     * @throws Exception
     */
    @WebMethod(operationName = "getObjectChildren")
    public RemoteObjectLight[] getObjectChildren(@WebParam(name = "oid") Long oid,
            @WebParam(name = "objectClassId") Long objectClassId,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("getObjectChildren", getIPAddress(), sessionId);
            RemoteObjectLight[] res = sbr.getObjectChildren(oid,objectClassId);
            return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Get all children in an object of an specific class
     * @param parentOid
     * @param childrenClass
     * @return An array with children
     * @throws An exception (ClassNotFoundException or any other) in case of error
     */
    @WebMethod(operationName="getChildrenOfClass")
    public RemoteObject[] getChildrenOfClass(@WebParam(name="parentOid")Long parentOid,
            @WebParam(name="childrenClass")String childrenClass,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("getChildrenOfClass", getIPAddress(), sessionId);
            Class myClass = sbr.getClassFor(childrenClass);
            if (myClass == null)
                throw new ClassNotFoundException(childrenClass);
                RemoteObject[] res = sbr.getChildrenOfClass(parentOid,myClass);
                return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
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
        try{
            sbr.validateCall("getObjectInfo", getIPAddress(), sessionId);
            Class myClass = sbr.getClassFor(objectClass);
            return sbr.getObjectInfo(myClass, oid);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
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
        try{
            sbr.validateCall("getObjectInfoLight", getIPAddress(), sessionId);
            if (oid == null)
                throw new InvalidArgumentException("Object id can't be null",Level.WARNING);

            Class myClass = sbr.getClassFor(objectClass);
            return sbr.getObjectInfoLight(myClass, oid);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Updates attributes of a given object
     * @param update ObjectUpdate object representing only the changes to be committed
     * @param sessionId
     * @return the updated object
     * @throws Exception
     */
    @WebMethod(operationName = "updateObject")
    public RemoteObject updateObject(@WebParam(name = "objectupdate")ObjectUpdate update,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("updatObject", getIPAddress(), sessionId);
            return sbr.updateObject(update);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Sets a lock over an object
     * @param oid
     * @param objectclass
     * @param value
     * @param sessionId
     * @return Success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "setObjectLock")
    public Boolean setObjectLock(@WebParam(name = "oid")Long oid,
            @WebParam(name = "objectclass")String objectclass,
            @WebParam(name = "value")Boolean value,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("setObjectLock", getIPAddress(), sessionId);
            Boolean res = sbr.setObjectLock(oid, objectclass, value);
            return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Updates the container hierarchy for a given class
     * @param parentClassId Class id where the possible children class will be attached
     * @param possibleChildren Array with all new possible children classes
     * @param sessionId
     * @return true if succeed, false otherwise
     * @throws Exception
     */
    @WebMethod(operationName = "addPossibleChildren")
    public Boolean addPossibleChildren(@WebParam(name = "parentClassId")Long parentClassId,
            @WebParam(name = "possibleChildren")Long[] possibleChildren,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("addPosibleChildren", getIPAddress(), sessionId);
            Boolean res = sbr.addPossibleChildren(parentClassId, possibleChildren);
            return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Remove a possible children in the container hierarchy
     * @param parentClassId
     * @param childrenToBeRemoved
     * @param sessionId
     * @return Success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "removePossibleChildren")
    public Boolean removePossibleChildren(@WebParam(name = "parentClassId")Long parentClassId,
            @WebParam(name = "childrenToBeRemoved")Long[] childrenToBeRemoved,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("removeChildren", getIPAddress(), sessionId);
            Boolean res = sbr.removePossibleChildren(parentClassId, childrenToBeRemoved);
            return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Return all possible classes that can be contained by the given class instances
     * @param _parentClass
     * @param sessionId
     * @return An array with all possible classes whose instances can be contained in the provided class instance
     * @throws Exception
     */
    @WebMethod(operationName = "getPossibleChildren")
    public List<ClassInfoLight> getPossibleChildren(
            @WebParam(name = "parentClass")String _parentClass,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("getPossibleChildren", getIPAddress(), sessionId);
            Class parentClass;
            parentClass = sbr.getClassFor(_parentClass);
            return sbr.getPossibleChildren(parentClass);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Return all possible classes that can be contained by the given class instances
     * @param _parentClass
     * @param sessionId
     * @return An array with the possible children classes
     * @throws Exception
     */
    @WebMethod(operationName = "getPossibleChildrenNoRecursive")
    public List<ClassInfoLight> getPossibleChildrenNoRecursive(
            @WebParam(name = "parentClass")String _parentClass,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("getPossibleChildrenNoRecursive", getIPAddress(), sessionId);
            Class parentClass = sbr.getClassFor(_parentClass);
            return sbr.getPossibleChildrenNoRecursive(parentClass);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Gets the possible children of a the root node
     * @param sessionId
     * @return An array with all possible children according to the current container hierarchy for the root object
     * @throws Exception
     */
    @WebMethod(operationName = "getRootPossibleChildren")
    public List<ClassInfoLight> getRootPossibleChildren(@WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("getRootPossibleChildren", getIPAddress(), sessionId);
            return sbr.getRootPossibleChildren();
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Creates a an object
     * @param objectClass
     * @param template This is not working by now
     * @param parentOid
     * @param sessionId
     * @return An object representing the newly created object
     * @throws Exception
     */
    @WebMethod(operationName = "createObject")
    public RemoteObjectLight createObject
            (@WebParam(name = "objectClass")String objectClass,
             @WebParam(name = "template")String template,
             @WebParam(name = "parentOid")Long parentOid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("createObject", getIPAddress(), sessionId);
            Class myClass = sbr.getClassFor(objectClass);
            return sbr.createObject(myClass,parentOid,template);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * METADATA
     */
         /**
     * Provides metadata for all classes, but the light version
     * @param sessionId
     * @param includeListTypes boolean to indicate if the list should include the subclasses of
     * GenericObjectList
     * @return An array with the basic class metadata
     * @throws Exception
     */
    @WebMethod(operationName = "getLightMetadata")
    public List<ClassInfoLight> getLightMetadata(@WebParam(name = "sessionId")String sessionId,
            @WebParam(name = "includeListTypes")Boolean includeListTypes) throws Exception{
        try{
            sbr.validateCall("getLightMetadata", getIPAddress(), sessionId);
            if (includeListTypes == null) 
                includeListTypes = false;
            return sbr.getLightMetadata(includeListTypes);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    
    /**
     * Retrieves all the class metadata
     * @param sessionId
     * @param includeListTypes boolean to indicate if the list should include the subclasses of
     * GenericObjectList
     * @return An array with the complete metadata for each class
     * @throws Exception
     */
    @WebMethod(operationName = "getMetadata")
    public List<ClassInfo> getMetadata(@WebParam(name = "sessionId")String sessionId,
            @WebParam(name = "includeListTypes")Boolean includeListTypes) throws Exception{
        try{
            sbr.validateCall("getMetadata", getIPAddress(), sessionId);
            if (includeListTypes == null)
                includeListTypes = false;
            return sbr.getMetadata(includeListTypes);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }


    /**
     * Gets the metadata of a single class
     * @param className
     * @param sessionId
     * @return The metadata for the given class
     * @throws Exception
     */
    @WebMethod(operationName = "getMetadataForClass")
    public ClassInfo getMetadataForClass(@WebParam(name = "className")String className,
                        @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("getMetadataForClass", getIPAddress(), sessionId);
            Class myClass = sbr.getClassFor(className);
            return sbr.getMetadataForClass(myClass);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
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
            sbr.validateCall("getClassHierarchy", getIPAddress(), sessionId); //NOI18N
            return sbr.getClassHierarchy(showAll);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Retrieves a list of objects corresponding to a GenericListType group of instances
     * @param className
     * @param sessionId
     * @return An object representing the basic information about the list type and the choices
     * @throws Exception
     */
    @WebMethod(operationName = "getMultipleChoice")
    public ObjectList getMultipleChoice(@WebParam(name = "className")String className,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("getMultipleChoice", getIPAddress(), sessionId);
            Class myClass = sbr.getClassFor(className);
            ObjectList res = sbr.getMultipleChoice(myClass);
            return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Deletes an object
     * @param className Object's class name
     * @param oid
     * @param sessionId
     * @return success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "removeObject")
    public Boolean removeObject(@WebParam(name = "className")String className,
            @WebParam(name = "oid")Long oid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("removeObject", getIPAddress(), sessionId);
            Class myClass = sbr.getClassFor(className);
            Boolean res = sbr.removeObject(myClass, oid);
            return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Copy objects from its current parent to a target. This is <b>not</b> a deep copy. Only the selected object will be copied, not the children
     * Note: This method does *not* check if the parent change is possible according to the container hierarchy
     * the developer must check it on his side!
     * @param targetOid The new parent oid
     * @param objectClasses Class names of the objects to be moved
     * @param templateObjects Oids of the objects to be used as templates
     * @param sessionId
     * @return An array with the new objects
     * @throws Exception
     */
    @WebMethod(operationName = "copyObjects")
    public RemoteObjectLight[] copyObjects(@WebParam(name = "targetOid")Long targetOid,
            @WebParam(name = "objectClases")String[] _objectClasses,
            @WebParam(name = "templateObjects")Long[] templateObjects,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("copyObjects", getIPAddress(), sessionId);
            Class[] objectClasses = new Class[_objectClasses.length];

            for (int i = 0; i < _objectClasses.length;i++){
                objectClasses[i] = sbr.getClassFor(_objectClasses[i]);
                if (objectClasses[i] == null)
                    throw new ClassNotFoundException(_objectClasses[i]);
            }

            RemoteObjectLight[] res = sbr.copyObjects(targetOid, templateObjects, objectClasses);
            return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Moves objects from its current parent to a target.
     * Note: This method does *not* check if the parent change is possible according to the container hierarchy
     * the developer must check it on his side!
     * @param targetOid The new parent's oid
     * @param objectClasses Class names of the objects to be moved
     * @param objectOids Oids of the objects to be moved
     * @param sessionId
     * @return Success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "moveObjects")
    public Boolean moveObjects(@WebParam(name = "targetOid")Long targetOid,
            @WebParam(name = "objectsClasses")String[] _objectClasses,
            @WebParam(name = "objectsOids")Long[] objectOids,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("moveObjects", getIPAddress(), sessionId);
            Class[] objectClasses = new Class[_objectClasses.length];

            for (int i = 0; i < _objectClasses.length;i++){
                objectClasses[i] = sbr.getClassFor(_objectClasses[i]);
                if (objectClasses[i] == null)
                    throw new ClassNotFoundException(_objectClasses[i]);
            }
            return sbr.moveObjects(targetOid, objectOids,objectClasses);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /********************************
     * Queries
     ********************************/

    /**
     * Searches for objects given some criteria
     * @param className
     * @param paramNames
     * @param paramTypes
     * @param paramValues
     * @param sessionId
     * @return An array with the search results
     * @throws Exception
     */
    @WebMethod(operationName = "searchForObjects")
    public RemoteObjectLight[] searchForObjects(@WebParam(name="className")String className,
            @WebParam(name="paramNames")String[] paramNames,
            @WebParam(name="paramTypes")String[] paramTypes,
            @WebParam(name="paramValues")String[] paramValues,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("searchForObjects", getIPAddress(), sessionId);
            if (paramNames.length != paramValues.length || paramTypes.length != paramValues.length)
                throw new Exception(java.util.ResourceBundle.
                        getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_ARRAYSIZESDONTMATCH")+"paramNames,paramValues, paramTypes");

            Class toBeSearched = sbr.getClassFor(className);

            if (!(HierarchyUtils.isSubclass(toBeSearched, InventoryObject.class) || HierarchyUtils.isSubclass(toBeSearched, GenericObjectList.class)))
                throw new MiscException("Only subclasses of InventoryObject or GenericObjectList can be searched using this method:"+toBeSearched.getSimpleName());

            RemoteObjectLight[] res = sbr.searchForObjects(toBeSearched,paramNames, paramTypes,paramValues);
            return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Execute a complex query generated using the Graphical Query Builder.  Please note
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
            sbr.validateCall("executeQuery", getIPAddress(), sessionId);
            Class queryClass = sbr.getClassFor(query.getClassName());

            if (!(HierarchyUtils.isSubclass(queryClass, InventoryObject.class) || HierarchyUtils.isSubclass(queryClass, GenericObjectList.class)))
                throw new MiscException("Only subclasses of InventoryObject or GenericObjectList can be searched using this method:"+queryClass.getSimpleName());

            if (query.getAttributeNames() != null && query.getAttributeValues() != null &&
                    query.getConditions() != null && query.getJoins() != null){
                if (query.getAttributeNames().size() != query.getAttributeValues().size() ||
                    query.getAttributeNames().size() != query.getConditions().size() ||
                    query.getConditions().size() != query.getJoins().size())
                throw new ArraySizeMismatchException("attributeNames","attributeValues","conditions","joins");
            }else{
                if (!(query.getAttributeNames() == null && query.getAttributeValues() == null &&
                        query.getConditions() == null) && query.getJoins() == null)
                    throw new ArraySizeMismatchException("attributeNames","attributeValues","conditions","joins");
            }
            return sbr.executeQuery(query);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Creates a query designed using the graphical query editor
     * @param queryName Query name
     * @param ownerOid OwnerOid. Null if public
     * @param queryStructure XML document as a byte array
     * @param sessionId session id to check permissions
     * @return a RemoteObjectLight wrapping the newly created query
     * @throws Exception in case something goes wrong
     */
    @WebMethod(operationName = "createQuery")
    public RemoteQueryLight createQuery(@WebParam(name="queryName")String queryName,
            @WebParam(name="ownerOid")Long ownerOid,
            @WebParam(name="queryStructure")byte[] queryStructure,
            @WebParam(name="description")String description,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("createQuery", getIPAddress(), sessionId);
            return new RemoteQueryLight(sbr.createQuery(queryName, ownerOid, queryStructure, description));
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     *
     * @param queryOid query oid to be updated
     * @param queryName query name (the same if unchanged)
     * @param ownerOid owneroid (if unchanged)
     * @param queryStructure XML document if unchanged
     * @param sessionId session id to check permissions
     * @return success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "saveQuery")
    public boolean saveQuery(@WebParam(name="queryOid")Long queryOid,
            @WebParam(name = "queryName")String queryName,
            @WebParam(name = "ownerOid")Long ownerOid,
            @WebParam(name = "queryStructure")byte[] queryStructure,
            @WebParam(name = "description")String description,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("saveQuery", getIPAddress(), sessionId);
            return sbr.saveQuery(queryOid,queryName, ownerOid, queryStructure, description);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Deletes a query
     * @param queryOid query oid to be deleted
     * @param sessionId session id to check permissions
     * @return success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "deleteQuery")
    public boolean deleteQuery(@WebParam(name="queryOid")Long queryOid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("deleteQuery", getIPAddress(), sessionId);
            return sbr.deleteQuery(queryOid);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    @WebMethod(operationName = "getQueries")
    public RemoteQueryLight[] getQueries(@WebParam(name="showPublic")boolean showPublic,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("getQueries", getIPAddress(), sessionId);
            UserSession session = sbr.getSession(sessionId);
            return sbr.getQueries(session.getUser().getId(),showPublic);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    @WebMethod(operationName = "getQuery")
    public RemoteQuery getQuery(@WebParam(name="queryOid")Long queryOid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("getQuery", getIPAddress(), sessionId);
            return sbr.getQuery(queryOid);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
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
    public Boolean setAttributePropertyValue(@WebParam(name = "classId")Long classId,
            @WebParam(name = "attributeName")String attributeName,
            @WebParam(name = "propertyName")String propertyName,
            @WebParam(name = "propertyValue")String propertyValue,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("setAttributePropertyValue", getIPAddress(), sessionId);
            return sbr.setAttributePropertyValue(classId, attributeName, propertyName, propertyValue);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
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
    public Boolean setClassPlainAttribute(@WebParam(name = "classId")Long classId,
            @WebParam(name = "attributeName")String attributeName,
            @WebParam(name = "attributeValue")String attributeValue,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("setClassPlainAttribute", getIPAddress(), sessionId);
            return sbr.setClassPlainAttribute(classId,attributeName,attributeValue);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
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
    public Boolean setClassIcon(@WebParam(name = "classId")Long classId,
            @WebParam(name = "iconAttribute")String iconAttribute,
            @WebParam(name = "iconImage")byte[] iconImage,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("setClassIcon", getIPAddress(), sessionId);
            return sbr.setClassIcon(classId, iconAttribute, iconImage);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    @WebMethod(operationName = "createListType")
    public RemoteObjectLight createListType(@WebParam(name = "className")String className,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("createListType", getIPAddress(), sessionId);
            Class listType = sbr.getClassFor(className);
            if (listType == null)
                throw new ClassNotFoundException(className);
            return sbr.createListType(listType);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }
    /**
     * Returns the list type attributes
     * @param sessionId
     * @return An array containing all list types
     * @throws Exception
     */
    @WebMethod(operationName = "getInstanceableListTypes")
    public List<ClassInfoLight> getInstanceableListTypes(@WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("getInstanceableListTypes", getIPAddress(), sessionId);
            return  sbr.getInstanceableListTypes();
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Views
     */

    /**
     * This method generates/retrieves the default view for a given object
     * @param oid Object id for the object
     * @param objectClass
     * @param sessionId
     * @return a view object associated to the given object. If there's no default view, an empty one (all field set to null) is returned
     * @throws Exception
     */
    @WebMethod(operationName = "getDefaultView")
    public ViewInfo getDefaultView(@WebParam(name="oid")Long oid,
            @WebParam(name="objectClass")String objectClass,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
       try{
           sbr.validateCall("getDefaultView", getIPAddress(), sessionId);
            ViewInfo res;
            Class myClass = sbr.getClassFor(objectClass);
            if (!HierarchyUtils.isSubclass(myClass, ViewableObject.class))
                throw new NotViewableObjectException(myClass);
            else
                res = sbr.getDefaultView(oid, myClass);
            return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Gets and special built-in room view
     * @param oid oid The oid for the related Room instance
     * @param sessionId
     * @return A viewInfo object enclosing the room view
     * @throws Exception
     */
    @WebMethod(operationName = "getRoomView")
    public ViewInfo getRoomView(@WebParam(name="oid")Long oid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("getRoomView", getIPAddress(), sessionId);
            ViewInfo res = sbr.getRoomView(oid);
            return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Get the view of a simple rack. This is nothing but the rack and its children
     * placed within depending on the "rackUnits" attributes
     * @param oid The oid for the related Room instance
     * @param sessionId
     * @return A viewInfo object enclosing the rack view
     * @throws Exception
     */
    @WebMethod(operationName = "getRackView")
    public ViewInfo getRackView(@WebParam(name="oid")Long oid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("getRackView", getIPAddress(), sessionId);
            ViewInfo res = sbr.getRackView(oid);
            return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Save and object view
     * Sets a view for a given object
     * @param oid object's oid
     * @param objectClass object's class
     * @param view object's serialized view
     * @return Success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "saveObjectView")
    public Boolean saveObjectView(@WebParam(name="oid")Long oid,
            @WebParam(name="objectClass")String objectClass,
            @WebParam(name="view") ViewInfo view,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("saveObjectView", getIPAddress(), sessionId);
            Class myClass = sbr.getClassFor(objectClass);
            if (!HierarchyUtils.isSubclass(myClass, ViewableObject.class))
                throw new Exception(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_NOVIEWS") + objectClass);

            else return sbr.saveObjectView(oid, myClass,view);

        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Creates a physical container connection (ditch, conduit, pipe, etc)
     * @param sourceObjectOid
     * @param targetObjectOid
     * @param containerClass
     * @param parentObjectOid
     * @param sessionId
     * @return An object representing the newly created object
     * @throws Exception
     */
    @WebMethod(operationName = "createPhysicalContainerConnection")
    public RemoteObject createPhysicalContainerConnection(@WebParam(name="sourceObjectOid")Long sourceObjectOid,
            @WebParam(name="targetObjectOid")Long targetObjectOid,@WebParam(name="containerClass")String containerClass,
            @WebParam(name="parentObjectOid")Long parentObjectOid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("createPhysicalContainerConnection", getIPAddress(), sessionId);
            Class myClass = sbr.getClassFor(containerClass);
            RemoteObject res = sbr.createPhysicalContainerConnection(sourceObjectOid,targetObjectOid,myClass,parentObjectOid);
            return res;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Creates a physical connection connection (cable, fiber optics, etc)
     * @param endpointAOid
     * @param endpointBOid
     * @param connectionClass
     * @param parentObjectOid
     * @param sessionId
     * @return An object representing the newly created object
     * @throws Exception
     */
    @WebMethod(operationName = "createPhysicalConnection")
    public RemoteObject createPhysicalConnection(@WebParam(name="endpointAOid")Long endpointAOid,
            @WebParam(name="endpointBOid")Long endpointBOid,@WebParam(name="connectionClass")String connectionClass,
            @WebParam(name="parentObjectOid")Long parentObjectOid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("createPhysicalConnection", getIPAddress(), sessionId);
            Class myClass = sbr.getClassFor(connectionClass);
            if(!HierarchyUtils.isSubclass(myClass, GenericPhysicalConnection.class))
                throw new Exception(java.util.ResourceBundle.
                        getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_WRONGCLASS")+ connectionClass);
            return sbr.createPhysicalConnection(endpointAOid,endpointBOid,myClass,parentObjectOid);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * User Management
     */

    /**
     * Retrieves all users
     * @param sessionId
     * @return An user list
     * @throws Exception
     */
    @WebMethod(operationName = "getUsers")
    public UserInfo[] getUsers(@WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("getUsers", getIPAddress(), sessionId);
            return sbr.getUsers();
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
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
    public UserGroupInfo[] getGroups(@WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("getGroups", getIPAddress(), sessionId);
            return sbr.getGroups();
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }

    }

    /**
     * Creates a new user
     * @param sessionId
     * @return The newly created user
     * @throws Exception
     */
    @WebMethod(operationName = "createUser")
    public UserInfo createUser(@WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("createUser", getIPAddress(), sessionId);
            return sbr.createUser();
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Deletes an user
     * @param toBeDeleted
     * @return Success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "deleteUsers")
    public Boolean deleteUsers(@WebParam(name="tobeDeleted")Long[] toBeDeleted,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("deleteUsers", getIPAddress(), sessionId);
            return sbr.deleteUsers(toBeDeleted);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Creates a new group
     * @param sessionId
     * @return An object representing the newly created object
     * @throws Exception
     */
    @WebMethod(operationName = "createGroup")
    public UserGroupInfo createGroup(@WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("createGroup", getIPAddress(), sessionId);
            return sbr.createGroup();
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Deletes a list of groups
     * @param toBeDeleted
     * @return Success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "deleteGroups")
    public Boolean deleteGroups(@WebParam(name="toBeDeleted")Long[] toBeDeleted,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("deleteGroups", getIPAddress(), sessionId);
            return sbr.deleteGroups(toBeDeleted);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Set properties for a given user (username, name, etc)
     * @param oid User oid
     * @param propertiesNames Array with the names of the properties to be set
     * @param propertiesValues Array with the values of the properties to be set
     * @param sessionId
     * @return Success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "setUserProperties")
    public Boolean setUserProperties(@WebParam(name="user")ObjectUpdate user,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("setUserProperties", getIPAddress(), sessionId);
            if (user.getUpdatedAttributes() == null || user.getNewValues() == null || user.getOid() == null)
                throw new InvalidArgumentException("Malformed update object (null parameters)", Level.SEVERE);

            if (user.getUpdatedAttributes().length != user.getNewValues().length)
                throw new ArraySizeMismatchException("updatedAttributes", "newValues");

            for (int i = 0; i < user.getUpdatedAttributes().length; i++){
                if (user.getUpdatedAttributes()[i].equals("password"))
                    user.getNewValues()[i] = MetadataUtils.getMD5Hash(user.getNewValues()[i]);
            }
            sbr.updateObject(user, User.class);
            return true;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Set properties for a given group (name, description)
     * @param oid Group oid
     * @param propertiesNames Array with the names of the properties to be set
     * @param propertiesValues Array with the values of the properties to be set
     * @param sessionId
     * @return Success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "setGroupProperties")
    public Boolean setGroupProperties(@WebParam(name="group")ObjectUpdate group,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("setGroupProperties", getIPAddress(), sessionId);
            if (group.getUpdatedAttributes() == null || group.getNewValues() == null || group.getOid() == null)
                throw new InvalidArgumentException("Malformed update object (null parameters)", Level.SEVERE);

            if (group.getUpdatedAttributes().length != group.getNewValues().length)
                throw new ArraySizeMismatchException("updatedAttributes", "newValues");

            for (int i = 0; i < group.getUpdatedAttributes().length; i++){
                if (group.getUpdatedAttributes()[i].equals("password"))
                    group.getNewValues()[i] = MetadataUtils.getMD5Hash(group.getNewValues()[i]);
            }
            sbr.updateObject(group, UserGroup.class);
            return true;
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }

    }

    /**
     * Adds users to a group
     * @param usersOids An array with The users oids
     * @param groupOid The group oid
     * @param sessionId
     * @return Success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "addUsersToGroup")
    public Boolean addUsersToGroup(@WebParam(name="usersOids")Long[] usersOids,
            @WebParam(name="groupOid")Long groupOid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("addUsersToGroup", getIPAddress(), sessionId);
            return sbr.addUsersToGroup(usersOids, groupOid);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Removes users from a group
     * @param usersOids An array with The users oids
     * @param groupOid The group's oid
     * @return Success or failure
     */
    @WebMethod(operationName = "removeUsersFromGroup")
    public Boolean removeUserfromGroup(@WebParam(name="usersOids")Long[] usersOids,
            @WebParam(name="groupOid")Long groupOid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("removeUsersFromGroup", getIPAddress(), sessionId);
            return sbr.removeUsersFromGroup(usersOids, groupOid);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Assigns groups to a user
     * @param groupsOids An array with The groups oids
     * @param userOid The user oid
     * @param sessionId
     * @return Success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "addGroupsToUser")
    public Boolean addGroupsToUser(@WebParam(name="groupsOids")Long[] groupsOids,
            @WebParam(name="userOid")Long userOid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("addGroupsToUser", getIPAddress(), sessionId);
            return sbr.addGroupsToUser(groupsOids, userOid);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Removes groups from a user
     * @param groupsOids An array with The groups oids
     * @param userOid The user oid
     * @param sessionId
     * @return Success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "removeGroupsFromUser")
    public Boolean removeGroupsFromUser(@WebParam(name="groupsOids")Long[] groupsOids,
            @WebParam(name="userOid")Long userOid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            sbr.validateCall("removeGroupsFromUser", getIPAddress(), sessionId);
            return sbr.removeGroupsFromUser(groupsOids, userOid);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof InventoryException)
                level = ((InventoryException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }

    /**
     * Helpers
     */
    /**
     * Gets the IP address from the client making the request
     * @return the IP address as string
     */
    private String getIPAddress(){
        return ((HttpServletRequest)context.getMessageContext().
                    get("javax.xml.ws.servlet.request")).getRemoteAddr().toString(); //NOI18N
    }
}

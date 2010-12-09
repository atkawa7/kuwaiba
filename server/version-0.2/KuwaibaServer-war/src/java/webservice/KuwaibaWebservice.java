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
package webservice;

import core.toserialize.ClassInfo;
import core.toserialize.ObjectList;
import core.todeserialize.ObjectUpdate;
import core.toserialize.RemoteObject;
import core.toserialize.RemoteObjectLight;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import businesslogic.BackendBeanRemote;
import core.toserialize.ClassInfoLight;
import core.toserialize.RemoteSession;
import core.toserialize.UserGroupInfo;
import core.toserialize.UserInfo;
import core.toserialize.ViewInfo;
import entity.connections.physical.GenericPhysicalConnection;
import entity.core.ViewableObject;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import util.HierarchyUtils;

/**
 * Represents the main webservice
 * @author Charles Edward Bedón Cortázar <charles.bedon@zoho.com>
 */
@WebService
public class KuwaibaWebservice {
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
    public boolean getServerStatus(){
        return true;
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
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
            throw e;
        }
    }


    /**
     * Close a session
     * @param sessionId
     * @return true if it could close the session, false otherwise. In this case, an error message is written in the error stack
     */
    @WebMethod(operationName = "closeSession")
    public boolean closeSession(@WebParam(name = "sessionId")String sessionId){
        return true;
    }

    /**
     * Get the children of a given object
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
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            Class myClass = sbr.getClassFor(childrenClass);
            if (myClass == null)
                throw new ClassNotFoundException(childrenClass);
                RemoteObject[] res = sbr.getChildrenOfClass(parentOid,myClass);
                return res;
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            Class myClass = sbr.getClassFor(objectClass);
            return sbr.getObjectInfo(myClass, oid);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            Class myClass = sbr.getClassFor(objectClass);
            return sbr.getObjectInfoLight(myClass, oid);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
            throw e;
        }
    }

    /**
     * Updates attributes of a given object
     * @param update ObjectUpdate object representing only the changes to be committed
     * @param sessionId
     * @return Success or failure
     * @throws Exception
     */
    @WebMethod(operationName = "updateObject")
    public boolean updateObject(@WebParam(name = "objectupdate")ObjectUpdate update,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            boolean res;
            res = sbr.updateObject(update);
            return res;
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            Boolean res = sbr.setObjectLock(oid, objectclass, value);
            return res;
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
    public ClassInfoLight[] getPossibleChildren(
            @WebParam(name = "parentClass")String _parentClass,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            Class parentClass;
            parentClass = sbr.getClassFor(_parentClass);
            return sbr.getPossibleChildren(parentClass);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
    public ClassInfoLight[] getPossibleChildrenNoRecursive(
            @WebParam(name = "parentClass")String _parentClass,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            Class parentClass = sbr.getClassFor(_parentClass);
            return sbr.getPossibleChildrenNoRecursive(parentClass);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
    public ClassInfoLight[] getRootPossibleChildren(@WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return sbr.getRootPossibleChildren();
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            Class myClass = sbr.getClassFor(objectClass);
            return sbr.createObject(myClass,parentOid,template);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves all the class metadata
     * @param sessionId
     * @return An array with the complete metadata for each class
     * @throws Exception
     */
    @WebMethod(operationName = "getMetadata")
    public List<ClassInfo> getMetadata(@WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return sbr.getMetadata();
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            Class myClass = sbr.getClassFor(className);
            return sbr.getMetadataForClass(myClass);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            Class myClass = sbr.getClassFor(className);
            ObjectList res = sbr.getMultipleChoice(myClass);
            return res;
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            Boolean res = sbr.addPossibleChildren(parentClassId, possibleChildren);
            return res;
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            Boolean res = sbr.removePossibleChildren(parentClassId, childrenToBeRemoved);
            return res;
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
            throw e;
        }
    }

    /**
     * Deletes an object
     * @param className Object class' name
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
            Class myClass = sbr.getClassFor(className);
            Boolean res = sbr.removeObject(myClass, oid);
            return res;
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
            throw e;
        }
    }

    /**
     * Provides metadata for all classes, but the light version
     * @param sessionId
     * @return An array with the basic class metadata
     * @throws Exception
     */
    @WebMethod(operationName = "getLightMetadata")
    public List<ClassInfoLight> getLightMetadata(@WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return sbr.getLightMetadata();
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
            throw e;
        }
    }

    /**
     * Gets the id that should be used for the root object
     * @param sessionId
     * @return the Id that should be used to reference the root object
     */
    @WebMethod(operationName = "getDummyRootId")
    public Long getDummyRootId(@WebParam(name = "sessionId")String sessionId) {
        return sbr.getDummyRootId();
    }

    /**
     * Copy objects from its current parent to a target.
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
            @WebParam(name = "objectClases")String[] objectClasses,
            @WebParam(name = "templateObjects")Long[] templateObjects,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            RemoteObjectLight[] res = sbr.copyObjects(targetOid, templateObjects, objectClasses);
            return res;
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            @WebParam(name = "objectsClasses")String[] objectClasses,
            @WebParam(name = "objectsOids")Long[] objectOids,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            return sbr.moveObjects(targetOid, objectOids,objectClasses);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
            throw e;
        }
    }

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
            if (paramNames.length != paramValues.length || paramTypes.length != paramValues.length)
                throw new Exception(java.util.ResourceBundle.
                        getBundle("internationalization/Bundle").getString("LBL_ARRAYSIZESDONTMATCH")+"paramNames,paramValues, paramTypes");

            Class toBeSearched = sbr.getClassFor(className);
            RemoteObjectLight[] res = sbr.searchForObjects(toBeSearched,paramNames, paramTypes,paramValues);
            return res;
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            return sbr.setAttributePropertyValue(classId, attributeName, propertyName, propertyValue);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            return sbr.setClassPlainAttribute(classId,attributeName,attributeValue);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            return sbr.setClassIcon(classId, iconAttribute, iconImage);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
    public ClassInfoLight[] getInstanceableListTypes(@WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            ClassInfoLight[] res = sbr.getInstanceableListTypes();
            return res;
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            ViewInfo res;
            Class myClass = sbr.getClassFor(objectClass);
            if (!HierarchyUtils.isSubclass(myClass, ViewableObject.class))
                throw new Exception(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOVIEWS") + objectClass);
            else
                res = sbr.getDefaultView(oid, myClass);
            return res;
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            ViewInfo res = sbr.getRoomView(oid);
            return res;
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            ViewInfo res = sbr.getRackView(oid);
            return res;
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            Class myClass = sbr.getClassFor(objectClass);
            if (!HierarchyUtils.isSubclass(myClass, ViewableObject.class))
                throw new Exception(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOVIEWS") + objectClass);

            else return sbr.saveObjectView(oid, myClass,view);

        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
    public RemoteObjectLight createPhysicalContainerConnection(@WebParam(name="sourceObjectOid")Long sourceObjectOid,
            @WebParam(name="targetObjectOid")Long targetObjectOid,@WebParam(name="containerClass")String containerClass,
            @WebParam(name="parentObjectOid")Long parentObjectOid,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            Class myClass = sbr.getClassFor(containerClass);
            RemoteObjectLight res = sbr.createPhysicalContainerConnection(sourceObjectOid,targetObjectOid,myClass,parentObjectOid);
            return res;
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            Class myClass = sbr.getClassFor(connectionClass);
            if(!HierarchyUtils.isSubclass(myClass, GenericPhysicalConnection.class))
                throw new Exception(java.util.ResourceBundle.
                        getBundle("internationalization/Bundle").getString("LBL_WRONGCLASS")+ connectionClass);
            return sbr.createPhysicalConnection(endpointAOid,endpointBOid,myClass,parentObjectOid);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            return sbr.getUsers();
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            return sbr.getGroups();
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            return sbr.createUser();
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
    public Boolean deleteUsers(@WebParam(name="tobeDeleted")Long[] toBeDeleted) throws Exception{
        try{
            return sbr.deleteUsers(toBeDeleted);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            return sbr.createGroup();
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
    public Boolean deleteGroups(@WebParam(name="toBeDeleted")Long[] toBeDeleted) throws Exception{
        try{
            return sbr.deleteGroups(toBeDeleted);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
    public Boolean setUserProperties(@WebParam(name="oid")Long oid,
            @WebParam(name="propertiesNames")String[] propertiesNames,
            @WebParam(name="propertiesValues")String[] propertiesValues,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            if (propertiesNames.length != propertiesValues.length)
                throw new Exception(java.util.ResourceBundle.
                        getBundle("internationalization/Bundle").getString("LBL_ARRAYSIZESDONTMATCH")+ "propertiesNames, propertiesValues");

            return sbr.setUserProperties(oid, propertiesNames,propertiesValues);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
    public Boolean setGroupProperties(@WebParam(name="oid")Long oid,
            @WebParam(name="propertiesNames")String[] propertiesNames,
            @WebParam(name="propertiesValues")String[] propertiesValues,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            if (propertiesNames.length != propertiesValues.length)
                throw new Exception(java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").getString("LBL_ARRAYSIZESDONTMATCH")+ "propertiesNames, propertiesValues");
        
            return true;//sbr.setGroupProperties(oid, propertiesNames,propertiesValues);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            return sbr.addUsersToGroup(usersOids, groupOid);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            return sbr.removeUsersFromGroup(usersOids, groupOid);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            return sbr.addGroupsToUser(groupsOids, userOid);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
            return sbr.removeGroupsFromUser(groupsOids, userOid);
        }catch(Exception e){
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, e.getClass()+": "+e.getMessage());
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
                    get("javax.xml.ws.servlet.request")).getRemoteAddr().toString();
    }
}

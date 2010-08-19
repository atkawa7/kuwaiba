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
//import com.sun.xml.internal.ws.developer.Stateful;
import core.toserialize.ClassInfoLight;
import core.toserialize.UserGroupInfo;
import core.toserialize.UserInfo;
import core.toserialize.View;

import entity.core.ConfigurationItem;
import java.util.List;
import util.HierarchyUtils;

/**
 * Represents the main webservice
 * @author Charles Edward Bedón Cortázar <charles.bedon@zoho.com>
 */
@WebService
//@Stateful
public class KuwaibaWebservice {
    @EJB
    private BackendBeanRemote sbr;
    /*
     * Contains the last error or notification
     * TODO: This should be a per-session variable
     */
    private String lastErr ="No error specified";


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
     * @return true success or failure
     **/
    @WebMethod(operationName = "createSession")
    public boolean createSession(@WebParam(name = "username") String username, @WebParam(name = "password") String password){
        if(sbr.createSession(username,password))
            return true;
        this.lastErr=sbr.getError();
        return false;
    }

    /*
     * Close a session
     * @return true if it could close the session, false otherwise. In this case, an error message is written in the error stack
     **/
    @WebMethod(operationName = "closeSession")
    public boolean closeSession(){
        return true;
    }

    @WebMethod(operationName = "getObjectChildren")
    public RemoteObjectLight[] getObjectChildren(@WebParam(name = "oid") Long oid, @WebParam(name = "objectClassId") Long objectClassId){
        List res = sbr.getObjectChildren(oid,objectClassId);
        if(res == null)
            this.lastErr = "Error en el backendBean";
        return RemoteObjectLight.toArray(res);
    }

    @WebMethod(operationName = "getObjectInfo")
    public RemoteObject getObjectInfo(@WebParam(name = "objectclass") String className, @WebParam(name = "oid") Long oid){
        return sbr.getObjectInfo(className, oid);
    }

    @WebMethod(operationName = "updateObject")
    public boolean updateObject(@WebParam(name = "objectupdate")ObjectUpdate update){
        boolean res;
        res = sbr.updateObject(update);
        return res;
    }

    /**
     * @return String with the last error
     */
    @WebMethod(operationName = "getLastErr")
    public String getLastErr() {
        return lastErr;
    }

    /**
     * Sets an object's lock
     * @return Error or success
     */
    @WebMethod(operationName = "setObjectLock")
    public Boolean setObjectLock(@WebParam(name = "oid")Long oid, 
            @WebParam(name = "objectclass")String objectclass,
            @WebParam(name = "value")Boolean value) {
        System.out.println("[setObjectLock]: Llamado oid="+oid+" objectClass="+objectclass);

        Boolean res = sbr.setObjectLock(oid, objectclass, value);
        if(!res)
            this.lastErr = "Error en el backendBean";
        return res;
    }

    /**
     * Return all possible classes taht can be contained by the given class instances
     */
    @WebMethod(operationName = "getPossibleChildren")
    public ClassInfoLight[] getPossibleChildren(
            @WebParam(name = "parentClass")String _parentClass) {
        Class parentClass;
        try {
            parentClass = Class.forName(_parentClass);
            return sbr.getPossibleChildren(parentClass);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Return all possible classes taht can be contained by the given class instances
     */
    @WebMethod(operationName = "getPossibleChildrenNoRecursive")
    public ClassInfoLight[] getPossibleChildrenNoRecursive(
            @WebParam(name = "parentClass")String _parentClass) {
        Class parentClass;
        try {
            parentClass = Class.forName(_parentClass);
            return sbr.getPossibleChildrenNoRecursive(parentClass);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /*
     * Inquires for the possible children of a the root node
     */
    @WebMethod(operationName = "getRootPossibleChildren")
    public ClassInfoLight[] getRootPossibleChildren(){
        return sbr.getRootPossibleChildren();
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "createObject")
    public RemoteObjectLight createObject
            (@WebParam(name = "objectClass")String objectClass,
             @WebParam(name = "template")String template,
             @WebParam(name = "parentOid")Long parentOid) {
        
        return sbr.createObject(objectClass,parentOid,template);
    }

    /**
     * Informa acerca del meta data asociado a las clases. Inicialmente es llamado
     * al inicio de cada sesión, para proveer información de despliegue de atributos
     * y otros
     */
    @WebMethod(operationName = "getMetadata")
    public ClassInfo[] getMetadata() {
        ClassInfo[] res = sbr.getMetadata();
        if (res == null)
            this.lastErr = sbr.getError();
        return res;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "getMetadataForClass")
    public ClassInfo getMetadataForClass(@WebParam(name = "className")
    String className) {
        return sbr.getMetadataForClass(className);
    }

    /**
     * Recupera una lista
     */
    @WebMethod(operationName = "getMultipleChoice")
    public ObjectList getMultipleChoice(@WebParam(name = "className")
        String className) {
        return sbr.getMultipleChoice(className);
    }

    /**
     * Updates the container hierarchy for a given class
     * @param parentClass Clase a la cual se le desea adicionar un posible hijo en la jerarquía
     * @param possibleChildren Arreglo con los nombre de
     * @return true si la operación fur completada con éxito, false si ocurrió algún error. Si sólo la adición de algunas 
     */
    @WebMethod(operationName = "addPossibleChildren")
    public Boolean addPossibleChildren(@WebParam(name = "parentClassId")
    Long parentClassId, @WebParam(name = "possibleChildren")
    Long[] possibleChildren) {
        Boolean res = sbr.addPossibleChildren(parentClassId, possibleChildren);
        if(!res)
            this.lastErr = sbr.getError();
        return res;
    }

    @WebMethod(operationName = "removePossibleChildren")
    public Boolean removePossibleChildren(@WebParam(name = "parentClassId")
    Long parentClassId, @WebParam(name = "childrenToBeRemoved")
    Long[] childrenToBeRemoved) {
        Boolean res = sbr.removePossibleChildren(parentClassId, childrenToBeRemoved);
        if(!res)
            this.lastErr = sbr.getError();
        return res;
    }

    /**
     * Deletes an object
     * @param className Nombre de la clase a la que pertenece el elemento
     * @return true si fue posible ejecutar la acción, false en otro caso
     */
    @WebMethod(operationName = "removeObject")
    public Boolean removeObject(@WebParam(name = "className")
    String className, @WebParam(name = "oid")
    Long oid) {
        try{
            Class myClass = Class.forName(className);
            Boolean res = sbr.removeObject(myClass, oid);
            if(!res)
                this.lastErr = sbr.getError();
            return res;
        }catch (ClassNotFoundException cnfe){
            this.lastErr = cnfe.getMessage();
            return false;
        }
    }

    /**
     * Provides metadata for all classes, but the ligh version
     */
    @WebMethod(operationName = "getLightMetadata")
    public ClassInfoLight[] getLightMetadata() {
        return sbr.getLightMetadata();
    }

    @WebMethod(operationName = "getDummyRootId")
    public Long getDummyRootId() {
        return sbr.getDummyRootId();
    }

    /**
     * Copy objects from its current parent to a target.
     * Note: This method does *not* check if the parent change is possible according to the container hierarchy
     * the developer must check it on his side!
     * @param targetOid The new parent's oid
     * @param objectClasses Class names of the objects to be moved
     * @param templateOids Oids of the objects to be used as templates
     * @return An array with the new objects
     */
    @WebMethod(operationName = "copyObjects")
    public RemoteObjectLight[] copyObjects(@WebParam(name = "targetOid")
    Long targetOid, @WebParam(name = "objectClases")
    String[] objectClasses, @WebParam(name = "templateObjects")Long[] templateObjects ) {
        RemoteObjectLight[] res = sbr.copyObjects(targetOid, templateObjects, objectClasses);
            if (res == null)
                this.lastErr = sbr.getError();
        return res;
    }

    /**
     * Moves objects from its current parent to a target.
     * Note: This method does *not* check if the parent change is possible according to the container hierarchy
     * the developer must check it on his side!
     * @param targetOid The new parent's oid
     * @param objectClasses Class names of the objects to be moved
     * @param objectOids Oids of the objects to be moved
     * @return Success or failure
     */
    @WebMethod(operationName = "moveObjects")
    public Boolean moveObjects(@WebParam(name = "targetOid")
    Long targetOid, @WebParam(name = "objectsClasses")
            String[] objectClasses,@WebParam(name = "objectsOids")Long[] objectOids) {
        return sbr.moveObjects(targetOid, objectOids,objectClasses);
    }

    /**
     * Searches for objects given some criteria
     * @param  
     */
    @WebMethod(operationName = "searchForObjects")
    public RemoteObjectLight[] searchForObjects(@WebParam(name="className")String className, @WebParam(name="paramNames")
            String[] paramNames, @WebParam(name="paramTypes")String[] paramTypes,
            @WebParam(name="paramValues")String[] paramValues){

        if (paramNames.length != paramValues.length || paramTypes.length != paramValues.length){
            this.lastErr = java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").getString("LBL_ARRAYSIZESDONTMATCH")+"paramNames,paramValues, paramTypes";
            return null;
        }

        Class toBeSearched;
        try {
            toBeSearched = Class.forName(className);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(KuwaibaWebservice.class.getName()).log(Level.SEVERE, null, ex);
            this.lastErr = ex.getMessage();
            return null;
        }

        RemoteObjectLight[] res = sbr.searchForObjects(toBeSearched,paramNames, paramTypes,paramValues);
        if (res == null)
            this.lastErr = sbr.getError();
        return res;
    }

    /**
     * Sets the value of a property associated to an attribute. So far there are only
     * 4 possible properties:
     * -displayName
     * -isVisible
     * -isAdministrative
     * -description
     * @param classid The id of the class associated to the attribute
     * @param attributeName The name of the attribute
     * @param propertyName The name of the property
     * @param propertyValue The value of the property
     * @return Success or failure
     */
    @WebMethod(operationName = "setAttributePropertyValue")
    public Boolean setAttributePropertyValue(@WebParam(name = "classId")
    Long classId, @WebParam(name = "attributeName")
    String attributeName, @WebParam(name = "propertyName")
    String propertyName, @WebParam(name = "propertyValue")
    String propertyValue) {
        return sbr.setAttributePropertyValue(classId, attributeName, propertyName, propertyValue);
    }

    /**
     * Sets the string attributes in a class meta data (by now only the display name and description)
     * @param classId Class to be modified
     * @param attributeName attribute to be modified
     * @param attributeValue value for such attribute
     * @return success or failure
     */
    @WebMethod(operationName = "setClassPlainAttribute")
    public Boolean setClassPlainAttribute(@WebParam(name = "classId")
    Long classId, @WebParam(name = "attributeName")
    String attributeName, @WebParam(name = "attributeValue")
    String attributeValue) {
        return sbr.setClassPlainAttribute(classId,attributeName,attributeValue);
    }

    /**
     * Sets the image (icons) attributes in a class meta data (smallIcon and Icon)
     * @param classId Class to be modified
     * @param attributeName attribute to be modified
     * @param attributeValue value for such attribute
     * @return success or failure
     */
    @WebMethod(operationName = "setClassIcon")
    public Boolean setClassIcon(@WebParam(name = "classId")
    Long classId, @WebParam(name = "iconAttribute")
    String iconAttribute, @WebParam(name = "iconImage")
    byte[] iconImage) {
        return sbr.setClassIcon(classId, iconAttribute, iconImage);
    }

    /**
     * Returns the list type attributes
     */
    @WebMethod(operationName = "getInstanceableListTypes")
    public ClassInfoLight[] getInstanceableListTypes() {
        ClassInfoLight[] res = sbr.getInstanceableListTypes();
        if (res==null)
            lastErr = sbr.getError();
        return res;
    }

    /**
     * Views
     */
    
    /**
     * This method generates/retrieves the 
     * @param oid Object id for the object
     * @param className
     * @return a view object associated to the given object
     */
    @WebMethod(operationName = "getDefaultView")
    public View getDefaultView(@WebParam(name="oid")Long oid,
            @WebParam(name="className")String className){
        View res=null;
        try{
            Class myClass = Class.forName(className);
            if (!HierarchyUtils.isSubclass(myClass, ConfigurationItem.class))
                this.lastErr = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOVIEWS") + className;
            else
                res = sbr.getDefaultView(oid, myClass);
        if(res == null)
            this.lastErr = sbr.getError();
        }catch (ClassNotFoundException cnfe){
            this.lastErr = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CLASSNOTFOUND")+className;
            return null;
        }
        return res;
    }

    /**
     *
     * @param oid The oid for the related Room instance
     * @return a view object associated to the given Room
     */
    @WebMethod(operationName = "getRoomView")
    public View getRoomView(@WebParam(name="oid")Long oid){
        View res = sbr.getRoomView(oid);
        if(res == null)
            this.lastErr = sbr.getError();

        return res;
    }

    /**
     * Get the view of a simple rack. This is nothing but the rack and its children
     * placed within depending on the "rackUnits" attributes
     * @param oid The oid for the related Room instance
     * @return a view object associated to the given Room
     */
    @WebMethod(operationName = "getRackView")
    public View getRackView(@WebParam(name="oid")Long oid){
        View res = sbr.getRackView(oid);
        if(res == null)
            this.lastErr = sbr.getError();

        return res;
    }

    /**
     * User Management
     */

    /**
     * Retrieves all users
     * @return An user list
     */
    @WebMethod(operationName = "getUsers")
    public UserInfo[] getUsers(){
        UserInfo[] entityUsers = sbr.getUsers();
        if (entityUsers == null){
            this.lastErr = sbr.getError();
            return null;
        }
        return entityUsers;
    }

    /**
     * Retrieves all groups
     * @return A group list
     */
    @WebMethod(operationName = "getGroups")
    public UserGroupInfo[] getGroups(){
        UserGroupInfo[] groups = sbr.getGroups();
        if (groups == null)
            this.lastErr = sbr.getError();
        return groups;
    }

    /**
     * Creates a new user
     * @return The newly created user
     */
    @WebMethod(operationName = "createUser")
    public UserInfo createUser(){
        UserInfo res = sbr.createUser();
        if(res ==null)
            this.lastErr = sbr.getError();
        return res;
    }

    /**
     * Deletes an user
     * @return Success or failure
     */
    @WebMethod(operationName = "deleteUsers")
    public Boolean deleteUsers(Long[] oids){
        Boolean res = sbr.deleteUsers(oids);
        if(!res)
            this.lastErr = sbr.getError();
        return res;
    }

    /**
     * Creates a new group
     * @return The newly created group
     */
    @WebMethod(operationName = "createGroup")
    public UserGroupInfo createGroup(){
        UserGroupInfo res = sbr.createGroup();
        if(res ==null)
            this.lastErr = sbr.getError();
        return res;
    }

    /**
     * Deletes a list of groups
     * @return Success or failure
     */
    @WebMethod(operationName = "deleteGroups")
    public Boolean deleteGroups(Long[] oids){
        Boolean res = sbr.deleteGroups(oids);
        if(!res)
            this.lastErr = sbr.getError();
        return res;
    }

    /**
     * Set properties for a given user (username, name, etc)
     * @param oid User oid
     * @param propertiesNames Array with the names of the properties to be set
     * @param propertiesValues Array with the values of the properties to be set
     * @return Success or failure
     */
    @WebMethod(operationName = "setUserProperties")
    public Boolean setUserProperties(@WebParam(name="oid")Long oid,
            @WebParam(name="propertiesNames")String[] propertiesNames,
            @WebParam(name="propertiesValues")String[] propertiesValues){
        if (propertiesNames.length != propertiesValues.length){
            this.lastErr = java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").getString("LBL_ARRAYSIZESDONTMATCH")+ "propertiesNames, propertiesValues";
            return false;
        }
        Boolean res = sbr.setUserProperties(oid, propertiesNames,propertiesValues);
        if(!res)
            this.lastErr = sbr.getError();
        return res;
    }

    /**
     * Set properties for a given group (name, description)
     * @param oid Group oid
     * @param propertiesNames Array with the names of the properties to be set
     * @param propertiesValues Array with the values of the properties to be set
     * @return Success or failure
     */
    @WebMethod(operationName = "setGroupProperties")
    public Boolean setGroupProperties(@WebParam(name="oid")Long oid,
            @WebParam(name="propertiesNames")String[] propertiesNames,
            @WebParam(name="propertiesValues")String[] propertiesValues){
        if (propertiesNames.length != propertiesValues.length){
            this.lastErr = java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").getString("LBL_ARRAYSIZESDONTMATCH")+ "propertiesNames, propertiesValues";
            return false;
        }
        Boolean res = sbr.setGroupProperties(oid, propertiesNames,propertiesValues);
        if(!res)
            this.lastErr = sbr.getError();
        return res;
    }

    /**
     * Adds users to a group
     * @param usersOids An array with The users oids
     * @param groupOid The group's oid
     * @return Success or failure
     */
    @WebMethod(operationName = "addUsersToGroup")
    public Boolean addUsersToGroup(@WebParam(name="usersOids")Long[] usersOids,
            @WebParam(name="groupOid")Long groupOid){
        Boolean res = sbr.addUsersToGroup(usersOids, groupOid);
        if(!res)
            this.lastErr = sbr.getError();
        return res;
    }

    /**
     * Removes users from a group
     * @param usersOids An array with The users oids
     * @param groupOid The group's oid
     * @return Success or failure
     */
    @WebMethod(operationName = "removeUsersFromGroup")
    public Boolean removeUserfromGroup(@WebParam(name="usersOids")Long[] usersOids,
            @WebParam(name="groupOid")Long groupOid){
        Boolean res = sbr.removeUsersFromGroup(usersOids, groupOid);
        if(!res)
            this.lastErr = sbr.getError();
        return res;
    }

    /**
     * Assigns groups to a user
     * @param groupsOids An array with The groups oids
     * @param userOid The user's oid
     * @return Success or failure
     */
    @WebMethod(operationName = "addGroupsToUser")
    public Boolean addGroupsToUser(@WebParam(name="groupsOids")Long[] groupsOids,
            @WebParam(name="userOid")Long userOid){
        Boolean res = sbr.addGroupsToUser(groupsOids, userOid);
        if(!res)
            this.lastErr = sbr.getError();
        return res;
    }

    /**
     * Removes groups from a user
     * @param groupsOids An array with The groups oids
     * @param groupOid The user's oid
     * @return Success or failure
     */
    @WebMethod(operationName = "removeGroupsFromUser")
    public Boolean removeGroupsFromUser(@WebParam(name="groupsOids")Long[] groupsOids,
            @WebParam(name="userOid")Long userOid){
        Boolean res = sbr.removeGroupsFromUser(groupsOids, userOid);
        if(!res)
            this.lastErr = sbr.getError();
        return res;
    }
}

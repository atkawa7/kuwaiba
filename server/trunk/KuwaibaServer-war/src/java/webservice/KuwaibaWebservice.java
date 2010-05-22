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
 *  under the License.
 */
package webservice;

import core.toserialize.ClassInfo;
import core.toserialize.ObjectList;
import core.toserialize.RemoteTreeNode;
import core.todeserialize.ObjectUpdate;
import core.toserialize.RemoteObject;
import core.toserialize.RemoteObjectLight;
import core.toserialize.RemoteTreeNodeLight;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import businesslogic.BackendBeanRemote;
import core.exceptions.ObjectNotFoundException;
import core.toserialize.ClassInfoLight;
import javassist.bytecode.stackmap.TypeData.ClassName;

/**
 * Represents the main webservice
 * @author Charles Edward Bedón Cortázar <charles.bedon@zoho.com>
 */
@WebService()
public class KuwaibaWebservice {
    @EJB
    private BackendBeanRemote sbr;

    /*TODO: *Todas las sesiones deberían ver esta misma variable
            *Hacerlo sincronizado, para que si hay varios hilos(del mismo cliente) que generan
     *       errores no vaya a sobreescribir
     */
    /*
     * Contains the last error or notification
     */
    private String lastErr ="No error specified";

    /*
     * Authenticates the user
     * @param username user login
     * @param password user password
     * @return true success or failure
     **/
    @WebMethod(operationName = "createSession")
    public boolean createSession(@WebParam(name = "username") String username, @WebParam(name = "password") String password){
        if(username.equalsIgnoreCase("test") && password.equals("test"))
            return true;
        this.lastErr="Usuario o contraseña incorrecta";
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

    @WebMethod(operationName = "getTreeNode")
    public RemoteTreeNode getTreeNode(@WebParam(name = "oid")Long oid, @WebParam(name = "objectclass")String objectClass){
        System.out.println("[getTreeNode]: Llamada");
        Class objClass = null;
        try{
            objClass= Class.forName("entity.equipment.applicationlayer." + objectClass);
        }catch (ClassNotFoundException e){
            this.lastErr = "La clase especificada no existe";
            return null;
        }
        RemoteTreeNode res = sbr.getObjectInmediateHierarchy(oid, objectClass);
        if(res == null)
            this.lastErr = "Error en el backendBean";
        return res;
    }

    @WebMethod(operationName = "getTreeNodeLight")
    public RemoteTreeNodeLight getTreeNodeLight(@WebParam(name = "oid")Long oid, @WebParam(name = "objectclass")String objectClass){
        System.out.println("[getTreeNodeLight]: Llamada");
        Class objClass = null;
        try{
            objClass= Class.forName("entity.equipment.applicationlayer." + objectClass);
        }catch (ClassNotFoundException e){
            this.lastErr = "La clase especificada no existe";
            return null;
        }
        RemoteTreeNode res = sbr.getObjectInmediateHierarchy(oid, objectClass);
        if(res == null)
            this.lastErr = "Error en el backendBean";
        return null;
    }

    @WebMethod(operationName = "getObjectChildren")
    public RemoteObjectLight[] getObjectChildren(@WebParam(name = "oid") Long oid, @WebParam(name = "objectClass") String objectClass){
        System.out.println("[getObjectChildren]: Llamada");
        RemoteObjectLight[] res = sbr.getObjectChildren(oid,objectClass);
        if(res == null)
            this.lastErr = "Error en el backendBean";
        return res;
    }

    @WebMethod(operationName = "getObjectInfo")
    public RemoteObject getObjectInfo(@WebParam(name = "objectclass") String className, @WebParam(name = "oid") Long oid){
        return sbr.getObjectInfo(className, oid);
    }

    @WebMethod(operationName = "updateObject")
    public boolean updateObject(@WebParam(name = "objectupdate")ObjectUpdate update){
        boolean res=false;
        try{
            res = sbr.updateObject(update);
        }catch(ObjectNotFoundException oe){
            return false;
        }

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
     * Fija el lock del objeto (lo ancla o lo libera)
     * @return Si pudo o no hacerlo
     */
    @WebMethod(operationName = "setObjectLock")
    public Boolean setObjectLock(@WebParam(name = "oid")Long oid, 
            @WebParam(name = "objectclass")String objectclass,
            @WebParam(name = "value")Boolean value) {
        System.out.println("[setObjectLock]: Llamado oid="+oid+" objectClass="+objectclass);
        /*Class objClass = null;
        try{
            objClass= Class.forName("entity.equipment.applicationlayer." + objectClass);
        }catch (ClassNotFoundException e){
            this.lastErr = "La clase especificada no existe";
            return null;
        }*/
        Boolean res = sbr.setObjectLock(oid, objectclass, value);
        if(!res)
            this.lastErr = "Error en el backendBean";
        return res;
    }

    /**
     * Return all possible classes taht can be contained by the given class instances
     * TODO: To cache results on client side
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
        //TODO write your implementation code here:
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


    @WebMethod(operationName = "getDummyRootClass")
    public String getDummyRootClass() {
        return sbr.getDummyRootClass().getSimpleName();
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
            String[] paramNames, @WebParam(name="paramValues")String[] paramValues){

        if (paramNames.length != paramValues.length){
            this.lastErr = "The array sizes don't match (paramNames,paramValues)";
            return null;
        }

        RemoteObjectLight[] res = sbr.searchForObjects(className,paramNames, paramValues);
        if (res == null)
            this.lastErr = sbr.getError();
        return res;
    }
}

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
import org.kuwaiba.beans.WebServiceBeanRemote;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.ws.toserialize.application.RemoteSession;
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
    private WebServiceBeanRemote wsBean;
   /**
     * The context to get information about each request
     */
    @Resource
    private WebServiceContext context;

    // <editor-fold defaultstate="collapsed" desc="Session methods. Click on the + sign on the left to edit the code.">
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
    }//</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Business Methods. Click on the + sign on the left to edit the code.">
    /**
     * Gets the children of a given object given his class id and object id
     * @param oid object's id
     * @param objectClassId object's class id
     * @param sessionId Session token
     * @return An array of all the direct children of the provided object according with the current container hierarchy
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectChildren")
    public RemoteObjectLight[] getObjectChildren(@WebParam(name = "oid") Long oid,
            @WebParam(name = "objectClassId") Long objectClassId,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            //wsBean.validateCall("getObjectChildren", getIPAddress(), sessionId);
            RemoteObjectLight[] res = wsBean.getObjectChildren(oid,objectClassId);
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
     * @param sessionId Session token
     * @return An array of all the direct children of the provided object according with the current container hierarchy
     * @throws Exception Generic exception encapsulating any possible error raised at runtime
     */
    @WebMethod(operationName = "getObjectChildrenByClassName")
    public RemoteObjectLight[] getObjectChildrenByClassName(@WebParam(name = "oid") Long oid,
            @WebParam(name = "objectClassName") Long objectClassName,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            //wsBean.validateCall("getObjectChildren", getIPAddress(), sessionId);
            RemoteObjectLight[] res = wsBean.getObjectChildren(objectClassName, oid);
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
     * @param parentOid Object oid whose cho
     * @param childrenClass
     * @return An array with children
     * @throws An general exception in case of error. Consumer of this method must check the message for details
     */
    @WebMethod(operationName="getChildrenOfClass")
    public RemoteObject[] getChildrenOfClass(@WebParam(name="parentOid")Long parentOid,
            @WebParam(name="parentClass")String parentClass,
            @WebParam(name="childrenClass")String childrenClass,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            //sbr.validateCall("getChildrenOfClass", getIPAddress(), sessionId);
            RemoteObject[] res = wsBean.getChildrenOfClass(parentOid,parentClass,childrenClass);
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
     * @return An array with children
     * @throws An general exception in case of error. Consumer of this method must check the message for details
     */
    @WebMethod(operationName="getChildrenOfClassLight")
    public RemoteObjectLight[] getChildrenOfClassLight(@WebParam(name="parentOid")Long parentOid,
            @WebParam(name="parentClass")String parentClass,
            @WebParam(name="childrenClass")String childrenClass,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            //sbr.validateCall("getChildrenOfClass", getIPAddress(), sessionId);
            return wsBean.getChildrenOfClassLight(parentOid,parentClass,childrenClass);
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
            //sbr.validateCall("getObjectInfo", getIPAddress(), sessionId);
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
            //sbr.validateCall("getObjectInfoLight", getIPAddress(), sessionId);
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
            //sbr.validateCall("updateObject", getIPAddress(), sessionId);
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

    @WebMethod(operationName = "createObject")
    public Long createObject(@WebParam(name = "className")String className,
            @WebParam(name = "parentObjectClassName")String parentObjectClassName,
            @WebParam(name = "parentOid")Long parentOid,
            @WebParam(name = "attributeNames")String[] attributeNames,
            @WebParam(name = "attributeValues")String[] attributeValues,
            @WebParam(name = "templateId")Long templateId,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            //sbr.validateCall("createObject", getIPAddress(), sessionId);
            return wsBean.createObject(className,parentObjectClassName, parentOid,attributeNames,attributeValues, templateId);
        }catch(Exception e){
            Level level = Level.SEVERE;
            if (e instanceof ServerSideException)
                level = ((ServerSideException)e).getLevel();
            Logger.getLogger(Kuwaiba.class.getName()).log(level,
                    e.getClass().getSimpleName()+": {0}",e.getMessage()); //NOI18N
            throw e;
        }
    }// </editor-fold>

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

            try
            {
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
            ClassInfo ci = new ClassInfo();
            ci.setClassName(name);
            ci.setDisplayName(displayName);
            ci.setDescription(description);
            ci.setIcon(icon);
            ci.setSmallIcon(smallIcon);
            ci.setParentClassName(parentClassName);
            ci.setIsAbstract(abstractClass);

            wsBean.changeClassDefinition(ci);

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
        try
        {
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
        try
        {
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

        try
        {
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

        try
        {
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

        try
        {
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

        try
        {
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

        try
        {
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
    @WebMethod(operationName = "deleteClassByName")
    public void deleteClassByName(@WebParam(name = "className")
    String className, @WebParam(name = "sessionId")
    String sessionId) throws Exception {

        try
        {
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

        try
        {
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

        try
        {
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

        try
        {
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
    Long parentClassId, @WebParam(name = "childrenToBeRemoved")
    Long[] newPossibleChildren, @WebParam(name = "sessionId")
    String sessionId) throws Exception {

        try
        {
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

        try
        {
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

    @WebMethod(operationName = "createListTypeItem")
    public Long createListTypeItem(
            @WebParam(name = "className") String className,
            @WebParam(name = "name") String name,
            @WebParam(name = "displayName") String displayName,
            @WebParam(name = "sessionId") String sessionId) throws Exception{

        try
        {
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


    @WebMethod(operationName = "getMultipleChoice")
    public RemoteObjectLight[] getMultipleChoice(
            @WebParam(name = "className") String className,
            @WebParam(name = "sessionId") String sessionId) throws Exception{
        try
        {
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

    @WebMethod(operationName = "getInstanceableListTypes")
    public ClassInfoLight[] getInstanceableListTypes(
            @WebParam(name = "sessionId") String sessionId) throws Exception{
        try
        {
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

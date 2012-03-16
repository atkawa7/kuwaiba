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

import java.util.Date;
import java.util.HashMap;
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
     * Get all children in an object of an specific class
     * @param parentOid
     * @param childrenClass
     * @return An array with children
     * @throws An exception (ClassNotFoundException or any other) in case of error
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
        if (oid == null)
                throw new ServerSideException(Level.WARNING, "Object id can't be null");

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
     * @param update ObjectUpdate object representing only the changes to be committed
     * @param sessionId
     * @return the updated object
     * @throws Exception
     */
    @WebMethod(operationName = "updateObject")
    public void updateObject(@WebParam(name = "className")String className,
            @WebParam(name = "oid")Long oid,
            @WebParam(name = "attributes")HashMap<String,String> attributes,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            //sbr.validateCall("updateObject", getIPAddress(), sessionId);
            wsBean.updateObject(className,oid,attributes);
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
    public RemoteObjectLight createObject(@WebParam(name = "className")String className, Long parentOid,
            HashMap<String,String> attributes,String template,
            @WebParam(name = "sessionId")String sessionId) throws Exception{
        try{
            //sbr.validateCall("createObject", getIPAddress(), sessionId);
            return wsBean.createObject(className,parentOid,attributes, template);
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
     * Web service operation
     */
    @WebMethod(operationName = "addAttribute")
    public Boolean addAttributeByClassId(@WebParam(name = "className")
        String ClassName, @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "type")
        String type, @WebParam(name = "description")
        Integer description, @WebParam(name = "administrative")
        Boolean administrative, @WebParam(name = "visible")
        Boolean visible, @WebParam(name = "mapping")
        Boolean mapping, @WebParam(name = "readOnly")
        Boolean readOnly, @WebParam(name = "unique")
        Boolean unique) throws Exception {

       AttributeInfo ai = new AttributeInfo(name, displayName, type,
                            administrative, visible, displayName, description);

        return wsBean.addAttribute(ClassName, ai);
    }

        /**
     * addAttribute
     */
    @WebMethod(operationName = "addAttribute")
    public Boolean addAttributeByClassName(@WebParam(name = "ClassId")
        Long ClassId, @WebParam(name = "name")
        String name, @WebParam(name = "displayName")
        String displayName, @WebParam(name = "type")
        String type, @WebParam(name = "description")
        Integer description, @WebParam(name = "administrative")
        Boolean administrative, @WebParam(name = "visible")
        Boolean visible, @WebParam(name = "mapping")
        Boolean mapping, @WebParam(name = "readOnly")
        Boolean readOnly, @WebParam(name = "unique")
        Boolean unique) throws Exception {

       AttributeInfo ai = new AttributeInfo(name, displayName, type,
                            administrative, visible, displayName, description);

        return wsBean.addAttribute(ClassId, ai);
    }
    
    /**
     * getClassByName Gets a classmetadata by its name
     */
    @WebMethod(operationName = "getClassByName")
    public ClassInfo getClassByName(@WebParam(name = "className")
    String className) throws Exception {
        ClassInfo ci = wsBean.getClass(className);
        return ci;
    }

    /**
     * getClassById Gets a classmetadata by its id
     */
    @WebMethod(operationName = "getClassById")
    public ClassInfo getClassById(@WebParam(name = "classId")
    Long classId) throws Exception {
        ClassInfo ci = wsBean.getClass(classId);
        return ci;
    }

    /**
     * deleteClass
     */
    @WebMethod(operationName = "deleteClassById")
    public boolean deleteClassByName(@WebParam(name = "className")
    String className) throws Exception {
        return wsBean.deleteClass(className);
    }

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

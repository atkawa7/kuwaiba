/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.communications;

import com.neotropic.inventory.modules.sdh.LocalSDHContainerLinkDefinition;
import com.neotropic.inventory.modules.sdh.LocalSDHPosition;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;
import org.inventory.communications.core.LocalApplicationLogEntry;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalBusinessRule;
import org.inventory.communications.core.LocalFavoritesFolder;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalLogicalConnectionDetails;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectLightList;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalReport;
import org.inventory.communications.core.LocalReportLight;
import org.inventory.communications.core.LocalTaskResultMessage;
import org.inventory.communications.core.LocalTask;
import org.inventory.communications.core.LocalTaskNotificationDescriptor;
import org.inventory.communications.core.LocalTaskResult;
import org.inventory.communications.core.LocalTaskScheduleDescriptor;
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.communications.core.LocalUserObjectLight;
import org.inventory.communications.core.caching.Cache;
import org.inventory.communications.core.queries.LocalQuery;
import org.inventory.communications.core.queries.LocalQueryLight;
import org.inventory.communications.core.queries.LocalResultRecord;
import org.inventory.communications.core.queries.LocalTransientQuery;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.communications.wsclient.ApplicationLogEntry;
import org.inventory.communications.wsclient.AttributeInfo;
import org.inventory.communications.wsclient.ClassInfo;
import org.inventory.communications.wsclient.ClassInfoLight;
import org.inventory.communications.wsclient.GroupInfo;
import org.inventory.communications.wsclient.KuwaibaService;
import org.inventory.communications.wsclient.KuwaibaService_Service;
import org.inventory.communications.wsclient.PrivilegeInfo;
import org.inventory.communications.wsclient.RemoteFavoritesFolder;
import org.inventory.communications.wsclient.RemoteBusinessObjectLight;
import org.inventory.communications.wsclient.RemoteBusinessObjectLightList;
import org.inventory.communications.wsclient.RemoteBusinessRule;
import org.inventory.communications.wsclient.RemoteObject;
import org.inventory.communications.wsclient.RemoteObjectLight;
import org.inventory.communications.wsclient.RemoteObjectLightArray;
import org.inventory.communications.wsclient.RemoteObjectSpecialRelationships;
import org.inventory.communications.wsclient.RemotePool;
import org.inventory.communications.wsclient.RemoteQueryLight;
import org.inventory.communications.wsclient.RemoteReport;
import org.inventory.communications.wsclient.RemoteReportLight;
import org.inventory.communications.wsclient.RemoteResultMessage;
import org.inventory.communications.wsclient.RemoteTask;
import org.inventory.communications.wsclient.RemoteTaskResult;
import org.inventory.communications.wsclient.ResultRecord;
import org.inventory.communications.wsclient.SdhContainerLinkDefinition;
import org.inventory.communications.wsclient.SdhPosition;
import org.inventory.communications.wsclient.ServerSideException_Exception;
import org.inventory.communications.wsclient.StringArray;
import org.inventory.communications.wsclient.StringPair;
import org.inventory.communications.wsclient.TaskNotificationDescriptor;
import org.inventory.communications.wsclient.TaskScheduleDescriptor;
import org.inventory.communications.wsclient.TransientQuery;
import org.inventory.communications.wsclient.UserInfo;
import org.inventory.communications.wsclient.UserInfoLight;
import org.inventory.communications.wsclient.Validator;
import org.inventory.communications.wsclient.ViewInfo;
import org.inventory.communications.wsclient.ViewInfoLight;

/**
 * Singleton class that provides communication and caching services to the rest of the modules
 * TODO: Make it a thread to support simultaneous operations
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class CommunicationsStub {
    private static CommunicationsStub instance;
    private KuwaibaService service;
    private static URL serverURL = null;
    private String error = java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_ERROR");
    private Cache cache;
    private LocalSession session;
    
    private static String[] classesWithCustomDeleteActions = new String[] {"ElectricalLink", "RadioLink", "OpticalLink", "MPLSLink",
                                                                    "VC4", "VC4-04", "VC4-16", "VC4TributaryLink", "VC12TributaryLink", "VC3TributaryLink",
                                                                    "STM1", "STM4", "STM16", "STM64", "STM256",
                                                                    "WireContainer", "WirelessContainer",
                                                                    "CorporateCustomer", "TelecomOperator", "Provider", "HomeCustomer",
                                                                    "Subnet" };
    
    private CommunicationsStub() {
        cache = Cache.getInstace();
    }

    //Implements the singleton pattern
    public static CommunicationsStub getInstance() {
            if(instance == null)
                instance = new CommunicationsStub();
            return instance;
    }

    /**
     * Resets the singleton instance to null so it has to be created again
     */
    public static void resetInstance() {
        serverURL = null;
        instance = null;
    }

    /**
     * Sets the webservice URL
     * @param URL A valid URL
     */
    public static void setServerURL(URL URL){
        serverURL = URL;
    }
    
    public static URL getServerURL (){
        return serverURL;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Session methods. Click on the + sign on the left to edit the code.">
    public LocalSession getSession(){
        return session;
    }
    
    /**
     * This method closes the current session
     */
    public void closeSession(){
        try {
            service.closeSession(this.session.getSessionId());
            session = null;
        }catch(Exception ex){
            this.error =  ex.getMessage();
        }
    }

    /**
     * Creates a session
     * @param user The user for this session
     * @param password The password for the user
     * @param disableHostNameValidation Checks if the CN in the certificate matches the server we're trying to connect to. Set to true when using self signed certificate and connecting to an IP address instead of a FQDN
     * @return Success or failure
     */
    public boolean createSession(String user, String password, boolean disableHostNameValidation){
        try {
            
            if (disableHostNameValidation) {
                javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                    new HostnameVerifier(){
                        @Override
                        public boolean verify(String hostname, SSLSession sslSession) {
                            return true;
                        }
                    });
            }
                
            if (serverURL == null)
                serverURL = new URL("http", "localhost", 8080,"/kuwaiba/KuwaibaService?wsdl"); //NOI18n

            this.service = new KuwaibaService_Service(serverURL).getKuwaibaServicePort();
            
            //Adds support for HTTP compression (if available). Don't forget to activate compression at server side.
            ((BindingProvider)service).getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS,
                Collections.singletonMap("Accept-Encoding",Collections.singletonList("gzip"))); //NOI18N
            
            this.session = new LocalSession(this.service.createSession(user, password));
            return true;
        }catch(Exception ex) { 
            this.error =  ex.getMessage();
            return false;
        }
    }// </editor-fold>

    /**
     * Retrieves an object children providing the object class id
     * @param oid object's id
     * @param objectClassId object's class id
     * @return an array of local objects representing the object's children. Null in a problem occurs during the execution
     */
    public List<LocalObjectLight> getObjectChildren(long oid, long objectClassId){
        try{
            List <RemoteObjectLight> children = service.getObjectChildrenForClassWithId(oid, objectClassId, 0, this.session.getSessionId());
            List <LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : children){
                HashMap<String, Integer> validators = new HashMap<>();
                for (Validator validator : rol.getValidators())
                    validators.put(validator.getLabel(), validator.getValue());
                
                res.add(new LocalObjectLight(rol.getClassName(), rol.getName(), rol.getOid(), validators));
            }
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight[] getSiblings(String objectClass, long objectId) {
        try{
            List <RemoteObjectLight> siblings = service.getSiblings(objectClass, objectId, 0,this.session.getSessionId());
            LocalObjectLight[] res = new LocalObjectLight[siblings.size()];
            
            int i = 0;
            for (RemoteObjectLight rol : siblings){
                HashMap<String, Integer> validators = new HashMap<>();
                for (Validator validator : rol.getValidators())
                    validators.put(validator.getLabel(), validator.getValue());
                
                res[i] = new LocalObjectLight(rol.getClassName(), rol.getName(), rol.getOid(), validators);
                i++;
            }
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Retrieves an object children providing the object class name
     * @param oid object id
     * @param className object class name
     * @return an array of local objects representing the object's children. Null in a problem occurs during the execution
     */
    public List<LocalObjectLight> getObjectChildren(long oid, String className) {
        try {
            List <RemoteObjectLight> children = service.getObjectChildren(className, oid, 0,this.session.getSessionId());
            List <LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : children)
                res.add(new LocalObjectLight(rol.getOid(), rol.getName(), rol.getClassName()));

            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }

    public List<LocalObject> getChildrenOfClass(long oid, String parentClassName, String childrenClassName){
        try{
            List <RemoteObject> children = service.getChildrenOfClass(oid, parentClassName, childrenClassName, 0, this.session.getSessionId());
            List <LocalObject> res = new ArrayList<>();

            for (RemoteObject rol : children){
                List<List<String>> values = new ArrayList<>();
                for (StringArray value : rol.getValues())
                    values.add(value.getItem());

                res.add(new LocalObject(rol.getClassName(), rol.getOid(), rol.getAttributes(),
                        values, getMetaForClass(rol.getClassName(), false)));
            }
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public List<LocalObjectLight> getObjectsOfClassLight(String className){
        try{
            List <RemoteObjectLight> instances = service.getObjectsOfClassLight(className, 0, this.session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : instances)
                res.add(new LocalObjectLight(rol.getOid(), rol.getName(), rol.getClassName()));

            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    // </editor-fold>

    /**
     * Updates the attributes of a given object
     * @param obj is the object to be updated. Note that this object doesn't have
     *            every field within the "original". it only has the field(s) to be updated
     * @return 
     */
    public boolean saveObject(LocalObject obj){

        try{
            List<String> attributeNames = new ArrayList<>();
            List<String> attributeValues = new ArrayList<>();

            for (String key : obj.getAttributes().keySet()){
                String value;
                attributeNames.add(key);
                Object theValue = obj.getAttribute(key);
                if (theValue instanceof LocalObjectListItem)
                    value = String.valueOf(((LocalObjectListItem)theValue).getId());
                else {
                    if (theValue instanceof Date)
                        value = String.valueOf(((Date)theValue).getTime());
                    else
                        value = theValue.toString();
                }
                attributeValues.add(value);
            }
            service.updateObject(obj.getClassName(),obj.getOid(), attributeNames, attributeValues, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }

    /**
     * Retrieves the whole object info
     * @param objectClass object class
     * @param oid object id
     * @return The local representation of the object
     */
    public LocalObject getObjectInfo(String objectClass, long oid){
        try {
            LocalClassMetadata lcmd = getMetaForClass(objectClass, false);
            RemoteObject myObject = service.getObject(objectClass, oid,this.session.getSessionId());
            List<List<String>> values = new ArrayList<>();
            for (StringArray value : myObject.getValues())
                values.add(value.getItem());
            return new LocalObject(myObject.getClassName(), myObject.getOid(), 
                    myObject.getAttributes(), values,lcmd);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /** 
     * Gets the common parent of a given object in the standard or special containment
     * hierarchy.
     * @param aObjectClass Object class name of child a
     * @param aOid Object id for the child a
     * @param bObjectClass Object class name of child b
     * @param bOid Object id for the child b
     * @return The common parent object
     */
    public LocalObjectLight getCommonParent(String aObjectClass, long aOid, String bObjectClass, long bOid) {
        try {
            RemoteObjectLight parent = service.getCommonParent(aObjectClass, aOid, bObjectClass, bOid, session.getSessionId());
            return new LocalObjectLight(parent.getOid(), parent.getName(), parent.getClassName());
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Retrieves all the ancestors of an object in the standard and special containment hierarchy. 
     * If the provided object is in a pool, the ancestor pools will be returned.
     * @param objectClass Object class of child
     * @param objectId Object id for the child
     * @return The list of ancestors.
     */
    public List<LocalObjectLight> getParents(String objectClass, long objectId) {
        try {
            List<RemoteObjectLight> parents = service.getParents(objectClass, objectId, session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();
            for (RemoteObjectLight aParent : parents)
                res.add(new LocalObjectLight(aParent.getOid(), aParent.getName(), aParent.getClassName()));

            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /** 
     * Gets the parent of a given object in the standard or special containment
     * hierarchy.
     * @param objectClass Object class of child
     * @param objectId Object id for the child
     * @return The parent object
     */
    public LocalObjectLight getParent(String objectClass, long objectId) {
        try {
            RemoteObjectLight parent = service.getParent(objectClass, objectId, session.getSessionId());
            return new LocalObjectLight(parent.getOid(), parent.getName(), parent.getClassName());
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets the list of parents (according to the special and standard containment hierarchy) until it finds an instance of class 
     * objectToMatchClassName (for example "give me the parents of this port until you find the nearest rack")
     * @param objectClass Class of the object to get the parents from
     * @param objectId Id of the object to get the parents from
     * @param objectToMatchClassName Class of the object that will limit the search. It can be a superclass, if you want to match many classes at once
     * @return The list of parents until an instance of objectToMatchClassName is found. If no instance of that class is found, all parents until the Dummy Root will be returned. NUll in case of error
     */
    public List<LocalObjectLight> getParentsUntilFirstOfClass(String objectClass, long objectId, String objectToMatchClassName) {
        try {
            List<RemoteObjectLight> parents = service.getParentsUntilFirstOfClass(objectClass, objectId, objectToMatchClassName, session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();
            for (RemoteObjectLight aParent : parents)
                res.add(new LocalObjectLight(aParent.getOid(), aParent.getName(), aParent.getClassName()));

            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public HashMap<String, LocalObjectLight[]> getSpecialAttributes (String objectClass, long objectId) {
        try{
            RemoteObjectSpecialRelationships remoteRelationships = service.getSpecialAttributes(objectClass, objectId, session.getSessionId());
            HashMap<String, LocalObjectLight[]> res = new HashMap<>();
            
            for (int i = 0; i < remoteRelationships.getRelationships().size(); i++){
                
                RemoteObjectLightArray relatedRemoteObjects = remoteRelationships.getRelatedObjects().get(i);
                LocalObjectLight[] relatedLocalObjects = new LocalObjectLight[relatedRemoteObjects.getItem().size()];
                int j = 0;
                for (RemoteObjectLight relatedRemoteObject : relatedRemoteObjects.getItem()) {
                    relatedLocalObjects[j] = new LocalObjectLight(relatedRemoteObject.getOid(), 
                                                    relatedRemoteObject.getName(), 
                                                    relatedRemoteObject.getClassName());
                    j++;
                }
                res.put(remoteRelationships.getRelationships().get(i), relatedLocalObjects);
            }
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    public List<LocalObjectLight> getSpecialAttribute(String objectClass, long objectId, String attributeName){
        try{

            List<RemoteObjectLight> values = service.getSpecialAttribute(objectClass, objectId,attributeName, session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();
            for (RemoteObjectLight value : values)
                res.add(new LocalObjectLight(value.getOid(), value.getName(), value.getClassName()));

            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Retrieves the basic object info
     * @param objectClass object class
     * @param oid object id
     * @return The local representation of the object
     */
    public LocalObjectLight getObjectInfoLight(String objectClass, long oid){
        try{
            RemoteObjectLight myLocalObject = service.getObjectLight(objectClass, oid,this.session.getSessionId());
            HashMap<String, Integer> validators = new HashMap<>();
                for (Validator validator : myLocalObject.getValidators())
                    validators.put(validator.getLabel(), validator.getValue());
            
                return new LocalObjectLight(myLocalObject.getClassName(), myLocalObject.getName(), 
                        myLocalObject.getOid(), validators);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalApplicationLogEntry[] getBusinessObjectAuditTrail(String objectClass, long oid){
        try{
            List<ApplicationLogEntry> myEntries = service.getBusinessObjectAuditTrail(objectClass, oid, 0, this.session.getSessionId());
            
            LocalApplicationLogEntry[] res = new LocalApplicationLogEntry[myEntries.size()];
            
            //We sort the array here, since it's not from the source
            Collections.sort(myEntries, new Comparator<ApplicationLogEntry>(){

                @Override
                public int compare(ApplicationLogEntry o1, ApplicationLogEntry o2) {
                    if (o1.getTimestamp() < o2.getTimestamp())
                        return -1;
                    return 1;
                }
            });
            
            for (int i = 0; i < myEntries.size(); i++)
                res[i] = new LocalApplicationLogEntry(myEntries.get(i).getId(),
                                                        myEntries.get(i).getObjectId(),
                                                        myEntries.get(i).getType(),
                                                        myEntries.get(i).getUserName(),
                                                        myEntries.get(i).getTimestamp(),
                                                        myEntries.get(i).getAffectedProperty(),
                                                        myEntries.get(i).getOldValue(),
                                                        myEntries.get(i).getNewValue(),
                                                        myEntries.get(i).getNotes());
            
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    public LocalApplicationLogEntry[] getGeneralActivityAuditTrail(int page, int limit){
        try{
            List<ApplicationLogEntry> myEntries = service.getGeneralActivityAuditTrail(page, limit, this.session.getSessionId());
            
            LocalApplicationLogEntry[] res = new LocalApplicationLogEntry[myEntries.size()];
         
            
            for (int i = 0; i < myEntries.size(); i++)
                res[i] = new LocalApplicationLogEntry(myEntries.get(i).getId(),
                                                        myEntries.get(i).getObjectId(),
                                                        myEntries.get(i).getType(),
                                                        myEntries.get(i).getUserName(),
                                                        myEntries.get(i).getTimestamp(),
                                                        myEntries.get(i).getAffectedProperty(),
                                                        myEntries.get(i).getOldValue(),
                                                        myEntries.get(i).getNewValue(),
                                                        myEntries.get(i).getNotes());
            
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Returns the last error
     * @return The error string
     */
    public synchronized  String getError() {
        if (error == null)
            error = "Unknown error";
        return error;
    }

    /**
     * Gets the possible instances that can be contained into a give class instance.
     * Pay attention that this method calls the recursive web method. This is,
     * this method won't give you the abstract classes in the container hierarchy
     * but those instanceables. This method is used by the navigation tree nodes
     * to know what classes to show in the menu, but it's not used by the container manager,
     * which uses getPossibleChildrenNoRecursive
     * The result is cached
     * @param className The class you want to get the possible children from
     * @param ignoreCache True to ignore the local cache, false otherwise
     * @return allPosible children
     */
    public List<LocalClassMetadataLight> getPossibleChildren(String className, boolean ignoreCache) {
        try{
            List<LocalClassMetadataLight> resAsLocal = null;
            if (!ignoreCache)
                resAsLocal = cache.getPossibleChildrenCached(className);
            
            if (resAsLocal == null){
                resAsLocal = new ArrayList<>();
                List<ClassInfoLight> resAsRemote = service.getPossibleChildren(className,this.session.getSessionId());

                for (ClassInfoLight cil : resAsRemote){
                    HashMap<String, Integer> validators = new HashMap<>();
                    for (Validator validator : cil.getValidators())
                        validators.put(validator.getLabel(), validator.getValue());
                    
                    resAsLocal.add(new LocalClassMetadataLight(cil.getId(),
                                                cil.getClassName(),
                                                cil.getDisplayName(),
                                                cil.getParentClassName(),
                                                cil.isAbstract(),cil.isViewable(), cil.isListType(),
                                                cil.isCustom(), cil.isInDesign(),
                                                cil.getSmallIcon(), 
                                                cil.getColor(), validators));
                }
                cache.addPossibleChildrenCached(className, resAsLocal);
            }
            return resAsLocal;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Same as above method, but this one doesn't go deeper into the container hierarchy
     * The result is not cached
     * @param className
     * @return allPosible children
     */
    public List<LocalClassMetadataLight> getPossibleChildrenNoRecursive(String className) {
        try{
            List<ClassInfoLight> resAsRemote = service.getPossibleChildrenNoRecursive(className,this.session.getSessionId());
            List<LocalClassMetadataLight> resAsLocal = new ArrayList<>();

            for (ClassInfoLight cil : resAsRemote){
               HashMap<String, Integer> validators = new HashMap<>();
                    for (Validator validator : cil.getValidators())
                        validators.put(validator.getLabel(), validator.getValue());
                    
                    resAsLocal.add(new LocalClassMetadataLight(cil.getId(),
                                                cil.getClassName(),
                                                cil.getDisplayName(),
                                                cil.getParentClassName(),
                                                cil.isAbstract(),cil.isViewable(), cil.isListType(),
                                                cil.isCustom(), cil.isInDesign(),
                                                cil.getSmallIcon(), 
                                                cil.getColor(), validators));
            }
            return resAsLocal;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets possible special children for a class. The result is cached
     * @param className Class name
     * @param ignoreCache Ignore local cache
     * @return The list of possible special children
     */
    public List<LocalClassMetadataLight> getPossibleSpecialChildren(String className, boolean ignoreCache) {
        try {
        List<LocalClassMetadataLight> resAsLocal = null;
        if (!ignoreCache) 
            resAsLocal = cache.getPossibleSpecialChildrenCached(className);
        
        if (resAsLocal == null) {
                resAsLocal = new ArrayList<>();
                List<ClassInfoLight> resAsRemote = service.getPossibleSpecialChildren(className, session.getSessionId());

                for (ClassInfoLight cil : resAsRemote){
                    HashMap<String, Integer> validators = new HashMap<>();
                    for (Validator validator : cil.getValidators())
                        validators.put(validator.getLabel(), validator.getValue());

                    resAsLocal.add(new LocalClassMetadataLight(cil.getId(),
                        cil.getClassName(),
                        cil.getDisplayName(),
                        cil.getParentClassName(),
                        cil.isAbstract(),cil.isViewable(), cil.isListType(),
                        cil.isCustom(), cil.isInDesign(),
                        cil.getSmallIcon(), 
                        cil.getColor(), validators));
                }
                cache.addPossibleSpecialChildrenCached(className, resAsLocal);
            }
            return resAsLocal;
        } catch (Exception ex) {
            error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets the possible children to the class. The result is not cached
     * @param className Class name
     * @return The list of possible special children for the class
     */
    public List<LocalClassMetadataLight> getPossibleSpecialChildrenNoRecursive(String className) {
        try {
            List<ClassInfoLight> resAsRemote = service.getPossibleSpecialChildrenNoRecursive(className, session.getSessionId());
            List<LocalClassMetadataLight> resAsLocal = new ArrayList<>();
            
            for (ClassInfoLight cil : resAsRemote){
                HashMap<String, Integer> validators = new HashMap<>();
                for (Validator validator : cil.getValidators())
                    validators.put(validator.getLabel(), validator.getValue());
                    
                resAsLocal.add(new LocalClassMetadataLight(cil.getId(),
                    cil.getClassName(),
                    cil.getDisplayName(),
                    cil.getParentClassName(),
                    cil.isAbstract(),cil.isViewable(), cil.isListType(),
                    cil.isCustom(), cil.isInDesign(),
                    cil.getSmallIcon(), 
                    cil.getColor(), validators));
            }
            return resAsLocal;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets the containment hierarchy of a given class, but upwards (i.e. for Building, it could return 
     * City, Country, Continent)
     * @param className Class name
     * @param recursive Do it recursively or not
     * @return The List of upstream containment hierarchy for a class
     */
    public List<LocalClassMetadataLight> getUpstreamContainmentHierarchy(String className, boolean recursive){
        try{
            List<LocalClassMetadataLight> res = new ArrayList<>();
            for (ClassInfoLight cil : service.getUpstreamContainmentHierarchy(className, recursive, this.session.getSessionId())){
                HashMap<String, Integer> validators = new HashMap<>();
                    for (Validator validator : cil.getValidators())
                        validators.put(validator.getLabel(), validator.getValue());
                    
                    res.add(new LocalClassMetadataLight(cil.getId(),
                                                cil.getClassName(),
                                                cil.getDisplayName(),
                                                cil.getParentClassName(),
                                                cil.isAbstract(),cil.isViewable(), cil.isListType(),
                                                cil.isCustom(), cil.isInDesign(),
                                                cil.getSmallIcon(), cil.getColor(), validators));
            }
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets the special containment hierarchy of a given class, but upwards (i.e. for Building, it could return 
     * City, Country, Continent)
     * @param className Class name
     * @param recursive Do it recursively or not
     * @return The List of upstream special containment hierarchy for a class
     */
    public List<LocalClassMetadataLight> getUpstreamSpecialContainmentHierarchy(String className, boolean recursive) {
        try {
            List<LocalClassMetadataLight> res = new ArrayList<>();
            for (ClassInfoLight cil : service.getUpstreamSpecialContainmentHierarchy(className, recursive, session.getSessionId())) {
                HashMap<String, Integer> validators = new HashMap<>();
                for (Validator validator : cil.getValidators())
                    validators.put(validator.getLabel(), validator.getValue());
                    
                res.add(new LocalClassMetadataLight(cil.getId(),
                    cil.getClassName(),
                    cil.getDisplayName(),
                    cil.getParentClassName(),
                    cil.isAbstract(),cil.isViewable(), cil.isListType(),
                    cil.isCustom(), cil.isInDesign(),
                    cil.getSmallIcon(), cil.getColor(), validators));
            }
            return res;
        } catch (Exception ex) {
            error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * According to the cached light metadata, finds out if a given class if subclass of another
     * @param className Class to be evaluated
     * @param allegedParentClassName Possible super class
     * @return is className subClass of allegedParentClass?
     */
    public boolean isSubclassOf(String className, String allegedParentClassName) {
        if (className == null || allegedParentClassName == null)
            return false;
        
        if (allegedParentClassName.equals("RootObject") || 
            allegedParentClassName.equals("ApplicationObject"))
            return false;
        
        if (allegedParentClassName.equals(className))
            return true;
       
        LocalClassMetadataLight allegedParentClass = cache.getLightMetaForClass(allegedParentClassName);        
                
        if (allegedParentClass == null) {
            List<LocalClassMetadataLight> subclasses = getLightSubclasses(allegedParentClassName, true, true);
            for (LocalClassMetadataLight subclass : subclasses)
                cache.addLightMeta(new LocalClassMetadataLight[]{subclass});
        }
        
        LocalClassMetadataLight currentClass = cache.getLightMetaForClass(className);
        
        if (currentClass == null) // The class name can not be found
            return false;
        
        if (currentClass.getParentName() == null)
            return false;
        
        if (allegedParentClassName.equals(currentClass.getParentName()))
            return true;
        else
            return isSubclassOf(currentClass.getParentName(), allegedParentClassName);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Metadata methods. Click on the + sign on the left to edit the code.">
    /**
     * The result is cached to be used when needed somewhere else, but the whole
     * metadata information is always retrieved directly from the ws
     * @param includeListTypes boolean to indicate if the list should include the list types,
     * such as CustomerType
     * @return an array with all class metadata (the light version)
     */
    public LocalClassMetadataLight[] getAllLightMeta(boolean includeListTypes) {
        try{
            List<ClassInfoLight> metas = service.getAllClassesLight(includeListTypes, this.session.getSessionId());

            LocalClassMetadataLight[] lm = new LocalClassMetadataLight[metas.size()];
            int i = 0;
            for (ClassInfoLight cil : metas){
                HashMap<String, Integer> validators = new HashMap<>();
                    for (Validator validator : cil.getValidators())
                        validators.put(validator.getLabel(), validator.getValue());
                    
                lm[i] = new LocalClassMetadataLight(cil.getId(),
                                                cil.getClassName(),
                                                cil.getDisplayName(),
                                                cil.getParentClassName(),
                                                cil.isAbstract(),cil.isViewable(), cil.isListType(),
                                                cil.isCustom(), cil.isInDesign(),
                                                cil.getSmallIcon(), cil.getColor(), validators);
                i++;
            }

            cache.addLightMeta(lm);
            return lm;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Retrieves complete information about classes. It always take them from the
     * server rather than from the cache, because this methods is suggested to be used
     * for administrative tasks when it's necessary to have the metadata up to date.
     * Anyway, the retrieved information is cached in order to be used when mapping the object's attributes
     * in the property sheets
     * @param includeListTypes boolean to indicate if the list should include the list types,
     * such as CustomerType
     * @return an array with all the class metadata information
     */
    public LocalClassMetadata[] getAllMeta(boolean includeListTypes) {
        try{
            List<ClassInfo> metas = service.getAllClasses(includeListTypes, this.session.getSessionId());
            LocalClassMetadata[] lm = new LocalClassMetadata[metas.size()];
            int i = 0;
            for (ClassInfo ci : metas){
                HashMap<String, Integer> validators = new HashMap<>();
                for (Validator validator : ci.getValidators())
                    validators.put(validator.getLabel(), validator.getValue());
                
                lm[i] = new LocalClassMetadata(ci.getId(),
                                                ci.getClassName(),
                                                ci.getDisplayName(),
                                                ci.getParentClassName(),
                                                ci.isAbstract(),ci.isViewable(), ci.isListType(),
                                                ci.isCustom(), ci.isInDesign(),
                                                ci.getSmallIcon(), ci.getColor(), validators, ci.getIcon(),
                                                ci.getDescription(), ci.getAttributesIds(), 
                                                ci.getAttributesNames().toArray(new String[0]),
                                                ci.getAttributesTypes().toArray(new String[0]),
                                                ci.getAttributesDisplayNames().toArray(new String[0]),
                                                ci.getAttributesMandatories(), ci.getAttributesUniques(),
                                                ci.getAttributesVisibles(), ci.getAttributesDescriptions().toArray(new String[0]));
                i++;
            }
            cache.addMeta(lm); //Refresh the cache
            return lm;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Retrieves the metadata for a given class
     * @param className the classmetadata name
     * @param ignoreCache True if the local cache should be bypassed, false otherwise
     * @return the metadata information
     */
    public LocalClassMetadata getMetaForClass(String className, boolean ignoreCache) {
        try{
            LocalClassMetadata res;
            if (!ignoreCache){
                res = cache.getMetaForClass(className);
                if (res != null)
                    return res;
            }

            ClassInfo cm = service.getClass(className,this.session.getSessionId());
            HashMap<String, Integer> validators = new HashMap<>();
            for (Validator validator : cm.getValidators())
                validators.put(validator.getLabel(), validator.getValue());

            res = new LocalClassMetadata(cm.getId(),
                                            cm.getClassName(),
                                            cm.getDisplayName(),
                                            cm.getParentClassName(),
                                            cm.isAbstract(),cm.isViewable(), cm.isListType(),
                                            cm.isCustom(), cm.isInDesign(),
                                            cm.getSmallIcon(), cm.getColor(), validators, cm.getIcon(),
                                            cm.getDescription(), 
                    cm.getAttributesIds(),                         
                    cm.getAttributesNames().toArray(new String[0]),
                    cm.getAttributesTypes().toArray(new String[0]),
                    cm.getAttributesDisplayNames().toArray(new String[0]),
                    cm.getAttributesMandatories(),
                    cm.getAttributesUniques(),
                    cm.getAttributesVisibles(), 
                    cm.getAttributesDescriptions().toArray(new String[0]));
            cache.addMeta(new LocalClassMetadata[]{res});
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Retrieves the metadata for a given class
     * @param classId classmetadata id
     * @param ignoreCache
     * @return the metadata information
     */
    public LocalClassMetadata getMetaForClass(long classId, boolean ignoreCache) {
        try{
            LocalClassMetadata res;
//            if (!ignoreCache){
//                res = cache.getMetaForClass(classId);
//                if (res != null){
//                    return res;
//                }
//            }
            ClassInfo cm = service.getClassWithId(classId,this.session.getSessionId());
            HashMap<String, Integer> validators = new HashMap<>();
            for (Validator validator : cm.getValidators())
                validators.put(validator.getLabel(), validator.getValue());
            
            res = new LocalClassMetadata(cm.getId(),
                        cm.getClassName(),
                        cm.getDisplayName(),
                        cm.getParentClassName(),
                        cm.isAbstract(),cm.isViewable(), cm.isListType(),
                        cm.isCustom(), cm.isInDesign(),
                        cm.getSmallIcon(), cm.getColor(), validators, cm.getIcon(),
                        cm.getDescription(), cm.getAttributesIds(), 
                        cm.getAttributesNames().toArray(new String[0]),
                        cm.getAttributesTypes().toArray(new String[0]),
                        cm.getAttributesDisplayNames().toArray(new String[0]),
                        cm.getAttributesMandatories(), cm.getAttributesUniques(),
                        cm.getAttributesVisibles(), cm.getAttributesDescriptions().toArray(new String[0]));
            cache.addMeta(new LocalClassMetadata[]{res});
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Retrieves the metadata for a given class
     * @param className the object class
     * @return the metadata information
     */
    /*public LocalClassMetadataLight getLightMetaForClass(String className, boolean ignoreCache){
        try{
            LocalClassMetadataLight res;
            if (!ignoreCache){
                res = cache.getLightMetaForClass(className);
                if (res != null)
                    return res;
            }

            ClassInfo cm = service.getClass(className,this.session.getSessionId());

            res = new LocalClassMetadataLight(cm);
            cache.addLightMeta(new LocalClassMetadataLight[]{res});
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }*/
    
    /**
     * Retrieves the metadata for a given class providing its ide
     * @param className the object class
     * @return the metadata information
     */
    public byte[] getClassHierarchy(boolean showAll) {
        try{
            return service.getClassHierarchy(showAll, session.getSessionId());
        }catch(Exception ex){
            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName()+": "+ ex.getMessage();
            return null;
        }
    }

    public List<LocalClassMetadataLight> getLightSubclasses(String className, boolean includeAbstractSubClasses, boolean includeSelf) {
        try {
            List<ClassInfoLight> subClasses = service.getSubClassesLight(className, includeAbstractSubClasses, includeSelf, session.getSessionId());
            List<LocalClassMetadataLight> res = new ArrayList<>();

            int i = 0;
            for (ClassInfoLight cil : subClasses){
                HashMap<String, Integer> validators = new HashMap<>();
                    for (Validator validator : cil.getValidators())
                        validators.put(validator.getLabel(), validator.getValue());
                    
                res.add(new LocalClassMetadataLight(cil.getId(),
                                cil.getClassName(),
                                cil.getDisplayName(),
                                cil.getParentClassName(),
                                cil.isAbstract(),cil.isViewable(), cil.isListType(),
                                cil.isCustom(), cil.isInDesign(),
                                cil.getSmallIcon(), cil.getColor(), validators));
                i++;
            }
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
     public List<LocalClassMetadataLight> getLightSubclassesNoRecursive(String className, boolean includeAbstractSubClasses, boolean includeSelf) {
        try {
            List<ClassInfoLight> subClasses = service.getSubClassesLightNoRecursive(className, includeAbstractSubClasses, includeSelf, session.getSessionId());
            List<LocalClassMetadataLight> res = new ArrayList<>();

            for (ClassInfoLight cil : subClasses){
                HashMap<String, Integer> validators = new HashMap<>();
                    for (Validator validator : cil.getValidators())
                        validators.put(validator.getLabel(), validator.getValue());
                    
                res.add(new LocalClassMetadataLight(cil.getId(),
                                cil.getClassName(),
                                cil.getDisplayName(),
                                cil.getParentClassName(),
                                cil.isAbstract(),cil.isViewable(), cil.isListType(),
                                cil.isCustom(), cil.isInDesign(),
                                cil.getSmallIcon(), cil.getColor(), validators));
            }
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }// </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="List methods. Click on the + sign on the left to edit the code.">
    /**
     *
     * @param className
     * @return
     */
    public LocalObjectListItem createListTypeItem(String className) {
        try {
            long myObjectId = service.createListTypeItem(className, "", "", this.session.getSessionId());
            return new LocalObjectListItem(myObjectId, className, null);
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Retrieves the list of items corresponding to a list type attribute.
     * @param className attribute class (usually descendant of GenericListType)
     * @param includeNullValue Add an entry for a "null" value to the list to be returned (usually used to display a list type attribute in a property sheet)
     * @param ignoreCache Use cached values or not
     * @return 
     */
    public List<LocalObjectListItem> getList(String className, boolean includeNullValue, boolean ignoreCache){
        try{
            List<LocalObjectListItem> res = null;
            if (!ignoreCache){
                res = cache.getListCached(className);
            }
            if (res == null){
               res = new ArrayList<>();
               res.add(new LocalObjectListItem());

                List<RemoteObjectLight> remoteList = service.getListTypeItems(className,this.session.getSessionId());

                for(RemoteObjectLight entry : remoteList)
                    res.add(new LocalObjectListItem(entry.getOid(),entry.getClassName(),entry.getName()));
                
                //Warning, the null value is always cached
                cache.addListCached(className, res);
            }
            if (includeNullValue){
                return res;
            }
            else
                return res.subList(1, res.size());
            
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }// </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Tasks">
    /**
     * Creates a task
     * @param name Task name
     * @param description Task description
     * @param enabled Is the task enabled?
     * @param script Task script
     * @param parameters Task parameters as pairs param name/param value
     * @param schedule Schedule descriptor
     * @param notificationType Notification type descriptor
     * @return A local representation of the task if the operation was successful, null otherwise
     */
    public LocalTask createTask(String name, String description, boolean enabled, 
            String script, HashMap<String, String> parameters, LocalTaskScheduleDescriptor schedule, LocalTaskNotificationDescriptor notificationType) {
        try {
            TaskScheduleDescriptor atsd = null;
            if (schedule != null) {
                atsd = new TaskScheduleDescriptor();
                atsd.setEveryXMinutes(schedule.getEveryXMinutes());
                atsd.setExecutionType(schedule.getExecutionType());
                atsd.setStartTime(schedule.getStartTime());
            }
            
            TaskNotificationDescriptor tnd = null;
            if (notificationType != null) {
                tnd = new TaskNotificationDescriptor();
                tnd.setEmail(notificationType.getEmail());
                tnd.setNotificationType(notificationType.getNotificationType());
            }
            
            List<StringPair> remoteParameters = null;
            if (parameters !=  null) {
                remoteParameters = new ArrayList<>();
                for (String parameter : parameters.keySet()) {
                    StringPair remoteParameter = new StringPair();
                    remoteParameter.setKey(parameter);
                    remoteParameter.setValue(parameters.get(parameter));
                    remoteParameters.add(remoteParameter);
                }
            }
                
            long taskId = service.createTask(name, description, enabled, script, 
                   remoteParameters, atsd, tnd, session.getSessionId());
            
            return new LocalTask(taskId, name, description, enabled, script, 
                    null, schedule, notificationType, new ArrayList<LocalUserObjectLight>());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Deletes a task
     * @param taskId Task id
     * @return True if it could be deleted, false if not
     */
    public boolean deleteTask(long taskId) {
        try {
            service.deleteTask(taskId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Updates the main properties of a task (name, description, enabled or script)
     * @param taskId Task id
     * @param propertyName Name of the property to be updated
     * @param propertyValue Value of the property to be updated
     * @return True if it could be updated, false if not
     */
    public boolean updateTaskProperties(long taskId, String propertyName, String propertyValue) {
        try {
            service.updateTaskProperties(taskId, propertyName, propertyValue, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }

    /**
     * Updates the schedule of a task
     * @param taskId Task id
     * @param schedule New schedule
     * @return True if the operation was successful, false if not
     */
    public boolean updateTaskSchedule(long taskId, LocalTaskScheduleDescriptor schedule) {
        try {
            TaskScheduleDescriptor remoteSchedule = new TaskScheduleDescriptor();
            remoteSchedule.setStartTime(schedule.getStartTime());
            remoteSchedule.setEveryXMinutes(schedule.getEveryXMinutes());
            remoteSchedule.setExecutionType(schedule.getExecutionType());
            
            service.updateTaskSchedule(taskId, remoteSchedule, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Updates the schedule of a task
     * @param taskId Task id
     * @param notificationType New notification type
     * @return True if the operation was successful, false if not
     */
    public boolean updateTaskNotificationType(long taskId, LocalTaskNotificationDescriptor notificationType) {
        try {
            TaskNotificationDescriptor remoteNotificationType = new TaskNotificationDescriptor();
            remoteNotificationType.setEmail(notificationType.getEmail());
            remoteNotificationType.setNotificationType(notificationType.getNotificationType());
            
            service.updateTaskNotificationType(taskId, remoteNotificationType, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Updates the main properties of a task (name, description, enabled or script)
     * @param taskId Task id
     * @param parameters Set of parameters to be updated. If you want to delete a parameter, the value must be set to null. New entries will be added automatically, while the existing will be updated
     * @return True if it could be updated, false if not
     */
    public boolean updateTaskParameters(long taskId, HashMap<String, String> parameters) {
        try {
            
            List<StringPair> remoteParameters = new ArrayList<>();
            
            for (String parameterName : parameters.keySet()) {
                StringPair remoteParameter = new StringPair();
                remoteParameter.setKey(parameterName);
                remoteParameter.setValue(parameters.get(parameterName));
                remoteParameters.add(remoteParameter);
            }
            
            service.updateTaskParameters(taskId, remoteParameters, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Gets all registered tasks
     * @return A list of tasks
     */
    public List<LocalTask> getTasks() {
        try {
            List<RemoteTask> remoteTasks = service.getTasks(session.getSessionId());
            List<LocalTask> localTasks = new ArrayList<>();
            
            for (RemoteTask remoteTask : remoteTasks) {
                TaskScheduleDescriptor schedule = remoteTask.getSchedule();
                LocalTaskScheduleDescriptor ltsd = new LocalTaskScheduleDescriptor(schedule.getStartTime(), schedule.getEveryXMinutes(), schedule.getExecutionType());

                TaskNotificationDescriptor notificationType = remoteTask.getNotificationType();
                LocalTaskNotificationDescriptor tnd = new LocalTaskNotificationDescriptor(notificationType.getEmail(), notificationType.getNotificationType());

                HashMap<String, String> remoteParameters = new HashMap<>();
                
                for (StringPair remoteParameter : remoteTask.getParameters())
                    remoteParameters.put(remoteParameter.getKey(), remoteParameter.getValue());
                
                List<LocalUserObjectLight> users = new ArrayList<>();
                
                for (UserInfoLight user : remoteTask.getUsers())
                    users.add(new LocalUserObjectLight(user.getId(), user.getUserName(),
                                        user.getFirstName(), user.getLastName(), user.isEnabled(), user.getType()));
                
                localTasks.add(new LocalTask(remoteTask.getId(), remoteTask.getName(), 
                        remoteTask.getDescription(), remoteTask.isEnabled(), remoteTask.getScript(), 
                        remoteParameters, ltsd, tnd, users));
            }
            
            return localTasks;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets the subscribers of a particular task
     * @param taskId TaskId
     * @return A list of users subscribed to the task
     */
    public List<LocalUserObjectLight> getSubscribersForTask(long taskId) {
        try {
            List<UserInfoLight> remoteSubscribers = service.getSubscribersForTask(taskId, session.getSessionId());
            List<LocalUserObjectLight> subscribers = new ArrayList<>();
            
            for (UserInfoLight remoteSubscriber : remoteSubscribers)
                subscribers.add(new LocalUserObjectLight(remoteSubscriber.getId(), remoteSubscriber.getUserName(),
                                        remoteSubscriber.getFirstName(), remoteSubscriber.getLastName(), remoteSubscriber.isEnabled(), remoteSubscriber.getType()));
            
            return subscribers;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Executes a task on demand
     * @param taskId Id of the task
     * @return A local representation of the result
     */
    public LocalTaskResult executeTask(long taskId){
        try {
            RemoteTaskResult remoteTaskResult  = service.executeTask(taskId, session.getSessionId());
            LocalTaskResult taskResult = new LocalTaskResult();
            
            for (RemoteResultMessage remoteResulMessage :remoteTaskResult.getMessages())
                taskResult.getMessages().add(new LocalTaskResultMessage(remoteResulMessage.getMessageType(), remoteResulMessage.getMessage()));
            
            return taskResult;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Subscribes a user to a task
     * @param userId User id
     * @param taskId Task id
     * @return True if the operation was successful. False if not 
     */
    public boolean subscribeUserToTask(long userId, long taskId) {
        try {
            service.subscribeUserToTask(userId, taskId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Unsubscribes a user from a task
     * @param userId User id
     * @param taskId Task id
     * @return True if the operation was successful. False if not
     */
    public boolean unsubscribeUserFromTask(long userId, long taskId) {
        try {
            service.unsubscribeUserFromTask(userId, taskId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Object methods. Click on the + sign on the left to edit the code.">
    /**
     * 
     * @param objectClass
     * @param parentClass
     * @param parentOid
     * @param attributes
     * @param template
     * @return 
     */
    public LocalObjectLight createObject(String objectClass, String parentClass, 
            long parentOid, HashMap<String, Object> attributes, long template)
    {
        try {
            List<String> attributeNames = new ArrayList<>();
            List<String> attributeValues = new ArrayList<>();

            for (String key : attributes.keySet()){
                String value;
                attributeNames.add(key);
                
                Object theValue = attributes.get(key);
                if (theValue instanceof LocalObjectListItem)
                    value = String.valueOf(((LocalObjectListItem)theValue).getId());
                else {
                    if (theValue instanceof Date)
                        value = String.valueOf(((Date)theValue).getTime());
                    else
                        value = theValue.toString();
                }
                attributeValues.add(value);
            }
            long objectId  = service.createObject(objectClass, parentClass, 
                    parentOid, attributeNames,
                    attributeValues,
                    template, this.session.getSessionId());
            return new LocalObjectLight(objectId, null, objectClass);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight createSpecialObject(String className, String parentClassName, 
            long parentOid, HashMap<String, Object> attributes, long templateId) {
        try{
            List<String> attributeNames = new ArrayList<>();
            List<String> attributeValues = new ArrayList<>();

            for (String key : attributes.keySet()){
                String value;
                attributeNames.add(key);
                
                Object theValue = attributes.get(key);
                if (theValue instanceof LocalObjectListItem)
                    value = String.valueOf(((LocalObjectListItem)theValue).getId());
                else {
                    if (theValue instanceof Date)
                        value = String.valueOf(((Date)theValue).getTime());
                    else
                        value = theValue.toString();
                }
                attributeValues.add(value);
            }
            long objectId  = service.createSpecialObject(className,parentClassName, parentOid, 
                    attributeNames, attributeValues,templateId,this.session.getSessionId());
            return new LocalObjectLight(objectId, null, className);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Adds a set of possible children
     * @param parentClassId Parent class id
     * @param possibleChildren The ids of possible children
     * @return True if the possible children was added
     */
    public boolean addPossibleChildren(long parentClassId, long[] possibleChildren){
        try{
            List<Long> pChildren = new ArrayList<Long>();
            for (long pChild : possibleChildren){
                pChildren.add(pChild);
            }
            service.addPossibleChildrenForClassWithId(parentClassId, pChildren,this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Adds a set of possible special children
     * @param parentClassId The parent class id
     * @param possibleSpecialChildren The ids of possible special children
     * @return True if the possible special children was added
     */
    public boolean addPossibleSpecialChildren(long parentClassId, long[] possibleSpecialChildren) {
        try {
            List<Long> psChildren = new ArrayList<>();
            for (long psChild : possibleSpecialChildren) {
                psChildren.add(psChild);
            }
            service.addPossibleSpecialChildrenWithId(parentClassId, psChildren, session.getSessionId());
            return true;
        } catch (Exception ex) {
            error = ex.getMessage();
            return false;
        }
    }

    /**
     * Removes possible children from the given class container hierarchy
     * @param parentClassId for the parent class
     * @param childrenToBeDeleted List if ids of the classes to be removed as possible children
     * @return Success or failure
     */
    public boolean removePossibleChildren(long parentClassId, long[] childrenToBeDeleted){
        try{
            List<Long> pChildren = new ArrayList<>();
            for (long pChild : childrenToBeDeleted){
                pChildren.add(pChild);
            }
            service.removePossibleChildrenForClassWithId(parentClassId, pChildren,this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Removes a set of possible special children
     * @param parentClassId Parent class id
     * @param specialChildrenToBeDeleted 
     * @return Success or failure
     */
    public boolean removePossibleSpecialChildren(long parentClassId, long [] specialChildrenToBeDeleted) {
        try {
            List<Long> psChildren = new ArrayList<>();
            for (long psChild : specialChildrenToBeDeleted)
                psChildren.add(psChild);
            service.removePossibleSpecialChildren(parentClassId, psChildren, session.getSessionId());
            return true;
        } catch (Exception ex) {
            error = ex.getMessage();
            return false;
        }
    }

    /**
     * Deletes a single object
     * @param className Object class
     * @param oid Object Id
     * @return  True if the operation was successful, an exception if not
     */
    public boolean deleteObject(String className, long oid){
        try {
            service.deleteObject(className, oid, false, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Deletes a set of objects
     * @param classNames Object classes
     * @param oids object ids
     * @return Success or failure
     */
    public boolean deleteObjects(List<String> classNames, List<Long> oids){
        try {
            service.deleteObjects(classNames, oids, false, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }

    public boolean moveObjectsToPool(String targetClass, long targetOid, LocalObjectLight[] objects) {

        try{
            List<Long> objectOids = new ArrayList<>();
            List<String> objectClasses = new ArrayList<>();

            for (LocalObjectLight lol : objects){
                objectOids.add(lol.getOid());
                objectClasses.add(lol.getClassName());
            }
            service.moveObjectsToPool(targetClass, targetOid, objectClasses, objectOids, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean moveObjects(String targetClass, long targetOid, LocalObjectLight[] objects) {
        try{
            List<Long> objectOids = new ArrayList<>();
            List<String> objectClasses = new ArrayList<>();

            for (LocalObjectLight lol : objects){
                objectOids.add(lol.getOid());
                objectClasses.add(lol.getClassName());
            }
            service.moveObjects(targetClass, targetOid, objectClasses, objectOids,this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }

    public LocalObjectLight[] copyObjects(String targetClass, long targetOid, LocalObjectLight[] objects){
        try{
            List<Long> objectOids = new ArrayList<>();
            List<String> objectClasses = new ArrayList<>();

            for (LocalObjectLight lol : objects){
                objectOids.add(lol.getOid());
                objectClasses.add(lol.getClassName());
            }

            //Let's do the copy recursive by default
            List<Long> objs = service.copyObjects(targetClass, targetOid, objectClasses, objectOids, true, this.session.getSessionId());

            LocalObjectLight[] res = new LocalObjectLight[objs.size()];
            for (int i = 0; i < res.length ; i++){
                res[i] = new LocalObjectLight(objs.get(i), objects[i].getName(), objects[i].getClassName());
                i++;
            }
            return res;

        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalAttributeMetadata[] getMandatoryAttributesInClass(String className){
        try {
            List<AttributeInfo> mandatoryObjectAttributesInfo = service.getMandatoryAttributesInClass(className, session.getSessionId());
            LocalAttributeMetadata[] mandatoryObjectAttributes = new LocalAttributeMetadata[mandatoryObjectAttributesInfo.size()];
            int i = 0;
            for (AttributeInfo mandatoryObjectAttributeInfo :  mandatoryObjectAttributesInfo) {
                mandatoryObjectAttributes[i] = new LocalAttributeMetadata(mandatoryObjectAttributeInfo.getId(),
                        mandatoryObjectAttributeInfo.getName(), mandatoryObjectAttributeInfo.getType(), 
                        mandatoryObjectAttributeInfo.getDisplayName(), mandatoryObjectAttributeInfo.isVisible(), 
                        mandatoryObjectAttributeInfo.isMandatory(), mandatoryObjectAttributeInfo.isUnique(), 
                        mandatoryObjectAttributeInfo.getDescription());
                i++;
            }
            return mandatoryObjectAttributes;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public void objectHasValuesInMandatoryAttributes(String className, long objId){
        try{
            service.objectHasValuesInMandatoryAttributes(className, objId, session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
        }
    }
    
    /**
     * Creates multiple objects using a given name pattern
     * @param className The class name for the new objects
     * @param parentClassName The parent class name for the new objects
     * @param parentOid The object id of the parent
     * @param numberOfObjects Number of objects to be created
     * @param namePattern A pattern to create the names for the new objects
     * @return A list of new objects or null if occur an error
     */
    public List<LocalObjectLight> createBulkObjects(String className, String parentClassName, long parentOid, int numberOfObjects, String namePattern) {
        try {
            List<Long> ids = service.createBulkObjects(className, parentClassName, parentOid, numberOfObjects, namePattern, session.getSessionId());
            
            List<LocalObjectLight> newObjects = new ArrayList<>();
            
            for (Long id : ids) {
                newObjects.add(getObjectInfoLight(className, id));
            }
            
            return newObjects;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Creates multiple special objects using a given name pattern
     * @param className The class name for the new special objects
     * @param parentClassName The parent class name for the new special objects
     * @param parentId The object id of the parent
     * @param numberOfSpecialObjects Number of special objects to be created
     * @param namePattern A pattern to create the names for the new special objects
     * @return A list of new special objects or null if occur an error
     */
    public List<LocalObjectLight> createBulkSpecialObjects(String className, String parentClassName, long parentId, int numberOfSpecialObjects, String namePattern) {
        try {
            List<Long> ids = service.createBulkSpecialObjects(className, parentClassName, parentId, numberOfSpecialObjects, namePattern, session.getSessionId());
            
            List<LocalObjectLight> newSpecialObjects = new ArrayList<>();
            
            for (Long id : ids) {
                newSpecialObjects.add(getObjectInfoLight(className, id));
            }
            
            return newSpecialObjects;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
        
    public boolean connectMirrorPort (String aObjectClass, long aObjectId, String bObjectClass, long bObjectId) {
        try{
            service.connectMirrorPort(aObjectClass, aObjectId, bObjectClass, bObjectId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean releaseMirrorPort (String objectClass, long objectId) {
        try {
            service.releaseMirrorPort (objectClass, objectId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Creates a physical link (cable, fiber optics, mw link) or container (pipe, conduit, ditch)
     * @param endpointAClass source object class name
     * @param endpointAId source object oid
     * @param endpointBClass target object class name
     * @param endpointBId target object oid
     * @param parentClass connection's parent class
     * @param parentId connection's parent id
     * @param name Name of the new connection. Leave empty if you want to use the name in the template
     * @param connectionClass Class for the corresponding connection to be created
     * @param templateId Id of the template for the connectionClass. Use -1 to create a connection without template
     * @return A local object light representing the new connection
     */
    public LocalObjectLight createPhysicalConnection(String endpointAClass, long endpointAId,
            String endpointBClass, long endpointBId, String parentClass, long parentId, String name, String connectionClass, long templateId) {
        try {
            long myObjectId = service.createPhysicalConnection(endpointAClass, endpointAId,
                    endpointBClass, endpointBId, parentClass, parentId, name, connectionClass, templateId, this.session.getSessionId());
            return new LocalObjectLight(myObjectId, name, connectionClass);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight[] getConnectionEndpoints(String connectionClass, long connectionId) {
        try{
            List<RemoteObjectLight> endpoints = service.getPhysicalConnectionEndpoints(connectionClass, connectionId, session.getSessionId());
            LocalObjectLight[] res = new LocalObjectLight[]{endpoints.get(0) == null ? 
                    null : new LocalObjectLight(endpoints.get(0).getOid(), endpoints.get(0).getName(), endpoints.get(0).getClassName()),
                    endpoints.get(1) == null ? 
                    null : new LocalObjectLight(endpoints.get(1).getOid(), endpoints.get(1).getName(), endpoints.get(1).getClassName())};
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Returns the structure of a logical connection. The current implementation is quite simple and the return object 
     * simply provides the endpoints and the next ports connected to such endpoints using a physical connection
     * @param linkClass The class of the connection to be evaluated
     * @param linkId The id of the connection to be evaluated
     * @return An object with the details of the connection and the physical resources associated to it. Null in case of error
     */
    public LocalLogicalConnectionDetails getLogicalLinkDetails(String linkClass, long linkId) { 
        try {
            return new LocalLogicalConnectionDetails(service.getLogicalLinkDetails(linkClass, linkId, session.getSessionId()));
        }catch(Exception ex) {
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight[] getPhysicalPath(String objectClass, long objectId) {
        try{
            List<RemoteObjectLight> trace = service.getPhysicalPath(objectClass, objectId, session.getSessionId());
            LocalObjectLight[] res = new LocalObjectLight[trace.size()];
            int i = 0;
            for (RemoteObjectLight element : trace){
                res[i] = new LocalObjectLight(element.getOid(), element.getName(), element.getClassName());
                i++;
            }
            
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public boolean deletePhysicalConnection(String objectClass, long objectId) {
        try {
            service.deletePhysicalConnection(objectClass, objectId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    public boolean connectPhysicalLinks(List<String> sideAClassNames, List<Long> sideAIds, 
                List<String> linksClassNames, List<Long> linksIds, List<String> sideBClassNames, 
                List<Long> sideBIds) {
        try {            
            service.connectPhysicalLinks(sideAClassNames, sideAIds, linksClassNames, linksIds, sideBClassNames, sideBIds, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    public boolean connectPhysicalContainers(List<String> sideAClassNames, List<Long> sideAIds, 
                List<String> containersClassNames, List<Long> containersIds, List<String> sideBClassNames, 
                List<Long> sideBIds) {
        try {
            service.connectPhysicalContainers(sideAClassNames, sideAIds, containersClassNames, containersIds, sideBClassNames, sideBIds, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    /**
     * Disconnects a side or both sides of a physical connection (a link or a container)
     * @param connectionClass Class of the connection to be edited
     * @param connectionId Id of the connection to be edited
     * @param sideToDisconnect Side to disconnect. Use 1 to disconnect only the side a, 2 to disconnect only side b and 3 to disconnect both sides at once
     * @return True if the operation was successful, false otherwise. Retrieve the details of the error using the getError method
     */
    public boolean disconnectPhysicalConnection(String connectionClass, long connectionId, int sideToDisconnect) {
        try {
            service.disconnectPhysicalConnection(connectionClass, connectionId, sideToDisconnect, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    //Service Manager
    public boolean associateObjectsToService(List<String> classNames, List<Long> objectIds, String serviceClass, long serviceId){
        try{
            List<String> objectsClassList = new ArrayList<>();
            List<Long> objectsIdList = new ArrayList<>();
            objectsClassList.addAll(classNames);
            objectsIdList.addAll(objectIds);
            service.associateObjectsToService(objectsClassList, objectsIdList, serviceClass, serviceId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    public boolean releaseObjectFromService(String serviceClass, long serviceId, long targetId){
        try{
            service.releaseObjectFromService(serviceClass, serviceId, targetId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    public List<LocalObjectLight> getServiceResources(String serviceClass, long serviceId){
        try{
            List <RemoteObjectLight> instances = service.getServiceResources(serviceClass, serviceId, this.session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : instances)
                res.add(new LocalObjectLight(rol.getOid(), rol.getName(), rol.getClassName()));

            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    //End Service Manager
    
    public List<LocalObjectLight> getObjectSpecialChildren(String objectClass, long objectId) {
        try {
            List<RemoteObjectLight> specialChildren = service.getObjectSpecialChildren (
                    objectClass, objectId, session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();
            
            for (RemoteObjectLight rol : specialChildren)
                res.add(new LocalObjectLight(rol.getOid(), rol.getName(), rol.getClassName()));

            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Query methods. Click on the + sign on the left to edit the code.">
    /**
     * Call to remote executeQuery method
     * @param query Query to be executed in an execution (code)-friendly format
     * @return an array with results
     */
    public LocalResultRecord[] executeQuery(LocalTransientQuery query){
        try{
            TransientQuery remoteQuery = LocalTransientQuery.toTransientQuery(query);
            List<ResultRecord> myResult = service.executeQuery(remoteQuery,session.getSessionId());
            LocalResultRecord[] res = new LocalResultRecord[myResult.size()];
            //The first record is used to store the table headers
            res[0] = new LocalResultRecord(null, myResult.get(0).getExtraColumns());
            for (int i = 1; i < res.length ; i++){
                HashMap<String, Integer> validators = new HashMap<String, Integer>();
                for (Validator validator : myResult.get(i).getObject().getValidators())
                    validators.put(validator.getLabel(), validator.getValue());
                
                LocalObjectLight anObjectLight = new LocalObjectLight(myResult.get(i).getObject().getClassName(),
                        myResult.get(i).getObject().getName(), myResult.get(i).getObject().getOid(), validators);
                
                res[i] = new LocalResultRecord(
                        anObjectLight, myResult.get(i).getExtraColumns());
            }
            return res;
        }catch(Exception ex){
            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName()+": "+ ex.getMessage();
            return null;
        }
    }

    /**
     * Call to remote createQuery method
     * @param queryName
     * @param queryStructure
     * @param description
     * @return success or failure
     */
    public long createQuery(String queryName, byte[] queryStructure, String description, boolean isPublic){
        try{
            return service.createQuery(queryName, isPublic ? -1 : session.getUserId(), queryStructure, description, session.getSessionId());
            
        }catch(Exception ex){
            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName()+": "+ ex.getMessage();
            return -1;
        }
    }

    /**
     * Call to remote saveQuery method
     * @param query query to be saved in a store-friendly format
     * @return
     */
    public boolean saveQuery(LocalQuery query){
        try{
            service.saveQuery(query.getId(),query.getName(),
                    query.isPublic() ? -1 : session.getUserId(),
                    query.getStructure(),
                    query.getDescription(),
                    session.getSessionId());
            return  true;
        }catch(Exception ex){
            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName()+": "+ ex.getMessage();
            return false;
        }
    }
    
    /**
     * Call to remote deleteQuery method
     * @param queryId query to be deleted
     * @return success or failure
     */
    public boolean deleteQuery(long queryId){
        try{
            service.deleteQuery(queryId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName()+": "+ ex.getMessage();
            return false;
        }
    }

    /**
     * Call to remote getQueries method
     * @param showAll True to show all queries (public and owned by this user) False to show only the queries
     * owned by this user
     * @return An array with the list of available queries
     */
    public LocalQueryLight[] getQueries(boolean showAll){
        try{
            List<RemoteQueryLight> queries = service.getQueries(showAll, session.getSessionId());
            LocalQueryLight[] res = new LocalQueryLight[queries.size()];
            int i = 0;
            for (RemoteQueryLight query : queries){
                res[i] = new LocalQueryLight(query);
                i++;
            }
            return res;
        }catch(Exception ex){
            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName()+": "+ ex.getMessage();
            return null;
        }
    }

    /**
     * Call to remote getQueries method
     * @param queryId query to be retrieved
     * @return The query
     */
    public LocalQuery getQuery(long queryId){
        try{
            return new LocalQuery(service.getQuery(queryId, session.getSessionId()));
        }catch(Exception ex){
            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName()+": "+ ex.getMessage();
            return null;
        }
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Misc methods. Click on the + sign on the left to edit the code.">

    /**
     * Refreshes all existing objects, according to the flags provided
     * @param refreshMeta
     * @param refreshLightMeta
     * @param refreshList
     * @param refreshPossibleChildren
     * @param refreshPossibleSpecialChildren
     */
    public void refreshCache(boolean refreshMeta, boolean refreshLightMeta,
            boolean refreshList, boolean refreshPossibleChildren, boolean refreshPossibleSpecialChildren){
        try {
            if (refreshMeta){
                for (LocalClassMetadata lcm : cache.getMetadataIndex()){
                    ClassInfo cm = service.getClass(lcm.getClassName(),this.session.getSessionId());
                    HashMap<String, Integer> validators = new HashMap<>();
                    for (Validator validator : cm.getValidators())
                        validators.put(validator.getLabel(), validator.getValue());
            
                    LocalClassMetadata myLocal = new LocalClassMetadata(cm.getId(),
                            cm.getClassName(),
                            cm.getDisplayName(),
                            cm.getParentClassName(),
                            cm.isAbstract(),cm.isViewable(), cm.isListType(),
                            cm.isCustom(), cm.isInDesign(),
                            cm.getSmallIcon(), cm.getColor(), validators, cm.getIcon(),
                            cm.getDescription(), cm.getAttributesIds(), 
                            cm.getAttributesNames().toArray(new String[0]),
                            cm.getAttributesTypes().toArray(new String[0]),
                            cm.getAttributesDisplayNames().toArray(new String[0]),
                            cm.getAttributesMandatories(), cm.getAttributesUniques(),
                            cm.getAttributesVisibles(), cm.getAttributesDescriptions().toArray(new String[0]));
                    
                    cache.addMeta(new LocalClassMetadata[]{myLocal});
                }
            }
            if (refreshLightMeta) {
                getAllLightMeta(true);
            }

            if (refreshList){
                HashMap<String, List<LocalObjectListItem>> myLocalList = cache.getAllListTypes();
                for (String key : myLocalList.keySet()){
                    myLocalList.remove(key);
                    getList(key,false,true);
                }
            }
            if (refreshPossibleChildren) {
                HashMap<String, List<LocalClassMetadataLight>> myLocalPossibleChildren
                        = cache.getAllPossibleChildren();
                for (String key : myLocalPossibleChildren.keySet()){
                    myLocalPossibleChildren.remove(key);
                    getPossibleChildren(key, true);
                }
                cache.resetPossibleChildrenCached();
            }
            if (refreshPossibleSpecialChildren) {
                HashMap<String, List<LocalClassMetadataLight>> myLocalPossibleSpecialChildren
                        = cache.getAllPossibleSpecialChildren();
                for (String key : myLocalPossibleSpecialChildren.keySet()){
                    myLocalPossibleSpecialChildren.remove(key);
                    getPossibleSpecialChildren(key, true);
                }
                cache.resetPossibleSpecialChildrenCached();
            }
        }catch(Exception ex){
            this.error = ex.getMessage();
        }
    }
    
    public boolean setAttributeProperties(long classId, String className, 
            long attributeId, String name, String displayName, String type,
            String description, Boolean administrative, Boolean mandatory, 
            Boolean noCopy, Boolean readOnly, Boolean unique,  Boolean visible)
    {
        try{
            service.setAttributePropertiesForClassWithId(classId, attributeId, 
                    name, displayName, description, type, administrative, 
                    mandatory, noCopy, readOnly, unique, visible, 
                    this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
        
    public long createClassMetadata(String className, String displayName, String description, String parentClassName, boolean custom, boolean countable, int color, boolean _abstract, boolean inDesign){
        try {
            return service.createClass(className, displayName, description, _abstract, custom, countable, inDesign, parentClassName, null, null, color, this.session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return -1;
        }
    }
    
    public boolean deleteClassMetadata(long classId){
        try {
            service.deleteClassWithId(classId, this.session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
        return true;
    }
    
    public boolean createAttribute(long classId, String name, String displayName, 
                                String description, String type, boolean administrative, 
                                boolean readOnly, boolean visible, boolean noCopy, 
                                boolean unique, boolean mandatory){
        try{
            service.createAttributeForClassWithId(classId, name, displayName, 
                    type, description, administrative, visible, readOnly, 
                    noCopy, unique, mandatory, this.session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
        return true;
    }
    
    public boolean createAttribute(String className, String name, String displayName, 
                                String description, String type, boolean administrative,
                                boolean readOnly, boolean visible, boolean noCopy, 
                                boolean unique, boolean mandatory){
        try{
            service.createAttribute(className, name, displayName, type, 
                    description, administrative, visible, readOnly, noCopy, 
                    unique, mandatory, this.session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
        return true;
    }
    
    public boolean deleteAttribute(long classId, String attributeName){
        try{
            service.deleteAttributeForClassWithId(classId, attributeName, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean setClassMetadataProperties(long classId, String className, 
                                                 String displayName, String description, 
                                                 byte[] smallIcon, byte[] icon, int color,
                                                 Boolean _abstract,Boolean inDesign, Boolean countable, Boolean custom){
        try{
            service.setClassProperties(classId, className, displayName, description, smallIcon, icon, color,
                    _abstract, inDesign, custom, countable, this.session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
        return true;
    }
    
    /**
     * Retrieves the list types
     * @return an array with all possible instanceable list types
     */
    public List<LocalClassMetadataLight> getInstanceableListTypes() {
        try{
            List<ClassInfoLight> listTypes;
            listTypes = service.getInstanceableListTypes(this.session.getSessionId());

            List<LocalClassMetadataLight> res = new ArrayList<>();
            for (ClassInfoLight cil : listTypes){
                HashMap<String, Integer> validators = new HashMap<>();
                    for (Validator validator : cil.getValidators())
                        validators.put(validator.getLabel(), validator.getValue());
                    
                res.add(new LocalClassMetadataLight(cil.getId(),
                                cil.getClassName(),
                                cil.getDisplayName(),
                                cil.getParentClassName(),
                                cil.isAbstract(),cil.isViewable(), cil.isListType(),
                                cil.isCustom(), cil.isInDesign(), 
                                cil.getSmallIcon(), cil.getColor(), validators));
            }
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Deletes a list type item
     * @param className The class the object is instance of
     * @param oid The object's id
     * @param force should it release all relationships to (or from) this object?
     * @return success or failure
     */
    public boolean deleteListTypeItem(String className, long oid, boolean force){
        try{
            service.deleteListTypeItem(className, oid, force, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }// </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="User management methods. Click on the + sign on the left to edit the code.">
    /**
     * Retrieves the user list
     * @return An array of LocalUserObject
     */
    public List<LocalUserObject> getUsers() {
        try {
            List<UserInfo> users = service.getUsers(this.session.getSessionId());
            List<LocalUserObject> localUsers = new ArrayList<>();
            
            for (UserInfo user : users) {
                List<LocalPrivilege> localPrivileges = new ArrayList<>();
                for (PrivilegeInfo remotePrivilege : user.getPrivileges())
                    localPrivileges.add(new LocalPrivilege(remotePrivilege.getFeatureToken(), remotePrivilege.getAccessLevel()));
                localUsers.add(new LocalUserObject(user.getId(), user.getUserName(),
                                        user.getFirstName(), user.getLastName(), user.isEnabled(), user.getType(), localPrivileges));
            }
            return localUsers;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Retrieves the users in a group
     * @param groupId The id of the group
     * @return The list of users in the requested group or null if something wrong happened
     */
    public List<LocalUserObject> getUsersInGroup(long groupId) {
        try {
            List<UserInfo> remoteUsers = service.getUsersInGroup(groupId, session.getSessionId());
            List<LocalUserObject> localUsers = new ArrayList<>();
            
            for (UserInfo remoteUser : remoteUsers) {
                List<LocalPrivilege> localPrivileges = new ArrayList<>();
                for (PrivilegeInfo remotePrivilege : remoteUser.getPrivileges())
                    localPrivileges.add(new LocalPrivilege(remotePrivilege.getFeatureToken(), remotePrivilege.getAccessLevel()));
                
                localUsers.add(new LocalUserObject(remoteUser.getId(), remoteUser.getUserName(),
                                        remoteUser.getFirstName(), remoteUser.getLastName(), 
                                        remoteUser.isEnabled(), remoteUser.getType(), localPrivileges));
            }
            return localUsers;
        } catch(Exception e){
            this.error = e.getMessage();
            return null;
        }
    }
    
    /**
     * Retrieves the group list
     * @return An array of LocalUserObject
     */
    public List<LocalUserGroupObject> getGroups() {
        try{
            List<GroupInfo> groups = service.getGroups(this.session.getSessionId());
            List<LocalUserGroupObject> localGroups = new ArrayList<>();

            for (GroupInfo group : groups)
                localGroups.add(new LocalUserGroupObject(group));

            return localGroups;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Creates a user
     * @param username Username
     * @param firstName User's first name (optional)
     * @param lastName User's last name (optional)
     * @param password Password
     * @param enabled Will this user be enabled by default?
     * @param type User type. See LocalUserObjectLight.USER_TYPE* for possible values
     * @param defaultGroupId Id of the default group this user will be associated to. Users <b>always</b> belong to at least one group. Other groups can be added later.
     * @return The newly created user
     */
    public LocalUserObject createUser(String username, String firstName, String lastName, 
            String password, boolean enabled, int type, long defaultGroupId) {
        try{
            long newUserId = service.createUser(username, password, firstName, lastName, 
                    true, type, null, defaultGroupId, this.session.getSessionId());
            
            UserInfo newUser = new UserInfo();
            newUser.setId(newUserId);
            newUser.setUserName(username);
            return new LocalUserObject(newUserId, username, firstName, lastName, enabled, type, null);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Sets the properties of a given user using the id to search for it
     * @param oid User id
     * @param username New user's name. Use null to leave it unchanged.
     * @param password New user's password. Use null to leave it unchanged
     * @param firstName New user's first name. Use null to leave it unchanged
     * @param lastName New user's last name. Use null to leave it unchanged
     * @param enabled 0 for false, 1 for true, -1 to leave it unchanged
     * @param type User type. See UserProfile.USER_TYPE* for possible values. Use -1 to leave it unchanged
     * @return ServerSideException Thrown if the username is null or empty or the username already exists or if the user could not be found
     */
    public boolean setUserProperties(long oid, String username, String password, 
            String firstName, String lastName, int enabled, int type) {
        try {            
            service.setUserProperties(oid, username, firstName, lastName, password, 
                    enabled, type, this.session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
        return true;
    }
    
    /**
     * Adds a user to a group
     * @param userId The id of the user to be added to the group
     * @param groupId Id of the group which the user will be added to
     * @return Success or failure
     */
    public boolean addUserToGroup(long userId, long groupId) {
        try {
            service.addUserToGroup(userId, groupId, session.getSessionId());
            return true;
        } catch(Exception e){
            this.error = e.getMessage();
            return false;
        }
    }
    
    /**
     * Removes a user from a group
     * @param userId The id of the user to be added to the group
     * @param groupId Id of the group which the user will be added to
     * @return Success or failure
     */
    public boolean removeUserFromGroup(long userId, long groupId) {
        try {
            service.removeUserFromGroup(userId, groupId, session.getSessionId());
            return true;
        } catch(Exception e){
            this.error = e.getMessage();
            return false;
        }
    }
    
    /**
     * Sets a privilege to a user. If the privilege does not exist already, one is created, otherwise, the access level is updated
     * @param userId The user Id
     * @param featureToken The feature token. See class Privilege for details. Note that this token must match to the one expected by the client application. That's the only way the correct features will be enabled.
     * @param accessLevel The feature token. See class Privilege.ACCESS_LEVEL* for details. 
     * @return Success or failure 
     */
    public boolean setPrivilegeToUser(long userId,  String featureToken, int accessLevel) {
        try {
            service.setPrivilegeToUser(userId, featureToken, accessLevel, session.getSessionId());
            return true;
        } catch(Exception e){
            this.error = e.getMessage();
            return false;
        }
    }
    
    /**
     * Adds a privilege to a group. If the privilege does not exist already, one is created, otherwise, the access level is updated
     * @param groupId The user Id
     * @param featureToken The feature token. See class Privilege for details. Note that this token must match to the one expected by the client application. That's the only way the correct features will be enabled.
     * @param accessLevel The feature token. See class Privilege.ACCESS_LEVEL* for details. 
     * @return Success of failure
     */
    public boolean setPrivilegeToGroup(long groupId, String featureToken, int accessLevel) {
        try {
            service.setPrivilegeToGroup(groupId, featureToken, accessLevel, session.getSessionId());
            return true;
        } catch(Exception e){
            this.error = e.getMessage();
            return false;
        }
    }
    
    /**
     * Removes a privilege from a user
     * @param userId Id of the user
     * @param featureToken The feature token. See class Privilege for details. 
     * @return Success or failure
     */
    public boolean removePrivilegeFromUser(long userId, String featureToken) {
        try {
            service.removePrivilegeFromUser(userId, featureToken, session.getSessionId());
            return true;
        } catch(Exception e){
            this.error = e.getMessage();
            return false;
        }
    }
    
    /**
     * Removes a privilege from a user
     * @param groupId Id of the group
     * @param featureToken The feature token. See class Privilege for details. 
     * @return Sucess or failure
     */
    public boolean removePrivilegeFromGroup(long groupId, String featureToken) {
        try {
            service.removePrivilegeFromGroup(groupId, featureToken, session.getSessionId());
            return true;
        } catch(Exception e){
            this.error = e.getMessage();
            return false;
        }
    }

    /**
     * Set group attributes (group membership is managed using other methods)
     * @param groupId Group id
     * @param groupName Group name (null if unchanged)
     * @param description Group description (null if unchanged)
     * @return success or failure
     */
    public boolean setGroupProperties(long groupId, String groupName, String description) {
        try{
            service.setGroupProperties(groupId, groupName, description, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
        
    }

    /**
     * Creates a new group
     * @return The newly created group or null in case of error
     */
    public LocalUserGroupObject createGroup(String groupName, String groupDescription){
        try {
            long newGroupId = service.createGroup(groupName, groupDescription, null, this.session.getSessionId()); //By default, the group is empty
            GroupInfo newGroup = new GroupInfo();
            newGroup.setId(newGroupId);
            newGroup.setName(groupName);
            newGroup.setDescription(groupDescription);
            return new LocalUserGroupObject(newGroup);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Removes a list of users
     * @param oids oids for the users to be deleted
     * @return success or failure
     */
    public boolean deleteUsers(List<Long> oids){
        try {
            service.deleteUsers(oids, session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
        return true;
    }

    /**
     * Removes a list of groups
     * @param oids oids for the users to be deleted
     * @return success or failure
     */
    public boolean deleteGroups(List<Long> oids) {
        try {
            service.deleteGroups(oids, session.getSessionId());
        } catch(Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
        return true;
    }// </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Views methods. Click on the + sign on the left to edit the code.">

    /**
     * Get a view related to an object, such as the default, rack or equipment views
     * @param oid object's id
     * @param objectClass object's class
     * @param viewId view id
     * @return The associated view (there should be only one of each type). Null if there's none yet
     */
    public LocalObjectView getObjectRelatedView(long oid, String objectClass, long viewId){
        try{
            ViewInfo view = service.getObjectRelatedView(oid, objectClass, viewId, session.getSessionId());
            return new LocalObjectView(view.getId(), view.getName(), view.getDescription(), view.getClassName(), view.getStructure(), view.getBackground());
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }

    /**
     * Get a view related to an object, such as the default, rack or equipment views
     * @param oid object's id
     * @param objectClass object's class
     * @return The associated view (there should be only one of each type). Null if there's none yet
     */
    public List<LocalObjectViewLight> getObjectRelatedViews(long oid, String objectClass){
        try{
            List<ViewInfoLight> views = service.getObjectRelatedViews(oid, objectClass, -1, 10, session.getSessionId());
            List<LocalObjectViewLight> res = new ArrayList<>();
            
            for (ViewInfoLight view : views)
                res.add(new LocalObjectViewLight(view.getId(), view.getName(), view.getDescription(), view.getClassName()));
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }

    /**
     * Retrieves the list of views not related to a given object like GIS, topological views
     * @param viewClass Type of view to be retrieved. The possible values depend on the module creating general views
     * @return a list of object with the minimum information about the view (id, class and name). Null if something went wrong
     */
    public List<LocalObjectViewLight> getGeneralViews(String viewClass) {
        try{
            List<ViewInfoLight> views = service.getGeneralViews(viewClass, -1, session.getSessionId());
            List<LocalObjectViewLight> res = new ArrayList<>();
            for (ViewInfoLight view : views)
                res.add(new LocalObjectViewLight(view.getId(), view.getName(), view.getDescription(), view.getClassName()));
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }

    /**
     * Returns a view of those that are not related to a particular object (i.e.: GIS views)
     * @param viewId view id
     * @return An object representing the view
     */
    public LocalObjectView getGeneralView(long viewId) {
        try{
            ViewInfo view = service.getGeneralView(viewId, session.getSessionId());
            return new LocalObjectView(view.getId(), view.getClassName(), view.getName(), view.getDescription(), view.getStructure(), view.getBackground());
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }

    /**
     * Creates a view for a given object. If there's already a view of the provided view type, it will be overwritten
     * @param oid object's oid
     * @param objectClass object class
     * @param name view name
     * @param description view description
     * @param viewClassName view type (The supported values are provided per each module creating object related views)
     * @param structure XML document with the view structure (see http://sourceforge.net/apps/mediawiki/kuwaiba/index.php?title=XML_Documents#To_Save_Object_Views for details about the supported format)
     * @param background background image
     * @return the new object id
     */
    public long createObjectRelatedView(long oid, String objectClass, String name, String description, String viewClassName, byte[] structure, byte[] background){
        try{
            return service.createObjectRelatedView(oid, objectClass, name, description, viewClassName, structure, background, session.getSessionId());
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return -1;
        }
    }

    /**
     * Creates a view not related to a particular object
     * @param viewClass The class of the new view
     * @param name view name
     * @param description view description
     * @param structure XML document specifying the view structure (nodes, edges, control points)
     * @param background Background image
     * @return the new object id
     */
    public long createGeneralView(String viewClass, String name, String description, byte[] structure, byte[] background){
        try{
            return service.createGeneralView(viewClass, name, description, structure, background, session.getSessionId());
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return -1;
        }
    }

    /**
     * Create a view for a given object. If there's already a view of the provided view type, it will be overwritten
     * @param oid object's oid
     * @param objectClass object class
     * @param name view name
     * @param description view description
     * @param structure XML document with the view structure (see http://neotropic.co/kuwaiba/wiki/index.php?title=XML_Documents#To_Save_Object_Views for details about the supported format)
     * @param background Background image. If null, the previous will be removed, if 0-sized array, it will remain unchanged
     */
    public boolean updateObjectRelatedView(long oid, String objectClass, long viewId, String name, String description, byte[] structure, byte[] background){
        try{
            service.updateObjectRelatedView(oid, objectClass, viewId, name, description, structure, background, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }

    /**
     * Saves a view not related to a particular object. The view type can not be changed
     * @param view id
     * @param name view name. Null to leave unchanged
     * @param description view description. Null to leave unchanged
     * @param structure XML document specifying the view structure (nodes, edges, control points). Null to leave unchanged
     * @param background Background image. If null, the previous will be removed, if 0-sized array, it will remain unchanged
     */
    public boolean updateGeneralView(long oid, String name, String description, byte[] structure, byte[] background){
        try{
            service.updateGeneralView(oid, name, description, structure, background, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }


    /**
     * Deletes a list of general views
     * @param ids view ids
     * @throws ObjectNotFoundException if the view can't be found
     */
    public boolean deleteGeneralViews(long [] ids) {
         try{
             List<Long> oids = new ArrayList<>();
             for (long l : ids){ 
                 oids.add(l);
             }
             service.deleteGeneralView(oids, session.getSessionId());
             return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Service methods">
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Pools methods. Click on the + sign on the left to edit the code.">
    /**
     * Creates a pool without a parent. They're used as general purpose place to put inventory objects, or as root for particular models
     * @param name Pool name
     * @param description Pool description
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @param type Type of pool. For possible values see ApplicationManager.POOL_TYPE_XXX
     * @return The id of the new pool
     */
    public LocalPool createRootPool(String name, String description, String instancesOfClass, int type){
        try {
            long newPoolId  = service.createRootPool(name, description, instancesOfClass, type,session.getSessionId());
            return new LocalPool(newPoolId, name, instancesOfClass, description, type);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Creates a pool that will have as parent an inventory object.
     * This special containment structure can be used to provide 
     * support for new models.
     * @param parentClassname Class name of the parent object
     * @param parentId Id of the parent object
     * @param name Pool name
     * @param description Pool description
     * @param instancesOfClass What kind of objects can this pool contain? 
     * @param type Type of pool. For possible values see ApplicationManager.POOL_TYPE_XXX
     * @return The id of the new pool
     */
    public LocalPool createPoolInObject(String parentClassname, long parentId, String name, String description, String instancesOfClass, int type){
        try {
            long newPoolId  = service.createPoolInObject(parentClassname, parentId, name, description, instancesOfClass, type, session.getSessionId());
            return new LocalPool(newPoolId, name, instancesOfClass, description, type);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
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
     * @return The id of the new pool
     */
    public LocalPool createPoolInPool(long parentId, String name, String description, String instancesOfClass, int type){
        try {
            long newPoolId  = service.createPoolInPool(parentId, name, description, instancesOfClass, type, session.getSessionId());
            return new LocalPool(newPoolId, name, instancesOfClass, description, type);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Retrieves the pools that don't have any parent and are normally intended to be managed by the Pool Manager
     * @param className The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use null if you want to get all
     * @param type The type of pools that should be retrieved. Root pools can be for general purpose, or as roots in models
     * @param includeSubclasses Use <code>true</code> if you want to get only the pools whose <code>className</code> property matches exactly the one provided, and <code>false</code> if you want to also include the subclasses
     * @return A set of pools
     */
    public List<LocalPool> getRootPools(String className, int type, boolean includeSubclasses)  {
        try {
            List<LocalPool> res = new ArrayList<>();
            List<RemotePool> rootPools = service.getRootPools(className, type, includeSubclasses, session.getSessionId());
            
            for (RemotePool aPool : rootPools)
                res.add(new LocalPool(aPool.getId(), aPool.getName(), aPool.getClassName(), aPool.getDescription(), aPool.getType()));
            
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Retrieves the pools associated to a particular object
     * @param objectClassName The parent object class name
     * @param objectId The parent object id
     * @param poolClass The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use null if you want to get all
     * @return A set of pools
     */
    public List<LocalPool> getPoolsInObject(String objectClassName, long objectId, String poolClass)  {
        try {
            List<LocalPool> res = new ArrayList<>();
            List<RemotePool> rootPools = service.getPoolsInObject(objectClassName, objectId, poolClass, session.getSessionId());
            
            for (RemotePool aPool : rootPools)
                res.add(new LocalPool(aPool.getId(), aPool.getName(), aPool.getClassName(), aPool.getDescription(), aPool.getType()));
            
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Retrieves the pools associated to a particular pool
     * @param parentPoolId The parent pool id
     * @param poolClass The class name used to filter the results. Only the pools with a className attribute matching the provided value will be returned. Use null if you want to get all
     * @return A set of pools
     */
    public List<LocalPool> getPoolsInPool(long parentPoolId, String poolClass)  {
        try {
            List<LocalPool> res = new ArrayList<>();
            List<RemotePool> rootPools = service.getPoolsInPool(parentPoolId, poolClass, session.getSessionId());
            
            for (RemotePool aPool : rootPools)
                res.add(new LocalPool(aPool.getId(), aPool.getName(), aPool.getClassName(), aPool.getDescription(), aPool.getType()));
            
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Creates an object inside a pool
     * @param poolId Pool id
     * @param className Class of the object to be created
     * @return A local representation of the newly created object
     */
    public LocalObjectLight createPoolItem (long poolId, String className){
        try {
            long objectId  = service.createPoolItem(poolId, className, null, null, -1,session.getSessionId());
            return new LocalObjectLight(objectId, null, className);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public boolean deletePool(long id){
        try{
            service.deletePools(Arrays.asList(id), this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean setPoolProperties(long poolId, String name, String description) {
        try {
            service.setPoolProperties(poolId, name, description, this.session.getSessionId());
            return true;
        } catch(Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    /**
     * Retrieves the whole pool info
     * @param oid
     * @return The local representation of the pool
     */
    public LocalPool getPoolInfo(long oid) {
        try {
            RemotePool remotePool = service.getPool(oid, this.session.getSessionId());
            
            LocalPool localPool = new LocalPool(oid, remotePool.getName(), 
                    remotePool.getClassName(), 
                    remotePool.getDescription(), 
                    remotePool.getType());
            
            return localPool;
        } catch(Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    /**
     * Retrieves the items inside a pool
     *
     * @param oid The pool
     * @return The list of items inside the pool. Null in case of error
     */
    public List<LocalObjectLight> getPoolItems(long oid) {
        try {
            List<RemoteObjectLight> items = service.getPoolItems(oid, -1, this.session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : items) {
                HashMap<String, Integer> validators = new HashMap<>();
                for (Validator validator : rol.getValidators())
                    validators.put(validator.getLabel(), validator.getValue());
                res.add(new LocalObjectLight(rol.getClassName(), rol.getName(), rol.getOid(), validators));
            }

            return res;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    // </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Reporting API methods">
    /**
     * Creates a class level report (a report that will be available for all instances of a given class -and its subclasses-)
     * @param className Class this report is going to be related to. It can be ab abstract class and the report will be available for all its subclasses
     * @param reportName Name of the report.
     * @param reportDescription Report description.
     * @param script Script text.
     * @param outputType What will be the default output of this report? See ClassLevelReportDescriptor for possible values
     * @param enabled If enabled, a report can be executed.
     * @return The local representation of the newly created report. Null in case of error
     */
    public LocalReportLight createClassLevelReport(String className, String reportName, String reportDescription, String script, 
            int outputType, boolean enabled) { 
        try {
            long newPoolId  = service.createClassLevelReport(className, reportName, 
                    reportDescription, script, outputType, enabled,session.getSessionId());
            cache.resetReportCache();
            return new LocalReportLight(newPoolId, reportName, reportDescription, enabled, outputType);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }    
    /**
     * Creates an inventory level report (a report that is not tied to a particlar instance or class. In most cases, they also receive parameters)
     * @param reportName Name of the report.
     * @param reportDescription Report description.
     * @param script Script text.
     * @param outputType What will be the default output of this report? See InventoryLevelReportDescriptor for possible values
     * @param enabled If enabled, a report can be executed.
     * @param parameterNames Optional (it might be either null or an empty array). The list of the names parameters that this report will support. They will always be captured as strings, so it's up to the author of the report the sanitization and conversion of the inputs
     * @return The local representation of the newly created report. Null in case of error.
     */
    public LocalReportLight createInventoryLevelReport(String reportName, String reportDescription, String script, int outputType, 
            boolean enabled, List<String> parameterNames) {
        try {
            List<StringPair> parameters = new ArrayList<>();
            
            if (parameterNames != null) {
                for (String parameter : parameterNames) {
                    StringPair stringPair = new StringPair();
                    stringPair.setKey(parameter);
                    stringPair.setValue("");
                    parameters.add(stringPair);
                }
            }
            
            long newPoolId  = service.createInventoryLevelReport(reportName, reportDescription, 
                    script, outputType, enabled, parameters,session.getSessionId());
            return new LocalReportLight(newPoolId, reportName, reportDescription, enabled, outputType);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Deletes a report
     * @param reportId The id of the report.
     * @return True if successful. False in case of error.
     */
    public boolean deleteReport(long reportId) {
        try {
            service.deleteReport(reportId, session.getSessionId());
            cache.resetReportCache();
            return true;
        } catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
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
     * @return True if successful. False in case of error.
     */
    public boolean updateReport(long reportId, String reportName, String reportDescription, Boolean enabled,
            Integer type, String script) {
        try {
            service.updateReport(reportId, reportName, reportDescription, enabled,
                                    type, script, session.getSessionId());
            cache.resetReportCache();
            return true;
        } catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    /**
     * Adds or removes parameters in a report.
     * @param reportId Id of the report
     * @param parametersToAddOrModify List of parameters to add or modify. Set to null to not add anything
     * @param parametersToDelete List of parameters to delete. Set to null to not delete anything
     * @return 
     */
    public boolean updateReportParameters(long reportId, String[] parametersToAddOrModify, String[] parametersToDelete) {
        try {
            List<StringPair> parameters = new ArrayList<>();
            
            if (parametersToAddOrModify != null) {
                for (String parameter : parametersToAddOrModify) {
                    StringPair entry = new StringPair();
                    entry.setKey(parameter);
                    entry.setValue("");
                    parameters.add(entry);
                }
            }
            
            if (parametersToDelete != null) {
                for (String parameter : parametersToDelete) {
                    StringPair entry = new StringPair();
                    entry.setKey(parameter);
                    entry.setValue(null);
                    parameters.add(entry);
                }
            }
                
            service.updateReportParameters(reportId, parameters, session.getSessionId());
            return true;
        } catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    /**
     * Gets the class level reports associated to the given class (or its superclasses)
     * @param className The class to extract the reports from.
     * @param recursive False to get only the directly associated reports. True top get also the reports associate top its superclasses
     * @param includeDisabled True to also include the reports marked as disabled. False to return only the enabled ones.
     * @return The list of reports. Null in case of error.
     */
    public List<LocalReportLight> getClassLevelReports(String className, boolean recursive, boolean includeDisabled) {
        try {
            
            List<LocalReportLight> cachedClassLevelReportsForClass = cache.getClassLevelReportForClass(className);
            
            if (cachedClassLevelReportsForClass != null)
                return cachedClassLevelReportsForClass;
            
            List<RemoteReportLight> remoteClassLevelReports = 
                    service.getClassLevelReports(className, recursive, includeDisabled, session.getSessionId());
            
            List<LocalReportLight> localClassLevelReports = new ArrayList<>();
            
            for (RemoteReportLight remoteReport : remoteClassLevelReports)
                localClassLevelReports.add(new LocalReportLight(remoteReport.getId(), 
                        remoteReport.getName(), remoteReport.getDescription(), remoteReport.isEnabled(), remoteReport.getType()));
            
            cache.addClassLevelReportsForClass(className, localClassLevelReports);            
            return localClassLevelReports;
        } catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    /**
     * Gets the inventory class reports.
     * @param includeDisabled True to also include the reports marked as disabled. False to return only the enabled ones.
     * @return The list of reports. Null in case of error.
     */
    public List<LocalReportLight> getInventoryLevelReports(boolean includeDisabled) {
        try {
            List<RemoteReportLight> remoteClassLevelReports = 
                    service.getInventoryLevelReports(includeDisabled, session.getSessionId());
            
            List<LocalReportLight> localClassLevelReports = new ArrayList<>();
            
            for (RemoteReportLight remoteReport : remoteClassLevelReports)
                localClassLevelReports.add(new LocalReportLight(remoteReport.getId(), 
                        remoteReport.getName(), remoteReport.getDescription(), remoteReport.isEnabled(), remoteReport.getType()));
            
            return localClassLevelReports;
        } catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets the information related to a class level report.
     * @param reportId The id of the report.
     * @return  The report. Null in case of error.
     */
    public LocalReport getReport(long reportId) {
        try {
            RemoteReport remoteReport = service.getReport(reportId, session.getSessionId());
            List<String> parameters = new ArrayList<>();
            
            for (StringPair remoteParameter : remoteReport.getParameters())
                parameters.add(remoteParameter.getKey());
            
            return new LocalReport(reportId, remoteReport.getName(), remoteReport.getDescription(), 
                    remoteReport.isEnabled(), remoteReport.getType(), remoteReport.getScript(), parameters);
            
        } catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Executes a class level report and returns the result.
     * @param objectClassName The class of the instance that will be used as input for the report.
     * @param objectId The id of the instance that will be used as input for the report.
     * @param reportId The id of the report.
     * @return The result of the report execution. Null in case of error.
     */
    public byte[] executeClassLevelReport(String objectClassName, long objectId, long reportId) {
        try {
            return service.executeClassLevelReport(objectClassName, objectId, reportId, session.getSessionId());
        } catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Executes an inventory level report and returns the result.
     * @param reportId The id of the report.
     * @param parameters List of pairs param name - param value
     * @return The result of the report execution. Null in case of error.
     */
    public byte[] executeInventoryLevelReport(long reportId, HashMap<String, String> parameters) {
        try {
            List<StringPair> remoteParameters = new ArrayList<>();
            
            for (String paramName : parameters.keySet()) {
                StringPair parameter = new StringPair();
                parameter.setKey(paramName);
                parameter.setValue(parameters.get(paramName));
                remoteParameters.add(parameter);
            }
            
            return service.executeInventoryLevelReport(reportId, remoteParameters, session.getSessionId());
        } catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    //</editor-fold>
    
    //<editor-fold desc="Templates" defaultstate="collapsed">
    /**
     * Creates a template.
     * @param templateClass The class you want to create a template for.
     * @param templateName The name of the template. It can not be null.
     * @return The newly created template as a LocalObjectLight object.
     */
    public LocalObjectLight createTemplate(String templateClass, String templateName) {
        try {
            LocalObjectLight newTemplate = new LocalObjectLight(service.createTemplate(templateClass, 
                    templateName, session.getSessionId()), templateName, templateClass);
            cache.removeTemplateForClass(templateName);
            return newTemplate;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    /**
     * Creates an object inside a template.
     * @param templateElementClass Class of the object you want to create.
     * @param templateElementParentClassName Class of the parent to the object you want to create.
     * @param templateElementParentId Id of the parent to the object you want to create.
     * @param templateElementName Name of the element.
     * @return The id of the new object.
     */
    public LocalObjectLight createTemplateElement(String templateElementClass, 
            String templateElementParentClassName, long templateElementParentId, String templateElementName) {
            try {
            return new LocalObjectLight(service.createTemplateElement(templateElementClass, templateElementParentClassName, 
                    templateElementParentId, templateElementName, session.getSessionId()), templateElementName, templateElementClass);
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    /**
     * Creates an special object inside a template.
     * @param tsElementClass Class of the special object you want to create.
     * @param tsElementParentClassName Class of the parent to the special object you want to create.
     * @param tsElementParentId Id of the parent to the special object you want to create.
     * @param tsElementName Name of the element.
     * @return The id of the new special object.
     */
    public LocalObjectLight createTemplateSpecialElement(String tsElementClass, String tsElementParentClassName, long tsElementParentId, String tsElementName) {
            try {
            return new LocalObjectLight(service.createTemplateSpecialElement(tsElementClass, tsElementParentClassName, 
                    tsElementParentId, tsElementName, session.getSessionId()), tsElementName, tsElementClass);
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    /**
     * Updates the value of an attribute of a template element.
     * @param templateElementClass Class of the element you want to update.
     * @param templateElementId Id of the element you want to update.
     * @param attributeNames Names of the attributes that you want to be updated as an array of strings.
     * @param attributeValues The values of the attributes you want to upfate. For list types, it's the id of the related type
     * @return <code>true</code> if the update was successful, <code>false</code> otherwise.
     */
    public boolean updateTemplateElement(String templateElementClass, long templateElementId, 
            String[] attributeNames, String[] attributeValues) {
        try {
            service.updateTemplateElement(templateElementClass, templateElementId, 
                    Arrays.asList(attributeNames), Arrays.asList(attributeValues), session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    /**
     * Deletes an element within a template or a template itself.
     * @param templateElementClass The template element class.
     * @param templateElementId The template element id.
     * @return <code>true</code> if the update was successful, <code>false</code> otherwise.
     */
    public boolean deleteTemplateElement(String templateElementClass, long templateElementId) {
        try {
            service.deleteTemplateElement(templateElementClass, templateElementId, session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    /**
     * Gets the templates available for a given class
     * @param className Class whose templates we need
     * @param ignoreCache True to bypass the local cache, false otherwise
     * @return A list of templates (actually, the top element) as a list of LocalObjects
     */
    public List<LocalObjectLight> getTemplatesForClass(String className, boolean ignoreCache) {
        try {
            List<LocalObjectLight> localTemplates = null;
            
            if (!ignoreCache)
                localTemplates = cache.getTemplatesForClass(className);
            
            if (localTemplates == null) {
                localTemplates = new ArrayList<>();
                List<RemoteObjectLight> remoteTemplates = service.getTemplatesForClass(className, session.getSessionId());
                
                for (RemoteObjectLight remoteTemplate : remoteTemplates)
                    localTemplates.add(new LocalObjectLight(remoteTemplate.getOid(), remoteTemplate.getName(), remoteTemplate.getClassName()));
                
                cache.addTemplateForClass(className, localTemplates);
            }
            
            return localTemplates;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Retrieves the children of a given template element.
     * @param templateElementClass Template element class.
     * @param templateElementId Template element id.
     * @return The template element's children as a list of LocalObjectLight instances. It will return null if something went wrong.
     */
    public List<LocalObjectLight> getTemplateElementChildren(String templateElementClass, long templateElementId) {
        try {
            List<LocalObjectLight> localTemplateElementChildren = new ArrayList<>();
            List<RemoteObjectLight> remoteTemplateElementChildren = service.getTemplateElementChildren(templateElementClass, templateElementId, session.getSessionId());
            for (RemoteObjectLight remoteTemplateElementChild : remoteTemplateElementChildren)
                localTemplateElementChildren.add(new LocalObjectLight(remoteTemplateElementChild.getOid(), remoteTemplateElementChild.getName(), remoteTemplateElementChild.getClassName()));
            return localTemplateElementChildren;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Retrieves the children of a given template special element.
     * @param tsElementClass Template special element class.
     * @param tsElementId Template special element id.
     * @return The template element's children as a list of LocalObjectLight instances. It will return null if something went wrong.
     */
    public List<LocalObjectLight> getTemplateSpecialElementChildren(String tsElementClass, long tsElementId) {
        try {
            List<LocalObjectLight> localTemplateElementChildren = new ArrayList<>();
            List<RemoteObjectLight> remoteTemplateSpecialElementChildren = service.getTemplateSpecialElementChildren(tsElementClass, tsElementId, session.getSessionId());
            for (RemoteObjectLight remoteTemplateSpecialElementChild : remoteTemplateSpecialElementChildren)
                localTemplateElementChildren.add(new LocalObjectLight(remoteTemplateSpecialElementChild.getOid(), remoteTemplateSpecialElementChild.getName(), remoteTemplateSpecialElementChild.getClassName()));
            return localTemplateElementChildren;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Retrives all the information of a given template element.
     * @param templateElementClass Template element class.
     * @param templateElementId Template element id.
     * @return The template element information. It will return null if something went wrong.
     */
    public LocalObject getTemplateElement(String templateElementClass, long templateElementId) {
        try {
            RemoteObject remoteTemplateElement = service.getTemplateElement(templateElementClass, templateElementId, session.getSessionId());
            LocalClassMetadata lcmd = getMetaForClass(templateElementClass, false);
            List<List<String>> values = new ArrayList<>();
            for (StringArray value : remoteTemplateElement.getValues())
                values.add(value.getItem());
            
            return new LocalObject(remoteTemplateElement.getClassName(), remoteTemplateElement.getOid(), 
                    remoteTemplateElement.getAttributes(), values,lcmd);
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Copy template elements within templates. Should not be used to copy entire templates.
     * @param sourceObjectsClassNames Array with the class names of the elements to be copied.
     * @param sourceObjectsIds  Array with the ids of the elements to be copied.
     * @param newParentClassName Class of the parent of the copied objects.
     * @param newParentId Id of the parent of the copied objects.
     * @return An array with the ids of the newly created elements in the same order they were provided. Null in case of error.
     */
    public List<LocalObjectLight> copyTemplateElements(List<String> sourceObjectsClassNames, List<Long> sourceObjectsIds, 
            String newParentClassName, long newParentId) {
        try {
            List<Long> remoteTemplateElements = service.copyTemplateElements(sourceObjectsClassNames, 
                    sourceObjectsIds, newParentClassName, newParentId, session.getSessionId());
            
            List<LocalObjectLight> localTemplateElements = new ArrayList<>();
            
            for (int i = 0; i < sourceObjectsClassNames.size(); i++) 
                localTemplateElements.add(new LocalObjectLight(sourceObjectsIds.get(i), "", sourceObjectsClassNames.get(i)));
            
            return localTemplateElements;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public List<LocalObjectLight> copyTemplateSpecialElements(List<String> sourceObjectsClassNames, List<Long> sourceObjectsIds,
        String newParentClassName, long newParentId) {
        try {
            service.copyTemplateSpecialElements(sourceObjectsClassNames, 
                sourceObjectsIds, newParentClassName, newParentId, session.getSessionId());
            
            List<LocalObjectLight> localTemplateSpecialElements = new ArrayList<>();
            
            for (int i = 0; i < sourceObjectsClassNames.size(); i += 1)
                localTemplateSpecialElements.add(new LocalObjectLight(sourceObjectsIds.get(i), "", sourceObjectsClassNames.get(i)));
            
            return localTemplateSpecialElements;
        } catch (Exception ex) {
            error = ex.getMessage();
            return null;
        }
    }
    
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Sync/bulk load data methods. Click on the + sign on the left to edit the code.">
    /**
     * Load data from a file 
     * @param file data with the information to be uploaded
     * @param commitSize commit after n rows 
     * @param classType if the file contains listTypes or Classes or any other kind of information
     * @return 
     */
    public String loadDataFromFile(byte[] file, int commitSize, int classType){
        try {
            return  service.bulkUpload(file, commitSize, classType, this.session.getSessionId());
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }  
    
    public byte[] downloadLog(String fileName){
        try {
            return service.downloadBulkLoadLog(fileName, this.session.getSessionId());
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    // </editor-fold>    
    
    // <editor-fold defaultstate="collapsed" desc="Commercial Modules">
        //<editor-fold defaultstate="collapsed" desc="SDH Module">
    public LocalObjectLight createSDHTransportLink(LocalObjectLight endpointA, LocalObjectLight endpointB, String transportLinkType, String defaultName){
        
        try { 
            long newObjectId = service.createSDHTransportLink(endpointA.getClassName(),
                    endpointA.getOid(), endpointB.getClassName(), endpointB.getOid(), transportLinkType, defaultName, session.getSessionId());
            return new LocalObjectLight(newObjectId, defaultName, transportLinkType);
        } catch (ServerSideException_Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight createSDHContainerLink(LocalObjectLight equipmentA, LocalObjectLight equipmentB, 
            String containerLinkType, List<LocalSDHPosition> positions, String defaultName){
        
        try { 
            List<SdhPosition> remotepositions = new ArrayList<>();
            
            for (LocalSDHPosition aLocalPosition : positions) {
                SdhPosition aRemotePosition = new SdhPosition();
                aRemotePosition.setConnectionClass(aLocalPosition.getLinkClass());
                aRemotePosition.setConnectionId(aLocalPosition.getLinkId());
                aRemotePosition.setPosition(aLocalPosition.getPosition());
                remotepositions.add(aRemotePosition);
            }
            
            long newObjectId = service.createSDHContainerLink(equipmentA.getClassName(),
                    equipmentA.getOid(), equipmentB.getClassName(), equipmentB.getOid(), 
                    containerLinkType, remotepositions, defaultName, session.getSessionId());
            return new LocalObjectLight(newObjectId, defaultName, containerLinkType);
        } catch (ServerSideException_Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight createSDHTributaryLink(LocalObjectLight equipmentA, LocalObjectLight equipmentB, 
            String containerLinkType, List<LocalSDHPosition> positions, String defaultName){
        
        try { 
            List<SdhPosition> remotepositions = new ArrayList<>();
            
            for (LocalSDHPosition aLocalPosition : positions) {
                SdhPosition aRemotePosition = new SdhPosition();
                aRemotePosition.setConnectionClass(aLocalPosition.getLinkClass());
                aRemotePosition.setConnectionId(aLocalPosition.getLinkId());
                aRemotePosition.setPosition(aLocalPosition.getPosition());
                remotepositions.add(aRemotePosition);
            }
            
            long newObjectId = service.createSDHTributaryLink(equipmentA.getClassName(),
                    equipmentA.getOid(), equipmentB.getClassName(), equipmentB.getOid(), 
                    containerLinkType, remotepositions, defaultName, session.getSessionId());
            return new LocalObjectLight(newObjectId, defaultName, containerLinkType);
        } catch (ServerSideException_Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public List<LocalObjectLightList> findRoutesUsingTransportLinks(LocalObjectLight aSide, LocalObjectLight bSide) {
        try {
            List<RemoteBusinessObjectLightList> routes = service.findSDHRoutesUsingTransportLinks(aSide.getClassName(), aSide.getOid(), bSide.getClassName(), bSide.getOid(), session.getSessionId());
            List<LocalObjectLightList> res = new ArrayList<>();
            for (RemoteBusinessObjectLightList route : routes) 
                res.add(new LocalObjectLightList(route));
            
            return res;
        } catch (ServerSideException_Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public List<LocalObjectLightList> findRoutesUsingContainerLinks(LocalObjectLight aSide, LocalObjectLight bSide) {
        try {
            List<RemoteBusinessObjectLightList> routes = service.findSDHRoutesUsingContainerLinks(aSide.getClassName(), aSide.getOid(), bSide.getClassName(), bSide.getOid(), session.getSessionId());
            List<LocalObjectLightList> res = new ArrayList<>();
            for (RemoteBusinessObjectLightList route : routes) 
                res.add(new LocalObjectLightList(route));
            
            return res;
        } catch (ServerSideException_Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public List<LocalSDHContainerLinkDefinition> getSDHTransportLinkStructure(String transportLinkClass, long transportLinkId) {
        try {
            List<SdhContainerLinkDefinition> transportLinkStructure = service.getSDHTransportLinkStructure(transportLinkClass, transportLinkId, session.getSessionId());
            List<LocalSDHContainerLinkDefinition> res = new ArrayList<>();
            
            for (SdhContainerLinkDefinition aContainerDefinition : transportLinkStructure) {
                RemoteBusinessObjectLight container = aContainerDefinition.getContainer();
                List<LocalSDHPosition> positions = new ArrayList<>();
                
                for (SdhPosition aRemotePosition : aContainerDefinition.getPositions()) 
                    positions.add(new LocalSDHPosition(aRemotePosition.getConnectionClass(), aRemotePosition.getConnectionId(), aRemotePosition.getPosition()));
                
                LocalSDHContainerLinkDefinition aLocalContainerDefinition = 
                        new LocalSDHContainerLinkDefinition(new LocalObjectLight(container.getId(), container.getName(), container.getClassName()), 
                                aContainerDefinition.isStructured(), positions);
                res.add(aLocalContainerDefinition);
            }            
            return res;
        } catch (ServerSideException_Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    public List<LocalSDHContainerLinkDefinition> getSDHContainerLinkStructure(String containerLinkClass, long containerLinkId) {
        try {
            List<SdhContainerLinkDefinition> containerLinkStructure = service.getSDHContainerLinkStructure(containerLinkClass, containerLinkId, session.getSessionId());
            List<LocalSDHContainerLinkDefinition> res = new ArrayList<>();
            
            for (SdhContainerLinkDefinition aContainerDefinition : containerLinkStructure) {
                RemoteBusinessObjectLight container = aContainerDefinition.getContainer();
                List<LocalSDHPosition> positions = new ArrayList<>();
                
                for (SdhPosition aRemotePosition : aContainerDefinition.getPositions()) 
                    positions.add(new LocalSDHPosition(aRemotePosition.getConnectionClass(), aRemotePosition.getConnectionId(), aRemotePosition.getPosition()));
                
                LocalSDHContainerLinkDefinition aLocalContainerDefinition = 
                        new LocalSDHContainerLinkDefinition(new LocalObjectLight(container.getId(), container.getName(), container.getClassName()), 
                                aContainerDefinition.isStructured(), positions);
                res.add(aLocalContainerDefinition);
            }            
            return res;
        } catch (ServerSideException_Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="IPAM Module">
    public List<LocalPool> getSubnetPools(long parentId, String className){
        try{
            List <RemotePool> pools = service.getSubnetPools(-1, parentId, className, this.session.getSessionId());
            List <LocalPool> res = new ArrayList<>();
            
            for (RemotePool pool : pools)
                res.add(new LocalPool(pool.getId(), pool.getName(), pool.getClassName(), pool.getDescription(), pool.getType()));
            
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalPool getSubnetPool(long id){
        try {
            RemotePool subnetPool = service.getSubnetPool(id, this.session.getSessionId());
            return new LocalPool(subnetPool.getId(), subnetPool.getName(), subnetPool.getClassName(), 
                    subnetPool.getDescription(), subnetPool.getType());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalObject getSubnet(long id, String className){
        try{
            LocalClassMetadata lcmd = getMetaForClass(className, false);
            RemoteObject subnet = service.getSubnet(id, className, this.session.getSessionId());
            List<List<String>> values = new ArrayList<>();
            for (StringArray value : subnet.getValues())
                values.add(value.getItem());
            return new LocalObject(subnet.getClassName(), subnet.getOid(), 
                    subnet.getAttributes(), values,lcmd);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public List<LocalObjectLight> getSubnets(long oid) {
        try {
            List<RemoteObjectLight> items = service.getSubnets(oid, -1, this.session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : items) {
                HashMap<String, Integer> validators = new HashMap<>();
                for (Validator validator : rol.getValidators())
                    validators.put(validator.getLabel(), validator.getValue());
                res.add(new LocalObjectLight(rol.getClassName(), rol.getName(), rol.getOid(), validators));
            }

            return res;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight createSubnetPool(long parentId, String className, String subnetPoolName, 
            String subnetPoolDescription, int type) {
         try{
             long objectId = service.createSubnetPool(parentId, subnetPoolName, subnetPoolDescription, className, this.session.getSessionId());
             return new LocalObjectLight(objectId, subnetPoolName, className);
         }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public boolean deleteSubnet(String className, long subnetId){
        try {
            List<Long> subnetsToBeDeleted = new ArrayList<>();
            subnetsToBeDeleted.add(subnetId);
            service.deleteSubnets(className, subnetsToBeDeleted, false, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean deleteSubnetPool(long id){
        try{
            service.deleteSubnetPools(Arrays.asList(id), this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
        
    public LocalObjectLight createSubnet(long poolId, String parentClassName, LocalObject obj){
        try {
            List<String> attributeNames = new ArrayList<>();
            List<String> attributeValues = new ArrayList<>();

            for (String key : obj.getAttributes().keySet()){
                String value;
                attributeNames.add(key);
                value = obj.getAttribute(key).toString();
                
                attributeValues.add(value);
            }
            
            long objectId  = service.createSubnet(poolId, parentClassName, attributeNames, attributeValues, this.session.getSessionId());
            return new LocalObjectLight(objectId, obj.getName(), parentClassName);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public boolean deleteSDHTransportLink(String transportLinkClass, long transportLinkId) {
        try {
            service.deleteSDHTransportLink(transportLinkClass, transportLinkId, true, session.getSessionId());
            return true;
        } catch (ServerSideException_Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean deleteSDHContainerLink(String containerLinkClass, long containerLinkId) {
        try {
            service.deleteSDHContainerLink(containerLinkClass, containerLinkId, true, session.getSessionId());
            return true;
        } catch (ServerSideException_Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean deleteSDHTributaryLink(String tributaryLinkClass, long tributaryLinkId) {
        try {
            service.deleteSDHTributaryLink(tributaryLinkClass, tributaryLinkId, true, session.getSessionId());
            return true;
        } catch (ServerSideException_Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public LocalObjectLight addIP(long id, String className, LocalObject obj){
        try {
            List<String> attributeNames = new ArrayList<>();
            List<String> attributeValues = new ArrayList<>();

            for (String key : obj.getAttributes().keySet()){
                attributeNames.add(key);
                attributeValues.add(obj.getAttribute(key).toString());
            }
            
            long objectId  = service.addIP(id, className, attributeNames, attributeValues, this.session.getSessionId());
            return new LocalObjectLight(objectId, obj.getName(), className);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public boolean removeIP(List<Long> oids){
        try{
            service.removeIP(oids, false, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean relateIPtoPort(long id, String portClassName, long PortId){
        try{
            service.relateIPtoPort(id, portClassName, PortId, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public List<LocalObjectLight> getSubnetUsedIps(long id, String className){
        try {
            List<LocalObjectLight> res = new ArrayList<>();
            for (RemoteObjectLight anIp : service.getSubnetUsedIps(id, 0, className, this.session.getSessionId())) 
                res.add(new LocalObjectLight(anIp.getOid(), anIp.getName(), anIp.getClassName()));
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
        }
        return null;
    }
    
    public List<LocalObjectLight> getSubnetsInSubent(long id, String className){
        try {
            List<LocalObjectLight> res = new ArrayList<>();
            for (RemoteObjectLight anIp : service.getSubnetsInSubent(id, 0, className, this.session.getSessionId())) 
                res.add(new LocalObjectLight(anIp.getOid(), anIp.getName(), anIp.getClassName()));
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
        }
        return null;
    }

    public boolean relateSubnetToVLAN(long subnetId, String className, long vlanId){
        try{
            service.relateSubnetToVlan(subnetId, className, vlanId, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean relateSubnetToVFR(long subnetId, String className, long vfrId){
        try{
            service.relateSubnetToVrf(subnetId, className, vfrId, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean releasePortFromIPAddress(String deviceClassName, long deviceId, long id){
        try{
            service.releasePortFromIP(deviceClassName, deviceId, id, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Releases a subnet from a VLAN
     * @param vlanId VLAN Id
     * @param subnetId Subnet Id
     * @return true if the operation was successful, false otherwise
     */
    public boolean releaseSubnetFromVLAN(long subnetId, long vlanId){
        try{
            service.releaseSubnetFromVlan(subnetId, vlanId, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        } 
    }
    
    public boolean releaseSubnetFromVFR(long subnetId, long vfrId){
        try{
            service.releaseSubnetFromVRF(subnetId, vfrId, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        } 
    }
    
    public List<LocalObjectLight> getIps(long id, int limit){
        return null;
    }
    public boolean itOverlaps(String networkIp, String broadcastIp){
        return false;
    }
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Contract manager">
    public boolean associateObjectsToContract(String[] objectsClass, Long[] objectsId, String contractClass, long contractId) {
        try {
            List<String> objectsClassList = new ArrayList<>();
            List<Long> objectsIdList = new ArrayList<>();
            objectsClassList.addAll(Arrays.asList(objectsClass));
            objectsIdList.addAll(Arrays.asList(objectsId));
            service.associateObjectsToContract(objectsClassList, objectsIdList, contractClass, contractId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    public boolean releaseObjectFromContract(String contractClass, long contractId, long targetId){
        try{
            service.releaseObjectFromContract(contractClass, contractId, targetId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
        // </editor-fold>
    
        // <editor-fold defaultstate="collapsed" desc="MPLS Module">
    public LocalObjectLight createMPLSLink(LocalObjectLight endpointA, LocalObjectLight endpointB, String transportLinkType, String defaultName){
        try { 
            long newObjectId = service.createMPLSLink(endpointA.getClassName(),
                    endpointA.getOid(), endpointB.getClassName(), endpointB.getOid(), transportLinkType, defaultName, session.getSessionId());
            return new LocalObjectLight(newObjectId, defaultName, transportLinkType);
        } catch (ServerSideException_Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public boolean deleteMPLSLink(String linkClass, long linkId) {
        try {
            service.deleteMPLSLink(linkClass, linkId, true, session.getSessionId());
            return true;
        } catch (ServerSideException_Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean relatePortToInterface(long id, String className, String interfaceClassName, long interfaceId){
        try{
            service.relatePortToInterface(id, className, interfaceClassName, interfaceId, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean releasePortFromInterface(String interfaceClassName, long interfaceId, long id){
        try{
            service.releasePortFromInterface(interfaceClassName, interfaceId,  id, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    
    
        // </editor-fold>
    
        // <editor-fold defaultstate="collapsed" desc="Projects Module">
    /**
     * Gets the project pools
     * @return The list of project pools
     */
    public List<LocalPool> getProjectPools() {
        try {
            List<RemotePool> remotePools = service.getProjectPools(session.getSessionId());

            List<LocalPool> localPools = new ArrayList<>();
            
            for (RemotePool remotePool : remotePools) {
                localPools.add(new LocalPool(remotePool.getId(), remotePool.getName(), 
                    remotePool.getClassName(), remotePool.getDescription(), remotePool.getType()));
            }
            
            return localPools;
        } catch (Exception ex) {
            error = ex.getMessage();
            return null;
        }
    }
        
    /**
     * Adds a remoteProject
     * @param parentId Project parent id
     * @param parentClassName Project parent class name
     * @param className Project class name
     * @return The new remoteProject
     */
    public LocalObjectLight addProject(long parentId, String parentClassName, String className) {
        try {
            long objectId = service.addProject(parentId, parentClassName, className, new ArrayList<String>(),new ArrayList<String>(), session.getSessionId());
            return new LocalObjectLight(objectId, null, className);
        } catch (Exception ex) { 
            error = ex.getMessage(); 
            return null;
        }
    }
    
    /**
     * Deletes a Project
     * @param projectClass Project class
     * @param projectId Project id
     * @return If the remoteProject was deleted
     */
    public boolean deleteProject(String projectClass, long projectId) {
        try {
            service.deleteProject(projectClass, projectId, false, session.getSessionId());
            return true;
        } catch (Exception ex) {
            error = ex.getMessage();
            return false;
        }
    }
        
    /**
     * Adds an activity
     * @param projectId Project Id
     * @param projectClass Project class
     * @param activityClass Activity class name
     * @return A new activity
     */
    public LocalObjectLight addActivity(long projectId, String projectClass, String activityClass) {
        try {
            long activityId = service.addActivity(projectId, projectClass, activityClass, new ArrayList<String>(), new ArrayList<String>(), session.getSessionId());
            return new LocalObjectLight(activityId, null, activityClass);
        } catch (Exception ex) {
            error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Deletes an remoteActivity
     * @param activityClass Activity Class
     * @param activityId Activity id
     * @return True if the remoteActivity was successfully deleted
     */
    public boolean deleteActivity(String activityClass, long activityId) {
        try {
            service.deleteActivity(activityClass, activityId, false, session.getSessionId());
            return true;
            
        } catch (Exception ex) {
            error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Gets the project in a Project pool
     * @param poolId Project pool id
     * @param limit Max number of results, -1 without limit
     * @return The list of projects
     */
    public List<LocalObjectLight> getProjectInProjectPool(long poolId, int limit) {
        try {
            List<RemoteObjectLight> remoteProjects = service.getProjectsInProjectPool(poolId, limit, session.getSessionId());
            
            List<LocalObjectLight> projects = new ArrayList<>();
            
            for (RemoteObjectLight remoteProject : remoteProjects)
                projects.add(new LocalObjectLight(remoteProject.getOid(), remoteProject.getName(), remoteProject.getClassName()));
            
            return projects;                                    
        } catch (Exception ex) {
            error = ex.getMessage();
            return null;
        }
    }
        
    /**
     * Gets the objects (resources) associated with a remoteProject
     * @param projectClass
     * @param projectId
     * @return The list of resources
     */
    public List<LocalObjectLight> getProjectResources(String projectClass, long projectId) {
        try {
            List<LocalObjectLight> resources = new ArrayList<>();
            
            for (RemoteObjectLight remoteResource : service.getProjectResurces(projectClass, projectId, session.getSessionId()))
                resources.add(new LocalObjectLight(remoteResource.getOid(), remoteResource.getName(), remoteResource.getClassName()));
            
            return resources;
            
        } catch (Exception ex) {
            error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets the activities of an remoteProject
     * @param projectClass Project class
     * @param projectId Project id
     * @return The list of activities
     */
    public List<LocalObjectLight> getProjectActivities(String projectClass, long projectId) {
        try {
            List<LocalObjectLight> activities = new ArrayList<>();
            
            for (RemoteObjectLight remoteActivity : service.getProjectActivities(projectClass, projectId, session.getSessionId()))
                activities.add(new LocalObjectLight(remoteActivity.getOid(), remoteActivity.getName(), remoteActivity.getClassName()));
            
            return activities;
        } catch (Exception ex) {
            error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Associates a set of object with a remoteProject
     * @param projectClass Project class
     * @param projectId Project id
     * @param objectClass The list of object classes
     * @param objectId The list of object ids
     * @return True if the objects was associated successfully
     */
    public boolean associateObjectsToProject(String projectClass, long projectId, List<String> objectClass, List<Long> objectId) {
        try {
            service.associateObjectsToProject(projectClass, projectId, objectClass, objectId, session.getSessionId());
            return true;
        } catch (Exception ex) {
            error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Associates an object with a remoteProject
     * @param projectClass Project class
     * @param projectId Project id
     * @param objectClass Object class
     * @param objectId Object id
     * @return True if the object was associated successfully
     */
    public boolean associateObjectToProject(String projectClass, long projectId, String objectClass, long objectId) {
        try {
            service.associateObjectToProject(projectClass, projectId, objectClass, objectId, session.getSessionId());
            return true;
        } catch (Exception ex) {
            error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Releases an object from a Project
     * @param objectClass Object class
     * @param objectId Object id
     * @param projectClass Project class
     * @param projectId Project id
     * @return True if the object was released successfully
     */
    public boolean releaseObjectFromProject(String objectClass, long objectId, String projectClass, long projectId) {
        try {
            service.freeObjectFromProject(objectClass, objectId, projectClass, projectId, session.getSessionId());
            return true;
        } catch (Exception ex) {
            error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Gets the projects associated to an object
     * @param objectClass Object Class
     * @param objectId Object id
     * @return The list of projects
     */
    public List<LocalObjectLight> getProjectsAssociateToObject(String objectClass, long objectId) {
        try {
            List<RemoteObjectLight> remoteProjects = service.getProjectsAssociateToObject(objectClass, objectId, session.getSessionId());
            
            List<LocalObjectLight> projects = new ArrayList<>();
            
            for (RemoteObjectLight remoteProject : remoteProjects)
                projects.add(new LocalObjectLight(remoteProject.getOid(), remoteProject.getName(), remoteProject.getClassName()));
            
            return projects;                                    
        } catch (Exception ex) {
            error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Creates a Project Pool
     * @param name Project name
     * @param description Project description
     * @param instanceOfClass Project class
     * @return The new Project Pool
     */
    public LocalPool createProjectPool(String name, String description, String instanceOfClass) {
        try {
            long projId = service.createProjectPool(name, description, instanceOfClass, session.getSessionId());
            return getPoolInfo(projId);
        } catch (ServerSideException_Exception ex) {
            error = ex.getMessage();
            return null;
        }
    }
        // </editor-fold>
    
    // </editor-fold>
    
    // <editor-fold desc="Helper Methods" defaultstate="collapsed">
    /**
     * Tells if the instances of a class have a custom delete method or if the generic delete action should be used instead
     * @param className Class to be evaluated
     * @return True if the instances of the class provided as argument have a custom delete method or false if the generic delete method should be used instead
     * @depreca@depreted This functionality should be integrated with the data model manager in future versions
     */
    public boolean hasCustomDeleteAction(String className) {
        for (String classWihCustomDeleteAction : classesWithCustomDeleteActions) {
            if(classWihCustomDeleteAction.equals(className))
                return true;
        }
        
        return false;
    }
    // </editor-fold>
    
    //<editor-fold desc="Favorites" defaultstate="collapsed">
    /**
     * Adds a list of objects to a Favorites folder
     * @param objectClass List of class names
     * @param objectId List of object id
     * @param bookmarkFolderId Favorites folder id
     * @return True if objects are associated with the bookmark
     */
    public boolean addObjectsToFavoritesFolder(List<String> objectClass, List<Long> objectId, long bookmarkFolderId) {
        try {
            service.addObjectsToFavoritesFolder(objectClass, objectId, bookmarkFolderId, session.getUserId(), session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Removes the objects from a Favorites folder
     * @param objectClass List of class names
     * @param objectId List of object id
     * @param bookmarkFolderId Favorites folder id
     * @return True if objects are released of the bookmark
     */
    public boolean removeObjectsFromFavoritesFolder(List<String> objectClass, List<Long> objectId, long bookmarkFolderId) {
        try {
            service.removeObjectsFromFavoritesFolder(objectClass, objectId, bookmarkFolderId, session.getUserId(), session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Gets the objects associated to a Favorites Folder
     * @param bookmarkFolderId Favorites folder id
     * @param limit Max number of items. Use -1 to results without limit in the number of items
     * @return The list of items (objects) associated to a bookmark folder
     */
    public List<LocalObjectLight> getObjectsInFavoritesFolder(long bookmarkFolderId, int limit) {
        try {
            List<RemoteObjectLight> bookmarkItems = service.getObjectsInFavoritesFolder(bookmarkFolderId, session.getUserId(), limit, session.getSessionId());

            List<LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : bookmarkItems) {
                HashMap<String, Integer> validators = new HashMap<>();
                for (Validator validator : rol.getValidators())
                    validators.put(validator.getLabel(), validator.getValue());
                res.add(new LocalObjectLight(rol.getClassName(), rol.getName(), rol.getOid(), validators));
            }

            return res;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Create a Favorites folder with for the current user
     * @param bookmarkFolderName The name of the bookmark
     * @return The local representation of the Favorites folder
     */        
    public LocalFavoritesFolder createFavoritesFolderForUser(String bookmarkFolderName) {
        try {
            long id = service.createFavoritesFolderForUser(bookmarkFolderName, session.getUserId(), session.getSessionId());
            return new LocalFavoritesFolder(id, bookmarkFolderName);
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Delete a bookmark with all his relations
     * @param bookmarkFolderId The Favorites folder id
     * @return true if the Favorites folder was deleted successfully
     */
    public boolean deleteFavoritesFolders (List<Long> bookmarkFolderId) {
        try {
            service.deleteFavoritesFolders (bookmarkFolderId, session.getUserId(), session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Gets the list of Favorites folders associated to the current user
     * @return The list of associate bookmark 
     */
    public List<LocalFavoritesFolder> getFavoritesFoldersForUser() {
        try {
            List<RemoteFavoritesFolder> remotefavoritesFolders = service.getFavoritesFoldersForUser(session.getUserId(), session.getSessionId());
            
            List<LocalFavoritesFolder> localBookmarks = new ArrayList<>();
            
            for (RemoteFavoritesFolder remoteFavorite : remotefavoritesFolders)
                localBookmarks.add(new LocalFavoritesFolder(remoteFavorite.getId(), 
                        remoteFavorite.getName()));
            
            return localBookmarks;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets the Bookmarks folder where an object are an item
     * @param objectClass the class name of the object
     * @param objectId the id of the object
     * @return The list of bookmarks where a given object are an item of the Favorites folder
     */
    public List<LocalFavoritesFolder> objectIsBookmarkItemIn(String objectClass, long objectId) {
        try {
            List<RemoteFavoritesFolder> remotefavoritesFolders = service.getFavoritesFoldersForObject(session.getUserId(), objectClass, objectId, session.getSessionId());
            
            List<LocalFavoritesFolder> localBookmarks = new ArrayList<>();
            
            for (RemoteFavoritesFolder remoteFavorite : remotefavoritesFolders)
                localBookmarks.add(new LocalFavoritesFolder(remoteFavorite.getId(), 
                        remoteFavorite.getName()));
            
            return localBookmarks;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets a Favorites folder
     * @param bookmarkFolderId The Favorites folder Id
     * @return The local representation of a Favorites folder
     */
    public LocalFavoritesFolder getFavoritesFolder(long bookmarkFolderId) {
        try {
            RemoteFavoritesFolder remoteFavorite = service.getFavoritesFolder(bookmarkFolderId, session.getUserId(), session.getSessionId());
            return new LocalFavoritesFolder(remoteFavorite.getId(), remoteFavorite.getName());
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Updates a Favorites folder
     * @param bookmarkFolderId The Favorites folder id
     * @param bookmarkFolderNewName The Favorites folder new name
     * @return True if the Favorites folder was updated successfully
     */
    public boolean updateFavoritesFolder(long bookmarkFolderId, String bookmarkFolderNewName) {
        try {
            service.updateFavoritesFolder(bookmarkFolderId, bookmarkFolderNewName, session.getUserId(), session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    //</editor-fold>
    
    //<editor-fold desc="Business Rules" defaultstate="collapsed">
    /**
     * Creates a business rule given a set of constraints
     * @param ruleName Rule name
     * @param ruleDescription Rule description
     * @param ruleType Rule type. See LocalBusinesRule.TYPE* for possible values.
     * @param ruleScope The scope of the rule. See LocalBusinesRule.SCOPE* for possible values.
     * @param appliesTo The class this rule applies to. Can not be null.
     * @param ruleVersion The version of the rule. Useful to migrate it if necessary in further versions of the platform
     * @param constraints An array with the definition of the logic to be matched with the rule. Can not be empty or null
     * @return The newly created business rule or null is an error was returned by the server
     */
    public LocalBusinessRule createBusinessRule(String ruleName, String ruleDescription, int ruleType, 
            int ruleScope, String appliesTo, String ruleVersion, List<String> constraints) {
        try {
            long newBussinessRuleId  = service.createBusinessRule(ruleName, ruleDescription, ruleType, ruleScope, appliesTo, ruleVersion, constraints,session.getSessionId());
            return new LocalBusinessRule(newBussinessRuleId, ruleName, ruleDescription, appliesTo, ruleType, ruleScope, ruleVersion);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Deletes a business rule
     * @param businessRuleId Rule id
     * @return true if it was possible to delete the rule or false otherwise
     */
    public boolean deleteBusinessRule(long businessRuleId) {
        try {
            service.deleteBusinessRule(businessRuleId,session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    public List<LocalBusinessRule> getBusinessRules(int type) {
        try {
            List<RemoteBusinessRule> remoteBusinessRules = service.getBusinessRules(type,session.getSessionId());
            List<LocalBusinessRule> res = new ArrayList<>();
            for (RemoteBusinessRule remoteBusinessRule : remoteBusinessRules)
                res.add(new LocalBusinessRule(remoteBusinessRule.getRuleId(), remoteBusinessRule.getName(), 
                        remoteBusinessRule.getDescription(), remoteBusinessRule.getAppliesTo(), remoteBusinessRule.getType(), 
                        remoteBusinessRule.getScope(), remoteBusinessRule.getVersion()));
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    //</editor-fold>
}

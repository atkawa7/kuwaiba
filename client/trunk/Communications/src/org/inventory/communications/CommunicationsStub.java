/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Response;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPFaultException;
import org.inventory.communications.core.LocalApplicationLogEntry;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalBackgroundJob;
import org.inventory.communications.core.LocalBusinessRule;
import org.inventory.communications.core.LocalFavoritesFolder;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalContact;
import org.inventory.communications.core.LocalFileObject;
import org.inventory.communications.core.LocalFileObjectLight;
import org.inventory.communications.core.LocalLogicalConnectionDetails;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectLightList;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalReport;
import org.inventory.communications.core.LocalReportLight;
import com.neotropic.inventory.modules.sync.LocalSyncDataSourceConfiguration;
import com.neotropic.inventory.modules.sync.LocalSyncFinding;
import com.neotropic.inventory.modules.sync.LocalSyncGroup;
import com.neotropic.inventory.modules.sync.LocalSyncResult;
import org.inventory.communications.core.LocalTaskResultMessage;
import org.inventory.communications.core.LocalTask;
import org.inventory.communications.core.LocalTaskNotificationDescriptor;
import org.inventory.communications.core.LocalTaskResult;
import org.inventory.communications.core.LocalTaskScheduleDescriptor;
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.communications.core.LocalUserObjectLight;
import org.inventory.communications.core.caching.Cache;
import org.inventory.communications.core.exceptions.InventoryException;
import org.inventory.communications.core.queries.LocalQuery;
import org.inventory.communications.core.queries.LocalQueryLight;
import org.inventory.communications.core.queries.LocalResultRecord;
import org.inventory.communications.core.queries.LocalTransientQuery;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import com.neotropic.inventory.modules.sync.AbstractRunnableSyncFindingsManager;
import com.neotropic.inventory.modules.sync.AbstractRunnableSyncResultsManager;
import com.neotropic.inventory.modules.sync.LocalSyncAction;
import com.neotropic.inventory.modules.sync.LocalSyncProvider;
import org.inventory.communications.core.LocalConfigurationVariable;
import org.inventory.communications.core.LocalMPLSConnectionDetails;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.core.LocalValidatorDefinition;
import org.inventory.communications.util.Constants;
import org.inventory.communications.wsclient.ApplicationLogEntry;
import org.inventory.communications.wsclient.RemoteAttributeMetadata;
import org.inventory.communications.wsclient.GetClassResponse;
import org.inventory.communications.wsclient.GetObjectChildrenResponse;
import org.inventory.communications.wsclient.GetObjectResponse;
import org.inventory.communications.wsclient.GetSpecialAttributesResponse;
import org.inventory.communications.wsclient.GroupInfo;
import org.inventory.communications.wsclient.KuwaibaService;
import org.inventory.communications.wsclient.KuwaibaService_Service;
import org.inventory.communications.wsclient.LaunchAdHocAutomatedSynchronizationTaskResponse;
import org.inventory.communications.wsclient.LaunchAutomatedSynchronizationTaskResponse;
import org.inventory.communications.wsclient.LaunchSupervisedSynchronizationTaskResponse;
import org.inventory.communications.wsclient.PrivilegeInfo;
import org.inventory.communications.wsclient.RemoteBackgroundJob;
import org.inventory.communications.wsclient.RemoteFavoritesFolder;
import org.inventory.communications.wsclient.RemoteBusinessRule;
import org.inventory.communications.wsclient.RemoteClassMetadata;
import org.inventory.communications.wsclient.RemoteClassMetadataLight;
import org.inventory.communications.wsclient.RemoteConfigurationVariable;
import org.inventory.communications.wsclient.RemoteContact;
import org.inventory.communications.wsclient.RemoteFileObject;
import org.inventory.communications.wsclient.RemoteFileObjectLight;
import org.inventory.communications.wsclient.RemoteLogicalConnectionDetails;
import org.inventory.communications.wsclient.RemoteMPLSConnectionDetails;
import org.inventory.communications.wsclient.RemoteObject;
import org.inventory.communications.wsclient.RemoteObjectLight;
import org.inventory.communications.wsclient.RemoteObjectLightList;
import org.inventory.communications.wsclient.RemoteObjectSpecialRelationships;
import org.inventory.communications.wsclient.RemotePool;
import org.inventory.communications.wsclient.RemoteQueryLight;
import org.inventory.communications.wsclient.RemoteReport;
import org.inventory.communications.wsclient.RemoteReportLight;
import org.inventory.communications.wsclient.RemoteResultMessage;
import org.inventory.communications.wsclient.RemoteSDHContainerLinkDefinition;
import org.inventory.communications.wsclient.RemoteSDHPosition;
import org.inventory.communications.wsclient.RemoteSynchronizationConfiguration;
import org.inventory.communications.wsclient.RemoteSynchronizationGroup;
import org.inventory.communications.wsclient.RemoteSynchronizationProvider;
import org.inventory.communications.wsclient.RemoteTask;
import org.inventory.communications.wsclient.RemoteTaskResult;
import org.inventory.communications.wsclient.RemoteValidator;
import org.inventory.communications.wsclient.RemoteValidatorDefinition;
import org.inventory.communications.wsclient.ResultRecord;
import org.inventory.communications.wsclient.StringPair;
import org.inventory.communications.wsclient.SyncFinding;
import org.inventory.communications.wsclient.SyncResult;
import org.inventory.communications.wsclient.TaskNotificationDescriptor;
import org.inventory.communications.wsclient.TaskScheduleDescriptor;
import org.inventory.communications.wsclient.TransientQuery;
import org.inventory.communications.wsclient.UserInfo;
import org.inventory.communications.wsclient.UserInfoLight;
import org.inventory.communications.wsclient.RemoteViewObject;
import org.inventory.communications.wsclient.RemoteViewObjectLight;
import org.inventory.communications.wsclient.SyncAction;

/**
 * Singleton class that provides communication and caching services to the rest of the modules
 * TODO: Make it a thread to support simultaneous operations
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class CommunicationsStub {
    private static CommunicationsStub instance;
    private KuwaibaService service;
    private static URL serverURL = null;
    private String error = java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_ERROR");
    private Cache cache;
    private LocalSession session;
    
    private static String[] classesWithCustomDeleteActions = new String[] {"ElectricalLink", "RadioLink", "OpticalLink", "MPLSLink",
                                                                    "VC4", "VC4-04", "VC4-16", "VC4-07", "VC4-64", "VC4TributaryLink", "VC12TributaryLink", "VC3TributaryLink",
                                                                    "STM1", "STM4", "STM16", "STM64", "STM256",
                                                                    "WireContainer", "WirelessContainer",
                                                                    "Provider","BGPPeer", "VFI",
                                                                    "CorporateCustomer", "TelecomOperator", "Provider", "HomeCustomer",
                                                                    "Subnet", "BillingContact", "TechnicalContact", "CommercialContact", "BridgeDomain" };
    
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
            
            //Overrides the schema location in the WSDL. Most of the times this is not necessary, but when the server is behind
            //a proxy, there will be a mismatch in the URI, which will be corrected here.
            ((BindingProvider)service).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                serverURL.toString().replace("wsdl", "xsd=1")); //NOI18N
            
            this.session = new LocalSession(this.service.createSession(user, password, LocalSession.TYPE_DESKTOP));
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
     * @return an array of local objects representing the object's children. Null if a problem occurs during the execution
     */
    public List<LocalObjectLight> getObjectChildren(String oid, long objectClassId){
        try{
            List <RemoteObjectLight> children = service.getObjectChildrenForClassWithId(oid, objectClassId, 0, this.session.getSessionId());
            List <LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : children)
                res.add(new LocalObjectLight(rol.getClassName(), rol.getName(), rol.getId(), rol.getValidators()));
            
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public List<LocalObjectLight> getObjectChildrenAsync(String oid, String className) {
        try {
            Response<GetObjectChildrenResponse> response = service.getObjectChildrenAsync(className, oid, 0, session.getSessionId());
            while (!response.isDone())
                Thread.sleep(100);        
            GetObjectChildrenResponse getObjectChildrenResponse = response.get();
            
            List <RemoteObjectLight> children = getObjectChildrenResponse.getReturn();
            
            List <LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : children)
                res.add(new LocalObjectLight(rol.getId(), rol.getName(), rol.getClassName()));
                        
            return res;
            
        } catch (InterruptedException | ExecutionException ex) {
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight[] getSiblings(String objectClass, String objectId) {
        try{
            List <RemoteObjectLight> siblings = service.getSiblings(objectClass, objectId, 0,this.session.getSessionId());
            LocalObjectLight[] res = new LocalObjectLight[siblings.size()];
            
            int i = 0;
            for (RemoteObjectLight rol : siblings) {
                res[i] = new LocalObjectLight(rol.getClassName(), rol.getName(), rol.getId(), rol.getValidators());
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
    public List<LocalObjectLight> getObjectChildren(String oid, String className) {
        try {
            List <RemoteObjectLight> children = service.getObjectChildren(className, oid, 0,this.session.getSessionId());
            List <LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : children)
                res.add(new LocalObjectLight(rol.getId(), rol.getName(), rol.getClassName()));

            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }

    /**
     * Gets only the children of a given class of an inventory object as a {@link LocalObject } instances.
     * @param id The id of the parent object
     * @param parentClassName The class of the parent object
     * @param childrenClassName The class used to filter the children. Abstract super classes are supported
     * @return The list of children filtered using the class provided as parameter
     */
    public List<LocalObject> getChildrenOfClass(String id, String parentClassName, String childrenClassName){
        try{
            List <RemoteObject> remoteChildren = service.getChildrenOfClass(id, parentClassName, childrenClassName, 0, this.session.getSessionId());
            List <LocalObject> res = new ArrayList<>();

            for (RemoteObject remoteChild : remoteChildren) {
                LocalClassMetadata classMetadata = getMetaForClass(remoteChild.getClassName(), false);
                res.add(new LocalObject(remoteChild.getClassName(), id, remoteChild.getAttributes(), classMetadata));
            }
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Returns the special children of a given object as RemoteBusinessObjectLight instances. This method is not recursive.
     * @param parentOid The id of the parent object
     * @param parentClass The class name of the parent object
     * @param classToFilter The superclass/class to be used to filter the results. You can also use abstract superclasses.
     * @return The list of special children of the given object, filtered using classToFilter. Null if  the parent class name provided could not be found or if the parent object could not be found
     */
    public List<LocalObjectLight> getSpecialChildrenOfClassLight(String parentClass, String parentOid, String classToFilter) {
        try {
            List <RemoteObjectLight> children = service.getSpecialChildrenOfClassLight(parentOid, parentClass, classToFilter, -1, session.getSessionId());
            List <LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : children)
                res.add(new LocalObjectLight(rol.getId(), rol.getName(), rol.getClassName()));

            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets recursively all children of an object of a given class
     * @param oid Parent whose children are requested
     * @param parentClassName Class name of the element we want the children from
     * @param childrenClassName The type of children we want to retrieve
     * @return An array with the children objects
     */
    public List<LocalObjectLight> getChildrenOfClassLightRecursive(String oid, String parentClassName, String childrenClassName) {
        try {
            List <RemoteObjectLight> children = service.getChildrenOfClassLightRecursive(oid, parentClassName, childrenClassName, -1, session.getSessionId());
            List <LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : children)
                res.add(new LocalObjectLight(rol.getId(), rol.getName(), rol.getClassName()));

            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Fetches the instances of a given class. Abstract classes are supported. Use with care, since the 
     * amount of objects might be extensive.
     * @param className The class of the objects to be retrieved
     * @return The list of inventory objects or null in case of error
     */
    public List<LocalObjectLight> getObjectsOfClassLight(String className){
        try{
            List <RemoteObjectLight> instances = service.getObjectsOfClassLight(className, 0, this.session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : instances)
                res.add(new LocalObjectLight(rol.getId(), rol.getName(), rol.getClassName()));

            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    // </editor-fold>

    /**
     * Updates the attributes of a given object
     * @param className Class name of the object to be updated
     * @param id Id of the object to be updated
     * @param attributesToBeUpdated A key-value dictionary with the attributes and their respective values to be updated
     * @return True if the operation was successful, false otherwise
     */
    public boolean updateObject(String className, String id, HashMap<String, Object> attributesToBeUpdated) {

        try {
            List<StringPair> attributes = new ArrayList<>();
            for (String attributeToBeUpdated : attributesToBeUpdated.keySet()) {
                Object theValue = attributesToBeUpdated.get(attributeToBeUpdated);
                if (theValue instanceof LocalObjectListItem)
                    attributes.add(new StringPair(attributeToBeUpdated, String.valueOf(((LocalObjectListItem)theValue).getId())));
                else {
                    if (theValue instanceof Date)
                        attributes.add(new StringPair(attributeToBeUpdated, String.valueOf(((Date)theValue).getTime())));
                    else
                        attributes.add(new StringPair(attributeToBeUpdated, theValue.toString()));
                }
            }
            service.updateObject(className, id, attributes, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }

    /**
     * Retrieves all the attributes of an inventory object
     * @param objectClass object class
     * @param id object id
     * @return The local representation of the object
     */
    public LocalObject getObjectInfo(String objectClass, String id) {
        try {
            LocalClassMetadata classMetadata = getMetaForClass(objectClass, false);
            RemoteObject remoteObject = service.getObject(objectClass, id,this.session.getSessionId());

            return new LocalObject(remoteObject.getClassName(), id, remoteObject.getAttributes(), classMetadata);
        } catch(Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Async version of the conventional {@link CommunicationsStub#getObjectInfo(java.lang.String, long) } method
     * @param objectClass The class of the object
     * @param id The id of the object
     * @return The local representation of the object
     */
    public LocalObject getObjectInfoAsync(String objectClass, String id) {
        try {
            LocalClassMetadata classMetadata = getMetaForClassAsync(objectClass, false);
            
            Response<GetObjectResponse> response = service.getObjectAsync(objectClass, id,this.session.getSessionId());
            while (!response.isDone())
                Thread.sleep(100);
            
            GetObjectResponse getObjectResponse = response.get();
                        
            RemoteObject remoteObject = getObjectResponse.getReturn();
            
            return new LocalObject(remoteObject.getClassName(), remoteObject.getId(), 
                    remoteObject.getAttributes(), classMetadata);
            
        } catch(Exception ex) {
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
    public LocalObjectLight getCommonParent(String aObjectClass, String aOid, String bObjectClass, String bOid) {
        try {
            RemoteObjectLight parent = service.getCommonParent(aObjectClass, aOid, bObjectClass, bOid, session.getSessionId());
            return new LocalObjectLight(parent.getId(), parent.getName(), parent.getClassName());
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
    public List<LocalObjectLight> getParents(String objectClass, String objectId) {
        try {
            List<RemoteObjectLight> parents = service.getParents(objectClass, objectId, session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();
            for (RemoteObjectLight aParent : parents)
                res.add(new LocalObjectLight(aParent.getId(), aParent.getName(), aParent.getClassName()));

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
    public LocalObjectLight getParent(String objectClass, String objectId) {
        try {
            RemoteObjectLight parent = service.getParent(objectClass, objectId, session.getSessionId());
            return new LocalObjectLight(parent.getId(), parent.getName(), parent.getClassName());
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
    public List<LocalObjectLight> getParentsUntilFirstOfClass(String objectClass, String objectId, String objectToMatchClassName) {
        try {
            List<RemoteObjectLight> parents = service.getParentsUntilFirstOfClass(objectClass, objectId, objectToMatchClassName, session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();
            for (RemoteObjectLight aParent : parents)
                res.add(new LocalObjectLight(aParent.getId(), aParent.getName(), aParent.getClassName()));

            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets the first occurrence of a parent with a given class (according to the special and standard containment hierarchy)
     * (for example "give me the parent of this port until you find the nearest rack")
     * @param objectClass Class of the object to get the parent from
     * @param objectId Id of the object to get the parent from
     * @param objectToMatchClassName Class of the object that will limit the search. It can be a superclass, if you want to match many classes at once
     * @return The the first occurrence of a parent with a given class. If no instance of that class is found, the Dummy Root will be returned
     */
    public LocalObjectLight getFirstParentOfClass(String objectClass, String objectId, String objectToMatchClassName) {
        try {
            RemoteObjectLight parent = service.getFirstParentOfClass(objectClass, objectId, objectToMatchClassName, session.getSessionId());
            return parent != null ? new LocalObjectLight(parent.getId(), parent.getName(), parent.getClassName()) : null;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public HashMap<String, LocalObjectLight[]> getSpecialAttributes (String objectClass, String objectId) {
        try{
            RemoteObjectSpecialRelationships remoteRelationships = service.getSpecialAttributes(objectClass, objectId, session.getSessionId());
            HashMap<String, LocalObjectLight[]> res = new HashMap<>();
            
            for (int i = 0; i < remoteRelationships.getRelationships().size(); i++){
                
                RemoteObjectLightList relatedRemoteObjects = remoteRelationships.getRelatedObjects().get(i);
                LocalObjectLight[] relatedLocalObjects = new LocalObjectLight[relatedRemoteObjects.getList().size()];
                int j = 0;
                for (RemoteObjectLight relatedRemoteObject : relatedRemoteObjects.getList()) {
                    relatedLocalObjects[j] = new LocalObjectLight(relatedRemoteObject.getId(), 
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
    
    public HashMap<String, LocalObjectLight[]> getSpecialAttributesAsync (String objectClass, String objectId) {
        try{
            Response<GetSpecialAttributesResponse> response = service.getSpecialAttributesAsync(objectClass, objectId, session.getSessionId());
            while (!response.isDone())
                Thread.sleep(100);
        
            GetSpecialAttributesResponse getSpecialAttributesResponse = response.get();
            RemoteObjectSpecialRelationships remoteRelationships = getSpecialAttributesResponse.getReturn();
                        
            HashMap<String, LocalObjectLight[]> res = new HashMap<>();
            
            for (int i = 0; i < remoteRelationships.getRelationships().size(); i++){
                
                RemoteObjectLightList relatedRemoteObjects = remoteRelationships.getRelatedObjects().get(i);
                LocalObjectLight[] relatedLocalObjects = new LocalObjectLight[relatedRemoteObjects.getList().size()];
                int j = 0;
                for (RemoteObjectLight relatedRemoteObject : relatedRemoteObjects.getList()) {
                    relatedLocalObjects[j] = new LocalObjectLight(relatedRemoteObject.getId(), 
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

    public List<LocalObjectLight> getSpecialAttribute(String objectClass, String objectId, String attributeName){
        try{

            List<RemoteObjectLight> values = service.getSpecialAttribute(objectClass, objectId,attributeName, session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();
            for (RemoteObjectLight value : values)
                res.add(new LocalObjectLight(value.getId(), value.getName(), value.getClassName()));

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
    public LocalObjectLight getObjectInfoLight(String objectClass, String oid){
        try{
            RemoteObjectLight myLocalObject = service.getObjectLight(objectClass, oid,this.session.getSessionId());
            return new LocalObjectLight(myLocalObject.getClassName(), myLocalObject.getName(), 
                        myLocalObject.getId(), myLocalObject.getValidators());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalApplicationLogEntry[] getBusinessObjectAuditTrail(String objectClass, String oid){
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
        try {            
            List<LocalClassMetadataLight> resAsLocal = null;
            if (!ignoreCache)
                resAsLocal = cache.getPossibleChildrenCached(className);
            
            if (resAsLocal == null) {
                resAsLocal = new ArrayList<>();
                List<RemoteClassMetadataLight> resAsRemote = service.getPossibleChildren(className,this.session.getSessionId());

                for (RemoteClassMetadataLight cil : resAsRemote)
                    resAsLocal.add(new LocalClassMetadataLight(cil.getId(),
                                                cil.getClassName(),
                                                cil.getDisplayName(),
                                                cil.getParentClassName(),
                                                cil.isAbstract(),cil.isViewable(), cil.isListType(),
                                                cil.isCustom(), cil.isInDesign(),
                                                cil.getSmallIcon(), 
                                                cil.getColor()));
                
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
            List<RemoteClassMetadataLight> resAsRemote = service.getPossibleChildrenNoRecursive(className,this.session.getSessionId());
            List<LocalClassMetadataLight> resAsLocal = new ArrayList<>();

            for (RemoteClassMetadataLight cil : resAsRemote)
                resAsLocal.add(new LocalClassMetadataLight(cil.getId(),
                                            cil.getClassName(),
                                            cil.getDisplayName(),
                                            cil.getParentClassName(),
                                            cil.isAbstract(),cil.isViewable(), cil.isListType(),
                                            cil.isCustom(), cil.isInDesign(),
                                            cil.getSmallIcon(), 
                                            cil.getColor()));
            
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
                List<RemoteClassMetadataLight> resAsRemote = service.getPossibleSpecialChildren(className, session.getSessionId());

                for (RemoteClassMetadataLight cil : resAsRemote)
                    resAsLocal.add(new LocalClassMetadataLight(cil.getId(),
                        cil.getClassName(),
                        cil.getDisplayName(),
                        cil.getParentClassName(),
                        cil.isAbstract(),cil.isViewable(), cil.isListType(),
                        cil.isCustom(), cil.isInDesign(),
                        cil.getSmallIcon(), 
                        cil.getColor()));
                
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
            List<RemoteClassMetadataLight> resAsRemote = service.getPossibleSpecialChildrenNoRecursive(className, session.getSessionId());
            List<LocalClassMetadataLight> resAsLocal = new ArrayList<>();
            
            for (RemoteClassMetadataLight cil : resAsRemote)
                resAsLocal.add(new LocalClassMetadataLight(cil.getId(),
                    cil.getClassName(),
                    cil.getDisplayName(),
                    cil.getParentClassName(),
                    cil.isAbstract(),cil.isViewable(), cil.isListType(),
                    cil.isCustom(), cil.isInDesign(),
                    cil.getSmallIcon(), 
                    cil.getColor()));

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
            for (RemoteClassMetadataLight cil : service.getUpstreamContainmentHierarchy(className, recursive, this.session.getSessionId()))
                    res.add(new LocalClassMetadataLight(cil.getId(),
                                                cil.getClassName(),
                                                cil.getDisplayName(),
                                                cil.getParentClassName(),
                                                cil.isAbstract(),cil.isViewable(), cil.isListType(),
                                                cil.isCustom(), cil.isInDesign(),
                                                cil.getSmallIcon(), cil.getColor()));
            
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
            for (RemoteClassMetadataLight cil : service.getUpstreamSpecialContainmentHierarchy(className, recursive, session.getSessionId()))
                res.add(new LocalClassMetadataLight(cil.getId(),
                    cil.getClassName(),
                    cil.getDisplayName(),
                    cil.getParentClassName(),
                    cil.isAbstract(),cil.isViewable(), cil.isListType(),
                    cil.isCustom(), cil.isInDesign(),
                    cil.getSmallIcon(), cil.getColor()));
            return res;
        } catch (Exception ex) {
            error = ex.getMessage();
            return null;
        }
    }
    
    //<editor-fold desc="Contact Manager" defaultstate="collapsed">
    /**
     * Creates a contact
     * @param contactClass The class of the new contact. It must be a subclass of GenericContact
     * @param name The name of the contact
     * @param customerClassName The class of the customer this contact will be associated to
     * @param customerId The id of the customer this contact will be associated to
     * @return True if the operation was successful, false otherwise
     */
    public boolean createContact(String contactClass, String name, 
            String customerClassName, String customerId) {
        try {
            service.createContact(contactClass, Arrays.asList(new StringPair(Constants.PROPERTY_NAME, name)), 
                    customerClassName, customerId, session.getSessionId());
            
            return true;
        } catch(Exception ex) {
            error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Deletes a contact
     * @param contactClass Class of the contact
     * @param contactId Id of the contact
     * @return True if the operation was successful, false otherwise
     */
    public boolean deleteContact(String contactClass, String contactId) {
        try {
            service.deleteContact(contactClass, contactId, session.getSessionId());
            return true;
        } catch(Exception ex) {
            error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Searches for contacts given a search string, This string will be searched in the attribute values of all contacts
     * @param searchString The string to be searched. Use null or an empty string to retrieve all the contacts
     * @param maxResults Maximum number of results. Use -1 to retrieve all results at once
     * @return The list of contacts for whom at least one of their attributes matches or null in case of error
     */
    public List<LocalContact> searchForContacts(String searchString, int maxResults) {
        try {
            List<RemoteContact> remoteContacts = service.searchForContacts(searchString, maxResults, session.getSessionId());
            List<LocalContact> res = new ArrayList<>();
            for (RemoteContact remoteContact : remoteContacts) {
                LocalClassMetadata classMetadata = getMetaForClass(remoteContact.getClassName(), false);
                res.add(new LocalContact(remoteContact.getClassName(), remoteContact.getId(), remoteContact.getAttributes(), classMetadata, 
                        new LocalObjectLight(remoteContact.getCustomer().getId(), remoteContact.getCustomer().getName(), remoteContact.getCustomer().getClassName())));
            }
            
            return res;
        } catch(Exception e){
            error = e.getMessage();
            return null;
        }
    }
    
    /**
     * Fetches the contacts associated to a given customer
     * @param customerClass The class of the customer to retrieve the contacts from
     * @param customerId The id of the customer to retrieve the contacts from
     * @return The list of contacts associated to the customer provided
     */
    public List<LocalContact> getContactsForCustomer(String customerClass, String customerId) {
        try {
            List<RemoteContact> remoteContacts = service.getContactsForCustomer(customerClass, customerId, session.getSessionId());
            List<LocalContact> res = new ArrayList<>();
            for (RemoteContact remoteContact : remoteContacts) {
                LocalClassMetadata classMetadata = getMetaForClass(remoteContact.getClassName(), false);
                res.add(new LocalContact(remoteContact.getClassName(), remoteContact.getId(), remoteContact.getAttributes(), classMetadata, 
                        new LocalObjectLight(remoteContact.getCustomer().getId(), remoteContact.getCustomer().getName(), remoteContact.getCustomer().getClassName())));
            }
            
            return res;
        } catch(Exception e){
            error = e.getMessage();
            return null;
        }
    }
    //</editor-fold>
    
    //<editor-fold desc="File attachments" defaultstate="collapsed">
    /**
     * Attaches a file to an inventory object
     * @param name The name of the file. It's more like its title, instead of the file name
     * @param tags A semicolon (";") separated string with the tags associated to this document. These tags can be used to help find documents in a search
     * @param file The actual file
     * @param className The class name of the inventory object the file will be attached to
     * @param objectId The id of the inventory object the file will be attached to
     * @return The file object that was created or null if the file can not be saved or if there's already a file with that name related to the object
     */
    public LocalFileObjectLight attachFileToObject(String name, String tags, byte[] file, String className, String objectId) {
        try {
            long fileObjectId = service.attachFileToObject(name, tags, file, className, objectId, session.getSessionId());
            return new LocalFileObjectLight(fileObjectId, name, Calendar.getInstance().getTimeInMillis(), tags);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Detaches a file from an inventory object. Note that the file will also be deleted. 
     * @param fileObjectId The id of the file object
     * @param className The class of the object the file will be detached from
     * @param objectId The id of the object the file will be detached from
     * @return True if the operation was successful, false otherwise
     */
    public boolean detachFileFromObject(long fileObjectId, String className, String objectId) {
        try{
            service.detachFileFromObject(fileObjectId, className, objectId, session.getSessionId());
            return true;
        } catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Retrieves the files associated to a given inventory object
     * @param className The class of the object o retrieve the files from
     * @param objectId The id of the object o retrieve the files from
     * @return  The list of files or null in case of error
     */
    public List<LocalFileObjectLight> getFilesForObject(String className, String objectId) {
        try {
            List<LocalFileObjectLight> res = new ArrayList<>();
            List<RemoteFileObjectLight> remoteFiles = service.getFilesForObject(className, objectId, session.getSessionId());
            for (RemoteFileObjectLight remoteFile : remoteFiles)
                res.add(new LocalFileObjectLight(remoteFile.getFileOjectId(), remoteFile.getName(), remoteFile.getCreationDate(), remoteFile.getTags()));
            
            return res;
        } catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Retrieves a particular file from those attached to an inventory object. The returned object contains the contents of the file
     * @param fileObjectId The id of the file object
     * @param className The class of the object the file will be detached from
     * @param objectId The id of the object the file will be detached from
     * @return The object file encapsulating the contents of the file or null otherwise.
     */
    public LocalFileObject getFile(long fileObjectId, String className, String objectId) {
        try {
            RemoteFileObject remoteObject = service.getFile(fileObjectId, className, objectId, session.getSessionId());
            return new LocalFileObject(remoteObject.getFileOjectId(), remoteObject.getName(), 
                    remoteObject.getCreationDate(), remoteObject.getTags(), remoteObject.getFile());
        } catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Updates the properties of a file object (name or tags)
     * @param fileObjectId The id of the file object
     * @param propertyName  Name of the property ti be updated
     * @param propertyValue  Value of the property ti be updated
     * @param className The class of the object the file is attached to
     * @param objectId The id of the object the file is attached to
     * @return True if the operation was completed successfully, false otherwise
     */
    public boolean updateFileProperties(long fileObjectId, String propertyName, String propertyValue, String className, String objectId) {
        try {
            
            List<StringPair> remoteProperties = new ArrayList<>();
            remoteProperties.add(new StringPair(propertyName, propertyValue));
            
            service.updateFileProperties(fileObjectId, remoteProperties, className, objectId, session.getSessionId());
            
            return true;
        } catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    //</editor-fold>
    
    /**
     * According to the cached light metadata, finds out if a given class if subclass of another
     * @param className Class to be evaluated
     * @param allegedParentClassName Possible super class
     * @return is className subClass of allegedParentClass?
     */
    public boolean isSubclassOf(String className, String allegedParentClassName) {
        if (className == null || allegedParentClassName == null)
            return false;
        
        if (allegedParentClassName.equals("RootObject") || //NOI18N
            allegedParentClassName.equals("ApplicationObject")) //NOI18N
            return false;
        
        if (allegedParentClassName.equals(className))
            return true;
       
        LocalClassMetadataLight allegedParentClass = cache.getLightMetaForClass(allegedParentClassName);        
                
        if (allegedParentClass == null) {
            List<LocalClassMetadataLight> subclasses = getLightSubclasses(allegedParentClassName, true, true);
            if (subclasses == null)
                return false;
            
            for (LocalClassMetadataLight subclass : subclasses)
                cache.addLightMeta(new LocalClassMetadataLight[]{subclass});
        }
        allegedParentClass = cache.getLightMetaForClass(allegedParentClassName);        
        if (allegedParentClass == null) // The class name can not be found
            return false;
        
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
            List<RemoteClassMetadataLight> metas = service.getAllClassesLight(includeListTypes, this.session.getSessionId());

            LocalClassMetadataLight[] lm = new LocalClassMetadataLight[metas.size()];
            int i = 0;
            for (RemoteClassMetadataLight cil : metas) {
                lm[i] = new LocalClassMetadataLight(cil.getId(),
                                                cil.getClassName(),
                                                cil.getDisplayName(),
                                                cil.getParentClassName(),
                                                cil.isAbstract(),cil.isViewable(), cil.isListType(),
                                                cil.isCustom(), cil.isInDesign(),
                                                cil.getSmallIcon(), cil.getColor());
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
            List<RemoteClassMetadata> metas = service.getAllClasses(includeListTypes, this.session.getSessionId());
            LocalClassMetadata[] lm = new LocalClassMetadata[metas.size()];
            int i = 0;
            for (RemoteClassMetadata ci : metas) {
                
                lm[i] = new LocalClassMetadata(ci.getId(),
                                                ci.getClassName(),
                                                ci.getDisplayName(),
                                                ci.getParentClassName(),
                                                ci.isAbstract(),ci.isViewable(), ci.isListType(),
                                                ci.isCustom(), ci.isInDesign(),
                                                ci.getSmallIcon(), ci.getColor(), ci.getIcon(),
                                                ci.getDescription(), ci.getAttributesIds(), 
                                                ci.getAttributesNames().toArray(new String[0]),
                                                ci.getAttributesTypes().toArray(new String[0]),
                                                ci.getAttributesDisplayNames().toArray(new String[0]), ci.getAttributesDescriptions().toArray(new String[0]),
                                                ci.getAttributesMandatories(), ci.getAttributesMultiples(), 
                                                ci.getAttributesUniques(),
                                                ci.getAttributesVisibles(), ci.getAttributesOrders());
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
        try {
            LocalClassMetadata res;
            if (!ignoreCache){
                res = cache.getMetaForClass(className);
                if (res != null)
                    return res;
            }

            RemoteClassMetadata cm = service.getClass(className,this.session.getSessionId());

            res = new LocalClassMetadata(cm.getId(),
                                            cm.getClassName(),
                                            cm.getDisplayName(),
                                            cm.getParentClassName(),
                                            cm.isAbstract(),cm.isViewable(), cm.isListType(),
                                            cm.isCustom(), cm.isInDesign(),
                                            cm.getSmallIcon(), cm.getColor(), cm.getIcon(),
                                            cm.getDescription(), 
                    cm.getAttributesIds(),                         
                    cm.getAttributesNames().toArray(new String[0]),
                    cm.getAttributesTypes().toArray(new String[0]),
                    cm.getAttributesDisplayNames().toArray(new String[0]), 
                    cm.getAttributesDescriptions().toArray(new String[0]),
                    cm.getAttributesMandatories(),
                    cm.getAttributesMultiples(),
                    cm.getAttributesUniques(),
                    cm.getAttributesVisibles(),
                    cm.getAttributesOrders());
            cache.addMeta(new LocalClassMetadata[]{res});
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalClassMetadata getMetaForClassAsync(String className, boolean ignoreCache) {
        try {
            LocalClassMetadata res;
            if (!ignoreCache){
                res = cache.getMetaForClass(className);
                if (res != null)
                    return res;
            }
            
            Response<GetClassResponse> response = service.getClassAsync(className,this.session.getSessionId());
            while (!response.isDone())
                Thread.sleep(100);
            GetClassResponse getClassResponse = response.get();
            
            RemoteClassMetadata cm = getClassResponse.getReturn();
            
            res = new LocalClassMetadata(cm.getId(),
                                            cm.getClassName(),
                                            cm.getDisplayName(),
                                            cm.getParentClassName(),
                                            cm.isAbstract(),cm.isViewable(), cm.isListType(),
                                            cm.isCustom(), cm.isInDesign(),
                                            cm.getSmallIcon(), cm.getColor(), cm.getIcon(),
                                            cm.getDescription(), 
                    cm.getAttributesIds(),                         
                    cm.getAttributesNames().toArray(new String[0]),
                    cm.getAttributesTypes().toArray(new String[0]),
                    cm.getAttributesDisplayNames().toArray(new String[0]), 
                    cm.getAttributesDescriptions().toArray(new String[0]),
                    cm.getAttributesMandatories(),
                    cm.getAttributesMultiples(),
                    cm.getAttributesUniques(),
                    cm.getAttributesVisibles(),
                    cm.getAttributesOrders());
            cache.addMeta(new LocalClassMetadata[]{res});
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Retrieves the metadata for a given class. This information is never cached, 
     * as this method is intended to be used in situations where we need the fresh data from the server
     * @param classId classmetadata id
     * @return the metadata information
     */
    public LocalClassMetadata getMetaForClass(long classId) {
        try{
            LocalClassMetadata res;
            RemoteClassMetadata cm = service.getClassWithId(classId,this.session.getSessionId());
            
            res = new LocalClassMetadata(cm.getId(),
                        cm.getClassName(),
                        cm.getDisplayName(),
                        cm.getParentClassName(),
                        cm.isAbstract(),cm.isViewable(), cm.isListType(),
                        cm.isCustom(), cm.isInDesign(),
                        cm.getSmallIcon(), cm.getColor(), cm.getIcon(),
                        cm.getDescription(), cm.getAttributesIds(), 
                        cm.getAttributesNames().toArray(new String[0]),
                        cm.getAttributesTypes().toArray(new String[0]),
                        cm.getAttributesDisplayNames().toArray(new String[0]), 
                        cm.getAttributesDescriptions().toArray(new String[0]),
                        cm.getAttributesMandatories(), cm.getAttributesMultiples(),
                        cm.getAttributesUniques(),
                        cm.getAttributesVisibles(),
                        cm.getAttributesOrders());
            cache.addMeta(new LocalClassMetadata[]{ res });
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Checks if a class has a attribute with a given name
     * @param className Class name
     * @param attributeName Attribute name
     * @return True if the given class has the attribute
     */
    public boolean hasAttribute(String className, String attributeName) {
        if (className == null || attributeName == null)
            return false;
        
        try {
            return service.hasAttribute(className, attributeName, session.getSessionId());
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public LocalAttributeMetadata getAttribute(String className, String attributeName) {
        try {
            RemoteAttributeMetadata attrInfo = service.getAttribute(className, attributeName, session.getSessionId());
            
            LocalAttributeMetadata lam = new LocalAttributeMetadata(
                attrInfo.getId(), attrInfo.getName(), attrInfo.getType(), 
                attrInfo.getDisplayName(), attrInfo.getDescription(), attrInfo.isVisible(), attrInfo.isMandatory(), 
                attrInfo.isMultiple(),
                attrInfo.isUnique(), attrInfo.getOrder());
            
            return lam;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets the class hierarchy tree
     * @param showAll Return all classes
     * @return A byte array with an XML document representing the class hierarchy tree
     */
    public byte[] getClassHierarchy(boolean showAll) {
        try{
            return service.getClassHierarchy(showAll, session.getSessionId());
        } catch(Exception ex){
            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName() + ": " + ex.getMessage();
            return null;
        }
    }

    public List<LocalClassMetadataLight> getLightSubclasses(String className, boolean includeAbstractSubClasses, boolean includeSelf) {
        try {
            List<RemoteClassMetadataLight> subClasses = service.getSubClassesLight(className, includeAbstractSubClasses, includeSelf, session.getSessionId());
            List<LocalClassMetadataLight> res = new ArrayList<>();

            for (RemoteClassMetadataLight cil : subClasses)
                res.add(new LocalClassMetadataLight(cil.getId(),
                                cil.getClassName(),
                                cil.getDisplayName(),
                                cil.getParentClassName(),
                                cil.isAbstract(),cil.isViewable(), cil.isListType(),
                                cil.isCustom(), cil.isInDesign(),
                                cil.getSmallIcon(), cil.getColor()));
            return res;
        } catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
     public List<LocalClassMetadataLight> getLightSubclassesNoRecursive(String className, boolean includeAbstractSubClasses, boolean includeSelf) {
        try {
            List<RemoteClassMetadataLight> subClasses = service.getSubClassesLightNoRecursive(className, includeAbstractSubClasses, includeSelf, session.getSessionId());
            List<LocalClassMetadataLight> res = new ArrayList<>();

            for (RemoteClassMetadataLight cil : subClasses)
                res.add(new LocalClassMetadataLight(cil.getId(),
                                cil.getClassName(),
                                cil.getDisplayName(),
                                cil.getParentClassName(),
                                cil.isAbstract(),cil.isViewable(), cil.isListType(),
                                cil.isCustom(), cil.isInDesign(),
                                cil.getSmallIcon(), cil.getColor()));
            
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
            String myObjectId = service.createListTypeItem(className, "", "", this.session.getSessionId());
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
                    res.add(new LocalObjectListItem(entry.getId(),entry.getClassName(),entry.getName()));
                
                //Warning, the null value is always cached
                cache.addListCached(className, res);
            }
            if (includeNullValue)
                return res;
            else
                return res.subList(1, res.size());
            
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }// </editor-fold>
    
    public LocalObjectListItem getCustomShape(String customShapeId, boolean ignoreCache) {        
        if (!ignoreCache) {
            for (LocalObjectListItem customShape : cache.getCustomShapes()) {
                if (customShape.getId().equals(customShapeId))
                    return customShape;
            }
        }
        getCustomShapes(false);
        
        for (LocalObjectListItem customShape : cache.getCustomShapes()) {
            if (customShape.getId().equals(customShapeId))
                return customShape;
        }
        return null;
    }
    
    public LocalObjectView getCustomShapeLayout(String customShapeId, boolean ignoreCache) {
        LocalObjectListItem customShape = getCustomShape(customShapeId, false);
        
        if (customShape != null) {
            if (!ignoreCache) {    
                LocalObjectView layout = cache.getCustomShapeLayout(customShape);

                if (layout != null)
                    return layout;
            }
            List<LocalObjectViewLight> views = getListTypeItemRelatedViews(customShape.getId(), Constants.CLASS_CUSTOMSHAPE);

            if (views != null) {
                if (!views.isEmpty()) {
                    LocalObjectView view = getListTypeItemRelatedView(customShape.getId(), Constants.CLASS_CUSTOMSHAPE, views.get(0).getId());

                    if (view != null) {
                        cache.setCustomShapeLayout(customShape, view);
                        return view;
                    }
                }
            }
        }
        return null;
    }
    
    public List<LocalObjectListItem> getCustomShapes(boolean ignoreCache) {
        List<LocalObjectListItem> customShapes = null;
        if (!ignoreCache) {
            customShapes = new ArrayList<>();
            
            for (LocalObjectListItem customShape : cache.getCustomShapes())
                customShapes.add(customShape);
            
            if (!customShapes.isEmpty())
                return customShapes;
        }
        customShapes = getList(Constants.CLASS_CUSTOMSHAPE, false, ignoreCache);
        
        if (customShapes != null) {
            for (LocalObjectListItem customShape : customShapes)
                cache.setCustomShapeLayout(customShape, null);
        }
        return customShapes;    
    }
    
    //<editor-fold defaultstate="collapsed" desc="Tasks">
    /**
     * Creates a task
     * @param name Task name
     * @param description Task description
     * @param enabled Is the task enabled?
     * @param commitOnExecute Should the changes made in this task (if any) be committed to the database after its execution?
     * @param script Task script
     * @param parameters Task parameters as pairs param name/param value
     * @param schedule Schedule descriptor
     * @param notificationType Notification type descriptor
     * @return A local representation of the task if the operation was successful, null otherwise
     */
    public LocalTask createTask(String name, String description, boolean enabled, boolean commitOnExecute,
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
                    StringPair remoteParameter = new StringPair(parameter, parameters.get(parameter));
                    remoteParameters.add(remoteParameter);
                }
            }
                
            long taskId = service.createTask(name, description, enabled, commitOnExecute, script, 
                   remoteParameters, atsd, tnd, session.getSessionId());
            
            return new LocalTask(taskId, name, description, enabled, commitOnExecute, script, 
                    null, schedule, notificationType, new ArrayList<>());
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
                StringPair remoteParameter = new StringPair(parameterName, parameters.get(parameterName));
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
                        remoteTask.getDescription(), remoteTask.isEnabled(), remoteTask.isCommitOnExecute(), remoteTask.getScript(), 
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
     * Create an Inventory Object
     * @param objectClass
     * @param parentClass
     * @param parentOid
     * @param attributes
     * @param templateId
     * @return 
     */
    public LocalObjectLight createObject(String objectClass, String parentClass, 
            String parentOid, HashMap<String, Object> attributes, String templateId)
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
            String objectId  = service.createObject(objectClass, parentClass, 
                    parentOid, attributeNames,
                    attributeValues,
                    templateId, this.session.getSessionId());
            return new LocalObjectLight(objectId, null, objectClass);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight createSpecialObject(String className, String parentClassName, 
            String parentOid, HashMap<String, Object> attributes, String templateId) {
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
            String objectId  = service.createSpecialObject(className,parentClassName, parentOid, 
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
        try {
            List<Long> pChildren = new ArrayList<>();
            for (long pChild : possibleChildren)
                pChildren.add(pChild);
            
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
     * Deletes a single object safely 
     * @param className Object class
     * @param oid Object Id
     * @param releaseRelationships Should this method check if there are existing relationships and stop the operation if any?
     * @return  True if the operation was successful, an exception if not
     */
    public boolean deleteObject(String className, String oid, boolean releaseRelationships) {
        try {
            service.deleteObject(className, oid, releaseRelationships, this.session.getSessionId());
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
    public boolean deleteObjects(List<String> classNames, List<String> oids){
        try {
            service.deleteObjects(classNames, oids, false, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }

    public boolean moveObjectsToPool(String targetClass, String targetOid, LocalObjectLight[] objects) {

        try{
            List<String> objectOids = new ArrayList<>();
            List<String> objectClasses = new ArrayList<>();

            for (LocalObjectLight lol : objects){
                objectOids.add(lol.getId());
                objectClasses.add(lol.getClassName());
            }
            service.moveObjectsToPool(targetClass, targetOid, objectClasses, objectOids, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean moveObjects(String targetClass, String targetOid, List<LocalObjectLight> objects) {
        try{
            List<String> objectOids = new ArrayList<>();
            List<String> objectClasses = new ArrayList<>();

            for (LocalObjectLight lol : objects){
                objectOids.add(lol.getId());
                objectClasses.add(lol.getClassName());
            }
            service.moveObjects(targetClass, targetOid, objectClasses, objectOids,this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean moveSpecialObjects(String targetClass, String targetOid, List<LocalObjectLight> objects) {
        try{
            List<String> objectOids = new ArrayList<>();
            List<String> objectClasses = new ArrayList<>();

            for (LocalObjectLight lol : objects){
                objectOids.add(lol.getId());
                objectClasses.add(lol.getClassName());
            }
            service.moveSpecialObjects(targetClass, targetOid, objectClasses, objectOids,this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Move a pool item from a pool to another pool
     * @param poolId The id of the pool node
     * @param poolItemClassName The class name for the pool item
     * @param poolItemId The id for the pool item
     * @return True if the pool item was moved
     */
    public boolean movePoolItem(String poolId, String poolItemClassName, String poolItemId) {
        try {
            service.movePoolItemToPool(poolId, poolItemClassName, poolItemId, session.getSessionId());
            return true;
        } catch (Exception ex) {
            error = ex.getMessage();
            return false;
        }
    }

    public LocalObjectLight[] copyObjects(String targetClass, String targetOid, LocalObjectLight[] objects){
        try{
            List<String> objectOids = new ArrayList<>();
            List<String> objectClasses = new ArrayList<>();

            for (LocalObjectLight lol : objects){
                objectOids.add(lol.getId());
                objectClasses.add(lol.getClassName());
            }

            //Let's do the copy recursive by default
            List<String> objs = service.copyObjects(targetClass, targetOid, objectClasses, objectOids, true, this.session.getSessionId());

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
    
    public LocalObjectLight[] copySpecialObjects(String targetClass, String targetOid, LocalObjectLight[] objects){
        try{
            List<String> objectOids = new ArrayList<>();
            List<String> objectClasses = new ArrayList<>();

            for (LocalObjectLight lol : objects){
                objectOids.add(lol.getId());
                objectClasses.add(lol.getClassName());
            }

            //Let's do the copy recursive by default
            List<String> objs = service.copySpecialObjects(targetClass, targetOid, objectClasses, objectOids, true, this.session.getSessionId());

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
    
    /**
     * Copy a pool item from a pool to another pool
     * @param poolId The id of the pool node
     * @param poolItemClassName The class name for the pool item
     * @param poolItemId The id for the pool item
     * @param recursive If this operation should also copy the children objects recursively
     * @return True if the pool item was moved
     */
    public boolean copyPoolItem(String poolId, String poolItemClassName, String poolItemId, boolean recursive) {
        try {
            service.copyPoolItemToPool(poolId, poolItemClassName, poolItemId, recursive, session.getSessionId());
            return true ;
        } catch (Exception ex) {
            error = ex.getMessage();
            return false;
        }
    }
    
    public List<LocalAttributeMetadata> getMandatoryAttributesInClass(String className){
        try {
            List<RemoteAttributeMetadata> mandatoryObjectAttributesInfo = service.getMandatoryAttributesInClass(className, session.getSessionId());
            List<LocalAttributeMetadata> mandatoryObjectAttributes = new ArrayList<>();
            for (RemoteAttributeMetadata mandatoryObjectAttributeInfo :  mandatoryObjectAttributesInfo)
                mandatoryObjectAttributes.add(new LocalAttributeMetadata(mandatoryObjectAttributeInfo.getId(),
                        mandatoryObjectAttributeInfo.getName(), mandatoryObjectAttributeInfo.getType(), 
                        mandatoryObjectAttributeInfo.getDisplayName(), mandatoryObjectAttributeInfo.getDescription(), 
                        mandatoryObjectAttributeInfo.isVisible(), mandatoryObjectAttributeInfo.isMandatory(), mandatoryObjectAttributeInfo.isMultiple(),
                        mandatoryObjectAttributeInfo.isUnique(), mandatoryObjectAttributeInfo.getOrder()));
            return mandatoryObjectAttributes;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
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
    public List<LocalObjectLight> createBulkObjects(String className, String parentClassName, String parentOid, int numberOfObjects, String namePattern) {
        try {
            List<String> ids = service.createBulkObjects(className, parentClassName, parentOid, numberOfObjects, namePattern, session.getSessionId());
            
            List<LocalObjectLight> newObjects = new ArrayList<>();
            
            for (String id : ids) {
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
    public List<LocalObjectLight> createBulkSpecialObjects(String className, String parentClassName, String parentId, int numberOfSpecialObjects, String namePattern) {
        try {
            List<String> ids = service.createBulkSpecialObjects(className, parentClassName, parentId, numberOfSpecialObjects, namePattern, session.getSessionId());
            
            List<LocalObjectLight> newSpecialObjects = new ArrayList<>();
            
            for (String id : ids) {
                newSpecialObjects.add(getObjectInfoLight(className, id));
            }
            
            return newSpecialObjects;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
        
    public boolean connectMirrorPort (List<String> aObjectClass, 
            List<String> aObjectId, List<String> bObjectClass, List<String> bObjectId) {
        try{
            service.connectMirrorPort(aObjectClass, aObjectId, bObjectClass, bObjectId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean releaseMirrorPort (String objectClass, String objectId) {
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
     * @param name Name of the new connection. Leave empty if you want to use the name in the template
     * @param connectionClass Class for the corresponding connection to be created
     * @param templateId Id of the template for the connectionClass. Use -1 to create a connection without template
     * @return A local object light representing the new connection
     */
    public LocalObjectLight createPhysicalConnection(String endpointAClass, String endpointAId,
            String endpointBClass, String endpointBId, String name, String connectionClass, String templateId) {
        try {
            String myObjectId = service.createPhysicalConnection(endpointAClass, endpointAId,
                    endpointBClass, endpointBId, name, connectionClass, templateId, this.session.getSessionId());
            return new LocalObjectLight(myObjectId, name, connectionClass);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight[] getPhysicalConnectionEndpoints(String connectionClass, String connectionId) {
        try{
            List<RemoteObjectLight> endpoints = service.getPhysicalConnectionEndpoints(connectionClass, connectionId, session.getSessionId());
            LocalObjectLight[] res = new LocalObjectLight[]{endpoints.get(0) == null ? 
                    null : new LocalObjectLight(endpoints.get(0).getId(), endpoints.get(0).getName(), endpoints.get(0).getClassName()),
                    endpoints.get(1) == null ? 
                    null : new LocalObjectLight(endpoints.get(1).getId(), endpoints.get(1).getName(), endpoints.get(1).getClassName())};
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    
    public LocalObjectView validateSavedE2EView(List<String> linkClasses, List<String> linkIds, LocalObjectView savedView) { 
        try {
            RemoteViewObject remoteSavedObjectView = new RemoteViewObject();
            
            remoteSavedObjectView.setId(savedView.getId());
            remoteSavedObjectView.setName(savedView.getName());
            remoteSavedObjectView.setName(savedView.getName());
            remoteSavedObjectView.setClassName(savedView.getClassName());
            remoteSavedObjectView.setDescription(savedView.getDescription());
            remoteSavedObjectView.setStructure(savedView.getStructure());
            
            remoteSavedObjectView.setName(savedView.getName());
            RemoteViewObject updatedView = service.validateSavedE2EView(linkClasses, linkIds, remoteSavedObjectView, session.getSessionId());
            return new LocalObjectView(savedView.getId(),
                                        savedView.getName(),
                                        savedView.getClassName(),
                                        savedView.getDescription(), updatedView.getStructure(), null);
        }catch(Exception ex) {
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Returns the end to end view staring from connections
     * Provides the endpoints and the next ports connected to such endpoints using a physical connection
     * also returns the continuity of VLANs and BDIs at the edge of those physical paths. 
     * @param linkClasses The classes of the connection to be evaluated
     * @param linkIds The ids of the connection to be evaluated
     * @return An object with the details of the connection and the physical resources associated to it. Null in case of error
     */
    public LocalObjectView getE2EView(List<String> linkClasses, List<String> linkIds) { 
        try {

            RemoteViewObject e2EView = service.getE2EView(linkClasses, linkIds, true, true, session.getSessionId());
            return new LocalObjectView(e2EView.getId(), e2EView.getClassName(),
                    e2EView.getName(), e2EView.getDescription(), e2EView.getStructure(), null);
            
        }catch(Exception ex) {
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
//    public LocalLogicalConnectionDetails getLogicalLinkDetails(String linkClass, String linkId) { 
//        try {
//            
//            return new LocalLogicalConnectionDetails(service.getLogicalLinkDetails(linkClass, linkId, session.getSessionId()));
//        }catch(Exception ex) {
//            this.error =  ex.getMessage();
//            return null;
//        }
//    }
    
    /**
     * Returns the structure of a logical connection. The current implementation is quite simple and the return object 
     * simply provides the endpoints and the next ports connected to such endpoints using a physical connection
     * @param linkClass The class of the connection to be evaluated
     * @param linkId The id of the connection to be evaluated
     * @return An object with the details of the connection and the physical resources associated to it. Null in case of error
     */
//    public LocalPhysicalConnectionDetails getPhysicalLinkDetails(String linkClass, String linkId) { 
//        try {
//            return new LocalPhysicalConnectionDetails(service.getPhysicalLinkDetails(linkClass, linkId, session.getSessionId()));
//        }catch(Exception ex) {
//            this.error =  ex.getMessage();
//            return null;
//        }
//    }
    
    public LocalObjectLight[] getPhysicalPath(String objectClass, String objectId) {
        try{
            List<RemoteObjectLight> trace = service.getPhysicalPath(objectClass, objectId, session.getSessionId());
            LocalObjectLight[] res = new LocalObjectLight[trace.size()];
            int i = 0;
            for (RemoteObjectLight element : trace){
                res[i] = new LocalObjectLight(element.getId(), element.getName(), element.getClassName());
                i++;
            }
            
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Convenience method that returns the link connected to a port (if any). It serves to avoid calling {@link getSpecialAttribute} two times.
     * @param portClassName The class of the port
     * @param portId The id of the port
     * @return The link connected to the port or null if there isn't any
     * @throws InventoryException If the port could not be found or if the class provided does not exist or if The class provided is not a subclass of GenericPort
     */
    public LocalObject getLinkConnectedToPort(String portClassName, String portId) throws InventoryException {
        try {
            RemoteObject remoteLink = service.getLinkConnectedToPort(portClassName, portId, session.getSessionId());
            
            if (remoteLink == null)
                return null;
            
            else {
                LocalClassMetadata classMetadata = getMetaForClass(remoteLink.getClassName(), false);
                return new LocalObject(remoteLink.getClassName(), remoteLink.getId(), 
                    remoteLink.getAttributes(), classMetadata);
            }
            
        }catch(Exception ex) {
            throw new InventoryException((ex.getMessage()));
        }
    }
    
    /**
     * Retrieves the existing containers between two given nodes. . 
     * Only the ports with connections (physicalPath.size > 1) are returned
     * @param objectAClass The class of the object A.
     * @param objectAId The id of the object A.
     * @param objectBClass The class of the object B. (end point B class)
     * @param objectBId The id of the object B (end point B id).
     * @param containerClass The class of the containers to be return.
     * @return The list of physical paths of the connected ports inside the given objects or null in case of error.
     */
    public List<LocalObjectLight> getContainersBetweenObjects(String objectAClass, String objectAId,
            String objectBClass, String objectBId, String containerClass){
        try{
            List<RemoteObjectLight> existingContainers = service.getContainersBetweenObjects(objectAClass, objectAId, objectBClass, objectBId, containerClass, session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();
            for (RemoteObjectLight container : existingContainers) 
                res.add(new LocalObjectLight(container.getId(), container.getName(), container.getClassName()));
            
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Loops through all instances of GenericCommunicationsPort at any level inside the given object and gets the physical path. 
     * Only the ports with connections (physicalPath.size > 1) are returned
     * @param objectClass The object class
     * @param objectId The object id
     * @return The list of physical paths of the connected ports inside the given objects or null in case of error.
     */
    public List<LocalObjectLightList> getPhysicalConnectionsInObject(String objectClass, String objectId){
        try{
            List<RemoteObjectLightList> paths = service.getPhysicalConnectionsInObject(objectClass, objectId, session.getSessionId());
            List<LocalObjectLightList> res = new ArrayList<>();
            for (RemoteObjectLightList route : paths) 
                res.add(new LocalObjectLightList(route));
            
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public boolean deletePhysicalConnection(String objectClass, String objectId) {
        try {
            service.deletePhysicalConnection(objectClass, objectId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    public boolean connectPhysicalLinks(List<String> sideAClassNames, List<String> sideAIds, 
                List<String> linksClassNames, List<String> linksIds, List<String> sideBClassNames, 
                List<String> sideBIds) {
        try {            
            service.connectPhysicalLinks(sideAClassNames, sideAIds, linksClassNames, linksIds, sideBClassNames, sideBIds, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
//    public boolean connectLogicalLinks(List<String> sideAClassNames, List<String> sideAIds, 
//                List<String> linksClassNames, List<String> linksIds, List<String> sideBClassNames, 
//                List<String> sideBIds) {
//        try {            
//            service.connectLogicalLinks(sideAClassNames, sideAIds, linksClassNames, linksIds, sideBClassNames, sideBIds, session.getSessionId());
//            return true;
//        }catch(Exception ex){
//            this.error =  ex.getMessage();
//            return false;
//        }
//    }
//    
    public boolean connectPhysicalContainers(List<String> sideAClassNames, List<String> sideAIds, 
                List<String> containersClassNames, List<String> containersIds, List<String> sideBClassNames, 
                List<String> sideBIds) {
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
    public boolean disconnectPhysicalConnection(String connectionClass, String connectionId, int sideToDisconnect) {
        try {
            service.disconnectPhysicalConnection(connectionClass, connectionId, sideToDisconnect, session.getSessionId());
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
    public boolean disconnectLogicalConnection(String connectionClass, String connectionId, int sideToDisconnect) {
        try {
            service.disconnectPhysicalConnection(connectionClass, connectionId, sideToDisconnect, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    /**
     * Changes one or both sides (endpoints) of a physical connection (link or container). Use this method carefully in containers, as it does not check 
     * if the endpoints of the links inside the container that was reconnected are consistent with its new endpoints. Also note that 
     * when used in physical links, the link will NOT be moved (as in the special containment hierarchy) to the nearest common parent of both endpoints. 
     * This method can not be used to <i>disconnect</i> connections, to do that use {@link #disconnectPhysicalConnection(java.lang.String, long, int, java.lang.String) }.
     * @param connectionClass The class of the connection to be modified
     * @param connectionId The id of the connection to be modified
     * @param newASideClass The class of the new side A of the connection. Use null if this side is not to be changed.
     * @param newASideId The id of the new side A of the connection. Use -1 if this side is not to be changed.
     * @param newBSideClass The class of the new side B of the connection. Use null if this side is not to be changed.
     * @param newBSideId The id of the new side B of the connection. Use -1 if this side is not to be changed.
     * @return false, If any of the objects provided could not be found or if the new endpoint is not a port (if reconnecting a link) or if it is a port 
     * (if reconnecting a container). True in case of success
     */
    public boolean reconnectPhysicalConnection(String connectionClass, String connectionId, 
                                      String newASideClass, String newASideId,
                                      String newBSideClass, String newBSideId) {
        try {
            service.reconnectPhysicalConnection(connectionClass, connectionId, newASideClass, newASideId,
                                      newBSideClass, newBSideId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    //Service Manager
    public boolean associateObjectsToService(List<String> classNames, List<String> objectIds, String serviceClass, String serviceId){
        try{
            List<String> objectsClassList = new ArrayList<>();
            List<String> objectsIdList = new ArrayList<>();
            objectsClassList.addAll(classNames);
            objectsIdList.addAll(objectIds);
            service.associateObjectsToService(objectsClassList, objectsIdList, serviceClass, serviceId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    public boolean releaseObjectFromService(String serviceClass, String serviceId, String targetId){
        try{
            service.releaseObjectFromService(serviceClass, serviceId, targetId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    public List<LocalObjectLight> getServiceResources(String serviceClass, String serviceId){
        try{
            List <RemoteObjectLight> instances = service.getServiceResources(serviceClass, serviceId, this.session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : instances)
                res.add(new LocalObjectLight(rol.getId(), rol.getName(), rol.getClassName()));

            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    //End Service Manager
    
    public List<LocalObjectLight> getObjectSpecialChildren(String objectClass, String objectId) {
        try {
            List<RemoteObjectLight> specialChildren = service.getObjectSpecialChildren (
                    objectClass, objectId, session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();
            
            for (RemoteObjectLight rol : specialChildren)
                res.add(new LocalObjectLight(rol.getId(), rol.getName(), rol.getClassName()));

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
            for (int i = 1; i < res.length ; i++) {
                LocalObjectLight anObjectLight = new LocalObjectLight(myResult.get(i).getObject().getClassName(),
                        myResult.get(i).getObject().getName(), myResult.get(i).getObject().getId(), myResult.get(i).getObject().getValidators());
                
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
    }
    // </editor-fold>

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
            if (refreshMeta) {
                for (LocalClassMetadata lcm : cache.getMetadataIndex()){
                    RemoteClassMetadata cm = service.getClass(lcm.getClassName(),this.session.getSessionId());
            
                    LocalClassMetadata myLocal = new LocalClassMetadata(cm.getId(),
                            cm.getClassName(),
                            cm.getDisplayName(),
                            cm.getParentClassName(),
                            cm.isAbstract(),cm.isViewable(), cm.isListType(),
                            cm.isCustom(), cm.isInDesign(),
                            cm.getSmallIcon(), cm.getColor(), cm.getIcon(),
                            cm.getDescription(), cm.getAttributesIds(), 
                            cm.getAttributesNames().toArray(new String[0]),
                            cm.getAttributesTypes().toArray(new String[0]),
                            cm.getAttributesDisplayNames().toArray(new String[0]), 
                            cm.getAttributesDescriptions().toArray(new String[0]),
                            cm.getAttributesMandatories(), cm.getAttributesMultiples(),
                            cm.getAttributesUniques(),
                            cm.getAttributesVisibles(),
                            cm.getAttributesOrders());
                    
                    cache.addMeta(new LocalClassMetadata[]{myLocal});
                }
            }
            if (refreshLightMeta) 
                getAllLightMeta(true);
            
            if (refreshList){
                HashMap<String, List<LocalObjectListItem>> myLocalList = cache.getAllListTypes();
                for (String key : myLocalList.keySet()){
                    myLocalList.remove(key);
                    getList(key,false,true);
                }
            }
            if (refreshPossibleChildren)
                cache.getAllPossibleChildren().clear();
            
            if (refreshPossibleSpecialChildren) 
                cache.getAllPossibleSpecialChildren().clear();
                
        }catch(Exception ex){
            this.error = ex.getMessage();
        }
    }
    
    public boolean setAttributeProperties(long classId, String className, 
            long attributeId, String name, String displayName, String type,
            String description, Boolean administrative, Boolean mandatory, Boolean multiple,
            Boolean noCopy, Boolean readOnly, Boolean unique,  Boolean visible, Integer order)
    {
        try{
            service.setAttributePropertiesForClassWithId(classId, attributeId, 
                    name, displayName, description, type, administrative, 
                    mandatory, multiple, noCopy, readOnly, unique, visible, order,
                    this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
        
    public long createClassMetadata(String className, String displayName, String description, 
            String parentClassName, boolean custom, boolean countable, int color, boolean isAbstract, boolean inDesign){
        try {
            return service.createClass(className, displayName, description, isAbstract, custom, countable, inDesign, parentClassName, null, null, color, this.session.getSessionId());
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
                                boolean unique, boolean mandatory, boolean multiple, int order){
        try{
            service.createAttributeForClassWithId(classId, name, displayName, 
                    type, description, administrative, visible, readOnly, 
                    noCopy, unique, mandatory, multiple, order, this.session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
        return true;
    }
    
    public boolean createAttribute(String className, String name, String displayName, 
                                String description, String type, boolean administrative,
                                boolean readOnly, boolean visible, boolean noCopy, 
                                boolean unique, boolean mandatory, boolean multiple, int order){
        try {
            service.createAttribute(className, name, displayName, type, 
                    description, administrative, visible, readOnly, noCopy, 
                    unique, mandatory, multiple, order, this.session.getSessionId());
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
     * Retrieves all the list types of a class
     * @param className the given class name of the list type
     * @param id the given list type id
     * @return an array with all possible list types of a given class
     */
    public LocalObjectListItem getListTypeItem(String className, String id) {
       try{
            LocalObjectListItem res = new LocalObjectListItem();
            List<LocalObjectListItem> cachedLists = new ArrayList<>();
            List<RemoteObjectLight> remoteList = service.getListTypeItems(className,this.session.getSessionId());

            for (RemoteObjectLight r : remoteList) {
                if(r.getId().equals(id)){
                    res.setOid(r.getId());
                    res.setName(r.getName());
                    res.setDisplayName(r.getName());
                }
                LocalObjectListItem localObjectListItem = new LocalObjectListItem();
                localObjectListItem.setOid(r.getId());
                localObjectListItem.setName(r.getName());
                localObjectListItem.setDisplayName(r.getName());
                cachedLists.add(localObjectListItem);
            }
            cache.addListCached(className, cachedLists);

            return res; 
            
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Retrieves the list types
     * @return an array with all possible instanceable list types
     */
    public List<LocalClassMetadataLight> getInstanceableListTypes() {
        try{
            List<RemoteClassMetadataLight> listTypes;
            listTypes = service.getInstanceableListTypes(this.session.getSessionId());
            
            List<LocalClassMetadataLight> res = new ArrayList<>();
            for (RemoteClassMetadataLight cil : listTypes)
                res.add(new LocalClassMetadataLight(cil.getId(),
                                cil.getClassName(),
                                cil.getDisplayName(),
                                cil.getParentClassName(),
                                cil.isAbstract(),cil.isViewable(), cil.isListType(),
                                cil.isCustom(), cil.isInDesign(), 
                                cil.getSmallIcon(), cil.getColor()));
            
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
    public boolean deleteListTypeItem(String className, String oid, boolean force){
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
     * @param groupName Group's name
     * @param groupDescription Group's description
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
     * Creates a view for a given list type item. If there's already a view of the provided view type, it will be overwritten
     * @param listTypeItemId list type item id
     * @param listTypeItemClassName list type item class name
     * @param viewClassName view class name
     * @param name view name
     * @param description view description
     * @param structure XML document with the view structure
     * @param background background image
     * @return The id of the new view.
     */
    public long createListTypeItemRelatedView(String listTypeItemId, String listTypeItemClassName, String viewClassName, 
        String name, String description, byte [] structure, byte [] background) {
        
        try{
            return service.createListTypeItemRelatedView(listTypeItemId, listTypeItemClassName, viewClassName, name, description, structure, background, session.getSessionId());
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return -1;
        }
    }
    
    /**
     * Updates a view for a given list type item. If there's already a view of the provided view type, it will be overwritten
     * @param listTypeItemId list type item id
     * @param listTypeItemClass list type item class
     * @param viewId viewId
     * @param name view name
     * @param description view description
     * @param structure XML document with the view structure
     * @param background Background image. If null, the previous will be removed, if 0-sized array, it will remain unchanged
     * @return true if the related view was updated
     */
    public boolean updateListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId, 
        String name, String description, byte[] structure, byte[] background) {
        
        try{
            service.updateListTypeItemRelatedView(listTypeItemId, listTypeItemClass, viewId, name, description, structure, background, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    /**
     * Gets a view related to an list type item, such as the default, rack or equipment views
     * @param listTypeItemId list type item id
     * @param listTypeItemClass list type item class
     * @param viewId view id
     * @return The associated view (there should be only one of each type). Null if there's none yet
     */
    public LocalObjectView getListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId) {
        try{
            RemoteViewObject view = service.getListTypeItemRelatedView(listTypeItemId, listTypeItemClass, viewId, session.getSessionId());
            return new LocalObjectView(view.getId(), view.getClassName(), view.getName(), view.getDescription(), view.getStructure(), view.getBackground());
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets the views related to a list type item, such as the default, rack or equipment views
     * @param listTypeItemId list type item id
     * @param listTypeItemClass list type class name
     * @return The associated views
     */
    public List<LocalObjectViewLight> getListTypeItemRelatedViews(String listTypeItemId, String listTypeItemClass) {
        try{
            List<RemoteViewObjectLight> views = service.getListTypeItemRelatedViews(listTypeItemId, listTypeItemClass, -1, session.getSessionId());
            List<LocalObjectViewLight> res = new ArrayList<>();
            
            for (RemoteViewObjectLight view : views)
                res.add(new LocalObjectViewLight(view.getId(), view.getName(), view.getDescription(), view.getClassName()));
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets the views related to a list type item, such as the default, rack or equipment views
     * @param listTypeItemId list type item id
     * @param listTypeItemClass list type class name
     * @param viewId Related view id
     * @return true if the related view was deleted
     */
    public boolean deleteListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId) {
        try{
            service.deleteListTypeItemRelatedView(listTypeItemId, listTypeItemClass, viewId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    /**
     * Gets the list of template elements with a device layout
     * @return the list of template elements with a device layout
     */
    public List<LocalObjectLight> getDeviceLayouts() {
        try {
            List<RemoteObjectLight> remoteTemplateElements = service.getDeviceLayouts(session.getSessionId());
            
            List<LocalObjectLight> templateElements = new ArrayList<>();
            
            for (RemoteObjectLight remoteTemplateElement : remoteTemplateElements)
                templateElements.add(new LocalObjectLight(remoteTemplateElement.getId(), remoteTemplateElement.getName(), remoteTemplateElement.getClassName()));
            
            return templateElements;
        } catch (Exception ex) {
            error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Gets the device layout structure
     * @param oid object id
     * @param className class of object
     * @return the structure of the device layout
     */
    public byte[] getDeviceLayoutStructure(String oid, String className) {
        try {
            return service.getDeviceLayoutStructure(oid, className, session.getSessionId());
        } catch (Exception ex) {
            error = ex.getMessage();
            return null;
        }
    }

    /**
     * Get a view related to an object, such as the default, rack or equipment views
     * @param oid object's id
     * @param objectClass object's class
     * @param viewId view id
     * @return The associated view (there should be only one of each type). Null if there's none yet
     */
    public LocalObjectView getObjectRelatedView(String oid, String objectClass, long viewId){
        try{
            RemoteViewObject view = service.getObjectRelatedView(oid, objectClass, viewId, session.getSessionId());
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
    public List<LocalObjectViewLight> getObjectRelatedViews(String oid, String objectClass){
        try{
            List<RemoteViewObjectLight> views = service.getObjectRelatedViews(oid, objectClass, -1, 10, session.getSessionId());
            List<LocalObjectViewLight> res = new ArrayList<>();
            
            for (RemoteViewObjectLight view : views)
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
            List<RemoteViewObjectLight> views = service.getGeneralViews(viewClass, -1, session.getSessionId());
            List<LocalObjectViewLight> res = new ArrayList<>();
            for (RemoteViewObjectLight view : views)
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
            RemoteViewObject view = service.getGeneralView(viewId, session.getSessionId());
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
    public long createObjectRelatedView(String oid, String objectClass, String name, String description, String viewClassName, byte[] structure, byte[] background){
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
     * @param viewId
     * @param name view name
     * @param description view description
     * @param structure XML document with the view structure (see http://neotropic.co/kuwaiba/wiki/index.php?title=XML_Documents#To_Save_Object_Views for details about the supported format)
     * @param background Background image. If null, the previous will be removed, if 0-sized array, it will remain unchanged
     * @return 
     */
    public boolean updateObjectRelatedView(String oid, String objectClass, long viewId, String name, String description, byte[] structure, byte[] background){
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
     * @param oid
     * @param name view name. Null to leave unchanged
     * @param description view description. Null to leave unchanged
     * @param structure XML document specifying the view structure (nodes, edges, control points). Null to leave unchanged
     * @param background Background image. If null, the previous will be removed, if 0-sized array, it will remain unchanged
     * @return 
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
     * @return 
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
            String newPoolId  = service.createRootPool(name, description, instancesOfClass, type, session.getSessionId());
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
    public LocalPool createPoolInObject(String parentClassname, String parentId, String name, String description, String instancesOfClass, int type){
        try {
            String newPoolId  = service.createPoolInObject(parentClassname, parentId, name, description, instancesOfClass, type, session.getSessionId());
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
    public LocalPool createPoolInPool(String parentId, String name, String description, String instancesOfClass, int type){
        try {
            String newPoolId  = service.createPoolInPool(parentId, name, description, instancesOfClass, type, session.getSessionId());
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
    public List<LocalPool> getPoolsInObject(String objectClassName, String objectId, String poolClass)  {
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
    public List<LocalPool> getPoolsInPool(String parentPoolId, String poolClass)  {
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
    public LocalObjectLight createPoolItem (String poolId, String className){
        try {
            String objectId  = service.createPoolItem(poolId, className, null, null, null, session.getSessionId());
            return new LocalObjectLight(objectId, null, className);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public boolean deletePool(String id){
        try{
            service.deletePools(Arrays.asList(id), this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean setPoolProperties(String poolId, String name, String description) {
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
    public LocalPool getPoolInfo(String oid) {
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
    public List<LocalObjectLight> getPoolItems(String oid) {
        try {
            List<RemoteObjectLight> items = service.getPoolItems(oid, -1, this.session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : items) 
                res.add(new LocalObjectLight(rol.getClassName(), rol.getName(), rol.getId(), rol.getValidators()));

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
                for (String parameter : parameterNames) 
                    parameters.add(new StringPair(parameter, ""));
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
                for (String parameter : parametersToAddOrModify) 
                    parameters.add(new StringPair(parameter, ""));
            }
            
            if (parametersToDelete != null) {
                for (String parameter : parametersToDelete) 
                    parameters.add(new StringPair(parameter, null));
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
    public byte[] executeClassLevelReport(String objectClassName, String objectId, long reportId) {
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
            
            for (String paramName : parameters.keySet()) 
                remoteParameters.add(new StringPair(paramName, parameters.get(paramName)));
            
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
            LocalObjectLight newTemplate = new LocalObjectLight(String.valueOf(service.createTemplate(templateClass, 
                    templateName, session.getSessionId())), templateName, templateClass);
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
            String templateElementParentClassName, String templateElementParentId, String templateElementName) {
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
    public LocalObjectLight createTemplateSpecialElement(String tsElementClass, String tsElementParentClassName, String tsElementParentId, String tsElementName) {
        try {
            return new LocalObjectLight(service.createTemplateSpecialElement(tsElementClass, tsElementParentClassName, 
                    tsElementParentId, tsElementName, session.getSessionId()), tsElementName, tsElementClass);
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    /**
     * Creates multiple template elements using a given name pattern
     * @param templateElementClassName The class name of the new set of template elements
     * @param templateElementParentClassName The parent class name of the new set of template elements
     * @param templateElementParentId The parent id of the new set of template elements
     * @param numberOfTemplateElements The number of template elements
     * @param templateElementNamePattern Name pattern of the new set of template elements
     * @return A list of new template elements or null 
     *         If the parent class name or the template element class name cannot be found
     *         If the given template element class cannot be a child of the given parent
     *         If the parent class name cannot be found
     *         If the given pattern to generate the name has less possibilities that the number of template elements to be created
     */
    public List<LocalObjectLight> createBulkTemplateElement(String templateElementClassName, 
            String templateElementParentClassName, String templateElementParentId, int numberOfTemplateElements, String templateElementNamePattern) {
        try {
            List<LocalObjectLight> result = new ArrayList<>();
            List<String> ids = service.createBulkTemplateElement(templateElementClassName, templateElementParentClassName, templateElementParentId, numberOfTemplateElements, templateElementNamePattern, session.getSessionId());
            /*
            TODO: createBulkTemplateElement should return RemoteObjectLight instances instead of merely ids, so we can save so many calls to 
            getTemplateElement
            */
            for (String id : ids) {
                LocalObject templateElement = getTemplateElement(templateElementClassName, id);
                if (templateElement == null)
                    throw new Exception();
                
                result.add(templateElement);
            }
            return result;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    /**
     * Creates multiple special template elements using a given name pattern
     * @param stElementClass The class name of the new set of special template elements
     * @param stElementParentClassName The parent class name of the new set of special template elements
     * @param stElementParentId The parent id of the new set of special template elements
     * @param numberOfTemplateElements The number of template elements
     * @param stElementNamePattern Name pattern of the new set of special template elements
     * @return A list of new special template elements or null
     *         If the parent class name or the special template element class name cannot be found
     *         If the given special template element class cannot be a child of the given parent
     *         If the parent class name cannot be found
     *         If the given pattern to generate the name has less possibilities that the number of special template elements to be created
     */
    public List<LocalObjectLight> createBulkSpecialTemplateElement(String stElementClass, String stElementParentClassName, 
            String stElementParentId, int numberOfTemplateElements, String stElementNamePattern) {
        try {
            List<LocalObjectLight> result = new ArrayList<>();
            List<String> ids = service.createBulkSpecialTemplateElement(stElementClass, stElementParentClassName, stElementParentId, numberOfTemplateElements, stElementNamePattern, session.getSessionId());
            /*
            TODO: createBulkSpecialTemplateElement should return RemoteObjectLight instances instead of merely ids, so we can save so many calls to 
            getTemplateElement
            */
            for (String id : ids) {
                LocalObject specialTemplateElement = getTemplateElement(stElementClass, id);
                if (specialTemplateElement == null)
                    throw new Exception();
                
                result.add(specialTemplateElement);
            }
            return result;
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
    public boolean updateTemplateElement(String templateElementClass, String templateElementId, 
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
    public boolean deleteTemplateElement(String templateElementClass, String templateElementId) {
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
                    localTemplates.add(new LocalObjectLight(remoteTemplate.getId(), remoteTemplate.getName(), remoteTemplate.getClassName()));
                
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
    public List<LocalObjectLight> getTemplateElementChildren(String templateElementClass, String templateElementId) {
        try {
            List<LocalObjectLight> localTemplateElementChildren = new ArrayList<>();
            List<RemoteObjectLight> remoteTemplateElementChildren = service.getTemplateElementChildren(templateElementClass, templateElementId, session.getSessionId());
            for (RemoteObjectLight remoteTemplateElementChild : remoteTemplateElementChildren)
                localTemplateElementChildren.add(new LocalObjectLight(remoteTemplateElementChild.getId(), remoteTemplateElementChild.getName(), remoteTemplateElementChild.getClassName()));
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
    public List<LocalObjectLight> getTemplateSpecialElementChildren(String tsElementClass, String tsElementId) {
        try {
            List<LocalObjectLight> localTemplateElementChildren = new ArrayList<>();
            List<RemoteObjectLight> remoteTemplateSpecialElementChildren = service.getTemplateSpecialElementChildren(tsElementClass, tsElementId, session.getSessionId());
            for (RemoteObjectLight remoteTemplateSpecialElementChild : remoteTemplateSpecialElementChildren)
                localTemplateElementChildren.add(new LocalObjectLight(remoteTemplateSpecialElementChild.getId(), remoteTemplateSpecialElementChild.getName(), remoteTemplateSpecialElementChild.getClassName()));
            return localTemplateElementChildren;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Retrieves all the information of a given template element.
     * @param templateElementClass Template element class.
     * @param templateElementId Template element id.
     * @return The template element information. It will return null if something went wrong.
     */
    public LocalObject getTemplateElement(String templateElementClass, String templateElementId) {
        try {
            RemoteObject remoteTemplateElement = service.getTemplateElement(templateElementClass, templateElementId, session.getSessionId());
            LocalClassMetadata classMetadata = getMetaForClass(templateElementClass, false);
                        
            return new LocalObject(remoteTemplateElement.getClassName(), remoteTemplateElement.getId(), 
                    remoteTemplateElement.getAttributes(), classMetadata);
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
    public List<LocalObjectLight> copyTemplateElements(List<String> sourceObjectsClassNames, List<String> sourceObjectsIds, 
            String newParentClassName, String newParentId) {
        try {
            List<String> remoteTemplateElements = service.copyTemplateElements(sourceObjectsClassNames, 
                    sourceObjectsIds, newParentClassName, newParentId, session.getSessionId());
            
            List<LocalObjectLight> localTemplateElements = new ArrayList<>();
            /*
            TODO: copyTemplateElements should return RemoteObjectLight instances instead of merely ids, because this method is 
            not safe.
            */
            for (int i = 0; i < sourceObjectsClassNames.size(); i++) 
                localTemplateElements.add(new LocalObjectLight(remoteTemplateElements.get(i), "", sourceObjectsClassNames.get(i)));
            
            return localTemplateElements;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public List<LocalObjectLight> copyTemplateSpecialElements(List<String> sourceObjectsClassNames, List<String> sourceObjectsIds,
        String newParentClassName, String newParentId) {
        try {
            List<String> remoteTemplateSpecialElements = service.copyTemplateSpecialElements(sourceObjectsClassNames, 
                    sourceObjectsIds, newParentClassName, newParentId, session.getSessionId());
            
            List<LocalObjectLight> localTemplateSpecialElements = new ArrayList<>();
            /*
            TODO: copyTemplateElements should return RemoteObjectLight instances instead of merely ids, because this method is 
            not safe.
            */
            for (int i = 0; i < sourceObjectsClassNames.size(); i++)
                localTemplateSpecialElements.add(new LocalObjectLight(remoteTemplateSpecialElements.get(i), "", sourceObjectsClassNames.get(i)));
            
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
            String newObjectId = service.createSDHTransportLink(endpointA.getClassName(),
                    endpointA.getId(), endpointB.getClassName(), endpointB.getId(), transportLinkType, defaultName, session.getSessionId());
            return new LocalObjectLight(newObjectId, defaultName, transportLinkType);
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight createSDHContainerLink(LocalObjectLight equipmentA, LocalObjectLight equipmentB, 
            String containerLinkType, List<LocalSDHPosition> positions, String defaultName){
        
        try { 
            List<RemoteSDHPosition> remotepositions = new ArrayList<>();
            
            for (LocalSDHPosition aLocalPosition : positions) {
                RemoteSDHPosition aRemotePosition = new RemoteSDHPosition();
                aRemotePosition.setConnectionClass(aLocalPosition.getLinkClass());
                aRemotePosition.setConnectionId(aLocalPosition.getLinkId());
                aRemotePosition.setPosition(aLocalPosition.getPosition());
                remotepositions.add(aRemotePosition);
            }
            
            String newObjectId = service.createSDHContainerLink(equipmentA.getClassName(),
                    equipmentA.getId(), equipmentB.getClassName(), equipmentB.getId(), 
                    containerLinkType, remotepositions, defaultName, session.getSessionId());
            return new LocalObjectLight(newObjectId, defaultName, containerLinkType);
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight createSDHTributaryLink(LocalObjectLight equipmentA, LocalObjectLight equipmentB, 
            String containerLinkType, List<LocalSDHPosition> positions, String defaultName){
        
        try { 
            List<RemoteSDHPosition> remotepositions = new ArrayList<>();
            
            for (LocalSDHPosition aLocalPosition : positions) {
                RemoteSDHPosition aRemotePosition = new RemoteSDHPosition();
                aRemotePosition.setConnectionClass(aLocalPosition.getLinkClass());
                aRemotePosition.setConnectionId(aLocalPosition.getLinkId());
                aRemotePosition.setPosition(aLocalPosition.getPosition());
                remotepositions.add(aRemotePosition);
            }
            
            String newObjectId = service.createSDHTributaryLink(equipmentA.getClassName(),
                    equipmentA.getId(), equipmentB.getClassName(), equipmentB.getId(), 
                    containerLinkType, remotepositions, defaultName, session.getSessionId());
            return new LocalObjectLight(newObjectId, defaultName, containerLinkType);
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public List<LocalObjectLightList> findRoutesUsingTransportLinks(LocalObjectLight aSide, LocalObjectLight bSide) {
        try {
            List<RemoteObjectLightList> routes = service.findSDHRoutesUsingTransportLinks(aSide.getClassName(), aSide.getId(), bSide.getClassName(), bSide.getId(), session.getSessionId());
            List<LocalObjectLightList> res = new ArrayList<>();
            for (RemoteObjectLightList route : routes) 
                res.add(new LocalObjectLightList(route));
            
            return res;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public List<LocalObjectLightList> findRoutesUsingContainerLinks(LocalObjectLight aSide, LocalObjectLight bSide) {
        try {
            List<RemoteObjectLightList> routes = service.findSDHRoutesUsingContainerLinks(aSide.getClassName(), aSide.getId(), bSide.getClassName(), bSide.getId(), session.getSessionId());
            List<LocalObjectLightList> res = new ArrayList<>();
            for (RemoteObjectLightList route : routes) 
                res.add(new LocalObjectLightList(route));
            
            return res;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public List<LocalSDHContainerLinkDefinition> getSDHTransportLinkStructure(String transportLinkClass, String transportLinkId) {
        try {
            List<RemoteSDHContainerLinkDefinition> transportLinkStructure = service.getSDHTransportLinkStructure(transportLinkClass, transportLinkId, session.getSessionId());
            List<LocalSDHContainerLinkDefinition> res = new ArrayList<>();
            
            for (RemoteSDHContainerLinkDefinition aContainerDefinition : transportLinkStructure) {
                RemoteObjectLight container = aContainerDefinition.getContainer();
                List<LocalSDHPosition> positions = new ArrayList<>();
                
                for (RemoteSDHPosition aRemotePosition : aContainerDefinition.getPositions()) 
                    positions.add(new LocalSDHPosition(aRemotePosition.getConnectionClass(), aRemotePosition.getConnectionId(), aRemotePosition.getPosition()));
                
                LocalSDHContainerLinkDefinition aLocalContainerDefinition = 
                        new LocalSDHContainerLinkDefinition(new LocalObjectLight(container.getId(), container.getName(), container.getClassName()), 
                                aContainerDefinition.isStructured(), positions);
                res.add(aLocalContainerDefinition);
            }            
            return res;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    public List<LocalSDHContainerLinkDefinition> getSDHContainerLinkStructure(String containerLinkClass, String containerLinkId) {
        try {
            List<RemoteSDHContainerLinkDefinition> containerLinkStructure = service.getSDHContainerLinkStructure(containerLinkClass, containerLinkId, session.getSessionId());
            List<LocalSDHContainerLinkDefinition> res = new ArrayList<>();
            
            for (RemoteSDHContainerLinkDefinition aContainerDefinition : containerLinkStructure) {
                RemoteObjectLight container = aContainerDefinition.getContainer();
                List<LocalSDHPosition> positions = new ArrayList<>();
                
                for (RemoteSDHPosition aRemotePosition : aContainerDefinition.getPositions()) 
                    positions.add(new LocalSDHPosition(aRemotePosition.getConnectionClass(), aRemotePosition.getConnectionId(), aRemotePosition.getPosition()));
                
                LocalSDHContainerLinkDefinition aLocalContainerDefinition = 
                        new LocalSDHContainerLinkDefinition(new LocalObjectLight(container.getId(), container.getName(), container.getClassName()), 
                                aContainerDefinition.isStructured(), positions);
                res.add(aLocalContainerDefinition);
            }            
            return res;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Deletes an SDH Transport Link
     * @param transportLinkClass The class of the transport link
     * @param transportLinkId The id of the transport link
     * @return True of the operation was successful, false otherwise
     */
    public boolean deleteSDHTransportLink(String transportLinkClass, String transportLinkId) {
        try {
            service.deleteSDHTransportLink(transportLinkClass, transportLinkId, true, session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Deletes an SDH Container link
     * @param containerLinkClass Class of the container
     * @param containerLinkId Id of the container
     * @return True of the operation was successful, false otherwise
     */
    public boolean deleteSDHContainerLink(String containerLinkClass, String containerLinkId) {
        try {
            service.deleteSDHContainerLink(containerLinkClass, containerLinkId, true, session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Deletes an SDH Tributary link
     * @param tributaryLinkClass Class of the link
     * @param tributaryLinkId Id of the link
     * @return True if the operation was successful, false otherwise
     */
    public boolean deleteSDHTributaryLink(String tributaryLinkClass, String tributaryLinkId) {
        try {
            service.deleteSDHTributaryLink(tributaryLinkClass, tributaryLinkId, session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="IPAM Module">
    public List<LocalPool> getSubnetPools(String parentId, String className){
        try{
            List <RemotePool> pools = service.getSubnetPools(parentId, className, this.session.getSessionId());
            List <LocalPool> res = new ArrayList<>();
            
            for (RemotePool pool : pools)
                res.add(new LocalPool(pool.getId(), pool.getName(), pool.getClassName(), pool.getDescription(), pool.getType()));
            
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalPool getSubnetPool(String id){
        try {
            RemotePool subnetPool = service.getSubnetPool(id, this.session.getSessionId());
            return new LocalPool(subnetPool.getId(), subnetPool.getName(), subnetPool.getClassName(), 
                    subnetPool.getDescription(), subnetPool.getType());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Fetches the information on a given IPv4/IPv6 subnet
     * @param id The id of the subnet
     * @param className The class of the subnet
     * @return The subnet as a LocalObject instance
     */
    public LocalObject getSubnet(String id, String className){
        try{
            LocalClassMetadata classMetadata = getMetaForClass(className, false);
            RemoteObject subnet = service.getSubnet(id, className, this.session.getSessionId());
            return new LocalObject(subnet.getClassName(), subnet.getId(), 
                    subnet.getAttributes(), classMetadata);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public List<LocalObjectLight> getSubnets(String oid) {
        try {
            List<RemoteObjectLight> items = service.getSubnets(oid, -1, this.session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<>();

            for (RemoteObjectLight rol : items)
                res.add(new LocalObjectLight(rol.getClassName(), rol.getName(), rol.getId(), rol.getValidators()));
            
            return res;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight createSubnetPool(String parentId, String className, String subnetPoolName, 
            String subnetPoolDescription, int type) {
         try{
             String objectId = service.createSubnetPool(parentId, subnetPoolName, subnetPoolDescription, className, this.session.getSessionId());
             return new LocalObjectLight(objectId, subnetPoolName, className);
         }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public boolean deleteSubnet(String className, String subnetId){
        try {
            List<String> subnetsToBeDeleted = new ArrayList<>();
            subnetsToBeDeleted.add(subnetId);
            service.deleteSubnets(className, subnetsToBeDeleted, false, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean deleteSubnetPool(String id){
        try{
            service.deleteSubnetPools(Arrays.asList(id), this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
        
    public LocalObjectLight createSubnet(String poolId, String parentClassName, String name, HashMap<String, String> attributes){
        try {
            
            List<StringPair> remoteAttributes = new ArrayList<>();
            for (String attribute : attributes.keySet())
                remoteAttributes.add(new StringPair(attribute, attributes.get(attribute)));
            
            String objectId  = service.createSubnet(poolId, parentClassName, remoteAttributes, this.session.getSessionId());
            return new LocalObjectLight(objectId, name, parentClassName);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public boolean relatePortToInterface(String id, String className, String interfaceClassName, String interfaceId){
        try{
            service.relatePortToInterface(id, className, interfaceClassName, interfaceId, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean releasePortFromInterface(String interfaceClassName, String interfaceId, String id){
        try{
            service.releasePortFromInterface(interfaceClassName, interfaceId,  id, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public LocalObjectLight addIPAddress(String id, String className, String name, HashMap<String, String> attributes){
        try {
            List<StringPair> remoteAttributes = new ArrayList<>();
            for (String attribute : attributes.keySet())
                remoteAttributes.add(new StringPair(attribute, attributes.get(attribute)));
            
            String objectId  = service.addIPAddress(id, className, remoteAttributes, this.session.getSessionId());
            return new LocalObjectLight(objectId, name, className);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public boolean removeIP(List<String> oids){
        try{
            service.removeIP(oids, false, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean relateIPtoPort(String id, String portClassName, String PortId){
        try{
            service.relateIPtoPort(id, portClassName, PortId, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public List<LocalObjectLight> getSubnetUsedIps(String id, String className){
        try {
            List<LocalObjectLight> res = new ArrayList<>();
            for (RemoteObjectLight anIp : service.getSubnetUsedIps(id, 0, className, this.session.getSessionId())) 
                res.add(new LocalObjectLight(anIp.getId(), anIp.getName(), anIp.getClassName()));
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
        }
        return null;
    }
    
    public List<LocalObjectLight> getSubnetsInSubnet(String id, String className){
        try {
            List<LocalObjectLight> res = new ArrayList<>();
            for (RemoteObjectLight anIp : service.getSubnetsInSubnet(id, 0, className, this.session.getSessionId())) 
                res.add(new LocalObjectLight(anIp.getId(), anIp.getName(), anIp.getClassName()));
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
        }
        return null;
    }

    public boolean relateSubnetToVLAN(String subnetId, String className, String vlanId){
        try{
            service.relateSubnetToVlan(subnetId, className, vlanId, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean relateSubnetToVFR(String subnetId, String className, String vfrId){
        try{
            service.relateSubnetToVrf(subnetId, className, vfrId, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean releasePortFromIPAddress(String deviceClassName, String deviceId, String id){
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
    public boolean releaseSubnetFromVLAN(String subnetId, String vlanId){
        try{
            service.releaseSubnetFromVlan(subnetId, vlanId, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        } 
    }
    
    public boolean releaseSubnetFromVFR(String subnetId, String vfrId){
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
    public boolean associateObjectsToContract(String[] objectsClass, String[] objectsId, String contractClass, String contractId) {
        try {
            List<String> objectsClassList = new ArrayList<>();
            List<String> objectsIdList = new ArrayList<>();
            objectsClassList.addAll(Arrays.asList(objectsClass));
            objectsIdList.addAll(Arrays.asList(objectsId));
            service.associateObjectsToContract(objectsClassList, objectsIdList, contractClass, contractId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    public boolean releaseObjectFromContract(String contractClass, String contractId, String targetId){
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
    public LocalObjectLight createMPLSLink(LocalObjectLight endpointA, LocalObjectLight endpointB, String mplsLinkName){
            try { 
                List<StringPair> attributesToBeSet =  new ArrayList<>();
                attributesToBeSet.add(new StringPair(Constants.PROPERTY_NAME, mplsLinkName));
                String newObjectId = service.createMPLSLink(endpointA.getClassName(), endpointA.getId(), 
                        endpointB.getClassName(), endpointB.getId(), attributesToBeSet,  session.getSessionId());
                
                return new LocalObjectLight(newObjectId, mplsLinkName, Constants.CLASS_MPLSLINK);
            } catch (Exception ex) {
                this.error = ex.getMessage();
                return null;
            }
        }
    
        public boolean deleteMPLSLink(String linkId) {
            try {
                service.deleteMPLSLink(linkId, true, session.getSessionId());
                return true;
            } catch (Exception ex) {
                this.error = ex.getMessage();
                return false;
            }
        }
        
        /**
        * Disconnects a side or both sides of a mpls link connection
        * @param connectionId Id of the connection to be edited
        * @param sideToDisconnect Side to disconnect. Use 1 to disconnect only the side a, 2 to disconnect only side b and 3 to disconnect both sides at once
        * @return True if the operation was successful, false otherwise. Retrieve the details of the error using the getError method
        */
       public boolean disconnectMPLSLink(String connectionId, int sideToDisconnect) {
           try {
               service.disconnectMPLSLink(connectionId, sideToDisconnect, session.getSessionId());
               return true;
           }catch(Exception ex){
               this.error =  ex.getMessage();
               return false;
           }
        }
        
      
        public LocalMPLSConnectionDetails getMPLSLinkEndpoints(String connectionId) {
            try{
                RemoteMPLSConnectionDetails mplsLinkEndpoints = service.getMPLSLinkEndpoints(connectionId, session.getSessionId());
                
                LocalMPLSConnectionDetails localMPLSConnectionDetails = new LocalMPLSConnectionDetails(mplsLinkEndpoints);
                
                return localMPLSConnectionDetails;
            }catch(Exception ex){
                this.error =  ex.getMessage();
                return null;
            }
        }
        
        public boolean connectMplsLinks(List<String> sideAClassNames, List<String> sideAIds, 
                List<String> linksIds, List<String> sideBClassNames, List<String> sideBIds) {
            try {            
                service.connectMplsLink(sideAClassNames, sideAIds, linksIds, sideBClassNames, sideBIds, session.getSessionId());
                return true;
            }catch(Exception ex){
                this.error =  ex.getMessage();
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
    public LocalObjectLight addProject(String parentId, String parentClassName, String className) {
        try {
            String objectId = service.addProject(parentId, parentClassName, className, new ArrayList<String>(),new ArrayList<String>(), session.getSessionId());
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
    public boolean deleteProject(String projectClass, String projectId) {
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
    public LocalObjectLight addActivity(String projectId, String projectClass, String activityClass) {
        try {
            String activityId = service.addActivity(projectId, projectClass, activityClass, new ArrayList<String>(), new ArrayList<String>(), session.getSessionId());
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
    public boolean deleteActivity(String activityClass, String activityId) {
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
    public List<LocalObjectLight> getProjectInProjectPool(String poolId, int limit) {
        try {
            List<RemoteObjectLight> remoteProjects = service.getProjectsInProjectPool(poolId, limit, session.getSessionId());
            
            List<LocalObjectLight> projects = new ArrayList<>();
            
            for (RemoteObjectLight remoteProject : remoteProjects)
                projects.add(new LocalObjectLight(remoteProject.getId(), remoteProject.getName(), remoteProject.getClassName()));
            
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
    public List<LocalObjectLight> getProjectResources(String projectClass, String projectId) {
        try {
            List<LocalObjectLight> resources = new ArrayList<>();
            
            for (RemoteObjectLight remoteResource : service.getProjectResurces(projectClass, projectId, session.getSessionId()))
                resources.add(new LocalObjectLight(remoteResource.getId(), remoteResource.getName(), remoteResource.getClassName()));
            
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
    public List<LocalObjectLight> getProjectActivities(String projectClass, String projectId) {
        try {
            List<LocalObjectLight> activities = new ArrayList<>();
            
            for (RemoteObjectLight remoteActivity : service.getProjectActivities(projectClass, projectId, session.getSessionId()))
                activities.add(new LocalObjectLight(remoteActivity.getId(), remoteActivity.getName(), remoteActivity.getClassName()));
            
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
    public boolean associateObjectsToProject(String projectClass, String projectId, List<String> objectClass, List<String> objectId) {
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
    public boolean associateObjectToProject(String projectClass, String projectId, String objectClass, String objectId) {
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
    public boolean releaseObjectFromProject(String objectClass, String objectId, String projectClass, String projectId) {
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
    public List<LocalObjectLight> getProjectsAssociateToObject(String objectClass, String objectId) {
        try {
            List<RemoteObjectLight> remoteProjects = service.getProjectsAssociateToObject(objectClass, objectId, session.getSessionId());
            
            List<LocalObjectLight> projects = new ArrayList<>();
            
            for (RemoteObjectLight remoteProject : remoteProjects)
                projects.add(new LocalObjectLight(remoteProject.getId(), remoteProject.getName(), remoteProject.getClassName()));
            
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
            String projId = service.createProjectPool(name, description, instanceOfClass, session.getSessionId());
            return getPoolInfo(projId);
        } catch (Exception ex) {
            error = ex.getMessage();
            return null;
        }
    }
        // </editor-fold>
    
        // <editor-fold defaultstate="collapsed" desc="Warehouse Module">   
    /**
     * Gets the warehouse module root pools
     * @return the warehouse module root pools, or null if the class Warehouse or VirtualWatehouse not exist
     */
    public List<LocalPool> getWarehouseRootPool() {        
        try {
            List<LocalPool> res = new ArrayList();
            
            List<RemotePool> rootPools = service.getWarehouseRootPools(session.getSessionId());
            
            for (RemotePool rootPool : rootPools)
                res.add(new LocalPool(rootPool.getId(), rootPool.getName(), rootPool.getClassName(), rootPool.getDescription(), rootPool.getType()));
            return res;
                        
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    };       
    /**
    * Associates a list of objects (resources) to an existing warehouse or virtual warehouse
    * @param classNames Object classes
    * @param objectIds Object ids
    * @param warehouseClass Warehouse class
    * @param warehouseId Warehouse id
    * @return False 
    *  If the user is not allowed to invoke the method
    *  If any of the objects can't be found
    *  If any of the objects involved can't be connected (i.e. if it's not an inventory object)
    *  If any of the classes provided can not be found
    */
    public boolean associatesPhysicalNodeToWarehouse(List<String> classNames, List<String> objectIds, String warehouseClass, String warehouseId){
        try{
            List<String> objectsClassList = new ArrayList<>();
            List<String> objectsIdList = new ArrayList<>();
            objectsClassList.addAll(classNames);
            objectsIdList.addAll(objectIds);
            service.associatesPhysicalNodeToWarehouse(objectsClassList, objectsIdList, warehouseClass, warehouseId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    /**
     * Releases an object from a warehouse or virtual warehouse that is using it
     * @param warehouseClass Warehouse class
     * @param warehouseId Warehouse id
     * @param targetId target object id
     * @return False
     *  If the user is not allowed to invoke the method
     *  If the object can not be found
     *  If the class can not be found
     *  If the object activity log could no be found
     */
    public boolean releasePhysicalNodeFromWarehouse(String warehouseClass, String warehouseId, String targetId){
        try{
            service.releasePhysicalNodeFromWarehouse(warehouseClass, warehouseId, targetId, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    /**
    * Moves objects from their current parent to a warehouse pool target object.
    * @param targetClass New parent object id
    * @param targetOid The new parent's oid
    * @param objects Class names and Oids of the objects to be moved
    * @return False
    *   If the object's or new parent's class can't be found
    *   If the object or its new parent can't be found
    *   If the update can't be performed due to a business rule
    */
    public boolean moveObjectsToWarehousePool(String targetClass, String targetOid, LocalObjectLight[] objects) {

        try{
            List<String> objectOids = new ArrayList<>();
            List<String> objectClasses = new ArrayList<>();

            for (LocalObjectLight lol : objects){
                objectOids.add(lol.getId());
                objectClasses.add(lol.getClassName());
            }
            service.moveObjectsToWarehousePool(targetClass, targetOid, objectClasses, objectOids, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    /**
     * Moves objects from their current parent to a target object.
     * @param  targetClass New parent object id
     * @param targetOid The new parent's oid
     * @param objects Class names and Oids of the objects to be moved
     * @return False
     *  If the object's or new parent's class can't be found
     *  If the object or its new parent can't be found
     *  If the update can't be performed due to a business rule
     */
    public boolean moveObjectsToWarehouse(String targetClass, String targetOid, List<LocalObjectLight> objects) {
        try{
            List<String> objectOids = new ArrayList<>();
            List<String> objectClasses = new ArrayList<>();

            for (LocalObjectLight lol : objects){
                objectOids.add(lol.getId());
                objectClasses.add(lol.getClassName());
            }
            service.moveObjectsToWarehouse(targetClass, targetOid, objectClasses, objectOids,this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
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
    public boolean addObjectsToFavoritesFolder(List<String> objectClass, List<String> objectId, long bookmarkFolderId) {
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
    public boolean removeObjectsFromFavoritesFolder(List<String> objectClass, List<String> objectId, long bookmarkFolderId) {
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

            for (RemoteObjectLight rol : bookmarkItems)
                res.add(new LocalObjectLight(rol.getClassName(), rol.getName(), rol.getId(), rol.getValidators()));

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
    public List<LocalFavoritesFolder> objectIsBookmarkItemIn(String objectClass, String objectId) {
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
    
    // <editor-fold defaultstate="collapsed" desc="Synchronization methods. Click on the + sign on the left to edit the code.">
    /**
     * Get the set of sync providers defined in the configuration variables pool called -Sync Providers-
     * @return The set of sync providers defined in the configuration variables pool called -Sync Providers-
     */
    public List<LocalSyncProvider> getSynchronizationProviders() {
        try {
            List<RemoteSynchronizationProvider> remoteSyncProviders = service.getSynchronizationProviders(session.getSessionId());
            List<LocalSyncProvider> syncProviders = new ArrayList();
            for (RemoteSynchronizationProvider remoteSyncProvider : remoteSyncProviders)
                syncProviders.add(new LocalSyncProvider(remoteSyncProvider.getId(), remoteSyncProvider.getDisplayName(), remoteSyncProvider.isAutomated()));
            return syncProviders;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    /**
     * Create a Sync Group
     * @param syncGroupName The name of the Sync group
     * @return The local representation of the Favorites folder
     */        
    public LocalSyncGroup createSyncGroup(String syncGroupName) {
        try {
            long id = service.createSynchronizationGroup(syncGroupName, session.getSessionId());
            return new LocalSyncGroup(id, syncGroupName);
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
     /**
     * Updates a Sync Group
     * @param syncGroupId The id of the Sync group
     * @param syncGroupProperties The properties of the Sync group
     * @return The local representation of the Favorites folder
     */        
    public boolean updateSyncGroup(long syncGroupId, HashMap<String, String> syncGroupProperties) {
        try {
            List<StringPair> remoteProperties = new ArrayList<>();
            
            for (String paramName : syncGroupProperties.keySet()) 
                remoteProperties.add(new StringPair(paramName, syncGroupProperties.get(paramName)));
            
            service.updateSynchronizationGroup(syncGroupId, remoteProperties, session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Deletes a Sync Group
     * @param syncGroupId the synchronization group id
     * @return True if was deleted successfully
     */
    public boolean deleteSyncGroup(long syncGroupId) {
        try {
            service.deleteSynchronizationGroup(syncGroupId, session.getSessionId());
            return true;        
        } catch (Exception ex) {
            return false;
        }
    }
    
    /**
     * Create a Sync Data Source Configuration
     * @param syncGroupId The name of the sync group id
     * @param syncDataSourceConfigName name of the data source configuration
     * @param parameters the parameters associated to the configuration
     * @return The local representation of the sync configuration
     */        
    public LocalSyncDataSourceConfiguration createSyncDataSourceConfiguration(String objectId, long syncGroupId, String syncDataSourceConfigName, HashMap<String, String> parameters) {
        try {
            List<StringPair> remoteParameters = new ArrayList<>();
            
            for (String paramName : parameters.keySet()) 
                remoteParameters.add(new StringPair(paramName, parameters.get(paramName)));
            
            long id = service.createSynchronizationDataSourceConfig(objectId, syncGroupId, syncDataSourceConfigName, remoteParameters, session.getSessionId());
            return new LocalSyncDataSourceConfiguration(id, syncDataSourceConfigName, parameters);
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Create a Sync Data Source Configuration
     * @param syncDataSourceConfigId The id of the sync data source configuration
     * @param parameters the parameters need it to make the sync id, name, className, IP address, port, community
     * @return The local representation of the Favorites folder
     */        
    public boolean updateSyncDataSourceConfiguration(long syncDataSourceConfigId, HashMap<String, String> parameters) {
        try {
            List<StringPair> remoteParameters = new ArrayList<>();
            
            for (String parameterName : parameters.keySet()) 
                remoteParameters.add(new StringPair(parameterName, parameters.get(parameterName)));
            
            service.updateSyncDataSourceConfiguration(syncDataSourceConfigId, remoteParameters, session.getSessionId());
            
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Deletes a Synchronization data source configuration
     * @param syncGroupId sync group source id
     * @param syncDataSourceConfigIds sync data source ids
     * @return True if was deleted successfully
     */
    public boolean releaseSyncDataSourceConfigurationFromSyncGroup(long syncGroupId, List<Long> syncDataSourceConfigIds) {
        try {
            service.releaseSyncDataSourceConfigFromSyncGroup(syncGroupId, syncDataSourceConfigIds, session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Deletes a Synchronization data source configuration
     * @param syncDataSourceConfigId sync data source id
     * @return True if was deleted successfully
     */
    public boolean deleteSyncDataSourceConfiguration(long syncDataSourceConfigId) {
        try {
            service.deleteSynchronizationDataSourceConfig(syncDataSourceConfigId, session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Copy a sync group
     * @param syncGroups The list of sync groups to copy
     * @return A list with the new sync groups
     */
    public List<LocalSyncGroup> copySyncGroup(LocalSyncGroup[] syncGroups) {
//        try {
//            List<Long> syncGroupsIds = new ArrayList<>();
//            for (LocalSyncGroup syncGroup : syncGroups)
//                syncGroupsIds.add(syncGroup.getId());
//            //List<RemoteSynchronizationGroup> remoteSyncGroups = service.copySyncGroup(syncGroupsIds, session.getSessionId());
//            
//            List<LocalSyncGroup> localSyncGroups = new ArrayList<>();
//            for (RemoteSynchronizationGroup remoteSyncGroup : remoteSyncGroups) {
//                localSyncGroups.add(new LocalSyncGroup(remoteSyncGroup.getId(), 
//                    remoteSyncGroup.getName(), 
//                    new LocalSyncProvider(remoteSyncGroup.getProvider().getId(), remoteSyncGroup.getProvider().getDisplayName(), 
//                            remoteSyncGroup.getProvider().isAutomated())));
//            }
//            return localSyncGroups;
//        } catch (Exception ex) {
//            this.error = ex.getMessage();
//            return null;
//        }

        return null;
    }
    
    /**
     * Copy a set of sync data source configuration into a given sync group
     * @param syncGroupId The Sync Group Id target
     * @param syncDataSourceConfiguration Set of sync data source configuration ids
     * @return The list of copied sync data source configuration
     */
    public boolean copySyncDataSourceConfiguration(long syncGroupId, LocalSyncDataSourceConfiguration[] syncDataSourceConfiguration) {
        try {
            List<Long> syncDataSrcConfigIds = new ArrayList<>();
            for (LocalSyncDataSourceConfiguration syncDataSrcConfig : syncDataSourceConfiguration)
                syncDataSrcConfigIds.add(syncDataSrcConfig.getId());
                            
            service.copySyncDataSourceConfiguration(syncGroupId, syncDataSrcConfigIds, session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Moves a sync data source configuration from a sync group to another sync group
     * @param oldSyncGroupId The Sync Group Id target
     * @param newSyncGroupId The Sync Group Id target
     * @param syncDataSourceConfiguration Set of sync data source configuration ids
     * @return True if the sync data source configuration was moved
     */
    public boolean moveSyncDataSourceConfiguration(long oldSyncGroupId, long newSyncGroupId, LocalSyncDataSourceConfiguration[] syncDataSourceConfiguration) {
        try {
            List<Long> syncDataSrcConfigIds = new ArrayList<>();
            for (LocalSyncDataSourceConfiguration syncDataSrcConfig : syncDataSourceConfiguration)
                syncDataSrcConfigIds.add(syncDataSrcConfig.getId());
            
            service.moveSyncDataSourceConfiguration(oldSyncGroupId, newSyncGroupId, syncDataSrcConfigIds, session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Gets the current jobs which are executing
     * @return The list of the current jobs which are executing
     */
    public List<LocalBackgroundJob> getCurrentJobs() {
        try {
            List<RemoteBackgroundJob> remoteJobs = service.getCurrentJobs(session.getSessionId());
            
            List<LocalBackgroundJob> result = new ArrayList<>();
            
            for (RemoteBackgroundJob remoteJob : remoteJobs) {
                result.add(new LocalBackgroundJob(remoteJob.getId(), remoteJob.getJobTag(), remoteJob.getProgress(), remoteJob.isAllowConcurrence(), remoteJob.getStatus(), remoteJob.getStartTime(), remoteJob.getEndTime()));
            }
            return result;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Kills a job given its id
     * @param jobId id of job to kill
     * @return True if the job was killed
     */
    public boolean killJob(long jobId) {
        try {
            service.killJob(jobId, session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
        
    /**
     * Gets the available sync groups
     * @return The list of available sync groups. Null otherwise
     */
    public List<LocalSyncGroup> getSyncGroups(){
        try {
            List<LocalSyncGroup> localSyncGroup = new ArrayList<>();
                    
            List<RemoteSynchronizationGroup> synchronizationGroups = service.getSynchronizationGroups(session.getSessionId());
            for (RemoteSynchronizationGroup synchronizationGroup : synchronizationGroups) {
                localSyncGroup.add(new LocalSyncGroup(synchronizationGroup.getId(), synchronizationGroup.getName()));
            }
            return localSyncGroup;
            
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Returns a data source configurations that belongs to a inventory object group
     * @param objectId the synchronization group id
     * @return a list of the data source configurations
     */
    public LocalSyncDataSourceConfiguration getSyncDataSourceConfiguration(String objectId) {
        try {
            List<LocalSyncDataSourceConfiguration> res = new ArrayList<>();
            RemoteSynchronizationConfiguration remoteConfiguration = 
                    service.getSyncDataSourceConfiguration(objectId, session.getSessionId());

            HashMap<String, String> parameters = new HashMap<>();
            for (StringPair parameter : remoteConfiguration.getParameters())
                parameters.put(parameter.getKey(), parameter.getValue());

            return new LocalSyncDataSourceConfiguration(remoteConfiguration.getId(), remoteConfiguration.getName(), parameters);

        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Returns the list of data source configurations that belongs to a sync group
     * @param syncGroupId the synchronization group id
     * @return a list of the data source configurations
     */
    public List<LocalSyncDataSourceConfiguration> getSyncDataSourceConfigurations(long syncGroupId) {
        try {
            List<LocalSyncDataSourceConfiguration> res = new ArrayList<>();
            List<RemoteSynchronizationConfiguration> syncDataSourceConfigurations = 
                    service.getSyncDataSourceConfigurations(syncGroupId, session.getSessionId());
            
            for (RemoteSynchronizationConfiguration remoteConfiguration : syncDataSourceConfigurations) {
                HashMap<String, String> parameters = new HashMap<>();
                for (StringPair parameter : remoteConfiguration.getParameters())
                    parameters.put(parameter.getKey(), parameter.getValue());
                
                res.add(new LocalSyncDataSourceConfiguration(remoteConfiguration.getId(), remoteConfiguration.getName(), parameters));
            }
                        
            return res;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    
    public void launchAutomatedSynchronizationTask(final LocalSyncGroup syncGroup, String providerId, int x, final AbstractRunnableSyncResultsManager progress) {
        try {
            service.launchAutomatedSynchronizationTaskAsync(syncGroup.getId(), providerId, session.getSessionId(), 
            new AsyncHandler<LaunchAutomatedSynchronizationTaskResponse>() {
                @Override
                public void handleResponse(Response<LaunchAutomatedSynchronizationTaskResponse> res) {
                    try {
                        LaunchAutomatedSynchronizationTaskResponse get = res.get();
            
                        List<LocalSyncResult> syncResults = new ArrayList<>();
                        for (SyncResult syncResult : get.getReturn())
                            syncResults.add(new LocalSyncResult(syncResult.getDataSourceId(), 
                                    syncResult.getType(), syncResult.getActionDescription(), syncResult.getResult()));

                        progress.setSyncResults(syncResults);
                        progress.handleSyncResults();    
                        
                    } catch (InterruptedException | ExecutionException ex) {
                        String message = ex.getMessage();
                        int idxOfSpace = message.indexOf(": ");
                        String kindMessage = message.substring(idxOfSpace + 1);
            
                        CommunicationsStub.this.error = kindMessage;
                        progress.setSyncResults(null);
                        progress.getProgressHandle().finish();                        
                        progress.handleSyncResults();
                    }
                }
            });           
        } catch (Exception ex) {
            this.error = ex.getMessage();
    
        }
    }
    
    public void launchAdHocAutomatedSynchronizationTask(final List<LocalSyncDataSourceConfiguration> syncdsConfigs, String providerId, int x, final AbstractRunnableSyncResultsManager progress) {
        try {
            List<Long> syncDsConfigIds = new ArrayList<>();
            syncdsConfigs.forEach(syncdsConfig -> {
                syncDsConfigIds.add(syncdsConfig.getId());
            });
                       
            service.launchAdHocAutomatedSynchronizationTaskAsync(syncDsConfigIds, providerId, session.getSessionId(), 
            new AsyncHandler<LaunchAdHocAutomatedSynchronizationTaskResponse>() {
                @Override
                public void handleResponse(Response<LaunchAdHocAutomatedSynchronizationTaskResponse> res) {
                    try {
                        LaunchAdHocAutomatedSynchronizationTaskResponse get = res.get();
            
                        List<LocalSyncResult> syncResults = new ArrayList<>();
                        for (SyncResult syncResult : get.getReturn())
                            syncResults.add(new LocalSyncResult(syncResult.getDataSourceId(), 
                                    syncResult.getType(), syncResult.getActionDescription(), syncResult.getResult()));
           
                        progress.setSyncResults(syncResults);
                        progress.handleSyncResults();    
                        
                    } catch (InterruptedException | ExecutionException ex) {
                        String message = ex.getMessage();
                        int idxOfSpace = message.indexOf(": ");
                        String kindMessage = message.substring(idxOfSpace + 1);
            
                        CommunicationsStub.this.error = kindMessage;
                        progress.setSyncResults(null);
                        progress.getProgressHandle().finish();                        
                        progress.handleSyncResults();
                    }
                }
            });           

        } catch (Exception ex) {
            this.error = ex.getMessage();
        }
    }
    
    /**
     * Launches a synchronization that requires a user to review the actions to 
     * be taken upon finding differences  between what's on he sync data sources 
     * and the inventory system.
     * @param syncGroup The sync group associated to the requested task
     * @param progress a handler that waits until the synchronization task ends and send the results
     */
    public void launchSupervisedSynchronizationTask(final LocalSyncGroup syncGroup, final AbstractRunnableSyncFindingsManager progress) {
        try {
            service.launchSupervisedSynchronizationTaskAsync(syncGroup.getId(), session.getSessionId(), 
            new AsyncHandler<LaunchSupervisedSynchronizationTaskResponse>(){
                @Override
                public void handleResponse(Response<LaunchSupervisedSynchronizationTaskResponse> res) {
                    try {
                        LaunchSupervisedSynchronizationTaskResponse get = res.get();
                        
                        List<LocalSyncFinding> syncFindings = new ArrayList<>();
                        for (SyncFinding syncFinding : get.getReturn())
                            syncFindings.add(new LocalSyncFinding(syncFinding.getDataSourceId(), 
                                    syncFinding.getType(), syncFinding.getDescription(), syncFinding.getExtraInformation()));
                        
                        progress.setLocalSyncGroup(syncGroup);
                        progress.setFindings(syncFindings);
                        progress.getProgressHandle().finish();
                        progress.handleSyncFindings();
                    } catch (InterruptedException | ExecutionException ex) {
                        String message = ex.getMessage();
                        int idxOfSpace = message.indexOf(": ");
                        String kindMessage = message.substring(idxOfSpace + 1);
                        
                        CommunicationsStub.this.error = kindMessage;
                        progress.setFindings(null);
                        progress.getProgressHandle().finish();                        
                        progress.handleSyncFindings();
                        
                    }
                }
            });
            progress.getProgressHandle().start();
        } catch (Exception ex) {
            this.error = ex.getMessage();
            
        }
    }
    
     /**
      * Executes the actions that the user has chosen after synchronization findings
      * @param syncGroupId
      * @param LocalActions
      * @return The list of results after executes the actions
      */
    public List<LocalSyncResult> executeSyncActions(long syncGroupId, 
            List<LocalSyncAction> LocalActions)
    {
        try {
            List<SyncAction> remoteActions = new ArrayList<>();
            for (LocalSyncAction localSyncAction : LocalActions) {
                
                SyncFinding finding = new SyncFinding();
                finding.setDataSourceId(localSyncAction.getFinding().getDataSourceId());
                finding.setDescription(localSyncAction.getFinding().getDescription());
                finding.setExtraInformation(localSyncAction.getFinding().getExtraInformation());
                finding.setType(localSyncAction.getFinding().getType());
                
                SyncAction action = new SyncAction();
                action.setFinding(finding);
                action.setType(localSyncAction.getType());
                remoteActions.add(action);
            }
            
            List<SyncResult> results = service.executeSyncActions(syncGroupId, remoteActions, session.getSessionId());
            
            List<LocalSyncResult> localResults = new ArrayList<>();
            for(SyncResult result : results)
                localResults.add(new LocalSyncResult(result.getDataSourceId(),
                        result.getType(), result.getActionDescription(), result.getResult()));
            return localResults;
            
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    } 
    //</editor-fold>
    //<editor-fold desc="Configuration Variables" defaultstate="collapsed">
    /**
     * Creates a configuration variable inside a pool. A configuration variable is a place where a value will be stored so it can retrieved by whomever need it. 
     * These variables are typically used to store values that help other modules to work, such as URLs, user names, dimensions, etc
     * @param configVariablesPoolId The id of the pool where the config variable will be put
     * @param name The name of the pool. This value can not be null or empty. Duplicate variable names are not allowed
     * @param description The description of the what the variable does
     * @param type The type of the variable. Use 1 for number, 2 for strings, 3 for booleans, 4 for unidimensional arrays and 5 for matrixes. 
     * @param masked If the value should be masked when rendered (for security reasons, for example)
     * @param valueDefinition In most cases (primitive types like numbers, strings or booleans) will be the actual value of the variable as a string (for example "5" or "admin" or "true"). For arrays and matrixes use the following notation: <br> 
     * Arrays: (value1,value2,value3,valueN), matrixes: [(row1col1, row1col2,... row1colN), (row2col1, row2col2,... row2colN), (rowNcol1, rowNcol2,... rowNcolN)]. The values will be interpreted as strings 
     * @return The newly created variable. Null if the parent pool could not be found or if the name is empty, the type is invalid, the value definition is empty
     */
    public LocalConfigurationVariable createConfigurationVariable(String configVariablesPoolId, String name, String description, int type, boolean masked, String valueDefinition) {
        try {
            long newConfigurationVariableId = service.createConfigurationVariable(configVariablesPoolId, name, description, type, masked, valueDefinition, session.getSessionId());
            
            return new LocalConfigurationVariable(newConfigurationVariableId, name, description, valueDefinition, masked, type);
            
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Updates the value of a configuration variable. See #{@link #createConfigurationVariable(long, java.lang.String, java.lang.String, int, java.lang.String) } for value definition syntax
     * @param name The current name of the variable that will be modified
     * @param propertyToUpdate The name of the property to be updated. Possible values are: "name", "description", "type", "masked" and "value"
     * @param newValue The new value as string
     * @return False if the property to be updated can not be recognized or if the config variable can not be found. True otherwise
     */
    public boolean updateConfigurationVariable(String name, String propertyToUpdate, String newValue) {
        try {
            service.updateConfigurationVariable(name, propertyToUpdate, newValue, session.getSessionId());
            return true;
            
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    /**
     * Deletes a config variable
     * @param name The name of the variable to be deleted
     * @return False if the config variable could not be found. True otherwise.
     */
    public boolean deleteConfigurationVariable(String name) {
        try {
            service.deleteConfigurationVariable(name, session.getSessionId());
            return true;
            
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    /**
     * Retrieves a configuration variable
     * @param name The name of the variable to be retrieved
     * @return Null if the variable could not be found. The local representation of the variable otherwise
     */
    public LocalConfigurationVariable getConfigurationVariable(String name) {
        try {
            RemoteConfigurationVariable configurationVariable = service.getConfigurationVariable(name, session.getSessionId());
            return new LocalConfigurationVariable(configurationVariable.getId(), configurationVariable.getName(),
                    configurationVariable.getDescription(), configurationVariable.getValueDefinition(), configurationVariable.isMasked(), configurationVariable.getType());
            
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    /**
     * Gets the configuration variables in a config variable pool.
     * @param parentPoolId The id pool to retrieve the variables from.
     * @return The list of configuration variables in the given pool or null if the pool could not be found.
     */
    public List<LocalConfigurationVariable> getConfigurationVariablesInPool(String parentPoolId) {
        try {
            List<RemoteConfigurationVariable> configurationVariablesInPool = service.getConfigurationVariablesInPool(parentPoolId, session.getSessionId());
            List<LocalConfigurationVariable> res = new ArrayList<>();
                    
            configurationVariablesInPool.stream().forEach((configurationVariable) -> {
                res.add(new LocalConfigurationVariable(configurationVariable.getId(), configurationVariable.getName(), 
                    configurationVariable.getDescription(), configurationVariable.getValueDefinition(), configurationVariable.isMasked(), configurationVariable.getType()));
            });
            
            return res;
            
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    /**
     * Retrieves the list of pools of configuration variables.
     * @return The available pools of configuration variables.
     */
    public List<LocalPool> getConfigurationVariablesPools() {
        try {
            List<RemotePool> configurationVariablesPools = service.getConfigurationVariablesPools(session.getSessionId());
            List<LocalPool> res = new ArrayList<>();
                    
            configurationVariablesPools.stream().forEach((pool) -> {
                res.add(new LocalPool(pool.getId(), pool.getName(), pool.getClassName(), pool.getDescription(), pool.getType()));
            });
            
            return res;
            
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    /**
     * Creates a pool of configuration variables.
     * @param name The name of the pool. Empty or null values are not allowed.
     * @param description The description of the pool.
     * @return The newly created pool or null if the name provided is null or empty.
     */
    public LocalPool createConfigurationVariablesPool(String name, String description) {
        try {
            String newPoolId = service.createConfigurationVariablesPool(name, description, session.getSessionId());
            return new LocalPool(newPoolId, name, "Pool of Configuration Variables", description, LocalPool.POOL_TYPE_MODULE_ROOT);
            
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    /**
     * Updates an attribute of a given configuration variables pool.
     * @param poolId The id of the pool to update.
     * @param propertyToUpdate The property to update. The valid values are "name" and "description".
     * @param value The value of the property to be updated.
     * @return false if the property provided is not valid or if the pool could not be found. True otherwise.
     */
    public boolean updateConfigurationVariablesPool(String poolId, String propertyToUpdate, String value) {
        try {
            service.updateConfigurationVariablesPool(poolId, propertyToUpdate, value, session.getSessionId());
            return true;
            
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    /**
     * Deletes a configuration variables pool. Deleting a pool also deletes the configuration variables contained within.
     * @param poolId The id of the pool to be deleted.
     * @return False if the pool could not be found. True if the operation was successful.
     */
    public boolean deleteConfigurationVariablesPool(String poolId) {
        try {
            service.deleteConfigurationVariablesPool(poolId, session.getSessionId());
            return true;
            
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    //</editor-fold>
    
    //<editor-fold desc="Validators" defaultstate="collapsed">
    /**
     * Creates a validator definition. 
     * @param name The name of the validator. It's recommended to use camel case notation (for example thisIsAName). This field is mandatory
     * @param description The optional description of the validator
     * @param classToBeApplied The class or super class of the classes whose instances will be checked against this validator
     * @param script The groovy script containing the logic of the validator , that is, the 
     * @param enabled If this validador should be applied or not
     * @return The id of the newly created validator definition or -1 in case of error, such as when the name is null or empty or if the classToBeApplied argument could not be found
     */
    public long createValidatorDefinition(String name, String description, String classToBeApplied, String script, boolean enabled) {
        try {
            return service.createValidatorDefinition(name, description, classToBeApplied, script, enabled, session.getSessionId());
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return -1;
        }
    }
    
    /**
     * Updates the properties of a validator. The null values will be ignored
     * @param validatorDefinitionId The id of teh validator definition to be updated
     * @param name The new name, if any, null otherwise
     * @param description The new description, if any, null otherwise
     * @param classToBeApplied The new class to be associated to this validator, if any, null otherwise
     * @param script The new script, if any, null otherwise
     * @param enabled If the validator should be enabled or not, if any, null otherwise
     * @return False if the validator definition could not be found or if the classToBeApplied parameter is not valid or 
     * if the name is not null, but it is empty. True otherwise
     */
    public boolean updateValidatorDefinition(long validatorDefinitionId, String name, String description, 
            String classToBeApplied, String script, Boolean enabled) { 
        try {
            service.updateValidatorDefinition(validatorDefinitionId, name, description, classToBeApplied, script, enabled, session.getSessionId());
            return true;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Retrieves all the validator definitions in the system
     * @param className The class to be retrieved the validators from
     * @return The list of validator definitions or null if an unexpected server side error occurred
     */
    
    public List<LocalValidatorDefinition> getValidatorDefinitionsForClass(String className) {
        try {
            List<RemoteValidatorDefinition> remoteValidatorDefinitions = service.getValidatorDefinitionsForClass(className, session.getSessionId());
            List<LocalValidatorDefinition> localValidatorDefinitions = new ArrayList<>();
            
            remoteValidatorDefinitions.forEach((aRemoteValidatorDefinition) -> {
                localValidatorDefinitions.add(new LocalValidatorDefinition(aRemoteValidatorDefinition.getId(), aRemoteValidatorDefinition.getName(), aRemoteValidatorDefinition.getDescription(), 
                        aRemoteValidatorDefinition.getClassToBeApplied(), aRemoteValidatorDefinition.getScript(), aRemoteValidatorDefinition.isEnabled()));
            });
            
            return localValidatorDefinitions;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Runs the existing validations for the class associated to the given object. Validators set to enabled = false will be ignored
     * @param objectClass The class of the object
     * @param objectId The id of the object
     * @return The list of validators associated to the object and its class or null if the object can not be found
     */
    public List<LocalValidator> runValidationsForObject(String objectClass, long objectId) {
        try {
            List<RemoteValidator> remoteValidators = service.runValidationsForObject(objectClass, objectId, session.getSessionId());
            List<LocalValidator> localValidatorDefinitions = new ArrayList<>();
            
            remoteValidators.forEach((aRemoteValidatorDefinition) -> {
                localValidatorDefinitions.add(new LocalValidator(aRemoteValidatorDefinition.getName(), aRemoteValidatorDefinition.getProperties()));
            });
            
            return localValidatorDefinitions;
        } catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Deletes a validator definition
     * @param validatorDefinitionId the id of the validator to be deleted
     * @return false If the validator definition could not be found, true if everything went as expected
     */
    public boolean deleteValidatorDefinition(long validatorDefinitionId) {
        try {
            service.deleteValidatorDefinition(validatorDefinitionId, session.getSessionId());
            return true;
        } catch(Exception ex){
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
    //<editor-fold desc="BGP" defaultstate="collapsed">
    /**
     * Updates a Favorites folder
     * @param mappedBGPLinksIds
     * @return True if the Favorites folder was updated successfully
     */
    public List<LocalLogicalConnectionDetails> getBGPMap(List<String> mappedBGPLinksIds) {
        try {
            List<LocalLogicalConnectionDetails> bgpMap = new ArrayList<>();
            List<RemoteLogicalConnectionDetails> remoteBgpMap = service.getBGPMap(mappedBGPLinksIds, session.getSessionId());
            for (RemoteLogicalConnectionDetails remoteLogicalConnectionDetails : remoteBgpMap) {
                bgpMap.add(new LocalLogicalConnectionDetails(remoteLogicalConnectionDetails));
            }
            return bgpMap;
        } catch (Exception ex) {
            this.error = ex.getMessage();
        }
        return null;
    }
    
    /**
     * Deletes the provider node created with BGP sync
     * @param id id of object of class providers
     * @return true if can delete
     */
    public boolean deleteProvider(String id){
        try {
            service.deleteObject("Provider", id, false, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    /**
     * Deletes the BGPPeer created with BGP sync
     * @param id BGPPeer id
     * @return true if can be delete
     */
    public boolean deleteBgpPeer(String id){
        try {
            service.deleteObject("BGPPeer", id, true, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    //</editor-fold>
}

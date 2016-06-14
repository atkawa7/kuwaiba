/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
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
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.xml.ws.soap.SOAPFaultException;
import org.inventory.communications.core.LocalApplicationLogEntry;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectLightList;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.LocalReportDescriptor;
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.communications.core.caching.Cache;
import org.inventory.communications.core.queries.LocalQuery;
import org.inventory.communications.core.queries.LocalQueryLight;
import org.inventory.communications.core.queries.LocalResultRecord;
import org.inventory.communications.core.queries.LocalTransientQuery;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.communications.util.Constants;
import org.kuwaiba.wsclient.ApplicationLogEntry;
import org.kuwaiba.wsclient.ClassInfo;
import org.kuwaiba.wsclient.ClassInfoLight;
import org.kuwaiba.wsclient.GroupInfo;
import org.kuwaiba.wsclient.KuwaibaService;
import org.kuwaiba.wsclient.KuwaibaService_Service;
import org.kuwaiba.wsclient.RemoteBusinessObjectLight;
import org.kuwaiba.wsclient.RemoteBusinessObjectLightList;
import org.kuwaiba.wsclient.RemoteObject;
import org.kuwaiba.wsclient.RemoteObjectLight;
import org.kuwaiba.wsclient.RemoteObjectLightArray;
import org.kuwaiba.wsclient.RemoteObjectSpecialRelationships;
import org.kuwaiba.wsclient.RemoteQueryLight;
import org.kuwaiba.wsclient.ReportDescriptor;
import org.kuwaiba.wsclient.ResultRecord;
import org.kuwaiba.wsclient.SdhContainerLinkDefinition;
import org.kuwaiba.wsclient.SdhPosition;
import org.kuwaiba.wsclient.ServerSideException_Exception;
import org.kuwaiba.wsclient.StringArray;
import org.kuwaiba.wsclient.StringPair;
import org.kuwaiba.wsclient.TransientQuery;
import org.kuwaiba.wsclient.UserInfo;
import org.kuwaiba.wsclient.Validator;
import org.kuwaiba.wsclient.ViewInfo;
import org.kuwaiba.wsclient.ViewInfoLight;

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
    
    private CommunicationsStub(){
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
        try{
            service.closeSession(this.session.getSessionId());
        }catch(Exception ex){
            this.error =  ex.getMessage();
        }
    }

    /**
     * Creates a session
     * @param user The user for this session
     * @param password The password for the user
     * @return Success or failure
     */
    public boolean createSession(String user, String password){
        try{
            if (serverURL == null)
                serverURL = new URL("http", "localhost", 8080,"/kuwaiba/KuwaibaService?wsdl"); //NOI18n

            this.service = new KuwaibaService_Service(serverURL).getKuwaibaServicePort();
            
            this.session = new LocalSession(this.service.createSession(user, password));
            return true;
        }catch(Exception ex){ 
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
            List <RemoteObjectLight> children = service.getObjectChildrenForClassWithId(oid, objectClassId, 0,this.session.getSessionId());
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
        try{
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
    
    public LocalObjectLight[] getObjectsOfClassLight(String className){
        try{
            List <RemoteObjectLight> instances = service.getObjectsOfClassLight(className, 0, this.session.getSessionId());
            LocalObjectLight[] res = new LocalObjectLight[instances.size()];

            int i = 0;
            for (RemoteObjectLight rol : instances){
                res[i] = new LocalObjectLight(rol.getOid(), rol.getName(), rol.getClassName());
                i++;
            }

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
     */
    public boolean saveObject(LocalObject obj){

        try{
            List<String> attributeNames = new ArrayList<>();
            List<StringArray> attributeValues = new ArrayList<>();

            for (String key : obj.getAttributes().keySet()){
                StringArray value = new StringArray();
                attributeNames.add(key);
                if (obj.getAttribute(key) instanceof List){
                    for (long itemId : (List<Long>)obj.getAttribute(key))
                        value.getItem().add(String.valueOf(itemId));
                }else
                    value.getItem().add(obj.getAttribute(key).toString());
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
     * This is a wrapper method with the same name as the one in the webservice used to lock
     * an object as read only because an operation is being performed on it
     * @param oid the object oid
     * @param objectClass the object class
     * @param value Lock value. By now is a boolean, but I expect in the future a three level lock can be implemented (r,w,nothing)
     * @return success or failure
     */
    public boolean setObjectLock(long oid, String objectClass,boolean value){
        return true;
    }

    /**
     * Retrieves the whole object info
     * @param objectClass object class
     * @param oid object id
     * @return The local representation of the object
     */
    public LocalObject getObjectInfo(String objectClass, long oid){
        try{
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
    
    public LocalObjectLight[] getParents(String objectClass, long objectId) {
        try{
            List<RemoteObjectLight> values = service.getParents(objectClass, objectId, session.getSessionId());
            LocalObjectLight[] res = new LocalObjectLight[values.size()];
            for (int i = 0; i < values.size(); i++)
                res[i]= new LocalObjectLight(values.get(i).getOid(), values.get(i).getName(), values.get(i).getClassName());

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

    public LocalObjectLight[] getSpecialAttribute(String objectClass, long objectId, String attributeName){
        try{

            List<RemoteObjectLight> values = service.getSpecialAttribute(objectClass, objectId,attributeName, session.getSessionId());
            LocalObjectLight[] res = new LocalObjectLight[values.size()];
            for (int i = 0; i < values.size(); i++)
                res[i]= new LocalObjectLight(values.get(i).getOid(), values.get(i).getName(), values.get(i).getClassName());

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
     * @param className
     * @return allPosible children
     */
    public List<LocalClassMetadataLight> getPossibleChildren(String className, boolean ignoreCache) {
        try{
            List<LocalClassMetadataLight> resAsLocal = null;
            if (!ignoreCache){
                    resAsLocal = cache.getPossibleChildrenCached(className);
            }
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
   
    public List<LocalClassMetadataLight> getSpecialPossibleChildren(String className) {
        try{
            List<ClassInfoLight> resAsRemote = service.getSpecialPossibleChildren(className,this.session.getSessionId());
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
                                                cil.getSmallIcon(), cil.getColor(), validators));
            }
            return resAsLocal;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
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
    
   public boolean isSubclassOf (String className, String subclassOf) {
       try {
           return service.isSubclassOf(className, subclassOf, this.session.getSessionId());
       } catch (Exception ex) {
           this.error = ex.getMessage();
           return false;
       }
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
            List<ClassInfo> metas;
            metas= service.getAllClasses(includeListTypes, this.session.getSessionId());
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
                                                ci.getDescription(), ci.getAttributeIds(), 
                                                ci.getAttributeNames().toArray(new String[0]),
                                                ci.getAttributeTypes().toArray(new String[0]),
                                                ci.getAttributeDisplayNames().toArray(new String[0]),
                                                ci.getAttributesIsVisible(), ci.getAttributesDescription().toArray(new String[0]));
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
     * @return the metadata information
     */
    public LocalClassMetadata getMetaForClass(String className, boolean ignoreCache){
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
                                            cm.getDescription(), cm.getAttributeIds(), 
                                            cm.getAttributeNames().toArray(new String[0]),
                                            cm.getAttributeTypes().toArray(new String[0]),
                                            cm.getAttributeDisplayNames().toArray(new String[0]),
                                            cm.getAttributesIsVisible(), cm.getAttributesDescription().toArray(new String[0]));
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
    public LocalClassMetadata getMetaForClass(long classId, boolean ignoreCache){
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
                        cm.getDescription(), cm.getAttributeIds(), 
                        cm.getAttributeNames().toArray(new String[0]),
                        cm.getAttributeTypes().toArray(new String[0]),
                        cm.getAttributeDisplayNames().toArray(new String[0]),
                        cm.getAttributesIsVisible(), cm.getAttributesDescription().toArray(new String[0]));
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
    /*public LocalClassMetadataLight getLightMetaForClass(long classId, boolean ignoreCache){
        try{
            LocalClassMetadataLight res;
//            if (!ignoreCache){
//                res = cache.getLightMetaForClass(className);
//                if (res != null)
//                    return res;
//            }

            ClassInfo cm = service.getClassWithId(classId,this.session.getSessionId());

            res = new LocalClassMetadataLight(cm);
            cache.addLightMeta(new LocalClassMetadataLight[]{res});
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }*/

    public byte[] getClassHierarchy(boolean showAll) {
        try{
            return service.getClassHierarchy(showAll, session.getSessionId());
        }catch(Exception ex){
            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName()+": "+ ex.getMessage();
            return null;
        }
    }

    public LocalClassMetadataLight[] getLightSubclasses(String className, boolean includeAbstractSubClasses, boolean includeSelf) {
        try{
            List<ClassInfoLight> subClasses = service.getSubClassesLight(className, includeAbstractSubClasses, includeSelf, session.getSessionId());
            LocalClassMetadataLight[] res = new LocalClassMetadataLight[subClasses.size()];

            int i = 0;
            for (ClassInfoLight cil : subClasses){
                HashMap<String, Integer> validators = new HashMap<>();
                    for (Validator validator : cil.getValidators())
                        validators.put(validator.getLabel(), validator.getValue());
                    
                res[i] = new LocalClassMetadataLight(cil.getId(),
                                cil.getClassName(),
                                cil.getDisplayName(),
                                cil.getParentClassName(),
                                cil.isAbstract(),cil.isViewable(), cil.isListType(),
                                cil.isCustom(), cil.isInDesign(),
                                cil.getSmallIcon(), cil.getColor(), validators);
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
            else{
                return res.subList(1, res.size());
            }
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Object methods. Click on the + sign on the left to edit the code.">
    /**
     * 
     * @param objectClass
     * @param parentClass
     * @param parentOid
     * @param template
     * @return 
     */
    public LocalObjectLight createObject(String objectClass, String parentClass, long parentOid, long template){
        try{
            long objectId  = service.createObject(objectClass,parentClass, parentOid, new ArrayList<String>(),new ArrayList<StringArray>(),template,this.session.getSessionId());
            return new LocalObjectLight(objectId, null, objectClass);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight createSpecialObject(String className, String parentClassName, 
            long parentOid, long templateId) {
        try{
            long objectId  = service.createSpecialObject(className,parentClassName, parentOid, 
                    new ArrayList<String>(),new ArrayList<StringArray>(),templateId,this.session.getSessionId());
            return new LocalObjectLight(objectId, null, className);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * 
     * @param parentClassId
     * @param possibleChildren
     * @return 
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
     * Removes possible children from the given class container hierarchy
     * @param Id for the parent class
     * @param childrenToBeDeleted List if ids of the classes to be removed as possible children
     * @return Success or failure
     */
    public boolean removePossibleChildren(long parentClassId, long[] childrenToBeDeleted){
        try{
            List<Long> pChildren = new ArrayList<Long>();
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
     * Deletes the given object
     * @param classNames Object classes
     * @param oids object ids
     * @return Success or failure
     */
    public boolean deleteObjects(List<String> classNames, List<Long> oids){
        try{
            service.deleteObjects(classNames, oids, false, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }

    public boolean moveObjects(String targetClass, long targetOid, LocalObjectLight[] objects) {

        try{
            List<Long> objectOids = new ArrayList<Long>();
            List<String> objectClasses = new ArrayList<String>();

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
            List<Long> objectOids = new ArrayList<Long>();
            List<String> objectClasses = new ArrayList<String>();

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
        try{
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
     * @param type This can be either the type name or its id
     * @param connectionClass Class for the corresponding connection to be created
     * @return A local object light representing the new connection
     */
    public LocalObjectLight createPhysicalConnection(String endpointAClass, long endpointAId,
            String endpointBClass, long endpointBId, String parentClass, long parentId, String name, String type, String connectionClass) {
        try{
            List<StringArray> values = new ArrayList<StringArray>();
            StringArray valueName = new StringArray();
            valueName.getItem().add(name);

            StringArray valueType = new StringArray();
            if (type != null && !type.equals("0")) //0 is the dummy id of a null list type item
                valueType.getItem().add(type);

            values.add(valueName);
            values.add(valueType);

            long myObjectId = service.createPhysicalConnection(endpointAClass, endpointAId,
                    endpointBClass, endpointBId, parentClass, parentId, Arrays.asList(new String[]{"name","type"}), values, connectionClass, this.session.getSessionId());
            return new LocalObjectLight(myObjectId, "", connectionClass);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public List<Long> createBulkPhysicalConnections(String connectionClass, int integer, String parentClass, long parentId) {
        try{
            return service.createBulkPhysicalConnections(connectionClass, 
                        integer, parentClass, parentId, session.getSessionId());
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight[] getConnectionEndpoints(String connectionClass, long connectionId) {
        try{
            List<RemoteObjectLight> endpoints = service.getConnectionEndpoints(connectionClass, connectionId, session.getSessionId());
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
    
    public boolean connectPhysicalLinks(String[] sideAClassNames, Long[] sideAIds, 
                String[] linksClassNames, Long[] linksIds, String[] sideBClassNames, 
                Long[] sideBIds) {
        try{
            List<String> sideAClassNamesList = new ArrayList<>();
            List<String> linksClassNamesList = new ArrayList<>();
            List<String> sideBClassNamesList = new ArrayList<>();
            List<Long> sideAIdsList = new ArrayList<>();
            List<Long> linksIdsList = new ArrayList<>();
            List<Long> sideBIdsList = new ArrayList<>();
            sideAClassNamesList.addAll(Arrays.asList(sideAClassNames));
            linksClassNamesList.addAll(Arrays.asList(linksClassNames));
            sideBClassNamesList.addAll(Arrays.asList(sideBClassNames));
            sideAIdsList.addAll(Arrays.asList(sideAIds));
            linksIdsList.addAll(Arrays.asList(linksIds));
            sideBIdsList.addAll(Arrays.asList(sideBIds));
            
            service.connectPhysicalLinks(sideAClassNamesList, sideAIdsList, linksClassNamesList, linksIdsList, sideBClassNamesList, sideBIdsList, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }
    
    //Service Manager
    public boolean associateObjectsToService(String[] objectClass, Long [] objectId, String serviceClass, long serviceId){
        try{
            List<String> objectsClassList = new ArrayList<>();
            List<Long> objectsIdList = new ArrayList<>();
            objectsClassList.addAll(Arrays.asList(objectClass));
            objectsIdList.addAll(Arrays.asList(objectId));
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
    
    public LocalObjectLight[] getServiceResources(String serviceClass, long serviceId){
        try{
            List <RemoteObjectLight> instances = service.getServiceResources(serviceClass, serviceId, this.session.getSessionId());
            LocalObjectLight[] res = new LocalObjectLight[instances.size()];

            int i = 0;
            for (RemoteObjectLight rol : instances){
                res[i] = new LocalObjectLight(rol.getOid(), rol.getName(), rol.getClassName());
                i++;
            }

            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight createService (String serviceClass, String customerClass, long customerId, String attributes[], String attributeValues) {
        try{
            long newServiceId = service.createService(serviceClass, customerClass, 
                    customerId, null, null, this.session.getSessionId());
            return new LocalObjectLight(newServiceId, null, serviceClass);

        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    public LocalObjectLight createCustomer (String customerClass, String attributes[], String attributeValues) {
        try{
            long newServiceId = service.createCustomer(customerClass, null, null, this.session.getSessionId());
            return new LocalObjectLight(newServiceId, null, customerClass);

        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight[] getServices(String customerClass, long customerId) {
        try{
            List <RemoteObjectLight> instances = service.getServices(customerClass, customerId, this.session.getSessionId());
            LocalObjectLight[] res = new LocalObjectLight[instances.size()];

            int i = 0;
            for (RemoteObjectLight rol : instances){
                res[i] = new LocalObjectLight(rol.getOid(), rol.getName(), rol.getClassName());
                i++;
            }
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    //End Service Manager
    
    public LocalObjectLight[] getObjectSpecialChildren(String objectClass, long objectId) {
        try{
            List<RemoteObjectLight> specialChildren = service.getObjectSpecialChildren (
                    objectClass, objectId, session.getSessionId());
            LocalObjectLight[] res = new LocalObjectLight[specialChildren.size()];
            int i = 0;
            for (RemoteObjectLight rol : specialChildren){
                res[i] = new LocalObjectLight(rol.getOid(), rol.getName(), rol.getClassName());
                i++;
            }
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
     * Reset the cache to the default cleaning all hashes:
     */
    public void resetCache(){
        //Wipe out hashes
        cache.resetMetadataIndex();
        cache.resetLightMetadataIndex();
        cache.resetPossibleChildrenCached();
        cache.resetLists();
    }

    /**
     * Refreshes all existing objects, according to the flags provided
     */
    public void refreshCache(boolean refreshMeta, boolean refreshLightMeta,
            boolean refreshList, boolean refreshPossibleChildren){
        try{
            if (refreshMeta){
                for (LocalClassMetadata lcm : cache.getMetadataIndex()){
                    ClassInfo cm = service.getClass(lcm.getClassName(),this.session.getSessionId());
                    HashMap<String, Integer> validators = new HashMap<String, Integer>();
                    for (Validator validator : cm.getValidators())
                        validators.put(validator.getLabel(), validator.getValue());
            
                    LocalClassMetadata myLocal = new LocalClassMetadata(cm.getId(),
                            cm.getClassName(),
                            cm.getDisplayName(),
                            cm.getParentClassName(),
                            cm.isAbstract(),cm.isViewable(), cm.isListType(),
                            cm.isCustom(), cm.isInDesign(),
                            cm.getSmallIcon(), cm.getColor(), validators, cm.getIcon(),
                            cm.getDescription(), cm.getAttributeIds(), 
                            cm.getAttributeNames().toArray(new String[0]),
                            cm.getAttributeTypes().toArray(new String[0]),
                            cm.getAttributeDisplayNames().toArray(new String[0]),
                            cm.getAttributesIsVisible(), cm.getAttributesDescription().toArray(new String[0]));
                    
                    cache.addMeta(new LocalClassMetadata[]{myLocal});
                }
            }
            if (refreshLightMeta){
                List<ClassInfoLight> myLocalLight  = service.getAllClassesLight(true, this.session.getSessionId());
                if (myLocalLight != null){
                    getAllLightMeta(true);
                }
            }

            if (refreshList){
                HashMap<String, List<LocalObjectListItem>> myLocalList = cache.getAllList();
                for (String key : myLocalList.keySet()){
                    myLocalList.remove(key);
                    getList(key,false,true);
                }
            }
            if (refreshPossibleChildren){
                HashMap<String, List<LocalClassMetadataLight>> myLocalPossibleChildren
                        = cache.getAllPossibleChildren();
                for (String key : myLocalPossibleChildren.keySet()){
                    myLocalPossibleChildren.remove(key);
                    getPossibleChildren(key,true);
                }
            }
        }catch(Exception ex){
            this.error = ex.getMessage();
        }
    }
    
    public boolean setAttributeProperties(long classId, long attributeId, String name, String displayName,
            String type, String description, Boolean administrative, Boolean visible, Boolean readOnly, Boolean noCopy, Boolean unique)  {
        try{
            service.setAttributePropertiesForClassWithId(classId, attributeId, name, displayName, type, description,
                    administrative, visible, readOnly, unique, noCopy, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
        
    public long createClassMetadata(String className, String displayName, String description, String parentClassName, boolean custom, boolean countable, int color, boolean _abstract, boolean inDesign){
        try{
            return service.createClass(className, displayName, description, _abstract, custom, countable, inDesign, parentClassName, null, null, color, this.session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return -1;
        }
    }
    
    public boolean deleteClassMetadata(long classId){
        try{
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
                                boolean unique){
        try{
            service.createAttributeForClassWithId(classId, name, displayName, type, description, administrative, visible, readOnly, noCopy, unique, this.session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
        return true;
    }
    
    public boolean createAttribute(String className, String name, String displayName, 
                                String description, String type, boolean administrative,
                                boolean readOnly, boolean visible, boolean noCopy, 
                                boolean unique){
        try{
            service.createAttribute(className, name, displayName, type, description, administrative, visible, readOnly, noCopy, unique, this.session.getSessionId());
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
                    _abstract, inDesign, countable, custom, this.session.getSessionId());
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
    
    // <editor-fold defaultstate="collapsed" desc="user/groups methods. Click on the + sign on the left to edit the code.">
    /**
     * Retrieves the user list
     * @return An array of LocalUserObject
     */
    public LocalUserObject[] getUsers() {
        try{
            List<UserInfo> users = service.getUsers(this.session.getSessionId());
            LocalUserObject[] localUsers = new LocalUserObject[users.size()];

            int i = 0;
            for (UserInfo user : users){
                localUsers[i] = (LocalUserObject) new LocalUserObject(user);
                i++;
            }
            return localUsers;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    /**
     * Retrieves the group list
     * @return An array of LocalUserObject
     */
    public LocalUserGroupObject[] getGroups() {
        try{
            List<GroupInfo> groups = service.getGroups(this.session.getSessionId());
            LocalUserGroupObject[] localGroups = new LocalUserGroupObject[groups.size()];

            int i = 0;
            for (GroupInfo group : groups){
                localGroups[i] = (LocalUserGroupObject) new LocalUserGroupObject(group);
                i++;
            }
            return localGroups;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Creates a new user
     * @return The newly created user
     */
    public LocalUserObject addUser(){
        try{
            Random random = new Random();
            UserInfo newUser = new UserInfo();
            newUser.setUserName("user"+random.nextInt(10000));
            newUser.setId(service.createUser(newUser.getUserName(), "kuwaiba", null, null, true, null, null, this.session.getSessionId()));
            return new LocalUserObject(newUser);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Set user attributes (group membership is managed using other methods)
     * @param update
     * @return success or failure
     */
    public boolean setUserProperties(long oid, String userName, String password, String firstName,
            String lastName, long[] groups) {
        try{
            List<Long> myGroups = new ArrayList<Long>();
            for (long aGroup : groups)
                myGroups.add(aGroup);
            service.setUserProperties(oid, userName, firstName, lastName, password, true, null, myGroups, this.session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
        return true;
    }

    /**
     * Set user attributes (group membership is managed using other methods)
     * @param update
     * @return success or failure
     */
    public boolean setGroupProperties(long oid, String groupName, String description) {
        try{
            service.setGroupProperties(oid, groupName, description, null, null, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
        
    }

    /**
     * Creates a new group
     * @return The newly created group
     */
    public LocalUserGroupObject addGroup(){
        try{
            Random random = new Random();
            GroupInfo newGroup = new GroupInfo();
            newGroup.setName("group"+random.nextInt(10000));
            newGroup.setId(service.createGroup(newGroup.getName(), null, null, null, this.session.getSessionId()));
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
    public boolean deleteUsers(long[] oids){
        try{
            ArrayList<Long> objects = new ArrayList<Long>();
            for (long oid : oids)
                objects.add(oid);
            service.deleteUsers(objects,session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
        }
        return true;
    }


    /**
     * Removes a list of groups
     * @param oids oids for the users to be deleted
     * @return success or failure
     */
    public boolean deleteGroups(long[] oids){
        try{
            ArrayList<Long> objects = new ArrayList<Long>();
            for (long oid : oids)
                objects.add(oid);
            service.deleteGroups(objects,session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
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
     * @param view id
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
     * Creates a pool
     * @param parentId the Parent id if parentId is -1 it means it has no parent.
     * @param name Pool name
     * @param description Pool description
     * @param className What kind of objects can this pool contain?
     * @return The newly created pool
     */
    public LocalObjectLight createPool(long parentId, String name, String description, String className){
        try{
            long objectId  = service.createPool(parentId, name, description, className,session.getSessionId());
            return new LocalObjectLight(objectId, name, className);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
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
    
    /**
     * Returns the list of pools available for a specific parent
     * @param parentId
     * @param className
     * @return The list of pools
     */
    public List<LocalObjectLight> getPools(long parentId, String className) {
        try{
            List <RemoteObjectLight> children = service.getPoolsForParentWithId(-1, parentId, className, this.session.getSessionId());
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
    
    /**
     * Returns the list of pools available
     * @param className
     * @return The list of pools
     */
    public List<LocalObjectLight> getPools(String className) {
        try{
            List <RemoteObjectLight> children = service.getPools(-1, className, this.session.getSessionId());
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
    // </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Reporting methods">
    /**
     * Retrieves the list of reports for a particular class
     * @param className The class to evaluate
     * @param limit The limit of results. Use -1 to retrieve all
     * @return A list of report descriptors or null if something went wrong
     */
    public List<LocalReportDescriptor> getReportsForClass(String className, int limit) {
        try {
            
            List<LocalReportDescriptor> localDescriptors = cache.getCachedReports(className);
            
            if (localDescriptors == null) {
                List<ReportDescriptor> remoteDescriptors = service.getReportsForClass(className, limit, session.getSessionId());

                for (ReportDescriptor aRemoteDescriptor : remoteDescriptors)
                    localDescriptors.add(new LocalReportDescriptor(aRemoteDescriptor.getClassName(), aRemoteDescriptor.getId(),
                                                    aRemoteDescriptor.getName(), aRemoteDescriptor.getDescription()));
                cache.addReport(className, localDescriptors);
            }
            return localDescriptors;
        }catch(Exception ex) {
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    /**
     * Executes a report
     * @param reportId Report id
     * @param arguments Arguments for this report as a key-value structure.
     * @return the html structure to be rendered
     */
    public byte[] executeReport(long reportId, HashMap<String, Object> arguments) {
        try {
            List<StringPair> remoteArguments = new ArrayList<>();
            
            for (String key : arguments.keySet()) {
                StringPair remoteArgument = new StringPair();
                remoteArgument.setKey(key);
                remoteArgument.setValue(String.valueOf(arguments.get(key)));
                remoteArguments.add(remoteArgument);
            }
            return service.executeReport(reportId, remoteArguments, session.getSessionId());
        }catch(Exception ex){
            this.error =  ex.getMessage();
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
    
    
    public List<LocalObjectLight> getSubnetPools(long parentId){
        try{
            List <RemoteObjectLight> poolRoots = service.getSubnetPools(-1, parentId, this.session.getSessionId());
            List <LocalObjectLight> res = new ArrayList<>();
            
            for (RemoteObjectLight rol : poolRoots){
                HashMap<String, Integer> validators = new HashMap<>();
                for (Validator validator : rol.getValidators())
                    validators.put(validator.getLabel(), validator.getValue());
                res.add(new LocalObjectLight(rol.getClassName(), rol.getName(), rol.getOid(), validators));
            }
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalObject getSubnetPool(long id){
        try{
            LocalClassMetadata lcmd = getMetaForClass(Constants.CLASS_SUBNET, false);
            RemoteObject subnetPool = service.getSubnetPool(id,this.session.getSessionId());
            List<List<String>> values = new ArrayList<>();
            for (StringArray value : subnetPool.getValues())
                values.add(value.getItem());
            return new LocalObject(subnetPool.getClassName(), subnetPool.getOid(), 
                    subnetPool.getAttributes(), values,lcmd);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public LocalObject getSubnet(long id){
        try{
            LocalClassMetadata lcmd = getMetaForClass(Constants.CLASS_SUBNET, false);
            RemoteObject subnet = service.getSubnet(id,this.session.getSessionId());
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
    
    public LocalObjectLight createSubnetPool(long parentId, String subnetPoolName, 
            String subnetPoolDescription, int type) {
         try{
             long objectId = service.createSubnetPool(parentId, subnetPoolName, subnetPoolDescription, type, this.session.getSessionId());
             return new LocalObjectLight(objectId, subnetPoolName, Constants.CLASS_SUBNET);
         }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    public boolean deleteSubnet(List<Long> oids){
        try{
            service.deleteSubnets(oids, false, this.session.getSessionId());
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
        
    public LocalObjectLight createSubnet(long poolId, LocalObject obj){
        try {
            List<String> attributeNames = new ArrayList<>();
            List<StringArray> attributeValues = new ArrayList<>();

            for (String key : obj.getAttributes().keySet()){
                StringArray value = new StringArray();
                attributeNames.add(key);
                if (obj.getAttribute(key) instanceof List){
                    for (long itemId : (List<Long>)obj.getAttribute(key))
                        value.getItem().add(String.valueOf(itemId));
                }else
                    value.getItem().add(obj.getAttribute(key).toString());
                attributeValues.add(value);
            }
            
            long objectId  = service.createSubnet(poolId, attributeNames, attributeValues, this.session.getSessionId());
            return new LocalObjectLight(objectId, obj.getName(), Constants.CLASS_SUBNET);
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
    
    public LocalObjectLight addIP(long id, LocalObject obj){
        try {
            List<String> attributeNames = new ArrayList<>();
            List<StringArray> attributeValues = new ArrayList<>();

            for (String key : obj.getAttributes().keySet()){
                StringArray value = new StringArray();
                attributeNames.add(key);
                if (obj.getAttribute(key) instanceof List){
                    for (long itemId : (List<Long>)obj.getAttribute(key))
                        value.getItem().add(String.valueOf(itemId));
                }else
                    value.getItem().add(obj.getAttribute(key).toString());
                attributeValues.add(value);
            }
            
            long objectId  = service.addIP(id, attributeNames, attributeValues, this.session.getSessionId());
            return new LocalObjectLight(objectId, obj.getName(), Constants.CLASS_SUBNET);
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
    
    public boolean relateIPtoDevice(long id, String classDevice, long deviceId){
        try{
            service.relateIPtoDevice(id, classDevice, deviceId, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public List<LocalObjectLight> getSubnetUsedIps(long id){
        try {
            List<LocalObjectLight> res = new ArrayList<>();
            for (RemoteObjectLight anIp : service.getSubnetUsedIps(id, 0, this.session.getSessionId())) 
                res.add(new LocalObjectLight(anIp.getOid(), anIp.getName(), anIp.getClassName()));
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
        }
        return null;
    }
    
    public boolean relateSubnetToVLAN(long id, long vlanId){
        try{
            service.relateSubnetToVlan(id, vlanId, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    public boolean releaseIPfromDevice(String deviceClassName, long deviceId, long id){
        try{
            service.releaseIPFromDevice(deviceClassName, deviceId, id, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    public boolean releaseSubnetFromVLAN(long vlanId, long id){
        try{
            service.releaseSubnetFromVlan(vlanId, id, this.session.getSessionId());
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
}

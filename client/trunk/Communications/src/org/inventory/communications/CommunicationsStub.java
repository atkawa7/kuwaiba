/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import javax.xml.ws.soap.SOAPFaultException;
import org.inventory.communications.core.LocalClassMetadataImpl;
import org.inventory.communications.core.LocalClassMetadataLightImpl;
import org.inventory.communications.core.LocalObjectImpl;
import org.inventory.communications.core.LocalObjectLightImpl;
import org.inventory.communications.core.LocalObjectListItemImpl;
import org.inventory.communications.core.LocalUserGroupObjectImpl;
import org.inventory.communications.core.LocalUserObjectImpl;
import org.inventory.communications.core.queries.LocalQueryImpl;
import org.inventory.communications.core.queries.LocalQueryLightImpl;
import org.inventory.communications.core.queries.LocalResultRecordImpl;
import org.inventory.communications.core.queries.LocalTransientQueryImpl;
import org.inventory.communications.core.views.LocalObjectViewImpl;
import org.inventory.communications.core.views.LocalObjectViewLightImpl;
import org.inventory.core.services.api.LocalObject;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.LocalObjectListItem;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.queries.LocalQuery;
import org.inventory.core.services.api.queries.LocalQueryLight;
import org.inventory.core.services.api.queries.LocalResultRecord;
import org.inventory.core.services.api.queries.LocalTransientQuery;
import org.inventory.core.services.api.session.LocalUserGroupObject;
import org.inventory.core.services.api.session.LocalUserObject;
import org.inventory.core.services.api.visual.LocalObjectView;
import org.inventory.core.services.api.visual.LocalObjectViewLight;
import org.inventory.core.services.caching.Cache;
import org.inventory.core.services.factories.ObjectFactory;
import org.kuwaiba.wsclient.ClassInfo;
import org.kuwaiba.wsclient.ClassInfoLight;
import org.kuwaiba.wsclient.Kuwaiba;
import org.kuwaiba.wsclient.KuwaibaService;
import org.kuwaiba.wsclient.RemoteObject;
import org.kuwaiba.wsclient.RemoteObjectLight;
import org.kuwaiba.wsclient.RemoteQueryLight;
import org.kuwaiba.wsclient.ResultRecord;
import org.kuwaiba.wsclient.StringArray;
import org.kuwaiba.wsclient.TransientQuery;
import org.kuwaiba.wsclient.UserGroupInfo;
import org.kuwaiba.wsclient.UserInfo;
import org.kuwaiba.wsclient.ViewInfo;
import org.kuwaiba.wsclient.ViewInfoLight;

/**
 * Singleton class that provides communication and caching services to the rest of the modules
 * TODO: Make it a thread to support simultaneous operations
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class CommunicationsStub {
    private static CommunicationsStub instance=null;
    private KuwaibaService service;
    private Kuwaiba port;
    private static URL serverURL = null;
    private String error = java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_ERROR");
    private Cache cache;
    private LocalSession session;
    
    private CommunicationsStub(){
        if (serverURL == null){
            try{
                //Default values
                serverURL = new URL("http", "localhost", 8080,"/kuwaiba/KuwaibaService?wsdl"); //NOI18n
            }catch (MalformedURLException mue){
                Logger.getAnonymousLogger("Malformed URL: "+mue.getMessage());
            }
        }
        this.service = new KuwaibaService(serverURL);
        this.port = service.getKuwaibaPort();
        cache = Cache.getInstace();
    }

    //Implements the singleton pattern
    public static CommunicationsStub getInstance(){
            if(instance==null) instance = new CommunicationsStub();
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
     * @param _URL A valid URL
     */
    public static void setServerURL(URL _URL){
        serverURL = _URL;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Session methods. Click on the + sign on the left to edit the code.">
    public LocalSession getSession(){
        return session;
    }
    
    /**
     * This method closes the current session
     * @return Success or failure
     */
    public void closeSession(){
        try{
            port.closeSession(this.session.getSessionId());
        }catch(Exception ex){
            this.error =  ex.getMessage();
        }
    }

    /**
     *
     * @param user The user for this session
     * @param password The password for the user
     * @return Success or failure
     */
    public boolean createSession(String user, String password){
        try{
            this.session = new LocalSession(port.createSession(user, password));
            return true;
        }catch(Exception ex){ 
            this.error =  ex.getMessage();
            return false;
        }
    }// </editor-fold>

    /**
     * Retrieves an object children providing the object class id
     * @return an array of local objects representing the object's children. Null in a problem occurs during the execution
     */
    public List<LocalObjectLight> getObjectChildren(long oid, long objectClassId){
        try{
            List <RemoteObjectLight> children = port.getObjectChildrenForClassWithId(oid, objectClassId, 0,this.session.getSessionId());
            List <LocalObjectLight> res = new ArrayList<LocalObjectLight>();

            for (RemoteObjectLight rol : children){
                res.add(new LocalObjectLightImpl(rol));
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
//            List <RemoteObjectLight> children = port.getObjectChildren(oid, className, 0,this.session.getSessionId());
//            List <LocalObjectLight> res = new ArrayList<LocalObjectLight>();
//
//            for (RemoteObjectLight rol : children)
//                res.add(new LocalObjectLightImpl(rol));
//
//            return res;
            return null;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }

    public List<LocalObject> getChildrenOfClass(long oid, String parentClassName, String childrenClassName){
        try{
            List <RemoteObject> children = port.getChildrenOfClass(oid, parentClassName, childrenClassName, 0, this.session.getSessionId());
            List <LocalObject> res = new ArrayList<LocalObject>();

            for (RemoteObject rol : children){
                res.add(new LocalObjectImpl(rol, getMetaForClass(rol.getClassName(), false)));
            }
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }// </editor-fold>

    /**
     * Updates the attributes of a given object
     * @param obj is the object to be updated. Note that this object doesn't have
     *            every field within the "original". it only has the field(s) to be updated
     */
    public boolean saveObject(LocalObject obj){

        try{
            List<String> attributeNames = new ArrayList<String>();
            List<StringArray> attributeValues = new ArrayList<StringArray>();

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
            port.updateObject(obj.getClassName(),obj.getOid(), attributeNames, attributeValues, this.session.getSessionId());
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
            RemoteObject myObject = port.getObject(objectClass, oid,this.session.getSessionId());
            return new LocalObjectImpl(myObject,lcmd);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    public long[] getSpecialAttribute(String objectClass, long objectId, String attributeName){
        try{

            List<String> values = port.getSpecialAttribute(objectClass, objectId,attributeName, session.getSessionId());
            long[] res = new long[values.size()];
            for (int i = 0; i < values.size(); i++)
                res[i]= Long.valueOf(values.get(i));

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
            RemoteObjectLight myLocalObject = port.getObjectLight(objectClass, oid,this.session.getSessionId());
            return new LocalObjectLightImpl(myLocalObject);
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
                resAsLocal = new ArrayList<LocalClassMetadataLight>();
                List<ClassInfoLight> resAsRemote = port.getPossibleChildren(className,this.session.getSessionId());

                for (ClassInfoLight cil : resAsRemote){
                    resAsLocal.add(new LocalClassMetadataLightImpl(cil));
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
            List<ClassInfoLight> resAsRemote = port.getPossibleChildrenNoRecursive(className,this.session.getSessionId());
            List<LocalClassMetadataLight> resAsLocal = new ArrayList<LocalClassMetadataLight>();

            for (ClassInfoLight cil : resAsRemote){
                resAsLocal.add(new LocalClassMetadataLightImpl(cil));
            }
            return resAsLocal;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
      
   public List<LocalClassMetadataLight> getUpstreamContainmentHierarchy(String className, boolean recursive){
        try{
            List<LocalClassMetadataLight> res = new ArrayList<LocalClassMetadataLight>();
            for (ClassInfoLight cil : port.getUpstreamContainmentHierarchy(className, recursive, this.session.getSessionId())){
                res.add(new LocalClassMetadataLightImpl(cil));
            }
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
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
            List<ClassInfoLight> metas = port.getAllClassesLight(includeListTypes, this.session.getSessionId());

            LocalClassMetadataLight[] lm = new LocalClassMetadataLight[metas.size()];
            int i=0;
            for (ClassInfoLight cm : metas){
                lm[i] = (LocalClassMetadataLight)new LocalClassMetadataLightImpl(cm);
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
            metas= port.getAllClasses(includeListTypes, this.session.getSessionId());
            LocalClassMetadata[] lm = new LocalClassMetadata[metas.size()];
            int i=0;
            for (ClassInfo cm : metas){
                lm[i] = new LocalClassMetadataImpl(cm);
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

            ClassInfo cm = port.getClass(className,this.session.getSessionId());

            res = new LocalClassMetadataImpl(cm);
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
            ClassInfo cm = port.getClassWithId(classId,this.session.getSessionId());

            res = new LocalClassMetadataImpl(cm);
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
    public LocalClassMetadataLight getLightMetaForClass(String className, boolean ignoreCache){
        try{
            LocalClassMetadataLight res;
            if (!ignoreCache){
                res = cache.getLightMetaForClass(className);
                if (res != null)
                    return res;
            }

            ClassInfo cm = port.getClass(className,this.session.getSessionId());

            res = new LocalClassMetadataLightImpl(cm);
            cache.addLightMeta(new LocalClassMetadataLight[]{res});
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Retrieves the metadata for a given class providing its ide
     * @param className the object class
     * @return the metadata information
     */
    public LocalClassMetadataLight getLightMetaForClass(long classId, boolean ignoreCache){
        try{
            LocalClassMetadataLight res;
            /*if (!ignoreCache){
                res = cache.getLightMetaForClass(className);
                if (res != null)
                    return res;
            }*/

            ClassInfo cm = port.getClassWithId(classId,this.session.getSessionId());

            res = new LocalClassMetadataLightImpl(cm);
            cache.addLightMeta(new LocalClassMetadataLight[]{res});
            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    public byte[] getClassHierarchy(boolean showAll) {
        try{
            return port.getClassHierarchy(showAll, session.getSessionId());
        }catch(Exception ex){
            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName()+": "+ ex.getMessage();
            return null;
        }
    }

    public List<LocalClassMetadataLight> getLightSubclasses(String className, boolean includeAbstractSubClasses, boolean includeSelf) {
        try{
            List<ClassInfoLight> subClasses = port.getSubClassesLight(className, includeAbstractSubClasses, includeSelf, session.getSessionId());
            List <LocalClassMetadataLight> res = new ArrayList<LocalClassMetadataLight>();

            for (ClassInfoLight rol : subClasses){
                res.add(new LocalClassMetadataLightImpl(rol));
            }
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
     public List<LocalClassMetadataLight> getLightSubclassesNoRecursive(String className, boolean includeAbstractSubClasses, boolean includeSelf) {
        try{
            List<ClassInfoLight> subClasses = port.getSubClassesLightNoRecursive(className, includeAbstractSubClasses, includeSelf, session.getSessionId());
            List <LocalClassMetadataLight> res = new ArrayList<LocalClassMetadataLight>();

            for (ClassInfoLight rol : subClasses){
                res.add(new LocalClassMetadataLightImpl(rol));
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
    public LocalObjectLight createListTypeItem(String className) {
        try {
            long myObjectId = port.createListTypeItem(className, "", "", this.session.getSessionId());
            return new LocalObjectLightImpl(myObjectId, null, className);
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
    public List<LocalObjectListItem> getList(String className, boolean includeNullValue,boolean ignoreCache){
        try{
            List<LocalObjectListItem> res = null;
            if (!ignoreCache){
                res = cache.getListCached(className);
            }
            if (res == null){
               res = new ArrayList<LocalObjectListItem>();
               res.add(ObjectFactory.createNullItem());

                List<RemoteObjectLight> remoteList = port.getListTypeItems(className,this.session.getSessionId());

                for(RemoteObjectLight entry : remoteList){
                    res.add(new LocalObjectListItemImpl(entry.getOid(),entry.getClassName(),entry.getName()));
                }
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
            long objectId  = port.createObject(objectClass,parentClass, parentOid, new ArrayList<String>(),new ArrayList<StringArray>(),template,this.session.getSessionId());
            return new LocalObjectLightImpl(objectId, null, objectClass);
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
            port.addPossibleChildrenForClassWithId(parentClassId, pChildren,this.session.getSessionId());
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
            port.removePossibleChildrenForClassWithId(parentClassId, pChildren,this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }

    /**
     * Deletes the given object
     * @param className Object class (including its package)
     * @param oid object id
     * @return Success or failure
     */
    public boolean deleteObject(String className, long oid){
        try{
            List classes = new ArrayList();
            classes.add(className);
            List ids = new ArrayList();
            ids.add(oid);
            port.deleteObjects(classes, ids, false, this.session.getSessionId());
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
            port.moveObjects(targetClass, targetOid, objectClasses, objectOids,this.session.getSessionId());
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
            List<Long> objs = port.copyObjects(targetClass, targetOid, objectClasses, objectOids, true, this.session.getSessionId());

            LocalObjectLight[] res = new LocalObjectLight[objs.size()];
            for (int i = 0; i < res.length ; i++){
                res[i] = new LocalObjectLightImpl(objs.get(i), objects[i].getName(), objects[i].getClassName());
                i++;
            }
            return res;

        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
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
            valueType.getItem().add(type);

            values.add(valueName);
            values.add(valueType);

            long myObjectId = port.createPhysicalConnection(endpointAClass, endpointAId,
                    endpointBClass, endpointBId, parentClass, parentId, Arrays.asList(new String[]{"name","type"}), values, connectionClass, this.session.getSessionId());
            return new LocalObjectLightImpl(myObjectId, "", connectionClass);
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
            TransientQuery remoteQuery = LocalTransientQueryImpl.toTransientQuery(query);
            List<ResultRecord> myResult = port.executeQuery(remoteQuery,session.getSessionId());
            LocalResultRecordImpl[] res = new LocalResultRecordImpl[myResult.size()];
            //The first record is used to store the table headers
            res[0] = new LocalResultRecordImpl(null, myResult.get(0).getExtraColumns());
            for (int i = 1; i<res.length ; i++)
                res[i] = new LocalResultRecordImpl(
                        new LocalObjectLightImpl(myResult.get(i).getObject()), myResult.get(i).getExtraColumns());
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
            return port.createQuery(queryName, isPublic ? -1 : session.getUserId(), queryStructure, description, session.getSessionId());
            
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
            port.saveQuery(query.getId(),query.getName(),
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
            port.deleteQuery(queryId, session.getSessionId());
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
            List<RemoteQueryLight> queries = port.getQueries(showAll, session.getSessionId());
            LocalQueryLightImpl[] res = new LocalQueryLightImpl[queries.size()];
            int i = 0;
            for (RemoteQueryLight query : queries){
                res[i] = new LocalQueryLightImpl(query);
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
            return new LocalQueryImpl(port.getQuery(queryId, session.getSessionId()));
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
                    LocalClassMetadata myLocal =
                            new LocalClassMetadataImpl(port.getClass(lcm.getClassName(),this.session.getSessionId()));
                    if(myLocal!=null){
                        cache.addMeta(new LocalClassMetadata[]{myLocal});
                    }
                }
            }
            if (refreshLightMeta){
                List<ClassInfoLight> myLocalLight  = port.getAllClassesLight(true, this.session.getSessionId());
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
            port.setAttributePropertiesForClassWithId(classId, attributeId, name, displayName, type, description,
                    administrative, visible, readOnly, unique, noCopy, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
        
    public long createClassMetadata(String className, String displayName, String description, String parentClassName, boolean custom, boolean countable, int color, boolean _abstract, boolean inDesign){
        try{
            return port.createClass(className, displayName, description, _abstract, custom, countable, inDesign, parentClassName, null, null, this.session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return -1;
        }
    }
    
    public boolean deleteClassMetadata(long classId){
        try{
            port.deleteClassWithId(classId, this.session.getSessionId());
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
            port.createAttributeForClassWithId(classId, name, displayName, type, description, administrative, visible, readOnly, noCopy, unique, this.session.getSessionId());
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
            port.createAttribute(className, name, displayName, type, description, administrative, visible, readOnly, noCopy, unique, this.session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
        return true;
    }
    
    public boolean deleteAttribute(long classId, String attributeName){
        try{
            port.deleteAttributeForClassWithId(classId, attributeName, this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
    }
    
    public boolean setClassMetadataProperties(long classId, String className, 
                                                 String displayName, String description, 
                                                 byte[] smallIcon, byte[] icon, 
                                                 Boolean _abstract,Boolean inDesign, Boolean countable, Boolean custom){
        try{
            port.setClassProperties(classId, className, displayName, description, smallIcon, icon,
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
    public LocalClassMetadataLight[] getInstanceableListTypes() {
        try{
            List<ClassInfoLight> listTypes;
            listTypes = port.getInstanceableListTypes(this.session.getSessionId());

            LocalClassMetadataLight[] res = new LocalClassMetadataLight[listTypes.size()];
            int i = 0;
            for (ClassInfoLight cil : listTypes){
                res[i] = new LocalClassMetadataLightImpl(cil);
                i++;
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
            port.deleteListTypeItem(className, oid, force, session.getSessionId());
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
            List<UserInfo> users = port.getUsers(this.session.getSessionId());
            LocalUserObject[] localUsers = new LocalUserObject[users.size()];

            int i = 0;
            for (UserInfo user : users){
                localUsers[i] = (LocalUserObject) new LocalUserObjectImpl(user);
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
            List<UserGroupInfo> groups = port.getGroups(this.session.getSessionId());
            LocalUserGroupObject[] localGroups = new LocalUserGroupObject[groups.size()];

            int i = 0;
            for (UserGroupInfo group : groups){
                localGroups[i] = (LocalUserGroupObject) new LocalUserGroupObjectImpl(group);
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
            newUser.setOid(port.createUser(newUser.getUserName(), "kuwaiba", null, null, true, null, null, this.session.getSessionId()));
            return new LocalUserObjectImpl(newUser);
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
            port.setUserProperties(oid, userName, firstName, lastName, password, true, null, myGroups, this.session.getSessionId());
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
            port.setGroupProperties(oid, groupName, description, null, null, this.session.getSessionId());
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
            UserGroupInfo newGroup = new UserGroupInfo();
            newGroup.setName("group"+random.nextInt(10000));
            newGroup.setOid(port.createGroup(newGroup.getName(), null, null, null, this.session.getSessionId()));
            return new LocalUserGroupObjectImpl(newGroup);
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
            port.deleteUsers(objects,session.getSessionId());
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
            port.deleteGroups(objects,session.getSessionId());
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
            ViewInfo view = port.getObjectRelatedView(oid, objectClass, viewId, session.getSessionId());
            return new LocalObjectViewImpl(view.getId(), view.getName(), view.getDescription(), view.getType(), view.getStructure(), view.getBackground());
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
            List<ViewInfoLight> views = port.getObjectRelatedViews(oid, objectClass, -1, 10, session.getSessionId());
            List<LocalObjectViewLight> res = new ArrayList<LocalObjectViewLight>();
            for (ViewInfoLight view : views)
                res.add(new LocalObjectViewLightImpl(view.getId(), view.getName(), view.getDescription(), view.getType()));
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }

    /**
     * Retrieves the list of views not related to a given object like GIS, topological views
     * @param viewType Type of view to be retrieved. The implementor must defined what are the possible admitted values
     * @return a list of object with the minimum information about the view (id, class and name)
     * @throws InvalidArgumentException if the viewType is not a valid value
     */
    public List<LocalObjectViewLight> getGeneralViews(int viewType){
        try{
            List<ViewInfoLight> views = port.getGeneralViews(viewType, -1, session.getSessionId());
            List<LocalObjectViewLight> res = new ArrayList<LocalObjectViewLight>();
            for (ViewInfoLight view : views)
                res.add(new LocalObjectViewLightImpl(view.getId(), view.getName(), view.getDescription(), view.getType()));
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
            ViewInfo view = port.getGeneralView(viewId, session.getSessionId());
            return new LocalObjectViewImpl(view.getId(), view.getName(), view.getDescription(), view.getType(), view.getStructure(), view.getBackground());
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
     * @param viewType view type (See class ViewObject for details about the supported types)
     * @param structure XML document with the view structure (see http://sourceforge.net/apps/mediawiki/kuwaiba/index.php?title=XML_Documents#To_Save_Object_Views for details about the supported format)
     * @param background background image
     * @return the new object id
     */
    public long createObjectRelatedView(long oid, String objectClass, String name, String description, int viewType, byte[] structure, byte[] background){
        try{
            return port.createObjectRelatedView(oid, objectClass, name, description, viewType, structure, background, session.getSessionId());
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return -1;
        }
    }

    /**
     * Creates a view not related to a particular object
     * @param view id
     * @param viewType
     * @param name view name
     * @param description view description
     * @param structure XML document specifying the view structure (nodes, edges, control points)
     * @param background Background image
     * @return the new object id
     */
    public long createGeneralView(int viewType, String name, String description, byte[] structure, byte[] background){
        try{
            return port.createGeneralView(viewType, name, description, structure, background, session.getSessionId());
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
            port.updateObjectRelatedView(oid, objectClass, viewId, name, description, structure, background, session.getSessionId());
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
            port.updateGeneralView(oid, name, description, structure, background, session.getSessionId());
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
             List<Long> oids = new ArrayList<Long>();
             for (long l : ids){ 
                 oids.add(l);
             }
             port.deleteGeneralView(oids, session.getSessionId());
             return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Services methods. Click on the + sign on the left to edit the code.">
    /**
     * Relates a resource to a service
     * @param resourceClassName
     * @param resourceId
     * @param serviceClassName
     * @param serviceId
     * @return
     */
    public boolean relateResourceToService(String resourceClassName, long resourceId, String serviceClassName, long serviceId){
//        try{
//            return port.relateResourceToService(resourceClassName, resourceId,
//                    serviceClassName,serviceId,this.session.getSessionId());
//        }catch(Exception ex){
//            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName()+": "+ ex.getMessage();
//            return false;
//        }
        return false;
    }// </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Pools methods. Click on the + sign on the left to edit the code.">
    /**
     * Creates a pool
     * @param name Pool name
     * @param description Pool description
     * @param className What kind of objects can this pool contain?
     * @return The newly created pool
     */
    public LocalObjectLight createPool(String name, String description, String className){
        try{
            long objectId  = port.createPool(name, description, className,session.getSessionId());
            return new LocalObjectLightImpl(objectId, name, className);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public LocalObjectLight createPoolItem (long poolId, String className){
        try{
            long objectId  = port.createPoolItem(poolId, className, null, null, -1,session.getSessionId());
            return new LocalObjectLightImpl(objectId, null, className);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    
    public boolean deletePool(long id){
        try{
            port.deletePools(Arrays.asList(new Long(id)), this.session.getSessionId());
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
            List<RemoteObjectLight> items = port.getPoolItems(oid, -1, this.session.getSessionId());
            List<LocalObjectLight> res = new ArrayList<LocalObjectLight>();

            for (RemoteObjectLight rol : items) {
                res.add(new LocalObjectLightImpl(rol));
            }

            return res;
        } catch (Exception ex) {
            this.error = ex.getMessage();
            return null;
        }
    }
    
    /**
     * Returns the list of pools available
     * @return The list of pools
     */
    public List<LocalObjectLight> getPools() {
        try{
            List <RemoteObjectLight> children = port.getPools(-1,this.session.getSessionId());
            List <LocalObjectLight> res = new ArrayList<LocalObjectLight>();

            for (RemoteObjectLight rol : children){
                res.add(new LocalObjectLightImpl(rol));
            }
            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }// </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Sync methods. Click on the + sign on the left to edit the code.">
    /**
     * Load data from a file 
     * @param choosenFile csv file with the data
     * @return percent  of progress
     */
    public String loadDataFromFile(byte[] choosenFile){
        try{
            return port.loadDataFromFile(choosenFile, this.session.getSessionId());
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return "";
        }
    }
     public byte[] downloadErrors(String fileName){
        try{
            return port.downloadErrors(fileName, this.session.getSessionId());
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }    
    
    public byte[] downloadLog(String fileName){
        try{
            return port.downloadLog(fileName, this.session.getSessionId());
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }
    // </editor-fold>   
}

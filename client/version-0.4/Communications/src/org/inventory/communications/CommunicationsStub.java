/*
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
import org.inventory.communications.core.queries.LocalResultRecordImpl;
import org.inventory.communications.core.queries.LocalTransientQueryImpl;
import org.inventory.communications.core.queries.LocalQueryImpl;
import org.inventory.communications.core.queries.LocalQueryLightImpl;
import org.inventory.communications.core.LocalUserGroupObjectImpl;
import org.inventory.communications.core.LocalUserObjectImpl;
import org.inventory.communications.core.views.LocalObjectViewImpl;
import org.inventory.core.services.factories.ObjectFactory;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.LocalObject;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.LocalObjectListItem;
import org.inventory.core.services.api.queries.LocalQuery;
import org.inventory.core.services.api.queries.LocalQueryLight;
import org.inventory.core.services.api.queries.LocalResultRecord;
import org.inventory.core.services.api.queries.LocalTransientQuery;
import org.inventory.core.services.api.session.LocalUserGroupObject;
import org.inventory.core.services.api.session.LocalUserObject;
import org.inventory.core.services.api.visual.LocalObjectView;
import org.inventory.objectcache.Cache;
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
    private String error=java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_ERROR");
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
    }

    /**
     * Retrieves a given object's children
     * @return an array of local objects representing the object's children
     */
    public List<LocalObjectLight> getObjectChildren(Long oid, Long objectClassId){
        try{
            List <RemoteObjectLight> children = port.getObjectChildren(oid, objectClassId, 0,this.session.getSessionId());
            List <LocalObjectLight> res = new ArrayList<LocalObjectLight>();

            for (RemoteObjectLight rol : children)
                res.add(new LocalObjectLightImpl(rol));

            return res;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }

    public List<LocalObject> getChildrenOfClass(Long oid, String parentClassName, String childrenClassName){
        try{
            List <RemoteObject> children = port.getChildrenOfClass(oid, parentClassName, childrenClassName, 0, this.session.getSessionId());
            List <LocalObject> res = new ArrayList<LocalObject>();

            for (RemoteObject rol : children)
                res.add(new LocalObjectImpl(rol, getMetaForClass(rol.getClassName(), false)));

            return res;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * Updates the attributes of a given object
     *
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
                    for (Long itemId : (List<Long>)obj.getAttribute(key))
                        value.getItem().add(itemId.toString());
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
    public boolean setObjectLock(Long oid, String objectClass,Boolean value){
        return true;
    }

    /**
     * Retrieves the whole object info
     * @param objectClass object class
     * @param oid object id
     * @return The local representation of the object
     */
    public LocalObject getObjectInfo(String objectClass, Long oid){
        try{
            LocalClassMetadata lcmd = getMetaForClass(objectClass, false);
            RemoteObject myObject = port.getObjectInfo(objectClass, oid,this.session.getSessionId());
            return new LocalObjectImpl(myObject,lcmd);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    public List<Long> getSpecialAttribute(String objectClass, Long objectId, String attributeName){
        try{
            //This is only temporal, since the result may not be a long in  the future, but it is right now
            List<Long> res = new ArrayList<Long>();
            for (String value : port.getSpecialAttribute(objectClass, objectId,attributeName, session.getSessionId()))
                res.add(Long.valueOf(value));

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
    public LocalObjectLight getObjectInfoLight(String objectClass, Long oid){
        try{
            RemoteObjectLight myLocalObject = port.getObjectInfoLight(objectClass, oid,this.session.getSessionId());
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
            if (!ignoreCache)
                    resAsLocal = cache.getPossibleChildrenCached(className);

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

            for (ClassInfoLight cil : resAsRemote)
                resAsLocal.add(new LocalClassMetadataLightImpl(cil));

            return resAsLocal;
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    public LocalObjectLight createObject(String objectClass, String parentClass, Long parentOid, Long template){
        try{
            Long objectId  = port.createObject(objectClass,parentClass, parentOid, new ArrayList<String>(),new ArrayList<StringArray>(),template,this.session.getSessionId());
            return new LocalObjectLightImpl(objectId, null, objectClass);
        }catch(Exception ex){
            this.error = ex.getMessage();
            return null;
        }
    }

    /**
     * The result is cached to be used when needed somewhere else, but the whole
     * metadata information is always retrieved directly from the ws
     * @param includeListTypes boolean to indicate if the list should include the list types,
     * such as CustomerType
     * @return an array with all class metadata (the light version)
     */
    public LocalClassMetadataLight[] getAllLightMeta(boolean includeListTypes) {
        try{
            List<ClassInfoLight> metas = port.getLightMetadata(includeListTypes, this.session.getSessionId());

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
            metas= port.getMetadata(includeListTypes, this.session.getSessionId());
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
     * @param className the object class
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

            ClassInfo cm = port.getMetadataForClass(className,this.session.getSessionId());

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

            ClassInfo cm = port.getMetadataForClass(className,this.session.getSessionId());

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

    public LocalObjectLight createListTypeItem(String className){
        try{
            Long myObjectId = port.createListTypeItem(className, "","",this.session.getSessionId());
            return new LocalObjectLightImpl(myObjectId,null,className);
        }catch(Exception ex){
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

            if (!ignoreCache)
                res = cache.getListCached(className);

            if (res == null){
               res = new ArrayList<LocalObjectListItem>();
               res.add(ObjectFactory.createNullItem());

                List<RemoteObjectLight> remoteList = port.getListTypeItems(className,this.session.getSessionId());

                for(RemoteObjectLight entry : remoteList)
                    res.add(new LocalObjectListItemImpl(entry.getOid(),entry.getClassName(),entry.getName()));

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
    }

    public boolean addPossibleChildren(Long parentClassId, List<Long> possibleChildren){
        try{
            port.addPossibleChildren(parentClassId, possibleChildren,this.session.getSessionId());
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
    public boolean removePossibleChildren(Long parentClassId, List<Long> childrenToBeDeleted){
        try{
            port.removePossibleChildren(parentClassId, childrenToBeDeleted,this.session.getSessionId());
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
    public boolean deleteObject(String className, Long oid){
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

    public boolean moveObjects(String targetClass, Long targetOid, LocalObjectLight[] _objects) {

        try{
            List<Long> objectOids = new ArrayList<Long>();
            List<String> objectClasses = new ArrayList<String>();

            for (LocalObjectLight lol : _objects){
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

    public LocalObjectLight[] copyObjects(String targetClass, Long targetOid, LocalObjectLight[] objects){
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
     * QUERIES
     */

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
    public Long createQuery(String queryName, byte[] queryStructure, String description, boolean isPublic){
        try{
            return port.createQuery(queryName, isPublic ? null : session.getUserId(), queryStructure, description, session.getSessionId());
            
        }catch(Exception ex){
            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName()+": "+ ex.getMessage();
            return null;
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
                    query.isPublic() ? null : session.getUserId(),
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
    public boolean deleteQuery(Long queryId){
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
    public LocalQuery getQuery(Long queryId){
        try{
            return new LocalQueryImpl(port.getQuery(queryId, session.getSessionId()));
        }catch(Exception ex){
            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName()+": "+ ex.getMessage();
            return null;
        }
    }

    /**
     * MISC
     */

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
            if (refreshMeta)
                for (LocalClassMetadata lcm : cache.getMetadataIndex()){
                    LocalClassMetadata myLocal =
                            new LocalClassMetadataImpl(port.getMetadataForClass(lcm.getClassName(),this.session.getSessionId()));
                    if(myLocal!=null)
                    cache.addMeta(new LocalClassMetadata[]{myLocal});
                }

            if (refreshLightMeta){
                List<ClassInfoLight> myLocalLight  = port.getLightMetadata(true, this.session.getSessionId());
                if (myLocalLight != null)
                    getAllLightMeta(true);
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
    
    public boolean setAttributePropertyValue(Long classId, String attributeName,
        String propertyName, String propertyType) {
        try{
            port.setAttributePropertyValue(classId, attributeName, propertyName, propertyType,this.session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
        return true;
    }

    public boolean setClassPlainAttribute(Long classId, String attributeName, String attributeValue){
        try{
            port.setClassPlainAttribute(classId, attributeName, attributeValue,this.session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
            return false;
        }
        return true;
    }

    public boolean setClassIcon(Long classId, String attributeName, byte[] attributeValue){
        try{
            port.setClassIcon(classId, attributeName, attributeValue,this.session.getSessionId());
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
    public boolean deleteListTypeItem(String className, Long oid, boolean force){
        try{
            port.deleteListTypeItem(className, oid, force, session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return false;
        }
    }

    /**
     * User management
     */

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
            newUser.setOid(port.createUser(newUser.getUserName(), "kuwaiba", null, null, null, null, null, this.session.getSessionId()));
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
    public boolean setUserProperties(Long oid, String userName, String password, String firstName,
            String lastName, List<Long> groups) {
        try{
            port.setUserProperties(oid, userName, firstName, lastName, password, null, null, groups, this.session.getSessionId());
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
    public boolean setGroupProperties(Long oid, String groupName, String description) {
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
    public boolean deleteUsers(Long[] oids){
        try{
            port.deleteUsers(Arrays.asList(oids), session.getSessionId());
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
    public boolean deleteGroups(Long[] oids){
        try{
            port.deleteGroups(Arrays.asList(oids),session.getSessionId());
        }catch(Exception ex){
            this.error = ex.getMessage();
        }
        return true;
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
    public LocalObjectLight createPhysicalConnection(String endpointAClass, Long endpointAId,
            String endpointBClass, Long endpointBId, String parentClass, Long parentId, String name, String type, String connectionClass) {
        try{
            List<StringArray> values = new ArrayList<StringArray>();
            StringArray valueName = new StringArray();
            valueName.getItem().add(name);

            StringArray valueType = new StringArray();
            valueType.getItem().add(type);

            values.add(valueName);
            values.add(valueType);

            Long myObjectId = port.createPhysicalConnection(endpointAClass, endpointAId,
                    endpointBClass, endpointBId, parentClass, parentId, Arrays.asList(new String[]{"name","type"}), values, connectionClass, this.session.getSessionId());
            return new LocalObjectLightImpl(myObjectId, "", connectionClass);
        }catch(Exception ex){
            this.error =  ex.getMessage();
            return null;
        }
    }

    /**
     * Views
     */

    /**
     * Gets the default view for an object
     * @param oid object oid
     * @param string object class name, including the package
     * @return a view or null, if not such default view is being set
     */
    public LocalObjectView getObjectDefaultView(Long oid, String objectClass) {
        try{
            ViewInfo myView = port.getView(oid, objectClass,LocalObjectView.TYPE_DEFAULT, this.session.getSessionId());
            if (myView == null) //There's no default view yet
                return null;
            return new LocalObjectViewImpl(myView.getStructure(),myView.getBackground(),myView.getType());
        }catch(Exception ex){
            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName()+": "+ ex.getMessage();
            return null;
        }
    }

    public boolean saveView(Long oid, String objectClass, String viewClass, byte[] background, byte[] viewStructure){
        try{
            port.saveView(oid, objectClass, LocalObjectView.TYPE_DEFAULT,viewStructure, background,this.session.getSessionId());
            return true;
        }catch(Exception ex){
            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName()+": "+ ex.getMessage();
            return false;
        }
    }

    /**
     * Relates a resource to a service
     * @param resourceClassName
     * @param resourceId
     * @param serviceClassName
     * @param serviceId
     * @return
     */
    public boolean relateResourceToService(String resourceClassName, Long resourceId, String serviceClassName, Long serviceId){
//        try{
//            return port.relateResourceToService(resourceClassName, resourceId,
//                    serviceClassName,serviceId,this.session.getSessionId());
//        }catch(Exception ex){
//            this.error = (ex instanceof SOAPFaultException)? ex.getMessage() : ex.getClass().getSimpleName()+": "+ ex.getMessage();
//            return false;
//        }
        return false;
    }
}

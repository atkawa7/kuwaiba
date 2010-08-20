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
package org.inventory.communications;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import org.inventory.communications.core.LocalClassMetadataImpl;
import org.inventory.communications.core.LocalClassMetadataLightImpl;
import org.inventory.communications.core.LocalObjectImpl;
import org.inventory.communications.core.LocalObjectLightImpl;
import org.inventory.communications.core.LocalObjectListItemImpl;
import org.inventory.communications.core.LocalUserGroupObjectImpl;
import org.inventory.communications.core.LocalUserObjectImpl;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.LocalObjectListItem;
import org.inventory.core.services.interfaces.LocalUserGroupObject;
import org.inventory.core.services.interfaces.LocalUserObject;
import org.inventory.objectcache.Cache;
import org.inventory.webservice.ClassInfo;
import org.inventory.webservice.ClassInfoLight;
import org.inventory.webservice.KuwaibaWebservice;
import org.inventory.webservice.KuwaibaWebserviceService;
import org.inventory.webservice.ObjectList;
import org.inventory.webservice.ObjectList.List.Entry;
import org.inventory.webservice.ObjectUpdate;
import org.inventory.webservice.RemoteObjectLight;
import org.inventory.webservice.UserGroupInfo;
import org.inventory.webservice.UserInfo;

/**
 * Singleton class that provides communication and caching services to the rest of the modules
 * TODO: Make it a thread to support simultaneous operations
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class CommunicationsStub {
    private static CommunicationsStub instance=null;
    private KuwaibaWebserviceService service;
    private KuwaibaWebservice port;
    private static URL serverURL = null;
    private String error=java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_ERROR");
    private Cache cache;

    
    private CommunicationsStub(){
        if (serverURL == null){
            try{
                //Default values
                serverURL = new URL("http", "localhost", 8080,"/KuwaibaServer-war/KuwaibaWebserviceService?wsdl"); //NOI18n
            }catch (MalformedURLException mue){
                Logger.getAnonymousLogger("Malformed URL: "+mue.getMessage());
            }
        }
        this.service = new KuwaibaWebserviceService(serverURL);
        this.port = service.getKuwaibaWebservicePort();
        cache = Cache.getInstace();
    }

    //Implements the singleton pattern
    public static CommunicationsStub getInstance(){
            if(instance==null) instance = new CommunicationsStub();
            return instance;
    }

    /**
     * Resets the singleton instance to null so it has to be created again
     * TODO: Is there a more graceful way to do it?
     */
    public static void resetInstance() {
        serverURL = null;
        instance = null;
    }

    /**
     * Sets the webservice's URL
     * @param _URL A valid URL
     */
    public static void setServerURL(URL _URL){
        serverURL = _URL;
    }
    
    /**
     * This method closes the current session
     * @return Success or failure
     */
    public boolean closeSession(){
        try{
            return port.closeSession();
        }catch(Exception e){
            this.error = this.error = java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_CONNECTION");
            return false;
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
            if (!port.createSession(user, password)){
                this.error = "Login or password incorrect";
                return false;
            }else return true;
        }catch(Exception e){
            this.error = this.error = java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_CONNECTION");
            return false;
        }
    }

    /**
     * Retrieves the root node's children
     * @return an array of local objects representing the root node's children
     */
    public LocalObjectLight[] getRootNodeChildren(){
        try{
            
            if (cache.getRootId()==null)
                cache.setRootId(port.getDummyRootId());
            if (cache.getMetaForClass("DummyRoot")==null){
                cache.addMeta(
                        new LocalClassMetadataImpl[]{new LocalClassMetadataImpl(
                                                            port.getMetadataForClass("DummyRoot"))});

            }

            List<RemoteObjectLight> result = port.getObjectChildren(cache.getRootId(),
                                                                    cache.getMetaForClass("DummyRoot").getId());
            if(result ==null){
                error = port.getLastErr();
                return null;
            }
        
            LocalObjectLightImpl[] children = new LocalObjectLightImpl[result.size()];
            int i = 0;
            for (RemoteObjectLight obj : result){
                children[i] = new LocalObjectLightImpl(obj);
                i++;
            }

            return children;

        }catch(Exception connectException){ //TODO Find out why the ConnectException is not the one thrown here
            this.error = java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_CONNECTION");
            return null;
        }
    }

    /**
     * Retrieves a given object's children
     * @return an array of local objects representing the object's children
     */
    public List<LocalObjectLight> getObjectChildren(Long oid, Long objectClassId){
        List <RemoteObjectLight> children = port.getObjectChildren(oid, objectClassId);
        List <LocalObjectLight> res = new ArrayList<LocalObjectLight>();
        
        for (RemoteObjectLight rol : children)
            res.add(new LocalObjectLightImpl(rol));
        
        return res;
    }
    
    /**
     * Updates the attributes of a given object
     *
     * @param obj is the object to be updated. Note that this object doesn't have
     *            every field within the "original". it only has field(s) to be updated
     * @return success or failure
     */
    public boolean saveObject(LocalObject obj){
        ObjectUpdate update = new ObjectUpdate();
        List<String> atts = new ArrayList<String>();
        List<String> vals = new ArrayList<String>();

        update.setClassname(obj.getClassName());
        update.setOid(obj.getOid());

        for (String key : obj.getAttributes().keySet()){
            atts.add(key);
            vals.add(obj.getAttribute(key).toString());
        }

        update.setUpdatedAttributes(atts);
        update.setNewValues(vals);

        boolean res = port.updateObject(update);
        if (!res) this.error = port.getLastErr();
        return res;
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
     * @param lcmd metadata associated. Useful to map the response. Mmm, this should be corrected
     * @return The local representation of the object
     */
    public LocalObject getObjectInfo(String objectClass, Long oid, LocalClassMetadata lcmd){

        LocalObjectImpl res = new LocalObjectImpl(port.getObjectInfo(objectClass, oid),lcmd);
        if (res == null){
            this.error = java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_OBJECT");
        }
        return res;
    }

    /**
     * Returns the last error related to communications
     * @return The error string
     */
    public String getError() {
        if (error == null)
            error = "No network connection";
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

        List<LocalClassMetadataLight> resAsLocal = null;
        if (!ignoreCache)
                resAsLocal = cache.getPossibleChildrenCached(className);

        if (resAsLocal == null){
            resAsLocal = new ArrayList<LocalClassMetadataLight>();
            List<ClassInfoLight> resAsRemote = port.getPossibleChildren(className);
            if (port == null){
                this.error = port.getLastErr();
                return null;
            }
            for (ClassInfoLight cil : resAsRemote){
                resAsLocal.add(new LocalClassMetadataLightImpl(cil));
            }
            cache.addPossibleChildrenCached(className, resAsLocal);
        }
        return resAsLocal;
    }

    /**
     * Same as above method, but this one doesn't go deeper into the container hierarchy
     * The result is not cached
     * @param className
     * @return allPosible children
     */
    public List<LocalClassMetadataLight> getPossibleChildrenNoRecursive(String className) {
        List<ClassInfoLight> resAsRemote = port.getPossibleChildrenNoRecursive(className);
        List<LocalClassMetadataLight> resAsLocal = new ArrayList<LocalClassMetadataLight>();
        if (port == null){
            this.error = port.getLastErr();
            return null;
        }
        for (ClassInfoLight cil : resAsRemote)
            resAsLocal.add(new LocalClassMetadataLightImpl(cil));
        
        return resAsLocal;
    }

    public LocalObjectLight createObject(String objectClass, Long parentOid, String template){
        RemoteObjectLight myObject = port.createObject(objectClass, template,parentOid);
        if (myObject == null){
            this.error = port.getLastErr();
            return null;
        }
        return new LocalObjectLightImpl(myObject);
    }

    /**
     * Retrieves complete information about classes. It always take them from the
     * server rather than from the cache, because this methods is suggested to be used
     * for administrative tasks when it's necessary to have the metadata up to date.
     * Anyway, the retrieved information is cached in order to be used when mapping the object's attributes
     * in the property sheets
     * @return an array with all the class metadata information
     */
    public LocalClassMetadata[] getAllMeta() {

        List<ClassInfo> metas;
        try{
            metas= port.getMetadata();
        }catch(Exception connectException){
            this.error = java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_CONNECTION");
            return null;
        }
        LocalClassMetadata[] lm = new LocalClassMetadata[metas.size()];
        int i=0;
        for (ClassInfo cm : metas){
            lm[i] = new LocalClassMetadataImpl(cm);
            i++;
        }
        cache.addMeta(lm); //wipe out the cache and write it again
        return lm;
    }

    /**
     * Retrieves the metadata for a given class
     * @param className the object class
     * @return the metadata information
     */
    public LocalClassMetadata getMetaForClass(String className, boolean ignoreCache){
        LocalClassMetadata res;
        if (!ignoreCache){
            res = cache.getMetaForClass(className);
            if (res != null)
                return res;
        }

        ClassInfo cm = port.getMetadataForClass(className);
        if (cm ==null){
            this.error = port.getLastErr();
            return null;
        }

        res = new LocalClassMetadataImpl(cm);
        cache.addMeta(new LocalClassMetadata[]{res});
        return res;
    }

    /**
     * Retrieves the metadata for a given class
     * @param className the object class
     * @return the metadata information
     */
    public LocalClassMetadataLight getLightMetaForClass(String className, boolean ignoreCache){
        LocalClassMetadataLight res;
        if (!ignoreCache){
            res = cache.getLightMetaForClass(className);
            if (res != null)
                return res;
        }

        ClassInfo cm = port.getMetadataForClass(className);
        if (cm ==null){
            this.error = port.getLastErr();
            return null;
        }

        res = new LocalClassMetadataLightImpl(cm);
        cache.addLightMeta(new LocalClassMetadataLight[]{res});
        return res;
    }

    /**
     * Retrieves a List type attribute.
     * @param className attribute class (usually descendant of GenericListType)
     * @return 
     */
    public LocalObjectListItem[] getList(String className, boolean ignoreCache){
        LocalObjectListItem[] res;

        if (!ignoreCache){
            res = cache.getListCached(className);
            if (res != null)
                return res;
        }

        ObjectList remoteList = port.getMultipleChoice(className);
        
        if (remoteList == null){
            this.error = port.getLastErr();
            return null;
        }
            
        List<LocalObjectListItem> loli = new ArrayList<LocalObjectListItem>();
        //The +1 represents the empty room left for the "null" value
        res = new LocalObjectListItemImpl[remoteList.getList().getEntry().size() + 1];
        res[0] = LocalObjectListItemImpl.getNullValue();
        loli.add(res[0]);
        int i = 1;
        for(Entry entry : remoteList.getList().getEntry()){
            res[i] = new LocalObjectListItemImpl(entry.getKey(),className,entry.getValue(),entry.getValue());
            loli.add(res[i]);
            i++;
        }

        cache.addListCached(className, loli);
        return res;
    }

    public boolean addPossibleChildren(Long parentClassId, List<Long> possibleChildren){
        boolean res = port.addPossibleChildren(parentClassId, possibleChildren);
        if (!res)
            this.error = port.getLastErr();
        return res;
    }

    /**
     * Removes possible children from the given class' container hierarchy
     * @param Id for the parent class
     * @param childrenToBeDeleted List if ids of the classes to be removed as possible children
     * @return Sucess or failure
     */
    public boolean removePossibleChildren(Long parentClassId, List<Long> childrenToBeDeleted){
        boolean res = port.removePossibleChildren(parentClassId, childrenToBeDeleted);
        if (!res)
            this.error = port.getLastErr();
        return res;
    }

    /*
     * Deletes the given object
     * @param className Object class (including its package)
     * @param oid object id
     * @return Success or failure
     */
    public boolean removeObject(String className, Long oid){
        boolean res = port.removeObject(className,oid);
        if (!res)
            this.error = port.getLastErr();
        return res;
    }

    /**
     * The result is cached to be used when needed somewhere else, but the whole
     * metadata information is always retrieved directly from the ws
     * @return an array with all class metadata (the light version)
     */
    public LocalClassMetadataLight[] getAllLightMeta() {
        List<ClassInfoLight> metas;
        try{
            metas= port.getLightMetadata();
        }catch(Exception connectException){
            this.error = java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_CONNECTION");
            return null;
        }
        LocalClassMetadataLight[] lm = new LocalClassMetadataLight[metas.size()];
        int i=0;
        for (ClassInfoLight cm : metas){
            lm[i] = (LocalClassMetadataLight)new LocalClassMetadataLightImpl(cm);
            i++;
        }

        cache.addLightMeta(lm);
        return lm;
    }

    public Long getRootId(){
        if (cache.getRootId() == null)
            cache.setRootId(port.getDummyRootId());
        return cache.getRootId();
    }

    public String getRootClass(){
        LocalClassMetadata lcm = getMetaForClass("DummyRoot",false);
        return lcm.getClassName();
    }

    /*
     * The only reason for this method is to avoid calling the getMetaForClass to know details about DummyRoot.
     * This may be addressed once the local cache is fully functional. But by now, we'll stick to this.
     */
    public List<LocalClassMetadataLight> getRootPossibleChildren() {
        List<ClassInfoLight> list = port.getRootPossibleChildren();
        List <LocalClassMetadataLight> res = new ArrayList<LocalClassMetadataLight>();
        if(list==null){
            this.error = port.getLastErr();
            return null;
        }
        else
            for (ClassInfoLight cil : list)
                res.add(new LocalClassMetadataLightImpl(cil));

        return res;
    }

    public boolean moveObjects(Long targetOid, LocalObjectLight[] _objects) {

        List<Long> objectOids = new ArrayList<Long>();
        List<String> objectClasses = new ArrayList<String>();

        for (LocalObjectLight lol : _objects){
            objectOids.add(lol.getOid());
            objectClasses.add(lol.getClassName());
        }

        if (port.moveObjects(targetOid, objectClasses, objectOids))
            return true;
        else{
            this.error = port.getLastErr();
            return false;
        }
    }

    public LocalObjectLight[] copyObjects(Long targetOid, LocalObjectLight[] _objects){
        List<Long> objectOids = new ArrayList<Long>();
        List<String> objectClasses = new ArrayList<String>();

        for (LocalObjectLight lol : _objects){
            objectOids.add(lol.getOid());
            objectClasses.add(lol.getClassName());
        }

        List<RemoteObjectLight> objs = port.copyObjects(targetOid, objectClasses, objectOids);

        if (objs != null){
            LocalObjectLight[] res = new LocalObjectLight[objs.size()];
            int i = 0;
            for (RemoteObjectLight rol : objs){
                res[i] = new LocalObjectLightImpl(rol);
                i++;
            }
            return res;
        }

        else{
            this.error = port.getLastErr();
            return null;
        }
    }

    public LocalObjectLight[] searchForObjects(String className, List<String> atts,
            List<String> types, List<String> values) {
        List<RemoteObjectLight> found = port.searchForObjects(className,atts, types, values);
        if (found == null)
            this.error = port.getLastErr();
        LocalObjectLight[] res = new LocalObjectLight[found.size()];

        int i = 0;
        for (RemoteObjectLight rol : found){
            res[i] = new LocalObjectLightImpl(rol);
            i++;
        }

        return res;

    }

    /**
     * Reset the cache to the default, this is, only:
     * -Root id and class
     */
    public void resetCache(){

        //Set the new values
        cache.setRootId(port.getDummyRootId());

        //Wipe out the dictionaries
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
        if (refreshMeta)
            for (LocalClassMetadata lcm : cache.getMetadataIndex()){
                LocalClassMetadata myLocal =
                        new LocalClassMetadataImpl(port.getMetadataForClass(lcm.getClassName()));
                if(myLocal!=null)
                cache.addMeta(new LocalClassMetadata[]{myLocal});
            }

        if (refreshLightMeta){
            List<ClassInfoLight> myLocalLight  = port.getLightMetadata();
            if (myLocalLight != null)
                getAllLightMeta();
        }

        if (refreshList){
            Dictionary<String, List<LocalObjectListItem>> myLocalList
                    = cache.getAllList();
            Enumeration em = myLocalList.keys();
            while(em.hasMoreElements()){
                String item = (String) em.nextElement();
                myLocalList.remove(item);
                getList(item,true);
            }
        }
        if (refreshPossibleChildren){
            Dictionary<String, List<LocalClassMetadataLight>> myLocalPossibleChildren
                    = cache.getAllPossibleChildren();
            Enumeration em = myLocalPossibleChildren.keys();
            while(em.hasMoreElements()){
                String item = (String) em.nextElement();
                myLocalPossibleChildren.remove(item);
                getPossibleChildren(item,true);
            }
        }
    }
    
    public boolean setAttributePropertyValue(Long classId, String attributeName,
            String propertyName, String propertyType) {
        return port.setAttributePropertyValue(classId, attributeName, propertyName, propertyType);
    }

    public boolean setClassPlainAttribute(Long classId, String attributeName, String attributeValue){
        boolean res = port.setClassPlainAttribute(classId, attributeName, attributeValue);
        if(!res)
            error = port.getLastErr();
        return res;
    }

    public boolean setClassIcon(Long classId, String attributeName, byte[] attributeValue){
        boolean res = port.setClassIcon(classId, attributeName, attributeValue);
        if(!res)
            error = port.getLastErr();
        return res;
    }

    /**
     * Retrives the list types
     * @return an array with all possible instanceable list types
     */
    public LocalClassMetadataLight[] getInstanceableListTypes() {
        List<ClassInfoLight> listTypes;
        try{
            listTypes = port.getInstanceableListTypes();
            if (listTypes==null){
              error = port.getLastErr();
                return null;
            }
        }catch(Exception cte){
            listTypes = null;
            this.error = java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_CONNECTION");
            return null;
        }
        
        LocalClassMetadataLight[] res = new LocalClassMetadataLight[listTypes.size()];
        int i = 0;
        for (ClassInfoLight cil : listTypes){
            res[i] = new LocalClassMetadataLightImpl(cil);
            i++;
        }
        return res;
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
            List<UserInfo> users = port.getUsers();
            if (users == null){
                this.error = port.getLastErr();
                return null;
            }
            LocalUserObject[] localUsers = new LocalUserObject[users.size()];

            int i = 0;
            for (UserInfo user : users){
                localUsers[i] = (LocalUserObject) new LocalUserObjectImpl(user);
                i++;
            }
            return localUsers;
        }catch (Exception e){
            this.error = java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_CONNECTION");
            return null;
        }
    }
    /**
     * Retrieves the group list
     * @return An array of LocalUserObject
     */
    public LocalUserGroupObject[] getGroups() {
        try{
            List<UserGroupInfo> groups = port.getGroups();
            if (groups == null){
                this.error = port.getLastErr();
                return null;
            }
            LocalUserGroupObject[] localGroups = new LocalUserGroupObject[groups.size()];

            int i = 0;
            for (UserGroupInfo group : groups){
                localGroups[i] = (LocalUserGroupObject) new LocalUserGroupObjectImpl(group);
                i++;
            }
            return localGroups;
        }catch (Exception e){
            //this.error = java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_CONNECTION");
            this.error = e.getMessage();
            return null;
        }
    }

    /**
     * Creates a new user
     * @return The newly created user
     */
    public LocalUserObject addUser(){
        UserInfo newUser = port.createUser();
        if (newUser == null){
            this.error = port.getLastErr();
            return null;
        }
        return new LocalUserObjectImpl(newUser);
    }

    /**
     * Creates a new group
     * @return The newly created group
     */
    public LocalUserGroupObject addGroup(){
        UserGroupInfo newGroup = port.createGroup();
        if (newGroup == null){
            this.error = port.getLastErr();
            return null;
        }
        return new LocalUserGroupObjectImpl(newGroup);
    }

    /**
     * Removes a list of users
     * @param oids oids for the users to be deleted
     * @return success or failure
     */
    public boolean deleteUsers(Long[] oids){
        Boolean res = port.deleteUsers(Arrays.asList(oids));
        if(!res)
            this.error = port.getLastErr();
        return res;
    }

    /**
     * Removes a list of groups
     * @param oids oids for the users to be deleted
     * @return success or failure
     */
    public boolean deleteGroups(Long[] oids){
        Boolean res = port.deleteGroups(Arrays.asList(oids));
        if(!res)
            this.error = port.getLastErr();
        return res;
    }

    /**
     * Assigns groups to a user
     * @param groupsOids An array with The groups oids
     * @param userOid The user's oid
     * @return Success or failure
     */
    public boolean addGroupsToUser(List<Long> groupsOids, Long userOid){
        boolean res = port.addGroupsToUser(groupsOids, userOid);
        if (!res)
            this.error = port.getLastErr();
        return res;
    }

    public boolean removeGroupsFromUser(List<Long> groupsOids, Long userOid){
        boolean res = port.removeGroupsFromUser(groupsOids, userOid);
        if (!res)
            this.error = port.getLastErr();
        return res;
    }
}
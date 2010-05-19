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
package org.inventory.communications;

import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.core.LocalClassMetadataImpl;
import org.inventory.communications.core.LocalClassMetadataLightImpl;
import org.inventory.communications.core.LocalObjectImpl;
import org.inventory.communications.core.LocalObjectLightImpl;
import org.inventory.communications.core.LocalObjectListItemImpl;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.LocalObjectListItem;
import org.inventory.webservice.ClassInfo;
import org.inventory.webservice.ClassInfoLight;
import org.inventory.webservice.KuwaibaWebservice;
import org.inventory.webservice.KuwaibaWebserviceService;
import org.inventory.webservice.ObjectList;
import org.inventory.webservice.ObjectList.List.Entry;
import org.inventory.webservice.ObjectUpdate;
import org.inventory.webservice.RemoteObjectLight;

/**
 * Singleton class that provides communication and caching services to the rest of the modules
 * TODO: Make it a thread to support simlutaneous operations
 * TODO: Use the cachin mechanism within this class, in order to avoid the other classes
 * to call it by themselves
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class CommunicationsStub {
    private static CommunicationsStub instance=null;
    private KuwaibaWebserviceService service;
    private KuwaibaWebservice port;
    private String error=java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_ERROR");
    private LocalObjectLightImpl context;

    
    //Implements the singleton pattern
    private CommunicationsStub(){
        this.service = new KuwaibaWebserviceService();
        this.port = service.getKuwaibaWebservicePort();
    }

    public static CommunicationsStub getInstance(){
            if(instance==null) instance = new CommunicationsStub();
            return instance;
    }

    public LocalObjectLight[] getRootNodeChildren(){
        try{

            List<RemoteObjectLight> result = port.getObjectChildren(port.getDummyRootId(),
                                                                    port.getDummyRootClass());
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

    public List<LocalObjectLight> getObjectChildren(Long oid, String objectClass){
        List <RemoteObjectLight> children = port.getObjectChildren(oid, objectClass);
        List <LocalObjectLight> res = new ArrayList<LocalObjectLight>();
        
        for (RemoteObjectLight rol : children)
            res.add(new LocalObjectLightImpl(rol));
        
        return res;
    }
    /*
     * @param obj is the object to be updated. Note that this object doesn't have
     *            every field within the "original". it only has field(s) to be updated
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

    /*
     * This is a wrapper method with the same name as the one in the webservice used to lock
     * an object as read only because an operation is being performed on it
     * @param oid the object oid
     * @param objectClass the object class
     * @param value Lock value. By now is a boolean, but I expect in the future a three level lock can be implemented (r,w,nothing)
     */
    public boolean setObjectLock(Long oid, String objectClass,Boolean value){
        return true;
    }


    /*
     * Retrieves the whole object info
     * @param objectClass object class
     * @param oid object id
     * @param lcmd metadata associated. Useful to map the response. Mmm, this should be corrected
     */
    public LocalObject getObjectInfo(String objectClass, Long oid, LocalClassMetadata lcmd){

        LocalObjectImpl res = new LocalObjectImpl(port.getObjectInfo(objectClass, oid),lcmd);
        if (res == null){
            this.error = java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("LBL_NO_OBJECT");
        }
        return res;
    }

    public String getError() {
        return error;
    }

    public List<LocalClassMetadataLight> getPossibleChildren(String className) {
        List<ClassInfoLight> resAsRemote = port.getPossibleChildren(className);
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
        return new LocalObjectLightImpl(port.createObject(objectClass, template,parentOid));
    }

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
        return lm;
    }

    /*
     * Retrieves the metadata for a given class
     * @param className the object class
     */
    public LocalClassMetadata getMetaForClass(String className){
        ClassInfo cm = port.getMetadataForClass(className);
        if (cm ==null){
            this.error = port.getLastErr();
            return null;
        }
        return new LocalClassMetadataImpl(cm);
    }

    /*
     * Retrieves a List type attribute.
     * @param className attribute class (usually sibling of GenericListType)
     */
    public LocalObjectListItem[] getList(String className){

        ObjectList list = port.getMultipleChoice(className);
        LocalObjectListItemImpl[] res=null;
        if (list == null)
            return res;

        //The +1 represents the empty room left for the "null" value
        res = new LocalObjectListItemImpl[list.getList().getEntry().size() + 1];
        res[0] = new LocalObjectListItemImpl(LocalObjectListItem.NULL_ID,"",java.util.ResourceBundle.getBundle("org/inventory/communications/Bundle").getString("NONE"));
        int i = 1;
        for(Entry entry : list.getList().getEntry()){
            res[i] = new LocalObjectListItemImpl(entry.getKey(),entry.getValue(),entry.getValue());
            i++;
        }

        return res;
    }

    public boolean addPossibleChildren(Long parentClassId, List<Long> possibleChildren){
        boolean res = port.addPossibleChildren(parentClassId, possibleChildren);
        if (!res)
            this.error = port.getLastErr();
        return res;
    }

    /*
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
        return lm;
    }

    public String getRootClass(){
        return port.getDummyRootClass();
    }

    public Long getRootId(){
        return port.getDummyRootId();
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

        if (port.moveObjects(targetOid, objectOids, objectClasses))
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

        List<RemoteObjectLight> objs = port.copyObjects(targetOid, objectOids, objectClasses);

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
}

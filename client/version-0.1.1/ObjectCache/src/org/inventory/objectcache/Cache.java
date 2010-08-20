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
package org.inventory.objectcache;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalObjectListItem;
import org.inventory.core.services.interfaces.LocalUserGroupObject;
import org.inventory.core.services.interfaces.LocalUserObject;

/**
 * This class implements the local caching functionality
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class Cache{
    private static Cache instance;
    private List<LocalObject> objectIndex; //Cache for objects (LocalObjects) --> Not used so far
    private Dictionary<String,LocalClassMetadata> metadataIndex; //Cache for metadata (the complete metadata information)
    private Dictionary<String,LocalClassMetadataLight> lightMetadataIndex; //Cache for lightmetadata (usually for administrative purposes)
    private Dictionary<String,List<LocalClassMetadataLight>> possibleChildrenIndex; //Cache for possible children
    private Dictionary<String,List<LocalObjectListItem>> listIndex; //Cache for list-type attributes
    private Long rootId = null;
    private Long rootClassId = null;
    /**
     * Information about the current logged user
     */
    private LocalUserObject currentUserInfo;
    /**
     * Information about the groups the current user belongs to
     */
    private LocalUserGroupObject[] currentUserGroupInfo;

    private Cache(){
        this.objectIndex = new ArrayList<LocalObject>();
        this.metadataIndex = new Hashtable<String, LocalClassMetadata>();
        this.lightMetadataIndex = new Hashtable<String, LocalClassMetadataLight>();
        this.possibleChildrenIndex = new Hashtable<String, List<LocalClassMetadataLight>>();
        this.listIndex = new Hashtable<String, List<LocalObjectListItem>>();
    }

    /**
     * This class is a singleton too
     * @return the singleton instance
     */
    public static Cache getInstace(){
        if(instance == null) instance = new Cache();
        return instance;
    }

    public void setRootId(Long _rootId){
        rootId = _rootId;
    }

    public Long getRootId(){
        return rootId;
    }

    public void setRootClass(Long _rootClassId){
        this.rootClassId = _rootClassId;
    }

    public Long getRootClass(){
        return rootClassId;
    }

    public void addObject(LocalObject lo) {
        this.objectIndex.add(lo);
    }

    public List<LocalObject> getObjectIndex() {
        return this.objectIndex;
    }

    public LocalClassMetadata[] getMetadataIndex(){
        LocalClassMetadata[] res = new LocalClassMetadata[this.metadataIndex.size()];
        int i = 0;
        Enumeration keys = metadataIndex.keys();
        while(keys.hasMoreElements()){
            res[i] = metadataIndex.get(keys.nextElement());
            i++;
        }
        return res;
    }

    public void resetMetadataIndex(){
        Enumeration keys = metadataIndex.keys();
        while(keys.hasMoreElements())
            metadataIndex.remove(keys.nextElement());
    }

    public LocalClassMetadata getMetaForClass(String className) {
        return this.metadataIndex.get(className);
    }

    public LocalClassMetadataLight getLightMetaForClass(String className) {
        return this.lightMetadataIndex.get(className);
    }

    public void addMeta(LocalClassMetadata[] all){
         for (LocalClassMetadata lcmi : all)
            this.metadataIndex.put(lcmi.getClassName(), lcmi);
    }

    public LocalClassMetadataLight[] getLightMetadataIndex() {
        LocalClassMetadataLight[] res = new LocalClassMetadataLight[this.metadataIndex.size()];
        int i = 0;
        Enumeration keys = lightMetadataIndex.keys();
        while(keys.hasMoreElements()){
            res[i] = metadataIndex.get(keys.nextElement());
            i++;
        }
        return res;
    }

    public void addLightMeta(LocalClassMetadataLight[] all){
        for (LocalClassMetadataLight lcml : all)
            this.lightMetadataIndex.put(lcml.getClassName(), lcml);
    }

    public void resetLightMetadataIndex() {
        Enumeration keys = lightMetadataIndex.keys();
        while(keys.hasMoreElements())
            lightMetadataIndex.remove(keys.nextElement());
    }

    public void addPossibleChildrenCached(String className, List<LocalClassMetadataLight> children){
        List<LocalClassMetadataLight> toBeAdded = new ArrayList<LocalClassMetadataLight>();
        for (LocalClassMetadataLight lcml : children){
            LocalClassMetadataLight myLocal = lightMetadataIndex.get(lcml.getClassName());
            if (myLocal==null){
                lightMetadataIndex.put(className, lcml);
                toBeAdded.add(lcml);
            }
            else
                toBeAdded.add(myLocal); //We reuse the instance in the light metadata index
        }
        possibleChildrenIndex.put(className,toBeAdded);
    }
    
    public List<LocalClassMetadataLight> getPossibleChildrenCached(String className){
        return possibleChildrenIndex.get(className);
    }

    public void resetPossibleChildrenCached() {
        Enumeration em = possibleChildrenIndex.keys();
        while (em.hasMoreElements())
            possibleChildrenIndex.remove(em.nextElement());
    }

    public Dictionary<String, List<LocalClassMetadataLight>> getAllPossibleChildren() {
        return possibleChildrenIndex;
    }

    public LocalObjectListItem[] getListCached(String className){
        List<LocalObjectListItem> existingItems = listIndex.get(className);
        if (existingItems == null) //The list is not cached
            return null;

        LocalObjectListItem[] res = new LocalObjectListItem[existingItems.size()];
        int i = 0;
        for (LocalObjectListItem item : existingItems){
            res[i] = item;
            i++;
        }
        return res;
    }

    public void addListCached(String className, List<LocalObjectListItem> items){
        listIndex.put(className, items);
    }

    public void resetLists() {
        Enumeration keys = listIndex.keys();
        while(keys.hasMoreElements())
            listIndex.remove(keys.nextElement());
    }

    public Dictionary<String, List<LocalObjectListItem>> getAllList() {
        return listIndex;
    }

    /**
     * Retrieves cached information about the current logged user
     * @return A LocalUserObject instance
     */
    public LocalUserObject getCurrentUserInfo(){
        return this.currentUserInfo;
    }

    /**
     * Get cached information about the groups the current user belongs to
     * @return
     */
    public LocalUserGroupObject[] getCurrentGroupsInfo(){
        return this.currentUserGroupInfo;
    }
}
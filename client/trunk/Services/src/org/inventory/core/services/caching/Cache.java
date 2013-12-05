/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.services.caching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.inventory.core.services.api.LocalObject;
import org.inventory.core.services.api.LocalObjectListItem;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.session.LocalUserGroupObject;
import org.inventory.core.services.api.session.LocalUserObject;

/**
 * This class implements the local caching functionality
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Cache{
    private static Cache instance;
    private List<LocalObject> objectIndex; //Cache for objects (LocalObjects) --> Not used so far
    private HashMap<String,LocalClassMetadata> metadataIndex; //Cache for metadata (the complete metadata information)
    private HashMap<String,LocalClassMetadataLight> lightMetadataIndex; //Cache for lightmetadata (usually for administrative purposes)
    private HashMap<String,List<LocalClassMetadataLight>> possibleChildrenIndex; //Cache for possible children
    private HashMap<String,List<LocalObjectListItem>> listIndex; //Cache for list-type attributes
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
        //this.objectIndex = new ArrayList<LocalObject>();
        this.metadataIndex = new HashMap<String, LocalClassMetadata>();
        this.lightMetadataIndex = new HashMap<String, LocalClassMetadataLight>();
        this.possibleChildrenIndex = new HashMap<String, List<LocalClassMetadataLight>>();
        this.listIndex = new HashMap<String, List<LocalObjectListItem>>();
    }

    /**
     * This class is a singleton too
     * @return the singleton instance
     */
    public static Cache getInstace(){
        if(instance == null) instance = new Cache();
        return instance;
    }

    public void setRootClass(Long rootClassId){
        this.rootClassId = rootClassId;
    }

    public Long getRootClass(){
        return rootClassId;
    }

    public void addObject(LocalObject lo) {
        if (objectIndex == null)
            objectIndex = new ArrayList<LocalObject>();
        this.objectIndex.add(lo);
    }

    public List<LocalObject> getObjectIndex() {
        return this.objectIndex;
    }

    public LocalClassMetadata[] getMetadataIndex(){
        return metadataIndex.values().toArray(new LocalClassMetadata[0]);
    }

    public LocalClassMetadata getMetaForClass(String className) {
        if (className == null)
            return null;
        return this.metadataIndex.get(className);
    }

    public LocalClassMetadataLight getLightMetaForClass(String className) {
        if (className == null)
            return null;
        //If the entry is already in the LocalClassMetadata cache, use it
        LocalClassMetadata possibleMetadata = metadataIndex.get(className);
        if (possibleMetadata != null)
            return possibleMetadata.asLocalClassMetadataLight();
        return this.lightMetadataIndex.get(className);
    }

    public void addMeta(LocalClassMetadata[] all){
         for (LocalClassMetadata lcmi : all)
            this.metadataIndex.put(lcmi.getClassName(), lcmi);
    }
    
    public void removeMeta(String className){
        metadataIndex.remove(className);
    }

    public LocalClassMetadataLight[] getLightMetadataIndex() {
        return metadataIndex.values().toArray(new LocalClassMetadataLight[0]);
    }

    public void addLightMeta(LocalClassMetadataLight[] all){
        for (LocalClassMetadataLight lcml : all)
            this.lightMetadataIndex.put(lcml.getClassName(), lcml);
    }

    public void addPossibleChildrenCached(String className, List<LocalClassMetadataLight> children){
        List<LocalClassMetadataLight> toBeAdded = new ArrayList<LocalClassMetadataLight>();
        for (LocalClassMetadataLight lcml : children){
            LocalClassMetadataLight myLocal = lightMetadataIndex.get(lcml.getClassName());
            if (myLocal==null){
                lightMetadataIndex.put(lcml.getClassName(), lcml);
                toBeAdded.add(lcml);
            }
            else
                toBeAdded.add(myLocal); //We reuse the instance in the light metadata index
        }
        possibleChildrenIndex.put(className,toBeAdded);
    }
    
    public List<LocalClassMetadataLight> getPossibleChildrenCached(String className){
        if (className == null)
            return null;
        return possibleChildrenIndex.get(className);
    }

    public HashMap<String, List<LocalClassMetadataLight>> getAllPossibleChildren() {
        return possibleChildrenIndex;
    }

    public List<LocalObjectListItem> getListCached(String className){
        if (className == null)
            return null;
        return listIndex.get(className);
    }

    public void addListCached(String className, List<LocalObjectListItem> items){
        listIndex.put(className, items);
    }

    public HashMap<String, List<LocalObjectListItem>> getAllList() {
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

    /**
     * Resets de cached list types
     */
    public void resetLists() {
        listIndex.clear();
    }

    /**
     * Resets the cached possible children
     */
    public void resetPossibleChildrenCached() {
        possibleChildrenIndex.clear();
    }

    /**
     * Resets the cached light class metadata
     */
    public void resetLightMetadataIndex() {
        lightMetadataIndex.clear();
    }

    /**
     * Resets the cached class metadata
     */
    public void resetMetadataIndex(){
        metadataIndex.clear();
    }
    
    public void resetAll(){
        listIndex.clear();
        possibleChildrenIndex.clear();
        metadataIndex.clear();
        lightMetadataIndex.clear();
    }
}
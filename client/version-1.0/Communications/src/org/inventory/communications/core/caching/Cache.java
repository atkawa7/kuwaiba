/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.communications.core.caching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.LocalReportDescriptor;
import org.inventory.communications.core.LocalUserGroupObject;
import org.inventory.communications.core.LocalUserObject;

/**
 * This class implements the local caching functionality
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Cache{
    private static Cache instance;
    private HashMap<String,LocalClassMetadata> metadataIndex; //Cache for metadata (the complete metadata information)
    private HashMap<String,LocalClassMetadataLight> lightMetadataIndex; //Cache for lightmetadata (usually for administrative purposes)
    private HashMap<String,List<LocalClassMetadataLight>> possibleChildrenIndex; //Cache for possible children
    private HashMap<String,List<LocalObjectListItem>> listIndex; //Cache for list-type attributes
    private HashMap<String, List<LocalReportDescriptor>> reportIndex; //Cache for class reports
    
    private Long rootClassId = null;
    /**
     * Information about the current logged user
     */
    private LocalUserObject currentUserInfo;
    /**
     * Information about the groups the current user belongs to
     */
    private LocalUserGroupObject[] currentUserGroupInfo;

    private Cache() {
        this.metadataIndex = new HashMap<>();
        this.lightMetadataIndex = new HashMap<>();
        this.possibleChildrenIndex = new HashMap<>();
        this.listIndex = new HashMap<>();
        this.reportIndex = new HashMap<>();
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

    public LocalClassMetadata[] getMetadataIndex(){
        return metadataIndex.values().toArray(new LocalClassMetadata[0]);
    }

    public LocalClassMetadata getMetaForClass(String className) {
        if (className == null)
            return null;
        return this.metadataIndex.get(className);
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
        List<LocalClassMetadataLight> toBeAdded = new ArrayList<>();
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
    
    public List<LocalReportDescriptor> getCachedReports(String className) {
        return reportIndex.get(className);
    }

    public void addListCached(String className, List<LocalObjectListItem> items){
        listIndex.put(className, items);
    }
    
    public void addReport(String className, List<LocalReportDescriptor> reports) {
        reportIndex.put(className, reports);
    }

    public HashMap<String, List<LocalObjectListItem>> getAllList() {
        return listIndex;
    }
    
    public HashMap<String, List<LocalReportDescriptor>> getReports() {
        return reportIndex;
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
    
    /**
     * Resets cached class reports
     */
    public void resetReportIndex(){
        reportIndex.clear();
    }
    
    public void resetAll(){
        listIndex.clear();
        possibleChildrenIndex.clear();
        metadataIndex.clear();
        lightMetadataIndex.clear();
        reportIndex.clear();
    }
}
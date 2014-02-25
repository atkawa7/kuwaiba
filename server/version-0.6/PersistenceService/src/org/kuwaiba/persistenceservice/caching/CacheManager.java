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

package org.kuwaiba.persistenceservice.caching;

import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.GenericObjectList;
import org.kuwaiba.persistenceservice.util.Constants;

/**
 * Manages the caching strategy
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class CacheManager {
    /**
     * Singleton
     */
    private static CacheManager cm;
    /**
     * Class cache
     */
    private HashMap<String, ClassMetadata> classIndex;
    /**
     * List type cache, the key is the listype 
     */
    private HashMap<String, GenericObjectList> listTypeIndex;
    /**
     * Possible children index. The key is the class, the value its possible children. Note that a blank key ("") represents the navigation tree root
     */
    private HashMap<String, List<String>> possibleChildrenIndex;
    /**
     * Users index. It is used to ease the username uniqueness validation
     */
    private HashMap<String, UserProfile> userIndex;
    /**
     * Groups index. It is used to ease the username uniqueness validation
     */
    private HashMap<String, GroupProfile> groupIndex;


    private CacheManager(){
        classIndex = new HashMap<String, ClassMetadata>();
        userIndex = new HashMap<String, UserProfile>();
        groupIndex = new HashMap<String, GroupProfile>();
        possibleChildrenIndex = new HashMap<String, List<String>>();
        listTypeIndex = new HashMap<String, GenericObjectList>();
    }

    public static CacheManager getInstance(){
        if (cm == null)
            cm = new CacheManager();
        return cm;
    }

    /**
     * Tries to retrieve a cached class
     * @param className the class to be retrieved from the cache
     * @return the cached version of the class. Null if it's  not cached
     */
    public ClassMetadata getClass(String className){
        return classIndex.get(className);
    }

    /**
     * Put/replaces an entry into the class cache
     * @param newClass
     */
    public void putClass(ClassMetadata newClass){
        classIndex.put(newClass.getName(), newClass);
    }
    
    /**
     * Remove a class from cache
     * @param className The class name
     */
    public void removeClass(String className){
        classIndex.remove(className);
    }

    /**
     * Adds an entry to the possible children index
     * @param parent
     * @param children
     */
    public void putPossibleChildren(String parent, List<String>children){
        possibleChildrenIndex.put(parent, children);
    }

    /**
     * Adds an entry to the possible children index
     * @param parent
     * @param children
     */
    public void putPossibleChild(String parent, String child){
        List<String> myList = possibleChildrenIndex.get(parent);
        if (myList != null)
            myList.add(child);
    }

    public void removePossibleChild(String parent, String child){
        List<String> myList = possibleChildrenIndex.get(parent);
        if (myList != null)
            myList.remove(child);
    }

    public List<String> getPossibleChildren(String parent){
        if (parent == null)
            return possibleChildrenIndex.get(Constants.NODE_DUMMYROOT);
        return possibleChildrenIndex.get(parent);
    }
    
    public void clearContainmentCache(){
        possibleChildrenIndex.clear();
    }

    /**
     * Tries to retrieve a cached user
     * @param userName the class to be retrieved from the cache
     * @return the cached version of the class. Null if it's  not cached
     */
    public UserProfile getUser(String userName){
        return userIndex.get(userName);
    }

    /**
     * Put/replaces an entry into the users cache
     * @param newUser user to be added
     */
    public void putUser(UserProfile newUser){
        userIndex.put(newUser.getUserName(), newUser);
    }

    /**
     * Tries to retrieve a cached user
     * @param userName the class to be retrieved from the cache
     * @return the cached version of the group. Null if it's  not cached
     */
    public GroupProfile getGroup(String groupName){
        return groupIndex.get(groupName);
    }

    /**
     * Put/replaces an entry into the group cache
     * @param newUser user to be added
     */
    public void putGroup(GroupProfile newGroup){
        groupIndex.put(newGroup.getName(), newGroup);
    }

    /**
     * Removes an entry into the users cache
     * @param userName
     */
    public void removeUser(String userName){
        userIndex.remove(userName);
    }

    /**
     * Removes an entry into the group cache
     * @param groupName
     */
    public void removeGroup(String groupName){
        userIndex.remove(groupName);
    }
    
    /**
     * Tries to retrieve a cached list type
     * @param listTypeName the list type to be retrieved from the cache
     * @return the cached version of the group. Null if it's  not cached
     */
    public GenericObjectList getListType(String listTypeName){
        return listTypeIndex.get(listTypeName);
    }
    
    /**
     * Put/replaces an entry into the list type cache
     * @param newListType list type to be added
     */
    public void putListType(GenericObjectList newListType){
        listTypeIndex.put(newListType.getClassName(), newListType);
    }
    
    /**
     * Removes an entry into the list type
     * @param listTypeName list type to be deleted
     */
    public void removeListType(String listTypeName){
        listTypeIndex.remove(listTypeName);
    }
    /**
     * Clear the cache
     */
    public void clear() {
        classIndex.clear();
        userIndex.clear();
        groupIndex.clear();
        possibleChildrenIndex.clear();
        listTypeIndex.clear();
    }

     /**
      * According to the cached metadata, finds out if a given class if subclass of another
      * @param allegedParentClass Possible super class
      * @param className Class to be evaluated
      * @return is className subClass of allegedParentClass?
      * @throws MetadataObjectNotFoundException If the class can not be found
      */
    public boolean isSubClass(String allegedParentClass, String className) throws MetadataObjectNotFoundException{

        if (className == null)
            return false;

        ClassMetadata currentClass = getClass(className);

        if (currentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Can not find a class with id %s", className));

        if (allegedParentClass.equals(className))
            return true;

        if (currentClass.getParentClassName() == null)
            return false;

        if (currentClass.getParentClassName().equals(allegedParentClass))
            return true;
        else
            return isSubClass(allegedParentClass, currentClass.getParentClassName());
    }

    /**
     * Finds out if a an instance of a given class can be child of an instance of allegedParent
     * @param allegedParent Possible parent
     * @param childToBeEvaluated Class to be evaluated
     * @return can an instance of childToBeEvaluated be a child of an instance of allegedParent
     * @throws MetadataObjectNotFoundException
     */
    public boolean canBeChild(String allegedParent, String childToBeEvaluated) throws MetadataObjectNotFoundException{
        List<String> possibleChildren;
        if (allegedParent == null) //The navigation tree root
            possibleChildren = possibleChildrenIndex.get("");
        else
            possibleChildren = possibleChildrenIndex.get(allegedParent);

        if (possibleChildren == null)
           throw new MetadataObjectNotFoundException(allegedParent);

        for (String possibleChild : possibleChildren){
            if (possibleChild.equals(childToBeEvaluated))
                return true;
        }
        return false;
    }
    
}

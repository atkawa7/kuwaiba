/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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

package org.kuwaiba.services.persistence.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.GenericObjectList;
import org.kuwaiba.services.persistence.util.Constants;

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
     * List type cache, the key is the list type 
     */
    private HashMap<String, GenericObjectList> listTypeIndex;
    /**
     * Possible children index. The key is the class, the value its possible children. Note that a blank key ("") represents the navigation tree root
     */
    private HashMap<String, List<String>> possibleChildrenIndex;
    /**
     * Possible special children index. The key is the class, the value its possible special children. Note that a blank key ("") represents the navigation tree root. The only difference with the possibleChildrenIndex, is that the relationship used to link the parent object with its children is CHILD_OF_SPECIAL
     */
    private HashMap<String, List<String>> possibleSpecialChildrenIndex;
    /**
     * List of subclasses of a class, the key is the name of the class, the value is the subclasses
     */
    private HashMap<String, List<ClassMetadataLight>> subClassesIndex;
    /**
     * List of subclasses of a class, the key is the name of the class, the value is the subclasses
     */
    private HashMap<String, List<ClassMetadataLight>> subClassesNoRecursiveIndex;
    /**
     * Users index. It is used to ease the username uniqueness validation
     */
    private HashMap<String, UserProfile> userIndex;
    /**
     * Groups index. It is used to ease the username uniqueness validation
     */
    private HashMap<String, GroupProfile> groupIndex;
    /**
     * List of the classes with unique attributes and its values index
     */
    private HashMap<String, HashMap<String, List<String>>> uniqueClassAttributesIndex;


    private CacheManager(){
        classIndex = new HashMap<>();
        userIndex = new HashMap<>();
        groupIndex = new HashMap<>();
        possibleChildrenIndex = new HashMap<>();
        possibleSpecialChildrenIndex = new HashMap<>();
        subClassesIndex =  new HashMap<>();
        subClassesNoRecursiveIndex = new HashMap<>();
        uniqueClassAttributesIndex = new HashMap<>();
        listTypeIndex = new HashMap<>();
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
     * Adds an entry to the possible special children index
     * @param parent The parent class
     * @param children the list of possible special children classes
     */
    public void putPossibleSpecialChildren(String parent, List<String>children){
        possibleSpecialChildrenIndex.put(parent, children);
    }

    public void putSubClassNoRecursive(String parent, ClassMetadataLight newSubClass){
        List<ClassMetadataLight> children = subClassesNoRecursiveIndex.get(parent);
        children.add(newSubClass);
        subClassesNoRecursiveIndex.put(parent, children);
    }
    /**
     * Adds an entry to the subclasses index
     * @param className the given class
     * @param subClasses  the subclasses of the given class
     */
    public void putSubclasses(String className, List<ClassMetadataLight> subClasses){
        subClassesIndex.put(className, subClasses);
    }
    
    /**
     * Adds an entry to the subclasses index
     * @param className the given class
     * @param subClasses  the subclasses of the given class
     */
    public void putSubclassesNorecursive(String className, List<ClassMetadataLight> subClasses){
        subClassesNoRecursiveIndex.put(className, subClasses);
    }
    
    /**
     * adds an entry for every unique attribute value of every class that has unique attributes
     * @param className class name
     * @param attributeName attribute name 
     * @param value new value of an unique attribute
     */
    public void putUniqueAttributeValueIndex(String className, String attributeName, String value){
        HashMap<String, List<String>> uniqueClassAttributes = uniqueClassAttributesIndex.get(className);
        if(uniqueClassAttributes == null){
            uniqueClassAttributes = new HashMap<>();
            List<String> values = new ArrayList<>();
            values.add(value);
            uniqueClassAttributes.put(attributeName, values);
            uniqueClassAttributesIndex.put(className, uniqueClassAttributes);
        }
        else{
            List<String> values = uniqueClassAttributes.get(attributeName);
            if(values == null){
                values = new ArrayList<>();
                values.add(value);
                uniqueClassAttributes.put(attributeName, values);
                uniqueClassAttributesIndex.put(className, uniqueClassAttributes);
            }
            else{
                values.add(value);
                uniqueClassAttributes.put(attributeName, values);
                uniqueClassAttributesIndex.put(className, uniqueClassAttributes);
            }
        }
    }
    
    public void putUniqueAttributeValuesIndex(String className, String attributeName, List<String> values){
        HashMap<String, List<String>> uniqueClassAttributes = uniqueClassAttributesIndex.get(className);
        if(uniqueClassAttributes == null){
            uniqueClassAttributes = new HashMap<>();
            uniqueClassAttributes.put(attributeName, values);
            uniqueClassAttributesIndex.put(className, uniqueClassAttributes);
        }
        else{
            uniqueClassAttributes.put(attributeName, values);
            uniqueClassAttributesIndex.put(className, uniqueClassAttributes);
        }
    }
    
    /**
     * Adds an entry to the possible children index
     * @param parent
     * @param child
     */
    public void putPossibleChild(String parent, String child){
        List<String> myList = possibleChildrenIndex.get(parent);
        if (myList != null)
            myList.add(child);
    }
    
    /**
     * Adds a single entry to the possible special children index
     * @param parent
     * @param child
     */
    public void putPossibleSpecialChild(String parent, String child){
        List<String> myList = possibleSpecialChildrenIndex.get(parent);
        if (myList != null)
            myList.add(child);
    }

    public void removePossibleChild(String parent, String child){
        List<String> myList = possibleChildrenIndex.get(parent);
        if (myList != null)
            myList.remove(child);
    }
            
    public void removePossibleSpecialChild(String parent, String child){
        List<String> myList = possibleSpecialChildrenIndex.get(parent);
        if (myList != null)
            myList.remove(child);
    }
    
    public List<String> getPossibleChildren(String parent){
        if (parent == null)
            return possibleChildrenIndex.get(Constants.NODE_DUMMYROOT);
        return possibleChildrenIndex.get(parent);
    }
    
    public List<String> getPossibleSpecialChildren(String parent){
        if (parent == null) //Should not happen
            return possibleSpecialChildrenIndex.get(Constants.NODE_DUMMYROOT);
        return possibleSpecialChildrenIndex.get(parent);
    }
    
    public List<ClassMetadataLight> getSubclasses(String className){
        return subClassesIndex.get(className);
    }
    
    public List<ClassMetadataLight> getSubclassesNorecursive(String className){
        return subClassesNoRecursiveIndex.get(className);
    }
    
    public HashMap<String, List<String>> getUniqueClassAttributes(String className){
        return uniqueClassAttributesIndex.get(className);
    }
    
    public List<String> getUniqueAttributeValues(String className, String attributeName){
        if (uniqueClassAttributesIndex.get(className) != null)
            return uniqueClassAttributesIndex.get(className).get(attributeName);
        else
            return null;
    }
    
    public void clearClassCache(){
        classIndex.clear();
        possibleChildrenIndex.clear();
        possibleSpecialChildrenIndex.clear();
        subClassesIndex.clear();
        subClassesNoRecursiveIndex.clear();
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
     * @param groupName the class to be retrieved from the cache
     * @return the cached version of the group. Null if it's  not cached
     */
    public GroupProfile getGroup(String groupName){
        return groupIndex.get(groupName);
    }

    /**
     * Put/replaces an entry into the group cache
     * @param newGroup user to be added
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
    
    public void removeUniqueAtribute(String className, String attributeName){
        HashMap<String, List<String>> uniqueClassAttributes = uniqueClassAttributesIndex.get(className);
        uniqueClassAttributes.remove(attributeName);
    }    
    
    public void removeUniqueAttributeValue(String className, String attributeName, String attributeValue){
        HashMap<String, List<String>> uniqueClassAttributes = uniqueClassAttributesIndex.get(className);
        List<String> uniqueValues = uniqueClassAttributes.get(attributeName);
        if(uniqueValues != null)
            uniqueValues.remove(attributeValue);
        uniqueClassAttributes.put(attributeName, uniqueValues);
        uniqueClassAttributesIndex.put(className, uniqueClassAttributes);
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
    public void clearAll() {
        classIndex.clear();
        userIndex.clear();
        groupIndex.clear();
        possibleChildrenIndex.clear();
        possibleSpecialChildrenIndex.clear();
        listTypeIndex.clear();
        subClassesIndex.clear();
        uniqueClassAttributesIndex.clear();
    }

     /**
      * According to the cached metadata, finds out if a given class if subclass of another
      * @param allegedParentClass Possible super class
      * @param className Class to be evaluated
      * @return is className subClass of allegedParentClass?
      */
    public boolean isSubClass(String allegedParentClass, String className) {

        if (className == null)
            return false;

        ClassMetadata currentClass = getClass(className);

        if (currentClass == null)
            return false;

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
    
    public boolean canBeSpecialChild(String allegedParent, String childToBeEvaluated) throws MetadataObjectNotFoundException{
        List<String> possibleSpecialChildren;
        if (allegedParent == null) //The navigation tree root
            possibleSpecialChildren = possibleSpecialChildrenIndex.get("");
        else
            possibleSpecialChildren = possibleSpecialChildrenIndex.get(allegedParent);

        if (possibleSpecialChildren == null)
           throw new MetadataObjectNotFoundException(allegedParent);

        for (String possibleSpecialChild : possibleSpecialChildren){
            if (possibleSpecialChild.equals(childToBeEvaluated))
                return true;
        }
        return false;
    }
}

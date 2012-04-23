/**
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

package org.kuwaiba.persistenceservice.caching;

import java.util.HashMap;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;

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
     * Users index. It is used to ease the username uniqueness validation
     */
    private HashMap<String, UserProfile> userIndex;


    private CacheManager(){
        classIndex = new HashMap<String, ClassMetadata>();
        userIndex = new HashMap<String, UserProfile>();
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
     * Clear the cache
     */
    public void clear() {
        classIndex.clear();
        userIndex.clear();
    }

    /**
     * According to the cached metadata, finds out if a given class if subclass of another
     * @param allegedParentClass Possible super class
     * @param className Class to be evaluated
     * @return is className subClass of allegedParentClass?
     */
    public boolean isSubClass(String allegedParentClass, String className) throws MetadataObjectNotFoundException{

        if (className == null)
            return false;

        ClassMetadata currentClass = getClass(className);

        if (currentClass == null)
            throw new MetadataObjectNotFoundException(className);

        if (currentClass.getParentClassName().equals(allegedParentClass))
            return true;
        else
            return isSubClass(allegedParentClass, currentClass.getParentClassName());
    }
}

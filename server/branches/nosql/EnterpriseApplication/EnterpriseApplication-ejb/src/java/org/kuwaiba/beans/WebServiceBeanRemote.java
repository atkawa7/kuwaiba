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

package org.kuwaiba.beans;

import java.util.List;
import javax.ejb.Remote;

import org.kuwaiba.exceptions.InvalidSessionException;
import org.kuwaiba.exceptions.NotAuthorizedException;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.ws.toserialize.application.RemoteSession;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.metadata.AttributeInfo;
import org.kuwaiba.ws.toserialize.metadata.CategoryInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfoLight;

/**
 * Main session bean backing the webservice
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Remote
public interface WebServiceBeanRemote {

    // <editor-fold defaultstate="collapsed" desc="Session methods. Click on the + sign on the left to edit the code.">
    /**
     *
     * @param user
     * @param password
     * @param IPAddress
     * @return
     * @throws NotAuthorizedException
     */
    public RemoteSession createSession(String user, String password, String IPAddress) throws NotAuthorizedException;
    /**
     * 
     * @param sessionId
     * @param remoteAddress
     * @return
     */
    public void closeSession(String sessionId, String remoteAddress) throws InvalidSessionException;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Metadata methods. Click on the + sign on the left to edit the code.">

    /**
     * Creates's a classMetada
     * @param classDefinition
     * @return
     * @throws ServerSideException
     */
    public Long createClass(ClassInfo classDefinition) throws ServerSideException;

    /**
     * Changes a classmetadata definition
     * @param newClassDefinition
     * @return true if success
     * @throws ClassNotFoundException if there is no class with such classId
     */

    public void changeClassDefinition(ClassInfo newClassDefinition) throws ServerSideException;

    /**
     * Sets a given attribute for a class metadata
     * @param classId
     * @param attributeName
     * @param attributeValue
     * @return
     * @throws Exception
     * @throws ServerSideException
     */
    public void setClassPlainAttribute(Long classId, String attributeName,
            String attributeValue)throws ServerSideException;

    /**
     * Set a class icon (big or small)
     * @param classId
     * @param attributeName
     * @param iconImage
     */
    public void setClassIcon(Long classId, String attributeName, byte[] iconImage) throws ServerSideException;

    /**
     * Deletes a classmetadata, its attributes and category relationships
     * @param classId
     * @return true if success
     * @throws ClassNotFoundException if there is not a class with de ClassId
     */
    
    public void deleteClass(String className) throws ServerSideException;

    /**
     * Deletes a classmetadata, its attributes and category relationships
     * @param classId
     */
    public void deleteClass(Long classId) throws ServerSideException;

    /**
     * Retrieves the simplified list of classes. This list won't include either
     * those classes marked as dummy
     * @param includeListTypes boolean to indicate if the list should include
     * the subclasses of GenericObjectList
     * @return the list of classes
     * @throws Exception EntityManagerNotAvailableException or something unexpected
     */
    public List<ClassInfoLight> getLightMetadata(Boolean includeListTypes) throws ServerSideException;

    /**
     * Retrieves all the class metadata except for classes marked as dummy
     * @param includeListTypes boolean to indicate if the list should include
     * the subclasses of GenericObjectList
     * @return An array of classes
     */
    public List<ClassInfo> getMetadata(Boolean includeListTypes) throws ServerSideException;

    /**
     * Gets Metadata For Class id its attributes and Category
     * @param className
     * @return A ClassMetadata with the className
     * @throws ClassNotFoundException there is no class with such className
     */
    public ClassInfo getMetadataForClass(String className) throws ServerSideException;

    /**
     * Gets Metadata For Class id its attributes and Category
     * @param classId
     * @return
     * @throws Exception
     */
    public ClassInfo getMetadataForClass(Long classId) throws ServerSideException;

    /**
     * Moves a class from one parentClass to an other parentClass
     * @param classToMoveName
     * @param targetParentName
     * @throws Exception
     */
    public void moveClass(String classToMoveName, String targetParentName) throws ServerSideException;
    
    /**
     * Moves a class from one parentClass to an other parentClass
     * @param classToMoveId
     * @param targetParentClassId
     */
    public void moveClass(Long classToMoveId, Long targetParentId) throws ServerSideException;

    /**
     * Adds an attribute to the class
     * @param className
     * @param attributeDefinition
     */
    public void addAttribute(String className, AttributeInfo attributeDefinition) throws ServerSideException;

    /**
     * Adds an attribute to a class
     * @param classId
     * @param attributeDefinition
     */
    public void addAttribute(Long classId, AttributeInfo attributeDefinition) throws ServerSideException;
    
    /**
     * Gets an attribute belonging to a class
     * @param className
     * @param attributeName
     * @return AttributeMetada, null if there is no attribute with such name
     */
    public AttributeInfo getAttribute(String className, String attributeName) throws ServerSideException;

    /**
     * Gets an attribute belonging to a class
     * @param classId
     * @param attributeName
     * @return AttributeMetada, null if there is no attribute with such name
     */
    public AttributeInfo getAttribute(Long classId, String attributeName) throws ServerSideException;

    /**
     * Changes an attribute definition belonging to a classMetadata
     * @param ClassId
     * @param newAttributeDefinition
     */
    public void changeAttributeDefinition(Long ClassId, AttributeInfo newAttributeDefinition) throws ServerSideException;

    /**
     * Deletes an attribute belonging to a classMetadata
     * @param className
     * @param attributeName
     */
    public void  deleteAttribute(String className, String attributeName) throws ServerSideException;

    /**
     * Deletes an attribute belonging to a classMetadata
     * @param classId
     * @param attributeName
     */
    public void deleteAttribute(Long classId,String attributeName) throws ServerSideException;

    /**
     * Creates a new category
     * @param categoryDefinition
     * @return CategoryId
     */
    public Long createCategory(CategoryInfo categoryDefinition) throws ServerSideException;

    /**
     * Gets a Category with it's name
     * @param categoryName
     * @return CategoryMetadata
     * @throws MiscException if the Category does not exist
     */
    public CategoryInfo getCategory(String categoryName) throws ServerSideException;

    /**
     * Gets a Category with it's Id
     * @param categoryId
     * @return CategoryMetadata
     * @throws MiscException if there is no Category with such cetegoryId
     */
    public CategoryInfo getCategory(Integer categoryId) throws ServerSideException;

    /**
     * Changes a category definition
     * @param categoryDefinition
     */
    public void changeCategoryDefinition(CategoryInfo categoryDefinition) throws ServerSideException;

        /**
     * Gets all classes whose instances can be contained into the given parent class. This method
     * is recursive, so the result include the possible children in children classes
     * @param parentClass
     * @return an array with the list of classes
     */
    public List<ClassInfoLight> getPossibleChildren(String parentClassName) throws ServerSideException;

    /**
     * Same as getPossibleChildren but this one only gets the possible children for the given class,
     * this is, subclasses are not included
     * @param parentClass
     * @return The list of possible children
     */
    public List<ClassInfoLight> getPossibleChildrenNoRecursive(String parentClassName) throws ServerSideException;
    /**
     * Adds to a given class a list of possible children classes whose instances can be contained
     *
     * @param parentClassId Id of the class whose instances can contain the instances of the next param
     * @param _possibleChildren ids of the candidates to be contained
     */
    public void addPossibleChildren(Long parentClassId, Long[] _possibleChildren) throws ServerSideException;

    /**
     * The opposite of addPossibleChildren. It removes the given possible children
     * TODO: Make this method safe. This is, check if there's already intances of the given
     * "children to be deleted" with parentClass as their parent
     * @param parentClassId Id of the class whos instances can contain the instances of the next param
     * @param childrenTBeRemoved ids of the candidates to be deleted
     */
    public void removePossibleChildren(Long parentClassId, Long[] childrenToBeRemoved) throws ServerSideException;


    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Business methods. Click on the + sign on the left to edit the code.">
    public RemoteObjectLight[] getObjectChildren(Long oid, Long objectClassId) throws ServerSideException;
    public RemoteObjectLight[] getObjectChildren(String objectClassName, Long oid) throws ServerSideException;

    public RemoteObject[] getChildrenOfClass(Long parentOid, String parentClass,String classToFilter) throws ServerSideException;
    public RemoteObjectLight[] getChildrenOfClassLight(Long parentOid, String parentClass,String classToFilter) throws ServerSideException;

    public RemoteObject getObjectInfo(String objectClass, Long oid) throws ServerSideException;

    public RemoteObjectLight getObjectInfoLight(String objectClass, Long oid) throws ServerSideException;

    public void updateObject(String className, Long oid, String[] attributeNames, String[] attributeValues) throws ServerSideException;

    public Long createObject(String className, String parentClassName, Long parentOid, String[] attributeNames, String[] attributeValues, Long templateId) throws ServerSideException;

    public Long createListTypeItem(String className, String name, String displayName) throws ServerSideException;
    public RemoteObjectLight[] getListTypeItems(String className) throws ServerSideException;

    public ClassInfoLight[] getInstanceableListTypes()throws ServerSideException;
   
    // </editor-fold>


}

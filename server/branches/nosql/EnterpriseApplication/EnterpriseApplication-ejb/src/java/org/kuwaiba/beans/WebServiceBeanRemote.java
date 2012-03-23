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

    public boolean changeClassDefinition(ClassInfo newClassDefinition) throws ServerSideException;

   /**
     * Set a class icon (big or small)
     * @param classId
     * @param attributeName
     * @param iconImage
     * @return
     */
    public Boolean setClassIcon(Long classId, String attributeName, byte[] iconImage) throws ServerSideException;

    /**
     * Deletes a classmetadata, its attributes and category relationships
     * @param classId
     * @return true if success
     * @throws ClassNotFoundException if there is not a class with de ClassId
     */
    
    public boolean deleteClass(String className) throws ServerSideException;

    /**
     * Deletes a classmetadata, its attributes and category relationships
     * @param classId
     * @return true if success
     * @throws ClassNotFoundException if there is not a class with de ClassName
     */
    public boolean deleteClass(Long classId) throws ServerSideException;

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
     * @return
     * @throws Exception
     */
    public boolean moveClass(String classToMoveName, String targetParentName) throws ServerSideException;
    
    /**
     * Moves a class from one parentClass to an other parentClass
     * @param classToMoveId
     * @param targetParentClassId
     * @return true if success
     * @throws ClassNotFoundException if there is no a classToMove with such classId
     * or if there is no a targetParentClass with such classId
     */
    public boolean moveClass(Long classToMoveId, Long targetParentId) throws ServerSideException;

    /**
     * Adds an attribute to the class
     * @param className
     * @param attributeDefinition
     * @return true if success
     * @throws ClassNotFoundException if there is no a class with such className
     */
    public boolean addAttribute(String className, AttributeInfo attributeDefinition) throws ServerSideException;

    /**
     * Adds an attribute to a class
     * @param classId
     * @param attributeDefinition
     * @return true if success
     * @throws ClassNotFoundException if there is no a class with such classId
     */
    public boolean addAttribute(Long classId, AttributeInfo attributeDefinition) throws ServerSideException;
    
    /**
     * Gets an attribute belonging to a class
     * @param className
     * @param attributeName
     * @return AttributeMetada, null if there is no attribute with such name
     * @throws ClassNotFoundException if there is no a class with such className
     * @throws MiscException if the attributeName does not exist
     */
    public AttributeInfo getAttribute(String className, String attributeName) throws ServerSideException;

    /**
     * Gets an attribute belonging to a class
     * @param classId
     * @param attributeName
     * @return AttributeMetada, null if there is no attribute with such name
     * @throws ClassNotFoundException if there is no a class with such classId
     * @throws MiscException if the attributeName does not exist
     */
    public AttributeInfo getAttribute(Long classId, String attributeName) throws ServerSideException;

    /**
     * Changes an attribute definition belonging to a classMetadata
     * @param ClassId
     * @param newAttributeDefinition
     * @return
     */
    public boolean changeAttributeDefinition(Long ClassId, AttributeInfo newAttributeDefinition) throws ServerSideException;

    /**
     * Deletes an attribute belonging to a classMetadata
     * @param className
     * @param attributeName
     * @return true if success
     * @throws ClassNotFoundException if there is no a class with such className
     * @throws MiscException if the attributeName does not exist
     */
    public boolean  deleteAttribute(String className, String attributeName) throws ServerSideException;

    /**
     * Deletes an attribute belonging to a classMetadata
     * @param classId
     * @param attributeName
     * @return true if success
     * @throws ClassNotFoundException if there is no a class with such classId
     * @throws MiscException if the attributeName does not exist
     */
    public boolean deleteAttribute(Long classId,String attributeName) throws ServerSideException;

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
     * @return true if success
     * @throws MiscException if there is no Category with such cetegoryId
     */
    public boolean changeCategoryDefinition(CategoryInfo categoryDefinition) throws ServerSideException;

// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Business methods. Click on the + sign on the left to edit the code.">
    public RemoteObjectLight[] getObjectChildren(Long oid, Long objectClassId) throws ServerSideException;
    public RemoteObjectLight[] getObjectChildren(String objectClassName, Long oid) throws ServerSideException;

    public RemoteObject[] getChildrenOfClass(Long parentOid, String parentClass,String classToFilter) throws ServerSideException;
    public RemoteObjectLight[] getChildrenOfClassLight(Long parentOid, String parentClass,String classToFilter) throws ServerSideException;

    public RemoteObject getObjectInfo(String objectClass, Long oid) throws ServerSideException;

    public RemoteObjectLight getObjectInfoLight(String objectClass, Long oid) throws ServerSideException;

    public void updateObject(String className, Long oid, String[] attributeNames, String[] attributeValues) throws ServerSideException;

    public Long createObject(String className, Long parentOid, String[] attributeNames, String[] attributeValues, String template) throws ServerSideException;

    
    // </editor-fold>


}

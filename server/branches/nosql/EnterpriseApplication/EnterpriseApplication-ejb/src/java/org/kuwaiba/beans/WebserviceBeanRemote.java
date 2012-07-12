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
import org.kuwaiba.exceptions.NotAuthorizedException;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.ws.todeserialize.TransientQuery;
import org.kuwaiba.ws.toserialize.application.RemoteQuery;
import org.kuwaiba.ws.toserialize.application.RemoteQueryLight;
import org.kuwaiba.ws.toserialize.application.RemoteSession;
import org.kuwaiba.ws.toserialize.application.ResultRecord;
import org.kuwaiba.ws.toserialize.application.UserGroupInfo;
import org.kuwaiba.ws.toserialize.application.UserInfo;
import org.kuwaiba.ws.toserialize.application.ViewInfo;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.metadata.AttributeInfo;
import org.kuwaiba.ws.toserialize.metadata.CategoryInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfoLight;
@Remote
public interface WebserviceBeanRemote {


    // <editor-fold defaultstate="collapsed" desc="Session methods. Click on the + sign on the left to edit the code.">
    /**
     *
     * @param user
     * @param password
     * @param IPAddress
     * @return
     * @throws NotAuthorizedException
     */
    public RemoteSession createSession(String user, String password, String IPAddress) throws ServerSideException;
    /**
     *
     * @param sessionId
     * @param remoteAddress
     * @return
     */
    public void closeSession(String sessionId, String remoteAddress) throws NotAuthorizedException;
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

    public void updateClassDefinition(ClassInfo newClassDefinition) throws ServerSideException;

    /**
     * Sets the value of a property associated to an attribute. So far there are only
     * 4 possible properties:
     * -displayName
     * -isVisible
     * -isAdministrative
     * -description
     * @param classId
     * @param attributeName
     * @param propertyName
     * @param propertyValue
     * @return
     * @throws ServerSideException
     */

    public void setAttributePropertyValue(Long classId, String attributeName,
            String propertyName, String propertyValue) throws ServerSideException;

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
    public void addPossibleChildren(Long parentClassId, Long[] possibleChildren) throws ServerSideException;
    /**
     * Adds to a given class a list of possible children classes whose instances can be contained using the class name to find the parent class
     * @param parentClassName parent class name
     * @param newPossibleChildren list of possible children
     * @throws ServerSideException In case something goes wrong
     */
    public void addPossibleChildren(String parentClassName, String[] newPossibleChildren) throws ServerSideException;

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
    public RemoteObjectLight[] getObjectChildren(Long oid, Long objectClassId, int maxResults) throws ServerSideException;
    public RemoteObjectLight[] getObjectChildren(String objectClassName, Long oid, int maxResults) throws ServerSideException;

    public RemoteObject[] getChildrenOfClass(Long parentOid, String parentClass,String classToFilter, int maxResults) throws ServerSideException;
    public RemoteObjectLight[] getChildrenOfClassLight(Long parentOid, String parentClass,String classToFilter, int maxResults) throws ServerSideException;

    public RemoteObject getObjectInfo(String objectClass, Long oid) throws ServerSideException;

    public RemoteObjectLight getObjectInfoLight(String objectClass, Long oid) throws ServerSideException;

    public String[] getSpecialAttribute(String objectClass, Long objectId, String attributeName) throws ServerSideException;

    public void updateObject(String className, Long oid, String[] attributeNames, String[][] attributeValues) throws ServerSideException;

    public Long createObject(String className, String parentClassName, Long parentOid, String[] attributeNames, String[][] attributeValues, Long templateId) throws ServerSideException;

    public Long createListTypeItem(String className, String name, String displayName) throws ServerSideException;

    public void deleteListTypeItem(String className, Long oid, boolean releaseRelationships) throws ServerSideException;

    public RemoteObjectLight[] getListTypeItems(String className) throws ServerSideException;

    public ClassInfoLight[] getInstanceableListTypes() throws ServerSideException;

    public void deleteObjects(String classNames[], Long[] oids, boolean releaseRelationships) throws ServerSideException;

    public void moveObjects(String targetClass, Long targetOid, String[] objectClasses, Long[] objectOids) throws ServerSideException;

    public Long[] copyObjects(String targetClass, Long targetOid, String[] templateClasses, Long[] templateOids, boolean recursive) throws ServerSideException;

    /**
     * Models
     */
    //Physical connections
    public Long createPhysicalConnection(String aObjectClass, Long aObjectId, String bObjectClass, Long bObjectId, String parentClass, Long parentId, String[] attributeNames, String[][] attributeValues, String connectionClass) throws ServerSideException;
    public void deletePhysicalConnection(String objectClass, Long objectId) throws ServerSideException;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Application methods. Click on the + sign on the left to edit the code.">
    /**
     *
     * @param userName
     * @param password
     * @param firstName
     * @param lastName
     * @param privileges
     * @param groups
     * @throws ServerSideException
     */
    public void setUserProperties(Long oid, String userName, String password, String firstName,
            String lastName, Boolean enabled, Integer[] privileges, Long[] groups)
            throws ServerSideException;


    /**
     * Creates a group
     * @param name
     * @param description
     * @param creationDate
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public Long createGroup(String groupName, String description,
            Integer[] privileges, Long[] users) throws ServerSideException;

    /**
     * Get all users
     * @return
     * @throws ServerSideException
     */
    public UserInfo[] getUsers() throws ServerSideException;

    /**
     * Get All Groups
     * @return
     * @throws ServerSideException
     */
    public UserGroupInfo[] getGroups() throws ServerSideException;

    /**
     * Creates a new user
     * @return The newly created user
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public Long createUser(String userName, String password, String firstName,
            String lastName, Boolean enabled, Integer[] privileges, Long[] groups)
            throws ServerSideException;;

    /**
     * Set user attributes (group membership is managed using other methods)
     * @param groupName
     * @param description
     * @param privileges
     * @return
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public void setGroupProperties(Long oid, String groupName, String description,
            Integer[] privileges, Long[] users)throws ServerSideException;

     /**
     * Removes a list of users
     * @param oids
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public void deleteUsers(Long[] oids)throws ServerSideException;

    /**
     * Removes a list of groups
     * @param oids
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public void deleteGroups(Long[] oids)
            throws ServerSideException;

    /**
     *
     * @param oid
     * @param objectClass
     * @param viewType
     * @return
     * @throws ServerSideException
     */
    public ViewInfo getView(Long oid, String objectClass, Integer viewType) throws ServerSideException;

    /**
     *
     * @param oid
     * @param objectClass
     * @param viewType
     * @param structure
     * @param background
     * @throws ServerSideException
     */
    public void saveView(Long oid, String objectClass, int viewType, byte[] structure, byte[] background) throws ServerSideException;

    /**
     * Executes a complex query generated using the Graphical Query Builder.  Please note
     * that the first record is reserved for the column headers, so and empty result set
     * will have at least one record.
     * @param query
     * @return
     * @throws ServerSideException
     */
    public ResultRecord[] executeQuery(TransientQuery query) throws ServerSideException;

    /**
     * Creates a query using the Graphical Query Builder
     * @param queryName
     * @param ownerOid
     * @param queryStructure
     * @param description
     * @return
     * @throws ServerSideException
     */
    public Long createQuery(String queryName, Long ownerOid, byte[] queryStructure,
            String description) throws ServerSideException;

    /**
     * Save the query made in the graphical Query builder
     * @param queryOid
     * @param queryName
     * @param ownerOid
     * @param queryStructure
     * @param description
     * @throws ServerSideException
     */
    public void saveQuery(Long queryOid, String queryName,
            Long ownerOid, byte[] queryStructure, String description) throws ServerSideException;

    /**
     * Deletes the query load in the graphical query builder
     * @param queryOid
     * @throws ServerSideException
     */
    public void deleteQuery(Long queryOid) throws ServerSideException;

    /**
     * Retrieves all queries made in the graphical Query builder
     * @param showPublic
     * @return
     * @throws ServerSideException
     */
    public RemoteQueryLight[] getQueries(boolean showPublic) throws ServerSideException;

    /**
     * Load a query from all saved queries
     * @param queryOid
     * @return
     * @throws ServerSideException
     */
    public RemoteQuery getQuery(Long queryOid) throws ServerSideException;

    /**
     *
     * @param showAll
     * @return a byte representing an XML document with the class hierarchy
     * @throws ServerSideException If something goes wrong
     */
    public byte[] getClassHierarchy(boolean showAll) throws ServerSideException;

    /**
     * Verifies if a given user is able to call a webservice method according to its privileges
     * @param methodName method that is trying to be called
     * @param ipAddress IP Address where th request is comming from
     * @param sessionId Session token
     */
    public void validateCall(String methodName, String ipAddress, String sessionId) throws NotAuthorizedException;

    // </editor-fold>

}

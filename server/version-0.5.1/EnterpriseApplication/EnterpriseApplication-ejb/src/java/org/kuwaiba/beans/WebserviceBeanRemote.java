/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>
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
import org.kuwaiba.beans.sessions.Session;
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
import org.kuwaiba.ws.toserialize.application.ViewInfoLight;
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
     * Closes a session
     * @param sessionId
     * @param remoteAddress
     * @return
     */
    public void closeSession(String sessionId, String remoteAddress) throws NotAuthorizedException;

    /**
     * Returns the user related to the given session id
     * @param sessionId The session id
     * @return The user or null if none
     */
    public UserInfo getUserInSession(String sessionId);
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Metadata methods. Click on the + sign on the left to edit the code.">

    /**
     * Creates a new class metadata entry
     * @param classDefinition
     * @return
     * @throws ServerSideException
     */
    public long createClass(ClassInfo classDefinition) throws ServerSideException;

    /**
     * Changes a classmetadata definition
     * @param newClassDefinition
     * @return true if success
     * @throws ClassNotFoundException if there is no class with such classId
     */

    public void setClassProperties (ClassInfo newClassDefinition) throws ServerSideException;

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
    public void deleteClass(long classId) throws ServerSideException;

    /**
     * Retrieves the simplified list of classes. This list won't include either
     * those classes marked as dummy
     * @param includeListTypes boolean to indicate if the list should include
     * the subclasses of GenericObjectList
     * @return the list of classes
     * @throws ServerSideException EntityManagerNotAvailableException or something unexpected
     */
    public List<ClassInfoLight> getAllClassesLight(boolean includeListTypes) throws ServerSideException;
    
    /**
     * Gets the subclasses of a given class
     * @param className Class name
     * @param includeAbstractClasses should the result include the abstract classes?
     * @param includeSelf Should the list include the subclasses and the parent class?
     * @param sessionId Session token
     * @return The list of subclasses
     * @throws Exception If the class can not be found
     */
    public List<ClassInfoLight> getSubClassesLight(String className, boolean includeAbstractClasses, boolean includeSelf) throws ServerSideException;

    /**
     * Gets the subclasses of a given class
     * @param className Class name
     * @param includeAbstractClasses should the result include the abstract classes?
     * @param includeSelf Should the list include the subclasses and the parent class?
     * @param sessionId Session token
     * @return The list of subclasses
     * @throws Exception If the class can not be found
     */
    public List<ClassInfoLight> getSubClassesLightNoRecursive(String className, boolean includeAbstractClasses, boolean includeSelf) throws ServerSideException;
    
    /**
     * Retrieves all the class metadata except for classes marked as dummy
     * @param includeListTypes boolean to indicate if the list should include
     * the subclasses of GenericObjectList
     * @return An array of classes
     */
    public List<ClassInfo> getAllClasses(boolean includeListTypes) throws ServerSideException;

    /**
     * Gets Metadata For Class id its attributes and Category
     * @param className
     * @return A ClassMetadata with the className
     * @throws ClassNotFoundException there is no class with such className
     */
    public ClassInfo getClass(String className) throws ServerSideException;

    /**
     * Gets the metadata for a class, providing its id
     * @param classId
     * @return
     * @throws Exception
     */
    public ClassInfo getClass(long classId) throws ServerSideException;

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
    public void moveClass(long classToMoveId, long targetParentId) throws ServerSideException;

    /**
     * Adds an attribute to the class
     * @param className
     * @param attributeDefinition
     */
    public void createAttribute(String className, AttributeInfo attributeDefinition) throws ServerSideException;

    /**
     * Adds an attribute to a class
     * @param classId
     * @param attributeDefinition
     */
    public void createAttribute(long classId, AttributeInfo attributeDefinition) throws ServerSideException;

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
    public AttributeInfo getAttribute(long classId, long attributeId) throws ServerSideException;

    /**
     * Changes an attribute definition using an id to get the class it belongs to
     * @param classId Class name this attribute belongs to
     * @param newAttributeDefinition
     */
    public void setAttributeProperties(long classId, AttributeInfo newAttributeDefinition) throws ServerSideException;
    /**
     * Changes an attribute definition using name to get the class it belongs to
     * @param className Class name this attribute belongs to
     * @param newAttributeDefinition
     */
    public void setAttributeProperties(String className, AttributeInfo newAttributeDefinition) throws ServerSideException;

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
    public void deleteAttribute(long classId, String attributeName) throws ServerSideException;

    /**
     * Creates a new category
     * @param categoryDefinition
     * @return CategoryId
     */
    public long createCategory(CategoryInfo categoryDefinition) throws ServerSideException;

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
    public CategoryInfo getCategory(long categoryId) throws ServerSideException;

    /**
     * Changes a category definition
     * @param categoryDefinition
     */
    public void setCategoryProperties (CategoryInfo categoryDefinition) throws ServerSideException;
        
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
     * Get the upstream containment hierarchy for a given class, unlike getPossibleChildren (which will give you the
     * downstream hierarchy).
     * @param className Class name
     * @throws ServerSideException
     */
    public List<ClassInfoLight> getUpstreamContainmentHierarchy(String className, boolean recursive) throws ServerSideException;
    /**
     * Adds to a given class a list of possible children classes whose instances can be contained
     *
     * @param parentClassId Id of the class whose instances can contain the instances of the next param
     * @param _possibleChildren ids of the candidates to be contained
     */
    public void addPossibleChildren(long parentClassId, long[] possibleChildren) throws ServerSideException;
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
    public void removePossibleChildren(long parentClassId, long[] childrenToBeRemoved) throws ServerSideException;


    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Business methods. Click on the + sign on the left to edit the code.">
    public RemoteObjectLight[] getObjectChildren(long oid, long objectClassId, int maxResults) throws ServerSideException;
    public RemoteObjectLight[] getObjectChildren(String objectClassName, long oid, int maxResults) throws ServerSideException;

    public RemoteObject[] getChildrenOfClass(long parentOid, String parentClass,String classToFilter, int maxResults) throws ServerSideException;
    public RemoteObjectLight[] getChildrenOfClassLight(long parentOid, String parentClass,String classToFilter, int maxResults) throws ServerSideException;

    public RemoteObject getObject(String objectClass, long oid) throws ServerSideException;

    public RemoteObjectLight getObjectLight(String objectClass, long oid) throws ServerSideException;
    
    public RemoteObject getParent(String objectClass, long oid) throws ServerSideException;
    public RemoteObject getParentOfClass(String objectClass, long oid, String parentClass) throws ServerSideException;

    public String[] getSpecialAttribute(String objectClass, long objectId, String attributeName) throws ServerSideException;

    public void updateObject(String className, long oid, String[] attributeNames, String[][] attributeValues) throws ServerSideException;

    public long createObject(String className, String parentClassName, long parentOid, String[] attributeNames, String[][] attributeValues, long templateId) throws ServerSideException;

    public long createListTypeItem(String className, String name, String displayName) throws ServerSideException;

    public void deleteListTypeItem(String className, long oid, boolean releaseRelationships) throws ServerSideException;

    public RemoteObjectLight[] getListTypeItems(String className) throws ServerSideException;

    public ClassInfoLight[] getInstanceableListTypes() throws ServerSideException;

    public void deleteObjects(String classNames[], long[] oids, boolean releaseRelationships) throws ServerSideException;

    public void moveObjects(String targetClass, long targetOid, String[] objectClasses, long[] objectOids) throws ServerSideException;

    public long[] copyObjects(String targetClass, long targetOid, String[] templateClasses, long[] templateOids, boolean recursive) throws ServerSideException;

    /**
     * Models
     */
    //Physical connections
    public long createPhysicalConnection(String aObjectClass, long aObjectId, String bObjectClass, long bObjectId, String parentClass, long parentId, String[] attributeNames, String[][] attributeValues, String connectionClass) throws ServerSideException;
    public void deletePhysicalConnection(String objectClass, long objectId) throws ServerSideException;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Application methods. Click on the + sign on the left to edit the code.">
    /**
     * Sets de user properties
     * @param userName user name
     * @param password user password
     * @param firstName user's first name
     * @param lastName user's last name
     * @param privileges user's individual privileges
     * @param groups groups to which the user belongs
     * @throws ServerSideException
     */
    public void setUserProperties(long oid, String userName, String password, String firstName,
            String lastName, boolean enabled, int[] privileges, long[] groups)
            throws ServerSideException;


    /**
     * Creates a group
     * @param name group's name
     * @param description group's name
     * @param creationDate group's creation date
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public long createGroup(String groupName, String description,
            int[] privileges, long[] users) throws ServerSideException;

    /**
     * Get all users
     * @return an array with all the users info
     * @throws ServerSideException 
     */
    public UserInfo[] getUsers() throws ServerSideException;

    /**
     * Get All Groups
     * @return an array with all the groups info
     * @throws ServerSideException
     */
    public UserGroupInfo[] getGroups() throws ServerSideException;

    /**
     * Creates a new user
     * @return The newly created user
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public long createUser(String userName, String password, String firstName,
            String lastName, boolean enabled, int[] privileges, long[] groups)
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
    public void setGroupProperties(long oid, String groupName, String description,
            int[] privileges, long[] users)throws ServerSideException;

     /**
     * Removes a list of users
     * @param oids
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public void deleteUsers(long[] oids)throws ServerSideException;

    /**
     * Removes a list of groups
     * @param oids
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     */
    public void deleteGroups(long[] oids)
            throws ServerSideException;

    /**
     *
     * @param oid
     * @param objectClass
     * @param viewType
     * @return
     * @throws ServerSideException
     */
    public ViewInfo getObjectRelatedView(long oid, String objectClass, long viewId) throws ServerSideException;

    public ViewInfoLight[] getObjectRelatedViews(long oid, String objectClass, int viewType, int limit) throws ServerSideException;

    public ViewInfo getGeneralView(long viewId) throws ServerSideException;

    public ViewInfoLight[] getGeneralViews(int viewType, int limit) throws ServerSideException;

    public long createObjectRelatedView(long objectId, String objectClass, String name, String description, int viewType, byte[] structure, byte[] background) throws ServerSideException;

    public long createGeneralView(int viewType, String name, String description, byte[] structure, byte[] background) throws ServerSideException;

    public void updateObjectRelatedView(long objectOid, String objectClass, long viewId, String viewName, String viewDescription, byte[] structure, byte[] background) throws ServerSideException;

    public void updateGeneralView(long viewId, String viewName, String viewDescription, byte[] structure, byte[] background) throws ServerSideException;

    public void deleteGeneralView(long [] oids) throws ServerSideException;

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
    public long createQuery(String queryName, long ownerOid, byte[] queryStructure,
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
    public void saveQuery(long queryOid, String queryName,
            long ownerOid, byte[] queryStructure, String description) throws ServerSideException;

    /**
     * Deletes the query load in the graphical query builder
     * @param queryOid
     * @throws ServerSideException
     */
    public void deleteQuery(long queryOid) throws ServerSideException;

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
    public RemoteQuery getQuery(long queryOid) throws ServerSideException;

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
     * @param ipAddress IP Address where th request is coming from
     * @param sessionId Session token
     * @return the active session if any
     * @throws NotAuthorizedException if the session id doesn't belong to an active session
     */
    public Session validateCall(String methodName, String ipAddress, String sessionId) throws NotAuthorizedException;
    //Pools
    public long createPool(String name, String description, String instancesOfClass, long owner) throws ServerSideException;
    
    public long createPoolItem(long poolId, String className, String[] attributeNames, String[][] attributeValues, long templateId) throws ServerSideException;

    public void deletePools(long[] ids) throws ServerSideException;
    
    public RemoteObjectLight[] getPools(int limit) throws ServerSideException;
    
    public RemoteObjectLight[] getPoolItems(long poolId, int limit) throws ServerSideException;
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Sync/Load data methods. Click on the + sign on the left to edit the code.">
    /**
     * Loads data from a csv file
     * @param choosenFile the csv file as a byte array
     * @return 
     */
    public String loadDataFromFile(byte[] choosenFile, long userId) throws ServerSideException;
    /**
     * Returns a file with the wrong lines of the load file
     * @param fileName
     * @return
     * @throws ServerSideException 
     */
    public byte[] downloadLog(String fileName) throws ServerSideException;
    
    /**
     * Returns a file with the errors in the lines of the load file
     * @param fileName
     * @return
     * @throws ServerSideException 
     */
    public byte[] downloadErrors(String fileName) throws ServerSideException;
    // </editor-fold>
}

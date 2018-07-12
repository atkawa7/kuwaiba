/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.persistence.business;

import com.neotropic.kuwaiba.modules.reporting.model.RemoteReport;
import com.neotropic.kuwaiba.modules.reporting.model.RemoteReportLight;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.persistence.application.FileObject;
import org.kuwaiba.apis.persistence.application.FileObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.util.ChangeDescriptor;
import org.kuwaiba.apis.persistence.util.StringPair;

/**
 * This is the entity in charge of manipulating business objects
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface BusinessEntityManager {
    /**
     * Sets the global configuration options 
     * @param configuration The set of configuration variables
     */
    public void setConfiguration(Properties configuration);
    /**
     * Creates a new inventory object
     * @param className Name of the class which this object will be instantiated from
     * @param parentClassName Parent object class name. If null, the parent will be the DummyRoot node
     * @param parentOid Parent's oid. If -1, the parent will be the DummyRoot node
     * @param attributes Attributes to be set by default in the new object. It's a HashMap where the keys are the attribute names and the values, the values for such attributes.
     * Note that binary type attributes can't be set here.
     * @param template Template id to be used to create the current object. Template values can be
     * overridden if "attributeValues" is not empty
     * @return The object's id
     * @throws MetadataObjectNotFoundException Thrown if the object's class can't be found
     * @throws BusinessObjectNotFoundException Thrown if the parent id is not found
     * @throws OperationNotPermittedException If there's a business constraint that doesn't allow to create the object.
     * @throws InvalidArgumentException If any of the attribute values has an invalid value or format.
     * @throws ApplicationObjectNotFoundException If the specified template could not be found
     */
    public long createObject(String className, String parentClassName, long parentOid,
            HashMap<String, String> attributes,long template)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, 
                OperationNotPermittedException, ApplicationObjectNotFoundException;
    
    /**
     * Creates an object
     * @param className Class the object will be instance of
     * @param parentClassName Class of the parent the object will be instance of. Use <b>root</b> for the navigation tree
     * @param criteria Criteria to search for the parent. This is a string with two parts: One is the name of the attribute and the other its value, both separated by a fixed colon <b>:</b>. Example: name:Colombia
     * @param attributes Dictionary with the names and the values of the attributes to be set.
     * @param template Reserved for future uses
     * @return The id of the new object.
     * @throws MetadataObjectNotFoundException Thrown if the object's class can't be found
     * @throws BusinessObjectNotFoundException Thrown if the parent id is not found
     * @throws InvalidArgumentException If any of the attribute values has an invalid value or format.
     * @throws OperationNotPermittedException If there's a business constraint that doesn't allow to create the object.
     * @throws ApplicationObjectNotFoundException If the specified template could not be found.
     */
    public long createObject(String className, String parentClassName, String criteria, HashMap<String,String> attributes, long template)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, ApplicationObjectNotFoundException;
    /**
     * Creates a new inventory object for a domain specific model (where the standard containment rules don't apply)
     * @param className Name of the class which this object will be instantiated from
     * @param parentClassName Parent object class name
     * @param parentOid Parent's oid
     * @param attributes Attributes to be set by default in the new object. It's a HashMap where the keys are the attribute names and the values, the values for such attributes.
     * Note that binary type attributes can't be set here.
     * @param template Template id to be used to create the current object. Template values can be
     * overridden if "attributeValues" is not empty
     * @return The id of the new object.
     * @throws MetadataObjectNotFoundException Thrown if the object's class can't be found
     * @throws BusinessObjectNotFoundException Thrown if the parent id is not found
     * @throws OperationNotPermittedException If the update can't be performed due to a format issue
     * @throws InvalidArgumentException If any of the attribute values has an invalid value or format.
     * @throws ApplicationObjectNotFoundException If the specified template could not be found.
     */
    public long createSpecialObject(String className, String parentClassName, long parentOid,
            HashMap<String,String> attributes,long template)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, ApplicationObjectNotFoundException;
    
    /**
     * Creates an object inside a pool
     * @param poolId Parent pool id
     * @param className Class this object is going to be instance of
     * @param attributeNames Attributes to be set
     * @param attributeValues Attribute values to be set
     * @param templateId Template used to create the object, if applicable. -1 for none
     * @throws ApplicationObjectNotFoundException If the parent pool can't be found
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @return the id of the newly created object
     * @throws ArraySizeMismatchException If attributeNames and attributeValues have different sizes.
     * @throws MetadataObjectNotFoundException If the class name could not be found 
     */
    public long createPoolItem(long poolId, String className, String[] attributeNames, String[] attributeValues, long templateId) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, ArraySizeMismatchException, MetadataObjectNotFoundException;
    /**
     * Creates multiple objects using a given name pattern
     * @param className The class name for the new objects
     * @param parentClassName The parent class name for the new objects
     * @param parentOid The object id of the parent
     * @param numberOfObjects Number of objects to be created
     * @param namePattern A pattern to create the names for the new objects
     * @return An arrays of ids for the new objects
     * @throws MetadataObjectNotFoundException If the className or the parentClassName can not be found.
     * @throws BusinessObjectNotFoundException If the parent node can not be found.
     * @throws InvalidArgumentException If the given name pattern not match with the regular expression to build the new object name.
     * @throws OperationNotPermittedException If the className is not a possible children of parentClassName.
     *                                        If the className is not in design or are abstract.
     *                                        If the className is not an InventoryObject.
     */
    public long [] createBulkObjects(String className, String parentClassName, long parentOid, int numberOfObjects, String namePattern) 
        throws MetadataObjectNotFoundException, OperationNotPermittedException, BusinessObjectNotFoundException, InvalidArgumentException;
    /**
     * Creates multiple special objects using a given name pattern
     * @param className The class name for the new special objects
     * @param parentClassName The parent class name for the new special objects
     * @param parentId The object id of the parent
     * @param numberOfSpecialObjects Number of special objects to be created
     * @param namePattern A pattern to create the names for the new special objects
     * @return An array of ids for the new special objects
     * @throws MetadataObjectNotFoundException If the className or the parentClassName can not be found.
     * @throws BusinessObjectNotFoundException If the parent node can not be found.
     * @throws InvalidArgumentException If the given name pattern not match with the regular expression to build the new object name.
     * @throws OperationNotPermittedException If the className is not a possible special children of parentClassName.
     *                                        If the className is not in design or are abstract.
     *                                        If the className is not an InventoryObject.
     */
    public long[] createBulkSpecialObjects(String className, String parentClassName, long parentId, int numberOfSpecialObjects, String namePattern) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException;
    
    /**
     * Gets the detailed information about an object
     * @param className Object class name
     * @param oid Object's oid
     * @return A detailed representation of the requested object
     * @throws MetadataObjectNotFoundException If the className class can't be found
     * @throws BusinessObjectNotFoundException If the requested object can't be found
     * @throws InvalidArgumentException If the object id can not be found.
     */
    public BusinessObject getObject(String className, long oid)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Gets the detailed information about an object using the id
     * @param oid Object's oid
     * @return A detailed representation of the requested object
     * @throws MetadataObjectNotFoundException If the className class can't be found
     * @throws BusinessObjectNotFoundException If the requested object can't be found
     * @throws InvalidArgumentException If the database object could not be properly mapped into a serializable java object.
     */
    public BusinessObject getObject(long oid) 
        throws InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException;

    /**
     * Gets the special children of a given object
     * @param objectClass Object class
     * @param objectId object id
     * @return The list of special children.
     * @throws MetadataObjectNotFoundException If the class could not be found
     * @throws BusinessObjectNotFoundException If the object could not be found.
     */
    public List<BusinessObjectLight> getObjectSpecialChildren(String objectClass, long objectId)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException;
    
    /**
     * Gets the simplified information about an object
     * @param className Object class name
     * @param oid Object's oid
     * @return A detailed representation of the requested object
     * @throws MetadataObjectNotFoundException If the className class can't be found
     * @throws BusinessObjectNotFoundException If the requested object can't be found
     */
    public BusinessObjectLight getObjectLight(String className, long oid)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException;
    
    /**
     * Retrieves a list of instances of a given class
     * @param className Class name. This method only works with non-abstract classes for now
     * @param filterName The attribute to be used as filter. This will work <b>only</b> with String-type attributes. Example: serialNumber
     * @param filterValue The value to be use to match the instances. Example "Serial-12345"
     * @return The list of instances that meet the filterName/filterValue criteria
     * @throws MetadataObjectNotFoundException If the class provided could not be found
     */
    public List<BusinessObjectLight> getObjectsWithFilterLight (String className, 
            String filterName, String filterValue) throws MetadataObjectNotFoundException;
    
    /**
     * Same as <code>getObjectsWithFilterLight</code>, but returns the full information about the objects involved
     * @param className Class name. This method only works with non-abstract classes for now
     * @param filterName The attribute to be used as filter. This will work <b>only</b> with String-type attributes. Example: serialNumber
     * @param filterValue The value to be use to match the instances. Example "Serial-12345"
     * @return The list of instances that meet the filterName/filterValue criteria
     * @throws MetadataObjectNotFoundException If the class provided could not be found
     * @throws InvalidArgumentException If it's not possible to construct the RemoteBusinessObjects from the information in the database
     */
    public List<BusinessObject> getObjectsWithFilter (String className, 
            String filterName, String filterValue) throws MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Utility method that returns the value of an attribute of a given object as a string. In date-type attributes, it will return 
     * the formated dated, while in list types, it will return the name of the linked element
     * @param objectClass The class of the object
     * @param objectId The id of the object
     * @param attributeName The attribute whose value will be retrieved
     * @return The value of the requested attribute. Null values are possible
     * @throws MetadataObjectNotFoundException If the class of the object could not be found
     * @throws BusinessObjectNotFoundException If the object itself could not be found
     * @throws InvalidArgumentException Check with the data model integrity, because this would mean that a the type of the attribute should be a list type, but it's not
     * @throws ApplicationObjectNotFoundException Check with the data model integrity, because this would mean that a list type item related to the object is not an instance of the right list type class
     */
    public String getAttributeValueAsString (String objectClass, long objectId, String attributeName) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException;
    
    /**
     * Gets the common parent between an a object and b object
     * @param aObjectClass Object a class name
     * @param aOid Object a id
     * @param bObjectClass Object b class name
     * @param bOid Object a id
     * @return The common parent
     * @throws BusinessObjectNotFoundException If the requested object can't be found
     * @throws MetadataObjectNotFoundException If any of the class nodes involved is malformed
     * @throws InvalidArgumentException If the database object could not be properly mapped into a serializable java object.
     */
    public BusinessObjectLight getCommonParent(String aObjectClass, long aOid, String bObjectClass, long bOid)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * Gets the parent of a given object in the containment hierarchy
     * @param objectClass Object class
     * @param oid Object id
     * @return The immediate parent. Null if the parent is null. A dummy object with id -1 if the parent is DummyRoot
     * @throws BusinessObjectNotFoundException If the requested object can't be found
     * @throws MetadataObjectNotFoundException If any of the class nodes involved is malformed
     * @throws InvalidArgumentException If the database object could not be properly mapped into a serializable java object.
     */
    public BusinessObjectLight getParent(String objectClass, long oid)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Retrieves recursively the list of parents of an object in the containment hierarchy
     * @param oid Object id 
     * @param objectClassName Object class
     * @return The list of parents
     * @throws BusinessObjectNotFoundException If the object does not exist
     * @throws MetadataObjectNotFoundException if the class can not be found
     */
    public List<BusinessObjectLight> getParents(String objectClassName, long oid)
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException;
    
    /**
     * Gets the list of parents (according to the special and standard containment hierarchy) until it finds an instance of class 
     * objectToMatchClassName (for example "give me the parents of this port until you find the nearest rack")
     * @param objectClassName Class of the object to get the parents from
     * @param oid Id of the object to get the parents from
     * @param objectToMatchClassName Class of the object that will limit the search. It can be a superclass, if you want to match many classes at once
     * @return The list of parents until an instance of objectToMatchClassName is found. If no instance of that class is found, all parents until the Dummy Root will be returned
     * @throws BusinessObjectNotFoundException If the object to evaluate can not be found
     * @throws MetadataObjectNotFoundException If any of the classes provided could not be found
     * @throws ApplicationObjectNotFoundException If the object provided is not in the standard containment hierarchy
     */
    public List<BusinessObjectLight> getParentsUntilFirstOfClass(String objectClassName, long oid, String objectToMatchClassName)
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException;

    /**
     * Gets the first occurrence of a parent with a given class (according to the special and standard containment hierarchy)
     * (for example "give me the parent of this port until you find the nearest rack")
     * @param objectClassName Class of the object to get the parent from
     * @param oid Id of the object to get the parent from
     * @param objectToMatchClassName Class of the object that will limit the search. It can be a superclass, if you want to match many classes at once
     * @return The the first occurrence of a parent with a given class. If no instance of that class is found, the child of Dummy Root related in this hierarchy will be returned
     * @throws BusinessObjectNotFoundException If the object to evaluate can not be found
     * @throws MetadataObjectNotFoundException If any of the classes provided could not be found
     * @throws ApplicationObjectNotFoundException If the object provided is not in the standard containment hierarchy
     */
    public BusinessObjectLight getFirstParentOfClass(String objectClassName, long oid, String objectToMatchClassName)
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException;

    /**
     * Gets the first parent of an object which matches the given class in the containment hierarchy
     * @param objectClass Object class
     * @param oid Object id
     * @param parentClass Parent class
     * @return The nearest parent of the provided class. Null if none found.
     * @throws BusinessObjectNotFoundException If any of the requested objects can't be found
     * @throws MetadataObjectNotFoundException If any of the class nodes involved is malformed
     * @throws InvalidArgumentException If the database object could not be properly mapped into a serializable java object.
     */
    public BusinessObject getParentOfClass(String objectClass, long oid, String parentClass)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Deletes a set of objects
     * @param releaseRelationships If all the relationships should be release upon deleting the objects. If false, an OperationNotPermittedException  will be raised if the object has incoming relationships.
     * @param  objects a hashmap where the class name is the key and the value is a list of long containing the ids of the objects to be deleted that are instance of the key class
     * @throws BusinessObjectNotFoundException If the requested object can't be found
     * @throws MetadataObjectNotFoundException If the requested object class can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked or it has relationships and releaseRelationships is false
     * @throws InvalidArgumentException If it was not possible to release the possible unique attributes
     */
    public void deleteObjects(HashMap<String, List<Long>> objects, boolean releaseRelationships)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException;

    /**
     * Deletes a single object
     * @param className Object's class name
     * @param oid Objects oid
     * @param releaseRelationships Release relationships automatically. If set to false, it will fail if the object already has incoming relationships
     * @throws BusinessObjectNotFoundException If the object couldn't be found
     * @throws MetadataObjectNotFoundException If the class could not be found
     * @throws OperationNotPermittedException If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     */
    public void deleteObject(String className, long oid, boolean releaseRelationships)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException;
    
    /**
     * Updates an object attributes. Note that you can't set binary attributes through this
     * method. Use setBinaryAttributes instead.
     * @param className Object class name
     * @param oid Object's oid
     * @param attributes The attributes to be updated (the key is the attribute name, 
     * the value is and array with the value -or values in case of MANY TO MANY list type attributes-)
     * @return The summary of the changes that were made
     * @throws MetadataObjectNotFoundException If the object class can't be found
     * @throws BusinessObjectNotFoundException If the object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * @throws InvalidArgumentException If any of the names provided does not exist or can't be set using this method or of the value of any of the attributes can not be mapped correctly.
     */
    public ChangeDescriptor updateObject(String className, long oid, HashMap<String, String> attributes)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException;
    /**
     * Updates an object binary attributes.
     * @param className Object's class name
     * @param oid Object's oid
     * @param attributeNames The attributes to be updated
     * @param attributeValues The attribute values
     * @return Success or failure
     * @throws MetadataObjectNotFoundException If the object class can't be found
     * @throws BusinessObjectNotFoundException If the object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * @throws ArraySizeMismatchException If the arrays attributeNames and attributeValues have different lengths
     */
    public boolean setBinaryAttributes(String className, long oid, List<String> attributeNames, List<byte[]> attributeValues)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, ArraySizeMismatchException;
    
    /**
     * Move a list of objects to a new parent: this methods ignores those who can't be moved and raises
     * an OperationNotPermittedException, however, it will move those which can be moved
     * @param objects Map using the object class name as keys and the respective objects oids as values
     * @param targetClassName Parent's class name
     * @param targetOid Parent's oid
     * @throws MetadataObjectNotFoundException If the object's or new parent's class can't be found
     * @throws BusinessObjectNotFoundException If the object or its new parent can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     */
    public void moveObjectsToPool(String targetClassName, long targetOid, HashMap<String, long[]> objects)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException;
    /**
     * Move a list of objects to a new parent: this methods ignores those who can't be moved and raises
     * an OperationNotPermittedException, however, it will move those which can be moved
     * @param objects Map using the object class name as keys and the respective objects oids as values
     * @param targetClassName Parent's class name
     * @param targetOid Parent's oid
     * @throws MetadataObjectNotFoundException If the object's or new parent's class can't be found
     * @throws BusinessObjectNotFoundException If the object or its new parent can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     */
    public void moveObjects(String targetClassName, long targetOid, HashMap<String,long[]> objects)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException;

     /**
     * Move a list of objects to a new parent(taking into account the special
     * hierarchy containment): this methods ignores those who can't be moved and raises an 
     * OperationNotPermittedException, however, it will move those which can be moved
     * @param objects Map using the object class name as keys and the respective objects oids as values
     * @param targetClassName Parent's class name
     * @param targetOid Parent's oid
     * @throws MetadataObjectNotFoundException If the object's or new parent's class can't be found
     * @throws BusinessObjectNotFoundException If the object or its new parent can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     */
    public void moveSpecialObjects(String targetClassName, long targetOid, HashMap<String,long[]> objects)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException;
    /**
     * Move a pool item from a pool to another pool
     * @param poolId The id of the pool node
     * @param poolItemClassName The class name for the pool item
     * @param poolItemId The id for the pool item
     * @throws ApplicationObjectNotFoundException If the pool node can not be found
     * @throws InvalidArgumentException If the pool item can not be move to the selected pool
     * @throws BusinessObjectNotFoundException If the pool item can not be found
     * @throws MetadataObjectNotFoundException If the pool item class name can no be found
     */
    public void movePoolItem(long poolId, String poolItemClassName, long poolItemId) throws 
        ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, 
        MetadataObjectNotFoundException;
    /**
     * Copy a set of objects
     * @param objects Hashmap with the objects class names as keys and their oids as values
     * @param targetClassName Target parent's class name
     * @param targetOid Target parent's oid
     * @param recursive If this operation should also copy the children objects recursively
     * @return A list containing the newly created object ids
     * @throws MetadataObjectNotFoundException If any of the provided classes couldn't be found
     * @throws BusinessObjectNotFoundException If any of the template objects couldn't be found
     * @throws OperationNotPermittedException If the target parent can't contain any of the new instances
     */
    public long[] copyObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects, boolean recursive)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException;

    /**
     * Copy a set of special objects (this is used to copy objects when they are containment are set in the special containment hierarchy)
     * use case: to move physical links into a wire Container
     * @param objects Hashmap with the objects class names as keys and their oids as values
     * @param targetClassName Target parent's class name
     * @param targetOid Target parent's oid
     * @param recursive If this operation should also copy the children objects recursively
     * @return A list containing the newly created object ids
     * @throws MetadataObjectNotFoundException If any of the provided classes couldn't be found
     * @throws BusinessObjectNotFoundException If any of the template objects couldn't be found
     * @throws OperationNotPermittedException If the target parent can't contain any of the new instances
     */
    public long[] copySpecialObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects, boolean recursive)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException;
    /**
     * Copy a pool item from a pool to another pool
     * @param poolId The id of the pool node
     * @param poolItemClassName The class name for the pool item
     * @param poolItemId The id for the pool item
     * @param recursive If this operation should also copy the children objects recursively
     * @return The newly created object id
     * @throws ApplicationObjectNotFoundException If the pool node can not be found
     * @throws InvalidArgumentException If the pool item can not be move to the selected pool
     * @throws BusinessObjectNotFoundException If the pool item can not be found
     * @throws MetadataObjectNotFoundException If the pool item class name can no be found
     */
    public long copyPoolItem(long poolId, String poolItemClassName, long poolItemId, boolean recursive) throws 
        ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, 
        MetadataObjectNotFoundException;
    /**
     * Gets the children of a given object
     * @param className Object's class name
     * @param oid Object's oid
     * @param maxResults max number of children to be returned, -1 to return all
     * @return The list of children
     * @throws MetadataObjectNotFoundException If the object's can't be found
     * @throws BusinessObjectNotFoundException If the object or its new parent can't be found
     */
    public List<BusinessObjectLight> getObjectChildren(String className, long oid, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException;
    
    /**
     * Gets the children of a given object, providing the class and object id.
     * @param classId The id of the class the object is instance of
     * @param oid The oid of the object
     * @param maxResults The max number of results to be retrieved. Use 0 to retrieve all
     * @return The list of children.
     * @throws BusinessObjectNotFoundException If the object could not be found.
     * @throws MetadataObjectNotFoundException If the class could not be found.
     */
    public List<BusinessObjectLight> getObjectChildren(long classId, long oid, int maxResults)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException;
    
    /**
     * Gets the direct children of a given object of a given class.
     * @param parentOid parent id
     * @param parentClass Parent class
     * @param classToFilter Class to be match against
     * @param maxResults max number of results. 0 to get all
     * @return A list of children of parentid/parentClass instance that are instances of classToFilter.
     * @throws MetadataObjectNotFoundException If any of the classes can not be found
     * @throws BusinessObjectNotFoundException If parent object can not be found
     * @throws InvalidArgumentException If the database objects can not be correctly mapped into serializable Java objects.
     */
    public List<BusinessObject> getChildrenOfClass(long parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Returns the special children of a given object as RemoteBusinessObjectLight instances. This method is not recursive.
     * @param parentOid The id of the parent object
     * @param parentClass The class name of the parent object
     * @param classToFilter The superclass/class to be used to filter the results. You can also use abstract superclasses.
     * @param maxResults The max number of results to fetch. Use -1 to retrieve all
     * @return The list of special children of the given object, filtered using classToFilter
     * @throws MetadataObjectNotFoundException If the parent class name provided could not be found
     * @throws BusinessObjectNotFoundException If the parent object could not be found
     */
    public List<BusinessObjectLight> getSpecialChildrenOfClassLight(long parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException;
    
    /**
     * Gets all class and abstract class children of a given class to filter in 
     * a hierarchy with root in the given parent.
     * Use case: used in some class level and inventory level reports script 
     * @param parentOid Object id of the root parent of the hierarchy
     * @param parentClass Class name of the root parent of the hierarchy
     * @param classToFilter Class name of the expected children
     * @param maxResults Maximum number of results, -1 no limit
     * @return The list of object instance of the given class to filter
     * @throws MetadataObjectNotFoundException If the parent class is not found
     * @throws BusinessObjectNotFoundException If the parent is not found
     */
    public List<BusinessObjectLight> getChildrenOfClassLightRecursive(long parentOid, String parentClass, String classToFilter, int maxResults) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException;
    /**
     * Gets all class and abstract class special children of a given class to filter 
     * in a hierarchy with root in the given parent.
     * Use case: used in some class level and inventory level reports script 
     * @param parentOid Object id of the root parent of the hierarchy
     * @param parentClass Class name of the root parent of the hierarchy
     * @param classToFilter Class name of the expected children
     * @param maxResults Maximum number of results, -1 no limit
     * @return The list of object instance of the given class to filter
     * @throws MetadataObjectNotFoundException If the parent class is not found
     * @throws BusinessObjectNotFoundException If the parent is not found
     */
    public List<BusinessObjectLight> getSpecialChildrenOfClassLightRecursive(long parentOid, String parentClass, String classToFilter, int maxResults) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException;
            
    /**
     * Same as getChildrenOfClass, but returns only the light version of the objects
     * @param parentOid parent id
     * @param parentClass Parent class
     * @param classToFilter Class to be match against
     * @param maxResults max number of results. 0 to get all
     * @return A list of children of parentid/parentClass instance, instances of classToFilter
     * @throws MetadataObjectNotFoundException If any of the classes can not be found
     * @throws BusinessObjectNotFoundException If parent object can not be found
     */
    public List<BusinessObjectLight> getChildrenOfClassLight(long parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException;

    /**
     * Gets the siblings of a given object in the containment hierarchy
     * @param className Object class
     * @param oid Object oid
     * @param maxResults Max number of results to be returned
     * @return List of siblings
     * @throws MetadataObjectNotFoundException If the class does not exist
     * @throws BusinessObjectNotFoundException If the object does not exist
     */
    public List<BusinessObjectLight> getSiblings(String className, long oid, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException;
    
    /**
     * Recursively gets all the light instances of given class
     * @param className Class name. It mist be a subclass of InventoryObject
     * @param maxResults Max number of results. 0 to get all
     * @return a list of instances 
     * @throws MetadataObjectNotFoundException if the class can not be found
     * @throws InvalidArgumentException If the class is not subclass of InventoryObject
     */
    public List<BusinessObjectLight> getObjectsOfClassLight(String className, int maxResults)
            throws MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Recursively gets all the instances of given class
     * @param className Class name. It mist be a subclass of InventoryObject
     * @param maxResults Max number of results. 0 to get all
     * @return a list of instances 
     * @throws MetadataObjectNotFoundException if the class can not be found
     * @throws InvalidArgumentException If the class is not subclass of InventoryObject
     */
    public List<BusinessObject> getObjectsOfClass(String className, int maxResults)
            throws MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Creates a relationship between two elements and labels it. Use with extreme care, since it can create arbitrary relationships
     * @param aObjectClass a side object class
     * @param aObjectId a side object id
     * @param bObjectClass b side object class
     * @param bObjectId b side object id
     * @param name Name to label the new relationship
     * @param unique If there could be only one relationship between both elements with that name
     * @throws BusinessObjectNotFoundException If any of the objects can't be found
     * @throws OperationNotPermittedException if any of the objects involved can't be connected (i.e. if it's not an inventory object)
     * @throws MetadataObjectNotFoundException if any of the classes provided can not be found
     */
    public void createSpecialRelationship(String aObjectClass, long aObjectId, String bObjectClass, long bObjectId, String name, boolean unique)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException;
    
    /**
     * This method creates a special relationship  with a set of property values
     * @param aObjectClass The class of the first object to related
     * @param aObjectId The id of the first object to related
     * @param bObjectClass The class of the second object to related
     * @param bObjectId The id of the first object to related
     * @param name The name of the relationship
     * @param unique If there could be only one relationship between both elements with that name
     * @param properties A hash with the set of properties and their respective values
     * @throws BusinessObjectNotFoundException If any of the objects can not be found
     * @throws OperationNotPermittedException If, due to a business rule, the objects can not be related
     * @throws MetadataObjectNotFoundException If any of the classes specified does not exist
     */
    public void createSpecialRelationship(String aObjectClass, long aObjectId, String bObjectClass, 
            long bObjectId, String name, boolean unique, HashMap<String, Object> properties) throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException;
    
    /**
     * Release all special relationships with a given name
     * @param objectClass Object class
     * @param objectId Object id
     * @param otherObjectId The object we want to be released from. -1 To all objects related with relationships with that name
     * @param relationshipName Relationship name
     * @throws BusinessObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException  If the class can not be found
     */
    public void releaseSpecialRelationship(String objectClass, long objectId, long otherObjectId, String relationshipName)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException;
    
    /**
     * Release all special relationships with a given name whose target object id matches with the one provided
     * @param objectClass Object class
     * @param objectId Object id
     * @param relationshipName Relationship name
     * @param targetId Id of the object at the end of the relationship
     * @throws BusinessObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException  If the class can not be found
     */
    public void releaseSpecialRelationship(String objectClass, long objectId, String relationshipName, long targetId)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException;

    /**
     * Gets the value of a special attribute. A special attribute is one belonging to a business domain specific attribute
     * (usually a model. Domain specific attribute information is not filed under the standard metadata but a special one. Implementations may vary)
     * @param objectClass object's class
     * @param objectId object's id
     * @param specialAttributeName Special attribute name
     * @return A list of objects related to the object through a special relationship
     * @throws BusinessObjectNotFoundException if the object can not be found
     * @throws MetadataObjectNotFoundException if either the object class or the attribute can not be found
     */    
    public List<BusinessObjectLight> getSpecialAttribute(String objectClass, long objectId, String specialAttributeName) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException;
    
    /**
     * This method will extract the object at the other side of the special relationship and all the properties of the relationship itself
     * @param objectClass The class of the object whose special attribute will be retrieved from
     * @param objectId The object's id
     * @param specialAttributeName The name of the special attribute
     * @return The list of elements related with such relationship plus the properties of theis relationships
     * @throws BusinessObjectNotFoundException If the object could not be found
     * @throws MetadataObjectNotFoundException If the object class could not be found
     */
    public List<AnnotatedBusinessObjectLight> getAnnotatedSpecialAttribute(String objectClass, long objectId, String specialAttributeName) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException;
    
    /**
     * Returns all the special relationships of a given object as a hashmap whose keys are
     * the names of the relationships and the values the list of related objects
     * @param className Object class
     * @param objectId Object Id
     * @return The hash map with the existing special relationships and the associated objects
     * @throws MetadataObjectNotFoundException If the class provided does not exist
     * @throws BusinessObjectNotFoundException if the object does not exist
     */
    public HashMap<String,List<BusinessObjectLight>> getSpecialAttributes (String className, long objectId) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException;
    
    /**
     * Checks if an object has a given number of standard relationships with another object
     * @param objectClass Object class
     * @param objectId Object id
     * @param relationshipName Relationship name
     * @param numberOfRelationships Number of relationships
     * @return True if the object has numberOfRelationships relationships with another object
     * @throws BusinessObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException  If objectClass does not exist
     */
    public boolean hasRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException;

    /**
     * Releases all the relationships with the given names associated to the object provided. If no relationships with such names exist, the method 
     * will do nothing. <b>Use this method with extreme care, you can seriously affect the relational integrity of the system</b>
     * @param objectClass The class of the target object
     * @param objectId The id of the target object
     * @param relationshipsToRelease An array with the relationships to be released
     * @throws MetadataObjectNotFoundException If the class provided does not exist
     * @throws BusinessObjectNotFoundException If the object could not be found
     * @throws InvalidArgumentException If any of the relationships is now allowed according to the defined data model
     */
    public void releaseRelationships(String objectClass, long objectId, List<String> relationshipsToRelease) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Checks if an object has a given number of special relationships with another object
     * @param objectClass Object class
     * @param objectId Object id
     * @param relationshipName Relationship name
     * @param numberOfRelationships Number of relationships
     * @return True if the object has numberOfRelationships relationships with another object
     * @throws BusinessObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException  if objectClass does not exist
     */
    public boolean hasSpecialRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException;
    
    /**
     * Finds all possible routes between two given inventory objects
     * @param objectAClassName Inventory object A class name
     * @param objectAId  Inventory object A id
     * @param objectBClassName  Inventory object B class name
     * @param objectBId  Inventory object B id
     * @param relationshipName The name of the relationship used to navigate through nodes and find the route
     * @return A list of the routes, including only the nodes as RemoteBusinessObjectLights
     */
    public List<BusinessObjectLightList> findRoutesThroughSpecialRelationships (String objectAClassName, long objectAId, String objectBClassName, long objectBId, String relationshipName);
    
    /**
     * Finds the physical path from one port to another
     * @param objectClass The source port class.
     * @param objectId The source port id.
     * @return A list of objects that make part of the physical trace.
     * @throws MetadataObjectNotFoundException can't f
     * @throws BusinessObjectNotFoundException 
     * @throws ApplicationObjectNotFoundException
     * @deprecated This method shouldn't be here since it's context dependant. Don't use it, will be removed in the future
     */
    public List<BusinessObjectLight> getPhysicalPath(String objectClass, long objectId) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException;
    
    /**
     * Convenience method that returns the link connected to a port (if any). It serves to avoid calling {@link getSpecialAttribute} two times.
     * @param portClassName The class of the port
     * @param portId The id of the port
     * @return The link connected to the port or null if there isn't any
     * @throws BusinessObjectNotFoundException If the port could not be found
     * @throws MetadataObjectNotFoundException If the class provided does not exist
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException If The class provided is not a subclass of GenericPort
     */
    public BusinessObject getLinkConnectedToPort(String portClassName, long portId) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    
    //Attachments management
    /**
     * Relates a file to an inventory object
     * @param name The name of the file
     * @param tags The tags that describe the contents of the file
     * @param file The file itself
     * @param className The class of the object the file will be attached to
     * @param objectId The id of the object the file will be attached to
     * @return The id of the resulting file object
     * @throws BusinessObjectNotFoundException If the inventory object could not be found
     * @throws OperationNotPermittedException If there's some sort of system restriction that prevented the file to be created
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException If the class provided does not exist
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException If the file size exceeds the max permitted (default value is 10MB)
     */
    public long attachFileToObject(String name, String tags, byte[] file, String className, 
            long objectId) throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Fetches the files associated to an inventory object. Note that this call won't retrieve the actual files, but only references to them
     * @param className The class of the object whose files will be fetched from
     * @param objectId The id of the object whose files will be fetched from
     * @return The list of files
     * @throws org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException If the object could not be found
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException If the class provided does not exist
     */
    public List<FileObjectLight> getFilesForObject(String className, long objectId) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException;
    /**
     * Retrieves a particular file associated to an inventory object. This call returns the actual file
     * @param fileObjectId The id of the file object
     * @param className The class of the object the file is associated to
     * @param objectId The id of the object the file is associated to
     * @return The file
     * @throws org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException If the object could not be found
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException If for some low level reason, the file could not be read from its original location
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException If the class provided does not exist
     */
    public FileObject getFile(long fileObjectId, String className, long objectId) throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException;
    /**
     * Releases (and deletes) a file associated to an inventory object
     * @param fileObjectId The id of the file object
     * @param className The class of the object the file is associated to
     * @param objectId The id of the object the file is associated to
     * @throws org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException If the object could not be found
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException If for some low level reason, the file could not be deleted from disk
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException If the class provided does not exist
     */
    public void detachFileFromObject(long fileObjectId, String className, long objectId) throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException;
    /**
     * Updates the properties of a file object (name or tags)
     * @param fileObjectId The id of the file object
     * @param properties The set of properties as a dictionary key-value. Valid keys are "name" and "tags"
     * @param className The class of the object the file is attached to
     * @param objectId The id of the object the file is attached to
     * @throws BusinessObjectNotFoundException If the object file is attached to could not be found
     * @throws ApplicationObjectNotFoundException If the file object could not be found
     * @throws InvalidArgumentException if any of the properties has an invalid name or if the file name is empty
     * @throws MetadataObjectNotFoundException If the class of the object file is attached to could not be found
     */
    public void updateFileProperties(long fileObjectId, List<StringPair> properties, String className, long objectId) throws BusinessObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException;
    /**
     * Creates a contact. Contacts are always associated to a customer
     * @param contactClass Class of the contact. This class should always be a subclass of GenericContact
     * @param properties The attributes to be set as a String based. key-value dictionary. The list types require only the id of the linked list type as a string
     * @param customerClassName The class of the customer this contact will be associated to
     * @param customerId The id of the customer this contact will be associated to
     * @return The id of the newly created contact
     * @throws BusinessObjectNotFoundException If the customer could not be found
     * @throws InvalidArgumentException If any of the properties or its value is invalid
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException If the customer class could not be found
     */
    public long createContact(String contactClass, List<StringPair> properties, 
            String customerClassName, long customerId) throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException;

    /**
     * Updates a contact's information
     * @param contactClass The class of the contact
     * @param contactId The id of the contact
     * @param properties The attributes to be updated. The list types require only the id of the linked list type as a string
     * @throws BusinessObjectNotFoundException If the contact could not be found
     * @throws InvalidArgumentException If any of the properties or its value is invalid
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException If the contact class could not be found
     */
    public void updateContact(String contactClass, long contactId, List<StringPair> properties) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException;
    /**
     * Deletes a contact
     * @param contactClass The class of the contact
     * @param contactId The id of the contact
     * @throws BusinessObjectNotFoundException If the contact could not be found
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException If the name is empty or there's an attempt to change the creationDate
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException If the class could not be found
     */
    public void deleteContact(String contactClass, long contactId) throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException;
    /**
     * Gets the entire information of a given contact
     * @param contactClass The class of the contact
     * @param contactId The id of the contact
     * @return the contact
     * @throws BusinessObjectNotFoundException If the contact could not be found
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException If the contact class could not be found
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException If the contact is malformed and the customer it should be related to does not exist
     */
    public Contact getContact(String contactClass, long contactId) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Retrieves the list of contacts associated to a customer
     * @param customerClass The class of the customer the contacts belong to
     * @param customerId The id of the customer the contacts belong to
     * @return The list of contacts
     * @throws BusinessObjectNotFoundException If the customer could not be found
     * @throws org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException If the customer class could not be found
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException If an error occurs while building the contact objects
     */
    public List<Contact> getContactsForCustomer(String customerClass, long customerId) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Searches in all the properties of a contact for a given string
     * @param searchString The string to be matched
     * @param maxResults The max number of results. Use -1 to retrieve al results
     * @return The list of contacts that matches the search criteria
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException If an error occurs while building the contact objects
     */
    public List<Contact> searchForContacts(String searchString, int maxResults) throws InvalidArgumentException;
    
    /**
     * Reporting API. Reports are actually Application Objects, however, the BEM has many utility methods that can be used in the scripts to query for inventory objects
     */
    //<editor-fold desc="Reporting API" defaultstate="collapsed">
    /**
     * Creates a class level report (a report that will be available for all instances of a given class -and its subclasses-)
     * @param className Class this report is going to be related to. It can be ab abstract class and the report will be available for all its subclasses
     * @param reportName Name of the report.
     * @param reportDescription Report description.
     * @param script Script text.
     * @param outputType What will be the default output of this report? See RemoteReportLight for possible values
     * @param enabled If enabled, a report can be executed.
     * @return The id of the newly created report.
     * @throws MetadataObjectNotFoundException If the class provided could not be found.
     */
    public long createClassLevelReport(String className, String reportName, String reportDescription, String script, 
            int outputType, boolean enabled) throws MetadataObjectNotFoundException;
    
    /**
     * Creates an inventory level report (a report that is not tied to a particlar instance or class. In most cases, they also receive parameters)
     * @param reportName Name of the report.
     * @param reportDescription Report description.
     * @param script Script text.
     * @param outputType What will be the default output of this report? See InventoryLevelReportDescriptor for possible values
     * @param enabled If enabled, a report can be executed.
     * @param parameters Optional (it might be either null or an empty list). The list of the parameters that this report will support and optional default values. They will always be captured as strings, so it's up to the author of the report the sanitization and conversion of the inputs
     * @return The id of the newly created report.
     * @throws ApplicationObjectNotFoundException If the dummy root could not be found, which is actually a severe problem.
     * @throws InvalidArgumentException If any of the parameter names is null or empty
     */
    public long createInventoryLevelReport(String reportName, String reportDescription, String script, int outputType, 
            boolean enabled, List<StringPair> parameters) throws ApplicationObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Deletes a report
     * @param reportId The id of the report.
     * @return The summary of the changes
     * @throws ApplicationObjectNotFoundException If the report could not be found.
     */
    public ChangeDescriptor deleteReport(long reportId) throws ApplicationObjectNotFoundException;
    
    /**
     * Updates the properties of an existing class level report.
     * @param reportId Id of the report.
     * @param reportName The name of the report. Null to leave it unchanged.
     * @param reportDescription The description of the report. Null to leave it unchanged.
     * @param enabled Is the report enabled? . Null to leave it unchanged.
     * @param type Type of the output of the report. See LocalReportLight for possible values
     * @param script Text of the script. 
     * @return The summary of the changes
     * @throws ApplicationObjectNotFoundException If the report could not be found.
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException If any of the report properties has a wrong or unexpected format.
     */
    public ChangeDescriptor updateReport(long reportId, String reportName, String reportDescription, Boolean enabled,
            Integer type, String script) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Updates the parameters of a report
     * @param reportId The id of the report
     * @param parameters The list of parameters and optional default values. Those with null values will be deleted and the ones that didn't exist previously will be created.
     * @return The summary of the changes
     * @throws ApplicationObjectNotFoundException If the report was not found.
     * @throws InvalidArgumentException If the any of the parameters has an invalid name.
     */
    public ChangeDescriptor updateReportParameters(long reportId, List<StringPair> parameters) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Gets the class level reports associated to the given class (or its superclasses)
     * @param className The class to extract the reports from.
     * @param recursive False to get only the directly associated reports. True top get also the reports associate top its superclasses
     * @param includeDisabled True to also include the reports marked as disabled. False to return only the enabled ones.
     * @return The list of reports.
     * @throws MetadataObjectNotFoundException If the class could not be found
     */
    public List<RemoteReportLight> getClassLevelReports(String className, boolean recursive, boolean includeDisabled) throws MetadataObjectNotFoundException;
    /**
     * Gets the inventory class reports.
     * @param includeDisabled True to also include the reports marked as disabled. False to return only the enabled ones.
     * @return The list of reports.
     * @throws ApplicationObjectNotFoundException f the dummy root could not be found, which is actually a severe problem.
     */
    public List<RemoteReportLight> getInventoryLevelReports(boolean includeDisabled) throws ApplicationObjectNotFoundException;
    
    /**
     * Gets the information related to a class level report.
     * @param reportId The id of the report.
     * @return  The report.
     * @throws ApplicationObjectNotFoundException If the report could not be found.
     */
    public RemoteReport getReport(long reportId) throws ApplicationObjectNotFoundException;
    
    /**
     * Executes a class level report and returns the result.
     * @param objectClassName The class of the instance that will be used as input for the report.
     * @param objectId The id of the instance that will be used as input for the report.
     * @param reportId The id of the report.
     * @return The result of the report execution.
     * @throws MetadataObjectNotFoundException If the class could not be found.
     * @throws ApplicationObjectNotFoundException If the report could not be found.
     * @throws BusinessObjectNotFoundException If the inventory object could not be found.
     * @throws InvalidArgumentException If there's an error during the execution of the report.
     */
    public byte[] executeClassLevelReport(String objectClassName, long objectId, long reportId) 
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Executes an inventory level report and returns the result.
     * @param reportId The id of the report.
     * @param parameters List of pairs param name - param value.
     * @return The result of the report execution.
     * @throws ApplicationObjectNotFoundException If the report could not be found.
     * @throws InvalidArgumentException If the associated script exits with error.
     */
    public byte[] executeInventoryLevelReport(long reportId, List<StringPair> parameters)
            throws ApplicationObjectNotFoundException, InvalidArgumentException;
    //</editor-fold>
    
    
}

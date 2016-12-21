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
 */

package org.kuwaiba.apis.persistence.business;

import com.neotropic.kuwaiba.modules.reporting.model.RemoteReport;
import com.neotropic.kuwaiba.modules.reporting.model.RemoteReportLight;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.util.ChangeDescriptor;
import org.kuwaiba.ws.todeserialize.StringPair;

/**
 * This is the entity in charge of manipulating business objects
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface BusinessEntityManager {
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
     * @throws ObjectNotFoundException Thrown if the parent id is not found
     * @throws OperationNotPermittedException If there's a business constraint that doesn't allow to create the object.
     * @throws InvalidArgumentException If any of the attribute values has an invalid value or format.
     * @throws ApplicationObjectNotFoundException If the specified template could not be found
     */
    public long createObject(String className, String parentClassName, long parentOid,
            HashMap<String,List<String>> attributes,long template)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, 
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
     * @throws ObjectNotFoundException Thrown if the parent id is not found
     * @throws InvalidArgumentException If any of the attribute values has an invalid value or format.
     * @throws OperationNotPermittedException If there's a business constraint that doesn't allow to create the object.
     * @throws ApplicationObjectNotFoundException If the specified template could not be found.
     */
    public long createObject(String className, String parentClassName, String criteria, HashMap<String,List<String>> attributes, long template)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, ApplicationObjectNotFoundException;
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
     * @throws ObjectNotFoundException Thrown if the parent id is not found
     * @throws OperationNotPermittedException If the update can't be performed due to a format issue
     * @throws InvalidArgumentException If any of the attribute values has an invalid value or format.
     * @throws ApplicationObjectNotFoundException If the specified template could not be found.
     */
    public long createSpecialObject(String className, String parentClassName, long parentOid,
            HashMap<String,List<String>> attributes,long template)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, ApplicationObjectNotFoundException;
    
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
    public long createPoolItem(long poolId, String className, String[] attributeNames, String[][] attributeValues, long templateId) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, ArraySizeMismatchException, MetadataObjectNotFoundException;
    
    /**
     * Create massively objects related to their parent using a child_of_special relationship.
     * The name of the objects is automatically set numerically from1 to numberOfChildren
     * @param objectClass Object class
     * @param numberOfChildren
     * @param parentClass Parent class
     * @param parentId parent id
     * @return A list of ids of the newly created objects 
     * @throws MetadataObjectNotFoundException If any of the classes provided can't be found
     * @throws ObjectNotFoundException if the parent can not be found
     * @throws OperationNotPermittedException If due to business rules, the operation can't be performed
     * @throws InvalidArgumentException If any of the attributes or its type is invalid.
     */
    public long[] createBulkSpecialObjects(String objectClass, int numberOfChildren, String parentClass, long parentId)
           throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException;
    /**
     * Gets the detailed information about an object
     * @param className Object class name
     * @param oid Object's oid
     * @return A detailed representation of the requested object
     * @throws MetadataObjectNotFoundException If the className class can't be found
     * @throws ObjectNotFoundException If the requested object can't be found
     * @throws InvalidArgumentException If the database object could not be properly mapped into a serializable java object.
     */
    public RemoteBusinessObject getObject(String className, long oid)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException;

    /**
     * Gets the special children of a given object
     * @param objectClass Object class
     * @param objectId object id
     * @return The list of special children.
     * @throws MetadataObjectNotFoundException If the class could not be found
     * @throws ObjectNotFoundException  If the object could not be found.
     */
    public List<RemoteBusinessObjectLight> getObjectSpecialChildren(String objectClass, long objectId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException;
    
    /**
     * Gets the simplified information about an object
     * @param className Object class name
     * @param oid Object's oid
     * @return A detailed representation of the requested object
     * @throws MetadataObjectNotFoundException If the className class can't be found
     * @throws ObjectNotFoundException If the requested object can't be found
     */
    public RemoteBusinessObjectLight getObjectLight(String className, long oid)
            throws MetadataObjectNotFoundException, ObjectNotFoundException;

    /**
     * Gets the parent of a given object in the containment hierarchy
     * @param objectClass Object class
     * @param oid Object id
     * @return The immediate parent. Null if the parent is null. A dummy object with id -1 if the parent is DummyRoot
     * @throws ObjectNotFoundException If the requested object can't be found
     * @throws MetadataObjectNotFoundException If any of the class nodes involved is malformed
     * @throws InvalidArgumentException If the database object could not be properly mapped into a serializable java object.
     */
    public RemoteBusinessObject getParent(String objectClass, long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Retrieves recursively the list of parents of an object in the containment hierarchy
     * @param oid Object id 
     * @param objectClassName Object class
     * @return The list of parents
     * @throws ObjectNotFoundException If the object does not exist
     * @throws MetadataObjectNotFoundException if the class can not be found
     */
    public List<RemoteBusinessObjectLight> getParents(String objectClassName, long oid)
        throws ObjectNotFoundException, MetadataObjectNotFoundException;

    /**
     * Gets the first parent of an object which matches the given class in the containment hierarchy
     * @param objectClass Object class
     * @param oid Object id
     * @param parentClass Parent class
     * @return The nearest parent of the provided class. Null if none found.
     * @throws ObjectNotFoundException If any of the requested objects can't be found
     * @throws MetadataObjectNotFoundException If any of the class nodes involved is malformed
     * @throws InvalidArgumentException If the database object could not be properly mapped into a serializable java object.
     */
    public RemoteBusinessObject getParentOfClass(String objectClass, long oid, String parentClass)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException;
    /**
     * Deletes a set of objects
     * @param releaseRelationships If all the relationships should be release upon deleting the objects. If false, an OperationNotPermittedException  will be raised if the object has incoming relationships.
     * @param  objects a hashmap where the class name is the key and the value is a list of long containing the ids of the objects to be deleted that are instance of the key class
     * @throws ObjectNotFoundException If the requested object can't be found
     * @throws MetadataObjectNotFoundException If the requested object class can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked or it has relationships and releaseRelationships is false
     */
    public void deleteObjects(HashMap<String, List<Long>> objects, boolean releaseRelationships)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException;

    /**
     * Deletes a single object
     * @param className Object's class name
     * @param oid Objects oid
     * @param releaseRelationships Release relationships automatically. If set to false, it will fail if the object already has incoming relationships
     * @throws ObjectNotFoundException If the object couldn't be found
     * @throws MetadataObjectNotFoundException If the class could not be found
     * @throws OperationNotPermittedException If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships.
     */
    public void deleteObject(String className, long oid, boolean releaseRelationships)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException;
    
    /**
     * Updates an object attributes. Note that you can't set binary attributes through this
     * method. Use setBinaryAttributes instead.
     * @param className Object class name
     * @param oid Object's oid
     * @param attributes The attributes to be updated (the key is the attribute name, 
     * the value is and array with the value -or values in case of MANY TO MANY list type attributes-)
     * @return The summary of the changes that were made
     * @throws MetadataObjectNotFoundException If the object class can't be found
     * @throws ObjectNotFoundException If the object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * @throws InvalidArgumentException If any of the names provided does not exist or can't be set using this method or of the value of any of the attributes can not be mapped correctly.
     */
    public ChangeDescriptor updateObject(String className, long oid, HashMap<String,List<String>> attributes)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException;
    /**
     * Updates an object binary attributes.
     * @param className Object's class name
     * @param oid Object's oid
     * @param attributeNames The attributes to be updated
     * @param attributeValues The attribute values
     * @return Success or failure
     * @throws MetadataObjectNotFoundException If the object class can't be found
     * @throws ObjectNotFoundException If the object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * @throws ArraySizeMismatchException If the arrays attributeNames and attributeValues have different lengths
     */
    public boolean setBinaryAttributes(String className, long oid, List<String> attributeNames, List<byte[]> attributeValues)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, ArraySizeMismatchException;
    /**
     * Move a list of objects to a new parent: this methods ignores those who can't be moved and raises
     * an OperationNotPermittedException, however, it will move those which can be moved
     * @param objects Map using the object class name as keys and the respective objects oids as values
     * @param targetClassName Parent's class name
     * @param targetOid Parent's oid
     * @throws MetadataObjectNotFoundException If the object's or new parent's class can't be found
     * @throws ObjectNotFoundException If the object or its new parent can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     */
    public void moveObjects(String targetClassName, long targetOid, HashMap<String,long[]> objects)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Copy a set of objects
     * @param objects Hashmap with the objects class names as keys and their oids as values
     * @param targetClassName Target parent's class name
     * @param targetOid Target parent's oid
     * @param recursive If this operation should also copy the children objects recursively
     * @return A list containing the newly created object ids
     * @throws MetadataObjectNotFoundException If any of the provided classes couldn't be found
     * @throws ObjectNotFoundException If any of the template objects couldn't be found
     * @throws OperationNotPermittedException If the target parent can't contain any of the new instances
     */
    public long[] copyObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects, boolean recursive)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Locks and object read-only or release the block. Not implemented yet.
     * @param className object's class name
     * @param oid object's oid
     * @param value true to set the block, false to release it
     * @return Success or failure
     * @throws MetadataObjectNotFoundException If the object's can't be found
     * @throws ObjectNotFoundException If the object or its new parent can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     */
    public boolean setObjectLockState(String className, long oid, Boolean value)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Gets the children of a given object
     * @param className Object's class name
     * @param oid Object's oid
     * @param maxResults max number of children to be returned
     * @return The list of children
     * @throws MetadataObjectNotFoundException If the object's can't be found
     * @throws ObjectNotFoundException If the object or its new parent can't be found
     */
    public List<RemoteBusinessObjectLight> getObjectChildren(String className, long oid, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException;
    
    /**
     * Gets the children of a given object, providing the class and object id.
     * @param classId The id of the class the object is instance of
     * @param oid The oid of the object
     * @param maxResults The max number of results to be retrieved. Use 0 to retrieve all
     * @return The list of children.
     * @throws ObjectNotFoundException If the object could not be found.
     * @throws MetadataObjectNotFoundException If the class could not be found.
     */
    public List<RemoteBusinessObjectLight> getObjectChildren(long classId, long oid, int maxResults)
            throws ObjectNotFoundException, MetadataObjectNotFoundException;
    
    /**
     * Gets the direct children of a given object of a given class.
     * @param parentOid parent id
     * @param parentClass Parent class
     * @param classToFilter Class to be match against
     * @param maxResults max number of results. 0 to get all
     * @return A list of children of parentid/parentClass instance that are instances of classToFilter.
     * @throws MetadataObjectNotFoundException If any of the classes can not be found
     * @throws ObjectNotFoundException If parent object can not be found
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException If the database objects can not be correctly mapped into serializable Java objects.
     */
    public List<RemoteBusinessObject> getChildrenOfClass(long parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Same as getChildrenOfClass, but returns only the light version of the objects
     * @param parentOid parent id
     * @param parentClass Parent class
     * @param classToFilter Class to be match against
     * @param maxResults max number of results. 0 to get all
     * @return A list of children of parentid/parentClass instance, instances of classToFilter
     * @throws MetadataObjectNotFoundException If any of the classes can not be found
     * @throws ObjectNotFoundException If parent object can not be found
     */
    public List<RemoteBusinessObjectLight> getChildrenOfClassLight(long parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException;

    /**
     * Gets the siblings of a given object in the containment hierarchy
     * @param className Object class
     * @param oid Object oid
     * @param maxResults Max number of results to be returned
     * @return List of siblings
     * @throws MetadataObjectNotFoundException If the class does not exist
     * @throws ObjectNotFoundException If the object does not exist
     */
    public List<RemoteBusinessObjectLight> getSiblings(String className, long oid, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException;
    
    /**
     * Recursively gets all the instances of  given class
     * @param className Class name. It mist be a subclass of InventoryObject
     * @param maxResults Max number of results. 0 to get all
     * @return a list of instances 
     * @throws MetadataObjectNotFoundException if the class can not be found
     * @throws InvalidArgumentException If the class is not subclass of InventoryObject
     */
    public List<RemoteBusinessObjectLight> getObjectsOfClassLight(String className, int maxResults)
            throws MetadataObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Creates a relationship between two elements and labels it
     * @param aObjectClass a side object class
     * @param aObjectId a side object id
     * @param bObjectClass b side object class
     * @param bObjectId b side object id
     * @param name Name to label the new relationship
     * @param unique If there could be only one relationship between both elements with that name
     * @throws ObjectNotFoundException If any of the objects can't be found
     * @throws OperationNotPermittedException if any of the objects involved can't be connected (i.e. if it's not an inventory object)
     * @throws MetadataObjectNotFoundException if any of the classes provided can not be found
     */
    public void createSpecialRelationship(String aObjectClass, long aObjectId, String bObjectClass, long bObjectId, String name, boolean unique)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException;
    
    /**
     * This method creates a special relationship  with a set of property values
     * @param aObjectClass The class of the first object to related
     * @param aObjectId The id of the first object to related
     * @param bObjectClass The class of the second object to related
     * @param bObjectId The id of the first object to related
     * @param name The name of the relationship
     * @param unique If there could be only one relationship between both elements with that name
     * @param properties A hash with the set of properties and their respective values
     * @throws ObjectNotFoundException If any of the objects can not be found
     * @throws OperationNotPermittedException If, due to a business rule, the objects can not be related
     * @throws MetadataObjectNotFoundException If any of the classes specified does not exist
     */
    public void createSpecialRelationship(String aObjectClass, long aObjectId, String bObjectClass, 
            long bObjectId, String name, boolean unique, HashMap<String, Object> properties) throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException;
    
    /**
     * Release all special relationships with a given name
     * @param objectClass Object class
     * @param objectId Object id
     * @param otherObjectId The object we want to be released from. -1 To all objects related with relationships with that name
     * @param relationshipName Relationship name
     * @throws ObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException  If the class can not be found
     */
    public void releaseSpecialRelationship(String objectClass, long objectId, long otherObjectId, String relationshipName)
            throws ObjectNotFoundException, MetadataObjectNotFoundException;
    
    /**
     * Release all special relationships with a given name whose target object id matches with the one provided
     * @param objectClass Object class
     * @param objectId Object id
     * @param relationshipName Relationship name
     * @param targetId Id of the object at the end of the relationship
     * @throws ObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException  If the class can not be found
     */
    public void releaseSpecialRelationship(String objectClass, long objectId, String relationshipName, long targetId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException;

    /**
     * Gets the value of a special attribute. A special attribute is one belonging to a business domain specific attribute
     * (usually a model. Domain specific attribute information is not filed under the standard metadata but a special one. Implementations may vary)
     * @param objectClass object's class
     * @param objectId object's id
     * @param specialAttributeName Special attribute name
     * @return A list of objects related to the object through a special relationship
     * @throws ObjectNotFoundException if the object can not be found
     * @throws MetadataObjectNotFoundException if either the object class or the attribute can not be found
     */    
    public List<RemoteBusinessObjectLight> getSpecialAttribute(String objectClass, long objectId, String specialAttributeName) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException;
    
    /**
     * This method will extract the object at the other side of the special relationship and all the properties of the relationship itself
     * @param objectClass The class of the object whose special attribute will be retrieved from
     * @param objectId The object's id
     * @param specialAttributeName The name of the special attribute
     * @return The list of elements related with such relationship plus the properties of theis relationships
     * @throws ObjectNotFoundException If the object could not be found
     * @throws MetadataObjectNotFoundException If the object class could not be found
     */
    public List<AnnotatedRemoteBusinessObjectLight> getAnnotatedSpecialAttribute(String objectClass, long objectId, String specialAttributeName) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException;
    
    /**
     * Returns all the special relationships of a given object as a hashmap whose keys are
     * the names of the relationships and the values the list of related objects
     * @param className Object class
     * @param objectId Object Id
     * @return The hash map with the existing special relationships and the associated objects
     * @throws MetadataObjectNotFoundException If the class provided does not exist
     * @throws ObjectNotFoundException if the object does not exist
     */
    public HashMap<String,List<RemoteBusinessObjectLight>> getSpecialAttributes (String className, long objectId) 
        throws MetadataObjectNotFoundException, ObjectNotFoundException;
    
    /**
     * Checks if an object has a given number of standard relationships with another object
     * @param objectClass Object class
     * @param objectId Object id
     * @param relationshipName Relationship name
     * @param numberOfRelationships Number of relationships
     * @return True if the object has numberOfRelationships relationships with another object
     * @throws ObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException  if objectClass does not exist
     */
    public boolean hasRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException;

    /**
     * Checks if an object has a given number of special relationships with another object
     * @param objectClass Object class
     * @param objectId Object id
     * @param relationshipName Relationship name
     * @param numberOfRelationships Number of relationships
     * @return True if the object has numberOfRelationships relationships with another object
     * @throws ObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException  if objectClass does not exist
     */
    public boolean hasSpecialRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException;
    
    /**
     * Finds all possible routes between two given inventory objects
     * @param objectAClassName Inventory object A class name
     * @param objectAId  Inventory object A id
     * @param objectBClassName  Inventory object B class name
     * @param objectBId  Inventory object B id
     * @param relationshipName The name of the relationship used to navigate through nodes and find the route
     * @return A list of the routes, including only the nodes as RemoteBusinessObjectLights
     */
    public List<RemoteBusinessObjectLightList> findRoutesThroughSpecialRelationships (String objectAClassName, long objectAId, String objectBClassName, long objectBId, String relationshipName);
    /**
     * Finds the physical path from one port to another
     * @param objectClass The source port class.
     * @param objectId The source port id.
     * @return A list of objects that make part of the physical trace.
     * @deprecated This method shouldn't be here since it's context dependant. Don't use it, will be removed in the future
     */
    public List<RemoteBusinessObjectLight> getPhysicalPath(String objectClass, long objectId);
    
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
     * @throws ApplicationObjectNotFoundException If the report could not be found.
     */
    public void deleteReport(long reportId) throws ApplicationObjectNotFoundException;
    
    /**
     * Updates the properties of an existing class level report.
     * @param reportId Id of the report.
     * @param reportName The name of the report. Null to leave it unchanged.
     * @param reportDescription The description of the report. Null to leave it unchanged.
     * @param enabled Is the report enabled? . Null to leave it unchanged.
     * @param type Type of the output of the report. See LocalReportLight for possible values
     * @param script Text of the script. 
     * @throws ApplicationObjectNotFoundException If the report could not be found.
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException If any of the report properties has a wrong or unexpected format.
     */
    public void updateReport(long reportId, String reportName, String reportDescription, Boolean enabled,
            Integer type, String script) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException;
    
    /**
     * Updates the parameters of a report
     * @param reportId The id of the report
     * @param parameters The list of parameters and optional default values. Those with null values will be deleted and the ones that didn't exist previously will be created.
     * @throws ApplicationObjectNotFoundException If the report was not found.
     * @throws InvalidArgumentException If the any of the parameters has an invalid name.
     */
    public void updateReportParameters(long reportId, List<StringPair> parameters) 
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
     * @throws ObjectNotFoundException If the inventory object could not be found.
     * @throws InvalidArgumentException If there's an error during the execution of the report.
     */
    public byte[] executeClassLevelReport(String objectClassName, long objectId, long reportId) 
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException;
    
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

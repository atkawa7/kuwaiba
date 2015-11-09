/*
 *  Copyright 2010 - 2014 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.persistence.interfaces;

import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.WrongMappingException;

/**
 * This is the entity in charge of manipulating business objects
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface BusinessEntityManager {
    /**
     * Creates a new inventory object
     * @param className Name of the class which this object will be instantiated from
     * @param parentClassName Parent object class name
     * @param parentOid Parent's oid
     * @param attributes Attributes to be set by default in the new object. It's a HashMap where the keys are the attribute names and the values, the values for such attributes.
     * Note that binary type attributes can't be set here.
     * @param template Template id to be used to create the current object. Template values can be
     * overridden if "attributeValues" is not empty
     * @return The object's id
     * @throws MetadataObjectNotFoundException Thrown if the object's class can't be found
     * @throws ObjectNotFoundException Thrown if the parent id is not found
     * @throws OperationNotPermittedException If the update can't be performed due to a format issue
     * @throws InvalidArgumentException If the parent node is malformed.
     * @throws DatabaseException if the reference node used by the dummy root doesn't exist
     */
    public long createObject(String className, String parentClassName, long parentOid,
            HashMap<String,List<String>> attributes,long template, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, DatabaseException, ApplicationObjectNotFoundException, NotAuthorizedException;
    /**
     * Creates a new inventory object for a domain specific model (where the standard containment rules don't apply)
     * @param className Name of the class which this object will be instantiated from
     * @param parentClassName Parent object class name
     * @param parentOid Parent's oid
     * @param attributes Attributes to be set by default in the new object. It's a HashMap where the keys are the attribute names and the values, the values for such attributes.
     * Note that binary type attributes can't be set here.
     * @param template Template id to be used to create the current object. Template values can be
     * overridden if "attributeValues" is not empty
     * @return The object's id
     * @throws MetadataObjectNotFoundException Thrown if the object's class can't be found
     * @throws ObjectNotFoundException Thrown if the parent id is not found
     * @throws OperationNotPermittedException If the update can't be performed due to a format issue
     * @throws InvalidArgumentException If the parent node is malformed.
     * @throws DatabaseException if the reference node used by the dummy root doesn't exist
     */
    public long createSpecialObject(String className, String parentClassName, long parentOid,
            HashMap<String,List<String>> attributes,long template, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, DatabaseException, ApplicationObjectNotFoundException, NotAuthorizedException;
    
    /**
     * Create massively objects related to their parent using a child_of_special relationship.
     * The name of the objects is automatically set numerically from1 to numberOfChildren
     * @param objectClass Object class
     * @param numberOfObjects Number of objets
     * @param parentClass Parent class
     * @param parentId parent id
     * @return A list of ids of the newly created objects 
     * @throws MetadataObjectNotFoundException If any of the classes provided can't be found
     * @throws ObjectNotFoundException if the parent can not be found
     * @throws OperationNotPermittedException If due to business rules, the operation can't be performed
     */
    public long[] createBulkSpecialObjects(String objectClass, int numberOfChildren, String parentClass, long parentId, String ipAddress, String sessionId)
           throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, ApplicationObjectNotFoundException, NotAuthorizedException;
    /**
     * Gets the detailed information about an object
     * @param className Object class name
     * @param oid Object's oid
     * @return A detailed representation of the requested object
     * @throws MetadataObjectNotFoundException If the className class can't be found
     * @throws ObjectNotFoundException If the requested object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * @throws NotAuthorizedException If the update can't be performed due to permissions
     */
    public RemoteBusinessObject getObject(String className, long oid, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException;

    /**
     * Gets the simplified information about an object
     * @param className Object class name
     * @param oid Object's oid
     * @return A detailed representation of the requested object
     * @throws MetadataObjectNotFoundException If the className class can't be found
     * @throws ObjectNotFoundException If the requested object can't be found
     * @throws NotAuthorizedException If the update can't be performed due to permissions
     */
    public RemoteBusinessObjectLight getObjectLight(String className, long oid, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException;

    /**
     * Gets the parent of a given object in the containment hierarchy
     * @param objectClass Object class
     * @param oid Object id
     * @return The immediate parent. Null if the parent is null. A dummy object with id -1 if the parent is DummyRoot
     * @throws ObjectNotFoundException If the requested object can't be found
     * @throws MetadataObjectNotFoundException If any of the class nodes involved is malformed
     * @throws InvalidArgumentException If any of the nodes involved is malformed
     */
    public RemoteBusinessObject getParent(String objectClass, long oid, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException;
    
    /**
     * Retrieves recursively the list of parents of an object in the containment hierarchy
     * @param oid Object id 
     * @param objectClassName Object class
     * @return The list of parents
     * @throws ObjectNotFoundException If the object does not exist
     * @throws MetadataObjectNotFoundException if the class can not be found
     */
    public List<RemoteBusinessObjectLight> getParents(String objectClassName, long oid, String ipAddress, String sessionId)
        throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException;

    /**
     * Gets the first parent of an object which matches the given class in the containment hierarchy
     * @param objectClass Object class
     * @param oid Object id
     * @param parentClass Parent class
     * @return The nearest parent of the provided class. Null if none found.
     * @throws ObjectNotFoundException If any of the requested objects can't be found
     * @throws MetadataObjectNotFoundException If any of the class nodes involved is malformed
     * @throws InvalidArgumentException If any of the nodes involved is malformed
     */
    public RemoteBusinessObject getParentOfClass(String objectClass, long oid, String parentClass, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException;
    /**
     * Deletes a set of objects
     * @param  objects a hashmap where the class name is the key and the value is a list of long containing the ids of the objects to be deleted that are instance of the key class
     * @param  should all relationships be released, forcing the deletion?
     * @throws ObjectNotFoundException If the requested object can't be found
     * @throws MetadataObjectNotFoundException If the requested object class can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * or it is blocked or if the requested object or one of it's children have
     * relationships that should be released manually before to delete them
     */
    public void deleteObjects(HashMap<String, long[]> oids, boolean releaseRelationships, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException, ApplicationObjectNotFoundException, NotAuthorizedException;

    /**
     * Updates an object attributes. Note that you can't set binary attributes through this
     * method. Use setBinaryAttributes instead.
     * @param className Object class name
     * @param oid Object's oid
     * @param attributes The attributes to be updated (the key is the attribute name, 
     * the value is and array with the value -or values in case of MANY TO MANY list type attributes-)
     * @throws MetadataObjectNotFoundException If the object class can't be found
     * @throws ObjectNotFoundException If the object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * @throws InvalidArgumentException If any of the names provided does not exist or can't be set using this method
     * @throws ApplicationObjectNotFoundException If it's not possible to create the log entry because the user couldn't be found
     */
    public void updateObject(String className, long oid, HashMap<String,List<String>> attributes, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException,
                WrongMappingException, InvalidArgumentException, ApplicationObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException;
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
    public boolean setBinaryAttributes(String className, long oid, List<String> attributeNames, List<byte[]> attributeValues, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, ArraySizeMismatchException, ApplicationObjectNotFoundException, NotAuthorizedException;
    /**
     * Move a list of objects to a new parent: this methods ignores those who can't be moved and raises
     * an OperationNotPermittedException, however, it will move those which can be moved
     * @param objects Map using the object class name as keys and the respective objects oids as values
     * @param targetClassName Parent's class name
     * @param targetOid Parent's oid
     * @throws MetadataObjectNotFoundException If the object's or new parent's class can't be found
     * @throws ObjectNotFoundException If the object or its new parent can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     * @throws ArraySizeMismatchException If the oids and classNames array sizes do not match
     */
    public void moveObjects(String targetClassName, long targetOid, HashMap<String,long[]> objects, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, ApplicationObjectNotFoundException, NotAuthorizedException;

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
    public long[] copyObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects, boolean recursive, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, ApplicationObjectNotFoundException, NotAuthorizedException;

    /**
     * Locks and object read-only or release the block
     * @param className object's class name
     * @param oid object's oid
     * @param value true to set the block, false to release it
     * @return Success or failure
     * @throws MetadataObjectNotFoundException If the object's can't be found
     * @throws ObjectNotFoundException If the object or its new parent can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     */
    public boolean setObjectLockState(String className, long oid, Boolean value, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, ApplicationObjectNotFoundException, NotAuthorizedException;

    /**
     * Gets the children of a given object
     * @param className Object's class name
     * @param oid Object's oid
     * @param maxResults max number of children to be returned
     * @return The list of children
     * @throws MetadataObjectNotFoundException If the object's can't be found
     * @throws ObjectNotFoundException If the object or its new parent can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     */
    public List<RemoteBusinessObjectLight> getObjectChildren(String className, long oid, int maxResults, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException;
    
    /**
     * Gets the direct children of a given object of a given class
     * @param parentOid parent id
     * @param parentClass Parent class
     * @param classToFilter Class to be match against
     * @param maxResults max number of results. 0 to get all
     * @return A list of children of parentid/parentClass instance, instances of classToFilter
     * @throws MetadataObjectNotFoundException If any of the classes can not be found
     * @throws ObjectNotFoundException If parent object can not be found
     */
    public List<RemoteBusinessObject> getChildrenOfClass(long parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException;
    
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
    public List<RemoteBusinessObjectLight> getChildrenOfClassLight(long parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException;

    /**
     * Gets the siblings of a given object in the containment hierarchy
     * @param className Object class
     * @param oid Object oid
     * @param maxResults Max number of results to be returned
     * @return List of siblings
     * @throws MetadataObjectNotFoundException If the class does not exist
     * @throws ObjectNotFoundException If the object does not exist
     */
    public List<RemoteBusinessObjectLight> getSiblings(String className, long oid, int maxResults, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException;
    
    /**
     * Recursively gets all the instances of  given class
     * @param className Class name. It mist be a subclass of InventoryObject
     * @param maxResults Max number of results. 0 to get all
     * @return a list of instances 
     * @throws MetadataObjectNotFoundException if the class can not be found
     * @throws InvalidArgumentException If the class is not subclass of InventoryObject
     */
    public List<RemoteBusinessObjectLight> getObjectsOfClassLight(String className, int maxResults, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException;
    
    /**
     * Creates a relationship between two elements and labels it
     * @param aObjectClass a side object class
     * @param aObjectId a side object id
     * @param bObjectClass b side object class
     * @param bObjectId b side object id
     * @param name Name to label the new relationship
     * @throws ObjectNotFoundException If any of the objects can't be found
     * @throws OperationNotPermittedException if any of the objects involved can't be connected (i.e. if it's not an inventory object)
     * @throws MetadataObjectNotFoundException if any of the classes provided can not be found
     */
    public void createSpecialRelationship(String aObjectClass, long aObjectId, String bObjectClass, long bObjectId, String name, String ipAddress, String sessionId)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException;
    
    /**
     * Release all special relationships with a given name
     * @param objectClass Object class
     * @param objectId Object id
     * @param otherObjectId The object we want to be released from. -1 To all objects related with relationships with that name
     * @param relationshipName Relationship name
     * @throws ObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException  If the class can not be found
     */
    public void releaseSpecialRelationship(String objectClass, long objectId, long otherObjectId, String relationshipName, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException;
    
    /**
     * Release all special relationships with a given name whose target object id matches with teh one provided
     * @param objectClass Object class
     * @param objectId Object id
     * @param relationshipName Relationship name
     * @param targetId Id of the object at the end of the relationship
     * @throws ObjectNotFoundException If the object can not be found
     * @throws MetadataObjectNotFoundException  If the class can not be found
     */
    public void releaseSpecialRelationship(String objectClass, long objectId, String relationshipName, long targetId, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException;

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
    public List<RemoteBusinessObjectLight> getSpecialAttribute(String objectClass, long objectId, String specialAttributeName, String ipAddress, String sessionId) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException;
    
    /**
     * Returns all the special relationships of a given object as a hashmap whose keys are
     * the names of the relationships and the values the list of related objects
     * @param className Object class
     * @param objectId Object Id
     * @return The hash map with the existing special relationships and the associated objects
     * @throws MetadataObjectNotFoundException If the class provided does not exist
     * @throws ObjectNotFoundException if the object does not exist
     */
    public HashMap<String,List<RemoteBusinessObjectLight>> getSpecialAttributes (String className, long objectId, String ipAddress, String sessionId) 
        throws MetadataObjectNotFoundException, ObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException;
    
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
    public boolean hasRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships, String ipAddress, String sessionId) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException;

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
    public boolean hasSpecialRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships, String ipAddress, String sessionId) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException;
    
    /**
     * Excute a patch file 
     * @throws MetadataObjectNotFoundException
     * @throws NotAuthorizedException 
     */
    public int[] executePatch() throws NotAuthorizedException;
}

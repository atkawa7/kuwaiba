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

package org.kuwaiba.apis.persistence.interfaces;

import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.application.ResultRecord;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
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
     */
    public Long createObject(String className, String parentClassName, Long parentOid,
            HashMap<String,List<String>> attributes,Long template)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException;
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
     */
    public Long createSpecialObject(String className, String parentClassName, Long parentOid,
            HashMap<String,List<String>> attributes,Long template)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException;
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
    public RemoteBusinessObject getObjectInfo(String className, Long oid)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException;

    /**
     * Gets the simplified information about an object
     * @param className Object class name
     * @param oid Object's oid
     * @return A detailed representation of the requested object
     * @throws MetadataObjectNotFoundException If the className class can't be found
     * @throws ObjectNotFoundException If the requested object can't be found
     * @throws NotAuthorizedException If the update can't be performed due to permissions
     */
    public RemoteBusinessObjectLight getObjectInfoLight(String className, Long oid)
            throws MetadataObjectNotFoundException, ObjectNotFoundException;

    /**
     * Deletes a set of objects
     * @param  objects a hashmap where the class name is the key and the value is a list of Long containing the ids of the objects to be deleted that are instance of the key class
     * @param  should all relationships be released, forcing the deletion?
     * @throws ObjectNotFoundException If the requested object can't be found
     * @throws MetadataObjectNotFoundException If the requested object class can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * or it is blocked or if the requested object or one of it's children have
     * relationships that should be released manually before to delete them
     */
    public void deleteObjects(HashMap<String, List<Long>> oids, boolean releaseRelationships)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException;


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
     */
    public void updateObject(String className, Long oid, HashMap<String,List<String>> attributes)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException,
                WrongMappingException, InvalidArgumentException;

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
    public boolean setBinaryAttributes(String className, Long oid, List<String> attributeNames, List<byte[]> attributeValues)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException,
                ArraySizeMismatchException;

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

    public void moveObjects(String targetClassName, Long targetOid, HashMap<String,List<Long>> objects)
            throws MetadataObjectNotFoundException, ObjectNotFoundException,
                 OperationNotPermittedException;

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
    public List<Long> copyObjects(String targetClassName, Long targetOid, HashMap<String, List<Long>> objects, boolean recursive)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException;

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
    public boolean setObjectLockState(String className, Long oid, Boolean value)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException;

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
    public List<RemoteBusinessObjectLight> getObjectChildren(String className, Long oid, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException;
    
    /**
     * Executes a query
     * @return The list of results
     * @throws MetadataObjectNotFoundException If any of the classes used as based for the search do not exist
     */
    public List<ResultRecord> executeQuery()
            throws MetadataObjectNotFoundException;

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
    public void createSpecialRelationship(String aObjectClass, Long aObjectId, String bObjectClass, Long bObjectId, String name)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException;

    /**
     * Gets the value of a special attribute. A special attribute is one belonging to a business domain specific attribute
     * (usually a model. Domain specific attribute information is not filed under the standard metadata but a special one. Implementations may vary)
     * @param objectClass object's class
     * @param objectId object's id
     * @param specialAttributeName Special attribute name
     * @return A list of string with the value associated to such object (typically a list of longs)
     * @throws ObjectNotFoundException if the object can not be found
     * @throws MetadataObjectNotFoundException if either the object class or the attribute can not be found
     */
    public List<String> getSpecialAttribute(String objectClass, Long objectId, String specialAttributeName)
            throws ObjectNotFoundException, MetadataObjectNotFoundException;
}

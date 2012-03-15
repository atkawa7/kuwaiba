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
import org.kuwaiba.apis.persistence.business.RemoteObject;
import org.kuwaiba.apis.persistence.business.RemoteObjectLight;
import org.kuwaiba.apis.persistence.application.ResultRecord;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectWithRelationsException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.WrongMappingException;

/**
 * This is the entity in charge of manipulating business objects
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface BusinessEntityManager {
    /**
     *
     * @param className Name of the class which this object will be instantiated from
     * @param parentOid Parent's oid
     * @param attributes Attributes to be set by default in the new object. It's a HashMap where the keys are the attribute names and the values, the values for such attributes.
     * Note that one-to-many and binary type attributes can't be set here
     * @param template Template name to be use to create the current object. Template values can be
     * overridden if "attributeValues" is not empty
     * @return The object's id
     * @throws MetadataObjectNotFoundException Thrown if the object's class can't be found
     * @throws ObjectNotFoundException Thrown if the parent id is not found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     */
    public Long createObject(String className, Long parentOid,
            HashMap<String,String> attributes,String template)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException;
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
    public RemoteObject getObjectInfo(String className, Long oid)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, 
                    OperationNotPermittedException;

    /**
     * Gets the simplified information about an object
     * @param className Object class name
     * @param oid Object's oid
     * @return A detailed representation of the requested object
     * @throws MetadataObjectNotFoundException If the className class can't be found
     * @throws ObjectNotFoundException If the requested object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * @throws NotAuthorizedException If the update can't be performed due to permissions
     */
    public RemoteObjectLight getObjectInfoLight(String className, Long oid)
            throws MetadataObjectNotFoundException, ObjectNotFoundException,
                    OperationNotPermittedException;

    /**
     * Deletes an object
     * @param oid object's id
     * @return Success of failure
     * @throws ObjectWithRelationsException If the requested object or one of it's children have
     * relationships that should be released manually before to delete them
     * @throws ObjectNotFoundException If the requested object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * or it is blocked
     */
    public boolean deleteObject(Long oid)
            throws ObjectWithRelationsException, ObjectNotFoundException, 
                OperationNotPermittedException;


    /**
     * Updates an object attributes. Note that you can't set one-to-many or binary attributes through this
     * method. Use setBinaryAttributes and setManyToManyAttribute instead.
     * @param className Object class name
     * @param oid Object's oid
     * @param attributes The attributes to be updated (the key is the attribute name, the value is the value)
     * @throws MetadataObjectNotFoundException If the object class can't be found
     * @throws ObjectNotFoundException If the object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * @throws InvalidArhumentException If any of the names provided does not exist or can't be set using this method
     */
    public void updateObject(String className, Long oid, HashMap<String,String> attributes)
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
     * Updates an object many-to-one, one-to-many and many-to-may type of attribute.
     * @param className Object's class name
     * @param oid Object's oid
     * @param attributeTypeClassName The class where the values are instance of
     * @param attributeName The attribute to be updated
     * @param attributeValues The attribute value(s) (given as oids)
     * @return Success or failure
     * @throws MetadataObjectNotFoundException If the object class or the list type class can't be found
     * @throws ObjectNotFoundException If the object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     */
    public boolean setManyToManyAttribute(String className, Long oid, String attributeTypeClassName, String attributeName, List<Long> attributeValues)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException;

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

    public void moveObjects(HashMap<String,List<Long>> objects, String targetClassName, Long targetOid)
            throws MetadataObjectNotFoundException, ObjectNotFoundException,
                 OperationNotPermittedException;

    /**
     * Copy a set of objects
     * @param objects Hashmap with the objects class names as keys and their oids as values
     * @param targetClassName Target parent's class name
     * @param targetOid Target parent's oid
     * @return A list containing the newly created objects
     * @throws MetadataObjectNotFoundException If any of the provided classes couldn't be found
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     */
    public RemoteObjectLight[] copyObjects(HashMap<String, Long> objects, String targetClassName, Long targetOid)
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
     * @return The list of children
     * @throws MetadataObjectNotFoundException If the object's can't be found
     * @throws ObjectNotFoundException If the object or its new parent can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     */
    public RemoteObjectLight[] getObjectChildren(String className, Long oid)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException;
    
    /**
     * Executes a query
     * @return The list of results
     * @throws MetadataObjectNotFoundException If any of the classes used as based for the search do not exist
     */
    public List<ResultRecord> executeQuery()
            throws MetadataObjectNotFoundException;

}

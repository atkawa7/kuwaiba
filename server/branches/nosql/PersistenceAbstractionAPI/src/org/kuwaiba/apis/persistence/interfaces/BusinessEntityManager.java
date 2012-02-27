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

import java.util.List;
import org.kuwaiba.apis.persistence.RemoteObject;
import org.kuwaiba.apis.persistence.RemoteObjectLight;
import org.kuwaiba.apis.persistence.ResultRecord;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
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
     * @param attributeNames Array with attributes to be set
     * @param attributeValues Attribute values corresponding to the "attributeNames" parameter as strings
     * please note that one-to-many and binary type attributes can't be set here
     * @param template Template name to be use to create the current object. Template values can be
     * overridden if "attributeValues" is not empty
     * @return A simple representation of the newly created object
     * @throws ClassNotFoundException Thrown if the object's class can't be found
     * @throws ObjectNotFoundException Thrown if the parent id is not found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * @throws NotAuthorizedException If the update can't be performed due to permissions
     * @throws ArraySizeMismatchException Thrown when the attributeNames and AttributeValues arrays have different sizes
     */
    public RemoteObjectLight createObject(String className, Long parentOid,
            List<String> attributeNames, List<String> attributeValues,String template)
            throws ClassNotFoundException, ObjectNotFoundException, ArraySizeMismatchException,
                NotAuthorizedException, OperationNotPermittedException;
    /**
     * Gets the detailed information about an object
     * @param className Object class name
     * @param oid Object's oid
     * @return A detailed representation of the requested object
     * @throws ClassNotFoundException If the className class can't be found
     * @throws ObjectNotFoundException If the requested object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * @throws NotAuthorizedException If the update can't be performed due to permissions
     */
    public RemoteObject getObjectInfo(String className, Long oid)
            throws ClassNotFoundException, ObjectNotFoundException, NotAuthorizedException,
                    OperationNotPermittedException;

    /**
     * Gets the simplified information about an object
     * @param className Object class name
     * @param oid Object's oid
     * @return A detailed representation of the requested object
     * @throws ClassNotFoundException If the className class can't be found
     * @throws ObjectNotFoundException If the requested object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * @throws NotAuthorizedException If the update can't be performed due to permissions
     */
    public RemoteObjectLight getObjectInfoLight(String className, Long oid)
            throws ClassNotFoundException, ObjectNotFoundException, NotAuthorizedException,
                    OperationNotPermittedException;

    /**
     * Deletes an object
     * @param oid object's id
     * @return Success of failure
     * @throws ObjectWithRelationsException If the requested object or one of it's children have
     * relationships that should be released manually before to delete them
     * @throws ObjectNotFoundException If the requested object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * @throws NotAuthorizedException If the update can't be performed due to permissions
     * or it is blocked
     */
    public boolean deleteObject(Long oid)
            throws ObjectWithRelationsException, ObjectNotFoundException, 
                OperationNotPermittedException, NotAuthorizedException;


    /**
     * Updates an object attributes. Note that you can't set one-to-many or binary attributes through this
     * method. Use setBinaryAttributes and setManyToManyAttribute instead.
     * @param className Object class name
     * @param oid Object's oid
     * @param attributeNames The attributes to be updated
     * @param attributeValues The attribute values
     * @return Success or failure
     * @throws ClassNotFoundException If the object class can't be found
     * @throws ObjectNotFoundException If the object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * @throws NotAuthorizedException If the update can't be performed due to permissions
     * @throws ArraySizeMismatchException If the arrays attributeNames and attributeValues have different lengths
     * @throws InvalidArhumentException If any of the names provided does not exist or can't be set using this method
     */
    public boolean updateObject(String className, Long oid, List<String> attributeNames,List<String> attributeValues)
            throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException,
                ArraySizeMismatchException,WrongMappingException, InvalidArgumentException,NotAuthorizedException;

    /**
     * Updates an object binary attributes.
     * @param className Object's class name
     * @param oid Object's oid
     * @param attributeNames The attributes to be updated
     * @param attributeValues The attribute values
     * @return Success or failure
     * @throws ClassNotFoundException If the object class can't be found
     * @throws ObjectNotFoundException If the object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due a business rule or because the object is blocked
     * @throws NotAuthorizedException If the update can't be performed due to permissions
     * @throws ArraySizeMismatchException If the arrays attributeNames and attributeValues have different lengths
     */
    public boolean setBinaryAttributes(String className, Long oid, List<String> attributeNames, List<byte[]> attributeValues)
            throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException,
                ArraySizeMismatchException, NotAuthorizedException;

    /**
     * Updates an object many-to-one, one-to-many and many-to-may type of attribute.
     * @param className Object's class name
     * @param oid Object's oid
     * @param attributeTypeClassName The class where the values are instance of
     * @param attributeName The attribute to be updated
     * @param attributeValues The attribute value(s) (given as oids)
     * @return Success or failure
     * @throws ClassNotFoundException If the object class or the list type class can't be found
     * @throws ObjectNotFoundException If the object can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     * @throws NotAuthorizedException If the update can't be performed due to permissions
     */
    public boolean setManyToManyAttribute(String className, Long oid, String attributeTypeClassName, String attributeName, List<Long> attributeValues)
            throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException, NotAuthorizedException;

    /**
     * Move a list of objects to a new parent: this methods ignores those who can't be moved and raises
     * an OperationNotPermittedException, however, it will move those which can be moved
     * @param classNames List if Objects' class names
     * @param oids List of objects' oids
     * @param targetClassName Parent's class name
     * @param targetOid Parent's oid
     * @return Success or failure
     * @throws ClassNotFoundException If the object's or new parent's class can't be found
     * @throws ObjectNotFoundException If the object or its new parent can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     * @throws NotAuthorizedException If the update can't be performed due to permissions
     * @throws ArraySizeMismatchException If the oids and classNames array sizes do not match
     */

    public boolean moveObjects(List<String> classNames, List<Long> oids, String targetClassName, Long targetOid)
            throws ClassNotFoundException, ObjectNotFoundException, 
                 OperationNotPermittedException, NotAuthorizedException, ArraySizeMismatchException;

    /**
     *
     * @param objectClassNames Class names for objects to be copied
     * @param templateOids Oids for objects to be copied
     * @param targetClassName Target parent's class name
     * @param targetOid Target parent's oid
     * @return A list containing the newly created objects
     * @throws ClassNotFoundException If any of the provided classes couldn't be found
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws NotAuthorizedException
     * @throws ArraySizeMismatchException
     */
    public RemoteObjectLight[] copyObjects(List<String> objectClassNames, List<Long> templateOids, String targetClassName, Long targetOid)
            throws ClassNotFoundException, ObjectNotFoundException,
                 OperationNotPermittedException, NotAuthorizedException, ArraySizeMismatchException;

    /**
     * Locks and object read-only or release the block
     * @param className object's class name
     * @param oid object's oid
     * @param value true to set the block, false to release it
     * @return Success or failure
     * @throws ClassNotFoundException If the object's can't be found
     * @throws ObjectNotFoundException If the object or its new parent can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     * @throws NotAuthorizedException If the update can't be performed due to permissions
     */
    public boolean setObjectLockSate(String className, Long oid, Boolean value)
            throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException, NotAuthorizedException;

    /**
     * Gets the children of a given object
     * @param className Object's class name
     * @param oid Object's oid
     * @return The list of children
     * @throws ClassNotFoundException If the object's can't be found
     * @throws ObjectNotFoundException If the object or its new parent can't be found
     * @throws OperationNotPermittedException If the update can't be performed due to a business rule
     * @throws NotAuthorizedException If the query can't be performed due to permissions
     */
    public RemoteObjectLight[] getObjectChildren(String className, Long oid)
            throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException, NotAuthorizedException;
    
    /**
     * Executes a query
     * @return The list of results
     * @throws ClassNotFoundException If any of the classes used as based for the search do not exist
     * @throws NotAuthorizedException If the query can't be performed due to permissions
     */
    public List<ResultRecord> executeQuery()
            throws ClassNotFoundException, NotAuthorizedException;

}

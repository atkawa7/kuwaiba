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
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.InterfaceMetadata;

/**
 * Manages the metadata entities
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface MetadataEntityManager {

    /**
     * Creates a classmetadata with its:
     * attributes(some new attributes and others extedended from the parent).
     * category (if the category does not exist it will be create).
     * @param classDefinition
     * @return the Id of the newClassMetadata
     * @throws MetadataObjectNotFoundException if the specified parent class doesn't exist
     * @throws DatabaseException if the reference node doesn't exist
     */
    public long createClass(ClassMetadata classDefinition) throws DatabaseException, MetadataObjectNotFoundException;

    /**
     * Changes a classmetadata definiton
     * @param newClassDefinition
     * @throws ClassNotFoundException if there is no class with such classId
     */
    public void changeClassDefinition(ClassMetadata newClassDefinition) throws Exception;

    /**
     * Deletes a classmetadata, its attributes and category relationships
     * @param classId
     * @throws ClassNotFoundException if there is not a class with de ClassId
     */
    public void deleteClass(String className) throws Exception;

    /**
     * Deletes a classmetadata, its attributes and category relationships
     * @param classId
     * @throws ClassNotFoundException if there is not a class with de ClassName
     */
    public void deleteClass(long classId) throws Exception;
    
    /**
     * Retrieves the simplified list of classes. This list won't include either
     * those classes marked as dummy
     * @param includeListTypes boolean to indicate if the list should include
     * the subclasses of GenericObjectList
     * @return the list of classes
     * @throws Exception EntityManagerNotAvailableException or something unexpected
     */
    public List<ClassMetadataLight> getLightMetadata(boolean includeListTypes, boolean includeIndesign) throws Exception;
    
    /**
     * Gets the subclasses of a given class
     * @param className Class name
     * @param includeAbstractClasses Should the list include the abstract subclasses
     * @param includeSelf Should the list include the subclasses and the parent class?
     * @return The list of subclasses
     * @throws MetadataObjectNotFoundException If the class can not be found
     * @throws InvalidArgumentException If the provided class is not a subclass of InventoryObject
     */
    public List<ClassMetadataLight> getLightSubClasses(String className, boolean includeAbstractClasses, boolean includeSelf) throws MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * Retrieves all the class metadata except for classes marked as dummy
     * @param includeListTypes boolean to indicate if the list should include
     * the subclasses of GenericObjectList
     * @return An array of classes
     */
    public List<ClassMetadata> getMetadata(boolean includeListTypes, boolean includeIndesign) throws Exception;

    /**
     * Gets a classmetadata, its attributes and Category
     * @param className
     * @return A ClassMetadata with the className
     * @throws ClassNotFoundException there is no class with such className
     */
    public ClassMetadata getClass(String className) throws Exception;

    /**
     * Gets a classmetadata, its attributes and Category
     * @param classId
     * @return A ClassMetadata with the classId
     * @throws ClassNotFoundException there is no class with such classId
     */
    public ClassMetadata getClass(long classId) throws Exception;

    /**
     * Moves a class from one parentClass to an other parentClass
     * @param classToMoveName
     * @param targetParentClassName
     * @throws ClassNotFoundException if there is no a classToMove with such name
     * or if there is no a targetParentClass with such name
     */
    public void moveClass(String classToMoveName, String targetParentName) throws Exception;

    /**
     * Moves a class from one parentClass to an other parentClass
     * @param classToMoveId
     * @param targetParentClassId
     * @throws ClassNotFoundException if there is no a classToMove with such classId
     * or if there is no a targetParentClass with such classId
     */
    public void moveClass(long classToMoveId, long targetParentId) throws Exception;

    /**
     * Adds an attribute to the class
     * @param className
     * @param attributeDefinition
     * @throws ClassNotFoundException if there is no a class with such className
     */
    public void addAttribute(String className, AttributeMetadata attributeDefinition) throws MetadataObjectNotFoundException, OperationNotPermittedException;

    /**
     * Adds an attribute to a class
     * @param classId
     * @param attributeDefinition
     * @throws ClassNotFoundException if there is no a class with such classId
     */
    public void addAttribute(long classId, AttributeMetadata attributeDefinition) throws MetadataObjectNotFoundException, OperationNotPermittedException;

    /**
     * Gets an attribute belonging to a class
     * @param className
     * @param attributeName
     * @return AttributeMetada, null if there is no attribute with such name
     * @throws ClassNotFoundException if there is no a class with such className
     * @throws MiscException if the attributeName does not exist
     */
    public AttributeMetadata getAttribute(String className, String attributeName) throws Exception;

    /**
     * Gets an attribute belonging to a class
     * @param classId
     * @param attributeName
     * @return AttributeMetada, null if there is no attribute with such name
     * @throws ClassNotFoundException if there is no a class with such classId
     * @throws MiscException if the attributeName does not exist
     */
    public AttributeMetadata getAttribute(long classId, String attributeName) throws Exception;

    /**
     * Changes an attribute definition belonging to a classMetadata
     * @param ClassId
     * @param newAttributeDefinition
     */
    public void changeAttributeDefinition(long ClassId, AttributeMetadata newAttributeDefinition) throws Exception;

    /**
     * Deletes an attribute belonging to a classMetadata
     * @param className
     * @param attributeName
     */
    public void  deleteAttribute(String className, String attributeName) throws Exception;

    /**
     * Deletes an attribute belonging to a classMetadata
     * @param classId
     * @param attributeName
     */
    public void deleteAttribute(long classId,String attributeName) throws Exception;

    /**
     * Creates a new category
     * @param categoryDefinition
     * @return CategoryId
     */
    public long createCategory(CategoryMetadata categoryDefinition) throws Exception;

    /**
     * Gets a Category with it's name
     * @param categoryName
     * @return CategoryMetadata
     * @throws MiscException if the Category does not exist
     */
    public CategoryMetadata getCategory(String categoryName) throws Exception;

    /**
     * Gets a Category with it's Id
     * @param categoryId
     * @return CategoryMetadata
     */
    public CategoryMetadata getCategory(int categoryId) throws Exception;

    /**
     * Changes a category definition
     * @param categoryDefinition
     * @return true if success
     */
    public void changeCategoryDefinition(CategoryMetadata categoryDefinition) throws Exception;

    /**
     * Deletes a category definition Still don't know what to do with the clasess
     * @param categoryDefinition
     */
    public void deleteCategory(String categoryName) throws Exception;
    public void deleteCategory(int categoryId) throws Exception;
    public void addImplementor(String classWhichImplementsName,String interfaceToImplementName) throws Exception;
    public void removeImplementor(String classWhichImplementsName ,String interfaceToBeRemovedName) throws Exception;
    public void addImplementor(int classWhichImplementsId, int interfaceToImplementId) throws Exception;
    public void removeImplementor(int classWhichImplementsId ,int interfaceToBeRemovedId) throws Exception;
    public InterfaceMetadata getInterface(String interfaceName) throws Exception;
    public InterfaceMetadata getInterface(int interfaceid) throws Exception;

    /**
     * Gets all classes whose instances can be contained into the given parent class. This method
     * is recursive, so the result include the possible children in children classes
     * @param parentClass
     * @return an array with the list of classes
     */
    public List<ClassMetadataLight> getPossibleChildren(String parentClassName) throws MetadataObjectNotFoundException;

    /**
     * Same as getPossibleChildren but this one only gets the possible children for the given class,
     * this is, subclasses are not included
     * @param parentClass
     * @return The list of possible children
     */
    public List<ClassMetadataLight> getPossibleChildrenNoRecursive(String parentClassName) throws MetadataObjectNotFoundException;

    /**
     * Adds to a given class a list of possible children classes whose instances can be contained
     *
     * @param parentClassId Id of the class whose instances can contain the instances of the next param
     * @param _possibleChildren ids of the candidates to be contained
     * @throws MetadataObjectNotFoundException if any of the possible children or the parent don't exist
     * @throws InvalidArgumentException
     * @throws DatabaseException if the reference node doesn't exist
     */
    public void addPossibleChildren(long parentClassId, long[] possibleChildren) throws MetadataObjectNotFoundException, InvalidArgumentException, DatabaseException;
    /**
     * Adds to a given class a list of possible children classes whose instances can be contained using the class name to find the parent class
     * @param parentClassName parent class name
     * @param newPossibleChildren list of possible children
     * @throws MetadataObjectNotFoundException if the parent class or any of the possible children can not be found
     * @throws InvalidArgumentException if any of the given possible children can not be a possible children of parentClassName
     */
    public void addPossibleChildren(String parentClassName, String[] possibleChildren) throws MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * The opposite of addPossibleChildren. It removes the given possible children
     * TODO: Make this method safe. This is, check if there's already intances of the given
     * "children to be deleted" with parentClass as their parent
     * @param parentClassId Id of the class whos instances can contain the instances of the next param
     * @param childrenTBeRemoved ids of the candidates to be deleted
     * @throws MetadataObjectNotFoundException If any of the ids provided can't be found
     */
    public void removePossibleChildren(long parentClassId, long[] childrenToBeRemoved) throws MetadataObjectNotFoundException;
    /**
     * Assess if a given class is subclass of another
     * @param allegedParent Alleged super class
     * @param classToBeEvaluated class to be evaluated
     * @return True if classToBeEvaluated is subclass of allegedParent
     */
    public boolean isSubClass(String allegedParent, String classToBeEvaluated);
    /**
     * Get the upstream containment hierarchy for a given class, unlike getPossibleChildren (which will give you the 
     * downstream hierarchy).
     * @param className
     * @param recursive Get only the direct possible parents, or go up into the <strong>containment</strong> hierarchy. Beware: don't mistake the class hierarchy for the containment one
     * @return An ordered list with the . Repeated elements are omitted
     * @throws MetadataObjectNotFoundException if className does not correspond to any existing class
     */
    public List<ClassMetadataLight> getUpstreamContainmentHierarchy(String className, boolean recursive) throws MetadataObjectNotFoundException;
}

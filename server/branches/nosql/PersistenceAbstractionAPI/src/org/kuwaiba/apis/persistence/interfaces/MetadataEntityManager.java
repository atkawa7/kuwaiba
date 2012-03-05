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

import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;

/**
 * Manages the metadata entities
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface MetadataEntityManager {

    /**
     * Creates a classmetadata with their:
     * attributes(some new attributes and others extedended from the parent).
     * category (if the category does not exist it will be create).
     * @param classDefinition
     * @return the Id of the newClassMetadata
     * @throws ClassNotFoundException if there's no Parent Class whit the ParentId
     */
    public Long createClass(ClassMetadata classDefinition) throws Exception;

    /**
     * Changes a classmetadata definiton
     * @param newClassDefinition
     * @return true if success
     * @throws ClassNotFoundException if there is no class with such classId
     */
    public boolean changeClassDefinition(ClassMetadata newClassDefinition) throws Exception;

    /**
     * Deletes a classmetadata, their attributes and category relationships
     * @param classId
     * @return true if success
     * @throws ClassNotFoundException if there is not a class with de ClassId
     */
    public boolean deleteClass(String className) throws Exception;

    /**
     * Deletes a classmetadata, their attributes and category relationships
     * @param classId
     * @return true if success
     * @throws ClassNotFoundException if there is not a class with de ClassName
     */
    public boolean deleteClass(Long classId) throws Exception;

    /**
     * Gets a classmetadata, their attributes and Category
     * @param className
     * @return A ClassMetadata with the className
     * @throws ClassNotFoundException there is no class with such className
     */
    public ClassMetadata getClass(String className) throws Exception;

    /**
     * Gets a classmetadata, their attributes and Category
     * @param classId
     * @return A ClassMetadata with the classId
     * @throws ClassNotFoundException there is no class with such classId
     */
    public ClassMetadata getClass(Long classId) throws Exception;

    /**
     * Moves a class from one parentClass to an other parentClass
     * @param classToMoveName
     * @param targetParentClassName
     * @return true if success
     * @throws ClassNotFoundException if there is no a classToMove with such name
     * or if there is no a targetParentClass with such name
     */
    public boolean moveClass(String classToMoveName, String targetParentName) throws Exception;

    /**
     * Moves a class from one parentClass to an other parentClass
     * @param classToMoveId
     * @param targetParentClassId
     * @return true if success
     * @throws ClassNotFoundException if there is no a classToMove with such classId
     * or if there is no a targetParentClass with such classId
     */
    public boolean moveClass(Long classToMoveId, Long targetParentId) throws Exception;

    /**
     * Adds an attribute to the class
     * @param className
     * @param attributeDefinition
     * @return true if success
     * @throws ClassNotFoundException if there is no a class with such className
     */
    public boolean addAttribute(String className, AttributeMetadata attributeDefinition) throws Exception;

    /**
     * Adds an attribute to a class
     * @param classId
     * @param attributeDefinition
     * @return true if success
     * @throws ClassNotFoundException if there is no a class with such classId
     */
    public boolean addAttribute(Long classId, AttributeMetadata attributeDefinition) throws Exception;

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
    public AttributeMetadata getAttribute(Long classId, String attributeName) throws Exception;

    /**
     * Changes an attribute definition belonging to a classMetadata
     * @param ClassId
     * @param newAttributeDefinition
     * @return
     */
    public boolean changeAttributeDefinition(Long ClassId, AttributeMetadata newAttributeDefinition) throws Exception;

    /**
     * Deletes an attribute belonging to a classMetadata
     * @param className
     * @param attributeName
     * @return true if success
     * @throws ClassNotFoundException if there is no a class with such className
     * @throws MiscException if the attributeName does not exist
     */
    public boolean  deleteAttribute(String className, String attributeName) throws Exception;

    /**
     * Deletes an attribute belonging to a classMetadata
     * @param classId
     * @param attributeName
     * @return true if success
     * @throws ClassNotFoundException if there is no a class with such classId
     * @throws MiscException if the attributeName does not exist
     */
    public boolean deleteAttribute(Long classId,String attributeName) throws Exception;

    /**
     * Creates a new category
     * @param categoryDefinition
     * @return CategoryId
     */
    public Long createCategory(CategoryMetadata categoryDefinition) throws Exception;

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
     * @throws MiscException if there is no Category with such cetegoryId
     */
    public CategoryMetadata getCategory(Integer categoryId) throws Exception;

    /**
     * Changes a category definition
     * @param categoryDefinition
     * @return true if success
     * @throws MiscException if there is no Category with such cetegoryId
     */
    public boolean changeCategoryDefinition(CategoryMetadata categoryDefinition) throws Exception;

    /**
     * Deletes a category definition Still don't know what to do with the clasess
     * @param categoryDefinition
     * @return true if success
     * @throws MiscException if there is no Category with such cetegoryId
     */
    public boolean deleteCategory(String categoryName) throws Exception;
    public boolean deleteCategory(Integer categoryId) throws Exception;
    public boolean addImplementor(String classWhichImplementsName,String interfaceToImplementName) throws Exception;
    public boolean removeImplementor(String classWhichImplementsName ,String interfaceToBeRemovedName) throws Exception;
    public boolean addImplementor(Integer classWhichImplementsId, Integer interfaceToImplementId) throws Exception;
    public boolean removeImplementor(Integer classWhichImplementsId ,Integer interfaceToBeRemovedId) throws Exception;
    public boolean getInterface(String interfaceName) throws Exception;
    public boolean getInterface(Integer interfaceid) throws Exception;

}

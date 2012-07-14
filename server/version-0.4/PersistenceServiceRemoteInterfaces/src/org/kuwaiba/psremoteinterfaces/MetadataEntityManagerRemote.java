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

package org.kuwaiba.psremoteinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.InterfaceMetadata;

/**
 * RMI wrapper for the MetadataEntityManager interface
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public interface MetadataEntityManagerRemote extends Remote{
    public static final String REFERENCE_MEM = "mem";
    /**
     * See Persistence Abstraction API documentation
     * @param classDefinition
     * @return
     * @throws RemoteException, Exception
     */
    public Long createClass(ClassMetadata classDefinition) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param newClassDefinition
     * @return
     * @throws RemoteException, Exception
     */
    public void changeClassDefinition(ClassMetadata newClassDefinition) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param className
     * @throws RemoteException, Exception
     */
    public void deleteClass(String className) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classId
     * @throws RemoteException, Exception
     */
    public void deleteClass(Long classId) throws RemoteException, MetadataObjectNotFoundException;

    /**
     * Retrieves the simplified list of classes. This list won't include either
     * those classes marked as dummy
     * @param includeListTypes boolean to indicate if the list should include
     * the subclasses of GenericObjectList
     * @return the list of classes
     * @throws Exception EntityManagerNotAvailableException or something unexpected
     */
    public List<ClassMetadataLight> getLightMetadata(Boolean includeListTypes) throws RemoteException, MetadataObjectNotFoundException;

    /**
     * Retrieves all the class metadata except for classes marked as dummy
     * @param includeListTypes boolean to indicate if the list should include
     * the subclasses of GenericObjectList
     * @return An array of classes
     */
    public List<ClassMetadata> getMetadata(Boolean includeListTypes) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param className
     * @return
     * @throws RemoteException, Exception
     */
    public ClassMetadata getClass(String className) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classId
     * @return
     * @throws RemoteException, Exception
     */
    public ClassMetadata getClass(Long classId) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classToMoveName
     * @param targetParentName
     * @return
     * @throws RemoteException, Exception
     */
    public void moveClass(String classToMoveName, String targetParentName) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classToMoveId
     * @param targetParentId
     * @return
     * @throws RemoteException, Exception
     */
    public void moveClass(Long classToMoveId, Long targetParentId) throws RemoteException, MetadataObjectNotFoundException;
    
    /**
     * Set a class icon (big or small)
     * @param classId
     * @param attributeName
     * @param iconImage
     * @return
     */
    public void setClassIcon(Long classId, String attributeName, byte[] iconImage) throws RemoteException, MetadataObjectNotFoundException;
    
    /**
     * See Persistence Abstraction API documentation
     * @param className
     * @param attributeDefinition
     * @return
     * @throws RemoteException, Exception
     */
    public void addAttribute(String className, AttributeMetadata attributeDefinition) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classId
     * @param attributeDefinition
     * @return
     * @throws RemoteException, Exception
     */
    public void addAttribute(Long classId, AttributeMetadata attributeDefinition) throws RemoteException, MetadataObjectNotFoundException;

    /**
     * See Persistence Abstraction API documentation
     * @param className
     * @param attributeName
     * @return
     * @throws RemoteException, Exception
     */
    public AttributeMetadata getAttribute(String className, String attributeName) throws RemoteException, MetadataObjectNotFoundException;

    /**
     * See Persistence Abstraction API documentation
     * @param classId
     * @param attributeName
     * @return
     * @throws RemoteException, Exception
     */
    public AttributeMetadata getAttribute(Long classId, String attributeName) throws RemoteException, MetadataObjectNotFoundException;

    /**
     * See Persistence Abstraction API documentation
     * @param ClassId
     * @param newAttributeDefinition
     * @throws RemoteException, Exception
     */
    public void setAttributePropertyValue(Long classId, String attributeName,
            String propertyName, String propertyValue) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classId
     * @param attributeName
     * @return
     * @throws RemoteException, Exception
     */
    public void changeAttributeDefinition(Long ClassId, AttributeMetadata newAttributeDefinition) throws RemoteException, MetadataObjectNotFoundException;

    /**
     * See Persistence Abstraction API documentation
     * @param classId
     * @param attributeName
     * @return
     * @throws RemoteException, Exception
     */
    public void setClassPlainAttribute(Long classId, String attributeName, String attributeValue) throws RemoteException, MetadataObjectNotFoundException;

    /**
     * See Persistence Abstraction API documentation
     * @param className
     * @param attributeName
     * @throws RemoteException, Exception
     */
    public void  deleteAttribute(String className, String attributeName) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classId
     * @param attributeName
     * @throws RemoteException, Exception
     */
    public void deleteAttribute(Long classId,String attributeName) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param categoryDefinition
     * @return
     * @throws RemoteException, Exception
     */
    public Long createCategory(CategoryMetadata categoryDefinition) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param categoryName
     * @return
     * @throws RemoteException, Exception
     */
    public CategoryMetadata getCategory(String categoryName) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param categoryId
     * @return
     * @throws RemoteException, Exception
     */
    public CategoryMetadata getCategory(Integer categoryId) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param categoryDefinition
     */
    public void changeCategoryDefinition(CategoryMetadata categoryDefinition) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param categoryName
     * @throws RemoteException, Exception
     */
    public void deleteCategory(String categoryName) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param categoryId
     * @throws RemoteException, Exception
     */
    public void deleteCategory(Integer categoryId) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classWhichImplementsName
     * @param interfaceToImplementName
     * @throws RemoteException, Exception
     */
    public void addImplementor(String classWhichImplementsName,String interfaceToImplementName) throws RemoteException, MetadataObjectNotFoundException;
    /**
     *
     * @param classWhichImplementsName
     * @param interfaceToBeRemovedName
     * @throws RemoteException, Exception
     */
    public void removeImplementor(String classWhichImplementsName ,String interfaceToBeRemovedName) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classWhichImplementsId
     * @param interfaceToImplementId
     * @throws RemoteException, Exception
     */
    public void addImplementor(Integer classWhichImplementsId, Integer interfaceToImplementId) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classWhichImplementsId
     * @param interfaceToBeRemovedId
     * @throws RemoteException, Exception
     */
    public void removeImplementor(Integer classWhichImplementsId ,Integer interfaceToBeRemovedId) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param interfaceName
     * @return The requested interface metadata
     * @throws RemoteException, Exception
     */
    public InterfaceMetadata getInterface(String interfaceName) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param interfaceid
     * @return The requested interface metadata
     * @throws RemoteException, Exception
     */
    public InterfaceMetadata getInterface(Integer interfaceid) throws RemoteException, MetadataObjectNotFoundException;

    /**
     * Gets all classes whose instances can be contained into the given parent class. This method
     * is recursive, so the result include the possible children in children classes
     * @param parentClass
     * @return an array with the list of classes
     */
    public List<ClassMetadataLight> getPossibleChildren(String parentClassName) throws RemoteException, MetadataObjectNotFoundException;

    /**
     * Same as getPossibleChildren but this one only gets the possible children for the given class,
     * this is, subclasses are not included
     * @param parentClass
     * @return The list of possible children
     */
    public List<ClassMetadataLight> getPossibleChildrenNoRecursive(String parentClassName) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * Adds to a given class a list of possible children classes whose instances can be contained
     *
     * @param parentClassId Id of the class whose instances can contain the instances of the next param
     * @param _possibleChildren ids of the candidates to be contained
     */
    public void addPossibleChildren(Long parentClassId, Long[] _possibleChildren) throws RemoteException, MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * Adds to a given class a list of possible children classes whose instances can be contained
     * @param parentClassName
     * @param possibleChildren
     * @throws RemoteException
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException
     */
    public void addPossibleChildren(String parentClassName, String[] possibleChildren) throws RemoteException, MetadataObjectNotFoundException, InvalidArgumentException;

    /**
     * The opposite of addPossibleChildren. It removes the given possible children
     * TODO: Make this method safe. This is, check if there's already intances of the given
     * "children to be deleted" with parentClass as their parent
     * @param parentClassId Id of the class whos instances can contain the instances of the next param
     * @param childrenTBeRemoved ids of the candidates to be deleted
     */
    public void removePossibleChildren(Long parentClassId, Long[] childrenToBeRemoved) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * Assess if a given class is subclass of another
     * @param allegedParent Alleged super class
     * @param classToBeEvaluated class to be evaluated
     * @return True if classToBeEvaluated is subclass of allegedParent
     */
    public boolean isSubClass(String allegedParent, String classToBeEvaluated) throws RemoteException;
}

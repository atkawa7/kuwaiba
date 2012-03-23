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
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;

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
    public boolean changeClassDefinition(ClassMetadata newClassDefinition) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param className
     * @return
     * @throws RemoteException, Exception
     */
    public boolean deleteClass(String className) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classId
     * @return
     * @throws RemoteException, Exception
     */
    public boolean deleteClass(Long classId) throws RemoteException, MetadataObjectNotFoundException;

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
    public boolean moveClass(String classToMoveName, String targetParentName) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classToMoveId
     * @param targetParentId
     * @return
     * @throws RemoteException, Exception
     */
    public boolean moveClass(Long classToMoveId, Long targetParentId) throws RemoteException, MetadataObjectNotFoundException;
    
    /**
     * Set a class icon (big or small)
     * @param classId
     * @param attributeName
     * @param iconImage
     * @return
     */
    public Boolean setClassIcon(Long classId, String attributeName, byte[] iconImage) throws RemoteException, MetadataObjectNotFoundException;
    
    /**
     * See Persistence Abstraction API documentation
     * @param className
     * @param attributeDefinition
     * @return
     * @throws RemoteException, Exception
     */
    public boolean addAttribute(String className, AttributeMetadata attributeDefinition) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classId
     * @param attributeDefinition
     * @return
     * @throws RemoteException, Exception
     */
    public boolean addAttribute(Long classId, AttributeMetadata attributeDefinition) throws RemoteException, MetadataObjectNotFoundException;
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
     * @return
     * @throws RemoteException, Exception
     */
    public boolean changeAttributeDefinition(Long ClassId, AttributeMetadata newAttributeDefinition) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param className
     * @param attributeName
     * @return
     * @throws RemoteException, Exception
     */
    public boolean  deleteAttribute(String className, String attributeName) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classId
     * @param attributeName
     * @return
     * @throws RemoteException, Exception
     */
    public boolean deleteAttribute(Long classId,String attributeName) throws RemoteException, MetadataObjectNotFoundException;
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
     * @return
     */
    public boolean changeCategoryDefinition(CategoryMetadata categoryDefinition) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param categoryName
     * @return
     * @throws RemoteException, Exception
     */
    public boolean deleteCategory(String categoryName) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param categoryId
     * @return
     * @throws RemoteException, Exception
     */
    public boolean deleteCategory(Integer categoryId) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classWhichImplementsName
     * @param interfaceToImplementName
     * @return
     * @throws RemoteException, Exception
     */
    public boolean addImplementor(String classWhichImplementsName,String interfaceToImplementName) throws RemoteException, MetadataObjectNotFoundException;
    /**
     *
     * @param classWhichImplementsName
     * @param interfaceToBeRemovedName
     * @return
     * @throws RemoteException, Exception
     */
    public boolean removeImplementor(String classWhichImplementsName ,String interfaceToBeRemovedName) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classWhichImplementsId
     * @param interfaceToImplementId
     * @return
     * @throws RemoteException, Exception
     */
    public boolean addImplementor(Integer classWhichImplementsId, Integer interfaceToImplementId) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param classWhichImplementsId
     * @param interfaceToBeRemovedId
     * @return
     * @throws RemoteException, Exception
     */
    public boolean removeImplementor(Integer classWhichImplementsId ,Integer interfaceToBeRemovedId) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param interfaceName
     * @return
     * @throws RemoteException, Exception
     */
    public boolean getInterface(String interfaceName) throws RemoteException, MetadataObjectNotFoundException;
    /**
     * See Persistence Abstraction API documentation
     * @param interfaceid
     * @return
     * @throws RemoteException, Exception
     */
    public boolean getInterface(Integer interfaceid) throws RemoteException, MetadataObjectNotFoundException;

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
     * @return success or failure
     */
    public Boolean addPossibleChildren(Long parentClassId, Long[] _possibleChildren) throws RemoteException, MetadataObjectNotFoundException;

    /**
     * The opposite of addPossibleChildren. It removes the given possible children
     * TODO: Make this method safe. This is, check if there's already intances of the given
     * "children to be deleted" with parentClass as their parent
     * @param parentClassId Id of the class whos instances can contain the instances of the next param
     * @param childrenTBeRemoved ids of the candidates to be deleted
     * @return success or failure
     */
    public Boolean removePossibleChildren(Long parentClassId, Long[] childrenToBeRemoved) throws RemoteException, MetadataObjectNotFoundException;

}

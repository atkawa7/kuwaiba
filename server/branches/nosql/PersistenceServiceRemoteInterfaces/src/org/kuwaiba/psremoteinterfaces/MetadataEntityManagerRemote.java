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
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;

/**
 * RMI wrapper for the MetadataEntityManager interface
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public interface MetadataEntityManagerRemote extends Remote{

    public Long createClass(ClassMetadata classDefinition) throws RemoteException;
    public boolean changeClassDefinition(ClassMetadata newClassDefinition) throws RemoteException;
    public boolean deleteClass(String className) throws RemoteException;
    public boolean deleteClass(Long classId) throws RemoteException;
    public ClassMetadata getClass(String className) throws RemoteException;
    public ClassMetadata getClass(Long classId) throws RemoteException;
    public boolean moveClass(String classToMoveName, String targetParentName) throws RemoteException;
    public boolean moveClass(Long classToMoveId, Long targetParentId) throws RemoteException;
    public boolean addAttribute(String className, AttributeMetadata attributeDefinition) throws RemoteException;
    public boolean addAttribute(Long classId, AttributeMetadata attributeDefinition) throws RemoteException;
    public AttributeMetadata getAttribute(String className, String attributeName) throws RemoteException;
    public AttributeMetadata getAttribute(Long classId, String attributeName) throws RemoteException;
    public boolean changeAttributeDefinition(Long ClassId, AttributeMetadata newAttributeDefinition) throws RemoteException;
    public boolean  deleteAttribute(String className, String attributeName) throws RemoteException;
    public boolean deleteAttribute(Long classId,String attributeName) throws RemoteException;
    public Long createCategory(CategoryMetadata categoryDefinition) throws RemoteException;
    public CategoryMetadata getCategory(String categoryName) throws RemoteException;
    public CategoryMetadata getCategory(Integer categoryId) throws RemoteException;
    public boolean changeCategoryDefinition(CategoryMetadata categoryDefinition);
    public boolean deleteCategory(String categoryName) throws RemoteException;
    public boolean deleteCategory(Integer categoryId) throws RemoteException;
    public boolean addImplementor(String classWhichImplementsName,String interfaceToImplementName) throws RemoteException;
    public boolean removeImplementor(String classWhichImplementsName ,String interfaceToBeRemovedName) throws RemoteException;
    public boolean addImplementor(Integer classWhichImplementsId, Integer interfaceToImplementId) throws RemoteException;
    public boolean removeImplementor(Integer classWhichImplementsId ,Integer interfaceToBeRemovedId) throws RemoteException;

    public boolean getInterface(String interfaceName) throws RemoteException;
    public boolean getInterface(Integer interfaceid) throws RemoteException;
}

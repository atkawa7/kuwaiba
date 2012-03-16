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

package org.kuwaiba.beans;

import java.util.HashMap;
import javax.ejb.Remote;
import org.kuwaiba.exceptions.InvalidSessionException;
import org.kuwaiba.exceptions.NotAuthorizedException;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.ws.toserialize.application.RemoteSession;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.metadata.AttributeInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;

/**
 * Main session bean backing the webservice
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Remote
public interface WebServiceBeanRemote {

    // <editor-fold defaultstate="collapsed" desc="Session methods. Click on the + sign on the left to edit the code.">
    /**
     *
     * @param user
     * @param password
     * @param IPAddress
     * @return
     * @throws NotAuthorizedException
     */
    public RemoteSession createSession(String user, String password, String IPAddress) throws NotAuthorizedException;
    /**
     * 
     * @param sessionId
     * @param remoteAddress
     * @return
     */
    public void closeSession(String sessionId, String remoteAddress) throws InvalidSessionException;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Metadata methods. Click on the + sign on the left to edit the code.">
    public Long createClass(ClassInfo classDefinition) throws Exception;

    /**
     * Changes a classmetadata definition
     * @param newClassDefinition
     * @return true if success
     * @throws ClassNotFoundException if there is no class with such classId
     */
//    public boolean changeClassDefinition(ClassMetadata newClassDefinition) throws Exception;
//
//    /**
//     * Deletes a classmetadata, its attributes and category relationships
//     * @param classId
//     * @return true if success
//     * @throws ClassNotFoundException if there is not a class with de ClassId
//     */
    
    public boolean deleteClass(String className) throws Exception;

    /**
     * Deletes a classmetadata, its attributes and category relationships
     * @param classId
     * @return true if success
     * @throws ClassNotFoundException if there is not a class with de ClassName
     */
    public boolean deleteClass(Long classId) throws Exception;

    /**
     * Gets a classmetadata, its attributes and Category
     * @param className
     * @return A ClassMetadata with the className
     * @throws ClassNotFoundException there is no class with such className
     */
    public ClassInfo getClass(String className) throws Exception;

    public ClassInfo getClass(Long classId) throws Exception;

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
    public boolean addAttribute(String className, AttributeInfo attributeDefinition) throws Exception;
    /**
     * Adds an attribute to a class
     * @param classId
     * @param attributeDefinition
     * @return true if success
     * @throws ClassNotFoundException if there is no a class with such classId
     */
    public boolean addAttribute(Long classId, AttributeInfo attributeDefinition) throws Exception;
    //
//    /**
//     * Gets an attribute belonging to a class
//     * @param className
//     * @param attributeName
//     * @return AttributeMetada, null if there is no attribute with such name
//     * @throws ClassNotFoundException if there is no a class with such className
//     * @throws MiscException if the attributeName does not exist
//     */
//    public AttributeMetadata getAttribute(String className, String attributeName) throws Exception;
//
//    /**
//     * Gets an attribute belonging to a class
//     * @param classId
//     * @param attributeName
//     * @return AttributeMetada, null if there is no attribute with such name
//     * @throws ClassNotFoundException if there is no a class with such classId
//     * @throws MiscException if the attributeName does not exist
//     */
//    public AttributeMetadata getAttribute(Long classId, String attributeName) throws Exception;
//
//    /**
//     * Changes an attribute definition belonging to a classMetadata
//     * @param ClassId
//     * @param newAttributeDefinition
//     * @return
//     */
//    public boolean changeAttributeDefinition(Long ClassId, AttributeMetadata newAttributeDefinition) throws Exception;
//
//    /**
//     * Deletes an attribute belonging to a classMetadata
//     * @param className
//     * @param attributeName
//     * @return true if success
//     * @throws ClassNotFoundException if there is no a class with such className
//     * @throws MiscException if the attributeName does not exist
//     */
//    public boolean  deleteAttribute(String className, String attributeName) throws Exception;
//
//    /**
//     * Deletes an attribute belonging to a classMetadata
//     * @param classId
//     * @param attributeName
//     * @return true if success
//     * @throws ClassNotFoundException if there is no a class with such classId
//     * @throws MiscException if the attributeName does not exist
//     */
//    public boolean deleteAttribute(Long classId,String attributeName) throws Exception;
//
//    /**
//     * Creates a new category
//     * @param categoryDefinition
//     * @return CategoryId
//     */
//    public Long createCategory(CategoryMetadata categoryDefinition) throws Exception;
//
//    /**
//     * Gets a Category with it's name
//     * @param categoryName
//     * @return CategoryMetadata
//     * @throws MiscException if the Category does not exist
//     */
//    public CategoryMetadata getCategory(String categoryName) throws Exception;
//
//    /**
//     * Gets a Category with it's Id
//     * @param categoryId
//     * @return CategoryMetadata
//     * @throws MiscException if there is no Category with such cetegoryId
//     */
//    public CategoryMetadata getCategory(Integer categoryId) throws Exception;
//
//    /**
//     * Changes a category definition
//     * @param categoryDefinition
//     * @return true if success
//     * @throws MiscException if there is no Category with such cetegoryId
//     */
//    public boolean changeCategoryDefinition(CategoryMetadata categoryDefinition) throws Exception;
//
//    /**
//     * Deletes a category definition Still don't know what to do with the clasess
//     * @param categoryDefinition
//     * @return true if success
//     * @throws MiscException if there is no Category with such cetegoryId
//     */
//    public boolean deleteCategory(String categoryName) throws Exception;
//    public boolean deleteCategory(Integer categoryId) throws Exception;
//    public boolean addImplementor(String classWhichImplementsName,String interfaceToImplementName) throws Exception;
//    public boolean removeImplementor(String classWhichImplementsName ,String interfaceToBeRemovedName) throws Exception;
//    public boolean addImplementor(Integer classWhichImplementsId, Integer interfaceToImplementId) throws Exception;
//    public boolean removeImplementor(Integer classWhichImplementsId ,Integer interfaceToBeRemovedId) throws Exception;
//    public boolean getInterface(String interfaceName) throws Exception;
//    public boolean getInterface(Integer interfaceid) throws Exception;

// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Business methods. Click on the + sign on the left to edit the code.">
    public RemoteObjectLight[] getObjectChildren(Long oid, Long objectClassId) throws ServerSideException;
    public RemoteObjectLight[] getObjectChildren(String objectClassName, Long oid) throws ServerSideException;

    public RemoteObject[] getChildrenOfClass(Long parentOid, String parentClass,String myClass);

    public RemoteObject getObjectInfo(String objectClass, Long oid);

    public RemoteObjectLight getObjectInfoLight(String objectClass, Long oid);

    public void updateObject(String className, Long oid, HashMap<String, String> attributes);

    public RemoteObjectLight createObject(String className, Long parentOid, HashMap<String, String> attributes, String template);

    
    // </editor-fold>

}

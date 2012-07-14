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
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.WrongMappingException;

/**
 * RMI wrapper for the BusinessEntityManager interface
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface BusinessEntityManagerRemote extends Remote{
    public static final String REFERENCE_BEM = "bem";

    public List<RemoteBusinessObjectLight> getObjectChildren(String className, Long oid, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, RemoteException;

    public List<RemoteBusinessObjectLight> getObjectChildren(Long oid, Long classId, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, RemoteException;

    public List<RemoteBusinessObject> getChildrenOfClass(Long parentOid, String parentClass, String myClass, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, RemoteException;

    public List<RemoteBusinessObjectLight> getChildrenOfClassLight(Long parentOid, String parentClass, String myClass, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, RemoteException;

    public RemoteBusinessObject getObjectInfo(String objectClass, Long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;

    public RemoteBusinessObjectLight getObjectInfoLight(String objectClass, Long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, RemoteException;

    public void updateObject(String className, Long oid, HashMap<String,List<String>> attributes)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException,
                WrongMappingException, InvalidArgumentException, RemoteException;
    public Long createObject(String className, String parentClassName, Long parentOid,
            HashMap<String,List<String>> attributes, Long template)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException, RemoteException;
   public Long createSpecialObject(String className, String parentClassName, Long parentOid,
            HashMap<String,List<String>> attributes,Long template)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, RemoteException;

    public void deleteObjects(HashMap<String, List<Long>> objects, boolean releaseRelationships)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, RemoteException;

    public void moveObjects(String targetClass, Long targetOid, HashMap<String, List<Long>> objects)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, RemoteException;

    public List<Long> copyObjects(String targetClass, Long targetOid, HashMap<String, List<Long>> objects, boolean recursive)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, RemoteException;

    public void createSpecialRelationship(String aObjectClass, Long aObjectId, String bObjectClass, Long bObjectId, String name)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, RemoteException;
    public List<String> getSpecialAttribute(String objectClass, Long objectId, String specialAttributeName)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, RemoteException;
}

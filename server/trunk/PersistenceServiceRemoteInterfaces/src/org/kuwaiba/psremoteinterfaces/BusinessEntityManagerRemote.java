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
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
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

    public List<RemoteBusinessObjectLight> getObjectChildren(String className, long oid, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, RemoteException;

    public List<RemoteBusinessObjectLight> getObjectChildren(long oid, long classId, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, RemoteException;

    public List<RemoteBusinessObject> getChildrenOfClass(long parentOid, String parentClass, String myClass, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, RemoteException;

    public List<RemoteBusinessObjectLight> getChildrenOfClassLight(long parentOid, String parentClass, String myClass, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, RemoteException;

    public RemoteBusinessObject getObject(String objectClass, long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;

    public RemoteBusinessObjectLight getObjectLight(String objectClass, long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, RemoteException;
    
    public RemoteBusinessObject getParent(String objectClass, long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;

    public RemoteBusinessObject getParentOfClass(String objectClass, long oid, String parentClass)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;

    public void updateObject(String className, long oid, HashMap<String,List<String>> attributes)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException,
                WrongMappingException, InvalidArgumentException, RemoteException;
    public long createObject(String className, String parentClassName, long parentOid,
            HashMap<String,List<String>> attributes, long template)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException, DatabaseException, RemoteException;
   public long createSpecialObject(String className, String parentClassName, long parentOid,
            HashMap<String,List<String>> attributes,long template)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, DatabaseException, RemoteException;

    public void deleteObjects(HashMap<String, long[]> objects, boolean releaseRelationships)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, RemoteException;

    public void moveObjects(String targetClass, long targetOid, HashMap<String, long[]> objects)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, RemoteException;

    public long[] copyObjects(String targetClass, long targetOid, HashMap<String, long[]> objects, boolean recursive)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, RemoteException;

    public void createSpecialRelationship(String aObjectClass, long aObjectId, String bObjectClass, long bObjectId, String name)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, RemoteException;
    public List<String> getSpecialAttribute(String objectClass, long objectId, String specialAttributeName)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, RemoteException;
}

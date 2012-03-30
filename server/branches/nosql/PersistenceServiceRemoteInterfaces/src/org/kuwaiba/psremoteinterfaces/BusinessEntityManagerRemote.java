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
import org.kuwaiba.apis.persistence.business.RemoteObject;
import org.kuwaiba.apis.persistence.business.RemoteObjectLight;
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

    public List<RemoteObjectLight> getObjectChildren(String className, Long oid)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, RemoteException;

    public List<RemoteObjectLight> getObjectChildren(Long oid, Long classId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, RemoteException;

    public List<RemoteObject> getChildrenOfClass(Long parentOid, String parentClass, String myClass)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, RemoteException;

    public List<RemoteObjectLight> getChildrenOfClassLight(Long parentOid, String parentClass, String myClass)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, RemoteException;

    public RemoteObject getObjectInfo(String objectClass, Long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;

    public RemoteObjectLight getObjectInfoLight(String objectClass, Long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, RemoteException;

    public void updateObject(String className, Long oid, HashMap<String,String> attributes)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException,
                WrongMappingException, InvalidArgumentException, RemoteException;
    public Long createObject(String className, Long parentOid,
            HashMap<String,String> attributes,String template)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, RemoteException;
    public Long createListTypeItem(String className, String name, String displayName)
            throws MetadataObjectNotFoundException, InvalidArgumentException, RemoteException;
    public List<RemoteObjectLight> getListTypeItems(String className)
            throws MetadataObjectNotFoundException, InvalidArgumentException;
}

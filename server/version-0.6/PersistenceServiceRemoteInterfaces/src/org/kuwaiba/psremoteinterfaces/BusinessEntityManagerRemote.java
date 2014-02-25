/*
 *  Copyright 2010 - 2014 Neotropic SAS <contact@neotropic.co>.
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
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.WrongMappingException;

/**
 * RMI wrapper for the BusinessEntityManager interface
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface BusinessEntityManagerRemote extends Remote{
    public static final String REFERENCE_BEM = "bem";

    public List<RemoteBusinessObjectLight> getObjectChildren(String className, long oid, int maxResults, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

    public List<RemoteBusinessObjectLight> getObjectChildren(long oid, long classId, int maxResults, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    
    public List<RemoteBusinessObjectLight> getSiblings(String className, long oid, int maxResults, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    
    public List<RemoteBusinessObjectLight> getObjectsOfClassLight(String className, int maxResults, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

    public List<RemoteBusinessObject> getChildrenOfClass(long parentOid, String parentClass, String myClass, int maxResults, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

    public List<RemoteBusinessObjectLight> getChildrenOfClassLight(long parentOid, String parentClass, String myClass, int maxResults, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

    public RemoteBusinessObject getObject(String objectClass, long oid, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

    public RemoteBusinessObjectLight getObjectLight(String objectClass, long oid, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    
    public RemoteBusinessObject getParent(String objectClass, long oid, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    
    public List<RemoteBusinessObjectLight> getParents (String objectClassName, long oid, String ipAddress, String sessionId)
        throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

    public RemoteBusinessObject getParentOfClass(String objectClass, long oid, String parentClass, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

    public void updateObject(String className, long oid, HashMap<String,List<String>> attributes, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException,
                WrongMappingException, InvalidArgumentException, ApplicationObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    
    public long createObject(String className, String parentClassName, long parentOid,
            HashMap<String,List<String>> attributes, long template, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException, DatabaseException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
   
    public long createSpecialObject(String className, String parentClassName, long parentOid,
            HashMap<String,List<String>> attributes,long template, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, DatabaseException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
   
   public long[] createBulkSpecialObjects(String objectClass, int numberOfObjects, String parentClass, long parentId, String ipAddress, String sessionId)
           throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

    public void deleteObjects(HashMap<String, long[]> objects, boolean releaseRelationships, String ipAddress, String sessionId)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

    public void moveObjects(String targetClass, long targetOid, HashMap<String, long[]> objects, String ipAddress, String sessionId)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

    public long[] copyObjects(String targetClass, long targetOid, HashMap<String, long[]> objects, boolean recursive, String ipAddress, String sessionId)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

    public void createSpecialRelationship(String aObjectClass, long aObjectId, String bObjectClass, long bObjectId, String name, String ipAddress, String sessionId)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    
    public void releaseSpecialRelationship(String objectClass, long objectId, long otherObjectId, String relationshipName, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    
    public void releaseSpecialRelationship(String objectClass, long objectId, String relationshipName, long targetId, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    
    public List<RemoteBusinessObjectLight> getSpecialAttribute(String objectClass, long objectId, String specialAttributeName, String ipAddress, String sessionId) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    
    public HashMap<String,List<RemoteBusinessObjectLight>> getSpecialAttributes (String className, long objectId, String ipAddress, String sessionId) 
        throws MetadataObjectNotFoundException, ObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    
    public List<RemoteBusinessObjectLight> getObjectSpecialChildren(String objectClass, long objectId, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;

    public boolean hasRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships, String ipAddress, String sessionId) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    
    public boolean hasSpecialRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships, String ipAddress, String sessionId) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
    //TO DELETE
    public List<RemoteBusinessObjectLight> getPhysicalPath(String objectClass, long objectId, String ipAddress, String sessionId) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, RemoteException;
}

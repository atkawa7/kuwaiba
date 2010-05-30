/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
 *  under the License.
 */
package businesslogic;

import core.todeserialize.ObjectUpdate;
import core.toserialize.ClassInfo;
import core.toserialize.ClassInfoLight;
import core.toserialize.RemoteObjectLight;
import core.toserialize.RemoteTreeNode;
import javax.ejb.Remote;
import javax.persistence.EntityManager;

/**
 * Interface exposing the methods within BackEndbean
 *
 * @author Charles Edward Bedón Cortázar <charles.bedon@zoho.com>
 */
@Remote
public interface BackendBeanRemote {

    public void createInitialDataset(); //just for testing purposes

    public EntityManager getEntityManager();
    public Long getDummyRootId();
    public RemoteTreeNode getObjectInmediateHierarchy(java.lang.Long oid, String objectClass);
    public core.toserialize.RemoteObject getObjectInfo(java.lang.String objectClass, java.lang.Long oid);
    public boolean updateObject(ObjectUpdate obj) throws core.exceptions.ObjectNotFoundException;
    public java.lang.String getError();
    public boolean setObjectLock(java.lang.Long oid, java.lang.String objectClass, java.lang.Boolean value);
    public RemoteObjectLight[] getObjectChildren(Long oid, Long objectClassId);
    public RemoteObjectLight createObject(String objectClass, Long parentOid, String template);
    public ClassInfo[] getMetadata();
    public ClassInfo getMetadataForClass(String className);
    public void buildMetaModel();
    public core.toserialize.ObjectList getMultipleChoice(java.lang.String className);
    public Boolean addPossibleChildren(Long parentClassId, Long[] possibleChildren);
    public Boolean removePossibleChildren(Long parentClassId, Long[] childrenToBeRemoved);
    public boolean removeObject(Class className, Long oid);
    public ClassInfoLight[] getPossibleChildren(java.lang.Class parentClass);
    public ClassInfoLight[] getPossibleChildrenNoRecursive(Class parentClass);
    public ClassInfoLight[] getRootPossibleChildren();
    public ClassInfoLight[] getLightMetadata();
    public boolean moveObjects(Long targetOid, Long[] objectOids, String[] objectClasses);
    public RemoteObjectLight[] copyObjects(Long targetOid, Long[] templateOids, String[] objectClasses);
    public RemoteObjectLight[] searchForObjects(java.lang.Class searchedClass, String[] paramNames, String [] paramTypes, String[] paramValues);
}

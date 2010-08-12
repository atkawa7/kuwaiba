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
 */
package businesslogic;

import core.todeserialize.ObjectUpdate;
import core.toserialize.ClassInfo;
import core.toserialize.ClassInfoLight;
import core.toserialize.RemoteObjectLight;
import core.toserialize.UserGroupInfo;
import core.toserialize.UserInfo;
import core.toserialize.View;
import java.util.List;
import javax.ejb.Remote;

/**
 * Interface exposing the methods within BackEndbean
 *
 * @author Charles Edward Bedón Cortázar <charles.bedon@zoho.com>
 */
@Remote
public interface BackendBeanRemote {

    public void createInitialDataset(); //just for testing purposes

    public Long getDummyRootId();
    public core.toserialize.RemoteObject getObjectInfo(String objectClass, Long oid);
    public boolean updateObject(ObjectUpdate obj);
    public java.lang.String getError();
    public boolean setObjectLock(Long oid, String objectClass, Boolean value);
    public List getObjectChildren(Long oid, Long objectClassId);
    public RemoteObjectLight createObject(String objectClass, Long parentOid, String template);
    public ClassInfo[] getMetadata();
    public ClassInfo getMetadataForClass(String className);
    public void buildMetaModel();
    public core.toserialize.ObjectList getMultipleChoice(String className);
    public Boolean addPossibleChildren(Long parentClassId, Long[] possibleChildren);
    public Boolean removePossibleChildren(Long parentClassId, Long[] childrenToBeRemoved);
    public boolean removeObject(Class className, Long oid);
    public ClassInfoLight[] getPossibleChildren(Class parentClass);
    public ClassInfoLight[] getPossibleChildrenNoRecursive(Class parentClass);
    public ClassInfoLight[] getRootPossibleChildren();
    public ClassInfoLight[] getLightMetadata();
    public boolean moveObjects(Long targetOid, Long[] objectOids, String[] objectClasses);
    public RemoteObjectLight[] copyObjects(Long targetOid, Long[] templateOids, String[] objectClasses);
    public RemoteObjectLight[] searchForObjects(Class searchedClass, String[] paramNames, String [] paramTypes, String[] paramValues);
    public Boolean setAttributePropertyValue(Long classId, String attributeName, String propertyName, String propertyValue);
    public Boolean setClassPlainAttribute(Long classId, String attributeName, String attributeValue);
    public Boolean setClassIcon(Long classId, String attributeName, byte[] iconImage);
    public ClassInfoLight[] getInstanceableListTypes();
    public boolean createSession(String username, String password);
    public View getDefaultView(Long oid, Class className);
    public View getRoomView(Long oid);
    public View getRackView(Long oid);
    public UserInfo[] getUsers();
    public UserGroupInfo[] getGroups();
    public Boolean setUserProperties(Long oid, String[] propertiesNames, String[] propertiesValues);
    public Boolean setGroupProperties(Long oid, String[] propertiesNames, String[] propertiesValues);
    public Boolean removeUsersFromGroup(Long[] usersOids, Long groupOid);
    public Boolean addUsersToGroup(Long[] usersOids, Long groupOid);
    public UserInfo createUser();
    public Boolean deleteUsers(java.lang.Long[] oids);
    public Boolean addGroupsToUser(Long[] groupsOids, Long userOid);
    public Boolean removeGroupsFromUser(Long[] groupsOids, Long userOid);
    public UserGroupInfo createGroup();
    public Boolean deleteGroups(Long[] oids);
}

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
package org.kuwaiba.businesslogic;

import org.kuwaiba.ws.todeserialize.ObjectUpdate;
import org.kuwaiba.ws.todeserialize.TransientQuery;
import org.kuwaiba.ws.toserialize.ClassInfo;
import org.kuwaiba.ws.toserialize.ClassInfoLight;
import org.kuwaiba.ws.toserialize.ObjectList;
import org.kuwaiba.ws.toserialize.RemoteObject;
import org.kuwaiba.ws.toserialize.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.RemoteQuery;
import org.kuwaiba.ws.toserialize.RemoteQueryLight;
import org.kuwaiba.ws.toserialize.ResultRecord;
import org.kuwaiba.ws.toserialize.UserGroupInfo;
import org.kuwaiba.ws.toserialize.UserInfo;
import org.kuwaiba.ws.toserialize.ViewInfo;
import org.kuwaiba.entity.queries.Query;
import org.kuwaiba.entity.session.UserSession;
import java.util.List;
import javax.ejb.Remote;

/**
 * Interface exposing the methods within Backendbean
 *
 * @author Charles Edward Bedón Cortázar <charles.bedon@zoho.com>
 */
@Remote
public interface BackendBeanRemote {
    public RemoteObject getObjectInfo(Class objectClass, Long oid) throws Exception;
    public RemoteObjectLight getObjectInfoLight(Class objectClass, Long oid) throws Exception;
    public RemoteObject updateObject(ObjectUpdate _obj, Class...constraints) throws Exception;
    public boolean setObjectLock(Long oid, String objectClass, Boolean value) throws Exception;
    public RemoteObjectLight[] getObjectChildren(Long oid, Long objectClassId)  throws Exception;
    public RemoteObjectLight createObject(Class objectClass, Long parentOid, String template) throws Exception;
    public List<ClassInfo> getMetadata(Boolean includeListTypes) throws Exception;
    public List<ClassInfoLight> getLightMetadata(Boolean includeListTypes) throws Exception;
    public ClassInfo getMetadataForClass(Class className) throws Exception;
    public byte[] getClassHierarchy(Boolean showAll) throws Exception;
    public ObjectList getMultipleChoice(Class className) throws Exception;
    public Boolean addPossibleChildren(Long parentClassId, Long[] possibleChildren) throws Exception;
    public Boolean removePossibleChildren(Long parentClassId, Long[] childrenToBeRemoved) throws Exception;
    public boolean removeObject(Class className, Long oid) throws Exception;
    public List<ClassInfoLight> getPossibleChildren(Class parentClass) throws Exception;
    public List<ClassInfoLight> getPossibleChildrenNoRecursive(Class parentClass) throws Exception;
    public List<ClassInfoLight> getRootPossibleChildren() throws Exception;
    public boolean moveObjects(Long targetOid, Long[] objectOids, Class[] objectClasses) throws Exception;
    public RemoteObjectLight[] copyObjects(Long targetOid, Long[] templateOids, Class[] objectClasses) throws Exception;
    public RemoteObjectLight[] searchForObjects(Class searchedClass, String[] paramNames, String [] paramTypes, String[] paramValues) throws Exception;
    public ResultRecord[] executeQuery(TransientQuery query) throws Exception;
    public Query createQuery(String queryName, Long ownerOid, byte[] queryStructure, String description) throws Exception;
    public boolean saveQuery(Long queryOid, String queryName, Long ownerOid, byte[] queryStructure, String description) throws Exception;
    public boolean deleteQuery(Long queryOid) throws Exception;
    public RemoteQueryLight[] getQueries(Long ownerId, boolean showPublic) throws Exception;
    public RemoteQuery getQuery(Long queryOid) throws Exception;
    public Boolean setAttributePropertyValue(Long classId, String attributeName, String propertyName, String propertyValue) throws Exception;
    public Boolean setClassPlainAttribute(Long classId, String attributeName, String attributeValue) throws Exception;
    public Boolean setClassIcon(Long classId, String attributeName, byte[] iconImage) throws Exception;
    public RemoteObjectLight createListType(Class objectClass) throws Exception;
    public List<ClassInfoLight> getInstanceableListTypes() throws Exception;
    public UserSession createSession(String username, String password, String remoteAddress) throws Exception;
    public Boolean closeSession(String sessionId, String remoteAddress) throws Exception;
    public ViewInfo getDefaultView(Long oid, Class className) throws Exception;
    public ViewInfo getRoomView(Long oid) throws Exception;
    public ViewInfo getRackView(Long oid) throws Exception;
    public RemoteObject createPhysicalContainerConnection(Long nodeA, Long nodeB, Class containerClass,Long parent) throws Exception;
    public RemoteObject createPhysicalConnection(Long endpointA, Long endpointB, Class connectionClass, Long parent) throws Exception;
    public UserInfo[] getUsers() throws Exception;
    public UserGroupInfo[] getGroups() throws Exception;
    public Boolean setUserProperties(Long oid, String[] propertiesNames, String[] propertiesValues) throws Exception;
    public Boolean removeUsersFromGroup(Long[] usersOids, Long groupOid) throws Exception;
    public Boolean addUsersToGroup(Long[] usersOids, Long groupOid) throws Exception;
    public UserInfo createUser() throws Exception;
    public Boolean deleteUsers(java.lang.Long[] oids) throws Exception;
    public Boolean addGroupsToUser(Long[] groupsOids, Long userOid) throws Exception;
    public Boolean removeGroupsFromUser(Long[] groupsOids, Long userOid) throws Exception;
    public UserGroupInfo createGroup() throws Exception;
    public Boolean deleteGroups(Long[] oids) throws Exception;
    public Boolean saveObjectView(Long oid, Class myClass, ViewInfo view) throws Exception;
    public RemoteObject[] getChildrenOfClass(Long parentOid, Class myClass) throws Exception;
    public Class getClassFor(String objectClass) throws Exception;
    public boolean validateCall(String method, String ipAddress, String token) throws Exception;
    public UserSession getSession(String sessionId) throws Exception;
}

/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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

import com.neotropic.kuwaiba.modules.reporting.model.RemoteReport;
import com.neotropic.kuwaiba.modules.reporting.model.RemoteReportLight;
import com.neotropic.kuwaiba.scheduling.BackgroundJob;
import com.neotropic.kuwaiba.sync.model.SyncAction;
import com.neotropic.kuwaiba.sync.model.SyncResult;
import java.util.HashMap;
import java.util.List;
import javax.ejb.Local;
import org.kuwaiba.exceptions.NotAuthorizedException;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.interfaces.ws.todeserialize.TransientQuery;
import org.kuwaiba.interfaces.ws.toserialize.application.ApplicationLogEntry;
import org.kuwaiba.interfaces.ws.toserialize.application.GroupInfo;
import org.kuwaiba.interfaces.ws.toserialize.application.GroupInfoLight;
import org.kuwaiba.interfaces.ws.toserialize.application.PrivilegeInfo;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteBackgroundJob;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteBusinessRule;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteConfigurationVariable;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteContact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteFavoritesFolder;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteForm;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteFormInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteKpiResult;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteFileObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteFileObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.application.RemotePool;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteQuery;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteQueryLight;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteScriptQuery;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteScriptQueryResult;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteScriptQueryResultCollection;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationConfiguration;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationGroup;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteTask;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteTaskResult;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteValidator;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteValidatorDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.ResultRecord;
import org.kuwaiba.interfaces.ws.toserialize.application.TaskNotificationDescriptor;
import org.kuwaiba.interfaces.ws.toserialize.application.TaskScheduleDescriptor;
import org.kuwaiba.interfaces.ws.toserialize.application.UserInfo;
import org.kuwaiba.interfaces.ws.toserialize.application.UserInfoLight;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObject;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.AssetLevelCorrelatedInformation;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteLogicalConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLightList;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectSpecialRelationships;
import org.kuwaiba.interfaces.ws.toserialize.business.RemotePhysicalConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.business.modules.sdh.RemoteSDHContainerLinkDefinition;
import org.kuwaiba.interfaces.ws.toserialize.business.modules.sdh.RemoteSDHPosition;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteAttributeMetadata;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;

@Local
public interface WebserviceBean {

    // <editor-fold defaultstate="collapsed" desc="Session methods. Click on the + sign on the left to edit the code.">
    public RemoteSession createSession(String user, String password, int sessionType, String IPAddress) throws ServerSideException;
    public void closeSession(String sessionId, String remoteAddress) throws ServerSideException, NotAuthorizedException;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Metadata methods. Click on the + sign on the left to edit the code.">

    public long createClass(RemoteClassMetadata classDefinition, String ipAddress, String sessionId) throws ServerSideException;

    public void setClassProperties (RemoteClassMetadata newClassDefinition, String ipAddress, String sessionId) throws ServerSideException;

    public void deleteClass(String className, String ipAddress, String sessionId) throws ServerSideException;

    public void deleteClass(long classId, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteClassMetadataLight> getAllClassesLight(boolean includeListTypes, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteClassMetadataLight> getSubClassesLight(String className, boolean includeAbstractClasses,
            boolean includeSelf, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteClassMetadataLight> getSubClassesLightNoRecursive(String className, 
            boolean includeAbstractClasses, boolean includeSelf, String ipAddress, String sessionId) throws ServerSideException;
    
    public boolean isSubclassOf(String className, String subclassOf, String remoteAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteClassMetadata> getAllClasses(boolean includeListTypes, String ipAddress, String sessionId) throws ServerSideException;

    public RemoteClassMetadata getClass(String className, String ipAddress, String sessionId) throws ServerSideException;

    public RemoteClassMetadata getClass(long classId, String ipAddress, String sessionId) throws ServerSideException;
    
    public void createAttribute(String className, RemoteAttributeMetadata attributeDefinition, String ipAddress, String sessionId) throws ServerSideException;

    public void createAttribute(long classId, RemoteAttributeMetadata attributeDefinition, String ipAddress, String sessionId) throws ServerSideException;
    
    public boolean hasAttribute(String className, String attributeName, String ipAddress, String sessionId) throws ServerSideException;

    public RemoteAttributeMetadata getAttribute(String className, String attributeName, String ipAddress, String sessionId) throws ServerSideException;

    public RemoteAttributeMetadata getAttribute(long classId, long attributeId, String ipAddress, String sessionId) throws ServerSideException;

    public void setAttributeProperties(long classId, RemoteAttributeMetadata newAttributeDefinition, String ipAddress, String sessionId) throws ServerSideException;

    public void setAttributeProperties(String className, RemoteAttributeMetadata newAttributeDefinition, String ipAddress, String sessionId) throws ServerSideException;

    public void deleteAttribute(String className, String attributeName, String ipAddress, String sessionId) throws ServerSideException;

    public void deleteAttribute(long classId, String attributeName, String ipAddress, String sessionId) throws ServerSideException;
        
    public List<RemoteClassMetadataLight> getPossibleChildren(String parentClassName, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteClassMetadataLight> getPossibleSpecialChildren(String parentClassName, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteClassMetadataLight> getPossibleChildrenNoRecursive(String parentClassName, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteClassMetadataLight> getPossibleSpecialChildrenNoRecursive(String parentClassName, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteClassMetadataLight> getUpstreamContainmentHierarchy(String className, boolean recursive, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteClassMetadataLight> getUpstreamSpecialContainmentHierarchy(String className, boolean recursive, String ipAddress, String sessionId) throws ServerSideException;
    
    public void addPossibleChildren(long parentClassId, long[] possibleChildren, String ipAddress, String sessionId) throws ServerSideException;
    
    public void addPossibleSpecialChildren(long parentClassId, long[] possibleSpecialChildren, String ipAddress, String sessionId) throws ServerSideException;
    
    public void addPossibleChildren(String parentClassName, String[] newPossibleChildren, String ipAddress, String sessionId) throws ServerSideException;
    
    public void addPossibleSpecialChildren(String parentClassName, String[] possibleChildren, String ipAddress, String sessionId) throws ServerSideException;

    public void removePossibleChildren(long parentClassId, long[] childrenToBeRemoved, String ipAddress, String sessionId) throws ServerSideException;
    
    public void removePossibleSpecialChildren(long parentClassId, long[] specialChildrenToBeRemoved, String ipAddress, String sessionId) throws ServerSideException;

    public String getAttributeValueAsString(String objectClass, long objectId, String attributeName, String ipAddress, String sessionId) throws ServerSideException;
    
    public HashMap<String, String> getAttributeValuesAsString(String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Business methods. Click on the + sign on the left to edit the code.">
    public List<RemoteObjectLight> getObjectChildren(long oid, long objectClassId, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getObjectChildren(String objectClassName, long oid, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getSiblings(String objectClassName, long oid, int maxResults, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteObject> getChildrenOfClass(long parentOid, String parentClass,String classToFilter, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getChildrenOfClassLight(long parentOid, String parentClass,String classToFilter, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getSpecialChildrenOfClassLight(long parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getChildrenOfClassLightRecursive(long parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    
    public RemoteObject getObject(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException;

    public RemoteObjectLight getObjectLight(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObject> getObjectsWithFilter (String className, String filterName, String filterValue, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getObjectsWithFilterLight (String className, String filterName, String filterValue, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObjectLight getCommonParent(String aObjectClass, long aOid, String bObjectClass, long bOid, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getSuggestedObjectsWithFilter(String filter, int limit, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getSuggestedObjectsWithFilter(String filter, String superClass, int limit, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObjectLight getParent(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getParents(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getParentsUntilFirstOfClass(String objectClassName,long oid, String objectToMatchClassName, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObjectLight getFirstParentOfClass(String objectClassName,long oid, String objectToMatchClassName, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObject getParentOfClass(String objectClass, long oid, String parentClass, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteObjectLight> getSpecialAttribute(String objectClass, long objectId, String attributeName, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObjectSpecialRelationships getSpecialAttributes(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getObjectSpecialChildren(String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException;

    public void updateObject(String className, long oid, List<StringPair> attributesToBeUpdated, String ipAddress, String sessionId) throws ServerSideException;

    public long createObject(String className, String parentClassName, long parentOid, String[] attributeNames, String[] attributeValues, long templateId, String ipAddress, String sessionId) throws ServerSideException;
    public long createSpecialObject(String className, String parentObjectClassName, long parentOid, String[] attributeNames, String[] attributeValues, long templateId, String ipAddress, String sessionId) throws ServerSideException;
    
    public long createListTypeItem(String className, String name, String displayName, String ipAddress, String sessionId) throws ServerSideException;

    public void deleteListTypeItem(String className, long oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteObjectLight> getListTypeItems(String className, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getObjectsOfClassLight(String className, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObject> getObjectsOfClass(String className, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteClassMetadataLight[] getInstanceableListTypes(String ipAddress, String sessionId) throws ServerSideException;

    public boolean canDeleteObject(String className, long oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteObject(String className, long oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteObjects(String classNames[], long[] oids, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;

    public void moveObjectsToPool(String targetClass, long targetOid, String[] objectClasses, long[] objectOids, String ipAddress, String sessionId) throws ServerSideException;
    public void moveObjects(String targetClass, long targetOid, String[] objectClasses, long[] objectOids, String ipAddress, String sessionId) throws ServerSideException;
    public void moveSpecialObjects(String targetClass, long targetOid, String[] objectClasses, long[] objectOids, String ipAddress, String sessionId) throws ServerSideException;
    public void movePoolItem(long poolId, String poolItemClassName, long poolItemId, String ipAddress, String sessionId) throws ServerSideException;

    public long[] copyObjects(String targetClass, long targetOid, String[] templateClasses, long[] templateOids, boolean recursive, String ipAddress, String sessionId) throws ServerSideException;
    public long[] copySpecialObjects(String targetClass, long targetOid, String[] templateClasses, long[] templateOids, boolean recursive, String ipAddress, String sessionId) throws ServerSideException;
    public long copyPoolItem(long poolId, String poolItemClassName, long poolItemId, boolean recursive, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteAttributeMetadata> getMandatoryAttributesInClass(String className, String ipAddress, String sessionId)  throws ServerSideException;
        
    public long [] createBulkObjects(String className, String parentClassName, long parentOid, int numberOfObjects, String namePattern, String ipAddress, String sessionId) throws ServerSideException;
    public long[] createBulkSpecialObjects(String className, String parentClassName, long parentId, int numberOfSpecialObjects, String namePattern, String ipAddress, String sessionId) throws ServerSideException;
    //Physical connections
    public void connectMirrorPort(String[] aObjectClass, long[] aObjectId, String[] bObjectClass, long[] bObjectId, String ipAddress, String sessionId) throws ServerSideException;
    public void releaseMirrorPort(String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException;
    public long createPhysicalConnection(String aObjectClass, long aObjectId, String bObjectClass, long bObjectId, String parentClass, long parentId, String name, String connectionClass, long templateId, String ipAddress, String sessionId) throws ServerSideException;
    public void deletePhysicalConnection(String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObjectLight[] getPhysicalConnectionEndpoints(String connectionClass, long connectionId, String ipAddress, String sessionId) throws ServerSideException;
    public void connectPhysicalLinks(String[] sideAClassNames, Long[] sideAIds, String[] linksClassNames, long[] linksIds, String[] sideBClassNames, Long[] sideBIds, String ipAddress, String sessionId) throws ServerSideException;
    public void connectPhysicalContainers(String[] sideAClassNames, Long[] sideAIds, String[] containerssClassNames, long[] containersIds, String[] sideBClassNames, Long[] sideBIds, String ipAddress, String sessionId) throws ServerSideException;
    public void reconnectPhysicalConnection(String connectionClass, long connectionId, String newASideClass, long newASideId, String newBSideClass, long newBSideId, String ipAddress, String sessionId) throws ServerSideException;
    public void disconnectPhysicalConnection(String connectionClass, long connectionId, int sideToDisconnect, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getPhysicalPath(String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteLogicalConnectionDetails getLogicalLinkDetails(String linkClass, long linkId, String ipAddress, String sessionId) throws ServerSideException;
    public RemotePhysicalConnectionDetails getPhysicalLinkDetails(String linkClass, long linkId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getContainersBetweenObjects(String objectAClassName, long objectAId,
            String objectBClassName, long objectBId, String containerClassName, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLightList> getPhysicalConnectionsInObject(String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObject getLinkConnectedToPort(String portClassName, long portId, String ipAddress, String sessionId) throws ServerSideException;
    //Service Manager
    public void associateObjectToService(String objectClass, long objectId, String serviceClass, long serviceId, String ipAddress, String sessionId) throws ServerSideException;
    public void associateObjectsToService(String[] objectClass, long[] objectId, String serviceClass, long serviceId, String ipAddress, String sessionId) throws ServerSideException;
    public void releaseObjectFromService(String serviceClass, long serviceId, long otherObjectId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getServiceResources(String serviceClass, long serviceId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getServicesForCustomer(String customerClass, long customerId, int limit, String ipAddress, String sessionId) throws ServerSideException;
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Application methods. Click on the + sign on the left to edit the code.">
    public void setUserProperties(long oid, String userName, String password, String firstName,
            String lastName, int enabled, int type, String ipAddress, String sessionId)
            throws ServerSideException;

    public void addUserToGroup(long userId, long groupId, String ipAddress, String sessionId) throws ServerSideException;
    public void removeUserFromGroup(long userId, long groupId, String ipAddress, String sessionId) throws ServerSideException;
    public void setPrivilegeToUser(long userId, String featureToken, int accessLevel, String ipAddress, String sessionId) throws ServerSideException;
    public void setPrivilegeToGroup(long groupId, String featureToken, int accessLevel, String ipAddress, String sessionId) throws ServerSideException;
    public void removePrivilegeFromUser(long userId, String featureToken, String ipAddress, String sessionId) throws ServerSideException;
    public void removePrivilegeFromGroup(long groupId, String featureToken, String ipAddress, String sessionId) throws ServerSideException;
    
    public long createGroup(String groupName, String description, List<Long> users, String ipAddress, String sessionId) throws ServerSideException;

    public List<UserInfo> getUsers(String ipAddress, String sessionId) throws ServerSideException;
    public List<UserInfo> getUsersInGroup(long groupId, String ipAddress, String sessionId) throws ServerSideException;

    public List<GroupInfoLight> getGroupsForUser(long userId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<GroupInfo> getGroups(String ipAddress, String sessionId) throws ServerSideException;

    public long createUser(String userName, String password, String firstName, 
        String lastName, boolean enabled, int type, List<PrivilegeInfo> privileges, 
        long defaultGroupId, String ipAddress, String sessionId) throws ServerSideException;

    public void setGroupProperties(long oid, String groupName, String description, String ipAddress, String sessionId)throws ServerSideException;

    public void deleteUsers(long[] oids, String ipAddress, String sessionId)throws ServerSideException;

    public void deleteGroups(long[] oids, String ipAddress, String sessionId)
            throws ServerSideException;
    
    public long createListTypeItemRelatedView(long listTypeItemId, String listTypeItemClassName, String viewClassName, 
        String name, String description, byte [] structure, byte [] background, String ipAddress, String sessionId) 
        throws ServerSideException;
    
    public void updateListTypeItemRelatedView(long listTypeItemId, String listTypeItemClass, long viewId, 
        String name, String description, byte[] structure, byte[] background, String ipAddress, String sessionId) 
        throws ServerSideException;
    
    public RemoteViewObject getListTypeItemRelatedView(long listTypeItemId, String listTypeItemClass, long viewId, String ipAddress, String sessionId) 
        throws ServerSideException;
    
    public RemoteViewObjectLight[] getListTypeItemRelatedViews(long listTypeItemId, String listTypeItemClass, int limit, String ipAddress, String sessionId) throws ServerSideException;
    
    public void deleteListTypeItemRelatedView(long listTypeItemId, String listTypeItemClass, long viewId, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteObjectLight> getListTypeItemUses(String listTypeItemClass, long listTypeItemId, int limit, 
        String ipAddress, String sessionId) throws ServerSideException;
    
    public RemoteViewObject getObjectRelatedView(long oid, String objectClass, long viewId, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteViewObjectLight> getObjectRelatedViews(long oid, String objectClass, int viewType, int limit, String ipAddress, String sessionId) throws ServerSideException;

    public RemoteViewObject getGeneralView(long viewId, String ipAddress, String sessionId) throws ServerSideException;
    
    public RemoteViewObjectLight[] getGeneralViews(String viewClassName, int limit, String ipAddress, String sessionId) throws ServerSideException;

    public long createObjectRelatedView(long objectId, String objectClass, String name, String description, String viewClassName, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException;

    public long createGeneralView(String viewClass, String name, String description, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException;

    public void updateObjectRelatedView(long objectOid, String objectClass, long viewId, String viewName, String viewDescription, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException;

    public void updateGeneralView(long viewId, String viewName, String viewDescription, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException;

    public void deleteGeneralView(long [] oids, String ipAddress, String sessionId) throws ServerSideException;

    public ResultRecord[] executeQuery(TransientQuery query, String ipAddress, String sessionId) throws ServerSideException;

    public long createQuery(String queryName, long ownerOid, byte[] queryStructure,
            String description, String ipAddress, String sessionId) throws ServerSideException;

    public void saveQuery(long queryOid, String queryName, long ownerOid, 
            byte[] queryStructure, String description, String ipAddress, String sessionId) throws ServerSideException;

    public void deleteQuery(long queryOid, String ipAddress, String sessionId) throws ServerSideException;

    public RemoteQueryLight[] getQueries(boolean showPublic, String ipAddress, String sessionId) throws ServerSideException;

    public RemoteQuery getQuery(long queryOid, String ipAddress, String sessionId) throws ServerSideException;

    public byte[] getClassHierarchy(boolean showAll, String ipAddress, String sessionId) throws ServerSideException;

    //Pools
    public long createRootPool(String name, String description, String instancesOfClass, int type, String ipAddress, String sessionId)
            throws ServerSideException;
    
    public long createPoolInObject(String parentClassname, long parentId, String name, String description, String instancesOfClass, int type, String ipAddress, String sessionId)
            throws ServerSideException;
    
    public long createPoolInPool(long parentId, String name, String description, String instancesOfClass, int type, String ipAddress, String sessionId) 
            throws ServerSideException;
    
    public long createPoolItem(long poolId, String className, String[] attributeNames, String[] attributeValues, long templateId, String ipAddress, String sessionId) throws ServerSideException;
    
    public void deletePools(long[] ids, String ipAddress, String sessionId) throws ServerSideException;
    public void setPoolProperties(long poolId, String name, String description, String ipAddress, String sessionId) throws ServerSideException;
    
    public RemotePool getPool(long poolId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemotePool> getRootPools(String className, int type, boolean includeSubclasses, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemotePool> getPoolsInObject(String objectClassName, long objectId, String poolClass, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemotePool> getPoolsInPool(long parentPoolId, String poolClass, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteObjectLight> getPoolItems(long poolId, int limit, String ipAddress, String sessionId) throws ServerSideException;
    
    public ApplicationLogEntry[] getBusinessObjectAuditTrail(String objectClass, long objectId, int limit, String ipAddress, String sessionId) throws ServerSideException;
    
    public ApplicationLogEntry[] getGeneralActivityAuditTrail(int page, int limit, String ipAddress, String sessionId) throws ServerSideException;

    public long attachFileToObject(String name, String tags, byte[] file, String className, long objectId, String ipAddress, String sessionId) throws ServerSideException;
    
    public void detachFileFromObject(long fileObjectId, String className, long objectId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteFileObjectLight> getFilesForObject(String className, long objectId, String ipAddress, String sessionId) throws ServerSideException;

    public RemoteFileObject getFile(long fileObjectId, String className, long objectId, String ipAddress, String sessionId) throws ServerSideException;
    
    public void updateFileProperties(long fileObjectId, List<StringPair> properties,String className, long objectId, String ipAddress, String sessionId) throws ServerSideException;
    
    public long createTask(String name, String description, boolean enabled, boolean commitOnExecute, String script, List<StringPair> parameters, TaskScheduleDescriptor schedule, TaskNotificationDescriptor notificationType, String ipAddress, String sessionId) throws ServerSideException;

    public void updateTaskProperties(long taskId, String propertyName, String propertyValue, String ipAddress, String sessionId) throws ServerSideException;

    public void updateTaskParameters(long taskId, List<StringPair> parameters, String ipAddress, String sessionId) throws ServerSideException;
    
    public void updateTaskSchedule(long taskId, TaskScheduleDescriptor schedule, String ipAddress, String sessionId) throws ServerSideException;

    public void updateTaskNotificationType(long taskId, TaskNotificationDescriptor notificationType, String ipAddress, String sessionId) throws ServerSideException;

    public RemoteTask getTask(long taskId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteTask> getTasks(String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteTask> getTasksForUser(long userId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<UserInfoLight> getSubscribersForTask(long taskId, String ipAddress, String sessionId) throws ServerSideException;

    public void deleteTask(long taskId, String ipAddress, String sessionId) throws ServerSideException;

    public void subscribeUserToTask(long userId, long taskId, String ipAddress, String sessionId) throws ServerSideException;

    public void unsubscribeUserFromTask(long userId, long taskId, String ipAddress, String sessionId) throws ServerSideException;
    
    public RemoteTaskResult executeTask(long taskId, String ipAddress, String sessionId) throws ServerSideException;
    
    public long createForm(String name, String description, byte[] structure, String ipAddress, String sessionId) throws ServerSideException;
    
    public void updateForm(long formId, String name, String description, byte[] structure, String ipAddress, String sessionId) throws ServerSideException;
    
    public RemoteForm getForm(long formId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteForm> getForms(String ipAddress, String sessionId) throws ServerSideException;
    
    public void deleteForm(long formId, String ipAddress, String sessionId) throws ServerSideException;
    
    public long createFormInstance(long formId, String name, String description, byte[] structure, String ipAddress, String sessionId) throws ServerSideException;
    
    public void updateFormInstance(long formInstanceId, String name, String description, byte[] structure, String ipAddress, String sessionId) throws ServerSideException;
    
    public RemoteFormInstance getFormInstance(long formInstanceId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteFormInstance> getFormInstances(String ipAddress, String sessionId) throws ServerSideException;
    
    public void deleteFormInstance(long formInstanceId, String ipAddress, String sessionId) throws ServerSideException;
                
    public long createScriptQuery(String name, String description, String script, String countable, List<StringPair> parameters, String ipAddress, String sessionId) throws ServerSideException;
    
    public void updateScriptQueryProperties(long scriptQueryId, String propertyName, String propertyValue, String ipAddress, String sessionId) throws ServerSideException;
    
    public void updateScriptQueryParameters(long scriptQueryId, List<StringPair> parameters, String ipAddress, String sessionId) throws ServerSideException;
    public void updateScriptQueryParameters(String scriptQueryName, List<StringPair> parameters, String ipAddress, String sessionId) throws ServerSideException;
    
    public RemoteScriptQuery getScriptQuery(long scriptQueryId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteScriptQuery> getScriptQueries(String ipAddress, String sessionId) throws ServerSideException;
    
    public void deleteScriptQuery(long scriptQueryId, String ipAddress, String sessionId) throws ServerSideException;
    
    public RemoteScriptQueryResult executeScriptQuery(long scriptQueryId, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteScriptQueryResult executeScriptQuery(String scriptQueryName, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteScriptQueryResultCollection executeScriptQueryCollection(long scriptQueryId, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteScriptQueryResultCollection executeScriptQueryCollection(String scriptQueryName, String ipAddress, String sessionId) throws ServerSideException;
    public long createContact(String contactClass, List<StringPair> properties, String customerClassName, long customerId, String ipAddress, String sessionId) throws  ServerSideException;
    public void updateContact(String contactClass, long contactId, List<StringPair> properties, String ipAddress, String sessionId) throws  ServerSideException;
    public void deleteContact(String contactClass, long contactId, String ipAddress, String sessionId) throws  ServerSideException;
    public RemoteContact getContact(String contactClass, long contactId, String ipAddress, String sessionId) throws  ServerSideException;
    public List<RemoteContact> searchForContacts(String searchString, int maxResults, String ipAddress, String sessionId) throws  ServerSideException;
    public List<RemoteContact> getContactsForCustomer(String customerClass, long customerId, String ipAddress, String sessionId) throws  ServerSideException;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Sync/bulkupload methods. Click on the + sign on the left to edit the code.">
    /**
     * Loads data from a csv file
     * @param file a csv file as a byte array with the data
     * @param commitSize commit after n rows  
     * @param dataType what kind of data contains the file, listTypes, Objects, etc
     * @param ipAddress
     * @param sessionId
     * @return
     * @throws ServerSideException 
     */
    public String bulkUpload(byte[] file, int commitSize, int dataType, String ipAddress, String sessionId) throws ServerSideException;
    /**
     * Returns a file with the wrong lines of the load file
     * @param fileName 
     * @param ipAddress
     * @param sessionId
     * @return 
     * @throws ServerSideException 
     */
    public byte[] downloadBulkLoadLog(String fileName, String ipAddress, String sessionId) throws ServerSideException;
    // </editor-fold>
    
    //<editor-fold desc="Templates" defaultstate="collapsed">
    public long createTemplate(String templateClass, String templateName, String ipAddress, String sessionId) throws ServerSideException;

    public long createTemplateElement(String templateElementClass, String templateElementParentClassName, long templateElementParentId, String templateElementName, String ipAddress, String sessionId) throws ServerSideException;
    
    public long createTemplateSpecialElement(String tsElementClass, String tsElementParentClassName, long tsElementParentId, String tsElementName, String ipAddress, String sessionId) throws ServerSideException;
    
    public long[] createBulkTemplateElement(String templateElementClassName, String templateElementParentClassName, long templateElementParentId, int numberOfTemplateElements, String templateElementNamePattern, String ipAddress, String sessionId) throws ServerSideException;
    
    public long[] createBulkSpecialTemplateElement(String stElementClass, String stElementParentClassName, long stElementParentId, int numberOfTemplateElements, String stElementNamePattern, String ipAddress, String sessionId) throws ServerSideException;

    public void updateTemplateElement(String templateElementClass, long templateElementId, String[] attributeNames, String[] attributeValues, String ipAddress, String sessionId) throws ServerSideException;

    public void deleteTemplateElement(String templateElementClass, long templateElementId, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteObjectLight> getTemplatesForClass(String className, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteObjectLight> getTemplateElementChildren(String templateElementClass, 
            long templateElementId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteObjectLight> getTemplateSpecialElementChildren(String tsElementClass, 
            long tsElementId, String ipAddress, String sessionId) throws ServerSideException;
    
    public RemoteObject getTemplateElement(String templateElementClass, long templateElementId, 
            String ipAddress, String sessionId) throws ServerSideException;
    
    public long[] copyTemplateElements(String[] sourceObjectsClassNames, long[] sourceObjectsIds, 
            String newParentClassName,long newParentId, String ipAddress, String sessionId) throws ServerSideException;
    
    public long[] copyTemplateSpecialElements(String[] sourceObjectsClassNames, long[] sourceObjectsIds, 
        String newParentClassName, long newParentId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteObjectLight> getDeviceLayouts(String ipAddress, String sessionId) throws ServerSideException;
        
    public byte[] getDeviceLayoutStructure(long oid, String className, String ipAddress, String sessionId) throws ServerSideException;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Reporting methods.">
    public long createClassLevelReport(String className, String reportName, String reportDescription, 
            String script, int outputType, boolean enabled, String ipAddress, String sessionId) throws ServerSideException;
    
    public long createInventoryLevelReport(String reportName, String reportDescription, String script, int outputType, 
            boolean enabled, List<StringPair> parameters, String ipAddress, String sessionId) throws ServerSideException;
    
    public void deleteReport(long reportId, String ipAddress, String sessionId) throws ServerSideException;
    
    public void updateReport(long reportId, String reportName, String reportDescription, Boolean enabled,
            Integer type, String script, String ipAddress, String sessionId) throws ServerSideException;

    public void updateReportParameters(long reportId, List<StringPair> parameters, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteReportLight> getClassLevelReports(String className, boolean recursive, 
            boolean includeDisabled, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteReportLight> getInventoryLevelReports(boolean includeDisabled, 
            String ipAddress, String sessionId) throws ServerSideException;
    
    public RemoteReport getReport(long reportId, String ipAddress, String sessionId) throws ServerSideException;
    
    public byte[] executeClassLevelReport(String objectClassName, long objectId, 
            long reportId, String ipAddress, String sessionId) throws ServerSideException;
   
    public byte[] executeInventoryLevelReport(long reportId, List<StringPair> parameters, 
            String ipAddress, String sessionId) throws ServerSideException;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Commercial modules data methods">
        // <editor-fold defaultstate="collapsed" desc="SDH Networks Module">
    public long createSDHTransportLink(String classNameEndpointA, long idEndpointA, 
            String classNameEndpointB, long idEndpointB, String linkType, String defaultName, String ipAddress, String sessionId) throws ServerSideException;
    
    public long createSDHContainerLink(String classNameEndpointA, long idEndpointA, 
            String classNameEndpointB, long idEndpointB, String linkType, List<RemoteSDHPosition> positions, String defaultName, String ipAddress, String sessionId) throws ServerSideException;
    
    public long createSDHTributaryLink(String classNameEndpointA, long idEndpointA, 
            String classNameEndpointB, long idEndpointB, String linkType, List<RemoteSDHPosition> positions, String defaultName, String ipAddress, String sessionId) throws ServerSideException;
    
    public void deleteSDHTransportLink(String transportLinkClass, long transportLinkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException;
    
    public void deleteSDHContainerLink(String containerLinkClass, long containerLinkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException;
    
    public void deleteSDHTributaryLink(String tributaryLinkClass, long tributaryLinkId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteObjectLightList> findSDHRoutesUsingTransportLinks(String communicationsEquipmentClassA, 
                                            long  communicationsEquipmentIdA, String communicationsEquipmentClassB, 
                                            long  communicationsEquipmentIB, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteObjectLightList> findSDHRoutesUsingContainerLinks(String communicationsEquipmentClassA, 
                                            long  communicationsEquipmentIdA, String communicationsEquipmentClassB, 
                                            long  communicationsEquipmentIB, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteSDHContainerLinkDefinition> getSDHTransportLinkStructure(String transportLinkClass, long transportLinkId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteSDHContainerLinkDefinition> getSDHContainerLinkStructure(String transportLinkClass, long transportLinkId, String ipAddress, String sessionId) throws ServerSideException;
        // </editor-fold>   
    
        // <editor-fold defaultstate="collapsed" desc="IP Address Manager module">
        public RemotePool[] getSubnetPools(long parentId, String className, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteObjectLight> getSubnets(long poolId, int limit, String ipAddress, String sessionId) throws ServerSideException;
        public RemoteObject getSubnet(long id, String className, String ipAddress, String sessionId) throws ServerSideException;
        public RemotePool getSubnetPool(long id, String ipAddress, String sessionId) throws ServerSideException;
        public long createSubnetPool(long parentId, String subnetPoolName, 
                String subnetPoolDescription, String className, String ipAddress, 
                String sessionId) throws ServerSideException;
        public long createSubnet(long poolId, String className, List<StringPair> attributes, String ipAddress, String sessionId) throws ServerSideException;
        public void deleteSubnets(String className, List<Long> ids, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;
        public void deleteSubnetPools(long[] ids, String ipAddress, String sessionId) throws ServerSideException;
        public long addIPAddress(long id, String parentClassName, List<StringPair> attributesToBeUpdated, String ipAddress, String sessionId) throws ServerSideException;
        public void removeIP(long[] ids, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;
        public void relateIPtoPort(long id, String deviceClassName, long deviceId, String ipAddress, String sessionId) throws ServerSideException;
        public void relateSubnetToVlan(long id, String className, long vlanId, String ipAddress, String sessionId) throws ServerSideException;
        public void releaseSubnetFromVlan(long vlanId, long id, String ipAddress, String sessionId) throws ServerSideException;
        public void relateSubnetToVrf(long id, String className, long vrfId, String ipAddress, String sessionId) throws ServerSideException;
        public void releasePortFromIP(String deviceClassName, long deviceId, long id, String ipAddress, String sessionId) throws ServerSideException;
        public void releaseSubnetFromVRF(long subnetId, long vrfId, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteObjectLight> getSubnetUsedIps(long id, String className, int limit, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteObjectLight> getSubnetsInSubnet(long id, String className, int limit, String ipAddress, String sessionId)  throws ServerSideException;
        public boolean itOverlaps(String networkIp, String broadcastIp, String ipAddress, String sessionId) throws ServerSideException;
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc="Contract Manager">
    public void associateObjectsToContract(String[] deviceClass, long[] deviceId, 
            String contractClass, long contractId, String ipAddress, String sessionId) throws ServerSideException;
    
    public void releaseObjectFromContract(String deviceClass, long deviceId, long contractId,
            String ipAddress, String sessionId) throws ServerSideException;    
        //</editor-fold>
      
        // <editor-fold defaultstate="collapsed" desc="MPLS Networks Module">
         public long createMPLSLink(String classNameEndpointA, long idEndpointA, 
            String classNameEndpointB, long idEndpointB, String linkType, String defaultName, String ipAddress, String sessionId) throws ServerSideException;
    
         public void deleteMPLSLink(String linkClass, long linkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException;
         public void relatePortToInterface(long portId, String portClassName, String interfaceClassName, long interfaceId, String ipAddress, String sessionId) throws ServerSideException;
         public void releasePortFromInterface(String interfaceClassName, long interfaceId ,long portId, String ipAddress, String sessionId) throws ServerSideException;
    // </editor-fold>
         
        // <editor-fold defaultstate="collapsed" desc="Projects Module">
        public List<RemotePool> getProjectPools(String ipAddress, String sessionId) throws ServerSideException;
        public long addProject(long parentId, String parentClassName, String className, String[] attributeNames, String[] attributeValues, String ipAddress, String sessionId) throws ServerSideException;
        public void deleteProject(String className, long oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;
        public long addActivity(long parentId, String parentClassName, String className, String attributeNames[], String attributeValues[], String ipAddress, String sessionId) throws ServerSideException;
        public void deleteActivity(String className, long oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteObjectLight> getProjectsInProjectPool(long poolId, int limit, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteObjectLight> getProjectResurces(String projectClass, long projectId, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteObjectLight> getProjectActivities(String projectClass, long projectId, String ipAddress, String sessionId) throws ServerSideException;
        public void associateObjectsToProject(String projectClass, long projectId, String[] objectClass, long[] objectId, String ipAddress, String sessionId) throws ServerSideException;
        public void associateObjectToProject(String projectClass, long projectId, String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException;
        public void releaseObjectFromProject(String objectClass, long objectId, String projectClass, long projectId, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteObjectLight> getProjectsAssociateToObject(String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException;
        public long createProjectPool(String name, String description, String instanceOfClass, String ipAddress, String sessionId) throws ServerSideException;
        // </editor-fold>
        
        //<editor-fold desc="Synchronization API" defaultstate="collapsed">
        public long createSynchronizationGroup(String name, String ipAddress, String sessionId)throws ServerSideException;
        public void updateSynchronizationGroup(long syncGroupId, List<StringPair> syncGroupProperties, String ipAddress, String sessionId)throws ServerSideException;
        
        public long createSynchronizationDataSourceConfig(long objectId, long syncGroupId, String name, List<StringPair> parameters, String ipAddress, String sessionId) throws ServerSideException;
        public void updateSyncDataSourceConfiguration(long syncDataSourceConfigId, List<StringPair> parameters, String ipAddress, String sessionId)throws ServerSideException;
        public BackgroundJob launchAutomatedSynchronizationTask(long syncGroupId, String ipAddress, String sessionId) throws ServerSideException;
        public BackgroundJob launchSupervisedSynchronizationTask(long syncGroupId, String ipAddress, String sessionId) throws ServerSideException;
        
        public BackgroundJob launchAdHocAutomatedSynchronizationTask(long[] syncDataSourceConfigIds, String syncProvidersName, String ipAddress, String sessionId) throws ServerSideException;
        public BackgroundJob launchAdHocAutomatedSynchronizationTask(long syncGroupId, String syncProvidersName, String ipAddress, String sessionId) throws ServerSideException;
       
        public RemoteSynchronizationGroup getSynchronizationGroup(long syncGroupId, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteSynchronizationGroup> getSynchronizationGroups(String ipAddress, String sessionId)throws ServerSideException;
        
        public RemoteSynchronizationConfiguration getSyncDataSourceConfiguration(long objectId, String ipAddress, String sessionId)throws ServerSideException;
        public List<RemoteSynchronizationConfiguration> getSyncDataSourceConfigurations(long syncGroupId, String ipAddress, String sessionId)throws ServerSideException;
        public void deleteSynchronizationGroup(long syncGroupId, String ipAddress, String sessionId)throws ServerSideException;
        public void deleteSynchronizationDataSourceConfig(long syncDataSourceConfigId, String ipAddress, String sessionId)throws ServerSideException;
        public List<SyncResult> executeSyncActions(long syncGroupId, List<SyncAction> actions, String ipAddress, String sessionId)throws ServerSideException;
        
        public List<RemoteSynchronizationGroup> copySyncGroup(long[] syncGroupIds, String ipAddress, String sessionId) throws ServerSideException;
        public void copySyncDataSourceConfiguration(long syncGroupId, long[] syncDataSourceConfigurationIds, String ipAddress, String sessionId) throws ServerSideException;
        public void releaseSyncDataSourceConfigFromSyncGroup(long syncGroupId, long[] syncDataSourceConfigurationIds, String ipAddress, String sessionId) throws ServerSideException;
        public void moveSyncDataSourceConfiguration(long oldSyncGroupId, long newSyncGroupId, long[] syncDataSourceConfigurationIds, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteBackgroundJob> getCurrentJobs(String ipAddress, String sessionId) throws ServerSideException;
        public void killJob(long jobId, String ipAddress, String sessionId) throws ServerSideException;
        //</editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc="Warehouse Module">
    public List<RemotePool> getWarehouseRootPool(String ipAddress, String sessionId) throws ServerSideException;
    public void associatePhysicalNodeToWarehouse(String objectClass, long objectId, String warehouseClass, long warehouseId, String ipAddress, String sessionId) throws ServerSideException;
    public void associatesPhysicalNodeToWarehouse(String[] objetClass, long[] objectId, String warehouseClass, long warehouseId, String ipAddress, String sessionId) throws ServerSideException;
    public void releasePhysicalNodeFromWarehouse(String warehouseClass, long warehouseId, long objectId, String ipAddress, String sessionId) throws ServerSideException;
    public void moveObjectsToWarehousePool(String targetClass, long targetOid, String[] objectClasses, long[] objectOids, String ipAddress, String sessionId) throws ServerSideException;
    public void moveObjectsToWarehouse(String targetClass, long targetOid, String[] objectClasses, long[] objectOids, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObjectLight getPhysicalNodeToObjectInWarehouse(String objectClassName, long objectId, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObjectLight getWarehouseToObject(String objectClassName, long objectId, String ipAddress, String sessionId) throws ServerSideException;
        // </editor-fold>
    
    //<editor-fold desc="Outside Plant" defaultstate="collapsed">
    public long createOSPView(String name, String description, byte[] content, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteViewObject getOSPView(long viewId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteViewObjectLight> getOSPViews(String ipAddressString, String sessionId) throws ServerSideException;
    public void updateOSPView(long viewId, String name, String description, byte[] content, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteOSPView(long viewId, String ipAddress, String sessionId) throws ServerSideException;
    
    //</editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc="BGP Viewer Module">
        public List<RemoteLogicalConnectionDetails> getBGPMap(List<Long> mappedBGPLinksIds, String ipAddress, String sessionId) throws ServerSideException;
        //</editor-fold>
    
    // Bookmarks
    public long createFavoritesFolderForUser(String favoritesFolderName, long userId, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteFavoritesFolders (long[] favoritesFolderId, long userId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteFavoritesFolder> getFavoritesFoldersForUser(long userId, String ipAddress, String sessionId) throws ServerSideException;
    public void addObjectsToFavoritesFolder(String[] objectClass, long[] objectId, long favoritesFolderId, long userId, String ipAddress, String sessionId) throws ServerSideException;
    public void removeObjectsFromFavoritesFolder(String[] objectClass, long[] objectId, long favoritesFolderId, long userId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getObjectsInFavoritesFolder(long favoritesFolderId, long userId, int limit, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteFavoritesFolder> getFavoritesFoldersForObject(long userId ,String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteFavoritesFolder getFavoritesFolder(long favoritesFolderId, long userId, String ipAddress, String sessionId) throws ServerSideException;
    public void updateFavoritesFolder(long favoritesFolderId, long userId, String favoritesFolderName, String ipAddress, String sessionId) throws ServerSideException;
    public long createBusinessRule(String ruleName, String ruleDescription, int ruleType, int ruleScope, String appliesTo, String ruleVersion, List<String> constraints, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteBusinessRule(long businessRuleId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteBusinessRule> getBusinessRules(int type, String ipAddress, String sessionId) throws ServerSideException;
    public AssetLevelCorrelatedInformation getAffectedServices(int resourceType, String resourceDefinition, String ipAddress, String sessionId) throws ServerSideException;
    //<editor-fold desc="Process API" defaultstate="collapsed">
    public RemoteArtifact getArtifactForActivity(long processInstanceId, long activityId, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteArtifactDefinition getArtifactDefinitionForActivity(long processDefinitionId, long activityDefinitionId, String ipAddress, String sessionId) throws ServerSideException;
    public void commitActivity(long processInstanceId, long activityDefinitionId, RemoteArtifact artifact, String ipAddress, String sessionId) throws ServerSideException;
    public void updateActivity(long processInstanceId, long activityDefinitionId, RemoteArtifact artifact, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteActivityDefinition getNextActivityForProcessInstance(long processInstanceId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteActivityDefinition> getProcessInstanceActivitiesPath(long processInstanceId, String ipAddress, String sessionId) throws ServerSideException;
    public long createProcessInstance(long processDefinitionId, String processInstanceName, String processInstanceDescription, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteProcessDefinition getProcessDefinition(long processDefinitionId, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteProcessDefinition(long processDefinitionId, String ipAddress, String sessionId) throws ServerSideException;
    public void updateProcessDefinition(long processDefinitionId, List<StringPair> properties, byte[] structure, String ipAddress, String sessionId) throws ServerSideException;
    public long createProcessDefinition(String name, String description, String version, boolean enabled, byte[] structure, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteProcessInstance> getProcessInstances(long processDefinitionId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteProcessDefinition> getProcessDefinitions(String ipAddress, String sessionId) throws ServerSideException;
    public RemoteProcessInstance getProcessInstance(long processInstanceId, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteProcessInstance(long processInstanceId, String ipAddress, String sessionId) throws ServerSideException;
    public void reloadProcessDefinitions(String ipAddress, String sessionId) throws ServerSideException;
    public RemoteKpiResult executeActivityKpiAction(String kpiActionName, RemoteArtifact remoteArtifact, long processDefinitionId, long activityDefinitionId, String ipAddress, String sessionId) throws ServerSideException;
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Configuration Values">
    public long createConfigurationVariable(long configVariablesPoolId, String name, String description, int type, boolean masked, String valueDefinition, String ipAddress, String sessionId) throws ServerSideException;
    public void updateConfigurationVariable(String name, String propertyToUpdate, String newValue, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteConfigurationVariable(String name, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteConfigurationVariable getConfigurationVariable(String name, String ipAddress, String sessionId) throws ServerSideException;
    public Object getConfigurationVariableValue(String name, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteConfigurationVariable> getConfigurationVariablesInPool(long parentPoolId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemotePool> getConfigurationVariablesPools(String ipAddress, String sessionId) throws ServerSideException;
    public long createConfigurationVariablesPool(String name, String description, String ipAddress, String sessionId) throws ServerSideException;
    public void updateConfigurationVariablesPool(long poolId, String propertyToUpdate, String value, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteConfigurationVariablesPool(long poolId, String ipAddress, String sessionId) throws ServerSideException;
    //</editor-fold>
    //<editor-fold desc="Validators" defaultstate="collapsed">
    public long createValidatorDefinition(String name, String description, String classToBeApplied, String script, boolean enabled, String ipAddress, String sessionId) 
            throws ServerSideException;

    public void updateValidatorDefinition(long validatorDefinitionId, String name, String description, String classToBeApplied, String script, Boolean enabled, String ipAddress, String sessionId) 
            throws ServerSideException;

    public List<RemoteValidatorDefinition> getValidatorDefinitionsForClass(String className, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteValidator> runValidationsForObject(String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException;
    
    public void deleteValidatorDefinition(long validatorDefinitionId, String ipAddress, String sessionId) throws ServerSideException;
    //</editor-fold>
}

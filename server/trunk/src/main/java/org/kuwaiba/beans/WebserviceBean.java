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
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteInventoryProxy;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteKpiResult;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteFileObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteFileObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.application.RemotePool;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteQuery;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteQueryLight;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationConfiguration;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationGroup;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationProvider;
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
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteMPLSConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLightList;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectRelatedObjects;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectSpecialRelationships;
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
    
    public List<RemoteClassMetadataLight> getUpstreamClassHierarchy(String className, boolean includeSelf, String ipAddress, String sessionId) throws ServerSideException;
    
    public void addPossibleChildren(long parentClassId, long[] possibleChildren, String ipAddress, String sessionId) throws ServerSideException;
    
    public void addPossibleSpecialChildren(long parentClassId, long[] possibleSpecialChildren, String ipAddress, String sessionId) throws ServerSideException;
    
    public void addPossibleChildren(String parentClassName, String[] newPossibleChildren, String ipAddress, String sessionId) throws ServerSideException;
    
    public void addPossibleSpecialChildren(String parentClassName, String[] possibleChildren, String ipAddress, String sessionId) throws ServerSideException;

    public void removePossibleChildren(long parentClassId, long[] childrenToBeRemoved, String ipAddress, String sessionId) throws ServerSideException;
    
    public void removePossibleSpecialChildren(long parentClassId, long[] specialChildrenToBeRemoved, String ipAddress, String sessionId) throws ServerSideException;

    public String getAttributeValueAsString(String objectClass, String objectId, String attributeName, String ipAddress, String sessionId) throws ServerSideException;
    
    public HashMap<String, String> getAttributeValuesAsString(String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Business methods. Click on the + sign on the left to edit the code.">
    public List<RemoteObjectLight> getObjectChildren(String oid, long objectClassId, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getObjectChildren(String objectClassName, String oid, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getSiblings(String objectClassName, String oid, int maxResults, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteObject> getChildrenOfClass(String parentOid, String parentClass,String classToFilter, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getChildrenOfClassLight(String parentOid, String parentClass,String classToFilter, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getSpecialChildrenOfClassLight(String parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getChildrenOfClassLightRecursive(String parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    
    public RemoteObject getObject(String objectClass, String oid, String ipAddress, String sessionId) throws ServerSideException;

    public RemoteObjectLight getObjectLight(String objectClass, String oid, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObject> getObjectsWithFilter (String className, String filterName, String filterValue, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getObjectsWithFilterLight (String className, String filterName, String filterValue, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObjectLight getCommonParent(String aObjectClass, String aOid, String bObjectClass, String bOid, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getSuggestedObjectsWithFilter(String filter, int limit, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getSuggestedObjectsWithFilter(String filter, String superClass, int limit, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObjectLight getParent(String objectClass, String oid, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getParents(String objectClass, String oid, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getParentsUntilFirstOfClass(String objectClassName, String oid, String objectToMatchClassName, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObjectLight getFirstParentOfClass(String objectClassName, String oid, String objectToMatchClassName, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObject getParentOfClass(String objectClass, String oid, String parentClass, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteObjectLight> getSpecialAttribute(String objectClass, String objectId, String attributeName, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObjectSpecialRelationships getSpecialAttributes(String objectClass, String oid, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getObjectSpecialChildren(String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException;

    public void updateObject(String className, String oid, List<StringPair> attributesToBeUpdated, String ipAddress, String sessionId) throws ServerSideException;

    public String createObject(String className, String parentClassName, String parentOid, String[] attributeNames, String[] attributeValues, String templateId, String ipAddress, String sessionId) throws ServerSideException;
    public String createSpecialObject(String className, String parentObjectClassName, String parentOid, String[] attributeNames, String[] attributeValues, String templateId, String ipAddress, String sessionId) throws ServerSideException;
    
    public String createListTypeItem(String className, String name, String displayName, String ipAddress, String sessionId) throws ServerSideException;

    public void deleteListTypeItem(String className, String oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteObjectLight> getListTypeItems(String className, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getObjectsOfClassLight(String className, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObject> getObjectsOfClass(String className, int maxResults, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteClassMetadataLight[] getInstanceableListTypes(String ipAddress, String sessionId) throws ServerSideException;

    public boolean canDeleteObject(String className, String oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteObject(String className, String oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteObjects(String classNames[], String[] oids, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;

    public void moveObjectsToPool(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String ipAddress, String sessionId) throws ServerSideException;
    public void moveObjects(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String ipAddress, String sessionId) throws ServerSideException;
    public void moveSpecialObjects(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String ipAddress, String sessionId) throws ServerSideException;
    public void movePoolItem(String poolId, String poolItemClassName, String poolItemId, String ipAddress, String sessionId) throws ServerSideException;

    public String[] copyObjects(String targetClass, String targetOid, String[] templateClasses, String[] templateOids, boolean recursive, String ipAddress, String sessionId) throws ServerSideException;
    public String[] copySpecialObjects(String targetClass, String targetOid, String[] templateClasses, String[] templateOids, boolean recursive, String ipAddress, String sessionId) throws ServerSideException;
    public String copyPoolItem(String poolId, String poolItemClassName, String poolItemId, boolean recursive, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteAttributeMetadata> getMandatoryAttributesInClass(String className, String ipAddress, String sessionId)  throws ServerSideException;
        
    public String[] createBulkObjects(String className, String parentClassName, String parentOid, int numberOfObjects, String namePattern, String ipAddress, String sessionId) throws ServerSideException;
    public String[] createBulkSpecialObjects(String className, String parentClassName, String parentId, int numberOfSpecialObjects, String namePattern, String ipAddress, String sessionId) throws ServerSideException;
    //Physical connections
    public void connectMirrorPort(String[] aObjectClass, String[] aObjectId, String[] bObjectClass, String[] bObjectId, String ipAddress, String sessionId) throws ServerSideException;
    public void connectMirrorMultiplePort(String aObjectClass, String aObjectId, List<String> bObjectClasses, List<String>  bObjectIds, String ipAddress, String sessionId) throws ServerSideException;
    public void releaseMirrorPort(String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException;
    public void releaseMirrorMultiplePort(String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException;
    public String createPhysicalConnection(String aObjectClass, String aObjectId, String bObjectClass, String bObjectId, String name, String connectionClass, String templateId, String ipAddress, String sessionId) throws ServerSideException;
    public String[] createPhysicalConnections(String[] aObjectClass, String[] aObjectId, String[] bObjectClass, String[] bObjectId, String name, String connectionClass, String templateId, String ipAddress, String sessionId) throws ServerSideException;
    public void deletePhysicalConnection(String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObjectLight[] getPhysicalConnectionEndpoints(String connectionClass, String connectionId, String ipAddress, String sessionId) throws ServerSideException;
    
    public void connectPhysicalLinks(String[] sideAClassNames, String[] sideAIds, String[] linksClassNames, String[] linksIds, String[] sideBClassNames, String[] sideBIds, String ipAddress, String sessionId) throws ServerSideException;
    
    public void connectPhysicalContainers(String[] sideAClassNames, String[] sideAIds, String[] containerssClassNames, String[] containersIds, String[] sideBClassNames, String[] sideBIds, String ipAddress, String sessionId) throws ServerSideException;
    public void reconnectPhysicalConnection(String connectionClass, String connectionId, String newASideClass, String newASideId, String newBSideClass, String newBSideId, String ipAddress, String sessionId) throws ServerSideException;
    public void disconnectPhysicalConnection(String connectionClass, String connectionId, int sideToDisconnect, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteObjectLight> getPhysicalPath(String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObjectRelatedObjects getPhysicalTree(String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException;
    @Deprecated
    public RemoteLogicalConnectionDetails getLogicalLinkDetails(String linkClass, String linkId, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteViewObject validateSavedE2EView(List<String> linkClasses, List<String> linkIds, RemoteViewObject savedView, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteViewObject getE2EMap(List<String> linkClasses, List<String> linkIds, boolean includePhyscialLinks, boolean includPhysicalPaths, boolean includeLogicalPaths, boolean includeVlans, boolean includeBDIs, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getContainersBetweenObjects(String objectAClassName, String objectAId, String objectBClassName, String objectBId, String containerClassName, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLightList> getPhysicalConnectionsInObject(String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getLogicalConnectionsInObject(String objectClass, String objectId, 
            String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObject getLinkConnectedToPort(String portClassName, String portId, String ipAddress, String sessionId) throws ServerSideException;
    //Service Manager
    public void associateObjectToService(String objectClass, String objectId, String serviceClass, String serviceId, String ipAddress, String sessionId) throws ServerSideException;
    public void associateObjectsToService(String[] objectClass, String[] objectId, String serviceClass, String serviceId, String ipAddress, String sessionId) throws ServerSideException;
    public void releaseObjectFromService(String serviceClass, String serviceId, String otherObjectId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getServiceResources(String serviceClass, String serviceId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getServicesForCustomer(String customerClass, String customerId, int limit, String ipAddress, String sessionId) throws ServerSideException;
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Application methods. Click on the + sign on the left to edit the code.">
    public void setUserProperties(long oid, String userName, String password, String firstName,
            String lastName, int enabled, int type, String email, String ipAddress, String sessionId)
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
        String lastName, boolean enabled, int type, String email, List<PrivilegeInfo> privileges, 
        long defaultGroupId, String ipAddress, String sessionId) throws ServerSideException;

    public void setGroupProperties(long oid, String groupName, String description, String ipAddress, String sessionId)throws ServerSideException;

    public void deleteUsers(long[] oids, String ipAddress, String sessionId)throws ServerSideException;

    public void deleteGroups(long[] oids, String ipAddress, String sessionId)
            throws ServerSideException;
    
    public long createListTypeItemRelatedView(String listTypeItemId, String listTypeItemClassName, String viewClassName, 
        String name, String description, byte [] structure, byte [] background, String ipAddress, String sessionId) 
        throws ServerSideException;
    
    public void updateListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId, 
        String name, String description, byte[] structure, byte[] background, String ipAddress, String sessionId) 
        throws ServerSideException;
    
    public RemoteViewObject getListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId, String ipAddress, String sessionId) 
        throws ServerSideException;
    
    public RemoteViewObjectLight[] getListTypeItemRelatedViews(String listTypeItemId, String listTypeItemClass, int limit, String ipAddress, String sessionId) throws ServerSideException;
    
    public void deleteListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteObjectLight> getListTypeItemUses(String listTypeItemClass, String listTypeItemId, int limit, 
        String ipAddress, String sessionId) throws ServerSideException;
    
    public RemoteViewObject getObjectRelatedView(String oid, String objectClass, long viewId, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteViewObjectLight> getObjectRelatedViews(String oid, String objectClass, int viewType, int limit, String ipAddress, String sessionId) throws ServerSideException;

    public RemoteViewObject getGeneralView(long viewId, String ipAddress, String sessionId) throws ServerSideException;
    
    public RemoteViewObjectLight[] getGeneralViews(String viewClassName, int limit, String ipAddress, String sessionId) throws ServerSideException;

    public long createObjectRelatedView(String objectId, String objectClass, String name, String description, String viewClassName, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException;

    public long createGeneralView(String viewClass, String name, String description, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException;

    public void updateObjectRelatedView(String objectOid, String objectClass, long viewId, String viewName, String viewDescription, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException;

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
    public String createRootPool(String name, String description, String instancesOfClass, int type, String ipAddress, String sessionId)
            throws ServerSideException;
    
    public String createPoolInObject(String parentClassname, String parentId, String name, String description, String instancesOfClass, int type, String ipAddress, String sessionId)
            throws ServerSideException;
    
    public String createPoolInPool(String parentId, String name, String description, String instancesOfClass, int type, String ipAddress, String sessionId) 
            throws ServerSideException;
    
    public String createPoolItem(String poolId, String className, String[] attributeNames, String[] attributeValues, String templateId, String ipAddress, String sessionId) throws ServerSideException;
    
    public void deletePools(String[] ids, String ipAddress, String sessionId) throws ServerSideException;
    public void setPoolProperties(String poolId, String name, String description, String ipAddress, String sessionId) throws ServerSideException;
    
    public RemotePool getPool(String poolId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemotePool> getRootPools(String className, int type, boolean includeSubclasses, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemotePool> getPoolsInObject(String objectClassName, String objectId, String poolClass, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemotePool> getPoolsInPool(String parentPoolId, String poolClass, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteObjectLight> getPoolItems(String poolId, int limit, String ipAddress, String sessionId) throws ServerSideException;
    
    public ApplicationLogEntry[] getBusinessObjectAuditTrail(String objectClass, String objectId, int limit, String ipAddress, String sessionId) throws ServerSideException;
    
    public ApplicationLogEntry[] getGeneralActivityAuditTrail(int page, int limit, String ipAddress, String sessionId) throws ServerSideException;

    public long attachFileToObject(String name, String tags, byte[] file, String className, String objectId, String ipAddress, String sessionId) throws ServerSideException;
    
    public void detachFileFromObject(long fileObjectId, String className, String objectId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteFileObjectLight> getFilesForObject(String className, String objectId, String ipAddress, String sessionId) throws ServerSideException;

    public RemoteFileObject getFile(long fileObjectId, String className, String objectId, String ipAddress, String sessionId) throws ServerSideException;
    
    public void updateFileProperties(long fileObjectId, List<StringPair> properties,String className, String objectId, String ipAddress, String sessionId) throws ServerSideException;
    
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
    
    public String createContact(String contactClass, List<StringPair> properties, String customerClassName, String customerId, String ipAddress, String sessionId) throws  ServerSideException;
    public void updateContact(String contactClass, String contactId, List<StringPair> properties, String ipAddress, String sessionId) throws  ServerSideException;
    public void deleteContact(String contactClass, String contactId, String ipAddress, String sessionId) throws  ServerSideException;
    public RemoteContact getContact(String contactClass, String contactId, String ipAddress, String sessionId) throws  ServerSideException;
    public List<RemoteContact> searchForContacts(String searchString, int maxResults, String ipAddress, String sessionId) throws  ServerSideException;
    public List<RemoteContact> getContactsForCustomer(String customerClass, String customerId, String ipAddress, String sessionId) throws  ServerSideException;
    
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
    public String createTemplate(String templateClass, String templateName, String ipAddress, String sessionId) throws ServerSideException;

    public String createTemplateElement(String templateElementClass, String templateElementParentClassName, String templateElementParentId, String templateElementName, String ipAddress, String sessionId) throws ServerSideException;
    
    public String createTemplateSpecialElement(String tsElementClass, String tsElementParentClassName, String tsElementParentId, String tsElementName, String ipAddress, String sessionId) throws ServerSideException;
    
    public String[] createBulkTemplateElement(String templateElementClassName, String templateElementParentClassName, String templateElementParentId, int numberOfTemplateElements, String templateElementNamePattern, String ipAddress, String sessionId) throws ServerSideException;
    
    public String[] createBulkSpecialTemplateElement(String stElementClass, String stElementParentClassName, String stElementParentId, int numberOfTemplateElements, String stElementNamePattern, String ipAddress, String sessionId) throws ServerSideException;

    public void updateTemplateElement(String templateElementClass, String templateElementId, String[] attributeNames, String[] attributeValues, String ipAddress, String sessionId) throws ServerSideException;

    public void deleteTemplateElement(String templateElementClass, String templateElementId, String ipAddress, String sessionId) throws ServerSideException;

    public List<RemoteObjectLight> getTemplatesForClass(String className, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteObjectLight> getTemplateElementChildren(String templateElementClass, 
            String templateElementId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteObjectLight> getTemplateSpecialElementChildren(String tsElementClass, 
            String tsElementId, String ipAddress, String sessionId) throws ServerSideException;
    
    public RemoteObject getTemplateElement(String templateElementClass, String templateElementId, 
            String ipAddress, String sessionId) throws ServerSideException;
    
    public String[] copyTemplateElements(String[] sourceObjectsClassNames, String[] sourceObjectsIds, 
            String newParentClassName, String newParentId, String ipAddress, String sessionId) throws ServerSideException;
    
    public String[] copyTemplateSpecialElements(String[] sourceObjectsClassNames, String[] sourceObjectsIds, 
        String newParentClassName, String newParentId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteObjectLight> getDeviceLayouts(String ipAddress, String sessionId) throws ServerSideException;
        
    public byte[] getDeviceLayoutStructure(String oid, String className, String ipAddress, String sessionId) throws ServerSideException;
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
    
    public byte[] executeClassLevelReport(String objectClassName, String objectId, 
            long reportId, String ipAddress, String sessionId) throws ServerSideException;
   
    public byte[] executeInventoryLevelReport(long reportId, List<StringPair> parameters, 
            String ipAddress, String sessionId) throws ServerSideException;
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Commercial modules data methods">
        // <editor-fold defaultstate="collapsed" desc="SDH Networks Module">
    public String createSDHTransportLink(String classNameEndpointA, String idEndpointA, 
            String classNameEndpointB, String idEndpointB, String linkType, String defaultName, String ipAddress, String sessionId) throws ServerSideException;
    
    public String createSDHContainerLink(String classNameEndpointA, String idEndpointA, 
            String classNameEndpointB, String idEndpointB, String linkType, List<RemoteSDHPosition> positions, String defaultName, String ipAddress, String sessionId) throws ServerSideException;
    
    public String createSDHTributaryLink(String classNameEndpointA, String idEndpointA, 
            String classNameEndpointB, String idEndpointB, String linkType, List<RemoteSDHPosition> positions, String defaultName, String ipAddress, String sessionId) throws ServerSideException;
    
    public void deleteSDHTransportLink(String transportLinkClass, String transportLinkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException;
    
    public void deleteSDHContainerLink(String containerLinkClass, String containerLinkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException;
    
    public void deleteSDHTributaryLink(String tributaryLinkClass, String tributaryLinkId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteObjectLightList> findSDHRoutesUsingTransportLinks(String communicationsEquipmentClassA, 
                                            String  communicationsEquipmentIdA, String communicationsEquipmentClassB, 
                                            String  communicationsEquipmentIB, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteObjectLightList> findSDHRoutesUsingContainerLinks(String communicationsEquipmentClassA, 
                                            String  communicationsEquipmentIdA, String communicationsEquipmentClassB, 
                                            String  communicationsEquipmentIB, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteSDHContainerLinkDefinition> getSDHTransportLinkStructure(String transportLinkClass, String transportLinkId, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteSDHContainerLinkDefinition> getSDHContainerLinkStructure(String transportLinkClass, String transportLinkId, String ipAddress, String sessionId) throws ServerSideException;
        // </editor-fold>   
    
        // <editor-fold defaultstate="collapsed" desc="IP Address Manager module">
        public RemotePool[] getSubnetPools(String parentId, String className, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteObjectLight> getSubnets(String poolId, int limit, String ipAddress, String sessionId) throws ServerSideException;
        public RemoteObject getSubnet(String id, String className, String ipAddress, String sessionId) throws ServerSideException;
        public RemotePool getSubnetPool(String id, String ipAddress, String sessionId) throws ServerSideException;
        public String createSubnetPool(String parentId, String subnetPoolName, String subnetPoolDescription, String className, String ipAddress, String sessionId) throws ServerSideException;
        public String createSubnet(String poolId, String className, List<StringPair> attributes, String ipAddress, String sessionId) throws ServerSideException;
        public void deleteSubnets(String className, List<String> ids, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;
        public void deleteSubnetPools(String[] ids, String ipAddress, String sessionId) throws ServerSideException;
        public String addIPAddress(String id, String parentClassName, List<StringPair> attributesToBeUpdated, String ipAddress, String sessionId) throws ServerSideException;
        public void removeIP(String[] ids, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;
        public void relateIPtoPort(String id, String deviceClassName, String deviceId, String ipAddress, String sessionId) throws ServerSideException;
        @Deprecated
        public void relateSubnetToVlan(String id, String className, String vlanId, String ipAddress, String sessionId) throws ServerSideException;
        @Deprecated
        public void releaseSubnetFromVlan(String vlanId, String id, String ipAddress, String sessionId) throws ServerSideException;
        public void relateSubnetToVrf(String id, String className, String vrfId, String ipAddress, String sessionId) throws ServerSideException;
        public void releasePortFromIP(String deviceClassName, String deviceId, String id, String ipAddress, String sessionId) throws ServerSideException;
        public void releaseSubnetFromVRF(String subnetId, String vrfId, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteObjectLight> getSubnetUsedIps(String id, String className, int limit, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteObjectLight> getSubnetsInSubnet(String id, String className, int limit, String ipAddress, String sessionId)  throws ServerSideException;
        public boolean itOverlaps(String networkIp, String broadcastIp, String ipAddress, String sessionId) throws ServerSideException;
        public void relatePortToInterface(String portId, String portClassName, String interfaceClassName, String interfaceId, String ipAddress, String sessionId) throws ServerSideException;
        public void releasePortFromInterface(String interfaceClassName, String interfaceId ,String portId, String ipAddress, String sessionId) throws ServerSideException;
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc="Contract Manager">
    public void associateObjectsToContract(String[] deviceClass, String[] deviceId, 
            String contractClass, String contractId, String ipAddress, String sessionId) throws ServerSideException;
    
    public void releaseObjectFromContract(String deviceClass, String deviceId, String contractId,
            String ipAddress, String sessionId) throws ServerSideException;    
        //</editor-fold>
      
        // <editor-fold defaultstate="collapsed" desc="MPLS Networks Module">
         public String createMPLSLink(String classNameEndpointA, String idEndpointA, String classNameEndpointB, String idEndpointB, List<StringPair> attributesToBeSet, String ipAddress, String sessionId) throws ServerSideException;
         public RemoteMPLSConnectionDetails getMPLSLinkEndpoints(String connectionId, String ipAddress, String sessionId) throws ServerSideException;
         public void connectMplsLink(String[] sideAClassNames, String[] sideAIds, String[] linksIds, String[] sideBClassNames, String[] sideBIds, String ipAddress, String sessionId) throws ServerSideException;
         public void disconnectMPLSLink(String connectionId, int sideToDisconnect, String ipAddress, String sessionId) throws ServerSideException;
         public void deleteMPLSLink(String linkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException;
        // </editor-fold>
         
        // <editor-fold defaultstate="collapsed" desc="Projects Module">
        public List<RemotePool> getProjectPools(String ipAddress, String sessionId) throws ServerSideException;
        public String addProject(String parentId, String parentClassName, String className, String[] attributeNames, String[] attributeValues, String ipAddress, String sessionId) throws ServerSideException;
        public void deleteProject(String className, String oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;
        public String addActivity(String parentId, String parentClassName, String className, String attributeNames[], String attributeValues[], String ipAddress, String sessionId) throws ServerSideException;
        public void deleteActivity(String className, String oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteObjectLight> getProjectsInProjectPool(String poolId, int limit, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteObjectLight> getAllProjects(String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteObjectLight> getProjectResurces(String projectClass, String projectId, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteObjectLight> getProjectActivities(String projectClass, String projectId, String ipAddress, String sessionId) throws ServerSideException;
        public void associateObjectsToProject(String projectClass, String projectId, String[] objectClass, String[] objectId, String ipAddress, String sessionId) throws ServerSideException;
        public void associateObjectToProject(String projectClass, String projectId, String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException;
        public void releaseObjectFromProject(String objectClass, String objectId, String projectClass, String projectId, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteObjectLight> getProjectsAssociateToObject(String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException;
        public String createProjectPool(String name, String description, String instanceOfClass, String ipAddress, String sessionId) throws ServerSideException;
        // </editor-fold>
        
        //<editor-fold desc="Synchronization API" defaultstate="collapsed">
        public List<RemoteSynchronizationProvider> getSynchronizationProviders(String ipAddress, String sessionId)throws ServerSideException;
        public long createSynchronizationGroup(String name, String ipAddress, String sessionId)throws ServerSideException;
        public void updateSynchronizationGroup(long syncGroupId, List<StringPair> syncGroupProperties, String ipAddress, String sessionId)throws ServerSideException;
        
        public long createSynchronizationDataSourceConfig(String objectId, long syncGroupId, String name, List<StringPair> parameters, String ipAddress, String sessionId) throws ServerSideException;
        public void updateSyncDataSourceConfiguration(long syncDataSourceConfigId, List<StringPair> parameters, String ipAddress, String sessionId)throws ServerSideException;
        public BackgroundJob launchAutomatedSynchronizationTask(long syncGroupId, String ipAddress, String sessionId) throws ServerSideException;
        public BackgroundJob launchSupervisedSynchronizationTask(long syncGroupId, String ipAddress, String sessionId) throws ServerSideException;
        
        public BackgroundJob launchAdHocAutomatedSynchronizationTask(long[] syncDataSourceConfigIds, String syncProvidersName, String ipAddress, String sessionId) throws ServerSideException;
        public BackgroundJob launchAdHocAutomatedSynchronizationTask(long syncGroupId, String syncProvidersName, String ipAddress, String sessionId) throws ServerSideException;
       
        public RemoteSynchronizationGroup getSynchronizationGroup(long syncGroupId, String ipAddress, String sessionId) throws ServerSideException;
        public List<RemoteSynchronizationGroup> getSynchronizationGroups(String ipAddress, String sessionId)throws ServerSideException;
        
        public RemoteSynchronizationConfiguration getSyncDataSourceConfiguration(String objectId, String ipAddress, String sessionId)throws ServerSideException;
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
    public void associatePhysicalNodeToWarehouse(String objectClass, String objectId, String warehouseClass, String warehouseId, String ipAddress, String sessionId) throws ServerSideException;
    public void associatesPhysicalNodeToWarehouse(String[] objetClass, String[] objectId, String warehouseClass, String warehouseId, String ipAddress, String sessionId) throws ServerSideException;
    public void releasePhysicalNodeFromWarehouse(String warehouseClass, String warehouseId, String objectId, String ipAddress, String sessionId) throws ServerSideException;
    public void moveObjectsToWarehousePool(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String ipAddress, String sessionId) throws ServerSideException;
    public void moveObjectsToWarehouse(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObjectLight getPhysicalNodeToObjectInWarehouse(String objectClassName, String objectId, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteObjectLight getWarehouseToObject(String objectClassName, String objectId, String ipAddress, String sessionId) throws ServerSideException;
        // </editor-fold>
    
        //<editor-fold desc="Outside Plant" defaultstate="collapsed">
    public long createOSPView(String name, String description, byte[] content, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteViewObject getOSPView(long viewId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteViewObjectLight> getOSPViews(String ipAddressString, String sessionId) throws ServerSideException;
    public void updateOSPView(long viewId, String name, String description, byte[] content, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteOSPView(long viewId, String ipAddress, String sessionId) throws ServerSideException;
    
    //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="BGP Viewer Module">
        public List<RemoteLogicalConnectionDetails> getBGPMap(List<String> mappedBGPLinksIds, String ipAddress, String sessionId) throws ServerSideException;
        //</editor-fold>
        
    // Bookmarks
    public long createFavoritesFolderForUser(String favoritesFolderName, long userId, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteFavoritesFolders (long[] favoritesFolderId, long userId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteFavoritesFolder> getFavoritesFoldersForUser(long userId, String ipAddress, String sessionId) throws ServerSideException;
    public void addObjectsToFavoritesFolder(String[] objectClass, String[] objectId, long favoritesFolderId, long userId, String ipAddress, String sessionId) throws ServerSideException;
    public void removeObjectsFromFavoritesFolder(String[] objectClass, String[] objectId, long favoritesFolderId, long userId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteObjectLight> getObjectsInFavoritesFolder(long favoritesFolderId, long userId, int limit, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteFavoritesFolder> getFavoritesFoldersForObject(long userId ,String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException;
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
    
    //<editor-fold defaultstate="collapsed" desc="Configuration Variables">
    public long createConfigurationVariable(String configVariablesPoolId, String name, String description, int type, boolean masked, String valueDefinition, String ipAddress, String sessionId) throws ServerSideException;
    public void updateConfigurationVariable(String name, String propertyToUpdate, String newValue, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteConfigurationVariable(String name, String ipAddress, String sessionId) throws ServerSideException;
    public RemoteConfigurationVariable getConfigurationVariable(String name, String ipAddress, String sessionId) throws ServerSideException;
    public Object getConfigurationVariableValue(String name, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteConfigurationVariable> getConfigurationVariablesInPool(String poolId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemotePool> getConfigurationVariablesPools(String ipAddress, String sessionId) throws ServerSideException;
    public String createConfigurationVariablesPool(String name, String description, String ipAddress, String sessionId) throws ServerSideException;
    public void updateConfigurationVariablesPool(String poolId, String propertyToUpdate, String value, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteConfigurationVariablesPool(String poolId, String ipAddress, String sessionId) throws ServerSideException;
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Proxies">
    public String createProxy(String proxyPoolId, String proxyClass, List<StringPair> attributes, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteProxy(String proxyClass, String proxyId, String ipAddress, String sessionId) throws ServerSideException;
    public void updateProxy(String proxyClass, String proxyId, List<StringPair> attributes, String ipAddress, String sessionId) throws ServerSideException;
    public String createProxyPool(String name, String description, String ipAddress, String sessionId) throws ServerSideException;
    public void updateProxyPool(String proxyPoolId, String attributeName, String attributeValue, String ipAddress, String sessionId) throws ServerSideException;
    public void deleteProxyPool(String proxyPoolId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemotePool> getProxyPools(String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteInventoryProxy> getProxiesInPool(String proxyPoolId, String ipAddress, String sessionId) throws ServerSideException;
    public List<RemoteInventoryProxy> getAllProxies(String ipAddress, String sessionId) throws ServerSideException;
    public void associateObjectToProxy(String objectClass, String objectId, String proxyClass, String proxyId, String ipAddress, String sessionId) throws ServerSideException;
    public void releaseObjectFromProxy(String objectClass, String objectId, String proxyClass, String proxyId, String ipAddress, String sessionId) throws ServerSideException;
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
    
    //<editor-fold desc="Kuwaiba 2.1" defaultstate="collapsed">
    public long getObjectChildrenCount(String className, String oid, String ipAddress, String sessionId) throws ServerSideException;
    
    public List<RemoteObjectLight> getObjectChildren(String className, String oid, long skip, long limit, String ipAddress, String sessionId) throws ServerSideException;
    //</editor-fold>
        
    //<editor-fold desc="special explorer actions for VLANs" defaultstate="collapsed">
        public void relatePortsToVlan(List<String> portsIds, List<String> portsClassNames, String vlanId, String ipAddress, String sessionId) throws ServerSideException;
        public void releasePortsFromVlan(List<String> portsIds, String vlanId, String ipAddress, String sessionId) throws ServerSideException;
    //</editor-fold>
}

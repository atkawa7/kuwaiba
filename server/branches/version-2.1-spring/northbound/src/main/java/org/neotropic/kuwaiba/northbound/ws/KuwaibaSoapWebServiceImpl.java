/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.northbound.ws;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.northbound.ws.model.application.ApplicationLogEntry;
import org.neotropic.kuwaiba.northbound.ws.model.application.GroupInfo;
import org.neotropic.kuwaiba.northbound.ws.model.application.GroupInfoLight;
import org.neotropic.kuwaiba.northbound.ws.model.application.PrivilegeInfo;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteActivityDefinition;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteArtifact;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteArtifactDefinition;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteBackgroundJob;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteBusinessRule;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteConfigurationVariable;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteFavoritesFolder;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemotePool;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteProcessDefinition;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteQuery;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteQueryLight;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteReportMetadata;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteReportMetadataLight;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteResultRecord;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteSession;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteSyncAction;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteSyncFinding;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteSyncResult;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteSynchronizationConfiguration;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteSynchronizationGroup;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteSynchronizationProvider;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteTask;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteTaskNotificationDescriptor;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteTaskResult;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteTaskScheduleDescriptor;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteUserInfo;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteUserInfoLight;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteValidator;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteValidatorDefinition;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteViewObject;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteViewObjectLight;
import org.neotropic.kuwaiba.northbound.ws.model.business.AssetLevelCorrelatedInformation;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteContact;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteFileObject;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteFileObjectLight;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteLogicalConnectionDetails;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteMPLSConnectionDetails;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteObject;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteObjectLight;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteObjectLightList;
import org.neotropic.kuwaiba.northbound.ws.model.business.RemoteObjectSpecialRelationships;
import org.neotropic.kuwaiba.northbound.ws.model.business.modules.sdh.RemoteSDHContainerLinkDefinition;
import org.neotropic.kuwaiba.northbound.ws.model.business.modules.sdh.RemoteSDHPosition;
import org.neotropic.kuwaiba.northbound.ws.model.metadata.RemoteAttributeMetadata;
import org.neotropic.kuwaiba.northbound.ws.model.metadata.RemoteClassMetadata;
import org.neotropic.kuwaiba.northbound.ws.model.metadata.RemoteClassMetadataLight;
import org.neotropic.kuwaiba.northbound.ws.todeserialize.TransientQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * SOAP-based web service implementation.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@WebService(endpointInterface = "org.neotropic.kuwaiba.northbound.ws.KuwaibaSoapWebService", 
        serviceName = "KuwaibaService")
@Service
public class KuwaibaSoapWebServiceImpl implements KuwaibaSoapWebService {
    /**
     * Reference to the persistence service to get references to the entity managers.
     */
    @Autowired
    private MetadataEntityManager mem;
    @Autowired
    private ApplicationEntityManager aem;
    @Autowired
    private BusinessEntityManager bem;
    @Autowired
    private TranslationService ts;
    
    @Override
    public RemoteSession createSession(String username, String password, int sessionType) throws ServerSideException {
        try {
            if (aem == null)
                throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
            
            Session session = aem.createSession(username, password, sessionType, "127.0.0.1");
            aem.createGeneralActivityLogEntry(username, ActivityLogEntry.ACTIVITY_TYPE_OPEN_SESSION, String.format("Connected from %s", "127.0.0.1"));
            return new RemoteSession(session.getToken(), session.getUser(), sessionType, "127.0.0.1");
        } catch (InventoryException ex) { // Expected error
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createSession"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void closeSession(String sessionId) throws ServerSideException {
        try {
            aem.closeSession(sessionId, "127.0.0.1");
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "closeSession"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteUserInfo> getUsers(String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getUsers", sessionId);
            List<UserProfile> users = aem.getUsers();
            List<RemoteUserInfo> remoteUsers = new ArrayList<>();
            
            users.stream().forEach(aUser -> remoteUsers.add(new RemoteUserInfo(aUser)));
            
            return remoteUsers;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteUserInfo> getUsersInGroup(long groupId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<GroupInfoLight> getGroupsForUser(long userId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<GroupInfo> getGroups(String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createUser(String username, String password, String firstName, String lastName, boolean enabled, int type, String email, List<PrivilegeInfo> privileges, long defaultGroupId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setUserProperties(long oid, String username, String firstName, String lastName, String password, int enabled, int type, String email, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addUserToGroup(long userId, long groupId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeUserFromGroup(long userId, long groupId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPrivilegeToUser(long userId, String featureToken, int accessLevel, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPrivilegeToGroup(long groupId, String featureToken, int accessLevel, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removePrivilegeFromUser(long userId, String featureToken, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removePrivilegeFromGroup(long groupId, String featureToken, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createGroup(String groupName, String description, List<Long> users, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setGroupProperties(long oid, String groupName, String description, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteUsers(long[] oids, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteGroups(long[] oids, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteViewObject getObjectRelatedView(String oid, String objectClass, long viewId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteViewObjectLight> getObjectRelatedViews(String oid, String objectClass, int viewType, int limit, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteViewObjectLight[] getGeneralViews(String viewClass, int limit, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteViewObject getGeneralView(long viewId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createListTypeItemRelatedView(String listTypeItemId, String listTypeItemClassName, String viewClassName, String name, String description, byte[] structure, byte[] background, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId, String name, String description, byte[] structure, byte[] background, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteViewObject getListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteViewObjectLight[] getListTypeItemRelatedViews(String listTypeItemId, String listTypeItemClass, int limit, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getDeviceLayouts(String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getDeviceLayoutStructure(String oid, String className, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createObjectRelatedView(String objectId, String objectClass, String name, String description, String viewClassName, byte[] structure, byte[] background, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createGeneralView(String viewClass, String name, String description, byte[] structure, byte[] background, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateObjectRelatedView(String objectOid, String objectClass, long viewId, String viewName, String viewDescription, byte[] structure, byte[] background, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateGeneralView(long viewId, String viewName, String viewDescription, byte[] structure, byte[] background, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteGeneralView(long[] oids, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createListTypeItem(String className, String name, String displayName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteListTypeItem(String className, String oid, boolean releaseRelationships, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getListTypeItems(String className, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteClassMetadataLight[] getInstanceableListTypes(String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteResultRecord[] executeQuery(TransientQuery query, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createQuery(String queryName, long ownerOid, byte[] queryStructure, String description, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveQuery(long queryOid, String queryName, long ownerOid, byte[] queryStructure, String description, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteQuery(long queryOid, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteQueryLight[] getQueries(boolean showPublic, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteQuery getQuery(long queryOid, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getClassHierarchy(boolean showAll, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createRootPool(String name, String description, String instancesOfClass, int type, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createPoolInObject(String parentClassname, String parentId, String name, String description, String instancesOfClass, int type, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createPoolInPool(String parentId, String name, String description, String instancesOfClass, int type, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createPoolItem(String poolId, String className, String[] attributeNames, String[] attributeValues, String templateId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deletePools(String[] ids, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemotePool> getRootPools(String className, int type, boolean includeSubclasses, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemotePool> getPoolsInObject(String objectClassName, String objectId, String poolClass, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemotePool> getPoolsInPool(String parentPoolId, String poolClass, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemotePool getPool(String poolId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setPoolProperties(String poolId, String name, String description, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getPoolItems(String poolId, int limit, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createTask(String name, String description, boolean enabled, boolean commitOnExecute, String script, List<StringPair> parameters, RemoteTaskScheduleDescriptor schedule, RemoteTaskNotificationDescriptor notificationType, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateTaskProperties(long taskId, String propertyName, String propertyValue, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateTaskParameters(long taskId, List<StringPair> parameters, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateTaskSchedule(long taskId, RemoteTaskScheduleDescriptor schedule, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateTaskNotificationType(long taskId, RemoteTaskNotificationDescriptor notificationType, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteTask getTask(long taskId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteTask> getTasks(String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteTask> getTasksForUser(long userId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteUserInfoLight> getSubscribersForTask(long taskId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteTask(long taskId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void subscribeUserToTask(long userId, long taskId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void unsubscribeUserFromTask(long userId, long taskId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteTaskResult executeTask(long taskId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createContact(String contactClass, List<StringPair> properties, String customerClassName, String customerId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateContact(String contactClass, String contactId, List<StringPair> properties, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteContact(String contactClass, String contactId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteContact getContact(String contactClass, String contactId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteContact> searchForContacts(String searchString, int maxResults, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteContact> getContactsForCustomer(String customerClass, String customerId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createConfigurationVariable(String configVariablesPoolId, String name, String description, int type, boolean masked, String valueDefinition, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateConfigurationVariable(String name, String propertyToUpdate, String newValue, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteConfigurationVariable(String name, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteConfigurationVariable getConfigurationVariable(String name, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteConfigurationVariable> getConfigurationVariablesInPool(String poolId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemotePool> getConfigurationVariablesPools(String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createConfigurationVariablesPool(String name, String description, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateConfigurationVariablesPool(String poolId, String propertyToUpdate, String value, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteConfigurationVariablesPool(String poolId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createValidatorDefinition(String name, String description, String classToBeApplied, String script, boolean enabled, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateValidatorDefinition(long validatorDefinitionId, String name, String description, String classToBeApplied, String script, Boolean enabled, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteValidatorDefinition> getValidatorDefinitionsForClass(String className, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteValidator> runValidationsForObject(String objectClass, long objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteValidatorDefinition(long validatorDefinitionId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getObjectChildrenForClassWithId(String oid, long objectClassId, int maxResults, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getObjectChildren(String objectClassName, String oid, int maxResults, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getSiblings(String objectClassName, String oid, int maxResults, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObject> getChildrenOfClass(String parentOid, String parentClass, String childrenClass, int maxResults, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getChildrenOfClassLightRecursive(String parentOid, String parentClass, String childrenClass, int maxResults, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getChildrenOfClassLight(String parentOid, String parentClass, String childrenClass, int maxResults, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getSpecialChildrenOfClassLight(String parentOid, String parentClass, String classToFilter, int maxResults, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteObject getObject(String objectClass, String oid, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteObjectLight getObjectLight(String objectClass, String oid, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getObjectsWithFilterLight(String className, String filterName, String filterValue, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObject> getObjectsWithFilter(String className, String filterName, String filterValue, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getObjectsOfClassLight(String className, int maxResults, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteObjectLight getCommonParent(String aObjectClass, String aOid, String bObjectClass, String bOid, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteObjectLight getParent(String objectClass, String oid, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getParents(String objectClass, String oid, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteObjectSpecialRelationships getSpecialAttributes(String objectClass, String oid, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getParentsUntilFirstOfClass(String objectClass, String oid, String objectToMatchClassName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteObjectLight getFirstParentOfClass(String objectClass, String oid, String objectToMatchClassName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteObject getParentOfClass(String objectClass, String oid, String parentClass, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getSpecialAttribute(String objectClass, String oid, String attributeName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getObjectSpecialChildren(String objectClass, String objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateObject(String className, String id, List<StringPair> attributes, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createObject(String className, String parentObjectClassName, String parentOid, String[] attributeNames, String[] attributeValues, String templateId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createSpecialObject(String className, String parentObjectClassName, String parentOid, String[] attributeNames, String[] attributeValues, String templateId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteObject(String className, String oid, boolean releaseRelationships, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteObjects(String[] classNames, String[] oids, boolean releaseRelationships, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void moveObjectsToPool(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void moveObjects(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void moveSpecialObjects(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void movePoolItemToPool(String poolId, String poolItemClassName, String poolItemId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] copyObjects(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, boolean recursive, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] copySpecialObjects(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, boolean recursive, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void copyPoolItemToPool(String poolId, String poolItemClassName, String poolItemId, boolean recursive, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteAttributeMetadata> getMandatoryAttributesInClass(String className, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] createBulkObjects(String className, String parentClassName, String parentOid, int numberOfObjects, String namePattern, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] createBulkSpecialObjects(String className, String parentClassName, String parentId, int numberOfSpecialObjects, String namePattern, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connectMirrorPort(String[] aObjectClass, String[] aObjectId, String[] bObjectClass, String[] bObjectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void releaseMirrorPort(String objectClass, String objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createPhysicalConnection(String aObjectClass, String aObjectId, String bObjectClass, String bObjectId, String name, String connectionClass, String templateId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] createPhysicalConnections(String[] aObjectClasses, String[] aObjectIds, String[] bObjectClasses, String[] bObjectIds, String name, String connectionClass, String templateId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteObjectLight[] getPhysicalConnectionEndpoints(String connectionClass, String connectionId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteLogicalConnectionDetails getLogicalLinkDetails(String linkClass, String linkId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteViewObject validateSavedE2EView(List<String> linkClasses, List<String> linkIds, RemoteViewObject savedView, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteViewObject getE2View(List<String> linkClasses, List<String> linkIds, boolean includeVLANs, boolean includeBDIs, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getContainersBetweenObjects(String objectAClass, String objectAId, String objectBClass, String objectBId, String containerClass, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLightList> getPhysicalConnectionsInObject(String objectClass, String objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteObject getLinkConnectedToPort(String portClassName, String portId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getPhysicalPath(String objectClass, String objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connectPhysicalLinks(String[] sideAClassNames, String[] sideAIds, String[] linksClassNames, String[] linksIds, String[] sideBClassNames, String[] sideBIds, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connectPhysicalContainers(String[] sideAClassNames, String[] sideAIds, String[] containersClassNames, String[] containersIds, String[] sideBClassNames, String[] sideBIds, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void disconnectPhysicalConnection(String connectionClass, String connectionId, int sideToDisconnect, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reconnectPhysicalConnection(String connectionClass, String connectionId, String newASideClass, String newASideId, String newBSideClass, String newBSideId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deletePhysicalConnection(String objectClass, String objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void associateObjectToService(String objectClass, String objectId, String serviceClass, String serviceId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void associateObjectsToService(String[] objectClass, String[] objectId, String serviceClass, String serviceId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void releaseObjectFromService(String serviceClass, String serviceId, String targetId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getServiceResources(String serviceClass, String serviceId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApplicationLogEntry[] getBusinessObjectAuditTrail(String objectClass, String objectId, int limit, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApplicationLogEntry[] getGeneralActivityAuditTrail(int page, int limit, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long attachFileToObject(String name, String tags, byte[] file, String className, String objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void detachFileFromObject(long fileObjectId, String className, String objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteFileObjectLight> getFilesForObject(String className, String objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteFileObject getFile(long fileObjectId, String className, String objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateFileProperties(long fileObjectId, List<StringPair> properties, String className, String objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createClass(String className, String displayName, String description, boolean isAbstract, boolean isCustom, boolean isCountable, boolean isInDesign, String parentClassName, byte[] icon, byte[] smallIcon, int color, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setClassProperties(long classId, String className, String displayName, String description, byte[] smallIcon, byte[] icon, int color, Boolean isAbstract, Boolean isInDesign, Boolean isCustom, Boolean isCountable, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasAttribute(String className, String attributeName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteAttributeMetadata getAttribute(String className, String attributeName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteAttributeMetadata getAttributeForClassWithId(String classId, String attributeName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void createAttribute(String className, String name, String displayName, String type, String description, boolean administrative, boolean visible, boolean isReadOnly, boolean noCopy, boolean unique, boolean mandatory, boolean multiple, int order, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void createAttributeForClassWithId(long ClassId, String name, String displayName, String type, String description, boolean administrative, boolean visible, boolean readOnly, boolean noCopy, boolean unique, boolean mandatory, boolean multiple, int order, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAttributeProperties(String className, long attributeId, String name, String displayName, String description, String type, Boolean administrative, Boolean mandatory, Boolean multiple, Boolean noCopy, Boolean readOnly, Boolean unique, Boolean visible, Integer order, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAttributePropertiesForClassWithId(long classId, long attributeId, String name, String displayName, String description, String type, Boolean administrative, Boolean mandatory, Boolean multiple, Boolean noCopy, Boolean readOnly, Boolean unique, Boolean visible, Integer order, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteAttribute(String className, String attributeName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteAttributeForClassWithId(long classId, String attributeName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteClassMetadata getClass(String className, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteClassMetadata getClassWithId(long classId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteClassMetadataLight> getSubClassesLight(String className, boolean includeAbstractClasses, boolean includeSelf, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteClassMetadataLight> getSubClassesLightNoRecursive(String className, boolean includeAbstractClasses, boolean includeSelf, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteClassMetadata> getAllClasses(boolean includeListTypes, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteClassMetadataLight> getAllClassesLight(boolean includeListTypes, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteClass(String className, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteClassWithId(long classId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteClassMetadataLight> getPossibleChildren(String parentClassName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteClassMetadataLight> getPossibleSpecialChildren(String parentClassName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteClassMetadataLight> getPossibleChildrenNoRecursive(String parentClassName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteClassMetadataLight> getPossibleSpecialChildrenNoRecursive(String parentClassName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPossibleChildrenForClassWithId(long parentClassId, long[] newPossibleChildren, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPossibleSpecialChildrenWithId(long parentClassId, long[] possibleSpecialChildren, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPossibleChildren(String parentClassName, String[] childrenToBeAdded, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPossibleSpecialChildren(String parentClassName, String[] possibleSpecialChildren, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removePossibleChildrenForClassWithId(long parentClassId, long[] childrenToBeRemoved, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removePossibleSpecialChildren(long parentClassId, long[] specialChildrenToBeRemoved, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteClassMetadataLight> getUpstreamContainmentHierarchy(String className, boolean recursive, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteClassMetadataLight> getUpstreamSpecialContainmentHierarchy(String className, boolean recursive, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteClassMetadataLight> getUpstreamClassHierarchy(String className, boolean includeSelf, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSubClassOf(String className, String allegedParentClass, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String bulkUpload(byte[] file, int commitSize, int dataType, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] downloadBulkLoadLog(String fileName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createTemplate(String templateClass, String templateName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createTemplateElement(String templateElementClass, String templateElementParentClassName, String templateElementParentId, String templateElementName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createTemplateSpecialElement(String tsElementClass, String tsElementParentClassName, String tsElementParentId, String tsElementName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] createBulkTemplateElement(String templateElementClassName, String templateElementParentClassName, String templateElementParentId, int numberOfTemplateElements, String templateElementNamePattern, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] createBulkSpecialTemplateElement(String stElementClass, String stElementParentClassName, String stElementParentId, int numberOfTemplateElements, String stElementNamePattern, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateTemplateElement(String templateElementClass, String templateElementId, String[] attributeNames, String[] attributeValues, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteTemplateElement(String templateElementClass, String templateElementId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getTemplatesForClass(String className, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] copyTemplateElements(String[] sourceObjectsClassNames, String[] sourceObjectsIds, String newParentClassName, String newParentId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] copyTemplateSpecialElements(String[] sourceObjectsClassNames, String[] sourceObjectsIds, String newParentClassName, String newParentId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getTemplateElementChildren(String templateElementClass, String templateElementId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getTemplateSpecialElementChildren(String tsElementClass, String tsElementId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteObject getTemplateElement(String templateElementClass, String templateElementId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createClassLevelReport(String className, String reportName, String reportDescription, String script, int outputType, boolean enabled, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createInventoryLevelReport(String reportName, String reportDescription, String script, int outputType, boolean enabled, List<StringPair> parameters, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteReport(long reportId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateReport(long reportId, String reportName, String reportDescription, Boolean enabled, Integer type, String script, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateReportParameters(long reportId, List<StringPair> parameters, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteReportMetadataLight> getClassLevelReports(String className, boolean recursive, boolean includeDisabled, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteReportMetadataLight> getInventoryLevelReports(boolean includeDisabled, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteReportMetadata getReport(long reportId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] executeClassLevelReport(String objectClassName, String objectId, long reportId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] executeInventoryLevelReport(long reportId, List<StringPair> parameters, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createFavoritesFolderForUser(String favoritesFolderName, long userId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteFavoritesFolders(long[] favoritesFolderId, long userId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteFavoritesFolder> getFavoritesFoldersForUser(long userId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addObjectsToFavoritesFolder(String[] objectClass, String[] objectId, long favoritesFolderId, long userId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeObjectsFromFavoritesFolder(String[] objectClass, String[] objectId, long favoritesFolderId, long userId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getObjectsInFavoritesFolder(long favoritesFolderId, long userId, int limit, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteFavoritesFolder> getFavoritesFoldersForObject(long userId, String objectClass, String objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteFavoritesFolder getFavoritesFolder(long favoritesFolderId, long userId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateFavoritesFolder(long favoritesFolderId, String favoritesFolderName, long userId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createBusinessRule(String ruleName, String ruleDescription, int ruleType, int ruleScope, String appliesTo, String ruleVersion, List<String> constraints, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteBusinessRule(long businessRuleId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteBusinessRule> getBusinessRules(int type, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteSyncFinding> launchSupervisedSynchronizationTask(long syncGroupId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteSyncResult> launchAutomatedSynchronizationTask(long syncGroupId, String providersName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteSyncResult> launchAdHocAutomatedSynchronizationTask(long[] synDsConfigIds, String providersName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteSyncResult> executeSyncActions(long syncGroupId, List<RemoteSyncAction> actions, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteBackgroundJob> getCurrentJobs(String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void killJob(long jobId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createSDHTransportLink(String classNameEndpointA, String idEndpointA, String classNameEndpointB, String idEndpointB, String linkType, String defaultName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createSDHContainerLink(String classNameEndpointA, String idEndpointA, String classNameEndpointB, String idEndpointB, String linkType, List<RemoteSDHPosition> positions, String defaultName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createSDHTributaryLink(String classNameEndpointA, String idEndpointA, String classNameEndpointB, String idEndpointB, String linkType, List<RemoteSDHPosition> positions, String defaultName, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteSDHTransportLink(String transportLinkClass, String transportLinkId, boolean forceDelete, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteSDHContainerLink(String containerLinkClass, String containerLinkId, boolean forceDelete, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteSDHTributaryLink(String tributaryLinkClass, String tributaryLinkId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLightList> findSDHRoutesUsingTransportLinks(String communicationsEquipmentClassA, String communicationsEquipmentIdA, String communicationsEquipmentClassB, String communicationsEquipmentIB, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLightList> findSDHRoutesUsingContainerLinks(String communicationsEquipmentClassA, String communicationsEquipmentIdA, String communicationsEquipmentClassB, String communicationsEquipmentIB, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteSDHContainerLinkDefinition> getSDHTransportLinkStructure(String transportLinkClass, String transportLinkId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteSDHContainerLinkDefinition> getSDHContainerLinkStructure(String containerLinkClass, String containerLinkId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemotePool[] getSubnetPools(String parentId, String className, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getSubnets(String poolId, int limit, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createSubnetPool(String parentId, String subnetPoolName, String subnetPoolDescription, String className, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createSubnet(String poolId, String className, List<StringPair> attributes, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteSubnetPools(String[] ids, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteSubnets(String className, List<String> oids, boolean releaseRelationships, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteObject getSubnet(String id, String className, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemotePool getSubnetPool(String subnetPoolId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String addIPAddress(String id, String parentClassName, List<StringPair> attributes, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeIP(String[] oids, boolean releaseRelationships, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getSubnetUsedIps(String id, int limit, String className, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getSubnetsInSubnet(String id, int limit, String className, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void relateSubnetToVlan(String id, String className, String vlanId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void releaseSubnetFromVlan(String subnetId, String vlanId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void releaseSubnetFromVRF(String subnetId, String vrfId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void relateSubnetToVrf(String id, String className, String vrfId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void relateIPtoPort(String id, String portClassName, String portId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean itOverlaps(String networkIp, String broadcastIp, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void releasePortFromIP(String deviceClassName, String deviceId, String id, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void associateObjectsToContract(String[] objectClass, String[] objectId, String contractClass, String contractId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void releaseObjectFromContract(String objectClass, String objectId, String contractId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createMPLSLink(String classNameEndpointA, String idEndpointA, String classNameEndpointB, String idEndpointB, List<StringPair> attributesToBeSet, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteMPLSConnectionDetails getMPLSLinkEndpoints(String connectionId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void connectMplsLink(String[] sideAClassNames, String[] sideAIds, String[] linksIds, String[] sideBClassNames, String[] sideBIds, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void disconnectMPLSLink(String connectionId, int sideToDisconnect, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteMPLSLink(String linkId, boolean forceDelete, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void relatePortToInterface(String portId, String portClassName, String interfaceClassName, String interfaceId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void releasePortFromInterface(String interfaceClassName, String interfaceId, String portId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemotePool> getProjectPools(String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String addProject(String parentId, String parentClassName, String className, String[] attributeNames, String[] attributeValues, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteProject(String className, String oid, boolean releaseRelationships, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String addActivity(String parentId, String parentClassName, String className, String[] attributeNames, String[] attributeValues, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteActivity(String className, String oid, boolean releaseRelationships, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getProjectsInProjectPool(String poolId, int limit, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getProjectResurces(String projectClass, String projectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getProjectActivities(String projectClass, String projectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void associateObjectsToProject(String projectClass, String projectId, String[] objectClass, String[] objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void associateObjectToProject(String projectClass, String projectId, String objectClass, String objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void freeObjectFromProject(String objectClass, String objectId, String projectClass, String projectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getProjectsAssociateToObject(String objectClass, String objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String createProjectPool(String name, String description, String instanceOfClass, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AssetLevelCorrelatedInformation getAffectedServices(int resourceType, String resourceDefinition, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteSynchronizationProvider> getSynchronizationProviders(String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createSynchronizationDataSourceConfig(String objectId, long syncGroupId, String name, List<StringPair> parameters, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createSynchronizationGroup(String name, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateSynchronizationGroup(long syncGroupId, List<StringPair> syncGroupProperties, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteSynchronizationGroup getSynchronizationGroup(long syncGroupId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteSynchronizationGroup> getSynchronizationGroups(String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteSynchronizationConfiguration getSyncDataSourceConfiguration(String objectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteSynchronizationConfiguration> getSyncDataSourceConfigurations(long syncGroupId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateSyncDataSourceConfiguration(long syncDataSourceConfigId, List<StringPair> parameters, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteSynchronizationGroup(long syncGroupId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteSynchronizationDataSourceConfig(long syncDataSourceConfigId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void copySyncDataSourceConfiguration(long syncGroupId, long[] syncDataSourceConfigurationIds, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void releaseSyncDataSourceConfigFromSyncGroup(long syncGroupId, long[] syncDataSourceConfigurationIds, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void moveSyncDataSourceConfiguration(long oldSyncGroupId, long newSyncGroupId, long[] syncDataSourceConfigurationIds, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createProcessDefinition(String name, String description, String version, boolean enabled, byte[] structure, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateProcessDefinition(long processDefinitionId, List<StringPair> properties, byte[] structure, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteProcessDefinition(long processDefinitionId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteProcessDefinition getProcessDefinition(long processDefinitionId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createProcessInstance(long processDefinitionId, String processInstanceName, String processInstanceDescription, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteActivityDefinition getNextActivityForProcessInstance(long processInstanceId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void commitActivity(long processInstanceId, long activityDefinitionId, RemoteArtifact artifact, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteArtifactDefinition getArtifactDefinitionForActivity(long processDefinitionId, long activityDefinitionId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteArtifact getArtifactForActivity(long processInstanceId, long activityId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemotePool> getWarehouseRootPools(String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void associatePhysicalNodeToWarehouse(String objectClass, String objectId, String warehouseClass, String warehouseId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void associatesPhysicalNodeToWarehouse(String[] objectClass, String[] objectId, String warehouseClass, String warehouseId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void releasePhysicalNodeFromWarehouse(String warehouseClass, String warehouseId, String targetId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void moveObjectsToWarehousePool(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void moveObjectsToWarehouse(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteLogicalConnectionDetails> getBGPMap(List<String> mappedBgpLinksIds, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createOSPView(String name, String description, byte[] content, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RemoteViewObject getOSPView(long viewId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteViewObjectLight> getOSPViews(String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateOSPView(long viewId, String name, String description, byte[] content, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteOSPView(long viewId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getIPAddress() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}

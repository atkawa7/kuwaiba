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

import com.neotropic.kuwaiba.commercial.SDHModule;
import com.neotropic.kuwaiba.commercial.sdh.SDHContainerLinkDefinition;
import com.neotropic.kuwaiba.commercial.sdh.SDHPosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.jws.WebService;
import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.BusinessRule;
import org.neotropic.kuwaiba.core.apis.persistence.application.BusinessRuleConstraint;
import org.neotropic.kuwaiba.core.apis.persistence.application.CompactQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.ConfigurationVariable;
import org.neotropic.kuwaiba.core.apis.persistence.application.ExtendedQuery;
import org.neotropic.kuwaiba.core.apis.persistence.application.FavoritesFolder;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfile;
import org.neotropic.kuwaiba.core.apis.persistence.application.GroupProfileLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.ResultMessage;
import org.neotropic.kuwaiba.core.apis.persistence.application.ResultRecord;
import org.neotropic.kuwaiba.core.apis.persistence.application.Session;
import org.neotropic.kuwaiba.core.apis.persistence.application.Task;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskNotificationDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskScheduleDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfile;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfileLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.Validator;
import org.neotropic.kuwaiba.core.apis.persistence.application.ValidatorDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ActivityDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.Artifact;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ArtifactDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.processman.ProcessDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.sync.AbstractSyncProvider;
import org.neotropic.kuwaiba.core.apis.persistence.application.sync.SyncDataSourceConfiguration;
import org.neotropic.kuwaiba.core.apis.persistence.application.sync.SynchronizationGroup;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLightList;
import org.neotropic.kuwaiba.core.apis.persistence.business.Contact;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
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
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteBusinessRuleConstraint;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteConfigurationVariable;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteFavoritesFolder;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteKpi;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteKpiAction;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemotePool;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteProcessDefinition;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteQuery;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteQueryLight;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteReportMetadata;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteReportMetadataLight;
import org.neotropic.kuwaiba.northbound.ws.model.application.RemoteResultMessage;
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
import org.neotropic.kuwaiba.northbound.ws.model.business.ServiceLevelCorrelatedInformation;
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
    //Modules
    @Autowired
    private SDHModule modSdh;
    
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
        } catch (Exception ex) { // Unexpected error. Log the stack trace and re-throw
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createSession"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void closeSession(String sessionId) throws ServerSideException {
        try {
            aem.closeSession(sessionId, "127.0.0.1");
        } catch (Exception ex) { // Unexpected error. Log the stack trace and re-throw
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
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getUsers"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteUserInfo> getUsersInGroup(long groupId, String sessionId) throws ServerSideException {
        if (aem == null)
             throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getUsersInGroup", "127.0.0.1", sessionId);
            List<UserProfile> users = aem.getUsersInGroup(groupId);

            List<RemoteUserInfo> usersInfo = new ArrayList<>();
            for (UserProfile userProfile : users)
                usersInfo.add(new RemoteUserInfo(userProfile));
            
            return usersInfo;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getUsersInGroup"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<GroupInfoLight> getGroupsForUser(long userId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getGroupsForUser", "127.0.0.1", sessionId);
            List<GroupProfileLight> groups = aem.getGroupsForUser(userId);

            List<GroupInfoLight> groupsInfo = new ArrayList<>();
            for (GroupProfileLight groupProfile : groups)
                groupsInfo.add(new GroupInfoLight(groupProfile));
            
            return groupsInfo;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getGroupsForUser"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<GroupInfo> getGroups(String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getGroups", "127.0.0.1", sessionId);
            List<GroupProfile> groups = aem.getGroups();

            List<GroupInfo> userGroupInfo = new ArrayList<>();
            for (GroupProfile group : groups)
                userGroupInfo.add(new GroupInfo(group));
                        
            return userGroupInfo;

        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getGroups"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createUser(String userName, String password, String firstName, String lastName, boolean enabled, int type, String email, List<PrivilegeInfo> privileges, long defaultGroupId, String sessionId) throws ServerSideException {
        
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createUser", "127.0.0.1", sessionId);
            List<Privilege> remotePrivileges = new ArrayList<>();
            
            if (privileges != null) {
                for (PrivilegeInfo privilege : privileges) {
                    Privilege remotePrivilege = new Privilege(privilege.getFeatureToken(), privilege.getAccessLevel());
                    if (!remotePrivileges.contains(remotePrivilege)) //Ignore duplicated privileges. This should not happen, but, you now...
                        remotePrivileges.add(remotePrivilege);
                }
            }
            
            long newUserId = aem.createUser(userName, password, firstName, lastName, enabled, type, email, remotePrivileges, defaultGroupId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, String.format("New User %s", userName));
            
            return newUserId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createUser"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setUserProperties(long oid, String username, String firstName, String lastName, String password, int enabled, int type, String email, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("setUserProperties", "127.0.0.1", sessionId);
            aem.setUserProperties(oid, username, password, firstName, lastName, enabled, type, email);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Set user %s properties", username));
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "setUserProperties"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void addUserToGroup(long userId, long groupId, String sessionId) throws ServerSideException {
        if (aem == null)
           throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("addUserToGroup", "127.0.0.1", sessionId);
            aem.addUserToGroup(userId, groupId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Added user to group", groupId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "addUserToGroup"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void removeUserFromGroup(long userId, long groupId, String sessionId) throws ServerSideException {
       if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("removeUserFromGroup", "127.0.0.1", sessionId);
            aem.removeUserFromGroup(userId, groupId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Removed user from group", groupId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "removeUserFromGroup"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setPrivilegeToUser(long userId, String featureToken, int accessLevel, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("addPrivilegeToUser", "127.0.0.1", sessionId);
            aem.setPrivilegeToUser(userId, featureToken, accessLevel);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Set privilege %s to user %s", featureToken, userId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "removeUserFromGroup"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setPrivilegeToGroup(long groupId, String featureToken, int accessLevel, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("addPrivilegeToGroup", "127.0.0.1", sessionId);
            aem.setPrivilegeToGroup(groupId, featureToken, accessLevel);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Set privilege %s to group %s", featureToken, groupId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "setPrivilegeToGroup"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void removePrivilegeFromUser(long userId, String featureToken, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("removePrivilegeFromUser", "127.0.0.1", sessionId);
            aem.removePrivilegeFromUser(userId, featureToken);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Removed privilege %s to user %s", featureToken, userId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "removePrivilegeFromUser"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void removePrivilegeFromGroup(long groupId, String featureToken, String sessionId) throws ServerSideException {
       if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("removePrivilegeFromGroup", "127.0.0.1", sessionId);            
            aem.removePrivilegeFromGroup(groupId, featureToken);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Removed privilege %s to group %s", featureToken, groupId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "removePrivilegeFromGroup"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public long createGroup(String groupName, String description, List<Long> users, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createGroup", "127.0.0.1", sessionId);
            
            long groupId = aem.createGroup(groupName, description, users);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, String.format("New group %s", groupName));
            return groupId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createGroup"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void setGroupProperties(long oid, String groupName, String description, String sessionId) throws ServerSideException {
       if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("setGroupProperties", "127.0.0.1", sessionId);
            aem.setGroupProperties(oid, groupName, description);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Set group %s properties", groupName));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "setGroupProperties"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void deleteUsers(List<Long> oids, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteUsers", "127.0.0.1", sessionId);
            aem.deleteUsers(oids);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, String.format("%s users deleted", oids.size()));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteUsers"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void deleteGroups(List<Long> oids, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteGroups", "127.0.0.1", sessionId);
            aem.deleteGroups(oids);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("%s groups deleted", oids.size()));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteGroups"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public RemoteViewObject getObjectRelatedView(String oid, String objectClass, long viewId, String sessionId) throws ServerSideException {
       if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getObjectRelatedView", "127.0.0.1", sessionId);
            ViewObject myView =  aem.getObjectRelatedView(oid, objectClass, viewId);
            if (myView == null)
                return null;
            RemoteViewObject res = new RemoteViewObject(myView);
            res.setBackground(myView.getBackground());
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getObjectRelatedView"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteViewObjectLight> getObjectRelatedViews(String oid, String objectClass, int viewType, int limit, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getObjectRelatedViews", "127.0.0.1", sessionId);
            List<ViewObjectLight> views = aem.getObjectRelatedViews(oid, objectClass, limit);
            List<RemoteViewObjectLight> res = new ArrayList<>();
            
            for (ViewObjectLight view : views)
                res.add(new RemoteViewObjectLight(view));

            return res;
        } catch(InventoryException e) {
            throw new ServerSideException(e.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getObjectRelatedViews"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public RemoteViewObjectLight[] getGeneralViews(String viewClass, int limit, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getGeneralViews", "127.0.0.1", sessionId);
            List<ViewObjectLight> views = aem.getGeneralViews(viewClass, limit);
            RemoteViewObjectLight[] res = new RemoteViewObjectLight[views.size()];
            for (int i = 0; i < views.size(); i++)
                res[i] = new RemoteViewObjectLight(views.get(i));
           return res;
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getGeneralViews"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public RemoteViewObject getGeneralView(long viewId, String sessionId) throws ServerSideException {
       if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getGeneralView", "127.0.0.1", sessionId);
            ViewObject viewObject = aem.getGeneralView(viewId);
            if(viewObject == null) {
                return null;
            }
            RemoteViewObject viewInfo = new RemoteViewObject(viewObject);
            viewInfo.setBackground(viewObject.getBackground());
            return viewInfo;
        } catch(InventoryException e) {
            throw new ServerSideException(e.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getGeneralView"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public long createListTypeItemRelatedView(String listTypeItemId, String listTypeItemClassName, String viewClassName, String name, String description, byte[] structure, byte[] background, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createListTypeItemRelatedView", "127.0.0.1", sessionId);            
            
            long viewId = aem.createListTypeItemRelatedView(listTypeItemId, listTypeItemClassName, viewClassName, name, description, structure, background);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created %s [%s] list type item related view %s [%s] with id %s", listTypeItemId, listTypeItemClassName, name, viewClassName, viewId));
            return viewId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createListTypeItemRelatedView"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void updateListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId, String name, String description, byte[] structure, byte[] background, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateListTypeItemRelatedView", "127.0.0.1", sessionId);
            
            ChangeDescriptor theChange = aem.updateListTypeItemRelatedView(listTypeItemId, listTypeItemClass, viewId, name, description, structure, background);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_UPDATE_VIEW, theChange);
        } catch(InventoryException ie){
            throw new ServerSideException(ie.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateListTypeItemRelatedView"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public RemoteViewObject getListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getListTypeItemRelatedView", "127.0.0.1", sessionId);
            ViewObject myView = aem.getListTypeItemRelatedView(listTypeItemId, listTypeItemClass, viewId);
            if (myView == null)
                return null;
            RemoteViewObject res = new RemoteViewObject(myView);
            res.setBackground(myView.getBackground());
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getListTypeItemRelatedView"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public RemoteViewObjectLight[] getListTypeItemRelatedViews(String listTypeItemId, String listTypeItemClass, int limit, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getListTypeItemRelatedViews", "127.0.0.1", sessionId);
            List<ViewObjectLight> views = aem.getListTypeItemRelatedViews(listTypeItemId, listTypeItemClass, limit);
            RemoteViewObjectLight[] res = new RemoteViewObjectLight[views.size()];
            int i = 0;
            for (ViewObjectLight view : views){
                res[i] = new RemoteViewObjectLight(view);
                i++;
            }
            return res;
        } catch(InventoryException e) {
            throw new ServerSideException(e.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getListTypeItemRelatedViews"), ex);
            throw new ServerSideException(ex.getMessage());
        }    
    }

    @Override
    public void deleteListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteListTypeItemRelatedView", "127.0.0.1", sessionId);
            aem.deleteListTypeItemRelatedView(listTypeItemId, listTypeItemClass, viewId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());            
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteListTypeItemRelatedView"), ex);
        }
    }

    @Override
    public List<RemoteObjectLight> getDeviceLayouts(String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("getDeviceLayouts", "127.0.0.1", sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(aem.getDeviceLayouts());
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getDeviceLayouts"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public byte[] getDeviceLayoutStructure(String oid, String className, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("getDeviceLayoutStructure", "127.0.0.1", sessionId);
            return aem.getDeviceLayoutStructure(oid, className);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getDeviceLayoutStructure"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createObjectRelatedView(String objectId, String objectClass, String name, String description, String viewClassName, byte[] structure, byte[] background, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createObjectRelatedView", "127.0.0.1", sessionId);            
            
            long viewId = aem.createObjectRelatedView(objectId, objectClass, name, description, viewClassName, structure, background);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created %s [%s] object related view %s [%s] with id %s", objectId, objectClass, name, viewClassName, viewId));
            return viewId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createObjectRelatedView"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createGeneralView(String viewClass, String name, String description, byte[] structure, byte[] background, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createGeneralView", "127.0.0.1", sessionId);
                        
            long viewId = aem.createGeneralView(viewClass, name, description, structure, background);
                        
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created general view %s [%s] with id %s", name, viewClass, viewId));
            
            return viewId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createGeneralView"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateObjectRelatedView(String objectOid, String objectClass, long viewId, String viewName, String viewDescription, byte[] structure, byte[] background, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateObjectRelatedView", "127.0.0.1", sessionId);
            
            ChangeDescriptor theChange = aem.updateObjectRelatedView(objectOid, objectClass, viewId, viewName, viewDescription, structure, background);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_UPDATE_VIEW, theChange);
        } catch(InventoryException ie){
            throw new ServerSideException(ie.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateObjectRelatedView"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateGeneralView(long viewId, String viewName, String viewDescription, byte[] structure, byte[] background, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateGeneralView", "127.0.0.1", sessionId);
            
            ChangeDescriptor theChange = aem.updateGeneralView(viewId, viewName, viewDescription, structure, background);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_UPDATE_VIEW, theChange);
        } catch(InventoryException ie) {
            throw new ServerSideException(ie.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateGeneralView"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteGeneralView(List<Long> oids, String sessionId) throws ServerSideException {
         if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteGeneralView", "127.0.0.1", sessionId);
            aem.deleteGeneralViews(oids);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("%s general views deleted", oids.size()));            
        } catch(InventoryException ie) {
            throw new ServerSideException(ie.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteGeneralView"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }   

    @Override
    public String createListTypeItem(String className, String name, String displayName, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createListTypeItem", "127.0.0.1", sessionId);
            
            String lstTypeItemId = aem.createListTypeItem(className, name, displayName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created list Type Item %s (%s)", name, className));
            
            return lstTypeItemId;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createListTypeItem"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteListTypeItem(String className, String oid, boolean releaseRelationships, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deletelistTypeItem", "127.0.0.1", sessionId);
            aem.deleteListTypeItem(className, oid, releaseRelationships);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("Deleted list Type Item with id %s", oid));

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteListTypeItem"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getListTypeItems(String className, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getListTypeItems", "127.0.0.1", sessionId);
            List<BusinessObjectLight> listTypeItems = aem.getListTypeItems(className);
            List<RemoteObjectLight> res = new ArrayList<>();
            for (BusinessObjectLight listTypeItem : listTypeItems)
                res.add(new RemoteObjectLight(listTypeItem));
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getListTypeItems"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteClassMetadataLight[] getInstanceableListTypes(String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));

        try {
            aem.validateCall("getInstanceableListTypes", "127.0.0.1", sessionId);
            List<ClassMetadataLight> instanceableListTypes = aem.getInstanceableListTypes();
            RemoteClassMetadataLight[] res = new RemoteClassMetadataLight[instanceableListTypes.size()];
            for (int i = 0; i < instanceableListTypes.size(); i++)
                res[i] = new RemoteClassMetadataLight(instanceableListTypes.get(i));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getInstanceableListTypes"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteResultRecord[] executeQuery(TransientQuery query, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("executeQuery", "127.0.0.1", sessionId);
            List<ResultRecord> resultRecordList = aem.executeQuery(transientQuerytoExtendedQuery(query));

            RemoteResultRecord[] resultArray = new RemoteResultRecord[resultRecordList.size()];
            
            for (int i=0;resultRecordList.size() >i; i++) {
                RemoteObjectLight rol = new RemoteObjectLight(resultRecordList.get(i).getClassName(), resultRecordList.get(i).getId(), resultRecordList.get(i).getName());
                resultArray[i] = new RemoteResultRecord(rol, (ArrayList<String>) resultRecordList.get(i).getExtraColumns());
            }

            return resultArray;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "executeQuery"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createQuery(String queryName, long ownerOid, byte[] queryStructure, String description, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createQuery", "127.0.0.1", sessionId);
            
            long queryId = aem.createQuery(queryName, ownerOid, queryStructure, description);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Create query %s with id %s", queryName, queryId));             
            return queryId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createQuery"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void saveQuery(long queryOid, String queryName, long ownerOid, byte[] queryStructure, String description, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("saveQuery", "127.0.0.1", sessionId);
            
            ChangeDescriptor changeDescriptor = aem.saveQuery(queryOid, queryName, ownerOid, queryStructure, description);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "saveQuery"), ex);
          throw new ServerSideException(ex.getMessage());
        }

    }

    @Override
    public void deleteQuery(long queryOid, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteQuery", "127.0.0.1", sessionId);
            aem.deleteQuery(queryOid);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("Deleted query with id %s", queryOid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteQuery"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteQueryLight[] getQueries(boolean showPublic, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getQueries", "127.0.0.1", sessionId);
            List<CompactQuery> queries = aem.getQueries(showPublic);
            RemoteQueryLight[] rql =  new RemoteQueryLight[queries.size()];
            Integer i = 0;
            for (CompactQuery compactQuery : queries) {
                rql[i] = new RemoteQueryLight(compactQuery.getId(),
                        compactQuery.getName(),
                        compactQuery.getDescription(),
                        compactQuery.getIsPublic());
                i++;
            }
            return rql;
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getQueries"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteQuery getQuery(long queryOid, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getQuery", "127.0.0.1", sessionId);
            return new RemoteQuery(aem.getQuery(queryOid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getQuery"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public byte[] getClassHierarchy(boolean showAll, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getClassHierarchy", "127.0.0.1", sessionId);
            return aem.getClassHierachy(showAll);
        } catch (InventoryException ex){
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getClassHierarchy"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createRootPool(String name, String description, String instancesOfClass, int type, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createRootPool", "127.0.0.1", sessionId);
            
            String poolId = aem.createRootPool(name, description, instancesOfClass, type);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created %s Root Pool", name));
            return poolId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createRootPool"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createPoolInObject(String parentClassname, String parentId, String name, String description, String instancesOfClass, int type, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createPoolInObject", "127.0.0.1", sessionId);
            String poolId = aem.createPoolInObject(parentClassname, parentId, name, description, instancesOfClass, type);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created %s pool in %s object with id %s", name, parentClassname, parentId));            
            return poolId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createPoolInObject"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createPoolInPool(String parentId, String name, String description, String instancesOfClass, int type, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createPoolInPool", "127.0.0.1", sessionId);
            String poolId = aem.createPoolInPool(parentId, name, description, instancesOfClass, type);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created %s Pool In Pool with id %s ", name, parentId));  
            return poolId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createPoolInPool"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createPoolItem(String poolId, String className, String[] attributeNames, String[] attributeValues, String templateId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createPoolItem", "", sessionId);
            Map<String, String> attributes = IntStream.range(0, attributeNames.length).boxed()
             .collect(Collectors.toMap(i -> attributeNames[i], i -> attributeValues[i]));
            
            String objectId = bem.createPoolItem(poolId, className, new HashMap(attributes), templateId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created pool item with id", objectId));
            
            return objectId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createPoolItem"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deletePools(String[] ids, String sessionId) throws ServerSideException {
       if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deletePools", "127.0.0.1", sessionId);
            aem.deletePools(ids);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("%s pools deleted", ids.length));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deletePools"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemotePool> getRootPools(String className, int type, boolean includeSubclasses, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getRootPools", "127.0.0.1", sessionId);
            List<RemotePool> res = new ArrayList<>();
            List<Pool> rootPools = bem.getRootPools(className, type, includeSubclasses);
            
            for (Pool aPool : rootPools)
                res.add(new RemotePool(aPool));
            
            return res;
        }  catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getRootPools"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemotePool> getPoolsInObject(String objectClassName, String objectId, String poolClass, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getPoolsInObject", "127.0.0.1", sessionId);
            List<RemotePool> res = new ArrayList<>();
            List<Pool> rootPools = bem.getPoolsInObject(objectClassName, objectId, poolClass);
            
            for (Pool aPool : rootPools)
                res.add(new RemotePool(aPool));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getPoolsInObject"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemotePool> getPoolsInPool(String parentPoolId, String poolClass, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getPoolsInPool", "127.0.0.1", sessionId);
            List<RemotePool> res = new ArrayList<>();
            List<Pool> rootPools = bem.getPoolsInPool(parentPoolId, poolClass);
            
            for (Pool aPool : rootPools)
                res.add(new RemotePool(aPool));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getPoolsInObject"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemotePool getPool(String poolId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getPool", "127.0.0.1", sessionId);
            return new RemotePool(bem.getPool(poolId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getPool"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setPoolProperties(String poolId, String name, String description, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("setPoolProperties", "127.0.0.1", sessionId);
            ChangeDescriptor changeDescriptor = aem.setPoolProperties(poolId, name, description);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "setPoolProperties"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getPoolItems(String poolId, int limit, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getPoolItems", "127.0.0.1", sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getPoolItems(poolId, limit));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getPoolItems"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createTask(String name, String description, boolean enabled, boolean commitOnExecute, String script, List<StringPair> parameters, RemoteTaskScheduleDescriptor schedule, RemoteTaskNotificationDescriptor notificationType, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createTask", "127.0.0.1", sessionId);
            long res = aem.createTask(name, description, enabled, commitOnExecute, script, parameters, 
                   new TaskScheduleDescriptor(schedule.getStartTime(), schedule.getEveryXMinutes(), schedule.getExecutionType()),
                   new TaskNotificationDescriptor(notificationType.getEmail(), notificationType.getNotificationType()));
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created task %s with id %s", name, res));
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }  catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createTask"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void updateTaskProperties(long taskId, String propertyName, String propertyValue, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateTaskProperties", "127.0.0.1", sessionId);
            ChangeDescriptor changeDescriptor = aem.updateTaskProperties(taskId, propertyName, propertyValue);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateTaskProperties"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateTaskParameters(long taskId, List<StringPair> parameters, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateTaskParameters", "127.0.0.1", sessionId);
            ChangeDescriptor changeDescriptor = aem.updateTaskParameters(taskId, parameters);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);  
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateTaskParameters"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void updateTaskSchedule(long taskId, RemoteTaskScheduleDescriptor schedule, String sessionId) throws ServerSideException {
       if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateTaskSchedule", "127.0.0.1", sessionId);
            ChangeDescriptor changeDescriptor = aem.updateTaskSchedule(taskId, 
                    new TaskScheduleDescriptor(schedule.getStartTime(), schedule.getEveryXMinutes(), schedule.getExecutionType()));
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor); 
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateTaskSchedule"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void updateTaskNotificationType(long taskId, RemoteTaskNotificationDescriptor notificationType, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateTaskNotificationType", "127.0.0.1", sessionId);
            ChangeDescriptor changeDescriptor = aem.updateTaskNotificationType(taskId,
                    new TaskNotificationDescriptor(notificationType.getEmail(), notificationType.getNotificationType()));
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor); 
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateTaskNotificationType"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public RemoteTask getTask(long taskId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getTask", "127.0.0.1", sessionId);
            Task theTask = aem.getTask(taskId);
            List<RemoteUserInfoLight> users = new ArrayList<>();
            
            for (UserProfileLight aUser : theTask.getUsers())
                users.add(new RemoteUserInfoLight(aUser));
            
            return new RemoteTask(theTask.getId(), theTask.getName(), theTask.getDescription(), theTask.isEnabled(), 
                                    theTask.commitOnExecute(), theTask.getScript(), theTask.getParameters(), new RemoteTaskScheduleDescriptor(theTask.getSchedule()), 
                                    new RemoteTaskNotificationDescriptor(theTask.getNotificationType()), users);
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getTask"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }  

    @Override
    public List<RemoteTask> getTasks(String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getTasks", "127.0.0.1", sessionId);
            List<Task> tasks = aem.getTasks();
            
            List<RemoteTask> remoteTasks = new ArrayList<>();
            
            for (Task task : tasks) {
                List<RemoteUserInfoLight> users = new ArrayList<>();
                for (UserProfileLight aUser : task.getUsers())
                    users.add(new RemoteUserInfoLight(aUser));
                remoteTasks.add(new RemoteTask(task.getId(), task.getName(), task.getDescription(), task.isEnabled(), task.commitOnExecute(), task.getScript(),
                                    task.getParameters(), new RemoteTaskScheduleDescriptor(task.getSchedule()), new RemoteTaskNotificationDescriptor(task.getNotificationType()), users));
            }
            
            return remoteTasks;
            
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getTask"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteTask> getTasksForUser(long userId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getTasks", "127.0.0.1", sessionId);
            List<Task> tasks = aem.getTasksForUser(userId);
            
            List<RemoteTask> remoteTasks = new ArrayList<>();
            
            for (Task task : tasks) {
                List<RemoteUserInfoLight> users = new ArrayList<>();
                for (UserProfileLight aUser : task.getUsers())
                    users.add(new RemoteUserInfoLight(aUser));
                remoteTasks.add(new RemoteTask(task.getId(), task.getName(), task.getDescription(), task.isEnabled(), task.commitOnExecute(), task.getScript(),
                                    task.getParameters(), new RemoteTaskScheduleDescriptor(task.getSchedule()), new RemoteTaskNotificationDescriptor(task.getNotificationType()), users));
            }
            return remoteTasks;
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getTasksForUser"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteUserInfoLight> getSubscribersForTask(long taskId, String sessionId) throws ServerSideException {
       if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getSubscribersForTask", "127.0.0.1", sessionId);
            List<UserProfileLight> upls = aem.getSubscribersForTask(taskId);
            
            return upls.stream().map(userProfileLight -> new RemoteUserInfoLight(userProfileLight)).collect(Collectors.toList());
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSubscribersForTask"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void deleteTask(long taskId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteTask", "127.0.0.1", sessionId);
            aem.deleteTask(taskId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                    String.format("Deleted task with id %s", taskId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteTask"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void subscribeUserToTask(long userId, long taskId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("subscribeUserToTask", "127.0.0.1", sessionId);
            ChangeDescriptor changeDescriptor = aem.subscribeUserToTask(userId, taskId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "subscribeUserToTask"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void unsubscribeUserFromTask(long userId, long taskId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("unsubscribeUserFromTask", "127.0.0.1", sessionId);
            ChangeDescriptor changeDescriptor = aem.unsubscribeUserFromTask(userId, taskId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "unsubscribeUserFromTask"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public RemoteTaskResult executeTask(long taskId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("executeTask", "127.0.0.1", sessionId);
            TaskResult theTaskResult = aem.executeTask(taskId);
            RemoteTaskResult remoteTaskResult = new RemoteTaskResult();
            
            for(ResultMessage resultMessage : theTaskResult.getMessages())
                remoteTaskResult.getMessages().add(new RemoteResultMessage(resultMessage.getMessageType(), resultMessage.getMessage()));
            
            return remoteTaskResult;
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "executeTask"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public String createContact(String contactClass, List<StringPair> properties, String customerClassName, String customerId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createContact", "127.0.0.1", sessionId);
            return bem.createContact(contactClass, properties, customerClassName, customerId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createContact"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void updateContact(String contactClass, String contactId, List<StringPair> properties, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateContact", "127.0.0.1", sessionId);
            bem.updateContact(contactClass, contactId, properties);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateContact"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void deleteContact(String contactClass, String contactId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteContact", "127.0.0.1", sessionId);
            bem.deleteContact(contactClass, contactId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteContact"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public RemoteContact getContact(String contactClass, String contactId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getContact", "127.0.0.1", sessionId);
            return new RemoteContact(bem.getContact(contactClass, contactId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getContact"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteContact> searchForContacts(String searchString, int maxResults, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            List<RemoteContact> res = new ArrayList();
            aem.validateCall("searchForContacts", "127.0.0.1", sessionId);
            for (Contact contact : bem.searchForContacts(searchString, maxResults)) 
                res.add(new RemoteContact(contact));

            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "searchForContacts"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteContact> getContactsForCustomer(String customerClass, String customerId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            List<RemoteContact> res = new ArrayList();
            aem.validateCall("getContactsForCustomer", "127.0.0.1", sessionId);
            for (Contact contact : bem.getContactsForCustomer(customerClass, customerId)) 
                res.add(new RemoteContact(contact));

            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getContactsForCustomer"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public long createConfigurationVariable(String configVariablesPoolId, String name, String description, int type, boolean masked, String valueDefinition, String sessionId) throws ServerSideException {
         if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("createConfigurationVariable", "127.0.0.1", sessionId);
            long res = aem.createConfigurationVariable(configVariablesPoolId, name, description, type, masked, valueDefinition);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, String.format("Configuration variable %s was created", name));
            return res;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createConfigurationVariable"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void updateConfigurationVariable(String name, String propertyToUpdate, String newValue, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("updateConfigurationVariable", "127.0.0.1", sessionId);
            aem.updateConfigurationVariable(name, propertyToUpdate, newValue);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, String.format("Configuration variable %s was was updated: %s -> %s", name, propertyToUpdate, newValue));
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateConfigurationVariable"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void deleteConfigurationVariable(String name, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("deleteConfigurationVariable", "127.0.0.1", sessionId);
            aem.deleteConfigurationVariable(name);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, String.format("Configuration variable %s was was deleted", name));
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteConfigurationVariable"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public RemoteConfigurationVariable getConfigurationVariable(String name, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));

        try {
            aem.validateCall("getConfigurationVariable", "127.0.0.1", sessionId);
            ConfigurationVariable configVariable = aem.getConfigurationVariable(name);
            return new RemoteConfigurationVariable(configVariable.getId(), configVariable.getName(), 
                    configVariable.getDescription(), configVariable.getValueDefinition(), configVariable.isMasked(), configVariable.getType());
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getConfigurationVariable"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteConfigurationVariable> getConfigurationVariablesInPool(String poolId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
            
        try {
            aem.validateCall("getConfigurationVariablesInPool", "127.0.0.1", sessionId);
            List<RemoteConfigurationVariable> res = new ArrayList<>();
            aem.getConfigurationVariablesInPool(poolId).forEach((configVariable) -> {
                res.add(new RemoteConfigurationVariable(configVariable.getId(), configVariable.getName(), 
                    configVariable.getDescription(), configVariable.getValueDefinition(), configVariable.isMasked(), configVariable.getType()));
            });
            
            return res;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getConfigurationVariablesInPool"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemotePool> getConfigurationVariablesPools(String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("getConfigurationVariablesPools", "127.0.0.1", sessionId);
            List<RemotePool> res = new ArrayList<>();
            aem.getConfigurationVariablesPools().forEach((aConfigVariablesPool) -> {
                res.add(new RemotePool(aConfigVariablesPool.getId(), aConfigVariablesPool.getName(), aConfigVariablesPool.getDescription(), 
                        aConfigVariablesPool.getClassName(), aConfigVariablesPool.getType()));
            });
            
            return res;
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getConfigurationVariablesPools"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public String createConfigurationVariablesPool(String name, String description, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("createConfigurationVariablesPool", "127.0.0.1", sessionId);
            String res = aem.createConfigurationVariablesPool(name, description);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, String.format("Configuration variables pool %s was created", name));
            return res;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createConfigurationVariablesPool"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void updateConfigurationVariablesPool(String poolId, String propertyToUpdate, String value, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("updateConfigurationVariablesPool", "127.0.0.1", sessionId);
            aem.updateConfigurationVariablesPool(poolId, propertyToUpdate, value);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, String.format("Configuration variables pool with id %s was was updated: %s -> %s", poolId, propertyToUpdate, value));
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateConfigurationVariablesPool"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void deleteConfigurationVariablesPool(String poolId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("deleteConfigurationVariablesPool", "127.0.0.1", sessionId);
            aem.deleteConfigurationVariablesPool(poolId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, String.format("Configuration variables pool with id %s was was deleted", poolId));
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteConfigurationVariablesPool"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public long createValidatorDefinition(String name, String description, String classToBeApplied, String script, boolean enabled, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("createValidatorDefinition", "127.0.0.1", sessionId);
            long res = aem.createValidatorDefinition(name, description, classToBeApplied, script, enabled);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, String.format("Validator definition %s ", name));
            return res;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createValidatorDefinition"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void updateValidatorDefinition(long validatorDefinitionId, String name, String description, String classToBeApplied, String script, Boolean enabled, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("updateValidatorDefinition", "127.0.0.1", sessionId);
            aem.updateValidatorDefinition(validatorDefinitionId, name, description, classToBeApplied, script, enabled);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, String.format("Validator definition with id %s ", validatorDefinitionId));
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateValidatorDefinition"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteValidatorDefinition> getValidatorDefinitionsForClass(String className, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            List<RemoteValidatorDefinition> res = new ArrayList<>();
            aem.validateCall("getValidatorDefinitionsForClass", "127.0.0.1", sessionId);
            
            List<ValidatorDefinition> validatorDefinitions = aem.getValidatorDefinitionsForClass(className);
            
            for (ValidatorDefinition validatorDefinition : validatorDefinitions) {
                res.add(new RemoteValidatorDefinition(validatorDefinition.getId(), validatorDefinition.getName(), validatorDefinition.getDescription(), 
                        validatorDefinition.getClassToBeApplied(), validatorDefinition.getScript(), validatorDefinition.isEnabled()));
            }
            
            return res;
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getValidatorDefinitionsForClass"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteValidator> runValidationsForObject(String objectClass, long objectId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            List<RemoteValidator> res = new ArrayList<>();
            aem.validateCall("runValidationsForObject", "127.0.0.1", sessionId);
            
            List<Validator> validators = aem.runValidationsForObject(objectClass, objectId);
            
            for (Validator validator : validators) {
                res.add(new RemoteValidator(validator.getName(), validator.getProperties()));
            }
            
            return res;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "runValidationsForObject"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public void deleteValidatorDefinition(long validatorDefinitionId, String sessionId) throws ServerSideException {
       if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("deleteValidatorDefinition", "127.0.0.1", sessionId);
            aem.deleteValidatorDefinition(validatorDefinitionId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, String.format("Validator definition with id %s ", validatorDefinitionId));
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteValidatorDefinition"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteObjectLight> getObjectChildrenForClassWithId(String oid, long objectClassId, int maxResults, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getObjectChildrenForClassWithId", "127.0.0.1", sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectChildren(objectClassId, oid, maxResults));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getObjectChildrenForClassWithId"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteObjectLight> getObjectChildren(String objectClassName, String oid, int maxResults, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getObjectChildren", "127.0.0.1", sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectChildren(objectClassName, oid, maxResults));
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getObjectChildren"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteObjectLight> getSiblings(String objectClassName, String oid, int maxResults, String sessionId) throws ServerSideException {
         if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getSiblings", "127.0.0.1", sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSiblings(objectClassName, oid, maxResults));
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getObjectChildren"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteObject> getChildrenOfClass(String parentOid, String parentClass, String childrenClass, int maxResults, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getChildrenOfClass", "127.0.0.1", sessionId);
            return RemoteObject.toRemoteObjectArray(bem.getChildrenOfClass(parentOid, parentClass, childrenClass, maxResults));
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getChildrenOfClass"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }


    @Override
    public List<RemoteObjectLight> getChildrenOfClassLightRecursive(String parentOid, String parentClass, String childrenClass, int maxResults, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getChildrenOfClassLightRecursive", "127.0.0.1", sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getChildrenOfClassLightRecursive(parentOid, parentClass, childrenClass, maxResults));
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getChildrenOfClassLightRecursive"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteObjectLight> getChildrenOfClassLight(String parentOid, String parentClass, String childrenClass, int maxResults, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getChildrenOfClassLight", "127.0.0.1", sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getChildrenOfClassLight(parentOid, parentClass, childrenClass, maxResults));
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getChildrenOfClassLight"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteObjectLight> getSpecialChildrenOfClassLight(String parentOid, String parentClass, String classToFilter, int maxResults, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getSpecialChildrenOfClassLight", "127.0.0.1", sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSpecialChildrenOfClassLight(parentOid, parentClass,classToFilter, maxResults));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSpecialChildrenOfClassLight"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public RemoteObject getObject(String objectClass, String oid, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getObject", "127.0.0.1", sessionId);
            return new RemoteObject(bem.getObject(objectClass, oid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getObject"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public RemoteObjectLight getObjectLight(String objectClass, String oid, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getObjectLight", "127.0.0.1", sessionId);
            return new RemoteObjectLight(bem.getObjectLight(objectClass, oid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getObjectLight"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteObjectLight> getObjectsWithFilterLight(String className, String filterName, String filterValue, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getObjectsWithFilterLight", "127.0.0.1", sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectsWithFilterLight(className, filterName, filterValue));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getObjectsWithFilterLight"), ex);
          throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteObject> getObjectsWithFilter(String className, String filterName, String filterValue, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getObjectsWithFilter", "127.0.0.1", sessionId);
            return RemoteObject.toRemoteObjectArray(bem.getObjectsWithFilter(className, filterName, filterValue));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getObjectsWithFilter"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getObjectsOfClassLight(String className, int maxResults, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getObjectsOfClassLight", "127.0.0.1", sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectsOfClassLight(className, maxResults));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());       
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getObjectsOfClassLight"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight getCommonParent(String aObjectClass, String aOid, String bObjectClass, String bOid, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getCommonParent", "127.0.0.1", sessionId);
            BusinessObjectLight commonParent = bem.getCommonParent(aObjectClass, aOid, bObjectClass, bOid);
            if (commonParent != null && commonParent.getId() != null && !commonParent.getId().equals("-1")) // is not DummyRoot
                return new RemoteObjectLight(commonParent.getClassName(), commonParent.getId(), commonParent.getName());
            else
                return new RemoteObjectLight("", "-1" , "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getCommonParent"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight getParent(String objectClass, String oid, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getParent", "127.0.0.1", sessionId);
            BusinessObjectLight parent = bem.getParent(objectClass, oid);
            return new RemoteObjectLight(parent.getClassName(), parent.getId(), parent.getName());
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getParent"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getParents(String objectClass, String oid, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getParents", "127.0.0.1", sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getParents(objectClass, oid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getParents"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObjectSpecialRelationships getSpecialAttributes(String objectClass, String oid, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getSpecialAttributes", "127.0.0.1", sessionId);
            HashMap<String, List<BusinessObjectLight>> relationships = bem.getSpecialAttributes(objectClass, oid);
            RemoteObjectSpecialRelationships res = new RemoteObjectSpecialRelationships(relationships);

            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSpecialAttributes"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getParentsUntilFirstOfClass(String objectClass, String oid, String objectToMatchClassName, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getParentsUntilFirstOfClass", "127.0.0.1", sessionId);
            List<RemoteObjectLight> remoteObjects = new ArrayList<>();
            for (BusinessObjectLight remoteObject : bem.getParentsUntilFirstOfClass(objectClass, oid, objectToMatchClassName))
                remoteObjects.add(new RemoteObjectLight(remoteObject));
            return remoteObjects;
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getParentsUntilFirstOfClass"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight getFirstParentOfClass(String objectClass, String oid, String objectToMatchClassName, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getLastParentUntilFirstOfClass", "127.0.0.1", sessionId);
            BusinessObjectLight firstParentOfClass = bem.getFirstParentOfClass(objectClass, oid, objectToMatchClassName);
            return firstParentOfClass != null ? new RemoteObjectLight(firstParentOfClass) : null;
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getFirstParentOfClass"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObject getParentOfClass(String objectClass, String oid, String parentClass, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getParentOfClass", "127.0.0.1", sessionId);
            return new RemoteObject(bem.getFirstParentOfClass(objectClass, oid, parentClass));
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getParentOfClass"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getSpecialAttribute(String objectClass, String oid, String attributeName, String sessionId) throws ServerSideException {
       if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getSpecialAttribute", "127.0.0.1", sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSpecialAttribute(objectClass, oid, attributeName));
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSpecialAttribute"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getObjectSpecialChildren(String objectClass, String objectId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getObjectSpecialChildren", "127.0.0.1", sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectSpecialChildren(objectClass, objectId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getObjectSpecialChildren"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateObject(String className, String oid, List<StringPair> attributesToBeUpdated, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));

        try {
            aem.validateCall("updateObject", "127.0.0.1", sessionId);
            HashMap<String, String> attributes = new HashMap<>();
            
            for (StringPair attributeToBeUpdated : attributesToBeUpdated)
                attributes.put(attributeToBeUpdated.getKey(), attributeToBeUpdated.getValue());

            ChangeDescriptor theChange = bem.updateObject(className, oid, attributes);
            
            if (mem.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, className))
                aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, theChange);
            else
                aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), className,
                        oid, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT, theChange);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateObject"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createObject(String className, String parentObjectClassName, String parentOid, String[] attributeNames, String[] attributeValues, String templateId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException("Attribute names and attribute values arrays sizes doesn't match");

        try {
            aem.validateCall("createObject", "127.0.0.1", sessionId);
            HashMap<String, String> attributes = new HashMap<>();
            
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], attributeValues[i]);
            
            String newObjectId = bem.createObject(className, parentObjectClassName, parentOid, attributes, templateId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId),
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.valueOf(newObjectId));
            return newObjectId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createObject"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createSpecialObject(String className, String parentObjectClassName, String parentOid, String[] attributeNames, String[] attributeValues, String templateId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException("Attribute names and attribute values arrays sizes doesn't match");

        try {
            aem.validateCall("createSpecialObject", "127.0.0.1", sessionId);
            HashMap<String, String> attributes = new HashMap<>();
            
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], attributeValues[i]);

            String newSpecialObjectId = bem.createSpecialObject(className, parentObjectClassName, parentOid, attributes, templateId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId),
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.valueOf(newSpecialObjectId));
            
            return newSpecialObjectId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createSpecialObject"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteObject(String className, String oid, boolean releaseRelationships, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            bem.deleteObject(className, oid, releaseRelationships);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                    String.format("Object with id %s of class %s deleted", className, oid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteObject"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteObjects(String[] classNames, String[] oids, boolean releaseRelationships, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        if (classNames.length != oids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateCall("deleteObjects", "127.0.0.1", sessionId);
            HashMap<String, List<String>> objects = new HashMap<>();
            for (int i = 0; i< classNames.length;i++) {
                List<String> existingObjects = objects.get(classNames[i]);
                if (existingObjects == null){
                    List<String> newIdList = new ArrayList<>();
                    newIdList.add(oids[i]);
                    objects.put(classNames[i], newIdList);
                }
                else
                    existingObjects.add(oids[i]);
            }

            bem.deleteObjects(objects, releaseRelationships);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                    String.format("%s objects deleted", oids.length ));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteObjects"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void moveObjectsToPool(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateCall("moveObjectsToPool", "127.0.0.1", sessionId);
            HashMap<String, List<String>> temObjects = new HashMap<>();
            for (int i = 0; i< objectClasses.length; i++){
                List<String> ids = temObjects.get(objectClasses[i]);
                if (ids == null)
                    ids = new ArrayList<>();
                
                ids.add(objectOids[i]);
                temObjects.put(objectClasses[i], ids);
            }

            HashMap<String, String[]> objects = new HashMap<>();
            for(String className : temObjects.keySet()){
                List<String> ids = temObjects.get(className);
                String[] ids_ = new String[ids.size()];
                for (int i=0; i<ids.size(); i++) 
                    ids_[i] = ids.get(i);
                
                objects.put(className, ids_);
            }
            bem.moveObjectsToPool(targetClass, targetOid, objects);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to pool with id %s", Arrays.toString(objectOids), targetOid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "moveObjectsToPool"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void moveObjects(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateCall("moveObjects", "127.0.0.1", sessionId);
            HashMap<String,List<String>> temObjects = new HashMap<>();
            for (int i = 0; i< objectClasses.length; i++){
                List<String> ids = temObjects.get(objectClasses[i]);
                if (ids == null)
                    ids = new ArrayList<>();
                
                ids.add(objectOids[i]);
                temObjects.put(objectClasses[i], ids);
            }
            
            HashMap<String,String[]> objects = new HashMap<>();
            for(String className : temObjects.keySet()){
                List<String> ids = temObjects.get(className);
                String[] ids_ = new String[ids.size()];
                for (int i=0; i<ids.size(); i++) 
                    ids_[i] = ids.get(i);
                
                objects.put(className, ids_);
            }
            bem.moveObjects(targetClass, targetOid, objects);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to object with id %s", Arrays.toString(objectOids), targetOid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "moveObjects"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void moveSpecialObjects(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateCall("moveSpecialObjects", "127.0.0.1", sessionId);
            HashMap<String,List<String>> temObjects = new HashMap<>();
            
            for (int i = 0; i< objectClasses.length; i++){
                List<String> ids = temObjects.get(objectClasses[i]);
                if (ids == null)
                    ids = new ArrayList<>();
                
                ids.add(objectOids[i]);
                temObjects.put(objectClasses[i], ids);
            }
            
            HashMap<String,String[]> objects = new HashMap<>();
            for(String className : temObjects.keySet()){
                List<String> ids = temObjects.get(className);
                String[] ids_ = new String[ids.size()];
                for (int i=0; i<ids.size(); i++) 
                    ids_[i] = ids.get(i);
                
                objects.put(className, ids_);
            }
            
            bem.moveSpecialObjects(targetClass, targetOid, objects);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to object with id %s", Arrays.toString(objectOids), targetOid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "moveSpecialObjects"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void movePoolItemToPool(String poolId, String poolItemClassName, String poolItemId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("movePoolItem", "127.0.0.1", sessionId);
            bem.movePoolItem(poolId, poolItemClassName, poolItemId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                String.format("%s moved to pool with id %s", poolItemId, poolId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "movePoolItemToPool"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String[] copyObjects(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, boolean recursive, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateCall("copyObjects", "127.0.0.1", sessionId);
            HashMap<String,String[]> objects = new HashMap<>();
            for (int i = 0; i< objectClasses.length;i++){
                if (objects.get(objectClasses[i]) == null)
                    objects.put(objectClasses[i], new String[]{objectOids[i]});
            }
            String[] newObjects = bem.copyObjects(targetClass, targetOid, objects, recursive);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to (Special) object with id %s of class %s", Arrays.toString(newObjects), targetOid, targetClass));
            return newObjects;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "copyObjects"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String[] copySpecialObjects(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, boolean recursive, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateCall("copySpecialObjects", "127.0.0.1", sessionId);
            HashMap<String,String[]> objects = new HashMap<>();
            for (int i = 0; i< objectClasses.length;i++){
                if (objects.get(objectClasses[i]) == null)
                    objects.put(objectClasses[i], new String[]{objectOids[i]});
            }
            String[] newObjects = bem.copySpecialObjects(targetClass, targetOid, objects, recursive);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to (Special)object with id %s of class %s", Arrays.toString(newObjects), targetOid, targetClass));
            return newObjects;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "copySpecialObjects"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void copyPoolItemToPool(String poolId, String poolItemClassName, String poolItemId, boolean recursive, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("movePoolItem", "127.0.0.1", sessionId);
            String id = bem.copyPoolItem(poolId, poolItemClassName, poolItemId, recursive);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                String.format("%s moved to pool with id %s", poolItemId, poolId));
//            return id;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "copyPoolItemToPool"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteAttributeMetadata> getMandatoryAttributesInClass(String className, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
                aem.validateCall("getMandatoryAttributesInClass", "127.0.0.1", sessionId);
                List<AttributeMetadata> mandatoryObjectAttributes = mem.getMandatoryAttributesInClass(className);
                return RemoteAttributeMetadata.toRemoteAttributeList(mandatoryObjectAttributes);

            } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getMandatoryAttributesInClass"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String[] createBulkObjects(String className, String parentClassName, String parentOid, int numberOfObjects, String namePattern, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createBulkObjects", "127.0.0.1", sessionId);
            
            String[] newObjects = bem.createBulkObjects(className, parentClassName, parentOid, numberOfObjects, namePattern);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                    String.format("%s new objects of class %s", numberOfObjects, className));
            
            return newObjects;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createBulkObjects"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String[] createBulkSpecialObjects(String className, String parentClassName, String parentId, int numberOfSpecialObjects, String namePattern, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createBulkSpecialObjects", "127.0.0.1", sessionId);
            
            String[] newSpecialObjects = bem.createBulkSpecialObjects(className, parentClassName, parentId, numberOfSpecialObjects, namePattern);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                    String.format("%s new special objects  of class %s", numberOfSpecialObjects, className));
            
            return newSpecialObjects;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createBulkSpecialObjects"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void connectMirrorPort(String[] aObjectClass, String[] aObjectId, String[] bObjectClass, String[] bObjectId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        if (aObjectId == bObjectId)
            throw new ServerSideException("A port can not be mirror to itself");
        
        try {
            aem.validateCall("connectMirrorPort", "127.0.0.1", sessionId);
            
            if(aObjectClass.length != bObjectClass.length || bObjectId.length != aObjectId.length){
                throw new ServerSideException(String.format("Not the same number of front = %s and back ports = %s", aObjectId.length, bObjectId.length));
            }
            
            for (int i=0; i < aObjectClass.length; i++) {
                if (!mem.isSubclassOf("GenericPort", aObjectClass[i])) //NOI18N
                    throw new ServerSideException(String.format("Object %s is not a port", bem.getObjectLight(aObjectClass[i], aObjectId[i])));
            
                if (!mem.isSubclassOf("GenericPort", bObjectClass[i])) //NOI18N
                    throw new ServerSideException(String.format("Object %s is not a port", bem.getObjectLight(bObjectClass[i], bObjectId[i])));

                if (bem.hasSpecialRelationship(aObjectClass[i], aObjectId[i], "mirror", 1)) //NOI18N
                    throw new ServerSideException(String.format("Object %s already has a mirror port", bem.getObjectLight(aObjectClass[i], aObjectId[i])));

                if (bem.hasSpecialRelationship(bObjectClass[i], bObjectId[i], "mirror", 1)) //NOI18N
                    throw new ServerSideException(String.format("Object %s already has a mirror port", bem.getObjectLight(bObjectClass[i], bObjectId[i])));
                
                bem.createSpecialRelationship(aObjectClass[i], aObjectId[i], bObjectClass[i], bObjectId[i], "mirror", true); //NOI18N
            
                aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), aObjectClass[i], aObjectId[i], 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                    "mirror", "", aObjectId[i] + ", " + bObjectId[i], ""); //NOI18N          
            }
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "connectMirrorPort"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void releaseMirrorPort(String objectClass, String objectId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("releaseMirrorPort", "127.0.0.1", sessionId);
            if (!mem.isSubclassOf("GenericPort", objectClass)) //NOI18N
                throw new ServerSideException(String.format("Object %s is not a port", bem.getObjectLight(objectClass, objectId)));
                        
            BusinessObjectLight theOtherPort = null;
            if (bem.hasSpecialRelationship(objectClass, objectId, "mirror", 1)) //NOI18N
                theOtherPort = bem.getSpecialAttribute(objectClass, objectId, "mirror").get(0); //NOI18N
            
            if (theOtherPort == null)
                throw new ServerSideException(String.format("Object %s no has a mirror port", bem.getObjectLight(objectClass, objectId)));
                
            bem.releaseSpecialRelationship(objectClass, objectId, "-1", "mirror"); //NOI18N   
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), objectClass, objectId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                "mirror", theOtherPort.getId(), "", ""); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "releaseMirrorPort"), ex);
          throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createPhysicalConnection(String aObjectClass, String aObjectId, String bObjectClass, String bObjectId, String name, String connectionClass, String templateId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        String newConnectionId = null;
        
        try {
            aem.validateCall("createPhysicalConnection", "127.0.0.1", sessionId);
            
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, connectionClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not subclass of GenericPhysicalConnection", connectionClass)); //NOI18N
            
            //The connection (either link or container, will be created in the closest common parent between the endpoints)
            BusinessObjectLight commonParent = bem.getCommonParent(aObjectClass, aObjectId, bObjectClass, bObjectId);
            
            if (commonParent == null || commonParent.getName().equals(Constants.DUMMY_ROOT))
                throw new ServerSideException("The objects provided does not have a common parent, or it is the navigation root. The connection can not be created");
            
            boolean isLink = false;
            
            //Check if the endpoints are already connected, but only if the connection is a link (the endpoints are ports)
            if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, connectionClass)) { //NOI18N
                
                if (!mem.isSubclassOf("GenericPort", aObjectClass) || !mem.isSubclassOf("GenericPort", bObjectClass)) //NOI18N
                    throw new ServerSideException("One of the endpoints provided is not a port");
                
                if (!bem.getSpecialAttribute(aObjectClass, aObjectId, "endpointA").isEmpty()) //NOI18N
                    
                    throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(aObjectClass, aObjectId)));

                if (!bem.getSpecialAttribute(bObjectClass, bObjectId, "endpointB").isEmpty()) //NOI18N
                    throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(bObjectClass, bObjectId)));
                
                isLink = true;
            }

            
            HashMap<String, String> attributes = new HashMap<>();
            if (name == null || name.isEmpty())
                throw new ServerSideException("The name of the connection can not be empty");
            
            attributes.put(Constants.PROPERTY_NAME, name);
            
            newConnectionId = bem.createSpecialObject(connectionClass, commonParent.getClassName(), commonParent.getId(), attributes, templateId);
            
            if (isLink) { //Check connector mappings only if it's a link
                aem.checkRelationshipByAttributeValueBusinessRules(connectionClass, newConnectionId, aObjectClass, aObjectId);
                aem.checkRelationshipByAttributeValueBusinessRules(connectionClass, newConnectionId, bObjectClass, bObjectId);
            }
            
            bem.createSpecialRelationship(connectionClass, newConnectionId, aObjectClass, aObjectId, "endpointA", true);
            bem.createSpecialRelationship(connectionClass, newConnectionId, bObjectClass, bObjectId, "endpointB", true);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s [%s] (%s)", name, connectionClass, newConnectionId));
            
            return newConnectionId;
        } catch (InventoryException e) {
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newConnectionId != null)
                deleteObjects(new String[]{ connectionClass }, new String[]{ newConnectionId }, true, sessionId);

            throw new ServerSideException(e.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createPhysicalConnection"), ex);
            if (newConnectionId != null)
                deleteObjects(new String[]{ connectionClass }, new String[]{ newConnectionId }, true, sessionId);
            throw new ServerSideException(ex.getMessage());
        }
        
    }

    @Override
    public String[] createPhysicalConnections(String[] aObjectClasses, String[] aObjectIds, String[] bObjectClasses, String[] bObjectIds, String name, String connectionClass, String templateId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        String newConnectionId = null;
        
        try {
            aem.validateCall("createPhysicalConnection", "127.0.0.1", sessionId);
            
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, connectionClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not subclass of GenericPhysicalConnection", connectionClass)); //NOI18N
            
            if(aObjectClasses.length != aObjectIds.length && bObjectClasses.length != bObjectIds.length && 
                    aObjectClasses.length != bObjectClasses.length && aObjectIds.length != bObjectIds.length)
                throw new ServerSideException("The number of ports provided does not match. The connections can not be created");
            
            BusinessObjectLight commonParent = null;
            boolean isLink;
            
            HashMap<String, String> attributes = new HashMap<>();
            if (name == null || name.isEmpty())
                throw new ServerSideException("The name of the connection can not be empty");
            //The connection (either link or container, will be created in the closest common parent between the endpoints)
            
            String[] newConnectionIds = new String[aObjectIds.length];
            
            for (int i = 0; i < aObjectClasses.length; i++) {
                commonParent = bem.getCommonParent(aObjectClasses[i], aObjectIds[i], bObjectClasses[i], bObjectIds[i]);
                
                if (commonParent == null || commonParent.getName().equals(Constants.DUMMY_ROOT))
                    throw new ServerSideException("The objects provided does not have a common parent, or it is the navigation root. The connection can not be created");
                
                isLink = false;
            
                //Check if the endpoints are already connected, but only if the connection is a link (the endpoints are ports)
                if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, connectionClass)) { //NOI18N

                    if (!mem.isSubclassOf("GenericPort", aObjectClasses[i]) || !mem.isSubclassOf("GenericPort", bObjectClasses[i])) //NOI18N
                        throw new ServerSideException("One of the endpoints provided is not a port");

                    if (!bem.getSpecialAttribute(aObjectClasses[i], aObjectIds[i], "endpointA").isEmpty()) //NOI18N

                        throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(aObjectClasses[i], aObjectIds[i])));

                    if (!bem.getSpecialAttribute(bObjectClasses[i], bObjectIds[i], "endpointB").isEmpty()) //NOI18N
                        throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(bObjectClasses[i], bObjectIds[i])));

                    isLink = true;
                }
                
                attributes.put(Constants.PROPERTY_NAME, name + "_" + i);
          
                newConnectionId = bem.createSpecialObject(connectionClass, commonParent.getClassName(), commonParent.getId(), attributes, templateId);
                newConnectionIds[i] = newConnectionId;
                
                if (isLink) { //Check connector mappings only if it's a link
                   aem.checkRelationshipByAttributeValueBusinessRules(connectionClass, newConnectionId, aObjectClasses[i], aObjectIds[i]);
                   aem.checkRelationshipByAttributeValueBusinessRules(connectionClass, newConnectionId, bObjectClasses[i], bObjectIds[i]);
                }

                bem.createSpecialRelationship(connectionClass, newConnectionId, aObjectClasses[i], aObjectIds[i], "endpointA", true);
                bem.createSpecialRelationship(connectionClass, newConnectionId, bObjectClasses[i], bObjectIds[i], "endpointB", true);

                aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                       ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s [%s] (%s)", name, connectionClass, newConnectionId));
            }//end for
            
            return newConnectionIds;
        } catch (InventoryException e) {
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newConnectionId != null)
                deleteObjects(new String[]{ connectionClass }, new String[]{ newConnectionId }, true, sessionId);

            throw new ServerSideException(e.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createPhysicalConnections"), ex);
            if (newConnectionId != null)
                deleteObjects(new String[]{ connectionClass }, new String[]{ newConnectionId }, true, sessionId);
             throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getPhysicalConnectionEndpoints(String connectionClass, String connectionId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            String endpointAName = null, endpointBName = null;
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, connectionClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a physical or logical connection", connectionClass));
            else{
                endpointAName = "endpointA";
                endpointBName = "endpointB";
            }
            List<BusinessObjectLight> endpointA = bem.getSpecialAttribute(connectionClass, connectionId, endpointAName); //NOI18N
            List<BusinessObjectLight> endpointB = bem.getSpecialAttribute(connectionClass, connectionId, endpointBName); //NOI18N
            return new RemoteObjectLight[] {endpointA.isEmpty() ? null : new RemoteObjectLight(endpointA.get(0)), 
                                            endpointB.isEmpty() ? null : new RemoteObjectLight(endpointB.get(0))};

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getPhysicalConnectionEndpoints"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteLogicalConnectionDetails getLogicalLinkDetails(String linkClass, String linkId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getLogicalLinkDetails", "127.0.0.1", sessionId); //NOI18N
            
            BusinessObject linkObject = bem.getObject(linkClass, linkId);
            
            BusinessObjectLight endpointA = null;
            BusinessObjectLight endpointB = null;
            List<BusinessObjectLight> physicalPathA = null, physicalPathB = null;
            String endpointARelationshipName, endpointBRelationshipName;
            
            if (mem.isSubclassOf("GenericSDHTributaryLink", linkClass)) { //NOI18N
                endpointARelationshipName = "sdhTTLEndpointA"; //NOI18N
                endpointBRelationshipName = "sdhTTLEndpointB"; //NOI18N
            } else {
                if ("MPLSLink".equals(linkClass)) { //NOI18N
                    endpointARelationshipName = "mplsEndpointA"; //NOI18N
                    endpointBRelationshipName = "mplsEndpointB"; //NOI18N
                }
                else
                    throw new ServerSideException(String.format("Class %s is not a supported logical link", linkClass)); 
            }
            
            List<BusinessObjectLight> endpointARelationship = bem.getSpecialAttribute(linkClass, linkId, endpointARelationshipName);
            if (!endpointARelationship.isEmpty()) {
                endpointA = endpointARelationship.get(0);
                physicalPathA = bem.getPhysicalPath(endpointA.getClassName(), endpointA.getId());
            }
            
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalPathForVlansEndpointA = new HashMap<>();    
            if(physicalPathA != null && !physicalPathA.isEmpty())
                physicalPathForVlansEndpointA = getPhysicalPathVlans(physicalPathA.get(physicalPathA.size() -1));
            
            else if(endpointA != null)
                physicalPathForVlansEndpointA = getPhysicalPathVlans(endpointA);
            
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalPathForBDisEndpointA = new HashMap<>();    
//            if(physicalPathA != null && !physicalPathA.isEmpty())
//                physicalPathForBDisEndpointA = getEndPointContinuity("networkBridgeInterface", physicalPathA.get(physicalPathA.size() -1));
//            
//            else if(endpointA != null)
//                physicalPathForBDisEndpointA = getEndPointContinuity("networkBridgeInterface", endpointA);

            List<BusinessObjectLight> endpointBRelationship = bem.getSpecialAttribute(linkClass, linkId, endpointBRelationshipName);
            if (!endpointBRelationship.isEmpty()) {
                endpointB = endpointBRelationship.get(0);
                physicalPathB = bem.getPhysicalPath(endpointB.getClassName(), endpointB.getId());
            }
            
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalPathForVlansEndpointB = new HashMap<>();    
            if(physicalPathB != null && !physicalPathB.isEmpty())
                physicalPathForVlansEndpointB = getPhysicalPathVlans(physicalPathB.get(physicalPathB.size() -1));
            
            else if(endpointB != null)
                physicalPathForVlansEndpointB = getPhysicalPathVlans(endpointB);
            
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalPathForBDisEndpointB = new HashMap<>();    
//            if(physicalPathB != null && !physicalPathB.isEmpty())
//                physicalPathForBDisEndpointB = getEndPointContinuity("networkBridgeInterface", physicalPathB.get(physicalPathB.size() -1));
//            
//            else if(endpointB != null)
//                physicalPathForBDisEndpointB =  getEndPointContinuity("networkBridgeInterface", endpointB);

            return new RemoteLogicalConnectionDetails(linkObject, endpointA, endpointB, 
                    physicalPathA == null ? new ArrayList<>() : physicalPathA, 
                    physicalPathB == null ? new ArrayList<>() : physicalPathB,
                    physicalPathForVlansEndpointA, physicalPathForBDisEndpointA, 
                    physicalPathForVlansEndpointB, physicalPathForBDisEndpointB);
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }  catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getLogicalLinkDetails"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    /**
     * Checks the continuity throw ports that belongs to the same VLAN
     * @param endpoint a given port to check if belong to a vlan
     * @return a map with key: port, value: physical path of that port
     * @throws ServerSideException 
     */
    private HashMap<BusinessObjectLight, List<BusinessObjectLight>> getPhysicalPathVlans(BusinessObjectLight endpoint) 
            throws ServerSideException{
        try {
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> vlansPhysicalPath = new HashMap<>();
            if(endpoint != null){
                //we get the the vlans to which the port belongs
                List<BusinessObjectLight> vlans = bem.getSpecialAttribute(endpoint.getClassName(), endpoint.getId(), "portBelongsToVlan");
                for (BusinessObjectLight vlan : vlans) { //We get all the port of every vlan
                    List<BusinessObjectLight> vlanPorts = bem.getSpecialAttribute(vlan.getClassName(), vlan.getId(), "portBelongsToVlan");
                    for (BusinessObjectLight vlanPort : vlanPorts) {
                        if(vlanPort.getId() != null && endpoint.getId() != null && !vlanPort.getId().equals(endpoint.getId())){//we get the physical path for every port of the vlan except of the given endpoint 
                            List<BusinessObjectLight> vlanPhysicalPath = bem.getPhysicalPath(vlanPort.getClassName(), vlanPort.getId());
                            if(!vlanPhysicalPath.isEmpty())
                                vlansPhysicalPath.put(vlanPort, vlanPhysicalPath);
                        }
                    }
                }
            }
            return vlansPhysicalPath;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public RemoteViewObject validateSavedE2EView(List<String> linkClasses, List<String> linkIds, RemoteViewObject savedView, String sessionId) throws ServerSideException {
//        if (bem == null)
//            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend")); //NOI18N
//        try {
//            aem.validateCall("validateSavedE2EView", "127.0.0.1", sessionId); //NOI18N
//            ViewModule viewModule = (ViewModule)aem.getCommercialModule("E2E Views Module"); //NOI18N
//            ViewObject viewObject = new ViewObject(savedView.getId(), savedView.getName(), savedView.getDescription(), savedView.getViewClassName());
//            viewObject.setStructure(savedView.getStructure());
//            
//            return new RemoteViewObject(viewModule.validateSavedE2EView(linkClasses, linkIds, viewObject));
//        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
//            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
//                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getLogicalLinkDetails"), ex);
//            throw new ServerSideException(ex.getMessage());
//        }
    return null;
    }

    @Override
    public RemoteViewObject getE2View(List<String> linkClasses, List<String> linkIds, boolean includeVLANs, boolean includeBDIs, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getContainersBetweenObjects(String objectAClass, String objectAId, String objectBClass, String objectBId, String containerClass, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend")); //NOI18N
        try {
            aem.validateCall("getContainersBetweenObjects", "127.0.0.1", sessionId); //NOI18N
            List<RemoteObjectLight> res = new ArrayList<>();

            HashMap<String, List<BusinessObjectLight>> specialAttributesA = bem.getSpecialAttributes(objectAClass, objectAId);
            HashMap<String, List<BusinessObjectLight>> specialAttributesB = bem.getSpecialAttributes(objectBClass, objectBId);
            
            if(!specialAttributesA.isEmpty() && !specialAttributesB.isEmpty()){
                List<BusinessObjectLight> wireContainersListA = new  ArrayList<>();

                if(specialAttributesA.get("endpointA") != null){
                    for(BusinessObjectLight container : specialAttributesA.get("endpointA")){
                        if(container.getClassName().equals(containerClass))
                            wireContainersListA.add(container);
                    }
                }

                if(specialAttributesA.get("endpointB") != null){
                    for(BusinessObjectLight container : specialAttributesA.get("endpointB")){
                        if(container.getClassName().equals(containerClass))
                            wireContainersListA.add(container);
                    }
                }

                if(specialAttributesB.get("endpointA") != null){
                    for(BusinessObjectLight container : specialAttributesB.get("endpointA")){
                        if(container.getClassName().equals(containerClass)){
                            if(wireContainersListA.contains(container))
                                res.add(new RemoteObjectLight(container));
                        }
                    }
                }

                if(specialAttributesB.get("endpointB") != null){
                    for(BusinessObjectLight container : specialAttributesB.get("endpointB")){
                        if(container.getClassName().equals(containerClass)){
                            if(wireContainersListA.contains(container))
                                res.add(new RemoteObjectLight(container));
                        }
                    }
                }
            }
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getContainersBetweenObjects"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLightList> getPhysicalConnectionsInObject(String objectClass, String objectId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend")); //NOI18N
        try {
            aem.validateCall("getPhysicalConnectionsInObject", "127.0.0.1", sessionId); //NOI18N
            List<RemoteObjectLightList> res = new ArrayList<>();
            
            List<BusinessObjectLight> allCommunicationsPorts = bem.getChildrenOfClassLightRecursive(objectId, objectClass, "GenericCommunicationsPort", -1);
            
            for (BusinessObjectLight aCommunicationsPort : allCommunicationsPorts) {
                List<BusinessObjectLight> physicalPath = bem.getPhysicalPath(aCommunicationsPort.getClassName(), aCommunicationsPort.getId());
                if (physicalPath.size() > 1)
                    res.add(new RemoteObjectLightList(physicalPath));
            }
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getPhysicalConnectionsInObject"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObject getLinkConnectedToPort(String portClassName, String portId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getPhysicalPath(String objectClass, String objectId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getPhysicalPath", "127.0.0.1", sessionId);
            if (!mem.isSubclassOf("GenericPort", objectClass))
                throw new ServerSideException(String.format("Class %s is not a port", objectClass));
            
            List<BusinessObjectLight> thePath = bem.getPhysicalPath(objectClass, objectId); 
            return RemoteObjectLight.toRemoteObjectLightArray(thePath);

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getPhysicalPath"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void connectPhysicalLinks(String[] sideAClassNames, String[] sideAIds, String[] linksClassNames, String[] linksIds, String[] sideBClassNames, String[] sideBIds, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("connectPhysicalLinks", "127.0.0.1", sessionId);
            for (int i = 0; i < sideAClassNames.length; i++){
                if (linksClassNames[i] != null && !mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, linksClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Class %s is not a physical link", linksClassNames[i]));
                if (sideAClassNames[i] != null && !mem.isSubclassOf("GenericPort", sideAClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Class %s is not a port", sideAClassNames[i]));
                if (sideBClassNames[i] != null && !mem.isSubclassOf("GenericPort", sideBClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Class %s is not a port", sideBClassNames[i]));
                
                if (Objects.equals(sideAIds[i], sideBIds[i]))
                    throw new ServerSideException("Can not connect a port to itself");
                
                String endpointAName = "endpointA", endpointBName = "endpointB";
 
                List<BusinessObjectLight> aEndpointList = bem.getSpecialAttribute(linksClassNames[i], linksIds[i], endpointAName); //NOI18N
                List<BusinessObjectLight> bEndpointList = bem.getSpecialAttribute(linksClassNames[i], linksIds[i], endpointBName); //NOI18N
                
                if (!aEndpointList.isEmpty()){
                    if (Objects.equals(aEndpointList.get(0).getId(), sideAIds[i]) || Objects.equals(aEndpointList.get(0).getId(), sideBIds[i]))
                        throw new ServerSideException("The link is already related to at least one of the endpoints");
                }
                
                if (!bEndpointList.isEmpty()){
                    if (Objects.equals(bEndpointList.get(0).getId(), sideAIds[i]) || Objects.equals(bEndpointList.get(0).getId(), sideBIds[i]))
                        throw new ServerSideException("The link is already related to at least one of the endpoints");
                }
                
                if (sideAIds[i] != null && sideAClassNames[i] != null) {
                    if (!bem.getSpecialAttribute(sideAClassNames[i], sideAIds[i], endpointAName).isEmpty() || //NOI18N
                        !bem.getSpecialAttribute(sideAClassNames[i], sideAIds[i], endpointBName).isEmpty()) //NOI18N
                        throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(sideAClassNames[i], sideAIds[i])));
                    
                    if (aEndpointList.isEmpty()) {
                        aem.checkRelationshipByAttributeValueBusinessRules(linksClassNames[i], linksIds[i], sideAClassNames[i], sideAIds[i]);
                        bem.createSpecialRelationship(linksClassNames[i], linksIds[i], sideAClassNames[i], sideAIds[i], endpointAName, true); //NOI18N
                    }
                    else
                        throw new ServerSideException(String.format("Link %s already has an endpoint A", bem.getObjectLight(linksClassNames[i], linksIds[i])));
                }
                if (sideBIds[i] != null && sideBClassNames[i] != null) {
                    if (!bem.getSpecialAttribute(sideBClassNames[i], sideBIds[i], endpointBName).isEmpty() || //NOI18N
                        !bem.getSpecialAttribute(sideBClassNames[i], sideBIds[i], endpointAName).isEmpty()) //NOI18N
                        throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(sideBClassNames[i], sideBIds[i])));
                    
                    if (bEndpointList.isEmpty()) {
                        aem.checkRelationshipByAttributeValueBusinessRules(linksClassNames[i], linksIds[i], sideBClassNames[i], sideBIds[i]);
                        bem.createSpecialRelationship(linksClassNames[i], linksIds[i], sideBClassNames[i], sideBIds[i], endpointBName, true); //NOI18N
                    }
                    else
                        throw new ServerSideException(String.format("Link %s already has an endpoint B", bem.getObjectLight(linksClassNames[i], linksIds[i])));
                }
                //Once the link has been connected, we have to check if the parent is consistent with the new endpoints
                //(that is, unless the link is inside a container. In that case, the container is respected as parent)
                //getParent("127.0.0.1", i, "127.0.0.1", sessionId)
            }
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                String.format("Connected physical links %s-%s-%s", Arrays.toString(sideAIds), Arrays.toString(linksIds), Arrays.toString(sideBIds)));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "connectPhysicalLinks"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void connectPhysicalContainers(String[] sideAClassNames, String[] sideAIds, String[] containersClassNames, String[] containersIds, String[] sideBClassNames, String[] sideBIds, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("connectPhysicalContainers", "127.0.0.1", sessionId);
            for (int i = 0; i < sideAClassNames.length; i++){
                
                if (containersClassNames[i] != null && !mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONTAINER, containersClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Class %s is not a physical container", containersClassNames[i]));
                if (sideAClassNames[i] != null && mem.isSubclassOf("GenericPort", sideAClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Can not connect an instance of %s to a port", containersClassNames[i]));
                if (sideBClassNames[i] != null && mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, sideBClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Can not connect an instance of %s to a port", containersClassNames[i]));
                
                if (Objects.equals(sideAIds[i], sideBIds[i]))
                    throw new ServerSideException("Can not connect an object to itself");
                
                if (sideAIds[i] != null && sideAClassNames[i] != null) {
                    List<BusinessObjectLight> aEndpointList = bem.getSpecialAttribute(containersClassNames[i], containersIds[i], "endpointA"); //NOI18N
                    if (aEndpointList.isEmpty())
                        bem.createSpecialRelationship(containersClassNames[i], containersIds[i], sideAClassNames[i], sideAIds[i], "endpointA", true); //NOI18N
                    else
                        throw new ServerSideException(String.format("Container %s already has an endpoint A", bem.getObjectLight(containersClassNames[i], containersIds[i])));
                }
                
                if (sideBIds[i] != null && sideBClassNames[i] != null) {
                    List<BusinessObjectLight> bEndpointList = bem.getSpecialAttribute(containersClassNames[i], containersIds[i], "endpointB"); //NOI18N
                    if (bEndpointList.isEmpty())
                        bem.createSpecialRelationship(containersClassNames[i], containersIds[i], sideBClassNames[i], sideBIds[i], "endpointB", true); //NOI18N
                    else
                        throw new ServerSideException(String.format("Container %s already has a endpoint B", bem.getObjectLight(containersClassNames[i], containersIds[i])));
                }
            }
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                String.format("Connected physical containers %s-%s-%s", Arrays.toString(sideAIds), Arrays.toString(containersIds), Arrays.toString(sideBIds)));            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "connectPhysicalContainers"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void disconnectPhysicalConnection(String connectionClass, String connectionId, int sideToDisconnect, String sessionId) throws ServerSideException {
         if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("disconnectPhysicalConnection", "127.0.0.1", sessionId);
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, connectionClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a physical connection", connectionClass));
            
            String  affectedProperties = "", oldValues = "";
            switch (sideToDisconnect) {
                case 1: //A side
                    BusinessObjectLight endpointA = bem.getSpecialAttribute(connectionClass, connectionId, "endpointA").get(0); //NOI18N                    
                    bem.releaseRelationships(connectionClass, connectionId, Arrays.asList("endpointA")); //NOI18N
                    
                    affectedProperties += "endpointA" + " "; //NOI18N
                    oldValues += endpointA.getId() + " ";
                    break;
                case 2: //B side
                    BusinessObjectLight endpointB = bem.getSpecialAttribute(connectionClass, connectionId, "endpointB").get(0); //NOI18N                    
                    bem.releaseRelationships(connectionClass, connectionId, Arrays.asList("endpointB")); //NOI18N
                    
                    affectedProperties += "endpointB" + " "; //NOI18N
                    oldValues += endpointB.getId() + " ";
                    break;
                case 3: //Both sides
                    endpointA = bem.getSpecialAttribute(connectionClass, connectionId, "endpointA").get(0); //NOI18N
                    endpointB = bem.getSpecialAttribute(connectionClass, connectionId, "endpointB").get(0); //NOI18N
                    bem.releaseRelationships(connectionClass, connectionId, Arrays.asList("endpointA", "endpointB")); //NOI18N
                    
                    affectedProperties += "endpointA" + " "; //NOI18N
                    oldValues += endpointA.getId() + " ";
                    
                    affectedProperties += "endpointB" + " "; //NOI18N
                    oldValues += endpointB.getId() + " ";
                    break;
                default:
                    throw new ServerSideException(String.format("Wrong side to disconnect option"));
            }
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), connectionClass, connectionId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                affectedProperties, oldValues, "", ""); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "disconnectPhysicalConnection"), ex);
            throw new ServerSideException(ex.getMessage());
        }

    }

    @Override
    public void reconnectPhysicalConnection(String connectionClass, String connectionId, String newASideClass, String newASideId, String newBSideClass, String newBSideId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("reconnectPhysicalConnection", "127.0.0.1", sessionId);
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, connectionClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a physical connection", connectionClass));
            
            boolean isLink = mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, connectionClass);
            
            if (newASideClass != null && newASideId != null && !newASideId.equals("-1")) { //Reconnect the A side
                disconnectPhysicalConnection(connectionClass, connectionId, 1 /*Disconnect A side*/, sessionId);
                if (isLink)
                    connectPhysicalLinks(new String[] { newASideClass }, new String[] { newASideId }, new String[] { connectionClass }, 
                            new String[] { connectionId }, new String[] { null }, 
                            new String[] { null }, sessionId);
                else
                    connectPhysicalContainers(new String[] { newASideClass }, new String[] { newASideId }, new String[] { connectionClass }, 
                            new String[] { connectionId }, new String[] { null }, 
                            new String[] { null }, sessionId);
            }
            
            if (newBSideClass != null && newBSideId != null && !newBSideId.equals("-1")) { //Reconnect the B side
                disconnectPhysicalConnection(connectionClass, connectionId, 2 /*Disconnect B side*/, sessionId);
                if (isLink)
                    connectPhysicalLinks(new String[] { null }, new String[] { null }, new String[] { connectionClass }, 
                            new String[] { connectionId }, new String[] { newBSideClass }, 
                            new String[] { newBSideId }, sessionId);
                else
                    connectPhysicalContainers(new String[] { null }, new String[] { null }, new String[] { connectionClass }, 
                            new String[] { connectionId }, new String[] { newBSideClass }, 
                            new String[] { newBSideId }, sessionId);
            }
            
            //getCommonParent(connectionClass, newASideId, connectionClass, newBSideId, "127.0.0.1", sessionId)
            //moveSpecialObjects(newASideClass, connectionId, objectClasses, objectOids, "127.0.0.1", sessionId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "reconnectPhysicalConnection"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deletePhysicalConnection(String objectClass, String objectId, String sessionId) throws ServerSideException {
         if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deletePhysicalConnection", "127.0.0.1", sessionId);
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, objectClass))
                throw new ServerSideException(String.format("Class %s is not a physical connection", objectClass));
            
            bem.deleteObject(objectClass, objectId, true);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                    String.format("Deleted %s instance with id %s", objectClass, objectId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deletePhysicalConnection"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void associateObjectToService(String objectClass, String objectId, String serviceClass, String serviceId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("associateObjectToService", "127.0.0.1", sessionId);
            if (!mem.isSubclassOf("GenericService", serviceClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a service", serviceClass));
            
            bem.createSpecialRelationship(serviceClass, serviceId, objectClass, objectId, "uses", true); //NOI18N
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), serviceClass, serviceId, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                "uses", "", objectId, ""); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "associateObjectToService"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void associateObjectsToService(String[] objectClass, String[] objectId, String serviceClass, String serviceId, String sessionId) throws ServerSideException {
         if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            String affectedProperties = "", newValues = "";
            aem.validateCall("associateObjectsToService", "127.0.0.1", sessionId);
            if (!mem.isSubclassOf("GenericService", serviceClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a service", serviceClass));
            for (int i = 0; i < objectId.length; i++) {
                bem.createSpecialRelationship(serviceClass, serviceId, objectClass[i], objectId[i], "uses", true); //NOI18N
                affectedProperties += "uses" + " "; //NOI18N
                newValues += objectId[i] + " ";
            }            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), serviceClass, serviceId, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                affectedProperties, "", newValues, "Associate objects to service"); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "associateObjectsToService"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void releaseObjectFromService(String serviceClass, String serviceId, String targetId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("releaseObjectFromService", "127.0.0.1", sessionId);
            bem.releaseSpecialRelationship(serviceClass, serviceId, targetId, "uses"); //NOI18N
                       
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), serviceClass, serviceId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                "uses", targetId, "", "Release object from service"); //NOI18N
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "releaseObjectFromService"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getServiceResources(String serviceClass, String serviceId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getServiceResources", "127.0.0.1", sessionId);
            if (!mem.isSubclassOf(Constants.CLASS_GENERICSERVICE, serviceClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a service", serviceClass));
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSpecialAttribute(serviceClass, serviceId, "uses")); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getServiceResources"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ApplicationLogEntry[] getBusinessObjectAuditTrail(String objectClass, String objectId, int limit, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getBusinessObjectAuditTrail", "127.0.0.1", sessionId);
            List<ActivityLogEntry> entries = aem.getBusinessObjectAuditTrail(objectClass, objectId, limit);
            ApplicationLogEntry[] res = new ApplicationLogEntry[entries.size()];
            for (int i = 0; i< entries.size(); i++)
                res[i] = new ApplicationLogEntry(entries.get(i));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getBusinessObjectAuditTrail"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ApplicationLogEntry[] getGeneralActivityAuditTrail(int page, int limit, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getGeneralActivityAuditTrail", "127.0.0.1", sessionId);
            List<ActivityLogEntry> entries = aem.getGeneralActivityAuditTrail(page, limit);
            ApplicationLogEntry[] res = new ApplicationLogEntry[entries.size()];
            for (int i = 0; i< entries.size(); i++)
                res[i] = new ApplicationLogEntry(entries.get(i));
            
            return res;
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getGeneralActivityAuditTrail"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long attachFileToObject(String name, String tags, byte[] file, String className, String objectId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("attachFileToObject", "127.0.0.1", sessionId);
            return bem.attachFileToObject(name, tags, file, className, objectId);
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "attachFileToObject"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void detachFileFromObject(long fileObjectId, String className, String objectId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("detachFileFromObject", "127.0.0.1", sessionId);
            bem.detachFileFromObject(fileObjectId, className, objectId);
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "detachFileFromObject"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteFileObjectLight> getFilesForObject(String className, String objectId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            List<RemoteFileObjectLight> res = new ArrayList<>();
            aem.validateCall("getFilesForObject", "127.0.0.1", sessionId);
            List<FileObjectLight>filesForObject = bem.getFilesForObject(className, objectId);
            for (FileObjectLight objectFile :filesForObject)
                res.add(new RemoteFileObjectLight(objectFile.getFileOjectId(), objectFile.getName(), objectFile.getCreationDate(), objectFile.getTags()));
            return res;
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getFilesForObject"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteFileObject getFile(long fileObjectId, String className, String objectId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getFile", "127.0.0.1", sessionId);
            FileObject  fileForObject = (FileObject) bem.getFile(fileObjectId, className, objectId);
            return new RemoteFileObject(fileForObject.getFileOjectId(), fileForObject.getName(), fileForObject.getCreationDate(), fileForObject.getTags(), fileForObject.getFile());
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getFile"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateFileProperties(long fileObjectId, List<StringPair> properties, String className, String objectId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateFileProperties", "127.0.0.1", sessionId);
            bem.updateFileProperties(fileObjectId, properties, className, objectId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateFileProperties"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createClass(String className, String displayName, String description, boolean isAbstract, boolean isCustom, boolean isCountable, boolean isInDesign, String parentClassName, byte[] icon, byte[] smallIcon, int color, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createClass", "127.0.0.1", sessionId);
            ClassMetadata cm = new ClassMetadata();

            cm.setName(className);
            cm.setDisplayName(displayName);
            cm.setDescription(description);
            cm.setParentClassName(parentClassName);
            cm.setAbstract(isAbstract);
            cm.setColor(color);
            cm.setCountable(isCountable);
            cm.setCreationDate(Calendar.getInstance().getTimeInMillis());
            cm.setIcon(icon);
            cm.setSmallIcon(smallIcon);
            cm.setCustom(isCustom);
//            cm.setViewable(isViewable);
            cm.setInDesign(isInDesign);
            //TODO decode flags, set category
            //cm.setCategory(classDefinition.getCategory());
            long newClassId = mem.createClass(cm);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                String.format("Created %s class", className)
            );
            return newClassId;

        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createClass"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setClassProperties(long classId, String className, String displayName, String description, byte[] smallIcon, byte[] icon, int color, Boolean isAbstract, Boolean isInDesign, Boolean isCustom, Boolean isCountable, String sessionId) throws ServerSideException {
        if (mem == null)
             throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("setClassProperties", "127.0.0.1", sessionId);
            ClassMetadata cm = new ClassMetadata();
            
            cm.setName(className);
            cm.setDisplayName(displayName);
            cm.setDescription(description);
            cm.setAbstract(isAbstract);
            cm.setColor(color);
            cm.setCountable(isCountable);
            cm.setCreationDate(Calendar.getInstance().getTimeInMillis());
            cm.setIcon(icon);
            cm.setSmallIcon(smallIcon);
            cm.setCustom(isCustom);
            
            ChangeDescriptor changeDescriptor = mem.setClassProperties(cm);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                changeDescriptor);

         } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public boolean hasAttribute(String className, String attributeName, String sessionId) throws ServerSideException {
        if (aem == null || mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("hasAttribute", "127.0.0.1", sessionId);
            return mem.hasAttribute(className, attributeName);
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "hasAttribute"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteAttributeMetadata getAttribute(String className, String attributeName, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getAttribute", "127.0.0.1", sessionId);
            AttributeMetadata atrbMtdt = mem.getAttribute(className, attributeName);

            RemoteAttributeMetadata atrbInfo = new RemoteAttributeMetadata(atrbMtdt.getName(),
                                                       atrbMtdt.getDisplayName(),
                                                       atrbMtdt.getType(),
                                                       atrbMtdt.isAdministrative(),
                                                       atrbMtdt.isVisible(),
                                                       atrbMtdt.isUnique(),
                                                       atrbMtdt.isMandatory(),
                                                       atrbMtdt.isMultiple(),
                                                       atrbMtdt.getDescription(), 
                                                       atrbMtdt.getOrder());
            return atrbInfo;
         } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getAttribute"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteAttributeMetadata getAttributeForClassWithId(String classId, String attributeName, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));       
        try {
            aem.validateCall("getAttributeForClassWithId", "127.0.0.1", sessionId);
            AttributeMetadata atrbMtdt = mem.getAttribute(classId, attributeName);

            RemoteAttributeMetadata attrInfo = new RemoteAttributeMetadata(atrbMtdt.getName(),
                                                       atrbMtdt.getDisplayName(),
                                                       atrbMtdt.getType(),
                                                       atrbMtdt.isAdministrative(),
                                                       atrbMtdt.isVisible(),
                                                       atrbMtdt.isUnique(),
                                                       atrbMtdt.isMandatory(),
                                                       atrbMtdt.isMultiple(),
                                                       atrbMtdt.getDescription(), 
                                                       atrbMtdt.getOrder());
            return attrInfo;

         } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getAttributeForClassWithId"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void createAttribute(String className, String name, String displayName, String type, String description, boolean administrative, boolean visible, boolean isReadOnly, boolean noCopy, boolean unique, boolean mandatory, boolean multiple, int order, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));       
        try {
            aem.validateCall("createAttribute", "127.0.0.1", sessionId);
            AttributeMetadata attributeMetadata = new AttributeMetadata();

            attributeMetadata.setName(name);
            attributeMetadata.setDisplayName(displayName);
            attributeMetadata.setDescription(description);
            attributeMetadata.setReadOnly(isReadOnly);
            attributeMetadata.setType(type);
            attributeMetadata.setUnique(unique);
            attributeMetadata.setMandatory(mandatory);
            attributeMetadata.setVisible(visible);
            attributeMetadata.setNoCopy(noCopy);
            attributeMetadata.setOrder(order);

            mem.createAttribute(className, attributeMetadata, true);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Created attribute in %s class", className));

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createAttribute"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void createAttributeForClassWithId(long ClassId, String name, String displayName, String type, String description, boolean administrative, boolean visible, boolean readOnly, boolean noCopy, boolean unique, boolean mandatory, boolean multiple, int order, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));       
        try {
            aem.validateCall("createAttributeForClassWithId", "127.0.0.1", sessionId);
            AttributeMetadata attributeMetadata = new AttributeMetadata();

            attributeMetadata.setName(name);
            attributeMetadata.setDisplayName(displayName);
            attributeMetadata.setDescription(description);
            attributeMetadata.setReadOnly(readOnly);
            attributeMetadata.setType(type);
            attributeMetadata.setUnique(unique);
            attributeMetadata.setMandatory(mandatory);
            attributeMetadata.setVisible(visible);
            attributeMetadata.setNoCopy(noCopy);
            attributeMetadata.setOrder(order);
            
            ClassMetadata classMetadata = mem.getClass(ClassId);
            mem.createAttribute(ClassId, attributeMetadata);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Created attribute in %s class", classMetadata.getName()));

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createAttributeForClassWithId"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setAttributeProperties(String className, long attributeId, String name, String displayName, String description, String type, Boolean administrative, Boolean mandatory, Boolean multiple, Boolean noCopy, Boolean readOnly, Boolean unique, Boolean visible, Integer order, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));       
        try {
            aem.validateCall("setAttributeProperties", "127.0.0.1", sessionId);
            AttributeMetadata attrMtdt = new AttributeMetadata();

            attrMtdt.setId(attributeId);
            attrMtdt.setName(name);
            attrMtdt.setDisplayName(displayName);
            attrMtdt.setDescription(description);
            attrMtdt.setType(type);
            attrMtdt.setAdministrative(administrative);
            attrMtdt.setUnique(unique);
            attrMtdt.setMandatory(mandatory);
            attrMtdt.setMultiple(multiple);
            attrMtdt.setVisible(visible);
            attrMtdt.setReadOnly(readOnly);
            attrMtdt.setNoCopy(noCopy);
            attrMtdt.setOrder(order);

            mem.setAttributeProperties(className, attrMtdt);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Updated property in %s class", className));

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "setAttributeProperties"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setAttributePropertiesForClassWithId(long classId, long attributeId, String name, String displayName, String description, String type, Boolean administrative, Boolean mandatory, Boolean multiple, Boolean noCopy, Boolean readOnly, Boolean unique, Boolean visible, Integer order, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));       
        try {
            aem.validateCall("setAttributePropertiesForClassWithId", "127.0.0.1", sessionId);
            AttributeMetadata attrMtdt = new AttributeMetadata();

            attrMtdt.setId(attributeId);
            attrMtdt.setName(name);
            attrMtdt.setDisplayName(displayName);
            attrMtdt.setDescription(description);
            attrMtdt.setType(type);
            attrMtdt.setAdministrative(administrative);
            attrMtdt.setUnique(unique);
            attrMtdt.setMandatory(mandatory);
            attrMtdt.setMultiple(multiple);
            attrMtdt.setVisible(visible);
            attrMtdt.setReadOnly(readOnly);
            attrMtdt.setNoCopy(noCopy);
            attrMtdt.setOrder(order);

            ChangeDescriptor changeDescriptor = mem.setAttributeProperties(classId, attrMtdt);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                changeDescriptor);

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "setAttributePropertiesForClassWithId"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteAttribute(String className, String attributeName, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteAttribute", "127.0.0.1", sessionId);
            mem.deleteAttribute(className, attributeName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_METADATA_OBJECT, 
                String.format("Deleted attribute in %s class", className));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteAttribute"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteAttributeForClassWithId(long classId, String attributeName, String sessionId) throws ServerSideException {
       if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteAttributeForClassWithId", "127.0.0.1", sessionId);
            ClassMetadata classMetadata = mem.getClass(classId);
            mem.deleteAttribute(classId, attributeName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_METADATA_OBJECT, 
                String.format("Deleted attribute in %s class", classMetadata.getName()));

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteAttributeForClassWithId"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteClassMetadata getClass(String className, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getClass", "127.0.0.1", sessionId);
            ClassMetadata myClass = mem.getClass(className);
            return new RemoteClassMetadata(myClass);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getClass"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteClassMetadata getClassWithId(long classId, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getClassWithId", "127.0.0.1", sessionId);
            ClassMetadata myClass = mem.getClass(classId);
         
            return new RemoteClassMetadata(myClass);

         } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getClassWithId"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadataLight> getSubClassesLight(String className, boolean includeAbstractClasses, boolean includeSelf, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getSubclassesLight", "127.0.0.1", sessionId);
            List<RemoteClassMetadataLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classLightMetadata = mem.getSubClassesLight(className, includeAbstractClasses, includeSelf);

            for (ClassMetadataLight classMetadataLight : classLightMetadata)
                cml.add(new RemoteClassMetadataLight(classMetadataLight));
            
            return cml;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSubClassesLight"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadataLight> getSubClassesLightNoRecursive(String className, boolean includeAbstractClasses, boolean includeSelf, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getSubClassesLightNoRecursive", "127.0.0.1", sessionId);
            List<RemoteClassMetadataLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classLightMetadata = mem.getSubClassesLightNoRecursive(className, includeAbstractClasses, includeSelf);

            for (ClassMetadataLight classMetadataLight : classLightMetadata)
                cml.add(new RemoteClassMetadataLight(classMetadataLight));

            return cml;
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSubClassesLightNoRecursive"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadata> getAllClasses(boolean includeListTypes, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getAllClasses", "127.0.0.1", sessionId);
            List<RemoteClassMetadata> cml = new ArrayList<>();
            List<ClassMetadata> classMetadataList = mem.getAllClasses(includeListTypes, false);

            for (ClassMetadata classMetadata : classMetadataList)
                cml.add(new RemoteClassMetadata(classMetadata));
            
            return cml;
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getAllClasses"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadataLight> getAllClassesLight(boolean includeListTypes, String sessionId) throws ServerSideException {
         if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getAllClassesLight", "127.0.0.1", sessionId);
            List<RemoteClassMetadataLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classLightMetadata = mem.getAllClassesLight(includeListTypes, false);

            for (ClassMetadataLight classMetadataLight : classLightMetadata)
                cml.add(new RemoteClassMetadataLight(classMetadataLight));
            
            return cml;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getAllClassesLight"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteClass(String className, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteClass", "127.0.0.1", sessionId);
            mem.deleteClass(className);
                        
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_METADATA_OBJECT, 
                String.format("Deleted %s class", className));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteClass"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteClassWithId(long classId, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteClassWithId", "127.0.0.1", sessionId);
            ClassMetadata classMetadata = mem.getClass(classId);
            mem.deleteClass(classId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_METADATA_OBJECT, 
                String.format("Deleted %s class", classMetadata.getName()));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteClassWithId"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadataLight> getPossibleChildren(String parentClassName, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getPossibleChildren", "127.0.0.1", sessionId);
            List<RemoteClassMetadataLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleChildren(parentClassName);

            for (ClassMetadataLight classMetadata : classMetadataList)
                cml.add(new RemoteClassMetadataLight(classMetadata));
            
            return cml;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }  catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getPossibleChildren"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadataLight> getPossibleSpecialChildren(String parentClassName, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getPossibleSpecialChildren", "127.0.0.1", sessionId);
            List<RemoteClassMetadataLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleSpecialChildren(parentClassName);

            for (ClassMetadataLight classMetadata : classMetadataList)
                cml.add(new RemoteClassMetadataLight(classMetadata));
            
            return cml;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getPossibleSpecialChildren"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadataLight> getPossibleChildrenNoRecursive(String parentClassName, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getPossibleChildrenNoRecursive", "127.0.0.1", sessionId);
            List<RemoteClassMetadataLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleChildrenNoRecursive(parentClassName);

            for (ClassMetadataLight classMetadata : classMetadataList)
                cml.add(new RemoteClassMetadataLight(classMetadata));
            
            return cml;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getPossibleChildrenNoRecursive"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadataLight> getPossibleSpecialChildrenNoRecursive(String parentClassName, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getPossibleSpecialChildrenNoRecursive", "127.0.0.1", sessionId);
            List<RemoteClassMetadataLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleSpecialChildrenNoRecursive(parentClassName);

            for (ClassMetadataLight classMetadata : classMetadataList) 
                cml.add(new RemoteClassMetadataLight(classMetadata));

            return cml;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getPossibleSpecialChildrenNoRecursive"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void addPossibleChildrenForClassWithId(long parentClassId, long[] newPossibleChildren, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("addPossibleChildrenForClassWithId", "127.0.0.1", sessionId);
            ClassMetadata classMetadata = parentClassId == -1 ? null : mem.getClass(parentClassId);
            mem.addPossibleChildren(parentClassId, newPossibleChildren);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Added possible children to %s", classMetadata == null ? "Navigation Tree Root" : classMetadata.getName()));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "addPossibleChildrenForClassWithId"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void addPossibleSpecialChildrenWithId(long parentClassId, long[] possibleSpecialChildren, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("addPossibleSpecialChildrenWithId", "127.0.0.1", sessionId);
            ClassMetadata classMetadata = null;
            if (parentClassId != -1)
                classMetadata = mem.getClass(parentClassId);
            mem.addPossibleSpecialChildren(parentClassId, possibleSpecialChildren);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Added possible special children to %s class", classMetadata != null ? classMetadata.getName() : "Navigation Tree Root"));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "addPossibleSpecialChildrenWithId"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void addPossibleChildren(String parentClassName, String[] childrenToBeAdded, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("addPossibleChildren", "127.0.0.1", sessionId);
            mem.addPossibleChildren(parentClassName, childrenToBeAdded);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Added possible children to %s", parentClassName));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "addPossibleChildren"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void addPossibleSpecialChildren(String parentClassName, String[] possibleSpecialChildren, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("addPossibleChildren", "127.0.0.1", sessionId);
            mem.addPossibleSpecialChildren(parentClassName, possibleSpecialChildren);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Added possible special children to %s", parentClassName));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "addPossibleSpecialChildren"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void removePossibleChildrenForClassWithId(long parentClassId, long[] childrenToBeRemoved, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("removePossibleChildrenForClassWithId", "127.0.0.1", sessionId);
            ClassMetadata classMetadata = null;
            if (parentClassId != -1)
                classMetadata = mem.getClass(parentClassId);
            mem.removePossibleChildren(parentClassId, childrenToBeRemoved);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Removed possible children from %s class", classMetadata != null ? classMetadata.getName() : "Navigation Tree Root"));

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "removePossibleChildrenForClassWithId"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void removePossibleSpecialChildren(long parentClassId, long[] specialChildrenToBeRemoved, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("removePossibleSpecialChildren", "127.0.0.1", sessionId);
            ClassMetadata classMetadata = null;
            if (parentClassId != -1)
                classMetadata = mem.getClass(parentClassId);
            
            mem.removePossibleSpecialChildren(parentClassId, specialChildrenToBeRemoved);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Removed possible special children from %s class", classMetadata != null ? classMetadata.getName() : "Navigation Tree Root"));

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "removePossibleSpecialChildren"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadataLight> getUpstreamContainmentHierarchy(String className, boolean recursive, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getUpstreamContainmentHierarchy", "127.0.0.1", sessionId);
            List<RemoteClassMetadataLight> res = new ArrayList<>();
            for (ClassMetadataLight cil : mem.getUpstreamContainmentHierarchy(className, recursive))
                res.add(new RemoteClassMetadataLight(cil));
            
            return res;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getUpstreamContainmentHierarchy"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadataLight> getUpstreamSpecialContainmentHierarchy(String className, boolean recursive, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getUpstreamSpecialContainmentHierarchy", "127.0.0.1", sessionId);
            List<RemoteClassMetadataLight> res = new ArrayList<>();
            for (ClassMetadataLight cil : mem.getUpstreamSpecialContainmentHierarchy(className, recursive))
                res.add(new RemoteClassMetadataLight(cil));
            
            return res;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getUpstreamSpecialContainmentHierarchy"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadataLight> getUpstreamClassHierarchy(String className, boolean includeSelf, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getUpstreamClassHierarchy", "127.0.0.1", sessionId);
            List<RemoteClassMetadataLight> res = new ArrayList<>();
            for (ClassMetadataLight cil : mem.getUpstreamClassHierarchy(className, includeSelf))
                res.add(new RemoteClassMetadataLight(cil));
            
            return res;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getUpstreamClassHierarchy"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public boolean isSubClassOf(String className, String allegedParentClass, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("isSubclassOf", "127.0.0.1", sessionId);
            return mem.isSubclassOf(allegedParentClass, className);
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "isSubClassOf"), ex);
            throw new ServerSideException(ex.getMessage());
        }
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
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createTemplate", "127.0.0.1", sessionId);
            String templateId = aem.createTemplate(templateClass, templateName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created template %s", templateName));
            return templateId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createTemplate"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createTemplateElement(String templateElementClass, String templateElementParentClassName, String templateElementParentId, String templateElementName, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createTemplateElement", "127.0.0.1", sessionId);
            String templateElementId = aem.createTemplateElement(templateElementClass, templateElementParentClassName, 
                    templateElementParentId, templateElementName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Create template element %s [%s] with id %s", templateElementName, templateElementClass, templateElementId));            
            return templateElementId;            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createTemplateElement"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createTemplateSpecialElement(String tsElementClass, String tsElementParentClassName, String tsElementParentId, String tsElementName, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createTemplateElement", "127.0.0.1", sessionId);
            String templateSpecialElementId = aem.createTemplateSpecialElement(tsElementClass, tsElementParentClassName, 
                    tsElementParentId, tsElementName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Create special template element %s [%s] with id %s", tsElementName, tsElementClass, templateSpecialElementId));
            return templateSpecialElementId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createTemplateSpecialElement"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String[] createBulkTemplateElement(String templateElementClassName, String templateElementParentClassName, String templateElementParentId, int numberOfTemplateElements, String templateElementNamePattern, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createBulkTemplateElement", "127.0.0.1", sessionId);
            String[] ids = aem.createBulkTemplateElement(templateElementClassName, templateElementParentClassName, 
                    templateElementParentId, numberOfTemplateElements, templateElementNamePattern);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("%s new templates elements of class %s", numberOfTemplateElements, templateElementClassName));            
            return ids;            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createBulkTemplateElement"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String[] createBulkSpecialTemplateElement(String stElementClass, String stElementParentClassName, String stElementParentId, int numberOfTemplateElements, String stElementNamePattern, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createBulkSpecialTemplateElement", "127.0.0.1", sessionId);
            String[] ids = aem.createBulkSpecialTemplateElement(stElementClass, stElementParentClassName, 
                    stElementParentId, numberOfTemplateElements, stElementNamePattern);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("%s new special templates elements of class %s", numberOfTemplateElements, stElementClass));  
            return ids;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createBulkSpecialTemplateElement"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateTemplateElement(String templateElementClass, String templateElementId, String[] attributeNames, String[] attributeValues, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateTemplateElement", "127.0.0.1", sessionId);
            ChangeDescriptor changeDescriptor = aem.updateTemplateElement(templateElementClass, templateElementId, attributeNames, attributeValues);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT,
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateTemplateElement"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteTemplateElement(String templateElementClass, String templateElementId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteTemplateElement", "127.0.0.1", sessionId);
            ChangeDescriptor changeDescriptor = aem.deleteTemplateElement(templateElementClass, templateElementId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT,
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteTemplateElement"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getTemplatesForClass(String className, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getTemplatesForClass", "127.0.0.1", sessionId);
            List<TemplateObjectLight> templates = aem.getTemplatesForClass(className);
            List<RemoteObjectLight> remoteTemplates = new ArrayList<>();
            
            for (TemplateObjectLight template : templates)
                remoteTemplates.add(new RemoteObjectLight(template.getClassName(), template.getId(), template.getName()));
                        
            return remoteTemplates;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getTemplatesForClass"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String[] copyTemplateElements(String[] sourceObjectsClassNames, String[] sourceObjectsIds, String newParentClassName, String newParentId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("copyTemplateElements", "127.0.0.1", sessionId);
            String [] templateElementsIds = aem.copyTemplateElements(sourceObjectsClassNames, sourceObjectsIds, newParentClassName, newParentId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT,
                String.format("%s template elements copied", templateElementsIds.length));
            return templateElementsIds;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "copyTemplateElements"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String[] copyTemplateSpecialElements(String[] sourceObjectsClassNames, String[] sourceObjectsIds, String newParentClassName, String newParentId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("copyTemplateSpecialElements", "127.0.0.1", sessionId);
            String[] templateSpecialElements = aem.copyTemplateSpecialElement(sourceObjectsClassNames, sourceObjectsIds, newParentClassName, newParentId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT,
                String.format("Copied %s template special elements", templateSpecialElements.length));
            return templateSpecialElements;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "copyTemplateSpecialElements"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getTemplateElementChildren(String templateElementClass, String templateElementId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getTemplateElementChildren", "127.0.0.1", sessionId);
            List<TemplateObjectLight> templateElementChildren = aem.getTemplateElementChildren(templateElementClass, templateElementId);
            List<RemoteObjectLight> remoteTemplateElementChildren = new ArrayList<>();
            
            for (TemplateObjectLight templateElementChild : templateElementChildren)
                remoteTemplateElementChildren.add(new RemoteObjectLight(templateElementChild.getClassName(), 
                    templateElementChild.getId(), templateElementChild.getName()));
            
            return remoteTemplateElementChildren;
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getTemplateElementChildren"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getTemplateSpecialElementChildren(String tsElementClass, String tsElementId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getTemplateSpecialElementChildren", "127.0.0.1", sessionId);
            List<TemplateObjectLight> templateElementChildren = aem.getTemplateSpecialElementChildren(tsElementClass, tsElementId);
            List<RemoteObjectLight> remoteTemplateElementChildren = new ArrayList<>();
            
            for (TemplateObjectLight templateElementChild : templateElementChildren)
                remoteTemplateElementChildren.add(new RemoteObjectLight(templateElementChild.getClassName(), 
                    templateElementChild.getId(), templateElementChild.getName()));
            
            return remoteTemplateElementChildren;
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getTemplateSpecialElementChildren"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObject getTemplateElement(String templateElementClass, String templateElementId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getTemplateElement", "127.0.0.1", sessionId);
            return new RemoteObject(aem.getTemplateElement(templateElementClass, templateElementId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getTemplateElement"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createClassLevelReport(String className, String reportName, String reportDescription, String script, int outputType, boolean enabled, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createClassLevelReport", "127.0.0.1", sessionId);
            long reportId = bem.createClassLevelReport(className, reportName, reportDescription, script, outputType, enabled);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created class level report %s", reportName));
            return reportId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createClassLevelReport"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createInventoryLevelReport(String reportName, String reportDescription, String script, int outputType, boolean enabled, List<StringPair> parameters, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createInventoryLevelReport", "127.0.0.1", sessionId);
            long reportId = bem.createInventoryLevelReport(reportName, reportDescription, script, outputType, enabled, parameters);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created inventory level report %s", reportName));
            return reportId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createInventoryLevelReport"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteReport(long reportId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteReport", "127.0.0.1", sessionId);
            ChangeDescriptor changeDescriptor = bem.deleteReport(reportId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteReport"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateReport(long reportId, String reportName, String reportDescription, Boolean enabled, Integer type, String script, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateReport", "127.0.0.1", sessionId);
            ChangeDescriptor changeDescriptor = bem.updateReport(reportId, reportName, reportDescription, enabled, type, script);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateReport"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateReportParameters(long reportId, List<StringPair> parameters, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateReportParameters", "127.0.0.1", sessionId);
            ChangeDescriptor changeDescriptor = bem.updateReportParameters(reportId, parameters);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateReportParameters"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteReportMetadataLight> getClassLevelReports(String className, boolean recursive, boolean includeDisabled, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getClassLevelReports", "127.0.0.1", sessionId);
            List<ReportMetadataLight> reportMetadataLights = bem.getClassLevelReports(className, recursive, includeDisabled);
             return reportMetadataLights.stream().map(reportMetadataLight -> new RemoteReportMetadataLight(reportMetadataLight)).collect(Collectors.toList());
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateReportParameters"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteReportMetadataLight> getInventoryLevelReports(boolean includeDisabled, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getInventoryLevelReports", "127.0.0.1", sessionId);
            List<ReportMetadataLight> reportMetadataLights = bem.getInventoryLevelReports(includeDisabled);
            return reportMetadataLights.stream().map(reportMetadataLight -> new RemoteReportMetadataLight(reportMetadataLight)).collect(Collectors.toList());
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getInventoryLevelReports"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteReportMetadata getReport(long reportId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getReport", "127.0.0.1", sessionId);
            return new RemoteReportMetadata(bem.getReport(reportId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getInventoryLevelReports"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public byte[] executeClassLevelReport(String objectClassName, String objectId, long reportId, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("executeClassLevelReport", "127.0.0.1", sessionId);
            return bem.executeClassLevelReport(objectClassName, objectId, reportId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "executeClassLevelReport"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public byte[] executeInventoryLevelReport(long reportId, List<StringPair> parameters, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("executeInventoryLevelReport", "127.0.0.1", sessionId);
            return bem.executeInventoryLevelReport(reportId, parameters);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "executeInventoryLevelReport"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createFavoritesFolderForUser(String favoritesFolderName, long userId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createFavoritesFolderForUser", "127.0.0.1", sessionId);
            long favoritesFolderId = aem.createFavoritesFolderForUser(favoritesFolderName, userId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Create Favorites Folder %s For User %s", favoritesFolderName, getUserNameFromSession(sessionId)));
            return favoritesFolderId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createFavoritesFolderForUser"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteFavoritesFolders(long[] favoritesFolderId, long userId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteFavoritesFolders ", "127.0.0.1", sessionId);
            
            aem.deleteFavoritesFolders (favoritesFolderId, userId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("Deleted %s Favorites Folders", favoritesFolderId.length));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteFavoritesFolders"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteFavoritesFolder> getFavoritesFoldersForUser(long userId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getFavoritesFoldersForUser", "127.0.0.1", sessionId);
            
            List<RemoteFavoritesFolder> remoteBookmarks = new ArrayList();
            List<FavoritesFolder> favoritesFolders = aem.getFavoritesFoldersForUser(userId);
            
            for (FavoritesFolder favoritesFolder : favoritesFolders)
                remoteBookmarks.add(new RemoteFavoritesFolder(favoritesFolder));
            
            return remoteBookmarks;
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getFavoritesFoldersForUser"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void addObjectsToFavoritesFolder(String[] objectClass, String[] objectId, long favoritesFolderId, long userId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        if (objectClass.length != objectId.length)
            throw new ServerSideException("The arrays provided have different lengths");
        
        try {
            aem.validateCall("addObjectsToFavoritesFolder", "127.0.0.1", sessionId);
            
            for (int i = 0; i < objectId.length; i += 1)
                aem.addObjectTofavoritesFolder(objectClass[i], objectId[i], favoritesFolderId, userId);
            
            String favoritesFolderName = aem.getFavoritesFolder(favoritesFolderId, userId).getName();
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                String.format("Added %s objects to favorites folder %s", objectId.length, favoritesFolderName));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "addObjectsToFavoritesFolder"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void removeObjectsFromFavoritesFolder(String[] objectClass, String[] objectId, long favoritesFolderId, long userId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        if (objectClass.length != objectId.length)
            throw new ServerSideException("The arrays provided have different lengths");
        
        try {
            aem.validateCall("removeObjectsFromFavoritesFolder", "127.0.0.1", sessionId);
            
            for (int i = 0; i < objectId.length; i += 1)
                aem.removeObjectFromfavoritesFolder(objectClass[i], objectId[i], favoritesFolderId, userId);
            
            String favoritesFolderName = aem.getFavoritesFolder(favoritesFolderId, userId).getName();
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                String.format("Remove %s objects from favorites folder %s", objectId.length, favoritesFolderName));                        
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "removeObjectsFromFavoritesFolder"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getObjectsInFavoritesFolder(long favoritesFolderId, long userId, int limit, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getObjectsInFavoritesFolder", "127.0.0.1", sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(aem.getObjectsInFavoritesFolder(favoritesFolderId, userId, limit));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getObjectsInFavoritesFolder"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteFavoritesFolder> getFavoritesFoldersForObject(long userId, String objectClass, String objectId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("getFavoritesFoldersForObject", "127.0.0.1", sessionId);
            
            List<RemoteFavoritesFolder> remoteBookmarks = new ArrayList();
            List<FavoritesFolder> favoritesFolders = aem.getFavoritesFoldersForObject(userId, objectClass, objectId);
            
            for (FavoritesFolder favoritesFolder : favoritesFolders)
                remoteBookmarks.add(new RemoteFavoritesFolder(favoritesFolder));
            
            return remoteBookmarks;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());            
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getFavoritesFoldersForObject"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteFavoritesFolder getFavoritesFolder(long favoritesFolderId, long userId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getFavoritesFolder", "127.0.0.1", sessionId);
            
            return new RemoteFavoritesFolder(aem.getFavoritesFolder(favoritesFolderId, userId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getFavoritesFolder"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateFavoritesFolder(long favoritesFolderId, String favoritesFolderName, long userId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateFavoritesFolder", "127.0.0.1", sessionId);
            
            String oldFavoritesFolderName = aem.getFavoritesFolder(favoritesFolderId, userId).getName();
            aem.updateFavoritesFolder(favoritesFolderId, userId, favoritesFolderName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT,
                new ChangeDescriptor(Constants.PROPERTY_NAME, oldFavoritesFolderName, favoritesFolderName, "Updated favorites folder"));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateFavoritesFolder"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createBusinessRule(String ruleName, String ruleDescription, int ruleType, int ruleScope, String appliesTo, String ruleVersion, List<String> constraints, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createBusinessRule", "127.0.0.1", sessionId);
            long businessRuleId = aem.createBusinessRule(ruleName, ruleDescription, ruleType, ruleScope, appliesTo, ruleVersion, constraints);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created business rule %s", ruleName));
            return businessRuleId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createBusinessRule"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteBusinessRule(long businessRuleId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteBusinessRule", "127.0.0.1", sessionId);
            aem.deleteBusinessRule(businessRuleId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("Deleted business rule %s", businessRuleId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteBusinessRule"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteBusinessRule> getBusinessRules(int type, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getBusinessRules", "127.0.0.1", sessionId);
            List<BusinessRule> businessRules = aem.getBusinessRules(type);
            
            List<RemoteBusinessRule> res = new ArrayList<>();
            
            for (BusinessRule businessRule : businessRules) {
                RemoteBusinessRule remoteBusinessRule = new RemoteBusinessRule(businessRule.getRuleId(), businessRule.getName(), businessRule.getDescription(), 
                        businessRule.getAppliesTo(), businessRule.getType(), businessRule.getScope(), businessRule.getVersion());
                
                for (BusinessRuleConstraint constraint : businessRule.getConstraints())
                    remoteBusinessRule.getConstraints().add(new RemoteBusinessRuleConstraint(constraint.getName(), constraint.getDefinition()));
                
                res.add(remoteBusinessRule);
            }
            return res;
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getBusinessRules"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteSyncFinding> launchSupervisedSynchronizationTask(long syncGroupId, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("launchSupervisedSynchronizationTask", "127.0.0.1", sessionId);
            Properties parameters = new Properties();
            parameters.put("syncGroupId", Long.toString(syncGroupId)); //NOI18N  
            parameters.put("sessionId", sessionId); //NOI18N  
            
            //groundJob backgroundJob = new BackgroundJob("DefaultSyncJob", false, parameters); //NOI18N
            //JobManager.getInstance().launch(backgroundJob);
            //return backgroundJob;
            return null;
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "launchSupervisedSynchronizationTask"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteSyncResult> launchAutomatedSynchronizationTask(long syncGroupId, String providersName, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("launchAutomatedSynchronizationTask", "127.0.0.1", sessionId);
            Properties parameters = new Properties();
            parameters.put("syncGroupId", Long.toString(syncGroupId)); //NOI18N
            
            //BackgroundJob backgroundJob = new BackgroundJob("DefaultSyncJob", false, parameters); //NOI18N
            //JobManager.getInstance().launch(backgroundJob);
            //return backgroundJob;
            return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "launchAutomatedSynchronizationTask"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteSyncResult> launchAdHocAutomatedSynchronizationTask(List<Long> synDsConfigIds, String providersName, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("launchAdHocAutomatedSynchronizationTask", "127.0.0.1", sessionId);
            Properties parameters = new Properties();
            
            parameters.put("sessionId", sessionId); //NOI18N
            parameters.put("provider", providersName); //NOI18N  
            String syncDSConfigIds= "";
            for (long syncDataSourceConfigId : synDsConfigIds) 
                syncDSConfigIds += Long.toString(syncDataSourceConfigId) +";";
            
            parameters.put("dataSourceConfigIds", syncDSConfigIds); //NOI18N  
            
            //BackgroundJob backgroundJob = new BackgroundJob("DefaultSyncJob", false, parameters); //NOI18N
            //JobManager.getInstance().launch(backgroundJob);
            //return backgroundJob;
            return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }  catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "launchAdHocAutomatedSynchronizationTask"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteSyncResult> executeSyncActions(long syncGroupId, List<RemoteSyncAction> actions, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("executeSyncActions", "127.0.0.1", sessionId);
            //SynchronizationGroup syncGroup = PersistenceService.getInstance().getApplicationEntityManager().getSyncGroup(syncGroupId);
            return null; //syncGroup.getProvider().finalize(actions);
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "executeSyncActions"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteBackgroundJob> getCurrentJobs(String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("getCurrentJobs", "127.0.0.1", sessionId);
        
            List<RemoteBackgroundJob> result = new ArrayList();
//            for (BackgroundJob job : JobManager.getInstance().getCurrentJobs()) {
//                
//                if (job.getStatus().equals(BackgroundJob.JOB_STATUS.RUNNNING)) {
//                    result.add(new RemoteBackgroundJob(
//                        job.getId(), job.getJobTag(), job.getProgress(), 
//                        job.allowConcurrence(), job.getStatus().toString(), 
//                        job.getStartTime(), job.getEndTime()));
//                }
//            }
            return result;
            
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getCurrentJobs"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void killJob(long jobId, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("killJob", "127.0.0.1", sessionId);
        
            //JobManager.getInstance().kill(jobId);
            
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "killJob"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createSDHTransportLink(String classNameEndpointA, String idEndpointA, String classNameEndpointB, String idEndpointB, String linkType, String defaultName, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("createSDHTransportLink", "127.0.0.1", sessionId);
            String SDHTransportLinkId = modSdh.createSDHTransportLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, linkType, defaultName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("Created SDH Transport Link %s [%s]", defaultName, linkType));
            return SDHTransportLinkId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createSDHTransportLink"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createSDHContainerLink(String classNameEndpointA, String idEndpointA, String classNameEndpointB, String idEndpointB, String linkType, List<RemoteSDHPosition> positions, String defaultName, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("createSDHContainerLink", "127.0.0.1", sessionId);
            List<SDHPosition> remotePositions = new ArrayList<>();
            for (RemoteSDHPosition position : positions)
                remotePositions.add(new SDHPosition(position.getLinkClass(), position.getLinkId(), position.getPosition()));
            
            String SDHContainerLinkId = modSdh.createSDHContainerLink(classNameEndpointA, idEndpointA, 
                    classNameEndpointB, idEndpointB, linkType, remotePositions, defaultName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("Created SDH Container Link %s [%s]", defaultName, linkType));
            return SDHContainerLinkId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createSDHContainerLink"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createSDHTributaryLink(String classNameEndpointA, String idEndpointA, String classNameEndpointB, String idEndpointB, String linkType, List<RemoteSDHPosition> positions, String defaultName, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("createSDHTributaryLink", "127.0.0.1", sessionId);
            
            List<SDHPosition> remotePositions = new ArrayList<>();
            
            for (RemoteSDHPosition position : positions)
                remotePositions.add(new SDHPosition(position.getLinkClass(), position.getLinkId(), position.getPosition()));
            
            String SDHTributaryLinkId = modSdh.createSDHTributaryLink(classNameEndpointA, idEndpointA, classNameEndpointB, 
                    idEndpointB, linkType, remotePositions, defaultName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("Created SDH Tributary Link %s [%s]", defaultName, linkType));
            return SDHTributaryLinkId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createSDHTributaryLink"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteSDHTransportLink(String transportLinkClass, String transportLinkId, boolean forceDelete, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("deleteSDHTransportLink", "127.0.0.1", sessionId);
            
            String transportLinkName = bem.getObject(transportLinkClass, transportLinkId).getName();
            modSdh.deleteSDHTransportLink(transportLinkClass, transportLinkId, forceDelete);
                                    
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                String.format("Deleted SDH Transport Link %s [%s]", transportLinkName, transportLinkClass));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteSDHTransportLink"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteSDHContainerLink(String containerLinkClass, String containerLinkId, boolean forceDelete, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("deleteSDHContainerLink", "127.0.0.1", sessionId);
            
            String containerLinkName = bem.getObject(containerLinkClass, containerLinkId).getName();
            modSdh.deleteSDHContainerLink(containerLinkClass, containerLinkId, forceDelete);
                        
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                String.format("Deleted SDH Container Link %s [%s]", containerLinkName, containerLinkClass));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteSDHContainerLink"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteSDHTributaryLink(String tributaryLinkClass, String tributaryLinkId, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("deleteSDHTributaryLink", "127.0.0.1", sessionId);
            
            String tributaryLinkName = bem.getObject(tributaryLinkClass, tributaryLinkId).getName();
            modSdh.deleteSDHTributaryLink(tributaryLinkClass, tributaryLinkId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                String.format("Deleted SDH Tributary Link %s [%s]", tributaryLinkName, tributaryLinkClass));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteSDHTributaryLink"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLightList> findSDHRoutesUsingTransportLinks(String communicationsEquipmentClassA, String communicationsEquipmentIdA, String communicationsEquipmentClassB, String communicationsEquipmentIB, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("findSDHRoutesUsingTransportLinks", "127.0.0.1", sessionId);
            List<RemoteObjectLightList> res  = new ArrayList<>();
            
            List<BusinessObjectLightList> routes = modSdh.findSDHRoutesUsingTransportLinks(communicationsEquipmentClassA, communicationsEquipmentIdA, communicationsEquipmentClassB, communicationsEquipmentIB);
            for (BusinessObjectLightList route : routes)
                res.add(new RemoteObjectLightList(route));
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "findSDHRoutesUsingTransportLinks"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLightList> findSDHRoutesUsingContainerLinks(String communicationsEquipmentClassA, String communicationsEquipmentIdA, String communicationsEquipmentClassB, String communicationsEquipmentIB, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("findSDHRoutesUsingContainerLinks", "127.0.0.1", sessionId);
            List<RemoteObjectLightList> res  = new ArrayList<>();
            List<BusinessObjectLightList> routes = modSdh.findSDHRoutesUsingContainerLinks(communicationsEquipmentClassA, communicationsEquipmentIdA, communicationsEquipmentClassB, communicationsEquipmentIB);
            for (BusinessObjectLightList route : routes)
                res.add(new RemoteObjectLightList(route));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "findSDHRoutesUsingContainerLinks"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteSDHContainerLinkDefinition> getSDHTransportLinkStructure(String transportLinkClass, String transportLinkId, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("getSDHTransportLinkStructure", "127.0.0.1", sessionId);
            List<SDHContainerLinkDefinition> containerLinks = modSdh.getSDHTransportLinkStructure(transportLinkClass, transportLinkId);
            
            List<RemoteSDHContainerLinkDefinition> res = new ArrayList<>();
            
            for (SDHContainerLinkDefinition containerLink : containerLinks)
                res.add(new RemoteSDHContainerLinkDefinition(containerLink));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSDHTransportLinkStructure"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteSDHContainerLinkDefinition> getSDHContainerLinkStructure(String containerLinkClass, String containerLinkId, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("getSDHContainerLinkStructure", "127.0.0.1", sessionId);
            List<SDHContainerLinkDefinition> containerLinks = modSdh.getSDHContainerLinkStructure(containerLinkClass, containerLinkId);
            List<RemoteSDHContainerLinkDefinition> res = new ArrayList<>();
            
            for (SDHContainerLinkDefinition containerLink : containerLinks)
                res.add(new RemoteSDHContainerLinkDefinition(containerLink));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSDHContainerLinkStructure"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemotePool[] getSubnetPools(String parentId, String className, String sessionId) throws ServerSideException {
        try {
//            aem.validateCall("getSubnetPools", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            return RemotePool.toRemotePoolArray(ipamModule.getSubnetPools(parentId, className));
            return null;
//        } catch (InventoryException ex) {
//            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSDHContainerLinkStructure"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getSubnets(String poolId, int limit, String sessionId) throws ServerSideException {
        try {
//            aem.validateCall("getSubnets", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            return RemoteObjectLight.toRemoteObjectLightArray(ipamModule.getSubnets(limit, poolId));
            return null;
//        } catch (InventoryException ex) {
//            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSubnets"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createSubnetPool(String parentId, String subnetPoolName, String subnetPoolDescription, String className, String sessionId) throws ServerSideException {
        try {
//            aem.validateCall("createSubnetPool", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            String subnetPoolId = ipamModule.createSubnetsPool(parentId, subnetPoolName, subnetPoolDescription, className);
            return null;
//            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
//                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
//                String.format("Created Subnet Pool %s [%s]", subnetPoolName, className));
//            
//            return subnetPoolId;            
//        } catch (InventoryException ex) {
//            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createSubnetPool"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createSubnet(String poolId, String className, List<StringPair> attributes, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("createSubnet", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            
//            String[] attributeNames = new String[attributes.size()];
//            String[] attributeValues = new String[attributes.size()];
//            
//            for (int i = 0; i < attributes.size(); i++) {
//                attributeNames[i] = attributes.get(i).getKey();
//                attributeValues[i] = attributes.get(i).getValue();
//            }
//                
//            String subnetId = ipamModule.createSubnet(id, className, attributeNames, attributeValues);
//            
//            String subnameName = bem.getObjectLight(className, subnetId).getName();
//            
//            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
//                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
//                String.format("Created subnet %s", subnameName));
//            return subnetId;
            return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createSubnet"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteSubnetPools(String[] ids, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("deleteSubnetPools", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            ipamModule.deleteSubnetPools(ids);
//            
//            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
//                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
//                String.format("Deleted %s subnet pools", ids.length));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteSubnetPools"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteSubnets(String className, List<String> oids, boolean releaseRelationships, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("deleteSubnets", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            ipamModule.deleteSubnets(className, ids, releaseRelationships);
//            
//            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
//                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
//                String.format("Deleted %s subnets", ids.size()));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteSubnets"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObject getSubnet(String id, String className, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("getSubnet", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            return new RemoteObject(ipamModule.getSubnet(className, id));
            return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSubnet"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemotePool getSubnetPool(String subnetPoolId, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("getSubnetPool", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            return ipamModule.getSubnetPool(subnetPoolId);
            return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSubnetPool"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String addIPAddress(String id, String parentClassName, List<StringPair> attributesToBeUpdated, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("addIPAddress", "127.0.0.1", sessionId);
//            HashMap<String, String> attributes = new HashMap<>();
//            
//            for (StringPair attribute : attributesToBeUpdated)
//                attributes.put(attribute.getKey(), attribute.getValue());
//            
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            String ipAddressId = ipamModule.addIPAddress(id, parentClassName, attributes);
//                        
//            String ipAddressName = bem.getObjectLight(Constants.CLASS_IP_ADDRESS, ipAddressId).getName();
//            
//            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
//                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
//                String.format("Created IP Address %s", ipAddressName));
//            return ipAddressId;
            return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "addIPAddress"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void removeIP(String[] oids, boolean releaseRelationships, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("removeIP", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N            
//            ipamModule.removeIP(ids, releaseRelationships);
//            
//            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
//                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
//                String.format("Removed %s IP Addresses", ids.length));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "removeIP"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getSubnetUsedIps(String id, int limit, String className, String sessionId) throws ServerSideException {
        try{
            aem.validateCall("getSubnetUsedIps", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            return RemoteObjectLight.toRemoteObjectLightArray(ipamModule.getSubnetUsedIps(id, className));
            return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSubnetUsedIps"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getSubnetsInSubnet(String id, int limit, String className, String sessionId) throws ServerSideException {
        try{
            aem.validateCall("getSubnetsInSubnet", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            return RemoteObjectLight.toRemoteObjectLightArray(ipamModule.getSubnetsInSubnet(id, className));
            return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSubnetsInSubnet"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void relateSubnetToVlan(String id, String className, String vlanId, String sessionId) throws ServerSideException {
        try{
            aem.validateCall("relateSubnetToVLAN", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            ipamModule.relateSubnetToVLAN(id, className, vlanId);
//            
//            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), Constants.CLASS_VLAN, vlanId, 
//                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
//                IPAMModule.RELATIONSHIP_IPAMBELONGSTOVLAN, "", id, "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "relateSubnetToVlan"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void releaseSubnetFromVlan(String subnetId, String vlanId, String sessionId) throws ServerSideException {
        try{
            aem.validateCall("releaseSubnetFromVLAN", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            ipamModule.releaseSubnetFromVLAN(vlanId, id);
//            
//            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), Constants.CLASS_VLAN, vlanId, 
//                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
//                IPAMModule.RELATIONSHIP_IPAMBELONGSTOVLAN, id, "", "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "releaseSubnetFromVlan"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void releaseSubnetFromVRF(String subnetId, String vrfId, String sessionId) throws ServerSideException {
        try{
            aem.validateCall("releaseSubnetFromVRF", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            ipamModule.releaseSubnetFromVRF(subnetId, vrfId);
//            
//            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), Constants.CLASS_VRF_INSTANCE, vrfId, 
//                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
//                IPAMModule.RELATIONSHIP_IPAMBELONGSTOVRFINSTACE, subnetId, "", "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "releaseSubnetFromVRF"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void relateSubnetToVrf(String id, String className, String vrfId, String sessionId) throws ServerSideException {
        try{
            aem.validateCall("relateSubnetToVRF", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            ipamModule.relateSubnetToVRF(id, className, vrfId);
//            
//            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), Constants.CLASS_VRF_INSTANCE, vrfId, 
//                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
//                IPAMModule.RELATIONSHIP_IPAMBELONGSTOVRFINSTACE, "", id, "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "relateSubnetToVrf"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void relateIPtoPort(String id, String portClassName, String portId, String sessionId) throws ServerSideException {
        try{
            aem.validateCall("relateIPtoDevice", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            ipamModule.relateIPtoPort(ipId, portClassName, portId);
//            
//            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), portClassName, portId,
//                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
//                IPAMModule.RELATIONSHIP_IPAMHASADDRESS, "", ipId, "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "relateIPtoPort"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public boolean itOverlaps(String networkIp, String broadcastIp, String sessionId) throws ServerSideException {
        try{
            aem.validateCall("itOverlaps", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            ipamModule.itOverlaps(networkIp, broadcastIp);
            return false;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "itOverlaps"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void releasePortFromIP(String deviceClassName, String deviceId, String id, String sessionId) throws ServerSideException {
        try{
            aem.validateCall("releasePortFromIP", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            ipamModule.releasePortFromIP(deviceClassName, deviceId, id);
//            
//            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), deviceClassName, deviceId,
//                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
//                IPAMModule.RELATIONSHIP_IPAMHASADDRESS, id, "", "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "releasePortFromIP"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void associateObjectsToContract(String[] objectClass, String[] objectId, String contractClass, String contractId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        if (objectClass.length != objectId.length)
            throw new ServerSideException("The arrays provided have different lengths");
        
        try {
            aem.validateCall("associateObjectsToContract", "127.0.0.1", sessionId);
            if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, contractClass))
                throw new ServerSideException(String.format("Class %s is not a contract", contractClass));
            
            boolean allEquipmentANetworkElement = true;
            
            for (int i = 0; i < objectId.length; i++) {
                if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, objectClass[i]))
                    allEquipmentANetworkElement = false;
                else
                    bem.createSpecialRelationship(objectClass[i], objectId[i], contractClass, contractId, "contractHas", true); //NOI18N
            }
            String contractName = bem.getObjectLight(contractClass, contractId).getName();
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                String.format("Associated %s objects to contract %s [%s]", objectId.length, contractName, contractClass));
            
            if (!allEquipmentANetworkElement)
                throw new InvalidArgumentException("All non-inventory elements were ignored");
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "associateObjectsToContract"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void releaseObjectFromContract(String objectClass, String objectId, String contractId, String sessionId) throws ServerSideException {
         if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("releaseObjectFromContract", "127.0.0.1", sessionId);
            bem.releaseSpecialRelationship(objectClass, objectId, contractId, "contractHas"); //NOI18N
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), 
                objectClass, objectId, ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                "contractHas", objectId, "", ""); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "releaseObjectFromContract"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createMPLSLink(String classNameEndpointA, String idEndpointA, String classNameEndpointB, String idEndpointB, List<StringPair> attributesToBeSet, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("createMPLSLink", "127.0.0.1", sessionId);
            
//            MPLSModule mplsModule = (MPLSModule)aem.getCommercialModule("MPLS Networks Module"); //NOI18N MPLS Networks Module
//            HashMap<String, String> attributes = new HashMap<>();
//            
//            for (StringPair attribute : attributesToBeSet)
//                attributes.put(attribute.getKey(), attribute.getValue());
//            
//            String MPLSLinkId = mplsModule.createMPLSLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, attributes, getUserNameFromSession(sessionId));
//            return MPLSLinkId;
            return null;
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createMPLSLink"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteMPLSConnectionDetails getMPLSLinkEndpoints(String connectionId, String sessionId) throws ServerSideException {
        try{
            aem.validateCall("getMPLSLinkEndpoints", "127.0.0.1", sessionId);
//            MPLSModule mplsModule = (MPLSModule)aem.getCommercialModule("MPLS Networks Module"); //NOI18N
//            MPLSConnectionDefinition mplsLinkEndpoints = mplsModule.getMPLSLinkDetails(connectionId);
            
//            RemoteMPLSConnectionDetails remoteMPLSConnectionDetails = new RemoteMPLSConnectionDetails(mplsLinkEndpoints);
//            
//            return remoteMPLSConnectionDetails;
            return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getMPLSLinkEndpoints"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void connectMplsLink(String[] sideAClassNames, String[] sideAIds, String[] linksIds, String[] sideBClassNames, String[] sideBIds, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("connectMplsLink", "127.0.0.1", sessionId);
//            MPLSModule mplsModule = (MPLSModule)aem.getCommercialModule("MPLS Networks Module"); //NOI18N
//            mplsModule.connectMplsLink(sideAClassNames, sideAIds, linksIds, sideBClassNames, sideBIds, getUserNameFromSession(sessionId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "connectMplsLink"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void disconnectMPLSLink(String connectionId, int sideToDisconnect, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("disconnectMPLSLink", "127.0.0.1", sessionId);
//            MPLSModule mplsModule = (MPLSModule)aem.getCommercialModule("MPLS Networks Module"); //NOI18N
//            mplsModule.disconnectMPLSLink(connectionId, sideToDisconnect, getUserNameFromSession(sessionId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "disconnectMPLSLink"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteMPLSLink(String linkId, boolean forceDelete, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("deleteMPLSLink", "127.0.0.1", sessionId);
//            MPLSModule mplsModule = (MPLSModule)aem.getCommercialModule("MPLS Networks Module"); //NOI18N
//            mplsModule.deleteMPLSLink(linkId, forceDelete, getUserNameFromSession(sessionId));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteMPLSLink"), ex);
            throw new ServerSideException(ex.getMessage());
        }   
    }

    @Override
    public void relatePortToInterface(String portId, String portClassName, String interfaceClassName, String interfaceId, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("relatePortToInterface", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            ipamModule.relatePortToInterface(portId, portClassName, interfaceClassName, interfaceId);
//            
//            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), interfaceClassName, interfaceId, 
//                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
//                IPAMModule.RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE, "", portId, "");            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "relatePortToInterface"), ex);
            throw new ServerSideException(ex.getMessage());
        }  
    }

    @Override
    public void releasePortFromInterface(String interfaceClassName, String interfaceId, String portId, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("releasePortFromInterface", "127.0.0.1", sessionId);
//            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
//            ipamModule.releasePortFromInterface(interfaceClassName, interfaceId, portId);
//            
//            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), interfaceClassName, interfaceId, 
//                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
//                IPAMModule.RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE, portId, "", ""); 
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "releasePortFromInterface"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemotePool> getProjectPools(String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getProjectPools", "127.0.0.1", sessionId);
//            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N
//            return projectsModule.getProjectPools();
            return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getProjectPools"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String addProject(String parentId, String parentClassName, String className, String[] attributeNames, String[] attributeValues, String sessionId) throws ServerSideException {
       try {
            aem.validateCall("addProject", "127.0.0.1", sessionId);
//            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N
//            
//            String projectId = projectsModule.addProject(parentId, parentClassName, className, attributeNames, attributeValues);
//            String projectName = bem.getObjectLight(className, projectId).getName();
//            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
//                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
//                String.format("Created Project %s [%s]", projectName, className));
            
//            return projectId;
            return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "addProject"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteProject(String className, String oid, boolean releaseRelationships, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            String projectName = bem.getObjectLight(className, oid).getName();
            aem.validateCall("deleteProject", "127.0.0.1", sessionId);
            
//            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N
//            projectsModule.deleteProject(className, oid, releaseRelationships);
//            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
//                ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
//                String.format("Deleted Project %s [%s]", projectName, className));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteProject"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String addActivity(String parentId, String parentClassName, String className, String[] attributeNames, String[] attributeValues, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("addActivity", "127.0.0.1", sessionId);
//            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N
//            
//            String activityId = projectsModule.addActivity(parentId, parentClassName, className, attributeNames, attributeValues);
//            String activityName = bem.getObjectLight(className, activityId).getName();
//            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
//                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
//                String.format("Created Activity %s [%s]", activityName, className));
//            
//            return activityId;
            return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "addActivity"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteActivity(String className, String oid, boolean releaseRelationships, String sessionId) throws ServerSideException {
         if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("deleteActivity", "127.0.0.1", sessionId);
            String activityName = bem.getObjectLight(className, oid).getName();
//            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
//            projectsModule.deleteActivity(className, oid, releaseRelationships);
//            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
//                ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
//                String.format("Deleted Activity %s [%s]", activityName, className));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteActivity"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getProjectsInProjectPool(String poolId, int limit, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("getProjectsInProjectPool", "127.0.0.1", sessionId);
            
//            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
//            return RemoteObjectLight.toRemoteObjectLightArray(projectsModule.getProjectsInProjectPool(poolId, limit));
            return null;        
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getProjectsInProjectPool"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getProjectResurces(String projectClass, String projectId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("getProjectResurces", "127.0.0.1", sessionId);
            
//            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
//            return RemoteObjectLight.toRemoteObjectLightArray(projectsModule.getProjectResurces(projectClass, projectId));
            return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getProjectResurces"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getProjectActivities(String projectClass, String projectId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("associateObjectsToProject", "127.0.0.1", sessionId);
            
//            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N
//            projectsModule.associateObjectsToProject(projectClass, projectId, objectClass, objectId);               
//            
//            String projectName = bem.getObjectLight(projectClass, projectId).getClassName();
//            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
//                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
//                String.format("Associated %s objects to project %s [%s]", objectId.length, projectName, projectClass));
              return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getProjectActivities"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void associateObjectsToProject(String projectClass, String projectId, String[] objectClass, String[] objectId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("associateObjectsToProject", "127.0.0.1", sessionId);
//            
//            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N
//            projectsModule.associateObjectsToProject(projectClass, projectId, objectClass, objectId);               
//            
//            String projectName = bem.getObjectLight(projectClass, projectId).getClassName();
//            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
//                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
//                String.format("Associated %s objects to project %s [%s]", objectId.length, projectName, projectClass));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "associateObjectsToProject"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void associateObjectToProject(String projectClass, String projectId, String objectClass, String objectId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("associateObjectToProject", "127.0.0.1", sessionId);
            
//            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
//            projectsModule.associateObjectToProject(projectClass, projectId, objectClass, objectId);
//            
//            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), 
//                objectClass, objectId,
//                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
//                ProjectsModule.RELATIONSHIP_PROJECTSPROJECTUSES, "", projectId, "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "associateObjectToProject"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void freeObjectFromProject(String objectClass, String objectId, String projectClass, String projectId, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RemoteObjectLight> getProjectsAssociateToObject(String objectClass, String objectId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("getProjectsAssociateToObject", "127.0.0.1", sessionId);
            
//            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
//            return RemoteObjectLight.toRemoteObjectLightArray(projectsModule.getProjectsAssociateToObject(objectClass, objectId));
            return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getProjectsAssociateToObject"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createProjectPool(String name, String description, String instanceOfClass, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            aem.validateCall("createProjectPool", "127.0.0.1", sessionId);
            
//            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
//            String projectId = projectsModule.createProjectPool(name, description, instanceOfClass);
//            
//            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
//                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
//                String.format("Create Project Pool with id %s", projectId));
//            return projectId;
            return null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createProjectPool"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public AssetLevelCorrelatedInformation getAffectedServices(int resourceType, String resourceDefinition, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getAffectedServices", "127.0.0.1", sessionId);
//            String[] resourceDefinitionTokens = resourceDefinition.split(";");
//            
//            if (resourceType == 1) { //Hardware
//            
//                switch (resourceDefinitionTokens.length) {
//                    case 1: //A whole network element
//                        return SimpleCorrelation.servicesInDevice(resourceDefinitionTokens[0], bem);
//                    case 2:
//                        return SimpleCorrelation.servicesInSlotOrBoard(resourceDefinitionTokens[0], resourceDefinitionTokens[1], bem, mem);
//                    case 3:
//                        
//                        List<BusinessObjectLight> matchedCommunicationsElements = bem.getObjectsWithFilterLight("GenericCommunicationsElement", "name", resourceDefinitionTokens[0]);
//                        
//                        if (matchedCommunicationsElements.isEmpty())
//                            throw new ServerSideException(String.format("No resource with name %s could be found", resourceDefinitionTokens[0]));
//                        
//                        if (matchedCommunicationsElements.size() > 1)
//                            throw new ServerSideException(String.format("More than one communications equipment with name %s was found", resourceDefinitionTokens[0]));
//                        
//                        List<BusinessObjectLight> deviceChildren = bem.getObjectChildren(matchedCommunicationsElements.get(0).getClassName(), 
//                                                                            matchedCommunicationsElements.get(0).getId(), -1);
//
//                        for (BusinessObjectLight deviceChild : deviceChildren) {
//                            if (resourceDefinitionTokens[1].equals(deviceChild.getName())) {
//                                List<BusinessObjectLight> portsInSlot = bem.getObjectChildren(deviceChild.getClassName(), deviceChild.getId(), -1);
//                                for (BusinessObjectLight portInSlot : portsInSlot) {
//                                    if (resourceDefinitionTokens[2].equals(portInSlot.getName())) 
//                                        return SimpleCorrelation.servicesInPorts(Arrays.asList(bem.getObject("GenericPort", portInSlot.getId())), bem);
//                                }
//                                throw new ServerSideException(String.format("No port %s was found on device %s", 
//                                        resourceDefinitionTokens[2], resourceDefinitionTokens[0]));
//                            }
//                        }  
//                        
//                        throw new ServerSideException(String.format("No slot in communications equipment %s with name %s was found", 
//                                resourceDefinitionTokens[0], resourceDefinitionTokens[1]));
//                    default:
//                        throw new ServerSideException("Invalid resource definition");
//                }
//            }
//            
//            if (resourceType == 2) { //Logical connection
//                List<BusinessObject> matchedConnections = bem.getObjectsWithFilter("GenericLogicalConnection", "name", resourceDefinitionTokens[0]);
//                if (matchedConnections.isEmpty())
//                    throw new ServerSideException(String.format("No logical connection with name %s could be found", resourceDefinitionTokens[0]));
//                
//                List<RemoteObjectLight> rawServices = new ArrayList<>();
//                for (BusinessObjectLight matchedConnection : matchedConnections) {
//                    List<BusinessObjectLight> servicesInConnection = bem.getSpecialAttribute(matchedConnection.getClassName(), 
//                            matchedConnection.getId(), "uses");
//                    for (BusinessObjectLight serviceInConnection : servicesInConnection)
//                        rawServices.add(new RemoteObjectLight(serviceInConnection));
//                }
//                
//                List<ServiceLevelCorrelatedInformation> serviceLevelCorrelatedInformation = new ArrayList<>();
//                HashMap<BusinessObjectLight, List<RemoteObjectLight>> rawCorrelatedInformation = new HashMap<>();
//
//                //Now we organize the rawServices by customers
//                for (RemoteObjectLight rawService : rawServices) {
//                    BusinessObjectLight customer = bem.getFirstParentOfClass(rawService.getClassName(), rawService.getId(), Constants.CLASS_GENERICCUSTOMER);
//                    if (customer != null) {//Services without customers will be ignored. This shouldn't happen, though
//                        if (!rawCorrelatedInformation.containsKey(customer))
//                            rawCorrelatedInformation.put(customer, new ArrayList<>());
//                        
//                        rawCorrelatedInformation.get(customer).add(rawService);
//                    }
//                }
//
//                for (BusinessObjectLight customer : rawCorrelatedInformation.keySet()) 
//                    serviceLevelCorrelatedInformation.add(new ServiceLevelCorrelatedInformation(new RemoteObjectLight(customer), rawCorrelatedInformation.get(customer)));
//
//                return new AssetLevelCorrelatedInformation(RemoteObject.toRemoteObjectArray(matchedConnections), serviceLevelCorrelatedInformation);
//            }
//            
//            if (resourceType == 3) { //Same as 2, but use a GenericPhysicalConnection
//                List<BusinessObject> matchedConnections = bem.getObjectsWithFilter(Constants.CLASS_GENERICPHYSICALCONNECTION, "name", resourceDefinitionTokens[0]);
//                if (matchedConnections.isEmpty())
//                    throw new ServerSideException(String.format("No physical connection with name %s could be found", resourceDefinitionTokens[0]));
//                
//                List<RemoteObjectLight> rawServices = new ArrayList<>();
//                for (BusinessObjectLight matchedConnection : matchedConnections) {
//                    List<BusinessObjectLight> servicesInConnection = bem.getSpecialAttribute(matchedConnection.getClassName(), 
//                            matchedConnection.getId(), "uses");
//                    for (BusinessObjectLight serviceInConnection : servicesInConnection)
//                        rawServices.add(new RemoteObjectLight(serviceInConnection));
//                }
//                
//                List<ServiceLevelCorrelatedInformation> serviceLevelCorrelatedInformation = new ArrayList<>();
//                HashMap<BusinessObjectLight, List<RemoteObjectLight>> rawCorrelatedInformation = new HashMap<>();
//
//                //Now we organize the rawServices by customers
//                for (RemoteObjectLight rawService : rawServices) {
//                    BusinessObjectLight customer = bem.getFirstParentOfClass(rawService.getClassName(), rawService.getId(), Constants.CLASS_GENERICCUSTOMER);
//                    if (customer != null) {//Services without customers will be ignored. This shouldn't happen, though
//                        if (!rawCorrelatedInformation.containsKey(customer))
//                            rawCorrelatedInformation.put(customer, new ArrayList<>());
//                        
//                        rawCorrelatedInformation.get(customer).add(rawService);
//                    }
//                }
//
//                for (BusinessObjectLight customer : rawCorrelatedInformation.keySet()) 
//                    serviceLevelCorrelatedInformation.add(new ServiceLevelCorrelatedInformation(new RemoteObjectLight(customer), rawCorrelatedInformation.get(customer)));
//
//                return new AssetLevelCorrelatedInformation(RemoteObject.toRemoteObjectArray(matchedConnections), serviceLevelCorrelatedInformation);
//            }
            
            throw new ServerSideException("Invalid resource type");
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getAffectedServices"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteSynchronizationProvider> getSynchronizationProviders(String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getSynchronizationProviders", "127.0.0.1", sessionId); //NOI18N
            HashMap<String, RemoteSynchronizationProvider> map = new HashMap();
                        
            List<ConfigurationVariable> configVariables = aem.getConfigurationVariablesWithPrefix("sync.providers.enabled.provider");

            for (ConfigurationVariable configVariable : configVariables) {
                Object configVariableValue = aem.getConfigurationVariableValue(configVariable.getName());
                if (configVariableValue instanceof String) {
                    try {
                        Class providerClass = Class.forName((String) configVariableValue);
                        if (AbstractSyncProvider.class.isAssignableFrom(providerClass)) {
                            AbstractSyncProvider syncProvider = (AbstractSyncProvider)providerClass.newInstance();
                            map.put(configVariable.getName(), new RemoteSynchronizationProvider(syncProvider.getId(), syncProvider.getDisplayName(), syncProvider.isAutomated()));
                        }
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                        throw new InvalidArgumentException(String.format("The configuration variable %s has an invalid value", configVariable.getName()));
                    }
                }
            }
            List<String> positions = new ArrayList();
            positions.addAll(map.keySet());
            Collections.sort(positions);
            
            List<RemoteSynchronizationProvider> syncProviders = new ArrayList();
            for (String position : positions)
                syncProviders.add(map.get(position));
            
            return syncProviders;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSynchronizationProviders"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createSynchronizationDataSourceConfig(String objectId, long syncGroupId, String name, List<StringPair> parameters, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createSynchronizationDataSourceConfig", "127.0.0.1", sessionId);
            //TODO: audit entry
            return aem.createSyncDataSourceConfig(objectId, syncGroupId, name, parameters);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createSynchronizationDataSourceConfig"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createSynchronizationGroup(String name, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createSynchronizationGroup", "127.0.0.1", sessionId);
            //TODO: audit entry
            return aem.createSyncGroup(name);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createSynchronizationGroup"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateSynchronizationGroup(long syncGroupId, List<StringPair> syncGroupProperties, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateSyncDataSourceConfiguration", "127.0.0.1", sessionId);
            aem.updateSyncGroup(syncGroupId, syncGroupProperties);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateSynchronizationGroup"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteSynchronizationGroup getSynchronizationGroup(long syncGroupId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getSynchronizationGroup", "127.0.0.1", sessionId);
            return new RemoteSynchronizationGroup(aem.getSyncGroup(syncGroupId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSynchronizationGroup"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteSynchronizationGroup> getSynchronizationGroups(String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getSynchronizationGroups", "127.0.0.1", sessionId);

            List<SynchronizationGroup> syncGroups = aem.getSyncGroups();
            return RemoteSynchronizationGroup.toArray(syncGroups);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSynchronizationGroups"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteSynchronizationConfiguration getSyncDataSourceConfiguration(String objectId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getSyncDataSourceConfigurations", "127.0.0.1", sessionId);

            SyncDataSourceConfiguration syncDataSourceConfiguration = aem.getSyncDataSourceConfiguration(objectId);
            
            List<StringPair> params = new ArrayList<>();
            for(String key : syncDataSourceConfiguration.getParameters().keySet())
                params.add(new StringPair(key, syncDataSourceConfiguration.getParameters().get(key)));

            return new RemoteSynchronizationConfiguration(
                    syncDataSourceConfiguration.getId(), syncDataSourceConfiguration.getName(), params);

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSyncDataSourceConfiguration"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteSynchronizationConfiguration> getSyncDataSourceConfigurations(long syncGroupId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getSyncDataSourceConfigurations", "127.0.0.1", sessionId);

            List<RemoteSynchronizationConfiguration> RemoteSynchronizationConfigurations = new ArrayList<>();

            List<SyncDataSourceConfiguration> syncDataSourceConfigurations = aem.getSyncDataSourceConfigurations(syncGroupId);

            for (SyncDataSourceConfiguration syncDataSourceConfiguration : syncDataSourceConfigurations) {
                List<StringPair> params = new ArrayList<>();
                for(String key : syncDataSourceConfiguration.getParameters().keySet())
                    params.add(new StringPair(key, syncDataSourceConfiguration.getParameters().get(key)));

                RemoteSynchronizationConfigurations.add(new RemoteSynchronizationConfiguration(
                        syncDataSourceConfiguration.getId(), syncDataSourceConfiguration.getName(), params));
            }

            return RemoteSynchronizationConfigurations;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getSyncDataSourceConfigurations"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateSyncDataSourceConfiguration(long syncDataSourceConfigId, List<StringPair> parameters, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateSyncDataSourceConfiguration", "127.0.0.1", sessionId);
            aem.updateSyncDataSourceConfig(syncDataSourceConfigId, parameters);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateSyncDataSourceConfiguration"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteSynchronizationGroup(long syncGroupId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteSynchronizationGroup", "127.0.0.1", sessionId);
            aem.deleteSynchronizationGroup(syncGroupId);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteSynchronizationGroup"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteSynchronizationDataSourceConfig(long syncDataSourceConfigId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteSynchronizationDataSourceConfig", "127.0.0.1", sessionId);
            aem.deleteSynchronizationDataSourceConfig(syncDataSourceConfigId);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteSynchronizationDataSourceConfig"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void copySyncDataSourceConfiguration(long syncGroupId, long[] syncDataSourceConfigurationIds, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("copySyncDataSourceConfiguration", "127.0.0.1", sessionId);
            
            aem.copySyncDataSourceConfiguration(syncGroupId, syncDataSourceConfigurationIds);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "copySyncDataSourceConfiguration"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void releaseSyncDataSourceConfigFromSyncGroup(long syncGroupId, long[] syncDataSourceConfigurationIds, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("releaseSyncDataSourceConfigFromSyncGroup", "127.0.0.1", sessionId);
            aem.releaseSyncDataSourceConfigFromSyncGroup(syncGroupId, syncDataSourceConfigurationIds);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "releaseSyncDataSourceConfigFromSyncGroup"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void moveSyncDataSourceConfiguration(long oldSyncGroupId, long newSyncGroupId, long[] syncDataSourceConfigurationIds, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("moveSyncDataSourceConfiguration", "127.0.0.1", sessionId);
            aem.moveSyncDataSourceConfiguration(oldSyncGroupId, newSyncGroupId, syncDataSourceConfigurationIds);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "moveSyncDataSourceConfiguration"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createProcessDefinition(String name, String description, String version, boolean enabled, byte[] structure, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createProcessDefinition", "127.0.0.1", sessionId);
            return aem.createProcessDefinition(name, description, version, enabled, structure);
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createProcessDefinition"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateProcessDefinition(long processDefinitionId, List<StringPair> properties, byte[] structure, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateProcessDefinition", "127.0.0.1", sessionId);
            aem.updateProcessDefinition(processDefinitionId, properties, structure);
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateProcessDefinition"), ex);
            throw new ServerSideException(ex.getMessage());
        }  
    }

    @Override
    public void deleteProcessDefinition(long processDefinitionId, String sessionId) throws ServerSideException {
         if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteProcessDefinition", "127.0.0.1", sessionId);
            aem.deleteProcessDefinition(processDefinitionId);
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateProcessDefinition"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public RemoteProcessDefinition getProcessDefinition(long processDefinitionId, String sessionId) throws ServerSideException {
         if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getProcessDefinition", "127.0.0.1", sessionId);
            ProcessDefinition processDefinition = aem.getProcessDefinition(processDefinitionId);
            
            return new RemoteProcessDefinition(processDefinitionId, processDefinition.getName(), 
                    processDefinition.getDescription(), processDefinition.getCreationDate(), 
                    processDefinition.getVersion(), processDefinition.isEnabled(), 
                    RemoteActivityDefinition.asRemoteActivityDefinition(processDefinition.getStartActivity()),
                    RemoteKpi.asRemoteKpis(processDefinition.getKpis()),
                    RemoteKpiAction.asRemoteKpiActions(processDefinition.getKpiActions())
            );
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getProcessDefinition"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public long createProcessInstance(long processDefinitionId, String processInstanceName, String processInstanceDescription, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("createProcessInstance", "127.0.0.1", sessionId);
            return aem.createProcessInstance(processDefinitionId, processInstanceName, processInstanceDescription);
                        
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createProcessInstance"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public RemoteActivityDefinition getNextActivityForProcessInstance(long processInstanceId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getNextActivityForProcessInstance", "127.0.0.1", sessionId);
            ActivityDefinition activityDefinition = aem.getNextActivityForProcessInstance(processInstanceId);
            return RemoteActivityDefinition.asRemoteActivityDefinition(activityDefinition);
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getNextActivityForProcessInstance"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void commitActivity(long processInstanceId, long activityDefinitionId, RemoteArtifact artifact, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("commitActivity", "127.0.0.1", sessionId);
            aem.commitActivity(processInstanceId, activityDefinitionId, 
                new Artifact(artifact.getId(), artifact.getName(), artifact.getContentType(), artifact.getContent(), artifact.getSharedInformation(), artifact.getCreationDate(), artifact.getCommitDate()));
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getNextActivityForProcessInstance"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteArtifactDefinition getArtifactDefinitionForActivity(long processDefinitionId, long activityDefinitionId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getArtifactDefinitionForActivity", "127.0.0.1", sessionId);
            ArtifactDefinition artifactDefinition = aem.getArtifactDefinitionForActivity(processDefinitionId, activityDefinitionId);
            
            RemoteArtifactDefinition remoteArtifactDefinition = new RemoteArtifactDefinition(artifactDefinition.getId(), artifactDefinition.getName(), 
                artifactDefinition.getDescription(), artifactDefinition.getVersion(), artifactDefinition.getType(), 
                artifactDefinition.getDefinition(), artifactDefinition.getPreconditionsScript(), artifactDefinition.getPostconditionsScript(), 
                artifactDefinition.isPrintable(), artifactDefinition.getPrintableTemplate(), artifactDefinition.getExternalScripts()
            );
                        
            remoteArtifactDefinition.setSharedInformation(artifactDefinition.getSharedInformation());
                        
            return remoteArtifactDefinition;
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getArtifactDefinitionForActivity"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteArtifact getArtifactForActivity(long processInstanceId, long activityId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getArtifactForActivity", "127.0.0.1", sessionId);
            Artifact artifact = aem.getArtifactForActivity(processInstanceId, activityId);
            
            return new RemoteArtifact(artifact.getId(), artifact.getName(), artifact.getContentType(), artifact.getContent(), artifact.getSharedInformation(), artifact.getCreationDate(), artifact.getCommitDate());
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getArtifactForActivity"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemotePool> getWarehouseRootPools(String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void associatePhysicalNodeToWarehouse(String objectClass, String objectId, String warehouseClass, String warehouseId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("associatePhysicalNodeToWarehouse", "127.0.0.1", sessionId);
            
            if (mem.isSubclassOf("Warehouse", warehouseClass) || mem.isSubclassOf("VirtualWarehouse", warehouseClass)) { //NOI18N
                
            
                bem.createSpecialRelationship(warehouseClass, warehouseId, objectClass, objectId, "warehouseHas", true); //NOI18N

                aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), warehouseClass, warehouseId, 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                    "warehouseHas", "", objectId, ""); //NOI18N
            }
            else
                throw new ServerSideException(String.format("Class %s is not a Warehouse or VirtualWarehouse", warehouseClass));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "associatePhysicalNodeToWarehouse"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void associatesPhysicalNodeToWarehouse(String[] objectClass, String[] objectId, String warehouseClass, String warehouseId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        
        try {
            String affectedProperties = "", newValues = "";
            
            aem.validateCall("associatesPhysicalNodeToWarehouse", "127.0.0.1", sessionId);
            
            if (mem.isSubclassOf("Warehouse", warehouseClass) || mem.isSubclassOf("VirtualWarehouse", warehouseClass)) { //NOI18N
                
                for (int i = 0; i < objectId.length; i++) {
                    bem.createSpecialRelationship(warehouseClass, warehouseId, objectClass[i], objectId[i], "warehouseHas", true); //NOI18N
                    affectedProperties += "warehouseHas" + " "; //NOI18N
                    newValues += objectId[i] + " ";
                }
                aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), warehouseClass, warehouseId, 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                    affectedProperties, "", newValues, "Associate objects to " + warehouseClass); //NOI18N            
            } 
            else
                throw new ServerSideException(String.format("Class %s is not a Warehouse or VirtualWarehouse", warehouseClass));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "associatesPhysicalNodeToWarehouse"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void releasePhysicalNodeFromWarehouse(String warehouseClass, String warehouseId, String targetId, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("releasePhysicalNodeFromWarehouse", "127.0.0.1", sessionId);
            bem.releaseSpecialRelationship(warehouseClass, warehouseId, targetId, "warehouseHas"); //NOI18N
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), warehouseClass, warehouseId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                "warehouseHas", targetId, "", "Release object from service"); //NOI18N
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "releasePhysicalNodeFromWarehouse"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void moveObjectsToWarehousePool(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateCall("moveObjectsToWarehousePool", "127.0.0.1", sessionId);
            HashMap<String,List<String>> temObjects = new HashMap<>();
            for (int i = 0; i< objectClasses.length; i++){
                List<String> ids = temObjects.get(objectClasses[i]);
                if (ids == null)
                    ids = new ArrayList<>();
                
                ids.add(objectOids[i]);
                temObjects.put(objectClasses[i], ids);
            }

            HashMap<String,String[]> objects = new HashMap<>();
            for(String className : temObjects.keySet()){
                List<String> ids = temObjects.get(className);
                String[] ids_ = new String[ids.size()];
                for (int i=0; i<ids.size(); i++) 
                    ids_[i] = ids.get(i);
                
                objects.put(className, ids_);
            }
            bem.moveObjectsToPool(targetClass, targetOid, objects);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to warehouse pool with id %s", Arrays.toString(objectOids), targetOid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "moveObjectsToWarehousePool"), ex);
            throw new ServerSideException(ex.getMessage());
        }    
    }

    @Override
    public void moveObjectsToWarehouse(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateCall("moveObjectsToWarehouse", "127.0.0.1", sessionId);
            HashMap<String,List<String>> temObjects = new HashMap<>();
            for (int i = 0; i< objectClasses.length; i++){
                List<String> ids = temObjects.get(objectClasses[i]);
                if (ids == null)
                    ids = new ArrayList<>();
                
                ids.add(objectOids[i]);
                temObjects.put(objectClasses[i], ids);
            }
            
            HashMap<String,String[]> objects = new HashMap<>();
            for(String className : temObjects.keySet()){
                List<String> ids = temObjects.get(className);
                String[] ids_ = new String[ids.size()];
                for (int i=0; i<ids.size(); i++) 
                    ids_[i] = ids.get(i);
                
                objects.put(className, ids_);
            }
            bem.moveObjects(targetClass, targetOid, objects);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to object with id %s", Arrays.toString(objectOids), targetOid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "moveObjectsToWarehouse"), ex);
            throw new ServerSideException(ex.getMessage());
        }  
    }

    @Override
    public List<RemoteLogicalConnectionDetails> getBGPMap(List<String> mappedBgpLinksIds, String sessionId) throws ServerSideException {
        if (aem == null)
                throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
            try {
                aem.validateCall("createBGPView", "127.0.0.1", sessionId);
                List<RemoteLogicalConnectionDetails> bgpMap = new ArrayList<>();
                List<BusinessObjectLight> bgpLinks = bem.getObjectsOfClassLight(Constants.CLASS_BGPLINK, -1);
                for (BusinessObjectLight bgpLink : bgpLinks) {
                    if(!mappedBgpLinksIds.contains(bgpLink.getId())){ //We only add the bgp links that are not yet rendered
                        List<BusinessObjectLight> physicalDeviceA = new ArrayList<>();
                        List<BusinessObjectLight> physicalDeviceB = new ArrayList<>();
                        List<BusinessObjectLight> bgpEndpointA = bem.getSpecialAttribute(bgpLink.getClassName(), bgpLink.getId(), "bgpLinkEndpointA");
                        if(!bgpEndpointA.isEmpty()){
                             BusinessObjectLight parent = bem.getFirstParentOfClass(bgpEndpointA.get(0).getClassName(), bgpEndpointA.get(0).getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                             if(parent == null)
                                parent = bem.getParent(bgpEndpointA.get(0).getClassName(), bgpEndpointA.get(0).getId());
                             if(parent != null)
                                physicalDeviceA.add(parent);
                        }
                        List<BusinessObjectLight> bgpEndpointB = bem.getSpecialAttribute(bgpLink.getClassName(), bgpLink.getId(), "bgpLinkEndpointB");
                        if(!bgpEndpointB.isEmpty()){
                             BusinessObjectLight parent = bem.getFirstParentOfClass(bgpEndpointB.get(0).getClassName(), bgpEndpointB.get(0).getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                             if(parent == null)
                                 parent = bem.getParent(bgpEndpointB.get(0).getClassName(), bgpEndpointB.get(0).getId());
                             if(parent != null)
                                 physicalDeviceB.add(parent);
                        }

                        bgpMap.add(new RemoteLogicalConnectionDetails(bem.getObject(bgpLink.getClassName(), bgpLink.getId()), 
                                   bgpEndpointA.isEmpty() ? null : bgpEndpointA.get(0), 
                                   bgpEndpointB.isEmpty() ? null : bgpEndpointB.get(0), 
                                   physicalDeviceA, physicalDeviceB));
                    }
                    
                }
 
                return bgpMap;
            
            } catch(InventoryException ex) {
                throw new ServerSideException(ex.getMessage());
            } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getBGPMap"), ex);
            throw new ServerSideException(ex.getMessage());
        }  
    }

    @Override
    public long createOSPView(String name, String description, byte[] content, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("createOSPView", "127.0.0.1", sessionId);
            long res = aem.createOSPView(name, description, content);
                    
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created OSP View with id %s ", res));
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "createOSPView"), ex);
            throw new ServerSideException(ex.getMessage());
        }  
    }

    @Override
    public RemoteViewObject getOSPView(long viewId, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getOSPView", "127.0.0.1", sessionId);
            return new RemoteViewObject(aem.getOSPView(viewId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getOSPView"), ex);
            throw new ServerSideException(ex.getMessage());
        } 
    }

    @Override
    public List<RemoteViewObjectLight> getOSPViews(String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("getOSPView", "127.0.0.1", sessionId);
            List<RemoteViewObjectLight> res = new ArrayList<>();
            
            aem.getOSPViews().forEach((aView) -> res.add(new RemoteViewObjectLight(aView)));
            
            res.sort((view1, view2) -> {
                return view1.getName().compareTo(view2.getName()); //To change body of generated lambdas, choose Tools | Templates.
            });
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "getOSPViews"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateOSPView(long viewId, String name, String description, byte[] content, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("updateOSPView", "127.0.0.1", sessionId);
            aem.updateOSPView(viewId, name, description, content);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("OSP View with id %s ", viewId));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "updateOSPView"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteOSPView(long viewId, String sessionId) throws ServerSideException {
       if (aem == null)
            throw new ServerSideException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));
        try {
            aem.validateCall("deleteOSPView", "127.0.0.1", sessionId);
            aem.deleteOSPView(viewId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("OSP View with id %s ", viewId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        } catch (Exception ex) { // Unexpected error. Log the stach trace and 
            Logger.getLogger(KuwaibaSoapWebServiceImpl.class.getName()).log(Level.SEVERE, 
                    String.format(ts.getTranslatedString("module.webservice.messages.unexpected-error"), "deleteOSPView"), ex);
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String getIPAddress() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Finds the user name using the session Id
     * @param sessionId The sessionId
     * @return The username or null of the session could not be found
     */
    public String getUserNameFromSession (String sessionId) {
        Session aSession = aem.getSessions().get(sessionId);
        if (aSession == null)
            return null;
        return aSession.getUser().getUserName();
    }
    
    /**
     * Helper class to parse from a transientQuery into a ExtendedQuery
     * @param query
     * @return
     */
    private ExtendedQuery transientQuerytoExtendedQuery(TransientQuery query){
        ExtendedQuery eq;
        List<ExtendedQuery> listeq = new ArrayList<>();

        if(query == null)
            return null;
        else
            eq = new ExtendedQuery(query.getClassName(),
                                query.getLogicalConnector(),
                                query.getAttributeNames(),
                                query.getVisibleAttributeNames(),
                                query.getAttributeValues(),
                                query.getConditions(), listeq, query.getPage(), query.getLimit());


        if(query.getJoins() != null){
            for(TransientQuery join : query.getJoins()){
                    listeq.add(transientQuerytoExtendedQuery(join));
            }
        }
        
        return eq;
    }

       
    
}

/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expregss or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kuwaiba.services.persistence.impl.neo4j;

import com.neotropic.kuwaiba.modules.GenericCommercialModule;
import com.neotropic.kuwaiba.sync.model.SyncDataSourceConfiguration;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.ConnectionManager;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.BusinessRule;
import org.kuwaiba.apis.persistence.application.CompactQuery;
import org.kuwaiba.apis.persistence.application.ConfigurationVariable;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.apis.persistence.application.FavoritesFolder;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.GroupProfileLight;
import org.kuwaiba.apis.persistence.application.Pool;
import org.kuwaiba.apis.persistence.application.Privilege;
import org.kuwaiba.apis.persistence.application.ResultRecord;
import org.kuwaiba.apis.persistence.application.Session;
import org.kuwaiba.apis.persistence.application.Task;
import org.kuwaiba.apis.persistence.application.TaskResult;
import org.kuwaiba.apis.persistence.application.TemplateObject;
import org.kuwaiba.apis.persistence.application.TemplateObjectLight;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.application.Validator;
import org.kuwaiba.apis.persistence.application.ValidatorDefinition;
import org.kuwaiba.apis.persistence.application.ViewObject;
import org.kuwaiba.apis.persistence.application.ViewObjectLight;
import org.kuwaiba.apis.persistence.application.process.ActivityDefinition;
import org.kuwaiba.apis.persistence.application.process.Artifact;
import org.kuwaiba.apis.persistence.application.process.ArtifactDefinition;
import org.kuwaiba.apis.persistence.application.process.ProcessDefinition;
import org.kuwaiba.apis.persistence.application.process.ProcessInstance;
import org.kuwaiba.apis.persistence.application.queries.WarehouseManagerSearchQuery;
import org.kuwaiba.apis.persistence.business.BusinessObject;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.business.BusinessObjectList;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.BusinessRuleException;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.exceptions.NoCommercialModuleFoundException;
import org.kuwaiba.apis.persistence.exceptions.UnsupportedPropertyException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.GenericObjectList;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.services.persistence.cache.CacheManager;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.services.persistence.util.Util;
import org.kuwaiba.util.ChangeDescriptor;
import org.kuwaiba.util.dynamicname.DynamicName;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.interfaces.ws.toserialize.application.TaskNotificationDescriptor;
import org.kuwaiba.interfaces.ws.toserialize.application.TaskScheduleDescriptor;
import org.kuwaiba.interfaces.ws.toserialize.application.UserInfoLight;
import org.kuwaiba.web.procmanager.ProcessCache;
import org.mindrot.jbcrypt.BCrypt;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterators;
import org.openide.util.Exceptions;

/**
 * Application Entity Manager reference implementation
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ApplicationEntityManagerImpl implements ApplicationEntityManager {
    /**
     * Graph db service
     */
    private GraphDatabaseService graphDb;
    /**
     * Configuration variables
     */
    private Properties configuration;
    /**
     * Default background path
     */
    private static String DEFAULT_BACKGROUNDS_PATH = "/data/img/backgrounds"; //NOI18N
    /**
     * Users index
     */
    private Label userLabel;
    /**
     * Object label
     */
    private Label inventoryObjectLabel;
    /**
     * Class label
     */
    private Label classLabel;
    /**
     * Groups label
     */
    private Label groupLabel;
    /**
     * Label for list type items (of all classes)
     */
    private Label listTypeItemLabel;
    /**
     * Pools label 
     */
    private Label poolLabel;
    /**
     * Device layouts label 
     */
    private Label layoutLabel;
    /**
     * Templates label 
     */
    private Label templateLabel;
    /**
     * Template elements label 
     */
    private Label templateElementLabel;
    /**
     * Label for special nodes(like group root node)
     */
    private Label specialNodeLabel;
    /**
     * Queries label 
     */
    private Label queryLabel;
    /**
     * Task label
     */
    private Label taskLabel;
    /**
     * Label for business rules
     */
    private Label businessRulesLabel;
    /**
     * Label for general views (those not related to a particular object)
     */
    private Label generalViewsLabel;
    /**
     * SyncGroup label
     */
    private Label syncGroupsLabel;
    /**
     * Process Instances Label
     */
    private Label processInstanceLabel;
    /**
     * The label that contains the configuration variables pools
     */
    private Label configurationVariablesPools;
    /**
     * The label that contains the configuration variables
     */
    private Label configurationVariables;
    /**
     * Label to tag the nodes corresponding to validators. Validators are pieces of logic that validate is a given object matches certain condition, 
     * for example, if the state of an object matches particular value, or if an object has a relationship with another. Validators are attached to 
     * BusinessObjectLight instances and are used mostly to render the objects in a certain way depending on the value of the validator. For example,
     * a client would like to show a green icon besides the name of an object whose state is "operational"
     */
    private Label validatorDefinitions;
    /**
     * Reference to the singleton instance of CacheManager
     */
    private CacheManager cm;
    /**
     * Reference to the metadata entity manager
     */
    private MetadataEntityManager mem;
    /**
     * Map with the current sessions. The key is the username, the value is the respective session object
     */
    private HashMap<String, Session> sessions;
    /**
     * A library of all registered commercial modules
     */
    private HashMap<String, GenericCommercialModule> commercialModules;
    
    public ApplicationEntityManagerImpl() {
        this.cm = CacheManager.getInstance();
        commercialModules = new HashMap<>();
        this.configuration = new Properties();
    }

    public ApplicationEntityManagerImpl(ConnectionManager cmn, MetadataEntityManager mem) {
        this();
        this.graphDb = (GraphDatabaseService) cmn.getConnectionHandler();
        this.mem = mem;
        
        userLabel = Label.label(Constants.LABEL_USER);
        inventoryObjectLabel = Label.label(Constants.LABEL_INVENTORY_OBJECTS);
        classLabel = Label.label(Constants.LABEL_CLASS);
        groupLabel = Label.label(Constants.LABEL_GROUP);
        listTypeItemLabel = Label.label(Constants.LABEL_LIST_TYPE_ITEMS);
        poolLabel = Label.label(Constants.LABEL_POOLS);
        layoutLabel = Label.label(Constants.LABEL_LAYOUTS);
        templateLabel = Label.label(Constants.LABEL_TEMPLATES);
        templateElementLabel = Label.label(Constants.LABEL_TEMPLATE_ELEMENTS);
        specialNodeLabel = Label.label(Constants.LABEL_SPECIAL_NODE);
        queryLabel = Label.label(Constants.LABEL_QUERIES);
        taskLabel = Label.label(Constants.LABEL_TASKS);
        businessRulesLabel = Label.label(Constants.LABEL_BUSINESS_RULES);
        generalViewsLabel = Label.label(Constants.LABEL_GENERAL_VIEWS);
        syncGroupsLabel = Label.label(Constants.LABEL_SYNCGROUPS);
        processInstanceLabel = Label.label(Constants.LABEL_PROCESS_INSTANCE);
        configurationVariablesPools = Label.label(Constants.LABEL_CONFIG_VARIABLES_POOLS);
        configurationVariables = Label.label(Constants.LABEL_CONFIG_VARIABLES);
        validatorDefinitions = Label.label(Constants.LABEL_VALIDATOR_DEFINITIONS);
        
        try (Transaction tx = graphDb.beginTx()) {
            
            ResourceIterator<Node> listTypeItems = graphDb.findNodes(listTypeItemLabel);
            
            while (listTypeItems.hasNext()) {
                
                Node listTypeNode = listTypeItems.next();
                
                GenericObjectList aListType = Util.createGenericObjectListFromNode(listTypeNode);
                cm.putListType(aListType);
            }
        } catch(Exception ex) {
            System.out.println(String.format("[KUWAIBA] [%s] An error was found while creating the AEM instance: %s", 
                    Calendar.getInstance().getTime(), ex.getMessage()));
        }
        this.sessions = new HashMap<>();
    }
    
    @Override
    public HashMap<String, Session> getSessions(){
        return sessions;
    }

    //TODO add ipAddress, sessionId
    @Override
    public long createUser(String userName, String password, String firstName,
            String lastName, boolean enabled, int type, List<Privilege> privileges, long defaultGroupId)
            throws InvalidArgumentException {
        if (userName == null)
            throw new InvalidArgumentException("User name can not be null");
        
        if (userName.trim().isEmpty())
            throw new InvalidArgumentException("User name can not be an empty string");
        
        if (!userName.matches("^[a-zA-Z0-9_.]*$"))
            throw new InvalidArgumentException(String.format("User name %s contains invalid characters", userName));
        
        if (password == null)
            throw new InvalidArgumentException("Password can not be null");
       
        if (password.trim().isEmpty())
            throw new InvalidArgumentException("Password can not be an empty string");
        
        if (type != UserProfile.USER_TYPE_GUI && type != UserProfile.USER_TYPE_WEB_SERVICE && type != UserProfile.USER_TYPE_SOUTHBOUND)
            throw new InvalidArgumentException("Invalid user type");
            
        try(Transaction tx = graphDb.beginTx()) {
            Node storedUser = graphDb.findNode(userLabel, Constants.PROPERTY_NAME, userName);

            if (storedUser != null)
                throw new InvalidArgumentException(String.format("User name %s already exists", userName));
            
            Node newUserNode = graphDb.createNode(userLabel);

            newUserNode.setProperty(UserProfile.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            newUserNode.setProperty(UserProfile.PROPERTY_NAME, userName);
            newUserNode.setProperty(UserProfile.PROPERTY_PASSWORD, BCrypt.hashpw(password, BCrypt.gensalt()));
            newUserNode.setProperty(UserProfile.PROPERTY_FIRST_NAME, firstName == null ? "" : firstName);
            newUserNode.setProperty(UserProfile.PROPERTY_LAST_NAME, lastName == null ? "" : lastName);
            newUserNode.setProperty(UserProfile.PROPERTY_TYPE, type);
            newUserNode.setProperty(Constants.PROPERTY_ENABLED, enabled);
            
            Node defaultGroupNode = Util.findNodeByLabelAndId(groupLabel, defaultGroupId);
            
            if (defaultGroupNode != null)
                newUserNode.createRelationshipTo(defaultGroupNode, RelTypes.BELONGS_TO_GROUP);

            else{
                tx.failure();
                throw new InvalidArgumentException(String.format("Group with id %s can not be found", defaultGroupId));
            }

            if (privileges != null) {
                for (Privilege privilege : privileges) {
                    Node privilegeNode = graphDb.createNode();
                    privilegeNode.setProperty(Privilege.PROPERTY_FEATURE_TOKEN, privilege.getFeatureToken());
                    privilegeNode.setProperty(Privilege.PROPERTY_ACCESS_LEVEL, privilege.getAccessLevel());
                    newUserNode.createRelationshipTo(privilegeNode, RelTypes.HAS_PRIVILEGE);
                }
            }
            tx.success();
            
            cm.putUser(Util.createUserProfileWithGroupPrivilegesFromNode(newUserNode));
            return newUserNode.getId();
        }
    }

    @Override
    public void setUserProperties(long oid, String userName, String password, String firstName,
            String lastName, int enabled, int type)
            throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx()) {
            Node userNode = Util.findNodeByLabelAndId(userLabel, oid);

            if(userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a user with id %s", oid));

            if(password != null) {
                if (password.trim().isEmpty())
                    throw new InvalidArgumentException("Password can't be an empty string");
            }
            
            if (password != null)
                userNode.setProperty(Constants.PROPERTY_PASSWORD, BCrypt.hashpw(password, BCrypt.gensalt()));
            if (firstName != null)
                userNode.setProperty(Constants.PROPERTY_FIRST_NAME, firstName);
            if (lastName != null)
                userNode.setProperty(Constants.PROPERTY_LAST_NAME, lastName);
            
            if (type != -1 && type != UserProfile.USER_TYPE_GUI && type != UserProfile.USER_TYPE_WEB_SERVICE && type != UserProfile.USER_TYPE_SOUTHBOUND)
                throw new InvalidArgumentException("User type provided is not valid");
            
            if (type != -1)
                userNode.setProperty(Constants.PROPERTY_TYPE, type);
            
            if (enabled != -1 && enabled != 0 && enabled != 1)
                throw new InvalidArgumentException("User enabled state is not valid");
            
            if (enabled != -1)
                userNode.setProperty(Constants.PROPERTY_ENABLED, enabled == 1 );
            
            if(userName != null) {
                
                if (userName.trim().isEmpty())
                    throw new InvalidArgumentException("User name can not be an empty string");

                if (!userName.matches("^[a-zA-Z0-9_.]*$"))
                    throw new InvalidArgumentException(String.format("The user name %s contains invalid characters", userName));

                if (UserProfile.DEFAULT_ADMIN.equals(userNode.getProperty(UserProfile.PROPERTY_NAME)))
                    throw new InvalidArgumentException("The default administrator user name can not be changed");
                
                Node aUser = graphDb.findNode(userLabel, Constants.PROPERTY_NAME, userName);
                if (aUser != null)
                    throw new InvalidArgumentException(String.format("User name %s already exists", userName));
                
                //Refresh the user index and update the user name
                userNode.setProperty(Constants.PROPERTY_NAME, userName);
                cm.removeUser(userName);
            }

            tx.success();
            
            cm.putUser(Util.createUserProfileWithGroupPrivilegesFromNode(userNode));
        }
    }

    @Override
    public void setUserProperties(String formerUsername, String newUserName, String password, String firstName,
            String lastName, int enabled, int type)
            throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx()) { 
            Node userNode = graphDb.findNode(userLabel, Constants.PROPERTY_NAME, formerUsername);

            if(userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a user with name %s", formerUsername));

            if(newUserName != null) {
                if (newUserName.trim().isEmpty())
                    throw new InvalidArgumentException("User name can not be an empty string");

                if (UserProfile.DEFAULT_ADMIN.equals(formerUsername))
                    throw new InvalidArgumentException("The default administrator user name can not be changed");
                
                Node storedUser =  graphDb.findNode(userLabel, Constants.PROPERTY_NAME, newUserName);
                if (storedUser != null)
                    throw new InvalidArgumentException(String.format("User name %s already exists", newUserName));

                if (!newUserName.matches("^[a-zA-Z0-9_.]*$"))
                    throw new InvalidArgumentException(String.format("The user name %s contains invalid characters", newUserName));
            }
            if(password != null){
                if (password.trim().isEmpty())
                    throw new InvalidArgumentException("Password can't be an empty string");
            }
        
            if (newUserName != null){
                //refresh the userindex
                userNode.setProperty(Constants.PROPERTY_NAME, newUserName);
                
                cm.removeUser(newUserName);
            }
            if (password != null)
                userNode.setProperty(Constants.PROPERTY_PASSWORD, BCrypt.hashpw(password, BCrypt.gensalt()));
            if(firstName != null)
                userNode.setProperty(Constants.PROPERTY_FIRST_NAME, firstName);
            if(lastName != null)
                userNode.setProperty(Constants.PROPERTY_LAST_NAME, lastName);
            if (type != -1 && type != UserProfile.USER_TYPE_GUI && type != UserProfile.USER_TYPE_WEB_SERVICE && type != UserProfile.USER_TYPE_SOUTHBOUND)
                throw new InvalidArgumentException("User type provided is not valid");
            if (type != -1)
                userNode.setProperty(Constants.PROPERTY_TYPE, type );
            if (enabled != -1 && enabled != 0 && enabled != 1)
                throw new InvalidArgumentException("User enabled state is not valid");
            if (enabled != -1)
                userNode.setProperty(Constants.PROPERTY_ENABLED, enabled == 1 );
            
            tx.success();
            
            cm.putUser(Util.createUserProfileWithGroupPrivilegesFromNode(userNode));
        }
    }

    @Override
    public void addUserToGroup(long userId, long groupId) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node groupNode = Util.findNodeByLabelAndId(groupLabel, groupId);
            if (groupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Group with id %s could not be found", groupId));
            
            for (Relationship belongsToGroupRelationship : groupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_GROUP)) {
                if (belongsToGroupRelationship.getStartNode().getId() == userId)
                    throw new InvalidArgumentException(String.format("The user with id %s already belongs to group with id %s", userId, groupId));
            }            
            Node userNode = Util.findNodeByLabelAndId(userLabel, userId);
            
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("User with id %s could not be found", userId));
            
            userNode.createRelationshipTo(groupNode, RelTypes.BELONGS_TO_GROUP);
            
            tx.success();
        }
    }

    @Override
    public void removeUserFromGroup(long userId, long groupId) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node groupNode = Util.findNodeByLabelAndId(groupLabel, groupId);
            
            if (groupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Group with id %s could not be found", groupId));
            
            for (Relationship belongsToGroupRelationship : groupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_GROUP)) {
                Node userNode = belongsToGroupRelationship.getStartNode();
                if (userNode.getId() == userId) {
                    belongsToGroupRelationship.delete();
                    if (!userNode.hasRelationship(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP))
                        throw new InvalidArgumentException("No orphan users are allowed. Put it in another group before removing it from this one");
                    
                    tx.success();
                    return;
                }
            }
            throw new ApplicationObjectNotFoundException(String.format("User with id %s is not related to group with id %s", userId, groupId));
        }
    }

    @Override
    public void setPrivilegeToUser(long userId, String featureToken, int accessLevel) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        //TODO: This method should check the new privilege against a list of predefined feature tokens to avoid setting/adding bogus items
        if (accessLevel != Privilege.ACCESS_LEVEL_READ && accessLevel != Privilege.ACCESS_LEVEL_READ_WRITE)
            throw new InvalidArgumentException(String.format("The access level privided is not valid: %s", accessLevel));
            
        try (Transaction tx = graphDb.beginTx()) {
            Node userNode = Util.findNodeByLabelAndId(userLabel, userId);
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("User with id %s could not be found", userId));

            Node privilegeNode = null;
            for (Relationship hasPrivilegeRelationship : userNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_PRIVILEGE)) {
                if(featureToken.equals(hasPrivilegeRelationship.getEndNode().getProperty(Privilege.PROPERTY_FEATURE_TOKEN)))
                    privilegeNode = hasPrivilegeRelationship.getEndNode();
            }
        
            if (privilegeNode == null) {
                privilegeNode = graphDb.createNode();
                userNode.createRelationshipTo(privilegeNode, RelTypes.HAS_PRIVILEGE);
                privilegeNode.setProperty(Privilege.PROPERTY_FEATURE_TOKEN, featureToken);
            }
            
            privilegeNode.setProperty(Privilege.PROPERTY_ACCESS_LEVEL, accessLevel);
            tx.success();
        }
    }

    @Override
    public void setPrivilegeToGroup(long groupId, String featureToken, int accessLevel) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        //TODO: This method should check the new privilege against a list of predefined feature tokens to avoid adding bogus items
        if (accessLevel != Privilege.ACCESS_LEVEL_READ && accessLevel != Privilege.ACCESS_LEVEL_READ_WRITE)
            throw new InvalidArgumentException(String.format("The provided access level is not valid: %s", accessLevel));
        
        try (Transaction tx = graphDb.beginTx()) {
            Node groupNode = Util.findNodeByLabelAndId(groupLabel, groupId);
            if (groupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Group with id %s could not be found", groupId));

            Node privilegeNode = null;
            for (Relationship hasPrivilegeRelationship : groupNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_PRIVILEGE)) {
                if(featureToken.equals(hasPrivilegeRelationship.getEndNode().getProperty(Privilege.PROPERTY_FEATURE_TOKEN)))
                    privilegeNode = hasPrivilegeRelationship.getEndNode();
            }
        
            if (privilegeNode == null) {
                privilegeNode = graphDb.createNode();
                groupNode.createRelationshipTo(privilegeNode, RelTypes.HAS_PRIVILEGE);
                privilegeNode.setProperty(Privilege.PROPERTY_FEATURE_TOKEN, featureToken);
            }
            
            privilegeNode.setProperty(Privilege.PROPERTY_ACCESS_LEVEL, accessLevel);
            tx.success();
        }
    }

    @Override
    public void removePrivilegeFromUser(long userId, String featureToken) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node userNode = Util.findNodeByLabelAndId(userLabel, userId);
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("User with id %s could not be found", userId));
        
            for (Relationship hasPrivilegeRelationship : userNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_PRIVILEGE)) {
                if (featureToken.equals(hasPrivilegeRelationship.getEndNode().getProperty(Privilege.PROPERTY_FEATURE_TOKEN))) {
                    hasPrivilegeRelationship.delete();
                    hasPrivilegeRelationship.getEndNode().delete();
                    tx.success();
                    return; 
                }
            }
            tx.failure();
            throw new InvalidArgumentException(String.format("The user with id %s already does not have the privilege %s", userId, featureToken));
        }
    }

    @Override
    public void removePrivilegeFromGroup(long groupId, String featureToken) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node groupNode = Util.findNodeByLabelAndId(groupLabel, groupId);
            if (groupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Group with id %s could not be found", groupId));
        
            for (Relationship hasPrivilegeRelationship : groupNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_PRIVILEGE)) {
                if (featureToken.equals(hasPrivilegeRelationship.getEndNode().getProperty(Privilege.PROPERTY_FEATURE_TOKEN))) {
                    hasPrivilegeRelationship.delete();
                    hasPrivilegeRelationship.getEndNode().delete();
                    tx.success();
                    return; 
                }
            }
            tx.failure();
            throw new InvalidArgumentException(String.format("The group with id %s already does not have the privilege %s", groupId, featureToken));
        }
    }
    
    @Override
    public long createGroup(String groupName, String description, List<Long> users) throws InvalidArgumentException, 
                    ApplicationObjectNotFoundException {
        if (groupName == null)
            throw new InvalidArgumentException("Group name can not be null");
        if (groupName.trim().isEmpty())
            throw new InvalidArgumentException("Group name can not be an empty string");
        if (!groupName.matches("^[a-zA-Z0-9_. ]*$"))
            throw new InvalidArgumentException(String.format("Group \"%s\" contains invalid characters", groupName));
        
        try (Transaction tx = graphDb.beginTx()) {
            Node storedGroup = graphDb.findNode(groupLabel, Constants.PROPERTY_NAME, groupName);
            if (storedGroup != null)
                throw new InvalidArgumentException(String.format("Group \"%s\" already exists", groupName));

            Node newGroupNode = graphDb.createNode(groupLabel);

            newGroupNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            newGroupNode.setProperty(Constants.PROPERTY_NAME, groupName);
            newGroupNode.setProperty(Constants.PROPERTY_DESCRIPTION, description == null ? "" : description);

            if (users != null){
                for (long userId : users) {
                    Node userNode = Util.findNodeByLabelAndId(userLabel, userId);
                    if(userNode != null)
                        userNode.createRelationshipTo(newGroupNode, RelTypes.BELONGS_TO_GROUP);
                    else {
                        tx.failure();
                        throw new ApplicationObjectNotFoundException(String.format("User with id %s can not be found. Group creation aborted.", userId));
                    }
                }
            }
            
            graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_GROUPS).createRelationshipTo(newGroupNode, RelTypes.GROUP);

            tx.success();
            cm.putGroup(Util.createGroupProfileFromNode(newGroupNode));
            
            return newGroupNode.getId();
        }
    }

    @Override
    public List<UserProfile> getUsers() {
        List<UserProfile> usersProfile = new ArrayList<>();
        
        try (Transaction tx = graphDb.beginTx()) {
            try (ResourceIterator<Node> users = graphDb.findNodes(userLabel)) {
                while (users.hasNext())
                    usersProfile.add(Util.createUserProfileWithGroupPrivilegesFromNode(users.next()));
                return usersProfile;
            }            
        }
    }

    @Override
    public List<GroupProfile> getGroups() {
        try(Transaction tx = graphDb.beginTx()) {
            ResourceIterator<Node> groupNodes = graphDb.findNodes(groupLabel);

            List<GroupProfile> groups =  new ArrayList<>();
            while (groupNodes.hasNext())
                groups.add((Util.createGroupProfileFromNode(groupNodes.next())));
            return groups;
        }
    }

    @Override
    public void setGroupProperties(long id, String groupName, String description)
            throws InvalidArgumentException, ApplicationObjectNotFoundException {
        
        try(Transaction tx = graphDb.beginTx()) {
                Node groupNode = Util.findNodeByLabelAndId(groupLabel, id);
            if(groupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find the group with id %s",id));
            
            if(groupName != null) {
                if (groupName.trim().isEmpty())
                    throw new InvalidArgumentException("Group name can not be an empty string");
                if (!groupName.matches("^[a-zA-Z0-9_. ]*$"))
                    throw new InvalidArgumentException(String.format("Group %s contains invalid characters", groupName));

                Node storedGroup = graphDb.findNode(groupLabel, Constants.PROPERTY_NAME, groupName);
                    if (storedGroup != null)
                        throw new InvalidArgumentException(String.format("The group name %s is already in use", groupName));
                cm.removeGroup((String)groupNode.getProperty(Constants.PROPERTY_NAME));
                groupNode.setProperty(Constants.PROPERTY_NAME, groupName);
            }
            if(description != null)
                groupNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            
            cm.putGroup(Util.createGroupProfileFromNode(groupNode));
            tx.success();
        }
    }

    @Override
    public void deleteUsers(long[] oids) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        
        try(Transaction tx = graphDb.beginTx()) {
            //TODO watch if there are relationships you can/should not delete
            if(oids != null){
                for (long id : oids) {
                    Node userNode = Util.findNodeByLabelAndId(userLabel, id);
                    Util.deleteUserNode(userNode);
                }
            }
            
            tx.success();
        }
    }

    @Override
    public void deleteGroups(long[] oids) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        
        try(Transaction tx = graphDb.beginTx()) {
            if(oids != null) {
                for (long id : oids) {
                    Node groupNode = Util.findNodeByLabelAndId(groupLabel, id);
                    if(groupNode == null)
                        throw new ApplicationObjectNotFoundException(String.format("Can not find the group with id %s",id));
                    
                    Node adminNode = graphDb.findNode(userLabel, Constants.PROPERTY_NAME, UserProfile.DEFAULT_ADMIN);
                    List<Node> adminGroupNodes = new ArrayList();

                    for (Relationship relationship : adminNode.getRelationships(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP))
                        adminGroupNodes.add(relationship.getEndNode());
                    
                    if (adminGroupNodes.size() == 1) {
                        if (groupNode.getId() == adminGroupNodes.get(0).getId())
                            throw new InvalidArgumentException("User admin can no be orphan. Put it in another group before removing this group");                                                                                    
                    }
                    
                    for (Relationship relationship : groupNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_PRIVILEGE)) {
                        Node privilegeNode = relationship.getEndNode();
                        relationship.delete();
                        privilegeNode.delete();
                    }
                    
                    for (Relationship relationship : groupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_GROUP)) {
                        Node userNode = relationship.getStartNode();
                        
                        if (adminNode.getId() == userNode.getId())
                            continue;
                                                
                        relationship.delete();
                        
                        //This will delete all users associated *only* to this group. The users associated to other groups will be kept and the relationship with this group will be released
                        if (userNode.hasRelationship(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP)) 
                            Util.deleteUserNode(userNode);
                            
                    }
                    
                    //Now we release the rest of the relationships
                    for (Relationship otherRelationship : groupNode.getRelationships())
                        otherRelationship.delete();
                    
                    cm.removeGroup((String)groupNode.getProperty(GroupProfile.PROPERTY_NAME));
                    groupNode.delete();
                }
            }
            tx.success();
        }
    }
    
   //List type related methods
    @Override
   public String createListTypeItem(String className, String name, String displayName)
            throws MetadataObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException {               
       if (name == null || className == null)
           throw new InvalidArgumentException("Item name and class name can not be null");
       
       ClassMetadata myClass= cm.getClass(className);
       try(Transaction tx = graphDb.beginTx()) {
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, className);
            if (classNode ==  null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found. Contact your administrator.",className));

            if (myClass == null) {
                 myClass = Util.createClassMetadataFromNode(classNode);
                 cm.putClass(myClass);
             }      

            if (!mem.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, className))
                 throw new InvalidArgumentException(String.format("Class %s is not a list type", className));

            if (myClass.isInDesign())
                 throw new OperationNotPermittedException("Can not create instances of classes marked as isDesign");

            if (myClass.isAbstract())
                 throw new OperationNotPermittedException("Can not create instances of abstract classes");
       
           Node newItem = graphDb.createNode(listTypeItemLabel);
           String uuid = UUID.randomUUID().toString();
           newItem.setProperty(Constants.PROPERTY_UUID, uuid);
                      
           newItem.setProperty(Constants.PROPERTY_NAME, name);
           if (displayName != null)
               newItem.setProperty(Constants.PROPERTY_DISPLAY_NAME, displayName);
           newItem.createRelationshipTo(classNode, RelTypes.INSTANCE_OF);

           tx.success();
           GenericObjectList newListType = new GenericObjectList(uuid, name);
           cm.putListType(newListType);
           return uuid;
        }
    }

    @Override
    public void deleteListTypeItem(String className, String oid, boolean realeaseRelationships) 
            throws MetadataObjectNotFoundException, OperationNotPermittedException, BusinessObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        try(Transaction tx = graphDb.beginTx())
        {
            if (!mem.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, className))
                throw new InvalidArgumentException(String.format("Class %s is not a list type", className));

            Node listTypeItemNode = getInstanceOfClass(className, oid);
            
            Iterator<Relationship> relationShipsIterator = listTypeItemNode.getRelationships(RelTypes.RELATED_TO).iterator();
            if (relationShipsIterator.hasNext()) {
                if (realeaseRelationships) {
                    for (Relationship aRelatedToRelationship : listTypeItemNode.getRelationships(RelTypes.RELATED_TO)) 
                        aRelatedToRelationship.delete();
                }
                else
                    throw new OperationNotPermittedException(String.format("The list type item with class %s and id %s can not be deleted because is related to inventory objects or another list types", className, oid));
            }
            if (listTypeItemNode.hasRelationship(Direction.OUTGOING, RelTypes.INSTANCE_OF))
                listTypeItemNode.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).delete();
            
            listTypeItemNode.delete();
            tx.success();
            cm.removeListType(className);
        }
    }

    @Override
    public List<BusinessObjectLight> getListTypeItems(String className)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        
        List<BusinessObjectLight> children = new ArrayList<>();
        try(Transaction tx = graphDb.beginTx()) {
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classNode ==  null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found. Contact your administrator.",className));

            if (!Util.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, classNode))
                throw new InvalidArgumentException(String.format("Class %s is not a list type", className));

            Iterable<Relationship> childrenAsRelationships = classNode.getRelationships(RelTypes.INSTANCE_OF);
            Iterator<Relationship> relationships = childrenAsRelationships.iterator();

            while(relationships.hasNext()){
                Node child = relationships.next().getStartNode();
                String childUuid = child.hasProperty(Constants.PROPERTY_UUID) ? (String) child.getProperty(Constants.PROPERTY_UUID) : null;
                if (childUuid == null)
                    throw new InvalidArgumentException(String.format("The list type item with id %s does not have uuid", child.getId()));
                                    
                children.add(new BusinessObjectLight(className, childUuid, (String)child.getProperty(Constants.PROPERTY_NAME)));
            }
            tx.success();
        }
        
        Collections.sort(children);
        
        return children;
    }
    
    @Override
    public BusinessObjectLight getListTypeItem(String listTypeClassName, String listTypeItemId) throws 
        MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, listTypeClassName);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found. Contact your administrator.", listTypeClassName));
            
            if (!Util.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, classNode))
                throw new InvalidArgumentException(String.format("Class %s is not a list type", listTypeClassName));
            
            for (Relationship childRel : classNode.getRelationships(RelTypes.INSTANCE_OF)) {
                Node child = childRel.getStartNode();
                if (listTypeItemId.equals((String) child.getProperty(Constants.PROPERTY_UUID))) { 
                    tx.success();
                    return new BusinessObjectLight(listTypeClassName, (String) child.getProperty(Constants.PROPERTY_UUID), 
                            (String) child.getProperty(Constants.PROPERTY_NAME));
                }
                
            }
            throw new ApplicationObjectNotFoundException(String.format("A list type of class %s and id %s could not be found", listTypeClassName, listTypeItemId));
        }
    }
    
    @Override
    public BusinessObjectLight getListTypeItemWithName(String listTypeClassName, String listTypeItemName) throws 
        MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, listTypeClassName);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found. Contact your administrator.", listTypeClassName));
            
            if (!Util.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, classNode))
                throw new InvalidArgumentException(String.format("Class %s is not a list type", listTypeClassName));
            
            for (Relationship childRel : classNode.getRelationships(RelTypes.INSTANCE_OF)) {
                Node child = childRel.getStartNode();
                if (child.hasProperty(Constants.PROPERTY_NAME) && child.getProperty(Constants.PROPERTY_NAME).equals(listTypeItemName)) {
                    tx.success();
                    String childUuid = child.hasProperty(Constants.PROPERTY_UUID) ? (String) child.getProperty(Constants.PROPERTY_UUID) : null;
                    if (childUuid == null)
                        throw new InvalidArgumentException(String.format("The list type item with id %s does not have uuid", child.getId()));                        
                    return new BusinessObjectLight(listTypeClassName, childUuid, (String) child.getProperty(Constants.PROPERTY_NAME));
                }
                
            }
            throw new ApplicationObjectNotFoundException(String.format("A list type of class %s and name %s could not be found", listTypeClassName, listTypeItemName));
        }
    }
    
    @Override
    public List<ClassMetadataLight> getInstanceableListTypes()
            throws ApplicationObjectNotFoundException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node genericObjectListNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, Constants.CLASS_GENERICOBJECTLIST);

            if (genericObjectListNode == null)
                throw new ApplicationObjectNotFoundException("ClassGenericObjectList not found");
            
            String cypherQuery = "MATCH (classmetadata:classes) <-[:".concat(RelTypes.EXTENDS.toString()).concat("*]-(listType) ").concat(
                                 "WHERE classmetadata.name IN ['").concat(Constants.CLASS_GENERICOBJECTLIST).concat("']").concat(
                                 "RETURN listType ").concat(
                                 "ORDER BY listType.name ASC");    
                        
            List<ClassMetadataLight> res = new ArrayList<>();
            Result result = graphDb.execute(cypherQuery);
        
            Iterator<Node> n_column = result.columnAs("listType");
            
            for (Node node : Iterators.asIterable(n_column)) {
                if (!(Boolean)node.getProperty(Constants.PROPERTY_ABSTRACT))
                    res.add(Util.createClassMetadataLightFromNode(node));
            }
            return res;
        }
        
    }
    
    private Node getListTypeItemNode(String listTypeItemId, String listTypeItemClassName) 
        throws MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = graphDb.beginTx()) {
            
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, listTypeItemClassName);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found. Contact your administrator.", listTypeItemClassName));
            
            if (!Util.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, classNode))
                throw new InvalidArgumentException(String.format("Class %s is not a list type", listTypeItemClassName));
            
            Node listTypeItemNode = null;
            
            for (Relationship childRel : classNode.getRelationships(RelTypes.INSTANCE_OF)) {
                Node child = childRel.getStartNode();
                
                String childUuid = child.hasProperty(Constants.PROPERTY_UUID) ? (String) child.getProperty(Constants.PROPERTY_UUID) : null;
                if (childUuid == null)
                    throw new InvalidArgumentException(String.format("The list type item with id %s does not have uuid", child.getId()));
                
                if (childUuid.equals(listTypeItemId)) {
                    listTypeItemNode = child;
                    break;
                }
            }
            if (listTypeItemNode == null)
                throw new InvalidArgumentException(String.format("Can not find the list type item with id %s", listTypeItemId));
            
            tx.success();
            return listTypeItemNode;
        }
    }
    
    @Override
    public long createListTypeItemRelatedView(String listTypeItemId, String listTypeItemClassName, 
            String viewClassName, String name, String description, byte [] structure, byte [] background) throws MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            Node listTypeItemNode = getListTypeItemNode(listTypeItemId, listTypeItemClassName);
            if (listTypeItemNode == null)
                throw new InvalidArgumentException(String.format("Can not find the list type item with id %s", listTypeItemId));
            
            Node viewNode = graphDb.createNode(layoutLabel); // This is temporary. In the future, not all list type item related views will be device layouts
            viewNode.setProperty(Constants.PROPERTY_CLASS_NAME, viewClassName);
            listTypeItemNode.createRelationshipTo(viewNode, RelTypes.HAS_VIEW);
            
            if (name != null && !name.isEmpty())
                viewNode.setProperty(Constants.PROPERTY_NAME, name);
            else
                throw new InvalidArgumentException("The name of the view can not be null or empty");
            
            if (description != null)
                viewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            
            if (structure != null)
                viewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure);
            else
                viewNode.setProperty(Constants.PROPERTY_STRUCTURE, new byte[0]);
            
            if (background != null) {
                try {
                    String fileName = "view-" + listTypeItemId + "-" + viewNode.getId() + "-" + viewClassName;
                    Util.saveFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH), fileName, background);
                    viewNode.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                } catch(Exception ex){
                    throw new InvalidArgumentException(String.format("Background image for view %s could not be saved: %s",
                            listTypeItemId, ex.getMessage()));
                }
            }
            tx.success();
            return viewNode.getId();
        }
    }
    
    @Override
    public ChangeDescriptor updateListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId, 
        String name, String description, byte[] structure, byte[] background) 
        throws MetadataObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node listTypeItemNode = getListTypeItemNode(listTypeItemId, listTypeItemClass);
            if (listTypeItemNode == null)
                throw new InvalidArgumentException(String.format("Can not find the list type item with id %s", listTypeItemId));
            
            Node viewNode = null;
            for (Relationship rel : listTypeItemNode.getRelationships(RelTypes.HAS_VIEW, Direction.OUTGOING)){
                if (rel.getEndNode().getId() == viewId){
                    viewNode = rel.getEndNode();
                    break;
                }
            }
            if (viewNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The view with id %s could not be found", viewId)); 
                        
            String affectedProperties = "", oldValues = "", newValues = "";
            
            if (name != null) {
                oldValues +=  " " + viewNode.getProperty(Constants.PROPERTY_NAME);
                newValues += " " + name;
                affectedProperties += " " + Constants.PROPERTY_NAME;
                viewNode.setProperty(Constants.PROPERTY_NAME, name);
            }

            if (structure != null) {
                affectedProperties += " " + Constants.PROPERTY_STRUCTURE;
                viewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure);
            }

            if (description != null) {
                oldValues += " " + viewNode.getProperty(Constants.PROPERTY_DESCRIPTION);
                newValues += " " + description;
                affectedProperties += " " + Constants.PROPERTY_DESCRIPTION;
                viewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            }

            String fileName = "view-" + listTypeItemId + "-" + viewId + "-" + viewNode.getProperty(Constants.PROPERTY_CLASS_NAME);
            if (background != null){
                try{
                    affectedProperties += " " + Constants.PROPERTY_BACKGROUND;
                    Util.saveFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH), fileName, background);
                    viewNode.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                }catch(Exception ex){
                    throw new InvalidArgumentException(String.format("Background image for view %s couldn't be saved: %s",
                            listTypeItemId, ex.getMessage()));
                }
            }
            else {
                if (viewNode.hasProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME)){
                    try{
                        new File(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName).delete();
                    }catch(Exception ex){
                        throw new InvalidArgumentException(String.format("View background %s couldn't be deleted: %s", 
                                configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName, ex.getMessage()));
                    }
                    viewNode.removeProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                    affectedProperties += " " + Constants.PROPERTY_BACKGROUND;
                }
            }
            tx.success();
            return new ChangeDescriptor(affectedProperties, oldValues, newValues, null);
        }
    }
    
    @Override
    public ViewObject getListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId) 
        throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node listTypeItemNode = getListTypeItemNode(listTypeItemId, listTypeItemClass);
            if (listTypeItemNode == null)
                throw new InvalidArgumentException(String.format("Can not find the list type item with id %s", listTypeItemId));
            
            for (Relationship rel : listTypeItemNode.getRelationships(RelTypes.HAS_VIEW, Direction.OUTGOING)) {
                Node viewNode = rel.getEndNode();
                if (viewNode.getId() == viewId){
                    ViewObject res = new ViewObject(viewId,
                            viewNode.hasProperty(Constants.PROPERTY_NAME) ? (String)viewNode.getProperty(Constants.PROPERTY_NAME) : null,
                            viewNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? (String)viewNode.getProperty(Constants.PROPERTY_DESCRIPTION) : null,
                            (String)viewNode.getProperty(Constants.PROPERTY_CLASS_NAME));
                    if (viewNode.hasProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME)){
                        String fileName = (String)viewNode.getProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                        byte[] background = null;
                        try {
                            background = Util.readBytesFromFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName);
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                        }
                        res.setBackground(background);
                    }
                    if (viewNode.hasProperty(Constants.PROPERTY_STRUCTURE))
                        res.setStructure((byte[])viewNode.getProperty(Constants.PROPERTY_STRUCTURE));
                    return res;
                }
            }
        }
        throw new ApplicationObjectNotFoundException(String.format("The view with id %s could not be found", viewId));                
    }
    
    @Override        
    public List<ViewObjectLight> getListTypeItemRelatedViews(String listTypeItemId, String listTypeItemClass, int limit) 
        throws MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node listTypeItemNode = getListTypeItemNode(listTypeItemId, listTypeItemClass);
            if (listTypeItemNode == null)
                throw new InvalidArgumentException(String.format("Can not find the list type item with id %s", listTypeItemId));
            
            List<ViewObjectLight> res = new ArrayList();
            int i = 0;
            for (Relationship rel : listTypeItemNode.getRelationships(RelTypes.HAS_VIEW, Direction.OUTGOING)) {
                if (limit != -1) {
                    if (i < limit)
                        i++;
                    else
                        break;
                }
                Node viewNode = rel.getEndNode();
                res.add(new ViewObjectLight(viewNode.getId(),
                    viewNode.hasProperty(Constants.PROPERTY_NAME) ? (String)viewNode.getProperty(Constants.PROPERTY_NAME) : null,
                    viewNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? (String)viewNode.getProperty(Constants.PROPERTY_DESCRIPTION) : null,
                    (String) viewNode.getProperty(Constants.PROPERTY_CLASS_NAME)));
            }
            return res;
        }
    }
    
    @Override        
    public void deleteListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId) 
        throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node listTypeItemNode = getListTypeItemNode(listTypeItemId, listTypeItemClass);            
            if (listTypeItemNode == null)
                throw new InvalidArgumentException(String.format("Can not find the list type item with id %s", listTypeItemId));
            
            for (Relationship rel : listTypeItemNode.getRelationships(RelTypes.HAS_VIEW, Direction.OUTGOING)) {
                Node viewNode = rel.getEndNode();
                if (viewNode.getId() == viewId) {
                    rel.delete();
                    viewNode.delete();
                    tx.success();
                    return;
                }
            }
            tx.success();
        }
        throw new ApplicationObjectNotFoundException(String.format("The view with id %s could not be found", viewId)); 
    }
    
    @Override
    public List<BusinessObjectLight> getListTypeItemUses(String listTypeItemClass, String listTypeItemId, int limit) 
        throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            
            String cypherQuery = String.format("MATCH (ltItem:%s)<-[:%s]-(ltUser) WHERE ltItem._uuid = '%s' RETURN ltUser ORDER BY ltUser.name ASC %s", 
                    listTypeItemLabel, RelTypes.RELATED_TO, listTypeItemId, limit < 1 ? "" : "LIMIT " + limit);
            
            List<BusinessObjectLight> res = new ArrayList<>();
            Result result = graphDb.execute(cypherQuery);
        
            Iterator<Node> objectsThatUseListType = result.columnAs("ltUser");
            
            while (objectsThatUseListType.hasNext()) {
                Node node = objectsThatUseListType.next();
                if (node.hasRelationship(RelTypes.INSTANCE_OF))
                    res.add(createRemoteObjectLightFromNode(node));
                else if (node.hasRelationship(RelTypes.INSTANCE_OF_SPECIAL)) {
                    Node templateObjectClassNode = node.getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                    
                    String nodeUuid = node.hasProperty(Constants.PROPERTY_UUID) ? (String) node.getProperty(Constants.PROPERTY_UUID) : null;
                    if (nodeUuid == null)
                        throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", node.getId()));
                    
                    res.add(new BusinessObjectLight((String)templateObjectClassNode.getProperty(Constants.PROPERTY_NAME), 
                            nodeUuid, (String)node.getProperty(Constants.PROPERTY_NAME)));
                }
            }
            tx.success();
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getDeviceLayouts() throws InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            String columnName = "elements"; //NOI18N
            String cypherQuery = String.format(
                "MATCH (classNode)<-[r1:%s]-(templateElement)-[r2:%s]->(list)-[:%s]->(view) "
              + "WHERE r1.name=\"template\" AND r2.name=\"model\" "
              + "RETURN templateElement AS %s "
              + "ORDER BY templateElement.name ASC ", 
                 RelTypes.INSTANCE_OF_SPECIAL, RelTypes.RELATED_TO, RelTypes.HAS_VIEW, columnName);
            
            Result result = graphDb.execute(cypherQuery);
            Iterator<Node> column = result.columnAs(columnName);
            
            List<BusinessObjectLight> templateElements = new ArrayList();
            
            for (Node templateElementNode : Iterators.asIterable(column)) {
                Node templateObjectClassNode = templateElementNode.getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                
                String templateElementNodeUuid = templateElementNode.hasProperty(Constants.PROPERTY_UUID) ? (String) templateElementNode.getProperty(Constants.PROPERTY_UUID) : null;
                if (templateElementNodeUuid == null)
                    throw new InvalidArgumentException(String.format("The template with id %s does not have uuid", templateElementNode.getId()));                    
                                
                templateElements.add(new BusinessObjectLight((String)templateObjectClassNode.getProperty(Constants.PROPERTY_NAME), 
                    templateElementNodeUuid, (String)templateElementNode.getProperty(Constants.PROPERTY_NAME)));
            }
                
                        
            return templateElements;
        }
    }
    
    @Override
    public byte[] getDeviceLayoutStructure(String oid, String className) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName tagStructure = new QName("deviceLayoutStructure");
            xmlew.add(xmlef.createStartElement(tagStructure, null, null));
            
            QName tagDevice = new QName("device"); // NOI18N
            xmlew.add(xmlef.createStartElement(tagDevice, null, null));
            xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_ID), oid));
            xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_CLASS_NAME), className));
            addDeviceModelAsXML(oid, xmlew, xmlef);
            xmlew.add(xmlef.createEndElement(tagDevice, null));
            
            addDeviceNodeChildrenAsXml(oid, className, xmlew, xmlef);
            
            xmlew.add(xmlef.createEndElement(tagStructure, null));          
            
            xmlew.close();
            
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            Logger.getLogger(ApplicationEntityManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;            
        }
    }
            
    @Override
    public long createObjectRelatedView(String oid, String objectClass, String name, String description, String viewClassName, 
        byte[] structure, byte[] background) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        if (objectClass == null)
            throw new InvalidArgumentException("The root object can not be related to any view");
        
        long id;
        try(Transaction tx = graphDb.beginTx()) {
            Node instance = getInstanceOfClass(objectClass, oid);
            Node viewNode = graphDb.createNode();
            viewNode.setProperty(Constants.PROPERTY_CLASS_NAME, viewClassName);
            instance.createRelationshipTo(viewNode, RelTypes.HAS_VIEW);

            if (name != null)
                viewNode.setProperty(Constants.PROPERTY_NAME, name);

            if (structure != null)
                viewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure);

            if (background != null){
                try{
                    String fileName = "view-" + oid + "-" + viewNode.getId() + "-" + viewClassName;
                    Util.saveFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH), fileName, background);
                    viewNode.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                }catch(Exception ex){
                    throw new InvalidArgumentException(String.format("Background image for view %s could not be saved: %s",
                            oid, ex.getMessage()));
                }
            }

            if (description != null)
                viewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);

            tx.success();
            id = viewNode.getId();
        }
        return id;
    }

    @Override
    public long createGeneralView(String viewClass, String name, String description, byte[] structure, byte[] background)
            throws InvalidArgumentException {
        
        try(Transaction tx = graphDb.beginTx()) {
            
            Node newView = graphDb.createNode(generalViewsLabel);

            newView.setProperty(Constants.PROPERTY_CLASS_NAME, viewClass);
            if (name != null)
                newView.setProperty(Constants.PROPERTY_NAME, name);
            if (description != null)
                newView.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            if (structure != null)
                newView.setProperty(Constants.PROPERTY_STRUCTURE, structure);
            if (background != null){
                try{
                    String fileName = "view-" + newView.getId() + "-" + viewClass;
                    Util.saveFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH), fileName, background);
                    newView.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                }catch(Exception ex){
                    throw new InvalidArgumentException(String.format("Background image for view %s couldn't be saved: %s", 
                            newView.getId(), ex.getMessage()));
                }
            }
            tx.success();
            return newView.getId();
        }
    }

    @Override
    public ChangeDescriptor updateObjectRelatedView(String oid, String objectClass, long viewId, 
    String name, String description, byte[] structure, byte[] background)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (objectClass == null)
            throw new InvalidArgumentException("The root object does not have any view");
        
        
        try(Transaction tx = graphDb.beginTx()) {
            Node instance = getInstanceOfClass(objectClass, oid);
            String affectedProperties = "", oldValues = "", newValues = "";
            Node viewNode = null;
            for (Relationship rel : instance.getRelationships(RelTypes.HAS_VIEW, Direction.OUTGOING)){
                if (rel.getEndNode().getId() == viewId){
                    viewNode = rel.getEndNode();
                    break;
                }
            }

            if (viewNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The view with id %s could not be found", viewId)); //NOI18N

            if (name != null) {
                oldValues +=  " " + viewNode.getProperty(Constants.PROPERTY_NAME);
                newValues += " " + name;
                affectedProperties += " " + Constants.PROPERTY_NAME;
                viewNode.setProperty(Constants.PROPERTY_NAME, name);
            }

            if (structure != null) {
                affectedProperties += " " + Constants.PROPERTY_STRUCTURE;
                viewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure);
            }

            if (description != null) {
                oldValues += " " + viewNode.getProperty(Constants.PROPERTY_DESCRIPTION);
                newValues += " " + description;
                affectedProperties += " " + Constants.PROPERTY_DESCRIPTION;
                viewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            }

            String fileName = "view-" + oid + "-" + viewId + "-" + viewNode.getProperty(Constants.PROPERTY_CLASS_NAME);
            if (background != null){
                try{
                    affectedProperties += " " + Constants.PROPERTY_BACKGROUND;
                    Util.saveFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH), fileName, background);
                    viewNode.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                }catch(Exception ex){
                    throw new InvalidArgumentException(String.format("Background image for view %s couldn't be saved: %s",
                            oid, ex.getMessage()));
                }
            }
            else {
                if (viewNode.hasProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME)){
                    try{
                        new File(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName).delete();
                    }catch(Exception ex){
                        throw new InvalidArgumentException(String.format("View background %s couldn't be deleted: %s", 
                                configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName, ex.getMessage()));
                    }
                    viewNode.removeProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                    affectedProperties += " " + Constants.PROPERTY_BACKGROUND;
                }
            }
            tx.success();
            
            return new ChangeDescriptor(affectedProperties, oldValues, newValues, null);
            
        }
    }

    @Override
    public ChangeDescriptor updateGeneralView(long oid, String name, String description, byte[] structure, byte[] background)
            throws InvalidArgumentException, ApplicationObjectNotFoundException {
        
        try(Transaction tx = graphDb.beginTx()) {
            String affectedProperty = "", oldValue = "", newValue = ""; //NOI18N
            
            Node gView = Util.findNodeByLabelAndId(generalViewsLabel, oid);
            if (gView == null)
                throw new ApplicationObjectNotFoundException(String.format("View with id %s could not be found", oid));
            if (name != null) {
                affectedProperty += Constants.PROPERTY_NAME;
                oldValue += String.valueOf(gView.getProperty(Constants.PROPERTY_NAME));
                gView.setProperty(Constants.PROPERTY_NAME, name);
                newValue += name;
            }
            if (description != null) {
                affectedProperty += " " + Constants.PROPERTY_DESCRIPTION;
                oldValue += " " + String.valueOf(gView.getProperty(Constants.PROPERTY_DESCRIPTION));
                gView.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            }
            if (structure != null) {
                gView.setProperty(Constants.PROPERTY_STRUCTURE, structure);
                affectedProperty += " " + Constants.PROPERTY_STRUCTURE;
            }
            if (background != null){
                if (background.length != 0){
                    try{
                        String fileName = "view-" + oid + "-" + gView.getProperty(Constants.PROPERTY_CLASS_NAME);
                        Util.saveFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH), fileName, background);
                        gView.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                        affectedProperty += " " + Constants.PROPERTY_BACKGROUND;
                    }catch(Exception ex){
                        throw new InvalidArgumentException(String.format("Background image for view %s couldn't be saved: %s",
                                oid, ex.getMessage()));
                    }
                }
            }else {
                gView.removeProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                affectedProperty += " " + Constants.PROPERTY_BACKGROUND;
            }   
            tx.success();
            return new ChangeDescriptor(affectedProperty, oldValue, newValue ,null);
        }
    }

    @Override
    public void deleteGeneralViews(long[] ids) throws ApplicationObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx()) {
            for (long id : ids){
                Node gView = Util.findNodeByLabelAndId(generalViewsLabel, id);
                
                if (gView == null)
                    throw new ApplicationObjectNotFoundException(String.format("View with id %s could not be found", id));
                
                gView.delete();
            }
            tx.success();
        }
    }

    @Override
    public ViewObject getObjectRelatedView(String oid, String objectClass, long viewId)
            throws ApplicationObjectNotFoundException, BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node instance = getInstanceOfClass(objectClass, oid);

            for (Relationship rel : instance.getRelationships(RelTypes.HAS_VIEW, Direction.OUTGOING)){
                Node viewNode = rel.getEndNode();
                if (viewNode.getId() == viewId){
                    ViewObject res = new ViewObject(viewId,
                            viewNode.hasProperty(Constants.PROPERTY_NAME) ? (String)viewNode.getProperty(Constants.PROPERTY_NAME) : null,
                            viewNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? (String)viewNode.getProperty(Constants.PROPERTY_DESCRIPTION) : null,
                            (String)viewNode.getProperty(Constants.PROPERTY_CLASS_NAME));
                    if (viewNode.hasProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME)){
                        String fileName = (String)viewNode.getProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                        byte[] background = null;
                        try {
                            background = Util.readBytesFromFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName);
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                        }
                        res.setBackground(background);
                    }
                    if (viewNode.hasProperty(Constants.PROPERTY_STRUCTURE))
                        res.setStructure((byte[])viewNode.getProperty(Constants.PROPERTY_STRUCTURE));
                    return res;
                }
            }
        }
        throw new ApplicationObjectNotFoundException(String.format("View Object with id %s could not be found. It might have been deleted already", viewId));
    }

    @Override
    public List<ViewObjectLight> getObjectRelatedViews(String oid, String objectClass, int limit)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node instance = getInstanceOfClass(objectClass, oid);
            List<ViewObjectLight> res = new ArrayList<>();
            int i = 0;
            for (Relationship rel : instance.getRelationships(RelTypes.HAS_VIEW, Direction.OUTGOING)){
                if (limit != -1){
                    if (i < limit)
                        i++;
                    else break;
                }
                Node viewNode = rel.getEndNode();
                res.add(new ViewObjectLight(viewNode.getId(), 
                        viewNode.hasProperty(Constants.PROPERTY_NAME) ? (String)viewNode.getProperty(Constants.PROPERTY_NAME) : null,
                        viewNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? (String)viewNode.getProperty(Constants.PROPERTY_DESCRIPTION) : null,
                        (String)viewNode.getProperty(Constants.PROPERTY_CLASS_NAME)));
            }
            return res;
        }
    }

    @Override
    public List<ViewObjectLight> getGeneralViews(String viewClass, int limit) 
            throws InvalidArgumentException, NotAuthorizedException 
    {
        String cypherQuery = "MATCH (gView:" + Constants.LABEL_GENERAL_VIEWS + ") ";
        cypherQuery += " WHERE gView." + Constants.PROPERTY_CLASS_NAME + "='" + viewClass + "'";

        cypherQuery += " RETURN gView";

        if (limit != -1)
            cypherQuery += " LIMIT " + limit;
    
        try(Transaction tx = graphDb.beginTx()) {
            Result result = graphDb.execute(cypherQuery);
            Iterator<Node> gViews = result.columnAs("gView");
            List<ViewObjectLight> myRes = new ArrayList<>();
            while (gViews.hasNext()){
                Node gView = gViews.next();
                ViewObjectLight aView = new ViewObjectLight(gView.getId(), (String)gView.getProperty(Constants.PROPERTY_NAME),
                        (String)gView.getProperty(Constants.PROPERTY_DESCRIPTION), (String)gView.getProperty(Constants.PROPERTY_CLASS_NAME));
                if (gView.hasProperty(Constants.PROPERTY_NAME));
                    aView.setName((String)gView.getProperty(Constants.PROPERTY_NAME));
                if (gView.hasProperty(Constants.PROPERTY_DESCRIPTION));
                    aView.setDescription((String)gView.getProperty(Constants.PROPERTY_DESCRIPTION));

                myRes.add(aView);
            }
            return myRes;
        }
    }

    @Override
    public ViewObject getGeneralView(long viewId) throws ApplicationObjectNotFoundException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node gView = Util.findNodeByLabelAndId(generalViewsLabel, viewId);

            if (gView == null)
                throw new ApplicationObjectNotFoundException(String.format("View Object with id %s could not be found. It might have been deleted already", viewId));

            ViewObject aView = new ViewObject(gView.getId(),
                    gView.hasProperty(Constants.PROPERTY_NAME) ? (String)gView.getProperty(Constants.PROPERTY_NAME) : null,
                    gView.hasProperty(Constants.PROPERTY_DESCRIPTION) ? (String)gView.getProperty(Constants.PROPERTY_DESCRIPTION) : null,
                    (String)gView.getProperty(Constants.PROPERTY_CLASS_NAME));
            if (gView.hasProperty(Constants.PROPERTY_STRUCTURE))
                aView.setStructure((byte[])gView.getProperty(Constants.PROPERTY_STRUCTURE));
            if (gView.hasProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME)){
                String fileName = (String)gView.getProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                byte[] background = null;
                try {
                    background = Util.readBytesFromFile(configuration.getProperty("backgroundsPath", DEFAULT_BACKGROUNDS_PATH) + "/" + fileName);
                }catch(Exception e){
                    System.out.println("[KUWAIBA] " + e.getMessage());
                }
                aView.setBackground(background);
            }
            return aView;
        }
    }

    //Queries
    @Override
    public long createQuery(String queryName, long ownerOid, byte[] queryStructure,
            String description) throws ApplicationObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx()) {
            Node queryNode =  graphDb.createNode(queryLabel);
            queryNode.setProperty(CompactQuery.PROPERTY_QUERYNAME, queryName);
            if(description == null)
                description = "";
            queryNode.setProperty(CompactQuery.PROPERTY_DESCRIPTION, description);
            queryNode.setProperty(CompactQuery.PROPERTY_QUERYSTRUCTURE, queryStructure);
            queryNode.setProperty(CompactQuery.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            
            if(ownerOid != -1){
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, false);
                Node userNode = Util.findNodeByLabelAndId(userLabel, ownerOid);

                if(userNode != null)
                    userNode.createRelationshipTo(queryNode, RelTypes.OWNS_QUERY);
                else
                    throw new ApplicationObjectNotFoundException(String.format("User with id %s could not be found", ownerOid));
            }
            else
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, true);

            tx.success();
            return queryNode.getId();

        }
    }

    @Override
    public ChangeDescriptor saveQuery(long queryOid, String queryName, long ownerOid,
            byte[] queryStructure, String description) throws ApplicationObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx()) {
            Node queryNode = Util.findNodeByLabelAndId(queryLabel, queryOid);
            
            if(queryNode == null)
                throw new ApplicationObjectNotFoundException(String.format(
                        "Can not find the query with id %s", queryOid));
            String affectedProperties = "", oldValues = "", newValues = "", notes = "";

            queryNode.setProperty(CompactQuery.PROPERTY_QUERYNAME, queryName);
            affectedProperties += CompactQuery.PROPERTY_QUERYNAME;
            newValues += queryName;
            
            if(description != null) {
                queryNode.setProperty(CompactQuery.PROPERTY_DESCRIPTION, description);
                affectedProperties += " " + CompactQuery.PROPERTY_DESCRIPTION;
                newValues += " " + description;
            }
            
            queryNode.setProperty(CompactQuery.PROPERTY_QUERYSTRUCTURE, queryStructure);
            affectedProperties += " " + CompactQuery.PROPERTY_QUERYSTRUCTURE;
            
            if(ownerOid != -1) {
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, false);
                
                affectedProperties += " " + CompactQuery.PROPERTY_IS_PUBLIC;
                newValues += " " + "false";
                
                Node userNode = Util.findNodeByLabelAndId(userLabel, ownerOid);
                if(userNode == null)
                    throw new ApplicationObjectNotFoundException(String.format(
                                "Can not find the query with id %s", queryOid));

                Relationship singleRelationship = queryNode.getSingleRelationship(RelTypes.OWNS_QUERY, Direction.INCOMING);

                if(singleRelationship == null)
                    userNode.createRelationshipTo(queryNode, RelTypes.OWNS_QUERY);
            }
            else {
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, true);
                
                affectedProperties += " " + CompactQuery.PROPERTY_IS_PUBLIC;
                newValues += " " + "true";
            }
            tx.success();
            return new ChangeDescriptor(affectedProperties, oldValues, newValues, notes);
        }
    }

    @Override
    public void deleteQuery(long queryOid) throws ApplicationObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx())
        {
            Node queryNode = Util.findNodeByLabelAndId(queryLabel, queryOid);
            if(queryNode == null)
                throw new ApplicationObjectNotFoundException(String.format(
                        "Can not find the query with id %1s", queryOid));

            Iterable<Relationship> relationships = queryNode.getRelationships(RelTypes.OWNS_QUERY, Direction.INCOMING);
            for (Relationship relationship : relationships) {
                relationship.delete();
            }
            queryNode.delete();
            tx.success();
        }
    }

    @Override
    public List<CompactQuery> getQueries(boolean showPublic) {
        
        try(Transaction tx = graphDb.beginTx())
        {
            List<CompactQuery> queryList = new ArrayList<>();
            ResourceIterator<Node> queries = graphDb.findNodes(queryLabel);
            while (queries.hasNext()) {
                Node queryNode = queries.next();
                
                CompactQuery cq =  new CompactQuery();
                cq.setName((String)queryNode.getProperty(CompactQuery.PROPERTY_QUERYNAME));
                cq.setDescription((String)queryNode.getProperty(CompactQuery.PROPERTY_DESCRIPTION));
                cq.setContent((byte[])queryNode.getProperty(CompactQuery.PROPERTY_QUERYSTRUCTURE));
                cq.setIsPublic((Boolean)queryNode.getProperty(CompactQuery.PROPERTY_IS_PUBLIC));
                cq.setId(queryNode.getId());

                Relationship ownRelationship = queryNode.getSingleRelationship(RelTypes.OWNS_QUERY, Direction.INCOMING);

                if(ownRelationship != null){
                    Node ownerNode =  ownRelationship.getStartNode();
                    cq.setOwnerId(ownerNode.getId());
                }
                queryList.add(cq);
            }//end for
            return queryList;
        }
    }

    @Override
    public CompactQuery getQuery(long queryOid)
            throws ApplicationObjectNotFoundException {
        
        CompactQuery cq =  new CompactQuery();

        try(Transaction tx = graphDb.beginTx()) {
            Node queryNode = Util.findNodeByLabelAndId(queryLabel, queryOid);
            if (queryNode == null)
                 throw new ApplicationObjectNotFoundException(String.format(
                            "Can not find the query with id %s", queryOid));

            cq.setName((String)queryNode.getProperty(CompactQuery.PROPERTY_QUERYNAME));
            if(queryNode.hasProperty(CompactQuery.PROPERTY_DESCRIPTION))
                cq.setDescription((String)queryNode.getProperty(CompactQuery.PROPERTY_DESCRIPTION));
            
            cq.setContent((byte[])queryNode.getProperty(CompactQuery.PROPERTY_QUERYSTRUCTURE));
            cq.setIsPublic((Boolean)queryNode.getProperty(CompactQuery.PROPERTY_IS_PUBLIC));
            cq.setId(queryNode.getId());

            Relationship ownRelationship = queryNode.getSingleRelationship(RelTypes.OWNS_QUERY, Direction.INCOMING);

            if(ownRelationship != null){
                Node ownerNode =  ownRelationship.getStartNode();
                cq.setOwnerId(ownerNode.getId());
            }
            return cq;
        }
    }

    @Override
    public List<ResultRecord> executeQuery(ExtendedQuery query) throws MetadataObjectNotFoundException, InvalidArgumentException {
        try(Transaction tx = graphDb.beginTx()) {
            CypherQueryBuilder cqb = new CypherQueryBuilder();
            cqb.setClassNodes(getNodesFromQuery(query));
            cqb.createQuery(query);
            return cqb.getResultList();
        }
    }
    
    @Override
    public byte[] getClassHierachy(boolean showAll) 
            throws MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName qnameHierarchy = new QName("hierarchy");
            xmlew.add(xmlef.createStartElement(qnameHierarchy, null, null));
            xmlew.add(xmlef.createAttribute(new QName("documentVersion"), Constants.CLASS_HIERARCHY_NEXT_DOCUMENT_VERSION));
            xmlew.add(xmlef.createAttribute(new QName("serverVersion"), Constants.PERSISTENCE_SERVICE_VERSION));
            xmlew.add(xmlef.createAttribute(new QName("date"), Long.toString(Calendar.getInstance().getTimeInMillis())));
            
            QName qnameInventory = new QName("inventory");
            xmlew.add(xmlef.createStartElement(qnameInventory, null, null));
            
            QName qnameClasses = new QName("classes");
            xmlew.add(xmlef.createStartElement(qnameClasses, null, null));
            
            Node rootObjectNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, Constants.CLASS_ROOTOBJECT);
            if (rootObjectNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", Constants.CLASS_ROOTOBJECT));
            getXMLNodeForClass(rootObjectNode, xmlew, xmlef);
            
            xmlew.add(xmlef.createEndElement(qnameClasses, null));
            
            xmlew.add(xmlef.createEndElement(qnameInventory, null));
            
            xmlew.add(xmlef.createEndElement(qnameHierarchy, null));
            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            Logger.getLogger(ApplicationEntityManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    //Pools
    @Override
    public String createRootPool(String name, String description, String instancesOfClass, int type)
            throws MetadataObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx()) {
            Node poolNode = graphDb.createNode(poolLabel);
            String uuid = UUID.randomUUID().toString();
            poolNode.setProperty(Constants.PROPERTY_UUID, uuid);

            if (name != null)
                poolNode.setProperty(Constants.PROPERTY_NAME, name);
            if (description != null)
                poolNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);

            poolNode.setProperty(Constants.PROPERTY_TYPE, type);
            
            ClassMetadata classMetadata = cm.getClass(instancesOfClass);
            
            if (classMetadata == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", instancesOfClass));
            
            poolNode.setProperty(Constants.PROPERTY_CLASS_NAME, instancesOfClass);
                                                
            tx.success();
            return uuid;
        }
    }
    
    @Override
    public String createPoolInObject(String parentClassname, String parentId, String name, String description, String instancesOfClass, int type)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx()) {
            Node poolNode =  graphDb.createNode(poolLabel);
            String uuid = UUID.randomUUID().toString();
            poolNode.setProperty(Constants.PROPERTY_UUID, uuid);

            if (name != null)
                poolNode.setProperty(Constants.PROPERTY_NAME, name);
            if (description != null)
                poolNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            if (type != 0)
                    poolNode.setProperty(Constants.PROPERTY_TYPE, type);
            
            ClassMetadata classMetadata = cm.getClass(instancesOfClass);
            
            if (classMetadata == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", instancesOfClass));
            
            poolNode.setProperty(Constants.PROPERTY_CLASS_NAME, instancesOfClass);
            
            Node parentNode = graphDb.findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, parentId);
            if (parentNode == null)
                throw new BusinessObjectNotFoundException(parentClassname, parentId);
            
            poolNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);          
            
            tx.success();
            return uuid;
        }
    }
    
    @Override
    public String createPoolInPool(String parentId, String name, String description, String instancesOfClass, int type)
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node poolNode =  graphDb.createNode(poolLabel);
            String uuid = UUID.randomUUID().toString();
            poolNode.setProperty(Constants.PROPERTY_UUID, uuid);

            if (name != null)
                poolNode.setProperty(Constants.PROPERTY_NAME, name);
            if (description != null)
                poolNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            if (type != 0)
                    poolNode.setProperty(Constants.PROPERTY_TYPE, type);
            
            ClassMetadata classMetadata = cm.getClass(instancesOfClass);
            
            if (classMetadata == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", instancesOfClass));
            
            poolNode.setProperty(Constants.PROPERTY_CLASS_NAME, instancesOfClass);
            
            Node parentNode = graphDb.findNode(poolLabel, Constants.PROPERTY_UUID, parentId);
            
            if (parentNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A pool with id %s could not be found", parentId));
            
            poolNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);          
                        
            tx.success();
            return uuid;
        }
    }
        
    private void deletePool(String id) throws ApplicationObjectNotFoundException, OperationNotPermittedException {
        try(Transaction tx = graphDb.beginTx()) {
            Node poolNode = graphDb.findNode(poolLabel, Constants.PROPERTY_UUID, id);
            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A pool with id %s does not exist", id));

            deletePool(poolNode);
            
            tx.success();
        }
    }
    
    @Override
    public void deletePools(String[] ids) throws ApplicationObjectNotFoundException, OperationNotPermittedException {
        for (String id : ids)
            deletePool(id);
    }
    
    @Override
    public ChangeDescriptor setPoolProperties(String poolId, String name, String description) {
        try (Transaction tx = graphDb.beginTx()) {
            Node poolNode = graphDb.findNode(poolLabel, Constants.PROPERTY_UUID, poolId);
            String affectedProperties = "", oldValues = "", newValues = "";
            
            if(name != null) {
                oldValues += " " + (poolNode.hasProperty(Constants.PROPERTY_NAME) ? poolNode.getProperty(Constants.PROPERTY_NAME) : " ");
                poolNode.setProperty(Constants.PROPERTY_NAME, name);
                affectedProperties += " " + Constants.PROPERTY_NAME;                
                newValues += " " + name;   
            }
            if(description != null) {
                oldValues += " " + (poolNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? poolNode.getProperty(Constants.PROPERTY_DESCRIPTION) : " ");
                poolNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
                affectedProperties += " " + Constants.PROPERTY_DESCRIPTION;                
                newValues += " " + description;                
            }
            
            tx.success();
            return new ChangeDescriptor(affectedProperties, oldValues, newValues, String.format("Set %s pool properties", name));
        }
    }
    
    @Override
    public String getNameOfSpecialParentByScaleUp(String className, long id, int targetLevel) {
        try(Transaction tx = graphDb.beginTx()) {
            Node node0 = graphDb.getNodeById(id);
            return getNameOfSpecialParentByScaleUpRecursive(node0, targetLevel, 1);
        }
    }
    
    @Override
    public List<ActivityLogEntry> getBusinessObjectAuditTrail(String objectClass, String objectId, int limit) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try(Transaction tx = graphDb.beginTx()) {
            if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, objectClass))
                throw new InvalidArgumentException(String.format("Class %s is not subclass of %s",
                        objectClass, Constants.CLASS_INVENTORYOBJECT));
            Node instanceNode = getInstanceOfClass(objectClass, objectId);
            List<ActivityLogEntry> log = new ArrayList<>();
            int i = 0;
            for (Relationship rel : instanceNode.getRelationships(RelTypes.HAS_HISTORY_ENTRY)){
                if (limit != 0){
                    if (i < limit)
                        i++;
                    else
                        break;
                }
                Node logEntry = rel.getEndNode();
                log.add(new ActivityLogEntry(logEntry.getId(), instanceNode.getId(), (Integer)logEntry.getProperty(Constants.PROPERTY_TYPE), 
                        (String)logEntry.getSingleRelationship(RelTypes.PERFORMED_BY, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME), 
                        (Long)logEntry.getProperty(Constants.PROPERTY_CREATION_DATE), 
                        logEntry.hasProperty(Constants.PROPERTY_AFFECTED_PROPERTY) ? (String)logEntry.getProperty(Constants.PROPERTY_AFFECTED_PROPERTY) : null, 
                        logEntry.hasProperty(Constants.PROPERTY_OLD_VALUE) ? (String)logEntry.getProperty(Constants.PROPERTY_OLD_VALUE) :  null, 
                        logEntry.hasProperty(Constants.PROPERTY_NEW_VALUE) ? (String)logEntry.getProperty(Constants.PROPERTY_NEW_VALUE) : null, 
                        logEntry.hasProperty(Constants.PROPERTY_NOTES) ? (String)logEntry.getProperty(Constants.PROPERTY_NOTES) : null));
            }
            return log;
        }
    }
    
    @Override
    public List<ActivityLogEntry> getGeneralActivityAuditTrail(int page, int limit) {        
        try(Transaction tx = graphDb.beginTx()) {
            Node generalActivityLogNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG);

            String query = String.format("MATCH (n)<-[:%s]-(m)-[:%s]->(u) WHERE id(n) = %s RETURN m AS auditTrailEntry, u AS user ORDER BY n.creationDate DESC %s", 
                    RelTypes.CHILD_OF_SPECIAL, RelTypes.PERFORMED_BY, generalActivityLogNode.getId(), page == 0 || limit == 0 ? "" : "SKIP " + (page * limit - limit) + " LIMIT " + limit);
            
            Result result = graphDb.execute(query);
            
            List<ActivityLogEntry> log = new ArrayList<>();
            while (result.hasNext()) {
                Map<String, Object> resultEntry = result.next();
                Node logEntry = (Node)resultEntry.get("auditTrailEntry");
                Node user = (Node)resultEntry.get("user");
                
                log.add(new ActivityLogEntry(logEntry.getId(), 0, (Integer)logEntry.getProperty(Constants.PROPERTY_TYPE), 
                        (String)user.getProperty(Constants.PROPERTY_NAME), 
                        (Long)logEntry.getProperty(Constants.PROPERTY_CREATION_DATE), 
                        logEntry.hasProperty(Constants.PROPERTY_AFFECTED_PROPERTY) ? (String)logEntry.getProperty(Constants.PROPERTY_AFFECTED_PROPERTY) : null, 
                        logEntry.hasProperty(Constants.PROPERTY_OLD_VALUE) ? (String)logEntry.getProperty(Constants.PROPERTY_OLD_VALUE) :  null, 
                        logEntry.hasProperty(Constants.PROPERTY_NEW_VALUE) ? (String)logEntry.getProperty(Constants.PROPERTY_NEW_VALUE) : null, 
                        logEntry.hasProperty(Constants.PROPERTY_NOTES) ? (String)logEntry.getProperty(Constants.PROPERTY_NOTES) : null));
            }
            return log;
        }
    }
    
    @Override
    public void validateWebServiceCall(String methodName, String ipAddress, String sessionId)
            throws NotAuthorizedException {
        Session aSession = sessions.get(sessionId);
        
        if(aSession == null)
                throw new NotAuthorizedException("Invalid session");
        
        if (!aSession.getIpAddress().equals(ipAddress))
            throw new NotAuthorizedException(String.format("The IP %s does not match the one registered for this session", ipAddress));

//        We won't be using this for now, since the desktop client still uses the web service. This will work as of version 2.0        
//        if (aSession.getUser().getType() != UserProfile.USER_TYPE_WEB_SERVICE)
//            throw new NotAuthorizedException(String.format("The user %s is not authorized to call web service methods", aSession.getUser().getUserName()));
        
//        for (Privilege privilege : aSession.getUser().getPrivileges()) { //The featureToken for web service users is the method name itself
//            if (methodName.equals(privilege.getFeatureToken()))
//                return;
//        }
//                
//        throw new NotAuthorizedException(String.format("The user %s is not authorized to call web service method %s", aSession.getUser().getUserName(), methodName));
            
    }
    
    @Override
    public Session createSession(String userName, String password, int sessionType, String IPAddress) throws ApplicationObjectNotFoundException, NotAuthorizedException {
        if (userName == null || password == null)
            throw  new ApplicationObjectNotFoundException("User or Password must not be null or empty");
        
        try(Transaction tx = graphDb.beginTx()) {
            Node userNode = graphDb.findNode(userLabel, Constants.PROPERTY_NAME, userName);

            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The user %s does not exist", userName));

            if (!(Boolean)userNode.getProperty(Constants.PROPERTY_ENABLED))
                throw new NotAuthorizedException(String.format("The user %s is not enabled", userName));

            if (BCrypt.checkpw(password, (String)userNode.getProperty(Constants.PROPERTY_PASSWORD))){
                UserProfile user = Util.createUserProfileWithGroupPrivilegesFromNode(userNode);

                for (Session aSession : sessions.values()){
                    if (aSession.getUser().getUserName().equals(userName) 
                            && aSession.getSessionType() == sessionType) { //Multiple sessions withe the same user are allowed as long as they have a different type (e.g. one mobile session and the other web session)
                        sessions.remove(aSession.getToken());
                        break;
                    }
                }
                Session newSession = new Session(user, IPAddress, sessionType);
                sessions.put(newSession.getToken(), newSession);
                cm.putUser(user);
                return newSession;
            } else
                throw new NotAuthorizedException("User or password incorrect");
        }
    }
    
    @Override
    public UserProfile getUserInSession(String sessionId) {
        return sessions.get(sessionId).getUser();
    }
    
    @Override
    public List<UserProfile> getUsersInGroup(long groupId) throws ApplicationObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx()) {
            Node groupNode = Util.findNodeByLabelAndId(groupLabel, groupId);
            if (groupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Group with id %s could not be found", groupId));

            List<UserProfile> usersInGroup = new ArrayList<>();
            for (Relationship userInGroupRelationship : groupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_GROUP))
                usersInGroup.add(Util.createUserProfileWithoutGroupPrivilegesFromNode(userInGroupRelationship.getStartNode()));

            return usersInGroup;
        }
    }
    
    @Override
    public List<GroupProfileLight> getGroupsForUser(long userId) throws ApplicationObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx()) {
            Node userNode = Util.findNodeByLabelAndId(userLabel, userId);
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("User with id %s could not be found", userId));

            List<GroupProfileLight> groupsForUser = new ArrayList<>();
            for (Relationship groupForUserRelationship : userNode.getRelationships(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP))
                groupsForUser.add(Util.createGroupProfileLightFromNode(groupForUserRelationship.getEndNode()));

            return groupsForUser;
        }
    }

    @Override
    public void closeSession(String sessionId, String remoteAddress) throws NotAuthorizedException {
        Session aSession = sessions.get(sessionId);
        if (aSession == null)
            throw new NotAuthorizedException("Invalid session ID");
        if (!aSession.getIpAddress().equals(remoteAddress))
            throw new NotAuthorizedException(String.format("The IP %s does not match with the one registered for this session", remoteAddress));
        sessions.remove(sessionId);
    }
            
    @Override
    public void setConfiguration (Properties properties) {
        this.configuration = properties;
    }

    @Override
    public Properties getConfiguration () {
        return this.configuration;
    }

    @Override
    public void createGeneralActivityLogEntry(String userName, int type, String notes) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node generalActivityLogNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG);

            if (generalActivityLogNode == null)
                throw new ApplicationObjectNotFoundException("The general activity log node can not be found. The database could be corrupted");

            Util.createActivityLogEntry(null, generalActivityLogNode, userName, type,
                    Calendar.getInstance().getTimeInMillis(), null, null, null, notes);

            tx.success();        
        }
    }
    
    @Override
    public void createGeneralActivityLogEntry(String userName, int type, ChangeDescriptor changeDescriptor) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {

            Node generalActivityLogNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG);

            if (generalActivityLogNode == null)
                throw new ApplicationObjectNotFoundException("The general activity log node can not be found. The database could be corrupted");

            Util.createActivityLogEntry(null, generalActivityLogNode, userName, type,
                    Calendar.getInstance().getTimeInMillis(), changeDescriptor.getAffectedProperties(), 
                    changeDescriptor.getOldValues(), changeDescriptor.getNewValues(), changeDescriptor.getNotes());

            tx.success();  
        }
    }
    
    @Override
    public void createObjectActivityLogEntry(String userName, String className, String oid, int type, 
        String affectedProperties, String oldValues, String newValues, String notes) throws ApplicationObjectNotFoundException, BusinessObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node objectNode = graphDb.findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, oid);
            
            if (objectNode == null)
                throw new BusinessObjectNotFoundException(className, oid);
            
            Node activityNode =  graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_OBJECT_ACTIVITY_LOG);
            if (activityNode == null)
                throw new ApplicationObjectNotFoundException("The object activity log node can not be found. The database could be corrupted");

            Util.createActivityLogEntry(objectNode, activityNode, userName, type, Calendar.getInstance().getTimeInMillis(), affectedProperties, oldValues, newValues, notes);

            tx.success();
        }
    }
    
    @Override
    public void createObjectActivityLogEntry(String userName, String className, String oid,  
            int type, ChangeDescriptor changeDescriptor) throws ApplicationObjectNotFoundException, BusinessObjectNotFoundException {
        createObjectActivityLogEntry(userName, className, oid, type, changeDescriptor.getAffectedProperties(), changeDescriptor.getOldValues(), changeDescriptor.getNewValues(), changeDescriptor.getNotes());
    }
    
    @Override
    public long createTask(String name, String description, boolean enabled, boolean commitOnExecute, String script, 
            List<StringPair> parameters, TaskScheduleDescriptor schedule, TaskNotificationDescriptor notificationType) {
        try(Transaction tx = graphDb.beginTx()) {
            Node taskNode = graphDb.createNode(taskLabel);
            taskNode.setProperty(Constants.PROPERTY_NAME, name == null ? "" : name);
            taskNode.setProperty(Constants.PROPERTY_DESCRIPTION, description == null ? "" : description);
            taskNode.setProperty(Constants.PROPERTY_COMMIT_ON_EXECUTE, commitOnExecute);
            taskNode.setProperty(Constants.PROPERTY_ENABLED, enabled);
            
            if (script != null)
                taskNode.setProperty(Constants.PROPERTY_SCRIPT, script);
            
            for (StringPair parameter : parameters) //param names have the prefic PARAM_ to identify them from the other properties
                taskNode.setProperty("PARAM_" + parameter.getKey(), parameter.getValue());
            
            if (schedule != null) {
                taskNode.setProperty(Constants.PROPERTY_EXECUTION_TYPE, schedule.getExecutionType());
                taskNode.setProperty(Constants.PROPERTY_EVERY_X_MINUTES, schedule.getEveryXMinutes());
                taskNode.setProperty(Constants.PROPERTY_START_TIME, schedule.getStartTime());
            }
            
            if (notificationType != null) {
                taskNode.setProperty(Constants.PROPERTY_NOTIFICATION_TYPE, notificationType.getNotificationType());
                taskNode.setProperty(Constants.PROPERTY_EMAIL, notificationType.getEmail() ==  null ? "" : notificationType.getEmail());
            }
                        
            tx.success();
            return taskNode.getId();
        }
    }

    @Override
    public ChangeDescriptor updateTaskProperties(long taskId, String propertyName, String propertyValue) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(taskLabel, taskId);
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));
            String affectedProperty, oldValue, newValue;
            affectedProperty = propertyName;
            oldValue = String.valueOf(taskNode.hasProperty(propertyName) ? taskNode.getProperty(propertyName) : " ");

            switch (propertyName) {
                case Constants.PROPERTY_NAME:
                case Constants.PROPERTY_DESCRIPTION:
                case Constants.PROPERTY_SCRIPT:
                    taskNode.setProperty(propertyName, propertyValue);
                    break;
                case Constants.PROPERTY_ENABLED:
                case Constants.PROPERTY_COMMIT_ON_EXECUTE:
                    taskNode.setProperty(propertyName, Boolean.valueOf(propertyValue));
                    break;
                default:
                    throw new InvalidArgumentException(String.format("%s is not a valid task property", propertyName));
            }
            String taskName = taskNode.hasProperty(Constants.PROPERTY_NAME) ? (String) taskNode.getProperty(Constants.PROPERTY_NAME) : " ";
            newValue = propertyValue;
            tx.success();
            return new ChangeDescriptor(affectedProperty, oldValue, newValue, 
                String.format("Updated properties in Task with name %s and id %s ", taskName, taskId));
        }
    }

    @Override
    public ChangeDescriptor updateTaskParameters(long taskId, List<StringPair> parameters) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(taskLabel, taskId);
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));
            String affectedProperties = "", oldValues = "", newValues = "";

            for (StringPair parameter : parameters) {
                String actualParameterName = "PARAM_" + parameter.getKey();
                //The parameters are stored with a prefix PARAM_
                //params set to null, must be deleted
                if (taskNode.hasProperty(actualParameterName) && parameter.getValue() == null)
                    taskNode.removeProperty(actualParameterName);
                else {                    
                    oldValues += " " + (taskNode.hasProperty(actualParameterName) ? taskNode.getProperty(actualParameterName) : " ");
                    
                    taskNode.setProperty(actualParameterName, parameter.getValue());
                    
                    affectedProperties += " " + parameter.getKey();
                    newValues += " " + parameter.getValue();
                }
            }
            String taskName = taskNode.hasProperty(Constants.PROPERTY_NAME) ? (String) taskNode.getProperty(Constants.PROPERTY_NAME) : " ";
            tx.success();
            return new ChangeDescriptor(affectedProperties, oldValues, newValues, 
                String.format("Updated parameters in Task with name %s and id %s ", taskName, taskId));
        }
    }

    @Override
    public ChangeDescriptor updateTaskSchedule(long taskId, TaskScheduleDescriptor schedule) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(taskLabel, taskId);
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));
            String affectedProperties = "", oldValues = "", newValues = "";
            
            affectedProperties += " " + Constants.PROPERTY_EXECUTION_TYPE;
            oldValues += " " + (taskNode.hasProperty(Constants.PROPERTY_EXECUTION_TYPE) ? taskNode.getProperty(Constants.PROPERTY_EXECUTION_TYPE) : " ");
            taskNode.setProperty(Constants.PROPERTY_EXECUTION_TYPE, schedule.getExecutionType());
            newValues += " " + schedule.getExecutionType();
            
            affectedProperties += " " + Constants.PROPERTY_EVERY_X_MINUTES;
            oldValues += " " + (taskNode.hasProperty(Constants.PROPERTY_EVERY_X_MINUTES) ? taskNode.getProperty(Constants.PROPERTY_EVERY_X_MINUTES) : " ");
            taskNode.setProperty(Constants.PROPERTY_EVERY_X_MINUTES, schedule.getEveryXMinutes());
            newValues += " " + schedule.getEveryXMinutes();
            
            affectedProperties += " " + Constants.PROPERTY_START_TIME;
            oldValues += " " + (taskNode.hasProperty(Constants.PROPERTY_START_TIME) ? taskNode.getProperty(Constants.PROPERTY_START_TIME) : " ");
            taskNode.setProperty(Constants.PROPERTY_START_TIME, schedule.getStartTime());
            newValues += " " + schedule.getStartTime();
            
            String taskName = taskNode.hasProperty(Constants.PROPERTY_NAME) ? (String) taskNode.getProperty(Constants.PROPERTY_NAME) : " ";
            tx.success();
            return new ChangeDescriptor(affectedProperties, oldValues, newValues, 
                String.format("Updated schedule in Task with name %s and id %s ", taskName, taskId));
        }
    }

    @Override
    public ChangeDescriptor updateTaskNotificationType(long taskId, TaskNotificationDescriptor notificationType) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(taskLabel, taskId);
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));
            String affectedProperties = "", oldValues = "", newValues = "";
            
            affectedProperties += " " + Constants.PROPERTY_NOTIFICATION_TYPE;
            oldValues += " " + (taskNode.hasProperty(Constants.PROPERTY_NOTIFICATION_TYPE) ? taskNode.getProperty(Constants.PROPERTY_NOTIFICATION_TYPE) : " ");
            taskNode.setProperty(Constants.PROPERTY_NOTIFICATION_TYPE, notificationType.getNotificationType());
            newValues += " " + notificationType.getNotificationType();
            
            affectedProperties += " " + Constants.PROPERTY_EMAIL;
            oldValues += " " + (taskNode.hasProperty(Constants.PROPERTY_EMAIL) ? taskNode.getProperty(Constants.PROPERTY_EMAIL) : " ");
            taskNode.setProperty(Constants.PROPERTY_EMAIL, notificationType.getEmail() == null ? "" : notificationType.getEmail());
            newValues += " " + notificationType.getEmail() == null ? "" : notificationType.getEmail();
            
            String taskName = taskNode.hasProperty(Constants.PROPERTY_NAME) ? (String) taskNode.getProperty(Constants.PROPERTY_NAME) : " ";
            tx.success();
            return new ChangeDescriptor(affectedProperties, oldValues, newValues, 
                String.format("Updated notification type in Task with name %s and id %s ", taskName, taskId));
        }
    }

    @Override
    public void deleteTask(long taskId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(taskLabel, taskId);
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));
                        
            for (Relationship rel : taskNode.getRelationships()) //Unsubscribe users
                rel.delete();
            
            taskNode.delete();
            tx.success();
        }
    }

    @Override
    public ChangeDescriptor subscribeUserToTask(long userId, long taskId) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(taskLabel, taskId);
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));
            
            boolean found = false;
            
            for (Relationship rel : taskNode.getRelationships(Direction.INCOMING, RelTypes.SUBSCRIBED_TO)) {
                if (rel.getStartNode().getId() == userId) {
                    found = true;
                    break;
                }
            }
            
            if (found)
                throw new InvalidArgumentException("This user is already subscribed to the task");
                        
            Node userNode = Util.findNodeByLabelAndId(userLabel, userId);
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A user with id %s could not be found", userId));
            
            Relationship rel = userNode.createRelationshipTo(taskNode, RelTypes.SUBSCRIBED_TO);
            rel.setProperty(Constants.PROPERTY_NAME, "task"); //NOI18N
            
            String taskName = taskNode.hasProperty(Constants.PROPERTY_NAME) ? (String) taskNode.getProperty(Constants.PROPERTY_NAME) : "";
            String userName = userNode.hasProperty(Constants.PROPERTY_NAME) ? (String) userNode.getProperty(Constants.PROPERTY_NAME) : "";
            tx.success();
            return new ChangeDescriptor("", "", "", String.format("Subscribed user %s to task %s", userName, taskName));
        }
    }

    @Override
    public ChangeDescriptor unsubscribeUserFromTask(long userId, long taskId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(taskLabel, taskId);
            
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));
            
            boolean found = false;
            String userName = null;
            
            for (Relationship rel : taskNode.getRelationships(Direction.INCOMING, RelTypes.SUBSCRIBED_TO)) {
                if (rel.getStartNode().getId() == userId) {
                    userName = rel.getStartNode().hasProperty(Constants.PROPERTY_NAME) ? (String) rel.getStartNode().getProperty(Constants.PROPERTY_NAME) : "";
                    rel.delete();
                    found = true;
                    break;
                }
            }
            
            if (!found)
                throw new ApplicationObjectNotFoundException(String.format("A user with id %s could not be found", taskId));
            String taskName = taskNode.hasProperty(Constants.PROPERTY_NAME) ? (String) taskNode.getProperty(Constants.PROPERTY_NAME) : "";            
            tx.success();
            return new ChangeDescriptor("", "", "", String.format("Unsubscribed user %s from task %s", userName, taskName));
        }
    }

    @Override
    public Task getTask(long taskId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(taskLabel, taskId);
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));
                        
            return Util.createTaskFromNode(taskNode);
        }
    }
    
    @Override
    public List<UserInfoLight> getSubscribersForTask(long taskId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(taskLabel, taskId);
            
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));
                
            List<UserInfoLight> subscribers = new ArrayList<>();
            for (Relationship rel : taskNode.getRelationships(Direction.INCOMING, RelTypes.SUBSCRIBED_TO)) {
                Node userNode = rel.getStartNode();
                subscribers.add(new UserInfoLight(userNode.getId(), (String)userNode.getProperty(Constants.PROPERTY_NAME)));
            }
            
            return subscribers;
        }
    }

    @Override
    public List<Task> getTasks() {
        try (Transaction tx = graphDb.beginTx()) {
            
            ResourceIterator<Node> taskNodes = graphDb.findNodes(taskLabel);
            List<Task> allTasks = new ArrayList<>();
            
            while (taskNodes.hasNext()) {
                Node taskNode = taskNodes.next();
                allTasks.add(Util.createTaskFromNode(taskNode));
            }
            return allTasks;
        }
    }

    @Override
    public List<Task> getTasksForUser(long userId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node userNode = Util.findNodeByLabelAndId(userLabel, userId);
            
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The user with id %s could not be found", userId));
            
            List<Task> allTasks = new ArrayList<>();
            
            for (Relationship rel : userNode.getRelationships(Direction.OUTGOING, RelTypes.SUBSCRIBED_TO)) {
                Node taskNode = rel.getEndNode();

                allTasks.add(Util.createTaskFromNode(taskNode));
            }
            
            return allTasks;
        }
    }
    
    @Override
    public TaskResult executeTask(long taskId) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        
        Binding environmentParameters = new Binding();
        
        try (Transaction tx = graphDb.beginTx()) {
            
            Node taskNode = Util.findNodeByLabelAndId(taskLabel, taskId);
            
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));
            if (!taskNode.hasProperty(Constants.PROPERTY_SCRIPT))
                throw new InvalidArgumentException(String.format("The task with id %s does not have a script", taskId));
            
            if (!(boolean)taskNode.getProperty(Constants.PROPERTY_ENABLED))
                throw new InvalidArgumentException("This task can not be executed because it's disabled");
            
            String script = (String)taskNode.getProperty(Constants.PROPERTY_SCRIPT);
            
            Iterable<String> allProperties = taskNode.getPropertyKeys();
            HashMap<String, String> scriptParameters = new HashMap<>();
            for (String property : allProperties) {
                if (property.startsWith("PARAM_"))
                    scriptParameters.put(property.replace("PARAM_", ""), (String)taskNode.getProperty(property));
            }
            
            environmentParameters.setVariable("graphDb", graphDb); //NOI18N
            environmentParameters.setVariable("inventoryObjectLabel", inventoryObjectLabel);
            environmentParameters.setVariable("classLabel", classLabel); //NOI18N
            environmentParameters.setVariable("TaskResult", TaskResult.class); //NOI18N
            environmentParameters.setVariable("Constants", Constants.class); //NOI18N
            environmentParameters.setVariable("Direction", Direction.class); //NOI18N
            environmentParameters.setVariable("RelTypes", RelTypes.class); //NOI18N
            environmentParameters.setVariable("scriptParameters", scriptParameters); //NOI18N
         
            GroovyShell shell = new GroovyShell(ApplicationEntityManager.class.getClassLoader(), environmentParameters);
            Object theResult = shell.evaluate(script);

            if (theResult == null)
                throw new InvalidArgumentException("The script returned a null object. Please check the syntax.");
            else if (!TaskResult.class.isInstance(theResult))
                throw new InvalidArgumentException("The script does not return a TaskResult object. Please check the return value.");
            //Commit only if it's configured to do so 
            if (taskNode.hasProperty(Constants.PROPERTY_COMMIT_ON_EXECUTE) && (boolean)taskNode.getProperty(Constants.PROPERTY_COMMIT_ON_EXECUTE))
                tx.success();

            return (TaskResult)theResult;

        } catch(Exception ex) {
            return TaskResult.createErrorResult(ex.getMessage());
        }
    }
    //Templates
    @Override
    public String createTemplate(String templateClass, String templateName) throws MetadataObjectNotFoundException, OperationNotPermittedException {  
        try (Transaction tx = graphDb.beginTx()) {
            
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, templateClass);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", templateClass));
            
            if (classNode.hasProperty(Constants.PROPERTY_ABSTRACT) && (boolean)classNode.getProperty(Constants.PROPERTY_ABSTRACT))
                throw new OperationNotPermittedException(String.format("Abstract class %s can not have templates", templateClass));
            
            String uuid = UUID.randomUUID().toString();
            Node templateNode = graphDb.createNode(templateLabel, templateElementLabel); // The template root object is also a template element
            templateNode.setProperty(Constants.PROPERTY_NAME, templateName == null ? "" : templateName);
            templateNode.setProperty(Constants.PROPERTY_UUID, uuid);
                        
            classNode.createRelationshipTo(templateNode, RelTypes.HAS_TEMPLATE);
            Relationship specialInstanceRelationship = templateNode.createRelationshipTo(classNode, RelTypes.INSTANCE_OF_SPECIAL);
            specialInstanceRelationship.setProperty(Constants.PROPERTY_NAME, "template"); //NOI18N
            
            tx.success();
            return uuid;
        }
    }

    @Override
    public String createTemplateElement(String templateElementClass, String templateElementParentClassName, String templateElementParentId, String templateElementName) throws 
        MetadataObjectNotFoundException, ApplicationObjectNotFoundException, OperationNotPermittedException {
        
        boolean isPossibleChildren = false;
        for (ClassMetadataLight possibleChildren : mem.getPossibleChildren(templateElementParentClassName)) {
            if (possibleChildren.getName().equals(templateElementClass)) {
                isPossibleChildren = true;
                break;
            }
        }
        if (!isPossibleChildren) 
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", templateElementClass, templateElementParentClassName == null ? Constants.NODE_DUMMYROOT : templateElementParentClassName));
        
        try (Transaction tx = graphDb.beginTx()) {
            
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, templateElementClass);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", templateElementClass));
            
            Node parentClassNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, templateElementParentClassName);
            if (parentClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Parent class %s can not be found", templateElementParentClassName));
            
            if (classNode.hasProperty(Constants.PROPERTY_ABSTRACT) && (boolean)classNode.getProperty(Constants.PROPERTY_ABSTRACT))
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", templateElementClass));
            
            Node parentNode = null;
            
            for(Relationship instanceOfSpecialRelationship : parentClassNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF_SPECIAL)) {
                if (templateElementParentId.equals(instanceOfSpecialRelationship.getStartNode().getProperty(Constants.PROPERTY_UUID))) {
                    parentNode = instanceOfSpecialRelationship.getStartNode();
                    break;
                }
            }
            
            if (parentNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Parent object %s of class %s not found", templateElementParentId, templateElementParentClassName));
            
            String uuid = UUID.randomUUID().toString();
            Node templateObjectNode = graphDb.createNode(templateElementLabel);
            templateObjectNode.setProperty(Constants.PROPERTY_NAME, templateElementName == null ? "" : templateElementName);
            templateObjectNode.setProperty(Constants.PROPERTY_UUID, uuid);
            
            templateObjectNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF);
            
            Relationship specialInstanceRelationship = templateObjectNode.createRelationshipTo(classNode, RelTypes.INSTANCE_OF_SPECIAL);
            specialInstanceRelationship.setProperty(Constants.PROPERTY_NAME, "template"); //NOI18N 
            
            tx.success();
            return uuid;
        }
    }
    
    @Override    
    public String createTemplateSpecialElement(String tsElementClass, String tsElementParentClassName, String tsElementParentId, String tsElementName) 
        throws OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        
        boolean isPossibleSpecialChildren = false;
        for (ClassMetadataLight  possibleSpecialChildren : mem.getPossibleSpecialChildren(tsElementParentClassName)) {
            if (possibleSpecialChildren.getName().equals(tsElementClass)) {
                isPossibleSpecialChildren = true;
                break;
            }
        }
        if (!isPossibleSpecialChildren)
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as special child of %s", tsElementClass, tsElementParentClassName == null ? Constants.NODE_DUMMYROOT : tsElementParentClassName));
            
        try (Transaction tx = graphDb.beginTx()) {
            
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, tsElementClass);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", tsElementClass));
                        
            Node parentClassNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, tsElementParentClassName);
            if (parentClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Parent class %s can not be found", tsElementParentClassName));
            
            if (classNode.hasProperty(Constants.PROPERTY_ABSTRACT) && (boolean)classNode.getProperty(Constants.PROPERTY_ABSTRACT))
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", tsElementClass));
            
            Node parentNode = null;
            
            for(Relationship instanceOfSpecialRelationship : parentClassNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF_SPECIAL)) {
                if (tsElementParentId.equals(instanceOfSpecialRelationship.getStartNode().getProperty(Constants.PROPERTY_UUID))) {
                    parentNode = instanceOfSpecialRelationship.getStartNode();
                    break;
                }
            }
            
            if (parentNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Parent object %s of class %s not found", tsElementParentId, tsElementParentClassName));
            
            String uuid = UUID.randomUUID().toString();
            Node templateObjectNode = graphDb.createNode(templateElementLabel);
            templateObjectNode.setProperty(Constants.PROPERTY_NAME, tsElementName == null ? "" : tsElementName);
            templateObjectNode.setProperty(Constants.PROPERTY_UUID, uuid);
            
            templateObjectNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);
            
            Relationship specialInstanceRelationship = templateObjectNode.createRelationshipTo(classNode, RelTypes.INSTANCE_OF_SPECIAL);
            specialInstanceRelationship.setProperty(Constants.PROPERTY_NAME, "template"); //NOI18N 
            
            tx.success();
            return uuid;
        }
    }
    
    @Override
    public String[] createBulkTemplateElement(String templateElementClassName, String templateElementParentClassName, 
            String templateElementParentId, int numberOfTemplateElements, String templateElementNamePattern) 
        throws MetadataObjectNotFoundException, OperationNotPermittedException, ApplicationObjectNotFoundException, InvalidArgumentException {
        
        boolean isPossibleChildren = false;
        for (ClassMetadataLight possibleChildren : mem.getPossibleChildren(templateElementParentClassName)) {
            if (possibleChildren.getName().equals(templateElementClassName)) {
                isPossibleChildren = true;
                break;
            }
        }
        if (!isPossibleChildren) 
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", templateElementClassName, templateElementParentClassName == null ? Constants.NODE_DUMMYROOT : templateElementParentClassName));
        
        try (Transaction tx = graphDb.beginTx()) {
            
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, templateElementClassName);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", templateElementClassName));
            
            Node parentClassNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, templateElementParentClassName);
            if (parentClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Parent class %s can not be found", templateElementParentClassName));
            
            if (classNode.hasProperty(Constants.PROPERTY_ABSTRACT) && (boolean)classNode.getProperty(Constants.PROPERTY_ABSTRACT))
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", templateElementClassName));
            
            Node parentNode = null;
            
            for(Relationship instanceOfSpecialRelationship : parentClassNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF_SPECIAL)) {
                if (templateElementParentId.equals(instanceOfSpecialRelationship.getStartNode().getProperty(Constants.PROPERTY_UUID))) {
                    parentNode = instanceOfSpecialRelationship.getStartNode();
                    break;
                }
            }
            if (parentNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Parent object %s of class %s not found", templateElementParentId, templateElementParentClassName));
            
            DynamicName dynamicName = new DynamicName(templateElementNamePattern);
            if (dynamicName.getNumberOfDynamicNames() < numberOfTemplateElements) 
                throw new InvalidArgumentException("The given pattern to generate the name has "
                        + "less possibilities than the number of objects to be created");
            
            String res[] = new String[numberOfTemplateElements];
            
            for (int i = 0; i < numberOfTemplateElements; i++) {
                String templateElementName = dynamicName.getDynamicNames().get(i);
                
                String uuid = UUID.randomUUID().toString();
                Node templateObjectNode = graphDb.createNode(templateElementLabel);
                templateObjectNode.setProperty(Constants.PROPERTY_NAME, templateElementName == null ? "" : templateElementName);
                templateObjectNode.setProperty(Constants.PROPERTY_UUID, uuid);

                templateObjectNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF);

                Relationship specialInstanceRelationship = templateObjectNode.createRelationshipTo(classNode, RelTypes.INSTANCE_OF_SPECIAL);
                specialInstanceRelationship.setProperty(Constants.PROPERTY_NAME, "template"); //NOI18N 
                
                res[i] = uuid;
            }            
            tx.success();
            return res;
        }
    }
        
    @Override
    public String[] createBulkSpecialTemplateElement(String stElementClass, String stElementParentClassName, String stElementParentId, int numberOfTemplateElements, String stElementNamePattern) 
        throws OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        
        boolean isPossibleSpecialChildren = false;
        for (ClassMetadataLight  possibleSpecialChildren : mem.getPossibleSpecialChildren(stElementParentClassName)) {
            if (possibleSpecialChildren.getName().equals(stElementClass)) {
                isPossibleSpecialChildren = true;
                break;
            }
        }
        if (!isPossibleSpecialChildren)
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as special child of %s", stElementClass, stElementParentClassName == null ? Constants.NODE_DUMMYROOT : stElementParentClassName));
            
        try (Transaction tx = graphDb.beginTx()) {
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, stElementClass);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", stElementClass));
                        
            Node parentClassNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, stElementParentClassName);
            if (parentClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Parent class %s can not be found", stElementParentClassName));
            
            if (classNode.hasProperty(Constants.PROPERTY_ABSTRACT) && (boolean)classNode.getProperty(Constants.PROPERTY_ABSTRACT))
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", stElementClass));
            
            Node parentNode = null;
            
            for(Relationship instanceOfSpecialRelationship : parentClassNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF_SPECIAL)) {
                if (stElementParentId.equals(instanceOfSpecialRelationship.getStartNode().getProperty(Constants.PROPERTY_UUID))) {
                    parentNode = instanceOfSpecialRelationship.getStartNode();
                    break;
                }
            }
            
            if (parentNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Parent object %s of class %s not found", stElementParentId, stElementParentClassName));
            
            DynamicName dynamicName = new DynamicName(stElementNamePattern);
            if (dynamicName.getNumberOfDynamicNames() < numberOfTemplateElements) 
                throw new InvalidArgumentException("The given pattern to generate the name has "
                        + "less possibilities that the number of objects to be created");
                   
            String res[] = new String[numberOfTemplateElements];
            
            for (int i = 0; i < numberOfTemplateElements; i++) {
                String stElementName = dynamicName.getDynamicNames().get(i);

                String uuid = UUID.randomUUID().toString();
                Node templateObjectNode = graphDb.createNode(templateElementLabel);
                templateObjectNode.setProperty(Constants.PROPERTY_NAME, stElementName == null ? "" : stElementName);
                templateObjectNode.setProperty(Constants.PROPERTY_UUID, uuid);

                templateObjectNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);

                Relationship specialInstanceRelationship = templateObjectNode.createRelationshipTo(classNode, RelTypes.INSTANCE_OF_SPECIAL);
                specialInstanceRelationship.setProperty(Constants.PROPERTY_NAME, "template"); //NOI18N 
                
                res[i] = uuid;
            }
            tx.success();
            return res;
        }
    }
        
    @Override
    public ChangeDescriptor updateTemplateElement(String templateElementClass, String templateElementId, String[] attributeNames, 
            String[] attributeValues) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        if (attributeNames.length != attributeValues.length)
            throw new InvalidArgumentException("Attribute names and values must have the same length");
        
        try (Transaction tx = graphDb.beginTx()) {
            try {
                Node objectNode = graphDb.findNode(templateElementLabel, Constants.PROPERTY_UUID, templateElementId);
                
                if (!objectNode.hasRelationship(Direction.OUTGOING, RelTypes.INSTANCE_OF_SPECIAL) ||
                        !objectNode.getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME).equals(templateElementClass))
                    throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", templateElementClass));
                
                String affectedProperties = "", oldValues = "", newValues = "";
                ClassMetadata classMetadata = cm.getClass(templateElementClass);

                for (int i = 0; i < attributeNames.length; i++) {
                    if (!classMetadata.hasAttribute(attributeNames[i]))
                        throw new MetadataObjectNotFoundException(String.format("Class %s does not have any attribute named %s", templateElementClass, attributeNames[i]));

                    affectedProperties += " " + attributeNames[i];

                    String attributeType = classMetadata.getType(attributeNames[i]);
                    if (AttributeMetadata.isPrimitive(attributeType)) {
                        oldValues += " " + (objectNode.hasProperty(attributeNames[i]) ? objectNode.getProperty(attributeNames[i]) : "null");

                        if (attributeValues[i] == null) {
                            if (objectNode.hasProperty(attributeNames[i]))
                                objectNode.removeProperty(attributeNames[i]);
                        } else {                        
                            objectNode.setProperty(attributeNames[i], Util.getRealValue(attributeValues[i], attributeType));
                            newValues += " " + objectNode.getProperty(attributeNames[i]);
                        }
                    } else { //It's a list type
                        for (Relationship relatedToRelationship : objectNode.getRelationships(Direction.OUTGOING, RelTypes.RELATED_TO)) {
                            if (relatedToRelationship.hasProperty(Constants.PROPERTY_NAME) && relatedToRelationship.getProperty(Constants.PROPERTY_NAME).equals(attributeNames[i])) {
                                oldValues += " " + (relatedToRelationship.getEndNode().hasProperty(Constants.PROPERTY_NAME) ? relatedToRelationship.getEndNode().getProperty(Constants.PROPERTY_NAME) : "null");
                                relatedToRelationship.delete();
                                break;
                            }
                        }

                        if (attributeValues[i] != null && !attributeValues[i].equals("0") ) { //NOI18N 
                            Node listTypeItemNode = graphDb.findNode(listTypeItemLabel, Constants.PROPERTY_UUID, attributeValues[i]);

                            if (listTypeItemNode == null)
                                throw new ApplicationObjectNotFoundException(String.format("A list type %s with id %s could not be found", attributeType, attributeValues[i]));

                            Relationship relatedToRelationship = objectNode.createRelationshipTo(listTypeItemNode, RelTypes.RELATED_TO);
                            relatedToRelationship.setProperty(Constants.PROPERTY_NAME, attributeNames[i]);

                            newValues += " " + (listTypeItemNode.hasProperty(Constants.PROPERTY_NAME) ? listTypeItemNode.getProperty(Constants.PROPERTY_NAME) : "null");
                        } 
                    }
                }
                String templateElementName = objectNode.hasProperty(Constants.PROPERTY_NAME) ? (String) objectNode.getProperty(Constants.PROPERTY_NAME) : "null";
                tx.success();
                return new ChangeDescriptor(affectedProperties, oldValues, newValues, String.format("Updated template element %s [%s]", templateElementName, templateElementClass));
            } catch (NotFoundException ex) {
                throw new ApplicationObjectNotFoundException(String.format("Template object %s of class %s could not be found", templateElementId, templateElementClass));
            }
        }
    }

    @Override
    public ChangeDescriptor deleteTemplateElement(String templateElementClass, String templateElementId) 
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node templateObjectNode = graphDb.findNode(templateElementLabel, Constants.PROPERTY_UUID, templateElementId);
                
            if (!templateObjectNode.hasRelationship(Direction.OUTGOING, RelTypes.INSTANCE_OF_SPECIAL) ||
                    !templateObjectNode.getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME).equals(templateElementClass))
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", templateElementClass));
             
            String templateObjectName = templateObjectNode.hasProperty(Constants.PROPERTY_NAME) ? (String) templateObjectNode.getProperty(Constants.PROPERTY_NAME) : "null";
            //Delete the template element recursively
            Util.deleteTemplateObject(templateObjectNode);
            
            tx.success();
            return new ChangeDescriptor("", "", "", String.format("Deleted template element %s [%s]", templateObjectName, templateElementClass));
        } catch (NotFoundException ex) {
            throw new ApplicationObjectNotFoundException(String.format("Template object %s of class %s could not be found", templateElementId, templateElementClass));
        }
    }

    @Override
    public List<TemplateObjectLight> getTemplatesForClass(String className) throws MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            List<TemplateObjectLight> templates = new ArrayList<>();
                        
            String query = "MATCH (classNode)-[:" + RelTypes.HAS_TEMPLATE + "]->(templateObject) WHERE classNode.name={className} RETURN templateObject ORDER BY templateObject.name ASC"; //NOI18N
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("className", className); //NOI18N
            ResourceIterator<Node> queryResult = graphDb.execute(query, parameters).columnAs("templateObject");
            
            while (queryResult.hasNext()) {
                Node templateNode = queryResult.next();
                templates.add(new TemplateObjectLight(className, (String)templateNode.getProperty(Constants.PROPERTY_UUID), 
                        (String)templateNode.getProperty(Constants.PROPERTY_NAME)));
            }
            return templates;
        }
    }
    
    @Override
    public List<TemplateObjectLight> getTemplateElementChildren(String templateElementClass, String templateElementId)  {
        try (Transaction tx = graphDb.beginTx()) {
            
            String query = "MATCH (classNode)<-[:" + RelTypes.INSTANCE_OF_SPECIAL + 
                    "]-(templateElement)<-[:" + RelTypes.CHILD_OF + "]-(templateElementChild) "
                    + "WHERE classNode.name={templateElementClass} AND templateElement._uuid = {templateElementId} "
                    + "RETURN templateElementChild ORDER BY templateElementChild.name ASC"; //NOI18N
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("templateElementClass", templateElementClass); //NOI18N
            parameters.put("templateElementId", templateElementId); //NOI18N
            ResourceIterator<Node> queryResult = graphDb.execute(query, parameters).columnAs("templateElementChild");
            
            List<TemplateObjectLight> templateElementChildren = new ArrayList<>();
            while (queryResult.hasNext()) {
                Node templateChildNode = queryResult.next();
                templateElementChildren.add(new TemplateObjectLight((String)templateChildNode.getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME), 
                        (String)templateChildNode.getProperty(Constants.PROPERTY_UUID), (String)templateChildNode.getProperty(Constants.PROPERTY_NAME)));
            }
            
            return templateElementChildren; 
        }
    }
    
    @Override
    public List<TemplateObjectLight> getTemplateSpecialElementChildren(String tsElementClass, String tsElementId) {
        try (Transaction tx = graphDb.beginTx()) {
            String query = "MATCH (classNode)<-[:" + RelTypes.INSTANCE_OF_SPECIAL + 
                    "]-(templateElement)<-[:" + RelTypes.CHILD_OF_SPECIAL + "]-(templateElementChild) "
                    + "WHERE classNode.name={templateElementClass} AND templateElement._uuid = {templateElementId} "
                    + "RETURN templateElementChild ORDER BY templateElementChild.name ASC"; //NOI18N
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("templateElementClass", tsElementClass); //NOI18N
            parameters.put("templateElementId", tsElementId); //NOI18N
            ResourceIterator<Node> queryResult = graphDb.execute(query, parameters).columnAs("templateElementChild");
            
            List<TemplateObjectLight> templateElementChildren = new ArrayList<>();
            while (queryResult.hasNext()) {
                Node templateChildNode = queryResult.next();
                templateElementChildren.add(new TemplateObjectLight((String)templateChildNode.getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME), 
                        (String)templateChildNode.getProperty(Constants.PROPERTY_UUID), (String)templateChildNode.getProperty(Constants.PROPERTY_NAME)));
            }
            
            return templateElementChildren; 
        }
    }
    
    @Override
    public TemplateObject getTemplateElement(String templateElementClass, String templateElementId)
        throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            try {
                //First we find the object, then we check if the class is the same as specified
                Node templateObjectNode = graphDb.findNode(templateElementLabel, Constants.PROPERTY_UUID, templateElementId);
                if (!templateObjectNode.hasRelationship(RelTypes.INSTANCE_OF_SPECIAL) ||
                        !templateObjectNode.getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode().
                                getProperty(Constants.PROPERTY_NAME).equals(templateElementClass))
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found or the object is not its instance", templateElementClass));
                return createTemplateElementFromNode(templateObjectNode, templateElementClass);
            } catch (NotFoundException ex) {
                throw new ApplicationObjectNotFoundException(String.format("Template object %s of class %s could not be found", templateElementId, templateElementClass));
            }
        }
    }

    @Override
    public String[] copyTemplateElements(String[] sourceObjectsClassNames, String[] sourceObjectsIds, String newParentClassName, 
            String newParentId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, ArraySizeMismatchException {
        
        if (sourceObjectsClassNames.length != sourceObjectsIds.length)
            throw new ArraySizeMismatchException("The sourceObjectsClassNames and sourceObjectsIds arrays have different sizes");
        try (Transaction tx = graphDb.beginTx()) {
            String[] newTemplateElements = new String[sourceObjectsClassNames.length];
            
            Node newParentNode = getTemplateElementInstance(newParentClassName, newParentId);
            
            for (int i = 0; i < sourceObjectsClassNames.length; i++) {
                Node templateObjectNode = getTemplateElementInstance(sourceObjectsClassNames[i], sourceObjectsIds[i]);
                Node newTemplateElementInstance = copyTemplateElement(templateObjectNode, true);
                newTemplateElementInstance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF);
                newTemplateElements[i] = (String)newTemplateElementInstance.getProperty(Constants.PROPERTY_UUID);
            }
            tx.success();
            return newTemplateElements;
        }
    }
    
    @Override
    public String[] copyTemplateSpecialElement(String[] sourceObjectsClassNames, String[] sourceObjectsIds, String newParentClassName, String newParentId) 
     throws ArraySizeMismatchException, ApplicationObjectNotFoundException, MetadataObjectNotFoundException {
        if(sourceObjectsClassNames.length != sourceObjectsIds.length)
            throw new ArraySizeMismatchException("The sourceObjectsClassNames and sourceObjectsIds arrays have different sizes");
        try (Transaction tx = graphDb.beginTx()) {
            String[] newTemplateSpecialElements = new String[sourceObjectsClassNames.length];
            
            Node newParentNode = getTemplateElementInstance(newParentClassName, newParentId);
            
            for (int i = 0; i < sourceObjectsClassNames.length; i++) {
                Node templateObjectNode = getTemplateElementInstance(sourceObjectsClassNames[i], sourceObjectsIds[i]);
                Node newTemplateSpecialElementInstance = copyTemplateElement(templateObjectNode, true);
                newTemplateSpecialElementInstance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF_SPECIAL);
                newTemplateSpecialElements[i] = (String)newTemplateSpecialElementInstance.getProperty(Constants.PROPERTY_UUID);
            }
            tx.success();
            return newTemplateSpecialElements;
        }
    }
    
    @Override
    public void registerCommercialModule(GenericCommercialModule module) throws NotAuthorizedException {
        commercialModules.put(module.getName(), module);
    }

    @Override
    public GenericCommercialModule getCommercialModule(String moduleName) throws NotAuthorizedException, NoCommercialModuleFoundException {
        GenericCommercialModule commercialModule = commercialModules.get(moduleName);
        if(commercialModule == null)
            throw new NoCommercialModuleFoundException(String.format("the module %s could not be found", moduleName));
        else
            return commercialModule;
    }

    @Override
    public Collection<GenericCommercialModule> getCommercialModules() throws NotAuthorizedException {
        return commercialModules.values();
    }
    
    @Override
    public HashMap<String, BusinessObjectList> executeCustomDbCode(String dbCode, boolean needReturn) throws NotAuthorizedException {
        try (Transaction tx = graphDb.beginTx()) {
        
            Map<String, Object> params = new HashMap<>();
            params.put("false", false);//NOI18N
            params.put("true", true);//NOI18N
            Result theResult = graphDb.execute(dbCode, params);
            if(needReturn){
                HashMap<String, BusinessObjectList> thePaths = new HashMap<>();
            
                for (String column : theResult.columns())
                    thePaths.put(column, new BusinessObjectList());

                try {
                    while (theResult.hasNext()) {
                        Map<String, Object> row = theResult.next();
                        for (String column : row.keySet()) 
                            thePaths.get(column).add(createRemoteObjectFromNode((Node)row.get(column)));
                    }
                } catch (InvalidArgumentException ex) {} //this should not happen

                return thePaths;
            }
            else {
                tx.success();
                return null;
            }
        }
    }
    
    @Override
    public List<Properties> executeCustomScriptedQuery(String queryName, Properties parameters) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        switch (queryName) {
            case "whSuggestions":
                return new WarehouseManagerSearchQuery().execute(parameters);
            default:
                throw new ApplicationObjectNotFoundException(String.format("Scripted query %s could not be found", queryName));
        }
    }
    
    // Helpers
    /**
     * recursive method used to generate a single "class" node (see the <a href="http://neotropic.co/kuwaiba/wiki/index.php?title=XML_Documents#To_describe_the_data_model">wiki</a> for details)
     * @param classNode Node representing the class to be added
     * @param paretTag Parent to attach the new class node
     */
    private void getXMLNodeForClass(Node classNode, XMLEventWriter xmlew, XMLEventFactory xmlef) throws XMLStreamException {
        int applicationModifiers = 0;
        int javaModifiers = 0;
        QName qnameClass = new QName("class"); // NOI18N
        xmlew.add(xmlef.createStartElement(qnameClass, null, null));
        
        xmlew.add(xmlef.createAttribute(new QName("id"), Long.toString(classNode.getId())));
        xmlew.add(xmlef.createAttribute(new QName("name"), classNode.getProperty(Constants.PROPERTY_NAME).toString()));
        xmlew.add(xmlef.createAttribute(new QName("classPackage"), ""));
        
        //Application modifiers
        if ((Boolean)classNode.getProperty(Constants.PROPERTY_COUNTABLE))
            applicationModifiers |= Constants.CLASS_MODIFIER_COUNTABLE;

        if ((Boolean)classNode.getProperty(Constants.PROPERTY_CUSTOM))
            applicationModifiers |= Constants.CLASS_MODIFIER_CUSTOM;
        xmlew.add(xmlef.createAttribute(new QName("applicationModifiers"), Integer.toString(applicationModifiers)));
        
        //Language modifiers
        if ((Boolean)classNode.getProperty(Constants.PROPERTY_ABSTRACT))
            javaModifiers |= Modifier.ABSTRACT;
        xmlew.add(xmlef.createAttribute(new QName("javaModifiers"), Integer.toString(javaModifiers)));
        
        //Class type
        if (classNode.getProperty(Constants.PROPERTY_NAME).equals(Constants.CLASS_ROOTOBJECT)) {
            xmlew.add(xmlef.createAttribute(new QName("classType"), Integer.toString(Constants.CLASS_TYPE_ROOT)));
        }else{
            if (Util.isSubclassOf("InventoryObject", classNode))
                xmlew.add(xmlef.createAttribute(new QName("classType"), Integer.toString(Constants.CLASS_TYPE_INVENTORY)));
            else{
                if (Util.isSubclassOf("ApplicationObject", classNode))
                    xmlew.add(xmlef.createAttribute(new QName("classType"), Integer.toString(Constants.CLASS_TYPE_APPLICATION)));
                else
                    xmlew.add(xmlef.createAttribute(new QName("classType"), Integer.toString(Constants.CLASS_TYPE_OTHER)));
            }
        }
        
        QName qnameAttributes = new QName("attributes");
        xmlew.add(xmlef.createStartElement(qnameAttributes, null, null));
        for (Relationship relWithAttributes : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE, Direction.OUTGOING)){
            QName qnameAttribute = new QName("attribute");
            xmlew.add(xmlef.createStartElement(qnameAttribute, null, null));
            
            Node attributeNode = relWithAttributes.getEndNode();
            int attributeApplicationModifiers = 0;
            
            xmlew.add(xmlef.createAttribute(new QName("name"), attributeNode.getProperty(Constants.PROPERTY_NAME).toString()));
            xmlew.add(xmlef.createAttribute(new QName("type"), attributeNode.getProperty(Constants.PROPERTY_TYPE).toString()));
            xmlew.add(xmlef.createAttribute(new QName("javaModifiers"), "0")); // Not used
            //Application modifiers
            if ((Boolean)attributeNode.getProperty(Constants.PROPERTY_NO_COPY))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_NOCOPY;
            if ((Boolean)attributeNode.getProperty(Constants.PROPERTY_VISIBLE))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_VISIBLE;
            if ((Boolean)attributeNode.getProperty(Constants.PROPERTY_ADMINISTRATIVE))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_ADMINISTRATIVE;
            if ((Boolean)attributeNode.getProperty(Constants.PROPERTY_READ_ONLY))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_READONLY;
            xmlew.add(xmlef.createAttribute(new QName("applicationModifiers"), Integer.toString(attributeApplicationModifiers)));
            xmlew.add(xmlef.createEndElement(qnameAttribute, null));
        }
        xmlew.add(xmlef.createEndElement(qnameAttributes, null));
        
        QName qnameSubclasses = new QName("subclasses");
        xmlew.add(xmlef.createStartElement(qnameSubclasses, null, null));
        for (Relationship relWithSubclasses : classNode.getRelationships(RelTypes.EXTENDS, Direction.INCOMING))
            getXMLNodeForClass(relWithSubclasses.getStartNode(), xmlew, xmlef);
        xmlew.add(xmlef.createEndElement(qnameSubclasses, null));
        
        xmlew.add(xmlef.createEndElement(qnameClass, null));
    }
    /**
     * Reads a ExtendedQuery looking for the classes involved in the query and returns all class nodes
     * @param query
     * @return class metadata nodes
     */
    private Map<String, Node> getNodesFromQuery(ExtendedQuery query)  throws MetadataObjectNotFoundException{

        Map<String, Node> classNodes = new HashMap<>();
        List<String> ListClassNames = new ArrayList();
        readJoins(ListClassNames, query);
        try(Transaction tx = graphDb.beginTx()) {
            for(String className : ListClassNames)
                classNodes.put(className, graphDb.findNode(classLabel, Constants.PROPERTY_NAME, className));            
        }
        return classNodes;
    }
    
    private String readJoins(List<String> l, ExtendedQuery query){
        
        String className;

        if(query == null)
            return null;
        else
            className = query.getClassName();

        if(query.getJoins() != null) {
            for(ExtendedQuery join : query.getJoins()){
                    readJoins(l,join);
            }
        }
        if(className != null || className.isEmpty())
            l.add(className);
        return className;
    }
    
    private Node getInstanceOfClass(String className, String oid) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException
    {
        //Note that for this method, the caller should handle the transaction
        //if any of the parameters is null, return the dummy root
        if (className == null)
            return graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
        
        Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME,className);
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));

        Iterable<Relationship> iteratorInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        Iterator<Relationship> instances = iteratorInstances.iterator();

        while (instances.hasNext()){
            Node otherSide = instances.next().getStartNode();
            String otherSideUuid = otherSide.hasProperty(Constants.PROPERTY_UUID) ? (String) otherSide.getProperty(Constants.PROPERTY_UUID) : null;
            if (otherSideUuid == null)
                throw new InvalidArgumentException(String.format("The node with id %s does not have uuid", otherSide.getId()));
                        
            if (otherSideUuid.equals(oid))
                return otherSide;
        }
        throw new BusinessObjectNotFoundException(className, oid);
    }    
    
    public void deletePool(Node poolNode) throws OperationNotPermittedException {
        
        for (Relationship containmentRelationship : poolNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)) {
                //A pool may have inventory objects as children or other pools
                Node child = containmentRelationship.getStartNode();
                if (child.hasRelationship(RelTypes.INSTANCE_OF)) //It's an inventory object
                    deleteObject(child, false);
                else
                    deletePool(child); //Although making deletePool to receive a node as argument would be more efficient,
                                               //the impact is not that much since the number of pools is expected to be low
            }
            
            //Removes any remaining relationships
            for (Relationship remainingRelationship : poolNode.getRelationships())
                remainingRelationship.delete();
            
            poolNode.delete();
    }
    
    private Node getTemplateElementInstance(String templateElementClassName, String templateElementId) throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException {
        Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, templateElementClassName);
            
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", templateElementClassName));

        Node templateElementNode = null;

        for (Relationship instanceOfSpecialRelationship : classNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF_SPECIAL)) {
            Node startNode = instanceOfSpecialRelationship.getStartNode();
            if (templateElementId.equals(startNode.getProperty(Constants.PROPERTY_UUID))) {
                templateElementNode = startNode;
                break;
            }
        }

        if (templateElementNode == null)
            throw new ApplicationObjectNotFoundException(String.format("Template object %s of class %s could not be found", templateElementId, templateElementClassName));

        return templateElementNode;
    }
    
    private Node copyTemplateElement(Node templateObject, boolean recursive) {
        
        Node newTemplateElementInstance = graphDb.createNode(templateElementLabel);
        newTemplateElementInstance.setProperty(Constants.PROPERTY_UUID, UUID.randomUUID().toString());
        
        for (String property : templateObject.getPropertyKeys())
            newTemplateElementInstance.setProperty(property, templateObject.getProperty(property));
        
        for (Relationship rel : templateObject.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING))
            newTemplateElementInstance.createRelationshipTo(rel.getEndNode(), RelTypes.RELATED_TO).setProperty(Constants.PROPERTY_NAME, rel.getProperty(Constants.PROPERTY_NAME));
        
        newTemplateElementInstance.createRelationshipTo(templateObject.getRelationships(RelTypes.INSTANCE_OF_SPECIAL).iterator().next().getEndNode(), RelTypes.INSTANCE_OF_SPECIAL);

        if (recursive){
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF, Direction.INCOMING)){
                Node newChild = copyTemplateElement(rel.getStartNode(), true);
                newChild.createRelationshipTo(newTemplateElementInstance, RelTypes.CHILD_OF);
            }
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING)){
                Node newChild = copyTemplateElement(rel.getStartNode(), true);
                newChild.createRelationshipTo(newTemplateElementInstance, RelTypes.CHILD_OF_SPECIAL);
            }
        }
        return newTemplateElementInstance;
    }
    
    //End of Helpers  
    
    // Bookmarks
    @Override
    public void addObjectTofavoritesFolder(String objectClass, String objectId, long favoritesFolderId, long userId)
            throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException{
        
        try (Transaction tx = graphDb.beginTx()) {
            Node favoritesFolderNode = getFavoritesFolderForUser(favoritesFolderId, userId);
            if (favoritesFolderNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a favorites folder with id %s", favoritesFolderId));
            
            Node objectNode = getInstanceOfClass(objectClass, objectId);
            
            if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.IS_BOOKMARK_ITEM_IN)) {
                for (Relationship relationship : objectNode.getRelationships(Direction.OUTGOING, RelTypes.IS_BOOKMARK_ITEM_IN)) {
                    if (favoritesFolderNode.getId() == relationship.getEndNode().getId())
                        throw new OperationNotPermittedException("An object can not be added twice to the same favorites folder");
                }
            }
            objectNode.createRelationshipTo(favoritesFolderNode, RelTypes.IS_BOOKMARK_ITEM_IN);
            
            tx.success();
        }
    }
    
    @Override
    public void removeObjectFromfavoritesFolder(String objectClass, String objectId, long favoritesFolderId, long userId) 
        throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
                
        try (Transaction tx = graphDb.beginTx()) {
            
            Node objectNode = getInstanceOfClass(objectClass, objectId);
            
            if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.IS_BOOKMARK_ITEM_IN)) {
                
                Node favoritesFolderNode = getFavoritesFolderForUser(favoritesFolderId, userId);
                if (favoritesFolderNode == null)
                    throw new ApplicationObjectNotFoundException(String.format("Can not find a favorites folder with id %s", favoritesFolderId));
                
                Relationship relationshipToDelete = null;
                
                for (Relationship relationship : objectNode.getRelationships(Direction.OUTGOING, RelTypes.IS_BOOKMARK_ITEM_IN)) {
                    
                    if (favoritesFolderNode.getId() == relationship.getEndNode().getId())
                        relationshipToDelete = relationship;
                }
                if (relationshipToDelete != null) {
                    relationshipToDelete.delete();
                    tx.success();
                }
            }
        }
    }
    
    @Override
    public long createFavoritesFolderForUser(String name, long userId) 
        throws ApplicationObjectNotFoundException, InvalidArgumentException {
        
        if (name == null || name.trim().isEmpty())
                throw new InvalidArgumentException("The name of the favorites folder can not be empty");
        
        try (Transaction tx = graphDb.beginTx()) {
            Node userNode = Util.findNodeByLabelAndId(userLabel, userId);
            
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("User with id %s could not be found", userId));
            
            Node favoritesFolderNode = graphDb.createNode();
            favoritesFolderNode.setProperty(Constants.PROPERTY_NAME, name);            
            
            userNode.createRelationshipTo(favoritesFolderNode, RelTypes.HAS_BOOKMARK);
            
            tx.success();
            return favoritesFolderNode.getId();
        }
    }
    
    @Override
    public void deleteFavoritesFolders (long[] favoritesFolderId, long userId)
        throws ApplicationObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            if (favoritesFolderId != null) {
                for (long id : favoritesFolderId) {
                    Node favoritesFolderNode = getFavoritesFolderForUser(id, userId);
                    
                    if (favoritesFolderNode == null)
                        throw new ApplicationObjectNotFoundException(String.format("Can not find a favorites folder with id %s",id));
                    
                    for (Relationship relationship : favoritesFolderNode.getRelationships(Direction.INCOMING, RelTypes.HAS_BOOKMARK))
                        relationship.delete();
                    
                    for (Relationship relationship : favoritesFolderNode.getRelationships(Direction.INCOMING, RelTypes.IS_BOOKMARK_ITEM_IN))
                        relationship.delete();
                    
                    favoritesFolderNode.delete();
                    tx.success();
                }
            }
        }
    }
    
    @Override
    public List<FavoritesFolder> getFavoritesFoldersForUser(long userId) 
        throws ApplicationObjectNotFoundException {
                
        try (Transaction tx = graphDb.beginTx()) {
            
            Node userNode = Util.findNodeByLabelAndId(userLabel, userId);
            
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("User with id %s could not be found", userId));
            
            List<FavoritesFolder> favoritesFolders = new ArrayList(); 
            
            if (userNode.hasRelationship(Direction.OUTGOING, RelTypes.HAS_BOOKMARK)) {
                
                for (Relationship relationship : userNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_BOOKMARK)) {
                    Node favoritesFolderNode = relationship.getEndNode();
                    String name = favoritesFolderNode.hasProperty(Constants.PROPERTY_NAME) ? (String) favoritesFolderNode.getProperty(Constants.PROPERTY_NAME) : null;
                    
                    favoritesFolders.add(new FavoritesFolder(favoritesFolderNode.getId(), name));
                }
            }
            return favoritesFolders;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectsInFavoritesFolder(long favoritesFolderId, long userId, int limit) 
        throws ApplicationObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node favoritesFolderNode = getFavoritesFolderForUser(favoritesFolderId, userId);
            
            if (favoritesFolderNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a favorites folder with id %s", favoritesFolderId));
            
            List<BusinessObjectLight> bookmarkItems = new ArrayList<>();
            
            int i = 0;
            for (Relationship relationship : favoritesFolderNode.getRelationships(Direction.INCOMING, RelTypes.IS_BOOKMARK_ITEM_IN)) {
                if (limit != -1) {
                    if (i >= limit)
                        break;
                    i++;
                }
                Node bookmarkItem = relationship.getStartNode();
                
                String bookmarkItemUuid = bookmarkItem.hasProperty(Constants.PROPERTY_UUID) ? (String) bookmarkItem.getProperty(Constants.PROPERTY_UUID) : null;
                if (bookmarkItemUuid == null)
                    throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", bookmarkItem.getId()));
                                                                
                BusinessObjectLight rbol = new BusinessObjectLight(Util.getClassName(bookmarkItem),
                    bookmarkItemUuid, 
                    bookmarkItem.hasProperty(Constants.PROPERTY_NAME) ? (String) bookmarkItem.getProperty(Constants.PROPERTY_NAME) : null);
                
                bookmarkItems.add(rbol);
            }
            return bookmarkItems;
        }
    }
    
    @Override
    public List<FavoritesFolder> getFavoritesFoldersForObject(long userId, String objectClass, String objectId) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node objectNode = getInstanceOfClass(objectClass, objectId);
                        
            List<FavoritesFolder> favoritesFolders = new ArrayList(); 
                
            for (Relationship relationship : objectNode.getRelationships(Direction.OUTGOING, RelTypes.IS_BOOKMARK_ITEM_IN)) {
                Node favoritesFolderNode = relationship.getEndNode();

                if (getFavoritesFolderForUser(favoritesFolderNode.getId(), userId) != null) //If null, the object is in a favorites folder, but that folder does not belong to the current user, so it's safe to omiit it
                    favoritesFolders.add(new FavoritesFolder(favoritesFolderNode.getId(), (String) favoritesFolderNode.getProperty(Constants.PROPERTY_NAME)));
            }
            return favoritesFolders;
        }
    }
    
    @Override
    public FavoritesFolder getFavoritesFolder(long favoritesFolderId, long userId) 
        throws ApplicationObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node favoritesFolderNode = getFavoritesFolderForUser(favoritesFolderId, userId);
            
            if (favoritesFolderNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a favorites folder with id %s", favoritesFolderId));
            
            String name = favoritesFolderNode.hasProperty(Constants.PROPERTY_NAME) ? (String) favoritesFolderNode.getProperty(Constants.PROPERTY_NAME) : null;
                    
            return new FavoritesFolder(favoritesFolderNode.getId(), name);
        }
    }
    
    @Override
    public void updateFavoritesFolder(long favoritesFolderId, long userId, String favoritesFolderName) 
        throws ApplicationObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node favoritesFolderNode = getFavoritesFolderForUser(favoritesFolderId, userId);
            
            if (favoritesFolderNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a favorites folder with id %s", favoritesFolderId));
            
            if (favoritesFolderName != null && !favoritesFolderName.trim().isEmpty()) {
                favoritesFolderNode.setProperty(Constants.PROPERTY_NAME, favoritesFolderName);
                tx.success();
            } else 
                throw new InvalidArgumentException("Favorites folder name can not be empty");
        }
    }
    
    //<editor-fold desc="Business Rules" defaultstate="collapsed">
    @Override
    public long createBusinessRule(String ruleName, String ruleDescription, int ruleType, 
            int ruleScope, String appliesTo, String ruleVersion, List<String> constraints) throws InvalidArgumentException {
        
        if (ruleName == null || ruleDescription == null || ruleVersion == null || appliesTo == null || ruleType < 1 || ruleScope < 1)
            throw new InvalidArgumentException("Invalid parameter. Make sure all parameters are different from null and greater than 1");
        
        if (constraints == null || constraints.isEmpty())
            throw new InvalidArgumentException("The rule must have at least one constraint");
        
        try (Transaction tx = graphDb.beginTx()) {
            Node businessRuleNode = graphDb.createNode(businessRulesLabel);
            
            businessRuleNode.setProperty(Constants.PROPERTY_NAME, ruleName);
            businessRuleNode.setProperty(Constants.PROPERTY_DESCRIPTION, ruleDescription);
            businessRuleNode.setProperty(Constants.PROPERTY_TYPE, ruleType);
            businessRuleNode.setProperty(Constants.PROPERTY_SCOPE, ruleScope);
            businessRuleNode.setProperty(Constants.PROPERTY_APPLIES_TO, appliesTo);
            businessRuleNode.setProperty(Constants.PROPERTY_VERSION, ruleVersion);
            
            for (int i = 0; i < constraints.size(); i++)
                businessRuleNode.setProperty("constraint" + (i + 1), constraints.get(i)); //NOI18N
            
            tx.success();
            return businessRuleNode.getId();
        }
    }
    
    @Override
    public void deleteBusinessRule(long businessRuleId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node businessRuleNode = Util.findNodeByLabelAndId(businessRulesLabel, businessRuleId);
            
            if (businessRuleNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Business rule with id %s not found", businessRuleId));
            
            businessRuleNode.delete();
            
            tx.success();
        }
    }
    
    @Override
    public List<BusinessRule> getBusinessRules(int type) {
        try (Transaction tx = graphDb.beginTx()) {
            List<BusinessRule> res = new ArrayList<>();
            
            ResourceIterator<Node> businessRuleNodes = graphDb.findNodes(businessRulesLabel);

            while (businessRuleNodes.hasNext()) {
                Node businessRuleNode = businessRuleNodes.next();
                if (type == -1 || type == (int)businessRuleNode.getProperty(Constants.PROPERTY_TYPE))
                    res.add(new BusinessRule(businessRuleNode.getId(), businessRuleNode.getAllProperties()));
            }
            return res;
        }
    }
    
    @Override
    public void checkRelationshipByAttributeValueBusinessRules(String sourceObjectClassName, String sourceObjectId ,
            String targetObjectClassName, String targetObjectId) throws BusinessRuleException, InvalidArgumentException  {
        
        if (!Boolean.valueOf(getConfiguration().getProperty("enforceBusinessRules", "false")))
            return;
        
        try (Transaction tx = graphDb.beginTx()) {    
            ResourceIterator<Node> businessRuleNodes = graphDb.findNodes(businessRulesLabel);

            while (businessRuleNodes.hasNext()) {
                Node businessRuleNode = businessRuleNodes.next();
                                
                if (sourceObjectClassName.equals(businessRuleNode.getProperty(Constants.PROPERTY_APPLIES_TO))) {
                    /**
                     * In this type of business rules:
                     * constraint1 is the class name of the target object
                     * constraint2 is the name of the attribute name of the source object
                     * constraint3 is the name of the attribute name of the target object
                     * constraint4 is the name of the attribute value of the source object
                     * constraint5 is the name of the attribute value of the target object
                     */
                    if (!businessRuleNode.hasProperty("constraint1") || !businessRuleNode.hasProperty("constraint2") || !businessRuleNode.hasProperty("constraint3") 
                            || !businessRuleNode.hasProperty("constraint4") || !businessRuleNode.hasProperty("constraint5"))
                        throw new InvalidArgumentException("Malformed busines rule. One of the 5 required constraints is not present");
                    
                    if (businessRuleNode.getProperty("constraint1").equals(targetObjectClassName)) {
                        
                        String sourceObjectAttributeConstraint = (String)businessRuleNode.getProperty("constraint4");
                        String targetObjectAttributeConstraint = (String)businessRuleNode.getProperty("constraint5");
                        if (sourceObjectAttributeConstraint.isEmpty() || targetObjectAttributeConstraint.isEmpty()) //This link can be connected to any object
                            return;
                        
                        Node sourceInstance = graphDb.findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, sourceObjectId);
                        String sourceInstanceAttributeValue = Util.getAttributeFromNode(sourceInstance, (String)businessRuleNode.getProperty("constraint2"));
                        
                        if (sourceObjectAttributeConstraint.equals(sourceInstanceAttributeValue)) {
                            Node targetInstance = graphDb.findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, targetObjectId);
                            String targetInstanceAttributeValue = Util.getAttributeFromNode(targetInstance, (String)businessRuleNode.getProperty("constraint3"));
                            
                            if (!targetObjectAttributeConstraint.equals(targetInstanceAttributeValue))
                                throw new BusinessRuleException(String.format("Value of %s in %s does not match %s in %s", 
                                                                        businessRuleNode.getProperty("constraint3"),
                                                                        targetObjectClassName, businessRuleNode.getProperty("constraint2"), sourceObjectClassName));
                            else
                                return; //After finding the first matching rule, return. This behavior might change in further releases
                        }
                    } else 
                        throw new BusinessRuleException(String.format("Objects of class %s can not be connected to objects of class %s", sourceObjectClassName, targetObjectClassName));
                }
            }
            throw new BusinessRuleException(String.format("No matching rule was found for %s and %s", sourceObjectClassName, targetObjectClassName));
        }
    }
    
    //</editor-fold>

    //<editor-fold desc="Synchronization API" defaultstate="collapsed">
    @Override
    public SynchronizationGroup getSyncGroup(long syncGroupId) throws InvalidArgumentException, ApplicationObjectNotFoundException, MetadataObjectNotFoundException, UnsupportedPropertyException {
        try (Transaction tx = graphDb.beginTx()) {
        
            Node syncGroupNode = Util.findNodeByLabelAndId(syncGroupsLabel, syncGroupId);
            if (syncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Sync group with id %s could not be found", syncGroupId));
            return Util.createSyncGroupFromNode(syncGroupNode);
        } 
    }
   
    @Override
    public List<SynchronizationGroup> getSyncGroups() throws InvalidArgumentException, MetadataObjectNotFoundException, UnsupportedPropertyException {
        
        List<SynchronizationGroup> synchronizationGroups = new ArrayList<>();
        
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterator<Node> syncGroupsNodes = graphDb.findNodes(syncGroupsLabel);

            while (syncGroupsNodes.hasNext()) {
                Node syncGroup = syncGroupsNodes.next();
                synchronizationGroups.add(Util.createSyncGroupFromNode(syncGroup));            
            }            
            tx.success();
            return synchronizationGroups;
            
        }
    }
    
    @Override
    public SyncDataSourceConfiguration getSyncDataSourceConfiguration(String objectId) 
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        try (Transaction tx = graphDb.beginTx()) {
            Node inventoryObjectNode = graphDb.findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, objectId);
            
            Node syncDatasourceConfiguration = null;
            if(inventoryObjectNode != null){ 
                if(!inventoryObjectNode.hasRelationship(RelTypes.HAS_CONFIGURATION))
                   throw new UnsupportedPropertyException(String.format("The object %s (%s) does not have a sync datasource configuration", 
                           inventoryObjectNode.getProperty(Constants.PROPERTY_NAME), objectId));
                
                syncDatasourceConfiguration = inventoryObjectNode.getSingleRelationship(RelTypes.HAS_CONFIGURATION, Direction.INCOMING).getStartNode();
                if(syncDatasourceConfiguration == null)
                    throw new ApplicationObjectNotFoundException(String.format("The object with id %s has no data source configuration related", objectId));
            }

            tx.success();
            return Util.createSyncDataSourceConfigFromNode(syncDatasourceConfiguration);
        }
    }
    
    @Override
    public SyncDataSourceConfiguration getSyncDataSourceConfigurationById(long syncDatasourceId) 
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        try (Transaction tx = graphDb.beginTx()) {
             
            Node syncDatasourceConfiguration = graphDb.getNodeById(syncDatasourceId);
            if(syncDatasourceConfiguration == null)
                throw new ApplicationObjectNotFoundException(String.format("The sync data source configuration with id: %s is not related with anything", syncDatasourceId));
            
            tx.success();
            return Util.createSyncDataSourceConfigFromNode(syncDatasourceConfiguration);
        }
    }
    
    @Override
    public  List<SyncDataSourceConfiguration> getSyncDataSourceConfigurations(long syncGroupId) 
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException{
        List<SyncDataSourceConfiguration> syncDataSourcesConfigurations = new ArrayList<>();
        
        try (Transaction tx = graphDb.beginTx()) {
            Node syncGroupNode = Util.findNodeByLabelAndId(syncGroupsLabel, syncGroupId);
            
            if (syncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The sync group with id %s could not be found", syncGroupId));
            
            for(Relationship rel : syncGroupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_GROUP))
                syncDataSourcesConfigurations.add(Util.createSyncDataSourceConfigFromNode(rel.getStartNode()));
            
            tx.success();
        }
        
        return syncDataSourcesConfigurations;
    }
    
    @Override
    public long createSyncGroup(String name) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        if (name == null || name.trim().isEmpty())
                throw new InvalidArgumentException("The name of the sync group can not be empty");
        
        try (Transaction tx = graphDb.beginTx()) {
            Node syncGroupNode = graphDb.createNode(syncGroupsLabel);
            syncGroupNode.setProperty(Constants.PROPERTY_NAME, name);
           
            tx.success();

            return syncGroupNode.getId();
        }
    }
    
    @Override
    public void updateSyncGroup(long syncGroupId, List<StringPair> syncGroupProperties) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        if (syncGroupProperties == null)
            throw new InvalidArgumentException(String.format("The parameters of the sync group with id %s can not be null", syncGroupId));
        
        try (Transaction tx = graphDb.beginTx()) {
            
            Node syncGroupNode = Util.findNodeByLabelAndId(syncGroupsLabel, syncGroupId);
            
            if (syncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Synchronization Group with id %s could not be found", syncGroupId));
            
            for (StringPair syncGroupProperty : syncGroupProperties)
                syncGroupNode.setProperty(syncGroupProperty.getKey(), syncGroupProperty.getValue());
                        
            tx.success();
        }
    }
    
    @Override
    public void deleteSynchronizationGroup(long syncGroupId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node syncGroupNode = Util.findNodeByLabelAndId(syncGroupsLabel, syncGroupId);
            
            if (syncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find the Synchronization group with id %s",syncGroupId));
            
            for (Relationship relationship : syncGroupNode.getRelationships())
                relationship.delete();
     
            syncGroupNode.delete();
            tx.success();
        }
    }
    
    @Override
    public long createSyncDataSourceConfig(String objectId, long syncGroupId, String configName, List<StringPair> parameters) throws ApplicationObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException {
        if (configName == null || configName.trim().isEmpty())
                throw new InvalidArgumentException("The sync configuration name can not be empty");
        
        try (Transaction tx = graphDb.beginTx()) {
            Node syncGroupNode = Util.findNodeByLabelAndId(syncGroupsLabel, syncGroupId);
            if(syncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The sync group with id %s could not be found", syncGroupId));
                       
            Node objectNode = graphDb.findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, objectId);
            if(syncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The object with id %s could not be found", objectId));
            
            if(objectNode.hasRelationship(Direction.OUTGOING, RelTypes.HAS_CONFIGURATION))
                throw new OperationNotPermittedException(String.format("The object id %s already has a sync datasource configuration", objectId));
            
            Node syncDataSourceConfigNode =  graphDb.createNode();
            syncDataSourceConfigNode.setProperty(Constants.PROPERTY_NAME, configName);
            
            for (StringPair parameter : parameters) {
                if (!syncDataSourceConfigNode.hasProperty(parameter.getKey()))
                    syncDataSourceConfigNode.setProperty(parameter.getKey(), parameter.getValue() == null ? "" : parameter.getValue());
                else
                    throw new InvalidArgumentException(String.format("Parameter %s in configuration %s is duplicated", configName, parameter.getKey()));
            }
            
            syncDataSourceConfigNode.createRelationshipTo(objectNode, RelTypes.HAS_CONFIGURATION);
            syncDataSourceConfigNode.createRelationshipTo(syncGroupNode, RelTypes.BELONGS_TO_GROUP);
            
            tx.success();
            return syncDataSourceConfigNode.getId();
        }           
    }
    
    @Override
    public void updateSyncDataSourceConfig(long syncDataSourceConfigId, List<StringPair> parameters) 
        throws ApplicationObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node syncDataSourceConfig = graphDb.getNodeById(syncDataSourceConfigId);
            if (syncDataSourceConfig == null)
                throw new ApplicationObjectNotFoundException(String.format("Synchronization Data Source Configuration with id %s could not be found", syncDataSourceConfigId));
            
            for (StringPair parameter : parameters)
                syncDataSourceConfig.setProperty(parameter.getKey(), parameter.getValue());
                        
            tx.success();
        }
    }
    
    @Override    
    public void deleteSynchronizationDataSourceConfig(long syncDataSourceConfigId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node syncDataSourceConfigNode = graphDb.getNodeById(syncDataSourceConfigId);
            if (syncDataSourceConfigNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find the Synchronization Data Source Configuration with id %s",syncDataSourceConfigId));
            
            List<Relationship> relationshipsToDelete = new ArrayList();
           
            for (Relationship relationship : syncDataSourceConfigNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_CONFIGURATION)) 
                relationshipsToDelete.add(relationship);
            
            for (Relationship relationship : syncDataSourceConfigNode.getRelationships(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP)) 
                relationshipsToDelete.add(relationship);
            
            while (!relationshipsToDelete.isEmpty())
                relationshipsToDelete.remove(0).delete();
            
            syncDataSourceConfigNode.delete();
            tx.success();
        }
    }
    
//    @Override
//    public List<SynchronizationGroup> copySyncGroup(long[] syncGroupIds) throws ApplicationObjectNotFoundException, InvalidArgumentException {
//        try (Transaction tx = graphDb.beginTx()) {
//            List<SynchronizationGroup> result = new ArrayList();
//            
//            for (long syncGroupId : syncGroupIds) {
//                
//                Node syncGroupNode = Util.findNodeByLabelAndId(syncGroupsLabel, syncGroupId);
//                if (syncGroupNode == null)
//                    throw new ApplicationObjectNotFoundException(String.format("The sync group with id %s could not be find", syncGroupId));
//                
//                SynchronizationGroup syncGroup = Util.createSyncGroupFromNode(syncGroupNode);
//                long newSyncGroupId = createSyncGroup(syncGroup.getName(), syncGroup.getProvider().getClass().getName());//TODO: review the second parameter
//                
//                List<SyncDataSourceConfiguration> syncDataSources = syncGroup.getSyncDataSourceConfigurations();
//                for (SyncDataSourceConfiguration syncDataSource : syncDataSources) {
//                    List<StringPair> parameters = new ArrayList();
//                    for (String paramKey : syncDataSource.getParameters().keySet()) {
//                        String paramValue = syncDataSource.getParameters().get(paramKey);
//                        parameters.add(new StringPair(paramKey, paramValue));
//                    }
//                    createSyncDataSourceConfig(newSyncGroupId, syncDataSource.getName(), parameters);
//                }
//                
//                Node newSyncGroupNode = Util.findNodeByLabelAndId(syncGroupsLabel, newSyncGroupId);
//                if (newSyncGroupNode == null)
//                    throw new ApplicationObjectNotFoundException(String.format("The sync group with id %s could not be find", newSyncGroupId));
//                result.add(Util.createSyncGroupFromNode(newSyncGroupNode));
//            }
//            tx.success();
//            return result;
//        }
//    }
    
    @Override
    public void moveSyncDataSourceConfiguration(long oldSyncGroupId, long newSyncGroupId, long[] syncDataSourceConfigurationIds) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node oldSyncGroupNode = Util.findNodeByLabelAndId(syncGroupsLabel, oldSyncGroupId);
            if (oldSyncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The sync group with id %s could not be find", oldSyncGroupId));

            Node newSyncGroupNode = Util.findNodeByLabelAndId(syncGroupsLabel, newSyncGroupId);
            if (newSyncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The sync group with id %s could not be find", newSyncGroupId));
            
            for (long syncDataSrcId : syncDataSourceConfigurationIds) {
                Node syncDataSrcNode = graphDb.getNodeById(syncDataSrcId);
                if (syncDataSrcNode == null)
                    throw new ApplicationObjectNotFoundException(String.format("Synchronization Data Source Configuration with id %s could not be found", syncDataSrcId));
                
                List <Relationship> relsToDelete = new ArrayList<>();
                Iterable<Relationship> relationships = syncDataSrcNode.getRelationships(RelTypes.BELONGS_TO_GROUP, Direction.OUTGOING);
                for (Relationship relationship : relationships) {
                    if(relationship.getEndNodeId() == oldSyncGroupNode.getId())
                        relsToDelete.add(relationship);
                }
                
                for (Relationship relationship : relationships)
                    relationship.delete();

                syncDataSrcNode.createRelationshipTo(newSyncGroupNode, RelTypes.BELONGS_TO_GROUP);
            }
            tx.success();
        }
    }
    
    @Override
    public void releaseSyncDataSourceConfigFromSyncGroup(long syncGroupId, long[] syncDataSourceConfigurationIds) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
                        
            for (long syncDataSrcId : syncDataSourceConfigurationIds) {
                Node syncDataSrcNode = graphDb.getNodeById(syncDataSrcId);
                if (syncDataSrcNode == null)
                    throw new ApplicationObjectNotFoundException(String.format("Synchronization Data Source Configuration with id %s could not be found", syncDataSrcId));

                List<Relationship> relsToDelete = new ArrayList<>();
                Iterable<Relationship> relationships = syncDataSrcNode.getRelationships(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP);
                
                int i = 0;
                for (Relationship relationship : relationships) {
                    i++;
                    if(relationship.getEndNodeId() == syncGroupId)
                        relsToDelete.add(relationship);
                }

                if(i == 1)
                    throw new ApplicationObjectNotFoundException(String.format("datasource Config, id: %s can not be release, must belong at least to one SyncGroup", syncDataSrcId));
                for (Relationship rel : relsToDelete) 
                    rel.delete();
            }
            tx.success();
        }
    }
    
    @Override
    public void copySyncDataSourceConfiguration(long syncGroupId, long[] syncDataSourceConfigurationIds) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node syncGroupNode = Util.findNodeByLabelAndId(syncGroupsLabel, syncGroupId);
            if (syncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The sync group with id %s could not be find", syncGroupId));
                        
            for (long syncDataSrcId : syncDataSourceConfigurationIds) {
                Node syncDataSrcNode = graphDb.getNodeById(syncDataSrcId);
                if (syncDataSrcNode == null)
                    throw new ApplicationObjectNotFoundException(String.format("Synchronization Data Source Configuration with id %s could not be found", syncDataSrcId));
                
                syncDataSrcNode.createRelationshipTo(syncGroupNode, RelTypes.BELONGS_TO_GROUP);
            }
            tx.success();
        }
    }
    //</editor-fold>
    //<editor-fold desc="Process API" defaultstate="collapsed">

    @Override
    public Artifact getArtifactForActivity(long processInstanceId, long activityId) throws ApplicationObjectNotFoundException {
        try {
            return ProcessCache.getInstance().getArtifactForActivity(processInstanceId, activityId);
        } catch (InventoryException ex) {
            throw new ApplicationObjectNotFoundException(ex.getMessage());
        }
    }

    @Override
    public ArtifactDefinition getArtifactDefinitionForActivity(long processDefinitionId, long activityDefinitionId) {
        try {
            return ProcessCache.getInstance().getArtifactDefinitionForActivity(processDefinitionId, activityDefinitionId);
        } catch (InventoryException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public void commitActivity(long processInstanceId, long activityDefinitionId, Artifact artifact) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            Node processInstanceNode = Util.findNodeByLabelAndId(processInstanceLabel, processInstanceId);
            if (processInstanceNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The Process Instance with id %s could not be found", processInstanceId));
            
            try {
                artifact.setCommitDate(new Date().getTime());
                ProcessCache.getInstance().commitActivity(processInstanceId, activityDefinitionId, artifact);
                
                ProcessInstance processInstace = ProcessCache.getInstance().getProcessInstance(processInstanceId);
                                
                processInstanceNode.setProperty(Constants.PROPERTY_CURRENT_ACTIVITY_ID, processInstace.getCurrentActivity());
                                
                if (processInstace.getArtifactsContent() != null)
                    processInstanceNode.setProperty(Constants.PROPERTY_ARTIFACTS_CONTENT, processInstace.getArtifactsContent());
                
            } catch (InventoryException ex) {
                throw new InvalidArgumentException(String.format("The Process Instance could not be commited", processInstanceId));
            }
            
            tx.success();
        }
    }
    
    @Override
    public void updateActivity(long processInstanceId, long activityDefinitionId, Artifact artifact) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            Node processInstanceNode = Util.findNodeByLabelAndId(processInstanceLabel, processInstanceId);
            if (processInstanceNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The Process Instance with id %s could not be found", processInstanceId));
            
            try {
                Artifact anArtifact = ProcessCache.getInstance().getArtifact(processInstanceId, activityDefinitionId);
                if (anArtifact != null) {
                    anArtifact.setId(artifact.getId());
                    anArtifact.setName(artifact.getName());
                    anArtifact.setContentType(artifact.getContentType());
                    anArtifact.setContent(artifact.getContent());
                    anArtifact.setSharedInformation(artifact.getSharedInformation());
                    ProcessCache.getInstance().updateActivity(processInstanceId, activityDefinitionId, anArtifact);
                } else
                    ProcessCache.getInstance().updateActivity(processInstanceId, activityDefinitionId, artifact);
                                    
                ProcessInstance processInstace = ProcessCache.getInstance().getProcessInstance(processInstanceId);
                if (processInstace.getArtifactsContent() != null)
                    processInstanceNode.setProperty(Constants.PROPERTY_ARTIFACTS_CONTENT, processInstace.getArtifactsContent());
                
            } catch (InventoryException ex) {
                throw new InvalidArgumentException(String.format("The Process Instance could not be updated", processInstanceId));
            }
            tx.success();
        }
    }
    
    @Override
    public List<ActivityDefinition> getProcessInstanceActivitiesPath(long processInstanceId) {
        try {
            return ProcessCache.getInstance().getProcessInstanceActivitiesPath(processInstanceId);
        } catch (InventoryException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public ActivityDefinition getNextActivityForProcessInstance(long processInstanceId) {
        try {
            return ProcessCache.getInstance().getNextActivityForProcessInstance(processInstanceId);
        } catch (InventoryException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public ProcessDefinition getProcessDefinition(long processDefinitionId) {
        try {
            return ProcessCache.getInstance().getProcessDefinition(processDefinitionId);
        } catch (InventoryException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    @Override
    public ActivityDefinition getActivityDefinition(long processDefinitionId, long activityDefinitionId) {
        try {
            return ProcessCache.getInstance().getActivityDefinition(processDefinitionId, activityDefinitionId);
        } catch (InventoryException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public void deleteProcessDefinition(long processDefinitionId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateProcessDefinition(long processDefinitionId, List<StringPair> properties, byte[] structure) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long createProcessDefinition(String name, String description, String version, boolean enabled, byte[] structure) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public List<ProcessInstance> getProcessInstances(long processDefinitionId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterator<Node> processInstanceNodes = graphDb.findNodes(processInstanceLabel);
            if (processInstanceNodes != null) {
                List<ProcessInstance> processInstances = new ArrayList();
                while (processInstanceNodes.hasNext()) {
                    Node processInstanceNode = processInstanceNodes.next();
                    ProcessInstance processInstance = Util.createProcessInstanceFromNode(processInstanceNode);
                    
                    if (processInstance.getProcessDefinition() == processDefinitionId) {
                        
                        processInstances.add(processInstance);
                        
                        try {
                            ProcessCache.getInstance().setProcessInstance(processInstance);
                        } catch (InventoryException ex) {
                            throw new ApplicationObjectNotFoundException(ex.getMessage());
                        }
                    }
                }
                try {
                    ProcessCache.getInstance().setProcessInstances(processDefinitionId, processInstances);
                } catch (InventoryException ex) {
                    throw new ApplicationObjectNotFoundException(ex.getMessage());
                }
            }
            tx.success();
        }
        try {
            return ProcessCache.getInstance().getProcessInstances(processDefinitionId);
        } catch (InventoryException ex) {
            throw new ApplicationObjectNotFoundException(ex.getMessage());
        }
    }
    
    @Override
    public List<ProcessDefinition> getProcessDefinitions() {
        return ProcessCache.getInstance().getProcessDefinitions();
    }
    
    @Override
    public ProcessInstance getProcessInstance(long processInstanceId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node processInstanceNode = Util.findNodeByLabelAndId(processInstanceLabel, processInstanceId);
            if (processInstanceNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The Process Instance with id %s could not be found", processInstanceId));
            
            ProcessInstance processInstance = Util.createProcessInstanceFromNode(processInstanceNode);
            
            try {
                ProcessCache.getInstance().setProcessInstance(processInstance);
                return ProcessCache.getInstance().getProcessInstance(processInstance.getId());
            } catch (InventoryException ex) {
                throw new ApplicationObjectNotFoundException(String.format("The Process Instance with id %s could not be found", processInstanceId));
            }
        }
    }
    
    @Override
    public void reloadProcessDefinitions() {
        ProcessCache.getInstance().updateProcessDefinitions();
    }

    @Override
    public long createProcessInstance(long processDefinitionId, String processInstanceName, String processInstanceDescription) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node processInstanceNode = graphDb.createNode(processInstanceLabel);
            processInstanceNode.setProperty(Constants.PROPERTY_PROCESS_DEFINITION_ID, processDefinitionId);
            processInstanceNode.setProperty(Constants.PROPERTY_NAME, processInstanceName != null ? processInstanceName : "");
            processInstanceNode.setProperty(Constants.PROPERTY_DESCRIPTION, processInstanceDescription != null ? processInstanceDescription : "");
            
            try {
                long processInstanceId = ProcessCache.getInstance().createProcessInstance(processInstanceNode.getId(), processDefinitionId, processInstanceName, processInstanceDescription);
                
                ProcessInstance processInstance = ProcessCache.getInstance().getProcessInstance(processInstanceId);
                
                processInstanceNode.setProperty(Constants.PROPERTY_CURRENT_ACTIVITY_ID, processInstance.getCurrentActivity());
                tx.success();
                return processInstanceId;
                
            } catch (InventoryException ex) {
                throw new ApplicationObjectNotFoundException(ex.getMessage());
            }
        }
    }
    
    @Override
    public void deleteProcessInstance(long processInstanceId) throws OperationNotPermittedException {
        try (Transaction tx = graphDb.beginTx()) {
            Node processInstanceNode = Util.findNodeByLabelAndId(processInstanceLabel, processInstanceId);
                        
            for (Relationship rel : processInstanceNode.getRelationships(RelTypes.HAS_PROCESS_INSTANCE, Direction.INCOMING)) {
                
                Node startNode = rel.getStartNode();
                rel.delete();
                deleteObject(startNode, false);
            }
            processInstanceNode.delete();
            
            tx.success();
        } catch(Exception ex) {
             throw new OperationNotPermittedException("Cannot delete process instance, because it still has relationships");
        }
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Configuration Values">
    @Override
    public long createConfigurationVariable(String configVariablesPoolId, String name, String description, int type, 
            boolean masked, String valueDefinition) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        
        if (type > 5 || type < 0)
            throw new InvalidArgumentException(String.format("The specified type (%s) is not valid", type));

        if (name == null || name.trim().isEmpty())
            throw  new InvalidArgumentException("The name of the configuration variable can not be empty");

        try (Transaction tx = graphDb.beginTx()) {
            if (graphDb.findNode(configurationVariables, Constants.PROPERTY_NAME, name) != null)
                throw new InvalidArgumentException(String.format("There is already a configuration value named %s", name));
            
            Node parentPoolNode = graphDb.findNode(configurationVariablesPools, Constants.PROPERTY_UUID, configVariablesPoolId);
            
            if (parentPoolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a pool with id %s", configVariablesPoolId));
            
            Node newConfigVariableNode = graphDb.createNode(configurationVariables);
            newConfigVariableNode.createRelationshipTo(parentPoolNode, RelTypes.CHILD_OF_SPECIAL);
            newConfigVariableNode.setProperty(Constants.PROPERTY_NAME, name);
            newConfigVariableNode.setProperty(Constants.PROPERTY_DESCRIPTION, description != null ? description : "");
            newConfigVariableNode.setProperty(Constants.PROPERTY_TYPE, type);
            newConfigVariableNode.setProperty(Constants.PROPERTY_MASKED, masked);
            newConfigVariableNode.setProperty(Constants.PROPERTY_VALUE, valueDefinition != null ? valueDefinition : "");
            
            tx.success();
            
            return newConfigVariableNode.getId();
        }
    }

    @Override
    public void updateConfigurationVariable(String name, String propertyToUpdate, String newValue) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node configVariableNode = graphDb.findNode(configurationVariables, Constants.PROPERTY_NAME, name);
            
            if (configVariableNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a configuration variable named %s", name));
            
            switch (propertyToUpdate) {
                case Constants.PROPERTY_NAME:
                    if (newValue == null || newValue.trim().isEmpty())
                        throw  new InvalidArgumentException("The name of the configuration variable can not be empty");
                    configVariableNode.setProperty(Constants.PROPERTY_NAME, newValue);
                    break;
                case Constants.PROPERTY_DESCRIPTION:
                    configVariableNode.setProperty(Constants.PROPERTY_DESCRIPTION, newValue);
                    break;
                case Constants.PROPERTY_TYPE:
                    try {
                        int type = Integer.valueOf(newValue);
                        
                        if (type > 5 || type < 0)
                            throw new InvalidArgumentException(String.format("The specified type (%s) is not valid", type));
                        
                        configVariableNode.setProperty(Constants.PROPERTY_TYPE, type);
                    } catch (NumberFormatException ex) {
                        throw new InvalidArgumentException(String.format("Type %s is not a number", newValue));
                    }
                    break;
                case Constants.PROPERTY_MASKED:
                    configVariableNode.setProperty(Constants.PROPERTY_MASKED, Boolean.valueOf(newValue));
                    break;
                case Constants.PROPERTY_VALUE:
                    configVariableNode.setProperty(Constants.PROPERTY_VALUE, newValue);
                    break;
                default:
                    throw new InvalidArgumentException(String.format("Invalid configuration variable property: %s", propertyToUpdate));
            }
            
            tx.success();
            cm.removeConfigurationVariableValue(name); //Removes the variable from the cache, it will be cached the next time it is requested 
        }
    }

    @Override
    public void deleteConfigurationVariable(String name) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node configVariableNode = graphDb.findNode(configurationVariables, Constants.PROPERTY_NAME, name);
            
            if (configVariableNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a configuration variable named %s", name));
            
            configVariableNode.getRelationships().forEach((relationship) -> {
                relationship.delete();
            });
            
            configVariableNode.delete();
            tx.success();
            cm.removeConfigurationVariableValue(name);
        }
    }

    @Override
    public ConfigurationVariable getConfigurationVariable(String name) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node configVariableNode = graphDb.findNode(configurationVariables, Constants.PROPERTY_NAME, name);
            
            if (configVariableNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a configuration variable named %s", name));
            
            return new ConfigurationVariable(configVariableNode.getId(), (String)configVariableNode.getProperty(Constants.PROPERTY_NAME), 
                    (String)configVariableNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                    (String)configVariableNode.getProperty(Constants.PROPERTY_VALUE), 
                    (boolean)configVariableNode.getProperty(Constants.PROPERTY_MASKED), 
                     (int)configVariableNode.getProperty(Constants.PROPERTY_TYPE));
        }
    }

    @Override
    public Object getConfigurationVariableValue(String name) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        if (cm.getConfigurationVariableValue(name) != null)
            return cm.getConfigurationVariableValue(name);
        
        try (Transaction tx = graphDb.beginTx()) {
            Node configVariableNode = graphDb.findNode(configurationVariables, Constants.PROPERTY_NAME, name);
            
            if (configVariableNode == null) 
                throw new ApplicationObjectNotFoundException(String.format("The configuration variable %s could not be found", name));
            
            String rawConfigVariableValue = (String)configVariableNode.getProperty(Constants.PROPERTY_VALUE); //The values are always stored as string, serialized versions of an object.
            Object realConfigVariableValue = null;
            switch ((int)configVariableNode.getProperty(Constants.PROPERTY_TYPE)) {
                case ConfigurationVariable.TYPE_STRING:
                    realConfigVariableValue = rawConfigVariableValue;
                    break;
                case ConfigurationVariable.TYPE_INTEGER:
                    try {
                        realConfigVariableValue = Integer.valueOf(rawConfigVariableValue);
                    } catch (NumberFormatException nfex) {
                        throw new InvalidArgumentException(String.format("Value of configuration variable %s (%s) can not be converted to integer", name, rawConfigVariableValue));
                    }
                    break;
                case ConfigurationVariable.TYPE_FLOAT:
                    try {
                        //In Java, Floats are 32-bits numbers, while Doubles are 64. For the sake of simplicity, while taking a bit more memory, all floats will be taken as doubles
                        realConfigVariableValue =  Double.valueOf(rawConfigVariableValue);
                    } catch (NumberFormatException nfex) {
                        throw new InvalidArgumentException(String.format("Value of configuration variable %s (%s) can not be converted to float", name, rawConfigVariableValue));
                    }
                    break;
                case ConfigurationVariable.TYPE_BOOLEAN:
                    realConfigVariableValue = Boolean.valueOf(rawConfigVariableValue);
                    break;
                case ConfigurationVariable.TYPE_ARRAY: //Not implemented yet
                    return rawConfigVariableValue.split(",");
                case ConfigurationVariable.TYPE_MATRIX:
                    return new ArrayList<>();
                default:
                    throw new InvalidArgumentException(String.format("Unknown data type for variable %s", name));
            }
            
            cm.addConfigurationValue(name, realConfigVariableValue);
            return realConfigVariableValue;
        }
    }

    @Override
    public List<ConfigurationVariable> getConfigurationVariablesInPool(String poolId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node parentPoolNode = graphDb.findNode(configurationVariablesPools, Constants.PROPERTY_UUID, poolId);
            
            if (parentPoolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a pool with id %s", poolId));
            
            List<ConfigurationVariable> res = new ArrayList<>();
            
            parentPoolNode.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING).forEach((childOfRelationship) -> {
                Node configVariableNode = childOfRelationship.getStartNode();
                
                res.add(new ConfigurationVariable(configVariableNode.getId(), (String)configVariableNode.getProperty(Constants.PROPERTY_NAME), 
                    (String)configVariableNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                    (String)configVariableNode.getProperty(Constants.PROPERTY_VALUE), 
                    (boolean)configVariableNode.getProperty(Constants.PROPERTY_MASKED), 
                     (int)configVariableNode.getProperty(Constants.PROPERTY_TYPE)));
            });
            
            tx.success();
            
            res.sort((o1, o2) -> {
                return o1.getName().compareTo(o2.getName());
            });
            
            return res;
        }
    }
    
    @Override
    public List<ConfigurationVariable> getConfigurationVariablesWithPrefix(String prefix) {
        if (prefix == null)
            return new ArrayList();
        if (prefix.isEmpty())
            return new ArrayList();
        
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterator<Node> configVariableNodes = graphDb.findNodes(configurationVariables);
            
            List<ConfigurationVariable> res = new ArrayList<>();
            
            while (configVariableNodes.hasNext()) {
                Node configVariableNode = configVariableNodes.next();
                
                String name = (String) configVariableNode.getProperty(Constants.PROPERTY_NAME);
                
                if (name != null && name.startsWith(prefix)) {
                    ConfigurationVariable configurationVariable = new ConfigurationVariable(configVariableNode.getId(), (String)configVariableNode.getProperty(Constants.PROPERTY_NAME), 
                                                (String)configVariableNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                                                (String)configVariableNode.getProperty(Constants.PROPERTY_VALUE), 
                                                (boolean)configVariableNode.getProperty(Constants.PROPERTY_MASKED), 
                                                 (int)configVariableNode.getProperty(Constants.PROPERTY_TYPE));
                    
                    res.add(configurationVariable);
                }
            }
            return res;            
        }
    }

    @Override
    public List<Pool> getConfigurationVariablesPools() {
        try (Transaction tx = graphDb.beginTx()) {
            List<Pool> res = new ArrayList<>();
            graphDb.findNodes(configurationVariablesPools).stream().forEach((poolNode) -> {
                if (!poolNode.hasProperty(Constants.PROPERTY_UUID))
                    poolNode.setProperty(Constants.PROPERTY_UUID, UUID.randomUUID().toString());
                res.add(new Pool((String)poolNode.getProperty(Constants.PROPERTY_UUID), (String)poolNode.getProperty(Constants.PROPERTY_NAME), (String)poolNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                        "Pool of Configuration Variables", POOL_TYPE_MODULE_ROOT));
            });
            
            tx.success();
            
            res.sort((o1, o2) -> {
                return o1.getName().compareTo(o2.getName());
            });
            
            return res;
        }
    }

    @Override
    public String createConfigurationVariablesPool(String name, String description) throws InvalidArgumentException {
        
        if (name == null || name.trim().isEmpty())
            throw  new InvalidArgumentException("The name of the configuration variables pool can not be empty");
        
        try (Transaction tx = graphDb.beginTx()) {
            Node newPoolNode = graphDb.createNode(configurationVariablesPools);
            String poolId = UUID.randomUUID().toString();
            newPoolNode.setProperty(Constants.PROPERTY_UUID, poolId);
            newPoolNode.setProperty(Constants.PROPERTY_NAME, name);
            newPoolNode.setProperty(Constants.PROPERTY_DESCRIPTION, description == null ? "" : description);
            
            tx.success();
            return poolId;
        }
    }

    @Override
    public void updateConfigurationVariablesPool(String poolId, String propertyToUpdate, String value) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node poolNode = graphDb.findNode(configurationVariablesPools, Constants.PROPERTY_UUID, poolId);
            
            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a pool with id %s", poolId));
            
            switch(propertyToUpdate) {
                case Constants.PROPERTY_NAME:
                    if (value == null || value.trim().isEmpty())
                        throw  new InvalidArgumentException("The name of the pool can not be empty");
                    
                    poolNode.setProperty(Constants.PROPERTY_NAME, value);
                    break;
                case Constants.PROPERTY_DESCRIPTION:
                    poolNode.setProperty(Constants.PROPERTY_DESCRIPTION, value == null ? "" : value);
                    break;
                default:
                    throw new InvalidArgumentException(String.format("Invalid pool property: %s", propertyToUpdate));
            }
            
            tx.success();
        }
    }

    @Override
    public void deleteConfigurationVariablesPool(String poolId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node poolNode = graphDb.findNode(configurationVariablesPools, Constants.PROPERTY_UUID, poolId);
            
            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a pool with id %s", poolId));
            
            poolNode.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING).forEach((childOfRelationship) -> {
                Node configVariableNode = childOfRelationship.getStartNode();
                childOfRelationship.delete();
                configVariableNode.delete();
            });
            
            poolNode.delete();
            
            tx.success();
        }
    }
    
    @Override
    public long createOSPView(String name, String description, byte[] structure) throws InvalidArgumentException {
        
        if (name == null || name.trim().isEmpty())
            throw new InvalidArgumentException("The name of the view can not be empty");
        
        try (Transaction tx = graphDb.beginTx()) {
            
            Node newViewNode = graphDb.createNode(Label.label("ospViews")); //NOI18N
            newViewNode.setProperty(Constants.PROPERTY_NAME, name);
            newViewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description == null ? "" : description);
            newViewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure == null ? new byte[0] : structure);
            
            tx.success();
            return newViewNode.getId();
        }
    }

    @Override
    public ViewObject getOSPView(long viewId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node ospViewNode = graphDb.findNodes(Label.label("ospViews")).stream().filter((viewNode) -> { //NOI18N
                return viewNode.getId() == viewId; 
            }).findFirst().get();
            
            if (ospViewNode == null) 
                throw new ApplicationObjectNotFoundException(String.format("OSP view with id %s could not be found", viewId));
            
            ViewObject res = new ViewObject(viewId, (String)ospViewNode.getProperty(Constants.PROPERTY_NAME), 
                    (String)ospViewNode.getProperty(Constants.PROPERTY_DESCRIPTION), "OSPView"); //NOI18N
            
            res.setStructure((byte[])ospViewNode.getProperty(Constants.PROPERTY_STRUCTURE));
            tx.success();
            
            return res;
        }
    }

    @Override
    public List<ViewObjectLight> getOSPViews() throws InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            List<ViewObjectLight> res = new ArrayList<>();
            graphDb.findNodes(Label.label("ospViews")).stream().forEach((viewNode) -> {
                res.add(new ViewObjectLight(viewNode.getId(), (String)viewNode.getProperty(Constants.PROPERTY_NAME), 
                        (String)viewNode.getProperty(Constants.PROPERTY_DESCRIPTION), "OSPView")); //NOI18N
            });
            
            tx.success();
            return res;
        }
    }

    @Override
    public void updateOSPView(long viewId, String name, String description, byte[] structure) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            Node ospViewNode = graphDb.findNodes(Label.label("ospViews")).stream().filter((viewNode) -> { //NOI18N
                return viewNode.getId() == viewId; 
            }).findFirst().get();
            
            if (ospViewNode == null) 
                throw new ApplicationObjectNotFoundException(String.format("OSP view with id %s could not be found", viewId));
            
            if (name != null) {
                if (name.trim().isEmpty())
                    throw new InvalidArgumentException("The name of the view can not be empty");
                else
                    ospViewNode.setProperty(Constants.PROPERTY_NAME, name);
            }
            
            if (description != null)
                ospViewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            
            if (structure != null)
                ospViewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure);
            
            tx.success();
        }
    }

    //</editor-fold>
    
    //<editor-fold desc="Validators" defaultstate="collapsed">
    @Override
    public long createValidatorDefinition(String name, String description, String classToBeApplied, String script, boolean enabled) 
            throws InvalidArgumentException, MetadataObjectNotFoundException {
        
        if (name == null || name.trim().isEmpty())
            throw new InvalidArgumentException("The validator name can not be empty or null");
        
        if (classToBeApplied == null || classToBeApplied.trim().isEmpty())
            throw new InvalidArgumentException("The validator has to be applied to instances of a valid class");
        
        try (Transaction tx = graphDb.beginTx()) {
            mem.getClass(classToBeApplied); //Checks that the class provided does exist
            
            Node newValidatorNode = graphDb.createNode(validatorDefinitions);
            
            newValidatorNode.setProperty(Constants.PROPERTY_NAME, name);
            newValidatorNode.setProperty(Constants.PROPERTY_DESCRIPTION, description == null ? "" : description);
            newValidatorNode.setProperty(Constants.PROPERTY_SCRIPT, script == null ? "" : script.trim());
            newValidatorNode.setProperty(Constants.PROPERTY_ENABLED, enabled);
            newValidatorNode.setProperty(Constants.PROPERTY_CLASS_NAME, classToBeApplied);
            
            tx.success();
            
            //While not entirely efficient, this will clear all cached validator definitions to prevent that a validator definition 
            //associated to a super class is missed by the caching system
            cm.clearValidatorDefinitionsCache();
            return newValidatorNode.getId();
        }
    }
    
    @Override
    public void updateValidatorDefinition(long validatorDefinitionId, String name, String description, String classToBeApplied, String script, Boolean enabled) 
            throws ApplicationObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = graphDb.beginTx()) {
            
            Node validatorDefinitionNode = graphDb.findNodes(validatorDefinitions).stream().filter((aValidatorDefinitionNode) -> {
                return aValidatorDefinitionNode.getId() == validatorDefinitionId; 
            }).findFirst().get();
            
            if (validatorDefinitionNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Validator definition with id %s could not be found", validatorDefinitionId));
            
            if (name != null) {
                if (name.trim().isEmpty())
                    throw new InvalidArgumentException("The validator name can not be null or empty");
                
                validatorDefinitionNode.setProperty(Constants.PROPERTY_NAME, name);
            }
            
            if (description != null)
                validatorDefinitionNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            
            if (classToBeApplied != null) {
                mem.getClass(classToBeApplied); //Check if the class does exist
                validatorDefinitionNode.setProperty(Constants.PROPERTY_CLASS_NAME, classToBeApplied);
            }
            
            if (script != null)
                validatorDefinitionNode.setProperty(Constants.PROPERTY_SCRIPT, script);
            
            if (enabled != null)
                validatorDefinitionNode.setProperty(Constants.PROPERTY_ENABLED, enabled);
            
            //While not entirely efficient, this will clear all cached validator definitions to prevent that a validator definition 
            //associated to a super class is missed by the caching system
            cm.clearValidatorDefinitionsCache();
            tx.success();
        }
    }
    
    @Override
    public List<ValidatorDefinition> getValidatorDefinitionsForClass(String className) {
        try (Transaction tx = graphDb.beginTx()) {
            List<ValidatorDefinition> res = new ArrayList<>();
            graphDb.findNodes(validatorDefinitions).stream().filter((aValidatorNode) -> {
                    return aValidatorNode.getProperty(Constants.PROPERTY_CLASS_NAME).equals(className);
                }).forEach((aValidatorDefinitionNode) -> {
                    res.add(new ValidatorDefinition(aValidatorDefinitionNode.getId(),
                            (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_NAME), 
                            (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                            (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_CLASS_NAME), 
                            (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_SCRIPT), 
                            (boolean)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_ENABLED)));
                });
            tx.success();
            Collections.sort(res);
            return res;
        }
    }
    
    @Override
    public List<Validator> runValidationsForObject(String objectClass, long objectId) throws BusinessObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            List<Validator> res = new ArrayList<>();
            graphDb.findNodes(validatorDefinitions).forEachRemaining((aValidatorDefinitionNode) -> { // Is it worth to cache this?
                String script = (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_SCRIPT);
                
                if (!script.trim().isEmpty()) { //Skip uninitialized scripts
                    Binding environmentParameters = new Binding();
                    environmentParameters.setVariable("className", objectClass);
                    environmentParameters.setVariable("id", objectId);
                    try {
                        if ((boolean)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_ENABLED) && 
                                mem.isSubclassOf((String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_CLASS_NAME), objectClass)) {
                            GroovyShell shell = new GroovyShell(ApplicationEntityManager.class.getClassLoader(), environmentParameters);
                            Object theResult = shell.evaluate(script);

                            if (theResult instanceof Validator) //The script must return a validator, otherwise, the result will be ignored
                                res.add((Validator)theResult);
                        }
                    } catch (MetadataObjectNotFoundException ex) { } // The validators referring to inexistent classes are ignored
                }
            });
            tx.failure(); //Rollback any non-nested transaction just in case
            return res;
        }
    }
    
    @Override
    public void deleteValidatorDefinition(long validatorDefinitionId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node validatorDefinitionNode = graphDb.findNodes(validatorDefinitions).stream().filter((aValidatorDefinitionNode) -> {
                return aValidatorDefinitionNode.getId() == validatorDefinitionId; 
            }).findFirst().get();
            
            if (validatorDefinitionNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Validator definition with id %s could not be found", validatorDefinitionId));
            
            validatorDefinitionNode.delete();
            
            //While not entirely efficient, this will clear all cached validator definitions to prevent that a validator definition 
            //associated to a super class is missed by the caching system
            cm.clearValidatorDefinitionsCache();
            tx.success();
        }
    }
    //</editor-fold>
    //<editor-fold desc="Outside Plant" defaultstate="collapsed">
    @Override    
    public void deleteOSPView(long viewId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node ospViewNode = graphDb.findNodes(Label.label("ospViews")).stream().filter((viewNode) -> { //NOI18N
                return viewNode.getId() == viewId; 
            }).findFirst().get();
            
            if (ospViewNode == null) 
                throw new ApplicationObjectNotFoundException(String.format("OSP view with id %s could not be found", viewId));
            
            ospViewNode.delete();
            tx.success();
        }
    }

    //</editor-fold>
    //</editor-fold>
//Helpers
    private Node getFavoritesFolderForUser(long favoritesFolderId, long userId) {
        Node userNode = Util.findNodeByLabelAndId(userLabel, userId);

        if (userNode == null)
            return null; // user not found
        
        
        if (userNode.hasRelationship(Direction.OUTGOING, RelTypes.HAS_BOOKMARK)) {
            for (Relationship relationship : userNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_BOOKMARK)) {

                Node favoritesFolderNode = relationship.getEndNode();

                if (favoritesFolderNode.getId() == favoritesFolderId)
                    return favoritesFolderNode;

            }
        }
        return null; //The user doesn't seem to have a favorites folder with that id
    }
      
    public TemplateObject createTemplateElementFromNode (Node templateInstanceNode, String className) throws InvalidArgumentException, 
            MetadataObjectNotFoundException {
        ClassMetadata classMetadata = mem.getClass(className);
        HashMap<String, String> attributes = new HashMap<>();
        String name = "";
        
        for (AttributeMetadata attribute : classMetadata.getAttributes()){
            //Only set the attributes existing in the current node. Please note that properties can't be null in
            //Neo4J, so a null value is actually a non-existing relationship/value
            if (templateInstanceNode.hasProperty(attribute.getName())) {
               String value = String.valueOf(templateInstanceNode.getProperty(attribute.getName()));
                        
                if (Constants.PROPERTY_NAME.equals(attribute.getName()))
                    name = value;

                attributes.put(attribute.getName(),value);
            }
        }
        
        for(Relationship aRelatedToRelationship : templateInstanceNode.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING)) {
            if (!aRelatedToRelationship.hasProperty(Constants.PROPERTY_NAME))
                throw new InvalidArgumentException(String.format("The object with id %s is malformed", templateInstanceNode.getId()));
            
            String relationshipName = (String)aRelatedToRelationship.getProperty(Constants.PROPERTY_NAME);              
            
            if (classMetadata.hasAttribute(relationshipName)) { 
                if (attributes.containsKey(relationshipName))
                    attributes.put(relationshipName, attributes.containsKey(relationshipName) ? "" : attributes.get(relationshipName) + ";" + aRelatedToRelationship.getEndNode().getId());
                else
                    attributes.put(relationshipName, (String)aRelatedToRelationship.getEndNode().getProperty(Constants.PROPERTY_UUID));
            } else //This verification will help us find potential inconsistencies with list types
                                  //What this does is to verify if is there is a RELATED_TO relationship that shouldn't exist because its name is not an attribute of the class
                throw new InvalidArgumentException(String.format("The object %s (%s) is related to list type %s (%s), but that is not consistent with the data model", 
                            templateInstanceNode.getProperty(Constants.PROPERTY_NAME), templateInstanceNode.getId(), aRelatedToRelationship.getEndNode().getProperty(Constants.PROPERTY_NAME), 
                            aRelatedToRelationship.getEndNode().getId()));
        } 

        return new TemplateObject(classMetadata.getName(), (String)templateInstanceNode.getProperty(Constants.PROPERTY_UUID), name, attributes);
    }
   
    /**
     * TODO: The following two methods are duplicated in BEM, this should be re-designed
     */
    private BusinessObjectLight createRemoteObjectLightFromNode (Node instance) throws InvalidArgumentException {
        Node classNode = instance.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).getEndNode();
        
        String instanceUuid = instance.hasProperty(Constants.PROPERTY_UUID) ? (String) instance.getProperty(Constants.PROPERTY_UUID) : null;
        if (instanceUuid == null)
            throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", instance.getId()));
        
        BusinessObjectLight res = new BusinessObjectLight((String)classNode.getProperty(Constants.PROPERTY_NAME), instanceUuid, 
            (String)instance.getProperty(Constants.PROPERTY_NAME));
        
        return res;
    }
    
    private BusinessObject createRemoteObjectFromNode(Node instance) throws InvalidArgumentException {
        String className = (String)instance.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME);
        try {
            return createRemoteObjectFromNode(instance, mem.getClass(className));
        } catch (MetadataObjectNotFoundException mex) {
            throw new InvalidArgumentException(mex.getLocalizedMessage());
        }
    }
    
    private String getNameOfSpecialParentByScaleUpRecursive(Node node, int targetLevel, int currentLevel) {
        if (node.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF_SPECIAL)) {
            for (Relationship rel : node.getRelationships(Direction.OUTGOING, RelTypes.CHILD_OF_SPECIAL)) {
                Node endNode = rel.getEndNode();
                
                if (currentLevel == targetLevel)
                    return endNode.hasProperty(Constants.PROPERTY_NAME) ? (String) endNode.getProperty(Constants.PROPERTY_NAME) : "<Not Set>";
                else
                    return getNameOfSpecialParentByScaleUpRecursive(endNode, targetLevel, currentLevel + 1);
            }
        }
        return null;                
    }
    
    /**
     * Builds a RemoteBusinessObject instance from a node representing a business object
     * @param instance The object as a Node instance.
     * @param classMetadata The class metadata to map the node's properties into a RemoteBussinessObject.
     * @return The business object.
     * @throws InvalidArgumentException If an attribute value can't be mapped into value.
     */
    private BusinessObject createRemoteObjectFromNode(Node instance, ClassMetadata classMetadata) throws InvalidArgumentException {
        
        HashMap<String, String> attributes = new HashMap<>();
        String name = "";
        
        for (AttributeMetadata myAtt : classMetadata.getAttributes()){
            //Only set the attributes existing in the current node. Please note that properties can't be null in
            //Neo4J, so a null value is actually a non-existing relationship/value
            if (instance.hasProperty(myAtt.getName())){
               if (AttributeMetadata.isPrimitive(myAtt.getType())) {
                    String value = String.valueOf(instance.getProperty(myAtt.getName()));

                    if (Constants.PROPERTY_NAME.equals(myAtt.getName()))
                        name = value;

                    attributes.put(myAtt.getName(),value);
                }
            }
        }

        //Iterates through relationships and transform the into "plain" attributes
        Iterable<Relationship> iterableRelationships = instance.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING);
        Iterator<Relationship> relationships = iterableRelationships.iterator();

        while(relationships.hasNext()){
            Relationship relationship = relationships.next();
            if (!relationship.hasProperty(Constants.PROPERTY_NAME))
                throw new InvalidArgumentException(String.format("The object with id %s is malformed", instance.getId()));

            String relationshipName = (String)relationship.getProperty(Constants.PROPERTY_NAME);              
            
            boolean hasRelationship = false;
            for (AttributeMetadata myAtt : classMetadata.getAttributes()) {
                if (myAtt.getName().equals(relationshipName)) {
                    if (attributes.containsKey(relationshipName))
                        attributes.put(relationshipName, attributes.get(relationshipName) + ";" + (String)relationship.getEndNode().getProperty(Constants.PROPERTY_UUID)); //A multiple selection list type
                    else    
                        attributes.put(relationshipName, (String)relationship.getEndNode().getProperty(Constants.PROPERTY_UUID));
                    hasRelationship = true;
                    break;
                }                  
            }
            
            if (!hasRelationship) //This verification will help us find potential inconsistencies with list types
                                  //What this does is to verify if is there is a RELATED_TO relationship that shouldn't exist because its name is not an attribute of the class
                throw new InvalidArgumentException(String.format("The object with %s (%s) is related to list type %s (%s), but that is not consistent with the data model", 
                            instance.getProperty(Constants.PROPERTY_NAME), instance.getId(), relationship.getEndNode().getProperty(Constants.PROPERTY_NAME), relationship.getEndNode().getId()));
        }
        String instanceUuid = instance.hasProperty(Constants.PROPERTY_UUID) ? (String) instance.getProperty(Constants.PROPERTY_UUID) : null;
        if (instanceUuid == null)
            throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", instance.getId()));
        
        BusinessObject res = new BusinessObject(classMetadata.getName(), instanceUuid, name, attributes);
        
        return res;
    }
    
    //The following methods are used 
    
    /**
     * Deletes recursively and object and all its children. Note that the transaction should be handled by the caller
     * @param instance The object to be deleted
     * @param unsafeDeletion True if you want the object to be deleted no matter if it has RELATED_TO, HAS_PROCESS_INSTANCE or RELATED_TO_SPECIAL relationships
     * @throws org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException If the object already has relationships
     */
    private void deleteObject(Node instance, boolean unsafeDeletion) throws OperationNotPermittedException {
        if(!unsafeDeletion && instance.hasRelationship(RelTypes.RELATED_TO, RelTypes.RELATED_TO_SPECIAL, RelTypes.HAS_PROCESS_INSTANCE)) 
            throw new OperationNotPermittedException(String.format("The object with %s (%s) can not be deleted since it has relationships", 
                    instance.getProperty(Constants.PROPERTY_NAME), instance.getProperty(Constants.PROPERTY_UUID)));
        
        for (Relationship rel : instance.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF, RelTypes.CHILD_OF_SPECIAL))
            deleteObject(rel.getStartNode(), unsafeDeletion);
        
        // Searches the related views to delete the nodes in the data base       
        for (Relationship aHasViewRelationship : instance.getRelationships(RelTypes.HAS_VIEW)) {
            Node viewNode = aHasViewRelationship.getEndNode();
            aHasViewRelationship.delete();
            viewNode.delete();
        }
        
        //Now we delete the audit trail entries
        for (Relationship aHasHistoryEntryRelationship : instance.getRelationships(RelTypes.HAS_HISTORY_ENTRY)) {
            Node historyEntryNode = aHasHistoryEntryRelationship.getEndNode();
            aHasHistoryEntryRelationship.delete();
            historyEntryNode.delete();
        }
        
        //Now we dispose of the attachments
        for (Relationship aHasAttachmentRelationship : instance.getRelationships(RelTypes.HAS_ATTACHMENT)) {
            Node attachmentNode = aHasAttachmentRelationship.getEndNode();
            aHasAttachmentRelationship.delete();
            
            String fileName = instance.getProperty(Constants.PROPERTY_UUID) + "_" + attachmentNode.getId(); //NOI18N
            try {
                Files.deleteIfExists(Paths.get(fileName));
                File fileToBeDeleted = new File(fileName);
                fileToBeDeleted.delete();
            } catch (IOException ex) {
                System.out.println(String.format("[KUWAIBA] [%s] An error occurred while deleting attachment %s for object %s (%s)", 
                        Calendar.getInstance().getTime(), fileName, instance.getProperty(Constants.PROPERTY_NAME), instance.getId()));
            }
            attachmentNode.delete();
        }
        
        for (Relationship rel : instance.getRelationships())
            rel.delete();

        instance.delete();
    }
    
    private void addDeviceNodeChildrenAsXml(String id, String className, XMLEventWriter xmlew, XMLEventFactory xmlef) throws XMLStreamException, ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            
            String columnName = "childNode"; //NOI18N

            String cypherQuery = String.format(
                "MATCH (classNode)<-[:%s]-(objectNode)<-[:%s]-(objChildNode) "
              + "WHERE objectNode._uuid = {id} AND classNode.name = {className}"
              + "RETURN objChildNode AS %s"
              , RelTypes.INSTANCE_OF, RelTypes.CHILD_OF, columnName);

            HashMap<String, Object> queryParameters = new HashMap<>();
            queryParameters.put("id", id); //NOI18N
            queryParameters.put("className", className); //NOI18N
            
            Result result = graphDb.execute(cypherQuery, queryParameters);
            Iterator<Node> column = result.columnAs(columnName);

            for (Node deviceNode : Iterators.asIterable(column))
                addDeviceNodeAsXML(deviceNode, id, xmlew, xmlef);
        }
    }
    
    private void addDeviceNodeAsXML(Node deviceNode, String parentId, XMLEventWriter xmlew, XMLEventFactory xmlef) throws XMLStreamException, ApplicationObjectNotFoundException, InvalidArgumentException {
        QName tagDevice = new QName("device"); // NOI18N
        
        String deviceId = (String) deviceNode.getProperty(Constants.PROPERTY_UUID);
        String className = Util.getClassName(deviceNode);

        xmlew.add(xmlef.createStartElement(tagDevice, null, null));
        xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_ID), deviceId));
        xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_NAME), deviceNode.getProperty(Constants.PROPERTY_NAME).toString()));
        xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_CLASS_NAME), className));
        xmlew.add(xmlef.createAttribute(new QName("parentId"), parentId)); //NOI18N     
        
        addDeviceModelAsXML(deviceId, xmlew, xmlef);
        
        xmlew.add(xmlef.createEndElement(tagDevice, null));
        
        addDeviceNodeChildrenAsXml(deviceId, className, xmlew, xmlef);
    }
    
    private void addDeviceModelAsXML(String id, XMLEventWriter xmlew, XMLEventFactory xmlef) throws XMLStreamException, ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            Node objectNode = graphDb.findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, id);
            
            Node modelNode = null;
            
            for (Relationship aListTypeAttributeRelationship : objectNode.getRelationships(Direction.OUTGOING, RelTypes.RELATED_TO)) {
                if (aListTypeAttributeRelationship.getProperty(Constants.PROPERTY_NAME).equals("model")) {
                    modelNode = aListTypeAttributeRelationship.getEndNode();
                    break;
                }
            }
            
            if (modelNode != null && modelNode.hasRelationship(RelTypes.HAS_VIEW, Direction.OUTGOING)) {
                Node layoutNode = modelNode.getSingleRelationship(RelTypes.HAS_VIEW, Direction.OUTGOING).getEndNode();
                QName tagModel = new QName("model");

                xmlew.add(xmlef.createStartElement(tagModel, null, null));
                xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_ID), (String)modelNode.getProperty(Constants.PROPERTY_UUID)));
                xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_CLASS_NAME), 
                        (String)modelNode.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME)));
                xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_NAME), (String)modelNode.getProperty(Constants.PROPERTY_NAME)));

                QName tagView = new QName("view");

                xmlew.add(xmlef.createStartElement(tagView, null, null));
                xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_ID), Long.toString(layoutNode.getId())));
                xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_CLASS_NAME), (String)layoutNode.getProperty(Constants.PROPERTY_CLASS_NAME)));

                QName tagStructure = new QName("structure");
                xmlew.add(xmlef.createStartElement(tagStructure, null, null));
                if (layoutNode.hasProperty(Constants.PROPERTY_STRUCTURE))
                    xmlew.add(xmlef.createCharacters(DatatypeConverter.printBase64Binary((byte[])layoutNode.getProperty(Constants.PROPERTY_STRUCTURE))));
                xmlew.add(xmlef.createEndElement(tagStructure, null));
                xmlew.add(xmlef.createEndElement(tagView, null));  
                xmlew.add(xmlef.createEndElement(tagModel, null));  
            }
        }
    }
}

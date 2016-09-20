/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.ConnectionManager;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.CompactQuery;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.Pool;
import org.kuwaiba.apis.persistence.application.ResultRecord;
import org.kuwaiba.apis.persistence.application.Session;
import org.kuwaiba.apis.persistence.application.Task;
import org.kuwaiba.apis.persistence.application.TaskResult;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.application.ViewObject;
import org.kuwaiba.apis.persistence.application.ViewObjectLight;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectList;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.GenericObjectList;
import org.kuwaiba.services.persistence.cache.CacheManager;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.services.persistence.util.Util;
import org.kuwaiba.util.ChangeDescriptor;
import org.kuwaiba.ws.todeserialize.StringPair;
import org.kuwaiba.ws.toserialize.application.TaskNotificationDescriptor;
import org.kuwaiba.ws.toserialize.application.TaskScheduleDescriptor;
import org.kuwaiba.ws.toserialize.application.UserInfoLight;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.QueryExecutionException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.helpers.collection.IteratorUtil;

/**
 * Application Entity Manager reference implementation
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
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
    private static String DEFAULT_BACKGROUNDS_PATH = "../img/backgrounds";
    /**
     * Class index
     */
    private Index<Node> classIndex;
    /**
     * Object index
     */
    private Index<Node> objectIndex;
    /**
     * Users index
     */
    private Index<Node> userIndex;
    /**
     * Groups index
     */
    private Index<Node> groupIndex;
    /**
     * Queries index; 
     */
    private Index<Node> queryIndex;
    /**
     * Index for list type items (of all classes)
     */
    private Index<Node> listTypeItemsIndex;
    /**
     * Pools index 
     */
    private Index<Node> poolsIndex;
    /**
     * Privilege index 
     */
    private Index<Node> privilegeIndex;
    /**
     * Task index
     */
    private Index<Node> taskIndex;
    /**
     * Index for general views (those not related to a particular object)
     */
    private Index<Node> generalViewsIndex;
    /**
     * Index for special nodes(like group root node)
     */
    private Index<Node> specialNodesIndex;
    /**
     * Reference to the singleton instance of CacheManager
     */
    private CacheManager cm;
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

    public ApplicationEntityManagerImpl(ConnectionManager cmn) {
        this();
        this.graphDb = (GraphDatabaseService) cmn.getConnectionHandler();
        try(Transaction tx = graphDb.beginTx()){
            this.userIndex = graphDb.index().forNodes(Constants.INDEX_USERS);
            this.groupIndex = graphDb.index().forNodes(Constants.INDEX_GROUPS);
            this.queryIndex = graphDb.index().forNodes(Constants.INDEX_QUERIES);
            this.classIndex = graphDb.index().forNodes(Constants.INDEX_CLASS);
            this.listTypeItemsIndex = graphDb.index().forNodes(Constants.INDEX_LIST_TYPE_ITEMS);
            this.objectIndex = graphDb.index().forNodes(Constants.INDEX_OBJECTS);
            this.generalViewsIndex = graphDb.index().forNodes(Constants.INDEX_GENERAL_VIEWS);
            this.poolsIndex = graphDb.index().forNodes(Constants.INDEX_POOLS);
            this.privilegeIndex = graphDb.index().forNodes(Constants.INDEX_PRIVILEGE_NODES);
            this.taskIndex = graphDb.index().forNodes(Constants.INDEX_TASKS);
            this.specialNodesIndex = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES);
            for (Node listTypeNode : listTypeItemsIndex.query(Constants.PROPERTY_ID, "*")){
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
            String lastName, boolean enabled, long[] privileges, long[] groups)
            throws InvalidArgumentException, NotAuthorizedException, NotAuthorizedException 
    {
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
        
        try(Transaction tx = graphDb.beginTx()) {
            Node storedUser = userIndex.get(Constants.PROPERTY_NAME, userName).getSingle();
            if (storedUser != null)
                throw new InvalidArgumentException(String.format("User name %s already exists", userName));
            
            Label label = DynamicLabel.label(Constants.INDEX_USERS);
            Node newUserNode = graphDb.createNode(label);

            newUserNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            newUserNode.setProperty(Constants.PROPERTY_NAME, userName);
            newUserNode.setProperty(Constants.PROPERTY_PASSWORD, Util.getMD5Hash(password));
                
            if(firstName == null)
                firstName = "";
            newUserNode.setProperty(Constants.PROPERTY_FIRST_NAME, firstName);
            if(lastName == null)
                lastName = "";
            newUserNode.setProperty(Constants.PROPERTY_LAST_NAME, lastName);

            newUserNode.setProperty(Constants.PROPERTY_ENABLED, enabled);
  
            if (groups != null){
                for (long groupId : groups){
                    Node group = groupIndex.get(Constants.PROPERTY_ID,groupId).getSingle();
                    if (group != null)
                        newUserNode.createRelationshipTo(group, RelTypes.BELONGS_TO_GROUP);
                    
                    else{
                        tx.failure();
                        throw new InvalidArgumentException(String.format("Group with id %s can not be found",groupId));
                    }
                }
            }
            userIndex.putIfAbsent(newUserNode, Constants.PROPERTY_ID, newUserNode.getId());
            userIndex.putIfAbsent(newUserNode, Constants.PROPERTY_NAME, userName);
                       
            tx.success();
            
            cm.putUser(Util.createUserProfileFromNode(newUserNode));
            return newUserNode.getId();
        }
    }

    @Override
    public void setUserProperties(long oid, String userName, String password, String firstName,
            String lastName, boolean enabled, long[] privileges, long[] groups)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException 
    {
        try(Transaction tx = graphDb.beginTx()) {
            Node userNode = userIndex.get(Constants.PROPERTY_ID, oid).getSingle();
            if(userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a user with id %s",oid));

            if(userName != null){
                if (userName.trim().isEmpty())
                    throw new InvalidArgumentException("User name can not be an empty string");

                if (!userName.matches("^[a-zA-Z0-9_.]*$"))
                    throw new InvalidArgumentException(String.format("The user name %s contains invalid characters", userName));

                Node storedUser = userIndex.get(Constants.PROPERTY_NAME, userName).getSingle();
                if (storedUser != null)
                    throw new InvalidArgumentException(String.format("User name %s already exists", userName));
            }
            if(password != null){
                if (password.trim().isEmpty())
                    throw new InvalidArgumentException("Password can't be an empty string");
            }
        
            if (userName != null){
                //refresh the userindex
                userIndex.remove(userNode, Constants.PROPERTY_NAME, (String)userNode.getProperty(Constants.PROPERTY_NAME));
                cm.removeUser(userName);
                userNode.setProperty(Constants.PROPERTY_NAME, userName);
                userIndex.putIfAbsent(userNode, Constants.PROPERTY_NAME, userName);
            }
            if (password != null)
                userNode.setProperty(Constants.PROPERTY_PASSWORD, Util.getMD5Hash(password));
            if (firstName != null)
                userNode.setProperty(Constants.PROPERTY_FIRST_NAME, firstName);
            if (lastName != null)
                userNode.setProperty(Constants.PROPERTY_LAST_NAME, lastName);
            if (groups != null){
                Iterable<Relationship> relationships = userNode.getRelationships(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP);
                for (Relationship relationship : relationships)
                    relationship.delete();
                for (long id : groups) {
                    Node groupNode = groupIndex.get(Constants.PROPERTY_ID, id).getSingle();
                    userNode.createRelationshipTo(groupNode, RelTypes.BELONGS_TO_GROUP);
                }
            }
            if (privileges != null){
                Iterable<Relationship> privilegesRelationships = userNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_PRIVILEGE);
                for (Relationship relationship : privilegesRelationships)
                    relationship.delete();
                for(long privilegeCode : privileges){
                    Node privilegeNode = privilegeIndex.get(Constants.PROPERTY_CODE, privilegeCode).getSingle();
                    if(privilegeNode != null)
                        privilegeNode.createRelationshipTo(userNode, RelTypes.HAS_PRIVILEGE);
                    else{
                        tx.failure();
                        throw new InvalidArgumentException(String.format("Privilege with coded %s can not be found",privilegeCode));
                    }
                }
            }
            tx.success();
            cm.putUser(Util.createUserProfileFromNode(userNode));
        }
    }

    @Override
    public void setUserProperties(String formerUsername, String newUserName, String password, String firstName,
            String lastName, boolean enabled, long[] privileges, long[] groups)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException 
    {
        try(Transaction tx = graphDb.beginTx()) { 
            Node userNode = userIndex.get(Constants.PROPERTY_NAME, formerUsername).getSingle();
            if(userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find a user with name %s", formerUsername));

            if(newUserName != null)
            {
                if (newUserName.trim().isEmpty())
                    throw new InvalidArgumentException("User name can not be an empty string");

                Node storedUser = userIndex.get(Constants.PROPERTY_NAME, newUserName).getSingle();
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
                userIndex.remove(userNode, Constants.PROPERTY_NAME, (String)userNode.getProperty(Constants.PROPERTY_NAME));
                userNode.setProperty(Constants.PROPERTY_NAME, newUserName);
                userIndex.putIfAbsent(userNode, Constants.PROPERTY_NAME, newUserName);
                cm.removeUser(newUserName);
            }
            if (password != null)
                userNode.setProperty(Constants.PROPERTY_PASSWORD, Util.getMD5Hash(password));
            if(firstName != null)
                userNode.setProperty(Constants.PROPERTY_FIRST_NAME, firstName);
            if(lastName != null)
                userNode.setProperty(Constants.PROPERTY_LAST_NAME, lastName);
            if(groups != null){
                Iterable<Relationship> relationships = userNode.getRelationships(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP);
                for (Relationship relationship : relationships)
                    relationship.delete();
                for (long id : groups) {
                    Node groupNode = groupIndex.get(Constants.PROPERTY_ID, id).getSingle();
                    userNode.createRelationshipTo(groupNode, RelTypes.BELONGS_TO_GROUP);
                }
            }
            if (privileges != null){
                Iterable<Relationship> privilegesRelationships = userNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_PRIVILEGE);
                for (Relationship relationship : privilegesRelationships)
                    relationship.delete();
                for(long privilegeCode : privileges){
                    Node privilegeNode = privilegeIndex.get(Constants.PROPERTY_CODE, privilegeCode).getSingle();
                    if(privilegeNode != null)
                        privilegeNode.createRelationshipTo(userNode, RelTypes.HAS_PRIVILEGE);
                    else{
                        tx.failure();
                        throw new InvalidArgumentException(String.format("Privilege with coded %s can not be found",privilegeCode));
                    }
                }
            }
            tx.success();
            cm.putUser(Util.createUserProfileFromNode(userNode));
        }
    }
    
    @Override
    public long createGroup(String groupName, String description,
            long[] privileges, long[] users) 
            throws InvalidArgumentException, NotAuthorizedException 
    {
        if (groupName == null)
            throw new InvalidArgumentException("Group name can not be null");
        if (groupName.trim().isEmpty())
            throw new InvalidArgumentException("Group name can not be an empty string");
        if (!groupName.matches("^[a-zA-Z0-9_.]*$"))
            throw new InvalidArgumentException(String.format("Class %s contains invalid characters", groupName));
        
        try (Transaction tx = graphDb.beginTx())
        {
            Node storedGroup = groupIndex.get(Constants.PROPERTY_NAME,groupName).getSingle();
            if (storedGroup != null)
                throw new InvalidArgumentException(String.format("Group %s already exists", groupName));

            Label label = DynamicLabel.label(Constants.INDEX_GROUPS);
            Node newGroupNode = graphDb.createNode(label);

            newGroupNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            newGroupNode.setProperty(Constants.PROPERTY_NAME, groupName);
            newGroupNode.setProperty(Constants.PROPERTY_DESCRIPTION, description == null ? "" : description);

            if (users != null){
                for (long userId : users) {
                    Node userNode = userIndex.get(Constants.PROPERTY_ID, userId).getSingle();
                    if(userNode != null)
                        userNode.createRelationshipTo(newGroupNode, RelTypes.BELONGS_TO_GROUP);
                    else{
                        tx.failure();
                        throw new InvalidArgumentException(String.format("User with id %s can not be found",userId));
                    }
                }
            }
            if (privileges != null){
                for(long privilegeCode : privileges){
                    Node privilegeNode = privilegeIndex.get(Constants.PROPERTY_CODE, privilegeCode).getSingle();
                    if(privilegeNode != null)
                        privilegeNode.createRelationshipTo(newGroupNode, RelTypes.HAS_PRIVILEGE);
                    else{
                        tx.failure();
                        throw new InvalidArgumentException(String.format("Privilege with coded %s can not be found",privilegeCode));
                    }
                }
            }
            specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_GROUPS).getSingle().createRelationshipTo(newGroupNode, RelTypes.GROUP);

            groupIndex.putIfAbsent(newGroupNode, Constants.PROPERTY_ID, newGroupNode.getId());
            groupIndex.putIfAbsent(newGroupNode, Constants.PROPERTY_NAME, groupName);
            tx.success();
            cm.putGroup(Util.createGroupProfileFromNode(newGroupNode));
            
            return newGroupNode.getId();
        }
    }

    @Override
    public List<UserProfile> getUsers() throws NotAuthorizedException
    {
        try(Transaction tx = graphDb.beginTx())
        {
            IndexHits<Node> usersNodes = userIndex.query(Constants.PROPERTY_NAME, "*");
            List<UserProfile> users = new ArrayList<>();
            for (Node node : usersNodes)
                users.add(Util.createUserProfileFromNode(node));
            return users;
        }
    }

    @Override
    public List<GroupProfile> getGroups() throws NotAuthorizedException
    {
        try(Transaction tx = graphDb.beginTx()) {
            IndexHits<Node> groupsNodes = groupIndex.query(Constants.PROPERTY_NAME, "*");

            List<GroupProfile> groups =  new ArrayList<>();
            for (Node node : groupsNodes)
                groups.add((Util.createGroupProfileFromNode(node)));
            return groups;
        }
    }

    @Override
    public void setGroupProperties(long id, String groupName, String description,
            long[] privileges, long[] users)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node groupNode = groupIndex.get(Constants.PROPERTY_ID, id).getSingle();
            if(groupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find the group with id %1s",id));
            
            if(groupName != null){
                if (groupName.isEmpty())
                    throw new InvalidArgumentException("Group name can not be an empty string");
                if (!groupName.matches("^[a-zA-Z0-9_.]*$"))
                    throw new InvalidArgumentException(String.format("Class %s contains invalid characters", groupName));

                Node storedGroup = groupIndex.get(Constants.PROPERTY_NAME, groupName).getSingle();
                    if (storedGroup != null)
                        throw new InvalidArgumentException(String.format("The group name %1s is already in use", groupName));
                groupIndex.remove(groupNode, Constants.PROPERTY_NAME, (String)groupNode.getProperty(Constants.PROPERTY_NAME));
                cm.removeGroup((String)groupNode.getProperty(Constants.PROPERTY_NAME));
                groupNode.setProperty(Constants.PROPERTY_NAME, groupName);
                groupIndex.add(groupNode, Constants.PROPERTY_NAME, groupName);
            }
            if(description != null)
                groupNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            if(users != null && users.length != 0){
                Iterable<Relationship> relationships = groupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_GROUP);
                for (Relationship relationship : relationships)
                    relationship.delete();
                for (long userId : users) {
                    Node userNode = userIndex.get(Constants.PROPERTY_ID, userId).getSingle();
                    if(userNode != null)
                        userNode.createRelationshipTo(groupNode, RelTypes.BELONGS_TO_GROUP);
                    else
                        throw new ApplicationObjectNotFoundException(String.format("User with id %s can not be found",userId));
                }
            }
            if (privileges != null && privileges.length != 0){
                Iterable<Relationship> privilegesRelationships = groupNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_PRIVILEGE);
                for (Relationship relationship : privilegesRelationships)
                    relationship.delete();
                for(long privilegeCode : privileges){
                    Node privilegeNode = privilegeIndex.get(Constants.PROPERTY_CODE, privilegeCode).getSingle();
                    if(privilegeNode != null)
                        privilegeNode.createRelationshipTo(groupNode, RelTypes.HAS_PRIVILEGE);
                    else
                        throw new InvalidArgumentException(String.format("Privilege with coded %s can not be found",privilegeCode));
                }
            }
            
            cm.putGroup(Util.createGroupProfileFromNode(groupNode));
            tx.success();
        }
    }

    @Override
    public void deleteUsers(long[] oids)
            throws ApplicationObjectNotFoundException, NotAuthorizedException {
        
        try(Transaction tx = graphDb.beginTx()) {
            //TODO watch if there are relationships you can/should not delete
            if(oids != null){
                for (long id : oids)
                {
                    Node userNode = userIndex.get(Constants.PROPERTY_ID, id).getSingle();
                    if(userNode == null){
                        throw new ApplicationObjectNotFoundException(String.format("Can not find a user with id %s",id));
                    }
                    cm.removeUser((String)userNode.getProperty(Constants.PROPERTY_NAME));
                    Iterable<Relationship> relationships = userNode.getRelationships();
                    for (Relationship relationship : relationships) 
                        relationship.delete();

                    userIndex.remove(userNode);
                    userNode.delete();
                }
            }
            
            tx.success();
        }
    }

    @Override
    public void deleteGroups(long[] oids)
            throws ApplicationObjectNotFoundException, NotAuthorizedException {
        
        try(Transaction tx = graphDb.beginTx()) {
            if(oids != null){
                for (long id : oids) {
                    Node groupNode = groupIndex.get(Constants.PROPERTY_ID, id).getSingle();
                    if(groupNode == null)
                        throw new ApplicationObjectNotFoundException(String.format("Can not find the group with id %s",id));
                    
                    cm.removeGroup((String)groupNode.getProperty(Constants.PROPERTY_NAME));

                    Iterable<Relationship> relationships = groupNode.getRelationships();
                    for (Relationship relationship : relationships) 
                        relationship.delete();
                    
                    groupIndex.remove(groupNode);
                    groupNode.delete();
                }
                tx.success();
            }
        }
    }

    @Override
    public RemoteBusinessObjectLight getListTypeItem(String listTypeName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
        if (listTypeName == null)
           throw new InvalidArgumentException("Item name and class name can not be null");
        GenericObjectList listType = cm.getListType(listTypeName);
        if(listType!=null){
            RemoteBusinessObjectLight rol = new RemoteBusinessObject(listType.getId(), listType.getClassName(), "");
            return rol;
        }
        else
            return null;
    }
    
   //List type related methods
    @Override
   public long createListTypeItem(String className, String name, String displayName)
            throws MetadataObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, NotAuthorizedException 
   {               
       if (name == null || className == null)
           throw new InvalidArgumentException("Item name and class name can not be null");
       
       ClassMetadata myClass= cm.getClass(className);
       try(Transaction tx = graphDb.beginTx()) {
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (classNode ==  null)
                throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s",className));

            if (myClass == null) {
                 myClass = Util.createClassMetadataFromNode(classNode);
                 cm.putClass(myClass);
             }      

            if (!cm.isSubClass(Constants.CLASS_GENERICOBJECTLIST, className))
                 throw new InvalidArgumentException(String.format("Class %s is not a list type", className));

            if (myClass.isInDesign())
                 throw new OperationNotPermittedException("Create List Type Item", "Can not create instances of classes marked as isDesign");

            if (myClass.isAbstract())
                 throw new OperationNotPermittedException("Create List Type Item", "Can not create instances of abstract classes");
       
           Label label = DynamicLabel.label(Constants.LABEL_LIST_TYPE);
           Node newItem = graphDb.createNode(label);
           newItem.setProperty(Constants.PROPERTY_NAME, name);
           if (displayName != null)
               newItem.setProperty(Constants.PROPERTY_DISPLAY_NAME, displayName);
           newItem.createRelationshipTo(classNode, RelTypes.INSTANCE_OF);
           listTypeItemsIndex.putIfAbsent(newItem, Constants.PROPERTY_ID, newItem.getId());
           tx.success();
           GenericObjectList newListType = new GenericObjectList(newItem.getId(), name);
           cm.putListType(newListType);
           return newItem.getId();
        }
    }

    @Override
    public void deleteListTypeItem(String className, long oid, boolean realeaseRelationships) 
            throws MetadataObjectNotFoundException, OperationNotPermittedException, ObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        try(Transaction tx = graphDb.beginTx())
        {
            if (!cm.isSubClass(Constants.CLASS_GENERICOBJECTLIST, className))
                throw new InvalidArgumentException(String.format("Class %s is not a list type", className));

            Node instance = getInstanceOfClass(className, oid);
            Util.deleteObject(instance, realeaseRelationships);
            tx.success();
            cm.removeListType(className);
        }
    }

    @Override
    public List<RemoteBusinessObjectLight> getListTypeItems(String className)
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
        List<RemoteBusinessObjectLight> children = new ArrayList<>();
        try(Transaction tx = graphDb.beginTx()) {
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (classNode ==  null)
                throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s",className));

            if (!Util.isSubClass(Constants.CLASS_GENERICOBJECTLIST, classNode))
                throw new InvalidArgumentException(String.format("Class %s is not a list type", className));

            Iterable<Relationship> childrenAsRelationships = classNode.getRelationships(RelTypes.INSTANCE_OF);
            Iterator<Relationship> relationships = childrenAsRelationships.iterator();

            while(relationships.hasNext()){
                Node child = relationships.next().getStartNode();
                children.add(new RemoteBusinessObjectLight(child.getId(), (String)child.getProperty(Constants.PROPERTY_NAME), className));
            }
        }
        return children;
    }

    @Override
    public List<ClassMetadataLight> getInstanceableListTypes()
            throws ApplicationObjectNotFoundException, NotAuthorizedException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node genericObjectListNode = classIndex.get(Constants.PROPERTY_NAME, Constants.CLASS_GENERICOBJECTLIST).getSingle();

            if (genericObjectListNode == null)
                throw new ApplicationObjectNotFoundException("ClassGenericObjectList not found");

            String cypherQuery = "START classmetadata = node:classes(name = {className}) ".concat(
                                 "MATCH classmetadata <-[:").concat(RelTypes.EXTENDS.toString()).concat("*]-listType ").concat(
                                 "RETURN listType ").concat(
                                 "ORDER BY listType.name ASC");

            Map<String, Object> params = new HashMap<>();
            params.put("className", Constants.CLASS_GENERICOBJECTLIST);//NOI18N
            List<ClassMetadataLight> res = new ArrayList<>();
            Result result = graphDb.execute(cypherQuery, params);
        
            Iterator<Node> n_column = result.columnAs("listType");
            
            for (Node node : IteratorUtil.asIterable(n_column)) {
                if (!(Boolean)node.getProperty(Constants.PROPERTY_ABSTRACT))
                    res.add(Util.createClassMetadataLightFromNode(node));
            }
            return res;
        }
        
    }

    @Override
    public long createObjectRelatedView(long oid, String objectClass, String name, String description, String viewClassName, 
        byte[] structure, byte[] background) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
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
            throws InvalidArgumentException, NotAuthorizedException {
        
        try(Transaction tx = graphDb.beginTx()) {
            
            Node newView = graphDb.createNode();

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
            generalViewsIndex.add(newView, Constants.PROPERTY_ID, newView.getId());
            tx.success();
            return newView.getId();
        }
    }

    @Override
    public ChangeDescriptor updateObjectRelatedView(long oid, String objectClass, long viewId, 
    String name, String description, byte[] structure, byte[] background)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
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
                throw new ObjectNotFoundException("View", viewId); //NOI18N

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
            throws InvalidArgumentException, ObjectNotFoundException, NotAuthorizedException {
        
        try(Transaction tx = graphDb.beginTx()) {
            String affectedProperty = "", oldValue = "", newValue = ""; //NOI18N
            Node gView = generalViewsIndex.get(Constants.PROPERTY_ID, oid).getSingle();
            if (gView == null)
                throw new ObjectNotFoundException("View", oid);
            if (name != null) {
                affectedProperty += Constants.PROPERTY_NAME;
                oldValue = String.valueOf(gView.getProperty(Constants.PROPERTY_NAME));
                gView.setProperty(Constants.PROPERTY_NAME, name);
                newValue = name;
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
    public void deleteGeneralViews(long[] ids) throws ObjectNotFoundException, NotAuthorizedException {
        try(Transaction tx = graphDb.beginTx()) {
            for (long id : ids){
                Node gView = generalViewsIndex.get(Constants.PROPERTY_ID, id).getSingle();
                generalViewsIndex.remove(gView);
                gView.delete();
            }
            tx.success();
        }
    }

    @Override
    public ViewObject getObjectRelatedView(long oid, String objectClass, long viewId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
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
        throw new ObjectNotFoundException("View", viewId);
    }

    @Override
    public List<ViewObjectLight> getObjectRelatedViews(long oid, String objectClass, int limit)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
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
        String cypherQuery = "START gView=node:"+ Constants.INDEX_GENERAL_VIEWS  + "('id:*')";
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
    public ViewObject getGeneralView(long viewId) throws ObjectNotFoundException, NotAuthorizedException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node gView = generalViewsIndex.get(Constants.PROPERTY_ID,viewId).getSingle();

            if (gView == null)
                throw new ObjectNotFoundException("View", viewId);

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
                    System.out.println(e.getMessage());
                }
                aView.setBackground(background);
            }
            return aView;
        }
    }

    //Queries
    @Override
    public long createQuery(String queryName, long ownerOid, byte[] queryStructure,
            String description) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException{
        try(Transaction tx = graphDb.beginTx()) {
            Node queryNode =  graphDb.createNode();
            queryNode.setProperty(CompactQuery.PROPERTY_QUERYNAME, queryName);
            if(description == null)
                description = "";
            queryNode.setProperty(CompactQuery.PROPERTY_DESCRIPTION, description);
            queryNode.setProperty(CompactQuery.PROPERTY_QUERYSTRUCTURE, queryStructure);
            queryNode.setProperty(CompactQuery.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            
            if(ownerOid != -1){
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, false);
                Node userNode = userIndex.get(Constants.PROPERTY_ID, ownerOid).getSingle();

                if(userNode != null)
                    userNode.createRelationshipTo(queryNode, RelTypes.OWNS_QUERY);
            }
            else
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, true);

            queryIndex.putIfAbsent(queryNode, CompactQuery.PROPERTY_ID, queryNode.getId());
            tx.success();
            return queryNode.getId();

        }
    }

    @Override
    public void saveQuery(long queryOid, String queryName, long ownerOid,
            byte[] queryStructure, String description) 
            throws ApplicationObjectNotFoundException, NotAuthorizedException
    {
        try(Transaction tx = graphDb.beginTx()) {
            Node queryNode =  queryIndex.get(CompactQuery.PROPERTY_ID, queryOid).getSingle();
            if(queryNode == null)
                throw new ApplicationObjectNotFoundException(String.format(
                        "Can not find the query with id %s", queryOid));

            queryNode.setProperty(CompactQuery.PROPERTY_QUERYNAME, queryName);
            if(description != null)
                queryNode.setProperty(CompactQuery.PROPERTY_DESCRIPTION, description);
            
            queryNode.setProperty(CompactQuery.PROPERTY_QUERYSTRUCTURE, queryStructure);
            
            if(ownerOid != -1) {
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, false);
                Node userNode = userIndex.get(Constants.PROPERTY_ID, ownerOid).getSingle();
                if(userNode == null)
                    throw new ApplicationObjectNotFoundException(String.format(
                                "Can not find the query with id %s", queryOid));

                Relationship singleRelationship = queryNode.getSingleRelationship(RelTypes.OWNS_QUERY, Direction.INCOMING);

                if(singleRelationship == null)
                    userNode.createRelationshipTo(queryNode, RelTypes.OWNS_QUERY);
            }
            else
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, true);
            tx.success();
        }
    }

    @Override
    public void deleteQuery(long queryOid)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        try(Transaction tx = graphDb.beginTx())
        {
            Node queryNode =  queryIndex.get(CompactQuery.PROPERTY_ID, queryOid).getSingle();
            if(queryNode == null)
                throw new ApplicationObjectNotFoundException(String.format(
                        "Can not find the query with id %1s", queryOid));

            Iterable<Relationship> relationships = queryNode.getRelationships(RelTypes.OWNS_QUERY, Direction.INCOMING);
            for (Relationship relationship : relationships) {
                relationship.delete();
            }
            queryIndex.remove(queryNode);
            queryNode.delete();
            tx.success();
        }
    }

    @Override
    public List<CompactQuery> getQueries(boolean showPublic) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, NotAuthorizedException{
        
        try(Transaction tx = graphDb.beginTx())
        {
            List<CompactQuery> queryList = new ArrayList<>();
            IndexHits<Node> queries = queryIndex.query(CompactQuery.PROPERTY_ID, "*");
            for (Node queryNode : queries)
            {
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
            throws ApplicationObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
        CompactQuery cq =  new CompactQuery();

        try(Transaction tx = graphDb.beginTx()) {
            Node queryNode = queryIndex.get(CompactQuery.PROPERTY_ID, queryOid).getSingle();
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
    public List<ResultRecord> executeQuery(ExtendedQuery query) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        try(Transaction tx = graphDb.beginTx()) {
            CypherQueryBuilder cqb = new CypherQueryBuilder();
            cqb.setClassNodes(getNodesFromQuery(query));
            cqb.createQuery(query);
            return cqb.getResultList();
        }
    }
    
    @Override
    public byte[] getClassHierachy(boolean showAll) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException{
        
        try(Transaction tx = graphDb.beginTx())
        {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            WAX xmlWriter = new WAX(bas);
            StartTagWAX rootTag = xmlWriter.start("hierarchy");
            rootTag.attr("documentVersion", Constants.CLASS_HIERARCHY_NEXT_DOCUMENT_VERSION);
            rootTag.attr("serverVersion", Constants.PERSISTENCE_SERVICE_VERSION);
            rootTag.attr("date", Calendar.getInstance().getTimeInMillis());
            StartTagWAX inventoryTag = rootTag.start("inventory");
            StartTagWAX classesTag = inventoryTag.start("classes");
            Node rootObjectNode = classIndex.get(Constants.PROPERTY_NAME, Constants.CLASS_ROOTOBJECT).getSingle(); //NOI18N
            if (rootObjectNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", Constants.CLASS_ROOTOBJECT));
            getXMLNodeForClass(rootObjectNode, rootTag);
            classesTag.end();
            inventoryTag.end();
            rootTag.end().close();
            return bas.toByteArray();
        }
    }
    
    //Pools
    @Override
    public long createRootPool(String name, String description, String instancesOfClass, int type)
            throws MetadataObjectNotFoundException, NotAuthorizedException {
        try(Transaction tx = graphDb.beginTx()) {
            Node poolNode =  graphDb.createNode();

            if (name != null)
                poolNode.setProperty(Constants.PROPERTY_NAME, name);
            if (description != null)
                poolNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);

            poolNode.setProperty(Constants.PROPERTY_TYPE, type);
            
            ClassMetadata classMetadata = cm.getClass(instancesOfClass);
            
            if (classMetadata == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", instancesOfClass));
            
            poolNode.setProperty(Constants.PROPERTY_CLASS_NAME, instancesOfClass);
                                    
            poolsIndex.putIfAbsent(poolNode, Constants.PROPERTY_ID, poolNode.getId());
            
            tx.success();
            return poolNode.getId();
        }
    }
    
    @Override
    public long createPoolInObject(String parentClassname, long parentId, String name, String description, String instancesOfClass, int type)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, NotAuthorizedException {
        try(Transaction tx = graphDb.beginTx()) {
            Node poolNode =  graphDb.createNode();

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
            
            Node parentNode = objectIndex.get(Constants.PROPERTY_ID, parentId).getSingle();
            if (parentNode == null)
                throw new ObjectNotFoundException(parentClassname, parentId);
            
            poolNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);          
                        
            poolsIndex.putIfAbsent(poolNode, Constants.PROPERTY_ID, poolNode.getId());
            
            tx.success();
            return poolNode.getId();
        }
    }
    
    @Override
    public long createPoolInPool(long parentId, String name, String description, String instancesOfClass, int type)
            throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        try (Transaction tx = graphDb.beginTx()) {
            Node poolNode =  graphDb.createNode();

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
            
            Node parentNode = poolsIndex.get(Constants.PROPERTY_ID, parentId).getSingle();
            
            if (parentNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A pool with id %s could not be found", parentId));
            
            poolNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);          
                        
            poolsIndex.putIfAbsent(poolNode, Constants.PROPERTY_ID, poolNode.getId());
            
            tx.success();
            return poolNode.getId();
        }
    }
    
    @Override
    public void deletePool(long id) throws NotAuthorizedException, ApplicationObjectNotFoundException, OperationNotPermittedException {
        try(Transaction tx = graphDb.beginTx()) {
            Node poolNode = poolsIndex.get(Constants.PROPERTY_ID, id).getSingle();
            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A pool with id %s does not exist", id));

            deletePool(poolNode);
            
            tx.success();
        }
    }
    
    @Override
    public void deletePools(long[] ids) throws NotAuthorizedException, ApplicationObjectNotFoundException, OperationNotPermittedException {
        for (long id : ids)
            deletePool(id);
    }
    
    @Override
    public void setPoolProperties(long poolId, String name, String description) {
        try (Transaction tx = graphDb.beginTx()) {
            Node poolNode = poolsIndex.get(Constants.PROPERTY_ID, poolId).getSingle();
            if(name != null)
                poolNode.setProperty(Constants.PROPERTY_NAME, name);
            if(description != null)
                poolNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            tx.success();
        }
    }
       
    @Override
    public List<Pool> getRootPools(String className, int type, boolean includeSubclasses) 
            throws NotAuthorizedException {
        try(Transaction tx = graphDb.beginTx()) {
            List<Pool> pools  = new ArrayList<>();
            
            IndexHits<Node> poolNodes = poolsIndex.query(Constants.PROPERTY_ID, "*");
            
            while (poolNodes.hasNext()) {
                Node poolNode = poolNodes.next();
                
                if (!poolNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF_SPECIAL)) { //Root pools don't have parents
                    if ((int)poolNode.getProperty(Constants.PROPERTY_TYPE) == type) {
                        
                        //The following conditions could probably normalized, but I think this way,
                        //the code is a bit more readable
                        if (className != null) { //We will return only those matching with the specified class name or its subclasses, depending on the value of includeSubclasses
                            String poolClass = (String)poolNode.getProperty(Constants.PROPERTY_CLASS_NAME);
                            if (includeSubclasses) {
                                if (cm.isSubClass(className, poolClass))
                                    pools.add(Util.createPoolFromNode(poolNode));
                            } else {
                                if (className.equals(poolClass))
                                    pools.add(Util.createPoolFromNode(poolNode));
                            }
                        } else //All pools with no parent are returned
                            pools.add(Util.createPoolFromNode(poolNode));
                    }
                }
            }
            return pools;
        }
    }
    
    @Override
    public List<Pool> getPoolsInObject(String objectClassName, long objectId, String poolClass) 
            throws NotAuthorizedException, ObjectNotFoundException {
        
        try(Transaction tx = graphDb.beginTx()) {
            List<Pool> pools  = new ArrayList<>();
            
            Node objectNode = objectIndex.get(Constants.PROPERTY_ID, objectId).getSingle();
            
            if (objectNode == null)
                throw new ObjectNotFoundException(objectClassName, objectId);
            
            for (Relationship containmentRelationship : objectNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)) {
                if (containmentRelationship.hasProperty(Constants.PROPERTY_NAME) && 
                        Constants.REL_PROPERTY_POOL.equals(containmentRelationship.getProperty(Constants.PROPERTY_NAME))){
                    Node poolNode = containmentRelationship.getStartNode();
                    if (poolClass != null) { //We will return only those matching with the specified class name
                        if (poolClass.equals((String)poolNode.getProperty(Constants.PROPERTY_CLASS_NAME)))
                            pools.add(Util.createPoolFromNode(poolNode));
                    } else
                        pools.add(Util.createPoolFromNode(poolNode));
                }
            }
            return pools;
        }
    }
    
    @Override
    public List<Pool> getPoolsInPool(long parentPoolId, String poolClass) 
            throws NotAuthorizedException, ApplicationObjectNotFoundException {
        
        try(Transaction tx = graphDb.beginTx()) {
            List<Pool> pools  = new ArrayList<>();
            
            Node parentPoolNode = poolsIndex.get(Constants.PROPERTY_ID, parentPoolId).getSingle();
            
            if (parentPoolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The pool with id %s could not be found", parentPoolId));
            
            for (Relationship containmentRelationship : parentPoolNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)) {
                Node poolNode = containmentRelationship.getStartNode();
                
                if (poolNode.hasRelationship(Direction.OUTGOING, RelTypes.INSTANCE_OF)) //The pool items and the pools themselves also have CHILD_OF_SPECIAL relationships
                    continue;
                
                if (poolClass != null) { //We will return only those matching with the specified class name
                    if (poolClass.equals((String)poolNode.getProperty(Constants.PROPERTY_CLASS_NAME)))
                        pools.add(Util.createPoolFromNode(poolNode));
                } else
                    pools.add(Util.createPoolFromNode(poolNode));
            }
            return pools;
        }
    }
           
    @Override
    public Pool getPool(long poolId) throws NotAuthorizedException, ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node poolNode = poolsIndex.get(Constants.PROPERTY_ID, poolId).getSingle();
            
            if (poolNode != null) {                
                
                String name = poolNode.hasProperty(Constants.PROPERTY_NAME) ? 
                                    (String)poolNode.getProperty(Constants.PROPERTY_NAME) : null;
                
                String description = poolNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? 
                                    (String)poolNode.getProperty(Constants.PROPERTY_DESCRIPTION) : null;
                
                String className = poolNode.hasProperty(Constants.PROPERTY_CLASS_NAME) ? 
                                    (String)poolNode.getProperty(Constants.PROPERTY_CLASS_NAME) : null;
                
                int type = poolNode.hasProperty(Constants.PROPERTY_TYPE) ? 
                                    (int)poolNode.getProperty(Constants.PROPERTY_TYPE) : 0;
                
                return new Pool(poolId, name, description, className, type);
            }
            else
                throw new ApplicationObjectNotFoundException(String.format("Pool with id %s could not be found", poolId));
        }
    }
    
    @Override
    public List<RemoteBusinessObjectLight> getPoolItems(long poolId, int limit)
            throws ApplicationObjectNotFoundException, NotAuthorizedException
    {
        try(Transaction tx = graphDb.beginTx()) {
            Node poolNode = poolsIndex.get(Constants.PROPERTY_ID, poolId).getSingle();

            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The pool with id %s could not be found", poolId));

            List<RemoteBusinessObjectLight> poolItems  = new ArrayList<>();

            int i = 0;
            for (Relationship rel : poolNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)){
                if (limit != -1){
                    if (i >= limit)
                         break;
                    i++;
                }
                if(rel.hasProperty(Constants.PROPERTY_NAME)){
                    if(rel.getProperty(Constants.PROPERTY_NAME).equals(Constants.REL_PROPERTY_POOL)){
                        Node item = rel.getStartNode();
                        Node temp = poolsIndex.get(Constants.PROPERTY_ID, item.getId()).getSingle();
                        if(temp == null) //if is not a pool
                        {
                            RemoteBusinessObjectLight rbol = new RemoteBusinessObjectLight(item.getId(), 
                                                item.hasProperty(Constants.PROPERTY_NAME) ? (String)item.getProperty(Constants.PROPERTY_NAME) : null,
                                                Util.getClassName(item));
                            poolItems.add(rbol);
                        }
                    }
                }
            }
            return poolItems;
        }
    }
    
    @Override
    public List<ActivityLogEntry> getBusinessObjectAuditTrail(String objectClass, long objectId, int limit) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException 
    {
        try(Transaction tx = graphDb.beginTx()) {
            if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, objectClass))
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
    public List<ActivityLogEntry> getGeneralActivityAuditTrail(int page, int limit) 
            throws NotAuthorizedException {        
        try(Transaction tx = graphDb.beginTx()) {
            Node generalActivityLogNode = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).
                    get(Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG).getSingle();

            List<ActivityLogEntry> log = new ArrayList<>();
            int i = 0, toBeSkipped = 0;
            int lowerLimit = page * limit - limit;
            for (Relationship rel : generalActivityLogNode.getRelationships(Direction.INCOMING,RelTypes.CHILD_OF_SPECIAL)){
                if (toBeSkipped < lowerLimit){
                    toBeSkipped++;
                    continue;
                }

                if (limit != 0){
                    if (i < limit)
                        i++;
                    else
                        break;
                }
                Node logEntry = rel.getStartNode();
                Node relatedObject = logEntry.hasRelationship(Direction.INCOMING, RelTypes.HAS_HISTORY_ENTRY) ?
                                        logEntry.getSingleRelationship(RelTypes.HAS_HISTORY_ENTRY, Direction.INCOMING).getStartNode() : null;

                log.add(new ActivityLogEntry(logEntry.getId(), relatedObject == null ? 0 : relatedObject.getId(), (Integer)logEntry.getProperty(Constants.PROPERTY_TYPE), 
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
    public void validateCall(String methodName, String ipAddress, String sessionId)
            throws NotAuthorizedException{
        Session aSession = sessions.get(sessionId);
//        try {
//        if(cm!=null){
//            for (Privilege privilege : cm.getUser(user.getUserName()).getPrivileges()){
//                if(privilege.getMethodName().contentEquals(methodName))
//                    return;
//            }
//            for (GroupProfile groupProfile : cm.getUser(user.getUserName()).getGroups()) {
//                for (Privilege privilege : groupProfile.getPrivileges()){
//                    if(privilege.getMethodName().equals(methodName))
//                        return;
//                }
//            }
//        }
            if(aSession == null)
                throw new NotAuthorizedException("Invalid session ID");
            if (!aSession.getIpAddress().equals(ipAddress))
                throw new NotAuthorizedException(String.format("The IP %s does not match with the one registered for this session", ipAddress));
    }

    @Override
    public Session createSession(String userName, String password, String IPAddress) throws ApplicationObjectNotFoundException 
    {
        if (userName == null || password == null)
            throw  new ApplicationObjectNotFoundException("User or Password can not be null");
        try(Transaction tx = graphDb.beginTx()) {
            Node userNode = userIndex.get(Constants.PROPERTY_NAME,userName).getSingle();
            
            if (userNode == null)
                throw new ApplicationObjectNotFoundException("User does not exist");

            if (!(Boolean)userNode.getProperty(Constants.PROPERTY_ENABLED))
                throw new ApplicationObjectNotFoundException("This user is not enabled");

            if (Util.getMD5Hash(password).equals(userNode.getProperty(Constants.PROPERTY_PASSWORD))){
                UserProfile user = Util.createUserProfileFromNode(userNode);
                cm.putUser(user);
            }
            else
                throw new ApplicationObjectNotFoundException("User or password incorrect");

            for (Session aSession : sessions.values()){
                if (aSession.getUser().getUserName().equals(userName)){
                    Logger.getLogger("createSession").log(Level.INFO, String.format("An existing session for user %s has been dropped", aSession.getUser().getUserName()));
                    sessions.remove(aSession.getToken());
                    break;
                }
            }
            Session newSession = new Session(Util.createUserProfileFromNode(userNode), IPAddress);
            sessions.put(newSession.getToken(), newSession);
            
            return newSession;
        }
    }
    
    @Override
    public UserProfile getUserInSession(String IPAddress, String sessionId) throws NotAuthorizedException{
        validateCall("getUserInSession", IPAddress, sessionId);
        return sessions.get(sessionId).getUser();
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
    public int[] executePatch() throws NotAuthorizedException {
        int executedFiles = 0;
        BufferedReader br = null;
        File patchDirectory = new File(Constants.PACTHES_PATH);
        int totalPatchFiles = patchDirectory.listFiles().length;

        for (File patchFile : patchDirectory.listFiles()) {
            if (!patchFile.getName().contains("~") && !patchFile.getName().endsWith(".ole")) {
                try {
                    br = new BufferedReader(new FileReader(patchFile));
                    String line = br.readLine();
                    while (line != null) {
                        if (line.startsWith("#"))
                            System.out.println(line);
                        if (line.startsWith(Constants.DATABASE_SENTENCE)) {
                            String cypherQuery = br.readLine();
                            graphDb.execute(cypherQuery);
                        }
                        line = br.readLine();
                    }
                    File readFile = new File(patchFile.getPath()+".ole");
                    patchFile.renameTo(readFile);
                    executedFiles++;
                }  catch (IOException | QueryExecutionException ex) {
                    throw new RuntimeException(ex.getMessage());
                } finally {
                    try {
                        if (br != null) {
                            br.close();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, "executePatch: {0}", ex.getMessage()); //NOI18N
                    }
                }
            }
        }//end for
        return new int[]{executedFiles, totalPatchFiles};
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

            Node generalActivityLogNode = specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG).getSingle();

            if (generalActivityLogNode == null)
                throw new ApplicationObjectNotFoundException("The general activity log node can not be found. The databse could be corrupted");

            Util.createActivityLogEntry(null, generalActivityLogNode, userName, type,
                    Calendar.getInstance().getTimeInMillis(), null, null, null, notes);

            tx.success();        
        }
    }
    
    @Override
    public void createGeneralActivityLogEntry(String userName, int type, ChangeDescriptor changeDescriptor) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {

            Node generalActivityLogNode = specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG).getSingle();

            if (generalActivityLogNode == null)
                throw new ApplicationObjectNotFoundException("The general activity log node can not be found. The database could be corrupted");

            Util.createActivityLogEntry(null, generalActivityLogNode, userName, type,
                    Calendar.getInstance().getTimeInMillis(), changeDescriptor.getAffectedProperties(), 
                    changeDescriptor.getOldValues(), changeDescriptor.getNewValues(), changeDescriptor.getNotes());

            tx.success();  
        }
    }
    
    @Override
    public void createObjectActivityLogEntry(String userName, String className, long oid, int type, 
        String affectedProperties, String oldValues, String newValues, String notes) throws ApplicationObjectNotFoundException, ObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
        
            Node objectNode = objectIndex.get(Constants.PROPERTY_ID, oid).getSingle();
            if (objectNode == null)
                throw new ObjectNotFoundException(className, oid);

            Node activityNode  = specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_OBJECT_ACTIVITY_LOG).getSingle();
            if (activityNode == null)
                throw new ApplicationObjectNotFoundException("The object activity log node can not be found. The database could be corrupted");

            Util.createActivityLogEntry(objectNode, activityNode, userName, type, Calendar.getInstance().getTimeInMillis(), affectedProperties, oldValues, newValues, notes);

            tx.success();
        }
    }
    
    @Override
    public void createObjectActivityLogEntry(String userName, String className, long oid,  
            int type, ChangeDescriptor changeDescriptor) throws ApplicationObjectNotFoundException, ObjectNotFoundException {
        createObjectActivityLogEntry(userName, className, oid, type, changeDescriptor.getAffectedProperties(), changeDescriptor.getOldValues(), changeDescriptor.getNewValues(), changeDescriptor.getNotes());
    }

    @Override
    public long createTask(String name, String description, boolean enabled, String script, List<StringPair> parameters, TaskScheduleDescriptor schedule, TaskNotificationDescriptor notificationType) {
        try(Transaction tx = graphDb.beginTx()) {
            Node taskNode = graphDb.createNode();
            taskNode.setProperty(Constants.PROPERTY_NAME, name == null ? "" : name);
            taskNode.setProperty(Constants.PROPERTY_DESCRIPTION, description == null ? "" : description);
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
            
            taskIndex.putIfAbsent(taskNode, Constants.PROPERTY_ID, taskNode.getId());
            tx.success();
            return taskNode.getId();
        }
    }

    @Override
    public void updateTaskProperties(long taskId, String propertyName, String propertyValue) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            Node taskNode = taskIndex.get(Constants.PROPERTY_ID, taskId).getSingle();
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));

            switch (propertyName) {
                case Constants.PROPERTY_NAME:
                case Constants.PROPERTY_DESCRIPTION:
                case Constants.PROPERTY_SCRIPT:
                    taskNode.setProperty(propertyName, propertyValue);
                    break;
                case Constants.PROPERTY_ENABLED:
                    taskNode.setProperty(propertyName, Boolean.valueOf(propertyValue));
                    break;
                default:
                    throw new InvalidArgumentException(String.format("%s is not a valid task property", propertyName));
            }
            tx.success();
        }
    }

    @Override
    public void updateTaskParameters(long taskId, List<StringPair> parameters) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node taskNode = taskIndex.get(Constants.PROPERTY_ID, taskId).getSingle();
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));

            for (StringPair parameter : parameters) {
                String actualParameterName = "PARAM_" + parameter.getKey();
                //The parameters are stored with a prefix PARAM_
                //params set to null, must be deleted
                if (taskNode.hasProperty(actualParameterName) && parameter.getValue() == null)
                    taskNode.removeProperty(actualParameterName);
                else
                    taskNode.setProperty(actualParameterName, parameter.getValue());
            }
            
            tx.success();
        }
    }

    @Override
    public void updateTaskSchedule(long taskId, TaskScheduleDescriptor schedule) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node taskNode = taskIndex.get(Constants.PROPERTY_ID, taskId).getSingle();
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));

            taskNode.setProperty(Constants.PROPERTY_EXECUTION_TYPE, schedule.getExecutionType());
            taskNode.setProperty(Constants.PROPERTY_EVERY_X_MINUTES, schedule.getEveryXMinutes());
            taskNode.setProperty(Constants.PROPERTY_START_TIME, schedule.getStartTime());
            
            tx.success();
        }
    }

    @Override
    public void updateTaskNotificationType(long taskId, TaskNotificationDescriptor notificationType) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node taskNode = taskIndex.get(Constants.PROPERTY_ID, taskId).getSingle();
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));

            taskNode.setProperty(Constants.PROPERTY_NOTIFICATION_TYPE, notificationType.getNotificationType());
            taskNode.setProperty(Constants.PROPERTY_EMAIL, notificationType.getEmail() == null ? "" : notificationType.getEmail());
            
            tx.success();
        }
    }

    @Override
    public void deleteTask(long taskId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node taskNode = taskIndex.get(Constants.PROPERTY_ID, taskId).getSingle();
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));

            taskIndex.remove(taskNode);
            
            for (Relationship rel : taskNode.getRelationships()) //Unsubscribe users
                rel.delete();
            
            taskNode.delete();
            
            tx.success();
        }
    }

    @Override
    public void subscribeUserToTask(long userId, long taskId) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            Node taskNode = taskIndex.get(Constants.PROPERTY_ID, taskId).getSingle();
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
            
            Node userNode = userIndex.get(Constants.PROPERTY_ID, userId).getSingle();
            if (userNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A user with id %s could not be found", userId));
            
            Relationship rel = userNode.createRelationshipTo(taskNode, RelTypes.SUBSCRIBED_TO);
            rel.setProperty(Constants.PROPERTY_NAME, "task"); //NOI18N
            
            tx.success();
        }
    }

    @Override
    public void unsubscribeUserFromTask(long userId, long taskId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node taskNode = taskIndex.get(Constants.PROPERTY_ID, taskId).getSingle();
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));
            
            boolean found = false;
            
            for (Relationship rel : taskNode.getRelationships(Direction.INCOMING, RelTypes.SUBSCRIBED_TO)) {
                if (rel.getStartNode().getId() == userId) {
                    rel.delete();
                    found = true;
                    break;
                }
            }
            
            if (!found)
                throw new ApplicationObjectNotFoundException(String.format("A user with id %s could not be found", taskId));

            tx.success();
        }
    }

    @Override
    public Task getTask(long taskId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node taskNode = taskIndex.get(Constants.PROPERTY_ID, taskId).getSingle();
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));
                        
            return Util.createTaskFromNode(taskNode);
        }
    }
    
    @Override
    public List<UserInfoLight> getSubscribersForTask(long taskId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node taskNode = taskIndex.get(Constants.PROPERTY_ID, taskId).getSingle();
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
            IndexHits<Node> taskNodes = taskIndex.query(Constants.PROPERTY_ID, "*");
            List<Task> allTasks = new ArrayList<>();
            for (Node taskNode : taskNodes)               
                allTasks.add(Util.createTaskFromNode(taskNode));
            
            return allTasks;
        }
    }

    @Override
    public List<Task> getTasksForUser(long userId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node userNode = userIndex.get(Constants.PROPERTY_ID, userId).getSingle();
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
        try (Transaction tx = graphDb.beginTx()) {
            Node taskNode = taskIndex.get(Constants.PROPERTY_ID, taskId).getSingle();
            if (taskNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A task with id %s could not be found", taskId));
            if (!taskNode.hasProperty(Constants.PROPERTY_SCRIPT))
                throw new InvalidArgumentException(String.format("The task with id %s does not have a script", taskId));
            
            String script = (String)taskNode.getProperty(Constants.PROPERTY_SCRIPT);
            
            Iterable<String> allProperties = taskNode.getPropertyKeys();
            HashMap<String, String> scriptParameters = new HashMap<>();
            for (String property : allProperties) {
                if (property.startsWith("PARAM_"))
                    scriptParameters.put(property.replace("PARAM_", ""), (String)taskNode.getProperty(property));
            }
            
            Binding environmentParameters = new Binding();
            environmentParameters.setVariable("graphDb", graphDb); //NOI18N
            environmentParameters.setVariable("objectIndex", objectIndex); //NOI18N
            environmentParameters.setVariable("classIndex", classIndex); //NOI18N
            environmentParameters.setVariable("TaskResult", TaskResult.class); //NOI18N
            environmentParameters.setVariable("Constants", Constants.class); //NOI18N
            environmentParameters.setVariable("Direction", Direction.class); //NOI18N
            environmentParameters.setVariable("RelTypes", RelTypes.class); //NOI18N
            environmentParameters.setVariable("scriptParameters", scriptParameters); //NOI18N
            try {
                GroovyShell shell = new GroovyShell(environmentParameters);
                Object theResult = shell.evaluate(script);
                
                if (theResult == null)
                    throw new InvalidArgumentException("The script returned a null object. Please check the syntax.");
                else if (!TaskResult.class.isInstance(theResult))
                    throw new InvalidArgumentException("The script does not return a TaskResult object. Please check the return value.");
                
                return (TaskResult)theResult;
                
            } catch(Exception ex) {
                return TaskResult.createErrorResult(ex.getMessage());
            }
        }
    }
    
    //Comercial modules

    @Override
    public void registerCommercialModule(GenericCommercialModule module) throws NotAuthorizedException {
        if (module.getName() != null)
            commercialModules.put(module.getName(), module);
        else
            throw new IllegalArgumentException("A module can not have an empty name");
    }

    @Override
    public GenericCommercialModule getCommercialModule(String moduleName) throws NotAuthorizedException {
        return commercialModules.get(moduleName);
    }

    @Override
    public Collection<GenericCommercialModule> getCommercialModules() throws NotAuthorizedException {
        return commercialModules.values();
    }
    
    @Override
    public HashMap<String, RemoteBusinessObjectList> executeCustomDbCode(String dbCode) throws NotAuthorizedException {
        try (Transaction tx = graphDb.beginTx()) {
        
            Result theResult = graphDb.execute(dbCode);
            HashMap<String, RemoteBusinessObjectList> thePaths = new HashMap<>();
            
            for (String column : theResult.columns())
                thePaths.put(column, new RemoteBusinessObjectList());
            
            try {
                while (theResult.hasNext()) {
                    Map<String, Object> row = theResult.next();
                    for (String column : row.keySet()) 
                        thePaths.get(column).add(Util.createRemoteObjectFromNode((Node)row.get(column)));
                }
            } catch (InvalidArgumentException ex) {} //this should not happen
            
            return thePaths;
        }
    }
    

    // Helpers
    /**
     * recursive method used to generate a single "class" node (see the <a href="http://neotropic.co/kuwaiba/wiki/index.php?title=XML_Documents#To_describe_the_data_model">wiki</a> for details)
     * @param classNode Node representing the class to be added
     * @param paretTag Parent to attach the new class node
     */
    private void getXMLNodeForClass(Node classNode, StartTagWAX parentTag) {
        int applicationModifiers = 0;
        int javaModifiers = 0;
        StartTagWAX currentTag = parentTag.start("class"); //NOI18N
        currentTag.attr("id", classNode.getId());
        currentTag.attr("name", classNode.getProperty(Constants.PROPERTY_NAME));        
        currentTag.attr("classPackage", "");
        
        //Application modifiers
        if ((Boolean)classNode.getProperty(Constants.PROPERTY_COUNTABLE))
            applicationModifiers |= Constants.CLASS_MODIFIER_COUNTABLE;

        if ((Boolean)classNode.getProperty(Constants.PROPERTY_CUSTOM))
            applicationModifiers |= Constants.CLASS_MODIFIER_CUSTOM;

        currentTag.attr("applicationModifiers",applicationModifiers);

        //Language modifiers
        if ((Boolean)classNode.getProperty(Constants.PROPERTY_ABSTRACT))
            javaModifiers |= Modifier.ABSTRACT;
        
        currentTag.attr("javaModifiers",javaModifiers);
        
        //Class type
        if (classNode.getProperty(Constants.PROPERTY_NAME).equals(Constants.CLASS_ROOTOBJECT)){
            currentTag.attr("classType",Constants.CLASS_TYPE_ROOT);
        }else{
            if (Util.isSubClass("InventoryObject", classNode))
                currentTag.attr("classType",Constants.CLASS_TYPE_INVENTORY);
            else{
                if (Util.isSubClass("ApplicationObject", classNode))
                    currentTag.attr("classType",Constants.CLASS_TYPE_APPLICATION);
                else
                    currentTag.attr("classType",Constants.CLASS_TYPE_OTHER);
            }
        }

        StartTagWAX attributesTag = currentTag.start("attributes");
        for (Relationship relWithAttributes : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE, Direction.OUTGOING)){
            StartTagWAX attributeTag = attributesTag.start("attribute");
            Node attributeNode = relWithAttributes.getEndNode();
            int attributeApplicationModifiers = 0;
            attributeTag.attr("name", attributeNode.getProperty(Constants.PROPERTY_NAME));
            attributeTag.attr("type", attributeNode.getProperty(Constants.PROPERTY_TYPE));
            attributeTag.attr("javaModifiers", 0); //Not used
            //Application modifiers
            if ((Boolean)attributeNode.getProperty(Constants.PROPERTY_NO_COPY))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_NOCOPY;
            if ((Boolean)attributeNode.getProperty(Constants.PROPERTY_VISIBLE))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_VISIBLE;
            if ((Boolean)attributeNode.getProperty(Constants.PROPERTY_ADMINISTRATIVE))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_ADMINISTRATIVE;
            if ((Boolean)attributeNode.getProperty(Constants.PROPERTY_READ_ONLY))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_READONLY;
            attributeTag.attr("applicationModifiers", attributeApplicationModifiers);
            attributeTag.end();
        }
        attributesTag.end();

        StartTagWAX subclassesTag = currentTag.start("subclasses");
        for (Relationship relWithSubclasses : classNode.getRelationships(RelTypes.EXTENDS, Direction.INCOMING))
            getXMLNodeForClass(relWithSubclasses.getStartNode(), currentTag);

        subclassesTag.end();
        currentTag.end();
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
        try(Transaction tx = graphDb.beginTx())
        {
            for(String className : ListClassNames)
                classNodes.put(className, classIndex.get(Constants.PROPERTY_NAME, className).getSingle());
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
    
    private Node getInstanceOfClass(String className, long oid) 
            throws MetadataObjectNotFoundException, ObjectNotFoundException, NotAuthorizedException
    {
        //Note that for this method, the caller should handle the transaction
        //if any of the parameters is null, return the dummy root
        if (className == null)
            return specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();

        Node classNode = classIndex.get(Constants.PROPERTY_NAME,className).getSingle();
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));

        Iterable<Relationship> iteratorInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        Iterator<Relationship> instances = iteratorInstances.iterator();

        while (instances.hasNext()){
            Node otherSide = instances.next().getStartNode();
            if (otherSide.getId() == oid)
                return otherSide;
        }
        throw new ObjectNotFoundException(className, oid);
    }    
    
    public void deletePool(Node poolNode) throws OperationNotPermittedException {
        
        for (Relationship containmentRelationship : poolNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)) {
                //A pool may have inventory objects as children or other pools
                Node child = containmentRelationship.getStartNode();
                if (child.hasRelationship(RelTypes.INSTANCE_OF)) //It's an inventory object
                    Util.deleteObject(child, false);
                else
                    deletePool(child); //Although making deletePool to receive a node as argument would be more efficient,
                                               //the impact is not that much since the number of pools is expected to be low
            }
            
            //Removes any remaining relationships
            for (Relationship remainingRelationship : poolNode.getRelationships())
                remainingRelationship.delete();
            
            poolsIndex.remove(poolNode);
            poolNode.delete();
    }
    
    //End of Helpers   
}
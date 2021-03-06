/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.persistenceservice.impl;

import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.CompactQuery;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.ResultRecord;
import org.kuwaiba.apis.persistence.application.Session;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.application.ViewObject;
import org.kuwaiba.apis.persistence.application.ViewObjectLight;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.interfaces.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.GenericObjectList;
import org.kuwaiba.persistenceservice.caching.CacheManager;
import org.kuwaiba.persistenceservice.queries.CypherQueryBuilder;
import org.kuwaiba.persistenceservice.util.Constants;
import org.kuwaiba.persistenceservice.util.Util;
import org.kuwaiba.psremoteinterfaces.ApplicationEntityManagerRemote;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;

/**
 * Application Entity Manager reference implementation
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ApplicationEntityManagerImpl implements ApplicationEntityManager, ApplicationEntityManagerRemote {
    /**
     * Graph db service
     */
    private GraphDatabaseService graphDb;
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
     * Index for general views (those not related to a particular object)
     */
    private Index<Node> generalViewsIndex;
    /**
     * Index for special nodes(like group root node)
     */
    private Index<Node> specialNodesIndex;
    /**
     * Instance of business entity manager
     */
    private  BusinessEntityManagerImpl bem;
    /**
     * Reference to the singleton instance of CacheManager
     */
    private CacheManager cm;
    /**
     * Hashmap with the current sessions. The key is the username, the value is the respective session object
     */
    private HashMap<String, Session> sessions;
    
    public ApplicationEntityManagerImpl() {
        this.cm = CacheManager.getInstance();
    }

    public ApplicationEntityManagerImpl(ConnectionManager cmn) {
        this();
        this.graphDb = (EmbeddedGraphDatabase)cmn.getConnectionHandler();
        this.userIndex = graphDb.index().forNodes(Constants.INDEX_USERS);
        this.groupIndex = graphDb.index().forNodes(Constants.INDEX_GROUPS);
        this.queryIndex = graphDb.index().forNodes(Constants.INDEX_QUERIES);
        this.classIndex = graphDb.index().forNodes(Constants.INDEX_CLASS);
        this.listTypeItemsIndex = graphDb.index().forNodes(Constants.INDEX_LIST_TYPE_ITEMS);
        this.objectIndex = graphDb.index().forNodes(Constants.INDEX_OBJECTS);
        this.generalViewsIndex = graphDb.index().forNodes(Constants.INDEX_GENERAL_VIEWS);
        this.poolsIndex = graphDb.index().forNodes(Constants.INDEX_POOLS);
        this.privilegeIndex = graphDb.index().forNodes(Constants.INDEX_PRIVILEGE_NODES);
        this.specialNodesIndex = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES);
        
        for (Node listTypeNode : listTypeItemsIndex.query(Constants.PROPERTY_ID, "*")){
            GenericObjectList aListType = Util.createGenericObjectListFromNode(listTypeNode);
            cm.putListType(aListType);
        }
        
        this.sessions = new HashMap<String, Session>();
    }
    
    public HashMap<String, Session> getSessions(){
        return sessions;
    }

    //TODO add ipAddress, sessionId
    @Override
    public long createUser(String userName, String password, String firstName,
            String lastName, boolean enabled, long[] privileges, long[] groups)
            throws InvalidArgumentException, NotAuthorizedException, NotAuthorizedException {
        //validateCall("createUser", ipAddres, sessionId);
        Transaction tx = null;
        
        if (userName == null){
            throw new InvalidArgumentException("User name can not be null", Level.INFO);
        }
        if (userName.trim().equals("")){
            throw new InvalidArgumentException("User name can not be an empty string", Level.INFO);
        }
        if (!userName.matches("^[a-zA-Z0-9_]*$"))
            throw new InvalidArgumentException(String.format("Class %s contains invalid characters", userName), Level.INFO);
        if (password == null){
            throw new InvalidArgumentException("Password can not be null", Level.INFO);
        }
        if (password.trim().equals("")){
            throw new InvalidArgumentException("Password can not be an empty string", Level.INFO);
        }
        Node storedUser = userIndex.get(Constants.PROPERTY_NAME,userName).getSingle();
        if (storedUser != null){
            throw new InvalidArgumentException(String.format("User name %s already exists", userName), Level.WARNING);
        }
        try{
            tx = graphDb.beginTx();

            Node newUser = graphDb.createNode();

            newUser.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            newUser.setProperty(Constants.PROPERTY_NAME, userName);
            newUser.setProperty(Constants.PROPERTY_PASSWORD, Util.getMD5Hash(password));

            if(firstName == null){
                firstName = "";
            }
            newUser.setProperty(Constants.PROPERTY_FIRST_NAME, firstName);
            if(lastName == null){
                lastName = "";
            }
            newUser.setProperty(Constants.PROPERTY_LAST_NAME, lastName);

            newUser.setProperty(Constants.PROPERTY_ENABLED, enabled);
  //        TODO privileges
  //        if (privileges != null || privileges.size()<1)
  //            newUser.setProperty(UserProfile.PROPERTY_PRIVILEGES, privileges);
            if (groups != null){
                for (long groupId : groups){
                    Node group = groupIndex.get(Constants.PROPERTY_ID,groupId).getSingle();
                    if (group != null){
                        newUser.createRelationshipTo(group, RelTypes.BELONGS_TO_GROUP);
                    }
                    else{
                        tx.failure();
                        tx.finish();
                        throw new InvalidArgumentException(String.format("Group with id %s can not be found",groupId), Level.OFF);
                    }
                }
            }
            userIndex.putIfAbsent(newUser, Constants.PROPERTY_ID, newUser.getId());
            userIndex.putIfAbsent(newUser, Constants.PROPERTY_NAME, userName);
            
            tx.success();
            
            cm.putUser(Util.createUserProfileFromNode(newUser));

            return newUser.getId();
           
        }catch(Exception ex){
            Logger.getLogger("Create user: "+ex.getMessage()); //NOI18N
            if (tx != null){
                tx.failure();
            }
            throw new RuntimeException(ex.getMessage());
        } finally {
            if (tx != null){
                tx.finish();
            }
        }
    }

    @Override
    public void setUserProperties(long oid, String userName, String password, String firstName,
            String lastName, boolean enabled, long[] privileges, long[] groups, String ipAddress, String sessionId)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        
        validateCall("setUserProperties", ipAddress, sessionId);
        
        Node userNode = userIndex.get(Constants.PROPERTY_ID, oid).getSingle();
        if(userNode == null)
            throw new ApplicationObjectNotFoundException(String.format("Can not find a user with id %s",oid));
        
        Transaction tx = null;
        if(userName != null){
            if (userName.trim().equals(""))
                throw new InvalidArgumentException("User name can not be an empty string", Level.INFO);
        
            if (!userName.matches("^[a-zA-Z0-9_]*$"))
                throw new InvalidArgumentException(String.format("Class %s contains invalid characters", userName), Level.INFO);

            Node storedUser = userIndex.get(Constants.PROPERTY_NAME, userName).getSingle();
            if (storedUser != null)
                throw new InvalidArgumentException(String.format("User name %s already exists", userName), Level.WARNING);
        }
        if(password != null){
            if (password.trim().isEmpty())
                throw new InvalidArgumentException("Password can't be an empty string", Level.INFO);
        }
        
        try{
            tx =  graphDb.beginTx();
            if (userName != null){
                //refresh the userindex
                userIndex.remove(userNode, Constants.PROPERTY_NAME, (String)userNode.getProperty(Constants.PROPERTY_NAME));
                cm.removeUser(userName);
                userNode.setProperty(Constants.PROPERTY_NAME, userName);
                userIndex.putIfAbsent(userNode, Constants.PROPERTY_NAME, userName);
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
                        tx.finish();
                        throw new InvalidArgumentException(String.format("Privilege with coded %s can not be found",privilegeCode), Level.OFF);
                    }
                }
            }
            
            tx.success();
            tx.finish();
            cm.putUser(Util.createUserProfileFromNode(userNode));
        }catch(Exception ex){
            Logger.getLogger("setUserProperties: "+ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public void setUserProperties(String formerUsername, String newUserName, String password, String firstName,
            String lastName, boolean enabled, long[] privileges, long[] groups, String ipAddress, String sessionId)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        
        //validateCall("setUserProperties", ipAddress, sessionId);
        
        Node userNode = userIndex.get(Constants.PROPERTY_NAME, formerUsername).getSingle();
        if(userNode == null)
            throw new ApplicationObjectNotFoundException(String.format("Can not find a user with name %s", formerUsername));
        
        if(newUserName != null){
            if (newUserName.isEmpty())
                throw new InvalidArgumentException("User name can not be an empty string", Level.INFO);

            Node storedUser = userIndex.get(Constants.PROPERTY_NAME, newUserName).getSingle();
            if (storedUser != null)
                throw new InvalidArgumentException(String.format("User name %s already exists", newUserName), Level.WARNING);
            
            if (!newUserName.matches("^[a-zA-Z0-9_]*$"))
                throw new InvalidArgumentException(String.format("Class %s contains invalid characters", newUserName), Level.INFO);
        }
        if(password != null){
            if (password.trim().isEmpty())
                throw new InvalidArgumentException("Password can't be an empty string", Level.INFO);
        }

        Transaction tx = null;
        try{
            tx =  graphDb.beginTx();
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
                        tx.finish();
                        throw new InvalidArgumentException(String.format("Privilege with coded %s can not be found",privilegeCode), Level.OFF);
                    }
                }
            }
            
            tx.success();
            tx.finish();
            cm.putUser(Util.createUserProfileFromNode(userNode));
        }catch(Exception ex){
            Logger.getLogger("setUserProperties: " + ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    @Override
    public long createGroup(String groupName, String description,
            long[] privileges, long[] users) 
            throws InvalidArgumentException, NotAuthorizedException {
        
        //validateCall("createGroup", ipAddress, sessionId);
               
        if (groupName == null)
            throw new InvalidArgumentException("Group name can not be null", Level.INFO);
        if (groupName.isEmpty())
            throw new InvalidArgumentException("Group name can not be an empty string", Level.INFO);
        if (!groupName.matches("^[a-zA-Z0-9_]*$"))
            throw new InvalidArgumentException(String.format("Class %s contains invalid characters", groupName), Level.INFO);
        
        Node storedGroup = groupIndex.get(Constants.PROPERTY_NAME,groupName).getSingle();
        if (storedGroup != null)
            throw new InvalidArgumentException(String.format("Group %s already exists", groupName), Level.WARNING);
        
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node newGroupNode = graphDb.createNode();

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
                        tx.finish();
                        throw new InvalidArgumentException(String.format("User with id %s can not be found",userId), Level.OFF);
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
                        tx.finish();
                        throw new InvalidArgumentException(String.format("Privilege with coded %s can not be found",privilegeCode), Level.OFF);
                    }
                }
            }
            specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_GROUPS).getSingle().createRelationshipTo(newGroupNode, RelTypes.GROUP);
           
            groupIndex.putIfAbsent(newGroupNode, Constants.PROPERTY_ID, newGroupNode.getId());
            groupIndex.putIfAbsent(newGroupNode, Constants.PROPERTY_NAME, groupName);
            tx.success();
            tx.finish();
            cm.putGroup(Util.createGroupProfileFromNode(newGroupNode));
            
            return newGroupNode.getId();
            
        }catch(Exception ex){
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public List<UserProfile> getUsers(String ipAddress, String sessionId) throws NotAuthorizedException{
        
        validateCall("getUsers", ipAddress, sessionId);
        
        IndexHits<Node> usersNodes = userIndex.query(Constants.PROPERTY_NAME, "*");

        List<UserProfile> users = new ArrayList<UserProfile>();
        
        for (Node node : usersNodes){
            users.add(Util.createUserProfileFromNode(node));
        }
        return users;
    }

    @Override
    public List<GroupProfile> getGroups(String ipAddress, String sessionId) throws NotAuthorizedException{
        
        validateCall("getGroups", ipAddress, sessionId);
        
        IndexHits<Node> groupsNodes = groupIndex.query(Constants.PROPERTY_NAME, "*");

        List<GroupProfile> groups =  new ArrayList<GroupProfile>();
        for (Node node : groupsNodes)
            groups.add((Util.createGroupProfileFromNode(node)));
        return groups;
    }

    @Override
    public void setGroupProperties(long id, String groupName, String description,
            long[] privileges, long[] users, String ipAddress, String sessionId)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        
        validateCall("setGroupProperties", ipAddress, sessionId);
        
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();

            Node groupNode = groupIndex.get(Constants.PROPERTY_ID, id).getSingle();
            if(groupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find the group with id %1s",id));
            
            if(groupName != null){
                if (groupName.isEmpty())
                    throw new InvalidArgumentException("Group name can not be an empty string", Level.INFO);
                if (!groupName.matches("^[a-zA-Z0-9_]*$"))
                    throw new InvalidArgumentException(String.format("Class %s contains invalid characters", groupName), Level.INFO);

                Node storedGroup = groupIndex.get(Constants.PROPERTY_NAME, groupName).getSingle();
                    if (storedGroup != null)
                        throw new InvalidArgumentException(String.format("The group name %1s is already in use", groupName), Level.WARNING);
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
                    else{
                        tx.failure();
                        tx.finish();
                        throw new InvalidArgumentException(String.format("User with id %s can not be found",userId), Level.OFF);
                    }
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
                    else{
                        tx.failure();
                        tx.finish();
                        throw new InvalidArgumentException(String.format("Privilege with coded %s can not be found",privilegeCode), Level.OFF);
                    }
                }
            }
            
            cm.putGroup(Util.createGroupProfileFromNode(groupNode));
            tx.success();
            tx.finish();
        }catch(Exception ex){
            Logger.getLogger("setGroupProperties: " + ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public void deleteUsers(long[] oids, String ipAddress, String sessionId)
            throws ApplicationObjectNotFoundException, NotAuthorizedException {
        
        validateCall("deleteUsers", ipAddress, sessionId);
        
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            //TODO watch if there is relationships you can not delete
            if(oids != null){
                for (long id : oids)
                {
                    Node userNode = userIndex.get(Constants.PROPERTY_ID, id).getSingle();
                    if(userNode == null){
                        throw new ApplicationObjectNotFoundException(String.format("Can not find a user with id %s",id));
                    }
                    cm.removeUser((String)userNode.getProperty(Constants.PROPERTY_NAME));
                    Iterable<Relationship> relationships = userNode.getRelationships();
                    for (Relationship relationship : relationships) {
                        relationship.delete();
                    }
                    userIndex.remove(userNode);
                    userNode.delete();
                }
            }
            tx.success();
            tx.finish();
        }catch(Exception ex){
            Logger.getLogger("deleteUsers: "+ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();    
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public void deleteGroups(long[] oids, String ipAddress, String sessionId)
            throws ApplicationObjectNotFoundException, NotAuthorizedException {
        
        validateCall("deleteGroups", ipAddress, sessionId);
        
        Transaction tx = null;
        try{
            if(oids != null){
                tx = graphDb.beginTx();
                for (long id : oids) {
                    Node groupNode = groupIndex.get(Constants.PROPERTY_ID, id).getSingle();
                    if(groupNode == null)
                        throw new ApplicationObjectNotFoundException(String.format("Can not find the group with id %1s",id));
                    
                    cm.removeGroup((String)groupNode.getProperty(Constants.PROPERTY_NAME));

                    Iterable<Relationship> relationships = groupNode.getRelationships();
                    for (Relationship relationship : relationships) 
                        relationship.delete();
                    
                    groupIndex.remove(groupNode);
                    groupNode.delete();
                }
                tx.success();
                tx.finish();
            }
        }catch(Exception ex){
            Logger.getLogger("deleteGroups: "+ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public RemoteBusinessObjectLight getListTypeItem(String listTypeName, String ipAddress, String sessionId) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
        validateCall("getListTypeItem", ipAddress, sessionId);
        
        if (listTypeName == null)
           throw new InvalidArgumentException("Item name and class name can not be null", Level.INFO);
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
   public long createListTypeItem(String className, String name, String displayName, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
       
       validateCall("createListTypeItem", ipAddress, sessionId);
        
       if (name == null || className == null){
           throw new InvalidArgumentException("Item name and class name can not be null", Level.INFO);
       }
       
       Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
       if (classNode ==  null){
           throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s",className));
       }
       if (!cm.isSubClass(Constants.CLASS_GENERICOBJECTLIST, className)){
            throw new InvalidArgumentException(String.format("Class %s is not a list type", className), Level.WARNING);
       }
       Transaction tx = null;
       try{
           tx = graphDb.beginTx();
           Node newItem = graphDb.createNode();
           newItem.setProperty(Constants.PROPERTY_NAME, name);
           if (displayName != null)
               newItem.setProperty(Constants.PROPERTY_DISPLAY_NAME, displayName);
           newItem.createRelationshipTo(classNode, RelTypes.INSTANCE_OF);
           listTypeItemsIndex.putIfAbsent(newItem, Constants.PROPERTY_ID, newItem.getId());
           tx.success();
           tx.finish();
           GenericObjectList newListType = new GenericObjectList(newItem.getId(), name);
           cm.putListType(newListType);
           return newItem.getId();
        }catch(Exception ex){
            Logger.getLogger("createListTypeItem: "+ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public void deleteListTypeItem(String className, long oid, boolean realeaseRelationships, String ipAddress, String sessionId) 
            throws MetadataObjectNotFoundException, OperationNotPermittedException, ObjectNotFoundException, NotAuthorizedException {
       
        validateCall("deleteListTypeItem", ipAddress, sessionId);
        
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            if (!cm.isSubClass(Constants.CLASS_GENERICOBJECTLIST, className))
                throw new InvalidArgumentException(String.format("Class %s is not a list type", className), Level.WARNING);

            Node instance = getInstanceOfClass(className, oid);
            Util.deleteObject(instance, realeaseRelationships);

            tx.success();
            tx.finish();
            cm.removeListType(className);
            
        }catch(Exception ex){
            Logger.getLogger("deleteListTypeItem: "+ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteBusinessObjectLight> getListTypeItems(String className, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
        validateCall("getListTypeItems", ipAddress, sessionId);
        
        Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
        if (classNode ==  null)
            throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s",className));

        if (!Util.isSubClass(Constants.CLASS_GENERICOBJECTLIST, classNode))
            throw new InvalidArgumentException(String.format("Class %s is not a list type", className), Level.WARNING);

        Iterable<Relationship> childrenAsRelationships = classNode.getRelationships(RelTypes.INSTANCE_OF);
        List<RemoteBusinessObjectLight> children = new ArrayList<RemoteBusinessObjectLight>();

        while(childrenAsRelationships.iterator().hasNext()){
            Node child = childrenAsRelationships.iterator().next().getStartNode();
            children.add(new RemoteBusinessObjectLight(child.getId(), (String)child.getProperty(Constants.PROPERTY_NAME), className));
        }
        return children;
    }

    @Override
    public List<ClassMetadataLight> getInstanceableListTypes(String ipAddress, String sessionId)
            throws ApplicationObjectNotFoundException, NotAuthorizedException {
        
        validateCall("getInstanceableListTypes", ipAddress, sessionId);
        
        Node genericObjectListNode = classIndex.get(Constants.PROPERTY_NAME, Constants.CLASS_GENERICOBJECTLIST).getSingle();

        if (genericObjectListNode == null)
            throw new ApplicationObjectNotFoundException("ClassGenericObjectList not found");

        String cypherQuery = "START classmetadata = node:classes(name = {className}) ".concat(
                             "MATCH classmetadata <-[:").concat(RelTypes.EXTENDS.toString()).concat("*]-listType ").concat(
                             "RETURN listType ").concat(
                             "ORDER BY listType.name ASC");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("className", Constants.CLASS_GENERICOBJECTLIST);//NOI18N

        ExecutionEngine engine = new ExecutionEngine(graphDb);
        ExecutionResult result = engine.execute(cypherQuery, params);
        Iterator<Node> n_column = result.columnAs("listType");
        List<ClassMetadataLight> res = new ArrayList<ClassMetadataLight>();
        for (Node node : IteratorUtil.asIterable(n_column))
        {
            if (!(Boolean)node.getProperty(Constants.PROPERTY_ABSTRACT))
                res.add(Util.createClassMetadataLightFromNode(node));
        }

        return res;
    }

    @Override
    public long createObjectRelatedView(long oid, String objectClass, String name, String description, int viewType, 
        byte[] structure, byte[] background, String ipAddress, String sessionId) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
        validateCall("createObjectRelatedView", ipAddress, sessionId);
        
        if (objectClass == null)
            throw new InvalidArgumentException("The root object can not be related to any view", Level.INFO);
        
        Node instance = getInstanceOfClass(objectClass, oid);
        Transaction tx = null;
        try{

            tx = graphDb.beginTx();

            Node viewNode = graphDb.createNode();
            viewNode.setProperty(Constants.PROPERTY_TYPE, viewType);
            instance.createRelationshipTo(viewNode, RelTypes.HAS_VIEW);

            if (name != null)
                viewNode.setProperty(Constants.PROPERTY_NAME, name);

            if (structure != null)
                viewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure);

            if (background != null){
                try{
                    Properties props = new Properties();
                    String fileName = "view-" + oid + "-" + viewNode.getId() + "-" + viewType;
                    props.load(new FileInputStream("persistence.properties"));
                    Util.saveFile(props.getProperty("background_path"), fileName, background);
                    viewNode.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                }catch(Exception ex){
                    throw new InvalidArgumentException(String.format("Background image for view %s could not be saved: %s",
                            oid, ex.getMessage()), Level.SEVERE);
                }
            }

            if (description != null)
                viewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);

            tx.success();
            tx.finish();
            return viewNode.getId();
        }catch (Exception ex){
            Logger.getLogger("createObjectRelatedView: "+ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public long createGeneralView(int viewType, String name, String description, byte[] structure, byte[] background, String ipAddress, String sessionId)
            throws InvalidArgumentException, NotAuthorizedException {
        
        validateCall("createGeneralView", ipAddress, sessionId);
        
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node newView = graphDb.createNode();

            newView.setProperty(Constants.PROPERTY_TYPE, viewType);
            if (name != null)
                newView.setProperty(Constants.PROPERTY_NAME, name);
            if (description != null)
                newView.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            if (structure != null)
                newView.setProperty(Constants.PROPERTY_STRUCTURE, structure);
            if (background != null){
                try{
                    Properties props = new Properties();
                    String fileName = "view-" + newView.getId() + "-" + viewType;
                    props.load(new FileInputStream("persistence.properties"));
                    Util.saveFile(props.getProperty("background_path"), fileName, background);
                    newView.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                }catch(Exception ex){
                    throw new InvalidArgumentException(String.format("Background image for view %s couldn't be saved: %s", 
                            newView.getId(), ex.getMessage()), Level.SEVERE);
                }
            }
            generalViewsIndex.add(newView, Constants.PROPERTY_ID, newView.getId());
            tx.success();
            tx.finish();
            return newView.getId();
        }catch (Exception ex){
            Logger.getLogger("createGeneralView: "+ ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public void updateObjectRelatedView(long oid, String objectClass, long viewId, 
    String name, String description, byte[] structure, byte[] background, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
        validateCall("updateObjectRelatedView", ipAddress, sessionId);
        
        if (objectClass == null)
            throw new InvalidArgumentException("The root object does not have any view", Level.INFO);
        Node instance = getInstanceOfClass(objectClass, oid);
        Transaction tx = null;
        try{
            Node viewNode = null;
            for (Relationship rel : instance.getRelationships(RelTypes.HAS_VIEW, Direction.OUTGOING)){
                if (rel.getEndNode().getId() == viewId){
                    viewNode = rel.getEndNode();
                    break;
                }
            }

            if (viewNode == null)
                throw new ObjectNotFoundException("View", viewId); //NOI18N

            tx = graphDb.beginTx();

            if (name != null)
                viewNode.setProperty(Constants.PROPERTY_NAME, name);

            if (structure != null)
                viewNode.setProperty(Constants.PROPERTY_STRUCTURE, structure);

            if (description != null)
                viewNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);

            Properties props = new Properties();
            props.load(new FileInputStream("persistence.properties"));
            String fileName = "view-" + oid + "-" + viewId + "-" + viewNode.getProperty(Constants.PROPERTY_TYPE);
            if (background != null){
                try{
                    Util.saveFile(props.getProperty("background_path"), fileName, background);
                    viewNode.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                }catch(Exception ex){
                    throw new InvalidArgumentException(String.format("Background image for view %s couldn't be saved: %s",
                            oid, ex.getMessage()), Level.SEVERE);
                }
            }
            else{
                if (viewNode.hasProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME)){
                    try{
                        new File(props.getProperty("background_path") + "/" + fileName).delete();
                    }catch(Exception ex){
                        throw new InvalidArgumentException(String.format("View background %s couldn't be deleted: %s", 
                                props.getProperty("background_path") + "/" + fileName, ex.getMessage()), Level.SEVERE);
                    }
                    viewNode.removeProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                }
            }

            tx.success();
            tx.finish();
        }catch (Exception ex){
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public void updateGeneralView(long oid, String name, String description, byte[] structure, byte[] background, String ipAddress, String sessionId)
            throws InvalidArgumentException, ObjectNotFoundException, NotAuthorizedException {
        
        validateCall("updateGeneralView", ipAddress, sessionId);
        
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node gView = generalViewsIndex.get(Constants.PROPERTY_ID, oid).getSingle();
            if (gView == null)
                throw new ObjectNotFoundException("View", oid);
            if (name != null)
                gView.setProperty(Constants.PROPERTY_NAME, name);
            if (description != null)
                gView.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            if (structure != null)
                gView.setProperty(Constants.PROPERTY_STRUCTURE, structure);
            if (background != null){
                if (background.length != 0){
                    try{
                        Properties props = new Properties();
                        String fileName = "view-" + oid + "-" + gView.getProperty(Constants.PROPERTY_TYPE);
                        props.load(new FileInputStream("persistence.properties"));
                        Util.saveFile(props.getProperty("background_path"), fileName, background);
                        gView.setProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME, fileName);
                    }catch(Exception ex){
                        throw new InvalidArgumentException(String.format("Background image for view %s couldn't be saved: %s",
                                oid, ex.getMessage()), Level.SEVERE);
                    }
                }
            }else
                gView.removeProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);

            tx.success();
            tx.finish();
            
        }catch (Exception ex){
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public void deleteGeneralViews(long[] ids, String ipAddress, String sessionId) throws ObjectNotFoundException, NotAuthorizedException {
        
        validateCall("deleteGeneralViews", ipAddress, sessionId);
        
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            for (long id : ids){
                Node gView = generalViewsIndex.get(Constants.PROPERTY_ID, id).getSingle();
                generalViewsIndex.remove(gView);
                gView.delete();
            }
            tx.success();
            tx.finish();
        }catch (Exception ex){
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public ViewObject getObjectRelatedView(long oid, String objectClass, long viewId, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
        validateCall("getObjectRelatedView", ipAddress, sessionId);
        
        Node instance = getInstanceOfClass(objectClass, oid);

        for (Relationship rel : instance.getRelationships(RelTypes.HAS_VIEW, Direction.OUTGOING)){
            Node viewNode = rel.getEndNode();
            if (viewNode.getId() == viewId){
                ViewObject res = new ViewObject(viewId,
                        viewNode.hasProperty(Constants.PROPERTY_NAME) ? (String)viewNode.getProperty(Constants.PROPERTY_NAME) : null,
                        viewNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? (String)viewNode.getProperty(Constants.PROPERTY_DESCRIPTION) : null,
                        (Integer)viewNode.getProperty(Constants.PROPERTY_TYPE));
                if (viewNode.hasProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME)){
                    String fileName = (String)viewNode.getProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
                    Properties props = new Properties();
                    byte[] background = null;
                    try {
                        props.load(new FileInputStream("persistence.properties"));
                        background = Util.readBytesFromFile(props.getProperty("background_path") + "/" + fileName);
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
        throw new ObjectNotFoundException("View", viewId);
    }

    @Override
    public List<ViewObjectLight> getObjectRelatedViews(long oid, String objectClass, int limit, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
        validateCall("getObjectRelatedViews", ipAddress, sessionId);
        
        Node instance = getInstanceOfClass(objectClass, oid);
        List<ViewObjectLight> res = new ArrayList<ViewObjectLight>();
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
                    (Integer)viewNode.getProperty(Constants.PROPERTY_TYPE)));
        }
        return res;
    }

    @Override
    public List<ViewObjectLight> getGeneralViews(int viewType, int limit, String ipAddress, String sessionId) 
            throws InvalidArgumentException, NotAuthorizedException {
        
        validateCall("getGeneralViews", ipAddress, sessionId);
        
        String cypherQuery = "START gView=node:"+ Constants.INDEX_GENERAL_VIEWS +"('id:*')";
        if (viewType != -1)
            cypherQuery += " WHERE gView."+Constants.PROPERTY_TYPE+"="+viewType;

        cypherQuery += " RETURN gView";

        if (limit != -1)
            cypherQuery += " LIMIT "+limit;

        ExecutionEngine engine = new ExecutionEngine(graphDb);
        ExecutionResult result = engine.execute(cypherQuery);
        Iterator<Node> gViews = result.columnAs("gView");
        List<ViewObjectLight> myRes = new ArrayList<ViewObjectLight>();
        while (gViews.hasNext()){
            Node gView = gViews.next();
            ViewObjectLight aView = new ViewObjectLight(gView.getId(), (String)gView.getProperty(Constants.PROPERTY_NAME),
                    (String)gView.getProperty(Constants.PROPERTY_DESCRIPTION), (Integer)gView.getProperty(Constants.PROPERTY_TYPE));
            if (gView.hasProperty(Constants.PROPERTY_NAME));
                aView.setName((String)gView.getProperty(Constants.PROPERTY_NAME));
            if (gView.hasProperty(Constants.PROPERTY_DESCRIPTION));
                aView.setDescription((String)gView.getProperty(Constants.PROPERTY_DESCRIPTION));

            myRes.add(aView);
        }
        return myRes;
    }

    @Override
    public ViewObject getGeneralView(long viewId, String ipAddress, String sessionId) throws ObjectNotFoundException, NotAuthorizedException {
        
        validateCall("getGeneralView", ipAddress, sessionId);
        
        Node gView = generalViewsIndex.get(Constants.PROPERTY_ID,viewId).getSingle();

        if (gView == null)
            throw new ObjectNotFoundException("View", viewId);

        ViewObject aView = new ViewObject(gView.getId(),
                gView.hasProperty(Constants.PROPERTY_NAME) ? (String)gView.getProperty(Constants.PROPERTY_NAME) : null,
                gView.hasProperty(Constants.PROPERTY_DESCRIPTION) ? (String)gView.getProperty(Constants.PROPERTY_DESCRIPTION) : null,
                (Integer)gView.getProperty(Constants.PROPERTY_TYPE));
        if (gView.hasProperty(Constants.PROPERTY_STRUCTURE))
            aView.setStructure((byte[])gView.getProperty(Constants.PROPERTY_STRUCTURE));
        if (gView.hasProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME)){
            String fileName = (String)gView.getProperty(Constants.PROPERTY_BACKGROUND_FILE_NAME);
            Properties props = new Properties();
            byte[] background = null;
            try {
                props.load(new FileInputStream("persistence.properties"));
                background = Util.readBytesFromFile(props.getProperty("background_path") + "/" + fileName);
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
            aView.setBackground(background);
        }
        return aView;
    }

    //Queries
    @Override
    public long createQuery(String queryName, long ownerOid, byte[] queryStructure,
            String description, String ipAddress, String sessionId) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException{
        
        validateCall("createQuery", ipAddress, sessionId);
        
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
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

        }catch(Exception ex){
            Logger.getLogger("createQuery: "+ex.getMessage()); //NOI18N
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
        }
    }

    @Override
    public void saveQuery(long queryOid, String queryName, long ownerOid,
            byte[] queryStructure, String description, String ipAddress, String sessionId) 
            throws MetadataObjectNotFoundException, NotAuthorizedException{

        validateCall("saveQuery", ipAddress, sessionId);
        
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node queryNode =  queryIndex.get(CompactQuery.PROPERTY_ID, queryOid).getSingle();
            if(queryNode == null)
                throw new ApplicationObjectNotFoundException(String.format(
                        "Can not find the query with id %1s", queryOid));

            queryNode.setProperty(CompactQuery.PROPERTY_QUERYNAME, queryName);
            if(description != null){
                queryNode.setProperty(CompactQuery.PROPERTY_DESCRIPTION, description);
            }
            queryNode.setProperty(CompactQuery.PROPERTY_QUERYSTRUCTURE, queryStructure);
            
            if(ownerOid != -1) {
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, false);
                Node userNode = userIndex.get(Constants.PROPERTY_ID, ownerOid).getSingle();
                if(userNode == null)
                    throw new ApplicationObjectNotFoundException(String.format(
                                "Can not find the query with id %1s", queryOid));

                Relationship singleRelationship = queryNode.getSingleRelationship(RelTypes.OWNS_QUERY, Direction.INCOMING);

                if(singleRelationship == null)
                    userNode.createRelationshipTo(queryNode, RelTypes.OWNS_QUERY);
            }
            else
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, true);
            tx.success();

        }catch(Exception ex){
            Logger.getLogger("saveQuery: "+ex.getMessage()); //NOI18N
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
        }
    }

    @Override
    public void deleteQuery(long queryOid, String ipAddress, String sessionId)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
        validateCall("deleteQuery", ipAddress, sessionId);
        
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
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

        }catch(Exception ex){
            Logger.getLogger("deleteQuery: "+ex.getMessage()); //NOI18N
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
        }
    }

    @Override
    public List<CompactQuery> getQueries(boolean showPublic, String ipAddress, String sessionId) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, NotAuthorizedException{
        
        validateCall("getQueries", ipAddress, sessionId);
        
        List<CompactQuery> queryList = new ArrayList<CompactQuery>();
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

    @Override
    public CompactQuery getQuery(long queryOid, String ipAddress, String sessionId)
            throws ApplicationObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
        validateCall("getQuery", ipAddress, sessionId);
        
        CompactQuery cq =  new CompactQuery();

        Node queryNode = queryIndex.get(CompactQuery.PROPERTY_ID, queryOid).getSingle();

        if (queryNode == null){
             throw new ApplicationObjectNotFoundException(String.format(
                        "Can not find the query with id %1s", queryOid));
        }
                
        cq.setName((String)queryNode.getProperty(CompactQuery.PROPERTY_QUERYNAME));
        if(queryNode.hasProperty(CompactQuery.PROPERTY_DESCRIPTION)){
            cq.setDescription((String)queryNode.getProperty(CompactQuery.PROPERTY_DESCRIPTION));
        }
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

    @Override
    public List<ResultRecord> executeQuery(ExtendedQuery query, String ipAddress, String sessionId) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
        validateCall("executeQuery", ipAddress, sessionId);
        
        CypherQueryBuilder cqb = new CypherQueryBuilder();
        cqb.setClassNodes(getNodesFromQuery(query));
        cqb.createQuery(query);

        return cqb.getResultList();
    }
    
    @Override
    public byte[] getClassHierachy(boolean showAll, String ipAddress, String sessionId) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException{
        
        validateCall("getClassHierachy", ipAddress, sessionId);
        
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        WAX xmlWriter = new WAX(bas);
        StartTagWAX rootTag = xmlWriter.start("hierarchy");
        rootTag.attr("documentVersion", Constants.CLASS_HIERARCHY_DOCUMENT_VERSION);
        rootTag.attr("serverVersion", Constants.PERSISTENCE_SERVICE_VERSION);
        rootTag.attr("date", Calendar.getInstance().getTimeInMillis());
        StartTagWAX inventoryTag = rootTag.start("inventory");
        StartTagWAX classesTag = inventoryTag.start("classes");
        Node rootObjectNode = classIndex.get(Constants.PROPERTY_NAME, Constants.CLASS_ROOTOBJECT).getSingle(); //NOI18N
        if (rootObjectNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %1s can not be found", Constants.CLASS_ROOTOBJECT));
        getXMLNodeForClass(rootObjectNode, rootTag);
        classesTag.end();
        inventoryTag.end();
        rootTag.end().close();
        return bas.toByteArray();
    }
    
    //Pools
    /**
     * Creates a pool
     * @param parentId Parent id. -1 for none
     * @param name Pool name
     * @param description Pool description
     * @param instancesOfClass What kind of elements can be contained in this pool
     * @return the id of the new pool
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException 
     */
    public long createPool(long parentId, String name, String description, String instancesOfClass, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        validateCall("createPool", ipAddress, sessionId);
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            
            Node parentNode = null;
            if (parentId != -1){
                parentNode = objectIndex.get(Constants.PROPERTY_ID, parentId).getSingle();
                if (parentNode == null)
                    throw new ObjectNotFoundException("N/A", parentId);
            }
            Node poolNode =  graphDb.createNode();
            if (name != null)
                poolNode.setProperty(Constants.PROPERTY_NAME, name);
            if (description != null)
                poolNode.setProperty(Constants.PROPERTY_DESCRIPTION, description);
            
            ClassMetadata classMetadata = cm.getClass(instancesOfClass);
            if (classMetadata == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", instancesOfClass));
            
            poolNode.setProperty(Constants.PROPERTY_CLASS_NAME, instancesOfClass);
            
            if (parentNode != null)
                poolNode.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);
                        
            poolsIndex.putIfAbsent(poolNode, Constants.PROPERTY_ID, poolNode.getId());
            tx.success();
            tx.finish();
            return poolNode.getId();
        }catch(Exception ex){
            Logger.getLogger("createPool: "+ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }
    /**
     * Creates an object inside a pool
     * @param poolId Parent pool id. 
     * @param attributeNames Attributes to be set
     * @param attributeValues Attribute values to be set
     * @param templateId Template used to create the object, if applicable. -1 for none
     * @throws ApplicationObjectNotFoundException If the parent pool can't be found
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @return the id of the newly created object
     */
    @Override
    public long createPoolItem(long poolId, String className, String[] attributeNames, 
    String[][] attributeValues, long templateId, String ipAddress, String sessionId) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, ArraySizeMismatchException, NotAuthorizedException {
        
        validateCall("createPoolItem", ipAddress, sessionId);
        
        if (attributeNames != null && attributeValues != null){
            if (attributeNames.length != attributeValues.length)
            throw new ArraySizeMismatchException("attributeNames", "attributeValues");
        }
        
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node pool = poolsIndex.get(Constants.PROPERTY_ID, poolId).getSingle();
            
            if (pool == null)
                throw new ApplicationObjectNotFoundException(String.format("Pool with id %s can not be found", poolId));
            
            if (!pool.hasProperty(Constants.PROPERTY_CLASS_NAME))
                throw new InvalidArgumentException("This pool has not set his class name attribute", Level.INFO);
            
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));
            
            ClassMetadata classMetadata = cm.getClass(className);
            
            if (!cm.isSubClass((String)pool.getProperty(Constants.PROPERTY_CLASS_NAME), className))
                throw new InvalidArgumentException(String.format("Class %s is not subclass of %s", className, (String)pool.getProperty(Constants.PROPERTY_CLASS_NAME)), Level.OFF);
            
            HashMap<String, List<String>> attributes = new HashMap<String, List<String>>();
            if (attributeNames != null && attributeValues != null){
                for (int i = 0; i < attributeNames.length; i++)
                    attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));
            }
            
            Node newObject = bem.createObject(classNode, classMetadata, attributes, templateId);
            newObject.createRelationshipTo(pool, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);
            tx.success();
            return newObject.getId();

        }catch(Exception ex){
            Logger.getLogger("createPoolItem: " + ex.getMessage()); //NOI18N
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
        }
    }

    @Override
    public void deletePools(long[] ids, String ipAddress, String sessionId) throws NotAuthorizedException, InvalidArgumentException {
        
        validateCall("deletePools", ipAddress, sessionId);
        
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            for (long id : ids){
                Node poolNode = poolsIndex.get(Constants.PROPERTY_ID, id).getSingle();
                if (poolNode == null)
                    throw new InvalidArgumentException(String.format("A pool with id %s does not exist", id),Level.INFO);

                //Let's delete the objects inside, if possible
                HashMap<String, long[]> toBeDeleted = new HashMap<String, long[]>();
                List<Long> poolsToBeDeleted = new ArrayList<Long>();
                for (Path p : Traversal.description()
                    .breadthFirst()
                    .evaluator(Evaluators.atDepth(1))    
                    .relationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING)
                    .evaluator(Evaluators.excludeStartPosition())
                    .traverse(poolNode)) {

                    Node objectNode = p.endNode();
                     
                    if(objectNode.hasProperty(Constants.PROPERTY_CLASS_NAME) && objectNode.getProperty(Constants.PROPERTY_CLASS_NAME).equals(Constants.CLASS_GENERICSERVICE))
                        poolsToBeDeleted.add(objectNode.getId());
                    else{
                        String className = Util.getObjectClassName(objectNode);
                        if (toBeDeleted.get(className) == null) 
                            toBeDeleted.put(className, new long[]{objectNode.getId()});
                        else{
                            long[] objectIds = toBeDeleted.get(className);
                            long[] newObjectIds = new long[objectIds.length+1];
                            System.arraycopy(objectIds, 0, newObjectIds, 0, objectIds.length);
                            newObjectIds[objectIds.length]=objectNode.getId();
                            toBeDeleted.put(className, newObjectIds);
                        }
                    }
                }
                bem.deleteObjects(toBeDeleted, false, ipAddress, sessionId);

                for (long pId : poolsToBeDeleted){
                    Node servicePoolNode = poolsIndex.get(Constants.PROPERTY_ID, pId).getSingle();
                    for (Relationship rel : servicePoolNode.getRelationships())
                        rel.delete();
                    poolsIndex.remove(servicePoolNode);
                }
                
                for (Relationship rel : poolNode.getRelationships())
                    rel.delete();
                poolsIndex.remove(poolNode);
                poolNode.delete();
            }
            tx.success();

        }catch(Exception ex){
            Logger.getLogger("deletePools: "+ex.getMessage()); //NOI18N
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
        }
    }

    @Override
    public List<RemoteBusinessObjectLight> getPools(int limit, long parentId, String className, String ipAddress, String sessionId) throws NotAuthorizedException, ObjectNotFoundException{
        validateCall("getPools", ipAddress, sessionId);
        
        Node parentNode = null;
        if (parentId != -1){
            parentNode = objectIndex.get(Constants.PROPERTY_ID, parentId).getSingle();
            if (parentNode == null)
                throw new ObjectNotFoundException("N/A", parentId);
        }
        List<RemoteBusinessObjectLight> pools  = new ArrayList<RemoteBusinessObjectLight>();
        int i = 0;
        for(Relationship rel: parentNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)){
            if (limit != -1){
                if (i >= limit)
                     break;
                i++;
            }
            Node node = poolsIndex.get(Constants.PROPERTY_ID, rel.getStartNode().getId()).getSingle();
            //or it could be use this if (rel.getProperty(Constants.PROPERTY_NAME).equals("Pools"))
            if(node != null){
                RemoteBusinessObjectLight rbol = new RemoteBusinessObjectLight(node.getId(),
                node.hasProperty(Constants.PROPERTY_NAME) ? (String)node.getProperty(Constants.PROPERTY_NAME) : null,
                node.hasProperty(Constants.PROPERTY_CLASS_NAME) ? (String)node.getProperty(Constants.PROPERTY_CLASS_NAME) : null);
                if(className != null && node.hasProperty(Constants.PROPERTY_CLASS_NAME)){
                    if(className.equals(node.getProperty(Constants.PROPERTY_CLASS_NAME)))
                        pools.add(rbol);
                }
                else
                    pools.add(rbol);
            }
        }
        return pools;
    }
        
    @Override
    public List<RemoteBusinessObjectLight> getPools(int limit, String className, String ipAddress, String sessionId) throws NotAuthorizedException{
        
        validateCall("getPools", ipAddress, sessionId);
        
        IndexHits<Node> poolNodes = poolsIndex.query(Constants.PROPERTY_ID, "*");

        List<RemoteBusinessObjectLight> pools  = new ArrayList<RemoteBusinessObjectLight>();
        int i = 0;
        for (Node node : poolNodes){
            if (limit != -1){
                if (i >= limit)
                     break;
                i++;
            }
            RemoteBusinessObjectLight rbol = new RemoteBusinessObjectLight(node.getId(),
                    node.hasProperty(Constants.PROPERTY_NAME) ? (String)node.getProperty(Constants.PROPERTY_NAME) : null,
                    node.hasProperty(Constants.PROPERTY_CLASS_NAME) ? (String)node.getProperty(Constants.PROPERTY_CLASS_NAME) : null);
            if(className != null){
                if(className.equals(node.getProperty(Constants.PROPERTY_CLASS_NAME)))
                    pools.add(rbol);
            }
            else{
                if(!Constants.CLASS_GENERICCUSTOMER.equals(node.getProperty(Constants.PROPERTY_CLASS_NAME)) &&
                   !Constants.CLASS_GENERICSERVICE.equals(node.getProperty(Constants.PROPERTY_CLASS_NAME)))
                    pools.add(rbol);
            }
        }
            
        return pools;
    }
    
    @Override
    public List<RemoteBusinessObjectLight> getPoolItems(long poolId, int limit, String ipAddress, String sessionId)
            throws ApplicationObjectNotFoundException, NotAuthorizedException{
        
        validateCall("getPoolItems", ipAddress, sessionId);
        
        Node poolNode = poolsIndex.get(Constants.PROPERTY_ID, poolId).getSingle();

        if (poolNode == null)
            throw new ApplicationObjectNotFoundException(String.format("The pool with id %s could not be found", poolId));
        
        List<RemoteBusinessObjectLight> poolItems  = new ArrayList<RemoteBusinessObjectLight>();
        
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
                    RemoteBusinessObjectLight rbol = new RemoteBusinessObjectLight(item.getId(), 
                                            item.hasProperty(Constants.PROPERTY_NAME) ? (String)item.getProperty(Constants.PROPERTY_NAME) : null,
                                            Util.getClassName(item));
                    poolItems.add(rbol);
                }
            }
        }
                   
        return poolItems;
    }
    
    @Override
    public List<ActivityLogEntry> getBusinessObjectAuditTrail(String objectClass, long objectId, int limit, String ipAddress, String sessionId) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, NotAuthorizedException {
        
        validateCall("getBusinessObjectAuditTrail", ipAddress, sessionId);
        
        if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, objectClass))
            throw new InvalidArgumentException(String.format("Class %s is not subclass of %s",
                    objectClass, Constants.CLASS_INVENTORYOBJECT), Level.INFO);
        Node instanceNode = getInstanceOfClass(objectClass, objectId);
        List<ActivityLogEntry> log = new ArrayList<ActivityLogEntry>();
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
    
    /**
     * Retrieves the list of activity log entries
     * @param page current page
     * @param limit limit of results per page. 0 to retrieve them all
     * @return The list of activity log entries
     */
    @Override
    public List<ActivityLogEntry> getGeneralActivityAuditTrail(int page, int limit, String ipAddress, String sessionId) throws NotAuthorizedException{
        Node generalActivityLogNode = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).
                get(Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG).getSingle();

        validateCall("getGeneralActivityAuditTrail", ipAddress, sessionId);
        
        List<ActivityLogEntry> log = new ArrayList<ActivityLogEntry>();
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
//            Node userNode = userIndex.get(Constants.PROPERTY_ID, aSession.getUser().getId()).getSingle();
//            if(userNode == null)
//                throw new ApplicationObjectNotFoundException(String.format("Can not find a user with id %s",aSession.getUser().getId()));
//            
//            Iterable<Relationship> groupUsersRels = userNode.getRelationships(RelTypes.BELONGS_TO_GROUP, Direction.OUTGOING);
//            for (Relationship groupUsersRel : groupUsersRels) {
//                Node groupNode = groupUsersRel.getEndNode();
//                for(Relationship rel : groupNode.getRelationships(RelTypes.HAS_PRIVILEGE, Direction.INCOMING))
//                    if(methodName.equals((String)rel.getStartNode().getProperty(Constants.PROPERTY_NAME)))
//                        return;
//            }
//            for(Relationship userPrivilegesRel: userNode.getRelationships(RelTypes.HAS_PRIVILEGE, Direction.INCOMING)){
//                if(methodName.equals((String)(userPrivilegesRel.getStartNode().getProperty(Constants.PROPERTY_NAME))))
//                    return;
//            }
//            throw new NotAuthorizedException(methodName,sessionId);
//        }catch(Exception ex){
//            throw new RuntimeException(ex.getMessage());
//        }
    }

    @Override
    public Session createSession(String userName, String password, String IPAddress) throws ApplicationObjectNotFoundException {
        
        if (userName == null || password == null)
            throw  new ApplicationObjectNotFoundException("User or Password can not be null");
        
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
                Logger.getLogger("createSession").log(Level.INFO, String.format("An existing session for user %1s has been dropped", aSession.getUser().getUserName()));
                sessions.remove(aSession.getToken());
                break;
            }
        }
        Session newSession = new Session(Util.createUserProfileFromNode(userNode), IPAddress);
        sessions.put(newSession.getToken(), newSession);
        return newSession;
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
            
    public void setBusinessEntityManager(BusinessEntityManagerImpl bem) {
        this.bem = bem;
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
            applicationModifiers |= Modifier.ABSTRACT;
        
        currentTag.attr("javaModifiers",javaModifiers);
        
        //Class type
        if (classNode.getProperty(Constants.PROPERTY_NAME).equals("RootObject")){
            currentTag.attr("classType",Constants.CLASS_TYPE_ROOT);
        }else{
            if (Util.isSubClass("InventoryObject", classNode))
                currentTag.attr("classType",Constants.CLASS_TYPE_INVENTORY);
            else{
                if (Util.isSubClass("MetadataObject", classNode))
                    currentTag.attr("classType",Constants.CLASS_TYPE_METADATA);
                else{
                    if (Util.isSubClass("ApplicationObject", classNode))
                        currentTag.attr("classType",Constants.CLASS_TYPE_APPLICATION);
                    else
                        currentTag.attr("classType",Constants.CLASS_TYPE_OTHER);
                }
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

        Map<String, Node> classNodes = new HashMap<String, Node>();
        List<String> ListClassNames = new ArrayList();
        readJoins(ListClassNames, query);
        for(String className : ListClassNames)
            classNodes.put(className, classIndex.get(Constants.PROPERTY_NAME, className).getSingle());
        
        return classNodes;
    }
    
    private String readJoins(List<String> l, ExtendedQuery query){
        
        String className;

        if(query == null)
            return null;
        else
            className = query.getClassName();

        if(query.getJoins() != null){
            for(ExtendedQuery join : query.getJoins()){
                    readJoins(l,join);
            }
        }
        if(className != null || className.equals(""))
            l.add(className);
        return className;
    }
    
    private Node getInstanceOfClass(String className, long oid) throws MetadataObjectNotFoundException, ObjectNotFoundException, NotAuthorizedException{
        //if any of the parameters is null, return the dummy root
        if (className == null){
            return graphDb.getReferenceNode().getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.BOTH).getEndNode();
        }
        Node classNode = classIndex.get(Constants.PROPERTY_NAME,className).getSingle();
        if (classNode == null){
            throw new MetadataObjectNotFoundException(String.format("Class %1s can not be found", className));
        }
        Iterable<Relationship> instances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        while (instances.iterator().hasNext()){
            Node otherSide = instances.iterator().next().getStartNode();
            if (otherSide.getId() == oid){
                return otherSide;
            }
        }
        throw new ObjectNotFoundException(className, oid);
    }
    //end Helpers
}

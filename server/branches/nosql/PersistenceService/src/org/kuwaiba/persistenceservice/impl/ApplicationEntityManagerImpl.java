/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.application.CompactQuery;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.ResultRecord;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.application.View;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.UserGroupNotFoundException;
import org.kuwaiba.apis.persistence.interfaces.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.persistenceservice.caching.CacheManager;
import org.kuwaiba.persistenceservice.util.Constants;
import org.kuwaiba.persistenceservice.util.Util;
import org.kuwaiba.psremoteinterfaces.ApplicationEntityManagerRemote;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.EmbeddedGraphDatabase;

/**
 * Application Entity Manager reference implementation
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ApplicationEntityManagerImpl implements ApplicationEntityManager, ApplicationEntityManagerRemote{

    /**
     * Index name for user nodes
     */
    public static final String INDEX_USER = "userIndex";
    /**
     * Index name for group nodes
     */
    public static final String INDEX_GROUP = "groupIndex";
    /**
     * Index name for group nodes
     */
    public static final String INDEX_QUERY = "queryIndex";
    /**
     * Name of the index for list type items
     */
    public static final String INDEX_LIST_TYPE_ITEMS = "listTypeItems"; //NOI18N
    /**
     * Property "background path" for views
     */
    public static final String PROPERTY_BACKGROUND_PATH = "backgroundPath";
    /**
     * Property "structure" for views
     */
    public static final String PROPERTY_STRUCTURE = "structure";
    /**
     * Date format for queries
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    /**
     * Graph db service
     */
    private GraphDatabaseService graphDb;
    /**
     * Class index
     */
    private Index<Node> classIndex;
    /**
     * Users index
     */
    private Index<Node> userIndex;
    /**
     * Groups index
     */
    private Index<Node> groupIndex;
    /**
     * query index; 
     */
    private Index<Node> queryIndex;
    /**
     * Index for list type items (of all classes)
     */
    private Index<Node> listTypeItemsIndex;
    /**
     * Reference to the singleton instance of CacheManager
     */
    private CacheManager cm;

    public ApplicationEntityManagerImpl(ConnectionManager cmn) {
        this.graphDb = (EmbeddedGraphDatabase)cmn.getConnectionHandler();
        this.userIndex = graphDb.index().forNodes(INDEX_USER);
        this.groupIndex = graphDb.index().forNodes(INDEX_GROUP);
        this.queryIndex = graphDb.index().forNodes(INDEX_QUERY);
        this.classIndex = graphDb.index().forNodes(MetadataEntityManagerImpl.INDEX_CLASS);
        this.listTypeItemsIndex = graphDb.index().forNodes(INDEX_LIST_TYPE_ITEMS);
        this.cm = CacheManager.getInstance();
    }

    public UserProfile login(String username, String password) {

        if (username == null || password == null)
            return null;

        Node user = userIndex.get(UserProfile.PROPERTY_USERNAME,username).getSingle();
        if (user == null)
            return null;

        if (!(Boolean)user.getProperty(UserProfile.PROPERTY_ENABLED))
            return null;

        if (Util.getMD5Hash(password).equals(user.getProperty(UserProfile.PROPERTY_PASSWORD)))
            return new UserProfile(new Long(user.getId()),
                    (String)user.getProperty(UserProfile.PROPERTY_USERNAME),
                    (String)user.getProperty(UserProfile.PROPERTY_FIRST_NAME),
                    (String)user.getProperty(UserProfile.PROPERTY_LAST_NAME),
                    (Boolean)user.getProperty(UserProfile.PROPERTY_ENABLED),
                    (Long)user.getProperty(UserProfile.PROPERTY_CREATION_DATE),
                    //(List<Integer>)user.getProperty(UserProfile.PROPERTY_PRIVILEGES)
                    new ArrayList<Integer>());
        else
            return null;
    }

    public Long createUser(String userName, String password, String firstName, 
            String lastName, Boolean enabled, List<Integer> privileges, List<Long> groups) 
            throws InvalidArgumentException
    {
        if (userName == null)
            throw new InvalidArgumentException("User name can't be null", Level.INFO);

        if (userName.trim().equals("")) //NOI18N
            throw new InvalidArgumentException("User name can't be an empty string", Level.INFO);

        if (password == null)
            throw new InvalidArgumentException("Password can't be null", Level.INFO);

        if (password.trim().equals("")) //NOI18N
            throw new InvalidArgumentException("Password can't be an empty string", Level.INFO);

        if (cm.getUser(userName) == null)
        {
            Node storedUser = userIndex.get(UserProfile.PROPERTY_USERNAME,userName).getSingle();
            if (storedUser != null)
                throw new InvalidArgumentException(Util.formatString("The username %1s is already in use", userName), Level.WARNING);
        }

        Transaction tx = graphDb.beginTx();
        
        Node newUser = graphDb.createNode();

        newUser.setProperty(UserProfile.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
        newUser.setProperty(UserProfile.PROPERTY_USERNAME, userName);
        newUser.setProperty(UserProfile.PROPERTY_PASSWORD, Util.getMD5Hash(password));
        
        if(firstName == null)
            firstName = "";

        newUser.setProperty(UserProfile.PROPERTY_FIRST_NAME, firstName);

        if(lastName == null)
            lastName = "";

        newUser.setProperty(UserProfile.PROPERTY_LAST_NAME, lastName);
        
        if(enabled == null)
            enabled = true;

        newUser.setProperty(UserProfile.PROPERTY_ENABLED, enabled);

//        TODO privileges
//        if (privileges != null || privileges.size()<1)
//            newUser.setProperty(UserProfile.PROPERTY_PRIVILEGES, privileges);

        if (groups != null){
            if(groups.size()>0)
            {
                for (Long groupId : groups){
                    Node group = groupIndex.get(UserProfile.PROPERTY_ID,groupId).getSingle();
                    if (group != null)
                        newUser.createRelationshipTo(group, RelTypes.BELONGS_TO_GROUP);
                    else{
                        tx.failure();
                        tx.finish();
                        throw new InvalidArgumentException(Util.formatString("Group with id %1s can't be found",groupId), Level.OFF);
                    }
                }
            }
        }
        
        userIndex.putIfAbsent(newUser, UserProfile.PROPERTY_ID, newUser.getId());
        userIndex.putIfAbsent(newUser, UserProfile.PROPERTY_USERNAME, userName);
        cm.putUser(new UserProfile(newUser.getId(), userName,
                firstName, lastName, true, (Long)newUser.getProperty(UserProfile.PROPERTY_CREATION_DATE), privileges));
        tx.success();
        tx.finish();
        return new Long(newUser.getId());
    }

    public void setUserProperties(Long oid, String userName, String password, String firstName,
            String lastName, Boolean enabled, List<Integer> privileges, List<Long> groups)
            throws InvalidArgumentException, UserGroupNotFoundException {
        Transaction tx = null;
        try{
            tx =  graphDb.beginTx();

            Node userNode = userIndex.get(UserProfile.PROPERTY_ID, oid).getSingle();
            if(userNode == null)
                throw new UserGroupNotFoundException(Util.formatString("Can not find the user with id %1s",oid));

            if(userName != null){
                if (userName.trim().equals("")) //NOI18N
                    throw new InvalidArgumentException("Username can not be an empty string", Level.INFO);

                Node storedUser = userIndex.get(UserProfile.PROPERTY_USERNAME,userName).getSingle();
                if (storedUser != null)
                    throw new InvalidArgumentException(Util.formatString("The username %1s is already in use", userName), Level.WARNING);
                
                //refresh the userindex
                userIndex.remove(userNode, UserProfile.PROPERTY_USERNAME, (String)userNode.getProperty(UserProfile.PROPERTY_USERNAME));
                userNode.setProperty(UserProfile.PROPERTY_USERNAME, userName);
                userIndex.putIfAbsent(userNode, UserProfile.PROPERTY_USERNAME, userName);
            }

            if(password != null){
                if (password.trim().equals("")) //NOI18N
                    throw new InvalidArgumentException("Password can't be an empty string", Level.INFO);

                userNode.setProperty(UserProfile.PROPERTY_PASSWORD, Util.getMD5Hash(password));
            }

            if(firstName != null)
                userNode.setProperty(UserProfile.PROPERTY_FIRST_NAME, firstName);

            if(lastName != null)
                userNode.setProperty(UserProfile.PROPERTY_LAST_NAME, lastName);

            if(groups != null){
                Iterable<Relationship> relationships = userNode.getRelationships(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP);
                for (Relationship relationship : relationships) {
                    relationship.delete();
                }

                for (Long id : groups) {
                    Node groupNode = groupIndex.get(GroupProfile.PROPERTY_ID, id).getSingle();
                        userNode.createRelationshipTo(groupNode, RelTypes.BELONGS_TO_GROUP);
                }
            }
            tx.success();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
        }
    }

    public void setUserProperties(String oldUserName, String newUserName, String password,
            String firstName, String lastName, Boolean enabled, List<Integer> privileges, List<Long> groups)
            throws InvalidArgumentException, UserGroupNotFoundException {
        Transaction tx = null;
        try{
            tx =  graphDb.beginTx();

            Node userNode = userIndex.get(UserProfile.PROPERTY_USERNAME, oldUserName).getSingle();
            if(userNode == null)
                throw new UserGroupNotFoundException(Util.formatString("Can not find the user with username %1s",oldUserName));

            if(newUserName != null){
                if (newUserName.trim().equals("")) //NOI18N
                    throw new InvalidArgumentException("Username can not be an empty string", Level.INFO);

                Node storedUser = userIndex.get(UserProfile.PROPERTY_USERNAME,oldUserName).getSingle();
                if (storedUser != null && !newUserName.equals("admin"))
                    throw new InvalidArgumentException(Util.formatString("The username %1s is already in use", oldUserName), Level.WARNING);

                //refresh the userindex
                userIndex.remove(userNode, UserProfile.PROPERTY_USERNAME, (String)userNode.getProperty(UserProfile.PROPERTY_USERNAME));
                cm.removeUser((String)userNode.getProperty(UserProfile.PROPERTY_USERNAME));

                userNode.setProperty(UserProfile.PROPERTY_USERNAME, newUserName);
                
                userIndex.putIfAbsent(userNode, UserProfile.PROPERTY_USERNAME, newUserName);
                cm.putUser(new UserProfile(userNode.getId(), newUserName,
                    firstName, lastName, true, (Long)userNode.getProperty(UserProfile.PROPERTY_CREATION_DATE), privileges));
            }

            if(password != null){
                if (password.trim().equals("")) //NOI18N
                    throw new InvalidArgumentException("Password can't be an empty string", Level.INFO);

                userNode.setProperty(UserProfile.PROPERTY_PASSWORD, Util.getMD5Hash(password));
            }

            if(firstName != null)
                userNode.setProperty(UserProfile.PROPERTY_FIRST_NAME, firstName);

            if(lastName != null)
                userNode.setProperty(UserProfile.PROPERTY_LAST_NAME, lastName);

            if(groups != null){
                Iterable<Relationship> relationships = userNode.getRelationships(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP);
                for (Relationship relationship : relationships) {
                    relationship.delete();
                }

                for (Long id : groups) {
                    Node groupNode = groupIndex.get(GroupProfile.PROPERTY_ID, id).getSingle();
                        userNode.createRelationshipTo(groupNode, RelTypes.BELONGS_TO_GROUP);
                }
            }
            tx.success();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
        }
    }

    public Long createGroup(String groupName, String description, 
            List<Integer> privileges, List<Long> users) throws InvalidArgumentException
    {
        if (groupName == null)
            throw new InvalidArgumentException("Group name can not be null", Level.INFO);

        if (groupName.trim().equals("")) //NOI18N
            throw new InvalidArgumentException("User name can't be an empty string", Level.INFO);

        if (cm.getGroup(groupName) == null)
        {
            Node storedGroup = groupIndex.get(GroupProfile.PROPERTY_GROUPNAME,groupName).getSingle();
            if (storedGroup != null){
                //When the admins acounts need to be restored

                if(((String)storedGroup.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME)).equals("admins") ||
                   ((String)storedGroup.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME)).equals("users"))
                    return storedGroup.getId();
                else
                    throw new InvalidArgumentException(Util.formatString("The group name %1s is already in use", groupName), Level.WARNING);
            }
        }
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node newGroup = graphDb.createNode();

            newGroup.setProperty(GroupProfile.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            newGroup.setProperty(GroupProfile.PROPERTY_GROUPNAME, groupName);
            
            if(description == null)
                description = "";

            newGroup.setProperty(GroupProfile.PROPERTY_DESCRIPTION, description);

            groupIndex.putIfAbsent(newGroup, GroupProfile.PROPERTY_ID, newGroup.getId());
            groupIndex.putIfAbsent(newGroup, GroupProfile.PROPERTY_GROUPNAME, groupName);

            cm.putGroup(new GroupProfile(newGroup.getId(), groupName,
                description, (Long)newGroup.getProperty(UserProfile.PROPERTY_CREATION_DATE)));

            tx.success();

            return new Long(newGroup.getId());
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
        }
    }

    public List<UserProfile> getUsers() throws UserGroupNotFoundException
    {
        IndexHits<Node> usersNodes = userIndex.query(UserProfile.PROPERTY_USERNAME, "*");
        if(usersNodes == null)
                throw new UserGroupNotFoundException(Util.formatString("Can not find users"));

        List<UserProfile> users = new ArrayList<UserProfile>();
        
        for (Node node : usersNodes)
        {
            users.add(Util.createUserProfileFromNode(node));
            
        }
        return users;
    }

    public List<GroupProfile> getGroups() throws UserGroupNotFoundException
    {
        IndexHits<Node> groupsNodes = groupIndex.query(GroupProfile.PROPERTY_GROUPNAME, "*");
        if(groupsNodes == null)
                throw new UserGroupNotFoundException(Util.formatString("Can not find the groups"));

        List<GroupProfile> groups =  new ArrayList<GroupProfile>();
        for (Node node : groupsNodes)
        {
            groups.add((Util.createGroupProfileFromNode(node)));
        }
        return groups;
    }

    public void setGroupProperties(Long id, String groupName, String description,
            List<Integer> privileges, List<Long> users)
            throws InvalidArgumentException, UserGroupNotFoundException
    {
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();

            Node groupNode = groupIndex.get(GroupProfile.PROPERTY_ID, id).getSingle();
            if(groupNode == null)
                throw new UserGroupNotFoundException(Util.formatString("Can not find the group with id %1s",id));

            if(groupName != null){
                if (groupName.trim().equals("")) //NOI18N
                    throw new InvalidArgumentException("User name can not be an empty string", Level.INFO);

                if (cm.getUser(groupName) == null)
                {
                    Node storedGroup = groupIndex.get(GroupProfile.PROPERTY_GROUPNAME, groupName).getSingle();
                    if (storedGroup != null)
                        throw new InvalidArgumentException(Util.formatString("The group name %1s is already in use", groupName), Level.WARNING);
                }
                groupIndex.remove(groupNode, GroupProfile.PROPERTY_GROUPNAME, (String)groupNode.getProperty(GroupProfile.PROPERTY_GROUPNAME));
                cm.removeGroup((String)groupNode.getProperty(GroupProfile.PROPERTY_GROUPNAME));

                groupNode.setProperty(GroupProfile.PROPERTY_GROUPNAME, groupName);
                groupIndex.putIfAbsent(groupNode, GroupProfile.PROPERTY_GROUPNAME, groupName);
                cm.putGroup(new GroupProfile(groupNode.getId(), groupName,
                description, (Long)groupNode.getProperty(UserProfile.PROPERTY_CREATION_DATE)));
            }
            
            if(description != null)
                groupNode.setProperty(GroupProfile.PROPERTY_DESCRIPTION, description);

            if(users != null){
                Iterable<Relationship> relationships = groupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_GROUP);
                for (Relationship relationship : relationships) {
                    relationship.delete();
                }
                for (Long userId : users) {
                    Node userNode = userIndex.get(UserProfile.PROPERTY_ID, userId).getSingle();
                    userNode.createRelationshipTo(groupNode, RelTypes.BELONGS_TO_GROUP);
                }
            }


            tx.success();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
        }
    }

    public void deleteUsers(List<Long> oids) throws UserGroupNotFoundException {
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            //TODO watch if there is relationships you can not delete
            if(oids != null){
                for (Long id : oids)
                {
                    Node userNode = userIndex.get(UserProfile.PROPERTY_ID, id).getSingle();
                    if(userNode == null)
                        throw new UserGroupNotFoundException(Util.formatString("Can not find the user with id %1s",id));
                    cm.removeUser((String)userNode.getProperty(UserProfile.PROPERTY_USERNAME));

                    Iterable<Relationship> relationships = userNode.getRelationships();
                    for (Relationship relationship : relationships) {
                        relationship.delete();
                    }
                    userIndex.remove(userNode);
                    userNode.delete();
                }
            }


            tx.success();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
        }
    }

    public void deleteGroups(List<Long> oids) throws UserGroupNotFoundException {
        Transaction tx = null;
        try{
            if(oids != null){
                tx = graphDb.beginTx();
                for (Long id : oids) {
                    Node groupNode = groupIndex.get(GroupProfile.PROPERTY_ID, id).getSingle();

                    if(groupNode == null)
                        throw new UserGroupNotFoundException(Util.formatString("Can not find the group with id %1s",id));

                    cm.removeGroup((String)groupNode.getProperty(GroupProfile.PROPERTY_GROUPNAME));

                    Iterable<Relationship> relationships = groupNode.getRelationships();
                    for (Relationship relationship : relationships) {
                        relationship.delete();
                    }
                    groupIndex.remove(groupNode);
                    groupNode.delete();
                }
                tx.success();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
        }
    }

    //List type related methods
   public Long createListTypeItem(String className, String name, String displayName)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
       if (name == null || className == null)
           throw new InvalidArgumentException("Item name and class name can not be null", Level.INFO);
       
        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME, className).getSingle();
        if (classNode ==  null)
            throw new MetadataObjectNotFoundException(Util.formatString("Can not find a class with name %1s",className));
        if (!cm.isSubClass("GenericObjectList", className))
            throw new InvalidArgumentException(Util.formatString("Class %1s is not a list type", className), Level.SEVERE);

        Transaction tx = null;
        try{
             tx = graphDb.beginTx();
             Node newItem = graphDb.createNode();
             newItem.setProperty(MetadataEntityManagerImpl.PROPERTY_NAME, name);
             if (displayName != null)
                newItem.setProperty(MetadataEntityManagerImpl.PROPERTY_DISPLAY_NAME, displayName);
             newItem.createRelationshipTo(classNode, RelTypes.INSTANCE_OF);
             listTypeItemsIndex.putIfAbsent(newItem, MetadataEntityManagerImpl.PROPERTY_ID, newItem.getId());
             tx.success();
             return newItem.getId();
        }catch(Exception ex){
            Logger.getLogger("createListTypeItem: "+ex.getMessage()); //NOI18N
            if (tx != null)
                tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
        }
    }

    public void deleteListTypeItem(String className, Long oid, boolean realeaseRelationships) throws MetadataObjectNotFoundException, OperationNotPermittedException, ObjectNotFoundException{
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            if (!cm.isSubClass("GenericObjectList", className))
                throw new InvalidArgumentException(Util.formatString("Class %1s is not a list type", className), Level.SEVERE);

            Node instance = getInstanceOfClass(className, oid);
            Util.deleteObject(instance, realeaseRelationships);

            tx.success();
        }catch(Exception ex){
            Logger.getLogger("deleteListTypeItem: "+ex.getMessage()); //NOI18N
            if (tx != null)
                tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
        }
    }

    public List<RemoteBusinessObjectLight> getListTypeItems(String className) throws MetadataObjectNotFoundException, InvalidArgumentException{
        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME, className).getSingle();
        if (classNode ==  null)
            throw new MetadataObjectNotFoundException(Util.formatString("Can not find a class with name %1s",className));

        if (!Util.isSubClass("GenericObjectList", classNode))
            throw new InvalidArgumentException(Util.formatString("Class %1s is not a list type", className), Level.SEVERE);

        Iterable<Relationship> childrenAsRelationships = classNode.getRelationships(RelTypes.INSTANCE_OF);
        List<RemoteBusinessObjectLight> children = new ArrayList<RemoteBusinessObjectLight>();

        while(childrenAsRelationships.iterator().hasNext()){
            Node child = childrenAsRelationships.iterator().next().getStartNode();
            children.add(new RemoteBusinessObjectLight(child.getId(), (String)child.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME), className));
        }
        return children;
    }

    public List<ClassMetadataLight> getInstanceableListTypes() throws MetadataObjectNotFoundException {
        Node genericObjectListNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME, "GenericObjectList").getSingle();

        if (genericObjectListNode == null)
            throw new MetadataObjectNotFoundException(Util.formatString("Class %1s is not a list type", "GenericObjectList"));

        String cypherQuery = "START classmetadata = node:classes(name = {className}) ".concat(
                             "MATCH classmetadata <-[:").concat(RelTypes.EXTENDS.toString()).concat("*]-listType ").concat(
                             "RETURN listType ").concat(
                             "ORDER BY listType.name ASC");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("className", MetadataEntityManagerImpl.LIST_TYPE);//NOI18N

        ExecutionEngine engine = new ExecutionEngine(graphDb);
        ExecutionResult result = engine.execute(cypherQuery, params);
        Iterator<Node> n_column = result.columnAs("listType");
        List<ClassMetadataLight> res = new ArrayList<ClassMetadataLight>();
        for (Node node : IteratorUtil.asIterable(n_column))
        {
            if (!(Boolean)node.getProperty(MetadataEntityManagerImpl.PROPERTY_ABSTRACT))
                res.add(Util.createClassMetadataLightFromNode(node));
        }

        return res;
    }

    public View getView(Long oid, String objectClass, int viewType) throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        Node instance = getInstanceOfClass(objectClass, oid);

        for (Relationship rel : instance.getRelationships(RelTypes.HAS_VIEW, Direction.OUTGOING)){
            Node viewNode = rel.getEndNode();
            if (((Integer)viewNode.getProperty(MetadataEntityManagerImpl.PROPERTY_TYPE)).intValue() == viewType){
                View res = new View(viewNode.getId(), viewType);
                if (viewNode.hasProperty(PROPERTY_BACKGROUND_PATH))
                    res.setBackgroundPath((String)viewNode.getProperty(PROPERTY_BACKGROUND_PATH));
                if (viewNode.hasProperty(PROPERTY_STRUCTURE))
                    res.setStructure((byte[])viewNode.getProperty(PROPERTY_STRUCTURE));
                return res;
            }
        }
        return null;
    }

    public void saveView(Long oid, String objectClass, int viewType, byte[] structure, String backgroundPath) throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        if (oid ==  null || objectClass == null)
            throw new InvalidArgumentException("The root object does not have any view", Level.INFO);
        Node instance = getInstanceOfClass(objectClass, oid);
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node viewNode = null;
            for (Relationship rel : instance.getRelationships(RelTypes.HAS_VIEW, Direction.OUTGOING)){
                if (((Integer)rel.getEndNode().getProperty(MetadataEntityManagerImpl.PROPERTY_TYPE)).intValue() == viewType){
                    viewNode = rel.getEndNode();
                    break;
                }
            }

            if (viewNode == null){
                viewNode = graphDb.createNode();
                viewNode.setProperty(MetadataEntityManagerImpl.PROPERTY_TYPE, viewType);
                instance.createRelationshipTo(viewNode, RelTypes.HAS_VIEW);
            }

            if (structure != null)
                viewNode.setProperty(PROPERTY_STRUCTURE, structure);

            if (backgroundPath != null)
                viewNode.setProperty(PROPERTY_BACKGROUND_PATH, backgroundPath);
            else{
                if (viewNode.hasProperty(PROPERTY_BACKGROUND_PATH))
                    viewNode.removeProperty(PROPERTY_BACKGROUND_PATH);
            }

            tx.success();
        }catch (Exception ex){
            Logger.getLogger("saveView: "+ex.getMessage()); //NOI18N
            if (tx != null)
                tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
        }
    }

    //Helpers
    private Node getInstanceOfClass(String className, Long oid) throws MetadataObjectNotFoundException, ObjectNotFoundException{

        //if any of the parameters is null, return the dummy root
        if (className == null || oid == null)
            return graphDb.getReferenceNode().getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.BOTH).getEndNode();


        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME,className).getSingle();

        if (classNode == null)
            throw new MetadataObjectNotFoundException(Util.formatString("Class %1s can not be found", className));

        Iterable<Relationship> instances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        while (instances.iterator().hasNext()){
            Node otherSide = instances.iterator().next().getStartNode();
            if (otherSide.getId() == oid.longValue())
                return otherSide;
        }
        throw new ObjectNotFoundException(className, oid);
    }

    //Queries
    public Long createQuery(String queryName, Long ownerOid, byte[] queryStructure,
            String description) throws MetadataObjectNotFoundException, InvalidArgumentException{

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
            
            if(ownerOid != null)
            {
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, false);
                Node userNode = userIndex.get(UserProfile.PROPERTY_ID, ownerOid).getSingle();

                if(userNode != null)
                    userNode.createRelationshipTo(queryNode, RelTypes.OWNS_QUERY);
            }
            else
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, true);


            queryIndex.putIfAbsent(queryNode, CompactQuery.PROPERTY_QUERYNAME, queryName);
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

    public void saveQuery(Long queryOid, String queryName, Long ownerOid,
            byte[] queryStructure, String description) throws MetadataObjectNotFoundException{

        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node queryNode =  queryIndex.get(CompactQuery.PROPERTY_ID, queryOid).getSingle();
            if(queryNode == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the query with the id %1s", queryOid));

            queryNode.setProperty(CompactQuery.PROPERTY_QUERYNAME, queryName);
            if(description == null)
                description = "";
            queryNode.setProperty(CompactQuery.PROPERTY_DESCRIPTION, description);
            queryNode.setProperty(CompactQuery.PROPERTY_QUERYSTRUCTURE, queryStructure);
            
            if(ownerOid != null)
            {
                queryNode.setProperty(CompactQuery.PROPERTY_IS_PUBLIC, false);
                Node userNode = userIndex.get(UserProfile.PROPERTY_ID, ownerOid).getSingle();
                if(userNode == null)
                    throw new MetadataObjectNotFoundException(Util.formatString(
                            "Can not find the query with the id %1s", queryOid));

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

    public void deleteQuery(Long queryOid) throws MetadataObjectNotFoundException, InvalidArgumentException {
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node queryNode =  queryIndex.get(CompactQuery.PROPERTY_ID, queryOid).getSingle();
            if(queryNode == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
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
    public List<CompactQuery> getQueries(boolean showPublic) 
            throws MetadataObjectNotFoundException, InvalidArgumentException
    {
        List<CompactQuery> queryList = new ArrayList<CompactQuery>();
        IndexHits<Node> queries = queryIndex.query(CompactQuery.PROPERTY_QUERYNAME, "*");
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
    public CompactQuery getQuery(Long queryOid) throws MetadataObjectNotFoundException, InvalidArgumentException {
        
        CompactQuery cq =  new CompactQuery();

        Node queryNode = queryIndex.get(CompactQuery.PROPERTY_ID, queryOid).getSingle();

        if (queryNode == null){
             throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the query with id %1s", queryOid));
        }
                
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

        return cq;
    }

    @Override
    public List<ResultRecord> executeQuery(ExtendedQuery query) throws MetadataObjectNotFoundException, InvalidArgumentException {

        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME, query.getClassName()).getSingle();
        Boolean isAbstract = (Boolean) classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_ABSTRACT);
        String cypherQuery = "";
        String match = "";
        String returnQuery = "instance";
        String whereQuery = "";
        Boolean isJoin = false;
        if (classNode == null) {
            throw new MetadataObjectNotFoundException(Util.formatString(
                    "Can not find the query with name %1s", query.getClassName()));//NOI18N
        }//query Params
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("className", query.getClassName());//NOI18N
        //limits
        params.put("s", (query.getLimit() * (query.getPage() - 1)));//NOI18N
        params.put("l", query.getLimit());//NOI18N
        List<String> visibleAttributeNames = query.getVisibleAttributeNames();
        List<String> attributeNames = query.getAttributeNames();
        List<String> attributeValues = query.getAttributeValues();
        List<Integer> conditions = query.getConditions();
        //result Records
        List<ResultRecord> rsltrcrdList = new ArrayList<ResultRecord>();
        if (isAbstract) {
            cypherQuery = "START abstractClassmetadata = node:classes(name = {className})";//NOI18N
            match = "MATCH abstractClassmetadata<-[:" + RelTypes.EXTENDS + "]-classmetadata<-[:" + RelTypes.INSTANCE_OF + "]-instance";//NOI18N
        } else {
            cypherQuery = "START classmetadata = node:classes(name = {className})";//NOI18N
            match = "MATCH classmetadata<-[:" + RelTypes.INSTANCE_OF + "]-instance";//NOI18N
        }
//if nothing has benn selected
        if (query.getAttributeNames() == null && query.getAttributeValues() == null && query.getConditions() == null && query.getJoins() == null) {
            cypherQuery = cypherQuery.concat(match);
            cypherQuery = cypherQuery.concat(" RETURN ".concat(returnQuery));//NOI18N
        } //Return statements
        else {
//where statements
            for (int i = 0; i < attributeNames.size(); i++) {
                String condition = "";//NOI18N
                String where = "";//NOI18N
                String value = attributeValues.get(i);
                if (attributeValues.get(i) != null) {
                    switch (conditions.get(i)) {
                        case ExtendedQuery.EQUAL:
                            condition = "! =~";//NOI18N
                            value = "(?i)".concat(value);//NOI18N
                            break;
                        case ExtendedQuery.EQUAL_OR_GREATER_THAN:
                            condition = "! >=";//NOI18N
                            break;
                        case ExtendedQuery.EQUAL_OR_LESS_THAN:
                            condition = "! <=";//NOI18N
                            break;
                        case ExtendedQuery.GREATER_THAN:
                            condition = "! >";//NOI18N
                            break;
                        case ExtendedQuery.LESS_THAN:
                            condition = "! <";//NOI18N
                            break;
                        case ExtendedQuery.LIKE:
                            condition = "! =~";//NOI18N
                            value = "(?i).*".concat(value).concat(".*");//NOI18N
                            break;
                    }
                    //the value to search for
                    Object newParam = null;/*Util.evalAttributeType(Util.getTypeOfAttribute(classNode, attributeNames.get(i)),
                            attributeNames.get(i),
                            value);*/
                    params.put(attributeNames.get(i),
                            newParam);
                    if (Long.class.isInstance(newParam) || Boolean.class.isInstance(newParam) || Float.class.isInstance(newParam) || Integer.class.isInstance(newParam)) {
                        condition = condition.substring(0, condition.length() - 1);
                    }
                    where = "instance.".concat(attributeNames.get(i)).concat(condition).concat(" {".concat(attributeNames.get(i)).concat("}"));
                }//end if is not a join value
                else {
                    List<ExtendedQuery> joins = query.getJoins();
                    for (ExtendedQuery join : joins) {
                    //if there are no filter in a listTypes, but there is at least a atrribute set a s visible
                        if (join.getAttributeNames() != null && join.getAttributeValues() != null) {
                            List<String> joinAttributeNames = join.getAttributeNames();
                            List<String> joinAttributeValues = join.getAttributeValues();
                            List<Integer> joinConditions = join.getConditions();
                            for (int j = 0; j < joinAttributeNames.size(); j++) {
                                String joinCondition = "";
                                String joinValue = joinAttributeValues.get(j);
                                if (joinAttributeValues.get(j) != null) {
                                    switch (joinConditions.get(j)) {
                                        case ExtendedQuery.EQUAL:
                                            joinCondition = "! =~";//NOI18N
                                            joinValue = "(?i)".concat(joinValue);//NOI18N
                                            break;
                                        case ExtendedQuery.EQUAL_OR_GREATER_THAN:
                                            joinCondition = "! >=";//NOI18N
                                            break;
                                        case ExtendedQuery.EQUAL_OR_LESS_THAN:
                                            joinCondition = "! <=";//NOI18N
                                            break;
                                        case ExtendedQuery.GREATER_THAN:
                                            joinCondition = "! >";//NOI18N
                                            break;
                                        case ExtendedQuery.LESS_THAN:
                                            joinCondition = "! <";//NOI18N
                                            break;
                                        case ExtendedQuery.LIKE:
                                            joinCondition = "! =~";//NOI18N
                                            joinValue = "(?i).*".concat(joinValue).concat(".*");//NOI18N
                                            break;
                                    }
                                    //if is small view
                                    if (joinAttributeNames.get(j).equals("id")) {
                                        params.put(joinAttributeNames.get(j), Long.valueOf(joinAttributeValues.get(j)));
                                        where = "ID(listype)".concat("=").concat(" {".concat(joinAttributeNames.get(j)).concat("}"));//NOI18N
                                    } else {
                                        Node joinNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME, join.getClassName()).getSingle();
                                        Object newJoinParam = null;/*Util.evalAttributeType(Util.getTypeOfAttribute(joinNode, joinAttributeNames.get(j)),
                                                joinAttributeNames.get(j),
                                                joinValue);*/
                                        params.put("join".concat(joinAttributeNames.get(j)),
                                                newJoinParam);
                                        if (Long.class.isInstance(newJoinParam) || Boolean.class.isInstance(newJoinParam) || Float.class.isInstance(newJoinParam) || Integer.class.isInstance(newJoinParam)) {
                                            joinCondition = joinCondition.substring(0, joinCondition.length() - 1);
                                        }
                                        where = "listype.".concat(joinAttributeNames.get(j)).concat(joinCondition).concat(" {".concat("join".concat(joinAttributeNames.get(j))).concat("}"));//NOI18N
                                    }
                                    isJoin = true;
                                }//end if
                            }//end else there ara a filter
                        }//end for
                    }//fin for joins
                }//end else join
                if (i + 1 < attributeNames.size()) {
                    whereQuery = whereQuery.concat(where.concat(query.getLogicalConnector() == ExtendedQuery.CONNECTOR_AND ? " AND " : " OR "));//NOI18N
                } else {
                    whereQuery = whereQuery.concat(where);
                }
            }//end for attributeNames
            if (isJoin) {
                match = match.concat("-[:").concat(RelTypes.RELATED_TO.toString()).concat("]->listype");//NOI18N
            }
            cypherQuery = cypherQuery.concat(match);
            //it probably occurs if a listtype atribute is set as visible and a class attributes is visible too
            if (!whereQuery.isEmpty()) {
                cypherQuery = cypherQuery.concat(" WHERE ".concat(whereQuery));//NOI18N
            }
            cypherQuery = cypherQuery.concat(" RETURN ".concat(returnQuery));//NOI18N
        }//end else
        if (query.getPage() == 0) {
            cypherQuery = cypherQuery.concat(" ORDER BY instance.name ASC");//NOI18N
        } else {
            cypherQuery = cypherQuery.concat(" ORDER BY instance.name ASC skip {s} limit {l}");//NOI18N
        }
        ExecutionEngine engine = new ExecutionEngine(graphDb);
        ExecutionResult result = engine.execute(cypherQuery, params);
        Iterator<Node> n_column = result.columnAs("instance");
        //headers
        ResultRecord resltRcrdHeader = new ResultRecord(null, null, null);
        if (visibleAttributeNames == null) {
            visibleAttributeNames = new ArrayList<String>();
            visibleAttributeNames.add(query.getClassName());
        }
        resltRcrdHeader.setExtraColumns(visibleAttributeNames);
        rsltrcrdList.add(resltRcrdHeader);
        for (Node node : IteratorUtil.asIterable(n_column)) {
            if (isAbstract) {
                rsltrcrdList.add(Util.createResultRecordFromNode(node, Util.getClassName(node), visibleAttributeNames));
            } else {
                rsltrcrdList.add(Util.createResultRecordFromNode(node, query.getClassName(), visibleAttributeNames));
            }
        }
        return rsltrcrdList;
    }
    
    public byte[] getClassHierachy(boolean showAll) throws MetadataObjectNotFoundException, InvalidArgumentException{
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        WAX xmlWriter = new WAX(bas);
        StartTagWAX rootTag = xmlWriter.start("hierarchy");
        rootTag.attr("documentVersion", Constants.CLASS_HIERARCHY_DOCUMENT_VERSION);
        rootTag.attr("serverVersion", Constants.PERSISTENCE_SERVICE_VERSION);
        rootTag.attr("date", Calendar.getInstance().getTimeInMillis());
        StartTagWAX inventoryTag = rootTag.start("inventory");
        StartTagWAX classesTag = inventoryTag.start("classes");
        Node rootObjectNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME, "RootObject").getSingle(); //NOI18N
        if (rootObjectNode == null)
            throw new MetadataObjectNotFoundException(Util.formatString("Class %1s can not be found", "RootObject"));
        getXMLNodeForClass(rootObjectNode, rootTag);
        classesTag.end();
        inventoryTag.end();
        rootTag.end().close();
        return bas.toByteArray();
    }

    /**
     * Helpers
     */
        /**
     * recursive method used to generate a single "class" node (see the <a href="http://neotropic.co/kuwaiba/wiki/index.php?title=XML_Documents#To_describe_the_data_model">wiki</a> for details)
     * @param classNode Node representing the class to be added
     * @param paretTag Parent to attach the new class node
     */
    private void getXMLNodeForClass(Node classNode, StartTagWAX parentTag) {
        int applicationModifiers = 0;
        int javaModifiers = 0;
        StartTagWAX currentTag = parentTag.start("class"); //NOI18N
        currentTag.attr("name", classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME));
        
        currentTag.attr("classPackage", "");
        
        //Application modifiers
        if ((Boolean)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_COUNTABLE))
            applicationModifiers |= Constants.CLASS_MODIFIER_COUNTABLE;

        if ((Boolean)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_CUSTOM))
            applicationModifiers |= Constants.CLASS_MODIFIER_CUSTOM;

        currentTag.attr("applicationModifiers",applicationModifiers);

        //Language modifiers
        if ((Boolean)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_ABSTRACT))
            applicationModifiers |= Modifier.ABSTRACT;
        
        currentTag.attr("javaModifiers",javaModifiers);
        
        //Class type
        if (classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME).equals("RootObject")){
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
            attributeTag.attr("name", attributeNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME));
            attributeTag.attr("type", attributeNode.getProperty(MetadataEntityManagerImpl.PROPERTY_TYPE));
            attributeTag.attr("javaModifiers", 0); //Not used
            //Application modifiers
            if ((Boolean)attributeNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NO_COPY))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_NOCOPY;
            if ((Boolean)attributeNode.getProperty(MetadataEntityManagerImpl.PROPERTY_VISIBLE))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_VISIBLE;
            if ((Boolean)attributeNode.getProperty(MetadataEntityManagerImpl.PROPERTY_ADMINISTRATIVE))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_ADMINISTRATIVE;
            if ((Boolean)attributeNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NO_SERIALIZE))
                attributeApplicationModifiers |= Constants.ATTRIBUTE_MODIFIER_NOSERIALIZE;
            if ((Boolean)attributeNode.getProperty(MetadataEntityManagerImpl.PROPERTY_READONLY))
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
}

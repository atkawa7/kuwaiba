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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.application.CompactQuery;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.ResultRecord;
import org.kuwaiba.apis.persistence.application.TransientQuery;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.application.View;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.interfaces.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.persistenceservice.caching.CacheManager;
import org.kuwaiba.persistenceservice.impl.enumerations.RelTypes;
import org.kuwaiba.persistenceservice.util.Util;
import org.kuwaiba.psremoteinterfaces.ApplicationEntityManagerRemote;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
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
            throws InvalidArgumentException, ObjectNotFoundException
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
            if(groups.size()<1)
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
            throws InvalidArgumentException, ObjectNotFoundException
    {
        Transaction tx = null;
        try{
            tx =  graphDb.beginTx();
            Node userNode = userIndex.get(UserProfile.PROPERTY_ID, oid).getSingle();

            if(userName != null){
                if (userName.trim().equals("")) //NOI18N
                    throw new InvalidArgumentException("User name can't be an empty string", Level.INFO);

                if (cm.getUser(userName) == null)
                {
                    Node storedUser = userIndex.get(UserProfile.PROPERTY_USERNAME,userName).getSingle();
                    if (storedUser != null)
                        throw new InvalidArgumentException(Util.formatString("The username %1s is already in use", userName), Level.WARNING);
                }

                userNode.setProperty(UserProfile.PROPERTY_USERNAME, userName);
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
            List<Integer> privileges, List<Long> users) throws InvalidArgumentException, ObjectNotFoundException
    {
        if (groupName == null)
            throw new InvalidArgumentException("Group name can't be null", Level.INFO);

        if (groupName.trim().equals("")) //NOI18N
            throw new InvalidArgumentException("User name can't be an empty string", Level.INFO);

        if (cm.getGroup(groupName) == null)
        {
            Node storedUser = groupIndex.get(GroupProfile.PROPERTY_GROUPNAME,groupName).getSingle();
            if (storedUser != null)
                throw new InvalidArgumentException(Util.formatString("The group name %1s is already in use", groupName), Level.WARNING);
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

    public List<UserProfile> getUsers() throws InvalidArgumentException, ObjectNotFoundException {
        IndexHits<Node> usersNodes = userIndex.query(UserProfile.PROPERTY_USERNAME, "*");
        List<UserProfile> users = new ArrayList<UserProfile>();
        
        for (Node node : usersNodes)
        {
            users.add(Util.createUserProfileFromNode(node));
            
        }
        return users;
    }

    public List<GroupProfile> getGroups() throws InvalidArgumentException, ObjectNotFoundException {
        IndexHits<Node> groupsNodes = groupIndex.query(GroupProfile.PROPERTY_GROUPNAME, "*");
        List<GroupProfile> groups =  new ArrayList<GroupProfile>();
        for (Node node : groupsNodes)
        {
            groups.add((Util.createGroupProfileFromNode(node)));
        }
        return groups;
    }

    public void setGroupProperties(Long id, String groupName, String description, List<Integer> privileges, List<Long> users) throws InvalidArgumentException, ObjectNotFoundException {

        Transaction tx = null;
        try{
            tx = graphDb.beginTx();

            Node groupNode = groupIndex.get(GroupProfile.PROPERTY_ID, id).getSingle();

            if(groupName != null){
                if (groupName.trim().equals("")) //NOI18N
                    throw new InvalidArgumentException("User name can't be an empty string", Level.INFO);

                if (cm.getUser(groupName) == null)
                {
                    Node storedGroup = groupIndex.get(GroupProfile.PROPERTY_GROUPNAME, groupName).getSingle();
                    if (storedGroup != null)
                        throw new InvalidArgumentException(Util.formatString("The group name %1s is already in use", groupName), Level.WARNING);
                }

                groupNode.setProperty(GroupProfile.PROPERTY_GROUPNAME, groupName);
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

    public void deleteUsers(List<Long> oids) throws InvalidArgumentException, ObjectNotFoundException {
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            //TODO watch if there is relationships you can not delete
            if(oids != null){
                for (Long id : oids)
                {
                    Node userNode = userIndex.get(UserProfile.PROPERTY_ID, id).getSingle();

                    cm.removeUser((String)userNode.getProperty(UserProfile.PROPERTY_USERNAME));

                    Iterable<Relationship> relationships = userNode.getRelationships();
                    for (Relationship relationship : relationships) {
                        relationship.delete();
                    }
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

    public void deleteGroups(List<Long> oids) throws InvalidArgumentException, ObjectNotFoundException {
        Transaction tx = null;
        try{
            if(oids != null){
                tx = graphDb.beginTx();
                for (Long id : oids) {
                    Node groupNode = groupIndex.get(GroupProfile.PROPERTY_ID, id).getSingle();

                    cm.removeGroup((String)groupNode.getProperty(GroupProfile.PROPERTY_GROUPNAME));

                    Iterable<Relationship> relationships = groupNode.getRelationships();
                    for (Relationship relationship : relationships) {
                        relationship.delete();
                    }
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
        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME, className).getSingle();
        if (classNode ==  null)
            throw new MetadataObjectNotFoundException(Util.formatString("Class for object with oid %1s could not be found",className));
        if (!cm.isSubClass("GenericObjectList", className))
            throw new InvalidArgumentException(Util.formatString("Class %1s is not a list type", className), Level.SEVERE);

        Transaction tx = null;
        try{
             tx = graphDb.beginTx();
             Node newItem = graphDb.createNode();
             newItem.setProperty(MetadataEntityManagerImpl.PROPERTY_NAME, name);
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
            throw new MetadataObjectNotFoundException(Util.formatString("Class for object with oid %1s could not be found",className));

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
        Traverser traverserMetadata = Util.traverserMetadata(genericObjectListNode);
        List<ClassMetadataLight> res = new ArrayList<ClassMetadataLight>();
        for (Node child : traverserMetadata){
            if (!(Boolean)child.getProperty(MetadataEntityManagerImpl.PROPERTY_ABSTRACT))
                res.add(new ClassMetadataLight(child.getId(),(String)child.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME),(String)child.getProperty(MetadataEntityManagerImpl.PROPERTY_DISPLAY_NAME)));
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
                    userNode.createRelationshipTo(queryNode, RelTypes.OWNS);
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

                Relationship singleRelationship = queryNode.getSingleRelationship(RelTypes.OWNS, Direction.INCOMING);

                if(singleRelationship == null)
                    userNode.createRelationshipTo(queryNode, RelTypes.OWNS);
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
                        "Can not find the query with the id %1s", queryOid));

            Iterable<Relationship> relationships = queryNode.getRelationships(RelTypes.OWNS, Direction.INCOMING);
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

            Relationship ownRelationship = queryNode.getSingleRelationship(RelTypes.OWNS, Direction.INCOMING);

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
                        "Can not find the query with the id %1s", queryOid));
        }
                
        cq.setName((String)queryNode.getProperty(CompactQuery.PROPERTY_QUERYNAME));
        cq.setDescription((String)queryNode.getProperty(CompactQuery.PROPERTY_DESCRIPTION));
        cq.setContent((byte[])queryNode.getProperty(CompactQuery.PROPERTY_QUERYSTRUCTURE));
        cq.setIsPublic((Boolean)queryNode.getProperty(CompactQuery.PROPERTY_IS_PUBLIC));
        cq.setId(queryNode.getId());

        Relationship ownRelationship = queryNode.getSingleRelationship(RelTypes.OWNS, Direction.INCOMING);

        if(ownRelationship != null){
            Node ownerNode =  ownRelationship.getStartNode();
            cq.setOwnerId(ownerNode.getId());
        }

        return cq;
    }

    @Override
    public List<ResultRecord> executeQuery(TransientQuery query) throws MetadataObjectNotFoundException, InvalidArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

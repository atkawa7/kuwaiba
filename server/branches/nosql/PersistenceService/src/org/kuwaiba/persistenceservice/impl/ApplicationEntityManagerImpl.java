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
import java.util.Random;
import java.util.logging.Level;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.interfaces.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
import org.kuwaiba.persistenceservice.caching.CacheManager;
import org.kuwaiba.persistenceservice.impl.enumerations.RelTypes;
import org.kuwaiba.persistenceservice.util.Util;
import org.kuwaiba.psremoteinterfaces.ApplicationEntityManagerRemote;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
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
     * Graph db service
     */
    private GraphDatabaseService graphDb;
    /**
     * Users index
     */
    private Index<Node> userIndex;
    /**
     * Groups index
     */
    private Index<Node> groupIndex;

    public ApplicationEntityManagerImpl(ConnectionManager cmn) {
        this.graphDb = (EmbeddedGraphDatabase)cmn.getConnectionHandler();
        this.userIndex = graphDb.index().forNodes(INDEX_USER);
        this.groupIndex = graphDb.index().forNodes(INDEX_GROUP);
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

        if (CacheManager.getInstance().getUser(userName) == null)
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
        newUser.setProperty(UserProfile.PROPERTY_FIRST_NAME, firstName);
        newUser.setProperty(UserProfile.PROPERTY_LAST_NAME, lastName);
        newUser.setProperty(UserProfile.PROPERTY_ENABLED, enabled);

        if (privileges != null)
            newUser.setProperty(UserProfile.PROPERTY_PRIVILEGES, privileges);

        if (groups != null)
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
        
        userIndex.putIfAbsent(newUser, UserProfile.PROPERTY_ID, newUser.getId());
        userIndex.putIfAbsent(newUser, UserProfile.PROPERTY_USERNAME, userName);
        CacheManager.getInstance().putUser(new UserProfile(newUser.getId(), userName,
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

                if (CacheManager.getInstance().getUser(userName) == null)
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
                Boolean isPartOf = false;
                for (Long id : groups) {
                    Node groupNode = groupIndex.get(GroupProfile.PROPERTY_ID, id).getSingle();
                    Iterable<Relationship> relationships = groupNode.getRelationships(RelTypes.BELONGS_TO_GROUP, Direction.INCOMING);
                    for (Relationship relationship : relationships) {
                        if(userNode.getId() != relationship.getStartNode().getId())
                            isPartOf = true;
                    }
                    if(!isPartOf)
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

    public Long createGroup(String groupName, String description)
            throws InvalidArgumentException, ObjectNotFoundException
    {
        if (groupName == null)
            throw new InvalidArgumentException("Group name can't be null", Level.INFO);

        if (groupName.trim().equals("")) //NOI18N
            throw new InvalidArgumentException("User name can't be an empty string", Level.INFO);

        if (CacheManager.getInstance().getGroup(groupName) == null)
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
            newGroup.setProperty(GroupProfile.PROPERTY_DESCRIPTION, description);

            groupIndex.putIfAbsent(newGroup, GroupProfile.PROPERTY_ID, newGroup.getId());
            groupIndex.putIfAbsent(newGroup, GroupProfile.PROPERTY_GROUPNAME, groupName);
            CacheManager.getInstance().putGroup(new GroupProfile(newGroup.getId(), groupName,
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
            users.add(Util.createsUserProfileFormNode(node));
            
        }
        return users;
    }

    public List<GroupProfile> getGroups() throws InvalidArgumentException, ObjectNotFoundException {
        IndexHits<Node> groupsNodes = groupIndex.query(GroupProfile.PROPERTY_GROUPNAME, "*");
        List<GroupProfile> groups =  new ArrayList<GroupProfile>();
        for (Node node : groupsNodes)
        {
            groups.add((Util.createsGroupProfileFormNode(node)));
        }
        return groups;
    }

    public UserProfile addUser() throws InvalidArgumentException, ObjectNotFoundException {
        UserProfile newUser = new UserProfile();
        Random random = new Random();
        newUser.setUserName("user"+random.nextInt(10000));
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node newUserNode = graphDb.createNode();

            newUserNode.setProperty(UserProfile.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            newUserNode.setProperty(UserProfile.PROPERTY_USERNAME, "user"+random.nextInt(10000));
            newUserNode.setProperty(UserProfile.PROPERTY_FIRST_NAME, "");
            newUserNode.setProperty(UserProfile.PROPERTY_LAST_NAME, "");
            newUserNode.setProperty(UserProfile.PROPERTY_ENABLED, true);
            newUserNode.setProperty(UserProfile.PROPERTY_PASSWORD, Util.getMD5Hash("kuwaiba"));

            
            CacheManager.getInstance().putUser(new UserProfile(newUserNode.getId(), "",
                "", "", true, (Long)newUserNode.getProperty(UserProfile.PROPERTY_CREATION_DATE), null));

            userIndex.putIfAbsent(newUserNode, UserProfile.PROPERTY_ID, newUserNode.getId());
            userIndex.putIfAbsent(newUserNode, UserProfile.PROPERTY_USERNAME, newUser.getUserName());
           
            //becomes a users group member default
            Node groupUserNode =  groupIndex.get(GroupProfile.PROPERTY_GROUPNAME, "users").getSingle();
            newUserNode.createRelationshipTo(groupUserNode, RelTypes.BELONGS_TO_GROUP);

            tx.success();

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
        }
        return newUser;
    }

    public void setGroupProperties(Long id, String groupName, String description, List<Integer> privileges) throws InvalidArgumentException, ObjectNotFoundException {

        Transaction tx = null;
        try{
            tx = graphDb.beginTx();

            Node groupNode = groupIndex.get(GroupProfile.PROPERTY_ID, id).getSingle();

            if(groupName != null){
                if (groupName.trim().equals("")) //NOI18N
                    throw new InvalidArgumentException("User name can't be an empty string", Level.INFO);

                if (CacheManager.getInstance().getUser(groupName) == null)
                {
                    Node storedGroup = groupIndex.get(GroupProfile.PROPERTY_GROUPNAME, groupName).getSingle();
                    if (storedGroup != null)
                        throw new InvalidArgumentException(Util.formatString("The group name %1s is already in use", groupName), Level.WARNING);
                }

                groupNode.setProperty(GroupProfile.PROPERTY_GROUPNAME, groupName);
            }
            
            if(description != null)
                groupNode.setProperty(GroupProfile.PROPERTY_DESCRIPTION, description);

            tx.success();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
        }
    }

    public GroupProfile addGroup() throws InvalidArgumentException, ObjectNotFoundException {
        
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            GroupProfile newGroup = new GroupProfile();
            Random random = new Random();
            newGroup.setName("user"+random.nextInt(10000));

            Node newGroupNode = graphDb.createNode();

            newGroupNode.setProperty(GroupProfile.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            newGroupNode.setProperty(GroupProfile.PROPERTY_GROUPNAME, newGroup.getName());

            CacheManager.getInstance().putGroup(new GroupProfile(newGroupNode.getId(), newGroup.getName(),
                "", (Long)newGroupNode.getProperty(UserProfile.PROPERTY_CREATION_DATE)));

            groupIndex.putIfAbsent(newGroupNode, GroupProfile.PROPERTY_ID, newGroupNode.getId());
            groupIndex.putIfAbsent(newGroupNode, GroupProfile.PROPERTY_GROUPNAME, newGroup.getName());

            tx.success();

            return newGroup;

        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
        }
    }

    public void deleteUsers(Long[] oids) throws InvalidArgumentException, ObjectNotFoundException {
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            //TODO watch if there is relationships you can not delete
            if(oids != null){
                for (Long id : oids)
                {
                    Node userNode = userIndex.get(UserProfile.PROPERTY_ID, id).getSingle();

                    CacheManager.getInstance().removeUser((String)userNode.getProperty(UserProfile.PROPERTY_USERNAME));

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

    public void deleteGroups(Long[] oids) throws InvalidArgumentException, ObjectNotFoundException {
        Transaction tx = null;
        try{
            if(oids != null){
                tx = graphDb.beginTx();
                for (Long id : oids) {
                    Node groupNode = groupIndex.get(GroupProfile.PROPERTY_ID, id).getSingle();

                    CacheManager.getInstance().removeGroup((String)groupNode.getProperty(GroupProfile.PROPERTY_GROUPNAME));

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

    public void addGroupsToUser(List<Long> groupsOids, Long userOid) throws InvalidArgumentException, ObjectNotFoundException {
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node userNode = userIndex.get(UserProfile.PROPERTY_ID, userOid).getSingle();
            for (Long groupIds : groupsOids) {
                Node groupNode = groupIndex.get(GroupProfile.PROPERTY_ID, groupIds).getSingle();
                userNode.createRelationshipTo(groupNode, RelTypes.BELONGS_TO_GROUP);
            }
            tx.success();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
        }
    }

    public void removeGroupsFromUser(List<Long> groupsOids, Long userOid) throws InvalidArgumentException, ObjectNotFoundException {
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node userNode = userIndex.get(UserProfile.PROPERTY_ID, userOid).getSingle();
            Iterable<Relationship> relationships = userNode.getRelationships(RelTypes.BELONGS_TO_GROUP, Direction.OUTGOING);
            for (Relationship relationship : relationships)
            {
                Node groupNode = relationship.getEndNode();
                for (Long groupIds : groupsOids)
                {
                    if(groupNode.getId() == groupIds)
                        relationship.delete();
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

}

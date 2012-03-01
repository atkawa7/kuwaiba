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

import java.util.List;
import java.util.logging.Level;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.interfaces.ApplicationEntityManager;
import org.kuwaiba.persistenceservice.caching.CacheManager;
import org.kuwaiba.persistenceservice.impl.enumerations.RelTypes;
import org.kuwaiba.persistenceservice.util.Util;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

/**
 * Application Entity Manager reference implementation
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ApplicationEntityManagerImpl implements ApplicationEntityManager{

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

    public ApplicationEntityManagerImpl(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        this.userIndex = graphDb.index().forNodes(INDEX_USER);
        this.groupIndex = graphDb.index().forNodes(INDEX_GROUP);
    }

    public UserProfile login(String username, String password) {

        if (username == null || password == null)
            return null;

        Node user = userIndex.get(UserProfile.PROPERTY_USERNAME,username).getSingle();
        if (user == null)
            return null;

        if (Util.getMD5Hash(password).equals(user.getProperty(UserProfile.PROPERTY_PASSWORD)))
            return new UserProfile(new Long(user.getId()),
                    (String)user.getProperty(UserProfile.PROPERTY_USERNAME),
                    (String)user.getProperty(UserProfile.PROPERTY_FIRST_NAME),
                    (String)user.getProperty(UserProfile.PROPERTY_LAST_NAME),
                    (List<Integer>)user.getProperty(UserProfile.PROPERTY_PRIVILEGES)
                    );
        else
            return null;
    }

    public Long createUser(String userName, String password, String firstName, String lastName, List<Integer> privileges, List<Long> groups) throws InvalidArgumentException, ObjectNotFoundException {
        if (userName == null)
            throw new InvalidArgumentException("User name can't be null", Level.INFO);

        if (userName.trim().equals("")) //NOI18N
            throw new InvalidArgumentException("User name can't be an empty string", Level.INFO);

        if (password == null)
            throw new InvalidArgumentException("Password can't be null", Level.INFO);

        if (userName.trim().equals("")) //NOI18N
            throw new InvalidArgumentException("Password can't be an empty string", Level.INFO);

        if (CacheManager.getInstance().getUser(userName) == null){
            Node storedUser = userIndex.get(UserProfile.PROPERTY_USERNAME,userName).getSingle();
            if (storedUser != null)
                throw new InvalidArgumentException(Util.formatString("The username %1s is already in use", userName), Level.WARNING);
        }
        Transaction tx = graphDb.beginTx();
        Node newUser = graphDb.createNode();
        newUser.setProperty(UserProfile.PROPERTY_USERNAME, userName);
        newUser.setProperty(UserProfile.PROPERTY_PASSWORD, Util.getMD5Hash(password));
        newUser.setProperty(UserProfile.PROPERTY_FIRST_NAME, firstName);
        newUser.setProperty(UserProfile.PROPERTY_LAST_NAME, lastName);

        if (privileges != null)
            newUser.setProperty(UserProfile.PROPERTY_PRIVILEGES, privileges);

        if (groups != null){
            for (Long groupId : groups){
                Node group = groupIndex.get(UserProfile.PROPERTY_ID,groupId).getSingle();
                if (group != null)
                    newUser.createRelationshipTo(group, RelTypes.BELONGS_TO_GROUP);
                else{
                    tx.failure();
                    throw new InvalidArgumentException(Util.formatString("Group with id %1s can't be found",groupId), Level.OFF);
                }
            }
        }
        userIndex.putIfAbsent(newUser, UserProfile.PROPERTY_ID, newUser.getId());
        userIndex.putIfAbsent(newUser, UserProfile.PROPERTY_USERNAME, userName);
        CacheManager.getInstance().putUser(new UserProfile(newUser.getId(), userName, firstName, lastName, privileges));
        tx.success();
        return new Long(newUser.getId());
    }

    public void setUserProperties(String userName, String password, String firstName, String lastName, List<Integer> privileges, List<Long> groups) throws InvalidArgumentException, ObjectNotFoundException {

    }
}

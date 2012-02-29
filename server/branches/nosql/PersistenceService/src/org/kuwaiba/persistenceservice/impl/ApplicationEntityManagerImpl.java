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

import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.interfaces.ApplicationEntityManager;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

/**
 * Application Entity Manager reference implementation
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ApplicationEntityManagerImpl implements ApplicationEntityManager{

    public static final String INDEX_USER = "userIndex";
    /**
     * Graph db service
     */
    private GraphDatabaseService graphDb;
    /**
     * User's index
     */
    private Index<Node> userIndex;

    public ApplicationEntityManagerImpl(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        this.userIndex = graphDb.index().forNodes(INDEX_USER);
    }

    public UserProfile login(String username, String password) {
        Node user = userIndex.get(UserProfile.PROPERTY_USERNAME,username).getSingle();
        if (user == null)
            return null;

        return new UserProfile(new Long(user.getId()),
                (String)user.getProperty(UserProfile.PROPERTY_USERNAME),
                (String)user.getProperty(UserProfile.PROPERTY_FIRST_NAME),
                (String)user.getProperty(UserProfile.PROPERTY_LAST_NAME),
                (List<Integer>)user.getProperty(UserProfile.PROPERTY_PRIVILEGES)
                );

    }

    public Long createUser(HashMap<String,String> properties) throws InvalidArgumentException {
        for
    }

    public void setUserProperties(Long oid, List<String> propertyNames, List<String> propertyValues) throws InvalidArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

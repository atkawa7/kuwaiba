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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kuwaiba.n4jdriver;

import java.util.HashMap;
import org.kuwaiba.n4jdriver.model.Person;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;
import org.neo4j.driver.v1.types.Node;

/**
 * A simple service that implements the actual persistence logic.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class PersistenceService {
    /**
     * Database server host.
     */
    private String dbHost;
    /**
     * Database server user.
     */
    private String dbUser;
    /**
     * Database server password.
     */
    private String dbPassword;
    /**
     * Database server port.
     */
    private int dbPort;
    /**
     * Connection handle
     */
    private Driver driver;

    public PersistenceService(String dbHost, String dbUser, String dbPassword, int dbPort) {
        this.dbHost = dbHost;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbPort = dbPort;
    }
    
    /**
     * Opens a connection to the database.
     * @throws org.kuwaiba.n4jdriver.PersistenceService.ConnectionException If the user, password or db location are incorrect or the 
     * server did not respond
     */
    public void open() throws ConnectionException {
        try {
            this.driver = GraphDatabase.driver("bolt://" + dbHost + ":" + dbPort, AuthTokens.basic(dbUser, dbPassword));
        } catch (Exception ex) {
            throw new ConnectionException(String.format("Unexpected error while creating aconnnection to the server: %s", ex.getMessage()));
        }
    }
    
    /**
     * Persists a person in the database.
     * @param aPerson The person to be persisted. The id is generated automatically. This is a pass by reference, 
     * the aPerson's id is updated during the transaction
     * @return If the operation was successful or not.
     * @throws IllegalArgumentException If one of the parameters supplied to execute the queries is inadequate.
     */
    public boolean createPerson(Person aPerson) throws IllegalArgumentException {
        if (this.driver == null)
            throw new IllegalArgumentException("The connection to the database is not active");
            
        try (Session session = this.driver.session()) {
            return session.writeTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction tx) {
                    HashMap<String, Object> parameters = new HashMap<>();
                    parameters.put("name", aPerson.getName());
                    StatementResult rs = tx.run("CREATE (p:Person) "
                            + "SET p.name = $name "
                            + "RETURN p", parameters);
                    aPerson.setId(rs.single().get(0).asNode().id());
                    tx.success();
                    
                    return rs.summary().counters().nodesCreated() > 0;  // This checks if there were records updated or not
                }
            });
        }
    }
    
    /**
     * Updates the name of of a person in the database.
     * @param aPerson The person.
     * @return If the operation was successful or not.
     * @throws IllegalArgumentException If the new name has an invalid character. 
     */
    public boolean updatePerson(Person aPerson) throws IllegalArgumentException {
        if (this.driver == null)
            throw new IllegalArgumentException("The connection to the database is not active");
            
        try (Session session = this.driver.session()) {
            return session.writeTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction tx) {
                    HashMap<String, Object> parameters = new HashMap<>();
                    parameters.put("name", aPerson.getName());
                    parameters.put("id", aPerson.getId());
                    StatementResult rs = tx.run("MATCH (p:Person) "
                            + "WHERE id(p) = $id "
                            + "SET p.name = $name", parameters);
                    tx.success();
                    
                    return rs.summary().counters().propertiesSet() > 0; // This checks if there were records updated or not
                }
            });
        }
    }
    
    /**
     * Fetches a person from the database taking its id as parameter.
     * @param id The id of the person to be fetched.
     * @return The person or null if none found.
     * @throws IllegalArgumentException If the name has an invalid value
     */
    public Person getPerson(long id) throws IllegalArgumentException {
        if (this.driver == null)
            throw new IllegalArgumentException("The connection to the database is not active");
            
        try (Session session = this.driver.session()) {
            return session.readTransaction(new TransactionWork<Person>() {
                @Override
                public Person execute(Transaction tx) {
                    HashMap<String, Object> parameters = new HashMap<>();
                    parameters.put("id", id);
                    StatementResult rs = tx.run("MATCH (p:Person) "
                            + "WHERE id(p) = $id "
                            + "RETURN p", parameters);
                    if (rs.hasNext()) {
                        Node personNode = rs.single().get(0).asNode();
                        return new Person(personNode.id(), personNode.get("name").asString());
                    }

                    return null;
                }
            });
        }
    }
    
    /**
     * Deletes a person from the database.
     * @param id the id of the person to be deleted.
     * @throws IllegalArgumentException if the id could not be found.
     */
    public void deletePerson(long id) throws IllegalArgumentException {
        if (this.driver == null)
            throw new IllegalArgumentException("The connection to the database is not active");
            
        try (Session session = this.driver.session()) {
            session.writeTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction tx) {
                    HashMap<String, Object> parameters = new HashMap<>();
                    parameters.put("id", id);
                    StatementResult rs = tx.run("MATCH (p:Person) "
                            + "WHERE id(p) = $id "
                            + "DELETE p", parameters);
                    tx.success();
                    return rs.summary().counters().nodesDeleted() != 0;
                }
            });
        }
    }
    
    /**
     * Shuts down the connection (if already open)
     */
    public void shutdown() {
        if (driver != null)
            driver.close();
    }
    
    /**
     * Exception thrown upon a problem connecting to the database or during the transport.
     */
    public static class ConnectionException extends Exception {

        public ConnectionException(String msg) {
            super (msg);
        }
    }
}

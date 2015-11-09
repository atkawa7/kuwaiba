/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>
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


import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.persistence.exceptions.ConnectionException;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

/**
 * ConnectionManager reference implementation
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ConnectionManagerImpl implements ConnectionManager <GraphDatabaseService>{

    /**
     *
     */
    public static final String DEFAULT_DB_PATH = "target/kuwaiba.db";
    /**
     * Neo4J Database handler
     */
    private GraphDatabaseService graphDb;
    /**
     * Neo4J Transaction handler
     */
    private Transaction tx;
    


    @Override
    public void closeConnection() {
        graphDb.shutdown();
    }

    @Override
    public void commitTransaction() {
        tx.success();
    }

    @Override
    public List<ConnectionManager> getConnectionPool() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSpawned() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void openConnection() throws ConnectionException {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("persistence.properties"));
            graphDb = new EmbeddedGraphDatabase(props.getProperty("db_path"));
            registerShutdownHook( graphDb );
        }catch(Exception e){
            System.out.println(e.getMessage());
            throw new ConnectionException(e.getMessage());
        }
    }

    @Override
    public void rollbackTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ConnectionManager spawnConnection() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public EmbeddedGraphDatabase getConnectionHandler(){
        return (EmbeddedGraphDatabase)graphDb;
    }

    public void shutDown()
    {
        System.out.println( "Shutting down db..." );
        graphDb.shutdown();
        System.out.println( "db shut down..." );
    }
    
    public void printConnectionDetails(){
        System.out.println(graphDb.toString());
    }
   
    private void registerShutdownHook( final GraphDatabaseService graphDb)
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }
 
}

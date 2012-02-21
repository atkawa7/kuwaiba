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


import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;

/**
 * ConnectionManager implementation
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ConnectionManagerImpl implements ConnectionManager <GraphDatabaseService, Transaction>{

    /**
     * Neo4J Database instance
     */
    private GraphDatabaseService graphDb;
    /**
     * ClassMetada index
     */
    private static Index<Node> classIndex;
    /**
     * Category index
     */
    private static Index<Node> categoryIndex;
    /**
     * Neo4J Transaction instance
     */
    private Transaction tx;
    


    @Override
    public void closeConnection() {
        System.out.println();
        System.out.println( "Shutting down database ..." );
        // START SNIPPET: shutdownServer
        graphDb.shutdown();
        
        // END SNIPPET: shutdownServer
    }

    @Override
    public void commitTransaction(Transaction tx) {
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
    public void openConnection() {
        
        Properties props = new Properties();
        try {
             props.load(new FileInputStream("connection.properties"));

        }catch(IOException e){
            System.out.println(e.getMessage());
        }
        
        graphDb = new EmbeddedGraphDatabase(props.getProperty("connection_host") 
                                            +  props.getProperty("conection_DB_PATH") +
                                            props.getProperty("connection_database"));

        classIndex = graphDb.index().forNodes("ClassMetadata");
        categoryIndex = graphDb.index().forNodes("Categories");
        graphDb.toString();
        registerShutdownHook( graphDb );
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
    public Transaction startTransaction() {
        tx = graphDb.beginTx();
        return tx;
    }
    
    @Override
    public EmbeddedGraphDatabase getConnectionHandler(){
        return (EmbeddedGraphDatabase)graphDb;
    }
    
   
    private static void registerShutdownHook( final GraphDatabaseService graphDb)
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed)
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

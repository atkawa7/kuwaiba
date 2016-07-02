/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.services.persistence.impl.neo4j;

import java.io.File;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.persistence.exceptions.ConnectionException;
import org.kuwaiba.apis.persistence.ConnectionManager;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 * ConnectionManager reference implementation
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ConnectionManagerImpl implements ConnectionManager <GraphDatabaseService>{

    /**
     * Default db path
     */
    private static final String DEFAULT_DB_PATH = "/data/db/kuwaiba.db";
    /**
     * Database path
     */
    private Properties configuration;
    /**
     * Neo4J Database handler
     */
    private GraphDatabaseService graphDb;
    /**
     * Neo4J Transaction handler
     */
    private Transaction tx;
    

    public ConnectionManagerImpl() {
        configuration = new Properties();
    }

    @Override
    public void closeConnection() {
        if (graphDb != null)
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
        try {
            String dbPathString = configuration.getProperty("dbPath", DEFAULT_DB_PATH);
            
            File dbFile = new File(dbPathString);
            if (!dbFile.exists() || !dbFile.canWrite())
                throw new Exception(String.format("Path %s does not exist or is not writeable", dbFile.getAbsolutePath()));
            graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
        }catch(Exception e){
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
    public GraphDatabaseService getConnectionHandler(){
        return graphDb;
    }

    @Override
    public void shutDown() {
        System.out.println( "[KUWAIBA] Shutting down database..." );
        graphDb.shutdown();
        System.out.println( "[KUWAIBA] Database shut down" );
    }
    
    @Override
    public String getConnectionDetails(){
        return graphDb.toString();
    }

    @Override
    public void setConfiguration(Properties properties) {
        this.configuration = properties;
    }
}

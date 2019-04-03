/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.tools.dbmigration;

import java.io.File;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Upgrader {
    private static Upgrader instance;
    
    private Upgrader() {
    }
    
    public static Upgrader getInstance() {
        return instance == null ? instance = new Upgrader() : instance;
    }
    
    public boolean upgrade(File storeDir) throws Exception {
        boolean allowUpgrade = false;
        GraphDatabaseService graphDb = null;
        
        try {
            graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);
        } catch(Exception ex) {
            allowUpgrade = true;
        }
        System.out.println("Upgrading database...");
        
        if (allowUpgrade) {
            GraphDatabaseBuilder gdb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(storeDir);
            gdb.setConfig(GraphDatabaseSettings.allow_upgrade, "true");
            
            graphDb = gdb.newGraphDatabase();
            graphDb.shutdown();
            
            gdb.setConfig(GraphDatabaseSettings.allow_upgrade, "false");
            graphDb = gdb.newGraphDatabase();
            graphDb.shutdown();
            
            graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);
                        
            try (Transaction tx = graphDb.beginTx()) {
                Result result = graphDb.execute("MATCH (node) RETURN node;");
                
                if (!result.hasNext()) {
                    graphDb.shutdown();
                    throw new Exception("Upgrading database failed");
                }
                tx.success();
            }
            graphDb.shutdown();
        }
        return allowUpgrade;        
    }
}

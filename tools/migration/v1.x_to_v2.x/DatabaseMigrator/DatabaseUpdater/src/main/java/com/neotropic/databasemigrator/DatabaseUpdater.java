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
package com.neotropic.databasemigrator;

import java.io.File;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 * Updates the database removing the empty legacy index to prepare the upgrade 
 * from 2.3.11 to 3.3.3 Neo4j version
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DatabaseUpdater {
    private static final String [] INDEXES = new String [] {
        Constants.INDEX_OBJECTS, Constants.INDEX_USERS, Constants.INDEX_GROUPS, Constants.INDEX_QUERIES, 
        Constants.INDEX_LIST_TYPE_ITEMS, Constants.INDEX_GENERAL_VIEWS, Constants.INDEX_POOLS, 
        Constants.INDEX_TASKS, Constants.INDEX_SYNCGROUPS, Constants.INDEX_REPORTS,
        Constants.INDEX_CLASS, Constants.INDEX_SPECIAL_NODES, Constants.INDEX_BUSINESS_RULES};
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Set the parameter dbPath");
            return;
        }
        File storDir = new File(args[0]);
        
        System.out.println("Starting database update...");
        
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storDir); 
        
        for (String index : INDEXES) {
            deleteEmptyIndex(index, graphDb);
        }
        
        try ( Transaction tx = graphDb.beginTx() ) {
            Result result = graphDb.execute("MATCH (n) RETURN n;");

            if (result.hasNext())
                System.out.println("...Database updated successfully");
            
            tx.success();
        }
        graphDb.shutdown();
    }
    
    private static void deleteEmptyIndex(String index, GraphDatabaseService graphDb) {
        try(Transaction tx = graphDb.beginTx()) {
            Result result = graphDb.execute("START node=node:" + index + "('*:*') MATCH node RETURN node;");
                        
            if (!result.hasNext())
                graphDb.index().forNodes(index).delete();
                        
            tx.success();
        }
    }
        
}

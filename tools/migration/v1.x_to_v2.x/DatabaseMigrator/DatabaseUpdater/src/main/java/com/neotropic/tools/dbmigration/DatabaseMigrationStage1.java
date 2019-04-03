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
import java.util.Calendar;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 * Migrates the Kuwaiba 1.x database so it can be used with server version 2.x by 
 * removing the legacy indexes and replacing them with label-based indexes. The empty indexes are removed,
 * since there's a bug in the migration script provided by Neo4J. Note that
 * Kuwaiba v1.x used Neo4J 2.3.x, while version 2.x use Neo4J 3.3.x libraries
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DatabaseMigrationStage1 {
    private static final String [] INDEXES = new String [] {
        Constants.INDEX_OBJECTS, Constants.INDEX_USERS, Constants.INDEX_GROUPS, Constants.INDEX_QUERIES, 
        Constants.INDEX_LIST_TYPE_ITEMS, Constants.INDEX_GENERAL_VIEWS, Constants.INDEX_POOLS, 
        Constants.INDEX_TASKS, Constants.INDEX_SYNCGROUPS, Constants.INDEX_REPORTS,
        Constants.INDEX_CLASS, Constants.INDEX_SPECIAL_NODES, Constants.INDEX_BUSINESS_RULES};
    
    /**
     * Application entry point
     * @param args The list of command line arguments, currently only one is needed: dbPath. If not specified, 
     * /data/db/kuwaiba.db will be used.
     */
    public static void main(String[] args) {
        String dbPath;
        
        if (args.length == 0)
            dbPath = "/data/db/kuwaiba.db";
        else {
            if (args.length != 1) {
                System.out.println("Wrong parameter set. Usage: DatabaseMigrationStage1 <dbPath>");
                return;
            } else
                dbPath = args[0];
        }
        
        File dbPathReference = new File(dbPath);
        
        if (!dbPathReference.exists()) {
            System.out.println(String.format("The specified dbPath (%s) does not exist", dbPath));
            return;
        }
        
        System.out.println(String.format("[%s] Starting database upgrade stage 1...", Calendar.getInstance().getTime()));
        GraphDatabaseFactory gf = new GraphDatabaseFactory();
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPathReference); 
        
        for (String index : INDEXES) 
            //Loop through the existing indexes. If the index is empty, delete it. This is necessary, since there's a 
            //bug in the migration script provided by Neo4J, and it fails is it finds an empty legacy indexes
            deleteEmptyIndex(index, graphDb);
        
        
        try ( Transaction tx = graphDb.beginTx() ) {
            Result result = graphDb.execute("MATCH (n) RETURN n;");

            if (result.hasNext())
                System.out.println(String.format("[%s] Database upgrade stage 1 ended successfully...", Calendar.getInstance().getTime()));
            
        }
        graphDb.shutdown();
    }
    
    private static void deleteEmptyIndex(String index, GraphDatabaseService graphDb) {
        try(Transaction tx = graphDb.beginTx()) {
            Result result = graphDb.execute("START node=node:" + index + "('*:*') MATCH node RETURN node;");
                        
            if (!result.hasNext())
                graphDb.index().forNodes(index).delete();
            
            System.out.println(String.format("Deleted empty legacy index %s", index));
            
            tx.success();
        } catch (Exception ex) {
            System.out.println(String.format("An unexpected error was found: %s", ex.getMessage()));
        }
    }
        
}

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
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class IndexUpgrader {
    public static final String PROPERTY_NAME = "name"; //NOI18N
    private static IndexUpgrader instance;
    
    private IndexUpgrader() {
    }
    
    public static IndexUpgrader getInstance() {
        return instance == null ? instance = new IndexUpgrader() : instance;
    }
    
    public boolean upgrade(File storDir) {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storDir);
        
        createIndex("users", PROPERTY_NAME, graphDb);
        createIndex("classes", PROPERTY_NAME, graphDb);
        createIndex("groups", PROPERTY_NAME, graphDb);
        createIndex("specialNodes", PROPERTY_NAME, graphDb);
        
        graphDb.shutdown();
        return true;
    }
    
    public void createIndex(String labelName, String propertyKey, GraphDatabaseService graphDb) {
        IndexDefinition indexDefinition;
        
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();
            
            indexDefinition = schema.indexFor(Label.label(labelName)).on(propertyKey).create();
            
            tx.success();
        }
        System.out.println(String.format( "Creating index on %s(%s) ...", labelName, propertyKey));
        
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();
            
            while (true) {
                float completedPercentage = schema.getIndexPopulationProgress(indexDefinition).getCompletedPercentage();
                
                if (completedPercentage == 100)
                    break;
            }
            tx.success();
        }
    }
    
}

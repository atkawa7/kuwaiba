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
import java.util.HashMap;
import java.util.UUID;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class LabelUpgrader {
    private final HashMap<String, String> labelNames = new HashMap();
    private static LabelUpgrader instance;
    
    private LabelUpgrader() {
        labelNames.put("users", "users");
        labelNames.put("objects", "inventory_objects");
        labelNames.put("classes", "classes");
        labelNames.put("groups", "groups");
        labelNames.put("listTypeItems", "listTypeItems");
        labelNames.put("pools", "pools");
        labelNames.put("specialNodes", "specialNodes");
        labelNames.put("reports", "reports");
        labelNames.put("queries", "queries");
        labelNames.put("tasks", "tasks");
        labelNames.put("businessRules", "businessRules");
        labelNames.put("generalViews", "generalViews");
        labelNames.put("syncGroups", "syncGroups");
    }
    
    public static LabelUpgrader getInstance() {
        return instance == null ? instance = new LabelUpgrader() : instance;
    }
    
    public boolean createLabels(File storDir) {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storDir);
        
        for (String indexName : labelNames.keySet()) {
            createLabel(indexName, labelNames.get(indexName), graphDb);
        }
        graphDb.shutdown();
        return true;                
    }
    
    public boolean deleteIndexes(File storDir) {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storDir);
        
        for (String indexName : labelNames.keySet())
            deleteIndex(indexName, graphDb);
        
        graphDb.shutdown();
        return true;                
    }
    
    private void createLabel(String indexName, String labelName, GraphDatabaseService graphDb) {
        try (Transaction tx = graphDb.beginTx()) {
            Index<Node> nodeIndex = graphDb.index().forNodes(indexName);
            
            IndexHits<Node> nodes = nodeIndex.query("*", "*");
            
            while (nodes.hasNext()) {
                Node node = nodes.next();
                node.addLabel(Label.label(labelName));
            }
            System.out.println(String.format("Created label %s", labelName));
            tx.success();
        }
    }
    
    private void deleteIndex(String indexName, GraphDatabaseService graphDb) {
        try (Transaction tx = graphDb.beginTx()) {
            Index<Node> nodeIndex = graphDb.index().forNodes(indexName);
            if (nodeIndex != null)
                nodeIndex.delete();
            System.out.println(String.format("Deleted unused legacy index %s", indexName));
            tx.success();
        }
    }
    
    public void deleteUnusedLabels(File storDir) {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storDir);
        
        try (Transaction tx = graphDb.beginTx()) {
            
            ResourceIterable<Label> labels = graphDb.getAllLabels();

            ResourceIterator<Label> labelsIterator = labels.iterator();

            while (labelsIterator.hasNext()) {
                Label label = labelsIterator.next();

                if (label.name().contains("org.kuwaiba.entity.") || 
                    label.name().equals("class") || 
                    label.name().equals("listType")) {
                                                            
                    deleteLabel(label, graphDb);

                    System.out.println(String.format("Deleted unused label %s", label.name()));
                }
            }
            tx.success();
        }
        graphDb.shutdown();
    }
    
    private void deleteLabel(Label label, GraphDatabaseService graphDb) {
        try (Transaction tx = graphDb.beginTx()) {
            
            ResourceIterator<Node> nodes = graphDb.findNodes(label);
            
            while (nodes.hasNext()) {
                Node node = nodes.next();
                node.removeLabel(label);
            }
            tx.success();
        }
    }
    
    public void replaceLabel(File storeDir, String oldLabel, String newLabel) {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);
        
        Label lblOld = Label.label(oldLabel);
        Label lblNew = Label.label(newLabel);
        
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterator<Node> nodes = graphDb.findNodes(lblOld);
            
            while (nodes.hasNext()) {
                Node node = nodes.next();
                node.addLabel(lblNew);
                node.removeLabel(lblOld);
                
            }
            tx.success();
        }
        System.out.println(String.format("Replaced node label %s with %s", oldLabel, newLabel));
        graphDb.shutdown();
    }
    
    private static final String INVENTORY_OBJECTS = "inventoryObjects";
    private static final String LIST_TYPE_ITEMS = "listTypeItems";
    private static final String POOLS = "pools";
    private static final String PROPERTY_UUID = "_uuid";
    
    public void setUUIDAttributeToInventoryObjects(File storeDir) {
        System.out.println(String.format("Start: Set UUID Attribute to Inventory Objects"));
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);
        Label labelInventoryObjects = Label.label(INVENTORY_OBJECTS);
        
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterator<Node> nodes = graphDb.findNodes(labelInventoryObjects);
            while (nodes.hasNext()) {
                Node node = nodes.next();
                if (!node.hasProperty(PROPERTY_UUID)) {
                    String uuid = UUID.randomUUID().toString();
                    node.setProperty(PROPERTY_UUID, uuid);
                }                
            }
            tx.success();
        }
        graphDb.shutdown();
        System.out.println(String.format("End: Set UUID Attribute to Inventory Objects"));
    }
    
    public void setUUIDAttributeToListTypeItems(File storeDir) {
        System.out.println(String.format("Start: Set UUID Attribute to List Type Items"));
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);
        Label labelListTypeItems = Label.label(LIST_TYPE_ITEMS);
        
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterator<Node> nodes = graphDb.findNodes(labelListTypeItems);
            while (nodes.hasNext()) {
                Node node = nodes.next();
                if (!node.hasProperty(PROPERTY_UUID)) {
                    String uuid = UUID.randomUUID().toString();
                    node.setProperty(PROPERTY_UUID, uuid);
                }                
            }
            tx.success();
        }
        graphDb.shutdown();
        System.out.println(String.format("End: Set UUID Attribute to List Type Items"));
    }
    
    public void setUUIDAttributeToPools(File storeDir) {
        System.out.println(String.format("Start: Set UUID Attribute to Pools"));
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);
        Label labelPools = Label.label(POOLS);
        
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterator<Node> nodes = graphDb.findNodes(labelPools);
            while (nodes.hasNext()) {
                Node node = nodes.next();
                if (!node.hasProperty(PROPERTY_UUID)) {
                    String uuid = UUID.randomUUID().toString();
                    node.setProperty(PROPERTY_UUID, uuid);
                }                
            }
            tx.success();
        }
        graphDb.shutdown();
        System.out.println(String.format("End: Set UUID Attribute to Pools"));
    }
}

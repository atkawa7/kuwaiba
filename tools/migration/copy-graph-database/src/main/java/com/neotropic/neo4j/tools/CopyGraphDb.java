/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.neo4j.tools;

import java.util.List;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import java.util.ArrayList;
import org.neo4j.graphdb.Node;
import java.util.HashMap;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import java.io.File;

/**
 * Copies a Neo4j Graph Database
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CopyGraphDb {
    /**
     * Source Graph Database.
     */
    private final File sourceStoreDir;
    /**
     * Target Graph Database.
     */
    private final File targetStoreDir;
    
    private int nodesSize = 0;
    private int relationshipsSize = 0;
    /**
     * @param sourceStoreDir Source Graph Database.
     * @param targetStoreDir Target Graph Database.
     */
    public CopyGraphDb(final File sourceStoreDir, final File targetStoreDir) {
        this.sourceStoreDir = sourceStoreDir;
        this.targetStoreDir = targetStoreDir;
    }
    /**
     * Copies a Neo4j Graph Database.
     */
    public void copyDatabase() {        
        final GraphDatabaseService sourceGraphDb = new GraphDatabaseFactory().newEmbeddedDatabase(this.sourceStoreDir);
        try (final Transaction sourceTx = sourceGraphDb.beginTx()) {
            System.out.println("Getting nodes from source store dir...");
            final ResourceIterable<Node> nodes = sourceGraphDb.getAllNodes();
            System.out.println("Getting relationships from source store dir...");
            final ResourceIterable<Relationship> relationships = sourceGraphDb.getAllRelationships();
            
            final GraphDatabaseService targetGraphDb = new GraphDatabaseFactory().newEmbeddedDatabase(this.targetStoreDir);
            try (final Transaction targetTx = targetGraphDb.beginTx()) {
                System.out.println("Copying nodes...");
                final HashMap<Long, Node> newNodes = new HashMap();
                nodes.forEach(node -> {
                    List<Label> labels = new ArrayList();
                    node.getLabels().forEach(label -> labels.add(label));
                    
                    Node newNode = targetGraphDb.createNode(labels.toArray(new Label[0]));
                    node.getAllProperties().forEach((key, value) -> newNode.setProperty(key, value));
                    
                    newNodes.put(node.getId(), newNode);
                    nodesSize++;
                });
                System.out.println("Copying relationships...");
                relationships.forEach(relationship -> {
                    Node startNode = newNodes.get(relationship.getStartNodeId());
                    Node endNode = newNodes.get(relationship.getEndNodeId());
                    
                    if (startNode != null && endNode != null) {
                        Relationship newRelationship = startNode.createRelationshipTo(endNode, relationship.getType());
                        relationship.getAllProperties().forEach((key, value) -> newRelationship.setProperty(key, value));
                        relationshipsSize++;
                    } else {
                        unableToLoadRelatioship(startNode, relationship, true);
                        unableToLoadRelatioship(endNode, relationship, false);
                    }
                });                
                targetTx.success();
                System.out.println(String.format("Copied %s nodes", nodesSize));
                System.out.println(String.format("Copied %s relationships", relationshipsSize));
            }
            targetGraphDb.shutdown();
        }
        sourceGraphDb.shutdown();
    }
    
    private void unableToLoadRelatioship(Node node, Relationship relationship, boolean startNode) {
        if (node == null) {
            System.out.println(String.format("Unable to load NODE with id %s", startNode ? relationship.getStartNodeId() : relationship.getEndNodeId()));
            System.out.println(String.format("Unable to load RELATIONSHIP (%s)-[:%s]->(%s)", relationship.getStartNodeId(), relationship.getType(), relationship.getEndNodeId()));
        }
    }
}
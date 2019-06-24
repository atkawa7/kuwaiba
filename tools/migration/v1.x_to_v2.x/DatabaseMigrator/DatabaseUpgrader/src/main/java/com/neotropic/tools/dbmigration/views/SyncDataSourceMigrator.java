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

package com.neotropic.tools.dbmigration.views;

import java.io.File;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 * Migrates the synchronization data sources, from now on there will be only one 
 * syncDatasource for a device, also the property id saved in the syncDatasource
 * would be updated from long into uuid
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SyncDataSourceMigrator {
   
    public static void migrate(File dbPathReference){
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPathReference);
        System.out.println(">>> Migrating synchronization datasources ...");
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterator<Node> syncGroupsNodes = graphDb.findNodes(Label.label("syncGroups"));
            //first we get all the syn datagroups
            while (syncGroupsNodes.hasNext()) {
                Node syncGroupNode = syncGroupsNodes.next();
                //then we need every syncdatasource in the group
                for(Relationship rel : syncGroupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_GROUP)){
                    Node dsConfigNode = rel.getStartNode();
                    //The node is malformed it should be deleted
                    if(dsConfigNode != null && (!dsConfigNode.hasProperty("name") || !dsConfigNode.hasProperty("deviceClass") || !dsConfigNode.hasProperty("deviceId")))
                        dsConfigNode.delete();
                    else if(dsConfigNode != null){
                        String propertyDeviceId = (String)dsConfigNode.getProperty("deviceId");
                        if(!dsConfigNode.hasRelationship(RelTypes.HAS_CONFIGURATION)){ //node exists but is note related
                            if(propertyDeviceId != null &&  isNumeric(propertyDeviceId)){
                                Node inventoryObjNode = graphDb.getNodeById(Long.valueOf(propertyDeviceId));
                                if(inventoryObjNode != null && !inventoryObjNode.hasRelationship(RelTypes.HAS_CONFIGURATION)){
                                    dsConfigNode.setProperty("deviceId", (String)inventoryObjNode.getProperty("_uuid"));
                                    dsConfigNode.createRelationshipTo(inventoryObjNode, RelTypes.HAS_CONFIGURATION);
                                    System.out.println(String.format("Synchronization Data Source Configuration %s related to inventory object %s",
                                                    dsConfigNode.getProperty("name"), inventoryObjNode.getProperty("name")));
                                } else{//we remove the ds config from all the groups, deleting its belongs groups relationships
                                    for(Relationship rel_ : dsConfigNode.getRelationships(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP))
                                        rel_.delete();

                                    System.out.println(String.format("The config %s was delete", dsConfigNode.getProperty("name")));
                                    dsConfigNode.delete();
                                }
                            }
                        }//end if doesn't have devide relationship
                        else if(isNumeric(propertyDeviceId)){ //we need to update the deviceId from long to uuid
                            Node inventoryObjNode = graphDb.getNodeById(Long.valueOf(propertyDeviceId));
                            dsConfigNode.setProperty("deviceId", (String)inventoryObjNode.getProperty("_uuid"));
                            System.out.println(String.format("Property deviceId updated from long %s to uuid in dsConfig %s",
						propertyDeviceId, dsConfigNode.getProperty("deviceId"), dsConfigNode.getProperty("name")));
			}
                    }
                }
            }
            System.out.println(">>> Synchronization datsources migration finished");
            tx.success();
        }
        graphDb.shutdown();
    }
    
    
    private static boolean isNumeric(String str) { 
        try {  
          Double.parseDouble(str);  
          return true;
        } catch(NumberFormatException e){  
          return false;  
        }  
    }

    
    private static class RelTypes{

        public static final RelationshipType BELONGS_TO_GROUP = new RelationshipType() {
            @Override
            public String name() {
                return "BELONGS_TO_GROUP";
            }
        };
        
        public static final RelationshipType HAS_CONFIGURATION = new RelationshipType() {
            @Override
            public String name() {
                return "HAS_CONFIGURATION";
            }
        };
    
    }
}


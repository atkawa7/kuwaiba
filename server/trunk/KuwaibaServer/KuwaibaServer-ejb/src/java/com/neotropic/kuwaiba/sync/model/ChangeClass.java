/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neotropic.kuwaiba.sync.model;

import java.io.File;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 * Changes the class of the given objects to the target class
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ChangeClass {
    public static void main (String args[]) {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File("/data/db/kuwaiba.db"));
        try (Transaction tx = graphDb.beginTx()) {
            Node targetClassNode = graphDb.index().forNodes("classes").get("name", "MPLSRouter").getSingle();
            if (targetClassNode == null)
                System.out.println("MPLSSwitch class not found");
            else {
                long[] ids = new long[] { 9361,
                                        9289,
                                        9460,
                                        9436,
                                        9346,
                                        9308,
                                        3398,
                                        10824,
                                        9446,
                                        21888,
                                        18287,
                                        21995,
                                        22007,
                                        21785,
                                        8639,
                                        8596,
                                         };
                
                for (long id : ids) {
                    Node objectNode = graphDb.index().forNodes("objects").get("id", id).getSingle();
                    Relationship rel = objectNode.getRelationships(RelTypes.INSTANCE_OF, Direction.OUTGOING).iterator().next();
                    System.out.println("Changing class for " + objectNode.getProperty("name") + " (" + objectNode.getId() + ")" + " from " + rel.getEndNode().getProperty("name") + " to MPLSRouter");
                    rel.delete();
                    objectNode.createRelationshipTo(targetClassNode, RelTypes.INSTANCE_OF);
                }
                //tx.failure();
                tx.success();
            }
        }
        graphDb.shutdown();
    }
    
    public enum RelTypes implements RelationshipType {
    
    ROOT, //Relationship to the root node
    EXTENDS, //Inheritance
    HAS_ATTRIBUTE, //A class has attributes
    IMPLEMENTS, //A class implements an interface
    BELONGS_TO_CATEGORY, //A class belongs to a category
    INSTANCE_OF, //An object is instance of a given class
    CHILD_OF, //An object is child of a given object
    RELATED_TO, //Represents the many-to-one, many-to-may relationships (like type, responsible, etc)
    BELONGS_TO_GROUP, //Used to associate a user to a group (group of user)
    OWNS_QUERY, //Used to asociate a user to a query
    POSSIBLE_CHILD, //Used to build the containment hierarchy
    POSSIBLE_SPECIAL_CHILD, //Used to build the containment hierarchy for special models
    HAS_VIEW, //Used to link an object to a particular view
    HAS_HISTORY_ENTRY, //Used to link an object to a particular historic entry
    RELATED_TO_SPECIAL, //Used to implement relationships for domain specific models
    CHILD_OF_SPECIAL, //Used to implement the parent-child relationship for domain specific models
    HAS_PRIVILEGE, //Used to associate the group/user nodes with methods group node
    PERFORMED_BY, //Connects a log entry node with a user
    GROUP, //Used to associate the groups nodes with group root node
    PRIVILEGE, //Used to associate the privilege nodes with privilege root node
    SUBSCRIBED_TO, //Used to relate a user to a task, so it can be notified about the result of its execution
    HAS_TEMPLATE, //Used to related a class to a template (which is basically a normal object)
    HAS_REPORT, //Relates a class or the dummy root (depending on if it's a class or inventory level report) to a report
    INSTANCE_OF_SPECIAL, //Used to relate a class with an instance that makes part of a template. These instances are not indexed and can not be searched, that's why they need a special relationship
    HAS_BOOKMARK, //Used to relate a bookmark with an user
    IS_BOOKMARK_ITEM_IN //Used to relate an object with a bookmark
}
}

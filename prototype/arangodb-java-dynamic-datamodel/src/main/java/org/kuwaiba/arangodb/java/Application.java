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
package org.kuwaiba.arangodb.java;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.entity.EdgeDefinition;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Sample application that shows how to create a graph composed by nodes in the
 * same collection, but not necessarily with the same properties
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class Application {

    protected static final String TEST_DB = "test_db";
    protected static final String GRAPH_NAME = "graph";
    protected static final String EDGE_COLLECTION_NAME = "edges";
    protected static final String VERTEX_COLLECTION_NAME = "nodes";

    public static void main(String[] args) {

        ArangoDB arangoDB = new ArangoDB.Builder().build();

        try {
            //Try to delete any old database with the same name.
            arangoDB.db(TEST_DB).drop();
        } catch (final ArangoDBException e) {
            //It doesn't matter of the db exists or not
        }

        arangoDB.createDatabase(TEST_DB);
        ArangoDatabase db = arangoDB.db(TEST_DB);

        //The nodes that we are going to connect are in the same collection. The collections are created automatically
        Collection<EdgeDefinition> edgeDefinitions = new ArrayList<>();
        EdgeDefinition edgeDefinition = new EdgeDefinition().collection(EDGE_COLLECTION_NAME)
                .from(VERTEX_COLLECTION_NAME).to(VERTEX_COLLECTION_NAME);
        edgeDefinitions.add(edgeDefinition);
        try {
            db.createGraph(GRAPH_NAME, edgeDefinitions, null);

            //First, we create and add the nodes. These nodes are going to be
            //plain BaseDocument, so we can add properties dynamically as needed.
            BaseDocument a = new BaseDocument("obj1");
            a.addAttribute("name", "Hello 1");
            BaseDocument b = new BaseDocument("obj2");
            b.addAttribute("name", "Hello 2");

            db.graph(GRAPH_NAME).vertexCollection(VERTEX_COLLECTION_NAME).insertVertex(a);
            db.graph(GRAPH_NAME).vertexCollection(VERTEX_COLLECTION_NAME).insertVertex(b);

            //Now the edge. Note that the constructor of the edge requires the ids (NOT the keys) of the
            //nodes to be connected. This is important, because the javadocs refer to the second and third 
            //parameters as keys, when it's not the case.
            BaseEdgeDocument c = new BaseEdgeDocument("edg1", a.getId(), b.getId());
            c.addAttribute("name", "relationship");

            db.graph(GRAPH_NAME).edgeCollection(EDGE_COLLECTION_NAME).insertEdge(c);

            //Now we modify the attributes of one of the nodes, and commiit the changes. 
            //Note tha here, it's the node's key what it's required to find it, not its id.
            b.addAttribute("description", "This is an extra attribute");
            db.graph(GRAPH_NAME).vertexCollection(VERTEX_COLLECTION_NAME).updateVertex(b.getKey(), b);

            //Don't forget to shutdown the session.
            arangoDB.shutdown();
            System.out.println("Done and closed.");
        } catch (final ArangoDBException ex) {
            System.out.println("Something nasty happened! -> " + ex.getMessage());
        }
    }
}

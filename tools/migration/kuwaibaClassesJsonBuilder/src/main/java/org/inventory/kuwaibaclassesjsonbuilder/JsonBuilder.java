/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.inventory.kuwaibaclassesjsonbuilder;

import com.google.gson.JsonObject;
import java.io.File;
import java.util.Iterator;
import javax.xml.bind.DatatypeConverter;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class JsonBuilder {
    private static JsonBuilder instance;
    final String PROPERTY_NAME = "name"; //NOI18N
    final String PROPERTY_TYPE = "type"; //NOI18N
    final String ATTRIBUTES = "_attributes"; //NOI18N
    final String SUPER_CLASS = "_superClass"; //NOI18N
    final RelationshipType rltHasAttribute = RelationshipType.withName("HAS_ATTRIBUTE"); //NOI18N
    final RelationshipType rltExtends = RelationshipType.withName("EXTENDS"); //NOI18N
    
    private JsonBuilder() {
    }
    
    public static JsonBuilder getInstance() {
        return instance != null ? instance : (instance = new JsonBuilder());
    }
    
    private void addProperty(JsonObject jsonObject, String propertyKey, Object propertyValue) {
        if (propertyValue instanceof Boolean)
            jsonObject.addProperty(propertyKey, (Boolean) propertyValue);
        else if (propertyValue instanceof Character)
            jsonObject.addProperty(propertyKey, (Character) propertyValue);
        else if (propertyValue instanceof Number)
            jsonObject.addProperty(propertyKey, (Number) propertyValue);
        else if (propertyValue instanceof String)
            jsonObject.addProperty(propertyKey, (String) propertyValue);
        else if (propertyValue instanceof byte[])
            jsonObject.addProperty(propertyKey, DatatypeConverter.printBase64Binary((byte[]) propertyValue));
        else
            jsonObject.add(propertyKey, null);
    }
    
    private void build(GraphDatabaseService graphDb, JsonObject jsonObjectClasses, String classNames) {
        if (jsonObjectClasses.has(classNames))
            return;
        try (Transaction tx = graphDb.beginTx()) {
            String query = "" + 
                "MATCH (class:classes)\n" +
                "WHERE class.name IN %s\n" +
                "RETURN class as classes\n" +
                "UNION\n" +
                "MATCH (class:classes)<-[:EXTENDS*]-(subclass:classes)\n" +
                "WHERE class.name IN %s\n" +
                "RETURN subclass as classes\n" +
                "UNION\n" +
                "MATCH (class:classes)-[:EXTENDS*]->(superclass:classes)\n" +
                "WHERE class.name IN %s\n" +
                "RETURN superclass as classes";
            query = String.format(query, classNames, classNames, classNames);
            Result result = graphDb.execute(query);
            Iterator<Node> classNodes = result.columnAs("classes");
            while (classNodes.hasNext()) {
                Node classNode = classNodes.next();
                if (classNode.hasProperty(PROPERTY_NAME) && classNode.getProperty(PROPERTY_NAME) instanceof String) {
                    JsonObject jsonObjectClass = new JsonObject();
                    for (String propertyKey : classNode.getPropertyKeys()) {
                        Object propertyValue = classNode.getProperty(propertyKey);
                        addProperty(jsonObjectClass, propertyKey, propertyValue);
                    }
                    JsonObject jsonObjectAttributes = new JsonObject();
                    if (classNode.hasRelationship(Direction.OUTGOING, rltHasAttribute)) {
                        for (Relationship rlt : classNode.getRelationships(Direction.OUTGOING, rltHasAttribute)) {
                            Node attributeNode = rlt.getEndNode();
                            if (attributeNode.hasProperty(PROPERTY_NAME) && attributeNode.getProperty(PROPERTY_NAME) instanceof String) {
                                JsonObject jsonObjectAttribute = new JsonObject();                                
                                for (String propertyKey : attributeNode.getPropertyKeys()) {
                                    Object propertyValue = attributeNode.getProperty(propertyKey);
                                                                        
                                    if (PROPERTY_TYPE.equals(propertyKey) && propertyValue instanceof String)
                                        build(graphDb, jsonObjectClasses, String.format("['%s']", propertyValue));
                                    
                                    addProperty(jsonObjectAttribute, propertyKey, propertyValue);
                                }
                                jsonObjectAttributes.add((String) attributeNode.getProperty(PROPERTY_NAME), jsonObjectAttribute);
                            }
                        }
                    }
                    if (classNode.hasRelationship(Direction.OUTGOING, rltExtends)) {
                        for (Relationship rlt : classNode.getRelationships(Direction.OUTGOING, rltExtends)) {
                            Node superClassNode = rlt.getEndNode();
                            jsonObjectClass.addProperty(SUPER_CLASS, (String) superClassNode.getProperty(PROPERTY_NAME));
                        }
                    }
                    jsonObjectClass.add(ATTRIBUTES, jsonObjectAttributes);
                    jsonObjectClasses.add((String) classNode.getProperty(PROPERTY_NAME), jsonObjectClass);
                }
            }
            tx.success();
        }
    }
    
    public JsonObject build(File storeDir, String classNames) {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);
        JsonObject jsonObjectClasses = new JsonObject();
        build(graphDb, jsonObjectClasses, classNames);
        graphDb.shutdown();
        return jsonObjectClasses;
    }
}

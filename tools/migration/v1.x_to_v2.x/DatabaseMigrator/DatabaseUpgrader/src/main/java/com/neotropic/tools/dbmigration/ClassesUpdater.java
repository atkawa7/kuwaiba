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
package com.neotropic.tools.dbmigration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.bind.DatatypeConverter;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ClassesUpdater {
    private static ClassesUpdater instance;
    final String PROPERTY_ATTRIBUTES = "_attributes"; //NOI18N
    final String PROPERTY_SUPER_CLASS = "_superClass"; //NOI18N
    final String PROPERTY_NAME = "name"; //NOI18N
    final String PROPERTY_ICON = "icon"; //NOI18N
    final String PROPERTY_CREATION_DATE = "creationDate"; //NOI18N
    final String PROPERTY_SMALL_ICON = "smallIcon"; //NOI18N
    private final Label lblClass = Label.label("classes"); //NOI18N;
    private final Label lblAttributes = Label.label("attributes"); //NOI18N;
    private final RelationshipType rltTypeHasAttribute = RelationshipType.withName("HAS_ATTRIBUTE"); //NOI18N
    private final RelationshipType rltTypeExtends = RelationshipType.withName("EXTENDS"); //NOI18N
    
    private ClassesUpdater() {
    }        
    
    public static ClassesUpdater getInstance() {
        return instance != null ? instance : (instance = new ClassesUpdater());
    }
    public void setLabelAttributes(File storeDir) {
        System.out.println(">>> Start Set Label Attributes");
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);
        try (Transaction tx = graphDb.beginTx()) {
            String query = "" +
                "MATCH (class)-[:HAS_ATTRIBUTE]->(attribute)\n" +
                "WHERE NOT (class)-[:HAS_ATTRIBUTE]->(attribute:attributes)\n" +
                "SET attribute:attributes\n" +
                "RETURN class, attribute;";
            graphDb.execute(query);
            tx.success();
        }
        graphDb.shutdown();
        System.out.println(">>> End Set Label Attributes");
    }    
    public void updateClasses(File storeDir) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("kuwaibaClasses.json");
        String json = getInputStreamAsString(inputStream);
        Gson gson = new Gson();
        JsonObject jsonClasses = gson.fromJson(json, JsonObject.class);
        
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);
        try (Transaction tx = graphDb.beginTx()) {
            for (String className : jsonClasses.keySet())
                createClass(graphDb, className, jsonClasses);
            tx.success();
        }
        graphDb.shutdown();
    }
    private Node getClassNode(GraphDatabaseService graphDb, String className) {
        Node classNode = null;
        try (Transaction tx = graphDb.beginTx()) {
            String query = "MATCH (class:classes) WHERE class.name = $className RETURN class"; //NOI18N
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("className", className); //NOI18N
            Result result = graphDb.execute(query, parameters);
            Iterator<Node> classNodes = result.columnAs("class"); //NOI18N
            while (classNodes.hasNext()) {
                classNode = classNodes.next();
                break;
            }
            tx.success();
        }
        return classNode;
    }    
    private boolean hasClass(GraphDatabaseService graphDb, String className) {
        boolean hasClass = false;
        try (Transaction tx = graphDb.beginTx()) {
            String query = "MATCH (class:classes) WHERE class.name = $className RETURN class"; //NOI18N
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("className", className); //NOI18N
            Result result = graphDb.execute(query, parameters);
            Iterator<Node> classNodes = result.columnAs("class"); //NOI18N
            while (classNodes.hasNext()) {
                hasClass = true;
                break;
            }
            tx.success();
        }
        return hasClass;
    }
    private boolean hasClassAttribute(GraphDatabaseService graphDb, String className, String attributeName) {
        boolean hasClassAttribute = false;
        try (Transaction tx = graphDb.beginTx()) {
            String query = "MATCH (class:classes)-[:HAS_ATTRIBUTE]->(attribute:attributes) WHERE class.name = $className AND attribute.name = $attributeName RETURN attribute"; //NOI18N
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("className", className); //NOI18N
            parameters.put("attributeName", attributeName); //NOI18N
            Result result = graphDb.execute(query, parameters);
            Iterator<Node> classNodes = result.columnAs("attribute"); //NOI18N
            while (classNodes.hasNext()) {
                hasClassAttribute = true;
                break;
            }
            tx.success();
        }
        return hasClassAttribute;
    }
    private void createAttributes(GraphDatabaseService graphDb, Node classNode, JsonObject jsonAttributes) {
        for (String attributeName : jsonAttributes.keySet()) {
            JsonObject jsonAttribute = jsonAttributes.getAsJsonObject(attributeName);
            Node attributeNode = graphDb.createNode(lblAttributes);
            for (String attributePropertyKey : jsonAttribute.keySet()) {
                Object attributePropertyValue = getPropertyValue(attributePropertyKey, jsonAttribute.getAsJsonPrimitive(attributePropertyKey));
                attributeNode.setProperty(attributePropertyKey, attributePropertyValue);
            }
            classNode.createRelationshipTo(attributeNode, rltTypeHasAttribute);
        }                
    }
    private void createClass(GraphDatabaseService graphDb, String className, JsonObject jsonClasses) {
        try (Transaction tx = graphDb.beginTx()) {
            if (!hasClass(graphDb, className)) {
                JsonObject jsonClass = jsonClasses.getAsJsonObject(className);

                Node classNode = graphDb.createNode(lblClass);

                for (String classPropertyKey : jsonClass.keySet()) {
                    if (PROPERTY_ATTRIBUTES.equals(classPropertyKey)) {
                        JsonObject jsonAttributes = jsonClass.getAsJsonObject(classPropertyKey);
                        createAttributes(graphDb, classNode, jsonAttributes);
                    }
                    else if (PROPERTY_SUPER_CLASS.equals(classPropertyKey)) {                            
                        String superClassName = jsonClass.getAsJsonPrimitive(classPropertyKey).getAsString();
                        createClass(graphDb, superClassName, jsonClasses);
                        Node superClassNode = getClassNode(graphDb, superClassName);
                        classNode.createRelationshipTo(superClassNode, rltTypeExtends);
                    }
                    else if (PROPERTY_ICON.equals(classPropertyKey)) {
                        String propertyIcon = jsonClass.getAsJsonPrimitive(classPropertyKey).getAsString();
                        byte[] icon = DatatypeConverter.parseBase64Binary(propertyIcon);
                        classNode.setProperty(classPropertyKey, icon);
                    }
                    else if (PROPERTY_SMALL_ICON.equals(classPropertyKey)) {
                        String propertySmallIcon = jsonClass.getAsJsonPrimitive(classPropertyKey).getAsString();
                        byte[] smallIcon = DatatypeConverter.parseBase64Binary(propertySmallIcon);
                        classNode.setProperty(classPropertyKey, smallIcon);
                    }
                    else {
                        Object classPropertyValue = getPropertyValue(classPropertyKey, jsonClass.getAsJsonPrimitive(classPropertyKey));
                        classNode.setProperty(classPropertyKey, classPropertyValue);
                    }
                }
                System.out.println(String.format("Create class %s", className));
            }
            else {
                Node classNode = getClassNode(graphDb, className);
                
                JsonObject jsonClass = jsonClasses.getAsJsonObject(className);
                
                JsonObject jsonAttributes = jsonClass.getAsJsonObject(PROPERTY_ATTRIBUTES);
                for (String attributeName : jsonAttributes.keySet()) {
                    if (!hasClassAttribute(graphDb, className, attributeName)) {
                        JsonObject jsonAttribute = jsonAttributes.getAsJsonObject(attributeName);
                        Node attributeNode = graphDb.createNode(lblAttributes);
                        for (String attributePropertyKey : jsonAttribute.keySet()) {
                            Object attributePropertyValue = getPropertyValue(attributePropertyKey, jsonAttribute.getAsJsonPrimitive(attributePropertyKey));
                            attributeNode.setProperty(attributePropertyKey, attributePropertyValue);
                        }
                        String a = (String) attributeNode.getProperty("type");
                        classNode.createRelationshipTo(attributeNode, rltTypeHasAttribute);
                        System.out.println(String.format("Create attribute %s:%s in class %s", attributeName, a, className));
                    }
                }
            }            
            tx.success();
        }
    }
    private Object getPropertyValue(String property, JsonPrimitive jsonPrimitive) {
        if (jsonPrimitive.isBoolean())
            return jsonPrimitive.getAsBoolean();
        else if (jsonPrimitive.isString())
            return jsonPrimitive.getAsString();
        else if (jsonPrimitive.isNumber())
            if (PROPERTY_CREATION_DATE.equals(property))
                return jsonPrimitive.getAsLong();
            else
                return jsonPrimitive.getAsInt();
        else
            return null;
    }
    
    private String getInputStreamAsString(InputStream inputStream) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String string;
        try {
            while ((string = bufferedReader.readLine()) != null)
                stringBuilder.append(string);
            bufferedReader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return stringBuilder.toString();
    }
}

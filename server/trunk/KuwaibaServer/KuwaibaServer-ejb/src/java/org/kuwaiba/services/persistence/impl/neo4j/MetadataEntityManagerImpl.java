/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kuwaiba.services.persistence.impl.neo4j;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.ConnectionManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.services.persistence.cache.CacheManager;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.services.persistence.util.Util;
import static org.kuwaiba.services.persistence.util.Util.createClassMetadataFromNode;
import org.kuwaiba.util.ChangeDescriptor;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.impl.traversal.MonoDirectionalTraversalDescription;

/**
 * MetadataEntityManager implementation for neo4j
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class MetadataEntityManagerImpl implements MetadataEntityManager {
    /**
     * Reference to the db handle
     */
    private GraphDatabaseService graphDb;
    /**
     * Class index
     */
    private Index<Node> classIndex;
    /**
     * Reference to the CacheManager
     */
    private CacheManager cm;
    /**
     * This hash contains the display name of the special relationship used in the different models
     */
    private HashMap<String, String> relationshipDisplayNames;

    private MetadataEntityManagerImpl() {
        cm = CacheManager.getInstance();
    }

    /**
     * Constructor
     * Get the a database connection and indexes from the connection manager.
     * @param cmn A reference to the db connection manager
     */
    public MetadataEntityManagerImpl(ConnectionManager cmn) {
        this();
        graphDb = (GraphDatabaseService) cmn.getConnectionHandler();
        this.relationshipDisplayNames = new HashMap<>();
        try(Transaction tx = graphDb.beginTx()) {
            classIndex = graphDb.index().forNodes(Constants.INDEX_CLASS);
            buildClassCache();
        }catch(Exception ex) {
            System.out.println(String.format("[KUWAIBA] [%s] An error was found while creating the MEM instance: %s", 
                    Calendar.getInstance().getTime(), ex.getMessage()));
        }
        
    }

    @Override
    public long createClass(ClassMetadata classDefinition) throws MetadataObjectNotFoundException, DatabaseException, InvalidArgumentException {
        long id;
        if (classDefinition.getName() == null)
            throw new InvalidArgumentException("Class name can not be null");
            
        if (!classDefinition.getName().matches("^[a-zA-Z0-9_-]*$"))
            throw new InvalidArgumentException(String.format("Class %s contains invalid characters", classDefinition.getName()));
        
        if(classDefinition.getName().isEmpty())
                    throw new InvalidArgumentException("Class name can not be an empty string");
        
        try (Transaction tx = graphDb.beginTx()) {
            if (classIndex.get(Constants.PROPERTY_NAME, classDefinition.getName()).getSingle() != null)
                throw new InvalidArgumentException(String.format("Class %s already exists", classDefinition.getName()));
            
            Label label = DynamicLabel.label(Constants.LABEL_CLASS);
            Label categoryLabel = DynamicLabel.label(classDefinition.getCategory() == null ? "org.kuwaiba.entity.undefined" : classDefinition.getCategory());
            Node classNode = graphDb.createNode(label, categoryLabel);

            classNode.setProperty(Constants.PROPERTY_NAME, classDefinition.getName());
            classNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, classDefinition.getDisplayName() == null ? "" : classDefinition.getDisplayName());
            classNode.setProperty(Constants.PROPERTY_CUSTOM, classDefinition.isCustom() == null ? true : classDefinition.isCustom());
            classNode.setProperty(Constants.PROPERTY_COUNTABLE, classDefinition.isCountable() == null ? true : classDefinition.isCountable());
            classNode.setProperty(Constants.PROPERTY_COLOR, classDefinition.getColor());
            classNode.setProperty(Constants.PROPERTY_DESCRIPTION, classDefinition.getDescription() == null ? "" : classDefinition.getDescription());
            classNode.setProperty(Constants.PROPERTY_ABSTRACT, classDefinition.isAbstract() == null ? false : classDefinition.isAbstract());
            classNode.setProperty(Constants.PROPERTY_ICON, classDefinition.getIcon() == null ? new byte[0] : classDefinition.getIcon());
            classNode.setProperty(Constants.PROPERTY_SMALL_ICON, classDefinition.getSmallIcon() ==  null ? new byte[0] : classDefinition.getSmallIcon());
            classNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            classNode.setProperty(Constants.PROPERTY_IN_DESIGN, classDefinition.isInDesign() == null ? false : classDefinition.isInDesign());

            id = classNode.getId();
            classIndex.putIfAbsent(classNode, Constants.PROPERTY_NAME, classDefinition.getName());
            classIndex.putIfAbsent(classNode, Constants.PROPERTY_ID, classNode.getId());
            
            //Here we add the attributes
            if (classDefinition.getAttributes() != null) {
                for (AttributeMetadata attributeMetadata : classDefinition.getAttributes())
                    //This no longer checks for duplicates since the attributes are now a set
                    Util.createAttribute(classNode, attributeMetadata, true);
            }
            
            //Now we make our class to inherit the attributes from its parent class (except for the root class, RootObject)
            if (classDefinition.getParentClassName() == null) { //Is this class the root of all class hierarchy
                
                if (classDefinition.getName().equals(Constants.CLASS_ROOTOBJECT))
                    classNode.addLabel(DynamicLabel.label(Constants.LABEL_ROOT));
                else
                    throw new MetadataObjectNotFoundException(String.format("Only %s can be the root superclass", Constants.CLASS_ROOTOBJECT));
            }
            else { 
                Node parentNode = classIndex.get(Constants.PROPERTY_NAME, classDefinition.getParentClassName()).getSingle();
                if (parentNode != null) {
                    classNode.createRelationshipTo(parentNode, RelTypes.EXTENDS);
                    Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
                    //Set extendended attributes from parent
                    for (Relationship rel : relationships) {
                        Node parentAttrNode = rel.getEndNode();
                        
                        //We ignore the attributes already existing in the class definition
                        //TODO: This block of code only exists because the class hierachy 
                        //file used to reset the data model is redundant. Once its fixed, please remove it
                        String attributeName = String.valueOf(parentAttrNode.getProperty(Constants.PROPERTY_NAME));
                        boolean skipThis = false;
                        for (AttributeMetadata anAttribute : classDefinition.getAttributes()) {
                            if (anAttribute.getName().equals(attributeName)) {
                                skipThis = true;
                                break;
                            }
                        }
                        
                        if (skipThis)
                            continue;
                        
                        label = DynamicLabel.label(Constants.LABEL_ATTRIBUTE);
                        Node newAttrNode = graphDb.createNode(label);
                        newAttrNode.setProperty(Constants.PROPERTY_NAME, attributeName);
                        newAttrNode.setProperty(Constants.PROPERTY_DESCRIPTION, parentAttrNode.getProperty(Constants.PROPERTY_DESCRIPTION));
                        newAttrNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, parentAttrNode.getProperty(Constants.PROPERTY_DISPLAY_NAME));
                        newAttrNode.setProperty(Constants.PROPERTY_TYPE, parentAttrNode.getProperty(Constants.PROPERTY_TYPE));
                        newAttrNode.setProperty(Constants.PROPERTY_READ_ONLY, parentAttrNode.getProperty(Constants.PROPERTY_READ_ONLY));
                        newAttrNode.setProperty(Constants.PROPERTY_VISIBLE, parentAttrNode.getProperty(Constants.PROPERTY_VISIBLE));
                        newAttrNode.setProperty(Constants.PROPERTY_ADMINISTRATIVE, parentAttrNode.getProperty(Constants.PROPERTY_ADMINISTRATIVE));
                        newAttrNode.setProperty(Constants.PROPERTY_NO_COPY, parentAttrNode.getProperty(Constants.PROPERTY_NO_COPY));
                        newAttrNode.setProperty(Constants.PROPERTY_UNIQUE, parentAttrNode.getProperty(Constants.PROPERTY_UNIQUE));
                        newAttrNode.setProperty(Constants.PROPERTY_MANDATORY, parentNode.hasProperty(Constants.PROPERTY_MANDATORY) ?
                                parentAttrNode.getProperty(Constants.PROPERTY_MANDATORY) : false);
                        //newAttrNode.setProperty(PROPERTY_LOCKED, parentAttrNode.getProperty(PROPERTY_LOCKED));
                        classNode.createRelationshipTo(newAttrNode, RelTypes.HAS_ATTRIBUTE);
                    }
                }//end if there is a Parent
                else
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find parent class with name %s", classDefinition.getParentClassName()));
            }//end else not rootNode
            
            buildClassCache();
            tx.success();
        }
        getSubClassesLight(classDefinition.getName(), true, false);
        getSubClassesLightNoRecursive(classDefinition.getName(), true, false);
        return id;
    }

    @Override
    public ChangeDescriptor setClassProperties (ClassMetadata newClassDefinition) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, ObjectNotFoundException {
        String affectedProperties = "", oldValues = "", newValues = "";
        
        try (Transaction tx = graphDb.beginTx()) {
            Node classMetadata = classIndex.get(Constants.PROPERTY_ID, newClassDefinition.getId()).getSingle();
            if (classMetadata == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %s", newClassDefinition.getName()));
            
            String formerName = (String)classMetadata.getProperty(Constants.PROPERTY_NAME);
            
            if(newClassDefinition.getName() != null){
                if(newClassDefinition.getName().isEmpty())
                    throw new InvalidArgumentException("Class name can not be an empty string");
                
                if (!newClassDefinition.getName().matches("^[a-zA-Z0-9_-]*$"))
                    throw new InvalidArgumentException(String.format("Class name %s contains invalid characters", newClassDefinition.getName()));
                
                if (classIndex.get(Constants.PROPERTY_NAME, newClassDefinition.getName()).getSingle() != null)
                   throw new InvalidArgumentException(String.format("Class %s already exists", newClassDefinition.getName()));
                
                classIndex.remove(classMetadata, Constants.PROPERTY_NAME);
                classMetadata.setProperty(Constants.PROPERTY_NAME, newClassDefinition.getName());
                
                affectedProperties = Constants.PROPERTY_NAME + " ";
                oldValues = classMetadata.getProperty(Constants.PROPERTY_NAME) + " ";
                newValues = newClassDefinition.getName() + " ";
                
                classIndex.add(classMetadata, Constants.PROPERTY_NAME, newClassDefinition.getName());
                buildClassCache();
            }
            if(newClassDefinition.getDisplayName() != null) {
                affectedProperties = Constants.PROPERTY_DISPLAY_NAME + " ";
                oldValues = classMetadata.getProperty(Constants.PROPERTY_DISPLAY_NAME) + " ";
                newValues = newClassDefinition.getDisplayName() + " ";
                
                classMetadata.setProperty(Constants.PROPERTY_DISPLAY_NAME, newClassDefinition.getDisplayName());
            }
            if(newClassDefinition.getDescription() != null) {
                affectedProperties = Constants.PROPERTY_DESCRIPTION + " ";
                oldValues = classMetadata.getProperty(Constants.PROPERTY_DESCRIPTION) + " ";
                newValues = newClassDefinition.getDescription() + " ";
                
                classMetadata.setProperty(Constants.PROPERTY_DESCRIPTION, newClassDefinition.getDescription());
            }
            if(newClassDefinition.getIcon() != null) {
                affectedProperties = Constants.PROPERTY_ICON + " ";
                oldValues = " ";
                newValues = " ";
                
                classMetadata.setProperty(Constants.PROPERTY_ICON, newClassDefinition.getIcon());
            }
            if(newClassDefinition.getSmallIcon() != null) {
                affectedProperties = Constants.PROPERTY_SMALL_ICON + " ";
                oldValues = " ";
                newValues = " ";
                
                classMetadata.setProperty(Constants.PROPERTY_SMALL_ICON, newClassDefinition.getSmallIcon());
            }
            if(newClassDefinition.getColor() != -1) {
                affectedProperties = Constants.PROPERTY_COLOR + " ";
                oldValues = classMetadata.getProperty(Constants.PROPERTY_COLOR) + " ";
                newValues = newClassDefinition.getColor() + " ";
                
                classMetadata.setProperty(Constants.PROPERTY_COLOR, newClassDefinition.getColor());
            }
            if (newClassDefinition.isCountable() != null) {
                affectedProperties = Constants.PROPERTY_COUNTABLE + " ";
                oldValues = classMetadata.getProperty(Constants.PROPERTY_COUNTABLE) + " ";
                newValues = newClassDefinition.isCountable() + " ";
                
                classMetadata.setProperty(Constants.PROPERTY_COUNTABLE, newClassDefinition.isCountable());
            }
            if (newClassDefinition.isAbstract() != null) {
                affectedProperties = Constants.PROPERTY_ABSTRACT + " ";
                oldValues = classMetadata.getProperty(Constants.PROPERTY_ABSTRACT) + " ";
                newValues = newClassDefinition.isAbstract() + " ";
                
                classMetadata.setProperty(Constants.PROPERTY_ABSTRACT, newClassDefinition.isAbstract());
            }
            if (newClassDefinition.isInDesign() != null) {
                affectedProperties = Constants.PROPERTY_IN_DESIGN + " ";
                oldValues = classMetadata.getProperty(Constants.PROPERTY_IN_DESIGN) + " ";
                newValues = newClassDefinition.isInDesign() + " ";
                
                classMetadata.setProperty(Constants.PROPERTY_IN_DESIGN, newClassDefinition.isInDesign());
            }
            if (newClassDefinition.isCustom() != null) {
                affectedProperties = Constants.PROPERTY_CUSTOM + " ";
                oldValues = classMetadata.getProperty(Constants.PROPERTY_CUSTOM) + " ";
                newValues = newClassDefinition.isCustom() + " ";
                
                classMetadata.setProperty(Constants.PROPERTY_CUSTOM, newClassDefinition.isCustom());
            }
            
            if(newClassDefinition.getAttributes() != null ) {
                for (AttributeMetadata attr : newClassDefinition.getAttributes()) {
                    ChangeDescriptor changeDescriptor = setAttributeProperties(newClassDefinition.getId(), attr);
                    
                    affectedProperties = changeDescriptor.getAffectedProperties() + " ";
                    oldValues = changeDescriptor.getOldValues() + " ";
                    newValues = changeDescriptor.getNewValues() + " ";
                }
            }        
            tx.success();
            cm.clearClassCache();
            cm.removeClass(formerName);
            cm.putClass(Util.createClassMetadataFromNode(classMetadata));

            return new ChangeDescriptor(affectedProperties.trim(), oldValues.trim(), 
                    newValues.trim(), String.format("Set class properties and/or attributes for class %s", classMetadata.getProperty(Constants.PROPERTY_NAME)));
        }
    }
  
    @Override
    public void deleteClass(long classId) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            Node node = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();

            if (node == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %s", classId));
                      
            if (!(Boolean)node.getProperty(Constants.PROPERTY_CUSTOM))
                throw new InvalidArgumentException(String.format(
                        "Core classes can not be deleted"));
            
            if (node.hasRelationship(RelTypes.INSTANCE_OF))
                throw new InvalidArgumentException(String.format(
                        "Class %s has instances and can not be deleted", node.getProperty(Constants.PROPERTY_NAME)));
            
            if (node.hasRelationship(Direction.INCOMING, RelTypes.EXTENDS))
                throw new InvalidArgumentException(String.format(
                        "Class %s has subclasses and can not be deleted", node.getProperty(Constants.PROPERTY_NAME)));

            String className = (String)node.getProperty(Constants.PROPERTY_NAME);
            if (isSubClass(Constants.CLASS_GENERICOBJECTLIST, className)) {
                //If the class is a list type, let's check if it's used by another class
                for (Node classNode : classIndex.query(Constants.PROPERTY_ID, "*")) {
                    for (Relationship hasAttributeRelationship : classNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_ATTRIBUTE)) {
                        Node attributeNode = hasAttributeRelationship.getEndNode();
                        if (attributeNode.getProperty(Constants.PROPERTY_TYPE).equals(className))
                            throw new InvalidArgumentException(
                                    String.format("%s is a list type and at least one attribute (%s) from another class (%s) is using it",
                                                    className, attributeNode.getProperty(Constants.PROPERTY_NAME), classNode.getProperty(Constants.PROPERTY_NAME)));
                    }
               }
            }
            
            //Delete the related elements that don't require any complex Remove operation
            Iterable<Relationship> outgoingSimpleRelationships = node.getRelationships(RelTypes.HAS_ATTRIBUTE, RelTypes.HAS_REPORT);
            for (Relationship rel : outgoingSimpleRelationships){
                Node relatedNode = rel.getEndNode();
                rel.delete();
                relatedNode.delete();
            }
            
            //Delete the existing templates
            Iterable<Relationship> templateRelationshipsRelationships = node.getRelationships(RelTypes.HAS_TEMPLATE);
            for (Relationship rel : templateRelationshipsRelationships)
                Util.deleteTemplateObject(rel.getEndNode());
            
            
            //Release the rest of relationships
            for (Relationship rel : node.getRelationships())
                rel.delete();
            
            classIndex.remove(node);
            node.delete();
            buildClassCache();
            tx.success();
        }
    }
    
    @Override
    public void deleteClass(String className) 
            throws MetadataObjectNotFoundException, InvalidArgumentException   {
        try (Transaction tx  = graphDb.beginTx()) {
            Node node = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();

            if (node == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with name %s", className));
            
            if (!(Boolean)node.getProperty(Constants.PROPERTY_CUSTOM))
                throw new InvalidArgumentException(String.format(
                        "Core classes can not be deleted"));
                       
            if (node.hasRelationship(RelTypes.INSTANCE_OF))
                throw new InvalidArgumentException(String.format(
                        "The class with name %s has instances and can not be deleted", className));
            
            if (node.hasRelationship(Direction.INCOMING, RelTypes.EXTENDS))
                throw new InvalidArgumentException(String.format(
                        "The class with name %s has subclasses and can not be deleted", className));
            
            if (isSubClass(Constants.CLASS_GENERICOBJECTLIST, className)) {
                //If the class is a list type, let's check if it's used by another class
                for (Node classNode : classIndex.query(Constants.PROPERTY_ID, "*")) {
                    for (Relationship hasAttributeRelationship : classNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_ATTRIBUTE)) {
                        Node attributeNode = hasAttributeRelationship.getEndNode();
                        if (attributeNode.getProperty(Constants.PROPERTY_TYPE).equals(className))
                            throw new InvalidArgumentException(
                                    String.format("%s is a list type and at least one attribute (%s) from another class (%s) is using it",
                                                    className, attributeNode.getProperty(Constants.PROPERTY_NAME), classNode.getProperty(Constants.PROPERTY_NAME)));
                    }
               }
            }
            
            //Delete the related elements that don't require any complex Remove operation
            Iterable<Relationship> outgoingSimpleRelationships = node.getRelationships(RelTypes.HAS_ATTRIBUTE, RelTypes.HAS_REPORT);
            for (Relationship rel : outgoingSimpleRelationships){
                Node relatedNode = rel.getEndNode();
                rel.delete();
                relatedNode.delete();
            }
            
            //Delete the existing templates
            Iterable<Relationship> templateRelationshipsRelationships = node.getRelationships(RelTypes.HAS_TEMPLATE);
            for (Relationship rel : templateRelationshipsRelationships)
                Util.deleteTemplateObject(rel.getEndNode());
            
            //Release the rest of relationships
            for (Relationship rel : node.getRelationships())
                rel.delete();
            
            classIndex.remove(node);
            node.delete();
            buildClassCache();
            tx.success();
        }
    }
   
    @Override
    public List<ClassMetadataLight> getAllClassesLight(boolean includeListTypes, 
            boolean includeIndesign) 
            throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> cml = new ArrayList<>();
        try (Transaction tx = graphDb.beginTx()) {
            Node myClassInventoryObjectNode =  classIndex.get(Constants.PROPERTY_NAME, Constants.CLASS_INVENTORYOBJECT).getSingle();
            Node myClassGenericObjectListNode = null;
            
            if (includeListTypes)
                myClassGenericObjectListNode =  classIndex.get(Constants.PROPERTY_NAME, Constants.CLASS_GENERICOBJECTLIST).getSingle();
            
            if (myClassInventoryObjectNode == null) {
                throw new MetadataObjectNotFoundException(String.format(
                         "Can not find a class with name %s", Constants.CLASS_INVENTORYOBJECT));
            }
            else
                cml.add(Util.createClassMetadataLightFromNode(myClassInventoryObjectNode));
                            
            if (includeListTypes && myClassGenericObjectListNode == null) {
                throw new MetadataObjectNotFoundException(String.format(
                         "Can not find a class with name %s", Constants.CLASS_GENERICOBJECTLIST));
            }
            
            if (includeListTypes && myClassGenericObjectListNode != null)
                cml.add(Util.createClassMetadataLightFromNode(myClassGenericObjectListNode));
            
            String cypherQuery = "START inventory = node:classes({className}) ".concat(
                                 "MATCH inventory <-[:").concat(RelTypes.EXTENDS.toString()).concat("*]-classmetadata ").concat(
                                 "RETURN classmetadata,inventory ").concat(
                                 "ORDER BY classmetadata.name ASC");

            Map<String, Object> params = new HashMap<>();
            if(includeListTypes)
                params.put("className", "name:"+ Constants.CLASS_INVENTORYOBJECT + " name:" + Constants.CLASS_GENERICOBJECTLIST);//NOI18N
            
            else
                params.put("className", "name:"+ Constants.CLASS_INVENTORYOBJECT);//NOI18N
            
            Result result = graphDb.execute(cypherQuery, params);
            Iterator<Node> n_column = result.columnAs("classmetadata"); 
            
            for (Node node : IteratorUtil.asIterable(n_column))
                 cml.add(Util.createClassMetadataLightFromNode(node));
        }
        return cml;
    }

    @Override
    public List<ClassMetadataLight> getSubClassesLight(String className, boolean includeAbstractClasses, 
            boolean includeSelf) throws MetadataObjectNotFoundException {
        
        ClassMetadata aClass = getClass(className);
        
        List<ClassMetadataLight> subclasses = cm.getSubclasses(className);
        
        List<ClassMetadataLight> classManagerResultList = new ArrayList<>();
        
        if(subclasses != null) {
            for (ClassMetadataLight subclass : subclasses) {                
                if (!includeAbstractClasses && subclass.isAbstract())
                    continue;
                classManagerResultList.add(subclass);
            }
            
            if (includeSelf && (includeAbstractClasses ? true : !aClass.isAbstract()))
                classManagerResultList.add(aClass);
            
            return classManagerResultList;
        }
        // Retrieving all subclasses to update the cache
        String cypherQuery = "START inventory = node:classes({className}) ".concat(
                             "MATCH (inventory)<-[:").concat(RelTypes.EXTENDS.toString()).concat("*]-(classmetadata) ").concat(
                             "RETURN classmetadata ").concat(
                             "ORDER BY classmetadata.name ASC");
                
        Map<String, Object> params = new HashMap<>();
        params.put("className", "name:"+ className); //NOI18N
        
        subclasses = new ArrayList();
            
        try (Transaction tx = graphDb.beginTx()) {
            Result result = graphDb.execute(cypherQuery, params);
            Iterator<Node> n_column = result.columnAs("classmetadata"); //NOI18N
            if (includeSelf && (includeAbstractClasses ? true : !aClass.isAbstract()))
                classManagerResultList.add(aClass);
            
            for (Node node : IteratorUtil.asIterable(n_column)) {
                ClassMetadataLight classMetadata = Util.createClassMetadataLightFromNode(node);
                subclasses.add(classMetadata);
                
                if (!includeAbstractClasses && classMetadata.isAbstract())
                    continue;
                    
                classManagerResultList.add(classMetadata);
            }
        }
        cm.putSubclasses(className, subclasses);
        
        return classManagerResultList;
    }


    @Override
    public List<ClassMetadataLight> getSubClassesLightNoRecursive(String className, 
            boolean includeAbstractClasses, boolean includeSelf) 
            throws MetadataObjectNotFoundException {
        ClassMetadata aClass = cm.getClass(className);
        if (aClass == null)
            throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s", className));
        
        List<ClassMetadataLight> subclasses = cm.getSubclassesNorecursive(className);        
        List<ClassMetadataLight> classManagerResultList = new ArrayList<>();
        if(subclasses != null) {
            for (ClassMetadataLight subclass : subclasses) {
                if (!includeAbstractClasses && subclass.isAbstract())
                    continue;
                classManagerResultList.add(subclass);
            }
            
            if (includeSelf && (includeAbstractClasses ? true : !aClass.isAbstract()))
                classManagerResultList.add(aClass);
            
            return classManagerResultList;
        }
        // Retrieving all subclasses to update the cache
        classManagerResultList = new ArrayList<>();
        String cypherQuery = "START inventory = node:classes({className}) ".concat(
                             "MATCH (inventory)<-[:").concat(RelTypes.EXTENDS.toString()).concat("]-(classmetadata) ").concat(
                             "RETURN classmetadata ").concat(
                             "ORDER BY classmetadata.name ASC");

        Map<String, Object> params = new HashMap<>();
        params.put("className", "name:"+ className); //NOI18N
        
        subclasses = new ArrayList();

        try (Transaction tx = graphDb.beginTx()) {
            
            Result result = graphDb.execute(cypherQuery, params);
            Iterator<Node> n_column = result.columnAs("classmetadata"); //NOI18N
            if (includeSelf && (includeAbstractClasses ? true : !aClass.isAbstract()))
                classManagerResultList.add(aClass);
            for (Node node : IteratorUtil.asIterable(n_column)) {
                ClassMetadataLight classMetadata = Util.createClassMetadataLightFromNode(node);
                subclasses.add(classMetadata);
                
                if (!includeAbstractClasses && classMetadata.isAbstract())
                    continue;
                    
                classManagerResultList.add(classMetadata);
            }
        }
        cm.putSubclassesNorecursive(className, subclasses);
        return classManagerResultList;
    }
    
    @Override
    public List<ClassMetadata> getAllClasses(boolean includeListTypes, boolean includeIndesign) {
        List<ClassMetadata> classMetadataResultList = new ArrayList<>();
        
        String cypherQuery = "START inventory = node:classes({className}) " +
                             "MATCH inventory <-[:" + (RelTypes.EXTENDS.toString()) + "*]-classmetadata " +
                             "RETURN classmetadata,inventory " +
                             "ORDER BY classmetadata.name ASC";

        Map<String, Object> params = new HashMap<>();
        if(includeListTypes)
            params.put("className", "name:"+ Constants.CLASS_INVENTORYOBJECT + " name:" + Constants.CLASS_GENERICOBJECTLIST);//NOI18N

        else
            params.put("className", "name:"+ Constants.CLASS_INVENTORYOBJECT);//NOI18N
        
        try (Transaction tx = graphDb.beginTx()) {
           Result result = graphDb.execute(cypherQuery, params);
           Iterator<Node> n_column = result.columnAs("classmetadata");
           //First, we inject the InventoryObject class (for some reason, the start node can't be retrieved as part of the path, so it can be sorted)
           Iterator<Node> roots = result.columnAs("inventory");
           classMetadataResultList.add(Util.createClassMetadataFromNode(roots.next()));

           for (Node node : IteratorUtil.asIterable(n_column))
                classMetadataResultList.add(Util.createClassMetadataFromNode(node));
        }
        return classMetadataResultList;
    }
   
    @Override
    public ClassMetadata getClass(long classId)  throws MetadataObjectNotFoundException {
        ClassMetadata clmt = null;
        try(Transaction tx = graphDb.beginTx()) 
        {
            Node node = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();
            if (node == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %s", classId));
            
            clmt = Util.createClassMetadataFromNode(node);
        } 
        return clmt;
    }
    
    @Override
    public ClassMetadata getClass(String className) throws MetadataObjectNotFoundException {
        ClassMetadata clmt = cm.getClass(className);
        
        if (clmt != null)
            return clmt;
        
        try (Transaction tx = graphDb.beginTx()) {
            Node node = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (node == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with name %s", className));
            clmt = Util.createClassMetadataFromNode(node);
            cm.putClass(clmt);
        }
        return clmt;
    }

    @Override
    public void createAttribute(String className, AttributeMetadata attributeDefinition, boolean recursive) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        if (attributeDefinition.getName() == null || attributeDefinition.getName().isEmpty())
            throw new InvalidArgumentException("Attribute name can not be null or an empty string");
        
        if (!attributeDefinition.getName().matches("^[a-zA-Z0-9_]*$"))
            throw new InvalidArgumentException(String.format("Attribute %s contains invalid characters", attributeDefinition.getName()));
        
        try (Transaction tx = graphDb.beginTx())
        {
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s", className));
            
            Util.createAttribute(classNode, attributeDefinition, recursive);
            //Refresh cache for the affected classes
            refreshCacheOn(classNode);
            tx.success();
        } 
    }
    
    @Override
    public void createAttribute(long classId, AttributeMetadata attributeDefinition) 
            throws MetadataObjectNotFoundException, InvalidArgumentException 
    {
        try (Transaction tx = graphDb.beginTx())
        {        
            Node classNode = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Can not find a class with id %s", classId));
        
            Util.createAttribute(classNode, attributeDefinition, true);
            //Refresh cache for the affected classes
            refreshCacheOn(classNode);
            tx.success();
            
        } 
    }
    
    @Override
    public boolean hasAttribute(String className, String attributeName) throws MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format(
                    "Can not find a class with name %s", className));
            
            for (Relationship relationship : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE)) {
                Node attrNode = relationship.getEndNode();
                
                if (String.valueOf(attrNode.getProperty(Constants.PROPERTY_NAME)).equals(attributeName))
                    return true;
            }
            return false;
        }
    }

    @Override
    public AttributeMetadata getAttribute(String className, String attributeName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        AttributeMetadata attribute = null;
        try (Transaction tx = graphDb.beginTx())
        {
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();

            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with name %s", className));
            
            Iterable<Relationship> relationships = classNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship relationship : relationships) {
                Node attrNode = relationship.getEndNode();
                if (String.valueOf(attrNode.getProperty(Constants.PROPERTY_NAME)).equals(attributeName)){ 
                    attribute = Util.createAttributeMetadataFromNode(attrNode);
                    break;
                }
            }
        }
        
        if (attribute == null)
            throw new InvalidArgumentException(String.format("Attribute %s does not exist in class %s", attributeName, className));
        
        return attribute;
    }
    
    @Override
    public AttributeMetadata getAttribute(long classId, long attributeId) 
            throws MetadataObjectNotFoundException, InvalidArgumentException
    {
        AttributeMetadata attribute = null;
        try (Transaction tx = graphDb.beginTx())
        {
            Node classNode = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();
            if (classNode == null) 
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %s", classId));
            
            Iterable<Relationship> relationships = classNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship relationship : relationships) {
                Node attrNode = relationship.getEndNode();
                if (attrNode.getId() == attributeId) {
                    attribute = Util.createAttributeMetadataFromNode(attrNode);
                    break;
                }
            }
        }
        
        if (attribute == null)
            throw new InvalidArgumentException(String.format("Attribute with id %s does not exist in class with id %s", attributeId, classId));
        
        return attribute;
    }
    
    @Override
    public ChangeDescriptor setAttributeProperties(long classId, AttributeMetadata newAttributeDefinition) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, ObjectNotFoundException {
        
        String affectedProperties = "", oldValues = "", newValues = "";
        
        try(Transaction tx = graphDb.beginTx()) {
            Node classNode = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Can not find a class with id %s", classId));

            for (Relationship relationship : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE)) {
                Node attrNode = relationship.getEndNode();
                if (attrNode.getId() == newAttributeDefinition.getId())  {
                    String currentAttributeName = (String)attrNode.getProperty(Constants.PROPERTY_NAME);

                    if (currentAttributeName.equals(Constants.PROPERTY_CREATION_DATE))
                        throw new InvalidArgumentException(String.format("Attribute \"%s\" can not be modified", currentAttributeName));

                    if(newAttributeDefinition.getName() != null){
                        if (currentAttributeName.equals(Constants.PROPERTY_NAME))
                            throw new InvalidArgumentException(String.format("Attribute \"%s\" can not be renamed", currentAttributeName));
                        if (!newAttributeDefinition.getName().matches("^[a-zA-Z0-9_]*$"))
                            throw new InvalidArgumentException(String.format("Attribute %s contains invalid characters", newAttributeDefinition.getName()));
                        
                        Util.changeAttributeName(classNode, currentAttributeName, newAttributeDefinition.getName());
                        
                        affectedProperties = Constants.PROPERTY_NAME + " ";
                        oldValues = currentAttributeName + " ";
                        newValues = newAttributeDefinition.getName() + " ";
                    }
                    if(newAttributeDefinition.getDescription() != null) {
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_DESCRIPTION, newAttributeDefinition.getDescription());
                        
                        affectedProperties = Constants.PROPERTY_DESCRIPTION + " ";
                        oldValues = currentAttributeName + " ";
                        newValues = newAttributeDefinition.getDescription() + " ";
                    }
                    if(newAttributeDefinition.getDisplayName() != null) {
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_DISPLAY_NAME, newAttributeDefinition.getDisplayName());
                        
                        affectedProperties = Constants.PROPERTY_DISPLAY_NAME + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.getDisplayName() + " ";
                    }
                    if(newAttributeDefinition.getType() != null){
                        if (currentAttributeName.equals(Constants.PROPERTY_NAME))
                            throw new InvalidArgumentException(String.format("Attribute \"%s\" can only be a String", currentAttributeName));
                        if (AttributeMetadata.isPrimitive((String)attrNode.getProperty(Constants.PROPERTY_TYPE)))
                            Util.changeAttributeTypeIfPrimitive(classNode, currentAttributeName, newAttributeDefinition.getType());
                        else
                            Util.changeAttributeTypeIfListType(classNode, currentAttributeName, newAttributeDefinition.getType());
                        
                        affectedProperties = Constants.PROPERTY_TYPE + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.getType() + " ";
                    }
                    if(newAttributeDefinition.isReadOnly() != null) {
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_READ_ONLY, newAttributeDefinition.isReadOnly());
                        
                        affectedProperties = Constants.PROPERTY_READ_ONLY + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.isReadOnly() + " ";
                    }
                    if(newAttributeDefinition.isVisible() != null) {
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_VISIBLE, newAttributeDefinition.isVisible());
                        
                        affectedProperties = Constants.PROPERTY_VISIBLE + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.isVisible() + " ";
                    }
                    if(newAttributeDefinition.isAdministrative() != null) {
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_ADMINISTRATIVE, newAttributeDefinition.isAdministrative());
                        
                        affectedProperties = Constants.PROPERTY_ADMINISTRATIVE + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.isAdministrative() + " ";
                    }
                    if(newAttributeDefinition.isNoCopy() != null) {
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_NO_COPY, newAttributeDefinition.isNoCopy());
                        
                        affectedProperties = Constants.PROPERTY_NO_COPY + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.isNoCopy() + " ";
                    }
                    if(newAttributeDefinition.isUnique() != null) {
                        if(newAttributeDefinition.isUnique()){//checks only if unique changed from false to true
                            if(canAttributeBeUnique(classNode, Util.getTypeOfAttribute(classNode, currentAttributeName), currentAttributeName))
                                Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_UNIQUE, newAttributeDefinition.isUnique());
                            else
                                 throw new InvalidArgumentException(String.format("In order to mark attribute \"%s\" as unique it is necessary to set a unique value for every existing object of this class and its subclasses", currentAttributeName));
                        }
                        else{
                            Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_UNIQUE, newAttributeDefinition.isUnique());
                            cm.removeUniqueAtribute((String)classNode.getProperty(Constants.PROPERTY_NAME), currentAttributeName);
                        }
                        affectedProperties = Constants.PROPERTY_UNIQUE + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.isUnique() + " ";
                    }       
                    if(newAttributeDefinition.isMandatory() != null){
                        //this check if every object of the class and subclasses has a value in this attribute marked as mandatory
                        if(newAttributeDefinition.isMandatory()){//checks only if mandatory has changed from false to true
                            if(objectsOfClassHasValueInMandatoryAttribute((String)classNode.getProperty(Constants.PROPERTY_NAME), currentAttributeName))
                                Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_MANDATORY, newAttributeDefinition.isMandatory());
                            else
                                throw new InvalidArgumentException(String.format("Before setting an attribute as mandatory, all instances of this class must have valid values for attribute %s", currentAttributeName));
                        }
                        else
                            Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_MANDATORY, newAttributeDefinition.isMandatory());
                        
                        affectedProperties = Constants.PROPERTY_MANDATORY + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.isMandatory() + " ";
                    }
                    //Refresh cache for the affected classes
                    refreshCacheOn(classNode);
                    tx.success();                    
                    return new ChangeDescriptor(affectedProperties.trim(), oldValues.trim(), newValues.trim(), String.format("Update attributes properties in class %s", classNode.getProperty(Constants.PROPERTY_NAME)));
                }
            }//end for
            throw new MetadataObjectNotFoundException(String.format(
                    "Can not find attribute %s in class %s", newAttributeDefinition.getName(), classNode.getProperty(Constants.PROPERTY_NAME)));
        } 
    }
    
    @Override
    public ChangeDescriptor setAttributeProperties (String className, AttributeMetadata newAttributeDefinition) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, ObjectNotFoundException {
        
        String affectedProperties = "", oldValues = "", newValues = "";
        
        try(Transaction tx = graphDb.beginTx()) 
        {
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s", className));

            for (Relationship relationship : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE)) {
                Node attrNode = relationship.getEndNode();
                if (attrNode.getId() == newAttributeDefinition.getId()) {
                    String currentAttributeName = (String)attrNode.getProperty(Constants.PROPERTY_NAME);

                    if (currentAttributeName.equals(Constants.PROPERTY_CREATION_DATE))
                        throw new InvalidArgumentException(String.format("Attribute \"%s\" can not be modified", currentAttributeName));

                    if(newAttributeDefinition.getName() != null){
                        if (currentAttributeName.equals(Constants.PROPERTY_NAME))
                            throw new InvalidArgumentException(String.format("Attribute \"%s\" can not be renamed", currentAttributeName));
                        if (!newAttributeDefinition.getName().matches("^[a-zA-Z0-9_]*$"))
                            throw new InvalidArgumentException(String.format("Attribute %s contains invalid characters", newAttributeDefinition.getName()));

                        Util.changeAttributeName(classNode, currentAttributeName, newAttributeDefinition.getName());
                        
                        affectedProperties = Constants.PROPERTY_NAME + " ";
                        oldValues = currentAttributeName + " ";
                        newValues = newAttributeDefinition.getName() + " ";
                    }
                    if(newAttributeDefinition.getDescription() != null) {
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_DESCRIPTION, newAttributeDefinition.getDescription());
                        
                        affectedProperties = Constants.PROPERTY_DESCRIPTION + " ";
                        oldValues = currentAttributeName + " ";
                        newValues = newAttributeDefinition.getDescription() + " ";
                    }
                    if(newAttributeDefinition.getDisplayName() != null) {
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_DISPLAY_NAME, newAttributeDefinition.getDisplayName());
                        
                        affectedProperties = Constants.PROPERTY_DISPLAY_NAME + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.getDisplayName() + " ";
                    }
                    if(newAttributeDefinition.getType() != null){
                        if (currentAttributeName.equals(Constants.PROPERTY_NAME))
                            throw new InvalidArgumentException(String.format("Attribute \"%s\" can only be a String", currentAttributeName));
                        if (AttributeMetadata.isPrimitive((String)attrNode.getProperty(Constants.PROPERTY_TYPE)))
                            Util.changeAttributeTypeIfPrimitive(classNode, currentAttributeName, newAttributeDefinition.getType());
                        else
                            Util.changeAttributeTypeIfListType(classNode, currentAttributeName, newAttributeDefinition.getType());
                        
                        affectedProperties = Constants.PROPERTY_TYPE + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.getType() + " ";
                    }
                    if(newAttributeDefinition.isReadOnly() != null) {
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_READ_ONLY, newAttributeDefinition.isReadOnly());
                        
                        affectedProperties = Constants.PROPERTY_READ_ONLY + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.isReadOnly() + " ";
                    }
                    if(newAttributeDefinition.isVisible() != null) {
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_VISIBLE, newAttributeDefinition.isVisible());
                        
                        affectedProperties = Constants.PROPERTY_VISIBLE + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.isVisible() + " ";
                    }
                    if(newAttributeDefinition.isAdministrative() != null) {
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_ADMINISTRATIVE, newAttributeDefinition.isAdministrative());
                        
                        affectedProperties = Constants.PROPERTY_ADMINISTRATIVE + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.isAdministrative() + " ";
                    }
                    if(newAttributeDefinition.isNoCopy() != null) {
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_NO_COPY, newAttributeDefinition.isNoCopy());
                        
                        affectedProperties = Constants.PROPERTY_NO_COPY + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.isNoCopy() + " ";
                    }
                    if(newAttributeDefinition.isUnique() != null){
                        if(newAttributeDefinition.isUnique()){//checks only if unique changed from false to true
                            if(canAttributeBeUnique(classNode, Util.getTypeOfAttribute(classNode, currentAttributeName), currentAttributeName))
                                Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_UNIQUE, newAttributeDefinition.isUnique());
                        else
                             throw new InvalidArgumentException(String.format("In order to mark attribute \"%s\" as unique it is necessary to set a unique value for every existing object with this attribute name", currentAttributeName));
                        }
                        else{
                            Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_UNIQUE, newAttributeDefinition.isUnique());
                            cm.removeUniqueAtribute(currentAttributeName, currentAttributeName);
                        }
                        
                        affectedProperties = Constants.PROPERTY_UNIQUE + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.isUnique() + " ";
                    }   
                    if(newAttributeDefinition.isMandatory() != null){
                        if(newAttributeDefinition.isMandatory()){//checks only if mandatory changed from false to true
                            //this check if every object of the class and subclasses has a value in this attribute marked as mandatory
                            if(objectsOfClassHasValueInMandatoryAttribute(className, currentAttributeName))
                                Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_MANDATORY, newAttributeDefinition.isMandatory());
                            else
                                throw new InvalidArgumentException(String.format("Before setting it as mandatory, all instances of this class must have valid values for attribute %s", currentAttributeName));
                        }
                        else
                            Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_MANDATORY, newAttributeDefinition.isMandatory());
                        
                        affectedProperties = Constants.PROPERTY_MANDATORY + " ";
                        oldValues = " ";
                        newValues = newAttributeDefinition.isMandatory() + " ";
                    }
                    //Refresh cache for the affected classes
                    refreshCacheOn(classNode);
                    tx.success();
                    return new ChangeDescriptor(affectedProperties.trim(), oldValues.trim(), newValues.trim(), String.format("Update attributes properties in class %s", classNode.getProperty(Constants.PROPERTY_NAME)));
                }
            }//end for
            throw new MetadataObjectNotFoundException(String.format(
                    "Can not find attribute %s in class %s", newAttributeDefinition.getName(), className));
        }
    }
    
    @Override
    public void deleteAttribute(String className, String attributeName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        if (attributeName.equals(Constants.PROPERTY_NAME) || attributeName.equals(Constants.PROPERTY_CREATION_DATE))
            throw new InvalidArgumentException(String.format("Attribute \"%s\" can not be deleted", attributeName));
        
        try(Transaction tx = graphDb.beginTx()) {
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, attributeName).getSingle();

            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s", className));

            for (Relationship relationship : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE)) {
                Node attrNode = relationship.getEndNode();
                if (String.valueOf(attrNode.getProperty(Constants.PROPERTY_NAME)).equals(attributeName)){
                
                    if (AttributeMetadata.isPrimitive((String)attrNode.getProperty(Constants.PROPERTY_TYPE)))
                        Util.deleteAttributeIfPrimitive(classNode, attributeName);
                    else
                        Util.deleteAttributeIfListType(classNode, attributeName);
                    tx.success();
                    return;
                }//end for
            }//end for
        } 
        throw new MetadataObjectNotFoundException(String.format("Can not find an attribute with the name %s", attributeName));
    }

    @Override
    public void deleteAttribute(long classId, String attributeName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        if (attributeName.equals(Constants.PROPERTY_CREATION_DATE) || attributeName.equals(Constants.PROPERTY_NAME))
            throw new InvalidArgumentException(String.format("Attribute \"%s\" can not be deleted", attributeName));
        
        try (Transaction tx = graphDb.beginTx()) {
            Node classNode = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();

            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Can not find a class with id %s", classId));

            for (Relationship relationship : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE)) {
                Node attrNode = relationship.getEndNode();
                if (String.valueOf(attrNode.getProperty(Constants.PROPERTY_NAME)).equals(attributeName)){
                        if (AttributeMetadata.isPrimitive((String)attrNode.getProperty(Constants.PROPERTY_TYPE)))
                            Util.deleteAttributeIfPrimitive(classNode, attributeName);
                        else
                            Util.deleteAttributeIfListType(classNode, attributeName);
                        tx.success();
                        return;
                }
            }//end for
        } 
        throw new MetadataObjectNotFoundException(String.format(
                "Can not find an attribute with name %s", attributeName));
    }
    
    @Override
    public List<ClassMetadataLight> getPossibleChildren(String parentClassName) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> classMetadataResultList = new ArrayList<>();
        
        List<String> cachedPossibleChildren = cm.getPossibleChildren(parentClassName);
                
        if (cachedPossibleChildren != null) {
            for (String cachedPossibleChild : cachedPossibleChildren)
                classMetadataResultList.add(cm.getClass(cachedPossibleChild));
            return classMetadataResultList;
        }
        
        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode;
            
            if (parentClassName == null || parentClassName.equals(Constants.NODE_DUMMYROOT))
                parentNode = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();
            else
                parentNode = classIndex.get(Constants.PROPERTY_NAME, parentClassName).getSingle();
            
            tx.success();
            return refreshPossibleChildren(parentNode);
        }
    }
    
    @Override
    public List<ClassMetadataLight> getPossibleSpecialChildren(String parentClassName) 
            throws MetadataObjectNotFoundException   {
        
        List<ClassMetadataLight> classMetadataResultList = new ArrayList<>();
        
        List<String> cachedPossibleSpecialChildren = cm.getPossibleSpecialChildren(parentClassName);
                
        if (cachedPossibleSpecialChildren != null) {
            for (String cachedPossibleChild : cachedPossibleSpecialChildren)
                classMetadataResultList.add(cm.getClass(cachedPossibleChild));
            return classMetadataResultList;
        }
        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode; 
            
            if (parentClassName == null || parentClassName.equals(Constants.NODE_DUMMYROOT))
                parentNode = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();
            else
                parentNode = classIndex.get(Constants.PROPERTY_NAME, parentClassName).getSingle();
            
            tx.success();
            return refreshPossibleSpecialChildren(parentNode);
        }
    }

    @Override
    public List<ClassMetadataLight> getPossibleChildrenNoRecursive(String parentClassName) 
            throws MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode;
            
            if (parentClassName == null || parentClassName.equals(Constants.NODE_DUMMYROOT))
                parentNode = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();
            else
                parentNode = classIndex.get(Constants.PROPERTY_NAME, parentClassName).getSingle();

            Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.POSSIBLE_CHILD, Direction.OUTGOING);
            
            List<ClassMetadataLight> classMetadataListResult = new ArrayList<>();
            
            for (Relationship rel: relationships) {
                    Node child = rel.getEndNode();
                    classMetadataListResult.add(Util.createClassMetadataFromNode(child));
            }
            
            return classMetadataListResult;
        }
    }
    
    @Override
    public List<ClassMetadataLight> getPossibleSpecialChildrenNoRecursive(String parentClassName) 
            throws MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode;
            
            if (parentClassName == null || parentClassName.equals(Constants.NODE_DUMMYROOT))
                parentNode = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();
            else
                parentNode = classIndex.get(Constants.PROPERTY_NAME, parentClassName).getSingle();

            Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.POSSIBLE_SPECIAL_CHILD, Direction.OUTGOING);
            
            List<ClassMetadataLight> classMetadataListResult = new ArrayList<>();
            
            for (Relationship rel: relationships) {
                Node child = rel.getEndNode();
                classMetadataListResult.add(Util.createClassMetadataFromNode(child));
            }
            
            return classMetadataListResult;
        }
    }
    
    
    @Override
    public boolean canBeChild(String allegedParent, String childToBeEvaluated) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> possibleChildren = getPossibleChildren(allegedParent);

        for (ClassMetadataLight possibleChild : possibleChildren) {
            if (possibleChild.getName().equals(childToBeEvaluated))
                return true;
        }
        return false;
    }
    
    @Override
    public boolean canBeSpecialChild(String allegedParent, String childToBeEvaluated) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> possibleSpecialChildren = getPossibleSpecialChildren(allegedParent);

        for (ClassMetadataLight possibleSpecialChild : possibleSpecialChildren) {
            if (possibleSpecialChild.getName().equals(childToBeEvaluated))
                return true;
        }
        return false;
    }

    @Override
    public void addPossibleChildren(long parentClassId, long[] possibleChildren)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        Node parentNode;
        try(Transaction tx = graphDb.beginTx()) {
            if(parentClassId != -1) {
                parentNode = classIndex.get(Constants.PROPERTY_ID, parentClassId).getSingle();

                if (parentNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find a class with id %s", parentClassId));
                if (!isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format("%s is not a business class, thus can not be added to the containment hierarchy", (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
            }else
                parentNode = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();

            List<ClassMetadataLight> currentPossibleChildren = refreshPossibleChildren(parentNode);
            
            for (long id : possibleChildren) {
                Node childNode = classIndex.get(Constants.PROPERTY_ID, id).getSingle();

                if (childNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find class with id %s", parentClassId));
                
                String newPossibleChildrenClassName = (String)childNode.getProperty(Constants.PROPERTY_NAME);
                String parentClassName = (String)parentNode.getProperty(Constants.PROPERTY_NAME);
                
                if (!isSubClass(Constants.CLASS_INVENTORYOBJECT, newPossibleChildrenClassName))
                    throw new InvalidArgumentException(
                            String.format("%s is not a business class, thus can not be added to the containment hierarchy", newPossibleChildrenClassName));
                
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                   for (Node subclassNode : Util.getAllSubclasses(childNode)){
                       for (ClassMetadataLight possibleChild : currentPossibleChildren){
                            if (possibleChild.getId() == subclassNode.getId())
                                throw new InvalidArgumentException(String.format("A subclass of %s is already a possible child for instances of %s", 
                                        newPossibleChildrenClassName, parentClassName));
                       }
                   }
                }
                else {
                    for (ClassMetadataLight possibleChild : currentPossibleChildren) {
                        if (possibleChild.getId() == childNode.getId())
                            throw new InvalidArgumentException(String.format("Class %s is already a possible child for instances of %s", 
                                    newPossibleChildrenClassName, parentClassName));
                    }
                }
                parentNode.createRelationshipTo(childNode, RelTypes.POSSIBLE_CHILD);               
                
                //Refresh cache
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)) {
                    for (Node subClassNode : Util.getAllSubclasses(childNode)) {
                        if (!(boolean)subClassNode.getProperty(Constants.PROPERTY_ABSTRACT))
                            cm.putPossibleChild(parentClassName, (String)subClassNode.getProperty(Constants.PROPERTY_NAME));
                    }
                } else
                    cm.putPossibleChild(parentClassName, newPossibleChildrenClassName);
            }
            tx.success();
        }
    }
    
    @Override
    public void addPossibleSpecialChildren(long parentClassId, long[] possibleSpecialChildren)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node parentNode;
            if(parentClassId != -1) {
                parentNode = classIndex.get(Constants.PROPERTY_ID, parentClassId).getSingle();

                if (parentNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find a class with id %s", parentClassId));
                if (!isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format("%s is not a business class, thus can not be added to the containment hierarchy", (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
            } else
                parentNode = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();

            List<ClassMetadataLight> currentPossibleSpecialChildren = refreshPossibleSpecialChildren(parentNode);
            
            for (long id : possibleSpecialChildren) {
                Node childNode = classIndex.get(Constants.PROPERTY_ID, id).getSingle();

                if (childNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find class with id %s", parentClassId));
                
                if (!isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)childNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format("%s is not a business class, thus can not be added to the containment hierarchy", (String)childNode.getProperty(Constants.PROPERTY_NAME)));
                
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                   for (Node subclassNode : Util.getAllSubclasses(childNode)){
                       for (ClassMetadataLight possibleSpecialChild : currentPossibleSpecialChildren){
                            if (possibleSpecialChild.getId() == subclassNode.getId())
                                throw new InvalidArgumentException(String.format("A subclass of %s is already a possible special child for instances of %s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
                       }
                   }
                }
                else {
                    for (ClassMetadataLight possibleSpecialChild : currentPossibleSpecialChildren){
                        if (possibleSpecialChild.getId() == childNode.getId())
                            throw new InvalidArgumentException(String.format("Class %s is already a possible special child for instances of %s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
                    }
                }
                parentNode.createRelationshipTo(childNode, RelTypes.POSSIBLE_SPECIAL_CHILD);
                                
                //Refresh cache
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                    for (Node subClassNode : Util.getAllSubclasses(childNode)) {
                        if (!(boolean)subClassNode.getProperty(Constants.PROPERTY_ABSTRACT))
                            cm.putPossibleSpecialChild((String) parentNode.getProperty(Constants.PROPERTY_NAME), (String) subClassNode.getProperty(Constants.PROPERTY_NAME));
                    }
                } else
                    cm.putPossibleSpecialChild((String)parentNode.getProperty(Constants.PROPERTY_NAME), (String)childNode.getProperty(Constants.PROPERTY_NAME));
                
                tx.success();
            }
        }
    }

    @Override
    public void addPossibleChildren(String parentClassName, String[] possibleChildren) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        Node parentNode;

        try(Transaction tx = graphDb.beginTx()) {
            if(parentClassName != null) {
                parentNode = classIndex.get(Constants.PROPERTY_NAME, parentClassName).getSingle();

                if (parentNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find class %s", parentClassName));
                
                if (!isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format("%s is not a business class, thus can not be added to the containment hierarchy", (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
            } else {
                parentNode = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();

                if(!(Constants.NODE_DUMMYROOT).equals((String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                        throw new MetadataObjectNotFoundException("DummyRoot node is corrupted");
            }
            List<ClassMetadataLight> currentPossibleChildren = refreshPossibleChildren(parentNode);
        
            for (String possibleChildName : possibleChildren) {
                Node childNode = classIndex.get(Constants.PROPERTY_NAME, possibleChildName).getSingle();
                if (childNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find class %s", possibleChildName));
                
                if (!isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)childNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format("%s is not a business class, thus can not be added to the containment hierarchy", (String)childNode.getProperty(Constants.PROPERTY_NAME)));
                
                if ((boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                   for (Node subclassNode : Util.getAllSubclasses(childNode)){
                       for (ClassMetadataLight possibleChild : currentPossibleChildren){
                            if (possibleChild.getId() == subclassNode.getId())
                                throw new InvalidArgumentException(String.format("A subclass of %s is already a possible child for instances of %s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
                       }
                   }
                }
                else {
                    for (ClassMetadataLight possibleChild : currentPossibleChildren){
                        if (possibleChild.getId() == childNode.getId())
                            throw new InvalidArgumentException(String.format("Class %s is already a possible child for instances of %s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
                    }
                }
                parentNode.createRelationshipTo(childNode, RelTypes.POSSIBLE_CHILD);
                //Refresh cache
                if ((boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                    for(ClassMetadataLight subclass : getSubClassesLight((String)childNode.getProperty(Constants.PROPERTY_NAME), false, false))
                        cm.putPossibleChild(parentClassName,subclass.getName());
                }
                else
                    cm.putPossibleChild(parentClassName, (String)childNode.getProperty(Constants.PROPERTY_NAME));
                tx.success();
            }
        }
    }
    
    @Override
    public void addPossibleSpecialChildren(String parentClassName, String[] possibleSpecialChildren) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        Node parentNode;

        try(Transaction tx = graphDb.beginTx()) {
            if(parentClassName != null) {
                parentNode = classIndex.get(Constants.PROPERTY_NAME, parentClassName).getSingle();

                if (parentNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find class %s", parentClassName));
                
                if (!isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format("%s is not a business class, thus can not be added to the containment hierarchy", (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
            } else {
                parentNode = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();

                if(!(Constants.NODE_DUMMYROOT).equals((String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                        throw new MetadataObjectNotFoundException("DummyRoot node is corrupted");
            }
            List<ClassMetadataLight> currentPossibleSpecialChildren = refreshPossibleSpecialChildren(parentNode);
        
            for (String possibleSpecialChildName : possibleSpecialChildren) {
                Node childNode = classIndex.get(Constants.PROPERTY_NAME, possibleSpecialChildName).getSingle();
                if (childNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find class %s", possibleSpecialChildName));
                
                if (!isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)childNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format("%s is not a business class, thus can not be added to the special containment hierarchy", (String)childNode.getProperty(Constants.PROPERTY_NAME)));
                
                if ((boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)) {
                   for (Node subclassNode : Util.getAllSubclasses(childNode)){
                       for (ClassMetadataLight possibleChild : currentPossibleSpecialChildren){
                            if (possibleChild.getId() == subclassNode.getId())
                                throw new InvalidArgumentException(String.format("A subclass of %s is already a possible special child for instances of %s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
                       }
                   }
                }
                else {
                    for (ClassMetadataLight possibleSpecialChild : currentPossibleSpecialChildren){
                        if (possibleSpecialChild.getId() == childNode.getId())
                            throw new InvalidArgumentException(String.format("Class %s is already a possible special child for instances of %s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
                    }
                }
                parentNode.createRelationshipTo(childNode, RelTypes.POSSIBLE_SPECIAL_CHILD);
                
                //Refresh cache
                if ((boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                    for(ClassMetadataLight subclass : getSubClassesLight((String)childNode.getProperty(Constants.PROPERTY_NAME), false, false))
                        cm.putPossibleSpecialChild(parentClassName,subclass.getName());
                }
                else
                    cm.putPossibleSpecialChild(parentClassName, (String)childNode.getProperty(Constants.PROPERTY_NAME));
                
                tx.success();
            }
        }
    }
    
    @Override
    public void removePossibleChildren(long parentClassId, long[] childrenToBeRemoved) 
            throws MetadataObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode;
            if (parentClassId == -1){
                parentNode = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();
                if (parentNode == null)
                    throw new MetadataObjectNotFoundException("DummyRoot is corrupted");
            }
            else {
                parentNode = classIndex.get(Constants.PROPERTY_ID, parentClassId).getSingle();
                if (parentNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find a class with id %s", parentClassId));
            }
            for (long id : childrenToBeRemoved) {
                Node childNode = classIndex.get(Constants.PROPERTY_ID, id).getSingle();
                Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.POSSIBLE_CHILD, Direction.OUTGOING);

                for (Relationship rel: relationships) {
                    Node possiblechild = rel.getEndNode();
                    if (childNode.getId() == possiblechild.getId())
                        rel.delete();
                }//end for
            }//end for
            refreshPossibleChildren(parentNode);
            tx.success();
        }
    }
    
    @Override
    public void removePossibleSpecialChildren(long parentClassId, long[] specialChildrenToBeRemoved) 
            throws MetadataObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode;
            if (parentClassId == -1){
                parentNode = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();
                if (parentNode == null)
                    throw new MetadataObjectNotFoundException("DummyRoot is corrupted");
            }
            else {
                parentNode = classIndex.get(Constants.PROPERTY_ID, parentClassId).getSingle();
                if (parentNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find a class with id %s", parentClassId));
            }
            for (long id : specialChildrenToBeRemoved) {
                Node childNode = classIndex.get(Constants.PROPERTY_ID, id).getSingle();
                Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.POSSIBLE_SPECIAL_CHILD, Direction.OUTGOING);

                for (Relationship rel: relationships) {
                    Node possiblechild = rel.getEndNode();
                    if (childNode.getId() == possiblechild.getId())
                        rel.delete();
                }//end for
            }//end for
            refreshPossibleSpecialChildren(parentNode);
            tx.success();
        }
        
    }

    @Override
    public List<ClassMetadataLight> getUpstreamContainmentHierarchy(String className, 
            boolean recursive) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> res = new ArrayList<>();
        try(Transaction tx = graphDb.beginTx()) {
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (classNode == null){
               throw new MetadataObjectNotFoundException(String.format(
                            "Can not find class %s", className));
            }

            String cypherQuery = "START classNode=node:classes(name=\""+className+"\") "+
                                 "MATCH possibleParentClassNode-[:POSSIBLE_CHILD"+(recursive ? "*" : "")+ "]->classNode "+
                                 "WHERE possibleParentClassNode.name <> \""+ Constants.NODE_DUMMYROOT +
                                 "\" RETURN distinct possibleParentClassNode "+
                                 "ORDER BY possibleParentClassNode.name ASC";

            Result result = graphDb.execute(cypherQuery);

            Iterator<Node> directPossibleChildren = result.columnAs("possibleParentClassNode"); //NOI18N
            for (Node node : IteratorUtil.asIterable(directPossibleChildren))
                res.add(Util.createClassMetadataLightFromNode(node));
        }
        return res;
    }
    
    @Override
    public List<ClassMetadataLight> getUpstreamSpecialContainmentHierarchy(String className, 
            boolean recursive) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> res = new ArrayList<>();
        try(Transaction tx = graphDb.beginTx()) {
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (classNode == null){
               throw new MetadataObjectNotFoundException(String.format(
                            "Can not find class %s", className));
            }

            String cypherQuery = "START classNode=node:classes(name=\""+className+"\") "+
                                 "MATCH possibleParentClassNode-[:POSSIBLE_SPECIAL_CHILD"+ (recursive ? "*" : "") + "]->classNode "+
                                 "WHERE possibleParentClassNode.name <> \""+ Constants.NODE_DUMMYROOT +
                                 "\" RETURN distinct possibleParentClassNode "+
                                 "ORDER BY possibleParentClassNode.name ASC";

            Result result = graphDb.execute(cypherQuery);

            Iterator<Node> directPossibleChildren = result.columnAs("possibleParentClassNode"); //NOI18N
            for (Node node : IteratorUtil.asIterable(directPossibleChildren))
                res.add(Util.createClassMetadataLightFromNode(node));
        }
        return res;
    }
    
    @Override
    public void setSpecialRelationshipDisplayName(String relationshipName, String relationshipDisplayName) {
        relationshipDisplayNames.put(relationshipName, relationshipDisplayName);
    }
    
    @Override
    public String getSpecialRelationshipDisplayName(String relationshipName) {
        String displayName = relationshipDisplayNames.get(relationshipName);
        return displayName == null ? relationshipName : displayName;
    }
     
    @Override
    public boolean isSubClass(String allegedParent, String classToBeEvaluated) throws MetadataObjectNotFoundException {
        if (classToBeEvaluated == null)
            return false;

        ClassMetadata currentClass = getClass(classToBeEvaluated);

        if (allegedParent.equals(classToBeEvaluated))
            return true;

        if (currentClass.getParentClassName() == null)
            return false;

        if (currentClass.getParentClassName().equals(allegedParent))
            return true;
        else
            return isSubClass(allegedParent, currentClass.getParentClassName());
    }
    
    /**
     * HELPERS
     */
    //Callers must handle associated transactions
   private void refreshCacheOn(Node rootClassNode){
        TraversalDescription UPDATE_TRAVERSAL = graphDb.traversalDescription().
                    breadthFirst().relationships(RelTypes.EXTENDS, Direction.INCOMING);

        for(Path p : UPDATE_TRAVERSAL.traverse(rootClassNode)){
            cm.removeClass((String)p.endNode().getProperty(Constants.PROPERTY_NAME));
            ClassMetadata rootClassMetadata = Util.createClassMetadataFromNode(p.endNode());
            cm.putClass(rootClassMetadata);            
                        
            refreshPossibleChildren(p.endNode());
            refreshPossibleSpecialChildren(p.endNode());
        }
    }
        
   //Callers must handle associated transactions
   private void buildClassCache() throws InvalidArgumentException {
        cm.clearClassCache();
        
        for (Node classNode : classIndex.query(Constants.PROPERTY_ID, "*")) {
             ClassMetadata aClass = Util.createClassMetadataFromNode(classNode);
             cm.putClass(aClass);
             
             refreshPossibleChildren(classNode);
             refreshPossibleSpecialChildren(classNode);
        }
        loadUniqueAttributesCache();
        //Only the DummyRoot is not cached. It will be cached on demand later
   }
   
    private List<ClassMetadataLight> refreshPossibleChildren(Node classNode) {
        String className = (String) classNode.getProperty(Constants.PROPERTY_NAME);

        if (cm.getPossibleChildren(className) != null)
            cm.getPossibleChildren(className).clear();
        else
            cm.putPossibleChildren(className, new ArrayList());

        List<ClassMetadataLight> possibleChildren = new ArrayList();

        for (Relationship relationship : classNode.getRelationships(Direction.OUTGOING, RelTypes.POSSIBLE_CHILD)) {
            if ((Boolean) relationship.getEndNode().getProperty(Constants.PROPERTY_ABSTRACT)) {
                Iterable<Node> allSubclasses = Util.getAllSubclasses(relationship.getEndNode());
                for (Node childNode : allSubclasses) {
                    if (!(Boolean) childNode.getProperty(Constants.PROPERTY_ABSTRACT)) {
                        cm.putPossibleChild(className, (String) childNode.getProperty(Constants.PROPERTY_NAME));
                        possibleChildren.add(Util.createClassMetadataLightFromNode(childNode));
                   }
               }
            } else {
                cm.putPossibleChild(className, (String) relationship.getEndNode().getProperty(Constants.PROPERTY_NAME));
                possibleChildren.add(Util.createClassMetadataLightFromNode(relationship.getEndNode()));
           }
       } 
       return possibleChildren;
    }
    
    private List<ClassMetadataLight> refreshPossibleSpecialChildren(Node classNode) {
        String className = (String) classNode.getProperty(Constants.PROPERTY_NAME);
        
        if (cm.getPossibleSpecialChildren(className) != null)
            cm.getPossibleSpecialChildren(className).clear();
        else
            cm.putPossibleSpecialChildren(className, new ArrayList());
        
        List<ClassMetadataLight> possibleSpecialChildren = new ArrayList();
        
        for (Relationship relationship : classNode.getRelationships(Direction.OUTGOING, RelTypes.POSSIBLE_SPECIAL_CHILD)) {
            
            if ((Boolean) relationship.getEndNode().getProperty(Constants.PROPERTY_ABSTRACT)) {
                Iterable<Node> allSubclasses = Util.getAllSubclasses(relationship.getEndNode());
                for (Node childNode : allSubclasses) {
                    if (!(Boolean) childNode.getProperty(Constants.PROPERTY_ABSTRACT)) {
                        cm.putPossibleSpecialChild(className, (String) childNode.getProperty(Constants.PROPERTY_NAME));
                        possibleSpecialChildren.add(Util.createClassMetadataLightFromNode(childNode));
                    }
                }
            } else {
                cm.putPossibleSpecialChild(className, (String) relationship.getEndNode().getProperty(Constants.PROPERTY_NAME));
                possibleSpecialChildren.add(Util.createClassMetadataLightFromNode(relationship.getEndNode()));
            }
       }
       return possibleSpecialChildren;
   }
   
   
   /**
    * Checks if all the objects of a given class has a value in a given attribute marked as mandatory
    * this method can also check all the objects from the subclasses of the given class.
    * @param className The object's class 
    * @param attributeName The object's attribute marked as mandatory
    * @param recursive false: if the method should evaluate all the objects of 
    * the class, true if also should evaluate all the objects of the subclasses
    * @return true if every object has a value in the attribute, false if at 
    * least one object has no value in the attribute marked as mandatory
    */
    private boolean objectsOfClassHasValueInMandatoryAttribute(String className, 
            String attributeName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException
    {
        Node classNode = classIndex.get(Constants.PROPERTY_NAME,className).getSingle();
        String attributeType = Util.createClassMetadataFromNode(classNode).getAttribute(attributeName).getType();
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s", className));
        
        if (!objectsHasAttribute(classNode, attributeName, attributeType))// check every object for the given class
            return false;
        //then check every object of the subclasses of the given class
        String cypherQuery = "START inventory = node:classes({className}) ".concat(
                             "MATCH inventory <-[:").concat(RelTypes.EXTENDS.toString()).concat("*]-classmetadata ").concat(
                             "RETURN classmetadata ").concat(
                             "ORDER BY classmetadata.name ASC");

        Map<String, Object> params = new HashMap<>();
        params.put("className", "name:"+ className);//NOI18N
        
        Result result = graphDb.execute(cypherQuery, params);
        Iterator<Node> n_column = result.columnAs("classmetadata");
        for (Node nodeClass : IteratorUtil.asIterable(n_column))
            return objectsHasAttribute(nodeClass, attributeName, attributeType);
        
        return true;
    }
    
    /**
     * Checks if all the instances of a class has a value in a given attribute
     * @param classNode the class node
     * @param attributeName name of the attribute
     * @param attributeType type of the attribute
     * @return true if the given attribute has a value
     * @throws InvalidArgumentException 
     */
    private boolean objectsHasAttribute(Node classNode, String attributeName, String attributeType) throws InvalidArgumentException{
        
        boolean everyObjectHasValue = true;
        Iterable<Relationship> iterableInstances = classNode.getRelationships(RelTypes.INSTANCE_OF, Direction.INCOMING);
        Iterator<Relationship> instances = iterableInstances.iterator();
        //check if attribute is a pirmitive type, a property in the object node
        while (instances.hasNext()){
            Node objectNode = instances.next().getStartNode();
            if(AttributeMetadata.isPrimitive(attributeType)){
                if(!objectNode.hasProperty(attributeName)){
                    everyObjectHasValue = false;
                    break;    
                }
            }
            else{//checks if the attribute is a list type property
                //Iterates through relationships and transform the into "plain" attributes
                everyObjectHasValue = false;
                Iterable<Relationship> iterableRelationships = objectNode.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING);
                Iterator<Relationship> relationships = iterableRelationships.iterator();
                if(!relationships.hasNext())//if has no attribute
                    return false;
                else{
                    while(relationships.hasNext()){
                        Relationship relationship = relationships.next();
                        if (!relationship.hasProperty(Constants.PROPERTY_NAME))
                            throw new InvalidArgumentException(String.format("Mandatory Attribute Missing: The object with id %s is malformed", objectNode.getId()));

                        if (attributeName.equals((String)relationship.getProperty(Constants.PROPERTY_NAME)))
                            everyObjectHasValue = true;
                    }
                }
            }
        }
        return everyObjectHasValue;
    }
    
    /**
     * Check if the value of the given attribute name is unique across other 
     * objects of the classes and subclasses in the inventory
     * @param className class name
     * @param attributeType attribute type
     * @param attributeName attribute name
     * @return true if every object of this class or its subclasses has a unique attribute value
     * @throws MetadataObjectNotFoundException
     * @throws InvalidArgumentException 
     */
    private boolean canAttributeBeUnique(Node classNode, String attributeType, String attributeName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException{
        
        List<String> values =  new ArrayList<>();
        String uniqueAttributeValue = null;
        HashMap<String, List<String>> uniqueValues = new HashMap<>();
        String className = (String)classNode.getProperty(Constants.PROPERTY_NAME);
        
        //List<ClassMetadataLight> subClassesLight = getSubClassesLight(className, false, true);
        List<ClassMetadataLight> subClassesLight = getSubClassesLight(classNode, false); //Uses this one to avoid the transaction
        subClassesLight.add(Util.createClassMetadataLightFromNode(classNode)); //Add itself, since getSubClassesLight(Node, boolean) does not do it on its own
        
        for(ClassMetadataLight subClassLight : subClassesLight){
            Node subClassNode = classIndex.get(Constants.PROPERTY_NAME, subClassLight.getName()).getSingle();
            ClassMetadata subClassMetadata = createClassMetadataFromNode(subClassNode);
            if(subClassMetadata.hasAttribute(attributeName)){
                if(subClassNode.hasRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.INCOMING) ||
                        subClassNode.hasRelationship(RelTypes.INSTANCE_OF, Direction.INCOMING)){
                    Iterable<Relationship> iterableRelationships = subClassNode.getRelationships(RelTypes.INSTANCE_OF, Direction.INCOMING);
                    Iterator<Relationship> relationships = iterableRelationships.iterator();
                    if(attributeType.equals(Util.getTypeOfAttribute(subClassNode, attributeName))){
                        while(relationships.hasNext()){
                            Relationship relationship = relationships.next();
                            RemoteBusinessObject remoteObject = Util.createRemoteObjectFromNode(relationship.getStartNode());
                            if(remoteObject.getAttributes().get(attributeName) != null)
                                uniqueAttributeValue = remoteObject.getAttributes().get(attributeName).get(0);
                            if(values.contains(uniqueAttributeValue))
                                return false;
                            else{
                                values.add(uniqueAttributeValue);
                                uniqueValues.put(className, values); //Put the className because the attributeName doesn't changes
                            }
                        }
                    }
                }
            }
        }
        for(String _className : uniqueValues.keySet())
            cm.putUniqueAttributeValuesIndex(className, attributeName, uniqueValues.get(_className));
        return true;
    }
    
    /**
     * Loads into cache the unique attributes of every class and if there are 
     * instances of the classes also saves the values of the unique attributes
     * @throws InvalidArgumentException if the attribute name doesn't exists
     */
    private void loadUniqueAttributesCache() throws InvalidArgumentException{
        
        Node inventoryObject = classIndex.get(Constants.PROPERTY_NAME, Constants.CLASS_INVENTORYOBJECT).getSingle();
        
        TraversalDescription td = new MonoDirectionalTraversalDescription(); //TODO Check this!
        td = td.breadthFirst();
        td = td.relationships(RelTypes.EXTENDS, Direction.INCOMING);
        org.neo4j.graphdb.traversal.Traverser traverse = td.traverse(inventoryObject);
        
        for (Node subClassNode : traverse.nodes()) {
            ClassMetadata subClassMetadata = createClassMetadataFromNode(subClassNode);
            for(AttributeMetadata attribute :subClassMetadata.getAttributes()){
                if(attribute.isUnique()){
                    if(subClassNode.hasRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.INCOMING) ||
                        subClassNode.hasRelationship(RelTypes.INSTANCE_OF, Direction.INCOMING)){
                        Iterable<Relationship> iterableRelationships = subClassNode.getRelationships(RelTypes.INSTANCE_OF, Direction.INCOMING);
                        Iterator<Relationship> relationships = iterableRelationships.iterator();
                        while(relationships.hasNext()){
                            Relationship relationship = relationships.next();
                            RemoteBusinessObject remoteObject = Util.createRemoteObjectFromNode(relationship.getStartNode());
                            if(remoteObject.getAttributes().get(attribute.getName()) != null)
                                cm.putUniqueAttributeValueIndex(subClassMetadata.getName(), 
                                        attribute.getName(), 
                                        remoteObject.getAttributes().get(attribute.getName()).get(0));
                        }
                    }
                }
            }
        }//end for 
    }
    
    /**
     * Fetches recursively all the subclasses of a given class without using a transaction
     * @param classNode The class node to start the search
     * @param includeAbstractClasses should abstract classes be included in the result?
     * @return The list of the recursive subclasses, given the filters <code>includeAbstractClasses</code>
     */
    private List<ClassMetadataLight> getSubClassesLight(Node classNode, boolean includeAbstractClasses) {
        List<ClassMetadataLight> res = new ArrayList<>();
        for (Relationship inheritanceRelationship : classNode.getRelationships(Direction.INCOMING, RelTypes.EXTENDS)) {
            Node subClassNode = inheritanceRelationship.getStartNode();
            if (subClassNode.hasProperty(Constants.PROPERTY_ABSTRACT) && includeAbstractClasses) {
                res.add(Util.createClassMetadataLightFromNode(subClassNode));
                res.addAll(getSubClassesLight(subClassNode, includeAbstractClasses));
            }
        }
        return res;
    }
}    
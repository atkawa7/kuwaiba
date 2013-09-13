/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.persistenceservice.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
import org.kuwaiba.apis.persistence.interfaces.MetadataEntityManager;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.InterfaceMetadata;
import org.kuwaiba.persistenceservice.caching.CacheManager;
import org.kuwaiba.persistenceservice.util.Constants;
import org.kuwaiba.persistenceservice.util.Util;
import org.kuwaiba.psremoteinterfaces.MetadataEntityManagerRemote;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.EmbeddedGraphDatabase;

/**
 * MetadataEntityManager implementation
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class MetadataEntityManagerImpl implements MetadataEntityManager, MetadataEntityManagerRemote {
    /**
     * Reference to the db handle
     */
    private EmbeddedGraphDatabase graphDb;
    /**
     * Class index
     */
    private Index<Node> classIndex;
    /**
     * Category index
     */
    private Index<Node> categoryIndex;
    /**
     * Reference to the CacheManager
     */
    private CacheManager cm;

    private MetadataEntityManagerImpl() {
        cm = CacheManager.getInstance();
    }

    /**
     * Constructor
     * Get the a database connection and indexes from the connection manager.
     */
    public MetadataEntityManagerImpl(ConnectionManager cmn) {
        this();
        graphDb = (EmbeddedGraphDatabase) cmn.getConnectionHandler();
        classIndex = graphDb.index().forNodes(Constants.INDEX_CLASS);
        categoryIndex = graphDb.index().forNodes(Constants.INDEX_CATEGORY);
        cm.clear();
        for (Node classNode : classIndex.query(Constants.PROPERTY_ID, "*")){
            ClassMetadata aClass = Util.createClassMetadataFromNode(classNode);
            cm.putClass(aClass);
            cm.putPossibleChildren(aClass.getName(), aClass.getPossibleChildren());
        }
        //TODO: Take this out of here, let the nav tree root possible children to be cached at getPossibleChildren
        try{
            List<String> possibleChildrenOfRoot = new ArrayList<String>();
            for (ClassMetadataLight aClass : getPossibleChildren(null))
                possibleChildrenOfRoot.add(aClass.getName());
            cm.putPossibleChildren("", possibleChildrenOfRoot);
        }catch(Exception e){}
    }

    /**
     * Creates a class metadata entry with its attributes(some new attributes and others
     * extended from the parent) and a category (if the category does not exist it will be created).
     * @param classDefinition. If the parent class is null, the node to be created will be interpreted as the RootObject or its equivalent
     * @return the Id of the newClassMetadata
     * @throws MetadataObjectNotFoundException if there's no a parent class with the identifier provided
     * @throws DatabaseException if the reference node does not exist
     */
    @Override
    public long createClass(ClassMetadata classDefinition) throws MetadataObjectNotFoundException, DatabaseException {
        Transaction tx = null;
        long id;
        classDefinition = Util.setDefaultsForClassMetadata(classDefinition);
        try {
            tx = graphDb.beginTx();
            if (graphDb.getReferenceNode() == null){//The reference node must exist
                throw new DatabaseException("Reference node does not exist. The database seems to be corrupted");
            }
            if (classIndex.get(Constants.PROPERTY_NAME, classDefinition.getName()).getSingle() != null){
                throw new InvalidArgumentException(String.format("Class %1s already exists in the database", classDefinition.getName()), Level.INFO);
            }
            Node classNode = graphDb.createNode();

            classNode.setProperty(Constants.PROPERTY_NAME, classDefinition.getName());
            classNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, classDefinition.getDisplayName());
            classNode.setProperty(Constants.PROPERTY_CUSTOM, classDefinition.isCustom());
            classNode.setProperty(Constants.PROPERTY_COUNTABLE, classDefinition.isCountable());
            classNode.setProperty(Constants.PROPERTY_COLOR, classDefinition.getColor());
            classNode.setProperty(Constants.PROPERTY_DESCRIPTION, classDefinition.getDescription());
            classNode.setProperty(Constants.PROPERTY_ABSTRACT, classDefinition.isAbstract());
            classNode.setProperty(Constants.PROPERTY_ICON, classDefinition.getIcon());
            classNode.setProperty(Constants.PROPERTY_SMALL_ICON, classDefinition.getSmallIcon());
            classNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            classNode.setProperty(Constants.PROPERTY_IN_DESIGN, classDefinition.isInDesing());

            id = classNode.getId();
            classIndex.putIfAbsent(classNode, Constants.PROPERTY_NAME, classDefinition.getName());
            classIndex.putIfAbsent(classNode, Constants.PROPERTY_ID, classNode.getId());
            //Is this class the root of all class hierarchy
            if (classDefinition.getParentClassName() == null){
                if (graphDb.getReferenceNode().getSingleRelationship(RelTypes.ROOT, Direction.BOTH) == null){
                    classNode.createRelationshipTo(graphDb.getReferenceNode(), RelTypes.ROOT);
                }
                else{
                    throw new MetadataObjectNotFoundException("Parent class can not be null, or if it is, only one root class is permitted");
                }
            }
            else { //Category
                if (classDefinition.getCategory() != null) //if the category already exists
                { 
                    Node ctgrNode = categoryIndex.get(Constants.PROPERTY_NAME, classDefinition.getCategory().getName()).getSingle();
                    if (ctgrNode == null) {
                        long ctgrId = createCategory(classDefinition.getCategory());
                        ctgrNode = categoryIndex.get(Constants.PROPERTY_ID, ctgrId).getSingle();
                    }
                    classNode.createRelationshipTo(ctgrNode, RelTypes.BELONGS_TO_GROUP);
                }//end if is category null
                Node parentNode = classIndex.get(Constants.PROPERTY_NAME, classDefinition.getParentClassName()).getSingle();
                if (parentNode != null) {
                    classNode.createRelationshipTo(parentNode, RelTypes.EXTENDS);
                    Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
                    //Set extendended attributes from parent
                    for (Relationship rel : relationships) {
                        Node parentAttrNode = rel.getEndNode();
                        Node newAttrNode = graphDb.createNode();
                        newAttrNode.setProperty(Constants.PROPERTY_NAME, parentAttrNode.getProperty(Constants.PROPERTY_NAME));
                        newAttrNode.setProperty(Constants.PROPERTY_DESCRIPTION, parentAttrNode.getProperty(Constants.PROPERTY_DESCRIPTION));
                        newAttrNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, parentAttrNode.getProperty(Constants.PROPERTY_DISPLAY_NAME));
                        newAttrNode.setProperty(Constants.PROPERTY_TYPE, parentAttrNode.getProperty(Constants.PROPERTY_TYPE));
                        newAttrNode.setProperty(Constants.PROPERTY_READONLY, parentAttrNode.getProperty(Constants.PROPERTY_READONLY));
                        newAttrNode.setProperty(Constants.PROPERTY_VISIBLE, parentAttrNode.getProperty(Constants.PROPERTY_VISIBLE));
                        newAttrNode.setProperty(Constants.PROPERTY_ADMINISTRATIVE, parentAttrNode.getProperty(Constants.PROPERTY_ADMINISTRATIVE));
                        newAttrNode.setProperty(Constants.PROPERTY_MAPPING, parentAttrNode.getProperty(Constants.PROPERTY_MAPPING));
                        newAttrNode.setProperty(Constants.PROPERTY_NO_COPY, parentAttrNode.getProperty(Constants.PROPERTY_NO_COPY));
                        newAttrNode.setProperty(Constants.PROPERTY_UNIQUE, parentAttrNode.getProperty(Constants.PROPERTY_UNIQUE));
                        //newAttrNode.setProperty(PROPERTY_LOCKED, parentAttrNode.getProperty(PROPERTY_LOCKED));
                        classNode.createRelationshipTo(newAttrNode, RelTypes.HAS_ATTRIBUTE);
                    }
                }//end if there is a Parent
                else{
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find parent class with name %1s", classDefinition.getParentClassName()));
                }
            }//end else not rootNode
            //Attributes
            if (classDefinition.getAttributes() != null) {
                for (AttributeMetadata at : classDefinition.getAttributes()) {
                    if (getAttribute(classDefinition.getName(), at.getName()) == null){
                        addAttribute(id, at);
                    }
                }
            }
            tx.success();
            return id;
        } catch (Exception ex) {
            Logger.getLogger("Create class: "+ex.getMessage()); //NOI18N
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null){
                tx.finish();
            }
        }
    }

    /**
     * Changes a class metadata definition
     * @param newClassDefinition
     * @return true if success
     * @throws ClassNotFoundException if there is no class with such classId
     */
    @Override
    public void setClassProperties (ClassMetadata newClassDefinition) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node classMetadata = classIndex.get(Constants.PROPERTY_ID, newClassDefinition.getId()).getSingle();
            if (classMetadata == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %1s", newClassDefinition.getName()));
            
            String formerName = (String)classMetadata.getProperty(Constants.PROPERTY_NAME);
            
            if(newClassDefinition.getName() != null){
                classIndex.remove(classMetadata, Constants.PROPERTY_NAME);
                classMetadata.setProperty(Constants.PROPERTY_NAME, newClassDefinition.getName());
                classIndex.add(classMetadata, Constants.PROPERTY_NAME, newClassDefinition.getName());
            }
            if(newClassDefinition.getDisplayName() != null)
                classMetadata.setProperty(Constants.PROPERTY_DISPLAY_NAME, newClassDefinition.getDisplayName());
            if(newClassDefinition.getDescription() != null)
                classMetadata.setProperty(Constants.PROPERTY_DESCRIPTION, newClassDefinition.getDescription());
            if(newClassDefinition.getIcon() != null)
                classMetadata.setProperty(Constants.PROPERTY_ICON, newClassDefinition.getIcon());
            if(newClassDefinition.getSmallIcon() != null)
                classMetadata.setProperty(Constants.PROPERTY_SMALL_ICON, newClassDefinition.getSmallIcon());
            if(newClassDefinition.getColor() != -1)
                classMetadata.setProperty(Constants.PROPERTY_COLOR, newClassDefinition.getColor());
            classMetadata.setProperty(Constants.PROPERTY_COUNTABLE, newClassDefinition.isCountable());
            classMetadata.setProperty(Constants.PROPERTY_ABSTRACT, newClassDefinition.isAbstract());
            classMetadata.setProperty(Constants.PROPERTY_IN_DESIGN, newClassDefinition.isInDesing());
            classMetadata.setProperty(Constants.PROPERTY_CUSTOM, newClassDefinition.isCustom());
            
            if(newClassDefinition.getAttributes() != null ){
                for (AttributeMetadata attr : newClassDefinition.getAttributes()) {
                    setAttributeProperties(newClassDefinition.getId(), attr);
                }
            }
            
            cm.removeClass(formerName);
            cm.putClass(Util.createClassMetadataFromNode(classMetadata));
            tx.success();
        }catch(Exception ex){
            Logger.getLogger("Change class definition: "+ex.getMessage()); //NOI18N
            if(tx != null){
                tx.failure();
            }
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null){
                tx.finish();
            }
        }
    }

    /**
     * Deletes a class metadata entry, its attributes and category relationships
     * @param classId
     * @throws ClassNotFoundException if there is not a class with de ClassId
     */
    @Override
    public void deleteClass(long classId) throws MetadataObjectNotFoundException, InvalidArgumentException {
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node node = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();

            if (node == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %1s", classId));
            
            if (!(Boolean)node.getProperty(Constants.PROPERTY_CUSTOM))
                throw new InvalidArgumentException(String.format(
                        "Core classes can not be deleted"), Level.SEVERE);
            if (node.hasRelationship(RelTypes.INSTANCE_OF))
                throw new InvalidArgumentException(String.format(
                        "The class with id %1s has instances and can not be deleted", classId), Level.SEVERE);
            
            if (node.hasRelationship(Direction.INCOMING, RelTypes.EXTENDS))
                throw new InvalidArgumentException(String.format(
                        "The class with id %1s has subclasses and can not be deleted", classId), Level.SEVERE);
            
            //Deletes the attribute nodes and their relationships to the class node
            Iterable<Relationship> attRelationships = node.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship rel : attRelationships){
                rel.getEndNode().getSingleRelationship(RelTypes.HAS_ATTRIBUTE, Direction.INCOMING).delete();
                rel.getEndNode().delete();
            }
            
            //Release the rest of relationships
            for (Relationship rel : node.getRelationships())
                rel.delete();
            
            classIndex.remove(node);
            node.delete();
            tx.success();

        } catch(Exception ex){
            Logger.getLogger("Delete class: "+ex.getMessage()); //NOI18N
            if(tx != null){
                tx.failure();
            }
            throw new RuntimeException(ex.getMessage());
        } finally {
            if (tx != null)
                tx.finish();
        }
    }

    /**
     * Deletes a classmetadata, its attributes and category relationships
     * @param className
     * @return true if success
     * @throws ClassNotFoundException if there is not a class with de ClassName
     */
    @Override
    public void deleteClass(String className) throws MetadataObjectNotFoundException {
                Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node node = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();

            if (node == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with name %1s", className));
            
            if (node.hasRelationship(RelTypes.INSTANCE_OF))
                throw new InvalidArgumentException(String.format(
                        "The class with name %1s has instances and can not be deleted", className), Level.SEVERE);
            
            if (node.hasRelationship(Direction.INCOMING, RelTypes.EXTENDS))
                throw new InvalidArgumentException(String.format(
                        "The class with name %1s has subclasses and can not be deleted", className), Level.SEVERE);
            
            //Deletes the attribute nodes and their relationships to the class node
            Iterable<Relationship> attRelationships = node.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship rel : attRelationships){
                rel.getEndNode().getSingleRelationship(RelTypes.HAS_ATTRIBUTE, Direction.INCOMING).delete();
                rel.getEndNode().delete();
            }
            
            //Release the rest of relationships
            for (Relationship rel : node.getRelationships())
                rel.delete();
            
            classIndex.remove(node);
            node.delete();
            tx.success();

        } catch(Exception ex){
            Logger.getLogger("Delete class: "+ex.getMessage()); //NOI18N
            if(tx != null){
                tx.failure();
            }
            throw new RuntimeException(ex.getMessage());
        } finally {
            if (tx != null)
                tx.finish();
        }
    }

    /**
     * Retrieves the simplified list of classes
     * @param includeListTypes boolean to indicate if the list should include
     * the subclasses of GenericObjectList
     * @return the list of classes
     * @throws Exception EntityManagerNotAvailableException or something unexpected
     */
    @Override
    public List<ClassMetadataLight> getAllClassesLight(boolean includeListTypes, boolean includeIndesign) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> cml = new ArrayList<ClassMetadataLight>();
        try {
            Node myClassNode =  classIndex.get(Constants.PROPERTY_NAME, Constants.CLASS_INVENTORYOBJECT).getSingle();

            if(myClassNode == null){
                throw new MetadataObjectNotFoundException(String.format(
                         "Can not find a class with name %1s", Constants.CLASS_INVENTORYOBJECT));
            }
            String cypherQuery = "START inventory = node:classes({className}) ".concat(
                                 "MATCH inventory <-[:").concat(RelTypes.EXTENDS.toString()).concat("*]-classmetadata ").concat(
                                 "RETURN classmetadata,inventory ").concat(
                                 "ORDER BY classmetadata.name ASC");

            Map<String, Object> params = new HashMap<String, Object>();
            if(includeListTypes){
                params.put("className", "name:"+ Constants.CLASS_INVENTORYOBJECT+" name:"+Constants.CLASS_GENERICOBJECTLIST);//NOI18N
            }
            else{
                params.put("className", "name:"+ Constants.CLASS_INVENTORYOBJECT);//NOI18N
            }
            ExecutionEngine engine = new ExecutionEngine(graphDb);
            ExecutionResult result = engine.execute(cypherQuery, params);
            Iterator<Node> n_column = result.columnAs("classmetadata");
            //First, we inject the InventoryObject class (for some reason, the start node can't be retrieved as part of the path, so it can be sorted)
            Iterator<Node> roots = result.columnAs("inventory");
            cml.add(Util.createClassMetadataLightFromNode(roots.next()));

            for (Node node : IteratorUtil.asIterable(n_column)){
                 cml.add(Util.createClassMetadataLightFromNode(node));
            }
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
        return cml;
    }

    @Override
    public List<ClassMetadataLight> getSubClassesLight(String className, boolean includeAbstractClasses, boolean includeSelf) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        List<ClassMetadataLight> cml = new ArrayList<ClassMetadataLight>();
        try {
            
            ClassMetadata aClass = cm.getClass(className);
            if (aClass == null)
                throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %1s", className));
            
            String cypherQuery = "START inventory = node:classes({className}) ".concat(
                                 "MATCH inventory <-[:").concat(RelTypes.EXTENDS.toString()).concat("*]-classmetadata ").concat(
                                 includeAbstractClasses ? "" : "WHERE classmetadata.abstract <> true ").concat(
                                 "RETURN classmetadata ").concat(
                                 "ORDER BY classmetadata.name ASC");

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("className", "name:"+ className);//NOI18N

            ExecutionEngine engine = new ExecutionEngine(graphDb);
            ExecutionResult result = engine.execute(cypherQuery, params);
            Iterator<Node> n_column = result.columnAs("classmetadata");

            if (includeSelf && !aClass.isAbstract())
                cml.add(aClass);

            for (Node node : IteratorUtil.asIterable(n_column)){
                 cml.add(Util.createClassMetadataLightFromNode(node));
            }
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }

        return cml;
    }


    @Override
    public List<ClassMetadataLight> getSubClassesLightNoRecursive(String className, boolean includeAbstractClasses, boolean includeSelf) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        List<ClassMetadataLight> cml = new ArrayList<ClassMetadataLight>();
        try {
            
            ClassMetadata aClass = cm.getClass(className);
            if (aClass == null)
                throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %1s", className));
            
            String cypherQuery = "START inventory = node:classes({className}) ".concat(
                                 "MATCH inventory <-[:").concat(RelTypes.EXTENDS.toString()).concat("]-classmetadata ").concat(
                                 includeAbstractClasses ? "" : "WHERE classmetadata.abstract <> true ").concat(
                                 "RETURN classmetadata ").concat(
                                 "ORDER BY classmetadata.name ASC");

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("className", "name:"+ className);//NOI18N

            ExecutionEngine engine = new ExecutionEngine(graphDb);
            ExecutionResult result = engine.execute(cypherQuery, params);
            Iterator<Node> n_column = result.columnAs("classmetadata");

            if (includeSelf)
                cml.add(aClass);

            for (Node node : IteratorUtil.asIterable(n_column)){
                 cml.add(Util.createClassMetadataLightFromNode(node));
            }
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }

        return cml;
    }
    /**
     * Retrieves all the class metadata
     * @param includeListTypes boolean to indicate if the list should include
     * the subclasses of GenericObjectList
     * @return An array of classes
     */
    @Override
    public List<ClassMetadata> getAllClasses(boolean includeListTypes, boolean includeIndesign) throws MetadataObjectNotFoundException {
        List<ClassMetadata> cml = new ArrayList<ClassMetadata>();
        try {
            String cypherQuery = "START inventory = node:classes({className}) ".concat(
                                 "MATCH inventory <-[:").concat(RelTypes.EXTENDS.toString()).concat("*]-classmetadata ").concat(
                                 "RETURN classmetadata,inventory ").concat(
                                 "ORDER BY classmetadata.name ASC");

            Map<String, Object> params = new HashMap<String, Object>();
           if(includeListTypes){
                params.put("className", "name:"+ Constants.CLASS_INVENTORYOBJECT+" name:" + Constants.CLASS_GENERICOBJECTLIST);//NOI18N
           }
           else{
                params.put("className", "name:"+ Constants.CLASS_INVENTORYOBJECT);//NOI18N
           }
           ExecutionEngine engine = new ExecutionEngine(graphDb);
           ExecutionResult result = engine.execute(cypherQuery, params);
           Iterator<Node> n_column = result.columnAs("classmetadata");
           //First, we inject the InventoryObject class (for some reason, the start node can't be retrieved as part of the path, so it can be sorted)
           Iterator<Node> roots = result.columnAs("inventory");
           cml.add(Util.createClassMetadataFromNode(roots.next()));

           for (Node node : IteratorUtil.asIterable(n_column)){
                cml.add(Util.createClassMetadataFromNode(node));
           }
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
        return cml;
    }

    /**
     * Gets a classmetadata, its attributes and Category
     * @param classId
     * @return A ClassMetadata with the classId
     * @throws ClassNotFoundException there is no class with such classId
     */
    @Override
    public ClassMetadata getClass(long classId) throws MetadataObjectNotFoundException {
        ClassMetadata clmt = new ClassMetadata();
        try {
            Node node = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();
            if (node == null){
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %1s", classId));
            }
            clmt = Util.createClassMetadataFromNode(node);
        } catch(Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        return clmt;
    }

    /**
     * Gets a class metadata, its attributes and Category
     * @param className
     * @return A ClassMetadata with the className
     * @throws MetadataObjectNotFoundException there is no a class with such name
     */
    @Override
    public ClassMetadata getClass(String className) throws MetadataObjectNotFoundException {
        ClassMetadata clmt = new ClassMetadata();
        try {
            Node node = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (node == null){
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with name %1s", className));
            }
            clmt = Util.createClassMetadataFromNode(node);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        return clmt;
    }

    /**
     * Moves a class from one parentClass to an other parentClass
     * @param classToMoveName
     * @param targetParentClassName
     * @throws MetadataObjectNotFoundException if there is no a classToMove with such name
     * or if there is no a targetParentClass with such name
     */
    @Override
    public void moveClass(String classToMoveName, String targetParentClassName) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node ctm = classIndex.get(Constants.PROPERTY_NAME, classToMoveName).getSingle();
            Node tcn = classIndex.get(Constants.PROPERTY_NAME, targetParentClassName).getSingle();

            if (ctm == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class  with name %1s", classToMoveName));
            } else if (tcn == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with name %1s", targetParentClassName));
            } else {
                Relationship rel = ctm.getSingleRelationship(RelTypes.EXTENDS, Direction.OUTGOING);
                rel.delete();
                ctm.createRelationshipTo(tcn, RelTypes.EXTENDS);
            }
            tx.success();
        } catch(Exception ex){
            Logger.getLogger("moveClass: "+ex.getMessage()); //NOI18N
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        } finally {
            if( tx != null){
                tx.finish();
            }
        }
    }

    /**
     * Moves a class from one parentClass to an other parentClass
     * @param classToMoveId
     * @param targetParentClassId
     * @throws MetadataObjectNotFoundException if there is no a classToMove with such classId
     * or if there is no a targetParentClass with such classId
     */
    @Override
    public void moveClass(long classToMoveId, long targetParentClassId) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node ctm = classIndex.get(Constants.PROPERTY_ID, classToMoveId).getSingle();
            Node tcn = classIndex.get(Constants.PROPERTY_ID, targetParentClassId).getSingle();

            if (ctm == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %1s", classToMoveId));
            } else if (tcn == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %1s", targetParentClassId));
            } else {
                Relationship rel = ctm.getSingleRelationship(RelTypes.EXTENDS, Direction.OUTGOING);
                rel.delete();
                ctm.createRelationshipTo(tcn, RelTypes.EXTENDS);
            }
            tx.success();
        } catch(Exception ex){
            Logger.getLogger("moveClass: "+ex.getMessage()); //NOI18N
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null){
                tx.finish();
            }
        }
    }

    /**
     * Adds an attribute to the class
     * @param className
     * @param attributeDefinition
     * @throws MetadataObjectNotFoundException if there is no a class with such className
     */
    @Override
    public void addAttribute(String className, AttributeMetadata attributeDefinition) throws MetadataObjectNotFoundException, OperationNotPermittedException {
        attributeDefinition = Util.createDefaultAttributeMetadata(attributeDefinition);
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (classNode == null){
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with name %1s", className));
            }
            if(!Util.isAttributeName(classNode, attributeDefinition)){
                Node attrNode = graphDb.createNode();
                attrNode.setProperty(Constants.PROPERTY_NAME, attributeDefinition.getName());
                attrNode.setProperty(Constants.PROPERTY_DESCRIPTION, attributeDefinition.getDescription());
                attrNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, attributeDefinition.getDisplayName());
                attrNode.setProperty(Constants.PROPERTY_TYPE, attributeDefinition.getType());
                attrNode.setProperty(Constants.PROPERTY_MAPPING, attributeDefinition.getMapping());
                attrNode.setProperty(Constants.PROPERTY_READONLY, attributeDefinition.isVisible());
                attrNode.setProperty(Constants.PROPERTY_VISIBLE, attributeDefinition.isVisible());
                attrNode.setProperty(Constants.PROPERTY_ADMINISTRATIVE, attributeDefinition.isAdministrative());
                attrNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
                attrNode.setProperty(Constants.PROPERTY_NO_COPY, attributeDefinition.isNoCopy());
                attrNode.setProperty(Constants.PROPERTY_UNIQUE, attributeDefinition.isUnique());

                classNode.createRelationshipTo(attrNode, RelTypes.HAS_ATTRIBUTE);

                tx.success();
                Util.addAttribute(classNode, attrNode);
            }else{
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not create the attribute, an attribute with name %1s already exists", attributeDefinition.getName()));
            }
        } catch(Exception ex){
            Logger.getLogger("addAttribute: "+ex.getMessage()); //NOI18N
            if (tx != null){
                tx.failure();
            }
            throw new RuntimeException(ex.getMessage());
        } finally {
            if (tx != null){
                tx.finish();
            }
        }
    }

    /**
     * Adds an attribute to a class
     * @param classId
     * @param attributeDefinition
     * @throws MetadataObjectNotFoundException if there is no a class with such classId
     */
    @Override
    public void addAttribute(long classId, AttributeMetadata attributeDefinition) throws MetadataObjectNotFoundException, OperationNotPermittedException {
        attributeDefinition = Util.createDefaultAttributeMetadata(attributeDefinition);
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node classNode = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();
            if (classNode == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %1s", classId));
            }
            if(!Util.isAttributeName(classNode, attributeDefinition)){
                Node attrNode = graphDb.createNode();
                attrNode.setProperty(Constants.PROPERTY_NAME, attributeDefinition.getName());
                attrNode.setProperty(Constants.PROPERTY_DESCRIPTION, attributeDefinition.getDescription());
                attrNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, attributeDefinition.getDisplayName());
                attrNode.setProperty(Constants.PROPERTY_TYPE, attributeDefinition.getType());
                attrNode.setProperty(Constants.PROPERTY_MAPPING, attributeDefinition.getMapping());
                attrNode.setProperty(Constants.PROPERTY_READONLY, attributeDefinition.isVisible());
                attrNode.setProperty(Constants.PROPERTY_VISIBLE, attributeDefinition.isVisible());
                attrNode.setProperty(Constants.PROPERTY_ADMINISTRATIVE, attributeDefinition.isAdministrative());
                attrNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
                attrNode.setProperty(Constants.PROPERTY_NO_COPY, attributeDefinition.isNoCopy());
                attrNode.setProperty(Constants.PROPERTY_UNIQUE, attributeDefinition.isUnique());
                
                classNode.createRelationshipTo(attrNode, RelTypes.HAS_ATTRIBUTE);

                tx.success();
                Util.addAttribute(classNode, attrNode);
            }else{
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not create the attribute, an attribute with the name %1s already exists", attributeDefinition.getName()));
            }
        } catch(Exception ex){
            Logger.getLogger("addAttribute: "+ex.getMessage()); //NOI18N
            if(tx != null){
                tx.failure();
            }
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null){
                tx.finish();
            }
        }
    }

    /**
     * Gets an attribute belonging to a class
     * @param className
     * @param attributeName
     * @return AttributeMetada, null if there is no attribute with such name
     * @throws MetadataObjectNotFoundException if there is no a class with such className
     */
    @Override
    public AttributeMetadata getAttribute(String className, String attributeName) throws MetadataObjectNotFoundException {
        AttributeMetadata attribute = null;
        try {
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();

            if (classNode == null){ 
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with name %1s", className));
            }
            Iterable<Relationship> relationships = classNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship relationship : relationships) {
                Node attrNode = relationship.getEndNode();
                if (String.valueOf(attrNode.getProperty(Constants.PROPERTY_NAME)).equals(attributeName)) {
                    attribute = new AttributeMetadata();
                    attribute = Util.createAttributeMetadataFromNode(attrNode);
                }
            }
        } catch(Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        return attribute;
    }

    /**
     * Gets an attribute belonging to a class
     * @param classId
     * @param attributeId
     * @return AttributeMetada, null if there is no attribute with such name
     * @throws MetadataObjectNotFoundException if there is no a class with such classId
     */
    @Override
    public AttributeMetadata getAttribute(long classId, long attributeId) throws MetadataObjectNotFoundException {
        AttributeMetadata attribute = null;
        try {
            Node classNode = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();
            if (classNode == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %1s", classId));
            }
            Iterable<Relationship> relationships = classNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship relationship : relationships) {
                Node attrNode = relationship.getEndNode();
                if (attrNode.getId() == attributeId) {
                    attribute = new AttributeMetadata();
                    attribute = Util.createAttributeMetadataFromNode(attrNode);
                    break;
                }
            }
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
        return attribute;
    }

    /**
     * Changes an attribute definition belonging to a classMetadata
     * @param ClassId
     * @param newAttributeDefinition
     */
    @Override
    public void setAttributeProperties(long classId, AttributeMetadata newAttributeDefinition) throws MetadataObjectNotFoundException{
        Transaction tx = null;
        boolean couldDelAtt = false;
        Node classNode = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();
        if (classNode == null){
            throw new MetadataObjectNotFoundException(String.format(
                    "Can not find a class with the id %1s", classId));
        }
        Iterable<Relationship> relationships = classNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
        for (Relationship relationship : relationships) {
            Node attrNode = relationship.getEndNode();
            if (attrNode.getId() == newAttributeDefinition.getId())
                    //&&!((Boolean)attrNode.getProperty(PROPERTY_LOCKED)).booleanValue())
            {
                if(!newAttributeDefinition.equals(Util.createAttributeMetadataFromNode(attrNode))){
                    String oldAttributeName = (String)attrNode.getProperty(Constants.PROPERTY_NAME);
                    String oldAttributeType = (String)attrNode.getProperty(Constants.PROPERTY_TYPE);
                    try {
                        tx = graphDb.beginTx();
                        if(newAttributeDefinition.getName() != null){
                            attrNode.setProperty(Constants.PROPERTY_NAME, newAttributeDefinition.getName());
                        }
                        if(newAttributeDefinition.getDescription() != null){
                            attrNode.setProperty(Constants.PROPERTY_DESCRIPTION, newAttributeDefinition.getDescription());
                        }
                        if(newAttributeDefinition.getDisplayName() != null){
                            attrNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, newAttributeDefinition.getDisplayName());
                        }
                        if(newAttributeDefinition.getType() != null){
                            attrNode.setProperty(Constants.PROPERTY_TYPE, newAttributeDefinition.getType());
                        }
                        attrNode.setProperty(Constants.PROPERTY_MAPPING, newAttributeDefinition.getMapping());
                        attrNode.setProperty(Constants.PROPERTY_READONLY, newAttributeDefinition.isReadOnly());
                        attrNode.setProperty(Constants.PROPERTY_VISIBLE, newAttributeDefinition.isVisible());
                        attrNode.setProperty(Constants.PROPERTY_ADMINISTRATIVE, newAttributeDefinition.isAdministrative());
                        attrNode.setProperty(Constants.PROPERTY_NO_COPY, newAttributeDefinition.isNoCopy());
                        couldDelAtt = true;
                        tx.success();
                        Util.changeAttributeTypes(classNode, attrNode, oldAttributeName, oldAttributeType);
                    
                    }catch(Exception ex){
                        Logger.getLogger("Change Attribute definition: "+ex.getMessage()); //NOI18N
                        if (tx != null){
                            tx.failure();
                        }
                        throw new RuntimeException(ex.getMessage());
                    } finally {
                        if (tx != null){
                            tx.finish();
                        }
                    }
                }
                else{
                    couldDelAtt = true;
                    break;
                }
            }
        }//end for
        if (!couldDelAtt) { //if the attribute does exist
            throw new MetadataObjectNotFoundException(String.format(
                    "Can not find an attribute with the name %1s", newAttributeDefinition.getName()));
        }
    }
    /**
     * Changes an attribute definition belonging to a classMetadata
     * @param ClassId
     * @param newAttributeDefinition
     */
    @Override
    public void setAttributeProperties (String className, AttributeMetadata newAttributeDefinition) throws MetadataObjectNotFoundException{
        Transaction tx = null;
        boolean couldDelAtt = false;
        Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();

        if (classNode == null){
            throw new MetadataObjectNotFoundException(String.format(
                    "Can not find a class with name %1s", className));
        }
        Iterable<Relationship> relationships = classNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
        for (Relationship relationship : relationships) {
            Node attrNode = relationship.getEndNode();
            if (attrNode.getId() == newAttributeDefinition.getId())
//                    && !((Boolean)attrNode.getProperty(PROPERTY_LOCKED)).booleanValue())
            {
                if(!newAttributeDefinition.equals(Util.createAttributeMetadataFromNode(attrNode))){
                    String oldAttributeName = (String)attrNode.getProperty(Constants.PROPERTY_NAME);
                    String oldAttributeType = (String)attrNode.getProperty(Constants.PROPERTY_TYPE);
                    try {
                        tx = graphDb.beginTx();
                        if(newAttributeDefinition.getName() != null){
                            attrNode.setProperty(Constants.PROPERTY_NAME, newAttributeDefinition.getName());}
                        if(newAttributeDefinition.getDescription() != null){
                            attrNode.setProperty(Constants.PROPERTY_DESCRIPTION, newAttributeDefinition.getDescription());}
                        if(newAttributeDefinition.getDisplayName() != null){
                            attrNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, newAttributeDefinition.getDisplayName());}
                        if(newAttributeDefinition.getType() != null){
                            attrNode.setProperty(Constants.PROPERTY_TYPE, newAttributeDefinition.getType());}
                        attrNode.setProperty(Constants.PROPERTY_MAPPING, newAttributeDefinition.getMapping());
                        attrNode.setProperty(Constants.PROPERTY_READONLY, newAttributeDefinition.isReadOnly());
                        attrNode.setProperty(Constants.PROPERTY_VISIBLE, newAttributeDefinition.isVisible());
                        attrNode.setProperty(Constants.PROPERTY_ADMINISTRATIVE, newAttributeDefinition.isAdministrative());
                        attrNode.setProperty(Constants.PROPERTY_NO_COPY, newAttributeDefinition.isNoCopy());
                        tx.success();
                        couldDelAtt = true;
                        Util.changeAttributeTypes(classNode, attrNode, oldAttributeName, oldAttributeType);
                    }catch(Exception ex){
                        Logger.getLogger("Change attribute definition: "+ex.getMessage()); //NOI18N
                        if (tx != null){
                            tx.failure();
                        }
                        throw new RuntimeException(ex.getMessage());
                    } finally {
                        if (tx != null){
                            tx.finish();
                        }
                    }
                }
            }
        }//end for
        if (!couldDelAtt) { //if the attribute does exist
            throw new MetadataObjectNotFoundException(String.format(
                    "Can not find the Attribute with the name %1s", newAttributeDefinition.getName()));
        }
    }

    /**
     * Deletes an attribute belonging to a classMetadata
     * @param className
     * @param attributeName
     * @return true if success
     * @throws MetadataObjectNotFoundException if there is no a class with such className
     */
    @Override
    public void deleteAttribute(String className, String attributeName) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        boolean couldDelAtt = false;
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (classNode == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with name %1s", attributeName));
            }
            Iterable<Relationship> relationships = classNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship relationship : relationships) {
                Node attrNode = relationship.getEndNode();
                if (String.valueOf(attrNode.getProperty(Constants.PROPERTY_NAME)).equals(attributeName)){ 
                    //&& !((Boolean)attrNode.getProperty(Constants.PROPERTY_LOCKED)).booleanValue()) {
                    try {
                        tx = graphDb.beginTx();
                        Util.deleteAttribute(classNode, attributeName);
                        attrNode.delete();
                        relationship.delete();
                        couldDelAtt = true;
                        tx.success();
                    }catch(Exception ex){
                        Logger.getLogger("deleteAttribute: "+ex.getMessage()); //NOI18N
                        if (tx != null){
                            tx.failure();
                        }
                        throw new RuntimeException(ex.getMessage());
                    } finally {
                        if (tx != null){
                            tx.finish();
                        }
                    }
                }//end for
            }
            if (!couldDelAtt) { //if the attribute doesn't exist
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find an attribute with the name %1s", attributeName));
            }
    }

    /**
     * Deletes an attribute belonging to a classMetadata
     * @param classId
     * @param attributeName
     * @throws MetadataObjectNotFoundException if there is no a class with such classId
     */
    @Override
    public void deleteAttribute(long classId, String attributeName) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        boolean couldDelAtt = false;

        Node classNode = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();

        if (classNode == null){
            throw new MetadataObjectNotFoundException(String.format(
                    "Can not find a class with id %1s", classId));
        }
        Iterable<Relationship> relationships = classNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
        for (Relationship relationship : relationships) {
            Node attrNode = relationship.getEndNode();
            if (String.valueOf(attrNode.getProperty(Constants.PROPERTY_NAME)).equals(attributeName)){
                    //&&!((Boolean)attrNode.getProperty(Constants.PROPERTY_LOCKED)).booleanValue()) {
                try {
                    tx = graphDb.beginTx();
                    Util.deleteAttribute(classNode, attributeName);
                    attrNode.delete();
                    relationship.delete();
                    couldDelAtt = true;
                    tx.success();
                }catch(Exception ex){
                    Logger.getLogger("deleteAttribute: "+ex.getMessage()); //NOI18N
                    if (tx != null){
                        tx.failure();
                    }
                    throw new RuntimeException(ex.getMessage());
                } finally {
                    if (tx != null){
                        tx.finish();
                    }
                }
            }
        }//end for
        if (!couldDelAtt) { //if the attribute doesn't exist
            throw new MetadataObjectNotFoundException(String.format(
                    "Can not find an attribute with name %1s", attributeName));
        }
    }

    /**
     * Creates a new category
     * @param categoryDefinition
     * @return CategoryId
     */
    @Override
    public long createCategory(CategoryMetadata categoryDefinition) throws MetadataObjectNotFoundException{
        Transaction tx = null;
        categoryDefinition = Util.createDefaultCategoryMetadata(categoryDefinition);
        try {
            tx = graphDb.beginTx();
            Node categoryNode = graphDb.createNode();
            categoryNode.setProperty(Constants.PROPERTY_NAME, categoryDefinition.getName());
            categoryNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            
            categoryNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, categoryDefinition.getDisplayName());
            categoryNode.setProperty(Constants.PROPERTY_DESCRIPTION, categoryDefinition.getDescription());
            
            categoryIndex.add(categoryNode, Constants.PROPERTY_ID, categoryNode.getId());
            categoryIndex.add(categoryNode, Constants.PROPERTY_NAME, categoryDefinition.getName());

            tx.success();
            return categoryNode.getId();
        }catch(Exception ex){
            Logger.getLogger("Create catecogry: "+ex.getMessage()); //NOI18N
            if (tx != null){
                tx.failure();
            }
            throw new RuntimeException(ex.getMessage());
        } finally {
            if (tx != null){
                tx.finish();
            }
        }
    }

    /**
     * Gets a Category with it's name
     * @param categoryName
     * @return CategoryMetadata
     * @throws MiscException if the Category does not exist
     */
    @Override
    public CategoryMetadata getCategory(String categoryName) throws MetadataObjectNotFoundException {
        CategoryMetadata ctgrMtdt = new CategoryMetadata();
        try
        {
            Node ctgNode = categoryIndex.get(Constants.PROPERTY_NAME, categoryName).getSingle();
            if (ctgNode == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a category with name %1s", categoryName));
            }
            ctgrMtdt.setName((String) ctgNode.getProperty(Constants.PROPERTY_NAME));
            ctgrMtdt.setDescription((String) ctgNode.getProperty(Constants.PROPERTY_DESCRIPTION));
            ctgrMtdt.setDisplayName((String) ctgNode.getProperty(Constants.PROPERTY_DISPLAY_NAME));
        }catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        return ctgrMtdt;
    }

    /**
     * Gets a Category with it's Id
     * @param categoryId
     * @return CategoryMetadata
     * @throws MiscException if there is no Category with such cetegoryId
     */
    public CategoryMetadata getCategory(long categoryId) throws MetadataObjectNotFoundException {
        CategoryMetadata ctgrMtdt = new CategoryMetadata();
        try {
            Node ctgNode = categoryIndex.get(Constants.PROPERTY_ID, categoryId).getSingle();
            if (ctgNode == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a category with id %1s", categoryId));
            }
            ctgrMtdt.setName((String) ctgNode.getProperty(Constants.PROPERTY_NAME));
            ctgrMtdt.setDescription((String) ctgNode.getProperty(Constants.PROPERTY_DESCRIPTION));
            ctgrMtdt.setDisplayName((String) ctgNode.getProperty(Constants.PROPERTY_DISPLAY_NAME));

        }catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
       return ctgrMtdt;
    }

    /**
     * Changes a category definition
     * @param categoryDefinition
     * @throws MetadataObjectNotFoundException if there is no Category with such cetegoryId
     */
    @Override
    public void setCategoryProperties(CategoryMetadata categoryDefinition) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node ctgr = categoryIndex.get(Constants.PROPERTY_NAME, categoryDefinition.getName()).getSingle();
            if (ctgr == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a category with name %1s", categoryDefinition.getName()));
            }
            ctgr.setProperty(Constants.PROPERTY_NAME, categoryDefinition.getName());
            ctgr.setProperty(Constants.PROPERTY_DISPLAY_NAME, categoryDefinition.getDisplayName());
            ctgr.setProperty(Constants.PROPERTY_DESCRIPTION, categoryDefinition.getDescription());
            tx.success();
        }catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        finally {
            if(tx != null){
                tx.finish();
            }
        }
    }

    @Override
    public void deleteCategory(String categoryName) {
        //TODO what about the classes?
    }

    @Override
    public void deleteCategory(int categoryId) {
        //TODO what about the classes?
    }

    @Override
    public void addImplementor(String classWhichImplementsName, String interfaceToImplementName) {
    }

    @Override
    public void removeImplementor(String classWhichImplementsName, String interfaceToBeRemovedName) {
    }

    @Override
    public void addImplementor(int classWhichImplementsId, int interfaceToImplementId) {
    }

    @Override
    public void removeImplementor(int classWhichImplementsId, int interfaceToBeRemovedId) {
    }

    @Override
    public InterfaceMetadata getInterface(String interfaceName) {
        return null;
    }

    @Override
    public InterfaceMetadata getInterface(int interfaceid) {
        return null;
    }

    @Override
    public List<ClassMetadataLight> getPossibleChildren(String parentClassName) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> cml = new ArrayList<ClassMetadataLight>();
        try {
            String cypherQuery;
            if (parentClassName == null || parentClassName.equals(Constants.DUMMYROOT)){
                cypherQuery = "START rootNode = node(0) ".concat(
                                 "MATCH rootNode -[:DUMMY_ROOT]->dummyRootNode-[:POSSIBLE_CHILD]->directChild<-[?:EXTENDS*]-subClass ").concat(
                                 "WHERE subClass.abstract=false OR subClass IS NULL ").concat(
                                 "RETURN directChild, subClass ").concat(
                                 "ORDER BY directChild.name,subClass.name ASC");
            }
            else{
                cypherQuery = "START parentClassNode = node:classes({className}) ".concat(
                                 "MATCH parentClassNode -[:POSSIBLE_CHILD]->directChild<-[?:EXTENDS*]-subClass ").concat(
                                 "WHERE subClass.abstract=false OR subClass IS NULL ").concat(
                                 "RETURN directChild, subClass ").concat(
                                 "ORDER BY directChild.name,subClass.name ASC");
            }
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(Constants.PROPERTY_CLASS_NAME, "name:" + parentClassName);//NOI18N

            ExecutionEngine engine = new ExecutionEngine(graphDb);
            ExecutionResult result = engine.execute(cypherQuery, params);

            Iterator<Map<String,Object>> entries = result.iterator();
            while (entries.hasNext()){
                Map<String,Object> entry = entries.next();
                Node directChildNode =  (Node)entry.get("directChild");
                Node indirectChildNode =  (Node)entry.get("subClass");
                if (!(Boolean)directChildNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                    cml.add(Util.createClassMetadataFromNode(directChildNode));
                }
                if (indirectChildNode != null){
                    cml.add(Util.createClassMetadataFromNode(indirectChildNode));
                }
            }
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
        return cml;
    }

    @Override
    public List<ClassMetadataLight> getPossibleChildrenNoRecursive(String parentClassName) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> cml = new ArrayList<ClassMetadataLight>();
        try {
            String cypherQuery;
            if (parentClassName == null || parentClassName.equals(Constants.DUMMYROOT)){//The Dummy Rooot
                cypherQuery = "START rootNode = node(0) ".concat(
                                 "MATCH rootNode -[:DUMMY_ROOT]->dummyRootNode-[:POSSIBLE_CHILD]->directChild ").concat(
                                 "RETURN directChild ").concat(
                                 "ORDER BY directChild.name ASC");
            }else{
                cypherQuery = "START parentClassNode = node:classes({className}) ".concat(
                                 "MATCH parentClassNode -[:POSSIBLE_CHILD]->directChild ").concat(
                                 "RETURN directChild ").concat(
                                 "ORDER BY directChild.name ASC");
            }
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(Constants.PROPERTY_CLASS_NAME, "name:" + parentClassName);//NOI18N

            ExecutionEngine engine = new ExecutionEngine(graphDb);
            ExecutionResult result = engine.execute(cypherQuery, params);

            Iterator<Node> directPossibleChildren = result.columnAs("directChild");
            for (Node node : IteratorUtil.asIterable(directPossibleChildren)){
                cml.add(Util.createClassMetadataFromNode(node));
            }
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
        return cml;
    }

    @Override
    public void addPossibleChildren(long parentClassId, long[] possibleChildren)
            throws MetadataObjectNotFoundException, InvalidArgumentException, DatabaseException {
        Transaction tx = null;
        Node parentNode;

        //The dummy root
        if(parentClassId != -1) {
            parentNode = classIndex.get(Constants.PROPERTY_ID, parentClassId).getSingle();

            if (parentNode == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %1s", parentClassId));
            if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                throw new InvalidArgumentException(
                        String.format("%1s is not a business class, thus can not be added to the containment hierarchy", (String)parentNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
        }else{
            Node referenceNode = graphDb.getReferenceNode();
            Relationship rel = referenceNode.getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.OUTGOING);
            parentNode = rel.getEndNode();
        }

        List<ClassMetadataLight> currentPossibleChildren = getPossibleChildren((String)parentNode.getProperty(Constants.PROPERTY_NAME));

        try{
            tx = graphDb.beginTx();
            for (long id : possibleChildren) {
                Node childNode = classIndex.get(Constants.PROPERTY_ID, id).getSingle();

                if (childNode == null){
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find class with id %1s", parentClassId));
                }
                if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)childNode.getProperty(Constants.PROPERTY_NAME))){
                    throw new InvalidArgumentException(
                            String.format("%1s is not a business class, thus can not be added to the containment hierarchy", (String)childNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
                }
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                   for (Node subclassNode : Util.getAllSubclasses(childNode)){
                       for (ClassMetadataLight possibleChild : currentPossibleChildren){
                            if (possibleChild.getId() == subclassNode.getId()){
                                throw new InvalidArgumentException(String.format("A subclass of %1s is already a possible child for instances of %2s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
                            }
                       }
                   }
                }
                else{
                    for (ClassMetadataLight possibleChild : currentPossibleChildren){
                        if (possibleChild.getId() == childNode.getId()){
                            throw new InvalidArgumentException(String.format("Class %1s is already a possible child for instances of %2s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
                        }
                    }
                }
                parentNode.createRelationshipTo(childNode, RelTypes.POSSIBLE_CHILD);
                
                //Refresh cache
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                    for(Node subclass : Util.getAllSubclasses(childNode))
                        cm.putPossibleChild((String)parentNode.getProperty(Constants.PROPERTY_NAME),(String)subclass.getProperty(Constants.PROPERTY_NAME));
                }else
                    cm.putPossibleChild((String)parentNode.getProperty(Constants.PROPERTY_NAME), (String)childNode.getProperty(Constants.PROPERTY_NAME));

                tx.success();
            }
        }catch (MetadataObjectNotFoundException ex) {
            tx.failure();
            throw ex;
        }
        catch (InvalidArgumentException ex) {
            tx.failure();
            throw ex;
        }finally {
            if (tx != null)
                tx.finish();
        }
    }

    public void addPossibleChildren(String parentClassName, String[] _possibleChildren) throws MetadataObjectNotFoundException, InvalidArgumentException {
        Transaction tx = null;
        Node parentNode;
        boolean isDummyRoot = false;

        if(parentClassName != null) {
            parentNode = classIndex.get(Constants.PROPERTY_NAME, parentClassName).getSingle();

            if (parentNode == null){
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find class %1s", parentClassName));
            }
            if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(Constants.PROPERTY_NAME))){
                throw new InvalidArgumentException(
                        String.format("%1s is not a business class, thus can not be added to the containment hierarchy", (String)parentNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
            }
        }
        else{
            Node referenceNode = graphDb.getReferenceNode();
            Relationship rel = referenceNode.getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.OUTGOING);
            parentNode = rel.getEndNode();

            if(!(Constants.DUMMYROOT).equals((String)parentNode.getProperty(Constants.PROPERTY_NAME))){
                    throw new MetadataObjectNotFoundException("DummyRoot node is corrupted");
            }
            else{
                isDummyRoot = true;
            }
        }
        List<ClassMetadataLight> currentPossibleChildren = getPossibleChildren(isDummyRoot ? null : (String)parentNode.getProperty(Constants.PROPERTY_NAME));
        tx = graphDb.beginTx();

        try{
            for (String possibleChildName : _possibleChildren) {
                Node childNode = classIndex.get(Constants.PROPERTY_NAME, possibleChildName).getSingle();
                if (childNode == null){
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find class %1s", possibleChildName));
                }
                if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)childNode.getProperty(Constants.PROPERTY_NAME))){
                    throw new InvalidArgumentException(
                            String.format("%1s is not a business class, thus can not be added to the containment hierarchy", (String)childNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
                }
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                   for (Node subclassNode : Util.getAllSubclasses(childNode)){
                       for (ClassMetadataLight possibleChild : currentPossibleChildren){
                            if (possibleChild.getId() == subclassNode.getId()){
                                throw new InvalidArgumentException(String.format("A subclass of %1s is already a possible child for instances of %2s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
                            }
                       }
                   }
                }
                else{
                    for (ClassMetadataLight possibleChild : currentPossibleChildren){
                        if (possibleChild.getId() == childNode.getId()){
                            throw new InvalidArgumentException(String.format("Class %1s is already a possible child for instances of %2s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
                        }
                    }
                }
                parentNode.createRelationshipTo(childNode, RelTypes.POSSIBLE_CHILD);

                //Refresh cache
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                    for(Node subclass : Util.getAllSubclasses(childNode)){
                        cm.putPossibleChild(parentClassName,(String)subclass.getProperty(Constants.PROPERTY_NAME));
                    }
                }
                else{
                    cm.putPossibleChild(parentClassName, (String)childNode.getProperty(Constants.PROPERTY_NAME));
                }
                tx.success();
            }
        }catch (MetadataObjectNotFoundException ex) {
            tx.failure();
            throw ex;
        }
        catch (InvalidArgumentException ex) {
            tx.failure();
            throw ex;
        }finally {
            if (tx != null){
                tx.finish();
            }
        }
    }
    
    @Override
    public void removePossibleChildren(long parentClassId, long[] childrenToBeRemoved) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        Node parentNode;
        if (parentClassId == -1){
            parentNode = graphDb.getReferenceNode().getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.OUTGOING).getEndNode();
            if (parentNode == null){
                throw new MetadataObjectNotFoundException("DummyRoot is corrupted");
            }
        }
        else{
            parentNode = classIndex.get(Constants.PROPERTY_ID, parentClassId).getSingle();
            if (parentNode == null){
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %1s", parentClassId));
            }
        }
        try
        {
            tx = graphDb.beginTx();
            for (long id : childrenToBeRemoved){
                Node childNode = classIndex.get(Constants.PROPERTY_ID, id).getSingle();
                Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.POSSIBLE_CHILD, Direction.OUTGOING);

                for (Relationship rel: relationships) {
                    Node possiblechild = rel.getEndNode();
                    if(childNode.getId() == possiblechild.getId()){
                        rel.delete();
                        String parentClassName = (String)parentNode.getProperty(Constants.PROPERTY_NAME);
                        if (cm.getClass((String)childNode.getProperty(Constants.PROPERTY_NAME)).isAbstract()){
                            for(Node subClass : Util.getAllSubclasses(childNode)){
                                cm.removePossibleChild(parentClassName, (String)subClass.getProperty(Constants.PROPERTY_NAME));
                            }
                        }
                        else{
                            cm.removePossibleChild(parentClassName, (String)childNode.getProperty(Constants.PROPERTY_NAME));
                        }
                        break;
                    }
                }//end for
            }//end for
            tx.success();
        }catch (Exception ex) {
            Logger.getLogger("removePossibleChildren: "+ex.getMessage()); //NOI18N
            if (tx != null){
                tx.failure();
            }
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null){
                tx.finish();
            }
        }
    }

    /**
     * Get the upstream containment hierarchy for a given class, unlike getPossibleChildren (which will give you the
     * downstream hierarchy).
     * @param className Class name
     * @param recursive Get only the direct possible parents, or go up into the <strong>containment</strong> hierarchy. Beware: don't mistake the class hierarchy for the containment one
     * @return An ordered list with the . Repeated elements are omitted
     * @throws MetadataObjectNotFoundException if className does not correspond to any existing class
     */
    public List<ClassMetadataLight> getUpstreamContainmentHierarchy(String className, boolean recursive) throws MetadataObjectNotFoundException {
        Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
        if (classNode == null){
           throw new MetadataObjectNotFoundException(String.format(
                        "Can not find class %1s", className));
        }
        List<ClassMetadataLight> res = new ArrayList<ClassMetadataLight>();
        
        String cypherQuery = "START classNode=node:classes(name=\""+className+"\") "+
                             "MATCH possibleParentClassNode-[:POSSIBLE_CHILD"+(recursive ? "*" : "")+ "]->classNode "+
                             "WHERE possibleParentClassNode.name <> \""+ Constants.DUMMYROOT +
                             "\" RETURN distinct possibleParentClassNode "+
                             "ORDER BY possibleParentClassNode.name ASC";

        ExecutionEngine engine = new ExecutionEngine(graphDb);
        ExecutionResult result = engine.execute(cypherQuery);

        Iterator<Node> directPossibleChildren = result.columnAs("possibleParentClassNode"); //NOI18N
        for (Node node : IteratorUtil.asIterable(directPossibleChildren)){
            res.add(Util.createClassMetadataLightFromNode(node));
        }
        return res;
    }
    /**
     * 
     * @param allegedParent
     * @param classToBeEvaluated
     * @return 
     */
    @Override
    public boolean isSubClass(String allegedParent, String classToBeEvaluated) {
        try {
            return cm.isSubClass(allegedParent, classToBeEvaluated);
        } catch (MetadataObjectNotFoundException ex) {
            return false;
        }
    }
}

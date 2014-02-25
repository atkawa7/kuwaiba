/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
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
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;

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
     * Instance of application entity manager
     */
    ApplicationEntityManagerImpl aem;
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
        buildContainmentCache();
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
    public long createClass(ClassMetadata classDefinition) throws MetadataObjectNotFoundException, DatabaseException, InvalidArgumentException {
        
        //aem.validateCall("createClass", ipAddress, sessionId);
        
        Transaction tx = null;
        long id;   
        if (classDefinition.getName() == null)
            throw new InvalidArgumentException("Class name can not be null", Level.INFO);
            
        if (!classDefinition.getName().matches("^[a-zA-Z0-9_]*$"))
            throw new InvalidArgumentException(String.format("Class %s contains invalid characters", classDefinition.getName()), Level.INFO);
        
        if(classDefinition.getName().isEmpty())
                    throw new InvalidArgumentException("Class name can not be an empty string", Level.INFO);
        
        try {
            tx = graphDb.beginTx();
            if (graphDb.getReferenceNode() == null)//The reference node must exist
                throw new DatabaseException("Reference node does not exist. The database seems to be corrupted");
            
            if (classIndex.get(Constants.PROPERTY_NAME, classDefinition.getName()).getSingle() != null)
                throw new InvalidArgumentException(String.format("Class %s already exists", classDefinition.getName()), Level.INFO);
            
            Node classNode = graphDb.createNode();

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
            //Is this class the root of all class hierarchy
            if (classDefinition.getParentClassName() == null){
                
                if (classDefinition.getName().equals(Constants.CLASS_ROOTOBJECT))
                    classNode.createRelationshipTo(graphDb.getReferenceNode(), RelTypes.ROOT);
                else
                    throw new MetadataObjectNotFoundException(String.format("Only %s can be the root superclass", Constants.CLASS_ROOTOBJECT));
            }
            else { //Category
//                if (classDefinition.getCategory() != null) //if the category already exists
//                { 
//                    Node ctgrNode = categoryIndex.get(Constants.PROPERTY_NAME, classDefinition.getCategory().getName()).getSingle();
//                    if (ctgrNode == null) {
//                        long ctgrId = createCategory(classDefinition.getCategory());
//                        ctgrNode = categoryIndex.get(Constants.PROPERTY_ID, ctgrId).getSingle();
//                    }
//                    classNode.createRelationshipTo(ctgrNode, RelTypes.BELONGS_TO_GROUP);
//                }//end if is category null
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
                        newAttrNode.setProperty(Constants.PROPERTY_READ_ONLY, parentAttrNode.getProperty(Constants.PROPERTY_READ_ONLY));
                        newAttrNode.setProperty(Constants.PROPERTY_VISIBLE, parentAttrNode.getProperty(Constants.PROPERTY_VISIBLE));
                        newAttrNode.setProperty(Constants.PROPERTY_ADMINISTRATIVE, parentAttrNode.getProperty(Constants.PROPERTY_ADMINISTRATIVE));
                        newAttrNode.setProperty(Constants.PROPERTY_NO_COPY, parentAttrNode.getProperty(Constants.PROPERTY_NO_COPY));
                        newAttrNode.setProperty(Constants.PROPERTY_UNIQUE, parentAttrNode.getProperty(Constants.PROPERTY_UNIQUE));
                        //newAttrNode.setProperty(PROPERTY_LOCKED, parentAttrNode.getProperty(PROPERTY_LOCKED));
                        classNode.createRelationshipTo(newAttrNode, RelTypes.HAS_ATTRIBUTE);
                    }
                }//end if there is a Parent
                else{
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find parent class with name %s", classDefinition.getParentClassName()));
                }
            }//end else not rootNode
            //Attributes
            if (classDefinition.getAttributes() != null) {
                for (AttributeMetadata at : classDefinition.getAttributes()) {
                    if (getAttribute(classDefinition.getName(), at.getName()) == null){
                        createAttribute(id, at);
                    }
                }
            }
            tx.success();
            tx.finish();
            cm.putClass(classDefinition);
            buildContainmentCache();
            return id;
        } catch (Exception ex) {
            Logger.getLogger("createClass: "+ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Changes a class metadata definition
     * @param newClassDefinition
     * @return true if success
     * @throws MetadataObjectNotFoundException if there is no class with such classId
     */
    @Override
    public void setClassProperties (ClassMetadata newClassDefinition, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("setClassProperties", ipAddress, sessionId);
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node classMetadata = classIndex.get(Constants.PROPERTY_ID, newClassDefinition.getId()).getSingle();
            if (classMetadata == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %s", newClassDefinition.getName()));
            
            String formerName = (String)classMetadata.getProperty(Constants.PROPERTY_NAME);
            
            if(newClassDefinition.getName() != null){
                if(newClassDefinition.getName().isEmpty())
                    throw new InvalidArgumentException("Class name can not be an empty string", Level.INFO);
                
                if (!newClassDefinition.getName().matches("^[a-zA-Z0-9_]*$"))
                    throw new InvalidArgumentException(String.format("Class name %s contains invalid characters", newClassDefinition.getName()), Level.INFO);
                
                if (classIndex.get(Constants.PROPERTY_NAME, newClassDefinition.getName()).getSingle() != null)
                   throw new InvalidArgumentException(String.format("Class %s already exists", newClassDefinition.getName()), Level.INFO);
                
                classIndex.remove(classMetadata, Constants.PROPERTY_NAME);
                classMetadata.setProperty(Constants.PROPERTY_NAME, newClassDefinition.getName());
                classIndex.add(classMetadata, Constants.PROPERTY_NAME, newClassDefinition.getName());
                buildContainmentCache();
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
            if (newClassDefinition.isCountable() != null)
                classMetadata.setProperty(Constants.PROPERTY_COUNTABLE, newClassDefinition.isCountable());
            if (newClassDefinition.isAbstract() != null)
                classMetadata.setProperty(Constants.PROPERTY_ABSTRACT, newClassDefinition.isAbstract());
            if (newClassDefinition.isInDesign() != null)
                classMetadata.setProperty(Constants.PROPERTY_IN_DESIGN, newClassDefinition.isInDesign());
            if (newClassDefinition.isCustom() != null)
                classMetadata.setProperty(Constants.PROPERTY_CUSTOM, newClassDefinition.isCustom());
            
            if(newClassDefinition.getAttributes() != null ){
                for (AttributeMetadata attr : newClassDefinition.getAttributes())
                    setAttributeProperties(newClassDefinition.getId(), attr, ipAddress, sessionId);
            }        
            tx.success();
            tx.finish();
            cm.removeClass(formerName);
            cm.putClass(Util.createClassMetadataFromNode(classMetadata));
        }catch(Exception ex){
            Logger.getLogger("setClassProperties: "+ex.getMessage()); //NOI18N
            if(tx != null){
                tx.failure();
                tx.finish();
            }
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Deletes a class metadata entry, its attributes and category relationships
     * @param classId
     * @throws ClassNotFoundException if there is not a class with de ClassId
     */
    @Override
    public void deleteClass(long classId, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException  {
        aem.validateCall("deleteClass", ipAddress, sessionId);
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node node = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();

            if (node == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %s", classId));
            
            String currentClassName = (String) node.getProperty(Constants.PROPERTY_NAME);
            
            if (!(Boolean)node.getProperty(Constants.PROPERTY_CUSTOM))
                throw new InvalidArgumentException(String.format(
                        "Core classes can not be deleted"), Level.SEVERE);
            if (node.hasRelationship(RelTypes.INSTANCE_OF))
                throw new InvalidArgumentException(String.format(
                        "Class %s has instances and can not be deleted", node.getProperty(Constants.PROPERTY_NAME)), Level.SEVERE);
            
            if (node.hasRelationship(Direction.INCOMING, RelTypes.EXTENDS))
                throw new InvalidArgumentException(String.format(
                        "Class %s has subclasses and can not be deleted", node.getProperty(Constants.PROPERTY_NAME)), Level.SEVERE);
            
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
            tx.finish();
            cm.removeClass(currentClassName);
            buildContainmentCache();

        } catch(Exception ex){
            Logger.getLogger("deleteClass: "+ex.getMessage()); //NOI18N
            if(tx != null){
                tx.failure();
                tx.finish();
            }
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Deletes a classmetadata, its attributes and category relationships
     * @param className
     * @return true if success
     * @throws MetadataObjectNotFoundException if there is not a class with de ClassName
     */
    @Override
    public void deleteClass(String className, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("deleteClass", ipAddress, sessionId);
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node node = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();

            if (node == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with name %s", className));
                       
            if (node.hasRelationship(RelTypes.INSTANCE_OF))
                throw new InvalidArgumentException(String.format(
                        "The class with name %s has instances and can not be deleted", className), Level.SEVERE);
            
            if (node.hasRelationship(Direction.INCOMING, RelTypes.EXTENDS))
                throw new InvalidArgumentException(String.format(
                        "The class with name %s has subclasses and can not be deleted", className), Level.SEVERE);
            
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
            tx.finish();
            cm.removeClass(className);
            buildContainmentCache();

        } catch(Exception ex){
            Logger.getLogger("deleteClass: "+ex.getMessage()); //NOI18N
            if(tx != null){
                tx.failure();
                tx.finish();
            }
            throw new RuntimeException(ex.getMessage());
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
    public List<ClassMetadataLight> getAllClassesLight(boolean includeListTypes, boolean includeIndesign, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("getAllClassesLight", ipAddress, sessionId);
        List<ClassMetadataLight> cml = new ArrayList<ClassMetadataLight>();
        try {
            Node myClassNode =  classIndex.get(Constants.PROPERTY_NAME, Constants.CLASS_INVENTORYOBJECT).getSingle();

            if(myClassNode == null){
                throw new MetadataObjectNotFoundException(String.format(
                         "Can not find a class with name %s", Constants.CLASS_INVENTORYOBJECT));
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
    public List<ClassMetadataLight> getSubClassesLight(String className, boolean includeAbstractClasses, boolean includeSelf, String ipAddress, String sessionId) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("getSubClassesLight", ipAddress, sessionId);
        List<ClassMetadataLight> cml = new ArrayList<ClassMetadataLight>();
        try {
            
            ClassMetadata aClass = cm.getClass(className);
            if (aClass == null)
                throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s", className));
            
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
    public List<ClassMetadataLight> getSubClassesLightNoRecursive(String className, boolean includeAbstractClasses, boolean includeSelf, String ipAddress, String sessionId) 
            throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("getSubClassesLightNoRecursive", ipAddress, sessionId);
        List<ClassMetadataLight> cml = new ArrayList<ClassMetadataLight>();
        try {
            
            ClassMetadata aClass = cm.getClass(className);
            if (aClass == null)
                throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s", className));
            
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

            for (Node node : IteratorUtil.asIterable(n_column))
                 cml.add(Util.createClassMetadataLightFromNode(node));
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
    public List<ClassMetadata> getAllClasses(boolean includeListTypes, boolean includeIndesign, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("getAllClasses", ipAddress, sessionId);
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
    public ClassMetadata getClass(long classId, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("getClass", ipAddress, sessionId);
        ClassMetadata clmt = new ClassMetadata();
        try {
            Node node = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();
            if (node == null){
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %s", classId));
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
    public ClassMetadata getClass(String className, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("getClass", ipAddress, sessionId);
        try {
            Node node = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (node == null){
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with name %s", className));
            }
            return Util.createClassMetadataFromNode(node);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        
    }

    /**
     * Moves a class from one parentClass to an other parentClass
     * @param classToMoveName
     * @param targetParentClassName
     * @throws MetadataObjectNotFoundException if there is no a classToMove with such name
     * or if there is no a targetParentClass with such name
     */
    @Override
    public void moveClass(String classToMoveName, String targetParentClassName, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("moveClass", ipAddress, sessionId);
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node ctm = classIndex.get(Constants.PROPERTY_NAME, classToMoveName).getSingle();
            Node tcn = classIndex.get(Constants.PROPERTY_NAME, targetParentClassName).getSingle();

            if (ctm == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with name %s", classToMoveName));
            } else if (tcn == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with name %s", targetParentClassName));
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
    public void moveClass(long classToMoveId, long targetParentClassId, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("moveClass", ipAddress, sessionId);
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node ctm = classIndex.get(Constants.PROPERTY_ID, classToMoveId).getSingle();
            Node tcn = classIndex.get(Constants.PROPERTY_ID, targetParentClassId).getSingle();

            if (ctm == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %s", classToMoveId));
            } else if (tcn == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %s", targetParentClassId));
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
    public void createAttribute(String className, AttributeMetadata attributeDefinition, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("createAttribute", ipAddress, sessionId);
        Transaction tx = null;
        if (attributeDefinition.getName() == null || attributeDefinition.getName().isEmpty())
            throw new InvalidArgumentException("Attribute name can not be null or an empty string", Level.INFO);
        
        if (!attributeDefinition.getName().matches("^[a-zA-Z0-9_]*$"))
            throw new InvalidArgumentException(String.format("Attribute %s contains invalid characters", attributeDefinition.getName()), Level.INFO);
        
        Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s", className));
        
        try {
            tx = graphDb.beginTx();
            Util.createAttribute(classNode, attributeDefinition);
            tx.success();
            tx.finish();
            //Refresh cache for the affected classes
            refreshCacheOn(classNode);
        } catch(Exception ex){
            Logger.getLogger("createAttribute: "+ex.getMessage()); //NOI18N
            if (tx != null){
                tx.failure();
                tx.finish();
            }
            throw new InvalidArgumentException(ex.getMessage(), Level.WARNING);
        }
    }

    /**
     * Adds an attribute to a class
     * @param classId
     * @param attributeDefinition
     * @throws MetadataObjectNotFoundException if there is no a class with such classId
     */
    @Override
    public void createAttribute(long classId, AttributeMetadata attributeDefinition) throws MetadataObjectNotFoundException, InvalidArgumentException {
        //aem.validateCall("createAttribute", ipAddress, sessionId);
        Transaction tx = null;
        if (attributeDefinition.getName() == null || attributeDefinition.getName().isEmpty())
            throw new InvalidArgumentException("Attribute name can not be null or an empty string", Level.INFO);
        
        if (!attributeDefinition.getName().matches("^[a-zA-Z0-9_]*$"))
            throw new InvalidArgumentException(String.format("Attribute %s contains invalid characters", attributeDefinition.getName()), Level.INFO);
        
        Node classNode = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Can not find a class with id %s", classId));
        
        try {
            tx = graphDb.beginTx();
            Util.createAttribute(classNode, attributeDefinition);
            tx.success();
            tx.finish();
            //Refresh cache for the affected classes
            refreshCacheOn(classNode);
        } catch(Exception ex){
            Logger.getLogger("createAttribute: "+ex.getMessage()); //NOI18N
            if (tx != null){
                tx.failure();
                tx.finish();
            }
            throw new InvalidArgumentException(ex.getMessage(), Level.WARNING);
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
        //aem.validateCall("getAttribute", ipAddress, sessionId);
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
    public AttributeMetadata getAttribute(long classId, long attributeId, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("getAttribute", ipAddress, sessionId);
        AttributeMetadata attribute = null;
        try {
            Node classNode = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();
            if (classNode == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %s", classId));
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
    public void setAttributeProperties(long classId, AttributeMetadata newAttributeDefinition, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException{
        aem.validateCall("setAttributeProperties", ipAddress, sessionId);
        Transaction tx = null;
        Node classNode = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();
        
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Can not find a class with id %s", classId));
        
        for (Relationship relationship : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE)) {
            Node attrNode = relationship.getEndNode();
            if (attrNode.getId() == newAttributeDefinition.getId()) {
                String currentAttributeName = (String)attrNode.getProperty(Constants.PROPERTY_NAME);
                
                if (currentAttributeName.equals(Constants.PROPERTY_CREATION_DATE))
                    throw new InvalidArgumentException("Attribute \"creationDate\" can not be modified", Level.INFO);
                
                try {
                    tx = graphDb.beginTx();
                    if(newAttributeDefinition.getName() != null){
                        if (currentAttributeName.equals(Constants.PROPERTY_NAME))
                            throw new InvalidArgumentException("Attribute \"name\" can not be renamed", Level.INFO);
                        if (!newAttributeDefinition.getName().matches("^[a-zA-Z0-9_]*$"))
                            throw new InvalidArgumentException(String.format("Attribute %s contains invalid characters", newAttributeDefinition.getName()), Level.INFO);
                        
                        Util.changeAttributeName(classNode, currentAttributeName, newAttributeDefinition.getName());
                    }
                    if(newAttributeDefinition.getDescription() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_DESCRIPTION, newAttributeDefinition.getDescription());
                    if(newAttributeDefinition.getDisplayName() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_DISPLAY_NAME, newAttributeDefinition.getDisplayName());
                    if(newAttributeDefinition.getType() != null){
                        if (currentAttributeName.equals(Constants.PROPERTY_NAME))
                            throw new InvalidArgumentException("Attribute \"name\" can only be a String", Level.INFO);
                        if (AttributeMetadata.isPrimitive((String)attrNode.getProperty(Constants.PROPERTY_TYPE)))
                            Util.changeAttributeTypeIfPrimitive(classNode, currentAttributeName, newAttributeDefinition.getType());
                        else
                            Util.changeAttributeTypeIfListType(classNode, currentAttributeName, newAttributeDefinition.getType());
                    }
                    if(newAttributeDefinition.isReadOnly() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_READ_ONLY, newAttributeDefinition.isReadOnly());
                    if(newAttributeDefinition.isVisible() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_VISIBLE, newAttributeDefinition.isVisible());
                    if(newAttributeDefinition.isAdministrative() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_ADMINISTRATIVE, newAttributeDefinition.isAdministrative());
                    if(newAttributeDefinition.isNoCopy() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_NO_COPY, newAttributeDefinition.isNoCopy());
                    if(newAttributeDefinition.isUnique() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_UNIQUE, newAttributeDefinition.isUnique());
                    tx.success();
                    tx.finish();
                    //Refresh cache for the affected classes
                    refreshCacheOn(classNode);
                    return;
                }catch(Exception ex){
                    Logger.getLogger("setAttributeProperties: " + ex.getMessage()); //NOI18N
                    if (tx != null){
                        tx.failure();
                        tx.finish();
                    }
                    throw new InvalidArgumentException(ex.getMessage(), Level.WARNING);
                } 
            }
        }//end for
        throw new MetadataObjectNotFoundException(String.format(
                    "Can not find attribute %s in the class with id %s", newAttributeDefinition.getName(), classId));
    }
    /**
     * Changes an attribute definition belonging to a classMetadata
     * @param ClassId
     * @param newAttributeDefinition
     */
    @Override
    public void setAttributeProperties (String className, AttributeMetadata newAttributeDefinition, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("setAttributeProperties", ipAddress, sessionId);
        Transaction tx = null;
        Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
        
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s", className));
        
        for (Relationship relationship : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE)) {
            Node attrNode = relationship.getEndNode();
            if (attrNode.getId() == newAttributeDefinition.getId()) {
                String currentAttributeName = (String)attrNode.getProperty(Constants.PROPERTY_NAME);
                
                if (currentAttributeName.equals(Constants.PROPERTY_CREATION_DATE))
                    throw new InvalidArgumentException("Attribute \"creationDate\" can not be modified", Level.INFO);
                
                try {
                    tx = graphDb.beginTx();
                    if(newAttributeDefinition.getName() != null){
                        if (currentAttributeName.equals(Constants.PROPERTY_NAME))
                            throw new InvalidArgumentException("Attribute \"name\" can not be renamed", Level.INFO);
                        if (!newAttributeDefinition.getName().matches("^[a-zA-Z0-9_]*$"))
                            throw new InvalidArgumentException(String.format("Attribute %s contains invalid characters", newAttributeDefinition.getName()), Level.INFO);

                        Util.changeAttributeName(classNode, currentAttributeName, newAttributeDefinition.getName());
                    }
                    if(newAttributeDefinition.getDescription() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_DESCRIPTION, newAttributeDefinition.getDescription());
                    if(newAttributeDefinition.getDisplayName() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_DISPLAY_NAME, newAttributeDefinition.getDisplayName());
                    if(newAttributeDefinition.getType() != null){
                        if (currentAttributeName.equals(Constants.PROPERTY_NAME))
                            throw new InvalidArgumentException("Attribute \"name\" can only be a String", Level.INFO);
                        if (AttributeMetadata.isPrimitive((String)attrNode.getProperty(Constants.PROPERTY_TYPE)))
                            Util.changeAttributeTypeIfPrimitive(classNode, currentAttributeName, newAttributeDefinition.getType());
                        else
                            Util.changeAttributeTypeIfListType(classNode, currentAttributeName, newAttributeDefinition.getType());
                    }
                    if(newAttributeDefinition.isReadOnly() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_READ_ONLY, newAttributeDefinition.isReadOnly());
                    if(newAttributeDefinition.isVisible() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_VISIBLE, newAttributeDefinition.isVisible());
                    if(newAttributeDefinition.isAdministrative() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_ADMINISTRATIVE, newAttributeDefinition.isAdministrative());
                    if(newAttributeDefinition.isNoCopy() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_NO_COPY, newAttributeDefinition.isNoCopy());
                    if(newAttributeDefinition.isUnique() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_UNIQUE, newAttributeDefinition.isUnique());
                    tx.success();
                    tx.finish();
                    //Refresh cache for the affected classes
                    refreshCacheOn(classNode);
                    return;
                }catch(Exception ex){
                    Logger.getLogger("setAttributeProperties: " + ex.getMessage()); //NOI18N
                    if (tx != null){
                        tx.failure();
                        tx.finish();
                    }
                    throw new InvalidArgumentException(ex.getMessage(), Level.WARNING);
                } 
            }
        }//end for
        throw new MetadataObjectNotFoundException(String.format(
                    "Can not find attribute %s in class %s", newAttributeDefinition.getName(), className));
    }

    /**
     * Deletes an attribute belonging to a classMetadata
     * @param className
     * @param attributeName
     * @return true if success
     * @throws MetadataObjectNotFoundException if there is no a class with such className
     */
    @Override
    public void deleteAttribute(String className, String attributeName, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("deleteAttribute", ipAddress, sessionId);
        Transaction tx = null;
        if (attributeName.equals(Constants.PROPERTY_NAME))
            throw new InvalidArgumentException("Attribute \"name\" can not be deleted", Level.INFO);
        
        if (attributeName.equals(Constants.PROPERTY_CREATION_DATE))
            throw new InvalidArgumentException("Attribute \"creationDate\" can not be deleted", Level.INFO);
        
        Node classNode = classIndex.get(Constants.PROPERTY_NAME, attributeName).getSingle();

        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s", className));

        for (Relationship relationship : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE)) {
            Node attrNode = relationship.getEndNode();
            if (String.valueOf(attrNode.getProperty(Constants.PROPERTY_NAME)).equals(attributeName)){
                try {
                    tx = graphDb.beginTx();
                    if (AttributeMetadata.isPrimitive((String)attrNode.getProperty(Constants.PROPERTY_TYPE)))
                        Util.deleteAttributeIfPrimitive(classNode, attributeName);
                    else
                        Util.deleteAttributeIfListType(classNode, attributeName);
                    tx.success();
                    tx.finish();
                    return;
                }catch(Exception ex){
                    Logger.getLogger("deleteAttribute: " + ex.getMessage()); //NOI18N
                    if (tx != null){
                        tx.failure();
                        tx.finish();
                    }
                    throw new InvalidArgumentException(ex.getMessage(), Level.WARNING);
                } 
            }
        }//end for
        throw new MetadataObjectNotFoundException(String.format("Can not find an attribute with the name %s", attributeName));
    }

    /**
     * Deletes an attribute belonging to a classMetadata
     * @param classId
     * @param attributeName
     * @throws MetadataObjectNotFoundException if there is no a class with such classId
     */
    @Override
    public void deleteAttribute(long classId, String attributeName, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("deleteAttribute", ipAddress, sessionId);
        Transaction tx = null;
        if (attributeName.equals(Constants.PROPERTY_CREATION_DATE))
            throw new InvalidArgumentException("Attribute \"creationDate\" can not be deleted", Level.INFO);
        
        if (attributeName.equals(Constants.PROPERTY_NAME))
            throw new InvalidArgumentException("Attribute \"name\" can not be deleted", Level.INFO);
        
        Node classNode = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();
               
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Can not find a class with id %s", classId));

        for (Relationship relationship : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE)) {
            Node attrNode = relationship.getEndNode();
            if (String.valueOf(attrNode.getProperty(Constants.PROPERTY_NAME)).equals(attributeName)){
                try {
                    tx = graphDb.beginTx();
                    if (AttributeMetadata.isPrimitive((String)attrNode.getProperty(Constants.PROPERTY_TYPE)))
                        Util.deleteAttributeIfPrimitive(classNode, attributeName);
                    else
                        Util.deleteAttributeIfListType(classNode, attributeName);
                    tx.success();
                    tx.finish();
                    return;
                }catch(Exception ex){
                    Logger.getLogger("deleteAttribute: " + ex.getMessage()); //NOI18N
                    if (tx != null){
                        tx.failure();
                        tx.finish();
                    }
                    throw new InvalidArgumentException(ex.getMessage(), Level.WARNING);
                } 
            }
        }//end for
        throw new MetadataObjectNotFoundException(String.format(
                "Can not find an attribute with name %s", attributeName));
    }

    /**
     * Creates a new category
     * @param categoryDefinition
     * @return CategoryId
     */
    @Override
    public long createCategory(CategoryMetadata categoryDefinition, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException{
        aem.validateCall("createCategory", ipAddress, sessionId);
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
    public CategoryMetadata getCategory(String categoryName, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("getCategory", ipAddress, sessionId);
        CategoryMetadata ctgrMtdt = new CategoryMetadata();
        try
        {
            Node ctgNode = categoryIndex.get(Constants.PROPERTY_NAME, categoryName).getSingle();
            if (ctgNode == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a category with name %s", categoryName));
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
    public CategoryMetadata getCategory(long categoryId, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("getCategory", ipAddress, sessionId);
        CategoryMetadata ctgrMtdt = new CategoryMetadata();
        try {
            Node ctgNode = categoryIndex.get(Constants.PROPERTY_ID, categoryId).getSingle();
            if (ctgNode == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a category with id %s", categoryId));
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
    public void setCategoryProperties(CategoryMetadata categoryDefinition, String ipAddress, String sessionId) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node ctgr = categoryIndex.get(Constants.PROPERTY_NAME, categoryDefinition.getName()).getSingle();
            if (ctgr == null) {
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a category with name %s", categoryDefinition.getName()));
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
    public void deleteCategory(String categoryName, String ipAddress, String sessionId) {
        //TODO what about the classes?
    }

    @Override
    public void deleteCategory(int categoryId, String ipAddress, String sessionId) {
        //TODO what about the classes?
    }

    @Override
    public void addImplementor(String classWhichImplementsName, String interfaceToImplementName, String ipAddress, String sessionId) {
    }

    @Override
    public void removeImplementor(String classWhichImplementsName, String interfaceToBeRemovedName, String ipAddress, String sessionId) {
    }

    @Override
    public void addImplementor(int classWhichImplementsId, int interfaceToImplementId, String ipAddress, String sessionId) {
    }

    @Override
    public void removeImplementor(int classWhichImplementsId, int interfaceToBeRemovedId, String ipAddress, String sessionId) {
    }

    @Override
    public InterfaceMetadata getInterface(String interfaceName, String ipAddress, String sessionId) {
        return null;
    }

    @Override
    public InterfaceMetadata getInterface(int interfaceid, String ipAddress, String sessionId) {
        return null;
    }
    
    //TODO add String ipAddress, String sessionId
    @Override
    public List<ClassMetadataLight> getPossibleChildren(String parentClassName) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        //aem.validateCall("getPossibleChildren", ipAddress, sessionId);
        List<ClassMetadataLight> cml = new ArrayList<ClassMetadataLight>();
        
        List<String> cachedPossibleChildren = cm.getPossibleChildren(parentClassName);
        if (cachedPossibleChildren != null){
            for (String cachedPossibleChild : cachedPossibleChildren)
                cml.add(cm.getClass(cachedPossibleChild));
            return cml;
        }
        
        cachedPossibleChildren = new ArrayList<String>();
        
        try {
            String cypherQuery;
            if (parentClassName == null || parentClassName.equals(Constants.NODE_DUMMYROOT)){
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
                    cachedPossibleChildren.add((String)directChildNode.getProperty(Constants.PROPERTY_NAME));
                }
                if (indirectChildNode != null){
                    cml.add(Util.createClassMetadataFromNode(indirectChildNode));
                    cachedPossibleChildren.add((String)indirectChildNode.getProperty(Constants.PROPERTY_NAME));
                }
            }
            cm.putPossibleChildren(parentClassName, cachedPossibleChildren);
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
        return cml;
    }

    @Override
    public List<ClassMetadataLight> getPossibleChildrenNoRecursive(String parentClassName, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("getPossibleChildrenNoRecursive", ipAddress, sessionId);
        List<ClassMetadataLight> cml = new ArrayList<ClassMetadataLight>();
        try {
            String cypherQuery;
            if (parentClassName == null || parentClassName.equals(Constants.NODE_DUMMYROOT)){//The Dummy Rooot
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
    public void addPossibleChildren(long parentClassId, long[] possibleChildren, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, DatabaseException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("addPossibleChildren", ipAddress, sessionId);
        Transaction tx = null;
        Node parentNode;

        //It's NOT the dummy root
        if(parentClassId != -1) {
            parentNode = classIndex.get(Constants.PROPERTY_ID, parentClassId).getSingle();

            if (parentNode == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with id %1s", parentClassId));
            if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                throw new InvalidArgumentException(
                        String.format("%s is not a business class, thus can not be added to the containment hierarchy", (String)parentNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
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
                            "Can not find class with id %s", parentClassId));
                }
                if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)childNode.getProperty(Constants.PROPERTY_NAME))){
                    throw new InvalidArgumentException(
                            String.format("%s is not a business class, thus can not be added to the containment hierarchy", (String)childNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
                }
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                   for (Node subclassNode : Util.getAllSubclasses(childNode)){
                       for (ClassMetadataLight possibleChild : currentPossibleChildren){
                            if (possibleChild.getId() == subclassNode.getId()){
                                throw new InvalidArgumentException(String.format("A subclass of %s is already a possible child for instances of %s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
                            }
                       }
                   }
                }
                else{
                    for (ClassMetadataLight possibleChild : currentPossibleChildren){
                        if (possibleChild.getId() == childNode.getId()){
                            throw new InvalidArgumentException(String.format("Class %s is already a possible child for instances of %s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
                        }
                    }
                }
                parentNode.createRelationshipTo(childNode, RelTypes.POSSIBLE_CHILD);
                 //Refresh cache
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                    for(ClassMetadataLight subclass : getSubClassesLight((String)childNode.getProperty(Constants.PROPERTY_NAME), false, false, ipAddress, sessionId))
                        cm.putPossibleChild((String)parentNode.getProperty(Constants.PROPERTY_NAME),subclass.getName());
                }else
                    cm.putPossibleChild((String)parentNode.getProperty(Constants.PROPERTY_NAME), (String)childNode.getProperty(Constants.PROPERTY_NAME));

                tx.success();
                tx.finish();
            }
        }catch (MetadataObjectNotFoundException ex) {
            tx.failure();
            tx.finish();
            throw ex;
        }
        catch (InvalidArgumentException ex) {
            tx.failure();
            tx.finish();
            throw ex;
        }
    }

    @Override
    public void addPossibleChildren(String parentClassName, String[] possibleChildren, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("addPossibleChildren", ipAddress, sessionId);
        Transaction tx = null;
        Node parentNode;
        boolean isDummyRoot = false;

        if(parentClassName != null) {
            parentNode = classIndex.get(Constants.PROPERTY_NAME, parentClassName).getSingle();

            if (parentNode == null){
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find class %s", parentClassName));
            }
            if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(Constants.PROPERTY_NAME))){
                throw new InvalidArgumentException(
                        String.format("%s is not a business class, thus can not be added to the containment hierarchy", (String)parentNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
            }
        }
        else{
            Node referenceNode = graphDb.getReferenceNode();
            Relationship rel = referenceNode.getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.OUTGOING);
            parentNode = rel.getEndNode();

            if(!(Constants.NODE_DUMMYROOT).equals((String)parentNode.getProperty(Constants.PROPERTY_NAME))){
                    throw new MetadataObjectNotFoundException("DummyRoot node is corrupted");
            }
            else{
                isDummyRoot = true;
            }
        }
        List<ClassMetadataLight> currentPossibleChildren = getPossibleChildren(isDummyRoot ? null : (String)parentNode.getProperty(Constants.PROPERTY_NAME));
        tx = graphDb.beginTx();

        try{
            for (String possibleChildName : possibleChildren) {
                Node childNode = classIndex.get(Constants.PROPERTY_NAME, possibleChildName).getSingle();
                if (childNode == null){
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find class %s", possibleChildName));
                }
                if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)childNode.getProperty(Constants.PROPERTY_NAME))){
                    throw new InvalidArgumentException(
                            String.format("%s is not a business class, thus can not be added to the containment hierarchy", (String)childNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
                }
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                   for (Node subclassNode : Util.getAllSubclasses(childNode)){
                       for (ClassMetadataLight possibleChild : currentPossibleChildren){
                            if (possibleChild.getId() == subclassNode.getId()){
                                throw new InvalidArgumentException(String.format("A subclass of %s is already a possible child for instances of %s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
                            }
                       }
                   }
                }
                else{
                    for (ClassMetadataLight possibleChild : currentPossibleChildren){
                        if (possibleChild.getId() == childNode.getId()){
                            throw new InvalidArgumentException(String.format("Class %s is already a possible child for instances of %s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)), Level.INFO);
                        }
                    }
                }
                parentNode.createRelationshipTo(childNode, RelTypes.POSSIBLE_CHILD);
                //Refresh cache
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                    for(ClassMetadataLight subclass : getSubClassesLight((String)childNode.getProperty(Constants.PROPERTY_NAME), false, false, ipAddress, sessionId))
                        cm.putPossibleChild(parentClassName,subclass.getName());
                }
                else
                    cm.putPossibleChild(parentClassName, (String)childNode.getProperty(Constants.PROPERTY_NAME));
                
                tx.success();
                tx.finish();
            }
        }catch (MetadataObjectNotFoundException ex) {
            tx.failure();
            tx.finish();
            throw ex;
        }
        catch (InvalidArgumentException ex) {
            tx.failure();
            tx.finish();
            throw ex;
        }
    }
    
    @Override
    public void removePossibleChildren(long parentClassId, long[] childrenToBeRemoved, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("removePossibleChildren", ipAddress, sessionId);
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
    @Override
    public List<ClassMetadataLight> getUpstreamContainmentHierarchy(String className, boolean recursive, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("getUpstreamContainmentHierarchy", ipAddress, sessionId);
        Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
        if (classNode == null){
           throw new MetadataObjectNotFoundException(String.format(
                        "Can not find class %1s", className));
        }
        List<ClassMetadataLight> res = new ArrayList<ClassMetadataLight>();
        
        String cypherQuery = "START classNode=node:classes(name=\""+className+"\") "+
                             "MATCH possibleParentClassNode-[:POSSIBLE_CHILD"+(recursive ? "*" : "")+ "]->classNode "+
                             "WHERE possibleParentClassNode.name <> \""+ Constants.NODE_DUMMYROOT +
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
    public boolean isSubClass(String allegedParent, String classToBeEvaluated, String ipAddress, String sessionId) throws ApplicationObjectNotFoundException, NotAuthorizedException{
        aem.validateCall("isSubClass", ipAddress, sessionId);
        try {
            return cm.isSubClass(allegedParent, classToBeEvaluated);
        } catch (MetadataObjectNotFoundException ex) {
            return false;
        }
    }
    
    /**
     * HELPERS
     */
    private void refreshCacheOn(Node rootClassNode){
        TraversalDescription UPDATE_TRAVERSAL = Traversal.description().
                    breadthFirst().relationships(RelTypes.EXTENDS, Direction.INCOMING);
        
        for(Path p : UPDATE_TRAVERSAL.traverse(rootClassNode)){
            cm.removeClass((String)p.endNode().getProperty(Constants.PROPERTY_NAME));
            cm.putClass(Util.createClassMetadataFromNode(p.endNode()));
        }
    }
    
   public final void buildContainmentCache() {
       cm.clearContainmentCache();
       for (Node classNode : classIndex.query(Constants.PROPERTY_ID, "*")){
            ClassMetadata aClass = Util.createClassMetadataFromNode(classNode);
            cm.putClass(aClass);
            cm.putPossibleChildren(aClass.getName(), aClass.getPossibleChildren());
        }
        try{
            List<String> possibleChildrenOfRoot = new ArrayList<String>();
            for (ClassMetadataLight aClass : getPossibleChildren(null))
                possibleChildrenOfRoot.add(aClass.getName());
            cm.putPossibleChildren(Constants.NODE_DUMMYROOT, possibleChildrenOfRoot);
        }catch(Exception e){}
   }
   
   public void setApplicationEntityManager(ApplicationEntityManagerImpl aem) {
        this.aem = aem;
   }

    public void createAttribute(long classId, AttributeMetadata attributeDefinition, String ipAddress, String sessionId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException, InvalidArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

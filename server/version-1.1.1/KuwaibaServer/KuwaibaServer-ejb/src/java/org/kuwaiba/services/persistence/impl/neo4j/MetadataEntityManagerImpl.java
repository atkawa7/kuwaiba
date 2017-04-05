/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.services.persistence.cache.CacheManager;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.services.persistence.util.Util;
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
     * Instance of application entity manager
     */
    ApplicationEntityManager aem;
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
     * @param cmn
     * @param aem
     */
    public MetadataEntityManagerImpl(ConnectionManager cmn, ApplicationEntityManager aem) {
        this();
        this.aem = aem;
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
                    Util.createAttribute(classNode, attributeMetadata);
            }
            
            //Now we make our class to inherit the atributes from its parent class (except for the root class, RootObject)
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
                        //TODO: This block of code only exists because the class hierachy file used to reset the data model is redundant. Once its fixed, please remove it
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
                        
                        Node newAttrNode = graphDb.createNode();
                        newAttrNode.setProperty(Constants.PROPERTY_NAME, attributeName);
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
                else
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find parent class with name %s", classDefinition.getParentClassName()));
            }//end else not rootNode
            
            buildClassCache();
            tx.success();
            return id;
        }
    }

    @Override
    public void setClassProperties (ClassMetadata newClassDefinition) 
            throws MetadataObjectNotFoundException, InvalidArgumentException 
    {
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
                classIndex.add(classMetadata, Constants.PROPERTY_NAME, newClassDefinition.getName());
                buildClassCache();
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
                    setAttributeProperties(newClassDefinition.getId(), attr);
            }        
            tx.success();
            cm.removeClass(formerName);
            cm.putClass(Util.createClassMetadataFromNode(classMetadata));
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
        List<ClassMetadataLight> cml = new ArrayList<>();
        
        ClassMetadata aClass = cm.getClass(className);
        if (aClass == null)
            throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s", className));

        String cypherQuery = "START inventory = node:classes({className}) ".concat(
                             "MATCH inventory <-[:").concat(RelTypes.EXTENDS.toString()).concat("*]-classmetadata ").concat(
                             includeAbstractClasses ? "" : "WHERE classmetadata.abstract <> true ").concat(
                             "RETURN classmetadata ").concat(
                             "ORDER BY classmetadata.name ASC");

        Map<String, Object> params = new HashMap<>();
        params.put("className", "name:"+ className);//NOI18N
            
        try (Transaction tx = graphDb.beginTx()) {
            Result result = graphDb.execute(cypherQuery, params);
            Iterator<Node> n_column = result.columnAs("classmetadata");
            if (includeSelf && !aClass.isAbstract())
                cml.add(aClass);
            for (Node node : IteratorUtil.asIterable(n_column))
                 cml.add(Util.createClassMetadataLightFromNode(node));
            tx.success();
        }
        return cml;
    }


    @Override
    public List<ClassMetadataLight> getSubClassesLightNoRecursive(String className, 
            boolean includeAbstractClasses, boolean includeSelf) 
            throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> classManagerResultList = new ArrayList<>();
            
        ClassMetadata aClass = cm.getClass(className);
        if (aClass == null)
            throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s", className));

        String cypherQuery = "START inventory = node:classes({className}) ".concat(
                             "MATCH inventory <-[:").concat(RelTypes.EXTENDS.toString()).concat("]-classmetadata ").concat(
                             includeAbstractClasses ? "" : "WHERE classmetadata.abstract <> true ").concat(
                             "RETURN classmetadata ").concat(
                             "ORDER BY classmetadata.name ASC");

        Map<String, Object> params = new HashMap<>();
        params.put("className", "name:"+ className);//NOI18N

        try (Transaction tx = graphDb.beginTx())
        {
            Result result = graphDb.execute(cypherQuery, params);
            Iterator<Node> n_column = result.columnAs("classmetadata");
            if (includeSelf)
                classManagerResultList.add(aClass);
            for (Node node : IteratorUtil.asIterable(n_column))
                 classManagerResultList.add(Util.createClassMetadataLightFromNode(node));
        }
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
        ClassMetadata clmt = null;
        try (Transaction tx = graphDb.beginTx())
        {
            Node node = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (node == null)
                throw new MetadataObjectNotFoundException(String.format(
                        "Can not find a class with name %s", className));
            clmt = Util.createClassMetadataFromNode(node);
        }
        return clmt;
    }

    @Override
    public void moveClass(String classToMoveName, String targetParentClassName) 
            throws MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx())
        {
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
        } 
    }

    @Override
    public void moveClass(long classToMoveId, long targetParentClassId) throws MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx())
        {
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
        } 
    }

    @Override
    public void createAttribute(String className, AttributeMetadata attributeDefinition) 
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
            
            Util.createAttribute(classNode, attributeDefinition);
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
        
            Util.createAttribute(classNode, attributeDefinition);
            //Refresh cache for the affected classes
            refreshCacheOn(classNode);
            tx.success();
            
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
    public void setAttributeProperties(long classId, AttributeMetadata newAttributeDefinition) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        try(Transaction tx = graphDb.beginTx())
        {
            Node classNode = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();

            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Can not find a class with id %s", classId));

            for (Relationship relationship : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE)) {
                Node attrNode = relationship.getEndNode();
                if (attrNode.getId() == newAttributeDefinition.getId()) 
                {
                    String currentAttributeName = (String)attrNode.getProperty(Constants.PROPERTY_NAME);

                    if (currentAttributeName.equals(Constants.PROPERTY_CREATION_DATE))
                        throw new InvalidArgumentException("Attribute \"creationDate\" can not be modified");

                    if(newAttributeDefinition.getName() != null){
                        if (currentAttributeName.equals(Constants.PROPERTY_NAME))
                            throw new InvalidArgumentException("Attribute \"name\" can not be renamed");
                        if (!newAttributeDefinition.getName().matches("^[a-zA-Z0-9_]*$"))
                            throw new InvalidArgumentException(String.format("Attribute %s contains invalid characters", newAttributeDefinition.getName()));
                        
                        Util.changeAttributeName(classNode, currentAttributeName, newAttributeDefinition.getName());
                    }
                    if(newAttributeDefinition.getDescription() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_DESCRIPTION, newAttributeDefinition.getDescription());
                    if(newAttributeDefinition.getDisplayName() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_DISPLAY_NAME, newAttributeDefinition.getDisplayName());
                    if(newAttributeDefinition.getType() != null){
                        if (currentAttributeName.equals(Constants.PROPERTY_NAME))
                            throw new InvalidArgumentException("Attribute \"name\" can only be a String");
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
                    //Refresh cache for the affected classes
                    refreshCacheOn(classNode);
                    tx.success();                    
                    return;
                }
            }//end for
        } 
        throw new MetadataObjectNotFoundException(String.format(
                    "Can not find attribute %s in the class with id %s", newAttributeDefinition.getName(), classId));
    }
    
    @Override
    public void setAttributeProperties (String className, AttributeMetadata newAttributeDefinition) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        try(Transaction tx = graphDb.beginTx()) {
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();

            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Can not find a class with name %s", className));

            for (Relationship relationship : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE)) {
                Node attrNode = relationship.getEndNode();
                if (attrNode.getId() == newAttributeDefinition.getId()) {
                    String currentAttributeName = (String)attrNode.getProperty(Constants.PROPERTY_NAME);

                    if (currentAttributeName.equals(Constants.PROPERTY_CREATION_DATE))
                        throw new InvalidArgumentException("Attribute \"creationDate\" can not be modified");

                    if(newAttributeDefinition.getName() != null){
                        if (currentAttributeName.equals(Constants.PROPERTY_NAME))
                            throw new InvalidArgumentException("Attribute \"name\" can not be renamed");
                        if (!newAttributeDefinition.getName().matches("^[a-zA-Z0-9_]*$"))
                            throw new InvalidArgumentException(String.format("Attribute %s contains invalid characters", newAttributeDefinition.getName()));

                        Util.changeAttributeName(classNode, currentAttributeName, newAttributeDefinition.getName());
                    }
                    if(newAttributeDefinition.getDescription() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_DESCRIPTION, newAttributeDefinition.getDescription());
                    if(newAttributeDefinition.getDisplayName() != null)
                        Util.changeAttributeProperty(classNode, currentAttributeName, Constants.PROPERTY_DISPLAY_NAME, newAttributeDefinition.getDisplayName());
                    if(newAttributeDefinition.getType() != null){
                        if (currentAttributeName.equals(Constants.PROPERTY_NAME))
                            throw new InvalidArgumentException("Attribute \"name\" can only be a String");
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
                    //Refresh cache for the affected classes
                    refreshCacheOn(classNode);
                    tx.success();
                    return;
                }
            }//end for
        }
        throw new MetadataObjectNotFoundException(String.format(
                    "Can not find attribute %s in class %s", newAttributeDefinition.getName(), className));
    }
    
    @Override
    public void deleteAttribute(String className, String attributeName) 
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        if (attributeName.equals(Constants.PROPERTY_NAME))
            throw new InvalidArgumentException("Attribute \"name\" can not be deleted");
        
        if (attributeName.equals(Constants.PROPERTY_CREATION_DATE))
            throw new InvalidArgumentException("Attribute \"creationDate\" can not be deleted");
        
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
        if (attributeName.equals(Constants.PROPERTY_CREATION_DATE))
            throw new InvalidArgumentException("Attribute \"creationDate\" can not be deleted");
        
        if (attributeName.equals(Constants.PROPERTY_NAME))
            throw new InvalidArgumentException("Attribute \"name\" can not be deleted");
        
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
    public List<ClassMetadataLight> getPossibleChildren(String parentClassName) 
            throws MetadataObjectNotFoundException   {
        
        List<ClassMetadataLight> classMetadataResultList = new ArrayList<>();
        List<String> cachedPossibleChildren = cm.getPossibleChildren(parentClassName);
        
        if (cachedPossibleChildren != null) {
            for (String cachedPossibleChild : cachedPossibleChildren)
                classMetadataResultList.add(cm.getClass(cachedPossibleChild));
            return classMetadataResultList;
        }
        cachedPossibleChildren = new ArrayList<>();
        String cypherQuery;
        Map<String, Object> params = new HashMap<>();
        if (parentClassName == null || parentClassName.equals(Constants.NODE_DUMMYROOT)) {
            cypherQuery = "MATCH (n:root {name:\"" + Constants.NODE_DUMMYROOT + "\"})-[:POSSIBLE_CHILD]->directChild " +
                    "OPTIONAL MATCH directChild<-[:EXTENDS*]-subClass " +
                    "WHERE subClass.abstract = false OR subClass IS NULL " +
                    "RETURN directChild, subClass " +
                    "ORDER BY directChild.name,subClass.name ASC ";
        }
        else {
            cypherQuery = "START parentClassNode=node:classes(name = {className}) " +
                        "MATCH (parentClassNode:class)-[:POSSIBLE_CHILD]->(directChild) " +
                        "OPTIONAL MATCH (directChild)<-[:EXTENDS*]-(subClass) " +
                        "RETURN directChild, subClass "+
                        "ORDER BY directChild.name,subClass.name ASC";
            params.put(Constants.PROPERTY_CLASS_NAME, "className:" + parentClassName);//NOI18N
        }
        try (Transaction tx = graphDb.beginTx())
        {
            Result result = graphDb.execute(cypherQuery, params);
            while (result.hasNext()){
                Map<String,Object> entry = result.next();
                Node directChildNode =  (Node)entry.get("directChild");
                Node indirectChildNode =  (Node)entry.get("subClass");
                if (!(Boolean)directChildNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                    classMetadataResultList.add(Util.createClassMetadataFromNode(directChildNode));
                    cachedPossibleChildren.add((String)directChildNode.getProperty(Constants.PROPERTY_NAME));
                }
                if (indirectChildNode != null){
                    classMetadataResultList.add(Util.createClassMetadataFromNode(indirectChildNode));
                    cachedPossibleChildren.add((String)indirectChildNode.getProperty(Constants.PROPERTY_NAME));
                }
            }
            cm.putPossibleChildren(parentClassName == null ? 
                    Constants.NODE_DUMMYROOT : parentClassName, cachedPossibleChildren);
        }
        return classMetadataResultList;
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
    public void addPossibleChildren(long parentClassId, long[] possibleChildren)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        Node parentNode;
        try(Transaction tx = graphDb.beginTx()) {
            if(parentClassId != -1) {
                parentNode = classIndex.get(Constants.PROPERTY_ID, parentClassId).getSingle();

                if (parentNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find a class with id %1s", parentClassId));
                if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format("%s is not a business class, thus can not be added to the containment hierarchy", (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
            }else
                parentNode = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();

            List<ClassMetadataLight> currentPossibleChildren = getPossibleChildren(parentNode);
            
            for (long id : possibleChildren) {
                Node childNode = classIndex.get(Constants.PROPERTY_ID, id).getSingle();

                if (childNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find class with id %s", parentClassId));
                
                if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)childNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format("%s is not a business class, thus can not be added to the containment hierarchy", (String)childNode.getProperty(Constants.PROPERTY_NAME)));
                
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                   for (Node subclassNode : Util.getAllSubclasses(childNode)){
                       for (ClassMetadataLight possibleChild : currentPossibleChildren){
                            if (possibleChild.getId() == subclassNode.getId())
                                throw new InvalidArgumentException(String.format("A subclass of %s is already a possible child for instances of %s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
                       }
                   }
                }
                else{
                    for (ClassMetadataLight possibleChild : currentPossibleChildren){
                        if (possibleChild.getId() == childNode.getId())
                            throw new InvalidArgumentException(String.format("Class %s is already a possible child for instances of %s", (String)childNode.getProperty(Constants.PROPERTY_NAME), (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
                    }
                }
                parentNode.createRelationshipTo(childNode, RelTypes.POSSIBLE_CHILD);
                 //Refresh cache
                if ((Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                    for(ClassMetadataLight subclass : getSubClassesLight((String)childNode.getProperty(Constants.PROPERTY_NAME), false, false))
                        cm.putPossibleChild((String)parentNode.getProperty(Constants.PROPERTY_NAME),subclass.getName());
                }else
                    cm.putPossibleChild((String)parentNode.getProperty(Constants.PROPERTY_NAME), (String)childNode.getProperty(Constants.PROPERTY_NAME));
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
                
                if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            String.format("%s is not a business class, thus can not be added to the containment hierarchy", (String)parentNode.getProperty(Constants.PROPERTY_NAME)));
            } else {
                parentNode = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();

                if(!(Constants.NODE_DUMMYROOT).equals((String)parentNode.getProperty(Constants.PROPERTY_NAME)))
                        throw new MetadataObjectNotFoundException("DummyRoot node is corrupted");
            }
            List<ClassMetadataLight> currentPossibleChildren = getPossibleChildren(parentNode);
        
            for (String possibleChildName : possibleChildren) {
                Node childNode = classIndex.get(Constants.PROPERTY_NAME, possibleChildName).getSingle();
                if (childNode == null)
                    throw new MetadataObjectNotFoundException(String.format(
                            "Can not find class %s", possibleChildName));
                
                if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, (String)childNode.getProperty(Constants.PROPERTY_NAME)))
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
                    if(childNode.getId() == possiblechild.getId()){
                        rel.delete();
                        String parentClassName = (String)parentNode.getProperty(Constants.PROPERTY_NAME);
                        if (cm.getClass((String)childNode.getProperty(Constants.PROPERTY_NAME)).isAbstract()){
                            for(Node subClass : Util.getAllSubclasses(childNode))
                                cm.removePossibleChild(parentClassName, (String)subClass.getProperty(Constants.PROPERTY_NAME));
                        }
                        else
                            cm.removePossibleChild(parentClassName, (String)childNode.getProperty(Constants.PROPERTY_NAME));
                        break;
                    }
                }//end for
            }//end for
            tx.success();
        }
    }

    @Override
    public List<ClassMetadataLight> getUpstreamContainmentHierarchy(String className, 
            boolean recursive) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> res = new ArrayList<>();
        try(Transaction tx = graphDb.beginTx())
        {
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
    public void setSpecialRelationshipDisplayName(String relationshipName, String relationshipDisplayName) {
        relationshipDisplayNames.put(relationshipName, relationshipDisplayName);
    }
    
    @Override
    public String getSpecialRelationshipDisplayName(String relationshipName) {
        String displayName = relationshipDisplayNames.get(relationshipName);
        return displayName == null ? relationshipName : displayName;
    }
     
    @Override
    public boolean isSubClass(String allegedParent, String classToBeEvaluated) {
        return cm.isSubClass(allegedParent, classToBeEvaluated);
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
            cm.putClass(Util.createClassMetadataFromNode(p.endNode()));
        }
    }
   
   private List<ClassMetadataLight> getPossibleChildren(Node parentClassNode) {
       List<ClassMetadataLight> possibleChildren = new ArrayList<>();
       
       for (Relationship rel : parentClassNode.getRelationships(Direction.OUTGOING, RelTypes.POSSIBLE_CHILD)) {
           Node possibleChildNode = rel.getEndNode();
           if (possibleChildNode.hasProperty(Constants.PROPERTY_ABSTRACT) && (boolean)possibleChildNode.getProperty(Constants.PROPERTY_ABSTRACT))
               possibleChildren.addAll(getPossibleChildren(possibleChildNode));
           else
               possibleChildren.add(Util.createClassMetadataLightFromNode(possibleChildNode));
       }
       
       return possibleChildren;
       
   }
    
   //Callers must handle associated transactions
   private void buildClassCache() {
        cm.clearClassCache();
        
        for (Node classNode : classIndex.query(Constants.PROPERTY_ID, "*")) {
             ClassMetadata aClass = Util.createClassMetadataFromNode(classNode);
             cm.putClass(aClass);
             cm.putPossibleChildren(aClass.getName(), aClass.getPossibleChildren());
        }
        //Only the DummyRoot is not cached. It will be cached on demand later
   }
}
/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.persistenceservice.impl;

import org.kuwaiba.apis.persistence.interfaces.MetadataEntityManager;
import org.kuwaiba.psremoteinterfaces.MetadataEntityManagerRemote;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.InterfaceMetadata;
import org.kuwaiba.persistenceservice.caching.CacheManager;
import org.kuwaiba.persistenceservice.util.Util;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.helpers.collection.IteratorUtil;

/**
 * MetadataEntityManager implementation
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class MetadataEntityManagerImpl implements MetadataEntityManager, MetadataEntityManagerRemote {

    public static final String PROPERTY_PRIVILEGES = "privileges"; //NOI18N
    public static final String PROPERTY_NAME = "name"; //NOI18N
    public static final String PROPERTY_CREATION_DATE = "creationDate"; //NOI18N
    public static final String PROPERTY_DISPLAY_NAME = "displayName"; //NOI18N
    public static final String PROPERTY_TYPE = "type"; //NOI18N
    public static final String PROPERTY_ADMINISTRATIVE = "administrative"; //NOI18N
    public static final String PROPERTY_VISIBLE = "isVisible"; //NOI18N
    public static final String PROPERTY_DESCRIPTION = "description"; //NOI18N
    public static final String PROPERTY_READONLY = "readOnly"; //NOI18N
    public static final String PROPERTY_ID = "id"; //NOI18N
    public static final String PROPERTY_ABSTRACT = "abstract"; //NOI18N
    public static final String PROPERTY_CUSTOM = "custom"; //NOI18N
    public static final String PROPERTY_COUNTABLE = "countable"; //NOI18N
    public static final String PROPERTY_COLOR = "color"; //NOI18N
    public static final String PROPERTY_ICON = "icon"; //NOI18N
    public static final String PROPERTY_SMALL_ICON = "smallIcon"; //NOI18N
    public static final String PROPERTY_NO_COPY = "noCopy"; //NOI18N
    public static final String PROPERTY_NO_SERIALIZE = "noSerialize"; //NOI18N
    public static final String PROPERTY_UNIQUE = "unique"; //NOI18N
    /**
     * How an attribute should be mapped (as a Float, Integer, relationship, etc)
     */
    public static final String PROPERTY_MAPPING = "mapping"; //NOI18N
    /**
     * Root for all business classes
     */
    public static final String CLASS_INVENTORYOBJECT = "InventoryObject"; //NOI18N ID 286
    /**
     * Root for all list types class name
     */
    public static final String CLASS_GENERICOBJECTLIST = "GenericObjectList"; //NOI18N ID 27
    /**
     * Root for all classes that can have a view attached
     */
    public static final String CLASS_VIEWABLEOBJECT = "ViewableObject"; //NOI18N
    /**
     * Class hierarchy root
     */
    public static final String CLASS_ROOTOBJECT = "RootObject"; //NOI18N
    /**
     * Dummy root node name
     */
    public static final String DUMMYROOT = "DummyRoot"; //NOI18N
    
    /**
     * Label used for the class index
     */
    public static final String INDEX_CLASS = "classes"; //NOI18N
    /**
     * Label used for the category index
     */
    public static final String INDEX_CATEGORY = "categories"; //NOI18N
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
        classIndex = graphDb.index().forNodes(INDEX_CLASS);
        categoryIndex = graphDb.index().forNodes(INDEX_CATEGORY);
        cm.clear();
        for (Node classNode : classIndex.query(MetadataEntityManagerImpl.PROPERTY_ID, "*")){
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
     * Creates a classmetadata with its attributes(some new attributes and others
     * extended from the parent) and a category (if the category does not exist it will be created).
     * @param classDefinition
     * @return the Id of the newClassMetadata
     * @throws ClassNotFoundException if there's no Parent Class whit the ParentId
     */
    @Override
    public Long createClass(ClassMetadata classDefinition) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        Long id;

        classDefinition = Util.createDefaultClassMetadata(classDefinition);

        try {
            tx = graphDb.beginTx();
            //The root must exist
            Node referenceNode = graphDb.getReferenceNode();
            Relationship rootRel = referenceNode.getSingleRelationship(
                    RelTypes.ROOT, Direction.BOTH);

            if (rootRel == null && classDefinition.getName().equals(CLASS_ROOTOBJECT)) {
                Node rootNode = graphDb.createNode();
                Node dummyRootNode = graphDb.createNode();

                rootNode.setProperty(PROPERTY_NAME, classDefinition.getName());
                rootNode.setProperty(PROPERTY_DISPLAY_NAME, classDefinition.getDisplayName());
                rootNode.setProperty(PROPERTY_CUSTOM, classDefinition.isCustom());
                rootNode.setProperty(PROPERTY_COUNTABLE, classDefinition.isCountable());
                rootNode.setProperty(PROPERTY_COLOR, classDefinition.getColor());
                rootNode.setProperty(PROPERTY_DESCRIPTION, classDefinition.getDescription());
                rootNode.setProperty(PROPERTY_ABSTRACT, classDefinition.isAbstractClass());
                rootNode.setProperty(PROPERTY_ICON, classDefinition.getIcon());
                rootNode.setProperty(PROPERTY_SMALL_ICON, classDefinition.getSmallIcon());
                rootNode.setProperty(PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());

                dummyRootNode.setProperty(PROPERTY_NAME, DUMMYROOT);
                dummyRootNode.setProperty(PROPERTY_DISPLAY_NAME, DUMMYROOT);
                dummyRootNode.setProperty(PROPERTY_CUSTOM, classDefinition.isCustom());
                dummyRootNode.setProperty(PROPERTY_COUNTABLE, classDefinition.isCountable());
                dummyRootNode.setProperty(PROPERTY_COLOR, classDefinition.getColor());
                dummyRootNode.setProperty(PROPERTY_DESCRIPTION, classDefinition.getDescription());
                dummyRootNode.setProperty(PROPERTY_ABSTRACT, classDefinition.isAbstractClass());
                dummyRootNode.setProperty(PROPERTY_ICON, classDefinition.getIcon());
                dummyRootNode.setProperty(PROPERTY_SMALL_ICON, classDefinition.getSmallIcon());
                dummyRootNode.setProperty(PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());

                classIndex.putIfAbsent(rootNode, PROPERTY_NAME, classDefinition.getName());
                classIndex.putIfAbsent(rootNode, PROPERTY_ID, rootNode.getId());

                referenceNode.createRelationshipTo(rootNode, RelTypes.ROOT);
                referenceNode.createRelationshipTo(dummyRootNode, RelTypes.DUMMY_ROOT);

                id = rootNode.getId();

            }//end if is rootNode
            else {
                //The ClassNode
                Node classNode = graphDb.createNode();

                classNode.setProperty(PROPERTY_NAME, classDefinition.getName());
                classNode.setProperty(PROPERTY_DISPLAY_NAME, classDefinition.getDisplayName());
                classNode.setProperty(PROPERTY_CUSTOM, classDefinition.isCustom());
                classNode.setProperty(PROPERTY_COUNTABLE, classDefinition.isCountable());
                classNode.setProperty(PROPERTY_COLOR, classDefinition.getColor());
                classNode.setProperty(PROPERTY_DESCRIPTION, classDefinition.getDescription());
                classNode.setProperty(PROPERTY_ABSTRACT, classDefinition.isAbstractClass());
                classNode.setProperty(PROPERTY_ICON, classDefinition.getIcon());
                classNode.setProperty(PROPERTY_SMALL_ICON, classDefinition.getSmallIcon());
                classNode.setProperty(PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());


                id = classNode.getId();

                classIndex.putIfAbsent(classNode, PROPERTY_NAME, classDefinition.getName());
                classIndex.putIfAbsent(classNode, PROPERTY_ID, classNode.getId());

                //Category
                //if the category already exists
                if (classDefinition.getCategory() != null) {
                    Node ctgrNode = categoryIndex.get(PROPERTY_NAME, classDefinition.getCategory().getName()).getSingle();
                    if (ctgrNode == null) {
                        Long ctgrId = createCategory(classDefinition.getCategory());
                        ctgrNode = categoryIndex.get(PROPERTY_ID, ctgrId).getSingle();
                    }
                    classNode.createRelationshipTo(ctgrNode, RelTypes.BELONGS_TO_GROUP);

                }//end if is category null

                Node parentNode = classIndex.get(PROPERTY_NAME, classDefinition.getParentClassName()).getSingle();

                if (parentNode != null) {
                    classNode.createRelationshipTo(parentNode, RelTypes.EXTENDS);
                    Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
                    //Set extendended attributes from parent
                    for (Relationship rel : relationships) {
                        Node parentAttrNode = rel.getEndNode();
                        Node newAttrNode = graphDb.createNode();

                        newAttrNode.setProperty(PROPERTY_NAME, parentAttrNode.getProperty(PROPERTY_NAME));
                        newAttrNode.setProperty(PROPERTY_DESCRIPTION, parentAttrNode.getProperty(PROPERTY_DESCRIPTION));
                        newAttrNode.setProperty(PROPERTY_DISPLAY_NAME, parentAttrNode.getProperty(PROPERTY_DISPLAY_NAME));
                        newAttrNode.setProperty(PROPERTY_TYPE, parentAttrNode.getProperty(PROPERTY_TYPE));
                        newAttrNode.setProperty(PROPERTY_READONLY, parentAttrNode.getProperty(PROPERTY_READONLY));
                        newAttrNode.setProperty(PROPERTY_VISIBLE, parentAttrNode.getProperty(PROPERTY_VISIBLE));
                        newAttrNode.setProperty(PROPERTY_ADMINISTRATIVE, parentAttrNode.getProperty(PROPERTY_ADMINISTRATIVE));
                        newAttrNode.setProperty(PROPERTY_MAPPING, parentAttrNode.getProperty(PROPERTY_MAPPING));
                        newAttrNode.setProperty(PROPERTY_NO_COPY, parentAttrNode.getProperty(PROPERTY_NO_COPY));
                        newAttrNode.setProperty(PROPERTY_NO_SERIALIZE, parentAttrNode.getProperty(PROPERTY_NO_SERIALIZE));
                        newAttrNode.setProperty(PROPERTY_UNIQUE, parentAttrNode.getProperty(PROPERTY_UNIQUE));

                        classNode.createRelationshipTo(newAttrNode, RelTypes.HAS_ATTRIBUTE);
                    }
                }//end if there is a Parent
                else {
                    throw new MetadataObjectNotFoundException(Util.formatString(
                            "Can not find the parent Class with the name %1s", classDefinition.getParentClassName()));
                }

            }//end else not rootNode

            //Attributes
            if (classDefinition.getAttributes() != null) {
                for (AttributeMetadata at : classDefinition.getAttributes()) {
                    AttributeMetadata newAttr = getAttribute(id, at.getName());
                    if (newAttr == null) {
                        addAttribute(id, at);
                    }
                }
            }

            tx.success();

            return id;
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
        }
 
    }

    /**
     * Changes a classmetadata definiton 
     * @param newClassDefinition
     * @return true if success
     * @throws ClassNotFoundException if there is no class with such classId
     */
    @Override
    public void changeClassDefinition(ClassMetadata newClassDefinition) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node newcm = classIndex.get(PROPERTY_NAME, newClassDefinition.getName()).getSingle();
            if (newcm == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class with the id %1s", newClassDefinition.getName()));
            }

            newcm.setProperty(PROPERTY_NAME, newClassDefinition.getName());
            newcm.setProperty(PROPERTY_DISPLAY_NAME, newClassDefinition.getDisplayName());
            newcm.setProperty(PROPERTY_CUSTOM, newClassDefinition.isCustom());
            newcm.setProperty(PROPERTY_COUNTABLE, newClassDefinition.isCountable());
            newcm.setProperty(PROPERTY_COLOR, newClassDefinition.getColor());
            newcm.setProperty(PROPERTY_DESCRIPTION, newClassDefinition.getDescription());
            newcm.setProperty(PROPERTY_ABSTRACT, newClassDefinition.isAbstractClass());
            newcm.setProperty(PROPERTY_ICON, newClassDefinition.getIcon());
            newcm.setProperty(PROPERTY_SMALL_ICON, newClassDefinition.getSmallIcon());

            Iterable<Relationship> relationships = newcm.getRelationships(RelTypes.HAS_ATTRIBUTE);

            int count = 0;
            List atrList = newClassDefinition.getAttributes();

            for (Relationship relationship : relationships) {
                Node newAttr = relationship.getEndNode();
                AttributeMetadata atr = (AttributeMetadata) atrList.get(count);
                newAttr.setProperty(PROPERTY_NAME, atr.getName());
                newAttr.setProperty(PROPERTY_DESCRIPTION, atr.getDescription());
                count++;
            }

            tx.success();

        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
        }
    }

    /**
     * Deletes a class metadata entry, its attributes and category relationships
     * @param classId
     * @throws ClassNotFoundException if there is not a class with de ClassId
     */
    @Override
    public void deleteClass(Long classId) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node node = classIndex.get(PROPERTY_ID, String.valueOf(classId)).getSingle();

            if (node == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class with the id %1s", classId));
            }

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                atr.delete();

                relationship.delete();
            }
            //Deleting other relationships
            relationships = node.getRelationships();
            for (Relationship relationship : relationships) {
                relationship.delete();
            }
            node.delete();
            classIndex.remove(node);
            tx.success();

        } catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        } finally {
            tx.finish();
        }
    }

    /**
     * Deletes a classmetadata, its attributes and category relationships
     * @param classId
     * @return true if success
     * @throws ClassNotFoundException if there is not a class with de ClassName
     */
    @Override
    public void deleteClass(String className) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node node = classIndex.get(PROPERTY_NAME, className).getSingle();

            if (node == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class with the name %1s", className));
            }

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                atr.delete();
                relationship.delete();
            }
            //Deleting other relationships
            relationships = node.getRelationships();
            for (Relationship relationship : relationships) {
                relationship.delete();
            }
            node.delete();
            tx.success();
        } catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        } finally {
            tx.finish();
        }
    }

    @Override
    public void setAttributePropertyValue(Long classId, String attributeName,
            String propertyName, String propertyValue) throws MetadataObjectNotFoundException
    {
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node classNode = classIndex.get(PROPERTY_ID, classId).getSingle();

            if (classNode == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class with the id %1s", classId));

            Iterable<Relationship> relationships = classNode.getRelationships(RelTypes.HAS_ATTRIBUTE);

            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                String dd = String.valueOf(atr.getProperty(PROPERTY_NAME));
                if (dd.equals(attributeName))
                {
                    if(propertyName.equalsIgnoreCase(PROPERTY_DISPLAY_NAME))
                    {
                        atr.setProperty(PROPERTY_DISPLAY_NAME, propertyValue);
                        break;
                    }

                    if(propertyName.equalsIgnoreCase(PROPERTY_VISIBLE))
                    {
                        atr.setProperty(PROPERTY_VISIBLE, Boolean.valueOf(propertyValue));
                        break;
                    }

                    if(propertyName.equalsIgnoreCase(PROPERTY_ADMINISTRATIVE))
                    {
                        atr.setProperty(PROPERTY_ADMINISTRATIVE, Boolean.valueOf(propertyValue));
                        break;
                    }

                    if(propertyName.equalsIgnoreCase(PROPERTY_DESCRIPTION))
                    {
                        atr.setProperty(PROPERTY_DESCRIPTION, propertyValue);
                        break;
                    }
                }//end if   
            }//end for
            tx.success();
            
        } catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        } finally {
            if( tx != null)
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
    public List<ClassMetadataLight> getLightMetadata(Boolean includeListTypes) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> cml = new ArrayList<ClassMetadataLight>();
        try {
            Node myClassNode =  classIndex.get(PROPERTY_NAME, CLASS_INVENTORYOBJECT).getSingle();

            if(myClassNode == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Class with the name %1s", CLASS_INVENTORYOBJECT));

            String cypherQuery = "START inventory = node:classes({className}) ".concat(
                                 "MATCH inventory <-[:").concat(RelTypes.EXTENDS.toString()).concat("*]-classmetadata ").concat(
                                 "RETURN classmetadata,inventory ").concat(
                                 "ORDER BY classmetadata.name ASC");

            Map<String, Object> params = new HashMap<String, Object>();
            if(includeListTypes)
                params.put("className", "name:"+CLASS_INVENTORYOBJECT+" name:"+CLASS_GENERICOBJECTLIST);//NOI18N
            else
                params.put("className", "name:"+CLASS_INVENTORYOBJECT);//NOI18N

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

    /**
     * Retrieves all the class metadata
     * @param includeListTypes boolean to indicate if the list should include
     * the subclasses of GenericObjectList
     * @return An array of classes
     */
    @Override
    public List<ClassMetadata> getMetadata(Boolean includeListTypes) throws MetadataObjectNotFoundException {
        List<ClassMetadata> cml = new ArrayList<ClassMetadata>();
        try {

            String cypherQuery = "START inventory = node:classes({className}) ".concat(
                                 "MATCH inventory <-[:").concat(RelTypes.EXTENDS.toString()).concat("*]-classmetadata ").concat(
                                 "RETURN classmetadata,inventory ").concat(
                                 "ORDER BY classmetadata.name ASC");

            Map<String, Object> params = new HashMap<String, Object>();
           if(includeListTypes)
                params.put("className", "name:"+CLASS_INVENTORYOBJECT+" name:" + CLASS_GENERICOBJECTLIST);//NOI18N
            else
                params.put("className", "name:"+ CLASS_INVENTORYOBJECT);//NOI18N

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
    public ClassMetadata getClass(Long classId) throws MetadataObjectNotFoundException {
        ClassMetadata clmt = new ClassMetadata();
        try {

            Node node = classIndex.get(PROPERTY_ID, classId).getSingle();

            if (node == null) 
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class with the id %1s", classId));

            clmt = Util.createClassMetadataFromNode(node);


        } catch(Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        return clmt;
    }

    /**
     * Gets a classmetadata, its attributes and Category
     * @param className
     * @return A ClassMetadata with the className
     * @throws MetadataObjectNotFoundException there is no a class with such name
     */
    @Override
    public ClassMetadata getClass(String className) throws MetadataObjectNotFoundException {
        ClassMetadata clmt = new ClassMetadata();

        try {
            Node node = classIndex.get(PROPERTY_NAME, className).getSingle();

            if (node == null) 
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the class with name %1s", className));

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
            Node ctm = classIndex.get(PROPERTY_NAME, classToMoveName).getSingle();
            Node tcn = classIndex.get(PROPERTY_NAME, targetParentClassName).getSingle();

            if (ctm == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class to move with the name %1s", classToMoveName));
            } else if (tcn == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find The target parent Class with the name %1s", targetParentClassName));
            } else {
                Relationship rel = ctm.getSingleRelationship(RelTypes.EXTENDS, Direction.OUTGOING);
                rel.delete();
                ctm.createRelationshipTo(tcn, RelTypes.EXTENDS);
            }
            tx.success();
        } catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        } finally {
            if( tx != null)
                tx.finish();
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
    public void moveClass(Long classToMoveId, Long targetParentClassId) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node ctm = classIndex.get(PROPERTY_ID, classToMoveId).getSingle();
            Node tcn = classIndex.get(PROPERTY_ID, targetParentClassId).getSingle();

            if (ctm == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class to move with the id %1s", classToMoveId));
            } else if (tcn == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find The targetn parent Class with the id %1s", targetParentClassId));
            } else {
                Relationship rel = ctm.getSingleRelationship(RelTypes.EXTENDS, Direction.OUTGOING);
                rel.delete();
                ctm.createRelationshipTo(tcn, RelTypes.EXTENDS);
            }
            tx.success();
        } catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
        }
    }

    /**
     * Set a class icon (big or small)
     * @param classId
     * @param attributeName
     * @param iconImage
     */
    @Override
    public void setClassIcon(Long classId, String attributeName, byte[] iconImage) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        try 
        {
            tx = graphDb.beginTx();
            Node ctm = classIndex.get(PROPERTY_ID, classId).getSingle();
            if (ctm == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class to move with the id %1s", classId));
            }

            if (attributeName.equalsIgnoreCase(PROPERTY_ICON)) {
                ctm.setProperty(PROPERTY_ICON, iconImage);
            } else if (attributeName.equalsIgnoreCase(PROPERTY_SMALL_ICON)) {
                ctm.setProperty(PROPERTY_SMALL_ICON, iconImage);
            }

            tx.success();
        } catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
        }
    }

    /**
     * Adds an attribute to the class
     * @param className
     * @param attributeDefinition
     * @throws MetadataObjectNotFoundException if there is no a class with such className
     */
    @Override
    public void addAttribute(String className, AttributeMetadata attributeDefinition) throws MetadataObjectNotFoundException {
        attributeDefinition = Util.createDefaultAttributeMetadata(attributeDefinition);

        Transaction tx = null;

        try {
            tx = graphDb.beginTx();

            Node node = classIndex.get(PROPERTY_NAME, className).getSingle();

            if (node == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class with the name %1s", className));
            }

            Node atr = graphDb.createNode();

            atr.setProperty(PROPERTY_NAME, attributeDefinition.getName());
            atr.setProperty(PROPERTY_DESCRIPTION, attributeDefinition.getDescription());
            atr.setProperty(PROPERTY_DISPLAY_NAME, attributeDefinition.getDisplayName());
            atr.setProperty(PROPERTY_TYPE, attributeDefinition.getType());
            atr.setProperty(PROPERTY_MAPPING, attributeDefinition.getMapping());
            atr.setProperty(PROPERTY_READONLY, attributeDefinition.isVisible());
            atr.setProperty(PROPERTY_VISIBLE, attributeDefinition.isVisible());
            atr.setProperty(PROPERTY_ADMINISTRATIVE, attributeDefinition.isAdministrative());
            atr.setProperty(PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            atr.setProperty(PROPERTY_NO_COPY, attributeDefinition.isNoCopy());
            atr.setProperty(PROPERTY_NO_SERIALIZE, attributeDefinition.isNoSerialize());
            atr.setProperty(PROPERTY_UNIQUE, attributeDefinition.isUnique());

            node.createRelationshipTo(atr, RelTypes.HAS_ATTRIBUTE);

            tx.success();
        } catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        } finally {
            tx.finish();
        }
    }

    /**
     * Adds an attribute to a class
     * @param classId
     * @param attributeDefinition
     * @throws MetadataObjectNotFoundException if there is no a class with such classId
     */
    @Override
    public void addAttribute(Long classId, AttributeMetadata attributeDefinition) throws MetadataObjectNotFoundException {
        attributeDefinition = Util.createDefaultAttributeMetadata(attributeDefinition);

        Transaction tx = null;

        try {
            tx = graphDb.beginTx();
            Node node = classIndex.get(PROPERTY_ID, classId).getSingle();

            if (node == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class with the id %1s", classId));
            }

            Node atr = graphDb.createNode();

            atr.setProperty(PROPERTY_NAME, attributeDefinition.getName());
            atr.setProperty(PROPERTY_DESCRIPTION, attributeDefinition.getDescription());
            atr.setProperty(PROPERTY_DISPLAY_NAME, attributeDefinition.getDisplayName());
            atr.setProperty(PROPERTY_TYPE, attributeDefinition.getType());
            atr.setProperty(PROPERTY_MAPPING, attributeDefinition.getMapping());
            atr.setProperty(PROPERTY_READONLY, attributeDefinition.isVisible());
            atr.setProperty(PROPERTY_VISIBLE, attributeDefinition.isVisible());
            atr.setProperty(PROPERTY_ADMINISTRATIVE, attributeDefinition.isAdministrative());
            atr.setProperty(PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            atr.setProperty(PROPERTY_NO_COPY, attributeDefinition.isNoCopy());
            atr.setProperty(PROPERTY_NO_SERIALIZE, attributeDefinition.isNoSerialize());
            atr.setProperty(PROPERTY_UNIQUE, attributeDefinition.isUnique());

            node.createRelationshipTo(atr, RelTypes.HAS_ATTRIBUTE);

            tx.success();

        } catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
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
            Node node = classIndex.get(PROPERTY_NAME, className).getSingle();

            if (node == null) 
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class with the name %1s", className));

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                if (String.valueOf(atr.getProperty(PROPERTY_NAME)).equals(attributeName)) {
                    attribute = new AttributeMetadata();
                    attribute = Util.createAttributeMetadataFromNode(atr);
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
     * @param attributeName
     * @return AttributeMetada, null if there is no attribute with such name
     * @throws MetadataObjectNotFoundException if there is no a class with such classId
     */
    @Override
    public AttributeMetadata getAttribute(Long classId, String attributeName) throws MetadataObjectNotFoundException {
        AttributeMetadata attribute = null;
        try {
            Node node = classIndex.get(PROPERTY_ID, classId).getSingle();

            if (node == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class with the id %1s", classId));
            }

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                if (String.valueOf(atr.getProperty(PROPERTY_NAME)).equals(attributeName)) {
                    attribute = new AttributeMetadata();
                    attribute = Util.createAttributeMetadataFromNode(atr);
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
    public void changeAttributeDefinition(Long classId, AttributeMetadata newAttributeDefinition) {
        Transaction tx = null;
        boolean couldDelAtt = false;
        try {
            tx = graphDb.beginTx();
            Node node = classIndex.get(PROPERTY_ID, classId).getSingle();

            if (node == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class with the id %1s", classId));
            }

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                if (String.valueOf(atr.getProperty(PROPERTY_NAME)).equals(newAttributeDefinition.getName()))
                {
                    atr.setProperty(PROPERTY_NAME, newAttributeDefinition.getName());
                    atr.setProperty(PROPERTY_DESCRIPTION, newAttributeDefinition.getDescription());
                    atr.setProperty(PROPERTY_DISPLAY_NAME, newAttributeDefinition.getDisplayName());
                    atr.setProperty(PROPERTY_TYPE, newAttributeDefinition.getType());
                    atr.setProperty(PROPERTY_MAPPING, newAttributeDefinition.getMapping());
                    atr.setProperty(PROPERTY_READONLY, newAttributeDefinition.isVisible());
                    atr.setProperty(PROPERTY_VISIBLE, newAttributeDefinition.isVisible());
                    atr.setProperty(PROPERTY_ADMINISTRATIVE, newAttributeDefinition.isAdministrative());
                    atr.setProperty(PROPERTY_NO_COPY, newAttributeDefinition.isNoCopy());
                    atr.setProperty(PROPERTY_NO_SERIALIZE, newAttributeDefinition.isNoSerialize());

                    couldDelAtt = true;
                }
            }//end for
            //if the attribute does exist
            if (!couldDelAtt) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Attribute with the name %1s", newAttributeDefinition.getName()));
            }

            tx.success();
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        } finally {
            tx.finish();
        }
    }

    @Override
    public void setClassPlainAttribute(Long classId, String attributeName,
        String attributeValue)throws MetadataObjectNotFoundException{
        Transaction tx = null;
        try
        {
            tx = graphDb.beginTx();
            Node classNode = classIndex.get(PROPERTY_ID, classId).getSingle();

            if (classNode == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class with the id %1s", classId));

            if(attributeName.equals(PROPERTY_DISPLAY_NAME))
                classNode.setProperty(PROPERTY_DISPLAY_NAME, attributeValue);

            else if(attributeName.equals(PROPERTY_DESCRIPTION))
                classNode.setProperty(PROPERTY_DESCRIPTION, attributeValue);

            tx.success();
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        } finally {
            tx.finish();
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
        try {
            tx = graphDb.beginTx();
            Node node = classIndex.get(PROPERTY_NAME, className).getSingle();

            if (node == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class with the name %1s", attributeName));
            }

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                if (String.valueOf(atr.getProperty(PROPERTY_NAME)).equals(attributeName)) {
                    atr.delete();
                    relationship.delete();
                    couldDelAtt = true;
                }
            }//end for
            //if the attribute does exist
            if (!couldDelAtt) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Attribute with the name %1s", attributeName));
            }

            tx.success();
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        } finally {
            tx.finish();
        }
    }

    /**
     * Deletes an attribute belonging to a classMetadata
     * @param classId
     * @param attributeName
     * @throws MetadataObjectNotFoundException if there is no a class with such classId
     */
    @Override //TODO ponerlo en el modelo
    public void deleteAttribute(Long classId, String attributeName) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        boolean couldDelAtt = false;

        try {
            tx = graphDb.beginTx();
            Node node = classIndex.get(PROPERTY_ID, classId).getSingle();

            if (node == null) 
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class with the id %1s", classId));

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS_ATTRIBUTE);
            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                //TODO poner exception no hay attributename, si un atributo no esta
                if (String.valueOf(atr.getProperty(PROPERTY_NAME)).equals(attributeName)) {
                    atr.delete();
                    relationship.delete();
                    couldDelAtt = true;
                }
            }//end for
            //if the attribute does exist
            if (!couldDelAtt) 
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Attibute with the name %1s", attributeName));

            tx.success();

        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        } finally {
            tx.finish();
        }
    }

    /**
     * Creates a new category
     * @param categoryDefinition
     * @return CategoryId
     */
    @Override
    public Long createCategory(CategoryMetadata categoryDefinition) {
        Long id = null;
        Transaction tx = null;
        
        try {
            tx = graphDb.beginTx();
            Node category = graphDb.createNode();
            category.setProperty(PROPERTY_NAME, categoryDefinition.getName());
            category.setProperty(PROPERTY_DISPLAY_NAME, categoryDefinition.getDisplayName());
            category.setProperty(PROPERTY_DESCRIPTION, categoryDefinition.getDescription());
            category.setProperty(PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());

            id = category.getId();

            categoryIndex.add(category, PROPERTY_ID, id);
            categoryIndex.add(category, PROPERTY_NAME, categoryDefinition.getName());

            tx.success();
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        } finally {
            tx.finish();
        }

        return id;
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
            Node ctgNode = categoryIndex.get(PROPERTY_NAME, categoryName).getSingle();

            if (ctgNode == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the category with the name %1s", categoryName));
            }

            ctgrMtdt.setName((String) ctgNode.getProperty(PROPERTY_NAME));
            ctgrMtdt.setDescription((String) ctgNode.getProperty(PROPERTY_DESCRIPTION));
            ctgrMtdt.setDisplayName((String) ctgNode.getProperty(PROPERTY_DISPLAY_NAME));

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
    public CategoryMetadata getCategory(Integer categoryId) throws MetadataObjectNotFoundException {

        CategoryMetadata ctgrMtdt = new CategoryMetadata();
        
        try {

            Node ctgNode = categoryIndex.get(PROPERTY_ID, categoryId).getSingle();

            if (ctgNode == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the category with the id %1s", categoryId));
            }

            ctgrMtdt.setName((String) ctgNode.getProperty(PROPERTY_NAME));
            ctgrMtdt.setDescription((String) ctgNode.getProperty(PROPERTY_DESCRIPTION));
            ctgrMtdt.setDisplayName((String) ctgNode.getProperty(PROPERTY_DISPLAY_NAME));

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
    public void changeCategoryDefinition(CategoryMetadata categoryDefinition) throws MetadataObjectNotFoundException {
        Transaction tx = null;
        try {
            tx = graphDb.beginTx();
            Node ctgr = categoryIndex.get(PROPERTY_NAME, categoryDefinition.getName()).getSingle();

            if (ctgr == null) {
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the category with the name %1s", categoryDefinition.getName()));
            }

            ctgr.setProperty(PROPERTY_NAME, categoryDefinition.getName());
            ctgr.setProperty(PROPERTY_DISPLAY_NAME, categoryDefinition.getDisplayName());
            ctgr.setProperty(PROPERTY_DESCRIPTION, categoryDefinition.getDescription());

            tx.success();

        }catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        finally {
            if(tx != null)
                tx.finish();
        }
    }

    @Override
    public void deleteCategory(String categoryName) {   //TODO what about the classes?
    }

    @Override
    public void deleteCategory(Integer categoryId) {
    }

    @Override
    public void addImplementor(String classWhichImplementsName, String interfaceToImplementName) {
    }

    @Override
    public void removeImplementor(String classWhichImplementsName, String interfaceToBeRemovedName) {
    }

    @Override
    public void addImplementor(Integer classWhichImplementsId, Integer interfaceToImplementId) {
    }

    @Override
    public void removeImplementor(Integer classWhichImplementsId, Integer interfaceToBeRemovedId) {
    }

    @Override
    public InterfaceMetadata getInterface(String interfaceName) {
        return null;
    }

    @Override
    public InterfaceMetadata getInterface(Integer interfaceid) {
        return null;
    }

    @Override
    public List<ClassMetadataLight> getPossibleChildren(String parentClassName) throws MetadataObjectNotFoundException {
        List<ClassMetadataLight> cml = new ArrayList<ClassMetadataLight>();
        try {
            String cypherQuery;
            if (parentClassName == null) //The Dummy Rooot
                cypherQuery = "START rootNode = node(0) ".concat(
                                 "MATCH rootNode -[:DUMMY_ROOT]->dummyRootNode-[:POSSIBLE_CHILD]->directChild<-[?:EXTENDS*]-subClass ").concat(
                                 "WHERE subClass.abstract=false OR subClass IS NULL ").concat(
                                 "RETURN directChild, subClass ").concat(
                                 "ORDER BY directChild.name,subClass.name ASC");
            else
                cypherQuery = "START parentClassNode = node:classes({className}) ".concat(
                                 "MATCH parentClassNode -[:POSSIBLE_CHILD]->directChild<-[?:EXTENDS*]-subClass ").concat(
                                 "WHERE subClass.abstract=false OR subClass IS NULL ").concat(
                                 "RETURN directChild, subClass ").concat(
                                 "ORDER BY directChild.name,subClass.name ASC");

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("className", "name:" + parentClassName);//NOI18N


            ExecutionEngine engine = new ExecutionEngine(graphDb);
            ExecutionResult result = engine.execute(cypherQuery, params);

            Iterator<Map<String,Object>> entries = result.iterator();
            while (entries.hasNext()){
                Map<String,Object> entry = entries.next();
                Node directChildNode =  (Node)entry.get("directChild");
                Node indirectChildNode =  (Node)entry.get("subClass");
                if (!(Boolean)directChildNode.getProperty(PROPERTY_ABSTRACT))
                    cml.add(Util.createClassMetadataFromNode(directChildNode));
                if (indirectChildNode != null)
                    cml.add(Util.createClassMetadataFromNode(indirectChildNode));
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
            if (parentClassName == null) //The Dummy Rooot
                cypherQuery = "START rootNode = node(0) ".concat(
                                 "MATCH rootNode -[:DUMMY_ROOT]->dummyRootNode-[:POSSIBLE_CHILD]->directChild ").concat(
                                 "RETURN directChild ").concat(
                                 "ORDER BY directChild.name ASC");
            else
                cypherQuery = "START parentClassNode = node:classes({className}) ".concat(
                                 "MATCH parentClassNode -[:POSSIBLE_CHILD]->directChild ").concat(
                                 "RETURN directChild ").concat(
                                 "ORDER BY directChild.name ASC");

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("className", "name:" + parentClassName);//NOI18N

            ExecutionEngine engine = new ExecutionEngine(graphDb);
            ExecutionResult result = engine.execute(cypherQuery, params);

            Iterator<Node> directPossibleChildren = result.columnAs("directChild");
            for (Node node : IteratorUtil.asIterable(directPossibleChildren))
                cml.add(Util.createClassMetadataFromNode(node));

        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
        return cml;
    }

    @Override
    public void addPossibleChildren(Long parentClassId, Long[] _possibleChildren)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        Transaction tx = null;
        Node parentNode;
        boolean isDummyRoot = false;

        if(parentClassId != null) {
            parentNode = classIndex.get(PROPERTY_ID, parentClassId).getSingle();

            if (parentNode == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the Class with the id %1s", parentClassId));
            if (!cm.isSubClass(CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(PROPERTY_NAME)))
                throw new InvalidArgumentException(
                        Util.formatString("%1s is not a business class, thus can not be added to the containment hierarchy", (String)parentNode.getProperty(PROPERTY_NAME)), Level.INFO);
        }else{
            Node referenceNode = graphDb.getReferenceNode();
            Relationship rel = referenceNode.getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.OUTGOING);
            parentNode = rel.getEndNode();

            if(!(DUMMYROOT).equals((String)parentNode.getProperty(PROPERTY_NAME)))
                    throw new MetadataObjectNotFoundException("DummyRoot node is corrupted");
            else
                isDummyRoot = true;
        }

        List<ClassMetadataLight> currentPossibleChildren = getPossibleChildren(isDummyRoot ? null : (String)parentNode.getProperty(PROPERTY_NAME));
        tx = graphDb.beginTx();

        try{
            for (Long id : _possibleChildren) {
                Node childNode = classIndex.get(PROPERTY_ID, id).getSingle();

                if (childNode == null)
                    throw new MetadataObjectNotFoundException(Util.formatString(
                            "Can not find class with id %1s", parentClassId));

                if (!cm.isSubClass(CLASS_INVENTORYOBJECT, (String)childNode.getProperty(PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            Util.formatString("%1s is not a business class, thus can not be added to the containment hierarchy", (String)childNode.getProperty(PROPERTY_NAME)), Level.INFO);

                if ((Boolean)childNode.getProperty(PROPERTY_ABSTRACT)){
                   for (Node subclassNode : Util.getAllSubclasses(childNode)){
                       for (ClassMetadataLight possibleChild : currentPossibleChildren)
                            if (possibleChild.getId().longValue() == subclassNode.getId())
                                throw new InvalidArgumentException(Util.formatString("A subclass of %1s is already a possible child for instances of %2s", (String)childNode.getProperty(PROPERTY_NAME), (String)parentNode.getProperty(PROPERTY_NAME)), Level.INFO);
                   }
                }
                else{
                    for (ClassMetadataLight possibleChild : currentPossibleChildren)
                        if (possibleChild.getId().longValue() == childNode.getId())
                            throw new InvalidArgumentException(Util.formatString("Class %1s is already a possible child for instances of %2s", (String)childNode.getProperty(PROPERTY_NAME), (String)parentNode.getProperty(PROPERTY_NAME)), Level.INFO);
                }

                parentNode.createRelationshipTo(childNode, RelTypes.POSSIBLE_CHILD);

                //Refresh cache
                if ((Boolean)childNode.getProperty(PROPERTY_ABSTRACT)){
                    for(Node subclass : Util.getAllSubclasses(childNode))
                        cm.putPossibleChild((String)parentNode.getProperty(PROPERTY_NAME),(String)subclass.getProperty(PROPERTY_NAME));
                }else
                    cm.putPossibleChild((String)parentNode.getProperty(PROPERTY_NAME), (String)childNode.getProperty(PROPERTY_NAME));

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
            tx.finish();
        }
    }

    public void addPossibleChildren(String parentClassName, String[] _possibleChildren) throws MetadataObjectNotFoundException, InvalidArgumentException {
        Transaction tx = null;
        Node parentNode;
        boolean isDummyRoot = false;

        if(parentClassName != null) {
            parentNode = classIndex.get(PROPERTY_NAME, parentClassName).getSingle();

            if (parentNode == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find class %1s", parentClassName));
            if (!cm.isSubClass(CLASS_INVENTORYOBJECT, (String)parentNode.getProperty(PROPERTY_NAME)))
                throw new InvalidArgumentException(
                        Util.formatString("%1s is not a business class, thus can not be added to the containment hierarchy", (String)parentNode.getProperty(PROPERTY_NAME)), Level.INFO);
        }else{
            Node referenceNode = graphDb.getReferenceNode();
            Relationship rel = referenceNode.getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.OUTGOING);
            parentNode = rel.getEndNode();

            if(!(DUMMYROOT).equals((String)parentNode.getProperty(PROPERTY_NAME)))
                    throw new MetadataObjectNotFoundException("DummyRoot node is corrupted");
            else
                isDummyRoot = true;
        }

        List<ClassMetadataLight> currentPossibleChildren = getPossibleChildren(isDummyRoot ? null : (String)parentNode.getProperty(PROPERTY_NAME));
        tx = graphDb.beginTx();

        try{
            for (String possibleChildName : _possibleChildren) {
                Node childNode = classIndex.get(PROPERTY_NAME, possibleChildName).getSingle();

                if (childNode == null)
                    throw new MetadataObjectNotFoundException(Util.formatString(
                            "Can not find class %1s", possibleChildName));

                if (!cm.isSubClass(CLASS_INVENTORYOBJECT, (String)childNode.getProperty(PROPERTY_NAME)))
                    throw new InvalidArgumentException(
                            Util.formatString("%1s is not a business class, thus can not be added to the containment hierarchy", (String)childNode.getProperty(PROPERTY_NAME)), Level.INFO);

                if ((Boolean)childNode.getProperty(PROPERTY_ABSTRACT)){
                   for (Node subclassNode : Util.getAllSubclasses(childNode)){
                       for (ClassMetadataLight possibleChild : currentPossibleChildren)
                            if (possibleChild.getId().longValue() == subclassNode.getId())
                                throw new InvalidArgumentException(Util.formatString("A subclass of %1s is already a possible child for instances of %2s", (String)childNode.getProperty(PROPERTY_NAME), (String)parentNode.getProperty(PROPERTY_NAME)), Level.INFO);
                   }
                }
                else{
                    for (ClassMetadataLight possibleChild : currentPossibleChildren)
                        if (possibleChild.getId().longValue() == childNode.getId())
                            throw new InvalidArgumentException(Util.formatString("Class %1s is already a possible child for instances of %2s", (String)childNode.getProperty(PROPERTY_NAME), (String)parentNode.getProperty(PROPERTY_NAME)), Level.INFO);
                }

                parentNode.createRelationshipTo(childNode, RelTypes.POSSIBLE_CHILD);

                //Refresh cache
                if ((Boolean)childNode.getProperty(PROPERTY_ABSTRACT)){
                    for(Node subclass : Util.getAllSubclasses(childNode))
                        cm.putPossibleChild(parentClassName,(String)subclass.getProperty(PROPERTY_NAME));
                }else
                    cm.putPossibleChild(parentClassName, (String)childNode.getProperty(PROPERTY_NAME));

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
            tx.finish();
        }
    }
    


    @Override
    public void removePossibleChildren(Long parentClassId, Long[] childrenToBeRemoved) throws MetadataObjectNotFoundException {
        Transaction tx = null;

        Node parentNode;

        if (parentClassId == null){
            parentNode = graphDb.getReferenceNode().getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.OUTGOING).getEndNode();
            if (parentNode == null)
                throw new MetadataObjectNotFoundException("DummyRoot is corrupted");
        }
        else{
            parentNode = classIndex.get(PROPERTY_ID, parentClassId).getSingle();
            if (parentNode == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find class with id %1s", parentClassId));
        }
        try
        {
            tx = graphDb.beginTx();
            for (Long id : childrenToBeRemoved)
            {
                Node childNode = classIndex.get(PROPERTY_ID, id).getSingle();
                Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.POSSIBLE_CHILD, Direction.OUTGOING);

                for (Relationship rel: relationships) {
                    Node possiblechild = rel.getEndNode();
                    if(childNode.getId() == possiblechild.getId())
                    {
                        rel.delete();
                        String parentClassName = (String)parentNode.getProperty(PROPERTY_NAME);
                        if (cm.getClass((String)childNode.getProperty(PROPERTY_NAME)).isAbstractClass()){
                            for(Node subClass : Util.getAllSubclasses(childNode))
                                cm.removePossibleChild(parentClassName, (String)subClass.getProperty(PROPERTY_NAME));
                        }
                        else
                            cm.removePossibleChild(parentClassName, (String)childNode.getProperty(PROPERTY_NAME));
                        
                        break;
                    }
                }//end for
            }//end for

            tx.success();

        }catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        } finally {
            if(tx != null)
                tx.finish();
        }
    }

    public boolean isSubClass(String allegedParent, String classToBeEvaluated) {
        try {
            return cm.isSubClass(allegedParent, classToBeEvaluated);
        } catch (MetadataObjectNotFoundException ex) {
            return false;
        }
    }
}

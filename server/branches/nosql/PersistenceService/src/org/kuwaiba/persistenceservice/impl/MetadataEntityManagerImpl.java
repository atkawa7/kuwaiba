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

import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.persistenceservice.impl.enumerations.RelTypes;
import org.kuwaiba.apis.persistence.interfaces.MetadataEntityManager;
import org.kuwaiba.psremoteinterfaces.MetadataEntityManagerRemote;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.persistenceservice.caching.CacheManager;
import org.kuwaiba.persistenceservice.util.Util;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;


/**
 * MetadataEntityManager implementation
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class MetadataEntityManagerImpl implements MetadataEntityManager, MetadataEntityManagerRemote{

    public static final String PROPERTY_PRIVILEGES = "privileges"; //NOI18N
    public static final String PROPERTY_NAME ="name"; //NOI18N
    public static final String PROPERTY_CREATION_DATE = "creationDate"; //NOI18N
    public static final String PROPERTY_DISPLAY_NAME ="displayName"; //NOI18N
    public static final String PROPERTY_TYPE ="type"; //NOI18N
    public static final String PROPERTY_ADMINISTRATIVE ="administrative"; //NOI18N
    public static final String PROPERTY_VISIBLE ="visible"; //NOI18N
    public static final String PROPERTY_DESCRIPTION ="description"; //NOI18N
    public static final String PROPERTY_READONLY = "readOnly"; //NOI18N
    public static final String PROPERTY_LOCKED = "locked"; //NOI18N
    public static final String PROPERTY_ID = "id"; //NOI18N
    public static final String PROPERTY_ABSTRACT = "abstract"; //NOI18N
    public static final String PROPERTY_CUSTOM = "custom"; //NOI18N
    public static final String PROPERTY_COUNTABLE = "countable"; //NOI18N
    public static final String PROPERTY_COLOR = "color"; //NOI18N
    public static final String PROPERTY_ICON = "icon"; //NOI18N
    public static final String PROPERTY_SMALL_ICON = "smallIcon"; //NOI18N
    public static final String PROPERTY_NO_DUMMY = "noDummy"; //NOI18N
    public static final String PROPERTY_VIEWABLE = "viewable"; //NOI18N

    /**
     * How an attribute should be mapped (as a Float, Integer, relationship, etc)
     */
    public static final String PROPERTY_MAPPING = "mapping"; //NOI18N

     /**
     * Label used for the class index
     */
    public static final String INDEX_CLASS = "classes";
    /**
     * Label used for the category index
     */
    public static final String INDEX_CATEGORY = "categories";
    /**
     * Label used for help index
     */
    public static final String INDEX_HELPER = "helperNodes";

    public static final String LIST_TYPE = "GenericObjectList";
    /**
     * Reference to the db handle
     */
    private EmbeddedGraphDatabase graphDb;
    /**
     * Class index
     */
    private static Index<Node> classIndex;
    /**
     * Category index
     */
    private static Index<Node> categoryIndex;
    /**
     * Helper index
     */
    private static Index<Node> helperIndex;
    /**
     * Reference to the CacheManager
     */
    private CacheManager cm;

    private MetadataEntityManagerImpl() {
        cm= CacheManager.getInstance();
    }

    /**
     * Constructor
     * Get the a database connection and indexes from the connection manager.
     */
    public MetadataEntityManagerImpl(ConnectionManager cmn) 
    {
        this();
        graphDb = (EmbeddedGraphDatabase)cmn.getConnectionHandler();
        classIndex = graphDb.index().forNodes(INDEX_CLASS);
        categoryIndex = graphDb.index().forNodes(INDEX_CATEGORY);
        helperIndex = graphDb.index().forNodes(INDEX_HELPER);
    }

    /**
     * Creates a classmetadata with its:
     * attributes(some new attributes and others extedended from the parent).
     * category (if the category does not exist it will be create).
     * @param classDefinition
     * @return the Id of the newClassMetadata
     * @throws ClassNotFoundException if there's no Parent Class whit the ParentId
     */

    @Override
    public Long createClass(ClassMetadata classDefinition) throws MetadataObjectNotFoundException
    {
        Transaction tx = graphDb.beginTx();
        Long id;

        classDefinition = Util.createDefaultClassMetadata(classDefinition);

        List<AttributeMetadata> ats = classDefinition.getAttributes();

        try{
            //The root must exist
            Node referenceNode = graphDb.getReferenceNode();
            Relationship rootRel = referenceNode.getSingleRelationship(
                    RelTypes.ROOT, Direction.BOTH);

            if (rootRel == null && classDefinition.getName().equals("RootObject")){
                Node rootNode = graphDb.createNode();

                rootNode.setProperty(PROPERTY_NAME, classDefinition.getName());
                rootNode.setProperty(PROPERTY_DISPLAY_NAME, classDefinition.getDisplayName());
                rootNode.setProperty(PROPERTY_CUSTOM, classDefinition.isCustom());
                rootNode.setProperty(PROPERTY_COUNTABLE, classDefinition.isCountable());
                rootNode.setProperty(PROPERTY_COLOR, classDefinition.getColor());
                rootNode.setProperty(PROPERTY_LOCKED, classDefinition.isLocked());
                rootNode.setProperty(PROPERTY_DESCRIPTION, classDefinition.getDescription());
                rootNode.setProperty(PROPERTY_ABSTRACT, classDefinition.isAbstractClass());
                rootNode.setProperty(PROPERTY_ICON, classDefinition.getIcon());
                rootNode.setProperty(PROPERTY_SMALL_ICON, classDefinition.getSmallIcon());
                rootNode.setProperty(PROPERTY_VIEWABLE, classDefinition.isViewable());
                rootNode.setProperty(PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());

                classIndex.putIfAbsent(rootNode, PROPERTY_NAME, classDefinition.getName());
                classIndex.putIfAbsent(rootNode, PROPERTY_ID, rootNode.getId());

                referenceNode.createRelationshipTo(rootNode, RelTypes.ROOT);

                id = rootNode.getId();

                //DummyNode?
                Node noDummyNode = graphDb.createNode();
                helperIndex.putIfAbsent(noDummyNode, PROPERTY_NAME, PROPERTY_NO_DUMMY);
                noDummyNode.createRelationshipTo(rootNode,RelTypes.IS_NOT_DUMMY);

            }//end if is rootNode
            else
            {
                //The ClassNode
                Node classNode = graphDb.createNode();

                classNode.setProperty(PROPERTY_NAME, classDefinition.getName());
                classNode.setProperty(PROPERTY_DISPLAY_NAME, classDefinition.getDisplayName());
                classNode.setProperty(PROPERTY_CUSTOM, classDefinition.isCustom());
                classNode.setProperty(PROPERTY_COUNTABLE, classDefinition.isCountable());
                classNode.setProperty(PROPERTY_COLOR, classDefinition.getColor());
                classNode.setProperty(PROPERTY_LOCKED, classDefinition.isLocked());
                classNode.setProperty(PROPERTY_DESCRIPTION, classDefinition.getDescription());
                classNode.setProperty(PROPERTY_ABSTRACT, classDefinition.isAbstractClass());
                classNode.setProperty(PROPERTY_ICON, classDefinition.getIcon());
                classNode.setProperty(PROPERTY_SMALL_ICON, classDefinition.getSmallIcon());
                classNode.setProperty(PROPERTY_VIEWABLE, classDefinition.isViewable());
                classNode.setProperty(PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
                
                //If the Class is not a dummy Class
                if(!classDefinition.isDummy())
                {
                    Node noDummyNode = helperIndex.get(PROPERTY_NAME, PROPERTY_NO_DUMMY).getSingle();
                    noDummyNode.createRelationshipTo(classNode,RelTypes.IS_NOT_DUMMY);
                }//end if is not a dummy Class

                id = classNode.getId();
                
                classIndex.putIfAbsent(classNode, PROPERTY_NAME,  classDefinition.getName());
                classIndex.putIfAbsent(classNode, PROPERTY_ID,  classNode.getId());

                //Category
                //if the category already exists
                if(classDefinition.getCategory() != null)
                {
                    Node ctgrNode = categoryIndex.get(PROPERTY_NAME, classDefinition.getCategory().getName()).getSingle();
                    if(ctgrNode == null)
                    {
                        Long ctgrId = createCategory(classDefinition.getCategory());
                        ctgrNode = categoryIndex.get(PROPERTY_ID, ctgrId).getSingle();
                    }
                    classNode.createRelationshipTo(ctgrNode, RelTypes.BELONGS_TO_GROUP);

                }//end if is category null

                Node parentNode = classIndex.get(PROPERTY_NAME, classDefinition.getParentClassName()).getSingle();

                if(parentNode != null)
                {
                    classNode.createRelationshipTo(parentNode, RelTypes.EXTENDS);
                    Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.HAS);
                    ////Set extendended attributes from parent
                    for (Relationship rel : relationships)
                    {
                        Node parentAttrNode = rel.getEndNode();
                        Node newAttrNode = graphDb.createNode();
                       
                        newAttrNode.setProperty(PROPERTY_NAME, parentAttrNode.getProperty(PROPERTY_NAME));
                        newAttrNode.setProperty(PROPERTY_DESCRIPTION, parentAttrNode.getProperty(PROPERTY_DESCRIPTION));
                        newAttrNode.setProperty(PROPERTY_DISPLAY_NAME, parentAttrNode.getProperty(PROPERTY_DISPLAY_NAME));
                        newAttrNode.setProperty(PROPERTY_TYPE,parentAttrNode.getProperty(PROPERTY_TYPE));
                        newAttrNode.setProperty(PROPERTY_READONLY, parentAttrNode.getProperty(PROPERTY_READONLY));
                        newAttrNode.setProperty(PROPERTY_VISIBLE, parentAttrNode.getProperty(PROPERTY_VISIBLE));
                        newAttrNode.setProperty(PROPERTY_ADMINISTRATIVE, parentAttrNode.getProperty(PROPERTY_ADMINISTRATIVE));

                        classNode.createRelationshipTo(newAttrNode, RelTypes.HAS);
                    }
                }//end if there is a Parent
                else
                    throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the parent Class with the name %1s", classDefinition.getParentClassName()));

            }//end else not rootNode

            //Attributes
            if(classDefinition.getAttributes() != null){
                for (AttributeMetadata at : classDefinition.getAttributes()) {
                    AttributeMetadata newAttr = getAttribute(id, at.getName());
                    if (newAttr == null)
                        addAttribute(id, at);
                }
            }

            tx.success();

            return id;

        }finally{
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
    public boolean changeClassDefinition(ClassMetadata newClassDefinition)throws MetadataObjectNotFoundException
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node newcm = classIndex.get(PROPERTY_NAME, newClassDefinition.getName()).getSingle();
            if(newcm == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Class with the id %1s", newClassDefinition.getName()));

            newcm.setProperty(PROPERTY_NAME, newClassDefinition.getName());
            newcm.setProperty(PROPERTY_DISPLAY_NAME, newClassDefinition.getDisplayName());
            newcm.setProperty(PROPERTY_CUSTOM, newClassDefinition.isCustom());
            newcm.setProperty(PROPERTY_COUNTABLE, newClassDefinition.isCountable());
            newcm.setProperty(PROPERTY_COLOR, newClassDefinition.getColor());
            newcm.setProperty(PROPERTY_LOCKED, newClassDefinition.isLocked());
            newcm.setProperty(PROPERTY_DESCRIPTION, newClassDefinition.getDescription());
            newcm.setProperty(PROPERTY_ABSTRACT, newClassDefinition.isAbstractClass());
            newcm.setProperty(PROPERTY_ICON, newClassDefinition.getIcon());
            newcm.setProperty(PROPERTY_SMALL_ICON, newClassDefinition.getSmallIcon());
            newcm.setProperty(PROPERTY_VIEWABLE, newClassDefinition.isViewable());

            Iterable<Relationship> relationships = newcm.getRelationships(RelTypes.HAS);

            int count=0;
            List atrList = newClassDefinition.getAttributes();

            for (Relationship relationship : relationships) {
                Node newAttr = relationship.getEndNode();
                AttributeMetadata atr = (AttributeMetadata)atrList.get(count);
                newAttr.setProperty(PROPERTY_NAME, atr.getName());
                newAttr.setProperty(PROPERTY_DESCRIPTION, atr.getDescription());
                count++;
            }

            tx.success();
            return true;

        }finally{
            tx.finish();
        }
    }

    /**
     * Deletes a classmetadata, its attributes and category relationships
     * @param classId
     * @return true if success
     * @throws ClassNotFoundException if there is not a class with de ClassId
     */
    @Override
    public boolean deleteClass(Long classId)throws MetadataObjectNotFoundException
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(PROPERTY_ID, String.valueOf(classId)).getSingle();

            if(node == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Class with the id %1s", classId));

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS);
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

            return true;
        }
        finally{
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
    public boolean deleteClass(String className)throws MetadataObjectNotFoundException
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(PROPERTY_NAME, className).getSingle();

            if(node == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Class with the name %1s", className));

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS);
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

            return true;
        }
        finally{
            tx.finish();
        }
    }

    /**
     * Retrieves the simplified list of classes. This list won't include either
     * those classes marked as dummy
     * @param includeListTypes boolean to indicate if the list should include
     * the subclasses of GenericObjectList
     * @return the list of classes
     * @throws Exception EntityManagerNotAvailableException or something unexpected
     */
    public List<ClassMetadataLight> getLightMetadata(Boolean includeListTypes) throws Exception
    {
        List<ClassMetadataLight> cml = new ArrayList<ClassMetadataLight>();
        Transaction tx = graphDb.beginTx();
        try{
            Node isNotDummyNode = helperIndex.get(PROPERTY_NAME, PROPERTY_NO_DUMMY).getSingle();
            Iterable<Relationship> relationships = isNotDummyNode.getRelationships();
            for (Relationship rel : relationships) {
                Node classNode = rel.getEndNode();
                if(includeListTypes)
                    cml.add(Util.createMetadataLightFromNode(classNode));
                else{
                    Relationship parentRel = classNode.getSingleRelationship(RelTypes.EXTENDS, Direction.OUTGOING);
                    Node parentNode = parentRel.getEndNode();

                    if(!Util.isSubClass(LIST_TYPE, parentNode))
                        cml.add(Util.createMetadataLightFromNode(classNode));
                    
                }
            }//end for
            
            tx.success();
        }
        finally{
            tx.finish();
        }

        return cml;
    }

    /**
     * Retrieves all the class metadata except for classes marked as dummy
     * @param includeListTypes boolean to indicate if the list should include
     * the subclasses of GenericObjectList
     * @return An array of classes
     */
    public List<ClassMetadata> getMetadata(Boolean includeListTypes) throws Exception
    {
        List<ClassMetadata> cml = new ArrayList<ClassMetadata>();
        Transaction tx = graphDb.beginTx();
        try{
            Node isNotDummyNode = helperIndex.get(PROPERTY_NAME, PROPERTY_NO_DUMMY).getSingle();
            Iterable<Relationship> relationships = isNotDummyNode.getRelationships();
            for (Relationship rel : relationships) {
                Node classNode = rel.getEndNode();
                if(includeListTypes)
                    cml.add(Util.createMetadataFromNode(classNode));
                else{
                    Relationship parentRel = classNode.getSingleRelationship(RelTypes.EXTENDS, Direction.OUTGOING);
                    Node parentNode = parentRel.getEndNode();
                    
                    if(!Util.isSubClass(LIST_TYPE, parentNode))
                        cml.add(Util.createMetadataFromNode(classNode));
                    
                }
            }//end for

            tx.success();
        }
        finally{
            tx.finish();
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
    public ClassMetadata getClass(Long classId)throws MetadataObjectNotFoundException
    {
        ClassMetadata clmt = new ClassMetadata();

        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(PROPERTY_ID, classId).getSingle();

            if(node == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Class with the id %1s", classId));

            clmt = Util.createMetadataFromNode(node);

            tx.success();
        }
        finally{
            tx.finish();
        }
        return clmt;
    }

    /**
     * Gets a classmetadata, its attributes and Category
     * @param className
     * @return A ClassMetadata with the className
     * @throws ClassNotFoundException there is no class with such className
     */
    @Override
    public ClassMetadata getClass(String className)throws MetadataObjectNotFoundException
    {
        ClassMetadata clmt = new ClassMetadata();
        
        Transaction tx = graphDb.beginTx();
        
        try{
            Node node = classIndex.get(PROPERTY_NAME,className).getSingle();

            if(node == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Class with the name %1s", className));

            clmt = Util.createMetadataFromNode(node);
            
            tx.success();

        }
        finally{
            tx.finish();
        }
        return clmt;
    }

    /**
     * Moves a class from one parentClass to an other parentClass
     * @param classToMoveName
     * @param targetParentClassName
     * @return true if success
     * @throws ClassNotFoundException if there is no a classToMove with such name
     * or if there is no a targetParentClass with such name
     */
    @Override
    public boolean moveClass(String classToMoveName, String targetParentClassName)throws MetadataObjectNotFoundException
    {
        Transaction tx = graphDb.beginTx();
        try{
                Node ctm = classIndex.get(PROPERTY_NAME, classToMoveName).getSingle();
                Node tcn = classIndex.get(PROPERTY_NAME, targetParentClassName).getSingle();

           if(ctm == null)
               throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Class to move with the name %1s", classToMoveName));
           else if(tcn == null)
               throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find The target parent Class with the name %1s", targetParentClassName));
           else{
                    Relationship rel = ctm.getSingleRelationship(RelTypes.EXTENDS, Direction.OUTGOING);
                    rel.delete();
                    ctm.createRelationshipTo(tcn, RelTypes.EXTENDS);
           }
            tx.success();
            return true;
        }
        finally{
            tx.finish();
        }
    }

    /**
     * Moves a class from one parentClass to an other parentClass
     * @param classToMoveId
     * @param targetParentClassId
     * @return true if success
     * @throws ClassNotFoundException if there is no a classToMove with such classId
     * or if there is no a targetParentClass with such classId
     */
    @Override
    public boolean moveClass(Long classToMoveId, Long targetParentClassId)throws MetadataObjectNotFoundException
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node ctm = classIndex.get(PROPERTY_ID, classToMoveId).getSingle();
            Node tcn = classIndex.get(PROPERTY_ID, targetParentClassId).getSingle();

            if(ctm == null)
               throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Class to move with the id %1s", classToMoveId));
               
            else if(tcn == null)
               throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find The targetn parent Class with the id %1s", targetParentClassId));
            else{
                    Relationship rel = ctm.getSingleRelationship(RelTypes.EXTENDS, Direction.OUTGOING);
                    rel.delete();
                    ctm.createRelationshipTo(tcn, RelTypes.EXTENDS);
            }
            tx.success();
            return true;
        }
        finally{
            tx.finish();
        }
    }


    /**
     * Set a class icon (big or small)
     * @param classId
     * @param attributeName
     * @param iconImage
     * @return
     */
    @Override
    public Boolean setClassIcon(Long classId, String attributeName, byte[] iconImage) throws Exception
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node ctm = classIndex.get(PROPERTY_ID, classId).getSingle();
            if(attributeName.equalsIgnoreCase("icon"))
                        ctm.setProperty(PROPERTY_ICON, iconImage);

            else if(attributeName.equalsIgnoreCase("smallicon"))
                        ctm.setProperty(PROPERTY_SMALL_ICON, iconImage);

            tx.success();
            return true;
        }
        finally{
            tx.finish();
        }
    }

    /**
     * Adds an attribute to the class
     * @param className
     * @param attributeDefinition
     * @return true if success
     * @throws ClassNotFoundException if there is no a class with such className
     */
    @Override
    public boolean addAttribute(String className, AttributeMetadata attributeDefinition)throws MetadataObjectNotFoundException
    {
        attributeDefinition = Util.createDefaultAttributeMetadata(attributeDefinition);

        Transaction tx = graphDb.beginTx();

        try{
            Node node = classIndex.get(PROPERTY_NAME,className).getSingle();

            if(node == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Class with the name %1s", className));

            Node atr = graphDb.createNode();

            atr.setProperty(PROPERTY_NAME, attributeDefinition.getName());
            atr.setProperty(PROPERTY_DESCRIPTION, attributeDefinition.getDescription());
            atr.setProperty(PROPERTY_DISPLAY_NAME, attributeDefinition.getDisplayName());
            atr.setProperty(PROPERTY_TYPE, attributeDefinition.getType());
            atr.setProperty(PROPERTY_MAPPING,attributeDefinition.getMapping());
            atr.setProperty(PROPERTY_READONLY, attributeDefinition.isVisible());
            atr.setProperty(PROPERTY_VISIBLE, attributeDefinition.isVisible());
            atr.setProperty(PROPERTY_ADMINISTRATIVE, attributeDefinition.isAdministrative());

            node.createRelationshipTo(atr, RelTypes.HAS);

            tx.success();

            return true;
        }
        finally{
            tx.finish();
        }
    }

    /**
     * Adds an attribute to a class
     * @param classId
     * @param attributeDefinition
     * @return true if success
     * @throws ClassNotFoundException if there is no a class with such classId
     */
    @Override //TODO agregarlo al modelo!
    public boolean addAttribute(Long classId, AttributeMetadata attributeDefinition )throws MetadataObjectNotFoundException
    {
        attributeDefinition = Util.createDefaultAttributeMetadata(attributeDefinition);

        Transaction tx = graphDb.beginTx();

        try{
            Node node = classIndex.get(PROPERTY_ID, classId).getSingle();

            if (node == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Class with the id %1s", classId));

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

            node.createRelationshipTo(atr, RelTypes.HAS);

            tx.success();

            return true;
        }
        finally{
            tx.finish();
        }
    }

    /**
     * Gets an attribute belonging to a class
     * @param className
     * @param attributeName
     * @return AttributeMetada, null if there is no attribute with such name
     * @throws ClassNotFoundException if there is no a class with such className
     * @throws MiscException if the attributeName does not exist
     */
    @Override
    public AttributeMetadata getAttribute(String className, String attributeName)throws MetadataObjectNotFoundException
    {
        AttributeMetadata attribute = null;
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(PROPERTY_NAME,className).getSingle();

            if(node == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Class with the name %1s", className));

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS);
            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                if (String.valueOf(atr.getProperty(PROPERTY_NAME)).equals(attributeName))
                {
                    attribute = new AttributeMetadata();
                    attribute = Util.createAttributeMetadataFromNode(node);
                }
            }

            tx.success();
        }
        finally{
            tx.finish();
        }
        return attribute;
    }

    /**
     * Gets an attribute belonging to a class
     * @param classId
     * @param attributeName
     * @return AttributeMetada, null if there is no attribute with such name
     * @throws ClassNotFoundException if there is no a class with such classId
     * @throws MiscException if the attributeName does not exist
     */
    @Override
    public AttributeMetadata getAttribute(Long classId, String attributeName)throws MetadataObjectNotFoundException
    {
        AttributeMetadata attribute = null;
        Transaction tx = graphDb.beginTx();
        try{
            //TODO poner exception no hay classId
            Node node = classIndex.get(PROPERTY_ID, classId).getSingle();

            if(node == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Class with the id %1s", classId));

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS);
            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                if (String.valueOf(atr.getProperty(PROPERTY_NAME)).equals(attributeName))
                {
                    attribute = new AttributeMetadata();
                    attribute = Util.createAttributeMetadataFromNode(node);
                }
            }

            tx.success();

        }finally{
            tx.finish();
        }
        return attribute;
    }

    /**
     * Changes an attribute definition belonging to a classMetadata
     * @param ClassId
     * @param newAttributeDefinition
     * @return
     */
    @Override //TODO mabe is not necesary
    public boolean changeAttributeDefinition(Long ClassId, AttributeMetadata newAttributeDefinition)
    {
        return true;
    }

    /**
     * Deletes an attribute belonging to a classMetadata
     * @param className
     * @param attributeName
     * @return true if success
     * @throws ClassNotFoundException if there is no a class with such className
     * @throws MiscException if the attributeName does not exist
     */
    @Override
    public boolean deleteAttribute(String className, String attributeName)throws MetadataObjectNotFoundException
    {
        Transaction tx = graphDb.beginTx();
        boolean couldDelAtt = false;
        try{
            //TODO poner exception no hay classId
            Node node = classIndex.get(PROPERTY_NAME, className).getSingle();

            if (node == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Class with the name %1s", attributeName));

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS);
            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                if (String.valueOf(atr.getProperty(PROPERTY_NAME)).equals(attributeName)){
                    atr.delete();
                    relationship.delete();
                    couldDelAtt =  true;
                }
            }//end for
            //if the attribute does exist
            if (!couldDelAtt)
                throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Attribute with the name %1s", attributeName));

            tx.success();
            return true;
        }
        finally{
            tx.finish();
        }
    }

    /**
     * Deletes an attribute belonging to a classMetadata
     * @param classId
     * @param attributeName
     * @return true if success
     * @throws ClassNotFoundException if there is no a class with such classId
     * @throws MiscException if the attributeName does not exist
     */
    @Override //TODO ponerlo en el modelo
    public boolean deleteAttribute(Long classId,String attributeName) throws MetadataObjectNotFoundException
    {
        Transaction tx = graphDb.beginTx();
        boolean couldDelAtt = false;
        try{
            //TODO poner exception no hay classId
            Node node = classIndex.get(PROPERTY_ID, classId).getSingle();

            if(node == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Class with the id %1s", classId));

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS);
            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                //TODO poner exception no hay attributename, si un atributo no esta
                if (String.valueOf(atr.getProperty(PROPERTY_NAME)).equals(attributeName)){
                    atr.delete();
                    relationship.delete();
                    couldDelAtt =  true;
                }
            }//end for
            //if the attribute does exist
            if (!couldDelAtt)
                throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the Attibute with the name %1s", attributeName));

            tx.success();
            return true;
        }
        finally{
            tx.finish();
        }
    }

    /**
     * Creates a new category
     * @param categoryDefinition
     * @return CategoryId
     */
    @Override
    public Long createCategory(CategoryMetadata categoryDefinition)
    {
        Transaction tx = graphDb.beginTx();
        Long id = null;
        try{
            Node category = graphDb.createNode();
            category.setProperty(PROPERTY_NAME, categoryDefinition.getName());
            category.setProperty(PROPERTY_DISPLAY_NAME, categoryDefinition.getDisplayName());
            category.setProperty(PROPERTY_DESCRIPTION, categoryDefinition.getDescription());
            category.setProperty(PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());

            id = category.getId();

            categoryIndex.add(category, PROPERTY_ID,id);
            categoryIndex.add(category, PROPERTY_NAME,categoryDefinition.getName());

            tx.success();

        }
        finally{
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
    public CategoryMetadata getCategory(String categoryName) throws MetadataObjectNotFoundException
    {
        CategoryMetadata ctgrMtdt = new CategoryMetadata();
        Transaction tx = graphDb.beginTx();
        try{
            Node ctgNode = categoryIndex.get(PROPERTY_NAME, categoryName).getSingle();

            if(ctgNode == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the category with the name %1s", categoryName));

            ctgrMtdt.setName((String)ctgNode.getProperty(PROPERTY_NAME));
            ctgrMtdt.setDescription((String)ctgNode.getProperty(PROPERTY_DESCRIPTION));
            ctgrMtdt.setDisplayName((String)ctgNode.getProperty(PROPERTY_DISPLAY_NAME));
            tx.success();

        }finally{
            tx.finish();
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
        Transaction tx = graphDb.beginTx();
        try{
            Node ctgNode = categoryIndex.get(PROPERTY_ID, categoryId).getSingle();

             if(ctgNode == null)
                 throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not find the category with the id %1s", categoryId));
                
            ctgrMtdt.setName((String)ctgNode.getProperty(PROPERTY_NAME));
            ctgrMtdt.setDescription((String)ctgNode.getProperty(PROPERTY_DESCRIPTION));
            ctgrMtdt.setDisplayName((String)ctgNode.getProperty(PROPERTY_DISPLAY_NAME));

            tx.success();

        }finally{
            tx.finish();
        }
        return ctgrMtdt;
    }

    /**
     * Changes a category definition
     * @param categoryDefinition
     * @return true if success
     * @throws MiscException if there is no Category with such cetegoryId
     */
    @Override
    public boolean changeCategoryDefinition(CategoryMetadata categoryDefinition) throws MetadataObjectNotFoundException{
        Transaction tx = graphDb.beginTx();
        try{
            Node ctgr = categoryIndex.get(PROPERTY_NAME, categoryDefinition.getName()).getSingle();

             if(ctgr == null)
                throw new MetadataObjectNotFoundException(Util.formatString(
                        "Can not find the category with the name %1s", categoryDefinition.getName()));

            ctgr.setProperty(PROPERTY_NAME, categoryDefinition.getName());
            ctgr.setProperty(PROPERTY_DISPLAY_NAME, categoryDefinition.getDisplayName());
            ctgr.setProperty(PROPERTY_DESCRIPTION, categoryDefinition.getDescription());

            tx.success();

        }finally{
            tx.finish();
        }
        return true;
    }

    @Override
    public boolean deleteCategory(String categoryName)
    {   //TODO what about the classes?
        return true;
    }

    @Override
    public boolean deleteCategory(Integer categoryId)
    {
        return true;
    }

    @Override
    public boolean addImplementor(String classWhichImplementsName,String interfaceToImplementName)
    {
        return true;
    }

    @Override
    public boolean removeImplementor(String classWhichImplementsName ,String interfaceToBeRemovedName)
    {
        return true;
    }

    @Override
    public boolean addImplementor(Integer classWhichImplementsId, Integer interfaceToImplementId)
    {
        return true;
    }

    @Override
    public boolean removeImplementor(Integer classWhichImplementsId ,Integer interfaceToBeRemovedId)
    {
        return true;
    }

    @Override
    public boolean getInterface(String interfaceName)
    {
        return true;
    }

    @Override
    public boolean getInterface(Integer interfaceid)
    {
        return true;
    }

}

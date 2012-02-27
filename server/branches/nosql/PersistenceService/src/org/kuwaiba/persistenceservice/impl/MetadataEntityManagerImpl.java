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

import org.kuwaiba.persistenceservice.impl.enumerations.RelTypes;
import org.kuwaiba.apis.persistence.interfaces.MetadataEntityManager;
import org.kuwaiba.psremoteinterfaces.MetadataEntityManagerRemote;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.persistence.AttributeMetadata;
import org.kuwaiba.apis.persistence.CategoryMetadata;
import org.kuwaiba.apis.persistence.ClassMetadata;
import org.kuwaiba.apis.persistence.exceptions.MiscException;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
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

    public static final String PROPERTY_NAME ="name"; //NOI18N
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
    public static final String PROPERTY_DUMMY = "dummy"; //NOI18N
    public static final String PROPERTY_COLOR = "color"; //NOI18N
    public static final String PROPERTY_ICON = "icon"; //NOI18N
    public static final String PROPERTY_SMALL_ICON = "smallIcon"; //NOI18N
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
     * Constructor
     * Get the a database connection and indexes from the connection manager.
     */
    public MetadataEntityManagerImpl(ConnectionManager cmn) {
        graphDb = (EmbeddedGraphDatabase)cmn.getConnectionHandler();
    }

    /**
     * Creates a classmetadata with their:
     * attributes(some new attributes and others extedended from the parent).
     * category (if the category does not exist it will be create).
     * @param classDefinition
     * @return the Id of the newClassMetadata
     * @throws ClassNotFoundException if there's no Parent Class whit the ParentId
     */
     
    @Override
    public Long createClass(ClassMetadata classDefinition) throws Exception
    {
        Transaction tx = graphDb.beginTx();
        Long id;
        List<AttributeMetadata> ats = classDefinition.getAttributes();
        try{
            //The root must exist
            Node referenceNode = graphDb.getReferenceNode();
            Relationship rootRel = referenceNode.getSingleRelationship(
                    RelTypes.ROOT, Direction.BOTH);

            if (rootRel == null){
                Node rootNode = graphDb.createNode();
                rootNode.setProperty(PROPERTY_NAME, "root");
                rootNode.setProperty(PROPERTY_LOCKED, "true");

                classIndex.add(rootNode, PROPERTY_NAME, "root");
                classIndex.add(rootNode, PROPERTY_ID,  String.valueOf(rootNode.getId()));

                referenceNode.createRelationshipTo(rootNode, RelTypes.ROOT);
            }

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
            classNode.setProperty(PROPERTY_DUMMY, classDefinition.isDummy());
            classNode.setProperty(PROPERTY_ICON, classDefinition.getIcon());
            classNode.setProperty(PROPERTY_SMALL_ICON, classDefinition.getSmallIcon());

            id = classNode.getId();
            classIndex.add(classNode, PROPERTY_NAME,  classDefinition.getName());
            classIndex.add(classNode, PROPERTY_ID,  String.valueOf(classNode.getId()));

            //Category
            //if the category already exists
            Node ctgrNode = categoryIndex.get(PROPERTY_NAME, classDefinition.getCategory().getName()).getSingle();
            if(ctgrNode == null){
                Long ctgrId = createCategory(classDefinition.getCategory());
                ctgrNode = categoryIndex.get(PROPERTY_ID, ctgrId).getSingle();
            }
            classNode.createRelationshipTo(ctgrNode, RelTypes.BELONGS_TO);

            Node parentNode = classIndex.get(PROPERTY_NAME, String.valueOf(classDefinition.getParentName())).getSingle();

            if(parentNode == null)
                    throw new ClassNotFoundException("The Parent Node with id" +
                            classDefinition.getParentName()
                            + "does not exist");
            
            //Set extendended attributes from parent
            else
            {
                classNode.createRelationshipTo(parentNode, RelTypes.EXTENDS);
                Iterable<Relationship> relationships = parentNode.getRelationships(RelTypes.HAS);
                for (Relationship rel : relationships) {
                    Node parentAttrNode = rel.getEndNode();
                    classNode.createRelationshipTo(parentAttrNode, RelTypes.HAS);
                }
            }

            //Attributes
            for (AttributeMetadata at : classDefinition.getAttributes()) {
                AttributeMetadata newAttr = getAttribute(id, at.getName());
                if (newAttr == null)
                    addAttribute(id, at);
            }

            tx.success();

            return id;
        
        }finally{
            tx.finish();
        }
    }

    /**
     * Changes the definiton of a classmetadata
     * @param newClassDefinition
     * @return true if success
     * @throws ClassNotFoundException if there is no class with such classId
     */
    @Override
    public boolean changeClassDefinition(ClassMetadata newClassDefinition)throws Exception
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node newcm = classIndex.get(PROPERTY_NAME, newClassDefinition.getName()).getSingle();
            if(newcm == null)
                throw new ClassNotFoundException("Can not find the class with the name "
                                                + newClassDefinition.getName() +
                                                " the class definition could not be changed");

            newcm.setProperty(PROPERTY_NAME, newClassDefinition.getName());
            newcm.setProperty(PROPERTY_DISPLAY_NAME, newClassDefinition.getDisplayName());
            newcm.setProperty(PROPERTY_CUSTOM, newClassDefinition.isCustom());
            newcm.setProperty(PROPERTY_COUNTABLE, newClassDefinition.isCountable());
            newcm.setProperty(PROPERTY_COLOR, newClassDefinition.getColor());
            newcm.setProperty(PROPERTY_LOCKED, newClassDefinition.isLocked());
            newcm.setProperty(PROPERTY_DESCRIPTION, newClassDefinition.getDescription());
            newcm.setProperty(PROPERTY_ABSTRACT, newClassDefinition.isAbstractClass());
            newcm.setProperty(PROPERTY_DUMMY, newClassDefinition.isDummy());
            newcm.setProperty(PROPERTY_ICON, newClassDefinition.getIcon());
            newcm.setProperty(PROPERTY_SMALL_ICON, newClassDefinition.getSmallIcon());

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
     * Deletes a classmetadata, their attributes and category relationships
     * @param classId
     * @return true if success
     * @throws ClassNotFoundException if there is not a class with de ClassId
     */
     
    @Override
    public boolean deleteClass(Long classId)throws Exception
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(PROPERTY_ID, String.valueOf(classId)).getSingle();

            if(node == null)
                throw new ClassNotFoundException("The ClassId: " + classId +
                                    ", you are trying to remove does not exist");
            
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
     * Deletes a classmetadata, their attributes and category relationships
     * @param classId
     * @return true if success
     * @throws ClassNotFoundException if there is not a class with de ClassName
     */
    @Override
    public boolean deleteClass(String className)throws Exception
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(PROPERTY_NAME, className).getSingle();

            if(node == null)
                throw new ClassNotFoundException("The ClassName: " + className +
                                    ", you are tryin to remove does not exist");

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
     * Gets a classmetadata, their attributes and Category
     * @param classId
     * @return A ClassMetadata with the classId
     * @throws ClassNotFoundException there is no class with such classId
     */
    @Override
    public ClassMetadata getClass(Long classId)throws Exception
    {
        ClassMetadata cm = new ClassMetadata();
        List<AttributeMetadata> listAttributes = new ArrayList();
        CategoryMetadata ctgr = new CategoryMetadata();
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(PROPERTY_ID, String.valueOf(classId)).getSingle();

            if(node == null)
                throw new ClassNotFoundException("The Class Id: " + classId +
                                        ", you are looking for does not exist");
            
            cm.setName((String)node.getProperty(PROPERTY_NAME));
            cm.setDisplayName((String)node.getProperty(PROPERTY_DISPLAY_NAME));
            cm.setCountable((Boolean)node.getProperty(PROPERTY_COUNTABLE));
            cm.setColor((Integer)node.getProperty(PROPERTY_COLOR));
            cm.setLocked((Boolean)node.getProperty(PROPERTY_LOCKED));
            cm.setDescription(PROPERTY_DESCRIPTION);
            cm.setAbstractClass((Boolean)node.getProperty(PROPERTY_ABSTRACT));
            cm.setDummy((Boolean)node.getProperty(PROPERTY_DUMMY));
            cm.setInterfaces(null);
            cm.setIcon((Byte)node.getProperty(PROPERTY_ICON));
            cm.setSmallIcon((Byte)node.getProperty(PROPERTY_SMALL_ICON));

            //Attributes
            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS);

            for (Relationship relationship : relationships) {

                Node attrNode =relationship.getEndNode();

                AttributeMetadata attr = new AttributeMetadata();

                attr.setName((String)attrNode.getProperty(PROPERTY_NAME));
                attr.setDescription((String)attrNode.getProperty(PROPERTY_DESCRIPTION));
                attr.setDisplayName((String)attrNode.getProperty(PROPERTY_DISPLAY_NAME));
                attr.setReadOnly((Boolean)attrNode.getProperty(PROPERTY_READONLY));
                attr.setType((String)attrNode.getProperty(PROPERTY_TYPE));
                attr.setVisible((Boolean)attrNode.getProperty(PROPERTY_VISIBLE));
                attr.setAdministrative((Boolean)attrNode.getProperty(PROPERTY_ADMINISTRATIVE));

                listAttributes.add(attr);
            }
            cm.setAttributes(listAttributes);

            //Category
            Relationship relationship = node.getSingleRelationship(RelTypes.BELONGS_TO, Direction.BOTH);
            Node ctgrNode = relationship.getEndNode();
            ctgr.setName((String)ctgrNode.getProperty(PROPERTY_NAME));
            ctgr.setDisplayName((String)ctgrNode.getProperty(PROPERTY_DISPLAY_NAME));
            ctgr.setDescription((String)ctgrNode.getProperty(PROPERTY_DESCRIPTION));

            cm.setCategory(ctgr);

            //parent
            Relationship parentRel = node.getSingleRelationship(RelTypes.EXTENDS, Direction.BOTH);
            cm.setParentName((String)parentRel.getProperty(PROPERTY_NAME));

            tx.success();
        }
        finally{
            tx.finish();
        }
        return cm;
    }

    /**
     * Gets a classmetadata, their attributes and Category
     * @param className
     * @return A ClassMetadata with the className
     * @throws ClassNotFoundException there is no class with such className
     */
    @Override
    public ClassMetadata getClass(String className)throws Exception
    {
        ClassMetadata cm = new ClassMetadata();
        List<AttributeMetadata> listAttributes = new ArrayList();
        CategoryMetadata ctgr = new CategoryMetadata();
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(PROPERTY_NAME,className).getSingle();
            
            if(node == null)
                throw new ClassNotFoundException("The Class Name: "+ className +
                                                 ", you are looking for does not exist");

            //node Properties
            cm.setName((String)node.getProperty(PROPERTY_NAME));
            cm.setDisplayName((String)node.getProperty(PROPERTY_DISPLAY_NAME));
            cm.setCountable((Boolean)node.getProperty(PROPERTY_COUNTABLE));
            cm.setColor((Integer)node.getProperty(PROPERTY_COLOR));
            cm.setLocked((Boolean)node.getProperty(PROPERTY_LOCKED));
            cm.setDescription(PROPERTY_DESCRIPTION);
            cm.setAbstractClass((Boolean)node.getProperty(PROPERTY_ABSTRACT));
            cm.setDummy((Boolean)node.getProperty(PROPERTY_DUMMY));
            cm.setInterfaces(null);
            cm.setIcon((Byte)node.getProperty(PROPERTY_ICON));
            cm.setSmallIcon((Byte)node.getProperty(PROPERTY_SMALL_ICON));

            //Attributes
            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS);

            for (Relationship relationship : relationships) {
                Node attrNode =relationship.getEndNode();
                AttributeMetadata attr = new AttributeMetadata();
                attr.setName((String)attrNode.getProperty(PROPERTY_NAME));
                attr.setDescription((String)attrNode.getProperty(PROPERTY_DESCRIPTION));
                attr.setDisplayName((String)attrNode.getProperty(PROPERTY_DISPLAY_NAME));
                //attr.setMultiple((Boolean)atr.getProperty(AttributeMetadata.PROPERTY_MUTIPLE));
                attr.setReadOnly((Boolean)attrNode.getProperty(PROPERTY_READONLY));
                attr.setType((String)attrNode.getProperty(PROPERTY_TYPE));
                attr.setVisible((Boolean)attrNode.getProperty(PROPERTY_VISIBLE));
                attr.setAdministrative((Boolean)attrNode.getProperty(PROPERTY_ADMINISTRATIVE));

                listAttributes.add(attr);
            }

            cm.setAttributes(listAttributes);

            //Category
            Relationship ctgrRel = node.getSingleRelationship(RelTypes.BELONGS_TO, Direction.BOTH);
            Node ctgrNode = ctgrRel.getEndNode();
            ctgr.setName((String)ctgrNode.getProperty(PROPERTY_NAME));
            ctgr.setDisplayName((String)ctgrNode.getProperty(PROPERTY_DISPLAY_NAME));
            ctgr.setDescription((String)ctgrNode.getProperty(PROPERTY_DESCRIPTION));

            cm.setCategory(ctgr);

            //Parent
            Relationship parentRel = node.getSingleRelationship(RelTypes.EXTENDS, Direction.BOTH);
            cm.setParentName((String)parentRel.getEndNode().getProperty(PROPERTY_NAME));

            tx.success();

        }
        finally{
            tx.finish();
        }
        return cm;
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
    public boolean moveClass(String classToMoveName, String targetParentClassName)throws Exception
    {
        Transaction tx = graphDb.beginTx();
        try{
                Node ctm = classIndex.get(PROPERTY_NAME, classToMoveName).getSingle();
                Node tcn = classIndex.get(PROPERTY_NAME, targetParentClassName).getSingle();

           if(ctm == null)
               throw new ClassNotFoundException("The Class name " +classToMoveName+
                       " you are trying to move does not exist");
           else if(tcn == null)
               throw new ClassNotFoundException("The Class name " +targetParentClassName+
                       " you are trying to set as parent does not exist");
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
    public boolean moveClass(Long classToMoveId, Long targetParentClassId)throws Exception
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node ctm = classIndex.get(PROPERTY_ID, String.valueOf(classToMoveId)).getSingle();
            Node tcn = classIndex.get(PROPERTY_ID, String.valueOf(targetParentClassId)).getSingle();

            if(ctm == null)
               throw new ClassNotFoundException("The Class Id " +classToMoveId+
                       " you are trying to move does not exist");
            else if(tcn == null)
               throw new ClassNotFoundException("The Class Id " +targetParentClassId+
                       " you are trying to set as parent does not exist");
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
     * Adds an attribute to the class
     * @param className
     * @param attributeDefinition
     * @return true if success
     * @throws ClassNotFoundException if there is no a class with such className
     */
    @Override
    public boolean addAttribute(String className, AttributeMetadata attributeDefinition)throws Exception
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(PROPERTY_NAME,className).getSingle();

            if(node == null)
                throw new ClassNotFoundException("The Class name " + className + "does not exist");

            Node atr = graphDb.createNode();
            
            atr.setProperty(PROPERTY_NAME, attributeDefinition.getName());
            atr.setProperty(PROPERTY_DESCRIPTION, attributeDefinition.getDescription());
            atr.setProperty(PROPERTY_DISPLAY_NAME, attributeDefinition.getDisplayName());
            atr.setProperty(PROPERTY_TYPE, attributeDefinition.getType());

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
    public boolean addAttribute(Long classId, AttributeMetadata attributeDefinition )throws Exception
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(PROPERTY_ID, String.valueOf(classId)).getSingle();
            
            if (node == null)
                throw new ClassNotFoundException("The Class Id " + classId + "does not exist");

            Node atr = graphDb.createNode();
            
            atr.setProperty(PROPERTY_NAME, attributeDefinition.getName());
            atr.setProperty(PROPERTY_DESCRIPTION, attributeDefinition.getDescription());
            atr.setProperty(PROPERTY_DISPLAY_NAME, attributeDefinition.getDisplayName());
            atr.setProperty(PROPERTY_TYPE, attributeDefinition.getType());
            //atr.setProperty(PROPERTY_MUTIPLE, attributeDefinition.getMultip);
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
     * Gets an attribute belonging to a class
     * @param className
     * @param attributeName
     * @return AttributeMetada, null if there is no attribute with such name
     * @throws ClassNotFoundException if there is no a class with such className
     * @throws MiscException if the attributeName does not exist
     */
    @Override
    public AttributeMetadata getAttribute(String className, String attributeName)throws Exception
    {
        AttributeMetadata attribute = null;
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(PROPERTY_NAME,className).getSingle();

            if(node == null)

                throw new ClassNotFoundException("The class Name: " + className + ", does not exist");

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS);
            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                if (String.valueOf(atr.getProperty(PROPERTY_NAME)).equals(attributeName))
                {
                    attribute = new AttributeMetadata();
                    attribute.setName((String)atr.getProperty(PROPERTY_NAME));
                    attribute.setDescription((String)atr.getProperty(PROPERTY_DESCRIPTION));
                    attribute.setDisplayName((String)atr.getProperty(PROPERTY_DISPLAY_NAME));
                    //attribute.setMultiple((Boolean)atr.getProperty(AttributeMetadata.PROPERTY_MUTIPLE));
                    attribute.setReadOnly((Boolean)atr.getProperty(PROPERTY_READONLY));
                    attribute.setType((String)atr.getProperty(PROPERTY_TYPE));
                    attribute.setVisible((Boolean)atr.getProperty(PROPERTY_VISIBLE));
                    attribute.setAdministrative((Boolean)atr.getProperty(PROPERTY_ADMINISTRATIVE));
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
    public AttributeMetadata getAttribute(Long classId, String attributeName)throws Exception
    {
        AttributeMetadata attribute = null;
        Transaction tx = graphDb.beginTx();
        try{
            //TODO poner exception no hay classId
            Node node = classIndex.get(PROPERTY_ID, String.valueOf(classId)).getSingle();

            if(node == null)
                throw new ClassNotFoundException(("The classId: " + classId + ", does not exist"));

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS);
            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                if (String.valueOf(atr.getProperty(PROPERTY_NAME)).equals(attributeName))
                {
                    attribute = new AttributeMetadata();
                    attribute.setName((String)atr.getProperty(PROPERTY_NAME));
                    attribute.setDescription((String)atr.getProperty(PROPERTY_DESCRIPTION));
                    attribute.setDisplayName((String)atr.getProperty(PROPERTY_DISPLAY_NAME));
//                    attribute.setMultiple((Boolean)atr.getProperty(PROPERTY_MUTIPLE));
                    attribute.setReadOnly((Boolean)atr.getProperty(PROPERTY_READONLY));
                    attribute.setType((String)atr.getProperty(PROPERTY_TYPE));
                    attribute.setVisible((Boolean)atr.getProperty(PROPERTY_VISIBLE));
                    attribute.setAdministrative((Boolean)atr.getProperty(PROPERTY_ADMINISTRATIVE));
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
    public boolean deleteAttribute(String className, String attributeName)throws Exception
    {
        Transaction tx = graphDb.beginTx();
        boolean couldDelAtt = false;
        try{
            //TODO poner exception no hay classId
            Node node = classIndex.get(PROPERTY_NAME, className).getSingle();

            if (node == null)
                throw new ClassNotFoundException ("The class name: " + className + ", does not exist");

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
                throw new MiscException ("The Attribute: " +attributeName+
                               ", you are looking for does not exist");

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
    public boolean deleteAttribute(Long classId,String attributeName) throws Exception
    {
        Transaction tx = graphDb.beginTx();
        boolean couldDelAtt = false;
        try{
            //TODO poner exception no hay classId
            Node node = classIndex.get(PROPERTY_ID, String.valueOf(classId)).getSingle();

            if(node == null)
                throw new ClassNotFoundException ("The classId: " + classId +
                                                  ", does not exist");

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
                throw new MiscException("The Attribute: " + attributeName +
                                                 ", you are looking for does not exist");

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

            id = category.getId();

            categoryIndex.add(category, PROPERTY_ID,String.valueOf(id));
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
    public CategoryMetadata getCategory(String categoryName) throws Exception
    {
        CategoryMetadata cm = new CategoryMetadata();
        Transaction tx = graphDb.beginTx();
        try{
            Node ctgNode = categoryIndex.get(PROPERTY_NAME, categoryName).getSingle();

            if(ctgNode == null)
                throw new MiscException("Can not find the category with the name "
                        + categoryName +
                        " the category definition could not be changed");

            cm.setName((String)ctgNode.getProperty(PROPERTY_NAME));
            cm.setDescription((String)ctgNode.getProperty(PROPERTY_DESCRIPTION));
            cm.setDisplayName((String)ctgNode.getProperty(PROPERTY_DISPLAY_NAME));
            tx.success();

        }finally{
            tx.finish();
        }
        return cm;
    }

    /**
     * Gets a Category with it's Id
     * @param categoryId
     * @return CategoryMetadata
     * @throws MiscException if there is no Category with such cetegoryId
     */
     
    public CategoryMetadata getCategory(Integer categoryId) throws Exception {
        CategoryMetadata cm = new CategoryMetadata();
        Transaction tx = graphDb.beginTx();
        try{
            Node ctgNode = categoryIndex.get(PROPERTY_ID, String.valueOf(categoryId)).getSingle();

             if(ctgNode == null)
                throw new MiscException("Can not find the category with the id "
                        + categoryId +
                        " the category definition could not be changed");

            cm.setName((String)ctgNode.getProperty(PROPERTY_NAME));
            cm.setDescription((String)ctgNode.getProperty(PROPERTY_DESCRIPTION));
            cm.setDisplayName((String)ctgNode.getProperty(PROPERTY_DISPLAY_NAME));

            tx.success();

        }finally{
            tx.finish();
        }
        return cm;
    }

    /**
     * Changes a category definition
     * @param categoryDefinition
     * @return true if success
     * @throws MiscException if there is no Category with such cetegoryId
     */
    public boolean changeCategoryDefinition(CategoryMetadata categoryDefinition) throws Exception {
        Transaction tx = graphDb.beginTx();
        try{
            Node ctgr = categoryIndex.get(PROPERTY_NAME, categoryDefinition.getName()).getSingle();

             if(ctgr == null)
                throw new MiscException("Can not find the category with the name "
                        + categoryDefinition.getName() +
                        " the category definition could not be changed");

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

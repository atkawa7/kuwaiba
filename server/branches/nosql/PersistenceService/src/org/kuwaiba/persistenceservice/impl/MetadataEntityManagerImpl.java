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
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
import org.neo4j.graphdb.*;
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

    public static final String PROPERTY_ID = "id"; //NOI18N
    public static final String PROPERTY_LOCKED = "locked"; //NOI18N
    public static final String PROPERTY_ABSTRACT = "abstract"; //NOI18N
    public static final String PROPERTY_CUSTOM = "custom"; //NOI18N
    public static final String PROPERTY_COUNTABLE = "countable"; //NOI18N
    public static final String PROPERTY_DUMMY = "dummy"; //NOI18N
    public static final String PROPERTY_PARENT_ID = "parentId"; //NOI18N
    public static final String PROPERTY_INTERFACES = "interfaces"; //NOI18N
    public static final String PROPERTY_COLOR = "color"; //NOI18N
    public static final String PROPERTY_ICON = "color"; //NOI18N
    public static final String PROPERTY_SMALL_ICON = "smallIcon"; //NOI18N
    public static final String PROPERTY_LIST_TYPE = "listType"; //NOI18N
    public static final String PROPERTY_ATRIBUTES = "atributes"; //NOI18N
    public static final String PROPERTY_REMOVABLE = "removable"; //NOI18N

    private static final String CLASS_NAME = "classname";
    private static final String CLASS_ID = "classid";
    private static final String CATEGORY_ID = "categoryid";
    private static final String CATEGORY_NAME = "categoryname";

    /**
     * Reference to the db's handle
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
     * Conntion manager
     */
    private ConnectionManager cmn = new ConnectionManagerImpl();

    /**
     * Constructor
     * Get the a database contection and indexes from the connection manager.
     */

    public MetadataEntityManagerImpl() {
        graphDb = (EmbeddedGraphDatabase) cmn.getConnectionHandler();
        classIndex = (Index<Node>) cmn.getIndexHandler();
    }

    /**
     * Creates a classmetadata with their attributes as a nodes
     * @param newclass
     * @return
     */
    @Override
    public Long createClass(ClassMetadata newclass)
    {
        Transaction tx = graphDb.beginTx();
        Long id;
        List<AttributeMetadata> ats = newclass.getAttributes();
        try{
            Node node = graphDb.createNode();

            node.setProperty(PROPERTY_NAME, newclass.getName());
            node.setProperty(PROPERTY_DISPLAY_NAME, newclass.getDisplayName());
            node.setProperty(PROPERTY_PARENT_ID, newclass.getParentId());

            id = node.getId();
            classIndex.add(node, CLASS_NAME,  newclass.getName());
            classIndex.add(node, CLASS_ID,  String.valueOf(node.getId()));

            //Category
            //if the category already exists
            Node ctgrNode = categoryIndex.get(CATEGORY_NAME, newclass.getCategory().getName()).getSingle();
            if(ctgrNode == null){
                Long ctgrId = createCategory(newclass.getCategory());
                ctgrNode = categoryIndex.get(CATEGORY_ID, ctgrId).getSingle();
            }
            node.createRelationshipTo(ctgrNode, RelTypes.BELONGS_TO);

            //Attributes
            for (AttributeMetadata at : newclass.getAttributes()) {
                addAttribute(id, at);
            }

            Node parentNode = classIndex.get(CLASS_ID, String.valueOf(newclass.getParentId())).getSingle();

            node.createRelationshipTo(parentNode, RelTypes.EXTENDS);

            tx.success();

            return id;

        }catch(IllegalArgumentException e){
            System.out.println("There is a null property in the class(may be "
                    + "the Parent Id or the Category definition), could not create the class");

        }catch(NullPointerException e){
            System.out.println("There is a null property in the class(may be "
                    + "the list attributes), could not create the class");
        }finally{
            tx.finish();
        }

        return null;

    }

    /**
     * Changes the definiton of a classmetadata
     * @param newClassDefinition
     * @return
     */
    @Override
    public boolean changeClassDefinition(ClassMetadata newClassDefinition)
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node newcm = classIndex.get(CLASS_NAME, newClassDefinition.getName()).getSingle();
            if(newcm == null)
                throw new NullPointerException();

            newcm.setProperty(PROPERTY_NAME, newClassDefinition.getName());
            newcm.setProperty(PROPERTY_DISPLAY_NAME, newClassDefinition.getDisplayName());

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

        }catch(NullPointerException e){
            System.out.println("Can not find the class with the name "
                               + newClassDefinition.getName() +
                               " the class definition could not be changed");
        }finally{
            tx.finish();
        }

        return false;
    }

    /**
     * Deletes a classmetadata and their attirbutes
     * @param classId
     * @return
     */
    @Override
    public boolean deleteClass(Long classId)
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(CLASS_ID, String.valueOf(classId)).getSingle();
            //Deleting attributes
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

        }catch (NullPointerException e){
            System.out.println("The ClassId: " + classId +
                               ", you are trying to remove does not exist");
        }
        finally{
            tx.finish();
        }

        return false;
    }

    /**
     * Deletes a classmetadata and their attirbutes
     * @param className
     * @return
     */
    @Override
    public boolean deleteClass(String className)
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(CLASS_NAME, className).getSingle();
            //Deleting attributes
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
        }catch (NullPointerException e){
            System.out.println("The ClassName: " + className +
                               ", you are tryin to remove does not exist");
        }
        finally{
            tx.finish();
        }
        return false;
    }

    /**
     * Gets a classmetadata and their attirbutes
     * @param classId
     * @return
     */
    @Override
    public ClassMetadata getClass(Long classId)
    {
        ClassMetadata cm = new ClassMetadata();
        List<AttributeMetadata> listAttributes = new ArrayList();
        CategoryMetadata ctgr = new CategoryMetadata();
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(CLASS_ID, String.valueOf(classId)).getSingle();

            //TODO poner los demas atributos de clase
            cm.setName((String)node.getProperty(PROPERTY_NAME));
            cm.setDisplayName((String)node.getProperty(PROPERTY_DISPLAY_NAME));
            cm.setParentId((Long)node.getProperty(PROPERTY_PARENT_ID));
            //Attributes
            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS);

            for (Relationship relationship : relationships) {

                Node attrNode =relationship.getEndNode();

                AttributeMetadata attr = new AttributeMetadata();
                attr.setName((String)attrNode.getProperty(PROPERTY_NAME));
                attr.setDescription((String)attrNode.getProperty(PROPERTY_DESCRIPTION));

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

            tx.success();
        }catch(NullPointerException e){
            System.out.println("The Class Id: " + classId +
                               ", you are looking for does not exist");
        }
        finally{
            tx.finish();
        }
        return cm;
    }

    /**
     * Gets a classmetadata and their attirbutes
     * @param className
     * @return
     */
    @Override
    public ClassMetadata getClass(String className)
    {
        ClassMetadata cm = new ClassMetadata();
        List<AttributeMetadata> listAttributes = new ArrayList();
        CategoryMetadata ctgr = new CategoryMetadata();
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(CLASS_NAME,className).getSingle();
            //TODO poner los demas atributos de clase
            cm.setName((String)node.getProperty(PROPERTY_NAME));
            cm.setDisplayName((String)node.getProperty(PROPERTY_DISPLAY_NAME));
            cm.setParentId((Long)node.getProperty(PROPERTY_PARENT_ID));

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS);

            for (Relationship relationship : relationships) {
                Node attrNode =relationship.getEndNode();
                AttributeMetadata attr = new AttributeMetadata();
                attr.setName((String)attrNode.getProperty(PROPERTY_NAME));
                attr.setDescription((String)attrNode.getProperty(PROPERTY_DESCRIPTION));

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

            tx.success();

        }catch(NullPointerException e){
            System.out.println("The Class Name: " + className +", you are looking for does not exist");
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
     * @return
     */
    @Override
    public boolean moveClass(String classToMoveName, String targetParentClassName)
    {
        Transaction tx = graphDb.beginTx();
        try{
                Node ctm = classIndex.get(CLASS_NAME, classToMoveName).getSingle();
                Node tcn = classIndex.get(CLASS_NAME, targetParentClassName).getSingle();

           if(ctm == null)
               throw new NullPointerException("The Class name " +classToMoveName+
                       " you are trying to move does not exist");
           else if(tcn == null)
               throw new NullPointerException("The Class name " +targetParentClassName+
                       " you are trying to set as parent does not exist");
           else{
                Iterable<Relationship> relationships = ctm.getRelationships(RelTypes.EXTENDS);

                for (Relationship rel : relationships) {
                    Node node = rel.getEndNode();

                    if(node.getId() == (Long)ctm.getProperty(PROPERTY_PARENT_ID))
                        rel.delete();
               }
               //TODO if the parent node has no relationships it must be deleted?
               ctm.createRelationshipTo(tcn, RelTypes.EXTENDS);
               ctm.setProperty(PROPERTY_PARENT_ID, tcn.getId());
           }

            tx.success();
        }catch(NullPointerException e){
                System.out.println(e.getMessage());
        }
        finally{
            tx.finish();
        }
        return true;
    }

    /**
     * Moves a class from one parentClass to an other parentClass
     * @param classToMoveId
     * @param targetParentClassId
     * @return
     */
    @Override
    public boolean moveClass(Long classToMoveId, Long targetParentClassId)
    {

        Transaction tx = graphDb.beginTx();
        try{
            Node ctm = classIndex.get(CLASS_ID, String.valueOf(classToMoveId)).getSingle();
            Node tcn = classIndex.get(CLASS_ID, String.valueOf(targetParentClassId)).getSingle();

            if(ctm == null)
               throw new NullPointerException("The Class Id " +classToMoveId+
                       " you are trying to move does not exist");
           else if(tcn == null)
               throw new NullPointerException("The Class Id " +targetParentClassId+
                       " you are trying to set as parent does not exist");
           else{
               Iterable<Relationship> relationships = ctm.getRelationships(RelTypes.EXTENDS);

                for (Relationship rel : relationships) {
                    Node node = rel.getEndNode();

                    if(node.getId() == (Long)ctm.getProperty(PROPERTY_PARENT_ID))
                        rel.delete();
               }
               //TODO if the parent node has no relationships it must be deleted?
               ctm.createRelationshipTo(tcn, RelTypes.EXTENDS);
               ctm.setProperty(PROPERTY_PARENT_ID, tcn.getId());
           }

            tx.success();
            return true;

        }catch(NullPointerException e){
                System.out.println(e.getMessage());
        }
        finally{
            tx.finish();
        }
        return false;
    }

    /**
     * Adds an attibute to the class
     * @param className
     * @param attributeDefinition
     * @return
     */
    @Override
    public boolean addAttribute(String className, AttributeMetadata attributeDefinition)
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(CLASS_NAME,className).getSingle();
            //TODO poner excepción nodo no encontrado
            Node atr = graphDb.createNode();
            //TODO poner los de mas class attributes
            atr.setProperty(PROPERTY_NAME, attributeDefinition.getName());
            atr.setProperty(PROPERTY_DESCRIPTION, attributeDefinition.getDescription());

            node.createRelationshipTo(atr, RelTypes.HAS);

            tx.success();
            return true;

        }catch(NullPointerException e){
            System.out.println("The Class name " + className + "does not exist");
        }catch(IllegalArgumentException e){
            System.out.println("There is a null property in the attribute you are trying to add");
        }
        finally{
            tx.finish();
        }
        return false;
    }

    /**
     * Adds an attibute to the class
     * @param classId
     * @param attributeDefinition
     * @return
     */
    @Override //TODO agregarlo al modelo!
    public boolean addAttribute(Long classId, AttributeMetadata attributeDefinition )
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(CLASS_ID, String.valueOf(classId)).getSingle();
            //TODO poner excepción nodo no encontrado
            Node atr = graphDb.createNode();
            //TODO ponerlos demas atributos
            atr.setProperty(PROPERTY_NAME, attributeDefinition.getName());
            atr.setProperty(PROPERTY_DESCRIPTION, attributeDefinition.getDescription());

            node.createRelationshipTo(atr, RelTypes.HAS);

            tx.success();
            return true;

        }catch(NullPointerException e){
            System.out.println("The Class Id " + classId + "does not exist");
        }catch(IllegalArgumentException e){
            System.out.println("There is a null property in the Attribute you are trying to add");
        }
        finally{
            tx.finish();
        }
        return false;
    }

    /**
     * Gets an attibute from a class
     * @param className
     * @param attributeName
     * @return
     */
    @Override
    public AttributeMetadata getAttribute(String className, String attributeName)
    {
        AttributeMetadata attribute = new AttributeMetadata();
        Transaction tx = graphDb.beginTx();
        try{
            Node node = classIndex.get(CLASS_NAME,className).getSingle();

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS);
            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                if (String.valueOf(atr.getProperty(PROPERTY_NAME)).equals(attributeName))
                {
                    attribute.setName((String)atr.getProperty(PROPERTY_NAME));
                    attribute.setDescription((String)atr.getProperty(PROPERTY_DESCRIPTION));
//                    attribute.setDisplayName((String)atr.getProperty(AttributeMetadata.PROPERTY_DISPLAY_NAME));
//                    attribute.setMultiple((Boolean)atr.getProperty(AttributeMetadata.PROPERTY_MUTIPLE));
//                    attribute.setReadOnly((Boolean)atr.getProperty(AttributeMetadata.PROPERTY_READONLY));
//                    attribute.setType((String)atr.getProperty(AttributeMetadata.PROPERTY_TYPE));
//                    attribute.setVisible((Boolean)atr.getProperty(AttributeMetadata.PROPERTY_VISIBLE));
//                    attribute.setAdministrative((Boolean)atr.getProperty(AttributeMetadata.PROPERTY_ADMINISTRATIVE));
                }
            }
            if (attribute.getName() == null)
                throw new NotFoundException();

            tx.success();

        }catch(NotFoundException e){
            System.out.println("The Attribute: " +attributeName+
                               ", you are looking for does not exist");

        }catch(NullPointerException e){
            System.out.println("The class Name: " + className + ", does not exist");
        }
        finally{
            tx.finish();
        }
        return attribute;
    }

    /**
     * Gets an attibute from a class
     * @param classId
     * @param attributeName
     * @return
     */
    @Override//TODO probar con mas relaciones
    public AttributeMetadata getAttribute(Long classId, String attributeName)
    {
        AttributeMetadata attribute = new AttributeMetadata();
        Transaction tx = graphDb.beginTx();
        try{
            //TODO poner exception no hay classId
            Node node = classIndex.get(CLASS_ID, String.valueOf(classId)).getSingle();

            Iterable<Relationship> relationships = node.getRelationships(RelTypes.HAS);
            for (Relationship relationship : relationships) {
                Node atr = relationship.getEndNode();
                if (String.valueOf(atr.getProperty(PROPERTY_NAME)).equals(attributeName))
                {
                    attribute.setName((String)atr.getProperty(PROPERTY_NAME));
                    attribute.setDescription((String)atr.getProperty(PROPERTY_DESCRIPTION));
//                    attribute.setDisplayName((String)atr.getProperty(AttributeMetadata.PROPERTY_DISPLAY_NAME));
//                    attribute.setMultiple((Boolean)atr.getProperty(AttributeMetadata.PROPERTY_MUTIPLE));
//                    attribute.setReadOnly((Boolean)atr.getProperty(AttributeMetadata.PROPERTY_READONLY));
//                    attribute.setType((String)atr.getProperty(AttributeMetadata.PROPERTY_TYPE));
//                    attribute.setVisible((Boolean)atr.getProperty(AttributeMetadata.PROPERTY_VISIBLE));
//                    attribute.setAdministrative((Boolean)atr.getProperty(AttributeMetadata.PROPERTY_ADMINISTRATIVE));
                }
            }
            if (attribute.getName() == null)
                throw new NotFoundException();

            tx.success();
        }catch(NotFoundException e){
            System.out.println("The Attribute: " +attributeName+
                               ", you are looking for does not exist");

        }catch(NullPointerException e){
            System.out.println("The classId: " + classId + ", does not exist");
        }finally{
            tx.finish();
        }
        return attribute;
    }

    /**
     * Changes an attibute definition in the classMetadata
     * @param ClassId
     * @param newAttributeDefinition
     * @return
     */
    @Override //TODO poner en el modelo
    public boolean changeAttributeDefinition(Long ClassId, AttributeMetadata newAttributeDefinition)
    {
        return true;
    }

    /**
     * Deletes an attibute from a classMetadata
     * @param className
     * @param attributeName
     * @return
     */

    @Override
    public boolean deleteAttribute(String className, String attributeName)
    {
        Transaction tx = graphDb.beginTx();
        boolean couldDelAtt = false;
        try{
            //TODO poner exception no hay classId
            Node node = classIndex.get(CLASS_NAME, className).getSingle();

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
                throw new NotFoundException();

            tx.success();
            return true;

        }catch(NotFoundException e){
            System.out.println("The Attribute: " +attributeName+
                               ", you are looking for does not exist");
        }catch(NullPointerException e){
            System.out.println("The class name: " + className + ", does not exist");
        }
        finally{
            tx.finish();
        }
        return false;
    }

    /**
     * Deletes an attibute from a classMetadata
     * @param classId
     * @param attributeName
     * @return
     */

    @Override //TODO ponerlo en el modelo
    public boolean deleteAttribute(Long classId,String attributeName)
    {
        Transaction tx = graphDb.beginTx();
        boolean couldDelAtt = false;
        try{
            //TODO poner exception no hay classId
            Node node = classIndex.get(CLASS_ID, String.valueOf(classId)).getSingle();

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
                throw new NotFoundException();

            tx.success();
            return true;

        }catch(NotFoundException e){
            System.out.println("The Attribute: " +attributeName+
                               ", you are looking for does not exist");

        }catch(NullPointerException e){
            System.out.println("The classId: " + classId + ", does not exist");
        }
        finally{
            tx.finish();
        }
        return false;
    }

    /**
     * Creates an category
     * @param categoryDefinition
     * @return
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

            categoryIndex.add(category, CATEGORY_ID,String.valueOf(id));
            categoryIndex.add(category, CATEGORY_NAME,categoryDefinition.getName());

            tx.success();

        }
        finally{
            tx.finish();
        }

        return id;
    }

    /**
     * Gets a category
     * @param categoryName
     * @return
     */

    @Override
    public CategoryMetadata getCategory(String categoryName)
    {
        CategoryMetadata cm = new CategoryMetadata();
        Transaction tx = graphDb.beginTx();
        try{
            Node ctgNode = categoryIndex.get(CATEGORY_NAME, categoryName).getSingle();

            if(ctgNode == null)
                throw new NullPointerException();

            cm.setName((String)ctgNode.getProperty(PROPERTY_NAME));
            cm.setDescription((String)ctgNode.getProperty(PROPERTY_DESCRIPTION));
            cm.setDisplayName((String)ctgNode.getProperty(PROPERTY_DISPLAY_NAME));
            tx.success();
        }catch(NullPointerException e){
            System.out.println("Can not find the category with the name "
                        + categoryName +
                        " the category definition could not be changed");
        }finally{
            tx.finish();
        }
        return cm;
    }

    /**
     * Gets a category
     * @param categoryId
     * @return
     */

    public CategoryMetadata getCategory(Integer categoryId)
    {
        CategoryMetadata cm = new CategoryMetadata();
        Transaction tx = graphDb.beginTx();
        try{
            Node ctgNode = categoryIndex.get(CATEGORY_ID, String.valueOf(categoryId)).getSingle();

            if(ctgNode == null)
                throw new NullPointerException();

            cm.setName((String)ctgNode.getProperty(PROPERTY_NAME));
            cm.setDescription((String)ctgNode.getProperty(PROPERTY_DESCRIPTION));
            cm.setDisplayName((String)ctgNode.getProperty(PROPERTY_DISPLAY_NAME));

            tx.success();
        }catch(NullPointerException e){
            System.out.println("Can not find the category with the id "
                        + categoryId +
                        " the category definition could not be changed");
        }finally{
            tx.finish();
        }
        return cm;
    }

    /**
     * Changes a category definition
     * @param categoryDefinition
     * @return
     */

    @Override
    public boolean changeCategoryDefinition(CategoryMetadata categoryDefinition)
    {
        Transaction tx = graphDb.beginTx();
        try{
            Node ctgr = categoryIndex.get(CATEGORY_NAME, categoryDefinition.getName()).getSingle();

            if(ctgr == null)
                throw new NullPointerException();

            ctgr.setProperty(PROPERTY_NAME, categoryDefinition.getName());
            ctgr.setProperty(PROPERTY_DISPLAY_NAME, categoryDefinition.getDisplayName());
            ctgr.setProperty(PROPERTY_DESCRIPTION, categoryDefinition.getDescription());

            tx.success();
        }catch(NullPointerException e){
            System.out.println("Can not find the category with the name "
                        + categoryDefinition.getName() +
                        " the category definition could not be changed");
        }finally{
            tx.finish();
        }
        return true;
    }

    @Override
    public boolean deleteCategory(String categoryName)
    {
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

//    public List<ClassMetadata> getMetadata(Integer options){
//        return true;
//    }
   
}

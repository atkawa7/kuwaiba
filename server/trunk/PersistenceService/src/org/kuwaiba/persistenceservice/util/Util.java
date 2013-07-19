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

package org.kuwaiba.persistenceservice.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.UnsupportedPropertyException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.persistenceservice.impl.RelTypes;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.Traversal;

/**
 * Utility class containing misc methods to perform common tasks
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Util {
     /**
     * Gets an attribute type by traversing through the "HAS" relationship of a given class metadata node
     * @param classNode
     * @param attributeName
     * @return attribute's type. 0 if it can't find the attribute
     */
//    public static int getTypeOfAttribute (Node classNode, String attributeName){
//        Iterable<Relationship> attributes = classNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
//        while (attributes.iterator().hasNext()){
//            Relationship rel = attributes.iterator().next();
//            if (rel.getEndNode().getProperty(Constants.PROPERTY_NAME).equals(attributeName))
//                return Integer.valueOf(rel.getEndNode().getProperty(Constants.PROPERTY_TYPE).toString());
//        }
//        return 0;
//    }

    /**
     * Converts a String value to an object value based on a give mapping. This method
     * does not convert binary or relationship-like attributes
     * @param value Value as String
     * @param type Mapping. The allowed values are the AttributeMetadata.MAPPING_XXX
     * @return the converted value
     * @throws InvalidArgumentException If the type can't be converted
     */
    public static Object getRealValue(String value, int mapping, String type) throws InvalidArgumentException{
        if (value == null)
            return null;
        try{
            switch(mapping){
                case AttributeMetadata.MAPPING_PRIMITIVE:
                    if(type.equals("Float"))
                        return Float.valueOf(value);
                    else
                        if(type.equals("Long"))
                            return Long.valueOf(value);
                        else
                            if(type.equals("Integer"))
                                return Integer.valueOf(value);
                            else
                                if(type.equals("Boolean"))
                                    return Boolean.valueOf(value);
                    return value;
                case AttributeMetadata.MAPPING_DATE:
                    return new Date(Long.valueOf(value));
                case AttributeMetadata.MAPPING_TIMESTAMP:
                    return Timestamp.valueOf(value);
                default:
                    throw new InvalidArgumentException(String.format("Can not convert value %1s to a typ %2s", value, type), Level.WARNING);
            }

        }catch (Exception e){
            throw new InvalidArgumentException(String.format("Can not convert value %1s to a typ %2s", value, type), Level.WARNING);
        }
    }

    /**
     * Gets the requested nodes representing list type items
     * @param values A list of Long objects containing the ids of the required list type items
     * @param listType Node the list items are supposed to be instance of
     * @return A list of nodes representing the list type items
     */
    public static List<Node> getRealValue(List<Long> values, Node listType) throws InvalidArgumentException{
        Iterable<Relationship> listTypeItems = listType.getRelationships(RelTypes.INSTANCE_OF, Direction.INCOMING);
        List<Node> res = new ArrayList<Node>();
        for (Relationship listTypeItem : listTypeItems){
            Node instance = listTypeItem.getStartNode();
            if (values.contains(new Long(instance.getId())))
                res.add(instance);
        }
        return res;
    }

    /**
     * Releases all relationships related to an object given its direction and a relationship's property value
     * @param instance Object from/to the relationships are connected
     * @param relationshipType Relationship type
     * @param relationshipDirection Relationship Direction
     * @param propertyName Relationship's property to be used as filter
     * @param propertyValue Relationship's property value to be used as filter
     */
    public static void releaseRelationships(Node instance, RelTypes relationshipType,
            Direction relationshipDirection, String propertyName, String propertyValue) {
        Iterable<Relationship> relatedItems = instance.getRelationships(relationshipType, relationshipDirection);
        for (Relationship relatedItemRelationship : relatedItems){
            if (relatedItemRelationship.getProperty(propertyName).equals(propertyValue))
                relatedItemRelationship.delete();
        }
    }

    /**
     * Deletes recursively and object and all its children. Note that the transaction should be handled by the caller
     * @param instance The object to be deleted
     */
    public static void deleteObject(Node instance, boolean releaseAll) throws OperationNotPermittedException {
        if(!releaseAll){
            if (instance.getRelationships(RelTypes.RELATED_TO, Direction.INCOMING).iterator().hasNext())
                throw new OperationNotPermittedException("deleteObject",String.format("The object with id %1s can not be deleted since it has relationships", instance.getId()));

            if (instance.getRelationships(RelTypes.RELATED_TO_SPECIAL, Direction.OUTGOING).iterator().hasNext())
                throw new OperationNotPermittedException("deleteObject",String.format("The object with id %1s can not be deleted since it has relationships", instance.getId()));
        }

        for (Relationship rel : instance.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING))
            deleteObject(rel.getStartNode(), releaseAll);

        for (Relationship rel : instance.getRelationships())
            rel.delete();

        instance.getGraphDatabase().index().forNodes(Constants.INDEX_OBJECTS).remove(instance);
        instance.delete();
    }

    /**
     * Read and returns the bytes of a given file
     * @param fileName file to be opened
     * @return bytes on that file
     */
    public static byte[] readBytesFromFile(String fileName) throws FileNotFoundException, IOException{
        byte[] bytes = null;
        File f = new File(fileName);
        InputStream is = new FileInputStream(f);
        long length = f.length();

        if (length < Integer.MAX_VALUE) { //checks if the file is too big
            bytes = new byte[(int)length];
            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                   && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file "+f.getName());
            }
        }
        is.close();
        return bytes;
    }


    /**
     * Saves a file, receiving the file name and the contents as parameters. If the directory structure doesn't exist, it's created
     * @param directory
     * @param fileName
     * @param content
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void saveFile(String directory, String fileName, byte[] content) throws FileNotFoundException, IOException {
        File imgDir = new File(directory);
        imgDir.mkdirs();
        FileOutputStream fos = new FileOutputStream(directory + "/" + fileName); //NOI18N
        fos.write(content);
        fos.close();
    }

    /**
     * Gets an object's class name given the node representing it
     * @param objectNode The node to e evaluated
     * @return The object's class name.
     * @throws MetadataObjectNotFoundException if no class node is associated to this node (this should not happen)
     * @throws UnsupportedPropertyException if the class node is malformed
     */
    public static String getObjectClassName(Node objectNode) throws MetadataObjectNotFoundException, UnsupportedPropertyException {
        Iterator<Relationship> iterator = objectNode.getRelationships(RelTypes.INSTANCE_OF).iterator();
        if (!iterator.hasNext())
            throw new MetadataObjectNotFoundException(String.format("The object with id %1s does not have a class associated to it", objectNode.getId()));
        
        Node classNode = iterator.next().getEndNode();
        if (!classNode.hasProperty(Constants.PROPERTY_NAME))
            throw new UnsupportedPropertyException(Constants.PROPERTY_NAME);
        return (String)classNode.getProperty(Constants.PROPERTY_NAME);
    }

    /**
     * Converts a String value to an object value based on a give mapping. This method
     * does not convert binary or relationship-like attributes
     * @param value Value as String
     * @param type Mapping. The allowed values are the AttributeMetadata.MAPPING_XXX
     * @return the converted value
     * @throws InvalidArgumentException If the type can't be converted
     */
    public Integer setRealValue(String value, int mapping, String type) throws InvalidArgumentException{

        try{
            if(type.equals("Float") || type.equals("Long")
                    || type.equals("Integer") || type.equals("Boolean") || type.equals("byte[]"))
                return AttributeMetadata.MAPPING_PRIMITIVE;
            else if(type.equals("Date"))
                return AttributeMetadata.MAPPING_DATE;
            else
                return AttributeMetadata.MAPPING_MANYTOONE;
//            throw new InvalidArgumentException("Can not retrieve the correct value for ("+
//                value+" "+type+"). Please check your mappings", Level.WARNING);
        }catch (Exception e){
            throw new InvalidArgumentException("Can not retrieve the correct value for ("+
                            value+" "+type+"). Please check your mappings", Level.WARNING);
        }
    }

    /**
     * Creates a ClassMetadata with default values
     * @param classMetadata
     * @return
     */
    public static ClassMetadata setDefaultsForClassMetadata(ClassMetadata classDefinition) throws MetadataObjectNotFoundException{
        Integer color = null;
        if(classDefinition.getName() == null){
            throw new MetadataObjectNotFoundException("Can not create a class metadata entry without a name");
        }
        if(classDefinition.getDisplayName() == null){
            classDefinition.setDisplayName("");
        }
        if(classDefinition.getDescription() == null){
            classDefinition.setDisplayName("");
        }
        if(classDefinition.getIcon() == null){
            classDefinition.setIcon(new byte[0]);
        }
        if(classDefinition.getSmallIcon() == null){
            classDefinition.setSmallIcon(new byte[0]);
        }
        try {
            color = Integer.valueOf(classDefinition.getColor());
        } catch (NumberFormatException e) {
            classDefinition.setColor(0);
        }

        return classDefinition;
    }

    /**
     * Creates default values for a AttirbuteMetadata
     * @param AttributeMetadata
     * @return
     */
    public static AttributeMetadata createDefaultAttributeMetadata(AttributeMetadata AttributeDefinition) throws MetadataObjectNotFoundException{
        //Integer mapping = null;
        if(AttributeDefinition.getName() == null){
            throw new MetadataObjectNotFoundException("Can not create an attribute without a name");
        }
        if(AttributeDefinition.getDisplayName() == null){
            AttributeDefinition.setDisplayName("");
        }
        if(AttributeDefinition.getDescription() == null){
            AttributeDefinition.setDisplayName("");
        }
        if(AttributeDefinition.getType() == null){
            AttributeDefinition.setType("");
        }
        return AttributeDefinition;
    }

    public static CategoryMetadata createDefaultCategoryMetadata(CategoryMetadata categoryDefinition) throws MetadataObjectNotFoundException{
        if(categoryDefinition.getName() == null){
            throw new MetadataObjectNotFoundException("Can not create a category without a name");
        }
        if(categoryDefinition.getDescription() == null){
            categoryDefinition.setDescription("");
        }
        if(categoryDefinition.getDisplayName() == null){
            categoryDefinition.setDisplayName("");
        }
        return categoryDefinition;
    }
    
    /**
     * Converts a class metadata node into a ClassMetadataLight object
     * @param classNode
     * @return
     */
    public static ClassMetadataLight createClassMetadataLightFromNode(Node classNode)
    {
        ClassMetadataLight myClass = new ClassMetadataLight(classNode.getId(),(String)classNode.getProperty(Constants.PROPERTY_NAME),(String)classNode.getProperty(Constants.PROPERTY_DISPLAY_NAME));
        
        myClass.setAbstract((Boolean)classNode.getProperty(Constants.PROPERTY_ABSTRACT));
        myClass.setSmallIcon((byte[])classNode.getProperty(Constants.PROPERTY_SMALL_ICON));
        myClass.setViewable((Boolean)isSubClass(Constants.CLASS_VIEWABLEOBJECT, classNode));
        myClass.setId(classNode.getId());
        
        //Parent
        if (classNode.getSingleRelationship(RelTypes.EXTENDS, Direction.OUTGOING) != null){
            myClass.setParentClassName(classNode.getSingleRelationship(
                    RelTypes.EXTENDS, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME).toString());
        }
        else{
            myClass.setParentClassName(null);
        }
        return myClass;
    }

    /**
     * Converts a class metadata node into a ClassMetadata object
     * @param classNode
     * @return
     */
    public static ClassMetadata createClassMetadataFromNode(Node classNode)
    {
        ClassMetadata myClass = new ClassMetadata();
        List<AttributeMetadata> listAttributes = new ArrayList();
        CategoryMetadata ctgr = new CategoryMetadata();

        myClass.setName((String)classNode.getProperty(Constants.PROPERTY_NAME));
        myClass.setAbstract((Boolean)classNode.getProperty(Constants.PROPERTY_ABSTRACT));
        myClass.setColor((Integer)classNode.getProperty(Constants.PROPERTY_COLOR));
        myClass.setCountable((Boolean)classNode.getProperty(Constants.PROPERTY_COUNTABLE));
        myClass.setCustom((Boolean)classNode.getProperty(Constants.PROPERTY_CUSTOM));
        myClass.setDescription((String)classNode.getProperty(Constants.PROPERTY_DESCRIPTION));
        myClass.setDisplayName((String)classNode.getProperty(Constants.PROPERTY_DISPLAY_NAME));
        myClass.setIcon((byte[])classNode.getProperty(Constants.PROPERTY_ICON));
        myClass.setSmallIcon((byte[])classNode.getProperty(Constants.PROPERTY_SMALL_ICON));
        myClass.setId(classNode.getId());
        myClass.setListType(isSubClass(Constants.CLASS_GENERICOBJECTLIST, classNode));
        //Is Viewable if is subclass of
        myClass.setViewable((Boolean)isSubClass(Constants.CLASS_VIEWABLEOBJECT, classNode));
        //Parent
        if (classNode.getSingleRelationship(RelTypes.EXTENDS, Direction.OUTGOING) != null){
            myClass.setParentClassName(
                    classNode.getSingleRelationship(
                        RelTypes.EXTENDS, Direction.OUTGOING).getEndNode().getProperty(
                            Constants.PROPERTY_NAME).toString());
        }
        else{
            myClass.setParentClassName(null);
        }
        //Attributes
        String cypherQuery = "START metadataclass = node({classid}) ".concat(
                             "MATCH metadataclass -[:").concat(RelTypes.HAS_ATTRIBUTE.toString()).concat("]->attribute ").concat(
                             "RETURN attribute ").concat(
                             "ORDER BY attribute.name ASC");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("classid", classNode.getId());//NOI18N

        ExecutionEngine engine = new ExecutionEngine(classNode.getGraphDatabase());
        ExecutionResult result = engine.execute(cypherQuery, params);
        Iterator<Node> n_column = result.columnAs("attribute");
        for (Node attributeNode : IteratorUtil.asIterable(n_column))
        {
             listAttributes.add(createAttributeMetadataFromNode(attributeNode));
        }

        myClass.setAttributes(listAttributes);

        //Category
        if(classNode.getSingleRelationship(RelTypes.BELONGS_TO_GROUP, Direction.BOTH) != null)
        {
            ctgr.setName((String)classNode.getSingleRelationship(RelTypes.BELONGS_TO_GROUP, Direction.BOTH).getEndNode().getProperty(Constants.PROPERTY_NAME));
            ctgr.setDisplayName((String)classNode.getSingleRelationship(RelTypes.BELONGS_TO_GROUP, Direction.BOTH).getEndNode().getProperty(Constants.PROPERTY_DISPLAY_NAME));
            ctgr.setDescription((String)classNode.getSingleRelationship(RelTypes.BELONGS_TO_GROUP, Direction.BOTH).getEndNode().getProperty(Constants.PROPERTY_DESCRIPTION));

            myClass.setCategory(ctgr);
        }
        //Possible Children
        for (Relationship rel : classNode.getRelationships(Direction.OUTGOING, RelTypes.POSSIBLE_CHILD))
        {
            if((Boolean)rel.getEndNode().getProperty(Constants.PROPERTY_ABSTRACT)){
                Traverser traverserMetadata = Util.getAllSubclasses(rel.getEndNode());
                for (Node childNode : traverserMetadata) {
                    if(!(Boolean)childNode.getProperty(Constants.PROPERTY_ABSTRACT)){
                        myClass.getPossibleChildren().add((String)childNode.getProperty(Constants.PROPERTY_NAME));
                    }
                }//end for
            }//end if
            else{
                myClass.getPossibleChildren().add((String)rel.getEndNode().getProperty(Constants.PROPERTY_NAME));
            }
        }

        return myClass;
    }

    /**
     * Converts a attribute metadata node into a AttrributeMetadata object
     * @param AttibuteNode
     * @return
     */
    public static AttributeMetadata createAttributeMetadataFromNode(Node AttibuteNode)
    {
        AttributeMetadata attribute =  new AttributeMetadata();
        try{
            attribute.setName((String)AttibuteNode.getProperty(Constants.PROPERTY_NAME));
            attribute.setDescription((String)AttibuteNode.getProperty(Constants.PROPERTY_DESCRIPTION));
            attribute.setDisplayName((String)AttibuteNode.getProperty(Constants.PROPERTY_DISPLAY_NAME));
            attribute.setMapping((Integer)AttibuteNode.getProperty(Constants.PROPERTY_MAPPING));
            attribute.setReadOnly((Boolean)AttibuteNode.getProperty(Constants.PROPERTY_READONLY));
            attribute.setType((String)AttibuteNode.getProperty(Constants.PROPERTY_TYPE));
            attribute.setVisible((Boolean)AttibuteNode.getProperty(Constants.PROPERTY_VISIBLE));
            attribute.setAdministrative((Boolean)AttibuteNode.getProperty(Constants.PROPERTY_ADMINISTRATIVE));
            attribute.setNoCopy((Boolean)AttibuteNode.getProperty(Constants.PROPERTY_NO_COPY));
            attribute.setUnique((Boolean)AttibuteNode.getProperty(Constants.PROPERTY_UNIQUE));
            attribute.setId(AttibuteNode.getId());

        }catch(Exception e){
            return null;
        }

        return attribute;
    }

    
    /**
     * Builds a RemoteBusinessObject instance from a node representing a business object
     * @param instance
     * @param myClass
     * @return
     * @throws InvalidArgumentException if an attribute value can't be mapped into value
     */
    public static RemoteBusinessObject createRemoteObjectFromNode(Node instance, ClassMetadata myClass) throws InvalidArgumentException{
        
        HashMap<String, List<String>> attributes = new HashMap<String, List<String>>();

        for (AttributeMetadata myAtt : myClass.getAttributes()){
            //Only set the attributes existing in the current node. Please note that properties can't be null in
            //Neo4J, so a null value is actually a non-existing relationship/value
            if (instance.hasProperty(myAtt.getName())){
               if (myAtt.getMapping() == AttributeMetadata.MAPPING_MANYTOMANY ||
                       myAtt.getMapping() == AttributeMetadata.MAPPING_MANYTOONE){
                   continue;
               }else{
                   if (myAtt.getMapping() != AttributeMetadata.MAPPING_BINARY) {
                            List<String> attributeValue = new ArrayList<String>();
                            attributeValue.add(instance.getProperty(myAtt.getName()).toString());
                            attributes.put(myAtt.getName(),attributeValue);
                    }
                }
            }
        }

        //Iterates through relationships and transform the into "plain" attributes
        Iterable<Relationship> relationships = instance.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING);
        while(relationships.iterator().hasNext()){
            Relationship relationship = relationships.iterator().next();
            if (!relationship.hasProperty(Constants.PROPERTY_NAME))
                throw new InvalidArgumentException(String.format("The object with id %1s is malformed", instance.getId()), Level.SEVERE);

            String attributeName = (String)relationship.getProperty(Constants.PROPERTY_NAME);
            for (AttributeMetadata myAtt : myClass.getAttributes()){
                if (myAtt.getName().equals(attributeName)){
                    if (attributes.get(attributeName)==null)
                        attributes.put(attributeName, new ArrayList<String>());
                    attributes.get(attributeName).add(String.valueOf(relationship.getEndNode().getId()));
                }
            }
        }
        RemoteBusinessObject res = new RemoteBusinessObject(instance.getId(), myClass.getName(), attributes);
        return res;
    }

    /**
     * Converts a node representing a user into a UserProfile object
     * @param userNode
     * @return UserProfile
     */

    public static UserProfile createUserProfileFromNode(Node userNode)
    {
       Iterable<Relationship> relationships = userNode.getRelationships(RelTypes.BELONGS_TO_GROUP, Direction.OUTGOING);
       List<GroupProfile> groups = new ArrayList<GroupProfile>();

       for (Relationship relationship : relationships) {
            Node groupNode = relationship.getEndNode();
            groups.add(new GroupProfile(groupNode.getId(),
                        (String)groupNode.getProperty(Constants.PROPERTY_NAME),
                        (String)groupNode.getProperty(Constants.PROPERTY_DESCRIPTION),
                        (Long)groupNode.getProperty(Constants.PROPERTY_CREATION_DATE))
                     );
        }

       UserProfile user =  new UserProfile(
                userNode.getId(),
                (String)userNode.getProperty(Constants.PROPERTY_NAME),
                (String)userNode.getProperty(Constants.PROPERTY_FIRST_NAME),
                (String)userNode.getProperty(Constants.PROPERTY_LAST_NAME),
                (Boolean)userNode.getProperty(Constants.PROPERTY_ENABLED),
                (Long)userNode.getProperty(Constants.PROPERTY_CREATION_DATE),
                null);

       user.setGroups(groups);

        return user;
    }

    /**
     * Converts a node representing a group into a GroupProfile object
     * @param groupNode
     * @return
     */
    public static GroupProfile createGroupProfileFromNode(Node groupNode){
        Iterable<Relationship> relationships = groupNode.getRelationships(RelTypes.BELONGS_TO_GROUP, Direction.INCOMING);
        List<UserProfile> users = new ArrayList<UserProfile>();

        for (Relationship relationship : relationships) {
            Node userNode = relationship.getStartNode();
            users.add(new UserProfile(userNode.getId(),
                        (String)userNode.getProperty(Constants.PROPERTY_NAME),
                        (String)userNode.getProperty(Constants.PROPERTY_FIRST_NAME),
                        (String)userNode.getProperty(Constants.PROPERTY_LAST_NAME),
                        (Boolean)userNode.getProperty(Constants.PROPERTY_ENABLED),
                        (Long)userNode.getProperty(Constants.PROPERTY_CREATION_DATE),
                        null)
                     );
        }
        GroupProfile group =  new GroupProfile(
                groupNode.getId(),
                (String)groupNode.getProperty(Constants.PROPERTY_NAME),
                (String)groupNode.getProperty(Constants.PROPERTY_DESCRIPTION),
                (Long)groupNode.getProperty(Constants.PROPERTY_CREATION_DATE),
                null,
                null);
        group.setUsers(users);
        return group;
    }

    /**
     * Traverses the graph up into the class hierarchy trying to find out if a given class
     * is the subclass of another
     * @param allegedParentClass The alleged parent class name
     * @param startNode Class metadata node corresponding to the child class
     * @return
     */
    public static boolean isSubClass(String allegedParentClass, Node currentNode){
        Iterable<Relationship> parent = currentNode.getRelationships(RelTypes.EXTENDS, Direction.OUTGOING);
        if (!parent.iterator().hasNext())
            return false;

        Node parentNode = parent.iterator().next().getEndNode();

        if (parentNode.getProperty(Constants.PROPERTY_NAME).equals(allegedParentClass))
            return true;

        return isSubClass(allegedParentClass, parentNode);
    }

   /**
     * Traverses the graph up into the class hierarchy trying to find out if a given class
     * is the possiblechild of another
     * @param allegedParentClass The alleged parent class name
     * @param startNode Class metadata node corresponding to the child class
     * @return
     */
    public static boolean isPossibleChild(String allegedParentClass, Node currentNode){
        Iterable<Relationship> parents = currentNode.getRelationships(RelTypes.POSSIBLE_CHILD, Direction.INCOMING);
        for(Relationship parent : parents){
            Node parentNode = parent.getStartNode();

            if (parentNode.getProperty(Constants.PROPERTY_NAME).equals(allegedParentClass))
                return true;
        }
        return false;
    }

    /**
     * Given a plain string, it calculate the MD5 hash. This method is used when authenticating users
     * Thanks to cholland for the code snippet at http://snippets.dzone.com/posts/show/3686
     * @param pass
     * @return the MD5 hash for the given string
     */
    public static String getMD5Hash(String pass) {
        try{
		MessageDigest m = MessageDigest.getInstance("MD5");
		byte[] data = pass.getBytes();
		m.update(data,0,data.length);
		BigInteger i = new BigInteger(1,m.digest());
		return String.format("%1$032X", i);
        }catch(NoSuchAlgorithmException nsa){
            return null;
        }
    }

    /**
     * Retrieves the subclasses of a given class metadata node within the class hierarchy
     * @param ClassMetadata
     * @return
     */

    public static Traverser getAllSubclasses(final Node ClassMetadata)
    {
        return ClassMetadata.traverse(Order.BREADTH_FIRST,
                StopEvaluator.END_OF_GRAPH,
                ReturnableEvaluator.ALL_BUT_START_NODE, RelTypes.EXTENDS,
                Direction.INCOMING);
    }

    /**
     * Retrieves a String with the property value of the attribute if exists as
     * attribute of the node, if the property is a date it is formating into
     * yyyy-MM-DD, if does not exists it return an empty string.
     * @param objectNode
     * @param className
     * @param visibleAttribute
     * @return
     */
    public static String getAttributeFromNode(Node objectNode, String attribute){
        if(objectNode.hasProperty(attribute)){
            Object property = objectNode.getProperty(attribute);
            if(attribute.equals(Constants.PROPERTY_CREATION_DATE)){
                Date creationDate = new Date((Long)property);
                SimpleDateFormat formatoDeFecha = new SimpleDateFormat(Constants.DATE_FORMAT);//NOI18N
                    return formatoDeFecha.format(creationDate);
            }
            else
                return property.toString();
        }//end if node has no attribute yet
        else
           return "";
    }

    /**
     * Gets the type(String, Integer, Float, Boolean) of an attribute
     * @param classNode
     * @param attributeName
     * @return
     */
    public static String getTypeOfAttribute(Node classNode, String attributeName){
        //get attribute type
        Iterable<Relationship> attributeRels = classNode.getRelationships(RelTypes.HAS_ATTRIBUTE, Direction.OUTGOING);
        for (Relationship attrRel:  attributeRels) {
                    Node endNode = attrRel.getEndNode();
                    if(attributeName.equals((String)endNode.getProperty(Constants.PROPERTY_NAME))){
                        return (String)endNode.getProperty(Constants.PROPERTY_TYPE);
                    }
        }
        return "";
    }

    /**
     * Evaluates attribute type
     * @param attributeType
     * @param attributeName
     * @param attributeValue
     * @return
     */
    public static Object evalAttributeType(String attributeType, String attributeValue){

        if(attributeType.equals("String"))//NOI18N
            return "(?i)".concat(attributeValue);

        if(attributeType.equals("Date")){//NOI18N
            //the date you are looking for into long
            Long attrbtDate = (long)0;
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);//NOI18N
            try {
                attrbtDate = dateFormat.parse(attributeValue).getTime();
            } catch (ParseException ex) {
                System.out.println("wrong date format should be " + Constants.DATE_FORMAT);//NOI18N
            }
            return attrbtDate;
        }//end if is date
        else if(attributeType.equals("Float")){//NOI18N
            Float attribute = Float.valueOf(attributeValue);
            return attribute;
        }else if(attributeType.equals("Integer")){
            Integer attribute = Integer.valueOf(attributeValue);
            return attribute;
        }else if(attributeType.equals("Boolean")){
            Boolean attribute = Boolean.valueOf(attributeValue);
            return attribute;
        }
        return null;
    }

    /**
     * Gets the class name of a given object given its respective node
     * @param instance the node to be tested
     * @return The object class name. Null if none
     */
    public static String getClassName(Node instance){
        Iterable<Relationship> aClass = instance.getRelationships(RelTypes.INSTANCE_OF, Direction.OUTGOING);
        if (!aClass.iterator().hasNext()){
            return null;
        }
        return (String)aClass.iterator().next().getEndNode().getProperty(Constants.PROPERTY_NAME);
    }
    
    public static boolean isAttributeName(Node node, AttributeMetadata attribute){
        ClassMetadata classMetadata = createClassMetadataFromNode(node);
        List<AttributeMetadata> attributes = classMetadata.getAttributes();
        for (AttributeMetadata attributeMetadata : attributes){
            if(attributeMetadata.getName().equals(attribute.getName())){
                return true;
            }
        }
       return false;
    }
    
    public static void addAttribute(Node classNode, Node attributeNode) throws MetadataObjectNotFoundException{
        Transaction tx = null;
        final TraversalDescription TRAVERSAL = Traversal.description().
                    breadthFirst().
                    relationships(RelTypes.EXTENDS, Direction.INCOMING).
                    relationships(RelTypes.INSTANCE_OF, Direction.INCOMING).
                    evaluator(Evaluators.excludeStartPosition());
        
        for(Path p : TRAVERSAL.traverse(classNode)){
            if(!p.endNode().hasRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING)){
                for (Relationship attrRel: p.endNode().getRelationships(RelTypes.HAS_ATTRIBUTE, Direction.OUTGOING)){
                    if(attrRel.getEndNode().getProperty(Constants.PROPERTY_NAME).equals(
                            attributeNode.getProperty(Constants.PROPERTY_NAME))){
                        throw new MetadataObjectNotFoundException(String.format(
                        "Can not create the attribute, an attribute with the name %1s already exist", 
                        attributeNode.getProperty(Constants.PROPERTY_NAME)));
                    }   
                }
                try{
                    tx = classNode.getGraphDatabase().beginTx();
                    p.endNode().createRelationshipTo(attributeNode, RelTypes.HAS_ATTRIBUTE);
                    tx.success();
                }catch(Exception ex){
                    throw new RuntimeException(ex.getMessage());
                } finally {
                    if (tx != null)
                        tx.finish();
                }
            }
        }//end for
    }
    public static void changeAttributeTypes(Node classNode, Node attributeNode, String oldAttributeName, String oldAttributeType){
        Transaction tx = null;
        final TraversalDescription UPDATE_TRAVERSAL = Traversal.description().
                    breadthFirst().
                    relationships(RelTypes.EXTENDS, Direction.INCOMING).
                    relationships(RelTypes.INSTANCE_OF, Direction.INCOMING).
                    evaluator(Evaluators.excludeStartPosition());
        try{
            tx = classNode.getGraphDatabase().beginTx();
            for(Path p : UPDATE_TRAVERSAL.traverse(classNode)){
        
                if(p.endNode().hasRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING)){
                    if(p.endNode().hasProperty(oldAttributeName)){
                        Object property = p.endNode().getProperty(oldAttributeName);
                        p.endNode().removeProperty(oldAttributeName);
                        if("String".equals(attributeNode.getProperty(Constants.PROPERTY_TYPE)) &&
                           oldAttributeType.equals("Boolean") || oldAttributeType.equals("Long") ||
                           oldAttributeType.equals("Integer") || oldAttributeType.equals("Float"))
                        {
                            p.endNode().setProperty((String)attributeNode.getProperty(Constants.PROPERTY_NAME), property.toString());
                        }
                    }
                }
                else{
                    for (Relationship attrRel: p.endNode().getRelationships(RelTypes.HAS_ATTRIBUTE, Direction.OUTGOING)){
                        
                        if(oldAttributeName.equals(attrRel.getEndNode().getProperty(Constants.PROPERTY_NAME)))
                        {
                            attrRel.getEndNode().delete();
                            attrRel.delete();
                            p.endNode().createRelationshipTo(attributeNode, RelTypes.HAS_ATTRIBUTE);
                        }
                    }
                }
            }//end for
            tx.success();
            }catch(Exception ex){
                Logger.getLogger("Delete attribute: "+ex.getMessage()); //NOI18N
            if (tx != null)
                tx.failure();
                throw new RuntimeException(ex.getMessage());
            } finally {
            if (tx != null)
                tx.finish();
        }
    }
    
    public static void show(Node node){
        String output = "";
        Transaction tx = node.getGraphDatabase().beginTx();
        try{
        final TraversalDescription TRAVERSAL = Traversal.description().
                    breadthFirst().
                    relationships(RelTypes.EXTENDS, Direction.INCOMING).
                    relationships(RelTypes.INSTANCE_OF, Direction.INCOMING).
                    evaluator(Evaluators.all());
            for(Path p : TRAVERSAL.traverse(node)){
                if(p.endNode().hasRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING)){
                    output += "Instance: " ;
                }
                output += p.endNode().getProperty("name") +" id: "+ p.endNode().getId() +"\n";
                for (Relationship attrRel: p.endNode().getRelationships(RelTypes.HAS_ATTRIBUTE, Direction.OUTGOING)){
                    output += "-" + attrRel.getEndNode().getProperty("name") + "\n";
                }
            }
        System.out.println("stop");
        }catch(Exception ex){
            Logger.getLogger("Delete attribute: "+ex.getMessage()); //NOI18N
            if (tx != null)
                tx.failure();
            throw new RuntimeException(ex.getMessage());
        } finally {
            if (tx != null)
                tx.finish();
        }
    }
    
    public static void deleteAttribute(Node node, String attributeName){
        Transaction tx = null;
        final TraversalDescription TRAVERSAL = Traversal.description().
                    breadthFirst().
                    relationships(RelTypes.EXTENDS, Direction.INCOMING).
                    relationships(RelTypes.INSTANCE_OF, Direction.INCOMING).
                    evaluator(Evaluators.excludeStartPosition());
        try{
            tx = node.getGraphDatabase().beginTx();
            for(Path p : TRAVERSAL.traverse(node)){
                if(p.endNode().hasRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING)){
                    p.endNode().getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).delete();
                    p.endNode().delete();
                }
                else{
                    for (Relationship attrRel: p.endNode().getRelationships(RelTypes.HAS_ATTRIBUTE, Direction.OUTGOING)){
                        if(attrRel.getEndNode().getProperty(Constants.PROPERTY_NAME).equals(attributeName)){
                            attrRel.delete();
                            attrRel.getEndNode().delete();
                        }        
                    }
                }
            }//end for
            tx.success();
        }catch(Exception ex){
            Logger.getLogger("Delete attribute: "+ex.getMessage()); //NOI18N
            if (tx != null)
                tx.failure();
            throw new RuntimeException(ex.getMessage());
        } finally {
            if (tx != null)
                tx.finish();
        }
    }
}

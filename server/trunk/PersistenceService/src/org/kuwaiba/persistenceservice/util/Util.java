/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.UnsupportedPropertyException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.GenericObjectList;
import org.kuwaiba.persistenceservice.impl.RelTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;

/**
 * Utility class containing misc methods to perform common tasks
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Util {
    /**
     * Converts a String value to an object value based on a give mapping. This method
     * does not convert binary or relationship-like attributes
     * @param value Value as String
     * @return the converted value
     * @throws InvalidArgumentException If the type can't be converted
     */
    public static Object getRealValue(String value, String type) throws InvalidArgumentException {
        if (value == null)
            return null;
        try{
            if(type.equals("String"))
                return value;
            else
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
                            else
                                if(type.equals("Date"))
                                    return new Date(Long.valueOf(value));
                                else
                                    if(type.equals("Timestamp"))
                                        return Timestamp.valueOf(value);
                                    else
                                        throw new InvalidArgumentException(String.format("Type %s not found",type),Level.WARNING);
        }catch (Exception e){
            throw new InvalidArgumentException(String.format("Can not convert value %s to a type %s", value, type), Level.WARNING);
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
    public static void releaseRelationshipss(Node instance, RelTypes relationshipType,
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
                throw new OperationNotPermittedException("deleteObject",String.format("The object with id %s can not be deleted since it has relationships", instance.getId()));

            if (instance.getRelationships(RelTypes.RELATED_TO_SPECIAL, Direction.OUTGOING).iterator().hasNext())
                throw new OperationNotPermittedException("deleteObject",String.format("The object with id %s can not be deleted since it has relationships", instance.getId()));
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
            throw new MetadataObjectNotFoundException(String.format("The object with id %s does not have a class associated to it", objectNode.getId()));
        
        Node classNode = iterator.next().getEndNode();
        if (!classNode.hasProperty(Constants.PROPERTY_NAME))
            throw new UnsupportedPropertyException(Constants.PROPERTY_NAME);
        return (String)classNode.getProperty(Constants.PROPERTY_NAME);
    }

    /**
     * Creates a ClassMetadata with default values
     * @param classMetadata
     * @return
     */
    public static ClassMetadata setDefaultsForClassMetadatas(ClassMetadata classDefinition) throws MetadataObjectNotFoundException{
        if(classDefinition.getName() == null){
            throw new MetadataObjectNotFoundException("Can not create a class metadata entry without a name");
        }
        if(classDefinition.getDisplayName() == null){
            classDefinition.setDisplayName("");
        }
        if(classDefinition.getDescription() == null){
            classDefinition.setDescription("");
        }
        if(classDefinition.getIcon() == null){
            classDefinition.setIcon(new byte[0]);
        }
        if(classDefinition.getSmallIcon() == null){
            classDefinition.setSmallIcon(new byte[0]);
        }
        try {
            Integer.valueOf(classDefinition.getColor());
        } catch (NumberFormatException e) {
            classDefinition.setColor(0);
        }

        return classDefinition;
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
        myClass.setCustom((Boolean)classNode.getProperty(Constants.PROPERTY_CUSTOM));
        myClass.setInDesign((Boolean)classNode.getProperty(Constants.PROPERTY_IN_DESIGN));
        myClass.setViewable((Boolean)isSubClass(Constants.CLASS_VIEWABLEOBJECT, classNode));
        myClass.setListType((Boolean)isSubClass(Constants.CLASS_GENERICOBJECTLIST, classNode));
        myClass.setId(classNode.getId());
        
        //Parent
        if (classNode.getSingleRelationship(RelTypes.EXTENDS, Direction.OUTGOING) != null){
            myClass.setParentClassName(classNode.getSingleRelationship(
                    RelTypes.EXTENDS, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME).toString());
        }
        else
            myClass.setParentClassName(null);
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
        myClass.setInDesign((Boolean)classNode.getProperty(Constants.PROPERTY_IN_DESIGN));
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
        else
            myClass.setParentClassName(null);
        
        for (Relationship rel : classNode.getRelationships(RelTypes.HAS_ATTRIBUTE))
            listAttributes.add(createAttributeMetadataFromNode(rel.getEndNode()));
        
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
     * @param AttributeNode
     * @return
     */
    public static AttributeMetadata createAttributeMetadataFromNode(Node attributeNode)
    {
        AttributeMetadata attribute =  new AttributeMetadata();

        attribute.setName((String)attributeNode.getProperty(Constants.PROPERTY_NAME));
        attribute.setDescription((String)attributeNode.getProperty(Constants.PROPERTY_DESCRIPTION));
        attribute.setDisplayName((String)attributeNode.getProperty(Constants.PROPERTY_DISPLAY_NAME));
        attribute.setReadOnly((Boolean)attributeNode.getProperty(Constants.PROPERTY_READ_ONLY));
        attribute.setType((String)attributeNode.getProperty(Constants.PROPERTY_TYPE));
        attribute.setVisible((Boolean)attributeNode.getProperty(Constants.PROPERTY_VISIBLE));
        attribute.setAdministrative((Boolean)attributeNode.getProperty(Constants.PROPERTY_ADMINISTRATIVE));
        attribute.setNoCopy((Boolean)attributeNode.getProperty(Constants.PROPERTY_NO_COPY));
        attribute.setUnique((Boolean)attributeNode.getProperty(Constants.PROPERTY_UNIQUE));
        attribute.setId(attributeNode.getId());

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
               if (AttributeMetadata.isPrimitive(myAtt.getType())) {
                   if (!myAtt.getType().equals("Binary")) {
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
                throw new InvalidArgumentException(String.format("The object with id %s is malformed", instance.getId()), Level.SEVERE);

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
                groupNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? 
                        (String)groupNode.getProperty(Constants.PROPERTY_DESCRIPTION) : "",
                (Long)groupNode.getProperty(Constants.PROPERTY_CREATION_DATE),
                null,
                null);
        group.setUsers(users);
        return group;
    }
    
    /**
     * Creates a generic object list (a list type) from a node
     * @param listTypeNode the list type node
     * @return a list type
     */
    public static GenericObjectList createGenericObjectListFromNode(Node listTypeNode){
        GenericObjectList listType = new GenericObjectList(listTypeNode.getId(), 
                (String)listTypeNode.getProperty(Constants.PROPERTY_NAME));
        return listType;
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

    public static Traverser getAllSubclasses(final Node classMetadata)
    {
        return classMetadata.traverse(Order.BREADTH_FIRST,
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
            if(attributeName.equals((String)endNode.getProperty(Constants.PROPERTY_NAME)))
                return (String)endNode.getProperty(Constants.PROPERTY_TYPE);
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
                System.out.println("Wrong date format. It should be " + Constants.DATE_FORMAT);//NOI18N
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
        if (!aClass.iterator().hasNext())
            return null;
        return (String)aClass.iterator().next().getEndNode().getProperty(Constants.PROPERTY_NAME);
    }
       
    public static void createAttribute(Node classNode, AttributeMetadata attributeDefinition) throws InvalidArgumentException{
            final TraversalDescription UPDATE_TRAVERSAL = Traversal.description().
                    breadthFirst().
                    relationships(RelTypes.EXTENDS, Direction.INCOMING);

        for(Path p : UPDATE_TRAVERSAL.traverse(classNode)){
            String currentClassName = (String) p.endNode().getProperty(Constants.PROPERTY_NAME);
            for(Relationship rel : p.endNode().getRelationships(RelTypes.HAS_ATTRIBUTE)){
                if (rel.getEndNode().getProperty(Constants.PROPERTY_NAME).equals(attributeDefinition.getName()))
                    throw new InvalidArgumentException(String.format("Class %s already has an attribute named %s", 
                            currentClassName, attributeDefinition.getName()), Level.INFO);
                
            }
            
            Node attrNode = classNode.getGraphDatabase().createNode();
            attrNode.setProperty(Constants.PROPERTY_NAME, attributeDefinition.getName()); //This should not be null. That should be checked in the caller
            attrNode.setProperty(Constants.PROPERTY_DESCRIPTION, attributeDefinition.getDescription() ==  null ? "" : attributeDefinition.getDescription());
            attrNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, attributeDefinition.getDisplayName() == null ? "" : attributeDefinition.getDisplayName());
            attrNode.setProperty(Constants.PROPERTY_TYPE, attributeDefinition.getType() == null ? "String" : attributeDefinition.getType());
            attrNode.setProperty(Constants.PROPERTY_READ_ONLY, attributeDefinition.isReadOnly() == null ? false : attributeDefinition.isReadOnly());
            attrNode.setProperty(Constants.PROPERTY_VISIBLE, attributeDefinition.isVisible() == null ? true : attributeDefinition.isVisible());
            attrNode.setProperty(Constants.PROPERTY_ADMINISTRATIVE, attributeDefinition.isAdministrative() == null ? false : attributeDefinition.isAdministrative());
            attrNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            attrNode.setProperty(Constants.PROPERTY_NO_COPY, attributeDefinition.isNoCopy() == null ? false : attributeDefinition.isNoCopy());
            attrNode.setProperty(Constants.PROPERTY_UNIQUE, attributeDefinition.isUnique() == null ? false : attributeDefinition.isUnique());

            p.endNode().createRelationshipTo(attrNode, RelTypes.HAS_ATTRIBUTE);
        }
    }
    
    /**
     * Transactions are not handled here
     * @param classNode
     * @param attributeName
     * @param attributeType 
     */
    public static void changeAttributeTypeIfPrimitive (Node classNode, String attributeName, String newAttributeType) throws InvalidArgumentException {
        final TraversalDescription UPDATE_TRAVERSAL = Traversal.description().
                    breadthFirst().
                    relationships(RelTypes.EXTENDS, Direction.INCOMING);

        for(Path p : UPDATE_TRAVERSAL.traverse(classNode)){
            for(Relationship rel : p.endNode().getRelationships(RelTypes.HAS_ATTRIBUTE)){
                if (rel.getEndNode().getProperty(Constants.PROPERTY_NAME).equals(attributeName)){
                    rel.getEndNode().setProperty(Constants.PROPERTY_TYPE, newAttributeType);
                    break;
                }
            }
            
            for(Relationship rel : p.endNode().getRelationships(RelTypes.INSTANCE_OF, Direction.INCOMING)){
                if(rel.getStartNode().hasProperty(attributeName)){
                    Object currentValue = rel.getStartNode().getProperty(attributeName);                   
                    Object newValue = Util.convertIfPossible(currentValue, newAttributeType);
                    if (newValue != null)
                        rel.getStartNode().setProperty(attributeName, newValue);
                    else
                        rel.getStartNode().removeProperty(attributeName);
                }
            }
        }//end for
    }
    
    public static void changeAttributeTypeIfListType (Node classNode, String attributeName, String newAttributeType) throws InvalidArgumentException {
        final TraversalDescription UPDATE_TRAVERSAL = Traversal.description().
                    breadthFirst().
                    relationships(RelTypes.EXTENDS, Direction.INCOMING);

        for(Path p : UPDATE_TRAVERSAL.traverse(classNode)){
            for(Relationship rel : p.endNode().getRelationships(RelTypes.HAS_ATTRIBUTE)){
                if (rel.getEndNode().getProperty(Constants.PROPERTY_NAME).equals(attributeName)){
                    rel.getEndNode().setProperty(Constants.PROPERTY_TYPE, newAttributeType);
                    break;
                }
            }
            
            for(Relationship rel : p.endNode().getRelationships(RelTypes.INSTANCE_OF, Direction.INCOMING)){
                for(Relationship listTypeRel : rel.getStartNode().getRelationships(Direction.OUTGOING, RelTypes.RELATED_TO, RelTypes.RELATED_TO_SPECIAL)){
                    if (listTypeRel.getProperty(Constants.PROPERTY_NAME).equals(attributeName))
                        listTypeRel.delete();
                }
            }
        }//end for
    }
    
    public static void changeAttributeProperty (Node classNode, String attributeName, String propertyName, Object propertyValue) throws InvalidArgumentException {
        final TraversalDescription UPDATE_TRAVERSAL = Traversal.description().
                    breadthFirst().
                    relationships(RelTypes.EXTENDS, Direction.INCOMING);

        for(Path p : UPDATE_TRAVERSAL.traverse(classNode)){
            for(Relationship rel : p.endNode().getRelationships(RelTypes.HAS_ATTRIBUTE)) {
                if (rel.getEndNode().getProperty(Constants.PROPERTY_NAME).equals(attributeName)){
                    rel.getEndNode().setProperty(propertyName, propertyValue);
                    break;
                }
            }
        }//end for
    }
    
    /**
     * 
     * @param classNode
     * @param oldAttributeName
     * @param newAttributeName
     * @throws InvalidArgumentException 
     */
    public static void changeAttributeName(Node classNode, String oldAttributeName, String newAttributeName) {
        final TraversalDescription UPDATE_TRAVERSAL = Traversal.description().
                    breadthFirst().
                    relationships(RelTypes.EXTENDS, Direction.INCOMING);

        for(Path p : UPDATE_TRAVERSAL.traverse(classNode)){
            for(Relationship rel : p.endNode().getRelationships(RelTypes.HAS_ATTRIBUTE)) {
                if (rel.getEndNode().getProperty(Constants.PROPERTY_NAME).equals(oldAttributeName)){
                    rel.getEndNode().setProperty(Constants.PROPERTY_NAME, newAttributeName);
                    break;
                }
            }
            
            for(Relationship rel : p.endNode().getRelationships(RelTypes.INSTANCE_OF, Direction.INCOMING)){
                if(rel.getStartNode().hasProperty(oldAttributeName)){
                    Object currentValue = rel.getStartNode().getProperty(oldAttributeName);
                    rel.getStartNode().removeProperty(oldAttributeName);
                    rel.getStartNode().setProperty(newAttributeName, currentValue);
                }
            }           
        }//end for
    }
    
    public static void deleteAttributeIfPrimitive(Node classNode, String attributeName){
        final TraversalDescription TRAVERSAL = Traversal.description().
                    breadthFirst().relationships(RelTypes.EXTENDS, Direction.INCOMING);
        
        for(Path p : TRAVERSAL.traverse(classNode)){
            for(Relationship rel : p.endNode().getRelationships(RelTypes.HAS_ATTRIBUTE)) {
                if (rel.getEndNode().getProperty(Constants.PROPERTY_NAME).equals(attributeName)){
                    rel.getEndNode().delete();
                    rel.delete();
                    break;
                }
            }
            
            for(Relationship rel : p.endNode().getRelationships(RelTypes.INSTANCE_OF, Direction.INCOMING)){
                if(rel.getStartNode().hasProperty(attributeName))
                    rel.getStartNode().removeProperty(attributeName);
            }           
        }//end for
    }
    
    public static void deleteAttributeIfListType(Node classNode, String attributeName){
        final TraversalDescription TRAVERSAL = Traversal.description().
                    breadthFirst().relationships(RelTypes.EXTENDS, Direction.INCOMING);
        
        for(Path p : TRAVERSAL.traverse(classNode)){
            for(Relationship rel : p.endNode().getRelationships(RelTypes.HAS_ATTRIBUTE)) {
                if (rel.getEndNode().getProperty(Constants.PROPERTY_NAME).equals(attributeName)){
                    rel.getEndNode().delete();
                    rel.delete();
                    break;
                }
            }
            
            for(Relationship rel : p.endNode().getRelationships(RelTypes.INSTANCE_OF, Direction.INCOMING)){
                for (Relationship relatedElement : rel.getStartNode().getRelationships(Direction.OUTGOING, RelTypes.RELATED_TO, RelTypes.RELATED_TO_SPECIAL)){
                    if(relatedElement.getProperty(Constants.PROPERTY_NAME).equals(attributeName))
                        relatedElement.delete();
                }
            }
        }//end for
    }
    
    /**
     * Creates a new log entry upon an action performed by an user. Transactions are not managed here
     * @param object The object that was affected by the action. Provide the db's root node if it's a general activity log entry (that is, it's not related to any specific object)
     * @param user User that performed the action
     * @param logEntry The information to be logged
     * @throws ApplicationObjectNotFoundException If the user or the root of all log entries can't be found
     */
    public static Node createActivityLogEntry(Node object, Node logRoot, String userName, 
            int type, long timestamp, String affectedProperty, String oldValue, String newValue, String notes) throws ApplicationObjectNotFoundException {
        
        Node userNode = logRoot.getGraphDatabase().index().forNodes(Constants.INDEX_USERS).get(Constants.PROPERTY_NAME, userName).getSingle();
        
        if (userNode == null)
            throw new ApplicationObjectNotFoundException(String.format("User %s can not be found", userName));
        
        Node newEntry = logRoot.getGraphDatabase().createNode();
        
        newEntry.setProperty(Constants.PROPERTY_TYPE, type);
        newEntry.setProperty(Constants.PROPERTY_CREATION_DATE, timestamp);
        if (affectedProperty != null)
            newEntry.setProperty(Constants.PROPERTY_AFFECTED_PROPERTY, affectedProperty);
        if (oldValue != null)
            newEntry.setProperty(Constants.PROPERTY_OLD_VALUE, oldValue);
        if (newValue != null)
            newEntry.setProperty(Constants.PROPERTY_NEW_VALUE, newValue);
        if (notes != null)
            newEntry.setProperty(Constants.PROPERTY_NOTES, notes);
        
        newEntry.createRelationshipTo(logRoot, RelTypes.CHILD_OF_SPECIAL);
        newEntry.createRelationshipTo(userNode, RelTypes.PERFORMED_BY);
        if (object != null)
            object.createRelationshipTo(newEntry, RelTypes.HAS_HISTORY_ENTRY);
        return newEntry;
    }
    
    /**
     * Tries to convert an attribute value to a new attribute type. It only works with primitive types String, Integer, Float, Boolean, Long, Date and Timestamp
     * @param oldValue The old value
     * @param convertTo The type we want to convert the old value to
     * @return The converted value
     * @throws InvalidArgumentException If it's not possible to perform the conversion
     */
    public static Object convertIfPossible(Object oldValue, String convertTo) throws InvalidArgumentException {
        if (oldValue == null)
            return null;
        
        String easierToHandleOldValue = oldValue.toString();
        if (convertTo.equals("String"))
            return easierToHandleOldValue;
        try {
            if (convertTo.equals("Integer"))
                return Integer.valueOf(easierToHandleOldValue);
            if (convertTo.equals("Float"))
                return Float.valueOf(easierToHandleOldValue);
            if (convertTo.equals("Boolean"))
                return Boolean.valueOf(easierToHandleOldValue);
            if (convertTo.equals("Long") || convertTo.equals("Date") || convertTo.equals("Timestamp"))
                return Integer.valueOf(easierToHandleOldValue);
        }catch (NumberFormatException ex){} //Does nothing
        
        return null;
    }
}

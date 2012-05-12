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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.persistenceservice.impl.BusinessEntityManagerImpl;
import org.kuwaiba.persistenceservice.impl.MetadataEntityManagerImpl;
import org.kuwaiba.persistenceservice.impl.enumerations.RelTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;

/**
 * Utility class containing misc methods to perform common tasks
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Util {

    /**
     * General purpose Formatter
     */
    private static Formatter formatter;
    
     /**
     * Gets an attribute type by traversing through the "HAS" relationship of a given class metadata node
     * @param classNode
     * @param attributeName
     * @return attribute's type. 0 if it can't find the attribute
     */
    public static int getTypeOfAttribute (Node classNode, String attributeName){
        Iterable<Relationship> attributes = classNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
        while (attributes.iterator().hasNext()){
            Relationship rel = attributes.iterator().next();
            if (rel.getEndNode().getProperty(MetadataEntityManagerImpl.PROPERTY_NAME).equals(attributeName))
                return Integer.valueOf(rel.getEndNode().getProperty(MetadataEntityManagerImpl.PROPERTY_TYPE).toString());
        }
        return 0;
    }

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
                    throw new InvalidArgumentException(formatString("Can not convert value %1s to a typ %2s", value, type), Level.WARNING);
            }

        }catch (Exception e){
            throw new InvalidArgumentException(formatString("Can not convert value %1s to a typ %2s", value, type), Level.WARNING);
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
     * Releases all relationships related to an object given its direction and a relationsship's property value
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
        if(!releaseAll)
            if (instance.getRelationships(RelTypes.RELATED_TO, Direction.INCOMING).iterator().hasNext())
                throw new OperationNotPermittedException("deleteObject",Util.formatString("The object with id %1s can not be deleted since it has relationships", instance.getId()));

        for (Relationship rel : instance.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING))
            deleteObject(rel.getStartNode(), releaseAll);

        for (Relationship rel : instance.getRelationships())
            rel.delete();

        instance.getGraphDatabase().index().forNodes(BusinessEntityManagerImpl.INDEX_OBJECTS).remove(instance);
        instance.delete();
    }

    public static Node copyObject(Node templateObject, Node newParentNode, boolean recursive, GraphDatabaseService graphDb) {
        Node newInstance = graphDb.createNode();
        for (String property : templateObject.getPropertyKeys())
            newInstance.setProperty(property, templateObject.getProperty(property));
        for (Relationship rel : templateObject.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING))
            newInstance.createRelationshipTo(rel.getEndNode(), RelTypes.RELATED_TO).setProperty(MetadataEntityManagerImpl.PROPERTY_NAME, rel.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME));

        newInstance.createRelationshipTo(templateObject.getRelationships(RelTypes.INSTANCE_OF).iterator().next().getEndNode(), RelTypes.INSTANCE_OF);
        newInstance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF);
        if (recursive){
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF, Direction.INCOMING))
                copyObject(rel.getStartNode(), newInstance, true, graphDb);
        }
        return newInstance;
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
    public static ClassMetadata createDefaultClassMetadata(ClassMetadata classDefinition) throws MetadataObjectNotFoundException{

        Integer color = null;

        if(classDefinition.getName() == null)
            throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not create a ClassMetada without a name"));

        if(classDefinition.getDisplayName() == null)
            classDefinition.setDisplayName("");

        if(classDefinition.getDescription() == null)
            classDefinition.setDisplayName("");

        if(classDefinition.getIcon() == null)
            classDefinition.setIcon(new byte[0]);

        if(classDefinition.getSmallIcon() == null)
            classDefinition.setSmallIcon(new byte[0]);

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

        if(AttributeDefinition.getName() == null)
            throw new MetadataObjectNotFoundException(Util.formatString(
                         "Can not create an attribute without a name"));

        if(AttributeDefinition.getDisplayName() == null)
            AttributeDefinition.setDisplayName("");

        if(AttributeDefinition.getDescription() == null)
            AttributeDefinition.setDisplayName("");

        if(AttributeDefinition.getType() == null)
            AttributeDefinition.setType("");

        return AttributeDefinition;
    }

    /**
     * Converts a class metadata node into a ClassMetadataLight object
     * @param classNode
     * @return
     */
    public static ClassMetadataLight createClassMetadataLightFromNode(Node classNode)
    {
        ClassMetadataLight myClass = new ClassMetadataLight(classNode.getId(),(String)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME),(String)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_DISPLAY_NAME));
        
        myClass.setAbstractClass((Boolean)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_ABSTRACT));
        myClass.setSmallIcon((byte[])classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_SMALL_ICON));
        myClass.setViewable((Boolean)isSubClass(MetadataEntityManagerImpl.VIEWABLE_OBJECT, classNode));
        myClass.setId(classNode.getId());
        
        //Parent
        if (classNode.getSingleRelationship(RelTypes.EXTENDS, Direction.OUTGOING) != null)
            myClass.setParentClassName(
                    classNode.getSingleRelationship(
                        RelTypes.EXTENDS, Direction.OUTGOING).getEndNode().getProperty(
                            MetadataEntityManagerImpl.PROPERTY_NAME).toString());
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

        myClass.setName((String)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME));
        myClass.setAbstractClass((Boolean)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_ABSTRACT));
        myClass.setColor((Integer)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_COLOR));
        myClass.setCountable((Boolean)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_COUNTABLE));
        myClass.setCustom((Boolean)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_CUSTOM));
        myClass.setDescription((String)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_DESCRIPTION));
        myClass.setDisplayName((String)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_DISPLAY_NAME));
        myClass.setIcon((byte[])classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_ICON));
        myClass.setSmallIcon((byte[])classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_SMALL_ICON));
        myClass.setId(classNode.getId());
        myClass.setListType(isSubClass(MetadataEntityManagerImpl.LIST_TYPE, classNode));
        //Is Viewable if is subclass of
        myClass.setViewable((Boolean)isSubClass(MetadataEntityManagerImpl.VIEWABLE_OBJECT, classNode));
        //Parent
        if (classNode.getSingleRelationship(RelTypes.EXTENDS, Direction.OUTGOING) != null)
            myClass.setParentClassName(
                    classNode.getSingleRelationship(
                        RelTypes.EXTENDS, Direction.OUTGOING).getEndNode().getProperty(
                            MetadataEntityManagerImpl.PROPERTY_NAME).toString());
        else
            myClass.setParentClassName(null);
        //Attributes
        Iterable<Relationship> attributes = classNode.getRelationships(RelTypes.HAS_ATTRIBUTE);
        while (attributes.iterator().hasNext()){
            Node attributeNode = attributes.iterator().next().getEndNode();
            listAttributes.add(createAttributeMetadataFromNode(attributeNode));
        }
        myClass.setAttributes(listAttributes);

        //Category
        if(classNode.getSingleRelationship(RelTypes.BELONGS_TO_GROUP, Direction.BOTH) != null)
        {
            ctgr.setName((String)classNode.getSingleRelationship(RelTypes.BELONGS_TO_GROUP, Direction.BOTH).getEndNode().getProperty(MetadataEntityManagerImpl.PROPERTY_NAME));
            ctgr.setDisplayName((String)classNode.getSingleRelationship(RelTypes.BELONGS_TO_GROUP, Direction.BOTH).getEndNode().getProperty(MetadataEntityManagerImpl.PROPERTY_DISPLAY_NAME));
            ctgr.setDescription((String)classNode.getSingleRelationship(RelTypes.BELONGS_TO_GROUP, Direction.BOTH).getEndNode().getProperty(MetadataEntityManagerImpl.PROPERTY_DESCRIPTION));

            myClass.setCategory(ctgr);
        }

        //Possible Children
        for (Relationship rel : classNode.getRelationships(Direction.OUTGOING, RelTypes.POSSIBLE_CHILD))
        {

            if((Boolean)rel.getEndNode().getProperty(MetadataEntityManagerImpl.PROPERTY_ABSTRACT)){
                Traverser traverserMetadata = Util.getAllSubclasses(rel.getEndNode());
                for (Node childNode : traverserMetadata) {
                    if(!(Boolean)childNode.getProperty(MetadataEntityManagerImpl.PROPERTY_ABSTRACT))
                        myClass.getPossibleChildren().add((String)childNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME));
                }//end for
            }//end if
            else
                myClass.getPossibleChildren().add((String)rel.getEndNode().getProperty(MetadataEntityManagerImpl.PROPERTY_NAME));
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
            attribute.setName((String)AttibuteNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME));
            attribute.setDescription((String)AttibuteNode.getProperty(MetadataEntityManagerImpl.PROPERTY_DESCRIPTION));
            attribute.setDisplayName((String)AttibuteNode.getProperty(MetadataEntityManagerImpl.PROPERTY_DISPLAY_NAME));
            attribute.setMapping((Integer)AttibuteNode.getProperty(MetadataEntityManagerImpl.PROPERTY_MAPPING));
            attribute.setReadOnly((Boolean)AttibuteNode.getProperty(MetadataEntityManagerImpl.PROPERTY_READONLY));
            attribute.setType((String)AttibuteNode.getProperty(MetadataEntityManagerImpl.PROPERTY_TYPE));
            attribute.setVisible((Boolean)AttibuteNode.getProperty(MetadataEntityManagerImpl.PROPERTY_VISIBLE));
            attribute.setAdministrative((Boolean)AttibuteNode.getProperty(MetadataEntityManagerImpl.PROPERTY_ADMINISTRATIVE));
            attribute.setNoCopy((Boolean)AttibuteNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NO_COPY));
            attribute.setNoSerialize((Boolean)AttibuteNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NO_SERIALIZE));
            attribute.setUnique((Boolean)AttibuteNode.getProperty(MetadataEntityManagerImpl.PROPERTY_UNIQUE));
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
            if (!relationship.hasProperty(MetadataEntityManagerImpl.PROPERTY_NAME))
                throw new InvalidArgumentException(Util.formatString("The object with id %1s is malformed", instance.getId()), Level.SEVERE);

            String attributeName = (String)relationship.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME);
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
                        (String)groupNode.getProperty(GroupProfile.PROPERTY_GROUPNAME),
                        (String)groupNode.getProperty(GroupProfile.PROPERTY_DESCRIPTION),
                        (Long)groupNode.getProperty(GroupProfile.PROPERTY_CREATION_DATE))
                     );
        }

       UserProfile user =  new UserProfile(
                userNode.getId(),
                (String)userNode.getProperty(UserProfile.PROPERTY_USERNAME),
                (String)userNode.getProperty(UserProfile.PROPERTY_FIRST_NAME),
                (String)userNode.getProperty(UserProfile.PROPERTY_LAST_NAME),
                (Boolean)userNode.getProperty(UserProfile.PROPERTY_ENABLED),
                (Long)userNode.getProperty(UserProfile.PROPERTY_CREATION_DATE),
                null);

       user.setGroups(groups);

        return user;
    }

    /**
     * Converts a node representing a group into a GroupProfile object
     * @param groupNode
     * @return
     */
    public static GroupProfile createGroupProfileFromNode(Node groupNode)
    {
        Iterable<Relationship> relationships = groupNode.getRelationships(RelTypes.BELONGS_TO_GROUP, Direction.INCOMING);
        List<UserProfile> users = new ArrayList<UserProfile>();

        for (Relationship relationship : relationships) {
            Node userNode = relationship.getStartNode();
            users.add(new UserProfile(userNode.getId(),
                        (String)userNode.getProperty(UserProfile.PROPERTY_USERNAME),
                        (String)userNode.getProperty(UserProfile.PROPERTY_FIRST_NAME),
                        (String)userNode.getProperty(UserProfile.PROPERTY_LAST_NAME),
                        (Boolean)userNode.getProperty(UserProfile.PROPERTY_ENABLED),
                        (Long)userNode.getProperty(UserProfile.PROPERTY_CREATION_DATE),
                        null)
                     );
        }

        GroupProfile group =  new GroupProfile(
                groupNode.getId(),
                (String)groupNode.getProperty(GroupProfile.PROPERTY_GROUPNAME),
                (String)groupNode.getProperty(GroupProfile.PROPERTY_DESCRIPTION),
                (Long)groupNode.getProperty(GroupProfile.PROPERTY_CREATION_DATE),
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

        if (parentNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME).equals(allegedParentClass))
            return true;

        return isSubClass(allegedParentClass, parentNode);
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
     * Formats a String. It's basically a wrapper for Formatter.format() method
     * @param stringToFormat String to be formatted
     * @param args a variable set of arguments to be used with the formatter
     * @return The resulting string of merging @stringToFormat with @args
     */
    public static String formatString(String stringToFormat,Object ... args){
        if (formatter == null)
            formatter = new Formatter();
        return formatter.format(stringToFormat, args).toString();
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
}

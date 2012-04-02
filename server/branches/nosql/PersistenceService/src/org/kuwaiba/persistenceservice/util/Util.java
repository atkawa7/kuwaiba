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
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.business.RemoteObject;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.persistenceservice.impl.MetadataEntityManagerImpl;
import org.kuwaiba.persistenceservice.impl.enumerations.RelTypes;
import org.neo4j.graphdb.Direction;
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
        Iterable<Relationship> attributes = classNode.getRelationships(RelTypes.HAS);
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
                    throw new InvalidArgumentException("Can not retrieve the correct value for ("+
                            value+" "+type+"). Please check your mappings", Level.WARNING);
            }

        }catch (Exception e){
            throw new InvalidArgumentException("Can not retrieve the correct value for ("+
                            value+" "+type+"). Please check your mappings", Level.WARNING);
        }
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
                         "Can not create a AttributeMetada without a name"));

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
        myClass.setLocked((Boolean)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_LOCKED));
        myClass.setViewable(true);
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
        myClass.setListType(isSubClass("GenericListType", classNode));
        myClass.setLocked((Boolean)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_LOCKED));
        myClass.setLocked((Boolean)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_VIEWABLE));
        //Parent
        if (classNode.getSingleRelationship(RelTypes.EXTENDS, Direction.OUTGOING) != null)
            myClass.setParentClassName(
                    classNode.getSingleRelationship(
                        RelTypes.EXTENDS, Direction.OUTGOING).getEndNode().getProperty(
                            MetadataEntityManagerImpl.PROPERTY_NAME).toString());
        else
            myClass.setParentClassName(null);
        //Attributes
        Iterable<Relationship> attributes = classNode.getRelationships(RelTypes.HAS);
        while (attributes.iterator().hasNext()){
            Node attributeNode = attributes.iterator().next().getEndNode();
            AttributeMetadata attribute = new AttributeMetadata();
            attribute.setName(attributeNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME).toString());
            attribute.setAdministrative((Boolean)attributeNode.getProperty(MetadataEntityManagerImpl.PROPERTY_ADMINISTRATIVE));
            attribute.setDescription((String)attributeNode.getProperty(MetadataEntityManagerImpl.PROPERTY_DESCRIPTION));
            attribute.setDisplayName((String)attributeNode.getProperty(MetadataEntityManagerImpl.PROPERTY_DISPLAY_NAME));
            attribute.setMapping((Integer)attributeNode.getProperty(MetadataEntityManagerImpl.PROPERTY_MAPPING));
            attribute.setReadOnly((Boolean)attributeNode.getProperty(MetadataEntityManagerImpl.PROPERTY_READONLY));
            attribute.setType((String)attributeNode.getProperty(MetadataEntityManagerImpl.PROPERTY_TYPE));
            attribute.setVisible((Boolean)attributeNode.getProperty(MetadataEntityManagerImpl.PROPERTY_VISIBLE));

            listAttributes.add(attribute);
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
        else
            myClass.setCategory(null);

        Iterable<Relationship> possibleChildren = classNode.getRelationships(RelTypes.POSSIBLE_CHILD);
        while (possibleChildren.iterator().hasNext()){
            Relationship possibleChild = possibleChildren.iterator().next();
            myClass.getPossibleChildren().add((String)possibleChild.getStartNode().getProperty(MetadataEntityManagerImpl.PROPERTY_NAME));
        }

        //IsDummy
        if(classNode.getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.BOTH) != null)
            myClass.setDummy(false);
        else
            myClass.setDummy(true);
        
        return myClass;
    }


    /**
     * Converts a atttribute metadata node into a AttrributeMetadata object
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
        }catch(Exception e){
            return null;
        }

        return attribute;
    }

    
    /**
     * Builds a RemoteObject instance from a node representing a business object
     * @param instance
     * @param myClass
     * @return
     * @throws InvalidArgumentException
     */
    public static RemoteObject createRemoteObjectFromNode(Node instance, ClassMetadata myClass) throws InvalidArgumentException{
        HashMap<String, List<String>> attributes = new HashMap<String, List<String>>();

                //Iterates through attributes
                Iterable<String> attributeNames = instance.getPropertyKeys();
                while (attributeNames.iterator().hasNext()){
                    String attributeName = attributeNames.iterator().next();
                    List<String> attributeValue = null;
                    if (instance.getProperty(attributeName) != null ){
                        try {
                            if (myClass.getAttributeMapping(attributeName) != AttributeMetadata.MAPPING_BINARY) {
                                attributeValue = new ArrayList<String>();
                                attributeValue.add(instance.getProperty(attributeName).toString());
                            }
                        } catch (InvalidArgumentException ex) { //This should never happen
                            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    attributes.put(attributeName,attributeValue);
                }

                //Iterates through relationships and transform the into "plain" attributes
                Iterable<Relationship> relationships = instance.getRelationships(RelTypes.RELATED_TO);
                while(relationships.iterator().hasNext()){
                    Relationship relationship = relationships.iterator().next();
                    String attributeName = relationship.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME).toString();
                    if (attributes.get(attributeName)==null)
                        attributes.put(attributeName, new ArrayList<String>());

                    attributes.get(attributeName).add(String.valueOf(relationship.getEndNode().getId()));

                }
                return new RemoteObject(instance.getId(), myClass.getName());
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
     * Retrieves the posible children of a classMetadata
     * @param ClassMetadata
     * @return
     */

    public static Traverser traverserPossibleChildren(final Node ClassMetadata)
    {
        return ClassMetadata.traverse(Order.BREADTH_FIRST,
                StopEvaluator.END_OF_GRAPH,
                ReturnableEvaluator.ALL_BUT_START_NODE, RelTypes.POSSIBLE_CHILD,
                Direction.OUTGOING);
    }

    /**
     * Retrieves the children of a given class metadata node within the class hierarchy
     * @param ClassMetadata
     * @return
     */

    public static Traverser traverserMetadata(final Node ClassMetadata)
    {
        return ClassMetadata.traverse(Order.BREADTH_FIRST,
                StopEvaluator.END_OF_GRAPH,
                ReturnableEvaluator.ALL_BUT_START_NODE, RelTypes.EXTENDS,
                Direction.INCOMING);
    }
}

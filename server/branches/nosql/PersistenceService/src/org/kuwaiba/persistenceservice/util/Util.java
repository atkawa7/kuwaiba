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
import java.util.Formatter;
import java.util.logging.Level;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.persistenceservice.impl.MetadataEntityManagerImpl;
import org.kuwaiba.persistenceservice.impl.enumerations.RelTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

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
     * Converts a class metadata node into a ClassMetadata object
     * @param classNode
     * @return
     */
    public static ClassMetadata createMetadataFromNode(Node classNode) {
        ClassMetadata myClass = new ClassMetadata();
        myClass.setName((String)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME));
        myClass.setAbstractClass((Boolean)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_ABSTRACT));
        myClass.setColor((Integer)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_COLOR));
        myClass.setCountable((Boolean)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_COUNTABLE));
        myClass.setCustom((Boolean)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_CUSTOM));
        myClass.setDescription((String)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_DESCRIPTION));
        myClass.setDisplayName((String)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_DISPLAY_NAME));
        myClass.setDummy((Boolean)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_DUMMY));
        myClass.setIcon((byte[])classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_ICON));
        myClass.setSmallIcon((byte[])classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_SMALL_ICON));
        myClass.setId(classNode.getId());
        myClass.setListType(isSubClass("GenericListType", classNode));
        myClass.setLocked((Boolean)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_LOCKED));
        if (classNode.getRelationships(RelTypes.EXTENDS).iterator().hasNext())
            myClass.setParentClassName(
                    classNode.getRelationships(RelTypes.EXTENDS).
                    iterator().next().getEndNode().getProperty(
                        MetadataEntityManagerImpl.PROPERTY_NAME).toString());
        else
            myClass.setParentClassName(null);

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
        }

        return myClass;
    }

    /**
     * Traverses the graph up into the class hierarchy trying to find out if a given class
     * is the subclass of another
     * @param allegedParentClass The alleged parent class name
     * @param startNode Class metadata node corresponding to the child class
     * @return
     */
    public static boolean isSubClass(String allegedParentClass, Node startNode){
        Iterable<Relationship> parent = startNode.getRelationships(RelTypes.EXTENDS, Direction.OUTGOING);
        if (!parent.iterator().hasNext())
            return false;

        Node currentNode = parent.iterator().next().getEndNode();

        if (currentNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME).equals(allegedParentClass))
            return true;

        return isSubClass(allegedParentClass, currentNode);
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
     * Formats 
     * @param stringToFormat
     * @param args a variable set of arguments to be used with the formatter
     * @return The resulting string of merging @stringToFormat with @args
     */
    public static String formatString(String stringToFormat,Object ... args){
        if (formatter == null)
            formatter = new Formatter();
        return formatter.format(stringToFormat, args).toString();
    }
}

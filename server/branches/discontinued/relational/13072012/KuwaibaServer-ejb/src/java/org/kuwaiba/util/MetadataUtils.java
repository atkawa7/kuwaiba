/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.kuwaiba.util;

import org.kuwaiba.core.annotations.NoCopy;
import org.kuwaiba.ws.todeserialize.TransientQuery;
import org.kuwaiba.ws.toserialize.ObjectList;
import org.kuwaiba.ws.toserialize.RemoteObject;
import org.kuwaiba.entity.core.RootObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Transient;


/**
 * Class for manipulating method and class names and stuff related to filtering data
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class MetadataUtils {

    /**
     * Retrieves recursively through the class hierarchy the attributes of a given class. this means it will include even those inherited
     * @param aClass The class to be tested
     * @param includePrivate should the private attribute be included?
     * @return A list with the protected attributes
     */
    public static List<Field> getAllFields(Class<?> aClass, boolean includePrivate){
        List<Field> myAtts = new ArrayList<Field>();
        for (Field f : aClass.getDeclaredFields()){
            boolean showPrivate = includePrivate && Modifier.isPrivate(f.getModifiers());
            if ((showPrivate || Modifier.isProtected(f.getModifiers()))
                    && !Modifier.isTransient(f.getModifiers())
                    && !Modifier.isFinal(f.getModifiers())
                    && f.getAnnotation(Transient.class) == null) //This last has to do with the introduction since EclipseLink 2.1.0
                                                                 //AttributeGroups http://wiki.eclipse.org/EclipseLink/Examples/JPA/AttributeGroup
                myAtts.add(f);
        }
        if (aClass != RootObject.class && aClass.getSuperclass() != null && aClass != Object.class)
            myAtts.addAll(getAllFields(aClass.getSuperclass(), includePrivate));
        return myAtts;
    }

    /*
     * Useful to simulate the getters
     */
    public static String capitalize(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /*
     * This method clones an object for a copy operation. It makes a deep copy of the object
     * Thanks to Jim Ferrans for this code
     * TODO read all @NoCopy fields and reset them!
     */
    public static Object clone(Object obj){
      try
        {
                ObjectOutputStream oos = null;
                ObjectInputStream ois = null;
                try
                {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        oos = new ObjectOutputStream(bos);
                        oos.writeObject(obj);
                        oos.flush();
                        ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
                        return ois.readObject();
                }
                finally
                {
                        oos.close();
                        ois.close();
                }
        }
        catch ( ClassNotFoundException cnfe )
        {
                // Impossible, since both sides deal in the same loaded classes.
                return null;
        }
        catch ( IOException ioe )
        {
                // This has to be "impossible", given that oos and ois wrap a *byte array*.
                return null;
        }

    }

    public static Object clone(RemoteObject clonnable, Class objectClass, EntityManager em){
        try{
            Object newObject = objectClass.newInstance();
            List<Field> allFields = getAllFields(objectClass, false);
            Field myField = null;
            for (int i = 0; i< clonnable.getAttributes().size(); i++){
                for (Field field : allFields){
                    if (field.getName().equals(clonnable.getAttributes().get(i))){
                        myField = field;
                        break;
                    }
                }
                if (myField !=null){
                    if (myField.getAnnotation(NoCopy.class) == null)
                        newObject.getClass().getMethod("set"+capitalize(clonnable.getAttributes().get(i)),
                            myField.getType()).invoke(newObject,
                            getRealValue(myField.getType().getSimpleName(),clonnable.getValues().get(i), em));
                }
            }
            return newObject;
        }catch (Exception e){
            return null;
        }
     }

    /*
     * Finds the real type for a given type provided as a string
     * Possible types:
     * -A string --> String
     * -A boolean --> Boolean
     * -A number --> Float, Integer, Long
     * -A Date --> Date, Time, Timestamp(?) --> Check this possibilities in the server
     * -A reference to any other object --> LocalObjectListItem
     *
     * If you're porting the client to other language you should map the types
     * as supported by such language.
     */
    public static Class getRealType(String typeAsString){
        if (typeAsString.equals("String"))
            return String.class;
        if (typeAsString.equals("Integer"))
            return Integer.class;
        if (typeAsString.equals("Float"))
            return Float.class;
        if (typeAsString.equals("Long"))
            return Long.class;
        if (typeAsString.equals("Date"))
            return Date.class;
        if (typeAsString.equals("Time"))
            return Time.class;
        if (typeAsString.equals("Timestamp"))
            return Timestamp.class;
        if (typeAsString.equals("Boolean"))
            return Boolean.class;
        else
            return ObjectList.class;
    }

    /**
     * Gets the mapped value of a given attribute provided the value and type as strings
     * TODO: Should this method be changed to throw a WrongMappingException?
     * @param type
     * @param valueAsString
     * @param em
     * @return
     */
    public static Object getRealValue (String type, String valueAsString, EntityManager em){
        if (valueAsString == null)
            return null;
        try{
            if (type.equals("Boolean"))
                return Boolean.valueOf(valueAsString);

            if (type.equals("String"))
                return valueAsString;

            if (type.equals("Integer"))
                return Integer.valueOf(valueAsString);

            if (type.equals("Float"))
                return Float.valueOf(valueAsString);

            if (type.equals("Long"))
                return Long.valueOf(valueAsString);

            if (type.equals("Date"))
                return new Date(Long.valueOf(valueAsString));
            if (type.equals("Timestamp"))
                return Timestamp.valueOf(valueAsString);
            if (type.equals("Time"))
                return Time.valueOf(valueAsString);
            //In any other case we try to find an ObjectListItem
            try{

                Long oid = Long.valueOf(valueAsString);

                if (oid == 0) //Id 0 means null
                    return null;

                //Class itemClass = Class.forName(type);
                Class itemClass = org.kuwaiba.entity.multiple.GenericObjectList.class; //Just by now
                Object item = em.find(itemClass, oid);
                if (item == null) //TODO Make this return safer
                    return valueAsString;
                return item;
            }catch(Exception e){
                return valueAsString;
            }
            
        }catch (Exception e){
            return valueAsString; //In case of error,
        }
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
     * Replace special characters to prevent SQL injection attacks. So far it prepends a "\" to
     * the characters ', & and "
     *
     * @param poisonedString
     * @return
     */
    public static String convertSpecialCharacters(String poisonedString){
        if (poisonedString == null)
            return null;
        String res =poisonedString.replace("&", "\\&");
        res = poisonedString.replace("'", "\\'");
        res =poisonedString.replace("\"", "\\\"");
        return res;
    }

    public static Class getClassFor(String className, HashMap<String,Class> classIndex) throws ClassNotFoundException{
        Class myClass = classIndex.get(className);
        if (myClass != null)
            return myClass;
        else throw new ClassNotFoundException(className);
    }

    /**
     * Recursive method to chain the attribute names and values to be used by the WHERE clause, this is:<br />
     * WHERE <condition_1> <logical_connector> <condition_2> ... <logical_connector> <condition_N> <br />
     * It's recursive because it support JOINS and they're treated as subqueries
     * @param prefix the prefix used to refer the class alias in the JPQL statement (i.e x0.name., x0.vendor.name.)
     * @param myQuery the current query as a TransientQuery object
     * @param formerPredicates the previous predicates
     * @param em The current entity manager used to check if the query class is valid
     * @throws ClassNotFoundException is the query class is not valid
     * @throws NoSuchFieldException if the attribute to be used as condition is not valid
     */
    public static void chainPredicates(String prefix, TransientQuery myQuery, 
            ArrayList<String> formerPredicates, EntityManager em, HashMap<String, Class> classIndex)
            throws ClassNotFoundException, NoSuchFieldException{

        if (myQuery.getAttributeNames() != null){
            Class toBeSearched = getClassFor(myQuery.getClassName(), classIndex);

            for (int i = 0; i < myQuery.getAttributeNames().size(); i++){
                String attribute = myQuery.getAttributeNames().get(i);
                Object mappedValue = MetadataUtils.getRealValue(HierarchyUtils.
                        getField(toBeSearched, attribute).getType().getSimpleName(), myQuery.getAttributeValues().get(i), em);

                if (mappedValue == null) { //Look for a join in the getJoins()
                    TransientQuery myJoin = myQuery.getJoins().get(i);
                    if (myJoin == null) //If this is null, we're trying to match what objects has the current attribute set to null
                        formerPredicates.add(prefix+myQuery.getAttributeNames().get(i)+" IS NULL"); //NOI18N
                    else {
                        Class innerClass = getClassFor(myJoin.getClassName(),classIndex); //Only used to check if the class is valid
                        chainPredicates(prefix+myQuery.getAttributeNames().get(i)+".", myJoin, formerPredicates, em, classIndex); //NOI18N
                    }
                } else { //Process a simple value
                    if (mappedValue instanceof String) {
                        switch (myQuery.getConditions().get(i)) {
                            case TransientQuery.EQUAL:
                                formerPredicates.add(prefix+myQuery.getAttributeNames().get(i)+
                                        "='"+MetadataUtils.convertSpecialCharacters((String)mappedValue)+"'"); //NOI18N
                                break;
                            case TransientQuery.LIKE:
                                //The like here is case-sensitive (?), so we have to lowercase the string
                                formerPredicates.add("LOWER("+prefix+myQuery.getAttributeNames().get(i)+   //NOI18N
                                        ") LIKE '%"+MetadataUtils.convertSpecialCharacters((String)mappedValue).toLowerCase()+"%'");  //NOI18N
                                break;
                        }
                    } else {
                        if (mappedValue instanceof Boolean)
                            formerPredicates.add(prefix+myQuery.getAttributeNames().get(i)+"="+mappedValue);  //NOI18N

                        else {
                            if (mappedValue instanceof Integer || mappedValue instanceof Float || mappedValue instanceof Long) {
                                switch (myQuery.getConditions().get(i)) {
                                    case TransientQuery.EQUAL:
                                        formerPredicates.add(prefix+myQuery.getAttributeNames().get(i)+"="+mappedValue);  //NOI18N
                                        break;
                                    case TransientQuery.EQUAL_OR_GREATER_THAN:
                                        formerPredicates.add(prefix+myQuery.getAttributeNames().get(i)+">="+mappedValue);  //NOI18N
                                        break;
                                    case TransientQuery.EQUAL_OR_LESS_THAN:
                                        formerPredicates.add(prefix+myQuery.getAttributeNames().get(i)+"<="+mappedValue);  //NOI18N
                                        break;
                                    case TransientQuery.GREATER_THAN:
                                        formerPredicates.add(prefix+myQuery.getAttributeNames().get(i)+">"+mappedValue);  //NOI18N
                                        break;
                                    case TransientQuery.LESS_THAN:
                                        formerPredicates.add(prefix+myQuery.getAttributeNames().get(i)+"<"+mappedValue);  //NOI18N
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Recursive method to chain the attribute names to be used by the SELECT clause<br />
     * SELECT x0.name x1.name FROM Building x0, LocationOwner x1 ...
     * @param classIndex the class index. It's used to compose the alias for the chained classes (i.e. x0, x1, ..., xN)
     * @param from The current "from" clause string. Initially the value is "FROM MySearchedClass x0"
     * @param fields the current fields 
     */
      public static void chainVisibleAttributes(TransientQuery myQuery,
        List<String> fields, List<String> columnNames, List<String> joins,String prefix) {
        if (myQuery.getVisibleAttributeNames() != null){
            for (String field : myQuery.getVisibleAttributeNames()){
                //We should ignore the attributes name and id since they have been already
                //added to the select clause
                columnNames.add(myQuery.getClassName()+"."+field); //NOI18N
                fields.add(prefix+field); //NOI18N
            }
        }
        if (myQuery.getAttributeNames() != null){
            for (int i = 0; i < myQuery.getAttributeNames().size(); i++)
                if (myQuery.getJoins().get(i) != null){
                    joins.add(prefix+myQuery.getAttributeNames().get(i)+" "+
                            myQuery.getJoins().get(i).getClassName().toLowerCase()+i);
                    chainVisibleAttributes(myQuery.getJoins().get(i),
                            fields, columnNames, joins,
                            myQuery.getJoins().get(i).getClassName().toLowerCase()+i+".");
                }
        }
    }
//        public static void chainVisibleAttributes(TransientQuery myQuery,
//            List<String> fields, List<String> columnNames, String prefix) {
//            if (myQuery.getVisibleAttributeNames() != null){
//                for (String field : myQuery.getVisibleAttributeNames()){
//                    //We should ignore the attributes name and id since they have been already
//                    //added to the select clause
//                    //if (!(field.equals("id") || field.equals("name")) && ignoreMainFields){
//                    columnNames.add(myQuery.getClassName()+"."+field); //NOI18N
//                    fields.add(prefix+field); //NOI18N
//                    //}
//                }
//            }
//            if (myQuery.getAttributeNames() != null){
//                for (int i = 0; i < myQuery.getAttributeNames().size(); i++)
//                    if (myQuery.getJoins().get(i) != null)
//                        chainVisibleAttributes(myQuery.getJoins().get(i),
//                                fields, columnNames, prefix+myQuery.getAttributeNames().get(i)+".");
//            }
//    }
}

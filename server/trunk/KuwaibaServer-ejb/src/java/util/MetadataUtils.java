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
 *  under the License.
 */

package util;

import core.toserialize.ObjectList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

/**
 * Class for manipulating method and class names and stuff related to filtering data
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class MetadataUtils {
     /*
     * TODO: Why the call to isPrivate or isProtected didn't work?
     */
    public static List<Field> getAllAttributes(Class<?> aClass, List<Field> attributesSoFar){
        for (Field f : aClass.getDeclaredFields())
            if(!(Modifier.toString(f.getModifiers()).startsWith("private") ||
                    Modifier.toString(f.getModifiers()).startsWith("protected transient")))
                attributesSoFar.add(f);

        if (aClass.getSuperclass() != null)
            getAllAttributes(aClass.getSuperclass(), attributesSoFar);
        return attributesSoFar;
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

    public static Object getRealValue (String type, String valueAsString){
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
                return Date.valueOf(valueAsString);
            if (type.equals("Timestamp"))
                return Timestamp.valueOf(valueAsString);
            if (type.equals("Time"))
                return Time.valueOf(valueAsString);
            return Long.valueOf(valueAsString); //An Id for an ObjectList
        }catch (Exception e){
            return valueAsString; //In case of error,
        }
    }
}

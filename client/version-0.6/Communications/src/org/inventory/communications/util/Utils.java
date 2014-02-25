/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.communications.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.inventory.communications.core.LocalObjectLight;

/**
 * Misc helpers
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Utils {
        /**
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
            return LocalObjectLight.class;
    }
    
        /**
     * Convert a byte array into an image
     * @param bytes The byte array
     * @return
     */
    public static Image getImageFromByteArray(byte[] bytes){
        if (bytes == null)
            return null;
        try {
            InputStream in = new ByteArrayInputStream(bytes);
            BufferedImage bf = ImageIO.read(in);
            return bf;
        } catch (IOException ex) {
            return null;
        }
    }
    
    /**
     * Converts a string value to the real type given the mapping and the type
     * @param type
     * @param mapping
     * @param valueAsString
     * @return
     * @throws IllegalArgumentException 
     */
    public static Object getRealValue (String type, Integer mapping, List<String> valueAsString) throws IllegalArgumentException{
        if (valueAsString == null)
            return null;
        if (valueAsString.isEmpty())
            return null;
        try{
            switch (mapping){
                case Constants.MAPPING_PRIMITIVE:
                case Constants.MAPPING_DATE:
                case Constants.MAPPING_TIMESTAMP:
                    if (type.equals("Boolean"))
                        return Boolean.valueOf(valueAsString.get(0));

                    if (type.equals("String"))
                        return valueAsString.get(0);

                    if (type.equals("Integer"))
                        return Integer.valueOf(valueAsString.get(0));

                    if (type.equals("Float"))
                        return Float.valueOf(valueAsString.get(0));

                    if (type.equals("Long"))
                        return Long.valueOf(valueAsString.get(0));

                    if (type.equals("Date"))
                        return new Date(Long.valueOf(valueAsString.get(0)));
                    if (type.equals("Timestamp"))
                        return Timestamp.valueOf(valueAsString.get(0));

                    //In any other case we treat it as a LocalObjectListItem, returning its id
                    return Long.valueOf(valueAsString.get(0));
                case Constants.MAPPING_MANYTOMANY:
                    List<Long> res = new ArrayList<Long>();
                    for (String value : valueAsString)
                        res.add(Long.valueOf(value));
                    return res;
                case Constants.MAPPING_MANYTOONE:
                    if (valueAsString.isEmpty())
                        return null;
                    return Long.valueOf(valueAsString.get(0));
                default:
                    throw new Exception();
            }
        }catch(Exception e){
            throw new IllegalArgumentException();
        }
    }
}

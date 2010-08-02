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

package org.inventory.core.services.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import javax.imageio.ImageIO;
import org.inventory.core.services.interfaces.LocalObjectListItem;

/**
 * Class with utility methods
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class Utils {
    public static Image getImageFromByteArray(byte[] bytes){
        try {
            InputStream in = new ByteArrayInputStream(bytes);
            BufferedImage bf = ImageIO.read(in);
            return bf;
        } catch (IOException ex) {
            return null;
        }
    }

    public static byte[] getByteArrayFromImage(File f,String format){
        try {
            BufferedImage img = ImageIO.read(f);
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            ImageIO.write(img, format, bas);
            return bas.toByteArray();
        } catch (IOException ex) {
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
            return LocalObjectListItem.class;
    }

    public static Object getRealValue (String type, String valueAsString){
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
            //In any other case we treat it as a LocalObjectListItem, returning its id
            return Long.valueOf(valueAsString);

        }catch(Exception e){
            return valueAsString;
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
}

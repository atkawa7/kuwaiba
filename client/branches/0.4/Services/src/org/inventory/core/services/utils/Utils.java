/*
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

package org.inventory.core.services.utils;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;
import org.inventory.core.services.api.LocalObjectLight;

/**
 * Class with utility methods
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Utils {
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
     * Gets the bytes from a file
     * @param f File object
     * @param format format to be read
     * @return The byte array
     */
    public static byte[] getByteArrayFromFile(File f) throws IOException{
        InputStream is = new FileInputStream(f);
        long length = f.length();
        byte[] bytes;
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
        }else{
            throw new IOException("File too big "+f.getName());
        }
        is.close();
        return bytes;
    }

    /**
    *  Converts a java.awt.Image into a byte array
    *  @param im Image to be converted
    *  @param format format used to save ("png","jpg", etc). Read more about the constraints <a href="http://docs.oracle.com/javase/6/docs/api/javax/imageio/ImageIO.html#write%28java.awt.image.RenderedImage,%20java.lang.String,%20java.io.File%29">here</a>
    *  @return An byte array o null if the image passed is null
    *  @throws IOException If it's not possible to create an image using the given format
    **/
    public static byte[] getByteArrayFromImage(Image im, String format) throws IOException{

        if (im == null)
            return null;

        //PNG by default
        String myFormat = format ==null?"png":format;

        BufferedImage bu = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        bu.getGraphics().drawImage(im, 0, 0, null);
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        ImageIO.write(bu, myFormat, bas);

        //Do we need bu.getGraphics().dispose()?
        return bas.toByteArray();
    }


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

    public static FileFilter getImageFileFilter(){
        return new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;

                String extension = getExtension(f);
                if (extension != null) {
                    if(extension.equals("gif") || extension.equals("jpeg") || extension.equals("jpg") ||
                            extension.equals("png")) {
                        return true;
                    } else {return false;}
                }
                return false;
            }

            public String getExtension(File f){
                String ext = null;
                String s = f.getName();
                int i = s.lastIndexOf('.');

                if (i > 0 &&  i < s.length() - 1) {
                    ext = s.substring(i+1).toLowerCase();
                }
                return ext;
            }

            @Override
            public String getDescription() {
                return "Image Files";
            }
        };
    }
    
    /**
     * This method receives two conjuncts and extract the elements that are not common among them
     * @param groupA
     * @param groupB
     * @return An array of two positions with the remaining elements in the conjunct A and the second with the B's elements
     */
    public static Object[] inverseIntersection(List groupA, List groupB){
        for (Object elementA : groupA){
            for (Object elementB : groupB){
                if (elementA.equals(elementB)){
                    List<Object> myGroupA = new ArrayList<Object>(groupA);
                    List<Object> myGroupB = new ArrayList<Object>(groupB);
                    myGroupA.remove(elementA);
                    myGroupB.remove(elementB);
                    return inverseIntersection(myGroupA, myGroupB);
                }
            }
        }
        return new Object[]{groupA,groupB};
    }

    /**
     * Returns string based on a given color. For now is pretty simple making a plain comparison
     * it could be extended by guessing the name base on the RGB value
     * @param value The color to be evaluated
     * @return The string representing the color
     */
    public static String getColorName(Color value){
        if (value.equals(Color.black))
                    return "Black";
                else
                    if (value.equals(Color.white))
                        return "White";
                    else
                        if (value.equals(Color.red))
                            return "Red";
                        else
                            if (value.equals(Color.blue))
                                return "Blue";
                            else
                                if (value.equals(Color.green))
                                    return "Green";
                                else
                                    if (value.equals(Color.orange))
                                        return "Orange";
                                    else
                                        if (value.equals(Color.yellow))
                                            return "Yellow";
        return "Other";
    }
}

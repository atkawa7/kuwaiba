/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import org.inventory.*;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;

/**
 * Misc helpers
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Utils {
    /**
     * Default icon color (used in navigation trees and views). It's a light blue
     */
    public static final Color DEFAULT_ICON_COLOR = new Color(0, 170, 212);
    /**
     * Default class icon color. Used in the Data Model Manager
     */
    public static final Color DEFAULT_CLASS_ICON_COLOR = new Color(32, 207, 29);
    /**
     * Default icon height (used in navigation trees and views)
     */
    public static final int DEFAULT_ICON_HEIGHT = 10;
    /**
     * Default icon height (used in navigation trees and views)
     */
    public static final int DEFAULT_ICON_WIDTH = 10;
    /**
     * A singleton used to open the file choosers in the last opened location
     */
    private static JFileChooser globalFileChooser;
    private static Object I18N;
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
     * The same as getImageFromByteArray, but it returns a default icon instead of null
     * if the input parameter is null, or the byte can't be converted to an image
     * @param bytes Bytes to create the icon from. Null if you want to get the default icon
     * @param defaultColor default color in case the byte array is null. Null to use the default icon color. Ignored if "bytes" is a readable image
     * @param defaultWidth default width in case the byte array is null. Ignored if "bytes" is a readable image
     * @param defaultHeight default height in case the byte array is null. Ignored if "bytes" is a readable image
     * @return 
     */
    public static Image getIconFromByteArray(byte[] bytes, Color defaultColor, int defaultWidth, int defaultHeight){
        if (bytes == null || bytes.length == 0)
            return createRectangleIcon(defaultColor == null ? DEFAULT_ICON_COLOR : defaultColor, 
                    defaultWidth, defaultHeight);
        try {
            InputStream in = new ByteArrayInputStream(bytes);
            BufferedImage bf = ImageIO.read(in);
            return bf;
        } catch (IOException ex) {
            return createRectangleIcon(defaultColor ==  null ? DEFAULT_ICON_COLOR : defaultColor, 
                    defaultWidth, defaultHeight);
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
                case Constants.MAPPING_BINARY:
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
                    
                    if (type.equals("Binary"))
                        return new Binary(valueAsString.get(0));

                    //In any other case we rise an IllegalArgumentException
                    throw new IllegalArgumentException(String.format("The type %s has a wrong mapping and will be ignored", type));
                case Constants.MAPPING_MANYTOMANY:
                    List<Long> res = new ArrayList<>();
                    for (String value : valueAsString)
                        res.add(Long.valueOf(value));
                    return res;
                case Constants.MAPPING_MANYTOONE:
                    if (valueAsString.isEmpty())
                        return null;
                    //return Long.valueOf(valueAsString.get(0));
                    return Utils.getListTypeItem(type, Long.valueOf(valueAsString.get(0)));
                default:
                    throw new Exception();
            }
        }catch(Exception e){
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Creates an image of a rectangle of given dimensions and color. can be used to generate icons
     * @param color Color
     * @param width Width
     * @param height Height
     * @return A BufferedImage object
     */
    public static Image createRectangleIcon(Color color, int width, int height){
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        if (color != null)
            graphics.setColor(color);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        return image;
    }
    
    /**
     * Creates a circle Image with the specified diameter and color
     * @param color Color of the icon
     * @param diameter Diameter of the icon
     * @return An Image object
     */
    public static Image createCircleIcon(Color color, int diameter){
        BufferedImage image = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                          RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(color);
        graphics.fillOval(0, 0, diameter, diameter);
        graphics.dispose();
        return image;
    }
    
    /**
     * Gets the bytes from a file
     * @param f File object
     * @param format format to be read
     * @return The byte array
     */
    public static byte[] getByteArrayFromFile(File f) throws IOException {
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
            if (offset < bytes.length) 
                throw new IOException("Could not completely read file "+f.getName());
            
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
        String myFormat = format == null ? "png" : format;

        BufferedImage bu = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        bu.getGraphics().drawImage(im, 0, 0, null);
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        ImageIO.write(bu, myFormat, bas);

        //Do we need bu.getGraphics().dispose()?
        return bas.toByteArray();
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
    
    /**
     * Manages the file chooser singleton
     * @return The instance of file chooser to be shared across modules. This way, the
     * last directory is preserved
     */
    public static JFileChooser getGlobalFileChooser() {
        if (globalFileChooser == null)
            globalFileChooser = new JFileChooser();
        return globalFileChooser;
    }
    
    /**
     * This is a utility method that sets an object's property. This is used mainly by property sheets
     * @param className Object class
     * @param objectId Object Id
     * @param propertyName Name of the property to be set
     * @param propertyValue Value of the property to be set
     * @throws Exception The same exception captured in the CommunicationsStub
     */
    public static void updateObject(String className, long objectId, String propertyName, Object propertyValue) throws Exception {
        LocalObject theUpdate = new LocalObject(className, objectId, new String [] { propertyName }, new Object [] { propertyValue });
        
        if(!CommunicationsStub.getInstance().saveObject(theUpdate))
            throw new Exception(CommunicationsStub.getInstance().getError());
    }
    
    public static LocalObjectListItem getListTypeItem(String listTypeClass, long listTypeItemId) throws IllegalAccessException {
        List<LocalObjectListItem> list = CommunicationsStub.getInstance().getList(listTypeClass, true, false);
        if (list == null)
            throw new IllegalAccessException(CommunicationsStub.getInstance().getError());
        
        for (LocalObjectListItem listItem : list) {
            if (listItem.getId() == listTypeItemId)
                return listItem;
        }
        
        throw new IllegalArgumentException(String.format("List type %s with id %s could not be found", listTypeClass, listTypeItemId));
    }

    /**
     * Outputs as a string a list of inventory objects (usually a list of parents in the containment hierarchy)
     * @param objectList The list of objects
     * @param startFromTheLast The output string should start from the first or the last object?
     * @param howManyToShow How many elements should be displayed? used -1 to show all
     * @return A string with the names of the objects concatenated with a "/" as separator
     */
    public static String formatObjectList(List<LocalObjectLight> objectList, boolean startFromTheLast, int howManyToShow) {
        if (startFromTheLast)
            Collections.reverse(objectList);
        
        String outputString = "";
        int i;
        
        for (i = 0;  i <  ((howManyToShow == -1 || howManyToShow >= objectList.size()) ? objectList.size() - 1 : howManyToShow - 1); i++) 
            outputString += objectList.get(i) + " / ";
        
        
        outputString += objectList.get(i);
        return outputString;
    }
    
    
    /**
     * Checks if a given string text is a number or not
     * @param s String text
     * @return true if is number
     */
    public static boolean isNumeric(String s) {  
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
    }
    
    /**
     * Checks if a given class may have a device layout
     * @param className className
     * @return true if the class can have a device layout
     * @throws Exception The same exception captured in the CommunicationsStub
     */
    public static boolean classMayHaveDeviceLayout(String className) throws Exception {
        if (CommunicationsStub.getInstance().hasAttribute(className, Constants.ATTRIBUTE_MODEL)) {
            LocalAttributeMetadata lam = CommunicationsStub.getInstance().getAttribute(className, Constants.ATTRIBUTE_MODEL);
            if (lam == null)
                throw new Exception(CommunicationsStub.getInstance().getError());

            if (CommunicationsStub.getInstance().isSubclassOf(
                lam.getListAttributeClassName(), Constants.CLASS_GENERICOBJECTLIST)) {
                return true;
            }
        }
        return false;
    }
}

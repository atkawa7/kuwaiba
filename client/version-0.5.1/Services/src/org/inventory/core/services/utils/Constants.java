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

package org.inventory.core.services.utils;

/**
 * Misc constants
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Constants {
     /**
     * Name of the validator to indicate if a given class is a link
     */
    public static String IS_PHYSICAL_LINK_VALIDATOR = "physicalLink";
    /**
     * Name of the validator to indicate if a given class is the endpoint to a connection
     */
    public static String IS_PHYSICAL_NODE_VALIDATOR = "physicalNode";
    /**
     * Name of the validator to indicate if a given class is the endpoint to a link
     */
    public static String IS_PHYSICAL_ENDPOINT_VALIDATOR = "physicalEndpoint";
    /**
     * Name for the class inventory object
     */
    public static String CLASS_INVENTORYOBJECT = "InventoryObject";
    /**
     * Name for the class Generic Object List
     */
    public static String CLASS_GENERICOBJECTLIST = "GenericObjectList";
    /**
     * Defaul type for a new attribute
     */
    public static final String DEFAULT_ATTRIBUTE_TYPE = "String";
    /**
     * Integer, Float, Long, Boolean, String or Text
     */
    public static final int MAPPING_PRIMITIVE = 1;
    /**
     * Dates
     */
    public static final int MAPPING_DATE = 2;
    /**
     * Timestamp
     */
    public static final int MAPPING_TIMESTAMP = 3;
    /**
     * Binary
     */
    public static final int MAPPING_BINARY = 4;
    /**
     * Many to one relationship (such as types)
     */
    public static final int MAPPING_MANYTOONE = 5;
    /**
     * Many to Many relationship (such as accountable persons for a given equipment)
     */
    public static final int MAPPING_MANYTOMANY = 6;
    /**
     * Possible attributes types
     */
    public static final String [] ATTRIBUTE_TYPES = new String[]{"String", "Integer", "Long", "Float", "Boolean", "Date", "Timestamp"};
    /**
     * Property name
     */
    public static final String PROPERTY_NAME = "name";
    /**
     * Property display name
     */
    public static final String PROPERTY_DISPLAYNAME = "displayName";
    /**
     * Property description
     */
    public static final String PROPERTY_DESCRIPTION = "description";
    /**
     * Property abstract
     */
    public static final String PROPERTY_ABSTRACT = "abstract";
    /**
     * Property in design
     */
    public static final String PROPERTY_INDESIGN = "inDesign";
    /**
     * Property countable
     */
    public static final String PROPERTY_COUNTABLE = "countable";
    /**
     * Property custom
     */
    public static final String PROPERTY_CUSTOM = "custom";
    /**
     * Property small icon
     */
    public static final String PROPERTY_SMALLICON = "smallIcon";
    /**
     * Property icon
     */
    public static final String PROPERTY_ICON = "icon";
    /**
     * Property creation date
     */
    public static final String PROPERTY_CREATIONDATE = "creationDate";
    /**
     * Property type
     */
    public static final String PROPERTY_TYPE = "type";
    /**
     * Property administrative
     */
    public static final String PROPERTY_ADMINISTRATIVE = "administrative";
    /**
     * Property no copy
     */
    public static final String PROPERTY_NOCOPY = "noCopy";
    /**
     * Property unique
     */
    public static final String PROPERTY_UNIQUE = "unique";
    /**
     * Property visible
     */
    public static final String PROPERTY_VISIBLE = "visible";
    /**
     * Property read only
     */
    public static final String PROPERTY_READONLY = "readOnly";
    
}

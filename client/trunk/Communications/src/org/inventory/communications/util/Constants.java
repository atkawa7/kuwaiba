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

package org.inventory.communications.util;

/**
 * Misc constants
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Constants {
    /**
     * Name of the validator to indicate if a given class is a container
     */
    public static String VALIDATOR__PHYSICAL_CONTAINER = "physicalContainer";
     /**
     * Name of the validator to indicate if a given class is a link
     */
    public static String VALIDATOR__PHYSICAL_LINK = "physicalLink";
    /**
     * Name of the validator to indicate if a given class is the endpoint to a connection
     */
    public static String VALIDATOR_PHYSICAL_NODE = "physicalNode";
    /**
     * Name of the validator to indicate if a given class is the endpoint to a link
     */
    public static String VALIDATOR_PHYSICAL_ENDPOINT = "physicalEndpoint";
    /**
     * Name for the class InventoryObject
     */
    public static String CLASS_INVENTORYOBJECT = "InventoryObject";
    /**
     * Name for the class GenericObjectList
     */
    public static String CLASS_GENERICOBJECTLIST = "GenericObjectList";
    /**
     * Root class to all ports
     */
    public static String CLASS_GENERICPORT = "GenericPort";
    /**
     * Root class to all physical links (cables, fibers, etc)
     */
    public static String CLASS_GENERICPHYSICALLINK = "GenericPhysicalLink";
    /**
     * Class Rack
     */
    public static String CLASS_RACK = "Rack";
    /**
     * Name for the class User
     */
    public static String CLASS_USER = "User";
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
    /**
     * Property parent
     */
    public static final String PROPERTY_PARENT = "parent";
    /**
     * Property id
     */
    public static final String PROPERTY_ID = "id";
    /**
     * Property rackUnits
     */
    public static final String PROPERTY_RACKUNITS = "rackUnits";
    /**
     * Property startRackUnit
     */
    public static final String PROPERTY_POSITION = "position";
    /**
     * Generic classes
     */
    public static String CLASS_GENERICCONNECTION="GenericConnection";

    //TODO: Gotta send this to a config file
    public static String CLASS_WIRECONTAINER="WireContainer";
    public static String CLASS_WIRELESSCONTAINER="WirelessContainer";

    /**
     * Physical connection classes
     */
    public static String CLASS_ELECTRICALLINK = "ElectricalLink";
    public static String CLASS_OPTICALLINK = "OpticalLink";
    public static String CLASS_WIRELESSLINK = "RadioLink";

    /**
     * Physical connection type classes
     */
    public static String CLASS_ELECTRICALLINKTYPE = "ElectricalLinkType";
    public static String CLASS_OPTICALLINKTYPE = "OpticalLinkType";
    public static String CLASS_WIRELESSLINKTYPE = "WirelessLinkType";

    /**
     * Physical container type classes
     */
    public static String CLASS_WIRECONTAINERTYPE = "WireContainerType";
    public static String CLASS_WIRELESSCONTAINERTYPE = "WirelessContainerType";

    //Misc versions
    /**
     * Version for the XML document to save views (see http://neotropic.co/kuwaiba/wiki/index.php?title=XML_Documents#To_Save_Object_Views for details)
     */
     public static String VIEW_FORMAT_VERSION = "1.1";

    /**
     * Returns the connection type class for a given connection class
     */
    public static String getConnectionType(String connectionClass){
        if (connectionClass.equals(CLASS_ELECTRICALLINK))
            return CLASS_ELECTRICALLINKTYPE;
        if (connectionClass.equals(CLASS_OPTICALLINK))
            return CLASS_OPTICALLINKTYPE;
        if (connectionClass.equals(CLASS_WIRELESSLINK))
            return CLASS_WIRELESSLINKTYPE;
        if (connectionClass.equals(CLASS_WIRECONTAINER))
            return CLASS_WIRECONTAINERTYPE;
        if (connectionClass.equals(CLASS_WIRELESSCONTAINER))
            return CLASS_WIRELESSCONTAINERTYPE;
        return null;
    }
    
}

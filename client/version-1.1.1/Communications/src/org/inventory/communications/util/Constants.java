/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
     * No debugging
     */
    public static final int DEBUG_LEVEL_DISABLED = 0;
    public static final int DEBUG_LEVEL_INFO = 1;
    public static final int DEBUG_LEVEL_FINE = 2;
    public static int DEBUG_LEVEL = DEBUG_LEVEL_INFO;
    //public static int DEBUG_LEVEL = DEBUG_LEVEL_DISABLED;
     /**
     * IP address type for IPv4
     */
    public static final int IPV4_TYPE = 4;
    /**
     * IP address type for IPv6
     */
    public static final int IPV6_TYPE = 6;
    /**
     * Name of the validator to indicate if a given class is a container
     */
    public static final String VALIDATOR_PHYSICAL_CONTAINER = "physicalContainer";
    /**
     * Name of the validator to indicate if a given class is a link
     */
    public static final String VALIDATOR_PHYSICAL_LINK = "physicalLink";
    /**
     * Name of the validator to indicate if a given class is the endpoint to a connection
     */
    public static final String VALIDATOR_PHYSICAL_NODE = "physicalNode";
    /**
     * Name of the validator to indicate if a given class is the endpoint to a link
     */
    public static final String VALIDATOR_PHYSICAL_ENDPOINT = "physicalEndpoint";
    /**
     * Name of the validator to indicate if a given class is the logical endpoint to a link
     */
    public static final String VALIDATOR_LOGICAL_ENDPOINT = "logicalEndpoint";
    /**
     * Name of the validator to indicate if a given class is the logical endpoint to a link
     */
    public static final String VALIDATOR_LOGICAL_SET = "logicalSet";
    /**
     * Name of the validator to indicate if a given class is the endpoint to a link
     */
    public static final String VALIDATOR_SERVICE_INSTANCE = "serviceInstance";
    /**
     * All instances of classes tagged with this validator may be related to a software asset
     */
    public static final String VALIDATOR_APPLICATION_ELEMENT = "genericApplicationElement";
    /**
     * All instances of classes tagged with this validator may be related to a subnet
     */
    public static final String VALIDATOR_SUBNET = "subnet";
    /**
     * All instances of classes tagged with this validator may be related to VLANs
     */
    public static final String VALIDATOR_VLAN = "vlanrule";
    /**
     * DummyRoot constant. It's a pseudo class
     */
    public static final String DUMMYROOT = "DummyRoot";
    /**
     * Name for the class InventoryObject
     */
    public static final String CLASS_INVENTORYOBJECT = "InventoryObject";
    /**
     * Name for the class GenericObjectList
     */
    public static final String CLASS_GENERICOBJECTLIST = "GenericObjectList";
    /**
     * Name for the class GenericService
     */
    public static final String CLASS_GENERICSERVICE = "GenericService";
    /**
     * Name for the class Service Instance
     */
    public static final String CLASS_SERVICEINSTANCE = "ServiceInstance";
    /**
     * Name for the class VRFInstance
     */
    public static final String CLASS_VRFINSTANCE = "VRFInstance";
    /**
     * Name for the class GenericCustomer
     */
    public static final String CLASS_GENERICCUSTOMER = "GenericCustomer";
    /**
     * Root class to all ports
     */
    public static final String CLASS_GENERICPORT = "GenericPort";
    /**
     * Root class to all physical links (cables, fibers, etc)
     */
    public static final String CLASS_GENERICPHYSICALLINK = "GenericPhysicalLink";
    /**
     * Root class of all communications equipment (GenericDataLinkLayerElement, GenericNetworkElement, etc)
     */
    public static final String CLASS_GENERICCOMMUNICATIONSELEMENT = "GenericCommunicationsElement";
    /**
     * Class Rack
     */
    public static final String CLASS_RACK = "Rack";
    /**
     * Name for the class User
     */
    public static final String CLASS_USER = "User";
    /**
     * Name for class subnet IPv4 is this constant used in the IPAM  module
     */
    public static final String CLASS_SUBNET = "Subnet";
    /**
     * Name for class subnet IPv4 is this constant used in the IPAM  module
     */
    public static final String CLASS_SUBNET_IPV4 = "SubnetIPv4";
    /**
     * Name for class subnet IPv6 is this constant used in the IPAM  module
     */
    public static final String CLASS_SUBNET_IPV6 = "SubnetIPv6";
    /**
     * Name for class Location owner this constant is used in the IPAM  module
     */
    public static final String CLASS_LOCATIONOWNER = "LocationOwner";
    /**
     * Name for class VLAN this constant is used in the IPAM  module
     */
    public static final String CLASS_VLAN = "VLAN";
    /**
     * Name for class GenericContract
     */
    public static final String CLASS_GENERICCONTRACT = "GenericContract";
    /**
     * Not an actual class, but yet used by the service manager to identify a pool mapped to a LocalObjectLight
     */
    public static final String CLASS_POOL = "Pool";
    /**
     * Class ViewableObject
     */
    public static final String CLASS_VIEWABLEOBJECT = "ViewableObject";
    /**
     * Class BridgeDomainInterface
     */
    public static final String CLASS_BRIDGEDOMAININTERFACE = "BridgeDomainInterface";
    /**
     * Class MPLSTunnel
     */
    public static final String CLASS_MPLSTUNNEL = "MPLSTunnel";
    /**
     * Class FrameRelay
     */
    public static final String CLASS_FRAMERELAYCIRCUIT = "FrameRelayCircuit";
    /**
     * Default type for a new attribute
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
     * This relationship is used to relate a GenericPort with an IP address 
     */
    public static final String RELATIONSHIP_IPAMHASADDRESS = "ipamHasIpAddress";
    /**
     * This relationship is used to relate a VLAN with a Subnet
     */
    public static final String RELATIONSHIP_IPAMBELONGSTOVLAN = "ipamBelongsToVlan";
    /**
     * This relationship is used to relate a VRF with a Subnet
     */
    public static final String RELATIONSHIP_IPAMBELONGSTOVRFINSTANCE = "ipamBelongsToVrfInstance";
    /**
     * This relationship is used to connect a generic port(service instances) with an interface
     */
    public static final String RELATIONSHIP_MPLSPORTBELONGSTOINTERFACE = "mplsportbelongtointerface";
    /**
     * A fixed label to indicate a given node doesn't have a name set
     */
    public static final String LABEL_NONAME = "<No Name>";
    
    /**
     * Possible attributes types
     */
    public static final String [] ATTRIBUTE_TYPES = new String[]{"String", "Integer", "Long", "Float", "Boolean", "Date", "Timestamp"};
    /**
     * Property name
     */
    public static final String PROPERTY_NAME = "name";
    
    /**
     * Property state
     */
    public static final String PROPERTY_STATE = "state";
    /**
     * Property class name
     */
    public static final String PROPERTY_CLASSNAME = "className";
    /**
     * Property display name
     */
    public static final String PROPERTY_DISPLAYNAME = "displayName";
    /**
     * Property description
     */
    public static final String PROPERTY_DESCRIPTION = "description";
    /**
     * Property enabled
     */
    public static final String PROPERTY_ENABLED = "enabled"; //NOI18N
    /**
     * Property script
     */
    public static final String PROPERTY_SCRIPT = "script"; //NOI18N
    /**
     * Property executionTime
     */
    public static final String PROPERTY_EXECUTION_TYPE = "executionType"; //NOI18N
    /**
     * Property everyXMinutes
     */
    public static final String PROPERTY_EVERY_X_MINUTES = "everyXMinutes"; //NOI18N
    /**
     * Property start time
     */
    public static final String PROPERTY_START_TIME = "startTime"; //NOI18N
    /**
     * Property Notification Type
     */
    public static final String PROPERTY_NOTIFICATION_TYPE = "notificationType"; //NOI18N
    /**
     * 
     */
    public static final String PROPERTY_PARAMETERS = "parameters"; //NOI18N
    /**
     * Property email
     */
    public static final String PROPERTY_EMAIL = "email"; //NOI18N
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
     * Property class color
     */
    public static final String PROPERTY_COLOR = "color";
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
     * Property network IP  in a subnet
     */
    public static final String PROPERTY_NETWORKIP = "networkIp";
    /**
     * Property broadcast IP in a subnet
     */
    public static final String PROPERTY_BROADCASTIP = "broadcastIp";
    /**
     * Property maximum number of hosts in a subnet
     */
    public static final String PROPERTY_HOSTS = "hosts";
    /**
     * List type class for operational state
     */
    public static final String LIST_TYPE_OPERATIONAL_STATE = "OperationalState";
    /**
     * Physical connection classes
     */
    public static final String CLASS_IP_ADDRESS = "IPAddress";
    
    /**
     * Generic classes
     */
    public static final String CLASS_GENERICCONNECTION="GenericConnection";

    //TODO: Gotta send this to a config file
    public static final String CLASS_WIRECONTAINER="WireContainer";
    public static final String CLASS_WIRELESSCONTAINER="WirelessContainer";

    /**
     * Physical connection classes
     */
    public static final String CLASS_ELECTRICALLINK = "ElectricalLink";
    public static final String CLASS_OPTICALLINK = "OpticalLink";
    public static final String CLASS_WIRELESSLINK = "RadioLink";
    public static final String CLASS_POWERLINK = "PowerLink";

    /**
     * Physical connection type classes
     */
    public static final String CLASS_ELECTRICALLINKTYPE = "ElectricalLinkType";
    public static final String CLASS_OPTICALLINKTYPE = "OpticalLinkType";
    public static final String CLASS_WIRELESSLINKTYPE = "WirelessLinkType";
    public static final String CLASS_POWERLINKTYPE = "PowerLinkType";

    /**
     * Physical container type classes
     */
    public static final String CLASS_WIRECONTAINERTYPE = "WireContainerType";
    public static final String CLASS_WIRELESSCONTAINERTYPE = "WirelessContainerType";

    //Misc versions
    /**
     * Version for the XML document to save views (see http://neotropic.co/kuwaiba/wiki/index.php?title=XML_Documents#To_Save_Object_Views for details)
     */
     public static final String VIEW_FORMAT_VERSION = "1.1";

    /**
     * Returns the connection type class for a given connection class
     * @param connectionClass The connection to be used
     * @return The type of connection corresponding to that connection class
     */
    public static String getConnectionType(String connectionClass){
        if (connectionClass.equals(CLASS_ELECTRICALLINK))
            return CLASS_ELECTRICALLINKTYPE;
        if (connectionClass.equals(CLASS_OPTICALLINK))
            return CLASS_OPTICALLINKTYPE;
        if (connectionClass.equals(CLASS_WIRELESSLINK))
            return CLASS_WIRELESSLINKTYPE;
        if (connectionClass.equals(CLASS_POWERLINK))
            return CLASS_POWERLINKTYPE;
        if (connectionClass.equals(CLASS_WIRECONTAINER))
            return CLASS_WIRECONTAINERTYPE;
        if (connectionClass.equals(CLASS_WIRELESSCONTAINER))
            return CLASS_WIRELESSCONTAINERTYPE;
        return null;
    }
    
}

/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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

package org.kuwaiba.services.persistence.util;

/**
 * Misc constants
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Constants {
    /**
     * Class hierarchy XML document version
     */
    public static final String CLASS_HIERARCHY_DOCUMENT_VERSION = "1.1";
    /**
     * Class hierarchy XML next document version
     */
    public static final String CLASS_HIERARCHY_NEXT_DOCUMENT_VERSION = "1.2";
    /**
     * Persistence Service version
     */
    public static final String PERSISTENCE_SERVICE_VERSION = "2.0";
    /**
     * Class type for RootObject
     */
    public static final int CLASS_TYPE_ROOT = 0;
    /**
     * Class Pool
     */
    public static final String CLASS_POOL = "Pool";
    /**
     * IP address type for IPv4
     */
    public static final int IPV4_TYPE = 4;
    /**
     * IP address type for IPv6
     */
    public static final int IPV6_TYPE = 6;
    /**
     * Class type for all subclasses of InventoryObject
     */
    public static final int CLASS_TYPE_INVENTORY = 1;
    /**
     * Class type for all subclasses of ApplicatioObject
     */
    public static final int CLASS_TYPE_APPLICATION = 2;
    /**
     * Class type for all subclasses of MetadataObject
     */
    public static final int CLASS_TYPE_METADATA = 3;
    /**
     * Class type for all subclasses of any class other than those above
     */
    public static final int CLASS_TYPE_OTHER = 4;
    /**
     * Application modifier for dummy classes
     */
    public static final int CLASS_MODIFIER_DUMMY = 1;
    /**
     * Application modifier for countable classes
     */
    public static final int CLASS_MODIFIER_COUNTABLE = 2;
    /**
     * Application modifier for custom classes
     */
    public static final int CLASS_MODIFIER_CUSTOM = 4;
    /**
     * Application modifier for attributes that shouldn't be copied
     */
    public static int ATTRIBUTE_MODIFIER_NOCOPY = 1;
    /**
     * Application modifier for attributes that shouldn't be serialized
     */
    public static int ATTRIBUTE_MODIFIER_NOSERIALIZE = 2;
    /**
     * Application modifier for attributes that can't be modified
     */
    public static int ATTRIBUTE_MODIFIER_READONLY = 4;
    /**
     * Application modifier for attributes that shouldn't be visible
     */
    public static int ATTRIBUTE_MODIFIER_VISIBLE = 8;
    /**
     * Application modifier for attributes used for administrative purposes only
     */
    public static int ATTRIBUTE_MODIFIER_ADMINISTRATIVE = 16;
    /**
     * Label name for user nodes
     */
    public static final String LABEL_USER = "users"; //NOI18N
    /**
     * To label the inventory objects
     */
    public static final String LABEL_INVENTORY_OBJECT = "inventory_objects"; //NOI18N
    /**
     * Label used for the metadata classes nodes label
     */
    public static final String LABEL_CLASS = "classes"; //NOI18N
    /**
     * Label name for group nodes
     */
    public static final String LABEL_GROUP = "groups"; //NOI18N
    /**
     * Name of the label for list type items
     */
    public static final String LABEL_LIST_TYPE_ITEM = "listTypeItems"; //NOI18N
    /**
     * Name of the label for pools
     */
    public static final String LABEL_POOL = "pools"; //NOI18N
    /**
     * Label used for the special nodes
     */
    public static final String LABEL_SPECIAL_NODE = "specialNodes"; //NOI18N
    /**
     * Name of the label for reports
     */
    public static final String LABEL_REPORTS = "reports"; //NOI18N
    /**
     * Name of the label for queries
     */
    public static final String LABEL_QUERIES = "queries"; //NOI18N
    /**
     * Name of the label for tasks
     */
    public static final String LABEL_TASKS = "tasks"; //NOI18N
    /**
     * Label used for the business rules
     */
    public static final String LABEL_BUSINESS_RULES = "businessRules"; //NOI18N
    /**
     * Name of the label for general views
     */
    public static final String LABEL_GENERAL_VIEWS = "generalViews"; //NOI18N
    /**
     * Name of the label for synchronization groups
     */
    public static final String LABEL_SYNCGROUPS = "syncGroups"; //NOI18N
    /**
     * Label used for the attributes nodes label
     */
    public static final String LABEL_ATTRIBUTE = "attribute"; //NOI18N
    /**
     * label used for root, dummyRoot, groupsRoot, nodes
     */
    public static final String LABEL_ROOT = "root";
    /**
     * label used for root, dummyRoot, groupsRoot, nodes
     */
    public static final String LABEL_LIST_TYPE = "listType";
    /**
     * Label used for script query nodes
     */
    public static final String LABEL_SCRIPT_QUERY = "scriptQuery";
    /**
     * label used for contacts
     */
    public static final String LABEL_CONTACTS = "contacts";
    /**
     * Property "background path" for views
     */
    public static final String PROPERTY_BACKGROUND_FILE_NAME = "backgroundPath";
    /**
     * Property "class name" for pools
     */
    public static final String PROPERTY_CLASS_NAME = "className";
    /**
     * Property CHILD_OF_SPECIAL relationship type
     */
    public static final String REL_PROPERTY_POOL = "pool";
    /**
     * Property "structure" for views
     */
    public static final String PROPERTY_STRUCTURE = "structure"; //NOI18N
    public static final String PROPERTY_BACKGROUND = "background"; //NOI18N
    public static final String PROPERTY_PRIVILEGES = "privileges"; //NOI18N
    public static final String PROPERTY_NAME = "name"; //NOI18N
    /**
     * the name of the synchronization provider
     */
    public static final String PROPERTY_SYNCPROVIDER = "syncProvider"; //NOI18N
    /** 
     * Defines if the attribute of a class is mandatory or not
     */
    public static final String PROPERTY_MANDATORY = "mandatory"; //NOI18N
    public static final String PROPERTY_CREATION_DATE = "creationDate"; //NOI18N
    public static final String PROPERTY_DISPLAY_NAME = "displayName"; //NOI18N
    public static final String PROPERTY_TYPE = "type"; //NOI18N
    public static final String PROPERTY_ADMINISTRATIVE = "administrative"; //NOI18N
    public static final String PROPERTY_VISIBLE = "isVisible"; //NOI18N
    public static final String PROPERTY_DESCRIPTION = "description"; //NOI18N
    public static final String PROPERTY_READ_ONLY = "readOnly"; //NOI18N
    public static final String PROPERTY_ID = "id"; //NOI18N
    public static final String PROPERTY_OID = "oid"; //NOI18N
    public static final String PROPERTY_ABSTRACT = "abstract"; //NOI18N
    public static final String PROPERTY_CUSTOM = "custom"; //NOI18N
    public static final String PROPERTY_COUNTABLE = "countable"; //NOI18N
    public static final String PROPERTY_COLOR = "color"; //NOI18N
    public static final String PROPERTY_ICON = "icon"; //NOI18N
    public static final String PROPERTY_SMALL_ICON = "smallIcon"; //NOI18N
    public static final String PROPERTY_NO_COPY = "noCopy"; //NOI18N
    public static final String PROPERTY_UNIQUE = "unique"; //NOI18N
    public static final String PROPERTY_IN_DESIGN = "inDesign"; //NOI18N
    public static final String PROPERTY_FIRST_NAME = "firstName"; //NOI18N
    public static final String PROPERTY_LAST_NAME = "lastName"; //NOI18N
    public static final String PROPERTY_ENABLED = "enabled"; //NOI18N
    public static final String PROPERTY_COMMIT_ON_EXECUTE = "commitOnExecute"; //NOI18N
    public static final String PROPERTY_SCRIPT = "script"; //NOI18N
    public static final String PROPERTY_EXECUTION_TYPE = "executionTime"; //NOI18N
    public static final String PROPERTY_EVERY_X_MINUTES = "everyXMinutes"; //NOI18N
    public static final String PROPERTY_START_TIME = "startTime"; //NOI18N
    public static final String PROPERTY_NOTIFICATION_TYPE = "notificationType"; //NOI18N
    public static final String PROPERTY_EMAIL = "email"; //NOI18N
    public static final String PROPERTY_PASSWORD = "password"; //NOI18N
    public static final String PROPERTY_OLD_VALUE = "oldValue"; //NOI18N
    public static final String PROPERTY_NEW_VALUE = "newValue"; //NOI18N
    public static final String PROPERTY_NOTES = "notes"; //NOI18N
    public static final String PROPERTY_AFFECTED_PROPERTY = "affectedProperty"; //NOI18N
    public static final String PROPERTY_CATEGORY = "category"; //NOI18N
    public static final String PROPERTY_SUPPORT_PHONE_NUMBER = "supportPhoneNumber"; //NOI18N
    public static final String PROPERTY_SUPPORT_EMAIL = "supportEmail"; //NOI18N
    public static final String PROPERTY_PARAMETERS = "parameters"; //NOI18N
    public static final String PROPERTY_SCOPE = "scope"; //NOI18N
    public static final String PROPERTY_VERSION = "version"; //NOI18N
    public static final String PROPERTY_APPLIES_TO = "appliesTo"; //NOI18N
    /**
     * Property SNMP version
     */
    public static final String PROPERTY_SNMP_VERSION = "version";
    /**
     * SNMP version 2c property community
     */
    public static final String PROPERTY_COMMUNITY = "community";
    /**
     * SNMP version 3 property authentication protocol
     */
    public static final String PROPERTY_AUTH_PROTOCOL = "authProtocol";
    /**
     * SNMP version 3 property authentication protocol pass phrase
     */
    public static final String PROPERTY_AUTH_PASS = "authPass";    
    /**
     * SNMP version 3 property security Level
     */
    public static final String PROPERTY_SECURITY_LEVEL = "securityLevel";
    /**
     * SNMP version 3 property context Name
     */
    public static final String PROPERTY_CONTEXT_NAME = "contextName";
    /**
     * SNMP version 3 property security name
     */
    public static final String PROPERTY_SECURITY_NAME = "securityName";
    /**
     * SNMP version 3 property privacy Protocol
     */
    public static final String PROPERTY_PRIVACY_PROTOCOL = "privacyProtocol";
    /**
     * SNMP version 3 property privacy protocol pass phrase
     */
    public static final String PROPERTY_PRIVACY_PASS = "privacyPass";
    
    /**
     * Root for all business classes
     */
    public static final String CLASS_INVENTORYOBJECT = "InventoryObject"; //NOI18N ID 286
    /**
     * Root for all list types class name
     */
    public static final String CLASS_GENERICOBJECTLIST = "GenericObjectList"; //NOI18N ID 27
    /**
     * Root for all classes that can have a view attached
     */
    public static final String CLASS_VIEWABLEOBJECT = "ViewableObject"; //NOI18N
    /**
     * Class hierarchy root
     */
    public static final String CLASS_ROOTOBJECT = "RootObject"; //NOI18N
    /**
     * Class Generic Service
     */
    public static final String CLASS_GENERICSERVICE = "GenericService"; //NOI18N
    /**
     * Class Generic Customer
     */
    public static final String CLASS_GENERICCUSTOMER = "GenericCustomer"; //NOI18N
    /**
     * Class service instance
     */
    public static final String CLASS_SERVICE_INSTANCE = "ServiceInstance"; //NOI18N
    /**
     * Class service provider
     */
    public static final String CLASS_SERVICEPROVIDER = "ServiceProvider"; //NOI18N
    /**
     * Class Generic physical connections
     */
    public static final String CLASS_PHYSICALCONNECTION= "GenericPhysicalConnection"; //NOI18N
    /**
     * IP Address
     */
    public static final String CLASS_IP_ADDRESS = "IPAddress"; //NOI18N
    /**
     * Class Subnet IPv4
     */
    public static final String CLASS_SUBNET_IPV4 = "SubnetIPv4"; //NOI18N
    /**
     * Class Subnet IPv6
     */
    public static final String CLASS_SUBNET_IPV6 = "SubnetIPv6"; //NOI18N
    /**
     * Class VLAN
     */
    public static final String CLASS_VLAN = "VLAN"; //NOI18N
     /**
     * Class VRF
     */
    public static final String CLASS_VRF_INSTANCE = "VRFInstance"; //NOI18N
    /**
     * Class Generic Communications Equipment
     */
    public static final String CLASS_GENERICCOMMUNICATIONSELEMENT = "GenericCommunicationsElement";
    /**
     * Class GenericBox
     */
    public static final String CLASS_GENERICBOX = "GenericBox";
    /**
     * Class GenericBox
     */
    public static final String CLASS_GENERICDISTRIBUTIONFRAME = "GenericDistributionFrame";
    /**
     * Class GenericPort
     */
    public static final String CLASS_GENERICPORT = "GenericPort";
    /**
     * Class Generic Contract
     */
    public static final String CLASS_GENERICCONTRACT = "GenericContract"; //NOI18N
    /**
     * Class Generic Project
     */
    public static final String CLASS_GENERICPROJECT = "GenericProject"; //NOI18N
    /**
     * Class Generic Activity
     */
    public static final String CLASS_GENERICACTIVITY = "GenericActivity"; //NOI18N
    /**
     * Class Generic Physical Container
     */
    public static final String CLASS_GENERICPHYSICALCONTAINER = "GenericPhysicalContainer"; //NOI18N
    /**
     * Class Generic Physical Link
     */
    public static final String CLASS_GENERICPHYSICALLINK = "GenericPhysicalLink"; //NOI18N
    /**
     * Dummy root node name
     */
    public static final String NODE_DUMMYROOT = "DummyRoot"; //NOI18N
    /**
     * IPv4 root node name
     */
    public static final String NODE_IPV4ROOT = "IPv4Root"; //NOI18N
    /**
     * IPv6 root node name
     */
    public static final String NODE_IPV6ROOT = "IPv6Root"; //NOI18N
    /**
     * Project root node name
     */
    public static final String NODE_PROJECTROOT = "ProjectRoot"; //NOI18N
    /**
     * General activity log root node name
     */
    public static final String NODE_GENERAL_ACTIVITY_LOG = "GeneralActivityLog"; //NOI18N
    /**
     * Object specific activity log root node name
     */
    public static final String NODE_OBJECT_ACTIVITY_LOG = "ObjectActivityLog"; //NOI18N
    /**
     * Group root node name
     */
    public static final String NODE_GROUPS = "Groups"; //NOI18N
    /**
     * Users root node name
     */
    public static final String NODE_USERS = "Users";
    /**
     * Privileges root node name
     */
    public static final String NODE_PRIVILEGES = "Privilges";
    /**
     * Sync root node name
     */
    public static final String NODE_SYNCGROUPSROOT = "SyncGroupsRoot"; //NOI18N
    /**
     * Date format for queries
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    /**
     * Path for patches files
     */
    public static final String PACTHES_PATH =  "patches/";
    /**
     * Hint in patch file for a cypher query
     */
    public static final String DATABASE_SENTENCE = "!db_sentence";
    
}

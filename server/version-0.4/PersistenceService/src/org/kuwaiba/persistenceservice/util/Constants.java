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

package org.kuwaiba.persistenceservice.util;

/**
 * Misc constants
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Constants {
    /**
     * Class hierarchy XML document version
     */
    public static final String CLASS_HIERARCHY_DOCUMENT_VERSION = "1.0";
    /**
     * Persistence Service version
     */
    public static final String PERSISTENCE_SERVICE_VERSION = "0.4";

    /**
     * Class type for RootObject
     */
    public static final int CLASS_TYPE_ROOT = 0;
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
}

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
}

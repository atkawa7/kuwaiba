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

package org.kuwaiba.util;

import java.io.File;

/**
 * Misc constants
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Constants {
    /**
     * Server version
     */
    public static final String SERVER_VERSION = "0.4";
    /**
     * Max icon size in bytes
     */
    public static final int MAX_ICON_SIZE = 5000;
    /**
     * Max background size in bytes
     */
    public static final int MAX_BACKGROUND_SIZE = 1000000;
    /**
     * Max binary file size in bytes
     */
    public static final int MAX_BINARY_FILE_SIZE = 10000000;
    /**
     * Base URL to retrieve images (mostly view backgrounds)
     */
    public static final String BASE_PATH_FOR_IMAGES = "files"+File.separator+"images"+File.separator+"views"+File.separator;
    /**
     * Name of the validator to indicate if a given class is a connection
     */
    public static String IS_PHYSICAL_CONNECTION_VALIDATOR = "physicalConnection";
    /**
     * Name of the validator to indicate if a given class is the endpoint to a connection
     */
    public static String IS_PHYSICAL_NODE_VALIDATOR = "physicalNode";
    /**
     * Name of the validator to indicate if a given class is the endpoint to a link
     */
    public static String IS_PHYSICAL_ENDPOINT_VALIDATOR = "physicalEndpoint";
    
    /**
     * Constant to identify when a requests refers to all views, no matter the type
     */
    public static int VIEWS_ALL = 0;
    /**
     * Constant to identify when a requests refers to the default object view
     */
    public static int VIEWS_DEFAULT = 1;
    /**
     * Constant to identify when a requests refers to an equipment view
     */
    public static int VIEWS_EQUIPMENT = 2;
    /**
     * Constant to identify when a requests refers to a GIS view
     */
    public static int VIEWS_GIS = 3;
     /**
     * Constant to identify when a requests refers to a Topology view
     */
    public static int VIEWS_TOPOLOGY = 4;

}

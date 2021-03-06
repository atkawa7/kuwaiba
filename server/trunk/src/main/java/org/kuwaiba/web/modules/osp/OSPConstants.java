/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.web.modules.osp;

/**
 * A set of constants (mostly default values) used in the Outside Plant module.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class OSPConstants {
    /**
     * Default map center latitude. This value is used when the configuration variable <code>widgets.simplemap.centerLatitude</code> can not be found or it's not a number.
     */
    public static double DEFAULT_CENTER_LATITUDE = 3.9353255;
    /**
     * Default map center longitude. This value is used when the configuration variable <code>widgets.simplemap.centerLongitude</code> can not be found or it's not a number.
     */
    public static double DEFAULT_CENTER_LONGITUDE = -73.5377146;
    /**
     * Default map center latitude. This value is used when the configuration variable <code>widgets.simplemap.zoom</code> can not be found or it's not a number.
     */
    public static int DEFAULT_ZOOM = 6;
    /**
     * Default map language (English). This language will be used if the configuration variable <code>widgets.simplemap.language</code> could not be found
     */
    public static String DEFAULT_LANGUAGE = "english";
    /**
     * The version of the XML document generated by the as
     */
    public static String VIEW_VERSION = "1.0";
}

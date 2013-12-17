/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */

package org.inventory.communications;

/**
 * This class contains constants shared between the server and the client. This could be replaced in the
 * future with an initial constant exchange to avoid the manual synchronization
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SharedInformation {

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

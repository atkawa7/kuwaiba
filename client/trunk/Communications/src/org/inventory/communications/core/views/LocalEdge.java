/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.communications.core.views;

import java.awt.Point;
import org.inventory.core.services.interfaces.LocalObject;


/**
 * Represents a connection in a view independent from the presentation. This class represents
 * an object to be render, but it's independent from the visual library so it can be rendered using anything
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalEdge {
    /**
     * Some constants
     */
    //TODO: Gotta send this to a config file
    public static String CLASS_WIRECONTAINER="entity.connections.physical.containers.WireContainer";
    public static String CLASS_WIRELESSCONTAINER="entity.connections.physical.containers.WirelessContainer";

    /**
     * Physical connection classes
     */
    public static String CLASS_ELECTRICALLINK = "entity.connections.physical.ElectricalLink";
    public static String CLASS_OPTICALLINK = "entity.connections.physical.OpticalLink";
    public static String CLASS_WIRELESSLINK = "entity.connections.physical.RadioLink";

    /**
     * Physical connection type classes
     */
    public static String CLASS_ELECTRICALLINKTYPE = "entity.multiple.types.links.ElectricalLinkType";
    public static String CLASS_OPTICALLINKTYPE = "entity.multiple.types.links.OpticalLinkType";
    public static String CLASS_WIRELESSLINKTYPE = "entity.multiple.types.links.WirelessLinkType";

    /**
     * Physical container type classes
     */
    public static String CLASS_WIRECONTAINERTYPE = "entity.multiple.types.links.WireContainerType";
    public static String CLASS_WIRELESSCONTAINERTYPE = "entity.multiple.types.links.WirelessContainerType";

    /**
     * Wrapped business object
     */
    private LocalObject object;
    /**
     * Reference to the "a" side of the connection
     */
    private LocalNode aSide;
    /**
     * Reference to the "b" side of the connection
     */
    private LocalNode bSide;
    /**
     * connection className
     */
    private String className;
    /**
     * Control points used to route the connection
     */
    private Point[] controlPoints;

    public LocalEdge(LocalObject _object, Point[] _controlsPoints){
        this.object = _object;
        this.controlPoints = _controlsPoints;
    }

    public LocalEdge(LocalObject _object, LocalNode _aSide, LocalNode _bSide, String _className,Point[] _controlsPoints){
        this (_object,_controlsPoints);
        this.aSide = _aSide;
        this.bSide =_bSide;
        this.className = _className;
    }

    public Point[] getControlPoints() {
        return controlPoints;
    }

    public LocalObject getObject() {
        return object;
    }

    public LocalNode getaSide() {
        return aSide;
    }

    public LocalNode getbSide() {
        return bSide;
    }

    public void setaSide(LocalNode aSide) {
        this.aSide = aSide;
    }

    public void setbSide(LocalNode bSide) {
        this.bSide = bSide;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

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

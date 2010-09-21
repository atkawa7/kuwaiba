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
import org.inventory.core.services.interfaces.LocalObjectLight;

/**
 * Represents a connection in a view independent from the presentation. This class represents
 * an object to be render, but it's independent from the visual library so it can be rendered using anything
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalEdge {
    /**
     * Wrapped business object
     */
    private LocalObjectLight object;
    /**
     * Control points used to route the connection
     */
    private Point[] controlPoints;

    public LocalEdge(LocalObjectLight _object, Point[] _controlsPoints){
        this.object = _object;
        this.controlPoints = _controlsPoints;
    }

    public Point[] getControlPoints() {
        return controlPoints;
    }

    public LocalObjectLight getObject() {
        return object;
    }
}

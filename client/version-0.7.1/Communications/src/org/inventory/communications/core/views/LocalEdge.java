/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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

import org.inventory.communications.core.LocalObjectLight;

/**
 * Represents a connection in a view independent from the presentation. This class represents
 * an object to be render, but it's independent from the visual library so it can be rendered using anything
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalEdge {

    /**
     * Wrapped business object
     */
    private LocalObjectLight object;
    /**
     * Reference to the "a" side of the connection
     */
    private LocalNode aSide;
    /**
     * Reference to the "b" side of the connection
     */
    private LocalNode bSide;
    /**
     * Control points used to route the connection
     */
    private double[][] controlPoints;

    public LocalEdge() {    }

    public LocalEdge(LocalObjectLight obj) {
        this.object = obj;
    }

    public LocalEdge(LocalObjectLight object, double[][] controlsPoints){
        this.object = object;
        this.controlPoints = controlsPoints;
    }

    public LocalEdge(LocalObjectLight object, LocalNode aSide, LocalNode bSide, double[][] controlsPoints){
        this (object,controlsPoints);
        this.aSide = aSide;
        this.bSide =bSide;
    }

    @Override
    public boolean equals (Object obj){
        if (obj == null)
            return false;
        if (!(obj instanceof LocalEdge))
            return false;
        return ((LocalEdge)obj).getObject().getOid() == this.object.getOid();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.object != null ? this.object.hashCode() : 0);
        return hash;
    }
    public double[][] getControlPoints() {
        return controlPoints;
    }

    public LocalObjectLight getObject() {
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

    public void setControlPoints(double[][] controlPoints) {
        this.controlPoints = controlPoints;
    }
}

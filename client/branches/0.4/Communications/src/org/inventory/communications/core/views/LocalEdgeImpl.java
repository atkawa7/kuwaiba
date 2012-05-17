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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.visual.LocalEdge;
import org.inventory.core.services.api.visual.LocalNode;
import org.openide.util.lookup.ServiceProvider;


/**
 * Represents a connection in a view independent from the presentation. This class represents
 * an object to be render, but it's independent from the visual library so it can be rendered using anything
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@ServiceProvider(service=LocalEdge.class)
public class LocalEdgeImpl implements LocalEdge {

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
    private List<Point> controlPoints;

    public LocalEdgeImpl() {    }

    public LocalEdgeImpl(LocalObjectLight obj) {
        this.object = obj;
    }

    public LocalEdgeImpl(LocalObjectLight object, Point[] controlsPoints){
        this.object = object;
        this.controlPoints = new ArrayList<Point>();
        if (controlsPoints != null)
            this.controlPoints.addAll(Arrays.asList(controlsPoints));
    }

    public LocalEdgeImpl(LocalObjectLight object, LocalNode aSide, LocalNode bSide, Point[] controlsPoints){
        this (object,controlsPoints);
        this.aSide = aSide;
        this.bSide =bSide;
    }

    @Override
    public boolean equals (Object obj){
        if (obj == null)
            return false;
        if (!(obj instanceof LocalEdgeImpl))
            return false;
        return ((LocalEdgeImpl)obj).getObject().getOid().equals(this.object.getOid());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.object != null ? this.object.hashCode() : 0);
        return hash;
    }
    public List<Point> getControlPoints() {
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
}

/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
 * Represents a node in a view independent from the presentation. This class represents
 * an object to be render, but it's independent from the visual library so it can be rendered using anything
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalNode {
    /**
     * Wrapped business object
     */
    private LocalObjectLight object;
    /**
     * Node's x position
     */
    private double x;
    /**
     * Node's y position
     */
    private double y;

    public LocalNode(){}

    public LocalNode(LocalObjectLight _object, double x, double y){
        this.object = _object;
        this.x = x;
        this.y = y;
    }

    public LocalObjectLight getObject() {
        return object;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null)
            return false;
        if (!(obj instanceof LocalNode))
            return false;
        return ((LocalNode)obj).getObject().getOid() == this.object.getOid();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.object != null ? this.object.hashCode() : 0);
        return hash;
    }
}

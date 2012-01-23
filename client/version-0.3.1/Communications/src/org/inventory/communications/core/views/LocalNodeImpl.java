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
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.visual.LocalNode;
import org.openide.util.lookup.ServiceProvider;

/**
 * Represents a node in a view independent from the presentation. This class represents
 * an object to be render, but it's independent from the visual library so it can be rendered using anything
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@ServiceProvider(service=LocalNode.class)
public class LocalNodeImpl implements LocalNode{
    /**
     * Wrapped business object
     */
    private LocalObjectLight object;
    /**
     * Node's position
     */
    private Point position;

    public LocalNodeImpl(){}

    public LocalNodeImpl(LocalObjectLight _object, int x, int y){
        this.object = _object;
        this.position = new Point(x, y);
    }

    public LocalObjectLight getObject() {
        return object;
    }

    public Point getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null)
            return false;
        if (!(obj instanceof LocalNodeImpl))
            return false;
        return ((LocalNodeImpl)obj).getObject().getOid().equals(this.object.getOid());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.object != null ? this.object.hashCode() : 0);
        return hash;
    }
}

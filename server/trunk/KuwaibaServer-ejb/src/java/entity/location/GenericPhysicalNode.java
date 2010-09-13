/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package entity.location;

import core.interfaces.PhysicalNode;
import entity.connections.physical.containers.GenericPhysicalContainer;
import entity.core.RootObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

/**
 * This is the superclass for all possible physically connectable objects (using containers)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class GenericPhysicalNode extends RootObject implements PhysicalNode{

     /**
     * This one has all pipes and ducts connected to the node
     */
    @ManyToMany
    protected List<GenericPhysicalContainer> containers;

    @Override
    public void addPhysicalContainers(GenericPhysicalContainer[] _containers) {
        if (containers == null)
            containers = new ArrayList<GenericPhysicalContainer>();
        containers.addAll(Arrays.asList(_containers));
    }

    @Override
    public List<GenericPhysicalContainer> getConnectedPhysicalContainers() {
        return containers;
    }
}

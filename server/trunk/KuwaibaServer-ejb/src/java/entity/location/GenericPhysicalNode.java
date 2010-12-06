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

import core.annotations.NoSerialize;
import core.interfaces.PhysicalNode;
import entity.connections.physical.containers.GenericPhysicalContainer;
import entity.core.AdministrativeItem;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

/**
 * This is the superclass for all possible physically connectable objects (using containers)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class GenericPhysicalNode extends AdministrativeItem implements PhysicalNode{

     /**
     * This one has all pipes and ducts connected to the node
     */
    @OneToMany
    @NoSerialize
    protected List<GenericPhysicalContainer> containers;

    public List<GenericPhysicalContainer> getContainers() {
        return containers;
    }

    public void setContainers(List<GenericPhysicalContainer> containers) {
        this.containers = containers;
    }
}

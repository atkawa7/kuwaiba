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

package entity.connections.physical.containers;

import core.interfaces.PhysicalContainer;
import core.interfaces.PhysicalNode;
import entity.connections.GenericConnection;
import entity.core.ConfigurationItem;
import entity.multiple.types.containers.PhysicalContainerType;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * This class represents a generic physical connection container
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericPhysicalContainer extends GenericConnection implements Serializable,PhysicalContainer {

    @OneToOne
    protected ConfigurationItem nodeA;
    @OneToOne
    protected ConfigurationItem nodeB;
    @OneToOne
    protected PhysicalContainerType type;

    @Override
    public PhysicalNode getNodeA() {
        return (PhysicalNode) nodeA;
    }

    @Override
    public PhysicalNode getNodeB() {
        return (PhysicalNode) nodeB;
    }

    @Override
    public void connectNodeA(PhysicalNode nodeA) {
        this.nodeA = (ConfigurationItem) nodeA;
    }

    @Override
    public void connectNodeB(PhysicalNode nodeB) {
        this.nodeB = (ConfigurationItem) nodeB;
    }

    @Override
    public PhysicalContainerType getType() {
        return type;
    }

}

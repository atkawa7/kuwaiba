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
import entity.multiple.types.containers.PhysicalContainerType;
import entity.adapters.PhysicalContainerNodeAdapter;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * This class represents a generic physical connection container
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericPhysicalContainer extends GenericConnection implements Serializable,PhysicalContainer {

    //If the adapters don't exist, create them
    @OneToOne(cascade=CascadeType.PERSIST)
    protected PhysicalContainerNodeAdapter nodeA;
    @OneToOne(cascade=CascadeType.PERSIST)
    protected PhysicalContainerNodeAdapter nodeB;
    @OneToOne
    protected PhysicalContainerType type;


    public GenericPhysicalContainer(){
        nodeA = new PhysicalContainerNodeAdapter();
        nodeA.setbSide(this.getId());
        nodeA.setbSideClass(this.getClass().getSimpleName());
        nodeB = new PhysicalContainerNodeAdapter();
        nodeB.setbSide(this.getId());
        nodeB.setbSideClass(this.getClass().getSimpleName());
    }

    @Override
    public PhysicalContainerNodeAdapter getNodeA() {
        return nodeA;
    }

    @Override
    public PhysicalContainerNodeAdapter getNodeB() {
        return nodeB;
    }

    @Override
    public void connectNodeA(PhysicalNode _nodeA) {
        this.nodeA.setaSide(_nodeA.getId());
        this.nodeA.setaSideClass(_nodeA.getClass().getSimpleName());
    }

    @Override
    public void connectNodeB(PhysicalNode _nodeB) {
        this.nodeB.setaSide(_nodeB.getId());
        this.nodeB.setaSideClass(_nodeB.getClass().getSimpleName());
    }

    @Override
    public PhysicalContainerType getType() {
        return type;
    }
}

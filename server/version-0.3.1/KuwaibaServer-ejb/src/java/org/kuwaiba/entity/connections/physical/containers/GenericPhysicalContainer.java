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

package org.kuwaiba.entity.connections.physical.containers;

import org.kuwaiba.core.annotations.NoSerialize;
import org.kuwaiba.entity.connections.GenericConnection;
import org.kuwaiba.entity.location.GenericPhysicalNode;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;

/**
 * This class represents a generic physical connection container
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class GenericPhysicalContainer extends GenericConnection {

    @OneToOne
    @NoSerialize
    protected GenericPhysicalNode nodeA;
    @OneToOne
    @NoSerialize
    protected GenericPhysicalNode nodeB;


    public GenericPhysicalNode getNodeA() {
        return nodeA;
    }

    public void setNodeA(GenericPhysicalNode nodeA) {
        this.nodeA = nodeA;
    }

    public GenericPhysicalNode getNodeB() {
        return nodeB;
    }

    public void setNodeB(GenericPhysicalNode nodeB) {
        this.nodeB = nodeB;
    }
}

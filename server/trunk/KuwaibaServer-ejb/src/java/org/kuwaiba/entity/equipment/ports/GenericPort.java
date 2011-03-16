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
 *  under the License.
 */
package org.kuwaiba.entity.equipment.ports;

import org.kuwaiba.core.annotations.NoCopy;
import org.kuwaiba.core.annotations.NoCount;
import org.kuwaiba.core.annotations.NoSerialize;
import org.kuwaiba.entity.connections.physical.GenericPhysicalConnection;
import org.kuwaiba.entity.core.InventoryObject;
import org.kuwaiba.entity.multiple.states.OperationalState;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * Represents a generic Port
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@NoCount
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class GenericPort extends InventoryObject{
    /**
     *The connected cable/fiber/radio channel. Can't use mappedBy here since the
     * connection has two endpoints (A or B) and it's not possible to know which
     * of them will be connected at design time
     */
    @OneToOne
    @NoSerialize
    @NoCopy
    protected GenericPhysicalConnection connectedConnection;
    
    @ManyToOne
    protected OperationalState state;

    public GenericPhysicalConnection getConnectedConnection() {
        return connectedConnection;
    }

    public void setConnectedConnection(GenericPhysicalConnection connectedConnection) {
        this.connectedConnection = connectedConnection;
    }

    public OperationalState getState() {
        return state;
    }

    public void setState(OperationalState state) {
        this.state = state;
    }
}

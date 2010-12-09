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
package entity.equipment.physicallayer.parts.ports;

import core.annotations.NoSerialize;
import core.interfaces.PhysicalEndpoint;
import entity.connections.physical.GenericPhysicalConnection;
import entity.equipment.physicallayer.parts.GenericPart;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;

/**
 * Represents a generic Port
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class GenericPort extends GenericPart implements PhysicalEndpoint {
    /**
     *The connected cable/fiber/radio channel. Can't use mappedBy here since the
     * connection has two endpoints (A or B) and it's not possible to know which
     * of them will be connected at design time
     */
    @OneToOne
    @NoSerialize
    protected GenericPhysicalConnection connectedConnection;

    public GenericPhysicalConnection getConnectedConnection() {
        return connectedConnection;
    }

    public void setConnectedConnection(GenericPhysicalConnection connectedConnection) {
        this.connectedConnection = connectedConnection;
    }
}

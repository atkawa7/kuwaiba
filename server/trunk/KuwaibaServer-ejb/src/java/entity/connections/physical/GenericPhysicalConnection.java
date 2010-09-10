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

package entity.connections.physical;

import core.interfaces.PhysicalConnection;
import core.interfaces.PhysicalEndpoint;
import entity.connections.GenericConnection;
import entity.multiple.types.links.PhysicalLinkType;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * This class represents a generic physical connection
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericPhysicalConnection extends GenericConnection implements Serializable, PhysicalConnection {

    protected PhysicalEndpoint endpointA;
    protected PhysicalEndpoint endpointB;
    @ManyToOne
    protected PhysicalLinkType type;

    @Override
    public PhysicalEndpoint getEndpointA() {
        return endpointA;
    }

    @Override
    public PhysicalEndpoint getEndpointB() {
        return endpointB;
    }

    /**
     * TODO: Is this the best way to disconnect?
     */
    @Override
    public void disconnectPointA() {
        endpointA = null;
    }

    @Override
    public void disconnectPointB() {
        endpointB = null;
    }

    @Override
    public PhysicalLinkType getType() {
        return type;
    }

}

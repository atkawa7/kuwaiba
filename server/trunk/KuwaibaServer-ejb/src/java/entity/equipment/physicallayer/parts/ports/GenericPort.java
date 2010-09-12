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

import core.interfaces.PhysicalConnection;
import core.interfaces.PhysicalEndpoint;
import entity.connections.physical.GenericPhysicalConnection;
import entity.equipment.physicallayer.parts.GenericPart;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * Represents a generic Port
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericPort extends GenericPart implements Serializable,PhysicalEndpoint {

    @OneToMany
    protected List<GenericPhysicalConnection> physicalConnections;

    @Override
    public void addPhysicalConnections(GenericPhysicalConnection[] _connections) {
        if (physicalConnections == null)
            physicalConnections = new ArrayList<GenericPhysicalConnection>();
        physicalConnections.addAll(Arrays.asList(_connections));
    }

    @Override
    public List<GenericPhysicalConnection> getPhysicalConnections() {
        return physicalConnections;
    }

    @Override
    public void removePhysicalConnections(GenericPhysicalConnection[] connections) {
        for (PhysicalConnection pConnection: connections){
            if (pConnection.getEndpointA() != null){
                if (pConnection.getEndpointA().equals(this)){
                    pConnection.disconnectEndpointA();
                    continue;
                }
                if (pConnection.getEndpointB().equals(this)){
                    pConnection.disconnectEndpointB();
                    continue;
                }
            }
        }
    }

}

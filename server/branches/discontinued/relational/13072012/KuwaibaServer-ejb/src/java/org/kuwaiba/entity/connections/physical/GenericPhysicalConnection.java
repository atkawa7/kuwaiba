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

package org.kuwaiba.entity.connections.physical;

import org.kuwaiba.core.annotations.RelatableToService;
import org.kuwaiba.entity.connections.GenericConnection;
import org.kuwaiba.entity.equipment.ports.GenericPort;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import org.kuwaiba.entity.multiple.companies.GenericCompany;

/**
 * This class represents a generic physical connection
 * Note: The endpoints should be PhysicalEndpoints, but since no interfaces can be referenced
 * as fields in an entity class, it's necessary to use GenericPorts
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@RelatableToService
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class GenericPhysicalConnection extends GenericConnection {

    /**
     * This one is the resource owner
     */
    @ManyToOne
    protected GenericCompany owner;
    /**
     * The resource operator
     */
    @ManyToOne
    protected GenericCompany operator;

    @Override
    public GenericPort getEndpointA() {
        return endpointA;
    }

    @Override
    public void setEndpointA(GenericPort endpointA) {
        this.endpointA = endpointA;
    }

    @Override
    public GenericPort getEndpointB() {
        return endpointB;
    }

    @Override
    public void setEndpointB(GenericPort endpointB) {
        this.endpointB = endpointB;
    }

    public GenericCompany getOwner() {
        return owner;
    }

    public void setOwner(GenericCompany owner) {
        this.owner = owner;
    }

    public GenericCompany getOperator() {
        return operator;
    }

    public void setOperator(GenericCompany operator) {
        this.operator = operator;
    }
}

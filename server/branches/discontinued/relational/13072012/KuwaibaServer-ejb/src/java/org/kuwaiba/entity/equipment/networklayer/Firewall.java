/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package org.kuwaiba.entity.equipment.networklayer;

import org.kuwaiba.core.annotations.NoSerialize;
import org.kuwaiba.entity.multiple.software.NetworkService;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

/**
 * A simple firewall
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Entity
public class Firewall extends GenericAppliance {
    /**
     * Firewalls usually run many services like DHCP, routing, DNS, etc
     */
    @ManyToMany
    @NoSerialize //Just for now because we don't have editor for this kind of relationships
    protected List<NetworkService> services;

    public List<NetworkService> getServices() {
        return services;
    }

    public void setServices(List<NetworkService> services) {
        this.services = services;
    }
}

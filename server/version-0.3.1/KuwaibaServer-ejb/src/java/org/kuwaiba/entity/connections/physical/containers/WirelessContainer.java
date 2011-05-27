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

import org.kuwaiba.entity.multiple.types.links.WirelessContainerType;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * This class represents an element to contain wireless connections, such as waveguides.
 * Radio channels can be contained as well
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class WirelessContainer extends GenericPhysicalContainer implements Serializable {
    @ManyToOne
    protected WirelessContainerType type;

    public WirelessContainerType getType() {
        return type;
    }

    public void setType(WirelessContainerType type) {
        this.type = type;
    }
}

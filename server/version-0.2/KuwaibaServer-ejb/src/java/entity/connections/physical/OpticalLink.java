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

import entity.multiple.types.links.OpticalLinkType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * Represents a link using a fiber optics as a transmission medium
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class OpticalLink extends GenericPhysicalConnection{
    @OneToOne
    protected OpticalLinkType type;

    public OpticalLinkType getType() {
        return type;
    }

    public void setType(OpticalLinkType type) {
        this.type = type;
    }
}

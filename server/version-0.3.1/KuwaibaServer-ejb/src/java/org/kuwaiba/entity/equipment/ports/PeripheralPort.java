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

package org.kuwaiba.entity.equipment.ports;

import org.kuwaiba.entity.multiple.types.parts.PeripheralPortType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * A simple port to connect peripherals
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Entity
public class PeripheralPort extends GenericPort {
    @ManyToOne
    protected PeripheralPortType type;

    public PeripheralPortType getType() {
        return type;
    }

    public void setType(PeripheralPortType type) {
        this.type = type;
    }
}

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
package org.kuwaiba.entity.equipment.containers;

import org.kuwaiba.entity.multiple.types.equipment.containers.RackType;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Represents a simple rack
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class Rack extends GenericContainer implements Serializable {
    @Column(length=3)
    protected Integer rackUnits;
    @ManyToOne
    protected RackType type;

    public Integer getRackUnits() {
        return rackUnits;
    }

    public void setRackUnits(Integer rackUnits) {
        this.rackUnits = rackUnits;
    }

    public RackType getType() {
        return type;
    }

    public void setType(RackType type) {
        this.type = type;
    }
}

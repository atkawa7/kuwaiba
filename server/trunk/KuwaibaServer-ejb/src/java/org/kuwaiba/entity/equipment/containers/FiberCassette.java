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

package org.kuwaiba.entity.equipment.containers;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import org.kuwaiba.entity.multiple.types.equipment.containers.FiberCassetteType;


/**
 * A simple fiber cassette used as adapter panels installed with
 * factory terminated MTP multifiber fanouts
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Entity
public class FiberCassette extends GenericContainer{
    @ManyToOne
    protected FiberCassetteType type;

    public FiberCassetteType getType() {
        return type;
    }

    public void setType(FiberCassetteType type) {
        this.type = type;
    }
}
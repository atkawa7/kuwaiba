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

import org.kuwaiba.entity.multiple.types.equipment.containers.DistributionFrameType;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Represents a distribution frame with optical ports
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class ODF extends GenericDistributionFrame implements Serializable {
    @ManyToOne
    protected DistributionFrameType type;

    public DistributionFrameType getType() {
        return type;
    }

    public void setType(DistributionFrameType type) {
        this.type = type;
    }
}

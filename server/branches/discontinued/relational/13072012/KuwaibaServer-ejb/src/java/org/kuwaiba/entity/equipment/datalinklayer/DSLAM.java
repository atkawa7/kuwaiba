/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package org.kuwaiba.entity.equipment.datalinklayer;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import org.kuwaiba.entity.multiple.types.equipment.DSLAMType;


/**
 * A Digital Subscriber Line Access Multiplexer
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Entity
public class DSLAM extends GenericDataLinkElement implements Serializable {
    @ManyToOne
    protected DSLAMType type;

    public DSLAMType getType() {
        return type;
    }

    public void setType(DSLAMType type) {
        this.type = type;
    }
}

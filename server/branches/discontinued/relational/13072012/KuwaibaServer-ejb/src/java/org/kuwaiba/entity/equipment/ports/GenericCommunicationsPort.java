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

import org.kuwaiba.entity.multiple.types.parts.CommunicationsPortType;
import org.kuwaiba.core.annotations.RelatableToService;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

/**
 * Represents a port used for communication equipment to send/receive data
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@RelatableToService
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class GenericCommunicationsPort extends GenericPort {

    @ManyToOne
    protected CommunicationsPortType type; //RJ-45, RJ-11, FC/PC, etc

    public CommunicationsPortType getType() {
        return type;
    }

    public void setType(CommunicationsPortType type) {
        this.type = type;
    }
}

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
 *  under the License.
 */
package entity.equipment.physicallayer.parts.ports;

import entity.equipment.physicallayer.parts.GenericPart;
import entity.multiple.types.parts.PortType;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Represents a generic Port
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public abstract class GenericPort extends GenericPart implements Serializable {

    //private boolean vendor; //Reuse the field as private to hide it. Uses a boolean to save diskspace
    //private boolean conditions; //same here
    @ManyToOne
    protected PortType connector; //RJ-45, RJ-11, FC/PC, etc

    public PortType getConnector() {
        return connector;
    }

    public void setConnector(PortType connector) {
        this.connector = connector;
    }
}

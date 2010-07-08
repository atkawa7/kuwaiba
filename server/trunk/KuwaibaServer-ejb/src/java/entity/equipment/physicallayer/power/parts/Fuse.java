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

package entity.equipment.physicallayer.power.parts;

import java.io.Serializable;
import javax.persistence.Entity;


/**
 * Represents a simple fuse
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class Fuse extends GenericPowerPart implements Serializable {
    protected Float breakingCapacity; //Maximum approved current at rated voltage
    protected Float currentRating; //Nominal amperage value of the fuse
    protected Float voltageRating; //Voltage in which the fuse safely inturrupt its rated short circuit current
    protected Float resistance; //Resistance offered by the fuse runniing a current equal to the currentRating

    public Float getBreakingCapacity() {
        return breakingCapacity;
    }

    public void setBreakingCapacity(Float breakingCapacity) {
        this.breakingCapacity = breakingCapacity;
    }

    public Float getCurrentRating() {
        return currentRating;
    }

    public void setCurrentRating(Float currentRating) {
        this.currentRating = currentRating;
    }

    public Float getResistance() {
        return resistance;
    }

    public void setResistance(Float resistance) {
        this.resistance = resistance;
    }

    public Float getVoltageRating() {
        return voltageRating;
    }

    public void setVoltageRating(Float voltageRating) {
        this.voltageRating = voltageRating;
    }
}

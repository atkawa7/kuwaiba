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
 * Represents a simple rectifier (converts AC to DC)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class Rectifier extends GenericPowerPart implements Serializable {
    protected Float inputVoltage; //Volts
    protected Float outputVoltage; //Volts

    public Float getInputVoltage() {
        return inputVoltage;
    }

    public void setInputVoltage(Float inputVoltage) {
        this.inputVoltage = inputVoltage;
    }

    public Float getOutputVoltage() {
        return outputVoltage;
    }

    public void setOutputVoltage(Float outputVoltage) {
        this.outputVoltage = outputVoltage;
    }
}

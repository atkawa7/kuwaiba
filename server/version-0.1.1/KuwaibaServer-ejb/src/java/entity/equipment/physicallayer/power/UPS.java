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

package entity.equipment.physicallayer.power;

import entity.multiple.types.parts.PowerConnectorType;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;


/**
 * Represents an UPS
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
public class UPS extends GenericPowerElement implements Serializable {
    protected Float nominalInputVoltage; //Volts
    protected Float inputFrecuency; //Hertz
    protected Float outputCapacity; //Watts
    protected Float nominalOutputVoltage; //Volts
    protected Float outputFrecuency; //Hertz
    protected Float cordLenght; //Meters
    @ManyToMany
    protected List<PowerConnectorType> inputConnectors;
    @ManyToMany
    protected List<PowerConnectorType> outputConnectors;

    public Float getCordLenght() {
        return cordLenght;
    }

    public void setCordLenght(Float cordLenght) {
        this.cordLenght = cordLenght;
    }

    public List<PowerConnectorType> getInputConnectors() {
        return inputConnectors;
    }

    public void setInputConnectors(List<PowerConnectorType> inputConnectors) {
        this.inputConnectors = inputConnectors;
    }

    public Float getInputFrecuency() {
        return inputFrecuency;
    }

    public void setInputFrecuency(Float inputFrecuency) {
        this.inputFrecuency = inputFrecuency;
    }

    public Float getNominalInputVoltage() {
        return nominalInputVoltage;
    }

    public void setNominalInputVoltage(Float nominalInputVoltage) {
        this.nominalInputVoltage = nominalInputVoltage;
    }

    public Float getNominalOutputVoltage() {
        return nominalOutputVoltage;
    }

    public void setNominalOutputVoltage(Float nominalOutputVoltage) {
        this.nominalOutputVoltage = nominalOutputVoltage;
    }

    public Float getOutputCapacity() {
        return outputCapacity;
    }

    public void setOutputCapacity(Float outputCapacity) {
        this.outputCapacity = outputCapacity;
    }

    public List<PowerConnectorType> getOutputConnectors() {
        return outputConnectors;
    }

    public void setOutputConnectors(List<PowerConnectorType> outputConnectors) {
        this.outputConnectors = outputConnectors;
    }

    public Float getOutputFrecuency() {
        return outputFrecuency;
    }

    public void setOutputFrecuency(Float outputFrecuency) {
        this.outputFrecuency = outputFrecuency;
    }
}

/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.entity.logical.misc;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import org.kuwaiba.entity.logical.GenericLogicalElement;
import org.kuwaiba.entity.multiple.software.VirtualMachineType;


/**
 * A virtual machine (for OS level virtualization)
 * @author Charles Edward bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Entity
public class VirtualMachine extends GenericLogicalElement{
    /**
     * Available hard disk space
     */
    protected Float hardDisk;
    /**
     * Available RAM
     */
    protected Integer ram;
    /**
     * Virtual machine type (VMWare, VirtualBox, etc.). It could also indicate the type of virtualization technique
     */
    @ManyToOne
    protected VirtualMachineType type;

    public Float getHardDisk() {
        return hardDisk;
    }

    public void setHardDisk(Float hardDisk) {
        this.hardDisk = hardDisk;
    }

    public Integer getRam() {
        return ram;
    }

    public void setRam(Integer ram) {
        this.ram = ram;
    }

    public VirtualMachineType getType() {
        return type;
    }

    public void setType(VirtualMachineType type) {
        this.type = type;
    }
}

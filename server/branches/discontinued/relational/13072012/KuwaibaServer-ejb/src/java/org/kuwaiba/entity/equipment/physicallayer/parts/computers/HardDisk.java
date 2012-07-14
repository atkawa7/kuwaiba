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

package org.kuwaiba.entity.equipment.physicallayer.parts.computers;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import org.kuwaiba.entity.multiple.types.parts.HardDiskType;

/**
 * A simple hard disk
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Entity
public class HardDisk extends GenericComputerPart {
    /**
     * Total disk space
     */
    protected Float diskSpace;
    /**
     * Free disk space (GB)
     */
    protected Float freeSpace;
    /**
     * SSD, spinning disks. You can also put the technology here (IDE, SATA, etc)
     */
    @ManyToOne
    protected HardDiskType type;
    /**
     * Is this a external or internal disk?
     */
    protected Boolean internal;

    public Float getDiskSpace() {
        return diskSpace;
    }

    public void setDiskSpace(Float diskSpace) {
        this.diskSpace = diskSpace;
    }

    public Float getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(Float freeSpace) {
        this.freeSpace = freeSpace;
    }

    public Boolean isInternal() {
        return internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    public HardDiskType getType() {
        return type;
    }

    public void setType(HardDiskType type) {
        this.type = type;
    }


}

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

package entity.equipment.applicationlayer;

import entity.multiple.types.equipment.PrinterType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * A simple printer
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Entity
public class Printer extends GenericApplicationElement {
    /**
     * Laser, cartridge, etc
     */
    @ManyToOne
    protected PrinterType type;
    /**
     * Use this attribute to show how much ink/toner left in this printer. Use it
     * as a score, i.e. 1 is low, and 5 is full
     */
    protected Integer inkLevel;

    public Integer getInkLevel() {
        return inkLevel;
    }

    public void setInkLevel(Integer inkLevel) {
        this.inkLevel = inkLevel;
    }

    public PrinterType getType() {
        return type;
    }

    public void setType(PrinterType type) {
        this.type = type;
    }
}

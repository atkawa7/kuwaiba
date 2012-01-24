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

package org.kuwaiba.entity.equipment.containers;

import javax.persistence.Entity;


/**
 * A simple indoors cabinet (an enclosure system).
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Entity
public class Cabinet extends GenericContainer{
    /**
     * The actual available width (there's some space between the total width and the insides)
     */
    protected Float mountingWidth;

    /**
     * Is the current object mounted on a wall?
     */
    protected Boolean mountedInWall = false;

    public Float getMountingWidth() {
        return mountingWidth;
    }

    public void setMountingWidth(Float mountingWidth) {
        this.mountingWidth = mountingWidth;
    }

    public Boolean isMountedInWall() {
        return mountedInWall;
    }

    public void setMountedInWall(Boolean isMountedInWall) {
        this.mountedInWall = isMountedInWall;
    }
}
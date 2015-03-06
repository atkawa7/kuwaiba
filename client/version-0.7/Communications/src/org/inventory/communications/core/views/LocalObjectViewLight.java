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

package org.inventory.communications.core.views;

/**
 * Light version of LocalObjectView. Reference implementation
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalObjectViewLight {

    /**
     * Id for a default view
     */
    public static final int TYPE_DEFAULT = 0;
    /**
     * Id for a view used for racks
     */
    public static final int TYPE_RACK = 1;
    /**
     * Id for a view used in equipment with slots and boards
     */
    public static final int TYPE_EQUIPMENT = 2;
    /**
     * A GIS view
     */
    public static final int TYPE_GIS = 3;
    /**
     * A Topology view
     */
    public static final int TYPE_TOPOLOGY = 4;

    /**
     * View id
     */
    protected long id;
    /**
     * View name
     */
    protected String name;
    /**
     * View description
     */
    protected String description;
    /**
     * View type
     */
    protected int type;

    public LocalObjectViewLight(long id, String name, String description, int type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return name;
    }
}

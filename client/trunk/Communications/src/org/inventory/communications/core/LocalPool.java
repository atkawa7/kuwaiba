/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.communications.core;

import java.awt.datatransfer.DataFlavor;

/**
 * A local representation of a pool (a place where you put similar objects)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalPool extends LocalObjectLight { 
    /**
     * Pool data flavor
     */
    private static final DataFlavor POOL_DATA_FLAVOR = new DataFlavor(LocalPool.class,"Object/LocalPool");
    /**
     * Pool description
     */
    private String description;
    private int type;

    public LocalPool(long oid, String name, String className, String description, int type) {
        this.oid = oid;
        this.name = name;
        this.className = className;
        this.description = description;
        this.type = type;
        DATA_FLAVOR = POOL_DATA_FLAVOR;
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
    
    public int compareTo(LocalPool o) {
        return getName().compareTo(o.getName());
    }
}
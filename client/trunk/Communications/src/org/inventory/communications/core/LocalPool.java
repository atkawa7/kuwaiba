/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
 * A local representation of a pool (a place where you put objects of a certain kind)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LocalPool extends LocalObjectLight {
    /**
     * Type of pool general purpose. These pools are not linked to any particular model
     */
    public static final int POOL_TYPE_GENERAL_PURPOSE = 1;
    /**
     * Type of pool module root. These pools are used in models and are the root of such model
     */
    public static final int POOL_TYPE_MODULE_ROOT = 2;
    /**
     * Type of pool module component. These pools are used in models and are in the lower levels of the pool containment hierarchy
     */
    public static int POOL_TYPE_MODULE_COMPONENT = 3;
    /**
     * Pool data flavor
     */
    private static final DataFlavor POOL_DATA_FLAVOR = new DataFlavor(LocalPool.class,"Object/LocalPool");
    /**
     * Pool description
     */
    private String description;
    private int type;

    public LocalPool(String oid, String name, String className, String description, int type) {
        this.id = oid;
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
    
    @Override
    public String toString() {
        return getName() +" [Pool of " + getClassName() + "]";
    }
}
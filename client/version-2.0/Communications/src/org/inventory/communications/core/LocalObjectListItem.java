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

import org.inventory.communications.util.Constants;

/**
 * This is a local representation of an element within a list (enumerations and so on)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LocalObjectListItem extends LocalObjectLight {
    /**
     * Id for null values
     */
    public static final String NULL_ID = "0";
    private String displayName;

    public LocalObjectListItem() {
        this.id = NULL_ID;
        this.className = "";
        this.name = "None";
    }

    /**
     * Used to create simple items at runtime
     * @param id
     * @param className
     * @param name
     */
    public LocalObjectListItem(String id, String className, String name){
        super(id, name, className);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString(){
        if (this.displayName != null && !this.displayName.isEmpty())
            return this.displayName;
        if (this.name != null && !this.name.isEmpty())
            return this.name;
        return Constants.LABEL_NONAME;
    }
}

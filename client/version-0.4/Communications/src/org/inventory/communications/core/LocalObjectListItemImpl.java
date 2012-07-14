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
package org.inventory.communications.core;

import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.LocalObjectListItem;
import org.openide.util.lookup.ServiceProvider;

/**
 * This is a local representation of an element within a list (enumerations and so on)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@ServiceProvider(service=LocalObjectListItem.class)
public class LocalObjectListItemImpl extends LocalObjectLightImpl implements LocalObjectListItem{

    String displayName;

    public LocalObjectListItemImpl(){
        oid = LocalObjectListItem.NULL_ID;
    }

    public LocalObjectListItemImpl(LocalObjectLight lol){
        this.oid = lol.getOid();
        this.className = lol.getClassName();
        this.name = lol.getName();
    }

    /**
     * Used to create simple items at runtime
     * @param _id
     * @param _className
     * @param _name
     */
    public LocalObjectListItemImpl(Long id, String className,String name){
        this.oid = id;
        this.className = className;
        this.name = name;
    }

    public Long getId() {
        return oid;
    }

    public void setId(Long id) {
        this.oid = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString(){
        if (this.displayName != null)
            if (!this.displayName.trim().equals(""))
                return this.displayName;
        return this.name;
    }
}

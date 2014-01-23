/*
 *  Copyright 2010 - 2013 Neotropic SAS <contact@neotropic.co>.
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

/**
 * This is a local representation of an element within a list (enumerations and so on)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalObjectListItem extends LocalObjectLight {
    //id for null values
    public static final long NULL_ID = 0;
    private String displayName;

    public LocalObjectListItem(){
        this.oid = NULL_ID;
        this.setName("None");
    }

    public LocalObjectListItem(LocalObjectLight lol){
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
    public LocalObjectListItem(long id, String className,String name){
        this.oid = id;
        this.className = className;
        this.name = name;
    }

    public long getId() {
        return oid;
    }

    public void setId(long id) {
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

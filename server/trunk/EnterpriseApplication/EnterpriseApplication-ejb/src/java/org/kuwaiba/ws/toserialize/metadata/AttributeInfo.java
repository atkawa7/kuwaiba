/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.ws.toserialize.metadata;

import java.io.Serializable;

/**
 * This is a wrapper class for AttributeMetadata, containing the info required for the clients
 * to render the object attributes in the right way
 *
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class AttributeInfo implements Serializable {

    /**
     * Attribute's id
     */
    private long id;
    /**
     * Attribute's name
     */
    private String name;
    /**
     * Attribute's display name
     */
    private String displayName;
    /**
     * Attribute's type
     */
    private String type;
    /**
     * Flag to mark an attribute to be used for administrative purposes (beyond the operational inventory)
     */
    private boolean administrative;
    /**
     * Attribute's visibility
     */
    private boolean visible;
     /**
     * Marks the attribute as read only
     */
    private boolean readOnly;
    /**
     * Marks the attribute as unique
     */
    private boolean unique;
    /**
     * Attribute's short description
     */
    private String description;
    /**
     * Indicates how this attribute should be mapped (into a primitive type, a relationship, etc)
     */
    private int mapping;
    /**
     * Indicates if an attribute is copy when the copy/paste is made
     */
    private boolean noCopy;
    /**
     * Cannot change or delete a locked attribute
     */
    private boolean locked;

    public AttributeInfo(String name, String displayName, String type, 
            boolean administrative, boolean visible, String description) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.administrative = administrative;
        this.visible = visible;
        this.description = description;
    }
    
    public AttributeInfo(String name, String displayName, String type, 
                         boolean administrative, boolean visible, 
                         boolean readOnly, boolean unique, String description, 
                         boolean noCopy) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.administrative = administrative;
        this.visible = visible;
        this.readOnly = readOnly;
        this.unique = unique;
        this.description = description;
        this.noCopy = noCopy;
    }

    public boolean isAdministrative() {
        return administrative;
    }

    public void setAdministrative(boolean administrative) {
        this.administrative = administrative;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMapping() {
        return mapping;
    }

    public void setMapping(int mapping) {
        this.mapping = mapping;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isNoCopy() {
        return noCopy;
    }

    public void setNoCopy(boolean noCopy) {
        this.noCopy = noCopy;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}

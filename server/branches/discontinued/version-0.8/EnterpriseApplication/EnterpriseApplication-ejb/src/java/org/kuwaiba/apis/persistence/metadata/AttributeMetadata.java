/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>
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

package org.kuwaiba.apis.persistence.metadata;

import java.io.Serializable;

/**
 * Contains the detailed metadata information about a class attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class AttributeMetadata implements Serializable{

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
    private Boolean administrative;
    /**
     * Should this be shown or hidden
     */
    private Boolean visible;
    /**
     * Attribute's short description
     */
    private String description;
    /**
     * Marks the attribute as unique
     */
    private Boolean unique;
    /**
     * Attribute's creation Date
     */
    private long creationDate;
    /**
     * Indicates if an attribute could be copy in the copy/paste operation
     */
    private Boolean noCopy;
    /**
     * Marks the attribute as read only
     */
    private Boolean readOnly;
    /**
     * Cannot change or delete a locked attribute
     */
    private Boolean locked;

    // <editor-fold defaultstate="collapsed" desc="getters and setters methods. Click on the + sign on the left to edit the code.">
    public Boolean isAdministrative() {
        return administrative;
    }

    public void setAdministrative(Boolean administrative) {
        this.administrative = administrative;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean isVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean isUnique() {
        return unique;
    }

    public void setUnique(Boolean unique) {
        this.unique = unique;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean isNoCopy() {
        return noCopy;
    }

    public void setNoCopy(Boolean noCopy) {
        this.noCopy = noCopy;
    }
    
    public Boolean isLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }
    // </editor-fold>
    
    @Override
    public boolean equals(Object obj){
        if (obj == null)
            return false;
        if (!(obj instanceof AttributeMetadata))
            return false;
        
        return this.getId() == ((AttributeMetadata)obj).getId();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
    
    /**
     * Checks if a given type is primitive (String, Integer, Float, Long, Boolean, Date or Timestamp)
     * @param type The type to be matched
     * @return true if the given type is primitive, false otherwise
     */
    public static boolean isPrimitive(String type){
        if (type.equals("String") || type.equals("Integer") || type.equals("Float") 
                || type.equals("Long") || type.equals("Boolean") || type.equals("Date")
                 || type.equals("Timestamp"))
            return true;
        return false;
    }
}

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

package org.kuwaiba.apis.persistence.metadata;

import java.io.Serializable;

/**
 * Contains the detailed metadata information about a class attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class AttributeMetadata implements Serializable{

    /**
     * Integer, Float, Long, Boolean, String or Text
     */
    public static final int MAPPING_PRIMITIVE = 1;
    /**
     * Dates
     */
    public static final int MAPPING_DATE = 2;
    /**
     * Timestamp
     */
    public static final int MAPPING_TIMESTAMP = 3;
    /**
     * Binary
     */
    public static final int MAPPING_BINARY = 4;
    /**
     * Many to one relationship (such as types)
     */
    public static final int MAPPING_MANYTOONE = 5;
    /**
     * Many to Many relationship (such as accountable persons for a given equipment)
     */
    public static final int MAPPING_MANYTOMANY = 6;
    /**
     * Attribute's id
     */
    private Long id;
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
     * Should this be shown or hidden
     */
    private boolean visible;
    /**
     * Attribute's short description
     */
    private String description;
    /**
     * Indicates how this attribute should be mapped (into a primitive type, a relationship, etc)
     */
    private Integer mapping;
    /**
     * Marks the attribute as read only
     */
    private boolean readOnly;
    /**
     * Marks the attribute as unique
     */
    private boolean unique;
    /**
     * Attribute's creation Date
     */
    private Long creationDate;
    /**
     *
     */
    private boolean noCopy;
    /**
     *
     */
    private boolean noSerialize;

    // <editor-fold defaultstate="collapsed" desc="getters and setters methods. Click on the + sign on the left to edit the code.">
    public boolean isAdministrative() {
        return administrative;
    }

    public void setAdministrative(boolean administrative) {
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

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setMapping(int mapping) {
        this.mapping = mapping;
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

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }

    public Integer getMapping() {
        return mapping;
    }

    public void setMapping(Integer mapping) {
        this.mapping = mapping;
    }

    public boolean isNoCopy() {
        return noCopy;
    }

    public void setNoCopy(boolean noCopy) {
        this.noCopy = noCopy;
    }

    public boolean isNoSerialize() {
        return noSerialize;
    }

    public void setNoSerialize(boolean noSerialize) {
        this.noSerialize = noSerialize;
    }// </editor-fold>
    
}

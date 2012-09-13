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

package org.kuwaiba.ws.toserialize.metadata;

/**
 * This is a wrapper class for AttributeMetadata, containing the info required for the clients
 * to render the object attributes in the right way
 *
 * @author Adrian Martinez Molina <adrian.martinez@gmail.com>
 */
public class AttributeInfo {

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
    private Boolean administrative;
    /**
     * Attribute's visibility
     */
    private Boolean visible;
     /**
     * Marks the attribute as read only
     */
    private Boolean readOnly;
    /**
     * Marks the attribute as unique
     */
    private Boolean unique;
    /**
     * Attribute's short description
     */
    private String description;
    /**
     * Indicates how this attribute should be mapped (into a primitive type, a relationship, etc)
     */
    private Integer mapping;

    public AttributeInfo(String name, String displayName, String type, 
                         Boolean administrative, Boolean visible,
                         String description, Integer mapping) {
        this.name = name;
        this.displayName = displayName;
        this.type = type;
        this.administrative = administrative;
        this.visible = visible;
        this.description = description;
        this.mapping = mapping;
    }

    public Boolean isAdministrative() {
        return administrative;
    }

    public void setAdministrative(Boolean administrative) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMapping() {
        return mapping;
    }

    public void setMapping(Integer mapping) {
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

    public Boolean isVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Boolean isUnique() {
        return unique;
    }

    public void setUnique(Boolean unique) {
        this.unique = unique;
    }
    
}

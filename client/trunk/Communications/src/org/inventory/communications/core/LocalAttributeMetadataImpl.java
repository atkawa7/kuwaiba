/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.inventory.communications.core;

import org.inventory.core.services.interfaces.LocalAttributeMetadata;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalAttributeMetadataImpl
        implements LocalAttributeMetadata{
    private String name;
    private String type;
    private String displayName;
    private Boolean isVisible;
    private Boolean isAdministrative;
    private String description;

    public LocalAttributeMetadataImpl(){}
    public LocalAttributeMetadataImpl(String name, String type, String displayName,
            Boolean isVisible, Boolean isAdministrative, String description){
        this.name = name;
        this.type = type;
        this.displayName = displayName;
        this.isVisible = isVisible;
        this.isAdministrative = isAdministrative;
        this.description = description;
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

    public Boolean getIsAdministrative() {
        return isAdministrative;
    }

    public void setIsAdministrative(Boolean isAdministrative) {
        this.isAdministrative = isAdministrative;
    }

    public Boolean getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
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
}

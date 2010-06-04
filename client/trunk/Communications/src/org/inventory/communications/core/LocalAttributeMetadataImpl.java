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

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import org.inventory.core.services.interfaces.LocalAttributeMetadata;
import org.inventory.core.services.interfaces.LocalObjectListItem;
import org.inventory.core.services.utils.Utils;

/**
 * Represents the metadata associated to a single attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalAttributeMetadataImpl
        implements LocalAttributeMetadata{
    private String name;
    private Long id;
    private Class type;
    private String displayName;
    private Boolean isVisible;
    private Boolean isAdministrative;
    private String description;

    private String listAttributeClassName = null;

    public LocalAttributeMetadataImpl(){}
    public LocalAttributeMetadataImpl(String _name, String _type, String _displayName,
            Boolean _isVisible, Boolean _isAdministrative, String _description){
        this.name = _name;
        this.type = Utils.getRealType(_type);
        this.displayName = _displayName;
        this.isVisible = _isVisible;
        this.isAdministrative = _isAdministrative;
        this.description = _description;

        if (this.type.equals(LocalObjectListItem.class))
            listAttributeClassName = _type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName.equals("")?name:displayName;
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

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    /*
     * If this is a list type attribute, returns the class name associated to yhe item
     */
    public String getListAttributeClassName(){
        return listAttributeClassName;
    }

    public Long getId(){
        return id;
    }

    public void setId(Long _id){
        this.id =_id;
    }
}

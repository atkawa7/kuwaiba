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
 *  under the License.
 */
package org.inventory.communications.core;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import org.inventory.core.services.interfaces.LocalAttributeMetadata;
import org.inventory.core.services.interfaces.LocalObjectListItem;

/**
 * Represents the metadata associated to a single attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalAttributeMetadataImpl
        implements LocalAttributeMetadata{
    private String name;
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
        this.type = getRealType(_type);
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
     * Finds the real type for a given type provided as a string
     * Possible types:
     * -A string --> String
     * -A boolean --> Boolean
     * -A number --> Float, Integer, Long
     * -A Date --> Date, Time, Timestamp(?) --> Check this possibilities in the server
     * -A reference to any other object --> LocalObjectListItem
     *
     * If you're porting the client to other language you should map the types
     * as supported by such language.
     */
    public static Class getRealType(String typeAsString){
        if (typeAsString.equals("String"))
            return String.class;
        if (typeAsString.equals("Integer"))
            return Integer.class;
        if (typeAsString.equals("Float"))
            return Float.class;
        if (typeAsString.equals("Long"))
            return Long.class;
        if (typeAsString.equals("Date"))
            return Date.class;
        if (typeAsString.equals("Time"))
            return Time.class;
        if (typeAsString.equals("Timestamp"))
            return Timestamp.class;
        if (typeAsString.equals("Boolean"))
            return Boolean.class;
        else
            return LocalObjectListItem.class;
    }

    /*
     * If this is a list type attribute, returns the class name associated to yhe item
     */
    public String getListAttributeClassName(){
        return listAttributeClassName;
    }
}

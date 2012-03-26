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

import org.inventory.core.services.api.metadata.LocalAttributeMetadata;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.utils.Utils;
import org.openide.util.lookup.ServiceProvider;

/**
 * Represents the metadata associated to a single attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@ServiceProvider(service=LocalAttributeMetadata.class)
public class LocalAttributeMetadataImpl
        implements LocalAttributeMetadata{
    private String name;
    private Long id;
    private Class type;
    private String displayName;
    private Boolean isVisible;
    private Integer mapping;
    private String description;

    private String listAttributeClassName = null;

    public LocalAttributeMetadataImpl(){}
    public LocalAttributeMetadataImpl(Long oid, String _name, String _type, String _displayName,
            Boolean _isVisible, Integer mapping, String _description){
        this.id = oid;
        this.name = _name;
        this.type = Utils.getRealType(_type);
        this.displayName = _displayName;
        this.isVisible = _isVisible;
        this.mapping = mapping;
        this.description = _description;

        if (this.type.equals(LocalObjectLight.class))
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

    public Boolean isVisible() {
        return isVisible;
    }

    public void setVisible(Boolean isVisible) {
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
     * If this is a list type attribute, returns the class name associated to the item
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

    public Integer getMapping() {
        return mapping;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null)
            return false;
        if (!(obj instanceof LocalAttributeMetadata))
            return false;
        return this.getId().longValue() == ((LocalAttributeMetadata)obj).getId().longValue();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}

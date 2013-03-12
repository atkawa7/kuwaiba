/*
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
package org.inventory.communications.core;

import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.metadata.LocalAttributeMetadata;
import org.inventory.core.services.utils.Utils;
import org.openide.util.lookup.ServiceProvider;

/**
 * Represents the metadata associated to a single attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service=LocalAttributeMetadata.class)
public class LocalAttributeMetadataImpl
        implements LocalAttributeMetadata{
    private String name;
    private long id;
    private Class type;
    private String displayName;
    private boolean isVisible;
    private int mapping;
    private String description;

    private String listAttributeClassName = null;

    public LocalAttributeMetadataImpl(){
        this.displayName = "";
    }
    public LocalAttributeMetadataImpl(long oid, String _name, String _type, String _displayName,
            boolean _isVisible, Integer mapping, String _description){
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

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDisplayName() {
        return displayName.equals("") ? name : displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Class getType() {
        return type;
    }

    @Override
    public void setType(Class type) {
        this.type = type;
    }

    /*
     * If this is a list type attribute, returns the class name associated to the item
     */
    @Override
    public String getListAttributeClassName(){
        return listAttributeClassName;
    }

    @Override
    public long getId(){
        return id;
    }

    @Override
    public void setId(long _id){
        this.id =_id;
    }

    @Override
    public int getMapping() {
        return mapping;
    }

    @Override
    public void setMapping(int mapping){
        this.mapping = mapping;
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null)
            return false;
        if (!(obj instanceof LocalAttributeMetadata))
            return false;
        return this.getId() == ((LocalAttributeMetadata)obj).getId();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

}

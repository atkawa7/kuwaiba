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
package entity.core.metamodel;

import entity.core.MetadataObject;
import entity.multiple.GenericObjectList;
import java.lang.reflect.Field;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.metamodel.Attribute;
import util.HierarchyUtils;

/**
 * Represents an attribute metadata information for each. It's used for mapping and documentation purposes
 * TODO: How should we handle the inherited attributes? so far it's storing them as well as the ones in the super classes
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@NamedQuery(name="flushAttributeMetadata", query="DELETE FROM AttributeMetadata x")
public class AttributeMetadata extends MetadataObject {
    
    @Column(nullable=false)
    private String type=""; //NOI18N
    private String displayName=""; //NOI18N
    /**
     * Mark this attribute as administrative (stuff like id or parent)
     */
    @Column(nullable=false)
    private Boolean isAdministrative=false;
    /**
     * Is this attribute a basic type (int, string, etc) or a list type
     */
    @Column(nullable=false,updatable=false)
    private Boolean isMultiple = false;
     /**
      * Should this be shown or hidden
      */
    @Column(nullable=false)
    private Boolean isVisible=true;
    /**
     * Attribute description. Used for documentation purposes
     */
    private String description;
    /**
     * Is this attribute read only?
     */
    private Boolean isReadOnly;

    public AttributeMetadata(){} //Required
    public AttributeMetadata(String _name, String _type, String _displayName,
            Boolean _isAdministrative, Boolean _isVisible, Boolean _isMultiple,
            Boolean _isReadonly, String _description){
        this.name = _name;
        this.type= _type;
        this.displayName = _displayName;
        this.isAdministrative = _isAdministrative;
        this.isVisible = _isVisible;
        //Should this be taken from the class metadata?
        this.isMultiple = _isMultiple;
        this.isReadOnly = _isReadonly;
        this.description = _description;
    }

    public AttributeMetadata(Attribute att){
        this.name = att.getName();
        this.type= att.getJavaType().getSimpleName();

        if (HierarchyUtils.isSubclass(att.getJavaType(), GenericObjectList.class))
                this.isMultiple = true;
        this.description = "Attribute "+this.name;
    }

    public AttributeMetadata(Field att){
        this.name = att.getName();
        this.type= att.getType().getSimpleName();
        if (HierarchyUtils.isSubclass(att.getType(), GenericObjectList.class))
                this.isMultiple = true;
        this.description = "Attribute "+this.name;
    }

    public Boolean isAdministrative() {
        return isAdministrative;
    }

    public void setAdministrative(Boolean isAdministrative) {
        this.isAdministrative = isAdministrative;
    }

    public Boolean isVisible() {
        return isVisible;
    }

    public void setVisible(Boolean isVisible) {
        this.isVisible = isVisible;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean isMultiple() {
        return isMultiple;
    }

    public void setMultiple(Boolean isMultiple) {
        this.isMultiple = isMultiple;
    }

    public Boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(Boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    @Override
    public String toString() {
        return getName();
    }

}

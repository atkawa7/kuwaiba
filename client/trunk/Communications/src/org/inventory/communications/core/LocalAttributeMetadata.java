/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;

/**
 * Represents the metadata associated to a single attribute
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalAttributeMetadata {

    private String name;
    private long id;
    private Class type;
    private String displayName;
    private boolean isVisible;
    private int mapping;
    private String description;
    private boolean administrative;
    private boolean noCopy;
    private boolean unique;
    private boolean mandatory;
    private boolean readOnly;
    private String listAttributeClassName = null;

    public LocalAttributeMetadata() {
        this.displayName = "";
    }

    public LocalAttributeMetadata(long oid, String name, String type, String displayName,
            boolean isVisible, boolean mandatory, boolean unique, String description) 
    {
        this.id = oid;
        this.name = name;
        this.type = Utils.getRealType(type);
        this.displayName = displayName;
        this.mandatory = mandatory;
        this.unique = unique;
        this.isVisible = isVisible;
        this.mapping = getMappingFromType(type);
        this.description = description;
        if (this.type.equals(LocalObjectLight.class)) 
            listAttributeClassName = type;
    }
    
    public LocalAttributeMetadata(long oid, String name, Class type, String displayName,
            boolean isVisible, boolean mandatory, boolean unique, 
            Integer mapping, String description) 
    {
        this.id = oid;
        this.name = name;
        this.type = type;
        this.displayName = displayName;
        this.mandatory = mandatory;
        this.unique = unique;
        this.isVisible = isVisible;
        this.mapping = mapping;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayName() {
        return displayName == null || displayName.isEmpty() ? name : displayName;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        return type;
    }
 
    public boolean isAdministrative(){
        return administrative;
    }
    
    /*
     * If this is a list type attribute, returns the class name associated to the item
     */

    public String getListAttributeClassName() {
        return listAttributeClassName;
    }

    public long getId() {
        return id;
    }

    public int getMapping() {
        return mapping;
    }

    public boolean isNoCopy(){
        return noCopy;
    }

    public boolean isIsVisible() {
        return isVisible;
    }

    public void setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }
    
    public boolean isUnique(){
        return unique;
    }

    public boolean isReadOnly(){
        return readOnly;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LocalAttributeMetadata)) {
            return false;
        }
        return this.getId() == ((LocalAttributeMetadata) obj).getId();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
    
    public final int getMappingFromType(String type){
        if (type.equals("String") || type.equals("Integer") || type.equals("Float") || type.equals("Long") || type.equals("Boolean"))
            return Constants.MAPPING_PRIMITIVE;
        if (type.equals("Timestamp"))
            return Constants.MAPPING_TIMESTAMP;
        if (type.equals("Date"))
            return Constants.MAPPING_DATE;
        if (type.equals("Binary"))
            return Constants.MAPPING_BINARY;
        return Constants.MAPPING_MANYTOONE;
    }
}
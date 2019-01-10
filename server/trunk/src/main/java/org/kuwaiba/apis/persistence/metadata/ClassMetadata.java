/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;

/**
 * Contains the detailed metadata information about a class
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ClassMetadata extends ClassMetadataLight {
    /**
     *  Indicates if the instances of this class are physical assets
     *  (in other words, if it's meaningful to have a count on them)
     *  Classes marked with the annotation NoCount (Slot, Port and the like)
     *  have this attribute set as false
     */
    private Boolean countable;
    /**
     * List of interfaces this class implements
     */
    private List<InterfaceMetadata> interfaces;
    /**
     *  Icon to show in views
     */
    private byte[] icon;
    /**
     *  Classmetada's attributes
     */
    private List<AttributeMetadata> attributes;
    /**
     *  Classmetada's category
     */
    private String category;
    /**
     *  ClassMetada's description
     */
    private String description;
    /**
     *  ClassMetada's creationDate
     */
    private long creationDate;

    public ClassMetadata() {
        attributes = new ArrayList<>();
    }
    
   // <editor-fold defaultstate="collapsed" desc="getters and setters methods. Click on the + sign on the left to edit the code.">
    public Boolean isCountable() {
        return countable;
    }

    public void setCountable(Boolean countable) {
        this.countable = countable;
    }

    public List<InterfaceMetadata> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<InterfaceMetadata> interfaces) {
        this.interfaces = interfaces;
    }

    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    public List<AttributeMetadata> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeMetadata> attributes) {
        this.attributes = attributes;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }
    // </editor-fold>

    public AttributeMetadata getAttribute(String attributeName) {
        for (AttributeMetadata att : attributes) {
            if (att.getName().equals(attributeName))
                return att;
        }
        return null;
    }
    
    public boolean implementsInterface(String interfaceName){
        if (interfaces == null)
            return false;
        for (InterfaceMetadata im : interfaces){
            if(im.getName().equals(interfaceName))
                return true;
        }
        return false;
    }

    /**
     * Checks if the current class has a given attribute
     * @param attribute attribute's name
     * @return if the class has or not such attribute
     */
    public boolean hasAttribute(String attribute){
        for (AttributeMetadata eachAttribute : attributes){
            if (eachAttribute.getName().equals(attribute))
                return true;
        }
        return false;
    }

    public String getType(String attributeName)  throws InvalidArgumentException{
        for (AttributeMetadata eachAttribute : attributes){
            if (eachAttribute.getName().equals(attributeName))
                return eachAttribute.getType();
        }
        throw new InvalidArgumentException(String.format ("Attribute %s could not be found in class %s", attributeName, getName()));
    }
    
    public boolean isMandatory(String attributeName)  throws InvalidArgumentException{
        for (AttributeMetadata eachAttribute : attributes){
            if (eachAttribute.getName().equals(attributeName))
                return eachAttribute.isMandatory();
        }
        throw new InvalidArgumentException(String.format ("Attribute %s could not be found in class %s", attributeName, getName()));
    }
    
    public boolean isUnique(String attributeName)  throws InvalidArgumentException {
        for (AttributeMetadata eachAttribute : attributes){
            if (eachAttribute.getName().equals(attributeName))
                return eachAttribute.isUnique();
        }
        throw new InvalidArgumentException(String.format ("Attribute %s could not be found in class %s", attributeName, getName()));
    }
    
    /**
     * Tells if a given (list type) attribute is multiple or single 
     * @param attributeName The name of the attribute
     * @return true if the attribute is multiple. False otherwise
     * @throws InvalidArgumentException If the attribute does not exist
     */
    public boolean isMultiple(String attributeName)  throws InvalidArgumentException{
        for (AttributeMetadata eachAttribute : attributes){
            if (eachAttribute.getName().equals(attributeName))
                return eachAttribute.isUnique();
        }
        throw new InvalidArgumentException(String.format ("Attribute %s could not be found in class %s", attributeName, getName()));
    }
}

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

import java.util.List;
import java.util.logging.Level;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;

/**
 * Contains the detailed metadata information about a class
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ClassMetadata extends ClassMetadataLight{

    /**
     *  Shows if this is a core class (the ones provided in the official release) or a custom one
     */
    private boolean custom;
    /**
     *  Indicates if the instances of this class are physical assets
     *  (in other words, if it's meaningful to have a count on them)
     *  Classes marked with the annotation NoCount (Slot, Port and the like)
     *  have this attribute set as false
     */
    private boolean countable;
    /**
     *  Is this a dummy class as described in the Dummy annotation?
     */
    private boolean dummy;
    /**
     *  The parent ClassMetada name
     */
    private String parentClassName;
    /**
     *
     */
    private List<InterfaceMetadata> interfaces;
    /**
     *  Color assigned to the instances when displayed
     */
    private Integer color;
    /**
     *  Icon to show in views
     */
    private byte[] icon;
    /**
     *  Icon to show in trees and lists
     */
    private byte[] smallIcon;
    /**
     *  Is this class a list type (Vendor, LocationOwner, OpticalLinkType, etc)
     */
    private boolean listType;
    /**
     *  Classmetada's attributes
     */
    private List<AttributeMetadata> attributes;
    /**
     *  Classmetada's displayName
     */
    private String displayName;
    /**
     *  Classmetada's category
     */
    private CategoryMetadata category;
    /**
     *  ClassMetada's description
     */
    private String description;
    /**
     *  ClassMetada's creationDate
     */
    private Long creationDate;

   // <editor-fold defaultstate="collapsed" desc="getters and setters methods. Click on the + sign on the left to edit the code.">
    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public boolean isCountable() {
        return countable;
    }

    public void setCountable(boolean countable) {
        this.countable = countable;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public boolean isDummy() {
        return dummy;
    }

    public void setDummy(boolean dummy) {
        this.dummy = dummy;
    }

    public List<InterfaceMetadata> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<InterfaceMetadata> interfaces) {
        this.interfaces = interfaces;
    }

    public boolean isListType() {
        return listType;
    }

    public void setListType(boolean listType) {
        this.listType = listType;
    }

    public String getParentClassName() {
        return parentClassName;
    }

    public void setParentClassName(String parentClassName) {
        this.parentClassName = parentClassName;
    }

    public byte[] getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(byte[] smallIcon) {
        this.smallIcon = smallIcon;
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

    public CategoryMetadata getCategory() {
        return category;
    }

    public void setCategory(CategoryMetadata category) {
        this.category = category;
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

    public Long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Long creationDate) {
        this.creationDate = creationDate;
    }// </editor-fold>

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

    /**
     * Gets the mapping for a given attribute
     * @param attributeName
     * @return
     * @throws InvalidArgumentException
     */
    public int getAttributeMapping(String attributeName) throws InvalidArgumentException{
        for (AttributeMetadata eachAttribute : attributes){
            if (eachAttribute.getName().equals(attributeName))
                return eachAttribute.getMapping();
        }
        throw new InvalidArgumentException("Attribute cannot be found in this class", Level.WARNING);
    }

    public String getType(String attributeName)  throws InvalidArgumentException{
        for (AttributeMetadata eachAttribute : attributes){
            if (eachAttribute.getName().equals(attributeName))
                return eachAttribute.getType();
        }
        throw new InvalidArgumentException("Attribute cannot be found in this class", Level.WARNING);
    }
}

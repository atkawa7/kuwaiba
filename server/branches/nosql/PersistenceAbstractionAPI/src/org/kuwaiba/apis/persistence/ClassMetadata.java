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

package org.kuwaiba.apis.persistence;

/**
 * Contains the detailed metadata information about a class
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ClassMetadata extends ClassMetadataLight{
    private Long [] attributeIds;
    private String [] attributeNames;
    private String [] attributeTypes;
    private String [] attributeDisplayNames;
    private Boolean [] attributesIsVisible;
    private Boolean [] attributesIsMultiple; //if the attribute is a list
    private String [] attributesDescription;
    protected byte[] icon;
    protected String description;

    public String[] getAttributeDisplayNames() {
        return attributeDisplayNames;
    }

    public void setAttributeDisplayNames(String[] attributeDisplayNames) {
        this.attributeDisplayNames = attributeDisplayNames;
    }

    public Long[] getAttributeIds() {
        return attributeIds;
    }

    public void setAttributeIds(Long[] attributeIds) {
        this.attributeIds = attributeIds;
    }

    public String[] getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeNames(String[] attributeNames) {
        this.attributeNames = attributeNames;
    }

    public String[] getAttributeTypes() {
        return attributeTypes;
    }

    public void setAttributeTypes(String[] attributeTypes) {
        this.attributeTypes = attributeTypes;
    }

    public String[] getAttributesDescription() {
        return attributesDescription;
    }

    public void setAttributesDescription(String[] attributesDescription) {
        this.attributesDescription = attributesDescription;
    }

    public Boolean[] getAttributesIsMultiple() {
        return attributesIsMultiple;
    }

    public void setAttributesIsMultiple(Boolean[] attributesIsMultiple) {
        this.attributesIsMultiple = attributesIsMultiple;
    }

    public Boolean[] getAttributesIsVisible() {
        return attributesIsVisible;
    }

    public void setAttributesIsVisible(Boolean[] attributesIsVisible) {
        this.attributesIsVisible = attributesIsVisible;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }
}

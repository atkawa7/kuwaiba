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
package org.kuwaiba.ws.toserialize.metadata;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.ws.toserialize.application.Validator;

/**
 * This is a wrapper class for ClassMetadata, containing the info required for the clients
 * to render the object attributes in the right way
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassInfo extends ClassInfoLight{
    /**
     * Attribute ids
     */
    private Long [] attributeIds;
    /**
     * Attribute names
     */
    private String [] attributeNames;
    /**
     * Attribute types
     */
    private String [] attributeTypes;
    /**
     * Attribute display names
     */
    private String [] attributeDisplayNames;
    /**
     * Attributes visibility
     */
    private Boolean [] attributesIsVisible;
    /**
     * How the attribute should be rendered. Check AttributeMetadata documentation in the Persistence Abstraction API
     */
    private Integer [] attributesMapping;
    /**
     * Attributes descriptions
     */
    private String [] attributesDescription;
    /**
     * 32x32 icon
     */
    protected byte[] icon;
    /**
     * Class description
     */
    protected String description;

    public ClassInfo(){}
    public ClassInfo(ClassMetadata myClass, Validator[] validators){
        super (myClass, validators);
        this.abstractClass = myClass.isAbstractClass();
        this.icon = myClass.getIcon();
        List<AttributeMetadata> ar = myClass.getAttributes();
        this.attributeIds = new Long[ar.size()];
        this.attributeNames = new String[ar.size()];
        this.attributeTypes = new String[this.attributeNames.length];
        this.attributeDisplayNames = new String[this.attributeNames.length];
        this.attributesIsVisible = new Boolean[this.attributeNames.length];
        this.attributesMapping = new Integer[this.attributeNames.length];
        this.attributesDescription = new String[this.attributeNames.length];
        this.description = myClass.getDescription();
        int i = 0;
        for (AttributeMetadata myAtt : ar){
            this.attributeIds[i] = myAtt.getId();
            this.attributeNames[i] = myAtt.getName();
            this.attributeTypes[i] = myAtt.getType();
            this.attributeDisplayNames[i] = myAtt.getDisplayName() == null?
                "":myAtt.getDisplayName();
            this.attributesIsVisible[i] = myAtt.isVisible();
            this.attributesMapping[i] = myAtt.getMapping();

            this.attributesDescription[i] = myAtt.getDescription()==null?
                "":myAtt.getDescription();
            i++;
        }
    }

    public String[] getAttributeDisplayNames() {
        return attributeDisplayNames;
    }

    public void setAttributeDisplayNames(String[] attributeDisplayNames) {
        this.attributeDisplayNames = attributeDisplayNames;
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

    public Boolean[] getAttributesIsVisible() {
        return attributesIsVisible;
    }

    public void setAttributesIsVisible(Boolean[] attributesIsVisible) {
        this.attributesIsVisible = attributesIsVisible;
    }

    public Integer[] getAttributesMapping() {
        return attributesMapping;
    }

    public void setAttributesMapping(Integer[] attributesMapping) {
        this.attributesMapping = attributesMapping;
    }

    public Long[] getAttributeIds() {
        return attributeIds;
    }

    public void setAttributeIds(Long[] attributeIds) {
        this.attributeIds = attributeIds;
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

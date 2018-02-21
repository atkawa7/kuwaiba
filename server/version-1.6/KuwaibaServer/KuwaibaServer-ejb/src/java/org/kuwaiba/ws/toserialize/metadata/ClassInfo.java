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
package org.kuwaiba.ws.toserialize.metadata;

import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.ws.toserialize.application.Validator;

/**
 * This is a wrapper class for ClassMetadata, containing the info required for 
 * the clients to render the object attributes in the right way
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassInfo extends ClassInfoLight{
    /**
     * Attribute ids
     */
    private long [] attributesIds;
    /**
     * Attribute names
     */
    private String [] attributesNames;
    /**
     * Attribute types
     */
    private String [] attributesTypes;
    /**
     * Attribute display names
     */
    private String [] attributesDisplayNames;
    /**
     * Attributes mandatory
     */
    private boolean [] attributesMandatories;
    /**
     * Attributes unique
     */
    private boolean [] attributesUniques;
    /**
     * Attributes visibility
     */
    private boolean [] attributesVisibles;
    /**
     * Attributes descriptions
     */
    private String [] attributesDescriptions;
    /**
     * 32x32 icon
     */
    protected byte[] icon;
    /**
     * Class description
     */
    protected String description;
    /**
     *  ClassMetada's creationDate
     */
    public long creationDate;
    /**
     *  Indicates if the instances of this class are physical assets
     *  (in other words, if it's meaningful to have a count on them)
     *  Classes marked with the annotation NoCount (Slot, Port and the like)
     *  have this attribute set as false
     */
    private Boolean countable;

    public ClassInfo(){}
    public ClassInfo(ClassMetadata myClass, Validator[] validators){
        super (myClass, validators);
        this._abstract = myClass.isAbstract();
        this.icon = myClass.getIcon();
        Set<AttributeMetadata> ar = myClass.getAttributes();
        this.attributesIds = new long[ar.size()];
        this.attributesNames = new String[ar.size()];
        this.attributesTypes = new String[this.attributesNames.length];
        this.attributesDisplayNames = new String[this.attributesNames.length];
        this.attributesUniques = new boolean[this.attributesNames.length];
        this.attributesMandatories = new boolean[this.attributesNames.length];
        this.attributesVisibles = new boolean[this.attributesNames.length];
        this.attributesDescriptions = new String[this.attributesNames.length];
        this.description = myClass.getDescription();
        this.countable = myClass.isCountable();
        int i = 0;
        for (AttributeMetadata myAtt : ar){
            this.attributesIds[i] = myAtt.getId();
            this.attributesNames[i] = myAtt.getName();
            this.attributesTypes[i] = myAtt.getType();
            this.attributesDisplayNames[i] = myAtt.getDisplayName() == null?
                "":myAtt.getDisplayName();
            this.attributesMandatories[i] = myAtt.isMandatory();
            this.attributesUniques[i] = myAtt.isUnique();
            this.attributesVisibles[i] = myAtt.isVisible();
            this.attributesDescriptions[i] = myAtt.getDescription()==null?
                "":myAtt.getDescription();
            i++;
        }
    }

    public String[] getAttributesDisplayNames() {
        return attributesDisplayNames;
    }

    public void setAttributeDisplayNames(String[] attributesDisplayNames) {
        this.attributesDisplayNames = attributesDisplayNames;
    }

    public String[] getAttributesNames() {
        return attributesNames;
    }

    public void setAttributesNames(String[] attributesNames) {
        this.attributesNames = attributesNames;
    }

    public String[] getAttributesTypes() {
        return attributesTypes;
    }

    public void setAttributesTypes(String[] attributesTypes) {
        this.attributesTypes = attributesTypes;
    }

    public String[] getAttributesDescription() {
        return attributesDescriptions;
    }

    public void setAttributesDescription(String[] attributesDescription) {
        this.attributesDescriptions = attributesDescription;
    }

     public boolean[] getAttributesMandatories() {
        return attributesMandatories;
    }

    public void setAttributesMandatories(boolean[] attributesMandatories) {
        this.attributesMandatories = attributesMandatories;
    }

    public boolean[] getAttributesUniques() {
        return attributesUniques;
    }

    public void setAttributesUniques(boolean[] attributesUniques) {
        this.attributesUniques = attributesUniques;
    }
    
    public boolean[] getAttributesVisibles() {
        return attributesVisibles;
    }

    public void setAttributesVisibles(boolean[] attributesVisibles) {
        this.attributesVisibles = attributesVisibles;
    }

    public String[] getAttributesDescriptions() {
        return attributesDescriptions;
    }

    public long[] getAttributesIds() {
        return attributesIds;
    }

    public void setAttributesIds(long[] attributesIds) {
        this.attributesIds = attributesIds;
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
    
    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean isCountable() {
        return countable;
    }

    public void setCountable(Boolean countable) {
        this.countable = countable;
    }
}

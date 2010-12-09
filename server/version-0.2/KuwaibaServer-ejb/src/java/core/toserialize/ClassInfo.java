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
package core.toserialize;

import entity.core.metamodel.AttributeMetadata;
import entity.core.metamodel.ClassMetadata;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * This is a wrapper class for ClassMetadata, containing the info requiered for the clients
 * to render the object attributes in the right way
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ClassInfo extends ClassInfoLight{
    
    private String [] attributeNames; 
    private String [] attributeTypes;
    private String [] attributeDisplayNames;
    private Boolean [] attributesIsVisible;
    private Boolean [] attributesIsAdministrative;
    private Boolean [] attributesIsMultiple; //if the attribute is a list
    private String [] attributesDescription;
    protected byte[] icon;

    public ClassInfo(){}
    public ClassInfo(ClassMetadata myClass){
        super (myClass);
        this.isAbstract = myClass.getIsAbstract();
        this.icon = myClass.getIcon();
        List<AttributeMetadata> ar = myClass.getAttributes();
        this.attributeNames = new String[ar.size()];
        this.attributeTypes = new String[this.attributeNames.length];
        this.attributeDisplayNames = new String[this.attributeNames.length];
        this.attributesIsVisible = new Boolean[this.attributeNames.length];
        this.attributesIsAdministrative = new Boolean[this.attributeNames.length];
        this.attributesIsMultiple = new Boolean[this.attributeNames.length];
        this.attributesDescription = new String[this.attributeNames.length];

        int i = 0;
        for (AttributeMetadata myAtt : ar){
            this.attributeNames[i] = myAtt.getName();
            this.attributeTypes[i] = myAtt.getType();
            this.attributeDisplayNames[i] = myAtt.getDisplayName() == null?
                "":myAtt.getDisplayName();
            this.attributesIsAdministrative[i] = myAtt.isAdministrative();
            this.attributesIsVisible[i] = myAtt.IsVisible();
            this.attributesIsMultiple[i] = myAtt.isMultiple();

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

    public Boolean[] getAttributesIsAdministrative() {
        return attributesIsAdministrative;
    }

    public void setAttributesIsAdministrative(Boolean[] attributesIsAdministrative) {
        this.attributesIsAdministrative = attributesIsAdministrative;
    }

    public Boolean[] getAttributesIsVisible() {
        return attributesIsVisible;
    }

    public void setAttributesIsVisible(Boolean[] attributesIsVisible) {
        this.attributesIsVisible = attributesIsVisible;
    }

    public Boolean[] getAttributesIsMultiple() {
        return attributesIsMultiple;
    }

    public void setAttributesIsMultiple(Boolean[] attributesIsMultiple) {
        this.attributesIsMultiple = attributesIsMultiple;
    }

    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }
}

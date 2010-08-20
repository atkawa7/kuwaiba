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

import java.awt.Image;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.utils.Utils;
import org.inventory.webservice.ClassInfo;

/**
 * It's a proxy class, whose instances represent the metadata information associated to a class
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalClassMetadataImpl extends LocalClassMetadataLightImpl
        implements LocalClassMetadata{

    private Image icon;
    private String [] attributeNames; 
    private String [] attributeTypes;
    private String [] attributeDisplayNames;
    private Boolean [] attributesIsVisible;
    private Boolean [] attributesIsAdministrative;
    private Boolean [] attributesIsMultiple;
    private String [] attributesDescription;

    public LocalClassMetadataImpl(ClassInfo cm){
        super(cm.getId(),cm.getClassName(),cm.getPackage(),cm.getDisplayName(),
                cm.getDescription(),cm.getSmallIcon());
        this.isAbstract = cm.isIsAbstract();
        this.icon = cm.getIcon()==null?null:Utils.getImageFromByteArray(cm.getIcon());
        this.attributeNames = cm.getAttributeNames().toArray(new String[0]);
        this.attributeTypes = cm.getAttributeTypes().toArray(new String[0]);
        this.attributeDisplayNames = cm.getAttributeDisplayNames().toArray(new String[0]);
        this.attributesIsVisible = cm.getAttributesIsVisible().toArray(new Boolean[0]);
        this.attributesIsAdministrative = cm.getAttributesIsAdministrative().toArray(new Boolean[0]);
        this.attributesIsMultiple = cm.getAttributesIsMultiple().toArray(new Boolean[0]);
        this.attributesDescription = cm.getAttributesDescription().toArray(new String[0]);
    }

    public Boolean[] getAttributesIsMultiple() {
        return attributesIsMultiple;
    }

    public void setAttributesIsMultiple(Boolean[] attributesIsMultiple) {
        this.attributesIsMultiple = attributesIsMultiple;
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

    public String getDisplayNameForAttribute(String att){
        for (int i=0; i< this.attributeNames.length;i++)
            if(this.attributeNames[i].equals(att))
                return this.attributeDisplayNames[i].equals("")?att:this.attributeDisplayNames[i];
        return att;
    }

    public Boolean isAdministrative(String att){
        for (int i=0; i< this.attributeNames.length;i++)
            if(this.attributeNames[i].equals(att))
                return this.attributesIsAdministrative[i];
        return false;
    }

    public Boolean isMultiple(String att){
        for (int i=0; i< this.attributeNames.length;i++)
            if(this.attributeNames[i].equals(att))
                return this.attributesIsMultiple[i];
        return false;
    }
    
    public Boolean isVisible(String att){
        for (int i=0; i< this.attributeNames.length;i++)
            if(this.attributeNames[i].equals(att))
                return this.attributesIsVisible[i];
        return false;
    }

    public String getDescriptionForAttribute(String att){
        for (int i=0; i< this.attributeNames.length;i++)
            if(this.attributeNames[i].equals(att))
                return this.attributesDescription[i];
        return "";
    }

    public String getTypeForAttribute(String att){
        for (int i=0; i< this.attributeNames.length;i++)
            if(this.attributeNames[i].equals(att))
                return this.attributeTypes[i];
        return "String";
    }

    public LocalAttributeMetadataImpl[] getAttributes(){
        LocalAttributeMetadataImpl[] res =
                new LocalAttributeMetadataImpl[attributeNames.length];
        for (int i = 0; i<res.length;i++)
            res[i] = new LocalAttributeMetadataImpl(attributeNames[i],
                                    attributeTypes[i],
                                    attributeDisplayNames[i],
                                    attributesIsVisible[i],
                                    attributesIsAdministrative[i],
                                    attributesDescription[i]);
        return res;
    }

    public Image getIcon() {
        return icon;
    }

    @Override
    public String toString(){
        return this.className;
    }
}

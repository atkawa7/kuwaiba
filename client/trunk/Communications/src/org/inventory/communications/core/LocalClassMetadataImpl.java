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

import java.awt.Image;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.utils.Utils;
import org.kuwaiba.wsclient.ClassInfo;
import org.openide.util.lookup.ServiceProvider;

/**
 * It's a proxy class, whose instances represent the metadata information associated to a class
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@ServiceProvider(service=LocalClassMetadata.class)
public class LocalClassMetadataImpl extends LocalClassMetadataLightImpl
        implements LocalClassMetadata{

    private Image icon;
    private String description;
    private long [] attributeIds;
    private String [] attributeNames; 
    private String [] attributeTypes;
    private String [] attributeDisplayNames;
    private boolean [] attributesIsVisible;
    private int [] attributeMappings;
    private String [] attributesDescription;

     /**
     * This constructor is called to create dummy class metadata objects, such as that used to represent the Navigation Tree root
     */
    public LocalClassMetadataImpl() { super();   }

    public LocalClassMetadataImpl(ClassInfo cm){
        super(cm);
        this.icon = (cm.getIcon()==null) ? null : Utils.getImageFromByteArray(cm.getIcon());
        this.description = cm.getDescription();
        this.attributeIds = new long[cm.getAttributeIds().size()];
        for (int i = 0; i < cm.getAttributeIds().size(); i++)
            attributeIds[i] = cm.getAttributeIds().get(i).longValue();

        this.attributeNames = cm.getAttributeNames().toArray(new String[0]);
        this.attributeTypes = cm.getAttributeTypes().toArray(new String[0]);
        this.attributeDisplayNames = cm.getAttributeDisplayNames().toArray(new String[0]);
        this.attributesIsVisible = new boolean[cm.getAttributesIsVisible().size()];
        for (int i = 0; i < cm.getAttributesIsVisible().size(); i++)
            attributesIsVisible[i] = cm.getAttributesIsVisible().get(i).booleanValue();
        this.attributeMappings = new int[cm.getAttributesMapping().size()];
        for (int i = 0; i < cm.getAttributesMapping().size(); i++)
            attributeMappings[i] = cm.getAttributesMapping().get(i).intValue();

        this.attributesDescription = cm.getAttributesDescription().toArray(new String[0]);
    }

    public int[] getAttributeMappings() {
        return attributeMappings;
    }

    public void setAttributeMappings(int[] attributeMappings) {
        this.attributeMappings = attributeMappings;
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

    public boolean[] getAttributesIsVisible() {
        return attributesIsVisible;
    }

    public void setAttributesIsVisible(boolean[] attributesIsVisible) {
        this.attributesIsVisible = attributesIsVisible;
    }

    public String getDisplayNameForAttribute(String att){
        for (int i=0; i< this.attributeNames.length;i++)
            if(this.attributeNames[i].equals(att))
                return this.attributeDisplayNames[i].equals("")?att:this.attributeDisplayNames[i];
        return att;
    }

    public int getMappingForAttribute(String att){
        for (int i=0; i< this.attributeNames.length;i++)
            if(this.attributeNames[i].equals(att))
                return this.attributeMappings[i];
        return 0;
    }
    
    public boolean isVisible(String att){
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
            res[i] = new LocalAttributeMetadataImpl(
                                    attributeIds[i],
                                    attributeNames[i],
                                    attributeTypes[i],
                                    attributeDisplayNames[i],
                                    attributesIsVisible[i],
                                    attributeMappings[i],
                                    attributesDescription[i]);
        return res;
    }

    public Image getIcon() {
        return icon;
    }

    public long[] getAttributeIds() {
        return attributeIds;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return this.description;
    }

    public LocalClassMetadataLight asLocalClassMetadataLight(){
        return new LocalClassMetadataLightImpl(id, className, displayName, smallIcon, abstractClass, viewable, listType, validators);
    }
}

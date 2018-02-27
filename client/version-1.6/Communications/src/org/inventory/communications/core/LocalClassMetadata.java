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

import java.awt.Color;
import java.awt.Image;
import java.util.HashMap;
import java.util.List;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;

/**
 * It's a proxy class, whose instances represent the metadata information associated to a class
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalClassMetadata extends LocalClassMetadataLight {

    private Image icon;
    private String description;
    private boolean countable;
    private long [] attributesIds;
    private String [] attributesNames; 
    private String [] attributesTypes;
    private String [] attributesDisplayNames;
    private boolean [] attributesMandatories;
    private boolean [] attributesUniques;
    private boolean [] attributesVisibles;
    private int [] attributesMappings;
    private String [] attributesDescriptions;
    /**
     * Creation Date
     */
    private long creationDate;
     
    public LocalClassMetadata(){
        super();
    }
    
    public LocalClassMetadata (long id, String className, String displayName, 
            String parentName, boolean _abstract, boolean viewable, boolean listType, 
            boolean custom, boolean inDesign, byte[] smallIcon, int color, HashMap<String, Integer> validators,
            byte[] icon, 
            String description, 
            List<Long> attributesIds, 
            String[] attributesNames, 
            String[] attributesTypes, 
            String[] attributesDisplayNames,
            List<Boolean> attributesMandatories, 
            List<Boolean> attributesUniques,
            List<Boolean> attributesVisibles, 
            String[] attributesDescriptions) {
        
        super(id, className, displayName, parentName, _abstract, viewable, listType, 
            custom, inDesign, smallIcon, color, validators);
        this.icon = Utils.getIconFromByteArray(icon, new Color(color), 24, 24);
        this.description = description;
        this.attributesIds = new long[attributesIds.size()];
        this.attributesMappings = new int[attributesIds.size()];
        this.attributesMandatories = new boolean[attributesMandatories.size()];
        this.attributesUniques = new boolean[attributesUniques.size()];
        this.attributesVisibles = new boolean[attributesVisibles.size()];
        
        for (int i = 0; i < attributesIds.size(); i++){
            this.attributesIds[i] = attributesIds.get(i);
            this.attributesMappings[i] = getMappingFromType(attributesTypes[i]);
            this.attributesVisibles[i] = attributesVisibles.get(i);
            this.attributesMandatories[i] = attributesMandatories.get(i);
            this.attributesUniques[i] = attributesUniques.get(i);
        }

        this.attributesNames = attributesNames;
        this.attributesTypes = attributesTypes;
        this.attributesDisplayNames = attributesDisplayNames;
        this.attributesDescriptions = attributesDescriptions;
    }

    public int[] getAttributesMappings() {
        return attributesMappings;
    }

    public String[] getAttributesDisplayNames() {
        return attributesDisplayNames;
    }

    public String[] getAttributesNames() {
        return attributesNames;
    }

    public String[] getAttributesTypes() {
        return attributesTypes;
    }

    public String[] getAttributesDescription() {
        return attributesDescriptions;
    }

    public boolean[] getAttributesVisibles() {
        return attributesVisibles;
    }

    public boolean[] getAttributesMandatories() {
        return attributesMandatories;
    }

    public boolean[] getAttributesUniques() {
        return attributesUniques;
    }

    public String getDisplayNameForAttribute(String att){
        for (int i=0; i< this.attributesNames.length;i++){
            if(this.attributesNames[i].equals(att)){
                return this.attributesDisplayNames[i].equals("")?att:this.attributesDisplayNames[i];
            }
        }
        return att;
    }

    public int getMappingForAttribute(String att){
        for (int i = 0; i< this.attributesNames.length;i++){
            if(this.attributesNames[i].equals(att))
                return this.attributesMappings[i];
        }
        return 0;
    }

    public boolean isVisible(String att){
        for (int i=0; i< this.attributesNames.length;i++){
            if(this.attributesNames[i].equals(att)){
                return this.attributesVisibles[i];
            }
        }
        return false;
    }

    public String getDescriptionForAttribute(String att){
        for (int i=0; i< this.attributesNames.length;i++){
            if(this.attributesNames[i].equals(att)){
                return this.attributesDescriptions[i];
            }
        }
        return "";
    }

    public String getTypeForAttribute(String att){
        for (int i=0; i< this.attributesNames.length;i++){
            if(this.attributesNames[i].equals(att)){
                return this.attributesTypes[i];
            }
        }
        return String.class.getName();
    }


    public LocalAttributeMetadata[] getAttributes(){
        LocalAttributeMetadata[] res =
                new LocalAttributeMetadata[attributesNames.length];
        for (int i = 0; i < res.length;i++){
            res[i] = new LocalAttributeMetadata(
                                    attributesIds[i],
                                    attributesNames[i],
                                    attributesTypes[i],
                                    attributesDisplayNames[i],
                                    attributesVisibles[i],
                                    attributesMandatories[i],
                                    attributesUniques[i],
                                    attributesDescriptions[i]);
        }
        return res;
    }

    public Image getIcon() {
        return icon;
    }
    
    public long[] getAttributeIds() {
        return attributesIds;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
    
    public boolean isCountable() {
        return countable;
    }
        
    public long getCreationDate() {
        return creationDate;
    }
    
    public boolean hasAttribute(String attribute) {
        for (String existingAttribute : attributesNames) {
            if (existingAttribute.equals(attribute))
                return true;
        }
        return false;
    }

    public static final int getMappingFromType(String type){
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
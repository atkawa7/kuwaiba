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
    private long [] attributeIds;
    private String [] attributeNames; 
    private String [] attributeTypes;
    private String [] attributeDisplayNames;
    private boolean [] attributeIsVisibles;
    private int [] attributeMappings;
    private String [] attributeDescriptions;
    /**
     * Creation Date
     */
    public long creationDate;
     
    public LocalClassMetadata(){
        super();
    }
    
    public LocalClassMetadata (long id, String className, String displayName, 
            String parentName, boolean _abstract, boolean viewable, boolean listType, 
            boolean custom, boolean inDesign, byte[] smallIcon, int color, HashMap<String, Integer> validators,
            byte[] icon, String description, List<Long> attributeIds, 
            String[] attributeNames, String[] attributeTypes, String[] attributeDisplayNames,
            List<Boolean> attributeIsVisibles, String[] attributeDescriptions) {
        
        super(id, className, displayName, parentName, _abstract, viewable, listType, 
            custom, inDesign, smallIcon, color, validators);
        this.icon = Utils.getIconFromByteArray(icon, new Color(color), 32, 32);
        this.description = description;
        this.attributeIds = new long[attributeIds.size()];
        this.attributeMappings = new int[attributeIds.size()];
        this.attributeIsVisibles = new boolean[attributeIsVisibles.size()];
        
        for (int i = 0; i < attributeIds.size(); i++){
            this.attributeIds[i] = attributeIds.get(i);
            this.attributeMappings[i] = getMappingFromType(attributeTypes[i]);
            this.attributeIsVisibles[i] = attributeIsVisibles.get(i);
        }

        this.attributeNames = attributeNames;
        this.attributeTypes = attributeTypes;
        this.attributeDisplayNames = attributeDisplayNames;
        this.attributeDescriptions = attributeDescriptions;
    }

    public int[] getAttributeMappings() {
        return attributeMappings;
    }

    public String[] getAttributeDisplayNames() {
        return attributeDisplayNames;
    }

    public String[] getAttributeNames() {
        return attributeNames;
    }

    public String[] getAttributeTypes() {
        return attributeTypes;
    }

    public String[] getAttributesDescription() {
        return attributeDescriptions;
    }

    public boolean[] getAttributesIsVisible() {
        return attributeIsVisibles;
    }

    public String getDisplayNameForAttribute(String att){
        for (int i=0; i< this.attributeNames.length;i++){
            if(this.attributeNames[i].equals(att)){
                return this.attributeDisplayNames[i].equals("")?att:this.attributeDisplayNames[i];
            }
        }
        return att;
    }

    public int getMappingForAttribute(String att){
        for (int i = 0; i< this.attributeNames.length;i++){
            if(this.attributeNames[i].equals(att))
                return this.attributeMappings[i];
        }
        return 0;
    }

    public boolean isVisible(String att){
        for (int i=0; i< this.attributeNames.length;i++){
            if(this.attributeNames[i].equals(att)){
                return this.attributeIsVisibles[i];
            }
        }
        return false;
    }

    public String getDescriptionForAttribute(String att){
        for (int i=0; i< this.attributeNames.length;i++){
            if(this.attributeNames[i].equals(att)){
                return this.attributeDescriptions[i];
            }
        }
        return "";
    }

    public String getTypeForAttribute(String att){
        for (int i=0; i< this.attributeNames.length;i++){
            if(this.attributeNames[i].equals(att)){
                return this.attributeTypes[i];
            }
        }
        return String.class.getName();
    }


    public LocalAttributeMetadata[] getAttributes(){
        LocalAttributeMetadata[] res =
                new LocalAttributeMetadata[attributeNames.length];
        for (int i = 0; i<res.length;i++){
            res[i] = new LocalAttributeMetadata(
                                    attributeIds[i],
                                    attributeNames[i],
                                    attributeTypes[i],
                                    attributeDisplayNames[i],
                                    attributeIsVisibles[i],
                                    attributeMappings[i],
                                    attributeDescriptions[i]);
        }
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
        return description;
    }
    
    public boolean isCountable() {
        return countable;
    }
        
    public long getCreationDate() {
        return creationDate;
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
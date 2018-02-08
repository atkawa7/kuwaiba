/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.customization.classhierarchy.nodes;

import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.i18n.I18N;
import org.inventory.customization.classhierarchy.nodes.properties.AttributeMetadataProperty;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * Represents an attribute as a node within the data model manager
 * @author Adrian Martinez Molina <charles.bedon@kuwaiba.org>
 */
public class AttributeMetadataNode extends AbstractNode  {
    
    private LocalAttributeMetadata attribute;
    private ClassMetadataNode classNode;
    private Sheet sheet;

    public AttributeMetadataNode(LocalAttributeMetadata lam, ClassMetadataNode classNode) {
        super(Children.LEAF,Lookups.singleton(lam));
        this.attribute = lam;
        this.classNode = classNode;
    }

    @Override
    public String getDisplayName(){
       return this.attribute.getName();
    }
    
    @Override
    protected Sheet createSheet() {
       sheet = Sheet.createDefault();
        
        Sheet.Set generalPropertySet = Sheet.createPropertiesSet();

        generalPropertySet.put(new AttributeMetadataProperty(classNode.getClassMetadata(),
                Constants.PROPERTY_NAME, Constants.PROPERTY_NAME, 
                I18N.gm("name"), attribute.getName(),this));
        
        generalPropertySet.put(new AttributeMetadataProperty(classNode.getClassMetadata(), 
                Constants.PROPERTY_DISPLAYNAME, Constants.PROPERTY_DISPLAYNAME, 
                I18N.gm("display_name"), attribute.getDisplayName(),this));
        
        generalPropertySet.put(new AttributeMetadataProperty(classNode.getClassMetadata(),
                Constants.PROPERTY_DESCRIPTION, Constants.PROPERTY_DESCRIPTION, 
                I18N.gm("description"), attribute.getDescription(),this));
        
        generalPropertySet.put(new AttributeMetadataProperty(classNode.getClassMetadata(), 
                Constants.PROPERTY_TYPE, Constants.PROPERTY_TYPE, 
                I18N.gm("type"), 
                (attribute.getType() == LocalObjectLight.class) ? this.attribute.getListAttributeClassName() : attribute.getType().getSimpleName(),this));
        
        generalPropertySet.put(new AttributeMetadataProperty(
                classNode.getClassMetadata(), Constants.PROPERTY_MANDATORY, 
                Constants.PROPERTY_MANDATORY, 
                I18N.gm("mandatory.description"), 
                attribute.isMandatory(), this));
        
        generalPropertySet.put(new AttributeMetadataProperty(classNode.getClassMetadata(), 
                Constants.PROPERTY_UNIQUE,
                Constants.PROPERTY_UNIQUE,
                I18N.gm("unique.description"), 
                attribute.isUnique(),this));
        
        generalPropertySet.put(new AttributeMetadataProperty(classNode.getClassMetadata(), 
                Constants.PROPERTY_VISIBLE,
                Constants.PROPERTY_VISIBLE,
                I18N.gm("visible"), 
                attribute.isVisible(),this));
        
        generalPropertySet.put(new AttributeMetadataProperty(classNode.getClassMetadata(), 
                Constants.PROPERTY_ADMINISTRATIVE, 
                Constants.PROPERTY_ADMINISTRATIVE, 
                I18N.gm("administrative.description"), 
                attribute.isAdministrative(),this));
        
        generalPropertySet.put(new AttributeMetadataProperty(classNode.getClassMetadata(), 
                Constants.PROPERTY_NOCOPY,
                Constants.PROPERTY_NOCOPY,
                I18N.gm("no_copy.description"),  
                attribute.isNoCopy(),this));
                
        generalPropertySet.setName("1");

        generalPropertySet.setDisplayName(I18N.gm("general_attributes"));

        sheet.put(generalPropertySet);
        return sheet;  
    }

    @Override
    public void setName(String s) {
        super.setName(s);
        refresh();
    }
   
    public boolean refresh(){
        if (this.sheet != null)
            setSheet(createSheet());
        
        return true;
    }
   
    public LocalAttributeMetadata getAttributeMetadata(){
        return attribute;
    }
    
    public ClassMetadataNode getClassNode(){
        return classNode;
    }
}
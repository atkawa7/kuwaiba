/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
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

        generalPropertySet.put(new AttributeMetadataProperty(Constants.PROPERTY_NAME, "Name", 
                attribute.getName(),this,classNode.getClassMetadata().getOid()));
        
        generalPropertySet.put(new AttributeMetadataProperty(Constants.PROPERTY_DISPLAYNAME, "Displa name", 
                attribute.getDisplayName(),this,classNode.getClassMetadata().getOid()));
        
        generalPropertySet.put(new AttributeMetadataProperty(Constants.PROPERTY_DESCRIPTION, "Description", 
                attribute.getDescription(),this,classNode.getClassMetadata().getOid()));
        
        generalPropertySet.put(new AttributeMetadataProperty(Constants.PROPERTY_TYPE, "Type", 
               (attribute.getType() == LocalObjectLight.class) ? this.attribute.getListAttributeClassName() : attribute.getType().getSimpleName(),this, classNode.getClassMetadata().getOid()));
        
        generalPropertySet.put(new AttributeMetadataProperty(Constants.PROPERTY_VISIBLE, "Visible", 
                attribute.isVisible(),this,classNode.getClassMetadata().getOid()));
        
        generalPropertySet.put(new AttributeMetadataProperty(Constants.PROPERTY_ADMINISTRATIVE, "Is this not an attribute use for operational purposes?", 
                attribute.isAdministrative(),this,classNode.getClassMetadata().getOid()));
        
        generalPropertySet.put(new AttributeMetadataProperty(Constants.PROPERTY_NOCOPY, "Can this attribute be transferred in copy operations?", 
                attribute.isNoCopy(),this,classNode.getClassMetadata().getOid()));
                
        generalPropertySet.setName("1");

        generalPropertySet.setDisplayName("General Attributes");

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
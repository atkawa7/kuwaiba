/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.navigation.applicationnodes.attributemetadatanodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.inventory.core.services.api.metadata.LocalAttributeMetadata;
import org.inventory.core.services.utils.Constants;
import org.inventory.navigation.applicationnodes.attributemetadatanodes.properties.AttributeMetadataProperty;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * Represents an attribute as a node within the data model manager
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class AttributeMetadataNode extends AbstractNode implements PropertyChangeListener{
    
    static final String ICON_PATH = "org/inventory/customization/attributecustomizer/res/flag-blue.png";
    private LocalAttributeMetadata attribute;
    private Sheet sheet;
    private long classId;

    public AttributeMetadataNode(LocalAttributeMetadata lam, long classId) {
        super(Children.LEAF,Lookups.singleton(lam));
        setIconBaseWithExtension(ICON_PATH);
        this.attribute = lam;
        this.classId = classId;
    }

    @Override
    public String getDisplayName(){
       return this.attribute.getName();
    }
    
    @Override
    protected Sheet createSheet() {
       sheet = Sheet.createDefault();
        
        Sheet.Set generalPropertySet = Sheet.createPropertiesSet();

        generalPropertySet.put(new AttributeMetadataProperty(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NAME"), 
                attribute.getName(),this,classId));
        
        generalPropertySet.put(new AttributeMetadataProperty(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DISPLAYNAME"), 
                attribute.getDisplayName(),this,classId));
        
        generalPropertySet.put(new AttributeMetadataProperty(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DESCRIPTION"), 
                attribute.getDescription(),this,classId));
        
        generalPropertySet.put(new AttributeMetadataProperty(Constants.PROPERTY_TYPE, 
                attribute.getType(),this,classId));
        
        generalPropertySet.put(new AttributeMetadataProperty(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_VISIBLE"), 
                attribute.isVisible(),this,classId));
        
        generalPropertySet.put(new AttributeMetadataProperty(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_ADMINISTRATIVE"), 
                attribute.isAdministrative(),this,classId));

//      Commented out for now 
//      generalPropertySet.put(new AttributeMetadataProperty(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_UNIQUE"), 
//              attribute.isUnique(),this,classId));
        
        generalPropertySet.put(new AttributeMetadataProperty(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NO_COPY"), 
                attribute.isNoCopy(),this,classId));
                
        generalPropertySet.setName("1");

        generalPropertySet.setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_GENERAL_ATTRIBUTES"));

        
        sheet.put(generalPropertySet);
        
        return sheet;  
    }

    @Override
    public void setName(String s) {
        super.setName(s);
        refresh();
    }
   
    public boolean refresh(){
        //LocalAttributeMetadata attributeMetadataRefresh = null;
        
//        if(attributeMetadataRefresh == null)
//            return false;
        
        if (this.sheet != null)
            setSheet(createSheet());
        
        return true;
    }
   
    public LocalAttributeMetadata getObject(){
        return attribute;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}

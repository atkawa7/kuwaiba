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

package org.inventory.customization.attributecustomizer.nodes;

import org.inventory.customization.attributecustomizer.nodes.properties.AttributeCustomizerNodeProperty;
import org.inventory.core.services.interfaces.LocalAttributeMetadata;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * Each node wraps an attribute object
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class AttributeMetadataNode extends AbstractNode{
    static final String ICON_PATH = "org/inventory/customization/attributecustomizer/res/flag-blue.png";
    private LocalAttributeMetadata object;

    public AttributeMetadataNode(LocalAttributeMetadata lam) {
        super(Children.LEAF,Lookups.singleton(lam));
        setIconBaseWithExtension(ICON_PATH);
        this.object = lam;
    }

    @Override
    public String getDisplayName(){
       return this.object.getName();
    }

    //This method exposes the properties for each node
   @Override
   protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set ss = s.get(Sheet.PROPERTIES);
        if (ss == null) {
            ss = Sheet.createPropertiesSet();
            s.put(ss);
        }

        ss.put(new AttributeCustomizerNodeProperty("name",object.getName(),java.util.ResourceBundle.getBundle("org/inventory/customization/attributecustomizer/Bundle").getString("LBL_NAME"),java.util.ResourceBundle.getBundle("org/inventory/customization/attributecustomizer/Bundle").getString("LBL_NAMEDESCRIPTION"),this));
        ss.put(new AttributeCustomizerNodeProperty("type",object.getType(),java.util.ResourceBundle.getBundle("org/inventory/customization/attributecustomizer/Bundle").getString("LBL_TYPE"),java.util.ResourceBundle.getBundle("org/inventory/customization/attributecustomizer/Bundle").getString("LBL_TYPEDESCRIPTION"),this));
        ss.put(new AttributeCustomizerNodeProperty("displayName",object.getDisplayName(),java.util.ResourceBundle.getBundle("org/inventory/customization/attributecustomizer/Bundle").getString("LBL_LABEL"),java.util.ResourceBundle.getBundle("org/inventory/customization/attributecustomizer/Bundle").getString("LBL_LABELDESCRIPTION"),this));
        ss.put(new AttributeCustomizerNodeProperty("isVisible",object.getIsVisible(),java.util.ResourceBundle.getBundle("org/inventory/customization/attributecustomizer/Bundle").getString("LBL_VISIBLE"),java.util.ResourceBundle.getBundle("org/inventory/customization/attributecustomizer/Bundle").getString("LBL_VISIBLEDESCRIPTION"),this));
        ss.put(new AttributeCustomizerNodeProperty("isAdministrative",object.getIsAdministrative(),java.util.ResourceBundle.getBundle("org/inventory/customization/attributecustomizer/Bundle").getString("LBL_ADMINISTRATIVE"),java.util.ResourceBundle.getBundle("org/inventory/customization/attributecustomizer/Bundle").getString("LBL_ADMINISTRATIVEDESCRIPTION"),this));
        ss.put(new AttributeCustomizerNodeProperty("description",object.getDescription(),java.util.ResourceBundle.getBundle("org/inventory/customization/attributecustomizer/Bundle").getString("LBL_DESCRIPTION"),java.util.ResourceBundle.getBundle("org/inventory/customization/attributecustomizer/Bundle").getString("LBL_DESCRIPTIONDESCRIPTION"),this));
        return s;
   }
   public LocalAttributeMetadata getObject(){
       return object;
   }

}
/*
 * Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.neotropic.inventory.modules.ipam.nodes;

import com.neotropic.inventory.modules.ipam.nodes.properties.GeneralProperty;
import com.neotropic.inventory.modules.ipam.nodes.properties.ListTypeProperty;
import com.neotropic.inventory.modules.ipam.nodes.properties.NotEditableProperty;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.nodes.Sheet;


/**
 * Represents an IPv4 or an IPv6 inside of a subnet
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class IPAddressNode extends ObjectNode {
    
    public IPAddressNode(LocalObjectLight lol) {
        super(lol, true);
    }
    
    @Override
    protected Sheet createSheet() {
        LocalObject sp = com.getObjectInfo(getObject().getClassName(), getObject().getOid());
        
        Sheet sheet = Sheet.createDefault();
        Sheet.Set generalPropertySet = Sheet.createPropertiesSet(); //General attributes category
        
        generalPropertySet.put(new NotEditableProperty(Constants.CLASS_SUBNET, String.class, 
                java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_SUBNET"),
                "",sp.getName()));
        
        generalPropertySet.put(new GeneralProperty(Constants.PROPERTY_DESCRIPTION, String.class, 
                java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_DESCRIPTION"),
                "",this, sp.getAttribute(Constants.PROPERTY_DESCRIPTION)));
        
        generalPropertySet.put(new NotEditableProperty(Constants.PROPERTY_CREATIONDATE, String.class, 
                java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_CREATION_DATE"),
                "",sp.getAttribute(Constants.PROPERTY_CREATIONDATE)));
        
        //List Type State
        List<LocalObjectListItem> list = com.getList(Constants.LIST_TYPE_OPERATIONAL_STATE, true, false);
        if (list == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                return sheet;
        }
        LocalObjectListItem val = null;
        if (sp.getAttribute(Constants.PROPERTY_STATE) == null) {
            val = list.get(0); //None
        } else {
            for (LocalObjectListItem loli : list) {
                if (sp.getAttribute(Constants.PROPERTY_STATE).equals(loli.getOid())) {
                    val = loli;
                    break;
                }
            }
        }
        generalPropertySet.put(new ListTypeProperty(
                Constants.PROPERTY_STATE, 
                Constants.PROPERTY_STATE,
                Constants.PROPERTY_STATE,list, this, val)); 
        
        generalPropertySet.setDisplayName(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_GENERAL_ATTRIBUTES"));
        sheet.put(generalPropertySet);
        return sheet;
    }

    @Override
    public Action[] getActions(boolean context) {
        return super.getActions(context);
    }
    
    @Override
    public boolean canRename() {
        return false;
    }
}

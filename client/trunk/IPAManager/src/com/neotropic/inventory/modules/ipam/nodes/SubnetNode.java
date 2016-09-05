/**
 * Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

import com.neotropic.inventory.modules.ipam.nodes.actions.AddIPAddressAction;
import java.awt.Image;
import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import com.neotropic.inventory.modules.ipam.nodes.actions.DeleteSubnetAction;
import com.neotropic.inventory.modules.ipam.nodes.actions.RelateSubnetToVRFAction;
import com.neotropic.inventory.modules.ipam.nodes.actions.RelateToVlanAction;
import com.neotropic.inventory.modules.ipam.nodes.actions.ReleaseFromVlanAction;
import com.neotropic.inventory.modules.ipam.nodes.actions.ReleaseSubnetFromVRFAction;
import com.neotropic.inventory.modules.ipam.nodes.properties.GeneralProperty;
import com.neotropic.inventory.modules.ipam.nodes.properties.NotEditableProperty;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.util.Constants;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.actions.ExecuteClassReportAction;
import org.kuwaiba.management.services.nodes.actions.RelateToServiceAction;
import org.kuwaiba.management.services.nodes.actions.ReleaseFromServiceAction;

import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;

/**
 * Represents a subnet 
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class SubnetNode extends ObjectNode {
    
    private static final String ICON_PATH="com/neotropic/inventory/modules/res/subnet-icon.png";
    private static Image defaultIcon = ImageUtilities.loadImage(ICON_PATH);
    

    public SubnetNode(LocalObjectLight subnet) {
        super(subnet);
        setChildren(new SubnetChildren());
    }
    
    @Override
    public Action[] getActions(boolean context){
        return new Action[]{
            new AddIPAddressAction(),
            null,
            new RelateToServiceAction(),
            new RelateToVlanAction(),
            new RelateSubnetToVRFAction(),
            null,
            new ReleaseFromServiceAction(),
            new ReleaseFromVlanAction(),
            new ReleaseSubnetFromVRFAction(),
            ExecuteClassReportAction.createExecuteReportAction(),
            null,
            new DeleteSubnetAction()
        };
    }
 
    @Override
    public Image getIcon(int i){
        return defaultIcon;
    }

    @Override
    public Image getOpenedIcon(int i){
        return getIcon(i);
    }
    
    @Override
    protected Sheet createSheet(){
        LocalObject sp = com.getObjectInfo(getObject().getClassName(), getObject().getOid());
        sheet = Sheet.createDefault();
        Sheet.Set generalPropertySet = Sheet.createPropertiesSet(); //General attributes category
        
        
        generalPropertySet.put(new NotEditableProperty(Constants.PROPERTY_NAME, String.class, 
                java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_NAME"),
                "",sp.getName()));
        
        generalPropertySet.put(new GeneralProperty(Constants.PROPERTY_DESCRIPTION, String.class, 
                java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_DESCRIPTION"),
                "",this, sp.getAttribute(Constants.PROPERTY_DESCRIPTION)));
        
        generalPropertySet.put(new NotEditableProperty(Constants.PROPERTY_NETWORKIP, String.class, 
                java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_NETWORK_IP"),
                "",sp.getAttribute(Constants.PROPERTY_NETWORKIP)));
        
        generalPropertySet.put(new NotEditableProperty(Constants.PROPERTY_BROADCASTIP, String.class, 
                java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_BROADCAST_IP"),
                "",sp.getAttribute(Constants.PROPERTY_BROADCASTIP)));
        
        generalPropertySet.put(new NotEditableProperty(Constants.PROPERTY_HOSTS, String.class, 
                java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_HOSTS"),
                "",sp.getAttribute(Constants.PROPERTY_HOSTS)));
        
        generalPropertySet.setName("1");
        generalPropertySet.setDisplayName(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_GENERAL_ATTRIBUTES"));
        sheet.put(generalPropertySet);
        return sheet;
    }
    
    @Override
    public boolean canRename() {
        return false;
    }
}

/*
 * Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

import com.neotropic.inventory.modules.ipam.engine.SubnetEngine;
import com.neotropic.inventory.modules.ipam.nodes.actions.AddIPAddressAction;
import com.neotropic.inventory.modules.ipam.nodes.actions.CreateSubnetAction;
import java.awt.Image;
import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import com.neotropic.inventory.modules.ipam.nodes.actions.DeleteSubnetAction;
import com.neotropic.inventory.modules.ipam.nodes.actions.RelateSubnetToVFRAction;
import com.neotropic.inventory.modules.ipam.nodes.actions.RelateSubnetToVlanAction;
import com.neotropic.inventory.modules.ipam.nodes.actions.ReleaseSubnetFromVlanAction;
import com.neotropic.inventory.modules.ipam.nodes.actions.ReleaseSubnetFromVFRAction;
import com.neotropic.inventory.modules.ipam.nodes.properties.GeneralProperty;
import com.neotropic.inventory.modules.ipam.nodes.properties.NotEditableProperty;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.ExecuteClassLevelReportAction;
import org.kuwaiba.management.services.nodes.actions.RelateToServiceAction;
import org.kuwaiba.management.services.nodes.actions.ReleaseFromServiceAction;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.PasteType;

/**
 * Represents a subnet 
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
public class SubnetNode extends ObjectNode {
    
    private static final String ICON_PATH="com/neotropic/inventory/modules/res/subnet-icon.png";
    private static Image defaultIcon = ImageUtilities.loadImage(ICON_PATH);
    
    /**
     * Should the context actions be available?
     */
    public SubnetNode(LocalObjectLight subnet) {
        super(subnet);
        setChildren(new SubnetChildren());
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            AddIPAddressAction.getInstance(),
            CreateSubnetAction.getInstance(),
            null,
            Lookup.getDefault().lookup(RelateToServiceAction.class),
            Lookup.getDefault().lookup(ReleaseFromServiceAction.class),
            RelateSubnetToVlanAction.getInstance(),
            ReleaseSubnetFromVlanAction.getInstance(),
            RelateSubnetToVFRAction.getInstance(),
            ReleaseSubnetFromVFRAction.getInstance(),
            null,
            DeleteSubnetAction.getInstance(),
            null,
            ExecuteClassLevelReportAction.getInstance()
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
        LocalObject sp = com.getObjectInfo(getObject().getClassName(), getObject().getId());
        Sheet sheet = Sheet.createDefault();
        Sheet.Set generalPropertySet = Sheet.createPropertiesSet(); //General attributes category
        
        
        generalPropertySet.put(new NotEditableProperty(Constants.PROPERTY_NAME, String.class, 
                I18N.gm("name"), "", sp.getName()));
        
        generalPropertySet.put(new GeneralProperty(Constants.PROPERTY_DESCRIPTION, String.class, 
                I18N.gm("description"), "",this, sp.getAttribute(Constants.PROPERTY_DESCRIPTION)));
        
        generalPropertySet.put(new NotEditableProperty(Constants.PROPERTY_NETWORKIP, String.class, 
                I18N.gm("network_ip_address"), "", sp.getAttribute(Constants.PROPERTY_NETWORKIP)));
        
        generalPropertySet.put(new NotEditableProperty(Constants.PROPERTY_BROADCASTIP, String.class, 
                I18N.gm("broadcast_ip_address"), "", sp.getAttribute(Constants.PROPERTY_BROADCASTIP)));
        
        generalPropertySet.put(new NotEditableProperty(Constants.PROPERTY_HOSTS, String.class, 
                I18N.gm("number_of_hosts"), "", sp.getAttribute(Constants.PROPERTY_HOSTS)));
        
        generalPropertySet.setName("1");
        generalPropertySet.setDisplayName(I18N.gm("general_attributes"));
        sheet.put(generalPropertySet);
        return sheet;
    }
    
    @Override
    public boolean canRename() {
        return false;
    }
    
    @Override
    public PasteType getDropType(Transferable _obj, final int action, int index) {
        final Node dropNode = NodeTransfer.node(_obj,
                NodeTransfer.DND_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
        
        if (dropNode == null) 
            return null;
        
        //Ignore those noisy attempts to move it to itself
        if (dropNode.getLookup().lookup(LocalObjectLight.class).equals(getObject()))
            return null;

        return new PasteType() {
            @Override
            public Transferable paste() throws IOException {
                try {
                    LocalObjectLight obj = dropNode.getLookup().lookup(LocalObjectLight.class);
                        //Check if the current object can contain the drop node
                    Node parentNode = null;
                    if (action == DnDConstants.ACTION_MOVE) {
                        String className = getObject().getClassName();
                        String oid = getObject().getId();
                        boolean networkIpBelongsTo = false;
                        boolean broadcastIpBelongsTo = false;
                            
                        LocalObject parentSubnet = com.getObjectInfo(className, oid);
                        String[] parentSplit = parentSubnet.getName().split("/");
                        LocalObject childNode = null;
                        
                        String parentNetworkIp = (String)parentSubnet.getAttribute("networkIp"); //NOI18N
                        String parentBroadcastIp = (String)parentSubnet.getAttribute("broadcastIp"); //NOI18N
                        
                        if(obj.getClassName().equals(Constants.CLASS_IP_ADDRESS)){
                            
                            if(className.equals(Constants.CLASS_SUBNET_IPV4))
                                networkIpBelongsTo = SubnetEngine.belongsTo(parentNetworkIp, obj.getName(), Integer.valueOf(parentSplit[1]));
                            
                            else if(className.equals(Constants.CLASS_SUBNET_IPV6))
                                networkIpBelongsTo = SubnetEngine.belongsToIpv6(parentNetworkIp, obj.getName(), Integer.valueOf(parentSplit[1]));
                            
                            if(networkIpBelongsTo){
                                if(com.moveObjectsToPool(className, oid, new LocalObjectLight[]{obj})){
                                    //Refreshes the old parent node
                                    if (dropNode.getParentNode().getChildren() instanceof AbstractChildren)
                                        ((AbstractChildren)dropNode.getParentNode().getChildren()).addNotify();

                                    //Refreshes the new parent node
                                    if (getChildren() instanceof AbstractChildren)
                                        ((AbstractChildren)getChildren()).addNotify();
                                }else
                                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
                            }
                            else
                                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, String.format(I18N.gm("ip_does_not_belong_to"), obj.getName(), getObject().getName()));
                        }
                        
                        else{ 
                            childNode = com.getObjectInfo(className, obj.getId());
                            String childNetworkIp = (String)childNode.getAttribute("networkIp"); //NOI18N
                            String childBroadcastIp = (String)childNode.getAttribute("broadcastIp"); //NOI18N
                        
                            if(className.equals(Constants.CLASS_SUBNET_IPV4)){
                                networkIpBelongsTo = SubnetEngine.belongsTo(parentNetworkIp, childNetworkIp, Integer.valueOf(parentSplit[1]));
                                broadcastIpBelongsTo = SubnetEngine.belongsTo(parentNetworkIp, childBroadcastIp, Integer.valueOf(parentSplit[1]));
                            }
                            else if(className.equals(Constants.CLASS_SUBNET_IPV6)){
                                networkIpBelongsTo = SubnetEngine.belongsToIpv6(parentNetworkIp, childNetworkIp, Integer.valueOf(parentSplit[1]));
                                broadcastIpBelongsTo = SubnetEngine.belongsToIpv6(parentNetworkIp, childBroadcastIp, Integer.valueOf(parentSplit[1]));
                            }
                            if(!networkIpBelongsTo && parentNetworkIp.equals(childNetworkIp))
                                networkIpBelongsTo =  true;
                            if(!broadcastIpBelongsTo && parentBroadcastIp.equals(childBroadcastIp))
                                broadcastIpBelongsTo = true;

                            if(networkIpBelongsTo && broadcastIpBelongsTo){
                                if(com.moveObjectsToPool(className, oid, new LocalObjectLight[]{obj})){
                                    //Refreshes the old parent node
                                    if (dropNode.getParentNode().getChildren() instanceof AbstractChildren)
                                        ((AbstractChildren)dropNode.getParentNode().getChildren()).addNotify();

                                    //Refreshes the new parent node
                                    if (getChildren() instanceof AbstractChildren)
                                        ((AbstractChildren)getChildren()).addNotify();
                                }else
                                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
                            }
                            else
                                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, String.format(I18N.gm("subnet_is_not_subnet_of"), obj.getName(), getObject().getName()));
                        }
                    }
                } catch (Exception ex) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getMessage());
                }
                return null;
            }
        };
    }
}

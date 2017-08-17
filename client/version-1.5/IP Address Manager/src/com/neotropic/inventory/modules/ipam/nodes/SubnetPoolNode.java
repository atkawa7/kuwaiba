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

import java.awt.Image;
import javax.swing.Action;
import com.neotropic.inventory.modules.ipam.nodes.actions.CreateSubnetAction;
import com.neotropic.inventory.modules.ipam.nodes.actions.CreateSubnetPoolAction;
import com.neotropic.inventory.modules.ipam.nodes.actions.DeleteSubnetPoolAction;
import com.neotropic.inventory.modules.ipam.nodes.properties.PoolNativeTypeProperty;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * Represent a pool of subnets(IPv4, IPv6) or a pool of VLANs.
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SubnetPoolNode extends AbstractNode implements PropertyChangeListener{
    
    private static final String ICON_PATH="com/neotropic/inventory/modules/res/folder-icon.png";
    private static Image defaultIcon = ImageUtilities.loadImage(ICON_PATH);
    protected Sheet sheet;
    private LocalPool subnetPool;
    private CommunicationsStub com;
        
    public SubnetPoolNode(LocalPool subnetPool) {
        super(new SubnetPoolChildren(), Lookups.singleton(subnetPool));
        this.subnetPool = subnetPool;
        this.subnetPool.addPropertyChangeListener(this);
        com = CommunicationsStub.getInstance();
        setChildren(new SubnetPoolChildren());
    }
    
    @Override
    public Action[] getActions(boolean context) {
        
        List<Action> actions = new ArrayList<>();
        
        actions.add(CreateSubnetAction.getInstance());
        actions.add(CreateSubnetPoolAction.getInstance());
        actions.add(null);
        if (!(getParentNode() instanceof IPAMRootNode)) {
            actions.add(DeleteSubnetPoolAction.getInstance());
            actions.add(null); //Separator
        }
            
        return actions.toArray(new Action[]{});
    }
         
    @Override
    public String getName(){
        return subnetPool.toString();
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
        LocalPool sp = com.getSubnetPool(getSubnetPool().getOid());
        sheet = Sheet.createDefault();
        Sheet.Set generalPropertySet = Sheet.createPropertiesSet(); //General attributes category
        
        generalPropertySet.put(new PoolNativeTypeProperty(
                Constants.PROPERTY_NAME, String.class, 
                java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_NAME"),
                "",this, sp.getName()));
        
        generalPropertySet.put(new PoolNativeTypeProperty(Constants.PROPERTY_DESCRIPTION, String.class, 
                java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_DESCRIPTION"),
                "", this, sp.getDescription()));

        generalPropertySet.setName("1");
        generalPropertySet.setDisplayName(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_GENERAL_ATTRIBUTES"));
        sheet.put(generalPropertySet);
        return sheet;
    }

    public LocalPool getSubnetPool() {
        return subnetPool;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
         if (evt.getPropertyName().equals(Constants.PROPERTY_NAME))
            fireNameChange("", (String)evt.getNewValue());
    }
    
    @Override
    public PasteType getDropType(Transferable _obj, final int action, int index) {
        final Node dropNode = NodeTransfer.node(_obj,
                NodeTransfer.DND_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
        
        if (dropNode == null) 
            return null;
        
        //Ignore those noisy attempts to move it to itself
        if (dropNode.getLookup().lookup(LocalObjectLight.class).equals(subnetPool))
            return null;
        
        return new PasteType() {

            @Override
            public Transferable paste() throws IOException {
                try {
                    LocalObjectLight obj = dropNode.getLookup().lookup(LocalObjectLight.class);
                    //Check if the current object can contain the drop node
                    Node parentNode = null;
                    if (action == DnDConstants.ACTION_MOVE) {
                        String className = getSubnetPool().getClassName();
                        int type = getSubnetPool().getType();
                        long oid = getSubnetPool().getOid();
                        
                        if(className.equals(obj.getClassName())){
                            if(com.moveObjectsToPool(className, oid, new LocalObjectLight[]{obj})){
                                //Refreshes the old parent node
                                if (dropNode.getParentNode().getChildren() instanceof AbstractChildren)
                                    ((AbstractChildren)dropNode.getParentNode().getChildren()).addNotify();

                                //Refreshes the new parent node
                                if (getChildren() instanceof AbstractChildren)
                                    ((AbstractChildren)getChildren()).addNotify();
                            }else
                                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                        }
                    }
                } catch (Exception ex) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, ex.getMessage());
                }
                return null;
            }
        };
    }
}

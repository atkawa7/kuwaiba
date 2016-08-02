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

import java.awt.Image;
import javax.swing.Action;
import com.neotropic.inventory.modules.ipam.nodes.actions.CreateSubnetAction;
import com.neotropic.inventory.modules.ipam.nodes.actions.CreateSubnetPoolAction;
import com.neotropic.inventory.modules.ipam.nodes.actions.DeleteSubnetPoolAction;
import com.neotropic.inventory.modules.ipam.nodes.properties.PoolNativeTypeProperty;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.util.Constants;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 * Represent a pool of subnets.
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SubnetPoolNode extends AbstractNode implements PropertyChangeListener{
    
    private static final String ICON_PATH="com/neotropic/inventory/modules/res/folder-icon.png";
    private static Image defaultIcon = ImageUtilities.loadImage(ICON_PATH);
    protected Sheet sheet;
    private LocalPool subnetPool;
    protected CommunicationsStub com;

    public SubnetPoolNode(LocalPool subnetPool) {
        super(new SubnetPoolChildren(), Lookups.singleton(subnetPool));
        this.subnetPool = subnetPool;
        this.subnetPool.addPropertyChangeListener(this);
        com = CommunicationsStub.getInstance();
        setChildren(new SubnetPoolChildren());
    }
    
    @Override
    public Action[] getActions(boolean context){
        List<Action> actions = new ArrayList<>();
        
        actions.add(new CreateSubnetAction(this));
        actions.add(new CreateSubnetPoolAction(this));
        actions.add(null);
        if (!(getParentNode() instanceof IPAMRootNode)) {
            actions.add(new DeleteSubnetPoolAction());
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
}

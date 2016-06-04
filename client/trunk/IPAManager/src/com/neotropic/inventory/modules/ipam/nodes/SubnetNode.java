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
import org.inventory.communications.core.LocalObjectLight;
import com.neotropic.inventory.modules.ipam.nodes.actions.CreateSubnetAction;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;

/**
 *
 * @author adrian
 */
public class SubnetNode extends ObjectNode{
    
    private static final String ICON_PATH="com/neotropic/inventory/modules/res/subnet-icon.png";
    private static Image defaultIcon = ImageUtilities.loadImage(ICON_PATH);
    
    private LocalObjectLight subnet;

    public SubnetNode(LocalObjectLight subnet) {
        super(subnet);
        this.subnet = subnet;
    }
    
    @Override
    public Action[] getActions(boolean context){
        return new Action[]{new CreateSubnetAction(this)};
    }
    
    @Override
    public String getName(){
        return subnet.getName() +" ["+subnet.getClassName()+"]";
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
        return Sheet.createDefault();
    }
    
    @Override
    public boolean canRename() {
        return false;
    }

    public LocalObjectLight getSubnet() {
        return subnet;
    }
}

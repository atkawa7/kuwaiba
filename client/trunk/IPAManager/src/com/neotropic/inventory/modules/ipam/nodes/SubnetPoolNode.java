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
import com.neotropic.inventory.modules.ipam.nodes.actions.CreateSubnetPoolAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;

/**
 * Represent a pool of subnets.
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SubnetPoolNode extends AbstractNode{
    
    private static final String ICON_PATH="com/neotropic/inventory/modules/res/folder-icon.png";
    private static Image defaultIcon = ImageUtilities.loadImage(ICON_PATH);
    
    private LocalObjectLight subnetPool;
    
    public SubnetPoolNode(LocalObjectLight subnetPool) {
        super(new SubnetPoolChildren(subnetPool));
        this.subnetPool = subnetPool;
    }
    
    @Override
    public Action[] getActions(boolean context){
        return new Action[]{
            new CreateSubnetAction(this), 
            new CreateSubnetPoolAction(this)
        };
    }
    
    @Override
    public String getName(){
        return subnetPool.getName() +" ["+subnetPool.getClassName()+"]";
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

    public LocalObjectLight getSubnetPool() {
        return subnetPool;
    }
    
    
    
}

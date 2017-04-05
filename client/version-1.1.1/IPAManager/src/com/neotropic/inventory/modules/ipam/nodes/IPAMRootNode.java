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
import org.inventory.communications.core.LocalPool;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

/**
 * This is the root node for all the IPAM Nodes, this is not visible
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class IPAMRootNode extends AbstractNode{
    
    private static final String ICON_PATH="org/inventory/navigation/applicationnodes/res/folder-icon.png";
    private static Image defaultIcon = ImageUtilities.loadImage(ICON_PATH);

    public IPAMRootNode(LocalPool[] subnetPools) {
        super (new Children.Array());
        for (LocalPool subnetPool : subnetPools)
            getChildren().add(new SubnetPoolNode[] { new SubnetPoolNode(subnetPool)});
    }
   
    @Override
    public Image getIcon(int i){
        return defaultIcon;
    }

    @Override
    public Image getOpenedIcon(int i){
        return getIcon(i);
    }    
}

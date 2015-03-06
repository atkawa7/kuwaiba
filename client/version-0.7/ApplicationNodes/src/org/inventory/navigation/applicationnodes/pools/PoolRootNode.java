/**
 * Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.pools;

import java.awt.Image;
import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.applicationnodes.pools.actions.NewPoolAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

/**
 * This is the root node in the pools tree
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PoolRootNode extends AbstractNode {
    
    public static final String ICON_PATH="org/inventory/navigation/applicationnodes/res/root.png";
    private static Image defaultIcon = ImageUtilities.loadImage(ICON_PATH);

    public PoolRootNode (LocalObjectLight[] pools){
        super (new Children.Array());
        setName(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_POOLS"));
        for (LocalObjectLight pool : pools)
            getChildren().add(new PoolNode[] { new PoolNode(pool)});
    }
    
    @Override
    public Action[] getActions(boolean context){
        return new Action[]{new NewPoolAction(this)};
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
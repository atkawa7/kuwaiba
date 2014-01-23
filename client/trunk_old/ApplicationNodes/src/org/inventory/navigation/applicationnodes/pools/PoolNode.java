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
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.actions.ShowObjectIdAction;
import org.inventory.navigation.applicationnodes.pools.actions.DeletePoolAction;
import org.inventory.navigation.applicationnodes.pools.actions.NewPoolItemAction;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
/**
 * Represents a pool (a set of objects of a certain kind)
 * @author Charles edward Bedon Cortazar <charles.bedon@neotropic.co>
 */
public class PoolNode extends AbstractNode {
    
    private static Image defaultIcon = ImageUtilities.loadImage("org/inventory/navigation/applicationnodes/res/pool.png");
    private LocalObjectLight pool;
    private NewPoolItemAction newPoolItemAction;
    private DeletePoolAction deletePoolAction;
    private ShowObjectIdAction showObjectIdAction;

    public PoolNode(LocalObjectLight lol) {
        super(new PoolChildren(lol));
        this.pool = lol;
    }
    
    @Override
    public String getName(){
        return pool.getName() +" ["+pool.getClassName()+"]";
    }
    
    public LocalObjectLight getPool(){
        return pool;
    }
    
    @Override
    public Action[] getActions(boolean context){
        if (newPoolItemAction == null){
            newPoolItemAction = new NewPoolItemAction(this);
            deletePoolAction = new DeletePoolAction(this);
            showObjectIdAction = new ShowObjectIdAction (pool.getOid(), pool.getClassName());
        }
        return new Action[]{newPoolItemAction, deletePoolAction, showObjectIdAction};
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
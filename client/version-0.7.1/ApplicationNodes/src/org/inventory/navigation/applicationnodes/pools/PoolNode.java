/*
 * Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.actions.ShowObjectIdAction;
import org.inventory.navigation.applicationnodes.pools.actions.DeletePoolAction;
import org.inventory.navigation.applicationnodes.pools.actions.NewPoolItemAction;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
/**
 * Represents a pool (a set of objects of a certain kind)
 * @author Charles edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PoolNode extends ObjectNode {
    
    private static Image defaultIcon = ImageUtilities.loadImage("org/inventory/navigation/applicationnodes/res/pool.png");
    private NewPoolItemAction newPoolItemAction;
    private DeletePoolAction deletePoolAction;
    
    public PoolNode(LocalObjectLight pool) {
        super(pool);
        this.object = pool;
        setChildren(new PoolChildren(pool));
    }
    
    @Override
    public String getName(){
        return object.getName() +" ["+object.getClassName()+"]";
    }
    
    @Override
    public Action[] getActions(boolean context){
        if (newPoolItemAction == null){
            newPoolItemAction = new NewPoolItemAction(this);
            deletePoolAction = new DeletePoolAction(this);
            showObjectIdAction = new ShowObjectIdAction (object.getOid(), object.getClassName());
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
    
    @Override
    protected Sheet createSheet(){
        sheet = Sheet.createDefault();
        return sheet;
    }
}
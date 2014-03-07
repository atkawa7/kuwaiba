/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kuwaiba.management.services.nodes;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.actions.ShowObjectIdAction;
import org.inventory.navigation.applicationnodes.pools.PoolNode;
import org.kuwaiba.management.services.nodes.actions.CreateCustomerAction;
import org.kuwaiba.management.services.nodes.actions.CreateServiceAction;
import org.kuwaiba.management.services.nodes.actions.DeleteCustomersPoolAction;
import org.openide.nodes.NodeTransfer;
import org.openide.util.ImageUtilities;
import org.openide.util.datatransfer.PasteType;

/**
 * Represents a pool (a set of customers)
 * @author adrian martinez molina <adrian.martinez@kuwaiba.org>
 */
public class CustomersPoolNode extends PoolNode{

    private static Image icon = ImageUtilities.loadImage("org/kuwaiba/management/services/res/customersPool.png");
    private CreateCustomerAction createCustomerAction;
    private DeleteCustomersPoolAction deleteCustomersPoolAction;
    private CreateServiceAction createServiceAction;
    private  ShowObjectIdAction showObjectIdAction; 
    
    public CustomersPoolNode(LocalObjectLight customer) {
        super(customer);
        this.pool = customer;
        setChildren(new CustomersPoolChildren(customer));
    }
    
    @Override
    public String getName(){
        return pool.getName() +" ["+java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_CUSTOMERS_POOL")+"]";
    }
    
    @Override
    public Action[] getActions(boolean context){
        List<Action> actions = new ArrayList<Action>();
        if(createCustomerAction == null){
            createCustomerAction= new CreateCustomerAction(this);
            createCustomerAction.setObject(pool);
            actions.add(createCustomerAction);
        }
        actions.add(createServiceAction == null ? createServiceAction = new CreateServiceAction(this) : createServiceAction);
        actions.add(null);
        actions.add(deleteCustomersPoolAction == null ? deleteCustomersPoolAction = new DeleteCustomersPoolAction(this) : deleteCustomersPoolAction);
        actions.add(null);
        actions.add(showObjectIdAction == null ? showObjectIdAction = new ShowObjectIdAction(pool.getOid(), pool.getClassName()) : showObjectIdAction);
        return actions.toArray(new Action[]{});
    }
   
    @Override
    public Image getIcon(int i){
        return icon;
    }

    @Override
    public Image getOpenedIcon(int i){
        return getIcon(i);
    }
    
}

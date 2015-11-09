/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.actions.DeleteBusinessObjectAction;
import org.inventory.navigation.applicationnodes.objectnodes.actions.ShowObjectIdAction;
import org.kuwaiba.management.services.nodes.actions.CreateServiceAction;
import org.kuwaiba.management.services.nodes.actions.CreateServicesPoolAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * Node representing a customer
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class CustomerNode extends ObjectNode {
    /**
     * icon node
     */
    private static Image icon = ImageUtilities.loadImage("org/kuwaiba/management/services/res/customer.png");
    
    private CreateServiceAction createServiceAction;
    private CreateServicesPoolAction createServicesPoolAction;
    private NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
    
    public CustomerNode(LocalObjectLight customer) {
        super(customer);
        this.object = customer;
        setChildren(new CustomerChildren(customer));
    }
    
    @Override
    public Action[] getActions(boolean context){
        List<Action> actions = new ArrayList<Action>();

        actions.add(createServiceAction == null ? createServiceAction = new CreateServiceAction(this) : createServiceAction);
        if(createServicesPoolAction == null){
            createServicesPoolAction = new CreateServicesPoolAction(this);
            createServicesPoolAction.setObject(object);
            actions.add(createServicesPoolAction);
        }
        else
            actions.add(createServicesPoolAction);
        actions.add(new DeleteBusinessObjectAction(this));
        actions.add(null); //Separator
        actions.add(explorerAction);
        actions.add(null); //Separator
        actions.add(showObjectIdAction == null ? showObjectIdAction = new ShowObjectIdAction(object.getOid(), object.getClassName()) : showObjectIdAction);
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

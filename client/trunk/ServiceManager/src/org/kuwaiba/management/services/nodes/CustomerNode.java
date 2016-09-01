/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.actions.ShowObjectIdAction;
import org.kuwaiba.management.services.nodes.actions.ServiceManagerActionFactory;

/**
 * Node representing a customer
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class CustomerNode extends ObjectNode {
    
    public CustomerNode(LocalObjectLight customer) {
        super(customer);
        this.object = customer;
        setChildren(new CustomerChildren());
    }
    
    @Override
    public Action[] getActions(boolean context) {
        
        return new Action [] { ServiceManagerActionFactory.getCreateServicePoolAction(),
            ServiceManagerActionFactory.getDeleteCustomerAction(),
            showObjectIdAction == null ? showObjectIdAction = new ShowObjectIdAction(object.getOid(), object.getClassName()) : showObjectIdAction
        };        
    }
}

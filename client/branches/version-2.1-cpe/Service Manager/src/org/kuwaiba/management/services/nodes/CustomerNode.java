/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.core.contacts.actions.AddContactAction;
import org.inventory.core.contacts.actions.ShowContactsAction;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.ShowMoreInformationAction;
import org.inventory.navigation.special.attachments.nodes.actions.AttachFileAction;
import org.kuwaiba.management.services.nodes.actions.ServiceManagerActionFactory;
import org.openide.util.Lookup;

/**
 * Node representing a customer
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class CustomerNode extends ObjectNode {
    
    public CustomerNode(LocalObjectLight customer) {
        super(customer);
        setChildren(new CustomerChildren());
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action [] { ServiceManagerActionFactory.getCreateServicePoolAction(),
            ServiceManagerActionFactory.getDeleteCustomerAction(),
            null,
            Lookup.getDefault().lookup(AttachFileAction.class),
            Lookup.getDefault().lookup(AddContactAction.class),
            Lookup.getDefault().lookup(ShowContactsAction.class),
            null,
            ShowMoreInformationAction.getInstance(getObject().getId(), getObject().getClassName())
        };        
    }
}

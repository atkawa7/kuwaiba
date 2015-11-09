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
import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import org.kuwaiba.management.services.nodes.actions.CreateCustomerAction;
import org.kuwaiba.management.services.nodes.actions.CreateCustomersPoolAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

/**
 * Node representing the service manager root
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ServiceManagerRootNode extends AbstractNode {
    private Image icon;
  
    public ServiceManagerRootNode(LocalObjectLight[] customers) {
        super(new Children.Array());
        icon = ImageUtilities.loadImage("org/kuwaiba/management/services/res/root.png");
        setDisplayName("Root");
        for (LocalObjectLight customer : customers){
            if(customer.getClassName().equals("GenericCustomer"))
                getChildren().add(new CustomersPoolNode[]{new CustomersPoolNode(customer)});
            else
                getChildren().add(new CustomerNode[]{new CustomerNode(customer)});
        }
    }

    @Override
    public Action[] getActions(boolean context){
        return new Action[]{
            new CreateCustomerAction(this), 
            new CreateCustomersPoolAction(this)
        };
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

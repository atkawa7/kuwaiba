/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.kuwaiba.management.services.nodes.actions.ServiceManagerActionFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 * Node representing the service manager root
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ServiceManagerRootNode extends AbstractNode {
    private Image icon = ImageUtilities.loadImage("org/kuwaiba/management/services/res/root.png");
  
    public ServiceManagerRootNode(ServiceManagerRootChildren children) {
        super(children);
        setDisplayName("Service Manager");
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{ ServiceManagerActionFactory.getCreateCustomerPoolAction() };
    }
    
    @Override
    public Image getIcon(int i){
        return icon;
    }
    
    @Override
    public Image getOpenedIcon(int i){
        return getIcon(i);
    }
    
    public static class ServiceManagerRootChildren extends Children.Keys <LocalPool> {

        @Override
        public void addNotify() {
            List<LocalPool> customerPools = CommunicationsStub.getInstance().getRootPools(
                    Constants.CLASS_GENERICCUSTOMER, LocalPool.POOL_TYPE_MODULE_ROOT, false);

            if (customerPools == null)
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
            else {
                Collections.sort(customerPools);
                setKeys(customerPools);
            }
        }
        
        @Override
        protected Node[] createNodes(LocalPool key) {
            return new Node[] { new CustomerPoolNode(key) };
        }
    }
}

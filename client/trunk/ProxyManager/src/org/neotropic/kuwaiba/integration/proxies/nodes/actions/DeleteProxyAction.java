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

package org.neotropic.kuwaiba.integration.proxies.nodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalInventoryProxy;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.neotropic.kuwaiba.integration.proxies.nodes.ProxyNode;
import org.neotropic.kuwaiba.integration.proxies.nodes.ProxyPoolNode;
import org.openide.util.Utilities;

/**
 * Deletes an inventory proxy. Related objects are left untouched.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class DeleteProxyAction extends GenericInventoryAction {

    public DeleteProxyAction() {
        putValue(NAME, "Delete Inventory Proxy");
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PROXIES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this proxy? Related projects and inventory elements will remain untouched", 
                "Delete Proxy", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
            return;
        
        ProxyNode selectedNode = Utilities.actionsGlobalContext().lookup(ProxyNode.class);
        if (selectedNode != null) {
            
            if (CommunicationsStub.getInstance().deleteProxy(selectedNode.getLookup().lookup(LocalInventoryProxy.class).getClassName(), 
                    selectedNode.getLookup().lookup(LocalInventoryProxy.class).getId()))
                ((ProxyPoolNode.ProxyPoolNodeChildren)selectedNode.getParentNode().getChildren()).addNotify();
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }

}

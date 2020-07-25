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
import org.inventory.communications.core.LocalPool;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.neotropic.kuwaiba.integration.proxies.nodes.ProxyPoolNode;
import org.neotropic.kuwaiba.integration.proxies.nodes.ProxyRootNode;
import org.openide.util.Utilities;

/**
 * Deletes a proxy pool and the proxies within.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class DeleteProxyPoolAction extends GenericInventoryAction {

    public DeleteProxyPoolAction() {
        putValue(NAME, "Delete Inventory Proxy Pool");
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PROXIES, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this proxy pool? Related projects and inventory elements will remain untouched", 
                "Delete Proxy Pool", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
            return;
        
        ProxyPoolNode selectedNode = Utilities.actionsGlobalContext().lookup(ProxyPoolNode.class);
        if (selectedNode != null) {
            if (CommunicationsStub.getInstance().deleteProxyPool(selectedNode.getLookup().lookup(LocalPool.class).getId()))
                ((ProxyRootNode.ProxiesRootChildren)selectedNode.getParentNode().getChildren()).addNotify();
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
}
